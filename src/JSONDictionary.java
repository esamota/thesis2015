import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONDictionary {

	// private static String dictionaryFilePath =
	// "C:\\Users\\Elena\\Desktop\\testForQ1.json";
	private static String dictionaryFilePath = "C:\\Users\\Elena\\Desktop\\test.json";
	private static ArrayList<BPMNAttribute> fixedAttributes = new ArrayList();
	private static ArrayList<BPMNAttribute> variableAttributes= new ArrayList();;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// call method
		JSONObject jsonObject = createJSONObject(dictionaryFilePath);
		BPMNElement element = getBPMNElements(jsonObject.get())
		HashMap<String, BPMNElement> mapping = parseJSONDictionary(jsonObject);
		System.out.println(mapping);
		for (String str : mapping.keySet()) {
			System.out.println(str);
		}

	}

	public static JSONObject createJSONObject(String dictionaryFilePath) {
		JSONParser parser = new JSONParser();
		Object obj;
		JSONObject jsonObject = new JSONObject();

		try {
			obj = parser.parse(new FileReader(dictionaryFilePath));
			jsonObject = (JSONObject) obj;
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;

	}

	public static HashMap<String, BPMNElement> parseJSONDictionary(
			JSONObject jsonObject) {
		HashMap<String, BPMNElement> mapping = new HashMap<String, BPMNElement>();
		BPMNElement element = new BPMNElement();
		BPMNAttribute bpmnAttr;

		// loop through the root array
		JSONArray dictionary = (JSONArray) jsonObject.get("nodeDictionary");
		Integer size = dictionary.size();
		for (int i = 0; i < size; i++) {
			JSONObject root = getNodeDictionaryObject(jsonObject);
			String xlmName = (String) root.get("xlmName");
			element = getBPMNElements(root);
			mapping.put(xlmName, element);
			}
		return mapping;
			
			/*
			 * System.out.println(mapping + element.getElementName()); for
			 * (BPMNAttribute attr: element.getFixedAttributes()){
			 * System.out.println (attr.getAttributeName()+" "+
			 * attr.getAttributeValue()); } for (BPMNAttribute attr :
			 * element.getVariableAttributes()){ System.out.println
			 * (attr.getAttributeName()+" "+ attr.getAttributeValue()); } }
			 */
		}


	public static JSONObject getNodeDictionaryObject(JSONObject jsonObject) {
		JSONArray dictionary = (JSONArray) jsonObject.get("nodeDictionary");
		JSONObject root = new JSONObject();
		Integer size = dictionary.size();

		for (int i = 0; i < size; i++) {
			root = (JSONObject) dictionary.get(i);
		}
		return root;
	}

	public static BPMNElement getBPMNElements(JSONObject root) {
		BPMNElement element = new BPMNElement();
		BPMNAttribute bpmnAttr = new BPMNAttribute();
		
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

				if (!attrName.equals("") && attrValue.equals("")) {
					bpmnAttr = new BPMNAttribute(attrName, attrValue);
					element.addVariableAttribute(bpmnAttr);
				}
				else {
					bpmnAttr = new BPMNAttribute(attrName, attrValue);
					element.addFixedAttribute(bpmnAttr);

			}
			}
		}
		return element;
	}

	public static ArrayList<BPMNAttribute> getVariableAttributes(BPMNElement element) {
		return element.getVariableAttributes();
		
	}
	
	public static ArrayList<BPMNAttribute> getFixedAttributes(BPMNElement element) {
		return element.getFixedAttributes();
	}
}
