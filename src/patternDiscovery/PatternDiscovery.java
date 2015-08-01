package patternDiscovery;
import toBPMN.*;
import utilities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Box.Filler;
import javax.swing.text.Utilities;

import org.apache.xerces.parsers.XMLParser;
import org.jgraph.graph.Edge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import com.mxgraph.swing.util.mxGraphActions.UpdateGroupBoundsAction;

import display.Demo;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;
import operationDictionary.OperationTypeName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;

public class PatternDiscovery extends DirectedAcyclicGraph {
	public static ArrayList<Integer> graphSourceNodes = new ArrayList<>();

	public PatternDiscovery(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	ETLFlowGraph G = utilities.XLMParser.getXLMGraph(Demo.XLMFilePathInput);
	Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
	HashMap<String, ArrayList<String>> flagMappings = JSONDictionaryParser.parsePatternFlags(Demo.patternFlagMappingPath);
	System.out.println("source operations "+getOpeartionSourceNodes(G));
		/*ArrayList<ETLFlowOperation> patternNodes = matchMergeJoin(ops.get(114), G);
		System.out.println("patternNodes: "+ patternNodes.size());
		for (ETLFlowOperation op: patternNodes){
			System.out.println(op.getNodeID());
		}*/

		/*Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation op = ops.get(v);
			System.out.println(v+" "+ op.getOperationType().getOpTypeName());
		}*/
		//System.out.println(utilities.XLMParser.getTargetOperationsGivenSource(ops.get(895), G).size());
		//System.out.println(G.getEtlFlowOperations().size());
		//getAllGraphPatterns(G);
		/*Pattern maxMatchedPattern = getMaxSubgraphMatch(ops.get(946), G, "default");
		ArrayList<ETLFlowOperation> patternNodes = maxMatchedPattern.getPatternSubgraph();
		
		for (ETLFlowOperation op: patternNodes) {
			System.out.println("---------------------> "+op.getOperationName());
		}*/

		//createSubGraphByCloningGraph(G, patternNodes);
	//ArrayList<ETLEdge> edges = getEdgesForSubGraph(G, patternNodes);
	//System.out.println("EDGES: "+edges);
	
	//ETLFlowGraph subGraph = createSubGraph(G, patternNodes);
	//System.out.println("SUBGRAPH: "+subGraph);
		//ArrayList<ETLFlowOperation> maxMatchingPatternSubgraph = getMaxSubgraphMatch(node, G);
		//System.out.println(maxMatchingPatternSubgraph.size());
		
		ArrayList<BPMNElement> graphElements = translateToBPMN(G, flagMappings, Demo.dictionaryFilePath);
		for (BPMNElement element: graphElements){
			System.out.println(element.getElementName());
			for (BPMNElement BPMN: element.getSubElements()){
			System.out.println("subElement "+BPMN.getElementName());
			System.out.println("subElement text"+ BPMN.getElementText());
		}
		}
		
	}
	
	public static ArrayList<ETLEdge> getEdgesForSubGraph(ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		ArrayList<ETLEdge> subGraphEdges = new ArrayList<ETLEdge>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		Integer counter =0;
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			
			if (patternNodes.contains(opS) && patternNodes.contains(opT)){
				counter++;
				subGraphEdges.add((ETLEdge) e);
			}
	}
		return subGraphEdges;
	}
	
	public static ETLFlowGraph createSubGraph(ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLEdge> edges = getEdgesForSubGraph(G, patternNodes);
		ETLFlowGraph subGraph = new ETLFlowGraph();
		
		for (ETLFlowOperation v: patternNodes){
			subGraph.addVertex(v.getNodeID());
			subGraph.addEtlFlowOperations(v.getNodeID(), v);
		}
		
		for (ETLEdge e: edges){
			ETLFlowOperation opS = ops.get(e.getSource());
			ETLFlowOperation opT = ops.get(e.getTarget());
			try {
				subGraph.addDagEdge(opS.getNodeID(), opT.getNodeID());
			} catch (CycleFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return subGraph;
	}
	
	public static ArrayList<ETLFlowOperation> matchMergeJoin(ETLFlowOperation node, ETLFlowGraph G){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		if (node.getOperationType().getOpTypeName().equals(OperationTypeName.Sort)){
			System.out.println("first node is sort "+ node.getNodeID());
			Integer mergeNodeID = 0;
			ArrayList<ETLFlowOperation> targetNodes = XLMParser.getTargetOperationsGivenSource(node, G);
			for (ETLFlowOperation target: targetNodes){
				if (target.getOperationType().getOpTypeName().equals(OperationTypeName.Join)||
						target.getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin)){
					mergeNodeID = target.getNodeID();
				}
			}
			//ETLFlowOperation nextNode = new ETLFlowOperation();
			Iterator<Integer> itr = G.iterator();
			while (itr.hasNext()) {
				Integer v = itr.next();
				ETLFlowOperation nextNode = ops.get(v);
				if ((nextNode.getOperationType().getOpTypeName().equals(OperationTypeName.Join) ||
						nextNode.getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin)) &&
						nextNode.getNodeID() == mergeNodeID){
					System.out.println("found a merge: "+ nextNode.getNodeID());
					if (patternNodes.size() <= 1) return new ArrayList<>();
					else { patternNodes.add(nextNode); return patternNodes;}
				} else if (nextNode.getOperationType().getOpTypeName().equals(OperationTypeName.Sort)){
					System.out.println("found another Sort " + nextNode.getNodeID());
					ArrayList<ETLFlowOperation> targets = XLMParser.getTargetOperationsGivenSource(nextNode, G);
					for (ETLFlowOperation t: targets){
						if ((t.getOperationType().getOpTypeName().equals(OperationTypeName.Join)||
								t.getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin))
								&& t.getNodeID() == mergeNodeID){
							patternNodes.add(nextNode);
						}else if (t.getOperationType().getOpTypeName().equals(OperationTypeName.Splitter) ||
								t.getOperationType().getOpTypeName().equals(OperationTypeName.Router)){
							ArrayList<ETLFlowOperation> splitTargets = XLMParser.getTargetOperationsGivenSource(t, G);
							for (ETLFlowOperation opT: splitTargets){
								if (opT.getNodeID() == mergeNodeID){
									patternNodes.add(nextNode);
									//add the splitter/router itself
									patternNodes.add(t);
								}
							}
						}
					}
					
				}
		}
	}
		System.out.println("never found a merge");
		return new ArrayList<>();
	}
	
	public static Pattern getMaxSubgraphMatch (ETLFlowOperation node, ETLFlowGraph G, HashMap<String, ArrayList<String>> flagMappings, String dictionaryFilePath) {
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		HashMap<Pattern, ArrayList<ETLFlowOperation>> matchedPatterns = new HashMap<>();
		Integer maxSize = 0;
		Pattern maxPattern = new Pattern();
		Pattern pattern = new Pattern();
		
		flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(flagMappings, node.getOperationType().getOpTypeName().name());
		for (String flagName: flagNamesPerOptype){
			//if (!((node.getNodeID() == 946 || node.getNodeID() == 1092) && flagName.equals("replication"))){
				if (flagName.equals("mergeJoin")){
					pattern = JSONDictionaryParser.getAnyPatternElementByName(dictionaryFilePath, flagName);
					patternNodes.addAll(matchMergeJoin(node, G));
				}else {
				pattern = JSONDictionaryParser.getAnyPatternElementByName(dictionaryFilePath, flagName);
				System.out.println("Starting to discover pattern: "+flagName);
				patternNodes.retainAll(pattern.match(node, G, patternNodes));
				}
			//}
				if (patternNodes.size() != 0 && patternNodes.size() != G.getEtlFlowOperations().size()){
					System.out.println(pattern.getElementName()+" is present in the graph");
					matchedPatterns.put(pattern, new ArrayList<>(patternNodes));
				} else System.out.println(pattern.getElementName()+" pattern failed");
				patternNodes.clear();
				}
				if (matchedPatterns.size() != 0){
				for(Pattern matchedPattern: matchedPatterns.keySet()){
					if (matchedPatterns.get(matchedPattern).size() > maxSize) {
						maxSize = matchedPatterns.get(matchedPattern).size();
						maxPattern = matchedPattern;
					}
				}
				System.out.println("max pattern "+maxPattern.getElementName());
				maxPattern.setPatternSubgraph(matchedPatterns.get(maxPattern));
				return maxPattern;
				} else return new Pattern();
	}
	
	public static ArrayList<BPMNElement> translateToBPMN(ETLFlowGraph G, HashMap<String, ArrayList<String>> flagMappings, 
			String dictionaryFilePath){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Pattern pattern = new Pattern();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> graphBpmnElements = new ArrayList<>();
		ArrayList<ETLFlowOperation> visitedNodes = new ArrayList<>();
		ArrayList<BPMNElement> graphEdges = BPMNConstructsGenerator.getBPMNElementsEdge(G);
		ArrayList<Integer> graphSourceNodes = getOpeartionSourceNodes(G);
		System.out.println("Graph "+G);
		Integer mergeCounter = 1;
		Integer subprocessCounter = 1;
		Integer wsCounter = 1;
		Integer replicationCounter = 1;
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			System.out.println("----------------------------------");
			Integer v = graphIter.next();
				System.out.println("we are at node "+v);
			ETLFlowOperation node = ops.get(v);
			if (v == 946)
				System.out.println();
			if (!visitedNodes.contains(node)){
			pattern = getMaxSubgraphMatch(node, G, flagMappings, dictionaryFilePath);
			patternNodes = pattern.getPatternSubgraph();
			if (patternNodes.size() > 1){
				ETLFlowGraph subGraph = createSubGraph(G, patternNodes);
				ArrayList<BPMNElement> maxBpmn = toBPMN.BPMNConstructsGenerator.getPatternBPMNElements(G, pattern);
				ArrayList<BPMNElement> nestedBpmn = translateToBPMN(subGraph, flagMappings, dictionaryFilePath);
				if (maxBpmn.get(0).getElementName().equals(BPMNElementTagName.subProcess.name())){
					maxBpmn.get(0).setSubElements(nestedBpmn);
					graphEdges = BPMNConstructsGenerator.updateEdgesWhenInsertingSubprocesses(graphEdges, G, subGraph, pattern, maxBpmn.get(0));
					graphSourceNodes = getMainProcessSourceNodes(subGraph, graphSourceNodes);
					//----naming
					for (BPMNAttribute attr: maxBpmn.get(0).getAttributes()){
						if (attr.getAttributeName().equals("name")){
							switch (attr.getAttributeValue()){
							case "mergeJoin": 
								attr.setAttributeValue("Join"+"_"+mergeCounter);
								mergeCounter++;
								break;
							
							case "replication":
								attr.setAttributeValue("Replicated Flow"+"_"+replicationCounter);
								replicationCounter++;
								break;
								
							case "externalDataValidation":
								attr.setAttributeValue("Web Service Lookup"+"_"+wsCounter);
								wsCounter++;
								break;
								
							case "subprocess":
								attr.setAttributeValue("Subprocess"+"_"+subprocessCounter);
								subprocessCounter++;
								break;
							
							}
						}
					}
					///---naming end
					graphBpmnElements.addAll(maxBpmn);
					
				} else {
					graphBpmnElements.addAll(maxBpmn);
					graphBpmnElements.addAll(nestedBpmn);
				}
				for (ETLFlowOperation op: patternNodes){
					visitedNodes.add(op);
				}
			}
			else {
				for (BPMNElement el: BPMNConstructsGenerator.getPatternBPMNElements(G, pattern)){
					if (el.getElementName().equals(BPMNElementTagName.sequenceFlow.name())) 
						graphEdges.add(el);
					else 
						graphBpmnElements.add(el);
				}
			}
			System.out.println("bpmn element added to graph elements");
			}
			System.out.println("----------------------------------");
		}
		//add graph edges to graph elements
		graphBpmnElements.addAll(graphEdges);
		System.out.println("source nodes "+ graphSourceNodes);
		return graphBpmnElements;
	}
	
	public static ArrayList<Integer> getOpeartionSourceNodes(ETLFlowGraph G){
		ArrayList<Integer> graphSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> sourceOperations = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		
		for (Integer i : graphSourceNodes) {
			//if the node is a datastore connect start event to their target unless it is a join
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				for (Object e : G.edgeSet()) {
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.Join)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.LeftOuterJoin)) {
						sourceOperations.add(targetId);
					}
				}
			} else {
				sourceOperations.add(i);
			}
		}
		return sourceOperations;
		
	}
	
	public static ArrayList<Integer> getMainProcessSourceNodes(ETLFlowGraph subGraph, ArrayList<Integer> graphSourceNodes){
		ArrayList<Integer> subGraphSourceNodes = subGraph.getAllSourceNodes();
		Integer counter = 0;
		boolean result = false;
	
		for (Integer i: subGraphSourceNodes){
			if (!graphSourceNodes.contains(i)) {
				result = true;
				break;
			}
		}
		if (result == true){
			graphSourceNodes.removeAll(subGraphSourceNodes);
		}
		
		return graphSourceNodes;
	}
	
	/*public static ArrayList<Pattern> getAllMatchedPatterns(ETLFlowGraph G){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		ArrayList<Pattern> matchedPatterns = new ArrayList<>();
		HashMap<String, ArrayList<String>> flagMappings = JSONDictionaryParser.parsePatternFlags(Demo.patternFlagMappingPath);
		
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			System.out.println("------------------------------");
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			System.out.println("start node "+node.getOperationName()+" node id "+ node.getNodeID());
			flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(flagMappings, node.getOperationType().getOpTypeName().name());
			for (String flagName: flagNamesPerOptype){
					System.out.println(flagName);
					Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(Demo.dictionaryFilePath, flagName);
					patternNodes.addAll(pattern.match(node, G, patternNodes));
					if (patternNodes.size() != 0){
						System.out.println(pattern.getElementName()+" pattern is present in the graph");
						pattern.setPatternSubgraph(new ArrayList<>(patternNodes));
						matchedPatterns.add(pattern);
					}
					patternNodes.clear();
			}
			}
		return matchedPatterns;
	}*/
}
