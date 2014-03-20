package org.shellmo.shellmobt;
/*
 * Copyright (C) 2014 Shellmo Project
 * 
 */



/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,OnSeekBarChangeListener,OnTouchListener {
    // Debugging
    private static final String TAG = "ShellmoBT";
  //  private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    SeekBar sb1,sb2;
    Button btn1,btn2,btn4,bt_connect;
    ImageView im_up,im_down,im_left,im_right,im_sp,im_blue,im_a,im_b,im_c; Vibrator vib;
    TextView comlog,blue_status;
    SharedPreferences pref;
String address = "";//RN42

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "+++ ON CREATE +++");
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Set up the window layout
        setContentView(R.layout.activity_main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sb1=(SeekBar)findViewById(R.id.seekBar1);
        sb2=(SeekBar)findViewById(R.id.seekBar2);

        im_up = (ImageView)findViewById(R.id.bt_up);
        im_down = (ImageView)findViewById(R.id.bt_down);
        im_right = (ImageView)findViewById(R.id.bt_right);
        im_left = (ImageView)findViewById(R.id.bt_left);
        im_sp =  (ImageView)findViewById(R.id.bt_stop);
        im_blue =  (ImageView)findViewById(R.id.bt_blue);
        im_a =  (ImageView)findViewById(R.id.bt_cus_A);       
        im_b =  (ImageView)findViewById(R.id.bt_cus_B);        
        im_c =  (ImageView)findViewById(R.id.bt_cus_C);        
        
        im_up.setOnTouchListener(this);
        im_down.setOnTouchListener(this);
        im_right.setOnTouchListener(this);
        im_left.setOnTouchListener(this);
        im_sp.setOnTouchListener(this);
        im_blue.setOnTouchListener(this);
        im_a.setOnTouchListener(this);
        im_b.setOnTouchListener(this);
        im_c.setOnTouchListener(this);
        
        comlog = (TextView)findViewById(R.id.comlog);
        blue_status = (TextView)findViewById(R.id.blue_status);
        
        vib= (Vibrator) getSystemService(VIBRATOR_SERVICE);
        
        pref=getSharedPreferences("pref",MODE_PRIVATE);
        
        sb1.setProgress(pref.getInt("v",99));
        sb2.setProgress(pref.getInt("a",49));
        ((TextView)findViewById(R.id.tx_speed)).setText("speed: "+Integer.toString(pref.getInt("v",99)));
        ((TextView)findViewById(R.id.tx_accel)).setText("accel: "+Integer.toString(pref.getInt("a",49)));
        
        sb1.setOnSeekBarChangeListener(this);
        sb2.setOnSeekBarChangeListener(this);
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
            address = pref.getString("address", "");
            
        }
        if(address!=""){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, true);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "++ ON START ++");
    //	String address = "00:06:66:4D:63:E8";        
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.i(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        /*
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        
        /*
        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
            
        });
        */

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.i(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        Log.i(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        Log.i(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	blue_status.setTextColor(Color.GREEN);
                	blue_status.setText(R.string.title_connected_to);
                	blue_status.append("\n"+mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                   	blue_status.setTextColor(Color.YELLOW);
                	blue_status.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                	blue_status.setTextColor(Color.RED);
                	blue_status.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
               // mConversationArrayAdapter.add("Me:  " + writeMessage);//write
               // comlog.append("Me: " + writeMessage + "\n" );
                comlog.setText("Me: " + writeMessage + "\n" + comlog.getText());
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage); //read
               // mConversationArrayAdapter.add("Shellmo: " + readMessage);
                comlog.setText("Shellmo: " + readMessage +  comlog.getText());
                //comlog.append("Shellmo: " + readMessage );
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
        //Save address
		Editor editor = pref.edit();
		  editor.putString("address", address);editor.commit();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    void sendbt(String message){
    	byte[] send = message.getBytes();
        mChatService.write(send);
        
    }
    boolean notstop = false;

	@Override
	public void onClick(View v) {		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		// TODO 自動生成されたメソッド・スタブ
		Log.d("debug","seekbar");
		if(seekBar==sb1){
			sb1.setEnabled(false);
			int val = seekBar.getProgress();
			String reverse = "";
			String[] senddata = new String[5];
			
			for(int i=0; i<5; i++){
				senddata[i]= "";//Init strings. if not, it will make error at substring(1,2)
			}
			//String senddata = "";
			//char[] chardata = new char[3];
			((TextView)findViewById(R.id.tx_speed)).setText("speed: "+Integer.toString(seekBar.getProgress()));
			senddata[0]="v";
			if(val<9){senddata[1]="0";  senddata[2] = Integer.toString(val);   }
			else {String senddata2 =  Integer.toString(val)+"00";Log.d("debug","send1b1"); senddata[1] = senddata2.substring(0, 1);Log.d("debug","send1b2");senddata[2] = senddata2.substring(1, 2); }
			
			int send_delay = 75; //Arduino will freeze when value is "50".
			if(senddata[0]=="v"&&senddata[0]!=""&&senddata[1]!=""&&senddata[2]!=""){
			sendbt(senddata[0]);
			sleep(send_delay);
			sendbt(senddata[1]);
			sleep(send_delay);
			sendbt(senddata[2]);
			sleep(send_delay);
			}
			 Editor editor = pref.edit();
			 editor.putInt("v", seekBar.getProgress());editor.commit();
			sb1.setEnabled(true);
			
		}
		else if(seekBar==sb2){
			sb2.setEnabled(false);
			int val = seekBar.getProgress();
			String reverse = "";
			String[] senddata = new String[5];
			
			for(int i=0; i<5; i++){
				senddata[i]= "";
			}
			
			//String senddata = "";
			//char[] chardata = new char[3];
			Log.d("debug","send1");
			((TextView)findViewById(R.id.tx_accel)).setText("accel: "+Integer.toString(seekBar.getProgress()));
			
			senddata[0]="b";
			if(val<9){senddata[1]="0";  senddata[2] = Integer.toString(val);   }
			else {String senddata2 =  Integer.toString(val)+"00";Log.d("debug","send1b1"); senddata[1] = senddata2.substring(0, 1);Log.d("debug","send1b2");senddata[2] = senddata2.substring(1, 2); }
			int send_delay = 75;
			if(senddata[0]=="b"&&senddata[0]!=""&&senddata[1]!=""&&senddata[2]!=""){
			sendbt(senddata[0]);
			sleep(send_delay);
			sendbt(senddata[1]);
			sleep(send_delay);
			sendbt(senddata[2]);
			sleep(send_delay);
			}
			Editor editor = pref.edit();
		  editor.putInt("a", seekBar.getProgress());editor.commit();
			sb2.setEnabled(true);
		}	
	}
	Boolean speedbt = false;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			if(v==im_right){
				//senddata("ON_RIGHT\r\n");
				sendbt("d");
				im_right.setImageResource(R.drawable.bt_right_on);
			}
			else if(v==im_left){
				sendbt("a");
				im_left.setImageResource(R.drawable.bt_left_on);
			}
			else if(v==im_down){
				sendbt("s");
				im_down.setImageResource(R.drawable.bt_down_on);
			}
			else if(v==im_up){
				sendbt("w");
				im_up.setImageResource(R.drawable.bt_up_on);
				}
			/*
			else if(v==im_sp){
				im_sp.setImageResource(R.drawable.bt_stop_on);
				if(speedbt==false){
				sendbt("v");
				sleep(75);
				sendbt("9");
				sleep(75);
				sendbt("9");
				sleep(75);
				speedbt=true;
				sb1.setProgress(99);
				((TextView)findViewById(R.id.textView1)).setText("99");
				}
				else if(speedbt==true){
					sendbt("v");
					sleep(75);
					sendbt("5");
					sleep(75);
					sendbt("0");
					sleep(75);
					speedbt=false;
					sb1.setProgress(50);
					((TextView)findViewById(R.id.textView1)).setText("50");
					}			
				}
				*/
			//Bluetooth button
			else if(v==im_blue){
				im_blue.setImageResource(R.drawable.bt_bluetooth_on);
				address = pref.getString("address", "");

				//connect last connected address
                if(address!=""){
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mChatService.connect(device, true);
                }
                //if first connect, search devices
                else{
                    // Launch the DeviceListActivity to see devices and do scan
                	 Intent serverIntent = null;
                    serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);}
			}
			else if(v==im_a){im_a.setImageResource(R.drawable.bt_cus_on2);sendbt("h");}
			else if(v==im_b){im_b.setImageResource(R.drawable.bt_cus_on2);sendbt("j");}
			else if(v==im_c){im_c.setImageResource(R.drawable.bt_cus_on2);sendbt("l");}

			vib.vibrate(30);
			
			
		}
		else if(event.getAction()==MotionEvent.ACTION_UP){
			if(v==im_right){
				im_right.setImageResource(R.drawable.bt_right_off);sendbt("z");notstop=false;
			}
			else if(v==im_left){
				im_left.setImageResource(R.drawable.bt_left_off);sendbt("z");notstop=false;
			}
			else if(v==im_up){
				im_up.setImageResource(R.drawable.bt_up_off);sendbt("z");notstop=false;
			}
			else if(v==im_down){
				im_down.setImageResource(R.drawable.bt_down_off);sendbt("z");notstop=false;
			}
			else if(v==im_sp){
				im_sp.setImageResource(R.drawable.bt_stop_off);
			}
			if(v==im_blue){
				im_blue.setImageResource(R.drawable.bt_bluetooth_off);
			}
			else if(v==im_a){im_a.setImageResource(R.drawable.bt_cus_off);sendbt("i");sleep(50);sendbt("i");}
			else if(v==im_b){im_b.setImageResource(R.drawable.bt_cus_off);sendbt("k");sleep(50);sendbt("k");}
			else if(v==im_c){im_c.setImageResource(R.drawable.bt_cus_off);sendbt("m");sleep(50);sendbt("m");}

			
		}
		sleep(50);
		return true;
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	  public synchronized void sleep(long msec)
	    {	
	    	try
	    	{
	    	wait(msec);
	    	}catch(InterruptedException e){}
	    }


	  

}
