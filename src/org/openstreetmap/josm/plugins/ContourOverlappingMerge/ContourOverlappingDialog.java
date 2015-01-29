package org.openstreetmap.josm.plugins.ContourOverlappingMerge;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;

import javafx.util.Pair;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.AddPrimitivesCommand;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.Geometry;

import com.sun.glass.events.MouseEvent;


public class ContourOverlappingDialog extends JPanel{
	MapView mv;
	public JTextArea pen1;
	public JOptionPane epsi;
	public JTextArea pen2;
	public JTextArea pen3;
	public Object[] p;
	public Collection<String> x;
	private JOptionPane optionPane;
	public static List<Command> cmm=new ArrayList<>();
	
	
	
	public  DataSet getDataSet()
	  {
	    return Main.main.getCurrentDataSet();
	  }
	public DataSet dataset = getDataSet();
	 public Collection<Way> way = dataset.getSelectedWays();
	
	  /**
	   * add selection parameters in an List<way>
	   * 
	   * @return List<Way>
	   */
	  public  List<Way> ListingWay(){
			List<Way> waylist=new ArrayList<>();
		    for (Way w : way){
		    	waylist.add(w);
			}
		    return waylist;
	  }
	  /**
	   * add first selection parameters in an List<way>
	   * 
	   * @return List<Way>
	   */
	  public  List<Way> getWay4(){
		  List<Way> waylist=new ArrayList<>();
		  List<Way> way4=new LinkedList<>();
		  Way[] way3 = new Way[2] ;
		  int c=0;
		    for (Way w : way){
		    	waylist.add(w);
		    	way3[c]=w;
				 c++;
			  }
		    way4.add(way3[0]);
		    System.out.println("way4"+way4);
		    return way4;
	  }
	  public List<Way> way4=getWay4();
	  
	  /**
	   * add second selection parameters in an List<way>
	   * 
	   * @return List<Way>
	   */
	  public List<Way> getWay5(){
		  List<Way> waylist=new ArrayList<>();
		  List<Way> way5=new LinkedList<>();
		  Way[] way3 = new Way[2] ;
		  int c=0;
		    for (Way w : way){
		    	waylist.add(w);
		    	way3[c]=w;
				 c++;
			  }
		    way5.add(way3[1]);
		    System.out.println("way5"+way5);
		    return way5;
	  }
	  public List<Way> way5=getWay5();

	  /**
	   * reading the intersection points from Geometry1.IntersectionSegmentsCoors
	   * adding them in List<org.openstreetmap.josm.data.osm.Node>
	   * 
	   * @return List<org.openstreetmap.josm.data.osm.Node>
	   */
	  public List<org.openstreetmap.josm.data.osm.Node> readIntersections(List<org.openstreetmap.josm.data.osm.Node> Inter){
		   	for(int j=0;j<Inter.size();j++){
		   		for(int i=0;i<Inter.size();i++){
			   		if(Inter.get(i)==Inter.get(j)){
			   			Inter.remove(i);
			   		}
				}
		   	}
		   	for(int i=0;i<Inter.size();i++){
		   		System.out.println("Intersection["+i+"]"+Inter.get(i));
		   	 }
		   	return Inter;
	  }
	  
	 
	  
	  public Pair<List<Node>,List<Node>>WaysWithIntresection(Way wayout,List<Node> Inter,List<Way> way4,int con){
		  for(int i=0;i<wayout.getNodes().size();i++){
			  System.out.println("out4["+i+"]"+wayout.getNode(i));
		  }
		  List<Node>w4_1=new ArrayList<>();
		  List<Node>w5_1=new ArrayList<>();
		  for(Way w4:way4){
			  for(int i=0;i<wayout.getNodes().size();i++){
				  if(i<w4.getNodes().size()+Inter.size()-con){
					  w4_1.add(wayout.getNode(i));
				  }
				  else{
					  w5_1.add(wayout.getNode(i));
				  }  	
			  }
		  }
		  w4_1.add(w4_1.get(0));
		  for(int i=0;i<w4_1.size();i++){
			  
			  System.out.println("w4_1["+i+"]"+w4_1.get(i));
		  }
		  w5_1.add(w5_1.get(0));
		  for(int i=0;i<w5_1.size();i++){
			 
			  System.out.println("\nw5_1["+i+"]"+w5_1.get(i));
		  }
		  return new Pair<List<Node>,List<Node>>(w4_1,w5_1);
	  }
	  

	  /**
	   * 
	   */
	  public boolean FirstPointInsideArea(List<Node>w4_1,List<Node>w5_1,List<Node>Inter,List<Command> cmm){
			boolean info=true;
			if( Geometry.nodeInsidePolygon(w5_1.get(0), w4_1) && !w4_1.contains(w5_1.get(0))){
				info=!info;
			}
		return info;
	  }
	  
	  /**
	   * 
	   */
	  public void EraseOverlapping2Intersections(List<Node>w4_1,List<Node>w5_1,List<Node>Inter,List<Command> cmm){

		  int a = 0;
			int b = 0;
			for(int i=0;i<w5_1.size();i++){	
				
				System.out.println("w5points dupa["+i+"]"+w5_1.get(i));
				if(w5_1.get(i)==Inter.get(0)){
					System.out.println("indexulw5 1:  "+i);
					 a=i;
				}
				if(w5_1.get(i)==Inter.get(1)){
					System.out.println("indexulw5 2:   "+i);
					 b=i;
				}
			}
			
			int e = 0;
			int d = 0;
			for(int i=0;i<w4_1.size();i++){
				System.out.println("w4points dupa["+i+"]"+w4_1.get(i));
				if(w4_1.get(i)==Inter.get(0)){
					System.out.println("indexulw4 1:  "+i);
					 e=i;
				}
				if(w4_1.get(i)==Inter.get(1)){
					System.out.println("indexulw4 2:   "+i);
					 d=i;
				}
			}
			for(int i=0;i<w5_1.size();i++){
				
				System.out.println("w5_1IIII["+i+"]"+w5_1.get(i));
				}
			if( Geometry.nodeInsidePolygon(w5_1.get(0), w4_1) && !w4_1.contains(w5_1.get(0))){
	  			if(a<b){
	  				List<Node> w5_2=new ArrayList<>();
	  				int j=0;
	  				while(j<w5_1.size()){
	  					if((j<a || j>b)){
	  						w5_2.add(w5_1.get(j));
	  						j++;
	  					}
	  					else{
	  						j++;
	  					}
	  				}
	  				
	  				for(int i=0;i<w5_2.size();i++){
  					w5_1.remove(w5_2.get(i));
  					System.out.println("w5_2pp["+i+"]"+w5_2.get(i));
  					
  				}
  				System.out.println("");
  				
  				for(int i=0;i<w5_1.size();i++){
					
					System.out.println("w5_1pp["+i+"]"+w5_1.get(i));
  				}
  				System.out.println("");
	  			
	  				
	  			
	  	  			for(Way w5:way5){
	  					Way newWay = new Way(w5);
	  				      newWay.setNodes(w5_1);
	  				      cmm.add(new ChangeCommand(w5, newWay));
	  				      if(w5_2.size()!=0){
	  				    	 cmm.add(new DeleteCommand(w5_2));
	  				      }
	  				     
	  				}
	  	  			

	 	    			while(d>=e){
	  	 	    			w5_1.add(w4_1.get(d));
	  	 	    			d--;
	  	 	    		}
	 	    			
	 	    			for(Way w5:way5){
	 						Way newWay = new Way(w5);
	 					      newWay.setNodes(w5_1);
	 					      cmm.add(new ChangeCommand(w5, newWay));
	 					}
	 	    			
	 	    			for(int i=0;i<w5_1.size();i++){
	 	    				System.out.println("w5points modif["+i+"]"+w5_1.get(i));
	 	    			}
	  			}
	  			else{
	  				List<Node> w5_2=new ArrayList<>();
  				int j=0;
  				while(j<w5_1.size()){
  					if((j<b || j>a)){
  						w5_2.add(w5_1.get(j));
  						j++;
  					}
  					else{
	  						j++;
	  					}
  				}

  				for(int i=0;i<w5_2.size();i++){
	  					w5_1.remove(w5_2.get(i));
	  					System.out.println("w5_2pp["+i+"]"+w5_2.get(i));
	  					
	  				}
	  				System.out.println("");
	  				
	  				for(int i=0;i<w5_1.size();i++){
  					
  					System.out.println("w5_1pp["+i+"]"+w5_1.get(i));
  				}
	  				System.out.println("");
	  				
	  				for(Way w5:way5){
	  					Way newWay = new Way(w5);
	  				      newWay.setNodes(w5_1);
	  				      cmm.add(new ChangeCommand(w5, newWay));
	  				      if(w5_2.size()!=0){
	  				    	 cmm.add(new DeleteCommand(w5_2));
	  				      }
	  				}
	  				
	 	    		while(e<=d){
  	 	    			w5_1.add(w4_1.get(e));
  	 	    			e++;
  	 	    	}
	 	    		
	 	    		for(Way w5:way5){
						Way newWay = new Way(w5);
					      newWay.setNodes(w5_1);
					      cmm.add(new ChangeCommand(w5, newWay));
					}
	 	    		
	 	    		for(int i=0;i<w5_1.size();i++){
	 	    			System.out.println("w5points modif["+i+"]"+w5_1.get(i));
	 	    		}
	  			} 
			}
			else{
	  			if(a<b){
	  				for(int i=0;i<w5_1.size();i++){
	 	    			System.out.println("w5points Inceputt["+i+"]"+w5_1.get(i));
	 	    		}
	  				List<Node> w5_2=new ArrayList<>();
	  	  			for(int i=a+1;i<b;i++){
	  	  				
	  	  				
	  	  					w5_2.add(w5_1.get(a+1));
	  	  					w5_1.remove(w5_1.get(a+1));
	  	  				
	  	  			}
	  	  		for(int i=0;i<w5_1.size();i++){
 	    			System.out.println("w5points cracccc["+i+"]"+w5_1.get(i));
 	    		}
	  	  			for(Way w5:way5){
	  					Way newWay = new Way(w5);
	  				      newWay.setNodes(w5_1);
	  				      cmm.add(new ChangeCommand(w5, newWay));
	  				      if(w5_2.size()!=0){
	  				    	 cmm.add(new DeleteCommand(w5_2));
	  				      }
	  				}
	  	  			
	  				int a1=a+1;
	 	    			while(e<=d){
	 	    				w5_1.add(a1,w4_1.get(e));
	 	        			a1++;
	 	    				e++;
	 	    			}
	 	    			
	 	    			for(Way w5:way5){
	 						Way newWay = new Way(w5);
	 					      newWay.setNodes(w5_1);
	 					      cmm.add(new ChangeCommand(w5, newWay));
	 					}
	 	    			
	 	    			for(int i=0;i<w5_1.size();i++){
	 	    				System.out.println("w5points modif["+i+"]"+w5_1.get(i));
	 	    			}
	  			}
	  			else{
	  				List<Node> w5_3=new ArrayList<>();
	  				for(int i=b+1;i<a;i++){
	  					w5_3.add(w5_1.get(b+1));
	 	    	    	w5_1.remove(w5_1.get(b+1));	
	 	    		}
	  				
	  				for(Way w5:way5){
	  					Way newWay = new Way(w5);
	  				      newWay.setNodes(w5_1);
	  				      cmm.add(new ChangeCommand(w5, newWay));
	  				      if(w5_3.size()!=0){
	  				    	 cmm.add(new DeleteCommand(w5_3));
	  				      }
	  				}
	  				
	 	    		int a1=b+1;
	 	    		while(d>=e){
	 	    			w5_1.add(a1,w4_1.get(d));
	 	        		a1++;
	 	    			d--;
	 	    		}
	 	    		
	 	    		for(Way w5:way5){
						Way newWay = new Way(w5);
					      newWay.setNodes(w5_1);
					      cmm.add(new ChangeCommand(w5, newWay));
					}
	 	    		
	 	    		for(int i=0;i<w5_1.size();i++){
	 	    			System.out.println("w5points modif["+i+"]"+w5_1.get(i));
	 	    		}
	  			} 
			}
	  
	  }
	  
	  /**
	   * 
	   */
	  public void EraseOverlapping(List<Node>w4_1,List<Node>w5_1,List<Node>Inter,List<Command> cmm){
		  int a[]=new int[Inter.size()];
  			int in[]=new int[Inter.size()];
  			int k=0;
  			for(int l=0;l<w5_1.size();l++){
  				System.out.println("TOATE["+l+"]="+w5_1.get(l));
  			}
  			
			for(int i=0;i<w5_1.size();i++){
				System.out.println("w5_1%["+i+"]"+w5_1.get(i));
				for(int j=0;j<Inter.size();j++){	
					if(w5_1.get(i).getCoor().lat()==Inter.get(j).getCoor().lat()){
						in[k]=j;
						a[k]=i;
						System.out.println("indexulw5 a["+k+"]:  "+a[k]);
						System.out.println("in["+k+"]:  "+in[k]);
						k++;
			 		}
				}
			}
			
			int b[]=new int[Inter.size()];
    		int k1=0;
			for(int i=0;i<w4_1.size();i++){
				System.out.println("w4points dupa["+i+"]"+w4_1.get(i));
				for(int j=0;j<Inter.size();j++){
					
    				if(w4_1.get(i).getCoor().lat()==Inter.get(j).getCoor().lat()){
    					 b[k1]=i;
    					 System.out.println("indexulw4 a["+k1+"]:  "+b[k1]);
    					 k1++;
    				}
				}
			}

			//remove OverlapingPoints to w4
			List<Node> w4_2=new ArrayList<>();
			for(int j=2;j<b.length;j=j+2){
				System.out.println("dee"+j);
				for(int i=b[b.length-j]-1;i>b[b.length-1-j];i--){
    				System.out.println("iii"+i);
    	    		w4_2.add((w4_1.get(i)));
    	    		w4_1.remove((w4_1.get(i)));
    			}
			}
			
			for(Way w4:way4){
				Way newWay = new Way(w4);
			      newWay.setNodes(w4_1);
			      cmm.add(new ChangeCommand(w4, newWay));
			      if(w4_2.size()!=0){
				    cmm.add(new DeleteCommand(w4_2));
				  }
			}
			
			int indRw4[]=new int[Inter.size()];
			int Rw4=0;
			for(int i=0;i<w4_1.size();i++){
				for(int j=0;j<Inter.size();j++){
					if(w4_1.get(i)==Inter.get(j)){
    					indRw4[Rw4]=i;
    					 System.out.println("indexulw5R R["+Rw4+"]:  "+indRw4[Rw4]);
    					 Rw4++;
    				}
				}
   			}
			
			for(int l=0;l<w4_1.size();l++){
   				System.out.println("w4pointsRem["+l+"]"+w4_1.get(l));
   			}
			
			
			List<Node> w5_2=new ArrayList<>();
			for(int j=1;j<a.length;j=j+2){
				System.out.println("dee"+j);
				for(int i=a[a.length-j]-1;i>a[a.length-1-j];i--){
    				System.out.println("iii"+i);
    				w5_2.add((w5_1.get(i)));
    	    		w5_1.remove((w5_1.get(i)));
    				
    			}
			}
			
			for(Way w5:way5){
				Way newWay = new Way(w5);
			      newWay.setNodes(w5_1);
			      cmm.add(new ChangeCommand(w5, newWay));
			      if(w5_2.size()!=0){
  					  cmm.add(new DeleteCommand(w5_2));
  				}
			}
			
			int indR[]=new int[Inter.size()];
			int R=0;
			for(int i=0;i<w5_1.size();i++){
				for(int j=0;j<Inter.size();j++){
					if(w5_1.get(i)==Inter.get(j)){
    					
    					indR[R]=i;
    					 System.out.println("indexulw5R R["+R+"]:  "+indR[R]);
    					 R++;
    				}
				}
   			}
			for(int l=0;l<w5_1.size();l++){
   				System.out.println("w5pointsRem["+l+"]"+w5_1.get(l));
   			}
			
			//adding
			if(in[0]<in[in.length-1]){
				int idx=0;
				int x=0;
				while(x<=indR.length-1){
					//for(int x=0;x<a.length-1;x=x+2){
					System.out.println("idx "+idx);
					int i=indR[x]+idx;
	    			int j=indRw4[x];
	    			System.out.println("DDD"+x);
	    			while(j<indRw4[x+1]){
		    			if(!w5_1.contains(w4_1.get(j))){
		    				w5_1.add(i,w4_1.get(j));
		    			}
		   	        	i++;
		   	    		j++;
		   	    		if(j<indRw4[x+1]){
		   	    			idx++;
		   	    		}	
		    		}
	    			x=x+2;
				}
				
				for(Way w5:way5){
					Way newWay = new Way(w5);
				      newWay.setNodes(w5_1);
				      cmm.add(new ChangeCommand(w5, newWay));
				}
				
				int indRw5[]=new int[Inter.size()];
    			int Rw5=0;
    			for(int i=0;i<w5_1.size();i++){
    				for(int j=0;j<Inter.size();j++){
    					if(w5_1.get(i)==Inter.get(j)){
	    					indRw5[Rw5]=i;
	    					Rw5++;
	    				}
    				}
	    			}
				int idxw4=1;
				int xw4=1;
				while(xw4<=indRw4.length-2){
					int i=indRw4[xw4]+idxw4;
	    			int j=indRw5[xw4];
	    			while(j<indRw5[xw4+1]){
		    			w4_1.add(i,w5_1.get(j));
		   	        	i++;
		   	    		j++;
		   	    		idxw4++;
		    		}
	    			xw4=xw4+2;
				}
				
				for(Way w4:way4){
					Way newWay = new Way(w4);
				      newWay.setNodes(w4_1);
				      cmm.add(new ChangeCommand(w4, newWay));
				}
			}
			else{			    			
    			int idx=0;
				int x=0;
				int y=indR.length-1;
				while(x<=indR.length-1){
					int i=indR[x]+idx;
	    			int j=indRw4[y];
	    			while(j>=indRw4[y-1]){
		    			if(!w5_1.contains(w4_1.get(j))){
		    				w5_1.add(i,w4_1.get(j));
		    			}
		   	        	i++;
		   	    		j--;
		   	    		if(j>indRw4[y-1]){
		   	    			idx++;
		   	    		}
		   	    		
		    		}
	    			y=y-2;
	    			x=x+2;
				}
				
				for(Way w5:way5){
					Way newWay = new Way(w5);
				      newWay.setNodes(w5_1);
				      cmm.add(new ChangeCommand(w5, newWay));
				}
				
				int indRw5[]=new int[Inter.size()];
    			int Rw5=0;
    			for(int i=0;i<w5_1.size();i++){
    				for(int j=0;j<Inter.size();j++){
    					if(w5_1.get(i)==Inter.get(j)){
	    					indRw5[Rw5]=i;
	    					Rw5++;
	    				}
    				}
	    			}
    			
				int idxw4=1;
				int xw4=1;
				int y1=indRw4.length-2;
				while(xw4<=indRw4.length-2){
					System.out.println("xw4"+xw4);
					int i=indRw4[xw4]+idxw4;
	    			int j=indRw5[y1];
	    			while(j>indRw5[y1-1]){
		    			w4_1.add(i,w5_1.get(j));
		   	        	i++;
		   	    		j--;
		   	    		idxw4++;
		    		}
	    			xw4=xw4+2;
	    			y1=y1-2;
				}
				
				for(Way w4:way4){
					Way newWay = new Way(w4);
				      newWay.setNodes(w4_1);
				      cmm.add(new ChangeCommand(w4, newWay));
				}
			}
	  }
	  
	  public Pair<List<Node>,List<Node>>WaysIfExistsAllIntersections (List<Node> Inter,List<Way> way4,List<Way> way5){
		  List<Node>w4_1=new ArrayList<>();
		  List<Node>w5_1=new ArrayList<>();
		  for(Way w4:way4){
			  for(int j=0;j<w4.getNodes().size();j++){
				  w4_1.add(w4.getNode(j));
			  }  
		  }
		  for(Way w5:way5){
			  for(int j=0;j<w5.getNodes().size();j++){
				  w5_1.add(w5.getNode(j));
			  }  
		  }
		  return new Pair<List<Node>,List<Node>>(w4_1,w5_1);
	  }
	  
	  public int NumberOfExistingIntersectionPoints(List<Node> Inter){
		  int con=0;
			for(Way w:way4){
				for(int l=0;l<w.getNodes().size();l++){
	  				for(int j=0;j<Inter.size();j++){
	  					if(w.getNode(l)==Inter.get(j)){
	  						con++;
	  					}
	  				}
	  				
	  			}
			}
		return con;
	  }
	  
	  public Pair<List<Node>,List<Node>>WaysIfDoNotExistAllIntersections (Way out4,List<Node> Inter,List<Way> way4,List<Way> way5){
		  List<Node>w4_1=new ArrayList<>();
		  List<Node>w5_1=new ArrayList<>();
		  Pair<List<Node>,List<Node>>  Ways=WaysWithIntresection(out4,Inter,way4,NumberOfExistingIntersectionPoints(Inter));
		  for(Node n:Ways.getKey()){
			  w4_1.add(n);
		  }
		  for(Node n:Ways.getValue()){
			  w5_1.add(n);
		  }
		  
		  return new Pair<List<Node>,List<Node>>(w4_1,w5_1);
	  }

	  
	  public void setOptionPane(JOptionPane optionPane)
	  {
		  this.optionPane = optionPane;
	  } 
}
