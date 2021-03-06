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

import display.Demo;
import operationDictionary.OperationTypeName;
import patternDiscovery.Pattern;
import patternDiscovery.PatternElement;
import patternDiscovery.PatternFlow;
import patternDiscovery.PatternSequence;
import patternDiscovery.PatternStep;
import toBPMN.BPMNAttribute;
import toBPMN.BPMNElement;


public class JSONDictionaryParser {
	
	public static JSONArray getJSONRootObject(String dictionaryFilePath, String rootName){
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

	public static HashSet<String> getOperationTypeEnums() {

		  HashSet<String> values = new HashSet<String>();

		  for (OperationTypeName op : OperationTypeName.values()) {
		      values.add(op.name());
		  }

		  return values;
	}
	
	public static HashMap<String, ArrayList<String>> parsePatternFlags(String patternFlagMappingPath){
		HashMap<String, ArrayList<String>> flagMappings = new HashMap<>();
		ArrayList<String> flagNames = new ArrayList<>();
		JSONArray dictionary = getJSONRootObject(patternFlagMappingPath, "flagDictionary");
		Integer size = dictionary.size();
		
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String optype = (String) root.get("optype");
			JSONArray patternFlag = (JSONArray) root.get("patterns");
			for (int f=0; f <patternFlag.size(); f++){
				JSONObject flag = (JSONObject) patternFlag.get(f);
				String flagName = (String) flag.get("name");
				flagNames.add(flagName);
			}
			flagMappings.put(optype, new ArrayList<>(flagNames));
			flagNames.clear();
			}
		return flagMappings;
		}
	
	//returns a list of pattern names that could start with a given optype
	public static ArrayList<String> getPatternNamesByOriginOperation(HashMap<String, ArrayList<String>> flagMappings, String optype){
		ArrayList<String> flagNames = new ArrayList<>();
		
		for (String str: flagMappings.keySet()){
			if (str.equals(optype)){
				for (String flagName: flagMappings.get(str)){
					flagNames.add(flagName);
				}
			}
		}
		return flagNames;
	}
	
	public static HashMap<String, ArrayList<String>> updateFlagMappings(HashMap<String, ArrayList<String>> flagMappings, 
			String optype, String action, String flagName){
		if (action.equals("add")){
			flagMappings.get(optype).add(flagName);
		} else if (action.equals("remove")){
			flagMappings.get(optype).remove(flagName);
		}
		return flagMappings;
	}

	public static ArrayList<Pattern> getAllPatternElements (String dictionaryFilePath){
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		Integer size = dictionary.size();
		ArrayList<Pattern> patterns = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Pattern patternElement = new Pattern();
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			String patternDescription = (String) root.get("description");
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
				patternElement.setElementName(patternName);
				patternElement.setDescription(patternDescription);
				patternElement.setElementID("p"+String.valueOf(i+1));
			patterns.add(patternElement);
		}
		return patterns;
		}
	
	public static Pattern getAnyPatternElementByName (String dictionaryFilePath, String flagName){
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		Integer size = dictionary.size();
		Pattern patternElement = new Pattern();
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			String patternDescription = (String) root.get("description");
			if(patternName.equals(flagName)){
				if (root.get("pattern") != null){
				patternElement = parsePatternStructure(root);
				} else return new Pattern();
				if (root.get("bpmnElement") != null){
				patternElement.setBpmnElements(parsePatternBPMNElements(root));
				}
				if (root.get("whiteList") != null){
					patternElement.setWhiteList(parsePatternWhiteList(root));
				}
				if (root.get("blackList") != null){
					patternElement.setBlackList(parsePatternBlackList(root));
				}
			
				patternElement.setElementName(patternName);
				patternElement.setDescription(patternDescription);
				patternElement.setElementID("p"+String.valueOf(i+1));
			}
		}
		return patternElement;
		}
		
	public static Pattern parsePatternStructure(JSONObject root){
		ArrayList<PatternFlow> patternFlows = new ArrayList<>();
		
		Pattern patternElement = new Pattern();
		JSONArray pattern = (JSONArray) root.get("pattern");	
		for (int p=0; p < pattern.size(); p++){
		JSONObject patternObj = (JSONObject) pattern.get(p);
		if (patternObj.get("sequence") != null){
			PatternSequence patternSequence = parsePatternSequence(patternObj, root);
			patternElement.addPatternSubElement(patternSequence);
			
		} 
		else if (patternObj.get("fork") != null){
			patternFlows.addAll(parsePatternFlow(patternObj, root));	
			for (PatternFlow patternFlow: patternFlows){
				patternElement.addPatternSubElement(patternFlow);
		} 
		}
		}
		return patternElement;
	}
	
	
	public static PatternSequence parsePatternSequence(JSONObject patternObj, JSONObject root){
		ArrayList<String> stepValues = new ArrayList<>();
		PatternSequence patternSequence = new PatternSequence();
		JSONArray sequence = (JSONArray) patternObj.get("sequence");
			for (int s = 0; s < sequence.size(); s++){
				PatternStep patternStep = new PatternStep();
				JSONObject seqObj = (JSONObject) sequence.get(s);
				JSONObject step = (JSONObject) seqObj.get("s"+String.valueOf(s+1));
				String name = (String) step.get("name");
				JSONArray values = (JSONArray) step.get("values");
				for (int v = 0; v < values.size(); v++) {
					JSONObject valuesObj = (JSONObject) values.get(v);
					String value = (String) valuesObj.get("value");
					stepValues.add(value);
				}
				patternStep.setElementName(name);
				if (name.equals("$whiteList") || name.equals("*t")) {
					patternSequence.setWhiteList(parsePatternWhiteList(root));
					patternSequence.setBlackList(parsePatternBlackList(root));
				}
				
				patternStep.setParentElement(patternSequence);
				patternStep.setElementID("s"+String.valueOf(s+1));
				patternStep.setStepValues(new ArrayList<String>(stepValues));
			patternSequence.addPatternSubElement(patternStep);
			patternSequence.setElementName("Sequence");
			stepValues.clear();	
			}
		return patternSequence;
	}
	
	public static ArrayList<PatternFlow> parsePatternFlow(JSONObject patternObj, JSONObject root){
		ArrayList<String> stepValues = new ArrayList<>();
		ArrayList<PatternFlow> flows = new ArrayList<>();
		JSONArray fork = (JSONArray) patternObj.get("fork");
		for (int sf=0; sf < fork.size(); sf++){
			JSONObject forkObj = (JSONObject) fork.get(sf);
			JSONArray flow = (JSONArray) forkObj.get("flow"+String.valueOf(sf+1));
			PatternFlow patternFlow = new PatternFlow();
			patternFlow.setElementName("Flow");
			for (int f=0; f< flow.size(); f++){
				JSONObject flowObj = (JSONObject) flow.get(f);
				if (flowObj.get("repeat") != null) {
					String repeat = (String) flowObj.get("repeat");
					patternFlow.setPatternFlowRepeatValue(repeat);
				}
				if (flowObj.get("sequence") != null){
					PatternSequence flowSequence = parsePatternSequence(flowObj, root);
					flowSequence.setElementName("Sequence");
					if (root.get("blackList")!= null) flowSequence.setBlackList(parsePatternBlackList(root));
					if (root.get("whiteList")!= null) flowSequence.setWhiteList(parsePatternWhiteList(root));
					patternFlow.addPatternSubElement(flowSequence);
				} else if (flowObj.get("fork") != null){
					ArrayList<PatternFlow> subFlows = parsePatternFlow(flowObj, root);
					for (PatternFlow subFlow: subFlows){
					patternFlow.addPatternSubElement(subFlow);}
				}
				flows.add(patternFlow);
			}
		}	
		return flows;
	}
	
	public static ArrayList<BPMNElement> parseBPMNForEdges(String dictionaryFilePath){
		ArrayList<BPMNElement> bpmnElements = new ArrayList<>();
		JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
		Integer size = dictionary.size();
	
		for (int i = 0; i < size; i++) {
			JSONObject root = (JSONObject) dictionary.get(i);
			String patternName = (String) root.get("name");
			if (patternName.equals("edge")){
			bpmnElements.addAll(parsePatternBPMNElements(root));
		}
		}
		return bpmnElements;
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
				element.setAttribute(bpmnAttr);
			}
			String text = (String) bpmn.get("text");
			element.setText(text);
			bpmnElements.add(element);
		}			
		return bpmnElements;
	}
	
	public static HashMap<String, ArrayList<String>> parsePatternWhiteList (JSONObject root) {
		HashMap<String, ArrayList<String>> wListOperations = new HashMap<>();
		ArrayList<String> whiteListValues = new ArrayList<>();
		
		if (root.get("whiteList") != null){
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
		}
		return wListOperations;
	}
	
	public static HashMap<String, ArrayList<String>> parsePatternBlackList(JSONObject root){
		HashMap<String, ArrayList<String>> bListOperations = new HashMap<>();
		ArrayList<String> blackListValues = new ArrayList<>();
				if (root.get("blackList") != null){
				JSONArray bListArray = (JSONArray) root.get("blackList");
				if (root.get("blackList") != null){
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
				}
				}
		return bListOperations;
	}
}
