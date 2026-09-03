import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Generic QuadTree Implementation
public class QuadTree<T> {

    // ------------------------------------------------------------
    // General Tree Information
    // ------------------------------------------------------------

    // max num. of objects per level and max level's of the of recursive tree

    private final int MAX_OBJECTS;
    private final int MAX_LEVELS;

    // Node information

    private final int level;
    private final AABB bounds;

    private final List<Entry<T>> objects;

    private QuadTree<T>[] children;

    // Entry
    // * Pair of Object + it's AABB bounds

    private static class Entry<T> {

        T object;
        AABB bounds;

        Entry(T object, AABB bounds) {
            this.object = object;
            this.bounds = bounds;
        }
    }

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------

    public QuadTree(AABB bounds, int maxObjects, int maxLevels) {
        this(0, bounds, maxObjects, maxLevels);
    }

    private QuadTree(
            int level,
            AABB bounds,
            int maxObjects,
            int maxLevels)
    {
        this.level = level;
        this.bounds = bounds;
        this.MAX_OBJECTS = maxObjects;
        this.MAX_LEVELS = maxLevels;

        this.objects = new ArrayList<>();
        this.children = null;
    }


    // ------------------------------------------------------------
    // Clear
    // ------------------------------------------------------------

    public void clear() {

        objects.clear();

        if (children != null) {

            for (QuadTree<T> child : children) {
                child.clear();
            }

            children = null;
        }
    }


    // ------------------------------------------------------------
    // Split node into four children
    // ------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void split() {

        double minX = bounds.min_pos.x;
        double minY = bounds.min_pos.y;

        double maxX = bounds.max_pos.x;
        double maxY = bounds.max_pos.y;

        double midX = (minX + maxX) * 0.5;
        double midY = (minY + maxY) * 0.5;

        children = new QuadTree[4];

        // 0 = top-left
        children[0] = new QuadTree<>(
                level + 1,
                new AABB( minX, minY, midX, midY),
                MAX_OBJECTS,
                MAX_LEVELS);

        // 1 = top-right
        children[1] = new QuadTree<>(
                level + 1,
                new AABB(midX, minY, maxX, midY),
                MAX_OBJECTS,
                MAX_LEVELS);

        // 2 = bottom-left
        children[2] = new QuadTree<>(
                level + 1,
                new AABB(minX, midY, midX, maxY),
                MAX_OBJECTS,
                MAX_LEVELS
        );

        // 3 = bottom-right
        children[3] = new QuadTree<>(
                level + 1,
                new AABB(midX, midY, maxX,maxY),
                MAX_OBJECTS,
                MAX_LEVELS
        );
    }

    // ------------------------------------------------------------
    // getContainingChild()
    // * Finds which child completely contains an AABB.
    // * Obs:
    // ** If an object doesn't fit completely inside one child,
    // ** return -1 and keep it in the current node.
    // ** This allows arbitrary-sized hitboxes.
    // ------------------------------------------------------------

    private int getContainingChild(AABB objectBounds) {

        double midX =
                (bounds.min_pos.x + bounds.max_pos.x) * 0.5;

        double midY =
                (bounds.min_pos.y + bounds.max_pos.y) * 0.5;

        boolean fitsLeft =
                objectBounds.min_pos.x >= bounds.min_pos.x &&
                objectBounds.max_pos.x <= midX;

        boolean fitsRight =
                objectBounds.min_pos.x >= midX &&
                objectBounds.max_pos.x <= bounds.max_pos.x;

        boolean fitsTop =
                objectBounds.min_pos.y >= bounds.min_pos.y &&
                objectBounds.max_pos.y <= midY;

        boolean fitsBottom =
                objectBounds.min_pos.y >= midY &&
                objectBounds.max_pos.y <= bounds.max_pos.y;

        if (fitsLeft && fitsTop) {
            return 0;
        }

        if (fitsRight && fitsTop) {
            return 1;
        }

        if (fitsLeft && fitsBottom) {
            return 2;
        }

        if (fitsRight && fitsBottom) {
            return 3;
        }

        return -1;
    }

    // ------------------------------------------------------------
    // Insert
    // ------------------------------------------------------------

    public void insert(T object, AABB objectBounds) {

        // Ignore objects completely outside this node.
        if (!bounds.intersect(objectBounds)) {
            return;
        }

        // If this node already has children, try to place the
        // object into one of them.
        if (children != null) {

            int childIndex = getContainingChild(objectBounds);

            if (childIndex != -1) {

                children[childIndex].insert(
                        object,
                        objectBounds
                );

                return;
            }

            // childIndex = -1 -> Object overlaps multiple children.
            // Keep it in this node.
        }

        objects.add(
                new Entry<>(object, objectBounds)
        );

        // Split if necessary.
        if (
                children == null &&
                objects.size() > MAX_OBJECTS &&
                level < MAX_LEVELS
        ) {

            split();

            boolean movedSomething = false;
            // Re-distribute objects that completely fit inside
            // one of the children.
            int i = 0;

            while (i < objects.size()) {

                Entry<T> entry = objects.get(i);

                int childIndex =
                        getContainingChild(entry.bounds);

                if (childIndex != -1) {

                    children[childIndex].insert(
                            entry.object,
                            entry.bounds
                    );

                    objects.remove(i);
                    movedSomething = true;

                } else {

                    i++;
                }
            }

            // if Splitting provided no benefit, removes all children
            if (movedSomething == false) {
            children = null;
            }
        }
    }

    // ------------------------------------------------------------
    // Query()
    // * Returns all objects whose AABBs intersect the target AABB.
    // ------------------------------------------------------------

    public List<T> query(AABB target) {

        List<T> result = new ArrayList<>();

        query(target, result);

        return result;
    }

    private void query(
            AABB target,
            List<T> result
    ) {

        // This node doesn't overlap the query.
        if (!bounds.intersect(target)) {
            return;
        }

        // Check objects stored directly in this node.
        for (Entry<T> entry : objects) {

            if (entry.bounds.intersect(target)) {
                result.add(entry.object);
            }
        }

        // Search children.
        if (children != null) {

            for (QuadTree<T> child : children) {
                child.query(target, result);
            }
        }
    }

    // ------------------------------------------------------------
    // Collision pairs
    // * Returns potential collision pairs based on AABB overlap.
    // ## THIS SHOULD ONLY BE USED IF THE NUMBER OF MOVABLE OBJECTS (M) IS FAR BIGGER THAN 
    // ## THE NUMBER OF IMOVABLE OBJECTS (I): M >= I^2 
    // ------------------------------------------------------------

    public List<CollisionPair<T>> getPotentialCollisions() {

        List<CollisionPair<T>> pairs =
                new ArrayList<>();

        Set<PairKey<T>> alreadyAdded =
                new HashSet<>();

        collectPotentialCollisions(
                pairs,
                alreadyAdded
        );

        return pairs;
    }

    private void collectPotentialCollisions(
            List<CollisionPair<T>> pairs,
            Set<PairKey<T>> alreadyAdded
    ) {

        // Compare objects within this node.
        for (int i = 0; i < objects.size(); i++) {

            Entry<T> a = objects.get(i);

            for (int j = i + 1; j < objects.size(); j++) {

                Entry<T> b = objects.get(j);

                if (a.bounds.intersect(b.bounds)) {

                    addPair(
                            a.object,
                            b.object,
                            pairs,
                            alreadyAdded);
                }
            }
        }

        // Objects stored in this node can collide with objects
        // stored in any child.
        if (children != null) {

            for (Entry<T> entry : objects) {

                for (QuadTree<T> child : children) {

                    child.collectCollisionsWith(
                            entry,
                            pairs,
                            alreadyAdded);
                }
            }

            // Continue recursively.
            for (QuadTree<T> child : children) {

                child.collectPotentialCollisions(
                        pairs,
                        alreadyAdded);
            }
        }
    }

    // ------------------------------------------------------------
    // Compare an object stored in a parent node against all
    // objects in a child.
    // ------------------------------------------------------------

    private void collectCollisionsWith(
            Entry<T> source,
            List<CollisionPair<T>> pairs,
            Set<PairKey<T>> alreadyAdded
    ) {

        if (!bounds.intersect(source.bounds)) {
            return;
        }

        for (Entry<T> target : objects) {

            if (source.bounds.intersect(target.bounds)) {

                addPair(
                        source.object,
                        target.object,
                        pairs,
                        alreadyAdded);
            }
        }

        if (children != null) {

            for (QuadTree<T> child : children) {

                child.collectCollisionsWith(
                        source,
                        pairs,
                        alreadyAdded);
            }
        }
    }

    // ------------------------------------------------------------
    // Add collision pair without duplicates.
    // ------------------------------------------------------------

    private void addPair(T a, T b, List<CollisionPair<T>> pairs, Set<PairKey<T>> alreadyAdded) 
    {

        if (a == b) {
            return;
        }

        PairKey<T> key = new PairKey<>(a, b);

        if (alreadyAdded.add(key)) {

            pairs.add(
                    new CollisionPair<>(a, b)
            );
        }
    }

    // ------------------------------------------------------------
    // Collision pair
    // ------------------------------------------------------------

    public static class CollisionPair<T> {

        public final T a;
        public final T b;

        public CollisionPair(T a, T b) {
            this.a = a;
            this.b = b;
        }
    }

    // ------------------------------------------------------------
    // Pair key
    //
    // Identity-based comparison is useful because game objects
    // may not override equals/hashCode.
    // ------------------------------------------------------------

    private static class PairKey<T> {

        private final T a;
        private final T b;

        PairKey(T a, T b) {

            // Canonical ordering based on object identity.
            if (System.identityHashCode(a) <=
                    System.identityHashCode(b)) 
            {
                this.a = a;
                this.b = b;
            } else {
                this.a = b;
                this.b = a;
            }
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            if (!(obj instanceof PairKey<?>)) {
                return false;
            }

            PairKey<?> other = (PairKey<?>) obj;

            return this.a == other.a &&
                   this.b == other.b;
        }

        @Override
        public int hashCode() {

            return
                    31 * System.identityHashCode(a) +
                    System.identityHashCode(b);
        }
    }
}