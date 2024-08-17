package ch.logixisland.anuto.util;

import android.content.res.Resources;

import java.text.DecimalFormat;

import ch.logixisland.anuto.R;

public final class StringUtils {
    private StringUtils() {

    }

    private static final DecimalFormat fmt0 = new DecimalFormat("0");
    private static final DecimalFormat fmt1 = new DecimalFormat("0.0");

    public static String formatSuffix(int value) {
        return formatSuffix(value, true);
    }

    public static String formatSuffix(float value) {
        return formatSuffix(value, false);
    }

    public static String formatSuffix(float value, boolean integer) {
        String suffix = "";
        boolean big = false;

        if (value >= 1e10f) {
            suffix = "G";
            value /= 1e9f;
            big = true;
        } else if (value >= 1e7f) {
            suffix = "M";
            value /= 1e6f;
            big = true;
        } else if (value >= 1e4f) {
            suffix = "k";
            value /= 1e3f;
            big = true;
        }

        DecimalFormat fmt = (value < 1e2f && (!integer || big)) ? fmt1 : fmt0;
        return fmt.format(value) + suffix;
    }

    public static String formatBoolean(boolean value, Resources resources) {
        return resources.getString(value ? R.string.on : R.string.off);
    }

    public static String formatSwitchButton(String name, String value) {
        return String.format("%1$s (%2$s)", name, value);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
