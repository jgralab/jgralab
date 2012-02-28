/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql2.funlib.strings;

import java.util.List;
import java.util.regex.Pattern;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.funlib.Function;

public class Split extends Function {
	public Split() {
		super(
				"Splits the given string according to the given regular expression and returns the parts as list.",
				10, 3, 0.1, Category.STRINGS);
	}

	public List<String> evaluate(String s, String regex) {
		Pattern pat = ReMatch.patternCache.get(regex);
		if (pat == null) {
			pat = Pattern.compile(regex);
			ReMatch.patternCache.put(regex, pat);
		}
		String[] parts = pat.split(s);
		PVector<String> result = JGraLab.vector();
		for (String part : parts) {
			result = result.plus(part);
		}
		return result;
	}
}
