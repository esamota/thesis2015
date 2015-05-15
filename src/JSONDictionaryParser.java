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
		//HashMap<String, ArrayList<String>> startProperties = getStartPatternProperties();
		//System.out.println(startProperties);
	
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
	
	public static HashMap<String, ArrayList<String>> getStartPatternProperties(String name){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		HashMap<String, ArrayList<String>> startProperties = new HashMap<String, ArrayList<String>>();
		ArrayList<String> propValues = new ArrayList<String>(); 
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if (patternName.equals(name)){
			JSONObject patternStart = (JSONObject) root.get("start");
			String xlmElement = (String) patternStart.get("name");
				//System.out.println(xlmElement);
				JSONArray properties = (JSONArray) patternStart.get("property");
				for(int p=0; p < properties.size(); p++){
					JSONObject prop = (JSONObject) properties.get(p);
					String propertyName = (String) prop.get("name");
					String propertyValue = (String) prop.get("value");
					if(startProperties.containsKey(propertyName)){
						propValues.add(propertyValue);
					} else if(!startProperties.containsKey(propertyName)){
						propValues.add(propertyValue);
						startProperties.put(propertyName, propValues);
				}
			}
		}
			}
		return startProperties;
	}
	
	public static ArrayList<HashMap> getMiddlePatternProperties(){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		ArrayList<HashMap> middleProperties = new ArrayList<HashMap>();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
					JSONObject middle = (JSONObject) root.get("middle");
					String xlmElement = (String) middle.get("name");
					System.out.println(xlmElement);
					JSONArray properties = (JSONArray) middle.get("property");
					for(int p=0; p < properties.size(); p++){
						JSONObject prop = (JSONObject) properties.get(p);
						String propertyName = (String) prop.get("name");
						String propertyValue = (String) prop.get("value");
						HashMap middleProperty = new HashMap();
						middleProperty.put(propertyName, propertyValue);
						middleProperties.add(middleProperty);
					}
				}
			return middleProperties;
		
	}
	
	public static ArrayList<HashMap> getPatternEndProperties(){
		JSONArray dictionary = getJSONRootObject("patternDictionary");
		Integer size = dictionary.size();
		ArrayList<HashMap> endProperties = new ArrayList<HashMap>();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			JSONObject end = (JSONObject) root.get("end");
			String xlmElement = (String) end.get("name");
			System.out.println(xlmElement);
			JSONArray properties = (JSONArray) end.get("property");
			for(int p=0; p < properties.size(); p++){
						JSONObject prop = (JSONObject) properties.get(p);
						String propertyName = (String) prop.get("name");
						String propertyValue = (String) prop.get("value");
						HashMap endProperty = new HashMap();
						endProperty.put(propertyName, propertyValue);
						endProperties.add(endProperty);
					}
				}
			return endProperties;	
	}
	

}
