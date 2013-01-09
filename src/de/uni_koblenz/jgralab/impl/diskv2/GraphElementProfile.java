package de.uni_koblenz.jgralab.impl.diskv2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Stack;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This class is used to detect and store the attributes of generated vertex and edge
 * classes. It stores the type of every generated attribute and offers 
 * ways to invoke the get- and set-methods for every attribute.
 * For every non-abstract vertex and edge class, there must be exactly one profile.
 * A profile is valid for all Edges and Vertices of the same class. 
 *  
 * @author aheld
 *
 */
public class GraphElementProfile {
	
	private static GraphElementProfile[] profiles;
		
	/**
	 * The size that the profiled GraphElement needs on the disk.
	 * This includes variables, like kappa and firstIncidenceId,
	 * as well as all generated attributes that are of primitive types.
	 */
	private int size;
	
	/**
	 * The positions at which the addresses of the Strings and Lists will be stored
	 * on the disk, relative to the position where the data for the GraphElement begins. 
	 * For example, if a vertex needs 80 bytes for its primitive attributes, then 
	 * startOfStrings #will be 81. 
	 */
	private int startOfStrings;
	private int startOfLists;
	
	/**
	 * Array holding the type IDs of the generated primitive attributes
	 * Each ID corresponds to a type. The table is:
	 * 0 -> boolean
	 * 1 -> integer
	 * 2 -> long
	 * 3 -> double
	 */
	private byte[] attrTypeIDs;
	
	/**
	 * Array holding the get method for every generated attribute
	 * that is a primitive
	 */
	private Method[] attrGetters;
	
	/**
	 * Array holding the set method for every generated attribute
	 * that is a primitive
	 * 
	 */
	private Method[] attrSetters;
	
	//constraints for the three arrays listed above:
	//- all four arrays must have the same length.
	//- attrTypeIDs[n], getters[n] and setters[n] refer to the type, the get method and the
	//  set method for the same attribute.
	
	/**
	 * Array holding the get method for every generated attribute
	 * that is a String
	 */
	private Method[] stringGetters;
	
	/**
	 * Array holding the set method for every generated attribute
	 * that is a String
	 * 
	 */
	private Method[] stringSetters;
	
	/**
	 * Array holding the get method for every generated attribute
	 * that is a List
	 */
	private Method[] listGetters;
	
	/**
	 * Array holding the set method for every generated attribute
	 * that is a List
	 * 
	 */
	private Method[] listSetters;
	
	/**
	 * Instantiates the Array that stores all profiles.
	 * 
	 * @param size
	 * 		The maximum amount of profiles we will need to store
	 */
	public static void setup(int size){
		profiles = new GraphElementProfile[size];
	}
	
	/**
	 * Creates a new profile for the given vertex class.
	 * 
	 * @param cls
	 * 		The vertex class to be profiled
	 * @param typeId
	 * 		The internal ID of the given vertex class
	 * @param graphdb
	 * 		The GraphDatabase that we work with
	 */
	public static int createProfile(VertexClass cls, int typeId, GraphFactory f){
		GraphElementProfile profile = new GraphElementProfile(f.getVertexImplementationClass(cls),
				//((AttributedElementClassImpl)cls).getSchemaImplementationClass(ImplementationType.DISKV2), 
				typeId);
		System.err.println("DEBUG: profile created "+cls);
		profiles[typeId] = profile;
		return profile.getSize();
	}
	
	/**
	 * Creates a new profile for the given edge class.
	 * 
	 * @param cls
	 * 		The edge class to be profiled
	 * @param typeId
	 * 		The internal ID of the given edge class
	 * @param graphdb
	 * 		The GraphDatabase that we work with
	 */
	public static int createProfile(EdgeClass cls, int typeId, GraphFactory f){
		GraphElementProfile profile = new GraphElementProfile(
				f.getEdgeImplementationClass(cls),
				//((AttributedElementClassImpl)cls).getSchemaImplementationClass(ImplementationType.DISKV2), 
				typeId);
		profiles[typeId] = profile;
		return profile.getSize();
	}
	
	/**
	 * Get a profile for the vertex or edge class which has the given internal ID.
	 * 
	 * @param typeId
	 * 		The internal ID of the vertex or edge class
	 * @return
	 * 		A profile for the specified vertex or edge class
	 */
	public static GraphElementProfile getProfile(int typeId){
		return profiles[typeId];
	}
	
	/**
	 * Returns the size of the profiled vertex or edge class
	 * 
	 * @return
	 * 		How many bytes an object of the profiled class needs on the disk 
	 */
	public int getSize(){
		return size;
	}
	
	public int getStartOfStrings(){
		return startOfStrings;
	}
	
	public int getStartOfLists(){
		return startOfLists;
	}
	
	/**
	 * Get the number of attributes of the profiled vertex or edge class that are Strings
	 *   
	 * @return
	 * 		The number of Strings
	 */
	public int getNumStrings(){
		return stringGetters.length;
	}

	/**
	 * Get the number of attributes of the profiled vertex or edge class that are Lists
	 *   
	 * @return
	 * 		The number of Lists
	 */
	public int getNumLists(){
		return listGetters.length;
	}
	
	/**
	 * Returns a ByteBuffer containing the primitive attributes of a GraphElement.
	 * 
	 * @param ge 
	 * 		The GraphElement whose attributes are written to the buffer
	 * 
	 * @return 
	 * 		A byte array containing the given GraphElement's attributes
	 */
	public ByteBuffer getAttributesForElement(GraphElement<?,?> ge){
		//make enough room to store all attributes
		ByteBuffer buf = ByteBuffer.allocate(startOfStrings - 64);
		//iterate over every attribute
		for (int i = 0; i < attrTypeIDs.length; i++){
			switch (attrTypeIDs[i]){
				case 0: //attribute is a Boolean, invoke its get method and store it
					if (invokeGetBoolean(ge, i))
						buf.put((byte) 1);
					else buf.put((byte) 0);
					break;
				case 1: //attribute is an Integer, invoke its get method and store it
					buf.putInt(invokeGetInteger(ge, i));
					break;
				case 2: //attribute is a Long, invoke its get method and store it
					buf.putLong(invokeGetLong(ge, i));
					break;
				case 3: //attribute is an Double, invoke its get method and store it
					buf.putDouble(invokeGetDouble(ge, i));
					break;
			}
		}
		
		return buf;
	}
	
	/**
	 * Writes the primitive attributes of a GraphElement.
	 * 
	 * @param ge
	 *      The GraphElement whose attributes are overwritten
	 *      
	 * @param buf
	 *      A ByteBuffer containing the values of the attributes
	 */
	public void restoreAttributesOfElement(GraphElement<?,?> ge, ByteBuffer buf){
		for (int i = 0; i < attrTypeIDs.length; i++){
			
			//iterate over every attribute
			switch (attrTypeIDs[i]){
				case 0: //attribute is a Boolean, invoke its set method and restore it
					if (buf.get() == 1)
						invokeSetBoolean(ge, true, i);
					else invokeSetBoolean(ge, false, i);
					break;
				case 1: //attribute is an Integer, invoke its set method and restore it
					invokeSetInteger(ge, buf.getInt(), i);
					break;
				case 2: //attribute is a Long, invoke its set method and restore it
					invokeSetLong(ge, buf.getLong(), i);
					break;
				case 3: //attribute is an Double, invoke its set method and restore it
					invokeSetDouble(ge, buf.getDouble(), i);
					break;
			}
		}
	}
	
	/**
	 * Returns an Array containing the String attributes of a GraphElement.
	 * 
	 * @param ge 
	 * 		The GraphElement whose Strings are written to the Array
	 * 
	 * @return 
	 * 		An Array containing the given GraphElement's Strings
	 */
	public String[] getStringsForElement(GraphElement<?,?> ge){
		if (stringGetters.length == 0) return null;
		
		String[] strings = new String[stringGetters.length];
		for(int i = 0; i < stringGetters.length; i++){
			strings[i] = invokeGetString(ge, i);
		}
		return strings;
	}
	
	/**
	 * Writes the String attributes of a GraphElement.
	 * 
	 * @param ge
	 *      The GraphElement whose Strings are overwritten
	 *      
	 * @param buf
	 *      An Array containing the Strings
	 */
	public void restoreStringsOfElement(GraphElement<?,?> ge, String[] strings){
		for(int i = 0; i < strings.length; i++){
			invokeSetString(ge, strings[i], i);
		}
	}
	
	/**
	 * Returns an Array containing the List attributes of a GraphElement.
	 * 
	 * @param ge 
	 * 		The GraphElement whose Lists are written to the Array
	 * 
	 * @return 
	 * 		An Array containing the given GraphElement's Lists
	 */
	public List<?>[] getListsForElement(GraphElement<?,?> ge){
		if (listGetters.length == 0) return null;
	
		List<?>[] lists = new List[listGetters.length];
		for(int i = 0; i < listGetters.length; i++){
			lists[i] = invokeGetList(ge, i);
		}
		return lists;
	}
	
	/**
	 * Writes the List attributes of a GraphElement.
	 * 
	 * @param ge
	 *      The GraphElement whose Lists are overwritten
	 *      
	 * @param buf
	 *      An Array containing the Lists
	 */
	public void restoreListsOfElement(GraphElement<?,?> ge, List<?>[] lists){
		for(int i = 0; i < lists.length; i++){
			invokeSetList(ge, lists[i], i);
		}
	}
	
	/**
	 * Creates a new profile for a given class, which must be a subclass of
	 * either VertexImpl or EdgeImpl.
	 * 
	 * @param cls - The Edge or Vertex class to be profiled
	 */
	private GraphElementProfile(Class<?> cls, int typeId){
		Field[] fields = cls.getDeclaredFields();
		
		Stack<Field> primitives = new Stack<Field>();
		Stack<Field> strings = new Stack<Field>();
		Stack<Field> lists = new Stack<Field>();
		
		byte attrTypeID;
		
		for (Field f: fields){
			attrTypeID = getTypeID(f.getType());
			switch (attrTypeID){
				case 4:
					strings.push(f);
					break;
				case 5:
					lists.push(f);
					break;
				default:
					primitives.push(f);
			}
		}
		
		initArrays(primitives.size(), strings.size(), lists.size());
		
		detectAttrTypes(primitives);
		
		detectGetters(cls, primitives, attrGetters, attrTypeIDs);
		detectGetters(cls, strings, stringGetters, null);
		detectGetters(cls, lists, listGetters, null);
		
		detectSetters(cls, primitives, attrSetters);
		detectSetters(cls, strings, stringSetters);
		detectSetters(cls, lists, listSetters);
		
		detectSize();
	}
	
	/**
	 * Initializes all the arrays in this class with given lengths.
	 * 
	 * @param numAttrs
	 * 		The length of the arrays attrTypeIDs, attrGetters and attrSetters
	 * 
	 * @param numStrings
	 * 		The length of the arrays stringGetters and stringSetters
	 * 
	 * @param numLists
	 * 		The length of the arrays listGetters and listSetters
	 */
	private void initArrays(int numAttrs, int numStrings, int numLists){
		attrTypeIDs = new byte[numAttrs];
		attrGetters = new Method[numAttrs];
		attrSetters = new Method[numAttrs];
		stringGetters = new Method[numStrings];
		stringSetters = new Method[numStrings];
		listGetters = new Method[numLists];
		listSetters = new Method[numLists];
	}
	
	/**
	 * Detect the typeID for each primitive on the given Stack.
	 * 
	 * @param primitive
	 * 		A stack containing primitive data types, e.g. integer or double.
	 */
	private void detectAttrTypes(Stack<Field> primitives){
		int index = 0;
		for (Field f: primitives){
			attrTypeIDs[index] = getTypeID(f.getType());
			index++;
		}
	}

	/**
	 * Uses the Java Reflection API to obtain all get methods for the attributes
	 * of a GraphElement class. These are stored in the array getters so they
	 * can be invoked later without having to use reflection again.
	 * 
	 * @param cls 
	 *     The GraphElement class to be profiled
	 * 
	 * @param attributes 
	 *     A stack holding the attributes of the given class
	 * 
	 * @param getters
	 * 	   The Array in which the get methods are stored
	 */
	private void detectGetters(Class<?> cls, Stack<Field> attributes, Method[] getters,byte[] attrTypeIDs){
		String methodName;
		Method m;
		int index = 0;
		
		for(Field f: attributes){
			if (attrTypeIDs != null && attrTypeIDs[index] == 0){
				methodName = "is" + f.getName();
			}
			else {
				methodName = "get" + f.getName();
			}
			try {
				//fetch the get method and store it in the array
				m = cls.getMethod(methodName, (Class<?>[]) null);
				getters[index] = m;
				index++;
			} catch (SecurityException e) {
				throw new IllegalArgumentException("Method " + methodName + " is not public");
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Unknown method " + methodName);
			}
		}
	}
	
	/**
	 * Uses the Java Reflection API to obtain all set methods for the attributes
	 * of a GraphElement class. These are stored in the array setters so they
	 * can be invoked later without having to use reflection again.
	 * 
	 * @param cls 
	 *     The GraphElement class to be profiled
	 * 
	 * @param primitives 
	 *     A stack holding the attributes of the given class
	 * 
	 * @param getters
	 * 	   The Array in which the set methods are stored
	 */
	private void detectSetters(Class<?> cls, Stack<Field> attributes, Method[] setters){
		byte attrTypeID;
		String methodName;
		Method m = null;
		int index = 0;
		
		for (Field f: attributes){
			attrTypeID = getTypeID(f.getType());
			methodName = "set" + f.getName();
			Class<?>[] parameter = new Class[1];
			switch (attrTypeID){
				case 0:
					m = detectBooleanSetter(methodName, cls, parameter);
					break;
				case 1:
					m = detectIntegerSetter(methodName, cls, parameter);
					break;
				case 2:
					m = detectLongSetter(methodName, cls, parameter);
					break;
				case 3:
					m = detectDoubleSetter(methodName, cls, parameter);
					break;
				case 4:
					m = detectStringSetter(methodName, cls, parameter);
					break;
				case 5:
					m = detectListSetter(methodName, cls, parameter);
					break;
				default:
					assert false;
 			}
			setters[index] = m;
			index++;
		}
	}
	
	/**
	 * Detect the proper values for startOfStrings, startOfLists and size
	 */
	private void detectSize(){
		for (int id: attrTypeIDs){
			switch(id){
				case 0:
					size++;
					break;
				case 1:
					size += 4;
					break;
				case 2:
				case 3:
					size += 8;
					break;
				default:
					assert false;
			}
		}
		
		startOfStrings = size + 64;
		startOfLists = startOfStrings + stringGetters.length * 8;
		size = startOfLists + listGetters.length * 8;
	}
	
	/**
	 * Helper method to obtain a set method for an attribute of type boolean.
	 */
	private Method detectBooleanSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = Boolean.TYPE;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Helper method to obtain a set method for an attribute of type integer.
	 */
	private Method detectIntegerSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = Integer.TYPE;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Helper method to obtain a set method for an attribute of type long.
	 */
	private Method detectLongSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = Long.TYPE;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Helper method to obtain a set method for an attribute of type double.
	 */
	private Method detectDoubleSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = Double.TYPE;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Helper method to obtain a set method for an attribute of type String.
	 */
	private Method detectStringSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = String.class;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Helper method to obtain a set method for a List.
	 */
	private Method detectListSetter(String methodName, Class<?> cls, Class<?>[] parameter){
		parameter[0] = java.util.List.class;
		try {
			return cls.getMethod(methodName, parameter);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Method " + methodName + " is not public");
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unknown method " + methodName);
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type boolean. 
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetBoolean(GraphElement<?,?> ge, boolean argument, int position){
		try {
			attrSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type int.
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetInteger(GraphElement<?,?> ge, int argument, int position){
		try {
			attrSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type long. 
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetLong(GraphElement<?,?> ge, long argument, int position){
		try {
			attrSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type double.
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetDouble(GraphElement<?,?> ge, double argument, int position){
		try {
			attrSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type String. 
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetString(GraphElement<?,?> ge, String argument, int position){
		try {
			stringSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the set method for an attribute of type List.
	 * 
	 * @param ge - The GraphElement whose set method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param argument - The argument that is passed to the set method.
	 * @param position - The position at which the set method invoked by this method is stored in the
	 *                   array 'setters'. The type of this method's argument must be the same as the 
	 *                   type of the argument passed to this method.
	 */
	private void invokeSetList(GraphElement<?,?> ge, List<?> argument, int position){
		try {
			listSetters[position].invoke(ge, argument);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrSetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrSetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type Boolean.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private boolean invokeGetBoolean(GraphElement<?,?> ge, int position){
		try {
			return (Boolean) attrGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type Integer.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private int invokeGetInteger(GraphElement<?,?> ge, int position){
		try {
			return (Integer) attrGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type Long.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private long invokeGetLong(GraphElement<?,?> ge, int position){
		try {
			return (Long) attrGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type Double.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private double invokeGetDouble(GraphElement<?,?> ge, int position){
		try {
			return (Double) attrGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type String.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private String invokeGetString(GraphElement<?,?> ge, int position){
		try {
			return (String) stringGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Method to invoke the get method for an attribute of type List.
	 * 
	 * @param ge - The GraphElement whose get method shall be invoked. Its class must be the same class
	 *             that was passed to the Constructor of the GraphElementProfile object for which
	 *             this method is called. 
	 * @param position - The position at which the get method invoked by this method is stored in the
	 *                   array 'getters'. The invoked method's return type must be the same as the 
	 *                   return type of this method.
	 */
	private List<?> invokeGetList(GraphElement<?,?> ge, int position){
		try {
			return (List<?>) listGetters[position].invoke(ge, (Object[]) null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Method " + attrGetters[position].getName() + " is not public");
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Exception thrown by invoked method " + attrGetters[position].getName());
		}
	}
	
	/**
	 * Helper method that returns the ID for a given type. For the full table, 
	 * see documentation of attrTypeIDs.
	 */
	private byte getTypeID(Class<?> type){
		String typeName = type.getName();
		
		if (typeName.equals("boolean")) return 0;
		if (typeName.equals("int")) return 1;
		if (typeName.equals("long")) return 2;
		if (typeName.equals("double")) return 3;
		if (typeName.equals("java.lang.String")) return 4;
		if (typeName.equals("java.util.List")) return 5;
		
		throw new IllegalArgumentException("Unknown attribute type: " + type);
	}
	
	/**
	 * Helper method to return the name of the
	 *  type for a given type ID. Used to
	 * output a profile in a human-readable form.
	 */
	private String getTypeName(byte typeID){
		switch (typeID){
			case 0: return "Boolean";
			case 1: return "Integer";
			case 2: return "Long";
			case 3: return "Double";
			case 4: return "String";
			case 5: return "List";
			default: throw new IllegalArgumentException ("Unknown type ID: " + typeID);
		}
	}
	
	@Override
	public String toString(){
		String output = "Size: " + Integer.toString(size) + " Bytes\n";
		output += "startOfStrings: " + Integer.toString(startOfStrings) + "\n";
		output += "startOfLists: " + Integer.toString(startOfLists) + "\n\n";
		output += ("Primitives:\n");
		for (int i = 0; i < attrTypeIDs.length; i++){
			output += "Type:   " + getTypeName(attrTypeIDs[i]) + "\n";
			output += "Getter: " + attrGetters[i].getName() + "\n";
			output += "Setter: " + attrSetters[i].getName() + "\n";
		}
		output += ("\nStrings:\n");
		for (int i = 0; i < stringGetters.length; i++){
			output += "Getter: " + stringGetters[i].getName() + "\n";
			output += "Setter: " + stringSetters[i].getName() + "\n";
		}
		output += ("\nLists:\n");
		for (int i = 0; i < listGetters.length; i++){
			output += "Getter: " + listGetters[i].getName() + "\n";
			output += "Setter: " + listSetters[i].getName() + "\n";
		}
		return output;
	}
}
