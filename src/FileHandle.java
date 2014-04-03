import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class FileHandle {
	private RandomAccessFile file;
	private Configuration config;
	
	public FileHandle(Configuration initial, int id) throws FileNotFoundException{
		config=initial;
		String pathname="peer_" + id + "/";
		File newfile=new File(pathname);
		if(!newfile.exists()) {
			newfile.mkdirs();
			System.out.println("file "+"peer_" + id+" created");
		}
		file=new RandomAccessFile(pathname+initial.getFileName(),"rw");
	}
	
	public synchronized Piece readFile(int index) throws IOException{
		int len;
		if(index==config.getNumPieces()-1){
			//read the last piece
			len=config.getLastPieceSize();
		}else{
			//read piece other than the last piece
			len=config.getPieceSize();
		}
		byte[] content=new byte[len];
		int offSet=index*config.getPieceSize();
		file.seek(offSet);
		int k=0;
		while(k<len){
			byte temp=file.readByte();
			content[k]=temp;
			k++;
		}
		
		Piece piece = new Piece(index, content);
		return piece;
	}
	
	public synchronized void writeFile(Piece piece) throws IOException{
		int pieceSize=config.getPieceSize();
		int offSet = piece.getPieceNum()*pieceSize;
		int len = piece.getPieceContent().length;
		byte[] temp = piece.getPieceContent();
		//locate the beginning point to write the piece
		file.seek(offSet);
		int k=0;
		while(k<len){
			file.writeByte(temp[k]);
			k++;
		}
	}
}
