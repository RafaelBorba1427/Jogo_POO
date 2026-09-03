// ------------------------------------------------------------
// RectangularHitBox
// Retangulo orientado (OBB): centro, meias-dimensoes e rotacao.
//
//Obs:
// A geracao de manifolds e os pontos de contato NAO ficam aqui: sao
// responsabilidade da classe CollisionManifold. Esta classe cuida so da
// geometria (vertices, eixos, normais de face) e dos testes booleanos.
// ------------------------------------------------------------

public class RectangularHitBox implements HitBox {

    private Vector2D center;

    // Half Dimensions. Ex: width = 100 -> halfWidth = 50.
    private double halfWidth;
    private double halfHeight;

    // Pre-computed sin and cos.
    private double rotation;
    private double cos, sin;

    // Axis Aligned Bounding Box that involves the whole OBB.
    private AABB aabb;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    public RectangularHitBox(double x, double y, double width, double height, double rotation) {
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;
        this.center = new Vector2D(x + halfWidth, y + halfHeight);
        this.rotation = rotation;
        updateCosSin();
        this.aabb = new AABB(this);
    }

    public RectangularHitBox(Vector2D top_left, Vector2D dimensions, double rotation) {
        this(top_left.x, top_left.y, dimensions.x, dimensions.y, rotation);
    }

    // Alternative Construtor if the center is known.
    public static RectangularHitBox fromCenter(Vector2D center, Vector2D dimensions, double rotation) {
        return new RectangularHitBox(center.x - dimensions.x / 2.0,
                                     center.y - dimensions.y / 2.0,
                                     dimensions.x, dimensions.y, rotation);
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------

    @Override
    public Vector2D getCenter() {
        return center;
    }

    //Natural Render position
    public Vector2D getTopLeft() {
        return new Vector2D(center.x - halfWidth, center.y - halfHeight);
    }

    public double getWidth() {
        return halfWidth * 2.0;
    }

    public double getHeight() {
        return halfHeight * 2.0;
    }

    public double getHalfWidth() {
        return halfWidth;
    }

    public double getHalfHeight() {
        return halfHeight;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public AABB getAABB() {
        return this.aabb;
    }

    // Position by center
    public void setPosition(double x, double y) {
        center.x = x;
        center.y = y;
        aabb.update(this);
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
        updateCosSin();
        aabb.update(this);
    }

    public void rotate(double amount) {
        this.rotation += amount;
        updateCosSin();
        aabb.update(this);
    }

    private void updateCosSin() {
        this.cos = Math.cos(rotation);
        this.sin = Math.sin(rotation);
    }

    public void updateHitBox(Vector2D top_left, Vector2D dimensions, double rotation) {
        this.halfWidth = dimensions.x / 2.0;
        this.halfHeight = dimensions.y / 2.0;
        this.center.x = top_left.x + halfWidth;
        this.center.y = top_left.y + halfHeight;

        if (this.rotation != rotation) {
            this.rotation = rotation;
            updateCosSin();
        }

        this.aabb.update(this);
    }

    // ------------------------------------------------------------
    // Axis Edges and Normals
    // ------------------------------------------------------------

    // Local X axis
    public Vector2D getAxisX() {
        return new Vector2D(cos, sin);
    }

    // Local Y axis
    public Vector2D getAxisY() {
        return new Vector2D(-sin, cos);
    }

    //Obs:
    // Os quatro vertices em coordenadas do mundo.
    //
    // A ordem importa e e um contrato com getFaceNormal(): a face i vai do
    // vertice i ao vertice (i+1) % 4.
    //
    //   0 = centro + X + Y
    //   1 = centro - X + Y
    //   2 = centro - X - Y
    //   3 = centro + X - Y
    public Vector2D[] getCorners() {
        Vector2D x = getAxisX().multiply(halfWidth);
        Vector2D y = getAxisY().multiply(halfHeight);

        return new Vector2D[] {
            center.add(x).add(y),
            center.subtract(x).add(y),
            center.subtract(x).subtract(y),
            center.add(x).subtract(y)
        };
    }

    //Obs:
    // Normal externa da face i, unitaria.
    // Face 0 = vertices 0-1, face 1 = 1-2, face 2 = 2-3, face 3 = 3-0.
    public Vector2D getFaceNormal(int face) {
        switch (face & 3) {
            case 0:  return getAxisY();
            case 1:  return getAxisX().multiply(-1);
            case 2:  return getAxisY().multiply(-1);
            default: return getAxisX();
        }
    }

    //Obs:
    // Os dois vertices que formam a face i.
    public Vector2D[] getFace(int face) {
        Vector2D[] corners = getCorners();
        return new Vector2D[] { corners[face & 3], corners[(face + 1) & 3] };
    }

    // ------------------------------------------------------------
    // Overlap Detection
    // ------------------------------------------------------------

    @Override
    public boolean intersects(HitBox other) {
        if (other instanceof RectangularHitBox) return intersects((RectangularHitBox) other);
        if (other instanceof CircularHitBox) return intersects((CircularHitBox) other);
        return false;
    }

    // Retangulo x retangulo pelo Teorema dos Eixos Separadores.
    public boolean intersects(RectangularHitBox other) {
        Vector2D[] axes = { getAxisX(), getAxisY(), other.getAxisX(), other.getAxisY() };

        for (Vector2D axis : axes) {
            Projection projection_a = projectOnto(axis);
            Projection projection_b = other.projectOnto(axis);

            // Um unico eixo separador ja prova que nao ha colisao.
            if (projection_a.isSeparatedFrom(projection_b)) return false;
        }
        return true;
    }

    // Retangulo x circulo. Trata explicitamente o caso do centro do circulo
    public boolean intersects(CircularHitBox circle) {
        Vector2D relative = circle.getCenter().subtract(center);

        double local_x = relative.dot(getAxisX());
        double local_y = relative.dot(getAxisY());

        if (Math.abs(local_x) <= halfWidth && Math.abs(local_y) <= halfHeight) {
            return true;
        }

        double closest_x = valueBoundedInRange(local_x, -halfWidth, halfWidth);
        double closest_y = valueBoundedInRange(local_y, -halfHeight, halfHeight);

        double difference_x = local_x - closest_x;
        double difference_y = local_y - closest_y;

        double radius = circle.getRadius();
        return (difference_x * difference_x + difference_y * difference_y) <= radius * radius;
    }

    // Ponto dentro do retangulo rotacionado.
    @Override
    public boolean contains(Vector2D point) {
        Vector2D relative = point.subtract(center);

        double local_x = relative.dot(getAxisX());
        double local_y = relative.dot(getAxisY());

        return Math.abs(local_x) <= halfWidth && Math.abs(local_y) <= halfHeight;
    }

    // Ponto do retangulo mais proximo de um ponto qualquer do mundo.
    public Vector2D closestPointTo(Vector2D point) {
        Vector2D relative = point.subtract(center);
        Vector2D axis_x = getAxisX();
        Vector2D axis_y = getAxisY();

        double local_x = valueBoundedInRange(relative.dot(axis_x), -halfWidth, halfWidth);
        double local_y = valueBoundedInRange(relative.dot(axis_y), -halfHeight, halfHeight);

        return center.add(axis_x.multiply(local_x)).add(axis_y.multiply(local_y));
    }

    private static double valueBoundedInRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ------------------------------------------------------------
    // Projection
    // ------------------------------------------------------------

    private static class Projection {
        final double min;
        final double max;

        Projection(double min, double max) {
            this.min = min;
            this.max = max;
        }

        boolean isSeparatedFrom(Projection other) {
            return max < other.min || other.max < min;
        }
    }

    // Projeta o retangulo em um eixo arbitrario.
    private Projection projectOnto(Vector2D axis) {
        Vector2D normalized = axis.normalize();

        // O raio da projecao de um OBB em um eixo e
        // |eixo . X| * halfWidth + |eixo . Y| * halfHeight.
        double radius = Math.abs(normalized.dot(getAxisX())) * halfWidth
                      + Math.abs(normalized.dot(getAxisY())) * halfHeight;

        double projected_center = center.dot(normalized);

        return new Projection(projected_center - radius, projected_center + radius);
    }

    @Override
    public String toString() {
        return "RectangularHitBox[center=" + center
             + " half=(" + halfWidth + ", " + halfHeight + ")"
             + " rot=" + rotation + "]";
    }
}
