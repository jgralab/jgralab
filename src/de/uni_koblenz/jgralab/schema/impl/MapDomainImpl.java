/**
 *
 */
package de.uni_koblenz.jgralab.schema.impl;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public final class MapDomainImpl extends CompositeDomainImpl implements
		MapDomain {
	/**
	 * The domain of this MapDomain's keys.
	 */
	private final Domain keyDomain;

	/**
	 * The domain of this MapDomain's values.
	 */
	private final Domain valueDomain;

	MapDomainImpl(Schema schema, Domain aKeyDomain, Domain aValueDomain) {
		super(MAPDOMAIN_NAME + "<"
				+ aKeyDomain.getTGTypeName(schema.getDefaultPackage()) + ", "
				+ aValueDomain.getTGTypeName(schema.getDefaultPackage()) + ">",
				schema.getDefaultPackage());

		assert parentPackage.getSchema().getDomain(
				aKeyDomain.getQualifiedName()) != null : aKeyDomain
				.getQualifiedName()
				+ " must be a domain of the schema "
				+ parentPackage.getSchema().getQualifiedName();

		assert parentPackage.getSchema().getDomain(
				aValueDomain.getQualifiedName()) != null : aValueDomain
				.getQualifiedName()
				+ " must be a domain of the schema "
				+ parentPackage.getSchema().getQualifiedName();

		keyDomain = aKeyDomain;
		valueDomain = aValueDomain;
	}

	@Override
	public Set<Domain> getAllComponentDomains() {
		HashSet<Domain> allComponentDomains = new HashSet<Domain>(2);
		allComponentDomains.add(keyDomain);
		allComponentDomains.add(valueDomain);
		return allComponentDomains;
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "java.util." + MAPDOMAIN_NAME + "<"
				+ keyDomain.getJavaClassName(schemaRootPackagePrefix) + ", "
				+ valueDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public Domain getKeyDomain() {
		return keyDomain;
	}

	@Override
	public CodeBlock getReadMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName, false);

		return code;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return MAPDOMAIN_NAME + "<" + keyDomain.getTGTypeName(pkg) + ", "
				+ valueDomain.getTGTypeName(pkg) + ">";
	}

	@Override
	public Domain getValueDomain() {
		return valueDomain;
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", variableName);
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;

	}

	@Override
	public String toString() {
		return "domain " + MAPDOMAIN_NAME + "<" + keyDomain.toString() + ", "
				+ valueDomain.toString() + ">";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MapDomain) {
			MapDomain other = (MapDomain) o;
			if (!getSchema().getQualifiedName().equals(
					other.getSchema().getQualifiedName())) {
				return false;
			}
			if ((keyDomain == null) || (valueDomain == null)) {
				return false;
			}
			return keyDomain.equals(other.getKeyDomain())
					&& valueDomain.equals(other.getValueDomain());
		}
		return false;
	}

	private void internalGetReadMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName, boolean transactionSupport) {
		code.setVariable("name", variableName);

		code.setVariable("keydom", getKeyDomain().getJavaClassName(
				schemaRootPackagePrefix));
		code.setVariable("keytype",
				getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("valuedom", getValueDomain().getJavaClassName(
				schemaRootPackagePrefix));
		code.setVariable("valuetype",
				getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		code.addNoIndent(new CodeSnippet("if (#io#.isNextToken(\"{\")) {"));
		if (transactionSupport)
			code
					.add(new CodeSnippet(
							"#name# = graph.createMap(#keydom#.class, #valuedom#.class);"));
		else
			code.add(new CodeSnippet(
					"#name# = new java.util.HashMap<#keydom#, #valuedom#>();"));
		code.add(new CodeSnippet("#io#.match(\"{\");",
				"while (!#io#.isNextToken(\"}\")) {", "\t#keytype# #name#Key;",
				"\t#valuetype# #name#Value;"));
		code.add(getKeyDomain().getReadMethod(schemaRootPackagePrefix,
				variableName + "Key", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#io#.match(\"-\");"));
		code.add(getValueDomain().getReadMethod(schemaRootPackagePrefix,
				variableName + "Value", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#name#.put(#name#Key, #name#Value);", "}",
				"#io#.match(\"}\");"));
		code
				.addNoIndent(new CodeSnippet(
						"} else if (#io#.isNextToken(GraphIO.NULL_LITERAL) || #io#.isNextToken(GraphIO.OLD_NULL_LITERAL)) {"));
		code.add(new CodeSnippet("#io#.match();", "#name# = null;"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	private void internalGetWriteMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("nameKey", "key");
		code.setVariable("nameValue", "value");

		code.setVariable("keytype",
				getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("valuetype",
				getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.writeSpace();", "#io#.write(\"{\");",
				"#io#.noSpace();"));
		code
				.add(new CodeSnippet(
						"for (#keytype# #nameKey#: #name#.keySet()) {"));

		code.add(new CodeSnippet(
				"#valuetype# #nameValue# = #name#.get(#nameKey#);"), 1);
		code.add(getKeyDomain().getWriteMethod(schemaRootPackagePrefix,
				code.getVariable("nameKey"), graphIoVariableName), 1);

		code.add(new CodeSnippet("\t#io#.write(\" -\");"));

		code.add(getValueDomain().getWriteMethod(schemaRootPackagePrefix,
				code.getVariable("nameValue"), graphIoVariableName), 1);

		code.add(new CodeSnippet("}", "#io#.write(\"}\");", "#io#.space();"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	@Override
	public CodeBlock getTransactionReadMethod(String schemaPrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", "get" + CodeGenerator.camelCase(variableName)
				+ "()");
		code.setVariable("init",
				"java.util.Map<#keydom#, #valuedom#> #name# = null;");
		internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName, true);
		return code;
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", "get" + CodeGenerator.camelCase(variableName)
				+ "()");
		internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.JGraLabMap<"
				+ keyDomain
						.getTransactionJavaClassName(schemaRootPackagePrefix)
				+ ", "
				+ valueDomain
						.getTransactionJavaClassName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.JGraLabMap";
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
}
