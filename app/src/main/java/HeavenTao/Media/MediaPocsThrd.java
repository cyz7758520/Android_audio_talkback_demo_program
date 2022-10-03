package HeavenTao.Media;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.SystemClock;
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

import HeavenTao.Data.*;

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

	class Msg
	{
		int m_MsgTyp;
		LinkedList< Object > m_MsgArgLnkLstPt;
	}
	public final LinkedList< Msg > m_MsgLnkLstPt; //存放消息链表的指针。

	public static final int MsgTypSetAdoInpt = 0;
	public static final int MsgTypSetAdoInptIsUseSystemAecNsAgc = MsgTypSetAdoInpt + 1;
	public static final int MsgTypSetAdoInptUseNoAec = MsgTypSetAdoInptIsUseSystemAecNsAgc + 1;
	public static final int MsgTypSetAdoInptUseSpeexAec = MsgTypSetAdoInptUseNoAec + 1;
	public static final int MsgTypSetAdoInptUseWebRtcAecm = MsgTypSetAdoInptUseSpeexAec + 1;
	public static final int MsgTypSetAdoInptUseWebRtcAec = MsgTypSetAdoInptUseWebRtcAecm + 1;
	public static final int MsgTypSetAdoInptUseSpeexWebRtcAec = MsgTypSetAdoInptUseWebRtcAec + 1;
	public static final int MsgTypSetAdoInptUseNoNs = MsgTypSetAdoInptUseSpeexWebRtcAec + 1;
	public static final int MsgTypSetAdoInptUseSpeexPrpocsNs = MsgTypSetAdoInptUseNoNs + 1;
	public static final int MsgTypSetAdoInptUseWebRtcNsx = MsgTypSetAdoInptUseSpeexPrpocsNs + 1;
	public static final int MsgTypSetAdoInptUseWebRtcNs = MsgTypSetAdoInptUseWebRtcNsx + 1;
	public static final int MsgTypSetAdoInptUseRNNoise = MsgTypSetAdoInptUseWebRtcNs + 1;
	public static final int MsgTypSetAdoInptIsUseSpeexPrpocsOther = MsgTypSetAdoInptUseRNNoise + 1;
	public static final int MsgTypSetAdoInptUsePcm = MsgTypSetAdoInptIsUseSpeexPrpocsOther + 1;
	public static final int MsgTypSetAdoInptUseSpeexEncd = MsgTypSetAdoInptUsePcm + 1;
	public static final int MsgTypSetAdoInptUseOpusEncd = MsgTypSetAdoInptUseSpeexEncd + 1;
	public static final int MsgTypSetAdoInptIsSaveAdoToFile = MsgTypSetAdoInptUseOpusEncd + 1;
	public static final int MsgTypSetAdoInptIsDrawAdoWavfmToSurface = MsgTypSetAdoInptIsSaveAdoToFile + 1;
	public static final int MsgTypSetAdoInptIsMute = MsgTypSetAdoInptIsDrawAdoWavfmToSurface + 1;

	public static final int MsgTypSetAdoOtpt = MsgTypSetAdoInptIsMute + 1;
	public static final int MsgTypAddAdoOtptStrm = MsgTypSetAdoOtpt + 1;
	public static final int MsgTypDelAdoOtptStrm = MsgTypAddAdoOtptStrm + 1;
	public static final int MsgTypSetAdoOtptStrmUsePcm = MsgTypDelAdoOtptStrm + 1;
	public static final int MsgTypSetAdoOtptStrmUseSpeexDecd = MsgTypSetAdoOtptStrmUsePcm + 1;
	public static final int MsgTypSetAdoOtptStrmUseOpusDecd = MsgTypSetAdoOtptStrmUseSpeexDecd + 1;
	public static final int MsgTypSetAdoOtptStrmIsUse = MsgTypSetAdoOtptStrmUseOpusDecd + 1;
	public static final int MsgTypSetAdoOtptIsSaveAdoToFile = MsgTypSetAdoOtptStrmIsUse + 1;
	public static final int MsgTypSetAdoOtptIsDrawAdoWavfmToSurface = MsgTypSetAdoOtptIsSaveAdoToFile + 1;
	public static final int MsgTypSetAdoOtptUseDvc = MsgTypSetAdoOtptIsDrawAdoWavfmToSurface + 1;
	public static final int MsgTypSetAdoOtptIsMute = MsgTypSetAdoOtptUseDvc + 1;

	public static final int MsgTypSetVdoInpt = MsgTypSetAdoOtptIsMute + 1;
	public static final int MsgTypSetVdoInptUseYU12 = MsgTypSetVdoInpt + 1;
	public static final int MsgTypSetVdoInptUseOpenH264Encd = MsgTypSetVdoInptUseYU12 + 1;
	public static final int MsgTypSetVdoInptUseSystemH264Encd = MsgTypSetVdoInptUseOpenH264Encd + 1;
	public static final int MsgTypSetVdoInptUseDvc = MsgTypSetVdoInptUseSystemH264Encd + 1;
	public static final int MsgTypSetVdoInptIsBlack = MsgTypSetVdoInptUseDvc + 1;

	public static final int MsgTypAddVdoOtptStrm = MsgTypSetVdoInptIsBlack + 1;
	public static final int MsgTypDelVdoOtptStrm = MsgTypAddVdoOtptStrm + 1;
	public static final int MsgTypSetVdoOtptStrm = MsgTypDelVdoOtptStrm + 1;
	public static final int MsgTypSetVdoOtptStrmUseYU12 = MsgTypSetVdoOtptStrm + 1;
	public static final int MsgTypSetVdoOtptStrmUseOpenH264Decd = MsgTypSetVdoOtptStrmUseYU12 + 1;
	public static final int MsgTypSetVdoOtptStrmUseSystemH264Decd = MsgTypSetVdoOtptStrmUseOpenH264Decd + 1;
	public static final int MsgTypSetVdoOtptStrmIsBlack = MsgTypSetVdoOtptStrmUseSystemH264Decd + 1;
	public static final int MsgTypSetVdoOtptStrmIsUse = MsgTypSetVdoOtptStrmIsBlack + 1;

	public static final int MsgTypSaveStngToFile = MsgTypSetVdoOtptStrmIsUse + 1;

	public static final int MsgTypSetIsUseAdoVdoInptOtpt = MsgTypSaveStngToFile + 1;

	public static Context m_AppCntxtPt; //存放应用程序上下文的指针。

	public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
	public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
	public Activity m_ShowToastActivityPt; //存放显示Toast界面的指针。

	int m_IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。
	int m_WakeLockIsUseAdoOtpt; //存放唤醒锁是否使用音频输出，为0表示不使用，为非0表示要使用。
	int m_WakeLockUseWhatAdoOtptDvc; //存放唤醒锁使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
	PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁的指针。
	PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

	public AdoInpt m_AdoInptPt; //存放音频输入的指针。

	public AdoOtpt m_AdoOtptPt; //存放音频输出的指针。

	public VdoInpt m_VdoInptPt; //存放视频输入的指针。

	public VdoOtpt m_VdoOtptPt; //存放视频输出的指针。

	//媒体处理线程的临时变量。
	short m_PcmAdoInptFrmPt[]; //存放PCM格式音频输入帧的指针。
	short m_PcmAdoOtptFrmPt[]; //存放PCM格式音频输出帧的指针。
	short m_PcmAdoRsltFrmPt[]; //存放PCM格式音频结果帧的指针。
	short m_PcmAdoTmpFrmPt[]; //存放PCM格式音频临时帧的指针。
	short m_PcmAdoSwapFrmPt[]; //存放PCM格式音频交换帧的指针。
	HTInt m_VoiceActStsPt; //存放语音活动状态，为1表示有语音活动，为0表示无语音活动。
	byte m_EncdAdoInptFrmPt[]; //存放已编码格式音频输入帧的指针。
	HTLong m_EncdAdoInptFrmLenPt; //存放已编码格式音频输入帧长度的指针，单位字节。
	HTInt m_EncdAdoInptFrmIsNeedTransPt; //存放已编码格式音频输入帧是否需要传输的指针，为1表示需要传输，为0表示不需要传输。
	VdoInpt.VdoInptFrmElm m_VdoInptFrmPt; //存放视频输入帧的指针。

	public Vstr m_ErrInfoVstrPt; //存放错误信息动态字符串的指针。

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
	public abstract void UserWriteAdoOtptFrm( int AdoOtptStrmIdx, short PcmAdoOtptFrmPt[], byte EncdAdoOtptFrmPt[], HTLong AdoOtptFrmLenPt );

	//用户定义的获取PCM格式音频输出帧函数。
	public abstract void UserGetPcmAdoOtptFrm( int AdoOtptStrmIdx, short PcmAdoOtptFrmPt[], long PcmAdoOtptFrmLen );

	//用户定义的写入视频输出帧函数。
	public abstract void UserWriteVdoOtptFrm( int VdoOtptStrmIdx, byte YU12VdoOtptFrmPt[], HTInt YU12VdoInptFrmWidthPt, HTInt YU12VdoInptFrmHeightPt, byte EncdVdoOtptFrmPt[], HTLong VdoOtptFrmLenPt );

	//用户定义的获取YU12格式视频输出帧函数。
	public abstract void UserGetYU12VdoOtptFrm( int VdoOtptStrmIdx, byte YU12VdoOtptFrmPt[], int YU12VdoOtptFrmWidth, int YU12VdoOtptFrmHeight );

	//构造函数。
	public MediaPocsThrd( Context AppCntxtPt )
	{
		m_MsgLnkLstPt = new LinkedList< Msg >();

		if( AppCntxtPt != null ) m_AppCntxtPt = AppCntxtPt;

		m_AdoInptPt = new AdoInpt();
		m_AdoInptPt.m_MediaPocsThrdPt = this;
		SetAdoInpt( 8000, 20 );

		m_AdoOtptPt = new AdoOtpt();
		m_AdoOtptPt.m_MediaPocsThrdPt = this;
		m_AdoOtptPt.m_AdoOtptStrmLnkLstPt = new LinkedList< AdoOtpt.AdoOtptStrm >();
		SetAdoOtpt( 8000, 20 );

		m_VdoInptPt = new VdoInpt();
		m_VdoInptPt.m_MediaPocsThrdPt = this;
		m_VdoInptPt.m_MaxSmplRate = 15;
		m_VdoInptPt.m_FrmWidth = 480;
		m_VdoInptPt.m_FrmHeight = 640;
		m_VdoInptPt.m_ScreenRotate = 0;
		m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt = null;
		SetVdoInptUseDvc( 0, -1, -1 );

		m_VdoOtptPt = new VdoOtpt();
		m_VdoOtptPt.m_MediaPocsThrdPt = this;
		m_VdoOtptPt.m_VdoOtptStrmLnkLstPt = new LinkedList< VdoOtpt.VdoOtptStrm >();
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
			WakeLockInitOrDstoy( m_IsUseWakeLock, m_WakeLockIsUseAdoOtpt, m_WakeLockUseWhatAdoOtptDvc );
		}
	}

	//设置音频输入。
	public void SetAdoInpt( int SmplRate, int FrmLenMsec )
	{
		if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
			( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
		{
			return;
		}

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInpt;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SmplRate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FrmLenMsec * SmplRate / 1000 );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
	public void SetAdoInptIsUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptIsUseSystemAecNsAgc;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseSystemAecNsAgc );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入不使用声学回音消除器。
	public void SetAdoInptUseNoAec()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseNoAec;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用Speex声学回音消除器。
	public void SetAdoInptUseSpeexAec( int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesAct, int IsSaveMemFile, String MemFileFullPathStrPt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseSpeexAec;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FilterLen );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseRec );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoMultiple );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoCont );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoSupes );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoSupesAct );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsSaveMemFile );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( MemFileFullPathStrPt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用WebRtc定点版声学回音消除器。
	public void SetAdoInptUseWebRtcAecm( int IsUseCNGMode, int EchoMode, int Delay )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseWebRtcAecm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseCNGMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Delay );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用WebRtc浮点版声学回音消除器。
	public void SetAdoInptUseWebRtcAec( int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseWebRtcAec;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EchoMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Delay );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseDelayAgstcMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseExtdFilterMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseRefinedFilterAdaptAecMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseAdaptAdjDelay );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsSaveMemFile );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( MemFileFullPathStrPt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用SpeexWebRtc三重声学回音消除器。
	public void SetAdoInptUseSpeexWebRtcAec( int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseSpeexWebRtcAec;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WorkMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecFilterLen );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecIsUseRec );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecEchoMultiple );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecEchoCont );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecEchoSupes );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SpeexAecEchoSupesAct );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecmIsUseCNGMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecmEchoMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecmDelay );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecEchoMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecDelay );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecIsUseDelayAgstcMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecIsUseExtdFilterMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecIsUseRefinedFilterAdaptAecMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( WebRtcAecIsUseAdaptAdjDelay );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseSameRoomAec );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SameRoomEchoMinDelay );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入不使用噪音抑制器。
	public void SetAdoInptUseNoNs()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseNoNs;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用Speex预处理器的噪音抑制。
	public void SetAdoInptUseSpeexPrpocsNs( int IsUseNs, int NoiseSupes, int IsUseDereverb )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseSpeexPrpocsNs;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseNs );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( NoiseSupes );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseDereverb );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用WebRtc定点版噪音抑制器。
	public void SetAdoInptUseWebRtcNsx( int PolicyMode )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseWebRtcNsx;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( PolicyMode );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用WebRtc浮点版噪音抑制器。
	public void SetAdoInptUseWebRtcNs( int PolicyMode )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseWebRtcNs;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( PolicyMode );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用RNNoise噪音抑制器。
	public void SetAdoInptUseRNNoise()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseRNNoise;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入是否使用Speex预处理器的其他功能。
	public void SetAdoInptIsUseSpeexPrpocsOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptIsUseSpeexPrpocsOther;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseOther );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseVad );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VadProbStart );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VadProbCont );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseAgc );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AgcIncrement );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AgcDecrement );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AgcLevel );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AgcMaxGain );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用PCM原始数据。
	public void SetAdoInptUsePcm()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUsePcm;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用Speex编码器。
	public void SetAdoInptUseSpeexEncd( int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseSpeexEncd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( UseCbrOrVbr );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Qualt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Cmplxt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( PlcExptLossRate );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入要使用Opus编码器。
	public void SetAdoInptUseOpusEncd()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptUseOpusEncd;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入是否保存音频到文件。
	public void SetAdoInptIsSaveAdoToFile( int IsSaveAdoToFile, String AdoInptFileFullPathStrPt, String AdoRsltFileFullPathStrPt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptIsSaveAdoToFile;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsSaveAdoToFile );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoInptFileFullPathStrPt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoRsltFileFullPathStrPt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入是否绘制音频波形到Surface。
	public void SetAdoInptIsDrawAdoWavfmToSurface( int IsDrawAdoWavfmToSurface, SurfaceView AdoInptWavfmSurfacePt, SurfaceView AdoRsltWavfmSurfacePt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptIsDrawAdoWavfmToSurface;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsDrawAdoWavfmToSurface );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoInptWavfmSurfacePt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoRsltWavfmSurfacePt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输入是否静音。
	public void SetAdoInptIsMute( int IsMute )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoInptIsMute;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsMute );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出。
	public void SetAdoOtpt( int SmplRate, int FrmLenMsec )
	{
		if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
			( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
		{
			return;
		}

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtpt;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( SmplRate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FrmLenMsec * SmplRate / 1000 );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//添加音频输出流。
	public void AddAdoOtptStrm( int AdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypAddAdoOtptStrm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出流要使用PCM原始数据。
	public void SetAdoOtptStrmUsePcm( int AdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptStrmUsePcm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出流要使用Speex解码器。
	public void SetAdoOtptStrmUseSpeexDecd( int AdoOtptStrmIdx, int IsUsePrcplEnhsmt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptStrmUseSpeexDecd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUsePrcplEnhsmt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出流要使用Opus编码器。
	public void SetAdoOtptStrmUseOpusDecd( int AdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptStrmUseOpusDecd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出流是否使用。
	public void SetAdoOtptStrmIsUse( int AdoOtptStrmIdx, int IsUseAdoOtptStrm )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptStrmIsUse;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseAdoOtptStrm );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//删除音频输出流。
	public void DelAdoOtptStrm( int AdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypDelAdoOtptStrm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出是否保存音频到文件。
	public void SetAdoOtptIsSaveAdoToFile( int IsSaveAdoToFile, String AdoOtptFileFullPathStrPt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptIsSaveAdoToFile;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsSaveAdoToFile );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptFileFullPathStrPt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出是否绘制音频波形到Surface。
	public void SetAdoOtptIsDrawAdoWavfmToSurface( int IsDrawAudioToSurface, SurfaceView AdoOtptWavfmSurfacePt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptIsDrawAdoWavfmToSurface;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsDrawAudioToSurface );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( AdoOtptWavfmSurfacePt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置音频输出使用的设备。
	public void SetAdoOtptUseDvc( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
	{
		if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) ) //如果使用听筒，则不能使用媒体类型音频输出流。
		{
			return;
		}

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptUseDvc;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( UseSpeakerOrEarpiece );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( UseVoiceCallOrMusic );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}

		m_WakeLockUseWhatAdoOtptDvc = UseSpeakerOrEarpiece;
		if( m_RunFlag == RUN_FLAG_INIT || m_RunFlag == RUN_FLAG_POCS ) //如果本线程为刚开始运行正在初始化或初始化完毕正在循环处理帧，就立即修改唤醒锁。
		{
			WakeLockInitOrDstoy( m_IsUseWakeLock, m_WakeLockIsUseAdoOtpt, m_WakeLockUseWhatAdoOtptDvc );
		}
	}

	//设置音频输出是否静音。
	public void SetAdoOtptIsMute( int IsMute )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetAdoOtptIsMute;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsMute );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输入。
	public void SetVdoInpt( int MaxSmplRate, int FrmWidth, int FrmHeight, int ScreenRotate, HTSurfaceView VdoInptPrvwSurfaceViewPt )
	{
		if( ( ( MaxSmplRate < 1 ) || ( MaxSmplRate > 60 ) ) || //如果采样频率不正确。
			( ( FrmWidth <= 0 ) || ( ( FrmWidth & 1 ) != 0 ) ) || //如果帧的宽度不正确。
			( ( FrmHeight <= 0 ) || ( ( FrmHeight & 1 ) != 0 ) ) || //如果帧的高度不正确。
			( ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) || //如果屏幕旋转的角度不正确。
			( VdoInptPrvwSurfaceViewPt == null ) ) //如果视频预览SurfaceView的指针不正确。
		{
			return;
		}

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInpt;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( MaxSmplRate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FrmWidth );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FrmHeight );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( ScreenRotate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoInptPrvwSurfaceViewPt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输入要使用YU12原始数据。
	public void SetVdoInptUseYU12()
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInptUseYU12;
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输入要使用OpenH264编码器。
	public void SetVdoInptUseOpenH264Encd( int VdoType, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvl, int Cmplxt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInptUseOpenH264Encd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoType );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EncdBitrate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( BitrateCtrlMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IDRFrmIntvl );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Cmplxt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输入要使用系统自带H264编码器。
	public void SetVdoInptUseSystemH264Encd( int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvlTimeSec, int Cmplxt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInptUseSystemH264Encd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( EncdBitrate );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( BitrateCtrlMode );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IDRFrmIntvlTimeSec );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( Cmplxt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
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

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInptUseDvc;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( UseFrontOrBack );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( FrontCameraDvcId );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( BackCameraDvcId );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输入是否黑屏。
	public void SetVdoInptIsBlack( int IsBlack )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoInptIsBlack;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsBlack );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//添加视频输出流。
	public void AddVdoOtptStrm( int VdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypAddVdoOtptStrm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//删除视频输出流。
	public void DelVdoOtptStrm( int VdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypDelVdoOtptStrm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流。
	public void SetVdoOtptStrm( int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt, float VdoOtptDspyScale )
	{
		if( ( VdoOtptDspySurfaceViewPt == null ) || //如果视频显示SurfaceView的指针不正确。
			( VdoOtptDspyScale <= 0 ) ) //如果视频输出显示缩放倍数不正确。
		{
			return;
		}

		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrm;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptDspySurfaceViewPt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptDspyScale );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流要使用YU12原始数据。
	public void SetVdoOtptStrmUseYU12( int VdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrmUseYU12;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流要使用OpenH264解码器。
	public void SetVdoOtptStrmUseOpenH264Decd( int VdoOtptStrmIdx, int DecdThrdNum )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrmUseOpenH264Decd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( DecdThrdNum );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流要使用系统自带H264解码器。
	public void SetVdoOtptStrmUseSystemH264Decd( int VdoOtptStrmIdx )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrmUseSystemH264Decd;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流是否黑屏。
	public void SetVdoOtptStrmIsBlack( int VdoOtptStrmIdx, int IsBlack )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrmIsBlack;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsBlack );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置视频输出流是否使用。
	public void SetVdoOtptStrmIsUse( int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetVdoOtptStrmIsUse;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( VdoOtptStrmIdx );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseVdoOtptStrm );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//保存设置到文件。
	public void SaveStngToFile( String StngFileFullPathStrPt )
	{
		Msg p_MsgPt = new Msg();

		p_MsgPt.m_MsgTyp = MsgTypSaveStngToFile;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( StngFileFullPathStrPt );

		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}
	}

	//设置是否使用音视频输入输出。
	public void SetIsUseAdoVdoInptOtpt( int IsUseAdoInpt, int IsUseAdoOtpt, int IsUseVdoInpt, int IsUseVdoOtpt )
	{
		Msg p_MsgPt = new Msg();
		p_MsgPt.m_MsgTyp = MsgTypSetIsUseAdoVdoInptOtpt;
		p_MsgPt.m_MsgArgLnkLstPt = new LinkedList< Object >();
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseAdoInpt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseAdoOtpt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseVdoInpt );
		p_MsgPt.m_MsgArgLnkLstPt.addLast( IsUseVdoOtpt );
		synchronized( m_MsgLnkLstPt )
		{
			m_MsgLnkLstPt.addLast( p_MsgPt );
		}

		m_WakeLockIsUseAdoOtpt = IsUseAdoOtpt;
		if( m_RunFlag == RUN_FLAG_INIT || m_RunFlag == RUN_FLAG_POCS ) //如果本线程为刚开始运行正在初始化或初始化完毕正在循环处理帧，就立即修改唤醒锁。
		{
			WakeLockInitOrDstoy( m_IsUseWakeLock, m_WakeLockIsUseAdoOtpt, m_WakeLockUseWhatAdoOtptDvc );
		}
	}

	//初始化或销毁唤醒锁。
	private void WakeLockInitOrDstoy( int IsInitOrDstoy, int IsUseAdoOtpt, int UseWhatAdoOtptDvc )
	{
		if( IsInitOrDstoy != 0 ) //如果要初始化唤醒锁。
		{
			if( IsUseAdoOtpt != 0 && UseWhatAdoOtptDvc != 0 ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
			{
				if( m_ProximityScreenOffWakeLockPt == null ) //如果接近息屏唤醒锁还没有初始化。
				{
					m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) m_AppCntxtPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
					if( m_ProximityScreenOffWakeLockPt != null )
					{
						m_ProximityScreenOffWakeLockPt.acquire();
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化接近息屏唤醒锁成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：初始化接近息屏唤醒锁失败。" );
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
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化屏幕键盘全亮唤醒锁成功。" );
				}
				else
				{
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：初始化屏幕键盘全亮唤醒锁失败。" );
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

	//请求权限。
	public static void RqstPrmsn( Activity RqstActivity, int IsRqstInternet, int IsRqstModifyAudioStng, int IsRqstForegroundService, int IsRqstWakeLock, int IsRqstReadPhoneState, int IsRqstRecordAdo, int IsRqstCamera, int DeniedIsPrintLogcat, int DeniedIsShowToast )
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

		//检测读取电话状态权限。
		if( ( IsRqstReadPhoneState != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.READ_PHONE_STATE );
			p_DeniedPermissionStrPt += "读取电话状态  ";
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

	//初始化媒体处理线程的临时变量。
	private void MediaPocsThrdTmpVarInit()
	{
		if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
		{
			m_PcmAdoInptFrmPt = null;
			m_PcmAdoOtptFrmPt = null;
			if( ( m_PcmAdoRsltFrmPt == null ) || ( m_PcmAdoRsltFrmPt.length != m_AdoInptPt.m_FrmLen ) )
			{
				m_PcmAdoRsltFrmPt = new short[ m_AdoInptPt.m_FrmLen ];
				m_PcmAdoTmpFrmPt = new short[ m_AdoInptPt.m_FrmLen ];
			}
			m_PcmAdoSwapFrmPt = null;
			if( m_VoiceActStsPt == null ) m_VoiceActStsPt = new HTInt( 1 ); //语音活动状态预设为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
			else m_VoiceActStsPt.m_Val = 1;
			if( m_AdoInptPt.m_UseWhatEncd != 0 )
			{
				if( ( m_EncdAdoInptFrmPt == null ) || ( m_EncdAdoInptFrmPt.length != m_AdoInptPt.m_FrmLen ) ) m_EncdAdoInptFrmPt = new byte[ m_AdoInptPt.m_FrmLen ];
				if( m_EncdAdoInptFrmLenPt == null ) m_EncdAdoInptFrmLenPt = new HTLong( 0 );
				if( m_EncdAdoInptFrmIsNeedTransPt == null ) m_EncdAdoInptFrmIsNeedTransPt = new HTInt( 1 ); //已编码格式音频输入帧是否需要传输预设为1，为了让在不使用非连续传输的情况下永远都是需要传输。
				else m_EncdAdoInptFrmIsNeedTransPt.m_Val = 1;
			}
		}
		else
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
		}
		m_VdoInptFrmPt = null;
		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化媒体处理线程的临时变量成功。" );
	}

	//销毁媒体处理线程的临时变量。
	private void MediaPocsThrdTmpVarDstoy()
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
	}

	//消息处理。
	private int MsgPocs()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_MsgLnkLstPt.size() > 0 )
			{
				synchronized( m_MsgLnkLstPt )
				{
					while( m_MsgLnkLstPt.size() > 0 )
					{
						Msg p_MsgPt = m_MsgLnkLstPt.getFirst();
						m_MsgLnkLstPt.removeFirst();

						switch( p_MsgPt.m_MsgTyp )
						{
							case MsgTypSetAdoInpt:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
								{
									m_AdoInptPt.Dstoy();
									if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) m_AdoOtptPt.Dstoy();
								}

								m_AdoInptPt.m_SmplRate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_FrmLen = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
								{
									if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) if( m_AdoOtptPt.Init() != 0 ) break Out;
									m_AdoInptPt.SetIsCanUseAec();
									if( m_AdoInptPt.Init() != 0 ) break Out;
									MediaPocsThrdTmpVarInit();
								}
								break;
							}
							case MsgTypSetAdoInptIsUseSystemAecNsAgc:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
								{
									m_AdoInptPt.DvcAndThrdDstoy();
									if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
								}

								m_AdoInptPt.m_IsUseSystemAecNsAgc = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
								{
									if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
									if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
								}
								break;
							}
							case MsgTypSetAdoInptUseNoAec:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

								m_AdoInptPt.m_UseWhatAec = 0;

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.AecInit() != 0 ) break Out; m_AdoInptPt.SetIsCanUseAec(); }
								break;
							}
							case MsgTypSetAdoInptUseSpeexAec:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

								m_AdoInptPt.m_UseWhatAec = 1;
								m_AdoInptPt.m_SpeexAecFilterLen = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_SpeexAecIsUseRec = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_SpeexAecEchoMultiple = ( Float ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_AdoInptPt.m_SpeexAecEchoCont = ( Float ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_AdoInptPt.m_SpeexAecEchoSupes = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );
								m_AdoInptPt.m_SpeexAecEchoSupesAct = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 5 );
								m_AdoInptPt.m_SpeexAecIsSaveMemFile = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 6 );
								m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 7 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.AecInit() != 0 ) break Out; m_AdoInptPt.SetIsCanUseAec(); }
								break;
							}
							case MsgTypSetAdoInptUseWebRtcAecm:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

								m_AdoInptPt.m_UseWhatAec = 2;
								m_AdoInptPt.m_WebRtcAecmIsUseCNGMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_WebRtcAecmEchoMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_WebRtcAecmDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.AecInit() != 0 ) break Out; m_AdoInptPt.SetIsCanUseAec(); }
								break;
							}
							case MsgTypSetAdoInptUseWebRtcAec:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

								m_AdoInptPt.m_UseWhatAec = 3;
								m_AdoInptPt.m_WebRtcAecEchoMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_WebRtcAecDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );
								m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 5 );
								m_AdoInptPt.m_WebRtcAecIsSaveMemFile = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 6 );
								m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 7 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.AecInit() != 0 ) break Out; m_AdoInptPt.SetIsCanUseAec(); }
								break;
							}
							case MsgTypSetAdoInptUseSpeexWebRtcAec:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

								m_AdoInptPt.m_UseWhatAec = 4;
								m_AdoInptPt.m_SpeexWebRtcAecWorkMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMultiple = ( Float ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCont = ( Float ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 5 );
								m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 6 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 7 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 8 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 9 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 10 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 11 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 12 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 13 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 14 );
								m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 15 );
								m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 16 );
								m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 17 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.AecInit() != 0 ) break Out; m_AdoInptPt.SetIsCanUseAec(); }
								break;
							}
							case MsgTypSetAdoInptUseNoNs:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.NsDstoy(); m_AdoInptPt.SpeexPrpocsDstoy(); }

								m_AdoInptPt.m_UseWhatNs = 0;

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.NsInit() != 0 ) break Out;
								break;
							}
							case MsgTypSetAdoInptUseSpeexPrpocsNs:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.NsDstoy(); m_AdoInptPt.SpeexPrpocsDstoy(); }

								m_AdoInptPt.m_UseWhatNs = 1;
								m_AdoInptPt.m_SpeexPrpocsIsUseNs = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_SpeexPrpocsNoiseSupes = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_SpeexPrpocsIsUseDereverb = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.NsInit() != 0 ) break Out; if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out; }
								break;
							}
							case MsgTypSetAdoInptUseWebRtcNsx:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.NsDstoy(); m_AdoInptPt.SpeexPrpocsDstoy(); }

								m_AdoInptPt.m_UseWhatNs = 2;
								m_AdoInptPt.m_WebRtcNsxPolicyMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.NsInit() != 0 ) break Out; if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out; }
								break;
							}
							case MsgTypSetAdoInptUseWebRtcNs:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.NsDstoy(); m_AdoInptPt.SpeexPrpocsDstoy(); }

								m_AdoInptPt.m_UseWhatNs = 3;
								m_AdoInptPt.m_WebRtcNsPolicyMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.NsInit() != 0 ) break Out; if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out; }
								break;
							}
							case MsgTypSetAdoInptUseRNNoise:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.NsDstoy(); m_AdoInptPt.SpeexPrpocsDstoy(); }

								m_AdoInptPt.m_UseWhatNs = 4;

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.NsInit() != 0 ) break Out; if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out; }
								break;
							}
							case MsgTypSetAdoInptIsUseSpeexPrpocsOther:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.SpeexPrpocsDstoy();

								m_AdoInptPt.m_IsUseSpeexPrpocsOther = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_SpeexPrpocsIsUseVad = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_SpeexPrpocsVadProbStart = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_AdoInptPt.m_SpeexPrpocsVadProbCont = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_AdoInptPt.m_SpeexPrpocsIsUseAgc = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );
								m_AdoInptPt.m_SpeexPrpocsAgcIncrement = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 5 );
								m_AdoInptPt.m_SpeexPrpocsAgcDecrement = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 6 );
								m_AdoInptPt.m_SpeexPrpocsAgcLevel = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 7 );
								m_AdoInptPt.m_SpeexPrpocsAgcMaxGain = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 8 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out; m_VoiceActStsPt.m_Val = 1; }
								break;
							}
							case MsgTypSetAdoInptUsePcm:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

								m_AdoInptPt.m_UseWhatEncd = 0;

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
								MediaPocsThrdTmpVarInit();
								break;
							}
							case MsgTypSetAdoInptUseSpeexEncd:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

								m_AdoInptPt.m_UseWhatEncd = 1;
								m_AdoInptPt.m_SpeexEncdUseCbrOrVbr = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_SpeexEncdQualt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_SpeexEncdCmplxt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_AdoInptPt.m_SpeexEncdPlcExptLossRate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
								MediaPocsThrdTmpVarInit();
								break;
							}
							case MsgTypSetAdoInptUseOpusEncd:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

								m_AdoInptPt.m_UseWhatEncd = 2;

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
								MediaPocsThrdTmpVarInit();
								break;
							}
							case MsgTypSetAdoInptIsSaveAdoToFile:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.WaveFileWriterDstoy();

								m_AdoInptPt.m_IsSaveAdoToFile = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_AdoInptFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_AdoRsltFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.WaveFileWriterInit() != 0 ) break Out;
								break;
							}
							case MsgTypSetAdoInptIsDrawAdoWavfmToSurface:
							{
								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.WavfmDstoy();

								m_AdoInptPt.m_IsDrawAdoWavfmToSurface = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoInptPt.m_AdoInptWavfmSurfacePt = ( SurfaceView ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_AdoInptPt.m_AdoRsltWavfmSurfacePt = ( SurfaceView ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );

								if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) if( m_AdoInptPt.WavfmInit() != 0 ) break Out;
								break;
							}
							case MsgTypSetAdoInptIsMute:
							{
								m_AdoInptPt.m_AdoInptIsMute = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								break;
							}
							case MsgTypSetAdoOtpt:
							{
								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 )
								{
									if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
									m_AdoOtptPt.Dstoy();
								}

								m_AdoOtptPt.m_SmplRate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoOtptPt.m_FrmLen = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );

								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 )
								{
									if( m_AdoOtptPt.Init() != 0 ) break Out;
									if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.SetIsCanUseAec(); if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out; }
									else { m_AdoOtptPt.m_AdoOtptDvcPt.play(); m_AdoOtptPt.m_AdoOtptThrdPt.start(); }
								}
								break;
							}
							case MsgTypAddAdoOtptStrm:
							{
								m_AdoOtptPt.AddAdoOtptStrm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypDelAdoOtptStrm:
							{
								m_AdoOtptPt.DelAdoOtptStrm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetAdoOtptStrmUsePcm:
							{
								m_AdoOtptPt.SetAdoOtptStrmUsePcm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetAdoOtptStrmUseSpeexDecd:
							{
								m_AdoOtptPt.SetAdoOtptStrmUseSpeexDecd( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ) );
								break;
							}
							case MsgTypSetAdoOtptStrmUseOpusDecd:
							{
								m_AdoOtptPt.SetAdoOtptStrmUseOpusDecd( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetAdoOtptStrmIsUse:
							{
								m_AdoOtptPt.SetAdoOtptStrmIsUse( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ) );
								break;
							}
							case MsgTypSetAdoOtptIsSaveAdoToFile:
							{
								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) m_AdoOtptPt.WaveFileWriterDstoy();

								m_AdoOtptPt.m_IsSaveAdoToFile = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoOtptPt.m_AdoOtptFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );

								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) if( m_AdoOtptPt.WaveFileWriterInit() != 0 ) break Out;
								break;
							}
							case MsgTypSetAdoOtptIsDrawAdoWavfmToSurface:
							{
								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) m_AdoOtptPt.WavfmDstoy();

								m_AdoOtptPt.m_IsDrawAdoWavfmToSurface = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoOtptPt.m_AdoOtptWavfmSurfacePt = ( SurfaceView ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );

								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) if( m_AdoOtptPt.WavfmInit() != 0 ) break Out;
								break;
							}
							case MsgTypSetAdoOtptUseDvc:
							{
								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 )
								{
									if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
									m_AdoOtptPt.DvcAndThrdDstoy();
								}

								m_AdoOtptPt.m_UseWhatAdoOtptDvc = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_AdoOtptPt.m_UseWhatAdoOtptStreamType = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								SetIsUseWakeLock( m_IsUseWakeLock ); //重新初始化唤醒锁。

								if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 )
								{
									if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
									if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) { m_AdoInptPt.SetIsCanUseAec(); if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out; }
									else { m_AdoOtptPt.m_AdoOtptDvcPt.play(); m_AdoOtptPt.m_AdoOtptThrdPt.start(); }
								}
								break;
							}
							case MsgTypSetAdoOtptIsMute:
							{
								m_AdoOtptPt.m_AdoOtptIsMute = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								break;
							}
							case MsgTypSetVdoInpt:
							{
								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) m_VdoInptPt.Dstoy();

								m_VdoInptPt.m_MaxSmplRate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_VdoInptPt.m_FrmWidth = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_VdoInptPt.m_FrmHeight = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_VdoInptPt.m_ScreenRotate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt = ( HTSurfaceView ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );

								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
								break;
							}
							case MsgTypSetVdoInptUseYU12:
							{
								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) m_VdoInptPt.Dstoy();

								m_VdoInptPt.m_UseWhatEncd = 0;

								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
								break;
							}
							case MsgTypSetVdoInptUseOpenH264Encd:
							{
								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) m_VdoInptPt.Dstoy();

								m_VdoInptPt.m_UseWhatEncd = 1;
								m_VdoInptPt.m_OpenH264EncdVdoType = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_VdoInptPt.m_OpenH264EncdEncdBitrate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );
								m_VdoInptPt.m_OpenH264EncdCmplxt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 4 );

								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
								break;
							}
							case MsgTypSetVdoInptUseSystemH264Encd:
							{
								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) m_VdoInptPt.Dstoy();

								m_VdoInptPt.m_UseWhatEncd = 2;
								m_VdoInptPt.m_SystemH264EncdEncdBitrate = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								m_VdoInptPt.m_SystemH264EncdCmplxt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );

								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
								break;
							}
							case MsgTypSetVdoInptUseDvc:
							{
								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) m_VdoInptPt.Dstoy();

								m_VdoInptPt.m_UseWhatVdoInptDvc = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								m_VdoInptPt.m_FrontCameraDvcId = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								m_VdoInptPt.m_BackCameraDvcId = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );

								if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
								break;
							}
							case MsgTypSetVdoInptIsBlack:
							{
								m_VdoInptPt.m_VdoInptIsBlack = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								break;
							}
							case MsgTypAddVdoOtptStrm:
							{
								m_VdoOtptPt.AddVdoOtptStrm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypDelVdoOtptStrm:
							{
								m_VdoOtptPt.DelVdoOtptStrm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetVdoOtptStrm:
							{
								m_VdoOtptPt.SetVdoOtptStrm( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( HTSurfaceView ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ), ( Float ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 ) );
								break;
							}
							case MsgTypSetVdoOtptStrmUseYU12:
							{
								m_VdoOtptPt.SetVdoOtptStrmUseYU12( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetVdoOtptStrmUseOpenH264Decd:
							{
								m_VdoOtptPt.SetVdoOtptStrmUseOpenH264Decd( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ) );
								break;
							}
							case MsgTypSetVdoOtptStrmUseSystemH264Decd:
							{
								m_VdoOtptPt.SetVdoOtptStrmUseSystemH264Decd( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ) );
								break;
							}
							case MsgTypSetVdoOtptStrmIsBlack:
							{
								m_VdoOtptPt.SetVdoOtptStrmIsBlack( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ) );
								break;
							}
							case MsgTypSetVdoOtptStrmIsUse:
							{
								m_VdoOtptPt.SetVdoOtptStrmIsUse( ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 ) );
								break;
							}
							case MsgTypSetIsUseAdoVdoInptOtpt:
							{
								int p_IsUseAdoInpt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								int p_IsUseAdoOtpt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 1 );
								int p_IsUseVdoInpt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 2 );
								int p_IsUseVdoOtpt = ( Integer ) p_MsgPt.m_MsgArgLnkLstPt.get( 3 );

								if( p_IsUseAdoInpt < 0 ) p_IsUseAdoInpt = m_AdoInptPt.m_IsUseAdoInpt;
								if( p_IsUseAdoOtpt < 0 ) p_IsUseAdoOtpt = m_AdoOtptPt.m_IsUseAdoOtpt;
								if( p_IsUseVdoInpt < 0 ) p_IsUseVdoInpt = m_VdoInptPt.m_IsUseVdoInpt;
								if( p_IsUseVdoOtpt < 0 ) p_IsUseVdoOtpt = m_VdoOtptPt.m_IsUseVdoOtpt;

								if( p_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
								{
									if( m_AdoOtptPt.m_IsUseAdoOtpt == 0 ) //如果当前不使用音频输出。
									{
										if( m_AdoOtptPt.Init() != 0 ) break Out;
										if( p_IsUseAdoInpt != 0 ) //如果要使用音频输入。
										{
											if( m_AdoInptPt.m_IsUseAdoInpt == 0 ) //如果当前不使用音频输入。
											{
												m_AdoOtptPt.m_IsUseAdoOtpt = 1;
											}
										}
										else //如果不使用音频输入。
										{
											m_AdoOtptPt.m_IsUseAdoOtpt = 1;
											m_AdoOtptPt.m_AdoOtptDvcPt.play();
											m_AdoOtptPt.m_AdoOtptThrdPt.start();
										}
									}
									else //如果当前要使用音频输出。
									{
										if( p_IsUseAdoInpt != 0 ) //如果要使用音频输入。
										{
											if( m_AdoInptPt.m_IsUseAdoInpt == 0 ) //如果当前不使用音频输入。
											{
												m_AdoOtptPt.DvcAndThrdDstoy(); //销毁音频输出设备和线程，因为要音频输入线程去启动音频输出设备和线程。
												if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
											}
										}
									}
								}
								else //如果不使用音频输出。
								{
									if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果当前要使用音频输出。
									{
										m_AdoOtptPt.m_IsUseAdoOtpt = 0;
										m_AdoOtptPt.Dstoy();
									}
								}

								if( p_IsUseAdoInpt != 0 ) //如果要使用音频输入。
								{
									if( m_AdoInptPt.m_IsUseAdoInpt == 0 ) //如果当前不使用音频输入。
									{
										m_AdoInptPt.m_IsUseAdoInpt = 1;
										m_AdoInptPt.SetIsCanUseAec();
										if( m_AdoInptPt.Init() != 0 ) break Out; //在音频输出初始化后再初始化音频输入，因为要音频输入线程去启动音频输出设备和线程。
										MediaPocsThrdTmpVarInit();
									}
									else //如果当前要使用音频输入。
									{
										if( p_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
										{
											if( m_AdoOtptPt.m_IsUseAdoOtpt == 0 ) //如果当前不使用音频输出。
											{
												m_AdoOtptPt.m_IsUseAdoOtpt = 1;
												m_AdoInptPt.DvcAndThrdDstoy();
												m_AdoInptPt.SetIsCanUseAec();
												if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
											}
										}
										else //如果不使用音频输出。
										{
											m_AdoInptPt.SetIsCanUseAec();
										}
									}
								}
								else //如果不使用音频输入。
								{
									if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) //如果当前要使用音频输入。
									{
										m_AdoInptPt.m_IsUseAdoInpt = 0;
										m_AdoInptPt.Dstoy();
										m_AdoInptPt.SetIsCanUseAec();
										MediaPocsThrdTmpVarInit();
									}
								}

								if( p_IsUseVdoInpt != 0 ) //如果要使用视频输入。
								{
									if( m_VdoInptPt.m_IsUseVdoInpt == 0 ) //如果当前不使用视频输入。
									{
										m_VdoInptPt.m_IsUseVdoInpt = 1;
										if( m_VdoInptPt.Init() != 0 ) break Out;
									}
								}
								else //如果不使用视频输入。
								{
									if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) //如果当前要使用音频输入。
									{
										m_VdoInptPt.m_IsUseVdoInpt = 0;
										m_VdoInptPt.Dstoy();
									}
								}

								if( p_IsUseVdoOtpt != 0 ) //如果要使用视频输出。
								{
									if( m_VdoOtptPt.m_IsUseVdoOtpt == 0 ) //如果当前不使用视频输出。
									{
										m_VdoOtptPt.m_IsUseVdoOtpt = 1;
										if( m_VdoOtptPt.Init() != 0 ) break Out;
									}
								}
								else //如果不使用视频输出。
								{
									if( m_VdoOtptPt.m_IsUseVdoOtpt != 0 ) //如果当前要使用音频输出。
									{
										m_VdoOtptPt.m_IsUseVdoOtpt = 0;
										m_VdoOtptPt.Dstoy();
									}
								}
								break;
							}
							case MsgTypSaveStngToFile:
							{
								String p_StngFileFullPathStrPt = ( String ) p_MsgPt.m_MsgArgLnkLstPt.get( 0 );
								File p_StngFilePt = new File( p_StngFileFullPathStrPt );

								try
								{
									if( !p_StngFilePt.exists() )
									{
										p_StngFilePt.createNewFile();
									}
									FileWriter p_StngFileWriterPt = new FileWriter( p_StngFilePt );

									p_StngFileWriterPt.write( "m_AppCntxtPt：" + m_AppCntxtPt + "\n" );
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
									p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptWavfmSurfacePt：" + m_AdoInptPt.m_AdoInptWavfmSurfacePt + "\n" );
									p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoRsltWavfmSurfacePt：" + m_AdoInptPt.m_AdoRsltWavfmSurfacePt + "\n" );
									p_StngFileWriterPt.write( "\n" );
									p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptDvcBufSz：" + m_AdoInptPt.m_AdoInptDvcBufSz + "\n" );
									p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptIsMute：" + m_AdoInptPt.m_AdoInptIsMute + "\n" );
									p_StngFileWriterPt.write( "\n" );

									p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsUseAdoOtpt：" + m_AdoOtptPt.m_IsUseAdoOtpt + "\n" );
									p_StngFileWriterPt.write( "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_SmplRate：" + m_AdoOtptPt.m_SmplRate + "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLen：" + m_AdoOtptPt.m_FrmLen + "\n" );
									p_StngFileWriterPt.write( "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptStrmLnkLstPt：" + m_AdoOtptPt.m_AdoOtptStrmLnkLstPt + "\n" );
									p_StngFileWriterPt.write( "\n" );
									for( AdoOtpt.AdoOtptStrm p_AdoOtptStrm : m_AdoOtptPt.m_AdoOtptStrmLnkLstPt )
									{
										p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptStrmIdx：" + p_AdoOtptStrm.m_AdoOtptStrmIdx + "\n" );
										p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatDecd：" + p_AdoOtptStrm.m_UseWhatDecd + "\n" );
										p_StngFileWriterPt.write( "m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt：" + p_AdoOtptStrm.m_SpeexDecdIsUsePrcplEnhsmt + "\n" );
										p_StngFileWriterPt.write( "\n" );
									}
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsSaveAdoToFile：" + m_AdoOtptPt.m_IsSaveAdoToFile + "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptFileFullPathStrPt：" + m_AdoOtptPt.m_AdoOtptFileFullPathStrPt + "\n" );
									p_StngFileWriterPt.write( "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsDrawAdoWavfmToSurface：" + m_AdoOtptPt.m_IsDrawAdoWavfmToSurface + "\n" );
									p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptWavfmSurfacePt：" + m_AdoOtptPt.m_AdoOtptWavfmSurfacePt + "\n" );
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
									for( VdoOtpt.VdoOtptStrm p_VdoOtptStrm : m_VdoOtptPt.m_VdoOtptStrmLnkLstPt )
									{
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptStrmIdx：" + p_VdoOtptStrm.m_VdoOtptStrmIdx + "\n" );
										p_StngFileWriterPt.write( "\n" );
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_UseWhatDecd：" + p_VdoOtptStrm.m_UseWhatDecd + "\n" );
										p_StngFileWriterPt.write( "\n" );
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_OpenH264DecdDecdThrdNum：" + p_VdoOtptStrm.m_OpenH264DecdDecdThrdNum + "\n" );
										p_StngFileWriterPt.write( "\n" );
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt：" + p_VdoOtptStrm.m_VdoOtptDspySurfaceViewPt + "\n" );
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt：" + p_VdoOtptStrm.m_VdoOtptDspyScale + "\n" );
										p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt：" + p_VdoOtptStrm.m_VdoOtptIsBlack + "\n" );
										p_StngFileWriterPt.write( "\n" );
									}

									p_StngFileWriterPt.flush();
									p_StngFileWriterPt.close();
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + p_StngFileFullPathStrPt + " 成功。" );
								}
								catch( IOException e )
								{
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + p_StngFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
									break Out;
								}
								break;
							}
						}
					}
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//本线程执行函数。
	public void run()
	{
		int p_TmpInt32 = 0;
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
				m_ErrInfoVstrPt = new Vstr();
				m_ErrInfoVstrPt.Init( null );

				//初始化唤醒锁。
				WakeLockInitOrDstoy( m_IsUseWakeLock, m_WakeLockIsUseAdoOtpt, m_WakeLockUseWhatAdoOtptDvc );

				//判断本线程退出标记。
				switch( m_ExitFlag )
				{
					case 0: //保持运行。
					case 1: //请求退出。
					{
						m_ExitFlag = 0; //设置本线程退出标记为保持运行。
						m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。

						//调用用户定义的初始化函数。
						p_TmpInt32 = UserInit();
						if( p_TmpInt32 == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt32 );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt32 );
							break OutMediaInitAndPocs;
						}
						break;
					}
					case 2: //请求重启。
					{
						m_ExitFlag = 0; //设置本线程退出标记为保持运行。
						m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。

						//调用用户定义的初始化函数。
						p_TmpInt32 = UserInit();
						if( p_TmpInt32 == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt32 );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt32 );
							break OutMediaInitAndPocs;
						}
						//break; //继续初始化音视频输入输出。
					}
					case 3: //请求重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。
					{
						if( m_ExitFlag == 3 )
						{
							m_ExitFlag = 0; //设置本线程退出标记为保持运行。
							m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。
						}

						if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 )
						{
							if( m_AdoOtptPt.Init() != 0 ) break OutMediaInitAndPocs;
						}
						if( m_AdoInptPt.m_IsUseAdoInpt != 0 )
						{
							m_AdoInptPt.SetIsCanUseAec();
							if( m_AdoInptPt.Init() != 0 ) break OutMediaInitAndPocs; //在音频输出初始化后再初始化音频输入，因为要音频输入线程去启动音频输出设备和线程。。
							MediaPocsThrdTmpVarInit();
						}
						else
						{
							if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) { m_AdoOtptPt.m_AdoOtptDvcPt.play(); m_AdoOtptPt.m_AdoOtptThrdPt.start(); }
						}
						if( m_VdoInptPt.m_IsUseVdoInpt != 0 )
						{
							if( m_VdoInptPt.Init() != 0 ) break OutMediaInitAndPocs;
						}
						if( m_VdoOtptPt.m_IsUseVdoOtpt != 0 )
						{
							if( m_VdoOtptPt.Init() != 0 ) break OutMediaInitAndPocs;
						}
						break;
					}
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowMsec = System.currentTimeMillis();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：媒体处理线程初始化完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，正式开始处理帧。" );
				}

				m_ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。
				m_RunFlag = RUN_FLAG_POCS; //设置本线程运行标记为初始化完毕正在循环处理帧。

				//音视频输入输出帧处理循环开始。
				while( true )
				{
					//消息处理。
					if( MsgPocs() != 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：消息处理失败。" );
						break OutMediaInitAndPocs;
					}

					//调用用户定义的处理函数。
					if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

					p_TmpInt32 = UserPocs();
					if( p_TmpInt32 == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt32 );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt32 );
						break OutMediaInitAndPocs;
					}

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//取出音频输入帧和音频输出帧。
					if( m_AdoInptPt.m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器。
					{
						if( ( m_AdoInptPt.m_AdoInptFrmLnkLstPt.size() > 0 ) && ( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size() > 0 ) ) //如果音频输入帧链表和音频输出帧链表中都有帧了，才开始取出。
						{
							//从音频输入帧链表中取出第一个音频输入帧。
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + m_AdoInptPt.m_AdoInptFrmLnkLstPt.size() + "。" );
							synchronized( m_AdoInptPt.m_AdoInptFrmLnkLstPt )
							{
								m_PcmAdoInptFrmPt = m_AdoInptPt.m_AdoInptFrmLnkLstPt.getFirst();
								m_AdoInptPt.m_AdoInptFrmLnkLstPt.removeFirst();
							}

							//从音频输出帧链表中取出第一个音频输出帧。
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size() + "。" );
							synchronized( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt )
							{
								m_PcmAdoOtptFrmPt = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.getFirst();
								m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.removeFirst();
							}

							//将音频输入帧复制到音频结果帧，方便处理。
							System.arraycopy( m_PcmAdoInptFrmPt, 0, m_PcmAdoRsltFrmPt, 0, m_PcmAdoInptFrmPt.length );
						}
					}
					else //如果不可以使用声学回音消除器。
					{
						if( ( m_AdoInptPt.m_AdoInptFrmLnkLstPt != null ) && ( m_AdoInptPt.m_AdoInptFrmLnkLstPt.size() > 0 ) ) //如果音频输入帧链表有帧了，就开始取出。
						{
							//从音频输入帧链表中取出第一个音频输入帧。
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + m_AdoInptPt.m_AdoInptFrmLnkLstPt.size() + "。" );
							synchronized( m_AdoInptPt.m_AdoInptFrmLnkLstPt )
							{
								m_PcmAdoInptFrmPt = m_AdoInptPt.m_AdoInptFrmLnkLstPt.getFirst();
								m_AdoInptPt.m_AdoInptFrmLnkLstPt.removeFirst();
							}

							//将音频输入帧复制到音频结果帧，方便处理。
							System.arraycopy( m_PcmAdoInptFrmPt, 0, m_PcmAdoRsltFrmPt, 0, m_PcmAdoInptFrmPt.length );
						}

						if( ( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt != null ) && ( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size() > 0 ) ) //如果音频输出帧链表有帧了，就开始取出。
						{
							//从音频输出帧链表中取出第一个音频输出帧。
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.size() + "。" );
							synchronized( m_AdoOtptPt.m_AdoOtptFrmLnkLstPt )
							{
								m_PcmAdoOtptFrmPt = m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.getFirst();
								m_AdoOtptPt.m_AdoOtptFrmLnkLstPt.removeFirst();
							}
						}
					}

					//处理音频输入帧开始。
					if( m_PcmAdoInptFrmPt != null )
					{
						//使用声学回音消除器。
						if( m_AdoInptPt.m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器。
						{
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
							if( m_AdoInptPt.m_AdoInptWavfmPt.Draw( m_PcmAdoInptFrmPt, m_PcmAdoInptFrmPt.length, m_AdoInptPt.m_AdoInptWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							}
							if( m_AdoInptPt.m_AdoRsltWavfmPt.Draw( m_PcmAdoRsltFrmPt, m_PcmAdoRsltFrmPt.length, m_AdoInptPt.m_AdoRsltWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频结果波形器绘制音频结果波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频结果波形器绘制音频结果波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							}
						}
					} //处理音频输入帧结束。

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：处理音频输入帧完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//处理音频输出帧开始。
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
							if( m_AdoOtptPt.m_AdoOtptWavfmPt.Draw( m_PcmAdoOtptFrmPt, m_PcmAdoOtptFrmPt.length, m_AdoOtptPt.m_AdoOtptWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出波形器绘制音频输入波形到Surface成功。" );
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出波形器绘制音频输出波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							}
						}
					} //处理音频输出帧结束。

					if( m_IsPrintLogcat != 0 )
					{
						p_NowMsec = System.currentTimeMillis();
						Log.i( m_CurClsNameStrPt, "媒体处理线程：处理音频输出帧完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
						p_LastMsec = System.currentTimeMillis();
					}

					//处理视频输入帧开始。
					if( ( m_VdoInptPt.m_VdoInptFrmLnkLstPt != null ) && ( m_VdoInptPt.m_VdoInptFrmLnkLstPt.size() > 0 ) && //如果要使用视频输入，且视频输入帧链表中有帧了。
						( ( m_PcmAdoInptFrmPt != null ) || ( m_AdoInptPt.m_AdoInptFrmLnkLstPt == null ) ) ) //且已经处理了音频输入帧或不使用音频输入帧链表。
					{
						//从视频输入帧链表中取出第一个视频输入帧。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输入帧链表中取出第一个视频输入帧，视频输入帧链表元素个数：" + m_VdoInptPt.m_VdoInptFrmLnkLstPt.size() + "。" );
						synchronized( m_VdoInptPt.m_VdoInptFrmLnkLstPt )
						{
							m_VdoInptFrmPt = m_VdoInptPt.m_VdoInptFrmLnkLstPt.getFirst();
							m_VdoInptPt.m_VdoInptFrmLnkLstPt.removeFirst();
						}
					} //处理视频输入帧结束。

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
							p_TmpInt32 = UserReadAdoVdoInptFrm( m_PcmAdoInptFrmPt, m_PcmAdoRsltFrmPt, m_VoiceActStsPt, m_EncdAdoInptFrmPt, m_EncdAdoInptFrmLenPt, m_EncdAdoInptFrmIsNeedTransPt, m_VdoInptFrmPt.m_YU12VdoInptFrmPt, m_VdoInptFrmPt.m_YU12VdoInptFrmWidthPt, m_VdoInptFrmPt.m_YU12VdoInptFrmHeightPt, m_VdoInptFrmPt.m_EncdVdoInptFrmPt, m_VdoInptFrmPt.m_EncdVdoInptFrmLenPt );
						else
							p_TmpInt32 = UserReadAdoVdoInptFrm( m_PcmAdoInptFrmPt, m_PcmAdoRsltFrmPt, m_VoiceActStsPt, m_EncdAdoInptFrmPt, m_EncdAdoInptFrmLenPt, m_EncdAdoInptFrmIsNeedTransPt, null, null, null, null, null );
						if( p_TmpInt32 == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数成功。返回值：" + p_TmpInt32 );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数失败。返回值：" + p_TmpInt32 );
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
				} //音视频输入输出帧处理循环结束。
			}

			m_RunFlag = RUN_FLAG_DSTOY; //设置本线程运行标记为跳出循环处理帧正在销毁。
			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程开始退出。" );

			MediaPocsThrdTmpVarDstoy();
			m_AdoInptPt.Dstoy();
			m_AdoOtptPt.Dstoy();
			m_VdoInptPt.Dstoy();
			m_VdoOtptPt.Dstoy();

			if( m_ExitFlag != 3 ) //如果需要调用用户定义的销毁函数。
			{
				UserDstoy(); //调用用户定义的销毁函数。
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
			}

			//销毁错误信息动态字符串。
			if( m_ErrInfoVstrPt != null )
			{
				if( m_ErrInfoVstrPt.Dstoy() == 0 )
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁错误信息动态字符串成功。" );
				}
				else
				{
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁错误信息动态字符串失败。" );
				}
				m_ErrInfoVstrPt = null;
			}

			m_RunFlag = RUN_FLAG_END; //设置本线程运行标记为销毁完毕。

			if( ( m_ExitFlag == 0 ) || ( m_ExitFlag == 1 ) ) //如果用户需要直接退出。
			{
				WakeLockInitOrDstoy( 0, m_WakeLockIsUseAdoOtpt, m_WakeLockUseWhatAdoOtptDvc ); //销毁唤醒锁。

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