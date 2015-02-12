package thenoob.blackbox.joystickdroid;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
public class FluxView extends View {
	private SharedPreferences pref;
	private Network connection;
	//private RectF screen;
	private int width, height;
	private float maxsize;
	private SensorManager sensorManager;
	private Sensor accel;
	public Frame leftFrame, rightFrame;
	private Context context;
	private boolean accelerometer, vibratrion;
	//keys used in saving bindings
	public static final String LEFT_TOP = "left_top";
	public static final String LEFT_RIGHT = "left_right";
	public static final String LEFT_BOTTOM = "left_bottom";
	public static final String LEFT_LEFT = "left_left";
	public static final String RIGHT_TOP = "right_top";
	public static final String RIGHT_RIGHT = "right_right";
	public static final String RIGHT_BOTTOM = "right_bottom";
	public static final String RIGHT_LEFT = "right_left";
	public static final String RIGHT_DISC_RADIUS = "right_disc_radius";
	public static final String LEFT_DISC_RADIUS = "left_disc_radius";
	public static final String LEFT_DISC_BINDING = "binding_ring_left";
	public static final String RIGHT_DISC_BINDING = "binding_ring_right";
	public static final int BINDING_NONE = 1000;
	public static final String UNDEFINED = "";
	public static final int RELEASE = 500;
	public static final int PRESS = 0;
	public static final int DO_NOTHING = -500;
	private static final long VIBRATE_EXIT = 50;
	private static final long VIBRATE_ENTER = 70;
	public FluxView(Context context) {
		super(context);
		
	}

	public FluxView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
	}

	public FluxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		connect();
		
	}
	public FluxView(Context context, AttributeSet attrs, Network connection) {
		super(context, attrs);
		this.connection = connection;
	}
	
	public void connect() {
		pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		String hostname = pref.getString(Main.COMP_NAME, "workstation").trim();
		if(hostname != null) {
			Log.e("hostname", hostname);
			connection = new Network(hostname);
			connection.start();
		}
	}
	
	public void disconnect() {
		if(connection != null) {
			try {
				connection.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	public Frame getLeftFrame() {
		return leftFrame;
	}
	
	public Frame getRightFrame() {
		return rightFrame;
	}
	
	private void initialize() {
		width = getWidth();
		height = getHeight();
		maxsize = (260.0f/720.0f) * height;
		//screen = new RectF(0, 0, width, height);
		leftFrame = new Frame(0, 0, width/2, height);
		rightFrame = new Frame(0, width/2 ,width, height);
		
		Ring left = new Ring(width/4, height/2, 260, BINDING_NONE);
		left.setRingColor(Color.rgb(28, 229, 146));
		Ring right = new Ring(width - width/4, height/2, 150, 'W');
		right.setRingColor(Color.rgb(255, 178, 56));
		leftFrame.attachRing(left);
		rightFrame.attachRing(right);
		//sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		//accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//sensorManager.registerListener(new Accelerometer(), accel, SensorManager.SENSOR_DELAY_NORMAL);
		loadPreference();
		
	}
	
	class Accelerometer implements SensorEventListener {
		private boolean callibrate;
		float x, y, z;
		Timer timer;
		private boolean pressed;
		int key;
		double rz, rx, ry, irz, iry, irx;
		final static double THRESHOLD = 0.1;
		final static double THRESHOLD_ANGLE = 7;
		
		public Accelerometer() {
			key = 1000;
			pressed = false;
			timer = new Timer("accelerometer");
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						if(key < 1000 && !pressed)
						connection.send(key+"");
						pressed = true;
					}
					catch(Exception e) {
						
					}
					
				}
			}, 0, 200);
		}
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			
			
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			float cx = 0, cy = 0, cz = 0;
			double crx = 0, cry = 0, crz = 0;
			if(!callibrate) {
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];
				rz = Math.acos(z/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				ry = Math.acos(y/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				rx = Math.acos(x/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				irz = Math.acos(z/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				iry = Math.acos(y/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				irx = Math.acos(x/Math.sqrt(x*x + y*y + z*z))*(180/Math.PI);
				callibrate = true;
			}
			else {
				cx = event.values[0];
				cy = event.values[1];
				cz = event.values[2];
				crz = Math.acos(cz/Math.sqrt(cx*cx + cy*cy + cz*cz))*(180/Math.PI);
				crx = Math.acos(cx/Math.sqrt(cx*cx + cy*cy + cz*cz))*(180/Math.PI);
				cry = Math.acos(cy/Math.sqrt(cx*cx + cy*cy + cz*cz))*(180/Math.PI);
				 if(Math.abs(crz - irz) > THRESHOLD_ANGLE && cry < iry - THRESHOLD_ANGLE/2){
					 try {
						 //connection.send('A'); 
						 key = 'A';
					 }
					 catch(Exception e) {
						 e.printStackTrace();
					 }
				 }
				else if(Math.abs(crz - irz) > THRESHOLD_ANGLE && cry > iry + THRESHOLD_ANGLE/2){
					try {
						//connection.send('D');
						key = 'D';
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
				else {
					key += 500;
					pressed = false;
				}
				
			}
			x = cx; y = cy; z = cz;
			rx = crx; rz = crz; ry = cry;
			
		}
	}
	
	public void loadPreference() {
		int top, right, bottom, left, ringleft, ringright;
		pref = PreferenceManager.getDefaultSharedPreferences(getContext());
		Ring leftring, rightring;
		//LEFT FRAME
		top = Utility.getparsedInt(pref.getString(LEFT_TOP, context.getString(R.string.E)));
		right = Utility.getparsedInt(pref.getString(LEFT_RIGHT, context.getString(R.string.D)));
		bottom = Utility.getparsedInt(pref.getString(LEFT_BOTTOM, context.getString(R.string.S)));
		left = Utility.getparsedInt(pref.getString(LEFT_LEFT, context.getString(R.string.A)));
		leftFrame.setBinding(new int[]{top, right, bottom, left}, 
				new String[]{getKeyName(top), getKeyName(right), getKeyName(bottom), getKeyName(left)});
		
		//RIGHT FRAME 
		top = Utility.getparsedInt(pref.getString(RIGHT_TOP, context.getString(R.string.NONE)));
		right = Utility.getparsedInt(pref.getString(RIGHT_RIGHT, context.getString(R.string.NONE)));
		bottom = Utility.getparsedInt(pref.getString(RIGHT_BOTTOM, context.getString(R.string.NONE)));
		left = Utility.getparsedInt(pref.getString(RIGHT_LEFT, context.getString(R.string.NONE)));
		rightFrame.setBinding(new int[]{top, right, bottom, left}, 
				new String[]{getKeyName(top), getKeyName(right), getKeyName(bottom), getKeyName(left)});
		
		rightring = rightFrame.getRing();
		leftring = leftFrame.getRing();
		ringleft = pref.getInt(LEFT_DISC_RADIUS, 100);
		ringright = pref.getInt(RIGHT_DISC_RADIUS, 57);
		rightring.setRadius((int)Math.floor((ringright)*(maxsize/100.0f)));
		leftring.setRadius((int)Math.floor(ringleft*(maxsize/100.0f)));
		ringleft = Utility.getparsedInt(pref.getString(LEFT_DISC_BINDING, context.getString(R.string.NONE)));
		ringright = Utility.getparsedInt(pref.getString(RIGHT_DISC_BINDING, context.getString(R.string.W)));
		rightring.setBinding(ringright, getKeyName(ringright));
		leftring.setBinding(ringleft, getKeyName(ringleft));
		
		//Accelerometer and vibration;
		accelerometer = pref.getBoolean("accelerometer", true);
		vibratrion = pref.getBoolean("vibration", true);
	}
	
	private String getKeyName(int key) {
		
		if(key >= 'A' && key <= 'Z' || (key >= '0' && key <= '9')) {
			return ((char)key)+"";
		}
		else if(key == Utility.VK_ALT)
			return "Alt";
		else if(key == Utility.VK_CAPSLOCK)
			return "Caps";
		else if(key == Utility.VK_CTRL)
			return "Ctrl";
		else if(key == Utility.VK_ENTER)
			return "Enter";
		else if(key == Utility.VK_UP)
			return "Up";
		else if(key == Utility.VK_RIGHT)
			return "Right";
		else if(key == Utility.VK_DOWN)
			return "Down";
		else if(key == Utility.VK_LEFT)
			return "Left";
		else if(key == Utility.VK_PAGEDOWN)
			return "PgDn";
		else if(key == Utility.VK_PAGEUP)
			return "PgUp";
		else if(key == Utility.VK_SHIFT)
			return "Shift";
		else if(key == Utility.VK_ESC)
		return "Esc";
		else if(key == Utility.VK_TAB)
			return "Tab";
		else 
			return "";
		
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int pointerindex = event.getActionIndex();
		int action = event.getActionMasked();
		switch(action){
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			if(leftFrame.isInside(event.getX(pointerindex), event.getY(pointerindex)) && 
				!leftFrame.isPointerAttached()){
				leftFrame.attachPointer(pointerindex);
				leftFrame.getRing().moveTo(event.getX(pointerindex), event.getY(pointerindex));
				pressRing(leftFrame);
				invalidate();
			}
			else if(!rightFrame.isPointerAttached() && 
					rightFrame.isInside(event.getX(pointerindex), event.getY(pointerindex))) {
				rightFrame.attachPointer(pointerindex);
				rightFrame.getRing().moveTo(event.getX(pointerindex), event.getY(pointerindex));
				pressRing(rightFrame);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			for(int i = 0; i < event.getPointerCount(); i++) {
				if(rightFrame.isPointerAttached() && i == rightFrame.getPointer()){
					if(rightFrame.isInside(event.getX(i), event.getY(i)))
					rightFrame.getRing().moveTo(event.getX(i), event.getY(i));
					}
				if(leftFrame.isPointerAttached() && i == leftFrame.getPointer()) {
					if(leftFrame.isInside(event.getX(i), event.getY(i)))
					leftFrame.getRing().moveTo(event.getX(i), event.getY(i));
				}
			}
			if(rightFrame.isPointerAttached()){
				if(rightFrame.isInside(event.getX(pointerindex), event.getY(pointerindex)))
				rightFrame.getRing().moveTo(event.getX(pointerindex), event.getY(pointerindex));
				}
			if(leftFrame.isPointerAttached()) {
				if(leftFrame.isInside(event.getX(pointerindex), event.getY(pointerindex)))
				leftFrame.getRing().moveTo(event.getX(pointerindex), event.getY(pointerindex));
			}
			pressKey(leftFrame);
			pressKey(rightFrame);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			if(pointerindex == leftFrame.getPointer() && leftFrame.isPointerAttached()){
				leftFrame.detachPointer();
				releaseRing(leftFrame);
			}
			else if(pointerindex == rightFrame.getPointer() && rightFrame.isPointerAttached()){
				rightFrame.detachPointer();
				releaseRing(rightFrame);
			}
		}
		invalidate();
		return true;
	}
	private void vibrate(long d)
	{
		Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

		if(v != null && vibratrion)
		{
			v.vibrate(d);
		}
	}
	private void pressKeySide(Frame frame, int side, int regionCode){
		int ring [] = new int[]{Ring.TOP, Ring.RIGHT, Ring.BOTTOM, Ring.LEFT};
		Log.e("REG CODE = ", regionCode+"");
		if((regionCode & ring[side]) != 0 &&
				!frame.isDisabled(side) && 
				!frame.isPressed(side) &&
				frame.getBinding(side) != BINDING_NONE) {
			try {
				int key = frame.getBinding(side);
				connection.send(key+"");
			}
			catch(Exception e){
				e.printStackTrace();
			}
			frame.setPressed(side, true);
			vibrate(VIBRATE_ENTER);
		}
		if((regionCode & ring[side]) == 0 &&
				!frame.isDisabled(side) && 
				frame.isPressed(side) && 
				frame.getBinding(side) != BINDING_NONE) {
			frame.setPressed(side, false);
			try{
				int key = frame.getBinding(side);
				key += RELEASE;
				connection.send(key+"");
			}
			catch(Exception e){
				e.printStackTrace();
			}
			vibrate(VIBRATE_EXIT);
		}
		
	}
	
	private void pressKey(Frame frame){
		int regionCode = frame.isTouching();
		int side[] = new int[]{Frame.TOP, Frame.RIGHT, Frame.BOTTOM, Frame.LEFT};
		for(int i = 0; i < side.length; i++) {
			pressKeySide(frame, side[i], regionCode);
		}
		
	}
	
	private void pressRing(Frame frame){
		Ring ring = frame.getRing();
		if(ring.getBinding() != FluxView.BINDING_NONE) {
			try {
				int key = ring.getBinding();
				connection.send(key+"");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void releaseRing(Frame frame){
		Ring ring = frame.getRing();
		if(ring.getBinding() != FluxView.BINDING_NONE) {
			try {
				int key = ring.getBinding();
				key += RELEASE;
				connection.send(key+"");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		initialize();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		rightFrame.drawFrame(canvas);
		leftFrame.drawFrame(canvas);
	}
}

//The main Frame Class 
@SuppressLint("RtlHardcoded")
class Frame {
	private int top, left, right, bottom;
	public static final int TOP = 0;
	@SuppressLint("RtlHardcoded")
	public static final int RIGHT = 1;
	public static final int BOTTOM = 2;
	public static final int LEFT = 3;
	private static final float STROKE_WIDTH = 20;
	private static final int TEXT_SIZE = 35;
	private static final int DETACH = -100;
	private boolean disabled[] = {false, false, false, false};
	private boolean pressed[] = {false, false, false, false};
	private boolean pointerAttached;
	private int pointerindex;
	private int colors[] = new int[4];
	private Paint paint, textPaint;
	private Ring ring;
	private int binding[];
	private String bindname[];
	public Frame(int top, int left, int right, int bottom){
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.ring = null;
		this.pointerAttached = false;
		this.pointerindex = DETACH;
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(Color.argb(255, 255, 0, 0));
		paint.setStrokeWidth(STROKE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		textPaint = new Paint();
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		textPaint.setAntiAlias(true);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(TEXT_SIZE);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setShadowLayer(1, 2, 2, 0xff000000);
		colors[0] = Color.TRANSPARENT;
		colors[1] = Color.TRANSPARENT;
		colors[2] = Color.TRANSPARENT;
		colors[3] = Color.TRANSPARENT;
		int bindings[] = new int[]{1000, 1000, 1000, 1000};
		String bindname[] = new String[]{"","","","",""};
		this.binding = bindings;
		this.bindname = bindname;
	}
	public void resetButtons(){
		Arrays.fill(this.pressed, false);
	}
	public void PrintDimen(){
		Log.e("TOP", top +"");
		Log.e("LEFT", left+"");
		Log.e("Right", right+"");
		Log.e("BOTTOM", bottom+"");
	}
	public void setDisabled(int side, boolean disabled){
		if(side >= TOP && side <= LEFT) {
			this.disabled[side] = disabled;
		}
	}
	
	public boolean isDisabled(int side){
		if(side >= TOP && side <= LEFT && this.disabled[side])
			return true;
		return false;
	}
	public void setPressed(int side, boolean value){
		if(side >= TOP && side <= LEFT) {
			this.pressed[side] = value;
		}
	}
	
	public boolean isPressed(int side) {
		if(side >= TOP && side <= LEFT) {
			return this.pressed[side];
		}
		return false;
	}
	
	public void attachPointer(int pointerindex){
		this.pointerAttached = true;
		this.pointerindex = pointerindex;
	}
	
	public void detachPointer(){
		this.pointerAttached = false;
		this.pointerindex = -1;
		resetButtons();
	}
	public boolean isPointerAttached(){
		return this.pointerAttached;
	}
	public boolean isRingAttacched(){
		if(ring == null)
			return false;
		return true;
	}
	public int getPointer(){
		return this.pointerindex;
	}
	
	public void attachRing(Ring ring){
		this.ring = ring;
	}
	
	public void detachRing(){
		this.ring = null;
	}
	
	public Ring getRing(){
		return ring;
	}
	public boolean isInside(float x, float y){
		float radius = ring.getRadius()* 0.8f;
		if(x >= (left + radius) && x < (right - radius) &&
				y >= top + radius && y <= bottom - radius){
			return true;
		}
		return false;
	}
	public boolean isPointerinside(float x, float y) {
		if(x >= left  && x < right &&
		   y >= top && y <= bottom ){
			return true;
		}
		return false;
	}
	public void setBinding(int position, int keyCode, String name){
		if(position >= TOP && position <= LEFT) {
			binding[position] = keyCode;
			bindname[position] = name;
		}
	}
	public void setBinding(int binding[], String names[]){
		this.binding = binding;
		this.bindname = names;
	}
	public int getBinding(int position) {
		if(position >= TOP && position <= LEFT) {
			return binding[position];
		}
		else
			return FluxView.BINDING_NONE;
	}
	
	public int isTouching(){
		RectF screen = new RectF(left, top, right, bottom);
		return ring.isTouching(screen);
	}
	
	private void drawText(int position, Canvas canvas){
		int offset = 15;
		float x = 0, y = 0;
		if(position >= TOP && position <= LEFT) {
			String text = "";
			if(binding[position] != FluxView.BINDING_NONE) {
				text = bindname[position];
			}
			else {
				text = "";
			}
			
			switch(position) {
			case TOP:
				x = (left + right)/2;
				y = STROKE_WIDTH + offset;
				break;
			case RIGHT:
				x = right - STROKE_WIDTH - offset;
				y = (top + bottom)/2;
				break;
			case BOTTOM:
				x = (left + right)/2;
				y = bottom - offset;
				break;
			case LEFT:
				x = left + STROKE_WIDTH + offset;
				y = (top + bottom)/2;
				break;
			}
			canvas.drawText(text, x, y, textPaint);
		}
	}
	
	public void drawFrame(Canvas canvas){
		if(!disabled[TOP]) {
			paint.setColor(colors[TOP]);
			canvas.drawLine(left, top, right, top, paint);
			drawText(TOP, canvas);
		}
		if(!disabled[RIGHT]) {
			paint.setColor(colors[RIGHT]);
			canvas.drawLine(right, 0, right, bottom, paint);
			drawText(RIGHT, canvas);
		}
		if(!disabled[BOTTOM]) {
			paint.setColor(colors[BOTTOM]);
			canvas.drawLine(right, bottom, left, bottom, paint);
			drawText(BOTTOM, canvas);
		}
		if(!disabled[LEFT]) {
			paint.setColor(colors[LEFT]);
			canvas.drawLine(left, top, left, bottom, paint);
			drawText(LEFT, canvas);
		}
		ring.drawRing(canvas);
	}
	
	
}

//The Disk Controllers
class Ring {
	private float cx, cy, radius;
	private Paint paint, textPaint;
	private int binding;
	private String bindname;
	private boolean enabled;
	public static final int INSIDE = -1;
	public static final int OUTSIDE = 1;
	public static final int ONLINE = 0;
	public static final int TOP = 1;
	public static final int RIGHT = 2;
	public static final int BOTTOM = 4;
	public static final int LEFT = 8;
	
	public Ring(int cx, int cy, int radius, int keyCode) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
		this.binding = keyCode;
		this.enabled = false;
		this.bindname = "";
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(Color.argb(255, 255, 0, 0));
		paint.setStrokeWidth(20f);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
		textPaint = new Paint();
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		textPaint.setAntiAlias(true);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(35);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setShadowLayer(1, 2, 2, 0xff000000);
		
	}
	public void setRingColor(int color){
		paint.setColor(color);
	}
	public Ring(int cx, int cy, int radius, int keyCode, Paint paint) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
		this.paint = paint;
		this.binding = keyCode;
		this.enabled = false;
	}
	public void enableBitmap(boolean value){
		this.enabled = value;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public float getRadius(){
		return radius;
	}
	public void moveTo(float x, float y){
		this.cx = x;
		this.cy = y;
	}
	public void setStrokeWidth(float strokeWidth) {
		paint.setStrokeWidth(strokeWidth);
	} 
	public void setBinding(int keycode, String name){
		this.binding = keycode;
		this.bindname = name;
	}
	public int getBinding() {
		return this.binding;
	}
	private void drawText(Canvas canvas){
		String text ="";
		if(binding != FluxView.BINDING_NONE){
			text = bindname;	
		}
		canvas.drawText(text, cx , cy, textPaint);
	}
	private void drawRingPath(Canvas canvas) {
		RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
		canvas.drawArc(oval, 0, 360, false, paint);
	}
	private void drawRingCircle(Canvas canvas){
		Paint p = new Paint(paint);
		p.setStyle(Paint.Style.FILL);
		p.setAlpha(100);
		canvas.drawCircle(cx, cy, radius - 10f, p);
	}
	public void drawRing(Canvas canvas) {
		if(!enabled) {
			drawRingPath(canvas);
			drawRingCircle(canvas);
		}
		drawText(canvas);
	}
	
	public int isPoint(float x, float y) {
		double distance = Math.sqrt((cx - x)*(cx - x) + (cy - y)*(cy - y));
		if(distance < radius){
			return INSIDE;
		}
		else if(distance > radius){
			return OUTSIDE;
		}
		else {
			return ONLINE;
		}
	}
	public int isTouching(RectF screen){
		int region = 0;
		if(cy - radius <= screen.top){
			region |= TOP;
		}
		if(cx - radius <= screen.left){
			region |= LEFT;
		}
		if(cx + radius >= screen.right){
			region |= RIGHT;
		}
		if(cy + radius >= screen.bottom) {
			region |= BOTTOM;
		}	
		return region;
	}
}

