package map.util;

import android.location.Location;
import android.util.Log;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;

import activities.MapEditorActivity;

public class MapUtilites {


    private static final String TAG = "MapUtilites";

    public static void showFullExtent(MapView mMapView) {
        Point centerPt = new Point(1293655.4932430403, 2288926.3523502923);
        mMapView.zoomTo(centerPt, 8);
    }

    public static void zoomToPoint(MapView mMapView, Geometry mapPoint) {
        try {
            Log.i(TAG, "zoomToPoint(): is called");

//            mapPoint = GeometryEngine.project(mapPoint, SpatialReference.create(MapEditorActivity.SPATIAL_REFERENCE_CODE), mMapView.getSpatialReference());
            mapPoint = GeometryEngine.project(mapPoint, SpatialReference.create(MapEditorActivity.GLOBAL_SPATIAL_REFERENCE_CODE), mMapView.getSpatialReference());
//            if (mapPoint instanceof Point) {
//                Point pointToZoom = (Point) mapPoint;
//                int factor = 10;
//                Envelope stExtent = new Envelope(pointToZoom.getX() - factor, pointToZoom.getY() - factor, pointToZoom.getX() + factor, pointToZoom.getY() + factor);
//                mMapView.setExtent(stExtent, 10, true);
//            } else

//            if (mapPoint instanceof MultiPath) {
            Log.i(TAG, "zoomToPoint(): setting extent");

            Envelope envelope = new Envelope(((Point) mapPoint).getX() - 100, ((Point) mapPoint).getY() - 100, ((Point) mapPoint).getX() + 100, ((Point) mapPoint).getY() + 100);

//            mMapView.setExtent(mapPoint, 20, true);
            mMapView.setExtent(envelope);

//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Point getMapPoint(MapView mMapView, Location location) {
        return new Point(location.getLongitude(), location.getLatitude());
//		return (Point) GeometryEngine.project(wgspoint, SpatialReference.create(4326), mMapView.getSpatialReference());
    }


}
