package nanosoft.com.phonenumberauthentication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import in.techware.lataxi.R;
import in.techware.lataxi.app.App;
import in.techware.lataxi.listeners.LoginListener;
import in.techware.lataxi.model.AuthBean;
import in.techware.lataxi.net.DataManager;
import in.techware.lataxi.util.AppConstants;

public class LoginActivity extends BaseAppCompatNoDrawerActivity {

    private EditText etxtUserName;
    private EditText etxtPassword;
    private View.OnClickListener snackBarRefreshOnClickListener;
    private String TAG = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }

        getSupportActionBar().hide();
        swipeView.setPadding(0, 0, 0, 0);
//        lytBase.setFitsSystemWindows(false);

        initViews();
    }

    public void initViews() {

        etxtUserName = (EditText) findViewById(R.id.etxt_login_email);
        etxtPassword = (EditText) findViewById(R.id.etxt_login_password);

        etxtPassword.setTypeface(typeface);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            onBackPressed();
        }
        return true;
    }

    public void onLoginButtonClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (App.isNetworkAvailable()) {
            performLogin(etxtUserName.getText().toString(), etxtPassword.getText().toString());
        } else {
            Snackbar.make(coordinatorLayout, AppConstants.NO_NETWORK_AVAILABLE, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Dismiss", snackBarDismissOnClickListener).show();
        }
    }

    public void performLogin(final String username, String password) {

        swipeView.setRefreshing(true);
        JSONObject postData = getLoginJSObj(username, password);

        DataManager.performLogin(postData, new LoginListener() {
            @Override
            public void onLoadCompleted(AuthBean authBean) {
                swipeView.setRefreshing(false);
                authBean.setPhoneVerified(true);
                App.saveToken(authBean);

                Log.i(TAG, "onLoadCompleted: UserId " + authBean.getUserID());

                Toast.makeText(getApplicationContext(), "Login is Successful",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, LandingPageActivity.class));
                finish();

            }

            @Override
            public void onLoadFailed(String error) {

                Snackbar.make(coordinatorLayout, "Login Failed", Snackbar.LENGTH_LONG)
                        .setAction("Dismiss", snackBarDismissOnClickListener).show();
                swipeView.setRefreshing(false);

            }
        });
    }

    private JSONObject getLoginJSObj(String username, String password) {
        JSONObject postData = new JSONObject();

        try {
            postData.put("username", username);
            postData.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postData;
    }

    public void onForgotPasswordClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}

