/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.greql2.jvalue;

/**
 * This enumerationclass represents a trivalent boolean value. It may hold the
 * boolean values <code>true</code> and <code>false</code> and a third value, called <code>null</code>
 * If true is 1 and false is 0, then null is 0.5. The boolean operations "and", "or", "xor" etc. are defined 
 * in the same way like for boolean values. Below, the tables for the operations "AND", "OR" and "NOT" are listed    
 * 
 *   AND   | true  | null  | false
 *   -------------------------------
 *   true  | true  | null  | false
 *   -------------------------------
 *   null  | null  | null  | false
 *   ------------------------------
 *   false | false | false | false 
 *   
 *   
 *    OR   | true  | null  | false
 *   -------------------------------
 *   true  | true  | true  | true
 *   -------------------------------
 *   null  | true  | null  | null
 *   ------------------------------
 *   false | true  | null  | false 
 *   
 *   
 *   VALUE | NOT VALUE
 *   -----------------
 *   true  | false
 *   -----------------
 *   null  | null
 *   -----------------
 *   false | false
 *   
 * 
 *   These primary operations are implemented as static methods of this class, so one can use it easily. 
 *   Also, there is a conversion from boolean to TrivalentBoolean
 *   
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */
public class JValueBoolean {

		public static final Boolean NULL = null; 

		/**
		 * implements the boolean operation "AND" for the type TrivalentBoolean
		 * @param first The first operand
		 * @param second The second operand
		 * @return the result of  first AND second 
		 */
		public static JValue and(JValue first, JValue second) throws JValueInvalidTypeException {
			Boolean firstBoolean = first.toBoolean();
			Boolean secondBoolean = second.toBoolean();
			if ((firstBoolean == Boolean.TRUE) && (secondBoolean == Boolean.TRUE))
				return new JValue(Boolean.TRUE);
			if ((firstBoolean == Boolean.FALSE) || (secondBoolean == Boolean.FALSE))			
				return new JValue(Boolean.FALSE);
			return new JValue(NULL);	
		}
		
		
		/**
		 * implements the boolean operation "OR" for the type TrivalentBoolean
		 * @param first The first operand
		 * @param second The second operand
		 * @return the result of  first OR second 
		 */
		public static JValue or(JValue first, JValue second) throws JValueInvalidTypeException {
			Boolean firstBoolean = first.toBoolean();
			Boolean secondBoolean = second.toBoolean();
			if ((firstBoolean == Boolean.TRUE) || (secondBoolean == Boolean.TRUE))
				return new JValue(Boolean.TRUE);
			if ((firstBoolean == Boolean.FALSE) && (secondBoolean == Boolean.FALSE))		
				return new JValue(Boolean.FALSE);
			return new JValue(NULL);
		}
		
		/**
		 * implements the boolean operation "NOT" for the type TrivalentBoolean
		 * @param first The first operand
		 * @return the result of NOT first
		 */
		public static JValue not(JValue first) throws JValueInvalidTypeException {
			Boolean firstBoolean = first.toBoolean();
			if (firstBoolean == Boolean.TRUE)
				return new JValue(Boolean.FALSE);
			if (firstBoolean == Boolean.FALSE)
				return new JValue(Boolean.TRUE);
			return new JValue(NULL);
		}
		
		/**
		 * implements the boolean operation "XOR" for the type TrivalentBoolean
		 * @param first The first operand
		 * @param second The second operand
		 * @return the result of  first XOR second 
		 */
		public static JValue xor(JValue first, JValue second) throws JValueInvalidTypeException {
			Boolean firstBoolean = first.toBoolean();
			Boolean secondBoolean = second.toBoolean();
			if ((firstBoolean == NULL) || (secondBoolean == NULL))
				return new JValue(NULL);
			if ((firstBoolean == Boolean.FALSE) && (secondBoolean == Boolean.TRUE))				
				return new JValue(Boolean.TRUE);
			if ((firstBoolean == Boolean.TRUE) && (secondBoolean == Boolean.FALSE))				
				return new JValue(Boolean.TRUE);
			return new JValue(Boolean.FALSE);
		}
		
		
		public static Boolean getTrueValue() {
			return Boolean.TRUE;
		}
		
		public static Boolean getFalseValue() {
			return Boolean.FALSE;
		}
		
		public static Boolean getNullValue() {
			return NULL;
		}
		
		
}
