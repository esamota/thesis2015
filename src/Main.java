import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import operationDictionary.ETLOperationType;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;

import etlFlowGraph.graph.ETLEdge;
import etlFlowGraph.graph.ETLFlowGraph;
import importXLM.ImportXLMToETLGraph;
import etlFlowGraph.operation.*;

public class Main {
	public static String XLMFilePathInput = "C:\\Users\\Elena\\Desktop\\xLMexamples\\etl-initial_agn.xml";

	public static void main(String[] args) {

	}
}
