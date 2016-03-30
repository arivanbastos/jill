package br.com.arivanbastos.signalcaptor.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.jillcore.models.map.MapObject;
import br.com.arivanbastos.jillcore.models.map.Map;
import br.com.arivanbastos.jillcore.models.map.MapPoint;
import br.com.arivanbastos.jillcore.models.map.rooms.RectangularRoom;
import br.com.arivanbastos.jillcore.models.map.rooms.Room;
import br.com.arivanbastos.jillcore.models.math.Point2;
import br.com.arivanbastos.jillcore.models.math.RectD;
import br.com.arivanbastos.jillcore.utils.MathUtil;

import br.com.arivanbastos.signalcaptor.location.LocationMethodCanvas;
import br.com.arivanbastos.signalcaptor.views.map.Circle;
import br.com.arivanbastos.signalcaptor.views.map.Shape;

public class MapView extends View implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        LocationMethodCanvas
{
    // 1 meter is equals to how many pixels?
    private float mapScale = 100.0f;

    private Canvas mapCanvas =null;
    private Bitmap mapBitmap =null;
    private Paint mapPaint;

    // Map.
    private Map map;

    // Location points.
    private List<LocationPoint> locationPoints;

    // Event listener.
    private IMapViewListener listener=null;

    // Visual options.
    private int pointRadius = 14;
    private String pointColor = "#FF0000";
    private String objectColor = "#00FF00";
    private int objectWidth = 14;

    private int locationPointWidth = 4;

    // Extra elements.
    private List<Shape> extra;

    // Grid.
    public static final int GRID_TYPE_GLOBAL = 0;
    public static final int GRID_TYPE_LOCAL = 1;
    private float gridSize = 1f;
    private int gridType = GRID_TYPE_LOCAL;

    //
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat gestureDetector;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPosX=0;
    private float mPosY=0;
    private float mLastTouchX=0;
    private float mLastTouchY=0;
    private float mScaleFactor = 1.f;

    public MapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setSaveEnabled(true);

        mapPaint = new Paint();
        mapPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mapPaint.setARGB(255, 0, 0, 0);

        gestureDetector = new GestureDetectorCompat(context,this);
        gestureDetector.setOnDoubleTapListener(this);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        locationPoints = new ArrayList<LocationPoint>();
        extra = new ArrayList<Shape>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        //canvas.drawRect(0, 0, 20, 20, paint);
        if (mapBitmap !=null)
            canvas.drawBitmap(mapBitmap,0,0, mapPaint);

        canvas.restore();
    }

    // -------------------------------------------------

    public void setMap(Map map)
    {
        this.map = map;
    }

    /**
     * Drawns the whole map.
     */
    public void drawn()
    {
        // Creates map bitmap.
        initMap();

        // Rooms.
        drawnRooms();

        // Points.
        drawnPoints();

        // Objects
        drawnObjects();

        // Extra (user elements).
        drawnExtra();

        // Grid.
        drawnGrid();
    }


    /**
     * Creates map bitmap.
     */
    protected void initMap()
    {
        Log.i("MapView", "initMap(" + map.getWidth() + "," + map.getLength() + "," + map.getColor() + "," + map.getScale()+")");
        this.mapScale = map.getScale();

        if (mapBitmap!=null) {
            mapBitmap.recycle();
            mapBitmap = null;
        }

        mapBitmap = Bitmap.createBitmap(toPixels(map.getWidth()), toPixels(map.getLength()), Bitmap.Config.RGB_565);
        mapBitmap.eraseColor(Color.parseColor(map.getColor()));

        mapCanvas = new Canvas(mapBitmap);

        //this.measure(map.getWidth().intValue(), map.getLength().intValue());
        //this.invalidate();
    }

    public void drawnCircle(float x, float y, float radius, int color)
    {
        Circle circle = new Circle(toPixels(x), toPixels(y), toPixels(radius), color);
        synchronized (extra) {
            extra.add(circle);
        }
        drawn();
    }

    // -------------------------------------------------------

    /**
     * Drawns map rooms.
     */
    protected void drawnRooms()
    {
        for (Room r : map.getRooms())
        {
            RectangularRoom rectRoom = (RectangularRoom) r;
            drawRect(rectRoom.getBounds(), rectRoom.getLabel(), rectRoom.getColor());
        }
    }

    /**
     * Drawns map points.
     */
    protected void drawnPoints()
    {
        // Draw data points.
        mapPaint.setStyle(Paint.Style.FILL);
        mapPaint.setColor(Color.parseColor(pointColor));
        for (MapPoint p : map.getPoints())
            mapCanvas.drawCircle(toPixels(p.getX()), toPixels(p.getY()), pointRadius, mapPaint);

        // Draw location points.
        synchronized (locationPoints) {
            for (LocationPoint lp : locationPoints) {
                mapPaint.setColor(lp.color);
                Point2.Double point = lp.point;
                int x =toPixels(point.x);
                int y = toPixels(point.y);
                mapCanvas.drawRect(x-locationPointWidth, y-locationPointWidth, x+locationPointWidth, y+locationPointWidth, mapPaint);
            }
        }
    }


    /**
     * Drawns map objects.
     */
    protected void drawnObjects()
    {
        // Draw data points.
        mapPaint.setStyle(Paint.Style.FILL);
        for (MapObject object : map.getObjects())
        {
            mapPaint.setColor(Color.parseColor(objectColor));
            float x = toPixels(object.getPosition().getX())-objectWidth*.5f;
            float y = toPixels(object.getPosition().getY())-objectWidth*.5f;
            mapCanvas.drawRect(x, y, x+objectWidth, y+objectWidth, mapPaint);

            mapPaint.setColor(Color.DKGRAY);
            mapPaint.setTextSize(13);
            mapPaint.setSubpixelText(true);
            mapPaint.setAntiAlias(true);

            float halfTextWidth = object.getId().length() * 7 * 0.5f;

            //float textX = Math.max(x-20, 0);
            float textX = x-objectWidth*.5f;
            if (textX-halfTextWidth<=0)
                textX = halfTextWidth;
            if (textX + halfTextWidth > mapBitmap.getWidth())
                textX = mapBitmap.getWidth() - halfTextWidth;

            // Above the object.
            float textY = y+objectWidth+10;
            if (textY > mapBitmap.getHeight()-halfTextWidth)
                textY = y-10;

            if (y<mapBitmap.getHeight())
                mapCanvas.drawText(object.getId(), textX, textY, mapPaint);

            mapPaint.setAntiAlias(false);
            mapPaint.setSubpixelText(false);
        }
    }

    protected void drawnExtra()
    {
        synchronized(extra) {
            for (Shape shape : extra)
                shape.drawn(this);
        }
    }

    public void clearMethodsCanvas()
    {
        extra = new ArrayList<Shape>();
    }


    /**
     * Drawns map grids.
     */
    protected void drawnGrid()
    {
        if (gridType == GRID_TYPE_GLOBAL)
            drawGridGlobal();
        else
            drawGridLocal();
    }

    protected void drawGridGlobal()
    {
        int cols = (int)Math.floor(map.getWidth() / gridSize);
        int rows = (int)Math.floor(map.getLength() / gridSize);

        mapPaint.setStyle(Paint.Style.STROKE);
        mapPaint.setColor(Color.GRAY);
        mapPaint.setStrokeWidth(1);

        for (int c = 1; c <= cols; c++)
            mapCanvas.drawLine(toPixels(c*gridSize), 0, toPixels(c*gridSize), toPixels(map.getLength()), mapPaint);
        for (int r = 1; r <= rows; r++)
            mapCanvas.drawLine(0, toPixels(r*gridSize), toPixels(map.getWidth()), toPixels(r*gridSize), mapPaint);

        mapPaint.setTextSize(13);
        mapPaint.setSubpixelText(true);
        mapPaint.setAntiAlias(true);
        for (int c = 1; c <= cols; c++)
            for (int r = 1; r <= rows; r++)
                mapCanvas.drawText(c+","+r, (float)toPixels(c*gridSize)+8, (float)toPixels(r*gridSize)-8, mapPaint);

        mapPaint.setAntiAlias(false);
        mapPaint.setSubpixelText(false);
    }

    protected void drawGridLocal()
    {
        for (Room r : map.getRooms())
        {
            RectangularRoom rectRoom = (RectangularRoom) r;
            RectD rect = rectRoom.getBounds();

            drawRectGrid(rect);
        }
    }

    protected void drawRectGrid(RectD rect)
    {
        int cols = (int)Math.floor(rect.getWidth() / gridSize);
        int rows = (int)Math.floor(rect.getLength() / gridSize);

        if (cols>0 && rows>0)
        {
            mapPaint.setStyle(Paint.Style.STROKE);
            mapPaint.setColor(Color.GRAY);
            mapPaint.setStrokeWidth(1);

            for (int c = 1; c <= cols; c++)
                mapCanvas.drawLine(toPixels(rect.left + c * gridSize), toPixels(rect.top),
                        toPixels(rect.left + c * gridSize), toPixels(rect.bottom), mapPaint);
            for (int r = 1; r <= rows; r++)
                mapCanvas.drawLine(toPixels(rect.left), toPixels(rect.top + r * gridSize),
                                   toPixels(rect.right), toPixels(rect.top + r * gridSize), mapPaint);

            mapPaint.setTextSize(13);
            mapPaint.setSubpixelText(true);
            mapPaint.setAntiAlias(true);
            for (int c = 1; c <= cols; c++)
                for (int r = 1; r <= rows; r++)
                    mapCanvas.drawText(c + "," + r, (float) toPixels(rect.left.intValue() + c * gridSize) + 8, (float) toPixels(rect.top.intValue() + r * gridSize) - 8, mapPaint);

            mapPaint.setAntiAlias(false);
            mapPaint.setSubpixelText(false);
        }
    }

    protected void drawRect(RectD rect, String label, String color)
    {
        this.drawRect(rect.left, rect.top, rect.right, rect.bottom, label, color);
    }

    protected void drawRect(Double left, Double top, Double right, Double bottom, String label, String color)
    {
        Rect roomRect = new Rect(
                toPixels(left),
                toPixels(top),
                toPixels(right),
                toPixels(bottom));
        Log.i("MapView", "Drawing "+label+": "+roomRect);

        mapPaint.setStyle(Paint.Style.FILL);
        try {
            mapPaint.setColor(Color.parseColor(color));
        }
        catch (Exception e)
        {
            Log.e("MapView", "Error parsing color "+ color);
            mapPaint.setColor(Color.WHITE);
        }
        mapCanvas.drawRect(roomRect, mapPaint);

        // border
        mapPaint.setStyle(Paint.Style.STROKE);
        mapPaint.setColor(Color.BLACK);
        mapPaint.setStrokeWidth(6);
        mapCanvas.drawRect(roomRect, mapPaint);

        // Text.
        mapPaint.setStyle(Paint.Style.FILL);
        if (!label.startsWith("wall")) {
            //mapPaint.setColor(Color.BLACK);
            //mapPaint.setAlpha(90);
            mapPaint.setColor(Color.parseColor("#20000000"));
            //mapPaint.setColor(Color.argb(alpha, red, green, blue));
            mapPaint.setTextSize(45);
            mapPaint.setTextAlign(Paint.Align.CENTER);
            mapPaint.setSubpixelText(true);
            mapPaint.setAntiAlias(true);
            mapCanvas.drawText(label, (float) roomRect.centerX(), (float) roomRect.centerY(), mapPaint);
            mapPaint.setSubpixelText(false);
            mapPaint.setAntiAlias(false);
        }
    }

    public int toPixels(int i)
    {
        return (int)(i*mapScale);
    }
    public int toPixels(Double d)
    {
        return new Double(d*mapScale).intValue();
    }
    public int toPixels(float f)
    {
        return new Float(f*mapScale).intValue();
    }
    public float toMeters(float f)
    {
        return f/mapScale;
    }

    // ---------------------------------------------

    public void addLocationPoint(Point2.Double point, int color)
    {
        LocationPoint p = new LocationPoint(point, color);
        synchronized (locationPoints) {
            locationPoints.add(p);
        }
    }

    public void clearLocationPoints()
    {
        synchronized (locationPoints) {
            locationPoints = new ArrayList<LocationPoint>();
        }
    }

    // ---------------------------------------------

    /**
     * Event listeners.
     */

    /**
     *
     * @param ev
     * @return
     */
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = ev.getX();
                final float y = ev.getY();
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.i("MapView","onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        //Log.i("MapView", "onFling: " + event1.toString()+event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //Log.i("MapView", "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        //Log.i("MapView", "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        //Log.i("MapView", "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.i("MapView", "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        // Transforms clicked coordinates to map coordinates.
        float tx = toMeters((x-mPosX)/mScaleFactor);
        float ty = toMeters((y-mPosY)/mScaleFactor);
        float radius = toMeters(pointRadius)*1.4f*mScaleFactor;

        Point2.Double clicked = new Point2.Double(tx, ty);

        // Searches point nearest the tapped area.
        for (MapPoint p : map.getPoints())
        {
            //Log.i("MapView", " distance ("+clicked.getX()+","+clicked.getY()+" and "+p.getX()+","+p.getY()+") "+MathUtil.distance(clicked, p)+" <= "+radius);
            Point2.Double pd = new Point2.Double(p.getX(), p.getY());
            if (MathUtil.distance(clicked, pd) <= radius)
            {
                if (listener!=null)
                    listener.onMapPointTap(p);

                break;
            }
        }

        // Searches object neares the tapped area.
        for (MapObject object : map.getObjects())
        {
            if (MathUtil.distance(clicked, object.getPosition()) <= radius)
            {
                if (listener!=null)
                    listener.onObjectTap(object);

                break;
            }
        }

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //Log.i("MapView", "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //Log.i("MapView", "onSingleTapConfirmed: " + event.toString());
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            final float scale = detector.getScaleFactor();
            //mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor * scale, 10.0f));

            if (mScaleFactor < 10.0f) {
                // 1 Grabbing
                final float centerX = detector.getFocusX();
                final float centerY = detector.getFocusY();
                // 2 Calculating difference
                float diffX = centerX - mPosX;
                float diffY = centerY - mPosY;
                // 3 Scaling difference
                diffX = diffX * scale - diffX;
                diffY = diffY * scale - diffY;
                // 4 Updating image origin
                mPosX -= diffX;
                mPosY -= diffY;
            }

            invalidate();
            return true;
        }
    }

    // ------------------------------------------------

    /**
     * Getters / setters.
     */

    /**
     *
     * @param listener
     */
    public void setListener(IMapViewListener listener) {
        this.listener = listener;
    }

    public int getGridType()
    {
        return gridType;
    }

    public void setGridType(int gridType)
    {
        this.gridType = gridType;
    }

    public Canvas getMapCanvas() {
        return mapCanvas;
    }

    public Paint getMapPaint() {
        return mapPaint;
    }

    private class LocationPoint
    {
        public Point2.Double point;
        public int color;

        private LocationPoint(Point2.Double point, int color) {
            this.point = point;
            this.color = color;
        }
    }
}