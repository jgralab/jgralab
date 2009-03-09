package de.uni_koblenz.jgralab.greql2;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueHTMLOutputVisitor;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

@WorkInProgress(description = "insufficcient result presentation, simplistic GUI, no optimizer control, no load/save functionality, ...", responsibleDevelopers = "horn")
public class GreqlGui extends JFrame {
	private static final long serialVersionUID = 1L;

	private Graph graph;

	private JFileChooser fileChooser;

	private JPanel panel;
	private JTextArea queryArea;
	private JEditorPane resultPane;
	private JButton fileSelectionButton, evalQueryButton;

	public GreqlGui() {
		super("GReQL GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		getContentPane().add(panel);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		queryArea = new JTextArea(20, 40);
		queryArea.setEditable(true);
		queryArea.setText("/*\n * Please enter your query here!\n */\n\n");

		resultPane = new JEditorPane(
				"text/html; charset=UTF-8",
				"<html>Here the <b>query results</b> will be shown. "
						+ "Simply select a graph, type a query and press the evaluation button."
						+ "</html>");
		resultPane.setEditable(false);
		resultPane.setBackground(Color.LIGHT_GRAY);
		JScrollPane resultScrollPane = new JScrollPane(resultPane);

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
					final File file = fileChooser.getSelectedFile();

					try {
						graph = GraphIO
								.loadSchemaAndGraphFromFile(file
										.getCanonicalPath(),
										new ProgressFunctionImpl());
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, e1.getMessage(), e1
								.getClass().getSimpleName(),
								JOptionPane.ERROR_MESSAGE);
					}

				}
			}
		});
		evalQueryButton = new JButton(new AbstractAction("Evaluate Query") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String query = queryArea.getText();
				GreqlEvaluator eval = new GreqlEvaluator(query, graph, null);
				eval.setOptimize(false);
				try {
					if (eval.parseQuery()) {
						assert eval.getSyntaxGraph() != null;
						Tg2Dot t2d = new Tg2Dot();
						t2d.setGraph(eval.getSyntaxGraph());
						t2d.setPrintEdgeAttributes(true);
						t2d.setPrintReversedEdges(true);
						t2d.setOutputFile("query.greql.dot");
						t2d.printGraph();
					}
					eval.startEvaluation();
					JValue result = eval.getEvaluationResult();
					File resultFile = File.createTempFile("greqlQueryResult",
							".html");
					new JValueHTMLOutputVisitor(result, resultFile
							.getCanonicalPath(), graph);
					resultPane.setPage(new URL("file", "localhost", resultFile
							.getCanonicalPath()));
				} catch (Exception e1) {
					String msg = e1.getMessage();
					if (msg == null) {
						msg = "An Exception occured!";
					}
					JOptionPane.showMessageDialog(null, msg, e1.getClass()
							.getSimpleName(), JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(fileSelectionButton);
		buttonPanel.add(evalQueryButton);
		panel.add(buttonPanel);

		JPanel textPanel = new JPanel(new GridLayout(2, 1));
		textPanel.add(queryArea);
		textPanel.add(resultScrollPane);
		panel.add(textPanel);

		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		new GreqlGui();
	}
}
