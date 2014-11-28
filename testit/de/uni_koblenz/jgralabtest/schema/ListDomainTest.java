/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

import java.util.HashSet;

import org.junit.Before;

public class ListDomainTest extends CollectionDomainTest {

	@Override
	@Before
	public void init() {
		super.init();
		// Initializing of DomainTest
		expectedDomainName = "List<Integer>";
		expectedJavaAttributeImplementationTypeName = "org.pcollections.PVector<java.lang.Integer>";
		expectedJavaClassName = "org.pcollections.PVector<java.lang.Integer>";
		expectedTgTypeName = "List<Integer>";
		expectedStringRepresentation = "domain List<domain Integer>";
		expectedQualifiedName1 = "List<Integer>";
		expectedQualifiedName2 = "List<Integer>";
		expectedDirectoryName1 = "List<Integer>";
		expectedDirectoryName2 = "List<Integer>";
		expectedSimpleName = "List<Integer>";
		domain1 = schema1.createListDomain(schema1.getDomain("Integer"));
		domain2 = schema2.createListDomain(schema2.getDomain("Integer"));
		otherDomain1 = schema1.getDomain("Integer");
		otherDomain2 = schema2.getDomain("String");
		// Initializing of CollectionDomainTest
		expectedBaseDomain = schema1.getDomain("Integer");
		// Initializing of CompositeDomainTest
		domain3 = schema1.createListDomain(schema1.getDomain("String"));
		domain4 = schema1.createListDomain(domain3);
		expectedCompositeDomains1 = new HashSet<>();
		expectedCompositeDomains2 = new HashSet<>();
		expectedCompositeDomains3 = new HashSet<>();
		expectedCompositeDomains4 = new HashSet<>();
		expectedCompositeDomains4.add(domain3);
		expectedDomains1 = new HashSet<>();
		expectedDomains1.add(schema1.getDomain("Integer"));
		expectedDomains2 = new HashSet<>();
		expectedDomains2.add(schema2.getDomain("Integer"));
		expectedDomains3 = new HashSet<>();
		expectedDomains3.add(schema1.getDomain("String"));
		expectedDomains4 = new HashSet<>();
		expectedDomains4.add(domain3);
	}

}
