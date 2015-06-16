package com.appycamp.mapmanager.tiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.appycamp.mapmanager.polylines.PolylineManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.projection.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Zach on 6/14/2015.
 */
public class CustomTileProvider implements TileProvider {

    private static final boolean STORE_LOCALLY = false;
    public static double MIN_ZOOM_THRESHOLD = 8.99;
    public static double MAX_ZOOM_THRESHOLD = 21.01;
    public static int MIN_ALPHA = 140;
    public static int MAX_ALPHA = 240;
    private static final int TILE_SIZE = 256;
    private static final int SCALE = 1;
    private static final int DIMENSION = TILE_SIZE * SCALE;

    private final SphericalMercatorProjection mProjection = new SphericalMercatorProjection(TILE_SIZE);
    private final PolylineOptions[] mPolylineOptions;
    private final ArrayList<ArrayList<LatLng>> mPoints;
    private Paint mPolylinePaint;

    public CustomTileProvider() {
        mPolylineOptions = PolylineManager.getInstance().getAllPolylineOptions();
        mPoints = PolylineManager.getInstance().getPolylineLatLngs();

        mPolylinePaint = new Paint();
        mPolylinePaint.setStyle(Paint.Style.STROKE);
        mPolylinePaint.setStrokeCap(Paint.Cap.ROUND);
        mPolylinePaint.setStrokeJoin(Paint.Join.ROUND);
        mPolylinePaint.setShadowLayer(0, 0, 0, 0);
        mPolylinePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        if(getZoomRatio(zoom) == 0.0f)
            return null;

        Log.d(getClass().getSimpleName(), "Getting tile for " + x +"x, " + y + "y, and " + zoom +"zoom");
        if(STORE_LOCALLY) {
            File tileFile = new File(TileOverlayManager.getOverlayFileString(x, y, zoom));
            if (tileFile.exists()) {
                Log.d(getClass().getSimpleName(), "Have file stored");
                Bitmap bmp = BitmapFactory.decodeFile(TileOverlayManager.getOverlayFileString(x, y, zoom));
                if (bmp != null) {
                    return generateTileFromBitmap(bmp);
                }
            }
        }

        Matrix matrix = new Matrix();

        /*The scale factor in the transformation matrix is 1/10 here because I scale up the tiles for drawing.
         * Why? Well, the spherical mercator projection doesn't seem to quite provide the resolution I need for
         * scaling up at high zoom levels. This bypasses it without needing a higher tile resolution.
         */
        float scale = ((float) Math.pow(2, zoom) * SCALE / 10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(-x * DIMENSION, -y * DIMENSION);

        Bitmap bitmap = Bitmap.createBitmap(DIMENSION, DIMENSION, Bitmap.Config.ARGB_8888); //save memory on old phones
        Canvas c = new Canvas(bitmap);
        c.setMatrix(matrix);

        c = drawCanvasFromArray(c, zoom);

        if(STORE_LOCALLY) {
            new PolylineOverlaySaver(x, y, zoom, new PolylineOverlaySaver.OnOverlaySavedListener() {
                @Override
                public void onOverlaySaved(boolean success) {
                }
            }).execute(bitmap);
        }

        return generateTileFromBitmap(bitmap);
    }

    public Tile generateTileFromBitmap(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new Tile(DIMENSION, DIMENSION, baos.toByteArray());
    }

    /**
     * Here the Canvas can be drawn on based on data provided from a Spherical Mercator Projection
     *
     * @param c
     * @param zoom
     * @return
     */
    private Canvas drawCanvasFromArray(Canvas c, int zoom) {
        mPolylinePaint.setAlpha(getAlpha(zoom));

        //Line features
        Path path = new Path();

        if (mPoints != null) {
            for (int i = 0; i < mPoints.size(); i++) {
                ArrayList<LatLng> route = mPoints.get(i);
                mPolylinePaint.setColor(mPolylineOptions[i].getColor());
                mPolylinePaint.setStrokeWidth(getLineWidth(zoom * (int) mPolylineOptions[i].getWidth()));
                mPolylinePaint.setAlpha(getAlpha(zoom));

                if (route != null && route.size() > 1) {
                    Point screenPt1 = mProjection.toPoint(route.get(0)); //first point
                    MarkerOptions m = new MarkerOptions();
                    m.position(route.get(0));

                    /* This is where the corresponding scaling is done to avoid high resolution tiles.
                     * We shrink our matrix and then scale up our paths to make things scale correctly
                     * without having to get crazy with the resolutions
                     */
                    path.moveTo((float) screenPt1.x * 10, (float) screenPt1.y * 10);
                    for (int j = 1; j < route.size(); j++) {
                        Point screenPt2 = mProjection.toPoint(route.get(j));
                        path.lineTo((float) screenPt2.x * 10, (float) screenPt2.y * 10);
                    }
                }

                c.drawPath(path, mPolylinePaint);
            }
        }
        return c;
    }

    /**
     * This will let you adjust the line width based on zoom level
     *
     * @param zoom
     * @return
     */
    private float getLineWidth(int zoom) {
        final float zoomMultiplier = .0002f;
        float width = zoomMultiplier * getZoomRatio(zoom);
        Log.d(getClass().getSimpleName(), "Line Width: " + width);
        return width;
    }

    /**
     * This will let you adjust the alpha value based on zoom level
     *
     * @param zoom
     * @return
     */
    private int getAlpha(int zoom) {
        int alphaDif = MAX_ALPHA - MIN_ALPHA;
        float zoomRatio = getZoomRatio(zoom);
        int alpha = (int) (alphaDif * zoomRatio) + MIN_ALPHA;
        Log.d(getClass().getSimpleName(), "Alpha: " + alpha);
        return alpha;
    }

    private float getZoomRatio(int zoom){
        if(zoom <= MIN_ZOOM_THRESHOLD || zoom >= MAX_ZOOM_THRESHOLD)
            return 0.0f;

        float diff = (float) (MAX_ZOOM_THRESHOLD - MIN_ZOOM_THRESHOLD);
        float zoomFromMin = (float) (zoom - MIN_ZOOM_THRESHOLD);
        return zoomFromMin / diff;
    }
}