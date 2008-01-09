/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.greql2.exception;

import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

import java.util.List;

/**
 * Should be thrown if the user tries to access a field of a type which doesn't exists for this type, e.g.
 * if the user tries to access vertex.color, but vertex has no attribute color  
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */
public class FunctionInvalidIndexException extends QuerySourceException {

	static final long serialVersionUID = -1234560;


	public FunctionInvalidIndexException(String className, int index, List<SourcePosition> sourcePositions ) {
		super("Index '" + index + "' out of bounds", className	, sourcePositions);
	}
	
}
