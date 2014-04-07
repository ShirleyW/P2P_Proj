import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class peerProcess implements Runnable{
	private int myId;
	private int numNeighbors;
	
	private Configuration config;
	private FileHandle fileHandle;
	private P2pLogger logger;
	private BitField bitfield;
	private NeighborRecords[] neighbor;

	public peerProcess(int myID) throws UnknownHostException, IOException {
		
		this.myId = myID;
		this.config = new Configuration("Common.cfg", "PeerInfo.cfg");
		this.fileHandle = new FileHandle(this.config, this.myId);
		this.logger = new P2pLogger(myID);
		this.bitfield = new BitField(config.getNumPieces());
		this.numNeighbors = config.getNumPeers() - 1;
		neighbor = new NeighborRecords[numNeighbors];
//		System.out.println("peerProecess 28: peerProcess construction finished");
	}
	
	public void getInit(ServerSocket downServerSkt, ServerSocket upServerSkt, ServerSocket controlServerSkt, int index)throws IOException{
		Calendar cal;

		Socket downSkt=downServerSkt.accept();
		Socket upSkt=upServerSkt.accept();
		Socket controlSkt=controlServerSkt.accept();

		HandShakeMessage handShake=new HandShakeMessage();
		handShake.receive(downSkt);
		int msgID=handShake.getID();
		neighbor[index]=new NeighborRecords(msgID,config.getNumPieces(),downSkt,upSkt,controlSkt);
		handShake.setID(myId);
		handShake.send(downSkt);

		Message bitFieldMsg=new Message();
		bitFieldMsg.receive(downSkt.getInputStream());
		BitField neighborBitField=new BitField(config.getNumPieces());
		neighborBitField.setBitField(bitFieldMsg.getPayload());
		neighbor[index].setBitField(neighborBitField);
		bitFieldMsg.setType(Message.bitfield);
		bitFieldMsg.setPayload(bitfield.byteField());
		bitFieldMsg.send(downSkt.getOutputStream());

		Message interestMsg=new Message();
		interestMsg.receive(downSkt.getInputStream());
		if(interestMsg.getType()==Message.interested)
		{
			cal=Calendar.getInstance();
			logger.interestedLog(msgID, cal);
		}
		else
		{
			cal=Calendar.getInstance();
			logger.notInterestedLog(msgID, cal);
		}
		if(bitfield.interested(neighbor[index].getBitField()))
			interestMsg.setType(Message.interested);
		else
			interestMsg.setType(Message.notinterested);
		interestMsg.setPayload(null);
		interestMsg.send(downSkt.getOutputStream());


	}
	
	public void initialization(NeighborRecords record) throws Exception {

		System.out.println("peerProcess: peer " + this.myId + " initialize");
		HandShakeMessage handshake = new HandShakeMessage();
		handshake.setID(myId);

		Socket socket = record.getUploadSocket();
		handshake.send(socket);
		handshake.receive(socket);

		if (handshake.getID() != record.getId()) {
			throw new Exception("Hand-shaking fails");
		}
//		shake.read(record.getDownloadSocket());
//		System.out.println("peerProcess:45: peer " + this.myID + " receives test hand shake message");
		Message bitFieldMessage = new Message();
		//send bit field
//		System.out.println("peerProcess:69:initialization: sends mybit field " + myBitField.getText() + " to peer " + record.getID());
		bitFieldMessage.setType(Message.bitfield);
		bitFieldMessage.setPayload(bitfield.byteField());
		bitFieldMessage.send(socket.getOutputStream());

		//receive bit field
		bitFieldMessage.receive(socket.getInputStream());
		BitField bitF = new BitField(config.getNumPieces());
		bitF.setBitField(bitFieldMessage.getPayload());
		record.setBitField(bitF);
//		System.out.println("peerProcess:79:initialization: recieves bit field " + bitField.getText() + " from peer " + record.getID());

		Message interestmessage = new Message();
		//send interest or not
		interestmessage.setPayload(null);
		if (bitfield.interested(bitF)) {
			interestmessage.setType(Message.interested);
		} else {
			interestmessage.setType(Message.notinterested);
		}
		interestmessage.send(socket.getOutputStream());
		//receive interest or not
		interestmessage.receive(socket.getInputStream());
		Calendar cal = Calendar.getInstance();
		if (interestmessage.getType() == Message.interested) {
			logger.interestedLog(record.getId(), cal);
		} else {
			logger.notInterestedLog(record.getId(), cal);
		}
//		System.out.println("peerPrecess: 71:peer " + this.myID + " finishes initialization");

	}


	public void unchokeControl() throws InterruptedException, ExecutionException, IOException {
		ArrayList<NeighborRecords> inorderInterestingPeers = new ArrayList<NeighborRecords>();
		ExecutorService uploadThreadPool = Executors.newFixedThreadPool(config.getNumPreNeighbor());
		
		while (true) {
			
			inorderInterestingPeers.clear();
			int numFinished = 0;
			
			for (int i = 0; i < numNeighbors; i++) {
				neighbor[i].compareAndSetChokeState(1, 0);
				if (neighbor[i].isFinished()) {
					numFinished++;
				}
			}
			
			if (numFinished == numNeighbors) {
				break;
			}

			for (int i = 0; i < numNeighbors; i++) {
				if ((neighbor[i].getBitField().interested(bitfield))) {
					inorderInterestingPeers.add(neighbor[i]);
					
					if (inorderInterestingPeers.size() == 1) {
						continue;
					}

					int k = inorderInterestingPeers.size() - 1;
					int j = k - 1;
					
					while (j >= 0 && inorderInterestingPeers.get(k).getDownload() > inorderInterestingPeers.get(j).getDownload()) {
						
						NeighborRecords temp = inorderInterestingPeers.get(k);
						inorderInterestingPeers.set(k, inorderInterestingPeers.get(j));
						inorderInterestingPeers.set(j, temp);
						k = j;
						j--;
					}
				}
				
				
			}
			
			if (inorderInterestingPeers.size() == 0) {
				Thread.sleep(1000);
				continue;
			}
			
			for (NeighborRecords r : neighbor) {
				r.clearDownload();
			}
			
			int count = 0;
			ArrayList<Future<Object>> unChokePeers = new ArrayList<Future<Object>>();
			int[] peerIds= new int[config.getNumPreNeighbor()];
			for (NeighborRecords r : inorderInterestingPeers) {
				if(count > config.getNumPreNeighbor()){
					break;
				}
				else if (r.compareAndSetChokeState(0, 1)) {
					//|| r.compareAndSetChokeState(2, 1)
					Future<Object> f = uploadThreadPool.submit(new UnChoke(r, fileHandle, config.getUnChokeTime(), logger));
					unChokePeers.add(f);
					peerIds[count]=r.getId();
					count ++;
				} 
				
			}
			
			if (count > 0) {
				Calendar cal = Calendar.getInstance();
				logger.changePrefNeighborsLog(peerIds, cal);
			}
			for (Future<Object> f : unChokePeers) {
				f.get();
			}
		}
		uploadThreadPool.shutdownNow();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	public static void main(String args[]) throws Exception {
		
		peerProcess peer = new peerProcess(Integer.parseInt(args[0]));
		Thread t = new Thread(peer);
		t.start();
	}

}
