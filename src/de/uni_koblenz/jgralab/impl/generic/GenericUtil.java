package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.pcollections.*;

import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.schema.*;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;

public class GenericUtil {

	/**
	 * Checks, if the value of an attribute in the generic implementation conforms to it's domain.
	 * @param value
	 * @param domain
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean conformsToDomain(Object value, Domain domain) throws ClassNotFoundException {
		if(domain instanceof BasicDomain) {
			if(!(domain instanceof StringDomain)) {
				return Class.forName(domain.getJavaClassName(null)).isInstance(value);
			}
			else {
				return value == null || Class.forName(domain.getJavaClassName(null)).isInstance(value);
			}
		}
		else {
			boolean result = true;
			if(value == null) {
				return result;
			}
			if(domain instanceof EnumDomain) {
				result &= value instanceof String && ((EnumDomain) domain).getConsts().contains(value);
			}
			else if(domain instanceof SetDomain) {
				result &= value instanceof PSet;
				if(!result) {
					return false;
				}
				for(Object o : ((PSet<?>) value)) {
					result &= conformsToDomain(o, ((SetDomain) domain).getBaseDomain());
				}
			}
			else if(domain instanceof ListDomain) {
				result &= (value instanceof PVector);
				if(!result) {
					return false;
				}
				for(Object o : ((PVector<?>) value)) {
					result &= conformsToDomain(o, ((ListDomain) domain).getBaseDomain());
				}
			}
			else if(domain instanceof MapDomain) {
				result &= value instanceof PMap;
				if(!result) {
					return false;
				}
				for(Object k : ((PMap<?,?>) value).keySet()) {
					result &= conformsToDomain(k, ((MapDomain) domain).getKeyDomain()) && conformsToDomain(((PMap<?, ?>) value).get(k), ((MapDomain) domain).getValueDomain());
				}
			}
			else if (domain instanceof RecordDomain) {
				result &= value instanceof Record;
				if(!result) {
					return false;
				}
				for(RecordComponent c : ((RecordDomain) domain).getComponents()) {
					result &= conformsToDomain(((Record) value).getComponent(c.getName()), c.getDomain());
				}
			}
			return result;
		}
	}
	

	@SuppressWarnings("unchecked")
	public static void serializeGenericAttribute(GraphIO io, Domain domain, Object data) throws IOException {
		// TODO Handle distinction of the cases more elegantly - somehow?
		if(domain instanceof BooleanDomain) {
			io.writeBoolean((Boolean) data);
		}
		else if(domain instanceof IntegerDomain) {
			io.writeInteger((Integer) data);
		}
		else if(domain instanceof LongDomain) {
			io.writeLong((Long) data);
		}
		else if(domain instanceof DoubleDomain) {
			io.writeDouble((Double) data);
		}
		else if(domain instanceof StringDomain) {
			io.writeUtfString((String) data);
		}
		else if(domain instanceof EnumDomain) {
			if(data != null) {
				io.writeIdentifier((String) data);
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
		else if(domain instanceof SetDomain) {
			if(data != null) {
				io.writeSpace();
				io.write("{");
				io.noSpace();
				for(Object value : (PSet<Object>) data) {
					serializeGenericAttribute(io, ((SetDomain) domain).getBaseDomain(), value);
				}
				io.write("}");
				io.space();
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
		else if(domain instanceof ListDomain) {
			if(data != null) {
				io.writeSpace();
				io.write("[");
				io.noSpace();
				for(Object value : (PVector<Object>) data) {
					serializeGenericAttribute(io, ((ListDomain) domain).getBaseDomain(), value);
				}
				io.write("]");
				io.space();
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
		else if(domain instanceof MapDomain) {
			if(data != null) {
				io.writeSpace();
				io.write("{");
				io.noSpace();
				for(Object key : ((PMap<Object, Object>) data).keySet()) {
					serializeGenericAttribute(io, ((MapDomain) domain).getKeyDomain(), key);
					io.write(" -");
					serializeGenericAttribute(io, ((MapDomain) domain).getValueDomain(), ((PMap<Object, Object>) data).get(key));
				}
				io.write("}");
				io.space();
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
		else if(domain instanceof RecordDomain) {
			if(data != null) {
				io.writeSpace();
				io.write("(");
				io.noSpace();
				
				// TODO Ugly! Is the sorting necessary?
				// Sort components by lexicographic order, first!
				Collection<RecordDomain.RecordComponent> components = ((RecordDomain) domain).getComponents();
				TreeMap<String, Domain> sortedComponents = new TreeMap<String, Domain>();
				for(RecordDomain.RecordComponent c : components) {
					sortedComponents.put(c.getName(), c.getDomain());
				}
				
				for(String componentName : sortedComponents.navigableKeySet()) {
					serializeGenericAttribute(io, sortedComponents.get(componentName), ((Record) data).getComponent(componentName));
				}
				io.write(")");
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
		else {
			throw new GraphException("Unknown domain " + domain.getQualifiedName());
		}
	}
	
	/**
	 * Parses a String representing an attribute value and returns an Object representing the attribute
	 * value. The created Object's type is determined by the attribute's Domain and the generic TGraph
	 * implementation's mapping of types to the domains.
	 * <br /><br />
	 * The type mapping is as follows:
	 * <table>
	 * 	<tr><td><b>Domain</b></td><td><b>Java-type</b></td></tr>
	 *  <tr><td>BooleanDomain</td><td>Boolean</td></tr>
	 *  <tr><td>IntegerDomain</td><td>Integer</td></tr>
	 *  <tr><td>LongDomain</td><td>Long</td></tr>
	 *  <tr><td>DoubleDomain</td><td>Double</td></tr>
	 *  <tr><td>StringDomain</td><td>String</td></tr>
	 *  <tr><td>EnumDomain</td><td>String (possible values are determined be the EnumDomain)</td></tr>
	 *  <tr><td>SetDomain</td><td>PSet</td></tr>
	 *  <tr><td>ListDomain</td><td>PVector</td></tr>
	 *  <tr><td>MapDomain</td><td>PMap</td></tr>
	 *  <tr><td>RecordDomain</td><td>Record</td></tr>
	 * </table>
	 * 
	 * @param domain The Expected domain of the attribute's value.
	 * @param io The {@link GraphIO} object serving as parser for the attribute's value.
	 * @return An Object representing the attribute value.
	 * @throws GraphIOException 
	 */
	public static Object parseGenericAttribute(Domain domain, GraphIO io) throws GraphIOException {
		if(domain instanceof BooleanDomain) {
			Boolean result = io.matchBoolean();
			return result;
		}
		else if(domain instanceof IntegerDomain) {
			Integer result = io.matchInteger();
			return result;
		}
		else if(domain instanceof LongDomain) {
			Long result = io.matchLong();
			return result;
		}
		else if(domain instanceof DoubleDomain) {
			Double result = io.matchDouble();
			return result;
		}
		else if(domain instanceof StringDomain) {
			String result = io.matchUtfString();
			return result;
		}
		else if(domain instanceof EnumDomain) {
			String result = io.matchEnumConstant();
			return result;
			
		}
		else if(domain instanceof SetDomain) {
			if(io.isNextToken("{")) {
				PSet<Object> result = JGraLab.set();
				io.match("{");
				while (!io.isNextToken("}")) {
					Object setElement = null;
					setElement = parseGenericAttribute(((SetDomain) domain).getBaseDomain(), io);
					result = result.plus(setElement);
				}
				io.match("}");
				return result;
			}
			else if (io.isNextToken(GraphIO.NULL_LITERAL)) {
				io.match();
				return null;
			}
			else {
				return null;
			}
		}
		else if(domain instanceof ListDomain) {
			if (io.isNextToken("[")) {
				PVector<Object> result = JGraLab.vector();
				io.match("[");
				while (!io.isNextToken("]")) {
					Object listElement = null;
					listElement = parseGenericAttribute(((ListDomain) domain).getBaseDomain(), io);
					result = result.plus(listElement);
				}
				io.match("]");
				return result;
			}
			else if(io.isNextToken(GraphIO.NULL_LITERAL)) {
				io.match(); 
				return null;
			} else {
				return null;
			}
		}
		else if(domain instanceof MapDomain) {
			if (io.isNextToken("{")) {
				PMap<Object, Object> result = JGraLab.map();
				io.match("{");
				while (!io.isNextToken("}")) {
					Object mapKey = null;
					Object mapValue = null;
					mapKey = parseGenericAttribute(((MapDomain) domain).getKeyDomain(), io);
					io.match("-");
					mapValue = parseGenericAttribute(((MapDomain) domain).getValueDomain(), io);
					result = result.plus(mapKey, mapValue);
				}
				io.match("}");
				return result;
			}
			else if (io.isNextToken(GraphIO.NULL_LITERAL)) {
				io.match();
				return null;
			} else {
				return null;
			}
		}
		else if(domain instanceof RecordDomain) {
			if(io.isNextToken("(")) {
				de.uni_koblenz.jgralab.impl.RecordImpl result = de.uni_koblenz.jgralab.impl.RecordImpl.empty();
				io.match("(");

				// TODO This is ugly. Is it necessary?
				// Component values are expected in lexicographic order -> sort them first!
				Collection<RecordDomain.RecordComponent> components = ((RecordDomain) domain).getComponents();
				TreeMap<String, Domain> sortedComponents = new TreeMap<String, Domain>();
				for(RecordDomain.RecordComponent c : components) {
					sortedComponents.put(c.getName(), c.getDomain());
				}
				Iterator<String> componentIterator = sortedComponents.navigableKeySet().iterator();
				String componentName = componentIterator.next();
				while(!io.isNextToken(")")) {
					Domain componentDomain = sortedComponents.get(componentName);
					Object componentValue = null;
					componentValue = parseGenericAttribute(componentDomain, io);
					result = result.plus(componentName, componentValue);
					componentName = componentIterator.hasNext() ? componentIterator.next() : null;
				}
				io.match(")");
				return result;
			}
			else if(io.isNextToken(GraphIO.NULL_LITERAL)){
				io.match();
				return null;
			}
			else {
				throw new GraphIOException("This is no record!");
			}
		}
		else {
			throw new GraphException("Unknown domain " + domain.getQualifiedName());
		}
	}
}
