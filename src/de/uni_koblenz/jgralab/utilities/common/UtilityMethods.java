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

package de.uni_koblenz.jgralab.utilities.common;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class UtilityMethods {

	/**
	 * Generates a UnexpectedError message, which includes a filename, a line
	 * number and a message. Line number and message are optional. If you don't
	 * want to declare a line number use a negative number. For no message use
	 * 'null'.
	 * 
	 * @param file
	 *            Filename of the current processed file. A null reference will
	 *            throw a NullPointerException.
	 * @param lineNumber
	 *            Line number, at which processing stopped. A value less then
	 *            zero results an error message without mentioning the line
	 *            number.
	 * @param message
	 *            Message, which should be added at the end. A null reference
	 *            will be handled like an empty message.
	 * @return UnexpectedError message
	 */
	public static String generateUnexpectedErrorMessage(String file,
			int lineNumber, String message) {

		StringBuilder sb = new StringBuilder();

		sb.append("Unexpected error occured in file '");
		sb.append(file);
		sb.append("'");

		if (lineNumber >= 0) {
			sb.append(" at line ");
			sb.append(lineNumber);
		}

		sb.append(".");
		if (message != null) {
			sb.append("\n");
			sb.append(message);
		}

		return sb.toString();
	}

	/**
	 * Generates a URI from a qualified Name for using in XML-files. It replaces
	 * all occurrences of "_" with "-", swaps the first and the second element
	 * of the qualified name and appends the remainder as folder structure.
	 * Example: de.uni_koblenz.jgralab.greql2 => uni-koblenz.de/jgralab/greql2
	 * 
	 * @param qualifiedName
	 *            the qualified name to convert.
	 * @return a URI as String according to the given qualified Name.
	 */
	public static String generateURI(String qualifiedName) {
		qualifiedName = qualifiedName.replace('_', '-');
		String[] uri = qualifiedName.split("\\.");

		StringBuilder namespaceURI = new StringBuilder();
		namespaceURI.append("http://");
		if (uri.length > 1) {
			namespaceURI.append(uri[1]).append(".").append(uri[0]);

			for (int i = 2; i < uri.length; i++) {
				namespaceURI.append("/").append(uri[i]);
			}

		}

		return namespaceURI.toString();
	}

	public static void sortIncidenceList(Vertex v, Comparator<Edge> cmp) {
		if (v.getDegree() > 0) {

			Edge currentSorted = null;

			// create copy of incidenceList
			List<Edge> incidenceList = new LinkedList<Edge>();
			for (Edge currentEdge : v.incidences()) {
				incidenceList.add(currentEdge);
			}

			// selection sort
			// maybe TODO change it to mergesort
			while (!incidenceList.isEmpty()) {
				// select minimum
				Edge currentMinimum = incidenceList.get(0);
				for (Edge currentEdge : incidenceList) {
					if (cmp.compare(currentEdge, currentMinimum) < 0) {
						currentMinimum = currentEdge;
					}
				}

				// place edge where it belongs
				if (currentSorted == null) {
					if (currentMinimum != v.getFirstEdge()) {
						// only put it there if the first edge is not already in
						// place
						currentMinimum.putEdgeBefore(v.getFirstEdge());
					}
				} else {
					currentMinimum.putEdgeAfter(currentSorted);
				}
				currentSorted = currentMinimum;

				incidenceList.remove(currentMinimum);
			}
		}
	}
}
