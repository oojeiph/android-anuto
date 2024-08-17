package ch.logixisland.anuto.view.load;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import ch.logixisland.anuto.AnutoApplication;
import ch.logixisland.anuto.GameFactory;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.business.game.GameLoader;
import ch.logixisland.anuto.business.game.SaveGameInfo;
import ch.logixisland.anuto.business.game.SaveGameRepository;
import ch.logixisland.anuto.view.AnutoActivity;

public class LoadGameActivity extends AnutoActivity implements AdapterView.OnItemClickListener,
        ViewTreeObserver.OnScrollChangedListener {

    public static final int CONTEXT_MENU_DELETE_ID = 0;

    private final GameLoader mGameLoader;
    private final SaveGameRepository mSaveGameRepository;

    private SaveGamesAdapter mAdapter;

    private ImageView arrow_up;
    private ImageView arrow_down;
    private GridView grid_savegames;


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

        arrow_up = findViewById(R.id.arrow_up);
        arrow_down = findViewById(R.id.arrow_down);

        grid_savegames = findViewById(R.id.grid_savegames);
        grid_savegames.setOnItemClickListener(this);
        grid_savegames.getViewTreeObserver().addOnScrollChangedListener(this);
        grid_savegames.post(this::updateArrowVisibility);
        grid_savegames.setAdapter(mAdapter);
        registerForContextMenu(grid_savegames);
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

    @Override
    public void onScrollChanged() {
        updateArrowVisibility();
    }

    private void updateArrowVisibility() {
        if (grid_savegames.getChildCount() <= 0) {
            arrow_up.setVisibility(View.INVISIBLE);
            arrow_down.setVisibility(View.INVISIBLE);
            return;
        }

        if (grid_savegames.getFirstVisiblePosition() == 0) {
            arrow_up.setVisibility(grid_savegames.getChildAt(0).getTop() < -10 ? View.VISIBLE : View.INVISIBLE);
        } else {
            arrow_up.setVisibility(grid_savegames.getFirstVisiblePosition() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        if (grid_savegames.getLastVisiblePosition() == mAdapter.getCount() - 1) {
            arrow_down.setVisibility(grid_savegames.getChildAt(grid_savegames.getChildCount() - 1).getBottom() > grid_savegames.getHeight() + 10 ? View.VISIBLE : View.INVISIBLE);
        } else {
            arrow_down.setVisibility(grid_savegames.getLastVisiblePosition() < mAdapter.getCount() - 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
