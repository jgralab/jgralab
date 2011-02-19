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
/**
 *
 */
package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.exception.RecordCycleException;

/**
 * TODO: More testing!
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class MapDomainTest extends CompositeDomainTest {

	protected Domain keyDomain1, keyDomain2;
	protected CompositeDomain keyDomain3, keyDomain4;

	protected Domain valueDomain1, valueDomain2;
	protected CompositeDomain valueDomain3, valueDomain4;

	@Override
	@Before
	public void init() {
		super.init();
		// Initializing for the DomainTest
		schema1Package = "package1";
		schema1Name = "schema1";
		schema2Package = "package2";
		schema2Name = "schema2";
		keyDomain1 = schema1.getIntegerDomain();
		valueDomain1 = schema1.getBooleanDomain();
		domain1 = schema1.createMapDomain(keyDomain1, valueDomain1);
		keyDomain2 = schema2.getDomain("Integer");
		valueDomain2 = schema2.getDomain("Boolean");
		domain2 = schema2.createMapDomain(keyDomain2, valueDomain2);
		Domain otherValueDomain = schema1.getDoubleDomain();
		otherDomain1 = schema1.createMapDomain(keyDomain1, otherValueDomain);
		Domain otherKeyDomain = schema1.getDomain("String");
		otherDomain2 = schema1.createMapDomain(otherKeyDomain, valueDomain1);
		expectedJavaAttributeImplementationTypeName = "java.util.Map<Integer, Boolean>";
		expectedJavaClassName = "java.util.Map<Integer, Boolean>";
		expectedPackage1 = "";
		expectedPackage2 = "";
		expectedPathName1 = "";
		expectedPathName2 = "";
		expectedTgTypeName = "Map<Integer, Boolean>";
		expectedStringRepresentation = "domain Map<domain Integer, domain Boolean>";
		expectedDirectoryName1 = "Map<Integer, Boolean>";
		expectedDirectoryName2 = "Map<Integer, Boolean>";
		expectedQualifiedName1 = "Map<Integer, Boolean>";
		expectedQualifiedName2 = "Map<Integer, Boolean>";
		expectedSimpleName = expectedUniqueName1 = expectedQualifiedName1;
		expectedUniqueName2 = expectedQualifiedName2;
		// Initializing for the CompositeDomainTest
		keyDomain3 = schema1.createListDomain(schema1.getDomain("Integer"));
		List<RecordComponent> components = new ArrayList<RecordComponent>();
		components.add(new RecordComponent("aList", keyDomain3));
		components.add(new RecordComponent("aMap", domain1));
		valueDomain3 = schema1.createRecordDomain("Record1", components);
		keyDomain4 = valueDomain4 = domain3 = schema1.createMapDomain(
				keyDomain3, valueDomain3);
		domain4 = schema1.createMapDomain(domain3, domain3);
		expectedCompositeDomains3.add(keyDomain3);
		expectedCompositeDomains3.add(valueDomain3);
		expectedCompositeDomains4.add(domain3);
		expectedDomains1.add(keyDomain1);
		expectedDomains1.add(valueDomain1);
		expectedDomains2.add(keyDomain2);
		expectedDomains2.add(valueDomain2);
		expectedDomains3.add(keyDomain3);
		expectedDomains3.add(valueDomain3);
		expectedDomains4.add(domain3);
	}

	@Test(expected = RecordCycleException.class)
	public void testRejectionOfCyclicInclusion() {
		/*
		 * Tests the rejection of the following case: A MapDomain M contains a
		 * RecordDomain, which contains another MapDomain, which contains a
		 * RecordDomain, which contains M.
		 */
		schema1.createRecordDomain("Record3");
		RecordDomain rec3 = (RecordDomain) schema1.getDomain("Record3");
		schema1.createMapDomain(schema1.getDomain("Boolean"), rec3);
		Domain map1 = schema1.getDomain("Map<Boolean, Record3>");
		schema1.createRecordDomain("Record2");
		RecordDomain rec2 = (RecordDomain) schema1.getDomain("Record2");
		schema1.createMapDomain(schema1.getDomain("Boolean"), rec2);
		Domain map2 = schema1.getDomain("Map<Boolean, Record2>");
		rec3.addComponent("map2", map2);
		rec2.addComponent("map1", map1);
	}

	@Override
	@Test
	public void testEquals() {
		super.testEquals();
		schema1.createMapDomain(schema1.getDomain("String"), schema1
				.getDomain("Double"));
		// A MapDomains with a different KeyDomain must not be equal
		assertFalse(schema1.getDomain("Map<String, Double>").equals(
				otherDomain1));
		// A MapDomains with a different ValueDomain must not be equal
		assertFalse(schema1.getDomain("Map<String, Double>").equals(
				otherDomain2));
	}

	@Test
	public void testGetKeyDomain() {
		// tests if the correct keyDomain is returned
		assertEquals(keyDomain1, ((MapDomain) domain1).getKeyDomain());
		assertEquals(keyDomain2, ((MapDomain) domain2).getKeyDomain());
		assertEquals(keyDomain3, ((MapDomain) domain3).getKeyDomain());
		assertEquals(keyDomain4, ((MapDomain) domain4).getKeyDomain());
	}

	@Test
	public void testGetValueDomain() {
		// tests if the correct valueDomain is returned
		assertEquals(valueDomain1, ((MapDomain) domain1).getValueDomain());
		assertEquals(valueDomain2, ((MapDomain) domain2).getValueDomain());
		assertEquals(valueDomain3, ((MapDomain) domain3).getValueDomain());
		assertEquals(valueDomain4, ((MapDomain) domain4).getValueDomain());
	}

}
