/**
 * 
 */
package de.uni_koblenz.jgralabtest.greql2.evaluator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;

/**
 * Test for all {@link VertexEvaluator}s. Used {@link Graph} is the
 * greqltestgraph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ LiteralEvaluatorTest.class, CollectionEvaluatorTest.class })
public class VertexEvaluatorTest {

}
