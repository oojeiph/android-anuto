package ch.logixisland.anuto.view.map;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import ch.logixisland.anuto.AnutoApplication;
import ch.logixisland.anuto.GameFactory;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.business.game.GameLoader;
import ch.logixisland.anuto.business.game.HighScores;
import ch.logixisland.anuto.business.game.MapRepository;
import ch.logixisland.anuto.engine.theme.ActivityType;
import ch.logixisland.anuto.view.AnutoActivity;
import ch.logixisland.anuto.view.ApplySafeInsetsHandler;

public class ChangeMapActivity extends AnutoActivity implements AdapterView.OnItemClickListener {

    private final GameLoader mGameLoader;
    private final MapRepository mMapRepository;
    private final HighScores mHighScores;

    public ChangeMapActivity() {
        GameFactory factory = AnutoApplication.getInstance().getGameFactory();
        mGameLoader = factory.getGameLoader();
        mMapRepository = factory.getMapRepository();
        mHighScores = factory.getHighScores();
    }

    @Override
    protected ActivityType getActivityType() {
        return ActivityType.Normal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_map);

        MapsAdapter mAdapter = new MapsAdapter(this, mMapRepository, mHighScores);

        GridView grid_maps = findViewById(R.id.grid_maps);
        grid_maps.setOnItemClickListener(this);
        grid_maps.setAdapter(mAdapter);

        int additionalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        grid_maps.setOnApplyWindowInsetsListener(new ApplySafeInsetsHandler(additionalPadding));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mGameLoader.loadMap(mMapRepository.getMapInfos().get(position).getMapId());
        finish();
    }
}
