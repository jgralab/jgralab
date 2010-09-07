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
package de.uni_koblenz.jgralab.schema.exception;

/**
 * Thrown when a schema tries to use a reserved word as attribute name or at
 * other places.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ReservedWordException extends SchemaException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3586887297159374554L;

	public ReservedWordException(String reservedWord, String triedUsage) {
		super("The reserved word " + reservedWord + " may not be used as "
				+ triedUsage + ".");
	}
}
