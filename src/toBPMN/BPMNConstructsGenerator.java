package toBPMN;
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
		ETLFlowGraph G = XLMParser.getXLMGraph();
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		HashMap<String, ArrayList<BPMNElement>> mapping = JSONDictionaryParser.getSingleOperationPatterns();
		HashMap<String, ArrayList<String>> flagMapping = JSONDictionaryParser.getOperatorPatternFlags();
		
		ArrayList<BPMNElement> graphBPMNElements = secondPass(G, ops, mapping);
		/*for(BPMNElement el: graphBPMNElements){
			for (BPMNAttribute attr: el.getAttributes()){
			System.out.println(el.getElementName());
			}
		}*/
		
		/*This is to check what bpmn element is created for each node, no patterns
		 * for (Integer i: ops.keySet()){
			ArrayList<BPMNElement> elements = getBPMNElementsOfANode(G, ops, ops.get(i).getNodeID(), mapping);
			System.out.println("---------------------");
			for (BPMNElement el: elements){
				System.out.println(ops.get(i).getNodeID()+ " "+ops.get(i).getOperationName()+" "+el.getElementName());
			}
			System.out.println("---------------------");
		}*/
		
	}
	
	public static void getGraphElementsPerParticipant(){
		//for each participant, or unique pool, add all tasks that belong there as subelements of the process element
		//then return the process elements to the printing method
	}
	
	public static ArrayList<BPMNElement> secondPass(ETLFlowGraph G,
			Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping) {
		ArrayList<Pattern> patternLinksPerNode = new ArrayList<Pattern>();
		ArrayList<BPMNElement> graphBPMNElements = new ArrayList<BPMNElement>();
		ArrayList<Integer> visitedNodes = new ArrayList<Integer>();
		Iterator<Integer> graphIter = G.iterator();
		while (graphIter.hasNext()) {
			Integer v = graphIter.next();
			ETLFlowOperation node = ops.get(v);
			System.out.println("*******************");
			System.out.println("node "+ v+", "+node.getOperationName());
			if (!visitedNodes.contains(v)) {
				System.out.println("node hasn't been visited");
				// get links to patterns for this node
				patternLinksPerNode = PatternDiscovery.getLinksToPatternsForNode(
						G, ops, node.getNodeID());
				if (patternLinksPerNode.size() == 0) {
					System.out.println("doesn't have pattern links");
					ArrayList<BPMNElement> nodeElements = getBPMNElementsOfANode(
							G, ops, v, mapping);
					System.out.println("----");
					for (BPMNElement el : nodeElements) {
						System.out.println(el.getElementName());
						graphBPMNElements.add(el);
					}
					System.out.println("----");
					visitedNodes.add(node.getNodeID());
				}
				if (patternLinksPerNode.size() != 0) {
					// in the future, check is there are overlappings here
					for (Pattern linkedPattern : patternLinksPerNode) {
						System.out.println("----");
						System.out.println("has a subprocess called "+linkedPattern.getPatternName());
						BPMNElement subprocess = createBPMNSubprocess(linkedPattern);
						for (ETLFlowOperation op : linkedPattern
								.getPatternNodes()) {
							ArrayList<BPMNElement> nodeElements = getBPMNElementsOfANode(
									G, ops, op.getNodeID(), mapping);
							for (BPMNElement el : nodeElements) {
								System.out.println("element in subprocess: "+el.getElementName());
								subprocess.addSubElement(el);
								;
							}
							visitedNodes.add(op.getNodeID());
						}
						System.out.println("----");
						graphBPMNElements.add(subprocess);
					}
				}
			}
			else System.out.println("node has been visited");
			System.out.println("*********************");
		}
		return graphBPMNElements;
	}
	
	//this works for the process, for subprocesses, need different attribute values.
	public static ArrayList<BPMNElement> createBPMNStartEvent(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops){
		ArrayList<BPMNElement> startEventAndEdges = new ArrayList<BPMNElement>();
		BPMNElement startEvent = new BPMNElement(BPMNElementTagName.startEvent.name());
		Random randomGenerator = new Random();
		String randomID= "_0000"+randomGenerator.nextInt(100);
		BPMNAttribute id = new BPMNAttribute("id", randomID);
		BPMNAttribute name= new BPMNAttribute("name", "StartProcess");
		startEvent.addAttribute(id);
		startEvent.addAttribute(name);
		startEventAndEdges.add(startEvent);
		//-----------------------------------------------------------------
		//if (type.equals("process")){
		ArrayList<Integer> allSourceNodes = G.getAllSourceNodes();
		ArrayList<Integer> targetOfSourceNodes = new ArrayList<Integer>();
		ArrayList<Integer> sourceNodes = new ArrayList<Integer>();
		for (Integer i : allSourceNodes) {
			if (ops.get(i).getNodeKind().equals(ETLNodeKind.Datastore)) {
				//System.out.println("lala: " + i);
				for (Object e : G.edgeSet()) {
					// is there a simpler way to do this????
					Integer sourceId = (Integer) ((ETLEdge) e).getSource();
					Integer targetId = (Integer) ((ETLEdge) e).getTarget();
					if (sourceId.intValue() == i.intValue()
							//why this condition??? where is targetOfSourceNodes getting populated???
							&& !targetOfSourceNodes.contains(targetId)
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
		for (Integer i: sourceNodes){
			BPMNElement seqFlow = new BPMNElement(BPMNElementTagName.sequenceFlow.name());
			BPMNAttribute sourceRef = new BPMNAttribute("sourceRef", randomID);
			BPMNAttribute targetRef = new BPMNAttribute("targerRef", "_"+String.valueOf(i));
			BPMNAttribute flowId = new BPMNAttribute("id", "_"+randomID+"-_"+String.valueOf(i));
			seqFlow.addAttribute(sourceRef);
			seqFlow.addAttribute(targetRef);
			seqFlow.addAttribute(flowId);
			startEventAndEdges.add(seqFlow);
		}
		return startEventAndEdges;
	}
	
	public static void createBPMNEndEvent(){
		
	}
	public static BPMNElement createBPMNSubprocess(Pattern pattern){
		BPMNElement subprocess = new BPMNElement(pattern.getPatternName()+"_"+"Pattern");
		//attributes ---------------------------------------------------------------------
		ArrayList<BPMNAttribute> subprocessAttributes = new ArrayList<BPMNAttribute>();
		BPMNAttribute completionQuantity = new BPMNAttribute("completionQuantity", "1");
		BPMNAttribute id = new BPMNAttribute("id", pattern.getPatternID());
		BPMNAttribute isForCompensation = new BPMNAttribute("isForCompensation", "false");
		BPMNAttribute name = new BPMNAttribute("name", pattern.getPatternName()+"_"+"Pattern");
		BPMNAttribute startQuantity = new BPMNAttribute("startQuantity", "1");
		BPMNAttribute triggeredByEvent = new BPMNAttribute("triggeredByEvent", "false");
		subprocessAttributes.addAll(Arrays.asList(completionQuantity, id, isForCompensation, name, startQuantity, triggeredByEvent));
		subprocess.addAttributes(subprocessAttributes);
		//-------------------------------------------------------------------------------
		//subelements
		return subprocess;
	}

	public static ArrayList<BPMNElement> getBPMNElementsOfANode(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops, Integer nodeID,  HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> nodeElements = new ArrayList<BPMNElement>();
		ETLFlowOperation node = ops.get(nodeID);
		for (String str : mapping.keySet()) {
			if(str.equals(node.getOperationType().getOpTypeName()
					.toString()) && mapping.get(str).size() == 1){
				nodeElements = getNodeBPMNElementsOneToOne(G, node, mapping);
				}
			else if(str.equals(node.getOperationType().getOpTypeName()
					.toString()) && mapping.get(str).size() > 1){
				nodeElements = getBPMNElementsOneToMany(node, mapping);
			}
			}
		return nodeElements;
	}
	
	//fill in attr values for nodes that correspond to a single bpmn element. save each node -- corresponding bpmn element pair
	public static ArrayList<BPMNElement> getNodeBPMNElementsOneToOne(
			ETLFlowGraph G, ETLFlowOperation node,
			HashMap<String, ArrayList<BPMNElement>> mapping) {
		ArrayList<BPMNElement> singleElements = new ArrayList<BPMNElement>();
		
		//one-to-one mappings
		//*****************************************************************************************	
			for (String str : mapping.keySet()) {
				for(BPMNElement el: mapping.get(str)){
				if (str.equals(node.getOperationType().getOpTypeName()
						.toString()) && mapping.get(str).size() == 1){
					
					for (BPMNAttribute attr : el.getAttributes()) {	
					if (attr.getAttributeValue().equals("")){
						//System.out.println(attr.getAttributeName());
						switch(attr.getAttributeName()){
						case "name":
							attr.setAttributeValue(node.getOperationName());
								break;
						case "id":
							attr.setAttributeValue("_"
									+ String.valueOf(node.getNodeID()));
							break;
					}
				}
					}
					
					singleElements.add(el);
				} 
			}
		}
		
		//System.out.println(graphElements);
		return singleElements;
	}
	
	//fill in attr values for xlm nodes that correspond to multiple bpmn elements. save each node-id -- list of bpmn elements pair.
	public static ArrayList<BPMNElement> getBPMNElementsOneToMany (ETLFlowOperation node,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> complexElements = new ArrayList<BPMNElement>();

			for (String str : mapping.keySet()) {
				String randomID="_0"+String.valueOf(node.getNodeID());
				
				for(BPMNElement el: mapping.get(str)){
					//one-to-many mapping for Join and LeftOuterJoin		
					if (str.equals(node.getOperationType().getOpTypeName()
							.toString()) && mapping.get(str).size()>1){	
						//System.out.println(str);
						
					for (BPMNAttribute attr : el.getAttributes()) {
							
						switch(attr.getAttributeName()){
						case "name":
							if(attr.getAttributeValue().equals("")){
								attr.setAttributeValue(node.getOperationName());
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(str);
								break;
							}
							break;
						case "id":
							if(attr.getAttributeValue().equals("") && !el.getElementName().equals("sequenceFlow")){
								attr.setAttributeValue("_"+ String.valueOf(node.getNodeID()));
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(randomID);
							} else if (el.getElementName().equals("sequenceFlow")){
								//System.out.println("source and target ref from inside the case id "+sourceRef+"-"+targetRef);
								attr.setAttributeValue("_"+String.valueOf(node.getNodeID())+"-"+randomID);
								break;
							}
							break;
						case "sourceRef":
							attr.setAttributeValue("_"+String.valueOf(node.getNodeID()));
							break;
						case "targetRef":
							attr.setAttributeValue(randomID);
							break;
						}
					
						
					}
					complexElements.add(el);
					
			}
			
			}
		}
			//System.out.println(complexElements);
			return complexElements;
	}
	
	//for now, leave alone, but then need to check that if the pattern eats up something, edges need to be removed/added
	public static ArrayList<BPMNElement> getBPMNElementsEdge(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> edgeElements = new ArrayList<BPMNElement>();
		for (String str : mapping.keySet()) {
			if (str.equals("edge")) {
				for (Object e : G.edgeSet()) {
					// adding link
					ETLFlowOperation opS = ops.get(((ETLEdge) e).getSource());
					ETLFlowOperation opT = ops.get(((ETLEdge) e).getTarget());
					for (BPMNElement el: mapping.get(str)){
					BPMNElement bpmnElement = new BPMNElement(el.getElementName());
					for (BPMNAttribute attr : el.getAttributes()) {
						String optypeName = opS.getOperationType().getOpTypeName().name();
						if (attr.getAttributeName().equals("sourceRef")) {
							if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
								//to match the id of the newly inserted task after the parallel gateway
								attr.setAttributeValue("_0"
										+ String.valueOf(opS.getNodeID()));
							}else attr.setAttributeValue("_"
									+ String.valueOf(opS.getNodeID()));

						} else if (attr.getAttributeName().equals("targetRef")) {
							attr.setAttributeValue("_"
									+ String.valueOf(opT.getNodeID()));

						} else if (attr.getAttributeName().equals("id")) {
							if (optypeName.contains("Join") || optypeName.equals(OperationTypeName.Merger)){
								attr.setAttributeValue("_0"
										+ String.valueOf(opS.getNodeID()) + "-_"
										+ String.valueOf(opT.getNodeID()));	
							} else
							attr.setAttributeValue("_"
									+ String.valueOf(opS.getNodeID()) + "-_"
									+ String.valueOf(opT.getNodeID()));
							//System.out.println(attr.name + " " + attr.value);
						}
						BPMNAttribute bpmnAttr = new BPMNAttribute(attr.name, attr.value);
						bpmnElement.addAttribute(bpmnAttr);
						}
					if (opS.getOperationType().getOpTypeName().equals(OperationTypeName.Splitter)){
						BPMNElement sub = new BPMNElement(BPMNElementTagName.conditionExpression.name());
						sub.addText("<![CDATA["+opS.getSemanticsExpressionTrees().toString()+"]]>");
						bpmnElement.addSubElement(sub);
					}
					edgeElements.add(bpmnElement);
				}
			}
		}
		}
		return edgeElements;
	}
	
	public static BPMNElement createDataStoreElement(){
		BPMNElement dataStoreElement = new BPMNElement(BPMNElementTagName.dataStore.name());
		BPMNAttribute attr1 = new BPMNAttribute("id", "DS_1");
		BPMNAttribute attr2 = new BPMNAttribute("isUnlimited", "false");
		dataStoreElement.addAttribute(attr1);
		dataStoreElement.addAttribute(attr2);
		
		return dataStoreElement;
	}
	
	public static ArrayList<BPMNElement> getPoolElements(
			Hashtable<Integer, ETLFlowOperation> ops) {
		ArrayList<BPMNElement> poolElements = new ArrayList<BPMNElement>();	
		BPMNElement collaborationElement = new BPMNElement(BPMNElementTagName.collaboration.name());
		BPMNAttribute collAttr1 = new BPMNAttribute("id", "COLLABORATION_1");
		BPMNAttribute collAttr2 = new BPMNAttribute("isClosed", "false");
		collaborationElement.addAttribute(collAttr1);
		collaborationElement.addAttribute(collAttr2);
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
		int counter = 1;
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
				processElement.addAttributes(processAttributes);
				//create a participant element for each pool
				BPMNElement participantElement = new BPMNElement(BPMNElementTagName.participant.name());
				BPMNAttribute partAttr1 = new BPMNAttribute("id", "_"+counter);
				BPMNAttribute partAttr2 = new BPMNAttribute("name", str + "_" + i);
				BPMNAttribute partAttr3 = new BPMNAttribute("processRef", "PROCESS_"+counter);
				ArrayList<BPMNAttribute> participantAttributes = new ArrayList<BPMNAttribute>();
				participantAttributes.addAll(Arrays.asList(partAttr1, partAttr2, partAttr3));
				participantElement.addAttributes(participantAttributes);
				collaborationElement.addSubElement(participantElement);
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

}