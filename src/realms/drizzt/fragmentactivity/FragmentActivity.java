package realms.drizzt.fragmentactivity;

import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class FragmentActivity extends Activity {
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        
        //Get a fragment manager and open a transaction
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
        
        //Create the Fragment
        TestFragment fragment = new TestFragment();
        Bundle newBundle = new Bundle();
        newBundle.putString("text", "This is a programmatically added fragment");
        newBundle.putInt("color", 0x88FEDCBA);
        fragment.setArguments(newBundle);
        fragTransaction.add(R.id.layout, fragment);
        fragTransaction.commit();       
    }
    
    
    
    
}
