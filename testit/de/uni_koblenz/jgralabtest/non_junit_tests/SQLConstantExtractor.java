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
package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.impl.db.DerbyStatementList;
import de.uni_koblenz.jgralab.impl.db.MySqlStatementList;
import de.uni_koblenz.jgralab.impl.db.PostgreSqlStatementList;
import de.uni_koblenz.jgralab.impl.db.SqlStatementList;

public class SQLConstantExtractor {

	private static final String EMPTY = "null";

	private static final String NEW_LINE_REPLACEMENT = "$$n$$";

	public static class CSVEntry {
		private String name;
		private Object mysqlValue;
		private Object derbyValue;
		private Object postgreValue;

		public CSVEntry(String name) {
			super();
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Object getMysqlValue() {
			return mysqlValue;
		}

		public void setMysqlValue(Object mysqlValue) {
			this.mysqlValue = mysqlValue;
		}

		public Object getDerbyValue() {
			return derbyValue;
		}

		public void setDerbyValue(Object derbyValue) {
			this.derbyValue = derbyValue;
		}

		public Object getPostgreValue() {
			return postgreValue;
		}

		public void setPostgreValue(Object postgreValue) {
			this.postgreValue = postgreValue;
		}
	}

	private static final Comparator<CSVEntry> entryComparator1 = new Comparator<CSVEntry>() {

		@Override
		public int compare(CSVEntry o1, CSVEntry o2) {
			int out = o1.getName().compareTo(o2.getName());
			return out;
		}

	};

	private static final Comparator<CSVEntry> entryComparator2 = new Comparator<CSVEntry>() {
		@Override
		public int compare(CSVEntry o1, CSVEntry o2) {
			boolean allSet1 = o1.getDerbyValue() != null
					&& o1.getMysqlValue() != null
					&& o1.getPostgreValue() != null;
			boolean allSet2 = o2.getDerbyValue() != null
					&& o2.getMysqlValue() != null
					&& o2.getPostgreValue() != null;
			return Double.compare(allSet1 ? 0.0 : 1.0, allSet2 ? 0.0 : 1.0);
		}
	};

	private static Map<String, CSVEntry> entries;

	public static void main(String[] args) throws IOException {
		entries = new HashMap<String, CSVEntry>();
		processClass(MySqlStatementList.class);
		processClass(DerbyStatementList.class);
		processClass(PostgreSqlStatementList.class);
		String outName = "./SQL_CONSTANTS.csv";
		File out = new File(outName);
		char separator = '|';
		writeCSVFile(out, separator);
		System.out.println("Fini.");
	}

	private static void writeCSVFile(File out, char separator)
			throws IOException {
		Collection<CSVEntry> col = entries.values();
		List<CSVEntry> csvOutputList = new LinkedList<CSVEntry>();
		for (CSVEntry current : col) {
			csvOutputList.add(current);
		}

		Collections.sort(csvOutputList, entryComparator1);
		Collections.sort(csvOutputList, entryComparator2);

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				out)));
		writer.print("CONSTANT");
		writer.print(separator);
		writer.print("MYSQL");
		writer.print(separator);
		writer.print("DERBY");
		writer.print(separator);
		writer.print("POSTGRE");
		writer.println();

		for (CSVEntry current : csvOutputList) {
			writer.print(current.getName());
			writer.print(separator);
			writer.print(getValueString(current.getMysqlValue()));
			writer.print(separator);
			writer.print(getValueString(current.getDerbyValue()));
			writer.print(separator);
			writer.print(getValueString(current.getPostgreValue()));
			writer.println();
		}
		writer.flush();
		writer.close();
	}

	private static String getValueString(Object value) {
		return value == null ? EMPTY : value.toString().replace("\n",
				NEW_LINE_REPLACEMENT);
	}

	private static void processClass(Class<? extends SqlStatementList> class1) {
		Field[] fields = class1.getDeclaredFields();
		for (Field current : fields) {
			current.setAccessible(true);
			try {
				String name = current.getName();
				// System.out.print(name);
				// System.out.print(" : ");
				Object value = current.get(null);
				// System.out.println(value);
				CSVEntry currentEntry = entries.containsKey(name) ? entries
						.get(name) : new CSVEntry(name);
				updateEntry(class1, value, currentEntry);
				entries.put(name, currentEntry);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	private static void updateEntry(Class<? extends SqlStatementList> class1,
			Object value, CSVEntry currentEntry) {
		if (class1.equals(MySqlStatementList.class)) {
			assert (currentEntry.getMysqlValue() == null);
			currentEntry.setMysqlValue(value);
		} else if (class1.equals(DerbyStatementList.class)) {
			assert (currentEntry.getDerbyValue() == null);
			currentEntry.setDerbyValue(value);
		} else if (class1.equals(PostgreSqlStatementList.class)) {
			assert (currentEntry.getPostgreValue() == null);
			currentEntry.setPostgreValue(value);
		} else {
			assert false;
		}
	}
}
