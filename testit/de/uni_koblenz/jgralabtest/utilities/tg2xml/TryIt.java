package de.uni_koblenz.jgralabtest.utilities.tg2xml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.xml.Tg2xml;

public class TryIt {

	// private Random rand;
	private String outName;

	// private VertexTestGraph graph;

	public TryIt(String outName) {
		this.outName = outName;
		// rand = new Random();
	}

	public static void main(String[] args) {
		TryIt it = new TryIt("out.xml");
		it.runIt();
	}

	public void runIt() {

		try {
			// Graph graph = createRandomGraph(false);
			Schema schema = GraphIO.loadSchemaFromFile("GrumlSchema.gruml.tg");
			System.out.println("Compiling schema");
			schema.compile(CodeGeneratorConfiguration.FULL_WITHOUT_SUBCLASS_FLAGS);
			System.out.println("done");
			Graph graph = GraphIO.loadGraphFromFile("GrumlSchema.gruml.tg",
					new ProgressFunctionImpl());
			Tg2xml converter = new Tg2xml(new BufferedOutputStream(
					new FileOutputStream(outName)), graph, "gruml",
					"./gruml.xsd");

			converter.visitAll();
			System.out.println("Fini.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// private VertexTestGraph createRandomGraph(boolean useAddTarget) {
	// VertexTestGraph graph = VertexTestSchema.instance()
	// .createVertexTestGraph();
	// A v1 = graph.createA();
	// C v2 = graph.createC();
	// B v3 = graph.createB();
	// D v4 = graph.createD();
	// B v5 = graph.createB();
	// D v6 = graph.createD();
	// A v7 = graph.createA();
	// C v8 = graph.createC();
	// C2 v9 = graph.createC2();
	// C2 v10 = graph.createC2();
	// D2 v11 = graph.createD2();
	// D2 v12 = graph.createD2();
	//		
	// for (int i = 0; i < 1000; i++) {
	// int howToCreate = rand.nextInt(2);
	// int whichEdge = rand.nextInt(useAddTarget ? 5 : 6);
	// if (whichEdge == 0) {
	// // edge E
	// int end = rand.nextInt(4);
	// int start = rand.nextInt(2);
	// E e = null;
	// switch (end) {
	// case 0:
	// e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
	// v3) : (useAddTarget ? (start == 0 ? v1 : v7)
	// .addX(v3) : v3.addSourceE(start == 0 ? v1 : v7));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
	// v4) : (useAddTarget ? (start == 0 ? v1 : v7)
	// .addX(v4) : v4.addSourceE(start == 0 ? v1 : v7));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
	// v5) : (useAddTarget ? (start == 0 ? v1 : v7)
	// .addX(v5) : v5.addSourceE(start == 0 ? v1 : v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createE(start == 0 ? v1 : v7,
	// v6) : (useAddTarget ? (start == 0 ? v1 : v7)
	// .addX(v6) : v6.addSourceE(start == 0 ? v1 : v7));
	// break;
	// }
	// } else if (whichEdge == 1) {
	// // edge F
	// int end = rand.nextInt(2);
	// int start = rand.nextInt(2);
	// F e = null;
	// switch (end) {
	// case 0:
	// e = howToCreate == 0 ? graph.createF(start == 0 ? v2 : v8,
	// v4) : (useAddTarget ? (start == 0 ? v2 : v8)
	// .addY(v4) : v4.addSourceF(start == 0 ? v2 : v8));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createF(start == 0 ? v2 : v8,
	// v6) : (useAddTarget ? (start == 0 ? v2 : v8)
	// .addY(v6) : v6.addSourceF(start == 0 ? v2 : v8));
	// break;
	// }
	// } else if (whichEdge == 2) {
	// // edge G
	// int end = rand.nextInt(2);
	// int start = rand.nextInt(2);
	// G e = null;
	// switch (end) {
	// case 0:
	// e = howToCreate == 0 ? graph.createG(start == 0 ? v2 : v8,
	// v4) : (useAddTarget ? (start == 0 ? v2 : v8)
	// .addZ(v4) : v4.addSourceG(start == 0 ? v2 : v8));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createG(start == 0 ? v2 : v8,
	// v6) : (useAddTarget ? (start == 0 ? v2 : v8)
	// .addZ(v6) : v6.addSourceG(start == 0 ? v2 : v8));
	// break;
	// }
	// } else if (whichEdge == 3) {
	// // edge H
	// int end = rand.nextInt(useAddTarget ? 4 : 6);
	// int start = rand.nextInt(useAddTarget ? 4 : 6);
	// H e = null;
	// switch (end) {
	// case 0:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v3)
	// : (useAddTarget ? v1.addW(v3) : v3
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v3)
	// : (useAddTarget ? v2.addW(v3) : v3
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v3)
	// : (useAddTarget ? v7.addW(v3) : v3
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v3)
	// : (useAddTarget ? v8.addW(v3) : v3
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v3)
	// : (useAddTarget ? v9.addW(v3) : v3
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v3)
	// : (useAddTarget ? v10.addW(v3) : v3
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// case 1:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v4)
	// : (useAddTarget ? v1.addW(v4) : v4
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v4)
	// : (useAddTarget ? v2.addW(v4) : v4
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v4)
	// : (useAddTarget ? v7.addW(v4) : v4
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v4)
	// : (useAddTarget ? v8.addW(v4) : v4
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v4)
	// : (useAddTarget ? v9.addW(v4) : v4
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v4)
	// : (useAddTarget ? v10.addW(v4) : v4
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// case 2:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v5)
	// : (useAddTarget ? v1.addW(v5) : v5
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v5)
	// : (useAddTarget ? v2.addW(v5) : v5
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v5)
	// : (useAddTarget ? v7.addW(v5) : v5
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v5)
	// : (useAddTarget ? v8.addW(v5) : v5
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v5)
	// : (useAddTarget ? v9.addW(v5) : v5
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v5)
	// : (useAddTarget ? v10.addW(v5) : v5
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// case 3:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v6)
	// : (useAddTarget ? v1.addW(v6) : v6
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v6)
	// : (useAddTarget ? v2.addW(v6) : v6
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v6)
	// : (useAddTarget ? v7.addW(v6) : v6
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v6)
	// : (useAddTarget ? v8.addW(v6) : v6
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v6)
	// : (useAddTarget ? v9.addW(v6) : v6
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v6)
	// : (useAddTarget ? v10.addW(v6) : v6
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// case 4:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v11)
	// : (useAddTarget ? v1.addW(v11) : v11
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v11)
	// : (useAddTarget ? v2.addW(v11) : v11
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v11)
	// : (useAddTarget ? v7.addW(v11) : v11
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v11)
	// : (useAddTarget ? v8.addW(v11) : v11
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v11)
	// : (useAddTarget ? v9.addW(v11) : v11
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v11)
	// : (useAddTarget ? v10.addW(v11) : v11
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// case 5:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createH(v1, v12)
	// : (useAddTarget ? v1.addW(v12) : v12
	// .addSourceH(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createH(v2, v12)
	// : (useAddTarget ? v2.addW(v12) : v12
	// .addSourceH(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createH(v7, v12)
	// : (useAddTarget ? v7.addW(v12) : v12
	// .addSourceH(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createH(v8, v12)
	// : (useAddTarget ? v8.addW(v12) : v12
	// .addSourceH(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createH(v9, v12)
	// : (useAddTarget ? v9.addW(v12) : v12
	// .addSourceH(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createH(v10, v12)
	// : (useAddTarget ? v10.addW(v12) : v12
	// .addSourceH(v10));
	// break;
	// }
	// break;
	// }
	// } else if (whichEdge == 4) {
	// // edge I
	// int end = rand.nextInt(useAddTarget ? 4 : 6);
	// int start = rand.nextInt(useAddTarget ? 4 : 6);
	// I e = null;
	// switch (end) {
	// case 0:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v1)
	// : (useAddTarget ? v1.addV(v1) : v1
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v1)
	// : (useAddTarget ? v2.addV(v1) : v1
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v7, v1)
	// : (useAddTarget ? v7.addV(v1) : v1
	// .addSourceI(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v8, v1)
	// : (useAddTarget ? v8.addV(v1) : v1
	// .addSourceI(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v1)
	// : (useAddTarget ? v9.addV(v1) : v1
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v1)
	// : (useAddTarget ? v10.addV(v1) : v1
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// case 1:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v2)
	// : (useAddTarget ? v1.addV(v2) : v2
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v2)
	// : (useAddTarget ? v2.addV(v2) : v2
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v7, v2)
	// : (useAddTarget ? v7.addV(v2) : v2
	// .addSourceI(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v8, v2)
	// : (useAddTarget ? v8.addV(v2) : v2
	// .addSourceI(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v2)
	// : (useAddTarget ? v9.addV(v2) : v2
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v2)
	// : (useAddTarget ? v10.addV(v2) : v2
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// case 2:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v7)
	// : (useAddTarget ? v1.addV(v7) : v7
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v7)
	// : (useAddTarget ? v2.addV(v7) : v7
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v7, v7)
	// : (useAddTarget ? v7.addV(v7) : v7
	// .addSourceI(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v8, v7)
	// : (useAddTarget ? v8.addV(v7) : v7
	// .addSourceI(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v7)
	// : (useAddTarget ? v9.addV(v7) : v7
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v7)
	// : (useAddTarget ? v10.addV(v7) : v7
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// case 3:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v8)
	// : (useAddTarget ? v1.addV(v8) : v8
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v8)
	// : (useAddTarget ? v2.addV(v8) : v8
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v7, v8)
	// : (useAddTarget ? v7.addV(v8) : v8
	// .addSourceI(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v8, v8)
	// : (useAddTarget ? v8.addV(v8) : v8
	// .addSourceI(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v8)
	// : (useAddTarget ? v9.addV(v8) : v8
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v8)
	// : (useAddTarget ? v10.addV(v8) : v8
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// case 4:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v9)
	// : (useAddTarget ? v1.addV(v9) : v9
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v9)
	// : (useAddTarget ? v2.addV(v9) : v9
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v9, v9)
	// : (useAddTarget ? v9.addV(v9) : v9
	// .addSourceI(v9));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v8, v9)
	// : (useAddTarget ? v8.addV(v9) : v9
	// .addSourceI(v8));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v9)
	// : (useAddTarget ? v9.addV(v9) : v9
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v9)
	// : (useAddTarget ? v10.addV(v9) : v9
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// case 5:
	// switch (start) {
	// case 0:
	// e = howToCreate == 0 ? graph.createI(v1, v10)
	// : (useAddTarget ? v1.addV(v10) : v10
	// .addSourceI(v1));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createI(v2, v10)
	// : (useAddTarget ? v2.addV(v10) : v10
	// .addSourceI(v2));
	// break;
	// case 2:
	// e = howToCreate == 0 ? graph.createI(v7, v10)
	// : (useAddTarget ? v7.addV(v10) : v10
	// .addSourceI(v7));
	// break;
	// case 3:
	// e = howToCreate == 0 ? graph.createI(v10, v10)
	// : (useAddTarget ? v10.addV(v10) : v10
	// .addSourceI(v10));
	// break;
	// case 4:
	// e = howToCreate == 0 ? graph.createI(v9, v10)
	// : (useAddTarget ? v9.addV(v10) : v10
	// .addSourceI(v9));
	// break;
	// case 5:
	// e = howToCreate == 0 ? graph.createI(v10, v10)
	// : (useAddTarget ? v10.addV(v10) : v10
	// .addSourceI(v10));
	// break;
	// }
	// break;
	// }
	// } else {
	// // edge J
	// int end = rand.nextInt(2);
	// int start = rand.nextInt(2);
	// J e = null;
	// switch (end) {
	// case 0:
	// e = howToCreate == 0 ? graph.createJ(start == 0 ? v9 : v10,
	// v11) : (useAddTarget ? (start == 0 ? v9 : v10)
	// .addU(v11) : v11.addSourceJ(start == 0 ? v9 : v10));
	// break;
	// case 1:
	// e = howToCreate == 0 ? graph.createJ(start == 0 ? v9 : v10,
	// v12) : (useAddTarget ? (start == 0 ? v9 : v10)
	// .addU(v12) : v12.addSourceJ(start == 0 ? v9 : v10));
	// break;
	// }
	// }
	// }
	//
	// return graph;
	// }
}
