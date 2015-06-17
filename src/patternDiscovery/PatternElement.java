package patternDiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public abstract class PatternElement {
	
	private ArrayList<PatternElement> subElements;
	private String elementName;
	private String elementID;
	private HashMap<String, ArrayList<String>> whiteList;
	private HashMap<String, ArrayList<String>> blackList;
	
	public PatternElement(){
		subElements = new ArrayList<>();
		elementName = "";
		elementID = "";
		whiteList = new HashMap<>();
		blackList = new HashMap<>();
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
public HashMap<String, ArrayList<String>> getWhiteList() {
	return whiteList;
}

public void setWhiteList(HashMap<String, ArrayList<String>> whiteList) {
	this.whiteList = whiteList;
}

public HashMap<String, ArrayList<String>> getBlackList() {
	return blackList;
}

public void setBlackList(HashMap<String, ArrayList<String>> blackList) {
	this.blackList = blackList;
}

public abstract ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes);
	
	
	
	
}
