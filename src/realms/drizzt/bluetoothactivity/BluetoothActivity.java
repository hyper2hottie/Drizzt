package realms.drizzt.bluetoothactivity;


import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * This activity is used to test the BluetoothClass.  It will connect
 * to a specific device and send outputs and receive inputs based on
 * some button presses to set states.
 */
public class BluetoothActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bluetooth_activity);
	}
}
