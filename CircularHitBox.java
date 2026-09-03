// ------------------------------------------------------------
// CircularHitBox
// Circulo definido por centro e raio.
//
// Obs:
// Convencao de posicao: assim como a RectangularHitBox, o construtor recebe o
// CANTO SUPERIOR ESQUERDO do quadrado que envolve o circulo e o DIAMETRO, que
// e exatamente o que GameObject.position e GameObject.dimensions guardam. O
// centro e derivado internamente.
// ------------------------------------------------------------

public class CircularHitBox implements HitBox {

    private Vector2D center;

    private double radius;

    private AABB aabb;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    // x, y = x,y in the top left, natural render
    public CircularHitBox(double x, double y, double diameter) {
        this.radius = diameter / 2.0;
        this.center = new Vector2D(x + radius, y + radius);
        this.aabb = new AABB(this);
    }

    public CircularHitBox(Vector2D top_left, double diameter) {
        this(top_left.x, top_left.y, diameter);
    }

    // Construtor alternativo para quem ja tem o centro pronto.
    public static CircularHitBox fromCenter(Vector2D center, double radius) {
        return new CircularHitBox(center.x - radius, center.y - radius, radius * 2.0);
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------

    @Override
    public Vector2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public double getDiameter() {
        return radius * 2.0;
    }

    @Override
    public AABB getAABB() {
        return this.aabb;
    }

    public void setCenter(double x, double y) {
        center.x = x;
        center.y = y;
        aabb.update(this);
    }

    public void setRadius(double radius) {
        this.radius = radius;
        aabb.update(this);
    }

    public void updateHitBox(Vector2D top_left, double diameter) {
        this.radius = diameter / 2.0;
        this.center.x = top_left.x + radius;
        this.center.y = top_left.y + radius;
        this.aabb.update(this);
    }

    // ------------------------------------------------------------
    // Overlap Detection
    // ------------------------------------------------------------

    @Override
    public boolean intersects(HitBox other) {
        if (other instanceof CircularHitBox) return intersects((CircularHitBox) other);
        if (other instanceof RectangularHitBox) return intersects((RectangularHitBox) other);
        return false;
    }

    public boolean intersects(CircularHitBox circle) {
        double radius_sum = this.radius + circle.radius;
        return this.center.subtract(circle.getCenter()).lengthSquared() <= radius_sum * radius_sum;
    }

    // Circulo x retangulo
    public boolean intersects(RectangularHitBox rectangle) {
        Vector2D relative = this.center.subtract(rectangle.getCenter());

        double local_x = relative.dot(rectangle.getAxisX());
        double local_y = relative.dot(rectangle.getAxisY());

        double half_width = rectangle.getHalfWidth();
        double half_height = rectangle.getHalfHeight();

        // Centro do circulo dentro do retangulo: sobreposicao garantida.
        if (Math.abs(local_x) <= half_width && Math.abs(local_y) <= half_height) {
            return true;
        }

        // Caso contrario, mede a distancia ate o ponto mais proximo do retangulo
        double closest_x = Math.max(-half_width, Math.min(half_width, local_x));
        double closest_y = Math.max(-half_height, Math.min(half_height, local_y));

        double difference_x = local_x - closest_x;
        double difference_y = local_y - closest_y;

        return (difference_x * difference_x + difference_y * difference_y) <= radius * radius;
    }

    @Override
    public boolean contains(Vector2D point) {
        return point.subtract(center).lengthSquared() <= radius * radius;
    }

    @Override
    public String toString() {
        return "CircularHitBox[center=" + center + " r=" + radius + "]";
    }
}
