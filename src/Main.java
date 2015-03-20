import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import operationDictionary.ETLOperationType;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import importXLM.ImportXLMToETLGraph;
import etlFlowGraph.operation.*;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImportXLMToETLGraph importXlm = new ImportXLMToETLGraph();
		ETLFlowGraph G = null;
		try {
			G = importXlm.getFlowGraph("C:\\Users\\Elena\\Desktop\\q1.xml");
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*System.out.println("source node "+ G.getAllSourceNodes());
		ArrayList<Integer> targetNodes = new ArrayList<Integer>();
		targetNodes = G.getAllTargetNodes();
		System.out.println("target node " + G.getAllTargetNodes());
		System.out.println(targetNodes);*/
		
		//System.out.println(G.getEtlFlowOperations());
		//System.out.println(G.getAllEdges(10, 17));
		System.out.println(G);
		Hashtable <Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		try{
		File file = BPMNConstructs.writeBPMNFileWithHeader();
		
		FileWriter fileWriter = new FileWriter(file,true);
        BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
        bufferWriter.write("<startEvent id=\"_01\" name=\"StartProcess\" />\n");
        
		for (Integer key : ops.keySet()) {
			bufferWriter.write("<scriptTask id=\"_" + key + "\" name=\"" + ops.get(key).getOperationName() + "\">");
			bufferWriter.newLine();
			bufferWriter.write("\t<script>System.out.println(\"" + ops.get(key).getOperationName() + "\");</script>");
			bufferWriter.newLine();	
			bufferWriter.write("</scriptTask>\n");
		}
		
		bufferWriter.write("<endEvent id=\"_003\" name=\"EndProcess\">"+'\n'+
        "<terminateEventDefinition/>"+'\n'+ "</endEvent>\n");
		
		for (Integer key : ops.keySet()) {
			Set<ETLEdge> edgeSet = G.outgoingEdgesOf(key);
			ETLEdge[] edges = edgeSet.toArray(new ETLEdge[edgeSet.size()]);
			for (int i = 0; i < edges.length; i++) {
				Integer source = (Integer)edges[i].getSource();
				Integer target = (Integer)edges[i].getTarget();
				bufferWriter.write("<sequenceFlow id=\"_"+ source+ "-_"+target+"\""+
						" sourceRef=\"_"+ source +"\"" + " targetRef=\"_"+ target+ "\""+"/>"+'\n');
			}
		}
		
		//startEventID hardcoded - fix
		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		sourceNodes = G.getAllSourceNodes();
		for (int i=0; i< sourceNodes.size(); i++){
			bufferWriter.write("<sequenceFlow id=\"_01-_"+sourceNodes.toString().replaceAll("\\[", "").replaceAll("\\]","")+"\""+
						" sourceRef=\"_01\" targetRef=\"_"+ sourceNodes.toString().replaceAll("\\[", "").replaceAll("\\]","")+ "\""+"/>\n");
		}
		//endEventID hardcoded - fix
		ArrayList<Integer> targetNodes = new ArrayList<Integer>();
		targetNodes = G.getAllTargetNodes();
		for (int i=0; i< targetNodes.size(); i++){
			bufferWriter.write("<sequenceFlow id=\"_"+ targetNodes.toString().replaceAll("\\[", "").replaceAll("\\]","")+ "-_003\" sourceRef=\"_"+ targetNodes.toString().replaceAll("\\[", "").replaceAll("\\]","") +"\"" + " targetRef=\"_003\"/>\n");
		}
		
		bufferWriter.write("</process>\n");
		bufferWriter.write("</definitions>");
		bufferWriter.close();
		
		}catch(IOException e){
    		e.printStackTrace();
    	}
		
		
		
		
		
		//ETLFlowGraph G1 = (ETLFlowGraph) G.clone();
		//System.out.println(G1);
	}

}
