import importXLM.ImportXLMToETLGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.GraphPath;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class BPMNConstructs extends DirectedAcyclicGraph {
	
	public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\q1.xml";
	public static String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
	public static String startEventID = "01";
	public static String endEventID = "09";
	
	
	public BPMNConstructs(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		String BPMN = toStringBPMN();
		toFileBPMN(BPMN);
	}

	public static String toStringBPMN(){
	ImportXLMToETLGraph importXlm = new ImportXLMToETLGraph();
	ETLFlowGraph G = new ETLFlowGraph();
	try {
		G = importXlm.getFlowGraph(XLMFilePathInput);
	} catch (CycleFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	System.out.println(G);
	Hashtable <Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
	Hashtable <String, ArrayList<ETLNonFunctionalCharacteristic>> props = G.getFlowProperties();
	Hashtable <String, ArrayList<ETLNonFunctionalCharacteristic>> feats = G.getFlowFeatures();
	Hashtable <String, ArrayList<ETLNonFunctionalCharacteristic>> resrs = G.getFlowResources();
	
		
		ArrayList<HashMap> nodes = new ArrayList<HashMap>();
		ArrayList<HashMap> edges = new ArrayList<HashMap>();
		
		int nodeCnt = -1;
		
		HashMap<Integer,Integer> added = new HashMap<Integer, Integer>();
				
		for (Object e: G.edgeSet()){
			// adding link
			HashMap edge = new HashMap();
			ETLFlowOperation opS = ops.get(((ETLEdge)e).getSource()); 
			ETLFlowOperation opT = ops.get(((ETLEdge)e).getTarget()); 
			int srcID = -1, targetID = -1;
			
			
			if (added.containsKey(opS.getNodeID())){
				srcID = added.get(opS.getNodeID());				
			}
			else{
				srcID = ++nodeCnt;
				HashMap nodeS = new HashMap();
				G.fillInNodes(opS, nodeS);
				nodes.add(nodeS);
				added.put(opS.getNodeID(), srcID);
			}
			
			
			if (added.containsKey(opT.getNodeID())){
				targetID = added.get(opT.getNodeID());				
			}
			else{
				targetID = ++nodeCnt;
				HashMap nodeT = new HashMap();
				G.fillInNodes(opT, nodeT);
				nodes.add(nodeT);
				added.put(opT.getNodeID(), targetID);
			}
			
			//source
			ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
			sourceNodes = G.getAllSourceNodes();
			
			ArrayList<Integer> targetNodes = new ArrayList<Integer>();
			targetNodes = G.getAllTargetNodes();
			
			//for edges get nodeID instead of name
			edge.put("from", opS.getNodeID());
			edge.put("to", opT.getNodeID());
			edge.put("source", sourceNodes.toString().replaceAll("\\[", "").replaceAll("\\]",""));
			edge.put("target", targetNodes.toString().replaceAll("\\[", "").replaceAll("\\]",""));
			edge.put("enabled", "Y");
			edges.add(edge);
			
		}
	
		
		//ndproperties
		ArrayList<HashMap> properties = new ArrayList<HashMap>();
		for (String key: props.keySet()){
			for (ETLNonFunctionalCharacteristic c: props.get(key)){
				HashMap prop = new HashMap();
				prop.put("name",key);
				prop.put("leftfun", c.getLeftFun());
				prop.put("leftop", c.getLeftOp());
				prop.put("oper", c.getOper());
				prop.put("rightfun", c.getRightFun());
				prop.put("rightop", c.getRightOp());
				properties.add(prop);
			}			
		}
		
						
		//ndresources
		ArrayList<HashMap> resources = new ArrayList<HashMap>();
		for (String key: resrs.keySet()){
			for (ETLNonFunctionalCharacteristic c: resrs.get(key)){
				HashMap res = new HashMap();
				res.put("name",key);
				res.put("leftfun", c.getLeftFun());
				res.put("leftop", c.getLeftOp());
				res.put("oper", c.getOper());
				res.put("rightfun", c.getRightFun());
				res.put("rightop", c.getRightOp());
				resources.add(res);
			}			
		}
		
		//ndfeatures
		ArrayList<HashMap> features = new ArrayList<HashMap>();
		for (String key: feats.keySet()){
			for (ETLNonFunctionalCharacteristic c: feats.get(key)){
				HashMap feat = new HashMap();
				feat.put("name",key);
				feat.put("leftfun", c.getLeftFun());
				feat.put("leftop", c.getLeftOp());
				feat.put("oper", c.getOper());
				feat.put("rightfun", c.getRightFun());
				feat.put("rightop", c.getRightOp());
				features.add(feat);
			}			
		}
		
		
		
	    VelocityEngine ve = new VelocityEngine();
	    ve.init();	       
	    Template t = ve.getTemplate( "vmTemplates//bpmn.vm" );
	    VelocityContext context = new VelocityContext();
	    context.put("edges", edges);
	    context.put("nodes", nodes);
	   // context.put("properties", properties);
	  //  context.put("resources", resources);
	  //  context.put("features", features);
	    //context.put("metadata", flowMetadata);
	    StringWriter writer = new StringWriter();
	    t.merge(context, writer);   
	    
	    return (writer.toString());
	}
	
	
	public static void toFileBPMN(String writerInput) {
		File file = new File(BPMNFilePathOutput);
		try{
 
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(writerInput);
		bw.close();
		}
	catch (IOException e) {
		e.printStackTrace();
	}
	}

	
}
