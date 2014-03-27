
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
    private SimpleDateFormat sdf; //time format
    
    P2pLogger(int peerId){
    	myId=peerId;
    	sdf = new SimpleDateFormat("HH:mm:ss");
		 try {  

			    logger = Logger.getLogger("log_peer_"+myId+".log");// get a new logger
		        // This block configure the logger with handler and formatter  
		        fh = new FileHandler("log_peer_"+myId+".log");  //create a log file
		        logger.addHandler(fh);
		        SimpleFormatter formatter = new SimpleFormatter();  //logger format
		        fh.setFormatter(formatter);  

		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  

	}

    //synchronized method for making connections, need Calendar as input to get the time of making connection
    public synchronized void tcpConnectionLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] makes a connection to Peer ["+peerId+"].");  
    }
    //synchronized method for being connected, need Calendar as input to get the time of being connected
    public synchronized void tcpConnectedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is connected from Peer ["+peerId+"].");  
    }
    //synchronized method for changing prefered neighbors, need Calendar as input to get the time of changing
    public synchronized void changePrefNeighborsLog(int[] peerId, Calendar cal){
    	String neighbors=Arrays.toString(peerId);
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has the preferred neighbors "+neighbors+".");  
    }
    //synchronized method for changing Optimistic neighbors, need Calendar as input to get the time of changing
    public synchronized void changeOptNeighborsLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has the optimistically-unchoked neighbors ["+peerId+"].");  
    } 
    //unchoked by a neighbor
    public synchronized void unchokedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is unchoked by ["+peerId+"].");  
    }
    //choked by a neighbor
    public synchronized void chokedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] is choked by ["+peerId+"].");  
    }
    //receives a 'have' message
    public synchronized void haveLog(int peerId, int pieceIndex, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received a 'have' message from ["+peerId+"] for the piece ["+pieceIndex+"].");  
    }
    //receives an 'interested' message
    public synchronized void interestedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received an 'interested' message from ["+peerId+"].");  
    }
    //receives a 'not interest' message
    public synchronized void notInterestedLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] received a 'not interested' message from ["+peerId+"].");  
    }
    //finishes downloading a piece
    public synchronized void downloadedLog(int peerId, int pieceIndex, int numPieces, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has downloaded the piece ["+pieceIndex+"] from ["+peerId+"]. Now the number of pieces it has is ["+numPieces+"].");  
    }
    //finishes downloading the complete file
    public synchronized void completionLog(int peerId, Calendar cal){
    	logger.info(sdf.format(cal.getTime())+": Peer ["+myId+"] has downloaded the complete file.");  
    }
}
