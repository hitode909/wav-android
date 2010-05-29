package foo.com;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;

//import android.util.Log;
import android.view.Display;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.MenuItem;

public class Good extends Activity {
	
	private AudioTrack track;
	private boolean isSquare;
	private byte[] audioData;
	private int viewWidth;
	private int viewHeight;
	private int currentTone;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	this.getViewSize();
        this.setupAudio();
    }
    
    @Override
    public void onResume(){
    	this.getViewSize();
    	super.onResume(); 
    }
    
    private void clearColor() {
    	View layout = findViewById(R.id.fill);
    	layout.setBackgroundColor(Color.BLACK);
    }
    private void setColorAt(int at) {
        View layout = findViewById(R.id.fill);
        float[] hsv = new float[3];
        hsv[0] = (float) ((at / 13.0) * 360.0);
        hsv[1] = (float) 1.0;
        hsv[2] = (float) 0.5;
        Color.HSVToColor(hsv);
        layout.setBackgroundColor(-1 * 255 * 255 * 255 * at / 13);
    }

    private void getViewSize() {
    	WindowManager windowManager = getWindowManager();
    	Display display = windowManager.getDefaultDisplay();    
    	viewWidth = display.getWidth();
    	viewHeight = display.getHeight();
    }
    	
	private double getFreqFromXY(double x, double y) {
		double freq = 55.0 * Math.pow(2, (int)(x / (viewWidth / 4)) + 1);
		int to = (int) (y * 13 / (double)(viewHeight));
		currentTone = to;

		for(int i = 0; i < to;i++) {
			freq *= 1.05946;
		}
		return freq;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			this.clearColor();
			return super.onTouchEvent(event);
		}
		int lastTone = this.currentTone;
		this.play(this.getFreqFromXY(event.getX(), event.getY()), event.getX() / 400.0);
		if (this.currentTone != lastTone) {
			this.setColorAt(currentTone);
		}

        return super.onTouchEvent(event);
	}
	
	private void setupAudio() {
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_DEFAULT, 8000, AudioTrack.MODE_STREAM);
		isSquare = true;
		
		audioData = new byte[2000];

		track.play();
	}
	
	private double filterSignal(double signal, double factor) {
		if (this.isSquare) {
			return signal > 0 ? 0.5 : 0;
		}
		return signal;
	}
	

	private void play(double freq, double factor) {
		double t = 0.0;
		double dt = 1.0 / 22050;
		double fade = 0.0;
		int fadeDuration = 100;
		int fadeOutFrom = audioData.length - fadeDuration;
		double fadeStep = 1.0 / fadeDuration;

		for (int i = 0; i < audioData.length; i++, t += dt) {
			if (i < fadeDuration) fade+= fadeStep;
			if (i > fadeOutFrom) fade-= fadeStep;
			audioData[i] = (byte) (Byte.MAX_VALUE * this.filterSignal(Math.sin(Math.PI * t * freq) * fade, factor));}
		
		track.write(audioData, 0, audioData.length);
	}
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);        
        outState.putBoolean("isSquare",this.isSquare );
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.isSquare = savedInstanceState.getBoolean("isSquare");
        this.getViewSize();
    }
    
	final static int CONTEXT_ITEM_ID1 = 1000;
	final static int CONTEXT_ITEM_ID2 = 1001;

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.menu_sin:
			this.isSquare = false;
			break;
		case R.id.menu_square:
			this.isSquare = true;
			break;
		}
    	return true;
    }


}

