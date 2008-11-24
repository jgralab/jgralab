/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;
import de.uni_koblenz.jgralab.schema.StringDomain;

public class StringDomainImpl extends BasicDomainImpl implements StringDomain {
	// private static StringDomainImpl instance = new StringDomainImpl();
	//
	// public static StringDomainImpl instance() {
	// return instance;
	// }

	public StringDomainImpl(Schema schema) throws SchemaException {
		QualifiedName qName = new QualifiedName("String");
		if (schema.getDomain(qName) != null) {
			throw new SchemaException(
					"Cannot create another StringDomain for Schema "
							+ schema.getQualifiedName());
		}
		initialize(schema, qName);
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "String";
	}

	@Override
	public String toString() {
		return "domain String";
	}

	public boolean isBasic() {
		return true;
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return "String";
	};

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = " + graphIoVariableName
				+ ".matchUtfString();");
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeUtfString("
				+ variableName + ");");
	}
}
