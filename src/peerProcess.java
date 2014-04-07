import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


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
