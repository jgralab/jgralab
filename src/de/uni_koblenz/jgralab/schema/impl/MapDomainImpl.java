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
/**
 *
 */
package de.uni_koblenz.jgralab.schema.impl;

import java.io.IOException;
import java.util.Iterator;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeList;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MapDomainImpl extends CompositeDomainImpl implements
MapDomain {
	/**
	 * The domain of this MapDomain's keys.
	 */
	private final Domain keyDomain;

	/**
	 * The domain of this MapDomain's values.
	 */
	private final Domain valueDomain;

	protected MapDomainImpl(Schema schema, Domain aKeyDomain,
			Domain aValueDomain) {
		super(MAPDOMAIN_NAME + "<"
				+ aKeyDomain.getTGTypeName(schema.getDefaultPackage()) + ", "
				+ aValueDomain.getTGTypeName(schema.getDefaultPackage()) + ">",
				(PackageImpl) schema.getDefaultPackage());

		if (this.parentPackage.getSchema().getDomain(aKeyDomain.getQualifiedName()) == null) {
			throw new SchemaException("Key domain '"
					+ aKeyDomain.getQualifiedName()
					+ "' not existent in schema "
					+ this.parentPackage.getSchema().getQualifiedName());
		}
		if (this.parentPackage.getSchema()
				.getDomain(aValueDomain.getQualifiedName()) == null) {
			throw new SchemaException("Value domain '"
					+ aValueDomain.getQualifiedName()
					+ "' not existent in schema "
					+ this.parentPackage.getSchema().getQualifiedName());
		}
		keyDomain = aKeyDomain;
		valueDomain = aValueDomain;
		((SchemaImpl) schema).addDomainDependency(this, keyDomain);
		((SchemaImpl) schema).addDomainDependency(this, valueDomain);
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return MAPDOMAIN_TYPE + "<"
				+ this.keyDomain.getJavaClassName(schemaRootPackagePrefix) + ", "
				+ this.valueDomain.getJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return this.getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public Domain getKeyDomain() {
		return this.keyDomain;
	}

	@Override
	public CodeBlock getReadMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		this.internalGetReadMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return MAPDOMAIN_NAME + "<" + this.keyDomain.getTGTypeName(pkg) + ", "
				+ this.valueDomain.getTGTypeName(pkg) + ">";
	}

	@Override
	public Domain getValueDomain() {
		return this.valueDomain;
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", variableName);
		this.internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);

		return code;

	}

	@Override
	public String toString() {
		return "domain " + MAPDOMAIN_NAME + "<" + this.keyDomain.toString() + ", "
				+ this.valueDomain.toString() + ">";
	}

	private void internalGetReadMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("name", variableName);
		code.setVariable("empty", MapDomain.EMPTY_MAP);
		code.setVariable("keydom",
				this.getKeyDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"keytype",
				this.getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("valuedom",
				this.getValueDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"valuetype",
				this.getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		code.addNoIndent(new CodeSnippet("if (#io#.isNextToken(\"{\")) {"));
		code.add(new CodeSnippet(MAPDOMAIN_TYPE
				+ "<#keydom#, #valuedom#> $#name# = #empty#;"));
		code.add(new CodeSnippet("#io#.match(\"{\");",
				"while (!#io#.isNextToken(\"}\")) {"));

		if (this.getKeyDomain().isComposite()) {
			code.add(new CodeSnippet("\t#keytype# #name#Key = null;"));
		} else {
			code.add(new CodeSnippet("\t#keytype# #name#Key;"));
		}
		if (this.getValueDomain().isComposite()) {
			code.add(new CodeSnippet("\t\t#valuetype# #name#Value = null;"));
		} else {
			code.add(new CodeSnippet("\t\t#valuetype# #name#Value;"));
		}

		code.add(
				this.getKeyDomain().getReadMethod(schemaRootPackagePrefix,
						variableName + "Key", graphIoVariableName), 1);
		code.add(new CodeSnippet("\t#io#.match(\"-\");"));
		code.add(
				this.getValueDomain().getReadMethod(schemaRootPackagePrefix,
						variableName + "Value", graphIoVariableName), 1);
		code.add(new CodeSnippet(
				"\t$#name# = $#name#.plus(#name#Key, #name#Value);", "}",
				"#io#.match(\"}\");", "#name# = $#name#;"));
		code.addNoIndent(new CodeSnippet(
				"} else if (#io#.isNextToken(GraphIO.NULL_LITERAL)) {"));
		code.add(new CodeSnippet("#io#.match();", "#name# = null;"));
		code.addNoIndent(new CodeSnippet("} else {", "\t#name# = null;", "}"));
	}

	private void internalGetWriteMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("nameKey", "key");
		code.setVariable("nameValue", "value");

		code.setVariable(
				"keytype",
				this.getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable(
				"valuetype",
				this.getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.writeSpace();", "#io#.write(\"{\");",
				"#io#.noSpace();"));
		code.add(new CodeSnippet("for (#keytype# #nameKey#: #name#.keySet()) {"));

		code.add(new CodeSnippet(
				"#valuetype# #nameValue# = #name#.get(#nameKey#);"), 1);
		code.add(
				this.getKeyDomain().getWriteMethod(schemaRootPackagePrefix,
						code.getVariable("nameKey"), graphIoVariableName), 1);

		code.add(new CodeSnippet("\t#io#.write(\" -\");"));

		code.add(
				this.getValueDomain().getWriteMethod(schemaRootPackagePrefix,
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
		code.setVariable("init", MAPDOMAIN_TYPE
				+ "<#keydom#, #valuedom#> #name# = null;");
		this.internalGetReadMethod(code, schemaPrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		CodeList code = new CodeList();
		code.setVariable("name", "get" + CodeGenerator.camelCase(variableName)
				+ "()");
		this.internalGetWriteMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName);
		return code;
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return this.getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return this.getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getVersionedClass(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.VersionedReferenceImpl<"
				+ this.getTransactionJavaAttributeImplementationTypeName(schemaRootPackagePrefix)
				+ ">";
	}

	@Override
	public String getInitialValue() {
		return "null";
	}

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken("{")) {
			PMap<Object, Object> result = JGraLab.map();
			io.match("{");
			while (!io.isNextToken("}")) {
				Object mapKey = null;
				Object mapValue = null;
				mapKey = this.getKeyDomain().parseGenericAttribute(io);
				io.match("-");
				mapValue = this.getValueDomain().parseGenericAttribute(io);
				result = result.plus(mapKey, mapValue);
			}
			io.match("}");
			return result;
		} else if (io.isNextToken(GraphIO.NULL_LITERAL)) {
			io.match();
			return null;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serializeGenericAttribute(GraphIO io, Object data)
			throws IOException {
		if (data != null) {
			io.writeSpace();
			io.write("{");
			io.noSpace();
			for (Object key : ((PMap<Object, Object>) data).keySet()) {
				this.getKeyDomain().serializeGenericAttribute(io, key);
				io.write(" -");
				this.getValueDomain().serializeGenericAttribute(io,
						((PMap<Object, Object>) data).get(key));
			}
			io.write("}");
			io.space();
		} else {
			io.writeIdentifier(GraphIO.NULL_LITERAL);
		}
	}

	@Override
	public boolean isConformGenericValue(Object value) {
		boolean result = true;
		if (value == null) {
			return result;
		}
		result &= value instanceof PMap;
		if (!result) {
			return false;
		}
		Iterator<?> iterator = ((PMap<?, ?>) value).keySet().iterator();
		while (iterator.hasNext() && result) {
			Object key = iterator.next();
			result &= getKeyDomain().isConformGenericValue(key)
					&& getValueDomain().isConformGenericValue(
							((PMap<?, ?>) value).get(key));
		}
		return result;
	}
}
