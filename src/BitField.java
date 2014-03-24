public class BitField {
	private final int pieceNum;
	private boolean[] bitField;
	private int finishedPiecesNum;
	private boolean isFinished;
	
	//constructor
	public BitField(int pieceNum){
		this.pieceNum=pieceNum;
		finishedPiecesNum=0;
		isFinished=false;
		bitField=new boolean[pieceNum];
		for(int i=0;i<pieceNum;i++)
			bitField[i]=false;
	}
	
	//to check whether the download is finished
	public synchronized boolean isFinished(){
		return isFinished;
	}
	
	//change the bitfield to byte representation
	public synchronized byte[] byteField(){
		int byteNum;
		if(pieceNum%8==0)byteNum=pieceNum/8;
		else byteNum=pieceNum/8+1;
		
		byte[] byteField=new byte[byteNum];
		for(int i=0;i<byteNum;i++)
			byteField[i]=(byte)0;
		
		for(int i=0;i<pieceNum;i++){
			int bitPos=i%8;
			int bytePos=i/8;
			
			if(bitField[i]==true)
				byteField[bytePos]=(byte)((1<<bitPos)|byteField[bytePos]);
			else
				byteField[bytePos]=(byte)(~(1<<bitPos)&byteField[bytePos]);
		}
		
		return byteField;
	}
	
	//set bitfield from bytefield
	public synchronized void setBitField(byte[] byteField){
		finishedPiecesNum=0;
		
		for(int i=0;i<pieceNum;i++){
			int bitPos=i%8;
			int bytePos=i/8;
			if(((1<<bitPos)&byteField[bytePos])==0)
				bitField[i]=false;
			else{
				bitField[i]=true;
				finishedPiecesNum++;
			}
		}
		
		if(finishedPiecesNum==pieceNum)
			isFinished=true;
	}
	
	//set a given bit true
	public synchronized void setBitTrue(int pos){
		if(bitField[pos]==false){
			bitField[pos]=true;
			finishedPiecesNum++;
			if(finishedPiecesNum==pieceNum)isFinished=true;
		}
	}
	
	//set a given bit false
	public synchronized void setBitFlase(int pos){
		if(bitField[pos]==true){
			bitField[pos]=false;
			finishedPiecesNum--;
			isFinished=false;
		}
	}
	
	//set all bits true
	public synchronized void setAllBitsTrue(){
		for(int i=0;i<pieceNum;i++)
			bitField[i]=true;
		
		finishedPiecesNum=pieceNum;
		isFinished=true;
	}
	
	//change the bitfield into a binary string
	public synchronized String bitsToString(){
		String bits = new String("");
		for(int i=0;i<pieceNum;i++){
			if(bitField[i])
				bits=bits+"1";
			else
				bits=bits+"0";
		}
		
		return bits;
	}
	
	//return true if is interested in another bitField
	public synchronized boolean interested(BitField f){
		for(int i=0;i<pieceNum;i++)
			if((bitField[i]==false)&&(f.bitField[i]==true))return true;
		
		return false;
	}
	
	//set the interesting piece true and return the index, otherwise return -1
	public synchronized int downloadInterestingPiece(BitField f){
		for(int i=0;i<pieceNum;i++){
			if((bitField[i]==false)&&(f.bitField[i]==true)){
				bitField[i]=true;
				finishedPiecesNum++;
				if(finishedPiecesNum==pieceNum)
					isFinished=true;
				
				return i;
			}
		}
		
		return -1;
	}
	
}
