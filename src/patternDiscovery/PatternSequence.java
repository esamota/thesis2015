package patternDiscovery;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import etlFlowGraph.graph.ETLFlowGraph;
import etlFlowGraph.operation.ETLFlowOperation;

public class PatternSequence extends PatternElement{
	
	public PatternSequence(){
		super();
	}
	

	public ArrayList<ETLFlowOperation> match(ETLFlowOperation node, ETLFlowGraph G, ArrayList<ETLFlowOperation> patternNodes){
		Hashtable<Integer, ETLFlowOperation> ops = G.getEtlFlowOperations();
		ArrayList<ETLFlowOperation> outPatternNodes = new ArrayList();
		System.out.println("Seq: we are at node "+ node.getNodeID());
		System.out.println("Seq size: "+ getSubElements().size());
		
		outPatternNodes.addAll(patternNodes);
		ETLFlowOperation nextNode = node;
		for (int s=0; s < getSubElements().size(); s++){
			if (s < getSubElements().size()-1 && utilities.XLMParser.getTargetOperationsGivenSource(node, G).size() > 1){
				System.out.println("More than one traget node in a sequence");
				return new ArrayList<>();
			}
			outPatternNodes = getSubElements().get(s).match(nextNode, G, outPatternNodes);
			
			if (outPatternNodes.size() == 0 || outPatternNodes.size() == patternNodes.size()){
				System.out.println("Seq match failed");
				return new ArrayList<>();
			}
			if(outPatternNodes.get(outPatternNodes.size()-1).getOperationName().equals("whiteList")){
				Integer nextNodeID = outPatternNodes.get(outPatternNodes.size()-1).getNodeID();
				outPatternNodes.remove(outPatternNodes.size() - 1);
				//old:
				//nextNode = utilities.XLMParser.getTargetOperationsGivenSource(outPatternNodes.get(outPatternNodes.size() - 1), G).get(0);
				//new:
				/*Iterator<Integer> graphIter = G.iterator();
				while (graphIter.hasNext()) {
					Integer v = graphIter.next();
					if (v.intValue() == outPatternNodes.get(outPatternNodes.size() - 1).getNodeID()){
					nextNode = ops.get(graphIter.next());
					break;
					}
				}*/ 
				nextNode = ops.get(nextNodeID);
				if (s < getSubElements().size()){
					for (ETLFlowOperation op: outPatternNodes){
						if (!patternNodes.contains(op)) patternNodes.add(op);
					}
				}
			} else patternNodes.add(node);
			
			if (s < getSubElements().size()-1 && !getSubElements().get(s).getElementName().equals("$whiteList") && 
					!getSubElements().get(s).getElementName().equals("$anyType")){
				if (utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).size() <= 1){
					//old:
					//nextNode = utilities.XLMParser.getTargetOperationsGivenSource(nextNode, G).get(0);
					//new:
					Iterator<Integer> graphIter = G.iterator();
				while (graphIter.hasNext()) {
					Integer v = graphIter.next();
					if (v.intValue() == node.getNodeID()){
						if (graphIter.hasNext()) nextNode = ops.get(graphIter.next());
						else nextNode = new ETLFlowOperation("dummy");
						break;
					}
				}
				}
				else return patternNodes;
			}
		}
		return outPatternNodes;
		}

}
