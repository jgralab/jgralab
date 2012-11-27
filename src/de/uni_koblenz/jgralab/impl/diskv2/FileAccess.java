package de.uni_koblenz.jgralab.impl.diskv2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Wrapper class to provide access to a file.
 * 
 * A FileAccess for a given file can be obtained by calling the factory method
 * provided by this class. The object returned by this method then provides methods
 * to write the contents of a ByteBuffer into this file, or to read a number of 
 * bytes from this file, which are then returned in a byte buffer.
 * 
 * Internally, it uses a MappedByteBuffer for increased efficiency if the operating
 * system isn't Windows. If Windows is used, it sticks with a FileChannel because a
 * bug in Java makes it impossible for files to be deleted if it has been 
 * accessed via a MappedByteBuffer at any point.
 *  
 * @author aheld
 *
 */
public abstract class FileAccess {
		
	/**
	 * Checks if the used OS is windows
	 */
	//always use the windows variant, because the default one isn't stable
	//private static boolean windows = isWindows();
	private static boolean windows = true;
	
	/**
	 * The FileChannel used to access the file.
	 */
	protected FileChannel channel;
	
	/**
	 * The current size of the accessed file
	 */
	private long size;
	
	/**
	 * Factory method that provides a FileAccess object for a specific file.
	 * If such an object was created previously, that object is returned. 
	 * Else, a new object is created and returned.
	 * 
	 * @param filename
	 *        The name of the file to access. The suffix ".dst" is added internally. 
	 * @return An access to the named file. 
	 */
	public static FileAccess createFileAccess(String filename){	
		FileAccess fileAccess;
		
		if (windows){
			fileAccess = new FileAccessForWindows(filename);
		}
		else {
			fileAccess = new FileAccessDefault(filename);
		}
		
		//add a shutdown hook so the Channel is closed during shutdown, 
		//or else we cannot delete the file
		fileAccess.addShutdownHook();
		
		return fileAccess;
	}
	
	/**
	 * Writes the contents of a ByteBuffer into the file that this object provides
	 * access to.
	 * 
	 * @param content
	 *        The ByteBuffer whose content is written to the file
	 * @param index
	 *        The position in the file to which the content is written, in bytes
	 */
	public abstract void write(ByteBuffer content, long index);
	
	/**
	 * Read the content of a file from a given position.
	 * 
	 * @param numBytes
	 *        The amount of bytes to be read
	 * @param index
	 *        The position in the file from which to start reading
	 * @return
	 *        A byte buffer containing the requested bytes
	 */
	public abstract ByteBuffer read(int numBytes, long index);
	
	/**
	 * Method that returns true if the used operating system is windows
	 * 
	 * @return true if the OS is Windows, false otherwise 
	 * 
	 * @author mkyong
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}
	
	/**
	 * Tell the DiskStorageManager that a file will grow
	 * 
	 * @param newSize
	 * 		What the new size of the file will be after it has grown
	 */
	protected void requestSizeChange(long newSize){
		if (newSize > size){
			DiskStorageManager.increaseDiskStorageSize(newSize - size);
			size = newSize;
		}
	}
	
	/**
	 * Code that is executed when the VMU exits
	 */
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	//close file channel so Java can delete the file.
		        try {
					channel.close();
				} catch (IOException e) {
					throw new RuntimeException("Unable to close FileChannel");
				}
		    }
		});
	}
}
