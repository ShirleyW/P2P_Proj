

import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class NeighborRecords {
//this class is for peers to maintain the records of each neighbors
//records include fields declared blow
	private final int peerId; //ID of this neighbor
    private AtomicInteger chokeState;//chokeState=0: choked; 1: unchoked; 2:OptUnchoked;
    private BitField bitField; //bitField of this neighbor
    private final Socket downloadSocket; 
    private final Socket uploadSocket;
    private final Socket controlSocket;
    private int download; //amount of download pieces from this neighbor
    
    public NeighborRecords(int Id, int pieceNum, Socket downSocket,Socket upSocket,Socket conSocket){
    	
    	peerId=Id;
    	chokeState=new AtomicInteger(0);
        bitField= new BitField(pieceNum);
    	downloadSocket=downSocket; 
        uploadSocket=upSocket;
        controlSocket=conSocket;
        download=0;
    }
    
    public int getId(){
    	return peerId;
    }
    
    public int getChokeState(){
    	return chokeState.get();
    }
    
    public Boolean compareAndSetChokeState(int expect, int update){
    	return chokeState.compareAndSet(expect, update);
    }
   
    
    //change the reference of bitField to a new one
     public void setBitField(BitField bitField){
    	this.bitField=bitField;
    }  
   
    //substitute for upper method
     public void setBitField(byte[] byteField){
    	bitField.setBitField(byteField);;
    }  
     
    //**bad design: expose the private field by return its reference, lose control after return
    public BitField getBitField(){
    	return bitField;
    }
    //**
    
    public boolean isFinished(){
    	return bitField.isFinished();
    }
    
    public void turnOnBit(int pos){
    	bitField.setBitTrue(pos);
    }
    
    
    public Socket getDownloadSocket(){
    	return downloadSocket;
    }
    
    public Socket getUploadSocket(){
    	return uploadSocket;
    }
    
    public Socket getControlSocket(){
    	return controlSocket;
    }
    
    public int getDownload(){
    	return download;
    }
    
    public int incDownload(){
    	return download++;
    }
    
    public void clearDownload(){
    	download=0;
    }
}
