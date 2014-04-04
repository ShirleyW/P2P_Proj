import java.io.*;
import java.net.*;

public class Message {
	//this class implements the message that peers send from each other
	
	private int length;
	private byte type;
	private byte[] payload;
	
	static final byte choke=0;
	static final byte unchoke=1;
	static final byte interested=2;
	static final byte notinterested=3;
	static final byte have=4;
	static final byte bitfield=5;
	static final byte request=6;
	static final byte piece=7;
	static final byte stop=8;
	
	public byte getType(){
		return type;
	}
	
	public byte[] getPayload(){
		return payload;
	}
	
	public void setType(byte type){
		this.type=type;
	}
	
	public void setPayload(byte[] payload){
		this.payload=payload;
	}
	
	
	
	//send a message to the outputstream
	public void send(OutputStream out)throws IOException{
		if(payload==null)
			length=1;
		else
			length=payload.length+1;
		
		out.write(ByteIntConvert.intToByte(length));
		out.write(type);
		
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
		byte[] byteType=new byte[1];
		while(totalRcvd<1){
			rcvd=in.read(byteType, totalRcvd, 1-totalRcvd);
			totalRcvd+=rcvd;
		}
		type=byteType[0];
		
		//get payload
		if(length>1)
			payload=new byte[length-1];
		else
			payload=null;
		totalRcvd=0;
		while(totalRcvd<length-1){
			rcvd=in.read(payload, totalRcvd, length-1-totalRcvd);
			totalRcvd+=rcvd;
		}
		
	}
}
