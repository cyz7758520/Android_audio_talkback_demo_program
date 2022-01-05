package HeavenTao.Media;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import HeavenTao.Ado.*;
import HeavenTao.Vdo.*;
import HeavenTao.Data.*;
import HeavenTao.Media.*;

//媒体处理线程类。
public abstract class MediaPocsThrd extends Thread
{
	public static String m_CurClsNameStrPt = "MediaPocsThrd"; //存放当前类名称字符串。

	public int m_RunFlag; //存放本线程运行标记。
	public static final int RUN_FLAG_NORUN = 0; //运行标记：未开始运行。
	public static final int RUN_FLAG_INIT = 1; //运行标记：刚开始运行正在初始化。
	public static final int RUN_FLAG_POCS = 2; //运行标记：初始化完毕正在循环处理帧。
	public static final int RUN_FLAG_DSTOY = 3; //运行标记：跳出循环处理帧正在销毁。
	public static final int RUN_FLAG_END = 4; //运行标记：销毁完毕。
	public int m_ExitFlag; //存放本线程退出标记，为0表示保持运行，为1表示请求退出，为2表示请求重启，为3表示请求重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。
	public int m_ExitCode; //存放本线程退出代码，为0表示正常退出，为-1表示初始化失败，为-2表示处理失败。

	public static Context m_AppCntxtPt; //存放应用程序上下文的指针。

	int m_IsSaveStngToFile; //存放是否保存设置到文件，为非0表示要保存，为0表示不保存。
	String m_StngFileFullPathStrPt; //存放设置文件的完整路径字符串的指针。

	public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
	public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
	public Activity m_ShowToastActivityPt; //存放显示Toast界面的指针。

	int m_IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。
	PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁的指针。
	PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

	public class AdoInpt //音频输入类。
	{
		public int m_IsUseAdoInpt; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。

		public int m_SmplRate; //存放采样频率，取值只能为8000、16000、32000、48000。
		public int m_FrmLen; //存放帧的长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。

		public int m_IsUseSystemAecNsAgc; //存放是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带），为0表示不使用，为非0表示要使用。

		public int m_UseWhatAec; //存放使用什么声学回音消除器，为0表示不使用，为1表示Speex声学回音消除器，为2表示WebRtc定点版声学回音消除器，为2表示WebRtc浮点版声学回音消除器，为4表示SpeexWebRtc三重声学回音消除器。

		SpeexAec m_SpeexAecPt; //存放Speex声学回音消除器的指针。
		int m_SpeexAecFilterLen; //存放Speex声学回音消除器的滤波器长度，单位毫秒。
		int m_SpeexAecIsUseRec; //存放Speex声学回音消除器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
		float m_SpeexAecEchoMultiple; //存放Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
		float m_SpeexAecEchoCont; //存放Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
		int m_SpeexAecEchoSupes; //存放Speex声学回音消除器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
		int m_SpeexAecEchoSupesAct; //存放Speex声学回音消除器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
		int m_SpeexAecIsSaveMemFile; //存放Speex声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
		String m_SpeexAecMemFileFullPathStrPt; //存放Speex声学回音消除器的内存块文件完整路径字符串的指针。

		WebRtcAecm m_WebRtcAecmPt; //存放WebRtc定点版声学回音消除器的指针。
		int m_WebRtcAecmIsUseCNGMode; //存放WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
		int m_WebRtcAecmEchoMode; //存放WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
		int m_WebRtcAecmDelay; //存放WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。

		WebRtcAec m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器的指针。
		int m_WebRtcAecEchoMode; //存放WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
		int m_WebRtcAecDelay; //存放WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
		int m_WebRtcAecIsUseDelayAgstcMode; //存放WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
		int m_WebRtcAecIsUseExtdFilterMode; //存放WebRtc浮点版声学回音消除器是否使用扩展滤波器模式，为非0表示要使用，为0表示不使用。
		int m_WebRtcAecIsUseRefinedFilterAdaptAecMode; //存放WebRtc浮点版声学回音消除器是否使用精制滤波器自适应Aec模式，为非0表示要使用，为0表示不使用。
		int m_WebRtcAecIsUseAdaptAdjDelay; //存放WebRtc浮点版声学回音消除器是否使用自适应调节回音的延迟，为非0表示要使用，为0表示不使用。
		int m_WebRtcAecIsSaveMemFile; //存放WebRtc浮点版声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
		String m_WebRtcAecMemFileFullPathStrPt; //存放WebRtc浮点版声学回音消除器的内存块文件完整路径字符串的指针。

		SpeexWebRtcAec m_SpeexWebRtcAecPt; //存放SpeexWebRtc三重声学回音消除器的指针。
		int m_SpeexWebRtcAecWorkMode; //存放SpeexWebRtc三重声学回音消除器的工作模式，为1表示Speex声学回音消除器+WebRtc定点版声学回音消除器，为2表示WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器，为3表示Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
		int m_SpeexWebRtcAecSpeexAecFilterLen; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器的滤波器长度，单位毫秒。
		int m_SpeexWebRtcAecSpeexAecIsUseRec; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
		float m_SpeexWebRtcAecSpeexAecEchoMultiple; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
		float m_SpeexWebRtcAecSpeexAecEchoCont; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
		int m_SpeexWebRtcAecSpeexAecEchoSupes; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
		int m_SpeexWebRtcAecSpeexAecEchoSupesAct; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
		int m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecWebRtcAecmEchoMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
		int m_SpeexWebRtcAecWebRtcAecmDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
		int m_SpeexWebRtcAecWebRtcAecEchoMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
		int m_SpeexWebRtcAecWebRtcAecDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
		int m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用扩展滤波器模式，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用精制滤波器自适应Aec模式，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用自适应调节回音的延迟，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecIsUseSameRoomAec; //存放SpeexWebRtc三重声学回音消除器是否使用同一房间声学回音消除，为非0表示要使用，为0表示不使用。
		int m_SpeexWebRtcAecSameRoomEchoMinDelay; //存放SpeexWebRtc三重声学回音消除器的同一房间回音最小延迟，单位毫秒，取值区间为[1,2147483647]。

		public int m_UseWhatNs; //存放使用什么噪音抑制器，为0表示不使用，为1表示Speex预处理器的噪音抑制，为2表示WebRtc定点版噪音抑制器，为3表示WebRtc浮点版噪音抑制器，为4表示RNNoise噪音抑制器。

		SpeexPrpocs m_SpeexPrpocsPt; //存放Speex预处理器的指针。
		int m_SpeexPrpocsIsUseNs; //存放Speex预处理器是否使用噪音抑制，为非0表示要使用，为0表示不使用。
		int m_SpeexPrpocsNoiseSupes; //存放Speex预处理器在噪音抑制时，噪音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
		int m_SpeexPrpocsIsUseDereverb; //存放Speex预处理器是否使用混响音消除，为非0表示要使用，为0表示不使用。

		WebRtcNsx m_WebRtcNsxPt; //存放WebRtc定点版噪音抑制器的指针。
		int m_WebRtcNsxPolicyMode; //存放WebRtc定点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

		WebRtcNs m_WebRtcNsPt; //存放WebRtc浮点版噪音抑制器的指针。
		int m_WebRtcNsPolicyMode; //存放WebRtc浮点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

		RNNoise m_RNNoisePt; //存放RNNoise噪音抑制器的指针。

		int m_IsUseSpeexPrpocsOther; //存放Speex预处理器是否使用其他功能，为非0表示要使用，为0表示不使用。
		int m_SpeexPrpocsIsUseVad; //存放Speex预处理器是否使用语音活动检测，为非0表示要使用，为0表示不使用。
		int m_SpeexPrpocsVadProbStart; //存放Speex预处理器在语音活动检测时，从无语音活动到有语音活动的判断百分比概率，概率越大越难判断为有语音活，取值区间为[0,100]。
		int m_SpeexPrpocsVadProbCont; //存放Speex预处理器在语音活动检测时，从有语音活动到无语音活动的判断百分比概率，概率越大越容易判断为无语音活动，取值区间为[0,100]。
		int m_SpeexPrpocsIsUseAgc; //存放Speex预处理器是否使用自动增益控制，为非0表示要使用，为0表示不使用。
		int m_SpeexPrpocsAgcLevel; //存放Speex预处理器在自动增益控制时，增益的目标等级，目标等级越大增益越大，取值区间为[1,2147483647]。
		int m_SpeexPrpocsAgcIncrement; //存放Speex预处理器在自动增益控制时，每秒最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。
		int m_SpeexPrpocsAgcDecrement; //存放Speex预处理器在自动增益控制时，每秒最大减益的分贝值，分贝值越小减益越大，取值区间为[-2147483648,0]。
		int m_SpeexPrpocsAgcMaxGain; //存放Speex预处理器在自动增益控制时，最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。

		public int m_UseWhatEncd; //存放使用什么编码器，为0表示PCM原始数据，为1表示Speex编码器，为2表示Opus编码器。

		SpeexEncd m_SpeexEncdPt; //存放Speex编码器的指针。
		int m_SpeexEncdUseCbrOrVbr; //存放Speex编码器使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
		int m_SpeexEncdQualt; //存放Speex编码器的编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
		int m_SpeexEncdCmplxt; //存放Speex编码器的编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
		int m_SpeexEncdPlcExptLossRate; //存放Speex编码器在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。

		public int m_IsSaveAdoToFile; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
		WaveFileWriter m_AdoInptWaveFileWriterPt; //存放音频输入Wave文件写入器对象的指针。
		WaveFileWriter m_AdoRsltWaveFileWriterPt; //存放音频结果Wave文件写入器对象的指针。
		String m_AdoInptFileFullPathStrPt; //存放音频输入文件的完整路径字符串的指针。
		String m_AdoRsltFileFullPathStrPt; //存放音频结果文件的完整路径字符串的指针。

		public int m_IsDrawAdoWavfmToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
		SurfaceView m_AdoInptOscilloSurfacePt; //存放音频输入波形Surface对象的指针。
		AdoWavfm m_AdoInptOscilloPt; //存放音频输入波形器对象的指针。
		SurfaceView m_AdoRsltOscilloSurfacePt; //存放音频结果波形Surface对象的指针。
		AdoWavfm m_AdoRsltOscilloPt; //存放音频结果波形器对象的指针。

		AudioRecord m_AdoInptDvcPt; //存放音频输入设备的指针。
		int m_AdoInptDvcBufSz; //存放音频输入设备缓冲区大小，单位字节。
		int m_AdoInptIsMute; //存放音频输入是否静音，为0表示有声音，为非0表示静音。

		public LinkedList< short[] > m_AdoInptFrmLnkLstPt; //存放音频输入帧链表的指针。
		public LinkedList< short[] > m_AdoInptIdleFrmLnkLstPt; //存放音频输入空闲帧链表的指针。

		//音频输入线程的临时变量。
		short m_AdoInptFrmPt[]; //存放音频输入帧的指针。
		int m_AdoInptFrmLnkLstElmTotal; //存放音频输入帧链表的元数总数。
		long m_LastTimeMsec; //存放上次时间的毫秒数。
		long m_NowTimeMsec; //存放本次时间的毫秒数。

		AdoInptThrd m_AdoInptThrdPt; //存放音频输入线程的指针。
		int m_AdoInptThrdExitFlag; //存放音频输入线程退出标记，0表示保持运行，1表示请求退出。
	}

	public AdoInpt m_AdoInptPt = new AdoInpt(); //存放音频输入的指针。

	public class AdoOtpt //音频输出类。
	{
		public int m_IsUseAdoOtpt; //存放是否使用音频输出，为0表示不使用，为非0表示要使用。

		public int m_SmplRate; //存放采样频率，取值只能为8000、16000、32000、48000。
		public int m_FrmLen; //存放帧的长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。

		public int m_UseWhatDecd; //存放使用什么解码器，为0表示PCM原始数据，为1表示Speex解码器，为2表示Opus解码器。

		SpeexDecd m_SpeexDecdPt; //存放Speex解码器的指针。
		int m_SpeexDecdIsUsePrcplEnhsmt; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

		public int m_IsSaveAdoToFile; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
		WaveFileWriter m_AdoOtptWaveFileWriterPt; //存放音频输出Wave文件写入器对象的指针。
		String m_AdoOtptFileFullPathStrPt; //存放音频输出文件的完整路径字符串。

		public int m_IsDrawAdoWavfmToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
		SurfaceView m_AdoOtptOscilloSurfacePt; //存放音频输出波形Surface对象的指针。
		AdoWavfm m_AdoOtptOscilloPt; //存放音频输出波形器对象的指针。

		public AudioTrack m_AdoOtptDvcPt; //存放音频输出设备的指针。
		int m_AdoOtptDvcBufSz; //存放音频输出设备缓冲区大小，单位字节。
		public int m_UseWhatAdoOtptDvc; //存放使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
		public int m_UseWhatAdoOtptStreamType; //存放使用什么音频输出流类型，为0表示通话类型，为非0表示媒体类型。
		int m_AdoOtptIsMute; //存放音频输出是否静音，为0表示有声音，为非0表示静音。

		public LinkedList< short[] > m_AdoOtptFrmLnkLstPt; //存放音频输出帧链表的指针。
		public LinkedList< short[] > m_AdoOtptIdleFrmLnkLstPt; //存放音频输出空闲帧链表的指针。

		//音频输出线程的临时变量。
		short m_AdoOtptFrmPt[]; //存放音频输出帧的指针。
		byte m_EncdAdoOtptFrmPt[]; //存放已编码格式音频输出帧的指针。
		HTLong m_AdoOtptFrmLenPt; //存放音频输出帧的长度，单位字节。
		int m_AdoOtptFrmLnkLstElmTotal; //存放音频输出帧链表的元数总数。
		long m_LastTimeMsec; //存放上次时间的毫秒数。
		long m_NowTimeMsec; //存放本次时间的毫秒数。

		AdoOtptThrd m_AdoOtptThrdPt; //存放音频输出线程的指针。
		int m_AdoOtptThrdExitFlag; //存放音频输出线程退出标记，0表示保持运行，1表示请求退出。
	}

	public AdoOtpt m_AdoOtptPt = new AdoOtpt(); //存放音频输出的指针。

	public class VdoInpt //视频输入类。
	{
		public int m_IsUseVdoInpt; //存放是否使用视频输入，为0表示不使用，为非0表示要使用。

		public int m_MaxSmplRate; //存放最大采样频率，取值范围为[1,60]，实际帧率和图像的亮度有关，亮度较高时采样频率可以达到最大值，亮度较低时系统就自动降低采样频率来提升亮度。
		public int m_FrmWidth; //存放屏幕旋转0度时，帧的宽度，单位为像素。
		public int m_FrmHeight; //存放屏幕旋转0度时，帧的高度，单位为像素。
		public int m_ScreenRotate; //存放屏幕旋转的角度，只能为0、90、180、270，0度表示竖屏，其他表示顺时针旋转。

		public int m_UseWhatEncd; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264编码器，为2表示系统自带H264编码器。

		OpenH264Encd m_OpenH264EncdPt; //存放OpenH264编码器的指针。
		int m_OpenH264EncdVdoType;//存放OpenH264编码器的视频类型，为0表示实时摄像头视频，为1表示实时屏幕内容视频，为2表示非实时摄像头视频，为3表示非实时屏幕内容视频，为4表示其他视频。
		int m_OpenH264EncdEncdBitrate; //存放OpenH264编码器的编码后比特率，单位为bps。
		int m_OpenH264EncdBitrateCtrlMode; //存放OpenH264编码器的比特率控制模式，为0表示质量优先模式，为1表示比特率优先模式，为2表示缓冲区优先模式，为3表示时间戳优先模式。
		int m_OpenH264EncdIDRFrmIntvl; //存放OpenH264编码器的IDR帧间隔帧数，单位为个，为0表示仅第一帧为IDR帧，为大于0表示每隔这么帧就至少有一个IDR帧。
		int m_OpenH264EncdCmplxt; //存放OpenH264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

		SystemH264Encd m_SystemH264EncdPt; //存放系统自带H264编码器的指针。
		int m_SystemH264EncdEncdBitrate; //存放系统自带H264编码器的编码后比特率，单位为bps。
		int m_SystemH264EncdBitrateCtrlMode; //存放系统自带H264编码器的比特率控制模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ(0x00)表示质量模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR(0x01)表示动态比特率模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR(0x02)表示固定比特率模式。
		int m_SystemH264EncdIDRFrmIntvlTimeSec; //存放系统自带H264编码器的IDR帧间隔时间，单位为秒，为负数表示仅第一帧为IDR帧，为0表示每一帧都为IDR帧，为大于0表示每这么多秒就有一个IDR帧。
		int m_SystemH264EncdCmplxt; //存放系统自带H264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

		public Camera m_VdoInptDvcPt; //存放视频输入设备的指针。
		public int m_UseWhatVdoInptDvc; //存放使用什么视频输入设备，为0表示前置摄像头，为1表示后置摄像头。
		int m_FrontCameraDvcId = -1; //存放前置摄像头的设备ID，为-1表示自动查找。
		int m_BackCameraDvcId = -1; //存放后置摄像头的设备ID，为-1表示自动查找。
		public HTSurfaceView m_VdoInptPrvwSurfaceViewPt; //存放视频输入预览SurfaceView的指针。
		public byte m_VdoInptPrvwClbkBufPtPt[][]; //存放视频输入预览回调函数缓冲区的指针。
		int m_VdoInptDvcFrmWidth; //存放视频输入设备帧的宽度，单位为像素。
		int m_VdoInptDvcFrmHeight; //存放视频输入设备帧的高度，单位为像素。
		int m_VdoInptDvcFrmIsCrop; //存放视频输入设备帧是否裁剪，为0表示不裁剪，为非0表示要裁剪。
		int m_VdoInptDvcFrmCropX; //存放视频输入设备帧裁剪区域左上角的横坐标，单位像素。
		int m_VdoInptDvcFrmCropY; //存放视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
		int m_VdoInptDvcFrmCropWidth; //存放视频输入设备帧裁剪区域的宽度，单位像素。
		int m_VdoInptDvcFrmCropHeight; //存放视频输入设备帧裁剪区域的高度，单位像素。
		int m_VdoInptDvcFrmRotate; //存放视频输入设备帧旋转的角度，只能为0、90、180、270，0度表示横屏，其他表示顺时针旋转。
		int m_VdoInptDvcFrmRotateWidth; //存放视频输入设备帧旋转后的宽度，单位为像素。
		int m_VdoInptDvcFrmRotateHeight; //存放视频输入设备帧旋转后的高度，单位为像素。
		int m_VdoInptDvcFrmIsScale; //存放视频输入设备帧是否缩放，为0表示不缩放，为非0表示要缩放。
		public int m_VdoInptDvcFrmScaleWidth; //存放视频输入帧缩放后的宽度，单位为像素。
		public int m_VdoInptDvcFrmScaleHeight; //存放视频输入帧缩放后的高度，单位为像素。
		int m_VdoInptIsBlack; //存放视频输入是否黑屏，为0表示有图像，为非0表示黑屏。

		public class VdoInptFrmElm //视频输入帧链表元素类。
		{
			VdoInptFrmElm()
			{
				m_YU12VdoInptFrmPt = ( m_VdoInptPt.m_IsUseVdoInpt != 0 ) ? new byte[ m_VdoInptPt.m_VdoInptDvcFrmScaleWidth * m_VdoInptPt.m_VdoInptDvcFrmScaleHeight * 3 / 2 ] : null;
				m_YU12VdoInptFrmWidthPt = ( m_VdoInptPt.m_IsUseVdoInpt != 0 ) ? new HTInt() : null;
				m_YU12VdoInptFrmHeightPt = ( m_VdoInptPt.m_IsUseVdoInpt != 0 ) ? new HTInt() : null;
				m_EncdVdoInptFrmPt = ( m_VdoInptPt.m_IsUseVdoInpt != 0 && m_VdoInptPt.m_UseWhatEncd != 0 ) ? new byte[ m_VdoInptPt.m_VdoInptDvcFrmScaleWidth * m_VdoInptPt.m_VdoInptDvcFrmScaleHeight * 3 / 2 ] : null;
				m_EncdVdoInptFrmLenPt = ( m_VdoInptPt.m_IsUseVdoInpt != 0 && m_VdoInptPt.m_UseWhatEncd != 0 ) ? new HTLong( 0 ) : null;
			}
			byte m_YU12VdoInptFrmPt[]; //存放YU12格式视频输入帧的指针。
			HTInt m_YU12VdoInptFrmWidthPt; //存放YU12格式视频输入帧的宽度。
			HTInt m_YU12VdoInptFrmHeightPt; //存放YU12格式视频输入帧的高度。
			byte m_EncdVdoInptFrmPt[]; //存放已编码格式视频输入帧。
			HTLong m_EncdVdoInptFrmLenPt; //存放已编码格式视频输入帧的长度，单位字节。
		}
		public LinkedList< byte[] > m_NV21VdoInptFrmLnkLstPt; //存放NV21格式视频输入帧链表的指针。
		public LinkedList< VdoInptFrmElm > m_VdoInptFrmLnkLstPt; //存放视频输入帧链表的指针。
		public LinkedList< VdoInptFrmElm > m_VdoInptIdleFrmLnkLstPt; //存放视频输入空闲帧链表的指针。

		//视频输入线程的临时变量。
		long m_LastVdoInptFrmTimeMsec; //存放上一个视频输入帧的时间，单位毫秒。
		long m_VdoInptFrmTimeStepMsec; //存放视频输入帧的时间步进，单位毫秒。
		byte m_VdoInptFrmPt[]; //存放视频输入帧的指针。
		byte m_VdoInptRsltFrmPt[]; //存放视频输入结果帧的指针。
		byte m_VdoInptTmpFrmPt[]; //存放视频输入临时帧的指针。
		byte m_VdoInptSwapFrmPt[]; //存放视频输入交换帧的指针。
		long m_VdoInptRsltFrmSz; //存放视频输入结果帧的内存大小，单位字节。
		HTLong m_VdoInptRsltFrmLenPt; //存放视频输入结果帧的长度，单位字节。
		VdoInptFrmElm m_VdoInptFrmElmPt; //存放视频输入帧元素的指针。
		int m_VdoInptFrmLnkLstElmTotal; //存放视频输入帧链表的元数总数。
		long m_LastTimeMsec; //存放上次时间的毫秒数。
		long m_NowTimeMsec; //存放本次时间的毫秒数。

		VdoInptThrd m_VdoInptThrdPt; //存放视频输入线程的指针。
		int m_VdoInptThrdExitFlag; //存放视频输入线程退出标记，0表示保持运行，1表示请求退出。
	}

	public VdoInpt m_VdoInptPt = new VdoInpt(); //存放视频输入的指针。

	public class VdoOtpt //视频输出类。
	{
		public int m_IsUseVdoOtpt; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。

		public int m_UseWhatDecd; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264解码器，为2表示系统自带H264解码器。

		OpenH264Decd m_OpenH264DecdPt; //存放OpenH264解码器的指针。
		int m_OpenH264DecdDecdThrdNum; //存放OpenH264解码器的解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。

		SystemH264Decd m_SystemH264DecdPt; //存放系统自带H264解码器的指针。

		HTSurfaceView m_VdoOtptDspySurfaceViewPt; //存放视频输出显示SurfaceView的指针。
		float m_VdoOtptDspyScale; //存放视频输出显示缩放倍数，为1.0f表示不缩放。
		int m_VdoOtptIsBlack; //存放视频输出是否黑屏，为0表示有图像，为非0表示黑屏。

		//视频输出线程的临时变量。
		byte m_VdoOtptRsltFrmPt[]; //存放视频输出结果帧的指针。
		byte m_VdoOtptTmpFrmPt[]; //存放视频输出临时帧的指针。
		byte m_VdoOtptSwapFrmPt[]; //存放视频输出交换帧的指针。
		HTLong m_VdoOtptRsltFrmLenPt; //存放视频输出结果帧的长度，单位字节。
		HTInt m_VdoOtptFrmWidthPt; //存放视频输出帧的宽度，单位为像素。
		HTInt m_VdoOtptFrmHeightPt; //存放视频输出帧的高度，单位为像素。
		long m_LastTimeMsec; //存放上次时间的毫秒数。
		long m_NowTimeMsec; //存放本次时间的毫秒数。

		VdoOtptThrd m_VdoOtptThrdPt; //存放视频输出线程的指针。
		int m_VdoOtptThrdExitFlag; //存放视频输出线程退出标记，0表示保持运行，1表示请求退出。
	}

	public VdoOtpt m_VdoOtptPt = new VdoOtpt(); //存放视频输出的指针。

	//媒体处理线程的临时变量。
	short m_PcmAdoInptFrmPt[] = null; //存放PCM格式音频输入帧的指针。
	short m_PcmAdoOtptFrmPt[] = null; //存放PCM格式音频输出帧的指针。
	short m_PcmAdoRsltFrmPt[] = null; //存放PCM格式音频结果帧的指针。
	short m_PcmAdoTmpFrmPt[] = null; //存放PCM格式音频临时帧的指针。
	short m_PcmAdoSwapFrmPt[] = null; //存放PCM格式音频交换帧的指针。
	HTInt m_VoiceActStsPt = null; //存放语音活动状态，为1表示有语音活动，为0表示无语音活动。
	byte m_EncdAdoInptFrmPt[] = null; //存放已编码格式音频输入帧的指针。
	HTLong m_EncdAdoInptFrmLenPt = null; //存放已编码格式音频输入帧长度的指针，单位字节。
	HTInt m_EncdAdoInptFrmIsNeedTransPt = null; //存放已编码格式音频输入帧是否需要传输的指针，为1表示需要传输，为0表示不需要传输。
	VdoInpt.VdoInptFrmElm m_VdoInptFrmPt = null; //存放视频输入帧的指针。

	public VarStr m_ErrInfoVarStrPt; //存放错误信息动态字符串的指针。

	//用户定义的相关回调函数。

	//用户定义的初始化函数。
	public abstract int UserInit();

	//用户定义的处理函数。
	public abstract int UserPocs();

	//用户定义的销毁函数。
	public abstract void UserDstoy();

	//用户定义的读取音视频输入帧函数。
	public abstract int UserReadAdoVdoInptFrm( short PcmAdoInptFrmPt[], short PcmAdoRsltFrmPt[], HTInt VoiceActStsPt, byte EncdAdoInptFrmPt[], HTLong EncdAdoInptFrmLenPt, HTInt EncdAdoInptFrmIsNeedTransPt,
											   byte YU12VdoInptFrmPt[], HTInt YU12VdoInptFrmWidthPt, HTInt YU12VdoInptFrmHeightPt, byte EncdVdoInptFrmPt[], HTLong EncdVdoInptFrmLenPt );

	//用户定义的写入音频输出帧函数。
	public abstract void UserWriteAdoOtptFrm( short PcmAdoOtptFrmPt[], byte EncdAdoOtptFrmPt[], HTLong AdoOtptFrmLenPt );

	//用户定义的获取PCM格式音频输出帧函数。
	public abstract void UserGetPcmAdoOtptFrm( short PcmAdoOtptFrmPt[], long PcmAdoOtptFrmLen );

	//用户定义的写入视频输出帧函数。
	public abstract void UserWriteVdoOtptFrm( byte YU12VdoOtptFrmPt[], HTInt YU12VdoInptFrmWidthPt, HTInt YU12VdoInptFrmHeightPt, byte EncdVdoOtptFrmPt[], HTLong VdoOtptFrmLenPt );

	//用户定义的获取YU12格式视频输出帧函数。
	public abstract void UserGetYU12VdoOtptFrm( byte YU12VdoOtptFrmPt[], int YU12VdoOtptFrmWidth, int YU12VdoOtptFrmHeight );

	//构造函数。
	public MediaPocsThrd( Context AppCntxtPt )
	{
		m_AppCntxtPt = AppCntxtPt; //设置应用程序上下文的指针。
	}

	//设置是否保存设置到文件。
	public void SetIsSaveStngToFile( int IsSaveStngToFile, String StngFileFullPathStrPt )
	{
		m_IsSaveStngToFile = IsSaveStngToFile;
		m_StngFileFullPathStrPt = StngFileFullPathStrPt;
	}

	//设置是否打印Logcat日志、显示Toast。
	public void SetIsPrintLogcatShowToast( int IsPrintLogcat, int IsShowToast, Activity ShowToastActivityPt )
	{
		if( ( IsShowToast != 0 ) && ( ShowToastActivityPt == null ) ) //如果显示Toast界面的指针不正确。
		{
			return;
		}

		m_IsPrintLogcat = IsPrintLogcat;
		m_IsShowToast = IsShowToast;
		m_ShowToastActivityPt = ShowToastActivityPt;
	}

	//设置是否使用唤醒锁。
	public void SetIsUseWakeLock( int IsUseWakeLock )
	{
		m_IsUseWakeLock = IsUseWakeLock;

		if( m_RunFlag == RUN_FLAG_INIT || m_RunFlag == RUN_FLAG_POCS ) //如果本线程为刚开始运行正在初始化或初始化完毕正在循环处理帧，就立即修改唤醒锁。
		{
			WakeLockInitOrDstoy( IsUseWakeLock );
		}
	}

	//初始化或销毁唤醒锁。
	void WakeLockInitOrDstoy( int IsInitOrDstoy )
	{
		if( IsInitOrDstoy != 0 ) //如果要初始化唤醒锁。
		{
			if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 && m_AdoOtptPt.m_UseWhatAdoOtptDvc != 0 ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
			{
				if( m_ProximityScreenOffWakeLockPt == null ) //如果接近息屏唤醒锁还没有初始化。
				{
					m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) m_AppCntxtPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
					if( m_ProximityScreenOffWakeLockPt != null )
					{
						m_ProximityScreenOffWakeLockPt.acquire();
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化接近息屏唤醒锁成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化接近息屏唤醒锁失败。" );
					}
				}
			}
			else //如果不使用音频输出，或不使用听筒音频输出设备，就不使用接近息屏唤醒锁。
			{
				if( m_ProximityScreenOffWakeLockPt != null )
				{
					try
					{
						m_ProximityScreenOffWakeLockPt.release();
					}
					catch( RuntimeException ignored )
					{
					}
					m_ProximityScreenOffWakeLockPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁接近息屏唤醒锁成功。" );
				}
			}

			if( m_FullWakeLockPt == null ) //如果屏幕键盘全亮唤醒锁还没有初始化。
			{
				m_FullWakeLockPt = ( ( PowerManager ) m_AppCntxtPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
				if( m_FullWakeLockPt != null )
				{
					m_FullWakeLockPt.acquire();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化屏幕键盘全亮唤醒锁成功。" );
				}
				else
				{
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化屏幕键盘全亮唤醒锁失败。" );
				}
			}
		}
		else //如果要销毁唤醒锁。
		{
			//销毁唤醒锁。
			if( m_ProximityScreenOffWakeLockPt != null )
			{
				try
				{
					m_ProximityScreenOffWakeLockPt.release();
				}
				catch( RuntimeException ignored )
				{
				}
				m_ProximityScreenOffWakeLockPt = null;
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁接近息屏唤醒锁成功。" );
			}
			if( m_FullWakeLockPt != null )
			{
				try
				{
					m_FullWakeLockPt.release();
				}
				catch( RuntimeException ignored )
				{
				}
				m_FullWakeLockPt = null;
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁屏幕键盘全亮唤醒锁成功。" );
			}
		}
	}

	//设置是否使用音频输入。
	public void SetIsUseAdoInpt( int IsUseAdoInpt, int SmplRate, int FrmLenMsec )
	{
		if( ( ( IsUseAdoInpt != 0 ) && ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) ) || //如果采样频率不正确。
				( ( IsUseAdoInpt != 0 ) && ( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) ) //如果帧的毫秒长度不正确。
		{
			return;
		}

		m_AdoInptPt.m_IsUseAdoInpt = IsUseAdoInpt;
		m_AdoInptPt.m_SmplRate = SmplRate;
		m_AdoInptPt.m_FrmLen = FrmLenMsec * SmplRate / 1000;
	}

	//设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
	public void SetAdoInptIsUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
	{
		m_AdoInptPt.m_IsUseSystemAecNsAgc = IsUseSystemAecNsAgc;
	}

	//设置音频输入不使用声学回音消除器。
	public void SetAdoInptUseNoAec()
	{
		m_AdoInptPt.m_UseWhatAec = 0;
	}

	//设置音频输入要使用Speex声学回音消除器。
	public void SetAdoInptUseSpeexAec( int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesActive, int IsSaveMemFile, String MemFileFullPathStrPt )
	{
		m_AdoInptPt.m_UseWhatAec = 1;
		m_AdoInptPt.m_SpeexAecFilterLen = FilterLen;
		m_AdoInptPt.m_SpeexAecIsUseRec = IsUseRec;
		m_AdoInptPt.m_SpeexAecEchoMultiple = EchoMultiple;
		m_AdoInptPt.m_SpeexAecEchoCont = EchoCont;
		m_AdoInptPt.m_SpeexAecEchoSupes = EchoSupes;
		m_AdoInptPt.m_SpeexAecEchoSupesAct = EchoSupesActive;
		m_AdoInptPt.m_SpeexAecIsSaveMemFile = IsSaveMemFile;
		m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt = MemFileFullPathStrPt;
	}

	//设置音频输入要使用WebRtc定点版声学回音消除器。
	public void SetAdoInptUseWebRtcAecm( int IsUseCNGMode, int EchoMode, int Delay )
	{
		m_AdoInptPt.m_UseWhatAec = 2;
		m_AdoInptPt.m_WebRtcAecmIsUseCNGMode = IsUseCNGMode;
		m_AdoInptPt.m_WebRtcAecmEchoMode = EchoMode;
		m_AdoInptPt.m_WebRtcAecmDelay = Delay;
	}

	//设置音频输入要使用WebRtc浮点版声学回音消除器。
	public void SetAdoInptUseWebRtcAec( int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
	{
		m_AdoInptPt.m_UseWhatAec = 3;
		m_AdoInptPt.m_WebRtcAecEchoMode = EchoMode;
		m_AdoInptPt.m_WebRtcAecDelay = Delay;
		m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode = IsUseDelayAgstcMode;
		m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode = IsUseExtdFilterMode;
		m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = IsUseRefinedFilterAdaptAecMode;
		m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay = IsUseAdaptAdjDelay;
		m_AdoInptPt.m_WebRtcAecIsSaveMemFile = IsSaveMemFile;
		m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt = MemFileFullPathStrPt;
	}

	//设置音频输入要使用SpeexWebRtc三重声学回音消除器。
	public void SetAdoInptUseSpeexWebRtcAec( int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSuppress, int SpeexAecEchoSuppressActive, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
	{
		m_AdoInptPt.m_UseWhatAec = 4;
		m_AdoInptPt.m_SpeexWebRtcAecWorkMode = WorkMode;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen = SpeexAecFilterLen;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec = SpeexAecIsUseRec;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMultiple = SpeexAecEchoMultiple;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCont = SpeexAecEchoCont;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes = SpeexAecEchoSuppress;
		m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct = SpeexAecEchoSuppressActive;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode = WebRtcAecmIsUseCNGMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode = WebRtcAecmEchoMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay = WebRtcAecmDelay;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode = WebRtcAecEchoMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay = WebRtcAecDelay;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode = WebRtcAecIsUseDelayAgstcMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode = WebRtcAecIsUseExtdFilterMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode = WebRtcAecIsUseRefinedFilterAdaptAecMode;
		m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay = WebRtcAecIsUseAdaptAdjDelay;
		m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec = IsUseSameRoomAec;
		m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay = SameRoomEchoMinDelay;
	}

	//设置音频输入不使用噪音抑制器。
	public void SetAdoInptUseNoNs()
	{
		m_AdoInptPt.m_UseWhatNs = 0;
	}

	//设置音频输入要使用Speex预处理器的噪音抑制。
	public void SetAdoInptUseSpeexPrpocsNs( int IsUseNs, int NoiseSupes, int IsUseDereverberation )
	{
		m_AdoInptPt.m_UseWhatNs = 1;
		m_AdoInptPt.m_SpeexPrpocsIsUseNs = IsUseNs;
		m_AdoInptPt.m_SpeexPrpocsNoiseSupes = NoiseSupes;
		m_AdoInptPt.m_SpeexPrpocsIsUseDereverb = IsUseDereverberation;
	}

	//设置音频输入要使用WebRtc定点版噪音抑制器。
	public void SetAdoInptUseWebRtcNsx( int PolicyMode )
	{
		m_AdoInptPt.m_UseWhatNs = 2;
		m_AdoInptPt.m_WebRtcNsxPolicyMode = PolicyMode;
	}

	//设置音频输入要使用WebRtc浮点版噪音抑制器。
	public void SetAdoInptUseWebRtcNs( int PolicyMode )
	{
		m_AdoInptPt.m_UseWhatNs = 3;
		m_AdoInptPt.m_WebRtcNsPolicyMode = PolicyMode;
	}

	//设置音频输入要使用RNNoise噪音抑制器。
	public void SetAdoInptUseRNNoise()
	{
		m_AdoInptPt.m_UseWhatNs = 4;
	}

	//设置音频输入是否使用Speex预处理器的其他功能。
	public void SetAdoInptIsUseSpeexPrpocsOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
	{
		m_AdoInptPt.m_IsUseSpeexPrpocsOther = IsUseOther;
		m_AdoInptPt.m_SpeexPrpocsIsUseVad = IsUseVad;
		m_AdoInptPt.m_SpeexPrpocsVadProbStart = VadProbStart;
		m_AdoInptPt.m_SpeexPrpocsVadProbCont = VadProbCont;
		m_AdoInptPt.m_SpeexPrpocsIsUseAgc = IsUseAgc;
		m_AdoInptPt.m_SpeexPrpocsAgcIncrement = AgcIncrement;
		m_AdoInptPt.m_SpeexPrpocsAgcDecrement = AgcDecrement;
		m_AdoInptPt.m_SpeexPrpocsAgcLevel = AgcLevel;
		m_AdoInptPt.m_SpeexPrpocsAgcMaxGain = AgcMaxGain;
	}

	//设置音频输入要使用PCM原始数据。
	public void SetAdoInptUsePcm()
	{
		m_AdoInptPt.m_UseWhatEncd = 0;
	}

	//设置音频输入要使用Speex编码器。
	public void SetAdoInptUseSpeexEncd( int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate )
	{
		m_AdoInptPt.m_UseWhatEncd = 1;
		m_AdoInptPt.m_SpeexEncdUseCbrOrVbr = UseCbrOrVbr;
		m_AdoInptPt.m_SpeexEncdQualt = Qualt;
		m_AdoInptPt.m_SpeexEncdCmplxt = Cmplxt;
		m_AdoInptPt.m_SpeexEncdPlcExptLossRate = PlcExptLossRate;
	}

	//设置音频输入要使用Opus编码器。
	public void SetAdoInptUseOpusEncd()
	{
		m_AdoInptPt.m_UseWhatEncd = 2;
	}

	//设置音频输入是否保存音频到文件。
	public void SetAdoInptIsSaveAdoToFile( int IsSaveAdoToFile, String AdoInptFileFullPathStrPt, String AdoRsltFileFullPathStrPt )
	{
		m_AdoInptPt.m_IsSaveAdoToFile = IsSaveAdoToFile;
		m_AdoInptPt.m_AdoInptFileFullPathStrPt = AdoInptFileFullPathStrPt;
		m_AdoInptPt.m_AdoRsltFileFullPathStrPt = AdoRsltFileFullPathStrPt;
	}

	//设置音频输入是否绘制音频波形到Surface。
	public void SetAdoInptIsDrawAdoWavfmToSurface( int IsDrawAdoWavfmToSurface, SurfaceView AdoInptOscilloSurfacePt, SurfaceView AdoRsltOscilloSurfacePt )
	{
		m_AdoInptPt.m_IsDrawAdoWavfmToSurface = IsDrawAdoWavfmToSurface;
		m_AdoInptPt.m_AdoInptOscilloSurfacePt = AdoInptOscilloSurfacePt;
		m_AdoInptPt.m_AdoRsltOscilloSurfacePt = AdoRsltOscilloSurfacePt;
	}

	//设置音频输入是否静音。
	public void SetAdoInptIsMute( int IsMute )
	{
		m_AdoInptPt.m_AdoInptIsMute = IsMute;
	}

	//设置是否使用音频输出。
	public void SetIsUseAdoOtpt( int IsUseAdoOtpt, int SmplRate, int FrmLenMsec )
	{
		if( ( ( IsUseAdoOtpt != 0 ) && ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) ) || //如果采样频率不正确。
				( ( IsUseAdoOtpt != 0 ) && ( ( FrmLenMsec == 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) ) //如果帧的毫秒长度不正确。
		{
			return;
		}

		m_AdoOtptPt.m_IsUseAdoOtpt = IsUseAdoOtpt;
		m_AdoOtptPt.m_SmplRate = SmplRate;
		m_AdoOtptPt.m_FrmLen = FrmLenMsec * SmplRate / 1000;
	}

	//设置音频输出要使用PCM原始数据。
	public void SetAdoOtptUsePcm()
	{
		m_AdoOtptPt.m_UseWhatDecd = 0;
	}

	//设置音频输出要使用Speex解码器。
	public void SetAdoOtptUseSpeexDecd( int IsUsePrcplEnhsmt )
	{
		m_AdoOtptPt.m_UseWhatDecd = 1;
		m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt = IsUsePrcplEnhsmt;
	}

	//设置音频输出要使用Opus编码器。
	public void SetAdoOtptUseOpusDecd()
	{
		m_AdoOtptPt.m_UseWhatDecd = 2;
	}

	//设置音频输出是否保存音频到文件。
	public void SetAdoOtptIsSaveAdoToFile( int IsSaveAdoToFile, String AdoOtptFileFullPathStrPt )
	{
		m_AdoOtptPt.m_IsSaveAdoToFile = IsSaveAdoToFile;
		m_AdoOtptPt.m_AdoOtptFileFullPathStrPt = AdoOtptFileFullPathStrPt;
	}

	//设置音频输出是否绘制音频波形到Surface。
	public void SetAdoOtptIsDrawAdoWavfmToSurface( int IsDrawAudioToSurface, SurfaceView AdoOtptOscilloSurfacePt )
	{
		m_AdoOtptPt.m_IsDrawAdoWavfmToSurface = IsDrawAudioToSurface;
		m_AdoOtptPt.m_AdoOtptOscilloSurfacePt = AdoOtptOscilloSurfacePt;
	}

	//设置音频输出使用的设备。
	public void SetAdoOtptUseDvc( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
	{
		if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) )//如果使用听筒，则不能使用媒体类型音频输出流。
		{
			return;
		}

		m_AdoOtptPt.m_UseWhatAdoOtptDvc = UseSpeakerOrEarpiece;
		m_AdoOtptPt.m_UseWhatAdoOtptStreamType = UseVoiceCallOrMusic;
		SetIsUseWakeLock( m_IsUseWakeLock ); //重新初始化唤醒锁。
	}

	//设置音频输出是否静音。
	public void SetAdoOtptIsMute( int IsMute )
	{
		m_AdoOtptPt.m_AdoOtptIsMute = IsMute; //设置音频输出是否静音。
	}

	//设置是否使用视频输入。
	public void SetIsUseVdoInpt( int IsUseVdoInpt, int MaxSmplRate, int FrmWidth, int FrmHeight, int ScreenRotate, HTSurfaceView VdoInptPrvwSurfaceViewPt )
	{
		if( ( ( IsUseVdoInpt != 0 ) && ( ( MaxSmplRate < 1 ) || ( MaxSmplRate > 60 ) ) ) || //如果采样频率不正确。
				( ( IsUseVdoInpt != 0 ) && ( ( FrmWidth <= 0 ) || ( ( FrmWidth & 1 ) != 0 ) ) ) || //如果帧的宽度不正确。
				( ( IsUseVdoInpt != 0 ) && ( ( FrmHeight <= 0 ) || ( ( FrmHeight & 1 ) != 0 ) ) ) || //如果帧的高度不正确。
				( ( IsUseVdoInpt != 0 ) && ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) || //如果屏幕旋转的角度不正确。
				( ( IsUseVdoInpt != 0 ) && ( VdoInptPrvwSurfaceViewPt == null ) ) ) //如果视频预览SurfaceView的指针不正确。
		{
			return;
		}

		m_VdoInptPt.m_IsUseVdoInpt = IsUseVdoInpt;
		m_VdoInptPt.m_MaxSmplRate = MaxSmplRate;
		m_VdoInptPt.m_FrmWidth = FrmWidth;
		m_VdoInptPt.m_FrmHeight = FrmHeight;
		m_VdoInptPt.m_ScreenRotate = ScreenRotate;
		m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt = VdoInptPrvwSurfaceViewPt;
	}

	//设置视频输入要使用YU12原始数据。
	public void SetVdoInptUseYU12()
	{
		m_VdoInptPt.m_UseWhatEncd = 0;
	}

	//设置视频输入要使用OpenH264编码器。
	public void SetVdoInptUseOpenH264Encd( int VdoType, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvl, int Cmplxt )
	{
		m_VdoInptPt.m_UseWhatEncd = 1;
		m_VdoInptPt.m_OpenH264EncdVdoType = VdoType;
		m_VdoInptPt.m_OpenH264EncdEncdBitrate = EncdBitrate;
		m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode = BitrateCtrlMode;
		m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl = IDRFrmIntvl;
		m_VdoInptPt.m_OpenH264EncdCmplxt = Cmplxt;
	}

	//设置视频输入要使用系统自带H264编码器。
	public void SetVdoInptUseSystemH264Encd( int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvlTimeSec, int Cmplxt )
	{
		m_VdoInptPt.m_UseWhatEncd = 2;
		m_VdoInptPt.m_SystemH264EncdEncdBitrate = EncdBitrate;
		m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode = BitrateCtrlMode;
		m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec = IDRFrmIntvlTimeSec;
		m_VdoInptPt.m_SystemH264EncdCmplxt = Cmplxt;
	}

	//设置视频输入使用的设备。
	public void SetVdoInptUseDvc( int UseFrontOrBack, int FrontCameraDvcId, int BackCameraDvcId )
	{
		if( ( ( UseFrontOrBack != 0 ) && ( UseFrontOrBack != 1 ) ) ||
				( FrontCameraDvcId < -1 ) ||
				( BackCameraDvcId < -1 ) )
		{
			return;
		}

		m_VdoInptPt.m_UseWhatVdoInptDvc = UseFrontOrBack; //设置视频输入设备。
		m_VdoInptPt.m_FrontCameraDvcId = FrontCameraDvcId; //设置视频输入前置摄像头的设备ID。
		m_VdoInptPt.m_BackCameraDvcId = BackCameraDvcId; //设置视频输入后置摄像头的设备ID。
	}

	//设置视频输入是否黑屏。
	public void SetVdoInptIsBlack( int IsBlack )
	{
		m_VdoInptPt.m_VdoInptIsBlack = IsBlack;
	}

	//设置是否使用视频输出。
	public void SetIsUseVdoOtpt( int IsUseVdoOtpt, HTSurfaceView VdoOtptDspySurfaceViewPt, float VdoOtptDspyScale )
	{
		if( ( ( IsUseVdoOtpt != 0 ) && ( VdoOtptDspySurfaceViewPt == null ) ) || //如果视频显示SurfaceView的指针不正确。
				( ( IsUseVdoOtpt != 0 ) && ( VdoOtptDspyScale <= 0 ) ) ) //如果视频输出显示缩放倍数不正确。
		{
			return;
		}

		m_VdoOtptPt.m_IsUseVdoOtpt = IsUseVdoOtpt;
		m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt = VdoOtptDspySurfaceViewPt;
		m_VdoOtptPt.m_VdoOtptDspyScale = VdoOtptDspyScale;
	}

	//设置视频输出要使用YU12原始数据。
	public void SetVdoOtptUseYU12()
	{
		m_VdoOtptPt.m_UseWhatDecd = 0;
	}

	//设置视频输出要使用OpenH264解码器。
	public void SetVdoOtptUseOpenH264Decd( int DecdThrdNum )
	{
		m_VdoOtptPt.m_UseWhatDecd = 1;
		m_VdoOtptPt.m_OpenH264DecdDecdThrdNum = DecdThrdNum;
	}

	//设置视频输出要使用系统自带H264解码器。
	public void SetVdoOtptUseSystemH264Decd()
	{
		m_VdoOtptPt.m_UseWhatDecd = 2;
	}

	//设置视频输出是否黑屏。
	public void SetVdoOtptIsBlack( int IsBlack )
	{
		m_VdoOtptPt.m_VdoOtptIsBlack = IsBlack;
	}

	//请求权限。
	public static void RqstPrmsn( Activity RqstActivity, int IsRqstInternet, int IsRqstModifyAudioStng, int IsRqstForegroundService, int IsRqstWakeLock, int IsRqstRecordAdo, int IsRqstCamera, int DeniedIsPrintLogcat, int DeniedIsShowToast )
	{
		String p_DeniedPermissionStrPt = "拒绝的权限：";
		int p_DeniedPermissionNum = 0;
		ArrayList<String> p_RqstPermissionStrArrPt = new ArrayList<String>();

		//检测网络权限。
		if( ( IsRqstInternet != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.INTERNET );
			p_DeniedPermissionStrPt += "网络  ";
			p_DeniedPermissionNum++;
		}

		//检测修改音频设置权限。
		if( ( IsRqstModifyAudioStng != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.MODIFY_AUDIO_SETTINGS ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.MODIFY_AUDIO_SETTINGS );
			p_DeniedPermissionStrPt += "修改音频设置  ";
			p_DeniedPermissionNum++;
		}

		//检测前台服务权限。
		if( ( IsRqstForegroundService != 0 ) && ( android.os.Build.VERSION.SDK_INT >= 28 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.FOREGROUND_SERVICE ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.FOREGROUND_SERVICE );
			p_DeniedPermissionStrPt += "前台服务  ";
			p_DeniedPermissionNum++;
		}

		//检测唤醒锁权限。
		if( ( IsRqstWakeLock != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.WAKE_LOCK ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.WAKE_LOCK );
			p_DeniedPermissionStrPt += "唤醒锁  ";
			p_DeniedPermissionNum++;
		}

		//检测录音权限。
		if( ( IsRqstRecordAdo != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.RECORD_AUDIO );
			p_DeniedPermissionStrPt += "录音  ";
			p_DeniedPermissionNum++;
		}

		//检测摄像头权限。
		if( ( IsRqstCamera != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.CAMERA );
			p_DeniedPermissionStrPt += "摄像头  ";
			p_DeniedPermissionNum++;
		}

		if( p_DeniedPermissionNum > 0 ) //有拒绝的权限。
		{
			//请求权限。
			if( !p_RqstPermissionStrArrPt.isEmpty() )
			{
				ActivityCompat.requestPermissions( RqstActivity, p_RqstPermissionStrArrPt.toArray( new String[p_RqstPermissionStrArrPt.size()] ), 1 );
			}

			//打印日志。
			if (DeniedIsPrintLogcat != 0)
			{
				Log.i(m_CurClsNameStrPt, p_DeniedPermissionStrPt);
			}

			//打印Toast。
			if (DeniedIsShowToast != 0)
			{
				Toast.makeText(RqstActivity, p_DeniedPermissionStrPt, Toast.LENGTH_LONG).show();
			}
		}
	}

	//请求本线程退出。
	public int RqirExit( int ExitFlag, int IsBlockWait )
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			//判断各个变量是否正确。
			if( ( ExitFlag < 0 ) || ( ExitFlag > 3 ) ) //如果退出标记不正确。
			{
				break Out;
			}

			m_ExitFlag = ExitFlag; //设置媒体处理线程的退出标记。

			if( IsBlockWait != 0 ) //如果需要阻塞等待。
			{
				if( ExitFlag == 1 ) //如果是请求退出。
				{
					do
					{
						if( this.isAlive() != true ) //如果媒体处理线程已经退出。
						{
							break;
						}

						SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
					}while( true );
				}
				else //如果是请求重启。
				{
					//等待重启完毕。
					do
					{
						if( this.isAlive() != true ) //如果媒体处理线程已经退出。
						{
							break;
						}
						if( m_ExitFlag == 0 ) //如果退出标记为0保持运行，表示重启完毕。
						{
							break;
						}

						SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
					}
					while( true );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		return p_Rslt;
	}

	//音频输入线程类。
	private class AdoInptThrd extends Thread
	{
		public void run()
		{
			this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
			Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

			if( m_AdoInptPt.m_UseWhatAec != 0 ) //如果要使用音频输入的声学回音消除，就自适应计算声学回音的延迟，并设置到声学回音消除器。放在音频输入线程中计算，可以减少媒体处理线程的初始化时间。
			{
				int p_Delay = 0; //存放声学回音的延迟，单位毫秒。
				HTInt p_HTIntDelay = new HTInt();

				//计算音频输出的延迟。
				m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
				m_AdoInptPt.m_AdoInptFrmPt = new short[ m_AdoOtptPt.m_FrmLen ]; //创建一个空的音频输出帧。
				m_AdoInptPt.m_LastTimeMsec = System.currentTimeMillis();
				while( true )
				{
					m_AdoOtptPt.m_AdoOtptDvcPt.write( m_AdoInptPt.m_AdoInptFrmPt, 0, m_AdoInptPt.m_AdoInptFrmPt.length ); //播放一个空的音频输出帧。
					m_AdoInptPt.m_NowTimeMsec = System.currentTimeMillis();
					p_Delay += m_AdoOtptPt.m_FrmLen; //递增音频输出的延迟。
					if( m_AdoInptPt.m_NowTimeMsec - m_AdoInptPt.m_LastTimeMsec >= 10 ) //如果播放耗时较长，就表示音频输出设备的缓冲区已经写满，结束计算。
					{
						break;
					}
					m_AdoInptPt.m_LastTimeMsec = m_AdoInptPt.m_NowTimeMsec;
				}
				p_Delay = p_Delay * 1000 / m_AdoOtptPt.m_SmplRate; //将音频输出的延迟转换为毫秒。
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟：" + p_Delay + " 毫秒。" );

				//计算音频输入的延迟。
				m_AdoInptPt.m_AdoInptDvcPt.startRecording(); //让音频输入设备开始录音。
				m_AdoInptPt.m_AdoInptFrmPt = new short[ m_AdoInptPt.m_FrmLen ]; //创建一个空的音频输入帧。
				m_AdoInptPt.m_LastTimeMsec = System.currentTimeMillis();
				m_AdoInptPt.m_AdoInptDvcPt.read( m_AdoInptPt.m_AdoInptFrmPt, 0, m_AdoInptPt.m_AdoInptFrmPt.length ); //计算读取一个音频输入帧的耗时。
				m_AdoInptPt.m_NowTimeMsec = System.currentTimeMillis();
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟：" + ( m_AdoInptPt.m_NowTimeMsec - m_AdoInptPt.m_LastTimeMsec ) + " 毫秒。" );

				m_AdoOtptPt.m_AdoOtptThrdPt.start(); //启动音频输出线程。

				//计算声学回音的延迟。
				p_Delay = p_Delay + ( int ) ( m_AdoInptPt.m_NowTimeMsec - m_AdoInptPt.m_LastTimeMsec );
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "声学回音延迟：" + p_Delay + " 毫秒，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

				//设置到WebRtc定点版和浮点版声学回音消除器。
				if( ( m_AdoInptPt.m_WebRtcAecmPt != null ) && ( m_AdoInptPt.m_WebRtcAecmPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc定点版声学回音消除器，且需要自适应设置回音的延迟。
				{
					m_AdoInptPt.m_WebRtcAecmPt.SetDelay( p_Delay / 2 );
					m_AdoInptPt.m_WebRtcAecmPt.GetDelay( p_HTIntDelay );
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_AdoInptPt.m_WebRtcAecPt != null ) && ( m_AdoInptPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc浮点版声学回音消除器，且需要自适应设置回音的延迟。
				{
					if( m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode == 0 ) //如果WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
					{
						m_AdoInptPt.m_WebRtcAecPt.SetDelay( p_Delay );
						m_AdoInptPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay );
					}
					else //如果WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
					{
						m_AdoInptPt.m_WebRtcAecPt.SetDelay( 20 );
						m_AdoInptPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay );
					}
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_AdoInptPt.m_SpeexWebRtcAecPt != null ) && ( m_AdoInptPt.m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc定点版声学回音消除器需要自适应设置回音的延迟。
				{
					m_AdoInptPt.m_SpeexWebRtcAecPt.SetWebRtcAecmDelay( p_Delay / 2 );
					m_AdoInptPt.m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay );
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_AdoInptPt.m_SpeexWebRtcAecPt != null ) && ( m_AdoInptPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
				{
					if( m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
					{
						m_AdoInptPt.m_SpeexWebRtcAecPt.SetWebRtcAecDelay( p_Delay );
						m_AdoInptPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
					}
					else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
					{
						m_AdoInptPt.m_SpeexWebRtcAecPt.SetWebRtcAecDelay( 20 );
						m_AdoInptPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
					}
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
			}
			else //如果不使用音频输入的声学回音消除，就直接启动音频输出线程。
			{
				m_AdoInptPt.m_AdoInptDvcPt.startRecording(); //让音频输入设备开始录音。
				if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
				{
					m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
					m_AdoOtptPt.m_AdoOtptThrdPt.start(); //启动音频输出线程。
				}
			}

			//开始音频输入循环。
			OutAdoInptLoop:
			while( true )
			{
				//获取一个音频输入空闲帧。
				if( ( m_AdoInptPt.m_AdoInptFrmLnkLstElmTotal = m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt.size() ) > 0 ) //如果音频输入空闲帧链表中有音频输入空闲帧。
				{
					//从音频输入空闲帧链表中取出第一个音频输入空闲帧。
					synchronized( m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt )
					{
						m_AdoInptPt.m_AdoInptFrmPt = m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt.getFirst();
						m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt.removeFirst();
					}
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：从音频输入空闲帧链表中取出第一个音频输入空闲帧，音频输入空闲帧链表元素个数：" + m_AdoInptPt.m_AdoInptFrmLnkLstElmTotal + "。" );
				}
				else //如果音频输入空闲帧链表中没有音频输入空闲帧。
				{
					if( ( m_AdoInptPt.m_AdoInptFrmLnkLstElmTotal = m_AdoInptPt.m_AdoInptFrmLnkLstPt.size() ) <= 50 )
					{
						m_AdoInptPt.m_AdoInptFrmPt = new short[m_AdoInptPt.m_FrmLen]; //创建一个音频输入空闲帧。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：音频输入空闲帧链表中没有音频输入空闲帧，创建一个音频输入空闲帧。" );
					}
					else
					{
						m_AdoInptPt.m_AdoInptFrmPt = null;
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输入线程：音频输入帧链表中音频输入帧数量为" + m_AdoInptPt.m_AdoInptFrmLnkLstElmTotal + "已经超过上限50，不再创建一个音频输入空闲帧。" );
					}
				}

				if( m_AdoInptPt.m_AdoInptFrmPt != null ) //如果获取了一个音频输入空闲帧。
				{
					if( m_IsPrintLogcat != 0 ) m_AdoInptPt.m_LastTimeMsec = System.currentTimeMillis();

					//读取本次音频输入帧。
					m_AdoInptPt.m_AdoInptDvcPt.read( m_AdoInptPt.m_AdoInptFrmPt, 0, m_AdoInptPt.m_AdoInptFrmPt.length );

					//追加本次音频输入帧到音频输入帧链表。
					synchronized( m_AdoInptPt.m_AdoInptFrmLnkLstPt )
					{
						m_AdoInptPt.m_AdoInptFrmLnkLstPt.addLast( m_AdoInptPt.m_AdoInptFrmPt );
					}

					if( m_IsPrintLogcat != 0 )
					{
						m_AdoInptPt.m_NowTimeMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "音频输入线程：本次音频输入帧读取完毕，耗时 " + ( m_AdoInptPt.m_NowTimeMsec - m_AdoInptPt.m_LastTimeMsec ) + " 毫秒。" );
					}
				}

				if( m_AdoInptPt.m_AdoInptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
					break OutAdoInptLoop;
				}
			} //音频输入循环完毕。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程已退出。" );
		}
	}

	//音频输出线程类。
	private class AdoOtptThrd extends Thread
	{
		public void run()
		{
			this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
			Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );

			//开始音频输出循环。
			OutAdoOtptLoop:
			while( true )
			{
				//获取一个音频输出空闲帧。
				if( ( m_AdoOtptPt.m_AdoOtptFrmLnkLstElmTotal = m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt.size() ) > 0 ) //如果音频输出空闲帧链表中有音频输出空闲帧。
				{
					//从音频输出空闲帧链表中取出第一个音频输出空闲帧。
					synchronized( m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt )
					{
						m_AdoOtptPt.m_AdoOtptFrmPt = m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt.getFirst();
						m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt.removeFirst();
					}
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：从音频输出空闲帧链表中取出第一个音频输出空闲帧，音频输出空闲帧链表元素个数：" + m_AdoOtptPt.m_AdoOtptFrmLnkLstElmTotal + "。" );
				}
				else //如果音频输出空闲帧链表中没有音频输出空闲帧。
				{
					if( ( m_AdoOtptPt.m_AdoOtptFrmLnkLstElmTotal = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size() ) <= 50 )
					{
						m_AdoOtptPt.m_AdoOtptFrmPt = new short[m_AdoOtptPt.m_FrmLen]; //创建一个音频输出空闲帧。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：音频输出空闲帧链表中没有音频输出空闲帧，创建一个音频输出空闲帧。" );
					}
					else
					{
						m_AdoOtptPt.m_AdoOtptFrmPt = null;
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：音频输出帧链表中音频输出帧数量为" + m_AdoOtptPt.m_AdoOtptFrmLnkLstElmTotal + "已经超过上限50，不再创建一个音频输出空闲帧。" );
					}
				}

				if( m_AdoOtptPt.m_AdoOtptFrmPt != null ) //如果获取了一个音频输出空闲帧。
				{
					if( m_IsPrintLogcat != 0 ) m_AdoOtptPt.m_LastTimeMsec = System.currentTimeMillis();

					//调用用户定义的写入音频输出帧函数，并解码成PCM原始数据。
					switch( m_AdoOtptPt.m_UseWhatDecd ) //使用什么解码器。
					{
						case 0: //如果要使用PCM原始数据。
						{
							//调用用户定义的写入音频输出帧函数。
							m_AdoOtptPt.m_AdoOtptFrmLenPt.m_Val = m_AdoOtptPt.m_AdoOtptFrmPt.length;
							UserWriteAdoOtptFrm( m_AdoOtptPt.m_AdoOtptFrmPt, null, m_AdoOtptPt.m_AdoOtptFrmLenPt );
							break;
						}
						case 1: //如果要使用Speex解码器。
						{
							//调用用户定义的写入音频输出帧函数。
							m_AdoOtptPt.m_AdoOtptFrmLenPt.m_Val = m_AdoOtptPt.m_EncdAdoOtptFrmPt.length;
							UserWriteAdoOtptFrm( null, m_AdoOtptPt.m_EncdAdoOtptFrmPt, m_AdoOtptPt.m_AdoOtptFrmLenPt );

							//使用Speex解码器。
							if( m_AdoOtptPt.m_SpeexDecdPt.Pocs( m_AdoOtptPt.m_EncdAdoOtptFrmPt, m_AdoOtptPt.m_AdoOtptFrmLenPt.m_Val, m_AdoOtptPt.m_AdoOtptFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：使用Speex解码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：使用Speex解码器失败。" );
							}
							break;
						}
						case 2: //如果要使用Opus解码器。
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：暂不支持使用Opus解码器。" );
						}
					}

					//判断音频输出是否静音。在音频处理完后再设置静音，这样可以保证音频处理器的连续性。
					if( m_AdoOtptPt.m_AdoOtptIsMute != 0 )
					{
						Arrays.fill( m_AdoOtptPt.m_AdoOtptFrmPt, ( short ) 0 );
					}

					//写入本次音频输出帧到音频输出设备。
					m_AdoOtptPt.m_AdoOtptDvcPt.write( m_AdoOtptPt.m_AdoOtptFrmPt, 0, m_AdoOtptPt.m_AdoOtptFrmPt.length );

					//调用用户定义的获取PCM格式音频输出帧函数。
					UserGetPcmAdoOtptFrm( m_AdoOtptPt.m_AdoOtptFrmPt, m_AdoOtptPt.m_AdoOtptFrmPt.length );

					//追加本次音频输出帧到音频输出帧链表。
					synchronized( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt )
					{
						m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.addLast( m_AdoOtptPt.m_AdoOtptFrmPt );
					}

					if( m_IsPrintLogcat != 0 )
					{
						m_AdoOtptPt.m_NowTimeMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "音频输出线程：本次音频输出帧写入完毕，耗时 " + ( m_AdoOtptPt.m_NowTimeMsec - m_AdoOtptPt.m_LastTimeMsec ) + " 毫秒。" );
					}
				}

				if( m_AdoOtptPt.m_AdoOtptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
					break OutAdoOtptLoop;
				}
			} //音频输出循环完毕。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
		}
	}

	//视频输入线程类。
	private class VdoInptThrd extends Thread implements Camera.PreviewCallback
	{
		//读取一个视频输入帧的预览回调函数，本函数是在主线程中运行的。
		@Override public void onPreviewFrame( byte[] data, Camera camera )
		{
			//追加本次视频输入帧到视频输入帧链表。
			synchronized( m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt )
			{
				m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.addLast( data );
			}
			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：读取一个视频输入帧。" );
		}

		public void run()
		{
			this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
			Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：开始准备视频输入。" );

			//开始视频输入循环。
			OutVdoInptLoop:
			while( true )
			{
				if( m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.size() > 0 )//如果NV21格式视频输入帧链表中有帧了。
				{
					//开始处理视频输入帧。
					OutPocsVdoInptFrm:
					{
						//从NV21格式视频输入帧链表中取出第一个视频输入帧。
						m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal = m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.size();
						synchronized( m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt )
						{
							m_VdoInptPt.m_VdoInptFrmPt = m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.getFirst();
							m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从NV21格式视频输入帧链表中取出第一个NV21格式视频输入帧，NV21格式视频输入帧链表元素个数：" + m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal + "。" );

						//丢弃采样频率过快的视频输入帧。
						m_VdoInptPt.m_LastTimeMsec = System.currentTimeMillis();
						if( m_VdoInptPt.m_LastVdoInptFrmTimeMsec != 0 ) //如果已经设置过上一个视频输入帧的时间。
						{
							if( m_VdoInptPt.m_LastTimeMsec - m_VdoInptPt.m_LastVdoInptFrmTimeMsec >= m_VdoInptPt.m_VdoInptFrmTimeStepMsec )
							{
								m_VdoInptPt.m_LastVdoInptFrmTimeMsec += m_VdoInptPt.m_VdoInptFrmTimeStepMsec;
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：采样频率过快，本次视频输入设备帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}
						else //如果没有设置过上一个视频输入帧的时间。
						{
							m_VdoInptPt.m_LastVdoInptFrmTimeMsec = m_VdoInptPt.m_LastTimeMsec;
						}

						//裁剪视频输入设备帧。
						if( m_VdoInptPt.m_VdoInptDvcFrmIsCrop != 0 )
						{
							if( LibYUV.PictrCrop(
									m_VdoInptPt.m_VdoInptFrmPt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VdoInptPt.m_VdoInptDvcFrmWidth, m_VdoInptPt.m_VdoInptDvcFrmHeight,
									m_VdoInptPt.m_VdoInptDvcFrmCropX, m_VdoInptPt.m_VdoInptDvcFrmCropY, m_VdoInptPt.m_VdoInptDvcFrmCropWidth, m_VdoInptPt.m_VdoInptDvcFrmCropHeight,
									m_VdoInptPt.m_VdoInptTmpFrmPt, m_VdoInptPt.m_VdoInptRsltFrmSz, m_VdoInptPt.m_VdoInptRsltFrmLenPt, null, null,
									null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧成功。" );
								m_VdoInptPt.m_VdoInptSwapFrmPt = m_VdoInptPt.m_VdoInptTmpFrmPt;
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧失败，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}
						else
						{
							m_VdoInptPt.m_VdoInptSwapFrmPt = m_VdoInptPt.m_VdoInptFrmPt;
						}

						//NV21格式视频输入帧旋转为YU12格式视频输入帧。
						if( LibYUV.PictrRotate(
								m_VdoInptPt.m_VdoInptSwapFrmPt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VdoInptPt.m_VdoInptDvcFrmCropWidth, m_VdoInptPt.m_VdoInptDvcFrmCropHeight,
								m_VdoInptPt.m_VdoInptDvcFrmRotate,
								m_VdoInptPt.m_VdoInptRsltFrmPt, m_VdoInptPt.m_VdoInptRsltFrmPt.length, null, null,
								null ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧失败，本次视频输入帧丢弃。" );
							break OutPocsVdoInptFrm;
						}

						//缩放视频输入设备帧。
						if( m_VdoInptPt.m_VdoInptDvcFrmIsScale != 0 )
						{
							if( LibYUV.PictrScale(
									m_VdoInptPt.m_VdoInptRsltFrmPt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoInptPt.m_VdoInptDvcFrmRotateWidth, m_VdoInptPt.m_VdoInptDvcFrmRotateHeight,
									3,
									m_VdoInptPt.m_VdoInptTmpFrmPt, m_VdoInptPt.m_VdoInptRsltFrmSz, m_VdoInptPt.m_VdoInptRsltFrmLenPt, m_VdoInptPt.m_VdoInptDvcFrmScaleWidth, m_VdoInptPt.m_VdoInptDvcFrmScaleHeight,
									null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧成功。" );
								m_VdoInptPt.m_VdoInptSwapFrmPt = m_VdoInptPt.m_VdoInptRsltFrmPt; m_VdoInptPt.m_VdoInptRsltFrmPt = m_VdoInptPt.m_VdoInptTmpFrmPt; m_VdoInptPt.m_VdoInptTmpFrmPt = m_VdoInptPt.m_VdoInptSwapFrmPt; //交换视频输入结果帧和视频输入临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧失败，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}

						//获取一个视频输入空闲帧。
						if( ( m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal = m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.size() ) > 0 ) //如果视频输入空闲帧链表中有视频输入空闲帧。
						{
							//从视频输入空闲帧链表中取出第一个视频输入空闲帧。
							synchronized( m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt )
							{
								m_VdoInptPt.m_VdoInptFrmElmPt = m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.getFirst();
								m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.removeFirst();
							}
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从视频输入空闲帧链表中取出第一个视频输入空闲帧，视频输入空闲帧链表元素个数：" + m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal + "。" );
						}
						else //如果视频输入空闲帧链表中没有视频输入空闲帧。
						{
							if( ( m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal = m_VdoInptPt.m_VdoInptFrmLnkLstPt.size() ) <= 20 )
							{
								m_VdoInptPt.m_VdoInptFrmElmPt = m_VdoInptPt.new VdoInptFrmElm(); //创建一个视频输入空闲帧。
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：视频输入空闲帧链表中没有视频输入空闲帧，创建一个视频输入空闲帧。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：视频输入帧链表中视频输入帧数量为" + m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal + "已经超过上限20，不再创建一个视频输入空闲帧，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}

						//将视频结果帧复制到视频输入帧元素。
						System.arraycopy( m_VdoInptPt.m_VdoInptRsltFrmPt, 0, m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, 0, m_VdoInptPt.m_VdoInptDvcFrmScaleWidth * m_VdoInptPt.m_VdoInptDvcFrmScaleHeight * 3 / 2 );
						m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmWidthPt.m_Val = m_VdoInptPt.m_VdoInptDvcFrmScaleWidth;
						m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmHeightPt.m_Val = m_VdoInptPt.m_VdoInptDvcFrmScaleHeight;

						//判断视频输入是否黑屏。在视频输入处理完后再设置黑屏，这样可以保证视频输入处理器的连续性。
						if( m_VdoInptPt.m_VdoInptIsBlack != 0 )
						{
							int p_TmpLen = m_VdoInptPt.m_VdoInptDvcFrmScaleWidth * m_VdoInptPt.m_VdoInptDvcFrmScaleHeight;
							Arrays.fill( m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, 0, p_TmpLen, ( byte ) 0 );
							Arrays.fill( m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, p_TmpLen, m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt.length, ( byte ) 128 );
						}

						//使用编码器。
						switch( m_VdoInptPt.m_UseWhatEncd )
						{
							case 0: //如果要使用YU12原始数据。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用YU12原始数据。" );
								break;
							}
							case 1: //如果要使用OpenH264编码器。
							{
								if( m_VdoInptPt.m_OpenH264EncdPt.Pocs(
										m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, m_VdoInptPt.m_VdoInptDvcFrmScaleWidth, m_VdoInptPt.m_VdoInptDvcFrmScaleHeight, m_VdoInptPt.m_LastTimeMsec,
										m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt, m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt.length, m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt,
										null ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器成功。H264格式视频输入帧的长度：" + m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt.m_Val + "，时间戳：" + m_VdoInptPt.m_LastTimeMsec + "，类型：" + ( m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt[4] & 0xff ) + "。" );
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器失败，本次视频输入帧丢弃。" );
									break OutPocsVdoInptFrm;
								}
								break;
							}
							case 2: //如果要使用系统自带H264编码器。
							{
								if( m_VdoInptPt.m_SystemH264EncdPt.Pocs(
										m_VdoInptPt.m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, m_VdoInptPt.m_LastTimeMsec,
										m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt, ( long )m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt.length, m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt,
										1000 / m_VdoInptPt.m_MaxSmplRate * 2 / 3, null ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器成功。H264格式视频输入帧的长度：" + m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt.m_Val + "，时间戳：" + m_VdoInptPt.m_LastTimeMsec + "，类型：" + ( m_VdoInptPt.m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt[4] & 0xff ) + "。" );
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器失败，本次视频输入帧丢弃。" );
									break OutPocsVdoInptFrm;
								}
								break;
							}
						}

						//追加本次视频输入帧到视频输入帧链表。
						synchronized( m_VdoInptPt.m_VdoInptFrmLnkLstPt )
						{
							m_VdoInptPt.m_VdoInptFrmLnkLstPt.addLast( m_VdoInptPt.m_VdoInptFrmElmPt );
						}
						m_VdoInptPt.m_VdoInptFrmElmPt = null;

						if( m_IsPrintLogcat != 0 )
						{
							m_VdoInptPt.m_NowTimeMsec = System.currentTimeMillis();
							Log.i( m_CurClsNameStrPt, "视频输入线程：本次视频输入帧处理完毕，耗时 " + ( m_VdoInptPt.m_NowTimeMsec - m_VdoInptPt.m_LastTimeMsec ) + " 毫秒。" );
						}
					} //处理视频输入帧完毕。

					if( m_VdoInptPt.m_VdoInptFrmElmPt != null ) //如果获取的视频输入空闲帧没有追加到视频输入帧链表。
					{
						m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.addLast( m_VdoInptPt.m_VdoInptFrmElmPt );
						m_VdoInptPt.m_VdoInptFrmElmPt = null;
					}

					//追加本次NV21格式视频输入帧到视频输入设备。
					m_VdoInptPt.m_VdoInptDvcPt.addCallbackBuffer( m_VdoInptPt.m_VdoInptFrmPt );
				}

				if( m_VdoInptPt.m_VdoInptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程接收到退出请求，开始准备退出。" );
					break OutVdoInptLoop;
				}

				SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
			} //视频输入循环完毕。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程已退出。" );
		}
	}

	//视频输出线程类。
	private class VdoOtptThrd extends Thread
	{
		public void run()
		{
			this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
			Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：开始准备视频输出。" );

			//开始视频输出循环。
			OutVdoOtptLoop:
			while( true )
			{
				//开始处理视频输出帧。
				OutPocsVdoOtptFrm:
				{
					if( m_IsPrintLogcat != 0 ) m_VdoOtptPt.m_LastTimeMsec = System.currentTimeMillis();

					//调用用户定义的写入视频输出帧函数，并解码成YU12原始数据。
					switch( m_VdoOtptPt.m_UseWhatDecd ) //使用什么解码器。
					{
						case 0: //如果使用YU12原始数据。
						{
							//调用用户定义的写入视频输出帧函数。
							m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val = 0;
							m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val = 0;
							m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptPt.m_VdoOtptRsltFrmPt.length;
							UserWriteVdoOtptFrm( m_VdoOtptPt.m_VdoOtptRsltFrmPt, m_VdoOtptPt.m_VdoOtptFrmWidthPt, m_VdoOtptPt.m_VdoOtptFrmHeightPt, null, m_VdoOtptPt.m_VdoOtptRsltFrmLenPt );

							if( ( m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val > 0 ) && ( m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val > 0 ) ) //如果本次写入了视频输出帧。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用YU12原始数据成功。YU12格式帧宽度：" + m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val + "，YU12格式帧高度：" + m_VdoOtptPt.m_VdoOtptFrmHeightPt + "。" );
							}
							else //如果本次没写入视频输出帧。
							{
								break OutPocsVdoOtptFrm;
							}
							break;
						}
						case 1: //如果使用OpenH264解码器。
						{
							//调用用户定义的写入视频输出帧函数。
							m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptPt.m_VdoOtptTmpFrmPt.length;
							UserWriteVdoOtptFrm( null, null, null, m_VdoOtptPt.m_VdoOtptTmpFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmLenPt );

							if( m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val > 0 ) //如果本次写入了视频输出帧。
							{
								//使用OpenH264解码器。
								if( m_VdoOtptPt.m_OpenH264DecdPt.Pocs(
										m_VdoOtptPt.m_VdoOtptTmpFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val,
										m_VdoOtptPt.m_VdoOtptRsltFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmPt.length, m_VdoOtptPt.m_VdoOtptFrmWidthPt, m_VdoOtptPt.m_VdoOtptFrmHeightPt,
										null ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器成功。已解码YU12格式帧宽度：" + m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val + "。" );
									if( ( m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val == 0 ) ) break OutPocsVdoOtptFrm; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器失败，本次视频输出帧丢弃。" );
									break OutPocsVdoOtptFrm;
								}
							}
							else //如果本次没写入视频输出帧。
							{
								break OutPocsVdoOtptFrm;
							}
							break;
						}
						case 2: //如果使用系统自带H264解码器。
						{
							//调用用户定义的写入视频输出帧函数。
							m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptPt.m_VdoOtptTmpFrmPt.length;
							UserWriteVdoOtptFrm( null, null, null, m_VdoOtptPt.m_VdoOtptTmpFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmLenPt );

							if( m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val != 0 ) //如果本次写入了视频输出帧。
							{
								//使用系统自带H264解码器。
								if( m_VdoOtptPt.m_SystemH264DecdPt.Pocs(
										m_VdoOtptPt.m_VdoOtptTmpFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmLenPt.m_Val,
										m_VdoOtptPt.m_VdoOtptRsltFrmPt, m_VdoOtptPt.m_VdoOtptRsltFrmPt.length, m_VdoOtptPt.m_VdoOtptFrmWidthPt, m_VdoOtptPt.m_VdoOtptFrmHeightPt,
										40, null ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用系统自带H264解码器成功。已解码YU12格式帧宽度：" + m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val + "。" );
									if( ( m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val == 0 ) ) break OutPocsVdoOtptFrm; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出线程：使用系统自带H264解码器失败，本次视频输出帧丢弃。" );
									break OutPocsVdoOtptFrm;
								}
							}
							else //如果本次没写入视频输出帧。
							{
								break OutPocsVdoOtptFrm;
							}
							break;
						}
					}

					//用户定义的获取YU12格式视频输出帧函数。
					UserGetYU12VdoOtptFrm( m_VdoOtptPt.m_VdoOtptRsltFrmPt, m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val );

					//判断视频输出是否黑屏。在视频处理完后再设置黑屏，这样可以保证视频处理器的连续性。
					if( m_VdoOtptPt.m_VdoOtptIsBlack != 0 )
					{
						int p_TmpLen = m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val * m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val;
						Arrays.fill( m_VdoOtptPt.m_VdoOtptRsltFrmPt, 0, p_TmpLen, ( byte ) 0 );
						Arrays.fill( m_VdoOtptPt.m_VdoOtptRsltFrmPt, p_TmpLen, p_TmpLen + p_TmpLen / 2, ( byte ) 128 );
					}

					//缩放视频输出帧。
					if( m_VdoOtptPt.m_VdoOtptDspyScale != 1.0f )
					{
						if( LibYUV.PictrScale(
								m_VdoOtptPt.m_VdoOtptRsltFrmPt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val,
								3,
								m_VdoOtptPt.m_VdoOtptTmpFrmPt, m_VdoOtptPt.m_VdoOtptTmpFrmPt.length, null, ( int )( m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val * m_VdoOtptPt.m_VdoOtptDspyScale ), ( int )( m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val * m_VdoOtptPt.m_VdoOtptDspyScale ),
								null ) != 0 )
						{
							Log.e( m_CurClsNameStrPt, "视频输出线程：视频输出显示缩放失败，本次视频输出帧丢弃。" );
							break OutPocsVdoOtptFrm;
						}
						m_VdoOtptPt.m_VdoOtptSwapFrmPt = m_VdoOtptPt.m_VdoOtptRsltFrmPt; m_VdoOtptPt.m_VdoOtptRsltFrmPt = m_VdoOtptPt.m_VdoOtptTmpFrmPt; m_VdoOtptPt.m_VdoOtptTmpFrmPt = m_VdoOtptPt.m_VdoOtptSwapFrmPt; //交换视频结果帧和视频临时帧。

						m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val *= m_VdoOtptPt.m_VdoOtptDspyScale;
						m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val *= m_VdoOtptPt.m_VdoOtptDspyScale;
					}

					//设置视频输出显示SurfaceView的宽高比。
					m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val / m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val );

					//显示视频输出帧。
					if( LibYUV.PictrDrawToSurface(
							m_VdoOtptPt.m_VdoOtptRsltFrmPt, 0, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoOtptPt.m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptPt.m_VdoOtptFrmHeightPt.m_Val,
							m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt.getHolder().getSurface(),
							null ) != 0 )
					{
						Log.e( m_CurClsNameStrPt, "视频输出线程：绘制视频输出帧到视频输出显示SurfaceView失败，本次视频输出帧丢弃。" );
						break OutPocsVdoOtptFrm;
					}

					if( m_IsPrintLogcat != 0 )
					{
						m_VdoOtptPt.m_NowTimeMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "视频输出线程：本次视频输出帧处理完毕，耗时 " + ( m_VdoOtptPt.m_NowTimeMsec - m_VdoOtptPt.m_LastTimeMsec ) + " 毫秒。" );
					}
				} //处理视频输出帧完毕。

				if( m_VdoOtptPt.m_VdoOtptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：本线程接收到退出请求，开始准备退出。" );
					break OutVdoOtptLoop;
				}

				SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
			} //视频输出循环完毕。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：本线程已退出。" );
		}
	}

	//本线程执行函数。
	public void run()
	{
		this.setPriority( this.MAX_PRIORITY ); //设置本线程优先级。
		Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

		int p_TmpInt321 = 0;
		int p_TmpInt322 = 0;
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		OutMediaPocsThrdLoop:
		while( true )
		{
			OutMediaInitAndPocs:
			{
				m_RunFlag = RUN_FLAG_INIT; //设置本线程运行标记为刚开始运行正在初始化。

				if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本地代码的指令集名称（CPU类型+ ABI约定）为" + android.os.Build.CPU_ABI + "，手机型号为" + android.os.Build.MODEL + "，上下文为" + m_AppCntxtPt + "。" );

				//初始化错误信息动态字符串。
				m_ErrInfoVarStrPt = new VarStr();
				m_ErrInfoVarStrPt.Init();

				//初始化唤醒锁。
				WakeLockInitOrDstoy( m_IsUseWakeLock );

				if( m_ExitFlag != 3 ) //如果需要执行用户定义的初始化函数。
				{
					m_ExitFlag = 0; //设置本线程退出标记为保持运行。
					m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。

					//调用用户定义的初始化函数。
					p_TmpInt321 = UserInit();
					if( p_TmpInt321 == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt321 );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt321 );
						break OutMediaInitAndPocs;
					}
				}
				else //如果不要执行用户定义的初始化函数。
				{
					m_ExitFlag = 0; //设置本线程退出标记为保持运行。
					m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。
				}

				//保存设置到文件。
				if( m_IsSaveStngToFile != 0 )
				{
					File p_StngFilePt = new File( m_StngFileFullPathStrPt );

					try
					{
						if( !p_StngFilePt.exists() )
						{
							p_StngFilePt.createNewFile();
						}
						FileWriter p_StngFileWriterPt = new FileWriter( p_StngFilePt );

						p_StngFileWriterPt.write( "m_AppCntxtPt：" + m_AppCntxtPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_IsSaveStngToFile：" + m_IsSaveStngToFile + "\n" );
						p_StngFileWriterPt.write( "m_StngFileFullPathStrPt：" + m_StngFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_IsPrintLogcat：" + m_IsPrintLogcat + "\n" );
						p_StngFileWriterPt.write( "m_IsShowToast：" + m_IsShowToast + "\n" );
						p_StngFileWriterPt.write( "m_ShowToastActivityPt：" + m_ShowToastActivityPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_IsUseWakeLock：" + m_IsUseWakeLock + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsUseAdoInpt：" + m_AdoInptPt.m_IsUseAdoInpt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SmplRate：" + m_AdoInptPt.m_SmplRate + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_FrmLen：" + m_AdoInptPt.m_FrmLen + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptIsUseSystemAecNsAgc：" + m_AdoInptPt.m_IsUseSystemAecNsAgc + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptUseWhatAec：" + m_AdoInptPt.m_UseWhatAec + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecFilterLen：" + m_AdoInptPt.m_SpeexAecFilterLen + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecIsUseRec：" + m_AdoInptPt.m_SpeexAecIsUseRec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoMultiple：" + m_AdoInptPt.m_SpeexAecEchoMultiple + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoCont：" + m_AdoInptPt.m_SpeexAecEchoCont + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoSupes：" + m_AdoInptPt.m_SpeexAecEchoSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoSupesAct：" + m_AdoInptPt.m_SpeexAecEchoSupesAct + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecIsSaveMemFile：" + m_AdoInptPt.m_SpeexAecIsSaveMemFile + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt：" + m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmIsUseCNGMode：" + m_AdoInptPt.m_WebRtcAecmIsUseCNGMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmEchoMode：" + m_AdoInptPt.m_WebRtcAecmEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmDelay：" + m_AdoInptPt.m_WebRtcAecmDelay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecEchoMode：" + m_AdoInptPt.m_WebRtcAecEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecDelay：" + m_AdoInptPt.m_WebRtcAecDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode：" + m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode：" + m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay：" + m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecIsSaveMemFile：" + m_AdoInptPt.m_WebRtcAecIsSaveMemFile + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt：" + m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWorkMode：" + m_AdoInptPt.m_SpeexWebRtcAecWorkMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMultiple：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMultiple + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCont：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCont + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay：" + m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec：" + m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay：" + m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_UseWhatNs：" + m_AdoInptPt.m_UseWhatNs + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsIsUseNs：" + m_AdoInptPt.m_SpeexPrpocsIsUseNs + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsNoiseSupes：" + m_AdoInptPt.m_SpeexPrpocsNoiseSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsIsUseDereverb：" + m_AdoInptPt.m_SpeexPrpocsIsUseDereverb + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcNsxPolicyMode：" + m_AdoInptPt.m_WebRtcNsxPolicyMode + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcNsPolicyMode：" + m_AdoInptPt.m_WebRtcNsPolicyMode + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsUseSpeexPrpocsOther：" + m_AdoInptPt.m_IsUseSpeexPrpocsOther + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsIsUseVad：" + m_AdoInptPt.m_SpeexPrpocsIsUseVad + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsVadProbStart：" + m_AdoInptPt.m_SpeexPrpocsVadProbStart + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsVadProbCont：" + m_AdoInptPt.m_SpeexPrpocsVadProbCont + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsIsUseAgc：" + m_AdoInptPt.m_SpeexPrpocsIsUseAgc + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsAgcLevel：" + m_AdoInptPt.m_SpeexPrpocsAgcLevel + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsAgcIncrement：" + m_AdoInptPt.m_SpeexPrpocsAgcIncrement + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsAgcDecrement：" + m_AdoInptPt.m_SpeexPrpocsAgcDecrement + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsAgcMaxGain：" + m_AdoInptPt.m_SpeexPrpocsAgcMaxGain + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_UseWhatEncd：" + m_AdoInptPt.m_UseWhatEncd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdUseCbrOrVbr：" + m_AdoInptPt.m_SpeexEncdUseCbrOrVbr + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdQualt：" + m_AdoInptPt.m_SpeexEncdQualt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdCmplxt：" + m_AdoInptPt.m_SpeexEncdCmplxt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdPlcExptLossRate：" + m_AdoInptPt.m_SpeexEncdPlcExptLossRate + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsSaveAdoToFile：" + m_AdoInptPt.m_IsSaveAdoToFile + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptFileFullPathStrPt：" + m_AdoInptPt.m_AdoInptFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoRsltFileFullPathStrPt：" + m_AdoInptPt.m_AdoRsltFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsDrawAdoWavfmToSurface：" + m_AdoInptPt.m_IsDrawAdoWavfmToSurface + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptOscilloSurfacePt：" + m_AdoInptPt.m_AdoInptOscilloSurfacePt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoRsltOscilloSurfacePt：" + m_AdoInptPt.m_AdoRsltOscilloSurfacePt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptDvcBufSz：" + m_AdoInptPt.m_AdoInptDvcBufSz + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptIsMute：" + m_AdoInptPt.m_AdoInptIsMute + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsUseAdoOtpt：" + m_AdoOtptPt.m_IsUseAdoOtpt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_SmplRate：" + m_AdoOtptPt.m_SmplRate + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLen：" + m_AdoOtptPt.m_FrmLen + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatDecd：" + m_AdoOtptPt.m_UseWhatDecd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt：" + m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsSaveAdoToFile：" + m_AdoOtptPt.m_IsSaveAdoToFile + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptFileFullPathStrPt：" + m_AdoOtptPt.m_AdoOtptFileFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsDrawAdoWavfmToSurface：" + m_AdoOtptPt.m_IsDrawAdoWavfmToSurface + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptOscilloSurfacePt：" + m_AdoOtptPt.m_AdoOtptOscilloSurfacePt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptDvcBufSz：" + m_AdoOtptPt.m_AdoOtptDvcBufSz + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatAdoOtptDvc：" + m_AdoOtptPt.m_UseWhatAdoOtptDvc + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatAdoOtptStreamType：" + m_AdoOtptPt.m_UseWhatAdoOtptStreamType + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptIsMute：" + m_AdoOtptPt.m_AdoOtptIsMute + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_VdoInptPt.m_IsUseVdoInpt：" + m_VdoInptPt.m_IsUseVdoInpt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_MaxSmplRate：" + m_VdoInptPt.m_MaxSmplRate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_FrmWidth：" + m_VdoInptPt.m_FrmWidth + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_FrmHeight：" + m_VdoInptPt.m_FrmHeight + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_ScreenRotate：" + m_VdoInptPt.m_ScreenRotate + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_UseWhatEncd：" + m_VdoInptPt.m_UseWhatEncd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdVdoType：" + m_VdoInptPt.m_OpenH264EncdVdoType + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdEncdBitrate：" + m_VdoInptPt.m_OpenH264EncdEncdBitrate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode：" + m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl：" + m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdCmplxt：" + m_VdoInptPt.m_OpenH264EncdCmplxt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdEncdBitrate：" + m_VdoInptPt.m_SystemH264EncdEncdBitrate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode：" + m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec：" + m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdCmplxt：" + m_VdoInptPt.m_SystemH264EncdCmplxt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_UseWhatVdoInptDvc：" + m_VdoInptPt.m_UseWhatVdoInptDvc + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt：" + m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_VdoInptIsBlack：" + m_VdoInptPt.m_VdoInptIsBlack + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_VdoOtptPt.m_IsUseVdoOtpt：" + m_VdoOtptPt.m_IsUseVdoOtpt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_UseWhatDecd：" + m_VdoOtptPt.m_UseWhatDecd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_OpenH264DecdDecdThrdNum：" + m_VdoOtptPt.m_OpenH264DecdDecdThrdNum + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt：" + m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt + "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspyScale：" + m_VdoOtptPt.m_VdoOtptDspyScale + "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptIsBlack：" + m_VdoOtptPt.m_VdoOtptIsBlack + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.flush();
						p_StngFileWriterPt.close();
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + m_StngFileFullPathStrPt + " 成功。" );
					}
					catch( IOException e )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + m_StngFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
						break OutMediaInitAndPocs;
					}
				}

				//初始化音频输入。
				if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) //如果要使用音频输入。
				{
					//创建并初始化声学回音消除器。
					if( m_AdoInptPt.m_UseWhatAec != 0 ) //如果要使用声学回音消除器。
					{
						if( m_AdoOtptPt.m_IsUseAdoOtpt == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：不使用音频输出时，不能使用声学回音消除器。" );
							break OutMediaInitAndPocs;
						}
						if( m_AdoOtptPt.m_SmplRate != m_AdoInptPt.m_SmplRate )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音频输出的采样频率与音频输入不一致时，不能使用声学回音消除器。" );
							break OutMediaInitAndPocs;
						}
						if( m_AdoOtptPt.m_FrmLen != m_AdoInptPt.m_FrmLen )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音频输出帧的长度与音频输入不一致时，不能使用声学回音消除器。" );
							break OutMediaInitAndPocs;
						}
					}
					switch( m_AdoInptPt.m_UseWhatAec )
					{
						case 0: //如果不使用声学回音消除器。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用声学回音消除器。" );
							break;
						}
						case 1: //如果要使用Speex声学回音消除器。
						{
							if( m_AdoInptPt.m_SpeexAecIsSaveMemFile != 0 )
							{
								m_AdoInptPt.m_SpeexAecPt = new SpeexAec();
								if( m_AdoInptPt.m_SpeexAecPt.InitByMemFile( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_SpeexAecFilterLen, m_AdoInptPt.m_SpeexAecIsUseRec, m_AdoInptPt.m_SpeexAecEchoMultiple, m_AdoInptPt.m_SpeexAecEchoCont, m_AdoInptPt.m_SpeexAecEchoSupes, m_AdoInptPt.m_SpeexAecEchoSupesAct, m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器成功。" );
								}
								else
								{
									m_AdoInptPt.m_SpeexAecPt = null;
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								}
							}
							if( m_AdoInptPt.m_SpeexAecPt == null )
							{
								m_AdoInptPt.m_SpeexAecPt = new SpeexAec();
								if( m_AdoInptPt.m_SpeexAecPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_SpeexAecFilterLen, m_AdoInptPt.m_SpeexAecIsUseRec, m_AdoInptPt.m_SpeexAecEchoMultiple, m_AdoInptPt.m_SpeexAecEchoCont, m_AdoInptPt.m_SpeexAecEchoSupes, m_AdoInptPt.m_SpeexAecEchoSupesAct, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器成功。" );
								}
								else
								{
									m_AdoInptPt.m_SpeexAecPt = null;
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
									break OutMediaInitAndPocs;
								}
							}
							break;
						}
						case 2: //如果要使用WebRtc定点版声学回音消除器。
						{
							m_AdoInptPt.m_WebRtcAecmPt = new WebRtcAecm();
							if( m_AdoInptPt.m_WebRtcAecmPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcAecmIsUseCNGMode, m_AdoInptPt.m_WebRtcAecmEchoMode, m_AdoInptPt.m_WebRtcAecmDelay, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器成功。" );
							}
							else
							{
								m_AdoInptPt.m_WebRtcAecmPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 3: //如果要使用WebRtc浮点版声学回音消除器。
						{
							if( m_AdoInptPt.m_WebRtcAecIsSaveMemFile != 0 )
							{
								m_AdoInptPt.m_WebRtcAecPt = new WebRtcAec();
								if( m_AdoInptPt.m_WebRtcAecPt.InitByMemFile( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcAecEchoMode, m_AdoInptPt.m_WebRtcAecDelay, m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode, m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode, m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器成功。" );
								}
								else
								{
									m_AdoInptPt.m_WebRtcAecPt = null;
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								}
							}
							if( m_AdoInptPt.m_WebRtcAecPt == null )
							{
								m_AdoInptPt.m_WebRtcAecPt = new WebRtcAec();
								if( m_AdoInptPt.m_WebRtcAecPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcAecEchoMode, m_AdoInptPt.m_WebRtcAecDelay, m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode, m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode, m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器成功。" );
								}
								else
								{
									m_AdoInptPt.m_WebRtcAecPt = null;
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
									break OutMediaInitAndPocs;
								}
							}
							break;
						}
						case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
						{
							m_AdoInptPt.m_SpeexWebRtcAecPt = new SpeexWebRtcAec();
							if( m_AdoInptPt.m_SpeexWebRtcAecPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_SpeexWebRtcAecWorkMode, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMultiple, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCont, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes, m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode, m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay, m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec, m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器成功。" );
							}
							else
							{
								m_AdoInptPt.m_SpeexWebRtcAecPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
					}

					//创建并初始化噪音抑制器对象。
					switch( m_AdoInptPt.m_UseWhatNs )
					{
						case 0: //如果不使用噪音抑制器。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用噪音抑制器。" );
							break;
						}
						case 1: //如果要使用Speex预处理器的噪音抑制。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：稍后在初始化Speex预处理器时一起初始化Speex预处理器的噪音抑制。" );
							break;
						}
						case 2: //如果要使用WebRtc定点版噪音抑制器。
						{
							m_AdoInptPt.m_WebRtcNsxPt = new WebRtcNsx();
							if( m_AdoInptPt.m_WebRtcNsxPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcNsxPolicyMode, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器成功。" );
							}
							else
							{
								m_AdoInptPt.m_WebRtcNsxPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 3: //如果要使用WebRtc浮点版噪音抑制器。
						{
							m_AdoInptPt.m_WebRtcNsPt = new WebRtcNs();
							if( m_AdoInptPt.m_WebRtcNsPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcNsPolicyMode, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器成功。" );
							}
							else
							{
								m_AdoInptPt.m_WebRtcNsPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 4: //如果要使用RNNoise噪音抑制器。
						{
							m_AdoInptPt.m_RNNoisePt = new RNNoise();
							if( m_AdoInptPt.m_RNNoisePt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器成功。" );
							}
							else
							{
								m_AdoInptPt.m_RNNoisePt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
					}

					//创建并初始化Speex预处理器。
					if( ( m_AdoInptPt.m_UseWhatNs == 1 ) || ( m_AdoInptPt.m_IsUseSpeexPrpocsOther != 0 ) )
					{
						if( m_AdoInptPt.m_UseWhatNs != 1 )
						{
							m_AdoInptPt.m_SpeexPrpocsIsUseNs = 0;
							m_AdoInptPt.m_SpeexPrpocsIsUseDereverb = 0;
						}
						if( m_AdoInptPt.m_IsUseSpeexPrpocsOther == 0 )
						{
							m_AdoInptPt.m_SpeexPrpocsIsUseVad = 0;
							m_AdoInptPt.m_SpeexPrpocsIsUseAgc = 0;
						}
						m_AdoInptPt.m_SpeexPrpocsPt = new SpeexPrpocs();
						if( m_AdoInptPt.m_SpeexPrpocsPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_SpeexPrpocsIsUseNs, m_AdoInptPt.m_SpeexPrpocsNoiseSupes, m_AdoInptPt.m_SpeexPrpocsIsUseDereverb, m_AdoInptPt.m_SpeexPrpocsIsUseVad, m_AdoInptPt.m_SpeexPrpocsVadProbStart, m_AdoInptPt.m_SpeexPrpocsVadProbCont, m_AdoInptPt.m_SpeexPrpocsIsUseAgc, m_AdoInptPt.m_SpeexPrpocsAgcLevel, m_AdoInptPt.m_SpeexPrpocsAgcIncrement, m_AdoInptPt.m_SpeexPrpocsAgcDecrement, m_AdoInptPt.m_SpeexPrpocsAgcMaxGain, m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器成功。"  );
						}
						else
						{
							m_AdoInptPt.m_SpeexPrpocsPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							break OutMediaInitAndPocs;
						}
					}

					//初始化编码器对象。
					switch( m_AdoInptPt.m_UseWhatEncd )
					{
						case 0: //如果要使用PCM原始数据。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
							break;
						}
						case 1: //如果要使用Speex编码器。
						{
							if( m_AdoInptPt.m_FrmLen != m_AdoInptPt.m_SmplRate / 1000 * 20 )
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的长度不为20毫秒不能使用Speex编码器。" );
								break OutMediaInitAndPocs;
							}
							m_AdoInptPt.m_SpeexEncdPt = new SpeexEncd();
							if( m_AdoInptPt.m_SpeexEncdPt.Init( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_SpeexEncdUseCbrOrVbr, m_AdoInptPt.m_SpeexEncdQualt, m_AdoInptPt.m_SpeexEncdCmplxt, m_AdoInptPt.m_SpeexEncdPlcExptLossRate ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex编码器成功。" );
							}
							else
							{
								m_AdoInptPt.m_SpeexEncdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex编码器失败。" );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 2: //如果要使用Opus编码器。
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus编码器。" );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输入Wave文件写入器、音频结果Wave文件写入器。
					if( m_AdoInptPt.m_IsSaveAdoToFile != 0 )
					{
						m_AdoInptPt.m_AdoInptWaveFileWriterPt = new WaveFileWriter();
						if( m_AdoInptPt.m_AdoInptWaveFileWriterPt.Init( m_AdoInptPt.m_AdoInptFileFullPathStrPt, ( short ) 1, m_AdoInptPt.m_SmplRate, 16 ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AdoInptPt.m_AdoInptFileFullPathStrPt + " 的Wave文件写入器成功。" );
						}
						else
						{
							m_AdoInptPt.m_AdoInptWaveFileWriterPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AdoInptPt.m_AdoInptFileFullPathStrPt + " 的Wave文件写入器失败。" );
							break OutMediaInitAndPocs;
						}
						m_AdoInptPt.m_AdoRsltWaveFileWriterPt = new WaveFileWriter();
						if( m_AdoInptPt.m_AdoRsltWaveFileWriterPt.Init( m_AdoInptPt.m_AdoRsltFileFullPathStrPt, ( short ) 1, m_AdoInptPt.m_SmplRate, 16 ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AdoInptPt.m_AdoRsltFileFullPathStrPt + " 的Wave文件写入器成功。" );
						}
						else
						{
							m_AdoInptPt.m_AdoRsltWaveFileWriterPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AdoInptPt.m_AdoRsltFileFullPathStrPt + " 的Wave文件写入器失败。" );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输入波形器、音频结果波形器。
					if( m_AdoInptPt.m_IsDrawAdoWavfmToSurface != 0 )
					{
						m_AdoInptPt.m_AdoInptOscilloPt = new AdoWavfm();
						if( m_AdoInptPt.m_AdoInptOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入波形器成功。" );
						}
						else
						{
							m_AdoInptPt.m_AdoInptOscilloPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							break OutMediaInitAndPocs;
						}
						m_AdoInptPt.m_AdoRsltOscilloPt = new AdoWavfm();
						if( m_AdoInptPt.m_AdoRsltOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果波形器成功。" );
						}
						else
						{
							m_AdoInptPt.m_AdoRsltOscilloPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输入设备。
					try
					{
						m_AdoInptPt.m_AdoInptDvcBufSz = AudioRecord.getMinBufferSize( m_AdoInptPt.m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
						m_AdoInptPt.m_AdoInptDvcBufSz = ( m_AdoInptPt.m_AdoInptDvcBufSz > m_AdoInptPt.m_FrmLen * 2 ) ? m_AdoInptPt.m_AdoInptDvcBufSz : m_AdoInptPt.m_FrmLen * 2;
						m_AdoInptPt.m_AdoInptDvcPt = new AudioRecord(
								( m_AdoInptPt.m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
								m_AdoInptPt.m_SmplRate,
								AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.ENCODING_PCM_16BIT,
								m_AdoInptPt.m_AdoInptDvcBufSz );
						if( m_AdoInptPt.m_AdoInptDvcPt.getState() == AudioRecord.STATE_INITIALIZED )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备成功。音频输入设备缓冲区大小：" + m_AdoInptPt.m_AdoInptDvcBufSz );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备失败。" );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, "媒体处理线程：创建并初始化音频输入设备失败。", Toast.LENGTH_LONG ).show(); } } );
							break OutMediaInitAndPocs;
						}
					}
					catch( IllegalArgumentException e )
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备失败。原因：" + e.getMessage() );
						if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, "媒体处理线程：创建并初始化音频输入设备失败。原因：" + e.getMessage(), Toast.LENGTH_LONG ).show(); } } );
						break OutMediaInitAndPocs;
					}

					//创建并初始化音频输入帧链表。
					m_AdoInptPt.m_AdoInptFrmLnkLstPt = new LinkedList< short[] >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入帧链表成功。" );

					//创建并初始化音频输入空闲帧链表。
					m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt = new LinkedList< short[] >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入空闲帧链表成功。" );

					//初始化音频输入线程的临时变量。
					{
						m_AdoInptPt.m_AdoInptFrmPt = null; //初始化音频输入帧的指针。
						m_AdoInptPt.m_AdoInptFrmLnkLstElmTotal = 0; //初始化音频输入帧链表的元数总数。
						m_AdoInptPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
						m_AdoInptPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音频输入线程的临时变量成功。" );
					}

					//创建并初始化音频输入线程。
					m_AdoInptPt.m_AdoInptThrdExitFlag = 0; //设置音频输入线程退出标记为0表示保持运行。
					m_AdoInptPt.m_AdoInptThrdPt = new AdoInptThrd();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入线程成功。" );
				} //初始化音频输入完毕。

				//初始化音频输出。
				if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
				{
					//初始化解码器对象。
					switch( m_AdoOtptPt.m_UseWhatDecd )
					{
						case 0: //如果要使用PCM原始数据。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
							break;
						}
						case 1: //如果要使用Speex解码器。
						{
							if( m_AdoOtptPt.m_FrmLen != m_AdoOtptPt.m_SmplRate / 1000 * 20 )
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的长度不为20毫秒不能使用Speex解码器。" );
								break OutMediaInitAndPocs;
							}
							m_AdoOtptPt.m_SpeexDecdPt = new SpeexDecd();
							if( m_AdoOtptPt.m_SpeexDecdPt.Init( m_AdoOtptPt.m_SmplRate, m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex解码器成功。" );
							}
							else
							{
								m_AdoOtptPt.m_SpeexDecdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex解码器失败。" );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 2: //如果要使用Opus解码器。
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus解码器。" );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输出Wave文件写入器。
					if( m_AdoOtptPt.m_IsSaveAdoToFile != 0 )
					{
						m_AdoOtptPt.m_AdoOtptWaveFileWriterPt = new WaveFileWriter();
						if( m_AdoOtptPt.m_AdoOtptWaveFileWriterPt.Init( m_AdoOtptPt.m_AdoOtptFileFullPathStrPt, ( short ) 1, m_AdoOtptPt.m_SmplRate, 16 ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AdoOtptPt.m_AdoOtptFileFullPathStrPt + " 的Wave文件写入器成功。" );
						}
						else
						{
							m_AdoOtptPt.m_AdoOtptWaveFileWriterPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AdoOtptPt.m_AdoOtptFileFullPathStrPt + " 的Wave文件写入器失败。" );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输出波形器。
					if( m_AdoOtptPt.m_IsDrawAdoWavfmToSurface != 0 )
					{
						m_AdoOtptPt.m_AdoOtptOscilloPt = new AdoWavfm();
						if( m_AdoOtptPt.m_AdoOtptOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出波形器成功。" );
						}
						else
						{
							m_AdoOtptPt.m_AdoOtptOscilloPt = null;
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							break OutMediaInitAndPocs;
						}
					}

					//设置音频输出设备。
					if( m_AdoOtptPt.m_UseWhatAdoOtptDvc == 0 ) //如果要使用扬声器。
					{
						( ( AudioManager )m_AppCntxtPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( true ); //打开扬声器。
					}
					else //如果要使用听筒。
					{
						( ( AudioManager )m_AppCntxtPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( false ); //关闭扬声器。
					}

					//用第一种方法创建并初始化音频输出设备。
					try
					{
						m_AdoOtptPt.m_AdoOtptDvcBufSz = m_AdoOtptPt.m_FrmLen * 2;
						m_AdoOtptPt.m_AdoOtptDvcPt = new AudioTrack( ( m_AdoOtptPt.m_UseWhatAdoOtptStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
								m_AdoOtptPt.m_SmplRate,
								AudioFormat.CHANNEL_CONFIGURATION_MONO,
								AudioFormat.ENCODING_PCM_16BIT,
								m_AdoOtptPt.m_AdoOtptDvcBufSz,
								AudioTrack.MODE_STREAM );
						if( m_AdoOtptPt.m_AdoOtptDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AdoOtptPt.m_AdoOtptDvcBufSz );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备失败。" );
							m_AdoOtptPt.m_AdoOtptDvcPt.release();
							m_AdoOtptPt.m_AdoOtptDvcPt = null;
						}
					}
					catch( IllegalArgumentException e )
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备失败。原因：" + e.getMessage() );
					}

					//用第二种方法创建并初始化音频输出设备。
					if( m_AdoOtptPt.m_AdoOtptDvcPt == null )
					{
						try
						{
							m_AdoOtptPt.m_AdoOtptDvcBufSz = AudioTrack.getMinBufferSize( m_AdoOtptPt.m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
							m_AdoOtptPt.m_AdoOtptDvcPt = new AudioTrack( ( m_AdoOtptPt.m_UseWhatAdoOtptStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
									m_AdoOtptPt.m_SmplRate,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									AudioFormat.ENCODING_PCM_16BIT,
									m_AdoOtptPt.m_AdoOtptDvcBufSz,
									AudioTrack.MODE_STREAM );
							if( m_AdoOtptPt.m_AdoOtptDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AdoOtptPt.m_AdoOtptDvcBufSz );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备失败。" );
								break OutMediaInitAndPocs;
							}
						}
						catch( IllegalArgumentException e )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备失败。原因：" + e.getMessage() );
							break OutMediaInitAndPocs;
						}
					}

					//创建并初始化音频输出帧链表。
					m_AdoOtptPt.m_AdoOtptFrmLnkLstPt = new LinkedList< short[] >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出帧链表成功。" );

					//创建并初始化音频输出空闲帧链表。
					m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt = new LinkedList< short[] >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出空闲帧链表成功。" );

					//初始化音频输出线程的临时变量。
					{
						m_AdoOtptPt.m_AdoOtptFrmPt = null; //初始化音频输出帧的指针。
						m_AdoOtptPt.m_EncdAdoOtptFrmPt = ( m_AdoOtptPt.m_UseWhatDecd != 0 ) ? new byte[ m_AdoOtptPt.m_FrmLen ] : null; //初始化已编码格式音频输出帧的指针。
						m_AdoOtptPt.m_AdoOtptFrmLenPt = new HTLong(); //初始化音频输出帧的长度，单位字节。
						m_AdoOtptPt.m_AdoOtptFrmLnkLstElmTotal = 0; //初始化音频输出帧链表的元数总数。
						m_AdoOtptPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
						m_AdoOtptPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音频输出线程的临时变量成功。" );
					}

					//创建并初始化音频输出线程。
					m_AdoOtptPt.m_AdoOtptThrdExitFlag = 0; //设置音频输出线程退出标记为0表示保持运行。
					m_AdoOtptPt.m_AdoOtptThrdPt = new AdoOtptThrd();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出线程成功。" );
				} //初始化音频输出完毕。

				//初始化视频输入。
				if( m_VdoInptPt.m_IsUseVdoInpt != 0 )
				{
					//创建视频输入线程。
					m_VdoInptPt.m_VdoInptThrdExitFlag = 0; //设置视频输入线程退出标记为0表示保持运行。
					m_VdoInptPt.m_VdoInptThrdPt = new VdoInptThrd();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输入线程成功。" );

					//创建并初始化视频输入设备。
					{
						//打开视频输入设备。
						{
							int p_CameraDvcId = 0;
							Camera.CameraInfo p_CameraInfoPt = new Camera.CameraInfo();

							//查找视频输入设备对应的ID。
							if( m_VdoInptPt.m_UseWhatVdoInptDvc == 0 ) //如果要使用前置摄像头。
							{
								p_CameraDvcId = m_VdoInptPt.m_FrontCameraDvcId;
							}
							else if( m_VdoInptPt.m_UseWhatVdoInptDvc == 1 ) //如果要使用后置摄像头。
							{
								p_CameraDvcId = m_VdoInptPt.m_BackCameraDvcId;
							}
							if( p_CameraDvcId == -1 ) //如果需要自动查找设备ID。
							{
								for( p_CameraDvcId = 0; p_CameraDvcId < Camera.getNumberOfCameras(); p_CameraDvcId++ )
								{
									try
									{
										Camera.getCameraInfo( p_CameraDvcId, p_CameraInfoPt );
									}
									catch( Exception e )
									{
										String p_InfoStrPt = "媒体处理线程：获取视频输入设备 " + p_CameraDvcId + " 的信息失败。原因：" + e.getMessage();
										if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
										if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
										break OutMediaInitAndPocs;
									}
									if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
									{
										if( m_VdoInptPt.m_UseWhatVdoInptDvc == 0 ) break;
									}
									else if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_BACK )
									{
										if( m_VdoInptPt.m_UseWhatVdoInptDvc == 1 ) break;
									}
								}
								if( p_CameraDvcId == Camera.getNumberOfCameras() )
								{
									String p_InfoStrPt = "媒体处理线程：查找视频输入设备对应的ID失败。原因：没有" + ( ( m_VdoInptPt.m_UseWhatVdoInptDvc == 0 ) ? "前置摄像头。" : "后置摄像头。" );
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
									break OutMediaInitAndPocs;
								}
							}

							//打开视频输入设备。
							try
							{
								m_VdoInptPt.m_VdoInptDvcPt = Camera.open( p_CameraDvcId );
							}
							catch( RuntimeException e )
							{
								String p_InfoStrPt = "媒体处理线程：创建并初始化视频输入设备失败。原因：打开视频输入设备失败。原因：" + e.getMessage();
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
								break OutMediaInitAndPocs;
							}
						}

						Camera.Parameters p_CameraParaPt = m_VdoInptPt.m_VdoInptDvcPt.getParameters(); //获取视频输入设备的参数。

						p_CameraParaPt.setPreviewFormat( ImageFormat.NV21 ); //设置预览帧的格式。

						p_CameraParaPt.setPreviewFrameRate( m_VdoInptPt.m_MaxSmplRate ); //设置最大采样频率。

						//选择合适的视频输入设备帧大小。
						int p_VdoInptTargetFrmWidth = m_VdoInptPt.m_FrmHeight; //存放视频输入目标帧的宽度，单位为像素。
						int p_VdoInptTargetFrmHeight = m_VdoInptPt.m_FrmWidth; //存放视频输入目标帧的高度，单位为像素。
						double p_TargetFrmWidthToHeightRatio = ( double )p_VdoInptTargetFrmWidth / ( double )p_VdoInptTargetFrmHeight; //存放目标帧的宽高比。
						List< Camera.Size > p_SupportedPrvwSizesListPt = p_CameraParaPt.getSupportedPreviewSizes(); //设置视频输入设备支持的预览帧大小。
						Camera.Size p_CameraSizePt; //存放本次的帧大小。
						double p_VdoInptDvcFrmWidthToHeightRatio; //存放本次视频输入设备帧的宽高比。
						int p_VdoInptDvcFrmCropX; //存放本次视频输入设备帧裁剪区域左上角的横坐标，单位像素。
						int p_VdoInptDvcFrmCropY; //存放本次视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
						int p_VdoInptDvcFrmCropWidth; //存放本次视频输入设备帧裁剪区域的宽度，单位像素。
						int p_VdoInptDvcFrmCropHeight; //存放本次视频输入设备帧裁剪区域的高度，单位像素。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备目标的帧大小：width：" + p_VdoInptTargetFrmWidth + " height：" + p_VdoInptTargetFrmHeight );
						for( p_TmpInt321 = 0; p_TmpInt321 < p_SupportedPrvwSizesListPt.size(); p_TmpInt321++ )
						{
							p_CameraSizePt = p_SupportedPrvwSizesListPt.get( p_TmpInt321 );
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的帧大小：width：" + p_CameraSizePt.width + " height：" + p_CameraSizePt.height );

							//设置本次视频输入设备帧的宽高比、裁剪宽度、裁剪高度。
							p_VdoInptDvcFrmWidthToHeightRatio = ( double )p_CameraSizePt.width / ( double )p_CameraSizePt.height;
							if( p_VdoInptDvcFrmWidthToHeightRatio >= p_TargetFrmWidthToHeightRatio ) //如果本次视频输入设备帧的宽高比目标帧的大，就表示需要裁剪宽度。
							{
								p_VdoInptDvcFrmCropWidth = ( int )( ( double )p_CameraSizePt.height * p_TargetFrmWidthToHeightRatio ); //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
								p_VdoInptDvcFrmCropWidth -= p_VdoInptDvcFrmCropWidth % 2;
								p_VdoInptDvcFrmCropHeight = p_CameraSizePt.height; //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。

								p_VdoInptDvcFrmCropX = ( p_CameraSizePt.width - p_VdoInptDvcFrmCropWidth ) / 2; //设置本次视频输入设备帧裁剪区域左上角的横坐标，使裁剪区域居中。
								p_VdoInptDvcFrmCropX -= p_VdoInptDvcFrmCropX % 2;
								p_VdoInptDvcFrmCropY = 0; //设置本次视频输入设备帧裁剪区域左上角的纵坐标。
							}
							else //如果本次视频输入设备帧的宽高比指定帧的小，就表示需要裁剪高度。
							{
								p_VdoInptDvcFrmCropWidth = p_CameraSizePt.width; //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
								p_VdoInptDvcFrmCropHeight = ( int )( ( double )p_CameraSizePt.width / p_TargetFrmWidthToHeightRatio ); //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。
								p_VdoInptDvcFrmCropHeight -= p_VdoInptDvcFrmCropHeight % 2;

								p_VdoInptDvcFrmCropX = 0; //设置本次视频输入设备帧裁剪区域左上角的横坐标。
								p_VdoInptDvcFrmCropY = ( p_CameraSizePt.height - p_VdoInptDvcFrmCropHeight ) / 2; //设置本次视频输入设备帧裁剪区域左上角的纵坐标，使裁剪区域居中。
								p_VdoInptDvcFrmCropY -= p_VdoInptDvcFrmCropY % 2;
							}

							//如果选择的帧裁剪区域不满足指定的（包括选择的帧裁剪区域为0），但是本次的帧裁剪区域比选择的高，就设置选择的为本次的。
							//如果本次的帧裁剪区域满足指定的（选择的帧裁剪区域肯定也满足指定的，如果选择的帧裁剪区域不满足指定的，那么就会走上一条判断），但是本次的帧裁剪区域比选择的低，就设置选择的为本次的。
							if(
									(
											( ( m_VdoInptPt.m_VdoInptDvcFrmCropWidth < p_VdoInptTargetFrmWidth ) || ( m_VdoInptPt.m_VdoInptDvcFrmCropHeight < p_VdoInptTargetFrmHeight ) )
													&&
													( ( p_VdoInptDvcFrmCropWidth > m_VdoInptPt.m_VdoInptDvcFrmCropWidth ) && ( p_VdoInptDvcFrmCropHeight > m_VdoInptPt.m_VdoInptDvcFrmCropHeight ) )
									)
											||
											(
													( ( p_VdoInptDvcFrmCropWidth >= p_VdoInptTargetFrmWidth ) && ( p_VdoInptDvcFrmCropHeight >= p_VdoInptTargetFrmHeight ) )
															&&
															(
																	( ( p_VdoInptDvcFrmCropWidth < m_VdoInptPt.m_VdoInptDvcFrmCropWidth ) || ( p_VdoInptDvcFrmCropHeight < m_VdoInptPt.m_VdoInptDvcFrmCropHeight ) )
																			||
																			( ( p_VdoInptDvcFrmCropWidth == m_VdoInptPt.m_VdoInptDvcFrmCropWidth ) && ( p_VdoInptDvcFrmCropHeight == m_VdoInptPt.m_VdoInptDvcFrmCropHeight ) && ( p_VdoInptDvcFrmCropX + p_VdoInptDvcFrmCropY < m_VdoInptPt.m_VdoInptDvcFrmCropX + m_VdoInptPt.m_VdoInptDvcFrmCropY ) )
															)
											)
							)
							{
								m_VdoInptPt.m_VdoInptDvcFrmWidth = p_CameraSizePt.width;
								m_VdoInptPt.m_VdoInptDvcFrmHeight = p_CameraSizePt.height;

								m_VdoInptPt.m_VdoInptDvcFrmCropX = p_VdoInptDvcFrmCropX;
								m_VdoInptPt.m_VdoInptDvcFrmCropY = p_VdoInptDvcFrmCropY;
								m_VdoInptPt.m_VdoInptDvcFrmCropWidth = p_VdoInptDvcFrmCropWidth;
								m_VdoInptPt.m_VdoInptDvcFrmCropHeight = p_VdoInptDvcFrmCropHeight;
							}
						}
						p_CameraParaPt.setPreviewSize( m_VdoInptPt.m_VdoInptDvcFrmWidth, m_VdoInptPt.m_VdoInptDvcFrmHeight ); //设置预览帧的宽度为设置的高度，预览帧的高度为设置的宽度，因为预览帧处理的时候要旋转。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备选择的帧大小：width：" + m_VdoInptPt.m_VdoInptDvcFrmWidth + " height：" + m_VdoInptPt.m_VdoInptDvcFrmHeight );

						//判断视频输入设备帧是否裁剪。
						if(
								( m_VdoInptPt.m_VdoInptDvcFrmWidth > m_VdoInptPt.m_VdoInptDvcFrmCropWidth ) //如果视频输入设备帧的宽度比裁剪宽度大，就表示需要裁剪宽度。
										||
										( m_VdoInptPt.m_VdoInptDvcFrmHeight > m_VdoInptPt.m_VdoInptDvcFrmCropHeight ) //如果视频输入设备帧的高度比裁剪高度大，就表示需要裁剪高度。
						)
						{
							m_VdoInptPt.m_VdoInptDvcFrmIsCrop = 1; //设置视频输入设备帧要裁剪。
						}
						else //如果视频输入设备帧的宽度和高度与裁剪宽度和高度一致，就表示不需要裁剪。
						{
							m_VdoInptPt.m_VdoInptDvcFrmIsCrop = 0; //设置视频输入设备帧不裁剪。
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否裁剪：" + m_VdoInptPt.m_VdoInptDvcFrmIsCrop + "  左上角的横坐标：" + m_VdoInptPt.m_VdoInptDvcFrmCropX + "  纵坐标：" + m_VdoInptPt.m_VdoInptDvcFrmCropY + "  裁剪区域的宽度：" + m_VdoInptPt.m_VdoInptDvcFrmCropWidth + "  高度：" + m_VdoInptPt.m_VdoInptDvcFrmCropHeight + "。" );

						//设置视频输入设备帧的旋转。
						if( m_VdoInptPt.m_UseWhatVdoInptDvc == 0 ) //如果要使用前置摄像头。
						{
							m_VdoInptPt.m_VdoInptDvcFrmRotate = ( 270 + m_VdoInptPt.m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
						}
						else //如果要使用后置摄像头。
						{
							m_VdoInptPt.m_VdoInptDvcFrmRotate = ( 450 - m_VdoInptPt.m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
						}
						if( ( m_VdoInptPt.m_VdoInptDvcFrmRotate == 0 ) || ( m_VdoInptPt.m_VdoInptDvcFrmRotate == 180 ) ) //如果旋转后为横屏。
						{
							m_VdoInptPt.m_VdoInptDvcFrmRotateWidth = m_VdoInptPt.m_VdoInptDvcFrmCropWidth; //设置视频输入设备帧旋转后的宽度。
							m_VdoInptPt.m_VdoInptDvcFrmRotateHeight = m_VdoInptPt.m_VdoInptDvcFrmCropHeight; //设置视频输入设备帧旋转后的高度。
						}
						else //如果旋转后为竖屏。
						{
							m_VdoInptPt.m_VdoInptDvcFrmRotateWidth = m_VdoInptPt.m_VdoInptDvcFrmCropHeight; //设置视频输入设备帧旋转后的宽度。
							m_VdoInptPt.m_VdoInptDvcFrmRotateHeight = m_VdoInptPt.m_VdoInptDvcFrmCropWidth; //设置视频输入设备帧旋转后的高度。
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧旋转后的宽度：" + m_VdoInptPt.m_VdoInptDvcFrmRotateWidth + "，旋转后的高度：" + m_VdoInptPt.m_VdoInptDvcFrmRotateHeight + "。" );

						//判断视频输入设备帧是否缩放。
						if( ( m_VdoInptPt.m_VdoInptDvcFrmCropWidth != p_VdoInptTargetFrmWidth ) || ( m_VdoInptPt.m_VdoInptDvcFrmCropHeight != p_VdoInptTargetFrmHeight ) )
						{
							m_VdoInptPt.m_VdoInptDvcFrmIsScale = 1; //设置视频输入设备帧要缩放。
						}
						else
						{
							m_VdoInptPt.m_VdoInptDvcFrmIsScale = 0; //设置视频输入设备帧不缩放。
						}
						if( ( m_VdoInptPt.m_VdoInptDvcFrmRotate == 0 ) || ( m_VdoInptPt.m_VdoInptDvcFrmRotate == 180 ) ) //如果旋转后为横屏。
						{
							m_VdoInptPt.m_VdoInptDvcFrmScaleWidth = p_VdoInptTargetFrmWidth; //设置视频输入设备帧缩放后的宽度。
							m_VdoInptPt.m_VdoInptDvcFrmScaleHeight = p_VdoInptTargetFrmHeight; //设置视频输入设备帧缩放后的高度。
						}
						else //如果旋转后为竖屏。
						{
							m_VdoInptPt.m_VdoInptDvcFrmScaleWidth = p_VdoInptTargetFrmHeight; //设置视频输入设备帧缩放后的宽度。
							m_VdoInptPt.m_VdoInptDvcFrmScaleHeight = p_VdoInptTargetFrmWidth; //设置视频输入设备帧缩放后的高度。
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否缩放：" + m_VdoInptPt.m_VdoInptDvcFrmIsScale + "，缩放后的宽度：" + m_VdoInptPt.m_VdoInptDvcFrmScaleWidth + "，缩放后的高度：" + m_VdoInptPt.m_VdoInptDvcFrmScaleHeight + "。" );

						//设置视频输入设备的对焦模式。
						List<String> p_FocusModesListPt = p_CameraParaPt.getSupportedFocusModes();
						String p_PrvwFocusModePt = "";
						for( p_TmpInt321 = 0; p_TmpInt321 < p_FocusModesListPt.size(); p_TmpInt321++ )
						{
							switch( p_FocusModesListPt.get( p_TmpInt321 ) )
							{
								case Camera.Parameters.FOCUS_MODE_AUTO: //自动对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式启动焦点。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_AUTO，自动对焦模式。" );
									break;
								case Camera.Parameters.FOCUS_MODE_MACRO: //微距（特写）对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式开始聚焦。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_MACRO，微距（特写）对焦模式。" );
									break;
								case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO: //用于视频的连续自动对焦模式。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_VIDEO，用于视频的连续自动对焦模式。" );
									p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
									break;
								case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE: //用于拍照的连续自动对焦模式，比视频的连续自动对焦模式对焦速度更快。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_PICTURE，用于拍照的连续自动对焦模式。" );
									if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) )
										p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
									break;
								case Camera.Parameters.FOCUS_MODE_EDOF: //扩展景深（EDOF）对焦模式，对焦以数字方式连续进行。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_EDOF，扩展景深（EDOF）对焦模式。" );
									if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
										p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_EDOF;
									break;
								case Camera.Parameters.FOCUS_MODE_FIXED: //固定焦点对焦模式。如果焦点无法调节，则相机始终处于此模式。如果相机具有自动对焦，则此模式可以固定焦点，通常处于超焦距。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_FIXED，固定焦点对焦模式。" );
									if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) )
										p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_FIXED;
									break;
								case Camera.Parameters.FOCUS_MODE_INFINITY: //无限远焦点对焦模式。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_INFINITY，无限远焦点对焦模式。" );
									if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) &&
											!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_FIXED ) )
										p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_INFINITY;
									break;
							}
						}
						p_CameraParaPt.setFocusMode( p_PrvwFocusModePt ); //设置对焦模式。

						try
						{
							m_VdoInptPt.m_VdoInptDvcPt.setParameters( p_CameraParaPt ); //设置参数到视频输入设备。
						}
						catch( RuntimeException e )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage() );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, "媒体处理线程：创建并初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage(), Toast.LENGTH_LONG ).show(); } } );
							break OutMediaInitAndPocs;
						}

						try
						{
							m_VdoInptPt.m_VdoInptDvcPt.setPreviewDisplay( m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt.getHolder() ); //设置视频输入预览SurfaceView。
							if( m_VdoInptPt.m_ScreenRotate == 0 || m_VdoInptPt.m_ScreenRotate == 180 ) //如果屏幕为竖屏。
							{
								m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoInptPt.m_VdoInptDvcFrmHeight / m_VdoInptPt.m_VdoInptDvcFrmWidth ); //设置视频输入预览SurfaceView的宽高比。
							}
							else //如果屏幕为横屏。
							{
								m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoInptPt.m_VdoInptDvcFrmWidth / m_VdoInptPt.m_VdoInptDvcFrmHeight ); //设置视频输入预览SurfaceView的宽高比。
							}
						}
						catch( Exception ignored )
						{
						}
						m_VdoInptPt.m_VdoInptDvcPt.setDisplayOrientation( ( 450 - m_VdoInptPt.m_ScreenRotate ) % 360 ); //调整相机拍到的图像旋转，不然竖着拿手机，图像是横着的。

						//设置视频输入预览回调函数缓冲区的指针。
						m_VdoInptPt.m_VdoInptPrvwClbkBufPtPt = new byte[ m_VdoInptPt.m_MaxSmplRate ][ m_VdoInptPt.m_VdoInptDvcFrmWidth * m_VdoInptPt.m_VdoInptDvcFrmHeight * 3 / 2 ];
						for( p_TmpInt321 = 0; p_TmpInt321 < m_VdoInptPt.m_MaxSmplRate; p_TmpInt321++ )
							m_VdoInptPt.m_VdoInptDvcPt.addCallbackBuffer( m_VdoInptPt.m_VdoInptPrvwClbkBufPtPt[p_TmpInt321] );

						m_VdoInptPt.m_VdoInptDvcPt.setPreviewCallbackWithBuffer( m_VdoInptPt.m_VdoInptThrdPt ); //设置视频输入预览回调函数。

						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备成功。" );
					}

					//初始化编码器对象。
					switch( m_VdoInptPt.m_UseWhatEncd )
					{
						case 0: //如果要使用YU12原始数据。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用YU12原始数据。" );
							break;
						}
						case 1: //如果要使用OpenH264编码器。
						{
							m_VdoInptPt.m_OpenH264EncdPt = new OpenH264Encd();
							if( m_VdoInptPt.m_OpenH264EncdPt.Init( m_VdoInptPt.m_VdoInptDvcFrmScaleWidth, m_VdoInptPt.m_VdoInptDvcFrmScaleHeight, m_VdoInptPt.m_OpenH264EncdVdoType, m_VdoInptPt.m_OpenH264EncdEncdBitrate, m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode, m_VdoInptPt.m_MaxSmplRate, m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl, m_VdoInptPt.m_OpenH264EncdCmplxt, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器成功。" );
							}
							else
							{
								m_VdoInptPt.m_OpenH264EncdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 2: //如果要使用系统自带H264编码器。
						{
							m_VdoInptPt.m_SystemH264EncdPt = new SystemH264Encd();
							if( m_VdoInptPt.m_SystemH264EncdPt.Init( m_VdoInptPt.m_VdoInptDvcFrmScaleWidth, m_VdoInptPt.m_VdoInptDvcFrmScaleHeight, m_VdoInptPt.m_SystemH264EncdEncdBitrate, m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode, m_VdoInptPt.m_MaxSmplRate, m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec, m_VdoInptPt.m_SystemH264EncdCmplxt, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264编码器成功。" );
							}
							else
							{
								m_VdoInptPt.m_SystemH264EncdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264编码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
					}

					//初始化视频输入线程的临时变量。
					{
						m_VdoInptPt.m_LastVdoInptFrmTimeMsec = 0; //初始化上一个视频输入帧的时间。
						m_VdoInptPt.m_VdoInptFrmTimeStepMsec = 1000 / m_VdoInptPt.m_MaxSmplRate; //初始化视频输入帧的时间步进。
						m_VdoInptPt.m_VdoInptFrmPt = null; //初始化视频输入帧的指针。
						if( m_VdoInptPt.m_FrmWidth * m_VdoInptPt.m_FrmHeight >= m_VdoInptPt.m_VdoInptDvcFrmWidth * m_VdoInptPt.m_VdoInptDvcFrmHeight ) //如果视频输入帧指定的大小大于等于视频输入设备帧的大小。
						{
							m_VdoInptPt.m_VdoInptRsltFrmSz = m_VdoInptPt.m_FrmWidth * m_VdoInptPt.m_FrmHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
						}
						else //如果视频输入帧指定的大小小于视频输入设备帧的大小。
						{
							m_VdoInptPt.m_VdoInptRsltFrmSz = m_VdoInptPt.m_VdoInptDvcFrmWidth * m_VdoInptPt.m_VdoInptDvcFrmHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
						}
						m_VdoInptPt.m_VdoInptRsltFrmPt = new byte[( int )m_VdoInptPt.m_VdoInptRsltFrmSz]; //初始化视频输入结果帧的指针。
						m_VdoInptPt.m_VdoInptTmpFrmPt = new byte[( int )m_VdoInptPt.m_VdoInptRsltFrmSz]; //初始化视频输入临时帧的指针。
						m_VdoInptPt.m_VdoInptSwapFrmPt = null; //初始化视频输入交换帧的指针。
						m_VdoInptPt.m_VdoInptRsltFrmLenPt = new HTLong(); //初始化视频输入结果帧的长度。
						m_VdoInptPt.m_VdoInptFrmElmPt = null; //初始化视频输入帧元素的指针。
						m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal = 0; //初始化视频输入帧链表的元数总数。
						m_VdoInptPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
						m_VdoInptPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化视频输入线程的临时变量成功。" );
					}

					//创建并初始化NV21格式视频输入帧链表。
					m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt = new LinkedList< byte[] >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化NV21格式视频输入帧链表成功。" );

					//创建并初始化视频输入帧链表。
					m_VdoInptPt.m_VdoInptFrmLnkLstPt = new LinkedList< VdoInpt.VdoInptFrmElm >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入帧链表成功。" );

					//创建并初始化视频输入空闲帧链表。
					m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt = new LinkedList< VdoInpt.VdoInptFrmElm >();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入空闲帧链表成功。" );
				} //初始化视频输入完毕。

				//初始化视频输出。
				if( m_VdoOtptPt.m_IsUseVdoOtpt != 0 )
				{
					//初始化解码器对象。
					switch( m_VdoOtptPt.m_UseWhatDecd )
					{
						case 0: //如果要使用YU12原始数据。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用YU12原始数据。" );
							break;
						}
						case 1: //如果要使用OpenH264解码器。
						{
							m_VdoOtptPt.m_OpenH264DecdPt = new OpenH264Decd();
							if( m_VdoOtptPt.m_OpenH264DecdPt.Init( m_VdoOtptPt.m_OpenH264DecdDecdThrdNum, m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器成功。" );
							}
							else
							{
								m_VdoOtptPt.m_OpenH264DecdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								break OutMediaInitAndPocs;
							}
							break;
						}
						case 2: //如果要使用系统自带H264解码器。
						{
							m_VdoOtptPt.m_SystemH264DecdPt = new SystemH264Decd();
							if( m_VdoOtptPt.m_SystemH264DecdPt.Init( null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264解码器成功。" );
							}
							else
							{
								m_VdoOtptPt.m_SystemH264DecdPt = null;
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264解码器失败。" );
								break OutMediaInitAndPocs;
							}
							break;
						}
					}

					//初始化视频输出线程的临时变量。
					{
						m_VdoOtptPt.m_VdoOtptRsltFrmPt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出结果帧的指针。
						m_VdoOtptPt.m_VdoOtptTmpFrmPt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出临时帧的指针。
						m_VdoOtptPt.m_VdoOtptSwapFrmPt = null; //初始化视频输出交换帧的指针。
						m_VdoOtptPt.m_VdoOtptRsltFrmLenPt = new HTLong(); //初始化视频输出结果帧的长度。
						m_VdoOtptPt.m_VdoOtptFrmWidthPt = new HTInt(); //初始化视频输出帧的宽度。
						m_VdoOtptPt.m_VdoOtptFrmHeightPt = new HTInt(); //初始化视频输出帧的高度。
						m_VdoOtptPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
						m_VdoOtptPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化视频输出线程的临时变量成功。" );
					}

					//创建视频输出线程。
					m_VdoOtptPt.m_VdoOtptThrdExitFlag = 0; //设置视频输出线程退出标记为0表示保持运行。
					m_VdoOtptPt.m_VdoOtptThrdPt = new VdoOtptThrd();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输出线程成功。" );
				} //初始化视频输出完毕。

				//初始化媒体处理线程的临时变量。
				{
					m_PcmAdoInptFrmPt = null;
					m_PcmAdoOtptFrmPt = null;
					m_PcmAdoRsltFrmPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 ) ? new short[ m_AdoInptPt.m_FrmLen ] : null;
					m_PcmAdoTmpFrmPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 ) ? new short[ m_AdoInptPt.m_FrmLen ] : null;
					m_PcmAdoSwapFrmPt = null;
					m_VoiceActStsPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 ) ? new HTInt( 1 ) : null; //语音活动状态预设为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
					m_EncdAdoInptFrmPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 && m_AdoInptPt.m_UseWhatEncd != 0 ) ? new byte[ m_AdoInptPt.m_FrmLen ] : null;
					m_EncdAdoInptFrmLenPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 && m_AdoInptPt.m_UseWhatEncd != 0 ) ? new HTLong( 0 ) : null;
					m_EncdAdoInptFrmIsNeedTransPt = ( m_AdoInptPt.m_IsUseAdoInpt != 0 && m_AdoInptPt.m_UseWhatEncd != 0 ) ? new HTInt( 1 ) : null; //已编码格式音频输入帧是否需要传输预设为1，为了让在不使用非连续传输的情况下永远都是需要传输。
					m_VdoInptFrmPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化媒体处理线程的临时变量成功。" );
				} //初始化媒体处理线程的临时变量完毕。

				//启动音频输入线程、音频输出线程、视频输入线程、视频输出线程。必须在初始化最后启动这些线程，因为这些线程会使用初始化时的相关。
				{
					if( m_AdoInptPt.m_AdoInptThrdPt != null ) //如果要使用音频输入线程。
					{
						m_AdoInptPt.m_AdoInptThrdPt.start(); //启动音频输入线程，让音频输入线程再去启动音频输出线程。
					}
					else if( m_AdoOtptPt.m_AdoOtptDvcPt != null ) //如果要使用音频输出线程。
					{
						m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
						m_AdoOtptPt.m_AdoOtptThrdPt.start(); //启动音频输出线程。
					}

					if( m_VdoInptPt.m_VdoInptDvcPt != null ) //如果要使用视频输入设备。
					{
						try
						{
							m_VdoInptPt.m_VdoInptDvcPt.startPreview(); //让视频输入设备开始预览。
						}
						catch( RuntimeException e )
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：让视频输入设备开始预览失败。原因：" + e.getMessage() );
							break OutMediaInitAndPocs;
						}
						m_VdoInptPt.m_VdoInptThrdPt.start(); //启动视频输入线程。
					}

					if( m_VdoOtptPt.m_VdoOtptThrdPt != null ) //如果要使用视频输出线程。
					{
						m_VdoOtptPt.m_VdoOtptThrdPt.start(); //启动视频输出线程。
					}
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowMsec = System.currentTimeMillis();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：媒体处理线程初始化完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，正式开始处理帧。" );
				}

				m_ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。
				m_RunFlag = RUN_FLAG_POCS; //设置本线程运行标记为初始化完毕正在循环处理帧。

				//开始音视频输入输出帧处理循环。
				while( true )
				{
					if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

					//调用用户定义的处理函数。
					p_TmpInt321 = UserPocs();
					if( p_TmpInt321 == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt321 );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt321 );
						break OutMediaInitAndPocs;
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//取出音频输入帧和音频输出帧。
					if( m_AdoInptPt.m_AdoInptFrmLnkLstPt != null ) p_TmpInt321 = m_AdoInptPt.m_AdoInptFrmLnkLstPt.size(); //获取音频输入帧链表的元素个数。
					else p_TmpInt321 = 0;
					if( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt != null ) p_TmpInt322 = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size(); //获取音频输出帧链表的元素个数。
					else p_TmpInt322 = 0;
					if( ( p_TmpInt321 > 0 ) && ( p_TmpInt322 > 0 ) ) //如果音频输入帧链表和音频输出帧链表中都有帧了，才开始取出。
					{
						//从音频输入帧链表中取出第一个音频输入帧。
						synchronized( m_AdoInptPt.m_AdoInptFrmLnkLstPt )
						{
							m_PcmAdoInptFrmPt = m_AdoInptPt.m_AdoInptFrmLnkLstPt.getFirst();
							m_AdoInptPt.m_AdoInptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

						//从音频输出帧链表中取出第一个音频输出帧。
						synchronized( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt )
						{
							m_PcmAdoOtptFrmPt = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.getFirst();
							m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );

						//将音频输入帧复制到音频结果帧，方便处理。
						System.arraycopy( m_PcmAdoInptFrmPt, 0, m_PcmAdoRsltFrmPt, 0, m_PcmAdoInptFrmPt.length );
					}
					else if( ( p_TmpInt321 > 0 ) && ( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt == null ) ) //如果音频输入帧链表有帧了，且不使用音频输出帧链表，就开始取出。
					{
						//从音频输入帧链表中取出第一个音频输入帧。
						synchronized( m_AdoInptPt.m_AdoInptFrmLnkLstPt )
						{
							m_PcmAdoInptFrmPt = m_AdoInptPt.m_AdoInptFrmLnkLstPt.getFirst();
							m_AdoInptPt.m_AdoInptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

						//将音频输入帧复制到音频结果帧，方便处理。
						System.arraycopy( m_PcmAdoInptFrmPt, 0, m_PcmAdoRsltFrmPt, 0, m_PcmAdoInptFrmPt.length );
					}
					else if( ( p_TmpInt322 > 0 ) && ( m_AdoInptPt.m_AdoInptFrmLnkLstPt == null ) ) //如果音频输出帧链表有帧了，且不使用音频输入帧链表，就开始取出。
					{
						//从音频输出帧链表中取出第一个音频输出帧。
						synchronized( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt )
						{
							m_PcmAdoOtptFrmPt = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.getFirst();
							m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );
					}

					//处理音频输入帧。
					if( m_PcmAdoInptFrmPt != null )
					{
						//使用声学回音消除器。
						switch( m_AdoInptPt.m_UseWhatAec )
						{
							case 0: //如果不使用声学回音消除器。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用声学回音消除器。" );
								break;
							}
							case 1: //如果要使用Speex声学回音消除器。
							{
								if( ( m_AdoInptPt.m_SpeexAecPt != null ) && ( m_AdoInptPt.m_SpeexAecPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoOtptFrmPt, m_PcmAdoTmpFrmPt ) == 0 ) )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器失败。" );
								}
								break;
							}
							case 2: //如果要使用WebRtc定点版声学回音消除器。
							{
								if( ( m_AdoInptPt.m_WebRtcAecmPt != null ) && ( m_AdoInptPt.m_WebRtcAecmPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoOtptFrmPt, m_PcmAdoTmpFrmPt ) == 0 ) )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器失败。" );
								}
								break;
							}
							case 3: //如果要使用WebRtc浮点版声学回音消除器。
							{
								if( ( m_AdoInptPt.m_WebRtcAecPt != null ) && ( m_AdoInptPt.m_WebRtcAecPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoOtptFrmPt, m_PcmAdoTmpFrmPt ) == 0 ) )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器失败。" );
								}
								break;
							}
							case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
							{
								if( ( m_AdoInptPt.m_SpeexWebRtcAecPt != null ) && ( m_AdoInptPt.m_SpeexWebRtcAecPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoOtptFrmPt, m_PcmAdoTmpFrmPt ) == 0 ) )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器失败。" );
								}
								break;
							}
						}

						//使用噪音抑制器。
						switch( m_AdoInptPt.m_UseWhatNs )
						{
							case 0: //如果不使用噪音抑制器。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用噪音抑制器。" );
								break;
							}
							case 1: //如果要使用Speex预处理器的噪音抑制。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：稍后在使用Speex预处理器时一起使用噪音抑制。" );
								break;
							}
							case 2: //如果要使用WebRtc定点版噪音抑制器。
							{
								if( m_AdoInptPt.m_WebRtcNsxPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoTmpFrmPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器失败。" );
								}
								break;
							}
							case 3: //如果要使用WebRtc浮点版噪音抑制器。
							{
								if( m_AdoInptPt.m_WebRtcNsPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoTmpFrmPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器失败。" );
								}
								break;
							}
							case 4: //如果要使用RNNoise噪音抑制器。
							{
								if( m_AdoInptPt.m_RNNoisePt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoTmpFrmPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器成功。" );
									m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器失败。" );
								}
								break;
							}
						}

						//使用Speex预处理器。
						if( ( m_AdoInptPt.m_UseWhatNs == 1 ) || ( m_AdoInptPt.m_IsUseSpeexPrpocsOther != 0 ) )
						{
							if( m_AdoInptPt.m_SpeexPrpocsPt.Pocs( m_PcmAdoRsltFrmPt, m_PcmAdoTmpFrmPt, m_VoiceActStsPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器成功。语音活动状态：" + m_VoiceActStsPt.m_Val );
								m_PcmAdoSwapFrmPt = m_PcmAdoRsltFrmPt;m_PcmAdoRsltFrmPt = m_PcmAdoTmpFrmPt;m_PcmAdoTmpFrmPt = m_PcmAdoSwapFrmPt; //交换音频结果帧和音频临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器失败。" );
							}
						}

						//判断音频输入是否静音。在音频输入处理完后再设置静音，这样可以保证音频输入处理器的连续性。
						if( m_AdoInptPt.m_AdoInptIsMute != 0 )
						{
							Arrays.fill( m_PcmAdoRsltFrmPt, ( short ) 0 );
							if( ( m_AdoInptPt.m_IsUseSpeexPrpocsOther != 0 ) && ( m_AdoInptPt.m_SpeexPrpocsIsUseVad != 0 ) ) //如果Speex预处理器要使用其他功能，且要使用语音活动检测。
							{
								m_VoiceActStsPt.m_Val = 0;
							}
						}

						//使用编码器。
						switch( m_AdoInptPt.m_UseWhatEncd )
						{
							case 0: //如果要使用PCM原始数据。
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
								break;
							}
							case 1: //如果要使用Speex编码器。
							{
								if( m_AdoInptPt.m_SpeexEncdPt.Pocs( m_PcmAdoRsltFrmPt, m_EncdAdoInptFrmPt, m_EncdAdoInptFrmPt.length, m_EncdAdoInptFrmLenPt, m_EncdAdoInptFrmIsNeedTransPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器成功。Speex格式音频输入帧的长度：" + m_EncdAdoInptFrmLenPt.m_Val + "，Speex格式音频输入帧是否需要传输：" + m_EncdAdoInptFrmIsNeedTransPt.m_Val );
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器失败。" );
								}
								break;
							}
							case 2: //如果要使用Opus编码器。
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus编码器。" );
								break OutMediaInitAndPocs;
							}
						}

						//使用音频输入Wave文件写入器写入音频输入帧数据、音频结果Wave文件写入器写入音频结果帧数据。
						if( m_AdoInptPt.m_IsSaveAdoToFile != 0 )
						{
							if( m_AdoInptPt.m_AdoInptWaveFileWriterPt.WriteData( m_PcmAdoInptFrmPt, m_PcmAdoInptFrmPt.length ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧失败。" );
							}
							if( m_AdoInptPt.m_AdoRsltWaveFileWriterPt.WriteData( m_PcmAdoRsltFrmPt, m_PcmAdoRsltFrmPt.length ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧失败。" );
							}
						}

						//使用音频输入波形器绘制音频输入波形到Surface、音频结果波形器绘制音频结果波形到Surface。
						if( m_AdoInptPt.m_IsDrawAdoWavfmToSurface != 0 )
						{
							if( m_AdoInptPt.m_AdoInptOscilloPt.Draw( m_PcmAdoInptFrmPt, m_PcmAdoInptFrmPt.length, m_AdoInptPt.m_AdoInptOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							}
							if( m_AdoInptPt.m_AdoRsltOscilloPt.Draw( m_PcmAdoRsltFrmPt, m_PcmAdoRsltFrmPt.length, m_AdoInptPt.m_AdoRsltOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频结果波形器绘制音频结果波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频结果波形器绘制音频结果波形到Surface失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							}
						}
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：处理音频输入帧完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//处理音频输出帧。
					if( m_PcmAdoOtptFrmPt != null )
					{
						//使用音频输出Wave文件写入器写入输出帧数据。
						if( m_AdoOtptPt.m_IsSaveAdoToFile != 0 )
						{
							if( m_AdoOtptPt.m_AdoOtptWaveFileWriterPt.WriteData( m_PcmAdoOtptFrmPt, m_PcmAdoOtptFrmPt.length ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧失败。" );
							}
						}

						//使用音频输出波形器绘制音频输出波形到Surface。
						if( m_AdoOtptPt.m_IsDrawAdoWavfmToSurface != 0 )
						{
							if( m_AdoOtptPt.m_AdoOtptOscilloPt.Draw( m_PcmAdoOtptFrmPt, m_PcmAdoOtptFrmPt.length, m_AdoOtptPt.m_AdoOtptOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出波形器绘制音频输入波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出波形器绘制音频输出波形到Surface失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
							}
						}
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：处理音频输出帧完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//处理视频输入帧。
					if( ( m_VdoInptPt.m_VdoInptFrmLnkLstPt != null ) && ( ( p_TmpInt321 = m_VdoInptPt.m_VdoInptFrmLnkLstPt.size() ) > 0 ) && //如果要使用视频输入，且视频输入帧链表中有帧了。
							( ( m_PcmAdoInptFrmPt != null ) || ( m_AdoInptPt.m_AdoInptFrmLnkLstPt == null ) ) ) //且已经处理了音频输入帧或不使用音频输入帧链表。
					{
						//从视频输入帧链表中取出第一个视频输入帧。
						synchronized( m_VdoInptPt.m_VdoInptFrmLnkLstPt )
						{
							m_VdoInptFrmPt = m_VdoInptPt.m_VdoInptFrmLnkLstPt.getFirst();
							m_VdoInptPt.m_VdoInptFrmLnkLstPt.removeFirst();
						}
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输入帧链表中取出第一个视频输入帧，视频输入帧链表元素个数：" + p_TmpInt321 + "。" );
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：处理视频输入帧完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//调用用户定义的读取音视频输入帧函数。
					if( ( m_PcmAdoInptFrmPt != null ) || ( m_VdoInptFrmPt != null ) ) //如果取出了音频输入帧或视频输入帧。
					{
						if( m_VdoInptFrmPt != null ) //如果取出了视频输入帧。
							p_TmpInt321 = UserReadAdoVdoInptFrm( m_PcmAdoInptFrmPt, m_PcmAdoRsltFrmPt, m_VoiceActStsPt, m_EncdAdoInptFrmPt, m_EncdAdoInptFrmLenPt, m_EncdAdoInptFrmIsNeedTransPt, m_VdoInptFrmPt.m_YU12VdoInptFrmPt, m_VdoInptFrmPt.m_YU12VdoInptFrmWidthPt, m_VdoInptFrmPt.m_YU12VdoInptFrmHeightPt, m_VdoInptFrmPt.m_EncdVdoInptFrmPt, m_VdoInptFrmPt.m_EncdVdoInptFrmLenPt );
						else
							p_TmpInt321 = UserReadAdoVdoInptFrm( m_PcmAdoInptFrmPt, m_PcmAdoRsltFrmPt, m_VoiceActStsPt, m_EncdAdoInptFrmPt, m_EncdAdoInptFrmLenPt, m_EncdAdoInptFrmIsNeedTransPt, null, null, null, null, null );
						if( p_TmpInt321 == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数成功。返回值：" + p_TmpInt321 );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数失败。返回值：" + p_TmpInt321 );
							break OutMediaInitAndPocs;
						}
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					if( m_PcmAdoInptFrmPt != null ) //追加本次音频输入帧到音频输入空闲帧链表。
					{
						synchronized( m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt )
						{
							m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt.addLast( m_PcmAdoInptFrmPt );
						}
						m_PcmAdoInptFrmPt = null; //清空PCM格式音频输入帧。
					}
					if( m_PcmAdoOtptFrmPt != null ) //追加本次音频输出帧到音频输出空闲帧链表。
					{
						synchronized( m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt )
						{
							m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt.addLast( m_PcmAdoOtptFrmPt );
						}
						m_PcmAdoOtptFrmPt = null; //清空PCM格式音频输出帧。
					}
					if( m_VdoInptFrmPt != null ) //追加本次视频输入帧到视频输入空闲帧链表。
					{
						synchronized( m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt )
						{
							m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.addLast( m_VdoInptFrmPt );
						}
						m_VdoInptFrmPt = null; //清空视频输入帧。
					}

					if( m_ExitFlag != 0 ) //如果本线程退出标记为请求退出。
					{
						m_ExitCode = 0; //处理已经成功了，再将本线程退出代码设置为正常退出。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求，开始准备退出。" );
						break OutMediaInitAndPocs;
					}

					SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
				} //音视频输入输出帧处理循环完毕。
			}

			m_RunFlag = RUN_FLAG_DSTOY; //设置本线程运行标记为跳出循环处理帧正在销毁。
			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程开始退出。" );

			//请求音视频输入输出线程退出。必须在销毁音视频输入输出前退出，因为音视频输入输出线程会使用音视频输入输出相关。
			if( m_AdoInptPt.m_AdoInptThrdPt != null ) m_AdoInptPt.m_AdoInptThrdExitFlag = 1; //请求音频输入线程退出。
			if( m_AdoOtptPt.m_AdoOtptThrdPt != null ) m_AdoOtptPt.m_AdoOtptThrdExitFlag = 1; //请求音频输出线程退出。
			if( m_VdoInptPt.m_VdoInptThrdPt != null ) m_VdoInptPt.m_VdoInptThrdExitFlag = 1; //请求视频输入线程退出。
			if( m_VdoOtptPt.m_VdoOtptThrdPt != null ) m_VdoOtptPt.m_VdoOtptThrdExitFlag = 1; //请求视频输出线程退出。

			//销毁音频输入。
			{
				//销毁音频输入线程。
				if( m_AdoInptPt.m_AdoInptThrdPt != null )
				{
					try
					{
						m_AdoInptPt.m_AdoInptThrdPt.join(); //等待音频输入线程退出。
					}
					catch( InterruptedException ignored )
					{
					}
					m_AdoInptPt.m_AdoInptThrdPt = null;
					m_AdoInptPt.m_AdoInptThrdExitFlag = 0;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入线程成功。" );
				}

				//销毁音频输入空闲帧链表。
				if( m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt != null )
				{
					m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt.clear();
					m_AdoInptPt.m_AdoInptIdleFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入空闲帧链表成功。" );
				}

				//销毁音频输入帧链表。
				if( m_AdoInptPt.m_AdoInptFrmLnkLstPt != null )
				{
					m_AdoInptPt.m_AdoInptFrmLnkLstPt.clear();
					m_AdoInptPt.m_AdoInptFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入帧链表成功。" );
				}

				//销毁音频输入设备。
				if( m_AdoInptPt.m_AdoInptDvcPt != null )
				{
					if( m_AdoInptPt.m_AdoInptDvcPt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_AdoInptPt.m_AdoInptDvcPt.stop();
					m_AdoInptPt.m_AdoInptDvcPt.release();
					m_AdoInptPt.m_AdoInptDvcPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入设备成功。" );
				}

				//销毁音频输入波形器、音频结果波形器。
				if( m_AdoInptPt.m_IsDrawAdoWavfmToSurface != 0 )
				{
					if( m_AdoInptPt.m_AdoInptOscilloPt != null )
					{
						if( m_AdoInptPt.m_AdoInptOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
						}
						m_AdoInptPt.m_AdoInptOscilloPt = null;
					}
					if( m_AdoInptPt.m_AdoRsltOscilloPt != null )
					{
						if( m_AdoInptPt.m_AdoRsltOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
						}
						m_AdoInptPt.m_AdoRsltOscilloPt = null;
					}
				}

				//销毁音频输入Wave文件写入器、音频结果Wave文件写入器。
				if( m_AdoInptPt.m_IsSaveAdoToFile != 0 )
				{
					if( m_AdoInptPt.m_AdoInptWaveFileWriterPt != null )
					{
						if( m_AdoInptPt.m_AdoInptWaveFileWriterPt.Dstoy() == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器失败。" );
						}
						m_AdoInptPt.m_AdoInptWaveFileWriterPt = null;
					}
					if( m_AdoInptPt.m_AdoRsltWaveFileWriterPt != null )
					{
						if( m_AdoInptPt.m_AdoRsltWaveFileWriterPt.Dstoy() == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器失败。" );
						}
						m_AdoInptPt.m_AdoRsltWaveFileWriterPt = null;
					}
				}

				//销毁编码器。
				switch( m_AdoInptPt.m_UseWhatEncd )
				{
					case 0: //如果要使用PCM原始数据。
					{
						break;
					}
					case 1: //如果要使用Speex编码器。
					{
						if( m_AdoInptPt.m_SpeexEncdPt != null )
						{
							if( m_AdoInptPt.m_SpeexEncdPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器失败。" );
							}
							m_AdoInptPt.m_SpeexEncdPt = null;
						}
						break;
					}
					case 2: //如果要使用Opus编码器。
					{
						break;
					}
				}

				//销毁Speex预处理器。
				if( m_AdoInptPt.m_SpeexPrpocsPt != null )
				{
					if( m_AdoInptPt.m_SpeexPrpocsPt.Dstoy() == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器失败。" );
					}
					m_AdoInptPt.m_SpeexPrpocsPt = null;
				}

				//销毁噪音抑制器。
				switch( m_AdoInptPt.m_UseWhatNs )
				{
					case 0: //如果不使用噪音抑制器。
					{
						break;
					}
					case 1: //如果要使用Speex预处理器的噪音抑制。
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：之前在销毁Speex预处理器时一起销毁Speex预处理器的噪音抑制。" );
						break;
					}
					case 2: //如果要使用WebRtc定点版噪音抑制器。
					{
						if( m_AdoInptPt.m_WebRtcNsxPt != null )
						{
							if( m_AdoInptPt.m_WebRtcNsxPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器失败。" );
							}
							m_AdoInptPt.m_WebRtcNsxPt = null;
						}
						break;
					}
					case 3: //如果要使用WebRtc浮点版噪音抑制器。
					{
						if( m_AdoInptPt.m_WebRtcNsPt != null )
						{
							if( m_AdoInptPt.m_WebRtcNsPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器失败。" );
							}
							m_AdoInptPt.m_WebRtcNsPt = null;
						}
						break;
					}
					case 4: //如果要使用RNNoise噪音抑制器。
					{
						if( m_AdoInptPt.m_RNNoisePt != null )
						{
							if( m_AdoInptPt.m_RNNoisePt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器失败。" );
							}
							m_AdoInptPt.m_RNNoisePt = null;
						}
						break;
					}
				}

				//销毁声学回音消除器。
				switch( m_AdoInptPt.m_UseWhatAec )
				{
					case 0: //如果不使用声学回音消除器。
					{
						break;
					}
					case 1: //如果要使用Speex声学回音消除器。
					{
						if( m_AdoInptPt.m_SpeexAecPt != null )
						{
							if( m_AdoInptPt.m_SpeexAecIsSaveMemFile != 0 )
							{
								if( m_AdoInptPt.m_SpeexAecPt.SaveMemFile( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_SpeexAecFilterLen, m_AdoInptPt.m_SpeexAecIsUseRec, m_AdoInptPt.m_SpeexAecEchoMultiple, m_AdoInptPt.m_SpeexAecEchoCont, m_AdoInptPt.m_SpeexAecEchoSupes, m_AdoInptPt.m_SpeexAecEchoSupesAct, m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt + " 成功。" );
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt + " 失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								}
							}
							if( m_AdoInptPt.m_SpeexAecPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器失败。" );
							}
							m_AdoInptPt.m_SpeexAecPt = null;
						}
						break;
					}
					case 2: //如果要使用WebRtc定点版声学回音消除器。
					{
						if( m_AdoInptPt.m_WebRtcAecmPt != null )
						{
							if( m_AdoInptPt.m_WebRtcAecmPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器失败。" );
							}
							m_AdoInptPt.m_WebRtcAecmPt = null;
						}
						break;
					}
					case 3: //如果要使用WebRtc浮点版声学回音消除器。
					{
						if( m_AdoInptPt.m_WebRtcAecPt != null )
						{
							if( m_AdoInptPt.m_WebRtcAecIsSaveMemFile != 0 )
							{
								if( m_AdoInptPt.m_WebRtcAecPt.SaveMemFile( m_AdoInptPt.m_SmplRate, m_AdoInptPt.m_FrmLen, m_AdoInptPt.m_WebRtcAecEchoMode, m_AdoInptPt.m_WebRtcAecDelay, m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode, m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode, m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt + " 成功。" );
								}
								else
								{
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt + " 失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
								}
							}
							if( m_AdoInptPt.m_WebRtcAecPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器失败。" );
							}
							m_AdoInptPt.m_WebRtcAecPt = null;
						}
						break;
					}
					case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
					{
						if( m_AdoInptPt.m_SpeexWebRtcAecPt != null )
						{
							if( m_AdoInptPt.m_SpeexWebRtcAecPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器失败。" );
							}
							m_AdoInptPt.m_SpeexWebRtcAecPt = null;
						}
						break;
					}
				}
			} //销毁音频输入完毕。

			//销毁音频输出。
			{
				//销毁音频输出线程。
				if( m_AdoOtptPt.m_AdoOtptThrdPt != null )
				{
					try
					{
						m_AdoOtptPt.m_AdoOtptThrdPt.join(); //等待音频输出线程退出。
					}
					catch( InterruptedException ignored )
					{
					}
					m_AdoOtptPt.m_AdoOtptThrdPt = null;
					m_AdoOtptPt.m_AdoOtptThrdExitFlag = 0;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出线程成功。" );
				}

				//销毁音频输出空闲帧链表。
				if( m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt != null )
				{
					m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt.clear();
					m_AdoOtptPt.m_AdoOtptIdleFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出空闲帧链表成功。" );
				}

				//销毁音频输出帧链表。
				if( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt != null )
				{
					m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.clear();
					m_AdoOtptPt.m_AdoOtptFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出帧链表成功。" );
				}

				//销毁音频输出设备。
				if( m_AdoOtptPt.m_AdoOtptDvcPt != null )
				{
					if( m_AdoOtptPt.m_AdoOtptDvcPt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_AdoOtptPt.m_AdoOtptDvcPt.stop();
					m_AdoOtptPt.m_AdoOtptDvcPt.release();
					m_AdoOtptPt.m_AdoOtptDvcPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出设备成功。" );
				}

				//销毁音频输出波形器。
				if( m_AdoOtptPt.m_IsDrawAdoWavfmToSurface != 0 )
				{
					if( m_AdoOtptPt.m_AdoOtptOscilloPt != null )
					{
						if( m_AdoOtptPt.m_AdoOtptOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出波形器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
						}
						m_AdoOtptPt.m_AdoOtptOscilloPt = null;
					}
				}

				//销毁音频输出Wave文件写入器。
				if( m_AdoOtptPt.m_IsSaveAdoToFile != 0 )
				{
					if( m_AdoOtptPt.m_AdoOtptWaveFileWriterPt != null )
					{
						if( m_AdoOtptPt.m_AdoOtptWaveFileWriterPt.Dstoy() == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器失败。" );
						}
						m_AdoOtptPt.m_AdoOtptWaveFileWriterPt = null;
					}
				}

				//销毁解码器。
				switch( m_AdoOtptPt.m_UseWhatDecd )
				{
					case 0: //如果要使用PCM原始数据。
					{
						break;
					}
					case 1: //如果要使用Speex解码器。
					{
						if( m_AdoOtptPt.m_SpeexDecdPt != null )
						{
							if( m_AdoOtptPt.m_SpeexDecdPt.Dstoy() == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器失败。" );
							}
							m_AdoOtptPt.m_SpeexDecdPt = null;
						}
						break;
					}
					case 2: //如果要使用Opus解码器。
					{
						break;
					}
				}
			} //销毁音频输出完毕。

			//销毁视频输入。
			{
				//销毁视频输入线程。
				if( m_VdoInptPt.m_VdoInptThrdPt != null )
				{
					try
					{
						m_VdoInptPt.m_VdoInptThrdPt.join(); //等待视频输入线程退出。
					}
					catch( InterruptedException ignored )
					{
					}
					m_VdoInptPt.m_VdoInptThrdPt = null;
					m_VdoInptPt.m_VdoInptThrdExitFlag = 0;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程成功。" );
				}

				//销毁视频输入线程的临时变量。
				{
					m_VdoInptPt.m_LastVdoInptFrmTimeMsec = 0; //销毁上一个视频输入帧的时间。
					m_VdoInptPt.m_VdoInptFrmTimeStepMsec = 0; //销毁视频输入帧的时间步进。
					m_VdoInptPt.m_VdoInptFrmPt = null; //销毁视频输入帧的指针。
					m_VdoInptPt.m_VdoInptRsltFrmPt = null; //初始化视频输入结果帧的指针。
					m_VdoInptPt.m_VdoInptTmpFrmPt = null; //初始化视频输入临时帧的指针。
					m_VdoInptPt.m_VdoInptSwapFrmPt = null; //初始化视频输入交换帧的指针。
					m_VdoInptPt.m_VdoInptRsltFrmLenPt = null; //初始化视频输入结果帧的长度。
					m_VdoInptPt.m_VdoInptRsltFrmSz = 0; //销毁视频输入结果帧的内存大小。
					m_VdoInptPt.m_VdoInptFrmElmPt = null; //销毁视频输入帧元素的指针。
					m_VdoInptPt.m_VdoInptFrmLnkLstElmTotal = 0; //销毁视频输入帧链表的元数总数。
					m_VdoInptPt.m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
					m_VdoInptPt.m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程的临时变量成功。" );
				}

				//销毁视频输入设备。
				if( m_VdoInptPt.m_VdoInptDvcPt != null )
				{
					m_VdoInptPt.m_VdoInptDvcPt.setPreviewCallback( null ); //设置预览回调函数为空，防止出现java.lang.RuntimeException: Method called after release()异常。
					m_VdoInptPt.m_VdoInptDvcPt.stopPreview(); //停止预览。
					m_VdoInptPt.m_VdoInptDvcPt.release(); //销毁摄像头。
					m_VdoInptPt.m_VdoInptDvcPt = null;
					m_VdoInptPt.m_VdoInptPrvwClbkBufPtPt = null;
					m_VdoInptPt.m_VdoInptDvcFrmRotate = 0;
					m_VdoInptPt.m_VdoInptDvcFrmWidth = 0;
					m_VdoInptPt.m_VdoInptDvcFrmHeight = 0;
					m_VdoInptPt.m_VdoInptDvcFrmIsCrop = 0;
					m_VdoInptPt.m_VdoInptDvcFrmCropX = 0;
					m_VdoInptPt.m_VdoInptDvcFrmCropY = 0;
					m_VdoInptPt.m_VdoInptDvcFrmCropWidth = 0;
					m_VdoInptPt.m_VdoInptDvcFrmCropHeight = 0;
					m_VdoInptPt.m_VdoInptDvcFrmIsScale = 0;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入设备成功。" );
				}

				//销毁视频输入空闲帧链表。
				if( m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt != null )
				{
					m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.clear();
					m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入空闲帧链表成功。" );
				}

				//销毁视频输入帧链表。
				if( m_VdoInptPt.m_VdoInptFrmLnkLstPt != null )
				{
					m_VdoInptPt.m_VdoInptFrmLnkLstPt.clear();
					m_VdoInptPt.m_VdoInptFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入帧链表成功。" );
				}

				//销毁NV21格式视频输入帧链表。
				if( m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt != null )
				{
					m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt.clear();
					m_VdoInptPt.m_NV21VdoInptFrmLnkLstPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁NV21格式视频输入帧链表成功。" );
				}

				//销毁编码器。
				switch( m_VdoInptPt.m_UseWhatEncd )
				{
					case 0: //如果要使用YU12原始数据。
					{
						break;
					}
					case 1: //如果要使用OpenH264编码器。
					{
						if( m_VdoInptPt.m_OpenH264EncdPt != null )
						{
							if( m_VdoInptPt.m_OpenH264EncdPt.Dstoy( null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器失败。" );
							}
							m_VdoInptPt.m_OpenH264EncdPt = null;
						}
						break;
					}
					case 2: //如果要使用系统自带H264编码器。
					{
						if( m_VdoInptPt.m_SystemH264EncdPt != null )
						{
							if( m_VdoInptPt.m_SystemH264EncdPt.Dstoy( null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器失败。" );
							}
							m_VdoInptPt.m_SystemH264EncdPt = null;
						}
						break;
					}
				}
			} //销毁视频输入完毕。

			//销毁视频输出。
			{
				//销毁视频输出线程。
				if( m_VdoOtptPt.m_VdoOtptThrdPt != null )
				{
					try
					{
						m_VdoOtptPt.m_VdoOtptThrdPt.join(); //等待视频输出线程退出。
					}
					catch( InterruptedException ignored )
					{
					}
					m_VdoOtptPt.m_VdoOtptThrdPt = null;
					m_VdoOtptPt.m_VdoOtptThrdExitFlag = 0;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出线程成功。" );
				}

				//销毁视频输出线程的临时变量。
				{
					m_VdoOtptPt.m_VdoOtptRsltFrmPt = null; //销毁视频输出结果帧的指针。
					m_VdoOtptPt.m_VdoOtptTmpFrmPt = null; //销毁视频输出临时帧的指针。
					m_VdoOtptPt.m_VdoOtptSwapFrmPt = null; //销毁视频输出交换帧的指针。
					m_VdoOtptPt.m_VdoOtptRsltFrmLenPt = null; //销毁视频输出结果帧的长度。
					m_VdoOtptPt.m_VdoOtptFrmWidthPt = null; //销毁视频输出帧的宽度。
					m_VdoOtptPt.m_VdoOtptFrmHeightPt = null; //销毁视频输出帧的高度。
					m_VdoOtptPt.m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
					m_VdoOtptPt.m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出线程的临时变量成功。" );
				}

				//销毁解码器。
				switch( m_VdoOtptPt.m_UseWhatDecd )
				{
					case 0: //如果要使用YU12原始数据。
					{
						break;
					}
					case 1: //如果要使用OpenH264解码器。
					{
						if( m_VdoOtptPt.m_OpenH264DecdPt != null )
						{
							if( m_VdoOtptPt.m_OpenH264DecdPt.Dstoy( null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器失败。" );
							}
							m_VdoOtptPt.m_OpenH264DecdPt = null;
						}
						break;
					}
					case 2: //如果要使用系统自带H264解码器。
					{
						if( m_VdoOtptPt.m_SystemH264DecdPt != null )
						{
							if( m_VdoOtptPt.m_SystemH264DecdPt.Dstoy( null ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264解码器成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264解码器失败。" );
							}
							m_VdoOtptPt.m_SystemH264DecdPt = null;
						}
						break;
					}
				}
			} //销毁视频输出完毕。

			//销毁媒体处理线程的临时变量。
			{
				m_PcmAdoInptFrmPt = null;
				m_PcmAdoOtptFrmPt = null;
				m_PcmAdoRsltFrmPt = null;
				m_PcmAdoTmpFrmPt = null;
				m_PcmAdoSwapFrmPt = null;
				m_VoiceActStsPt = null;
				m_EncdAdoInptFrmPt = null;
				m_EncdAdoInptFrmLenPt = null;
				m_EncdAdoInptFrmIsNeedTransPt = null;
				m_VdoInptFrmPt = null;
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁媒体处理线程的临时变量成功。" );
			} //销毁媒体处理线程的临时变量完毕。

			//销毁唤醒锁。
			WakeLockInitOrDstoy( 0 );

			if( m_ExitFlag != 3 ) //如果需要调用用户定义的销毁函数。
			{
				UserDstoy(); //调用用户定义的销毁函数。
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
			}

			//销毁错误信息动态字符串。
			if( m_ErrInfoVarStrPt != null )
			{
				if( m_ErrInfoVarStrPt.Dstoy() == 0 )
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁错误信息动态字符串成功。" );
				}
				else
				{
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁错误信息动态字符串失败。" );
				}
				m_ErrInfoVarStrPt = null;
			}

			m_RunFlag = RUN_FLAG_END; //设置本线程运行标记为销毁完毕。

			if( ( m_ExitFlag == 0 ) || ( m_ExitFlag == 1 ) ) //如果用户需要直接退出。
			{
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程已退出。" );
				break OutMediaPocsThrdLoop;
			}
			else //如果用户需要重新初始化。
			{
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程重新初始化。" );
			}
		}
	}
}