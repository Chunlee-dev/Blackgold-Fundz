package com.chunleedev.bgfundz;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroApp extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle("Register");
        sliderPage1.setDescription("Contact a coupon distributor from the coupon distributors page to get registered");
        sliderPage1.setImageDrawable(R.drawable.intro1);
        sliderPage1.setBgColor(getResources().getColor(R.color.colorPrimary));

        SliderPage sliderPage12 = new SliderPage();
        sliderPage12.setTitle("Login");
        sliderPage12.setDescription("Already have an account? Login and earn seamlessly");
        sliderPage12.setImageDrawable(R.drawable.intro2);
        sliderPage12.setBgColor(getResources().getColor(R.color.colorPrimary));

        SliderPage sliderPage13 = new SliderPage();
        sliderPage13.setTitle("Get Started");
        sliderPage13.setDescription("Get started with bgfundz activities and referrals to earn, money must be made 🤑");
        sliderPage13.setImageDrawable(R.drawable.intro3);
        sliderPage13.setBgColor(getResources().getColor(R.color.colorPrimary));


        addSlide(AppIntroFragment.newInstance(sliderPage1));
        addSlide(AppIntroFragment.newInstance(sliderPage12));
        addSlide(AppIntroFragment.newInstance(sliderPage13));

        setBarColor(getResources().getColor(R.color.colorPrimary));
        setSeparatorColor(getResources().getColor(android.R.color.transparent));

        showSkipButton(false);

        setFadeAnimation();

        setButtonState(null, true);

        setBackButtonVisibilityWithDone(true);

        setVibrate(true);
        setVibrateIntensity(50);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}
