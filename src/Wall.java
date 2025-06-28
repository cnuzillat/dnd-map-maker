package src;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

public class Wall {
    private double x, y;
    private double width, height;
    
    public Wall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public double getLeft() { return x; }
    public double getRight() { return x + width; }
    public double getTop() { return y; }
    public double getBottom() { return y + height; }

    public boolean overlaps(Wall other) {
        return !(getRight() <= other.getLeft() || 
                getLeft() >= other.getRight() || 
                getBottom() <= other.getTop() || 
                getTop() >= other.getBottom());
    }

    public boolean touches(Wall other) {
        return overlaps(other) || isAdjacent(other);
    }

    public List<WallSegment> getOutlineSegments() {
        List<WallSegment> segments = new ArrayList<>();

        segments.add(new WallSegment(getLeft(), getTop(), getRight(), getTop()));
        segments.add(new WallSegment(getRight(), getTop(), getRight(), getBottom()));
        segments.add(new WallSegment(getRight(), getBottom(), getLeft(), getBottom()));
        segments.add(new WallSegment(getLeft(), getBottom(), getLeft(), getTop()));
        
        return segments;
    }

    public Polygon toJtsPolygon() {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(getLeft(), getTop());
        coords[1] = new Coordinate(getRight(), getTop());
        coords[2] = new Coordinate(getRight(), getBottom());
        coords[3] = new Coordinate(getLeft(), getBottom());
        coords[4] = new Coordinate(getLeft(), getTop());
        return gf.createPolygon(gf.createLinearRing(coords), null);
    }

    public static class WallSegment {
        public double x1, y1, x2, y2;
        
        public WallSegment(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        public boolean isHorizontal() {
            return Math.abs(y1 - y2) < 0.01;
        }
        
        public boolean isVertical() {
            return Math.abs(x1 - x2) < 0.01;
        }
        
        public boolean overlaps(WallSegment other) {
            if (isHorizontal() && other.isHorizontal()) {
                return Math.abs(y1 - other.y1) < 0.01 && 
                       (Math.max(x1, x2) >= Math.min(other.x1, other.x2) && 
                        Math.min(x1, x2) <= Math.max(other.x1, other.x2));
            } else if (isVertical() && other.isVertical()) {
                return Math.abs(x1 - other.x1) < 0.01 && 
                       (Math.max(y1, y2) >= Math.min(other.y1, other.y2) && 
                        Math.min(y1, y2) <= Math.max(other.y1, other.y2));
            }
            return false;
        }
        
        public WallSegment merge(WallSegment other) {
            if (isHorizontal() && other.isHorizontal() && Math.abs(y1 - other.y1) < 0.01) {
                double newX1 = Math.min(Math.min(x1, x2), Math.min(other.x1, other.x2));
                double newX2 = Math.max(Math.max(x1, x2), Math.max(other.x1, other.x2));
                return new WallSegment(newX1, y1, newX2, y1);
            } else if (isVertical() && other.isVertical() && Math.abs(x1 - other.x1) < 0.01) {
                double newY1 = Math.min(Math.min(y1, y2), Math.min(other.y1, other.y2));
                double newY2 = Math.max(Math.max(y1, y2), Math.max(other.y1, other.y2));
                return new WallSegment(x1, newY1, x1, newY2);
            }
            return null;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WallSegment other = (WallSegment) obj;
            return (Math.abs(x1 - other.x1) < 0.01 && Math.abs(y1 - other.y1) < 0.01 && 
                    Math.abs(x2 - other.x2) < 0.01 && Math.abs(y2 - other.y2) < 0.01) ||
                   (Math.abs(x1 - other.x2) < 0.01 && Math.abs(y1 - other.y2) < 0.01 && 
                    Math.abs(x2 - other.x1) < 0.01 && Math.abs(y2 - other.y1) < 0.01);
        }
        
        @Override
        public int hashCode() {
            double minX = Math.min(x1, x2);
            double maxX = Math.max(x1, x2);
            double minY = Math.min(y1, y2);
            double maxY = Math.max(y1, y2);
            return Double.hashCode(minX) + 31 * Double.hashCode(maxX) + 
                   31 * 31 * Double.hashCode(minY) + 31 * 31 * 31 * Double.hashCode(maxY);
        }
    }

    public boolean isAdjacent(Wall other) {
        boolean sharesHorizontalEdge = (Math.abs(getTop() - other.getBottom()) < 0.01 || 
                                       Math.abs(getBottom() - other.getTop()) < 0.01) &&
                                      getLeft() < other.getRight() && getRight() > other.getLeft();

        boolean sharesVerticalEdge = (Math.abs(getLeft() - other.getRight()) < 0.01 || 
                                     Math.abs(getRight() - other.getLeft()) < 0.01) &&
                                    getTop() < other.getBottom() && getBottom() > other.getTop();
                                    
        return sharesHorizontalEdge || sharesVerticalEdge;
    }

    public static List<WallSegment> getMergedOutline(List<Wall> walls) {
        if (walls.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (walls.size() == 1) {
            return walls.get(0).getOutlineSegments();
        }
        
        try {
            List<List<Wall>> connectedComponents = findConnectedComponents(walls);
            
            List<WallSegment> allSegments = new ArrayList<>();

            for (List<Wall> component : connectedComponents) {
                if (component.size() == 1) {
                    allSegments.addAll(component.get(0).getOutlineSegments());
                }
                else {
                    List<Polygon> polygons = new ArrayList<>();
                    for (Wall wall : component) {
                        polygons.add(wall.toJtsPolygon());
                    }
                    
                    GeometryFactory gf = new GeometryFactory();
                    Geometry union = CascadedPolygonUnion.union(polygons);

                    allSegments.addAll(extractOuterBoundary(union));
                }
            }
            
            return allSegments;
            
        } catch (Exception e) {
            System.err.println("Error in JTS union: " + e.getMessage());
            List<WallSegment> allSegments = new ArrayList<>();
            for (Wall wall : walls) {
                allSegments.addAll(wall.getOutlineSegments());
            }
            return allSegments;
        }
    }

    private static List<List<Wall>> findConnectedComponents(List<Wall> walls) {
        List<List<Wall>> components = new ArrayList<>();
        boolean[] visited = new boolean[walls.size()];
        
        for (int i = 0; i < walls.size(); i++) {
            if (!visited[i]) {
                List<Wall> component = new ArrayList<>();
                dfs(walls, i, visited, component);
                components.add(component);
            }
        }
        
        return components;
    }

    private static void dfs(List<Wall> walls, int index, boolean[] visited, List<Wall> component) {
        visited[index] = true;
        component.add(walls.get(index));
        
        for (int i = 0; i < walls.size(); i++) {
            if (!visited[i] && wallsAreClose(walls.get(index), walls.get(i))) {
                dfs(walls, i, visited, component);
            }
        }
    }

    private static boolean wallsAreClose(Wall wall1, Wall wall2) {
        if (wall1.overlaps(wall2)) {
            return true;
        }

        if (wall1.isAdjacent(wall2)) {
            return true;
        }

        double distance = getDistance(wall1, wall2);
        return distance < 1.0;
    }

    private static double getDistance(Wall wall1, Wall wall2) {
        double dx = Math.max(0, Math.max(wall1.getLeft() - wall2.getRight(), wall2.getLeft() - wall1.getRight()));
        double dy = Math.max(0, Math.max(wall1.getTop() - wall2.getBottom(), wall2.getTop() - wall1.getBottom()));
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static List<WallSegment> extractOuterBoundary(Geometry geometry) {
        List<WallSegment> segments = new ArrayList<>();
        
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
            
            for (int i = 0; i < coords.length - 1; i++) {
                Coordinate c1 = coords[i];
                Coordinate c2 = coords[i + 1];
                segments.add(new WallSegment(c1.x, c1.y, c2.x, c2.y));
            }
        }
        else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPoly = (MultiPolygon) geometry;
            for (int i = 0; i < multiPoly.getNumGeometries(); i++) {
                Geometry geom = multiPoly.getGeometryN(i);
                if (geom instanceof Polygon) {
                    Polygon polygon = (Polygon) geom;
                    Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
                    
                    for (int j = 0; j < coords.length - 1; j++) {
                        Coordinate c1 = coords[j];
                        Coordinate c2 = coords[j + 1];
                        segments.add(new WallSegment(c1.x, c1.y, c2.x, c2.y));
                    }
                }
            }
        }
        
        return segments;
    }

    public static class ComplexWall extends Wall {
        private List<WallSegment> segments;
        
        public ComplexWall(List<WallSegment> segments) {
            super(calculateBoundingBox(segments)[0], calculateBoundingBox(segments)[1], 
                  calculateBoundingBox(segments)[2], calculateBoundingBox(segments)[3]);
            this.segments = new ArrayList<>(segments);
        }
        
        private static double[] calculateBoundingBox(List<WallSegment> segments) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
            
            for (WallSegment segment : segments) {
                minX = Math.min(minX, Math.min(segment.x1, segment.x2));
                minY = Math.min(minY, Math.min(segment.y1, segment.y2));
                maxX = Math.max(maxX, Math.max(segment.x1, segment.x2));
                maxY = Math.max(maxY, Math.max(segment.y1, segment.y2));
            }
            
            return new double[]{minX, minY, maxX - minX, maxY - minY};
        }
        
        @Override
        public List<WallSegment> getOutlineSegments() {
            return new ArrayList<>(segments);
        }
    }
} 