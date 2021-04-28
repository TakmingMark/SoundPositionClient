package com.example.mark.SocketClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.mark.Protocol.HandlerProtocol;
import com.example.mark.Protocol.SocketProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Mark on 2016/7/4.
 * with server communication thread
 */
public class SocketThread extends Thread
{
	private final String TAG="VPSocketThread";
	//server IP
	private String serverIP="";
	// server port
	private int serverPort=0;
	// server return message
	private String line="";
	// local client IP
	private String userName="";

	// 因為client只會有一個socket對Server，所以宣告在外面
	private Socket socket=null;
	private DataOutputStream output=null;
	private DataInputStream input=null;
	// the message return MainActivity.java handler
	private Handler mHandler;


	/**
	 * construction of this class
	 * @param handler=intermediary of MainActivity.java
	 * @param serverIP= server IP
	 * @param serverPort= server Port
	 */
	public SocketThread(Handler handler, String serverIP, int serverPort)
	{
		this.mHandler=handler;
		this.serverIP=serverIP;
		this.serverPort=serverPort;
	}

	@Override
	public void run()
	{
		initSocketClient(serverIP, serverPort);
		initHeartBeat();
	}

	/**
	 * initial socket client , login and communicate with the server
	 * @param serverIP=server IP
	 * @param serverPort=server Port
	 */
	private void initSocketClient(String serverIP, int serverPort)
	{
		String msg,act;
		try
		{
			socket = new Socket( serverIP, serverPort );
			socket.setSoTimeout(60000);
			output=new DataOutputStream(socket.getOutputStream());
			input=new DataInputStream(socket.getInputStream());
			setIPAddress(); //當連接後再給予IPAdrress，才有連接port

			while(true)
			{
				//第一次連接，發送自己的IP過去
				output.writeUTF(SocketProtocol.C_USER_ROUND+userName+ SocketProtocol.C_USER_ROUND);
				line=readFromServer(input);
				if(line.equals(SocketProtocol.S_NAME_REP))
				{
					msg= "使用者IP重複，請檢查IP";
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);
					break;
				}
				else if(line.equals(SocketProtocol.S_LOGIN_SUCCESS))
				{
					sendMessage(userName,HandlerProtocol.selfIP_UPDATE);
					msg="使用者IP登入成功!";
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);
					break;
				}
			}
		}
		catch(UnknownHostException ex)
		{
			msg="找不到遠端伺服器，請確定伺服器已經啟動！";//可以做幾秒後自動連線
			act=HandlerProtocol.etContent_UPDATE;
			sendMessage(msg, act);
			closeSocket();
		}
		catch(ConnectException ex)
		{
			msg="找不到遠端伺服器，請確定伺服器已經啟動！";//可以做幾秒後自動連線
			act=HandlerProtocol.etContent_UPDATE;
			sendMessage(msg, act);
			closeSocket();
		} catch ( IOException e )
		{
			e.printStackTrace();
			closeSocket();
		}
		new Thread(new ClientThread()).start();
	}

	/**
	 * set IP address in userName
	 */
	private void setIPAddress()
	{
		userName=socket.getLocalAddress()+":"+socket.getLocalPort();
	}

	/**
	 * read from server DataInputStream data
	 * @param input=Server input message
	 * @return =after transform data
	 */
	private String readFromServer(DataInputStream input)
	{
		String msg,act;
		try
		{
			return input.readUTF();
		}
		catch(SocketTimeoutException e)
		{
			msg="連線超時："+ e ;
			act=HandlerProtocol.etContent_UPDATE;
			sendMessage(msg, act);
			closeSocket();
			return null;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			System.out.println("ClientReceiveThread run error:" + e);
			closeSocket();
			return null;
		}
	}

	/**
	 * close socket
	 */
	private void closeSocket()
	{
		if(input!=null)
			input=null;
		if(output!=null)
			output=null;
		if(socket!=null)
			socket=null;
//		System.exit(1);
	}

	/**
	 * initial heart beat
	 */
	private void initHeartBeat()
	{
		String msg= SocketProtocol.C_HEART_BEAT+userName+ SocketProtocol.C_HEART_BEAT;
		new Thread(new ClientHeartBeatThread(msg)).start();
	}

	/**
	 * this class message transfer MainActivity.java
	 * @param msg=message
	 * @param act=action
	 */
	private void sendMessage(String msg , String act)
	{
		Log.e(TAG,"msg="+msg+",act="+act);
		Message message=new Message();
		message.what= Integer.valueOf(act);
		Bundle data=new Bundle();
		data.putString("message", msg);
		message.setData(data);
		mHandler.sendMessage(message);
	}

	/**
	 * other java method message transfer this class
	 * @param message
	 */
	public void sendServer(String message)
	{
		new Thread(new ClientSentThread(message)).start();
	}

	/**
	 * keep with server communicate , handler from server message
	 */
	class ClientThread implements Runnable //可以一直接收伺服器訊息
	{
		@Override
		public void run()
		{
			String msg="",act="";
			while((line=readFromServer(input))!=null) //input.readUTF()會等待stream，直到傳東西回來
			{
				Log.e(TAG, "line="+line);
				msg=getRealMsg(line);
				if(line.startsWith(SocketProtocol.S_USER_LOGIN) && line.endsWith(SocketProtocol.S_USER_LOGIN))
				{
					act=HandlerProtocol.spUser_INSERT;
					sendMessage(msg, act);
				}
				else if(line.startsWith(SocketProtocol.S_USER_LOGOUT) && line.endsWith(SocketProtocol.S_USER_LOGOUT))
				{
					act=HandlerProtocol.spUser_DELETE;
					sendMessage(msg, act);
				}
				else if(line.startsWith(SocketProtocol.S_PRIVATE_ROUND) && line.endsWith(SocketProtocol.S_PRIVATE_ROUND))
				{
					String user=msg.split("\\"+ SocketProtocol.SPLIT_SIGN)[0];
					String content=msg.split("\\"+ SocketProtocol.SPLIT_SIGN)[1];
					msg=user+":"+content;
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);
				}
				else if(line.startsWith(SocketProtocol.S_GIVEN_ROOM_AND_NO) && line.endsWith(SocketProtocol.S_GIVEN_ROOM_AND_NO))
				{
					act=HandlerProtocol.give_INFORMATION;
					sendMessage(msg,act);
				}
				else if(line.startsWith(SocketProtocol.S_RETURN_NODE_POSITION) && line.endsWith(SocketProtocol.S_RETURN_NODE_POSITION))
				{
					act=HandlerProtocol.draw_Node;
					sendMessage(msg,act);
				}
				else if(line.startsWith(SocketProtocol.S_USER_START_SOUND_TRACK) && line.endsWith(SocketProtocol.S_USER_START_SOUND_TRACK))
				{
					if(msg==userName || msg.equals(userName))
					{
						act=HandlerProtocol.Start_SoundTrack;
						sendMessage(msg,act);
					}
				}
				else if(line.startsWith(SocketProtocol.S_USER_STOP_SOUND_RECORD) && line.endsWith(SocketProtocol.S_USER_STOP_SOUND_RECORD))
				{
					act=HandlerProtocol.Stop_Rerord_Sound;
					sendMessage(msg,act);
				}
				else if(line.startsWith(SocketProtocol.S_SUCCESS_CALCULATE_COORDINATE) && line.endsWith(SocketProtocol.S_SUCCESS_CALCULATE_COORDINATE))
				{
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);

					act=HandlerProtocol.success_Calculate_Coordinate;
					sendMessage(msg,act);
				}
				else if(line.startsWith(SocketProtocol.S_SUCCESS_CALCULATE_DISTANCE) && line.startsWith(SocketProtocol.S_SUCCESS_CALCULATE_DISTANCE))
				{
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);

					act=HandlerProtocol.success_Calculate_Distance;
					sendMessage(msg,act);
				}
				else if(line.startsWith(SocketProtocol.S_MSG_ROUND) && line.startsWith(SocketProtocol.S_MSG_ROUND))
				{
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(msg, act);
				}
				else if(line.startsWith(SocketProtocol.S_HEART_BEAT) && line.endsWith(SocketProtocol.S_HEART_BEAT))
				{
					try
					{
						socket.setSoTimeout(60000);
						act=HandlerProtocol.etContent_UPDATE;
						sendMessage(msg, act);
					} catch (SocketException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					Log.e(TAG,"else,No split line="+line);
					act=HandlerProtocol.etContent_UPDATE;
					sendMessage(line, act);
				}
			}
		}

		/**
		 * delete package communication of header and trailer
		 * @param line= include package communication of header and trailer
		 * @return = Communication package of after removal
		 */
		public String getRealMsg(String line)
		{
			return line.substring(SocketProtocol.PROTOCOL_LEN,line.length()- SocketProtocol.PROTOCOL_LEN);
		}

	}

	/**
	 * the message transfer server
	 */
	class ClientSentThread implements Runnable
	{

		String msg;
		public ClientSentThread(String msg)
		{
			this.msg=msg;
		}
		@Override
		public void run()
		{
			try
			{
				Log.e(TAG, "msg="+msg);
				output.writeUTF(msg);
				output.flush();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * always know there is no break with the server
	 */
	class ClientHeartBeatThread implements Runnable
	{
		String msg;
		public ClientHeartBeatThread(String msg)
		{
			this.msg=msg;
		}
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					output.writeUTF(msg);
					output.flush();
					Thread.sleep(30000); //心跳速度
				}
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
