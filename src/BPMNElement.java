import java.util.ArrayList;


public class BPMNElement {
	
private String name;
private ArrayList<BPMNAttribute> fixedAttributes;
private ArrayList<BPMNAttribute> variableAttributes;

public BPMNElement(){
	
}

public BPMNElement(String elementName){
	name = elementName;
	fixedAttributes = new ArrayList<>();
	variableAttributes = new ArrayList<>();
	
}

public void addFixedAttribute(BPMNAttribute fixedAttr){
	fixedAttributes.add(fixedAttr);
}

public void addVariableAttribute(BPMNAttribute variableAttr){
	variableAttributes.add(variableAttr);
}

public String getElementName(){
	return name;
}

public ArrayList<BPMNAttribute> getFixedAttributes(){
	return fixedAttributes;
}

public ArrayList<BPMNAttribute> getVariableAttributes(){
	return variableAttributes;
}
}
