package patternDiscovery;

import java.util.ArrayList;
import java.util.Random;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public abstract class PatternElement {
	
	private ArrayList<PatternElement> subElements;
	private String elementName;
	private String elementID;
	
	public PatternElement(){
		subElements = new ArrayList<>();
		elementName = "";
		elementID = "";
	}

public void setElementName(String name){
	elementName = name;
	
}

public void setElementID(String id){
	elementID = id;
	
}

public String getElementName(){
	return elementName;
	
}

public String getElementID(){
	return elementID;
	
}
public void addPatternSubElement(PatternElement element){
	this.subElements.add(element);
}

public ArrayList<PatternElement> getSubElements(){
	return this.subElements;
}

public abstract ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes);
	
	
	
	
}
