import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class BPMNConstructs {
	
	public static File writeBPMNFileWithHeader() {
		File file = new File("C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn");
		try{
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+'\n'+
		"<definitions id=\"Definition\""+'\n'+'\t'+
        "targetNamespace=\"http://www.example.org/MinimalExample\""+'\n'+'\t'+
        "typeLanguage=\"http://www.java.com/javaTypes\""+'\n'+'\t'+
        "expressionLanguage=\"http://www.mvel.org/2.0\""+'\n'+'\t'+
        "xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""+'\n'+'\t'+
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\""+'\n'+'\t'+
        "xs:schemaLocation=\"http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd\">"+'\n'+
        "<process processType=\"Private\" isExecutable=\"false\" id=\"test\" name=\"Test\">\n";
		 
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(header);
		bw.close();
		}
	catch (IOException e) {
		e.printStackTrace();
	}
		return file;
	}
	
}
