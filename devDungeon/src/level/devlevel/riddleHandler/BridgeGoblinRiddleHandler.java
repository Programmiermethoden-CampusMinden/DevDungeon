package level.devlevel.riddleHandler;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.DialogFactory;
import item.concreteItem.ItemPotionSpeedPotion;
import java.util.ArrayList;
import java.util.List;
import level.utils.ITickable;
import task.game.hud.QuizUI;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;

public class BridgeGoblinRiddleHandler implements ITickable {

  private static final int RIDDLE_REWARD = 5;
  private final TileLevel level;
  private final List<Quiz> riddles = new ArrayList<>();
  private boolean rewardGiven = false;
  private Coordinate riddleRewardSpawn = new Coordinate(0, 0);

  public BridgeGoblinRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
    this.level = level;

    this.riddles.add(new SingleChoice("What is the answer to life, the universe and everything?"));
    this.riddles.get(0).taskName("Bridge Goblin Riddle");
    this.riddles.get(0).addAnswer(new Quiz.Content("42", null));
    this.riddles.get(0).addAnswer(new Quiz.Content("43", null));
    this.riddles.get(0).addAnswer(new Quiz.Content("44", null));
    this.riddles.get(0).addAnswer(new Quiz.Content("45", null));
    this.riddles.get(0).addCorrectAnswerIndex(0);

    this.riddles.add(new SingleChoice("What is better than 42?"));
    this.riddles.get(1).taskName("Bridge Goblin Riddle");
    this.riddles.get(1).addAnswer(new Quiz.Content("42", null));
    this.riddles.get(1).addAnswer(new Quiz.Content("43", null));
    this.riddles.get(1).addAnswer(new Quiz.Content("44", null));
    this.riddles.get(1).addAnswer(new Quiz.Content("45", null));
    this.riddles.get(1).addCorrectAnswerIndex(1);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      Entity t =
          DialogFactory.createBridgeGoblin(
              this.riddles.subList(0, this.riddles.size() - 1),
              () -> {
                QuizUI.showQuizDialog(
                    this.riddles.getLast(),
                    (Entity hudEntity) ->
                        UIAnswerCallback.uiCallback(
                            this.riddles.getLast(),
                            hudEntity,
                            (task, taskContents) -> {
                              task.gradeTask(taskContents);
                              String output = "You have incorrectly solved the task";

                              OkDialog.showOkDialog(output, "Result", () -> {});
                            }));
              });
      t.add(new PositionComponent(new Point(24, 58)));
      Game.add(t);
      this.handleFirstTick();
    }
  }

  private void giveReward() {
    DialogFactory.showTextPopup(
        "You will receive "
            + RIDDLE_REWARD
            + " additional maximum health points \nas a reward for solving this puzzle!",
        "Riddle solved");
    Game.hero()
        .flatMap(hero -> hero.fetch(HealthComponent.class))
        .ifPresent(
            hc -> {
              hc.maximalHealthpoints(hc.maximalHealthpoints() + RIDDLE_REWARD);
              hc.receiveHit(new Damage(-RIDDLE_REWARD, DamageType.HEAL, null));
              this.rewardGiven = true;
            });
  }

  private void handleFirstTick() {
    this.spawnSigns();
    this.spawnChest();
    this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22FF22FF);
  }

  private void spawnSigns() {}

  private void spawnChest() {
    Entity speedPotionChest;
    try {
      speedPotionChest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create chest");
    }
    PositionComponent pc =
        speedPotionChest
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, PositionComponent.class));

    pc.position();

    InventoryComponent ic =
        speedPotionChest
            .fetch(InventoryComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(speedPotionChest, InventoryComponent.class));
    ic.add(new ItemPotionSpeedPotion());
    Game.add(speedPotionChest);
  }
}
