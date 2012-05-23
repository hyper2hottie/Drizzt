package realms.drizzt.newactivity;

import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.os.Bundle;

public class NewActivity extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity);
    }
}
