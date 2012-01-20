package de.uni_koblenz.jgralab.utilities.greqlinterface;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class SettingsDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -5656234050436317561L;

	private GreqlGui gui;
	private JButton cancelButton;
	private JButton okButton;

	public SettingsDialog(GreqlGui owner) {
		super(owner, owner.getApplicationName() + " - Settings", true);
		gui = owner;
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		getContentPane().add(pnl);

		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new BorderLayout());
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		okButton = new JButton("OK");
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
			gui.saveSettings();
			dispose();
		}
	}

	private boolean isOk() {
		return true;
	}

}
