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
		
		for (Integer i: ops.keySet()){
		ArrayList<Pattern> nodePatterns = getLinksToPatternsForNode(G, ops, ops.get(i).getNodeID());
		for (Pattern pattern: nodePatterns){
			System.out.println(ops.get(i).getNodeID()+" "+pattern.getPatternName());
		}
		}
		
		/*HashMap<Integer, ArrayList<Pattern>> patternLinks = getLinksToPatterns(G, ops);
		for (Integer i: patternLinks.keySet()){
			for (Pattern pattern: patternLinks.get(i)){
				System.out.println(i+" "+pattern.getPatternName());
			}
		}*/
		
		/*for (Integer i: ops.keySet()){
			ArrayList<String> flagNames = getNodeFlags(ops.get(i).getOperationType().getOpTypeName());
			if (flagNames.size() == 0){
				System.out.println(ops.get(i).getNodeID()+" no pattern flag");
			} else if(flagNames.size() > 0){
			for (String flagName: flagNames){
				System.out.println("--------------");
				HashMap<Pattern, ArrayList<ETLFlowOperation>> patternNodes = getNodesOfExistingPatterns(ops, G, ops.get(i), flagName);
				for (Pattern pattern: patternNodes.keySet()){
					for (ETLFlowOperation op: patternNodes.get(pattern)){
						System.out.println(pattern.getPatternName()+": "+op.getNodeID());
					}
				}
				System.out.println("--------------");
			}
			}
		}*/
		}
		
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
		
	public static ArrayList<Pattern> getLinksToPatternsForNode(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops, Integer nodeID){
		HashMap<Integer, ArrayList<Pattern>> patternLinks = getLinksToPatterns(G, ops);
		ArrayList<Pattern> patternLinksPerNode= new ArrayList<Pattern>();
		for (Integer key: patternLinks.keySet()){
			if (key.intValue() == nodeID.intValue()){
			for(Pattern pattern: patternLinks.get(key)){
				patternLinksPerNode.add(pattern);
			}
			}
		}
		return patternLinksPerNode;
	}
	
	//returns each node with a list of links to pattern objects that it belongs to 
	public static HashMap<Integer, ArrayList<Pattern>> getLinksToPatterns(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<String> flagNames = new ArrayList<String>();
		// In the HashMap: vector id, and an array of patterns that it points to
		HashMap<Integer, ArrayList<Pattern>> patternLinks = new HashMap<Integer, ArrayList<Pattern>>();
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<ETLFlowOperation>();
		//HashMap<Pattern, ArrayList<ETLFlowOperation>> patternNodes = new HashMap<Pattern, ArrayList<ETLFlowOperation>>();
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
				patternLinks.put(node.getNodeID(), new ArrayList<Pattern>());
			} else if (flagNames.size() > 0) {
				// for each pattern that this node could start, check if it
				// exists and obtain a list of nodes inside an existing pattern
				for (String flagName : flagNames) {
					//patterns that start with this flag name and a list of all operations inside them 
					patternNodes = getNodesOfExistingPatterns(ops, G, node, flagName);
					if (patternNodes.size() != 0) {
						Pattern patternObj = new Pattern(flagName);

						for (ETLFlowOperation op : patternNodes) {
							patternObj.addPatternNode(op);
							if (patternLinks.get(op.getNodeID()) == null) {
								patternLinks.put(op.getNodeID(),
										new ArrayList<Pattern>());
								patternLinks.get(op.getNodeID())
										.add(patternObj);
							} else
								patternLinks.get(op.getNodeID())
										.add(patternObj);
						}
					}
				}
			}
		}
		// System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		return patternLinks;
	}

	public static ArrayList<ETLFlowOperation> getTargetNodesGivenSource(ETLFlowGraph G,
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
	public static ArrayList<ETLFlowOperation> getNodesOfExistingPatterns(
			Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			ETLFlowOperation flagNode, String flagName) {
		HashMap<String, ArrayList<String>> stepOperations =  new HashMap<>();
		ArrayList<ETLFlowOperation> targetNodes = getTargetNodesGivenSource(G, ops, flagNode);
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<ETLFlowOperation>();
		ArrayList<String> patternNames = JSONDictionaryParser.getPatternNames();
		Integer numOfVersions = 0;
		Integer numOfFlows = 0;
		Integer numOfSteps = 0;
		boolean pattern = false;
		int counter =0;
		if (patternNames.contains(flagName)) {
			for (ETLFlowOperation opT : targetNodes) {
				//******System.out.println("source node: "+flagNode.getNodeID()+", target node: "+opT.getNodeID());
			//in the HashMap: string1 =name (ex.implementationType), string2=value(ex.MERGE)
				numOfVersions = JSONDictionaryParser.getNumberOfPatternVersions(flagName);
				for (int v = 0; v < numOfVersions; v++){
					numOfFlows = JSONDictionaryParser.getNumberOfPatternFlows(flagName, v);
					for (int f=0; f < numOfFlows; f++){
						numOfSteps = JSONDictionaryParser.getNumberOfVersionFlowSteps(flagName, v, f);
						for (int s=0; s< numOfSteps; s++){
							stepOperations = JSONDictionaryParser.parseJSONPatternSteps(flagName, v, f, s);
							for (String name: stepOperations.keySet()){
								for (String value: stepOperations.get(name)){
						if ((name.equals("implementationType")
								&& opT.getImplementationType().equals(value)) || (name.equals("type")
								&& opT.getNodeKind().name().equals(value)) || (name.equals("optype") && opT.getOperationType().getOpTypeName().equals(value))) {
							pattern = true;
							patternNodes.add(flagNode);
							patternNodes.add(opT);
						} else if ()
					}
				}
			}
		}
				}}}
		return patternNodes;	
	}

}
