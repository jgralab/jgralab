package de.uni_koblenz.jgralab.impl.diskv2.cache;

import java.lang.ref.ReferenceQueue;

import de.uni_koblenz.jgralab.impl.diskv2.EdgeImpl;
import de.uni_koblenz.jgralab.impl.diskv2.EdgeTracker;
import de.uni_koblenz.jgralab.impl.diskv2.Tracker;

final class EdgeCacheEntry extends CacheEntry<EdgeImpl> {
	EdgeCacheEntry(EdgeImpl value, ReferenceQueue<EdgeImpl> refQueue) {
		super(value, refQueue);
	}

	@Override
	protected Tracker<EdgeImpl> createTracker() {
		return new EdgeTracker();
	}
}
