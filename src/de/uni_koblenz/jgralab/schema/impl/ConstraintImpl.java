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
package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.schema.Constraint;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class ConstraintImpl implements Constraint {

	private String message;
	private String predicate;
	private String offendingElements;

	public ConstraintImpl(String msg, String pred, String offendingElems) {
		message = msg;
		predicate = pred;
		offendingElements = offendingElems;
	}

	public ConstraintImpl(String msg, String pred) {
		message = msg;
		predicate = pred;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getOffendingElementsQuery() {
		return offendingElements;
	}

	@Override
	public String getPredicate() {
		return predicate;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Constraint) {
			Constraint other = (Constraint) o;
			return this.compareTo(other) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + message.hashCode();
		hash = 31 * hash + predicate.hashCode();
		hash = 31
				* hash
				+ (null == offendingElements ? 0 : offendingElements.hashCode());
		return hash;

	}

	@Override
	public int compareTo(Constraint o) {
		int result = message.compareTo(o.getMessage());
		if (result != 0) {
			return result;
		}
		result = predicate.compareTo(o.getPredicate());
		if (result != 0) {
			return result;
		}
		return offendingElements.compareTo(o.getOffendingElementsQuery());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{Constraint: message = \"");
		sb.append(CodeGenerator.stringQuote(message));
		sb.append("\", predicate = \"");
		sb.append(CodeGenerator.stringQuote(predicate));
		sb.append("\", offendingElements = ");
		sb.append(offendingElements);
		sb.append("}");
		return sb.toString();
	}
}
