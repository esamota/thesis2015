package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import operationDictionary.OperationTypeName;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import patternDiscovery.PatternSequence;
import patternDiscovery.PatternStep;
import toBPMN.BPMNAttribute;
import toBPMN.BPMNElement;

public class DictionaryParser2 {

	// private static String dictionaryFilePath =
		// "C:\\Users\\Elena\\Desktop\\testForQ1.json";
		public static final String dictionaryFilePath = "mappings//newJSONDictionary.json";
		private static ArrayList<BPMNAttribute> attributes = new ArrayList();

		public static void main(String[] args) {
			// call method
			HashMap<String, ArrayList<BPMNElement>> mapping = parseSingleOperationPatterns();
			HashMap<String, ArrayList<String>> flagMapping = getOperatorPatternFlags();
			
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
		public static HashMap<String, ArrayList<BPMNElement>> parseSingleOperationPatterns() {
			HashMap<String, ArrayList<BPMNElement>> mapping = new HashMap<String, ArrayList<BPMNElement>>();
			BPMNElement element = new BPMNElement();
			ArrayList<BPMNElement> elementsPerOptype = new ArrayList<BPMNElement>();
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
					
					if (operations.contains(patternName) || patternName.equals("edge")){
					JSONArray pattern = (JSONArray) root.get("pattern");
					for (int p = 0; p < pattern.size(); p++) {
					JSONObject patternObj = (JSONObject) pattern.get(p);
					JSONArray sequence = (JSONArray) patternObj.get("sequence");
						for (int seq = 0; seq < sequence.size(); seq++) {
						JSONObject sequenceObj = (JSONObject) sequence.get(seq);
						JSONObject step1 = (JSONObject) sequenceObj.get("s1");
						name = (String) step1.get("name");
						JSONArray values = (JSONArray) step1.get("values");
							for (int v = 0; v < values.size(); v++) {
							JSONObject valuesObj = (JSONObject) values.get(v);
							value = (String) valuesObj.get("value");
							}
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
						elementsPerOptype.add(element);
					}
					//System.out.println(xlmName+" "+ elementsPerOptype);
					if (patternName.equals("edge")) mapping.put(name, new ArrayList<BPMNElement>(elementsPerOptype));	
					else mapping.put(value, new ArrayList<BPMNElement>(elementsPerOptype));
					elementsPerOptype.clear();
				}
				} 
			return mapping;
		}
		//works
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
				return flagMapping;
		}
		
		//TODO:change to new format
		/*public static HashMap<String, ArrayList<String>> parseJSONPatternOriginOptypes(){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			HashMap<String, ArrayList<String>> patternOriginOptype = new HashMap<String, ArrayList<String>>();
			ArrayList<String> optypeValues = new ArrayList<String>(); 
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				String description = (String) root.get("description");
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
		}*/
		
		//old need to change
		public static ArrayList<String> getPatternOrigin(String name){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
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
		//ok
		public static String getPatternDescription(String patternName){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			String description = "";
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String name = (String) root.get("name");
				if (patternName.equals(name)) description = (String) root.get("description");
			}
			return description;
		}
		// no longer relevant
		public static Integer getNumberOfPatternStructuralElements(String flagName){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer numOfPatternElements = 0;
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");	
					numOfPatternElements = pattern.size();
				}
			}
			return numOfPatternElements;	
		}
		
		public static Integer getNumOfStepsInFirstSequence(String flagName){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer numOfSteps = 0;
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");	
					JSONObject patternObj = (JSONObject) pattern.get(0);
					JSONArray sequence = (JSONArray) patternObj.get("sequence");
					numOfSteps = sequence.size();
				}
			}
				return numOfSteps;
		}
		
		public static Integer getNumOfStepsInLastSequence(String flagName){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer numOfSteps = 0;
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");
					if (pattern.size() > 2){
					JSONObject patternObj = (JSONObject) pattern.get(2);
					JSONArray sequence = (JSONArray) patternObj.get("sequence");
					numOfSteps = sequence.size();
				}
				}
			}
				return numOfSteps;
		}
		
		public static Integer getNumberOfSplitFlows(String flagName){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer numOfFlows = 0;
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");
					for(int p = 0; p < pattern.size(); p++) {
						JSONObject patternObj = (JSONObject) pattern.get(p);
						if (patternObj.get("splitFlow") != null){
						JSONArray splitFlows =(JSONArray) patternObj.get("splitFlow");
						numOfFlows = splitFlows.size();
						}
				}
			}
			}
			return numOfFlows;
		}
		
		public static Integer getNumberOfStepsInAFlow(String flagName, Integer flowID){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer numOfStepsInAFlow = 0;
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");
					for (int p = 0; p < pattern.size(); p++){
						JSONObject patternObj = (JSONObject) pattern.get(p);
						if (patternObj.get("splitFlow") != null){
							JSONArray splitFlow = (JSONArray) patternObj.get("splitFlow");
							JSONObject splitFlowObj = (JSONObject) splitFlow.get(flowID);	
							JSONArray flow = (JSONArray) splitFlowObj.get("flow"+String.valueOf((flowID+1)));
							for (int f=0; f< flow.size();f++){
								JSONObject flowObj = (JSONObject) flow.get(f);
								
								JSONArray sequence = (JSONArray) flowObj.get("sequence");
								numOfStepsInAFlow = sequence.size();
							}}
							}}
						}
			return numOfStepsInAFlow;
		}
		
		public static HashMap<String, ArrayList<String>> getFirstSequenceSteps (String flagName, Integer stepID){
			HashMap<String, ArrayList<String>> firstSequenceSteps = new HashMap<>();
			ArrayList<String> stepValues = new ArrayList<>();
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			Integer patternSize = 0;
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");	
						for (int p=0; p < pattern.size(); p++){
						JSONObject patternObj = (JSONObject) pattern.get(0);
						if (patternObj.get("sequence")!= null){
						JSONArray sequence = (JSONArray) patternObj.get("sequence");
							JSONObject seqObj = (JSONObject) sequence.get(stepID);
							JSONObject step = (JSONObject) seqObj.get("s"+ String.valueOf(stepID+1));
							String name = (String) step.get("name");
							JSONArray values = (JSONArray) step.get("values");
								for (int v = 0; v < values.size(); v++) {
									JSONObject valuesObj = (JSONObject) values.get(v);
									String value = (String) valuesObj.get("value");
									stepValues.add(value);
							}
								firstSequenceSteps.put(name, new ArrayList<String>(stepValues));
								stepValues.clear();
						}}}
				}
			return firstSequenceSteps;
			}
		
		public static HashMap<String, ArrayList<String>> getLastSequenceSteps (String flagName, Integer stepID){
			HashMap<String, ArrayList<String>> lastSequenceSteps = new HashMap<>();
			ArrayList<String> stepValues = new ArrayList<>();
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");	
						if (pattern.size() > 2){
						JSONObject patternObj = (JSONObject) pattern.get(2);
						if (patternObj.get("sequence")!= null){
						JSONArray sequence = (JSONArray) patternObj.get("sequence");
							JSONObject seqObj = (JSONObject) sequence.get(stepID);
							JSONObject step = (JSONObject) seqObj.get("s"+ String.valueOf(stepID+1));
							String name = (String) step.get("name");
							JSONArray values = (JSONArray) step.get("values");
								for (int v = 0; v < values.size(); v++) {
									JSONObject valuesObj = (JSONObject) values.get(v);
									String value = (String) valuesObj.get("value");
									stepValues.add(value);
							}
								lastSequenceSteps.put(name, new ArrayList<String>(stepValues));
								stepValues.clear();
						}}}
				}
			return lastSequenceSteps;
			}
		
		public static HashMap<String, ArrayList<String>> getSplitFlowSteps (String flagName, Integer flowID, Integer stepID){
			HashMap<String, ArrayList<String>> splitFlowSteps = new HashMap<>();
			ArrayList<String> stepValues = new ArrayList<>();
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");
					for (int p = 0; p < pattern.size(); p++){
						JSONObject patternObj = (JSONObject) pattern.get(p);
						if (patternObj.get("splitFlow") != null){
							JSONArray splitFlow = (JSONArray) patternObj.get("splitFlow");
								JSONObject splitFlowObj = (JSONObject) splitFlow.get(flowID);
								JSONArray flow = (JSONArray) splitFlowObj.get("flow"+String.valueOf(flowID+1));
								for (int q=0; q< flow.size(); q++){
								JSONObject flowObj = (JSONObject) flow.get(q);
								
								JSONArray sequence = (JSONArray) flowObj.get("sequence");
								JSONObject seqObj = (JSONObject) sequence.get(stepID);
								JSONObject step = (JSONObject) seqObj.get("s"+ String.valueOf(stepID+1));
								String name = (String) step.get("name");
								JSONArray values = (JSONArray) step.get("values");
								for (int v = 0; v < values.size(); v++) {
									JSONObject valuesObj = (JSONObject) values.get(v);
									String value = (String) valuesObj.get("value");
									stepValues.add(value);
							}
								splitFlowSteps.put(name, new ArrayList<String>(stepValues));
								stepValues.clear();
						}
				}}
			}}
			return splitFlowSteps;
			}
		
		public static String getFlowRepeatValue(String flagName, Integer flowID){
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			String repeat="";
			
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if(patternName.equals(flagName)){
					JSONArray pattern = (JSONArray) root.get("pattern");
					for (int p = 0; p < pattern.size(); p++){
						JSONObject patternObj = (JSONObject) pattern.get(p);
						if (patternObj.get("splitFlow") != null){
							JSONArray splitFlow = (JSONArray) patternObj.get("splitFlow");
								JSONObject splitFlowObj = (JSONObject) splitFlow.get(flowID);
								JSONArray flow = (JSONArray) splitFlowObj.get("flow"+String.valueOf(flowID+1));
								for (int q=0; q< flow.size(); q++){
								JSONObject flowObj = (JSONObject) flow.get(q);
								if (flowObj.get("repeat") != null) repeat = (String) flowObj.get("repeat");
								}
						}
					}
				}
			}
			return repeat;
		}
		
		public static HashMap<String, ArrayList<String>> getWhiteListItems(String flagName){
			HashMap<String, ArrayList<String>> wListOperations = new HashMap<>();
			ArrayList<String> whiteListValues = new ArrayList<>();
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if (patternName.equals(flagName)) {
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
				}
		}
			return wListOperations;
		}
		
		public static HashMap<String, ArrayList<String>> getBlackListItems(String flagName){
			HashMap<String, ArrayList<String>> bListOperations = new HashMap<>();
			ArrayList<String> blackListValues = new ArrayList<>();
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
			Integer size = dictionary.size();
			for (int i = 0; i < size; i++) {
				JSONObject root = (JSONObject) dictionary.get(i);
				String patternName = (String) root.get("name");
				if (patternName.equals(flagName)) {
					if (root.get("blackList") != null){
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
					}
				}
				}
			return bListOperations;
		}
		//based on the pattern name, returns name-value pairs for each version-flow-step combination
		public static HashMap<String, ArrayList<String>> parseJSONPatternSteps(
				String flagName, Integer versionID, Integer flowID, Integer stepID) {
			JSONArray dictionary = getJSONRootObject(dictionaryFilePath, "patternDictionary");
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
