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
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;

public class DoubleDomainImpl extends BasicDomainImpl implements DoubleDomain {
	// private static DoubleDomainImpl instance = new DoubleDomainImpl();

	// public static DoubleDomainImpl instance() {
	// return instance;
	// }

	public DoubleDomainImpl(Schema schema) throws SchemaException {
		QualifiedName qName = new QualifiedName("Double");
		if (schema.getDomain(qName) != null)
			throw new SchemaException(
					"Cannot create another DoubleDomain for Schema "
							+ schema.getQualifiedName());
		initialize(schema, qName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaString()
	 */
	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "double";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaStringNonPrimitive()
	 */
	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return "Double";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toTGString()
	 */
	@Override
	public String getTGTypeName(Package pkg) {
		return "Double";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getReadMethod(java.lang.String, java.lang.String)
	 */
	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = " + graphIoVariableName
				+ ".matchDouble();");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getWriteMethod(java.lang.String, java.lang.String)
	 */
	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeDouble("
				+ variableName + ");");
	}
}
