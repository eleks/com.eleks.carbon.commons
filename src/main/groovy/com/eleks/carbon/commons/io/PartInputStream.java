package com.eleks.carbon.commons.io;


import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/** remembers the first x bytes of the input stream for later usage */

public class PartInputStream extends java.io.FilterInputStream {
	private byte[] part;
	private int partlen;
	
	public  PartInputStream(InputStream in, int partBytes){
		super(in);
		part = new byte[partBytes];
		partlen = 0;
	}
	
	private void storePart(byte[] b, int off, int len){
		if(partlen<part.length && len>0){
			int i=part.length - partlen;
			if(len<i)i=len;
			System.arraycopy(b, off, part, partlen, i);
			partlen+=i;
		}
	}
	
	private void storePart(byte b){
		if(partlen<part.length){
			part[partlen]=b;
			partlen++;
		}
	}
	
	public int read(byte[] b, int off, int len) throws IOException{
		int i=super.read(b, off, len);
		storePart(b,off,i);
		return i;
	}
	
	public int read(byte[] b) throws IOException{
		int i=super.read(b);
		storePart(b,0,i);
		return i;
	}
	
	public int read() throws IOException{
		int i=super.read();
		if(i!=-1){
			storePart((byte)i);
		}
		return i;
	}
	
	public ByteBuffer getPart(){
		return ByteBuffer.wrap(part, 0, partlen);
	}
}
