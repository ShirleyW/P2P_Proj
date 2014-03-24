

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class P2pLogger {
	private Logger logger;  
    private FileHandler fh;
    private int myId;
    private SimpleDateFormat sdf;
    P2pLogger(int peerId){
	
    	myId=peerId;
    	sdf = new SimpleDateFormat("HH:mm:ss");
		 try {  

			    logger = Logger.getLogger("log_peer_"+myId+".log");
		        // This block configure the logger with handler and formatter  
		        fh = new FileHandler("log_peer_"+myId+".log");  
		        logger.addHandler(fh);
		        SimpleFormatter formatter = new SimpleFormatter();  
		        fh.setFormatter(formatter);  


		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  

	}

    public synchronized void tcpConnectionLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] makes a connection to Peer ["+peerId+"].");  
    }
    
    public synchronized void tcpConnectedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is connected from Peer ["+peerId+"].");  
    }
    public synchronized void changePrefNeighborsLog(int[] peerId, Calendar cal){
    	String neighbors=Arrays.toString(peerId);
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has the preferred neighbors "+neighbors+".");  
    }
    public synchronized void changeOptNeighborsLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has the optimistically-unchoked neighbors ["+peerId+"].");  
    } 
    public synchronized void unchokedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is unchoked by ["+peerId+"].");  
    }
    public synchronized void chokedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is choked by ["+peerId+"].");  
    }
    public synchronized void haveLog(int peerId, int pieceIndex, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received a 'have' message from ["+peerId+"] for the piece ["+pieceIndex+"].");  
    }
    public synchronized void interestedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received an 'interested' message from ["+peerId+"].");  
    }
    public synchronized void notInterestedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received a 'not interested' message from ["+peerId+"].");  
    }
    public synchronized void downloadedLog(int peerId, int pieceIndex, int numPieces, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has downloaded the piece ["+pieceIndex+"] from ["+peerId+"]. Now the number of pieces it has is ["+numPieces+"].");  
    }
    public synchronized void completionLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has downloaded the complete file.");  
    }
}
