import java.util.ArrayList;

import org.apache.commons.collections.ListUtils;


public class BPMNElement {
	
private String name;
private ArrayList<BPMNAttribute> attributes;


public BPMNElement(){
	
}

public BPMNElement(String elementName){
	name = elementName;
	attributes = new ArrayList<>();	
}

public void addAttribute(BPMNAttribute attr){
	attributes.add(attr);
}

public String getElementName(){
	return name;
}


public ArrayList<BPMNAttribute> getAttributes(){
	return attributes;
}

}
