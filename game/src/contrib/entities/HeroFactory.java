package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.health.Damage;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;

import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.Comparator;

/** A utility class for building the hero entity in the game world. */
public class HeroFactory {

    public static final int DEFAULT_INVENTORY_SIZE = 10;
    private static final String HERO_FILE_PATH = "character/wizard";
    private static final float X_SPEED_HERO = 7.5f;
    private static final float Y_SPEED_HERO = 7.5f;
    private static final int FIREBALL_COOL_DOWN = 500;
    private static final int HERO_HP = 100;

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * CameraComponent}, {@link PlayerComponent}. {@link PositionComponent}, {@link
     * VelocityComponent} {@link DrawComponent}, {@link CollideComponent} and {@link
     * HealthComponent}.
     *
     * @return Created Entity
     */
    public static Entity newHero() throws IOException {
        Entity hero = new Entity("hero");
        CameraComponent cc = new CameraComponent();
        hero.addComponent(cc);
        PositionComponent poc = new PositionComponent();
        hero.addComponent(poc);
        hero.addComponent(new VelocityComponent(X_SPEED_HERO, Y_SPEED_HERO));
        hero.addComponent(new DrawComponent(HERO_FILE_PATH));
        HealthComponent hc =
                new HealthComponent(
                        HERO_HP,
                        entity -> {
                            // play sound
                            Sound sound =
                                    Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
                            long soundId = sound.play();
                            sound.setLooping(soundId, false);
                            sound.setVolume(soundId, 0.3f);
                            sound.setLooping(soundId, false);
                            sound.play();
                            sound.setVolume(soundId, 0.9f);

                            // relink components for camera
                            Entity cameraDummy = new Entity();
                            cameraDummy.addComponent(cc);
                            cameraDummy.addComponent(poc);
                            Game.add(cameraDummy);
                        });
        hero.addComponent(hc);
        hero.addComponent(
                new CollideComponent(
                        (you, other, direction) ->
                                other.fetch(SpikyComponent.class)
                                        .ifPresent(
                                                spikyComponent -> {
                                                    if (spikyComponent.isActive()) {
                                                        hc.receiveHit(
                                                                new Damage(
                                                                        spikyComponent
                                                                                .damageAmount(),
                                                                        spikyComponent.damageType(),
                                                                        other));
                                                        spikyComponent.activateCoolDown();
                                                    }
                                                }),
                        (you, other, direction) -> {}));

        PlayerComponent pc = new PlayerComponent();
        hero.addComponent(pc);
        InventoryComponent ic = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
        hero.addComponent(ic);
        Skill fireball =
                new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

        // hero movement
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_UP.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));
                    vc.currentYVelocity(1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_DOWN.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentYVelocity(-1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_RIGHT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(1 * vc.xVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_LEFT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(-1 * vc.xVelocity());
                });

        pc.registerCallback(
                KeyboardConfig.INVENTORY_OPEN.value(),
                (e) -> {
                    UIComponent uiComponent = e.fetch(UIComponent.class).orElse(null);
                    if (uiComponent != null) {
                        if (uiComponent.dialog() instanceof GUICombination) {
                            InventoryGUI.inHeroInventory = false;
                            e.removeComponent(UIComponent.class);
                        }
                    } else {
                        InventoryGUI.inHeroInventory = true;
                        e.addComponent(
                                new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
                    }
                },
                false,
                false);

        pc.registerCallback(
                KeyboardConfig.CLOSE_UI.value(),
                (e) -> {
                    var firstUI =
                            Game.entityStream() // would be nice to directly access HudSystems
                                    // stream (no access to the System object)
                                    .filter(
                                            x ->
                                                    x.isPresent(
                                                            UIComponent.class)) // find all Entities
                                    // which have a
                                    // UIComponent
                                    .map(
                                            x ->
                                                    new Tuple<>(
                                                            x,
                                                            x.fetch(UIComponent.class)
                                                                    .orElseThrow(
                                                                            () ->
                                                                                    MissingComponentException
                                                                                            .build(
                                                                                                    x,
                                                                                                    UIComponent
                                                                                                            .class)))) // create a tuple to
                                    // still have access to
                                    // the UI Entity
                                    .filter(x -> x.b().closeOnUICloseKey())
                                    .max(
                                            Comparator.comparingInt(
                                                    x -> x.b().dialog().getZIndex())) // find dialog
                                    // with highest
                                    // z-Index
                                    .orElse(null);
                    if (firstUI != null) {
                        InventoryGUI.inHeroInventory = false;
                        firstUI.a().removeComponent(UIComponent.class);
                        if (firstUI.a().componentStream().findAny().isEmpty()) {
                            Game.remove(firstUI.a()); // delete unused Entity
                        }
                    }
                },
                false,
                false);

        pc.registerCallback(
                KeyboardConfig.INTERACT_WORLD.value(),
                InteractionTool::interactWithClosestInteractable,
                false);

        pc.registerCallback(
                KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
                hero1 -> {
                    // only interact with entities the cursor points at
                    Point mousePosition = SkillTools.cursorPositionAsPoint();
                    Tile mouseTile = Game.tileAT(mousePosition);
                    if (mouseTile == null) return; // mouse out of bound

                    Game.entityAtTile(mouseTile)
                            .filter(e -> e.isPresent(InteractionComponent.class))
                            .findFirst()
                            .ifPresent(
                                    interactable -> {
                                        InteractionComponent ic1 =
                                                interactable
                                                        .fetch(InteractionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        interactable,
                                                                                        InteractionComponent
                                                                                                .class));
                                        PositionComponent pc1 =
                                                interactable
                                                        .fetch(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        interactable,
                                                                                        PositionComponent
                                                                                                .class));
                                        PositionComponent heroPC =
                                                hero1.fetch(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        hero1,
                                                                                        PositionComponent
                                                                                                .class));
                                        if (Point.calculateDistance(
                                                        pc1.position(), heroPC.position())
                                                < ic1.radius())
                                            ic1.triggerInteraction(interactable, hero1);
                                    });
                },
                false);

        // skills
        pc.registerCallback(KeyboardConfig.FIRST_SKILL.value(), fireball::execute);

        return hero;
    }
}
