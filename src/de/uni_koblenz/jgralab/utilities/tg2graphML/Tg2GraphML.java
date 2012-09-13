/**
 * 
 */
package de.uni_koblenz.jgralab.utilities.tg2graphML;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

/**
 * @author horn
 * 
 */
public class Tg2GraphML extends Tg2Whatever {

	private String id(GraphElement<?, ?> elem) {
		return (elem instanceof Vertex ? "v" : "e") + elem.getId();
	}

	private String label(GraphElement<?, ?> elem) {
		return id(elem) + ": "
				+ elem.getAttributedElementClass().getSimpleName();
	}

	private String attrs(GraphElement<?, ?> elem) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (Attribute a : elem.getAttributedElementClass().getAttributeList()) {
			sb.append(a.getName());
			sb.append(" = ");
			sb.append(formatAttrValue(elem.getAttribute(a.getName())));
			sb.append("\n");
		}
		return sb.toString();
	}

	private String formatAttrValue(Object val) {
		StringBuilder sb = new StringBuilder();
		if (val == null) {
			return "null";
		} else if (val instanceof String) {
			sb.append('"');
			sb.append(val);
			sb.append('"');
		} else if (val instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) val;
			sb.append("{");
			boolean first = true;
			for (Entry<?, ?> e : m.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(formatAttrValue(e.getKey()));
				sb.append(" -&gt; ");
				sb.append(formatAttrValue(e.getValue()));
			}
			sb.append("}");
		} else if (val instanceof Record) {
			Record r = (Record) val;
			sb.append("[");
			boolean first = true;
			for (String c : r.getComponentNames()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(c);
				sb.append(" = ");
				sb.append(formatAttrValue(r.getComponent(c)));
			}
			sb.append("]");
		} else {
			return val.toString();
		}
		return sb.toString();
	}

	private static final String NODE_GRAPHICS_ATTR = "nodeGraphics";
	private static final String EDGE_GRAPHICS_ATTR = "edgeGraphics";

	@Override
	protected void graphStart(PrintStream out) {
		if (isReversedEdges()) {
			throw new RuntimeException("Reversed edges not supported.");
		}

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">");
		out.printf(
				"<key for=\"node\" id=\"%s\" yfiles.type=\"nodegraphics\"/>\n",
				NODE_GRAPHICS_ATTR);
		out.printf(
				"<key for=\"edge\" id=\"%s\" yfiles.type=\"edgegraphics\"/>\n",
				EDGE_GRAPHICS_ATTR);
		out.printf(" <graph id=\"%s\" edgedefault=\"directed\">\n",
				graph.getId());
	}

	@Override
	protected void graphEnd(PrintStream out) {
		out.println(" </graph>");
		out.println("</graphml>");
	}

	@Override
	protected void printVertex(PrintStream out, Vertex v) {
		out.printf("  <node id=\"%s\">\n", id(v));
		out.printf("   <data key=\"%s\">\n", NODE_GRAPHICS_ATTR);
		out.println("    <y:GenericNode configuration=\"com.yworks.entityRelationship.big_entity\">");
		out.printf(
				"     <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" backgroundColor=\"#B7C9E3\" configuration=\"com.yworks.entityRelationship.label.name\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" textColor=\"#000000\" underlinedText=\"true\" visible=\"true\"  modelName=\"internal\" modelPosition=\"t\">%s</y:NodeLabel>\n",
				label(v));
		if (v.getAttributedElementClass().hasAttributes()) {
			out.printf(
					"     <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\" configuration=\"com.yworks.entityRelationship.label.attributes\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"internal\" modelPosition=\"bl\" textColor=\"#000000\" visible=\"true\">%s</y:NodeLabel>",
					attrs(v));
		}
		out.println("    </y:GenericNode>");
		out.println("   </data>");
		out.println("  </node>");
	}

	@Override
	protected void printEdge(PrintStream out, Edge e) {
		out.printf("    <edge id=\"%s\" source=\"%s\" target=\"%s\">\n", id(e),
				id(e.getAlpha()), id(e.getOmega()));
		out.printf("   <data key=\"%s\">\n", EDGE_GRAPHICS_ATTR);
		out.println("    <y:PolyLineEdge>");
		out.println("     <y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>");
		out.println("     <y:Arrows source=\"none\" target=\"standard\"/>");
		out.printf(
				"     <y:EdgeLabel alignment=\"center\" distance=\"2.0\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"centered\" modelPosition=\"center\" ratio=\"0.5\" underlinedText=\"true\" textColor=\"#000000\" visible=\"true\">%s</y:EdgeLabel>",
				label(e));
		if (edgeAttributes) {
			out.printf(
					"     <y:EdgeLabel alignment=\"center\" distance=\"2.0\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" modelName=\"centered\" modelPosition=\"center\" ratio=\"0.5\" textColor=\"#000000\" visible=\"true\">%s</y:EdgeLabel>",
					attrs(e));
		}
		out.println("    </y:PolyLineEdge>");
		out.println("   </data>");
		out.println("  </edge>");
	}

	@Override
	protected String stringQuote(String s) {
		return s;
	}

	/**
	 * @param args
	 * @throws GraphIOException
	 * @throws IOException
	 */
	public static void main(String[] args) throws GraphIOException, IOException {
		Graph g = GraphIO.loadGraphFromFile(
				"testit/testgraphs/greqltestgraph.tg",
				ImplementationType.GENERIC, null);
		Tg2GraphML converter = new Tg2GraphML();
		converter.setGraph(g);
		File f = new File("/home/horn/greqltestgraph.graphml");
		f.createNewFile();
		converter.setOutputFile(f.getPath());
		converter.convert();
	}

}
