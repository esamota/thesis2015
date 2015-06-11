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

public class PatternSearch extends DirectedAcyclicGraph {

	public PatternSearch(Class arg0) {
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
	public static ArrayList<String> getNodeFlags(OperationTypeName optypeName) {
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser
				.getOperatorPatternFlags();
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

	public static ArrayList<PatternElement> getLinksToPatternsForNode(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops, Integer nodeID) {
		HashMap<Integer, ArrayList<PatternElement>> patternLinks = getLinksToPatterns(
				G, ops);
		ArrayList<PatternElement> patternLinksPerNode = new ArrayList<PatternElement>();
		for (Integer key : patternLinks.keySet()) {
			if (key.intValue() == nodeID.intValue()) {
				for (PatternElement pattern : patternLinks.get(key)) {
					patternLinksPerNode.add(pattern);
				}
			}
		}
		return patternLinksPerNode;
	}

	// returns each node with a list of links to pattern objects that it belongs
	// to
	public static HashMap<Integer, ArrayList<PatternElement>> getLinksToPatterns(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<String> flagNames = new ArrayList<String>();
		// In the HashMap: vector id, and an array of patterns that it points to
		HashMap<Integer, ArrayList<PatternElement>> patternLinks = new HashMap<Integer, ArrayList<PatternElement>>();
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<ETLFlowOperation>();
		// HashMap<Pattern, ArrayList<ETLFlowOperation>> patternNodes = new
		// HashMap<Pattern, ArrayList<ETLFlowOperation>>();
		int counter = 1;
		// start to iterate the graph in topological order
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			// get pattern name flags from dictionary
			flagNames = getNodeFlags(node.getOperationType().getOpTypeName());
			// if no flag names, no need to check if pattern exists
			 if (flagNames.size() > 0) {
				for (String flagName : flagNames) {
					// patterns that start with this flag name and a list of all
					// operations inside them
					patternNodes = getNodesOfExistingPatterns(ops, G, node,
							flagName);
					if (patternNodes.size() != 0) {
						PatternElement patternObj = new PatternElement(flagName);

						for (ETLFlowOperation op : patternNodes) {
							patternObj.addPatternNode(op);
							if (patternLinks.get(op.getNodeID()) == null) {
								patternLinks.put(op.getNodeID(),
										new ArrayList<PatternElement>());
								patternLinks.get(op.getNodeID())
										.add(patternObj);
							} else
								patternLinks.get(op.getNodeID())
										.add(patternObj);
						}
					}
				}
			} else patternLinks.put(node.getNodeID(), new ArrayList<PatternElement>());
		}
		// System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		return patternLinks;
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
	public static ArrayList<ETLFlowOperation> getNodesOfExistingPatterns(
			Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			ETLFlowOperation flagNode, String flagName) {
		HashMap<String, ArrayList<String>> stepOperations = new HashMap<>();
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodesGivenSource(G,
				ops, flagNode);
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<ETLFlowOperation>();
		ArrayList<String> patternNames = JSONDictionaryParser.getPatternNames();
		HashMap<String, ArrayList<String>> whiteList = JSONDictionaryParser
				.getWhiteListItems(flagName);
		HashMap<String, ArrayList<String>> blackList = JSONDictionaryParser
				.getBlackListItems(flagName);
		Integer numOfVersions = 0;
		Integer numOfFlows = 0;
		Integer numOfSteps = 0;
		boolean pattern = true;
		if (patternNames.contains(flagName)) {
			patternNodes.add(flagNode);
			for (ETLFlowOperation opT : targetNodes) {

				numOfVersions = 0;
				System.out.println("new version^^^^^^^^^^^^^^^^^^^");
				for (int v = 0; v < numOfVersions; v++) {

					numOfFlows = 0;
					System.out.println("new flow------------------------------");
					for (int f = 0; f < numOfFlows; f++) {

						if (pattern == false) break;
						numOfSteps = 0;
						System.out.println("new step************************");
						
						for (int s = 0; s < numOfSteps; s++) {
							stepOperations = JSONDictionaryParser
									.parseJSONPatternSteps(flagName, v, f, s);
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
				}
			}
		}

		if (pattern == true)
			return patternNodes;
		else
			return new ArrayList<ETLFlowOperation>();
	}
	
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
