package src;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class MapCanvas extends Canvas {
    private static final int GRID_SIZE = 32;
    
    private List<Wall> walls = new ArrayList<>();
    private List<Token> tokens = new ArrayList<>();
    private List<Wall> blackOverlays = new ArrayList<>();
    private Mode currentMode = Mode.WALL;
    private Token.Type selectedTokenType = Token.Type.PLAYER;

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double dragEndX, dragEndY;
    private Token selectedToken = null;
    private boolean isDraggingToken = false;
    private List<String> layerOrder = new ArrayList<>();
    private Set<String> visibleLayers = new HashSet<>();
    private String currentLayerCategory = "Default";

    private Stack<MapState> undoStack = new Stack<>();
    private Stack<MapState> redoStack = new Stack<>();
    
    public enum Mode {
        WALL, ERASER, TOKEN, SELECT, BLACK_OVERLAY, LAYER
    }
    
    public MapCanvas() {
        layerOrder.add("Default");
        visibleLayers.add("Default");
        setupMouseHandlers();
        draw();
    }
    
    private void setupMouseHandlers() {
        setOnScroll(e -> {
            double zoomFactor = 1.1;
            if (e.getDeltaY() < 0) zoomFactor = 1 / zoomFactor;
            
            double mouseX = e.getX();
            double mouseY = e.getY();
            
            offsetX = (offsetX - mouseX) * zoomFactor + mouseX;
            offsetY = (offsetY - mouseY) * zoomFactor + mouseY;
            zoom *= zoomFactor;
            
            draw();
        });

        setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                if (currentMode == Mode.TOKEN) {
                    Point gridPos = screenToGrid(e.getX(), e.getY());
                    placeToken(gridPos);
                }
                else if (currentMode == Mode.SELECT) {
                    Point gridPos = screenToGrid(e.getX(), e.getY());
                    Token clickedToken = getTokenAt(gridPos);
                    if (clickedToken != null) {
                        selectedToken = clickedToken;
                        isDraggingToken = true;
                    }
                }
                else if (currentMode == Mode.LAYER) {
                    Point gridPos = screenToGrid(e.getX(), e.getY());
                    Token clickedToken = getTokenAt(gridPos);
                    if (clickedToken != null) {
                        clickedToken.setLayerCategory(currentLayerCategory);
                        System.out.println("Set " + clickedToken.getName() + " to layer: " + currentLayerCategory);
                        draw();
                    }
                }
                else {
                    double worldX = (e.getX() - offsetX) / zoom;
                    double worldY = (e.getY() - offsetY) / zoom;
                    double gridX = worldX / GRID_SIZE;
                    double gridY = worldY / GRID_SIZE;
                    
                    dragStartX = gridX;
                    dragStartY = gridY;
                    dragEndX = gridX;
                    dragEndY = gridY;
                    isDragging = true;
                }
            }
        });

        setOnMouseDragged(e -> {
            if (isDraggingToken && selectedToken != null) {
                double worldX = (e.getX() - offsetX) / zoom;
                double worldY = (e.getY() - offsetY) / zoom;

                double gridX = worldX / GRID_SIZE;
                double gridY = worldY / GRID_SIZE;

                selectedToken.setExactPosition(gridX, gridY);
                draw();
            }
            else if (isDragging && e.isPrimaryButtonDown() && currentMode != Mode.TOKEN) {
                double worldX = (e.getX() - offsetX) / zoom;
                double worldY = (e.getY() - offsetY) / zoom;
                double gridX = worldX / GRID_SIZE;
                double gridY = worldY / GRID_SIZE;
                
                dragEndX = gridX;
                dragEndY = gridY;
                draw();
            }
        });

        setOnMouseReleased(e -> {
            if (isDraggingToken) {
                isDraggingToken = false;
                selectedToken = null;
            }
            else if (isDragging) {
                if (Math.abs(dragEndX - dragStartX) > 0.1 || Math.abs(dragEndY - dragStartY) > 0.1) {
                    createWall();
                }
                isDragging = false;
            }
        });

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && currentMode == Mode.SELECT) {
                Point gridPos = screenToGrid(e.getX(), e.getY());
                Token clickedToken = getTokenAt(gridPos);
                if (clickedToken != null) {
                    editToken(clickedToken);
                }
            }
        });
    }
    
    private Point screenToGrid(double screenX, double screenY) {
        double adjustedX = (screenX - offsetX) / zoom;
        double adjustedY = (screenY - offsetY) / zoom;
        int gridX = (int) (adjustedX / GRID_SIZE);
        int gridY = (int) (adjustedY / GRID_SIZE);
        return new Point(gridX, gridY);
    }
    
    private void createWall() {
        saveState();
        
        double x = Math.min(dragStartX, dragEndX);
        double y = Math.min(dragStartY, dragEndY);
        double width = Math.abs(dragEndX - dragStartX);
        double height = Math.abs(dragEndY - dragStartY);
        
        if (width < 0.1) width = 0.1;
        if (height < 0.1) height = 0.1;
        
        if (currentMode == Mode.WALL) {
            Wall newWall = new Wall(x, y, width, height);
            walls.add(newWall);
        } else if (currentMode == Mode.BLACK_OVERLAY) {
            Wall newOverlay = new Wall(x, y, width, height);
            blackOverlays.add(newOverlay);
        }
        
        draw();
    }
    
    private List<Wall.WallSegment> createShapeOutline() {
        if (walls.isEmpty()) {
            return new ArrayList<>();
        }

        List<Wall.WallSegment> outline = new ArrayList<>();
        for (Wall wall : walls) {
            outline.addAll(wall.getOutlineSegments());
        }
        
        System.out.println("Created outline with " + outline.size() + " segments from " + walls.size() + " walls");
        return outline;
    }
    
    private void placeToken(Point gridPos) {
        saveState();
        
        String tokenName = selectedTokenType.getDisplayName() + " " + (tokens.size() + 1);
        Token newToken = new Token(gridPos, selectedTokenType, tokenName, 1);

        newToken.setLayerCategory(currentLayerCategory);
        
        tokens.add(newToken);

        editToken(newToken);
        
        draw();
    }
    
    private void editToken(Token token) {
        TokenEditor editor = new TokenEditor(token, this);
        if (editor.showAndWait()) {
            draw();
        }
    }
    
    private Token getTokenAt(Point gridPos) {
        for (Token token : tokens) {
            double exactX = token.getExactX();
            double exactY = token.getExactY();
            int tokenSize = token.getSize();

            if (gridPos.x >= (int)exactX && gridPos.x < (int)exactX + tokenSize &&
                gridPos.y >= (int)exactY && gridPos.y < (int)exactY + tokenSize) {
                return token;
            }
        }
        return null;
    }
    
    private void saveState() {
        MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens), new ArrayList<>(blackOverlays));
        undoStack.push(currentState);
        redoStack.clear();
    }
    
    public void undo() {
        if (!undoStack.isEmpty()) {
            MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens), new ArrayList<>(blackOverlays));
            redoStack.push(currentState);
            
            MapState previousState = undoStack.pop();
            walls = previousState.walls;
            tokens = previousState.tokens;
            blackOverlays = previousState.blackOverlays;
            draw();
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens), new ArrayList<>(blackOverlays));
            undoStack.push(currentState);
            
            MapState nextState = redoStack.pop();
            walls = nextState.walls;
            tokens = nextState.tokens;
            blackOverlays = nextState.blackOverlays;
            draw();
        }
    }
    
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);

        int tilesWide = (int) (getWidth() / (zoom * GRID_SIZE)) + 2;
        int tilesHigh = (int) (getHeight() / (zoom * GRID_SIZE)) + 2;
        int startX = (int) (-offsetX / (zoom * GRID_SIZE)) - 1;
        int startY = (int) (-offsetY / (zoom * GRID_SIZE)) - 1;

        for (int y = startY; y < startY + tilesHigh; y++) {
            for (int x = startX; x < startX + tilesWide; x++) {
                gc.setFill(Color.WHITE);
                gc.fillRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE, GRID_SIZE);

                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(1);
                gc.strokeRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE, GRID_SIZE);
            }
        }

        if (!walls.isEmpty()) {
            List<Wall.WallSegment> outlineSegments = Wall.getMergedOutline(walls);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            for (Wall.WallSegment segment : outlineSegments) {
                double screenX1 = segment.x1 * GRID_SIZE;
                double screenY1 = segment.y1 * GRID_SIZE;
                double screenX2 = segment.x2 * GRID_SIZE;
                double screenY2 = segment.y2 * GRID_SIZE;
                gc.strokeLine(screenX1, screenY1, screenX2, screenY2);
            }
        }

        for (Wall overlay : blackOverlays) {
            double screenX = overlay.getX() * GRID_SIZE;
            double screenY = overlay.getY() * GRID_SIZE;
            double screenWidth = overlay.getWidth() * GRID_SIZE;
            double screenHeight = overlay.getHeight() * GRID_SIZE;
            
            gc.setFill(Color.BLACK);
            gc.fillRect(screenX, screenY, screenWidth, screenHeight);
        }

        for (int i = layerOrder.size() - 1; i >= 0; i--) {
            String layerName = layerOrder.get(i);
            if (!visibleLayers.contains(layerName)) {
            }
            
            for (Token token : tokens) {
                if (!token.getLayerCategory().equals(layerName)) {
                }
                
                double exactX = token.getExactX();
                double exactY = token.getExactY();
                int size = token.getSize();

                double screenX = exactX * GRID_SIZE;
                double screenY = exactY * GRID_SIZE;

                Point gridPos = new Point((int)exactX, (int)exactY);
                if (gridPos.x >= startX && gridPos.x < startX + tilesWide && 
                    gridPos.y >= startY && gridPos.y < startY + tilesHigh) {
                    
                    double tokenSize = size * GRID_SIZE;

                    if (currentMode == Mode.LAYER) {
                        if (token.isInLayer(currentLayerCategory)) {
                            gc.setStroke(Color.BLUE);
                            gc.setLineWidth(3);
                            gc.strokeRect(screenX - 2, screenY - 2, tokenSize + 4, tokenSize + 4);
                        }
                    }
                    
                    if (token.hasCustomImage()) {
                        Image image = token.getCustomImage();
                        gc.drawImage(image, screenX + 2, screenY + 2, tokenSize - 4, tokenSize - 4);
                    }
                    else {
                        gc.setFill(token.getType().getColor());
                        gc.fillOval(screenX + 2, screenY + 2, tokenSize - 4, tokenSize - 4);

                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(2);
                        gc.strokeOval(screenX + 2, screenY + 2, tokenSize - 4, tokenSize - 4);
                    }

                    gc.setFill(Color.WHITE);
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1);
                    String name = token.getName();
                    if (name.length() > 8) name = name.substring(0, 8);
                    gc.fillText(name, screenX + 4, screenY + tokenSize - 4);
                }
            }
        }

        if (isDragging && currentMode == Mode.WALL) {
            double x = Math.min(dragStartX, dragEndX) * GRID_SIZE;
            double y = Math.min(dragStartY, dragEndY) * GRID_SIZE;
            double width = Math.abs(dragEndX - dragStartX) * GRID_SIZE;
            double height = Math.abs(dragEndY - dragStartY) * GRID_SIZE;
            
            gc.setStroke(Color.rgb(0, 0, 255, 0.7));
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        } else if (isDragging && currentMode == Mode.BLACK_OVERLAY) {
            double x = Math.min(dragStartX, dragEndX) * GRID_SIZE;
            double y = Math.min(dragStartY, dragEndY) * GRID_SIZE;
            double width = Math.abs(dragEndX - dragStartX) * GRID_SIZE;
            double height = Math.abs(dragEndY - dragStartY) * GRID_SIZE;
            
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, width, height);
        }
        
        gc.restore();
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode;
        draw();
    }
    
    public void setTokenType(Token.Type tokenType) {
        this.selectedTokenType = tokenType;
    }
    
    public void resizeCanvas(double width, double height) {
        setWidth(width);
        setHeight(height);
        draw();
    }
    
    public boolean[][] getWallsAsArray() {
        if (walls.isEmpty()) return new boolean[0][0];

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for (Wall wall : walls) {
            minX = Math.min(minX, wall.getLeft());
            minY = Math.min(minY, wall.getTop());
            maxX = Math.max(maxX, wall.getRight());
            maxY = Math.max(maxY, wall.getBottom());
        }
        
        int width = (int) Math.ceil(maxX - minX);
        int height = (int) Math.ceil(maxY - minY);
        
        boolean[][] array = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double gridX = minX + x;
                double gridY = minY + y;

                for (Wall wall : walls) {
                    if (wall.contains(gridX, gridY)) {
                        array[y][x] = true;
                        break;
                    }
                }
            }
        }
        
        return array;
    }
    
    public void setWalls(boolean[][] wallArray) {
        walls.clear();
        
        for (int y = 0; y < wallArray.length; y++) {
            for (int x = 0; x < wallArray[0].length; x++) {
                if (wallArray[y][x]) {
                    Wall wall = new Wall(x, y, 1, 1);
                    walls.add(wall);
                }
            }
        }
        
        draw();
    }
    
    public void setWalls(List<Wall> newWalls) {
        walls = new ArrayList<>(newWalls);
        draw();
    }
    
    public List<Wall> getWalls() {
        return new ArrayList<>(walls);
    }
    
    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }
    
    public void setTokens(List<Token> newTokens) {
        tokens = new ArrayList<>(newTokens);
        draw();
    }
    
    public List<Wall> getBlackOverlays() {
        return new ArrayList<>(blackOverlays);
    }
    
    public void setBlackOverlays(List<Wall> newOverlays) {
        blackOverlays = new ArrayList<>(newOverlays);
        draw();
    }

    public void setVisibleLayers(Set<String> layers) {
        visibleLayers = new HashSet<>(layers);
        draw();
    }
    
    public Set<String> getVisibleLayers() {
        return new HashSet<>(visibleLayers);
    }
    
    public void setCurrentLayerCategory(String category) {
        this.currentLayerCategory = category != null ? category : "Default";
        draw();
    }
    
    public String getCurrentLayerCategory() {
        return currentLayerCategory;
    }
    
    public void toggleLayer(String layerName) {
        if (visibleLayers.contains(layerName)) {
            visibleLayers.remove(layerName);
        } else {
            visibleLayers.add(layerName);
        }
        draw();
    }
    
    public Set<String> getAllLayerCategories() {
        Set<String> categories = new HashSet<>();
        for (Token token : tokens) {
            categories.add(token.getLayerCategory());
        }
        return categories;
    }

    public List<String> getLayerOrder() {
        return new ArrayList<>(layerOrder);
    }
    
    public void addLayer(String layerName) {
        if (!layerOrder.contains(layerName)) {
            layerOrder.add(layerName);
            visibleLayers.add(layerName);
            draw();
        }
    }
    
    public void removeLayer(String layerName) {
        if (layerOrder.size() > 1 && layerOrder.contains(layerName)) {
            layerOrder.remove(layerName);
            visibleLayers.remove(layerName);

            for (Token token : tokens) {
                if (token.getLayerCategory().equals(layerName)) {
                    token.setLayerCategory("Default");
                }
            }
            
            draw();
        }
    }
    
    public void moveLayerUp(String layerName) {
        int index = layerOrder.indexOf(layerName);
        if (index > 0) {
            layerOrder.remove(index);
            layerOrder.add(index - 1, layerName);
            draw();
        }
    }
    
    public void moveLayerDown(String layerName) {
        int index = layerOrder.indexOf(layerName);
        if (index >= 0 && index < layerOrder.size() - 1) {
            layerOrder.remove(index);
            layerOrder.add(index + 1, layerName);
            draw();
        }
    }
    
    private static class MapState {
        List<Wall> walls;
        List<Token> tokens;
        List<Wall> blackOverlays;
        
        MapState(List<Wall> walls, List<Token> tokens, List<Wall> blackOverlays) {
            this.walls = walls;
            this.tokens = tokens;
            this.blackOverlays = blackOverlays;
        }
    }

    private List<Wall.WallSegment> findOuterBoundary() {
        if (walls.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Wall.WallSegment> allEdges = new ArrayList<>();
        List<Wall.WallSegment> outerEdges = new ArrayList<>();

        for (Wall wall : walls) {
            allEdges.addAll(wall.getOutlineSegments());
        }

        for (Wall.WallSegment edge : allEdges) {
            boolean isInternal = false;

            int containingWalls = 0;
            for (Wall wall : walls) {
                if (isEdgeContainedInWall(edge, wall)) {
                    containingWalls++;
                }
            }

            if (containingWalls > 1) {
                isInternal = true;
            }

            if (!isInternal) {
                outerEdges.add(edge);
            }
        }

        return mergeAdjacentEdges(outerEdges);
    }
    
    private boolean isEdgeContainedInWall(Wall.WallSegment edge, Wall wall) {
        double wallLeft = wall.getLeft();
        double wallRight = wall.getRight();
        double wallTop = wall.getTop();
        double wallBottom = wall.getBottom();

        if (edge.isHorizontal()) {
            return edge.y1 >= wallTop && edge.y1 < wallBottom &&
                   edge.x1 >= wallLeft && edge.x2 <= wallRight;
        }
        else if (edge.isVertical()) {
            return edge.x1 >= wallLeft && edge.x1 < wallRight &&
                   edge.y1 >= wallTop && edge.y2 <= wallBottom;
        }
        
        return false;
    }
    
    private List<Wall.WallSegment> mergeAdjacentEdges(List<Wall.WallSegment> edges) {
        if (edges.size() <= 1) return edges;
        
        List<Wall.WallSegment> merged = new ArrayList<>();
        boolean[] used = new boolean[edges.size()];
        
        for (int i = 0; i < edges.size(); i++) {
            if (used[i]) continue;
            
            Wall.WallSegment current = edges.get(i);
            used[i] = true;

            boolean extended;
            do {
                extended = false;
                for (int j = 0; j < edges.size(); j++) {
                    if (used[j]) continue;
                    
                    Wall.WallSegment next = edges.get(j);

                    if (current.isHorizontal() && next.isHorizontal() && 
                        Math.abs(current.y1 - next.y1) < 0.01 &&
                        Math.abs(current.x2 - next.x1) < 0.01) {
                        current = new Wall.WallSegment(current.x1, current.y1, next.x2, next.y2);
                        used[j] = true;
                        extended = true;
                    }
                    else if (current.isVertical() && next.isVertical() &&
                               Math.abs(current.x1 - next.x1) < 0.01 &&
                               Math.abs(current.y2 - next.y1) < 0.01) {
                        current = new Wall.WallSegment(current.x1, current.y1, next.x2, next.y2);
                        used[j] = true;
                        extended = true;
                    }
                }
            } while (extended);
            
            merged.add(current);
        }
        
        return merged;
    }

    private boolean isEdgeInternal(Wall.WallSegment segment, Wall sourceWall) {
        for (Wall otherWall : walls) {
            if (otherWall == sourceWall) continue;

            double wallLeft = otherWall.getLeft();
            double wallRight = otherWall.getRight();
            double wallTop = otherWall.getTop();
            double wallBottom = otherWall.getBottom();

            if (segment.isHorizontal()) {
                if (segment.y1 >= wallTop && segment.y1 < wallBottom &&
                    segment.x1 >= wallLeft && segment.x2 <= wallRight) {
                    return true;
                }
            }
            else if (segment.isVertical()) {
                if (segment.x1 >= wallLeft && segment.x1 < wallRight &&
                    segment.y1 >= wallTop && segment.y2 <= wallBottom) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private List<Wall.WallSegment> getUniqueSegments(List<Wall.WallSegment> segments) {
        List<Wall.WallSegment> unique = new ArrayList<>();
        boolean[] used = new boolean[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            if (used[i]) continue;
            Wall.WallSegment seg1 = segments.get(i);
            boolean foundPair = false;
            for (int j = i + 1; j < segments.size(); j++) {
                if (used[j]) continue;
                Wall.WallSegment seg2 = segments.get(j);
                if (segmentsEqual(seg1, seg2)) {
                    used[i] = true;
                    used[j] = true;
                    foundPair = true;
                    break;
                }
            }
            if (!foundPair) {
                unique.add(seg1);
            }
        }
        return unique;
    }

    private boolean segmentsEqual(Wall.WallSegment a, Wall.WallSegment b) {
        return (almostEqual(a.x1, b.x1) && almostEqual(a.y1, b.y1) && almostEqual(a.x2, b.x2) && almostEqual(a.y2, b.y2)) ||
               (almostEqual(a.x1, b.x2) && almostEqual(a.y1, b.y2) && almostEqual(a.x2, b.x1) && almostEqual(a.y2, b.y1));
    }

    private boolean almostEqual(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }
} 