package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.impl.db.DerbyStatementList;
import de.uni_koblenz.jgralab.impl.db.MySqlStatementList;
import de.uni_koblenz.jgralab.impl.db.PostgreSqlStatementList;
import de.uni_koblenz.jgralab.impl.db.SqlStatementList;

public class SQLConstantExtractor {

	public static class Tuple<A, B> {
		private A left;
		private B right;

		public Tuple(A left, B right) {
			this.left = left;
			this.right = right;
		}

		public A getLeft() {
			return left;
		}

		public B getRight() {
			return right;
		}
	}

	private static final Comparator<Tuple<String, Object>> tupleComparator = new Comparator<Tuple<String, Object>>() {

		@Override
		public int compare(Tuple<String, Object> o1, Tuple<String, Object> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}

	};

	public static void main(String[] args) throws IOException {

		String fileName1 = "./mysqlStatements.csv";
		String fileName2 = "./derbyStatements.csv";
		String filename3 = "./postgreStatements.csv";

		File out1 = new File(fileName1);
		File out2 = new File(fileName2);
		File out3 = new File(filename3);

		char separator = '|';

		processClass(MySqlStatementList.class, out1, separator);

		processClass(DerbyStatementList.class, out2, separator);

		processClass(PostgreSqlStatementList.class, out3, separator);
		System.out.println("Fini.");
	}

	private static void processClass(Class<? extends SqlStatementList> class1,
			File out, char separator) throws IOException {
		List<Tuple<String, Object>> currentList = new LinkedList<Tuple<String, Object>>();
		Field[] fields = class1.getDeclaredFields();
		for (Field current : fields) {
			current.setAccessible(true);
			try {
				String name = current.getName();
				// System.out.print(name);
				// System.out.print(" : ");
				Object value = current.get(null);
				// System.out.println(value);
				currentList.add(new Tuple<String, Object>(name, value));
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
		Collections.sort(currentList, tupleComparator);
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				out)));
		for (Tuple<String, Object> current : currentList) {
			writer.print(current.getLeft());
			writer.print(separator);
			writer
					.println(current.getRight().toString().replace("\n",
							"$$n$$"));
		}
		writer.flush();
		writer.close();
	}
}
