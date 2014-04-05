import java.io.IOException;
import java.net.UnknownHostException;


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
