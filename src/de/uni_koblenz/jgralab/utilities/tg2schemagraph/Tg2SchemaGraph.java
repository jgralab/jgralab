/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.CompositeDomain;
import de.uni_koblenz.jgralab.Domain;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.BooleanDomain;
import de.uni_koblenz.jgralab.DoubleDomain;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.AggregationClass;
import de.uni_koblenz.jgralab.CompositionClass;
import de.uni_koblenz.jgralab.EnumDomain;
import de.uni_koblenz.jgralab.IntDomain;
import de.uni_koblenz.jgralab.ListDomain;
import de.uni_koblenz.jgralab.LongDomain;
import de.uni_koblenz.jgralab.ObjectDomain;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.RecordDomain;
import de.uni_koblenz.jgralab.SetDomain;
import de.uni_koblenz.jgralab.StringDomain;
import de.uni_koblenz.jgralab.VertexClass;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Schema;

import de.uni_koblenz.jgralab.utilities.tg2schemagraph.grumlschema.*;

/**
 * This class represents any <code>Schema</code> object as an <code>Graph</code> object.
 * This resulting <code>Graph</code> object is an instance of the M3 schema <code>GrUMLSchema</code>.
 * 
 * @author HiWi
 */
public class Tg2SchemaGraph {
	
	private int MAX_VERTICES = 1000;
	private int MAX_EDGES = 1000;
	
	private String outputFilename;
	//the schema, this class was instantiated with.
	private Schema schema;
	
	//this object will be returned, when getSchemaGraph() is called.
	private GrUMLSchemaGraph schemagraph;
	
	//helpful to encapsulate the CompositeDomain hierarchy from the
	//rest of the graph
	private Map<Domain, DomainM2> domainMap;
	
	//mergeGraphClasses==true creates a schemagraph with only one GraphClass
	private boolean combineGraphClasses;
	
	/**
	 * The unparameterized constructor is only used in the command line mode.
	 * The method <code>private void setSchema()</code> ensures the initialization of <code>private Schema schema</code> 	  
	 */
	private Tg2SchemaGraph(){}
	
	/**
	 * This class must be instantiated with a schema.
	 * So for every schema, you want to represent with a <Code>Graph</Code> object,
	 * you have to create a new instance of this class.
	 * 
	 * @param schema Any desired <code>Schema</code> object. 
	 */
	public Tg2SchemaGraph(Schema schema){
		this.schema=schema;
		combineGraphClasses = false;
	}
	
	/**
	 * This class must be instantiated with a schema.
	 * So for every schema, you want to represent with a <Code>Graph</Code> object,
	 * you have to create a new instance of this class.
	 * 
	 * @param schema Any desired <code>Schema</code> object.
	 * @param combineGraphClasses ==true => one <code>GraphClassM2</code> combines all GraphClasses of <code>schema</code>.
	 */
	public Tg2SchemaGraph(Schema schema, Boolean combineGraphClasses){
		this.schema=schema;
		this.combineGraphClasses = combineGraphClasses;
	}

	/**
	 * <code>mergeGraphClasses = b</code>
	 *  
	 * @param b set <code>mergeGraphClasses</code> to this value 
	 */
	public void setMergeGraphClasses(boolean b){
		combineGraphClasses = b;
	}
	
	/**
	 * This method creates an instance graph of the grUML language's meta schema.
	 * Its result is a <code>Graph</code>, that represents any desired <code>Schema</code>.  
	 * 
	 * @return a <code>Graph</code> object that represents a <code>Schema</code>.  	  
	 */
	public Graph getSchemaGraph(){
		if (schema==null)return null;
		if (schemagraph == null) {			
			
			//create the schemagraph
			schemagraph = GrUMLSchema.instance().createGrUMLSchemaGraph(schema.getName(), MAX_VERTICES, MAX_EDGES);
			
			//create a HashMap, that maps each schema domain to the corresponding schemagraph domain
			domainMap = new HashMap<Domain, DomainM2>();
			createDomainToDomainM2Map();
			
			//the proper createM2Graph job			
			createGraphClassesM2();
			createVertexClassesM2();
			createEdgeClassesM2();
		}
		return schemagraph;
	}	
	
	/**
	 *  This method values the <code>Map<Domain, DomainM2> domainMap</code>.
	 *  i.e. <code>domainMap.get(\<Domain\> d)</code> return the corresponding <code>DomainM2</code> object.
	 *  
	 *  At first only the <code>BasicDomain</code>s get mapped. 
	 *	The <code>CompositeDomain</code>s get mapped according to their "structural depth".
	 *	First, composites of basic types get mapped.
	 *	Then composites of composites of basic types get mapped.
	 *	...and so on...
	 */
	private void createDomainToDomainM2Map() {
		Map<String, Domain> domains = schema.getDomains();
		while(domainMap.size()!=domains.size()){
			for(Domain d: domains.values()){
				
				if(d instanceof BooleanDomain
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createBooleanDomainM2());				
				if(d instanceof DoubleDomain
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createDoubleDomainM2());				
				if(d instanceof EnumDomain
						&& (domainMap.get(d)==null)){
					EnumDomainM2 enumM2 = schemagraph.createEnumDomainM2();
					enumM2.setConstants(((EnumDomain)d).getConsts());
					domainMap.put(d, enumM2);
				}
				if(d instanceof LongDomain
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createLongDomainM2());				
				if(d instanceof IntDomain
					    && !(d instanceof LongDomain)
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createIntDomainM2());		
				if(d instanceof ObjectDomain
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createObjectDomainM2());
				if(d instanceof StringDomain
						&& (domainMap.get(d)==null))
					domainMap.put(d, schemagraph.createStringDomainM2());
				if (d instanceof CompositeDomain 
						&& domainMap.get(d)==null)
					createCompositeDomainM2(d);		
			}
		}
	}
	
	/**
	 *	This method checks, if a <code>CompositeDomainM2</code> can be created.
	 *	The condition asks every underlying <code>DomainM2</code> to be created first.  
	 *  If so, it creates a <code>CompositeDomainM2</code> object and maps its corresponding <code>CompositeDomain</code> object to it. 
	 */ 
	private void createCompositeDomainM2(Domain d) {
		if(d instanceof ListDomain
				&& !(domainMap.get(((ListDomain)d).getBaseDomain())==null)){

			ListDomainM2 dM2 = schemagraph.createListDomainM2();
			schemagraph.createHasBaseDomainM2(dM2, domainMap.get(((ListDomain)d).getBaseDomain()));
			domainMap.put(d, dM2);							
		}
		if(d instanceof SetDomain
				&& !(domainMap.get(((SetDomain)d).getBaseDomain())==null)){
	
			SetDomainM2 dM2 = schemagraph.createSetDomainM2();
			schemagraph.createHasBaseDomainM2(dM2, domainMap.get(((SetDomain)d).getBaseDomain()));
			domainMap.put(d, dM2);			
		}
		if(d instanceof RecordDomain){
			boolean allBaseDomainsMapped=true;
			for(Domain dom:((RecordDomain)d).getComponents().values())
				if(domainMap.get(dom)==null){
					allBaseDomainsMapped=false;
					break;
				}
			if(allBaseDomainsMapped){
				RecordDomainM2 dM2 = schemagraph.createRecordDomainM2();
				dM2.setName(((RecordDomain)d).getName());
				Map<String, Domain> recordMap = ((RecordDomain)d).getComponents();
				for(String key:recordMap.keySet()){
					HasRecordDomainComponentM2 hRDCM2 = 
						schemagraph.createHasRecordDomainComponentM2(dM2, domainMap.get(recordMap.get(key)));
					hRDCM2.setName(key);					
				}			
				domainMap.put(d, dM2);
			}			
		}
	}

	/**
	 * This method creates all <code>GraphClassM2</code> objects
	 * and the <code>isSubGraphClassOfM2</code> edges.	   
	 */
	private void createGraphClassesM2() {
		SchemaM2 schemagraphschema = schemagraph.createSchemaM2();
		schemagraphschema.setName("GrUMLSchema");
		
		if(combineGraphClasses){
			GraphClassM2 gcM2 = schemagraph.createGraphClassM2();
			gcM2.setName(schema.getName());
			gcM2.setIsAbstract(false);
			schemagraph.createContainsGraphClassM2(gcM2, schemagraphschema);
			
			//the new GraphClass
			//"derives" the attributes of ALL GraphClasses of the schema
			TreeSet<String> attributeNames = new TreeSet<String>();
			for(GraphClass gc:schema.getGraphClassesInTopologicalOrder()){
				for(Attribute attr:gc.getOwnAttributeList()){
					if(!attributeNames.contains(attr.getName())){
						createAttributeM2(attr, gcM2);
						attributeNames.add(attr.getName());
					}					
				}
			}			
		}
		else{
			for(GraphClass gc:schema.getGraphClassesInTopologicalOrder()){

				GraphClassM2 gcM2 = schemagraph.createGraphClassM2();
				gcM2.setName(gc.getName());
				gcM2.setIsAbstract(gc.isAbstract());	
				
				schemagraph.createContainsGraphClassM2(gcM2, schemagraphschema);
				
				//for each super class of the given GraphClass
				//"the one and only" equivalent GraphClassM2 
				//,which was surely created before(see getGraphClassesInTopologicalOrder() in Schema.java),
				//gets determined.
				for(AttributedElementClass gcSuperClass: gc.getDirectSuperClasses())
					for(GraphClassM2 aM2GraphClass: schemagraph.getGraphClassM2Vertices())
						if(aM2GraphClass.getName().equals(gcSuperClass.getName()))
							schemagraph.createIsSubGraphClassOfM2(gcM2, aM2GraphClass);
				
				//print all attributed of this GraphClass
				for(Attribute attr:gc.getOwnAttributeList())
					createAttributeM2(attr, gcM2);			
			}
	
		}
	}	
	
	/**
	 * This method creates all <code>VertexClassM2</code> objects,
	 * the <code>isSubVertexClassOfM2</code> edges and
	 * the <code>containsGraphElementClassM2</code> edge. 
	 */
	private void createVertexClassesM2() {
		//for each vertex...
		for(VertexClass vc:schema.getVertexClassesInTopologicalOrder()){
			VertexClassM2 vcM2 = schemagraph.createVertexClassM2();
			vcM2.setName(vc.getName());
			vcM2.setIsAbstract(vc.isAbstract());
			

			//..find the GraphClassM2, it is corresponding to.
			//..the ContainsGraphElementClass gets generated.
			for(GraphClassM2 gcM2:schemagraph.getGraphClassM2Vertices())
					schemagraph.createContainsGraphElementClassM2(vcM2, gcM2);
	
		

			//..each super class link gets created.
			for(AttributedElementClass vcSuperClass: vc.getDirectSuperClasses())
				for(VertexClassM2 aM2VertexClass: schemagraph.getVertexClassM2Vertices())
					if(aM2VertexClass.getName().equals(vcSuperClass.getName()))
						schemagraph.createIsSubVertexClassOfM2(vcM2, aM2VertexClass);

			//..each attribute gets created.
			for(Attribute attr: vc.getOwnAttributeList())
				createAttributeM2(attr, vcM2);
		}
		
	}
	
	/**
	 * This method creates all <code>EdgeClassM2</code> objects,
	 * the <code>FromM2</code> and <code>ToM2</code> edges,
	 * the <code>isSubEdgeClassOfM2</code> edges and
	 * the <code>containsGraphElementClassM2</code> edge. 
	 */	
	private void createEdgeClassesM2(){
		//for each edge class..
		for(EdgeClass ec:schema.getEdgeClassesInTopologicalOrder()){
			EdgeClassM2 ecM2 = null;
			
			//..either an EdgeClassM2 or EdgeClassM2 subclass objects gets created.
			
			if(ec instanceof CompositionClass)
				ecM2 = schemagraph.createCompositionClassM2();
			else if(ec instanceof AggregationClass)
				ecM2 = schemagraph.createAggregationClassM2();
			else ecM2 = schemagraph.createEdgeClassM2();
			
			ecM2.setName(ec.getName());
			ecM2.setIsAbstract(ec.isAbstract());
			
			//..the FromM2 aggregation gets created.
			//..the FromM2 attributes gets initialized.
			for(VertexClassM2 vcFrom:schemagraph.getVertexClassM2Vertices())
				if (vcFrom.getName().equals(ec.getFrom().getName())){
					FromM2 fromM2 = schemagraph.createFromM2(ecM2, vcFrom);
					fromM2.setRoleName(ec.getFromRolename());
					fromM2.setMin(ec.getFromMin());
					fromM2.setMax(ec.getFromMax());					
				}
			
			//..the ToM2 aggregation gets created.
			//..the ToM2 attributes gets initialized.
			for(VertexClassM2 vcTo:schemagraph.getVertexClassM2Vertices())
				if (vcTo.getName().equals(ec.getTo().getName())){
					ToM2 toM2 = schemagraph.createToM2(ecM2, vcTo);
					toM2.setRoleName(ec.getToRolename());
					toM2.setMin(ec.getToMin());
					toM2.setMax(ec.getToMax());					
				}

			//..find the GraphClassM2, it is corresponding to.
			//..the ContainsGraphElementClass gets generated.
			for(GraphClassM2 gcM2:schemagraph.getGraphClassM2Vertices())
					schemagraph.createContainsGraphElementClassM2(ecM2, gcM2);
					

			//..each super class link gets created.
			for(AttributedElementClass vcSuperClass: ec.getDirectSuperClasses())
				for(EdgeClassM2 aM2EdgeClass: schemagraph.getEdgeClassM2Vertices())
					if(aM2EdgeClass.getName().equals(vcSuperClass.getName()))
						schemagraph.createIsSubEdgeClassOfM2(ecM2, aM2EdgeClass);

			//..each attribute gets created.
			for (Attribute attr:ec.getOwnAttributeList())
				createAttributeM2(attr, ecM2);
		}
	}
	
	/**
	 * This method creates <code>AttributeM2</code> objects.
	 * It gets called by <code>createGraphClassesM2</code>, <code>createVertexClassesM2</code> or <code>createEdgeClassesM2</code>.
	 * The association to the caller: <code>hasAttributeM2</code>, 
	 * and the association to the <code>AttributeM2</code>'s <code>DomainM2</code>: <code>hasDomainM2</code>
	 * also get created here.
	 */	
	private void createAttributeM2(Attribute attr, AttributedElementClassM2 elemM2) {
		AttributeM2 attrM2 = schemagraph.createAttributeM2();
		attrM2.setName(attr.getName());
		
		//the link HasAttributeM2 from AttributedElementClassM2 to AttributeM2 gets created.
		schemagraph.createHasAttributeM2(attrM2, elemM2);
		
		schemagraph.createHasDomainM2(domainMap.get(attr.getDomain()), attrM2);
	}

	/**
	 * This methods writes the schemagrpah to a file
	 * (see GraphIO.java)
	 */ 
	public void saveSchemaGraphToFile(String filename, ProgressFunction pf){
		try {
			GraphIO.saveGraphToFile(filename, getSchemaGraph(), pf);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method writes the schemagraph to an DataOutputStream
	 * (see GraphIO.java)
	 */ 
	public void saveSchemaGraphToStream(DataOutputStream stream, ProgressFunction pf){
		try {
			GraphIO.saveGraphToStream(stream, getSchemaGraph(), pf);
			
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * writes a schema's schemagraph to a file.
	 * the schema .tg file and the outputfile get defined by the command line options
	 */
	public static void main(String[] args){
		Tg2SchemaGraph tg2sg = new Tg2SchemaGraph();
		tg2sg.getOptions(args);
		tg2sg.saveSchemaGraphToFile();
	}	
	
	/**
	 * sets the local variable Schema schema.
	 * Only used in command line mode.  
	 */
	private void setSchema(String filename) throws GraphIOException{
		schema=GraphIO.loadSchemaFromFile(filename);
	}
	
	/**
	 * this method is used, if Tg2SchemaGraph was called from the command line
	 */
	private void saveSchemaGraphToFile() {
		try {
			GraphIO.saveGraphToFile(outputFilename, getSchemaGraph(), null);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * This methods processes the command-line arguments.
	 * uses gnu.getopt.GetOpt and gnu.getopt.LongOpt
	 */
	private void getOptions(String[] args) {
		LongOpt[] longOptions = new LongOpt[4];

		int c = 0;
		longOptions[c++] = 
			new LongOpt(
				"schema", 
				LongOpt.REQUIRED_ARGUMENT,
				null, 
				's');
		longOptions[c++] = new LongOpt(
				"output", 
				LongOpt.REQUIRED_ARGUMENT,
				null, 
				'o');
		longOptions[c++] = new LongOpt(
				"combine", 
				LongOpt.NO_ARGUMENT,
				null, 
				'c');
		longOptions[c++] = new LongOpt(
				"help", 
				LongOpt.NO_ARGUMENT, 
				null, 
				'h');

		Getopt g = new Getopt("Tg2SchemaGraph", args, "s:o:h", longOptions);
		c = g.getopt();
		String schemaName = null;
		while (c >= 0) {
			switch (c) {
			case 's':
				try {
					schemaName = g.getOptarg();
					setSchema(schemaName);
				} catch (GraphIOException e) {
					System.err.println("Coundn't load schema in file '"
							+ schemaName + "': " + e.getMessage());
					if (e.getCause() != null) {
						e.getCause().printStackTrace();
					}
					System.exit(1);
				}
				break;
			case 'o':
				outputFilename = g.getOptarg();
				if (outputFilename == null) {
					usage(1);
				}
				break;
			case 'c':
				combineGraphClasses=true;
			case '?':
			case 'h':
				usage(0);
				break;
			default:
				throw new RuntimeException("FixMe (c='" + (char) c + "')");
			}
			c = g.getopt();
		} 
		if (g.getOptind() < args.length) {
			System.err.println("Extra arguments!");
			usage(1);
		} 
		if (g.getOptarg() == null) {
			System.out.println("Missing option");
			usage(1);
		}
		if (outputFilename == null) {
			outputFilename = schema.getName()+"schemagraph.tg";
		}
	}
	
	/**
	 * A help message.
	 * Printed, when invalid command-line options or command line option -h was typed 
	 */
	private void usage(int exitCode) {
		System.err.println("Usage: Tg2SchemaGraph -s schemaFileName [options]");
		
		System.err.println("Options are:");
		System.err
				.println("-s schemaFileName  (--schema)    the schema to be converted");
		System.err
				.println("-o outputFileName  (--output)    the output file name. If it is empty");
		System.err
				.println("                                 schema.getName()+\"schemagraph\" is used.");
		System.err
				.println("-c                 (--combine)   combines all GraphClasses to one GraphClass.");
		System.err
				.println("                                 This makes the schemagraph suitable for valid GXL.");

		System.err
				.println("-h                 (--help)      prints usage information");

		System.exit(exitCode);
	}

}