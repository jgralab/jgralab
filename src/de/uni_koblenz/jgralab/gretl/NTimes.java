/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.gretl;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.gretl.parser.TokenTypes;

/**
 * Executes the given {@link CountingTransformation}s at most N times. If an
 * earlier application doesn't succeed, it may shortcut. Returns the actual
 * number of applications.
 * 
 * @author horn
 * 
 */
public class NTimes extends CountingTransformation {

	private CountingTransformation[] transforms;
	private final int times;

	public NTimes(Context context, int times,
			CountingTransformation... transformations) {
		super(context);
		this.transforms = transformations;
		this.times = times;
	}

	public static NTimes parseAndCreate(ExecuteTransformation et) {
		List<CountingTransformation> ts = new LinkedList<>();
		int times = Integer.valueOf(et.match(TokenTypes.IDENT).value);
		while (et.tryMatchTransformationCall()) {
			CountingTransformation t = (CountingTransformation) et
					.matchTransformationCall();
			ts.add(t);
		}
		return new NTimes(et.context, times,
				ts.toArray(new CountingTransformation[ts.size()]));
	}

	@Override
	protected Integer transform() {
		int cnt = Integer.MAX_VALUE;
		int i = 0;
		for (; (i < times) && (cnt > 0); i++) {
			cnt = 0;
			for (CountingTransformation t : transforms) {
				// System.out.println(t.getClass().getSimpleName() +
				// ", iteration " + iterations);
				cnt += t.execute();
			}
		}
		return i;
	}

}
