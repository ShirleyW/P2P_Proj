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
		try {
			
			for (int i = 0; i < config.getNumPeers(); i++) {
				if (config.getID().get(i) == myId) {
					if (config.getHasFile().get(i)) {
						bitfield.setAllBitsTrue();
						break;
					}
				}
			}
			int index=0;
			for (int i = 0; i < config.getNumPeers(); i ++) {
				
				if (config.getID().get(i) == myId) {
					index=i;
					break;
				}
				Socket socketD = new Socket(config.getHostName().get(i), config.getDownPort().get(i));	
				Socket socketU = new Socket(config.getHostName().get(i), config.getUpPort().get(i));
				Socket socketC = new Socket(config.getHostName().get(i), config.getHavePort().get(i));
				Calendar cal = Calendar.getInstance();
				logger.tcpConnectionLog(config.getID().get(i), cal);
				neighbor[i] = new NeighborRecords(config.getNumPieces(),  config.getID().get(i), socketD, socketU, socketC);
				initialization(neighbor[i]);//handshake and bitfield
			}

//			if (index != config.getNumPeers() - 1) {
				
//				System.out.println("peerProcess: 243: peer " + this.myID +  " starts to listern to the port");
				ServerSocket downloadServSoc = new ServerSocket(config.getDownPort().get(index));
				ServerSocket uploadServSoc = new ServerSocket(config.getUpPort().get(index));
				ServerSocket haveServSoc = new ServerSocket(config.getHavePort().get(index));
				
				for (int i = index; i < config.getNumPeers() - 1; i++) {
					getInit(downloadServSoc, uploadServSoc, haveServSoc,i);
//				}
			}

			ExecutorService downloadThreadPool = Executors.newFixedThreadPool(numNeighbors);
			ArrayList<Future<Object>> downloadReturn = new ArrayList<Future<Object>>();
			
			ExecutorService haveThreadPool = Executors.newFixedThreadPool(numNeighbors);
			ArrayList<Future<Object>> haveReturn = new ArrayList<Future<Object>>();
			
			//start download and have listening
			for (int i = 0; i < numNeighbors; i++) {
				NeighborRecords r = neighbor[i];
				Future<Object> f1 =  downloadThreadPool.submit(new Download( r, neighbor, bitfield, fileHandle, this.myId,logger));
				downloadReturn.add(f1);
				Future<Object> f2 = haveThreadPool.submit(new ControlListener(this.myId, r, logger, numNeighbors));
				haveReturn.add(f2);
			}
			
			//start opt unchoking upload
			ExecutorService OptUpload = Executors.newSingleThreadExecutor();
			Future<Object> OptUploadResult = OptUpload.submit(new OptUnchoking(this.myId, bitfield, neighbor, fileHandle, config.getOptUnChokeTime(), logger));
				
            //start unchoking upload and wait for its return
			////bad design: wait for every peer finished and then return
			unchokeControl();
			
			//wait for opt unload return
			OptUploadResult.get();
			
			//send "stop" message to neighbors
			Message msg = new Message();
			msg.setType(Message.stop);
			msg.setPayload(null);
			for (int i = 0; i < numNeighbors; i++) {
				msg.send(neighbor[i].getUploadSocket().getOutputStream());
			}
			
			//waiting for all download stop
			
			for (int i = 0; i < numNeighbors; i++) {
				
				downloadReturn.get(i).get();
				haveReturn.get(i).get();
				
			}
			Calendar cal = Calendar.getInstance();
			////no meaning because of last unchoking bad design
			logger.completionLog(myId, cal);;
			downloadThreadPool.shutdownNow();
			OptUpload.shutdownNow();
			haveThreadPool.shutdownNow();
//			if (index != config.getNumPeers() - 1) {
				downloadServSoc.close();
				uploadServSoc.close();
				haveServSoc.close();
//			} 

			for (int i = 0; i < numNeighbors; i++) {
				neighbor[i].getDownloadSocket().close();
				neighbor[i].getUploadSocket().close();
				neighbor[i].getControlSocket().close();
			}
			
//			System.out.println("peerProecess:342: peerProcess quit: peer " + this.myID);
//			System.out.println("peerProcesss:343: thread profile:");
//			Profiler p = new Profiler();
//			p.profile();
//			System.out.println("peer " + this.myId + " finishes downloading");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	public static void main(String args[]) throws Exception {
		
		peerProcess peer = new peerProcess(Integer.parseInt(args[0]));
		Thread t = new Thread(peer);
		t.start();
	}

}
