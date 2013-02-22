package de.uni_koblenz.jgralab.impl.diskv2.cache;

import de.uni_koblenz.jgralab.impl.diskv2.DiskStorageManager;
import de.uni_koblenz.jgralab.impl.diskv2.Tracker;
import de.uni_koblenz.jgralab.impl.diskv2.VertexImpl;

public final class VertexCache extends ElementCache<VertexImpl> {

	public VertexCache(DiskStorageManager dsm) {
		super(dsm);
	}

	@Override
	protected CacheEntry<VertexImpl> createEntry(VertexImpl element) {
		return new VertexCacheEntry(element, refQueue);
	}

	@Override
	protected VertexImpl readElementFromDisk(int id) {
		return dsm.readVertexFromDisk(id);
	}

	@Override
	protected void writeElementToDisk(int id, Tracker<VertexImpl> tracker) {
		dsm.writeVertexToDisk(id, tracker);
	}
}
