/**
 * 
 */
package com.ds.tire.bluetooth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ds.tire.MonitorActivity;
import com.ds.tire.R;
import com.ds.tire.util.SpUtils;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**  
* 类功能描述
* @author 李小敏
* @version 2013-12-20 下午04:02:18 
*/
public class GetDataService extends Service {

	private int m=0;
	private int count=0;
	private static String TAG = "GetDataService";
    private float y;
    private final float k=0.9667f;
    private final float b=105.17f;
	public static BluetoothDevice mDevice;
	private BluetoothSocket mmSocket;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;

	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	protected BroadcastReceiver controlReceiver;

	/******************************Service自有函数**********************************************/
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		registerBroadcastReceiver();
		Log.d(TAG, "GetDataService 注册广播成功");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart");
		setTalk();
		
	}
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		//count=0;
		resetTalk();
		unregisterReceiver(controlReceiver);

	}
	/******************************Service自有函数**********************************************/

	/**
	 * 开启服务
	 */
	public static void start(Context context, BluetoothDevice device) {
		mDevice = device;
		context.startService(new Intent(Constants.ACTION_BLUETOOTH_SERVICE));
	}

	/**
	 * 注册广播信息
	 */
	private void registerBroadcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_BLUETOOTH_RECEIVEDE_BROADCAST_CONNECT);
		intentFilter.addAction(Constants.ACTION_BLUETOOTH_RECEIVEDE_BROADCAST_STOP);
		intentFilter.addAction(Constants.ACTION_BLUETOOTH_RECEIVEDE_BROADCAST_TALK);
		intentFilter.addAction(Constants.REFRESH);
		controlReceiver = new ControlBroadcastReceiver();
		registerReceiver(controlReceiver, intentFilter);
	}

	//发送广播
	private void mSendBroadcast(int id) {
		Log.i(TAG, "mSendBroadcast");
		Intent intent_broad = new Intent();
		intent_broad.putExtra(Constants.MSG, id);
		intent_broad.setAction(Constants.ACTION_BLUETOOTH_BROADCAST);
		getApplicationContext().sendBroadcast(intent_broad);
	}

	private void mSendBroadcast(String mID ,float yaqiang, float wendu, float jiasudu) {
		Intent intent_broad = new Intent();
		intent_broad.putExtra(Constants.JIASUDU, jiasudu);
		intent_broad.putExtra(Constants.YAQIANG, yaqiang);
		intent_broad.putExtra(Constants.WENDU, wendu);
		intent_broad.putExtra(Constants.mID, mID);
		intent_broad.setAction(Constants.ACTION_BLUETOOTH_BROADCAST);
		getApplicationContext().sendBroadcast(intent_broad);
	}

	/**
	 * 建立回话
	 */
	public void setTalk() {
		resetTalk();
		connectThread = new ConnectThread();
		connectThread.start();
		

	}

	/**
	 * 开始回话
	 */
	private void startTalk() {
		connectedThread = new ConnectedThread();
		connectedThread.start();
		Log.d(TAG, "33create ConnectedThread: " );
	}

	/**
	 * 重置回话
	 */
	private synchronized void resetTalk() {
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		if (connectedThread != null) {			
			connectedThread.cancel();
			connectedThread = null;
		}
	}
	
	

	/**
	 * 建立连接的线程
	 * @author 李小敏
	 *
	 */

	private class ConnectThread extends Thread {
		

		public ConnectThread() {
			mSendBroadcast(Constants.STATE_CONNECTING);
			BluetoothSocket tmp = null;
			try {
				tmp = mDevice.createInsecureRfcommSocketToServiceRecord(Constants.MY_UUID_INSECURE);

			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
		
			setName("ConnectThread" + "Insecure");
			mBluetoothAdapter.cancelDiscovery();
			try {
				mmSocket.connect();
			} catch (IOException e) {
				try {
					mmSocket.connect();
				} catch (IOException ex) {
					try {
						mmSocket.close();
					} catch (IOException e2) {
						Log.e(TAG, "unable to close() " + "Insecure" + " socket during connection failure", e2);
					}
					Log.e(TAG, "ConnectThread socket failed", e);
					mSendBroadcast(Constants.MESSAGE_CONNECT_FAILED);
					return;
				}
			}
			synchronized (GetDataService.this) {
				connectThread = null;
			}
			mSendBroadcast(Constants.STATE_CONNECTED);
		
		}
		public void cancel() {
			try {
				Log.e(TAG, "mConnectedThread cancel");
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + "Insecure" + " socket failed", e);
			}
		}
		
	}
	
	/**
	 * 保持会话的线程
	 * @author 李小敏
	 *
	 */
	private class ConnectedThread extends Thread {
		
//		private final InputStream mmInStream;
//		private final OutputStream mmOutStream;
		private InputStream mmInStream;
		private OutputStream mmOutStream;
		private ArrayList<Integer> buffer = null;

		private int mDateChange(int date) {
			int finalDate;
			if (date > 0x7fff) {
				int fanma = date - 1;
				int yuanma = fanma ^ 0xffff;
				finalDate = 0-yuanma;
			}else
				finalDate= date;

			return finalDate;
		}
		public ConnectedThread() {
			Log.d(TAG, "create ConnectedThread: " + "Insecure");
//			InputStream tmpIn = null;
//			OutputStream tmpOut = null;
			this.buffer = new ArrayList<Integer>(100);

			try {
//				tmpIn = mmSocket.getInputStream();
//				tmpOut = mmSocket.getOutputStream();
				mmInStream= mmSocket.getInputStream();
				mmOutStream = mmSocket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

//			mmInStream = tmpIn;
//			mmOutStream = tmpOut;
		}

		public void run() {
		  
			Log.i(TAG, "read data start.....");
			byte c = 0;
			int a = 0;
			int num1=0;
			int num2=0;
			int index=-1;
			while (true) {
				Log.i(TAG,"data start!!!！");
				try {
					//完整数据格式3c3c3c    帧头 （4） ID号（4）胎压（2）胎温（2）加速度（2） 电压（2）        3e3e3e
					buffer.clear();
				 while (num2!= 3) {
				//	while (buffer.size()!=98) {					
					  c = (byte) mmInStream.read();
					  if(c+""!=null){
								
						//判断是否为<<<开始，<对应的码值0x3c
						if (Integer.toHexString(c).toString().equals("3c")) {
							num1++;						
						}
						if(num1>=3){
							int i=(int)(c);
							if(i<0){
								//Log.d(TAG,"c是负数_>"+i);
								a=((i+256)%256);
								buffer.add(a);
								//Log.d("取正数后的a:",a+"");
								//Log.d("转换后的a:",Integer.toHexString(a));
									
							}else{
								buffer.add(i);															
							}				
						}
						//判断是否为>>>结尾，>对应的码值0x3e
						if (Integer.toHexString(c).toString().equals("3e")) {
							num2++;
							if (num2 == 1) {
							  index = buffer.size();
							} else {
								if ((index + num2 - 1) != buffer.size()) {
									index = -1;
									num2 = 0;
									num1=0;
									}
								}
							}
							//Log.i("转换前c:","测试c->"+c);
							//Log.i("转换后c:",Integer.toHexString(c));
//							int i=(int)(c);
//							if(i<0){
//								//Log.d(TAG,"c是负数_>"+i);
//								a=((i+256)%256);
//								buffer.add(a);
//								//Log.d("取正数后的a:",a+"");
//								//Log.d("转换后的a:",Integer.toHexString(a));
//									
//							}else{
//								buffer.add(i);															
//							}																										
						   // Log.i(TAG, "buffer 缓存区大小 size1111:"+buffer.size());						   
					 }else{
						 Log.i(TAG,"c为空！！！");
						 m++;
							Log.i("测试：","缓存区大小："+buffer.size()+"测试m:"+m);
							num1=0;
							num2=0;
							index=-1;
							buffer.clear();
//							Intent intent2 = new Intent();  //Itent就是我们要发送的内容 
//				            intent2.setAction(Constants.REFRESHOK);   //设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
//				            sendBroadcast(intent2);   //发送广播
					 }
					  
					}
					Log.d("buffer数据：",buffer.toString());
					Log.i(TAG, "buffer 缓存区大小 size:"+buffer.size());				
					if(buffer.size() == 36){
						if(Integer.toHexString(buffer.get(0)).toString().equals("3c")&&Integer.toHexString(buffer.get(33)).toString().equals("3e")&&Integer.toHexString(buffer.get(34)).toString().equals("3e")
								&&Integer.toHexString(buffer.get(35)).toString().equals("3e")){
							//数据校验
							if ((buffer.get(5) ^ buffer.get(6) ^ buffer.get(7) ^ buffer.get(8)) == buffer.get(21)&&(buffer.get(9) ^ buffer.get(10) ^ buffer.get(11) ^ buffer.get(12)) == buffer.get(22)
									&&(buffer.get(13) ^ buffer.get(14) ^ buffer.get(15) ^ buffer.get(16)) == buffer.get(23)&&(buffer.get(17) ^ buffer.get(18) ^ buffer.get(19) ^ buffer.get(20)) == buffer.get(24)
									&&(buffer.get(5) ^ buffer.get(9) ^ buffer.get(13) ^ buffer.get(17)) == buffer.get(25)&&(buffer.get(6) ^ buffer.get(10) ^ buffer.get(14) ^ buffer.get(18)) == buffer.get(26)
									&&(buffer.get(7) ^ buffer.get(11) ^ buffer.get(15) ^ buffer.get(19)) == buffer.get(27)&&(buffer.get(8) ^ buffer.get(12) ^ buffer.get(16) ^ buffer.get(20)) == buffer.get(28)) {
								Constants.Tag=0;
								String mID = Integer.toHexString(buffer.get(5))+Integer.toHexString(buffer.get(6))+Integer.toHexString(buffer.get(7))+Integer.toHexString(buffer.get(8));
								float yaqiang = (float) (mDateChange(buffer.get(9) * 256 + buffer.get(10))*1.0/16.0);
								float wendu = (float) (mDateChange(buffer.get(11) * 256 + buffer.get(12))*1.0/128.0);
								float jiasudu = (float) (mDateChange(buffer.get(13) * 256 + buffer.get(14))*1.0/16.0);
								//float jiasudu = (float) (java.lang.Math.abs((mDateChange(buffer.get(14) * 256 + buffer.get(15)))/8));
								
								y=java.lang.Math.abs((yaqiang-b)/k);							
								count++;
								Log.e(TAG,"获取数据的条数："+count);
								mSendBroadcast(mID, y, wendu, jiasudu);
							}
						
						}else{
							Log.e(TAG,"buffer.size!!!!!=======36");
							num1=0;
							num2=0;
							index=-1;
							buffer.clear();
							
//							m++;
//							Log.i("测试m：","缓存区大小："+buffer.size()+"测试m:"+m);
//							Intent intent2 = new Intent();  //Itent就是我们要发送的内容 
//				            intent2.setAction(Constants.REFRESHOK);   //设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
//				            sendBroadcast(intent2);   //发送广播
						}
					}
					num1=0;
					num2=0;
					index=-1;
					buffer.clear();	
					
				  
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					mSendBroadcast(Constants.MESSAGE_CONNECT_LOST);
					resetTalk();
					break;
				}
			}
		  
		}
	
		
		public void cancel() {
			try {
				Log.e(TAG, "mConnectedThread cancel");
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * 广播接收器，接受传送过来的关于蓝牙的指令，选择相应的操作
	 * @author 李小敏
	 *
	 */
	private class ControlBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.ACTION_BLUETOOTH_RECEIVEDE_BROADCAST_CONNECT)) {
				setTalk();
			}else if (intent.getAction().equals(Constants.ACTION_BLUETOOTH_RECEIVEDE_BROADCAST_TALK)) {
				startTalk();
			}
		}

	}	
}
