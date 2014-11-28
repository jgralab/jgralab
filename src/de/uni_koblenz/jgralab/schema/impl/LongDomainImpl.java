/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.io.IOException;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.impl.TgLexer.Token;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeSnippet;

public final class LongDomainImpl extends BasicDomainImpl implements LongDomain {

	LongDomainImpl(Schema schema) {
		super(LONGDOMAIN_NAME, (PackageImpl) schema.getDefaultPackage());
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "long";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return "java.lang.Long";
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName, boolean withUnsetCheck) {
		return maybeWrapInUnsetCheck(graphIoVariableName, withUnsetCheck,
				variableName + " = " + graphIoVariableName + ".matchLong();");
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return LONGDOMAIN_NAME;
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeLong("
				+ variableName + ");");
	}

	@Override
	public String getInitialValue() {
		return "0L";
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public Object parseGenericAttribute(GraphIO io) throws GraphIOException {
		if (io.isNextToken(Token.UNSET_LITERAL)) {
			io.match();
			return GraphIO.Unset.UNSET;
		}
		return io.matchLong();
	}

	@Override
	public void serializeGenericAttribute(GraphIO io, Object data)
			throws IOException {
		io.writeLong((Long) data);

	}

	@Override
	public boolean isConformValue(Object value) {
		return Long.class.isInstance(value);
	}
}
