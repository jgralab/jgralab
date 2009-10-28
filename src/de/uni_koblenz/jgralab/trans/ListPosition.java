package de.uni_koblenz.jgralab.trans;

/**
 * This enumeration is needed for validation. Signalizes for an element of a
 * list (Vseq, Eseq or Iseq(v) of a <code>Vertex</code> v) if the previous
 * (PREV) or next (NEXT) element has been explicitly changed within a
 * <code>Transaction</code>.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public enum ListPosition {
	PREV, NEXT;
}
