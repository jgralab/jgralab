package de.uni_koblenz.jgralab.schema.impl;

import java.util.List;
import java.util.Set;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TemporaryVertexClassImpl extends VertexClassImpl {

	protected TemporaryVertexClassImpl(GraphClassImpl gc,
			ClassLoader schemaClassLoader) {
		super("TemporaryVertexClass", (PackageImpl) gc.getSchema()
				.getDefaultPackage(), gc, schemaClassLoader);
	}

	@Override
	public void addSuperClass(VertexClass superClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PSet<VertexClass> getDirectSubClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<VertexClass> getDirectSuperClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<VertexClass> getAllSubClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public PSet<VertexClass> getAllSuperClasses() {
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
	public Attribute createAttribute(Attribute anAttribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Attribute createAttribute(String name, Domain domain,
			String defaultValueAsString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Attribute createAttribute(String name, Domain domain) {
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

	@SuppressWarnings("unchecked")
	@Override
	public Class<Vertex> getSchemaClass() {
		return (Class<Vertex>) (Class<?>) TemporaryVertex.class;
	}

	@Override
	public Class<Vertex> getSchemaImplementationClass() {
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
		return ArrayPVector.empty();
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public void setAbstract(boolean isAbstract) {
		throw new UnsupportedOperationException(
				"TemporaryVertexClass can not be abstract.");
	}

	@Override
	public int getAttributeIndex(String name) throws NoSuchAttributeException {
		throw new NoSuchAttributeException();
	}

	@Override
	public void setQualifiedName(String newQName) {
		throw new UnsupportedOperationException(
				"Name of TemporaryVertexClass can not be changed.");
	}

	@Override
	public void addComment(String comment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<IncidenceClass> getAllInIncidenceClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public Set<IncidenceClass> getAllOutIncidenceClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public Set<IncidenceClass> getValidFromFarIncidenceClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public Set<IncidenceClass> getValidToFarIncidenceClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public Set<IncidenceClass> getOwnAndInheritedFarIncidenceClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public DirectedSchemaEdgeClass getDirectedEdgeClassForFarEndRole(
			String roleName) {
		return null;
	}

	@Override
	public boolean isValidFromFor(EdgeClass ec) {
		return ec == this.graphClass.getTemporaryEdgeClass();
	}

	@Override
	public boolean isValidToFor(EdgeClass ec) {
		return ec == this.graphClass.getTemporaryEdgeClass();
	}

	@Override
	public Set<EdgeClass> getValidToEdgeClasses() {
		ArrayPSet<EdgeClass> set = ArrayPSet.empty();
		return set.plus(this.graphClass.getTemporaryEdgeClass());
	}

	@Override
	public Set<EdgeClass> getValidFromEdgeClasses() {
		ArrayPSet<EdgeClass> set = ArrayPSet.empty();
		return set.plus(this.graphClass.getTemporaryEdgeClass());
	}

	@Override
	public Set<EdgeClass> getConnectedEdgeClasses() {
		return ArrayPSet.empty();
	}

	@Override
	public Set<EdgeClass> getOwnConnectedEdgeClasses() {
		return ArrayPSet.empty();
	}

	@Override
	protected VertexClass getDefaultClass() {
		return this.graphClass.getDefaultVertexClass();
	}
}
