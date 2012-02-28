/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralabtest.greql2;

import org.junit.Test;

public class AdditionalTest extends GenericTest {

	@Test
	public void testRecordAccess() throws Exception {
		evalTestQuery("rec (menue1:'bratwurst', menue2:'currywurst', menue3:'steak', menue4:'kaenguruhfleisch', menue5:'spiessbraten') store as x");

		assertQueryEquals("using x: x.menue4", "kaenguruhfleisch");
		assertQueryEquals("using x: x.menue1", "bratwurst");
		assertQueryEquals("using x: x.menue5", "spiessbraten");
		assertQueryEquals("using x: x.menue3", "steak");

		assertQueryIsUndefined("using x: x.menue0");
		assertQueryIsUndefined("using x: x.menue6");
	}
	

}
