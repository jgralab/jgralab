package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.Locale;

import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.impl.SwingProgressFunction;

public class SwingProgressFunctionTest {
	public static void main(String[] args) {
		// try {
		// // UIManager.setLookAndFeel(UIManager
		// // .getSystemLookAndFeelClassName());
		// // .getCrossPlatformLookAndFeelClassName());
		// } catch (ClassNotFoundException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } catch (InstantiationException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } catch (IllegalAccessException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// } catch (UnsupportedLookAndFeelException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		Locale.setDefault(Locale.ENGLISH);
		ProgressFunction pf = new SwingProgressFunction("ProgressFunctionTest",
				"simple loop", "items");

		System.out.println("Start");
		final int N = 1000;
		pf.init(N);
		long cnt = 0;
		long interval = pf.getUpdateInterval();
		for (int i = 1; i <= N; ++i) {
			++cnt;
			if (cnt == interval) {
				pf.progress(i);
				cnt = 0;
			}
			try {
				Thread.sleep(Math.round(Math.random() * 10 + 10));
			} catch (InterruptedException e) {
			}
		}
		pf.finished();
		System.out.println("Fini.");
	}
}
