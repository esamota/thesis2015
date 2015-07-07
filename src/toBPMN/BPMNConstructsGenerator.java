package toBPMN;
import utilities.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import org.apache.xerces.parsers.XMLParser;

import operationDictionary.OperationTypeName;
import patternDiscovery.PatternDiscovery;
import patternDiscovery.Pattern;
import utilities.BPMNElementTagName;
import utilities.JSONDictionaryParser;
import utilities.XLMParser;
import etlFlowGraph.attribute.Attribute;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;
import etlFlowGraph.operation.ETLNodeKind;


public class BPMNConstructsGenerator {
	
	public static void main(String[] args) {
		/*ETLFlowGraph G = XLMParser.getXLMGraph();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> subgraph = new ArrayList<>();*/
		/*subgraph.add(ops.get(34));
		Pattern pattern = JSONDictionaryParser.getAnyPatternElementByName("Join");
		pattern.setPatternSubgraph(subgraph);
		ArrayList<BPMNElement> sortElements = getPatternBPMNElements(pattern);
		for (BPMNElement el: sortElements){
			for (BPMNAttribute attr: el.getAttributes()){
				System.out.println("element "+ el.getElementName()+", attribute name: "+ attr.getAttributeName()+" , attribte values: "+attr.getAttributeValue());
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
		
		/*ArrayList<BPMNElement> edges = getBPMNElementsEdge(G);
		for (BPMNElement el: edges){
			System.out.println("1. name "+ el.getElementName());
			for (BPMNAttribute attr: el.getAttributes()){
				System.out.println(attr.name +" "+attr.value);
			}
		}*/
	}
	
	public static void getGraphElementsPerParticipant(){
		//for each participant, or unique pool, add all tasks that belong there as subelements of the process element
		//then return the process elements to the printing method
	}
		
	//this works for the process, for subprocesses, need different attribute values.
	public static ArrayList<BPMNElement> createProcessStartEvent(ETLFlowGraph G){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> startEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement startEvent = new BPMNElement(BPMNElementTagName.startEvent.name());
		Random randomGenerator = new Random();
		String randomID= "_00"+randomGenerator.nextInt(100);
		BPMNAttribute id = new BPMNAttribute("id", randomID);
		BPMNAttribute name= new BPMNAttribute("name", "StartProcess");
		startEvent.setAttribute(id);
		startEvent.setAttribute(name);
		startEventAndEdges.add(startEvent);
		
		ArrayList<Integer> allSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> targetOfSourceNodes = new ArrayList<Integer>();
		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		//loop through all source nodes of a given graph
		for (Integer i : allSourceNodes) {
			//if the node is a datastore connect start event to their target unless it is a join
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				for (Object e : G.edgeSet()) {
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							//why this condition??? where is targetOfSourceNodes getting populated???
							//&& !targetOfSourceNodes.contains(targetId)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.Join)
							&& !ops.get(targetId).getOperationType()
									.getOpTypeName()
									.equals(OperationTypeName.LeftOuterJoin)) {
						sourceNodes.add(targetId);
					}
				}
				// if not a datastore, connect the start event to the node itself
			} else {
				sourceNodes.add(i);
			}
		}
		// generate sequence flows from start event to all source nodes
		for (Integer i: sourceNodes){
			if (i == 1) i = 1111;
			BPMNElement seqFlow = new BPMNElement(BPMNElementTagName.sequenceFlow.name());
			BPMNAttribute sourceRef = new BPMNAttribute("sourceRef", randomID);
			BPMNAttribute targetRef = new BPMNAttribute("targetRef", "_"+String.valueOf(i));
			BPMNAttribute flowId = new BPMNAttribute("id", randomID+"-_"+String.valueOf(i));
			seqFlow.setAttribute(sourceRef);
			seqFlow.setAttribute(targetRef);
			seqFlow.setAttribute(flowId);
			startEventAndEdges.add(seqFlow);
		}
		return startEventAndEdges;
	}
	
	public static ArrayList<BPMNElement> createBPMNEndEvent(ETLFlowGraph G){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> endEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement endEvent = new BPMNElement(BPMNElementTagName.endEvent.name());
		Random randomGenerator = new Random();
		String randomID= "_00"+randomGenerator.nextInt(100);
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
				//System.out.println("all target nodes: " + i);
				for (Object e : G.edgeSet()) {
				// is there a simpler way to do this????
				Integer sourceId = (Integer) ((ETLEdge) e).getSource();
				Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (targetId.intValue() == i.intValue() && !sourceOfTargetNodes.contains(sourceId)) {
						targetNodes.add(sourceId);
					}
				}
			} else targetNodes.add(i);
			}
		// generate sequence flows from all target nodes to the end event
		for (Integer i: targetNodes){
			if (i == 1) i = 1111;
			BPMNElement seqFlow = new BPMNElement(BPMNElementTagName.sequenceFlow.name());
			BPMNAttribute sourceRef = new BPMNAttribute("sourceRef", "_"+String.valueOf(i));
			BPMNAttribute targetRef = new BPMNAttribute("targetRef", randomID);
			BPMNAttribute flowId = new BPMNAttribute("id", "_"+String.valueOf(i)+"-"+randomID);
			seqFlow.setAttribute(sourceRef);
			seqFlow.setAttribute(targetRef);
			seqFlow.setAttribute(flowId);
			endEventAndEdges.add(seqFlow);
		}
	return endEventAndEdges;

	}
	public static BPMNElement createBPMNSubprocess(Pattern pattern, boolean isForCompensationValue, boolean triggeredByEventValue){
		BPMNElement subprocess = new BPMNElement(pattern.getElementName()+"_"+"Pattern");
		//attributes ---------------------------------------------------------------------
		ArrayList<BPMNAttribute> subprocessAttributes = new ArrayList<BPMNAttribute>();
		BPMNAttribute completionQuantity = new BPMNAttribute("completionQuantity", "1");
		BPMNAttribute id = new BPMNAttribute("id", pattern.getElementID());
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
		ETLFlowOperation node = patternSubgraph.get(0);
		String randomID="_0"+String.valueOf(node.getNodeID());
		ArrayList<Integer> nodesWithInput = XLMParser.nodesWithDataInput(G);
		ArrayList<Integer> nodesWithOutput = XLMParser.nodesWithDataOutput(G);
		
		//single node patterns
		//*****************************************************************************************	
		if (patternSubgraph.size() == 1){
		for(BPMNElement el: patternBPMNElements){
			el.setID(pattern.getElementID());
			//if (el.getElementText().equals("$condition"))
			//	el.addText("function for node's condition?");
			if (nodesWithInput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.task.name())){
				el = createInputSpecification(G, node, el);
			} 
			if (nodesWithOutput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.task.name())){
				el = createOutputSpecification(G, node, el);
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
							if (attr.getAttributeValue().equals("$graph") && !el.getElementName().equals("sequenceFlow")){
								if (node.getNodeID() == 1) attr.setAttributeValue("_111"+ String.valueOf(node.getNodeID()));
								else attr.setAttributeValue("_"+ String.valueOf(node.getNodeID()));
								break;
							} else if (attr.getAttributeValue().equals("$create")){
								attr.setAttributeValue(randomID);
								break;
							} else if (el.getElementName().equals("sequenceFlow")){
								if (node.getNodeID() == 1) attr.setAttributeValue("_111"+String.valueOf(node.getNodeID())+"-"+randomID);
								else attr.setAttributeValue("_"+String.valueOf(node.getNodeID())+"-"+randomID);
								break;
							}
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
			} else if (patternSubgraph.size() > 1){
				for(BPMNElement el: patternBPMNElements){
					el.setID(pattern.getElementID());
					for (ETLFlowOperation subgraphNode: patternSubgraph){
						if (nodesWithInput.contains(subgraphNode.getNodeID()) && el.getElementName().equals(BPMNElementTagName.subprocess.name())){
							el = createInputSpecification(G, subgraphNode, el);
						}
						if (nodesWithOutput.contains(node.getNodeID()) && el.getElementName().equals(BPMNElementTagName.subprocess.name())){
							el = createOutputSpecification(G, subgraphNode, el);
						}
						
					}
					if (patternBPMNElements.size() > 1 && el.getElementName().equals(BPMNElementTagName.subprocess.name())){
						el.setSubElement(patternBPMNElements.get(1));
					}
					for (BPMNAttribute attr : el.getAttributes()) {	
						switch(attr.getAttributeName()){
							case "name": {
								attr.setAttributeValue(pattern.getElementName());
							}
							break;
							case "id": {
								attr.setAttributeValue(pattern.getElementID());
							}
							break;
						}
					}
					bpmnElements.add(el);
				}
			}
		//System.out.println(graphElements);
		return bpmnElements;
	}
	
	public static BPMNElement createInputSpecification (ETLFlowGraph G, ETLFlowOperation node, BPMNElement el){
		ArrayList<ETLFlowOperation> sourceNodes = utilities.XLMParser.getSourceNodesGivenTarget(G, node);
		Integer dataStoreID = 0;
		String ioSpecificationText= "";
		for (ETLFlowOperation sourceNode: sourceNodes){
			if (sourceNode.getNodeKind().equals(ETLNodeKind.Datastore)){
				dataStoreID = sourceNode.getNodeID();
				if (dataStoreID == 1) dataStoreID = 1111;
				break;
			}
		}
		//----------------------------------------------------------
		BPMNElement ioSpecification = new BPMNElement(BPMNElementTagName.ioSpecification.name());
		BPMNElement dataInput = new BPMNElement(BPMNElementTagName.dataInput.name());
		Integer nodeID = node.getNodeID();
		if (nodeID == 1) nodeID = 1111;
			String attr1Value = "Din_"+nodeID+"_"+dataStoreID;
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
		String idValue = "_"+nodeID+"-_"+dataStoreID;
		BPMNAttribute idAttr = new BPMNAttribute("id", idValue);
		dataInputAssociation.setAttribute(idAttr);
		String inputAssociationText = '\n'+"<sourceRef>"+"_"+dataStoreID+"</sourceRef>"+'\n'+
				"<targetRef>"+attr1Value+"</targetRef>"+'\n';
		dataInputAssociation.setText(inputAssociationText);
		el.setSubElement(dataInputAssociation);
		return el;
	}
	
	public static BPMNElement createOutputSpecification (ETLFlowGraph G, ETLFlowOperation node, BPMNElement el){
		ArrayList<ETLFlowOperation> targetNodes = utilities.XLMParser.getTargetOperationsGivenSource(node, G);
		Integer dataStoreID = 0;
		String ioSpecificationText="";
		Integer nodeID = node.getNodeID();
		if (nodeID == 1) nodeID = 1111;
		for (ETLFlowOperation targetNode: targetNodes){
			if (targetNode.getNodeKind().equals(ETLNodeKind.Datastore)){
				dataStoreID = targetNode.getNodeID();
				if (dataStoreID == 1) dataStoreID = 1111;
				break;
			}
		}
		BPMNElement ioSpecification = new BPMNElement(BPMNElementTagName.ioSpecification.name());
		BPMNElement dataOutput = new BPMNElement(BPMNElementTagName.dataOutput.name());
			String idValue = "Dout_"+nodeID+"_"+dataStoreID;
			BPMNAttribute attr1 = new BPMNAttribute("id", idValue);
			BPMNAttribute attr2 = new BPMNAttribute("isCollection", "false");
		dataOutput.setAttribute(attr1);
		dataOutput.setAttribute(attr2);
		ioSpecificationText = '\n'+"<"+dataOutput.getElementName()+ " "+ attr1.name + "=\"" + attr1.value + "\"" + " "+ attr2.name + "=\"" + attr2.value + "\"" + "/>"+ '\n';
		BPMNElement outputSet = new BPMNElement(BPMNElementTagName.outputSet.name());
		BPMNElement dataOutputRefs = new BPMNElement(BPMNElementTagName.dataOutputRefs.name());
			dataOutputRefs.setText(idValue);
		outputSet.setSubElement(dataOutputRefs);
		ioSpecificationText += "<"+outputSet.getElementName()+">"+'\n'+"<"+dataOutputRefs.getElementName()+">"+attr1.value+
				"</"+dataOutputRefs.getElementName()+">"+'\n'+
				"</"+outputSet.getElementName()+">"+'\n';
		BPMNElement inputSet = new BPMNElement(BPMNElementTagName.inputSet.name());
		ioSpecificationText += "<"+inputSet.getElementName()+"/>"+'\n';
		
		ioSpecification.setText(ioSpecificationText);
		el.setSubElement(ioSpecification);
		//----------------------------------------------------------
		BPMNElement dataOutputAssociation = new BPMNElement(BPMNElementTagName.dataOutputAssociation.name());
		String attr1Value = "_"+nodeID+"-_"+dataStoreID;
		BPMNAttribute idAttr = new BPMNAttribute("id", attr1Value);
		dataOutputAssociation.setAttribute(idAttr);
		String outputAssociationText = '\n'+"<sourceRef>"+idValue+"</sourceRef>"+'\n'+
				"<targetRef>"+"_"+dataStoreID+"</targetRef>"+'\n';
		dataOutputAssociation.setText(outputAssociationText);
		el.setSubElement(dataOutputAssociation);
	
		return el;
	}
	//for now, leave alone, but then need to check that if the pattern eats up something, edges need to be removed/added
	public static ArrayList<BPMNElement> getBPMNElementsEdge(ETLFlowGraph G){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<BPMNElement> dictionaryEdgeElement = JSONDictionaryParser.parseBPMNForEdges();
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
					if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
						//to match the id of the newly inserted task after the parallel gateway
						attr.setAttributeValue("_0"+ String.valueOf(opS.getNodeID()));
					} else attr.setAttributeValue("_"+ String.valueOf(opS.getNodeID()));
					
				} else if (attr.getAttributeName().equals("targetRef")) {
					if (opT.getNodeID() == 1)
						attr.setAttributeValue("_1111"+ String.valueOf(opT.getNodeID()));
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
				BPMNElement sub = new BPMNElement(BPMNElementTagName.conditionExpression.name());
				sub.setText("<![CDATA["+opS.getSemanticsExpressionTrees().toString()+"]]>");
				bpmnElement.setSubElement(sub);
			}
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

}