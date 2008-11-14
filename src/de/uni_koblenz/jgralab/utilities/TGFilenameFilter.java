/**
 * 
 */
package de.uni_koblenz.jgralab.utilities;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A {@link FilenameFilter} that accepts TG files.
 * 
 * @author ist@uni-koblenz.de
 */
public class TGFilenameFilter implements FilenameFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (name.matches(".*\\.[Tt][Gg]$")) {
			return true;
		}
		return false;
	}
}
