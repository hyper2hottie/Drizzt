package realms.drizzt.mainactivity;

import realms.drizzt.fragmentactivity.FragmentActivity;
import realms.drizzt.newactivity.NewActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DrizztActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    /**
     * This is called by a button to create a new activity..
     * The activity it creates is empty except for some simple text.
     */
    public void createActivity(View view)
    {
    	Intent newActivityIntent = new Intent(this, NewActivity.class);
    	startActivity(newActivityIntent);
    }
    
    /**
     * This is called by a button to create a new activity with a fragment in it.
     * The activity it creates is empty except for some simple text and a fragment with more text.
     */
    public void createFragmentActivity(View view)
    {
    	Intent newActivityIntent = new Intent(this, FragmentActivity.class);
    	startActivity(newActivityIntent);
    }
}