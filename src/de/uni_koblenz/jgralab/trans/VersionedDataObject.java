package de.uni_koblenz.jgralab.trans;

import de.uni_koblenz.jgralab.impl.trans.TransactionImpl;

/**
 * This interface represents a versioned data-object of type <code>E</code>. It
 * is responsible for the (centralized) management of persistent versions of the
 * data-object. Temporary versions are managed within the corresponding
 * <code>Transaction</code>.
 * 
 * This interface is only relevant for internal use.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 *            the type of the data-object to be versioned
 */
public interface VersionedDataObject<E> {

	/**
	 * 
	 * @return the latest persistent version number
	 */
	public long getLatestPersistentVersion();

	/**
	 * Returns the persistent version with a version number <=
	 * {@link TransactionImpl#getPersistentVersionAtBot()
	 * TransactionImpl.getPersistentVersionAtBot()}
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which the corresponding
	 *            persistent version should be retrieved
	 * @return the persistent version which was valid for
	 *         <code>transaction</code> at BOT-time.
	 */
	public E getPersistentValueAtBot(Transaction transaction);

	/**
	 * 
	 * @return the latest persistent version
	 */
	public E getLatestPersistentValue();

	/**
	 * 
	 * @param dataObject
	 *            the new persistent version
	 * @param explicitChange
	 *            indicates if currentPersistentVersion should be increased
	 *            (true only for explicit changes)
	 */
	public void setNewPersistentValue(E dataObject, boolean explicitChange);

	/**
	 * Updates the value of the current persistent version.
	 * 
	 * @param dataObject
	 *            the new value for the current persistent version
	 */
	public void setPersistentValue(E dataObject);

	/**
	 * Removes all persistent values with a version number <
	 * <code>maxNumber</code> which are not referenced anymore.
	 * 
	 * @param maxVersion
	 *            the maximum version number
	 */
	public void removePersistentValues(long maxVersion);

	/**
	 * This method is needed to delete persistent values which are no more
	 * needed and whose versions are between <code>minRange</code> and
	 * <code>maxRange</code>.
	 * 
	 * @param minRange
	 * @param maxRange
	 */
	public void removePersistentValues(long minRange, long maxRange);

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which the current temporary
	 *            version number should be retrieved
	 * @return the temporary version number
	 */
	public long getTemporaryVersion(Transaction transaction);

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which the temporary version
	 *            should be retrieved
	 * @return the currently valid temporary version for
	 *         <code>transaction</code>
	 */
	public E getTemporaryValue(Transaction transaction);

	/**
	 * Updates the value of current temporary version for
	 * <code>transaction</code>
	 * 
	 * @param dataObject
	 *            the new value of the current temporary version for
	 *            <code>transaction</code>
	 * @param transaction
	 * 
	 */
	public void setTemporaryValue(E dataObject, Transaction transaction);

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code>
	 * @return if there exists a temporary version for <code>transaction</code>
	 */
	public boolean hasTemporaryValue(Transaction transaction);

	/**
	 * Removes all temporary versions with a version number >
	 * <code>minVersionNumber</code>.
	 * 
	 * @param minVersion
	 *            the minimum version number
	 * @param transaction
	 *            the <code>Transaction</code> for which the temporary versions
	 *            should be removed
	 */
	public void removeTemporaryValues(long minVersion, Transaction transaction);

	/**
	 * Removes all temporary versions for the given <code>Transaction</code>.
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which all temporary versions
	 *            should be removed.
	 */
	public void removeAllTemporaryValues(Transaction transaction);

	/**
	 * 
	 * @param transaction
	 *            the <code>Transaction</code> for which a new temporary version
	 *            should be created.
	 */
	public void createNewTemporaryValue(Transaction transaction);

	/**
	 * 
	 * @param dataObject
	 *            the object to be copied
	 * @return a deep copy of the given object
	 */
	public E copyOf(E dataObject);
	
	public boolean isCloneable();
	
	public boolean isPartOfRecord();
	
	public void setPartOfRecord(boolean isPartOfRecord);
}