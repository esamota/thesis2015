import importXLM.ImportXLMToETLGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import operationDictionary.OperationTypeName;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.GraphPath;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;

public class BPMNConstructs extends DirectedAcyclicGraph {

	public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\q3.xml";
	//public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-initial_agn.xml";
	public static String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
	public static String startEventID = "0001";
	public static String endEventID = "0009";

	public BPMNConstructs(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String BPMN = toStringBPMN(XLMFilePathInput);
		toFileBPMN(BPMN);

	}

	public static String toStringBPMN(String XLMFilePathInput) {
		ImportXLMToETLGraph importXlm = new ImportXLMToETLGraph();
		ETLFlowGraph G = new ETLFlowGraph();
		try {
			G = importXlm.getFlowGraph(XLMFilePathInput);
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(G);
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		Hashtable<String, ArrayList<ETLNonFunctionalCharacteristic>> props = G
				.getFlowProperties();
		Hashtable<String, ArrayList<ETLNonFunctionalCharacteristic>> feats = G
				.getFlowFeatures();
		Hashtable<String, ArrayList<ETLNonFunctionalCharacteristic>> resrs = G
				.getFlowResources();
		Hashtable<String, Object> flowMeta = G.getFlowMetadata();
		// System.out.println("Flow meta " +flowMeta);

		ArrayList<HashMap> nodes = new ArrayList<HashMap>();
		ArrayList<HashMap> edges = new ArrayList<HashMap>();
		ArrayList<HashMap> sources = new ArrayList<HashMap>();
		ArrayList<HashMap> targets = new ArrayList<HashMap>();
		ArrayList<HashMap> dbInputNodes = new ArrayList<HashMap>();
		ArrayList<HashMap> engines = new ArrayList<HashMap>();
		
		ArrayList<Integer> nodesWithInput = new ArrayList<Integer>();
		ArrayList<Integer> nodesWithOutput = new ArrayList<Integer>(); 
		
		//populate arraylists of nodes with inputs and outputs
		nodesWithInput = nodesWithDataInput(G, ops);
		nodesWithOutput = nodesWithDataOutput(G, ops);
		engines = flowEngineTypes(ops);

		// source nodes of the graph to use when connecting the start even in
		// the template
		ArrayList<Integer> allSourceNodes = new ArrayList<Integer>();
		allSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> targetOfSourceNodes = new ArrayList<Integer>();

		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		for (Integer i : allSourceNodes) {
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				System.out.println("lala: " + i);
				for (Object e : G.edgeSet()) {
					// is there a simpler way to do this????
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							&& !targetOfSourceNodes.contains(targetId)) {
						sourceNodes.add(targetId);
					}
				}
			} else {
				sourceNodes.add(i);
			}
		}
		// System.out.println(sourceNodes);

		// fill in the source arraylist
		for (Integer i : sourceNodes) {
			HashMap source = new HashMap();
			source.put("id", i);
			source.put("size", sourceNodes.size());
			sources.add(source);
		}

		// target nodes of the graph to use when connecting to the final BPMN
		// place
		ArrayList<Integer> allTargetNodes = new ArrayList<Integer>();
		allTargetNodes = G.getAllTargetNodes();
		ArrayList<Integer> sourceOfTargetNodes = new ArrayList<Integer>();
		ArrayList<Integer> targetNodes = new ArrayList<Integer>();

		for (Integer i : allTargetNodes) {
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				System.out.println("all target nodes: " + i);
				for (Object e : G.edgeSet()) {
					// is there a simpler way to do this????
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (targetId.intValue() == i.intValue()
							&& !sourceOfTargetNodes.contains(sourceId)) {
						targetNodes.add(sourceId);
					}
				}
			} else {
				targetNodes.add(i);
			}
		}
		System.out.println("not datastore target nodes " + targetNodes);

		// fill in the source arraylist
		for (Integer i : targetNodes) {
			HashMap target = new HashMap();
			target.put("id", i);
			target.put("size", targetNodes.size());
			targets.add(target);
		}

		int nodeCnt = -1;

		HashMap<Integer, Integer> added = new HashMap<Integer, Integer>();
		

		for (Object e : G.edgeSet()) {
			// adding link
			HashMap edge = new HashMap();
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			
			int srcID = -1, targetID = -1;

			if (added.containsKey(opS.getNodeID())) {
				srcID = added.get(opS.getNodeID());
			} else {
				srcID = ++nodeCnt;
				HashMap nodeS = new HashMap();
				fillInNodeMap(opS, nodeS, nodesWithInput, nodesWithOutput);
				nodes.add(nodeS);
				added.put(opS.getNodeID(), srcID);
			}

			if (added.containsKey(opT.getNodeID())) {
				targetID = added.get(opT.getNodeID());
			} else {
				targetID = ++nodeCnt;
				HashMap nodeT = new HashMap();
				fillInNodeMap(opT, nodeT, nodesWithInput, nodesWithOutput);
				nodes.add(nodeT);
				added.put(opT.getNodeID(), targetID);
			}

			// for edges get nodeID instead of name
			if (opS.getNodeID() == 1){
				edge.put("from", 111111);
			} else edge.put("from", opS.getNodeID());
			if (opT.getNodeID() == 1){
				edge.put("to", 111111);
			} else edge.put("to", opT.getNodeID());
			edge.put("fromKind", opS.getNodeKind());
			edge.put("toKind", opT.getNodeKind());
			edge.put("fromOpType", opS.getOperationType().getOpTypeName());
			edge.put("toOpType", opT.getOperationType().getOpTypeName());
			edge.put("fromEngine", opS.getEngine().toString());
			edge.put("toEngine", opT.getEngine().toString());
			edge.put("enabled", "Y");
			edges.add(edge);

		}

		// ndproperties
		ArrayList<HashMap> properties = new ArrayList<HashMap>();
		for (String key : props.keySet()) {
			for (ETLNonFunctionalCharacteristic c : props.get(key)) {
				HashMap prop = new HashMap();
				prop.put("name", key);
				prop.put("leftfun", c.getLeftFun());
				prop.put("leftop", c.getLeftOp());
				prop.put("oper", c.getOper());
				prop.put("rightfun", c.getRightFun());
				prop.put("rightop", c.getRightOp());
				properties.add(prop);
			}
		}

		// ndresources
		ArrayList<HashMap> resources = new ArrayList<HashMap>();
		for (String key : resrs.keySet()) {
			for (ETLNonFunctionalCharacteristic c : resrs.get(key)) {
				HashMap res = new HashMap();
				res.put("name", key);
				res.put("leftfun", c.getLeftFun());
				res.put("leftop", c.getLeftOp());
				res.put("oper", c.getOper());
				res.put("rightfun", c.getRightFun());
				res.put("rightop", c.getRightOp());
				resources.add(res);
			}
		}

		// ndfeatures
		ArrayList<HashMap> features = new ArrayList<HashMap>();
		for (String key : feats.keySet()) {
			for (ETLNonFunctionalCharacteristic c : feats.get(key)) {
				HashMap feat = new HashMap();
				feat.put("name", key);
				feat.put("leftfun", c.getLeftFun());
				feat.put("leftop", c.getLeftOp());
				feat.put("oper", c.getOper());
				feat.put("rightfun", c.getRightFun());
				feat.put("rightop", c.getRightOp());
				features.add(feat);
			}
		}
		
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate("vmTemplates//bpmn.vm");
		VelocityContext context = new VelocityContext();
		context.put("edges", edges);
		context.put("nodes", nodes);
		context.put("sources", sources);
		context.put("targets", targets);
		// context.put("properties", properties);
		// context.put("resources", resources);
		// context.put("features", features);
		context.put("metadata", flowMeta);
		context.put("engines", engines);
		
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return (writer.toString());
	}
	
	public static void fillInNodeMap(ETLFlowOperation op, HashMap node, ArrayList<Integer> nodesWithDBInput, 
										ArrayList<Integer> nodesWithDBOutput) {
		// id's 0 and 1 are reserved in Yaoquiang, and therefore the editor inserts an extra "_" before such values which creates problems
		if (op.getNodeID() == 1){
			node.put("id", 111111);
		} else node.put("id", op.getNodeID());
		node.put("name", op.getOperationName());
		node.put("type", op.getNodeKind().name());
		node.put("optype", op.getOperationType().getOpTypeName());
		node.put("engine", op.getEngine().name());
		node.put("flowID", op.getParentFlowID());
		node.put("implementationType", op.getImplementationType());
		//insert a flag if node has db input 
		if (nodesWithDBInput.contains(op.getNodeID()) == true){
			node.put("hasInput", "Y");
		} else node.put("hasInput", "N");
		
		//insert a flag if node has db output
		System.out.println("nodes "+ nodesWithDBOutput);
		System.out.println("op "+ op.getNodeID());
		System.out.println ("operation id "+ op.getNodeID()+ " boolean for nodes with output " + nodesWithDBOutput.contains(op.getNodeID()));
		if (nodesWithDBOutput.contains(op.getNodeID()) == true){
			node.put("hasOutput", "Y");
		}
		else node.put("hasOutput", "N");
		
		}
	

	public static void toFileBPMN(String writerInput) {
		File file = new File(BPMNFilePathOutput);
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(writerInput);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// identify all nodes that have a data input going in
	public static ArrayList<Integer> nodesWithDataInput(ETLFlowGraph G,
			Hashtable<Integer,ETLFlowOperation> ops) {
		ArrayList<Integer> nodesWithDBInput = new ArrayList<Integer>();
		//ArrayList<HashMap> dbInputNodes = new ArrayList<HashMap>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			
			Integer sourceId = (Integer) ((ETLEdge) e).getSource();
			Integer targetId = (Integer) ((ETLEdge) e).getTarget();
			if (targetId.intValue() == opT.getNodeID()
					&& opS.getOperationType().getOpTypeName()
							.equals(OperationTypeName.TableInput)) {
				System.out.println("targetId " + targetId);
				nodesWithDBInput.add(targetId);
			}
		}
		/*for (Integer i : nodesWithDBInput) {
			HashMap dbInputNode = new HashMap();
			dbInputNode.put("id", i);
			dbInputNodes.add(dbInputNode);
		}*/

		// System.out.println("dbInputNodes end " + dbInputNodes);
		return nodesWithDBInput;
	}

	// identify all nodes that have a data output coming out
	public static ArrayList<Integer> nodesWithDataOutput(ETLFlowGraph G,
			Hashtable<Integer,ETLFlowOperation> ops) {
		ArrayList<Integer> nodesWithDBOutput = new ArrayList<Integer>();
		ArrayList<HashMap> dbOutputNodes = new ArrayList<HashMap>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			// is there a simpler way to do this????
			Integer sourceId = (Integer) ((ETLEdge) e).getSource();
			Integer targetId = (Integer) ((ETLEdge) e).getTarget();
			// System.out.println("targetId " + targetId);
			if (sourceId.intValue() == opS.getNodeID()
					&& opT.getOperationType().getOpTypeName()
							.equals(OperationTypeName.TableOutput)) {
				System.out.println("sourceId " + sourceId);
				nodesWithDBOutput.add(sourceId);
			}
		}
//System.out.println ("nodesWithDBOutput within method "+ nodesWithDBOutput);
		/*for (Integer i : nodesWithDBOutput) {
			HashMap dbOutputNode = new HashMap();
			dbOutputNode.put("id", i);
			dbOutputNodes.add(dbOutputNode);
		}*/

		// System.out.println("dbOutputNodes end " + dbOutputNodes);
		return nodesWithDBOutput;
	}
	

	public static ArrayList<HashMap> flowEngineTypes(Hashtable<Integer,ETLFlowOperation> ops) {
		ArrayList<String> engineTypes = new ArrayList<String>();
		ArrayList<HashMap> engines = new ArrayList<HashMap>();
		System.out.println("engineTypes before " + engineTypes);
		for (Integer i: ops.keySet()){
			System.out.println("get engine " +ops.get(i).getEngine().toString());
			System.out.println("engineTypes contains " + engineTypes.contains(ops.get(i).getEngine().toString()));
			if (!engineTypes.contains(ops.get(i).getEngine().toString())){
				engineTypes.add(ops.get(i).getEngine().toString());
			} else if (ops.get(i).getEngine().toString().isEmpty()){
				System.out.println("empty engine info");
			}
			
		}
		System.out.println("engineTypes after " + engineTypes);
		//engineTypes.add("PDI");
		
		for(String str: engineTypes){
			HashMap engine = new HashMap();
			engine.put("name", str);
			engine.put("size", engineTypes.size());
			engines.add(engine);
		}
		System.out.println("engineTypes "+engineTypes);
		return engines;
	}

}
