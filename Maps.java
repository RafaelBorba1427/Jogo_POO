import java.util.ArrayList;

class Maps {
    static int number = 10;

    static ArrayList<GameObject> generation(int i) {
        ArrayList<GameObject> saida = new ArrayList<GameObject>();
        switch (i) {
            case 1:
                MovableObj obj_render_test7 = new MovableObj(1000f, 400f, 67, 40, 0, 1, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                MovableObj obj_render_test6 = new MovableObj(900f, 400f, 80, 180, 0, 1, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                MovableObj obj_render_test5 = new MovableObj(800f, 400f, 99, 300, 0, 1, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                MovableObj obj_render_test4 = new MovableObj(700f, 400f, 30, 400, 0, 1, GameRules.DEFAULT_FRICTION,
                        true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3);
                RigidObj obj_render_test3 = new RigidObj(100f, 300f, 50f, 40f, (Math.PI / 4),
                        GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG);

                BallObj obj_render_test2 = new BallObj(200f, 200f, 60f, 1, GameRules.DEFAULT_FRICTION, true,
                        GameObject.ID_BALL_2,
                        0.8);

                obj_render_test7.changeNoGravityOnSpawnStatus(true);
                obj_render_test6.changeNoGravityOnSpawnStatus(true);
                obj_render_test5.changeNoGravityOnSpawnStatus(true);
                obj_render_test4.changeNoGravityOnSpawnStatus(true);
                obj_render_test2.changeNoGravityOnSpawnStatus(true);
                saida.add(obj_render_test7);
                saida.add(obj_render_test6);
                saida.add(obj_render_test5);
                saida.add(obj_render_test4);
                saida.add(obj_render_test3);
                saida.add(obj_render_test2);
                break;
            case 2:
                saida.add(new RigidObj(600f, 600f, 67f, 40f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(1000f, 400f, 80f, 180f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(900f, 500f, 99f, 300f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(700f, 400f, 30f, 400f, 0, GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));

                saida.add(new RigidObj(200f, 600f, 50f, 40f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new BallObj(200f, 200f, 60f, 1, GameRules.DEFAULT_FRICTION, true,
                        GameObject.ID_BALL_2,
                        0.8));

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
                saida.add(new RigidObj(150f, 400f, 60f, 400f, 0, GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));

                saida.add(new RigidObj(300f, 700f, 50f, 60f, 0, GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                break;
            case 4:
                saida.add(new RigidObj(781f, 623f, 74f, 58f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(987f, 412f, 46f, 91f, (Math.PI / 5), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(305f, 855f, 62f, 33f, (Math.PI / 1), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(453f, 271f, 88f, 77f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(622f, 738f, 35f, 49f, (Math.PI / 8), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                break;
            case 5:
                saida.add(new RigidObj(544f, 902f, 41f, 66f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(213f, 347f, 95f, 52f, (Math.PI / 7), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(869f, 210f, 33f, 84f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(390f, 761f, 70f, 30f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(958f, 500f, 58f, 99f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                break;
            case 6:
                saida.add(new RigidObj(672f, 288f, 87f, 45f, (Math.PI / 5), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(931f, 654f, 52f, 73f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(258f, 819f, 64f, 38f, (Math.PI / 9), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(447f, 372f, 30f, 91f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(806f, 947f, 79f, 56f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                break;
            case 7:
                saida.add(new RigidObj(538f, 719f, 61f, 44f, (Math.PI / 5), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(892f, 347f, 38f, 97f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(217f, 605f, 74f, 30f, (Math.PI / 8), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new MovableObj(714f, 863f, 47, 92, (Math.PI / 4), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                saida.add(new MovableObj(365f, 291f, 83, 56, (Math.PI / 7), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));

                break;
            case 8:
                saida.add(new RigidObj(429f, 856f, 55f, 68f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(741f, 264f, 92f, 41f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(298f, 592f, 37f, 79f, (Math.PI / 9), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(958f, 348f, 66f, 53f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new MovableObj(600f, 913f, 63, 34, (Math.PI / 2), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                break;

            case 9:
                saida.add(new RigidObj(312f, 847f, 58f, 71f, (Math.PI / 5), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(689f, 253f, 44f, 90f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new MovableObj(950f, 610f, 67, 38, (Math.PI / 2), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                saida.add(new MovableObj(214f, 480f, 35, 82, (Math.PI / 7), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                saida.add(new MovableObj(535f, 900f, 91, 47, (Math.PI / 4), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                break;
            case 10:
                saida.add(new RigidObj(200f, 847f, 58f, 71f, (Math.PI / 5), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(320f, 253f, 44f, 90f, (Math.PI / 3), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(440f, 610f, 67f, 38f, (Math.PI / 2), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(560f, 480f, 35f, 82f, (Math.PI / 7), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new RigidObj(680f, 900f, 91f, 47f, (Math.PI / 4), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(800f, 340f, 52f, 63f, (Math.PI / 6), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PERMANENT_WALL));
                saida.add(new RigidObj(920f, 700f, 40f, 55f, (Math.PI / 9), GameRules.DEFAULT_FRICTION, true,
                        true,
                        GameObject.ID_PINGPONG));
                saida.add(new MovableObj(650f, 230f, 78, 33, (Math.PI / 2), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                saida.add(new MovableObj(900f, 800f, 60, 90, (Math.PI / 3), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                saida.add(new MovableObj(250f, 600f, 45, 70, (Math.PI / 5), 1, GameRules.DEFAULT_FRICTION, true, true,
                        true,
                        GameObject.ID_PERMANENT_WALL, 0.3));
                break;

        }
        return saida;
    }

}
