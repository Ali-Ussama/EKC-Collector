package util;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.gcs.riyadh.R;

import activities.MapEditorActivity;
import activities.SplashActivity;

public class Utilities {

    static ProgressDialog progressDialog;
    private final String TAG = "Utilities";

    public static void showToast(final Context context, final String message) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isWifiConnected(Context c) {
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();

    }

    public static void showLoadingDialog(Context context) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(context.getString(R.string.loading));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showFileLoadingDialog(Context context) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(context.getString(R.string.loading_file));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissLoadingDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T convertStringToObject(String jsonString, Class<T> castClassTo) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, castClassTo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertObjectToJsonString(Object data) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(data);
    }

    public static String getDateNow() {
        String dateTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa", Locale.ENGLISH);
        dateTime = dateFormat.format(new Date());
        return dateTime;
    }

    public static void showConfirmDialog(Context context, String title, String message, DialogInterface.OnClickListener ok) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.yes), ok)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }

    public static void showInfoDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.yes), null)
                .show();
    }

    public static void zoomToCity(Point mapPoint, MapView mapView) {
        int factor = 9500;
        Envelope stExtent = new Envelope(mapPoint.getX() - factor, mapPoint.getY() - factor, mapPoint.getX() + factor, mapPoint.getY() + factor);
        mapView.setExtent(stExtent);
    }

    public static void hideKeyBoard(final Context context) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        }, 100);
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }


    /**
     * ------------------------------Ali Ussama Update----------------------------------------------
     */

    /**
     * Creating App Folder to hold offline map indexes
     *
     * @param mContext to access the directory
     */
    public static void createAppFolder(SplashActivity mContext) {
        try {

            String TAG = "createAppFolder";

            // Handle if context is not Null
            if (mContext != null) {
                //assign the context to a weakReference to avoid memory leak
                WeakReference<SplashActivity> context = new WeakReference<>(mContext);
                //handle if storage access permission is granted
                if (checkStoragePermission(context)) {
                    // Check, Retrieve or Create App's Folder in Internal Storage
                    File mGCS_CollectorDir = new File(Environment.getExternalStorageDirectory(), "/GCSCollector/Indexes");
                    Log.i(TAG, Environment.getExternalStorageDirectory().getPath());
                    //check if folder exists
                    if (!mGCS_CollectorDir.exists() && mGCS_CollectorDir.mkdir()) {
                        //Logging the path of the folder
                        Log.i(TAG, "Folder has been created with path : " + mGCS_CollectorDir.getPath());
                    } else {
                        Log.i(TAG, "Folder exists or hasn't been created");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * /**
     * Returns all available external SD-Card roots in the system
     *
     * @param context to access System Methods
     * @return paths to all available external SD-Card roots in the system
     */
    public static String[] getStorageDirectories(MapEditorActivity context) {
        //Declaring Array of String to hold the Paths
        String[] storageDirectories;
        // Returns the value of the environment
        // variable with the given name,
        // or null if no such variable exists.
        String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Handle if the SDK version >= API 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Declaring ArrayList of String
            // to hold the paths
            List<String> results = new ArrayList<String>();
            // Declaring Array of File
            // to hold External Files Dirs
            File[] externalDirs = context.getExternalFilesDirs(null);
            // Loop over the External Dirs
            for (File file : externalDirs) {
                //Declare String to hold the path
                String path = null;
                try {

                    path = file.getPath().split("/Android")[0];
                } catch (Exception e) {
                    e.printStackTrace();
                    path = null;
                }
                if (path != null) {
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.isExternalStorageRemovable(file))
                            || rawSecondaryStoragesStr != null && rawSecondaryStoragesStr.contains(path)) {
                        results.add(path);
                    }
                }
            }
            storageDirectories = results.toArray(new String[0]);
        } else {
            final Set<String> rv = new HashSet<String>();

            if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
                Collections.addAll(rv, rawSecondaryStorages);
            }
            storageDirectories = rv.toArray(new String[rv.size()]);
        }

        return storageDirectories;
    }

    /**
     * If the Device android api version => 23 so check permission,
     * Else the Device grant the permission
     *
     * @param context a weakReference of SplashActivity
     */

    private static boolean checkStoragePermission(WeakReference<SplashActivity> context) {
        String TAG = "checkStoragePermission";

        try {
            //Handle SDK version => 23
            if (Build.VERSION.SDK_INT >= 23) {
                //Handle if doesn't grant a storage permission
                if (context.get().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {

                    //request storage access permission
                    Log.i(TAG, "Permission is revoked");
                    ActivityCompat.requestPermissions(context.get(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //permission is automatically granted on sdk<23 upon installation
        Log.i(TAG, "Permission is granted");
        return true;

    }
    /**
     * ------------------------------Ali Ussama Update----------------------------------------------
     */
}
