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
package de.uni_koblenz.jgralab.schema.exception;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Thrown when trying to add an attribute xxx to an
 * {@link AttributedElementClass} which already contains an attribute with name
 * xxx.
 *
 * @author ist@uni-koblenz.de
 *
 */
public class DuplicateAttributeException extends SchemaException {

	private static final long serialVersionUID = 8996065398207556377L;

	public DuplicateAttributeException(String attrName, String attrElemClassName) {
		super("Duplicate attribute " + attrName + " in AttributedElementClass "
				+ attrElemClassName);
	}

	public DuplicateAttributeException(String message) {
		super(message);
	}
}
