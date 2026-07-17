package model;

import model.skills.Element;

public class Skeleton extends Enemy {
    public Skeleton(int wave) {
        super("Skeleton Warrior",
              80 + ((wave - 10) * 12),
              15 + ((wave - 10) * 3),
              5 + ((wave - 10) * 1),
              Element.DARK);
    }
}
