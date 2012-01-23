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
import java.awt.event.KeyEvent;
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
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.gui.RecentFilesList;
import de.uni_koblenz.ist.utilities.gui.SwingApplication;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.ParsingException;
import de.uni_koblenz.jgralab.greql2.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql2.serialising.XMLOutputWriter;

@SuppressWarnings("serial")
public class GreqlGui extends SwingApplication {
	// keys for preferences
	private static final String PREFS_KEY_LAST_QUERY_DIRECTORY = "LAST_QUERY_DIRECTORY"; //$NON-NLS-1$
	private static final String PREFS_KEY_LAST_GRAPH_DIRECTORY = "LAST_GRAPH_DIRECTORY"; //$NON-NLS-1$
	private static final String PREFS_KEY_RECENT_GRAPH = "RECENT_GRAPH"; //$NON-NLS-1$
	private static final String PREFS_KEY_RECENT_QUERY = "RECENT_QUERY"; //$NON-NLS-1$
	private static final String PREFS_KEY_RESULT_FONT = "RESULT_FONT"; //$NON-NLS-1$
	private static final String PREFS_KEY_QUERY_FONT = "QUERY_FONT"; //$NON-NLS-1$

	private static final String VERSION = "0.0"; //$NON-NLS-1$

	private static final String BUNDLE_NAME = GreqlGui.class.getPackage()
			.getName() + ".resources.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private static final String DOCUMENT_EXTENSION = ".greql";
	private static final String GRAPH_EXTENSION = ".tg";

	private Graph graph;
	private JTabbedPane editorPane;
	private List<QueryEditorPanel> queries;

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
	private Action clearRecentGraphsAction;
	private Action evaluateQueryAction;
	private Action stopEvaluationAction;
	private Action enableOptimizerAction;
	private Action debugOptimizerAction;

	private JCheckBoxMenuItem enableOptimizerCheckBoxItem;
	private JCheckBoxMenuItem debugOptimizerCheckBoxItem;

	private boolean graphLoading;

	private boolean evaluating;

	private boolean resultFontSet;

	private Preferences prefs;

	private RecentFilesList recentQueryList;
	private RecentFilesList recentGraphList;
	private JMenu recentGraphsMenu;

	private Font queryFont;
	private Font resultFont;

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
				recentGraphList.rememberFile(file);
				graphLoading = false;
			} catch (Exception e1) {
				graph = null;
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
					updateActions();
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
			eval.setOptimize(enableOptimizerCheckBoxItem.isSelected());
			GreqlEvaluator.DEBUG_OPTIMIZATION = debugOptimizerCheckBoxItem
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
						resultFontSet = false;
						resultPane.setText(ex.getClass().getSimpleName() + ": "
								+ msg);
						setResultFont(resultFont);
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
							resultFontSet = false;
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
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		prefs = Preferences.userNodeForPackage(GreqlGui.class);

		loadSettings();

		initializeApplication();

		recentQueryList = new RecentFilesList(prefs, PREFS_KEY_RECENT_QUERY,
				10, recentFilesMenu) {
			@Override
			public void openRecentFile(File file) {
				openFile(file);
			}
		};

		recentGraphList = new RecentFilesList(prefs, PREFS_KEY_RECENT_GRAPH,
				10, recentGraphsMenu) {
			@Override
			public void openRecentFile(File file) {
				loadGraph(file);
			}
		};

		fd = new FileDialog(getApplicationName());
	}

	private void loadSettings() {
		String fontName = prefs
				.get(PREFS_KEY_QUERY_FONT, "Monospaced-plain-14"); //$NON-NLS-1$ //$NON-NLS-2$
		queryFont = Font.decode(fontName);
		if (queryFont == null) {
			queryFont = new Font("Monospaced", Font.PLAIN, 14); //$NON-NLS-1$
		}

		fontName = prefs.get(PREFS_KEY_RESULT_FONT, "Monospaced-plain-14"); //$NON-NLS-1$ //$NON-NLS-2$
		resultFont = Font.decode(fontName);
		if (resultFont == null) {
			resultFont = new Font("Monospaced", Font.PLAIN, 14); //$NON-NLS-1$
		}
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

		recentGraphsMenu.setEnabled(!graphLoading);

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
		enableOptimizerAction.setEnabled(!evaluating);
		debugOptimizerAction.setEnabled(!evaluating);

		setTitle(MessageFormat.format(
				getMessage("Application.mainwindow.title"), //$NON-NLS-1$ 
				getCurrentQuery() != null ? getCurrentQuery().getFileName()
						: getMessage("GreqlGui.NoQuery.Title"))); //$NON-NLS-1$
		if (getCurrentQuery() != null) {
			editorPane.setTitleAt(editorPane.getSelectedIndex(),
					(getCurrentQuery().isModified() ? "*" : "") //$NON-NLS-1$ //$NON-NLS-2$ 
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
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_L, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				loadGraph();
			}
		};

		clearRecentGraphsAction = new AbstractAction("Clear list") {
			@Override
			public void actionPerformed(ActionEvent e) {
				recentGraphList.clear();
			}
		};

		unloadGraphAction = new AbstractAction("Unload graph") {
			@Override
			public void actionPerformed(ActionEvent e) {
				unloadGraph();
			}
		};

		evaluateQueryAction = new AbstractAction("Evaluate query") {
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_R, menuEventMask));
			}

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

		enableOptimizerAction = new AbstractAction("Enable optimizer") {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};

		debugOptimizerAction = new AbstractAction("Debug optimizer") {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
	}

	@Override
	protected JMenuBar createMenuBar() {
		// TODO Auto-generated method stub
		JMenuBar mb = super.createMenuBar();
		JMenu graphMenu = new JMenu("Graph");
		graphMenu.add(loadGraphAction);
		recentGraphsMenu = new JMenu("Recent graphs");
		recentGraphsMenu.addSeparator();
		recentGraphsMenu.add(clearRecentGraphsAction);
		graphMenu.add(recentGraphsMenu);
		graphMenu.addSeparator();
		graphMenu.add(unloadGraphAction);
		mb.add(graphMenu, mb.getComponentIndex(helpMenu));

		JMenu queryMenu = new JMenu("Query");
		queryMenu.add(evaluateQueryAction);
		queryMenu.add(stopEvaluationAction);
		queryMenu.addSeparator();
		queryMenu.add(insertJavaQuotesAction);
		queryMenu.add(removeJavaQuotesAction);
		queryMenu.addSeparator();

		enableOptimizerCheckBoxItem = new JCheckBoxMenuItem(
				enableOptimizerAction);
		enableOptimizerCheckBoxItem.setSelected(true);
		queryMenu.add(enableOptimizerCheckBoxItem);

		debugOptimizerCheckBoxItem = new JCheckBoxMenuItem(debugOptimizerAction);
		queryMenu.add(debugOptimizerCheckBoxItem);

		mb.add(queryMenu, mb.getComponentIndex(graphMenu));
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
		resultPane.addPropertyChangeListener("page", //$NON-NLS-1$ 
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						if (!resultFontSet) {
							setResultFont(resultFont);
							resultFontSet = true;
							getStatusBar().setText("Result complete.");
						}
					}
				});

		resultScrollPane = new JScrollPane(resultPane);
		resultScrollPane.setPreferredSize(new Dimension(200, 200));

		brm = new DefaultBoundedRangeModel();
		progressBar = new JProgressBar();
		progressBar.setModel(brm);
		progressBar.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		if (RUNS_ON_MAC_OS_X) {
			progressBar.putClientProperty("JComponent.sizeVariant", "small"); //$NON-NLS-1$ //$NON-NLS-2$ 
		}

		consoleOutputArea = new JTextArea();
		JScrollPane consoleScrollPane = new JScrollPane(consoleOutputArea);
		consoleScrollPane.setPreferredSize(new Dimension(200, 200));

		System.setOut(new ConsoleOutputStream());
		// System.setErr(new ConsoleOutputStream());

		outputPane = new JTabbedPane();
		outputPane.addTab("Result", resultScrollPane);
		outputPane.addTab("Console", consoleScrollPane);

		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BorderLayout());

		queryPanel.add(editorPane, BorderLayout.CENTER);
		queryPanel.add(progressBar, BorderLayout.SOUTH);

		JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spl.add(queryPanel, JSplitPane.TOP);
		spl.add(outputPane, JSplitPane.BOTTOM);

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

	protected void openFile(File f) {
		// look for existing editor
		for (QueryEditorPanel q : queries) {
			if (f.equals(q.getQueryFile())) {
				editorPane.setSelectedComponent(q);
				return;
			}
		}
		try {
			if (getCurrentQuery() != null
					&& getCurrentQuery().getQueryFile() == null
					&& !getCurrentQuery().isModified()) {
				// re-use fresh editor
				getCurrentQuery().loadFromFile(f);
			} else {
				// create new editor
				QueryEditorPanel newQuery = new QueryEditorPanel(this, f);
				queries.add(newQuery);
				editorPane.addTab("", newQuery); //$NON-NLS-1$ 
				editorPane.setSelectedComponent(newQuery);
			}
			recentQueryList.rememberFile(f);
			setPrefString(PREFS_KEY_LAST_QUERY_DIRECTORY, f.getParentFile()
					.getCanonicalPath());
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		} finally {
			updateActions();
		}
	}

	protected boolean saveFile(File f) {
		try {
			getCurrentQuery().saveToFile(f);
			setPrefString(PREFS_KEY_LAST_QUERY_DIRECTORY, f.getParentFile()
					.getCanonicalPath());
			return true;
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
			return false;
		} finally {
			updateActions();
		}
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
		} finally {
			updateActions();
		}
	}

	@Override
	protected void fileOpen() {
		String lastQueryDirectory = getPrefString(
				PREFS_KEY_LAST_QUERY_DIRECTORY, System.getProperty("user.dir")); //$NON-NLS-1$ 
		fd.setDirectory(new File(lastQueryDirectory));
		File queryFile = fd.showFileOpenDialog(
				this,
				getMessage("GreqlGui.FileOpenDialog.Title"), //$NON-NLS-1$ 
				DOCUMENT_EXTENSION,
				getMessage("GreqlGui.FileOpenDialog.FilterName")); //$NON-NLS-1$ 
		if (queryFile != null) {
			openFile(queryFile);
		}
	}

	@Override
	protected boolean confirmClose() {
		if (!isModified()) {
			return true;
		}
		switch (JOptionPane.showConfirmDialog(this,
				getMessage("GreqlGui.ConfirmUnsaved"), //$NON-NLS-1$ 
				getMessage("Application.name"), //$NON-NLS-1$ 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
		case JOptionPane.YES_OPTION:
			if (getCurrentQuery().getQueryFile() != null) {
				return saveFile(getCurrentQuery().getQueryFile());
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
			QueryEditorPanel curr = getCurrentQuery();
			editorPane.remove(curr);
			queries.remove(curr);
		}
	}

	@Override
	protected void fileSave() {
		if (getCurrentQuery().getQueryFile() == null) {
			fileSaveAs();
		} else {
			saveFile(getCurrentQuery().getQueryFile());
		}
	}

	@Override
	protected boolean fileSaveAs() {
		File f = fd.showFileSaveAsDialog(this,
				getMessage("GreqlGui.FileSaveAsDialog.Title"), //$NON-NLS-1$ 
				DOCUMENT_EXTENSION, getCurrentQuery().getQueryFile());
		if (f == null) {
			return false;
		}
		return saveFile(f);
	}

	@Override
	protected void fileClearRecentFiles() {
		recentQueryList.clear();
	}

	@Override
	protected void editPreferences() {
		new SettingsDialog(this);
	}

	public void loadGraph(File f) {
		try {
			setPrefString(PREFS_KEY_LAST_GRAPH_DIRECTORY, f.getParentFile()
					.getCanonicalPath());
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		graphLoading = true;
		updateActions();
		new GraphLoader(brm, f).start();

	}

	public void loadGraph() {
		String lastDirectoryName = getPrefString(
				PREFS_KEY_LAST_GRAPH_DIRECTORY, System.getProperty("user.dir")); //$NON-NLS-1$ 
		fd.setDirectory(new File(lastDirectoryName));
		File graphFile = fd.showFileOpenDialog(this,
				getMessage("GreqlGui.GraphOpenDialog.Title"), GRAPH_EXTENSION, //$NON-NLS-1$ 
				getMessage("GreqlGui.GraphOpenDialog.FilterName")); //$NON-NLS-1$ 
		if (graphFile != null) {
			loadGraph(graphFile);
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

	@SuppressWarnings("deprecation")
	private void stopEvaluation() {
		if (evaluating) {
			evaluator.stop(); // this brutal brake is intended!
			evaluating = false;
			resultFontSet = false;
			evaluator = null;
			brm.setValue(brm.getMinimum());
			resultPane.setText("Query aborted.");
			setResultFont(resultFont);
			getStatusBar().setText("Query aborted.");
			updateActions();
		}
	}

	public String getEvaluateQueryShortcut() {
		KeyStroke ks = (KeyStroke) evaluateQueryAction
				.getValue(Action.ACCELERATOR_KEY);
		return getKeyStrokeAsString(ks);
	}

	private QueryEditorPanel getCurrentQuery() {
		return (QueryEditorPanel) editorPane.getSelectedComponent();
	}

	public String getFontName(Font font) {
		String style = (font.getStyle() == Font.PLAIN) ? "plain" : font //$NON-NLS-1$
				.getStyle() == Font.BOLD ? "bold" //$NON-NLS-1$
				: font.getStyle() == Font.ITALIC ? "italic" : "bolditalic"; //$NON-NLS-1$ //$NON-NLS-2$
		return font.getFamily() + "-" + style + "-" + font.getSize(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setQueryFont(Font font) {
		queryFont = font;
		for (QueryEditorPanel p : queries) {
			p.setQueryFont(queryFont);
		}
	}

	public void setResultFont(Font font) {
		resultFont = font;
		MutableAttributeSet attrs = resultPane.getInputAttributes();
		StyleConstants.setFontSize(attrs, resultFont.getSize());
		StyleConstants.setFontFamily(attrs, resultFont.getFamily());
		StyledDocument doc = resultPane.getStyledDocument();
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}

	public Font getQueryFont() {
		return queryFont;
	}

	public Font getResultFont() {
		return resultFont;
	}

	public void saveSettings() {
		if (queryFont == null) {
			prefs.remove(PREFS_KEY_QUERY_FONT);
		} else {
			prefs.put(PREFS_KEY_QUERY_FONT, getFontName(queryFont));
		}
		if (resultFont == null) {
			prefs.remove(PREFS_KEY_RESULT_FONT);
		} else {
			prefs.put(PREFS_KEY_RESULT_FONT, getFontName(resultFont));
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingApplication.invokeLater(new Runnable() {
			@Override
			public void run() {
				Locale.setDefault(Locale.ENGLISH);
				new GreqlGui().setVisible(true);
			}
		});
	}
}
