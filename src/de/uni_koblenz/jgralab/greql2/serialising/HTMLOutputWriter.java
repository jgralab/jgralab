package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HTMLOutputWriter extends DefaultWriter {

	/**
	 * The writer which stores the elements
	 */
	private BufferedWriter outputWriter;

	/**
	 * The path to the file the value should be stored in
	 */
	private String filePath;

	/**
	 * The graph all elements in the jvalue to visit belong to
	 */
	private Graph dataGraph = null;

	private Object rootValue;

	private boolean createElementLinks;

	private void storeln(String s) {
		try {
			outputWriter.write(s);
			outputWriter.write("\n");
		} catch (IOException e) {
			throw new SerialisingException("Can't write to output file",
					null, e);
		}
	}

	private void store(String s) {
		try {
			outputWriter.write(s);
		} catch (IOException e) {
			throw new SerialisingException("Can't write to output file",
					null, e);
		}
	}

	private String htmlQuote(String string) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < string.length(); ++i) {
			char c = string.charAt(i);
			if (c == '<') {
				result.append("&lt;");
			} else if (c == '>') {
				result.append("&gt;");
			} else if (c == '"') {
				result.append("&quot;");
			} else if (c == '\'') {
				result.append("&apos;");
			} else if (c == '&') {
				result.append("&amp;");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	public HTMLOutputWriter(Object value, String filePath) {
		this(value, filePath, null);
	}

	public HTMLOutputWriter(Object value, String filePath,
			Graph dataGraph) {
		this(value, filePath, dataGraph, true);
	}

	public HTMLOutputWriter(Object value, String filePath,
			Graph dataGraph, boolean createElementLinks) {
		this.filePath = filePath;
		this.dataGraph = dataGraph;
		this.rootValue = value;
		this.createElementLinks = createElementLinks;
		head();
		this.write(value);
		foot();
	}

	@Override
	public void pre() {
		storeln("<table><tr><td>");
	}

	@Override
	public void post() {
		storeln("</td></tr></table>");
	}

	@Override
	public void inter() {
		storeln("</td></tr><tr><td>");
	}

	@Override
	public void writeTuple(Tuple t) {
		storeln("<table><tr><td>");
		boolean first = true;
		for (Object val : t) {
			if (first) {
				first = false;
			} else {
				storeln("</td><td>");
			}
			this.write(val);
		}
		storeln("</td></tr></table>");
	}

	@Override
	public void writeRecord(Record r) {
		storeln("<table><tr><td>");
		boolean first = true;
		//TODO find out how get on record components without knowing names
//		for (Map.Entry<String, Object> entry : r.entrySet()) {
//			if (first) {
//				first = false;
//			} else {
//				storeln("</td><td>");
//			}
//			storeln(entry.getKey() + ": ");
//			entry.getValue().accept(this);
//		}
		storeln("</td></tr></table>");
	}

	@Override
	public void writeTable(Table<?> table) {
		store("<table style=\"align:left;\"><tr><th>");
		boolean first = true;
		for (Object val : table.getTitles()) {
			if (first) {
				first = false;
			} else {
				store("</th><th>");
			}
			this.write(val);
		}
		storeln("</th></tr>");
		
		//TODO find out how to access data of table
		for (int i = 0; i < table.toPVector().size();i++) {
			PCollection<?> row = (PCollection<?>) table.get(i);
			store("<tr>");
			for (Object cell : row) {
				store("<td>");
				this.write(cell);
				store("</td>");
			}
			store("</tr>");
		}
		storeln("</table>");
	}

	@Override
	public void writeVertex(Vertex vertex) {
		if (createElementLinks) {
			storeln("<a href=\"v" + vertex.getId() + "\">v" + vertex.getId()
					+ ": " + vertex.getAttributedElementClass().getUniqueName()
					+ "</a>");
		} else {
			storeln("v" + vertex.getId() + ": "
					+ vertex.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	public void writeEdge(Edge edge) {
		if (createElementLinks) {
			storeln("<a href=\"e" + edge.getId() + "\">e" + edge.getId() + ": "
					+ edge.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			storeln("e" + edge.getId() + ": "
					+ edge.getAttributedElementClass().getUniqueName());
		}
	}

	
	@Override
	public void writeInteger(Integer b) {
		storeln(b.toString());
	}

	@Override
	public void writeLong(Long b) {
		storeln(b.toString());
	}

	@Override
	public void writeDouble(Double b) {
		storeln(b.toString());
	}

	@Override
	public void writeString(String b) {
		storeln(htmlQuote(b));
	}

	@Override
	public void writeEnum(Enum<?> e) {
		String b = e.toString();
		storeln(b);
	}

	@Override
	public void writeGraph(Graph gr) {
		if (createElementLinks) {
			storeln("<a href=\"g" + gr.getId() + "\">" + gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			storeln(gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName());
		}
	}


	@Override
	public void writeBoolean(Boolean v) {
		if (v != null) {
			storeln(v.toString());
		} else {
			storeln("null");
		}
	}

	public void writeDefaultObject(Object o){
		String b = o.toString();
		storeln(b.toString());	}

	@Override
	public void writeAttributedElementClass(AttributedElementClass c) {
		storeln(c.getQualifiedName());
	}

	@Override
	public void head() {
		try {
			outputWriter = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			throw new SerialisingException("Can't create HTML output", null,
					e);
		}
		storeln("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		storeln("<html>");
		storeln("<head>\n");
		storeln("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		storeln("<style type=\"text/css\">\n"
				+ "table { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "td { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "th { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "</style>\n");
		storeln("</head><body><table>");
		if (dataGraph != null) {
			storeln("<tr><td>Graph id: </td><td>" + dataGraph.getId()
					+ "</td></tr>");
		}
		storeln("<tr><td>Result size: </td><td>");
		if (rootValue instanceof PCollection) {
			storeln(Integer.toString(((PCollection<?>)rootValue).size()));
		} else {
			storeln("1");
		}
		storeln("</td></tr></table>\n<br/><br/>\n");
	}

	@Override
	public void foot() {
		storeln("</body></html>");
		try {
			outputWriter.close();
		} catch (IOException e) {
			throw new SerialisingException("Can't close file", null, e);
		}
	}
}
