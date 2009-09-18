/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

/**
 * This exception should be thrown if a query accesses a type that doesn't exist
 * in the datagraph schema
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class UnknownTypeException extends QuerySourceException {
	private static Logger logger = Logger.getLogger(UnknownTypeException.class
			.getName());
	static final long serialVersionUID = -1234560;

	public UnknownTypeException(String typeName,
			List<SourcePosition> sourcePositions, Exception cause) {
		super("The Datagraph schema doesn't contain a type ", typeName,
				sourcePositions, cause);
		for (SourcePosition sp : sourcePositions) {
			logger.severe("UnknownTypeException");
			logger.severe("  (" + sp.get_offset() + ", " + sp.get_length()
					+ ")");
		}

	}

	public UnknownTypeException(String typeName,
			List<SourcePosition> sourcePositions) {
		super("The Datagraph schema doesn't contain a type ", typeName,
				sourcePositions);
		for (SourcePosition sp : sourcePositions) {
			logger.severe("UnknownTypeException");
			logger.severe("  (" + sp.get_offset() + ", " + sp.get_length()
					+ ")");
		}
	}

}
