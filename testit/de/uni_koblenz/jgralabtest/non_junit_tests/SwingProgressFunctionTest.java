package de.uni_koblenz.jgralabtest.non_junit_tests;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.impl.SwingProgressFunction;

public class SwingProgressFunctionTest {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager
			// .getSystemLookAndFeelClassName());
					.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ProgressFunction pf = new SwingProgressFunction("ProgressFunctionTest",
				"Labeltext");

		System.out.println("Start");
		pf.init(100);
		for (int i = 1; i <= 100; ++i) {
			pf.progress(i);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		pf.finished();
		System.out.println("Fini.");
	}
}
