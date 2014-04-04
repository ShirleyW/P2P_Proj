import java.io.*;
import java.net.*;


public class HandShakeMessage {
	private final String header;
	private byte[] zeroBits;
	private int ID;
	
	public HandShakeMessage(){
		header=new String("HELLO");
		zeroBits=new byte[27];
		for(int i=0;i<27;i++)
			zeroBits[i]=0;
	}
	
	public int getID(){
		return ID;
	}
	
	public void setID(int ID){
		this.ID=ID;
	}
	
	//receive the handshake message from a socket
	public void receive(Socket s)throws IOException{
		InputStream in = s.getInputStream();
		
		//read header
		int rcvd;
		int totalRcvd=0;
		byte[] byteHeader=new byte[5];
		while(totalRcvd<5){
			rcvd=in.read(byteHeader, totalRcvd, 5-totalRcvd);
			totalRcvd+=rcvd;
		}
		
		//read zerobits
		totalRcvd=0;
		while(totalRcvd<27){
			rcvd=in.read(zeroBits, totalRcvd, 27-totalRcvd);
			totalRcvd+=rcvd;
		}
		
		//read peerID
		totalRcvd=0;
		byte[] byteID=new byte[4];
		while(totalRcvd<4){
			rcvd=in.read(byteID, totalRcvd, 4-totalRcvd);
			totalRcvd+=rcvd;
		}
		ID=ByteIntConvert.byteToInt(byteID);
	}
	
	//send a message to the socket
	public void send(Socket s)throws IOException{
		OutputStream out=s.getOutputStream();
		out.write(header.getBytes());
		out.write(zeroBits);
		out.write(ByteIntConvert.intToByte(ID));
		out.flush();
	}
	
}
