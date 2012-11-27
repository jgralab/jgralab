package de.uni_koblenz.jgralab.impl.diskv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Implementation for the abstract class FileAccess that is used if
 * the operating system is not Windows.
 * 
 * @author aheld
 *
 */
public class FileAccessDefault extends FileAccess{
	
	/**
	 * The part of the file that is mapped into memory.
	 */
	private MappedByteBuffer accessWindow;
	
	/**
	 * The size of the area of the file that is mapped into memory, in bytes.
	 */
	private static final int FILE_AREA = 1048576; //1 MB
	
	/**
	 * Denotes the first byte of the access window.
	 */
	private long firstByte;
	
	/**
	 * Denotes the last byte of the access window.
	 */
	private long lastByte;
	
	// --------------------------[(a)-----------------(b)]------------
	// 
	// The hyphens represent the entire file. The area between [ and ] is 
	// the part of the file mapped into memory. Then, (a) is the first byte, 
	// whereas (b) is the last byte of the area mapped into memory.

	/**
	 * Creates a FileAccess to a specific file.
	 * 
	 * @param filename
	 *        The name of the file the constructed object provides access to.
	 */
	protected FileAccessDefault(String filename){
		File file = new File(filename + ".dst");
		file.deleteOnExit();
		try {
			RandomAccessFile ramFile = new RandomAccessFile(file, "rw");
			FileChannel fileChannel = ramFile.getChannel();
			this.channel = fileChannel;
			//use invalid values to force a remapping at the first read or write operation
			firstByte = lastByte = -1;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error: Could not create file " + filename + ".dst");
		}
	}
	
	@Override
	public void write(ByteBuffer content, long index){
		checkAccessWindow(content.capacity(), index);
	
		content.position(0);
		accessWindow.position((int) (index - firstByte));
		
		accessWindow.put(content);
	}
	
	@Override
	public ByteBuffer read(int numBytes, long index){
		checkAccessWindow(numBytes, index);
		
		byte[] readBytes = new byte[numBytes];
		
		accessWindow.position((int) (index - firstByte));
		accessWindow.get(readBytes);
		
		return ByteBuffer.wrap(readBytes);
	}

	/**
	 * Checks if the current read or write operation fits into the access window,
	 * i.e. if the part of the file that the operation wants to access is currently
	 * in the memory.
	 * 
	 * If it is, this method does nothing.
	 * If it isn't, the changes made to the part of the file that was mapped to the
	 * memory when this method was called are forced out to the disk. After that, a new
	 * part of the file is mapped into memory. The area that is mapped into memory is 
	 * chosen in such a way that the start position of the read or write operation (i.e.
	 * the byte at which the read or write operation is started) is in the middle
	 * of the area that is mapped to the memory during this method call. 
	 * An exception to this rule occurs if the condition "index < FILE_AREA/2" is met.
	 * In this case, the first FILE_AREA bytes of the file are mapped to the memory.
	 * 
	 * @param bufSize
	 *        The amount of bytes that are read or written in the next operation
	 * @param index
	 *        The starting position of the next read or write operation
	 */
	private void checkAccessWindow(int bufSize, long index){
		if (index < firstByte | index + bufSize > lastByte){
			
			if (index < FILE_AREA/2){
				//case 1: index < FILE_AREA/2
				//map the first FILE_AREA bytes
				requestSizeChange(FILE_AREA);
				firstByte = 0;
				lastByte = FILE_AREA;
			}
			else {
				//case 2: index > FILE_AREA/2
				//map from index - FILE_AREA/2 to index + FILE_AREA/2
				requestSizeChange(index + FILE_AREA/2);
				firstByte = index - FILE_AREA/2;
				lastByte = index + FILE_AREA/2;
			}
			
			try {
				//write the pending changes to the disk and map the new area
				//if (accessWindow != null) accessWindow.force();
				accessWindow = channel.map(MapMode.READ_WRITE, firstByte, FILE_AREA);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
