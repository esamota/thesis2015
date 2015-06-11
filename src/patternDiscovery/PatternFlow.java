package patternDiscovery;

public class PatternFlow {
	
private String flowID;
private String repeat;
private Integer counter = 1;
private PatternSequence flowSequence;

public PatternFlow(){
	this.repeat = "";
	this.flowID = "f"+counter;
	this.flowSequence = new PatternSequence();
	this.counter = counter +1;
}
public void addPatternFlowSequence(PatternSequence sequence){
	this.flowSequence = sequence;
}

public void addPatternFlowRepeatValue(String repeat){
	this.repeat = repeat;
}

public String getFlowRepeatValue(){
	return this.repeat;
}

public PatternSequence getFlowSequence(){
	return this.flowSequence;
}

}
