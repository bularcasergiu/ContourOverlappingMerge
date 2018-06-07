/**
 * 
 */
package org.openstreetmap.josm.plugins.ContourOverlappingMerge;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;









import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.gui.Notification;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.I18n;





/**
 * @author Sergiu Bularca
 *
 */
public class ContourOverlappingAction extends JosmAction {
	MapFrame thisMapFrame;
	public ContourOverlappingAction(){
		super(tr("ContourOverlappedMerge"), "dialogs/ContourOverlapping1.png",
		        tr("This plugin merge two overlapped contours.First selected contour is priority."),
		        Shortcut.registerShortcut("menu:ContourOverlappingMerge", tr("Menu: {0}", tr("ContourOverlappingMerge")),
		        KeyEvent.VK_0, Shortcut.DIRECT), true);
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		ContourOverlappingDialog dialog = new ContourOverlappingDialog();
		  List<Command> cmds = new LinkedList<>();
		  Pair<List<Node>,Way>  Inter1 = Geometry1.IntersectionSegmentsCoors(dialog.ListingWay(), false, cmds);
		  List<Node>in=Inter1.getKey();
		  List<org.openstreetmap.josm.data.osm.Node> Inter=dialog.readIntersections(in);
		  Way out4=Inter1.getValue();
		
		  List<Way> way4=dialog.getWay4();
		  List<Way> way5=dialog.getWay5();
			
		  if(Inter.size()<=1){
			  new Notification(I18n.tr("Overlapping don't exist", new Object[0])).setIcon(2).show();
			    return;	
		  } 
		  
		  if(dialog.NumberOfExistingIntersectionPoints(Inter)==Inter.size()){
			  
			  Pair<List<Node>,List<Node>>w=dialog.WaysIfExistsAllIntersections(Inter, way4, way5);
			  List<Node> w4_1=w.getKey();
			  List<Node> w5_1=w.getValue();
			  if(Inter.size()<=2){
				  dialog.EraseOverlapping(w4_1,w5_1,Inter,cmds); 
			  }
			  else{
				  if(!dialog.FirstPointInsideArea(w4_1, w5_1, Inter, cmds)){
					  new Notification(I18n.tr("Plugin can't merge contour overlapped becuse firs point to the second selected contour is inside first contour.", new Object[0])).setIcon(2).show();
					  new Notification(I18n.tr("Please redrow second contour,and put first point to the non-priority contour outside the priority contour", new Object[0])).setIcon(2).show();
					  return;
				  }
				  else{
					  dialog.EraseOverlapping(w4_1,w5_1,Inter,cmds); 
				  } 
			  }
			  
			  
		  }
		  else{
			  Pair<List<Node>,List<Node>>w=dialog.WaysIfDoNotExistAllIntersections(out4, Inter, way4, way5);
			  List<Node> w4_1=w.getKey();
			  List<Node> w5_1=w.getValue();
			  if(Inter.size()<=2){
				  dialog.EraseOverlapping2Intersections(w4_1, w5_1, Inter, cmds);
			  }
			  else{
				  if(!dialog.FirstPointInsideArea(w4_1, w5_1, Inter, cmds)){
					  new Notification(I18n.tr("Plugin can't merge contour overlapped becuse firs point to the second selected contour is inside first contour.", new Object[0])).setIcon(2).show();
					  new Notification(I18n.tr("Please redrow second contour,and put first point to the non-priority contour outside the priority contour", new Object[0])).setIcon(2).show();
					  return;
				  }
				  else{
					  dialog.EraseOverlapping(w4_1,w5_1,Inter,cmds); 
				  }
				  
			  }
		  }
		  
		  
		  
		  Main.main.undoRedo.add(new SequenceCommand("crrr", cmds));
	      MainApplication.getMap().repaint();
		  
		new Notification(I18n.tr("This plugin merge two overlapped contours.First selected contour is priority", new Object[0])).setIcon(2).show();
	    return;	
	}
	  
}
