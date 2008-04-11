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
 
package de.uni_koblenz.jgralab.schema;

public interface NamedElement {
	
	QualifiedName getQName();

	/**
	 * @return the unique name of the element in the package
	 * without the fully qualified package name
	 */
	public String getSimpleName();
	
	
	/**	 
	 * @return the unique name of the element in the schema,
	 * if there is only one class in the schema with this short name, 
	 * the short name is returned. Otherwise, the fully qualified package name
	 * is returned in a camel-cased underscored manner
	 */
	public String getUniqueName();
	
	/**
	 * Sets the unique name of this element in the schema
	 * @throws Exception if the same unique name is used for another element
	 * @param name
	 */
	public void setUniqueName(String name);

	public String getVariableName();

	/**
	 * @return the fully qualified name of this element in the schema.
	 * This is the fully qualified name of the package the element is  
	 * located in together with the short name of the element
	 */
	public String getQualifiedName(); 
	
	@Deprecated
	public String getName();
	
	String getQualifiedName(Package pkg);
	String getPackageName();
	String getPathName();
	String getDirectoryName();
	
	public Schema getSchema();
}
