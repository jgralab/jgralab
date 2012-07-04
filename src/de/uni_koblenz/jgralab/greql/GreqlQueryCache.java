package de.uni_koblenz.jgralab.greql;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;

public class GreqlQueryCache {

	private final HashMap<String, SoftReference<GreqlQuery>> cache = new HashMap<String, SoftReference<GreqlQuery>>();

	private Optimizer optimizer;

	private OptimizerInfo optimizerInfo;

	public GreqlQueryCache() {
		this(new DefaultOptimizer());
	}

	public GreqlQueryCache(Optimizer optimizer) {
		this(optimizer, optimizer == null ? null : OptimizerUtility
				.getDefaultOptimizerInfo());
	}

	public GreqlQueryCache(Optimizer optimizer, OptimizerInfo optimizerInfo) {
		if (optimizer != null && optimizerInfo != null) {
			this.optimizer = optimizer;
			this.optimizerInfo = optimizerInfo;
		}
	}

	public GreqlQuery getQuery(String queryText) {
		String key = queryText;
		SoftReference<GreqlQuery> ref = cache.get(key);
		if (ref != null) {
			GreqlQuery e = ref.get();
			if (e != null) {
				return e;
			} else {
				cache.remove(key);
			}
		}
		GreqlQuery query = GreqlQuery.createQuery(queryText, optimizer != null,
				optimizerInfo, optimizer);
		cache.put(key, new SoftReference<GreqlQuery>(query));
		return query;
	}

}
