import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import operationDictionary.OperationTypeName;

public class PatternSearch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ETLFlowGraph G = XLMParser.getXLMGraph();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser.getNodePatternFlags();

	}
	
	//can obtain a list of pattern names that belong to a node
	public static ArrayList<String> getNodeFlags(OperationTypeName optypeName){
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser.getNodePatternFlags();
		ArrayList<String> nodeFlagNames = new ArrayList<String>();
		for (String opName: flagMapping.keySet()){
			if(opName.equals(optypeName.name())){
			for (String flagName: flagMapping.get(opName)){
				if(!flagName.equals("")){
				nodeFlagNames.add(flagName);
				}
			}
			}
		}
		return nodeFlagNames;
	}
	
	public static boolean checkPatternExistence(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			HashMap<String, ArrayList<String>> flagMapping) {
		ArrayList<String> flagNames = new ArrayList<String>();
		boolean pattern = false;
		
		for (Integer key : ops.keySet()) {
			OperationTypeName optypeName = ops.get(key).getOperationType().getOpTypeName();
			flagNames = getNodeFlags(optypeName);
			int pointer = 0;		
			for (String str : flagMapping.keySet()) {
					if (str.equals(optypeName.toString())) {
						if (flagNames.size() > 0){
							pointer = ops.get(key).getNodeID();
						for (String flagName: flagMapping.get(str)){
							if(flagName.equals("sortMerge")){
								pattern = checkSortMergePattern(ops, G, pointer);
							}else if(flagName.equals("recoveryPoint")){
								//call appropriate method
							}else if(flagName.equals("compensationAction")){
								//call appropriate method
							}else if(flagName.equals("subprocess")){
								//call subprocess pattern method
							}
						}
					
				}
			}
		}
	}
		return pattern;
	
	}
	
	public static boolean checkSortMergePattern(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G, Integer pointer){
		HashMap<String, ArrayList<String>> patternProperties = JSONDictionaryParser.getStartPatternProperties("sortMerge");
		boolean pattern = false;
		for (Integer key: ops.keySet()){
			if(ops.get(key).getNodeID() == pointer){
				for (Object e : G.edgeSet()) {
					ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
					ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
					if (opS.getNodeID() == pointer){
						for (String str: patternProperties.keySet()){
							for (String value: patternProperties.get(str)){
								if(str.equals("implementationType") && opT.getImplementationType().equals(value)){
									pattern = true;
								}
							}
						}
							
						}
						
					}
					
			}
		}
		return pattern;
	}
	
}
