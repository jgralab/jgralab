package de.uni_koblenz.jgralab.impl.diskv2;

import java.lang.ref.ReferenceQueue;
import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class realizes the caching of vertices, edges and incidences 
 * in memory in a distributed environment. All methods may be used
 * only with local objects and local ids.
 * 
 * @author dbildh, aheld
 * 
 */
public final class MemStorageManager {
	
	/**
	 * maximum load factor of the caches. If load factor is exceeded, do a rehash
	 */
	private static final double MAX_LOAD_FACTOR = 0.7;
	
	/**
	 * the disk storage manager
	 */
	private DiskStorageManager diskStorage;
	
	/**
	 * maximum number of entries in the vertex, edge and incidence caches
	 * if these are exceeded by the amount of entries of the respective cache,
	 * the load factor of the respective cache has been exceeded
	 */ 
	private int vertexCacheMaxEntries;
	private int edgeCacheMaxEntries;
	
	/**
	 * log(2) of the cache sizes
	 */
	private int vertexCacheExp;
	private int edgeCacheExp;
	
	/**
	 * these are used to efficiently compute the buckets
	 * with a logical and, instead of with modulo arithmetics
	 */
	private int vertexMask;
	private int edgeMask;
	
	/**
	 * number of objects in the caches
	 */
	private int vertexCacheEntries;
	private int edgeCacheEntries;

	/**
	 * in-memory-cache for vertices
	 */
	private volatile CacheEntry<VertexImpl>[] vertexCache;

	/**
	 * in-memory-cache for edges
	 */
	private volatile CacheEntry<EdgeImpl>[] edgeCache;


	/**
	 * The queue in which references to Incidence objects are put after
	 * the garbage collector automatically deletes the referenced 
	 * Incidence object
	 */
	private ReferenceQueue<VertexImpl> vertexQueue 
		= new ReferenceQueue<VertexImpl>();
	
	private ReferenceQueue<EdgeImpl> edgeQueue 
		= new ReferenceQueue<EdgeImpl>();
	
	public MemStorageManager(GraphImpl database) {
		diskStorage = new DiskStorageManager(database);
		
		//set log(2) of the cache sizes
		vertexCacheExp = 21;
		edgeCacheExp = 23;
		
		//calculate default cache sizes and max load factors
		int vertexCacheSize = (int) Math.pow(2, vertexCacheExp);
		vertexCacheMaxEntries = (int) (vertexCacheSize * MAX_LOAD_FACTOR);
		int edgeCacheSize = (int) Math.pow(2, edgeCacheExp);
		edgeCacheMaxEntries = (int) (edgeCacheSize * MAX_LOAD_FACTOR);
		
		//instantiate caches with calculated sizes
		vertexCache = new CacheEntry[vertexCacheSize];
		edgeCache = new CacheEntry[edgeCacheSize];
		
		vertexMask = (int) (Math.pow(2, vertexCacheExp)) - 1;
		edgeMask = (int) (Math.pow(2, edgeCacheExp)) - 1;
		
		//initialize element counts
		vertexCacheEntries = 0;
		edgeCacheEntries = 0;
	}

	//---- Methods to put, get and remove Graph elements and incidences from the cache ----

	/**
	 * Retrieves a Vertex from the vertex cache
	 * 
	 * @param id the id of the Vertex to be retrieved
	 * @return the Vertex with the given id
	 */
	public synchronized final Vertex getVertexObject(int id) {
		cleanupVertexCache();
		
		CacheEntry<VertexImpl> entry = getElement(vertexCache, id, hash(id, vertexMask));
		
		if (entry == null){
			return getVertexObjectFromDisk(id);
		}
		
		VertexImpl v = entry.get();
		
		if(v == null){
			diskStorage.writeVertexToDisk(entry);
			removeVertex(entry.getKey());
			v = (VertexImpl) diskStorage.readVertexFromDisk(entry.getKey());
			CacheEntry<VertexImpl> vRef = new CacheEntry<VertexImpl>(v, vertexQueue);
			putElement(vRef, vertexCache, hash(id, vertexMask));
		}
		
		return v;
	}
	
	/**
	 * Read a vertex from the disk
	 * 
	 * @param id
	 * 		The id of the vertex
	 * 
	 * @return
	 * 		The vertex that was reconstructed with data from the disk
	 */
	private Vertex getVertexObjectFromDisk(int id){
		System.err.println("READ VERTEX FROM DISK: " + id);
		cleanupVertexCache();
		VertexImpl v = diskStorage.readVertexFromDisk(id);
		CacheEntry<VertexImpl> vRef = new CacheEntry<VertexImpl>(v, vertexQueue);
		putElement(vRef, vertexCache, hash(id, vertexMask));
		//System.err.println(" -- next vertex ID" + v.getNextVertexId());
		//System.err.println(" -- prev vertex ID" + v.getPrevVertexId());
		return vRef.get();
	}

	/**
	 * Retrieves an Edge from the edge cache
	 * 
	 * @param id the id of the Edge to be retrieved
	 * @return the Edge with the given id
	 */
	public synchronized final Edge getEdgeObject(int id) {
		cleanupEdgeCache();
		
		
		int eId = id > 0 ? id : - id;
		CacheEntry<EdgeImpl> entry = getElement(edgeCache, eId, hash(eId, edgeMask));
		
		if (entry == null){
			Edge e = getEdgeObjectFromDisk(eId);
			return id > 0 ? e : e.getReversedEdge();
		}
		
		EdgeImpl e = entry.get();
		
		if(e == null){
			diskStorage.writeEdgeToDisk(entry);
			removeEdge(entry.getKey());
			e = (EdgeImpl) diskStorage.readEdgeFromDisk(entry.getKey());
			CacheEntry<EdgeImpl> eRef = new CacheEntry<EdgeImpl>(e, edgeQueue);
			putElement(eRef, edgeCache, hash(eId, edgeMask));
		}else{
			if(id<0)
				return e.getReversedEdge();
		}
		
		return e;
	}
	
	/**
	 * Read an Edge from the disk
	 * 
	 * @param id
	 * 		The id of the edge
	 * 
	 * @return
	 * 		The edge that was reconstructed with data from the disk
	 */
	private Edge getEdgeObjectFromDisk(int id){
		//System.err.println("READ EDGE FROM DISK: " + id);

		EdgeImpl e = diskStorage.readEdgeFromDisk(id);
		CacheEntry<EdgeImpl> eRef = new CacheEntry<EdgeImpl>(e, edgeQueue);
		putElement(eRef, edgeCache, hash(id, edgeMask));
		return eRef.get();
	}
	
	/**
	 * Fetches an CacheEntry specified by the given id from a cache.
	 * 
	 * @param cache - the cache to fetch an entry from
	 * @param key - the key of the requested entry
	 * @param bucket - the bucket the element is in
	 * @return the CacheEntry with the given key
	 */
	private <V> CacheEntry<V> getElement(CacheEntry<V>[] cache, int key, int bucket){
		//retrieve first entry in the bucket
		CacheEntry<V> current = cache[bucket];
				
		//search bucket for the requested entry
		while (current != null && !current.hasKey(key)){
			current = current.getNext();
		}

		return current;
	}

	/**
	 * Puts a Vertex in the cache
	 * 
	 * @param v the Vertex to be cached
	 */
	public synchronized void putVertex(VertexImpl v) {
		CacheEntry<VertexImpl> vEntry = new CacheEntry<VertexImpl>(v, vertexQueue);
		putElement(vEntry, vertexCache, hash(v.getId(), vertexMask));
		
		vEntry.getOrCreateTracker(v).fill(v);
		
		vertexCacheEntries++;
		testVertexLoadFactor();
	}
	
	/**
	 * Puts an Edge in the cache
	 * 
	 * @param e the Edge to be cached
	 */
	public synchronized void putEdge(EdgeImpl e) {
		CacheEntry<EdgeImpl> eEntry = new CacheEntry<EdgeImpl>(e, edgeQueue);
		putElement(eEntry, edgeCache, hash(e.getId(), edgeMask));
		
		eEntry.getOrCreateTracker(e).fill(e);
		
		edgeCacheEntries++;
		testEdgeLoadFactor();
	}
	
	
	/**
	 * Puts a CacheEntry into a cache
	 * 
	 * @param entry - the entry to be cached
	 * @param cache - the cache to store the entry in
	 * @param bucket - the bucket in which to store the entry
	 */
	private <V> void putElement(CacheEntry<V> entry, CacheEntry<V>[] cache, int bucket){
		//case 1: no collision - put entry in bucket and return
		if (cache[bucket] == null) {
			cache[bucket] = entry;
			return;
		}
					
		//case 2: collision detected
		//put new element at the start of the list
		entry.setNext(cache[bucket]);
		cache[bucket] = entry;
	}

	/**
	 * Removes a vertex from the cache
	 * 
	 * @param vertexId the id of the vertex to be deleted
	 */
	public void removeVertex(int vertexId) {		
		removeElement(vertexCache, vertexId, hash(vertexId, vertexMask));
		
		vertexCacheEntries--;
	}
	
	/**
	 * Removes an edge from the cache
	 * 
	 * @param edgeId the id of the edge to be deleted
	 */
	public void removeEdge(int edgeId) {
		removeElement(edgeCache, edgeId, hash(edgeId, edgeMask));
		
		edgeCacheEntries--;
	}

	/**
	 * Removes a CacheEntry from a cache
	 * 
	 * @param cache - the cache the entry is deleted from 
	 * @param key - the key of the entry
	 * @param bucket - the bucket the entry is in
	 */
	private <V> void removeElement(CacheEntry<V>[] cache, int key, int bucket){
		//retrieve first entry in bucket
		CacheEntry<V> current = cache[bucket];
		//keep track of predecessor because we only have a singly linked list
		CacheEntry<V> predecessor = null;
		boolean isFirstEntry = true;
				
		//look for entry to be deleted
		while(!current.hasKey(key)){
			isFirstEntry = false;
			predecessor = current;
			current = current.getNext();
		}
		
		//delete entry from the cache
		if (isFirstEntry){
			//case 1: entry is the first entry in the chain, or the only entry in the bucket
			if (current.getNext() == null){
				//case 1a: entry is the only entry in the bucket
				cache[bucket] = null;	
			} 
			else {
				//case 1b: entry is the first entry in the chain
				//put its successor as the first element in that bucket
				cache[bucket] = current.getNext();				
			}
		}
		
		else {
			//case 2: entry is neither the only entry in the bucket nor the first entry in the chain
			//set "next" pointer of the entry's predecessor to point at its successor
			predecessor.setNext(current.getNext());
		}
	}
	
	/**
	 * Get a specific GraphElementTracker
	 * 
	 * @param vertexId
	 * 		The id of the vertex whose tracker is requested
	 * 
	 * @return
	 * 		The tracker for the given vertex
	 */
	public VertexTracker getVertexTracker(int vertexId){
		CacheEntry<VertexImpl> vEntry = getElement
				(vertexCache, vertexId, hash(vertexId, vertexMask));
		if (vEntry == null) return null;
		return (VertexTracker) vEntry.getOrCreateTracker(vEntry.get());
	}
	
	/**
	 * Get a specific GraphElementTracker
	 * 
	 * @param edgeId
	 * 		The id of the edge whose tracker is requested
	 * 
	 * @return
	 * 		The tracker for the given edge
	 */
	public EdgeTracker getEdgeTracker(int edgeId){
		CacheEntry<EdgeImpl> eEntry = getElement
				(edgeCache, edgeId, hash(edgeId, edgeMask));
		if (eEntry == null) return null;
		return (EdgeTracker) eEntry.getOrCreateTracker(eEntry.get());
	}
	
	
	// ---- Methods to manage the cache ----
	
	/**
	 * Checks if any vertex objects have been deleted by the garbage collector.
	 * If so, the states of these vertices are written to the disk (only if
	 * the vertex hasn't been written to the disk before or if its state
	 * was changed after the last time it has been loaded from the disk) and
	 * the CacheEntry that referenced the deleted object is removed from the cache.
	 */
	private void cleanupVertexCache(){
		CacheEntry<VertexImpl> current = (CacheEntry<VertexImpl>) vertexQueue.poll();
		
		while(current != null){
			diskStorage.writeVertexToDisk(current);
			removeVertex(current.getKey());
			current = (CacheEntry<VertexImpl>) vertexQueue.poll();
		}
	}
	
	/**
	 * Checks if any edge objects have been deleted by the garbage collector.
	 * If so, the states of these edges are written to the disk (only if
	 * the edge hasn't been written to the disk before or if ist state
	 * was changed after the last time it has been loaded from the disk) and
	 * the CacheEntry that referenced the deleted object is removed from the cache.
	 */
	private void cleanupEdgeCache(){
		CacheEntry<EdgeImpl> current = (CacheEntry<EdgeImpl>) edgeQueue.poll();
		
		while(current != null){
			diskStorage.writeEdgeToDisk(current);
			removeEdge(current.getKey());
			current = (CacheEntry<EdgeImpl>) edgeQueue.poll();
		}
	}
	
	
	/**
	 * Tests the load factor of the vertex cache an does a rehashing if it exceeds 
	 * MAX_LOAD_FACTOR.
	 */
	protected void testVertexLoadFactor(){		
		if (vertexCacheEntries > vertexCacheMaxEntries){
			rehashVertexCache();
		}
	}
	
	/**
	 * Tests the load factor of the edge cache an does a rehashing if it exceeds 
	 * MAX_LOAD_FACTOR.
	 */
	protected void testEdgeLoadFactor(){		
		if (edgeCacheEntries > edgeCacheMaxEntries){
			rehashEdgeCache();
		}
	}

	
	/**
	 * Doubles the size of the vertex cache and rehashes all vertices
	 */
	protected void rehashVertexCache(){
		//max allowed cache size is 2^31 because Multiplication Method requires that
		//log(2) of cache size is smaller then the number of bits in an Integer
		if (vertexCacheExp >= 31){
			//remove size limit so we don't end up here after every put
			vertexCacheMaxEntries = Integer.MAX_VALUE;
			return;
		}
		
		//adjust variables that hold information about the cache
		vertexCacheExp++;
		int vertexCacheSize = (int) Math.pow(2, vertexCacheExp);
		vertexCacheMaxEntries *= 2;
		vertexMask *= 2;
		
	    CacheEntry<VertexImpl>[] newCache = new CacheEntry[vertexCacheSize];
	    
	    vertexCache = moveCachedObjects(vertexCache, newCache, vertexMask);
	}
	
	/**
	 * Doubles the size of the vertex cache and rehashes all vertices
	 */
	protected void rehashEdgeCache(){
		if (edgeCacheExp >= 31){
			edgeCacheMaxEntries = Integer.MAX_VALUE;
			return;
		}
		
		edgeCacheExp++;
		int edgeCacheSize = (int) Math.pow(2, edgeCacheExp);
		edgeCacheMaxEntries *= 2;
		edgeMask *= 2;
		
	    CacheEntry<EdgeImpl>[] newCache = new CacheEntry[edgeCacheSize];
	    
	    edgeCache = moveCachedObjects(edgeCache, newCache, edgeMask);
	}
	
	
	/**
	 * Rehashes all CacheEntries in the old cache by moving them to the new cache
	 *  
	 * @param oldCache - the old cache
	 * @param newCache - the new, bigger cache
	 * @param mask - the mask corresponding to the new cache's size
	 * @return the new cache
	 */
    private <V> CacheEntry<V>[] moveCachedObjects(CacheEntry<V>[] oldCache, CacheEntry<V>[] newCache, int mask){
    	
    	Stack<CacheEntry<V>> oldEntries = new Stack<CacheEntry<V>>();
    	
    	for (int i = 0; i < oldCache.length; i++){
	    	if (oldCache[i] != null){
	    		
	    		//Put vertices in current bucket on a stack
	    		CacheEntry<V> current = oldCache[i];
	    		oldEntries.push(current);
	    		
	    		while (current.getNext() != null){
	    			current = current.getNext();
	    			oldEntries.push(current);
	    		}
	    		
	    		//rehash all the vertices on the stack
	    		while(!oldEntries.empty()){
	    			current = oldEntries.pop();
	    			//erase pointer, because it is now invalid
	    			current.setNext(null);
	    			putElement(current, newCache, hash(current.getKey(), mask));
	    		}
	    	}
	    }
    	
    	return newCache;
    }
	
	/**
	 * Calculates the hash value for an object using Cormen's Multiplication Method
	 * 
	 * @param hashCode - the hashCode of the object to be cached
	 * @param mask - used to compute (hashCode modulo cacheSize)
	 */
	protected int hash(int hashCode, int mask){
		return hashCode & mask;
	}
	
}