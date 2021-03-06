package com.example.mark.voiceposition;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Mark on 2016/5/26.
 * SoundRecord class order to recording the elapsed time
 */
public class SoundRecord
{
	private final String TAG="SoundRecord";
	AudioRecord audioRecord;
	Thread audioThread;
	private Handler mHandler;

	// 聲音採集率
	private static int samplingRateInHz = 44100;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	//缓冲区字节
	private static int inbufferSizeInBytes = 0;
	// 设置音频的录制的声道CHANNEL_CONFIGURATION__STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private static int inChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
	//雙聲道
	private static int inChannelConfigInt=2;
	// 錄製聲音來源
	private static int audioSource = MediaRecorder.AudioSource.MIC;
	//控制是否繼續錄製
	private static boolean isRecording = false;
	// AudioName裸音频数据文件
	private static final String AudioName = "/sdcard/Download/new.raw";
	// NewAudioName可播放的音频文件
	private static String NewAudioName = "/sdcard/Download/new.wav";

	public SoundRecord(Handler mhandler)
	{
		// 获得缓冲区字节大小(很像Buffer調高，能讓採樣不丟失？？)
		inbufferSizeInBytes = AudioRecord.getMinBufferSize(samplingRateInHz,
				inChannelConfig, audioFormat);

		// 创建AudioRecord对象
		audioRecord = new AudioRecord(audioSource, samplingRateInHz,
				inChannelConfig, audioFormat, inbufferSizeInBytes);

		this.mHandler=mhandler;
//		findAudioRecord();
	}

	/**
	 * 	此方法可以找尋最佳AudioRecord組合
	 */
	public AudioRecord findAudioRecord() {
		Log.d("test","AudioFormat.ENCODING_PCM_8BIT="+ AudioFormat.ENCODING_PCM_8BIT);
		Log.d("test","AudioFormat.ENCODING_PCM_16BIT="+ AudioFormat.ENCODING_PCM_16BIT);
		Log.d("test","AudioFormat.CHANNEL_IN_MONO="+ AudioFormat.CHANNEL_IN_MONO);
		Log.d("test","AudioFormat.CHANNEL_IN_STEREO="+ AudioFormat.CHANNEL_IN_STEREO);
		int[] mSampleRates = new int[] { 44100,22050,11025,8000};
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] {  AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT }) {
				for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_IN_MONO }) {
					try {

						int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(audioSource, rate, channelConfig, audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
							{
								Log.d("test", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
										+ channelConfig);
								return recorder;
							}
						}
					} catch (Exception e) {
						Log.e("test", rate + "Exception, keep trying.",e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * set file name is name and rank
	 */
	public void setNewAudioRank(String rank)
	{
		NewAudioName="/sdcard/Download/new"+rank+".wav";
	}

	/**
	 * Start android audio record ,create new thread run audio recrod ,avoid lock in UI thread
	 */
	public void startAudioRecord()
	{
		audioRecord.startRecording();
		// 让录制状态为true
		isRecording = true;
		// 开启音频文件写入线程
		audioThread=new Thread(new AudioRecordRunnable());
		audioThread.start();
	}

	public void stopAudioRecord()
	{
		if (audioRecord != null)
		{
			System.out.println("stopRecord");
			isRecording = false;// 停止文件写入
			audioRecord.stop();
		}
	}

	/**
	 * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
	 *
	 * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
	 *
	 * 在这里就进行音频的处理，然后在別處重新封装 所以这样得到的音频比较容易做一些音频的处理。
	 */
	private void writeDateToFile()
	{
		// new一个short陣列用来存一些字节数据，大小为缓冲区大小
		short[] audioRecordBuffer = new short[inbufferSizeInBytes];
		DataOutputStream dos = null;
		int audioRecordReadSize = 0;
		FileOutputStream fos=null;
		//有沒有檔案存在，有的話刪掉重新放入
		try
		{
			File file = new File(AudioName);
			if (file.exists())
			{
				file.delete();
			}
			//开通输出流到指定的文件
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			fos=new FileOutputStream(file);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		while (isRecording == true)
		{
			try
			{
				audioRecordReadSize = audioRecord.read(audioRecordBuffer, 0, inbufferSizeInBytes);//除與2比較穩定的樣子
				byte bData[] = shortToByte(audioRecordBuffer);
//				Log.e(TAG,"bData[]="+bData.length+"audioRecordReadSize="+audioRecordReadSize * 2);

				if(audioRecord.ERROR_INVALID_OPERATION!=audioRecordReadSize)
				{
					dos.write(bData, 0, audioRecordReadSize * 2);
				}

			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			dos.close();// 关闭写入流
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * let short array convert to byte array
	 * @param sData= short array
	 * @return=byte array
	 */
	private byte[] shortToByte(short[] sData)
	{
		int shortArrsize = sData.length;
		byte[] bytes = new byte[shortArrsize * 2];

		for (int i = 0; i < shortArrsize; i++)
		{
			bytes[i * 2] = (byte) (sData[i] & 0x00FF);
			bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
			sData[i] = 0;
		}
		return bytes;
	}

	/**
	 * the original audio data convert to audio wav
	 * @param inFilename= original audio data(.raw)
	 * @param outFilename=converted audio data(.wav)
	 */
	private void rawToWaveFile(String inFilename, String outFilename)
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;//這裏有可能有寫錯！！
		long longSampleRate = samplingRateInHz;

		int channels = inChannelConfigInt;
		long byteRate = 16 * samplingRateInHz * channels / 8;
		byte[] data = new byte[inbufferSizeInBytes];
		try
		{
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			int size = 0;
			while ((size = in.read(data)) != -1)
			{
//				System.out.println("copyWaveFile...." + size);
				out.write(data, 0, size);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
	 *
	 * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 *
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
	 *
	 * 自己特有的头文件。
	 */
	private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException
	{
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
/*-----------------------other function-----------------------------------------------------------*/
	/**
	 * the other write raw to wave file function
	 * @param rawFile= raw audio data
	 * @param waveFile= wav audio data
	 * @throws IOException
	 */
	private void rawToWave(final File rawFile, final File waveFile) throws IOException
	{
		byte[] rawData = new byte[(int) rawFile.length()];
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(rawFile));
			input.read(rawData);
		} finally {
			if (input != null) {
				input.close();
			}
		}

		DataOutputStream output = null;
		try {
			output = new DataOutputStream(new FileOutputStream(waveFile));
			// WAVE header
			// see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
			writeString(output, "RIFF"); // chunk id
			writeInt(output, 36 + rawData.length); // chunk size
			writeString(output, "WAVE"); // format
			writeString(output, "fmt "); // subchunk 1 id
			writeInt(output, 16); // subchunk 1 size
			writeShort(output, (short) 1); // audio format (1 = PCM)
			writeShort(output, (short) inChannelConfigInt); // number of channels
			writeInt(output, samplingRateInHz); // sample rate
			writeInt(output, 16 * samplingRateInHz * inChannelConfigInt / 8); // byte rate
			writeShort(output, (short) (2*16/8)); // block align
			writeShort(output, (short) 16); // bits per sample
			writeString(output, "data"); // subchunk 2 id
			writeInt(output, rawData.length); // subchunk 2 size
			// Audio data (conversion big endian -> little endian)
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
			ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				bytes.putShort(s);
			}
			output.write(bytes.array());
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	private void writeInt(final DataOutputStream output, final int value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
		output.write(value >> 16);
		output.write(value >> 24);
	}

	private void writeShort(final DataOutputStream output, final short value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
	}

	private void writeString(final DataOutputStream output, final String value) throws IOException {
		for (int i = 0; i < value.length(); i++) {
			output.write(value.charAt(i));
		}
	}
/*----------------------------------------------------------------------------------*/

	public class AudioRecordRunnable implements Runnable
	{
		@Override
		public void run()
		{
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			writeDateToFile();// 往文件中写入裸数据
			rawToWaveFile(AudioName, NewAudioName);// 给裸数据加上头文件
			//end write to file
			sendMessage(1);
			/* other function */
//			File in = new File(AudioName);
//			File out = new File(NewAudioName);
//			try
//			{
//				rawToWave(in,out); 這個有問題 有空可以改寫一下 比較簡單
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//			}
		}
	}

	/**
	 * the message transfer MainActivity
	 * @param finishMsg=1,mean raw to wav conversion is complete
	 */
	public void sendMessage(int finishMsg)
	{
		Message message=new Message();
		message.what= Integer.valueOf(finishMsg);
		mHandler.sendMessage(message);
	}
}
