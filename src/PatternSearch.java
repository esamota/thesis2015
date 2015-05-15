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
		checkPatternExistence(ops, G, flagMapping);

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
	
	public static ArrayList<ETLFlowOperation> checkPatternExistence(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G,
			HashMap<String, ArrayList<String>> flagMapping) {
		ArrayList<String> flagNames = new ArrayList<String>();
		ArrayList<ETLFlowOperation> mappedNodes = new ArrayList<ETLFlowOperation>();
		boolean pattern = false;
		
		for (Integer key : ops.keySet()) {
			OperationTypeName optypeName = ops.get(key).getOperationType().getOpTypeName();
			//System.out.println("id= "+ ops.get(key).getNodeID()+ " "+optypeName +" implementationType= "+ops.get(key).getImplementationType());
			flagNames = getNodeFlags(optypeName);
			int pointer = 0;		
			for (String str : flagMapping.keySet()) {
					if (str.equals(optypeName.toString())) {
						if(flagNames.size() == 0){
							//System.out.println("flagName size= "+ flagNames.size());
							mappedNodes.add(ops.get(key));
						}
						else if (flagNames.size() > 0){
							//System.out.println("flagName size= "+ flagNames.size());
							pointer = ops.get(key).getNodeID();
						for (String flagName: flagMapping.get(str)){
							//System.out.println("flagName name= "+ flagName);
								pattern = checkPatternStart(ops, G, pointer, flagName);
								//System.out.println("sortMerge pattern= "+ pattern);
								if(pattern == false){
									mappedNodes.add(ops.get(key));
								} else if (pattern == true){
									//pattern = checkMiddlePattern();
											
				}
			}
		}
	}
			}
		}
		//return pattern;
		return mappedNodes;
	
	}
	
	public static boolean checkPatternStart(Hashtable<Integer, ETLFlowOperation> ops, ETLFlowGraph G, Integer pointer, String flagName){
		HashMap<String, ArrayList<String>> patternStartProperties = JSONDictionaryParser.getStartPatternProperties(flagName);
		//save ids of nodes that are consumed by a pattern
		boolean pattern = false;
		for (Integer key: ops.keySet()){
			if(ops.get(key).getNodeID() == pointer){
				for (Object e : G.edgeSet()) {
					ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
					ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
					if (opS.getNodeID() == pointer){
						for (String str: patternStartProperties.keySet()){
							for (String value: patternStartProperties.get(str)){
								if(str.equals("implementationType") && opT.getImplementationType().equals(str)){
									pattern = true;
								}
								else if(str.equals("type") && opT.getOperationType().getOpTypeName().equals(str)){
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
