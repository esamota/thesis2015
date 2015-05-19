import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

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
				.getNodePatternFlags();
		// checkPatternExistence(ops, G, flagMapping);
		// checkGraphForPatterns(G, ops, flagMapping);
		//HashMap<Integer, ArrayList<Pattern>> patternLinks = getAllLinksToPatterns(G, ops);
		//System.out.println("patternLinks "+patternLinks);
		//System.out.println(patternLinks);
		/*ArrayList<String> existingPatterns = new ArrayList<String>();
		for (Integer key: ops.keySet()){
			existingPatterns = checkPatternSteps(ops, G, ops.get(key), "sortMerge"); 
		}*/
		/*for (Integer key: ops.keySet()){
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodes(G, ops, ops.get(key));
		for (ETLFlowOperation op: targetNodes){
		System.out.println(ops.get(key).getNodeID()+": "+op.getNodeID());
		}
		}*/
		
		//ETLFlowGraph g1 = applyPatterns(G, ops);
		//System.out.println(g1);
		
		}
		

	/*public static ArrayList<Pattern> createPatternObjects() {
		ArrayList<String> patternNames = JSONDictionaryParser.getPatternNames();
		ArrayList<Pattern> dictionaryPatterns = new ArrayList<Pattern>();
		int counter = 1;
		String patternID= "0"+counter;
		for (String name : patternNames) {
			Pattern pattern = new Pattern(name);
			if (!dictionaryPatterns.contains(pattern)) {
				dictionaryPatterns.add(pattern);
			}
			counter = counter+1;
		}
		return dictionaryPatterns;
	}*/

	// can obtain a list of pattern names that belong to a node
	public static ArrayList<String> getNodeFlags(OperationTypeName optypeName) {
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser
				.getNodePatternFlags();
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
	
	/*public static ETLFlowGraph applyPatterns(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<Pattern> patternLinksPerNode = new ArrayList<Pattern>();
		// all existing patterns with a list of operations that belong to them
		ETLFlowOperation lastPatternOperation = new ETLFlowOperation();
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			patternLinksPerNode = getLinksToPatternsForNode(G, ops,
					node.getNodeID());
			if (patternLinksPerNode.size() != 0) {
				// in the future, check is there are overlappings here
				for (Pattern linkedPattern : patternLinksPerNode) {
					
					for (ETLFlowOperation op : linkedPattern.getPatternNodes()) {
						System.out.println("op to be deleted " + op.getNodeID());
						G.removeVertex(op);
						lastPatternOperation = op;
					}
					G.addVertex(linkedPattern);
					
					ArrayList<ETLFlowOperation> sources = getSourceNodesGivenTarget(
							G, ops, node);
					for (ETLFlowOperation sourceOp : sources) {
						G.addEdge(sourceOp, linkedPattern);
					}
					ArrayList<ETLFlowOperation> targets = getTargetNodes(G,
							ops, lastPatternOperation);
					for (ETLFlowOperation targetOp : targets) {
						G.addEdge(linkedPattern, targetOp);
					}
				}
			}

		}
		return G;
	}*/
		
	public static ArrayList<Pattern> getLinksToPatternsForNode(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops, Integer nodeID){
		HashMap<Integer, ArrayList<Pattern>> patternLinks = getLinksToPatterns(G, ops);
		ArrayList<Pattern> patternLinksPerNode= new ArrayList<Pattern>();
		for (Integer key: patternLinks.keySet()){
			if (key == nodeID){
			for(Pattern pattern: patternLinks.get(key)){
				patternLinksPerNode.add(pattern);
			}
			}
		}
		return patternLinksPerNode;
	}
	
	public static void testTarget(ETLFlowGraph G,Hashtable<Integer, ETLFlowOperation> ops ){
		
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			ArrayList<ETLFlowOperation> myTargets = getTargetNodes(G, ops, node);
			for (ETLFlowOperation op: myTargets){
			System.out.println("Source "+node.getNodeID()+", MY TARGET: "+ op.getNodeID());
			}
			ArrayList<Integer> graphTargets = G.getAllTargetNodes();
			for (Integer i: graphTargets){
				System.out.println("Source "+node.getNodeID()+", GRAPH TARGET "+i);
			}
		}
	}

	public static HashMap<Integer, ArrayList<Pattern>> getLinksToPatterns(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<String> flagNames = new ArrayList<String>();
		// In the HashMap: vector id, and an array of patterns that it points to
		HashMap<Integer, ArrayList<Pattern>> patternLinks = new HashMap<Integer, ArrayList<Pattern>>();
		ArrayList<Pattern> nodePatterns = new ArrayList<Pattern>();
		HashMap<Pattern, ArrayList<ETLFlowOperation>> patternNodes = new HashMap<Pattern, ArrayList<ETLFlowOperation>>();
		int counter = 1;
		// start to iterate the graph in topological order
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			// get pattern name flags from dictionary
			flagNames = getNodeFlags(node.getOperationType().getOpTypeName());
			// if no flag names, no need to check if pattern exists
			if (flagNames.size() == 0) {
				patternLinks.put(node.getNodeID(), nodePatterns);
			} else if (flagNames.size() >= 1) {
				// for each pattern that this node could start, check if it
				// exists and obtain a list of nodes inside an existing pattern
				for (String flagName : flagNames) {
					patternNodes = getNodesOfExistingPatterns(ops, G, node,
							flagName);
					for (Pattern patternObj : patternNodes.keySet()) {
						for (ETLFlowOperation op : patternNodes.get(patternObj)) {
							nodePatterns.add(patternObj);
						}
					}
					patternLinks.put(node.getNodeID(), new ArrayList<Pattern>(
							nodePatterns));
					nodePatterns.clear();
				}
			} else {
				patternLinks.put(node.getNodeID(), nodePatterns);
			}
		}
		//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		return patternLinks;
	}

	public static ArrayList<ETLFlowOperation> getTargetNodes(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops,
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
	
	public static ArrayList<ETLFlowOperation> getSourceNodesGivenTarget (ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops,
			ETLFlowOperation targetNode){
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
	public static HashMap<Pattern, ArrayList<ETLFlowOperation>> getNodesOfExistingPatterns(
			Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			ETLFlowOperation flagNode, String flagName) {
		ArrayList<HashMap<String, String>> step1Operations = JSONDictionaryParser
				.parseJSONSingleFlowPatternStep1(flagName);
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodes(G, ops, flagNode);
		HashMap<Pattern, ArrayList<ETLFlowOperation>> patternNodes = new HashMap<Pattern, ArrayList<ETLFlowOperation>>();
		ArrayList<ETLFlowOperation> nodesPerPattern = new ArrayList<ETLFlowOperation>();
		ArrayList<String> patternNames = JSONDictionaryParser.getPatternNames();
		// save ids of nodes that are consumed by a pattern
		boolean pattern = false;
		int counter =0;
		if (patternNames.contains(flagName)) {
			for (ETLFlowOperation opT : targetNodes) {
				//******System.out.println("source node: "+flagNode.getNodeID()+", target node: "+opT.getNodeID());
			//in the HashMap: string1 =name (ex.implementationType), string2=value(ex.MERGE)
			for (HashMap<String, String> map : step1Operations) {
				for (String str : map.keySet()) {
					// doesn't take into account other dictionary structures
						if ((str.equals("implementationType")
								&& opT.getImplementationType().equals(map.get(str))) || (str.equals("type")
								&& opT.getOperationType().getOpTypeName()
								.equals(map.get(str)))) {
							pattern = true;
							nodesPerPattern.add(flagNode);
							nodesPerPattern.add(opT);
						} else
							pattern = false;
					}
				}
			Pattern patternObj = new Pattern(flagName);
			patternObj.addPatternNodes(nodesPerPattern);
			//for testing------------------------------------------------------------------------------
			/*System.out.println(patternObj.getPatternName()+", patternID: "+patternObj.getPatternID());
			for (ETLFlowOperation oper :patternObj.getPatternNodes()){
				System.out.println(oper.getNodeID());
			}
			if(patternObj.getPatternNodes().size() == 0){
				System.out.println("pattern does not exist in G");
			}*/
			//---------------------------------------------------------------------------------------
			patternNodes.put(patternObj, new ArrayList<ETLFlowOperation> (nodesPerPattern));
			nodesPerPattern.clear();
			}
		}
		//System.out.println("======================================");
		return patternNodes;	
	}

}
