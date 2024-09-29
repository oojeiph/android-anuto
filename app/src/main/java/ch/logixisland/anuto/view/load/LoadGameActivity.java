package ch.logixisland.anuto.view.load;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.GridView;

import ch.logixisland.anuto.AnutoApplication;
import ch.logixisland.anuto.GameFactory;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.business.game.GameLoader;
import ch.logixisland.anuto.business.game.SaveGameInfo;
import ch.logixisland.anuto.business.game.SaveGameRepository;
import ch.logixisland.anuto.view.AnutoActivity;
import ch.logixisland.anuto.view.ApplySafeInsetsHandler;

public class LoadGameActivity extends AnutoActivity implements AdapterView.OnItemClickListener {

    public static final int CONTEXT_MENU_DELETE_ID = 0;

    private final GameLoader mGameLoader;
    private final SaveGameRepository mSaveGameRepository;

    private SaveGamesAdapter mAdapter;

    public LoadGameActivity() {
        GameFactory factory = AnutoApplication.getInstance().getGameFactory();
        mGameLoader = factory.getGameLoader();
        mSaveGameRepository = factory.getSaveGameRepository();
    }

    @Override
    protected ch.logixisland.anuto.engine.theme.ActivityType getActivityType() {
        return ch.logixisland.anuto.engine.theme.ActivityType.Normal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load_menu);

        mAdapter = new SaveGamesAdapter(this, mSaveGameRepository);

        GridView grid_savegames = findViewById(R.id.grid_savegames);
        grid_savegames.setOnItemClickListener(this);
        grid_savegames.setAdapter(mAdapter);
        registerForContextMenu(grid_savegames);

        int additionalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        grid_savegames.setOnApplyWindowInsetsListener(new ApplySafeInsetsHandler(additionalPadding));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SaveGameInfo saveGameInfo = mAdapter.getItem(position);
        mGameLoader.loadGame(mSaveGameRepository.getGameStateFile(saveGameInfo));

        finish();
    }

    public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CONTEXT_MENU_DELETE_ID) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            SaveGameInfo saveGameInfo = mSaveGameRepository.getSaveGameInfos().get(info.position);
            mSaveGameRepository.deleteSaveGame(saveGameInfo);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }
}
