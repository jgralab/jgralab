/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.schema.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.exception.DuplicateRecordComponentException;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.M1ClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.NoSuchRecordComponentException;
import de.uni_koblenz.jgralab.schema.exception.RecordCycleException;
import de.uni_koblenz.jgralab.schema.exception.WrongSchemaException;

public final class RecordDomainImpl extends CompositeDomainImpl implements
		RecordDomain {

	/**
	 * The class object representing the generated interface for this
	 * AttributedElementClass
	 */
	private Class<? extends Object> m1Class;

	/**
	 * holds a list of the components of the record
	 */
	private final Map<String, RecordComponent> components = new TreeMap<String, RecordComponent>();

	/**
	 * @param qn
	 *            the unique name of the record in the schema
	 * @param components
	 *            a list of the components of the record
	 */
	RecordDomainImpl(String sn, Package pkg,
			Collection<RecordComponent> components) {
		super(sn, pkg);
		if (components != null) {
			for (RecordComponent c : components) {
				addComponent(c.getName(), c.getDomain());
			}
		}
	}

	@Override
	public void addComponent(String name, Domain domain) {
		if (name.isEmpty()) {
			throw new InvalidNameException(
					"Cannot create a record component with an empty name.");
		}
		if (components.containsKey(name)) {
			throw new DuplicateRecordComponentException(name,
					getQualifiedName());
		}
		if (parentPackage.getSchema().getDomain(domain.getQualifiedName()) != domain) {
			throw new WrongSchemaException(domain.getQualifiedName()
					+ " must be a domain of the schema "
					+ parentPackage.getSchema().getQualifiedName());
		}
		if (!staysAcyclicAfterAdding(domain)) {
			throw new RecordCycleException(
					"The creation of a component, which has the type " + domain
							+ ", would create a cycle of RecordDomains.");
		}
		RecordComponent c = new RecordComponent(name, domain);
		components.put(name, c);
	}

	@Override
	public Set<Domain> getAllComponentDomains() {
		Set<Domain> domains = new HashSet<Domain>();
		for (RecordComponent c : components.values()) {
			domains.add(c.getDomain());
		}
		return domains;
	}

	@Override
	public Collection<RecordComponent> getComponents() {
		return components.values();
	}

	@Override
	public Domain getDomainOfComponent(String name) {
		if (!components.containsKey(name)) {
			throw new NoSuchRecordComponentException(getQualifiedName(), name);
		}
		return components.get(name).getDomain();
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return schemaRootPackagePrefix + "." + getQualifiedName();
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
		// return getJavaAttributeTypeName(schemaRootPackagePrefix);
	}

	public Class<? extends Object> getM1Class() {
		if (m1Class == null) {
			String m1ClassName = getSchema().getPackagePrefix() + "."
					+ getQualifiedName();
			try {
				m1Class = Class.forName(m1ClassName, true, M1ClassManager
						.instance(getSchema().getQualifiedName()));
			} catch (ClassNotFoundException e) {
				throw new M1ClassAccessException(
						"Can't load M1 class for AttributedElementClass '"
								+ getQualifiedName() + "'", e);
			}
		}
		return m1Class;
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("name", variableName);
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName);

		return code;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return getQualifiedName(pkg);
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("name", variableName);
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;
	}

	/**
	 * @param d
	 *            the component domain which should be checked
	 * @return <code>true</code> if the addition of <code>d</code> wouldn't
	 *         create an inclusion cycle, <code>false</code> otherwise
	 */
	private boolean staysAcyclicAfterAdding(Domain d) {
		if (d == this) {
			return false;
		}
		if (!(d instanceof CompositeDomain)) {
			return true;
		}
		CompositeDomain c = (CompositeDomain) d;
		for (CompositeDomain comp : c.getAllComponentCompositeDomains()) {
			if (!staysAcyclicAfterAdding(comp)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("Record " + getQualifiedName());
		String delim = " (";
		for (RecordComponent component : components.values()) {
			output.append(delim);
			output.append(component.getName());
			output.append('=');
			output.append(component.getDomain());
			delim = ", ";
		}
		output.append(")");
		return output.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RecordDomain) {
			RecordDomain other = (RecordDomain) o;
			if (!getSchema().getQualifiedName().equals(
					other.getSchema().getQualifiedName())) {
				return false;
			}
			if (!qualifiedName.equals(other.getQualifiedName())) {
				return false;
			}
			return getComponents().equals(other.getComponents());
		}
		return false;
	}

	private void internalGetReadMethod(CodeSnippet code, String schemaPrefix,
			String variableName, String graphIoVariableName) {
		code.add("#init#");
		code.add("if (" + graphIoVariableName + ".isNextToken(\"(\")) {");
		/*
		 * code.add("\t" + "#name# = ((" + schemaPrefix + "." +
		 * parentPackage.getSchema().getGraphClass().getSimpleName() + ")" +
		 * "graph).create" + getSimpleName() + "(io);");
		 */
		code.add("\t" + "#name# = #theGraph#.createRecord("
				+ getSchema().getPackagePrefix() + "." + getQualifiedName()
				+ ".class, io);");
		code.add("} else if (" + graphIoVariableName
				+ ".isNextToken(GraphIO.NULL_LITERAL)) {");
		code.add("\t" + graphIoVariableName + ".match();");
		code.add("\t" + variableName + " = null;");
		code.add("} else {");
		code.add("\tthrow new GraphIOException(\"This is no record!\");");
		code.add("}");
	}

	private void internalGetWriteMethod(CodeSnippet code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.add("if (#name# != null) {");
		code.add("\t" + "#name#.writeComponentValues(" + graphIoVariableName
				+ ");");
		code.add("} else {");
		code.add("\t" + graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);");
		code.add("}");
	}

	@Override
	public CodeBlock getTransactionReadMethod(String schemaPrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("name", variableName);
		code.setVariable("init",
				getJavaAttributeImplementationTypeName(schemaPrefix)
						+ " #name# = null;");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeSnippet code = new CodeSnippet();
		code.setVariable("name", "get" + CodeGenerator.camelCase(variableName)
				+ "()");
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix
				+ ".impl.trans")
				+ "Impl";
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getVersionedClass(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.VersionedJGraLabCloneableImpl<"
				+ getTransactionJavaAttributeImplementationTypeName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getInitialValue() {
		return "null";
	}

	@Override
	public String getStandardJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return schemaRootPackagePrefix + ".impl.std." + getQualifiedName()
				+ "Impl";
	}

	@Override
	public String getSavememJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return schemaRootPackagePrefix + ".impl.savemem." + getQualifiedName()
				+ "Impl";
	}
}
