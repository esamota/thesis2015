import java.util.Random;

public class Pattern {

	private String patternName;
	private Integer patternID;

	public Pattern() {

	}

	public Pattern(String name) {
		Random randomGenerator = new Random();
		this.patternName = name;
		this.patternID = Integer.valueOf("_0" + randomGenerator.nextInt(100));
	}

	public void setPatternName(String name) {
		this.patternName = name;
	}

	public void setPatternID(Integer id) {
		this.patternID = id;
	}

	public String getPatternName() {
		return this.patternName;
	}

	public Integer getPatternID() {
		return this.patternID;
	}

}
