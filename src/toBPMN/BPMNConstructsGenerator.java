package toBPMN;
import utilities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.collections.functors.OrPredicate;
import org.apache.xerces.parsers.XMLParser;

import display.Demo;
import operationDictionary.OperationTypeName;
import patternDiscovery.PatternDiscovery;
import patternDiscovery.Pattern;
import utilities.BPMNElementTagName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;
import etlFlowGraph.ETLNonFunctionalCharacteristic;
import etlFlowGraph.attribute.Attribute;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;


public class BPMNConstructsGenerator {
	
	public static void main(String[] args) {
		ETLFlowGraph G = XLMParser.getXLMGraph(Demo.XLMFilePathInput);
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> subgraph = new ArrayList<>();
		subgraph.add(ops.get(72)); 
		subgraph.add(ops.get(668));
		subgraph.add(ops.get(424));
		subgraph.add(ops.get(1092));
		Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(Demo.dictionaryFilePath, "mergeJoin");
		pattern.setPatternSubgraph(subgraph);
		ETLFlowGraph subGraph = PatternDiscovery.createSubGraph(G, subgraph);
		/*System.out.println("subG "+subGraph);
		System.out.println("subG target nodes "+subGraph.getAllTargetNodes().size());
		ArrayList<BPMNElement> endEventElements = createBPMNEndEvent(subGraph, null);
		for (BPMNElement el: endEventElements){
			System.out.println("name "+ el.getElementName());
			for (BPMNAttribute attr: el.getAttributes()){
				System.out.println(attr.name +" "+attr.value);
			}
		}*/
		/*ArrayList<BPMNElement> poolElements = getPoolElements(ops);
		for (BPMNElement el: poolElements){
			System.out.println("name "+ el.getElementName());
			for (BPMNAttribute attr: el.getAttributes()){
				System.out.println(attr.name +" "+attr.value);
			}
		}*/
		
		/*BPMNElement el = createOutputSpecification(G, ops.get(30), JSONDictionaryParser.getAnyPatternElementByName("Filter").getBpmnElements().get(0));
		System.out.println(el.getSubElements().get(0).getElementText());
		//System.out.println(el.getElementName()+ " " + el.getElementText());*/
		
		/*Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName(JSONDictionaryParser.dictionaryFilePath, "mergeJoin");
		ArrayList<ETLFlowOperation> patternNodes = new ArrayList<>();
		patternNodes.add(ops.get(105));
		patternNodes.add(ops.get(649));
		patternNodes.add(ops.get(583));
		pattern.setPatternSubgraph(patternNodes);
		Integer counter = 1;
		ArrayList<BPMNElement> bpmn = getPatternBPMNElements(G, pattern);
		for (BPMNElement el: bpmn){
			System.out.println(el.getElementName());
			for (BPMNAttribute attr: el.getAttributes()){
				System.out.println(attr.getAttributeName()+" "+attr.getAttributeValue());
			}
			for (BPMNElement sub: el.getSubElements()){
				System.out.println(sub.getElementName()+" "+sub.getElementText());
				for (BPMNAttribute attr1: sub.getAttributes()){
					System.out.println(attr1.getAttributeName()+" "+attr1.getAttributeValue());
				}
			}
		}
		*/
		/*ArrayList<BPMNElement> startEventElements = createProcessStartEvent(G);
		for (BPMNElement el: startEventElements){
			System.out.println(el.getElementName());
		}*/
		/*ArrayList<BPMNElement> edges = getBPMNElementsEdge(G);
		for (BPMNElement el: edges){
			if (el.getElementText() != null){
				System.out.println(el.getElementText());
				for (BPMNAttribute attr: el.getAttributes()){
					System.out.println(attr.name+" "+attr.value);
				}
			}
		}*/
	}
	
	public static ArrayList<BPMNElement> createMainProcessStartEvent(ArrayList<Integer> graphSourceNodes, String idValue){
		ArrayList<BPMNElement> startEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement startEvent = new BPMNElement(BPMNElementTagName.startEvent.name());
		BPMNAttribute id = new BPMNAttribute("id", "_s0"+idValue);
		BPMNAttribute name= new BPMNAttribute("name", "StartProcess");
		startEvent.setAttribute(id);
		startEvent.setAttribute(name);
		startEventAndEdges.add(startEvent);
		
		if (graphSourceNodes.size() > 1) {
			startEventAndEdges.add(createSequenceFlow(id.getAttributeValue(), "_g0"+idValue));
			startEventAndEdges.add(createParallelGateway("Diverging", null ,"_g0"+idValue));
		}
		
		// generate sequence flows from start event to all source nodes
		for (Integer i: graphSourceNodes){
			if (i == 1) i = 1111;
			if (graphSourceNodes.size() == 1){
				startEventAndEdges.add(createSequenceFlow(id.getAttributeValue(), "_"+String.valueOf(i)));
			} else if (graphSourceNodes.size() > 1){
				startEventAndEdges.add(createSequenceFlow("_g0"+idValue, "_"+String.valueOf(i)));
			}
		}
		return startEventAndEdges;
	}
	//this works for the process, for subprocesses, need different attribute values.
	public static ArrayList<BPMNElement> createProcessStartEvent(ETLFlowGraph G){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> startEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement startEvent = new BPMNElement(BPMNElementTagName.startEvent.name());
		Random randomGenerator = new Random();
		Integer randomNumber = randomGenerator.nextInt(1000);
		String randomID = "_s0"+randomNumber; 
		BPMNAttribute id = new BPMNAttribute("id");
		id.setAttributeValue(randomID);
		
		BPMNAttribute name= new BPMNAttribute("name", "StartProcess");
		startEvent.setAttribute(id);
		startEvent.setAttribute(name);
		startEventAndEdges.add(startEvent);
		
		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		ArrayList<Integer> allSourceNodes = G.getAllSourceNodes();
		
		for (Integer i : allSourceNodes) {
			//if the node is a datastore connect start event to their target unless it is a join
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				for (Object e : G.edgeSet()) {
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.Join)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.LeftOuterJoin)) {
						sourceNodes.add(targetId);
					}
				}
			} else {
				sourceNodes.add(i);
			}
		}
		
		System.out.println("********************* sourceNodes size"+sourceNodes.size());
		
		if (sourceNodes.size() > 1) {
			startEventAndEdges.add(createSequenceFlow(id.getAttributeValue(), "_g0"+randomNumber));
			startEventAndEdges.add(createParallelGateway("Diverging", null ,"_g0"+randomNumber));
		}
		
		// generate sequence flows from start event to all source nodes
		for (Integer i: sourceNodes){
			if (i == 1) i = 1111;
			if (sourceNodes.size() == 1){
				startEventAndEdges.add(createSequenceFlow(id.getAttributeValue(), "_"+String.valueOf(i)));
			} else if (sourceNodes.size() > 1){
				startEventAndEdges.add(createSequenceFlow("_g0"+randomNumber, "_"+String.valueOf(i)));
			}
		}
		return startEventAndEdges;
	}
	
	public static ArrayList<BPMNElement> createBPMNEndEvent(ETLFlowGraph G, String idValue){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> endEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement endEvent = new BPMNElement(BPMNElementTagName.endEvent.name());
		Random randomGenerator = new Random();
		Integer randomNumber = randomGenerator.nextInt(1000);
		String randomID="";
		if (idValue == null) randomID= "_e0"+randomNumber;
		else randomID = "_e0"+idValue;
		BPMNAttribute id = new BPMNAttribute("id", randomID);
		BPMNAttribute name= new BPMNAttribute("name", "EndProcess");
		endEvent.setAttribute(id);
		endEvent.setAttribute(name);
		endEventAndEdges.add(endEvent);
		// target nodes of the graph to use when connecting to the final BPMN
				// place
		ArrayList<Integer> allTargetNodes = new ArrayList<Integer>();
		allTargetNodes = G.getAllTargetNodes();
		ArrayList<Integer> sourceOfTargetNodes = new ArrayList<Integer>();
		ArrayList<Integer> targetNodes = new ArrayList<Integer>();

		for (Integer i : allTargetNodes) {
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				for (Object e : G.edgeSet()) {
				Integer sourceId = (Integer) ((ETLEdge) e).getSource();
				Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (targetId.intValue() == i.intValue() && !sourceOfTargetNodes.contains(sourceId) 
							&& !(ops.get(sourceId).getOperationType().getOpTypeName().equals(OperationTypeName.Router) &&
								XLMParser.getTargetOperationsGivenSource(ops.get(sourceId), G).size() > 1)
							&& !(ops.get(sourceId).getOperationType().getOpTypeName().equals(OperationTypeName.Splitter) && 
									XLMParser.getTargetOperationsGivenSource(ops.get(sourceId), G).size() > 1)) {
						System.out.println("adding to targetNodes "+sourceId);
						targetNodes.add(sourceId);
					}
				}
			}else{
				System.out.println("adding to targetNodes "+i);
				targetNodes.add(i);
			}
			}
		if (targetNodes.size() == 1) {
			if (ops.get(targetNodes.get(0)).getOperationType().getOpTypeName().equals(OperationTypeName.Join)||
					ops.get(targetNodes.get(0)).getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin)){
				endEventAndEdges.add(createSequenceFlow("_0"+String.valueOf(targetNodes.get(0)), randomID));
			} else{
				endEventAndEdges.add(createSequenceFlow("_"+String.valueOf(targetNodes.get(0)), randomID));
			}
		} else {
		endEventAndEdges.add(createParallelGateway("Converging", null, "_g0"+randomNumber));
		// generate sequence flows from all target nodes to the end event
		for (Integer i: targetNodes){
			if (ops.get(i).getOperationType().getOpTypeName().equals(OperationTypeName.Join)||
					ops.get(i).getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin)){
				if (i == 1) i = 1111;
				endEventAndEdges.add(createSequenceFlow("_0"+String.valueOf(i), "_g0"+randomNumber));
				endEventAndEdges.add(createSequenceFlow("_g0"+randomNumber, randomID));
			} else{
				if (i == 1) i = 1111;
				endEventAndEdges.add(createSequenceFlow("_"+String.valueOf(i), "_g0"+randomNumber));
				endEventAndEdges.add(createSequenceFlow("_g0"+randomNumber, randomID));
			}
		}
		}
	return endEventAndEdges;

	}
	public static BPMNElement createBPMNSubprocess(Pattern pattern, boolean isForCompensationValue, boolean triggeredByEventValue){
		BPMNElement subprocess = new BPMNElement(pattern.getElementName()+"_"+"Pattern");
		//attributes ---------------------------------------------------------------------
		ArrayList<BPMNAttribute> subprocessAttributes = new ArrayList<BPMNAttribute>();
		BPMNAttribute completionQuantity = new BPMNAttribute("completionQuantity", "1");
		BPMNAttribute id = new BPMNAttribute("id", "_"+pattern.getElementID());
		BPMNAttribute isForCompensation = new BPMNAttribute("isForCompensation", "\""+isForCompensationValue+"\"");
		BPMNAttribute name = new BPMNAttribute("name", pattern.getElementName()+"_"+"Pattern");
		BPMNAttribute startQuantity = new BPMNAttribute("startQuantity", "1");
		BPMNAttribute triggeredByEvent = new BPMNAttribute("triggeredByEvent", "\""+triggeredByEventValue+"\"");
		subprocessAttributes.addAll(Arrays.asList(completionQuantity, id, isForCompensation, name, startQuantity, triggeredByEvent));
		subprocess.setAttributes(subprocessAttributes);
		//-------------------------------------------------------------------------------
		//subelements
		return subprocess;
	}
	
	//fill in attr values for nodes that correspond to a single bpmn element. save each node -- corresponding bpmn element pair
	public static ArrayList<BPMNElement> getPatternBPMNElements(ETLFlowGraph G, Pattern pattern) {
		
		ArrayList<BPMNElement> bpmnElements = new ArrayList<BPMNElement>();
		ArrayList<BPMNElement> patternBPMNElements = pattern.getBpmnElements();
		ArrayList<ETLFlowOperation> patternSubgraph = pattern.getPatternSubgraph();
		ETLFlowOperation node = new ETLFlowOperation();
		if (patternSubgraph.size() > 0) node = patternSubgraph.get(0);
		String randomID="_0"+String.valueOf(node.getNodeID());
		ArrayList<Integer> nodesWithInput = XLMParser.nodesWithDataInput(G);
		ArrayList<Integer> nodesWithOutput = XLMParser.nodesWithDataOutput(G);
		
		//single node patterns
		//*****************************************************************************************	
		if (patternSubgraph.size() == 1 && !pattern.getElementName().equals("subprocess")){
		for(BPMNElement el: patternBPMNElements){
			el.setID(String.valueOf(node.getNodeID()));
			
			//if gateway outputs to a file, add a task + seqFlow in between
			if (node.getOperationType().getOpTypeName().equals(OperationTypeName.Router) || 
					node.getOperationType().getOpTypeName().equals(OperationTypeName.Splitter)){
			for (ETLFlowOperation target: XLMParser.getTargetOperationsGivenSource(node, G)){
				if (target.getNodeKind().equals(ETLNodeKind.Datastore)){
					BPMNElement task = new BPMNElement(BPMNElementTagName.task.name());
					BPMNAttribute attr1 = new BPMNAttribute("id", "_0"+ String.valueOf(node.getNodeID()));
					BPMNAttribute attr2 = new BPMNAttribute("name", "write Data");
					task.setAttribute(attr1);
					task.setAttribute(attr2);
					task = createOutputSpecification(G, node, "0"+node.getNodeID(), task);
					task.setID(String.valueOf(node.getNodeID()));
					BPMNElement seq = createSequenceFlow("_"+String.valueOf(node.getNodeID()), "_0"+String.valueOf(node.getNodeID()));
					seq.setID(String.valueOf(node.getNodeID()));
					bpmnElements.add(task);
					bpmnElements.add(seq);
				}
			}
				
			}
			if (nodesWithInput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.task.name())){
				el = createInputSpecification(G, node, String.valueOf(node.getNodeID()), el);
				el.setID(String.valueOf(node.getNodeID()));
			} 
			if (nodesWithOutput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.task.name())){
				el = createOutputSpecification(G, node, String.valueOf(node.getNodeID()), el);
				el.setID(String.valueOf(node.getNodeID()));
			}
				for (BPMNAttribute attr : el.getAttributes()) {	
						switch(attr.getAttributeName()){
						case "name":
							if (attr.getAttributeValue().equals("$graph")){
								attr.setAttributeValue(node.getOperationName());
								break;
							}else if (attr.getAttributeValue().equals("$create")){
								attr.setAttributeValue(node.getOperationType().getOpTypeName().name());
								break;
							}
							break;
						case "id":
							if (attr.getAttributeValue().equals("$graph")){
								if (el.getElementName().equals(BPMNElementTagName.sequenceFlow.name())){
									if (node.getNodeID() == 1) attr.setAttributeValue("_111"+String.valueOf(node.getNodeID())+"-"+randomID);
									else attr.setAttributeValue("_"+String.valueOf(node.getNodeID())+"-"+randomID);
									break;
								} else {
									if (node.getNodeID() == 1) attr.setAttributeValue("_111"+ String.valueOf(node.getNodeID()));
									else attr.setAttributeValue("_"+ String.valueOf(node.getNodeID()));
									break;
								}
							} else if (attr.getAttributeValue().equals("$create")){
								attr.setAttributeValue(randomID);
								break;
							} else 
							break;
							
						case "sourceRef":
							if (el.getElementName().equals(BPMNElementTagName.sequenceFlow.name())){
								if (node.getNodeID() == 1) {
									attr.setAttributeValue("_111"+String.valueOf(node.getNodeID()));
									break;
								}else{
									attr.setAttributeValue("_"+String.valueOf(node.getNodeID()));
									break;
								}
							} else if (el.getElementName().equals(BPMNElementTagName.association.name())){
								attr.setAttributeValue("_"+String.valueOf(patternSubgraph.get(0).getNodeID()));
								if (attr.equals("_1")) attr.setAttributeValue("_1111");
								break;
							}
							break;
						case "targetRef":
								attr.setAttributeValue(randomID);
							break;
						}
					}
				bpmnElements.add(el);
				}
			//complex patterns
			//***********************************************************************************
			} else if (patternSubgraph.size() > 1 || pattern.getElementName().equals("subprocess")){
				Random randomGenerator = new Random();
				String elementID= "e"+randomGenerator.nextInt(100);
				ETLFlowGraph Gsub = PatternDiscovery.createSubGraph(G, patternSubgraph);
				Hashtable <Integer, ETLFlowOperation> opsSub = Gsub.getEtlFlowOperations();	
				
				for(BPMNElement el: patternBPMNElements){
					el.setID(elementID);
					for (ETLFlowOperation subgraphNode: patternSubgraph){
						if (nodesWithInput.contains(subgraphNode.getNodeID()) && el.getElementName().equals(BPMNElementTagName.subProcess.name())){
							el = (createInputSpecification(G, subgraphNode, pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID(), el));
							el.setID(elementID);
						}
						if (nodesWithOutput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.subProcess.name())){
							el = (createOutputSpecification(G, subgraphNode, pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID(), el));
							el.setID(elementID);
						}
						
					}
					if (patternBPMNElements.size() > 1 && el.getElementName().equals(BPMNElementTagName.subProcess.name())){
						el.setSubElement(patternBPMNElements.get(1));
					}
					for (BPMNAttribute attr : el.getAttributes()) {	
						switch(attr.getAttributeName()){
							case "name": {
								attr.setAttributeValue(pattern.getElementName());
							}
							break;
							case "id": {
								if (el.getElementName().equals(BPMNElementTagName.textAnnotation.name())){
									if (attr.getAttributeValue().equals("$create")) 
										attr.setAttributeValue("_"+elementID);
									break;
									} else attr.setAttributeValue("_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID());
								break;
							}
							case "sourceRef": {
								attr.setAttributeValue("_"+String.valueOf(patternSubgraph.get(0).getNodeID()));
								break;
							}
							case "targetRef":{
								attr.setAttributeValue("_"+elementID);
							}
						}
					}
					
					if (el.getElementName().equals(BPMNElementTagName.subProcess.name())){
						el.setSubElements(createProcessStartEvent(Gsub));
						el.setSubElements(createBPMNEndEvent(Gsub, null));
					}
					if (el.getElementName().equals(BPMNElementTagName.textAnnotation.name())) {
						System.out.println("textAnnotation");
						BPMNElement text = new BPMNElement("text");
						text.setText(pattern.getElementName());
						el.setSubElement(text);
					}
					bpmnElements.add(el);
				}
			}
		//System.out.println(graphElements);
		return bpmnElements;
	}
	
	public static BPMNElement createInputSpecification (ETLFlowGraph G, ETLFlowOperation node, String stringID, BPMNElement el){
		ArrayList<ETLFlowOperation> sourceNodes = utilities.XLMParser.getSourceNodesGivenTarget(G, node);
		Integer dataStoreID = 0;
		String ioSpecificationText= "";
		for (ETLFlowOperation sourceNode: sourceNodes){
			if (sourceNode.getNodeKind().equals(ETLNodeKind.Datastore)){
				dataStoreID = sourceNode.getNodeID();
				if (dataStoreID == 1) dataStoreID = 1111;
		//----------------------------------------------------------
		BPMNElement ioSpecification = new BPMNElement(BPMNElementTagName.ioSpecification.name());
		BPMNElement dataInput = new BPMNElement(BPMNElementTagName.dataInput.name());
		//Integer nodeID = node.getNodeID();
		//if (nodeID == 1) nodeID = 1111;
		if (stringID.equals("01")) stringID = "01111";
		if (stringID.equals("1")) stringID = "1111";
			String attr1Value = "Din_"+stringID+"_"+dataStoreID;
			BPMNAttribute attr1 = new BPMNAttribute("id", attr1Value);
			BPMNAttribute attr2 = new BPMNAttribute("isCollection", "false");
		dataInput.setAttribute(attr1);
		dataInput.setAttribute(attr2);
		ioSpecificationText = '\n'+"<"+dataInput.getElementName()+ " "+ attr1.name + "=\"" + attr1.value + "\"" + " "+ attr2.name + "=\"" + attr2.value + "\"" + "/>"+ '\n';
		BPMNElement inputSet = new BPMNElement(BPMNElementTagName.inputSet.name());
		BPMNElement dataInputRefs = new BPMNElement(BPMNElementTagName.dataInputRefs.name());
			dataInputRefs.setText(attr1Value);
		inputSet.setSubElement(dataInputRefs);
		ioSpecificationText += "<"+inputSet.getElementName()+">"+'\n'+"<"+dataInputRefs.getElementName()+">"+attr1.value+
				"</"+dataInputRefs.getElementName()+">"+'\n'+
				"</"+inputSet.getElementName()+">"+'\n';
		BPMNElement outputSet = new BPMNElement(BPMNElementTagName.outputSet.name());
		ioSpecificationText += "<"+outputSet.getElementName()+"/>"+'\n';;
		ioSpecification.setText(ioSpecificationText);
		el.setSubElement(ioSpecification);
		//----------------------------------------------------------
		BPMNElement dataInputAssociation = new BPMNElement(BPMNElementTagName.dataInputAssociation.name());
		String idValue = "_"+stringID+"-_"+dataStoreID;
		BPMNAttribute idAttr = new BPMNAttribute("id", idValue);
		dataInputAssociation.setAttribute(idAttr);
		String inputAssociationText = '\n'+"<sourceRef>"+"_"+dataStoreID+"</sourceRef>"+'\n'+
				"<targetRef>"+attr1Value+"</targetRef>"+'\n';
		dataInputAssociation.setText(inputAssociationText);
		el.setSubElement(dataInputAssociation);
		}
	}
		return el;
	}
	
	public static BPMNElement createOutputSpecification (ETLFlowGraph G, ETLFlowOperation node, String stringID, BPMNElement el){
		ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
		Integer dataStoreID = 0;
		String ioSpecificationText="";
		String idValue="";
		/*Integer nodeID = node.getNodeID();
		if (nodeID == 1) nodeID = 1111;*/
		if (stringID.equals("01")) stringID = "01111";
		if (stringID.equals("1")) stringID = "1111";
		for (ETLFlowOperation targetNode: targetNodes){
			if (targetNode.getNodeKind().equals(ETLNodeKind.Datastore)){
				dataStoreID = targetNode.getNodeID();
				if (dataStoreID == 1) dataStoreID = 1111;
		BPMNElement ioSpecification = new BPMNElement(BPMNElementTagName.ioSpecification.name());
		BPMNElement dataOutput = new BPMNElement(BPMNElementTagName.dataOutput.name());
			/*if ((node.getOperationType().getOpTypeName().equals(OperationTypeName.Splitter)||
					node.getOperationType().getOpTypeName().equals(OperationTypeName.Router))
					&& el.getElementName().equals(BPMNElementTagName.task.name())){*/
				//idValue = "Dout_0"+nodeID+"_"+dataStoreID;
			idValue = "Dout_"+stringID+"_"+dataStoreID;
			BPMNAttribute attr1 = new BPMNAttribute("id", idValue);
			BPMNAttribute attr2 = new BPMNAttribute("isCollection", "false");
		dataOutput.setAttribute(attr1);
		dataOutput.setAttribute(attr2);
		ioSpecificationText = '\n'+"<"+dataOutput.getElementName()+ " "+ attr1.name + "=\"" + attr1.value + "\"" + " "+ attr2.name + "=\"" + attr2.value + "\"" + "/>"+ '\n';
		BPMNElement inputSet = new BPMNElement(BPMNElementTagName.inputSet.name());
		ioSpecificationText += "<"+inputSet.getElementName()+"/>"+'\n';
		BPMNElement outputSet = new BPMNElement(BPMNElementTagName.outputSet.name());
		BPMNElement dataOutputRefs = new BPMNElement(BPMNElementTagName.dataOutputRefs.name());
			dataOutputRefs.setText(idValue);
		outputSet.setSubElement(dataOutputRefs);
		ioSpecificationText += "<"+outputSet.getElementName()+">"+'\n'+"<"+dataOutputRefs.getElementName()+">"+attr1.value+
				"</"+dataOutputRefs.getElementName()+">"+'\n'+
				"</"+outputSet.getElementName()+">"+'\n';
		ioSpecification.setText(ioSpecificationText);
		el.setSubElement(ioSpecification);
		//----------------------------------------------------------
		BPMNElement dataOutputAssociation = new BPMNElement(BPMNElementTagName.dataOutputAssociation.name());
		String attr1Value = "";
		/*if ((node.getOperationType().getOpTypeName().equals(OperationTypeName.Splitter)||
				node.getOperationType().getOpTypeName().equals(OperationTypeName.Router))
				&& el.getElementName().equals(BPMNElementTagName.task.name())){
		attr1Value = "_0"+stringID+"-_"+dataStoreID;
		} else */  
		attr1Value = "_"+stringID+"-_"+dataStoreID;
		BPMNAttribute idAttr = new BPMNAttribute("id", attr1Value);
		dataOutputAssociation.setAttribute(idAttr);
		String outputAssociationText = '\n'+"<sourceRef>"+idValue+"</sourceRef>"+'\n'+
				"<targetRef>"+"_"+dataStoreID+"</targetRef>"+'\n';
		dataOutputAssociation.setText(outputAssociationText);
		el.setSubElement(dataOutputAssociation);
			}
		}
		return el;
	}
	//for now, leave alone, but then need to check that if the pattern eats up something, edges need to be removed/added
	public static ArrayList<BPMNElement> getBPMNElementsEdge(ETLFlowGraph G){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> dictionaryEdgeElement = JSONDictionaryParser.parseBPMNForEdges(Demo.dictionaryFilePath);
		ArrayList<BPMNElement> outputElements = new ArrayList<BPMNElement>();
		
		for (Object e : G.edgeSet()) {
		ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
		ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
		
		if (!opS.getNodeKind().equals(ETLNodeKind.Datastore) && !opT.getNodeKind().equals(ETLNodeKind.Datastore)){
		BPMNElement el = dictionaryEdgeElement.get(0);
		BPMNElement bpmnElement = new BPMNElement(el.getElementName());
		
			for (BPMNAttribute attr : el.getAttributes()) {
				String optypeName = opS.getOperationType().getOpTypeName().name();
				if (attr.getAttributeName().equals("sourceRef")) {
					if ((optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)) || 
							((optypeName.equals(OperationTypeName.Router) || optypeName.equals(OperationTypeName.Splitter)) &&
									opT.getNodeKind().equals(ETLNodeKind.Datastore))){
						//to match the id of the newly inserted task after the parallel gateway
						attr.setAttributeValue("_0"+ String.valueOf(opS.getNodeID()));
					} else {
						attr.setAttributeValue("_"+ String.valueOf(opS.getNodeID()));
						if (attr.value.equals("_1")) attr.setAttributeValue("_1111");
					}
					
				} else if (attr.getAttributeName().equals("targetRef")) {
					if (opT.getNodeID() == 1)
						attr.setAttributeValue("_111"+ String.valueOf(opT.getNodeID()));
					else attr.setAttributeValue("_"+ String.valueOf(opT.getNodeID()));
	
				} else if (attr.getAttributeName().equals("id")) {
					if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
						if (opT.getNodeID() == 1)
							attr.setAttributeValue("_0"+ String.valueOf(opS.getNodeID()) + "-_111"+ String.valueOf(opT.getNodeID()));	
						else 
							attr.setAttributeValue("_0"+ String.valueOf(opS.getNodeID()) + "-_"+ String.valueOf(opT.getNodeID()));	
					} else {
						if (opT.getNodeID() == 1)
							attr.setAttributeValue("_"+ String.valueOf(opS.getNodeID()) + "-_111"+ String.valueOf(opT.getNodeID()));
						else if (opS.getNodeID() == 1)
							attr.setAttributeValue("_111"+ String.valueOf(opS.getNodeID()) + "-_"+ String.valueOf(opT.getNodeID()));
						else 
							attr.setAttributeValue("_"+ String.valueOf(opS.getNodeID()) + "-_"+ String.valueOf(opT.getNodeID()));
								//System.out.println(attr.name + " " + attr.value);
					}
				}
				BPMNAttribute bpmnAttr = new BPMNAttribute(attr.name, attr.value);
				bpmnElement.setAttribute(bpmnAttr);
			}
			if (opS.getOperationType().getOpTypeName().equals(OperationTypeName.Router)){
				System.out.println("opS is Router");
				ETLFlowOperation filter = new ETLFlowOperation();
				String conditionTrueTarget = "";
				ArrayList<ETLFlowOperation> sourceNodes = XLMParser.getSourceNodesGivenTarget(G, opS);
				System.out.println(sourceNodes);
				for (ETLFlowOperation sourceNode: sourceNodes){
					if (sourceNode.getOperationType().getOpTypeName().name().equals("Filter")){
						System.out.println("source of Router is Filter "+sourceNode.getNodeID());
						filter = sourceNode;
							for(ETLNonFunctionalCharacteristic chr: sourceNode.getoProperties().get("send_true_to")){
								conditionTrueTarget = chr.getRightOp();
								System.out.println("send true to target is : "+ conditionTrueTarget);
							}
					}
				}
				String opTName = opT.getOperationName();
				if (opTName.contains(".")){
					opTName = opTName.substring(opTName.lastIndexOf(".") + 1);
				}
				System.out.println("target Operation name: "+opTName);
				if (opTName.equals(conditionTrueTarget)){
					System.out.println("opt equals target operation");
				//BPMNElement sub = new BPMNElement(BPMNElementTagName.conditionExpression.name());
				bpmnElement.setText("<conditionExpression><![CDATA["+filter.getSemanticsExpressionTrees().toString().substring(1, filter.getSemanticsExpressionTrees().toString().length()-1)+"]]></conditionExpression>");
				}
			}
			bpmnElement.setID(opS+"-"+opT);
			outputElements.add(bpmnElement);
		}
		}
		return outputElements;
	}
	
	public static BPMNElement createDataStoreElement(){
		BPMNElement dataStoreElement = new BPMNElement(BPMNElementTagName.dataStore.name());
		BPMNAttribute attr1 = new BPMNAttribute("id", "DS_1");
		BPMNAttribute attr2 = new BPMNAttribute("isUnlimited", "false");
		dataStoreElement.setAttribute(attr1);
		dataStoreElement.setAttribute(attr2);
		
		return dataStoreElement;
	}
	
	public static ArrayList<BPMNElement> getPoolElements(
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<BPMNElement> poolElements = new ArrayList<BPMNElement>();	
		BPMNElement collaborationElement = new BPMNElement(BPMNElementTagName.collaboration.name());
		BPMNAttribute collAttr1 = new BPMNAttribute("id", "COLLABORATION_1");
		BPMNAttribute collAttr2 = new BPMNAttribute("isClosed", "false");
		collaborationElement.setAttribute(collAttr1);
		collaborationElement.setAttribute(collAttr2);
		poolElements.add(collaborationElement);
		
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
		int counter = 2;
		for (Integer i : flowIDs) {
			for (String str : engineTypes) {
				if (uniquePools.contains(str + "_"+ i)){
				//create a process element for each pool
				BPMNElement processElement = new BPMNElement(BPMNElementTagName.process.name());
				BPMNAttribute processAttr1 = new BPMNAttribute("id", "PROCESS_"+counter);
				BPMNAttribute processAttr2 = new BPMNAttribute("isExecutable", "false");
				BPMNAttribute processAttr3 = new BPMNAttribute("processType", "None");
				BPMNAttribute processAttr4 = new BPMNAttribute("name", str+"_"+i );
				ArrayList<BPMNAttribute> processAttributes = new ArrayList<BPMNAttribute>();
				processAttributes.addAll(Arrays.asList(processAttr1, processAttr2, processAttr3, processAttr4));
				processElement.setAttributes(processAttributes);
				//create a participant element for each pool
				BPMNElement participantElement = new BPMNElement(BPMNElementTagName.participant.name());
				BPMNAttribute partAttr1 = new BPMNAttribute("id", "_"+counter);
				BPMNAttribute partAttr2 = new BPMNAttribute("name", str + "_" + i);
				BPMNAttribute partAttr3 = new BPMNAttribute("processRef", "PROCESS_"+counter);
				ArrayList<BPMNAttribute> participantAttributes = new ArrayList<BPMNAttribute>();
				participantAttributes.addAll(Arrays.asList(partAttr1, partAttr2, partAttr3));
				participantElement.setAttributes(participantAttributes);
				collaborationElement.setSubElement(participantElement);
				counter = counter +1;
				poolElements.add(processElement);
			}
			}
		}
		//System.out.println("engineTypes " + engineTypes);
		return poolElements;

	}
	
	public static ArrayList<HashMap> getPoolElementsVelocityFormat(ArrayList<BPMNElement> poolElements){
		ArrayList<HashMap> collaborationElements = new ArrayList<HashMap>();
		String stringAttributes ="";
		for (BPMNElement el: poolElements){
			if (!el.getElementName().equals(BPMNElementTagName.process.name())){
			HashMap collaborationElement = new HashMap();
			for (BPMNAttribute attr : el.getAttributes()) {
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
			collaborationElement.put("attributes", stringAttributes);
			stringAttributes = "";
			// System.out.println(optypeMapping.get(str).getElementName());
			collaborationElement.put("name", el.getElementName());
			collaborationElements.add(collaborationElement);
		}
		}
		return collaborationElements;
	}
	
	public static ArrayList<HashMap> getPoolSubElementsVelocityFormat(ArrayList<BPMNElement> poolElements){
		ArrayList<HashMap> collaborationSubElements = new ArrayList<HashMap>();
		String stringAttributes ="";
		for (BPMNElement el: poolElements){
			if (!el.getElementName().equals(BPMNElementTagName.process.name())){
			for (BPMNElement subEl: el.getSubElements()){
				HashMap collaborationSubElement = new HashMap();
				collaborationSubElement.put("name", subEl.getElementName());
				collaborationSubElement.put("text", subEl.getElementText());
				for (BPMNAttribute attr : subEl.getAttributes()) {
					stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
			}
				collaborationSubElement.put("attributes", stringAttributes);		
				stringAttributes = "";
				collaborationSubElements.add(collaborationSubElement);
				}
		}
		}
		return collaborationSubElements;
	}
	
	public static ArrayList<HashMap> getProcessTagElementsVelocityFormat(ArrayList<BPMNElement> poolElements){
		ArrayList<HashMap> processElements = new ArrayList<HashMap>();
		String stringAttributes ="";
		for (BPMNElement el: poolElements){
			if (el.getElementName().equals(BPMNElementTagName.process.name())){
			HashMap pElement = new HashMap();
			pElement.put("name", el.getElementName());
				for (BPMNAttribute attr : el.getAttributes()) {
				stringAttributes += attr.name + "=\"" + attr.value + "\"" + " ";
				}
				pElement.put("attributes", stringAttributes);		
				stringAttributes = "";
				processElements.add(pElement);
			}
		}
		
		return processElements;
	}
	
	public static BPMNElement createParallelGateway(String gatewayDirectionValue, String nameValue, String idValue){
		Random randomGenerator = new Random();
		String randomID= "_g"+randomGenerator.nextInt(100);
		
		BPMNElement element = new BPMNElement(BPMNElementTagName.parallelGateway.name());
		BPMNAttribute attr1 = new BPMNAttribute("gatewayDirection", gatewayDirectionValue);
		BPMNAttribute attr2 = new BPMNAttribute("id");
		if (idValue == null) attr2.setAttributeValue(randomID);
		else attr2.setAttributeValue(idValue);
		BPMNAttribute attr3 = new BPMNAttribute("name");
		if (nameValue != null) attr3.setAttributeValue(nameValue);
		else {
			if (gatewayDirectionValue.equals("Converging")) attr3.setAttributeValue("AND-Join");
			else if (gatewayDirectionValue.equals("Diverging")) attr3.setAttributeValue("AND-Split");
		}
		element.setAttribute(attr1);
		element.setAttribute(attr2);
		element.setAttribute(attr3);
		return element;
	}
	
	public static BPMNElement createSequenceFlow(String sourceRefValue, String targetRefValue){
		BPMNElement seqElement = new BPMNElement(BPMNElementTagName.sequenceFlow.name());
		BPMNAttribute attr1 = new BPMNAttribute("id", sourceRefValue+"-"+targetRefValue);
		BPMNAttribute attr2 = new BPMNAttribute("sourceRef", sourceRefValue);
		BPMNAttribute attr3 = new BPMNAttribute("targetRef", targetRefValue);
		seqElement.setAttribute(attr1);
		seqElement.setAttribute(attr2);
		seqElement.setAttribute(attr3);
		
		return seqElement;
	}
	
	public static ArrayList<BPMNElement> updateEdgesWhenInsertingSubprocesses (ArrayList<BPMNElement> graphEdges, ETLFlowGraph G, 
			ETLFlowGraph subGraph, Pattern pattern, BPMNElement subprocess){
		ArrayList<Integer> subGraphSourceNodes = subGraph.getAllSourceNodes();
		ArrayList<Integer> subGraphTargetNodes = subGraph.getAllTargetNodes();
		Hashtable<Integer, ETLFlowOperation> subOps = subGraph.getEtlFlowOperations();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> patternSubgraph = pattern.getPatternSubgraph();
		//check here whether there are multiple incoming edges and insert a parallel gateway.
		/*BPMNElement join = createParallelGateway("Converging", null, null);
		BPMNElement split = createParallelGateway("Diverging", null, null);
		if (subGraphSourceNodes.size() > 1) graphEdges.add(split);
		if (subGraphTargetNodes.size() > 1) graphEdges.add(join);*/
		
		for (Object e: G.edgeSet()){
			Integer sourceID = (Integer)((ETLEdge) e).getSource();
			Integer targetID = (Integer)((ETLEdge) e).getTarget();	
			//the whole edge is contained in subprocess
			if (subOps.containsKey(sourceID) && subOps.containsKey(targetID)){
				Iterator<BPMNElement> itr = graphEdges.iterator();
				while (itr.hasNext()){
					BPMNElement el = itr.next();
					for (BPMNAttribute attr: el.getAttributes()){
						if (sourceID == 1 ) sourceID = 1111;
						if (targetID == 1) targetID = 1111;
						if (attr.name.equals("id") && attr.value.equals("_"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID))){
							//subprocess.setSubElement(el);
							itr.remove();
							break;
						} if (attr.name.equals("id") && attr.value.equals("_0"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID))){
							//subprocess.setSubElement(el);
							itr.remove();
							break;
						}
					}	
				}
			}
			
			//the target is in the subprocess
			if(subGraphSourceNodes.contains(targetID) && ops.get(sourceID).getNodeKind().equals(ETLNodeKind.Operator)){
				System.out.println("subgraph nodes contain targetID. Need to change edge targetRef.");
				if (targetID == 1) targetID = 1111;
				if (sourceID == 1) sourceID = 1111;
				
				Iterator<BPMNElement> itr = graphEdges.iterator();
				while (itr.hasNext()){
					BPMNElement el = itr.next();
					for (BPMNAttribute attr: el.getAttributes()){
						if (attr.getAttributeName().equals("targetRef") &&
							attr.getAttributeValue().equals("_"+String.valueOf(targetID))){
								attr.setAttributeValue("_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID());
								System.out.println("new edge targetRef: "+ attr.value);
							}
						if (attr.getAttributeName().equals("id")){
							if (attr.getAttributeValue().equals("_"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID))){
								attr.setAttributeValue("_"+String.valueOf(sourceID)+"-_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID());
							} else if (attr.getAttributeValue().equals("_0"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID))){
								attr.setAttributeValue("_0"+String.valueOf(sourceID)+"-_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID());
						}
					}
				}
			}
			}
			
			//the source is in the subprocess
			if (subGraphTargetNodes.contains(sourceID)){
				if (ops.get(sourceID).getOperationType().getOpTypeName().equals(OperationTypeName.Join)||
						ops.get(sourceID).getOperationType().getOpTypeName().equals(OperationTypeName.LeftOuterJoin)){
				}
				if (sourceID == 1) sourceID = 1111;
				if (targetID == 1) targetID = 1111;
				Iterator<BPMNElement> itr = graphEdges.iterator();
				while (itr.hasNext()){
					BPMNElement el = itr.next();
					for (BPMNAttribute attr: el.getAttributes()){
						if (attr.getAttributeName().equals("sourceRef") &&
							attr.getAttributeValue().equals("_"+String.valueOf(sourceID))){
						attr.setAttributeValue("_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID());
						}
						if (attr.getAttributeName().equals("id") && 
								((attr.getAttributeValue().equals("_"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID))) || 
								attr.getAttributeValue().equals("_0"+String.valueOf(sourceID)+"-_"+String.valueOf(targetID)))){
									attr.setAttributeValue("_"+pattern.getElementID()+"-"+patternSubgraph.get(0).getNodeID()+"-_"+String.valueOf(targetID));
						} 
					}
					}
				}
			}
		return graphEdges;
	}
	
	/*public static ArrayList<String> getMainProcessSourceNodes (ArrayList<Integer> startEventAndEdges, 
			ETLFlowGraph G, ETLFlowGraph subGraph, Pattern pattern){
		ArrayList<String> outputSourceNodes = new ArrayList<>();
		ArrayList<Integer> graphSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> subGraphSourceNodes = subGraph.getAllSourceNodes();
		Integer counter = 0;
		
		// all subgraph source nodes are contained in graph source nodes -->
		// create a sequence flow pointing from start event to the subprocess.
		for (Integer i: graphSourceNodes){
			if (subGraphSourceNodes.contains(i)) {
				counter++;
			} else {
				outputSourceNodes.add("_"+i);
			}
		}
		if (counter == subGraphSourceNodes.size()){
			outputSourceNodes.add("_"+pattern.getElementID()+"-"+pattern.getPatternSubgraph().get(0).getNodeID());
		}
		
		return outputSourceNodes;
	}*/

}