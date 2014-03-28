
// Piece of the data file
public class Piece {
	
	private final int pieceNum; // the number or rank of piece
	private final byte[] pieceContent; // the content of this piece
	
	public Piece(int num, byte[] content){
		pieceNum=num;
		pieceContent=content;
	}
	
	// return the number
	public int getPieceNum(){
		return pieceNum;
	}
	
	// return the content
	public byte[] getPieceContent(){
		return pieceContent;
	}
	
//	public static void main(){
//		int num=5;
//		byte[] content= new byte[10];
//		content[0]=10;
//		Piece pie=new Piece(num, content);
//		
//	}

}
