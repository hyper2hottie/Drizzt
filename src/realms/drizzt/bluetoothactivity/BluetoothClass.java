package realms.drizzt.bluetoothactivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import realms.drizzt.mainactivity.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/** This class is used to create a connection over
 * Bluetooth to a device that is set.  It is meant
 * to be a general class that can be used in many situations.
 */
public class BluetoothClass {

	/** This is the UUID for serial port service connections */
	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	/** The adapter on the phone, allows us to actually use bluetooth */
	private BluetoothAdapter mBluetoothAdapter;
	
	/** Name of the device to connect to*/
	private String mRemoteDeviceName;
	
	/** Device that is found and connected to */
	private BluetoothDevice mBluetoothDevice;
	
	/** Code for enabling bluetooth */
	private static final int REQUEST_ENABLE_BT = 1;
	
	/** The activity that contains this class */
	private Activity parentActivity;
	
	/** Threads for running/creating connections */
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	/** 
	 * Constructor.
	 */
	public BluetoothClass(Activity a)
	{
		if(a == null)
			throw new IllegalArgumentException("The parent activity can not be null.");
		else
			parentActivity = a;
	}
	
	/**
	 *  This can be used to test if bluetooth is available on the device.
	 */
	public boolean isBluetoothAvailable()
	{
		if(BluetoothAdapter.getDefaultAdapter() == null)
			return false;
		else
			return true;
	}
	
	//----------------------------------Configuration--------------------------------------
	
	/**
	 * Set the name of the remove device to connect to.
	 * @param name - The name of the device to connect
	 */
	public void setDeviceName(String name)
	{
		if(name == null)
			throw new IllegalArgumentException("Name can not be null");
		mRemoteDeviceName = name;
	}
	
	/**
	 * Get the name of the remote device we are connecting with.
	 * @return - Name of the current device to connect with
	 */
	public String getDeviceName()
	{
		return mRemoteDeviceName;
	}
	
	
	
	//-------------------------------------------------------------------------------------
	
	//-----------------------------------Creating a Connection-----------------------------
	
	/**
	 * Creates a connection.  This will handle everything, including enabling bluetooth.
	 */
	public void createConnection()
	{
		//Try get adapter for bluetooth
		if(mBluetoothAdapter == null)
			getBluetoothAdapter();
		
		//If no adapter then we can not connect
		if(mBluetoothAdapter == null)
		{
			Toast.makeText(parentActivity, R.string.bluetoothNotAvailable, Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!mBluetoothAdapter.isEnabled())
			mBluetoothAdapter.enable();
		
		//Register a reciever for bluetooth device discovery
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		parentActivity.registerReceiver(mAutoConnectReciever, filter);
		
		//Start Discovery
		mBluetoothAdapter.startDiscovery();		
	}
	
	/**
	 * Get the bluetooth adapter and store it.
	 */
	private void getBluetoothAdapter()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	/**
	 * This reciever is used for when bluetooth has finished discovery or has discovered
	 * a device.  It will connect to any device that matches the name of the current
	 * device to connect with.
	 */
	public final BroadcastReceiver mAutoConnectReciever = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			
			//Check the action
			if(action.equals(BluetoothDevice.ACTION_FOUND))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device.getName().equals(mRemoteDeviceName))
				{
					mBluetoothAdapter.cancelDiscovery();
					connect(device);
					parentActivity.unregisterReceiver(mAutoConnectReciever);
				}
			}
			else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			{
				mBluetoothAdapter.cancelDiscovery();
				parentActivity.unregisterReceiver(mAutoConnectReciever);
				//TODO: Error message
			}
		}
	};
	
	/**
	 * Connect to the input bluetooth device.  This will cancel any
	 * open connection or connection attempts currently running.
	 * 
	 * @param device - The device to connect with.
	 */
	protected void connect(BluetoothDevice device)
	{
		if(mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		if(mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
	}
	
	/**
	 * This thread attempts to conenct to the input device.  It
	 * will run until a connection succeeds or the thread is cancelled.
	 */
	private class ConnectThread extends Thread{
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;
		
		public ConnectThread(BluetoothDevice device) 
		{
			mDevice = device;
			BluetoothSocket tmp = null;
			
			//Create a socket to the device
			try
			{
				tmp = device.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
			}
			catch(IOException e)
			{
				//TODO: Give an error to the main activity
			}
			
			mSocket = tmp;
			
		}
		
		/**
		 * Run method.  This tries to connect the socket until success or
		 * is cancelled.
		 */
		public void run()
		{
			//Make sure discovery is cancelled, if not it can slow a connection
			mBluetoothAdapter.cancelDiscovery();
			
			while(!mSocket.isConnected())
			{
				// Make a connection to the BluetoothSocket
	            try 
	            {
	                // This is a blocking call and will only return on a
	                // successful connection or an exception
	                mSocket.connect();
	            } 
	            catch (IOException e) 
	            {
	            	// Close the socket
	                try
	                {
	                    mSocket.close();
	                }
	                catch (IOException e2)
	                {  }	
	            }
			}
			connected(mSocket, mDevice);
		}
		
		public void cancel() {
			try 
			{
                mSocket.close();
            } 
			catch (IOException e) 
			{ }
			
		}
		
	}

	//-------------------------------------------------------------------------------------
	
	//-----------------------------------Manage a Connection-------------------------------
	/**
    * Start the ConnectedThread to begin managing a Bluetooth connection
    * @param socket  The BluetoothSocket on which the connection was made
    * @param device  The BluetoothDevice that has been connected
    */
   public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) 
	{
	   	//Cancel the thread that completed the connection
	   if(mConnectThread != null)
	   {
		   mConnectThread.cancel();
		   mConnectThread = null;
	   }
	   
	   //Cancel any thread currently running a connection
	   if(mConnectedThread != null)
	   {
		   mConnectedThread.cancel();
	   }
	   mConnectedThread = null;
	   
	   //Start the thread to manage the connection
	   mConnectedThread = new ConnectedThread(socket);
	   mConnectedThread.start();
	}
	
   /**
    * This thread runs during a connection with a remote device.
    * It handles all incoming and outgoing transmissions.
    */
   private class ConnectedThread extends Thread
   {
	   private final BluetoothSocket mSocket;
	   private final InputStream mInStream;
	   private final OutputStream mOutStream;
	   
	   public ConnectedThread(BluetoothSocket socket)
	   {
		   mSocket = socket;
		   InputStream tmpIn = null;
		   OutputStream tmpOut = null;
		   
		   //Get the BluetoothSocket input and output streams
		   try
		   {
			   tmpIn = socket.getInputStream();
			   tmpOut = socket.getOutputStream();
		   }
		   catch(IOException e)
		   {}
		   
		   mInStream = tmpIn;
		   mOutStream = tmpOut;
	   }
	   
	   public void run()
	   {
		   byte[] buffer = new byte[1024];
		   int bytes;
		   
		   //Keep listening while connected
		   while(true)
		   {
			   try
			   {
				   bytes = mInStream.read(buffer);
			   }
			   catch(IOException e)
			   {
				   break;
			   }
		   }
	   }
	   
	   /**
	    * Write to the connected OutStream
	    * @param buffer - The bytes to write
	    */
	   public void write(char buffer)
	   {
		   try
		   {
			   mOutStream.write(buffer);
		   }
		   catch(IOException e)
		   {  }
	   }
	   
	   public void cancel()
	   {
		   try
		   {
			   mSocket.close();
		   }
		   catch(IOException e)
		   {  }
	   }
   }
   
   /**
    * Write to the bluetooth device non-synchronized
    * @param out - Bytes to write
    */
   public void write(char out)
   {
	   mConnectedThread.write(out);
   }
   
   //-------------------------------------------------------------------------------------
}
