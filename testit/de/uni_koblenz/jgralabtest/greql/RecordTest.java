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

package de.uni_koblenz.jgralabtest.greql;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.impl.RecordImpl;

public class RecordTest {

	@Test
	public void testHashCode() throws Exception {
		Record r1 = RecordImpl.empty().plus("offset", 4).plus("length", 17);
		Record r2 = RecordImpl.empty().plus("offset", 4).plus("length", 17);
		assertEquals(r1.hashCode(), r2.hashCode());
	}

	@Test
	public void testEquals() throws Exception {
		Record r1 = RecordImpl.empty().plus("offset", 4).plus("length", 17);
		RecordImpl r2 = RecordImpl.empty().plus("offset", 4);

		assertFalse(r1.equals(null));
		assertFalse(r1.equals(r2));

		assertFalse(r2.equals(null));
		assertFalse(r2.equals(r1));

		r2 = r2.plus("length", 17);

		assertTrue(r1.equals(r2));
		assertTrue(r2.equals(r1));
	}
}
