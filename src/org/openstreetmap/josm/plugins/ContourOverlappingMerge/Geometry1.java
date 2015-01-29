package org.openstreetmap.josm.plugins.ContourOverlappingMerge;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javafx.util.Pair;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.MultipolygonBuilder;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.NodePositionComparator;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Predicate;

import com.sun.javafx.geom.Line2D;

public class Geometry1 {


	    private static BBox getNodesBounds(List<Node> nodes) {

	        BBox bounds = new BBox(nodes.get(0));
	        for (Node n: nodes) {
	            bounds.add(n.getCoor());
	        }
	        return bounds;
	    }

	    /**
	     * Finds the intersection of two line segments
	     * @return EastNorth null if no intersection was found, the EastNorth coordinates of the intersection otherwise
	     */
	    public static EastNorth getSegmentSegmentIntersection(EastNorth p1, EastNorth p2, EastNorth p3, EastNorth p4) {

	        CheckParameterUtil.ensureValidCoordinates(p1, "p1");
	        CheckParameterUtil.ensureValidCoordinates(p2, "p2");
	        CheckParameterUtil.ensureValidCoordinates(p3, "p3");
	        CheckParameterUtil.ensureValidCoordinates(p4, "p4");
	        
	        

	        double x1 = p1.getX();
	        double y1 = p1.getY();
	        double x2 = p2.getX();
	        double y2 = p2.getY();
	        double x3 = p3.getX();
	        double y3 = p3.getY();
	        double x4 = p4.getX();
	        double y4 = p4.getY();

	        //TODO: do this locally.
	        //TODO: remove this check after careful testing
	        //if (!Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) return null;

	        // solve line-line intersection in parametric form:
	        // (x1,y1) + (x2-x1,y2-y1)* u  = (x3,y3) + (x4-x3,y4-y3)* v
	        // (x2-x1,y2-y1)*u - (x4-x3,y4-y3)*v = (x3-x1,y3-y1)
	        // if 0<= u,v <=1, intersection exists at ( x1+ (x2-x1)*u, y1 + (y2-y1)*u )

	        double a1 = x2 - x1;
	        double b1 = x3 - x4;
	        double c1 = x3 - x1;

	        double a2 = y2 - y1;
	        double b2 = y3 - y4;
	        double c2 = y3 - y1;

	        // Solve the equations
	        double det = a1*b2 - a2*b1;

	        double uu = b2*c1 - b1*c2 ;
	        double vv = a1*c2 - a2*c1;
	        double mag = Math.abs(uu)+Math.abs(vv);

	        if (Math.abs(det) > 1e-12 * mag) {
	            double u = uu/det, v = vv/det;
	            if (u>-1e-8 && u < 1+1e-8 && v>-1e-8 && v < 1+1e-8 ) {
	            	System.out.println(" ");
	    	        System.out.println("Punctele sunttttt"+p1+";"+p2+";"+p3+";"+p4);
	    	        System.out.println(" ");
	                if (u<0) u=0;
	                if (u>1) u=1.0;
	                return new EastNorth(x1+a1*u, y1+a2*u);
	            } else {
	                return null;
	            }
	        } else {
	            // parallel lines
	            return null;
	        }
	    }

	    
	    
	    
	    public static Pair <List<Node>, Way> IntersectionSegmentsCoors(List<Way> ways, boolean test, List<Command> cmds) {

	    	Collection<List<Node>> out=new ArrayList<>();
	        int n = ways.size();
	        @SuppressWarnings("unchecked")
	        List<Node>[] newNodes = new ArrayList[n];
	        BBox[] wayBounds = new BBox[n];
	        boolean[] changedWays = new boolean[n];

	        List<Node> intersectionNodes = new ArrayList<>();
	        List<Node> Seg1Coor1 = new ArrayList<>();
	        List<Node> Seg1Coor2 = new ArrayList<>();
	        List<Node> Seg2Coor1 = new ArrayList<>();
	        List<Node> Seg2Coor2 = new ArrayList<>();
	        
	       
	        //copy node arrays for local usage.
	        for (int pos = 0; pos < n; pos ++) {
	            newNodes[pos] = new ArrayList<>(ways.get(pos).getNodes());
	            wayBounds[pos] = getNodesBounds(newNodes[pos]);
	            changedWays[pos] = false;
	        }

	        //iterate over all way pairs and introduce the intersections
	        Comparator<Node> coordsComparator = new NodePositionComparator();
	        for (int seg1Way = 0; seg1Way < n; seg1Way ++) {
	            for (int seg2Way = seg1Way; seg2Way < n; seg2Way ++) {

	                //do not waste time on bounds that do not intersect
	                if (!wayBounds[seg1Way].intersects(wayBounds[seg2Way])) {
	                    continue;
	                }

	                List<Node> way1Nodes = newNodes[seg1Way];
	                List<Node> way2Nodes = newNodes[seg2Way];

	                //iterate over primary segmemt
	               // Way newWay1 = new Way();
	                for (int seg1Pos = 0; seg1Pos + 1 < way1Nodes.size(); seg1Pos ++) {

	                    //iterate over secondary segment
	                    int seg2Start = seg1Way != seg2Way ? 0: seg1Pos + 2;//skip the adjacent segment

	                    for (int seg2Pos = seg2Start; seg2Pos + 1< way2Nodes.size(); seg2Pos ++) {

	                        //need to get them again every time, because other segments may be changed
	                        Node seg1Node1 = way1Nodes.get(seg1Pos);
	                        Node seg1Node2 = way1Nodes.get(seg1Pos + 1);
	                        Node seg2Node1 = way2Nodes.get(seg2Pos);
	                        Node seg2Node2 = way2Nodes.get(seg2Pos + 1);

	                        int commonCount = 0;
	                        //test if we have common nodes to add.
	                        if (seg1Node1 == seg2Node1 || seg1Node1 == seg2Node2) {
	                            commonCount ++;

	                            if (seg1Way == seg2Way &&
	                                    seg1Pos == 0 &&
	                                    seg2Pos == way2Nodes.size() -2) {
	                                //do not add - this is first and last segment of the same way.
	                            } else {
	                                intersectionNodes.add(seg1Node1);
	                            }
	                        }

	                        if (seg1Node2 == seg2Node1 || seg1Node2 == seg2Node2) {
	                            commonCount ++;

	                            intersectionNodes.add(seg1Node2);
	                        }

	                        //no common nodes - find intersection
	                        Way newWay1 = new Way();
	                        if (commonCount == 0) {
	                            EastNorth intersection = getSegmentSegmentIntersection(
	                                    seg1Node1.getEastNorth(), seg1Node2.getEastNorth(),
	                                    seg2Node1.getEastNorth(), seg2Node2.getEastNorth());

	                            if (intersection != null) {
	                                if (test) {
	                                    intersectionNodes.add(seg2Node1);
	                                    Seg1Coor1.add(seg1Node1);
		                                Seg1Coor2.add(seg1Node2);
		                                Seg2Coor1.add(seg2Node1);
		                                Seg2Coor2.add(seg2Node2);
		                                out.add(intersectionNodes);
		                                out.add(Seg1Coor1);
		                                out.add(Seg1Coor2);
		                                out.add(Seg2Coor1);
		                                out.add(Seg2Coor2);
		                                
		                               
		                                for (int pos = 0; pos < ways.size(); pos ++) {
		                    	            if (!changedWays[pos]) {
		                    	                continue;
		                    	            }

		                    	            Way way = ways.get(pos);
		                    	            Way newWay = new Way(way);
		                    	            newWay.setNodes(newNodes[pos]);

		                    	            cmds.add(new ChangeCommand(way, newWay));
		                    	            for(int i=0;i<newWay.getNodes().size();i++){
		                    					  
		                    					  newWay1.addNode(newWay.getNode(i));
		                    				  }
		                    	        }
	                                    return new Pair <List<Node>, Way>(intersectionNodes,newWay1);
	                                }

	                                Node newNode = new Node(Main.getProjection().eastNorth2latlon(intersection));
	                                Node intNode = newNode;
	                                boolean insertInSeg1 = false;
	                                boolean insertInSeg2 = false;
	                                //find if the intersection point is at end point of one of the segments, if so use that point

	                                //segment 1
	                                if (coordsComparator.compare(newNode, seg1Node1) == 0) {
	                                    intNode = seg1Node1;
	                                } else if (coordsComparator.compare(newNode, seg1Node2) == 0) {
	                                    intNode = seg1Node2;
	                                } else {
	                                    insertInSeg1 = true;
	                                }

	                                //segment 2
	                                if (coordsComparator.compare(newNode, seg2Node1) == 0) {
	                                    intNode = seg2Node1;
	                                } else if (coordsComparator.compare(newNode, seg2Node2) == 0) {
	                                    intNode = seg2Node2;
	                                } else {
	                                    insertInSeg2 = true;
	                                }

	                                if (insertInSeg1) {
	                                    way1Nodes.add(seg1Pos +1, intNode);
	                                    changedWays[seg1Way] = true;

	                                    //fix seg2 position, as indexes have changed, seg2Pos is always bigger than seg1Pos on the same segment.
	                                    if (seg2Way == seg1Way) {
	                                        seg2Pos ++;
	                                    }
	                                }

	                                if (insertInSeg2) {
	                                    way2Nodes.add(seg2Pos +1, intNode);
	                                    changedWays[seg2Way] = true;

	                                    //Do not need to compare again to already split segment
	                                    seg2Pos ++;
	                                }

	                                intersectionNodes.add(intNode);
	                                Seg1Coor1.add(seg1Node1);
	                                Seg1Coor2.add(seg1Node2);
	                                Seg2Coor1.add(seg2Node1);
	                                Seg2Coor2.add(seg2Node2);
	                                out.add(intersectionNodes);
	                                out.add(Seg1Coor1);
	                                out.add(Seg1Coor2);
	                                out.add(Seg2Coor1);
	                                out.add(Seg2Coor2);

	                                if (intNode == newNode) {
	                                    cmds.add(new AddCommand(intNode));
	                                }
	                            }
	                            for (int pos = 0; pos < ways.size(); pos ++) {
                    	            if (!changedWays[pos]) {
                    	                continue;
                    	            }

                    	            Way way = ways.get(pos);
                    	            Way newWay = new Way(way);
                    	            newWay.setNodes(newNodes[pos]);

                    	            cmds.add(new ChangeCommand(way, newWay));
                    	            for(int i=0;i<newWay.getNodes().size();i++){
                    					  
                    					  newWay1.addNode(newWay.getNode(i));
                    				  }
                    	        } 
	                        }
	                        else if (test && !intersectionNodes.isEmpty())
	                        	
	                        	 return new Pair <List<Node>, Way>(intersectionNodes,newWay1);
	                    }
	                }
	            }
	        }

	        Way newWay1 = new Way();
	        for (int pos = 0; pos < ways.size(); pos ++) {
	            if (!changedWays[pos]) {
	                continue;
	            }

	            Way way = ways.get(pos);
	            Way newWay = new Way(way);
	            newWay.setNodes(newNodes[pos]);

	            cmds.add(new ChangeCommand(way, newWay));
	            for(int i=0;i<newWay.getNodes().size();i++){
					  
					  newWay1.addNode(newWay.getNode(i));
				  }
	        }
	        
	        return new Pair <List<Node>, Way>(intersectionNodes,newWay1);
	    }

	    
//		private static List<Node> (intersectionNodes(List<Node> seg1Coor1,
//				List<Node> seg1Coor2, List<Node> seg2Coor1, List<Node> seg2Coor2) {
//			// TODO Auto-generated method stub
//			return null;
//		}

	    public static List<LatLon> getLinesSegments(List<Way> ways, boolean test, List<Command> cmds) {
	    	List<Node> segments=new ArrayList<>();
	    	List<LatLon> RealSegNode=new ArrayList<>();
	        int n = ways.size();
	        @SuppressWarnings("unchecked")
	        List<Node>[] newNodes = new ArrayList[n];
	        BBox[] wayBounds = new BBox[n];
	        boolean[] changedWays = new boolean[n];

	        Set<Node> intersectionNodes = new LinkedHashSet<>();

	        //copy node arrays for local usage.
	        for (int pos = 0; pos < n; pos ++) {
	            newNodes[pos] = new ArrayList<>(ways.get(pos).getNodes());
	            wayBounds[pos] = getNodesBounds(newNodes[pos]);
	            changedWays[pos] = false;
	        }

	        //iterate over all way pairs and introduce the intersections
	        Comparator<Node> coordsComparator = new NodePositionComparator();
	        for (int seg1Way = 0; seg1Way < n; seg1Way ++) {
	            for (int seg2Way = seg1Way; seg2Way < n; seg2Way ++) {

	                //do not waste time on bounds that do not intersect
	                if (!wayBounds[seg1Way].intersects(wayBounds[seg2Way])) {
	                    continue;
	                }
	               Point a = new Point(0, 0);
				Point b = new Point(0, 0);
				LineSegment Seg1 = new LineSegment(a, b);
	               LineSegment Seg2 = new LineSegment(a, b);
	              
	                
	                List<Node> way1Nodes = newNodes[seg1Way];
	                List<Node> way2Nodes = newNodes[seg2Way];

	                //iterate over primary segmemt
	                for (int seg1Pos = 0; seg1Pos + 1 < way1Nodes.size(); seg1Pos ++) {

	                    //iterate over secondary segment
	                    int seg2Start = seg1Way != seg2Way ? 0: seg1Pos + 2;//skip the adjacent segment

	                    for (int seg2Pos = seg2Start; seg2Pos + 1< way2Nodes.size(); seg2Pos ++) {

	                        //need to get them again every time, because other segments may be changed
	                        Node seg1Node1 = way1Nodes.get(seg1Pos);
	                        Node seg1Node2 = way1Nodes.get(seg1Pos + 1);
	                        Node seg2Node1 = way2Nodes.get(seg2Pos);
	                        Node seg2Node2 = way2Nodes.get(seg2Pos + 1);
	                        
	                        Seg1.first.x=seg1Node1.getCoor().lat();
	                        Seg1.first.y=seg1Node1.getCoor().lon();
	                        Seg1.second.x=seg1Node2.getCoor().lat();
	                        Seg1.second.y=seg1Node2.getCoor().lon();
	                        
	                        Seg2.first.x=seg2Node1.getCoor().lat();
	                        Seg2.first.y=seg2Node1.getCoor().lon();
	                        Seg2.second.x=seg2Node2.getCoor().lat();
	                        Seg2.second.y=seg2Node2.getCoor().lon();


	                        int commonCount = 0;
	                        //test if we have common nodes to add.
	                        if (seg1Node1 == seg2Node1 || seg1Node1 == seg2Node2) {
	                            commonCount ++;

	                            if (seg1Way == seg2Way &&
	                                    seg1Pos == 0 &&
	                                    seg2Pos == way2Nodes.size() -2) {
	                                //do not add - this is first and last segment of the same way.
	                            } else {
	                                intersectionNodes.add(seg1Node1);  
	                                segments.add(seg1Node1);
	                            }
	                        }

	                        if (seg1Node2 == seg2Node1 || seg1Node2 == seg2Node2) {
	                            commonCount ++;

	                            intersectionNodes.add(seg1Node2);
	                            segments.add(seg1Node2);
	                        }

	                        //no common nodes - find intersection
	                        if (commonCount == 0) {
	                        	List< EastNorth> compPoints=new ArrayList<>();
	                        	List<LatLon> compPointsLatLon=new ArrayList<>();
	                            EastNorth intersection = getSegmentSegmentIntersection(
	                                    seg1Node1.getEastNorth(), seg1Node2.getEastNorth(),
	                                    seg2Node1.getEastNorth(), seg2Node2.getEastNorth());
	                           List< EastNorth>  SegIntersection = getSegmentSegmentIntersection1(
	                                    seg1Node1.getEastNorth(), seg1Node2.getEastNorth(),
	                                    seg2Node1.getEastNorth(), seg2Node2.getEastNorth());
	                           compPoints.add(seg1Node1.getEastNorth());
	                           compPoints.add(seg1Node2.getEastNorth());
	                           compPoints.add(seg2Node1.getEastNorth());
	                           compPoints.add(seg2Node2.getEastNorth());
	                           
	                           compPointsLatLon.add(seg1Node1.getCoor());
	                           compPointsLatLon.add(seg1Node2.getCoor());
	                           compPointsLatLon.add(seg2Node1.getCoor());
	                           compPointsLatLon.add(seg2Node2.getCoor());
	                            
	                            if(SegIntersection!=null){
	                            	for(int i=0;i<SegIntersection.size();i++){
		                            	if(SegIntersection!=null 
		                            			&& SegIntersection.get(i).getX()==compPoints.get(i).getX()
		                            			//||SegIntersection==seg2Node1.getEastNorth()||SegIntersection==seg2Node2.getEastNorth()
		                            			){
		                            		System.out.println("SegIntersection"+SegIntersection.get(i));
		    	                            System.out.println("seg1Node1"+compPoints.get(i));
		    	                            RealSegNode.add(compPointsLatLon.get(i));
			                            	System.out.println("Latittttttt"+compPointsLatLon.get(i));
			                            }
		                            }
	                            }
	                            
	                            
	                            
	                        }
	                        else if (test && !intersectionNodes.isEmpty())
	                            return RealSegNode;
	                    }
	                }
	            }
	        }


	        for (int pos = 0; pos < ways.size(); pos ++) {
	            if (!changedWays[pos]) {
	                continue;
	            }

	            Way way = ways.get(pos);
	            Way newWay = new Way(way);
	            newWay.setNodes(newNodes[pos]);

	            cmds.add(new ChangeCommand(way, newWay));
	        }

	        return RealSegNode;
	    }
	    
	    public static List< EastNorth> getSegmentSegmentIntersection1(EastNorth p1, EastNorth p2, EastNorth p3, EastNorth p4) {

	        CheckParameterUtil.ensureValidCoordinates(p1, "p1");
	        CheckParameterUtil.ensureValidCoordinates(p2, "p2");
	        CheckParameterUtil.ensureValidCoordinates(p3, "p3");
	        CheckParameterUtil.ensureValidCoordinates(p4, "p4");
	       
	        

	        double x1 = p1.getX();
	        double y1 = p1.getY();
	        double x2 = p2.getX();
	        double y2 = p2.getY();
	        double x3 = p3.getX();
	        double y3 = p3.getY();
	        double x4 = p4.getX();
	        double y4 = p4.getY();

	        //TODO: do this locally.
	        //TODO: remove this check after careful testing
	        //if (!Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) return null;

	        // solve line-line intersection in parametric form:
	        // (x1,y1) + (x2-x1,y2-y1)* u  = (x3,y3) + (x4-x3,y4-y3)* v
	        // (x2-x1,y2-y1)*u - (x4-x3,y4-y3)*v = (x3-x1,y3-y1)
	        // if 0<= u,v <=1, intersection exists at ( x1+ (x2-x1)*u, y1 + (y2-y1)*u )

	        double a1 = x2 - x1;
	        double b1 = x3 - x4;
	        double c1 = x3 - x1;

	        double a2 = y2 - y1;
	        double b2 = y3 - y4;
	        double c2 = y3 - y1;

	        // Solve the equations
	        double det = a1*b2 - a2*b1;

	        double uu = b2*c1 - b1*c2 ;
	        double vv = a1*c2 - a2*c1;
	        double mag = Math.abs(uu)+Math.abs(vv);

	        if (Math.abs(det) > 1e-12 * mag) {
	            double u = uu/det, v = vv/det;
	            if (u>-1e-8 && u < 1+1e-8 && v>-1e-8 && v < 1+1e-8 ) {
	            	 List<EastNorth> pcte=new ArrayList<>();
	    	        pcte.add(p1);
	    	        pcte.add(p2);
	    	        pcte.add(p3);
	    	        pcte.add(p4);
	                if (u<0) u=0;
	                if (u>1) u=1.0;
	                System.out.println(" ");
	    	        //System.out.println("PUNCTUL"+p1);
	    	        System.out.println("segSIZE"+pcte.size());
	    	        System.out.println(" ");
	                return pcte;
	            } else {
	                return null;
	            }
	        } else {
	            // parallel lines
	            return null;
	        }
	    }

}