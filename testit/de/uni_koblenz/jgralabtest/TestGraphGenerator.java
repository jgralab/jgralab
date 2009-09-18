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

package de.uni_koblenz.jgralabtest;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralabtest.schemas.citymap.Bridge;
import de.uni_koblenz.jgralabtest.schemas.citymap.CarPark;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMap;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMapSchema;
import de.uni_koblenz.jgralabtest.schemas.citymap.Intersection;
import de.uni_koblenz.jgralabtest.schemas.citymap.Street;

/**
 * Generates a TestGrapg which is used to perform several tests on
 */
public class TestGraphGenerator {

	public static void main(String[] args) throws GraphIOException {
		/*
		 * try to build up the following graph
		 * 
		 * v1 -e1-> v2 -e2-> v3 // v1 and v2 are Intersections | ^ | // v3 is a
		 * CarPark, capacity 2500 | | | e5 is a footway, e3 e4 e5 other edges
		 * are streets | | | v | v v4 -e6-> v5 -e7-> v6 // all Intersections | |
		 * / e9 is a footway | | / e10 is a bridge e8 e9 e10 v v / other edges
		 * are streets v7 -e11->v8 </ //v7 and v8 are CarParks, cap. 500
		 */

		CityMapSchema schema = CityMapSchema.instance();

		// generate graph g1
		CityMap g1 = schema.createCityMap("CityMapSample", 1000, 1000);

		Intersection v1 = g1.createIntersection();
		Intersection v2 = g1.createIntersection();

		CarPark v3 = g1.createCarPark();
		v3.set_capacity(2500);

		Intersection v4 = g1.createIntersection();
		Intersection v5 = g1.createIntersection();
		Intersection v6 = g1.createIntersection();

		CarPark v7 = g1.createCarPark();
		v7.set_capacity(500);

		CarPark v8 = g1.createCarPark();
		v8.set_capacity(500);

		Street e1 = g1.createStreet(v1, v2);
		e1.set_name("e1");
		Street e2 = g1.createStreet(v2, v3);
		e2.set_name("e2");
		Street e3 = g1.createStreet(v1, v4);
		e3.set_name("e3");
		Street e4 = g1.createStreet(v5, v2);
		e4.set_name("e4");
		Bridge e5 = g1.createBridge(v3, v6);
		e5.set_name("e5");
		Street e6 = g1.createStreet(v4, v5);
		e6.set_name("e6");
		Street e7 = g1.createStreet(v5, v6);
		e7.set_name("e7");
		Street e8 = g1.createStreet(v4, v7);
		e8.set_name("e8");
		Bridge e9 = g1.createBridge(v5, v8);
		e9.set_name("e9");
		Bridge e10 = g1.createBridge(v6, v8);
		e10.set_name("e10");
		Street e11 = g1.createStreet(v7, v8);
		e11.set_name("e11");

		System.out.println("Storing graph to file 'citymapgraph.tg'");
		GraphIO.saveGraphToFile("citymapgraph.tg", g1,
				new ProgressFunctionImpl());
	}

}
