package de.uni_koblenz.jgralab.schema.impl;

import java.util.List;
import java.util.Set;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class TemporaryEdgeClassImpl extends EdgeClassImpl implements EdgeClass {
	
	protected TemporaryEdgeClassImpl(
			GraphClassImpl graphClass) {
		//super(simpleName, pkg, gc, from, fromMin, fromMax, fromRoleName, aggrFrom, to, toMin, toMax, toRoleName, aggrTo)
		super("TemporaryEdgeClass", 
				(PackageImpl)graphClass.getSchema().getDefaultPackage(), 
				graphClass, 
				graphClass.getDefaultVertexClass(), 0, Integer.MAX_VALUE,"",AggregationKind.NONE,
				graphClass.getDefaultVertexClass(),0, Integer.MAX_VALUE, "", AggregationKind.NONE);
	}

	@Override
	public PSet<EdgeClass> getDirectSubClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<EdgeClass> getDirectSuperClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<EdgeClass> getAllSubClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<EdgeClass> getAllSuperClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDefaultGraphElementClass() {
		return false;
	}

	@Override
	public void addAttribute(Attribute anAttribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttribute(String name, Domain domain,
			String defaultValueAsString) {
		throw new UnsupportedOperationException();	
	}

	@Override
	public void addAttribute(String name, Domain domain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addConstraint(Constraint constraint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAttribute(String name) {
		return false;
	}

	@Override
	public Attribute getAttribute(String name) {
		return null;
	}

	@Override
	public int getAttributeCount() {
		return 0;
	}

	@Override
	public List<Attribute> getAttributeList() {
		return null;
	}

	@Override
	public Class<Edge> getSchemaClass() {
		return null;
	}

	@Override
	public Class<Edge> getSchemaImplementationClass() {
		return null;
	}

	@Override
	public String getVariableName() {
		return null; // no code generation for TemporaryEdgeClass
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

	@Override
	public boolean hasOwnAttributes() {
		return false;
	}

	@Override
	public Attribute getOwnAttribute(String name) {
		return null;
	}

	@Override
	public int getOwnAttributeCount() {
		return 0;
	}

	@Override
	public List<Attribute> getOwnAttributeList() {
		return ArrayPVector.empty();
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		throw new UnsupportedOperationException();	
	}

	@Override
	public int getAttributeIndex(String name) throws NoSuchAttributeException {
		throw new NoSuchAttributeException();
	}

	@Override
	public void setQualifiedName(String newQName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addComment(String comment) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addSuperClass(EdgeClass superClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EdgeClass getDefaultClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
