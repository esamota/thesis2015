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
		HashMap<String, ArrayList<BPMNElement>> mapping = parseJSONDictionary(dictionaryFilePath);

		for (String str : mapping.keySet()) {
			for (BPMNElement el : mapping.get(str)) {
				
				if (str.equals("Join")){
				for (BPMNAttribute attr : el.getAttributes()) {
					//System.out.println(attr.name + " " + attr.value);
				}
			}
			}
		}

	}

	public static HashMap<String, ArrayList<BPMNElement>> parseJSONDictionary(
			String dictionaryFilePath) {
		HashMap<String, ArrayList<BPMNElement>> mapping = new HashMap<String, ArrayList<BPMNElement>>();
		BPMNElement element = new BPMNElement();
		ArrayList<BPMNElement> elementsPerOptype = new ArrayList<BPMNElement>();
		BPMNAttribute bpmnAttr;

		JSONParser parser = new JSONParser();
		Object obj;

		try {
			obj = parser.parse(new FileReader(dictionaryFilePath));
			JSONObject jsonObject = (JSONObject) obj;

			// loop through the root array
			JSONArray dictionary = (JSONArray) jsonObject.get("nodeDictionary");

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
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapping;
	}
}
