package de.uni_koblenz.jgralab.trans;

import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.trans.VersionedReferenceImpl;

/**
 * For internal use in validation.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 */
public interface VersionedIncidence {

	/**
	 * 
	 * @return
	 */
	VersionedReferenceImpl<IncidenceImpl> getVersionedNextIncidence();

	/**
	 * 
	 * @return
	 */
	VersionedReferenceImpl<IncidenceImpl> getVersionedPrevIncidence();
}
