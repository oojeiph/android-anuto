package ch.logixisland.anuto.engine.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.logixisland.anuto.Preferences;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.render.Renderer;

public class ThemeManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    public interface Listener {
        void themeChanged(Theme theme);
    }

    private final SharedPreferences mPreferences;
    private final Renderer mRenderer;

    private Theme mTheme;
    private final List<Theme> mAvailableThemes = new ArrayList<>();

    private final List<Listener> mListeners = new CopyOnWriteArrayList<>();

    public ThemeManager(Context context, Renderer renderer) {
        mRenderer = renderer;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        initThemes(context);
        updateTheme();
    }

    private void initThemes(Context context) {
        mAvailableThemes.add(new Theme(context, R.string.theme_original, R.style.OriginalTheme));
        mAvailableThemes.add(new Theme(context, R.string.theme_dark, R.style.DarkTheme));
        mAvailableThemes.add(new Theme(context, R.string.theme_colour, R.style.ColourTheme));
    }

    public Theme getTheme() {
        return mTheme;
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Preferences.THEME_INDEX.equals(key)) {
            updateTheme();
        }
    }

    private void updateTheme() {
        int themeIndex = Integer.parseInt(mPreferences.getString(Preferences.THEME_INDEX, "0"));
        Theme theme = mAvailableThemes.get(themeIndex);
        setTheme(theme);
    }

    private void setTheme(Theme theme) {
        if (mTheme != theme) {
            mTheme = theme;
            mRenderer.setBackgroundColor(mTheme.getColor(R.attr.backgroundColor));

            for (Listener listener : mListeners) {
                listener.themeChanged(theme);
            }
        }
    }
}
