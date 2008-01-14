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
 
package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;

/**
 * generalizes all domains
 * 
 * @author Steffen Kahle
 * 
 */
public interface Domain {

	/**
	 * @return the name of this domain
	 */
	public String getName();

	
	/**
	 * @return true if this domain is a composite domain
	 */
	public boolean isComposite();
	
	/**
	 * @param o the domain which is to be compared
	 * @return true, if domains are equal
	 */
	public boolean equals(Object o);

	/**
	 * example: int for integer List<Boolean> for a list with basedomain
	 * boolean
	 * 
	 * @return java representation of this attribute
	 */
	public String getJavaAttributeImplementationTypeName();

	/**
	 * example: Integer for integer
	 * 
	 * @return the non primitive representation of this attribute, only affects
	 *         int, boolean, double
	 */
	public String getJavaClassName();

	/**
	 * example: List<Boolean> returns "list with basedomain boolean"
	 * 
	 * @return a textual representation of this attribute
	 */
	public String toString();

	/**
	 * example: List<String>
	 * 
	 * @return the string how the domain is represented in the jg file
	 */
	public String getTGTypeName();

	/**
	 * @return a code fragment to read a value of this domain from the GraphIO
	 *         object named graphIoVariablename into the variableName
	 */
	public CodeBlock getReadMethod(String variableName,
			String graphIoVariableName);

	/**
	 * @return a code fragment to write a value of this domain to the GraphIO
	 *         oject named graphIoVariablename into the variableName
	 */
	public CodeBlock getWriteMethod(String variableName,
			String graphIoVariableName);
}