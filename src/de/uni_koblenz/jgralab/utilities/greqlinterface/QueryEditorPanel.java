/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
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
					// System.out.println("pressed " + e);
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

	// content assist
	private String prefix;
	CompletionEntryType lookupType;

	private CompletionTableModel tm;

	enum CompletionEntryType {
		GREQL_FUNCTION, GREQL_IDIOM, VERTEXCLASS, EDGECLASS, ATTRIBUTE, GRAPHELEMENTCLASS
	};

	private static class CompletionEntry implements Comparable<CompletionEntry> {
		CompletionEntryType type;
		String name;
		String replacement;
		String info;
		String description;
		int offset;

		public CompletionEntry(CompletionEntryType type, String name,
				String replacement, String info, String description) {
			this.type = type;
			this.name = name;
			this.replacement = replacement;
			this.info = info;
			this.description = description;
		}

		public CompletionEntry(CompletionEntryType type, String name,
				String replacement, String info, String description, int offset) {
			this(type, name, replacement, info, description);
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
			switch (lookupType) {
			case GRAPHELEMENTCLASS:
			case VERTEXCLASS:
			case EDGECLASS:
			case ATTRIBUTE:
				return 3;
			default:
				return 2;
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return lookupType == CompletionEntryType.ATTRIBUTE ? "Attribute"
						: "Name";
			case 1:
				return lookupType == CompletionEntryType.ATTRIBUTE ? "Type"
						: lookupType == CompletionEntryType.GREQL_FUNCTION ? "Info"
								: "V/E";
			case 2:
				return lookupType == CompletionEntryType.ATTRIBUTE ? "Vertex/EdgeClass"
						: "Qualified Name";
			}
			return null;
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
			case 1:
				return e.info;
			case 2:
				return e.description;
			}
			return null;
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
						for (Attribute attr : vc.getAttributeList()) {
							if (attr.getName().toLowerCase().startsWith(prefix)) {
								completionEntries.add(new CompletionEntry(
										CompletionEntryType.ATTRIBUTE, attr
												.getName(), attr.getName(),
										attr.getDomain().getSimpleName(), vc
												.getQualifiedName()));
							}
						}
					}
					for (EdgeClass ec : pkg.getEdgeClasses()) {
						for (Attribute attr : ec.getAttributeList()) {
							if (attr.getName().toLowerCase().startsWith(prefix)) {
								completionEntries.add(new CompletionEntry(
										CompletionEntryType.ATTRIBUTE, attr
												.getName(), attr.getName(),
										attr.getDomain().getSimpleName(), ec
												.getQualifiedName()));
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
											.getQualifiedName() + "}", "V", vc
											.getQualifiedName()));
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
											.getQualifiedName() + "}", "E", ec
											.getQualifiedName()));
						}
					}
				}
				for (Package sub : pkg.getSubPackages()) {
					s.push(sub);
				}
			}
		}
	}

	private Set<CompletionEntry> greqlEntries;

	private Set<CompletionEntry> getGreqlEntries() {
		if (greqlEntries != null) {
			return greqlEntries;
		}
		greqlEntries = new TreeSet<CompletionEntry>();
		Set<String> funcs = FunLib.getFunctionNames();
		for (String s : funcs) {
			greqlEntries.add(new CompletionEntry(
					CompletionEntryType.GREQL_FUNCTION, s, s + "()",
					"GReQL function", "", -1));
		}
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"V", "V{}", "Vertex Set", "", -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"E", "E{}", "Edge Set", "", -1));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"false", "false", "Constant", ""));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"true", "true", "Constant", ""));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"undefined", "undefined", "Constant", ""));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"fwr", "from\n\t\nwith\n\t\nreport\n\t\nend", "FWR-Expression",
				"", -20));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"exists", "exists @ ", "Existential quantifier (at least one)",
				"", -2));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"exists!", "exists!  @ ",
				"Existential quantifier (excatly one)", "", -3));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"forall", "forall  @ ", "Universal quantifier (all)", "", -3));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"let", "let  in ", "let expression", "", -4));
		greqlEntries.add(new CompletionEntry(CompletionEntryType.GREQL_IDIOM,
				"where", "\nwhere ", "where expression", "", -7));
		return greqlEntries;
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

	private static class CompletionTableCellRenderer extends
			DefaultTableCellRenderer {
		private static final long serialVersionUID = 8947541205236635434L;
		private int oldRow;
		private String toolTip;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			CompletionEntry e = ((CompletionTableModel) table.getModel())
					.getEntry(row);
			if (toolTip == null || oldRow != row) {
				toolTip = e.type == CompletionEntryType.GREQL_FUNCTION ? FunLib
						.getFunctionInfo(e.name).getHtmlDescription()
						: e.description;
				oldRow = row;
			}
			setToolTipText(toolTip);
			return this;
		}
	}

	private class CompletionTable extends JTable {
		private static final long serialVersionUID = 8741021448180241310L;

		public CompletionTable(TableModel model) {
			super(model);
			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(false);
			getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
			changeSelection(0, 0, false, false);
			installCellRenderer();
		}

		private void installCellRenderer() {
			CompletionTableCellRenderer cellRenderer = new CompletionTableCellRenderer();
			Enumeration<TableColumn> cols = getColumnModel().getColumns();
			while (cols.hasMoreElements()) {
				TableColumn col = cols.nextElement();
				col.setCellRenderer(cellRenderer);
			}
		}

		@Override
		public void setModel(TableModel dataModel) {
			super.setModel(dataModel);
			installCellRenderer();
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
		try {
			prefix = queryArea.getText(insertPos, insertLength).toLowerCase();

			tm = getCompletionTableModel(prefix, lookupType);
			if (tm == null || tm.getRowCount() == 0) {
				Toolkit.getDefaultToolkit().beep();

				// } else if (tm.getRowCount() == 1) {
				// queryArea.getDocument().remove(insertPos, insertLength);
				// queryArea.getDocument().insertString(insertPos,
				// tm.getEntry(0).replacement, null);
				// queryArea.setCaretPosition(queryArea.getCaretPosition()
				// + tm.getEntry(0).offset);
				// return;

			} else {
				Rectangle r = queryArea.modelToView(caretPosition);
				Point caretCoordinates = SwingUtilities.convertPoint(queryArea,
						new Point(r.x, r.y - 32), gui);
				SwingUtilities.convertPointToScreen(caretCoordinates, gui);

				final JDialog selectWindow = new JDialog(gui,
						lookupType.toString());
				final JTable selectTable = new CompletionTable(tm);
				selectTable.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						selectWindow.setVisible(false);
						queryArea.requestFocus();
					}
				});
				selectTable.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
							e.consume();
							selectWindow.setVisible(false);
							queryArea.requestFocus();
						} else if (e.getKeyCode() == KeyEvent.VK_ENTER
								|| e.getKeyCode() == KeyEvent.VK_SPACE) {
							e.consume();
							try {
								queryArea.getDocument().remove(insertPos,
										insertLength);
								String completion = tm.getEntry(selectTable
										.getSelectedRow()).replacement;
								queryArea.getDocument().insertString(insertPos,
										completion, null);
								queryArea.setCaretPosition(queryArea
										.getCaretPosition()
										+ tm.getEntry(selectTable
												.getSelectedRow()).offset);
							} catch (BadLocationException e1) {
							}
							selectWindow.setVisible(false);
							queryArea.requestFocus();
						}
					}

					@Override
					public void keyTyped(KeyEvent e) {
						char c = e.getKeyChar();
						String oldPrefix = prefix;
						if (Character.isLetterOrDigit(c) || c == '_'
								|| c == '$') {
							prefix += Character.toLowerCase(c);
							e.consume();
						} else if (c == KeyEvent.VK_BACK_SPACE) {
							if (prefix.length() > 0) {
								prefix = prefix.substring(0,
										prefix.length() - 1);
							} else {
								Toolkit.getDefaultToolkit().beep();
							}
							e.consume();
						}
						if (!prefix.equals(oldPrefix)) {
							tm = getCompletionTableModel(prefix, lookupType);
							if (tm == null || tm.getRowCount() == 0) {
								Toolkit.getDefaultToolkit().beep();
								prefix = oldPrefix;
							} else {
								selectTable.setModel(tm);
								selectTable.changeSelection(0, 0, false, false);
							}
						}
					}
				});
				JScrollPane scp = new JScrollPane(selectTable);
				scp.setPreferredSize(new Dimension(300, 200));
				scp.setColumnHeaderView(new JPanel());
				selectWindow.getContentPane().add(scp);
				selectWindow.pack();
				selectWindow.setLocation(caretCoordinates);
				selectWindow.setModalityType(ModalityType.APPLICATION_MODAL);
				selectWindow.setVisible(true);
				selectWindow.toFront();
				selectTable.requestFocus();
			}
		} catch (BadLocationException e) {
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
		StringBuffer sb = new StringBuffer();
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
