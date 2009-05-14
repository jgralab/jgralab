/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralabtest.greql2.exception;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ExceptionTest extends GenericTests {

	@Test(expected = WrongFunctionParameterException.class)
	public void testFunctionException() throws Exception {
		String queryString = "let x:=list(\"a\",4 ,5) in avg(x)";
		evalTestQuery("FunctionException", queryString);
	}

	@Test(expected = UnknownTypeException.class)
	public void testUnknownTypeException() throws Exception {
		String queryString = "from e:E{isEdgeOf} report e end";
		evalTestQuery("UnknownTypeException", queryString);
	}

}
