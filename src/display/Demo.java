package display;

import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JRadioButton;
import javax.swing.JTextPane;

import toBPMN.BPMNConstructsGenerator;
import toBPMN.BPMNConstructsToFile;
import toBPMN.BPMNElement;
import utilities.JSONDictionaryParser;
import etlFlowGraph.graph.ETLFlowGraph;

import javax.swing.JLabel;

public class Demo {

	private JFrame frame;
	private JButton btnUploadFile, btnTranslateToBpmn;
	JRadioButton rdbtnSemanticPatterns, rdbtnDirectTranslation, rdbtnPipelinedSubprocesses;
	JLabel lblSelectOneTranslation;
    private JFileChooser fc = new JFileChooser("C:\\Users\\Elena\\Desktop");
    private File xlmFile; 
    public static  String dictionaryFilePath = "mappings//semanticPatternDictionary.json";
    public static String XLMFilePathInput = "C:\\Users\\Elena\\Dropbox\\Thesis 2015 Samota - ETL Conceptualization and Standardization\\kettle example\\modified by Petar\\etl-all-patterns-2.xml";
    public static  String BPMNFilePathOutput = "C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn";
    public static String patternFlagMappingPath = "mappings//patternFlags.json";
    public static HashMap<String, ArrayList<String>> flagMappings = new HashMap<>();
    private ETLFlowGraph G;
    private int clicked = 0; 

    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Demo window = new Demo();
					window.rdbtnPipelinedSubprocesses.setVisible(false);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Demo() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnUploadFile = new JButton("Upload File");
		btnUploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//handle upload button
				if (e.getSource() == btnUploadFile) {
				fc = new JFileChooser("C:\\Users\\Elena\\Desktop");
				int returnVal = fc.showOpenDialog(frame);

			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			         xlmFile = fc.getSelectedFile();
			         XLMFilePathInput = xlmFile.getAbsolutePath();
			         G = utilities.XLMParser.getXLMGraph(XLMFilePathInput);
			            //This is where a real application would open the file.
			         JOptionPane.showMessageDialog(frame,
			        		    "File uploaded successfully.");
			        }
			}
			}
		});
		btnUploadFile.setBounds(157, 59, 115, 23);
		frame.getContentPane().add(btnUploadFile);
		
		btnTranslateToBpmn = new JButton("Translate to BPMN");
		btnTranslateToBpmn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//handle upload button
				clicked++;
				if (e.getSource() == btnTranslateToBpmn) {
					
				flagMappings = JSONDictionaryParser.parsePatternFlags(patternFlagMappingPath);
				ArrayList<BPMNElement> graphElements = patternDiscovery.PatternDiscovery.translateToBPMN(G, flagMappings, dictionaryFilePath);
				String BPMN = BPMNConstructsToFile.toStringBPMNWithDictionary(G, graphElements);
				
				//this lets you choose where to save the file
				fc = new JFileChooser("C:\\Users\\Elena\\Desktop");
				fc.setSelectedFile(new File("output.bpmn"));
				int returnVal = fc.showSaveDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
			        File file = fc.getSelectedFile();
			        BPMNFilePathOutput = file.getAbsolutePath();
			        BPMNConstructsToFile.toFileBPMN(BPMNFilePathOutput, BPMN);
			      }
				//only start yaoqiang the first time
				if (clicked == 1) {
				try {
					Runtime.getRuntime().exec("java -jar yaoqiang.jar");
					JOptionPane.showMessageDialog(frame,
		        		    "Opening BPMN Editor.");
					//Runtime.getRuntime().exec("cmd /c \"C:\\Users\\Elena\\Desktop\\xLMtoBPMNtest.bpmn\"");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
				JOptionPane.showMessageDialog(frame,
	        		    "File translated successfully.");
				
			}
			}
		});
		btnTranslateToBpmn.setBounds(144, 157, 168, 23);
		frame.getContentPane().add(btnTranslateToBpmn);
		
		rdbtnSemanticPatterns = new JRadioButton("Semantic Patterns");
		rdbtnSemanticPatterns.setBounds(237, 113, 158, 23);
		frame.getContentPane().add(rdbtnSemanticPatterns);
		
		rdbtnPipelinedSubprocesses = new JRadioButton("Pipelined Subprocesses");
		rdbtnPipelinedSubprocesses.setBounds(292, 7, 136, 23);
		frame.getContentPane().add(rdbtnPipelinedSubprocesses);
		
		rdbtnDirectTranslation = new JRadioButton("Direct Translation");
		rdbtnDirectTranslation.setBounds(50, 113, 185, 23);
		frame.getContentPane().add(rdbtnDirectTranslation);
		
		lblSelectOneTranslation = new JLabel("Select one translation option:");
		lblSelectOneTranslation.setBounds(55, 93, 168, 14);
		frame.getContentPane().add(lblSelectOneTranslation);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnSemanticPatterns);
		group.add(rdbtnPipelinedSubprocesses);
		group.add(rdbtnDirectTranslation);
		
		rdbtnSemanticPatterns.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dictionaryFilePath = "mappings//semanticPatternDictionary.json";
				patternFlagMappingPath = "mappings//patternFlags.json";
				flagMappings = JSONDictionaryParser.parsePatternFlags(patternFlagMappingPath);
				System.out.println(dictionaryFilePath);
				System.out.println(patternFlagMappingPath);
			}
		});
		
		rdbtnPipelinedSubprocesses.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dictionaryFilePath = "mappings//subprocessDictionary.json";
				patternFlagMappingPath = "mappings//subprocessFlags.json";
				flagMappings = JSONDictionaryParser.parsePatternFlags(patternFlagMappingPath);
				System.out.println(dictionaryFilePath);
			}
		});
		
		rdbtnDirectTranslation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dictionaryFilePath = "mappings//simplePatternDictionary.json";
				patternFlagMappingPath = "mappings//simplePatternFlags.json";
				flagMappings = JSONDictionaryParser.parsePatternFlags(patternFlagMappingPath);
				System.out.println(dictionaryFilePath);
			}
		});
	}
}
