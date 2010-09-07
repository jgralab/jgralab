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
package de.uni_koblenz.jgralabtest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.codegenerator.RunCodeGeneratorTests;
import de.uni_koblenz.jgralabtest.graphvalidator.RunGraphValidatorTests;
import de.uni_koblenz.jgralabtest.greql2.RunGreql2Tests;
import de.uni_koblenz.jgralabtest.instancetest.RunInstanceTests;
import de.uni_koblenz.jgralabtest.schema.RunSchemaTests;
import de.uni_koblenz.jgralabtest.utilities.RunUtilitiesTests;

/**
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { RunInstanceTests.class, RunSchemaTests.class,
		RunGraphValidatorTests.class, RunGreql2Tests.class,
		RunCodeGeneratorTests.class, RunUtilitiesTests.class })
public class RunTests {

}
