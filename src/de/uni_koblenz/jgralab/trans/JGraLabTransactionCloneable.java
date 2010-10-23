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
package de.uni_koblenz.jgralab.trans;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * This interface declares the clone()-method missing in Java-interface
 * <code>java.lang.Cloneable</code> to allow deep copying of attributes of type
 * <code>java.util.List<E></code>, <code>java.util.Set<E></code>,
 * <code>java.util.Map<K,V></code> and Record within JGraLab.
 * 
 * @author Jose Monte(monte@uni-koblenz)
 */
public interface JGraLabTransactionCloneable extends JGraLabCloneable {

	/**
	 * 
	 * @return a reference to the graph the object belongs to
	 */
	public Graph getGraph();

	public void setName(String name);
}
