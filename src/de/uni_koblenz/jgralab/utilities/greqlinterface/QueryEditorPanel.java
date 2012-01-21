package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class QueryEditorPanel extends JPanel {
	private static final long serialVersionUID = -5113284469152114552L;

	private GreqlGui app;
	private JTextArea queryArea;
	private UndoManager undoManager;
	private File queryFile;
	private boolean modified;

	public QueryEditorPanel(GreqlGui parent) throws IOException {
		this(parent, null);
	}

	public QueryEditorPanel(GreqlGui gui, File f) throws IOException {
		app = gui;
		queryFile = null;

		queryArea = new JTextArea(15, 50);
		queryArea.setEditable(true);
		queryArea.setFont(gui.getQueryFont());
		queryArea.setToolTipText(MessageFormat.format(
				gui.getMessage("GreqlGui.QueryArea.ToolTip"),
				gui.getEvaluateQueryShortcut()));
		undoManager = new UndoManager();
		undoManager.setLimit(10000);
		Document doc = queryArea.getDocument();

		if (f == null) {
			newFile();
		} else {
			loadFromFile(f);
		}

		doc.addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undoManager.addEdit(evt.getEdit());
				setModified(true);
				app.updateActions();
			}
		});

		JScrollPane queryScrollPane = new JScrollPane(queryArea);
		setLayout(new BorderLayout());
		add(queryScrollPane, BorderLayout.CENTER);
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
		String text = queryArea.getText();
		String[] lines = text.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String[] strings = line.split("\" \\+");
			int i = 0;
			for (String s : strings) {
				if (i < strings.length - 1) {
					s += "\"";
				}
				System.out.println(s);
				++i;
				int p = s.indexOf('\"');
				if (p >= 0) {
					s = s.substring(p + 1);
				}
				if (!(strings.length > 1 && strings[1].length() > 0)) {
					p = s.lastIndexOf('\"');
					if (p >= 0) {
						s = s.substring(0, p);
					}
				}
				s = s.replace("\\\"", "\"");
				s = s.replace("\\\\", "\\");
				sb.append(s).append("\n");
			}
		}
		text = sb.toString();
		queryArea.setText(text);
	}

	public void insertJavaQuotes() {
		String text = queryArea.getText();
		String[] lines = text.split("\n");
		StringBuilder sb = new StringBuilder();
		boolean firstLine = true;
		boolean spaceRequired = false;
		for (String line : lines) {
			line = line.replace("\t", " ");
			line = line.replace("\\", "\\\\");
			line = line.replace("\"", "\\\"");
			if (firstLine) {
				sb.append("\"").append(line).append("\"");
				firstLine = false;
			} else {
				boolean startsWithWs = line.length() > 0
						&& Character.isWhitespace(line.charAt(0));
				sb.append(" +\n\"")
						.append(spaceRequired && !startsWithWs ? " " : "")
						.append(line).append("\"");
			}
			spaceRequired = line.length() > 0
					&& !Character.isWhitespace(line.charAt(line.length() - 1));
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
			sb.append(line).append("\n");
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
		setQuery(null, "// Please enter your query here!");
		setSelection(0, queryArea.getDocument().getLength());
	}

	public String getFileName() {
		return queryFile == null ? "<new query>" : queryFile.getName();
	}

	public void setQueryFont(Font queryFont) {
		queryArea.setFont(queryFont);
	}
}
