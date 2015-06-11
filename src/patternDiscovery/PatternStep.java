package patternDiscovery;
import java.util.ArrayList;
import java.util.Random;


public class PatternStep {

	private String stepName;
	private ArrayList<String> stepValues;
	
	public PatternStep (){
		stepName="";
		stepValues = new ArrayList<String>();
	}
	
	public void addStep (String name, String value){
		this.stepName = name;
		this.stepValues.add(value);
	}
	
	public void addStep (String name, ArrayList<String> values){
		this.stepName = name;
		for (String value: values){
			this.stepValues.add(value);
		}
	}
	
	public String getStepName(){
		return stepName;
	}
	
	public ArrayList<String> getStepValus(){
		return stepValues;
	}
}
