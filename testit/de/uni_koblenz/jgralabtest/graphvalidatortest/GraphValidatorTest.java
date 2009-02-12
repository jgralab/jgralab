/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.graphvalidatortest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation.ConstraintType;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedGraph;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedLink;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedNode;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedSchema;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class GraphValidatorTest {
	private ConstrainedGraph g = null;
	private GraphValidator validator = null;

	@Before
	public void setup() {
		g = ConstrainedSchema.instance().createConstrainedGraph();
		validator = new GraphValidator(g);
	}

	@Test
	public void validate1() throws IOException {
		g.createConstrainedNode();
		Set<ConstraintViolation> brokenConstraints = validator.validate();

		// Only one isolated node, so the following constraints cannot be met:
		// 1. uid is not > 0. 2. multiplicity is broken twice, cause there has
		// to be at least one ConstrainedLink between ConstrainedNodes. 3. The
		// name attribute is not set.

		assertEquals(4, brokenConstraints.size());
		assertEquals(2, getNumberOfBrokenConstraints(
				ConstraintType.MULTIPLICITY, brokenConstraints));
		assertEquals(2, getNumberOfBrokenConstraints(ConstraintType.GREQL,
				brokenConstraints));
		assertEquals(0, getNumberOfBrokenConstraints(
				ConstraintType.INVALID_GREQL_EXPRESSION, brokenConstraints));
	}

	@Test
	public void validate2() {
		g.createConstrainedNode();
		g.getSchema().getAttributedElementClass(
				new QualifiedName("ConstrainedNode")).addConstraint(
				"fihp.c tmi imp.t");
		Set<ConstraintViolation> brokenConstraints = validator.validate();

		// The graph equals the one from validate1(), but now we have one
		// additional broken constraint, e.g. the constraint is broken itself
		// (no valid GReQL query).

		assertEquals(5, brokenConstraints.size());
		assertEquals(2, getNumberOfBrokenConstraints(
				ConstraintType.MULTIPLICITY, brokenConstraints));
		assertEquals(2, getNumberOfBrokenConstraints(ConstraintType.GREQL,
				brokenConstraints));
		assertEquals(1, getNumberOfBrokenConstraints(
				ConstraintType.INVALID_GREQL_EXPRESSION, brokenConstraints));
	}

	@Test
	public void validate3() {
		ConstrainedNode n1 = g.createConstrainedNode();
		n1.setName("n1");
		n1.setUid(n1.getId());
		ConstrainedNode n2 = g.createConstrainedNode();
		n2.setName("n2");
		n2.setUid(n2.getId());
		ConstrainedLink l1 = g.createConstrainedLink(n1, n2);
		l1.setUid(Integer.MAX_VALUE - l1.getId());

		Set<ConstraintViolation> brokenConstraints = validator.validate();

		// This one is fine, except the broken GReQL query...

		System.out.println(brokenConstraints);
		assertEquals(1, brokenConstraints.size());
		assertEquals(1, getNumberOfBrokenConstraints(
				ConstraintType.INVALID_GREQL_EXPRESSION, brokenConstraints));
	}

	@Test
	public void createValidationReport() throws IOException {
		g.createConstrainedNode();
		GraphValidator v = new GraphValidator(g);
		File f = File.createTempFile("validation-report", ".html");
		v.createValidationReport(f.getCanonicalPath());
		assertTrue(f.length() > 0);
	}

	private static int getNumberOfBrokenConstraints(ConstraintType type,
			Set<ConstraintViolation> set) {
		int number = 0;
		for (ConstraintViolation ci : set) {
			if (ci.getConstraintType() == type) {
				number++;
			}
		}
		return number;
	}
}
