package toBPMN;
import patternDiscovery.*;
import importXLM.ImportXLMToETLGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import operationDictionary.ETLOperationType;
import operationDictionary.OperationTypeName;

import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.GraphPath;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import display.Demo;
import utilities.BPMNElementTagName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;
import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;

public class BPMNConstructsToFile extends DirectedAcyclicGraph {
	//private static String BPMNFilePathOutput = Demo.BPMNFilePathOutput;
	//private static String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
	
	public BPMNConstructsToFile(Class arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// String BPMN = toStringBPMN();
		ETLFlowGraph G = XLMParser.getXLMGraph(Demo.XLMFilePathInput);
		HashMap<String, ArrayList<String>> flagMappings = JSONDictionaryParser.parsePatternFlags(Demo.patternFlagMappingPath);
		ArrayList<BPMNElement> graphElements = PatternDiscovery.translateToBPMN(G, flagMappings, Demo.dictionaryFilePath);
		String BPMN = toStringBPMNWithDictionary(G, graphElements);
		toFileBPMN(Demo.BPMNFilePathOutput, BPMN);
		
		/*ArrayList<BPMNElement> graphElements = PatternDiscovery.translateToBPMN(G, Demo.flagMappings);
		for (BPMNElement el: graphElements){
			if (el.getElementName().equals(BPMNElementTagName.sequenceFlow.name())){
				//System.out.println(el.getSubElements().get(0).getElementName());
			}
		}*/
	}
	
	public static void toFileBPMN(String BPMNFilePathOutput, String writerInput) {
		File file = new File(BPMNFilePathOutput);
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(writerInput);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String toStringBPMNWithDictionary(ETLFlowGraph G, ArrayList<BPMNElement> graphElements) {
		//all graph operations
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		
		//gets an array list of pattern name flags for each optype in the dictionary
		
		//all elements from the dictionary that belong to the graph of this xLM document
		//ArrayList<BPMNElement> graphElements = PatternDiscovery.translateToBPMN(G, Demo.flagMappings);
		//edges already added in translateToBpmn function in PatternDiscovery
		//ArrayList<BPMNElement> edges = BPMNConstructsGenerator.getBPMNElementsEdge(G);
		//graphElements.addAll(edges);
		//startEvent and edges
		ArrayList<BPMNElement> startAndEgdes = BPMNConstructsGenerator.createProcessStartEvent(G);
		graphElements.addAll(startAndEgdes);
		//end event and edges
		ArrayList<BPMNElement> endAndEgdes = BPMNConstructsGenerator.createBPMNEndEvent(G);
		graphElements.addAll(endAndEgdes);
		//all elements that belong inside of a process tag and require a 
		// single--one-line self closing tag, like a task without i/o's
		ArrayList<HashMap> simpleProcessElements = new ArrayList<HashMap>();
		
		//all elements that belong inside of a process but have subelements inside, like a task with an I/O
		ArrayList<HashMap> complexProcessElements = new ArrayList<HashMap>();
		
		//all elements that are inside another element, like the data I/O association
		ArrayList<HashMap> subElements = new ArrayList<HashMap>();
		
		//a list of bpmn element collaboration with corresponding subelements
		ArrayList<BPMNElement> poolElements = BPMNConstructsGenerator.getPoolElements(ops);
		
		//all elements & attrs necessary to fill in the collaboration for the bpmn model
		ArrayList<HashMap> collaborationElements = BPMNConstructsGenerator.getPoolElementsVelocityFormat(poolElements);
		
		//all necessary subelements(participant) to fill the collaboration for the bpmn model
		ArrayList<HashMap> collborationSubElements = BPMNConstructsGenerator.getPoolSubElementsVelocityFormat(poolElements);
		
		ArrayList<HashMap> processTagElements = BPMNConstructsGenerator.getProcessTagElementsVelocityFormat(poolElements);
		
		//all elements that belong after the process tag is over, eg dataStore
		ArrayList<HashMap> dataStoreElements = new ArrayList<HashMap>();
		
		//all header elements that belong in the beginning of each bpmn model before the process starts
		ArrayList<HashMap> headerElements = new ArrayList<HashMap>();
		
		String stringAttributes = "";	
		int counter = 0;
		for (BPMNElement el : graphElements) {
			
			//System.out.println(el.getElementName()+" "+el.getSubElements().size());
			HashMap element = new HashMap();
			if (el.getElementName().equals(BPMNElementTagName.dataStore.name())) counter++;
			String dataStoreName="";
			for (BPMNAttribute attr : el.getAttributes()) {
				if (el.getElementName().equals(BPMNElementTagName.dataStore.name()) && 
						attr.name.equals("id"))
				attr.setAttributeValue("DS_"+String.valueOf(counter));
				if (attr.name.equals("dataStoreRef"))
					attr.setAttributeValue("DS_"+String.valueOf(counter));
				if (attr.getAttributeValue().contains(".")){
					attr.setAttributeValue(attr.value.substring(attr.value.lastIndexOf(".") + 1));
				}
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			element.put("attributes", stringAttributes);
			stringAttributes = "";
			element.put("name", el.getElementName());
			element.put("id", el.getElementID());
			element.put("text", el.getElementText());
			
			if (el.getElementName().equals(BPMNElementTagName.dataStore.name())){
				dataStoreElements.add(element);
			} else if(el.getSubElements().size() < 1 && !el.getElementName()
					.equals(BPMNElementTagName.multiInstanceLoopCharacteristics.name())){
				simpleProcessElements.add(element);
			} else if(el.getSubElements().size() >= 1){
				System.out.println(el.getElementName());
					complexProcessElements.add(element);
					subElements.addAll(loadSubElements(el));
		}
		}
		
		//System.out.println(subElements);
		
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate("vmTemplates2//jsonTest.vm");
		VelocityContext context = new VelocityContext();
		context.put("simpleElements", simpleProcessElements);
		context.put("complexElements", complexProcessElements);
		context.put("collaborationElements", collaborationElements);
		context.put("collaborationSubElements", collborationSubElements);
		context.put("subElements", subElements);
		context.put("processTagElements", processTagElements);
		context.put("dataStoreElements", dataStoreElements);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return (writer.toString());
	}
	
	public static ArrayList<HashMap> loadSubElements(BPMNElement el){
		ArrayList<HashMap> subElements = new ArrayList<HashMap>();
		String stringAttributes="";
		HashMap subElement = new HashMap();
		for (BPMNElement subEl: el.getSubElements()){
			subElement = new HashMap();
			subElement.put("elementID", el.getElementID());
			subElement.put("subName", subEl.getElementName());

			if (subEl.getElementText() != null)
			subElement.put("text", subEl.getElementText());
			
			for (BPMNAttribute attr : subEl.getAttributes()) {
				if (attr.getAttributeValue().contains(".")){
					attr.setAttributeValue(attr.value.substring(attr.value.lastIndexOf(".") + 1));
				}
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			subElement.put("attributes", stringAttributes);		
			stringAttributes = "";
		
			subElements.add(subElement);
		}
		return subElements;
	}
}
