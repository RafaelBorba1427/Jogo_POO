public class MovableObj extends GameObject {
    // global quantifiers

    private static int global_quantity = 0;
    private static int global_active = 0;
    public boolean click = false;

    // movable objects variables
    protected Vector2D velocity = new Vector2D(0, 0);
    protected Vector2D acceleration = new Vector2D(0, GameRules.GRAVITY);

    protected double angular_velocity = 0.0;
    protected double angular_acceleration = 0.0;

    protected double elastic_factor;
    public static final double MIN_VELOCITY = 0.001,
            TERMINAL_VELOCITY = 30,
            MAX_VELOCITY = 50,
            MAX_ANGULAR_VELOCITY = 0.5;

    // ------------------------------------------------------------
    // Obj inherited methods
    // ------------------------------------------------------------

    MovableObj(double x_pos, double y_pos, double width, double height, double rotation, double mass, double friction,
            boolean movable, boolean rotatable, boolean active, int obj_id, double elastic_factor) {
        super(x_pos, y_pos, width, height, rotation, mass, friction, movable, rotatable, active, GameObject.MOVABLE_OBJ,
                obj_id);
        this.elastic_factor = elastic_factor;

        global_quantity++;
        if (active)
            global_active++;
    }

    @Override
    protected void updateInertialVariables() {

        this.inverse_mass = 1.0 / mass;
        this.moment_inertia = (mass * dimensions.lengthSquared()) / 12.0;
        this.inverse_moment_inertia = 1.0 / this.moment_inertia;
    }

    // ------------------------------------------------------------
    // Interface com o solver de colisao
    // ------------------------------------------------------------

    @Override
    public Vector2D getLinearVelocity() {
        return new Vector2D(velocity);
    }

    @Override
    public double getAngularVelocity() {
        return angular_velocity;
    }

    // Aplica o impulso no centro de massa e o torque correspondente.
    // O torque e o produto vetorial 2D entre o braco de alavanca e o impulso:
    // um impulso que passa pelo centro de massa (braco paralelo) nao gira nada,
    // um impulso na quina gira o maximo.
    @Override
    public void applyImpulse(Vector2D impulse, Vector2D contact_arm) {
        if (!active)
            return;

        velocity.x += impulse.x * inverse_mass;
        velocity.y += impulse.y * inverse_mass;

        if (rotatable) {
            angular_velocity += inverse_moment_inertia * contact_arm.cross(impulse);
        }
    }

    @Override
    public void translate(Vector2D delta) {
        position.x += delta.x;
        position.y += delta.y;
        updateHitBox();
    }

    @Override
    public double getRestitution() {
        return elastic_factor;
    }

    // ------------------------------------------------------------
    // MovableObj exclusive methods
    // ------------------------------------------------------------

    public Vector2D getVelocity() {
        return new Vector2D(velocity);
    }

    public Vector2D getAcceleration() {
        return new Vector2D(acceleration);
    }

    public void changeVelocity(double new_x_vel, double new_y_vel) {
        this.velocity.x = new_x_vel;
        this.velocity.y = new_y_vel;
    }

    public void addVelocity(double delta_x, double delta_y) {
        this.velocity.x = delta_x;
        this.velocity.y = delta_y;
    }

    public void changeAcceleration(double new_x_accel, double new_y_accel) {
        this.acceleration.x = new_x_accel;
        this.acceleration.y = new_y_accel;
    }

    public void changeAngularVelocity(double new_angular_velocity) {
        this.angular_velocity = new_angular_velocity;
    }

    public void addAngularVelocity(double delta) {
        this.angular_velocity += delta;
    }

    public void changeAngularAcceleration(double new_angular_acceleration) {
        this.angular_acceleration = new_angular_acceleration;
    }

    public void changeElasticFactor(double new_elastic_factor) {
        this.elastic_factor = new_elastic_factor;
    }

    // ------------------------------------------------------------
    // Integracao
    // ------------------------------------------------------------

    // Primeira metade: forcas -> velocidade.
    public void integrateForces(double dt) {
        if (!active)
            return;

        velocity.x += acceleration.x * dt;
        velocity.y += acceleration.y * dt;

        if (rotatable) {
            angular_velocity += angular_acceleration * dt;
        }

        clampVelocity();
    }

    // Segunda metade: velocidade -> posicao e rotacao.
    public void integrateVelocity(double dt) {
        if (!active)
            return;

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        if (rotatable && angular_velocity != 0.0) {
            rotation = Math.IEEEremainder(rotation + angular_velocity * dt, 2.0 * Math.PI);
        }

        updateHitBox();
    }

    // Zera velocidades residuais e aplica os limites de velocidade.
    protected void clampVelocity() {
        if (Math.abs(velocity.x) < MIN_VELOCITY)
            velocity.x = 0;
        else if (velocity.x > MAX_VELOCITY)
            velocity.x = MAX_VELOCITY;
        else if (velocity.x < -MAX_VELOCITY)
            velocity.x = -MAX_VELOCITY;

        if (Math.abs(velocity.y) < MIN_VELOCITY)
            velocity.y = 0;
        else if (velocity.y < -MAX_VELOCITY)
            velocity.y = -MAX_VELOCITY;
        else if (velocity.y > TERMINAL_VELOCITY)
            velocity.y = TERMINAL_VELOCITY;

        if (Math.abs(angular_velocity) < MIN_VELOCITY)
            angular_velocity = 0;
        else if (angular_velocity > MAX_ANGULAR_VELOCITY)
            angular_velocity = MAX_ANGULAR_VELOCITY;
        else if (angular_velocity < -MAX_ANGULAR_VELOCITY)
            angular_velocity = -MAX_ANGULAR_VELOCITY;
    }

    // Integracao completa, sem passar pelo solver.
    // Mantido para uso avulso e compatibilidade; o caminho normal e o
    // GameMap.step(), que intercala o solver entre as duas metades.
    public void update() {
        if (GameRules.physics_on && !(GameRules.ballGravity == false && obj_id != -1)) {
            integrateForces(1.0);
            integrateVelocity(1.0);
        }
    }
}
