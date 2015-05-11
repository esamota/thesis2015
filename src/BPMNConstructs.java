import importXLM.ImportXLMToETLGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import operationDictionary.ETLOperationType;
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

	private static final String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\q1.xml";
	// public static String XLMFilePathInput =
	// "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-initial_agn.xml";
	private static final String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
	private static final String startEventID = "0001";
	private static final String endEventID = "0009";
	private static final ArrayList<OperationTypeName> nonBlockingOperations = new ArrayList<OperationTypeName>();
	private static final ArrayList<OperationTypeName> blockingOperations = new ArrayList<OperationTypeName>();
	private static final ArrayList<OperationTypeName> uncertainBlockingTypeOperations = new ArrayList<OperationTypeName>();

	public BPMNConstructs(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// String BPMN = toStringBPMN(XLMFilePathInput);
		String BPMN = toStringBPMNWithDictionary(XLMFilePathInput);
		toFileBPMN(BPMN);
	}

	public static String toStringBPMNWithDictionary(String XLMFilePathInput) {
		ImportXLMToETLGraph importXlm = new ImportXLMToETLGraph();
		ETLFlowGraph G = new ETLFlowGraph();
		try {
			G = importXlm.getFlowGraph(XLMFilePathInput);
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//all graph operations
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		//parsed JSON dictionary
		HashMap<String, ArrayList<BPMNElement>> mapping = JSONDictionary
				.parseJSONDictionary();
		//gets an array list of pattern name flags for each optype in the dictionary
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionary.getPatternFlagsPerNode();
		//all elements from the dictionary that belong to the graph of this xLM document
		ArrayList<BPMNElement> graphElements = getGraphElements(G, ops, mapping);
		//add a dataStore element in case it needs to be references by one of the elements
		graphElements.add(createDataStoreElement());
		//all elements that belong inside of a process tag and require a 
		// single--one-line self closing tag, like a task without i/o's
		ArrayList<HashMap> simpleProcessElements = new ArrayList<HashMap>();
		//all elements that belong inside of a process but have subelements inside, like a task with an I/O
		ArrayList<HashMap> complexProcessElements = new ArrayList<HashMap>();
		//all elements that are inside another element, like the data I/O association
		ArrayList<HashMap> subElements = new ArrayList<HashMap>();
		//all elements necessary to fill in the collaboration for the bpmn model
		ArrayList<HashMap> collaborationElements = new ArrayList<HashMap>();
		//all elements that belong after the process tag is over, eg dataStore
		ArrayList<HashMap> nonProcessElements = new ArrayList<HashMap>();
		//all header elements that belong in the beginning of each bpmn model before the process starts
		ArrayList<HashMap> headerElements = new ArrayList<HashMap>();
		
		String stringAttributes = "";
//System.out.println(graphElements);
		
		for (BPMNElement el : graphElements) {
			System.out.println(el.getElementName()+" "+el.getSubElements().size());
			HashMap element = new HashMap();
			
			for (BPMNAttribute attr : el.getAttributes()) {
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			
			element.put("attributes", stringAttributes);
			stringAttributes = "";
			// System.out.println(optypeMapping.get(str).getElementName());
			element.put("name", el.getElementName());
			if(el.getSubElements().size() < 1){
				//create a dataStore for each process even if it is not being referenced later
				if(el.getElementName().equals(BPMNElementTagName.dataStore.name())){
					nonProcessElements.add(element);
				} else 
				simpleProcessElements.add(element);
			}
			else if(el.getSubElements().size() >= 1){
				if(el.getElementName().equals(BPMNElementTagName.collaboration.name())){
					collaborationElements.add(element);
				} else
					complexProcessElements.add(element);
			for (BPMNElement subEl: el.getSubElements()){
				HashMap subElement = new HashMap();
				subElement.put("name", subEl.getElementName());
				for (BPMNAttribute attr : subEl.getAttributes()) {
					stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
				subElement.put("attributes", stringAttributes);		
				stringAttributes = "";
			subElements.add(subElement);
			}
		}
		}
		
		//System.out.println(simpleElements);
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate("vmTemplates2//jsonTest.vm");
		VelocityContext context = new VelocityContext();
		context.put("simpleElements", simpleProcessElements);
		context.put("complexElements", complexProcessElements);
		context.put("collaborationElements", collaborationElements);
		context.put("subElements", subElements);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return (writer.toString());
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
		ArrayList<HashMap> pools = new ArrayList<HashMap>();

		ArrayList<Integer> nodesWithInput = new ArrayList<Integer>();
		ArrayList<Integer> nodesWithOutput = new ArrayList<Integer>();

		// populate arraylists of nodes with inputs and outputs
		nodesWithInput = nodesWithDataInput(G, ops);
		nodesWithOutput = nodesWithDataOutput(G, ops);
		pools = flowEngineTypes(ops);

		// source nodes of the graph to use when connecting the start even in
		// the template. check here is the source node is a datastore, then the
		// target task of that datastore should be considered a source node.
		// if the target task is a join or a leftouterjoin, then it is not a
		// source node, since the datastore can be one of the things to be
		// joined
		// and doesn't need to be connected to the start place -- see example in
		// q13.xml
		ArrayList<Integer> allSourceNodes = new ArrayList<Integer>();
		allSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> targetOfSourceNodes = new ArrayList<Integer>();

		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		for (Integer i : allSourceNodes) {
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				//System.out.println("lala: " + i);
				for (Object e : G.edgeSet()) {
					// is there a simpler way to do this????
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							&& !targetOfSourceNodes.contains(targetId)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.Join)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.LeftOuterJoin)) {
						sourceNodes.add(targetId);
					}
				}
			} else {
				sourceNodes.add(i);
			}
		}
		//System.out.println("sourceNodes " + allSourceNodes);
		// sourceNodes.remove(34);

		// fill in the source arraylist
		for (Integer i : sourceNodes) {
			HashMap source = new HashMap();
			if (i == 1) {
				source.put("id", 111111);
			} else
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
				//System.out.println("all target nodes: " + i);
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
		//System.out.println("not datastore target nodes " + targetNodes);

		// fill in the source arraylist
		for (Integer i : targetNodes) {
			HashMap target = new HashMap();
			if (i == 1) {
				target.put("id", 111111);
			} else
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
			if (opS.getNodeID() == 1) {
				edge.put("from", 111111);
			} else
				edge.put("from", opS.getNodeID());
			if (opT.getNodeID() == 1) {
				edge.put("to", 111111);
			} else
				edge.put("to", opT.getNodeID());
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
		context.put("pools", pools);

		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return (writer.toString());
	}

	public static void fillInNodeMap(ETLFlowOperation op, HashMap node,
			ArrayList<Integer> nodesWithDBInput,
			ArrayList<Integer> nodesWithDBOutput) {
		// id's 0 and 1 are reserved in Yaoquiang, and therefore the editor
		// inserts an extra "_" before such values which creates problems
		if (op.getNodeID() == 1) {
			node.put("id", 111111);
		} else
			node.put("id", op.getNodeID());
		node.put("name", op.getOperationName());
		node.put("type", op.getNodeKind().name());
		node.put("optype", op.getOperationType().getOpTypeName());
		node.put("engine", op.getEngine().name());
		node.put("flowID", op.getParentFlowID());
		node.put("implementationType", op.getImplementationType());
		// insert a flag if node has db input
		if (nodesWithDBInput.contains(op.getNodeID()) == true) {
			node.put("hasInput", "Y");
		} else
			node.put("hasInput", "N");

		// insert a flag if node has db output
		System.out.println("nodes " + nodesWithDBOutput);
		System.out.println("op " + op.getNodeID());
		System.out.println("operation id " + op.getNodeID()
				+ " boolean for nodes with output "
				+ nodesWithDBOutput.contains(op.getNodeID()));
		if (nodesWithDBOutput.contains(op.getNodeID()) == true) {
			node.put("hasOutput", "Y");
		} else
			node.put("hasOutput", "N");

		// insert a flag for blocking non-blocking operation
		// datastores are considered non-blocking; UNION is a bit unclear. for
		// now included with Joins and Grouper
		fillInBlockingFlag();

		String opName = op.getOperationType().getOpTypeName().toString();
		for (OperationTypeName opn : nonBlockingOperations) {
			if (opName.equals(opn)) {
				node.put("blocking", "N");
			}
		}
		for (OperationTypeName opn : blockingOperations) {
			if (opName.equals(opn)) {
				node.put("blocking", "Y");
			}
		}

		for (OperationTypeName opn : uncertainBlockingTypeOperations) {
			if (opName.equals(opn)) {
				if (op.getImplementationType().toUpperCase().contains("MERGE")) {
					node.put("blocking", "N");
				} else
					node.put("blocking", "Y");
			}
		}
		if (op.getNodeKind().equals(ETLNodeKind.Datastore)) {
			node.put("blocking", "N");
		}
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
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<Integer> nodesWithDBInput = new ArrayList<Integer>();
		// ArrayList<HashMap> dbInputNodes = new ArrayList<HashMap>();
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
		/*
		 * for (Integer i : nodesWithDBInput) { HashMap dbInputNode = new
		 * HashMap(); dbInputNode.put("id", i); dbInputNodes.add(dbInputNode); }
		 */

		// System.out.println("dbInputNodes end " + dbInputNodes);
		return nodesWithDBInput;
	}

	// identify all nodes that have a data output coming out
	public static ArrayList<Integer> nodesWithDataOutput(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops) {
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
		// System.out.println ("nodesWithDBOutput within method "+
		// nodesWithDBOutput);
		/*
		 * for (Integer i : nodesWithDBOutput) { HashMap dbOutputNode = new
		 * HashMap(); dbOutputNode.put("id", i);
		 * dbOutputNodes.add(dbOutputNode); }
		 */

		// System.out.println("dbOutputNodes end " + dbOutputNodes);
		return nodesWithDBOutput;
	}

	//this was necessary for the hardcoded part, can be removed after getPoolElements method is finished
	public static ArrayList<HashMap> flowEngineTypes(
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<String> engineTypes = new ArrayList<String>();
		ArrayList<HashMap> pools = new ArrayList<HashMap>();
		ArrayList<Integer> flowIDs = new ArrayList<Integer>();
		ArrayList<String> uniquePools = new ArrayList<String>();
		// System.out.println("engineTypes before " + engineTypes);
		for (Integer i : ops.keySet()) {
			if (!engineTypes.contains(ops.get(i).getEngine().toString())) {
				engineTypes.add(ops.get(i).getEngine().toString());
			} else if (ops.get(i).getEngine().toString().isEmpty()) {
				System.out.println("empty engine info");
			}
			if (!flowIDs.contains(ops.get(i).getParentFlowID())) {
				flowIDs.add(ops.get(i).getParentFlowID());
			}
			if (!uniquePools.contains(ops.get(i).getEngine() + "_"
					+ ops.get(i).getParentFlowID())) {
				uniquePools.add(ops.get(i).getEngine() + "_"
						+ ops.get(i).getParentFlowID());
			}
		}
		System.out.println("flowIDS " + flowIDs);
		System.out.println("unique pools " + uniquePools);
		// engineTypes.add("PDI");
		for (Integer i : flowIDs) {
			for (String str : engineTypes) {
				HashMap pool = new HashMap();
				pool.put("name", str + "_" + i);
				pool.put("size", uniquePools.size());
				pool.put("engine", str);
				pool.put("flow", i);
				pools.add(pool);
			}
		}
		System.out.println("engineTypes " + engineTypes);
		return pools;
	}

	public static ArrayList<BPMNElement> getPoolElements(
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<BPMNElement> poolElements = new ArrayList<BPMNElement>();	
		BPMNElement collaborationElement = new BPMNElement(BPMNElementTagName.collaboration.name());
		BPMNAttribute collAttr1 = new BPMNAttribute("id", "COLLABORATION_1");
		BPMNAttribute collAttr2 = new BPMNAttribute("isClosed", "false");
		collaborationElement.addAttribute(collAttr1);
		collaborationElement.addAttribute(collAttr2);
		poolElements.add(collaborationElement);
		
		ArrayList<String> engineTypes = new ArrayList<String>();
		ArrayList<HashMap> pools = new ArrayList<HashMap>();
		ArrayList<Integer> flowIDs = new ArrayList<Integer>();
		ArrayList<String> uniquePools = new ArrayList<String>();
		// System.out.println("engineTypes before " + engineTypes);
		for (Integer i : ops.keySet()) {
			if (!engineTypes.contains(ops.get(i).getEngine().toString())) {
				engineTypes.add(ops.get(i).getEngine().toString());
			} else if (ops.get(i).getEngine().toString().isEmpty()) {
				System.out.println("empty engine info");
			}
			if (!flowIDs.contains(ops.get(i).getParentFlowID())) {
				flowIDs.add(ops.get(i).getParentFlowID());
			}
		}
		int counter = 1;
		for (Integer i : flowIDs) {
			for (String str : engineTypes) {
				//create a process element for each pool
				BPMNElement processElement = new BPMNElement(BPMNElementTagName.process.name());
				BPMNAttribute processAttr1 = new BPMNAttribute("id", "PROCESS_"+counter);
				BPMNAttribute processAttr2 = new BPMNAttribute("isExecutable", "false");
				BPMNAttribute processAttr3 = new BPMNAttribute("processType", "None");
				BPMNAttribute processAttr4 = new BPMNAttribute("name", str+"_"+i );
				ArrayList<BPMNAttribute> processAttributes = new ArrayList<BPMNAttribute>();
				processAttributes.addAll(Arrays.asList(processAttr1, processAttr2, processAttr3, processAttr4));
				processElement.addAttributes(processAttributes);
				//create a participant element for each pool
				BPMNElement participantElement = new BPMNElement(BPMNElementTagName.participant.name());
				BPMNAttribute partAttr1 = new BPMNAttribute("id", "_"+counter);
				BPMNAttribute partAttr2 = new BPMNAttribute("name", str + "_" + i);
				BPMNAttribute partAttr3 = new BPMNAttribute("processRef", "PROCESS_"+counter);
				ArrayList<BPMNAttribute> participantAttributes = new ArrayList<BPMNAttribute>();
				participantAttributes.addAll(Arrays.asList(partAttr1, partAttr2, partAttr3));
				participantElement.addAttributes(participantAttributes);
				collaborationElement.addSubElement(participantElement);
				counter = counter +1;
				poolElements.add(processElement);
			}
		}
		System.out.println("engineTypes " + engineTypes);
		return poolElements;
	}
	public static void fillInBlockingFlag() {
		nonBlockingOperations.addAll(Arrays.asList(OperationTypeName.Splitter,
				OperationTypeName.Router, OperationTypeName.Merger,
				OperationTypeName.Voter, OperationTypeName.Filter,
				OperationTypeName.AttributeAddition, OperationTypeName.Rename,
				OperationTypeName.Project));
		blockingOperations.addAll(Arrays.asList(OperationTypeName.Sort,
				OperationTypeName.TopK, OperationTypeName.UserDefinedFunction));
		uncertainBlockingTypeOperations.addAll(Arrays.asList(
				OperationTypeName.Join, OperationTypeName.LeftOuterJoin,
				OperationTypeName.Grouper, OperationTypeName.Union));
	}

	public static void subprocessExtraction(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops) {
		fillInBlockingFlag();
		ArrayList<Integer> subprocess = new ArrayList<Integer>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			for (OperationTypeName opn : nonBlockingOperations) {
				// nonBlockingOperations.contains(arg0)
				if (opS.getOperationName().equals(opn)
						&& opT.getOperationName().equals(opn)) {
					subprocess.add(opS.getNodeID());
					subprocess.add(opT.getNodeID());
				} else if (opS.getOperationName().equals(opn)
						&& !opT.getOperationName().equals(opn)) {
					if (subprocess.size() > 1) {
						// add subprocess id, and

					}
				}

			}
		}
	}
	
	public static void getGraphElementsPerParticipant(){
		//for each participant, or unique pool, add all tasks that belong there as subelements of the process element
		//then return the process elements to the printing method
		
		
	}

	public static ArrayList<BPMNElement> getGraphElements(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> graphElements = new ArrayList<BPMNElement>();
		//call a method that creates a sequence flow element for each edge
		ArrayList<BPMNElement> edgeElements = fillInEdgeAttributeValues(G, ops, mapping);
		ArrayList<BPMNElement> singleElements = fillInAttributeValuesOneToOne(G, ops, mapping);
		ArrayList<BPMNElement> complexElements = fillInAtrributeValuesOneToMany(ops, mapping);
		ArrayList<BPMNElement> poolElements = getPoolElements(ops);
		
		
		//add sequence flow elements to graphElements
		for (BPMNElement edgeEl: edgeElements){
			graphElements.add(edgeEl);
		}
		for (BPMNElement singleEl: singleElements){
			graphElements.add(singleEl);
		}
		for (BPMNElement complexEl: complexElements){
			graphElements.add(complexEl);
		}
		
		for (BPMNElement poolEl: poolElements){
			graphElements.add(poolEl);
		}
		
		return graphElements;
	}
	
	public static ArrayList<BPMNElement> fillInAttributeValuesOneToOne(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping) {
		ArrayList<BPMNElement> singleElements = new ArrayList<BPMNElement>();

		//one-to-one mappings
		//*****************************************************************************************
		for (Integer key : ops.keySet()) {
			for (String str : mapping.keySet()) {
				//System.out.println("blah2 str");
				//Random randomGenerator = new Random();
				//String randomID= "_0"+randomGenerator.nextInt(100);
				for(BPMNElement el: mapping.get(str)){
				if (str.equals(ops.get(key).getOperationType().getOpTypeName()
						.toString()) && mapping.get(str).size() == 1){
					for (BPMNAttribute attr : el.getAttributes()) {
						
					if (attr.getAttributeValue().equals("")){
						System.out.println(attr.getAttributeName());
						switch(attr.getAttributeName()){
						case "name":
							attr.setAttributeValue(ops.get(key).getOperationName());
								break;
						case "id":
							attr.setAttributeValue("_"
									+ String.valueOf(ops.get(key).getNodeID()));
							break;
					}
				}
					}
					singleElements.add(el);
				} 
			}
		}
		}

		//System.out.println(graphElements);
		return singleElements;
	}
	
	public static ArrayList<BPMNElement> fillInEdgeAttributeValues(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> edgeElements = new ArrayList<BPMNElement>();
		for (String str : mapping.keySet()) {
			if (str.equals("edge")) {
				for (Object e : G.edgeSet()) {
					// adding link
					ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
					ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
					for (BPMNElement el: mapping.get(str)){
					BPMNElement bpmnElement = new BPMNElement(el.getElementName());
					for (BPMNAttribute attr : el.getAttributes()) {
						String optypeName = opS.getOperationType().getOpTypeName().name();
						if (attr.getAttributeName().equals("sourceRef")) {
							if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
								//to match the id of the newly inserted task after the parallel gateway
								attr.setAttributeValue("_0"
										+ String.valueOf(opS.getNodeID()));
							}else attr.setAttributeValue("_"
									+ String.valueOf(opS.getNodeID()));

						} else if (attr.getAttributeName().equals("targetRef")) {
							attr.setAttributeValue("_"
									+ String.valueOf(opT.getNodeID()));

						} else if (attr.getAttributeName().equals("id")) {
							if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
								attr.setAttributeValue("_0"
										+ String.valueOf(opS.getNodeID()) + "-_"
										+ String.valueOf(opT.getNodeID()));	
							} else
							attr.setAttributeValue("_"
									+ String.valueOf(opS.getNodeID()) + "-_"
									+ String.valueOf(opT.getNodeID()));
							//System.out.println(attr.name + " " + attr.value);
						}
						BPMNAttribute bpmnAttr = new BPMNAttribute(attr.name, attr.value);
						bpmnElement.addAttribute(bpmnAttr);
					}
					edgeElements.add(bpmnElement);
				}
			}
		}
		}
		return edgeElements;
	}
	
	public static ArrayList<BPMNElement> fillInAtrributeValuesOneToMany (Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> complexElements = new ArrayList<BPMNElement>();
		for (Integer key : ops.keySet()) {
			System.out.println("blah1 ops");
			for (String str : mapping.keySet()) {
				//System.out.println("blah2 str");
				/*Random randomGenerator = new Random();
				String randomID= "_0"+randomGenerator.nextInt(100);*/
				String randomID="_0"+String.valueOf(ops.get(key).getNodeID());
				for(BPMNElement el: mapping.get(str)){
					//one-to-many mapping for Join and LeftOuterJoin		
					if (str.equals(ops.get(key).getOperationType().getOpTypeName()
							.toString()) && mapping.get(str).size()>1){	

					for (BPMNAttribute attr : el.getAttributes()) {
							
						switch(attr.getAttributeName()){
						case "name":
							if(attr.getAttributeValue().equals("")){
								attr.setAttributeValue(ops.get(key).getOperationName());
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(str);
								break;
							}
							break;
						case "id":
							if(attr.getAttributeValue().equals("") && !el.getElementName().equals("sequenceFlow")){
								attr.setAttributeValue("_"+ String.valueOf(ops.get(key).getNodeID()));
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(randomID);
							} else if (el.getElementName().equals("sequenceFlow")){
								//System.out.println("source and target ref from inside the case id "+sourceRef+"-"+targetRef);
								attr.setAttributeValue("_"+String.valueOf(ops.get(key).getNodeID())+"-"+randomID);
								break;
							}
							break;
						case "sourceRef":
							attr.setAttributeValue("_"+String.valueOf(ops.get(key).getNodeID()));
							break;
						case "targetRef":
							attr.setAttributeValue(randomID);
							break;
						}
						//for a sequence flow coming out of the task
						/*BPMNElement seqFlow2 = new BPMNElement("sequenceFlow");
						BPMNAttribute attr1 = new BPMNAttribute("sourceRef", "randomID");
						BPMNAttribute attr2 = new BPMNAttribute("targetRef", );*/
						}
						complexElements.add(el);
					}
				}
			}
			}

			//System.out.println(graphElements);
			return complexElements;
	}
	
	public static BPMNElement createDataStoreElement(){
		BPMNElement dataStoreElement = new BPMNElement(BPMNElementTagName.dataStore.name());
		BPMNAttribute attr1 = new BPMNAttribute("id", "DS_1");
		BPMNAttribute attr2 = new BPMNAttribute("isUnlimited", "false");
		dataStoreElement.addAttribute(attr1);
		dataStoreElement.addAttribute(attr2);
		
		return dataStoreElement;
	}

}
