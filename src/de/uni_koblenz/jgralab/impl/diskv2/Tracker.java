package de.uni_koblenz.jgralab.impl.diskv2;

import java.nio.ByteBuffer;
import java.util.List;

import de.uni_koblenz.jgralab.impl.GraphElementImpl;

/**
 * Abstract class used to track a graph object. Every tracker tracks exactly one
 * graph object. A tracker for a graph object is instantiated if the object is
 * newly created, or if the object has been restored from the disk and one of
 * its attributes has been changed.
 * 
 * @author aheld
 */
public abstract class Tracker<T extends GraphElementImpl<?, ?>> {

	/**
	 * The amount of memory in bytes an edge uses on the disk
	 */
	protected static final int EDGE_BASE_SIZE = 68;

	/**
	 * The amount of memory in bytes a Vertex, sans its attributes, needs on the
	 * disk
	 */
	protected static final int VERTEX_BASE_SIZE = 44;

	/**
	 * A buffer used to track the variables of a GraphElement or Incidence It is
	 * used to track the attributes that are the same for every Incidence or
	 * every GraphElement, i.e. the variables that are not declared in the
	 * generated code. The ClassId of each object is also stored here.
	 * 
	 * If the tracked object is an Incidence, this Buffer holds six values of
	 * type long and its size is 52 Bytes. If the tracked object is a
	 * GraphElement, it holds seven values of type long and one value of type
	 * int, which means its size is 64 Bytes.
	 * 
	 * Every index in the buffer is reserved for a specific attribute.
	 * 
	 * For an Edge, the indexes are: 0 - EdgeClassId 4 - nextElementId 12 -
	 * previousElementId 20 - nextIncidenceId 28 - previousIncidenceId 36 -
	 * incidentVertexId 44 - nextIncidenceReversedId 52 -
	 * previousIncidenceReversedId 60 - incidentVertexReversedId
	 * 
	 * For a Vertex, the indexes are: 0 - VertexClassId 4 - nextElementId 12 -
	 * previousElementId 20 - firstIncidenceId 28 - lastIncidenceId 36 -
	 * incidenceListVersion
	 */
	protected ByteBuffer variables;

	/**
	 * Creates a new Tracker.
	 * 
	 * @param size
	 *            - The accumulated size of all the attributes that are be
	 *            tracked.
	 */
	protected Tracker(int size) {
		variables = ByteBuffer.allocate(size);
	}

	/**
	 * Puts an attribute into the buffer.
	 * 
	 * @param attribute
	 *            - the attribute to be tracked
	 * @param index
	 *            - the position at which the tracked attribute is stored
	 */
	public void putVariable(int index, long variable) {
		variables.putLong(index, variable);
	}

	/**
	 * Store the attributes, Strings and Lists of a GraphElement.
	 */
	private ByteBuffer attributes;
	private String[] strings;
	private List<?>[] lists;

	/**
	 * Stores the variables of a GraphElement in the ByteBuffer.
	 * 
	 * @param ge
	 *            - The GraphElement to be tracked
	 */
	public void fill(T ge) {
		storeAttributes(ge);
		storeStrings(ge);
		storeLists(ge);
	}

	protected abstract void storeVariables(T ge);

	/**
	 * Stores the attributes of a GraphElement in the ByteBuffer.
	 * 
	 * @param ge
	 *            - The GraphElement to be tracked
	 */
	public void storeAttributes(T ge) {
		storeVariables(ge);
		int typeId = ge.getAttributedElementClass()
				.getGraphElementClassIdInSchema();
		GraphElementProfile profile = GraphElementProfile.getProfile(typeId);
		attributes = profile.getAttributesForElement(ge);
	}

	/**
	 * Stores the attributes of a GraphElement in the ByteBuffer.
	 * 
	 * @param ge
	 *            - The GraphElement to be tracked
	 */
	public void storeStrings(T ge) {
		// TODO avoid doing this
		if (attributes == null) {
			storeAttributes(ge);
		}
		int typeId = ge.getAttributedElementClass()
				.getGraphElementClassIdInSchema();
		GraphElementProfile profile = GraphElementProfile.getProfile(typeId);
		strings = profile.getStringsForElement(ge);
	}

	/**
	 * Stores the attributes of a GraphElement in the ByteBuffer.
	 * 
	 * @param ge
	 *            - The GraphElement to be tracked
	 */
	public void storeLists(T ge) {
		// TODO avoid doing this
		if (attributes == null) {
			storeAttributes(ge);
		}
		int typeId = ge.getAttributedElementClass()
				.getGraphElementClassIdInSchema();
		GraphElementProfile profile = GraphElementProfile.getProfile(typeId);
		lists = profile.getListsForElement(ge);
	}

	/**
	 * Put the contents of 'variables' and 'attributes' in a single Buffer and
	 * return it
	 */
	public ByteBuffer getVariables() {
		if (attributes == null) {
			return variables;
		}

		int totalSize = variables.capacity() + attributes.capacity();
		ByteBuffer buf = ByteBuffer.allocate(totalSize);
		buf.put(variables.array());
		buf.put(attributes.array());
		return buf;
	}

	/**
	 * Return all Strings
	 */
	public String[] getStrings() {
		return strings;
	}

	/**
	 * Return all Lists
	 */
	public List<?>[] getLists() {
		return lists;
	}
}
