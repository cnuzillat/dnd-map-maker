# D&D Map Maker

A simple JavaFX application for creating tactical maps for Dungeons & Dragons and other tabletop RPGs.

## Features

### Map Creation
- **Grid-based map** with 5-foot squares
- **Wall placement** - Click or drag to create walls (black squares)
- **Eraser tool** - Remove walls by clicking or dragging
- **Zoom and pan** - Mouse wheel to zoom, drag to pan around the map

### Token System
- **Multiple token types**: Player, Enemy, NPC, Prop, Door, Trap
- **Custom images** - Upload your own character portraits, monster images, or prop pictures
- **Custom names** - Give each token a unique name
- **Adjustable sizes** - Set token size from 1 to 10 grid squares
- **Drag and drop** - Move tokens around the map in Select mode

### Token Management
- **Place tokens** - Click the "Token" button, select a type, then click on the map
- **Edit tokens** - Double-click any token in Select mode to open the editor
- **Customize appearance** - Set custom images, names, types, and sizes
- **Visual feedback** - Tokens show custom images or colored circles as fallbacks

### File Operations
- **Save maps** - Save your map with walls and tokens as JSON files
- **Load maps** - Load previously saved maps
- **Undo/Redo** - Full undo/redo support for all operations

## How to Use

### Basic Map Creation
1. **Create walls**: Click "Wall" button, then click or drag on the map
2. **Erase walls**: Click "Eraser" button, then click or drag to remove walls
3. **Zoom**: Use mouse wheel to zoom in/out
4. **Pan**: Click and drag on empty areas to move around the map

### Adding Tokens
1. **Select token type**: Choose from the dropdown (Player, Enemy, NPC, etc.)
2. **Place token**: Click "Token" button, then click where you want to place it
3. **Customize**: The token editor will open automatically for new tokens
4. **Add custom image**: Click "Select Image" in the editor to choose a picture file
5. **Set name**: Enter a custom name for the token
6. **Adjust size**: Use the spinner to set token size (1 = 5ft, 2 = 10ft, etc.)

### Editing Existing Tokens
1. **Switch to Select mode**: Click "Select" button
2. **Edit token**: Double-click any token to open the editor
3. **Move token**: Click and drag tokens to new positions
4. **Update properties**: Change name, type, size, or image in the editor

### File Management
1. **Save map**: Click "Save" to save your current map as a JSON file
2. **Load map**: Click "Load" to open a previously saved map
3. **Undo/Redo**: Use the Undo/Redo buttons to step through your changes

## Supported Image Formats
- PNG
- JPG/JPEG
- GIF
- BMP

## Tips
- **Token images work best** when they're square and around 100-200 pixels
- **Use descriptive names** for tokens to keep track of characters and monsters
- **Save frequently** to avoid losing your work
- **Use different token types** to visually distinguish between players, enemies, and NPCs
- **Large tokens** (size 2+) are great for representing large creatures or objects

## Technical Details
- Built with JavaFX
- Uses JSON for map file format
- Supports custom image paths (relative or absolute)
- Grid-based coordinate system
- Efficient rendering with viewport culling

## Future Enhancements
- Line of sight calculations
- Area of effect templates
- Fog of war
- Initiative tracking
- Multiple layers
- Export to image formats
- Measurement tools
- Object library with pre-made tokens