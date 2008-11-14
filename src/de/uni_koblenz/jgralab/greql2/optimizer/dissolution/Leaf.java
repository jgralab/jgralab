/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Leaf extends SemanticGraph {

	@Override
	public Set<Leaf> getLeafs() {
		HashSet<Leaf> leafs = new HashSet<Leaf>();
		leafs.add(this);
		return leafs;
	}

	@Override
	public SemanticGraph getSubgraphRelativeTo(Set<Leaf> leafs) {
		if (leafs.contains(this))
			return this;
		return new EmptySemanticGraph();
	}

	@Override
	public SemanticGraph getCPathExtension(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph)
			return new EmptySemanticGraph();

		return this;
	}

	@Override
	public SemanticGraph getCPathComplement(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph)
			return this;

		return new EmptySemanticGraph();
	}

	@Override
	public Set<Set<Leaf>> getCPaths() {
		Set<Leaf> cpath = new HashSet<Leaf>(1, 1);
		cpath.add(this);
		Set<Set<Leaf>> myCPaths = new HashSet<Set<Leaf>>(1, 1);
		myCPaths.add(cpath);
		return myCPaths;
	}

	@Override
	public Set<Set<Leaf>> getDPaths() {
		Set<Leaf> dpath = new HashSet<Leaf>(1, 1);
		dpath.add(this);
		Set<Set<Leaf>> myDPaths = new HashSet<Set<Leaf>>(1, 1);
		myDPaths.add(dpath);
		return myDPaths;
	}
}
