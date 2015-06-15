package patternDiscovery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import toBPMN.BPMNElement;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public class Pattern extends PatternElement{
private String description;
private ArrayList<BPMNElement> bpmnElements;
private HashMap<String, ArrayList<String>> whiteList;
private HashMap<String, ArrayList<String>> blackList;

	public Pattern() {
		super();
		description ="";
		bpmnElements = new ArrayList<BPMNElement>();
		whiteList = new HashMap<>();
		blackList = new HashMap<>();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
	public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>(patternNodes);
		ArrayList<ETLFlowOperation> nextNodes = new ArrayList<>();
		nextNodes.add(node);
		for (PatternElement element: getSubElements()){
			for (ETLFlowOperation nextNode: nextNodes){
			outPatternNodes = element.match(nextNode, G, outPatternNodes);
			if (outPatternNodes.size() == patternNodes.size()){
				return outPatternNodes;
			}
		}
			nextNodes.clear();
			nextNodes.addAll(patternDiscovery.PatternDiscovery.getTargetNodesGivenSource(G, ops, outPatternNodes.get(outPatternNodes.size()-1)));
		}
		return outPatternNodes;
		}

	public ArrayList<BPMNElement> getBpmnElements() {
		return bpmnElements;
	}

	public void setBpmnElements(ArrayList<BPMNElement> bpmnElements) {
		this.bpmnElements = bpmnElements;
	}
	
	public void addBpmnElement (BPMNElement bpmnElement){
		this.bpmnElements.add(bpmnElement);
	}
	}



