import java.awt.Dimension;

public class Camera {
    // The Position is in the upper left corner
    public Vector2D map_camera_position;              
    // camera view size
    public Dimension camera_size = new Dimension();
    public Vector2D camera_size_game_units = new Vector2D(); 
    // camera follow delay
    public static final double follow_delay = 0.1; 
    

    public Camera(int x, int y, int width, int height) {
        map_camera_position.setSize(x, y);
        camera_size.width = width;
        camera_size.height = height;
        camera_size_game_units.x = width;
        camera_size_game_units.y = height;
    }

    public Camera(Vector2D map_camera_position, Dimension camera_size) {
        this.map_camera_position = new Vector2D(map_camera_position);
        this.camera_size = camera_size;
        camera_size_game_units = new Vector2D(camera_size).multiply(1.25);
    }

    //update resolution
    public void updateResolution(Dimension new_camera_size){
        camera_size = new Dimension(new_camera_size);
        camera_size_game_units = new Vector2D(camera_size).multiply(1.25);
    }

    //Centers the camera in the perspective of the target
    public void follow(float targetX, float targetY, int mapPixelW, int mapPixelH) {
        // deadzone dimensions
        double dW = camera_size_game_units.x  * 0.3f, dH = camera_size_game_units.y * 0.3f; 
        // x position of the left and right box vertexes
        double left = map_camera_position.x + (camera_size_game_units.x - dW) / 2, right = left + dW; 
        // y position of the bottom and top vertexes
        double top  = map_camera_position.y + (camera_size_game_units.y - dH) / 2, bottom = top + dH;

        if (targetX < left)   map_camera_position.x -= (left - targetX)* follow_delay;
        if (targetX > right)  map_camera_position.x += (targetX - right)* follow_delay;
        if (targetY < top)    map_camera_position.y -= (top - targetY)* follow_delay;
        if (targetY > bottom) map_camera_position.y += (targetY - bottom)* follow_delay;
        clamp(mapPixelW, mapPixelH);
    }

    //Ensures that the camera won't escape the map
    private void clamp(int mapPixelW, int mapPixelH) {
        if (map_camera_position.x < 0) map_camera_position.x = 0;
        if (map_camera_position.y < 0) map_camera_position.y = 0;
        if (map_camera_position.x > mapPixelW - camera_size_game_units.x)  map_camera_position.x = Math.max(0, mapPixelW - camera_size_game_units.x);
        if (map_camera_position.y > mapPixelH - camera_size_game_units.y) map_camera_position.y = Math.max(0, mapPixelH - camera_size_game_units.y);
    }
}
