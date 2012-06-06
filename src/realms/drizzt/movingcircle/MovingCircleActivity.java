package realms.drizzt.movingcircle;

import java.util.Timer;
import java.util.TimerTask;

import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

public class MovingCircleActivity extends Activity implements MovingCircleListener {

	/** A task that sets the circle offset textbox */
	TimerTask circleTask;
	
	/** A timer that runs the circle task */
	Timer circleTimer;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moving_circle_activity);
        
        	
    }
    
    /** Called when the activity gains focus */
    @Override
    public void onResume()
    {
    	super.onResume();
    	circleTimer = new Timer();
    	circleTask = new TimerTask() {
			
			@Override
			public void run() {
				MovingCircleFragment fragment = (MovingCircleFragment)getFragmentManager().findFragmentById(R.id.movingCircleFragment);
				final TextView text = (TextView)findViewById(R.id.movingCircleTextView);
				final Pair<Integer, Integer> offset = fragment.getCircleOffset();
				runOnUiThread(new Runnable()
				{
					@Override
					public void run() {
						text.setText(offset.first.toString() + ", " + offset.second.toString());
					}
					
				});
				
				
			}
		};    
    	circleTimer.schedule(circleTask, 0, 20);
    	
    	//Register this as a listener to the MovingCircleView
    	MovingCircleFragment fragment = (MovingCircleFragment)getFragmentManager().findFragmentById(R.id.movingCircleFragment);
    	fragment.registerListener(this);
    }
    
    /** Called when losing focus */
    @Override
    public void onPause()
    {
    	super.onPause();
    	circleTimer.cancel();
    	circleTimer.purge();
    	circleTask.cancel();
    	circleTimer = null;    	
    	circleTask = null;
    	
    	//unregister this as a listener to the MovingCircleView
    	MovingCircleFragment fragment = (MovingCircleFragment)getFragmentManager().findFragmentById(R.id.movingCircleFragment);
    	fragment.unregisterListener(this);
    }

	
    /**
     * When the circle moves then we want to change
     * the value in the text box.
     */
    @Override
	public void onCircleMoved(final int X, final int Y) {
    	runOnUiThread(new Runnable()
		{
			@Override
			public void run() {
				((TextView)findViewById(R.id.movingCircleTextViewInterface)).setText(X + ", " + Y);
			}
			
		});
	}
}
