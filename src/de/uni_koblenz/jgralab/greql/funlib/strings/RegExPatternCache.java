package de.uni_koblenz.jgralab.greql.funlib.strings;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegExPatternCache {
	// cache is also used by Split function
	private static HashMap<String, PatternRef> patternCache = new HashMap<String, PatternRef>();

	private static ReferenceQueue<Pattern> patternQueue = new ReferenceQueue<Pattern>();

	private static class PatternRef extends SoftReference<Pattern> {
		String regex;

		PatternRef(String regex, Pattern pat) {
			super(pat, patternQueue);
			this.regex = regex;
		}
	}

	public static Pattern get(String regex) {
		synchronized (patternCache) {
			PatternRef ref = patternCache.get(regex);
			Pattern pat = null;
			if (ref != null) {
				pat = ref.get();
			}
			if (pat == null) {
				PatternRef r = (PatternRef) patternQueue.poll();
				while (r != null) {
					patternCache.remove(r.regex);
					r = (PatternRef) patternQueue.poll();
				}
				pat = Pattern.compile(regex);
				patternCache.put(regex, new PatternRef(regex, pat));
			}
			return pat;
		}
	}
}
