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
	ArrayList<ETLFlowOperation> nextNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
	int counter = 0;
	for (ETLFlowOperation nextNode: nextNodes){
		if (outPatternNodes.size() == 0) outPatternNodes.addAll(patternNodes);
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
	if (repeat.equals(">1") && counter > 1) {
		int midIndex = (outPatternNodes.size() - 1) / 2;
		for (int i = 1; i < midIndex; i++) {
			if (!outPatternNodes.get(i).getOperationType().getOpTypeName().name().equals(outPatternNodes.get(i + midIndex).getOperationType().getOpTypeName().name())) {
				return new ArrayList<>();
			}
		}
		return outPatternNodes;
	}
	else return patternNodes;
	}

}
