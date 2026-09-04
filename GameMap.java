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
            roof;

    private QuadTree<GameObject> collision_detection;

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

    GameMap(double width, double height) {
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
        left_wall = new RigidObj(0, 0, 0.025 * width, height,
                0, GameRules.DEFAULT_FRICTION, false, true, 0);

        right_wall = new RigidObj(width - 0.025 * width, 0, 0.025 * width, height,
                0, GameRules.DEFAULT_FRICTION, false, true, 0);

        floor = new RigidObj(0.025 * width, height - 0.025 * height, width - 0.050 * width, 0.025 * height,
                0, GameRules.DEFAULT_FRICTION, false, true, 0);

        roof = new RigidObj(0.025 * width, 0, width - 0.050 * width, 0.025 * height,
                0, GameRules.DEFAULT_FRICTION, false, true, 0);

        permanent_objects.add(left_wall);
        permanent_objects.add(right_wall);
        permanent_objects.add(floor);
        permanent_objects.add(roof);

        collision_detection = new QuadTree<>(
                world_bounds,
                8, // max objects per cell
                8); // maximum recursion depth

        is_loaded = true;
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

    // Transforma os pares candidatos da QuadTree em manifolds reais.
    private void buildManifolds() {
        HashMap<Long, CollisionManifold> new_cache = new HashMap<>();
        HashSet<Long> already_tested = new HashSet<>();

        active_manifolds.clear();
        boolean nextLevel = false;
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
                if ((body_a.getObjId() == GameObject.ID_BALDE | body_b.getObjId() == GameObject.ID_BALDE)
                        && (body_a.getObjType() == GameObject.PLAYER || body_a.getObjType() == GameObject.PLAYER)) {
                    nextLevel = true;

                }

                manifold.inheritImpulses(manifold_cache.get(key));

                new_cache.put(key, manifold);
                active_manifolds.add(manifold);
            }
        }

        manifold_cache = new_cache;
        if (nextLevel) {
            synchronized (Main.rules) {

                Main.rules.nextLevel();

            }
            Game.pingbongBall.move(100, 100);
            Game.pingbongBall.changeVelocity(0.0, 0.0);
            Game.pingbongBall.acceleration.y = 0;
        }
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

    public Vector2D getMapSize() {
        return new Vector2D(map_size);
    }

    public AABB getWorldBounds() {
        return world_bounds;
    }
}
