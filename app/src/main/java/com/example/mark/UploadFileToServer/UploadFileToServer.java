package com.example.mark.UploadFileToServer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mark on 2016/8/26.
 * oder to upload file to server
 */
public class UploadFileToServer implements Runnable
{

	private final String TAG="VPUploadFileToServer";
	// let android device transfer audio file to sever php file
	private String serverURL = "/VoicePosition/PHP/UploadToServer.php";
	// server IP
	private String serverIP;
	// need transfer file path
	private String selectedFilePath="/sdcard/Download/";
	// file name
	private String fileName;
	// the message return MainActivity.java handler
	private Handler mHandler;

	/**
	 * construction of this class
	 * @param handler= intermediary of MainActivity.java
	 * @param serverIP= server IP
	 */
	public UploadFileToServer(Handler handler, String serverIP)
	{
		serverURL="http://"+serverIP+serverURL;
		mHandler=handler;
		this.serverIP=serverIP;
	}

	/**
	 * set file name plus rank
	 * @param fileRank
	 */
	public void setFileRank(String fileRank)
	{
		fileName="new"+fileRank+".wav";
	}

	@Override
	public void run()
	{
		if(fileExist()==true)
		{
			uploadFile(selectedFilePath + fileName);
		}
		else
		{
			Log.e(TAG,"File not exist");
		}

	}

	/**
	 * 	android upload file to server method
	 */
	private int uploadFile(final String selectedFilePathName)
	{
		Log.e(TAG, serverURL);
		int serverResponseCode = 0;

		HttpURLConnection connection;
		DataOutputStream dataOutputStream;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead,bytesAvailable,bufferSize;
		byte[] buffer;
		int maxBufferSize =1 * 1024;
		File selectedFile = new File(selectedFilePathName);

		String[] parts = selectedFilePathName.split("/");
		final String fileName = parts[parts.length-1];
		Log.e(TAG, fileName);
		if (!selectedFile.isFile()){
			Log.e(TAG, "Source File Doesn't Exist: " + selectedFilePathName);
			return 0;
		}else{
			try{
				//read File
				FileInputStream fileInputStream = new FileInputStream(selectedFile);
				URL url = new URL(serverURL);
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);//Allow Inputs
				connection.setDoOutput(true);//Allow Outputs
				connection.setUseCaches(false);//Don't use a cached Copy
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("ENCTYPE", "multipart/form-data");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				connection.setRequestProperty("uploaded_file", selectedFilePathName);

				//creating new dataoutputstream(write file)
				dataOutputStream = new DataOutputStream(connection.getOutputStream());

				//writing bytes to data outputstream
				dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
				dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ selectedFilePathName + "\"" + lineEnd);

				dataOutputStream.writeBytes(lineEnd);

				//returns file total of bytes present in fileInputStream
				bytesAvailable = fileInputStream.available();
				//selecting the buffer size as minimum of available bytes or 1 MB
				bufferSize = Math.min(bytesAvailable,maxBufferSize);
				//setting the buffer as byte array of size of bufferSize
				buffer = new byte[bufferSize];

				//reads bytes from FileInputStream(from 0th index of buffer to buffersize)


				//loop repeats till bytesRead = -1, i.e., no bytes are left to read

				while((bytesRead=fileInputStream.read(buffer,0,bufferSize))>0)
				{
					//write the bytes read from inputstream
					dataOutputStream.write(buffer,0,bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					Log.d("bytesRead",bytesRead+"");
				}

				dataOutputStream.writeBytes(lineEnd);
				dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				serverResponseCode = connection.getResponseCode();
				String serverResponseMessage = connection.getResponseMessage();

				Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

				//response code of 200 indicates the server status OK
				if(serverResponseCode == 200){
					Log.e(TAG, "File Upload completed.\n\n You can see the uploaded file here: \n\n" + serverIP + "uploads/" + fileName);
				}

				//closing the input and output streams
				fileInputStream.close();
				dataOutputStream.flush();
				dataOutputStream.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.e(TAG, "File Not Found");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.e(TAG, "URL error!");

			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Cannot Read/Write File!");
			}
			Log.e(TAG, "transmission end");

			sendMessage(String.valueOf(serverResponseCode));
			deleteFile();
			return serverResponseCode;
		}


	}

	/**
	 * if upload success , delete file
	 */
	private void deleteFile()
	{
		File file=new File(selectedFilePath+fileName);
		if(file.exists())
		{
//			file.delete();
		}
	}

	/**
	 *
	 */
	private boolean fileExist()
	{
		File file=new File(selectedFilePath+fileName);
		if(file.exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	/**
	 * the message transfer MainActivity
	 * @param serverResponseCode= response code after upload file
	 */
	public void sendMessage(String serverResponseCode)
	{
		Message message=new Message();
		message.what= Integer.valueOf(serverResponseCode);
		mHandler.sendMessage(message);
	}

}
