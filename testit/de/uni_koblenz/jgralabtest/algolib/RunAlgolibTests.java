package de.uni_koblenz.jgralabtest.algolib;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.algolib.algorithms.RunAlgorithmTests;
import de.uni_koblenz.jgralabtest.algolib.functions.RunFunctionTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RunFunctionTests.class, RunAlgorithmTests.class })
public class RunAlgolibTests {

}
