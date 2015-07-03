package patternDiscovery;

import java.util.ArrayList;
import java.util.Hashtable;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public class PatternFlow extends PatternElement{
	
private String repeat;


public PatternFlow(){
	super();
	this.repeat = "";
}


public void setPatternFlowRepeatValue(String repeat){
	this.repeat = repeat;
}

public String getFlowRepeatValue(){
	return this.repeat;
}

public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
	Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
	ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList();
	
	if (utilities.XLMParser.getTargetOperationsGivenSource(node, G).size() == 1){
		return patternNodes;
	}
	outPatternNodes.addAll(patternNodes);
	ArrayList<ETLFlowOperation> nextNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
	int counter = 0;
	for (ETLFlowOperation nextNode: nextNodes){
		for (int s=0; s < getSubElements().size(); s++){
			outPatternNodes = getSubElements().get(s).match(nextNode, G, outPatternNodes);
			if (outPatternNodes.size() > patternNodes.size()){
				counter++;
			}
			if (repeat.equals("=1") && counter == 1) {
				return outPatternNodes;
			}
		}
	}
	if (repeat.equals(">1") && counter > 1) return outPatternNodes;
	else return patternNodes;
	}

}
