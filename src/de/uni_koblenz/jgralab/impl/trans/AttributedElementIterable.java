package de.uni_koblenz.jgralab.impl.trans;

import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.Transaction;

/**
 * This class should be used as something like a "Decorator"-class (?) for
 * <code>VertexIterable</code>, <code>EdgeIterable</code> and
 * <code>IncidenceIterable</code>. Makes sure that an <code>Iterable</code>
 * -instance can only be used in the <code>Transaction</code> in which it has
 * been created.
 * 
 * @author José Monte(monte@uni-koblenz)
 * 
 * @param <A>
 *            <code>Vertex</code> or <code>Edge</code>
 */
public class AttributedElementIterable<A extends AttributedElement> implements
		Iterable<A> {
	/**
	 * The <code>Iterable</code>-instance to which the method-calls are
	 * delegated to.
	 */
	private Iterable<A> delegateIterable;
	private Graph graph;
	private Transaction transaction;

	/**
	 * 
	 * @param iterable
	 *            should be either <code>VertexIterable</code>,
	 *            <code>EdgeIterable</code> or <code>IncidenceIterable</code>
	 * @param graph
	 *            reference to graph needed to obtain reference to current
	 *            transaction in Thread.currentThread()
	 */
	public AttributedElementIterable(Iterable<A> iterable, Graph graph) {
		this.delegateIterable = iterable;
		this.graph = graph;
		transaction = graph.getCurrentTransaction();
		if (transaction == null)
			throw new GraphException("Current transaction is null.");
	}

	@Override
	public Iterator<A> iterator() {
		return new AttributedElementIterator(delegateIterable.iterator(), graph);
	}

	/**
	 * "Decorator"-class for Iterator<V> of "decorated" <code>Iterable</code>.
	 * This iterator is only valid within the transaction the corresponding
	 * <code>Iterable</code> was initialized.
	 * 
	 * @author JosÃ© Monte(monte@uni-koblenz.de)
	 * 
	 */
	private class AttributedElementIterator implements Iterator<A> {
		/**
		 * The <code>Iterator</code>-instance to which the method-calls are
		 * delegated to.
		 */
		private Iterator<A> delegateIterator;
		private Graph graph;

		/**
		 * 
		 * @param vertexIterator
		 * @param graph
		 */
		private AttributedElementIterator(Iterator<A> vertexIterator,
				Graph graph) {
			this.delegateIterator = vertexIterator;
			this.graph = graph;
		}

		@Override
		public boolean hasNext() {
			Transaction currentTransaction = graph.getCurrentTransaction();
			if (currentTransaction != transaction)
				throw new GraphException(
						"VertexIterable isn't valid within current transaction "
								+ currentTransaction + ".");
			return delegateIterator.hasNext();
		}

		@Override
		public A next() {
			Transaction currentTransaction = graph.getCurrentTransaction();
			if (currentTransaction != transaction)
				throw new GraphException(
						"VertexIterable isn't valid within current transaction "
								+ currentTransaction + ".");
			return delegateIterator.next();
		}

		@Override
		public void remove() {
			Transaction currentTransaction = graph.getCurrentTransaction();
			if (currentTransaction != transaction)
				throw new GraphException(
						"VertexIterable isn't valid within current transaction "
								+ currentTransaction + ".");
			delegateIterator.remove();
		}
	}
}
