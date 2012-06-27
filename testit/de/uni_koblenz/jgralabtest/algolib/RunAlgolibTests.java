package de.uni_koblenz.jgralabtest.algolib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.algolib.algorithms.RunAlgorithmTests;
import de.uni_koblenz.jgralabtest.algolib.functions.RunFunctionTests;
import de.uni_koblenz.jgralabtest.algolib.utils.RunUtilsTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RunUtilsTests.class, RunFunctionTests.class,
		RunAlgorithmTests.class })
public class RunAlgolibTests {

}
