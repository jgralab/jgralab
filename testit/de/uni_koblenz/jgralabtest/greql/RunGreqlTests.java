package de.uni_koblenz.jgralabtest.greql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.greql.evaluator.RunVertexEvaluatorTests;
import de.uni_koblenz.jgralabtest.greql.exception.ExceptionTest;
import de.uni_koblenz.jgralabtest.greql.funlib.RunFunlibTests;
import de.uni_koblenz.jgralabtest.greql.optimizer.OptimizerTest;
import de.uni_koblenz.jgralabtest.greql.parallel.ParallelTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ExceptionTest.class, RunFunlibTests.class,
		RunVertexEvaluatorTests.class, OptimizerTest.class, ProgressTest.class, // what
																				// does
																				// ProgressTest
																				// actually
																				// test?
		SpeedTest.class, // what does SpeedTest actually test?
		SystemTest.class, // what does SystemTest actually test?
		ThisLiteralTest.class, GreqlQueryFunctionTest.class, ParallelTest.class })
public class RunGreqlTests {

}
