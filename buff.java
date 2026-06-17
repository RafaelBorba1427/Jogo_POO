class buff extends coisa {

  public buffSystem.buffs buff_active;
  public game current;

  public buff(int x, int y, int diametro, buffSystem.buffs buff, int id, game current) {
    super(x, y, diametro, id, current);
    this.buff_active = buff;
    this.buff = true;
  };

  @Override
  public void verify(ball bola) {
    //System.out.println("buff verify called buffSys=" + game.buffSys + " buff_active=" + buff_active);
    if ((intersects(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2, bola.getDiameter(),
        bola.getDiameter())
        || contains(bola.getX() - bola.getDiameter() / 2.0, bola.getY() - bola.getDiameter() / 2.0))
        && (bola.bateuX == false || bola.bateuY == false)) {

      game.gaming.buffSys.ApplyBuff(buff_active, 10);
      bateu = true;

    } else {
      game.add++;
    }

  }

}
