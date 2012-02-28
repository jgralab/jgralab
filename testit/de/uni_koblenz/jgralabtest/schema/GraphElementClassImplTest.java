/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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

import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

import de.uni_koblenz.jgralab.schema.GraphElementClass;

public abstract class GraphElementClassImplTest<GEC extends GraphElementClass<?, ?>>
		extends AttributedElementClassImplTest<GEC> {

	protected static Random random = new Random();

	/*
	 * Tests for the getAllSubClasses() method.
	 */
	/**
	 * getAllSubClasses()
	 * 
	 * TEST CASE: Getting all subclasses of an element with one direct subclass
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct
	 * subclasses
	 * 
	 * TEST CASE: Getting all subclasses of an element with multiple direct and
	 * indirect subclasses
	 * 
	 * TEST CASE: Getting all subclasses of an element that has no subclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSubClasses(Vector<GEC> expectedSubClasses) {
		schema.finish();

		@SuppressWarnings("unchecked")
		Set<GEC> subClasses = (Set<GEC>) attributedElement.getAllSubClasses();

		Assert.assertEquals(expectedSubClasses.size(), subClasses.size());

		// Check if this element contains all expected subclasses
		for (GEC expectedSubClass : expectedSubClasses) {
			boolean expectedSubClassFound = false;
			for (GEC subClass : subClasses) {
				if (subClass.getQualifiedName().equals(
						expectedSubClass.getQualifiedName())) {
					expectedSubClassFound = true;
					break;
				}
			}
			Assert.assertTrue(
					"The following subclass was expected but not found: "
							+ expectedSubClass.getQualifiedName(),
					expectedSubClassFound);
		}
	}

	/*
	 * Tests for the getAllSuperClasses() method.
	 */
	/**
	 * getAllSuperClasses()
	 * 
	 * TEST CASE: Getting all superclasses of an element with one direct
	 * superclass
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * superclasses
	 * 
	 * TEST CASE: Getting all superclasses of an element with multiple direct
	 * and indirect superclasses
	 * 
	 * TEST CASE: Getting all superclasses of an element that has no
	 * superclasses
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetAllSuperClasses(Vector<GEC> expectedSuperClasses) {
		schema.finish();

		@SuppressWarnings("unchecked")
		Set<GEC> superClasses = (Set<GEC>) attributedElement
				.getAllSuperClasses();

		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if this element contains all expected superclasses
		for (GEC expectedSuperClass : expectedSuperClasses) {
			boolean expectedSuperClassFound = false;
			for (GEC superClass : superClasses) {
				if (superClass.getQualifiedName().equals(
						expectedSuperClass.getQualifiedName())) {
					expectedSuperClassFound = true;
					break;
				}
			}
			Assert.assertTrue(
					"The following superclass was expected but not found: "
							+ expectedSuperClass.getQualifiedName(),
					expectedSuperClassFound);
		}
	}

	/*
	 * Tests for the getDirectSubClasses() method.
	 */
	/**
	 * getDirectSubClasses()
	 * 
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has one
	 * direct subclass.
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has multiple
	 * direct subclasses.
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has multiple
	 * direct and indirect subclasses.
	 * 
	 * TEST CASE: Getting all direct subclasses of an element that has no direct
	 * subclasses.
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetDirectSubClasses(Vector<GEC> expectedSubClasses) {
		schema.finish();

		@SuppressWarnings("unchecked")
		Set<GEC> subClasses = (Set<GEC>) attributedElement
				.getDirectSubClasses();

		Assert.assertEquals(subClasses.size(), expectedSubClasses.size());

		// Check if subClasses contains all expected subclasses, and only these
		for (GEC subClass : subClasses) {
			boolean subClassFound = false;
			for (GEC expectedSubClass : expectedSubClasses) {
				if (expectedSubClass.getQualifiedName().equals(
						subClass.getQualifiedName())) {
					subClassFound = true;
					break;
				}
			}
			Assert.assertTrue("The following subclass is unexpected: "
					+ subClass.getQualifiedName(), subClassFound);
		}
	}

	/*
	 * Tests for the getDirectSuperClasses() method.
	 */
	/**
	 * getDirectSuperClasses()
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has one
	 * direct superclass.
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has
	 * multiple direct superclasses.
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has
	 * multiple direct and indirect superclasses.
	 * 
	 * TEST CASE: Getting all direct superclasses of an element that has no
	 * direct superclasses.
	 * 
	 * NOTE: This method is called upon in all of this classes´ subclasses.
	 */
	public final void testGetDirectSuperClasses(Vector<GEC> expectedSuperClasses) {
		schema.finish();

		@SuppressWarnings("unchecked")
		Set<GEC> superClasses = (Set<GEC>) attributedElement
				.getDirectSuperClasses();

		if (expectedSuperClasses.size() != superClasses.size()) {
			System.err.println("EXPECTED");
			System.err.println("\t" + expectedSuperClasses);
			System.err.println("\t" + superClasses);
		}
		Assert.assertEquals(expectedSuperClasses.size(), superClasses.size());

		// Check if superClasses contains all expected superclasses, and only
		// these
		for (GEC superClass : superClasses) {
			boolean superClassFound = false;
			for (GEC expectedSuperClass : expectedSuperClasses) {
				if (expectedSuperClass.getQualifiedName().equals(
						superClass.getQualifiedName())) {
					superClassFound = true;
					break;
				}
			}
			Assert.assertTrue("The following superclass is unexpected: "
					+ superClass.getQualifiedName(), superClassFound);
		}
	}

	@Test
	public void testGetGraphClass() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testGetSchema() {
		// TODO Auto-generated method stub
	}

	@Override
	@Test
	public void testToString() {
		// TODO Auto-generated method stub
	}
}
