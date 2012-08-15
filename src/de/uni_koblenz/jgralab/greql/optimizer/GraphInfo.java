package de.uni_koblenz.jgralab.greql.optimizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import de.uni_koblenz.jgralab.greql.evaluator.GraphSize;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class GraphInfo extends GraphSize {

	private String qualifiedSchemaName;
	private Schema schema;

	private HashMap<String, Double> relativeFrequencyOfVertexClasses;
	private HashMap<String, Double> relativeFrequencyOfEdgeClasses;

	public GraphInfo(long vCount, long eCount, String schemaName,
			int knownVertexTypes, int knownEdgeTypes,
			HashMap<String, Double> vcfreqs, HashMap<String, Double> ecfreqs) {
		super(vCount, eCount, knownVertexTypes, knownEdgeTypes);
		this.relativeFrequencyOfVertexClasses = vcfreqs;
		this.relativeFrequencyOfEdgeClasses = ecfreqs;
		this.qualifiedSchemaName = schemaName;
	}

	public GraphInfo(GraphClass graphClass, int vCount, int eCount,
			HashMap<String, Double> vcfreqs, HashMap<String, Double> ecfreqs) {
		super(graphClass, vCount, eCount);
		this.relativeFrequencyOfVertexClasses = vcfreqs;
		this.relativeFrequencyOfEdgeClasses = ecfreqs;
		this.qualifiedSchemaName = graphClass.getSchema().getQualifiedName();
		this.schema = graphClass.getSchema();
	}

	public String getQualifiedSchemaName() {
		return this.qualifiedSchemaName;
	}

	public Schema getSchema() {
		return this.schema;
	}

	public void setSchema(Schema s) {
		if (this.qualifiedSchemaName != null && this.qualifiedSchemaName != "") {
			if (!this.qualifiedSchemaName.equals(s.getQualifiedName())) {
				throw new GreqlException(
						"Can not add Schema to GraphInfo because a previously qualified Schema name is in conflict.");
			}
		} else {
			this.qualifiedSchemaName = s.getQualifiedName();
		}
		this.schema = s;
	}

	@Override
	public double getRelativeFrequencyOfVertexClass(String vcName) {
		if (this.relativeFrequencyOfVertexClasses.containsKey(vcName)) {
			return this.relativeFrequencyOfVertexClasses.get(vcName);
		} else {
			throw new GreqlException(
					"Error in GraphInfo: No relative frequency for VertexClass"
							+ vcName + " available.");
		}
	}

	@Override
	public double getRelativeFrequencyOfEdgeClass(String ecName) {
		if (this.relativeFrequencyOfEdgeClasses.containsKey(ecName))
			return this.relativeFrequencyOfEdgeClasses.get(ecName);
		else
			throw new GreqlException(
					"Error in GraphInfo: No relative frequency for EdgeClass "
							+ ecName + " available.");
	}

	@Override
	public double getRelativeFrequencyOfGraphElementClass(String geName) {
		if (this.relativeFrequencyOfVertexClasses.containsKey(geName)) {
			return this.relativeFrequencyOfVertexClasses.get(geName)
					/ (1.0 + this.getEdgesPerVertex());
		} else if (this.relativeFrequencyOfEdgeClasses.containsKey(geName)) {
			return (this.relativeFrequencyOfEdgeClasses.get(geName) * this
					.getEdgesPerVertex()) / (1.0 + this.getEdgesPerVertex());
		} else {
			throw new GreqlException(
					"Error in GraphInfo: No relative frequency for GraphElement "
							+ geName + " available.");
		}
	}

	@Override
	public double getRelativeFrequencyOfTypeCollection(TypeCollection tc) {
		if (schema == null)
			return 1.0d;
		double sum = 0.0d;
		for (GraphElementClass<?, ?> gec : schema.getGraphClass()
				.getGraphElementClasses()) {
			if (tc.acceptsType(gec)) {
				double diff = 0.0d;
				for (GraphElementClass<?, ?> subClass : gec
						.getDirectSubClasses()) {
					diff += this.getRelativeFrequencyOfGraphElementClass(subClass
							.getQualifiedName());
				}
				sum += (this.getRelativeFrequencyOfGraphElementClass(gec
						.getQualifiedName()) - diff);
			}
		}
		return sum;
	}

	@Override
	public double getEdgesPerVertex() {
		return this.getEdgeCount() / (double) this.getVertexCount();
	}

	@Override
	public String toString() {
		String text = "Schema: " + this.qualifiedSchemaName + "\n";
		text += "vCount: " + this.getVertexCount() + "\n";
		text += "eCount: " + this.getEdgeCount() + "\n";
		text += "VertexClasses:\n";
		for (String vcname : this.relativeFrequencyOfVertexClasses.keySet()) {
			text += vcname + ": "
					+ this.relativeFrequencyOfVertexClasses.get(vcname) + "\n";
		}
		text += "EdgeClasses:\n";
		for (String ecname : this.relativeFrequencyOfEdgeClasses.keySet()) {
			text += ecname + ": "
					+ this.relativeFrequencyOfEdgeClasses.get(ecname) + "\n";
		}
		return text;
	}

	/**
	 * Save the GraphInfo object to the given .properties file
	 * 
	 * @param filename
	 *            path to the properties file
	 */
	public void save(String filename) {
		Properties properties = new Properties();
		properties.put("QualifiedSchemaName", this.qualifiedSchemaName);
		properties.put("AverageVCount", this.getVertexCount() + "");
		properties.put("AverageECount", this.getEdgeCount() + "");
		for (String vcName : this.relativeFrequencyOfVertexClasses.keySet()) {
			properties.put("VC_" + vcName,
					this.relativeFrequencyOfVertexClasses.get(vcName) + "");
		}
		for (String ecName : this.relativeFrequencyOfEdgeClasses.keySet()) {
			properties.put("EC_" + ecName,
					this.relativeFrequencyOfEdgeClasses.get(ecName) + "");
		}
		try {
			BufferedOutputStream stream = new BufferedOutputStream(
					new FileOutputStream(filename));
			properties.store(stream, null);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a GraphInfo from a properties file
	 * 
	 * @param filename
	 *            path to the properties file
	 * @return the loaded GraphInfo
	 */
	public static GraphInfo load(String filename) {
		Properties properties = new Properties();
		BufferedInputStream stream;
		GraphInfo gi = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(filename));
			properties.load(stream);
			HashMap<String, Double> vc2freq = new HashMap<String, Double>();
			HashMap<String, Double> ec2freq = new HashMap<String, Double>();
			int vCount = 0;
			int eCount = 0;
			String schemaName = "";
			for (Object s : properties.keySet()) {
				if (s.toString().startsWith("VC_")) {
					vc2freq.put(s.toString().substring(3),
							Double.parseDouble((String) properties.get(s)));
				} else if (s.toString().startsWith("EC_")) {
					ec2freq.put(s.toString().substring(3),
							Double.parseDouble((String) properties.get(s)));
				} else if (s.toString().equals("AverageVCount")) {
					vCount = (Integer.parseInt((String) properties.get(s)));
				} else if (s.toString().equals("AverageECount")) {
					eCount = (Integer.parseInt((String) properties.get(s)));
				} else if (s.toString().equals("QualifiedSchemaName")) {
					schemaName = (String) properties.get(s);
				}
			}

			stream.close();

			gi = new GraphInfo(vCount, eCount, schemaName, vc2freq.size(),
					ec2freq.size(), vc2freq, ec2freq);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gi;
	}
}
