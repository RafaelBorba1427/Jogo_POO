
public class GameMap {
    
    // ------------------------------------------------------------
    // Map Variables and the it's collision detection system
    // ------------------------------------------------------------

    private Vector2D map_size;
    private AABB world_bounds;

    private QuadTree<GameObject> collision_detection;


    // ------------------------------------------------------------
    // Map constructor
    // ------------------------------------------------------------
    
    GameMap(int width, int height){
        map_size = new Vector2D(width, height);
        world_bounds =
        new AABB(
            0,
            0,
            width,
            800);
        
        collision_detection =
        new QuadTree<>(
            world_bounds,
            8, // max objects per cell
            8); // maximum recursion depth
        
        //quadTree/collision detection system usage example:
        /*

        //each game tick loop:

        quadtree.clear();

        for (GameObject object : gameObjects) {

            AABB bounds = object.hitbox.getAABB();

            quadtree.insert(
                    object,
                    bounds
            );
        }
        
        for (GameObject moving : movingObjects) {

            AABB bounds = moving.hitbox.getAABB();

            List<GameObject> candidates =
                staticTree.query(bounds);

            for (GameObject candidate : candidates) {

                if (moving.hitbox.intersect(candidate.hitbox)) {

                    handleCollision(
                        moving,
                        candidate
                    );
                }
            }
        }

        */

    }
}
