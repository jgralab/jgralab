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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.exception;

import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;

/**
 * Should be thrown on errors in an {@link Optimizer}.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class OptimizerException extends Greql2Exception {

	private static final long serialVersionUID = 8869420302056125072L;

	public OptimizerException(String message) {
		super(message);
	}

	public OptimizerException(String message, Exception cause) {
		super(message, cause);
	}
}
