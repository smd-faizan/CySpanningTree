package org.cytoscape.spanningtree.internal.clustering;

import java.util.Collections;
import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.spanningtree.internal.SpanningTreeStartMenu;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class EdgeRemoval extends Thread{
    public boolean stop;
    public CyNetwork currentnetwork;
    boolean larger;
    String edgeWeightAttribute;
    SpanningTreeStartMenu menu;
    
    public EdgeRemoval(CyNetwork currentnetwork, boolean larger, String edgeWeightAttribute, SpanningTreeStartMenu menu){
        this.currentnetwork = currentnetwork;
        this.larger = larger;
        this.edgeWeightAttribute = edgeWeightAttribute;
        this.menu = menu;
    }

    @Override
    public void run() {
        menu.calculatingresult("Removing edge");
        stop = false;
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
                menu.endOfComputation("Largest Edge succesfully deleted");
            else
                menu.endOfComputation("problem in deleting largest edge");
        }
    }
    
    public void end(){
        stop = true;
    }
}
