/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.evaluator;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * EvaluatorUtilities helps {@link InternalGreqlEvaluator}s to resolve type
 * names.
 * 
 * @author ist@uni-koblenz.de
 */
public class EvaluatorUtilities {

	/**
	 * Checks whether all import declarations of the <code>root</code>
	 * {@link GreqlExpression} are valid for {@link Schema} <code>schema</code>.
	 * 
	 * @param root
	 *            a {@link GreqlExpression} vertex
	 * @param schema
	 *            a {@link Schema}
	 * 
	 * @throws UnknownTypeException
	 *             when import declarations are not valid for the
	 *             <code>schema</code>
	 */
	public static void checkImports(GreqlExpression root, Schema schema) {
		PSet<String> importedTypes = root.get_importedTypes();
		if (importedTypes == null) {
			return;
		}
		for (String importedType : importedTypes) {
			if (importedType.endsWith(".*")) {
				String packageName = importedType.substring(0,
						importedType.length() - 2);
				de.uni_koblenz.jgralab.schema.Package p = schema
						.getPackage(packageName);
				if (p == null) {
					throw new UnknownTypeException(importedType);
				}
			} else {
				AttributedElementClass<?, ?> aec = schema
						.getAttributedElementClass(importedType);
				if (aec == null) {
					throw new UnknownTypeException(importedType);
				}
			}
		}
	}

	/**
	 * Returns the {@link GraphElementClass} corresponding to
	 * <code>typeName</code> in {@link Schema} <code>schema</code>, considering
	 * import declarations at the <code>root</code> {@link GreqlException}. When
	 * <code>typeName</code> is qualified, direct schema lookup is used.
	 * Otherwise, the default package and all import statements are tried in
	 * declaration order.
	 * 
	 * @param root
	 *            a {@link GreqlExpression} vertex
	 * @param schema
	 *            a {@link Schema}
	 * @param typeName
	 *            the name of a {@link GraphElementClass}
	 * @throws UnknownTypeException
	 *             when the <code>typeName</code> can not be resolved
	 */
	public static GraphElementClass<?, ?> getGraphElementClass(
			GreqlExpression root, Schema schema, String typeName) {

		GraphElementClass<?, ?> gec = schema
				.getAttributedElementClass(typeName);
		if (typeName.contains(".")) {
			// if typeName contains a dot, it must be a qualified name
			// in this case, the schema has to know the type
			if (gec == null) {
				throw new UnknownTypeException(typeName);
			}
			return gec;
		} else if (gec != null) {
			// typeName is a simple name which was found in default package
			return gec;
		}

		// simple name not found in default package, try imported types
		PSet<String> importedTypes = root.get_importedTypes();
		if (importedTypes != null) {
			for (String importedType : importedTypes) {
				if (importedType.endsWith(".*")) {
					gec = schema
							.getAttributedElementClass(importedType.substring(
									0, importedType.length() - 1) + typeName);
				} else if (importedType.endsWith("." + typeName)) {
					gec = schema.getAttributedElementClass(importedType);
				}
				if (gec != null) {
					return gec;
				}
			}
		}
		throw new UnknownTypeException(typeName);
	}

}
