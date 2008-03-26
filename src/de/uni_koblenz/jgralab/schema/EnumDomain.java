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

import java.util.List;


/**
 * represents an enumeration domain, instances may exist multiple times per schema
 * @author Steffen Kahle
 *
 */
public interface EnumDomain extends BasicDomain {

	/**
	 * adds aConst to the list of enums
	 * @param aConst constant to be added
	 */
	public void addConst(String aConst) ;

	/**
	 * deletes aConst from the list of enums
	 * @param aConst
	 */
	public void deleteConst(String aConst);


	/**
	 * @return the name of this enum, must be unique in schema
	 */
	public String getName();
	
	/**
	 * @return all the enum strings of this enum domain
	 */
	public List<String> getConsts();
}