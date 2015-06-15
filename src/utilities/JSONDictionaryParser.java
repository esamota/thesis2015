package utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.util.Elements;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import operationDictionary.OperationTypeName;
import patternDiscovery.Pattern;
import patternDiscovery.PatternElement;
import patternDiscovery.PatternFlow;
import patternDiscovery.PatternSequence;
import patternDiscovery.PatternStep;
import toBPMN.BPMNAttribute;
import toBPMN.BPMNElement;


public class JSONDictionaryParser {

	// private static String dictionaryFilePath =
	// "C:\\Users\\Elena\\Desktop\\testForQ1.json";
	public static final String dictionaryFilePath = "mappings//newJSONDictionary.json";
	private static ArrayList<BPMNAttribute> attributes = new ArrayList();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// call method
		ArrayList<PatternElement> operationPatterns = getSingleOperationPatterns();
		for (PatternElement patternElement: operationPatterns){
			System.out.println(patternElement.getElementName()+" "+patternElement.getElementID());		
				}
	}
	// works
	public static JSONArray getJSONRootObject(String filePath, String rootName){
		JSONParser parser = new JSONParser();
		Object obj;
		JSONArray dictionary = new JSONArray();

		try {
			obj = parser.parse(new FileReader(filePath));
			JSONObject jsonObject = (JSONObject) obj;

			// loop through the root array
			dictionary = (JSONArray) jsonObject.get(rootName);
	} catch (IOException | ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		return dictionary;	
	}
	//works
	public static HashSet<String> getOperationTypeEnums() {

		  HashSet<String> values = new HashSet<String>();

		  for (OperationTypeName op : OperationTypeName.values()) {
		      values.add(op.name());
		  }

		  return values;
	}
	//works
	public static ArrayList<PatternElement> getSingleOperationPatterns() {
		ArrayList<PatternElement> operationPatterns = new ArrayList<PatternElement>();
		BPMNElement element = new BPMNElement();
		HashSet<String> operations = getOperationTypeEnums();
		BPMNAttribute bpmnAttr;
		String value = "";
		String name = "";
			// loop through the root array
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");

			Integer size = dictionary.size();

			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				String patternDescription = (String) root.get("description");
				Pattern patternElement = new Pattern();
				if (operations.contains(patternName) || patternName.equals("edge")){
					patternElement.setElementName(patternName);
					if (patternName != "edge") patternElement.setElementID("op"+(i+1));
					else patternElement.setElementID("e"+(i+1));
					patternElement.setDescription(patternDescription);
				JSONArray pattern = (JSONArray) root.get("pattern");
				for (int p = 0; p < pattern.size(); p++) {
				JSONObject patternObj = (JSONObject) pattern.get(p);
				JSONArray sequence = (JSONArray) patternObj.get("sequence");
					PatternSequence patternSequence = new PatternSequence();
					patternSequence.setElementName("sequence");
					for (int seq = 0; seq < sequence.size(); seq++) {
					JSONObject sequenceObj = (JSONObject) sequence.get(seq);
					JSONObject step1 = (JSONObject) sequenceObj.get("s1");
					PatternStep patternStep = new PatternStep();
					name = (String) step1.get("name");
					patternStep.setElementName(name);
					JSONArray values = (JSONArray) step1.get("values");
						for (int v = 0; v < values.size(); v++) {
						JSONObject valuesObj = (JSONObject) values.get(v);
						value = (String) valuesObj.get("value");
						patternStep.addStepValue(value);
						}
						patternSequence.addPatternSubElement(patternStep);
					}
				}
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
					patternElement.addBpmnElement(element);
				}			
			}
				operationPatterns.add(patternElement);
			}
		return operationPatterns;
	}
	//works
	//returns a {pattern name (operation name) - bpmnElements} pair
	public static HashMap<String, ArrayList<String>> getOperatorPatternFlags(){
		HashMap<String, ArrayList<String>> flagMapping = new HashMap<String, ArrayList<String>>();
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		HashSet<String> operationTypes = getOperationTypeEnums();
		ArrayList<String> flagNames = new ArrayList<String>();
		Integer size = dictionary.size();

		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if (operationTypes.contains(patternName)) {
				if (root.get("patternFlag") != null){
			JSONArray patternFlag = (JSONArray) root.get("patternFlag");
		
			for (int f=0; f <patternFlag.size(); f++){
				JSONObject flag = (JSONObject) patternFlag.get(f);
				String flagName = (String) flag.get("name");
				flagNames.add(flagName);
			}
			flagMapping.put(patternName, new ArrayList<String>(flagNames));
			}
			flagNames.clear();
		}
		}
			return flagMapping;
	}
		
	//ok
	public static ArrayList<String> getPatternNames(){
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		Integer size = dictionary.size();
		ArrayList<String> patternNames = new ArrayList<String>(); 
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			patternNames.add(patternName);
		}
		return patternNames;
	}

	public static ArrayList<PatternElement> getFullPatternElementByName (String flagName){
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		Integer size = dictionary.size();
		ArrayList<PatternElement> patterns = new ArrayList<>();
		Pattern patternElement = new Pattern();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			String patternDescription = (String) root.get("description");
			if(patternName.equals(flagName)){
				patternElement.setElementName(patternName);
				patternElement.setDescription(patternDescription);
				patternElement.setElementID("p"+String.valueOf(i+1));
				if (root.get("pattern") != null){
				patternElement = parsePatternStructure(root);
				} 
				if (root.get("bpmnElement") != null){
				patternElement.setBpmnElements(parsePatternBPMNElements(root));
				}
				if (root.get("whiteList") != null){
					patternElement.setWhiteList(parsePatternWhiteList(root));
				}
				if (root.get("blackList") != null){
					patternElement.setBlackList(parsePatternBlackList(root));
				}
			}
			patterns.add(patternElement);
			patternElement = new Pattern();
		}
		return patterns;
		}
	
	public static Pattern parsePatternStructure(JSONObject root){
		ArrayList<PatternFlow> patternFlows = new ArrayList<>();
		
		Pattern patternElement = new Pattern();
		JSONArray pattern = (JSONArray) root.get("pattern");	
		for (int p=0; p < pattern.size(); p++){
		JSONObject patternObj = (JSONObject) pattern.get(p);
		if (patternObj.get("sequence") != null){
			PatternSequence patternSequence = parsePatternSequence(patternObj);
			patternElement.addPatternSubElement(patternSequence);
		} 
		else if (patternObj.get("splitFlow") != null){
			patternFlows.addAll(parsePatternFlow(patternObj));	
			for (PatternFlow patternFlow: patternFlows){
				patternElement.addPatternSubElement(patternFlow);
		} 
		}
		}
		return patternElement;
	}
	
	
	public static PatternSequence parsePatternSequence(JSONObject patternObj){
		ArrayList<String> stepValues = new ArrayList<>();
		PatternSequence patternSequence = new PatternSequence();
		JSONArray sequence = (JSONArray) patternObj.get("sequence");
			for (int s = 0; s < sequence.size(); s++){
				JSONObject seqObj = (JSONObject) sequence.get(s);
				PatternStep patternStep = new PatternStep();
				JSONObject step = (JSONObject) seqObj.get("s"+ String.valueOf(s+1));
				String name = (String) step.get("name");
				JSONArray values = (JSONArray) step.get("values");
				for (int v = 0; v < values.size(); v++) {
					JSONObject valuesObj = (JSONObject) values.get(v);
					String value = (String) valuesObj.get("value");
					stepValues.add(value);
		}
				patternStep.setElementName(name);
				patternStep.setElementID("s"+String.valueOf(s+1));
				patternStep.setStepValues(new ArrayList<String>(stepValues));
			patternSequence.addPatternSubElement(patternStep);
			stepValues.clear();	
			}
		return patternSequence;
	}
	
	public static ArrayList<PatternFlow> parsePatternFlow(JSONObject patternObj){
		ArrayList<String> stepValues = new ArrayList<>();
		ArrayList<PatternFlow> flows = new ArrayList<>();
		JSONArray splitFlow = (JSONArray) patternObj.get("splitFlow");
		for (int sf=0; sf < splitFlow.size(); sf++){
			JSONObject splitFlowObj = (JSONObject) splitFlow.get(sf);
			JSONArray flow = (JSONArray) splitFlowObj.get("flow"+String.valueOf(sf+1));
			PatternFlow patternFlow = new PatternFlow();
			for (int f=0; f< flow.size(); f++){
				JSONObject flowObj = (JSONObject) flow.get(f);
				if (flowObj.get("repeat") != null) {
					String repeat = (String) flowObj.get("repeat");
					patternFlow.setPatternFlowRepeatValue(repeat);
				}
				if (flowObj.get("sequence") != null){
					PatternSequence flowSequence = parsePatternSequence(flowObj);
					patternFlow.addPatternSubElement(flowSequence);
				} else if (flowObj.get("splitFlow") != null){
					ArrayList<PatternFlow> subFlows = parsePatternFlow(flowObj);
					for (PatternFlow subFlow: subFlows){
					patternFlow.addPatternSubElement(subFlow);}
				}
				flows.add(patternFlow);
			}
		}	
		return flows;
	}
	

	public static ArrayList<BPMNElement> parsePatternBPMNElements(JSONObject root){
		ArrayList<BPMNElement> bpmnElements = new ArrayList<>();
		
		JSONArray bpmnElement = (JSONArray) root.get("bpmnElement");
		for (int k = 0; k < bpmnElement.size(); k++) {
			JSONObject bpmn = (JSONObject) bpmnElement.get(k);
			String elName = (String) bpmn.get("name");
			BPMNElement element = new BPMNElement(elName);

			JSONArray attributes = (JSONArray) bpmn.get("attributes");
			for (int l = 0; l < attributes.size(); l++) {
				JSONObject attribute = (JSONObject) attributes.get(l);
				String attrName = (String) attribute.get("name");
				String attrValue = (String) attribute.get("value");
				BPMNAttribute bpmnAttr = new BPMNAttribute(attrName, attrValue);
				element.addAttribute(bpmnAttr);
			}
			String text = (String) bpmn.get("text");
			element.addText(text);
			bpmnElements.add(element);
		}			
		return bpmnElements;
	}
	
	public static HashMap<String, ArrayList<String>> parsePatternWhiteList (JSONObject root) {
		HashMap<String, ArrayList<String>> wListOperations = new HashMap<>();
		ArrayList<String> whiteListValues = new ArrayList<>();
		
		JSONArray wListArray = (JSONArray) root.get("whiteList");
		for (int w=0; w < wListArray.size(); w++){
			JSONObject wListObj = (JSONObject) wListArray.get(w);
			String name = (String) wListObj.get("name");
					
			JSONArray values = (JSONArray) wListObj.get("values");
			for (int v = 0; v < values.size(); v++) {
				JSONObject valuesObj = (JSONObject) values.get(v);
				String value = (String) valuesObj.get("value");
				whiteListValues.add(value);
			} 
			wListOperations.put(name, new ArrayList<>(whiteListValues));
			whiteListValues.clear();
				}
		return wListOperations;
	}
	
	public static HashMap<String, ArrayList<String>> parsePatternBlackList(JSONObject root){
		HashMap<String, ArrayList<String>> bListOperations = new HashMap<>();
		ArrayList<String> blackListValues = new ArrayList<>();
		
				JSONArray bListArray = (JSONArray) root.get("blackList");
				for (int b=0; b < bListArray.size(); b++){
					JSONObject bListObj = (JSONObject) bListArray.get(b);
					String name = (String) bListObj.get("name");
					JSONArray values = (JSONArray) bListObj.get("values");
					for (int v = 0; v < values.size(); v++) {
						JSONObject valuesObj = (JSONObject) values.get(v);
						String value = (String) valuesObj.get("value");
						blackListValues.add(value);
					} 
					bListOperations.put(name, new ArrayList<>(blackListValues));
					blackListValues.clear();
				}
		return bListOperations;
	}
}
