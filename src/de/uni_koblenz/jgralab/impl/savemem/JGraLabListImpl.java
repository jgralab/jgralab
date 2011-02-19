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

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * FIXME This is a 1:1 clone of the code found in std.
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabListImpl<E> extends ArrayList<E> implements
		de.uni_koblenz.jgralab.JGraLabList<E> {

	/**
	 * The generated id for serialization.
	 */
	private static final long serialVersionUID = -3622764334130460297L;

	public JGraLabListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabListImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabListImpl() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabListImpl<E> clone() {
		JGraLabListImpl<E> copy = new JGraLabListImpl<E>();
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
