package de.uni_koblenz.jgralab.impl.diskv2.cache;

import de.uni_koblenz.jgralab.impl.diskv2.DiskStorageManager;
import de.uni_koblenz.jgralab.impl.diskv2.EdgeImpl;
import de.uni_koblenz.jgralab.impl.diskv2.Tracker;

public final class EdgeCache extends ElementCache<EdgeImpl> {

	public EdgeCache(DiskStorageManager dsm) {
		super(dsm);
	}

	@Override
	protected CacheEntry<EdgeImpl> createEntry(EdgeImpl element) {
		return new EdgeCacheEntry(element, refQueue);
	}

	@Override
	protected EdgeImpl readElementFromDisk(int id) {
		return dsm.readEdgeFromDisk(id);
	}

	@Override
	protected void writeElementToDisk(int id, Tracker<EdgeImpl> tracker) {
		dsm.writeEdgeToDisk(id, tracker);
	}

}
