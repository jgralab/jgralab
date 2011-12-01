package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;

import org.pcollections.*;

import de.uni_koblenz.jgralab.GraphIO;
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
	public static boolean testDomainConformity(Object value, Domain domain) throws ClassNotFoundException {
		if(domain instanceof BasicDomain) {
			return Class.forName(domain.getJavaClassName(null)).isInstance(value);
		}
		else {
			boolean result = true;
			if(domain instanceof EnumDomain) {
				result &= value instanceof PSet && ((EnumDomain) domain).getConsts().contains(value);
			}
			else if(domain instanceof SetDomain) {
				result &= value instanceof PSet;
				if(!result) {
					return false;
				}
				for(Object o : ((PSet<?>) value)) {
					result &= testDomainConformity(o, ((SetDomain) domain).getBaseDomain());
				}
			}
			else if(domain instanceof ListDomain) {
				result &= value instanceof PVector;
				if(!result) {
					return false;
				}
				for(Object o : ((PVector<?>) value)) {
					result &= testDomainConformity(o, ((ListDomain) domain).getBaseDomain());
				}
			}
			else if(domain instanceof MapDomain) {
				result &= value instanceof PMap;
				if(!result) {
					return false;
				}
				for(Object k : ((PMap<?,?>) value).keySet()) {
					result &= testDomainConformity(k, ((MapDomain) domain).getKeyDomain()) && testDomainConformity(((PMap<?, ?>) value).get(k), ((MapDomain) domain).getValueDomain());
				}
			}
			else if (domain instanceof RecordDomain) {
				result &= value instanceof Record;
				if(!result) {
					return false;
				}
				for(RecordComponent c : ((RecordDomain) domain).getComponents()) {
					result &= testDomainConformity(((Record) value).getComponent(c.getName()), c.getDomain());
				}
			}
			return result;
		}
	}
	

	@SuppressWarnings("unchecked")
	public static void serializeGenericAttribute(GraphIO io, Domain domain, Object data) throws IOException {
		// TODO Distinction of the cases should be more elegant!
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
				// TODO Record writeComponentValues?
			}
			else {
				io.writeIdentifier(GraphIO.NULL_LITERAL);
			}
		}
	}
}
