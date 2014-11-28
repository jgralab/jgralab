/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
import java.util.List;
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
import javax.swing.text.html.HTMLDocument;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.gui.FontSelectionDialog;
import de.uni_koblenz.ist.utilities.gui.RecentFilesList;
import de.uni_koblenz.ist.utilities.gui.StringListPreferences;
import de.uni_koblenz.ist.utilities.gui.SwingApplication;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.exception.EvaluationInterruptedException;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.exception.ParsingException;
import de.uni_koblenz.jgralab.greql.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizerInfo;
import de.uni_koblenz.jgralab.greql.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql.serialising.GreqlSerializer;
import de.uni_koblenz.jgralab.greql.serialising.HTMLOutputWriter;
import de.uni_koblenz.jgralab.greql.serialising.XMLOutputWriter;

@SuppressWarnings("serial")
public class GreqlGui extends SwingApplication {
	// keys for preferences
	private static final String PREFS_KEY_LAST_QUERY_DIRECTORY = "LAST_QUERY_DIRECTORY"; //$NON-NLS-1$
	private static final String PREFS_KEY_LAST_GRAPH_DIRECTORY = "LAST_GRAPH_DIRECTORY"; //$NON-NLS-1$
	private static final String PREFS_KEY_RECENT_GRAPH = "RECENT_GRAPH"; //$NON-NLS-1$
	private static final String PREFS_KEY_RECENT_QUERY = "RECENT_QUERY"; //$NON-NLS-1$
	private static final String PREFS_KEY_RESULT_FONT = "RESULT_FONT"; //$NON-NLS-1$
	private static final String PREFS_KEY_QUERY_FONT = "QUERY_FONT"; //$NON-NLS-1$
	private static final String PREFS_KEY_GENERIC_IMPL = "GENERIC_IMPL"; //$NON-NLS-1$
	private static final String PREFS_KEY_ENABLE_OPTIMIZER = "ENABLE_OPTIMIZER"; //$NON-NLS-1$
	private static final String PREFS_KEY_DEBUG_OPTIMIZER = "DEBUG_OPTIMIZER"; //$NON-NLS-1$
	private static final String PREFS_KEY_GREQL_FUNCTIONS = "GREQL_FUNCTION"; //$NON-NLS-1$

	private static final String VERSION = "0.0"; //$NON-NLS-1$

	private static final String BUNDLE_NAME = GreqlGui.class.getPackage()
			.getName() + ".resources.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private static final String DOCUMENT_EXTENSION = ".greql"; //$NON-NLS-1$
	private static final String GRAPH_EXTENSION = ".tg"; //$NON-NLS-1$

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

	private final FileDialog fd;

	private Action insertJavaQuotesAction;
	private Action removeJavaQuotesAction;

	private Action loadGraphAction;
	private Action unloadGraphAction;
	private Action genericImplementaionAction;
	private Action clearRecentGraphsAction;
	private Action evaluateQueryAction;
	private Action showQueryGraphAction;
	private Action stopEvaluationAction;
	private Action enableOptimizerAction;
	private Action debugOptimizerAction;

	private JCheckBoxMenuItem enableOptimizerCheckBoxItem;
	private JCheckBoxMenuItem debugOptimizerCheckBoxItem;
	private JCheckBoxMenuItem genericImplementationCheckBoxItem;

	private boolean graphLoading;

	private boolean evaluating;
	private double parseTime;
	private double evaluationTime;

	private final Preferences prefs;

	private final RecentFilesList recentQueryList;
	private final RecentFilesList recentGraphList;
	private final StringListPreferences greqlFunctionList;
	private JMenu recentGraphsMenu;

	private Font queryFont;
	private Font resultFont;

	class Worker extends Thread implements ProgressFunction {
		BoundedRangeModel brm;
		private long totalElements;
		Exception ex;

		Worker(String threadName, BoundedRangeModel brm) {
			super(threadName);
			this.brm = brm;
		}

		protected void finish() {
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(false);
			brm.setValue(brm.getMinimum());
		}

		@Override
		public void finished() {
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					finish();
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
			invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setIndeterminate(false);
					progressBar.setStringPainted(true);
					brm.setValue(brm.getValue() + 1);
				}
			});
		}
	}

	class GreqlFunctionLoader extends Worker {
		boolean errors;

		GreqlFunctionLoader(BoundedRangeModel brm) {
			super("GreqlFunctionLoaderThread", brm);
		}

		@Override
		public void run() {
			init(greqlFunctionList.size());
			try {
				errors = false;
				for (String className : greqlFunctionList.getEntries()) {
					try {
						FunLib.register(className);
					} catch (GreqlException e) {
						System.err
								.println(MessageFormat
										.format(getMessage("GreqlGui.StatusMessage.FunctionGreqlException"),
												className, e.getMessage()));
					} catch (ClassNotFoundException e1) {
						errors = true;
						System.err
								.println(MessageFormat
										.format(getMessage("GreqlGui.StatusMessage.FunctionNotFound"),
												className));
					} catch (ClassCastException e2) {
						errors = true;
						System.err
								.println(MessageFormat
										.format(getMessage("GreqlGui.StatusMessage.FunctionWrongType"),
												className));
					}
					progress(1);
				}
			} finally {
				finished();
			}
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getStatusBar()
							.setText(
									getMessage("GreqlGui.StatusMessage.FunctionsLoading")); //$NON-NLS-1$
				}
			});
		}

		@Override
		public void finished() {
			super.finished();
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getStatusBar()
							.setText(
									getMessage(errors ? "GreqlGui.StatusMessage.FunctionsLoadedWithErrors" //$NON-NLS-1$
											: "GreqlGui.StatusMessage.FunctionsLoaded")); //$NON-NLS-1$
				}
			});
		}
	}

	class GraphLoader extends Worker {
		private final File file;

		GraphLoader(BoundedRangeModel brm, File file) {
			super("GraphLoaderThread", brm);
			this.file = file;
		}

		@Override
		public void run() {
			try {
				progressBar.setIndeterminate(true);
				graph = GraphIO
						.loadGraphFromFile(
								file.getCanonicalPath(),
								genericImplementationCheckBoxItem.isSelected() ? ImplementationType.GENERIC
										: ImplementationType.STANDARD, this);
				recentGraphList.rememberFile(file);
			} catch (Exception e1) {
				brm.setValue(brm.getMinimum());
				progressBar.setIndeterminate(false);
				graph = null;
				ex = e1;
			} finally {
				graphLoading = false;
			}
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (graph == null) {
						// TODO externalize string
						String msg = "Can't load "; //$NON-NLS-1$
						try {
							msg += file.getCanonicalPath() + "\n" //$NON-NLS-1$
									+ ex.getMessage();
						} catch (IOException e) {
							msg += "graph\n"; //$NON-NLS-1$
						}
						Throwable cause = ex.getCause();
						if (cause != null) {
							msg += "\ncaused by " + cause; //$NON-NLS-1$
						}
						JOptionPane.showMessageDialog(GreqlGui.this, msg, ex
								.getClass().getSimpleName(),
								JOptionPane.ERROR_MESSAGE);
						getStatusBar()
								.setText(
										getMessage("GreqlGui.StatusMessage.GraphLoadingFailed")); //$NON-NLS-1$
					} else {
						getStatusBar()
								.setText(
										MessageFormat
												.format(getMessage("GreqlGui.StatusMessage.GraphLoadingFinished"), //$NON-NLS-1$
														graph.getId()));
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
					getStatusBar().setText(
							getMessage("GreqlGui.StatusMessage.GraphLoading")); //$NON-NLS-1$
				}
			});

		}
	}

	class Evaluator extends Worker {
		private final String queryString;
		private GreqlQuery query;
		private Object queryResult;

		Evaluator(BoundedRangeModel brm, String query) {
			super("EvaluatorThread", brm);
			this.queryString = query;
		}

		@Override
		public void run() {
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					progressBar.setIndeterminate(true);
				}
			});
			queryResult = null;
			try {
				parseTime = System.currentTimeMillis();
				GreqlQueryImpl.DEBUG_OPTIMIZATION = debugOptimizerCheckBoxItem
						.isSelected();
				try {
					query = GreqlQuery
							.createQuery(
									queryString,
									enableOptimizerCheckBoxItem.isSelected() ? new DefaultOptimizer(
											new DefaultOptimizerInfo(
													graph != null ? graph
															.getSchema() : null))
											: null);
				} catch (Exception e1) {
					ex = e1;
				}
				if (ex == null) {
					evaluationTime = System.currentTimeMillis();
					parseTime = (evaluationTime - parseTime) / 1000.0;
					try {
						queryResult = query.evaluate(graph,
								new GreqlEnvironmentAdapter(), this);
						System.out.println("done");
					} catch (Exception e1) {
						ex = e1;
					} finally {
						evaluationTime = (System.currentTimeMillis() - evaluationTime) / 1000.0;
					}
				}
				if (ex != null
						&& !(ex instanceof EvaluationInterruptedException)) {
					invokeAndWait(new Runnable() {
						// update statusbar and handle exceptions
						@Override
						public void run() {
							evaluating = false;
							brm.setValue(brm.getMinimum());
							getStatusBar()
									.setText(
											getMessage("GreqlGui.StatusMessage.EvaluationFailed")); //$NON-NLS-1$
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
								List<SourcePosition> spl = qs
										.getSourcePositions();
								if (spl.size() > 0) {
									SourcePosition sp = spl.get(0);
									getCurrentQuery().setSelection(
											sp.get_offset(), sp.get_length());
								}
							} else if (ex instanceof ParsingException) {
								ParsingException pe = (ParsingException) ex;
								getCurrentQuery().setSelection(pe.getOffset(),
										pe.getLength());
							}
							resultPane.setText(ex.getClass().getSimpleName()
									+ ": " //$NON-NLS-1$
									+ msg);
							setResultFont(resultFont);
							updateActions();
						}
					});
				}
				if (ex == null) {
					invokeAndWait(new Runnable() {
						// save and display result
						@Override
						public void run() {
							getStatusBar()
									.setText(
											MessageFormat
													.format(getMessage("GreqlGui.StatusMessage.EvaluationFinished"), evaluationTime)); //$NON-NLS-1$
							evaluating = false;
							updateActions();
							try {
								File xmlResultFile = new File(
										"greqlQueryResult.xml"); //$NON-NLS-1$
								XMLOutputWriter xw = new XMLOutputWriter(graph);
								xw.writeValue(queryResult, xmlResultFile);
							} catch (SerialisingException e) {
								JOptionPane.showMessageDialog(GreqlGui.this,
										"Exception during XML output of result: " //$NON-NLS-1$
												+ e.toString());
							} catch (XMLStreamException e) {
								JOptionPane.showMessageDialog(GreqlGui.this,
										"Exception during XML output of result: " //$NON-NLS-1$
												+ e.toString());
							}
							try {
								File resultFile = new File(
										"greqlQueryResult.html"); //$NON-NLS-1$
								// File resultFile = File.createTempFile(
								// "greqlQueryResult", ".html");
								// resultFile.deleteOnExit();
								HTMLOutputWriter w = new HTMLOutputWriter(graph);
								w.setUseCss(false);
								w.writeValue(queryResult, resultFile);
								Document doc = resultPane.getDocument();
								doc.putProperty(
										Document.StreamDescriptionProperty,
										null);
								outputPane
										.setSelectedComponent(resultScrollPane);
								resultPane.setPage(new URL("file", "localhost", //$NON-NLS-1$ //$NON-NLS-2$
										resultFile.getCanonicalPath()));
							} catch (SerialisingException e) {
								// TODO externalize string
								JOptionPane.showMessageDialog(GreqlGui.this,
										"Exception during HTML output of result: " //$NON-NLS-1$
												+ e.toString());
							} catch (IOException e) {
								// TODO externalize string
								JOptionPane.showMessageDialog(GreqlGui.this,
										"Exception during HTML output of result: " //$NON-NLS-1$
												+ e.toString());
							}
						}
					});
				}
			} finally {
				finished();
			}
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getStatusBar().setText(
							getMessage("GreqlGui.StatusMessage.Evaluating")); //$NON-NLS-1$
				}
			});
		}

		@Override
		public void finish() {
			super.finish();
			evaluator = null;
			evaluating = false;
			if (ex != null && ex instanceof EvaluationInterruptedException) {
				resultPane
						.setText(getMessage("GreqlGui.StatusMessage.QueryAborted")); //$NON-NLS-1$
				setResultFont(resultFont);
				getStatusBar().setText(
						getMessage("GreqlGui.StatusMessage.QueryAborted")); //$NON-NLS-1$
			}
			updateActions();
		}
	}

	public GreqlGui() {
		super(RESOURCE_BUNDLE);
		prefs = Preferences.userNodeForPackage(GreqlGui.class);
		loadFontSettings();
		initializeApplication();
		loadCheckBoxSettings();

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

		greqlFunctionList = new StringListPreferences(prefs,
				PREFS_KEY_GREQL_FUNCTIONS);
		greqlFunctionList.load();
		loadGreqlFunctions();
	}

	public boolean isGraphLoading() {
		return graphLoading;
	}

	public boolean isEvaluating() {
		return evaluating;
	}

	private void loadFontSettings() {
		String fontName = prefs
				.get(PREFS_KEY_QUERY_FONT, "Monospaced-plain-14"); //$NON-NLS-1$
		queryFont = Font.decode(fontName);
		if (queryFont == null) {
			queryFont = new Font("Monospaced", Font.PLAIN, 14); //$NON-NLS-1$
		}

		fontName = prefs.get(PREFS_KEY_RESULT_FONT, "Monospaced-plain-14"); //$NON-NLS-1$
		resultFont = Font.decode(fontName);
		if (resultFont == null) {
			resultFont = new Font("Monospaced", Font.PLAIN, 14); //$NON-NLS-1$
		}
	}

	private void loadCheckBoxSettings() {
		enableOptimizerCheckBoxItem.setSelected(prefs.getBoolean(
				PREFS_KEY_ENABLE_OPTIMIZER, true));

		debugOptimizerCheckBoxItem.setSelected(prefs.getBoolean(
				PREFS_KEY_DEBUG_OPTIMIZER, false));

		genericImplementationCheckBoxItem.setSelected(prefs.getBoolean(
				PREFS_KEY_GENERIC_IMPL, true));
	}

	private class ConsoleOutputStream extends PrintStream {
		public ConsoleOutputStream() {
			super(new ByteArrayOutputStream());
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			final String aString = new String(buf, off, len);
			invokeLater(new Runnable() {
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
	protected void updateActions() {
		setModified((getCurrentQuery() != null)
				&& getCurrentQuery().isModified());

		fileCloseAction.setEnabled(getCurrentQuery() != null);
		fileSaveAction.setEnabled((getCurrentQuery() != null) && isModified());
		fileSaveAsAction.setEnabled(getCurrentQuery() != null);
		filePrintAction.setEnabled(false);

		recentGraphsMenu.setEnabled(!graphLoading);

		editUndoAction.setEnabled((getCurrentQuery() != null)
				&& getCurrentQuery().canUndo());
		editRedoAction.setEnabled((getCurrentQuery() != null)
				&& getCurrentQuery().canRedo());
		editCutAction.setEnabled(getCurrentQuery() != null);
		editCopyAction.setEnabled(getCurrentQuery() != null);
		editPasteAction.setEnabled(getCurrentQuery() != null);

		insertJavaQuotesAction.setEnabled(getCurrentQuery() != null);
		removeJavaQuotesAction.setEnabled(getCurrentQuery() != null);

		loadGraphAction.setEnabled(!evaluating && !graphLoading);
		unloadGraphAction.setEnabled(!evaluating && !graphLoading
				&& (graph != null));
		evaluateQueryAction.setEnabled((getCurrentQuery() != null)
				&& !evaluating && !graphLoading);
		showQueryGraphAction.setEnabled((getCurrentQuery() != null)
				&& !evaluating && !graphLoading);
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
		super.createActions();
		loadGraphAction = new AbstractAction(
				getMessage("GreqlGui.Action.LoadGraph")) { //$NON-NLS-1$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_L, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				loadGraph();
			}
		};

		clearRecentGraphsAction = new AbstractAction(
				getMessage("GreqlGui.Action.ClearRecentGraphList")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				recentGraphList.clear();
			}
		};

		unloadGraphAction = new AbstractAction(
				getMessage("GreqlGui.Action.UnloadGraph")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				unloadGraph();
			}
		};

		genericImplementaionAction = new AbstractAction(
				getMessage("GreqlGui.Action.GenericImplementation")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCheckBoxSettings();
			}
		};
		evaluateQueryAction = new AbstractAction(
				getMessage("GreqlGui.Action.EvaluateQuery")) { //$NON-NLS-1$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_R, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				evaluateQuery();
			}
		};

		showQueryGraphAction = new AbstractAction(
				getMessage("GreqlGui.Action.ShowQueryGraph")) { //$NON-NLS-1$
			{
				putValue(AbstractAction.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_G, menuEventMask));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				showQueryGraph();
			}
		};

		stopEvaluationAction = new AbstractAction(
				getMessage("GreqlGui.Action.StopEvaluation")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				stopEvaluation();
			}
		};

		insertJavaQuotesAction = new AbstractAction(
				getMessage("GreqlGui.Action.InsertJavaQuotes")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				insertJavaQuotes();
			}
		};

		removeJavaQuotesAction = new AbstractAction(
				getMessage("GreqlGui.Action.RemoveJavaQuotes")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				removeJavaQuotes();
			}
		};

		enableOptimizerAction = new AbstractAction(
				getMessage("GreqlGui.Action.EnableOptimizer")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCheckBoxSettings();
			}
		};

		debugOptimizerAction = new AbstractAction(
				getMessage("GreqlGui.Action.DebugOptimizer")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCheckBoxSettings();
			}
		};
	}

	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = super.createMenuBar();

		JMenu graphMenu = new JMenu(getMessage("GreqlGui.Menu.Graph")); //$NON-NLS-1$
		graphMenu.add(loadGraphAction);
		recentGraphsMenu = new JMenu(getMessage("GreqlGui.Menu.RecentGraphs")); //$NON-NLS-1$
		recentGraphsMenu.addSeparator();
		recentGraphsMenu.add(clearRecentGraphsAction);
		graphMenu.add(recentGraphsMenu);
		graphMenu.addSeparator();
		genericImplementationCheckBoxItem = new JCheckBoxMenuItem(
				genericImplementaionAction);
		graphMenu.add(genericImplementationCheckBoxItem);
		graphMenu.add(unloadGraphAction);
		mb.add(graphMenu, mb.getComponentIndex(helpMenu));

		JMenu queryMenu = new JMenu(getMessage("GreqlGui.Menu.Query")); //$NON-NLS-1$
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

		queryMenu.add(showQueryGraphAction);

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
		queries = new ArrayList<>();
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
						setResultFont(resultFont);
						getStatusBar()
								.setText(
										MessageFormat
												.format(getMessage("GreqlGui.StatusMessage.ResultComplete"), //$NON-NLS-1$
														parseTime,
														evaluationTime));
					}
				});

		resultScrollPane = new JScrollPane(resultPane);
		resultScrollPane.setPreferredSize(new Dimension(200, 200));

		brm = new DefaultBoundedRangeModel();
		progressBar = new JProgressBar();
		progressBar.setModel(brm);
		progressBar.setStringPainted(true);
		progressBar.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		if (RUNS_ON_MAC_OS_X) {
			progressBar.putClientProperty("JComponent.sizeVariant", "small"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		consoleOutputArea = new JTextArea();
		JScrollPane consoleScrollPane = new JScrollPane(consoleOutputArea);
		consoleScrollPane.setPreferredSize(new Dimension(200, 200));

		System.setOut(new ConsoleOutputStream());
		System.setErr(new ConsoleOutputStream());

		outputPane = new JTabbedPane();
		outputPane
				.addTab(getMessage("GreqlGui.Result.Title"), resultScrollPane); //$NON-NLS-1$
		outputPane.addTab(
				getMessage("GreqlGui.Console.Title"), consoleScrollPane); //$NON-NLS-1$

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
			if ((getCurrentQuery() != null)
					&& (getCurrentQuery().getQueryFile() == null)
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
			recentQueryList.rememberFile(f);
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
			editorPane.addTab("", newQuery); //$NON-NLS-1$
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
		new SettingsDialog(this, queryFont, resultFont,
				greqlFunctionList.getEntries());
	}

	private void loadGraph(File f) {
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

	public synchronized Graph getGraph() {
		return graph;
	}

	private void loadGreqlFunctions() {
		if (greqlFunctionList.size() == 0) {
			return;
		}
		new GreqlFunctionLoader(brm).start();
	}

	private void loadGraph() {
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

	private void showQueryGraph() {
		String queryString = getCurrentQuery().getText();
		GreqlQuery query = GreqlQuery
				.createQuery(
						queryString,
						enableOptimizerCheckBoxItem.isSelected() ? new DefaultOptimizer(
								new DefaultOptimizerInfo(graph != null ? graph
										.getSchema() : null)) : null);
		new GraphViewer(this, query.getQueryGraph(),
				GreqlSerializer.serializeGraph(query.getQueryGraph()))
				.setVisible(true);
	}

	private synchronized void unloadGraph() {
		graph = null;
		getStatusBar().setText(
				getMessage("GreqlGui.StatusMessage.GraphUnloaded")); //$NON-NLS-1$
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
			evaluator.interrupt();
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

	private void setQueryFont(Font font) {
		queryFont = font;
		for (QueryEditorPanel p : queries) {
			p.setQueryFont(queryFont);
		}
	}

	public Font getQueryFont() {
		return queryFont;
	}

	private void setResultFont(Font font) {
		resultFont = font;
		resultPane.setContentType("text/html");
		String bodyRule = "body { font-family: " + font.getFamily() + "; "
				+ "font-size: " + font.getSize() + "pt; }";
		((HTMLDocument) resultPane.getDocument()).getStyleSheet().addRule(
				bodyRule);
		resultPane.setCaretPosition(0);
	}

	public void saveSettings(SettingsDialog d) {
		setQueryFont(d.getQueryFont());
		if (queryFont == null) {
			prefs.remove(PREFS_KEY_QUERY_FONT);
		} else {
			prefs.put(PREFS_KEY_QUERY_FONT,
					FontSelectionDialog.getInternalFontName(queryFont));
		}
		setResultFont(d.getResultFont());
		if (resultFont == null) {
			prefs.remove(PREFS_KEY_RESULT_FONT);
		} else {
			prefs.put(PREFS_KEY_RESULT_FONT,
					FontSelectionDialog.getInternalFontName(resultFont));
		}
		greqlFunctionList.setEntries(d.getGreqlFunctionList());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveCheckBoxSettings() {
		prefs.putBoolean(PREFS_KEY_DEBUG_OPTIMIZER,
				debugOptimizerCheckBoxItem.isSelected());

		prefs.putBoolean(PREFS_KEY_ENABLE_OPTIMIZER,
				enableOptimizerCheckBoxItem.isSelected());

		prefs.putBoolean(PREFS_KEY_GENERIC_IMPL,
				genericImplementationCheckBoxItem.isSelected());
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
				new GreqlGui().setVisible(true);
			}
		});
	}
}
