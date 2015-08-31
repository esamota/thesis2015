package utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import importXLM.ImportXLMToETLGraph;
import operationDictionary.ETLOpTypeCharacteristic;
import operationDictionary.OperationTypeName;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import display.Demo;
import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.expressionTree.Expression;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class XLMParser {
	
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
		Integer sourceId = (Integer) ((ETLEdge) e).getSource();
		Integer targetId = (Integer) ((ETLEdge) e).getTarget();
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
	return pools;
}
}
