package de.uni_koblenz.jgralab.schema.impl;

import java.util.List;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;

public class TemporaryEdgeClassImpl extends GraphElementClassImpl<EdgeClass, Edge> implements EdgeClass {

	private IncidenceClass from, to;

	
	protected TemporaryEdgeClassImpl(
			GraphClassImpl graphClass) {
		super("TemporaryEdgeClass", (PackageImpl)graphClass.getSchema().getDefaultPackage(), 
				graphClass, graphClass.edgeClassDag);
		
		IncidenceClass fromInc = new IncidenceClassImpl(this, graphClass.getDefaultVertexClass(),
				"", 0, Integer.MAX_VALUE, IncidenceDirection.OUT,
				AggregationKind.NONE);
		IncidenceClass toInc = new IncidenceClassImpl(this, graphClass.getDefaultVertexClass(), "",
				0, Integer.MAX_VALUE, IncidenceDirection.IN, AggregationKind.NONE);
		this.from = fromInc;
		this.to = toInc;
	}

	@Override
	public PSet<EdgeClass> getDirectSubClasses() {
		return null;
	}

	@Override
	public PSet<EdgeClass> getDirectSuperClasses() {
		return null;
	}

	@Override
	public PSet<EdgeClass> getAllSubClasses() {
		return null;
	}

	@Override
	public PSet<EdgeClass> getAllSuperClasses() {
		return null;
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
	public Set<Constraint> getConstraints() {
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
		return null;
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
	public List<String> getComments() {
		return null;
	}

	@Override
	public void addSuperClass(EdgeClass superClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IncidenceClass getFrom() {
		return this.from;
	}

	@Override
	public IncidenceClass getTo() {
		return this.to;
	}

	@Override
	protected EdgeClass getDefaultClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
