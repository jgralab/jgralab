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
/**
 *
 */
package de.uni_koblenz.jgralab.schema.impl;

import java.io.IOException;
import java.util.Iterator;

import org.pcollections.PMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeList;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

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
				(PackageImpl) schema.getDefaultPackage());

		if (parentPackage.getSchema().getDomain(aKeyDomain.getQualifiedName()) == null) {
			throw new SchemaException("Key domain '"
					+ aKeyDomain.getQualifiedName()
					+ "' not existent in schema "
					+ parentPackage.getSchema().getQualifiedName());
		}
		if (parentPackage.getSchema()
				.getDomain(aValueDomain.getQualifiedName()) == null) {
			throw new SchemaException("Value domain '"
					+ aValueDomain.getQualifiedName()
					+ "' not existent in schema "
					+ parentPackage.getSchema().getQualifiedName());
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
			String variableName, String graphIoVariableName,
			boolean withUnsetCheck) {
		CodeList code = new CodeList();
		code.setVariable("init", "");
		internalGetReadMethod(code, schemaRootPackagePrefix, variableName,
				graphIoVariableName, withUnsetCheck);

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

	private void internalGetReadMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName, boolean withUnsetCheck) {
		code.setVariable("name", variableName);
		code.setVariable("empty", MapDomain.EMPTY_MAP);
		code.setVariable("keydom",
				getKeyDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"keytype",
				getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("valuedom",
				getValueDomain().getJavaClassName(schemaRootPackagePrefix));
		code.setVariable(
				"valuetype",
				getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("#init#"));
		if (withUnsetCheck) {
			code.addNoIndent(new CodeSnippet("boolean attrIsSet = true;"));
		}
		code.addNoIndent(new CodeSnippet(
				"if (#io#.isNextToken(#token#.LCRL)) {"));
		code.add(new CodeSnippet(MAPDOMAIN_TYPE
				+ "<#keydom#, #valuedom#> $#name# = #empty#;"));
		code.add(new CodeSnippet("#io#.match();",
				"while (!#io#.isNextToken(#token#.RCRL)) {"));

		if (getKeyDomain().isComposite()) {
			code.add(new CodeSnippet("\t#keytype# #name#Key = null;"));
		} else {
			code.add(new CodeSnippet("\t#keytype# #name#Key;"));
		}
		if (getValueDomain().isComposite()) {
			code.add(new CodeSnippet("\t\t#valuetype# #name#Value = null;"));
		} else {
			code.add(new CodeSnippet("\t\t#valuetype# #name#Value;"));
		}

		code.add(
				getKeyDomain().getReadMethod(schemaRootPackagePrefix,
						variableName + "Key", graphIoVariableName, false), 1);
		code.add(new CodeSnippet("\t#io#.match(#token#.HYPHEN);"));
		code.add(
				getValueDomain().getReadMethod(schemaRootPackagePrefix,
						variableName + "Value", graphIoVariableName, false), 1);
		code.add(new CodeSnippet(
				"\t$#name# = $#name#.plus(#name#Key, #name#Value);", "}",
				"#io#.match();", "#name# = $#name#;"));
		code.addNoIndent(new CodeSnippet(
				"} else if (#io#.isNextToken(#token#.NULL_LITERAL)) {"));
		code.add(new CodeSnippet("#io#.match();", "#name# = null;"));
		if (withUnsetCheck) {
			code.addNoIndent(new CodeSnippet(
					"} else if (#io#.isNextToken(#token#.UNSET)) {",
					"\t#io#.match();", "\tattrIsSet = false;"));
		}
		code.addNoIndent(new CodeSnippet("} else {",
				"\tthrow new GraphIOException(\"Unknown Map value\");", "}"));
	}

	private void internalGetWriteMethod(CodeList code,
			String schemaRootPackagePrefix, String variableName,
			String graphIoVariableName) {
		code.setVariable("nameKey", "key");
		code.setVariable("nameValue", "value");

		code.setVariable(
				"keytype",
				getKeyDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable(
				"valuetype",
				getValueDomain().getJavaAttributeImplementationTypeName(
						schemaRootPackagePrefix));

		code.setVariable("io", graphIoVariableName);

		code.addNoIndent(new CodeSnippet("if (#name# != null) {"));
		code.add(new CodeSnippet("#io#.write(\"{\");"));
		code.add(new CodeSnippet("for (#keytype# #nameKey#: #name#.keySet()) {"));

		code.add(new CodeSnippet(
				"#valuetype# #nameValue# = #name#.get(#nameKey#);"), 1);
		code.add(
				getKeyDomain().getWriteMethod(schemaRootPackagePrefix,
						code.getVariable("nameKey"), graphIoVariableName), 1);

		code.add(new CodeSnippet("\t#io#.write(\" -\");"));

		code.add(
				getValueDomain().getWriteMethod(schemaRootPackagePrefix,
						code.getVariable("nameValue"), graphIoVariableName), 1);

		code.add(new CodeSnippet("}", "#io#.write(\"}\");"));
		code.addNoIndent(new CodeSnippet("} else {"));
		code.add(new CodeSnippet(graphIoVariableName
				+ ".writeIdentifier(GraphIO.NULL_LITERAL);"));
		code.addNoIndent(new CodeSnippet("}"));
	}

	@Override
	public String getInitialValue() {
		return "null";
	}

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken(Token.UNSET)) {
			io.match();
			return GraphIO.Unset.UNSET;
		} else if (io.isNextToken(Token.LCRL)) {
			PMap<Object, Object> result = JGraLab.map();
			io.match();
			while (!io.isNextToken(Token.RCRL)) {
				Object mapKey = null;
				Object mapValue = null;
				mapKey = getKeyDomain().parseGenericAttribute(io);
				io.match(Token.HYPHEN);
				mapValue = getValueDomain().parseGenericAttribute(io);
				result = result.plus(mapKey, mapValue);
			}
			io.match();
			return result;
		} else if (io.isNextToken(Token.NULL_LITERAL)) {
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
			io.write("{");
			for (Object key : ((PMap<Object, Object>) data).keySet()) {
				getKeyDomain().serializeGenericAttribute(io, key);
				io.write("-");
				getValueDomain().serializeGenericAttribute(io,
						((PMap<Object, Object>) data).get(key));
			}
			io.write("}");
		} else {
			io.writeIdentifier(GraphIO.NULL_LITERAL);
		}
	}

	@Override
	protected void registerAttribute(Attribute a) {
		attributes = attributes.plus(a);
		((DomainImpl) keyDomain).registerAttribute(a);
		((DomainImpl) valueDomain).registerAttribute(a);
	}

	@Override
	public boolean isConformValue(Object value) {
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
			result &= getKeyDomain().isConformValue(key)
					&& getValueDomain().isConformValue(
							((PMap<?, ?>) value).get(key));
		}
		return result;
	}
}
