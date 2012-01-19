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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.gui.SwingApplication;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIO.TGFilenameFilter;
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

	private Graph graph;
	private JFileChooser fileChooser;
	private JPanel queryPanel;
	private JTextArea queryArea;
	private JEditorPane resultPane;
	private JTabbedPane tabPane;
	private JTextArea consoleOutputArea;
	private JButton fileSelectionButton;
	private JButton evalQueryButton;
	private JButton stopButton;
	private JButton fromJavaButton;
	private JButton toJavaButton;
	private JProgressBar progressBar;
	private BoundedRangeModel brm;
	private Evaluator evaluator;
	private JCheckBox optimizeCheckBox;
	private JCheckBox debugOptimizationCheckBox;
	private JScrollPane resultScrollPane;
	private UndoManager undoManager;

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
				ex = e1;
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
					fileSelectionButton.setEnabled(true);
					evalQueryButton.setEnabled(graph != null);
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
					stopButton.setEnabled(false);
					evalQueryButton.setEnabled(true);
					fileSelectionButton.setEnabled(true);
					if (ex != null) {
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
								if (sp.get_offset() >= 0
										&& sp.get_length() >= 0) {
									queryArea
											.setSelectionStart(sp.get_offset());
									queryArea.setSelectionEnd(sp.get_offset()
											+ sp.get_length());
								}
							}
						} else if (ex instanceof ParsingException) {
							ParsingException pe = (ParsingException) ex;
							if (pe.getOffset() >= 0 && pe.getLength() >= 0) {
								queryArea.setSelectionStart(pe.getOffset());
								queryArea.setSelectionEnd(pe.getOffset()
										+ pe.getLength());
							}
						}
						resultPane.setText(ex.getClass().getSimpleName() + ": "
								+ msg);
						// JOptionPane.showMessageDialog(GreqlGui.this, msg,
						// ex.getClass().getSimpleName(),
						// JOptionPane.ERROR_MESSAGE);
						queryArea.requestFocus();
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
							resultPane.setPage(new URL("file", "localhost",
									resultFile.getCanonicalPath()));
							tabPane.setSelectedComponent(resultScrollPane);
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
	}

	private class ConsoleOutputStream extends PrintStream {
		public ConsoleOutputStream() {
			super(new ByteArrayOutputStream());
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			final String aString = new String(buf, off, len);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						consoleOutputArea.append(aString);
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}

	@Override
	protected void editUndo() {
		undoManager.undo();
		updateActions();
	}

	@Override
	protected void editRedo() {
		undoManager.redo();
		updateActions();
	}

	@Override
	protected void editCut() {
		queryArea.cut();
		updateActions();
	}

	@Override
	protected void editCopy() {
		queryArea.copy();
		updateActions();
	}

	@Override
	protected void editPaste() {
		queryArea.paste();
		updateActions();
	}

	@Override
	protected boolean confirmClose() {
		return true;
	}

	@Override
	protected void updateActions() {
		editUndoAction.setEnabled(undoManager.canUndo());
		editRedoAction.setEnabled(undoManager.canRedo());
	}

	@Override
	protected Component createContent() {
		queryArea = new JTextArea(15, 50);
		queryArea.setEditable(true);
		queryArea.setFont(new java.awt.Font("Monaco", java.awt.Font.PLAIN, 13));

		// Start code for undo/redo ---------------
		undoManager = new UndoManager();
		undoManager.setLimit(10000);
		Document doc = queryArea.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undoManager.addEdit(evt.getEdit());
				updateActions();
				setModified(true);
			}
		});

		queryArea.setText("// Please enter your query here!");
		JScrollPane queryScrollPane = new JScrollPane(queryArea);

		queryPanel = new JPanel();
		queryPanel.setLayout(new BorderLayout(4, 4));
		queryPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		queryPanel.add(queryScrollPane, BorderLayout.CENTER);

		resultPane = new JEditorPane(
				"text/html; charset=UTF-8",
				"<html>Here the <b>query results</b> will be shown. "
						+ "Simply select a graph, type a query and press the evaluation button."
						+ "</html>");
		resultPane.setEditable(false);

		// install property change listener to update status label
		resultPane.addPropertyChangeListener("page",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent pce) {
						getStatusBar().setText("Result complete.");
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
		System.setErr(new ConsoleOutputStream());

		tabPane = new JTabbedPane();
		tabPane.addTab("Console", consoleScrollPane);
		tabPane.addTab("Result", resultScrollPane);

		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(TGFilenameFilter.instance());

		fileSelectionButton = new JButton(new AbstractAction("Select Graph") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Preferences prefs = Preferences
						.userNodeForPackage(GreqlGui.class);
				String lastDirectoryName = prefs.get("LAST_DIRECTORY",
						System.getProperty("user.dir"));
				File lastDir = new File(lastDirectoryName);
				if (lastDir.isDirectory() && lastDir.canRead()) {
					fileChooser.setCurrentDirectory(lastDir);
				} else {
					lastDir = new File(System.getProperty("user.dir"));
					if (lastDir.isDirectory() && lastDir.canRead()) {
						fileChooser.setCurrentDirectory(lastDir);
					}
				}
				int returnVal = fileChooser.showOpenDialog(fileSelectionButton);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileSelectionButton.setEnabled(false);
					evalQueryButton.setEnabled(false);
					getStatusBar().setText("Compiling schema...");
					new GraphLoader(brm, fileChooser.getSelectedFile()).start();
					try {
						prefs.put("LAST_DIRECTORY", fileChooser
								.getCurrentDirectory().getCanonicalPath());
					} catch (IOException e1) {
					}
				}
			}
		});

		evalQueryButton = new JButton(new AbstractAction("Evaluate Query") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				fileSelectionButton.setEnabled(false);
				evalQueryButton.setEnabled(false);
				brm.setValue(brm.getMinimum());
				evaluator = new Evaluator(brm, queryArea.getText());
				evaluator.start();
				stopButton.setEnabled(true);
			}

		});
		// evalQueryButton.setEnabled(false);

		optimizeCheckBox = new JCheckBox("Enable optimizer");
		optimizeCheckBox.setSelected(true);

		debugOptimizationCheckBox = new JCheckBox("Debug optimization");
		debugOptimizationCheckBox.setSelected(false);

		stopButton = new JButton(new AbstractAction("Stop evaluation") {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				evaluator.stop(); // this brutal brake is intended!
				stopButton.setEnabled(false);
				fileSelectionButton.setEnabled(true);
				evalQueryButton.setEnabled(true);
				evaluator = null;
				brm.setValue(brm.getMinimum());
				getStatusBar().setText("Query aborted.");
			}

		});
		stopButton.setEnabled(false);

		fromJavaButton = new JButton("Remove Java Quotes");
		fromJavaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = queryArea.getText();
				String[] lines = text.split("\n");
				StringBuilder sb = new StringBuilder();
				for (String line : lines) {
					String[] strings = line.split("\" \\+");
					for (String s : strings) {
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
				queryArea.requestFocus();
			}
		});

		toJavaButton = new JButton("Insert Java Quotes");
		toJavaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
								.append(spaceRequired && !startsWithWs ? " "
										: "").append(line).append("\"");
					}
					spaceRequired = line.length() > 0
							&& !Character.isWhitespace(line.charAt(line
									.length() - 1));
				}
				text = sb.toString();
				queryArea.setText(text);
				queryArea.select(0, text.length());
				queryArea.copy();
				queryArea.requestFocus();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(fileSelectionButton);
		buttonPanel.add(evalQueryButton);
		buttonPanel.add(optimizeCheckBox);
		buttonPanel.add(debugOptimizationCheckBox);
		buttonPanel.add(stopButton);
		buttonPanel.add(fromJavaButton);
		buttonPanel.add(toJavaButton);
		queryPanel.add(buttonPanel, BorderLayout.SOUTH);

		JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		spl.add(queryPanel, JSplitPane.TOP);
		spl.add(tabPane, JSplitPane.BOTTOM);

		// Don't allow shrinking so that buttons get invisible
		spl.setMinimumSize(new Dimension(
				buttonPanel.getPreferredSize().width + 10, 450));

		queryArea.setSelectionStart(0);
		queryArea.setSelectionEnd(queryArea.getText().length());
		queryArea.requestFocus();
		setModified(false);
		return spl;
	}

	@Override
	public String getVersion() {
		return VERSION;
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
