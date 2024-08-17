package ch.logixisland.anuto.engine.logic.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.logixisland.anuto.util.container.KeyValueStore;

public class GameMap {

    private final int mWidth;
    private final int mHeight;
    private final List<PlateauInfo> mPlateaus = new ArrayList<>();
    private final List<MapPath> mPaths = new ArrayList<>();

    public GameMap(KeyValueStore data) {
        mWidth = data.getInt("width");
        mHeight = data.getInt("height");

        for (KeyValueStore plateauData : data.getStoreList("plateaus")) {
            mPlateaus.add(new PlateauInfo(plateauData));
        }

        for (KeyValueStore pathData : data.getStoreList("paths")) {
            mPaths.add(new MapPath(pathData.getVectorList("wayPoints")));
        }
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public Collection<PlateauInfo> getPlateaus() {
        return mPlateaus;
    }

    public List<MapPath> getPaths() {
        return mPaths;
    }
}
