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
package de.uni_koblenz.jgralabtest.greql2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.greql2.evaluator.GreqlEvaluatorTest;
import de.uni_koblenz.jgralabtest.greql2.exception.ExceptionTest;
import de.uni_koblenz.jgralabtest.greql2.funlib.FunctionTest;
import de.uni_koblenz.jgralabtest.greql2.funlib.PathSystemFunctionTest;
import de.uni_koblenz.jgralabtest.greql2.funlib.SliceFunctionTest;
import de.uni_koblenz.jgralabtest.greql2.jvalue.JValueTest;
import de.uni_koblenz.jgralabtest.greql2.optimizer.OptimizerTest;

/**
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ExceptionTest.class, FunctionTest.class,
		GreqlEvaluatorTest.class, JValueTest.class, OptimizerTest.class,
		ParserTest.class, PathSystemFunctionTest.class, ProgressTest.class,
		SliceFunctionTest.class, SpeedTest.class, StoreJValueTest.class,
		SystemTest.class, ThisLiteralTest.class, GreqlSerializationTest.class })
public class RunGreql2Tests {

}