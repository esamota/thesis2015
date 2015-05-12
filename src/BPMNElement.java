import java.util.ArrayList;

import org.apache.commons.collections.ListUtils;


public class BPMNElement {
	
private String name;
private ArrayList<BPMNAttribute> attributes;
private ArrayList<BPMNElement> subElements;
private String text;


public BPMNElement(){
	
}

public BPMNElement(String elementName){
	name = elementName;
	attributes = new ArrayList<>();	
	subElements = new ArrayList<>();
	text = "";
}

public void addAttribute(BPMNAttribute attr){
	attributes.add(attr);
}

public void addAttributes(ArrayList<BPMNAttribute> attrs){
	for (BPMNAttribute attr: attrs){
		attributes.add(attr);
	}
}

public void addSubElement(BPMNElement element){
	subElements.add(element);
}

public void addSubElements(ArrayList<BPMNElement> elements){
	for (BPMNElement el: elements){
		subElements.add(el);
	}
}

public void addText (String newText){
	this.text = newText;
}

public String getElementName(){
	return name;
}


public ArrayList<BPMNAttribute> getAttributes(){
	return attributes;
}

public ArrayList<BPMNElement> getSubElements(){
	return subElements;
}

public String getElementText(){
	return text;
}

}
