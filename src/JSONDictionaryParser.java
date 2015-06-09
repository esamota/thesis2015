import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.util.Elements;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import operationDictionary.OperationTypeName;


public class JSONDictionaryParser {

	// private static String dictionaryFilePath =
	// "C:\\Users\\Elena\\Desktop\\testForQ1.json";
	public static final String dictionaryFilePath = "mappings//JSONDictionary.json";
	private static ArrayList<BPMNAttribute> attributes = new ArrayList();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// call method
		HashMap<String, ArrayList<BPMNElement>> mapping = parseNodeDictionary();
		HashMap<String, ArrayList<String>> flagMapping = getNodePatternFlags();
		HashMap<String, ArrayList<String>> patternOriginOptype = parseJSONPatternOriginOptypes();
		
		HashMap<String, ArrayList<String>> stepOperations = parseJSONPatternSteps("subprocess", 0, 0, 0);
		for (String name: stepOperations.keySet()){
			for (String value: stepOperations.get(name)){
				System.out.println("name = "+name+", value = "+value);
				//if (value.equals("whiteList")) System.out.println("true");
			}
		}
		System.out.println(stepOperations.get("*").contains("whiteList"));
		
		//test parsing version-flow-step of a pattern -- reused in pattern search class
		/* Integer numOfVersions = getNumberOfPatternVersions("recoveryPoint");
		for (int v = 0; v < numOfVersions; v++){
			Integer numOfFlows = getNumberOfPatternFlows("recoveryPoint", v);
			for (int f=0; f < numOfFlows; f++){
				Integer numOfSteps = getNumberOfVersionFlowSteps("recoveryPoint", v, f);
				//System.out.println("v = "+v+", f = "+f+ " numOfSteps = " + numOfSteps);
				for (int s=0; s< numOfSteps; s++){
					HashMap<String, ArrayList<String>> stepOperations = parseJSONPatternSteps("recoveryPoint", v, f, s);
					for (String name: stepOperations.keySet()){
						for (String value: stepOperations.get(name)){
							System.out.println("version "+(v+1)+", flow "+(f+1)+", step "+ (s+1)+", name = "+name+", value = "+value);
						}
					}
					
			}
		}
		
		}*/
		
        HashMap<String, ArrayList<String>> wListOperations = getBlackListItems("recoveryPoint");
        for (String name: wListOperations.keySet()){
        	for(String value: wListOperations.get(name)){
        		System.out.println(name+" "+value);
        	}
        }
	}
	
	public static JSONArray getJSONRootObject(String rootName){
		JSONParser parser = new JSONParser();
		Object obj;
		JSONArray dictionary = new JSONArray();

		try {
			obj = parser.parse(new FileReader(dictionaryFilePath));
			JSONObject jsonObject = (JSONObject) obj;

			// loop through the root array
			dictionary = (JSONArray) jsonObject.get(rootName);
	} catch (IOException | ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		return dictionary;	
	}

	public static HashMap<String, ArrayList<BPMNElement>> parseNodeDictionary() {
		HashMap<String, ArrayList<BPMNElement>> mapping = new HashMap<String, ArrayList<BPMNElement>>();
		//hashMap between opType and the name of the pattern that could start at the node
		BPMNElement element = new BPMNElement();
		ArrayList<BPMNElement> elementsPerOptype = new ArrayList<BPMNElement>();
		BPMNAttribute bpmnAttr;

			// loop through the root array
			JSONArray dictionary = getJSONRootObject("nodeDictionary");

			Integer size = dictionary.size();

			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String category = (String) root.get("category");
				String xlmName = (String) root.get("name");

				JSONArray bpmnElement = (JSONArray) root.get("bpmnElement");
				for (int k = 0; k < bpmnElement.size(); k++) {
					JSONObject bpmn = (JSONObject) bpmnElement.get(k);
					String elName = (String) bpmn.get("name");
					element = new BPMNElement(elName);

					JSONArray attributes = (JSONArray) bpmn.get("attributes");
					for (int l = 0; l < attributes.size(); l++) {
						JSONObject attribute = (JSONObject) attributes.get(l);
						String attrName = (String) attribute.get("name");
						String attrValue = (String) attribute.get("value");
						bpmnAttr = new BPMNAttribute(attrName, attrValue);
						element.addAttribute(bpmnAttr);
					}
					String text = (String) bpmn.get("text");
					element.addText(text);
					elementsPerOptype.add(element);
				}
				//System.out.println(xlmName+" "+ elementsPerOptype);
				if (category.equals("optype")) {
					mapping.put(xlmName, new ArrayList<BPMNElement>(elementsPerOptype));
				} else if (category.equals("edge")) {
					mapping.put(category, new ArrayList<BPMNElement>(elementsPerOptype));
				}
			elementsPerOptype.clear();
			}
		return mapping;
	}
	
	public static HashMap<String, ArrayList<String>> getNodePatternFlags(){
		HashMap<String, ArrayList<String>> flagMapping = new HashMap<String, ArrayList<String>>();
		JSONArray dictionary = getJSONRootObject("nodeDictionary");

		Integer size = dictionary.size();

		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String category = (String) root.get("category");
			String xlmName = (String) root.get("name");
			
			JSONArray patternFlag = (JSONArray) root.get("patternFlag");
			ArrayList<String> flagNames = new ArrayList<String>();
			for (int f=0; f <patternFlag.size(); f++){
				JSONObject flag = (JSONObject) patternFlag.get(f);
				String flagName = (String) flag.get("name");
				flagNames.add(flagName);
			}
			if (category.equals("optype")) {
				flagMapping.put(xlmName, new ArrayList<String>(flagNames));
			}
			flagNames.clear();
		}
			return flagMapping;
	}
	
	public static HashMap<String, ArrayList<String>> parseJSONPatternOriginOptypes(){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		HashMap<String, ArrayList<String>> patternOriginOptype = new HashMap<String, ArrayList<String>>();
		ArrayList<String> optypeValues = new ArrayList<String>(); 
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
				JSONArray origin = (JSONArray) root.get("origin");
				for(int p=0; p < origin.size(); p++){
					JSONObject originObj = (JSONObject) origin.get(p);
					String originType = (String) originObj.get("name");
					String originTypeName = (String) originObj.get("value");
					//System.out.println(originType+" "+originTypeName);
					optypeValues.add(originTypeName);
			}
				patternOriginOptype.put(patternName, new ArrayList<String> (optypeValues));
				optypeValues.clear();
		}
		return patternOriginOptype;
	}
	
	public static ArrayList<String> getPatternOrigin(String name){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		ArrayList<String> optypeValues = new ArrayList<String>(); 
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if(patternName.equals(name)){
				JSONArray origin = (JSONArray) root.get("origin");
				for(int p=0; p < origin.size(); p++){
					JSONObject originObj = (JSONObject) origin.get(p);
					String originType = (String) originObj.get("name");
					String originTypeName = (String) originObj.get("value");
					//System.out.println(originType+" "+originTypeName);
					optypeValues.add(originTypeName);
			}
			}
				
		}
		return optypeValues;
	}
	
	public static ArrayList<String> getPatternNames(){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		ArrayList<String> patternNames = new ArrayList<String>(); 
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			patternNames.add(patternName);
		}
		return patternNames;
	}
	
	public static Integer getNumberOfPatternVersions(String flagName){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		Integer numOfVersions = 0;
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if(patternName.equals(flagName)){
				JSONArray versionsArray = (JSONArray) root.get("patternVersions");	
				numOfVersions = versionsArray.size();
			}
		}
		return numOfVersions;	
	}
	
	public static Integer getNumberOfPatternFlows(String flagName, Integer versionNumber){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		Integer numOfFlows = 0;
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if(patternName.equals(flagName)){
				JSONArray versionsArray = (JSONArray) root.get("patternVersions");	
				for(int p=0; p < versionsArray.size(); p++){
					JSONObject versionsObj = (JSONObject) versionsArray.get(versionNumber);
					JSONArray flowsObj =(JSONArray) versionsObj.get("flows");
					numOfFlows = flowsObj.size();
			}
		}
		}
		return numOfFlows;
	}
	
	public static Integer getNumberOfVersionFlowSteps(String flagName, Integer versionID, Integer flowID){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		Integer numOfSteps = 0;
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if(patternName.equals(flagName)){
				JSONArray versionsArray = (JSONArray) root.get("patternVersions");	
				JSONObject versionsObj = (JSONObject) versionsArray.get(versionID);
					
				JSONArray flowsArray = (JSONArray) versionsObj.get("flows");
				JSONObject flowsObj = (JSONObject) flowsArray.get(flowID);
					
				JSONArray flow1Array = (JSONArray) flowsObj.get("flow"+String.valueOf((flowID+1)));
				for (int q=0; q< flow1Array.size(); q++){
				JSONObject flowObj = (JSONObject) flow1Array.get(q);
					
				JSONArray stepsArray = (JSONArray) flowObj.get("steps");
				numOfSteps = stepsArray.size();
				}
				}
		}
		return numOfSteps;
	}
	
	public static HashMap<String, ArrayList<String>> getWhiteListItems(String flagName){
		HashMap<String, ArrayList<String>> wListOperations = new HashMap<>();
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			System.out.println("inside getWhiteList items "+ patternName + " "+ flagName);
			if (patternName.equals(flagName)) {
				JSONArray wListArray = (JSONArray) root.get("whiteList");
				for (int w=0; w < wListArray.size(); w++){
					JSONObject wListObj = (JSONObject) wListArray.get(w);
					String name = (String) wListObj.get("name");
					String value = (String) wListObj.get("value");
					if (wListOperations.get(name) == null){
						wListOperations.put(name, new ArrayList<String>());
						wListOperations.get(name).add(value);
					} else wListOperations.get(name).add(value);
				}
			}
	}
		return wListOperations;
	}
	
	public static HashMap<String, ArrayList<String>> getBlackListItems(String flagName){
		HashMap<String, ArrayList<String>> bListOperations = new HashMap<>();
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if (patternName.equals(flagName)) {
				JSONArray bListArray = (JSONArray) root.get("blackList");
				for (int b=0; b < bListArray.size(); b++){
					JSONObject bListObj = (JSONObject) bListArray.get(b);
					String name = (String) bListObj.get("name");
					String value = (String) bListObj.get("value");
					if (bListOperations.get(name) == null){
						bListOperations.put(name, new ArrayList<String>());
						bListOperations.get(name).add(value);
					} else bListOperations.get(name).add(value);
				}
			}
	}
		return bListOperations;
	}
	//based on the pattern name, returns name-value pairs for each version-flow-step combination
	public static HashMap<String, ArrayList<String>> parseJSONPatternSteps(
			String flagName, Integer versionID, Integer flowID, Integer stepID) {
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		HashMap<String, ArrayList<String>> stepOperations = new HashMap<String, ArrayList<String>>();
		ArrayList<String> patternOriginOptypes = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if (patternName.equals(flagName)) {

				JSONArray versionsArray = (JSONArray) root
						.get("patternVersions");
				// for(int p=0; p < versionsArray.size(); p++){
				JSONObject versionsObj = (JSONObject) versionsArray
						.get(versionID);
				JSONArray flowsArray = (JSONArray) versionsObj.get("flows");
				// for(int f=0; f < flowsArray.size(); f++){
				JSONObject flowsObj = (JSONObject) flowsArray.get(flowID);

				JSONArray flowNArray = (JSONArray) flowsObj.get("flow"
						+ String.valueOf((flowID + 1)));
				for (int q = 0; q < flowNArray.size(); q++) {
					JSONObject flowObj = (JSONObject) flowNArray.get(q);

					JSONArray stepsArray = (JSONArray) flowObj.get("steps");
					// for(int s=0; s < stepsArray.size(); s++){
					JSONObject stepsObj = (JSONObject) stepsArray.get(stepID);

					JSONArray stepNArray = (JSONArray) stepsObj.get("s"
							+ String.valueOf((stepID + 1)));
					for (int t = 0; t < stepNArray.size(); t++) {
						JSONObject stepObj = (JSONObject) stepNArray.get(t);
						String name = (String) stepObj.get("name");
						String value = (String) stepObj.get("value");
						if (stepOperations.get(name) == null) {
							stepOperations.put(name, new ArrayList<String>());
							stepOperations.get(name).add(value);
						} else
							stepOperations.get(name).add(value);
					}
				}
			}
		}
		// }
		// }
		// }
		return stepOperations;
	}


}
