import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class JSONDictionary {
	
	//private static String dictionaryFilePath = "C:\\Users\\Elena\\Desktop\\testForQ1.json";
	private static String dictionaryFilePath = "C:\\Users\\Elena\\Desktop\\test.json";
	
	public static void parseJSONDictionary(String dictionaryFilePath){
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONParser parser = new JSONParser();
		HashMap<String, BPMNElement> mapping = new HashMap<String, BPMNElement>();
		BPMNElement element = new BPMNElement();
		BPMNAttribute bpmnAttr;
		
		Object obj;
		try {
			obj = parser.parse(new FileReader(dictionaryFilePath));

			JSONObject jsonObject = (JSONObject) obj;

			//String age = (String) jsonObject.get("age");
			//System.out.println(age);
			
			// loop array
			JSONArray dictionary = (JSONArray) jsonObject.get("nodeDictionary");
			Integer size = dictionary.size();
			for(int i = 0; i < size; i++){
				JSONObject a = (JSONObject) dictionary.get(i);
				String category = (String) a.get("category");
				String xlmName = (String) a.get("xlmName");
				//System.out.println("xlmName "+ a.get("xlmName"));
				
				JSONArray bpmnElement = (JSONArray)a.get("bpmnElement");
				for(int k=0; k < bpmnElement.size(); k++){
					JSONObject bpmn = (JSONObject) bpmnElement.get(k);
					String elName = (String)bpmn.get("name");
					element = new BPMNElement(elName);
					element.getElementName();
					//System.out.println("bpmn element "+bpmn.get("name"));
					
					JSONArray attributes = (JSONArray)bpmn.get("attributes");
					for(int l=0; l < attributes.size(); l++){
						JSONObject attribute = (JSONObject) attributes.get(l);
						String attrName = (String) attribute.get("name");
						String attrValue = (String) attribute.get("value");
						
						if (!attrName.equals("") && attrValue.equals("")){
							bpmnAttr = new BPMNAttribute (attrName, attrValue);
							element.addVariableAttribute(bpmnAttr);
						}
						else {
							bpmnAttr = new BPMNAttribute (attrName, attrValue);
							element.addFixedAttribute(bpmnAttr);
						}
						
						
					}
					
				}
				mapping.put(xlmName, element);
				System.out.println(mapping + element.getElementName());
				for (BPMNAttribute attr: element.getFixedAttributes()){
					System.out.println (attr.getAttributeName()+" "+ attr.getAttributeValue());
				}
				for (BPMNAttribute attr : element.getVariableAttributes()){
					//System.out.println (attr.getAttributeName()+" "+ attr.getAttributeValue());
				}
				//System.out.println(element.getElementName());
			}
			
		//	System.out.println(mapping + element.getElementName());
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
