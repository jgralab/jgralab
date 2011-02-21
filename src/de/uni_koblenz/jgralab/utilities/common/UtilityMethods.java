/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.utilities.common;

public class UtilityMethods {

	/**
	 * Generates a URI from a qualified Name for use in XML-files. It replaces
	 * all occurrences of "_" with "-", swaps the first and the second element
	 * of the qualified name and appends the remainder as folder structure.
	 * Example: de.uni_koblenz.jgralab.greql2 => uni-koblenz.de/jgralab/greql2
	 * 
	 * @param qualifiedName
	 *            the qualified name to convert.
	 * @return a URI as String according to the given qualified Name.
	 */
	public static String generateURI(String qualifiedName) {
		qualifiedName = qualifiedName.replace('_', '-');
		String[] uri = qualifiedName.split("\\.");

		StringBuilder namespaceURI = new StringBuilder();
		namespaceURI.append("http://");
		if (uri.length > 1) {
			namespaceURI.append(uri[1]).append(".").append(uri[0]);

			for (int i = 2; i < uri.length; i++) {
				namespaceURI.append("/").append(uri[i]);
			}

		}

		return namespaceURI.toString();
	}
}
