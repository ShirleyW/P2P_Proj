import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.Callable;


public class UnChoke implements Callable<Object> {
	
	private int unchoketime;
	private NeighborRecords record;
	private FileHandle fileHandler;
	private P2pLogger myLogger;
	private Message message;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	
	public UnChoke(NeighborRecords record, FileHandle fileManager, int time, P2pLogger logger) throws IOException {
		
		this.record = record;
		fileHandler = fileManager;
		unchoketime = time;
		myLogger = logger;
		message = new Message();
		socket = record.getUploadSocket();
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}

	@Override
	public Object call() throws Exception {
		
		message.setPayload(null);
		message.setType(Message.unchoke);
		message.send(out);
		long starttime = System.currentTimeMillis();	
		while (true) {
			message.receive(in);
			if (message.getType() == Message.notinterested) {
				Calendar cal = Calendar.getInstance();
				myLogger.notInterestedLog(record.getId(),cal);
				break;
			}
			
			if (message.getType() == Message.request) {
				int index = ByteIntConvert.byteToInt(message.getPayload());
				Piece uploadPiece = fileHandler.readFile(index);
				message.setType(Message.piece);
				message.setPayload(uploadPiece.getPieceContent());
				message.send(out);
			}
			
			//check if timeout
			if ((System.currentTimeMillis() - starttime) > unchoketime * 1000) {
				message.receive(in);
				message.setType(Message.choke);
				message.setPayload(null);
				message.send(out);
				break;
			} 
		}
		return new Object();
	}
}
