import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import importXLM.ImportXLMToETLGraph;
import operationDictionary.OperationTypeName;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class XLMParser {
	
	//public static final String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\q13.xml";
	public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-initial_agn.xml";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static ETLFlowGraph getXLMGraph(){
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
	
	public static ArrayList<ETLFlowOperation> getTargetOperation(ETLFlowOperation sourceNode, ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops){	
	ArrayList<ETLFlowOperation> targetNodes = new ArrayList<ETLFlowOperation>();
		for (Object e : G.edgeSet()) {
			ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
			ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
			if (sourceNode.getNodeID() == opS.getNodeID()){
				targetNodes.add(opT);
			}
		}
		return targetNodes;
	}
	
	// identify all nodes that have a data input going in
	public static ArrayList<Integer> nodesWithDataInput(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops) {
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
public static ArrayList<Integer> nodesWithDataOutput(ETLFlowGraph G,
		Hashtable<Integer, ETLFlowOperation> ops) {
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
