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
 
package de.uni_koblenz.jgralab.utilities.tg2whatever;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;

public abstract class Tg2Whatever {

	protected Graph graph = null;

	protected String outputName = null;

	protected boolean domainNames = false;

	protected boolean edgeAttributes = false;

	protected boolean reversedEdges = false;

	protected boolean roleNames = false;

	protected boolean shortenStrings = false;

	protected BooleanGraphMarker marker = null;

	public Tg2Whatever() {
		// System.err.println("outputName = '" + outputName + "'");
		// System.err.println("domainNames = " + domainNames);
		// System.err.println("edgeAttributes = " + edgeAttributes);
		// System.err.println("reversedEdges = " + reversedEdges);
		// System.err.println("roleNames = " + roleNames);
		// System.err.println("shortenStringsNames = " + shortenStrings);
	}

	/**
	 * sets the graph marker. If this is not null, only vertices and edges that
	 * are marked with the marker will be printed to the dot-file
	 */
	public void setGraphMarker(BooleanGraphMarker m) {
		marker = m;
	}

	/**
	 * toggles which graph to convertes
	 */
	public void setGraph(Graph g) {
		graph = g;
	}

	/**
	 * loads the graph from file
	 */
	public void setGraph(String fileName) throws GraphIOException {
		graph = GraphIO.loadGraphFromFile(fileName, new ProgressFunctionImpl(
				System.out));
	}

	/**
	 * toggles the name of the output file
	 */
	public void setOutputFile(String file) {
		outputName = file;
	}

	/**
	 * toggles wether to print rolenames or not
	 * 
	 * @param print
	 */
	public void setPrintRoleNames(boolean print) {
		roleNames = print;
	}

	/**
	 * toggles wether to shorten long strings
	 * 
	 * @param shorten
	 */
	public void setShortenStrings(boolean shorten) {
		shortenStrings = shorten;
	}

	/**
	 * toggles wether to print edgeAttributes or not
	 * 
	 * @param print
	 */
	public void setPrintEdgeAttributes(boolean print) {
		edgeAttributes = print;
	}

	/**
	 * toggles wether to print reversed edges or not
	 * 
	 * @param print
	 */
	public void setPrintReversedEdges(boolean print) {
		reversedEdges = print;
	}

	/**
	 * toggles wether to print domain names of attributes or not
	 * 
	 * @param print
	 */
	public void setPrintDomainNames(boolean print) {
		domainNames = print;
	}
	
	
	public void printGraph() {
		try {
			System.out.println("Trying to print graph");
			PrintStream out;
			if (outputName.equals("")) {
				out = System.out;
			} else {
				out = new PrintStream(new FileOutputStream(outputName));
			}
			graphStart(out);
			Vertex v = graph.getFirstVertex();
			while (v != null) {
				if ((marker == null) || (marker.isMarked(v)))
					printVertex(out, v);
				v = v.getNextVertex();
			}

			Edge e = graph.getFirstEdgeInGraph();
			while (e != null) {
				if ((marker == null) || (marker.isMarked(e)))
					printEdge(out, e);
				e = e.getNextEdgeInGraph();
			}
			graphEnd(out);
		} catch (FileNotFoundException e) {
			System.err.println("File '" + outputName
					+ "' could not be created.");
			System.exit(1);
		}
	}
	
	protected abstract void graphStart(PrintStream out);
	
	protected abstract void graphEnd(PrintStream out);
	
	protected abstract void printVertex(PrintStream out, Vertex v);
	
	protected abstract void printEdge(PrintStream out, Edge e);
	
	

	/**
	 * replaces characters in the given string by the escape sequences
	 * that are appropriate for the specific output format
	 * @param s
	 * @return
	 */
	protected abstract String stringQuote(String s);


	protected void getOptions(String[] args) {
		LongOpt[] longOptions = new LongOpt[8];

		int c = 0;
		longOptions[c++] = new LongOpt("graph", LongOpt.REQUIRED_ARGUMENT,
				null, 'g');
		longOptions[c++] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
				null, 'o');
		longOptions[c++] = new LongOpt("domains", LongOpt.NO_ARGUMENT, null,
				'd');

		longOptions[c++] = new LongOpt("edgeattr", LongOpt.NO_ARGUMENT, null,
				'e');

		longOptions[c++] = new LongOpt("reversed", LongOpt.NO_ARGUMENT, null,
				'r');

		longOptions[c++] = new LongOpt("rolenames", LongOpt.NO_ARGUMENT, null,
				'n');

		longOptions[c++] = new LongOpt("shorten-strings", LongOpt.NO_ARGUMENT,
				null, 's');

		longOptions[c++] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');

		Getopt g = new Getopt("Tg2Dot", args, "g:o:dernsh", longOptions);
		c = g.getopt();
		String graphName = null;
		while (c >= 0) {
			switch (c) {
			case 'g':
				try {
					graphName = g.getOptarg();
					setGraph(graphName);
				} catch (GraphIOException e) {
					System.err.println("Coundn't load graph in file '"
							+ graphName + "': " + e.getMessage());
					if (e.getCause() != null) {
						e.getCause().printStackTrace();
					}
					System.exit(1);
				}
				break;
			case 'o':
				outputName = g.getOptarg();
				if (outputName == null) {
					usage(1);
				}
				break;
			case 'r':
				reversedEdges = true;
				break;
			case 'd':
				domainNames = true;
				break;
			case 'e':
				edgeAttributes = true;
				break;
			case 'n':
				roleNames = true;
				break;
			case 's':
				shortenStrings = true;
				break;
			case '?':
			case 'h':
				usage(0);
				break;
			default:
				throw new RuntimeException("FixMe (c='" + (char) c + "')");
			}
			c = g.getopt();
		} 
		if (g.getOptind() < args.length) {
			System.err.println("Extra arguments!");
			usage(1);
		} 
		if (g.getOptarg() == null) {
			System.out.println("Missing option");
		//	usage(1);
		}
		if (outputName == null) {
			outputName = "";
		}
	}

	protected void usage(int exitCode) {
		System.err.println("Usage: Tg2Dot -g graphFileName [options]");
		System.err
				.println("The schema classes of the graph must be reachable via CLASSPATH.");
		System.err.println("Options are:");
		System.err
				.println("-g graphFileName   (--graph)     the graph to be converted");
		System.err
				.println("-o outputFileName  (--output)    the output file name, or empty for stdout");
		System.err
				.println("-d                 (--domains)   if set, domain names of attributes will be printed");
		System.err
				.println("-e                 (--edgeattr)  if set, edge attributes will be printed");
		System.err
				.println("-n                 (--rolenames) if set, role names will be printed");
		System.err
				.println("-r                 (--reversed)  useful if edges run from child nodes to their parents");
		System.err
				.println("                                 results in a tree with root node at top");
		System.err
				.println("-h                 (--help)      prints usage information");

		System.exit(exitCode);
	}

}
