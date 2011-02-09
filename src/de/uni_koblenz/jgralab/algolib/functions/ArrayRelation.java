/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.algolib.functions;

import java.util.Arrays;

public class ArrayRelation<DOMAIN> implements Relation<DOMAIN, DOMAIN> {

	private boolean[][] values;
	private IntFunction<DOMAIN> indexMapping;

	public ArrayRelation(boolean[][] values, IntFunction<DOMAIN> indexMapping) {
		this.values = values;
		this.indexMapping = indexMapping;
	}

	public boolean[][] getValues() {
		return values;
	}

	@Override
	public boolean get(DOMAIN parameter1, DOMAIN parameter2) {
		return values[indexMapping.get(parameter1)][indexMapping
				.get(parameter2)];
	}

	@Override
	public boolean isDefined(DOMAIN parameter1, DOMAIN parameter2) {
		return indexMapping.isDefined(parameter1)
				&& indexMapping.isDefined(parameter2);
	}

	@Override
	public void set(DOMAIN parameter1, DOMAIN parameter2, boolean value) {
		throw new UnsupportedOperationException("This relation is immutable.");
	}
	
	public String toString(){
		StringBuilder out = new StringBuilder();
		for(int i = 1; i < values.length; i++){
			out.append(Arrays.toString(values[i]));
			out.append('\n');
		}
		return out.toString();
	}

}
