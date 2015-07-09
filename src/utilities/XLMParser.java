package utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import importXLM.ImportXLMToETLGraph;
import operationDictionary.ETLOpTypeCharacteristic;
import operationDictionary.OperationTypeName;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.expressionTree.Expression;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class XLMParser {
	
	//public static final String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\q13.xml";
	//public static final String XLMFilePathInput = display.Demo.xlmFile.getAbsolutePath();
	//public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-initial_agn.xml";
	//public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-all-patterns-1.xml";
	public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-all-patterns-2.xml";
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ETLFlowGraph G = getXLMGraph(XLMFilePathInput);
		Hashtable <Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		/*ArrayList<ETLFlowOperation> sourceNodes = getSourceNodesGivenTarget(G, ops.get(610));
		for (ETLFlowOperation op: sourceNodes){
			System.out.println(op.getNodeID());
		}*/
		
		//System.out.println(ops.get(137).getoProperties().get("send_true_to"));
		for (String str: ops.get(137).getoProperties().keySet()){
			for(ETLNonFunctionalCharacteristic chr: ops.get(137).getoProperties().get(str)){
				//System.out.println(chr.getChName()+" " +chr.getLeftOp()+" "+chr.getOper()+" "+chr.getRightOp());
			}
		}
		
		for(ETLNonFunctionalCharacteristic chr: ops.get(137).getoProperties().get("send_true_to")){
			System.out.println(chr.getRightOp());
		}
			/*ops.get(137).getoFeatures();
			System.out.println(ops.get(137).getSemanticsExpressionTrees());
			Hashtable <String, Expression> exprTree = ops.get(137).getSemanticsExpressionTrees();
			for (String str: exprTree.keySet()){
				System.out.println("expression kind:"+exprTree.get(str).getExKind());
				System.out.println("expression :"+exprTree.get(str).getLeftEx()+" "+exprTree.get(str).getOperator()+" "+exprTree.get(str).getRightEx());
				}*/
	}
	
	public static ETLFlowGraph getXLMGraph(String XLMFilePathInput){
		ImportXLMToETLGraph importXlm = new ImportXLMToETLGraph();
		ETLFlowGraph G = new ETLFlowGraph();
		try {
			G = importXlm.getFlowGraph(XLMFilePathInput);
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return G;
	}
	
	public static ArrayList<ETLFlowOperation> getTargetOperationsGivenSource (ETLFlowOperation sourceNode, ETLFlowGraph G){	
	Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
	ArrayList<ETLFlowOperation> targetNodes = new ArrayList<ETLFlowOperation>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			if (sourceNode == opS){
				targetNodes.add(opT);
			}
		}
		return targetNodes;
	}

	public static ArrayList<ETLFlowOperation> getSourceNodesGivenTarget(
			ETLFlowGraph G,
			ETLFlowOperation targetNode) {
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> sourceNodes = new ArrayList<ETLFlowOperation>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			if (targetNode == opT) {
				sourceNodes.add(opS);
			}

		}
		return sourceNodes;
	}
	
	// identify all nodes that have a data input going in
	public static ArrayList<Integer> nodesWithDataInput(ETLFlowGraph G) {
		
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<Integer> nodesWithDBInput = new ArrayList<Integer>();
		// ArrayList<HashMap> dbInputNodes = new ArrayList<HashMap>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());

			Integer sourceId = (Integer) ((ETLEdge) e).getSource();
			Integer targetId = (Integer) ((ETLEdge) e).getTarget();
			if (targetId.intValue() == opT.getNodeID()
					&& opS.getOperationType().getOpTypeName()
							.equals(OperationTypeName.TableInput)) {
				System.out.println("targetId " + targetId);
				nodesWithDBInput.add(targetId);
			}
		}
		/*
		 * for (Integer i : nodesWithDBInput) { HashMap dbInputNode = new
		 * HashMap(); dbInputNode.put("id", i); dbInputNodes.add(dbInputNode); }
		 */

		// System.out.println("dbInputNodes end " + dbInputNodes);
		return nodesWithDBInput;
	}

// identify all nodes that have a data output coming out
public static ArrayList<Integer> nodesWithDataOutput(ETLFlowGraph G) {
	
	Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
	ArrayList<Integer> nodesWithDBOutput = new ArrayList<Integer>();
	ArrayList<HashMap> dbOutputNodes = new ArrayList<HashMap>();
	for (Object e : G.edgeSet()) {
		ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
		ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
		// is there a simpler way to do this????
		Integer sourceId = (Integer) ((ETLEdge) e).getSource();
		Integer targetId = (Integer) ((ETLEdge) e).getTarget();
		// System.out.println("targetId " + targetId);
		if (sourceId.intValue() == opS.getNodeID()
				&& opT.getOperationType().getOpTypeName()
						.equals(OperationTypeName.TableOutput)) {
			System.out.println("sourceId " + sourceId);
			nodesWithDBOutput.add(sourceId);
		}
	}
	return nodesWithDBOutput;
}

//this was necessary for the hardcoded part, can be removed after getPoolElements method is finished
public static ArrayList<HashMap> flowEngineTypes(
		Hashtable<Integer, ETLFlowOperation> ops) {
	ArrayList<String> engineTypes = new ArrayList<String>();
	ArrayList<HashMap> pools = new ArrayList<HashMap>();
	ArrayList<Integer> flowIDs = new ArrayList<Integer>();
	ArrayList<String> uniquePools = new ArrayList<String>();
	// System.out.println("engineTypes before " + engineTypes);
	for (Integer i : ops.keySet()) {
		if (!engineTypes.contains(ops.get(i).getEngine().toString())) {
			engineTypes.add(ops.get(i).getEngine().toString());
		} else if (ops.get(i).getEngine().toString().isEmpty()) {
			System.out.println("empty engine info");
		}
		if (!flowIDs.contains(ops.get(i).getParentFlowID())) {
			flowIDs.add(ops.get(i).getParentFlowID());
		}
		if (!uniquePools.contains(ops.get(i).getEngine() + "_"
				+ ops.get(i).getParentFlowID())) {
			uniquePools.add(ops.get(i).getEngine() + "_"
					+ ops.get(i).getParentFlowID());
		}
	}
	System.out.println("flowIDS " + flowIDs);
	System.out.println("unique pools " + uniquePools);
	// engineTypes.add("PDI");
	for (Integer i : flowIDs) {
		for (String str : engineTypes) {
			HashMap pool = new HashMap();
			pool.put("name", str + "_" + i);
			pool.put("size", uniquePools.size());
			pool.put("engine", str);
			pool.put("flow", i);
			pools.add(pool);
		}
	}
	System.out.println("engineTypes " + engineTypes);
	return pools;
}
}
