package view.screens;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import model.skills.Skill;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.rendering.PixelRenderer;
import view.ui.CombatMenu;

/**
 * Implements Screen. Displays 3 skill options from engine.getPendingSkillChoices().
 * Shows skill name, element color, mana cost, description.
 * Player clicks or presses 1-3 to select. Calls engine.processSkillChoice(index).
 */
public class SkillChoiceScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private int selectedIndex;

    public SkillChoiceScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.selectedIndex = 0;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.0f, 0.1f, 1f);

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        font.setColor(Color.GOLD);
        font.draw(batch, "Choose a New Skill!", 90f, 220f);

        List<Skill> choices = engine.getPendingSkillChoices();
        if (choices != null) {
            float startY = 180f;
            float spacing = 50f;

            for (int i = 0; i < choices.size(); i++) {
                Skill skill = choices.get(i);
                float yPos = startY - i * spacing;

                // Highlight selected
                Color elementColor = CombatMenu.getElementColor(skill.getElement());
                boolean isSelected = (i == selectedIndex);

                // Skill number and name
                font.setColor(isSelected ? Color.YELLOW : Color.WHITE);
                String prefix = isSelected ? "> " : "  ";
                font.draw(batch, prefix + (i + 1) + ". " + skill.getName(), 30f, yPos);

                // Element and mana cost
                font.setColor(elementColor);
                font.draw(batch, "[" + skill.getElement().name() + "]", 30f, yPos - 10f);

                font.setColor(Color.CYAN);
                font.draw(batch, "Mana: " + skill.getManaCost(), 100f, yPos - 10f);

                // Cooldown info
                if (skill.getCooldownTurns() > 0) {
                    font.setColor(Color.GRAY);
                    font.draw(batch, "CD: " + skill.getCooldownTurns() + " turns", 170f, yPos - 10f);
                }

                // Effect description
                font.setColor(Color.WHITE);
                String effectDesc = getEffectDescription(skill);
                font.draw(batch, effectDesc, 30f, yPos - 22f);
            }
        }

        font.setColor(Color.GRAY);
        font.draw(batch, "Press 1-3 or Enter to select", 70f, 20f);

        font.setColor(Color.WHITE);
        renderer.end();
    }

    private String getEffectDescription(Skill skill) {
        switch (skill.getSkillEffect()) {
            case DAMAGE: return "Deals " + (int)(skill.getDamageMultiplier() * 100) + "% damage";
            case HEAL: return "Restores HP";
            case BUFF_DEF: return "Increases defense";
            case BUFF_ATK: return "Increases attack";
            case STUN: return "Damages and stuns target";
            case MULTI_TARGET: return "Hits all enemies";
            case DRAIN_LIFE: return "Damages and heals caster";
            case ASSASSINATE: return "6x dmg if target < 30% HP";
            case BLOOD_PACT: return "Costs 20 HP, deals 5x damage";
            case DIVINE_SHIELD: return "Blocks next 2 attacks";
            case BERSERKER_RAGE: return "+100% ATK, -50% DEF for 3 turns";
            case RESURRECTION: return "Auto-revive at 50% HP on death";
            default: return "";
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        List<Skill> choices = engine.getPendingSkillChoices();
        if (choices == null || choices.isEmpty()) return false;

        switch (keycode) {
            case Input.Keys.NUM_1:
                selectSkill(0, choices);
                return true;
            case Input.Keys.NUM_2:
                selectSkill(1, choices);
                return true;
            case Input.Keys.NUM_3:
                selectSkill(2, choices);
                return true;
            case Input.Keys.UP:
                selectedIndex = Math.max(0, selectedIndex - 1);
                return true;
            case Input.Keys.DOWN:
                selectedIndex = Math.min(choices.size() - 1, selectedIndex + 1);
                return true;
            case Input.Keys.ENTER:
                selectSkill(selectedIndex, choices);
                return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        List<Skill> choices = engine.getPendingSkillChoices();
        if (choices == null || choices.isEmpty()) return false;

        // Simple click detection by screen region
        float normalizedY = 1f - (float) screenY / Gdx.graphics.getHeight();
        float startY = 180f / 240f;
        float spacing = 50f / 240f;

        for (int i = 0; i < choices.size(); i++) {
            float optionY = startY - i * spacing;
            if (normalizedY >= optionY - spacing / 2f && normalizedY <= optionY + spacing / 2f) {
                selectSkill(i, choices);
                return true;
            }
        }
        return false;
    }

    private void selectSkill(int index, List<Skill> choices) {
        if (index >= 0 && index < choices.size()) {
            engine.processSkillChoice(index);
            game.setScreen(new GameScreen(game, engine));
        }
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
