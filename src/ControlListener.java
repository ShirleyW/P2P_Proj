
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


public class ControlListener implements Callable<Object> {
	static AtomicInteger stopCount = new AtomicInteger(0);
	private int myId;
	private int neighborsNum;
	private NeighborRecords record;
	private P2pLogger logger;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private Message message;
	
	public ControlListener(int myID, NeighborRecords record, P2pLogger logger, int numNeighbors) throws IOException {
		this.record = record;
		this.logger = logger;
		myId = myID;
		neighborsNum = numNeighbors;
		socket = record.getControlSocket();
		in = socket.getInputStream();
		out = socket.getOutputStream();
		message = new Message();
	}
	
	@Override
	public Object call() throws Exception {
		while (true) {
			message.receive(in);
			if (message.getType() == Message.stop) {
				stopCount.incrementAndGet();
				if (stopCount.get() == neighborsNum) {
					break;
				}
			} 
			else {
				if (message.getType() == Message.have) {
					byte[] payLoad = message.getPayload();
					int index = ByteIntConvert.byteToInt(payLoad);
					record.getBitField().setBitTrue(index);
					Calendar cal = Calendar.getInstance();
					logger.haveLog(record.getId(), index, cal);
				}
			}

		}	
		return new Object();
	}
	
}
