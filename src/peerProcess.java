import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;


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
