package model;

import model.skills.Element;

public class Goblin extends Enemy {
    public Goblin(int wave) {
        super("Goblin Grunt", 40 + (wave * 10), 8 + (wave * 2), 0, Element.PHYSICAL);
    }
}
