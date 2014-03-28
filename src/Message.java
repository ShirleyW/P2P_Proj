import java.io.*;
import java.net.*;

public class Message {
	//this class implements the message that peers send from each other
	
	private int length;
	private int type;
	private byte[] payload;
	
	static final int choke=0;
	static final int unchoke=1;
	static final int interested=2;
	static final int notinterested=3;
	static final int have=4;
	static final int bitfield=5;
	static final int request=6;
	static final int piece=7;
	static final int stop=8;
	
	public int getType(){
		return type;
	}
	
	public byte[] getPayload(){
		return payload;
	}
	
	public void setType(int type){
		this.type=type;
	}
	
	public void setPayload(byte[] payload){
		this.payload=payload;
	}
	
	
	
	//send a message to the outputstream
	public void send(OutputStream out)throws IOException{
		if(payload==null)
			length=4;
		else
			length=payload.length+4;
		
		out.write(ByteIntConvert.intToByte(length));
		out.write(ByteIntConvert.intToByte(type));
		
		if(payload!=null)
			out.write(payload);
		
		out.flush();
	}
	
	//receive a message from an inputstream
	public void receive(InputStream in)throws IOException{
		
		//to get the information of message length
		int rcvd;
		int totalRcvd=0;
		byte[] byteLength=new byte[4];
		while(totalRcvd<4){
			rcvd=in.read(byteLength,totalRcvd,4-totalRcvd);
			totalRcvd+=rcvd;
		}
		length=ByteIntConvert.byteToInt(byteLength);
		
		//to get the information of message type
		totalRcvd=0;
		byte[] byteType=new byte[4];
		while(totalRcvd<4){
			rcvd=in.read(byteType, totalRcvd, 4-totalRcvd);
			totalRcvd+=rcvd;
		}
		type=ByteIntConvert.byteToInt(byteType);
		
		//get payload
		if(length>4)
			payload=new byte[length-4];
		else
			payload=null;
		totalRcvd=0;
		while(totalRcvd<length-4){
			rcvd=in.read(payload, totalRcvd, length-4-totalRcvd);
			totalRcvd+=rcvd;
		}
		
	}
}
