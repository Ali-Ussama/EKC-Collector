package activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ekc.collector.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import data.Surveyor;

import connection.ConnectionManager;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import util.DataCollectionApplication;
import util.Utilities;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    @BindView(R.id.etName)
    EditText etName;

    @BindView(R.id.etPassword)
    EditText etPassword;

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize activity views
     */
    private void initViews() {
        try {
            ButterKnife.bind(this);
            //Call onClick method when this button clicked
            btnLogin.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            //If internet available invoke Login API using retrofit
            if (Utilities.isNetworkAvailable(this)) {

                //If name empty show error
                if (etName.getText().toString().equals("")) {
                    etName.setError(getString(R.string.user_name));

                    //If password empty show error
                } else if (etPassword.getText().toString().equals("")) {
                    etPassword.setError(getString(R.string.password));

                    //Get the device Id
                } else {
//                getDeviceId();
                    forceLogin();
                }
            } else {
                Utilities.showToast(this, getString(R.string.no_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forceLogin() {
        try {
            String username = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.matches("AJC") && password.matches("1234")) {
                //Get surveyor
                Surveyor surveyor = new Surveyor();
                surveyor.setSurveyorId(5);
                surveyor.setSurveyorName("AJC Surveyor");
                surveyor.setUserName("AJC");
                surveyor.setPassword("1234");
                surveyor.setGlobalID("00000000-0000-0000-0000-000000000000");
                surveyor.setHashPassword("1000:nSrY4BtyhyfITya5PMyKMbVYi4wFBrP+:oAxOQnZNjiW42Hq1wDDBKjw5mC0RMmuf");
                surveyor.setIsAdmin(true);
                surveyor.setDeviceID("");

                //---------Save surveyor info in shared preference
                DataCollectionApplication.setSurveyorName(surveyor.getSurveyorName());
                DataCollectionApplication.setSurveyorId(surveyor.getSurveyorId());
                //---------
                //Create shortcut if it not created before
                if (!DataCollectionApplication.isShortCutCreated())
                    addShortcut();

                //Start map activity
                Intent intent = new Intent(LoginActivity.this, MapEditorActivity.class);
                startActivity(intent);
                finish();
            } else {
                Utilities.showToast(LoginActivity.this, getString(R.string.wrong_username_or_password));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the phone ID to send it to server
     */
    private void getDeviceId() {
        try {
            //If user not accept permission read phone state, request it
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else {
                //Process login scenario
                processLogin(etName.getText().toString(), etPassword.getText().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            //User accepted permission, Process login scenario
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                processLogin(etName.getText().toString(), etPassword.getText().toString());
            } else {
                Toast.makeText(this, getResources().getString(R.string.accept_permission), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLogin(String userName, String password) {
        try {

            Log.i(TAG, "processLogin is called with username = " + userName + " password = " + password);
            //Show loading dialog
            Utilities.showLoadingDialog(this);
            //Get device id
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //The device id
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            String deviceId = telephonyManager.getDeviceId();
            //If no device id set it with default value
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = "200";
            }

            //Call Login api
            ConnectionManager.getInstance().login(userName, password, deviceId).enqueue(new Callback<Surveyor>() {
                @Override
                public void onResponse(Call<Surveyor> call, Response<Surveyor> response) {
                    try {
                        Utilities.dismissLoadingDialog();
                        if (response.isSuccessful()) {
                            //Get surveyor
                            Surveyor surveyor = response.body();
                            //---------Save surveyor info in shared preference
                            DataCollectionApplication.setSurveyorName(surveyor.getSurveyorName());
                            DataCollectionApplication.setSurveyorId(surveyor.getSurveyorId());
                            //---------
                            //Create shortcut if it not created before
                            if (!DataCollectionApplication.isShortCutCreated())
                                addShortcut();

                            //Start map activity
                            Intent intent = new Intent(LoginActivity.this, MapEditorActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Utilities.showToast(LoginActivity.this, getString(R.string.wrong_username_or_password));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Surveyor> call, Throwable t) {
                    try {
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(LoginActivity.this, getString(R.string.connection_error));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create shortcut on user phone home screen
     */
    private void addShortcut() {
        try {
            Log.i(TAG, "In Add Shortcut");
            Intent shortcutIntent = new Intent(getApplicationContext(), SplashActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
            addIntent.putExtra("duplicate", false);
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
