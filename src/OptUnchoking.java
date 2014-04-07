import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.Callable;


public class OptUnchoking implements Callable<Object> {
	private BitField bitfield;
	private NeighborRecords[] neighbors;
	private FileHandle filehandle;
	private P2pLogger logger;
	private int interval;
	private int id;
	private static final int TRYTIME=10;
	
	public OptUnchoking(int id, BitField bitfield, NeighborRecords[] neighbors, FileHandle filehandle, int interval, P2pLogger logger){
		this.bitfield=bitfield;
		this.neighbors=neighbors;
		this.filehandle=filehandle;
		this.logger=logger;
		this.interval=interval;
		this.id=id;
		
	}

	@Override
	public Object call() throws Exception {
		System.out.println("OptUnchoking: peer " + this.id + " starts optimal uploading");
		int count = 0;
		int chokenum = 0;
		int unchokenum = 0;
	
		while (true) {
			boolean allFinished = true;
			//continue upload until all neighbors has finished download.
			for (int i = 0; i < neighbors.length; i++) {
				if (!neighbors[i].getBitField().isFinished()) {
					allFinished = false;
					//if there exist unfinished neighbors, let allFinished be false
					break;
				}
			}
			if (allFinished) {
				System.out.println("OptUnchoking: peer " + this.id + ": all its neighbors finished");
				break; //if your neighbors finish downloading, stop this "while" loop
			}
			int optNeighborIndex = -1;
			boolean found = false;
			
//			while(!found){
//				optNeighborIndex=(int)(Math.random()*neighbors.length);
//				//selected from neighbors choked and interested in this peer's data
//				if ((neighbors[optNeighborIndex].getBitField().interested(bitfield)) && neighbors[optNeighborIndex].compareAndSetChokeState(0, 2)) {
//					found = true; 
//					System.out.println("OptUnchoking: peer" + this.id +"current optimal neighbor is"+neighbors[optNeighborIndex].getId());
//				}
//				
//			}
			
			for (int i = 0; i < TRYTIME; i++) {
				optNeighborIndex=(int)(Math.random()*neighbors.length);
				if ((neighbors[optNeighborIndex].getBitField().interested(bitfield)) && neighbors[optNeighborIndex].compareAndSetChokeState(0, 2)) {
					found = true; 
					break;
				}
			}
			if (!found) {
				Thread.sleep(1000);
				System.out.println("OptUnchoking: not found");
				continue;
			}
			
			Calendar cal = Calendar.getInstance();
			NeighborRecords record = neighbors[optNeighborIndex];
			logger.changeOptNeighborsLog(record.getId(), cal);			
			Socket socket = record.getUploadSocket();
			InputStream instream = socket.getInputStream();
			OutputStream outstream = socket.getOutputStream();
			Message message = new Message();
			System.out.println("OptUnchoking: peer" + this.id + " sends unchoking message to the peer " + record.getId());
			message.setType(Message.unchoke);
			message.setPayload(null);
			message.send(outstream);
			unchokenum++;
//			System.out.println("OptUnchoke:80:sends unchoking message to peer " + record.getID());
			long start = System.currentTimeMillis();
			
			while (true) {
				message.receive(instream);
				if (message.getType() == Message.notinterested) {
					logger.notInterestedLog(record.getId(), cal);
//					message.setType(Message.CHOKE);
//					message.setPayLoad(null);
//					System.out.println("OptUnchoking:90: receives not interested, before send choke");
//					message.send(out);
//					System.out.println("OptUnchoking:92: receives not interested, after send choke");
					break;
				}
				
				if (message.getType() == Message.request) {
					int index = ByteIntConvert.byteToInt(message.getPayload());
					Piece upload = filehandle.readFile(index);
//					if (index == 246) {
//						System.out.println("OptUnchoking: 79: peer " + this.myID + " reads one piece from disk, piece index = " + index + ", length = " + uploadPiece.getPieceBytes().length);
//					}
					
					message.setType(Message.piece);
					message.setPayload(upload.getPieceContent());
					message.send(outstream);
					count++;
//					System.out.println("OptUnchoking: sends piece " + index + " to peer " + record.getID());
				}
				
				//check optimal unchoking interval ends
				long last = System.currentTimeMillis();
				if ((last - start) > interval * 1000) {
					System.out.println("OptUnchoking: unchoking interval ends");
					message.receive(instream);
					message.setType(Message.choke);
					message.setPayload(null);
					message.send(outstream);
					chokenum++;
					break;
				} 
			}
			record.compareAndSetChokeState(2, 0);
			System.out.println("OptUnchoking: after compare and set");
	
 		}
		System.out.println("OptUnchoking: "+ this.id +" optUnchoking finished: send # piece =" + count + ", choke =" + chokenum + ", unchoke = " + unchokenum);
		return new Object();
	}

}
