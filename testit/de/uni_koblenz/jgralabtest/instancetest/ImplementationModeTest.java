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
package de.uni_koblenz.jgralabtest.instancetest;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

@RunWith(Parameterized.class)
public class ImplementationModeTest extends InstanceTest {
	public ImplementationModeTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	final int V = 4;
	final int E = 4;
	final int N = 10;
	private static final String filename = "./testit/testgraphs/implementationMode.tg";
	MinimalGraph g;

	@Before
	public void setup() throws CommitFailedException, GraphIOException {
		switch (implementationType) {
		case STANDARD:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		case TRANSACTION:
			g = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport(V, E);
			break;
		case SAVEMEM:
			g = MinimalSchema.instance().createMinimalGraph(V, E);
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createTransaction(g);
		for (int i = 0; i < N; ++i) {
			g.createNode();
		}
		for (int i = 0; i < N; ++i) {
			g.createLink(g.getFirstNode(), (Node) g.getVertex(i + 1));
		}
		commit(g);
		createTransaction(g);
		MinimalSchema.instance().saveMinimalGraph(filename, g);
		commit(g);
	}

	@AfterClass
	public static void cleanup() {
		new File(filename).delete();
	}

	@Test
	public void testLoadGraphFromFile() throws GraphIOException,
			CommitFailedException {
		MinimalGraph g2;
		switch (implementationType) {
		case STANDARD:
			g2 = MinimalSchema.instance().loadMinimalGraph(filename);
			assertTrue(g2.hasStandardSupport());
			assertFalse(g2.hasTransactionSupport());
			assertFalse(g2.hasSavememSupport());
			break;
		case TRANSACTION:
			g2 = MinimalSchema.instance()
					.loadMinimalGraphWithTransactionSupport(filename);
			createReadOnlyTransaction(g2);
			assertFalse(g2.hasStandardSupport());
			assertTrue(g2.hasTransactionSupport());
			assertFalse(g2.hasSavememSupport());
			commit(g2);
			break;
		case SAVEMEM:
			g2 = MinimalSchema.instance().loadMinimalGraphWithSavememSupport(
					filename);
			assertFalse(g2.hasStandardSupport());
			assertFalse(g2.hasTransactionSupport());
			assertTrue(g2.hasSavememSupport());
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}

	}

}
