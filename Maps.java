import java.util.ArrayList;

class Maps {
    static int number = 3;

    static ArrayList<GameObject> generation(int map_select) {
        ArrayList<GameObject> saida = new ArrayList<GameObject>();
        switch (map_select) {
            case 1:
                int width = 50, height = 100;
                BuffObj speed_buff = new BuffObj(800, 200, 50, 50, 0, false, true, GameObject.ID_BUFF_SPEED_BOOST);
                saida.add(speed_buff);
                for(int wall_qtd = 0; wall_qtd < 10; wall_qtd++){
                        MovableObj obj = new MovableObj(1000, 50 + (height + 10)* wall_qtd, width, height, 0, .4, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                
                saida.add(obj);
                }

                for(int wall_qtd = 0; wall_qtd < 10; wall_qtd++){
                        MovableObj obj = new MovableObj(1080, 50 + (height + 10)* wall_qtd, width, height, 0, .4, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                
                saida.add(obj);
                }
                break;
            case 2:
                BuffObj intangible_buff_1 = new BuffObj(800, 200, 50, 50, 0, false, true, GameObject.ID_BUFF_INTANGIBLE);
                saida.add(intangible_buff_1);
                BuffObj intangible_buff_2 = new BuffObj(800, 600, 50, 50, 0, false, true, GameObject.ID_BUFF_INTANGIBLE);
                saida.add(intangible_buff_2);

                RigidObj great_wall = new RigidObj(1000, 25, 50, 925, 0, GameRules.DEFAULT_FRICTION, false, true, GameObject.ID_PERMANENT_WALL);
                saida.add(great_wall);
                break;
            case 3:
                saida.add(new RigidObj(1000f, 600f, 67f, 40f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(820f, 400f, 80f, 180f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(1000f, 800f, 99f, 300f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(150f, 400f, 60f, 400f, Math.PI / 6, GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));

                saida.add(new RigidObj(300f, 700f, 50f, 60f, 0, GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                break;
        }
        return saida;
    }

}
