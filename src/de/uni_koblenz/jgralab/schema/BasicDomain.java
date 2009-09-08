/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class of the basic domains Boolean, Double, Integer, Long, and String.
 * 
 * @author ist@uni-koblenz.de
 */
public interface BasicDomain extends Domain {

	/**
	 * List of names of the basic domains.
	 */
	public static final Set<String> BASIC_DOMAINS = new TreeSet<String>(Arrays
			.asList(new String[] { BooleanDomain.BOOLEANDOMAIN_NAME,
					DoubleDomain.DOUBLEDOMAIN_NAME,
					IntegerDomain.INTDOMAIN_NAME, LongDomain.LONGDOMAIN_NAME,
					StringDomain.STRINGDOMAIN_NAME }));

}
