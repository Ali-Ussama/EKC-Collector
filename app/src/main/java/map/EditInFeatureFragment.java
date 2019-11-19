package map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esri.android.map.AttachmentManager;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.AttachmentInfo;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.Domain;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Field;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import activities.MapEditorActivity;
import activities.VideoActivity;

import com.ekc.collector.R;
import com.esri.core.table.FeatureTable;

import butterknife.BindView;
import butterknife.ButterKnife;
import util.CollectorMediaPlayer;
import util.OConstants;
import util.Utilities;
import util.ZoomableImageView;


public class EditInFeatureFragment extends Fragment {
    public static final int REQUEST_CODE_GALLERY = 1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 2;
    //    public static final int REQUEST_CODE_CROP_IMAGE = 3;
    public static final int REQUEST_CODE_VIDEO = 4;
    private static final String Attachment_Info = "AttachmentInfo";
    private static final String ATTRIBUTES = "Attributes";
    private static final String FEATURE_ID = "FeatureId";
    private static final String TAG = "EditInFeatureFragment";
    public static String TEMP_PHOTO_FILE_NAME;
    public final String IMAGE_FOLDER_NAME = "AJC_Collector";
    public static AttributeViewsBuilder listAdapter;
    ArcGISFeatureLayer featureLayer;
    MapEditorActivity editorActivity;
    LinearLayout lnAttachments;

    EditText mCodeEt, mDeviceNumEt, mGeneratedCodeEt;

    HorizontalScrollView hsAttachments;
    ScrollView scrollAttributes;
    TextView tvAttachment;
    FeatureLayer featureLayerOffline;
    FeatureLayer featureLayerForTable;

    GeodatabaseFeatureTable featureTable;
    private AttachmentInfo[] attachmentInfo;
    private HashMap<String, Object> attributes;
    private int featureId;
    private EditorFragmentListener mListener;
    private File mFileTemp;
    private MediaRecorder mRecorder = null;
    private CollectorMediaPlayer mPlayer = null;
    private String shapeType;
    private boolean isGcsShape;

    private Map<String, String> types = null;
    private ArrayList<String> typesList = null;
    private CodedValueDomain damageDomain;
    private HashMap<String, String> codeValue;
    private ArrayList<String> codeList;


    private static final String JPG = "jpg";
    private static final String MP4 = "mp4";

    @BindView(R.id.offline_attachments_recycler_view)
    RecyclerView mOfflineAttachmentsRV;

    @BindView(R.id.object_id)
    TextView mObjectIDTV;
    @BindView(R.id.type_spinner)
    Spinner typesSpinner;

    private ArrayList<File> adapterData;
    private OfflineAttachmentRVAdapter mOfflineAttachmentRVAdapter;

    private ArrayAdapter arrayAdapter;

    CallbackListener<GeodatabaseFeatureServiceTable.Status> mInitFeatureServiceTableListener = new CallbackListener<GeodatabaseFeatureServiceTable.Status>() {

        @Override
        public void onError(Throwable ex) {
//                    showToast("Error initializing FeatureServiceTable");
            ex.getStackTrace();
        }

        @Override
        public void onCallback(GeodatabaseFeatureServiceTable.Status arg0) {
            try {

//                if (arg0.equals(GeodatabaseFeatureServiceTable.Status.INITIALIZED)) {
                Log.i(TAG, "initFeatureServiceTable(): onCallback(): status = " + arg0);
                // Create a FeatureLayer from the initialized GeodatabaseFeatureServiceTable.

                // Emphasize the selected features by increasing selection halo size.
//                    featureLayerForTable.setSelectionColorWidth(20);
//                    featureLayerForTable.setSelectionColor(-16711936);

//                    // Add the feature layer to the map.
//                    editorActivity.mapView.addLayer(featureLayer);

                // Set up spinners to contain values from the layer to query against.
//                    setupQuerySpinners();

                // Get the fields that will be used to query the layer.
                Field typeField = mFeatureTable.getField(ColumnNames.Type);
//                    Field causeField = featureServiceTable.getField(CAUSE_FIELD_NAME);

                // Retrieve the possible domain values for each field and add to the spinner data adapters.
                CodedValueDomain damageDomain = (CodedValueDomain) typeField.getDomain();

                try {

                    for (String value : damageDomain.getCodedValues().values()) {
                        Log.i(TAG, "initFeatureServiceTable(): onCallback(): domain value = " + value);
                    }

                    // On the main thread, connect up the spinners with the filled data adapters.
                    editorActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                typesList = new ArrayList<>();
                                typesList.add(getString(R.string.type));

                                typesList.addAll(damageDomain.getCodedValues().values());
                                arrayAdapter = new ArrayAdapter<String>(editorActivity, android.R.layout.simple_dropdown_item_1line, typesList);
                                typesSpinner.setAdapter(arrayAdapter);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                }else{
//                    Log.i(TAG, "initFeatureServiceTable(): onCallback(): status = " + arg0);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    GeodatabaseFeatureServiceTable mFeatureTable;

    public static EditInFeatureFragment newInstance(int featureId, HashMap<String, Object> attributes, AttachmentInfo[] attachmentInfos) {
        EditInFeatureFragment fragment = new EditInFeatureFragment();
        Bundle args = new Bundle();
        args.putSerializable(ATTRIBUTES, attributes);
        args.putSerializable(FEATURE_ID, featureId);
        args.putSerializable(Attachment_Info, attachmentInfos);
        fragment.setArguments(args);
        return fragment;
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof EditorFragmentListener) {
                mListener = (EditorFragmentListener) context;
                editorActivity = ((MapEditorActivity) context);

                if (editorActivity.shapeToAdd[0].getGeometry() instanceof Point) {
                    featureLayer = editorActivity.selectedLayer;
                    featureLayerOffline = editorActivity.selectedLayerOffline;
//                    CodedValueDomain mCodedValueDomain = (CodedValueDomain) featureLayerOffline.getFeatureTable().getField("Type").getDomain();
                    shapeType = MapEditorActivity.POINT;
//                    try {
//                        if (((featureLayer.getField(ColumnNames.Type))) != null) {
//
//                            Log.i(TAG, "onAttach(): field type is not null");
//
//                            CodedValueDomain domain = ((CodedValueDomain) featureLayer.getField(ColumnNames.Type).getDomain());
//                            types = domain.getCodedValues();
//
//
//                        } else {
//                            Log.i(TAG, "onAttach(): field type is null");
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

//                    try {
//                        for (FeatureType type : featureLayer.getTypes()) {
//                            try {
//                                Log.i(TAG, "" + type.getName());
//                                Log.i(TAG, "" + type.getDomains().size());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                } else if (editorActivity.shapeToAdd[0].getGeometry() instanceof Polyline) {
                    featureLayer = editorActivity.lineFeatureLayer;
                    shapeType = MapEditorActivity.LINE;
                    featureLayerOffline = editorActivity.selectedLayerOffline;
                } else if (editorActivity.shapeToAdd[0].getGeometry() instanceof Polygon) {
                    featureLayer = editorActivity.polygonFeatureLayer;
                    shapeType = MapEditorActivity.POLYGON;
                    featureLayerOffline = editorActivity.selectedLayerOffline;
                }


                if (editorActivity.onlineData) {
                    listAdapter = new AttributeViewsBuilder(editorActivity, getFields(featureLayer.getFields()), featureLayer.getTypes(), ColumnNames.E_FEATURETYPE);
                } else {
                    featureTable = ((GeodatabaseFeatureTable) featureLayerOffline.getFeatureTable());
                    editorActivity.selectedTableOffline = featureTable;

                    listAdapter = new AttributeViewsBuilder(editorActivity, getFields(featureTable.getFields().toArray(new Field[0])), featureTable.getFeatureTypes().toArray(new FeatureType[0]), featureTable.getTypeIdField());
                }
            } else {
                throw new RuntimeException(context.toString() + " must implement EditorFragmentListener");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("test", "On Attach");
    }

    private void initFeatureServiceTable(GeodatabaseFeatureServiceTable featureServiceTable) {
        try {

            if (featureLayerOffline != null && featureLayerOffline.getFeatureTable() != null) {
                Field typeField = featureLayerOffline.getFeatureTable().getField(ColumnNames.Type);
//                    Field causeField = featureServiceTable.getField(CAUSE_FIELD_NAME);

                // Retrieve the possible domain values for each field and add to the spinner data adapters.
                damageDomain = (CodedValueDomain) typeField.getDomain();

                try {

                    for (String value : damageDomain.getCodedValues().values()) {
                        Log.i(TAG, "initFeatureServiceTable(): onCallback(): domain value = " + value);
                    }

                    // On the main thread, connect up the spinners with the filled data adapters.
                    editorActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                typesList = new ArrayList<>();
                                codeList = new ArrayList<>();
                                codeValue = new HashMap<>();

                                typesList.add(getString(R.string.type));


                                typesList.addAll(damageDomain.getCodedValues().values());
                                codeList.addAll(damageDomain.getCodedValues().keySet());

                                for (String code : damageDomain.getCodedValues().keySet()) {
                                    Log.i(TAG, "initFeatureServiceTable(): domain Code = " + code);
                                    Log.i(TAG, "initFeatureServiceTable(): domain Value = " + damageDomain.getCodedValues().get(code));
                                    codeValue.put(damageDomain.getCodedValues().get(code), code);
                                }
                                arrayAdapter = new ArrayAdapter<String>(editorActivity, android.R.layout.simple_dropdown_item_1line, typesList);
                                typesSpinner.setAdapter(arrayAdapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            mFeatureTable.initialize(mInitFeatureServiceTableListener);

        } catch (
                Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (editorActivity != null) {
            editorActivity.findViewById(R.id.linear_layers_info).setVisibility(View.GONE);
            editorActivity.findViewById(R.id.rlLatLong).setVisibility(View.GONE);
            initFeatureServiceTable(mFeatureTable);

        }
    }

    private Field[] getFields(Field[] layerFields) {
        if (shapeType.equals(MapEditorActivity.POINT)) {
            ArrayList<Field> fields = new ArrayList<>();
            for (int i = 0; i < layerFields.length; i++) {
                fields.add(null);
            }

            /**-----------------------------Ali Ussama Update-------------------------------------*/
            for (Field field : layerFields) {
                if (ColumnNames.A_FEATURETYPE.equals(field.getName())) {
                    fields.set(0, field);
                } else if (ColumnNames.ENAME.equals(field.getName())) {
                    fields.add(field);
                } else if (ColumnNames.ANAME.equals(field.getName())) {
                    fields.add(field);
                } else if (ColumnNames.COMMENTS.equals(field.getName())) {
                    fields.add(field);
                } else if (ColumnNames.GlobalID.equals(field.getName())) {
                    Log.i("getFeilds", "GlobalID");
                    fields.add(field);
                } else if (ColumnNames.Type.equals(field.getName())) {
                    Log.i("getFeilds", "Type");
                    fields.add(field);
                } else if (ColumnNames.Device_No.equals(field.getName())) {
                    Log.i("getFeilds", "Device_No");
                    fields.add(field);
                } else if (ColumnNames.Code.equals(field.getName())) {
                    Log.i("getFeilds", "Code");
                    fields.add(field);
                }
            }
            /**-----------------------------Ali Ussama Update-------------------------------------*/
            return fields.toArray(new Field[0]);
        }
        return layerFields;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                attributes = (HashMap<String, Object>) getArguments().getSerializable(ATTRIBUTES);
                attachmentInfo = (AttachmentInfo[]) getArguments().getSerializable(Attachment_Info);
                featureId = (int) getArguments().getSerializable(FEATURE_ID);

                Integer surveyorId = (Integer) attributes.get(ColumnNames.SURVEYOR_ID);
                isGcsShape = surveyorId == null || surveyorId == 0;

            }

            Log.i("test", "On Create");

            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {
            inflater.inflate(R.menu.menu_fragment_edit, menu);

            editorActivity.menuItemOverflow.setVisible(false);
//            editorActivity.menuItemSearch.setVisible(false);

            if (!editorActivity.onlineData) {
                editorActivity.menuItemSync.setVisible(false);
                editorActivity.menuItemOnline.setVisible(false);
                editorActivity.item_load_previous_offline.setVisible(false);
            } else {
                editorActivity.menuItemOffline.setVisible(false);
//                editorActivity.menuItemIndex.setVisible(false);
//            editorActivity.menuItemGCS.setVisible(false);
                editorActivity.menuItemSatellite.setVisible(false);
//                editorActivity.menuItemBaseMap.setVisible(false);
            }

            if (!shapeType.equals(MapEditorActivity.POINT)) {
                menu.findItem(R.id.menu_change_location).setVisible(false);
            }


            if (isGcsShape) {
                menu.findItem(R.id.menu_change_location).setVisible(false);
                menu.findItem(R.id.menu_delete).setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.menu_save:
                    saveChanges(listAdapter);
                    break;
                case R.id.menu_delete:
                    onDelete();
                    break;
                case R.id.menu_camera:

                    if (ActivityCompat.checkSelfPermission(editorActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                    } else {
                        takePicture();
                    }
                    break;

                case R.id.menu_audio:
                    if (ActivityCompat.checkSelfPermission(editorActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                    } else {
                        showAudioDialog();
                    }
                    break;

                case R.id.menu_gallery:
                    if (ActivityCompat.checkSelfPermission(editorActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        openGallery();
                    }
                    break;
                case R.id.menu_video:
                    if (ActivityCompat.checkSelfPermission(editorActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                    } else {
                        recordVideo();
                    }
                    break;
                case R.id.menu_change_location:
                    getView().setVisibility(View.GONE);
                    editorActivity.changeLocationOnline();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isSpeakButtonLongPressed;
    private AlertDialog audioDialog;

    private void showAudioDialog() {
        if (editorActivity != null) {
            editorActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_record_audio, null);
                    ImageView icRecordAudio = (ImageView) view.findViewById(R.id.icRecordAudio);
                    icRecordAudio.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Log.d("audio", "In Long Click");
                            if (!isSpeakButtonLongPressed) {
                                Log.d("audio", "Record Now");
                                isSpeakButtonLongPressed = true;
                                startRecording();
                            }
                            return true;
                        }
                    });
                    icRecordAudio.setOnTouchListener(new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(View pView, MotionEvent pEvent) {

                            pView.onTouchEvent(pEvent);
                            // We're only interested in when the button is released.
                            if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                                // We're only interested in anything if our speak button is currently pressed.
                                if (isSpeakButtonLongPressed) {
                                    // Do something when the button is released.
                                    isSpeakButtonLongPressed = false;
                                    Log.d("audio", "Stop Recording");
                                    if (audioDialog != null) {
                                        audioDialog.dismiss();
                                    }
                                    stopRecording();
                                }
                            }
                            return false;
                        }
                    });
                    builder.setView(view);
                    audioDialog = builder.show();

                }
            });
        }
    }

    private void recordVideo() {
        try {

            String pointName = editorActivity.shapeToAdd[0].getAttributes().get("OBJECTID").toString();
            String pointFolderName = (editorActivity.selectedLayer.getName().split("\\.")[2]);

            createFile(pointName, pointFolderName, MP4, "VID");

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Uri contentUri = FileProvider.getUriForFile(getContext(), getString(R.string.app_package_name), mFileTemp);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set (1) video quality to high, (0) LOW
            startActivityForResult(takeVideoIntent, REQUEST_CODE_VIDEO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        try {

            String pointName;
            try {
                pointName = String.valueOf(attributes.get(ColumnNames.ObjectID));
            } catch (Exception e) {
                e.printStackTrace();
                pointName = "1"; // TODO Remove
            }
            String pointFolderName;

            if (editorActivity.onlineData)
                pointFolderName = (editorActivity.selectedLayer.getName().split("\\.")[2]);
            else {
                pointFolderName = (editorActivity.selectedLayerOffline.getName().split("\\.")[2]);
            }

            createFile(pointName, pointFolderName, JPG, "IMG");
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(editorActivity, getString(R.string.app_package_name), mFileTemp);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PICTURE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {

        String pointName = editorActivity.shapeToAdd[0].getAttributes().get("OBJECTID").toString();
        String pointFolderName;
        if (editorActivity.onlineData)
            pointFolderName = (editorActivity.selectedLayer.getName().split("\\.")[2]);
        else {
            pointFolderName = (editorActivity.selectedLayerOffline.getName().split("\\.")[2]);
        }
        createFile(pointName, pointFolderName, JPG, "IMG");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }


    private void createFile(String name, String layerFolderName, String extension, String type) {
        try {
            Date d = new Date();
            TEMP_PHOTO_FILE_NAME = "Image_" + new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension;


            File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME);

            if (!rootFolder.exists()) {
                if (rootFolder.mkdir()) {
                    Log.i(TAG, "createFile(): rootFolder director is created = " + rootFolder.toString());
                    try {
                        File dateFolderName = new File(rootFolder, new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d));

                        if (!dateFolderName.exists()) {

                            if (dateFolderName.mkdir()) {

                                File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

                                if (!layerFolder.exists()) {
                                    if (layerFolder.mkdir()) {
                                        Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                                        File pointFolder = new File(layerFolder.getPath(), name);
                                        if (!pointFolder.exists()) {
                                            if (pointFolder.mkdir()) {

                                                mFileTemp = new File(pointFolder.getPath() + File.separator +
                                                        type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                                Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                            } else {
                                                Log.i(TAG, "createFile(): pointFolder director not created");
                                            }
                                        }
                                    } else {
                                        Log.i(TAG, "createFile(): layerFolder directory not created");
                                    }
                                }
                            } else {
                                Log.i(TAG, "createFile(): dateFolderName directory not created");
                            }
                        } else {
                            File layerFolder = new File(rootFolder.getPath(), layerFolderName);

                            if (!layerFolder.exists()) {
                                if (layerFolder.mkdir()) {
                                    Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                                    File pointFolder = new File(layerFolder.getPath(), name);
                                    if (!pointFolder.exists()) {
                                        if (pointFolder.mkdir()) {

                                            mFileTemp = new File(pointFolder.getPath() + File.separator +
                                                    type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                            Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                        } else {
                                            Log.i(TAG, "createFile(): pointFolder director not created");
                                        }
                                    }
                                } else {
                                    Log.i(TAG, "createFile(): layerFolder directory not created");
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "createFile(): rootFolder director not created");
                }
            } else {

                try {
                    File dateFolderName = new File(rootFolder, new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d));

                    if (!dateFolderName.exists()) {

                        if (dateFolderName.mkdir()) {

                            File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

                            if (!layerFolder.exists()) {
                                if (layerFolder.mkdir()) {
                                    Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                                    File pointFolder = new File(layerFolder.getPath(), name);
                                    if (!pointFolder.exists()) {
                                        if (pointFolder.mkdir()) {

                                            mFileTemp = new File(pointFolder.getPath() + File.separator +
                                                    type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                            Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                        } else {
                                            Log.i(TAG, "createFile(): pointFolder director not created");

                                        }
                                    }
                                } else {
                                    Log.i(TAG, "createFile(): layerFolder directory not created");
                                }
                            } else {
                                Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                                File pointFolder = new File(layerFolder.getPath(), name);
                                if (!pointFolder.exists()) {
                                    if (pointFolder.mkdir()) {

                                        mFileTemp = new File(pointFolder.getPath() + File.separator +
                                                type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                        Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                    } else {
                                        Log.i(TAG, "createFile(): pointFolder director not created");

                                    }
                                } else {
                                    mFileTemp = new File(pointFolder.getPath() + File.separator +
                                            type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                    Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                }
                            }
                            Log.i(TAG, "createFile(): rootFolder director exists");

                        } else {
                            Log.i(TAG, "createFile(): dateFolderName directory not created");
                        }
                    } else {

                        File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

                        if (!layerFolder.exists()) {
                            if (layerFolder.mkdir()) {
                                Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                                File pointFolder = new File(layerFolder.getPath(), name);
                                if (!pointFolder.exists()) {
                                    if (pointFolder.mkdir()) {

                                        mFileTemp = new File(pointFolder.getPath() + File.separator +
                                                type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                        Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                    } else {
                                        Log.i(TAG, "createFile(): pointFolder director not created");

                                    }
                                }
                            } else {
                                Log.i(TAG, "createFile(): layerFolder directory not created");
                            }
                        } else {
                            Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());

                            File pointFolder = new File(layerFolder.getPath(), name);
                            if (!pointFolder.exists()) {
                                if (pointFolder.mkdir()) {

                                    mFileTemp = new File(pointFolder.getPath() + File.separator +
                                            type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                    Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                                } else {
                                    Log.i(TAG, "createFile(): pointFolder director not created");

                                }
                            } else {
                                mFileTemp = new File(pointFolder.getPath() + File.separator +
                                        type + "_" + new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension.trim());

                                Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                            }
                        }
                        Log.i(TAG, "createFile(): rootFolder director exists");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "createFile(): rootFolder director is created = " + rootFolder.toString());


            }

            // rename image...


            Log.i(TAG, "file createFile " + mFileTemp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void startCropImage() {

//        Intent intent = new Intent(editorActivity, CropImage.class);
//        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
//        intent.putExtra(CropImage.SCALE, false);
//        intent.putExtra(CropImage.ASPECT_X, 0);
//        intent.putExtra(CropImage.ASPECT_Y, 0);
//
//        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(editorActivity, R.string.permission_gallery, Toast.LENGTH_LONG).show();
            } else {
                openGallery();
            }
        } else if (requestCode == 2) {

            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(editorActivity, R.string.permission_record, Toast.LENGTH_LONG).show();
            } else {
                showAudioDialog();
            }

        } else if (requestCode == 3) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(editorActivity, R.string.permission_camera, Toast.LENGTH_LONG).show();
            } else {
                takePicture();
            }

        } else {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(editorActivity, R.string.permission_camera, Toast.LENGTH_LONG).show();
            } else {
                recordVideo();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_in_feature, container, false);
        try {
            ButterKnife.bind(this, view);

            try {
                adapterData = new ArrayList<>();
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(editorActivity, 2);
                mOfflineAttachmentRVAdapter = new OfflineAttachmentRVAdapter(adapterData, editorActivity);
                mOfflineAttachmentsRV.setLayoutManager(mGridLayoutManager);
                mOfflineAttachmentsRV.setAdapter(mOfflineAttachmentRVAdapter);
                mOfflineAttachmentsRV.setNestedScrollingEnabled(true);

                mObjectIDTV.setText(String.valueOf(attributes.get(ColumnNames.ObjectID)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            listAdapter.setFeatureSet(attributes);

            LinearLayout listView = (LinearLayout) view.findViewById(R.id.list_view);
            lnAttachments = (LinearLayout) view.findViewById(R.id.lnAttachments);
            hsAttachments = (HorizontalScrollView) view.findViewById(R.id.hsAttachments);
            scrollAttributes = (ScrollView) view.findViewById(R.id.scrollAttributes);
            tvAttachment = (TextView) view.findViewById(R.id.tvAttachment);
            mCodeEt = view.findViewById(R.id.mCodeEt);
            mDeviceNumEt = view.findViewById(R.id.device_num);

            try {
                typesSpinner = view.findViewById(R.id.type_spinner);

//                mFeatureTable = new GeodatabaseFeatureServiceTable(getString(R.string.gcs_feature_server_test), MapEditorActivity.featureServiceToken, (int) featureLayer.getID());
//                mFeatureTable.setSpatialReference(editorActivity.mapView.getSpatialReference());
                if (!editorActivity.onlineData) {
                    initFeatureServiceTable(mFeatureTable);
                } else {
                    HashMap<String, String> tempCodeValue = OConstants.getDomain(featureLayer.getName());
                    typesList = new ArrayList<>();
                    codeList = new ArrayList<>();
                    codeValue = new HashMap<>();
                    typesList.addAll(tempCodeValue.values());
                    codeList.addAll(tempCodeValue.keySet());

                    for (String code : tempCodeValue.keySet()) {
                        codeValue.put(tempCodeValue.get(code), code);
                    }
                    arrayAdapter = new ArrayAdapter<String>(editorActivity, android.R.layout.simple_dropdown_item_1line, typesList);
                    typesSpinner.setAdapter(arrayAdapter);
                }
//
            } catch (Exception e) {
                e.printStackTrace();
            }

//            mGeneratedCodeEt = view.findViewById(R.id.generated_code);
//
//        boolean focused = false;
//
//
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//
//            Log.i(TAG, "" + listAdapter.getCount());
//            if (listAdapter.getItem(i) != null) {
//                View v = listAdapter.getView(i, isGcsShape);
//
//                if (!focused) {
//                    EditText editText = (EditText) v.findViewById(R.id.field_value_txt);
//                    if (editText != null) {
//                        editText.requestFocus();
//                        focused = true;
//                    }
//                }
//                listView.addView(v);
//            }
//        }

//            if (attachmentInfo == null || attachmentInfo.length == 0) {
            tvAttachment.setText(R.string.no_attachment);
            hsAttachments.setVisibility(View.GONE);
//            } else {
//                for (AttachmentInfo anAttachmentInfo : attachmentInfo) {
//                    addAttachmentFileToView(anAttachmentInfo.getName(), (int) anAttachmentInfo.getId(), null, false);
//                }
//            }

            if (attributes != null && !attributes.isEmpty()) {
                Log.i(TAG, "onCreate(): attributes not null");
                if (attributes.get(ColumnNames.Code) != null)
                    mCodeEt.setText(attributes.get(ColumnNames.Code).toString());
                if (attributes.get(ColumnNames.Device_No) != null)
                    mDeviceNumEt.setText(attributes.get(ColumnNames.Device_No).toString());
            } else {
                Log.i(TAG, "onCreate(): attributes are null");
                Log.i(TAG, "onCreate(): ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void addAttachmentFileToView(final String name, final int id, final File file, boolean scrollToEnd) {

        if (editorActivity != null) {
            int screenWidth = editorActivity.getResources().getDisplayMetrics().widthPixels;

            final RelativeLayout relativeLayout = new RelativeLayout(editorActivity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (screenWidth * 0.4), (int) (screenWidth * 0.4));
            layoutParams.setMargins((int) (screenWidth * 0.02), 0, (int) (screenWidth * 0.02), 0);
            relativeLayout.setLayoutParams(layoutParams);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(editorActivity, R.color.attachmentBackgroundColor));

            final ImageView ivAttachment = new ImageView(editorActivity);
            ivAttachment.setId(android.R.id.icon);
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.4), (int) (screenWidth * 0.3));
            relativeParams.setMargins(0, (int) (screenWidth * 0.01), 0, 0);
            ivAttachment.setLayoutParams(relativeParams);

            if (file == null) {
                ivAttachment.setImageResource(R.drawable.ic_file_download_white_24dp);
            } else {
                Log.d("Here1", file.getAbsolutePath());
                if (Utilities.getFileExt(file.getName()).equals(JPG)) {
                    setPic(editorActivity, ivAttachment, file);
//                    ivAttachment.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                }
//                else if (file.getName().contains("Audio")) {
//                    if (hsAttachments.getVisibility() == View.GONE) {
//                        hsAttachments.setVisibility(View.VISIBLE);
//                        tvAttachment.setText(getString(R.string.attachments));
//
//                    }
//                    ivAttachment.setImageResource(R.drawable.ic_play);
//                }
                else {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                    if (bitmap != null) {
                        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                        ivAttachment.setBackground(ob);
                    }
                    ivAttachment.setImageResource(R.drawable.play_circle);
                }

                ivAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Utilities.getFileExt(file.getName()).equals(JPG))
                            showImageInDialog(file);
//                        else if (file.getName().contains("Audio"))
//                            startPlaying(file, ivAttachment);
                        else
                            showVideoInDialog(file);
                    }
                });

                ivAttachment.setScaleType(ImageView.ScaleType.CENTER);
            }


            relativeLayout.addView(ivAttachment);

            final ProgressBar progressBar = new ProgressBar(editorActivity);
            relativeParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.1), (int) (screenWidth * 0.1));
            relativeParams.setMargins(0, (int) (screenWidth * 0.01), 0, 0);
            relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(relativeParams);
            relativeLayout.addView(progressBar);
            progressBar.setVisibility(View.GONE);

            if (file == null) {

                ivAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadAttachmentFile(progressBar, ivAttachment, name, id, featureId);

//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (editorActivity != null) {
//                                    if (ivAttachment.getVisibility() != View.VISIBLE)
//                                        downloadAttachmentFile(progressBar, ivAttachment, name, id, featureId);
//                                }
//                            }
//                        }, 6000);
                    }
                });
//
                progressBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadAttachmentFile(progressBar, ivAttachment, name, id, featureId);

//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (editorActivity != null) {
//                                    if (ivAttachment.getVisibility() != View.VISIBLE)
//                                        downloadAttachmentFile(progressBar, ivAttachment, name, id, featureId);
//                                }
//                            }
//                        }, 6000);
                    }
                });

            }

            TextView tvAttachment = new TextView(editorActivity);
            relativeParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.4), FrameLayout.LayoutParams.WRAP_CONTENT);
            relativeParams.addRule(RelativeLayout.BELOW, ivAttachment.getId());
            relativeParams.setMargins((int) (screenWidth * 0.01), (int) (screenWidth * 0.01), 0, (int) (screenWidth * 0.01));
            tvAttachment.setLayoutParams(relativeParams);
            relativeLayout.addView(tvAttachment);
            tvAttachment.setText(name);
            tvAttachment.setMaxLines(1);
            tvAttachment.setEllipsize(TextUtils.TruncateAt.END);


            final ImageView ivDeleteAttachment = new ImageView(editorActivity);
            relativeParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.08), (int) (screenWidth * 0.08));
            relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            ivDeleteAttachment.setLayoutParams(relativeParams);
            ivDeleteAttachment.setImageResource(R.drawable.close_circle_outline);
            relativeLayout.addView(ivDeleteAttachment);

            ivDeleteAttachment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utilities.showConfirmDialog(editorActivity, getString(R.string.delete_attachment), getString(R.string.are_you_sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (mPlayer != null && mPlayer.isPlaying() && file != null && mPlayer.getFileName().equals(file.getName())) {
                                stopPlaying();
                            }
                            deleteAttachment(relativeLayout, id, featureId);
                        }
                    });
                }
            });

            lnAttachments.addView(relativeLayout);

            if (scrollToEnd) {
                hsAttachments.postDelayed(new Runnable() {
                    public void run() {
                        if (editorActivity != null) {
                            hsAttachments.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                            scrollAttributes.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }
                }, 100);
            }
        }
    }

    public void saveChanges(AttributeViewsBuilder listAdapter) {
        try {
            Log.i(TAG, "saveChanges(): is called");
            if (mListener != null) {
                Log.i(TAG, "saveChanges(): Listener not null");

                if (mCodeEt.getText() == null || mCodeEt.getText().toString().trim().isEmpty()) {
                    mCodeEt.setError("مطلوب");
                } else if (mDeviceNumEt.getText() == null || mDeviceNumEt.getText().toString().trim().isEmpty()) {
                    mDeviceNumEt.setError("مطلوب");
                } else if (typesList != null && !typesList.isEmpty() && typesSpinner.getSelectedItemPosition() == 0) {
                    Log.i(TAG, "saveChanges(): typeList not null");
                    if (typesSpinner.getSelectedItemPosition() == 0) {
                        Log.i(TAG, "saveChanges(): selected type position 0");
                        Utilities.showToast(editorActivity, "من فضلك اختر نوع النقطة");
                    }
                } else {
                    String typeCode = "";
                    try {
                        typeCode = codeValue.get(typesSpinner.getSelectedItem().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "saveChanges(): typeCode = " + typeCode);

                    mListener.onSave(listAdapter, mCodeEt.getText().toString().trim(), mDeviceNumEt.getText().toString().trim(), String.valueOf(attributes.get(ColumnNames.ObjectID)), typeCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDelete() {

        Utilities.showConfirmDialog(editorActivity, getString(R.string.delete_service), getString(R.string.are_you_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onDelete(featureId);
                }
            }
        });
    }

    private void downloadAttachmentFile(final ProgressBar progressBar, final ImageView view, final String attachmentName, final long attachmentId, final int featureId) {

        if (editorActivity != null && editorActivity.onlineData) {
            Log.d("editorActivity : ", editorActivity.getExternalCacheDir().toString());

            AttachmentManager attachmentManager = new AttachmentManager(editorActivity, featureLayer.getUrl(), editorActivity.featureServiceToken, editorActivity.getExternalCacheDir());
            attachmentManager.downloadAttachment(featureId, (int) attachmentId, attachmentName, new AttachmentManager.AttachmentDownloadListener() {
                @Override
                public void onAttachmentDownloading() {
                    Log.d("Attachment", "on Attachment Downloading");
                    if (editorActivity != null) {
                        editorActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                                view.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }

                @Override
                public void onAttachmentDownloaded(final File file) {
                    if (file != null && editorActivity != null) {
                        Log.d("Attachment", "on Attachment Downloaded " + file.getAbsolutePath());
                        editorActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (file.exists()) {
                                    progressBar.setVisibility(View.GONE);
                                    view.setVisibility(View.VISIBLE);
                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (Utilities.getFileExt(file.getName()).equals(JPG)) {
                                                showImageInDialog(file);
                                            }
//                                            else if (file.getName().contains("Audio")) {
//                                                startPlaying(file, view);
//                                            }
                                            else {
                                                showVideoInDialog(file);
                                            }
                                        }
                                    });
                                    progressBar.setOnClickListener(null);
                                    if (Utilities.getFileExt(file.getName()).equals(JPG)) {
//                                        view.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                        setPic(editorActivity, view, file);

                                    }
//                                    else if (file.getName().contains("Audio")) {
//                                        view.setImageResource(R.drawable.ic_play);
//                                    }
                                    else {
                                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(Uri.fromFile(file).getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                                        if (bitmap != null) {
                                            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                                            view.setBackground(ob);
                                        }

                                        view.setImageResource(R.drawable.play_circle);
                                    }
                                    view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    view.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        downloadAttachmentFile(progressBar, view, attachmentName, attachmentId, featureId);
                    }
                }

                @Override
                public void onAttachmentDownloadFailed(Exception e) {
                    if (editorActivity != null) {
                        Log.d("Attachment", "on Attachment Download Failed ");
                        e.printStackTrace();
                        editorActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                view.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        } else {
            featureTable.retrieveAttachment(featureId, attachmentId, new CallbackListener<InputStream>() {
                @Override
                public void onCallback(final InputStream inputStream) {
                    Log.d("Attachment", "on Attachment Downloaded ");

                    if (editorActivity != null) {

                        try {
                            final File file = new File(editorActivity.getExternalCacheDir(), attachmentName);
                            OutputStream output = new FileOutputStream(file);
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            int read;

                            while ((read = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                            output.close();

                            editorActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    view.setVisibility(View.VISIBLE);
                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (Utilities.getFileExt(file.getName()).equals(JPG))
                                                showImageInDialog(file);
//                                            else if (file.getName().contains("Audio"))
//                                                startPlaying(file, view);
                                            else
                                                showVideoInDialog(file);
                                        }
                                    });
                                    progressBar.setOnClickListener(null);
                                    if (Utilities.getFileExt(file.getName()).equals(JPG)) {
//                                        view.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                        setPic(editorActivity, view, file);
                                    }
//                                    else if (file.getName().contains("Audio")) {
//                                        view.setImageResource(R.drawable.ic_play);
//                                    }
                                    else {
                                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(Uri.fromFile(file).getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                                        if (bitmap != null) {
                                            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                                            view.setBackground(ob);
                                        }

                                        view.setImageResource(R.drawable.play_circle);
                                    }
                                    view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace(); // handle exception, define IOException and others
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private void showImageInDialog(final File file) {
        if (editorActivity != null) {

            editorActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("Attachment", "Show Dialog ");

                    AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity, R.style.Theme_Holo_Dialog_Alert);
                    builder.setTitle(file.getName());
                    View dialogView = LayoutInflater.from(editorActivity).inflate(R.layout.dialog_img_attachment, null, false);

                    ((ZoomableImageView) dialogView).setImageBitmap(decodeScaledBitmapFromSdCard(file.getAbsolutePath(), 400, 300));
                    builder.setView(dialogView);
                    builder.show().getWindow().setBackgroundDrawableResource(R.color.dialogAttachmentBackgroundColor);
                }
            });
        }
    }

    public static Bitmap decodeScaledBitmapFromSdCard(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    private void showVideoInDialog(final File file) {
        if (editorActivity != null) {

            editorActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Intent i = new Intent(editorActivity, VideoActivity.class);
                    i.putExtra("file", file);
                    startActivity(i);

//                int screenWidth = editorActivity.getResources().getDisplayMetrics().widthPixels;
//                int screenHeight = editorActivity.getResources().getDisplayMetrics().heightPixels;
//
//                Log.d("Attachment", "Show Dialog ");
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity, R.style.Theme_Holo_Dialog_Alert);
//                builder.setTitle(file.getName());
//                View dialogView = LayoutInflater.from(editorActivity).inflate(R.layout.dialog_video_attachment, null, false);
//
//                final VideoView videoView = (VideoView) dialogView.findViewById(R.id.videoView);
//                final Uri video = Uri.fromFile(file);
//                videoView.setVideoURI(video);
//                videoView.setZOrderOnTop(true);
//                videoView.start();
//                final MediaController mediaController = new MediaController(videoView.getContext());
//                videoView.setMediaController(mediaController);
//                mediaController.setFocusable(true);
//                mediaController.setMediaPlayer(videoView);
//                mediaController.setEnabled(true);
//                mediaController.setActivated(true);
//                mediaController.setClickable(true);
//                builder.setView(dialogView);
//                AlertDialog dialog = builder.show();
//                dialog.getWindow().setBackgroundDrawableResource(R.color.dialogAttachmentBackgroundColor);
//
//
////                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
////                lp.copyFrom(dialog.getWindow().getAttributes());
////                lp.gravity = Gravity.CENTER_HORIZONTAL & Gravity.CENTER_VERTICAL;
////
////                lp.width = (int) (screenWidth * 0.9);
////                lp.height = (int) (screenHeight * 0.6);
////                lp.x = 0;
////                lp.y = 0;
////                dialog.getWindow().setAttributes(lp);
//
//                WindowManager.LayoutParams a = dialog.getWindow().getAttributes();
//                a.dimAmount = 0;
//                dialog.getWindow().setAttributes(a);
//

                }
            });
        }
    }

    private void addAttachmentToFeature(final File file) {
        try {
            Log.i(TAG, "addAttachmentToFeature(): is called");

            if (editorActivity != null) {

                if (Utilities.isNetworkAvailable(editorActivity)) {
                    if (editorActivity.onlineData) {
                        Utilities.showFileLoadingDialog(editorActivity);
                        mOfflineAttachmentsRV.setVisibility(View.GONE);
                        hsAttachments.setVisibility(View.VISIBLE);
                        featureLayer.addAttachment(featureId, file, new CallbackListener<FeatureEditResult>() {
                            @Override
                            public void onCallback(final FeatureEditResult featureEditResult) {
                                Log.d("Attachment", "Done Add Attachments ");
                                if (editorActivity != null) {

                                    editorActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int attachmentId = (int) featureEditResult.getObjectId();
                                            addAttachmentFileToView(file.getName(), attachmentId, file, true);
                                            Utilities.dismissLoadingDialog();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(final Throwable e) {
                                e.printStackTrace();
                                Log.d("Attachment", "Error In Add Attachments ");
                                if (editorActivity != null) {

                                    editorActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utilities.dismissLoadingDialog();
//                                    Utilities.showToast(editorActivity, e.toString());
                                            Utilities.showToast(editorActivity, getString(R.string.connection_failed));
                                        }
                                    });
                                }
                            }
                        });

                    } else {
//                        try {
//                            featureTable.addAttachment(featureId, file, "Service", file.getName(), new CallbackListener<Long>() {
//                                @Override
//                                public void onCallback(final Long aLong) {
//                                    if (aLong != -1) {
//                                        Log.d("Attachment", "Done Add Attachments ");
//                                        if (editorActivity != null) {
//                                            editorActivity.runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    int attachmentId = aLong.intValue();
//                                                    addAttachmentFileToView(file.getName(), attachmentId, file, true);
//                                                    Utilities.dismissLoadingDialog();
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onError(Throwable throwable) {
//                                    Log.d("Attachment", "Error In Add Attachments ");
//                                    if (editorActivity != null) {
//
//                                        editorActivity.runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Utilities.dismissLoadingDialog();
//                                                Utilities.showToast(editorActivity, getString(R.string.attachment_offline_error));
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            Log.d("Attachment", "Error In Add Attachments ");
//                            if (editorActivity != null) {
//                                editorActivity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Utilities.dismissLoadingDialog();
//                                        Utilities.showToast(editorActivity, getString(R.string.attachment_offline_error));
//                                    }
//                                });
//                            }
//                        }
                        hsAttachments.setVisibility(View.GONE);
                        mOfflineAttachmentsRV.setVisibility(View.VISIBLE);
                        File newFile = compressImage(file.getPath(), editorActivity, "", "");
                        mOfflineAttachmentRVAdapter.addImageBitmap(newFile);
                        mOfflineAttachmentRVAdapter.notifyDataSetChanged();
                    }
                } else {
//                        try {
//                            featureTable.addAttachment(featureId, file, "Service", file.getName(), new CallbackListener<Long>() {
//                                @Override
//                                public void onCallback(final Long aLong) {
//                                    if (aLong != -1) {
//                                        Log.d("Attachment", "Done Add Attachments ");
//                                        if (editorActivity != null) {
//                                            editorActivity.runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    int attachmentId = aLong.intValue();
//                                                    addAttachmentFileToView(file.getName(), attachmentId, file, true);
//                                                    Utilities.dismissLoadingDialog();
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onError(Throwable throwable) {
//                                    Log.d("Attachment", "Error In Add Attachments ");
//                                    if (editorActivity != null) {
//
//                                        editorActivity.runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Utilities.dismissLoadingDialog();
//                                                Utilities.showToast(editorActivity, getString(R.string.attachment_offline_error));
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                            Log.d("Attachment", "Error In Add Attachments ");
//                            if (editorActivity != null) {
//                                editorActivity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Utilities.dismissLoadingDialog();
//                                        Utilities.showToast(editorActivity, getString(R.string.attachment_offline_error));
//                                    }
//                                });
//                            }
//                        }
                    hsAttachments.setVisibility(View.GONE);
                    mOfflineAttachmentsRV.setVisibility(View.VISIBLE);
                    File newFile = compressImage(file.getPath(), editorActivity, "", "");
                    mOfflineAttachmentRVAdapter.addImageBitmap(newFile);
                    mOfflineAttachmentRVAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAttachment(final RelativeLayout relativeLayout, long attachmentId, int featureId) {
        Log.d("attachmentId Hanan", String.valueOf(attachmentId));
        if (editorActivity != null) {

            Utilities.showLoadingDialog(editorActivity);

            if (editorActivity.onlineData) {
                featureLayer.deleteAttachments(featureId, new int[]{(int) attachmentId}, new CallbackListener<FeatureEditResult[]>() {
                    @Override
                    public void onCallback(FeatureEditResult[] featureEditResults) {
                        Log.d("Attachment", "Done Delete Attachments ");
                        if (editorActivity != null) {
                            editorActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.dismissLoadingDialog();
                                    lnAttachments.removeView(relativeLayout);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        e.printStackTrace();
                        Log.d("Attachment", "Error In delete Attachments ");
                        if (editorActivity != null) {

                            editorActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.dismissLoadingDialog();
//                                    Utilities.showToast(editorActivity, e.toString());
                                    Utilities.showToast(editorActivity, getString(R.string.connection_failed));
                                }
                            });
                        }
                    }
                });
            } else {
                featureTable.deleteAttachment(featureId, attachmentId, new CallbackListener<Void>() {
                    @Override
                    public void onCallback(Void aVoid) {
                        Log.d("Attachment", "Done Delete Attachments ");
                        if (editorActivity != null) {

                            editorActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.dismissLoadingDialog();
                                    lnAttachments.removeView(relativeLayout);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        e.printStackTrace();
                        Log.d("Attachment", "Error In delete Attachments ");
                        if (editorActivity != null) {

                            editorActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.dismissLoadingDialog();
//                                    Utilities.showToast(editorActivity, e.toString());
                                    Utilities.showToast(editorActivity, getString(R.string.connection_failed));
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult(): is called");
        Log.i(TAG, "onActivityResult(): requestCode = " + requestCode);
        Log.i(TAG, "onActivityResult(): resultCode = " + resultCode);

        if ((resultCode == Activity.RESULT_OK)) {
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PICTURE:
                    hsAttachments.setVisibility(View.VISIBLE);
                    tvAttachment.setText(getString(R.string.attachments));
                    saveFileToStorage(mFileTemp);
                    addAttachmentToFeature(mFileTemp);
                    break;
                case REQUEST_CODE_GALLERY:
                    if (data != null) {
                        try {
                            InputStream inputStream = editorActivity.getContentResolver().openInputStream(data.getData());
                            FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                            copyStream(inputStream, fileOutputStream);
                            fileOutputStream.close();
                            if (inputStream != null) {
                                inputStream.close();
                            }
//                        startCropImage();
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                                writeBitmapInFile(bitmap);
//                            startCropImage();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        } finally {
//                        String path = data.getStringExtra(CropImage.IMAGE_PATH);
//                        if (path == null) {
//                            return;
//                        }
                            hsAttachments.setVisibility(View.VISIBLE);
                            tvAttachment.setText(getString(R.string.attachments));
                            addAttachmentToFeature(mFileTemp);
                        }
                    }
                    break;

                case REQUEST_CODE_VIDEO:
                    try {
//                        InputStream inputStream = editorActivity.getContentResolver().openInputStream(data.getData());
//                        FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
//                        copyStream(inputStream, fileOutputStream);
//                        fileOutputStream.close();
//                        inputStream.close();
//                        mFileTemp = new File(getRealPathFromURI(data.getData()));
//                        Log.d("video",mFileTemp.getPath());
                        checkVideoSize(mFileTemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

//                case REQUEST_CODE_CROP_IMAGE:
//                    break;

            }
        }
    }

    private void saveFileToStorage(File mFileTemp) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File folder = new File(Environment.getExternalStorageDirectory(), "/EKC Collector/");
            folder.mkdir();
            //TODO Move mFileTemp to this Folder
        }
    }

    private void checkVideoSize(File file) {
        long Filesize = file.length() / 1024;//call function and convert bytes into Kb
        if (Filesize >= 1024) {
            if ((Filesize / 1024) > 30) {
                Utilities.showInfoDialog(editorActivity, getString(R.string.video_size), getString(R.string.change_video_setting));
            } else {
                hsAttachments.setVisibility(View.VISIBLE);
                tvAttachment.setText(getString(R.string.attachments));
                saveFileToStorage(file);
                addAttachmentToFeature(file);
            }
        } else {
            hsAttachments.setVisibility(View.VISIBLE);
            tvAttachment.setText(getString(R.string.attachments));
            saveFileToStorage(file);
            addAttachmentToFeature(file);
        }
    }

    private void writeBitmapInFile(Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mFileTemp);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("test", "Detach");
        mListener = null;
        editorActivity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("test", "On Save Instance State");
    }

    public interface EditorFragmentListener {
        void onSave(AttributeViewsBuilder listAdapter, String code, String deviceID, String domainCode, String GlobalID);

        void onDelete(int featureId);

    }

    private void startRecording() {
        Log.d("audio", "Start Recording...");


        if (mPlayer != null && mPlayer.isPlaying()) {
            stopPlaying();
        }
        MediaPlayer ring = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.starting_record);
        ring.start();

        String pointName = editorActivity.shapeToAdd[0].getAttributes().get("OBJECTID").toString();
        String pointFolderName = (editorActivity.selectedLayer.getName().split("\\.")[2]);

        createFile(pointName, pointFolderName, MP4, "AUD");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(String.valueOf(mFileTemp));
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecorder.start();
            }
        }, 3);

    }

    private void stopRecording() {
        Log.d("audio", "Stop Recording...");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        saveFileToStorage(mFileTemp);
        addAttachmentToFeature(mFileTemp);
    }

    private ImageView prevIvClicked;

    private void startPlaying(File mediaFile, ImageView ivClicked) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mediaFile);

            if (mPlayer != null && mPlayer.isPlaying() && mPlayer.getFileName().equals(mediaFile.getName())) {
                stopPlaying();
                ivClicked.setImageResource(R.drawable.ic_play);
            } else {

                if (mPlayer != null && mPlayer.isPlaying()) {
                    stopPlaying();
                    prevIvClicked.setImageResource(R.drawable.ic_play);
                }

                mPlayer = new CollectorMediaPlayer(mediaFile.getName());
                try {
                    mPlayer.setDataSource(fis.getFD());
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepare();
                    mPlayer.start();
                    ivClicked.setImageResource(R.drawable.ic_pause);
                    prevIvClicked = ivClicked;

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopPlaying();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ignore) {
            }
        }

    }

    private void setPic(Context mContext, ImageView mImageView, File mCurrentPhotoPath) {
        // Get the dimensions of the View

        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int targetW = (int) (screenWidth * 0.3);
        int targetH = (int) (screenWidth * 0.4);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 2;
        try {
            // Determine how much to scale down the image
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.getAbsolutePath(), bmOptions);
        mImageView.setImageBitmap(bitmap);

//        Picasso.with(mContext).load(mCurrentPhotoPath).into(mImageView);
    }

    private void stopPlaying() {
        Log.d("audio", "Stop Playing");
        mPlayer.release();
        mPlayer = null;

        if (prevIvClicked != null) {
            prevIvClicked.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public static File compressImage(String filePath, Context context, String imageClassType, String customerCode) {

//        String filePath = getRealPathFromURI(imageUri, context);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        try {
            Canvas canvas = new Canvas(Objects.requireNonNull(scaledBitmap));
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        } catch (Exception e) {
            e.printStackTrace();
        }
//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;

        File file = getImageFile();
        String filename = getFilename(imageClassType, context, customerCode);

        File mImageFile = new File(file.getPath(), filename);

        try {
            out = new FileOutputStream(mImageFile);

//          write the compressed bitmap at the destination specified by filename.
            Objects.requireNonNull(scaledBitmap).compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mImageFile;
    }

    private static File getImageFile() {
        final String IMAGES_FOLDER_NAME = "AJC_Collector_COMPRESSED_Images";
        File mediaStorageDir = null;

        try {

            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGES_FOLDER_NAME);

            if (!mediaStorageDir.exists())
                mediaStorageDir.mkdir();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaStorageDir;
    }

    private static String getFilename(String type, Context context, String customerCode) {
        String uriSting = null;


        try {
            Date d = new Date();
            uriSting = "Image_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d) + ".png";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uriSting;
    }
}
