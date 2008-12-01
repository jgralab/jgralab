/**
 *
 */
package de.uni_koblenz.jgralab.schema.impl;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MapDomainImpl extends CompositeDomainImpl implements MapDomain {
	/**
	 * The domain of this MapDomain's keys.
	 */
	protected Domain keyDomain;

	/**
	 * The domain of this MapDomain's values.
	 */
	protected Domain valueDomain;

	public MapDomainImpl(Schema schema, QualifiedName qn, Domain aKeyDomain,
			Domain aValueDomain) {
		super(schema, qn);
		if (!isDomainOfSchema(schema, aKeyDomain)
				|| !isDomainOfSchema(schema, aValueDomain)) {
			throw new SchemaException(aKeyDomain + " and " + aValueDomain
					+ " must be domains of the schema "
					+ schema.getQualifiedName());
		}
		keyDomain = aKeyDomain;
		valueDomain = aValueDomain;
	}

	public MapDomainImpl(Schema schema, Domain aKeyDomain, Domain aValueDomain) {
		this(schema,
				new QualifiedName("Map<"
						+ aKeyDomain.getTGTypeName(schema.getDefaultPackage())
						+ ", "
						+ aValueDomain
								.getTGTypeName(schema.getDefaultPackage())
						+ ">"), aKeyDomain, aValueDomain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.impl.CompositeDomainImpl#equals(java.lang
	 * .Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof MapDomain)) {
			return false;
		}

		MapDomain other = (MapDomain) o;
		return keyDomain.equals(other.getKeyDomain())
				&& valueDomain.equals(other.getValueDomain());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.schema.MapDomain#getKeyDomain()
	 */
	@Override
	public Domain getKeyDomain() {
		return keyDomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.schema.MapDomain#getValueDomain()
	 */
	@Override
	public Domain getValueDomain() {
		return valueDomain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.CompositeDomain#getAllComponentDomains()
	 */
	@Override
	public Set<Domain> getAllComponentDomains() {
		HashSet<Domain> allComponentDomains = new HashSet<Domain>(2);
		allComponentDomains.add(keyDomain);
		allComponentDomains.add(valueDomain);
		return allComponentDomains;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.Domain#getJavaAttributeImplementationTypeName
	 * (java.lang.String)
	 */
	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "java.util.Map<"
				+ keyDomain.getJavaClassName(schemaRootPackagePrefix) + ", "
				+ valueDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.Domain#getJavaClassName(java.lang.String)
	 */
	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.schema.Domain#getReadMethod(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public CodeBlock getReadMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {

		CodeList code = new CodeList();
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

		code.addNoIndent(new CodeSnippet(
				"if (!#io#.isNextToken(\"\\\\null\")) {"));
		code.add(new CodeSnippet(
				"#name# = new java.util.HashMap<#keydom#, #valuedom#>();",
				"#io#.match(\"{\");", "while (!#io#.isNextToken(\"}\")) {",
				"\t#keytype# #name#Key;", "\t#valuetype# #name#Value;"));
		code.add(getKeyDomain().getReadMethod(schemaRootPackagePrefix,
				variableName + "Key", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#io#.match(\"-\");"));
		code.add(getValueDomain().getReadMethod(schemaRootPackagePrefix,
				variableName + "Value", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#name#.put(#name#Key, #name#Value);", "}",
				"#io#.match(\"}\");"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet("io.match(\"\\\\null\");", "#name# = null;"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.Domain#getTGTypeName(de.uni_koblenz.jgralab
	 * .schema.Package)
	 */
	@Override
	public String getTGTypeName(Package pkg) {
		return "Map<" + keyDomain.getTGTypeName(pkg) + ", "
				+ valueDomain.getTGTypeName(pkg) + ">";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.schema.Domain#getWriteMethod(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", variableName);
		code.setVariable("nameKey", variableName + "Key");
		code.setVariable("nameValue", variableName + "Value");

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

		code.add(new CodeSnippet("}", "#io#.write(\"}\");"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(\"\\\\null\");"));
		code.addNoIndent(new CodeSnippet("}"));
		return code;

	}

	@Override
	public String toString() {
		return "domain Map<" + keyDomain.toString() + ", "
				+ valueDomain.toString() + ">";
	}

}
