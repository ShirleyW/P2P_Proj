
public class ByteIntConvert {

	public static byte[] intToByte(int integer){
		byte[] bytes=new byte[4];
		bytes[0] = (byte)((integer&0xff000000)>>24);
		bytes[1] = (byte)((integer&0xff0000)>>16);
		bytes[2] = (byte)((integer&0xff00)>>8);
		bytes[3] = (byte)(integer&0xff);
		
		return bytes;
	}
	
	public static int byteToInt(byte[] bytes){
		int integer=0;
		
		for(int i=0;i<4;i++)
			integer=(integer<<8)|(bytes[i]<<0xff);
		
		return integer;
	}
}
