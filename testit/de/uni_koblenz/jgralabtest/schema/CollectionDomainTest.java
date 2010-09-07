/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.CollectionDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.exception.WrongSchemaException;

public abstract class CollectionDomainTest extends CompositeDomainTest {

	protected Domain expectedBaseDomain;

	@Override
	public void init() {
		super.init();
		expectedPackage1 = "";
		expectedPackage2 = "";
		expectedPathName1 = "";
		expectedPathName2 = "";
	}

	@Test
	public void testGetBaseDomain() {
		// tests if the correct baseDomain is returned
		assertEquals(expectedBaseDomain, ((CollectionDomain) domain1)
				.getBaseDomain());
	}

	@Test(expected = WrongSchemaException.class)
	public void testRejectionOfForeignSchemas() {
		// test if baseDomains of a foreign schema are rejected
		schema2.createEnumDomain("Enum1");
		schema1.createListDomain(schema2.getDomain("Enum1"));
	}
}
