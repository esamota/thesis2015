
public class BPMNAttribute {

	public String name;
	public String value;

public BPMNAttribute(){
	
}

public BPMNAttribute(String attrName){
	name = attrName;
	value = "";
}

public BPMNAttribute(String attrName, String attrValue){
	name = attrName;
	value = attrValue;
}

public String getAttributeName(){
	return name;
}

public String getAttributeValue(){
	return value;
}

}
