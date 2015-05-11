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

public class JSONDictionary {

	// private static String dictionaryFilePath =
	// "C:\\Users\\Elena\\Desktop\\testForQ1.json";
	public static final String dictionaryFilePath = "C:\\Users\\Elena\\Desktop\\test.json";
	private static ArrayList<BPMNAttribute> attributes = new ArrayList();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// call method
		HashMap<String, ArrayList<BPMNElement>> mapping = parseJSONDictionary();
		HashMap<String, ArrayList<String>> flagMapping = getPatternFlagsPerNode();
		for (String str : mapping.keySet()) {
			for (BPMNElement el : mapping.get(str)) {
				
				if (str.equals("Join")){
				for (BPMNAttribute attr : el.getAttributes()) {
					//System.out.println(attr.name + " " + attr.value);
				}
			}
			}
		}
		for (String optype: flagMapping.keySet()){
			for (String flagName: flagMapping.get(optype)){
				if(optype.equals("TableInput")){
					System.out.println(optype+" "+flagName);
				}
			}
		}

	}
	
	public static JSONArray getJSONRootObject(){
		JSONParser parser = new JSONParser();
		Object obj;
		JSONArray dictionary = new JSONArray();

		try {
			obj = parser.parse(new FileReader(dictionaryFilePath));
			JSONObject jsonObject = (JSONObject) obj;

			// loop through the root array
			dictionary = (JSONArray) jsonObject.get("nodeDictionary");
	} catch (IOException | ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		return dictionary;	
	}

	public static HashMap<String, ArrayList<BPMNElement>> parseJSONDictionary() {
		HashMap<String, ArrayList<BPMNElement>> mapping = new HashMap<String, ArrayList<BPMNElement>>();
		//hashMap between opType and the name of the pattern that could start at the node
		BPMNElement element = new BPMNElement();
		ArrayList<BPMNElement> elementsPerOptype = new ArrayList<BPMNElement>();
		BPMNAttribute bpmnAttr;

			// loop through the root array
			JSONArray dictionary = getJSONRootObject();

			Integer size = dictionary.size();

			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String category = (String) root.get("category");
				String xlmName = (String) root.get("xlmName");

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
	
	public static HashMap<String, ArrayList<String>> getPatternFlagsPerNode(){
		HashMap<String, ArrayList<String>> flagMapping = new HashMap<String, ArrayList<String>>();
		JSONArray dictionary = getJSONRootObject();

		Integer size = dictionary.size();

		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String category = (String) root.get("category");
			String xlmName = (String) root.get("xlmName");
			
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
}
