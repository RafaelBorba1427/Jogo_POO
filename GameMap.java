import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
            roof,
            bucket;

    private QuadTree<GameObject> collision_detection;

    static boolean next_level = false;

    public Vector2D player_spawn_position = new Vector2D();

    // ------------------------------------------------------------
    // Parametros do passo de fisica
    // ------------------------------------------------------------

    // Quantas vezes o tick e subdividido. Mais substeps = objetos rapidos
    // atravessam menos paredes, porque cada pedaco do movimento e testado
    // separadamente. Custo linear.
    //
    // Regra: MAX_VELOCITY / substeps precisa ser menor que a
    // espessura do obstaculo mais fino e que o raio do menor corpo. Com
    // MAX_VELOCITY = 50 e as paredes de 20px deste mapa, 1 substep deixa um
    // objeto na velocidade maxima atravessar a parede inteira em um tick;
    // 4 substeps limitam a penetracao a poucos pixels. Suba para 8 se a cena
    // tiver corpos pequenos e rapidos.
    public static final int DEFAULT_SUBSTEPS = 4;

    // Quantas passadas o solver faz sobre a lista de contatos. Mais iteracoes
    // = pilhas e cantos convergem melhor Custo linear.
    public static final int DEFAULT_SOLVER_ITERATIONS = 8;

    // Manifolds vivos no passo atual.
    private final ArrayList<CollisionManifold> active_manifolds = new ArrayList<>();

    // Manifolds do passo anterior, indexados pelo par de uids. E daqui que sai
    // o warm starting: o contato equivalente do frame passado devolve seus
    // impulsos acumulados para o contato novo.
    private HashMap<Long, CollisionManifold> manifold_cache = new HashMap<>();

    // ------------------------------------------------------------
    // Map constructor
    // ------------------------------------------------------------

    GameMap(double width, double height, Vector2D player_spawn_position) {
        all_objects.clear();
        all_objects.add(permanent_objects);
        all_objects.add(immovable_objects);
        all_objects.add(moving_objects);

        map_size = new Vector2D(width, height);
        world_bounds = new AABB(
                0,
                0,
                width,
                height);

        // Game Boundaries
        left_wall = new RigidObj(0, 0, 0.01 * width, height,
                0, GameRules.DEFAULT_FRICTION, false, true, GameObject.ID_PERMANENT_WALL);

        right_wall = new RigidObj(width - (0.01 * width), 0, (0.01 * width), height,
                0, GameRules.DEFAULT_FRICTION, false, true, GameObject.ID_PERMANENT_WALL);

        floor = new RigidObj((0.01 * width), height - (0.020 * height), width - (0.02 * width), (0.020 * height),
                0, GameRules.DEFAULT_FRICTION, false, true, GameObject.ID_PERMANENT_FLOOR);

        roof = new RigidObj(0.010 * width, 0, width - (0.02 * width), 0.020 * height,
                0, GameRules.DEFAULT_FRICTION, false, true, GameObject.ID_INVISIBLE_OBJ);

        double bucket_x = 70, bucket_y = 80; // bucket width and height, always placed next to the wall
        bucket = new EventTriggerObj(width - (0.01 * width) - bucket_x, height - (0.020 * height) - bucket_y, bucket_x,
                bucket_y,
                0, true, true, GameObject.ID_BUCKET);

        permanent_objects.add(left_wall);
        permanent_objects.add(right_wall);
        permanent_objects.add(floor);
        permanent_objects.add(roof);
        permanent_objects.add(bucket);

        collision_detection = new QuadTree<>(
                world_bounds,
                8, // max objects per cell
                8); // maximum recursion depth

        this.player_spawn_position = new Vector2D(player_spawn_position);
        is_loaded = true;
    }

    // ----------------------------------------------------
    // Unit conversion methods AND CONSTANTS
    // ----------------------------------------------------
    public static double MAP_UNIT_TO_PIXEL = 0.8;

    public static Dimension MapUnit_to_Pixel(Vector2D MapDimension) {
        return new Dimension((int) (MapDimension.x * 0.8), (int) (MapDimension.y * 0.8));
    }

    public static double PIXEL_TO_MAP_UNIT = 1.25;

    public static Vector2D Pixel_to_MapUnit(Dimension PixelDimension) {
        return new Vector2D(PixelDimension).multiply(1.25);
    }

    public static Vector2D Pixel_to_MapUnit(Vector2D PixelDimension) {
        return new Vector2D(PixelDimension).multiply(1.25);
    }

    // --------------------------
    // Change map elements
    // --------------------------

    boolean addObject(GameObject target) {
        if (target == null)
            return false;

        buildBroadPhase();

        AABB bounds = target.getHitBox().getAABB();
        List<GameObject> candidates = collision_detection.query(bounds);

        for (GameObject candidate : candidates) {
            if (target.collides(candidate))
                return false;
        }

        switch (target.obj_type) {
            case GameObject.MOVABLE_OBJ:
            case GameObject.BALL_OBJ: {
                moving_objects.add(target);
            }
                break;

            default: {
                immovable_objects.add(target);
            }
                break;
        }
        return true;
    }

    void deleteInactiveObjs() {
        for (ArrayList<GameObject> obj_list : all_objects) {
            obj_list.removeIf(obj -> !obj.isActive());
        }
    }

    // --------------------------
    // Passo de fisica
    // --------------------------

    // Avanca a simulacao em dt unidades de tempo.
    // dt = 1.0 default
    public void step(double dt) {
        step(dt, DEFAULT_SUBSTEPS, DEFAULT_SOLVER_ITERATIONS);

        for(GameObject obj : moving_objects){
            ((MovableObj)obj).collided = false;
            ((MovableObj)obj).changeAcceleration(0, 0);
        }

        // Gera o Popup
        if (next_level) {
            LevelRules.nextLevel(this);
            Game.pingbongBall.move(player_spawn_position);
            Game.pingbongBall.changeVelocity(0.0, 0.0);
            Game.pingbongBall.changeAcceleration(0, 0);
            Game.pingbongBall.changeRotation(0);
            Game.pingbongBall.changeAngularVelocity(0);
            Game.pingbongBall.changeAngularAcceleration(0);

            for(GameObject obj : moving_objects)
                if(((MovableObj)obj).noGravityOnSpawn())
                    ((MovableObj)obj).changeNoGravityStatus(true);

            next_level = false;
            Game.next_level = true;
            GameRules.current_game_mode = GameRules.GameModes.EDIT;

        }
    }

    // Ordem do passo:
    // 1. integrateForces gravidade e outras forcas viram velocidade
    // 2. broad phase a QuadTree devolve os pares que PODEM colidir
    // 3. narrow phase gera os manifolds reais e herda os impulsos
    // 4. preStep massas efetivas, bias de posicao e restituicao
    // 5. warmStart reaplica os impulsos do frame anterior
    // 6. solver x N corrige as velocidades ate os contatos fecharem
    // 7. integrateVelocity so agora a velocidade vira posicao
    public void step(double dt, int substeps, int solver_iterations) {
        if (!GameRules.physics_on)
            return;
        if (substeps < 1)
            substeps = 1;
        if (solver_iterations < 1)
            solver_iterations = 1;

        double sub_dt = dt / substeps;
        double inverse_dt = (sub_dt > 0.0) ? 1.0 / sub_dt : 0.0;

        for (int s = 0; s < substeps; s++) {

            for (GameObject obj : moving_objects) {

                int gravity = 0;
                if(GameRules.global_gravity_on && !((MovableObj)obj).noGravity()){
                    gravity = 1;
                }

                if( ((MovableObj)obj).noGravityOnSpawn() && ((MovableObj)obj).collided && 
                Math.abs(((MovableObj)obj).getVelocityX()) > MovableObj.MIN_VELOCITY*10 ||
                Math.abs(((MovableObj)obj).getVelocityY()) > MovableObj.MIN_VELOCITY*10){
                    gravity = 1;
                    ((MovableObj)obj).changeNoGravityStatus(false);
                }

                ((MovableObj)obj).changeAcceleration(MovableObj.global_acceleration.x, 
                    MovableObj.global_acceleration.y + gravity*GameRules.GRAVITY);

                ((MovableObj) obj).integrateForces(sub_dt);
            }

            buildBroadPhase();
            buildManifolds();

            for (CollisionManifold manifold : active_manifolds) {
                manifold.preStep(inverse_dt);
            }

            for (CollisionManifold manifold : active_manifolds) {
                manifold.warmStart();
            }

            for (int iteration = 0; iteration < solver_iterations; iteration++) {
                for (CollisionManifold manifold : active_manifolds) {
                    manifold.solveVelocityConstraints();
                }
            }

            for (GameObject obj : moving_objects) {
                ((MovableObj) obj).integrateVelocity(sub_dt);
            }
        }

        deleteInactiveObjs();
    }

    // --------------------------
    // Broad phase
    // --------------------------

    // Reconstroi a QuadTree com os AABBs atuais de todos os objetos ativos.
    private void buildBroadPhase() {
        collision_detection.clear();

        for (ArrayList<GameObject> obj_list : all_objects) {
            for (GameObject object : obj_list) {
                if (!object.isActive())
                    continue;
                if (object.getHitBox() == null)
                    continue;
                collision_detection.insert(object, object.getHitBox().getAABB());
            }
        }
    }

    // --------------------------
    // Narrow phase
    // --------------------------
    private DeltaTime delta_time = new DeltaTime();
    private GameObject last_collided = null;

    // Transforma os pares candidatos da QuadTree em manifolds reais.
    private void buildManifolds() {
        HashMap<Long, CollisionManifold> new_cache = new HashMap<>();
        HashSet<Long> already_tested = new HashSet<>();

        active_manifolds.clear();

        for (GameObject moving : moving_objects) {
            if (!moving.isActive())
                continue;

            List<GameObject> candidates = collision_detection.query(moving.getHitBox().getAABB());

            for (GameObject candidate : candidates) {
                if (candidate == moving || !candidate.isActive())
                    continue;

                // Dois objetos moveis aparecem duas vezes nessa varredura
                // (um encontra o outro nas duas direcoes).
                long key = pairKey(moving, candidate);
                if (!already_tested.add(key))
                    continue;

                // A normal do manifold aponta de A para B, entao a ordem dos
                // corpos precisa ser a mesma todo frame: caso contrario os
                // impulsos herdados pelo warm starting viriam com o sinal
                // trocado. O uid da essa ordem estavel.
                GameObject body_a = (moving.getUid() <= candidate.getUid()) ? moving : candidate;
                GameObject body_b = (body_a == moving) ? candidate : moving;

                CollisionManifold manifold = CollisionManifold.generate(body_a, body_b);
                if (manifold == null)
                    continue;
                
                //---------------------------------------------------------------------------------
                //Checks that use collsion
                //---------------------------------------------------------------------------------
                if(body_a.getObjType() == GameObject.MOVABLE_OBJ || body_a.getObjType() == GameObject.BALL_OBJ){
                    ((MovableObj)body_a).collided = true;
                }

                if(body_b.getObjType() == GameObject.MOVABLE_OBJ || body_b.getObjType() == GameObject.BALL_OBJ){
                    ((MovableObj)body_b).collided = true;
                }

                // Checks if the player is in contact with the bucket to trigger the next level

                if (body_a.getObjType() == GameObject.EVENT_TRIGGER_OBJ
                        || body_b.getObjType() == GameObject.EVENT_TRIGGER_OBJ) {

                    if (body_a.getObjType() == GameObject.PLAYER && body_b.getObjId() == GameObject.ID_BUCKET ||
                            body_a.getObjId() == GameObject.ID_BUCKET && body_b.getObjType() == GameObject.PLAYER) {
                        SoundEffectPlayer.playSound("goal");
                        next_level = true;
                    }

                } else if (body_a.getObjType() == GameObject.PLAYER || body_b.getObjType() == GameObject.PLAYER) {
                    GameObject player;
                    GameObject other_object = null;
                    if (body_a.getObjType() == GameObject.PLAYER) {
                        player = body_a;
                        other_object = body_b;
                    } else {
                        player = body_b;
                        other_object = body_a;
                    }

                    if (Math.abs(player.getLinearVelocity().y) > 1 && last_collided != other_object) {
                        SoundEffectPlayer.playBounceSound();
                        last_collided = other_object;

                        if (delta_time.get() >= 20) {
                            PointCounter.addPoints(1);
                            System.out.println("Points: " + PointCounter.getPoints());
                        }
                    }
                } else {
                    last_collided = null;
                }
                //---------------------------------------------------------------------------------

                manifold.inheritImpulses(manifold_cache.get(key));

                new_cache.put(key, manifold);
                active_manifolds.add(manifold);
            }
        }

        manifold_cache = new_cache;
    }

    // Chave simetrica do par, montada a partir dos uids.
    private static long pairKey(GameObject a, GameObject b) {
        int low = Math.min(a.getUid(), b.getUid());
        int high = Math.max(a.getUid(), b.getUid());
        return (((long) low) << 32) | (high & 0xFFFFFFFFL);
    }

    // --------------------------
    // Getter methods
    // --------------------------
    public static ArrayList<ArrayList<GameObject>> getAllObjects() {
        return all_objects;
    }

    public ArrayList<GameObject> getMovingObjects() {
        return moving_objects;
    }

    // Manifolds resolvidos no ultimo passo. Util para depuracao: da para
    // desenhar os pontos de contato e as normais por cima da cena.
    public List<CollisionManifold> getActiveManifolds() {
        return active_manifolds;
    }

    public Vector2D getPlayerSpawn() {
        return new Vector2D(player_spawn_position);
    }

    public Vector2D getMapSize() {
        return new Vector2D(map_size);
    }

    public AABB getWorldBounds() {
        return world_bounds;
    }
}
