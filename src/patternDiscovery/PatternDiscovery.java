package patternDiscovery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
		ETLFlowGraph G = XLMParser.getXLMGraph();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser
				.getOperatorPatternFlags();

		/*
		 * for (Integer i: ops.keySet()){ ArrayList<Pattern> nodePatterns =
		 * getLinksToPatternsForNode(G, ops, ops.get(i).getNodeID()); for
		 * (Pattern pattern: nodePatterns){
		 * System.out.println(ops.get(i).getNodeID
		 * ()+" "+pattern.getPatternName()); } }
		 */

		/*
		 * HashMap<Integer, ArrayList<Pattern>> patternLinks =
		 * getLinksToPatterns(G, ops); for (Integer i: patternLinks.keySet()){
		 * for (Pattern pattern: patternLinks.get(i)){
		 * System.out.println(i+" "+pattern.getPatternName()); } }
		 */

		for (Integer i : ops.keySet()) {
			ArrayList<String> flagNames = getNodeFlags(ops.get(i)
					.getOperationType().getOpTypeName());
			if (flagNames.size() == 0) {
				System.out.println(ops.get(i).getNodeID() + " no pattern flag");
			} else if (flagNames.size() > 0) {
				for (String flagName : flagNames) {
					System.out.println("--------------");
					ArrayList<ETLFlowOperation> patternNodes = getNodesOfExistingPatterns(
							ops, G, ops.get(i), flagName);
					for (ETLFlowOperation op : patternNodes) {

						System.out.println(flagName + ", " + op.getNodeID()
								+ ", " + op.getOperationName());

					}
					System.out.println("--------------");
				}
			}
		}
	}

	// can obtain a list of pattern names that belong to a node
	public static ArrayList<String> getNodeFlags(OperationTypeName optypeName, HashMap<String, ArrayList<String>> flagMapping) {
		ArrayList<String> nodeFlagNames = new ArrayList<String>();
		for (String opName : flagMapping.keySet()) {
			if (opName.equals(optypeName.name())) {
				for (String flagName : flagMapping.get(opName)) {
					if (!flagName.equals("")) {
						nodeFlagNames.add(flagName);
					}
				}
			}
		}
		return nodeFlagNames;
	}
	
	public static void getPotentialMaxMatch(ETLFlowOperation node, HashMap<String, ArrayList<String>> flagMapping){
		ArrayList<String> nodeFlagNames = new ArrayList<String>();
		String maxPatternName = "";
		for (String opName : flagMapping.keySet()) {
			if (opName.equals(node.getOperationType().getOpTypeName().name())) {
				for (String flagName : flagMapping.get(opName)) {
					if (flagMapping.get(opName).size() != 0) {
						nodeFlagNames.add(flagName);
					}
				}
			}
		}
		
		if (nodeFlagNames.size() == 1) maxPatternName = nodeFlagNames.get(0);
		else 
		
	}
	
	
	public static ArrayList<Pattern> getLinksToPatternsForNode(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops, Integer nodeID) {
		HashMap<Integer, ArrayList<Pattern>> patternLinks = getLinksToPatterns(
				G, ops);
		ArrayList<Pattern> patternLinksPerNode = new ArrayList<Pattern>();
		for (Integer key : patternLinks.keySet()) {
			if (key.intValue() == nodeID.intValue()) {
				for (Pattern pattern : patternLinks.get(key)) {
					patternLinksPerNode.add(pattern);
				}
			}
		}
		return patternLinksPerNode;
	}

	public static void getPotentialMaxMatch () {
		
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
		}
	}
	

	public static ArrayList<ETLFlowOperation> getTargetNodesGivenSource(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			ETLFlowOperation sourceNode) {
		ArrayList<ETLFlowOperation> targetNodes = new ArrayList<ETLFlowOperation>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			if (sourceNode == opS) {
				targetNodes.add(opT);
			}

		}
		return targetNodes;
	}

	public static ArrayList<ETLFlowOperation> getSourceNodesGivenTarget(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			ETLFlowOperation targetNode) {
		ArrayList<ETLFlowOperation> sourceNodes = new ArrayList<ETLFlowOperation>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			if (targetNode == opT) {
				sourceNodes.add(opS);
			}

		}
		return sourceNodes;
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
	
	public static boolean checkSimplePattern(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			ETLFlowOperation opT, String flagName, HashMap<String, ArrayList<String>> stepOperations){
		boolean pattern = false;
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		if ((stepOperations
				.containsKey("implementationType") && stepOperations
				.get("implementationType").contains(
						opT.getImplementationType()
								.toUpperCase()))
				|| (stepOperations.containsKey("type") && stepOperations
						.get("type").contains(
								opT.getNodeKind().name()))
				|| (stepOperations.containsKey("optype") && stepOperations
						.get("optype").contains(
								opT.getOperationType()
										.getOpTypeName()))) {
				pattern = true;
		}
		return pattern;
	}

	public static HashMap<ETLFlowOperation, ArrayList<ETLFlowOperation>> checkTargetTypeInWhiteList(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			ETLFlowOperation opT,
			HashMap<String, ArrayList<String>> whiteList) {
		System.out.println("in checkTargetInWhiteList");
		String nodeValue = "";
		String key = "";
		ArrayList<ETLFlowOperation> nodes = new ArrayList<>();
		HashMap <ETLFlowOperation, ArrayList<ETLFlowOperation>> lastIndexPlusPatternNodes = new HashMap<>();
			ArrayList<ETLFlowOperation> targetNodes = getTargetNodesGivenSource(
					G, ops, opT);
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
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodesGivenSource(
					G, ops, opT);
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
