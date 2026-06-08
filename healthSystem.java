// Dunno how you expected this to be implemented, made it as a subclass to be added either to ball or to the main game loop


public class healthSystem {
    private int max_hp;
    private int current_hp;

    // A bit redundant, but might be useful
    private boolean is_dead;


    public healthSystem(int max_hp) {
        this.max_hp = max_hp;
        this.current_hp = max_hp;
        this.is_dead = false;
    }


    //returns true if the player dies, false otherwise
    public boolean takeDamageAndCheckDeath(){
        if(current_hp > 0){
            current_hp--;
        }
        else{
            this.is_dead = true;
            return true; // Dead
        }
        return false; // Not dead
    }


    public void heal(){
        if(current_hp < max_hp){
            current_hp++;
        }
    }


    // Getters
    public int getCurrentHp() {
        return current_hp;
    }
    public int getMaxHp() {
        return max_hp;
    }
    public boolean isDead() {
        return is_dead;
    }
}
