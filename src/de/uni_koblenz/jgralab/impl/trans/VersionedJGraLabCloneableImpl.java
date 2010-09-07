/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.impl.trans;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.trans.JGraLabTransactionCloneable;

/**
 * This class is responsible for the versioning of cloneable classes in the
 * context of JGraLab. These can be attributes of type JGraLabList, JGraLabSet,
 * JGraLabMap and Record-classes.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 *            the type
 */
public class VersionedJGraLabCloneableImpl<E extends JGraLabTransactionCloneable>
		extends VersionedDataObjectImpl<E> {

	/**
	 * Should be used for attributes.
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 * @param name
	 *            the name of the attribute
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			E initialPersistentValue, String name) {
		super(attributedElement, initialPersistentValue, name);
	}

	/**
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			E initialPersistentValue) {
		super(attributedElement, initialPersistentValue);
	}

	/**
	 * 
	 * @param graph
	 * @param name
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			String name) {
		super(attributedElement, name);
	}

	/**
	 * 
	 * @param graph
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement) {
		super(attributedElement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E copyOf(E dataObject) {
		if (dataObject == null) {
			return null;
		}
		return (E) dataObject.clone();
	}

	@Override
	public boolean isCloneable() {
		return true;
	}
}
