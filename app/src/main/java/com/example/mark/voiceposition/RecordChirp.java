package com.example.mark.voiceposition;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Mark on 2016/9/14.
 */
public class RecordChirp
{
	AudioTrack audioTrack;
	private static int outbufferSizeInBytes = 0;
	// 聲音採集率
	private static int samplingRateInHz = 44100;
	/**
	 * component of fixed frequency sound
	 */
	// 振幅
	private int amplitude = 10000;
	private static int outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
	// double the PI
	private static final double twoPI = 8. * Math.atan(1.); // 6.283185307179586
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	//after the calculate of data buffer of sound
	private short audioTrackBuffer[];  //-32768 to 32767  2 Bytes
	//單聲道
	private static int inChannelConfigInt=1;

	RecordChirp()
	{
		initChirp(8000,12000,0.05);
		writeFile(0.05);
		rawToWaveFile("/sdcard/Download/chirp.raw","/sdcard/Download/chirp.wav",0.05);
	}
	private void initChirp(int startFrequency,int endFrequnecy,double duration)
	{
		// set the buffer size 根據採樣率、採樣精度、單雙聲道 來得到一個frame的大小
		// AudioFormat.ENCODING_PCM_16BIT 一個採樣點16 bit-兩個字節
		// 按照數位音頻知識，算出來是一秒鐘buffer大小
		// CHANNEL_OUT_MONO(single sound channel)和 CHANNEL_IN_STEREO(double sound channel)
		outbufferSizeInBytes = AudioTrack.getMinBufferSize(samplingRateInHz,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		// create an audioTrack object
		// MODE_STREAM 和 MODE_STATIC有差，要使用哪個要仔細
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				samplingRateInHz, outChannelConfig,
				audioFormat, outbufferSizeInBytes,
				AudioTrack.MODE_STREAM);

		//sound time=2205/44100=0.05ms
		int length = (int)(duration * 44100);
		//time rate from slow to quickly
		double[] time = new double[length];

		// variety rate
		double rate1 = (endFrequnecy-startFrequency)/(duration*2);
		ComplexNumber x = new ComplexNumber();
		x.setRealPart(0.0);
		x.setImaginPart(1.0);
		ComplexNumber[] signal = new ComplexNumber[length];
		double[] real = new double[length];
		audioTrackBuffer = new short[length];
		for (int i = 0; i < length; i++)
		{
			time[i] = (double) (i + 1) / (double) 44100;
			signal[i] = x.ComplexNumberMuti(twoPI * (startFrequency * time[i] + rate1 * time[i] * time[i]));
			signal[i] = signal[i].exp();
			real[i] = signal[i].getRealPart();
			audioTrackBuffer[i] = (short) (real[i] * amplitude);
		}
	}

	private void writeFile(double duration)
	{
		DataOutputStream dos = null;

		//有沒有檔案存在，有的話刪掉重新放入
		try
		{
			File file = new File("/sdcard/Download/chirp.raw");
			if (file.exists())
			{
				file.delete();
			}
			//开通输出流到指定的文件
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			byte bData[] = shortToByte(audioTrackBuffer);
//				Log.e(TAG,"bData[]="+bData.length+"audioRecordReadSize="+audioRecordReadSize * 2);
			dos.write(bData, 0, (int)(duration*44100) * 2); //2205 short=> 4410 bites
		} catch (IOException e)
		{
			e.printStackTrace();
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
	private void rawToWaveFile(String inFilename, String outFilename, double duration)
	{
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;//這裏有可能有寫錯！！
		long longSampleRate = samplingRateInHz;

		int channels = inChannelConfigInt;
		long byteRate = 16 * samplingRateInHz * channels / 8;
		byte[] data = new byte[(int)((duration*44100)/2)+1];
		try
		{
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			Log.e("test", totalAudioLen + "");
			totalDataLen = totalAudioLen + 36;
			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			int size = 0;
			while ((size = in.read(data)) != -1)
			{
				System.out.println("copyWaveFile...." + size);
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
}
