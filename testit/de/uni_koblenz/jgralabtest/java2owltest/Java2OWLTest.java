/*
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

package de.uni_koblenz.jgralabtest.java2owltest;

import java.io.IOException;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.jgralab2owl.JGraLab2OWL;

public class Java2OWLTest {

	public static void main(String[] args) {
		try {
			Schema cityMapSchema = GraphIO
					.loadSchemaFromFile("src/de/uni_koblenz/jgralabtest/java2owltest/citymapschema.tg");
			JGraLab2OWL.saveSchemaToOWL(
					"src/de/uni_koblenz/jgralabtest/java2owltest/CityMap.owl",
					cityMapSchema, true, false);
		} catch (GraphIOException gioe) {
			gioe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}
