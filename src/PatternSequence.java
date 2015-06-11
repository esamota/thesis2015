import java.util.ArrayList;

public class PatternSequence {

private Integer counter;
private String stepID;
private PatternStep seqStep;
private ArrayList<PatternStep> seqSteps;
 
	public PatternSequence(){
		counter = 1;
		stepID = "";
		seqStep = new PatternStep();
		seqSteps = new ArrayList<>();
	}
	
	public void addSequenceStep(PatternStep step){
		this.stepID = "s"+counter;
		this.seqStep = step;
		counter = counter+1;
	}
	
	public void addSequenceSteps(ArrayList<PatternStep> steps){
		for (PatternStep step: steps){
			this.stepID = "s"+counter;
			this.seqSteps.add(step);
			counter = counter+1;
			}
		}
		
	public ArrayList<PatternStep> getSequenceSteps(){
		return seqSteps;	
	}

}
