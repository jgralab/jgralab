package de.uni_koblenz.jgralab.impl.diskv2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.GraphElementImpl;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class DiskStorageManager {

	/**
	 * The Graph that this DiskStorageManager works for
	 */
	private GraphImpl graph;

	/**
	 * FileAccess objects to all the files used by this manager
	 */
	private FileAccess vertexFile;
	private FileAccess edgeFile;
	private FileAccess stringFile;
	private FileAccess listFile;

	/**
	 * Pointers to the first free byte in strings.dst and lists.dst
	 */
	private long stringsPointer;
	private long listsPointer;

	/**
	 * Sizes (in bytes) of the biggest vertex class and the biggest edge calss
	 */
	private int maxVSize;
	private int maxESize;

	/**
	 * Create a new DiskStorageManager.
	 * 
	 * @param graphdb
	 *            The Graph Database whose data this DiskStorageManager manages.
	 */
	public DiskStorageManager(GraphImpl graphdb) {
		this.graph = graphdb;

		setupFilesAndProfiles();
	}

	/**
	 * For every non-abstract Vertex and Edge class, this method creates a
	 * GraphElementProfile. All files used by the DiskStorageManager are also
	 * created here.
	 */
	private void setupFilesAndProfiles() {
		Schema s = graph.getSchema();

		// get the amount of classes
		int amountOfClasses = s.getGraphElementClassCount();
		// tell GraphElementProfile to instantiate the Array in which profiles
		// are stored
		GraphElementProfile.setup(amountOfClasses);

		// get lists of all vertex and edge classes
		List<VertexClass> vClasses = s.getGraphClass().getVertexClasses();
		List<EdgeClass> eClasses = s.getGraphClass().getEdgeClasses();

		// make FileAccess objects for graph building blocks, strings and lists
		vertexFile = FileAccess.createFileAccess("vertices");
		edgeFile = FileAccess.createFileAccess("edges");
		stringFile = FileAccess.createFileAccess("strings");
		listFile = FileAccess.createFileAccess("lists");

		maxVSize = 0;
		maxESize = 0;
		stringsPointer = 0;
		listsPointer = 0;

		// create a profile and a FileAccess for all non-abstract vertex classes
		// also detect the biggest vertex class and store its size
		int typeId, vSize, eSize;
		for (VertexClass vClass : vClasses) {
			if (!vClass.isAbstract()) {
				typeId = vClass.getGraphElementClassIdInSchema();
				vSize = GraphElementProfile.createProfile(vClass, typeId,
						graph.getGraphFactory());
				if (vSize > maxVSize) {
					maxVSize = vSize;
				}
			}
		}

		// create a profile and a FileAccess for all non-abstract edge classes
		// also detect the biggest edge class and store its size
		for (EdgeClass eClass : eClasses) {
			if (!eClass.isAbstract()) {
				typeId = eClass.getGraphElementClassIdInSchema();
				eSize = GraphElementProfile.createProfile(eClass, typeId,
						graph.getGraphFactory());
				if (eSize > maxESize) {
					maxESize = eSize;
				}
			}
		}
	}

	/**
	 * Writes a Vertex to the disk if it has been newly created, or if it has
	 * been changed since the last time it was loaded from the disk.
	 * 
	 * @param vRef
	 *            The Reference to the Vertex that is written out.
	 */
	public void writeVertexToDisk(int id, Tracker<VertexImpl> tracker) {
		assert id > 0;
		assert tracker != null;
		// System.out.println("write vertex to disk: " + vRef.getKey());
		writeGraphElementToDisk(id, tracker, vertexFile, maxVSize);
	}

	/**
	 * Writes a n Edge to the disk if it has been newly created, or if it has
	 * been changed since the last time it was loaded from the disk.
	 * 
	 * @param eRef
	 *            The Reference to the Vertex that is written out.
	 */
	public void writeEdgeToDisk(int id, Tracker<EdgeImpl> tracker) {
		assert id > 0;
		assert tracker != null;
		// System.out.println("write edge " + eRef.getKey() + " to disk");
		writeGraphElementToDisk(id, tracker, edgeFile, maxESize);
	}

	/**
	 * Writes a Graph Element to the disk if it has been newly created, or if it
	 * has been changed since the last time it was loaded from the disk.
	 * 
	 * @param geRef
	 *            The Reference to the GraphElement that is written out.
	 * 
	 * @param file
	 *            The access to the file in which the GraphElement is stored.
	 */
	private void writeGraphElementToDisk(int id, Tracker<?> tracker,
			FileAccess file, int maxSize) {
		ByteBuffer attributes = tracker.getVariables();
		String[] strings = tracker.getStrings();
		List<?>[] lists = tracker.getLists();

		// detect the type of the vertex or edge we want to write out
		int typeId = attributes.getInt(0) - 1;

		// fetch the profile of this type
		GraphElementProfile profile = GraphElementProfile.getProfile(typeId);

		// determine the location of the element we want to store
		long baseLocation = maxSize * id;

		// write the primitive attributes to a file
		file.write(attributes, baseLocation);

		// write all Strings to a file, and store their location on the disk
		if (strings != null) {
			int numElems = profile.getNumStrings();
			ByteBuffer locations = ByteBuffer.allocate(numElems * 8);

			for (int i = 0; i < numElems; i++) {
				long location = writeStringToDisk(strings[i]);
				locations.putLong(location);
			}

			locations.position(0);
			file.write(locations, baseLocation + profile.getStartOfStrings());
		}

		// write all Lists to a file, and store their location on the disk
		if (lists != null) {
			int numElems = profile.getNumLists();
			ByteBuffer locations = ByteBuffer.allocate(numElems * 8);

			for (int i = 0; i < numElems; i++) {
				locations.putLong(writeListToDisk(lists[i]));
			}

			file.write(locations, baseLocation + profile.getStartOfLists());
		}
	}

	/**
	 * Reads a vertex from the disk and restores it.
	 * 
	 * @param key
	 *            The local id of the vertex
	 * @return A soft reference to the restored vertex
	 */
	public VertexImpl readVertexFromDisk(int key) {
		// read the data from the disk
		ByteBuffer buf = readGraphElementFromDisk(key, vertexFile, maxVSize);

		// create a vertex that is identical to the vertex we deleted earlier
		VertexImpl ver = restoreVertex(buf, key);

		// return a CacheEntry for that new vertex so we can put it back in the
		// cache
		return ver;
	}

	/**
	 * Reads an edge from the disk and restores it.
	 * 
	 * @param key
	 *            The local id of the vertex
	 * @return A soft reference to the restored vertex
	 */
	public EdgeImpl readEdgeFromDisk(int key) {
		// read the data from the disk
		ByteBuffer buf = readGraphElementFromDisk(key, edgeFile, maxESize);

		// create a vertex that is identical to the vertex we deleted earlier
		EdgeImpl edge = restoreEdge(buf, key);

		// return a CacheEntry for that new vertex so we can put it back in the
		// cache
		return edge;
	}

	/**
	 * Helper method to avoid duplicate code in readVertexFromDisk and
	 * readEdgeFromDisk
	 */
	private ByteBuffer readGraphElementFromDisk(int key, FileAccess file,
			int byteSize) {
		ByteBuffer buf = file.read(byteSize, key * byteSize);

		buf.position(0);

		return buf;
	}

	/**
	 * Writes a String to the disk.
	 * 
	 * @param s
	 *            The String to be written to the disk
	 * 
	 * @return The position in strings.dst at which the String was stored, or -1
	 *         if the String was null.
	 */
	public long writeStringToDisk(String s) {
		if (s == null) {
			return -1;
		}

		// start write operation at the first free byte in strings.dst
		long currentPosition = stringsPointer;

		// convert the String to a byte array and obtain its length
		byte[] bytes = s.getBytes();
		int length = bytes.length;

		// write the length of the String into a ByteBuffer, followed by the
		// String itself
		ByteBuffer buf = ByteBuffer.allocate(4 + length);
		buf.putInt(length);
		buf.put(bytes);

		// write the contents of the buffer to strings.dst
		stringFile.write(buf, stringsPointer);

		// advance the pointer to first free byte in strings.dst
		stringsPointer += (4 + length);

		return currentPosition;
	}

	/**
	 * Reads a String from the disk.
	 * 
	 * @param position
	 *            The position at which the String is stored in strings.dst
	 * 
	 * @return The String, or a nullpointer if position was less than zero
	 */
	public String readStringFromDisk(long position) {
		if (position < 0) {
			return null;
		}

		// read the length of the string from the file
		ByteBuffer buf = stringFile.read(4, position);
		int length = buf.getInt(0);

		// read 'length' bytes
		String res = new String(stringFile.read(length, position + 4).array());

		return res;
	}

	/**
	 * Writes a List to the disk.
	 * 
	 * @param s
	 *            The List to be written to the disk
	 * 
	 * @return The position in lists.dst at which the List was stored, or -1 if
	 *         the List was null.
	 */
	public long writeListToDisk(List<?> l) {
		if (l == null) {
			return -1;
		}

		long currentPosition = listsPointer;

		byte[] bytes = serializeList(l);
		int length = bytes.length;

		ByteBuffer buf = ByteBuffer.allocate(4 + length);
		buf.putInt(length);
		buf.put(bytes);

		listFile.write(buf, listsPointer);

		listsPointer += (4 + length);

		return currentPosition;
	}

	/**
	 * Reads a List from the disk.
	 * 
	 * @param position
	 *            The position at which the List is stored in lists.dst
	 * 
	 * @return The List, or a nullpointer if position was less than zero
	 */
	public List<?> readListFromDisk(long position) {
		if (position == -1) {
			return null;
		}

		ByteBuffer buf = listFile.read(4, position);
		int length = buf.getInt(0);

		byte[] readBytes = listFile.read(length, position + 4).array();

		return restoreList(readBytes);
	}

	/**
	 * Restores a Vertex using the data provided by a ByteBuffer.
	 * 
	 * @param buf
	 *            The ByteBuffer holding the Vertex' variables
	 * @param key
	 *            The Vertex' id
	 * @return The restored Vertex
	 */
	private VertexImpl restoreVertex(ByteBuffer buf, int key) {
		// System.out.println("restore vertex: " + key);
		int typeId = buf.getInt(0) - 1;

		Schema schema = graph.getSchema();

		// get the vertex class for the typeId we read from the disk
		VertexClass verClass = (VertexClass) schema
				.getGraphElementClassById(typeId);

		GraphFactory factory = graph.getGraphFactory();

		VertexImpl ver = (VertexImpl) factory.restoreVertex(verClass, key,
				graph);
		ver.restoreNextVertexId((int) buf.getLong(4));
		ver.restorePrevVertexId((int) buf.getLong(12));

		ver.restoreFirstIncidenceId((int) buf.getLong(20));
		ver.restoreLastIncidenceId((int) buf.getLong(28));
		ver.restoreIncidenceListVersion(buf.getLong(36));

		// restore the vertex' variables and restore it
		return (VertexImpl) restoreGraphElement(ver, buf, typeId);

	}

	/**
	 * Restores a Vertex using the data provided by a ByteBuffer.
	 * 
	 * @param buf
	 *            The ByteBuffer holding the Vertex' variables
	 * @param key
	 *            The Vertex' id
	 * @return The restored Vertex
	 */
	private EdgeImpl restoreEdge(ByteBuffer buf, int key) {
		// System.out.println("restore edge: " + key);
		int typeId = buf.getInt(0) - 1;

		Schema schema = graph.getSchema();

		// get the edge class for the typeId we read from the disk
		EdgeClass edgeClass = (EdgeClass) schema
				.getGraphElementClassById(typeId);

		// get alpha and omega
		Vertex alpha = graph.getVertex((int) buf.getLong(36));
		Vertex omega = graph.getVertex((int) buf.getLong(60));

		// create a new Edge of the given edge class with the given ID
		GraphFactory factory = graph.getGraphFactory();

		if (alpha == null || omega == null) {
			// System.out.println("restore "+key + " from " + (int)
			// buf.getLong(36) + " to " +(int) buf.getLong(60) );
			if (alpha == null || omega == null) {
				throw new RuntimeException();
			}
		}

		EdgeImpl edge = (EdgeImpl) factory.restoreEdge(edgeClass, key, graph,
				alpha, omega);
		edge.restoreIncidentVertexId(alpha.getId());
		edge.restoreNextEdgeId((int) buf.getLong(4));
		edge.restorePrevEdgeId((int) buf.getLong(12));
		edge.restoreNextIncidenceId((int) buf.getLong(20));
		edge.restorePrevIncidenceId((int) buf.getLong(28));
		((ReversedEdgeImpl) edge.getReversedEdge())
				.restoreIncidentVertexId(omega.getId());
		((ReversedEdgeImpl) edge.getReversedEdge())
				.restoreNextIncidenceId((int) buf.getLong(44));
		((ReversedEdgeImpl) edge.getReversedEdge())
				.restorePrevIncidenceId((int) buf.getLong(52));

		// restore the edge's variables and restore it
		return (EdgeImpl) restoreGraphElement(edge, buf, typeId);

	}

	/**
	 * Helper method to avoid duplicate code in restoreVertex and restoreEdge
	 */
	private GraphElementImpl<?, ?> restoreGraphElement(
			GraphElementImpl<?, ?> ge, ByteBuffer buf, int typeId) {
		// restore the generated attributes of this GraphElement
		buf.position(64);
		GraphElementProfile prof = GraphElementProfile.getProfile(typeId);
		prof.restoreAttributesOfElement(ge, buf);

		// restore the Strings of this GraphElement
		long position;
		buf.position(prof.getStartOfStrings());

		int numElems = prof.getNumStrings();
		String[] strings = new String[numElems];

		for (int i = 0; i < numElems; i++) {
			position = buf.getLong();
			strings[i] = readStringFromDisk(position);
		}

		prof.restoreStringsOfElement(ge, strings);

		// restore the Lists of this GraphElement
		numElems = prof.getNumLists();
		List<?>[] lists = new List[numElems];

		for (int i = 0; i < numElems; i++) {
			position = buf.getLong();
			lists[i] = readListFromDisk(position);
		}

		prof.restoreListsOfElement(ge, lists);

		return ge;
	}

	/**
	 * Converts a List to a ByteArray using the Java Serialization API.
	 * 
	 * @param The
	 *            List to be serialized
	 * 
	 * @return The List as a ByteArray
	 */
	public byte[] serializeList(List<?> l) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objStream = new ObjectOutputStream(outStream);
			objStream.writeObject(l);
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize list");
		}
		return outStream.toByteArray();
	}

	/**
	 * Restore a List that was previously serialized.
	 * 
	 * @param readBytes
	 *            The byte array that represents the List
	 * 
	 * @return The reconstructed List
	 */
	public List<?> restoreList(byte[] readBytes) {
		ByteArrayInputStream inStream = new ByteArrayInputStream(readBytes);
		try {
			ObjectInputStream objReader = new ObjectInputStream(inStream);
			return (List<?>) objReader.readObject();
		} catch (IOException e) {
			throw new RuntimeException("Unable to create ObjectInputStream");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to restore list");
		}
	}

	// -------------------------------------------------------------------
	// Methods and Variables to enforce a maximum size of the disk storage
	// -------------------------------------------------------------------

	/**
	 * The current total size of all the files
	 */
	private static long diskStorageSize;

	/**
	 * The maximum allowed size for all the files. If this is less than zero, no
	 * limit has been set.
	 */
	private static long maxDiskStorageSize = -1;

	/**
	 * Set the maximum aloowed size for all the files
	 * 
	 * @param size
	 *            The maximum allowed size
	 */
	public static void setMaxDiskStorageSize(long size) {
		if (size < 1) {
			throw new IllegalArgumentException(
					"Maximum Disk Storage size must be bigger than zero");
		}
		maxDiskStorageSize = size;
		checkDiskStorage();
	}

	/**
	 * Increase 'diskStorageSize' if a file will grow
	 * 
	 * @param increment
	 *            The amount in Bytes by which the file will grow
	 */
	public static void increaseDiskStorageSize(long increment) {
		diskStorageSize += increment;
		checkDiskStorage();
	}

	/**
	 * Check if the growth of a file would make the total size of all files
	 * exceed the maximum allowed size, or if the size of the disk storage is
	 * beneath the new limit if a new limit has been set.
	 */
	private static void checkDiskStorage() {
		if (maxDiskStorageSize > 0 && diskStorageSize > maxDiskStorageSize) {
			throw new RuntimeException("Maximum Disk Storage size exceeded");
		}
	}
}
