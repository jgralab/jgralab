package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uni_koblenz.ist.utilities.gui.FontSelectionDialog;

public class SettingsDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -5656234050436317561L;

	private GreqlGui app;
	private JButton cancelButton;
	private JButton okButton;

	private Font qf;
	private JTextField queryFontLabel;
	private JButton queryFontButton;

	private Font rf;
	private JTextField resultFontLabel;
	private JButton resultFontButton;

	public SettingsDialog(GreqlGui gui) {
		super(gui, gui.getApplicationName()
				+ gui.getMessage("SettingsDialog.Title"), true); //$NON-NLS-1$
		app = gui;
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		getContentPane().add(pnl);

		qf = app.getQueryFont();
		rf = app.getResultFont();

		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new GridBagLayout());
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;

		c.gridy = 0;
		c.gridx = 0;
		JLabel lbl = new JLabel("Query font:", JLabel.RIGHT);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		settingsPanel.add(lbl, c);
		++c.gridx;
		queryFontLabel = new JTextField(app.getFontName(qf), 30);
		queryFontLabel.setEditable(false);
		settingsPanel.add(queryFontLabel, c);
		queryFontButton = new JButton("Select ...");
		++c.gridx;
		settingsPanel.add(queryFontButton, c);
		queryFontButton.addActionListener(this);

		c.gridy = 1;
		c.gridx = 0;
		lbl = new JLabel("Result font:", JLabel.RIGHT);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		settingsPanel.add(lbl, c);
		++c.gridx;
		resultFontLabel = new JTextField(app.getFontName(rf), 30);
		resultFontLabel.setEditable(false);
		settingsPanel.add(resultFontLabel, c);
		resultFontButton = new JButton("Select ...");
		++c.gridx;
		settingsPanel.add(resultFontButton, c);
		resultFontButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		cancelButton = new JButton(
				app.getMessage("SettingsDialog.CancelButtonText")); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		okButton = new JButton(app.getMessage("SettingsDialog.OkButtonText")); //$NON-NLS-1$
		okButton.addActionListener(this);
		okButton.setDefaultCapable(true);
		buttonPanel.add(okButton);

		pnl.add(settingsPanel, BorderLayout.CENTER);
		pnl.add(buttonPanel, BorderLayout.SOUTH);
		pack();
		okButton.requestFocus();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			dispose();
		}
		if (e.getSource() == okButton && isOk()) {
			app.setQueryFont(qf);
			app.setResultFont(rf);
			app.saveSettings();
			dispose();
		}
		if (e.getSource() == resultFontButton) {
			Font newFont = FontSelectionDialog.selectFont(app,
					"Select result font", rf, false);
			if (newFont != null) {
				rf = newFont;
				resultFontLabel.setText(app.getFontName(rf));
			}
		}
		if (e.getSource() == queryFontButton) {
			Font newFont = FontSelectionDialog.selectFont(app,
					"Select query font", qf, false);
			if (newFont != null) {
				qf = newFont;
				queryFontLabel.setText(app.getFontName(qf));
			}
		}
	}

	private boolean isOk() {
		return true;
	}

}
