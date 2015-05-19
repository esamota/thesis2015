import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.util.Elements;

import operationDictionary.OperationTypeName;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		//System.out.println(patternOriginOptype);
		ArrayList<HashMap<String,String>> step1Operations = parseJSONSingleFlowPatternStep1("sortMerge");
		System.out.println(step1Operations);
	
		/*for (String str : mapping.keySet()) {
			for (BPMNElement el : mapping.get(str)) {
				
				System.out.println(el.getElementText());
				for (BPMNAttribute attr : el.getAttributes()) {
					//System.out.println(attr.name + " " + attr.value);
				}
			}
			}*/
		/*for (String optype: flagMapping.keySet()){
			for (String flagName: flagMapping.get(optype)){
					System.out.println(optype+" "+flagName);
			}
		}*/

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
	
	public static ArrayList<HashMap<String, String>> parseJSONSingleFlowPatternStep1(String flagName){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		ArrayList<HashMap<String, String>> step1Operations = new ArrayList<HashMap<String, String>>(); 
		ArrayList<String> patternOriginOptypes = new ArrayList<String>();
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if(patternName.equals(flagName)){
				patternOriginOptypes = getPatternOrigin(patternName);
				if (!patternOriginOptypes.contains(OperationTypeName.Router.name()) && !patternOriginOptypes.contains(OperationTypeName.Splitter.name())){
				JSONArray stepsArray = (JSONArray) root.get("steps");
				for(int s=0; s < stepsArray.size(); s++){
					JSONObject stepsObj = (JSONObject) stepsArray.get(s);
					
					JSONArray step1Array = (JSONArray) stepsObj.get("s1");
					for(int t=0; t < step1Array.size(); t++){
					JSONObject stepObj = (JSONObject) step1Array.get(t);	
					HashMap<String, String> stepOperation = new HashMap<String, String>();
					String name = (String) stepObj.get("name");
					String value = (String) stepObj.get("value");
					stepOperation.put(name, value);
					//System.out.println(name+" "+value);
					step1Operations.add(stepOperation);
					}
			}
				}
				}
		}
		return step1Operations;
	}
	
	//start here
	public static HashMap<String, ArrayList<HashMap>> parseJSONSplitFlowPatternStep1(){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		HashMap<String, ArrayList<HashMap>> step1Operations = new HashMap<String, ArrayList<HashMap>>();
		ArrayList<HashMap> stepOperations = new ArrayList<HashMap>(); 
		ArrayList<String> patternOriginOptypes = new ArrayList<String>();
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
				patternOriginOptypes = getPatternOrigin(patternName);
				if (!patternOriginOptypes.contains(OperationTypeName.Router.name()) && !patternOriginOptypes.contains(OperationTypeName.Splitter.name())){
				JSONArray stepsArray = (JSONArray) root.get("steps");
				for(int s=0; s < stepsArray.size(); s++){
					JSONObject stepsObj = (JSONObject) stepsArray.get(s);
					
					JSONArray step1Array = (JSONArray) stepsObj.get("s1");
					for(int t=0; t < step1Array.size(); t++){
					JSONObject stepObj = (JSONObject) step1Array.get(t);	
					HashMap stepOperation = new HashMap();
					String name = (String) stepObj.get("name");
					String value = (String) stepObj.get("value");
					stepOperation.put(name, value);
					//System.out.println(name+" "+value);
					stepOperations.add(stepOperation);
					}
					step1Operations.put(patternName, new ArrayList<HashMap> (stepOperations));
					stepOperations.clear();
			}
				}
		}
		return step1Operations;
	}
	
	

}
