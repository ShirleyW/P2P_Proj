import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class Download implements Callable<Object>{
	
	private Socket skt;
	private InputStream in;
	private OutputStream out;
	private NeighborRecords myRecord;
	private NeighborRecords[] neighborRecords;
	private BitField myBitField;
	private FileHandle fileHandle;
	private int myID;
	private P2pLogger logger;
	
	public Download(NeighborRecords myRecord, NeighborRecords[] neighborRecords, BitField myBitField, FileHandle fileHandle, int myID, P2pLogger logger)throws IOException{
		this.myRecord=myRecord;
		this.neighborRecords=neighborRecords;
		this.myBitField=myBitField;
		this.fileHandle=fileHandle;
		this.myID=myID;
		this.logger=logger;
		this.skt=myRecord.getDownloadSocket();
		this.in=this.skt.getInputStream();
		this.out=this.skt.getOutputStream();
	}
	
	public Object call()throws IOException{
		Message msg=new Message();
		Calendar cal;
		
		while(true)
		{
			msg.receive(in);
			if(msg.getType()==Message.stop)
			{
				for(int i=0; i<neighborRecords.length; i++)
					msg.send(neighborRecords[i].getControlSocket().getOutputStream());
				break;
			}
			else if(msg.getType()==Message.unchoke)
			{
				cal = Calendar.getInstance();
				logger.unchokedLog(myRecord.getId(), cal);
				
				while(true)
				{
					int interest=myBitField.downloadInterestingPiece(myRecord.getBitField());
					
					if(interest==-1)
					{
						msg.setType(Message.notinterested);
						msg.setPayload(null);
						msg.send(out);
						break;
					}
					else
					{
						msg.setType(Message.request);
						msg.setPayload(ByteIntConvert.intToByte(interest));
						msg.send(out);
						msg.receive(in);
						
						if(msg.getType()==Message.choke)
						{
							cal = Calendar.getInstance();
							logger.chokedLog(myRecord.getId(), cal);
							myBitField.setBitFlase(interest);
							break;
						}
						
						if(msg.getType()==Message.piece)
						{
							Piece getPiece=new Piece(interest,msg.getPayload());
							fileHandle.writeFile(getPiece);
							myRecord.incDownload();
							cal = Calendar.getInstance();
							logger.downloadedLog(myRecord.getId(), interest, 1, cal);
							msg.setType(Message.have);
							msg.setPayload(ByteIntConvert.intToByte(interest));
							for(int i=0; i<neighborRecords.length; i++)
								msg.send(neighborRecords[i].getControlSocket().getOutputStream());
						}
					}
				}
			}
		}
		return new Object();
	}

}
