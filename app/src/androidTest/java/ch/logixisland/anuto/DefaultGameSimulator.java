package ch.logixisland.anuto;

import java.util.List;
import java.util.Random;

import ch.logixisland.anuto.business.game.ScoreBoard;
import ch.logixisland.anuto.business.tower.TowerControl;
import ch.logixisland.anuto.business.tower.TowerInserter;
import ch.logixisland.anuto.business.tower.TowerSelector;
import ch.logixisland.anuto.business.wave.WaveManager;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.engine.logic.map.MapPath;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.plateau.Plateau;
import ch.logixisland.anuto.entity.tower.Tower;
import ch.logixisland.anuto.entity.tower.TowerStrategy;
import ch.logixisland.anuto.util.iterator.Function;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Intersections;
import ch.logixisland.anuto.util.math.Line;
import ch.logixisland.anuto.util.math.Vector2;

public class DefaultGameSimulator extends GameSimulator {

    private static final int TARGET_TOWER_COUNT_PER_TYPE_AND_TIER = 2;

    private final TowerTiers mTowerTiers;
    private final Random mRandom = new Random();
    private final TickTimer mAutoSaveAndLoadTimer = TickTimer.createInterval(300f);
    private final TickTimer mSaveAndLoadTimer = TickTimer.createInterval(120f);
    private final TickTimer mSimulationTickTimer = TickTimer.createInterval(2f);

    private int mSaveAndLoadState = 0;
    private boolean mAllTowersUpgraded = false;

    DefaultGameSimulator(GameFactory gameFactory) {
        super(gameFactory);
        mTowerTiers = new TowerTiers(gameFactory);
    }

    @Override
    protected void tick() {
        if (mSimulationTickTimer.tick()) {
            tryUpgradeTower();
            tryEnhanceTower();
            tryBuildTower();
            tryStartNextWave();
        }

        if (mAutoSaveAndLoadTimer.tick()) {
            autoSaveAndLoad();
        }

        if (mSaveAndLoadTimer.tick()) {
            switch (++mSaveAndLoadState) {
                case 1:
                    saveGame();
                    break;
                case 2:
                    loadGame();
                    break;
                case 3:
                    deleteSaveGame();
                    break;
                case 10:
                    mSaveAndLoadState = 0;
                    break;
            }
        }
    }

    private void tryUpgradeTower() {
        final ScoreBoard scoreBoard = getGameFactory().getScoreBoard();
        final TowerSelector towerSelector = getGameFactory().getTowerSelector();
        final TowerControl towerControl = getGameFactory().getTowerControl();

        mAllTowersUpgraded = true;
        StreamIterator<Tower> iterator = getTowers();

        while (iterator.hasNext()) {
            Tower tower = iterator.next();

            // check if upgrade is possible
            if (!tower.isUpgradeable()) {
                continue;
            }

            mAllTowersUpgraded = false;

            // check if upgrade is affordable
            if (tower.getUpgradeCost() > scoreBoard.getCredits()) {
                continue;
            }

            // check if tower was already enhanced
            if (tower.getLevel() > 1) {
                continue;
            }

            // check if next tier already reached target tower count
            int upgradedTowerCount = getTowers()
                    .filter(t -> tower.getUpgradeName().equals(t.getEntityName()))
                    .count();

            if (upgradedTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER) {
                continue;
            }

            Vector2 position = tower.getPosition();

            towerSelector.selectTower(tower);
            towerControl.upgradeTower();

            randomizeTowerStrategy(position);
        }
    }

    private void tryEnhanceTower() {
        final TowerSelector towerSelector = getGameFactory().getTowerSelector();
        final TowerControl towerControl = getGameFactory().getTowerControl();
        final ScoreBoard scoreBoard = getGameFactory().getScoreBoard();

        StreamIterator<Tower> iterator = getTowers();

        while (iterator.hasNext()) {
            Tower tower = iterator.next();

            // check if enhancing is possible
            if (!tower.isEnhanceable()) {
                continue;
            }

            // check if enhancing is affordable
            if (tower.getEnhanceCost() > scoreBoard.getCredits()) {
                continue;
            }

            // keep one tower un-enhanced for upgrading
            int enhancedTowerCount = getTowers()
                    .filter(t -> tower.getEntityName().equals(t.getEntityName()))
                    .filter(t -> t.getLevel() > 1)
                    .count();

            if (tower.isUpgradeable() && enhancedTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER - 1) {
                continue;
            }

            // prioritize upgrading before enhancing the last tier
            if (!tower.isUpgradeable() && !mAllTowersUpgraded) {
                continue;
            }

            towerSelector.selectTower(tower);
            towerControl.enhanceTower();
            return;
        }
    }

    private void tryBuildTower() {
        final TowerInserter towerInserter = getGameFactory().getTowerInserter();
        final ScoreBoard scoreBoard = getGameFactory().getScoreBoard();

        StreamIterator<Tower> iterator = mTowerTiers.getBuildableTowers();

        while (iterator.hasNext()) {
            Tower tower = iterator.next();

            // check if building is affordable
            if (tower.getValue() > scoreBoard.getCredits()) {
                continue;
            }

            // don't exceed target tower count
            int builtTowerCount = getTowers()
                    .filter(t -> tower.getEntityName().equals(t.getEntityName()))
                    .count();

            if (builtTowerCount >= TARGET_TOWER_COUNT_PER_TYPE_AND_TIER) {
                continue;
            }

            Plateau selectedPlateau = findTowerPlateau(tower);

            if (selectedPlateau == null) {
                return;
            }

            towerInserter.insertTower(tower.getEntityName());
            towerInserter.setPosition(selectedPlateau.getPosition());
            towerInserter.buyTower();

            randomizeTowerStrategy(selectedPlateau.getPosition());
        }
    }

    private void randomizeTowerStrategy(Vector2 position) {
        final TowerSelector towerSelector = getGameFactory().getTowerSelector();
        final TowerControl towerControl = getGameFactory().getTowerControl();

        boolean toggleLock = mRandom.nextBoolean();
        int cycleStrategyCount = mRandom.nextInt(TowerStrategy.values().length);

        towerSelector.selectTowerAt(position);

        if (toggleLock) {
            towerControl.toggleLockTarget();
        }

        for (int i = 0; i < cycleStrategyCount; i++) {
            towerControl.cycleTowerStrategy();
        }
    }

    private Plateau findTowerPlateau(Tower tower) {
        final List<MapPath> paths = getGameFactory().getGameEngine().getGameMap().getPaths();
        final float range = tower.getRange();

        final Function<Plateau, Float> distanceCovered = plateau -> {
            float distanceCovered1 = 0;

            for (MapPath path : paths) {
                for (Line line : Intersections.getPathSectionsInRange(path.getWayPoints(), plateau.getPosition(), range)) {
                    distanceCovered1 += line.length();
                }
            }

            return distanceCovered1;
        };

        final Float maxDistanceCovered = getFreePlateaus()
                .map(distanceCovered)
                .max(input -> input);

        if (maxDistanceCovered == null) {
            return null;
        }

        return getFreePlateaus()
                .filter(value -> distanceCovered.apply(value) > maxDistanceCovered * 0.8f)
                .random(mRandom);
    }

    private void tryStartNextWave() {
        final WaveManager waveManager = getGameFactory().getWaveManager();

        if (waveManager.isNextWaveReady() && waveManager.getRemainingEnemiesCount() < 50) {
            waveManager.startNextWave();
        }
    }

    private StreamIterator<Tower> getTowers() {
        final GameEngine gameEngine = getGameFactory().getGameEngine();

        return gameEngine.getEntitiesByType(EntityTypes.TOWER)
                .cast(Tower.class);
    }

    private StreamIterator<Plateau> getFreePlateaus() {
        final GameEngine gameEngine = getGameFactory().getGameEngine();

        return gameEngine.getEntitiesByType(EntityTypes.PLATEAU)
                .cast(Plateau.class)
                .filter(Plateau.unoccupied());
    }

}
