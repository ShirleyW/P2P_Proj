import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;




public class Configuration{
	//common properties
	private final int numPreNeighbor;
	private final int unChokeTime;
	private final int optUnChokeTime;
	private final String fileName;
	private final int fileSize;
	private final int pieceSize;
	private final int lastPieceSize;
	private final int numPieces;

	//peer information
	private final ArrayList<Integer> peerID;
	private final ArrayList<String> hostName;
	private final ArrayList<Integer> downPort;
	private final ArrayList<Boolean> hasFile;
	private final ArrayList<Integer> upPort;
	private final ArrayList<Integer> havePort;
	private final int numPeers;


	public Configuration(String Common, String PeerInfo) throws FileNotFoundException{
			 	///Specify common properties
			FileReader fileReader1 =new FileReader(Common);
		    Scanner sc1= new Scanner(fileReader1);
		    String[] tokens =sc1.nextLine().split(" ");
		    numPreNeighbor=Integer.parseInt(tokens[1].trim());

		    tokens =sc1.nextLine().split(" ");
		    unChokeTime=Integer.parseInt(tokens[1].trim());

		    tokens =sc1.nextLine().split(" ");
		    optUnChokeTime=Integer.parseInt(tokens[1].trim());

		    tokens =sc1.nextLine().split(" ");
		    fileName=tokens[1].trim();

		    tokens =sc1.nextLine().split(" ");
		    fileSize=Integer.parseInt(tokens[1].trim());

		    tokens =sc1.nextLine().split(" ");
		    pieceSize=Integer.parseInt(tokens[1].trim());

		    if(fileSize%pieceSize==0){
		    	lastPieceSize=pieceSize;
		    	numPieces=fileSize/pieceSize;
		    }else{
		    	lastPieceSize=fileSize%pieceSize;
		    	numPieces=fileSize/pieceSize+1;
		    }
		    sc1.close();

		    //Specify peer information
		    peerID = new ArrayList<Integer>();
	        hostName = new ArrayList<String>();
		    downPort = new ArrayList<Integer>();
			hasFile = new ArrayList<Boolean>();
			upPort = new ArrayList<Integer>();
			havePort = new ArrayList<Integer>();

		   FileReader fileReader2 =new FileReader(PeerInfo);
		   Scanner sc2 = new Scanner(fileReader2);
		   int num=0;
		   while (sc2.hasNextLine()) {
		   		num++;
		   		String[] newTokens =sc2.nextLine().split(" ");
		   		peerID.add(Integer.parseInt(newTokens[0].trim()));
		   		hostName.add(newTokens[1].trim());
		   		downPort.add(Integer.parseInt(newTokens[2].trim()));
		   		upPort.add(Integer.parseInt(newTokens[2].trim())+1);
		   		havePort.add(Integer.parseInt(newTokens[2].trim())+2);
		   		if (Integer.parseInt(newTokens[3].trim())==1) {
		   			hasFile.add(true);
		   		}
		   		else{
		   			hasFile.add(false);
		   		}
		    }
		    	numPeers=num;
		        sc2.close();

		   //for test
//	    System.out.println("Configuration...");
//		System.out.println("numPreNeighbor = " + this.numPreNeighbor);
//		System.out.println("unChokeTime = " + this.unChokeTime);
//		System.out.println("optUnChokeTime = " + this.optUnChokeTime );
//		System.out.println("fileName = " + this.fileName);
//		System.out.println("fileSize = " + this.fileSize);
//		System.out.println("pieceSize = " + this.pieceSize);
//		System.out.println("lastPieceSize = " + this.lastPieceSize);
//		System.out.println("numPieces = " + this.numPieces);
//
//		for (int i = 0; i < this.numPeers; i++) {
//			System.out.println(peerID.get(i)+" "+hostName.get(i)+" "+this.downPort.get(i)+" "+upPort.get(i)+" "+havePort.get(i)+" "+hasFile.get(i));
//        	}
	}
	public int getNumPreNeighbor(){
		return numPreNeighbor;
	}
	public int getUnChokeTime(){
		return unChokeTime;
	}
	public int getOptUnChokeTime(){
		return optUnChokeTime;
	}
	public String getFileName(){
		return fileName;
	}
	public int getFileSize(){
		return fileSize;
	}
	public int getPieceSize(){
		return pieceSize;
	}
	public int getLastPieceSize(){
		return lastPieceSize;
	}
	public int getNumPieces(){
		return numPieces;
	}
	public ArrayList<Integer> getID(){
		return peerID;
	}
	public ArrayList<String> getHostName(){
		return hostName;
	}
	public ArrayList<Integer> getDownPort(){
		return downPort;
	}
	public ArrayList<Boolean> getHasFile(){
		return hasFile;
	}
	public ArrayList<Integer> getUpPort(){
		return upPort;
	}
	public ArrayList<Integer> getHavePort(){
		return havePort;
	}
	public int getNumPeers(){
		return numPeers;
	}
}
