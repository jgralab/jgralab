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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.BooleanDomain;
import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;

public class BooleanDomainImpl extends BasicDomainImpl implements BooleanDomain {

	public BooleanDomainImpl() {
		super("Boolean");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaString()
	 */
	public String getJavaAttributeImplementationTypeName() {
		return "boolean";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toJavaStringNonPrimitive()
	 */
	public String getJavaClassName() {
		return "Boolean";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#toTGString()
	 */
	public String getTGTypeName() {
		return "Boolean";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getReadMethod(java.lang.String, java.lang.String)
	 */
	public CodeBlock getReadMethod(String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = " + graphIoVariableName
				+ ".matchBoolean();");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Domain#getWriteMethod(java.lang.String, java.lang.String)
	 */
	public CodeBlock getWriteMethod(String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeBoolean("
				+ variableName + ");");
	}
}
