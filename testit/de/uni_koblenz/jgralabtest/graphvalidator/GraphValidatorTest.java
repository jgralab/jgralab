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
package de.uni_koblenz.jgralabtest.graphvalidator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.graphvalidator.BrokenGReQLConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GReQLConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.graphvalidator.MultiplicityConstraintViolation;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedGraph;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedLink;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedNode;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedSchema;
import de.uni_koblenz.jgralabtest.schemas.constrained.OtherConstrainedNode;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class GraphValidatorTest {
	private ConstrainedGraph g = null;
	private GraphValidator validator = null;

	{
		JGraLab.setLogLevel(Level.OFF);
	}

	@Before
	public void setup() {
		g = ConstrainedSchema.instance().createConstrainedGraph();
		validator = new GraphValidator(g);
	}

	@Test
	public void validate1() throws IOException {
		g.createConstrainedNode();
		Set<ConstraintViolation> brokenConstraints = validator.validate();

		printBrokenConstraints(brokenConstraints);

		// each ConstrainedNode must have (1,*) in and outgoing ConstrainedLink
		assertEquals(2, getNumberOfBrokenConstraints(
				MultiplicityConstraintViolation.class, brokenConstraints));
		// uid should be > 0 and name has to be set
		assertEquals(2, getNumberOfBrokenConstraints(
				GReQLConstraintViolation.class, brokenConstraints));
		// The graph class has to invalid constraints
		assertEquals(2, getNumberOfBrokenConstraints(
				BrokenGReQLConstraintViolation.class, brokenConstraints));
	}

	@Test
	public void validate2() {
		ConstrainedNode n1 = g.createConstrainedNode();
		n1.set_name("n1");
		n1.set_uid(n1.getId());
		ConstrainedNode n2 = g.createConstrainedNode();
		n2.set_name("n2");
		n2.set_uid(n2.getId());
		ConstrainedLink l1 = g.createConstrainedLink(n1, n2);
		l1.set_uid(Integer.MAX_VALUE - l1.getId());

		Set<ConstraintViolation> brokenConstraints = validator.validate();

		printBrokenConstraints(brokenConstraints);
		// This one is fine, except the broken GReQL query...
		assertEquals(2, getNumberOfBrokenConstraints(
				BrokenGReQLConstraintViolation.class, brokenConstraints));
	}

	@Test
	public void validate3() {
		OtherConstrainedNode n1 = g.createOtherConstrainedNode();
		n1.set_name("n1");
		n1.set_uid(n1.getId());
		// This should be between 0 and 20.
		n1.set_niceness(-17);

		Set<ConstraintViolation> brokenConstraints = validator
				.validateConstraints(n1.getAttributedElementClass());

		printBrokenConstraints(brokenConstraints);
		// This one is fine, except that niceness should be between 0 and 20.
		assertEquals(1, getNumberOfBrokenConstraints(
				GReQLConstraintViolation.class, brokenConstraints));
	}

	private static int getNumberOfBrokenConstraints(
			Class<? extends ConstraintViolation> type,
			Set<ConstraintViolation> set) {
		int number = 0;
		for (ConstraintViolation ci : set) {
			if (type.isInstance(ci)) {
				number++;
			}
		}
		return number;
	}

	private static void printBrokenConstraints(Set<ConstraintViolation> set) {
		System.out.println(">>>------------------------------");
		if (set.isEmpty()) {
			System.out.println("No broken constraints. :-)");
		} else {
			for (ConstraintViolation ci : set) {
				System.out.println(ci);
			}
		}
		System.out.println("<<<------------------------------");
	}
}
