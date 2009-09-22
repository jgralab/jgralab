package de.uni_koblenz.jgralabtest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.codegenerator.RunCodeGeneratorTests;
import de.uni_koblenz.jgralabtest.graphvalidator.RunGraphValidatorTests;
import de.uni_koblenz.jgralabtest.greql2.RunGreql2Tests;
import de.uni_koblenz.jgralabtest.schema.RunSchemaTests;

/**
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { RunInstanceTests.class, RunSchemaTests.class,
		RunGraphValidatorTests.class, RunGreql2Tests.class,
		RunCodeGeneratorTests.class })
public class RunTests {

}
