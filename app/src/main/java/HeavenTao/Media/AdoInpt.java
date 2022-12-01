package HeavenTao.Media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.LinkedList;

import HeavenTao.Ado.*;
import HeavenTao.Data.*;

public class AdoInpt //音频输入类。
{
	MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

	public int m_IsUseAdoInpt; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。
	public int m_IsInitAdoInpt; //存放是否初始化音频输入，为0表示未初始化，为非0表示已初始化。
	
	public int m_SmplRate; //存放采样频率，单位为赫兹，取值只能为8000、16000、32000、48000。
	public int m_FrmLen; //存放帧的长度，单位为采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。

	public int m_IsUseSystemAecNsAgc; //存放是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带），为0表示不使用，为非0表示要使用。

	public int m_UseWhatAec; //存放使用什么声学回音消除器，为0表示不使用，为1表示Speex声学回音消除器，为2表示WebRtc定点版声学回音消除器，为2表示WebRtc浮点版声学回音消除器，为4表示SpeexWebRtc三重声学回音消除器。
	public int m_IsCanUseAec; //存放是否可以使用声学回音消除器，为0表示不可以，为非0表示可以。

	SpeexAec m_SpeexAecPt; //存放Speex声学回音消除器的指针。
	int m_SpeexAecFilterLen; //存放Speex声学回音消除器的滤波器长度，单位毫秒。
	int m_SpeexAecIsUseRec; //存放Speex声学回音消除器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
	float m_SpeexAecEchoMutp; //存放Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
	float m_SpeexAecEchoCntu; //存放Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
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
	float m_SpeexWebRtcAecSpeexAecEchoMutp; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
	float m_SpeexWebRtcAecSpeexAecEchoCntu; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
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

	public int m_IsUseSpeexPrpocsOther; //存放Speex预处理器是否使用其他功能，为非0表示要使用，为0表示不使用。
	int m_SpeexPrpocsIsUseVad; //存放Speex预处理器是否使用语音活动检测，为非0表示要使用，为0表示不使用。
	int m_SpeexPrpocsVadProbStart; //存放Speex预处理器在语音活动检测时，从无语音活动到有语音活动的判断百分比概率，概率越大越难判断为有语音活，取值区间为[0,100]。
	int m_SpeexPrpocsVadProbCntu; //存放Speex预处理器在语音活动检测时，从有语音活动到无语音活动的判断百分比概率，概率越大越容易判断为无语音活动，取值区间为[0,100]。
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
	WaveFileWriter m_AdoInptWaveFileWriterPt; //存放音频输入Wave文件写入器的指针。
	WaveFileWriter m_AdoRsltWaveFileWriterPt; //存放音频结果Wave文件写入器的指针。
	String m_AdoInptFileFullPathStrPt; //存放音频输入文件的完整路径字符串的指针。
	String m_AdoRsltFileFullPathStrPt; //存放音频结果文件的完整路径字符串的指针。

	public int m_IsDrawAdoWavfmToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
	SurfaceView m_AdoInptWavfmSurfacePt; //存放音频输入波形Surface的指针。
	AdoWavfm m_AdoInptWavfmPt; //存放音频输入波形器的指针。
	SurfaceView m_AdoRsltWavfmSurfacePt; //存放音频结果波形Surface的指针。
	AdoWavfm m_AdoRsltWavfmPt; //存放音频结果波形器的指针。

	AudioRecord m_AdoInptDvcPt; //存放音频输入设备的指针。
	int m_AdoInptDvcBufSz; //存放音频输入设备缓冲区大小，单位字节。
	public int m_AdoInptIsMute; //存放音频输入是否静音，为0表示有声音，为非0表示静音。

	public LinkedList< short[] > m_AdoInptFrmLnkLstPt; //存放音频输入帧链表的指针。
	public LinkedList< short[] > m_AdoInptIdleFrmLnkLstPt; //存放音频输入空闲帧链表的指针。

	int m_IsInitAdoInptThrdTmpVar; //存放是否初始化音频输入线程的临时变量。
	short[] m_AdoInptFrmPt; //存放音频输入帧的指针。
	int m_AdoInptFrmLnkLstElmTotal; //存放音频输入帧链表的元数总数。
	long m_LastTimeMsec; //存放上次时间的毫秒数。
	long m_NowTimeMsec; //存放本次时间的毫秒数。

	AdoInptThrd m_AdoInptThrdPt; //存放音频输入线程的指针。
	int m_AdoInptThrdExitFlag; //存放音频输入线程退出标记，0表示保持运行，1表示请求退出。

	//初始化声学回音消除器。
	public int AecInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			switch( m_UseWhatAec )
			{
				case 0: //如果不使用声学回音消除器。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：不使用声学回音消除器。" );
					break;
				}
				case 1: //如果要使用Speex声学回音消除器。
				{
					if( m_SpeexAecIsSaveMemFile != 0 ) //如果Speex声学回音消除器要保存内存块到文件。
					{
						m_SpeexAecPt = new SpeexAec();
						if( m_SpeexAecPt.InitByMemFile( m_SmplRate, m_FrmLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMutp, m_SpeexAecEchoCntu, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct, m_SpeexAecMemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_SpeexAecMemFileFullPathStrPt + " 来初始化Speex声学回音消除器成功。" );
						}
						else
						{
							m_SpeexAecPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_SpeexAecMemFileFullPathStrPt + " 来初始化Speex声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						}
					}
					if( m_SpeexAecPt == null )
					{
						m_SpeexAecPt = new SpeexAec();
						if( m_SpeexAecPt.Init( m_SmplRate, m_FrmLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMutp, m_SpeexAecEchoCntu, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex声学回音消除器成功。" );
						}
						else
						{
							m_SpeexAecPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
					}
					break;
				}
				case 2: //如果要使用WebRtc定点版声学回音消除器。
				{
					m_WebRtcAecmPt = new WebRtcAecm();
					if( m_WebRtcAecmPt.Init( m_SmplRate, m_FrmLen, m_WebRtcAecmIsUseCNGMode, m_WebRtcAecmEchoMode, m_WebRtcAecmDelay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc定点版声学回音消除器成功。" );
					}
					else
					{
						m_WebRtcAecmPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc定点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
				case 3: //如果要使用WebRtc浮点版声学回音消除器。
				{
					if( m_WebRtcAecIsSaveMemFile != 0 ) //如果WebRtc浮点版声学回音消除器要保存内存块到文件。
					{
						m_WebRtcAecPt = new WebRtcAec();
						if( m_WebRtcAecPt.InitByMemFile( m_SmplRate, m_FrmLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgstcMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay, m_WebRtcAecMemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecMemFileFullPathStrPt + " 来初始化WebRtc浮点版声学回音消除器成功。" );
						}
						else
						{
							m_WebRtcAecPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecMemFileFullPathStrPt + " 来初始化WebRtc浮点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						}
					}
					if( m_WebRtcAecPt == null )
					{
						m_WebRtcAecPt = new WebRtcAec();
						if( m_WebRtcAecPt.Init( m_SmplRate, m_FrmLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgstcMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc浮点版声学回音消除器成功。" );
						}
						else
						{
							m_WebRtcAecPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc浮点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
					}
					break;
				}
				case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
				{
					m_SpeexWebRtcAecPt = new SpeexWebRtcAec();
					if( m_SpeexWebRtcAecPt.Init( m_SmplRate, m_FrmLen, m_SpeexWebRtcAecWorkMode, m_SpeexWebRtcAecSpeexAecFilterLen, m_SpeexWebRtcAecSpeexAecIsUseRec, m_SpeexWebRtcAecSpeexAecEchoMutp, m_SpeexWebRtcAecSpeexAecEchoCntu, m_SpeexWebRtcAecSpeexAecEchoSupes, m_SpeexWebRtcAecSpeexAecEchoSupesAct, m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_SpeexWebRtcAecWebRtcAecmEchoMode, m_SpeexWebRtcAecWebRtcAecmDelay, m_SpeexWebRtcAecWebRtcAecEchoMode, m_SpeexWebRtcAecWebRtcAecDelay, m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode, m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode, m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode, m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay, m_SpeexWebRtcAecIsUseSameRoomAec, m_SpeexWebRtcAecSameRoomEchoMinDelay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化SpeexWebRtc三重声学回音消除器成功。" );
					}
					else
					{
						m_SpeexWebRtcAecPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化SpeexWebRtc三重声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁声学回音消除器。
	public void AecDstoy()
	{
		switch( m_UseWhatAec )
		{
			case 0: //如果不使用声学回音消除器。
			{
				break;
			}
			case 1: //如果要使用Speex声学回音消除器。
			{
				if( m_SpeexAecPt != null )
				{
					if( m_SpeexAecIsSaveMemFile != 0 ) //如果Speex声学回音消除器要保存内存块到文件。
					{
						if( m_SpeexAecPt.SaveMemFile( m_SmplRate, m_FrmLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMutp, m_SpeexAecEchoCntu, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct, m_SpeexAecMemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecMemFileFullPathStrPt + " 成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecMemFileFullPathStrPt + " 失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						}
					}
					if( m_SpeexAecPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器失败。" );
					}
					m_SpeexAecPt = null;
				}
				break;
			}
			case 2: //如果要使用WebRtc定点版声学回音消除器。
			{
				if( m_WebRtcAecmPt != null )
				{
					if( m_WebRtcAecmPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器失败。" );
					}
					m_WebRtcAecmPt = null;
				}
				break;
			}
			case 3: //如果要使用WebRtc浮点版声学回音消除器。
			{
				if( m_WebRtcAecPt != null )
				{
					if( m_WebRtcAecIsSaveMemFile != 0 ) //如果WebRtc浮点版声学回音消除器要保存内存块到文件。
					{
						if( m_WebRtcAecPt.SaveMemFile( m_SmplRate, m_FrmLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgstcMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay, m_WebRtcAecMemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecMemFileFullPathStrPt + " 成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecMemFileFullPathStrPt + " 失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						}
					}
					if( m_WebRtcAecPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器失败。" );
					}
					m_WebRtcAecPt = null;
				}
				break;
			}
			case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
			{
				if( m_SpeexWebRtcAecPt != null )
				{
					if( m_SpeexWebRtcAecPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器失败。" );
					}
					m_SpeexWebRtcAecPt = null;
				}
				break;
			}
		}
	}

	//设置是否可以使用声学回音消除器。
	void SetIsCanUseAec()
	{
		if( m_UseWhatAec != 0 ) //如果要使用声学回音消除器。
		{
			if( m_MediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt == 0 )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为不使用音频输入，所以不可以使用声学回音消除器。" );
				m_IsCanUseAec = 0;
			}
			else if( m_MediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt == 0 )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为不使用音频输出，所以不可以使用声学回音消除器。" );
				m_IsCanUseAec = 0;
			}
			else if( m_MediaPocsThrdPt.m_AdoOtptPt.m_SmplRate != m_SmplRate )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为音频输出的采样频率与音频输入不一致，所以不可以使用声学回音消除器。" );
				m_IsCanUseAec = 0;
			}
			else if( m_MediaPocsThrdPt.m_AdoOtptPt.m_FrmLen != m_FrmLen )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为音频输出帧的长度与音频输入不一致，所以不可以使用声学回音消除器。" );
				m_IsCanUseAec = 0;
			}
			else
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为要使用声学回音消除器，所以可以使用声学回音消除器。" );
				m_IsCanUseAec = 1;
			}
		}
		else //如果不使用声学回音消除器。
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：因为不使用声学回音消除器，所以不可以使用声学回音消除器。" );
			m_IsCanUseAec = 0;
		}
	}

	//初始化噪音抑制器。
	public int NsInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			switch( m_UseWhatNs )
			{
				case 0: //如果不使用噪音抑制器。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：不使用噪音抑制器。" );
					break;
				}
				case 1: //如果要使用Speex预处理器的噪音抑制。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：在初始化Speex预处理器时一起初始化Speex预处理器的噪音抑制。" );
					break;
				}
				case 2: //如果要使用WebRtc定点版噪音抑制器。
				{
					m_WebRtcNsxPt = new WebRtcNsx();
					if( m_WebRtcNsxPt.Init( m_SmplRate, m_FrmLen, m_WebRtcNsxPolicyMode, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc定点版噪音抑制器成功。" );
					}
					else
					{
						m_WebRtcNsxPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc定点版噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
				case 3: //如果要使用WebRtc浮点版噪音抑制器。
				{
					m_WebRtcNsPt = new WebRtcNs();
					if( m_WebRtcNsPt.Init( m_SmplRate, m_FrmLen, m_WebRtcNsPolicyMode, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc浮点版噪音抑制器成功。" );
					}
					else
					{
						m_WebRtcNsPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化WebRtc浮点版噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
				case 4: //如果要使用RNNoise噪音抑制器。
				{
					m_RNNoisePt = new RNNoise();
					if( m_RNNoisePt.Init( m_SmplRate, m_FrmLen, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化RNNoise噪音抑制器成功。" );
					}
					else
					{
						m_RNNoisePt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化RNNoise噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁噪音抑制器。
	public void NsDstoy()
	{
		switch( m_UseWhatNs )
		{
			case 0: //如果不使用噪音抑制器。
			{
				break;
			}
			case 1: //如果要使用Speex预处理器的噪音抑制。
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：在销毁Speex预处理器时一起销毁Speex预处理器的噪音抑制。" );
				break;
			}
			case 2: //如果要使用WebRtc定点版噪音抑制器。
			{
				if( m_WebRtcNsxPt != null )
				{
					if( m_WebRtcNsxPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器失败。" );
					}
					m_WebRtcNsxPt = null;
				}
				break;
			}
			case 3: //如果要使用WebRtc浮点版噪音抑制器。
			{
				if( m_WebRtcNsPt != null )
				{
					if( m_WebRtcNsPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器失败。" );
					}
					m_WebRtcNsPt = null;
				}
				break;
			}
			case 4: //如果要使用RNNoise噪音抑制器。
			{
				if( m_RNNoisePt != null )
				{
					if( m_RNNoisePt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器失败。" );
					}
					m_RNNoisePt = null;
				}
				break;
			}
		}
	}

	//初始化Speex预处理器。
	public int SpeexPrpocsInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			if( ( m_UseWhatNs == 1 ) || ( m_IsUseSpeexPrpocsOther != 0 ) )
			{
				m_SpeexPrpocsPt = new SpeexPrpocs();
				if( m_SpeexPrpocsPt.Init( m_SmplRate, m_FrmLen, ( m_UseWhatNs == 1 ) ? m_SpeexPrpocsIsUseNs : 0, m_SpeexPrpocsNoiseSupes, ( m_UseWhatNs == 1 ) ? m_SpeexPrpocsIsUseDereverb : 0, ( m_IsUseSpeexPrpocsOther != 0 ) ? m_SpeexPrpocsIsUseVad : 0, m_SpeexPrpocsVadProbStart, m_SpeexPrpocsVadProbCntu, ( m_IsUseSpeexPrpocsOther != 0 ) ? m_SpeexPrpocsIsUseAgc : 0, m_SpeexPrpocsAgcLevel, m_SpeexPrpocsAgcIncrement, m_SpeexPrpocsAgcDecrement, m_SpeexPrpocsAgcMaxGain, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex预处理器成功。"  );
				}
				else
				{
					m_SpeexPrpocsPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex预处理器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
					break Out;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁Speex预处理器。
	public void SpeexPrpocsDstoy()
	{
		if( m_SpeexPrpocsPt != null )
		{
			if( m_SpeexPrpocsPt.Dstoy() == 0 )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器成功。" );
			}
			else
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器失败。" );
			}
			m_SpeexPrpocsPt = null;
		}
	}

	//初始化编码器。
	public int EncdInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			switch( m_UseWhatEncd )
			{
				case 0: //如果要使用PCM原始数据。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化PCM原始数据成功。" );
					break;
				}
				case 1: //如果要使用Speex编码器。
				{
					if( m_FrmLen != m_SmplRate / 1000 * 20 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：帧的长度不为20毫秒不能使用Speex编码器。" );
						break Out;
					}
					m_SpeexEncdPt = new SpeexEncd();
					if( m_SpeexEncdPt.Init( m_SmplRate, m_SpeexEncdUseCbrOrVbr, m_SpeexEncdQualt, m_SpeexEncdCmplxt, m_SpeexEncdPlcExptLossRate ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex编码器成功。" );
					}
					else
					{
						m_SpeexEncdPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化Speex编码器失败。" );
						break Out;
					}
					break;
				}
				case 2: //如果要使用Opus编码器。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus编码器。" );
					break Out;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁编码器。
	public void EncdDstoy()
	{
		switch( m_UseWhatEncd )
		{
			case 0: //如果要使用PCM原始数据。
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁PCM原始数据成功。" );
				break;
			}
			case 1: //如果要使用Speex编码器。
			{
				if( m_SpeexEncdPt != null )
				{
					if( m_SpeexEncdPt.Dstoy() == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器失败。" );
					}
					m_SpeexEncdPt = null;
				}
				break;
			}
			case 2: //如果要使用Opus编码器。
			{
				break;
			}
		}
	}

	//初始化音频输入Wave文件写入器、音频结果Wave文件写入器。
	public int WaveFileWriterInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_IsSaveAdoToFile != 0 )
			{
				m_AdoInptWaveFileWriterPt = new WaveFileWriter();
				if( m_AdoInptWaveFileWriterPt.Init( m_AdoInptFileFullPathStrPt, ( short ) 1, m_SmplRate, 16 ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入文件 " + m_AdoInptFileFullPathStrPt + " 的Wave文件写入器成功。" );
				}
				else
				{
					m_AdoInptWaveFileWriterPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入文件 " + m_AdoInptFileFullPathStrPt + " 的Wave文件写入器失败。" );
					break Out;
				}
				m_AdoRsltWaveFileWriterPt = new WaveFileWriter();
				if( m_AdoRsltWaveFileWriterPt.Init( m_AdoRsltFileFullPathStrPt, ( short ) 1, m_SmplRate, 16 ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频结果文件 " + m_AdoRsltFileFullPathStrPt + " 的Wave文件写入器成功。" );
				}
				else
				{
					m_AdoRsltWaveFileWriterPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频结果文件 " + m_AdoRsltFileFullPathStrPt + " 的Wave文件写入器失败。" );
					break Out;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输入Wave文件写入器、音频结果Wave文件写入器。
	public void WaveFileWriterDstoy()
	{
		if( m_IsSaveAdoToFile != 0 )
		{
			if( m_AdoInptWaveFileWriterPt != null )
			{
				if( m_AdoInptWaveFileWriterPt.Dstoy() == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器失败。" );
				}
				m_AdoInptWaveFileWriterPt = null;
			}
			if( m_AdoRsltWaveFileWriterPt != null )
			{
				if( m_AdoRsltWaveFileWriterPt.Dstoy() == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器失败。" );
				}
				m_AdoRsltWaveFileWriterPt = null;
			}
		}
	}

	//初始化音频输入波形器、音频结果波形器。
	public int WavfmInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_IsDrawAdoWavfmToSurface != 0 )
			{
				m_AdoInptWavfmPt = new AdoWavfm();
				if( m_AdoInptWavfmPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入波形器成功。" );
				}
				else
				{
					m_AdoInptWavfmPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
					break Out;
				}
				m_AdoRsltWavfmPt = new AdoWavfm();
				if( m_AdoRsltWavfmPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频结果波形器成功。" );
				}
				else
				{
					m_AdoRsltWavfmPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频结果波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
					break Out;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输入波形器、音频结果波形器。
	public void WavfmDstoy()
	{
		if( m_IsDrawAdoWavfmToSurface != 0 )
		{
			if( m_AdoInptWavfmPt != null )
			{
				if( m_AdoInptWavfmPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
				}
				m_AdoInptWavfmPt = null;
			}
			if( m_AdoRsltWavfmPt != null )
			{
				if( m_AdoRsltWavfmPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
				}
				m_AdoRsltWavfmPt = null;
			}
		}
	}

	//初始化音频输入设备和线程。
	public int DvcAndThrdInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			//初始化音频输入设备。
			try
			{
				m_AdoInptDvcBufSz = AudioRecord.getMinBufferSize( m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
				m_AdoInptDvcBufSz = ( m_AdoInptDvcBufSz > m_FrmLen * 2 ) ? m_AdoInptDvcBufSz : m_FrmLen * 2;
				m_AdoInptDvcPt = new AudioRecord(
						( m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
						m_SmplRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT,
						m_AdoInptDvcBufSz );
				if( m_AdoInptDvcPt.getState() == AudioRecord.STATE_INITIALIZED )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入设备成功。音频输入设备缓冲区大小：" + m_AdoInptDvcBufSz );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入设备失败。" );
					if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, "媒体处理线程：初始化音频输入设备失败。", Toast.LENGTH_LONG ).show(); } } );
					break Out;
				}
			}
			catch( IllegalArgumentException e )
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入设备失败。原因：" + e.getMessage() );
				if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, "媒体处理线程：初始化音频输入设备失败。原因：" + e.getMessage(), Toast.LENGTH_LONG ).show(); } } );
				break Out;
			}

			//初始化音频输入帧链表。
			m_AdoInptFrmLnkLstPt = new LinkedList< short[] >();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入帧链表成功。" );

			//初始化音频输入空闲帧链表。
			m_AdoInptIdleFrmLnkLstPt = new LinkedList< short[] >();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入空闲帧链表成功。" );

			//初始化音频输入线程的临时变量。
			{
				m_IsInitAdoInptThrdTmpVar = 1; //设置已初始化音频输入线程的临时变量。
				m_AdoInptFrmPt = null; //初始化音频输入帧的指针。
				m_AdoInptFrmLnkLstElmTotal = 0; //初始化音频输入帧链表的元数总数。
				m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
				m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入线程的临时变量成功。" );
			}

			//初始化音频输入线程。
			{
				m_AdoInptThrdExitFlag = 0; //设置音频输入线程退出标记为0表示保持运行。
				m_AdoInptThrdPt = new AdoInptThrd();
				m_AdoInptThrdPt.start();
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入线程成功。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输入设备和线程。
	public void DvcAndThrdDstoy()
	{
		//销毁音频输入线程。
		if( m_AdoInptThrdPt != null )
		{
			m_AdoInptThrdExitFlag = 1; //请求音频输入线程退出。
			try
			{
				m_AdoInptThrdPt.join(); //等待音频输入线程退出。
			}
			catch( InterruptedException ignored )
			{
			}
			m_AdoInptThrdPt = null;
			m_AdoInptThrdExitFlag = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入线程成功。" );
		}

		//销毁音频输入线程的临时变量。
		if( m_IsInitAdoInptThrdTmpVar != 0 )
		{
			m_IsInitAdoInptThrdTmpVar = 0; //设置未初始化音频输入线程的临时变量。
			m_AdoInptFrmPt = null; //销毁音频输入帧的指针。
			m_AdoInptFrmLnkLstElmTotal = 0; //销毁音频输入帧链表的元数总数。
			m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
			m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入线程的临时变量成功。" );
		}

		//销毁音频输入空闲帧链表。
		if( m_AdoInptIdleFrmLnkLstPt != null )
		{
			m_AdoInptIdleFrmLnkLstPt.clear();
			m_AdoInptIdleFrmLnkLstPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入空闲帧链表成功。" );
		}

		//销毁音频输入帧链表。
		if( m_AdoInptFrmLnkLstPt != null )
		{
			m_AdoInptFrmLnkLstPt.clear();
			m_AdoInptFrmLnkLstPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入帧链表成功。" );
		}

		//销毁音频输入设备。
		if( m_AdoInptDvcPt != null )
		{
			if( m_AdoInptDvcPt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_AdoInptDvcPt.stop();
			m_AdoInptDvcPt.release();
			m_AdoInptDvcPt = null;
			m_AdoInptDvcBufSz = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁音频输入设备成功。" );
		}
	}

	//初始化音频输入。
	public int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		Out:
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

			if( AecInit() != 0 ) break Out;
			if( NsInit() != 0 ) break Out;
			if( SpeexPrpocsInit() != 0 ) break Out;
			if( EncdInit() != 0 ) break Out;
			if( WaveFileWriterInit() != 0 ) break Out;
			if( WavfmInit() != 0 ) break Out;
			if( DvcAndThrdInit() != 0 ) break Out;

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
			{
				p_NowMsec = System.currentTimeMillis(); //记录初始化结束的时间。
				Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化音频输入耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//销毁音频输入。
	public void Dstoy()
	{
		DvcAndThrdDstoy();
		WavfmDstoy();
		WaveFileWriterDstoy();
		EncdDstoy();
		SpeexPrpocsDstoy();
		NsDstoy();
		AecDstoy();
	}

	//音频输入线程类。
	public class AdoInptThrd extends Thread
	{
		public void run()
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

			if( m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器，就自适应计算声学回音的延迟，并设置到声学回音消除器。放在音频输入线程中计算，可以减少媒体处理线程的初始化时间。
			{
				int p_AdoOtptDelay = -10; //存放音频输出延迟。播放的最后一个10ms空的音频输出帧不算音频输出延迟，因为是多写进去的。
				int p_AdoInptDelay = 0; //存放音频输入延迟。
				int p_Delay; //存放声学回音的延迟，单位毫秒。
				HTInt p_HTIntDelay = new HTInt();

				//计算音频输出的延迟。
				m_MediaPocsThrdPt.m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
				m_AdoInptFrmPt = new short[ m_MediaPocsThrdPt.m_AdoOtptPt.m_SmplRate / 1000 * 10 ]; //创建一个10ms空的音频输出帧。
				m_LastTimeMsec = System.currentTimeMillis();
				while( true )
				{
					m_MediaPocsThrdPt.m_AdoOtptPt.m_AdoOtptDvcPt.write( m_AdoInptFrmPt, 0, m_AdoInptFrmPt.length ); //播放一个空的音频输出帧。
					m_NowTimeMsec = System.currentTimeMillis();
					p_AdoOtptDelay += 10; //递增音频输出延迟。
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "本次音频输出耗时 " + ( m_NowTimeMsec - m_LastTimeMsec ) + " 毫秒，音频输出延迟 " + p_AdoOtptDelay + " 毫秒。" );
					if( m_NowTimeMsec - m_LastTimeMsec >= 10 ) //如果播放耗时较长，就表示音频输出设备的缓冲区已经写满，结束计算。
					{
						break;
					}
					m_LastTimeMsec = m_NowTimeMsec;
				}
				m_AdoInptFrmPt = null;
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟 " + p_AdoOtptDelay + " 毫秒。" );

				//计算音频输入的延迟。
				m_AdoInptDvcPt.startRecording(); //让音频输入设备开始录音。
				p_AdoInptDelay = 0; //音频输入延迟不方便计算，调用耗时在不同的设备都不一样，可能为0也可能很高，也数据不一定为全0，所以直接认定音频输入延迟为0ms。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟 " + p_AdoInptDelay + " 毫秒。" );

				//计算声学回音的延迟。
				p_Delay = p_AdoOtptDelay + p_AdoInptDelay;
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "声学回音延迟 " + p_Delay + " 毫秒，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

				m_MediaPocsThrdPt.m_AdoOtptPt.m_AdoOtptThrdIsStart = 1; //设置音频输出线程已开始。

				//设置到WebRtc定点版和浮点版声学回音消除器。
				if( ( m_WebRtcAecmPt != null ) && ( m_WebRtcAecmPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc定点版声学回音消除器，且需要自适应设置回音的延迟。
				{
					m_WebRtcAecmPt.SetDelay( p_Delay / 2 ); //WebRtc定点版声学回音消除器的回音延迟应为实际声学回音延迟的二分之一，这样效果最好。
					m_WebRtcAecmPt.GetDelay( p_HTIntDelay );
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_WebRtcAecPt != null ) && ( m_WebRtcAecPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc浮点版声学回音消除器，且需要自适应设置回音的延迟。
				{
					if( m_WebRtcAecIsUseDelayAgstcMode == 0 ) //如果WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
					{
						m_WebRtcAecPt.SetDelay( p_Delay );
						m_WebRtcAecPt.GetDelay( p_HTIntDelay );
					}
					else //如果WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
					{
						m_WebRtcAecPt.SetDelay( 20 );
						m_WebRtcAecPt.GetDelay( p_HTIntDelay );
					}
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_SpeexWebRtcAecPt != null ) && ( m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc定点版声学回音消除器需要自适应设置回音的延迟。
				{
					m_SpeexWebRtcAecPt.SetWebRtcAecmDelay( p_Delay / 2 ); //WebRtc定点版声学回音消除器的回音延迟应为实际声学回音延迟的二分之一，这样效果最好。
					m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay );
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
				if( ( m_SpeexWebRtcAecPt != null ) && ( m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
				{
					if( m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
					{
						m_SpeexWebRtcAecPt.SetWebRtcAecDelay( p_Delay );
						m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
					}
					else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
					{
						m_SpeexWebRtcAecPt.SetWebRtcAecDelay( 20 );
						m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
					}
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
				}
			}
			else //如果不使用音频输入的声学回音消除，就直接启动音频输出线程。
			{
				m_AdoInptDvcPt.startRecording(); //让音频输入设备开始录音。
				if( m_MediaPocsThrdPt.m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) //如果已初始化音频输出。
				{
					m_MediaPocsThrdPt.m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
					m_MediaPocsThrdPt.m_AdoOtptPt.m_AdoOtptThrdIsStart = 1; //设置音频输出线程已开始。
				}
			}

			//音频输入循环开始。
			while( true )
			{
				//获取一个音频输入空闲帧。
				if( ( m_AdoInptFrmLnkLstElmTotal = m_AdoInptIdleFrmLnkLstPt.size() ) > 0 ) //如果音频输入空闲帧链表中有音频输入空闲帧。
				{
					//从音频输入空闲帧链表中取出第一个音频输入空闲帧。
					synchronized( m_AdoInptIdleFrmLnkLstPt )
					{
						m_AdoInptFrmPt = m_AdoInptIdleFrmLnkLstPt.getFirst();
						m_AdoInptIdleFrmLnkLstPt.removeFirst();
					}
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：从音频输入空闲帧链表中取出第一个音频输入空闲帧，音频输入空闲帧链表元素个数：" + m_AdoInptFrmLnkLstElmTotal + "。" );
				}
				else //如果音频输入空闲帧链表中没有音频输入空闲帧。
				{
					if( ( m_AdoInptFrmLnkLstElmTotal = m_AdoInptFrmLnkLstPt.size() ) <= 50 )
					{
						m_AdoInptFrmPt = new short[ m_FrmLen ]; //创建一个音频输入空闲帧。
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：音频输入空闲帧链表中没有音频输入空闲帧，创建一个音频输入空闲帧。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：音频输入帧链表中音频输入帧数量为" + m_AdoInptFrmLnkLstElmTotal + "已经超过上限50，不再创建一个音频输入空闲帧。" );
						SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
					}
				}

				if( m_AdoInptFrmPt != null ) //如果获取了一个音频输入空闲帧。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) m_LastTimeMsec = System.currentTimeMillis();

					//读取本次音频输入帧。
					m_AdoInptDvcPt.read( m_AdoInptFrmPt, 0, m_AdoInptFrmPt.length );

					//追加本次音频输入帧到音频输入帧链表。
					synchronized( m_AdoInptFrmLnkLstPt )
					{
						m_AdoInptFrmLnkLstPt.addLast( m_AdoInptFrmPt );
					}
					m_AdoInptFrmPt = null;

					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
					{
						m_NowTimeMsec = System.currentTimeMillis();
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：本次音频输入帧读取完毕，耗时 " + ( m_NowTimeMsec - m_LastTimeMsec ) + " 毫秒。" );
					}
				}

				if( m_AdoInptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：接收到退出请求，开始准备退出。" );
					break;
				}
			} //音频输入循环结束。

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：本线程已退出。" );
		}
	}
}