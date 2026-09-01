import java.util.ArrayList;
import java.util.List;

public class GameMap {
    
    // ------------------------------------------------------------
    // Map Variables and the it's collision detection system
    // ------------------------------------------------------------

    private Vector2D map_size;

    private AABB world_bounds;

    public static boolean is_loaded = false;

    private static ArrayList<ArrayList<GameObject>> all_objects = new ArrayList<>();
    private ArrayList<GameObject> immovable_objects = new ArrayList<>();
    private ArrayList<GameObject> moving_objects = new ArrayList<>();
    private ArrayList<GameObject> permanent_objects = new ArrayList<>();


    GameObject left_wall,
               right_wall,
               floor,
               roof;

    private QuadTree<GameObject> collision_detection;

    // ------------------------------------------------------------
    // Map constructor
    // ------------------------------------------------------------
    
    GameMap(double width, double height){
        all_objects.add(permanent_objects);
        all_objects.add(immovable_objects);
        all_objects.add(moving_objects);

        map_size = new Vector2D(width, height);
        world_bounds =
        new AABB(
            0,
            0,
            width,
            height);

        //Game Boundaries
        left_wall = new RigidObj(0, 0, 0.025*width, height,
             0, false, true, 0);

        right_wall = new RigidObj(width-0.025*width, 0, 0.025*width, height,
             0, false, true, 0);

        floor = new RigidObj(0.025*width, height-0.025*height, width-0.050*width, 0.025*height,
             0, false, true, 0);

        roof = new RigidObj(0.025*width, 0, width-0.050*width, 0.025*height,
             0, false, true, 0);
        
        permanent_objects.add(left_wall);
        permanent_objects.add(right_wall);
        permanent_objects.add(floor);
        permanent_objects.add(roof);

        collision_detection =
        new QuadTree<>(
            world_bounds,
            8, // max objects per cell
            8); // maximum recursion depth

        is_loaded = true;

    }

    //--------------------------
    // Change map elements
    //--------------------------
    boolean addObject(GameObject target){
        boolean flag = true;

        AABB bounds = target.hit_box.getAABB();
        List<GameObject> candidates =
        collision_detection.query(bounds);

        for (GameObject candidate : candidates) {

            if (target.collides(candidate)) {
                flag = false;
                break;
            }
        }

        if(flag){
        switch(target.obj_type){
            case GameObject.MOVABLE_OBJ:{
                moving_objects.add(target);
            }break;

            case GameObject.BALL_OBJ:{
                moving_objects.add(target);
            }break;

            default:{
                immovable_objects.add(target);
            }break;
        }
        return true;
        }

        return false;
    }

    void deleteInactiveObjs(){
        for(ArrayList<GameObject> obj_list :  all_objects){
            obj_list.removeIf(obj -> !obj.isActive());
        }
    }

    void updateMovingObjs(){
        for(GameObject obj : moving_objects){
            ((MovableObj) obj).update();
        }
    }


    //--------------------------
    //Collision Checking Methods
    //--------------------------

    void handleCollisions(){
        collision_detection.clear();

        for(ArrayList<GameObject> obj_list :  all_objects){
            for (GameObject object : obj_list) {
                if(!object.isActive()){
                    continue;
                }
                AABB bounds = object.hit_box.getAABB();
                
                collision_detection.insert(
                        object,
                        bounds
                );
            }
        }
        
        
        for (GameObject moving : moving_objects) {

            AABB bounds = moving.hit_box.getAABB();

            List<GameObject> candidates =
                collision_detection.query(bounds);

            for (GameObject candidate : candidates) {
                if (!(candidate == moving) && moving.collides(candidate)) {
                    ((MovableObj)moving).bounce(candidate);
                }
            }
        }
    }

    //--------------------------
    // Getter methods
    //--------------------------
    public static ArrayList<ArrayList<GameObject>> getAllObjects(){ 
        return all_objects;
    }



}

        
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
