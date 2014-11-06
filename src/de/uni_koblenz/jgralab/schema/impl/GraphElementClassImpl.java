/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.util.BitSet;
import java.util.List;
import java.util.TreeSet;

import org.pcollections.ArrayPVector;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public abstract class GraphElementClassImpl<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends AttributedElementClassImpl<SC, IC> implements
		GraphElementClass<SC, IC> {

	/**
	 * The {@link GraphClass} of this {@link GraphElementClass}.
	 */
	protected final GraphClassImpl graphClass;

	/**
	 * The list of attributes. Only the own attributes of this class are stored
	 * here, no inherited attributes.
	 */
	protected PVector<Attribute> ownAttributes;

	/**
	 * The subclasses of this class - only set if the schema is finished. The
	 * HashSet is used to speed up isInstance test.
	 */
	protected PSet<SC> allSubClasses;
	protected BitSet allSubClassesBitSet;

	/**
	 * The superclasses of this class - only set if the schema is finished. The
	 * HashSet is used to speed up isInstance test.
	 */
	protected PSet<SC> allSuperClasses;
	protected BitSet allSuperClassesBitSet;

	/**
	 * A {@link DirectedAcyclicGraph} representing the generalization hierarchy.
	 * Edges direction is from superclass to subclass.
	 */
	protected final DirectedAcyclicGraph<GraphElementClass<SC, IC>> subclassDag;

	/**
	 * The class id of this class in the schema
	 */
	protected final int classId;

	/**
	 * delegates its constructor to the generalized class
	 * 
	 * @param qn
	 *            the unique identifier of the element in the schema
	 */
	@SuppressWarnings("unchecked")
	protected GraphElementClassImpl(String simpleName, PackageImpl pkg,
			GraphClassImpl graphClass, DirectedAcyclicGraph<SC> dag,
			ClassLoader schemaClassLoader) {
		super(simpleName, pkg, graphClass.schema, schemaClassLoader);
		ownAttributes = ArrayPVector.empty();
		subclassDag = (DirectedAcyclicGraph<GraphElementClass<SC, IC>>) dag;
		subclassDag.createNode(this);
		this.graphClass = graphClass;
		this.classId = schema.getNextGraphElementClassId();
	}

	@Override
	protected Attribute createAttribute(Attribute anAttribute) {
		assertNotFinished();
		// Check if a subclass already contains an attribute with that name. In
		// that case, it may not be added, too.
		if (subclassContainsAttribute(anAttribute.getName())) {
			throw new SchemaException(
					"Duplicate attribute '"
							+ anAttribute.getName()
							+ "' in AttributedElementClass '"
							+ getQualifiedName()
							+ "'. A derived AttributedElementClass already contains this Attribute.");
		}
		super.createAttribute(anAttribute);
		TreeSet<Attribute> s = new TreeSet<Attribute>(ownAttributes);
		s.add(anAttribute);
		ownAttributes = ArrayPVector.<Attribute> empty().plusAll(s);
		return anAttribute;
	}

	@Override
	public GraphClass getGraphClass() {
		return graphClass;
	}

	/**
	 * adds a superClass to this class
	 * 
	 * @param superClass
	 *            the class to add as superclass
	 */
	protected void addSuperClass(SC superClass) {
		assertNotFinished();
		if (superClass == this) {
			return;
		}
		for (Attribute a : superClass.getAttributeList()) {
			if (getAttribute(a.getName()) != null
					&& getAttribute(a.getName()) != a) {
				throw new SchemaException("Cannot add "
						+ superClass.getQualifiedName() + " as superclass of "
						+ getQualifiedName() + ". Cause: Attribute "
						+ a.getName() + " is declared in both classes");
			}
		}
		subclassDag.createEdge(superClass, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<SC> getDirectSubClasses() {
		return (PSet<SC>) subclassDag.getDirectSuccessors(this);
	}

	/**
	 * @return either the default vertex class or the default edge class
	 */
	protected abstract SC getDefaultClass();

	@SuppressWarnings("unchecked")
	@Override
	public PSet<SC> getDirectSuperClasses() {
		return ((PSet<SC>) subclassDag.getDirectPredecessors(this))
				.minus(getDefaultClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<SC> getAllSubClasses() {
		if (finished) {
			return allSubClasses;
		}
		return (PSet<SC>) subclassDag.getAllSuccessorsInTopologicalOrder(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PSet<SC> getAllSuperClasses() {
		if (finished) {
			return allSuperClasses;
		}
		return (PSet<SC>) subclassDag
				.getAllPredecessorsInTopologicalOrder(this).minus(
						getDefaultClass());
	}

	@Override
	public final boolean isSubClassOf(SC anAttributedElementClass) {
		if (finished) {
			return allSuperClassesBitSet
					.get(((GraphElementClass<?, ?>) anAttributedElementClass)
							.getGraphElementClassIdInSchema());
		}
		return getAllSuperClasses().contains(anAttributedElementClass);
	}

	@Override
	public final boolean isSuperClassOf(SC anAttributedElementClass) {
		if (finished) {
			return allSubClassesBitSet
					.get(((GraphElementClass<?, ?>) anAttributedElementClass)
							.getGraphElementClassIdInSchema());
		}
		return getAllSubClasses().contains(anAttributedElementClass);
	}

	private boolean subclassContainsAttribute(String name) {
		for (SC subClass : getAllSubClasses()) {
			if (subClass.getAttribute(name) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void finish() {
		allSuperClasses = getAllSuperClasses();
		allSuperClassesBitSet = new BitSet();
		for (GraphElementClass<?, ?> superClass : allSuperClasses) {
			allSuperClassesBitSet.set(
					superClass.getGraphElementClassIdInSchema(), true);
		}
		allSubClasses = getAllSubClasses();
		allSubClassesBitSet = new BitSet();
		for (GraphElementClass<?, ?> subClass : allSubClasses) {
			allSubClassesBitSet.set(subClass.getGraphElementClassIdInSchema(),
					true);
		}

		// System.err.println("Finish " + getClass().getSimpleName() + " "
		// + getQualifiedName());
		TreeSet<Attribute> s = new TreeSet<Attribute>(ownAttributes);
		for (AttributedElementClass<SC, IC> superClass : subclassDag
				.getDirectPredecessors(this)) {
			// System.err.println("\t" + superClass.getQualifiedName() + " "
			// + superClass.getAttributeList());
			s.addAll(superClass.getAttributeList());
		}

		allAttributes = ArrayPVector.<Attribute> empty().plusAll(s);
		// System.err.println("\tall attributes: " + allAttributes);
		super.finish();
	}

	@Override
	public int getAttributeCount() {
		if (finished) {
			return allAttributes.size();
		}
		return getAttributeList().size();
	}

	@Override
	public List<Attribute> getAttributeList() {
		if (finished) {
			return allAttributes;
		}

		TreeSet<Attribute> attrList = new TreeSet<Attribute>();
		attrList.addAll(ownAttributes);
		for (AttributedElementClass<SC, IC> superClass : subclassDag
				.getDirectPredecessors(this)) {
			attrList.addAll(superClass.getAttributeList());
		}
		return ArrayPVector.<Attribute> empty().plusAll(attrList);
	}

	@Override
	public Attribute getAttribute(String name) {
		if (finished) {
			return super.getAttribute(name);
		}

		Attribute ownAttr = getOwnAttribute(name);
		if (ownAttr != null) {
			return ownAttr;
		}
		for (AttributedElementClass<SC, IC> superClass : subclassDag
				.getDirectPredecessors(this)) {
			Attribute inheritedAttr = superClass.getAttribute(name);
			if (inheritedAttr != null) {
				return inheritedAttr;
			}
		}
		return null;
	}

	@Override
	public Attribute getOwnAttribute(String name) {
		for (Attribute a : ownAttributes) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	@Override
	public int getOwnAttributeCount() {
		return ownAttributes.size();
	}

	@Override
	public List<Attribute> getOwnAttributeList() {
		return ownAttributes;
	}

	@Override
	public boolean hasOwnAttributes() {
		return !ownAttributes.isEmpty();
	}

	public String getDescriptionString() {
		StringBuilder output = new StringBuilder(this.getClass()
				.getSimpleName() + " '" + getQualifiedName() + "'");
		if (isAbstract()) {
			output.append(" (abstract)");
		}
		output.append(":\n");

		output.append("Subclasses of '" + getQualifiedName() + "': ");

		for (SC aec : getAllSubClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}

		output.append("\nSuperclasses of '" + getQualifiedName() + "': ");
		for (SC aec : getAllSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}

		output.append("\nDirect Superclasses of '" + getQualifiedName() + "': ");
		for (SC aec : getDirectSuperClasses()) {
			output.append("'" + aec.getQualifiedName() + "' ");
		}

		output.append(attributesToString());
		output.append("\n");

		return output.toString();
	}

	@Override
	public int getGraphElementClassIdInSchema() {
		return classId;
	}

	@Override
	public void setQualifiedName(String newQName) {
		if (qualifiedName.equals(newQName)) {
			return;
		}
		if (schema.knows(newQName)) {
			throw new SchemaException(newQName
					+ " is already known to the schema.");
		}
		String[] ps = SchemaImpl.splitQualifiedName(newQName);
		String newPackageName = ps[0];
		String newSimpleName = ps[1];
		if (!NamedElementImpl.ATTRELEM_OR_NOCOLLDOMAIN_PATTERN.matcher(
				newSimpleName).matches()) {
			throw new SchemaException("Invalid graph element class name '"
					+ newSimpleName + "'.");
		}

		unregister();

		qualifiedName = newQName;
		simpleName = newSimpleName;
		parentPackage = schema.createPackageWithParents(newPackageName);

		register();
	}

	@Override
	protected void reopen() {
		allSuperClasses = null;
		allSuperClassesBitSet = null;
		allSubClasses = null;
		allSubClassesBitSet = null;

		super.reopen();
	}

	@Override
	protected void deleteAttribute(AttributeImpl attr) {
		allAttributes = allAttributes.minus(attr);
		ownAttributes = ownAttributes.minus(attr);
	}

	@Override
	public void delete() {
		for (Attribute a : ownAttributes) {
			a.delete();
		}
		ownAttributes = null;
		allAttributes = null;
		schema.namedElements.remove(qualifiedName);
	}
}
