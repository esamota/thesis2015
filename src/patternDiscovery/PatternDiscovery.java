package patternDiscovery;
import toBPMN.BPMNElement;
import utilities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.Utilities;

import org.apache.xerces.parsers.XMLParser;
import org.jgraph.graph.Edge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import operationDictionary.OperationTypeName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;

public class PatternDiscovery extends DirectedAcyclicGraph {

	public PatternDiscovery(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ETLFlowGraph G = utilities.XLMParser.getXLMGraph();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		 
		//System.out.println(utilities.XLMParser.getTargetOperationsGivenSource(ops.get(895), G).size());
		//System.out.println(G.getEtlFlowOperations().size());
		//getAllGraphPatterns(G);
		Pattern maxMatchedPattern = getMaxSubgraphMatch(ops.get(921), G, "default");
		ArrayList<ETLFlowOperation> patternNodes = maxMatchedPattern.getPatternSubgraph();
		System.out.println("Max Pattern "+ maxMatchedPattern.getElementName());
		for (ETLFlowOperation op: patternNodes) {
			System.out.println("---------------------> "+op.getOperationName());
		}
		
		
		//createSubGraphByCloningGraph(G, patternNodes);
	//ArrayList<ETLEdge> edges = getEdgesForSubGraph(G, patternNodes);
	//System.out.println("EDGES: "+edges);
	
	//ETLFlowGraph subGraph = createSubGraph(G, patternNodes);
	//System.out.println("SUBGRAPH: "+subGraph);
		//ArrayList<ETLFlowOperation> maxMatchingPatternSubgraph = getMaxSubgraphMatch(node, G);
		//System.out.println(maxMatchingPatternSubgraph.size());
		
		/*ArrayList<BPMNElement> graphElements = translateToBPMN(G);
		for (BPMNElement element: graphElements){
			System.out.println(element.getElementName());
			for (BPMNElement BPMN: element.getSubElements())
			System.out.println("subElement "+BPMN.getElementName());
		}*/
		
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
	
	public static ETLFlowGraph createSubGraphByCloningGraph(ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ETLFlowGraph subGraph = new ETLFlowGraph();
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			if (patternNodes.contains(node)){
				subGraph.addVertex(v);
				subGraph.addEtlFlowOperations(v, node);
			}
		}
		return subGraph;
	}
	
	public static Pattern getMaxSubgraphMatch (ETLFlowOperation node, ETLFlowGraph G, String bigPatternName) {
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		HashMap<Pattern, ArrayList<ETLFlowOperation>> matchedPatterns = new HashMap<>();
		Integer maxSize = 0;
		Pattern maxPattern = new Pattern();
		Pattern pattern = new Pattern();
		
		flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(node.getOperationType().getOpTypeName());
		System.out.println("flag names per optype "+flagNamesPerOptype);
			for (String flagName: flagNamesPerOptype){
				if (!bigPatternName.equals("subprocess")){
				System.out.println("flagName "+flagName+". BigPattern "+bigPatternName);
				pattern = JSONDictionaryParser.getAnyPatternElementByName(flagName);
				System.out.println("Starting to discover pattern: "+flagName);
				patternNodes.addAll(pattern.match(node, G, patternNodes));
				}
				if (patternNodes.size() != 0 && patternNodes.size() != G.getEtlFlowOperations().size()){
					System.out.println(pattern.getElementName()+" is present in the graph");
					matchedPatterns.put(pattern, new ArrayList<>(patternNodes));
				}
				patternNodes.clear();
			}
				for(Pattern matchedPattern: matchedPatterns.keySet()){
					if (matchedPatterns.get(matchedPattern).size() > maxSize) {
						maxSize = matchedPatterns.get(matchedPattern).size();
						maxPattern = matchedPattern;
					}
				}
				System.out.println("max pattern "+maxPattern.getElementName());
				maxPattern.setPatternSubgraph(matchedPatterns.get(maxPattern));
				return maxPattern;
			
	}
	
	public static ArrayList<BPMNElement> translateToBPMN(ETLFlowGraph G){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Pattern pattern = new Pattern();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> graphBpmnElements = new ArrayList<>();
		ArrayList<ETLFlowOperation> visitedNodes = new ArrayList<>();
		String bigPatternName = "";
		System.out.println("Graph "+G);
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			System.out.println("----------------------------------");
			Integer v = graphIter.next();
				System.out.println("we are at node "+v);
			ETLFlowOperation node = ops.get(v);
			if (!visitedNodes.contains(node)){
			pattern = getMaxSubgraphMatch(node, G, bigPatternName);
			patternNodes = pattern.getPatternSubgraph();
			if (patternNodes.size() > 1){
				bigPatternName = pattern.getElementName();
					System.out.println("pattern size > 1");
				ETLFlowGraph subGraph = createSubGraph(G, patternNodes);
					System.out.println("Max subGraph "+subGraph);
				ArrayList<BPMNElement> MaxBpmn = pattern.getBpmnElements();
				ArrayList<BPMNElement> nestedBpmn = translateToBPMN(subGraph);
					System.out.println("go into recursion");
				if (MaxBpmn.size() != 0){
					MaxBpmn.get(0).addSubElements(nestedBpmn);
					graphBpmnElements.addAll(MaxBpmn);
				} else graphBpmnElements.addAll(nestedBpmn);
				for (ETLFlowOperation op: patternNodes){
					visitedNodes.add(op);
				}
			}
			else graphBpmnElements.addAll(pattern.getBpmnElements());	
			System.out.println("bpmn element added to graph elements");
			}
			System.out.println("----------------------------------");
		}
		return graphBpmnElements;
	}
	
	public static void getAllGraphPatterns(ETLFlowGraph G){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		HashSet<String> optypes = JSONDictionaryParser.getOperationTypeEnums();
		
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			System.out.println("------------------------------");
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			System.out.println("start node "+node.getOperationName()+" node id "+ node.getNodeID());
			flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(node.getOperationType().getOpTypeName());
			for (String flagName: flagNamesPerOptype){
					System.out.println(flagName);
					Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(flagName);
					patternNodes.addAll(pattern.match(node, G, patternNodes));
					if (patternNodes.size() != 0){
						System.out.println(pattern.getElementName()+" pattern is present in the graph");
						System.out.println(patternNodes.size());
					}
					patternNodes.clear();
			}
			}
	}
}
