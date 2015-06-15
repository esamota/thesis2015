package patternDiscovery;

import java.util.ArrayList;

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
	ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>(patternNodes);
	outPatternNodes = getSubElements().get(0).match(node, G, outPatternNodes);
	return outPatternNodes;
	
}

}
