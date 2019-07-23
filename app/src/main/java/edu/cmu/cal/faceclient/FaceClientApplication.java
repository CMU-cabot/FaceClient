package edu.cmu.cal.faceclient;

import com.vuzix.hud.resources.DynamicThemeApplication;

public class FaceClientApplication extends DynamicThemeApplication {
    @Override
    protected int getNormalThemeResId() {
        return R.style.AppTheme;
    }

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme_Light;
    }
}
