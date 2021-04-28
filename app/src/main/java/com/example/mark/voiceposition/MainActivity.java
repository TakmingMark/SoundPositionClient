package com.example.mark.voiceposition;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mark.Protocol.HandlerProtocol;
import com.example.mark.Protocol.SocketProtocol;
import com.example.mark.SocketClient.SocketThread;
import com.example.mark.SocketClient.UserSet;
import com.example.mark.UploadFileToServer.UploadFileToServer;
import com.example.mark.XclChart.DemoView;
import com.example.mark.XclChart.ScatterChart01View;
import com.example.soundposition.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity
{
	private final String TAG="VPMainActivity";
	private RxPermissions rxPermissions=null;

	/**
	 * layout component
	 */
	private View mainLayout;
	private ScrollView scrollView;
	private LinearLayout llChartView;
	private Button btnRecord, btnTrack, btnControlVoice, btnCleanEditText,btnPositionStatus,btnOptionPositionMode;
	private Spinner spOption,spUser;
	private EditText etNodeX,etNodeY,etContent,etActualX,etActualY;
	private TextView tvInformation,tvTargetNode,tvTargetNodeTitle,tvExplain,tvDistance,tvDistanceTitle;

	/**
	 * sound position component
	 */
	private AudioManager audioManager;
	private SoundRecord soundRecord;
	private SoundTrack soundTrack;
	private FileSaveFinishHandler fileSaveFinishHandler;
	private static boolean isRecording = false;
	private int amSystemSound=0,amMusicSound=0,amVoiceCallSound=0,amRingSound=0;
	private boolean singlePosition=false;
	/**
	 * use sound file make noise
	 */
	private SoundPool soundPool;
	private int chirpFile;

	//some devices are not supported,so we are use soundPool
	private RecordChirp recordChirp;

	/**
	 * socket component
	 */
	private SocketThread socketThread;
	private SocketHandler socketHandler;
	private static UserSet<String> usClients;
//	private String serverIP="10.105.1.13";
//	private String fileServerIP="10.105.1.13";
	private String serverIP="120.127.14.91";
	private String fileServerIP="120.127.14.91";
	private int serverPort=2525;
	private String localIP="";
	private String roomID="";
	private String soundRank;
//	role of device
	private int role=0;
	private int nodeX=0,nodeY=0;
//	control whether to continue positioning
	private boolean controlCenter=false;

	private boolean controlAssist=false;

	/**
	 * Timer component
	 */
	private static int countingTime;
	private static long timerStart, timerEnd;
	private static long currTimerStart, currTimerEnd;
	private static long pastedTime;

	/**
	 * Upload File to server component
	 */
	private UploadFileToServer uploadFileToServer;
	private UploadFileHandler uploadFileHandler;

	/**
	 * touch component
	 */
	private OnTouchListener onTouchListener;

	/**
	 * repeat sound position
	 */
	RepeatEmittedSound repeatEmittedSound;
	ScatterChart01View scatter;

	/**
	 * positioning scale
	 */
	private int positionX=100,positionY=100;
	private double scaleX,scaleY;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		grantPermissions();
		// initialize UI
		initUI();
		// initialize Object
		initObject();
		// initialize Button;
		initButton();
		// initialize initAudioTrack
//		soundTrack.initAudioTrackForSin();
		soundTrack.initAudioTrackForChirp();
//		initialize chart view
		initChartView();
		// initialize initIPAdrress
		initIPAdrress();

		initRecordChirp();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		socketThread.start();
	}

	private void grantPermissions() {
		rxPermissions=new RxPermissions(this);
		rxPermissions
				.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
				.subscribe(new Consumer<Boolean>() {
					@Override
					public void accept(Boolean aBoolean) throws Exception {

					}
				});

	}

	private void initUI()
	{
		mainLayout=(View)findViewById(R.id.mainLayout);
		scrollView=(ScrollView)findViewById(R.id.scrollView);
		llChartView=(LinearLayout)findViewById(R.id.llChartView);
		btnRecord = (Button) findViewById(R.id.btnRecord);
		btnTrack = (Button) findViewById(R.id.btnTrack);
		btnControlVoice = (Button) findViewById(R.id.btnControlVoice);
		btnCleanEditText= (Button) findViewById(R.id.btnCleanEditText);
		btnPositionStatus=(Button) findViewById(R.id.btnPositionStatus);
		btnOptionPositionMode=(Button)findViewById(R.id.btnOptionPositionMode);
		etNodeX=(EditText) findViewById(R.id.etNodeX);
		etNodeY=(EditText) findViewById(R.id.etNodeY);
		etContent=(EditText)findViewById(R.id.etContent);
		etActualX=(EditText)findViewById(R.id.etActualX);
		etActualY=(EditText)findViewById(R.id.etActualY);
		spOption=(Spinner) findViewById(R.id.spOption);
		spUser=(Spinner)findViewById(R.id.spUser);
		tvInformation=(TextView)findViewById(R.id.tvInformation);
		tvTargetNode=(TextView)findViewById(R.id.tvTargetNode);
		tvTargetNodeTitle=(TextView)findViewById(R.id.tvTargetNodeTitle);
		tvExplain=(TextView)findViewById(R.id.tvExplain);
		tvDistance=(TextView)findViewById(R.id.tvDistance);
		tvDistanceTitle=(TextView)findViewById(R.id.tvDistanceTitle);
	}

	private void initObject()
	{
		soundTrack = new SoundTrack();
		fileSaveFinishHandler=new FileSaveFinishHandler();
		soundRecord = new SoundRecord(fileSaveFinishHandler);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		soundPool=new SoundPool(1, AudioManager.STREAM_MUSIC,0);
		chirpFile=soundPool.load(this,R.raw.chirp081250,1);

		socketHandler=new SocketHandler();
		socketThread=new SocketThread(socketHandler,serverIP,serverPort);
		usClients=new UserSet<>();

		uploadFileHandler=new UploadFileHandler();
		uploadFileToServer=new UploadFileToServer(uploadFileHandler,fileServerIP);

		onTouchListener=new OnTouchListener();

		scatter=new ScatterChart01View(this);
	}

	private void initButton()
	{
		btnTrack.setOnClickListener(new ClickTrack());
		btnRecord.setOnClickListener(new ClickRecord());
		btnControlVoice.setOnClickListener(new ClickControlVoice());
		btnCleanEditText.setOnClickListener(new ClicketCleanEditText());
		btnPositionStatus.setOnClickListener(new ClickPositionStatus());
		btnOptionPositionMode.setOnClickListener(new ClickOptionPositionMode());
		spOption.setOnItemSelectedListener(new OnSpOptionItemSelectedListener());
		etContent.setOnTouchListener(onTouchListener);
		mainLayout.setOnTouchListener(onTouchListener);
		scrollView.setOnTouchListener(onTouchListener);
	}
	private void initChartView()
	{

		DemoView mCharts =scatter;
		FrameLayout content = new FrameLayout(this);

		//缩放控件放置在FrameLayout的上层，用于放大缩小图表
		FrameLayout.LayoutParams frameParm = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		frameParm.gravity = Gravity.BOTTOM| Gravity.RIGHT;

		//图表显示范围在占屏幕大小的90%的区域内
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int scrWidth = (int) (dm.widthPixels * 0.9);
		int scrHeight = (int) (dm.heightPixels * 0.9);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				scrWidth, scrHeight);//居中显示
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		//图表view放入布局中，也可直接将图表view放入Activity对应的xml文件中
		final RelativeLayout chartLayout = new RelativeLayout(this);

		chartLayout.addView(mCharts, layoutParams);

		//增加控件
		((ViewGroup) content).addView(chartLayout);
		//((ViewGroup) content).addView(mZoomControls);
		llChartView.addView(content);


	}
	/**
	 * initial self connect network IP of device
	 */
	private void initIPAdrress()
	{
		WifiManager wifi_service = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
		//取得wifi資訊
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();
		//取得IP，但這會是一個詭異的數字，還要再自己換算才行
		int ipAddress = wifiInfo.getIpAddress();
		//利用位移運算和AND運算計算IP
		String ip = String.format("%d.%d.%d.%d",(ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
		localIP=ip;
		tvInformation.setText("IP:" + localIP + " roomID:0" + " soundRank:0");
	}

	private void initRecordChirp()
	{
//		recordChirp=new RecordChirp();
	}

	private void calUsedTime(String action)
	{
		if(action=="start" || action.equals("start"))
		{
			currTimerStart = System.currentTimeMillis();
			timerStart = System.nanoTime();
		}
		else if(action=="end" || action.equals("end"))
		{
			currTimerEnd = System.currentTimeMillis();
			timerEnd = System.nanoTime();
		}
		else if(action=="cal" || action.equals("cal"))
		{
			Log.e(TAG,"pasted="+ Long.toString(timerEnd - timerStart));
			Log.e(TAG,"pasted="+ Long.toString(currTimerEnd - currTimerStart));
			pastedTime=currTimerEnd - currTimerStart;
		}
	}

	/**
	 * SocketThread class communicate interface,the message across to the MainActivity
	 */
	class SocketHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			String type= String.valueOf(msg.what);

			Bundle bundle=msg.getData();
			String str=bundle.getString("message");
			Log.e(TAG,"SocketHandler str="+str);
			if(type==HandlerProtocol.etContent_UPDATE || type.equals(HandlerProtocol.etContent_UPDATE))
			{
				str=str+"\r\n";
				etContent.append(str);
			}
			else if(type==HandlerProtocol.spUser_INSERT || type.equals(HandlerProtocol.spUser_INSERT))
			{
				splitUserIP(str);
				updateCBuser("null", "add");
			}
			else if(type==HandlerProtocol.spUser_DELETE || type.equals(HandlerProtocol.spUser_DELETE))
			{
				updateCBuser(str, "remove");
			}
			else if(type==HandlerProtocol.selfIP_UPDATE||type.equals(HandlerProtocol.selfIP_UPDATE))
			{
				tvInformation.setText("IP:" + str + " roomID:0"  + " soundRank:0");
				localIP=str;
			}
			else if(type==HandlerProtocol.give_INFORMATION || type.equals(HandlerProtocol.give_INFORMATION))
			{
				String content=str.split("\\"+ SocketProtocol.SPLIT_SIGN)[0];
				roomID=str.split("\\"+ SocketProtocol.SPLIT_SIGN)[1];
				soundRank=str.split("\\"+ SocketProtocol.SPLIT_SIGN)[2];
				etContent.append(content);
				etContent.append(getMachineTime());
				soundRecord.setNewAudioRank(soundRank);
				uploadFileToServer.setFileRank(soundRank);
				tvInformation.setText("IP:" + localIP + " roomID:" + roomID + " soundRank:" + soundRank);
				startAudioRecord();
			}
			else if(type==HandlerProtocol.draw_Node || type.equals(HandlerProtocol.draw_Node))
			{
				int nodePosition= Integer.valueOf(str.split("\\"+ SocketProtocol.SPLIT_SIGN)[0]);
				double nodeX= Double.valueOf(str.split("\\"+ SocketProtocol.SPLIT_SIGN)[1]);
				double nodeY= Double.valueOf(str.split("\\"+ SocketProtocol.SPLIT_SIGN)[2]);

				if(nodePosition!=0)
				{
					updateXclChart(nodePosition, nodeX, nodeY);
				}
			}
			else if(type==HandlerProtocol.Start_SoundTrack || type.equals(HandlerProtocol.Start_SoundTrack))
			{
				btnControlVoice.callOnClick();
				btnTrack.callOnClick();
			}
			else if(type==HandlerProtocol.Stop_Rerord_Sound || type.equals(HandlerProtocol.Stop_Rerord_Sound))
			{
				soundRecord.stopAudioRecord();
				btnControlVoice.callOnClick();
			}
			else if(type==HandlerProtocol.success_Calculate_Coordinate || type.equals(HandlerProtocol.success_Calculate_Coordinate))
			{
				double estimateX= Double.valueOf(str.split("\\"+ SocketProtocol.SPLIT_SIGN)[0]);
				double estimateY= Double.valueOf(str.split("\\"+ SocketProtocol.SPLIT_SIGN)[1]);
				updateXclChart(0, estimateX, estimateY);

				double actualX= Double.valueOf(etActualX.getText().toString());
				double actualY= Double.valueOf(etActualY.getText().toString());
				insertTargetNodeDB(localIP, roomID, actualX, actualY, estimateX, estimateY);

				if(role==0)
				{
					repeatEmittedSound = new RepeatEmittedSound(2000, 2000);
					repeatEmittedSound.start();
				}
			}
			else if(type==HandlerProtocol.success_Calculate_Distance || type.equals(HandlerProtocol.success_Calculate_Distance))
			{
				//如果要連續測量距離的話，加上|| role==1 || role==2 || role==3|| role==4
				if( role==0|| role==1 || role==2 || role==3|| role==4)
				{
					repeatEmittedSound = new RepeatEmittedSound(2000, 2000);
					repeatEmittedSound.start();
				}
				else if(role==5)
				{
					String[] distanceArray=str.split("\\|");

					for(String distance:distanceArray)
					{
						if(distance!="0" && !distance.equals("0"))
						{
							tvDistance.setText(distance+"cm");
							break;
						}
					}

				}
			}
		}

		/**
		 * split Users IP put the usClients
		 * @param string =Users IP
		 */
		private void splitUserIP(String string)
		{
			String[] splitString=string.split("\\" + SocketProtocol.SPLIT_SIGN);
			for(int i=0;i<splitString.length;i++)
				usClients.put(splitString[i]); //Set不會新增有重複的數值
		}

		/**
		 * Update CBuser layout content
		 * @param User= action user
		 * @param action= action need to be done
		 */
		private void updateCBuser(String User , String action)
		{
			if(action=="add")
			{
				ArrayList<String> userList=new ArrayList<String>();
				for (String user : usClients.valueSet())
					userList.add(user);
				ArrayAdapter<String> adapter;
				adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,userList);
				spUser.setAdapter(adapter);
			}
			else if(action=="remove")
			{
				usClients.remove(User);
				ArrayList<String> userList=new ArrayList<String>();
				for (String user : usClients.valueSet())
					userList.add(user);
				ArrayAdapter<String> adapter;
				adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,userList);
				spUser.setAdapter(adapter);
			}
		}

		/**
		 * get device machine time
		 * @return=hours+minutes+seconds ,each device is different of time
		 */
		private String getMachineTime()
		{
			Calendar calender= Calendar.getInstance();
			int seconds,minutes,hours;

			seconds=calender.get(calender.SECOND);
			minutes=calender.get(calender.MINUTE);
			hours=calender.get(calender.HOUR_OF_DAY);

			return hours+":"+minutes+":"+seconds+"\r\n";
		}

		/**
		 * handle message to server
		 */
		private void startAudioRecord()
		{
			soundRecord.startAudioRecord();
			String message="";
			message= SocketProtocol.C_USER_START_SOUND_RECORD+roomID+
					SocketProtocol.SPLIT_SIGN+localIP+
					SocketProtocol.SPLIT_SIGN+role+
					SocketProtocol.SPLIT_SIGN+nodeX+
					SocketProtocol.SPLIT_SIGN+nodeY+
					SocketProtocol.C_USER_START_SOUND_RECORD;
			socketThread.sendServer(message);
		}


	}

	private void updateXclChart(int nodePosition,double nodeX,double nodeY)
	{
		tvTargetNode.setText("("+(int)nodeX+","+(int)nodeY+")");
		nodeX=(int)(nodeX*scaleX);
		nodeY=(int)(nodeY*scaleY);
		scatter.insertDataSeries(nodePosition,nodeX,nodeY);
		scatter.invalidate();
	}

	private void insertTargetNodeDB(String parimaryIP, String room, double actualX, double actualY, double estimateX, double estimateY)
	{
		double deviation= Math.sqrt((actualX - estimateX)*(actualX-estimateX)+(actualY-estimateY)*(actualY-estimateY));
		String message="";
		message= SocketProtocol.C_INSERT_TARGET_NODE_DB+parimaryIP+
				SocketProtocol.SPLIT_SIGN+room+
				SocketProtocol.SPLIT_SIGN+actualX+
				SocketProtocol.SPLIT_SIGN+actualY+
				SocketProtocol.SPLIT_SIGN+estimateX+
				SocketProtocol.SPLIT_SIGN+estimateY+
				SocketProtocol.SPLIT_SIGN+deviation+
				SocketProtocol.C_INSERT_TARGET_NODE_DB;
		socketThread.sendServer(message);
	}
	/**
	 * 	when sound file from raw to wav need buffer time in soundRecord.java
 	 */
	class FileSaveFinishHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			int finishMsg=(msg.what);
			if(finishMsg==1)
			{
				//File upload completed ,the Thread is end;
				new Thread(uploadFileToServer).start();
			}
		}
	}

	/**
	 * handle response from UploadFileToServer
	 * then tell sever upload file has been complete
	 */
	class UploadFileHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			String serverResponseCode= String.valueOf(msg.what);
			Log.e(TAG,"serverResponseCode="+serverResponseCode);
			// if serverResponseCode==200, return message to socketServer,tell him I upload a file successfully
			if(serverResponseCode=="200"|| serverResponseCode.equals("200"))
			{
					String message="";
					message= SocketProtocol.C_FILE_UPLOAD_COMPLETED+roomID+
							SocketProtocol.SPLIT_SIGN+localIP+
							SocketProtocol.C_FILE_UPLOAD_COMPLETED;
					socketThread.sendServer(message);
			}
			else
			{
				etContent.append(serverResponseCode + "\r\n");
			}
		}
	}



	private class RepeatEmittedSound extends CountDownTimer
	{
		public RepeatEmittedSound(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished)
		{

		}

		@Override
		public void onFinish()
		{
			if(controlCenter==true)
			{
				if(singlePosition!=true)
				{
					String message="";
					message= SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT+localIP+":C_FIND_ASSISTANT_AND_TRANSMIT"+ SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT;
					socketThread.sendServer(message);
				}
			}
		}
	}

	/**
	 * ClickTrack action
	 */
	private class ClickTrack implements View.OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			soundPool.play(chirpFile, 1.0F, 1.0F, 0, 0, 1.0F);

			String message= SocketProtocol.C_USER_END_SOUND_TRACK+roomID+ SocketProtocol.C_USER_END_SOUND_TRACK;
			socketThread.sendServer(message);
		}
	}

	/**
	 * ClickRecord action
	 */
	private class ClickRecord implements View.OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			//brnRecord click event
			if (v == btnRecord)
			{
				if (isRecording)
				{
					btnRecord.setText("開始錄音");
					soundRecord.stopAudioRecord();
					isRecording=false;
				} else
				{
					btnRecord.setText("正在錄音...");
					soundRecord.startAudioRecord();
					isRecording=true;
				}
			}
		}
	}

	/**
	 * ClickHVoice , when click him can adjust device Sound
	 */
	private class ClickControlVoice implements View.OnClickListener
	{
		//false is low voice,true is high voice
		private boolean controlVoice=true;
		@Override
		public void onClick(View v)
		{
			audioManager.setMode(AudioManager.MODE_NORMAL);
			if(controlVoice==true)
			{
//		    	record original sound state
				amRingSound=audioManager.getStreamVolume(AudioManager.STREAM_RING);
				amSystemSound=audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				amMusicSound=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				amVoiceCallSound=audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
	//			漸進式調整聲音
	//			audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_RAISE, audioManager.FLAG_SHOW_UI);
	//			直接設置調整聲音
				audioManager.setStreamVolume(AudioManager.STREAM_RING,audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),0);
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),0);
				controlVoice=false;
				btnControlVoice.setText("轉小聲");
			}
			else if(controlVoice==false)
			{
//				audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_LOWER, audioManager.FLAG_SHOW_UI);
				audioManager.setStreamVolume(AudioManager.STREAM_RING,amRingSound,0);
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, amSystemSound, 0);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, amMusicSound, 0);
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,amVoiceCallSound,0);
				controlVoice=true;
				btnControlVoice.setText("轉大聲");

			}

		}
	}

	private class ClicketCleanEditText implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			etContent.setText("");
		}
	}
	private class ClickPositionStatus implements View.OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			if(role==0 && controlCenter==false)
			{
				if(etNodeX.getText()!=null)
					nodeX= Integer.valueOf(etNodeX.getText().toString());
				if(etNodeY.getText()!=null)
					nodeY= Integer.valueOf(etNodeY.getText().toString());

				scaleX=100.0/ Double.valueOf(nodeX);
				scaleY=100.0/ Double.valueOf(nodeY);

				String message="";
				message= SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT+localIP+":C_FIND_ASSISTANT_AND_TRANSMIT"+ SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT;
				socketThread.sendServer(message);
				controlCenter=true;
				btnPositionStatus.setText("停止定位");
			}
			else if(role==0 && controlCenter==true)
			{
				controlCenter=false;
				btnPositionStatus.setText("開始定位");
			}
			else if((role==1 ||role==2||role==3 ||role==4) && controlCenter==false && controlAssist==false)
			{
				etNodeX.setEnabled(false);
				etNodeY.setEnabled(false);
				spOption.setEnabled(false);
				btnPositionStatus.setText("輔助中");
				controlAssist=true;
				nodeX= Integer.valueOf(etNodeX.getText().toString());
				nodeY= Integer.valueOf(etNodeY.getText().toString());
			}
			else if((role==1 ||role==2||role==3 ||role==4) && controlCenter==false  && controlAssist==true)
			{
				etNodeX.setEnabled(true);
				etNodeY.setEnabled(true);
				spOption.setEnabled(true);
				btnPositionStatus.setText("建立連結");
				controlAssist=false;
			}
			else if(role==5)
			{
				if(!etNodeX.getText().toString().equals("") && etNodeX.getText()!=null)
					nodeX= Integer.valueOf(etNodeX.getText().toString());
				else
					nodeX=1;

				if(!etNodeY.getText().toString().equals("") && etNodeY.getText()!=null)
					nodeY= Integer.valueOf(etNodeY.getText().toString());
				else
					nodeY=1;

				scaleX=100.0/ Double.valueOf(nodeX);
				scaleY=100.0/ Double.valueOf(nodeY);

				String message="";
				message= SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT+localIP+":C_FIND_ASSISTANT_AND_TRANSMIT"+ SocketProtocol.C_FIND_ASSISTANT_AND_TRANSMIT;
				socketThread.sendServer(message);
			}
		}
	}

	private class ClickOptionPositionMode implements View.OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			if(singlePosition==false)
			{
				singlePosition=true;
				btnOptionPositionMode.setText("連續定位");
			}
			else if(singlePosition==true)
			{
				singlePosition=false;
				btnOptionPositionMode.setText("單次定位");
			}
		}
	}

	private class OnSpOptionItemSelectedListener implements AdapterView.OnItemSelectedListener
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			if(position==0)
			{
				role=position;
				etNodeX.setEnabled(true);
				etNodeY.setEnabled(true);

				etActualX.setEnabled(true);
				etActualY.setEnabled(true);

				tvTargetNode.setVisibility(View.VISIBLE);
				tvTargetNodeTitle.setVisibility(View.VISIBLE);
				tvDistance.setVisibility(View.GONE);
				tvDistanceTitle.setVisibility(View.GONE);
				tvExplain.setText("請輸入定位範圍值");
				btnPositionStatus.setText("開始定位");
			}
			else if(position==1 ||position==2 || position==3 ||position==4)
			{
				role=position;
				etNodeX.setEnabled(true);
				etNodeY.setEnabled(true);

				etActualX.setEnabled(false);
				etActualY.setEnabled(false);

				tvTargetNode.setVisibility(View.VISIBLE);
				tvTargetNodeTitle.setVisibility(View.VISIBLE);
				tvDistance.setVisibility(View.GONE);
				tvDistanceTitle.setVisibility(View.GONE);

				tvExplain.setText("請輸入節點座標");
				btnPositionStatus.setText("建立連結");
			}
			else if(position==5)
			{
				role=position;
				etNodeX.setEnabled(false);
				etNodeY.setEnabled(false);

				etActualX.setEnabled(false);
				etActualY.setEnabled(false);

				tvTargetNode.setVisibility(View.GONE);
				tvTargetNodeTitle.setVisibility(View.GONE);
				tvDistance.setVisibility(View.VISIBLE);
				tvDistanceTitle.setVisibility(View.VISIBLE);

				btnPositionStatus.setText("計算距離");
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{

		}
	}

	private class OnTouchListener implements View.OnTouchListener
	{
		boolean etControl=false;
		int etContentX,etContentY;
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(v==etContent)
			{
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					etContentX=(int)event.getX();
					etContentY=(int)event.getY();
					break;
					case MotionEvent.ACTION_MOVE:
						int offsetY=(int)event.getY()-etContentY;
						etContent.setScrollY(etContent.getScrollY()-(offsetY*5));
						break;
				}
				etControl=true;
				return false;
			}
			else if(v==mainLayout)
			{
				etControl=false;
			}
			else if(v==scrollView)
			{
				return etControl;
			}
			return false;
		}
	}
}
