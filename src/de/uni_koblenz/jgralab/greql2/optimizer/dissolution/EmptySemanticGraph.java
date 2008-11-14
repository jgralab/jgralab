/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.TrivalentBoolean;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EmptySemanticGraph extends SemanticGraph {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getCPathComplement(de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph)
	 */
	@Override
	public SemanticGraph getCPathComplement(SemanticGraph subGraph) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getCPathExtension(de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph)
	 */
	@Override
	public SemanticGraph getCPathExtension(SemanticGraph subGraph) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getCPaths()
	 */
	@Override
	public Set<Set<Leaf>> getCPaths() {
		return new HashSet<Set<Leaf>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getDPaths()
	 */
	@Override
	public Set<Set<Leaf>> getDPaths() {
		return new HashSet<Set<Leaf>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getNodes()
	 */
	@Override
	public Set<Leaf> getLeafs() {
		return new HashSet<Leaf>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#getSubgraphRelativeTo(java.util.Set)
	 */
	@Override
	public SemanticGraph getSubgraphRelativeTo(Set<Leaf> leafs) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#toExpression(de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public Expression toExpression(Greql2 syntaxgraph) {
		BoolLiteral bl = syntaxgraph.createBoolLiteral();
		bl.setBoolValue(TrivalentBoolean.FALSE);
		return bl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#toNegationNormalForm()
	 */
	@Override
	public SemanticGraph toNegationNormalForm() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.dissolution.SemanticGraph#toString()
	 */
	@Override
	public String toString() {
		return "∅";
	}

	@Override
	public SemanticGraph deepCopy() {
		return new EmptySemanticGraph();
	}

	@Override
	public boolean isEqualTo(SemanticGraph sg) {
		return sg instanceof EmptySemanticGraph;
	}

	@Override
	public long getCosts() {
		return 1;
	}
}
