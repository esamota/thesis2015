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
		
		/*if (utilities.XLMParser.getTargetOperationsGivenSource(node, G).size() != 1){
			System.out.println("More than one traget node in a sequence");
			return patternNodes;
		}*/
		outPatternNodes.addAll(patternNodes);
		ETLFlowOperation nextNode = node;
		for (int s=0; s < getSubElements().size(); s++){
			//System.out.println("node : "+nextNode.getOperationName());
			outPatternNodes = getSubElements().get(s).match(nextNode, G, outPatternNodes);
			//System.out.println("out size "+outPatternNodes.size()+ ", patternNodes size "+ patternNodes.size());
			if (outPatternNodes.size() == patternNodes.size()){
				System.out.println("outPat is equal to patNodes");
				return new ArrayList<>();
			}
			patternNodes.add(nextNode);
			if (s < getSubElements().size()-1){
				if (utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).size() == 1)
					nextNode = utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).get(0);
				else return patternNodes;
			}
		}
		//System.out.println("SEQUENCE out array size "+ outPatternNodes.size());
		return outPatternNodes;
		}

}
