package de.uni_koblenz.jgralab.schema.impl;

import java.util.List;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TemporaryVertexClassImpl extends
		GraphElementClassImpl<VertexClass, Vertex> implements VertexClass {

	protected TemporaryVertexClassImpl(
			GraphClassImpl gc) {
		super("TemporaryVertexClass", (PackageImpl) gc.getSchema().getDefaultPackage(), 
				gc, gc.vertexClassDag);
	}

	@Override
	public void addSuperClass(VertexClass superClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSet<VertexClass> getDirectSubClasses() {
		return null;
	}

	@Override
	public PSet<VertexClass> getDirectSuperClasses() {
		return null;
	}

	@Override
	public PSet<VertexClass> getAllSubClasses() {
		return null;
	}

	@Override
	public PSet<VertexClass> getAllSuperClasses() {
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
	public Class<Vertex> getSchemaClass() {
		return null;
	}

	@Override
	public Class<Vertex> getSchemaImplementationClass() {
		return null;
	}

	@Override
	public String getVariableName() {
		return null;
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
	public Set<IncidenceClass> getAllInIncidenceClasses() {
		return null;
	}

	@Override
	public Set<IncidenceClass> getAllOutIncidenceClasses() {
		return null;
	}

	@Override
	public Set<IncidenceClass> getValidFromFarIncidenceClasses() {
		return null;
	}

	@Override
	public Set<IncidenceClass> getValidToFarIncidenceClasses() {
		return null;
	}

	@Override
	public Set<IncidenceClass> getOwnAndInheritedFarIncidenceClasses() {
		return null;
	}

	@Override
	public DirectedSchemaEdgeClass getDirectedEdgeClassForFarEndRole(
			String roleName) {
		return null;
	}

	@Override
	public boolean isValidFromFor(EdgeClass ec) {
		return true;
	}

	@Override
	public boolean isValidToFor(EdgeClass ec) {
		return true;
	}

	@Override
	public Set<EdgeClass> getValidToEdgeClasses() {
		return null;
	}

	@Override
	public Set<EdgeClass> getValidFromEdgeClasses() {
		return null;
	}

	@Override
	public Set<EdgeClass> getConnectedEdgeClasses() {
		return null;
	}

	@Override
	public Set<EdgeClass> getOwnConnectedEdgeClasses() {
		return null;
	}

	@Override
	protected VertexClass getDefaultClass() {
		return this.graphClass.getDefaultVertexClass();
	}
}
