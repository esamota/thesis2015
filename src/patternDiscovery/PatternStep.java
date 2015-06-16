package patternDiscovery;
import java.util.ArrayList;
import java.util.Random;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class PatternStep extends PatternElement{
private ArrayList<String> stepValues;
	
	public PatternStep (){
		super();
		stepValues = new ArrayList<>();
	}

	public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		if((getElementName().equals("optype") && stepValues.contains(node.getOperationType().getOpTypeName().name()))||
				(getElementName().equals("implementationType") && stepValues.contains(node.getImplementationType()))||
				(getElementName().equals("type") && stepValues.contains(node.getNodeKind().name()))){
			patternNodes.add(node);
		}
		return patternNodes;
		}


	public ArrayList<String> getStepValues() {
		return stepValues;
	}


	public void setStepValues(ArrayList<String> stepValues) {
		this.stepValues = stepValues;
	}
	
	public void addStepValue(String stepValue){
		this.stepValues.add(stepValue);
	}
	
}
