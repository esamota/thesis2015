package patternDiscovery;
import java.util.ArrayList;
import java.util.HashMap;
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
		String stepName = getElementName();
		String nextStepName = "";
		ArrayList<String> nextStepValues = new ArrayList<>();
		HashMap<String, ArrayList<String>> whiteList = getParentElement().getWhiteList();
		HashMap<String, ArrayList<String>> blackList = getParentElement().getBlackList();
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>(patternNodes);
		ArrayList<ETLFlowOperation> targetNodes = new ArrayList<>();
		
		// if I need to loop I need to know my stoppers. Natural stopper is the blackList.
		//however, if the blackList is empty, we need to look for the next step as a stopper
		ArrayList<PatternElement> parentSubElements = getParentElement().getSubElements();
		//get nextStep name and values for stoppers
		for (int i=0; i< parentSubElements.size(); i++){
			if (parentSubElements.get(i).getElementID().equals(getElementID())){
				if (i+1 < parentSubElements.size()){
					nextStepName = parentSubElements.get(i+1).getElementName();
					nextStepValues = ((PatternStep)parentSubElements.get(i+1)).getStepValues();
					break;
				}
			}
		}
		if(simpleMatch(node) == true){
			System.out.println("Simple match true");
			outPatternNodes.add(node);
			return outPatternNodes;
		} else if (stepName.equals("$whiteList")){
				outPatternNodes.addAll(matchWhiteListStep(node, patternNodes, G, blackList, whiteList, nextStepName, nextStepValues));
				return outPatternNodes;
		
		} else if (stepName.equals("$anyType")){
			outPatternNodes.addAll(matchDoubleStarStep(node, patternNodes, G, blackList, nextStepName, nextStepValues));
			return outPatternNodes;	
		}
	return new ArrayList<>();
	}
	
	public boolean simpleMatch(ETLFlowOperation node){
		if (!node.getOperationName().equals("dummy")){
		if((getElementName().equals("optype") && stepValues.contains(node.getOperationType().getOpTypeName().name()))||
				(getElementName().equals("implementationType") && stepValues.contains(node.getImplementationType()))||
				(getElementName().equals("type") && stepValues.contains(node.getNodeKind().name()))){
			return true;
		}
		else return false;
		}
		return false;
	}
	
	public ArrayList<ETLFlowOperation> matchWhiteListStep(ETLFlowOperation node, ArrayList<ETLFlowOperation> patternNodes, ETLFlowGraph G, HashMap<String, ArrayList<String>> blackList, HashMap<String, ArrayList<String>> whiteList,
			String nextStepName, ArrayList<String> nextStepValues){
		ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>();
		//since steps are in a sequence, targetNodes.size is always 1
		if (checkNodeInList(node, whiteList) == true){
			System.out.println("whiteList match for target "+node.getNodeID()+ " true");
				outPatternNodes.add(node);
				for (ETLFlowOperation nextNode: targetNodes){
					outPatternNodes.addAll(matchWhiteListStep(nextNode, patternNodes, G, blackList, whiteList, nextStepName, nextStepValues));
				}
			return outPatternNodes;
		//this is for cases when $whitelist *v returns zero nodes before next step
		} else if (checkNodeAgainstNextStep(node, nextStepName, nextStepValues) == true){
			ETLFlowOperation fake = new ETLFlowOperation("whiteList");
			fake.setNodeID(node.getNodeID());
			outPatternNodes.add(fake);
			return outPatternNodes;
		//this is when the $whiteList *v match a blackList item
		} else {
			System.out.println("whiteList match for target "+node.getNodeID()+ " false");	
			return new ArrayList<>();
			}
		}
	
	public ArrayList<ETLFlowOperation> matchDoubleStarStep(ETLFlowOperation node, ArrayList<ETLFlowOperation> patternNodes, ETLFlowGraph G, 
			HashMap<String, ArrayList<String>> blackList, String nextStepName, ArrayList<String> nextStepValues){
		ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList<>();
		System.out.println("Step: we are at node "+ node.getNodeID());
		if (checkNodeAgainstNextStep(node, nextStepName, nextStepValues) == false && checkNodeInList(node, blackList) == false){
			System.out.println("Step: double star match for target "+node.getNodeID()+" true");
			outPatternNodes.add(node);
			for (ETLFlowOperation nextNode: targetNodes){
				outPatternNodes.addAll(matchDoubleStarStep(nextNode, patternNodes, G, blackList, nextStepName, nextStepValues));
			}
			return outPatternNodes;
		}else if (checkNodeAgainstNextStep(node, nextStepName, nextStepValues) == true || checkNodeInList(node, blackList) == true){
			ETLFlowOperation fake = new ETLFlowOperation("whiteList");
			fake.setNodeID(node.getNodeID());
			outPatternNodes.add(fake);
			System.out.println("This is the next step's node");
			return outPatternNodes;
		}else {
			System.out.println("Step: double star match for target "+node.getNodeID()+" false");
			return new ArrayList<>();
		}
	}
	
	public boolean checkNodeInList(ETLFlowOperation node, HashMap<String, ArrayList<String>> list){
		if (list.size() != 0){
			if ((list.containsKey("optype") && list.get("optype").contains(node.getOperationType().getOpTypeName().name()))||
				(list.containsKey("type") && list.get("type").contains(node.getNodeKind().name())) ||
						(list.containsKey("implementationType") && list.get("implementationType").contains(node.getImplementationType()))) {
				return true;
			} else return false;
		} else return false;
	}
	public ArrayList<String> getStepValues() {
		return stepValues;
	}

	public boolean checkNodeAgainstNextStep(ETLFlowOperation node, String nextStepName, ArrayList<String> nextStepValues){
		if (!nextStepName.isEmpty() && nextStepValues.size() != 0){
			if ((nextStepName.equals("optype") && nextStepValues.contains(node.getOperationType().getOpTypeName().name()))||
				(nextStepName.equals("type") && nextStepValues.contains(node.getNodeKind().name()))||
					(nextStepName.equals("implementationType") && nextStepValues.contains(node.getImplementationType()))){
				return true;
			} else return false;
		} else return false;
	}
	
	public void setStepValues(ArrayList<String> stepValues) {
		this.stepValues = stepValues;
	}
	
	public void addStepValue(String stepValue){
		this.stepValues.add(stepValue);
	}
	
}
