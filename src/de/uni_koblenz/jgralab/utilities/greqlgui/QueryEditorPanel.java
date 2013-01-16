/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.greqlgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.undo.UndoManager;

import de.uni_koblenz.ist.utilities.gui.SwingApplication;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class QueryEditorPanel extends JPanel {
	private static final long serialVersionUID = -5113284469152114552L;

	private GreqlGui gui;
	private JTextArea queryArea;
	private UndoManager undoManager;
	private File queryFile;
	private boolean modified;

	public QueryEditorPanel(GreqlGui parent) throws IOException {
		this(parent, null);
	}

	public QueryEditorPanel(GreqlGui app, File f) throws IOException {
		gui = app;
		queryFile = null;

		queryArea = new JTextArea(15, 50);
		queryArea.setEditable(true);
		queryArea.setFont(gui.getQueryFont());
		queryArea.setToolTipText(MessageFormat.format(
				gui.getMessage("GreqlGui.QueryArea.ToolTip"),//$NON-NLS-1$
				gui.getEvaluateQueryShortcut()));
		queryArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (!gui.isGraphLoading() && !gui.isEvaluating()) {
						lookupWord();
					}
				}
			}
		});
		undoManager = new UndoManager();
		undoManager.setLimit(10000);
		Document doc = queryArea.getDocument();

		if (f == null) {
			newFile();
		} else {
			loadFromFile(f);
		}

		doc.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent evt) {
				undoManager.addEdit(evt.getEdit());
				setModified(true);
				gui.updateActions();
			}
		});

		JScrollPane queryScrollPane = new JScrollPane(queryArea);
		setLayout(new BorderLayout());
		add(queryScrollPane, BorderLayout.CENTER);
	}

	// fields for content assist
	CompletionEntryType lookupType;

	private CompletionTableModel completions;

	private Set<CompletionEntry> greqlEntries;

	private JTextPane descriptionTextPane;

	private boolean fontSet;

	private JTable selectTable;

	private JDialog selectWindow;

	private JTextField prefixField;

	private enum CompletionEntryType {
		GREQL_FUNCTION, GREQL_IDIOM, VERTEXCLASS, EDGECLASS, ATTRIBUTE, GRAPHELEMENTCLASS
	};

	private static class CompletionEntry implements Comparable<CompletionEntry> {
		@SuppressWarnings("unused")
		CompletionEntryType type;
		String name;
		String replacement;
		String description;
		int offset;

		public CompletionEntry(CompletionEntryType type, String name,
				String replacement, String description) {
			this.type = type;
			this.name = name;
			this.replacement = replacement;
			this.description = description;
		}

		public CompletionEntry(CompletionEntryType type, String name,
				String replacement, String description, int offset) {
			this(type, name, replacement, description);
			this.offset = offset;
		}

		@Override
		public int compareTo(CompletionEntry c) {
			return name.compareTo(c.name);
		}
	}

	private class CompletionTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 4313285792993087566L;

		ArrayList<CompletionEntry> entries;
		CompletionEntryType lookupType;

		public CompletionTableModel(Collection<CompletionEntry> e,
				CompletionEntryType t) {
			entries = new ArrayList<CompletionEntry>(e);
			lookupType = t;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return lookupType == CompletionEntryType.ATTRIBUTE ? "Attribute"
						: "Name";
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			CompletionEntry e = entries.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return e.name;
			default:
				return null;
			}
		}

		CompletionEntry getEntry(int row) {
			return entries.get(row);
		}
	}

	private CompletionTableModel getCompletionTableModel(String prefix,
			CompletionEntryType lookupType) {
		TreeSet<CompletionEntry> completionEntries = new TreeSet<CompletionEntry>();

		switch (lookupType) {
		case GREQL_FUNCTION:
			addMatchingGreqlFunctions(prefix, completionEntries);
			break;
		case VERTEXCLASS:
		case EDGECLASS:
		case ATTRIBUTE:
		case GRAPHELEMENTCLASS:
			addMatchingSchemaInformation(prefix, completionEntries, lookupType);
			break;
		default:
			throw new RuntimeException("FIXME!");
		}
		return new CompletionTableModel(completionEntries, lookupType);
	}

	private void addMatchingSchemaInformation(String prefix,
			TreeSet<CompletionEntry> completionEntries,
			CompletionEntryType lookupType) {
		Graph g = gui.getGraph();
		if (g != null) {
			Schema schema = g.getSchema();
			Stack<Package> s = new Stack<Package>();
			s.push(schema.getDefaultPackage());
			prefix = prefix.toLowerCase();
			while (!s.isEmpty()) {
				Package pkg = s.pop();
				if (lookupType == CompletionEntryType.ATTRIBUTE) {
					for (VertexClass vc : pkg.getVertexClasses()) {
						for (Attribute attr : vc.getOwnAttributeList()) {
							if (attr.getName().toLowerCase().startsWith(prefix)) {
								completionEntries.add(new CompletionEntry(
										CompletionEntryType.ATTRIBUTE, attr
												.getName()
												+ " in "
												+ vc.getSimpleName(), attr
												.getName(), getDescription(vc,
												attr)));
							}
						}
					}
					for (EdgeClass ec : pkg.getEdgeClasses()) {
						for (Attribute attr : ec.getOwnAttributeList()) {
							if (attr.getName().toLowerCase().startsWith(prefix)) {
								completionEntries.add(new CompletionEntry(
										CompletionEntryType.ATTRIBUTE, attr
												.getName()
												+ " in "
												+ ec.getSimpleName(), attr
												.getName(), getDescription(ec,
												attr)));
							}
						}
					}
				}
				if (lookupType == CompletionEntryType.VERTEXCLASS
						|| lookupType == CompletionEntryType.GRAPHELEMENTCLASS) {
					for (VertexClass vc : pkg.getVertexClasses()) {
						if (vc.getSimpleName().toLowerCase().startsWith(prefix)
								|| vc.getQualifiedName().toLowerCase()
										.startsWith(prefix)) {
							completionEntries.add(new CompletionEntry(
									CompletionEntryType.VERTEXCLASS, vc
											.getSimpleName(), vc
											.getQualifiedName(),
									getDescription(vc)));
						}
					}
				}
				if (lookupType == CompletionEntryType.EDGECLASS
						|| lookupType == CompletionEntryType.GRAPHELEMENTCLASS) {
					for (EdgeClass ec : pkg.getEdgeClasses()) {
						if (ec.getSimpleName().toLowerCase().startsWith(prefix)
								|| ec.getQualifiedName().toLowerCase()
										.startsWith(prefix)) {
							completionEntries.add(new CompletionEntry(
									CompletionEntryType.EDGECLASS, ec
											.getSimpleName(), ec
											.getQualifiedName(),
									getDescription(ec)));
						}
					}
				}
				for (Package sub : pkg.getSubPackages()) {
					s.push(sub);
				}
			}
		}
	}

	private String getDescription(GraphElementClass<?, ?> gec) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<p>").append(gec.isAbstract() ? "abstract " : "")
				.append(gec instanceof VertexClass ? "Vertex" : "Edge")
				.append("Class <strong>").append(gec.getQualifiedName())
				.append("</strong></p><dl>");
		if (gec instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) gec;
			sb.append("<dt>from</dt><dd>")
					.append(ec.getFrom().getVertexClass().getQualifiedName())
					.append(" (")
					.append(ec.getFrom().getMin())
					.append(", ")
					.append(ec.getFrom().getMax() == Integer.MAX_VALUE ? "*"
							: "" + ec.getFrom().getMax()).append(")");
			String role = ec.getFrom().getRolename();
			if (role != null && !role.isEmpty()) {
				sb.append(" role ").append(role);
			}
			sb.append(" aggregation ")
					.append(ec.getFrom().getAggregationKind());
			sb.append("</dd><dt>to</dt><dd>")
					.append(ec.getTo().getVertexClass().getQualifiedName())
					.append(" (")
					.append(ec.getTo().getMin())
					.append(", ")
					.append(ec.getTo().getMax() == Integer.MAX_VALUE ? "*" : ""
							+ ec.getTo().getMax()).append(")");
			role = ec.getTo().getRolename();
			if (role != null && !role.isEmpty()) {
				sb.append(" role ").append(role);
			}
			sb.append(" aggregation ").append(ec.getTo().getAggregationKind());
			sb.append("</dd>");
		}
		if (!gec.getAllSuperClasses().isEmpty()) {
			String delim = "<dt>Superclasses:</dt><dd>";
			for (GraphElementClass<?, ?> sup : gec.getAllSuperClasses()) {
				sb.append(delim).append(sup.getQualifiedName());
				delim = ", ";
			}
			sb.append("</dd>");
		}
		if (!gec.getAllSubClasses().isEmpty()) {
			String delim = "<dt>Subclasses:</dt><dd>";
			for (GraphElementClass<?, ?> sub : gec.getAllSubClasses()) {
				sb.append(delim).append(sub.getQualifiedName());
				delim = ", ";
			}
			sb.append("</dd>");
		}
		if (gec.getAttributeCount() > 0) {
			sb.append("<dt>Attributes:</dt>");
			for (Attribute attr : gec.getAttributeList()) {
				sb.append("<dd><strong>").append(attr.getName())
						.append("</strong>: <font color=\"purple\">")
						.append(attr.getDomain().getQualifiedName())
						.append("</font></dd>");
			}
			sb.append("</p>");
		}
		sb.append("</dl></body></html>");
		return sb.toString();
	}

	private String getDescription(GraphElementClass<?, ?> gec, Attribute attr) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<p>Attribute <strong>").append(attr.getName())
				.append("</strong>: <font color=\"purple\">")
				.append(attr.getDomain().getQualifiedName())
				.append("</font></p><p>&nbsp;&nbsp;in ")
				.append(gec instanceof VertexClass ? "Vertex" : "Edge")
				.append("Class ").append(gec.getQualifiedName()).append("</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

	private Set<CompletionEntry> getGreqlEntries() {
		if (greqlEntries != null) {
			return greqlEntries;
		}
		greqlEntries = new TreeSet<CompletionEntry>();
		Set<String> funcs = FunLib.getFunctionNames();
		for (String s : funcs) {
			greqlEntries.add(new CompletionEntry(
					CompletionEntryType.GREQL_FUNCTION, s, s + "()", FunLib
							.getFunctionInfo(s).getHtmlDescription(), -1));
		}
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"V", "V{}", getDescriptionFromResources("vertexset.html"), -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"E", "E{}", getDescriptionFromResources("edgeset.html"), -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"false", "false", "Boolean constant"));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"true", "true", "Boolean constant"));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"undefined", "undefined", "Undefined constant"));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"tv", "thisVertex",
				getDescriptionFromResources("thisliteral.html")));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"te", "thisEdge",
				getDescriptionFromResources("thisliteral.html")));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"fwr", "from\n\t\nwith\n\t\nreport\n\t\nend",
				getDescriptionFromResources("listcomp.html"), -20));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"fwr set", "from\n\t\nwith\n\t\nreportSet\n\t\nend",
				getDescriptionFromResources("setcomp.html"), -23));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"fwr map", "from\n\t\nwith\n\t\nreportMap\n\t\nend",
				getDescriptionFromResources("mapcomp.html"), -23));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"exists", "exists @ ",
				getDescriptionFromResources("exists.html"), -2));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"exists!", "exists!  @ ",
				getDescriptionFromResources("exists1.html"), -3));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"forall", "forall  @ ",
				getDescriptionFromResources("forall.html"), -3));
		greqlEntries
				.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
						"let", "let  in ",
						getDescriptionFromResources("let.html"), -4));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"where", "\nwhere ", getDescriptionFromResources("where.html"),
				-7));
		greqlEntries
				.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
						"list", "list()",
						getDescriptionFromResources("list.html"), -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"set", "set()", getDescriptionFromResources("set.html"), -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"map", "map()", getDescriptionFromResources("map.html"), -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"tup", "tup()", getDescriptionFromResources("tup.html"), -1));
		return greqlEntries;
	}

	private String getDescriptionFromResources(String filename) {
		InputStream is = getClass().getResourceAsStream(
				"resources/completion/" + filename);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		try {
			for (String line = rdr.readLine(); line != null; line = rdr
					.readLine()) {
				sb.append(line);
			}
			rdr.close();
		} catch (IOException e) {
		}
		return sb.toString();
	}

	private void addMatchingGreqlFunctions(String prefix,
			TreeSet<CompletionEntry> completionEntries) {
		prefix = prefix.toLowerCase();
		for (CompletionEntry e : getGreqlEntries()) {
			if (e.name.toLowerCase().startsWith(prefix)
					|| e.replacement.toLowerCase().startsWith(prefix)) {
				completionEntries.add(e);
			}
		}
	}

	private void setDescription(String text) {
		descriptionTextPane.setContentType("text/html");
		descriptionTextPane.setText(text);
		Font font = UIManager.getFont("Label.font");
		String bodyRule = "body { font-family: " + font.getFamily() + "; "
				+ "font-size: " + font.getSize() + "pt; }";
		((HTMLDocument) descriptionTextPane.getDocument()).getStyleSheet()
				.addRule(bodyRule);
		descriptionTextPane.setCaretPosition(0);
	}

	private class CompletionTable extends JTable {
		private static final long serialVersionUID = 8741021448180241310L;

		public CompletionTable(TableModel model) {
			super();
			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(false);
			setModel(model);
		}

		private void installSelectionListener() {
			getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
			getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							int row = getSelectedRow();
							if (completions != null && row >= 0) {
								setDescription(completions.getEntry(row).description);
							} else {
								setDescription("");
							}
						}
					});
		}

		@Override
		public void setModel(TableModel dataModel) {
			if (dataModel == null) {
				dataModel = new DefaultTableModel();
			}
			super.setModel(dataModel);
			if (dataModel.getRowCount() > 0) {
				changeSelection(0, 0, false, false);
			}
			installSelectionListener();
		}

		@Override
		public Point getToolTipLocation(MouseEvent event) {
			return new Point(event.getX() + 15, event.getY());
		}
	}

	protected void lookupWord() {
		int caretPosition = queryArea.getCaretPosition();
		char[] a = queryArea.getText().toCharArray();

		int p = caretPosition - 1;
		while (p >= 0 && (Character.isLetterOrDigit(a[p]) || a[p] == '_')) {
			--p;
		}
		// p is the position of the first non-id character to the left of
		// caretPosition
		int dot = p;
		while (dot >= 0 && Character.isWhitespace(a[dot])) {
			--dot;
		}
		int prefixStart = p + 1;
		final int insertPos = prefixStart;
		final int insertLength = caretPosition - prefixStart;

		// try to figure out whether a schema type should be inserted
		// this is the case if an opening curly bracket { is found to the left
		// of prefixStart
		while (p >= 0 && a[p] != '{') {
			if (Character.isWhitespace(a[p]) || Character.isLetterOrDigit(a[p])
					|| a[p] == '_' || a[p] == '$' || a[p] == '^' || a[p] == ','
					|| a[p] == '.' || a[p] == '!') {
				--p;
			} else {
				break;
			}
		}
		lookupType = null;
		if (p >= 0 && a[p] == '{') {
			// type is possible, try to find out whether EdgeClass or
			// VertexClass should be inserted
			--p;
			while (p >= 0 && Character.isWhitespace(a[p])) {
				--p;
			}
			if (p >= 0) {
				if (a[p] == 'V' || a[p] == '&') {
					// VertexClass
					lookupType = CompletionEntryType.VERTEXCLASS;
				} else if (a[p] == 'E' || a[p] == '-' || a[p] == '>') {
					// EdgeClass
					lookupType = CompletionEntryType.EDGECLASS;
				}
			}
			if (lookupType == null) {
				lookupType = CompletionEntryType.GRAPHELEMENTCLASS;
			}
		}
		if (lookupType == null && dot >= 0 && a[dot] == '.') {
			lookupType = CompletionEntryType.ATTRIBUTE;
		}

		if (lookupType == null) {
			lookupType = CompletionEntryType.GREQL_FUNCTION;
		}
		String prefix = "";
		try {
			prefix = queryArea.getText(insertPos, insertLength).toLowerCase();
		} catch (BadLocationException e) {
			return;
		}
		descriptionTextPane = new JTextPane();
		descriptionTextPane.setContentType("text/html");
		descriptionTextPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4,
				4));
		descriptionTextPane.setEditable(false);

		descriptionTextPane.addPropertyChangeListener("page",
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						if (!fontSet) {
							MutableAttributeSet attrs = descriptionTextPane
									.getInputAttributes();
							StyleConstants
									.setFontFamily(attrs, Font.SANS_SERIF);
							StyledDocument doc = descriptionTextPane
									.getStyledDocument();
							doc.setCharacterAttributes(0, doc.getLength() + 1,
									attrs, false);
							fontSet = true;
						}
					}
				});

		selectTable = new CompletionTable(null);

		selectWindow = new JDialog(gui, lookupType.toString());

		prefixField = new JTextField(prefix);
		if (SwingApplication.RUNS_ON_MAC_OS_X) {
			prefixField.setCaret(new DefaultCaret());
			prefixField.putClientProperty("JTextField.variant", "search");
		}
		prefixField.getCaret().setDot(prefixField.getDocument().getLength());
		prefixField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateCompletion();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateCompletion();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateCompletion();
			}
		});

		KeyListener l = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					e.consume();
					selectWindow.setVisible(false);
					queryArea.requestFocus();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
					if (selectTable.getSelectedRow() >= 0) {
						try {
							queryArea.getDocument().remove(insertPos,
									insertLength);
							CompletionEntry entry = completions
									.getEntry(selectTable.getSelectedRow());
							String completion = entry.replacement;
							queryArea.getDocument().insertString(insertPos,
									completion, null);
							queryArea.setCaretPosition(queryArea
									.getCaretPosition()
									+ completions.getEntry(selectTable
											.getSelectedRow()).offset);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
					selectWindow.setVisible(false);
					queryArea.requestFocus();
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					e.consume();
					int r = selectTable.getSelectedRow() - 1;
					if (r >= 0) {
						selectTable.changeSelection(r, 0, false, false);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					e.consume();
					int r = selectTable.getSelectedRow() + 1;
					if (r < completions.getRowCount()) {
						selectTable.changeSelection(r, 0, false, false);
					}
				}
			}
		};
		prefixField.addKeyListener(l);
		selectTable.addKeyListener(l);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(prefixField, BorderLayout.NORTH);
		JScrollPane scp = new JScrollPane(selectTable);
		scp.setPreferredSize(new Dimension(200, 300));
		leftPanel.add(scp, BorderLayout.CENTER);

		// JPanel rightPanel = new JPanel();
		// rightPanel.setLayout(new BorderLayout());
		// JScrollPane scp1 = new JScrollPane(rightPanel);
		JScrollPane scp1 = new JScrollPane(descriptionTextPane);
		scp1.setPreferredSize(new Dimension(500, 300));
		descriptionTextPane.setBackground(new Color(254, 254, 189));
		scp1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// rightPanel.add(descriptionLabel, BorderLayout.CENTER);

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				scp1);
		sp.setContinuousLayout(true);

		selectWindow.getContentPane().add(sp);

		updateCompletion();
		selectWindow.pack();
		try {
			Rectangle r = queryArea.modelToView(caretPosition);
			Point caretCoordinates = SwingUtilities.convertPoint(queryArea,
					new Point(r.x, r.y - 32), gui);
			SwingUtilities.convertPointToScreen(caretCoordinates, gui);
			selectWindow.setLocation(caretCoordinates);
		} catch (BadLocationException e) {
			// should not happen at all because caretPosition is valid
			e.printStackTrace();
		}
		selectWindow.setVisible(true);
		selectWindow.toFront();
	}

	private void updateCompletion() {
		completions = getCompletionTableModel(prefixField.getText(), lookupType);
		selectTable.setModel(completions);
		if (completions.getRowCount() > 0) {
			selectTable.changeSelection(0, 0, false, false);
		} else {
			setDescription("No matches :-(");
		}
	}

	public void setSelection(int offset, int length) {
		if (offset < 0) {
			return;
		}
		int start = Math.max(0,
				Math.min(offset, queryArea.getDocument().getLength()));
		int end = Math.max(start,
				Math.min(start + length, queryArea.getDocument().getLength()));
		queryArea.setSelectionStart(start);
		queryArea.setSelectionEnd(end);
	}

	public boolean canUndo() {
		return undoManager.canUndo();
	}

	public boolean canRedo() {
		return undoManager.canRedo();
	}

	public void undo() {
		undoManager.undo();
		setModified(true);
	}

	public void redo() {
		undoManager.redo();
		setModified(true);
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public boolean isModified() {
		return modified;
	}

	public void cut() {
		queryArea.cut();
	}

	public void copy() {
		queryArea.copy();
	}

	public void paste() {
		queryArea.paste();
	}

	public String getText() {
		return queryArea.getText();
	}

	public void removeJavaQuotes() {
		String text = queryArea.getText().trim();
		text = text.replaceAll("\"\\s*\\+\\s*\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\"", "\uffff"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replace("\uffff", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		queryArea.setText(text);
	}

	public void insertJavaQuotes() {
		String text = queryArea.getText();
		String[] lines = text.split("\n"); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			line = line.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			line = line.replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
			line = line.replace("\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("\"").append(line); //$NON-NLS-1$
			if (i < lines.length - 1) {
				sb.append("\\n\" +\n"); //$NON-NLS-1$ 
			} else {
				sb.append("\""); //$NON-NLS-1$
			}
		}
		text = sb.toString();
		queryArea.setText(text);
		setSelection(0, queryArea.getDocument().getLength());
		queryArea.copy();
	}

	@Override
	public void requestFocus() {
		queryArea.requestFocus();
	}

	public void setQuery(File queryFile, String queryText) {
		this.queryFile = queryFile;
		queryArea.setText(queryText);
		undoManager.discardAllEdits();
		setModified(false);
	}

	public File getQueryFile() {
		return queryFile;
	}

	public void setQueryFile(File queryFile) {
		this.queryFile = queryFile;
	}

	public void loadFromFile(File f) throws IOException {
		BufferedReader rdr = new BufferedReader(new FileReader(f));
		StringBuilder sb = new StringBuilder();
		for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
			sb.append(line).append("\n"); //$NON-NLS-1$
		}
		rdr.close();
		setQuery(f, sb.toString());
	}

	public void saveToFile(File f) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		pw.print(queryArea.getText());
		pw.close();
		queryFile = f;
		setModified(false);
	}

	private void newFile() {
		setQuery(null, gui.getMessage("GreqlGui.NewQuery.Text")); //$NON-NLS-1$
		setSelection(0, queryArea.getDocument().getLength());
	}

	public String getFileName() {
		return queryFile == null ? gui.getMessage("GreqlGui.NewQuery.Title") //$NON-NLS-1$
				: queryFile.getName();
	}

	public void setQueryFont(Font queryFont) {
		queryArea.setFont(queryFont);
	}
}
