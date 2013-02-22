package de.uni_koblenz.jgralab.impl.diskv2.cache;

import java.lang.ref.ReferenceQueue;

import de.uni_koblenz.jgralab.impl.diskv2.Tracker;
import de.uni_koblenz.jgralab.impl.diskv2.VertexImpl;
import de.uni_koblenz.jgralab.impl.diskv2.VertexTracker;

final class VertexCacheEntry extends CacheEntry<VertexImpl> {
	VertexCacheEntry(VertexImpl value, ReferenceQueue<VertexImpl> refQueue) {
		super(value, refQueue);
	}

	@Override
	protected Tracker<VertexImpl> createTracker() {
		return new VertexTracker();
	}
}
