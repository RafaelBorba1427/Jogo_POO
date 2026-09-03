// ------------------------------------------------------------
// HitBox
// Interface comum a todas as formas de colisao.
// ------------------------------------------------------------
public interface HitBox {

    // Caixa alinhada aos eixos que envolve a forma. Usada pela QuadTree.
    AABB getAABB();

    // Centro geometrico da forma, em coordenadas do mundo.
    Vector2D getCenter();

    // Teste de sobreposicao contra qualquer outra hitbox.
    boolean intersects(HitBox other);

    // Verdadeiro se o ponto esta dentro da forma.
    boolean contains(Vector2D point);
}
