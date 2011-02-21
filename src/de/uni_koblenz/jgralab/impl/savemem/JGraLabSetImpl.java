/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.impl.savemem;

import java.util.Collection;
import java.util.HashSet;

import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.JGraLabSet;

/**
 * FIXME This is a 1:1 clone of the code found in std.
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabSetImpl<E> extends HashSet<E> implements JGraLabSet<E> {

	private static final long serialVersionUID = 5890950480302617008L;

	public JGraLabSetImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabSetImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabSetImpl() {
		super();
	}

	public JGraLabSetImpl(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabSetImpl<E> clone() {
		JGraLabSetImpl<E> copy = new JGraLabSetImpl<E>();
		for (E element : this) {
			if (element instanceof JGraLabCloneable) {
				copy.add((E) ((JGraLabCloneable) element).clone());
			} else {
				copy.add(element);
			}
		}
		return copy;
	}
}
