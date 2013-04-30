package com.example.toarduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;






import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	private static final String TAG = "B";
	    private static BluetoothAdapter wBluetoothAdapter = null; 
		private static BluetoothSocket wBluetoothSocket = null; 
		private static final UUID theUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); 
	    private static  OutputStream wOutputStream = null;
	    private final int REQUEST_ENABLE_BT=1;
	    private ConnectThread wConnectThread;
	    private ConnectedThread wConnectedThread;
	    byte[] buffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
	wBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	if (wBluetoothAdapter == null) {		
		    
		    Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();			
		    return;			
		}						         									
		if (!wBluetoothAdapter.isEnabled()) {						    			
		        Intent mIntentOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);			
		        startActivityForResult(mIntentOpenBT, REQUEST_ENABLE_BT);			
		}
		wDiscoverable();
		
	}
	@Override
	protected void onStart() {		
	    
	    super.onStart();
	}

	public void connectclick(View view){
		 BluetoothDevice device = wBluetoothAdapter.getRemoteDevice("00:06:66:49:58:AE");//00:06:66:49:58:AE
		 wConnectThread = new ConnectThread(device);
         wConnectThread.start();
	}
	 private void wDiscoverable() {
	        Log.i(TAG, "ensure discoverable");
	        if (wBluetoothAdapter.getScanMode() !=
	            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
	            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	            startActivity(discoverableIntent);
	        }
	    }

	
	
	 
	 private class ConnectThread extends Thread {
		    private final BluetoothSocket mmSocket;
		    private final BluetoothDevice mmDevice;
		 
		    public ConnectThread(BluetoothDevice device) {
		        // Use a temporary object that is later assigned to mmSocket,
		        // because mmSocket is final
		    	Log.i(TAG, "device=" + device);
		        BluetoothSocket tmp = null;
		        mmDevice = device;
		 
		        // Get a BluetoothSocket to connect with the given BluetoothDevice
		        try {
		            // MY_UUID is the app's UUID string, also used by the server code
		            tmp = device.createRfcommSocketToServiceRecord(theUUID);
		        } catch (IOException e) { }
		        mmSocket = tmp;
		    }
		 
		
			public void run() {
		    	Log.i(TAG, "BEGIN ConnectThread");
		        // Cancel discovery because it will slow down the connection
		        wBluetoothAdapter.cancelDiscovery();
		
		    	   
		       
		 
		        try {
		        	Log.i(TAG, "connecting");
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		           
		          
		        } catch (IOException connectException) {
		        	Log.e(TAG, "disconnect", connectException);
		            // Unable to connect; close the socket and get out
		            try {
		            	Log.i(TAG, "close");
		                mmSocket.close();
		                
		            } catch (IOException closeException) {Log.e(TAG, "disclose", closeException);}
		            return;
		        }
		       
		        // Do work to manage the connection (in a separate thread)
		        synchronized (Main.this) {
	                wConnectThread = null;
	            }
		       connected(mmSocket);
		    }
		    public void cancel() {
		        try {
		            mmSocket.close();
		        } catch (IOException e) { Log.e(TAG, "discancel", e);}
		    }
		}
	 private class ConnectedThread extends Thread {
		    private final BluetoothSocket mmSocket;
		    private final InputStream mmInStream;
		    private final OutputStream mmOutStream;
		    Intent data;
		   
		 
		    public ConnectedThread(BluetoothSocket socket) {
		        mmSocket = socket;
		        InputStream tmpIn = null;
		        OutputStream tmpOut = null;
		       
		 
		        // Get the input and output streams, using temp objects because
		        // member streams are final
		        try {
		            tmpIn = socket.getInputStream();
		            tmpOut = socket.getOutputStream();
		            
		        } catch (IOException e) { }
		 
		        mmInStream = tmpIn;
		        mmOutStream = tmpOut;
		    }
		 
		    public void run() {
		        byte[] buffer = new byte[1024];  // buffer store for the stream
		        int bytes; // bytes returned from read()
		        
		 
		        // Keep listening to the InputStream until an exception occurs
		        while (true) {
		            try {
		                // Read from the InputStream
		                bytes = mmInStream.read(buffer);
		                byte[] readBuf = (byte[]) buffer;
		                String i = new String(readBuf, 0, bytes);
		                Log.i(TAG, i);
		                // construct a string from the valid bytes in the buffer
		               
		               
		                Log.i(TAG, "read");
		                //text.setText(i);
		            } catch (IOException e) {Log.i(TAG, "noread");
		                break;
		            }
		        }
		      
		        
		    }
		 
		     //Call this from the main activity to send data to the remote device 
		    public void write(byte[] buffers) {
		        try {
		        
		        	
		            mmOutStream.write(buffers);
		            String g = new String(buffers);
		            Log.i(TAG,"write" + g);
		        } catch (IOException e) {Log.i(TAG, "nowrited"); }
		    }
		 
		    //Call this from the main activity to shutdown the connection 
		    public void cancel() {
		        try {
		            mmSocket.close();
		        } catch (IOException e) { }
		    }
		}
	 public synchronized void connected(BluetoothSocket socket){
		 if (wConnectThread != null) {wConnectThread.cancel(); wConnectThread = null;}
	        if (wConnectedThread != null) {wConnectedThread.cancel(); wConnectedThread = null;}
            wConnectedThread = new ConnectedThread(socket);
	       wConnectedThread.start();
	        Log.i(TAG, "connecteddd");
		 Log.i(TAG, "connecttt");
		 TextView words = (TextView) findViewById(R.id.editText1);
	     String a = words.getText().toString();
	     Log.i(TAG, a);

		 sendMessage(a);

		 
	 }
	 private void sendMessage(String message){
		
		 	buffer = message.getBytes();
		 	
		 wConnectedThread.write(buffer);
	 }
public void click(View i){
	 TextView words = (TextView) findViewById(R.id.editText1);
     String b = words.getText().toString();
     sendMessage(b);
}}
