package patternDiscovery;
import utilities.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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

getGraphPatterns(G);
		//ArrayList<ETLFlowOperation> maxMatchingPatternSubgraph = getMaxSubgraphMatch(node, G);
		//System.out.println(maxMatchingPatternSubgraph.size());
	}
	
	
	public static ArrayList<ETLFlowOperation> getMaxSubgraphMatch (ETLFlowOperation node, ETLFlowGraph G) {
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		HashMap<Pattern, ArrayList<ETLFlowOperation>> matchedPatterns = new HashMap<>();
		Integer maxSize = 0;
		Pattern maxPattern = new Pattern();
		
		flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(node.getOperationType().getOpTypeName());
		if (flagNamesPerOptype.size() >= 1){
			for (String flagName: flagNamesPerOptype){
				Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(flagName);
				patternNodes.addAll(pattern.match(node, G, patternNodes));
				if (patternNodes.size() != 0){
					System.out.println(pattern.getElementName()+" is present in the graph");
					matchedPatterns.put(pattern, new ArrayList<>(patternNodes));
				}
				patternNodes.clear();
			}
		}
			if (matchedPatterns.size() > 1){
				for(Pattern pattern: matchedPatterns.keySet()){
					if (matchedPatterns.get(pattern).size() > maxSize) {
						maxSize = matchedPatterns.get(pattern).size();
						maxPattern = pattern;
					}
				}
				return matchedPatterns.get(maxPattern);
			}
			else return patternNodes;	
	
	}
	
	public static void getGraphPatterns(ETLFlowGraph G){
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<String> flagNamesPerOptype = new ArrayList<>();
		
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			flagNamesPerOptype = JSONDictionaryParser.getPatternNamesByOriginOperation(node.getOperationType().getOpTypeName());
			if (flagNamesPerOptype.size() >= 1){
				for (String flagName: flagNamesPerOptype){
					Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(flagName);
					patternNodes.addAll(pattern.match(node, G, patternNodes));
					if (patternNodes.size() != 0){
						System.out.println(pattern.getElementName()+" is present in the graph");
					}
					patternNodes.clear();
				}
			}
		}
	}
	
	// transforming old to new, calls jsonDictionary to check if a pattern
	// exists
	/*public static ArrayList<ETLFlowOperation> getNodesOfExistingPatterns(
			Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			ETLFlowOperation flagNode, String flagName) {
		HashMap<String, ArrayList<String>> stepOperations = new HashMap<>();
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodesGivenSource(G,
				ops, flagNode);
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<ETLFlowOperation>();
		HashMap<String, ArrayList<String>> whiteList = JSONDictionaryParser
				.getWhiteListItems(flagName);
		HashMap<String, ArrayList<String>> blackList = JSONDictionaryParser
				.getBlackListItems(flagName);
		boolean pattern = true;
		
		Integer numOfSteps = utilities.JSONDictionaryParser.getNumOfStepsInFirstSequence(flagName);
		if (numOfSteps > 1) {
			
		for (ETLFlowOperation opT : targetNodes) {			
		for (int s = 0; s < numOfSteps; s++) {
			stepOperations = JSONDictionaryParser.parseJSONPatternSteps(flagName, v, f, s);
							//*******************************************************************
							//for simple patterns w/out *, #,  wList, bList or two origins
							if (stepOperations.containsKey("implementationType") || stepOperations
									.containsKey("type") || stepOperations
									.containsKey("optype")){ //add condition for multiple origins later
								if (s > 0) pattern = checkSimplePattern(ops, G, opT, flagName, stepOperations);
								else pattern = checkSimplePattern(ops, G, opT, flagName, stepOperations);
								if (pattern == true) {
									//if (opTadded = false)
									patternNodes.add(opT);
								} else {pattern = false; break;}
							//**********************************************************************
						    // for pattern steps with * and whiteList
							} else if (stepOperations.containsKey("*")
									&& stepOperations.get("*").contains(
											"whiteList")) {
								System.out.println("potential recovery and subprocess");
								if ((whiteList.containsKey("implementationType") && whiteList
										.get("implementationType").contains(
												opT.getImplementationType()
														.toUpperCase()))
										|| (whiteList.containsKey("type") && whiteList
												.get("type").contains(
														opT.getNodeKind().name()))
										|| (whiteList.containsKey("optype") && whiteList
												.get("optype").contains(
														opT.getOperationType()
																.getOpTypeName()))) {
									pattern = true;
									patternNodes.add(opT);
									for (ETLFlowOperation lastIndex: checkTargetTypeInWhiteList(G, ops, opT, whiteList).keySet()){
										patternNodes.addAll(checkTargetTypeInWhiteList(G, ops, opT, whiteList).get(lastIndex));
										opT = lastIndex;
									}
									
								} else {pattern = false; break;}
							//**************************************************************************
							// double star steps
							} else if (stepOperations.containsKey("*")
									&& stepOperations.get("*").contains("*")) {
								if (blackList.size() == 0 || (blackList.containsKey("implementationType") && !blackList
										.get("implementationType").contains(
												opT.getImplementationType()
														.toUpperCase()))
										|| (blackList.containsKey("type") && !blackList
												.get("type").contains(
														opT.getNodeKind().name()))
										|| (blackList.containsKey("optype") && !blackList
												.get("optype").contains(
														opT.getOperationType()
																.getOpTypeName()))){
									pattern = true;
									patternNodes.add(opT);
									patternNodes.addAll(getDoubleStarPatternNodes(ops, G, opT, flagName, stepOperations, blackList));
									
								} else {pattern = false; break; }
							//*****************************************************************************
							// with n number of nodes from a list
							} else if (stepOperations.containsKey("#1")
									&& stepOperations.get("#1").contains(
											"whiteList")) {
								System.out.println("potential compensation");
							} else { pattern = false; break; }
						}
					}

		if (pattern == true)
			return patternNodes;
		else
			return new ArrayList<ETLFlowOperation>();
	}*/
	
	public static HashMap<ETLFlowOperation, ArrayList<ETLFlowOperation>> checkTargetTypeInWhiteList(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			ETLFlowOperation opT,
			HashMap<String, ArrayList<String>> whiteList) {
		System.out.println("in checkTargetInWhiteList");
		String nodeValue = "";
		String key = "";
		ArrayList<ETLFlowOperation> nodes = new ArrayList<>();
		HashMap <ETLFlowOperation, ArrayList<ETLFlowOperation>> lastIndexPlusPatternNodes = new HashMap<>();
			ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(opT, G);
			for (ETLFlowOperation op : targetNodes) {
				if (whiteList.containsKey("optype")) {
					nodeValue = op.getOperationType().getOpTypeName().name();
					key = "optype";
				} else if (whiteList.containsKey("implementationType")) {
					nodeValue = op.getImplementationType();
					key = "implementationType";
				} else if (whiteList.containsKey("type")) {
					nodeValue = op.getNodeKind().name();
					key = "type";
				}
				if (whiteList.get(key).contains(nodeValue)) {
					nodes.add(op);
					// to get the last node in the list :
					// nodes.get(nodes.size()- 1);
					for (ETLFlowOperation OPS: checkTargetTypeInWhiteList(G, ops, op,
							whiteList).keySet()){
						nodes.addAll(checkTargetTypeInWhiteList(G, ops, op,
								whiteList).get(OPS));
					}
					
				} else
					lastIndexPlusPatternNodes.put(op, new ArrayList<> (nodes));
			}
		return lastIndexPlusPatternNodes;

	}
	
	public static ArrayList<ETLFlowOperation> getDoubleStarPatternNodes(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
		ETLFlowOperation opT, String flagName, HashMap<String, ArrayList<String>> stepOperations,
		HashMap<String, ArrayList<String>> blackList){
		
	String nodeValue = "";
	String key = "";
		ArrayList<ETLFlowOperation> nodes = new ArrayList<>();
		ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(opT, G);
			for (ETLFlowOperation op : targetNodes) {
				if (blackList.size() == 0 || (blackList.containsKey("implementationType") && !blackList
						.get("implementationType").contains(
								op.getImplementationType()
										.toUpperCase()))
						|| (blackList.containsKey("type") && !blackList
								.get("type").contains(
										op.getNodeKind().name()))
						|| (blackList.containsKey("optype") && !blackList
								.get("optype").contains(
										op.getOperationType()
												.getOpTypeName()))){
					nodes.add(op);
					nodes.addAll(getDoubleStarPatternNodes(ops, G, op, flagName, stepOperations, blackList));
				}
			
		}
			return nodes;
		}
	

}
