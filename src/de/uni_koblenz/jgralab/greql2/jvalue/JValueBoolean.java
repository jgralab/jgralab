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

package de.uni_koblenz.jgralab.greql2.jvalue;

import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;

/**
 * This enumerationclass represents a trivalent boolean value. It may hold the
 * boolean values <code>true</code> and <code>false</code> and a third value,
 * called <code>null</code> If true is 1 and false is 0, then null is 0.5. The
 * boolean operations "and", "or", "xor" etc. are defined in the same way like
 * for boolean values. Below, the tables for the operations "AND", "OR" and
 * "NOT" are listed
 * 
 * AND | true | null | false ------------------------------- true | true | null
 * | false ------------------------------- null | null | null | false
 * ------------------------------ false | false | false | false
 * 
 * 
 * OR | true | null | false ------------------------------- true | true | true |
 * true ------------------------------- null | true | null | null
 * ------------------------------ false | true | null | false
 * 
 * 
 * VALUE | NOT VALUE ----------------- true | false ----------------- null |
 * null ----------------- false | false
 * 
 * 
 * These primary operations are implemented as static methods of this class, so
 * one can use it easily. Also, there is a conversion from boolean to
 * TrivalentBoolean
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class JValueBoolean {

	public static final JValue trueJValue = new JValueImpl(true);

	public static final JValue falseJValue = new JValueImpl(false);

	/**
	 * implements the boolean operation "AND" for the type TrivalentBoolean
	 * 
	 * @param first
	 *            The first operand
	 * @param second
	 *            The second operand
	 * @return the result of first AND second
	 */
	public static JValue and(JValue first, JValue second)
			throws JValueInvalidTypeException {
		Boolean b1 = first.toBoolean();
		Boolean b2 = second.toBoolean();

		if (b1 && b2) {
			return trueJValue;
		}
		return falseJValue;
	}

	/**
	 * implements the boolean operation "OR" for the type TrivalentBoolean
	 * 
	 * @param first
	 *            The first operand
	 * @param second
	 *            The second operand
	 * @return the result of first OR second
	 */
	// TODO
	public static JValue or(JValue first, JValue second)
			throws JValueInvalidTypeException {
		Boolean b1 = first.toBoolean();
		Boolean b2 = second.toBoolean();

		if (b1 || b2) {
			return trueJValue;
		}
		return falseJValue;
	}

	/**
	 * implements the boolean operation "NOT" for the type TrivalentBoolean
	 * 
	 * @param first
	 *            The first operand
	 * @return the result of NOT first
	 */
	public static JValue not(JValue first) throws JValueInvalidTypeException {
		Boolean firstBoolean = first.toBoolean();

		if (!firstBoolean.booleanValue()) {
			return trueJValue;
		}
		return falseJValue;
	}

	/**
	 * implements the boolean operation "XOR" for the type TrivalentBoolean
	 * 
	 * @param first
	 *            The first operand
	 * @param second
	 *            The second operand
	 * @return the result of first XOR second
	 */
	public static JValue xor(JValue first, JValue second)
			throws JValueInvalidTypeException {
		Boolean firstBoolean = first.toBoolean();
		Boolean secondBoolean = second.toBoolean();

		boolean value = !firstBoolean.equals(secondBoolean);
		if (value) {
			return trueJValue;
		}
		return falseJValue;
	}

	public static Boolean getTrueValue() {
		return Boolean.TRUE;
	}

	public static Boolean getFalseValue() {
		return Boolean.FALSE;
	}

	public static JValue getValue(boolean value) {
		if (value) {
			return trueJValue;
		}
		return falseJValue;
	}

}
