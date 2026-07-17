package view.screens;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import model.skills.Skill;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;
import view.ui.CombatMenu;

/**
 * Implements Screen. Displays 3 skill options from engine.getPendingSkillChoices().
 * Shows skill name, element icon, mana cost, description.
 * Decorative frame border using getMenuFrame(). Element icons from getElementIcon().
 */
public class SkillChoiceScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private int selectedIndex;
    private float bgScrollY;

    public SkillChoiceScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.selectedIndex = 0;
        this.bgScrollY = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        bgScrollY += delta * 5f;

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Draw darker background tiles
        drawBackground(batch);

        // Draw decorative frame border
        drawDecorativeFrame(batch);

        // Title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "Choose a New Skill!", 90f, 225f);

        List<Skill> choices = engine.getPendingSkillChoices();
        if (choices != null) {
            float startY = 190f;
            float spacing = 55f;

            for (int i = 0; i < choices.size(); i++) {
                Skill skill = choices.get(i);
                float yPos = startY - i * spacing;

                Color elementColor = CombatMenu.getElementColor(skill.getElement());
                boolean isSelected = (i == selectedIndex);

                // Draw element icon next to skill
                TextureRegion elementIcon = assets.getElementIcon(skill.getElement());
                if (elementIcon != null) {
                    batch.setColor(Color.WHITE);
                    batch.draw(elementIcon, 15f, yPos - 14f, 12f, 12f);
                }

                // Skill number and name with element color
                font.setColor(isSelected ? ColorPalette.CRIT_YELLOW : elementColor);
                String prefix = isSelected ? "> " : "  ";
                font.draw(batch, prefix + (i + 1) + ". " + skill.getName(), 30f, yPos);

                // Element tag and mana cost
                font.setColor(elementColor);
                font.draw(batch, "[" + skill.getElement().name() + "]", 30f, yPos - 12f);

                font.setColor(ColorPalette.MP_BLUE);
                font.draw(batch, "Mana: " + skill.getManaCost(), 100f, yPos - 12f);

                // Cooldown info
                if (skill.getCooldownTurns() > 0) {
                    font.setColor(ColorPalette.UI_BORDER);
                    font.draw(batch, "CD: " + skill.getCooldownTurns() + " turns", 170f, yPos - 12f);
                }

                // Effect description
                font.setColor(ColorPalette.TEXT_WHITE);
                String effectDesc = getEffectDescription(skill);
                font.draw(batch, effectDesc, 30f, yPos - 24f);

                // Draw selection indicator line for selected item
                if (isSelected) {
                    TextureRegion particleTex = assets.getParticleTexture("physical");
                    if (particleTex != null) {
                        batch.setColor(elementColor.r, elementColor.g, elementColor.b, 0.6f);
                        batch.draw(particleTex, 10f, yPos - 30f, 290f, 1f);
                        batch.setColor(Color.WHITE);
                    }
                }
            }
        }

        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "Press 1-3 or Enter to select", 70f, 20f);

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;
        float scrollOffset = bgScrollY % tileH;

        batch.setColor(0.2f, 0.15f, 0.3f, 0.5f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += tileW) {
            for (int y = -tileH; y < PixelRenderer.VIRTUAL_HEIGHT + tileH; y += tileH) {
                batch.draw(bgTile, x, y + scrollOffset);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawDecorativeFrame(SpriteBatch batch) {
        TextureRegion menuFrame = assets.getMenuFrame();
        if (menuFrame == null) return;

        // Draw frame around the skill area
        // Top-left corner
        batch.draw(menuFrame, 2f, 2f, 16f, 16f);
        // Top-right corner
        batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, 2f, 16f, 16f);
        // Bottom-left corner
        batch.draw(menuFrame, 2f, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);
        // Bottom-right corner
        batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);

        // Top and bottom edges
        for (int x = 20; x < PixelRenderer.VIRTUAL_WIDTH - 20; x += 16) {
            batch.draw(menuFrame, x, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);
            batch.draw(menuFrame, x, 2f, 16f, 16f);
        }

        // Left and right edges
        for (int y = 20; y < PixelRenderer.VIRTUAL_HEIGHT - 20; y += 16) {
            batch.draw(menuFrame, 2f, y, 16f, 16f);
            batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, y, 16f, 16f);
        }
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

        Vector2 worldCoords = renderer.getViewport().unproject(new Vector2(screenX, screenY));
        float worldY = worldCoords.y;

        float startY = 190f;
        float spacing = 55f;

        for (int i = 0; i < choices.size(); i++) {
            float optionY = startY - i * spacing;
            if (worldY >= optionY - spacing / 2f && worldY <= optionY + spacing / 2f) {
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
