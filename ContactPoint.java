// ------------------------------------------------------------
// ContactPoint
// Um unico ponto de contato dentro de um CollisionManifold.
//
// Guarda tres tipos de informacao:
//  1. Geometria do contato  -> posicao no mundo e profundidade de penetracao.
//  2. Estado do solver      -> impulsos acumulados ao longo das iteracoes.
//  3. Cache do passo atual  -> bracos de alavanca e massas efetivas, calculados
//                              uma unica vez por passo em CollisionManifold.preStep().
//
// Os impulsos acumulados (normal_impulse / tangent_impulse) sao mantidos entre
// frames pelo warm starting: o manifold do frame seguinte procura o contato com
// o mesmo feature_id e herda esses valores, o que faz pilhas de objetos
// estabilizarem em poucas iteracoes em vez de ficarem tremendo.
// ------------------------------------------------------------
public class ContactPoint {

    // Identificador usado quando a colisao produz um unico ponto de contato
    // (circulo x circulo, circulo x retangulo). Retangulo x retangulo gera
    // ids derivados das faces envolvidas.
    public static final int SINGLE_CONTACT_ID = -1;

    // ------------------------------------------------------------
    // 1. Geometria do contato
    // ------------------------------------------------------------

    public final Vector2D position;

    // Profundidade da penetracao. Positiva quando os corpos estao sobrepostos.
    public double penetration;

    // Identifica o contato entre frames consecutivos (warm starting).
    public final int feature_id;

    // ------------------------------------------------------------
    // 2. Estado do solver (persiste entre frames)
    // ------------------------------------------------------------

    // Impulso acumulado na direcao da normal. Sempre >= 0: um contato
    // so pode empurrar, nunca puxar.
    public double normal_impulse = 0.0;

    // Impulso acumulado na direcao da tangente (atrito). Limitado por
    // +-(friction * normal_impulse), que e a lei de Coulomb.
    public double tangent_impulse = 0.0;

    // ------------------------------------------------------------
    // 3. Cache do passo atual (recalculado em preStep)
    // ------------------------------------------------------------

    // Vetor do centro de massa de cada corpo ate o ponto de contato.
    public Vector2D ra = new Vector2D(0, 0);
    public Vector2D rb = new Vector2D(0, 0);

    // Massa efetiva do contato nas direcoes normal e tangente,
    // ja incluindo a contribuicao rotacional dos dois corpos.
    public double normal_mass = 0.0;
    public double tangent_mass = 0.0;

    // Velocidade extra injetada para simular o quique (restituicao).
    public double restitution_bias = 0.0;

    // Velocidade extra injetada para empurrar os corpos para fora da
    // sobreposicao (estabilizacao de Baumgarte).
    public double position_bias = 0.0;

    // ------------------------------------------------------------
    // Construtores
    // ------------------------------------------------------------

    public ContactPoint(Vector2D position, double penetration, int feature_id) {
        this.position = new Vector2D(position);
        this.penetration = penetration;
        this.feature_id = feature_id;
    }

    public ContactPoint(Vector2D position, double penetration) {
        this(position, penetration, SINGLE_CONTACT_ID);
    }

    // Copia os impulsos acumulados de um contato equivalente do frame anterior.
    public void inheritImpulsesFrom(ContactPoint previous) {
        this.normal_impulse = previous.normal_impulse;
        this.tangent_impulse = previous.tangent_impulse;
    }

    @Override
    public String toString() {
        return "ContactPoint" + position + " depth=" + penetration + " id=" + feature_id;
    }
}
