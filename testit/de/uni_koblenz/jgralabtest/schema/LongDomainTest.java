/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.schema;

import org.junit.Before;

public class LongDomainTest extends NativeDomainTest {

	@Override
	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "Long";
		expectedJavaAttributeImplementationTypeName = "long";
		expectedJavaClassName = "Long";
		expectedTgTypeName = "Long";
		expectedStringRepresentation = "domain Long";
		expectedQualifiedName1 = "Long";
		expectedQualifiedName2 = "Long";
		expectedDirectoryName1 = "Long";
		expectedDirectoryName2 = "Long";
		expectedSimpleName = "Long";
		expectedUniqueName1 = "Long";
		expectedUniqueName2 = "Long";
		domain1 = schema1.getDomain("Long");
		domain2 = schema2.getDomain("Long");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema2.getDomain("String");
	}

}
