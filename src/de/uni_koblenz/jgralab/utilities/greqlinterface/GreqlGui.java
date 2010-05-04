package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueHTMLOutputVisitor;

@WorkInProgress(description = "insufficcient result presentation, simplistic hacked GUI, no load/save functionality, ...", responsibleDevelopers = "horn")
public class GreqlGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private Graph graph;
	private JFileChooser fileChooser;
	private JPanel queryPanel, resultPanel;
	private JTextArea queryArea;
	private JEditorPane resultPane;
	private JButton fileSelectionButton;
	private JButton evalQueryButton;
	private JButton stopButton;
	private JProgressBar progressBar;
	private BoundedRangeModel brm;
	private JLabel statusLabel;
	private Evaluator evaluator;
	private JCheckBox optimizeCheckBox;

	class Worker extends Thread implements ProgressFunction {
		BoundedRangeModel brm;
		private long totalElements;
		Exception ex;

		Worker(BoundedRangeModel brm) {
			this.brm = brm;
		}

		@Override
		public void finished() {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						brm.setValue(brm.getMaximum());
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}

		@Override
		public long getUpdateInterval() {
			return brm.getMaximum() > totalElements ? 1 : totalElements
					/ brm.getMaximum();
		}

		@Override
		public void init(long totalElements) {
			this.totalElements = totalElements;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						brm.setValue(brm.getMinimum());
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}

		@Override
		public void progress(long processedElements) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if (brm.getValue() < brm.getMaximum()) {
							brm.setValue(brm.getValue() + 1);
						}
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
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
				graph = GraphIO.loadSchemaAndGraphFromFile(file
						.getCanonicalPath(),
						CodeGeneratorConfiguration.WITHOUT_TRANSACTIONS, this);
			} catch (Exception e1) {
				graph = null;
				ex = e1;
			}
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if (graph == null) {
							JOptionPane.showMessageDialog(null,
									ex.getMessage(), ex.getClass()
											.getSimpleName(),
									JOptionPane.ERROR_MESSAGE);
							statusLabel.setText("Couldn't load graph :-(");
						} else {
							statusLabel.setText("Graph '" + graph.getId()
									+ "' loaded.");
						}
						fileSelectionButton.setEnabled(true);
						evalQueryButton.setEnabled(graph != null);
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						statusLabel.setText("Loading graph...");
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
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
			final GreqlEvaluator eval = new GreqlEvaluator(query, graph, null,
					this);
			eval.setOptimize(optimizeCheckBox.isSelected());
			try {
				// if (eval.parseQuery()) {
				// assert eval.getSyntaxGraph() != null;
				// Tg2Dot t2d = new Tg2Dot();
				// t2d.setGraph(eval.getSyntaxGraph());
				// t2d.setPrintEdgeAttributes(true);
				// t2d.setPrintReversedEdges(true);
				// t2d.setOutputFile("query.greql.dot");
				// t2d.printGraph();
				// }
				eval.startEvaluation();
			} catch (Exception e1) {
				ex = e1;
			}
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						stopButton.setEnabled(false);
						if (ex != null) {
							statusLabel.setText("Couldn't evaluate query :-(");
						} else {
							statusLabel
									.setText("Evaluation finished, loading result - this may take a while...");
						}
					}
				});
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (ex == null) {
							JValue result = eval.getEvaluationResult();
							File resultFile;
							try {
								resultFile = File.createTempFile(
										"greqlQueryResult", ".html");
								new JValueHTMLOutputVisitor(result, resultFile
										.getCanonicalPath(), graph);
								resultPane.setPage(new URL("file", "localhost",
										resultFile.getCanonicalPath()));
								statusLabel.setText("Ready.");
							} catch (IOException e) {
							}
						} else {
							String msg = ex.getMessage();
							if (msg == null) {
								msg = "An exception occured!";
							}
							JOptionPane.showMessageDialog(null, msg, ex
									.getClass().getSimpleName(),
									JOptionPane.ERROR_MESSAGE);

						}
						fileSelectionButton.setEnabled(true);
						evalQueryButton.setEnabled(true);
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}

		@Override
		public void init(long totalElements) {
			super.init(totalElements);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						statusLabel.setText("Evaluating query...");
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}

	public GreqlGui() {
		super("GReQL GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		queryArea = new JTextArea(15, 50);
		queryArea.setEditable(true);
		queryArea.setText("/*\n * Please enter your query here!\n */\n\n");
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
		resultPane.setMinimumSize(new Dimension(200, 200));
		resultPane.setPreferredSize(resultPane.getMinimumSize());
		JScrollPane resultScrollPane = new JScrollPane(resultPane);

		brm = new DefaultBoundedRangeModel();
		progressBar = new JProgressBar();
		progressBar.setModel(brm);

		resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout(4, 4));
		resultPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		resultPanel.add(resultScrollPane, BorderLayout.CENTER);
		resultPanel.add(progressBar, BorderLayout.SOUTH);

		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()
						|| Pattern.compile("\\.[Tt][Gg]$").matcher(f.getName())
								.find()) {
					return true;
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "TG Files";
			}

		});

		fileSelectionButton = new JButton(new AbstractAction("Select Graph") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileChooser.showOpenDialog(fileSelectionButton);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileSelectionButton.setEnabled(false);
					evalQueryButton.setEnabled(false);
					statusLabel.setText("Compling schema...");
					new GraphLoader(brm, fileChooser.getSelectedFile()).start();
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
		evalQueryButton.setEnabled(false);

		optimizeCheckBox = new JCheckBox("Enable optimizer");
		optimizeCheckBox.setSelected(true);

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
				statusLabel.setText("Query aborted.");
			}

		});
		stopButton.setEnabled(false);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(fileSelectionButton);
		buttonPanel.add(evalQueryButton);
		buttonPanel.add(optimizeCheckBox);
		buttonPanel.add(stopButton);
		queryPanel.add(buttonPanel, BorderLayout.SOUTH);

		statusLabel = new JLabel("Welcome", JLabel.LEFT);
		statusLabel.setBorder(new EmptyBorder(0, 4, 4, 4));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(queryPanel, BorderLayout.NORTH);
		getContentPane().add(resultPanel, BorderLayout.CENTER);
		getContentPane().add(statusLabel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		new GreqlGui();
	}
}
