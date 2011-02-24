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
package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;

public class EnumDomainTest extends BasicDomainTest {

	private EnumDomain domain3;
	private EnumDomain domain4;

	@Override
	@Before
	public void init() {
		super.init();
		// Initializing DomainTest
		schema1.createEnumDomain("package1.subpackage1.Enum1");
		domain1 = schema1.getDomain("package1.subpackage1.Enum1");
		schema2.createEnumDomain("package1.subpackage1.Enum1");
		domain2 = schema2.getDomain("package1.subpackage1.Enum1");
		otherDomain1 = schema1.getDomain("Boolean");
		otherDomain2 = schema2.getDomain("String");
		expectedJavaClassName = schema1Package + ".package1.subpackage1.Enum1";
		expectedTgTypeName = "Enum1";
		expectedStringRepresentation = "domain Enum package1.subpackage1.Enum1 ()";
		expectedDirectoryName1 = "package1" + sep + "subpackage1" + sep
				+ "Enum1";
		expectedDirectoryName2 = "package1" + sep + "subpackage1" + sep
				+ "Enum1";
		expectedQualifiedName1 = "package1.subpackage1.Enum1";
		expectedQualifiedName2 = "package1.subpackage1.Enum1";
		expectedPackage1 = "package1.subpackage1";
		expectedPackage2 = "package1.subpackage1";
		expectedPathName1 = "package1" + sep + "subpackage1";
		expectedPathName2 = "package1" + sep + "subpackage1";
		expectedSimpleName = "Enum1";

		// same domainname as Domain1 but in other package (for testing
		// getUniqueName)
		schema1.createEnumDomain("package1.Enum1");
		domain3 = (EnumDomain) schema1.getDomain("package1.Enum1");
		domain3.addConst("Hugo");
		domain3.addConst("Sebastian");
		domain3.addConst("Volker");
		domain3.addConst("Kerstin");
		domain3.addConst("Sascha");
		schema1.createEnumDomain("package1.Domain2");
		domain4 = (EnumDomain) schema1.getDomain("package1.Domain2");
	}

	@Test
	@Override
	public void testGetJavaAttributeImplementationTypeName() {
		// tests if the correct javaAttributeImplementationTypeName is returned
		assertEquals(schema1Package + ".package1.subpackage1.Enum1",
				domain1.getJavaAttributeImplementationTypeName(schema1Package));
	}

	@Test
	@Override
	public void testToString() {
		// tests if the correct string representation is returned
		super.testToString();
		assertEquals(
				"domain Enum package1.Enum1 (0: Hugo, 1: Sebastian, 2: Volker, 3: Kerstin, 4: Sascha)",
				domain3.toString());
	}

	@Test
	@Override
	public void testGetPackageName() {
		// tests if the correct packageName is returned
		super.testGetPackageName();
		assertEquals("package1", domain3.getPackageName());
		assertEquals("package1", domain4.getPackageName());
	}

	@Test
	public void testAddConst1() {
		// Test of adding a new constant in a nonempty EnumDomain
		int oldsize = domain3.getConsts().size();
		domain3.addConst("Daniel");
		assertEquals(oldsize + 1, domain3.getConsts().size());
		assertTrue(domain3.getConsts().contains("Daniel"));
		assertTrue(domain3.getConsts().contains("Hugo"));
		assertTrue(domain3.getConsts().contains("Sebastian"));
		assertTrue(domain3.getConsts().contains("Volker"));
		assertTrue(domain3.getConsts().contains("Kerstin"));
		assertTrue(domain3.getConsts().contains("Sascha"));
	}

	@Test
	public void testAddConst2() {
		// add constant into an empty EnumDomain
		EnumDomain enum1 = (EnumDomain) domain1;
		enum1.addConst("newConstant");
		assertEquals(1, enum1.getConsts().size());
		assertTrue(enum1.getConsts().contains("newConstant"));
	}

	@Test(expected = InvalidNameException.class)
	public void testAddConst3() {
		// add constant that already exists
		domain3.addConst("Sebastian");
	}

	@Test(expected = InvalidNameException.class)
	public void testAddConst4() {
		// add constant that already exists
		domain3.addConst("-a");
	}

	@Test(expected = InvalidNameException.class)
	public void testAddConst5() {
		// add constant that already exists
		domain3.addConst("abc%");
	}

	@Test
	public void testGetConsts() {
		// get constants of an EnumDomain which doesn't contain any
		assertEquals(0, domain4.getConsts().size());
		// get constants of an EnumDomain which contains several
		List<String> consts = domain3.getConsts();
		assertEquals(5, consts.size());
		assertEquals("Hugo", consts.get(0));
		assertEquals("Sebastian", consts.get(1));
		assertEquals("Volker", consts.get(2));
		assertEquals("Kerstin", consts.get(3));
		assertEquals("Sascha", consts.get(4));
	}
}
