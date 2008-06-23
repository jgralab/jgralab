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

/**
 * Should be thrown if there is a Vertex in the GReQL Syntaxgraph for which no
 * VertexEvaluator exists
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class UnknownVertexException extends EvaluateException {

	static final long serialVersionUID = -1234565;

	public UnknownVertexException(String vertexName, Exception cause) {
		super(
				"Vertex "
						+ vertexName
						+ " unknown, cannot create a VertexEvaluator for a unknown syntaxgraph vertex",
				cause);
	}

	public UnknownVertexException(String vertexName) {
		super(
				"Vertex "
						+ vertexName
						+ " unknown, cannot create a VertexEvaluator for a unknown syntaxgraph vertex");
	}

}
