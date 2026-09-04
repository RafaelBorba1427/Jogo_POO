import java.util.ArrayList;
import java.util.List;

// ------------------------------------------------------------
// CollisionManifold
// Descreve uma colisao entre dois GameObjects:
//
//   - os dois corpos envolvidos (A e B);
//   - a normal de colisao, unitaria, sempre apontando de A para B;
//   - a lista de pontos de contato (1 ou 2 em 2D);
//   - os coeficientes de restituicao e atrito do par.
//
// Alem de possui informações sobre a colisao, a classe tambem resolve as colisões.
// O ciclo de uso, executado pelo GameMap a cada passo de fisica, e:
//
//   1. generate(a, b)               -> detecta e constroi o manifold (null se nao colide)
//   2. inheritImpulses(anterior)    -> warm starting: recupera os impulsos do frame passado
//   3. preStep(1/dt)                -> calcula massas efetivas, bias de posicao e restituicao
//   4. warmStart()                  -> reaplica os impulsos herdados
//   5. solveVelocityConstraints()   -> N vezes (solver iterations)
//
// A resolucao usa impulsos sequenciais: em vez de resolver o sistema linear de
// todos os contatos de uma vez, cada contato eh corrigido isoladamente varias
// vezes seguidas. Com impulsos acumulados e clamp, isso converge para a solucao
// correta.
// ------------------------------------------------------------

public class CollisionManifold {

    // ------------------------------------------------------------
    // Solver Constants
    // ------------------------------------------------------------

    // Fracao da penetracao corrigida por passo (estabilizacao de Baumgarte).
    // Obs: Valores altos corrigem rapido mas injetam energia e causam tremor.
    public static final double BAUMGARTE = 0.2;

    // Penetracao tolerada sem correcao, em pixels. Evita que objetos em
    // repouso fiquem oscilando eternamente em torno do contato exato.
    public static final double PENETRATION_SLOP = 0.5;

    // Abaixo desta velocidade de aproximacao a colisao e tratada como
    // inelastica. Sem isso um objeto apoiado quicaria para sempre.
    public static final double RESTITUTION_THRESHOLD = 0.5;

    // Limite de quanto o bias de posicao pode empurrar por passo (pixels).
    public static final double MAX_LINEAR_CORRECTION = 4.0;

    private static final double EPSILON = 1e-9;

    // Tolerancias da escolha da face de referencia no SAT. Preferir a face do
    // corpo A quando as separacoes sao quase iguais evita que a normal fique
    // alternando entre os dois corpos de frame para frame.
    private static final double RELATIVE_TOLERANCE = 0.98;
    private static final double ABSOLUTE_TOLERANCE = 0.001;

    // ------------------------------------------------------------
    // Dados do manifold
    // ------------------------------------------------------------

    private final GameObject body_a;
    private final GameObject body_b;

    // Unitaria, aponta de A para B.
    private final Vector2D normal;

    // Normal girada 90 graus. Direcao em que o atrito age.
    private final Vector2D tangent;

    private final List<ContactPoint> contacts;

    private final double restitution;
    private final double friction;

    private CollisionManifold(GameObject a, GameObject b, Vector2D normal, List<ContactPoint> contacts) {
        this.body_a = a;
        this.body_b = b;
        this.normal = normal;
        this.tangent = new Vector2D(-normal.y, normal.x);
        this.contacts = contacts;

        // O par quica tanto quanto o mais elastico dos dois corpos.
        this.restitution = Math.max(a.getRestitution(), b.getRestitution());

        // Media geometrica, convencao comum.
        this.friction = Math.sqrt(Math.abs(a.getFriction() * b.getFriction()));
    }

    // ------------------------------------------------------------
    // Geracao do manifold (deteccao + pontos de contato)
    // ------------------------------------------------------------

    // Retorna o manifold da colisao entre a e b, ou null se nao ha colisao.
    // A normal resultante sempre aponta de a para b, entao a ORDEM DOS
    // ARGUMENTOS IMPORTA e deve ser estavel entre frames (o GameMap ordena
    // os corpos pelo uID para garantir isso).

    public static CollisionManifold generate(GameObject a, GameObject b) {
        if (a == null || b == null || a == b)
            return null;
        if (!a.isActive() || !b.isActive())
            return null;

        // Dois corpos de massa infinita não possuem resolução
        if (a.getInverseMass() == 0.0 && b.getInverseMass() == 0.0)
            return null;

        HitBox hitbox_a = a.getHitBox();
        HitBox hitbox_b = b.getHitBox();
        if (hitbox_a == null || hitbox_b == null)
            return null;

        // Teste rápido e barato antes do check geometrico mais caro
        if (!hitbox_a.getAABB().intersect(hitbox_b.getAABB()))
            return null;

        if (hitbox_a instanceof RectangularHitBox && hitbox_b instanceof RectangularHitBox) {
            return rectangleVsRectangle(a, b, (RectangularHitBox) hitbox_a, (RectangularHitBox) hitbox_b);
        }
        if (hitbox_a instanceof RectangularHitBox && hitbox_b instanceof CircularHitBox) {
            return rectangleVsCircle(a, b, (RectangularHitBox) hitbox_a, (CircularHitBox) hitbox_b, true);
        }
        if (hitbox_a instanceof CircularHitBox && hitbox_b instanceof RectangularHitBox) {
            return rectangleVsCircle(a, b, (RectangularHitBox) hitbox_b, (CircularHitBox) hitbox_a, false);
        }
        if (hitbox_a instanceof CircularHitBox && hitbox_b instanceof CircularHitBox) {
            return circleVsCircle(a, b, (CircularHitBox) hitbox_a, (CircularHitBox) hitbox_b);
        }

        return null;
    }

    // ------------------------------------------------------------
    // Retangulo x Retangulo : SAT + clipping de Sutherland-Hodgman
    // ------------------------------------------------------------
    //
    // 1. O SAT devolve, para cada corpo, a face cuja separacao e maxima.
    // Se alguma separacao for positiva os corpos nao se tocam.
    // 2. O corpo cuja separacao eh maior vira o corpo de REFERENCIA, e sua
    // face vira o plano contra o qual tudo e medido.
    // 3. No outro corpo (INCIDENTE) escolhe-se a face mais anti-paralela
    // a normal de referencia.
    // 4. A aresta incidente eh recortada pelos dois planos laterais da face de
    // referencia; o que sobra e que fica atras do plano da face sao os
    // pontos de contato, com a profundidade de cada um.
    //
    // O passo 4 e o que produz DOIS pontos quando duas faces se encostam, e e
    // ele que permite que uma caixa fique apoiada sem cair de lado.

    private static CollisionManifold rectangleVsRectangle(GameObject a, GameObject b,
            RectangularHitBox rect_a, RectangularHitBox rect_b) {
        int[] face_a = new int[1];
        double separation_a = maxSeparation(face_a, rect_a, rect_b);
        if (separation_a > 0.0)
            return null;

        int[] face_b = new int[1];
        double separation_b = maxSeparation(face_b, rect_b, rect_a);
        if (separation_b > 0.0)
            return null;

        RectangularHitBox reference, incident;
        int reference_face;
        boolean flipped;

        if (separation_b > RELATIVE_TOLERANCE * separation_a + ABSOLUTE_TOLERANCE) {
            reference = rect_b;
            incident = rect_a;
            reference_face = face_b[0];
            flipped = true;
        } else {
            reference = rect_a;
            incident = rect_b;
            reference_face = face_a[0];
            flipped = false;
        }

        Vector2D reference_normal = reference.getFaceNormal(reference_face);
        Vector2D[] reference_corners = reference.getCorners();
        Vector2D reference_start = reference_corners[reference_face];
        Vector2D reference_end = reference_corners[(reference_face + 1) % 4];
        Vector2D side = reference_end.subtract(reference_start).normalize();

        int incident_face = incidentFace(incident, reference_normal);
        Vector2D[] incident_corners = incident.getCorners();

        ClipVertex[] edge = new ClipVertex[] {
                new ClipVertex(incident_corners[incident_face],
                        featureId(reference_face, incident_face, 0, flipped)),
                new ClipVertex(incident_corners[(incident_face + 1) % 4],
                        featureId(reference_face, incident_face, 1, flipped))
        };

        // Recorta pelos dois planos laterais da face de referencia.
        edge = clipSegment(edge, side.multiply(-1), reference_start,
                featureId(reference_face, incident_face, 2, flipped));
        if (edge == null)
            return null;

        edge = clipSegment(edge, side, reference_end,
                featureId(reference_face, incident_face, 3, flipped));
        if (edge == null)
            return null;

        double face_offset = reference_start.dot(reference_normal);

        List<ContactPoint> contacts = new ArrayList<>(2);
        for (ClipVertex vertex : edge) {
            double separation = vertex.point.dot(reference_normal) - face_offset;
            if (separation <= 0.0) {
                contacts.add(new ContactPoint(vertex.point, -separation, vertex.id));
            }
        }
        if (contacts.isEmpty())
            return null;

        // reference_normal aponta para fora do corpo de referencia, ou seja,
        // do corpo de referencia para o incidente. Se a referencia virou B,
        // e preciso inverter para manter a convencao "de A para B".
        Vector2D manifold_normal = flipped ? reference_normal.multiply(-1) : reference_normal;

        return new CollisionManifold(a, b, manifold_normal, contacts);
    }

    // Maior separacao entre as faces de "reference" e o corpo "other".
    // Negativa quando ha sobreposicao; o indice da face fica em best_face[0].
    private static double maxSeparation(int[] best_face, RectangularHitBox reference, RectangularHitBox other) {
        Vector2D[] reference_corners = reference.getCorners();
        Vector2D[] other_corners = other.getCorners();

        double best_separation = -Double.MAX_VALUE;
        int best_index = 0;

        for (int i = 0; i < 4; i++) {
            Vector2D face_normal = reference.getFaceNormal(i);

            // Ponto de suporte de "other" na direcao oposta a normal:
            // o vertice mais fundo dentro da face.
            double support = Double.MAX_VALUE;
            for (Vector2D corner : other_corners) {
                double projection = corner.dot(face_normal);
                if (projection < support)
                    support = projection;
            }

            double separation = support - reference_corners[i].dot(face_normal);
            if (separation > best_separation) {
                best_separation = separation;
                best_index = i;
            }
        }

        best_face[0] = best_index;
        return best_separation;
    }

    // Face do corpo incidente mais anti-paralela a normal de referencia.
    private static int incidentFace(RectangularHitBox incident, Vector2D reference_normal) {
        int best_index = 0;
        double smallest_dot = Double.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            double dot = incident.getFaceNormal(i).dot(reference_normal);
            if (dot < smallest_dot) {
                smallest_dot = dot;
                best_index = i;
            }
        }
        return best_index;
    }

    // Recorta o segmento mantendo apenas o que esta do lado de dentro do plano,
    // isto e, onde (p - plane_point) . plane_normal <= 0.
    // Retorna sempre 2 vertices, ou null se o segmento ficou completamente fora.
    private static ClipVertex[] clipSegment(ClipVertex[] input, Vector2D plane_normal,
            Vector2D plane_point, int new_id) {
        ClipVertex[] output = new ClipVertex[2];
        int count = 0;

        double distance_0 = input[0].point.subtract(plane_point).dot(plane_normal);
        double distance_1 = input[1].point.subtract(plane_point).dot(plane_normal);

        if (distance_0 <= 0.0)
            output[count++] = input[0];
        if (distance_1 <= 0.0 && count < 2)
            output[count++] = input[1];

        // O segmento cruza o plano: gera o vertice da intersecao.
        if (distance_0 * distance_1 < 0.0 && count < 2) {
            double t = distance_0 / (distance_0 - distance_1);
            Vector2D crossing = input[0].point.add(input[1].point.subtract(input[0].point).multiply(t));
            output[count++] = new ClipVertex(crossing, new_id);
        }

        if (count < 2)
            return null;
        return output;
    }

    // Empacota a origem do contato em um inteiro estavel entre frames.
    private static int featureId(int reference_face, int incident_face, int slot, boolean flipped) {
        return ((reference_face & 3) << 6) | ((incident_face & 3) << 4) | ((slot & 3) << 2) | (flipped ? 1 : 0);
    }

    // Vertice temporario usado durante o clipping.
    private static class ClipVertex {
        final Vector2D point;
        final int id;

        ClipVertex(Vector2D point, int id) {
            this.point = point;
            this.id = id;
        }
    }

    // ------------------------------------------------------------
    // Retangulo x Circulo
    // ------------------------------------------------------------
    //
    // O circulo e levado para o sistema local do retangulo (eixos X e Y do OBB),
    // onde o problema vira um AABB x circulo. Dois casos:
    //
    // a) centro do circulo FORA do retangulo -> o ponto mais proximo e o
    // centro clampado nos meios-lados; a normal e a direcao desse ponto
    // ate o centro do circulo.
    //
    // b) centro do circulo DENTRO do retangulo -> nao existe direcao de saida
    // bem definida pelo ponto mais proximo (a distancia seria zero), entao
    // empurra-se pela face mais proxima, e a penetracao inclui o raio
    // inteiro mais o quanto o centro entrou.

    private static CollisionManifold rectangleVsCircle(GameObject a, GameObject b,
            RectangularHitBox rect, CircularHitBox circle,
            boolean rectangle_is_a) {
        Vector2D rect_center = rect.getCenter();
        Vector2D circle_center = circle.getCenter();
        double radius = circle.getRadius();

        Vector2D axis_x = rect.getAxisX();
        Vector2D axis_y = rect.getAxisY();
        double half_width = rect.getHalfWidth();
        double half_height = rect.getHalfHeight();

        Vector2D relative = circle_center.subtract(rect_center);
        double local_x = relative.dot(axis_x);
        double local_y = relative.dot(axis_y);

        boolean center_inside = Math.abs(local_x) <= half_width && Math.abs(local_y) <= half_height;

        Vector2D normal; // do retangulo para o circulo
        Vector2D contact_point;
        double penetration;

        if (!center_inside) {
            double closest_x = clamp(local_x, -half_width, half_width);
            double closest_y = clamp(local_y, -half_height, half_height);

            Vector2D closest = rect_center
                    .add(axis_x.multiply(closest_x))
                    .add(axis_y.multiply(closest_y));

            Vector2D difference = circle_center.subtract(closest);
            double distance_squared = difference.lengthSquared();

            if (distance_squared > radius * radius)
                return null;

            double distance = Math.sqrt(distance_squared);

            if (distance > EPSILON) {
                normal = difference.multiply(1.0 / distance);
            } else {
                // Centro exatamente sobre a borda: escolhe o eixo dominante.
                normal = (Math.abs(local_x) > Math.abs(local_y))
                        ? axis_x.multiply(local_x < 0 ? -1 : 1)
                        : axis_y.multiply(local_y < 0 ? -1 : 1);
            }

            penetration = radius - distance;
            contact_point = closest;

        } else {
            double overlap_x = half_width - Math.abs(local_x);
            double overlap_y = half_height - Math.abs(local_y);

            if (overlap_x < overlap_y) {
                double sign = (local_x < 0) ? -1.0 : 1.0;
                normal = axis_x.multiply(sign);
                penetration = radius + overlap_x;
                contact_point = rect_center
                        .add(axis_x.multiply(sign * half_width))
                        .add(axis_y.multiply(local_y));
            } else {
                double sign = (local_y < 0) ? -1.0 : 1.0;
                normal = axis_y.multiply(sign);
                penetration = radius + overlap_y;
                contact_point = rect_center
                        .add(axis_x.multiply(local_x))
                        .add(axis_y.multiply(sign * half_height));
            }
        }

        List<ContactPoint> contacts = new ArrayList<>(1);
        contacts.add(new ContactPoint(contact_point, penetration, ContactPoint.SINGLE_CONTACT_ID));

        // A normal foi calculada do retangulo para o circulo. A convencao do
        // manifold e de A para B, entao inverte quando o circulo e o corpo A.
        Vector2D manifold_normal = rectangle_is_a ? normal : normal.multiply(-1);

        return new CollisionManifold(a, b, manifold_normal, contacts);
    }

    // ------------------------------------------------------------
    // Circulo x Circulo
    // ------------------------------------------------------------

    private static CollisionManifold circleVsCircle(GameObject a, GameObject b,
            CircularHitBox circle_a, CircularHitBox circle_b) {
        Vector2D difference = circle_b.getCenter().subtract(circle_a.getCenter());
        double radius_sum = circle_a.getRadius() + circle_b.getRadius();
        double distance_squared = difference.lengthSquared();

        if (distance_squared > radius_sum * radius_sum)
            return null;

        double distance = Math.sqrt(distance_squared);

        // Centros coincidentes: qualquer direcao serve, escolhe uma fixa
        // para que o resultado seja deterministico.
        Vector2D normal = (distance > EPSILON)
                ? difference.multiply(1.0 / distance)
                : new Vector2D(0, -1);

        double penetration = radius_sum - distance;

        // Ponto no meio da regiao sobreposta.
        Vector2D contact_point = circle_a.getCenter()
                .add(normal.multiply(circle_a.getRadius() - penetration * 0.5));

        List<ContactPoint> contacts = new ArrayList<>(1);
        contacts.add(new ContactPoint(contact_point, penetration, ContactPoint.SINGLE_CONTACT_ID));

        return new CollisionManifold(a, b, normal, contacts);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ------------------------------------------------------------
    // Warm starting
    // ------------------------------------------------------------

    // Copia os impulsos acumulados do manifold equivalente do frame anterior.
    // Contatos sao pareados pelo feature_id, entao um contato que mudou de
    // face (o objeto girou, por exemplo) corretamente comeca do zero.
    public void inheritImpulses(CollisionManifold previous) {
        if (previous == null)
            return;

        boolean same_order = (previous.body_a == this.body_a && previous.body_b == this.body_b);

        for (ContactPoint contact : contacts) {
            for (ContactPoint old_contact : previous.contacts) {
                if (old_contact.feature_id != contact.feature_id)
                    continue;

                contact.normal_impulse = old_contact.normal_impulse;
                // Se a ordem dos corpos inverteu, a tangente inverte junto.
                contact.tangent_impulse = same_order ? old_contact.tangent_impulse
                        : -old_contact.tangent_impulse;
                break;
            }
        }
    }

    // ------------------------------------------------------------
    // Preparacao do passo
    // ------------------------------------------------------------

    // Calcula, uma unica vez por passo, tudo que nao muda durante as iteracoes:
    // bracos de alavanca, massas efetivas, bias de posicao e de restituicao.
    public void preStep(double inverse_dt) {
        Vector2D center_a = body_a.getCenterOfMass();
        Vector2D center_b = body_b.getCenterOfMass();

        double inverse_mass_a = body_a.getInverseMass();
        double inverse_mass_b = body_b.getInverseMass();
        double inverse_inertia_a = body_a.getInverseMomentOfInertia();
        double inverse_inertia_b = body_b.getInverseMomentOfInertia();

        for (ContactPoint contact : contacts) {
            contact.ra = contact.position.subtract(center_a);
            contact.rb = contact.position.subtract(center_b);

            // Massa efetiva na normal: massa linear dos dois corpos mais o
            // quanto cada um resiste a girar em torno do ponto de contato.
            double rn_a = contact.ra.cross(normal);
            double rn_b = contact.rb.cross(normal);

            double k_normal = inverse_mass_a + inverse_mass_b
                    + inverse_inertia_a * rn_a * rn_a
                    + inverse_inertia_b * rn_b * rn_b;
            contact.normal_mass = (k_normal > EPSILON) ? 1.0 / k_normal : 0.0;

            double rt_a = contact.ra.cross(tangent);
            double rt_b = contact.rb.cross(tangent);
            double k_tangent = inverse_mass_a + inverse_mass_b
                    + inverse_inertia_a * rt_a * rt_a
                    + inverse_inertia_b * rt_b * rt_b;
            contact.tangent_mass = (k_tangent > EPSILON) ? 1.0 / k_tangent : 0.0;

            // Baumgarte: velocidade extra proporcional ao excesso de penetracao.
            double correction = Math.max(0.0, contact.penetration - PENETRATION_SLOP);
            contact.position_bias = Math.min(BAUMGARTE * inverse_dt * correction,
                    MAX_LINEAR_CORRECTION * inverse_dt);

            // Restituicao medida ANTES de qualquer impulso ser aplicado.
            // Velocidade normal negativa significa aproximacao.
            double approach_velocity = relativeVelocityAt(contact).dot(normal);
            contact.restitution_bias = (approach_velocity < -RESTITUTION_THRESHOLD)
                    ? -restitution * approach_velocity
                    : 0.0;
        }
    }

    // Reaplica os impulsos herdados do frame anterior. Precisa rodar depois de
    // preStep (usa ra e rb) e antes das iteracoes do solver.
    public void warmStart() {
        for (ContactPoint contact : contacts) {
            Vector2D impulse = normal.multiply(contact.normal_impulse)
                    .add(tangent.multiply(contact.tangent_impulse));

            body_a.applyImpulse(impulse.multiply(-1), contact.ra);
            body_b.applyImpulse(impulse, contact.rb);
        }
    }

    // ------------------------------------------------------------
    // Uma iteracao do solver
    // ------------------------------------------------------------

    public void solveVelocityConstraints() {
        for (ContactPoint contact : contacts) {

            // ---- Impulso normal (nao interpenetrar / quicar) ----
            double normal_velocity = relativeVelocityAt(contact).dot(normal);

            double lambda = contact.normal_mass
                    * (-normal_velocity + contact.position_bias + contact.restitution_bias);

            // Clamp do impulso ACUMULADO, nao do incremento: e isso que permite
            // que iteracoes sucessivas corrijam umas as outras sem que o
            // contato chegue a "puxar" os corpos.
            double previous_normal = contact.normal_impulse;
            contact.normal_impulse = Math.max(previous_normal + lambda, 0.0);
            lambda = contact.normal_impulse - previous_normal;

            Vector2D normal_impulse = normal.multiply(lambda);
            body_a.applyImpulse(normal_impulse.multiply(-1), contact.ra);
            body_b.applyImpulse(normal_impulse, contact.rb);

            // ---- Impulso tangencial (atrito de Coulomb) ----
            double tangent_velocity = relativeVelocityAt(contact).dot(tangent);

            double lambda_tangent = contact.tangent_mass * (-tangent_velocity);

            double max_friction = friction * contact.normal_impulse;
            double previous_tangent = contact.tangent_impulse;
            contact.tangent_impulse = clamp(previous_tangent + lambda_tangent,
                    -max_friction, max_friction);
            lambda_tangent = contact.tangent_impulse - previous_tangent;

            Vector2D tangent_impulse = tangent.multiply(lambda_tangent);
            body_a.applyImpulse(tangent_impulse.multiply(-1), contact.ra);
            body_b.applyImpulse(tangent_impulse, contact.rb);
        }
    }

    // Velocidade do corpo B em relacao ao corpo A, medida no ponto de contato.
    // Inclui a contribuicao rotacional: v_ponto = v + w x r, que em 2D e
    // simplesmente (-w * r.y, w * r.x).
    private Vector2D relativeVelocityAt(ContactPoint contact) {
        Vector2D velocity_a = body_a.getLinearVelocity();
        Vector2D velocity_b = body_b.getLinearVelocity();
        double angular_a = body_a.getAngularVelocity();
        double angular_b = body_b.getAngularVelocity();

        Vector2D point_velocity_a = velocity_a.add(
                new Vector2D(-angular_a * contact.ra.y, angular_a * contact.ra.x));
        Vector2D point_velocity_b = velocity_b.add(
                new Vector2D(-angular_b * contact.rb.y, angular_b * contact.rb.x));

        return point_velocity_b.subtract(point_velocity_a);
    }

    // ------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------

    public GameObject getBodyA() {
        return body_a;
    }

    public GameObject getBodyB() {
        return body_b;
    }

    public Vector2D getNormal() {
        return new Vector2D(normal);
    }

    public Vector2D getTangent() {
        return new Vector2D(tangent);
    }

    public List<ContactPoint> getContacts() {
        return contacts;
    }

    public int getContactCount() {
        return contacts.size();
    }

    public double getRestitution() {
        return restitution;
    }

    public double getFriction() {
        return friction;
    }

    // Maior penetracao entre os contatos. Util para depuracao.
    public double getMaxPenetration() {
        double deepest = 0.0;
        for (ContactPoint contact : contacts) {
            if (contact.penetration > deepest)
                deepest = contact.penetration;
        }
        return deepest;
    }

    @Override
    public String toString() {
        return "CollisionManifold[" + body_a.getUid() + " x " + body_b.getUid()
                + "] n=" + normal + " contacts=" + contacts.size()
                + " depth=" + getMaxPenetration();
    }
}
