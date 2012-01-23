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

	private GreqlGui gui;
	private JButton cancelButton;
	private JButton okButton;

	private Font queryFont;
	private JTextField queryFontLabel;
	private JButton queryFontButton;

	private Font resultFont;
	private JTextField resultFontLabel;
	private JButton resultFontButton;

	public SettingsDialog(GreqlGui app, Font qf, Font rf) {
		super(app, app.getApplicationName()
				+ app.getMessage("SettingsDialog.Title"), true); //$NON-NLS-1$
		gui = app;
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		getContentPane().add(pnl);

		queryFont = qf;
		resultFont = rf;

		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new GridBagLayout());
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;

		c.gridy = 0;
		c.gridx = 0;
		JLabel lbl = new JLabel(
				gui.getMessage("SettingsDialog.QueryFontLabel"), JLabel.RIGHT); //$NON-NLS-1$
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		settingsPanel.add(lbl, c);
		++c.gridx;
		queryFontLabel = new JTextField(gui.getFontName(queryFont), 30);
		queryFontLabel.setEditable(false);
		settingsPanel.add(queryFontLabel, c);
		queryFontButton = new JButton(
				gui.getMessage("SettingsDialog.QueryFontButtonText")); //$NON-NLS-1$
		++c.gridx;
		settingsPanel.add(queryFontButton, c);
		queryFontButton.addActionListener(this);

		c.gridy = 1;
		c.gridx = 0;
		lbl = new JLabel(gui.getMessage("SettingsDialog.ResultFontLabel"), //$NON-NLS-1$
				JLabel.RIGHT);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		settingsPanel.add(lbl, c);
		++c.gridx;
		resultFontLabel = new JTextField(gui.getFontName(resultFont), 30);
		resultFontLabel.setEditable(false);
		settingsPanel.add(resultFontLabel, c);
		resultFontButton = new JButton(
				gui.getMessage("SettingsDialog.ResultFontButtonText")); //$NON-NLS-1$
		++c.gridx;
		settingsPanel.add(resultFontButton, c);
		resultFontButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		cancelButton = new JButton(
				gui.getMessage("SettingsDialog.CancelButtonText")); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		okButton = new JButton(gui.getMessage("SettingsDialog.OkButtonText")); //$NON-NLS-1$
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
			gui.saveSettings(this);
			dispose();
		}
		if (e.getSource() == resultFontButton) {
			Font newFont = FontSelectionDialog.selectFont(gui,
					gui.getMessage("SettingsDialog.ResultFontTitle"), //$NON-NLS-1$
					resultFont, false);
			if (newFont != null) {
				resultFont = newFont;
				resultFontLabel.setText(gui.getFontName(resultFont));
			}
		}
		if (e.getSource() == queryFontButton) {
			Font newFont = FontSelectionDialog
					.selectFont(
							gui,
							gui.getMessage("SettingsDialog.QueryFontTitle"), queryFont, false); //$NON-NLS-1$
			if (newFont != null) {
				queryFont = newFont;
				queryFontLabel.setText(gui.getFontName(queryFont));
			}
		}
	}

	public Font getQueryFont() {
		return queryFont;
	}

	public Font getResultFont() {
		return resultFont;
	}

	private boolean isOk() {
		return true;
	}

}
