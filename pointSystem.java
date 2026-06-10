// Just the logic, made to be used inside the ball class or the main game loop.

public class pointSystem {
    private int points;

    public pointSystem() {
        this.points = 0;
    }


    // Placeholder values, ideally add point values to the objects themselves
    // Use coisa id for pts
    public void addPoints(coisa obj) {
        if (obj instanceof buff) {
            points += 10;
        }
        else {
            points += 1;
        }
    }

    public int getPoints() {
        return points;
    }
}