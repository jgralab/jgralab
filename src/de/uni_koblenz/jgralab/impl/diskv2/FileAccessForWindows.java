package de.uni_koblenz.jgralab.impl.diskv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Implementation for the abstract class FileAccess that is used if
 * the operating system is Windows.
 * 
 * @author aheld
 *
 */
public class FileAccessForWindows extends FileAccess{
	
	/**
	 * Creates a FileAccess to a specific file.
	 * 
	 * @param filename
	 *        The name of the file the constructed object provides access to.
	 */
	protected FileAccessForWindows(String filename){
		//System.err.println("WARNING: Windows detected. Use another OS for better efficiency.");
		File file = new File(filename + ".dst");
		file.deleteOnExit();
		try {
			RandomAccessFile ramFile = new RandomAccessFile(file, "rw");
			FileChannel fileChannel = ramFile.getChannel();
			this.channel = fileChannel;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error: Could not create file " + filename + ".dst");
		}
	}
	
	@Override
	public void write(ByteBuffer content, long index){
		requestSizeChange(index + content.capacity());
		content.position(0);
		
		try {
			channel.write(content, index);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write to file");
		}
	}
	
	@Override
	public ByteBuffer read(int numBytes, long index){
		ByteBuffer buf = ByteBuffer.allocate(numBytes);
		
		try {
			channel.read(buf, index);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read from file");
		}
		
		return buf;
	}
}
