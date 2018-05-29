package com.shakuro.firebaseproject.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.shakuro.firebaseproject.FirebaseApplication;

public class ScreenUtils {
    private static final float BASELINE_DENSITY = 160f;

    public static int convertPixelsToDp(float px){
        Resources resources = FirebaseApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = Math.round(px / (metrics.densityDpi / BASELINE_DENSITY));
        return dp;
    }

    public static int convertDpToPixels(float dp){
        Resources resources = FirebaseApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = Math.round(dp * (metrics.densityDpi / BASELINE_DENSITY));
        return px;
    }

    @SuppressWarnings("deprecation")
    public static Point getScreenSize() {
        Point result = new Point();
        WindowManager windowManager = (WindowManager) FirebaseApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(result);
        } else {
            result.set(display.getWidth(), display.getHeight());
        }

        return result;
    }

    public static int getScreenWidth() {
        return getScreenSize().x;
    }

    public static int getScreenHeight() {
        return getScreenSize().y;
    }
}
