package map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Environment;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTableEditErrors;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.renderer.StretchParameters;
import com.esri.core.renderer.StretchRenderer;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.core.tasks.geodatabase.SyncGeodatabaseParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;

import activities.MapEditorActivity;

import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;
import com.ekc.collector.R;

import util.DataCollectionApplication;
import util.Utilities;

import static android.content.Context.MODE_PRIVATE;

public class GeoDatabaseUtil {
    private static long t;


    private static final String TAG = "GeoDatabaseUtil";
    //    private static final String ROOT_GEO_DATABASE_PATH = "/farsi/OfflineEditor"; //Geo Database Tables (Service Layer)
    private static final String ROOT_GEO_DATABASE_PATH = "/AJC_Collector/"; //Geo Database Tables (Service Layer)

    private static final String ROOT_BASE_MAP_PATH = "/farsi/BaseMap"; //Map Tiles (Base Map Images)
    private static GeodatabaseSyncTask gdbTask;

    private static boolean error = false;
    private static final String LOCAL_GCS_GEO_DATABASE_PATH = "/Internal storage/emulated/0/test/database.gdb";

//    private static final String LOCAL_GCS_GEO_DATABASE_PARENT_PATH = "/storage/emulated/0/GCSCollector/comp2"; //TODO change it to the default path of offline rasters

    private static final String LOCAL_GCS_GEO_DATABASE_PARENT_PATH = "/storage/emulated/0/test/"; //TODO change it to the default path of offline rasters

    public static void goOnline(final MapEditorActivity activity, final MapView mapView, final int localDatabaseNumber) {

        if (Utilities.isNetworkAvailable(activity) && !activity.onlineData) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case DialogInterface.BUTTON_POSITIVE:

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    synchronize(activity, mapView, true, localDatabaseNumber);
                                }
                            }).start();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                            preOnline(activity, mapView);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getString(R.string.sync_before_online))
                    .setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(activity.getString(R.string.no), dialogClickListener)
                    .show();

        } else {
            Utilities.showToast(activity, activity.getString(R.string.no_internet));
        }
    }

    private static void preOnline(final MapEditorActivity activity, final MapView mapView) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {

                    case DialogInterface.BUTTON_POSITIVE:
                        deleteLocalGeoDatabase(activity);
                        finishGoingOnline(activity, mapView);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        finishGoingOnline(activity, mapView);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.delete_current_offline_map))
                .setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(activity.getString(R.string.no), dialogClickListener)
                .show();

    }

    public static void finishGoingOnline(final MapEditorActivity activity, final MapView mapView) {
        try {
            Log.i(TAG, "Going online ....");

            activity.onlineData = true;
            activity.menuItemOnline.setVisible(false);
            activity.menuItemSync.setVisible(false);
            activity.menuItemOffline.setVisible(true);
//        activity.menuItemIndex.setVisible(true);
//       activity.menuItemGCS.setVisible(true);
            // activity.menuItemSatellite.setVisible(true);
            // activity.menuItemBaseMap.setVisible(true);
//        activity.menuItemSearch.setVisible(true);
            activity.item_load_previous_offline.setVisible(false);

            if (!GeoDatabaseUtil.isGeoDatabaseLocal()) {
                activity.menuItemLoad.setVisible(false);
            } else {
                activity.menuItemLoad.setVisible(true);
            }

            for (Layer layer : mapView.getLayers()) {
                if (layer instanceof ArcGISFeatureLayer || layer instanceof ArcGISTiledMapServiceLayer || layer instanceof ArcGISDynamicMapServiceLayer)
                    mapView.removeLayer(layer);
            }

            activity.initMapOnlineLayers();

            if (!MapEditorActivity.isInitialized) {
                activity.mapInitialized();
            } else {
                activity.clearAllGraphicLayers();
                activity.refreshPOI();
            }

            if (activity.offlineGraphicLayer != null) {
                activity.offlineGraphicLayer.removeAll();
            }

            MapEditorActivity.LAYER_SR = SpatialReference.create(MapEditorActivity.SPATIAL_REFERENCE_CODE);

            activity.mapLayersUpdated();

            Log.i(TAG, "Finish Going online");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadData(final MapEditorActivity activity) {
        downloadGeoDatabase(activity, activity.getMapView());
    }

    private static void downloadGeoDatabase(final MapEditorActivity activity, final MapView mapView) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utilities.showLoadingDialog(activity);
            }
        });

        Log.i(TAG, "Getting Feature Service Info...");

        gdbTask = new GeodatabaseSyncTask(activity.getResources().getString(R.string.gcs_feature_server_test), MapEditorActivity.featureServiceToken);
        gdbTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "Error In Getting Feature Service Info" + " time = " + System.currentTimeMillis());
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorInDownloadDialog(activity, mapView);
                        Utilities.dismissLoadingDialog();
                    }
                });
            }

            @Override
            public void onCallback(FeatureServiceInfo fsInfo) {
                try {
                    Log.i(TAG, "FeatureServiceInfo isSyncSupported = " + fsInfo.getCapabilities().isSyncSupported());
                    if (fsInfo.isSyncEnabled()) {
                        t = System.currentTimeMillis();
                        Log.i(TAG, "Feature Service Is Sync Enable" + " time = " + System.currentTimeMillis());

                        if (fsInfo.getUrl().equals(activity.getResources().getString(R.string.gcs_feature_server_test))) {
                            Log.i(TAG, "Feature Service :" + fsInfo.getUrl() + " time = " + System.currentTimeMillis());

                            createGeoDatabaseOffline(gdbTask, activity, mapView, fsInfo);
                        }
                    } else {
                        Log.i(TAG, "Feature Service Is not Sync Enable" + " time = " + System.currentTimeMillis());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Feature Service Is not Sync Enable" + " time = " + System.currentTimeMillis());
                                Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                                Utilities.dismissLoadingDialog();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void createGeoDatabaseOffline(GeodatabaseSyncTask geodatabaseSyncTask, final MapEditorActivity activity, final MapView mapView, FeatureServiceInfo fsInfo) {
        try {
            Log.i(TAG, "Downloading...");
//            GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(fsInfo, mapView.getExtent(), mapView.getSpatialReference(), null, true);
            GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(fsInfo, mapView.getExtent(), SpatialReference.create(MapEditorActivity.SPATIAL_REFERENCE_CODE), null, false);

            params.setOutSpatialRef(fsInfo.getSpatialReference());

            CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {
                @Override
                public void onCallback(String path) {
                    try {

                        Log.i(TAG, "Offline Database Created Successfully" + " time = " + ((System.currentTimeMillis() - t) / 1000));

//                        downloadBaseMap(activity, mapView);//TODO delete after the test

                        addLocalLayers(mapView, activity, DataCollectionApplication.getDatabaseNumber());
                        DataCollectionApplication.setLocalDatabaseTitle(MapEditorActivity.localDatabaseTitle);
                        DataCollectionApplication.incrementDatabaseNumber();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error in creating offline database" + " time = " + System.currentTimeMillis());

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorInDownloadDialog(activity, mapView);
                            Utilities.dismissLoadingDialog();
                        }
                    });
                }
            };

            GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {

                @Override
                public void statusUpdated(GeodatabaseStatusInfo status) {

                    Log.i(TAG, "Database Offline Status: " + status.getStatus().toString() + " time = " + System.currentTimeMillis());
                    if (status.getError() != null && status.getError().getThrowable() != null) {
                        Log.i(TAG, "Database Offline Error: " + status.getError().getThrowable().getMessage() + " time = " + System.currentTimeMillis());

                    }
                }


            };

            File file = new File(Environment.getExternalStorageDirectory().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + DataCollectionApplication.getDatabaseNumber());
            if (!file.exists()) {
                file.mkdir();
            }
//            String databasePath = activity.getFilesDir().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + DataCollectionApplication.getDatabaseNumber() + "/offlinedata.geodatabase";
            String databasePath = file.getPath() + "/offlinedata.geodatabase";
            Log.i(TAG, "database file path = " + databasePath);

            geodatabaseSyncTask.generateGeodatabase(params, databasePath, false, statusCallback, gdbResponseCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load Geo Database (Features Layer and Raster Layer as a BaseMap) into MapView
     *
     * @param mapView  to add Layers into it
     * @param activity a context reference from
     *                 MapEditorActivity to access it's vars and Resources
     */
    public static void addLocalLayers(final MapView mapView, final MapEditorActivity activity, final int databaseNumber) {

        activity.onlineData = false;

        Log.i(TAG, "Removing all the features layers from map");

        for (Layer layer : mapView.getLayers()) {
            if (layer instanceof ArcGISFeatureLayer || layer instanceof ArcGISTiledMapServiceLayer || layer instanceof ArcGISDynamicMapServiceLayer) {
                Log.i(TAG, "addLocalLayers(): Layer " + layer.getName() + " has been deleted");
                mapView.removeLayer(layer);
            }
        }

        Log.i(TAG, "addLocalLayers(): Add features layers from Local Geo Database");

        Geodatabase geodatabase;

        try {

//            String databasePath = activity.getFilesDir().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + databaseNumber + "/offlinedata.geodatabase";
            String databasePath = Environment.getExternalStorageDirectory().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + databaseNumber + "/offlinedata.geodatabase";

//            String baseMapPath = activity.getFilesDir() + ROOT_BASE_MAP_PATH + databaseNumber + "/tiledcache/Layers";//TODO delete after the test
//            ArcGISLocalTiledLayer localTiledLayer = new ArcGISLocalTiledLayer(baseMapPath);//TODO delete after the test
//            mapView.addLayer(localTiledLayer, 0);//TODO delete after the test

            geodatabase = new Geodatabase(databasePath);

            for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseTables()) {
                if (gdbFeatureTable.hasGeometry()) {
                    Log.i(TAG, "addLocalLayers(): gdb Feature Table has geometry");

                    if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.FCL_DISTRIBUTIONBOX_layer_name))) {
                        activity.FCL_DISTRIBUTIONBOXLayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.FCL_DISTRIBUTIONBOXTable = ((GeodatabaseFeatureTable) activity.FCL_DISTRIBUTIONBOXLayerOffline.getFeatureTable());
                        mapView.addLayer(activity.FCL_DISTRIBUTIONBOXGraphicsLayer);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.FCL_DISTRIBUTIONBOXTable.getTableName());

                    } else if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.FCL_POLES_layer_name))) {
                        activity.FCL_POLESLayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.FCL_POLESTable = ((GeodatabaseFeatureTable) activity.FCL_POLESLayerOffline.getFeatureTable());
                        mapView.addLayer(activity.FCL_POLESLayerOffline);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.FCL_POLESTable.getTableName());

                    } else if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.FCL_RMU_layer_name))) {
                        activity.FCL_RMULayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.FCL_RMUTable = ((GeodatabaseFeatureTable) activity.FCL_RMULayerOffline.getFeatureTable());
                        mapView.addLayer(activity.FCL_RMULayerOffline);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.FCL_RMUTable.getTableName());

                    } else if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.FCL_Substation_layer_name))) {
                        activity.FCL_SubstationLayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.FCL_SubstationTable = ((GeodatabaseFeatureTable) activity.FCL_SubstationLayerOffline.getFeatureTable());
                        mapView.addLayer(activity.FCL_SubstationLayerOffline);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.FCL_SubstationTable.getTableName());

                    } else if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.OCL_METER_layer_name))) {
                        activity.OCL_METERLayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.OCL_METERTable = ((GeodatabaseFeatureTable) activity.OCL_METERLayerOffline.getFeatureTable());
                        mapView.addLayer(activity.OCL_METERLayerOffline);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.OCL_METERTable.getTableName());

                    } else if (gdbFeatureTable.getFeatureServiceLayerName().matches(activity.getString(R.string.Service_Point_layer_name))) {
                        activity.Service_PointLayerOffline = new FeatureLayer(gdbFeatureTable);
                        activity.Service_PointTable = ((GeodatabaseFeatureTable) activity.Service_PointLayerOffline.getFeatureTable());
                        mapView.addLayer(activity.Service_PointLayerOffline);

                        Log.i(TAG, "addLocalLayers(): LayerName is " + activity.Service_PointTable.getTableName());

                    }






                    /*else if (gdbFeatureTable.getFeatureServiceLayerName().equals(activity.getString(R.string.line_feature_layer_name))) {
                        activity.featureLayerLinesOffline = new FeatureLayer(gdbFeatureTable);
                        activity.featureTableLines = ((GeodatabaseFeatureTable) activity.featureLayerLinesOffline.getFeatureTable());
                        Log.i(TAG, "addLocalLayers(): LayerName is NameExtendLine");
                    } else if (gdbFeatureTable.getFeatureServiceLayerName().equals(activity.getString(R.string.polygon_feature_layer_name))) {
                        activity.featureLayerPolygonsOffline = new FeatureLayer(gdbFeatureTable);
                        activity.featureTablePolygons = ((GeodatabaseFeatureTable) activity.featureLayerPolygonsOffline.getFeatureTable());
                        Log.i(TAG, "addLocalLayers(): LayerName is NameExtendArea");
                    } else if (gdbFeatureTable.getFeatureServiceLayerName().toLowerCase().startsWith("index")) {
                        Log.i(TAG, "addLocalLayers(): LayerName starts with \"index\" ");

                        mapView.addLayer(new FeatureLayer(gdbFeatureTable));

                    }*/
                }
            }

//            mapView.setMaxExtent(activity.featureTableLines.getExtent());
            Envelope mapExtent = null;
            if (activity.FCL_DISTRIBUTIONBOXTable != null && activity.FCL_DISTRIBUTIONBOXTable.getExtent() != null) {
                mapExtent = activity.FCL_DISTRIBUTIONBOXTable.getExtent();
            } else if (activity.FCL_POLESTable != null && activity.FCL_POLESTable.getExtent() != null) {
                mapExtent = activity.FCL_POLESTable.getExtent();
            } else if (activity.FCL_RMUTable != null && activity.FCL_RMUTable.getExtent() != null) {
                mapExtent = activity.FCL_RMUTable.getExtent();
            } else if (activity.FCL_SubstationTable != null && activity.FCL_SubstationTable.getExtent() != null) {
                mapExtent = activity.FCL_SubstationTable.getExtent();
            } else if (activity.OCL_METERTable != null && activity.OCL_METERTable.getExtent() != null) {
                mapExtent = activity.OCL_METERTable.getExtent();
            } else if (activity.Service_PointTable != null && activity.Service_PointTable.getExtent() != null) {
                mapExtent = activity.Service_PointTable.getExtent();
            }

            if (mapExtent != null)
                mapView.setExtent(mapExtent);


//            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 3, SimpleLineSymbol.STYLE.SOLID);
//            if (activity.offlineGraphicLayer == null)
//                activity.offlineGraphicLayer = new GraphicsLayer();
//
//            mapView.addLayer(activity.offlineGraphicLayer);
//            activity.offlineGraphicLayer.removeAll();
//            activity.offlineGraphicLayer.addGraphic(new Graphic(activity.featureTableLines.getExtent(), lineSymbol));


            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MapEditorActivity.LAYER_SR = null;
                        MapEditorActivity.currentOfflineVersion = databaseNumber;
                        if (activity.isInDrawMood)
                            activity.endDrawOnMap();
                        activity.item_load_previous_offline.setVisible(true);
                        activity.menuItemOffline.setVisible(false);
//                        activity.menuItemIndex.setVisible(false);
//                        activity.menuItemGCS.setVisible(false);
                        activity.menuItemSatellite.setVisible(false);
//                        activity.menuItemBaseMap.setVisible(false);
                        activity.menuItemSync.setVisible(true);
                        activity.menuItemOnline.setVisible(true);
//                        activity.menuItemSearch.setVisible(false);
                        if (!GeoDatabaseUtil.isGeoDatabaseLocal()) {
                            activity.menuItemLoad.setVisible(false);
                        } else {
                            activity.menuItemLoad.setVisible(true);
                        }

                        if (!MapEditorActivity.isInitialized) {
                            activity.mapInitialized();
                        } else {
                            Log.i(TAG, "addLocalLayers(): Map Editor not Initialized");
                            activity.clearAllGraphicLayers();
                            activity.refreshPOI();
                        }

                        Utilities.dismissLoadingDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (FileNotFoundException e) {
            Log.i(TAG, "Error in adding feature layers from Local Geo Database");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void synchronize(final MapEditorActivity activity, final MapView mapView, final boolean goOnline, final int localDatabaseNumber) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utilities.showLoadingDialog(activity);
            }
        });

        if (gdbTask == null) {
            gdbTask = new GeodatabaseSyncTask(activity.getResources().getString(R.string.gcs_feature_server_test), MapEditorActivity.featureServiceToken);
            gdbTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error in upload and synchronize local geo database to the server");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                            Utilities.dismissLoadingDialog();
                        }
                    });
                }

                @Override
                public void onCallback(FeatureServiceInfo objs) {
                    if (objs.isSyncEnabled()) {
                        doSyncAllInOne(activity, mapView, goOnline, localDatabaseNumber);
                    } else {
                        Log.i(TAG, "Feature Service Not Sync Enable");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                                Utilities.dismissLoadingDialog();
                            }
                        });

                    }
                }
            });
        } else {
            doSyncAllInOne(activity, mapView, goOnline, localDatabaseNumber);
        }
    }

    private static void doSyncAllInOne(final MapEditorActivity activity, final MapView mapView, final boolean goOnline, int localDatabaseNumber) {
        try {

            Log.d(TAG, "Getting Local Geo database " + localDatabaseNumber);

//            String databasePath = activity.getFilesDir().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + localDatabaseNumber + "/offlinedata.geodatabase";
            String databasePath = Environment.getExternalStorageDirectory().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + localDatabaseNumber + "/offlinedata.geodatabase";

            Geodatabase gdb = new Geodatabase(databasePath);

            SyncGeodatabaseParameters syncParams = gdb.getSyncParameters();

            CallbackListener<Map<Integer, GeodatabaseFeatureTableEditErrors>> syncResponseCallback = new CallbackListener<Map<Integer, GeodatabaseFeatureTableEditErrors>>() {
                @Override
                public void onCallback(Map<Integer, GeodatabaseFeatureTableEditErrors> objs) {
                    if (objs != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                                Utilities.dismissLoadingDialog();
                            }
                        });

                    } else {
                        Log.i(TAG, "Sync Completed Without Errors");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.showToast(activity, activity.getResources().getString(R.string.sync_completed));
                                Utilities.dismissLoadingDialog();
                                if (goOnline) {
                                    preOnline(activity, mapView);
                                } else {
                                    activity.refreshPOI();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Log.i(TAG, "Error in Syncing");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                            Utilities.dismissLoadingDialog();
                        }
                    });

                }

            };

            GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {
                @Override
                public void statusUpdated(GeodatabaseStatusInfo status) {
                    Log.i(TAG, "Syncing status " + status.getStatus().toString());
                    if (status.getStatus().getValue().matches(GeodatabaseStatusInfo.Status.COMPLETED.getValue())) {
                        //Remove shared preferences database
                        //of the features which has been added offline
                        SharedPreferences mPreferences = activity.getSharedPreferences(activity.getString(R.string.New_Drawed_Featur_ids), MODE_PRIVATE);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Log.i(TAG, "SharedPreference Has been cleared");
                    }
                }
            };

            gdbTask.syncGeodatabase(syncParams, gdb, statusCallback, syncResponseCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Error in Syncing ");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                    Utilities.dismissLoadingDialog();
                }
            });

        }
    }

    public static boolean isGeoDatabaseLocal() {
        ArrayList<String> databaseTitles = DataCollectionApplication.getOfflineDatabasesTitle();
        for (String title : databaseTitles) {
            if (title != null)
                return true;
        }
        DataCollectionApplication.resetDatabaseNumber();
        return false;
    }

    private static void downloadBaseMap(final MapEditorActivity activity, final MapView mapView) {

        DataCollectionApplication.changeLocal(activity);
        Envelope extentForTPK = new Envelope();
        mapView.getExtent().queryEnvelope(extentForTPK);

        String tileCachePath = activity.getFilesDir() + ROOT_BASE_MAP_PATH + DataCollectionApplication.getDatabaseNumber() + "/tiledcache/";

        ExportTileCacheTask exportTileCacheTask = new ExportTileCacheTask(activity.getResources().getString(R.string.BaseMap), MapEditorActivity.mapServiceToken);

        int level = activity.baseMap.getCurrentLevel();
        Log.d(TAG, "Zoom Level " + level);
        ExportTileCacheParameters params = new ExportTileCacheParameters(false, level, 26, extentForTPK, mapView.getSpatialReference());
        createTileCache(params, exportTileCacheTask, tileCachePath, activity, mapView);
    }

    private static void createTileCache(ExportTileCacheParameters params, final ExportTileCacheTask exportTileCacheTask, final String tileCachePath, final MapEditorActivity activity, final MapView mapView) {

        exportTileCacheTask.estimateTileCacheSize(params, new CallbackListener<Long>() {
            @Override
            public void onError(Throwable e) {
                error = true;
                e.printStackTrace();
                Log.d(TAG, "Error In Estimate Tile Cache Size");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                        Utilities.dismissLoadingDialog();
                    }
                });
            }

            @Override
            public void onCallback(Long size) {
                Log.d(TAG, "Tile Cache Size: " + size);
            }
        });

        CallbackListener<ExportTileCacheStatus> statusListener = new CallbackListener<ExportTileCacheStatus>() {

            @Override
            public void onError(Throwable e) {
                DataCollectionApplication.resetLanguage(activity);
                error = true;
                e.printStackTrace();
                Log.d(TAG, "Error In Export Tile Cache Status");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.showToast(activity, activity.getResources().getString(R.string.connection_failed));
                        Utilities.dismissLoadingDialog();
                    }
                });

            }

            @Override
            public void onCallback(ExportTileCacheStatus objs) {
                Log.d(TAG, "Tile Cache Status: " + objs.getStatus().toString());
            }
        };


        CallbackListener<String> downLoadCallback = new CallbackListener<String>() {

            @Override
            public void onError(Throwable e) {
                DataCollectionApplication.resetLanguage(activity);
                error = true;
                e.printStackTrace();
                Log.d(TAG, "Error In generate Tile Cache");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorInDownloadDialog(activity, mapView);
                        Utilities.dismissLoadingDialog();
                    }
                });

            }

            @Override
            public void onCallback(String baseMapPath) {
                if (!error) {
                    DataCollectionApplication.resetLanguage(activity);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utilities.dismissLoadingDialog();
                        }
                    });
                    Log.i(TAG, "Base Map Downloaded Successfully");
                    addLocalLayers(mapView, activity, DataCollectionApplication.getDatabaseNumber());
                    DataCollectionApplication.setLocalDatabaseTitle(MapEditorActivity.localDatabaseTitle);
                    DataCollectionApplication.incrementDatabaseNumber();
                }
            }
        };

        exportTileCacheTask.generateTileCache(params, statusListener, downLoadCallback, tileCachePath);
    }

    private static void showErrorInDownloadDialog(final MapEditorActivity context, final MapView mapView) {
        errorInMapDownloadDismiss(context);
        context.errorInMapDownloadDialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.dialog_connection_failed_title))
                .setMessage(context.getResources().getString(R.string.connection_failed))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.try_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Utilities.isNetworkAvailable(context)) {
                            context.goOffline();
                        } else {
                            showErrorInDownloadDialog(context, mapView);
                        }
                    }
                }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Utilities.isNetworkAvailable(context)) {
                            if (GeoDatabaseUtil.isGeoDatabaseLocal()) {
                                context.showOfflineMapsList(context, mapView);
                                Log.i("isGeoLocationDatabase", "one");
                            } else {
                                Log.i("isGeoLocationDatabase", "two");
                                MapEditorActivity.showNoOfflineMapDialog(context, mapView);
                            }
                        } else {
                            finishGoingOnline(context, mapView);
                        }
                    }
                })
                .show();

    }

    private static void errorInMapDownloadDismiss(final MapEditorActivity context) {

        if (context.errorInMapDownloadDialog != null && context.errorInMapDownloadDialog.isShowing())
            context.errorInMapDownloadDialog.dismiss();

    }

    private static void deleteLocalGeoDatabase(MapEditorActivity activity) {
        File fileGeo = new File(Environment.getExternalStorageDirectory().getPath() + GeoDatabaseUtil.ROOT_GEO_DATABASE_PATH + MapEditorActivity.currentOfflineVersion);
        File fileBaseMap = new File(activity.getFilesDir() + ROOT_BASE_MAP_PATH + MapEditorActivity.currentOfflineVersion);
        DataCollectionApplication.setLocalDatabaseTitle(null, MapEditorActivity.currentOfflineVersion);
        try {
            if (fileGeo.exists()) {
                fileGeo.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "deleteLocalGeoDatabase");

        try {

            if (fileBaseMap.exists()) {
                fileBaseMap.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ------------------------------Ali Ussama Update----------------------------------------------
     */

    /**
     * Check if there is offline Geo database (.gdb) in Device or not
     */
    public static boolean isOfflineGDBAvailable(Context context) {
        try {
            //Declaring parent File var holding
            //Parent pass
//            File parentDir = new File(context.getAssets().open("filename.txt"));

            try {

                try {
//                    gdbTask.
                    AssetManager manager = context.getAssets();

                    if (manager != null && manager.list("assets") != null && manager.list("assets").length > 0) {
                        for (String s : manager.list("assets")) {
                            Log.i(TAG, s);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                InputStream inputStream = context.getAssets().open("geodatabase.mdb");


                File gdb = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EKC_Collector/");

                if (!gdb.exists()) {
                    gdb.mkdir();
                }

                FileOutputStream outputStream = new FileOutputStream(gdb);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                Log.i(TAG, "isOfflineGDBAvailable(): gdb length = " + gdb.length());
                if (gdb.length() > 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            //Declaring list of files to hold
//            //the files in the parent directory
//            List<File> inFiles = new ArrayList<>();
//            //Declaring queue of files to hold
//            //the files in the parent directory
//            Queue<File> files = new LinkedList<>();
//            //Listing all the files in the Parent Path
//            files.addAll(Arrays.asList(parentDir.listFiles()));
//            //Iterate over the files in the Parent Path
//            while (!files.isEmpty()) {
//                //reading and removing the top file in the queue
//                //so we can read the next file after that
//                File file = files.remove();
//                //handle if this file is a folder/directory
//                if (file.getName().endsWith(".gdb")) {
//                    //add geo file into list of geo database files
//                    inFiles.add(file);
//                }
//            }
//            //if there is gdb files have been read
//            if (inFiles.size() > 0) {
//                return true;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // if there is not gdb files have been read
        return false;
    }

    /**
     * Loads Raster file (Extension -> .TIF ) and adds it to a new RasterLayer. The RasterLayer is then added
     * to the map as an operational layer. Map viewpoint is then set based on the Raster's geometry.
     */

    public static void loadRaster(MapView mapView, Activity activity) {
        try {

            Log.i(TAG, "LoadRaster is called");
            final int REQUEST_CHOOSER = 1;
            // Create the ACTION_GET_CONTENT Intent
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            activity.startActivityForResult(Intent.createChooser(intent, "choose your map"), REQUEST_CHOOSER);

//            for (int i = 1; i <= 3; i++) {//TODO remove for loop
//                // create a raster from a local raster file
//                FileRasterSource rasterSource = new FileRasterSource("/storage/UsbDriveA/GCS/" + i + ".tif");
//
////            FileRasterSource rasterSource = new FileRasterSource("/storage/emulated/0/test/1.tif");
//
//                rasterSource.project(mapView.getSpatialReference());
//
//                //A raster layer to hold raster data
//                RasterLayer rasterLayer = new RasterLayer(rasterSource);
//
//                StretchParameters stretchParams = new StretchParameters.MinMaxStretchParameters();
//
//                StretchRenderer renderer = new StretchRenderer();
//
//                renderer.setStretchParameters(stretchParams);
//
//                rasterLayer.setRenderer(renderer);
//
//                //Add the raster layer to the MapView
//                mapView.addLayer(rasterLayer);
//            }

        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * ------------------------------Ali Ussama Update----------------------------------------------
     */
}
