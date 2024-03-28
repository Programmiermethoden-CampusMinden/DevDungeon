package level.devlevel;

import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.BridgeGoblinRiddleHandler;
import level.utils.ITickable;
import utils.EntityUtils;

/** The Bridge Goblin Riddle Level */
public class BridgeGoblinRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 0;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;
  private static final int MOB_COUNT_PER_CAMP = 3;

  // Spawn Points / Locations
  private final Coordinate[] campSpawns;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final BridgeGoblinRiddleHandler riddleHandler;

  public BridgeGoblinRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new BridgeGoblinRiddleHandler(customPoints, this);
    this.campSpawns =
        new Coordinate[] {
          this.customPoints().get(13),
          this.customPoints().get(14),
          this.customPoints().get(15),
          this.customPoints().get(16),
          this.customPoints().get(17),
          this.customPoints().get(18),
          this.customPoints().get(19),
          this.customPoints().get(20),
          this.customPoints().get(21),
          this.customPoints().get(22),
          this.customPoints().get(23),
          this.customPoints().get(24),
        };

    Coordinate[] mobSpawns = new Coordinate[29];
    for (int i = 25; i < 54; i++) {
      mobSpawns[i - 25] = this.customPoints().get(i);
    }
    this.mobSpawns = mobSpawns;
    this.levelBossSpawn = this.customPoints().get(54);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
      this.handleFirstTick();
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {
    // Spawn all entities and it's content
    this.spawnCamps();
    this.spawnChestsAndCauldrons();

    // EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, this.mobSpawns, BOSS_TYPE,
    // this.levelBossSpawn);
  }

  private void spawnCamps() {
    for (Coordinate campSpawn : this.campSpawns) {
      EntityUtils.spawnMobSpawner(campSpawn, MONSTER_TYPES, MOB_COUNT_PER_CAMP);
    }
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {}
}
