package toBPMN;
import java.util.ArrayList;

import org.apache.commons.collections.ListUtils;


public class BPMNElement {
	
private String name;
private ArrayList<BPMNAttribute> attributes;
private ArrayList<BPMNElement> subElements;
private String text;
private String id;


public BPMNElement(){
	name = "";
	attributes = new ArrayList<>();	
	subElements = new ArrayList<>();
	text = "";
	id="";
}

public BPMNElement(String elementName){
	name = elementName;
	attributes = new ArrayList<>();	
	subElements = new ArrayList<>();
	text = "";
	id="";
}

public void setAttribute(BPMNAttribute attr){
	attributes.add(attr);
}

public void setAttributes(ArrayList<BPMNAttribute> attrs){
	for (BPMNAttribute attr: attrs){
		this.attributes.add(attr);
	}
}

public void setSubElement(BPMNElement element){
	subElements.add(element);
}

public void setSubElements(ArrayList<BPMNElement> elements){
	for (BPMNElement el: elements){
		subElements.add(el);
	}
}

public void setText (String newText){
	this.text = newText;
}

public void setID (String newID){
	this.id = newID;
}

public String getElementName(){
	return name;
}

public String getElementID(){
	return id;
}

public ArrayList<BPMNAttribute> getAttributes(){
	return attributes;
}

public ArrayList<BPMNElement> getSubElements(){
	return subElements;
}

public String getElementText(){
	return this.text;
}

}
