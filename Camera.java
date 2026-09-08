import java.awt.Dimension;

public class Camera {
    // The Position is in the upper left corner
    public Vector2D map_position = new Vector2D();

    // camera view size
    public Dimension size = new Dimension();
    public Vector2D size_game_units = new Vector2D(); 
    
    // camera follow delay
    public static final double follow_delay = 0.1; 
    

    public Camera(int x, int y, int width, int height) {
        map_position = new Vector2D(x, y);
        size.width = width;
        size.height = height;
        size_game_units = new Vector2D(width, height).multiply(1.25);
    }

    public Camera(Vector2D map_position, Dimension size) {
        this.map_position = new Vector2D(map_position);
        this.size = size;
        size_game_units = new Vector2D(size).multiply(1.25);
    }

    //update resolution
    public void updateResolution(Dimension new_size){
        size = new Dimension(new_size);
        size_game_units = new Vector2D(size).multiply(1.25);
    }

    //Centers the camera in the perspective of the target
    // mapWidth/mapHeight must be in MAP UNITS
    public void follow(double targetX, double targetY, double mapWidth, double mapHeight) {
        // deadzone dimensions
        double dW = size_game_units.x  * 0.3f, dH = size_game_units.y * 0.3f; 
        // x position of the left and right box vertexes
        double left = map_position.x + (size_game_units.x - dW) / 2, right = left + dW; 
        // y position of the bottom and top vertexes
        double top  = map_position.y + (size_game_units.y - dH) / 2, bottom = top + dH;

        if (targetX < left)   map_position.x -= (left - targetX)* follow_delay;
        if (targetX > right)  map_position.x += (targetX - right)* follow_delay;
        if (targetY < top)    map_position.y -= (top - targetY)* follow_delay;
        if (targetY > bottom) map_position.y += (targetY - bottom)* follow_delay;
        clamp(mapWidth, mapHeight);
    }

    //Ensures that the camera won't escape the map
    private void clamp(double mapWidth, double mapHeight) {
        if (map_position.x < 0) map_position.x = 0;
        if (map_position.y < 0) map_position.y = 0;
        if (map_position.x > mapWidth - size_game_units.x)  map_position.x = Math.max(0, mapWidth - size_game_units.x);
        if (map_position.y > mapHeight - size_game_units.y) map_position.y = Math.max(0, mapHeight - size_game_units.y);
    }
}
