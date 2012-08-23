package de.uni_koblenz.jgralabtest.greql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralabtest.greql.evaluator.RunVertexEvaluatorTests;
import de.uni_koblenz.jgralabtest.greql.exception.ExceptionTest;
import de.uni_koblenz.jgralabtest.greql.funlib.RunFunlibTests;
import de.uni_koblenz.jgralabtest.greql.optimizer.OptimizerTest;
import de.uni_koblenz.jgralabtest.greql.parallel.ParallelTest;
import de.uni_koblenz.jgralabtest.greql.types.TypeCollectionTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TypeCollectionTest.class, ExceptionTest.class,
		RunFunlibTests.class, RunVertexEvaluatorTests.class,
		OptimizerTest.class, ThisLiteralTest.class,
		GreqlQueryFunctionTest.class, ParallelTest.class })
public class RunGreqlTests {

}
