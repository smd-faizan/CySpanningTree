/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cytoscape.spanningtree.internal.visuals;

import org.cytoscape.spanningtree.internal.CyActivator;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 *
 * @author smd.faizan@gmail.com
 */
public class Saloon {
    
    public static void applyStyle(CyNetworkView myView){
        // Apply style
        // To create a new VisualStyle object and set the mapping function
        VisualStyle vs= CyActivator.visualStyleFactoryServiceRef.createVisualStyle("My visual style");
        //Use pass-through mapping
        String ctrAttrName1 = "SUID";
        PassthroughMapping pMapping = (PassthroughMapping) CyActivator.vmfFactoryP.createVisualMappingFunction(ctrAttrName1, String.class, BasicVisualLexicon.NETWORK);
        //ContinuousMapping cMapping = (ContinuousMapping) CyActivator.vmfFactoryC.createVisualMappingFunction(ctrAttrName1, String.class, BasicVisualLexicon.NODE_LABEL);
        DiscreteMapping dMapping = (DiscreteMapping)CyActivator.vmfFactoryD.createVisualMappingFunction(ctrAttrName1, String.class, BasicVisualLexicon.NETWORK);
        //vs.addVisualMappingFunction(pMapping);
        vs.addVisualMappingFunction(dMapping);
        vs.addVisualMappingFunction(pMapping);
        // Add the new style to the VisualMappingManager
        CyActivator.vmmServiceRef.addVisualStyle(vs);
        // Apply the visual style to a NetwokView
        vs.apply(myView);
        myView.updateView();
    }
}
