package patternDiscovery;
import java.util.ArrayList;
import java.util.Hashtable;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public class PatternSequence extends PatternElement{
	
	public PatternSequence(){
		super();
	}
	

	public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList();
		System.out.println("Seq: we are at node "+ node.getNodeID());
		System.out.println("Seq size: "+ getSubElements().size());
		/*if (utilities.XLMParser.getTargetOperationsGivenSource(node, G).size() != 1){
			System.out.println("More than one traget node in a sequence");
			return patternNodes;
		}*/
		outPatternNodes.addAll(patternNodes);
		ETLFlowOperation nextNode = node;
		for (int s=0; s < getSubElements().size(); s++){
			outPatternNodes = getSubElements().get(s).match(nextNode, G, outPatternNodes);
			
			if (outPatternNodes.size() == patternNodes.size()){
				System.out.println("Seq match failed");
				return patternNodes;
			}
			if(outPatternNodes.get(outPatternNodes.size()-1).getOperationName().equals("whiteList")){
				outPatternNodes.remove(outPatternNodes.size() - 1);
				nextNode = utilities.XLMParser.getTargetOperationsGivenSource(outPatternNodes.get(outPatternNodes.size() - 1), G).get(0);
				for (ETLFlowOperation op: outPatternNodes){
					if (!patternNodes.contains(op)) patternNodes.add(op);
				}
			} else patternNodes.add(node);
			
			if (s <= getSubElements().size()-1 && !getSubElements().get(s).getElementName().equals("$whiteList") && !getSubElements().get(s).getElementName().equals("*t")){
				if (utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).size() == 1)
					nextNode = utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).get(0);
				else return patternNodes;
			}
		}
		return outPatternNodes;
		}

}
