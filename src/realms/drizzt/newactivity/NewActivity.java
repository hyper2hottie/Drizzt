package realms.drizzt.newactivity;

import realms.drizzt.mainactivity.DrizztActivity;
import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class NewActivity extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity);
    }
    
    /**
     * This is called by a button to start that activity that started it.
     */
    public void createActivity(View view)
    {
    	Intent newActivityIntent = new Intent(this, DrizztActivity.class);
    	startActivity(newActivityIntent);
    }
}
