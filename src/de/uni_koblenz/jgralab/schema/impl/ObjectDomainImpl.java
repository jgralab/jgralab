/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
import de.uni_koblenz.jgralab.schema.ObjectDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SchemaException;

public class ObjectDomainImpl extends BasicDomainImpl implements ObjectDomain {
	// private static ObjectDomainImpl instance = new ObjectDomainImpl();
	//	
	// public static ObjectDomainImpl instance() {
	// return instance;
	// }

	public ObjectDomainImpl(Schema schema) throws SchemaException {
		QualifiedName qName = new QualifiedName("Object");
		if (schema.getDomain(qName) != null)
			throw new SchemaException(
					"Cannot create another ObjectDomain for Schema "
							+ schema.getQualifiedName());
		initialize(schema, qName);
	}

	@Override
	public boolean isComposite() {
		// TODO why?
		return true;
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "java.lang.Object";
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return getJavaAttributeImplementationTypeName(schemaRootPackagePrefix);
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return "Object";
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = " + graphIoVariableName
				+ ".matchObject();");
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeObject("
				+ variableName + ");");
	}

}
