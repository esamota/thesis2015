import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import operationDictionary.OperationTypeName;
import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;


public class BPMNConstructsGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void getGraphElementsPerParticipant(){
		//for each participant, or unique pool, add all tasks that belong there as subelements of the process element
		//then return the process elements to the printing method
		
		
	}
	
	public static ArrayList<BPMNElement> getGraphElements(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> graphElements = new ArrayList<BPMNElement>();
		//call a method that creates a sequence flow element for each edge
	 
		ArrayList<BPMNElement> edgeElements = 
											fillInEdgeAttributeValues(G, ops, mapping);
		ArrayList<BPMNElement> singleElements = 
											fillInAttributeValuesOneToOne(G, ops, mapping);
		ArrayList<BPMNElement> complexElements = 
											fillInAtrributeValuesOneToMany(ops, mapping);
		ArrayList<BPMNElement> poolElements = getPoolElements(ops);
		
		
		//add sequence flow elements to graphElements
		for (BPMNElement edgeEl: edgeElements){
			graphElements.add(edgeEl);
		}
		for (BPMNElement singleEl: singleElements){
			graphElements.add(singleEl);
		}
		for (BPMNElement complexEl: complexElements){
			graphElements.add(complexEl);
		}
		
		for (BPMNElement poolEl: poolElements){
			graphElements.add(poolEl);
		}
		
		return graphElements;
	}
	
	public static ArrayList<BPMNElement> fillInAttributeValuesOneToOne(
			ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping) {
		ArrayList<BPMNElement> singleElements = new ArrayList<BPMNElement>();

		//one-to-one mappings
		//*****************************************************************************************
		for (Integer key : ops.keySet()) {
			for (String str : mapping.keySet()) {
				for(BPMNElement el: mapping.get(str)){
				if (str.equals(ops.get(key).getOperationType().getOpTypeName()
						.toString()) && mapping.get(str).size() == 1){
					for (BPMNAttribute attr : el.getAttributes()) {	
					if (attr.getAttributeValue().equals("")){
						System.out.println(attr.getAttributeName());
						switch(attr.getAttributeName()){
						case "name":
							attr.setAttributeValue(ops.get(key).getOperationName());
								break;
						case "id":
							attr.setAttributeValue("_"
									+ String.valueOf(ops.get(key).getNodeID()));
							break;
					}
				}
					}
					singleElements.add(el);
				} 
			}
		}
		}

		//System.out.println(graphElements);
		return singleElements;
	}
	
	public static ArrayList<BPMNElement> fillInEdgeAttributeValues(ETLFlowGraph G, Hashtable<Integer, ETLFlowOperation> ops,
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
	
	public static ArrayList<BPMNElement> fillInAtrributeValuesOneToMany (Hashtable<Integer, ETLFlowOperation> ops,
			HashMap<String, ArrayList<BPMNElement>> mapping){
		ArrayList<BPMNElement> complexElements = new ArrayList<BPMNElement>();
		for (Integer key : ops.keySet()) {
			/*ArrayList<String> flags = getNodeFlags(ops.get(key).getOperationType().getOpTypeName());
			System.out.println(ops.get(key).getOperationType().getOpTypeName()+" "+ flags.size());
			if(flags.size() == 0){*/
			for (String str : mapping.keySet()) {
				//System.out.println("blah2 str");
				/*Random randomGenerator = new Random();
				String randomID= "_0"+randomGenerator.nextInt(100);*/
				String randomID="_0"+String.valueOf(ops.get(key).getNodeID());
				for(BPMNElement el: mapping.get(str)){
					//one-to-many mapping for Join and LeftOuterJoin		
					if (str.equals(ops.get(key).getOperationType().getOpTypeName()
							.toString()) && mapping.get(str).size()>1){	

					for (BPMNAttribute attr : el.getAttributes()) {
							
						switch(attr.getAttributeName()){
						case "name":
							if(attr.getAttributeValue().equals("")){
								attr.setAttributeValue(ops.get(key).getOperationName());
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(str);
								break;
							}
							break;
						case "id":
							if(attr.getAttributeValue().equals("") && !el.getElementName().equals("sequenceFlow")){
								attr.setAttributeValue("_"+ String.valueOf(ops.get(key).getNodeID()));
								break;
							} else if (attr.getAttributeValue().equals("create")){
								attr.setAttributeValue(randomID);
							} else if (el.getElementName().equals("sequenceFlow")){
								//System.out.println("source and target ref from inside the case id "+sourceRef+"-"+targetRef);
								attr.setAttributeValue("_"+String.valueOf(ops.get(key).getNodeID())+"-"+randomID);
								break;
							}
							break;
						case "sourceRef":
							attr.setAttributeValue("_"+String.valueOf(ops.get(key).getNodeID()));
							break;
						case "targetRef":
							attr.setAttributeValue(randomID);
							break;
						}
						//for a sequence flow coming out of the task
						/*BPMNElement seqFlow2 = new BPMNElement("sequenceFlow");
						BPMNAttribute attr1 = new BPMNAttribute("sourceRef", "randomID");
						BPMNAttribute attr2 = new BPMNAttribute("targetRef", );*/
						}
						complexElements.add(el);
					}
			}
			}
		}

			//System.out.println(graphElements);
			return complexElements;
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
		}
		int counter = 1;
		for (Integer i : flowIDs) {
			for (String str : engineTypes) {
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
		System.out.println("engineTypes " + engineTypes);
		return poolElements;
	}

}
