class PhysicsSolver {

  static double elasticity(double ea, double eb) {
    return (ea + eb) / 2;
  }

  static Vector2D Vrel(Vector2D ra, Vector2D rb, MovableObj obj1, MovableObj obj2) {
    Vector2D result1 = obj2.velocity.add(Vector2D.cross_especial(rb, obj2.angular_velocity));

    Vector2D result2 = obj1.velocity.add(Vector2D.cross_especial(ra, obj1.angular_velocity));
    return result1.subtract(result2);
  }

  static double very_hard_math(Vector2D ra, Vector2D normal) {
    return ra.cross(normal) * ra.cross(normal);

  }

  static double very_hard_math_2(Vector2D ra, Vector2D rb, Vector2D normal, MovableObj obj1, MovableObj obj2) {
    return obj1.inverse_mass + obj2.inverse_mass + very_hard_math(ra, normal) * obj1.inverse_moment_inertia
        + very_hard_math(rb, normal) * obj2.inverse_moment_inertia;
  }

  static double fritcion_prod(double fritcion1, double fritcion2) {
    return Math.sqrt(fritcion1 * fritcion2);
  }

  static double impulse_tangent(MovableObj obj1, MovableObj obj2, Vector2D tangent, Vector2D ra, Vector2D rb,
      double jn) {
    double jt = (-Vrel(ra, rb, obj1, obj2).dot(tangent))
        / (very_hard_math_2(ra, rb, tangent, obj1, obj2));

    return Math.max(jt,
        Math.min(-fritcion_prod(obj1.friction, obj2.friction) * jn, fritcion_prod(obj1.friction, obj2.friction) * jn));

  }

  static Vector2D final_impulse(double jn, double jt, Vector2D normal, Vector2D tangent) {
    return normal.multiply(jn).add(tangent.multiply(jt));
  }

  static double impulse_normal(MovableObj obj1, MovableObj obj2, Vector2D normal, Vector2D ra, Vector2D rb) {
    return (-(1 + elasticity(obj1.elasticity, obj2.elasticity)) * Vrel(ra, rb, obj1, obj2).dot(normal))
        / (very_hard_math_2(ra, rb, normal, obj1, obj2));
  }

}
