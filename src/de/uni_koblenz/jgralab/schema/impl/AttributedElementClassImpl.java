/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.schema.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

public abstract class AttributedElementClassImpl<SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>>
		extends NamedElementImpl implements AttributedElementClass<SC, IC> {

	/**
	 * the list of all attributes. Own attributes and inherited attributes are
	 * stored here - but only if the schema is finished
	 */
	protected PVector<Attribute> allAttributes;

	/**
	 * A set of {@link Constraint}s which can be used to validate the graph.
	 */
	protected PSet<Constraint> constraints;

	/**
	 * maps each attribute to an index -- computed on schema finish
	 */
	protected HashMap<String, Integer> attributeIndex;

	/**
	 * true if the schema is finish
	 */
	protected boolean finished;

	/**
	 * true if element class is abstract
	 */
	private boolean isAbstract;

	/**
	 * The class object representing the generated interface for this
	 * AttributedElementClass
	 */
	private Class<IC> schemaClass;

	/**
	 * The class object representing the implementation class for this
	 * AttributedElementClass. This may be either the generated class or a
	 * subclass of this
	 */
	private Class<IC> schemaImplementationClass;

	protected AttributedElementClassImpl(String simpleName, PackageImpl pkg,
			SchemaImpl schema) {
		super(simpleName, pkg, schema);
		allAttributes = ArrayPVector.empty();
		constraints = ArrayPSet.empty();
	}

	protected Attribute createAttribute(Attribute anAttribute) {
		assertNotFinished();

		if (containsAttribute(anAttribute.getName())) {
			throw new SchemaException("Duplicate attribute '"
					+ anAttribute.getName() + "' in AttributedElementClass '"
					+ getQualifiedName() + "'");
		}
		TreeSet<Attribute> s = new TreeSet<Attribute>(allAttributes);
		s.add(anAttribute);
		allAttributes = ArrayPVector.<Attribute> empty().plusAll(s);
		return anAttribute;
	}

	@Override
	public Attribute createAttribute(String name, Domain domain,
			String defaultValueAsString) {
		return createAttribute(new AttributeImpl(name, domain, this,
				defaultValueAsString));
	}

	@Override
	public Attribute createAttribute(String name, Domain domain) {
		return createAttribute(new AttributeImpl(name, domain, this, null));
	}

	@Override
	public void addConstraint(Constraint constraint) {
		assertNotFinished();
		constraints = constraints.plus(constraint);
	}

	/**
	 * @return a textual representation of all attributes the element holds
	 */
	protected String attributesToString() {
		StringBuilder output = new StringBuilder("Attributes:\n");
		for (Attribute a : getAttributeList()) {
			output.append("\t" + a.toString() + "\n");
		}
		return output.toString();
	}

	@Override
	public boolean containsAttribute(String name) {
		if (finished) {
			return attributeIndex.containsKey(name);
		}
		return (getAttribute(name) != null);
	}

	@Override
	public Attribute getAttribute(String name) {
		for (Attribute a : allAttributes) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	@Override
	public int getAttributeCount() {
		return allAttributes.size();
	}

	@Override
	public List<Attribute> getAttributeList() {
		return allAttributes;
	}

	@Override
	public PSet<Constraint> getConstraints() {
		return constraints;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IC> getSchemaClass() {
		if (schemaClass == null) {
			String schemaClassName = schema.getPackagePrefix() + "."
					+ getQualifiedName();
			try {
				schemaClass = (Class<IC>) Class.forName(schemaClassName, true,
						SchemaClassManager.instance(schema.getQualifiedName()));
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"Can't load (generated) schema class for AttributedElementClass '"
								+ getQualifiedName() + "'", e);
			}
		}
		return schemaClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IC> getSchemaImplementationClass() {
		if (isAbstract()) {
			throw new SchemaClassAccessException(
					"Can't get (generated) schema implementation class. AttributedElementClass '"
							+ getQualifiedName() + "' is abstract!");
		}
		if (schemaImplementationClass == null) {
			try {
				Field f = getSchemaClass().getField("IMPLEMENTATION_CLASS");
				schemaImplementationClass = (Class<IC>) f.get(schemaClass);
				
				
			} catch (SecurityException e) {
				throw new SchemaClassAccessException(e);
			} catch (NoSuchFieldException e) {
				throw new SchemaClassAccessException(e);
			} catch (IllegalArgumentException e) {
				throw new SchemaClassAccessException(e);
			} catch (IllegalAccessException e) {
				throw new SchemaClassAccessException(e);
			}
		}
		return schemaImplementationClass;
	}
	
	@SuppressWarnings("unchecked")
	//@Override
	public Class<IC> getSchemaImplementationClass(ImplementationType implType) {
		if (isAbstract()) {
			throw new SchemaClassAccessException(
					"Can't get (generated) schema implementation class. AttributedElementClass '"
							+ getQualifiedName() + "' is abstract!");
		}
		if (schemaImplementationClass == null) {
			try {
				String sc = getSchemaClass().getName();
				String packString = sc.substring(0,sc.lastIndexOf("."));
				String nameString = sc.substring(sc.lastIndexOf("."));
				String impltype = (implType == ImplementationType.STANDARD) ?  "std" : "diskv2";
				String newname = packString + ".impl."+impltype + nameString + "Impl";
				schemaImplementationClass = (Class<IC>)Class.forName(newname);
				//Field f = getSchemaClass().getField("IMPLEMENTATION_CLASS");
				//schemaImplementationClass = (Class<IC>) f.get(schemaClass);
				
				
			} catch (SecurityException e) {
				throw new SchemaClassAccessException(e);
			} catch (ClassNotFoundException e){
				throw new SchemaClassAccessException(e);
			//} catch (NoSuchFieldException e) {
			//	throw new SchemaClassAccessException(e);
			} catch (IllegalArgumentException e) {
				throw new SchemaClassAccessException(e);
			//} catch (IllegalAccessException e) {
			//	throw new SchemaClassAccessException(e);
			}
		}
		return schemaImplementationClass;
	}

	@Override
	public boolean hasAttributes() {
		return !getAttributeList().isEmpty();
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * Called if the schema is finished, saves complete subclass, superclass and
	 * attribute list
	 */
	protected void finish() {
		assert allAttributes != null;

		attributeIndex = new HashMap<String, Integer>();
		int i = 0;
		for (Attribute a : allAttributes) {
			attributeIndex.put(a.getName(), i);
			++i;
		}

		finished = true;
	}

	protected boolean isFinished() {
		return finished;
	}

	protected void assertNotFinished() {
		if (finished) {
			throw new SchemaException(
					"No changes allowed in a finished Schema.");
		}
	}

	@Override
	public int getAttributeIndex(String name) {
		if (finished) {
			Integer i = attributeIndex.get(name);
			if (i != null) {
				return i;
			}
		} else {
			int i = 0;
			for (Attribute a : getAttributeList()) {
				if (a.getName().equals(name)) {
					return i;
				}
				++i;
			}
		}
		throw new NoSuchAttributeException(getQualifiedName()
				+ " doesn't contain an attribute '" + name + "'");
	}

	protected void reopen() {
		attributeIndex = null;
		finished = false;
	}

	protected abstract void deleteAttribute(AttributeImpl attr);
}
