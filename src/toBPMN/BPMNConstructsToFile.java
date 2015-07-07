package toBPMN;
import patternDiscovery.*;
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

import utilities.BPMNElementTagName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;
import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;

public class BPMNConstructsToFile extends DirectedAcyclicGraph {

	
	private static final String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
	private static final String startEventID = "0001";
	private static final String endEventID = "0009";
	private static final ArrayList<OperationTypeName> nonBlockingOperations = new ArrayList<OperationTypeName>();
	private static final ArrayList<OperationTypeName> blockingOperations = new ArrayList<OperationTypeName>();
	private static final ArrayList<OperationTypeName> uncertainBlockingTypeOperations = new ArrayList<OperationTypeName>();

	public BPMNConstructsToFile(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// String BPMN = toStringBPMN();
		//ETLFlowGraph G = XLMParser.getXLMGraph();
		//String BPMN = toStringBPMNWithDictionary();
		//toFileBPMN(BPMN);
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

	public static String toStringBPMNWithDictionary(ETLFlowGraph G) {
		//all graph operations
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		
		//gets an array list of pattern name flags for each optype in the dictionary
		
		//all elements from the dictionary that belong to the graph of this xLM document
		ArrayList<BPMNElement> graphElements = PatternDiscovery.translateToBPMN(G);
		//edges
		ArrayList<BPMNElement> edges = BPMNConstructsGenerator.getBPMNElementsEdge(G);
		graphElements.addAll(edges);
		//startEvent and edges
		ArrayList<BPMNElement> startAndEgdes = BPMNConstructsGenerator.createProcessStartEvent(G);
		graphElements.addAll(startAndEgdes);
		//end event and edges
		ArrayList<BPMNElement> endAndEgdes = BPMNConstructsGenerator.createBPMNEndEvent(G);
		graphElements.addAll(endAndEgdes);
		//all elements that belong inside of a process tag and require a 
		// single--one-line self closing tag, like a task without i/o's
		ArrayList<HashMap> simpleProcessElements = new ArrayList<HashMap>();
		
		//all elements that belong inside of a process but have subelements inside, like a task with an I/O
		ArrayList<HashMap> complexProcessElements = new ArrayList<HashMap>();
		
		//all elements that are inside another element, like the data I/O association
		ArrayList<HashMap> subElements = new ArrayList<HashMap>();
		
		//a list of bpmn element collaboration with corresponding subelements
		ArrayList<BPMNElement> poolElements = BPMNConstructsGenerator.getPoolElements(ops);
		
		//all elements & attrs necessary to fill in the collaboration for the bpmn model
		ArrayList<HashMap> collaborationElements = BPMNConstructsGenerator.getPoolElementsVelocityFormat(poolElements);
		
		//all necessary subelements(participant) to fill the collaboration for the bpmn model
		ArrayList<HashMap> collborationSubElements = BPMNConstructsGenerator.getPoolSubElementsVelocityFormat(poolElements);
		
		ArrayList<HashMap> processTagElements = BPMNConstructsGenerator.getProcessTagElementsVelocityFormat(poolElements);
		
		//all elements that belong after the process tag is over, eg dataStore
		ArrayList<HashMap> dataStoreElements = new ArrayList<HashMap>();
		
		//all header elements that belong in the beginning of each bpmn model before the process starts
		ArrayList<HashMap> headerElements = new ArrayList<HashMap>();
		
		String stringAttributes = "";	
		int counter = 0;
		for (BPMNElement el : graphElements) {
			//System.out.println(el.getElementName()+" "+el.getSubElements().size());
			HashMap element = new HashMap();
			if (el.getElementName().equals("dataStoreReference")) counter++;
			String dataStoreName="";
			for (BPMNAttribute attr : el.getAttributes()) {
				if (attr.name.equals("dataStoreRef")) attr.setAttributeValue("DS_"+String.valueOf(counter));
				System.out.println(attr.name+" "+attr.value+" "+counter);
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			element.put("attributes", stringAttributes);
			stringAttributes = "";
			// System.out.println(optypeMapping.get(str).getElementName());
			element.put("name", el.getElementName());
			element.put("id", el.getElementID());
			
			if (el.getElementName().equals("dataStoreReference")){
				HashMap dsElement = new HashMap();
				dsElement.put("name", "dataStore");
				stringAttributes = "id=\""+"DS_"+counter+ "\""+" isUnlimited=\""+"false"+"\"";
				dsElement.put("attributes", stringAttributes);
				stringAttributes = "";
				dataStoreElements.add(dsElement);
			// for recovery point: need to test
			} if (el.getElementName().equals(BPMNElementTagName.textAnnotation.name())){
				String text = "<text>recoveryPoint</text>";
				el.setText(text);
				collborationSubElements.add(element);
			} if(el.getSubElements().size() < 1 && !el.getElementName().equals(BPMNElementTagName.conditionExpression.name())){
				simpleProcessElements.add(element);
			} else if(el.getSubElements().size() >= 1){
					complexProcessElements.add(element);
					subElements.addAll(loadSubElements(el));
		}
		}
		
		//System.out.println(subElements);
		
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate("vmTemplates2//jsonTest.vm");
		VelocityContext context = new VelocityContext();
		context.put("simpleElements", simpleProcessElements);
		context.put("complexElements", complexProcessElements);
		context.put("collaborationElements", collaborationElements);
		context.put("collaborationSubElements", collborationSubElements);
		context.put("subElements", subElements);
		context.put("processTagElements", processTagElements);
		context.put("dataStoreElements", dataStoreElements);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return (writer.toString());
	}
	
	public static ArrayList<HashMap> loadSubElements(BPMNElement el){
		ArrayList<HashMap> subElements = new ArrayList<HashMap>();
		String stringAttributes="";
		for (BPMNElement subEl: el.getSubElements()){
			HashMap subElement = new HashMap();
			subElement.put("elementID", el.getElementID());
			subElement.put("subName", subEl.getElementName());
			System.out.println(el.getElementName()+" "+subEl.getElementName()+" "+ subEl.getElementText());
			subElement.put("text", subEl.getElementText());
			//TODO:edge after splitter has a text value and not just attributes. How to deal with that?
			for (BPMNAttribute attr : subEl.getAttributes()) {
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			subElement.put("attributes", stringAttributes);		
			stringAttributes = "";
			subElements.add(subElement);
		}
		return subElements;
	}

	public static String toStringBPMN(ETLFlowGraph G) {
		//ETLFlowGraph G = XLMParser.getXLMGraph();

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
		nodesWithInput = XLMParser.nodesWithDataInput(G);
		nodesWithOutput = XLMParser.nodesWithDataOutput(G);
		pools = XLMParser.flowEngineTypes(ops);

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
		//System.out.println("nodes " + nodesWithDBOutput);
		//System.out.println("op " + op.getNodeID());
		System.out.println("operation id " + op.getNodeID()
				+ " boolean for nodes with output "
				+ nodesWithDBOutput.contains(op.getNodeID()));
		if (nodesWithDBOutput.contains(op.getNodeID()) == true) {
			node.put("hasOutput", "Y");
		} else
			node.put("hasOutput", "N");
	}
}
