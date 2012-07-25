package realms.drizzt.bluetoothactivity;


import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is used to test the BluetoothClass.  It will connect
 * to a specific device and send outputs and receive inputs based on
 * some button presses to set states.
 */
public class BluetoothActivity extends Activity {
	
	/** The bluetooth class that will be used to communicate over bluetooth */
	private BluetoothClass mBluetooth;
	
	/** Dialog for user to know a connection is being made. */
	private ProgressDialog mDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bluetooth_activity);
		
		mBluetooth = new BluetoothClass(this, mHandler);
		if(!mBluetooth.isBluetoothAvailable())
		{
			Toast.makeText(this, "Bluetooth is not available.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(mBluetooth.isConnected())
		{
			mBluetooth.setDeviceName("bToothTest");
			mBluetooth.autoConnect();
		}
	}
	
	/** Activity is done.  Cancel threads. */
	public void onDestroy()
	{
		super.onDestroy();
		if(mBluetooth != null)
		{
			mBluetooth.cancel();
			mBluetooth = null;
		}
		
		if(mDialog != null)
		{
			mDialog.cancel();
			mDialog = null;
		}
	}
	
	//-------------------------------------Dialogs-------------------------------------
	
	/**
	 * Display a dialog indicating that a connection is being made.
	 */
	private void displayConnectingDialog()
	{
		mDialog = new ProgressDialog(this);
		mDialog.setMessage("Connecting Bluetooth");
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.setOnCancelListener(cancelConnectingListener);
		mDialog.setButton("Cancel", dialogButtonListener);
		mDialog.show();
	}
	
	/**
	 * Listener for buttons in dialog indicating bluetooth progress.
	 */
	private final OnClickListener dialogButtonListener = new OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			switch(which)
			{
			case -1 :
				//Cancel connecting
				mBluetooth.cancel();
				if(mDialog != null)
					mDialog.dismiss();
				mDialog = null;
				break;
			}
			
		}
		
	};
	
	/**
	 * Listener for dialog cancel, cancels making a connection.
	 */
	private final OnCancelListener cancelConnectingListener = new OnCancelListener()
	{
		
		@Override
		public void onCancel(DialogInterface dialog) 
		{
			//Cancel connecting
			mBluetooth.cancel();
			if(mDialog != null)
				mDialog.dismiss();
			mDialog = null;
			
		}
	};
	
	//---------------------------------------------------------------------------------
	
	//--------------------------Handlers-----------------------------------------------
	/**
	 * Handles a button press by the onButton.  Sends an "On" signal.
	 * @param view - The calling view
	 */
	public void onButtonClick(View view)
	{
		sendData('A');
	}
	
	/**
	 * Handles a button press by the offButton.  Sends an "Off" signal.
	 * @param view - The calling view
	 */
	public void offButtonClick(View view)
	{
		sendData('B');
	}
	
	/**
	 * This class will handle any messages that come from
	 * the bluetooth class.
	 */
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			//Bundle data = msg.getData();
			//for(String key : data.keySet())
			//{
				switch(msg.what)
				{
				case BluetoothClass.MESSAGE_CONNECTION_COMPLETE:
					if(mDialog != null)
					{
						mDialog.dismiss();
						mDialog = null;
					}
					break;
				case BluetoothClass.MESSAGE_DEVICE_NAME:
					String deviceName = msg.getData().getString(BluetoothClass.BUNDLE_DEVICE_NAME); 
					((TextView)findViewById(R.id.device)).setText(deviceName);
					break;
				case BluetoothClass.MESSAGE_CONNECTION_LOST:
					Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_LONG).show();
					((TextView)findViewById(R.id.device)).setText("No Connection");
					break;
				case BluetoothClass.MESSAGE_ERROR_CONNECTING:
					Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_LONG).show();
					mBluetooth.cancel();
					if(mDialog != null)
						mDialog.dismiss();
					mDialog = null;
					break;
				}
			//}
						
		}
	};
	
	//---------------------------------------------------------------------------------
	
	//--------------------------Bluetooth Management-----------------------------------
	
	/**
	 * Sends data over bluetooth.
	 * @param data - The data to write to the bluetooth stream
	 */
	protected void sendData(char data)
	{
		if(mBluetooth != null)
		{
			mBluetooth.write(data);
		}
	}
	
	/**
	 * Connect to bluetooth.
	 * @param view - The calling view
	 */
	public void connect(View view)
	{
		displayConnectingDialog();
		mBluetooth.setDeviceName("robot-AAC2");
		mBluetooth.autoConnect();
	}
	//---------------------------------------------------------------------------------
}
