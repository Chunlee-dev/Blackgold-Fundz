package com.chunleedev.bgfundz;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

@SuppressWarnings("setjavascriptenabled")
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button retry;
    WebView webView;
    TextView showUrl;
    ProgressBar progressBar;
    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    LinearLayout linearNetwork;
    ActionBarDrawerToggle toggle;
    ProgressDialog progressDialog;
    NavigationView navigationView;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean isError, isError1, reload = false;
    boolean isAboveMiddle = true;
    private float lastTranslate = 0.0f;
    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_layout);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroApp.class);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            startActivity(i);
                        }
                    });
                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);
                    //  Apply changes
                    e.apply();
                }
            }
        });
        t.start();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        showUrl = findViewById(R.id.showUrl);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.floatingActionButton);
        relativeLayout = findViewById(R.id.mainLayout);

        drawerLayout.setDrawerElevation(0);
        drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
        drawerLayout.setDrawerShadow(null, GravityCompat.START);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer){

            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                super.onDrawerSlide(drawerView, slideOffset);
                float moveFactor = (navigationView.getWidth() * (slideOffset/2));


                relativeLayout.setTranslationX(moveFactor);

                TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                anim.setDuration(0);
                anim.setFillAfter(true);
                relativeLayout.startAnimation(anim);

                lastTranslate = moveFactor;

            }
        };

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        webView = findViewById(R.id.webView);

        retry = findViewById(R.id.retryBtn);
        linearNetwork = findViewById(R.id.linear);

        retry.setVisibility(View.INVISIBLE);
        linearNetwork.setVisibility(View.INVISIBLE);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
                linearNetwork.setVisibility(View.GONE);
                retry.setVisibility(View.GONE);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

        Window window = MainActivity.this.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(MainActivity.this.getResources().getColor(R.color.colorPrimaryDark));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {

                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int height = (int) Math.floor(webView.getContentHeight() * webView.getScale());
                    int webViewHeight = webView.getMeasuredHeight();
                    if (webView.getScrollY() + webViewHeight >= (height / 2)) {
                        if (isAboveMiddle) {
                            fab.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
                            isAboveMiddle = false;
                        }
                    } else {
                        if (!isAboveMiddle) {
                            fab.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                            isAboveMiddle = true;
                        }
                    }

                }

            });

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fab.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isAboveMiddle) {
                    webView.pageUp(true);
                } else {
                    webView.pageDown(true);
                }

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Clearing cookies and cache");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .showEvery(1)
                .setTitleOnUpdateAvailable("Update available")
                .setContentOnUpdateAvailable("Update now to get up to date features and information")
                .setButtonUpdate("Update")
                .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    }
                })
	            .setButtonDismiss("Maybe later")
                .setButtonDismissClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
	            .setButtonDoNotShowAgain("")
                .setButtonDoNotShowAgainClickListener(null)
                .setCancelable(false)
                .start();

        WebView.setWebContentsDebuggingEnabled(false);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            webView.reload();
        } else {
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.getSettings().setAppCacheEnabled(true);
            webView.setBackgroundColor(Color.WHITE);
            webView.setLongClickable(true);
            webView.requestFocus();
            webView.getSettings().setSaveFormData(true);
            webView.setHapticFeedbackEnabled(true);
            webView.setWebViewClient(new MyWebViewClient());

            CookieSyncManager.createInstance(this);
            CookieManager.getInstance().setAcceptCookie(true);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    progressBar.setProgress(newProgress);
                    if (newProgress < 100 && (progressBar.getVisibility() == ProgressBar.GONE)) {
                        progressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                    if (newProgress == 100) {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }
                }
            });

            webView.loadUrl("https://www.bgfundz.com");

            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    WebView webView = (WebView) v;
                    WebView.HitTestResult result = webView.getHitTestResult();

                    if (result != null) {
                        if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                            String linkToCopy = result.getExtra();
                            if (linkToCopy != null) {
                                if (linkToCopy.endsWith("2020/") || linkToCopy.endsWith("2021/")) {
                                    shareAppLinkViaFacebook(linkToCopy);
                                }
                            }
                        }
                    }

                    return true;
                }
            });

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                    linearNetwork.setVisibility(View.GONE);
                    retry.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 1500);
                }
            });
        } // end of else
    }//onCreate ends here

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        String loadUrl;
        final String websiteUrl = "https://www.bgfundz.com/";
        switch (id) {
            case R.id.nav_home:
                loadUrl = "https://www.bgfundz.com/";
                break;
            case R.id.account:
                loadUrl = websiteUrl + "affiliate-area/";
                break;
            case R.id.register:
                loadUrl = websiteUrl + "checkout/?add-to-cart=11";
                break;
            case R.id.howitworks:
                loadUrl = websiteUrl + "how-it-works/";
                break;
            case R.id.coupon:
                loadUrl = websiteUrl + "coupon-distributors/";
                break;
            case R.id.advertise:
                loadUrl = websiteUrl + "advertise-with-us/";
                break;
            case R.id.top5earners:
                loadUrl = websiteUrl + "top-5-earners/";
                break;
            case R.id.terms:
                loadUrl = websiteUrl + "terms-and-conditions/";
                break;
            case R.id.support:
                loadUrl = websiteUrl + "support/";
                break;
            case R.id.about:
                loadUrl = websiteUrl + "about-us/";
                break;
            case R.id.sponsored:
                loadUrl = websiteUrl + "sponsored/";
                break;
            case R.id.withdrawal:
                loadUrl = "https://docs.google.com/forms/d/1IlBLzE0f2hnz3in9Ox3JpviQRDZPOanJ7Azn9fJTUnM/edit";
                break;
            case R.id.logout:
                loadUrl = "https://www.bgfundz.com/bg-login-2/?action=logout&redirect_to=https%3A%2F%2Fwww.bgfundz.com%2Faffiliate-area%2F&_wpnonce=87abf12316";
                break;
            case R.id.entertain:
                loadUrl = websiteUrl + "category/entertainment/";
                break;
            case R.id.educate:
                loadUrl = websiteUrl + "category/education/";
                break;
            case R.id.lifestyle:
                loadUrl = websiteUrl + "category/lifestyle/";
                break;
            case R.id.finance:
                loadUrl = websiteUrl + "category/finance/";
                break;
            case R.id.politics:
                loadUrl = websiteUrl + "category/politics/";
                break;
            case R.id.sports:
                loadUrl = websiteUrl + "category/sports/";
                break;
            case R.id.affiliateinfo:
                loadUrl = websiteUrl + "affiliate-area/?tab=urls";
                break;
            case R.id.edit:
                loadUrl = websiteUrl + "affiliate-settings/";
                break;
            case R.id.paidreferrals:
                loadUrl = websiteUrl + "affiliate-area/?tab=payouts";
                break;
            case R.id.referrals:
                loadUrl = websiteUrl + "affiliate-area/?tab=referrals";
                break;
            case R.id.visits:
                loadUrl = websiteUrl + "affiliate-area/?tab=visits";
                break;
            case R.id.settings:
                loadUrl = websiteUrl + "affiliate-area/?tab=settings";
                break;
            case R.id.facebook:
                loadUrl = "https://www.facebook.com/blackgoldfundz";
                break;
            case R.id.twitter:
                loadUrl = "https://twitter.com/bgfundz/";
                break;
            case R.id.instagram:
                loadUrl = "https://instagram.com/bgfundz_?igshid=1aatr6qmu22ue";
                break;
            case R.id.telegram:
                loadUrl = "https://t.me/BLACKGOLDFC";
                break;
            default:
                loadUrl = websiteUrl;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        if (loadUrl.startsWith("https://www.bgfundz.com/bg-login-2/?action=logout&redirect_to=https%3A%2F%2Fwww.bgfundz.com%2Faffiliate-area")) {

            webView.loadUrl(loadUrl);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    webView.clearHistory();
                }
            }, 4000);

        } else if (loadUrl.startsWith("https://www.facebook.com/blackgo")) {

            startActivity(newFacebookIntent(getPackageManager(), "blackgoldfundz"));

        } else if (loadUrl.startsWith("https://t.me")) {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(loadUrl)));

        } else {

            webView.loadUrl(loadUrl);

        }

        linearNetwork.setVisibility(View.INVISIBLE);
        retry.setVisibility(View.INVISIBLE);

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {

        super.onSaveInstanceState(outState, outPersistentState);
        webView.saveState(outState);

    }

    public void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("less than lollipop", "Using clearCookies code for API >=" + (Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d("higher than lollipop", "Using clearCookies code for API <" + (Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public void clearcatche(WebView webView) {
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();
        WebStorage.getInstance().deleteAllData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.Cookies) {
            clearCookies(MainActivity.this);
            clearcatche(webView);
            webView.reload();
            progressDialog.show();
        } else if (id == R.id.nav_rate) {
            rate();
        } else if (id == R.id.facebookItem) {
            sendToTwitter();
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendToTwitter() {
        Intent send = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"));
        startActivity(send);
    }

    public void createDialog() {
        if (!reload) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Clear Cookies")
                    .setMessage("Clear cookies and cache for effective registration")
                    .setNeutralButton("CANCEL", null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearCookies(MainActivity.this);
                            clearcatche(webView);
                            reload = true;
                            webView.reload();
                            progressDialog.show();
                        }
                    }).setCancelable(false).create().show();
        }

    }

    public void share() {
        String subTitle = "Download the BgFundz app at " + "https://play.google.com/store/apps/details?id=" + getPackageName();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, subTitle);
        startActivity(Intent.createChooser(share, "Share via"));
    }

    public void rate() {
        String marketUrl = "market://details?id=" + getPackageName();
        String playstoreUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(playstoreUrl)));
        }
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else if (webView.getUrl().startsWith("https://www.bgfundz.com/checkout") && !reload) {
            webView.loadUrl("https://www.bgfundz.com");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String facebookUrl = webView.getUrl();
        if (facebookUrl.endsWith("2020/") || facebookUrl.endsWith("2021/")) {
            Snackbar.make(webView, "Now click 'View portfolio' to get credited.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void shareAppLinkViaFacebook(String urlToShare) {
        try {
            Intent intent1 = new Intent();
            intent1.setPackage("com.facebook.katana");
            intent1.setAction("android.intent.action.SEND");
            intent1.setType("text/plain");
            intent1.putExtra("android.intent.extra.TEXT", urlToShare);
            startActivity(intent1);
        } catch (Exception e) { // If we failed (not native FB app installed), try share through SEND
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
            startActivity(intent);
        }
    }

    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            uri = Uri.parse("https://www.facebook.com/blackgoldfundz");
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    private boolean checkError(String error) {

        if (TextUtils.isEmpty(error)) {
            return false;
        } else if (!TextUtils.isEmpty(error)) {
            return true;
        }
        return true;
    }

    private boolean checkError1(String error1) {

        if (TextUtils.isEmpty(error1)) {
            return false;
        } else if (!TextUtils.isEmpty(error1)) {
            return true;
        }
        return true;
    }

    public class MyWebViewClient extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            webView.setVisibility(View.INVISIBLE);
            fab.hide();
            linearNetwork.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
            isError = checkError(error.toString());
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            webView.setVisibility(View.INVISIBLE);
            fab.hide();
            linearNetwork.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
            isError1 = checkError1(errorResponse.toString());
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && (url.startsWith("https://www.bgfundz.com") || url.startsWith("https://docs.google") || url.contains("payst"))) {
                return false;
            } else {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (url.startsWith("https://www.bgfundz.com/bg-login-2/?action=logout&redirect_to=https%3A%2F%2Fwww.bgfundz.com%2Faffiliate-area")) {
                linearNetwork.setVisibility(View.INVISIBLE);
                retry.setVisibility(View.INVISIBLE);
            }

            if (!isError || !isError1) {
                webView.setVisibility(View.VISIBLE);
                fab.show();
            }

            if (url.startsWith("https://www.bgfundz.com/checkout")) {
                createDialog();
            }

            final String homeUrl = "https://www.bgfundz.com/";
            if (url.contentEquals(homeUrl)) {
                showUrl.setText("Home");
            } else if (url.contentEquals(homeUrl + "affiliate-area/")) {
                showUrl.setText("My Account");
            } else if (url.contentEquals(homeUrl + "checkout/?add-to-cart=11")) {
                showUrl.setText("Register");
            } else if (url.startsWith(homeUrl + "checkout/?add-to-cart=11")) {
                showUrl.setText("Referral Link Registration");
            } else if (url.contentEquals(homeUrl + "how-it-works/")) {
                showUrl.setText("How It Works");
            } else if (url.contentEquals(homeUrl + "coupon-distributors/")) {
                showUrl.setText("Coupon Distributors");
            } else if (url.contentEquals(homeUrl + "advertise-with-us/")) {
                showUrl.setText("Advertise With Us");
            } else if (url.contentEquals(homeUrl + "top-5-earners/")) {
                showUrl.setText("Top 5 Earners");
            } else if (url.contentEquals(homeUrl + "terms-and-conditions/")) {
                showUrl.setText("Terms and Conditions");
            } else if (url.contentEquals(homeUrl + "support/")) {
                showUrl.setText("Support");
            } else if (url.contentEquals(homeUrl + "about-us/")) {
                showUrl.setText("About Us");
            } else if (url.startsWith(homeUrl + "sponsored/")) {
                showUrl.setText("Sponsored Posts");
            } else if (url.contentEquals("https://www.bgfundz.com/submit-posts/")) {
                showUrl.setText("Submit Posts");
            } else if (url.startsWith("https://docs.google.com/forms/")) {
                showUrl.setText("Withdrawal");
            } else if (url.startsWith("https://www.bgfundz.com/bg-login-2/?action=logout&redirect_to=https%3A%2F%2Fwww.bgfundz.com%2Faffiliate-area%2F&_wpnonce")) {
                showUrl.setText("Logout");
            } else if (url.contentEquals(homeUrl + "category/entertainment/")) {
                showUrl.setText(getString(R.string.entertainment));
            } else if (url.contentEquals(homeUrl + "category/education/")) {
                showUrl.setText("Education");
            } else if (url.contentEquals(homeUrl + "category/lifestyle/")) {
                showUrl.setText("Lifestyle");
            } else if (url.contentEquals(homeUrl + "category/finance/")) {
                showUrl.setText("Finance");
            } else if (url.contentEquals(homeUrl + "category/politics/")) {
                showUrl.setText("Politics");
            } else if (url.contentEquals(homeUrl + "category/sports/")) {
                showUrl.setText("Sports");
            } else if (url.contentEquals(homeUrl + "affiliate-area/?tab=urls")) {
                showUrl.setText("Affiliate Info");
            } else if (url.contentEquals(homeUrl + "affiliate-settings/")) {
                showUrl.setText("Edit profile");
            } else if (url.contentEquals(homeUrl + "affiliate-area/?tab=payouts")) {
                showUrl.setText("Paid referrals");
            } else if (url.contentEquals(homeUrl + "affiliate-area/?tab=referrals")) {
                showUrl.setText("Referrals");
            } else if (url.contentEquals(homeUrl + "affiliate-area/?tab=visits")) {
                showUrl.setText("Visits");
            } else if (url.contentEquals(homeUrl + "affiliate-area/?tab=settings")) {
                showUrl.setText("Settings");
            } else if (url.length() > 55) {
                showUrl.setText("News");
            } else {
                showUrl.setText("Bgfundz");
            }

            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);

            if (url.startsWith("https://www.bgfundz.com/bg-login-2/?action=logout&redirect_to=https%3A%2F%2Fwww.bgfundz.com%2Faffiliate-area")) {
                webView.clearHistory();
            }

            if (url.startsWith("https://www.bgfundz.com/checkout")) {
                progressDialog.dismiss();
                reload = false;
            }
            progressDialog.dismiss();

        }

    }

}