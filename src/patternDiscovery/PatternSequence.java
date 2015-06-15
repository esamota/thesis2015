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
		outPatternNodes.addAll(patternNodes);
		ETLFlowOperation nextNode = node;
		for (PatternElement step: getSubElements()){
			outPatternNodes = step.match(nextNode, G, outPatternNodes);
			if (outPatternNodes.size() == patternNodes.size()){
				return outPatternNodes;
			}
			if (patternDiscovery.PatternDiscovery.getTargetNodesGivenSource(G, ops, nextNode).size() != 0)
				nextNode = patternDiscovery.PatternDiscovery.getTargetNodesGivenSource(G, ops, nextNode).get(0);
		}
		return outPatternNodes;
		}

}
