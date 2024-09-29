package ch.logixisland.anuto.view;

import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

public class ApplySafeInsetsHandler implements View.OnApplyWindowInsetsListener {
    private final int mAdditionalPadding;

    public ApplySafeInsetsHandler() {
        this.mAdditionalPadding = 0;
    }

    public ApplySafeInsetsHandler(int mAdditionalPadding) {
        this.mAdditionalPadding = mAdditionalPadding;
    }

    @Override
    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT >= 30) {
            Insets systemBars = windowInsets.getInsets(WindowInsets.Type.systemBars());
            Insets displayCutout = windowInsets.getInsets(WindowInsets.Type.displayCutout());

            int top = Math.max(systemBars.top, displayCutout.top);
            int bottom = Math.max(systemBars.bottom, displayCutout.bottom);
            int left = Math.max(systemBars.left, displayCutout.left);
            int right = Math.max(systemBars.right, displayCutout.right);

            view.setPadding(
                    left + mAdditionalPadding,
                    top + mAdditionalPadding,
                    right + mAdditionalPadding,
                    bottom + mAdditionalPadding
            );
        }

        return windowInsets;
    }
}
