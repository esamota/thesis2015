import java.util.ArrayList;
import java.util.Random;

import etlFlowGraph.operation.ETLFlowOperation;

public class Pattern {

	private String patternName;
	private String patternID;
	private ArrayList<ETLFlowOperation> patternNodes;
	private ETLFlowOperation patternStartNode;
	private ETLFlowOperation patternEndNode;

	public Pattern() {

	}

	public Pattern(String name) {
		Random randomGenerator = new Random();
		String randomID= "_p"+randomGenerator.nextInt(100);
		this.patternName = name;
		this.patternID = randomID;
		patternNodes = new ArrayList<ETLFlowOperation>();
		patternStartNode= new ETLFlowOperation();
		patternEndNode = new ETLFlowOperation();
	}

	public void addPatternNodes(ArrayList<ETLFlowOperation> nodes){
		for (ETLFlowOperation op: nodes){
			patternNodes.add(op);
		}
	}
	public void addPatternNode(ETLFlowOperation op1){
		patternNodes.add(op1);
	}
	
	public void addPatternNodes(ETLFlowOperation op1, ETLFlowOperation op2){
		patternNodes.add(op1);
		patternNodes.add(op2);
	}
	
	public void addPatternStartNode(ETLFlowOperation startOp){
		this.patternStartNode = startOp;
	}
	
	public void addPatternEndNode(ETLFlowOperation endOp){
		this.patternEndNode = endOp;
	}
	
	public void setPatternName(String name) {
		this.patternName = name;
	}

	public void setPatternID(String id) {
		this.patternID = id;
	}

	public String getPatternName() {
		return this.patternName;
	}

	public String getPatternID() {
		return this.patternID;
	}
	
	public ArrayList<ETLFlowOperation> getPatternNodes(){
		return this.patternNodes;
	}
	
	public ETLFlowOperation getPatternStartNode(){
		return this.patternStartNode;
	}
	
	public ETLFlowOperation getPatternEndNode(){
		return this.patternEndNode;
	}

}
