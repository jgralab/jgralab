/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.gui.SwingApplication;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.ParsingException;
import de.uni_koblenz.jgralab.greql2.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql2.serialising.XMLOutputWriter;

@SuppressWarnings("serial")
@WorkInProgress(description = "insufficcient result presentation, simplistic hacked GUI, no load/save functionality, ...", responsibleDevelopers = "horn")
public class GreqlGui extends SwingApplication {

	private static final String VERSION = "0.0"; //$NON-NLS-1$

	private static final String BUNDLE_NAME = GreqlGui.class.getPackage()
			.getName() + ".resources.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private static final String DOCUMENT_NAME = "GReQL query";

	private static final String DOCUMENT_EXTENSION = ".greql";

	private static final String GRAPH_NAME = "Graph";

	private static final String GRAPH_EXTENSION = ".tg";

	private Graph graph;
	private JTabbedPane editorPane;
	private List<QueryEditorPanel> queries;

	private JCheckBox optimizeCheckBox;
	private JCheckBox debugOptimizationCheckBox;

	private JTextPane resultPane;
	private JTabbedPane outputPane;
	private JScrollPane resultScrollPane;
	private JTextArea consoleOutputArea;

	private JProgressBar progressBar;
	private BoundedRangeModel brm;

	private Evaluator evaluator;

	private FileDialog fd;

	private Action insertJavaQuotesAction;
	private Action removeJavaQuotesAction;

	private Action loadGraphAction;
	private Action unloadGraphAction;
	private Action evaluateQueryAction;
	private Action stopEvaluationAction;

	private boolean graphLoading;

	private boolean evaluating;

	private boolean fontSet;

	class Worker extends Thread implements ProgressFunction {
		BoundedRangeModel brm;
		private long totalElements;
		Exception ex;

		Worker(BoundedRangeModel brm) {
			this.brm = brm;
		}

		@Override
		public void finished() {
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					brm.setValue(brm.getMaximum());
				}
			});
		}

		@Override
		public long getUpdateInterval() {
			return brm.getMaximum() > totalElements ? 1 : totalElements
					/ brm.getMaximum();
		}

		@Override
		public void init(long totalElements) {
			this.totalElements = totalElements;
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					brm.setValue(brm.getMinimum());
				}
			});
		}

		@Override
		public void progress(long processedElements) {
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					brm.setValue(brm.getValue() + 1);
				}
			});
		}
	}

	class GraphLoader extends Worker {
		private File file;

		GraphLoader(BoundedRangeModel brm, File file) {
			super(brm);
			this.file = file;
		}

		@Override
		public void run() {
			try {
				graph = GraphIO.loadSchemaAndGraphFromFile(
						file.getCanonicalPath(),
						CodeGeneratorConfiguration.MINIMAL, this);
			} catch (Exception e1) {
				graph = null;
				e1.printStackTrace();
				ex = e1;
			} finally {
				graphLoading = false;
			}
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (graph == null) {

						String msg = "Can't load ";
						try {
							msg += file.getCanonicalPath() + "\n"
									+ ex.getMessage();
						} catch (IOException e) {
							msg += "graph\n";
						}
						Throwable cause = ex.getCause();
						if (cause != null) {
							msg += "\ncaused by " + cause;
						}
						JOptionPane.showMessageDialog(GreqlGui.this, msg, ex
								.getClass().getSimpleName(),
								JOptionPane.ERROR_MESSAGE);
						getStatusBar().setText("Couldn't load graph :-(");
					} else {
						getStatusBar().setText(
								"Graph '" + graph.getId() + "' loaded.");
					}
				}
			});
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getStatusBar().setText("Loading graph...");
				}
			});

		}
	}

	class Evaluator extends Worker {
		private String query;

		Evaluator(BoundedRangeModel brm, String query) {
			super(brm);
			this.query = query;
		}

		@Override
		public void run() {
			final GreqlEvaluator eval = new GreqlEvaluator(query, graph,
					new HashMap<String, Object>(), this);
			eval.setOptimize(optimizeCheckBox.isSelected());
			GreqlEvaluator.DEBUG_OPTIMIZATION = debugOptimizationCheckBox
					.isSelected();
			try {
				eval.startEvaluation();
			} catch (Exception e1) {
				ex = e1;
			}
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (ex != null) {
						evaluating = false;
						brm.setValue(brm.getMinimum());
						getStatusBar().setText("Couldn't evaluate query :-(");
						String msg = ex.getMessage();
						if (msg == null) {
							if (ex.getCause() != null) {
								msg = ex.getCause().toString();
							} else {
								msg = ex.toString();
							}
						}
						ex.printStackTrace();
						if (ex instanceof QuerySourceException) {
							QuerySourceException qs = (QuerySourceException) ex;
							List<SourcePosition> spl = qs.getSourcePositions();
							if (spl.size() > 0) {
								SourcePosition sp = spl.get(0);
								getCurrentQuery().setSelection(sp.get_offset(),
										sp.get_length());
							}
						} else if (ex instanceof ParsingException) {
							ParsingException pe = (ParsingException) ex;
							getCurrentQuery().setSelection(pe.getOffset(),
									pe.getLength());
						}
						fontSet = false;
						resultPane.setText(ex.getClass().getSimpleName() + ": "
								+ msg);
						updateActions();
						// JOptionPane.showMessageDialog(GreqlGui.this, msg,
						// ex.getClass().getSimpleName(),
						// JOptionPane.ERROR_MESSAGE);
					} else {
						getStatusBar()
								.setText(
										"Evaluation finished, loading HTML result - this may take a while...");
					}
				}
			});
			if (ex == null) {
				invokeLater(new Runnable() {
					@Override
					public void run() {
						Object result = eval.getResult();
						evaluating = false;
						updateActions();
						try {
							File xmlResultFile = new File(
									"greqlQueryResult.xml");
							XMLOutputWriter xw = new XMLOutputWriter(graph);
							xw.writeValue(result, xmlResultFile);
							File resultFile = new File("greqlQueryResult.html");
							// File resultFile = File.createTempFile(
							// "greqlQueryResult", ".html");
							// resultFile.deleteOnExit();
							HTMLOutputWriter w = new HTMLOutputWriter(graph);
							w.setUseCss(false);
							w.writeValue(result, resultFile);
							Document doc = resultPane.getDocument();
							doc.putProperty(Document.StreamDescriptionProperty,
									null);
							outputPane.setSelectedComponent(resultScrollPane);
							fontSet = false;
							resultPane.setPage(new URL("file", "localhost",
									resultFile.getCanonicalPath()));
						} catch (SerialisingException e) {
							JOptionPane.showMessageDialog(GreqlGui.this,
									"Exception during HTML output of result: "
											+ e.toString());
						} catch (IOException e) {
							JOptionPane.showMessageDialog(GreqlGui.this,
									"Exception during HTML output of result: "
											+ e.toString());
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getStatusBar().setText("Evaluating query...");
				}
			});
		}
	}

	public GreqlGui() {
		super(RESOURCE_BUNDLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initializeApplication();
		fd = new FileDialog(getApplicationName());
	}

	private class ConsoleOutputStream extends PrintStream {
		public ConsoleOutputStream() {
			super(new ByteArrayOutputStream());
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			final String aString = new String(buf, off, len);
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					consoleOutputArea.append(aString);
				}
			});
		}
	}

	@Override
	protected void editUndo() {
		getCurrentQuery().undo();
		updateActions();
	}

	@Override
	protected void editRedo() {
		getCurrentQuery().redo();
		updateActions();
	}

	@Override
	protected void editCut() {
		getCurrentQuery().cut();
		updateActions();
	}

	@Override
	protected void editCopy() {
		getCurrentQuery().copy();
		updateActions();
	}

	@Override
	protected void editPaste() {
		getCurrentQuery().paste();
		updateActions();
	}

	@Override
	public void updateActions() {
		setModified(getCurrentQuery() != null && getCurrentQuery().isModified());

		fileCloseAction.setEnabled(getCurrentQuery() != null);
		fileSaveAction.setEnabled(getCurrentQuery() != null && isModified());
		fileSaveAsAction.setEnabled(getCurrentQuery() != null);
		filePrintAction.setEnabled(false);

		editUndoAction.setEnabled(getCurrentQuery() != null
				&& getCurrentQuery().canUndo());
		editRedoAction.setEnabled(getCurrentQuery() != null
				&& getCurrentQuery().canRedo());
		editCutAction.setEnabled(getCurrentQuery() != null);
		editCopyAction.setEnabled(getCurrentQuery() != null);
		editPasteAction.setEnabled(getCurrentQuery() != null);

		insertJavaQuotesAction.setEnabled(getCurrentQuery() != null);
		removeJavaQuotesAction.setEnabled(getCurrentQuery() != null);

		loadGraphAction.setEnabled(!evaluating && !graphLoading);
		unloadGraphAction.setEnabled(!evaluating && !graphLoading
				&& graph != null);
		evaluateQueryAction.setEnabled(getCurrentQuery() != null && !evaluating
				&& !graphLoading);
		stopEvaluationAction.setEnabled(evaluating);

		setTitle(MessageFormat.format(
				getMessage("Application.mainwindow.title"),
				getCurrentQuery() != null ? getCurrentQuery().getFileName()
						: "(no query opened)"));
		if (getCurrentQuery() != null) {
			editorPane.setTitleAt(editorPane.getSelectedIndex(),
					(getCurrentQuery().isModified() ? "*" : "")
							+ getCurrentQuery().getFileName());
		}
		if (getCurrentQuery() != null) {
			getCurrentQuery().requestFocus();
		}
	}

	@Override
	protected void createActions() {
		// TODO Auto-generated method stub
		super.createActions();
		loadGraphAction = new AbstractAction("Load graph ...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadGraph();
			}
		};

		unloadGraphAction = new AbstractAction("Unload graph") {
			@Override
			public void actionPerformed(ActionEvent e) {
				unloadGraph();
			}
		};

		evaluateQueryAction = new AbstractAction("Evaluate query") {
			@Override
			public void actionPerformed(ActionEvent e) {
				evaluateQuery();
			}
		};

		stopEvaluationAction = new AbstractAction("Stop evaluation") {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopEvaluation();
			}
		};

		insertJavaQuotesAction = new AbstractAction("Insert Java quotes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertJavaQuotes();
			}
		};

		removeJavaQuotesAction = new AbstractAction("Remove Java quotes") {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeJavaQuotes();
			}
		};
	}

	@Override
	protected JMenuBar createMenuBar() {
		// TODO Auto-generated method stub
		JMenuBar mb = super.createMenuBar();
		JMenu graphMenu = new JMenu("Graph");
		graphMenu.add(loadGraphAction);
		graphMenu.add(unloadGraphAction);
		mb.add(graphMenu, mb.getComponentIndex(helpMenu));
		return mb;
	}

	@Override
	protected JPanel createToolBar() {
		JPanel pnl = super.createToolBar();
		pnl.add(new JButton(loadGraphAction));
		pnl.add(new JButton(evaluateQueryAction));
		pnl.add(new JButton(stopEvaluationAction));
		pnl.add(new JButton(insertJavaQuotesAction));
		pnl.add(new JButton(removeJavaQuotesAction));
		return pnl;
	}

	@Override
	protected Component createContent() {
		queries = new ArrayList<QueryEditorPanel>();
		editorPane = new JTabbedPane();
		editorPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateActions();
			}
		});
		resultPane = new JTextPane();
		resultPane.setEditable(false);

		// install property change listener to update status label
		resultPane.addPropertyChangeListener("page",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						if (!fontSet) {
							MutableAttributeSet attrs = resultPane
									.getInputAttributes();
							StyleConstants
									.setFontFamily(attrs, Font.SANS_SERIF);
							StyledDocument doc = resultPane.getStyledDocument();
							doc.setCharacterAttributes(0, doc.getLength() + 1,
									attrs, false);
							fontSet = true;
							getStatusBar().setText("Result complete.");
						}
					}
				});

		resultScrollPane = new JScrollPane(resultPane);
		resultScrollPane.setPreferredSize(new Dimension(200, 200));

		brm = new DefaultBoundedRangeModel();
		progressBar = new JProgressBar();
		progressBar.setModel(brm);

		consoleOutputArea = new JTextArea();
		JScrollPane consoleScrollPane = new JScrollPane(consoleOutputArea);
		consoleScrollPane.setPreferredSize(new Dimension(200, 200));

		System.setOut(new ConsoleOutputStream());
		// System.setErr(new ConsoleOutputStream());

		outputPane = new JTabbedPane();
		outputPane.addTab("Result", resultScrollPane);
		outputPane.addTab("Console", consoleScrollPane);

		optimizeCheckBox = new JCheckBox("Enable optimizer");
		optimizeCheckBox.setSelected(true);

		debugOptimizationCheckBox = new JCheckBox("Debug optimization");
		debugOptimizationCheckBox.setSelected(false);

		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BorderLayout(4, 4));
		queryPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		queryPanel.add(editorPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(optimizeCheckBox);
		buttonPanel.add(debugOptimizationCheckBox);
		queryPanel.add(buttonPanel, BorderLayout.SOUTH);

		JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spl.add(queryPanel, JSplitPane.TOP);
		spl.add(outputPane, JSplitPane.BOTTOM);

		// Don't allow shrinking so that buttons get invisible
		spl.setMinimumSize(new Dimension(
				buttonPanel.getPreferredSize().width + 10, 450));

		fileNew();
		return spl;
	}

	protected String getPrefString(String key, String def) {
		Preferences prefs = Preferences.userNodeForPackage(GreqlGui.class);
		return prefs.get(key, def);
	}

	protected void setPrefString(String key, String val) {
		Preferences prefs = Preferences.userNodeForPackage(GreqlGui.class);
		prefs.put(key, val);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	protected void openFile(File f) throws IOException {
		// look for existing editor
		for (QueryEditorPanel q : queries) {
			if (f.equals(q.getQueryFile())) {
				editorPane.setSelectedComponent(q);
				return;
			}
		}
		System.out.println("openFile(" + f.getAbsolutePath() + ")");
		System.out.println("\t" + getCurrentQuery());
		if (getCurrentQuery() != null
				&& getCurrentQuery().getQueryFile() == null
				&& !getCurrentQuery().isModified()) {
			// re-use fresh editor
			getCurrentQuery().loadFromFile(f);
			updateActions();
		} else {
			// create new editor
			QueryEditorPanel newQuery = new QueryEditorPanel(this, f);
			queries.add(newQuery);
			editorPane.addTab("", newQuery);
			editorPane.setSelectedComponent(newQuery);
		}
		setPrefString("LAST_QUERY_DIRECTORY", f.getParentFile()
				.getCanonicalPath());
	}

	protected void saveFile(File f) throws IOException {
		getCurrentQuery().saveToFile(f);
		setPrefString("LAST_QUERY_DIRECTORY", f.getParentFile()
				.getCanonicalPath());
		updateActions();
	}

	@Override
	protected void fileNew() {
		QueryEditorPanel newQuery;
		try {
			newQuery = new QueryEditorPanel(this);
			queries.add(newQuery);
			editorPane.addTab("", newQuery);
			editorPane.setSelectedComponent(newQuery);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void fileOpen() {
		String lastQueryDirectory = getPrefString("LAST_QUERY_DIRECTORY",
				System.getProperty("user.dir"));
		fd.setDirectory(new File(lastQueryDirectory));
		File queryFile = fd.showFileOpenDialog(this, "Open " + DOCUMENT_NAME,
				DOCUMENT_EXTENSION, DOCUMENT_NAME + "s");
		if (queryFile != null) {
			try {
				openFile(queryFile);
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean confirmClose() {
		if (!isModified()) {
			return true;
		}
		switch (JOptionPane.showConfirmDialog(this,
				"Unsaved changes, save them?", getMessage("Application.name"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
		case JOptionPane.YES_OPTION:
			if (getCurrentQuery().getQueryFile() != null) {
				try {
					saveFile(getCurrentQuery().getQueryFile());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			} else {
				return fileSaveAs();
			}
		case JOptionPane.NO_OPTION:
			return true;
		default:
			return false;
		}
	}

	@Override
	protected boolean confirmExit() {
		for (QueryEditorPanel q : queries) {
			if (q.isModified()) {
				editorPane.setSelectedComponent(q);
				if (!confirmClose()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void fileClose() {
		if (confirmClose()) {
			int i = editorPane.getSelectedIndex();
			editorPane.remove(i);
			queries.remove(getCurrentQuery());
		}
	}

	@Override
	protected void fileSave() {
		if (getCurrentQuery().getQueryFile() == null) {
			fileSaveAs();
		} else {
			try {
				saveFile(getCurrentQuery().getQueryFile());
				updateActions();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean fileSaveAs() {
		File f = fd.showFileSaveAsDialog(this, "Save " + DOCUMENT_NAME + " as",
				DOCUMENT_EXTENSION, getCurrentQuery().getQueryFile());
		if (f == null) {
			return false;
		}
		System.out.println("SaveFileAs " + f.getAbsolutePath());
		try {
			saveFile(f);
			updateActions();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void editPreferences() {
		new SettingsDialog(this);
	}

	public void loadGraph() {
		String lastDirectoryName = getPrefString("LAST_GRAPH_DIRECTORY",
				System.getProperty("user.dir"));

		fd.setDirectory(new File(lastDirectoryName));
		File graphFile = fd.showFileOpenDialog(this, "Open " + GRAPH_NAME,
				GRAPH_EXTENSION, GRAPH_NAME + "s");
		if (graphFile != null) {
			try {
				setPrefString("LAST_GRAPH_DIRECTORY", graphFile.getParentFile()
						.getCanonicalPath());
				graphLoading = true;
				updateActions();
				new GraphLoader(brm, graphFile).start();
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	public void unloadGraph() {
		graph = null;
		getStatusBar().setText("Graph unloaded.");
		updateActions();
	}

	private void insertJavaQuotes() {
		getCurrentQuery().insertJavaQuotes();
		updateActions();
	}

	private void removeJavaQuotes() {
		getCurrentQuery().removeJavaQuotes();
		updateActions();
	}

	private void evaluateQuery() {
		evaluating = true;
		brm.setValue(brm.getMinimum());
		evaluator = new Evaluator(brm, getCurrentQuery().getText());
		updateActions();
		evaluator.start();
	}

	private void stopEvaluation() {
		if (evaluating) {
			evaluator.stop(); // this brutal brake is intended!
			evaluating = false;
			fontSet = false;
			evaluator = null;
			brm.setValue(brm.getMinimum());
			resultPane.setText("Query aborted.");
			getStatusBar().setText("Query aborted.");
			updateActions();
		}
	}

	public static void main(String[] args) {
		SwingApplication.invokeLater(new Runnable() {
			@Override
			public void run() {
				Locale.setDefault(Locale.ENGLISH);
				GreqlGui g = new GreqlGui();
				g.setVisible(true);
				// FontSelectionDialog.selectFont(g,
				// g.getMessage("Settings.SelectQueryFont"), null, false);
			}
		});
	}

	private QueryEditorPanel getCurrentQuery() {
		return (QueryEditorPanel) editorPane.getSelectedComponent();
	}

	public void saveSettings() {
		// TODO
	}
}
