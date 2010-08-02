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

package de.uni_koblenz.jgralab.greql2.exception;

import java.util.List;

import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

/**
 * Should be thrown if an undefined function should be used, that is a function
 * that is not part of the function libary
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class UndefinedFunctionException extends QuerySourceException {

	static final long serialVersionUID = -1234563;

	public UndefinedFunctionException(FunctionApplication function,
			String functionName, List<SourcePosition> sourcePositions,
			Exception cause) {
		super("Undefined Function '" + functionName + "'", function,
				sourcePositions, cause);
	}

	public UndefinedFunctionException(FunctionApplication function,
			String functionName, List<SourcePosition> sourcePositions) {
		this(function, functionName, sourcePositions, null);
	}

}
