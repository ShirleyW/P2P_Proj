import java.io.IOException;
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
