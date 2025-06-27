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

public class MapCanvas extends Canvas {
    private static final int GRID_SIZE = 32;
    
    private List<Wall> walls = new ArrayList<>();
    private List<Token> tokens = new ArrayList<>();
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

    private Stack<MapState> undoStack = new Stack<>();
    private Stack<MapState> redoStack = new Stack<>();
    
    public enum Mode {
        WALL, ERASER, TOKEN, SELECT
    }
    
    public MapCanvas() {
        setWidth(1200);
        setHeight(800);
        
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
        }
        
        draw();
    }
    
    private void placeToken(Point gridPos) {
        saveState();
        
        String tokenName = selectedTokenType.getDisplayName() + " " + (tokens.size() + 1);
        Token newToken = new Token(gridPos, selectedTokenType, tokenName, 1);
        tokens.add(newToken);

        editToken(newToken);
        
        draw();
    }
    
    private void editToken(Token token) {
        TokenEditor editor = new TokenEditor(token);
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
        MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens));
        undoStack.push(currentState);
        redoStack.clear();
    }
    
    public void undo() {
        if (!undoStack.isEmpty()) {
            MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens));
            redoStack.push(currentState);
            
            MapState previousState = undoStack.pop();
            walls = previousState.walls;
            tokens = previousState.tokens;
            draw();
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            MapState currentState = new MapState(new ArrayList<>(walls), new ArrayList<>(tokens));
            undoStack.push(currentState);
            
            MapState nextState = redoStack.pop();
            walls = nextState.walls;
            tokens = nextState.tokens;
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

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Wall wall : walls) {
            double screenX = wall.getX() * GRID_SIZE;
            double screenY = wall.getY() * GRID_SIZE;
            double screenWidth = wall.getWidth() * GRID_SIZE;
            double screenHeight = wall.getHeight() * GRID_SIZE;
            
            gc.strokeRect(screenX, screenY, screenWidth, screenHeight);
        }

        for (Token token : tokens) {
            double exactX = token.getExactX();
            double exactY = token.getExactY();
            int size = token.getSize();

            double screenX = exactX * GRID_SIZE;
            double screenY = exactY * GRID_SIZE;

            Point gridPos = new Point((int)exactX, (int)exactY);
            if (gridPos.x >= startX && gridPos.x < startX + tilesWide && 
                gridPos.y >= startY && gridPos.y < startY + tilesHigh) {
                
                double tokenSize = size * GRID_SIZE;
                
                if (token.hasCustomImage()) {
                    Image image = token.getCustomImage();
                    gc.drawImage(image, screenX + 2, screenY + 2, tokenSize - 4, tokenSize - 4);
                } else {
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

        if (isDragging && currentMode == Mode.WALL) {
            double x = Math.min(dragStartX, dragEndX) * GRID_SIZE;
            double y = Math.min(dragStartY, dragEndY) * GRID_SIZE;
            double width = Math.abs(dragEndX - dragStartX) * GRID_SIZE;
            double height = Math.abs(dragEndY - dragStartY) * GRID_SIZE;
            
            gc.setStroke(Color.rgb(0, 0, 255, 0.7)); // Semi-transparent blue
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
        
        gc.restore();
    }
    
    public void setMode(Mode mode) {
        this.currentMode = mode;
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

    private static class MapState {
        List<Wall> walls;
        List<Token> tokens;
        
        MapState(List<Wall> walls, List<Token> tokens) {
            this.walls = walls;
            this.tokens = tokens;
        }
    }
} 