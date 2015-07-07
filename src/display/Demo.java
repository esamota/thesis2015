package display;

import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JRadioButton;
import javax.swing.JTextPane;

import toBPMN.BPMNConstructsGenerator;
import toBPMN.BPMNConstructsToFile;
import etlFlowGraph.graph.ETLFlowGraph;

public class Demo {

	private JFrame frame;
	private JButton btnUploadFile, btnTranslateToBpmn;
    private JFileChooser fc;
    private File xlmFile; 

    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Demo window = new Demo();
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
		//Create the log first, because the action listeners
	    //need to refer to it.
	  
		
		btnUploadFile = new JButton("Upload File");
		btnUploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//handle upload button
				if (e.getSource() == btnUploadFile) {
				int returnVal = fc.showOpenDialog(frame);

			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			         xlmFile = fc.getSelectedFile();
			            //This is where a real application would open the file.
			        }
			}
			}
		});
		btnUploadFile.setBounds(157, 69, 115, 23);
		frame.getContentPane().add(btnUploadFile);
		
		btnTranslateToBpmn = new JButton("Translate to BPMN");
		btnTranslateToBpmn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//handle upload button
				if (e.getSource() == btnTranslateToBpmn) {
				ETLFlowGraph G = utilities.XLMParser.getXLMGraph(xlmFile.getAbsolutePath());
				patternDiscovery.PatternDiscovery.translateToBPMN(G);
				String BPMN = BPMNConstructsToFile.toStringBPMNWithDictionary(G);
				BPMNConstructsToFile.toFileBPMN(BPMN);
			}
			}
		});
		btnTranslateToBpmn.setBounds(157, 117, 121, 23);
		frame.getContentPane().add(btnTranslateToBpmn);
	}
}
