# Reintegração dos sistemas do `oldGame` no jogo novo

Documento das mudanças feitas para trazer `healthSystem`, `pointSystem` e `buffSystem` da pasta
`oldGame` para o jogo atual, ligar as duas partes gráficas que estavam soltas, e fechar o ciclo
Menu → Game → Game Over → Menu.

Data: 10/09/2026
Compilado com `javac` (OpenJDK 21), sem erros nem avisos.

- **Parte 1 — seções 1 a 9:** a reintegração dos três sistemas e o ciclo de telas.
- **Parte 2 — seções 10 a 17:** ícones animados do menu, volume nas opções, dois bugs corrigidos,
  lançamento único por estilingue e teclas de debug.
- **Parte 3 — seções 18 a 21:** ícones do menu trocados para o `Menu_Icons.png`, escolha do sprite
  da bola nas opções e mais um bug corrigido no `AnimationPlayer`.

---

## 0. Resumo em uma tela

| Arquivo | Situação | O que é |
|---|---|---|
| `PointSystem.java` | **novo** | Refatoração de `oldGame/pointSystem.java`; absorve o `PointCounter` |
| `HealthSystem.java` | **novo** | Refatoração de `oldGame/healthSystem.java` (+ a classe `heart`) |
| `BuffSystem.java` | **novo** | Refatoração de `oldGame/buffSystem.java` + os efeitos que viviam no `oldGame/ball.java` |
| `GameOverScreen.java` | **novo** | Refatoração de `oldGame/end.java` |
| `GameFlow.java` | **novo** | Máquina de estados de telas (o ciclo do jogo) |
| `Game.java` | modificado | HUD, tick dos sistemas, perda de vida, game over, ciclo de vida do laço |
| `GameMap.java` | modificado | Buff no `step()`, filtro do INTANGIBLE, pontuação por objeto, guarda de reentrância |
| `LevelRules.java` | modificado | Vida de verdade, meta de pontos por nível, game over pela tela nova |
| `Main.java` | modificado | Sobe no menu, inicializa o `MusicPlayer` |
| `MainMenu.java` | modificado | Assets corrigidos, botões funcionais, ligado ao `GameFlow` |
| `PointCounter.java` | **aposentado** | Casca `@Deprecated` que repassa tudo ao `PointSystem` — pode apagar |
| `End_of_game.java` | **aposentado** | Casca `@Deprecated` que abre a `GameOverScreen` — pode apagar |
| `AnimationPlayer.java` | modificado | Guarda de sprite nulo; correção do último quadro nunca desenhado |
| `GameRules.java` | modificado (parte 3) | Sprite da bola escolhido pelo jogador |
| `SoundEffectPlayer.java` | modificado | Guarda de arquivo ausente; volume seguro + `getVolume` |
| `MusicPlayer.java` | modificado | Guarda de clip/arquivo ausente; `applyGain`, `clampVolume`, `getVolume` |
| `Slingshot.java` | **novo** (parte 2) | Estilingue animado: mira e disparo |

**Nada foi alterado no `QuadTree.java` nem no `CollisionManifold.java`.** A seção 6 explica por que não
foi preciso, e o que foi feito no lugar.

---

## 1. `PointSystem` — a lógica antiga com a parte gráfica de volta

Arquivo: `PointSystem.java` (substitui `PointCounter.java`).

### O que existia

Havia **dois** sistemas de pontos, e nenhum completo:

- `oldGame/pointSystem.java` tinha o modelo bom (pontos potenciais x confirmados, tabela
  id → pontos, animação de bônus), mas dependia de `coisa` e de `game`, e estendia `JFrame`
  sem nunca abrir janela nenhuma.
- `PointCounter.java`, no jogo novo, era só `points += 1` com um `Signal` de meta. O HUD
  — o painel com "Total Points" e "Points" — tinha ficado para trás dentro do
  `game.paintComponent()` antigo e nunca foi trazido.

### O que existe agora

`PointSystem` é estático (como o `PointCounter`, porque quem pontua é o laço de colisão do
`GameMap`, que não tem referência para a instância do jogo) e reúne os dois:

```
potential_points  pontos da tentativa / nível em andamento
points            pontos já confirmados
target_points     meta do nível atual
```

- `addPotentialPoints(GameObject)` — pontua pela tabela.
- `processPoints()` — confirma os potenciais. Chamado ao vencer o nível.
- `removePotentialPoints()` — descarta. Chamado ao perder.
- `removeALLPoints()` — zera tudo. Chamado ao começar partida nova.
- `hasReachedTarget()` / `reached_target_points` — a meta e o `Signal`, herdados do `PointCounter`.
- `drawHud(g2d, largura, altura)` — **o HUD que voltou**, chamado por `Game.paintComponent`.

### Tabela de pontos

Os valores são os mesmos do `oldGame`, com os IDs traduzidos de `coisa.*` para `GameObject.*`:

| Objeto | Pontos |
|---|---|
| Balde (`ID_BUCKET`) | 1000 |
| Buff Iced | 300 |
| Plataforma congelada | 200 |
| Mesa | 150 |
| Buff Time Travel | 150 |
| Plataforma | 100 |
| Parede / Speed Boost / Elastic Collision | 50 |
| Buff Lag | −100 |
| Buff Intangible | −150 |
| **Chão, paredes permanentes, objeto invisível** | **0** |
| Qualquer outro | 10 (`DEFAULT_OBJECT_POINTS`) |

As duas linhas de baixo são decisões novas, e valem uma explicação:

- **Zero para o cenário fixo.** O `GameMap` dava `PointCounter.addPoints(1)` para qualquer
  quique. Se o chão pagasse pontos pela tabela, bastaria deixar a bola quicando no chão para
  bater qualquer meta.
- **Padrão de 10.** O `pointSystem` antigo fazia `id_to_points.get(obj.id)` sem checar nulo: um
  objeto fora da tabela dava `NullPointerException` no meio do laço. Agora há um valor padrão.

### Onde o HUD é desenhado

Em `Game.paintComponent`, **depois** de `obj_g2d.dispose()` — ou seja, fora da escala e da
translação da câmera. O HUD vive em pixels de tela e fica parado enquanto o mundo se move. As
medidas escalam com o tamanho do painel contra `Main.DEFAULT_RESOLUTION`, que é o papel que
`game.rescaleX` / `rescaleY` faziam no jogo antigo.

> ✅ **Colisão de IDs em `GameObject` — resolvida na parte 3.**
> Quando esta seção foi escrita, `ID_BALL_3 = 16` valia o mesmo que `ID_PERMANENT_FLOOR = 16`, e
> `ID_BALL_4 = 17` o mesmo que `ID_PERMANENT_WALL = 17`. O chão e a parede foram para **98 e 99**,
> que não indexam linha nenhuma da folha de sprites (eles têm PNGs próprios), então as quatro
> bolas passaram a ser usáveis. Ver a seção 20.

---

## 2. `HealthSystem` — a parte gráfica ligada à renderização do jogo

Arquivo: `HealthSystem.java`.

### O que existia

`oldGame/healthSystem.java` era um `JPanel` adicionado por cima do painel do jogo
(`gaming.add(healthSys)`), com um `java.util.Timer` próprio repintando a 10 FPS. No jogo novo
isso não funcionaria por dois motivos:

1. `Game` não define layout manager. Um filho cairia no `FlowLayout` padrão e os corações
   apareceriam centralizados no topo, do tamanho que o layout decidisse.
2. Um segundo timer repintando por fora do laço principal briga com o `repaint()` do `Game`.

E, além disso, o `Game` novo nunca chegou a adicionar o painel — a vida simplesmente não
aparecia.

### O que existe agora

`HealthSystem` é um objeto comum, uma instância por partida, com:

- `tick()` — chamado uma vez por tick pelo laço do `Game`. Um relógio só para tudo.
- `draw(g2d, largura, altura)` — chamado por `Game.paintComponent`, em espaço de tela,
  ao lado do HUD de pontos.
- `takeDamageAndCheckDeath()`, `heal()`, `AddMaxHearts()`, `resetToFull()` — mesma API de antes.

Outras mudanças:

- Os sprites passam pelo `SpriteLoader` do jogo novo. Antes, cada coração fazia o próprio
  `ImageIO.read("spritesheet/heart.png")` no construtor: 5 corações = 5 leituras do mesmo
  arquivo. O `SpriteLoader` tem cache por chave.
- A animação **não** usa `AnimationPlayer`. O `AnimationPlayer` roda em loop e só para frente;
  a animação do coração é um estado que vai de cheio (quadro 0) a vazio (quadro 4) ao levar
  dano e volta ao curar. O avanço de quadro continua manual, mas com contador de ticks próprio
  (`TICKS_PER_FRAME = 4`) em vez de depender do FPS de repintura.
- Som e música passam por `SoundEffectPlayer` / `MusicPlayer` no lugar de `game.soundMaster` /
  `game.musicMaster`. A troca de trilha quando a vida cai pela metade foi mantida.
- Se `spritesheet/heart.png` não carregar, os corações caem para quadrados desenhados em
  código, em vez de derrubar o `paintComponent`.

`heart.png` é 80×16 = 5 quadros de 16×16, confirmado no arquivo.

---

## 3. `BuffSystem` — reintegrado com o `BuffObj`

Arquivo: `BuffSystem.java`.

### O que existia

`oldGame/buffSystem.java` era **só um cronômetro**: guardava quantos ticks faltavam de cada buff
e nada mais. Quem aplicava o efeito era o `ball.update()`, cheio de
`if (game.buffSys.HasBuff(...))` misturado com a física caseira da bola (`x_vel`, `y_vel`,
`bounce_factor`). Como o jogo novo tem um solver de impulsos de verdade, esses `if` não tinham
mais onde morar.

### O que existe agora

O `BuffSystem` é dono dos timers **e** dos efeitos, e conversa com a bola pela API pública do
`MovableObj`. Os oito buffs continuam existindo, com as mesmas durações padrão.

`GameMap.step()` chama `BuffSystem.update(Game.pingbongBall)` **antes** da integração da física.
A ordem importa: o sistema mexe em velocidade, atrito, restituição e gravidade; se rodasse depois
do solver, as alterações só valeriam no tick seguinte e o efeito ficaria um quadro atrasado — mais
visível no LAG, que congela a bola.

### Tradução dos efeitos

| Buff | Como era (`ball.java`) | Como é agora |
|---|---|---|
| `ELASTIC_COLLISION` | "não multiplique a velocidade por `bounce_factor` ao quicar" | `ball.changeElasticFactor(1.0)`; o solver já usa o maior valor do par |
| `SLIPPERY` | não aplicar o multiplicador de atrito quando no chão | `ball.changeFriction(0.0)`; o solver faz atrito de Coulomb no contato |
| `MASSIVE_DRAG` | `x_vel *= friction * 0.96` no chão | amortecimento de 0,96 na velocidade por tick |
| `SPEED_BOOST` | empurrão de ±12/±15 + `*1.0001` por tick | igual, via `changeVelocity` |
| `ICED` | freia a bola e vira `SLIPPERY` pelo tempo restante | igual, via `changeVelocity` |
| `TIME_TRAVEL` | grava `x`/`y` no início e restaura no fim | grava `getPosition()` e faz `move()` de volta, zerando velocidade |
| `LAG` | alterna a cada 32 ticks; congela posição de desenho e física | alterna a cada 32 ticks; guarda a velocidade, congela a bola sem gravidade e devolve tudo ao sair |
| `INTANGIBLE` | `ball.bounce()` retornava cedo | o `GameMap` não gera o manifold do par (ver seção 6) |

**Valores base preservados.** Atrito e restituição originais da bola são capturados na primeira
aplicação e devolvidos quando o buff acaba. Sem isso, dois buffs seguidos deixariam a bola
permanentemente escorregadia ou saltitante — que é o que aconteceria numa tradução literal.

**Duração.** O sistema antigo contava `90 ticks ≈ 1 segundo`. O laço novo roda a 16 ms, ou seja
~62 ticks por segundo. `TICKS_PER_SECOND` virou 60, então as durações em segundos passaram a
valer de verdade.

### Detecção de colisão com o `BuffObj`

Como pedido, no `step()` do `GameMap`:

```java
public void step(double dt) { ... }        // guarda de reentrância
private void stepInternal(double dt) {
    BuffSystem.update(Game.pingbongBall);  // timers + efeitos
    step(dt, DEFAULT_SUBSTEPS, DEFAULT_SOLVER_ITERATIONS);
    checkPlayerBuffCollisions();           // <-- aqui
    ...
}
```

`checkPlayerBuffCollisions()`:

1. Acha o corpo com a tag `GameObject.PLAYER` em `moving_objects`. (A tag é colocada por
   `setPlayer()`, que troca o `obj_type` **depois** do objeto já ter entrado em `moving_objects`
   como `BALL_OBJ` — por isso é lá que ele continua estando.)
2. Reconstrói a QuadTree. O último `buildBroadPhase()` do passo aconteceu **antes** do
   `integrateVelocity` final, então as caixas guardadas lá estão um substep atrasadas.
3. Consulta a QuadTree pelo AABB do jogador e, para cada candidato com
   `getObjType() == GameObject.BUFF_OBJ` que realmente colide, chama `BuffSystem.pickUpBuff()`.

`pickUpBuff()` aplica o buff, pontua pela tabela, toca som de buff ou debuff conforme o valor
ser positivo ou negativo, e desativa o objeto — o `deleteInactiveObjs()` do passo seguinte
remove ele da lista.

**Por que não junto com os manifolds:** `BuffObj` tem massa inversa infinita, então a massa
efetiva do contato dá zero e o solver não aplica impulso nenhum. Ele já é um gatilho atravessável
do ponto de vista da física, e a colisão com ele não precisa (nem deve) passar pelo solver. Uma
consulta direta à QuadTree é mais barata e não suja o cache de warm starting com manifolds que
não resolvem nada.

---

## 4. Perda de vida e game over

Regra combinada (as duas condições valem):

**a) A bola parou sem bater a meta** — `Game.checkBallAtRest()`

Equivalente ao bloco `trickshot_in_progress && ball parou` do `game.update()` antigo. Lá a
velocidade era zerada à mão por um limiar (`min_X_speed` / `min_Y_speed`); aqui ela é resultado
do solver, então a parada é medida por um limiar sustentado:

- `REST_SPEED_THRESHOLD = 0.05` — velocidade abaixo da qual a bola conta como parada;
- `REST_TICKS_REQUIRED = 45` — ~0,7 s parada seguidos, para não punir a bola no topo de uma
  parábola, onde a velocidade passa perto de zero por um instante;
- enquanto o `LAG` estiver congelando a bola, o contador é zerado: ela está parada de mentira.

O arremesso começa a contar no clique (`mouseClicked`, modo `GAMELOOP`). Se ao parar a meta já
tiver sido batida, não custa nada. Se não, perde um coração e a bola volta ao ponto de partida.
**Os pontos potenciais do nível não são descartados**, senão a meta seria inalcançável para quem
precisa de mais de um arremesso.

**b) O nível terminou sem bater a meta** — `LevelRules.nextLevel()`

Ao encostar no balde: os 1000 pontos do balde entram (uma vez por nível — se pontuassem na
detecção de colisão, entrariam uma vez por substep enquanto a bola estivesse sobreposta ao
gatilho), a meta é checada, e se não foi batida perde um coração. Depois `processPoints()`
confirma os pontos e a meta do próximo nível sobe.

Meta por nível: `300 + 150 × nível` (`BASE_TARGET_POINTS`, `TARGET_POINTS_PER_LEVEL`).

**Quando o HP zera** → `Game.triggerGameOver()` → para o laço → `GameFlow.showGameOver()` →
`GameOverScreen`.

### O que o `LevelRules` tinha antes

`static int health = 1`, um contador solto que **ninguém nunca decrementava**. A única leitura
era `if (health == 0)` dentro do `nextLevel`, e como começava em 1 e nada escrevia nele, esse
`if` nunca era verdadeiro — a tela de fim de jogo era código inalcançável. Agora a vida mora no
`HealthSystem` da instância de `Game`, e `static int points` saiu (a pontuação é do
`PointSystem`).

---

## 5. Ciclo Menu → Game → Game Over → Menu

Arquivos: `GameFlow.java` (novo), `Main.java`, `MainMenu.java`, `GameOverScreen.java`.

### O que existia

Cada tela sabia construir a próxima, e ninguém limpava nada:

- `MainMenu.mouseClicked` dava `new Game(...)` e `frame.add(...)` direto;
- `LevelRules` dava `new End_of_game(...)` no meio da lógica de nível;
- `Main` nem passava pelo menu — subia direto na partida, com o `MainMenu` comentado;
- ninguém removia o painel anterior do `JFrame` nem parava o `Timer` da partida anterior.

Resultado: o caminho de volta ao menu não existia, e reentrar no jogo empilharia painéis e laços
de 16 ms rodando ao mesmo tempo.

### O que existe agora

`GameFlow` concentra as três transições. Cada uma:

1. para o laço da partida que está saindo (`Game.stopGame()`);
2. remove o painel antigo do frame antes de adicionar o novo;
3. zera o estado **global** — `PointSystem`, `BuffSystem`, `LevelRules` e `GameRules` são
   estáticos, então o estado da partida anterior sobrevive à troca de tela se ninguém limpar;
4. devolve o foco de teclado ao painel que entrou.

```
Main.boot()
   └─ GameFlow.showMenu() ──── botão JOGAR ───→ GameFlow.startNewGame()
             ↑                                          │
             │                                    HP chega a zero
             │                                          ↓
             └───── botão "Voltar ao Menu" ──── GameFlow.showGameOver()
                                                (GameOverScreen)
```

### `GameOverScreen` (refatoração do `end.java`)

- Não depende mais da classe `game`. Recebia um `game current` só para alcançar `current.frame`
  e `game.pointSys`; agora recebe o `JFrame` e a pontuação prontos.
- **Anima sozinha.** O `update_panel()` antigo precisava ser chamado pelo timer do jogo — e o
  timer do jogo para justamente quando o jogador perde. Agora a tela tem `javax.swing.Timer`
  próprio, encerrado quando ela fecha.
- **Tem saída:** botão "Voltar ao Menu", que dispara o callback e fecha o ciclo.
- **Não é modal.** Um diálogo modal bloqueia dentro do próprio `setVisible(true)`, então
  `showGameOver` só retornaria depois da tela fechar e a volta ao menu aconteceria aninhada
  dentro dela. (O `end.java` original também era não-modal; o `End_of_game.java` decompilado
  não era.)
- O `JLabel` era adicionado duas vezes (uma no painel, outra no próprio diálogo), e o
  `setContentPane` depois do `add` descartava metade do trabalho. Agora há uma árvore só.

`done.png` é 200×100 = 2 quadros de 100×100, confirmado no arquivo.

### `MainMenu`

- **Fundo:** `MainMenu_screen.png` (que não existe no projeto) → `spritesheet/inicial_screen.png`.
- **Botões:** `spritesheet/Menu_Stuff(1).png` continua sendo tentado; como ele não está no
  projeto hoje, os botões caem para um desenho em código com o texto **JOGAR** / **OPCOES**,
  com estado de hover. Basta colocar o PNG na pasta que a arte passa a ser usada, sem tocar
  no código.
- `ActionListener` no lugar de `MouseListener`. Um `MouseListener` num `JButton` dispara em
  qualquer clique, inclusive num que começa fora e termina em cima; o `ActionListener` é o
  evento de acionamento de verdade e também responde ao teclado.
- O botão de jogar chama `GameFlow.startNewGame()` em vez de construir a partida sozinho.

### `Main`

- Sobe no **menu**, não na partida.
- Inicializa o `MusicPlayer`, que **nunca era inicializado**: qualquer `setTrackAndPlay` antes de
  `initialiseMusicPlayer()` batia num clip nulo.
- Tudo dentro de `SwingUtilities.invokeLater`, e a janela só aparece depois de montada.

---

## 6. Sistema de colisão: o que NÃO foi alterado

Você pediu para ser avisado antes de qualquer mudança em `QuadTree.java` ou
`CollisionManifold.java`. **Nenhum dos dois foi tocado**, e não precisou ser. Explicando os dois
pontos onde isso poderia ter acontecido:

### 6.1 Buffs e gatilhos atravessáveis — resolvido sem mexer em nada

`BuffObj` e `EventTriggerObj` definem `inverse_mass = Double.POSITIVE_INFINITY`. No `preStep`
do manifold isso faz `k_normal` virar infinito (ou `NaN`), e a linha

```java
contact.normal_mass = (k_normal > EPSILON) ? 1.0 / k_normal : 0.0;
```

dá `0.0` nos dois casos — então `lambda` é sempre zero e nenhum impulso é aplicado. Ou seja: os
objetos de buff **já são atravessáveis** pelo motor atual, sem nenhuma alteração. Foi por isso
que a checagem de coleta pôde ficar fora do solver, no `step()`.

### 6.2 Buff `INTANGIBLE` — um filtro de pares, não uma mudança de motor

Este é o único lugar que encosta no caminho da colisão, e vale sua avaliação crítica.

No jogo antigo o efeito era um `return` cedo dentro de `ball.bounce()`. O equivalente no motor
novo é **não gerar o manifold** daquele par: sem manifold, o solver nunca vê o par e o jogador
atravessa o corpo. O trecho está em `GameMap.buildManifolds`, logo antes de
`CollisionManifold.generate`:

```java
if (BuffSystem.isIntangible()) {
    boolean a_is_player = body_a.getObjType() == GameObject.PLAYER;
    boolean b_is_player = body_b.getObjType() == GameObject.PLAYER;

    if (a_is_player || b_is_player) {
        GameObject other = a_is_player ? body_b : body_a;
        boolean other_is_trigger = other.getObjType() == GameObject.EVENT_TRIGGER_OBJ
                || other.getObjType() == GameObject.BUFF_OBJ;
        if (!other_is_trigger)
            continue;
    }
}
```

O que isso é e o que não é:

- **É** um filtro de pares na narrow phase do `GameMap`, do mesmo tipo dos `if` que já existiam
  logo abaixo (checagem do balde, som de quique, pontuação).
- **Não é** uma mudança de comportamento do `QuadTree` nem do `CollisionManifold`. As duas classes
  continuam byte a byte iguais.
- Gatilhos (balde) e objetos de buff seguem passando, senão o jogador ficaria impedido de vencer
  o nível ou de pegar itens justamente enquanto estivesse intangível.
- O par volta a ser considerado normalmente assim que o buff acaba. O cache de warm starting
  perde a entrada daquele par durante o buff, o que é o comportamento correto: não há contato
  para herdar impulso.

Se você preferir uma abordagem diferente — por exemplo uma flag `collision_enabled` no
`GameObject`, ou máscaras de camada de colisão — é uma troca de poucas linhas no mesmo ponto.
Me avise.

### 6.3 Guarda de reentrância no `step()` — mudança de controle, não de física

`LevelRules.nextLevel` abre `JDialog`s **modais** (`Adding_to_Map`, `Item_Select`), e é chamado
de dentro do `step()`. Um diálogo modal no EDT sobe um laço de eventos aninhado, e o `Timer` de
16 ms do `Game` continua disparando dentro dele — ou seja, `step()` reentrava enquanto a chamada
de fora ainda estava parada no diálogo, e a física avançava por trás da janela (a bola caindo,
perdendo vida, virando de nível de novo).

Isso já acontecia antes desta refatoração, mas ficou mais visível com a perda de vida ligada.
A correção é um flag booleano em volta do corpo do `step()`:

```java
public void step(double dt) {
    if (stepping) return;
    stepping = true;
    try { stepInternal(dt); } finally { stepping = false; }
}
```

Nenhuma linha de física mudou — o corpo antigo virou `stepInternal` sem alterações.

---

## 7. Correções pequenas de robustez

Três guardas contra `NullPointerException` que apareceram rodando o jogo com assets faltando.
Nenhuma muda comportamento quando os arquivos estão no lugar.

| Arquivo | Problema | Correção |
|---|---|---|
| `AnimationPlayer.paint` | `SpriteLoader` guarda o array mesmo quando a leitura do PNG falha, deixando posições nulas. Um asset faltando derrubava o `paintComponent` a cada quadro e a tela inteira parava de desenhar. | teste `sprites[current_frame] != null` |
| `MusicPlayer.setTrackAndPlay` | se `AudioSystem.getClip()` falhasse no init (máquina sem placa de som, driver ocupado), `clip` ficava nulo e a primeira troca de faixa derrubava a tela. Idem para `getResource()` nulo. | checa `clip` e a URL; segue mudo com um aviso |
| `SoundEffectPlayer.playSound` | `getAudioInputStream(null)` lançava NPE, virando um stack trace por quique da bola. | checa a URL; avisa uma vez por som que falta |

---

## 8. Como testei

Compilação: `javac` (OpenJDK 21) sobre todos os `.java` do projeto, sem erros nem avisos.

Dois testes de integração escritos e executados sob `Xvfb` (foram removidos depois; posso
recolocá-los como suíte permanente se você quiser):

**Teste de física / buffs** — todos passaram:

- jogador encosta num `BuffObj` → buff consumido, `SPEED_BOOST` ativo, +50 pontos;
- sem buff, uma `RigidObj` segura a bola em queda;
- com `INTANGIBLE`, a mesma bola atravessa a mesma parede;
- `ELASTIC_COLLISION` leva a restituição a 1.0 e a devolve ao valor original ao expirar;
- `ICED` freia a bola e vira `SLIPPERY`; `SLIPPERY` zera o atrito; `reset()` devolve o atrito;
- meta de pontos: 150 não bate, 300 bate, `Signal` emitido uma vez, `processPoints()` move o
  potencial para o total.

**Teste do ciclo de telas** — passou:

menu → clique em JOGAR → partida rodando com HP 5/5 e meta 300 → `paintComponent` completo
(mundo + HUD de pontos + corações + HUD de buffs) → dano até morrer → `GameOverScreen` →
"Voltar ao Menu" → menu → segunda partida com HP 5/5 e 0 pontos (estado global limpo).

---

## 9. Pendências e sugestões

1. **Apagar as duas cascas.** `PointCounter.java` e `End_of_game.java` ficaram como cascas
   `@Deprecated` que repassam para `PointSystem` / `GameOverScreen`, para não quebrar nada de
   uma vez. Não há estado duplicado. Quando quiser, apague os dois `.java` e os `.class`
   correspondentes.
2. **`spritesheet/Menu_Stuff(1).png`.** Enquanto não existir, os botões do menu são desenhados em
   código. É só colocar o arquivo na pasta.
3. **IDs colididos em `GameObject`** (seção 1): `ID_BALL_3`/`ID_PERMANENT_FLOOR` = 16 e
   `ID_BALL_4`/`ID_PERMANENT_WALL` = 17.
4. **Diálogos modais dentro do laço de física.** A guarda de reentrância resolve o sintoma pior,
   mas o desenho certo seria parar o `Timer` do `Game` antes de abrir os diálogos de escolha de
   item e retomar depois. Agora que `game_loop_timer` é um campo, é uma mudança pequena.
5. **Ajuste de dificuldade.** `BASE_TARGET_POINTS` (300), `TARGET_POINTS_PER_LEVEL` (150),
   `STARTING_HEARTS` (5), `REST_TICKS_REQUIRED` (45) e `REST_SPEED_THRESHOLD` (0.05) estão todos
   como constantes nomeadas no topo do `LevelRules` e do `Game`, feitos para serem mexidos.
6. **`MASSIVE_DRAG` e `SLIPPERY` não têm objeto no mapa.** Existem no enum e funcionam, mas
   nenhum `ID_BUFF_*` aponta para eles — `SLIPPERY` só é alcançado pelo `ICED`. Se quiser
   coletá-los, precisam de ID, sprite e entrada em `id_to_buffs`.

---
---

# Parte 2 — ícones do menu, volume, bugs e lançamento por estilingue

Segunda rodada de mudanças, feita depois da reintegração acima.

---

## 10. Ícones animados do menu

Arquivo: `MainMenu.java`.

Os botões usam agora os ícones animados da `combined_spritesheet.png`, pelas **fatias 12 (jogar)**
e **13 (opções)** — índice a partir de 0 —, tocados por `AnimationPlayer`, o mesmo player que os
objetos do jogo usam e no mesmo relógio global.

A folha tem 240×352 px = **15 quadros de 16×16 por linha, 22 linhas**, então cada ícone carrega
15 quadros a 15 FPS:

```java
new AnimationPlayer("menu_icon_play",    "spritesheet/combined_spritesheet.png", 16, 16, 12, 15, 15);
new AnimationPlayer("menu_icon_options", "spritesheet/combined_spritesheet.png", 16, 16, 13, 15, 15);
```

Cada ícone precisa de uma **chave própria** (`menu_icon_play` / `menu_icon_options`): o
`SpriteLoader` guarda os quadros fatiados por chave, então duas linhas diferentes com a mesma
chave devolveriam a mesma animação.

### Dois problemas que impediam isso de funcionar antes

1. **O sprite não existia.** O menu apontava para `spritesheet/Menu_Stuff(1).png`, que não está no
   projeto. A `combined_spritesheet.png` já tinha os ícones.
2. **O quadro nunca mudava.** O código antigo tinha `int animation = 0;` como variável **local do
   construtor**, usada dentro das classes anônimas dos botões. Uma variável capturada por classe
   anônima precisa ser efetivamente final, então ela valia zero para sempre — `animation % 4 * 32`
   dava sempre a mesma coluna.

### O `Timer` de repintura

O `AnimationPlayer` avança os quadros sozinho, no relógio global iniciado pelo `Main`, mas **nada
manda o Swing redesenhar o menu** — sem isso a animação roda e não aparece. O `MainMenu` ganhou um
`javax.swing.Timer` de 16 ms que só chama `repaint()` nos dois botões.

Esse timer é parado em `MainMenu.dispose()`, chamado pelo `GameFlow` ao sair do menu
(`stopCurrentMenu()`), pelo mesmo motivo que o laço da partida é parado: senão ele continuaria
repintando componentes que ninguém mais vê, por cima do jogo.

Se o spritesheet não carregar, os botões caem para o desenho em código com o texto **JOGAR** /
**OPCOES** — o jogo não quebra por falta de asset. O hover cresce o ícone 12%, para o botão
responder ao mouse sem precisar de outra linha de sprites.

---

## 11. Volume nas configurações

Arquivos: `MainMenu.java`, `MusicPlayer.java`, `SoundEffectPlayer.java`.

A tela de opções tinha dois `JPanel` vazios chamados `screenSizePanel` e `ballColorPanel`. Eles
deram lugar a **dois sliders de 0 a 100%**, um para a música e um para os efeitos, cada um com a
porcentagem ao lado:

- **Música:** acompanha ao vivo — o clip que já está tocando muda de volume enquanto você arrasta.
- **Efeitos:** o valor é aplicado ao vivo, mas o som de amostra só toca quando você **solta** o
  slider (`getValueIsAdjusting() == false`), senão tocaria dezenas de vezes durante o arraste.

O botão "Apply" virou "Voltar", que é o que ele realmente faz.

### O bug que o slider no zero encontraria

Os dois players faziam:

```java
gainControl.setValue(20f * (float) Math.log10(volume));
```

Com `volume = 0`, `log10(0)` é `-Infinity`, e `FloatControl.setValue(-Infinity)` lança
`IllegalArgumentException`. Ou seja: **o slider no zero — mudo, que é o valor que mais se usa —
derrubaria o som**. Fora do intervalo que o controle declara, o erro é o mesmo.

Isso virou `MusicPlayer.applyGain(FloatControl, float)`, usado pelos dois players:

```java
float decibels = (linear_volume <= 0.0001f)
        ? control.getMinimum()
        : (float) (20.0 * Math.log10(linear_volume));

if (decibels < control.getMinimum()) decibels = control.getMinimum();
if (decibels > control.getMaximum()) decibels = control.getMaximum();
control.setValue(decibels);
```

Mais `clampVolume()` (limita o valor linear a 0..1) e `getVolume()` nos dois players, para os
sliders nascerem na posição certa.

---

## 12. Bug corrigido: o teto do mapa perdia a hitbox

Arquivos: `LevelRules.java`, `GameMap.java`.

### A causa

O `nextLevel` limpa o mapa depois do painel do God. A lista de exceções era **por `obj_id`**:

```java
!(obj2.getObjType() == GameObject.PLAYER
  || obj2.getObjId() == GameObject.ID_PERMANENT_FLOOR
  || obj2.getObjId() == GameObject.ID_PERMANENT_WALL
  || obj2.getObjId() == GameObject.ID_BUCKET)
```

O **teto** do mapa é criado com `GameObject.ID_INVISIBLE_OBJ` — justamente para não ser desenhado.
Ele não estava na lista, então era desativado nesse laço. Como o `deleteInactiveObjs()` do
`GameMap` remove os inativos no fim do passo de física, **o teto sumia de vez do mapa na primeira
vez que o painel do God abria**, e a partir dali a bola atravessava o topo.

Reproduzido antes de mexer:

```
1) bola sobe contra o teto  -> y mínimo alcançado: 547.7   (o teto segurou)
2) wipe do painel do God    -> desativou 1 objeto permanente
3) mesma bola, mesmo tiro   -> y mínimo alcançado: -413.9  (atravessou)
```

### A correção

Em vez de tentar manter uma lista de ids em dia, a limpeza **pula a lista de objetos permanentes
inteira**. Qualquer estrutura fixa adicionada ao mapa no futuro já fica protegida por construção.

- `GameMap.getPermanentObjects()` — getter novo para a lista.
- `LevelRules.wipeObjectsNotKept(GameMap, Set<GameObject>)` — a limpeza foi extraída do
  `nextLevel` para um método próprio, para poder ser testada sem abrir os diálogos modais.

---

## 13. Bug corrigido: bola parada com a meta atingida não resetava

Arquivo: `Game.java`, em `checkBallAtRest()`.

Na parte 1 havia este trecho:

```java
// Meta batida: o arremesso acabou, mas nao custa vida.
if (PointSystem.hasReachedTarget())
    return;
```

O `return` dispensava a vida — mas dispensava junto o `respawnBall()` logo abaixo. Resultado: com
a meta já atingida, a bola ficava largada onde parou, sem nada acontecendo. Com o lançamento único
da seção 14 isso **travaria a partida de vez**, porque não há mais como empurrar a bola no clique.

Conforme decidido, o comportamento agora é o mais simples e previsível: **o fim de um arremesso
sempre custa um coração e sempre devolve a bola ao spawn**, com a meta batida ou não. A meta
continua valendo no fim de nível (`LevelRules.nextLevel`) e na pontuação.

Também foi adicionada uma guarda no topo do método: uma bola **armada** está parada de propósito,
esperando o lançamento, e não pode ser confundida com uma bola que morreu parada.

---

## 14. Lançamento único por estilingue

Arquivos: `Slingshot.java` (novo), `Game.java`.

### O que mudou no controle

Antes, **todo clique** somava velocidade à bola:

```java
pingbongBall.velocity = xy.subtract(centro)
    .multiply(0.08 * inverse_mass)
    .add(pingbongBall.velocity);   // <- e ainda acumulava em cima da velocidade atual
```

Dava para empurrar a bola no ar quantas vezes quisesse. Agora a bola nasce **armada**:

1. fica **parada** no ponto de spawn — sem gravidade e sem velocidade residual;
2. o estilingue mira junto com o mouse;
3. **um** clique a lança;
4. a partir daí os cliques não mexem mais nela.

A velocidade agora é **atribuída**, não somada. O lançamento é rearmado quando:

- a bola para depois de um arremesso (`respawnBall`);
- o nível vira (`next_level`);
- o modo de edição termina — o clique que fecha a edição **não** lança a bola, só devolve o
  controle com ela armada;
- a tecla de debug `1` é usada.

O congelamento é reaplicado a cada tick (`updateArmedBall`), porque o `GameMap.step` reescreve a
aceleração de todo corpo móvel em cada substep.

### A câmera no modo de edição

Continua como estava: o ramo `else` do laço principal, que segue o mouse quando o modo não é
`GAMELOOP`, não foi tocado.

### O estilingue

`Slingshot.java` traz de volta o estilingue que existia no `oldGame/game.java` (o bloco
`if (aiming)` do `paintComponent`, com `sling_counter` e `g2.rotate(angle, centerX, centerY)`),
agora como um objeto com estado próprio em vez de variáveis estáticas espalhadas pelo painel.

Os quadros vêm da linha `GameObject.ID_SLINGSHOT` (5) da `combined_spritesheet.png`, 15 quadros
de 16×16, divididos em duas fases:

| Quadros | Fase |
|---|---|
| 0 – 9 | carregar: avança enquanto você mira e **segura** no quadro 9 |
| 10 – 14 | disparo: toca uma vez e some |

**Por que não usa `AnimationPlayer`:** o `AnimationPlayer` roda em loop, sempre para frente, no
mesmo compasso para todas as instâncias da mesma chave. As duas fases acima não cabem num loop —
a mira precisa segurar num quadro pelo tempo que o jogador quiser, e o disparo precisa tocar uma
vez só. Os quadros vêm do `SpriteLoader` (o mesmo cache que o `AnimationPlayer` usa), indexados à
mão — mesma abordagem do `HealthSystem`.

O estilingue é desenhado em **espaço de mundo**, dentro do `Graphics2D` já escalado e transladado
pela câmera, para acompanhar a bola quando a câmera se move. Ele é rotacionado na direção do mouse
e deslocado para trás da bola (`PIVOT_OFFSET_FACTOR`), de forma que a bola pareça encaixada na
bolsa.

### Posição do mouse

O `MouseMotionListener` do `Game`, que estava vazio, agora alimenta a posição do mouse. Antes o
painel só consultava `MouseInfo.getPointerInfo()` na hora de pintar, e convertia em relação ao
`Main.frame` — o que inclui a barra de título, e por isso **não batia** com o `e.getX()` do
clique. O listener dá o valor certo, relativo ao próprio painel, e de graça.

---

## 15. Teclas de debug

Arquivo: `Game.java`, em `keyPressed`.

| Tecla | Efeito |
|---|---|
| `0` | liga/desliga o desenho das hitboxes |
| `1` | teleporta a bola para a posição atual do mouse |

Detalhes:

- Os dois códigos de tecla são aceitos (fileira de números **e** teclado numérico), porque `VK_0`
  e `VK_NUMPAD0` são teclas diferentes para o Swing.
- A tecla `1` move pelo **centro de massa** (`changeCenterOfMass`), para a bola cair exatamente sob
  o cursor em vez de encostar nele com o canto superior esquerdo. Ela também zera a rotação e
  **rearma o lançamento**: teleportar a bola é o começo de uma tentativa nova, e sem isso ela
  ficaria parada no lugar novo sem nada para fazer.
- A tecla `H` provisória da parte 1 foi removida; `0` ocupa o lugar dela.

---

## 16. Como testei (parte 2)

Compilação: `javac` (OpenJDK 21), sem erros nem avisos.

Dois testes de integração executados sob `Xvfb` (removidos depois; posso recolocá-los como suíte
permanente). **Todos passaram.**

**Teste de funcionalidades:**

- teto existe, está na lista de permanentes e **continua ativo depois do wipe**; a bola não
  atravessa o topo (`y >= 0`);
- a bola nasce armada e **não cai** enquanto espera (deslocamento < 1 unidade em 1,2 s);
- o clique lança a bola (desarma, ganha velocidade);
- com a física congelada, **cliques posteriores não alteram a velocidade** e a bola continua
  desarmada;
- tecla `0` inverte as hitboxes;
- tecla `1` leva a bola ao mouse (alvo e chegada idênticos até a casa decimal) e rearma;
- os ícones `menu_icon_play` e `menu_icon_options` carregam 15 quadros cada, todos não nulos, e as
  duas imagens são diferentes entre si;
- volume 0 não lança exceção; valores acima de 1 e negativos são limitados; música e efeitos são
  independentes;
- estilingue: começa invisível → mira → segura no último quadro de carga → dispara → some.

**Teste da bola parada, com a meta forçada como atingida:**

```
meta atingida? true
hp antes: 5    spawn: (100.0, 500.0)
lançou? armada=false
hp depois: 4
posição da bola: (100.0, 500.0)
armada de novo? true
```

---

## 17. Pendências (atualizado)

Continuam valendo os itens 1, 3, 4, 5 e 6 da seção 9. O item 2 (`Menu_Stuff(1).png`) saiu: o menu
usa a `combined_spritesheet.png` agora.

Novos pontos que valem sua avaliação:

1. **Onde o estilingue aponta.** Ele gira na direção do clique/mouse, não na direção contrária.
   Se você preferir a leitura de "puxar o elástico para trás", é inverter o sinal do ângulo em
   `Slingshot.aim` e `Slingshot.release` — uma linha em cada.
2. **`SIZE_FACTOR` e `PIVOT_OFFSET_FACTOR`** no topo do `Slingshot` controlam o tamanho e a
   distância do estilingue em relação à bola. Estão em 2,6 e 0,55 do diâmetro; são o primeiro
   lugar a mexer se ele parecer grande ou deslocado demais na tela.
3. **Força do lançamento.** Continua o `0.08 * inverse_mass` do código antigo, e agora ela é o
   ÚNICO controle que o jogador tem sobre a bola — pode valer a pena ajustar, ou limitar a
   distância máxima do clique para a força não depender do tamanho da tela.
4. **Volume não persiste.** Os sliders valem só enquanto o jogo está aberto; ao reabrir, ambos
   voltam para 10%. Se quiser guardar, um `java.util.Properties` gravado num arquivo ao aplicar
   resolve.

---
---

# Parte 3 — ícones do `Menu_Icons.png` e escolha da bola

---

## 18. Ícones do menu trocados para o `Menu_Icons.png`

Arquivo: `MainMenu.java`.

Os ícones das linhas 12 e 13 da `combined_spritesheet.png` são de **16×16**, e esticados no
tamanho de um botão ficavam pixelados demais. Eles deram lugar ao arquivo novo
`spritesheet/Menu_Icons.png`.

Formato confirmado no arquivo: **128×192 px = 4 quadros de 32×32 por linha, 6 linhas**.

| Linha | Conteúdo |
|---|---|
| 0 | botão **START** |
| 1 | botão de opções (chave inglesa) |
| 2 – 5 | as quatro bolas, em 32×32 (não usadas aqui — a prévia usa a `combined_spritesheet`) |

```java
new AnimationPlayer("menu_icon_play",    "spritesheet/Menu_Icons.png", 32, 32, 0, 4, 8);
new AnimationPlayer("menu_icon_options", "spritesheet/Menu_Icons.png", 32, 32, 1, 4, 8);
```

Duas mudanças de número acompanham a troca:

- **4 quadros** em vez de 15, e **8 FPS** em vez de 15. Com 15 FPS os 4 quadros fechariam o ciclo
  em 0,27 s, rápido demais para o leve balanço que a arte faz; a 8 FPS o ciclo leva 0,5 s.
- Os botões passaram de 150×150 para **160×160 px**, que é 5× exatos de 32. Num múltiplo inteiro o
  nearest-neighbour não deixa umas linhas de pixel mais grossas que as outras.

---

## 19. Bug corrigido: o último quadro de toda animação nunca era desenhado

Arquivo: `AnimationPlayer.java`, em `update()`.

```java
if (current_frame >= last_frame) {   // <- era assim
    current_frame = 0;
}
```

`last_frame` é o **índice** do último quadro (`length - 1`), então essa condição reiniciava a
animação **ao chegar nele**: o último quadro nunca aparecia. Com 15 quadros dava para não reparar
(perdia 1 de 15); com os ícones do menu, que têm 4, sumia **um quarto** da animação.

A correção é trocar `>=` por `>`. Ela vale para todas as animações do jogo — objetos, bolas,
buffs — que agora tocam o ciclo inteiro.

Verificado rodando 400 ticks de um `AnimationPlayer` de 4 quadros e coletando os índices
alcançados: antes `[0, 1, 2]`, agora `[0, 1, 2, 3]`.

---

## 20. Escolha do sprite da bola nas opções

Arquivos: `MainMenu.java`, `GameRules.java`, `Game.java`.

A tela de opções ganhou uma fileira com as **quatro bolas**, mais uma **prévia animada maior** da
escolhida, com o nome embaixo. A bola selecionada fica realçada em amarelo.

Os quatro sprites são as linhas **14 a 17** da `combined_spritesheet.png`, que são exatamente os
valores de `ID_BALL_1` a `ID_BALL_4` — é o `obj_id` que escolhe a linha da folha em
`GameObject.createAnimationPlayer()`. Trocar o id troca a aparência, sem mexer em mais nada.

Em `GameRules`:

```java
static final int[] BALL_IDS = { ID_BALL_1, ID_BALL_2, ID_BALL_3, ID_BALL_4 };
static final String[] BALL_NAMES = { "Classica", "Dorminhoca", "Halterofilista", "Elegante" };
static int selected_ball_id = GameObject.ID_BALL_1;
```

`Game.startGame()` lê `GameRules.selected_ball_id` na hora de criar a bola, no lugar do
`GameObject.ID_BALL_1` que estava fixo no código. A escolha vale **a partir da próxima partida** —
que na prática é sempre, porque as opções só são acessíveis pelo menu.

### A colisão de IDs

Isso só funciona porque `ID_PERMANENT_FLOOR` e `ID_PERMANENT_WALL` **já estão em 98 e 99** no seu
código. Enquanto valiam 16 e 17 eles colidiam com `ID_BALL_3` e `ID_BALL_4`, e uma bola com esses
ids receberia o sprite da **tábua do chão**, porque o `createAnimationPlayer` testa o chão e a
parede antes de cair no caso geral da folha. Com 98/99 as quatro bolas são selecionáveis sem
nenhuma gambiarra. O aviso da seção 1 sobre essa colisão fica resolvido.

### A prévia compartilha os quadros com o jogo

As animações da prévia usam a chave `"objects1_<id>"`, que é **a mesma** que o
`GameObject.createAnimationPlayer` usa para os objetos do jogo. Isso é de propósito: o
`SpriteLoader` guarda os quadros fatiados por chave, então a prévia do menu e o sprite da bola em
partida compartilham o mesmo fatiamento em vez de carregar a folha duas vezes.

### Layout da tela de opções

Os controles ficavam soltos por cima da arte do fundo (uma rua cheia de detalhe), e o *thumb*
branco do `JSlider` sumia contra as luzes. Tudo passou para um **cartão escuro translúcido**
centralizado, com título, largura fixa de 520 px e o botão "Voltar" no canto. O `Timer` de
repintura do menu foi estendido para redesenhar o painel de opções enquanto ele está na tela,
porque a prévia da bola é animada.

---

## 21. Como testei (parte 3)

Compilação: `javac` (OpenJDK 21), sem erros nem avisos. Teste de integração sob `Xvfb` —
**todos passaram**:

- os ícones `menu_icon_play` e `menu_icon_options` carregam **4 quadros de 32×32**, todos não
  nulos, diferentes entre si, e os 4 quadros do START não são todos idênticos (a animação existe
  mesmo);
- um `AnimationPlayer` de 4 quadros alcança os índices `[0, 1, 2, 3]` ao longo de 400 ticks;
- as quatro bolas carregam 15 quadros cada; a bola 1 é diferente da bola 3 (a colisão de ids não
  atrapalha mais);
- a tela de opções abre e pinta sem exceção, com 4 botões de bola;
- clicar no terceiro botão muda `GameRules.selected_ball_id`;
- voltar ao menu e começar a partida cria a bola com **o sprite escolhido**
  (`Game.pingbongBall.getObjId() == BALL_IDS[2]`).

Também tirei retratos renderizados do menu, da tela de opções e da partida para conferir o visual:
os botões novos aparecem em 32×32 sem borrar, a bola selecionada fica realçada, a prévia mostra a
bola certa com o nome, e em jogo a bola do jogador usa o sprite escolhido.

---

## 22. Pendências (atualizado)

Continuam valendo os itens da seção 17, menos o alerta sobre a colisão de IDs, que você já
resolveu com 98/99.

Novos pontos:

1. **A escolha da bola não persiste**, pelo mesmo motivo que o volume: tudo vive em memória. Um
   `java.util.Properties` gravado num arquivo resolveria os três de uma vez (volume da música,
   volume dos efeitos, bola escolhida).
2. **Os nomes das bolas** (`GameRules.BALL_NAMES`) eu inventei olhando os sprites: Classica,
   Dorminhoca, Halterofilista, Elegante. Troque à vontade — é um array de strings num lugar só.
3. **As linhas 2 a 5 do `Menu_Icons.png`** têm as mesmas quatro bolas em 32×32. A prévia das
   opções usa a versão 16×16 da `combined_spritesheet` para compartilhar cache com o jogo; se
   quiser a prévia mais nítida, é trocar a folha e a chave em `loadBallIcon`.
