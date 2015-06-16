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
		
		if (PatternDiscovery.getTargetNodesGivenSource(G, node).size() != 1){
			return patternNodes;
		}
		outPatternNodes.addAll(patternNodes);
		ETLFlowOperation nextNode = PatternDiscovery.getTargetNodesGivenSource(G, node).get(0);
		for (int s=0; s < getSubElements().size(); s++){
			outPatternNodes = getSubElements().get(s).match(nextNode, G, outPatternNodes);
			if (outPatternNodes.size() == patternNodes.size()){
				return outPatternNodes;
			}
			if (s < getSubElements().size()-1){
				if (patternDiscovery.PatternDiscovery.getTargetNodesGivenSource(G, nextNode).size() == 1)
					nextNode = patternDiscovery.PatternDiscovery.getTargetNodesGivenSource(G, nextNode).get(0);
				else return patternNodes;
		}
		}
		return outPatternNodes;
		}

}
