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
public class Link {

	private Atom atom;
	private Not negatedAtom;

	public Link(Atom atom, Not negatedAtom) {
		this.atom = atom;
		this.negatedAtom = negatedAtom;
	}

	public Atom getAtom() {
		return atom;
	}

	public Not getNegatedAtom() {
		return negatedAtom;
	}

	public Set<Leaf> getLeafs() {
		HashSet<Leaf> leafs = new HashSet<Leaf>();
		leafs.add(atom);
		leafs.add(negatedAtom);
		return leafs;
	}

	@Override
	public String toString() {
		return "Link(" + atom + ", " + negatedAtom + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Link) {
			Link otherLink = (Link) obj;
			return atom == otherLink.atom
					&& negatedAtom == otherLink.negatedAtom;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return atom.hashCode() + negatedAtom.hashCode();
	}

	public boolean isContainedIn(Set<Leaf> path) {
		return path.contains(atom) && path.contains(negatedAtom);
	}

	public boolean isResolutionChain(SemanticGraph sg) {
		// A single link is always a resolution chain! Cause a link is a pair of
		// c-connected leafs, the chain-subgraph is always (atom & negatedAtom)
		// and thus contains the one and only cpath [atom, negatedAtom], and
		// that clearly contains the link.

		// SemanticGraph chainSubgraph = sg.getSubgraphRelativeTo(getNodes());
		// for (Set<Node> cpath : chainSubgraph.getCPaths()) {
		// if (!isContainedIn(cpath)) {
		// return false;
		// }
		// }
		return true;
	}
}
