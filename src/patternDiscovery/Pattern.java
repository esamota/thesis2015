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
		for (int e = 0; e < getSubElements().size(); e++){
			outPatternNodes = getSubElements().get(e).match(nextNode, G, outPatternNodes);
			if ((patternNodes.size() != 0 && outPatternNodes.size() == 0) || outPatternNodes.size() == patternNodes.size()){
				return new ArrayList<>();
			}
			for (ETLFlowOperation outNode: outPatternNodes){
				if (!patternNodes.contains(outNode)) patternNodes.add(outNode);
			}
			if (e < getSubElements().size()-1 ){
				if (getSubElements().get(e+1).getElementName().equals("Sequence")){
					//if (!G.getAllTargetNodes().contains(utilities.XLMParser.getTargetOperationsGivenSource(outPatternNodes.get(outPatternNodes.size()-1), G).get(0))){
						nextNode = utilities.XLMParser.getTargetOperationsGivenSource(outPatternNodes.get(outPatternNodes.size()-1), G).get(0);
					//} else return outPatternNodes;
				} else {
				for (ETLFlowOperation op: outPatternNodes){
					if (utilities.XLMParser.getTargetOperationsGivenSource(op, G).size() > 1)
						nextNode = op;
					break;
				}
			}
			}
			else return outPatternNodes;
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



