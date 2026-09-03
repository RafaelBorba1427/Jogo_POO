# Sistema de colisão — o que mudou

Documento das alterações feitas no sistema de física do `Jogo_POO`.
Ele cobre: os arquivos novos, os bugs corrigidos (com o antes e o depois),
as decisões de convenção que precisaram ser tomadas, como o passo de física
funciona agora, e o que ficou de fora.

Data: 2026-09-03 (última atualização: bug do `QuadTree` abaixo)

---

## 1. Resumo em uma tela

| | Antes | Depois |
|---|---|---|
| Resposta à colisão | `bounce()` invertia `vx` e `vy` e multiplicava pelo `elastic_factor` | Solver de impulsos sequenciais com atrito, torque, restituição e warm starting |
| Pontos de contato | Código pela metade em `RectangularHitBox`, não compilava | Classe `CollisionManifold` + `ContactPoint`, 1 ou 2 pontos por colisão |
| Rect × Círculo | Só teste booleano, e o `CircularHitBox` delegava para o retângulo | Manifold próprio nas duas direções, com normal correta |
| Passo de física | `handleCollisions()` e depois `updateMovingObjs()` | `GameMap.step(dt, substeps, solverIterations)` |
| Rotação | `angular_velocity` era um `Vector2D` que nunca era usado | Escalar, integrado e alimentado pelo torque dos contatos |
| AABB | Nunca era recalculado depois da criação | Recalculado a cada `updateHitBox()` |
| QuadTree, > 8 objetos num nó | Objetos que cabiam num quadrante eram apagados da árvore no split (ver 9.1) | Split preserva os objetos redistribuídos |

Arquivos **novos**: `ContactPoint.java`, `CollisionManifold.java`.

Arquivos **modificados**: `HitBox.java`, `RectangularHitBox.java`, `CircularHitBox.java`,
`GameObject.java`, `MovableObj.java`, `RigidObj.java`, `BallObj.java`, `GameMap.java`,
e depois `QuadTree.java` (uma linha, ver seção 9.1 — bug encontrado num teste posterior
com mais objetos na cena).

Arquivos **não tocados**: `AABB.java`, `Vector2D.java`, `GameRules.java`,
`RenderTest.java`, `Game.java` e todo o resto.

---

## 2. Decisões de convenção

Três incoerências entre o desenho e a física precisavam de uma escolha antes de
qualquer código novo. Elas estão listadas aqui porque afetam o comportamento
visível, não só o interno.

### 2.1 `position` é o canto superior esquerdo

`GameObject.drawHitbox()` sempre desenhou o objeto com o centro em
`position + dimensions/2`, e o `GameMap` cria as paredes em `(0,0)` como canto.
Mas as hitboxes recebiam `position` como se fosse o **centro**:

```java
// antes — RectangularHitBox tratava o argumento como centro
hit_box = new RectangularHitBox(this.position, this.dimensions, this.rotation);
```

Resultado: toda hitbox ficava deslocada meia largura e meia altura em relação ao
retângulo vermelho na tela. Um objeto 100×20 tinha a colisão 50px à direita e
10px abaixo do que aparecia.

**Escolha:** `position` continua sendo o canto superior esquerdo. Quem se adaptou
foram as hitboxes — os construtores e o `updateHitBox()` recebem canto e dimensões
completas, e derivam o centro internamente. Existe `RectangularHitBox.fromCenter()`
e `CircularHitBox.fromCenter()` para quem já tem o centro na mão.

O centro de massa agora tem um lugar só: `GameObject.getCenterOfMass()`.

### 2.2 `dimensions.x` da bola é o diâmetro

`BallObj.drawHitbox()` chamava `fillOval(x, y, dimensions.x, dimensions.x)`, e o
terceiro argumento do `fillOval` é a **largura**. Então o desenho tratava
`dimensions.x` como diâmetro. Mas a hitbox e o momento de inércia tratavam o
mesmo número como raio:

```java
// antes
hit_box = new CircularHitBox(position, dimensions.x);      // raio = diâmetro
this.moment_inertia = (mass*dimensions.x*dimensions.x)/2.0; // I = m*d²/2
```

O círculo físico tinha o dobro do tamanho do círculo desenhado, e estava centrado
no canto superior esquerdo dele.

**Escolha:** `dimensions.x` é o diâmetro, que é o que o desenho já assumia.
`getRadius()` agora devolve `dimensions.x / 2`, o momento de inércia usa
`I = m·r²/2`, e o parâmetro do construtor foi renomeado de `radius` para `diameter`.

Isso **não** quebra as chamadas existentes: `new BallObj(200, 200, 32, ...)`
continua produzindo exatamente a bola que já era desenhada. O que mudou é que a
física passou a concordar com ela.

### 2.3 Velocidade angular é escalar

```java
// antes
protected Vector2D angular_velocity = new Vector2D(0,0);
protected Vector2D angular_acceleration = new Vector2D(0,0);
```

Em 2D a rotação tem um grau de liberdade só: o eixo é sempre perpendicular à tela.
Guardar isso em um `Vector2D` não tem significado físico e não dá para somar
torque nele. Viraram `double`.

---

## 3. Bugs corrigidos

### 3.1 `RectangularHitBox.getManifold()` não compilava

O método referenciava três coisas que não existiam:

```java
OverlapData overlap = intersects(other);          // o record se chama overlap_data
... overlap.axis ...                              // o componente se chama overlap_axis
... overlap.referenceBody.getVertices() ...       // o método se chama getCorners()
... new Manifold(...) ... new ContactPoint(...)   // classes inexistentes
```

Além disso, componentes de `record` são acessados por método (`overlap.intersects()`),
não por campo. E `Manifold`/`ContactPoint` nunca tinham sido escritas.

**Correção:** toda a geração de manifold saiu da `RectangularHitBox` e foi para a
classe `CollisionManifold`, que é o que você pediu. O `record overlap_data` foi
removido; a `RectangularHitBox` voltou a expor apenas testes booleanos, e o
`CollisionManifold` refaz o SAT na formulação de separação de faces, que é a que o
clipping precisa.

### 3.2 `intersects(RectangularHitBox)` devolvia um record onde se esperava boolean

`GameObject.collides()` fazia:

```java
return ((RectangularHitBox) this.hit_box).intersects((RectangularHitBox) outro_obj.getHitBox());
```

mas o método devolvia `overlap_data`. Erro de compilação em cadeia, que atingia
`GameMap.addObject()` e `GameMap.handleCollisions()`.

**Correção:** `intersects(RectangularHitBox)` voltou a devolver `boolean`.
Os dados completos da colisão vêm de `CollisionManifold.generate()`.

### 3.3 O construtor `(double, double, double, double, double)` não calculava `cos`/`sin`

```java
public RectangularHitBox(double x, double y, double width, double height, double rotation) {
    this.center = new Vector2D(x, y);
    this.halfWidth = width / 2.0;
    this.halfHeight = height / 2.0;
    this.rotation = rotation;
    this.aabb = new AABB(this);   // <- cos e sin ainda valem 0.0
}
```

O outro construtor chamava `updateCosSin()`, esse não. Com `cos = sin = 0`,
`getAxisX()` devolvia `(0,0)`, `getAxisY()` devolvia `(0,0)`, e o `AABB` construído
em seguida nascia com largura e altura zero. Qualquer hitbox criada por esse
construtor era invisível para a QuadTree até a primeira rotação.

**Correção:** `updateCosSin()` no construtor. Os dois construtores agora delegam
para o mesmo caminho.

### 3.4 `updateHitBox()` nunca atualizava o AABB — o bug mais caro

```java
// antes
public void updateHitBox(Vector2D center, Vector2D dimensions, double rotation) {
    if(this.center != center || ...) {   // comparação de REFERÊNCIA
        this.center = center;
        ...
        this.aabb.update(this);
    }
    if(this.rotation != rotation){
        this.rotation = rotation;
        this.updateCosSin();             // rotação mudava, AABB não
    }
}
```

Dois problemas somados:

1. A hitbox guardava **a mesma referência** do `position` do `GameObject`
   (`this.center = center`, sem cópia). O `MovableObj.update()` mutava esse vetor
   no lugar (`position.x += velocity.x`). Então na chamada seguinte
   `this.center != center` comparava o objeto com ele mesmo e dava **sempre falso**.
   O AABB ficava congelado no valor do frame em que o objeto foi criado.

2. Mesmo se a comparação funcionasse, o ramo da rotação atualizava `cos`/`sin` sem
   recalcular o AABB, que depende da rotação.

Como a QuadTree indexa tudo por AABB, o efeito prático era que a broad phase
respondia sobre posições antigas: objetos colidiam com o fantasma de onde estavam
e atravessavam onde de fato estão.

**Correção:** o centro agora é um `Vector2D` próprio da hitbox, copiado, e o
`updateHitBox()` recalcula tudo incondicionalmente. A mesma correção foi aplicada
ao `CircularHitBox`, que tinha exatamente o mesmo padrão.

### 3.5 `CircularHitBox.intersects(RectangularHitBox)` delegava para o retângulo

```java
public boolean intersects(RectangularHitBox rectangle) {
    return rectangle.intersects(this);
}
```

Isso cria dependência circular entre as classes e herda qualquer bug do lado do
retângulo. Além disso o teste do retângulo tinha um caso não tratado: quando o
centro do círculo está **dentro** do retângulo, o ponto mais próximo é o próprio
centro, a distância dá zero e o teste responde `true` — por acidente, não por
lógica. Na hora de gerar o manifold isso deixa de funcionar, porque a distância
zero não define direção de saída nenhuma.

**Correção:** o `CircularHitBox` faz o próprio teste, e os dois lados tratam o caso
"centro dentro" explicitamente. No manifold, esse caso empurra pela face mais
próxima, com penetração `raio + quanto o centro entrou`.

### 3.6 `collides()` decidia o tipo da hitbox pelo `obj_type`

```java
// GameObject
public boolean collides(GameObject outro_obj){
    if(outro_obj.getObjType() == GameObject.BALL_OBJ){
        return ((RectangularHitBox) this.hit_box).intersects((CircularHitBox) outro_obj.getHitBox());
    }
    return ((RectangularHitBox) this.hit_box).intersects((RectangularHitBox) outro_obj.getHitBox());
}
```

Dois pressupostos frágeis: que a hitbox do próprio objeto é sempre retangular, e
que só `BALL_OBJ` usa círculo. Um `BUFF_OBJ` com hitbox circular dava
`ClassCastException`. E cada tipo novo exigia mais um `if` aqui e outro no `BallObj`.

**Correção:** a interface `HitBox` ganhou `intersects(HitBox)` com despacho duplo.
Cada hitbox descobre o tipo concreto do argumento e chama a sobrecarga certa.
`GameObject.collides()` virou uma linha, e o override em `BallObj` foi deletado.

### 3.7 `findReferenceFace()` / `findIncidentEdge()` escolhiam a face por vizinhança

A busca antiga achava o vértice de projeção máxima e depois escolhia entre os dois
vizinhos comparando a diferença de projeção. Quando duas faces são paralelas —
que é justamente o caso de uma caixa apoiada — os dois vértices da face de contato
têm projeção **idêntica**, a comparação vira um empate resolvido por erro de
ponto flutuante, e a face escolhida oscila de frame para frame.

**Correção:** as faces passaram a ser indexadas. `getCorners()` tem ordem
contratual (a face `i` vai do vértice `i` ao `(i+1)%4`) e `getFaceNormal(i)`
devolve a normal externa correspondente. O SAT agora percorre as 4 faces
diretamente e guarda o índice da melhor, sem inferir nada por vizinhança.
A escolha entre os dois corpos usa a tolerância relativa do Box2D (`0.98`), que
evita o corpo de referência ficar alternando quando as separações empatam.

### 3.8 `all_objects` estático acumulava mapas antigos

```java
private static ArrayList<ArrayList<GameObject>> all_objects = new ArrayList<>();
// no construtor:
all_objects.add(permanent_objects);   // sem limpar
```

As listas internas são de instância, mas a lista que as contém é estática. Cada
`new GameMap(...)` empilhava mais três listas nela. O segundo mapa criado na mesma
execução simulava e desenhava também os objetos do primeiro.

**Correção:** `all_objects.clear()` no início do construtor. Mantive o campo
estático porque `RenderTest.paintComponent()` chama `GameMap.getAllObjects()`
estaticamente — mudar isso exigiria mexer no `RenderTest`.

### 3.9 `addObject()` consultava uma QuadTree desatualizada

A árvore só era preenchida dentro de `handleCollisions()`. Objetos adicionados
antes do primeiro passo de física eram testados contra uma árvore vazia e entravam
em cima de qualquer coisa.

**Correção:** `addObject()` reconstrói a broad phase antes de consultar.

### 3.10 Outros ajustes menores

- `move()` e `changeDimensions()` não chamavam `updateHitBox()`; agora chamam
  (`changeDimensions` também recalcula a inércia).
- `setRotation()` e `rotate()` da hitbox atualizavam `cos`/`sin` sem atualizar o AABB.
- `MovableObj.update()` tinha um `else if` a menos na proteção de `velocity.x`:
  velocidade abaixo de `-MAX_VELOCITY` não era limitada.
- `BallObj.update()` era uma cópia do `update()` do pai sem os limites de
  velocidade, o que deixava bolas acelerarem sem teto. O override foi removido.
- `AnimationMaster`, `Signal` e afins não foram tocados.

---

## 4. As classes novas

### `ContactPoint`

Um ponto de contato. Guarda três coisas:

1. **Geometria** — `position` (mundo) e `penetration` (positiva quando sobrepostos).
2. **Estado do solver** — `normal_impulse` e `tangent_impulse`, os impulsos
   acumulados. Persistem entre frames: é isso que o warm starting reaproveita.
3. **Cache do passo** — `ra`/`rb` (braços de alavanca), `normal_mass`/`tangent_mass`
   (massas efetivas), `restitution_bias` e `position_bias`. Calculados uma vez por
   passo no `preStep()` e reusados por todas as iterações.

O `feature_id` é o que permite parear um contato deste frame com o equivalente do
frame anterior. Para retângulo × retângulo ele codifica (face de referência, face
incidente, qual das duas pontas, se os corpos foram invertidos). Se o objeto girou
e o contato mudou de face, o id muda e o contato corretamente começa do zero em vez
de herdar um impulso que não faz mais sentido.

### `CollisionManifold`

Descreve **e resolve** uma colisão entre dois `GameObject`.

Dados: os dois corpos, a normal unitária (**sempre de A para B**), a lista de
contatos, e a restituição/atrito do par.

- Restituição do par: `max(eA, eB)`. Assim uma bola elástica quica na parede
  mesmo que a parede não devolva energia por conta própria.
- Atrito do par: `sqrt(fA · fB)`, a convenção usual.

Ciclo de uso (é o que o `GameMap` faz):

```
generate(a, b)              -> detecta e constrói (null se não colide)
inheritImpulses(anterior)   -> warm starting
preStep(1/dt)               -> massas efetivas, bias de posição e restituição
warmStart()                 -> reaplica os impulsos herdados
solveVelocityConstraints()  -> N vezes
```

---

## 5. Como cada tipo de colisão vira um manifold

### Retângulo × Retângulo — SAT + clipping

1. **SAT por separação de faces.** Para cada uma das 4 faces de A, projeta B na
   normal daquela face e mede a separação até o vértice mais fundo de B. Guarda a
   maior separação e o índice da face. Repete trocando os papéis. Se alguma
   separação for positiva, existe eixo separador e não há colisão.

2. **Face de referência.** O corpo com a maior separação vira a referência
   (`separationB > 0.98 · separationA + 0.001` para trocar; o fator evita alternância
   quando empatam). A normal dessa face é a normal da colisão.

3. **Face incidente.** No outro corpo, a face cuja normal é mais anti-paralela à
   normal de referência.

4. **Clipping.** A aresta incidente é recortada pelos dois planos laterais da face
   de referência (Sutherland–Hodgman). O que sobra e está atrás do plano da face
   vira ponto de contato, com `profundidade = -separação`.

É o passo 4 que produz **dois** pontos quando duas faces se encostam. Sem ele uma
caixa apoiada teria um contato só e tombaria, porque não haveria torque para
equilibrá-la.

### Retângulo × Círculo

O círculo é levado para o sistema local do retângulo (eixos X e Y do OBB), onde o
problema vira AABB × círculo. Dois casos:

- **Centro fora:** o ponto mais próximo é o centro clampado nos meios-lados; a
  normal é a direção desse ponto até o centro do círculo, e
  `penetração = raio − distância`.
- **Centro dentro:** não existe direção bem definida pelo ponto mais próximo
  (a distância é zero). Empurra pela face mais próxima, com
  `penetração = raio + quanto o centro entrou`.

Sempre 1 ponto de contato. A normal é calculada do retângulo para o círculo e
invertida quando o círculo é o corpo A, para manter a convenção "de A para B".

### Círculo × Círculo

Direto: normal na linha entre centros, `penetração = soma dos raios − distância`,
contato no meio da região sobreposta. Centros coincidentes usam `(0,-1)` como
direção fixa, para o resultado ser determinístico.

---

## 6. O solver

### Impulsos sequenciais

Em vez de montar e resolver o sistema linear de todos os contatos de uma vez, cada
contato é corrigido isoladamente, várias vezes seguidas. Com impulso acumulado e
clamp, isso converge para a solução correta. É o método do Box2D.

Para cada contato, por iteração:

**Normal.** Mede a velocidade relativa no ponto de contato (`v + ω × r` dos dois
corpos), projeta na normal, e calcula

```
λ = normal_mass · (−v_normal + position_bias + restitution_bias)
```

O clamp é feito no impulso **acumulado**, não no incremento:

```java
double anterior = contact.normal_impulse;
contact.normal_impulse = Math.max(anterior + λ, 0.0);
λ = contact.normal_impulse - anterior;
```

É esse detalhe que deixa as iterações se corrigirem umas às outras sem que o
contato chegue a "puxar" os corpos. Clampar o incremento em vez do acumulado é o
erro clássico que faz pilhas afundarem.

**Atrito.** Mesma coisa na tangente, com o limite de Coulomb
`|impulso tangencial| ≤ μ · impulso normal`.

**Torque.** `MovableObj.applyImpulse()` aplica `Δv = P/m` no centro de massa e
`Δω = (r × P)/I`. Um impulso que passa pelo centro de massa não gira nada; um na
quina gira o máximo. É daí que sai a rotação real nas colisões.

### Warm starting

Os impulsos acumulados de um frame são reaplicados no começo do frame seguinte,
antes das iterações. O solver começa perto da resposta em vez de recomeçar do
zero, e uma pilha que precisaria de dezenas de iterações converge em poucas.

O `GameMap` guarda os manifolds do passo anterior em um `HashMap` indexado pelo
par de `uid`s. Os contatos são pareados por `feature_id`.

Para isso funcionar a **ordem dos corpos precisa ser estável entre frames** — se
A e B trocassem de papel, a normal inverteria e os impulsos herdados viriam com o
sinal errado. Por isso foi adicionado um `uid` único por instância no `GameObject`
e o par é sempre ordenado por ele. O `obj_id` não serve: ele identifica o *tipo*
do objeto, não a instância.

### Estabilização de Baumgarte

Corpos sobrepostos ganham uma velocidade extra proporcional ao excesso de
penetração:

```
position_bias = min(BAUMGARTE · (1/dt) · max(0, penetração − SLOP),
                    MAX_LINEAR_CORRECTION · (1/dt))
```

O `SLOP` é a penetração tolerada sem correção. Sem ele os objetos em repouso
oscilariam eternamente em torno do contato exato. O `MAX_LINEAR_CORRECTION`
impede que uma sobreposição grande (dois objetos criados um dentro do outro,
por exemplo) empurre com força explosiva.

### Restituição

Medida **antes** de qualquer impulso, a partir da velocidade de aproximação. Abaixo
de `RESTITUTION_THRESHOLD` a colisão é tratada como inelástica — sem isso um objeto
apoiado quicaria para sempre com amplitude cada vez menor, mas nunca zero.

---

## 7. O passo de física no `GameMap`

```java
public void step(double dt)
public void step(double dt, int substeps, int solver_iterations)
```

Ordem, e o porquê de cada etapa:

| # | Etapa | O que faz |
|---|---|---|
| 1 | `integrateForces` | gravidade e outras forças viram velocidade |
| 2 | broad phase | a QuadTree é reconstruída e devolve os pares candidatos |
| 3 | narrow phase | gera os manifolds reais e herda os impulsos do frame anterior |
| 4 | `preStep` | massas efetivas, bias de posição e restituição |
| 5 | `warmStart` | reaplica os impulsos herdados |
| 6 | solver × N | corrige as velocidades até os contatos fecharem |
| 7 | `integrateVelocity` | só agora a velocidade vira posição e rotação |

O ponto importante é a etapa 7 vir por último. A colisão é resolvida **antes** do
movimento acontecer, então os corpos em geral nem chegam a se atravessar. No
código antigo a posição era atualizada junto com a velocidade, e a colisão só era
vista depois que a interpenetração já tinha ocorrido.

Foi por isso que `MovableObj.update()` precisou ser partido em `integrateForces(dt)`
e `integrateVelocity(dt)`: o solver roda entre os dois. O `update()` original
continua existindo, fazendo as duas metades em sequência, para uso avulso.

### `dt`

`dt = 1.0` reproduz exatamente o comportamento antigo, em que cada tick somava a
aceleração inteira na velocidade. `GameRules.gravity`, `MIN_VELOCITY`,
`TERMINAL_VELOCITY` e `MAX_VELOCITY` continuam valendo com os mesmos números —
nada precisa ser retunado.

### Compatibilidade com o loop atual

O `RenderTest` chama:

```java
game_map.handleCollisions();
game_map.updateMovingObjs();
```

Como agora colisão e integração são a mesma etapa, `updateMovingObjs()` faz o passo
inteiro (`step(1.0)`) e `handleCollisions()` virou um no-op. Os dois estão marcados
`@Deprecated`. **O código atual funciona sem alteração**, mas o certo é trocar as
duas linhas por:

```java
game_map.step(1.0);
```

Não mexi no `RenderTest.java` porque ele não estava na lista de arquivos liberados.

---

## 8. Parâmetros de ajuste

Todos em `CollisionManifold`, exceto os dois últimos em `GameMap`:

| Constante | Valor | O que faz | Efeito de aumentar |
|---|---|---|---|
| `BAUMGARTE` | `0.2` | fração da penetração corrigida por passo | separa mais rápido, mas injeta energia e causa tremor |
| `PENETRATION_SLOP` | `0.5` px | penetração tolerada sem correção | menos tremor em repouso, mais afundamento visível |
| `RESTITUTION_THRESHOLD` | `0.5` px/tick | abaixo disso a colisão é inelástica | corpos param de quicar mais cedo |
| `MAX_LINEAR_CORRECTION` | `4.0` px | teto do empurrão de separação por passo | resolve sobreposições grandes mais rápido, com risco de "explodir" |
| `DEFAULT_SUBSTEPS` | `4` | subdivisões do tick | menos túnel, custo linear |
| `DEFAULT_SOLVER_ITERATIONS` | `8` | passadas do solver | pilhas mais firmes, custo linear |

Atrito por objeto: `GameObject.changeFriction(double)`, padrão `0.3`.
Restituição: `MovableObj.changeElasticFactor(double)`, como antes.

### Sobre os substeps

A regra é `MAX_VELOCITY / substeps` precisa ser menor que a espessura do obstáculo
mais fino **e** que o raio do menor corpo. Com `MAX_VELOCITY = 50` e as paredes de
20px deste mapa:

| substeps | deslocamento máximo por substep | penetração máxima medida | atravessou? |
|---|---|---|---|
| 1 | 50 px | — | **sim**, saiu do mapa |
| 2 | 25 px | 5.0 px | não |
| 4 | 12.5 px | 5.0 px | não |
| 8 | 6.25 px | 1.0 px | não |

Por isso o padrão é 4. Suba para 8 se a cena tiver corpos pequenos e rápidos.

---

## 9. Testes feitos

Os testes foram escritos e rodados fora do repositório (não deixei arquivo de teste
no projeto). Todos passaram com o código final.

**Geometria dos manifolds**

- Retângulo × retângulo face a face → 2 pontos de contato, normal `(0, 1)`,
  profundidade exata.
- Retângulo × círculo → 1 ponto, normal correta, profundidade exata; a ordem
  invertida (`generate(bola, retângulo)`) devolve a normal invertida.
- Círculo com o centro dentro do retângulo → detectado nos dois sentidos e gera
  manifold (o caso que a versão antiga não sabia resolver).
- `intersects` é simétrico entre `RectangularHitBox` e `CircularHitBox`.
- Círculo × círculo → profundidade e normal exatas.

**Simulação**

- Bola cai 900 ticks e para em cima do chão: base em `585.06` contra chão em
  `585.00`, velocidade final `0`.
- Caixa cai, para no chão, não gira sozinha e não desliza de lado.
- Pilha de 3 caixas: assentam em `545.4 / 505.5 / 465.5` (espaçamento de 40px,
  que é a altura delas), velocidade máxima final `0` — sem tremor.
- Caixa girada 45° cai, assenta na quina e para (`ω = 0`).
- Cena com 6 corpos misturados por 3000 passos: sem `NaN`, soma das velocidades
  cai para `~0`. Sem ganho de energia.
- Bola a `MAX_VELOCITY` por 3000 passos nunca sai dos limites do mapa.
- Impulso normal acumulado de uma caixa parada = `0.025`, que é exatamente
  `massa × gravidade × sub_dt` (2 × 0.05 × 0.25). O warm starting está de fato
  sustentando o peso entre frames.

**AABB**

- Girar 90° um retângulo 100×20 muda a largura do AABB de 100 para 20.
- Mover o objeto move o AABB junto.
- O centro da hitbox coincide com o centro do objeto desenhado.

**Compilação:** `javac` limpo em todo o subconjunto de física mais o `RenderTest`.
O único aviso é o de API deprecada, do `RenderTest` chamando `handleCollisions()`.

> Nota: `MainMenu.java` não compila, mas é problema anterior e não relacionado —
> ele referencia uma classe `game` em minúsculo que não existe (a classe é `Game`).

---

## 9.1 Adendo — bug no `QuadTree` encontrado depois (objetos somem da broad phase)

Depois da entrega inicial, ao testar uma cena com mais objetos (paredes do mapa
+ vários `MovableObj`), objetos móveis pararam de colidir entre si. O `QuadTree`
não estava na lista de bugs revisados na primeira passada — ele é uma das classes
liberadas para acesso, mas eu só tinha lido a lista de métodos, não o corpo. Esse
bug é dele, não da geração de manifold.

**O sintoma:** com poucos objetos (por exemplo as 4 paredes do mapa + 2 objetos
móveis, 6 no total) tudo funcionava. Ao passar de 8 objetos no mesmo nó da árvore,
objetos móveis passavam a atravessar uns aos outros como se não tivessem hitbox.

**A causa**, em `QuadTree.insert()`, no trecho que redistribui os objetos depois
de um `split()`:

```java
split();

boolean movedSomething = false;
int i = 0;

while (i < objects.size()) {
    Entry<T> entry = objects.get(i);
    int childIndex = getContainingChild(entry.bounds);

    if (childIndex != -1) {
        children[childIndex].insert(entry.object, entry.bounds);
        objects.remove(i);
        // BUG: nunca marcava movedSomething = true
    } else {
        i++;
    }
}

// if Splitting provided no benefit, removes all children
if (movedSomething == false) {
    children = null;
}
```

`movedSomething` é declarada, nunca é setada como `true` em lugar nenhum do laço,
e a condição no fim sempre dispara — mesmo quando objetos acabaram de ser movidos
com sucesso para dentro de `children[i]`. O objeto já tinha sido removido da lista
`objects` do nó pai (`objects.remove(i)`) e já estava inserido dentro do filho, mas
a própria referência `children` era descartada logo em seguida. O filho e tudo que
tinha sido movido para ele ficavam inalcançáveis a partir da raiz da árvore —
efetivamente apagados.

Isso é um limiar, não algo proporcional à quantidade de objetos novos: com o total
de objetos em um nó ≤ `MAX_OBJECTS` (8, no `GameMap`), o `split()` nunca é chamado
e o bug nunca aparece. Assim que esse total passa de 8, qualquer objeto pequeno o
bastante para caber inteiro dentro de um quadrante é apagado da árvore no split
seguinte — **não só os objetos adicionados por último**, mas potencialmente
qualquer objeto móvel da cena, dependendo da ordem de inserção. Como
`GameMap.step()` reconstrói a árvore do zero a cada substep (`buildBroadPhase()`),
isso se repete a cada passo de física.

Confirmado isolando só o `QuadTree`: inserindo 4 "paredes" grandes (que não cabem
em nenhum quadrante) e 5 objetos pequenos (que cabem), a consulta pelo mundo
inteiro devolvia só as 4 paredes — os 5 objetos móveis simplesmente não estavam
mais na árvore.

**Correção**, uma linha:

```java
if (childIndex != -1) {
    children[childIndex].insert(entry.object, entry.bounds);
    objects.remove(i);
    movedSomething = true;   // <- adicionado
} else {
```

Com isso, `children` só é descartado quando o split de fato não moveu nada (todos
os objetos continuam sobrepondo mais de um quadrante) — que era a intenção
original do comentário "if splitting provided no benefit, removes all children".

Revalidado depois da correção: a mesma consulta agora devolve os 9 objetos, e uma
cena com 4 paredes + 5 `MovableObj` (reproduzindo as instâncias adicionadas no
`Game.java`) mantém todos os 5 na broad phase, gera manifold quando dois deles são
forçados a se sobrepor, e resolve a sobreposição corretamente. A suíte completa de
testes da seção 9 foi re-executada depois do patch e continua passando.

## 10. O que ficou de fora

- **CCD (colisão contínua).** Um corpo muito rápido ainda pode penetrar alguns
  pixels antes de ser empurrado de volta. O controle disso é o número de substeps.
- **Sleeping.** Corpos parados continuam sendo processados todo frame. Com muitos
  objetos, marcar os que estão em repouso e pulá-los é o próximo ganho grande.
- **Joints/constraints.** Só contato por enquanto.
- **Atrito de rolamento.** Uma bola em repouso no chão plano não perde rotação. Não
  aparece hoje porque nada desenha a rotação da bola, mas vai aparecer quando o
  sprite girar.
- **`bounceX()` / `bounceY()`.** Continuam lá como utilitários, mas o solver não
  os usa. `bounce(GameObject)`, que era a implementação temporária, foi removido.
- **Buffs.** `BuffObj` e `EventTriggerObj` não foram tocados. Eles herdam o novo
  `collides()` e funcionam, mas se um deles precisar de hitbox circular, basta
  sobrescrever `createHitBox()`/`updateHitBox()` como o `BallObj` faz — o despacho
  duplo cuida do resto.

---

## 11. Referência rápida da API nova

```java
// --- passo de física ---
game_map.step(1.0);                    // padrões: 4 substeps, 8 iterações
game_map.step(1.0, 8, 12);             // cena rápida / pilha alta

// --- manifold avulso ---
CollisionManifold m = CollisionManifold.generate(objA, objB);
if (m != null) {
    m.getNormal();            // unitária, de A para B
    m.getContacts();          // List<ContactPoint>
    m.getMaxPenetration();
}

// --- depuração: desenhar os contatos do último passo ---
for (CollisionManifold m : game_map.getActiveManifolds())
    for (ContactPoint c : m.getContacts())
        g2d.fillOval((int)c.position.x - 2, (int)c.position.y - 2, 4, 4);

// --- hitboxes ---
hitbox.intersects(outraHitbox);        // despacho duplo, qualquer combinação
hitbox.getCenter();
hitbox.contains(ponto);
rect.getCorners();                     // face i = vértices i e (i+1)%4
rect.getFaceNormal(i);                 // normal externa da face i
rect.closestPointTo(ponto);

// --- objetos ---
obj.getCenterOfMass();
obj.getUid();
obj.changeFriction(0.5);
obj.applyImpulse(impulso, bracoDeAlavanca);   // no-op em RigidObj
movable.updateAngularVelocity(0.02);
ball.getRadius();                      // = dimensions.x / 2
ball.getDiameter();                    // = dimensions.x
```
