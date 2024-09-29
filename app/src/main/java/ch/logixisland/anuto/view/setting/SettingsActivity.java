package ch.logixisland.anuto.view.setting;

import android.os.Bundle;

import ch.logixisland.anuto.engine.theme.ActivityType;
import ch.logixisland.anuto.view.AnutoActivity;
import ch.logixisland.anuto.view.ApplySafeInsetsHandler;

public class SettingsActivity extends AnutoActivity {
    @Override
    protected ActivityType getActivityType() {
        return ActivityType.Normal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        findViewById(android.R.id.content).setOnApplyWindowInsetsListener(new ApplySafeInsetsHandler());
    }
}
