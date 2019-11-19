package activities;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.esri.android.map.CalloutPopupWindow;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.analysis.MeasuringTool;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.AttachmentInfo;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.renderer.StretchParameters;
import com.esri.core.renderer.StretchRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.table.TableException;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;
import com.ekc.collector.R;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import adapter.BookMarkAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import data.BookMark;
import data.Index;
import map.AttributeViewsBuilder;
import map.ColumnNames;
import map.EditInFeatureFragment;
import map.GeoDatabaseUtil;
import map.util.DialogSearchIndex;
import map.util.MapUtilites;
import util.DataCollectionApplication;
import util.Utilities;

public class MapEditorActivity extends AppCompatActivity implements EditInFeatureFragment.EditorFragmentListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, android.location.LocationListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    protected static final String TAG = "MapEditorActivity";
    private static final String DOWNLOAD_GEO_DATABASE = "DownGDB";
    private static final String SYNC_WITH_SERVER = "Sync";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1;
    static int SearchCase = 0;
    public Point mLocation;
    public ArcGISTiledMapServiceLayer satelliteMap;
    public ArcGISTiledMapServiceLayer baseMap;
    public ArcGISDynamicMapServiceLayer baseMap2;
    public ArcGISDynamicMapServiceLayer gcsMap;
    public ArcGISFeatureLayer pointFeatureLayer, polygonFeatureLayer, lineFeatureLayer, riyadhRectangle, FCL_DISTRIBUTIONBOX, FCL_POLES, FCL_RMU, FCL_Substation, Service_Point, OCL_METER;

    public FragmentManager fragmentManager;
    public GraphicsLayer userLocationGraphicLayer, offlineGraphicLayer, workingAreaGraphicLayer, labelsLayer;
    public GraphicsLayer FCL_DISTRIBUTIONBOXGraphicsLayer, FCL_POLESGraphicsLayer, FCL_RMUGraphicsLayer, FCL_SubstationGraphicsLayer, Service_PointGraphicsLayer, OCL_METERGraphicsLayer, pointFeatuersGraphicsLayer, polygonFeatuersGraphicsLayer, lineFeatuersGraphicsLayer;

    public boolean onlineData = true, isFragmentShown, isAddNew, changeLocationOnline, isInOnlineAddingMode;
    public FeatureLayer FCL_DISTRIBUTIONBOXLayerOffline, FCL_POLESLayerOffline, FCL_RMULayerOffline, FCL_SubstationLayerOffline, Service_PointLayerOffline, OCL_METERLayerOffline, featureLayerPointsOffline, featureLayerLinesOffline, featureLayerPolygonsOffline;
    public boolean isInDrawMood;
    public GeodatabaseFeatureTable FCL_DISTRIBUTIONBOXTable, FCL_POLESTable, FCL_RMUTable, FCL_SubstationTable, Service_PointTable, OCL_METERTable, featureTablePoints, featureTableLines, featureTablePolygons;
    public GraphicsLayer drawGraphicLayer;
    public Graphic[] shapeToAdd;
    PictureMarkerSymbol locationSymbol;
    SimpleMarkerSymbol pointSymbol;
    SimpleLineSymbol shapeSymbol;
    SimpleMarkerSymbol pointSymbolGCS, pointSymbolGCSEdited;
    SimpleLineSymbol shapeSymbolGCS, shapeSymbolGCSEdited;

    public static final int SPATIAL_REFERENCE_CODE = 3857;
    public static final int GLOBAL_SPATIAL_REFERENCE_CODE = 4326;

    boolean isOnRequestUpdate;
    boolean isRequestBefore;
    boolean isFullScreenMode;

    int queryCount = 0;
    ArrayList<FeatureResult> mQueryResults;
    Query onlineQuery;
    QueryParameters offlineQuery;

    /**
     * ----------------------------------Query All Callback--------------------------------------
     **/

    private CallbackListener<FeatureSet> distributionBoxQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;

            Log.i(TAG, "distributionBoxQueryAllCallBack(): ResponseForAll Error: " + errorCount);
            e.printStackTrace();
            /*if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

//            if ((successCount + errorCount) > 0 && (this.queryResults == null || this.queryResults.size() == 0)) {
            if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "distributionBoxQueryAllCallBack(): ResponseForAll onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {

            Log.i(TAG, "distributionBoxQueryAllCallBack(): onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;
            if (queryResult.getGraphics().length > 0) {
                selectedLayer = FCL_DISTRIBUTIONBOX;

                Log.i(TAG, "distributionBoxQueryAllCallBack(): onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, "distributionBoxQueryAllCallBack(): onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

            try {
                if (queryResults != null)
                    Log.i(TAG, "distributionBoxQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 1 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "distributionBoxQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 1 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, "distributionBoxQueryAllCallBack(): onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));

                        FCL_POLES.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, polesQueryAllCallBack);

                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i(TAG, "distributionBoxQueryAllCallBack(): Success: " + successCount);
        }
    };

    private CallbackListener<FeatureSet> polesQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;

            Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll Error: " + errorCount);
            e.printStackTrace();
              /*if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

//            if ((successCount + errorCount) == 1 && (this.queryResults == null || this.queryResults.size() == 0)) {
            if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {
            Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;

            if (queryResult.getGraphics().length > 0) {
                selectedLayer = FCL_POLES;

                Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            try {
                if (queryResults != null)
                    Log.i(TAG, "polesQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 2 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "polesQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 2 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, " polesQueryAllCallBack(): ResponseForAll onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();

                        FCL_RMU.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, rmuQueryAllCallBack);


//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };


    private CallbackListener<FeatureSet> rmuQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;
            Log.i("ResponseForAll", "Error: " + errorCount);
            e.printStackTrace();
              /*if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

//            if ((successCount + errorCount) == 1 && (this.queryResults == null || this.queryResults.size() == 0)) {
            if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ResponseForAll", " onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {


            Log.i(TAG, "rmuQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;
            if (queryResult.getGraphics().length > 0) {
                selectedLayer = FCL_RMU;

                Log.i(TAG, "rmuQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, "rmuQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            try {
                if (queryResults != null)
                    Log.i(TAG, "rmuQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 3 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "rmuQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 3 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, "rmuQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
                        FCL_Substation.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, substationQueryAllCallBack);

//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };


    private CallbackListener<FeatureSet> substationQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;
            Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll Error: " + errorCount);
            e.printStackTrace();
             /*if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

//            if ((successCount + errorCount) == 1 && (this.queryResults == null || this.queryResults.size() == 0)) {
            if (queryCount == 6 && (this.queryResults == null || this.queryResults.size() == 0)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {
            Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;
            if (queryResult.getGraphics().length > 0) {

                selectedLayer = FCL_Substation;

                Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            try {
                if (queryResults != null)
                    Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 4 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "substationQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 4 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, "substationQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();

                        OCL_METER.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, oclMeterQueryAllCallBack);
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };


    private CallbackListener<FeatureSet> oclMeterQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;
            Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll Error: " + errorCount);
            e.printStackTrace();
  /*if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

//            if ((successCount + errorCount) == 1 && (this.queryResults == null || this.queryResults.size() == 0)) {
            if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll  onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {

            Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;
            if (queryResult.getGraphics().length > 0) {

                selectedLayer = OCL_METER;
                Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            try {
                if (queryResults != null)
                    Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 5 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "oclMeterQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 5 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, "oclMeterQueryAllCallBack(): ResponseForAll queryAllCallBack onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();

                        Service_Point.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, servicePointQueryAllCallBack);

//                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };


    private CallbackListener<FeatureSet> servicePointQueryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            queryCount++;

            Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll Error: " + errorCount);
            e.printStackTrace();
//              /*if (errorCount == 3) {
//                selectFeaturesCallBackOnline.onError(e);
//                errorCount = 0;
//                successCount = 0;
//                queryResults = null;
//            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {*/
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.zoom_more));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });
        }

        public void onCallback(final FeatureSet queryResult) {


            Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            queryCount++;

            if (queryResult.getGraphics().length > 0) {

                selectedLayer = Service_Point;

                Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll onCallback 2");

                if (this.queryResults == null) {

                    Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            } else {
                selectedLayer = null;
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            try {
                if (queryResults != null)
                    Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll queryResults size = " + queryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {
            if (queryCount == 6 && this.queryResults != null && this.queryResults.size() > 0) {
                Log.i(TAG, "servicePointQueryAllCallBack(): onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if (queryCount == 6 && (this.queryResults == null || this.queryResults.isEmpty())) {
                Log.i(TAG, "servicePointQueryAllCallBack(): ResponseForAll onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.zoom_more));
                    }
                });

            }

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Utilities.dismissLoadingDialog();
//                }
//            });

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };

    /**
     * ----------------------------------Query All Callback--------------------------------------
     **/

    /**
     * ----------------------------------Query Attachments Callback--------------------------------------
     **/
    private CallbackListener<AttachmentInfo[]> distributionBoxAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
            selectedLayer = FCL_DISTRIBUTIONBOX;
            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<AttachmentInfo[]> polesAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
            selectedLayer = FCL_POLES;

            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<AttachmentInfo[]> rmuAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
            selectedLayer = FCL_RMU;
            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<AttachmentInfo[]> substationAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
            selectedLayer = FCL_Substation;
            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<AttachmentInfo[]> oclMeterAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
            selectedLayer = OCL_METER;

            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<AttachmentInfo[]> servicePointAttachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);

            selectedLayer = Service_Point;
            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    /**
     * ----------------------------------Query Attachments Callback--------------------------------------
     **/

    /**
     * ---------------------------------Query All Offline Callback----------------------------------------
     */

    CallbackListener<FeatureResult> queryAllOfflineCallBackFCLDistributionBox = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            try {
                errorCount++;
                Log.i(TAG, "queryAllOfflineCallBackFCLDistributionBox(): Error: " + errorCount);
//                if (errorCount == 3) {
//                    selectFeaturesCallBackOffline.onError(e);
                e.printStackTrace();
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;
//                }

                FCL_POLESLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLPoles);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void onCallback(FeatureResult queryResults) {
            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackFCLDistributionBox(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackFCLDistributionBox(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackFCLDistributionBox(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (queryCount == 1 && mQueryResults != null && !mQueryResults.isEmpty()) {
                FCL_POLESLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLPoles);
//                processSelectQueryResultOffline(this.queryResults);
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = FCL_DISTRIBUTIONBOXLayerOffline;
                selectedTableOffline = FCL_DISTRIBUTIONBOXTable;

            } else if (queryCount == 1 && mQueryResults == null || mQueryResults.isEmpty()) {
                FCL_POLESLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLPoles);
            }

//            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };

    CallbackListener<FeatureResult> queryAllOfflineCallBackFCLPoles = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i(TAG, "queryAllOfflineCallBackFCLPoles(): Error: " + errorCount);
//            if (errorCount == 3) {
//            selectFeaturesCallBackOffline.onError(e);
            e.printStackTrace();
//            this.queryResults = null;
            errorCount = 0;
            successCount = 0;

//        }
        }

        public void onCallback(FeatureResult queryResults) {
            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackFCLPoles(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackFCLPoles(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackFCLPoles(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (queryCount == 2 && mQueryResults != null && !mQueryResults.isEmpty()) {
                FCL_RMULayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLRmu);
//                processSelectQueryResultOffline(mQueryResults);
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = FCL_POLESLayerOffline;
                selectedTableOffline = FCL_POLESTable;

            } else if (queryCount == 2 && (mQueryResults == null || mQueryResults.isEmpty())) {
                FCL_RMULayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLRmu);
            }

//            Log.d("ResponseForAll", "Success: " + successCount);
        }
    };

    CallbackListener<FeatureResult> queryAllOfflineCallBackFCLRmu = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i(TAG, "queryAllOfflineCallBackFCLRmu(): Error: " + errorCount);
//            if (errorCount == 3) {
//            selectFeaturesCallBackOffline.onError(e);
//            this.queryResults = null;
            errorCount = 0;
            successCount = 0;
//            }
        }

        public void onCallback(FeatureResult queryResults) {
            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackFCLRmu(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackFCLRmu(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackFCLRmu(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (queryCount == 3 && mQueryResults != null && !mQueryResults.isEmpty()) {
                FCL_SubstationLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLSubstation);
//                processSelectQueryResultOffline(mQueryResults);
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = FCL_RMULayerOffline;
                selectedTableOffline = FCL_RMUTable;

            } else if (queryCount == 3 && (mQueryResults == null || mQueryResults.isEmpty())) {
                FCL_SubstationLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLSubstation);
            }

//            Log.d("ResponseForAll", "Success: " + successCount);
        }
    };

    CallbackListener<FeatureResult> queryAllOfflineCallBackFCLSubstation = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i(TAG, "queryAllOfflineCallBackFCLSubstation(): Error: " + errorCount);
//            if (errorCount == 3) {
//            selectFeaturesCallBackOffline.onError(e);
//            this.queryResults = null;
            errorCount = 0;
            successCount = 0;
//            }
        }

        public void onCallback(FeatureResult queryResults) {
            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackFCLSubstation(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackFCLSubstation(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackFCLSubstation(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (queryCount == 4 && mQueryResults != null && !mQueryResults.isEmpty()) {
                OCL_METERLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackOCLMeter);
//                processSelectQueryResultOffline(this.queryResults);
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = FCL_SubstationLayerOffline;
                selectedTableOffline = FCL_SubstationTable;

            } else if (queryCount == 4 && (mQueryResults == null || mQueryResults.isEmpty())) {
                OCL_METERLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackOCLMeter);
            }

        }
    };

    CallbackListener<FeatureResult> queryAllOfflineCallBackOCLMeter = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i(TAG, "queryAllOfflineCallBackOCLMeter(): Error: " + errorCount);
//            if (errorCount == 3) {
//                selectFeaturesCallBackOffline.onError(e);
//            this.queryResults = null;
            errorCount = 0;
            successCount = 0;
//            }
        }

        public void onCallback(FeatureResult queryResults) {
            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackOCLMeter(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackOCLMeter(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackOCLMeter(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (
//            if ((successCount + errorCount) == 3) {
            if (queryCount == 5 && mQueryResults != null && !mQueryResults.isEmpty()) {
                Service_PointLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackServicePoint);
//                processSelectQueryResultOffline(this.queryResults);
//                this.queryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = OCL_METERLayerOffline;
                selectedTableOffline = OCL_METERTable;

            } else if (queryCount == 5 && (mQueryResults == null || mQueryResults.isEmpty())) {
                Service_PointLayerOffline.selectFeatures(offlineQuery, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackServicePoint);
            }
        }
    };

    CallbackListener<FeatureResult> queryAllOfflineCallBackServicePoint = new CallbackListener<FeatureResult>() {
        private int errorCount = 0;
        private int successCount = 0;
//        private ArrayList<FeatureResult> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i(TAG, "queryAllOfflineCallBackServicePoint(): Error: " + errorCount);
//            if (errorCount == 3) {
//            selectFeaturesCallBackOffline.onError(e);
//            this.queryResults = null;
            errorCount = 0;
            successCount = 0;
//            }
        }

        public void onCallback(FeatureResult queryResults) {


            successCount++;
            queryCount++;

            if (queryResults != null) {
                if (mQueryResults == null) {
                    Log.i(TAG, "queryAllOfflineCallBackServicePoint(): this.queryResults == null");
                    mQueryResults = new ArrayList<>();
                }
                mQueryResults.add(queryResults);
            }

            try {
                Log.i(TAG, "queryAllOfflineCallBackServicePoint(): onCallback is called");

                if (queryResults != null)
                    Log.i(TAG, "queryAllOfflineCallBackServicePoint(): onCallback queryResults size = " + mQueryResults.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if ((successCount + errorCount) == 3) {
            if (queryCount == 6 && mQueryResults != null && !mQueryResults.isEmpty()) {
                processSelectQueryResultOffline(mQueryResults);
                mQueryResults = null;
                errorCount = 0;
                successCount = 0;

                selectedLayerOffline = Service_PointLayerOffline;
                selectedTableOffline = Service_PointTable;

            } else if (queryCount == 6 && mQueryResults == null || mQueryResults.isEmpty()) {
//                Utilities.showToast(this,"");

            }

            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };

    /**
     * ---------------------------------Query All Offline Callback----------------------------------------
     */

    private CallbackListener<FeatureEditResult[][]> addShapeCallBack = new CallbackListener<FeatureEditResult[][]>() {
        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    Log.i(TAG, "Error In Adding");
                    Utilities.dismissLoadingDialog();
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                    Utilities.showToast(MapEditorActivity.this, e.toString());
                    finishAddingOnline();
                }
            });
        }

        @Override
        public void onCallback(final FeatureEditResult[][] editResult) {
            Log.i(TAG, "addShapeCallBack onCallback(): is called");

            if (editResult[0] != null && editResult[0][0] != null && editResult[0][0].isSuccess()) {
                Log.d(TAG, "Added Successfully");
                Utilities.showToast(MapEditorActivity.this, "Added Successfully");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.dismissLoadingDialog();
                        getFeatureAndShowEditFragment(shapeToAdd[0].getGeometry(), editResult[0][0].getObjectId(), true);
                        finishAddingOnline();
                    }
                });
            } else {
                Log.d(TAG, "Not Added Successfully");
                Utilities.showToast(MapEditorActivity.this, "Not Added Successfully");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                        finishAddingOnline();
                    }
                });
            }
        }
    };

    private CallbackListener<FeatureSet> selectFeaturesCallBackOnline = new CallbackListener<FeatureSet>() {

        public void onError(final Throwable e) {
            Log.i(TAG, "Select Features Error" + e.getLocalizedMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                    Utilities.showToast(MapEditorActivity.this, e.getMessage());
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                }
            });
        }

        public void onCallback(final FeatureSet queryResults) {
            Log.i(TAG, "selectFeaturesCallBackOnline(): is called");

            if (queryResults == null) {
                Log.i(TAG, "selectFeaturesCallBackOnline(): query result == null");

                Log.i(TAG, "selectFeaturesCallBackOnline");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });
            } else {
                Log.i(TAG, "selectFeaturesCallBackOnline(): query result != null");

                ArrayList<FeatureSet> featureSets = new ArrayList<FeatureSet>();
                featureSets.add(queryResults);
                processSelectQueryResultOnline(featureSets);
            }
        }
    };

    private CallbackListener<FeatureResult> selectFeaturesCallBackOffline = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(final FeatureResult queryResults) {
            if (queryResults == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "selectFeaturesCallBackOffline");
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                    }
                });
            } else {
                ArrayList<FeatureResult> featureSets = new ArrayList<>();
                featureSets.add(queryResults);
                processSelectQueryResultOffline(featureSets);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            Utilities.showToast(MapEditorActivity.this, getString(R.string.error_in_adding_feature));
        }
    };

    private CallbackListener<AttachmentInfo[]> attachmentCallbackOnline = new CallbackListener<AttachmentInfo[]>() {
        @Override
        public void onCallback(AttachmentInfo[] attachmentInfo) {
            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);

            showResult(selectedObjectId, attributes, attachmentInfo, isNewFeature);
        }

        @Override
        public void onError(final Throwable e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_error));
                    Utilities.dismissLoadingDialog();
                }
            });
        }
    };

    private CallbackListener<List<AttachmentInfo>> attachmentCallbackOffline = new CallbackListener<List<AttachmentInfo>>() {
        @Override
        public void onCallback(List<AttachmentInfo> attachmentInfos) {
            showResult(selectedObjectId, selectedFeatureAttributes, attachmentInfos.toArray(new AttachmentInfo[0]), isNewFeature);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureEditResult[][]> editOrDeleteInShapeCallback = new CallbackListener<FeatureEditResult[][]>() {

        public void onCallback(FeatureEditResult[][] result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MapUtilites.zoomToPoint(mapView, shapeToAdd[0].getGeometry());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 300);
                    hideFragment();

                    String typeToRefresh = null;
                    if (shapeToAdd[0].getGeometry() instanceof Point) {
                        typeToRefresh = POINT;
                    } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                        typeToRefresh = LINE;
                    } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                        typeToRefresh = POLYGON;
                    }
                    refreshPOI();
                }
            });
        }

        public void onError(final Throwable e) {

            Log.i(MapEditorActivity.TAG, "error updating feature: " + e.getLocalizedMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                    Utilities.showToast(MapEditorActivity.this, e.toString());
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                }
            });
        }
    };

    private CallbackListener<FeatureSet> queryAllCallBack = new CallbackListener<FeatureSet>() { //TODO queryAllCallBack Error
        private int errorCount = 0;
        private int successCount = 0;
        private ArrayList<FeatureSet> queryResults;

        public void onError(final Throwable e) {
            errorCount++;
            Log.i("ResponseForAll", "Error: " + errorCount);
            e.printStackTrace();
            if (errorCount == 3) {
                selectFeaturesCallBackOnline.onError(e);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            } else if ((successCount + errorCount) == 1 && this.queryResults != null && this.queryResults.size() > 0) {
                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

            if ((successCount + errorCount) == 1 && (this.queryResults == null || this.queryResults.size() == 0)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ResponseForAll", " onError 1");

                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                }
            });
        }

        public void onCallback(final FeatureSet queryResult) {
            Log.i("ResponseForAll", "queryAllCallBack onCallback 1");

//            this.queryResults = new ArrayList<>();
//            this.queryResults.add(queryResult);
//
//            processSelectQueryResultOnline(this.queryResults);

            successCount++;
            if (queryResult.getGraphics().length > 0) {

                Log.i("ResponseForAll", "queryAllCallBack onCallback 2");

                if (this.queryResults == null) {

                    Log.i("ResponseForAll", "queryAllCallBack onCallback 3");

                    this.queryResults = new ArrayList<>();
                }
                this.queryResults.add(queryResult);
            }

            if ((successCount + errorCount) <= 6 && this.queryResults != null && this.queryResults.size() > 0) {

                Log.i("ResponseForAll", "queryAllCallBack onCallback 4");

                processSelectQueryResultOnline(this.queryResults);
                errorCount = 0;
                successCount = 0;
                queryResults = null;
            }

            if ((successCount + errorCount) <= 6 && (this.queryResults == null || this.queryResults.size() == 0)) {

                Log.i("ResponseForAll", "queryAllCallBack onCallback 5");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorCount = 0;
                        successCount = 0;
                        queryResults = null;
                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                }
            });

            Log.i("ResponseForAll", "Success: " + successCount);
        }
    };

    /**
     * -----------------------------Table Query Callbacks------------------------------------------
     */

    private CallbackListener<FeatureResult> callbackOfflineQuery = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryFCLDistributionBox = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        FCL_DISTRIBUTIONBOXGraphicsLayer.addGraphic(getGraphic(feature));
//                        if (feature.getGeometry() instanceof Point) {
//                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
//                        } else if (feature.getGeometry() instanceof Polygon) {
//                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
//                        } else {
//                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
//                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryFCLPoles = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryFCLRMU = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryFCLSubstation = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryOCLMeter = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private CallbackListener<FeatureResult> callbackTableOfflineQueryServicePoint = new CallbackListener<FeatureResult>() {
        @Override
        public void onCallback(FeatureResult results) {

            if (results != null) {
                for (Object e : results) {
                    if (e instanceof Feature) {
                        Log.i("callbackOfflineQuery", "callbackOfflineQuery");
                        final Feature feature = (Feature) e;
                        if (feature.getGeometry() instanceof Point) {
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else if (feature.getGeometry() instanceof Polygon) {
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        } else {
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                        }
                    }
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    /**
     * -----------------------------Table Query Callbacks------------------------------------------
     */

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.sb_gcs_satalite_opacity)
                gcsMap.setOpacity(((float) progress / 10));
            else if (seekBar.getId() == R.id.sb_esri_satalite_opacity)
                satelliteMap.setOpacity(((float) progress / 10));
            else
                baseMap.setOpacity(((float) progress / 10));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private Index selectedWorkIndex;

    @BindView(R.id.rlFragment)
    public RelativeLayout rlFragment;

    @BindView(R.id.esriSeekBar)
    LinearLayout esriSeekBar;

    @BindView(R.id.gcsSeekBar)
    LinearLayout gcsSeekBar;

    @BindView(R.id.baseMapSeekBar)
    LinearLayout baseMapSeekBar;

    @BindView(R.id.fab_general)
    FloatingActionMenu fabGeneral;

    @BindView(R.id.fab_add_distribution_box)
    public com.github.clans.fab.FloatingActionButton fabDistributionBox;

    @BindView(R.id.fab_add_poles)
    public com.github.clans.fab.FloatingActionButton fabPoles;

    @BindView(R.id.fab_add_rmu)
    public com.github.clans.fab.FloatingActionButton fabRMU;

    @BindView(R.id.fab_add_sub_station)
    public com.github.clans.fab.FloatingActionButton fabSubStation;

    @BindView(R.id.fab_add_ocl_meter)
    public com.github.clans.fab.FloatingActionButton fabOCLMeter;

    @BindView(R.id.fab_add_service_point)
    public com.github.clans.fab.FloatingActionButton fabServicePoint;

    @BindView(R.id.fabLocation)
    public FloatingActionButton fabLocation;

    @BindView(R.id.fabFullScreen)
    public FloatingActionButton fabFullScreen;

    public MenuItem menuItemOnline;

    public MenuItem menuItemLoad;

    public MenuItem menuItemSync;

    public MenuItem menuItemIndex;

    public MenuItem menuItemOffline;

    public MenuItem item_load_previous_offline;

    public MenuItem menuItemOverflow;

    //    public MenuItem menuItemSearch;
    public MenuItem menuItemSatellite;

//    public MenuItem menuItemBaseMap;

    public MenuItem menuItemGCS;

    public MenuItem menuItemGoOfflineMode;

    public MenuItem menuItemGoOnlineMode;

    @BindView(R.id.sb_esri_satalite_opacity)
    SeekBar esriOpacity;

    @BindView(R.id.sb_gcs_satalite_opacity)
    SeekBar gcsOpacity;

    @BindView(R.id.baseMapOpacity)
    SeekBar baseMapOpacity;

    EditInFeatureFragment editInFeatureFragment;

    View dialogView;

    @BindView(R.id.tvLatLong)
    TextView tvLatLong;

    @BindView(R.id.tv_more_layer_info)
    TextView tvMoreLayerInfo;

    private boolean isShowingLayerInfo;

    FloatingActionButton fabMeasure;

    Polygon poly;

    ActionMode drawToolsActionMode;

    @BindView(R.id.compass)
    ImageView mCompass;

    Matrix mMatrix;

    Bitmap mBitmap;

    LocationManager manager;

    Geometry workingAreaGeometry;

    @BindView(R.id.btnCancel)
    Button btnCancelAddOnline;

//    @BindView(R.id.toolbar)
//    Toolbar mToolbar;

    @BindView(R.id.mapView)
    public MapView mapView;

    private GoogleApiClient mGoogleApiClient;
    private Location googleLocation;
    private boolean isSatelliteAdded, isBaseMapAdded = true, isGCSAdded;
    public AlertDialog errorInMapDownloadDialog;
    public static boolean isInitialized;
    public static int currentOfflineVersion = 1;
    public static String localDatabaseTitle;
    public static SpatialReference LAYER_SR;
    private FeatureSet queryFeatureResults;
    private int selectedIndex;
    private int selectedObjectId;
    private boolean isNewFeature;
    private HashMap<String, Object> selectedFeatureAttributes;
    public static UserCredentials mapServiceToken, featureServiceToken, GcsSatelliteToken;

    public ArrayList<Index> indices;

    public MapEditorActivity mCurrent;

    public MapView getMapView() {
        return mapView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_editor);

        init();
    }

    private void init() {
        try {
            mCurrent = MapEditorActivity.this;

            ButterKnife.bind(mCurrent);

            isInitialized = false;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }

//            mToolbar.setTitle(getResources().getString(R.string.app_name));

//            setActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                Log.i(TAG, "init(): actionBar != null");
                actionBar.setTitle(getResources().getString(R.string.app_name));
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setCustomView(R.layout.title);
            } else {

                Log.i(TAG, "init(): actionBar = null");
            }

            mapServiceToken = new UserCredentials();
            featureServiceToken = new UserCredentials();
            GcsSatelliteToken = new UserCredentials();
            // mapServiceToken.setUserToken(getString(R.string.MapServiceEnglishToken), "android");
            // mapServiceToken.setAuthenticationType(UserCredentials.AuthenticationType.TOKEN);
            //featureServiceToken.setUserToken(getString(R.string.FeatureServiceToken), "android");
            // featureServiceToken.setAuthenticationType(UserCredentials.AuthenticationType.TOKEN);
            mapServiceToken.setUserAccount("siteadmin", "siteadmin");

            featureServiceToken.setUserAccount("siteadmin", "siteadmin");

            GcsSatelliteToken.setUserToken(getString(R.string.GcsSatelliteToken), "android");
            GcsSatelliteToken.setAuthenticationType(UserCredentials.AuthenticationType.TOKEN);


            tvLatLong.setVisibility(View.VISIBLE);
            tvLatLong.setText(getResources().getString(R.string.loading_location));


            mCompass.setScaleType(ImageView.ScaleType.MATRIX);
            mMatrix = new Matrix();
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_compass);

            locationSymbol = new PictureMarkerSymbol(ContextCompat.getDrawable(this, R.drawable.ic_user_location));

            pointSymbol = new SimpleMarkerSymbol(Color.RED, 6, SimpleMarkerSymbol.STYLE.CIRCLE); //New Points
            shapeSymbol = new SimpleLineSymbol(Color.RED, 4);

            pointSymbolGCS = new SimpleMarkerSymbol(Color.BLUE, 6, SimpleMarkerSymbol.STYLE.CIRCLE); //GCS Points
            shapeSymbolGCS = new SimpleLineSymbol(Color.BLUE, 4);

            pointSymbolGCSEdited = new SimpleMarkerSymbol(Color.GREEN, 6, SimpleMarkerSymbol.STYLE.CIRCLE); //Edit GCS
            shapeSymbolGCSEdited = new SimpleLineSymbol(Color.GREEN, 4);

            fabDistributionBox.setOnClickListener(this);
            fabPoles.setOnClickListener(this);
            fabRMU.setOnClickListener(this);
            fabSubStation.setOnClickListener(this);
            fabOCLMeter.setOnClickListener(this);
            fabServicePoint.setOnClickListener(this);

            //Hide Map legend
            showMapLegend();


            fabFullScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFullScreenMode) {
                        fabFullScreen.setImageResource(R.drawable.ic_fullscreen_white_24dp);
                        exitFullScreenMode();
                    } else {
                        fabFullScreen.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
                        fullScreenMode();
                    }

                    isFullScreenMode = !isFullScreenMode;
                }
            });

            fabLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showUserLocation();
                    if (mLocation != null) {
                        Log.i(TAG, "onCreate(): fab on Click listener mLocation != null");
                        MapUtilites.zoomToPoint(mapView, mLocation);
                    } else {
                        Log.i(TAG, "onCreate(): fab on Click listener mLocation == null");

                    }

                }
            });

            tvMoreLayerInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowingLayerInfo) {
                        findViewById(R.id.linear_layers_details).setVisibility(View.GONE);
                        tvMoreLayerInfo.setText("More Info >>");
                    } else {
                        findViewById(R.id.linear_layers_details).setVisibility(View.VISIBLE);
                        tvMoreLayerInfo.setText("Less Info <<");
                    }
                    isShowingLayerInfo = !isShowingLayerInfo;
                }
            });

            fabLocation.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(MapEditorActivity.this, "تحديد المكان", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            btnCancelAddOnline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAddingOnline();
                }
            });

            btnCancelAddOnline.setVisibility(View.GONE);

            mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
                @Override
                public void onStatusChanged(final Object source, final STATUS status) {
                    if (STATUS.INITIALIZED == status) {
                        if (source instanceof MapView) {
                            if (!isInitialized)
                                mapInitialized();
                        }
                    }
                    if (STATUS.LAYER_LOADED == status) {
                        if (source == pointFeatureLayer) {
                            Log.i("MapStatus", "baseMap Loaded");
                            mapView.setMaxExtent(new Envelope(3648439.283628551, 1218200.3409051022, 6691440.351455368, 4016160.2906351034));
                            mapView.setMinScale(baseMap.getMinScale());
                            mapView.setMaxScale(baseMap.getMaxScale());
                            LAYER_SR = SpatialReference.create(SPATIAL_REFERENCE_CODE);
                            if (workingAreaGeometry != null)
                                mapView.setExtent(workingAreaGeometry, 100, true);
                        }
                    }
                }
            });

            fragmentManager = ((FragmentManager) getSupportFragmentManager());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            //handle Online Map if Network Available
            if (Utilities.isNetworkAvailable(this)) {
                //Initializing Online Map Layers
                initMapOnlineLayers();
                //Handling Offline Map if there is no network
            } else {
                Log.i(TAG, "There Is No Connection Local Map is Loading....");

                onlineData = false;

                Log.i(TAG, "Map is offline");


                if (GeoDatabaseUtil.isGeoDatabaseLocal()) {

                    showOfflineMapsList(this, mapView);


                    Log.i("isGeoLocationDatabase", "one");
                } else {
                    Log.i("isGeoLocationDatabase", "two");
                    showNoOfflineMapDialog(this, mapView);
                }

            }


//            if (GeoDatabaseUtil.isOfflineGDBAvailable(this)) {
//                Log.i(TAG, "gdb has been read");
//            } else {
//                Log.i(TAG, "cannot read gdb");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMapLegend() {
        try {
            findViewById(R.id.linear_layers_info).setVisibility(View.VISIBLE);
            updateFeatureNumbers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/**
 *  ------------------------------Ali Ussama Update--------------------------------------------
 * */
    /**
     * Loads Raster file (Extension -> .TIF ) and adds it to a new RasterLayer. The RasterLayer is then added
     * to the map as an operational layer. Map viewpoint is then set based on the Raster's geometry.
     */
    public void loadRaster(String path) {
        try {
            Log.i(TAG, "LoadRaster is called");

            // create a raster from a local raster file
            FileRasterSource rasterSource = new FileRasterSource(path);

            rasterSource.project(mapView.getSpatialReference());

            //A raster layer to hold raster data
            RasterLayer rasterLayer = new RasterLayer(rasterSource);

            StretchParameters stretchParams = new StretchParameters.MinMaxStretchParameters();

            StretchRenderer renderer = new StretchRenderer();

            renderer.setStretchParameters(stretchParams);

            rasterLayer.setRenderer(renderer);

            //Add the raster layer to the MapView
            mapView.addLayer(rasterLayer, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//TODO remove for method
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Log.i(TAG, "" + requestCode);
//            Toast.makeText(this, "requestCode = " + requestCode, Toast.LENGTH_LONG).show();

            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK) {
                        try {
                            final Uri uri = data.getData();
                            if (uri != null) {

                                // Get the File path from the Uri
                                String path = uri.getPathSegments().get(1);

                                // Alternatively, use FileUtils.getFile(Context, Uri)
                                String file_path = (path.split(":"))[1];

                                if (path.contains("primary")) {
                                    path = "/storage/emulated/0/" + file_path;
                                    Log.i(TAG, "Path 0 = " + path);

                                } else {
                                    path = Utilities.getStorageDirectories(this)[0] + "/" + file_path;

                                    Log.i(TAG, "Path 1 = " + path);

                                    Toast.makeText(this, path, Toast.LENGTH_LONG).show();

                                }

                                loadRaster(path);

                            }
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * ------------------------------Ali Ussama Update-------------------------------------------
     */

    public void initMapOnlineLayers() {
        try {
            onlineData = true;
//            baseMap = new ArcGISTiledMapServiceLayer(getResources().getString(R.string.BaseMap), mapServiceToken);
            /***
             * */
            //TODO Ali Ussama Update

            MapOptions mStreetMapOptions = new MapOptions(MapOptions.MapType.STREETS);

            baseMap2 = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.BaseMap2));

//            mapView.addLayer(baseMap, 0);
            mapView.addLayer(baseMap2);


//            baseMap.setOpacity(1.0f);
//            baseMapOpacity.setProgress(10);
//            baseMapOpacity.setOnSeekBarChangeListener(seekBarChangeListener);

//            satelliteMap = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer");
//            satelliteMap.setOpacity(0.2f);
//            esriOpacity.setProgress(2);
//            esriOpacity.setOnSeekBarChangeListener(seekBarChangeListener);
//
//            gcsMap = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.GcsSatellite), null, GcsSatelliteToken);
//            gcsMap.setOpacity(0.2f);
//            gcsOpacity.setProgress(2);
//            gcsOpacity.setOnSeekBarChangeListener(seekBarChangeListener);

            pointFeatureLayer = new ArcGISFeatureLayer(getResources().getString(R.string.point_feature_layer_services), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            lineFeatureLayer = new ArcGISFeatureLayer(getResources().getString(R.string.line_feature_layer_services), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            polygonFeatureLayer = new ArcGISFeatureLayer(getResources().getString(R.string.polygon_feature_layer_services), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);

            FCL_DISTRIBUTIONBOX = new ArcGISFeatureLayer(getResources().getString(R.string.FCL_DISTRIBUTIONBOX), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            FCL_POLES = new ArcGISFeatureLayer(getResources().getString(R.string.FCL_POLES), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            FCL_RMU = new ArcGISFeatureLayer(getResources().getString(R.string.FCL_RMU), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            FCL_Substation = new ArcGISFeatureLayer(getResources().getString(R.string.FCL_Substation), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            Service_Point = new ArcGISFeatureLayer(getResources().getString(R.string.Service_Point), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);
            OCL_METER = new ArcGISFeatureLayer(getResources().getString(R.string.OCL_METER), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);

            mapView.addLayer(FCL_DISTRIBUTIONBOX);
            mapView.addLayer(FCL_POLES);
            mapView.addLayer(FCL_RMU);
            mapView.addLayer(FCL_Substation);
            mapView.addLayer(Service_Point);
            mapView.addLayer(OCL_METER);

//            riyadhRectangle = new ArcGISFeatureLayer(getResources().getString(R.string.index_services), ArcGISFeatureLayer.MODE.ONDEMAND, featureServiceToken);


            Log.i(TAG, "loading label Rectangle");

            mapView.setMapOptions(mStreetMapOptions);

            MapUtilites.zoomToExtent(mapView);
//            mapView.addLayer(riyadhRectangle);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showNoOfflineMapDialog(final MapEditorActivity context, final MapView mapView) {
        try {
            Log.i(TAG, "showNoOfflineMapDialog(): is called");

            new AlertDialog.Builder(context)
                    .setTitle(context.getResources().getString(R.string.no_internet))
                    .setMessage(context.getResources().getString(R.string.no_offline_version))
                    .setCancelable(true)
                    .setPositiveButton(context.getResources().getString(R.string.action_go_online), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!Utilities.isNetworkAvailable(context)) {
                                showNoOfflineMapDialog(context, mapView);
                            } else {
                                GeoDatabaseUtil.finishGoingOnline(context, mapView);
                            }
                        }
                    }).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updated By Ali Ussama 7/11/2019
     */
    public void mapInitialized() {
        try {

            Log.i("MapStatus", "INITIALIZED");

            isInitialized = true;

            workingAreaGraphicLayer = new GraphicsLayer();
            mapView.addLayer(workingAreaGraphicLayer);

            initGraphicLayers(); // TODO Init Graphic Layers

            pointFeatuersGraphicsLayer = new GraphicsLayer();
            lineFeatuersGraphicsLayer = new GraphicsLayer();
            polygonFeatuersGraphicsLayer = new GraphicsLayer();
            labelsLayer = new GraphicsLayer();
            mapView.addLayer(labelsLayer);


            mapView.addLayer(pointFeatuersGraphicsLayer);
            mapView.addLayer(lineFeatuersGraphicsLayer);
            mapView.addLayer(polygonFeatuersGraphicsLayer);

            drawGraphicLayer = new GraphicsLayer();
            mapView.addLayer(drawGraphicLayer);

            userLocationGraphicLayer = new GraphicsLayer();
            mapView.addLayer(userLocationGraphicLayer);

            //To display user location marker when userLocationGraphicLayer initialized
            showUserLocationOnMap();

            // Enabled wrap around map.
            mapView.enableWrapAround(true);
            mapView.setMapBackground(Color.parseColor("#f2f0e5"), Color.RED, 0, 0);

            mGraphicsLayerAddShapes = new GraphicsLayer();
            mapView.addLayer(mGraphicsLayerAddShapes);

            fabGeneral.setClosedOnTouchOutside(true);

            if (onlineData /*&& baseMap != null*/) {
//                mapView.setMaxExtent(new Envelope(3648439.283628551, 1218200.3409051022, 6691440.351455368, 4016160.2906351034)); // TODO
//                mapView.setMinScale(1.801524E7); // TODO
            }

            mapView.setOnPinchListener(new OnPinchListener() {

                private static final long serialVersionUID = 1L;

                @Override
                public void prePointersUp(float arg0, float arg1, float arg2, float arg3, double arg4) {
                }

                @Override
                public void prePointersMove(float arg0, float arg1, float arg2, float arg3, double arg4) {
                }

                @Override
                public void prePointersDown(float arg0, float arg1, float arg2, float arg3, double arg4) {
                }

                @Override
                public void postPointersUp(float arg0, float arg1, float arg2, float arg3, double arg4) {
                }

                @Override
                public void postPointersMove(float arg0, float arg1, float arg2, float arg3, double arg4) {
                    // Update the compass angle from the map rotation angle (the arguments passed in to the method are not
                    // relevant in this case).

                    mMatrix.reset();
                    mMatrix.postRotate(-(float) mapView.getRotationAngle(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                    mCompass.setImageMatrix(mMatrix);
                }

                @Override
                public void postPointersDown(float arg0, float arg1, float arg2, float arg3, double arg4) {
                }
            });

            //Cumpuss rotation
            mCompass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mapView.isAllowRotationByPinch()) {
                        mapView.setAllowRotationByPinch(false);
                    } else {
                        mapView.setAllowRotationByPinch(true);
                    }
                    mMatrix.reset();
                    mapView.setRotationAngle(0, true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMatrix.postRotate(-(float) mapView.getRotationAngle(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                            mCompass.setImageMatrix(mMatrix);
                        }
                    }, 200);
                }
            });

            getUserWorkingArea();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGraphicLayers() {
        try {
            FCL_DISTRIBUTIONBOXGraphicsLayer = new GraphicsLayer();//TODO Offline
            FCL_POLESGraphicsLayer = new GraphicsLayer();//TODO Offline
            FCL_RMUGraphicsLayer = new GraphicsLayer();//TODO Offline
            FCL_SubstationGraphicsLayer = new GraphicsLayer();//TODO Offline
            OCL_METERGraphicsLayer = new GraphicsLayer();//TODO Offline
            Service_PointGraphicsLayer = new GraphicsLayer();//TODO Offline

            mapView.addLayer(FCL_DISTRIBUTIONBOXGraphicsLayer);//TODO Offline
            mapView.addLayer(FCL_POLESGraphicsLayer);//TODO Offline
            mapView.addLayer(FCL_RMUGraphicsLayer);//TODO Offline
            mapView.addLayer(FCL_SubstationGraphicsLayer);//TODO Offline
            mapView.addLayer(OCL_METERGraphicsLayer);//TODO Offline
            mapView.addLayer(Service_PointGraphicsLayer);//TODO Offline

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserWorkingArea() {

//        if (Utilities.isNetworkAvailable(this) && onlineData) {
//            initMap();
//            refreshPOI(null);
//        } else {
        initMap();
        refreshPOI();

//        try {
//            if (googleLocation != null) {
//                Log.i(TAG, "getUserWorkingArea(): googleLocation not Null");
//                mLocation = MapUtilites.getMapPoint(mapView, googleLocation);
//                if (mLocation.getX() > 0 && mLocation.getY() > 0) {
//                    Log.i(TAG, "getUserWorkingArea(): mLocation not Null");
//
//                    Point mapPoint = (Point) GeometryEngine.project(mLocation, SpatialReference.create(GLOBAL_SPATIAL_REFERENCE_CODE), mapView.getSpatialReference());
//                    if (mapPoint != null && mapPoint.getX() > 0 && mapPoint.getY() > 0) {
//                        Log.i(TAG, "getUserWorkingArea(): mapPoint not Null");
//                        MapUtilites.zoomToPoint(mapView, mapPoint);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        }
    }

    private void initMap() {
        setOnTapListenerOnMap();
    }

    private void fullScreenMode() {
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fabLocation.setVisibility(View.GONE);
        fabGeneral.setVisibility(View.GONE);
//        fabMeasure.setVisibility(View.GONE);
        tvLatLong.setVisibility(View.GONE);
    }

    private void exitFullScreenMode() {
        try {
            getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fabLocation.setVisibility(View.VISIBLE);
        fabGeneral.setVisibility(View.VISIBLE);
//        fabMeasure.setVisibility(View.VISIBLE);
        tvLatLong.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {

        Log.i(TAG, "onBackPressed(): is called");
        /*if ((isSatelliteAdded || isBaseMapAdded) && getTiledOrDynamicLayersCount() > 1) {

            //isGCSAdded ||
//            if (menuItemGCS.isChecked()) {
//                isGCSAdded = false;z
//                if (gcsMap != null)
//                    mapView.removeLayer(gcsMap);
//                gcsSeekBar.setVisibility(View.GONE);
//                tvLatLong.setVisibility(View.VISIBLE);
//                menuItemGCS.setChecked(isGCSAdded);
//            } else
//            if (menuItemSatellite.isChecked()) {
//                isSatelliteAdded = false;
//                if (satelliteMap != null)
//                    mapView.removeLayer(satelliteMap);
//                esriSeekBar.setVisibility(View.GONE);
//                tvLatLong.setVisibility(View.VISIBLE);
//                menuItemSatellite.setChecked(isSatelliteAdded);
//            } else if (menuItemBaseMap.isChecked()) {
//                isBaseMapAdded = false;
//                baseMapSeekBar.setVisibility(View.GONE);
//                tvLatLong.setVisibility(View.VISIBLE);
//                menuItemBaseMap.setChecked(isBaseMapAdded);
//            }
//            mapLayersUpdated();
        } else */
        if (isFragmentShown) {
            Log.i(TAG, "onBackPressed(): isFragmentShown = " + isFragmentShown);
            if (isAddNew) {
                Log.i(TAG, "onBackPressed(): isAddNew = true");

                editInFeatureFragment.onDelete();
            } else {
                Log.i(TAG, "onBackPressed(): isAddNew = false");

                Utilities.showConfirmDialog(this, getResources().getString(R.string.exit), getResources().getString(R.string.exit_without_saving), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        if (checkRequiredFields()) {
                        MapEditorActivity.super.onBackPressed();
                        isFragmentShown = false;
                        isAddNew = false;
//
                        //TODO New
                        fabGeneral.setVisibility(View.VISIBLE);
                        fabLocation.setVisibility(View.VISIBLE);
                        mCompass.setVisibility(View.VISIBLE);
                        fabFullScreen.setVisibility(View.VISIBLE);
                        tvLatLong.setVisibility(View.VISIBLE);

                        findViewById(R.id.linear_layers_info).setVisibility(View.VISIBLE);
                        selectedLayer = null;
                        shapeType = null;
                    }
                });
            }

//            //TODO New
//            fabGeneral.setVisibility(View.VISIBLE);
//            fabLocation.setVisibility(View.VISIBLE);
//            mCompass.setVisibility(View.VISIBLE);
//            fabFullScreen.setVisibility(View.VISIBLE);
//            findViewById(R.id.linear_layers_info).setVisibility(View.VISIBLE);
//            selectedLayer = null;
//            shapeType = null;

        } else if (isInOnlineAddingMode) {
            finishAddingOnline();
        } else {
//            super.onBackPressed();
            showExitDialog();
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.exit))
                .setMessage(getResources().getString(R.string.are_you_sure))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapEditorActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
//        ButterKnife.bind(mCurrent, menu);
//        menuItemIndex = menu.findItem(R.id.item_index);

            menuItemOffline = menu.findItem(R.id.item_go_offline);
            item_load_previous_offline = menu.findItem(R.id.item_load_previous_offline);

            menuItemOnline = menu.findItem(R.id.item_go_online);
//        menuItemSearch = menu.findItem(R.id.item_search);
            menuItemOverflow = menu.findItem(R.id.overflow);
            menuItemSync = menu.findItem(R.id.item_sync);
            menuItemLoad = menu.findItem(R.id.item_load_previous);
            menuItemSatellite = menu.findItem(R.id.item_satellite);
            menuItemGoOfflineMode = menu.findItem(R.id.item_go_offline_mode);
            menuItemGoOnlineMode = menu.findItem(R.id.item_go_online_mode);

            //        menuItemGCS = menu.findItem(R.id.item_gcs);
//        menuItemBaseMap = menu.findItem(R.id.item_base_map);

            if (!GeoDatabaseUtil.isGeoDatabaseLocal()) {
                menuItemLoad.setVisible(false);
            } else {
                menuItemLoad.setVisible(true);
            }
            if (!onlineData) {
                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
//                menuItemIndex.setVisible(false);
//            menuItemGCS.setVisible(false);
                menuItemSatellite.setVisible(false);
//                menuItemBaseMap.setVisible(false);
                menuItemSync.setVisible(true);
                menuItemOnline.setVisible(true);
//            menuItemSearch.setVisible(false);
                menuItemGoOfflineMode.setVisible(false);
                menuItemGoOnlineMode.setVisible(true);
            } else {
                menuItemGoOfflineMode.setVisible(true);
                menuItemGoOnlineMode.setVisible(false);
            }
//            menuItemBaseMap.setChecked(isBaseMapAdded);
            Log.d("Test", "In OnCreate Options Menu");

            if (selectedWorkIndex != null)
                menuItemIndex.setTitle(selectedWorkIndex.getSheetNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            /*case R.id.item_search:
                SearchCase = 1;
                startActionMode(SearchTool.getInstance(this, mapView));
                return true;*/
            case R.id.item_draw_polygon:
                startDrawOnMap();
                return true;
            case R.id.item_current_extent:
                goOffline();
                try {
                    menuItemGoOfflineMode.setVisible(false);
                    menuItemGoOnlineMode.setVisible(true);
//                    GeoDatabaseUtil.addLocalLayers(mapView, this, currentOfflineVersion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_load_previous:
                showOfflineMapsList(this, mapView);
                return true;
            case R.id.item_load_previous_offline:
                showOfflineMapsList(this, mapView);
                return true;
            case R.id.item_go_online:
                GeoDatabaseUtil.goOnline(MapEditorActivity.this, mapView, currentOfflineVersion);
                return true;
            case R.id.item_sync:
                syncData();
                return true;
            case R.id.item_Add_Bookmark:
                showAddNewBookmarkDialog();
                return true;
            case R.id.item_Show_Bookmarks:
                showBookmarksDialog();
                return true;
            /**------------------------------Ali Ussama Update------------------------------------*/
            case R.id.item_go_offline_mode:
                try {
//                    showOfflineMapsList(this, mapView);
                    menuItemGoOfflineMode.setVisible(false);
                    menuItemGoOnlineMode.setVisible(true);
                    goOffline();
//                    GeoDatabaseUtil.addLocalLayers(mapView, this, currentOfflineVersion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_go_online_mode:
                try {
                    menuItemGoOfflineMode.setVisible(true);
                    menuItemGoOnlineMode.setVisible(false);
                    if (!Utilities.isNetworkAvailable(this)) {
                        showNoOfflineMapDialog(this, mapView);
                    } else {
                        GeoDatabaseUtil.finishGoingOnline(this, mapView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            /**------------------------------Ali Ussama Update------------------------------------*/
            case R.id.item_Logout:
                logout();
                return true;
            case R.id.item_satellite:
                if (isInitialized) {
                    if (!isSatelliteAdded) {
                        isSatelliteAdded = true;
                        mapView.addLayer(satelliteMap, 0);
                    } else {
                        if (isBaseMapAdded) { //isGCSAdded ||
                            isSatelliteAdded = false;
                            mapView.removeLayer(satelliteMap);
                        } else {
                            Toast.makeText(this, "One option must be checked!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    menuItemSatellite.setChecked(isSatelliteAdded);
                    mapLayersUpdated();
                } else {
                    Toast.makeText(this, R.string.map_not_loaded_yet, Toast.LENGTH_SHORT).show();
                }
                return true;
//            case R.id.item_gcs:
//                if (isInitialized) {
//                    if (!isGCSAdded) {
//                        isGCSAdded = true;
//                        mapView.addLayer(gcsMap, 0);
////                    gcsSeekBar.setVisibility(View.VISIBLE);
////                    tvLatLong.setVisibility(View.GONE);
////                    Toast t = Toast.makeText(this, "Change Opacity from Second Slider", Toast.LENGTH_LONG);
////                    t.setGravity(Gravity.CENTER, 0, 0);
////                    t.show();
//                    } else {
//                        if (isSatelliteAdded || isBaseMapAdded) {
//                            isGCSAdded = false;
//                            mapView.removeLayer(gcsMap);
////                        if (esriSeekBar.getVisibility() == View.GONE) {
////                            tvLatLong.setVisibility(View.VISIBLE);
////                            baseMapSeekBar.setVisibility(View.GONE);
////                        }
//                        } else {
//                            Toast.makeText(this, "One option must be checked!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    menuItemGCS.setChecked(isGCSAdded);
//                    mapLayersUpdated();
//                } else {
//                    Toast.makeText(this, R.string.map_not_loaded_yet, Toast.LENGTH_SHORT).show();
//                }
//                return true;
            case R.id.item_base_map: {
                if (isInitialized) {
                    if (!isBaseMapAdded) {
                        isBaseMapAdded = true;
//                        mapView.addLayer(baseMap, 0); // TODO
//                    baseMapSeekBar.setVisibility(View.VISIBLE);
//                    tvLatLong.setVisibility(View.GONE);
//                    Toast t = Toast.makeText(this, "Change Opacity from third Slider", Toast.LENGTH_LONG);
//                    t.setGravity(Gravity.CENTER, 0, 0);
//                    t.show();
                    } else {
                        if (isSatelliteAdded) { //|| isGCSAdded
                            isBaseMapAdded = false;
                            try {
//                                mapView.removeLayer(baseMap); // TODO
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                        baseMapSeekBar.setVisibility(View.GONE);
//                        // mapView.removeLayer(baseMap);
//                        if (esriSeekBar.getVisibility() == View.GONE)
//                            tvLatLong.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "One option must be checked!", Toast.LENGTH_SHORT).show();
                        }
                    }
//                    menuItemBaseMap.setChecked(isBaseMapAdded);
                    mapLayersUpdated();
                } else {
                    Toast.makeText(this, R.string.map_not_loaded_yet, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            /*case R.id.item_index:
                if (indices != null) {
                    showIndicesDialog();
                } else {
                    getIndices();
                }
                return true;*/

        }
        return super.onOptionsItemSelected(item);
    }

    DialogSearchIndex dialogSearchIndex;

    private void showIndicesDialog() {
        Log.i(TAG, "showIndicesDialog() : Indices Number : " + indices.size());
        dialogSearchIndex = new DialogSearchIndex(this, indices, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedWorkIndex = ((Index) parent.getItemAtPosition(position));
                Geometry geometryToZoom = GeometryEngine.project(selectedWorkIndex.getGeometry(), SpatialReference.create(SPATIAL_REFERENCE_CODE), mapView.getSpatialReference());
                mapView.setExtent(geometryToZoom);
                refreshPOI();
                dialogSearchIndex.dismiss();
                menuItemIndex.setTitle(((Index) parent.getItemAtPosition(position)).getSheetNumber());
            }
        });

        dialogSearchIndex.show();

    }

    /**
     * Reading and Loading all downloaded map features' titles from SharedPreferences
     * <p>
     * #AliUssama documentation
     */
    public void showOfflineMapsList(final MapEditorActivity context, final MapView mapView) {
        //Declaring List to hold offline database titles
        ArrayList<String> databaseTitles = DataCollectionApplication.getOfflineDatabasesTitle();
        //Declaring List to hold NonNull offline database titles
        ArrayList<String> databaseTitlesWithoutNull = new ArrayList<>();
        //Filtering NonNull database titles
        for (String title : databaseTitles) {
            if (title != null) {
                Log.i(TAG, "showOfflineMapsList(): title " + title + " is not null");
                databaseTitlesWithoutNull.add(title);
            } else {
                Log.i(TAG, "showOfflineMapsList(): title  is null");
            }
        }
        //Handle database's titles not null
        if (databaseTitlesWithoutNull.size() > 0) {
            Log.i(TAG, "showOfflineMapsList() : database titles without null size = " + databaseTitlesWithoutNull.size());
            //declaring array to hold database titles
            String[] titles = new String[databaseTitlesWithoutNull.size()];
            //Converting ArrayList to array
            titles = databaseTitlesWithoutNull.toArray(titles);
            //calling method to display dialog with available offline database titles
            displayTitlesOfflineMapDialog(context, mapView, titles, databaseTitles);
        } else {
            Log.i(TAG, "showOfflineMapsList() : displaying No Offline Map Dialog");

            MapEditorActivity.showNoOfflineMapDialog(context, mapView);
        }
    }

    private void displayTitlesOfflineMapDialog(final MapEditorActivity context, final MapView mapView, final String[] titles, final ArrayList<String> databaseTitles) {
        new AlertDialog.Builder(context)
                .setItems(titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedTitle = titles[which];
                        int selectedVersion = 0;
                        for (int i = 0; i < databaseTitles.size(); i++) {
                            if (selectedTitle.equals(databaseTitles.get(i))) {
                                selectedVersion = i + 1;
                                break;
                            }
                        }

                        Log.i(TAG, "Selected Version: " + selectedVersion);
                        GeoDatabaseUtil.addLocalLayers(mapView, context, selectedVersion);// TODO un comment
                        currentOfflineVersion = selectedVersion; // TODO un comment
//                        GeoDatabaseUtil.setMapExtentFromFeatureExtent(mapView,context,currentOfflineVersion);
//                        GeoDatabaseUtil.loadRaster(mapView, context);
                        refreshPOI();
                    }
                }).setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        if (!Utilities.isNetworkAvailable(context)) {
//                            showNoOfflineMapDialog(context, mapView);
//                        } else {
//                            GeoDatabaseUtil.finishGoingOnline(context, mapView);
//                        }
                    }
                }).show();
    }

    public void mapLayersUpdated() {

        baseMapSeekBar.setVisibility(View.GONE);
        esriSeekBar.setVisibility(View.GONE);
//        gcsSeekBar.setVisibility(View.GONE);

        if (getTiledOrDynamicLayersCount() > 1) {

            if (isBaseMapAdded) {
                baseMapSeekBar.setVisibility(View.VISIBLE);
            }

//            if (isGCSAdded) {
//                gcsSeekBar.setVisibility(View.VISIBLE);
//            }

            if (isSatelliteAdded) {
                esriSeekBar.setVisibility(View.GONE);
            }

            Toast t = Toast.makeText(this, "Change Opacity from Slider Above", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        } else {
            if (isBaseMapAdded) {
                try {
                    baseMap.setOpacity(1);
//                    baseMapOpacity.setProgress(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            else if (isGCSAdded) {
//                gcsMap.setOpacity(1);
//                gcsOpacity.setProgress(100);
//            }
            else {
                // satelliteMap.setOpacity(1);
//                esriOpacity.setProgress(100);
            }
        }
    }

    private int getTiledOrDynamicLayersCount() {
        int count = 0;
        for (Layer layer : mapView.getLayers()) {
            if (layer instanceof ArcGISTiledMapServiceLayer || layer instanceof ArcGISDynamicMapServiceLayer) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.d("onRequest ", userLocation + "");
                    if (userLocation != null) {
                        googleLocation = userLocation;
                        showUserLocationOnMap();
                    } else {
                        if (mGoogleApiClient.isConnected() && !isOnRequestUpdate) {
                            createLocationRequest();
                        }
                    }
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void setOnTapListenerOnMap() {
        mapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {
                Point mapPt = mapView.toMapPoint(x, y);
                if (isInOnlineAddingMode) {
                    Log.i(TAG, "setOnTapListenerOnMap(): is isInOnlineAddingMode");
                    if (shapeType != null) {
                        if (shapeType.equals(POINT)) {
                            mapPt = (Point) GeometryEngine.project(mapPt, mapView.getSpatialReference(), LAYER_SR);
                            geometryToAdd = mapPt;
                            addNewFeature();
                        } else {
                            currentAddedPoints.add(mapPt);
                            prevAddedPoints.clear();
                            drawShape();
                        }
                    } else {
                        Log.i(TAG, "shape is null");

                        Log.i(TAG, "calling getFeatureAndShowEditFragment()");
                        getFeatureAndShowEditFragment(mapPt, -1, false);

//                        Toast.makeText(MapEditorActivity.this, getString(R.string.map_not_loaded), Toast.LENGTH_SHORT).show();


                    }
                } else if (changeLocationOnline) {
                    Log.i("setOnTapListenerOnMap", "changeLocationOnline");
                    mapPt = (Point) GeometryEngine.project(mapPt, mapView.getSpatialReference(), LAYER_SR);
                    Graphic graphic = new Graphic(mapPt, new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.SQUARE));
                    shapeToAdd = new Graphic[]{graphic};
                    finishAddingOnline();
                } else {
                    Log.i(TAG, "Select Feature");
                    mapPt = (Point) GeometryEngine.project(mapPt, mapView.getSpatialReference(), LAYER_SR);

                    Log.i(TAG, "calling getFeatureAndShowEditFragment()");
                    getFeatureAndShowEditFragment(mapPt, -1, false);
                }

            }
        });
    }

    public void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {

            if (manager == null) {
                manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
                Location userLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (userLocation != null) {
                    googleLocation = userLocation;
                }
            }

            if (mGoogleApiClient.isConnected()) {
                if (!isOnRequestUpdate) {
                    //this case when activity is loading for first time and we don't have a location yet
                    // and that means there's no RequestUpdate yet
                    isRequestBefore = false;
                    createLocationRequest();
                } else {
                    //incase we click clicked the location before that means we have lastLocation already so we just get and and displayit On map
                    Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (userLocation != null) {
                        googleLocation = userLocation;
                    }
                }
            } else {
                mGoogleApiClient.connect();
                isRequestBefore = false;
                createLocationRequest();
            }

        }

        showUserLocationOnMap();
    }

    private void getFeatureServiceOffline() {

        for (Layer layer : mapView.getLayers()) {
            if (layer instanceof FeatureLayer) {

                if (layer.getName().matches(getString(R.string.FCL_DISTRIBUTIONBOX_layer_name))) {
                    FCL_DISTRIBUTIONBOXLayerOffline = (FeatureLayer) layer;
                    FCL_DISTRIBUTIONBOXTable = ((GeodatabaseFeatureTable) FCL_DISTRIBUTIONBOXLayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + FCL_DISTRIBUTIONBOXTable.getTableName());

                } else if (layer.getName().matches(getString(R.string.FCL_POLES_layer_name))) {
                    FCL_POLESLayerOffline = (FeatureLayer) layer;
                    FCL_POLESTable = ((GeodatabaseFeatureTable) FCL_POLESLayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + FCL_POLESTable.getTableName());

                } else if (layer.getName().matches(getString(R.string.FCL_RMU_layer_name))) {
                    FCL_RMULayerOffline = (FeatureLayer) layer;
                    FCL_RMUTable = ((GeodatabaseFeatureTable) FCL_RMULayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + FCL_RMUTable.getTableName());

                } else if (layer.getName().matches(getString(R.string.FCL_Substation_layer_name))) {
                    FCL_SubstationLayerOffline = (FeatureLayer) layer;
                    FCL_SubstationTable = ((GeodatabaseFeatureTable) FCL_SubstationLayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + FCL_SubstationTable.getTableName());

                } else if (layer.getName().matches(getString(R.string.OCL_METER_layer_name))) {
                    OCL_METERLayerOffline = (FeatureLayer) layer;
                    OCL_METERTable = ((GeodatabaseFeatureTable) OCL_METERLayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + OCL_METERTable.getTableName());

                } else if (layer.getName().matches(getString(R.string.Service_Point_layer_name))) {
                    Service_PointLayerOffline = (FeatureLayer) layer;
                    Service_PointTable = ((GeodatabaseFeatureTable) Service_PointLayerOffline.getFeatureTable());

                    Log.i(TAG, "getFeatureServiceOffline(): LayerName is " + Service_PointTable.getTableName());

                }
//                if (layer.getName().equals(getString(R.string.point_feature_layer_name))) {
//                    featureLayerPointsOffline = (FeatureLayer) layer;
//                    featureTablePoints = ((GeodatabaseFeatureTable) featureLayerPointsOffline.getFeatureTable());
//                } else if (layer.getName().equals(getString(R.string.line_feature_layer_name))) {
//                    featureLayerLinesOffline = (FeatureLayer) layer;
//                    featureTableLines = ((GeodatabaseFeatureTable) featureLayerLinesOffline.getFeatureTable());
//                } else if (layer.getName().equals(getString(R.string.polygon_feature_layer_name))) {
//                    featureLayerPolygonsOffline = (FeatureLayer) layer;
//                    featureTablePolygons = ((GeodatabaseFeatureTable) featureLayerPolygonsOffline.getFeatureTable());
//                }

            }
        }

    }

    private void showResult(final int id, final HashMap<String, Object> attributes, final AttachmentInfo[] attachmentInfos, final boolean isNewFeature) {
        Log.i(TAG, "showResult(): is called");

        MapEditorActivity.this.runOnUiThread(new Runnable() {

            public void run() {
                Utilities.dismissLoadingDialog();

                if (onlineData) {

                    if (shapeToAdd[0].getGeometry() instanceof Point) {
                        Log.i(TAG, "showResult(): shape to add is point");

                        if (selectedLayer != null) {
                            if (selectedLayer.isInitialized()) {
                                Log.i(TAG, "showResult(): FCL_DISTRIBUTIONBOX  is Initialized");

                                showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                            } else {
                                Log.i(TAG, "showResult(): FCL_DISTRIBUTIONBOX  is Not Initialized");
//                                pointFeatureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
                                selectedLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

                                    public void onStatusChanged(Object source, STATUS status) {
                                        if (status == STATUS.INITIALIZED) {
                                            showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                                        }
                                    }
                                });
                            }
                        }


                    } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {

                        if (lineFeatureLayer.isInitialized()) {
                            showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                        } else {
                            lineFeatureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

                                public void onStatusChanged(Object source, STATUS status) {
                                    if (status == STATUS.INITIALIZED) {
                                        showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                                    }
                                }
                            });
                        }


                    } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {

                        if (polygonFeatureLayer.isInitialized()) {
                            showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                        } else {
                            polygonFeatureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

                                public void onStatusChanged(Object source, STATUS status) {
                                    if (status == STATUS.INITIALIZED) {
                                        showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                                    }
                                }
                            });
                        }

                    }


                } else {
                    showEditingFragment(id, attributes, attachmentInfos, isNewFeature);
                }
            }
        });

    }

    private void hideFragment() {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.rlFragment)).commit();
        rlFragment.setVisibility(View.INVISIBLE);
        isFragmentShown = false;
        isAddNew = false;
    }

    private void setUpAddingNewFeatureOnline() {
        fabGeneral.setVisibility(View.GONE);
//        fabMeasure.setVisibility(View.GONE);
        fabLocation.setVisibility(View.GONE);
        Utilities.showToast(MapEditorActivity.this, getString(R.string.tap_on_map_to_add));
    }

    private void addNewFeature() {

        try {
            Log.i(TAG, "addNewFeature(): is called");
            createShapeToAdd();
            if (Utilities.isNetworkAvailable(MapEditorActivity.this) && onlineData) {
                Utilities.showLoadingDialog(MapEditorActivity.this);
//            if (pointFeatureLayer.isInitialized()) {
                if (selectedLayer.isInitialized()) {
                    Log.i(TAG, "addNewFeature(): FCL_DISTRIBUTIONBOX is Initialized");
                    addFeatureOnLine();
                } else {
//                    pointFeatureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
                    selectedLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

                        public void onStatusChanged(Object source, STATUS status) {
                            if (status == STATUS.INITIALIZED) {
                                addFeatureOnLine();
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(this, shapeType, Toast.LENGTH_SHORT).show();
                try {
//                    if (featureLayerPointsOffline == null || featureLayerPolygonsOffline == null || featureLayerLinesOffline == null) {

                    if (FCL_DISTRIBUTIONBOXLayerOffline == null || FCL_POLESLayerOffline == null || FCL_RMULayerOffline == null ||
                            FCL_SubstationLayerOffline == null || OCL_METERLayerOffline == null || Service_PointLayerOffline == null) {
                        getFeatureServiceOffline();
                    }

                    Geometry envelope;
                    long objId;
                    if (shapeType.equals(POINT)) {

//                        featureTablePoints = ((GeodatabaseFeatureTable) featureLayerPointsOffline.getFeatureTable());
//                        addNewFeatureOfflineOnLayer();

                        selectedTableOffline = ((GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable());
                        objId = selectedLayerOffline.getFeatureTable().addFeature(shapeToAdd[0]);
                        Point pointOfTap = (Point) shapeToAdd[0].getGeometry();
                        Geometry featureExtent = GeometryEngine.project(mapView.getExtent(), mapView.getSpatialReference(), LAYER_SR);
                        double currentMapWidth = featureExtent.calculateLength2D();
//                        double twoPixels = currentMapWidth / 70;
                        double twoPixels = 5;
                        envelope = new Envelope(pointOfTap.getX() - twoPixels, pointOfTap.getY() - twoPixels, pointOfTap.getX() + twoPixels, pointOfTap.getY() + twoPixels);
                    } else if (shapeType.equals(POLYGON)) {
                        featureTablePolygons = ((GeodatabaseFeatureTable) featureLayerPolygonsOffline.getFeatureTable());
                        objId = featureLayerPolygonsOffline.getFeatureTable().addFeature(shapeToAdd[0]);

                        envelope = shapeToAdd[0].getGeometry();
                    } else {
                        featureTableLines = ((GeodatabaseFeatureTable) featureLayerLinesOffline.getFeatureTable());
                        objId = featureLayerLinesOffline.getFeatureTable().addFeature(shapeToAdd[0]);
                        envelope = shapeToAdd[0].getGeometry();
                    }

                    Log.i("AddNewFeature", String.valueOf(objId));

                    isNewFeature = true;

                    SharedPreferences mPreferences = getSharedPreferences(getString(R.string.New_Drawed_Featur_ids), MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putLong(String.valueOf(objId), objId);
                    editor.apply();


                    QueryParameters query = new QueryParameters();
                    query.setOutFields(new String[]{"*"});
                    query.setObjectIds(new long[]{objId});
                    query.setOutSpatialReference(mapView.getSpatialReference());
                    query.setGeometry(envelope);
                    selectFeatureOfflineAndShowDetails(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.error_in_adding_feature));
                }

                finishAddingOnline();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewFeatureOfflineOnLayer() {
        if (selectedLayerOffline.equals(FCL_DISTRIBUTIONBOXLayerOffline)) {
            selectedTableOffline = ((GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable());
        }
    }

    private void createShapeToAdd() {
        Log.i("setOnTapListenerOnMap", "isInOnlineAddingMode");
        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("CREATED_DATE", Utilities.getDateNow());
//        attributes.put(ColumnNames.SURVEYOR_ID, DataCollectionApplication.getSurveyorId());
        if (shapeType.equals(POINT)) {
//            attributes.put(ColumnNames.A_FEATURETYPE, 0); // TODO Old version
            attributes.put(ColumnNames.Code, 25); // TODO New version
            Graphic graphic = new Graphic(geometryToAdd, new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.SQUARE), attributes);
            shapeToAdd = new Graphic[]{graphic};
        } else {
            attributes.put("GN_ID", 0);
            Graphic graphic = new Graphic(geometryToAdd, new SimpleLineSymbol(Color.RED, 10, SimpleLineSymbol.STYLE.SOLID), attributes);
            shapeToAdd = new Graphic[]{graphic};
        }
    }

    private void addFeatureOnLine() {
        if (shapeType.equals(POINT)) {
//            pointFeatureLayer.applyEdits(shapeToAdd, null, null, addShapeCallBack);
            if (selectedLayer.equals(FCL_DISTRIBUTIONBOX)) {
                FCL_DISTRIBUTIONBOX.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            } else if (selectedLayer.equals(FCL_POLES)) {
                FCL_POLES.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            } else if (selectedLayer.equals(FCL_RMU)) {
                FCL_RMU.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            } else if (selectedLayer.equals(FCL_Substation)) {
                FCL_Substation.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            } else if (selectedLayer.equals(OCL_METER)) {
                OCL_METER.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            } else if (selectedLayer.equals(Service_Point)) {
                Service_Point.applyEdits(shapeToAdd, null, null, addShapeCallBack);

            }

        } else {
            if (shapeType.equals(LINE)) {
                lineFeatureLayer.applyEdits(shapeToAdd, null, null, addShapeCallBack);
            } else {
                polygonFeatureLayer.applyEdits(shapeToAdd, null, null, addShapeCallBack);
            }
        }
    }

    private void getFeatureAndShowEditFragment(Geometry pointOfTap, long featureId, final boolean isNew) {
        try {

            Log.i(TAG, "getFeatureAndShowEditFragment(): is called");
            isNewFeature = isNew;

//            LAYER_SR = SpatialReference.create(GLOBAL_SPATIAL_REFERENCE_CODE);

//            Geometry featureExtent = GeometryEngine.project(mapView.getExtent(), LAYER_SR, mapView.getSpatialReference());
            Geometry featureExtent = GeometryEngine.project(mapView.getExtent(), mapView.getSpatialReference(), LAYER_SR);
            double currentMapWidth = featureExtent.calculateLength2D();
//            double twoPixels = currentMapWidth / 70;
            double twoPixels = 5;

            Log.i(TAG, "getFeatureAndShowEditFragment(): current map width = " + currentMapWidth);
            Log.i(TAG, "getFeatureAndShowEditFragment(): two Pixels = " + twoPixels);

            if (onlineData) {
                Log.i(TAG, "getFeatureAndShowEditFragment(): status for query is online");

                Utilities.showLoadingDialog(MapEditorActivity.this);
                onlineQuery = new Query();
                onlineQuery.setOutFields(new String[]{"*"});
                // query.setWhere((ColumnNames.SURVEYOR_ID + " = " + DataCollectionApplication.getSurveyorId()) + " OR " + (ColumnNames.SURVEYOR_ID + " is null " + " OR " + (ColumnNames.SURVEYOR_ID + " = 0 ")));
                onlineQuery.setWhere("1=1");

                if (featureId != -1) {
                    onlineQuery.setObjectIds(new long[]{featureId});
                    Log.i(TAG, "getFeatureAndShowEditFragment(): featureId = " + featureId);
                }
                if (pointOfTap != null) {
                    if (shapeType == null || shapeType.equals(POINT)) {
                        Envelope envelope = new Envelope(((Point) pointOfTap).getX() - twoPixels, ((Point) pointOfTap).getY() - twoPixels, ((Point) pointOfTap).getX() + twoPixels, ((Point) pointOfTap).getY() + twoPixels);
//                        Envelope envelope = new Envelope(((Point) pointOfTap), 1000.0, 1000.0);

                        onlineQuery.setGeometry(envelope);
                        Log.i(TAG, "getFeatureAndShowEditFragment(): geometry is created  x = " + envelope.getCenter().getX() + " Y = " + envelope.getCenter().getY());

                    } else {
                        onlineQuery.setGeometry(pointOfTap);
                    }
                }
                onlineQuery.setSpatialRelationship(SpatialRelationship.INTERSECTS);
//                String [] outfields = new String[]{"Code", "OBJECTID","GlobalID"};
//              query.setOutFields("*");
                if (shapeType == null) {
                    Log.i(TAG, "getFeatureAndShowEditFragment query all callback");

                    queryOnAllLayersOnline(onlineQuery);
                    shapeType = POINT;
                } else if (shapeType.equals(POINT)) {

                    Log.i(TAG, "getFeatureAndShowEditFragment pointFeatureLayer callback");
//                    pointFeatureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                    if (selectedLayer != null) {
                        if (selectedLayer.equals(FCL_DISTRIBUTIONBOX)) {
                            FCL_DISTRIBUTIONBOX.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        } else if (selectedLayer.equals(FCL_POLES)) {
                            FCL_POLES.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        } else if (selectedLayer.equals(FCL_RMU)) {
                            FCL_RMU.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        } else if (selectedLayer.equals(FCL_Substation)) {
                            FCL_Substation.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        } else if (selectedLayer.equals(OCL_METER)) {
                            OCL_METER.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        } else if (selectedLayer.equals(Service_Point)) {
                            Service_Point.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);

                        }
                    } else {
                        queryOnAllLayersOnline(onlineQuery);
                    }
                } else if (shapeType.equals(POLYGON)) {
                    Log.i(TAG, "getFeatureAndShowEditFragment polygonFeatureLayer callback");
                    polygonFeatureLayer.selectFeatures(onlineQuery, ArcGISFeatureLayer.SELECTION_METHOD.NEW, selectFeaturesCallBackOnline);
                }
            } else {
//                double currentMapWidth = mapView.getExtent().calculateLength2D();
//                double fivePixels = currentMapWidth / 70;
                Log.i(TAG, "getFeatureAndShowEditFragment(): status for query is offline");

                QueryParameters query = new QueryParameters();
                query.setOutSpatialReference(mapView.getSpatialReference());
                query.setOutFields(new String[]{"*"});
                if (featureId != -1)
                    query.setObjectIds(new long[]{featureId});
                if (pointOfTap != null) {
                    if (shapeType == null || shapeType.equals(POINT)) {
                        Envelope envelope = new Envelope(((Point) pointOfTap).getX() - twoPixels, ((Point) pointOfTap).getY() - twoPixels, ((Point) pointOfTap).getX() + twoPixels, ((Point) pointOfTap).getY() + twoPixels);
                        query.setGeometry(envelope);
                    } else {
                        query.setGeometry(pointOfTap);
                    }
                }
                query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
                selectFeatureOfflineAndShowDetails(query);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.dismissLoadingDialog();
                    Utilities.showToast(MapEditorActivity.this, e.toString());
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.connection_failed));
                }
            });
        }

    }

    private void queryOnAllLayersOnline(final Query query) {
//        pointFeatureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
        try {

//            FCL_DISTRIBUTIONBOX.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//            FCL_POLES.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//            FCL_RMU.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//            FCL_Substation.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//            OCL_METER.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//            Service_Point.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);

            queryCount = 0;
            FCL_DISTRIBUTIONBOX.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, distributionBoxQueryAllCallBack);
//            FCL_POLES.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, polesQueryAllCallBack);
////            FCL_RMU.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, rmuQueryAllCallBack);
////            FCL_Substation.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, substationQueryAllCallBack);
////            OCL_METER.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, oclMeterQueryAllCallBack);
////            Service_Point.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, servicePointQueryAllCallBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        lineFeatureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);
//        polygonFeatureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, queryAllCallBack);

    }

    private void queryOnAllLayersOffline(final QueryParameters query) {
        queryCount = 0;
        offlineQuery = query;
        mQueryResults = new ArrayList<>();
        FCL_DISTRIBUTIONBOXLayerOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackFCLDistributionBox);

//        featureLayerLinesOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackDistributionBox);
//        featureLayerPointsOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackDistributionBox);
//        featureLayerPolygonsOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, queryAllOfflineCallBackDistributionBox);

    }

    //TODO
    private void processSelectQueryResultOnline(final ArrayList<FeatureSet> listQueryResults) {
        try {
            Log.i(TAG, "processSelectQueryResultOnline(): is called");

            boolean noFeatures = true;

            final ArrayList<String> names = new ArrayList<>();
            final ArrayList<String> nameFeaturesIndex = new ArrayList<>();


            Log.i(TAG, "processSelectQueryResultOnline(): feature code = " + listQueryResults.get(0).getGraphics().length);

            for (int i = 0; i < listQueryResults.size(); i++) {
                FeatureSet queryResults = listQueryResults.get(i);
                if (queryResults.getGraphics().length > 0) {
                    noFeatures = false;
                    for (int j = 0; j < queryResults.getGraphics().length; j++) {

                        String str = String.valueOf("Point Code ( " + queryResults.getGraphics()[j].getAttributes().get(ColumnNames.Code) + " )"); // TODO New version

                        if (str == null || str.isEmpty())
                            str = "No Name";


//                    str += "  ( " + queryResults.getGraphics()[j].getGeometry().getType().name() + " ) ";

                        Log.i(TAG, "processSelectQueryResultOnline(): feature code = " + str);
                        names.add(str);

                        //To Store Feature Index and graphic Index
                        nameFeaturesIndex.add(i + "," + j);
                    }
                }
            }

            if (noFeatures) {
                Log.i(TAG, "processSelectQueryResultOnline(): listQueryResults size = " + listQueryResults.size());

                Log.i(TAG, "processSelectQueryResultOnline(): there is no feature");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "processSelectQueryResultOnline(): dismissing dialog");

                        Utilities.dismissLoadingDialog();
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });
            } else {

                Log.i(TAG, "processSelectQueryResultOnline(): there is feature");
                Log.i(TAG, "processSelectQueryResultOnline(): listQueryResults size = " + listQueryResults.size());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "processSelectQueryResultOnline(): dismissing dialog");

                        Utilities.dismissLoadingDialog();

                        if (names.size() == 1) {
                            showSelectedFeature(listQueryResults.get(0), 0, isNewFeature);
                        } else {

                            String[] array = names.toArray(new String[0]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(MapEditorActivity.this);

                            builder.setTitle("Choose Service To Edit");

                            builder.setCancelable(true);

                            builder.setItems(array, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utilities.showLoadingDialog(MapEditorActivity.this);
                                        }
                                    });

                                    //get Feature Index and graphic Index
                                    String[] str = nameFeaturesIndex.get(which).split(",");
                                    if (str.length == 2) {
                                        int featureIndex = Integer.parseInt(str[0]);
                                        int graphicIndex = Integer.parseInt(str[1]);
                                        shapeType = POINT;
                                        showSelectedFeature(listQueryResults.get(featureIndex), graphicIndex, isNewFeature);
                                    }
                                }
                            });

                            builder.setNegativeButton("Cancel", null);
                            builder.show();

                            shapeType = null;

                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSelectQueryResultOffline(ArrayList<FeatureResult> listQueryResults) {
        try {
            boolean noFeatures = true;

            final ArrayList<String> names = new ArrayList<>();
            final ArrayList<String> nameFeaturesIndex = new ArrayList<>();
            final HashMap<String, Feature> featureHashMap = new HashMap<>();

            try {
                if (listQueryResults != null) {
                    Log.i(TAG, "processSelectQueryResultOffline(): listQueryResult size = " + listQueryResults.size());
                }

                for (int i = 0; i < listQueryResults.size(); i++) {
                    FeatureResult results = listQueryResults.get(i);
                    int j = 0;
                    try {
                        Log.i(TAG, "processSelectQueryResultOffline(): FeatureResult at index " + i + " size = " + listQueryResults.size());
                        Log.i(TAG, "processSelectQueryResultOffline(): FeatureResult at index " + i + " feature Count = " + results.featureCount());
                        Log.i(TAG, "processSelectQueryResultOffline(): FeatureResult at index " + i + " Field Name = " + results.getDisplayFieldName());
                        Log.i(TAG, "processSelectQueryResultOffline(): FeatureResult at index " + i + " Object Id Field Name = " + results.getObjectIdFieldName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (Object element : results) {
                        if (element instanceof Feature) {
                            noFeatures = false;
                            Feature feature = (Feature) element;
                            String str = String.valueOf(feature.getAttributes().get(ColumnNames.Code));
                            if (str == null || str.isEmpty())
                                str = "No Name";

                            str += "  ( " + feature.getGeometry().getType().name() + " ) ";

                            names.add(str);

                            //To Store Feature Index and graphic Index
                            nameFeaturesIndex.add(i + "," + j);
                            featureHashMap.put(i + "," + j, feature);
                        }
                        j++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (noFeatures) {
                Log.i(TAG, "processSelectQueryResultOffline");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (names.size() == 1) {
                            showSelectedFeature(featureHashMap.get(nameFeaturesIndex.get(0)), isNewFeature);

                            mQueryResults = new ArrayList<>(); // TODO New 17/11/2019

                        } else {
                            String[] array = names.toArray(new String[0]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapEditorActivity.this);
                            builder.setTitle("Choose Service To Edit");
                            builder.setCancelable(true);
                            builder.setItems(array, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utilities.showLoadingDialog(MapEditorActivity.this);
                                        }
                                    });
                                    //get Feature Index and graphic Index
                                    String key = nameFeaturesIndex.get(which);
                                    showSelectedFeature(featureHashMap.get(key), isNewFeature);

                                }
                            });
                            builder.setNegativeButton("Cancel", null);
                            builder.show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showSelectedFeature(FeatureSet queryResults, int i, boolean isNew) {

        try {
            Log.i(TAG, "showSelectedFeature(): is called");

//        Log.d(TAG, "Feature found id=" + queryResults.getGraphics()[i].getAttributeValue(pointFeatureLayer.getObjectIdField()));
            shapeToAdd = new Graphic[]{queryResults.getGraphics()[i]};
            MapUtilites.zoomToPoint(mapView, shapeToAdd[0].getGeometry());
            int objId = queryResults.getObjectIds()[i];

            this.isNewFeature = isNew;
            this.queryFeatureResults = queryResults;
            this.selectedIndex = i;
            this.selectedObjectId = objId;

            Utilities.showLoadingDialog(this);
            if (shapeToAdd[0].getGeometry() instanceof Point) {

//            pointFeatureLayer.queryAttachmentInfos(objId, attachmentCallbackOnline);
                Log.i(TAG, "showSelectedFeature(): shape to add is point");
                Log.i(TAG, "showSelectedFeature(): requesting attachments");

                if (selectedLayer != null) {
                    Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
                    final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
//                    selectedLayer = FCL_DISTRIBUTIONBOX;
                    showResult(selectedObjectId, attributes, null, isNewFeature);

                /*
                    if (selectedLayer.equals(FCL_DISTRIBUTIONBOX)) {
                        selectedLayer = FCL_DISTRIBUTIONBOX;

                        FCL_DISTRIBUTIONBOX.queryAttachmentInfos(objId, attachmentCallbackOnline);

                    } else if (selectedLayer.equals(FCL_POLES)) {

                        FCL_POLES.queryAttachmentInfos(objId, attachmentCallbackOnline);

                    } else if (selectedLayer.equals(FCL_RMU)) {
                        FCL_RMU.queryAttachmentInfos(objId, attachmentCallbackOnline);

                    } else if (selectedLayer.equals(FCL_Substation)) {
                        FCL_Substation.queryAttachmentInfos(objId, attachmentCallbackOnline);

                    } else if (selectedLayer.equals(OCL_METER)) {
                        OCL_METER.queryAttachmentInfos(objId, attachmentCallbackOnline);

                    } else if (selectedLayer.equals(Service_Point)) {
                        Service_Point.queryAttachmentInfos(objId, attachmentCallbackOnline);
                    } else {
                        FCL_DISTRIBUTIONBOX.queryAttachmentInfos(objId, distributionBoxAttachmentCallbackOnline);
                        FCL_POLES.queryAttachmentInfos(objId, polesAttachmentCallbackOnline);
                        FCL_RMU.queryAttachmentInfos(objId, rmuAttachmentCallbackOnline);
                        FCL_Substation.queryAttachmentInfos(objId, substationAttachmentCallbackOnline);
                        OCL_METER.queryAttachmentInfos(objId, oclMeterAttachmentCallbackOnline);
                        Service_Point.queryAttachmentInfos(objId, servicePointAttachmentCallbackOnline);
                    }*/
                }
            } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                lineFeatureLayer.queryAttachmentInfos(objId, attachmentCallbackOnline);
                Log.i(TAG, "showSelectedFeature(): shape to add is Polyline");
                Log.i(TAG, "showSelectedFeature(): requesting attachments");
            } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                Log.i(TAG, "showSelectedFeature(): shape to add is Polygon");
                Log.i(TAG, "showSelectedFeature(): requesting attachments");
                polygonFeatureLayer.queryAttachmentInfos(objId, attachmentCallbackOnline);
//            Map<String, Object> map = queryFeatureResults.getGraphics()[selectedIndex].getAttributes();
//            final HashMap<String, Object> attributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);
//            showResult(selectedObjectId, attributes, null, isNewFeature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectFeatureOfflineAndShowDetails(QueryParameters query) {

        Log.i(TAG, "selectFeatureOfflineAndShowDetails(): is called");
        if (featureLayerPointsOffline == null || featureLayerPolygonsOffline == null || featureLayerLinesOffline == null) {
            Log.i(TAG, "selectFeatureOfflineAndShowDetails(): calling getFeatureServiceOffline() ");
            getFeatureServiceOffline();
        }

        if (shapeType == null) {
            shapeType = POINT;
            Log.i(TAG, "selectFeatureOfflineAndShowDetails(): calling queryOnAllLayersOffline() ");
            queryOnAllLayersOffline(query);
        } else if (shapeType.equals(LINE))
            featureLayerLinesOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, selectFeaturesCallBackOffline);
        else if (shapeType.equals(POINT)) {
            Log.i(TAG, "selectFeatureOfflineAndShowDetails(): calling queryOnAllLayersOffline() ");

            queryOnAllLayersOffline(query);

        } else if (shapeType.equals(POLYGON)) {
            featureLayerPolygonsOffline.selectFeatures(query, FeatureLayer.SelectionMode.NEW, selectFeaturesCallBackOffline);
        }
    }

    public void showSelectedFeature(final Feature feature, final boolean isNewFeature) {

        if (feature == null) {
            Log.i(TAG, "showSelectedFeature");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.no_service_found));
                }
            });
        } else {

            this.isNewFeature = isNewFeature;

            Map<String, Object> map = feature.getAttributes();

            Graphic graphic = new Graphic(feature.getGeometry(), new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.SQUARE));
            shapeToAdd = new Graphic[]{graphic};

            this.selectedFeatureAttributes = (map instanceof HashMap) ? (HashMap<String, Object>) map : new HashMap<>(map);

            final int objId = (int) feature.getId();
            this.selectedObjectId = objId;

            if (shapeToAdd[0].getGeometry() instanceof Point) {
//                featureTablePoints.queryAttachmentInfos(objId, attachmentCallbackOffline);
                selectedTableOffline.queryAttachmentInfos(objId, attachmentCallbackOffline); // TODO New 13/11/2019

            } else if (shapeToAdd[0].getGeometry() instanceof Polyline)
                featureTableLines.queryAttachmentInfos(objId, attachmentCallbackOffline);
            else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                featureTablePolygons.queryAttachmentInfos(objId, attachmentCallbackOffline);
            }
        }
    }

    private void showEditingFragment(int id, HashMap<String, Object> attributes, AttachmentInfo[] attachmentInfos, boolean isNewFeature) {
        try {
            Log.i(TAG, "showEditingFragment(): is called");
            Log.i(TAG, "showEditingFragment(): attributes size" + (attributes.isEmpty() ? 0 : attributes.size()));
//            Log.i(TAG, "showEditingFragment(): attachmentInfos size" + (attachmentInfos.length));

            finishAddingOnline();

            rlFragment.setVisibility(View.VISIBLE);
            editInFeatureFragment = EditInFeatureFragment.newInstance(id, attributes, attachmentInfos);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.rlFragment, editInFeatureFragment)
                    .addToBackStack(null)
                    .commit();
            isFragmentShown = true;
            isAddNew = isNewFeature;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishAddingOnline() {
        try {
            shapeType = null;
            if (changeLocationOnline && editInFeatureFragment.getView() != null) {
                editInFeatureFragment.getView().setVisibility(View.VISIBLE);
            }

            btnCancelAddOnline.setVisibility(View.GONE);

            //TODO New
            fabGeneral.setVisibility(View.GONE);
            fabLocation.setVisibility(View.GONE);
            mCompass.setVisibility(View.GONE);
            fabFullScreen.setVisibility(View.GONE);
            tvLatLong.setVisibility(View.GONE);

            findViewById(R.id.linear_layers_info).setVisibility(View.GONE);

//        fabMeasure.setVisibility(View.VISIBLE);
            isInOnlineAddingMode = false;
            changeLocationOnline = false;
            mGraphicsLayerAddShapes.removeAll();
            hideCallout();
            if (addShapeActionMode != null) {
                addShapeActionMode.finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeLocationOnline() {
        Utilities.showToast(this, "Tap to change Location");
        changeLocationOnline = true;
        btnCancelAddOnline.setVisibility(View.VISIBLE);
        fabGeneral.setVisibility(View.GONE);
//        fabMeasure.setVisibility(View.GONE);
        fabLocation.setVisibility(View.GONE);
    }

    void showAddNewBookmarkDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_bookmark_title));
        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_bookmark, null, false);
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.dialog_bookmark_add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });

        builder.setNegativeButton(getString(R.string.dialog_bookmark_cancel), null);
        final android.app.AlertDialog alertDialog = builder.create();
        //Show dialog and launch keyboard
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        alertDialog.show();

        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText editText = ((EditText) dialogView.findViewById(R.id.title_text_input));
                    String titleText = editText.getText().toString().trim();

                    if (!titleText.isEmpty()) {
                        String jsonPoly = GeometryEngine.geometryToJson(mapView.getSpatialReference(), mapView.getExtent());
                        DataCollectionApplication.addBookMark(jsonPoly, titleText);
                        Utilities.showToast(MapEditorActivity.this, getString(R.string.bookmark_added));
                        alertDialog.dismiss();
                    } else {
                        editText.setError(getString(R.string.no_title));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utilities.showToast(MapEditorActivity.this, getString(R.string.bookmark_not_added));
                    alertDialog.dismiss();
                }

            }
        });

    }

    void showBookmarksDialog() {
        ArrayList<BookMark> bookMarks = DataCollectionApplication.getAllBookMarks();

        if (bookMarks.size() > 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_show_bookmarks_title));
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_show_bookmarks, null, false);

            ListView listView = (ListView) dialogView.findViewById(R.id.lvBookmarks);


            builder.setView(dialogView);
            final android.app.AlertDialog alertDialog = builder.create();

            final BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(this, bookMarks, alertDialog);
            listView.setAdapter(bookMarkAdapter);

            alertDialog.show();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {

                        BookMark bookMark = (BookMark) bookMarkAdapter.getItem(position);

                        JsonFactory jsonFactory = new JsonFactory();
                        JsonParser parser = jsonFactory.createJsonParser(bookMark.getJson());

                        Polygon polygon = (Polygon) GeometryEngine.jsonToGeometry(parser).getGeometry();
                        mapView.setExtent(polygon);
                        alertDialog.dismiss();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        } else {
            Utilities.showToast(this, getString(R.string.no_bookmarks));
        }
    }

    private void logout() {
        DataCollectionApplication.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSave(AttributeViewsBuilder listAdapter, String code, String deviceID, String objectID) {
//        if (true) {
        try {
            boolean isGCS = false;
            boolean isTypeField = false;
            boolean hasEdits = false;
            Map<String, Object> attrs = new HashMap<>();
//            for (int i = 0; i < listAdapter.getCount(); i++) {
//                AttributeItem item = (AttributeItem) listAdapter.getItem(i);
//                String value;
//                if (item.getView() != null) {
//                    if (item.getField().getName().equals(ColumnNames.A_FEATURETYPE) || item.getField().getName().equals(ColumnNames.E_FEATURETYPE)) {
////                        Spinner spinner = (Spinner) item.getView();
////                        String typeName = spinner.getSelectedItem().toString();
////                        if (onlineData)
////                            value = FeatureLayerUtils.returnTypeIdFromTypeName(pointFeatureLayer.getTypes(), typeName);
////                        else {
////                            value = FeatureLayerUtils.returnTypeIdFromTypeName(featureTablePoints.getFeatureTypes().toArray(new FeatureType[0]), typeName);
////                        }
//                        if (AttributeViewsBuilder.selectedCode == null)
//                            value = "30";
//                        else
//                            value = AttributeViewsBuilder.selectedCode;
//                        isTypeField = true;
//
//                    } else if (item.getField().getName().equals(ColumnNames.A_FEATURE)) {
//                        Spinner spinner = (Spinner) item.getView();
//                        if (spinner.getSelectedItem() != null && AttributeViewsBuilder.featureCodeValues != null) {
//                            String typeName = spinner.getSelectedItem().toString();
//                            value = AttributeViewsBuilder.featureCodeValues.get(typeName);
//                            if (value == null)
//                                value = "0";
//                        } else {
//                            value = "0";
//                        }
//                    } else if (item.getField().getName().equals(ColumnNames.A_PROVINCE)
//                            || item.getField().getName().equals(ColumnNames.A_DATASOURCE)
//                            || item.getField().getName().equals(ColumnNames.A_NAMESTATUS)) {
//                        Spinner spinner = (Spinner) item.getView();
//                        if (spinner.getSelectedItem() != null) {
//                            value = spinner.getSelectedItem().toString();
//                        } else {
//                            value = "";
//                        }
//                    } else if (item.getField().getName().equals(ColumnNames.ADMIN_NOTES)) {
//                        if (item.getView().getVisibility() == View.VISIBLE) {
//                            TextView textView = (TextView) item.getView().findViewById(R.id.tvAdminNotes);
//                            value = textView.getText() + "";
//                        } else
//                            value = "";
//                    } else if (item.getField().getName().equals(ColumnNames.SURVEYOR_ID)) {
//                        EditText editText = (EditText) item.getView();
//                        value = editText.getText().toString();
//                        if (value.isEmpty()) {
//                            value = "";
//                            isGCS = true;
//                        }
//                    } else if (FeatureLayerUtils.FieldType.determineFieldType(item.getField()) == FieldType.DATE) {
//                        Button dateButton = (Button) item.getView();
//                        value = dateButton.getText().toString();
//                    } else {
//                        EditText editText = (EditText) item.getView();
//                        value = editText.getText().toString();
//                    }
//
//                    if (value == null)
//                        value = "";
//
//                    boolean hasChanged = FeatureLayerUtils.setAttribute(attrs, shapeToAdd[0], item.getField(), value);
//
//                    if (hasChanged) {
//                        Log.d(TAG, "Change found for field=" + item.getField().getName() + " value = " + value + " applyEdits() will be called");
//                        hasEdits = true;
//                    }
//                    if (isTypeField) {
//                        isTypeField = false;
//                    }
//                }
//            }

//            if (hasEdits) {
            Utilities.showLoadingDialog(MapEditorActivity.this);

            attrs.put(ColumnNames.Code, code);
//            attrs.put(ColumnNames.GlobalID, GlobalID);
            attrs.put(ColumnNames.Device_No, deviceID);
//                if (isGCS)
//                    attrs.put(ColumnNames.CHECK_SURVEYOR, DataCollectionApplication.getSurveyorId());
            if (onlineData) {
                attrs.put(selectedLayer.getObjectIdField(), objectID);

//                    Graphic geometry = shapeToAdd[0];
//                    geometry.getAttributes().get("");
//                    Graphic graphic = new Graphic()

                Graphic newGraphic = new Graphic(shapeToAdd[0].getGeometry(), null, attrs);


                if (shapeToAdd[0].getGeometry() instanceof Point) {

                    if (selectedLayer.equals(FCL_DISTRIBUTIONBOX)) {
                        Log.i(TAG, "onSave(): FCL_DISTRIBUTIONBOX.applyEdits ");
                        FCL_DISTRIBUTIONBOX.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);

                    } else if (selectedLayer.equals(FCL_POLES)) {
                        Log.i(TAG, "onSave(): FCL_POLES.applyEdits ");
                        FCL_POLES.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);

                    } else if (selectedLayer.equals(FCL_RMU)) {
                        Log.i(TAG, "onSave(): FCL_RMU.applyEdits ");
                        FCL_RMU.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);

                    } else if (selectedLayer.equals(FCL_Substation)) {
                        Log.i(TAG, "onSave(): FCL_Substation.applyEdits ");
                        FCL_Substation.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);

                    } else if (selectedLayer.equals(OCL_METER)) {
                        Log.i(TAG, "onSave(): OCL_METER.applyEdits ");
                        OCL_METER.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);

                    } else if (selectedLayer.equals(Service_Point)) {
                        Log.i(TAG, "onSave(): Service_Point.applyEdits ");
                        Service_Point.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);
                    }

                } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                    lineFeatureLayer.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);
                } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                    polygonFeatureLayer.applyEdits(null, null, new Graphic[]{newGraphic}, editOrDeleteInShapeCallback);
                }


            } else {

                try {
                    attrs.put(selectedTableOffline.getObjectIdField(), shapeToAdd[0].getAttributes().get("OBJECTID"));

                    if (shapeToAdd[0].getGeometry() instanceof Point) {

//                        featureTablePoints.updateFeature((long) listAdapter.attributes.get(featureTableLines.getObjectIdField()), attrs, shapeToAdd[0].getGeometry());
                        selectedTableOffline.updateFeature((long) listAdapter.attributes.get(selectedTableOffline.getObjectIdField()), attrs, shapeToAdd[0].getGeometry());

                    } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                        featureTableLines.updateFeature((long) listAdapter.attributes.get(featureTableLines.getObjectIdField()), attrs, shapeToAdd[0].getGeometry());
                    } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                        featureTablePolygons.updateFeature((long) listAdapter.attributes.get(featureTableLines.getObjectIdField()), attrs, shapeToAdd[0].getGeometry());
                    }

                } catch (TableException e) {
                    e.printStackTrace();
                }
                Utilities.dismissLoadingDialog();
                hideFragment();

                String typeToRefresh = null;
                if (shapeToAdd[0].getGeometry() instanceof Point) {
                    typeToRefresh = POINT;
                } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                    typeToRefresh = LINE;
                } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                    typeToRefresh = POLYGON;
                }
                refreshPOI();
            }
//            } else {
//                hideFragment();
//                String typeToRefresh = null;
//                if (shapeToAdd[0].getGeometry() instanceof Point) {
//                    typeToRefresh = POINT;
//                } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
//                    typeToRefresh = LINE;
//                } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
//                    typeToRefresh = POLYGON;
//                }
//                refreshPOI();
//            }

            changeLocationOnline = false;
//        }
        } catch (Exception e) {
            Log.i(TAG, "onSave(): exception " + e.getMessage());
            e.printStackTrace();

        }
    }

    @Override
    public void onDelete(final int featureId) {
        deleteFeature(featureId);
    }

    public void goOffline() {
// get By Hanan 332457.7771338887 this value old version
// get By Ali 700000.00 this value latest version 26/9/2018
//        if (!onlineData || mapView.getScale() <= 700000.0) {


        final Dialog openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.dialog_local_db_name);

        final EditText databaseName = (EditText) openDialog.findViewById(R.id.local_db_name);
        Button dialogCloseButton = (Button) openDialog.findViewById(R.id.local_db_download);

        dialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localDatabaseTitle = databaseName.getText().toString();
                if (localDatabaseTitle.equals("")) {
                    databaseName.setError(getString(R.string.name_validation));
                } else {
                    openDialog.dismiss();
                    // goOffline();
                    try {
                        Log.d(TAG, "Going offline ....");
                        //Async task
                        new ConnectToServer().execute(DOWNLOAD_GEO_DATABASE);
                    } catch (Exception e) {
                        Log.d(TAG, "Error in Going offline");
                        e.printStackTrace();
                    }
                }

            }
        });
        openDialog.setCancelable(true);
        openDialog.show();


//        } else {
//            Utilities.showToast(this, getString(R.string.zoom_more));
//        }
    }

    public void syncData() {
        try {

            Log.i(TAG, "Syncing With Server ....");
            new ConnectToServer().execute(SYNC_WITH_SERVER);

        } catch (Exception e) {
            Log.i(TAG, "Error in Syncing With Server");
            e.printStackTrace();
        }
    }

    private void deleteFeature(final int featureId) {

        if (onlineData) {
            Utilities.showLoadingDialog(MapEditorActivity.this);
            Graphic graphic = shapeToAdd[0];
            if (shapeToAdd[0].getGeometry() instanceof Point) {
                pointFeatureLayer.applyEdits(null, new Graphic[]{graphic}, null, editOrDeleteInShapeCallback);
            } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                lineFeatureLayer.applyEdits(null, new Graphic[]{graphic}, null, editOrDeleteInShapeCallback);
            } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                polygonFeatureLayer.applyEdits(null, new Graphic[]{graphic}, null, editOrDeleteInShapeCallback);
            }

        } else {
            try {

                if (shapeToAdd[0].getGeometry() instanceof Point) {
                    featureTablePoints.deleteFeature(featureId);
                } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                    featureTableLines.deleteFeature(featureId);
                } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                    featureTablePolygons.deleteFeature(featureId);
                }
                Utilities.showLoadingDialog(this);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Feature feature = null;
                        try {
                            if (shapeToAdd[0].getGeometry() instanceof Point) {
                                feature = featureTablePoints.getFeature(featureId);
                            } else if (shapeToAdd[0].getGeometry() instanceof Polyline) {
                                feature = featureTableLines.getFeature(featureId);
                            } else if (shapeToAdd[0].getGeometry() instanceof Polygon) {
                                feature = featureTablePolygons.getFeature(featureId);
                            }
                        } catch (TableException e) {
                            e.printStackTrace();
                        }

                        if (feature != null)
                            new Handler().postDelayed(this, 1000);
                        else {
                            editOrDeleteInShapeCallback.onCallback(null);
                        }
                    }
                }, 1000);
            } catch (TableException e) {
                e.printStackTrace();
            }
        }
    }

    private void measure() {

        Unit[] linearUnits = new Unit[]{
                Unit.create(LinearUnit.Code.METER),
                Unit.create(LinearUnit.Code.KILOMETER),
                Unit.create(LinearUnit.Code.CENTIMETER),
                Unit.create(LinearUnit.Code.INCH),
                Unit.create(LinearUnit.Code.FOOT),
                Unit.create(LinearUnit.Code.YARD),
                Unit.create(LinearUnit.Code.MILE_STATUTE)
        };

        Unit[] areaUnits = new Unit[]{
                Unit.create(AreaUnit.Code.SQUARE_KILOMETER),
                Unit.create(AreaUnit.Code.SQUARE_METER),
                Unit.create(AreaUnit.Code.SQUARE_FOOT),
                Unit.create(AreaUnit.Code.SQUARE_MILE_STATUTE)
        };

        SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 20, SimpleMarkerSymbol.STYLE.DIAMOND);
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.BLACK, 6);
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.argb(100, 0, 225, 255));
        fillSymbol.setOutline(new SimpleLineSymbol(Color.TRANSPARENT, 0));

        // create the tool, required.
        CustomMeasuringTool measuringTool = new CustomMeasuringTool(mapView);

        // customize the tool, optional.
        measuringTool.setLinearUnits(linearUnits);
        measuringTool.setAreaUnits(areaUnits);
        measuringTool.setMarkerSymbol(markerSymbol);
        measuringTool.setLineSymbol(lineSymbol);
        measuringTool.setFillSymbol(fillSymbol);

        // fire up the tool, required.
        startActionMode(measuringTool);


    }

    private void startDrawOnMap() {
        poly = null;
        isInDrawMood = true;
        drawGraphicLayer.removeAll();
        //Instantiate object fron MyTouchListener that will handle drawing on map in MyTouchListener of the map
        MyTouchListener myListener = new MyTouchListener(this, mapView);
        mapView.setOnTouchListener(myListener);

        fabGeneral.setVisibility(View.GONE);
//        fabMeasure.setVisibility(View.GONE);
        fabLocation.setVisibility(View.GONE);
        tvLatLong.setVisibility(View.GONE);
        Utilities.showToast(this, "Draw Area On Map");

        drawToolsActionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.add(0, 0, 0, R.string.clear);
//                item.setIcon(R.drawable.ic_clear_white_24dp);
                item.setVisible(true);

                item = menu.add(0, 1, 1, R.string.download);
//                item.setIcon(R.drawable.ic_cloud_download_white_24dp);
                item.setVisible(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        drawGraphicLayer.removeAll();
                        poly = null;
                        break;
                    case 1:
                        downloadTheDrawnPolygon();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (isInDrawMood) {
                    isInDrawMood = false;
                    endDrawOnMap();
                }
            }
        });
    }

    public void endDrawOnMap() {
        if (isInDrawMood) {
            isInDrawMood = false;
            drawToolsActionMode.finish();
        }
        poly = null;
        fabGeneral.setVisibility(View.VISIBLE);
        fabLocation.setVisibility(View.VISIBLE);
//        fabMeasure.setVisibility(View.VISIBLE);
        tvLatLong.setVisibility(View.VISIBLE);
        drawGraphicLayer.removeAll();
        ClearMyTouchListener clearMyTouchListener = new ClearMyTouchListener(this, mapView);
        mapView.setOnTouchListener(clearMyTouchListener);
        setOnTapListenerOnMap();
    }

    private void downloadTheDrawnPolygon() {
        if (poly != null) {
            goOffline();
        } else {
            Utilities.showToast(this, getString(R.string.draw_on_map));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Test", "on Connected");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (userLocation != null) {
                googleLocation = userLocation;
                showUserLocationOnMap();
            } else {
                if (mGoogleApiClient.isConnected() && !isOnRequestUpdate) {
                    createLocationRequest();
                }
            }
        }
    }

    protected void createLocationRequest() {
        if (!isRequestBefore) {
            isRequestBefore = true;
//getting the last location of the user
            @SuppressLint("MissingPermission")
            Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (userLocation != null) {
                googleLocation = userLocation;
                showUserLocationOnMap();
            }


// to get updates of the location incase the user is moved on from his last location
            final LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            builder.setAlwaysShow(true);
            //check if location setting if satisfied or not
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());

            //location setting cases
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
//                final LocationSettingsStates  = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location requests here.
                            if (ActivityCompat.checkSelfPermission(MapEditorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MapEditorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MapEditorActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                            } else {
                                isOnRequestUpdate = true;
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapEditorActivity.this);
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a activity with theme dialog
                            if (onlineData) {
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(MapEditorActivity.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            break;
                    }

                }
            });
        }
    }

    public void showUserLocationOnMap() {
        if (googleLocation != null) {
            mLocation = MapUtilites.getMapPoint(mapView, googleLocation);
            if (userLocationGraphicLayer != null) {

                Log.i(TAG, "showUserLocationOnMap(): user Location Graphic Layer is initialized");
                Log.i(TAG, "showUserLocationOnMap(): location lat = " + mLocation.getY() + " Lang = " + mLocation.getX());

                Point mapPoint = (Point) GeometryEngine.project(mLocation, SpatialReference.create(GLOBAL_SPATIAL_REFERENCE_CODE), mapView.getSpatialReference());
                Graphic graphic = new Graphic(mapPoint, locationSymbol);
                userLocationGraphicLayer.removeAll();
                userLocationGraphicLayer.addGraphic(graphic);

//                MapUtilites.zoomToPoint(mapView, mapPoint);
            } else {
                Log.i(TAG, "showUserLocationOnMap(): user Location Graphic Layer is null");

            }
            tvLatLong.setVisibility(View.VISIBLE);
            float accuracy = googleLocation.getAccuracy();
            tvLatLong.setText("WGS84 (" + new DecimalFormat("##.######").format(googleLocation.getLatitude()) + ","
                    + new DecimalFormat("##.######").format(googleLocation.getLongitude()) + ")"
                    + "    Accuracy: " + accuracy);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Test", "on Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("Test", "On Connection Failed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location", location + "");
        googleLocation = location;

        showUserLocationOnMap();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("fun", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        tvLatLong.setVisibility(View.VISIBLE);
        tvLatLong.setText(R.string.loading_location);
//        tvLatLong.setText("Hanan...");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("fn", "onProviderDisabled");
        if (isOnRequestUpdate) {
            isRequestBefore = false;
            isOnRequestUpdate = false;
        }
        tvLatLong.setVisibility(View.GONE);
        manager.removeUpdates(this);
        manager = null;
        showOpenGPSDialog();
    }

    private void showOpenGPSDialog() {
        new AlertDialog.Builder(this)
                .setTitle("GPS")
                .setMessage(R.string.gps_setting)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !isOnRequestUpdate) {
            createLocationRequest();
        } else if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    protected void stopLocationUpdates() {
        if (isOnRequestUpdate) {
            isRequestBefore = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapEditorActivity.this);
        }
    }

    public void refreshPOI() {
        try {
            newLines = 0;
            editLines = 0;
            gcsLines = 0;
            newPolygons = 0;
            editPolygons = 0;
            gcsPolygons = 0;
            newPoints = 0;
            editPoints = 0;
            gcsPoints = 0;
            shapeType = null;
            updateFeatureNumbers();

            //TODO New
            fabGeneral.setVisibility(View.VISIBLE);
            fabLocation.setVisibility(View.VISIBLE);
            mCompass.setVisibility(View.VISIBLE);
            fabFullScreen.setVisibility(View.VISIBLE);
            findViewById(R.id.linear_layers_info).setVisibility(View.VISIBLE);

            if (Utilities.isNetworkAvailable(this) && onlineData) {
              /*  if (indices == null || indices.size() == 0) {
                    getIndices();
                } else if (selectedWorkIndex == null) {
                    Toast.makeText(this, R.string.choose_index_to_work, Toast.LENGTH_SHORT).show();
                } else {
                    getFeatures();
                }*/

            } else {

                pointFeatuersGraphicsLayer.removeAll();
                polygonFeatuersGraphicsLayer.removeAll();
                lineFeatuersGraphicsLayer.removeAll();
                QueryParameters query = new QueryParameters();
                query.setOutFields(new String[]{"*"});
                query.setWhere("1=1");
                query.setOutSpatialReference(mapView.getSpatialReference());
                query.setReturnGeometry(true);

                if (featureTablePoints == null)
                    Log.i(TAG, "featureTablePoints is null");
                if (featureTableLines == null)
                    Log.i(TAG, "featureTableLines is null");
                if (featureTablePolygons == null)
                    Log.i(TAG, "featureTablePolygons is null");


                FCL_DISTRIBUTIONBOXTable.queryFeatures(query, callbackTableOfflineQueryFCLDistributionBox);
                FCL_POLESTable.queryFeatures(query, callbackTableOfflineQueryFCLPoles);
                FCL_RMUTable.queryFeatures(query, callbackTableOfflineQueryFCLRMU);
                FCL_SubstationTable.queryFeatures(query, callbackTableOfflineQueryFCLSubstation);
                OCL_METERTable.queryFeatures(query, callbackTableOfflineQueryOCLMeter);
                Service_PointTable.queryFeatures(query, callbackTableOfflineQueryServicePoint);

//                featureTablePoints.queryFeatures(query, callbackOfflineQuery);
//                featureTableLines.queryFeatures(query, callbackOfflineQuery);
//                featureTablePolygons.queryFeatures(query, callbackOfflineQuery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIndices() {
        AsyncQueryTaskForIndices taskForIndices = new AsyncQueryTaskForIndices();
        QueryParameters qParameters = new QueryParameters();
        qParameters.setOutFields(new String[]{"*"});
        qParameters.setReturnGeometry(true);
        qParameters.setWhere("1=1");
        taskForIndices.execute(qParameters);
    }

    private void getFeatures() {

        if (labelsLayer != null)
            labelsLayer.removeAll();
        AsyncQueryTaskForPoints taskForPOI = new AsyncQueryTaskForPoints();
//        AsyncQueryTaskForLines taskForLines = new AsyncQueryTaskForLines();
//        AsyncQueryTaskForPolygons taskForPolygons = new AsyncQueryTaskForPolygons();


        QueryParameters qParameters = new QueryParameters();
        // qParameters.setOutFields(new String[]{ColumnNames.SURVEYOR_ID, ColumnNames.CHECK_SURVEYOR, "ENGLISHN"});
        qParameters.setOutFields(new String[]{"*"});

        qParameters.setReturnGeometry(true);
        // qParameters.setWhere((ColumnNames.SURVEYOR_ID + " = " + DataCollectionApplication.getSurveyorId()) + " OR " + (ColumnNames.SURVEYOR_ID + " is null " + " OR " + (ColumnNames.SURVEYOR_ID + " = 0 ")));
        qParameters.setWhere("1=1");

        qParameters.setGeometry(selectedWorkIndex.getGeometry());
        qParameters.setOutSpatialReference(mapView.getSpatialReference());

//        if (shapeType == null) {
        taskForPOI.setGetAnotherFeatures(true);
        taskForPOI.execute(qParameters);
//        } else if (shapeType.equals(POINT)) {
//            taskForPOI.execute(qParameters);
//        } else if (shapeType.equals(LINE)) {
//            taskForLines.execute(qParameters);
//        } else {
//            taskForPolygons.execute(qParameters);
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            Log.d("test", "In On New Intent");
            super.onNewIntent(intent);
            if (intent != null) {
                if (intent.getAction().equals("Notification")) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        String featureId = intent.getExtras().getString("OBJECTID");
                        if (featureId != null && !featureId.isEmpty()) {
                            if (pointFeatureLayer != null) {
                                Log.i("onNewIntent", "calling getFeatureAndShowEditFragment()");
                                getFeatureAndShowEditFragment(null, Long.parseLong(featureId), false);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllGraphicLayers() {

        try {
            if (lineFeatuersGraphicsLayer == null) {
                mapInitialized();
            } else {
                lineFeatuersGraphicsLayer.removeAll();
                pointFeatuersGraphicsLayer.removeAll();
                polygonFeatuersGraphicsLayer.removeAll();
                labelsLayer.removeAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (buttonView.getId() == R.id.cb_point_layer) {
//            pointFeatuersGraphicsLayer.setVisible(isChecked);
//            labelsLayer.setVisible(isChecked);
//        } else if (buttonView.getId() == R.id.cb_line_layer) {
//            lineFeatuersGraphicsLayer.setVisible(isChecked);
//        } else {
//            polygonFeatuersGraphicsLayer.setVisible(isChecked);
//        }
    }


    private class ConnectToServer extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                if (params[0].equals(SYNC_WITH_SERVER)) {
                    Log.i("cases", "One");
                    //Sync case
                    GeoDatabaseUtil.synchronize(MapEditorActivity.this, mapView, false, currentOfflineVersion);
                } else if (params[0].equals(DOWNLOAD_GEO_DATABASE)) {
                    Log.i("cases", "Two");
                    //Download GeoDatabase
                    GeoDatabaseUtil.downloadData(MapEditorActivity.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "Error in Show Database");
                Utilities.showToast(MapEditorActivity.this, getString(R.string.download_map_again));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class CustomMeasuringTool extends MeasuringTool {
        int pointNumbers;

        @Override
        public void onSingleTap(float x, float y) {
            super.onSingleTap(x, y);
            if (pointNumbers == 0) {
                Toast.makeText(MapEditorActivity.this, "اختر نقطه اخري علي الخريطه", Toast.LENGTH_LONG).show();
            } else if (pointNumbers == 1) {
                Toast.makeText(MapEditorActivity.this, "يمكنك تعديل وحده القياس من اعلي", Toast.LENGTH_LONG).show();
            }
            pointNumbers++;

        }

        public CustomMeasuringTool(MapView map) {
            super(map);
            pointNumbers = 0;
            Toast.makeText(MapEditorActivity.this, "اختر نقطه علي الخريطه", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            super.onDestroyActionMode(mode);
            fabLocation.setVisibility(View.VISIBLE);
            fabGeneral.setVisibility(View.VISIBLE);
//            fabMeasure.setVisibility(View.VISIBLE);
        }
    }

    private class MyTouchListener extends MapOnTouchListener {

        String type = "POLYGON";
        Point startPoint = null;

        public MyTouchListener(Context context, MapView view) {
            super(context, view);
        }

        public boolean onSingleTap(MotionEvent e) {
            if (type.length() > 1 && type.equalsIgnoreCase("POINT")) {
                drawGraphicLayer.removeAll();
                Graphic graphic = new Graphic(mapView.toMapPoint(new Point(e.getX(), e.getY())), new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE));
                //graphic.setGeometry();
                drawGraphicLayer.addGraphic(graphic);

                return true;
            }
            return false;

        }

        public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {

            if (type.length() > 1 && type.equalsIgnoreCase("POLYGON")) {

                Point mapPt = mapView.toMapPoint(to.getX(), to.getY());

                if (startPoint == null) {
                    drawGraphicLayer.removeAll();

                    poly = new Polygon();

                    startPoint = mapView.toMapPoint(from.getX(), from.getY());

                    poly.startPath((float) startPoint.getX(), (float) startPoint.getY());

                }

                poly.lineTo((float) mapPt.getX(), (float) mapPt.getY());


                Graphic graphic = new Graphic(poly, new SimpleMarkerSymbol(Color.parseColor("#90000000"), 10, SimpleMarkerSymbol.STYLE.CIRCLE));

                drawGraphicLayer.addGraphic(graphic);
                return true;

            }
            return super.onDragPointerMove(from, to);
        }

        @Override
        public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
            if (type.length() > 1 && type.equalsIgnoreCase("POLYGON")) {
                if (type.equalsIgnoreCase("POLYGON")) {
                    poly.lineTo((float) startPoint.getX(), (float) startPoint.getY());
                    drawGraphicLayer.removeAll();
                    drawGraphicLayer.addGraphic(new Graphic(poly, new SimpleFillSymbol(Color.parseColor("#88000000"))));
                }

                Graphic graphic = new Graphic(poly, new SimpleLineSymbol(Color.BLUE, 1));
                drawGraphicLayer.addGraphic(graphic);
                startPoint = null;

//                Envelope env = new Envelope();
//                Envelope NewEnv = new Envelope();
//                for (int i : drawGraphicLayer.getGraphicIDs()) {
//                    Polygon p = (Polygon) drawGraphicLayer.getGraphic(i).getGeometry();
//                    p.queryEnvelope(env);
//                    NewEnv.merge(env);
//                }
//
//                Log.d("Test", "Graphic Extent = " + NewEnv.getXMin());
//                Log.d("Test", "Map Extent = " + mapView.getExtent().getPoint(0).getX());
//                mapView.setExtent(NewEnv);

                mapView.setExtent(poly);

                return true;
            }
            return super.onDragPointerUp(from, to);
        }

    }

    private class ClearMyTouchListener extends MapOnTouchListener {

        String type = "POLYGON";
        Point startPoint = null;

        public ClearMyTouchListener(Context context, MapView view) {
            super(context, view);
        }

        public String getType() {
            return this.type;
        }

        public void setType(String geometryType) {
            this.type = geometryType;
        }

    }

    private class AsyncQueryTaskForPoints extends AsyncTask<QueryParameters, Void, FeatureResult> {

        private boolean getAnotherFeatures;
        private QueryParameters qParameters;

        public void setGetAnotherFeatures(boolean getAnotherFeatures) {
            this.getAnotherFeatures = getAnotherFeatures;
        }

        @Override
        protected void onPreExecute() {
            if (!this.getAnotherFeatures)
                Utilities.showLoadingDialog(MapEditorActivity.this);
        }

        @Override
        protected FeatureResult doInBackground(QueryParameters... qParameters) {

            this.qParameters = qParameters[0];
            this.qParameters.setSpatialRelationship(SpatialRelationship.CONTAINS);

            String url = getString(R.string.point_feature_layer_services);
            QueryTask qTask = null;
            try {
                qTask = new QueryTask(url, featureServiceToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FeatureResult results = null;

            try {
                if (qTask != null) {
                    results = qTask.execute(qParameters[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            pointFeatuersGraphicsLayer.removeAll();
            if (results != null) {
                int i = 0;
                for (Object element : results) {
                    if (element instanceof Feature) {
                        try {
                            final Feature feature = (Feature) element;
                            pointFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                            i++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("Number", "Point: " + i);
            }

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                String featureId = getIntent().getExtras().getString("OBJECTID");
                if (featureId != null) {
                    Log.i(TAG, "AsyncQueryTaskForPoints(): calling getFeatureAndShowEditFragment()");
                    getFeatureAndShowEditFragment(null, Long.parseLong(featureId), false);
                }
            }

            if (this.getAnotherFeatures) {
                AsyncQueryTaskForLines taskForLines = new AsyncQueryTaskForLines();
                taskForLines.setGetAnotherFeatures(true);
                taskForLines.execute(qParameters);
            } else {
                Utilities.dismissLoadingDialog();
            }
//            shapeType = null;
        }

    }

    private class AsyncQueryTaskForIndices extends AsyncTask<QueryParameters, Void, FeatureResult> {

        @Override
        protected void onPreExecute() {
            Utilities.showLoadingDialog(MapEditorActivity.this);
        }

        @Override
        protected FeatureResult doInBackground(QueryParameters... qParameters) {

//            String url = getString(R.string.index_services);
            String url = getString(R.string.FCL_DISTRIBUTIONBOX);
            QueryTask qTask = null;
            try {
                qTask = new QueryTask(url, featureServiceToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FeatureResult results = null;

            try {
                if (qTask != null) {
                    results = qTask.execute(qParameters[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            if (results != null) {
                indices = new ArrayList<>();
                for (Object element : results) {
                    if (element instanceof Feature) {
                        try {
                            final Feature feature = (Feature) element;

                            Log.i(TAG, "onPostExecute" + String.valueOf(feature.getAttributeValue("Device_No ")));

                            indices.add(new Index(feature.getGeometry(), String.valueOf(feature.getAttributeValue("SHEET_NO"))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Utilities.dismissLoadingDialog();
//            shapeType = null;
        }

    }

    private class AsyncQueryTaskForLines extends AsyncTask<QueryParameters, Void, FeatureResult> {
        private boolean getAnotherFeatures;
        private QueryParameters qParameters;

        public void setGetAnotherFeatures(boolean getAnotherFeatures) {
            this.getAnotherFeatures = getAnotherFeatures;
        }

        @Override
        protected void onPreExecute() {
            if (!this.getAnotherFeatures)
                Utilities.showLoadingDialog(MapEditorActivity.this);
        }

        @Override
        protected FeatureResult doInBackground(QueryParameters... qParameters) {

            this.qParameters = qParameters[0];
            this.qParameters.setSpatialRelationship(SpatialRelationship.INTERSECTS);
            String url = getString(R.string.line_feature_layer_services);
            QueryTask qTask = null;
            try {
                qTask = new QueryTask(url, featureServiceToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FeatureResult results = null;

            try {
                if (qTask != null) {
                    results = qTask.execute(qParameters[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            lineFeatuersGraphicsLayer.removeAll();
            if (results != null) {
                int i = 0;
                for (Object element : results) {
                    if (element instanceof Feature) {
                        try {
                            final Feature feature = (Feature) element;
                            lineFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                            i++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("Number", "Line: " + i);
            }
            if (this.getAnotherFeatures) {
                AsyncQueryTaskForPolygons taskForPolygons = new AsyncQueryTaskForPolygons();
                taskForPolygons.setGetAnotherFeatures(true);
                taskForPolygons.execute(qParameters);
            } else {
                Utilities.dismissLoadingDialog();
            }
//            shapeType = null;
        }
    }

    private class AsyncQueryTaskForPolygons extends AsyncTask<QueryParameters, Void, FeatureResult> {

        private boolean getAnotherFeatures;
        private QueryParameters qParameters;

        public void setGetAnotherFeatures(boolean getAnotherFeatures) {
            this.getAnotherFeatures = getAnotherFeatures;
        }

        @Override
        protected void onPreExecute() {
            if (!this.getAnotherFeatures)
                Utilities.showLoadingDialog(MapEditorActivity.this);
        }

        @Override
        protected FeatureResult doInBackground(QueryParameters... qParameters) {

            String url = getString(R.string.polygon_feature_layer_services);
            QueryTask qTask = null;
            try {
                qTask = new QueryTask(url, featureServiceToken);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FeatureResult results = null;

            try {
                if (qTask != null) {
                    qParameters[0].setSpatialRelationship(SpatialRelationship.INTERSECTS);
                    results = qTask.execute(qParameters[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @Override
        protected void onPostExecute(FeatureResult results) {
            if (!this.getAnotherFeatures)
                Utilities.dismissLoadingDialog();

            polygonFeatuersGraphicsLayer.removeAll();
            if (results != null) {
                int i = 0;
                for (Object element : results) {
                    if (element instanceof Feature) {
                        try {
                            final Feature feature = (Feature) element;
                            polygonFeatuersGraphicsLayer.addGraphic(getGraphic(feature));
                            i++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("Number", "Polygon: " + i);
            }

//            shapeType = null;
        }


    }

    int newPoints, newLines, newPolygons;
    int editPoints, editLines, editPolygons;
    int gcsPoints, gcsLines, gcsPolygons;

    private Graphic getGraphic(Feature feature) {


        /**-------------------------------------Ali Ussama Update---------------------------------------*/

        Graphic graphic = null;
        try {

            String name = String.valueOf(feature.getAttributes().get("OBJECTID"));

            String type = String.valueOf(feature.getGeometry().getType().value());

            Log.i(TAG, name);
            if (name != null && !name.isEmpty() && !name.equals("null") && String.valueOf(feature.getGeometry().getType()).matches("POINT")) {
                TextSymbol textSymbol = new TextSymbol(15, name, Color.BLACK, TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.TOP);
                Graphic textGraphic = new Graphic(feature.getGeometry(), textSymbol);
                labelsLayer.addGraphic(textGraphic);
            }

            // if (feature.getAttributes() != null && (feature.getAttributes().get(ColumnNames.SURVEYOR_ID) == null || ((Integer) feature.getAttributes().get(ColumnNames.SURVEYOR_ID)) == 0)) {
            if (feature.getAttributes() != null) {

                //if (feature.getAttributes() != null && (feature.getAttributes().get(ColumnNames.CHECK_SURVEYOR) == null || ((Integer) feature.getAttributes().get(ColumnNames.CHECK_SURVEYOR)) == 0)) {
                SharedPreferences mPreferences = getSharedPreferences(getString(R.string.New_Drawed_Featur_ids), MODE_PRIVATE);

                long feature_obj_id = mPreferences.getLong(String.valueOf(feature.getAttributeValue("OBJECTID")), -1);

                if (feature.getGeometry() instanceof Point) {

                    gcsPoints++;
                    if (feature_obj_id != -1) {//TODO
                        newPoints++;
                        graphic = new Graphic(feature.getGeometry(), pointSymbol);

                    } else {
                        graphic = new Graphic(feature.getGeometry(), pointSymbolGCS);
                    }
                } else if (feature.getGeometry() instanceof Polygon) {

                    if (feature_obj_id != -1) {//TODO
                        newPolygons++;
                        Log.i("getGraphic1", "Found " + (feature.getAttributes().get("OBJECTID")));

                        graphic = new Graphic(feature.getGeometry(), shapeSymbol);

                    } else {
                        graphic = new Graphic(feature.getGeometry(), shapeSymbolGCS);
                    }
                    gcsPolygons++;
                } else {
                    gcsLines++;
                    if (feature_obj_id != -1) {//TODO
                        newLines++;
                        graphic = new Graphic(feature.getGeometry(), shapeSymbol);

                    } else {
                        graphic = new Graphic(feature.getGeometry(), shapeSymbolGCS);
                    }
                }


            }
            /**---------------------------------------------------------------------------------*/
        } catch (Exception e) {
            e.printStackTrace();
        }
//            else {
//                Log.i("getGraphic", "Edit");
//                if (feature.getGeometry() instanceof Point) {
//                    editPoints++;
//                    graphic = new Graphic(feature.getGeometry(), pointSymbolGCSEdited);
//                } else if (feature.getGeometry() instanceof Polygon) {
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbolGCSEdited);
//                    editPolygons++;
//                } else {
//                    editLines++;
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbolGCSEdited);
//                }
//            }
//        } else {
//            Log.i("getGraphic", "New");
//            SharedPreferences mPreferences = getSharedPreferences(getString(R.string.New_Drawed_Featur_ids), MODE_PRIVATE);
//
//            long feature_obj_id = mPreferences.getLong(String.valueOf(feature.getAttributeValue("OBJECTID")), -1);
//
//            if (feature.getGeometry() instanceof Point) {
//
//                gcsPoints++;
//                if (feature_obj_id != -1) {//TODO
//                    newPoints++;
//                    graphic = new Graphic(feature.getGeometry(), pointSymbol);
//
//                } else {
//                    graphic = new Graphic(feature.getGeometry(), pointSymbolGCS);
//                }
//            } else if (feature.getGeometry() instanceof Polygon) {
//
//                if (feature_obj_id != -1) {//TODO
//                    newPolygons++;
//                    Log.i("getGraphic1", "Found " + (feature.getAttributes().get("OBJECTID")));
//
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbol);
//
//                } else {
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbolGCS);
//                }
//                gcsPolygons++;
//            } else {
//                gcsLines++;
//                if (feature_obj_id != -1) {//TODO
//                    newLines++;
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbol);
//
//                } else {
//                    graphic = new Graphic(feature.getGeometry(), shapeSymbolGCS);
//                }
//            }
//        }

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                findViewById(R.id.linear_layers_info).setVisibility(View.VISIBLE);
//                updateFeatureNumbers();
//            }
//        });
        return graphic;
    }

    private void updateFeatureNumbers() {
        try {
            ((TextView) findViewById(R.id.tv_new_point)).setText("مربع توزيع");
            ((TextView) findViewById(R.id.tv_edit_point)).setText("عمود كهرباء");
            ((TextView) findViewById(R.id.tv_gcs_point)).setText("حلقة وحدة رئيسية");

            ((TextView) findViewById(R.id.tv_new_line)).setText("محطة فرعية");
            ((TextView) findViewById(R.id.tv_edit_line)).setText("عداد كهرباء");
            ((TextView) findViewById(R.id.tv_new_polygon)).setText("نقطة الخدمة");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    private Geometry geometryToAdd;
    private ActionMode addShapeActionMode;
    private double mResult;
    private CalloutPopupWindow mCallout;
    private TextView mText;
    private GraphicsLayer mGraphicsLayerAddShapes;
    private ArrayList<Point> currentAddedPoints;
    private ArrayList<Point> prevAddedPoints;
    public static final String POLYGON = "Poly";
    public static final String LINE = "Line";
    public static final String POINT = "Point";
    private static String shapeType = null;

    public ArcGISFeatureLayer selectedLayer = null;
    public FeatureLayer selectedLayerOffline = null;
    public GeodatabaseFeatureTable selectedTableOffline;

    private ActionMode.Callback addShapeActionModeCallback = new ActionMode.Callback() {
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_action_add_shape, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.item_undo:
                    undo();
                    return true;
                case R.id.item_redo:
                    redo();
                    return true;
                case R.id.item_Done:
                    done();
                    return true;
            }
            return false;
        }

        private void done() {
            addNewFeature();
        }

        private void redo() {
            if (prevAddedPoints.size() > 0) {
                int lastIndex = prevAddedPoints.size() - 1;
                Point p = prevAddedPoints.get(lastIndex);
                currentAddedPoints.add(p);
                prevAddedPoints.remove(lastIndex);
                drawShape();
            } else {
                Toast.makeText(MapEditorActivity.this, "No Steps", Toast.LENGTH_SHORT).show();
            }
        }

        private void undo() {
            if (currentAddedPoints.size() > 0) {
                int lastIndex = currentAddedPoints.size() - 1;
                Point p = currentAddedPoints.get(lastIndex);
                prevAddedPoints.add(p);
                currentAddedPoints.remove(lastIndex);
                if (currentAddedPoints.size() > 0)
                    drawShape();
                else
                    mGraphicsLayerAddShapes.removeAll();
            } else {
                Toast.makeText(MapEditorActivity.this, "No Steps", Toast.LENGTH_SHORT).show();
            }
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            if (addShapeActionMode != null) {
                addShapeActionMode = null;
                finishAddingOnline();
            }
        }
    };

    @Override
    public void onClick(View v) {

        try {
            if (mapView.isLoaded()) {

//        fabGeneral.close(true);
                fabGeneral.hideMenu(true);
                currentAddedPoints = new ArrayList<>();
                prevAddedPoints = new ArrayList<>();
                mGraphicsLayerAddShapes.removeAll();
                if (v.getId() == R.id.fab_add_distribution_box) {
                    shapeType = POINT;
                    selectedLayer = FCL_DISTRIBUTIONBOX;
                    selectedLayerOffline = FCL_DISTRIBUTIONBOXLayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                } else if (v.getId() == R.id.fab_add_poles) {
                    shapeType = POINT;
                    selectedLayer = FCL_POLES;
                    selectedLayerOffline = FCL_POLESLayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                } else if (v.getId() == R.id.fab_add_rmu) {
                    shapeType = POINT;
                    selectedLayer = FCL_RMU;
                    selectedLayerOffline = FCL_RMULayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                } else if (v.getId() == R.id.fab_add_sub_station) {
                    shapeType = POINT;
                    selectedLayer = FCL_Substation;
                    selectedLayerOffline = FCL_SubstationLayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                } else if (v.getId() == R.id.fab_add_ocl_meter) {
                    shapeType = POINT;
                    selectedLayer = OCL_METER;
                    selectedLayerOffline = OCL_METERLayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                } else if (v.getId() == R.id.fab_add_service_point) {
                    shapeType = POINT;
                    selectedLayer = Service_Point;
                    selectedLayerOffline = Service_PointLayerOffline;
                    selectedTableOffline = (GeodatabaseFeatureTable) selectedLayerOffline.getFeatureTable();
                    addShapeActionMode = startActionMode(addShapeActionModeCallback);
                }

                isInOnlineAddingMode = true;
                setUpAddingNewFeatureOnline();

            } else {
                Toast.makeText(MapEditorActivity.this, getResources().getString(R.string.map_not_loaded), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawShape() {
        if (currentAddedPoints.size() > 0) {
            MultiPath shape;
            if (shapeType.equals(POLYGON))
                shape = new Polygon();
            else
                shape = new Polyline();

            mGraphicsLayerAddShapes.removeAll();
            shape.startPath(currentAddedPoints.get(0));
            for (int i = 1; i < currentAddedPoints.size(); i++) {
                shape.lineTo(currentAddedPoints.get(i));
            }

            Graphic shapeGraphic = new Graphic(shape, shapeSymbol);
            mGraphicsLayerAddShapes.addGraphic(shapeGraphic);


            //TODO
            Log.i("draw Shape", "draw Shape");
            for (int i = 0; i < currentAddedPoints.size(); i++) {
                Graphic startGraphic = new Graphic(currentAddedPoints.get(i), new SimpleMarkerSymbol(Color.BLACK, 4, SimpleMarkerSymbol.STYLE.CIRCLE));
                mGraphicsLayerAddShapes.addGraphic(startGraphic);
            }

            geometryToAdd = GeometryEngine.project(shape, mapView.getSpatialReference(), LAYER_SR);

            if (currentAddedPoints.size() > 1)
                showAreaLength(shape);
            else
                hideCallout();
        }
    }

    private void showAreaLength(MultiPath shape) {
        Point var4;
        if (shapeType.equals(LINE)) {
            this.mResult += GeometryEngine.geodesicLength(shape, mapView.getSpatialReference(), (LinearUnit) Unit.create(LinearUnit.Code.METER));
            var4 = mapView.toScreenPoint(currentAddedPoints.get(currentAddedPoints.size() - 1));
            this.showResult((float) var4.getX(), (float) var4.getY());
        } else if (shapeType.equals(POLYGON)) {
            this.mResult = GeometryEngine.geodesicArea(shape, mapView.getSpatialReference(), (AreaUnit) Unit.create(AreaUnit.Code.SQUARE_METER));
            var4 = GeometryEngine.getLabelPointForPolygon((Polygon) shape, this.mapView.getSpatialReference());
            Point screenPoint = this.mapView.toScreenPoint(var4);
            this.showResult((float) screenPoint.getX(), (float) screenPoint.getY());
        }
    }

    private String getResultString() {
        String unit;
        if (shapeType.equals(LINE)) {
            unit = "M";
        } else {
            unit = "SQ-M";
        }
        return this.mResult > 0.0D ? String.format(Locale.ENGLISH, "%.2f", this.mResult) + " " + unit : "";
    }

    private void hideCallout() {
        if (this.mCallout != null && this.mCallout.isShowing()) {
            this.mCallout.hide();
        }
    }

    private void showResult(float x, float y) {
        if (this.mResult > 0.0D) {
            if (this.mCallout == null) {
                this.mText = new TextView(this);
                this.mCallout = new CalloutPopupWindow(this.mText);
            }

            this.mText.setText(this.getResultString());
            this.mCallout.showCallout(mapView, mapView.toMapPoint(x, y), 0, 0);
        } else if (this.mCallout != null && this.mCallout.isShowing()) {
            this.mCallout.hide();
        }

    }

}