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
private ArrayList<ETLFlowOperation> patternSubgraph;

	public Pattern() {
		super();
		description ="";
		bpmnElements = new ArrayList<BPMNElement>();
		patternSubgraph = new ArrayList<>();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	
	public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>(patternNodes);
		ETLFlowOperation nextNode = node;
		//System.out.println("subElement size: "+ getSubElements().size());
		for (PatternElement element: getSubElements()){
			outPatternNodes = element.match(nextNode, G, outPatternNodes);
			//System.out.println("outPatternNodes "+outPatternNodes);
			if (outPatternNodes.size() == patternNodes.size()){
				return outPatternNodes;
			}
			nextNode = outPatternNodes.get(outPatternNodes.size()-1);
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

	public ArrayList<ETLFlowOperation> getPatternSubgraph() {
		return patternSubgraph;
	}

	public void setPatternSubgraph(ArrayList<ETLFlowOperation> patternSubgraph) {
		this.patternSubgraph = patternSubgraph;
	}

	}



