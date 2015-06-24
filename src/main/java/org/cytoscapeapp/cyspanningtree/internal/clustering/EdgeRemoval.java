package org.cytoscapeapp.cyspanningtree.internal.clustering;

import java.util.Collections;
import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscapeapp.cyspanningtree.internal.SpanningTreeStartMenu;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class EdgeRemoval extends Thread{
    public boolean stop;
    public CyNetwork currentnetwork;
    boolean larger;
    String edgeWeightAttribute;
    int edgesToRemove;
    SpanningTreeStartMenu menu;
    
    public EdgeRemoval(CyNetwork currentnetwork, boolean larger, String edgeWeightAttribute, int edgesToRemove, SpanningTreeStartMenu menu){
        this.currentnetwork = currentnetwork;
        this.larger = larger;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.edgesToRemove = edgesToRemove;
        this.menu = menu;
    }

    @Override
    public void run() {
        menu.calculatingresult("Removing edge");
        stop = false;
        
        for(int i=1; i<= edgesToRemove ; i++){
            List<CyEdge> edgeList = currentnetwork.getEdgeList();
            CyTable edgeTable = currentnetwork.getDefaultEdgeTable();

            if(larger){
                // find the largest edge
                double largestEdgeWeight = Double.MIN_VALUE;
                double temp;
                CyEdge largestEdge = null;
                CyRow largestRow = null;
                for(CyEdge edge: edgeList){
                    if(stop)
                        return;
                    CyRow row = edgeTable.getRow(edge.getSUID());
                        try {
                            temp = Double.parseDouble(""+ row.get(edgeWeightAttribute, SpanningTreeStartMenu.edgeWeightAttributeColumn.getType()));
                            if(largestEdgeWeight < temp){
                                largestEdgeWeight = temp;
                                largestEdge = edge;
                                largestRow = row;
                            }
                        } catch (NumberFormatException ex) {
                        }
                }
                // remove the largest edge
                //edgeTable.deleteRows(Collections.singletonList(edgeTable.getRow(largestEdge.getSUID())));
                edgeTable.deleteRows(Collections.singletonList(largestRow.getRaw("SUID")));
                if(currentnetwork.removeEdges(Collections.singletonList(largestEdge)))
                    menu.calculatingresult(i+"th largest edge removed");
                else
                    menu.calculatingresult("problem in deleting "+i+"th"+"largest edge");
            }
        }
        menu.endOfComputation(edgesToRemove+" large edges removed! Remaining edges = "+currentnetwork.getEdgeCount());
    }
    
    public void end(){
        stop = true;
    }
}
