package realms.drizzt.movingcircle;

import realms.drizzt.mainactivity.R;
import realms.drizzt.movingcircle.MovingCircleView.MovingCircleThread;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MovingCircleFragment extends Fragment {
	
	/** A handle to the thread that runs the circle. */
    private MovingCircleThread circleThread;
    
    /** A handle to the view where the circle is running. */
    private MovingCircleView circleView;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		//Inflate the view to return
		View toReturn = inflater.inflate(R.layout.circle_fragment, container, false);	
		
		//Get the circle view and thread
        circleView = (MovingCircleView) toReturn.findViewById(R.id.movingCircle);
        circleThread = circleView.getThread();
        
		return toReturn;
	}
 
    /**
     * Invoked when Activity loses user focus.
     */
    @Override
	public void onPause()
    {
    	super.onPause();
    	circleView.getThread().pause();
    }
    
    /**
     * Invoked when Activity regains focus
     */
    @Override
	public void onResume()
    {
    	super.onResume();
    	circleView.getThread().unpause();
    }    
}
