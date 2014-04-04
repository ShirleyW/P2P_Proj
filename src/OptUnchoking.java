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
		System.out.println("Optimal Unchoking: peer " + this.id + " starts optimal uploading");
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
				System.out.println("Optimal Unchoking: peer " + this.id + ": all its neighbors finished");
				break;
			}
			int optIndex = -1;
			boolean find = false;
			while(!find){
				optIndex=(int)(Math.random()*neighbors.length);
				if ((neighbors[optIndex].getBitField().interested(bitfield)) && neighbors[optIndex].compareAndSetChokeState(0, 5)) {
					find = true; 
				}
				
			}
//			for (int i = 0; i < TRYTIME; i++) {
//				optIndex=(int)(Math.random()*neighbors.length);
//				if ((neighbors[optIndex].getBitField().interested(bitfield)) && neighbors[optIndex].compareAndSetChokeState(0, 5)) {
//					find = true; 
//					break;
//				}
//			}
//			if (!find) {
//				Thread.sleep(1000);
////				System.out.println("OptUnchoke:59: not found");
//				continue;
//			}
			Calendar cal = Calendar.getInstance();
			//logger.haveLog(record.getId(), index, cal);
			logger.changeOptNeighborsLog(neighbors[optIndex].getId(), cal);
			NeighborRecords record = neighbors[optIndex];
			Socket socket = record.getUploadSocket();
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			Message msg = new Message();
//			System.out.println("OptUnchoke:75: before peer " + this.myID + " sends unchoke message to the peer " + record.getID());
			msg.setType(Message.unchoke);
			msg.setPayload(null);
			msg.send(out);
			unchokenum++;
//			System.out.println("OptUnchoke:80:sends unchoking message to peer " + record.getID());
			long start = System.currentTimeMillis();
			
			while (true) {
//				mark++;
//				System.out.println("OptUnchoke:80: before read a socket, mark = " + mark);
				msg.receive(in);
//				System.out.println("OptUnchoke:82: after read a socket, mark = " + mark + ", message type =" + msg.getType()) ;
//				System.out.println("OptUnchoke:70:peer " + this.myID + " receives a message from peer " + record.getID() + ", message type = " + msg.getType());
				if (msg.getType() == Message.notinterested) {
					logger.notInterestedLog(record.getId(), cal);
//					msg.setType(Message.CHOKE);
//					msg.setPayLoad(null);
//					System.out.println("OptUnchoke:90: receives not interested, before send choke");
//					msg.send(out);
//					System.out.println("OptUnchoke:92: receives not interested, after send choke");
					break;
				}
				
				if (msg.getType() == Message.request) {
					int index = ByteIntConvert.byteToInt(msg.getPayload());
					Piece uploadPiece = filehandle.readFile(index);
//					if (index == 246) {
//						System.out.println("OptUnchoke:79: peer " + this.myID + " reads one piece from disk, piece index = " + index + ", length = " + uploadPiece.getPieceBytes().length);
//					}
					
					msg.setType(Message.piece);
					msg.setPayload(uploadPiece.getPieceContent());
					msg.send(out);
					count++;
//					System.out.println("OptUnchoke:110: sends piece " + index + " to peer " + record.getID());


				}
				
				//check if timeout
				if ((System.currentTimeMillis() - start) > interval * 1000) {
//					System.out.println("OptUnchoke:87:time out!");
					msg.receive(in);
					msg.setType(Message.choke);
					msg.setPayload(null);
					msg.send(out);
					chokenum++;
					break;
				} 
			}
//			System.out.println("OptUnchoke:117: before compare and set");
			neighbors[optIndex].compareAndSetChokeState(5, 0);
//			System.out.println("OptUnchoke119: after compare and set");
	
 		}
		return null;
	}

}
