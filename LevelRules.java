import javax.swing.JFrame;

class LevelRules {
   int counter = 0;
   int level_cap = 0;
   int level_count = 0;
   int health = 1;
   int points = 0;
   JFrame frame;
   Item_Select god;
   Adding_to_Map adition;
   boolean bateu = false;

   LevelRules(JFrame var1) {
      this.frame = var1;
      this.god = new Item_Select(var1);
      this.adition = new Adding_to_Map(var1);
   }

   int generate_cap() {
      return this.level_cap == 0 ? 1 : this.level_cap + 2 * (this.level_cap - 1);
   }

   public void nextLevel() {

      if (this.health == 0) {
         new End_of_game(this.frame, this.points);
      } else {

         this.counter++;
         this.god.dialogInit(this);
         if (this.counter >= this.level_cap) {
            this.adition.dialog_init(2, this);

         }
         this.counter = 0;
         this.generate_cap();
      }
   }
}
