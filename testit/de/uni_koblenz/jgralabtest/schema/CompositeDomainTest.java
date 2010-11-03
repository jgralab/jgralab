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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;

public abstract class CompositeDomainTest extends DomainTest {

	protected CompositeDomain domain3;
	protected CompositeDomain domain4;
	protected Set<CompositeDomain> expectedCompositeDomains1 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains2 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains3 = new HashSet<CompositeDomain>();
	protected Set<CompositeDomain> expectedCompositeDomains4 = new HashSet<CompositeDomain>();
	protected Set<Domain> expectedDomains1 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains2 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains3 = new HashSet<Domain>();
	protected Set<Domain> expectedDomains4 = new HashSet<Domain>();

	@Override
	public void init() {
		super.init();
		isComposite = true;
	}

	@Test
	public void testGetAllComponentCompositeDomains() {
		// tests if a set with all CompositeDomains is returned
		Set<CompositeDomain> set1 = ((CompositeDomain) domain1)
				.getAllComponentCompositeDomains();
		Set<CompositeDomain> set2 = ((CompositeDomain) domain2)
				.getAllComponentCompositeDomains();
		Set<CompositeDomain> set3 = domain3.getAllComponentCompositeDomains();
		Set<CompositeDomain> set4 = domain4.getAllComponentCompositeDomains();

		assertEquals(expectedCompositeDomains1, set1);
		assertEquals(expectedCompositeDomains2, set2);
		assertEquals(expectedCompositeDomains3, set3);
		assertEquals(expectedCompositeDomains4, set4);
	}

	@Test
	public void testGetAllComponentDomains() {
		// tests if a set with the domains of all components is returned
		Set<Domain> set1 = ((CompositeDomain) domain1).getAllComponentDomains();
		Set<Domain> set2 = ((CompositeDomain) domain2).getAllComponentDomains();
		Set<Domain> set3 = domain3.getAllComponentDomains();
		Set<Domain> set4 = domain4.getAllComponentDomains();

		assertEquals(expectedDomains1, set1);
		assertEquals(expectedDomains2, set2);
		assertEquals(expectedDomains3, set3);
		assertEquals(expectedDomains4, set4);
	}
}
