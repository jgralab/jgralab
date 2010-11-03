/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
