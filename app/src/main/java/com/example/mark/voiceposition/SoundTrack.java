package com.example.mark.voiceposition;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by Mark on 2016/5/26.
 * SoundTrack class order to emit chirp sound or fixed frequency sound
 */
public class SoundTrack
{
	AudioTrack audioTrack;

	// 聲音採集率
	private static int samplingRateInHz = 44100;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	//after the calculate of data buffer of sound
	private short audioTrackBuffer[];  //-32768 to 32767  2 Bytes
	//from hardware read data into the buffer position
	private static int outbufferSizeInBytes = 0;
	// 设置音频的录制的声道CHANNEL_CONFIGURATION__STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private static int outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
	// double the PI
	private static final double twoPI = 8. * Math.atan(1.); // 6.283185307179586

	/**
	 * component of fixed frequency sound
	 */
	// 振幅
	private int amplitude = 10000;
	// 播放頻率
	private static double frequency = 1000.f;

	/**
	 * set fixed frequency sound of amplitude
	 * @param amplitude= int number
	 */
	public void setAmplitude(int amplitude)
	{
		this.amplitude=amplitude;
		initAudioTrackForChirp();
	}

	/**
	 * get fixed frequency sound of amplitude
	 * @return= int amplitude
	 */
	public int getAmplitude()
	{
		return amplitude;
	}

	public void initAudioTrackForSin()
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

		audioTrackBuffer = new short[2205];
		double ph = 0.0;
		for (int i = 0; i < 2205; i++)
		{
			audioTrackBuffer[i] = (short) (amplitude * Math.sin(ph));
			ph += twoPI * frequency / samplingRateInHz;
		}
	}

	/**
	 * the chirp sound is 50ms, 3K->8K frequency LFM Singal
	 */
	public void initAudioTrackForChirp()
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
		int length = 2205;
		//time rate from slow to quickly
		double[] time = new double[length];
		//start frequency
		double startFrequency = 3000;
		// variety rate
		double rate1 = 50000;
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
/*
 * 原始的程式碼，沒經過縮減可以看得懂他在做什麼事，上面則是避免for迴圈重複增加效能
	public void initAudioTrackForChirp()
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

		int length = 2205;
		double[] time = new double[length];
		for (int i = 0; i < length; i++)
		{
			time[i] = (double) (i + 1) / (double) 44100;
		}
		double startFrequency = 3000;
		double rate1 = 50000;
		ComplexNumber x = new ComplexNumber();
		x.setRealPart(0.0);
		x.setImaginPart(1.0);
		ComplexNumber[] signal = new ComplexNumber[length];
		for (int i = 0; i < length; i++)
		{
			signal[i] = x.ComplexNumberMuti(twoPI * (startFrequency * time[i] + rate1 * time[i] * time[i]));
			signal[i] = signal[i].exp();
		}

		double[] real = new double[length];
		for (int i = 0; i < length; i++)
		{
			real[i] = signal[i].getRealPart();
		}

		audioTrackBuffer = new short[length];
		for (int i = 0; i < length; i++)
		{
			audioTrackBuffer[i] = (short) (real[i] * amplitude);
		}
	}
 */

	/**
	 * 避免創造執行緒花費效能，所以不使用
	 * public void startAudioTrack()
	 * {
	 * threadAudioTrack = new Thread(new AudioTrackRunnable());
	 * // set process priority
	 * threadAudioTrack.setPriority(Thread.MAX_PRIORITY);
	 * threadAudioTrack.start();
	 * }
	 **/

	public void writeDateToAudioTack()
	{
		audioTrack.play();
		//往track中寫數據
//		audioTrack.write(samples, 0, outbufferSizeInBytes);
		audioTrack.write(audioTrackBuffer, 0, 2205);
		audioTrack.stop();
//		audioTrack.release();
	}
}
