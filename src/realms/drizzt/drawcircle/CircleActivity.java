package realms.drizzt.drawcircle;

import realms.drizzt.drawcircle.CircleView.CircleThread;
import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.os.Bundle;

public class CircleActivity extends Activity {

	/** A handle to the thread that runs the circle. */
    private CircleThread circleThread;
    
    /** A handle to the view where the circle is running. */
    private CircleView circleView;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_activity);
        
        //Get the circle view and thread
        circleView = (CircleView) findViewById(R.id.circle);
        circleThread = circleView.getThread();
    }
 
    /**
     * Invoked when Activity loses user focus.
     */
    @Override
    protected void onPause()
    {
    	super.onPause();
    	circleView.getThread().pause();
    }
    
    /**
     * Invoked when Activity regains focus
     */
    @Override
    protected void onResume()
    {
    	super.onResume();
    	circleView.getThread().unpause();
    }    
    
}
