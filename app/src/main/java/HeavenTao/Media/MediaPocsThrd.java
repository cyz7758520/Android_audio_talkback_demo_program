package HeavenTao.Media;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
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
import java.util.HashMap;

import HeavenTao.Data.*;

public abstract class MediaPocsThrd extends Thread //媒体处理线程。
{
	public static String m_CurClsNameStrPt = "MediaPocsThrd"; //存放当前类名称字符串。

	public class ThrdMsgTyp
	{
		public static final int SetAdoInptOtptUseDvc = 0;

		public static final int SetAdoInpt = 1;
		public static final int AdoInptSetIsUseSystemAecNsAgc = 2;
		public static final int AdoInptSetUseNoAec = 3;
		public static final int AdoInptSetUseSpeexAec = 4;
		public static final int AdoInptSetUseWebRtcAecm = 5;
		public static final int AdoInptSetUseWebRtcAec = 6;
		public static final int AdoInptSetUseWebRtcAec3 = 7;
		public static final int AdoInptSetUseSpeexWebRtcAec = 8;
		public static final int AdoInptSetUseNoNs = 9;
		public static final int AdoInptSetUseSpeexPrpocsNs = 10;
		public static final int AdoInptSetUseWebRtcNsx = 11;
		public static final int AdoInptSetUseWebRtcNs = 12;
		public static final int AdoInptSetUseRNNoise = 13;
		public static final int AdoInptSetIsUseSpeexPrpocs = 14;
		public static final int AdoInptSetUsePcm = 15;
		public static final int AdoInptSetUseSpeexEncd = 16;
		public static final int AdoInptSetUseOpusEncd = 17;
		public static final int AdoInptSetIsSaveAdoToWaveFile = 18;
		public static final int AdoInptSetIsDrawAdoWavfmToSurface = 19;
		public static final int AdoInptSetIsMute = 20;

		public static final int SetAdoOtpt = 21;
		public static final int AdoOtptAddStrm = 22;
		public static final int AdoOtptDelStrm = 23;
		public static final int AdoOtptSetStrmUsePcm = 24;
		public static final int AdoOtptSetStrmUseSpeexDecd = 25;
		public static final int AdoOtptSetStrmUseOpusDecd = 26;
		public static final int AdoOtptSetStrmIsUse = 27;
		public static final int AdoOtptSetIsSaveAdoToWaveFile = 28;
		public static final int AdoOtptSetIsDrawAdoWavfmToSurface = 29;
		public static final int AdoOtptSetIsMute = 30;

		public static final int SetVdoInpt = 31;
		public static final int VdoInptSetUseYu12 = 32;
		public static final int VdoInptSetUseOpenH264Encd = 33;
		public static final int VdoInptSetUseSystemH264Encd = 34;
		public static final int VdoInptSetUseDvc = 35;
		public static final int VdoInptSetIsBlack = 36;

		public static final int VdoOtptAddStrm = 37;
		public static final int VdoOtptDelStrm = 38;
		public static final int VdoOtptSetStrm = 39;
		public static final int VdoOtptSetStrmUseYu12 = 40;
		public static final int VdoOtptSetStrmUseOpenH264Decd = 41;
		public static final int VdoOtptSetStrmUseSystemH264Decd = 42;
		public static final int VdoOtptSetStrmIsBlack = 43;
		public static final int VdoOtptSetStrmIsUse = 44;

		public static final int SetIsUseAdoVdoInptOtpt = 45;

		public static final int SetIsUseWakeLock = 46;
		public static final int SetIsSaveAdoVdoInptOtptToAviFile = 47;
		public static final int SaveStsToTxtFile = 48;

		public static final int RqirExit = 49;

		public static final int UserInit = 50;
		public static final int UserDstoy = 51;

		public static final int AdoVdoInptOtptInit = 52;
		public static final int AdoVdoInptOtptDstoy = 53;

		public static final int ThrdMsgTypAdoInptOtptDvcChg = 54;
		public static final int ThrdMsgTypAdoInptOtptDvcClos = 55;
		public static final int ThrdMsgTypVdoInptDvcChg = 56;
		public static final int ThrdMsgTypVdoInptDvcClos = 57;

		public static final int UserMsgMinVal = 100; //用户消息的最小值。
	}
	public final MsgQueue m_ThrdMsgQueuePt = new MsgQueue( this ) //存放线程消息队列的指针。
	{
		@Override public int UserMsgPocs( int MsgTyp, Object[] MsgParmPt )
		{
			return ThrdMsgPocs( MsgTyp, MsgParmPt );
		}
	};

	class RunFlag //运行标记。
	{
		public static final int Norun = 0; //未开始运行。
		public static final int Run = 1; //正在运行。
		public static final int Exit = 2; //已经退出。
	}
	public int m_RunFlag; //存放本线程运行标记。

	public int m_LastCallUserInitOrDstoy; //存放上一次调用了用户定义的初始化函数或销毁函数，为0表示初始化函数，为1表示销毁函数。

	public static Context m_CtxPt; //存放上下文的指针。

	public static byte m_LicnCodePt[]; //存放授权码。

	public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
	public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
	public Activity m_ShowToastActPt; //存放显示Toast界面的指针。

	int m_IsUseWakeLock; //存放是否使用唤醒锁，为非0表示要使用，为0表示不使用。
	PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁的指针。
	PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

	public static class AdoInptOtptDvcInfo //音频输入输出设备信息。
	{
		public int m_DvcTyp; //存放设备类型。
		public class DvcTyp
		{
			public static final int DftSpeaker = 0; //默认扬声器。
			public static final int DftEarpiece = 1; //默认听筒。
			public static final int Other = 2; //其他。
		}
		public String m_NameStrPt;
		public AudioDeviceInfo m_AdoInptDvcInfoPt;
		public AudioDeviceInfo m_AdoOtptDvcInfoPt;
	}
	public AdoInptOtptDvcInfo m_AdoInptOtptUseDvcInfoPt; //存放音频输入输出使用的设备信息的指针。

	public static class VdoInptDvcInfo //视频输入设备信息。
	{
		public int m_DvcTyp; //存放设备类型。
		public class DvcTyp
		{
			public static final int FrontCamera = 0; //前置摄像头。
			public static final int BackCamera = 1; //后置摄像头。
		}
		public int m_CameraId;
	}
	public VdoInptDvcInfo m_VdoInptUseDvcInfoPt; //存放视频输入使用的设备信息的指针。

	class AdoVdoInptOtptAviFile //存放音视频输入输出Avi文件。
	{
		AviFileWriter m_WriterPt; //存放写入器的指针。
		String m_FullPathStrPt; //存放完整路径字符串的指针。
		long m_WrBufSzByt; //存放写入缓冲区的大小，单位为字节。
		int m_MaxStrmNum; //存放最大流数量，取值区间为[1,100]。
		int m_IsSaveAdoInpt; //存放是否保存音频输入，为非0表示要保存，为0表示不保存。
		int m_IsSaveAdoOtpt; //存放是否保存音频输出，为非0表示要保存，为0表示不保存。
		int m_IsSaveVdoInpt; //存放是否保存视频输入，为非0表示要保存，为0表示不保存。
		int m_IsSaveVdoOtpt; //存放是否保存视频输出，为非0表示要保存，为0表示不保存。
		int m_AdoInptPcmSrcStrmIdx; //存放音频输入Pcm格式原始流的索引。
		int m_AdoInptPcmRsltStrmIdx; //存放音频输入Pcm格式结果流的索引。
		int m_AdoInptStrmTimeStampIsReset; //存放音频输入流时间戳是否重置，为非0表示要重置，为0表示不重置。
		int m_AdoOtptPcmSrcStrmIdx; //存放音频输出Pcm格式原始流的索引。
		int m_AdoOtptStrmTimeStampIsReset; //存放音频输出流时间戳是否重置，为非0表示要重置，为0表示不重置。
		int m_VdoInptEncdRsltStrmIdx; //存放视频输入已编码格式结果流的索引。
		HashMap< Integer, Integer > m_VdoOtptEncdSrcStrmIdxMapPt; //存放视频输出已编码格式原始流的索引映射的指针。
	}
	AdoVdoInptOtptAviFile m_AdoVdoInptOtptAviFilePt = new AdoVdoInptOtptAviFile();

	public AudioDeviceCallback m_AdoInptOtptDvcChgCallbackPt; //存放音频输入输出设备修改回调的指针。

	public AdoInpt m_AdoInptPt = new AdoInpt(); //存放音频输入的指针。
	public AdoOtpt m_AdoOtptPt = new AdoOtpt(); //存放音频输出的指针。
	public VdoInpt m_VdoInptPt = new VdoInpt(); //存放视频输入的指针。
	public VdoOtpt m_VdoOtptPt = new VdoOtpt(); //存放视频输出的指针。

	class Thrd //线程。
	{
		int m_IsInitThrdTmpVar; //存放是否初始化线程的临时变量。
		short m_AdoInptPcmSrcFrmPt[]; //存放音频输入Pcm格式原始帧的指针。
		short m_AdoInptPcmRsltFrmPt[]; //存放音频输入Pcm格式结果帧的指针。
		short m_AdoInptPcmTmpFrmPt[]; //存放音频输入Pcm格式临时帧的指针。
		short m_AdoOtptPcmSrcFrmPt[]; //存放音频输出Pcm格式原始帧的指针。
		HTInt m_AdoInptPcmRsltFrmVoiceActStsPt; //存放音频输入Pcm格式结果帧语音活动状态的指针，为非0表示有语音活动，为0表示无语音活动。
		byte m_AdoInptEncdRsltFrmPt[]; //存放音频输入已编码格式结果帧的指针，大小为 m_AdoInptPt.m_FrmLenByt 字节。
		HTLong m_AdoInptEncdRsltFrmLenBytPt; //存放音频输入已编码格式结果帧的长度的的指针，单位为字节。
		HTInt m_AdoInptEncdRsltFrmIsNeedTransPt; //存放音频输入已编码格式结果帧是否需要传输的指针，为非0表示需要传输，为0表示不要传输。
		VdoInpt.Frm m_VdoInptFrmPt; //存放视频输入帧的指针。
		VdoOtpt.Frm m_VdoOtptFrmPt; //存放视频输出帧的指针。
	}
	Thrd m_ThrdPt = new Thrd(); //存放线程。

	public Vstr m_ErrInfoVstrPt = new Vstr(); //存放错误信息动态字符串的指针。

	//用户定义的相关回调函数。

	//用户定义的初始化函数。
	public abstract void UserInit();

	//用户定义的销毁函数。
	public abstract void UserDstoy();

	//用户定义的处理函数。
	public abstract void UserPocs();

	//用户定义的消息函数。
	public abstract int UserMsg( int MsgTyp, Object MsgParmPt[] );

	//用户定义的设备改变函数。
	public abstract void UserDvcChg( AdoInptOtptDvcInfo AdoInptOtptDvcInfoPt, VdoInptDvcInfo VdoInptDvcInfoPt );

	//用户定义的读取音视频输入帧函数。
	public abstract void UserReadAdoVdoInptFrm( short AdoInptPcmSrcFrmPt[], short AdoInptPcmRsltFrmPt[], long AdoInptPcmFrmLenUnit, int AdoInptPcmRsltFrmVoiceActSts,
												byte AdoInptEncdRsltFrmPt[], long AdoInptEncdRsltFrmLenByt, int AdoInptEncdRsltFrmIsNeedTrans,
												byte VdoInptNv21SrcFrmPt[], int VdoInptNv21SrcFrmWidthPt, int VdoInptNv21SrcFrmHeightPt, long VdoInptNv21SrcFrmLenByt,
												byte VdoInptYu12RsltFrmPt[], int VdoInptYu12RsltFrmWidth, int VdoInptYu12RsltFrmHeight, long VdoInptYu12RsltFrmLenByt,
												byte VdoInptEncdRsltFrmPt[], long VdoInptEncdRsltFrmLenByt );

	//用户定义的写入音频输出帧函数。
	public abstract void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
											  short AdoOtptPcmSrcFrmPt[], int AdoOtptPcmFrmLenUnit,
											  byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmSzByt, HTLong AdoOtptEncdSrcFrmLenBytPt );

	//用户定义的获取音频输出帧函数。
	public abstract void UserGetAdoOtptFrm( int AdoOtptStrmIdx,
											short AdoOtptPcmSrcFrmPt[], long AdoOtptPcmFrmLenUnit,
											byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmLenByt );

	//用户定义的写入视频输出帧函数。
	public abstract void UserWriteVdoOtptFrm( int VdoOtptStrmIdx,
											  byte VdoOtptYu12SrcFrmPt[], HTInt VdoOtptYu12SrcFrmWidthPt, HTInt VdoOtptYu12SrcFrmHeightPt,
											  byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmSzByt, HTLong VdoOtptEncdSrcFrmLenBytPt );

	//用户定义的获取视频输出帧函数。
	public abstract void UserGetVdoOtptFrm( int VdoOtptStrmIdx,
											byte VdoOtptYu12SrcFrmPt[], int VdoOtptYu12SrcFrmWidth, int VdoOtptYu12SrcFrmHeight,
											byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmLenByt );

	//构造函数。
	public MediaPocsThrd( Context CtxPt, byte LicnCodePt[] )
	{
		m_LastCallUserInitOrDstoy = 1; //设置上一次调用了用户定义的销毁函数。

		m_CtxPt = CtxPt; //设置上下文的指针。

		m_LicnCodePt = LicnCodePt; //设置授权码。

		m_AdoInptOtptUseDvcInfoPt = GetAllAdoInptOtptDvcInfo().get( 0 ); //设置音频输入输出使用的设备信息的指针。

		//初始化音频输入。
		m_AdoInptPt.m_MediaPocsThrdPt = this;
		SetAdoInpt( 0, 8000, 20, 0 );

		//初始化音频输出。
		m_AdoOtptPt.m_MediaPocsThrdPt = this;
		m_AdoOtptPt.m_StrmCntnrPt = new ArrayList< AdoOtpt.Strm >();
		SetAdoOtpt( 0, 8000, 20 );

		//初始化视频输入。
		m_VdoInptPt.m_MediaPocsThrdPt = this;
		SetVdoInpt( 0, 15, 480, 640, 0, 0, 0, null );
		VdoInptSetUseDvc( 0, 0, -1, -1 );

		//初始化视频输出。
		m_VdoOtptPt.m_MediaPocsThrdPt = this;

		//初始化错误信息动态字符串。
		m_ErrInfoVstrPt.Init( null );
	}

	//析构函数。
	protected void finalize()
	{
		//销毁错误信息动态字符串。
		if( m_ErrInfoVstrPt != null )
		{
			m_ErrInfoVstrPt.Dstoy();
			m_ErrInfoVstrPt = null;
		}
	}

	//获取音频设备信息类型名称。
	public static String GetAdoDvcInfoTypeName( int AdoDvcInfoType )
	{
		switch( AdoDvcInfoType )
		{
			case AudioDeviceInfo.TYPE_UNKNOWN: return "未知";
			case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE: return "内置听筒";
			case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER: return "内置扬声器";
			case AudioDeviceInfo.TYPE_WIRED_HEADSET: return "有线带麦耳机";
			case AudioDeviceInfo.TYPE_WIRED_HEADPHONES: return "有线无麦耳机";
			case AudioDeviceInfo.TYPE_BLUETOOTH_SCO: return "Sco蓝牙";
			case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: return "A2dp蓝牙";
			case AudioDeviceInfo.TYPE_HDMI: return "HDMI通道";
			case AudioDeviceInfo.TYPE_HDMI_ARC: return "HDMI音频回传通道";
			case AudioDeviceInfo.TYPE_USB_DEVICE: return "USB设备";
			case AudioDeviceInfo.TYPE_DOCK: return "扩展坞";
			case AudioDeviceInfo.TYPE_FM: return "FM收音";
			case AudioDeviceInfo.TYPE_BUILTIN_MIC: return "内置麦克风";
			case AudioDeviceInfo.TYPE_FM_TUNER: return "FM调谐器";
			case AudioDeviceInfo.TYPE_TV_TUNER: return "TV调谐器";
			case AudioDeviceInfo.TYPE_TELEPHONY: return "电话";
			case AudioDeviceInfo.TYPE_USB_HEADSET: return "USB带麦耳机";
			case AudioDeviceInfo.TYPE_REMOTE_SUBMIX: return "远程设备";
			case AudioDeviceInfo.TYPE_BLE_HEADSET: return "低功耗蓝牙带麦耳机";
			case AudioDeviceInfo.TYPE_HDMI_EARC: return "HDMI增强型音频回传通道";
			case AudioDeviceInfo.TYPE_BLE_SPEAKER: return "低功耗蓝牙扬声器";
			case AudioDeviceInfo.TYPE_BLE_BROADCAST: return "低功耗蓝牙广播";
			default: return "无效";
		}
	}

	//获取全部音频输入输出设备信息。
	public static ArrayList< AdoInptOtptDvcInfo > GetAllAdoInptOtptDvcInfo()
	{
		ArrayList< AdoInptOtptDvcInfo > p_AdoInptOtptDvcInfoArrPt = new ArrayList<>();
		AdoInptOtptDvcInfo p_AdoInptOtptDvcInfoPt;

		//添加默认扬声器。
		p_AdoInptOtptDvcInfoPt = new AdoInptOtptDvcInfo();
		p_AdoInptOtptDvcInfoPt.m_DvcTyp = AdoInptOtptDvcInfo.DvcTyp.DftSpeaker;
		p_AdoInptOtptDvcInfoPt.m_NameStrPt = "默认扬声器";
		p_AdoInptOtptDvcInfoArrPt.add( p_AdoInptOtptDvcInfoPt );

		//添加默认听筒。
		p_AdoInptOtptDvcInfoPt = new AdoInptOtptDvcInfo();
		p_AdoInptOtptDvcInfoPt.m_DvcTyp = AdoInptOtptDvcInfo.DvcTyp.DftEarpiece;
		p_AdoInptOtptDvcInfoPt.m_NameStrPt = "默认听筒";
		p_AdoInptOtptDvcInfoArrPt.add( p_AdoInptOtptDvcInfoPt );

		//添加指定的音频输入输出设备。
		if( android.os.Build.VERSION.SDK_INT >= 23 )
		{
			AudioDeviceInfo[] p_AdoInptDvcInfoArrPt = ( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).getDevices( AudioManager.GET_DEVICES_INPUTS ); //获取全部音频输入设备信息。
			AudioDeviceInfo[] p_AdoOtptDvcInfoArrPt = ( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).getDevices( AudioManager.GET_DEVICES_OUTPUTS ); //获取全部音频输出设备信息。
			AudioDeviceInfo p_BottomBuiltinMicDvcInfoPt = null; //存放底部内置麦克风设备。

			//设置底部内置麦克风设备，并删除背部麦克风。
			for( int i = 0; i < p_AdoInptDvcInfoArrPt.length; i++ )
			{
				if( ( p_AdoInptDvcInfoArrPt[ i ] != null ) && ( p_AdoInptDvcInfoArrPt[ i ].getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC ) )
				{
					if( android.os.Build.VERSION.SDK_INT >= 28 )
					{
						if( p_AdoInptDvcInfoArrPt[ i ].getAddress().equals( "bottom" ) )
						{
							p_BottomBuiltinMicDvcInfoPt = p_AdoInptDvcInfoArrPt[ i ];
							p_AdoInptDvcInfoArrPt[ i ] = null;
						}
						else if( p_AdoInptDvcInfoArrPt[ i ].getAddress().equals( "back" ) )
						{
							p_AdoInptDvcInfoArrPt[ i ] = null;
						}
					}
					else
					{
						if( p_BottomBuiltinMicDvcInfoPt == null )
						{
							p_BottomBuiltinMicDvcInfoPt = p_AdoInptDvcInfoArrPt[ i ];
							p_AdoInptDvcInfoArrPt[ i ] = null;
						}
						else
						{
							p_AdoInptDvcInfoArrPt[ i ] = null;
						}
					}
				}
			}

			//查找合适的音频输出设备，并查找配对的音频输入设备。
			for( int i = 0; i < p_AdoOtptDvcInfoArrPt.length; i++ )
			{
				if( p_AdoOtptDvcInfoArrPt[ i ] != null )
				{
					switch( p_AdoOtptDvcInfoArrPt[ i ].getType() )
					{
						//case AudioDeviceInfo.TYPE_UNKNOWN:
						case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE: //内置听筒和内置扬声器与底部内置麦克风配对。
						case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
						{
							p_AdoInptOtptDvcInfoPt = new AdoInptOtptDvcInfo();
							p_AdoInptOtptDvcInfoPt.m_DvcTyp = AdoInptOtptDvcInfo.DvcTyp.Other;
							p_AdoInptOtptDvcInfoPt.m_NameStrPt = GetAdoDvcInfoTypeName( p_AdoOtptDvcInfoArrPt[ i ].getType() ) + p_AdoOtptDvcInfoArrPt[ i ].getProductName();
							p_AdoInptOtptDvcInfoPt.m_AdoOtptDvcInfoPt = p_AdoOtptDvcInfoArrPt[ i ];
							p_AdoInptOtptDvcInfoPt.m_AdoInptDvcInfoPt = p_BottomBuiltinMicDvcInfoPt;
							p_AdoInptOtptDvcInfoArrPt.add( p_AdoInptOtptDvcInfoPt );
							break;
						}
						case AudioDeviceInfo.TYPE_WIRED_HEADSET:
						case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
						case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
						//case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
						case AudioDeviceInfo.TYPE_HDMI:
						//case AudioDeviceInfo.TYPE_HDMI_ARC:
						case AudioDeviceInfo.TYPE_USB_DEVICE:
						//case AudioDeviceInfo.TYPE_DOCK:
						//case AudioDeviceInfo.TYPE_FM:
						//case AudioDeviceInfo.TYPE_BUILTIN_MIC:
						//case AudioDeviceInfo.TYPE_FM_TUNER:
						//case AudioDeviceInfo.TYPE_TV_TUNER:
						//case AudioDeviceInfo.TYPE_TELEPHONY:
						case AudioDeviceInfo.TYPE_USB_HEADSET:
						//case AudioDeviceInfo.TYPE_REMOTE_SUBMIX:
						case AudioDeviceInfo.TYPE_BLE_HEADSET:
						//case AudioDeviceInfo.TYPE_HDMI_EARC:
						case AudioDeviceInfo.TYPE_BLE_SPEAKER:
						case AudioDeviceInfo.TYPE_BLE_BROADCAST:
						{
							p_AdoInptOtptDvcInfoPt = new AdoInptOtptDvcInfo();
							p_AdoInptOtptDvcInfoPt.m_DvcTyp = AdoInptOtptDvcInfo.DvcTyp.Other;
							p_AdoInptOtptDvcInfoPt.m_NameStrPt = GetAdoDvcInfoTypeName( p_AdoOtptDvcInfoArrPt[ i ].getType() ) + p_AdoOtptDvcInfoArrPt[ i ].getProductName();
							p_AdoInptOtptDvcInfoPt.m_AdoOtptDvcInfoPt = p_AdoOtptDvcInfoArrPt[ i ];
							for( int j = 0; j < p_AdoInptDvcInfoArrPt.length; j++ )
							{
								if( ( p_AdoInptDvcInfoArrPt[ j ] != null ) && ( p_AdoInptDvcInfoArrPt[ j ].getType() == p_AdoOtptDvcInfoArrPt[ i ].getType() ) )
								{
									if( p_AdoInptDvcInfoArrPt[ j ].getProductName().equals( p_AdoOtptDvcInfoArrPt[ i ].getProductName() ) )
									{
										p_AdoInptOtptDvcInfoPt.m_AdoInptDvcInfoPt = p_AdoInptDvcInfoArrPt[ j ];
										p_AdoInptDvcInfoArrPt[ j ] = null;
										break;
									}
								}
							}
							p_AdoInptOtptDvcInfoArrPt.add( p_AdoInptOtptDvcInfoPt );
							break;
						}
					}
					p_AdoOtptDvcInfoArrPt[ i ] = null;
				}
			}

			//查找剩余合适的音频输入设备。
			for( int i = 0; i < p_AdoInptDvcInfoArrPt.length; i++ )
			{
				if( p_AdoInptDvcInfoArrPt[ i ] != null )
				{
					switch( p_AdoInptDvcInfoArrPt[ i ].getType() )
					{
						//case AudioDeviceInfo.TYPE_UNKNOWN:
						//case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
						//case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
						case AudioDeviceInfo.TYPE_WIRED_HEADSET:
						//case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
						case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
						//case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
						//case AudioDeviceInfo.TYPE_HDMI:
						case AudioDeviceInfo.TYPE_HDMI_ARC:
						case AudioDeviceInfo.TYPE_USB_DEVICE:
						//case AudioDeviceInfo.TYPE_DOCK:
						//case AudioDeviceInfo.TYPE_FM:
						//case AudioDeviceInfo.TYPE_BUILTIN_MIC:
						//case AudioDeviceInfo.TYPE_FM_TUNER:
						//case AudioDeviceInfo.TYPE_TV_TUNER:
						//case AudioDeviceInfo.TYPE_TELEPHONY:
						case AudioDeviceInfo.TYPE_USB_HEADSET:
						//case AudioDeviceInfo.TYPE_REMOTE_SUBMIX:
						case AudioDeviceInfo.TYPE_BLE_HEADSET:
						case AudioDeviceInfo.TYPE_HDMI_EARC:
						case AudioDeviceInfo.TYPE_BLE_SPEAKER:
						case AudioDeviceInfo.TYPE_BLE_BROADCAST:
						{
							p_AdoInptOtptDvcInfoPt = new AdoInptOtptDvcInfo();
							p_AdoInptOtptDvcInfoPt.m_DvcTyp = AdoInptOtptDvcInfo.DvcTyp.Other;
							p_AdoInptOtptDvcInfoPt.m_NameStrPt = GetAdoDvcInfoTypeName( p_AdoInptDvcInfoArrPt[ i ].getType() ) + p_AdoInptDvcInfoArrPt[ i ].getProductName();
							p_AdoInptOtptDvcInfoPt.m_AdoInptDvcInfoPt = p_AdoInptDvcInfoArrPt[ i ];
							p_AdoInptOtptDvcInfoArrPt.add( p_AdoInptOtptDvcInfoPt );
							break;
						}
					}
					p_AdoInptDvcInfoArrPt[ i ] = null;
				}
			}
		}

		if( p_AdoInptOtptDvcInfoArrPt.isEmpty() ) return null;
		else return p_AdoInptOtptDvcInfoArrPt;
	}

	//媒体处理线程的设置音频输入输出使用的设备。
	public int SetAdoInptOtptUseDvc( int IsBlockWait, AdoInptOtptDvcInfo AdoInptOtptUseDvcInfoPt, int AdoOtptUseVoiceCallOrMusic )
	{
		if( AdoInptOtptUseDvcInfoPt == null )
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetAdoInptOtptUseDvc, AdoInptOtptUseDvcInfoPt, AdoOtptUseVoiceCallOrMusic );
	}

	//媒体处理线程的设置音频输入。
	public int SetAdoInpt( int IsBlockWait, int SmplRate, long FrmLenMsec, int m_IsStartRecordingAfterRead )
	{
		if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
			( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetAdoInpt, SmplRate, FrmLenMsec, m_IsStartRecordingAfterRead );
	}

	//媒体处理线程的音频输入设置设置是否使用系统自带声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
	public int AdoInptSetIsUseSystemAecNsAgc( int IsBlockWait, int IsUseSystemAecNsAgc )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetIsUseSystemAecNsAgc, IsUseSystemAecNsAgc );
	}

	//媒体处理线程的音频输入设置不使用声学回音消除器。
	public int AdoInptSetUseNoAec( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseNoAec );
	}

	//媒体处理线程的音频输入设置要使用Speex声学回音消除器。
	public int AdoInptSetUseSpeexAec( int IsBlockWait, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseSpeexAec, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct );
	}

	//媒体处理线程的音频输入设置要使用WebRtc定点版声学回音消除器。
	public int AdoInptSetUseWebRtcAecm( int IsBlockWait, int IsUseCNGMode, int EchoMode, int Delay )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseWebRtcAecm, IsUseCNGMode, EchoMode, Delay );
	}

	//媒体处理线程的音频输入设置要使用WebRtc浮点版声学回音消除器。
	public int AdoInptSetUseWebRtcAec( int IsBlockWait, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseWebRtcAec, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay );
	}

	//媒体处理线程的音频输入设置要使用WebRtc第三版声学回音消除器。
	public int AdoInptSetUseWebRtcAec3( int IsBlockWait, int Delay )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseWebRtcAec3, Delay );
	}

	//媒体处理线程的音频输入设置要使用SpeexWebRtc三重声学回音消除器。
	public int AdoInptSetUseSpeexWebRtcAec( int IsBlockWait, int WorkMode, int SpeexAecFilterLenMsec, int SpeexAecIsUseRec, float SpeexAecEchoMutp, float SpeexAecEchoCntu, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int WebRtcAec3Delay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseSpeexWebRtcAec, WorkMode, SpeexAecFilterLenMsec, SpeexAecIsUseRec, SpeexAecEchoMutp, SpeexAecEchoCntu, SpeexAecEchoSupes, SpeexAecEchoSupesAct, WebRtcAecmIsUseCNGMode, WebRtcAecmEchoMode, WebRtcAecmDelay, WebRtcAecEchoMode, WebRtcAecDelay, WebRtcAecIsUseDelayAgstcMode, WebRtcAecIsUseExtdFilterMode, WebRtcAecIsUseRefinedFilterAdaptAecMode, WebRtcAecIsUseAdaptAdjDelay, WebRtcAec3Delay, IsUseSameRoomAec, SameRoomEchoMinDelay );
	}

	//媒体处理线程的音频输入设置不使用噪音抑制器。
	public int AdoInptSetUseNoNs( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseNoNs );
	}

	//媒体处理线程的音频输入设置要使用Speex预处理器的噪音抑制。
	public int AdoInptSetUseSpeexPrpocsNs( int IsBlockWait, int IsUseNs, int NoiseSupes, int IsUseDereverb )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseSpeexPrpocsNs, IsUseNs, NoiseSupes, IsUseDereverb );
	}

	//媒体处理线程的音频输入设置要使用WebRtc定点版噪音抑制器。
	public int AdoInptSetUseWebRtcNsx( int IsBlockWait, int PolicyMode )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseWebRtcNsx, PolicyMode );
	}

	//媒体处理线程的音频输入设置要使用WebRtc浮点版噪音抑制器。
	public int AdoInptSetUseWebRtcNs( int IsBlockWait, int PolicyMode )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseWebRtcNs, PolicyMode );
	}

	//媒体处理线程的音频输入设置要使用RNNoise噪音抑制器。
	public int AdoInptSetUseRNNoise( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseRNNoise );
	}

	//媒体处理线程的音频输入设置是否使用Speex预处理器。
	public int AdoInptSetIsUseSpeexPrpocs( int IsBlockWait, int IsUseSpeexPrpocs, int IsUseVad, int VadProbStart, int VadProbCntu, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetIsUseSpeexPrpocs, IsUseSpeexPrpocs, IsUseVad, VadProbStart, VadProbCntu, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain );
	}

	//媒体处理线程的音频输入设置要使用Pcm原始数据。
	public int AdoInptSetUsePcm( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUsePcm );
	}

	//媒体处理线程的音频输入设置要使用Speex编码器。
	public int AdoInptSetUseSpeexEncd( int IsBlockWait, int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseSpeexEncd, UseCbrOrVbr, Qualt, Cmplxt, PlcExptLossRate );
	}

	//媒体处理线程的音频输入设置要使用Opus编码器。
	public int AdoInptSetUseOpusEncd( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetUseOpusEncd );
	}

	//媒体处理线程的音频输入设置是否绘制音频波形到Surface。
	public int AdoInptSetIsDrawAdoWavfmToSurface( int IsBlockWait, int IsDraw, SurfaceView SrcSurfacePt, SurfaceView RsltSurfacePt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetIsDrawAdoWavfmToSurface, IsDraw, SrcSurfacePt, RsltSurfacePt );
	}

	//媒体处理线程的音频输入设置是否保存音频到Wave文件。
	public int AdoInptSetIsSaveAdoToWaveFile( int IsBlockWait, int IsSave, String SrcFullPathStrPt, String RsltFullPathStrPt, long WrBufSzByt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetIsSaveAdoToWaveFile, IsSave, SrcFullPathStrPt, RsltFullPathStrPt, WrBufSzByt );
	}

	//媒体处理线程的音频输入设置是否静音。
	public int AdoInptSetIsMute( int IsBlockWait, int IsMute )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoInptSetIsMute, IsMute );
	}

	//媒体处理线程的设置音频输出。
	public int SetAdoOtpt( int IsBlockWait, int SmplRate, long FrmLenMsec )
	{
		if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
			( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetAdoOtpt, SmplRate, FrmLenMsec );
	}

	//媒体处理线程的音频输出添加流。
	public int AdoOtptAddStrm( int IsBlockWait, int StrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptAddStrm, StrmIdx );
	}

	//媒体处理线程的音频输出删除流。
	public int AdoOtptDelStrm( int IsBlockWait, int AddFirstOrLast, int StrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, AddFirstOrLast, ThrdMsgTyp.AdoOtptDelStrm, StrmIdx );
	}

	//媒体处理线程的音频输出设置流要使用Pcm原始数据。
	public int AdoOtptSetStrmUsePcm( int IsBlockWait, int StrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetStrmUsePcm, StrmIdx );
	}

	//媒体处理线程的音频输出设置流要使用Speex解码器。
	public int AdoOtptSetStrmUseSpeexDecd( int IsBlockWait, int StrmIdx, int IsUsePrcplEnhsmt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetStrmUseSpeexDecd, StrmIdx, IsUsePrcplEnhsmt );
	}

	//媒体处理线程的音频输出设置流要使用Opus解码器。
	public int AdoOtptSetStrmUseOpusDecd( int IsBlockWait, int StrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetStrmUseOpusDecd, StrmIdx );
	}

	//媒体处理线程的音频输出设置流是否要使用。
	public int AdoOtptSetStrmIsUse( int IsBlockWait, int StrmIdx, int IsUseAdoOtptStrm )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetStrmIsUse, StrmIdx, IsUseAdoOtptStrm );
	}

	//媒体处理线程的音频输出设置是否绘制音频波形到Surface。
	public int AdoOtptSetIsDrawAdoWavfmToSurface( int IsBlockWait, int IsDraw, SurfaceView SrcSurfacePt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetIsDrawAdoWavfmToSurface, IsDraw, SrcSurfacePt );
	}

	//媒体处理线程的音频输出设置是否保存音频到Wave文件。
	public int AdoOtptSetIsSaveAdoToWaveFile( int IsBlockWait, int IsSave, String SrcFullPathStrPt, long WrBufSzByt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetIsSaveAdoToWaveFile, IsSave, SrcFullPathStrPt, WrBufSzByt );
	}

	//媒体处理线程的音频输出设置是否静音。
	public int AdoOtptSetIsMute( int IsBlockWait, int IsMute )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.AdoOtptSetIsMute, IsMute );
	}

	//媒体处理线程的设置视频输入。
	public int SetVdoInpt( int IsBlockWait, int MaxSmplRate, int FrmWidth, int FrmHeight, int SrcFrmWidth, int SrcFrmHeight, int ScreenRotate, HTSurfaceView VdoInptPrvwSurfaceViewPt )
	{
		if( ( ( MaxSmplRate < 1 ) || ( MaxSmplRate > 60 ) ) || //如果采样频率不正确。
			( ( FrmWidth <= 0 ) || ( ( FrmWidth & 1 ) != 0 ) ) || //如果帧的宽度不正确。
			( ( FrmHeight <= 0 ) || ( ( FrmHeight & 1 ) != 0 ) ) || //如果帧的高度不正确。
			( ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) ) //如果屏幕旋转的角度不正确。
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetVdoInpt, MaxSmplRate, FrmWidth, FrmHeight, SrcFrmWidth, SrcFrmHeight, ScreenRotate, VdoInptPrvwSurfaceViewPt );
	}

	//媒体处理线程的视频输入设置要使用Yu12原始数据。
	public int VdoInptSetUseYu12( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoInptSetUseYu12 );
	}

	//媒体处理线程的视频输入设置要使用OpenH264编码器。
	public int VdoInptSetUseOpenH264Encd( int IsBlockWait, int VdoType, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvl, int Cmplxt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoInptSetUseOpenH264Encd, VdoType, EncdBitrate, BitrateCtrlMode, IDRFrmIntvl, Cmplxt );
	}

	//媒体处理线程的视频输入设置要使用系统自带H264编码器。
	public int VdoInptSetUseSystemH264Encd( int IsBlockWait, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvlTimeSec, int Cmplxt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoInptSetUseSystemH264Encd, EncdBitrate, BitrateCtrlMode, IDRFrmIntvlTimeSec, Cmplxt );
	}

	//媒体处理线程的视频输入设置使用的设备。
	public int VdoInptSetUseDvc( int IsBlockWait, int UseFrontOrBack, int FrontCameraDvcId, int BackCameraDvcId )
	{
		if( ( ( UseFrontOrBack != -1 ) && ( UseFrontOrBack != 0 ) && ( UseFrontOrBack != 1 ) ) ||
			( FrontCameraDvcId < -1 ) ||
			( BackCameraDvcId < -1 ) )
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoInptSetUseDvc, UseFrontOrBack, FrontCameraDvcId, BackCameraDvcId );
	}

	//媒体处理线程的视频输入设置是否黑屏。
	public int VdoInptSetIsBlack( int IsBlockWait, int IsBlack )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoInptSetIsBlack, IsBlack );
	}

	//媒体处理线程的视频输出添加流。
	public int VdoOtptAddStrm( int IsBlockWait, int VdoOtptStrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptAddStrm, VdoOtptStrmIdx );
	}

	//媒体处理线程的视频输出删除流。
	public int VdoOtptDelStrm( int IsBlockWait, int AddFirstOrLast, int VdoOtptStrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, AddFirstOrLast, ThrdMsgTyp.VdoOtptDelStrm, VdoOtptStrmIdx );
	}

	//媒体处理线程的视频输出设置流。
	public int VdoOtptSetStrm( int IsBlockWait, int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt )
	{
		if( VdoOtptDspySurfaceViewPt == null ) //如果视频显示SurfaceView的指针不正确。
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrm, VdoOtptStrmIdx, VdoOtptDspySurfaceViewPt );
	}

	//媒体处理线程的视频输出设置流要使用Yu12原始数据。
	public int VdoOtptSetStrmUseYu12( int IsBlockWait, int VdoOtptStrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrmUseYu12, VdoOtptStrmIdx );
	}

	//媒体处理线程的视频输出设置流要使用OpenH264解码器。
	public int VdoOtptSetStrmUseOpenH264Decd( int IsBlockWait, int VdoOtptStrmIdx, int DecdThrdNum )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrmUseOpenH264Decd, VdoOtptStrmIdx, DecdThrdNum );
	}

	//媒体处理线程的视频输出设置流要使用系统自带H264解码器。
	public int VdoOtptSetStrmUseSystemH264Decd( int IsBlockWait, int VdoOtptStrmIdx )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrmUseSystemH264Decd, VdoOtptStrmIdx );
	}

	//媒体处理线程的视频输出设置流是否黑屏。
	public int VdoOtptSetStrmIsBlack( int IsBlockWait, int VdoOtptStrmIdx, int IsBlack )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrmIsBlack, VdoOtptStrmIdx, IsBlack );
	}

	//媒体处理线程的视频输出设置流是否使用。
	public int VdoOtptSetStrmIsUse( int IsBlockWait, int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.VdoOtptSetStrmIsUse, VdoOtptStrmIdx, IsUseVdoOtptStrm );
	}

	//媒体处理线程的设置音视频输入输出是否使用。
	public int SetIsUseAdoVdoInptOtpt( int IsBlockWait, int IsUseAdoInpt, int IsUseAdoOtpt, int IsUseVdoInpt, int IsUseVdoOtpt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetIsUseAdoVdoInptOtpt, IsUseAdoInpt, IsUseAdoOtpt, IsUseVdoInpt, IsUseVdoOtpt );
	}

	//媒体处理线程的设置是否打印Logcat日志、显示Toast。
	public int SetIsPrintLogcatShowToast( int IsPrintLogcat, int IsShowToast, Activity ShowToastActPt )
	{
		if( ( IsShowToast != 0 ) && ( ShowToastActPt == null ) ) //如果显示Toast界面的指针不正确。
		{
			return -1;
		}

		m_IsPrintLogcat = IsPrintLogcat;
		m_IsShowToast = IsShowToast;
		m_ShowToastActPt = ShowToastActPt;

		return 0;
	}

	//设媒体处理线程的设置是否使用唤醒锁。
	public int SetIsUseWakeLock( int IsBlockWait, int IsUseWakeLock )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetIsUseWakeLock, IsUseWakeLock );
	}

	//媒体处理线程的设置是否保存音视频输入输出到Avi文件。
	public int SetIsSaveAdoVdoInptOtptToAviFile( int IsBlockWait, String AdoVdoInptOtptAviFileFullPathStrPt, long AdoVdoInptOtptAviFileWrBufSzByt, int MaxStrmNum, int IsSaveAdoInpt, int IsSaveAdoOtpt, int IsSaveVdoInpt, int IsSaveVdoOtpt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetIsSaveAdoVdoInptOtptToAviFile, AdoVdoInptOtptAviFileFullPathStrPt, AdoVdoInptOtptAviFileWrBufSzByt, MaxStrmNum, IsSaveAdoInpt, IsSaveAdoOtpt, IsSaveVdoInpt, IsSaveVdoOtpt );
	}

	//媒体处理线程的保存状态到文件。
	public int SaveStsToTxtFile( int IsBlockWait, String StngFileFullPathStrPt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SaveStsToTxtFile, StngFileFullPathStrPt );
	}

	//媒体处理线程的唤醒锁初始化或销毁。
	private void WakeLockInitOrDstoy( int IsInitWakeLock )
	{
		if( IsInitWakeLock != 0 ) //如果要初始化唤醒锁。
		{
			if( ( m_AdoOtptPt.m_IsUse != 0 ) && ( m_AdoInptOtptUseDvcInfoPt.m_NameStrPt.equals( "默认听筒" ) || m_AdoInptOtptUseDvcInfoPt.m_NameStrPt.startsWith( "内置听筒" ) ) ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
			{
				if( m_ProximityScreenOffWakeLockPt == null ) //如果接近息屏唤醒锁还没有初始化。
				{
					m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) m_CtxPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
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
				m_FullWakeLockPt = ( ( PowerManager ) m_CtxPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
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
	public static void RqstPrmsn( Activity RqstActivity, int IsRqstInternet, int IsRqstModifyAudioStng, int IsRqstForegroundService, int IsRqstForegroundServicePhoneCall, int IsRqstWakeLock, int IsRqstReadPhoneState, int IsRqstRecordAdo, int IsRqstCamera, int DeniedIsPrintLogcat, int DeniedIsShowToast )
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

		//检测前台服务通话权限。
		if( ( IsRqstForegroundServicePhoneCall != 0 ) && ( android.os.Build.VERSION.SDK_INT >= 34 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL ) != PackageManager.PERMISSION_GRANTED ) )
		{
			p_RqstPermissionStrArrPt.add( Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL );
			p_DeniedPermissionStrPt += "前台服务通话  ";
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
				ActivityCompat.requestPermissions( RqstActivity, p_RqstPermissionStrArrPt.toArray( new String[ p_RqstPermissionStrArrPt.size() ] ), 1 );
			}

			//打印日志。
			if (DeniedIsPrintLogcat != 0)
			{
				Log.i(m_CurClsNameStrPt, p_DeniedPermissionStrPt);
			}

			//打印Toast。
			if (DeniedIsShowToast != 0)
			{
				Toast.makeText( RqstActivity, p_DeniedPermissionStrPt, Toast.LENGTH_LONG ).show();
			}
		}
	}

	//媒体处理线程请求退出。
	public int RqirExit( int IsBlockWait, int ExitFlag )
	{
		if( ( ExitFlag < 1 ) || ( ExitFlag > 3 ) )
		{
			return -1;
		}

		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.RqirExit, ExitFlag );
	}

	//发送用户消息到媒体处理线程。
	public int SendUserMsg( int IsBlockWait, int MsgTyp, Object... MsgParmPt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.UserMsgMinVal + MsgTyp, MsgParmPt );
	}

	//音频输入输出设备修改回调初始化。
	private int AdoInptOtptDvcChgCallbackInit()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( android.os.Build.VERSION.SDK_INT >= 23 )
			{
				if( m_AdoInptOtptDvcChgCallbackPt == null )
				{
					m_AdoInptOtptDvcChgCallbackPt = new android.media.AudioDeviceCallback()
					{
						int m_IsFirstCallback = 1;

						public void onAudioDevicesAdded( AudioDeviceInfo[] addedDevices )
						{
							if( m_IsFirstCallback == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：检测到音频设备添加，发送音频输入输出设备关闭线程消息。" ); //因为音频设备改变都会导致音频服务卡顿，从而导致音频输入输出帧不同步，所以音频设备需要重新初始化。
								m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcClos );
							}
							else
							{
								m_IsFirstCallback = 0;
							}
						}

						public void onAudioDevicesRemoved( AudioDeviceInfo[] removedDevices )
						{
							if( m_IsFirstCallback == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：检测到音频设备删除，发送音频输入输出设备关闭线程消息。" ); //因为音频设备改变都会导致音频服务卡顿，从而导致音频输入输出帧不同步，所以音频设备需要重新初始化。
								m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcClos );
							}
							else
							{
								m_IsFirstCallback = 0;
							}
						}
					};
					( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).registerAudioDeviceCallback( m_AdoInptOtptDvcChgCallbackPt, null );
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音频输入输出设备修改回调成功。" );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//音频输入输出设备修改回调销毁。
	private void AdoInptOtptDvcChgCallbackDstoy()
	{
		if( android.os.Build.VERSION.SDK_INT >= 23 )
		{
			if( m_AdoInptOtptDvcChgCallbackPt != null )
			{
				( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).unregisterAudioDeviceCallback( m_AdoInptOtptDvcChgCallbackPt );
				m_AdoInptOtptDvcChgCallbackPt = null;
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入输出设备修改回调成功。" );
			}
		}
	}

	//媒体处理线程的音视频输入输出Avi文件写入器初始化。
	private int AdoVdoInptOtptAviFileWriterInit()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		HTInt p_TmpHTIntPt = new HTInt();

		Out:
		{
			if( ( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt != 0 ) || ( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt != 0 ) || ( m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt != 0 ) || ( m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt != 0 ) )
			{
				if( m_AdoVdoInptOtptAviFilePt.m_WriterPt == null )
				{
					m_AdoVdoInptOtptAviFilePt.m_WriterPt = new AviFileWriter();
					if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.Init( m_LicnCodePt, m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt, m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt, m_AdoVdoInptOtptAviFilePt.m_MaxStrmNum, m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音视频输入输出Avi文件 " + m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt + " 的Avi文件写入器成功。" );
					}
					else
					{
						m_AdoVdoInptOtptAviFilePt.m_WriterPt = null;
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：初始化音视频输入输出Avi文件 " + m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt + " 的Avi文件写入器失败。原因：" + m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					long p_AdoVdoStartTimeStamp = SystemClock.uptimeMillis();
					m_AdoVdoInptOtptAviFilePt.m_WriterPt.SetStartTimeStamp( p_AdoVdoStartTimeStamp, null );
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件时间线的起始时间戳为 " + p_AdoVdoStartTimeStamp + " 。" );
					m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx = -1;
					m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx = -1;
					m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx = -1;
					m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx = -1;
					m_AdoVdoInptOtptAviFilePt.m_VdoOtptEncdSrcStrmIdxMapPt = new HashMap< Integer, Integer >();
				}

				if( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt != 0 ) //如果要保存音频输入。
				{
					if( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx == -1 )
					{
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AddAdoStrm( 1, m_AdoInptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
						{
							m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx = p_TmpHTIntPt.m_Val;
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输入Pcm格式原始流成功。索引：" + m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx + "。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输入Pcm格式原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AddAdoStrm( 1, m_AdoInptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
						{
							m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx = p_TmpHTIntPt.m_Val;
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输入Pcm格式结果流成功。索引：" + m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx + "。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输入Pcm格式结果流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
					}
				}

				if( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt != 0 ) //如果要保存音频输出。
				{
					if( m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx == -1 )
					{
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AddAdoStrm( 1, m_AdoOtptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
						{
							m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx = p_TmpHTIntPt.m_Val;
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输出Pcm格式原始流成功。索引：" + m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx + "。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加音频输出Pcm格式原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
					}
				}

				if( ( m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt != 0 ) && ( m_VdoInptPt.m_IsInit != 0 ) && ( m_VdoInptPt.m_UseWhatEncd != 0 ) ) //如果要保存视频输入，且已初始化视频输入，且视频输入不使用Yu12原始数据。
				{
					if( m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx == -1 )
					{
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AddVdoStrm( 875967048/*H264*/, 50, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 ) //最大采样频率应该尽量被1000整除。
						{
							m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx = p_TmpHTIntPt.m_Val;
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加视频输入已编码格式结果流成功。索引：" + m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx + "。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加视频输入已编码格式结果流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
					}
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//媒体处理线程的音视频输入输出Avi文件写入器销毁。
	private void AdoVdoInptOtptAviFileWriterDstoy()
	{
		if( m_AdoVdoInptOtptAviFilePt.m_WriterPt != null )
		{
			if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.Dstoy( m_ErrInfoVstrPt ) == 0 )
			{
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音视频输入输出Avi文件写入器成功。" );
			}
			else
			{
				if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音视频输入输出Avi文件写入器失败。原因：" + m_ErrInfoVstrPt.GetStr() );
			}
			m_AdoVdoInptOtptAviFilePt.m_WriterPt = null;
			m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx = -1;
			m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx = -1;
			m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 0;
			m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx = -1;
			m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 0;
			m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx = -1;
			m_AdoVdoInptOtptAviFilePt.m_VdoOtptEncdSrcStrmIdxMapPt = null;
		}
	}

	//媒体处理线程的临时变量初始化。
	private void MediaPocsThrdTmpVarInit()
	{
		m_ThrdPt.m_IsInitThrdTmpVar = 1; //设置已初始化线程的临时变量。
		if( m_AdoInptPt.m_IsInit != 0 )
		{
			m_ThrdPt.m_AdoInptPcmSrcFrmPt = null;
			m_ThrdPt.m_AdoOtptPcmSrcFrmPt = null;
			if( ( m_ThrdPt.m_AdoInptPcmRsltFrmPt == null ) || ( m_ThrdPt.m_AdoInptPcmRsltFrmPt.length != m_AdoInptPt.m_FrmLenData ) )
			{
				m_ThrdPt.m_AdoInptPcmRsltFrmPt = new short[ ( int ) m_AdoInptPt.m_FrmLenData ];
				m_ThrdPt.m_AdoInptPcmTmpFrmPt = new short[ ( int ) m_AdoInptPt.m_FrmLenData ];
			}
			if( m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt == null ) m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt = new HTInt( 1 ); //设置音频输入Pcm格式结果帧的语音活动状态为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
			else m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val = 1;
			if( m_AdoInptPt.m_UseWhatEncd != 0 )
			{
				if( ( m_ThrdPt.m_AdoInptEncdRsltFrmPt == null ) || ( m_ThrdPt.m_AdoInptEncdRsltFrmPt.length != m_AdoInptPt.m_FrmLenByt ) ) m_ThrdPt.m_AdoInptEncdRsltFrmPt = new byte[ ( int ) m_AdoInptPt.m_FrmLenByt ];
				if( m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt == null ) m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt = new HTLong();
				if( m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt == null ) m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt = new HTInt( 1 ); //设置音频输入已编码格式结果帧是否需要传输为1，为了让在不使用非连续传输的情况下永远都是需要传输。
				else m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val = 1;
			}
			else
			{
				m_ThrdPt.m_AdoInptEncdRsltFrmPt = null;
				m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt = null;
				m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt = null;
			}
		}
		else
		{
			m_ThrdPt.m_AdoInptPcmSrcFrmPt = null;
			m_ThrdPt.m_AdoOtptPcmSrcFrmPt = null;
			m_ThrdPt.m_AdoInptPcmRsltFrmPt = null;
			m_ThrdPt.m_AdoInptPcmTmpFrmPt = null;
			m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt = null;
		}
		m_ThrdPt.m_VdoInptFrmPt = null;
		m_ThrdPt.m_VdoOtptFrmPt = null;
		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化媒体处理线程的临时变量成功。" );
	}

	//媒体处理线程的临时变量销毁。
	private void MediaPocsThrdTmpVarDstoy()
	{
		if( m_ThrdPt.m_IsInitThrdTmpVar != 0 )
		{
			m_ThrdPt.m_IsInitThrdTmpVar = 0; //设置未初始化线程的临时变量。
			m_ThrdPt.m_AdoInptPcmSrcFrmPt = null;
			m_ThrdPt.m_AdoOtptPcmSrcFrmPt = null;
			m_ThrdPt.m_AdoInptPcmRsltFrmPt = null;
			m_ThrdPt.m_AdoInptPcmTmpFrmPt = null;
			m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt = null;
			m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt = null;
			m_ThrdPt.m_VdoInptFrmPt = null;
			m_ThrdPt.m_VdoOtptFrmPt = null;
			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁媒体处理线程的临时变量成功。" );
		}
	}

	//媒体处理线程的音视频输入输出初始化。
	private int AdoVdoInptOtptInit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_AdoOtptPt.m_IsUse != 0 ) //如果要使用音频输出。在初始化音频输入前初始化音频输出，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
			{
				if( m_AdoOtptPt.m_IsInit == 0 ) //如果未初始化音频输出。
				{
					if( m_AdoOtptPt.Init() != 0 ) break Out;
					m_AdoOtptPt.m_IsInit = 1; //设置已初始化音频输出。
					m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
					if( m_AdoInptPt.m_IsUse == 0 ) //如果不使用音频输入。
					{
						m_AdoOtptPt.m_DvcPt.m_Pt.play(); //让音频输出设备开始播放。
						m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart = 1; //设置音频输出线程已开始。
					} //如果要使用音频输入，就不设置已初始化音频输出，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
				}
				else //如果已初始化音频输出。
				{
					if( m_AdoInptPt.m_IsUse != 0 ) //如果要使用音频输入。
					{
						if( m_AdoInptPt.m_IsInit == 0 ) //如果未初始化音频输入。
						{
							m_AdoOtptPt.DvcAndThrdDstoy(); //销毁并初始化音频输出设备和线程，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
							if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
							m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
						} //如果音频输入已初始化，表示音频输入输出都已初始化，无需再销毁并初始化。
					}
				}
			}
			else //如果不使用音频输出。
			{
				if( m_AdoOtptPt.m_IsInit != 0 ) //如果已初始化音频输出。
				{
					m_AdoOtptPt.Dstoy();
					m_AdoOtptPt.m_IsInit = 0; //设置未初始化音频输出。
				}
			}

			if( m_AdoInptPt.m_IsUse != 0 ) //如果要使用音频输入。
			{
				if( m_AdoInptPt.m_IsInit == 0 ) //如果未初始化音频输入。
				{
					m_AdoInptPt.SetIsCanUseAec();
					if( m_AdoInptPt.Init() != 0 ) break Out; //在音频输出初始化后再初始化音频输入，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
					m_AdoInptPt.m_IsInit = 1; //设置已初始化音频输入。
					m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
					MediaPocsThrdTmpVarInit();
				}
				else //如果已初始化音频输入。
				{
					if( m_AdoOtptPt.m_IsUse != 0 ) //如果要使用音频输出。
					{
						if( m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart == 0 ) //如果音频输出线程未开始。
						{
							if( m_AdoInptPt.m_ThrdPt.m_IsStartAdoOtptThrd != 0 ) //如果音频输入线程已开始音频输出线程。
							{
								m_AdoInptPt.DvcAndThrdDstoy(); //销毁并初始化音频输入设备和线程，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
								m_AdoInptPt.SetIsCanUseAec();
								if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
								m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
							} //如果音频输入线程未开始音频输出线程，就不用管，等一会音频输入线程就会开始音频输出线程。
						} //如果音频输出线程已开始，表示音频输入输出都已初始化，无需再销毁并初始化。
					}
					else //如果不使用音频输出。
					{
						m_AdoInptPt.SetIsCanUseAec();
					}
				}
			}
			else //如果不使用音频输入。
			{
				if( m_AdoInptPt.m_IsInit != 0 ) //如果已初始化音频输入。
				{
					m_AdoInptPt.Dstoy();
					m_AdoInptPt.m_IsInit = 0; //设置未初始化音频输入。
					m_AdoInptPt.SetIsCanUseAec();
					MediaPocsThrdTmpVarInit();
				}
			}

			if( m_VdoInptPt.m_IsUse != 0 ) //如果要使用视频输入。
			{
				if( m_VdoInptPt.m_IsInit == 0 ) //如果未初始化视频输入。
				{
					if( m_VdoInptPt.Init() != 0 ) break Out;
					m_VdoInptPt.m_IsInit = 1; //设置已初始化视频输入。
				}
			}
			else //如果不使用视频输入。
			{
				if( m_VdoInptPt.m_IsInit != 0 ) //如果已初始化视频输入。
				{
					m_VdoInptPt.Dstoy();
					m_VdoInptPt.m_IsInit = 0; //设置未初始化视频输入。
				}
			}

			if( m_VdoOtptPt.m_IsUse != 0 ) //如果要使用视频输出。
			{
				if( m_VdoOtptPt.m_IsInit == 0 ) //如果未初始化视频输出。
				{
					if( m_VdoOtptPt.Init() != 0 ) break Out;
					m_VdoOtptPt.m_IsInit = 1; //设置已初始化视频输出。
				}
			}
			else //如果不使用视频输出。
			{
				if( m_VdoOtptPt.m_IsInit != 0 ) //如果已初始化视频输出。
				{
					m_VdoOtptPt.Dstoy();
					m_VdoOtptPt.m_IsInit = 0; //设置未初始化视频输出。
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//媒体处理线程的音视频输入输出销毁。
	private void AdoVdoInptOtptDstoy()
	{
		if( m_AdoInptPt.m_IsInit != 0 ) //如果未初始化音频输入。
		{
			m_AdoInptPt.Dstoy();
			m_AdoInptPt.m_IsInit = 0; //设置未初始化音频输入。
			MediaPocsThrdTmpVarInit();
		}

		if( m_AdoOtptPt.m_IsInit != 0 ) //如果已初始化音频输出。
		{
			m_AdoOtptPt.Dstoy();
			m_AdoOtptPt.m_IsInit = 0; //设置未初始化音频输出。
		}

		if( m_VdoInptPt.m_IsInit != 0 ) //如果已初始化视频输入。
		{
			m_VdoInptPt.Dstoy();
			m_VdoInptPt.m_IsInit = 0; //设置未初始化视频输入。
		}

		if( m_VdoOtptPt.m_IsInit != 0 ) //如果已初始化视频输出。
		{
			m_VdoOtptPt.Dstoy();
			m_VdoOtptPt.m_IsInit = 0; //设置未初始化视频输出。
		}
	}

	//媒体处理线程的线程消息处理。
	private int ThrdMsgPocs( int MsgTyp, Object[] MsgParmPt )
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		int p_TmpInt32;

		Out:
		{
			switch( MsgTyp )
			{
				case ThrdMsgTyp.SetAdoInptOtptUseDvc:
				{
					if( m_AdoOtptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
						m_AdoOtptPt.DvcAndThrdDstoy();
					}

					m_AdoInptOtptUseDvcInfoPt = ( AdoInptOtptDvcInfo ) MsgParmPt[ 0 ];
					m_AdoOtptPt.m_DvcPt.m_UseWhatStreamType = ( Integer ) MsgParmPt[ 1 ];
					WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。

					if( m_AdoOtptPt.m_IsInit != 0 )
					{
						if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
						m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
						if( m_AdoInptPt.m_IsInit != 0 )
						{
							if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
							m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
						}
						else
						{
							m_AdoOtptPt.m_DvcPt.m_Pt.play(); //让音频输出设备开始播放。
							m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart = 1; //设置音频输出线程已开始。
						}
					}
					break;
				}
				case ThrdMsgTyp.SetAdoInpt:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.Dstoy();
						if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
					}

					m_AdoInptPt.m_SmplRate = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_FrmLenMsec = ( Long ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_FrmLenUnit = m_AdoInptPt.m_FrmLenMsec * m_AdoInptPt.m_SmplRate / 1000;
					m_AdoInptPt.m_FrmLenData = m_AdoInptPt.m_FrmLenUnit * 1;
					m_AdoInptPt.m_FrmLenByt = m_AdoInptPt.m_FrmLenData * 2;
					m_AdoInptPt.m_IsStartRecordingAfterRead = ( Integer ) MsgParmPt[ 2 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoOtptPt.m_IsInit != 0 )
						{
							if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
							m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
						}
						m_AdoInptPt.SetIsCanUseAec();
						if( m_AdoInptPt.Init() != 0 ) break Out;
						m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
						MediaPocsThrdTmpVarInit();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetIsUseSystemAecNsAgc:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.DvcAndThrdDstoy();
						if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
					}

					m_AdoInptPt.m_IsUseSystemAecNsAgc = ( Integer ) MsgParmPt[ 0 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoOtptPt.m_IsInit != 0 )
						{
							if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
							m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
						}
						if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
						m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseNoAec:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 0;

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseSpeexAec:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 1;
					m_AdoInptPt.m_SpeexAecPt.m_FilterLenMsec = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_SpeexAecPt.m_IsUseRec = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_SpeexAecPt.m_EchoMutp = ( Float ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_SpeexAecPt.m_EchoCntu = ( Float ) MsgParmPt[ 3 ];
					m_AdoInptPt.m_SpeexAecPt.m_EchoSupes = ( Integer ) MsgParmPt[ 4 ];
					m_AdoInptPt.m_SpeexAecPt.m_EchoSupesAct = ( Integer ) MsgParmPt[ 5 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseWebRtcAecm:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 2;
					m_AdoInptPt.m_WebRtcAecmPt.m_IsUseCNGMode = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_WebRtcAecmPt.m_EchoMode = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_WebRtcAecmPt.m_Delay = ( Integer ) MsgParmPt[ 2 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseWebRtcAec:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 3;
					m_AdoInptPt.m_WebRtcAecPt.m_EchoMode = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_WebRtcAecPt.m_Delay = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_WebRtcAecPt.m_IsUseDelayAgstcMode = ( Integer ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_WebRtcAecPt.m_IsUseExtdFilterMode = ( Integer ) MsgParmPt[ 3 ];
					m_AdoInptPt.m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode = ( Integer ) MsgParmPt[ 4 ];
					m_AdoInptPt.m_WebRtcAecPt.m_IsUseAdaptAdjDelay = ( Integer ) MsgParmPt[ 5 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseWebRtcAec3:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 4;
					m_AdoInptPt.m_WebRtcAec3Pt.m_Delay = ( Integer ) MsgParmPt[ 0 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseSpeexWebRtcAec:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

					m_AdoInptPt.m_UseWhatAec = 5;
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WorkMode = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecFilterLenMsec = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecIsUseRec = ( Integer ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoMutp = ( Float ) MsgParmPt[ 3 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoCntu = ( Float ) MsgParmPt[ 4 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupes = ( Integer ) MsgParmPt[ 5 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupesAct = ( Integer ) MsgParmPt[ 6 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmIsUseCNGMode = ( Integer ) MsgParmPt[ 7 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmEchoMode = ( Integer ) MsgParmPt[ 8 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmDelay = ( Integer ) MsgParmPt[ 9 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecEchoMode = ( Integer ) MsgParmPt[ 10 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecDelay = ( Integer ) MsgParmPt[ 11 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode = ( Integer ) MsgParmPt[ 12 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseExtdFilterMode = ( Integer ) MsgParmPt[ 13 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) MsgParmPt[ 14 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseAdaptAdjDelay = ( Integer ) MsgParmPt[ 15 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAec3Delay = ( Integer ) MsgParmPt[ 11 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_IsUseSameRoomAec = ( Integer ) MsgParmPt[ 16 ];
					m_AdoInptPt.m_SpeexWebRtcAecPt.m_SameRoomEchoMinDelay = ( Integer ) MsgParmPt[ 17 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.AecInit() != 0 ) break Out;
						m_AdoInptPt.SetIsCanUseAec();
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseNoNs:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.NsDstoy();
						m_AdoInptPt.SpeexPrpocsDstoy();
					}

					m_AdoInptPt.m_UseWhatNs = 0;

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.NsInit() != 0 ) break Out;
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseSpeexPrpocsNs:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.NsDstoy();
						m_AdoInptPt.SpeexPrpocsDstoy();
					}

					m_AdoInptPt.m_UseWhatNs = 1;
					m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseNs = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_SpeexPrpocsNsPt.m_NoiseSupes = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseDereverb = ( Integer ) MsgParmPt[ 2 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.NsInit() != 0 ) break Out;
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseWebRtcNsx:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.NsDstoy();
						m_AdoInptPt.SpeexPrpocsDstoy();
					}

					m_AdoInptPt.m_UseWhatNs = 2;
					m_AdoInptPt.m_WebRtcNsxPt.m_PolicyMode = ( Integer ) MsgParmPt[ 0 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.NsInit() != 0 ) break Out;
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseWebRtcNs:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.NsDstoy();
						m_AdoInptPt.SpeexPrpocsDstoy();
					}

					m_AdoInptPt.m_UseWhatNs = 3;
					m_AdoInptPt.m_WebRtcNsPt.m_PolicyMode = ( Integer ) MsgParmPt[ 0 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.NsInit() != 0 ) break Out;
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseRNNoise:
				{
					if( m_AdoInptPt.m_IsInit != 0 )
					{
						m_AdoInptPt.NsDstoy();
						m_AdoInptPt.SpeexPrpocsDstoy();
					}

					m_AdoInptPt.m_UseWhatNs = 4;

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.NsInit() != 0 ) break Out;
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetIsUseSpeexPrpocs:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.SpeexPrpocsDstoy();

					m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseVad = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbStart = ( Integer ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbCntu = ( Integer ) MsgParmPt[ 3 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseAgc = ( Integer ) MsgParmPt[ 4 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_AgcLevel = ( Integer ) MsgParmPt[ 5 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_AgcIncrement = ( Integer ) MsgParmPt[ 6 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_AgcDecrement = ( Integer ) MsgParmPt[ 7 ];
					m_AdoInptPt.m_SpeexPrpocsPt.m_AgcMaxGain = ( Integer ) MsgParmPt[ 8 ];

					if( m_AdoInptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
						m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val = 1;
					}
					break;
				}
				case ThrdMsgTyp.AdoInptSetUsePcm:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

					m_AdoInptPt.m_UseWhatEncd = 0;

					if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
					MediaPocsThrdTmpVarInit();
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseSpeexEncd:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

					m_AdoInptPt.m_UseWhatEncd = 1;
					m_AdoInptPt.m_SpeexEncdPt.m_UseCbrOrVbr = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_SpeexEncdPt.m_Qualt = ( Integer ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_SpeexEncdPt.m_Cmplxt = ( Integer ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_SpeexEncdPt.m_PlcExptLossRate = ( Integer ) MsgParmPt[ 3 ];

					if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
					MediaPocsThrdTmpVarInit();
					break;
				}
				case ThrdMsgTyp.AdoInptSetUseOpusEncd:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

					m_AdoInptPt.m_UseWhatEncd = 2;

					if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
					MediaPocsThrdTmpVarInit();
					break;
				}
				case ThrdMsgTyp.AdoInptSetIsSaveAdoToWaveFile:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.WaveFileWriterDstoy();

					m_AdoInptPt.m_WaveFileWriterPt.m_IsSave = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt = ( String ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_WaveFileWriterPt.m_RsltFullPathStrPt = ( String ) MsgParmPt[ 2 ];
					m_AdoInptPt.m_WaveFileWriterPt.m_WrBufSzByt = ( long ) MsgParmPt[ 3 ];

					if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.WaveFileWriterInit() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.AdoInptSetIsDrawAdoWavfmToSurface:
				{
					if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.WavfmDstoy();

					m_AdoInptPt.m_WavfmPt.m_IsDraw = ( Integer ) MsgParmPt[ 0 ];
					m_AdoInptPt.m_WavfmPt.m_SrcSurfacePt = ( SurfaceView ) MsgParmPt[ 1 ];
					m_AdoInptPt.m_WavfmPt.m_RsltSurfacePt = ( SurfaceView ) MsgParmPt[ 2 ];

					if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.WavfmInit() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.AdoInptSetIsMute:
				{
					m_AdoInptPt.m_DvcPt.m_IsMute = ( Integer ) MsgParmPt[ 0 ];
					break;
				}
				case ThrdMsgTyp.SetAdoOtpt:
				{
					if( m_AdoOtptPt.m_IsInit != 0 )
					{
						if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
						m_AdoOtptPt.Dstoy();
					}

					m_AdoOtptPt.m_SmplRate = ( Integer ) MsgParmPt[ 0 ];
					m_AdoOtptPt.m_FrmLenMsec = ( Long ) MsgParmPt[ 1 ];
					m_AdoOtptPt.m_FrmLenUnit = m_AdoOtptPt.m_FrmLenMsec * m_AdoOtptPt.m_SmplRate / 1000;
					m_AdoOtptPt.m_FrmLenData = m_AdoOtptPt.m_FrmLenUnit * 1;
					m_AdoOtptPt.m_FrmLenByt = m_AdoOtptPt.m_FrmLenData * 2;

					if( m_AdoOtptPt.m_IsInit != 0 )
					{
						if( m_AdoOtptPt.Init() != 0 ) break Out;
						m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输出流时间戳要重置。
						if( m_AdoInptPt.m_IsInit != 0 )
						{
							m_AdoInptPt.SetIsCanUseAec();
							if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
							m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件的音频输入流时间戳要重置。
						}
						else
						{
							m_AdoOtptPt.m_DvcPt.m_Pt.play(); //让音频输出设备开始播放。
							m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart = 1; //设置音频输出线程已开始。
						}
					}
					break;
				}
				case ThrdMsgTyp.AdoOtptAddStrm:
				{
					m_AdoOtptPt.AddStrm( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptDelStrm:
				{
					m_AdoOtptPt.DelStrm( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptSetStrmUsePcm:
				{
					m_AdoOtptPt.SetStrmUsePcm( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptSetStrmUseSpeexDecd:
				{
					m_AdoOtptPt.SetStrmUseSpeexDecd( ( Integer ) MsgParmPt[ 0 ], ( Integer ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptSetStrmUseOpusDecd:
				{
					m_AdoOtptPt.SetStrmUseOpusDecd( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptSetStrmIsUse:
				{
					m_AdoOtptPt.SetStrmIsUse( ( Integer ) MsgParmPt[ 0 ], ( Integer ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.AdoOtptSetIsSaveAdoToWaveFile:
				{
					if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.WaveFileWriterDstoy();

					m_AdoOtptPt.m_WaveFileWriterPt.m_IsSave = ( Integer ) MsgParmPt[ 0 ];
					m_AdoOtptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt = ( String ) MsgParmPt[ 1 ];
					m_AdoOtptPt.m_WaveFileWriterPt.m_WrBufSzByt = ( long ) MsgParmPt[ 2 ];

					if( m_AdoOtptPt.m_IsInit != 0 ) if( m_AdoOtptPt.WaveFileWriterInit() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.AdoOtptSetIsDrawAdoWavfmToSurface:
				{
					if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.WavfmDstoy();

					m_AdoOtptPt.m_WavfmPt.m_IsDraw = ( Integer ) MsgParmPt[ 0 ];
					m_AdoOtptPt.m_WavfmPt.m_SrcSurfacePt = ( SurfaceView ) MsgParmPt[ 1 ];

					if( m_AdoOtptPt.m_IsInit != 0 ) if( m_AdoOtptPt.WavfmInit() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.AdoOtptSetIsMute:
				{
					m_AdoOtptPt.m_DvcPt.m_IsMute = ( Integer ) MsgParmPt[ 0 ];
					break;
				}
				case ThrdMsgTyp.SetVdoInpt:
				{
					if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

					m_VdoInptPt.m_MaxSmplRate = ( Integer ) MsgParmPt[ 0 ];
					m_VdoInptPt.m_FrmWidth = ( Integer ) MsgParmPt[ 1 ];
					m_VdoInptPt.m_FrmHeight = ( Integer ) MsgParmPt[ 2 ];
					m_VdoInptPt.m_SrcFrmWidth = ( Integer ) MsgParmPt[ 3 ];
					m_VdoInptPt.m_SrcFrmHeight = ( Integer ) MsgParmPt[ 4 ];
					m_VdoInptPt.m_Yu12FrmLenByt = m_VdoInptPt.m_FrmWidth * m_VdoInptPt.m_FrmHeight * 3 / 2;
					m_VdoInptPt.m_ScreenRotate = ( Integer ) MsgParmPt[ 5 ];
					m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt = ( HTSurfaceView ) MsgParmPt[ 6 ];

					if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.VdoInptSetUseYu12:
				{
					if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

					m_VdoInptPt.m_UseWhatEncd = 0;

					if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.VdoInptSetUseOpenH264Encd:
				{
					if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

					m_VdoInptPt.m_UseWhatEncd = 1;
					m_VdoInptPt.m_OpenH264EncdPt.m_VdoType = ( Integer ) MsgParmPt[ 0 ];
					m_VdoInptPt.m_OpenH264EncdPt.m_EncdBitrate = ( Integer ) MsgParmPt[ 1 ];
					m_VdoInptPt.m_OpenH264EncdPt.m_BitrateCtrlMode = ( Integer ) MsgParmPt[ 2 ];
					m_VdoInptPt.m_OpenH264EncdPt.m_IDRFrmIntvl = ( Integer ) MsgParmPt[ 3 ];
					m_VdoInptPt.m_OpenH264EncdPt.m_Cmplxt = ( Integer ) MsgParmPt[ 4 ];

					if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.VdoInptSetUseSystemH264Encd:
				{
					if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

					m_VdoInptPt.m_UseWhatEncd = 2;
					m_VdoInptPt.m_SystemH264EncdPt.m_EncdBitrate = ( Integer ) MsgParmPt[ 0 ];
					m_VdoInptPt.m_SystemH264EncdPt.m_BitrateCtrlMode = ( Integer ) MsgParmPt[ 1 ];
					m_VdoInptPt.m_SystemH264EncdPt.m_IDRFrmIntvlTimeSec = ( Integer ) MsgParmPt[ 2 ];
					m_VdoInptPt.m_SystemH264EncdPt.m_Cmplxt = ( Integer ) MsgParmPt[ 3 ];

					if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.VdoInptSetUseDvc:
				{
					if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

					m_VdoInptPt.m_DvcPt.m_UseWhatDvc = ( Integer ) MsgParmPt[ 0 ];
					m_VdoInptPt.m_DvcPt.m_FrontCameraId = ( Integer ) MsgParmPt[ 1 ];
					m_VdoInptPt.m_DvcPt.m_BackCameraId = ( Integer ) MsgParmPt[ 2 ];

					if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.VdoInptSetIsBlack:
				{
					m_VdoInptPt.m_DvcPt.m_IsBlack = ( Integer ) MsgParmPt[ 0 ];
					break;
				}
				case ThrdMsgTyp.VdoOtptAddStrm:
				{
					m_VdoOtptPt.AddStrm( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptDelStrm:
				{
					m_VdoOtptPt.DelStrm( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrm:
				{
					m_VdoOtptPt.SetStrm( ( Integer ) MsgParmPt[ 0 ], ( HTSurfaceView ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrmUseYu12:
				{
					m_VdoOtptPt.SetStrmUseYu12( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrmUseOpenH264Decd:
				{
					m_VdoOtptPt.SetStrmUseOpenH264Decd( ( Integer ) MsgParmPt[ 0 ], ( Integer ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrmUseSystemH264Decd:
				{
					m_VdoOtptPt.SetStrmUseSystemH264Decd( ( Integer ) MsgParmPt[ 0 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrmIsBlack:
				{
					m_VdoOtptPt.SetStrmIsBlack( ( Integer ) MsgParmPt[ 0 ], ( Integer ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.VdoOtptSetStrmIsUse:
				{
					m_VdoOtptPt.SetStrmIsUse( ( Integer ) MsgParmPt[ 0 ], ( Integer ) MsgParmPt[ 1 ] );
					break;
				}
				case ThrdMsgTyp.SetIsUseAdoVdoInptOtpt:
				{
					int p_IsUseAdoInpt = ( Integer ) MsgParmPt[ 0 ];
					int p_IsUseAdoOtpt = ( Integer ) MsgParmPt[ 1 ];
					int p_IsUseVdoInpt = ( Integer ) MsgParmPt[ 2 ];
					int p_IsUseVdoOtpt = ( Integer ) MsgParmPt[ 3 ];

					if( p_IsUseAdoInpt >= 0 ) m_AdoInptPt.m_IsUse = p_IsUseAdoInpt;
					if( p_IsUseAdoOtpt >= 0 ) m_AdoOtptPt.m_IsUse = p_IsUseAdoOtpt;
					if( p_IsUseVdoInpt >= 0 ) m_VdoInptPt.m_IsUse = p_IsUseVdoInpt;
					if( p_IsUseVdoOtpt >= 0 ) m_VdoOtptPt.m_IsUse = p_IsUseVdoOtpt;

					if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
					WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
					break;
				}
				case ThrdMsgTyp.SetIsUseWakeLock:
				{
					m_IsUseWakeLock = ( Integer ) MsgParmPt[ 0 ];
					WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
					break;
				}
				case ThrdMsgTyp.SetIsSaveAdoVdoInptOtptToAviFile:
				{
					if( ( ( m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt != null ) && ( !m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt.equals( ( String ) MsgParmPt[ 0 ] ) ) ) || //当完整路径字符串或写入缓冲区的大小或最大流数量有修改时，才销毁。
						( m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt != ( Long ) MsgParmPt[ 1 ] ) ||
						( m_AdoVdoInptOtptAviFilePt.m_MaxStrmNum != ( Integer ) MsgParmPt[ 2 ] ) )
						AdoVdoInptOtptAviFileWriterDstoy();

					m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt = ( String )MsgParmPt[ 0 ];
					m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt = ( Long )MsgParmPt[ 1 ];
					m_AdoVdoInptOtptAviFilePt.m_MaxStrmNum = ( Integer )MsgParmPt[ 2 ];
					m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt = ( Integer ) MsgParmPt[ 3 ];
					m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt = ( Integer ) MsgParmPt[ 4 ];
					m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt = ( Integer ) MsgParmPt[ 5 ];
					m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt = ( Integer ) MsgParmPt[ 6 ];

					if( m_LastCallUserInitOrDstoy == 0 ) if( AdoVdoInptOtptAviFileWriterInit() != 0 ) break Out;
					break;
				}
				case ThrdMsgTyp.SaveStsToTxtFile:
				{
					String p_StngFileFullPathStrPt = ( String ) MsgParmPt[ 0 ];
					File p_StngFilePt = new File( p_StngFileFullPathStrPt );

					try
					{
						if( !p_StngFilePt.exists() )
						{
							p_StngFilePt.createNewFile();
						}
						FileWriter p_StngFileWriterPt = new FileWriter( p_StngFilePt );

						p_StngFileWriterPt.write( "m_CtxPt：" + m_CtxPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_IsPrintLogcat：" + m_IsPrintLogcat + "\n" );
						p_StngFileWriterPt.write( "m_IsShowToast：" + m_IsShowToast + "\n" );
						p_StngFileWriterPt.write( "m_ShowToastActPt：" + m_ShowToastActPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_IsUseWakeLock：" + m_IsUseWakeLock + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt：" + m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt：" + m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt + "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt：" + m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt + "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt：" + m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt + "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt：" + m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt + "\n" );
						p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt：" + m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_AdoInptOtptUseDvcInfoPt.m_NameStrPt：" + m_AdoInptOtptUseDvcInfoPt.m_NameStrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsUse：" + m_AdoInptPt.m_IsUse + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsInit：" + m_AdoInptPt.m_IsInit + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SmplRate：" + m_AdoInptPt.m_SmplRate + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_FrmLenMsec：" + m_AdoInptPt.m_FrmLenMsec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_FrmLenUnit：" + m_AdoInptPt.m_FrmLenUnit + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_FrmLenData：" + m_AdoInptPt.m_FrmLenData + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_FrmLenByt：" + m_AdoInptPt.m_FrmLenByt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_IsUseAdoInptSystemAecNsAgc：" + m_AdoInptPt.m_IsUseSystemAecNsAgc + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptUseWhatAec：" + m_AdoInptPt.m_UseWhatAec + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_FilterLenMsec：" + m_AdoInptPt.m_SpeexAecPt.m_FilterLenMsec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_IsUseRec：" + m_AdoInptPt.m_SpeexAecPt.m_IsUseRec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_EchoMutp：" + m_AdoInptPt.m_SpeexAecPt.m_EchoMutp + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_EchoCntu：" + m_AdoInptPt.m_SpeexAecPt.m_EchoCntu + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_EchoSupes：" + m_AdoInptPt.m_SpeexAecPt.m_EchoSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_EchoSupesAct：" + m_AdoInptPt.m_SpeexAecPt.m_EchoSupesAct + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmPt.m_IsUseCNGMode：" + m_AdoInptPt.m_WebRtcAecmPt.m_IsUseCNGMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmPt.m_EchoMode：" + m_AdoInptPt.m_WebRtcAecmPt.m_EchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecmPt.m_Delay：" + m_AdoInptPt.m_WebRtcAecmPt.m_Delay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_EchoMode：" + m_AdoInptPt.m_WebRtcAecPt.m_EchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_Delay：" + m_AdoInptPt.m_WebRtcAecPt.m_Delay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_IsUseDelayAgstcMode：" + m_AdoInptPt.m_WebRtcAecPt.m_IsUseDelayAgstcMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_IsUseExtdFilterMode：" + m_AdoInptPt.m_WebRtcAecPt.m_IsUseExtdFilterMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode：" + m_AdoInptPt.m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_IsUseAdaptAdjDelay：" + m_AdoInptPt.m_WebRtcAecPt.m_IsUseAdaptAdjDelay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAec3Pt.m_Delay：" + m_AdoInptPt.m_WebRtcAec3Pt.m_Delay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WorkMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WorkMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecFilterLenMsec：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecFilterLenMsec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecIsUseRec：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecIsUseRec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoMutp：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoMutp + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoCntu：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoCntu + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupes：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupesAct：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupesAct + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmIsUseCNGMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmIsUseCNGMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmEchoMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmDelay：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecEchoMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecEchoMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecDelay：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseExtdFilterMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseExtdFilterMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseAdaptAdjDelay：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseAdaptAdjDelay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAec3Delay：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAec3Delay + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_IsUseSameRoomAec：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_IsUseSameRoomAec + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecPt.m_SameRoomEchoMinDelay：" + m_AdoInptPt.m_SpeexWebRtcAecPt.m_SameRoomEchoMinDelay + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_UseWhatNs：" + m_AdoInptPt.m_UseWhatNs + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseNs：" + m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseNs + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsNsPt.m_NoiseSupes：" + m_AdoInptPt.m_SpeexPrpocsNsPt.m_NoiseSupes + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseDereverb：" + m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseDereverb + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcNsxPt.m_PolicyMode：" + m_AdoInptPt.m_WebRtcNsxPt.m_PolicyMode + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcNsPt.m_PolicyMode：" + m_AdoInptPt.m_WebRtcNsPt.m_PolicyMode + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs：" + m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseVad：" + m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseVad + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbStart：" + m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbStart + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbCntu：" + m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbCntu + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseAgc：" + m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseAgc + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_AgcLevel：" + m_AdoInptPt.m_SpeexPrpocsPt.m_AgcLevel + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_AgcIncrement：" + m_AdoInptPt.m_SpeexPrpocsPt.m_AgcIncrement + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_AgcDecrement：" + m_AdoInptPt.m_SpeexPrpocsPt.m_AgcDecrement + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsPt.m_AgcMaxGain：" + m_AdoInptPt.m_SpeexPrpocsPt.m_AgcMaxGain + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_UseWhatEncd：" + m_AdoInptPt.m_UseWhatEncd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdPt.m_UseCbrOrVbr：" + m_AdoInptPt.m_SpeexEncdPt.m_UseCbrOrVbr + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdPt.m_Qualt：" + m_AdoInptPt.m_SpeexEncdPt.m_Qualt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdPt.m_Cmplxt：" + m_AdoInptPt.m_SpeexEncdPt.m_Cmplxt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexEncdPt.m_PlcExptLossRate：" + m_AdoInptPt.m_SpeexEncdPt.m_PlcExptLossRate + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WaveFileWriterPt.m_IsSave：" + m_AdoInptPt.m_WaveFileWriterPt.m_IsSave + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt：" + m_AdoInptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WaveFileWriterPt.m_RsltFullPathStrPt：" + m_AdoInptPt.m_WaveFileWriterPt.m_RsltFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WaveFileWriterPt.m_WrBufSzByt：" + m_AdoInptPt.m_WaveFileWriterPt.m_WrBufSzByt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WavfmPt.m_IsDraw：" + m_AdoInptPt.m_WavfmPt.m_IsDraw + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WavfmPt.m_SrcSurfacePt：" + m_AdoInptPt.m_WavfmPt.m_SrcSurfacePt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_WavfmPt.m_RsltSurfacePt：" + m_AdoInptPt.m_WavfmPt.m_RsltSurfacePt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_DvcPt.m_BufSzByt：" + m_AdoInptPt.m_DvcPt.m_BufSzByt + "\n" );
						p_StngFileWriterPt.write( "m_AdoInptPt.m_DvcPt.m_IsMute：" + m_AdoInptPt.m_DvcPt.m_IsMute + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsUse：" + m_AdoOtptPt.m_IsUse + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsInit：" + m_AdoOtptPt.m_IsInit + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_SmplRate：" + m_AdoOtptPt.m_SmplRate + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenMsec：" + m_AdoOtptPt.m_FrmLenMsec + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenUnit：" + m_AdoOtptPt.m_FrmLenUnit + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenData：" + m_AdoOtptPt.m_FrmLenData + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenByt：" + m_AdoOtptPt.m_FrmLenByt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_StrmCntnrPt：" + m_AdoOtptPt.m_StrmCntnrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						for( AdoOtpt.Strm p_AdoOtptStrm : m_AdoOtptPt.m_StrmCntnrPt )
						{
							p_StngFileWriterPt.write( "m_AdoOtptPt.m_Idx：" + p_AdoOtptStrm.m_Idx + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsUse：" + p_AdoOtptStrm.m_IsUse + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatDecd：" + p_AdoOtptStrm.m_UseWhatDecd + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_AdoOtptPt.m_SpeexDecdPt.m_IsUsePrcplEnhsmt：" + p_AdoOtptStrm.m_SpeexDecdPt.m_IsUsePrcplEnhsmt + "\n" );
							p_StngFileWriterPt.write( "\n" );
						}
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_WavfmPt.m_IsDraw：" + m_AdoOtptPt.m_WavfmPt.m_IsDraw + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_WavfmPt.m_SrcSurfacePt：" + m_AdoOtptPt.m_WavfmPt.m_SrcSurfacePt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_WaveFileWriterPt.m_IsSave：" + m_AdoOtptPt.m_WaveFileWriterPt.m_IsSave + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_WaveFileWriterPt.m_WaveFileFullPathStrPt：" + m_AdoOtptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_WaveFileWriterPt.m_WaveFileWrBufSzByt：" + m_AdoOtptPt.m_WaveFileWriterPt.m_WrBufSzByt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_DvcPt.m_BufSzByt：" + m_AdoOtptPt.m_DvcPt.m_BufSzByt + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_DvcPt.m_UseWhatAdoOtptStreamType：" + m_AdoOtptPt.m_DvcPt.m_UseWhatStreamType + "\n" );
						p_StngFileWriterPt.write( "m_AdoOtptPt.m_DvcPt.m_AdoOtptIsMute：" + m_AdoOtptPt.m_DvcPt.m_IsMute + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_VdoInptPt.m_IsUse：" + m_VdoInptPt.m_IsUse + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_IsInit：" + m_VdoInptPt.m_IsInit + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_MaxSmplRate：" + m_VdoInptPt.m_MaxSmplRate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_FrmWidth：" + m_VdoInptPt.m_FrmWidth + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_FrmHeight：" + m_VdoInptPt.m_FrmHeight + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_Yu12FrmLenByt：" + m_VdoInptPt.m_Yu12FrmLenByt + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_ScreenRotate：" + m_VdoInptPt.m_ScreenRotate + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_UseWhatEncd：" + m_VdoInptPt.m_UseWhatEncd + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdPt.m_VdoType：" + m_VdoInptPt.m_OpenH264EncdPt.m_VdoType + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdPt.m_EncdBitrate：" + m_VdoInptPt.m_OpenH264EncdPt.m_EncdBitrate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdPt.m_BitrateCtrlMode：" + m_VdoInptPt.m_OpenH264EncdPt.m_BitrateCtrlMode + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdPt.m_IDRFrmIntvl：" + m_VdoInptPt.m_OpenH264EncdPt.m_IDRFrmIntvl + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_OpenH264EncdPt.m_Cmplxt：" + m_VdoInptPt.m_OpenH264EncdPt.m_Cmplxt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdPt.m_EncdBitrate：" + m_VdoInptPt.m_SystemH264EncdPt.m_EncdBitrate + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdPt.m_BitrateCtrlMode：" + m_VdoInptPt.m_SystemH264EncdPt.m_BitrateCtrlMode + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdPt.m_IDRFrmIntvlTimeSec：" + m_VdoInptPt.m_SystemH264EncdPt.m_IDRFrmIntvlTimeSec + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_SystemH264EncdPt.m_Cmplxt：" + m_VdoInptPt.m_SystemH264EncdPt.m_Cmplxt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_DvcPt.m_UseWhatDvc：" + m_VdoInptPt.m_DvcPt.m_UseWhatDvc + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt：" + m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt + "\n" );
						p_StngFileWriterPt.write( "m_VdoInptPt.m_DvcPt.m_IsBlack：" + m_VdoInptPt.m_DvcPt.m_IsBlack + "\n" );
						p_StngFileWriterPt.write( "\n" );

						p_StngFileWriterPt.write( "m_VdoOtptPt.m_IsUse：" + m_VdoOtptPt.m_IsUse + "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_IsInit：" + m_VdoOtptPt.m_IsInit + "\n" );
						p_StngFileWriterPt.write( "\n" );
						p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt：" + m_VdoOtptPt.m_StrmCntnrPt + "\n" );
						p_StngFileWriterPt.write( "\n" );
						for( VdoOtpt.Strm p_VdoOtptStrm : m_VdoOtptPt.m_StrmCntnrPt )
						{
							p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt.m_Idx：" + p_VdoOtptStrm.m_Idx + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt.m_IsUse：" + p_VdoOtptStrm.m_UseWhatDecd + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt.m_UseWhatDecd：" + p_VdoOtptStrm.m_OpenH264DecdPt.m_DecdThrdNum + "\n" );
							p_StngFileWriterPt.write( "\n" );
							p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt.m_DvcPt.m_DspySurfaceViewPt：" + p_VdoOtptStrm.m_DvcPt.m_DspySurfaceViewPt + "\n" );
							p_StngFileWriterPt.write( "m_VdoOtptPt.m_StrmCntnrPt.m_DvcPt.m_IsBlack：" + p_VdoOtptStrm.m_DvcPt.m_IsBlack + "\n" );
							p_StngFileWriterPt.write( "\n" );
						}

						p_StngFileWriterPt.flush();
						p_StngFileWriterPt.close();
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存状态到Txt文件 " + p_StngFileFullPathStrPt + " 成功。" );
					} catch( IOException e )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存状态到Txt文件 " + p_StngFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
						break Out;
					}
					break;
				}
				case ThrdMsgTyp.RqirExit:
				{
					switch( ( Integer ) MsgParmPt[ 0 ] )
					{
						case 1: //为请求退出。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收退出请求：退出。" );

							//执行顺序：媒体销毁，用户销毁。
							if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
							{
								if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
								if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.UserDstoy ) != 0 ) break Out;
							}
							else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
							{

							}
							break;
						}
						case 2: //请求重启。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收退出请求：重启。" );

							//执行顺序：媒体销毁，用户销毁，用户初始化，媒体初始化。
							if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
							{
								if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
								if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.UserDstoy ) != 0 ) break Out;
							}
							else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
							{

							}
							if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.UserInit ) != 0 ) break Out;
							if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
							break;
						}
						case 3: //请求重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收退出请求：重启但不调用用户定义的UserInit初始化函数和UserDstoy销毁函数。" );

							//执行顺序：媒体销毁，媒体初始化。
							if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
							if( m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
							break;
						}
					}
					break;
				}
				case ThrdMsgTyp.UserInit:
				{
					m_LastCallUserInitOrDstoy = 0; //设置上一次调用了用户定义的初始化函数。
					UserInit(); //调用用户定义的初始化函数。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。" );
					break;
				}
				case ThrdMsgTyp.UserDstoy:
				{
					m_LastCallUserInitOrDstoy = 1; //设置上一次调用了用户定义的销毁函数。
					UserDstoy(); //调用用户定义的销毁函数。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
					break;
				}
				case ThrdMsgTyp.AdoVdoInptOtptInit:
				{
					if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就初始化音视频输入输出、音视频输入输出Avi文件写入器。
					{
						if( AdoInptOtptDvcChgCallbackInit() != 0 ) break Out;
						if( AdoVdoInptOtptInit() != 0 ) break Out;
						if( AdoVdoInptOtptAviFileWriterInit() != 0 ) break Out;
					}
					break;
				}
				case ThrdMsgTyp.AdoVdoInptOtptDstoy:
				{
					AdoVdoInptOtptAviFileWriterDstoy();
					AdoVdoInptOtptDstoy();
					AdoInptOtptDvcChgCallbackDstoy();
					break;
				}
				case ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcChg:
				{
					UserDvcChg( m_AdoInptOtptUseDvcInfoPt, null );
					break;
				}
				case ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcClos:
				{
					ArrayList< AdoInptOtptDvcInfo > p_AdoInptOtptDvcInfoLstPt = MediaPocsThrd.GetAllAdoInptOtptDvcInfo();

					OutFindAdoInptOtptDvcInfo:
					{
						//查找当前使用的音频输入输出设备信息。
						for( int i = 0; i < p_AdoInptOtptDvcInfoLstPt.size(); i++ )
						{
							if( p_AdoInptOtptDvcInfoLstPt.get( i ).m_NameStrPt.equals( m_AdoInptOtptUseDvcInfoPt.m_NameStrPt ) )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音频输入输出设备[" + m_AdoInptOtptUseDvcInfoPt.m_NameStrPt + "]已经关闭，切换到音频输入输出设备[" + p_AdoInptOtptDvcInfoLstPt.get( i ).m_NameStrPt + "]。" );
								m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.SetAdoInptOtptUseDvc, p_AdoInptOtptDvcInfoLstPt.get( i ), m_AdoOtptPt.m_DvcPt.m_UseWhatStreamType );
								break OutFindAdoInptOtptDvcInfo;
							}
						}

						//如果没有找到，就使用默认扬声器。
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音频输入输出设备[" + m_AdoInptOtptUseDvcInfoPt.m_NameStrPt + "]已经关闭，切换到音频输入输出设备[" + p_AdoInptOtptDvcInfoLstPt.get( 0 ).m_NameStrPt + "]。" );
						m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.SetAdoInptOtptUseDvc, p_AdoInptOtptDvcInfoLstPt.get( 0 ), m_AdoOtptPt.m_DvcPt.m_UseWhatStreamType );
					}

					break;
				}
				case ThrdMsgTyp.ThrdMsgTypVdoInptDvcChg:
				{
					m_VdoInptUseDvcInfoPt = new VdoInptDvcInfo();
					m_VdoInptUseDvcInfoPt.m_DvcTyp = m_VdoInptPt.m_DvcPt.m_UseWhatDvc;
					m_VdoInptUseDvcInfoPt.m_CameraId = ( int )MsgParmPt[ 0 ];
					UserDvcChg( null, m_VdoInptUseDvcInfoPt );
					break;
				}
				default: //用户消息。
				{
					p_TmpInt32 = UserMsg( MsgTyp - ThrdMsgTyp.UserMsgMinVal, MsgParmPt );
					if( p_TmpInt32 == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的消息函数成功。返回值：" + p_TmpInt32 );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的消息函数失败。返回值：" + p_TmpInt32 );
						break Out;
					}
					break;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就执行销毁。
			{
				//执行顺序：媒体销毁，用户销毁。
				m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.AdoVdoInptOtptDstoy );
				m_ThrdMsgQueuePt.SendMsg( 1, 0, ThrdMsgTyp.UserDstoy );
			}
		}
		return p_Rslt;
	}

	//媒体处理线程的音视频输入输出帧处理。
	private void AdoVdoInptOtptFrmPocs()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		int p_TmpInt321;
		int p_TmpInt322;
		long p_LastTickMsec = 0;
		long p_NowTickMsec = 0;

		Out:
		{
			if( m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis();

			//调用用户定义的处理函数。
			{
				UserPocs();
				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			}

			//取出音频输入Pcm格式原始帧和音频输出Pcm格式原始帧。
			if( m_AdoInptPt.m_PcmSrcFrmCntnrPt != null ) p_TmpInt321 = m_AdoInptPt.m_PcmSrcFrmCntnrPt.size(); //获取Pcm格式原始帧容器的元素总数。
			else p_TmpInt321 = 0;
			if( m_AdoOtptPt.m_PcmSrcFrmCntnrPt != null ) p_TmpInt322 = m_AdoOtptPt.m_PcmSrcFrmCntnrPt.size(); //获取Pcm格式原始帧容器的元素总数。
			else p_TmpInt322 = 0;
			if( m_AdoInptPt.m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器。
			{
				if( ( p_TmpInt321 > 0 ) && ( p_TmpInt322 > 0 ) ) //如果Pcm格式原始帧容器和Pcm格式原始帧容器中都有帧了，就开始取出。
				{
					m_ThrdPt.m_AdoInptPcmSrcFrmPt = ( short[] ) m_AdoInptPt.m_PcmSrcFrmCntnrPt.poll(); //从音频输入Pcm格式原始帧容器中取出并删除第一个帧。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入Pcm格式原始帧容器中取出并删除第一个帧，音频输入Pcm格式原始帧容器元素总数：" + p_TmpInt321 + "。" );

					m_ThrdPt.m_AdoOtptPcmSrcFrmPt = ( short[] ) m_AdoOtptPt.m_PcmSrcFrmCntnrPt.poll(); //从音频输出Pcm格式原始帧容器中取出并删除第一个帧。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出Pcm格式原始帧容器中取出并删除第一个帧，音频输出Pcm格式原始帧容器元素总数：" + p_TmpInt322 + "。" );

					//将音频输入Pcm格式原始帧复制到音频输入Pcm格式结果帧，方便处理。
					System.arraycopy( m_ThrdPt.m_AdoInptPcmSrcFrmPt, 0, m_ThrdPt.m_AdoInptPcmRsltFrmPt, 0, m_ThrdPt.m_AdoInptPcmSrcFrmPt.length );
				}
			}
			else //如果不可以使用声学回音消除器。
			{
				if( p_TmpInt321 > 0 ) //如果Pcm格式原始帧容器有帧了，就开始取出。
				{
					m_ThrdPt.m_AdoInptPcmSrcFrmPt = ( short[] ) m_AdoInptPt.m_PcmSrcFrmCntnrPt.poll(); //从音频输入Pcm格式原始帧容器中取出并删除第一个帧。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入Pcm格式原始帧容器中取出并删除第一个帧，音频输入Pcm格式原始帧容器元素总数：" + p_TmpInt321 + "。" );

					//将音频输入Pcm格式原始帧复制到音频输入Pcm格式结果帧，方便处理。
					System.arraycopy( m_ThrdPt.m_AdoInptPcmSrcFrmPt, 0, m_ThrdPt.m_AdoInptPcmRsltFrmPt, 0, m_ThrdPt.m_AdoInptPcmSrcFrmPt.length );
				}

				if( p_TmpInt322 > 0 ) //如果Pcm格式原始帧容器有帧了，就开始取出。
				{
					m_ThrdPt.m_AdoOtptPcmSrcFrmPt = ( short[] ) m_AdoOtptPt.m_PcmSrcFrmCntnrPt.poll(); //从音频输出Pcm格式原始帧容器中取出并删除第一个帧。
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出Pcm格式原始帧容器中取出并删除第一个帧，音频输出Pcm格式原始帧容器元素总数：" + p_TmpInt322 + "。" );
				}
			}

			//处理音频输入帧开始。
			if( m_ThrdPt.m_AdoInptPcmSrcFrmPt != null )
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
							if( m_AdoInptPt.m_SpeexAecPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器成功。" );
								short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器失败。" );
							}
							break;
						}
						case 2: //如果要使用WebRtc定点版声学回音消除器。
						{
							if( m_AdoInptPt.m_WebRtcAecmPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器成功。" );
								short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器失败。" );
							}
							break;
						}
						case 3: //如果要使用WebRtc浮点版声学回音消除器。
						{
							if( m_AdoInptPt.m_WebRtcAecPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器成功。" );
								short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器失败。" );
							}
							break;
						}
						case 4: //如果要使用WebRtc第三版声学回音消除器。
						{
							if( m_AdoInptPt.m_WebRtcAec3Pt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc第三版声学回音消除器成功。" );
								short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc第三版声学回音消除器失败。" );
							}
							break;
						}
						case 5: //如果要使用SpeexWebRtc三重声学回音消除器。
						{
							if( m_AdoInptPt.m_SpeexWebRtcAecPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器成功。" );
								short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
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
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：在使用Speex预处理器时一起使用噪音抑制。" );
						break;
					}
					case 2: //如果要使用WebRtc定点版噪音抑制器。
					{
						if( m_AdoInptPt.m_WebRtcNsxPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器成功。" );
							short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器失败。" );
						}
						break;
					}
					case 3: //如果要使用WebRtc浮点版噪音抑制器。
					{
						if( m_AdoInptPt.m_WebRtcNsPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器成功。" );
							short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器失败。" );
						}
						break;
					}
					case 4: //如果要使用RNNoise噪音抑制器。
					{
						if( m_AdoInptPt.m_RNNoisePt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器成功。" );
							short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器失败。" );
						}
						break;
					}
				}

				//使用Speex预处理器。
				if( ( m_AdoInptPt.m_UseWhatNs == 1 ) || ( m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs != 0 ) )
				{
					if( m_AdoInptPt.m_SpeexPrpocsPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmTmpFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器成功。语音活动状态：" + m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val );
						short p_TmpPt[] = m_ThrdPt.m_AdoInptPcmRsltFrmPt;m_ThrdPt.m_AdoInptPcmRsltFrmPt = m_ThrdPt.m_AdoInptPcmTmpFrmPt;m_ThrdPt.m_AdoInptPcmTmpFrmPt = p_TmpPt; //交换音频输入Pcm格式结果帧和音频输入Pcm格式临时帧。
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器失败。" );
					}
				}

				//判断音频输入是否静音。在音频输入处理完后再设置静音，这样可以保证音频输入处理器的连续性。
				if( m_AdoInptPt.m_DvcPt.m_IsMute != 0 )
				{
					Arrays.fill( m_ThrdPt.m_AdoInptPcmRsltFrmPt, ( short ) 0 );
					if( ( m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs != 0 ) && ( m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseVad != 0 ) ) //如果要使用Speex预处理器，且要使用语音活动检测。
					{
						m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val = 0;
					}
				}

				//使用编码器。
				switch( m_AdoInptPt.m_UseWhatEncd )
				{
					case 0: //如果要使用Pcm原始数据。
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Pcm原始数据。" );
						break;
					}
					case 1: //如果要使用Speex编码器。
					{
						if( m_AdoInptPt.m_SpeexEncdPt.m_Pt.Pocs( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmPt.length, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器成功。音频输入已编码格式结果帧的长度：" + m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val + "，是否需要传输：" + m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val );
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
						break;
					}
				}

				//使用波形器。
				if( m_AdoInptPt.m_WavfmPt.m_IsDraw != 0 )
				{
					if( m_AdoInptPt.m_WavfmPt.m_SrcPt.Draw( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmSrcFrmPt.length, m_AdoInptPt.m_WavfmPt.m_SrcSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始波形器绘制音频输入原始波形到Surface成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始波形器绘制音频输入原始波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
					if( m_AdoInptPt.m_WavfmPt.m_RsltPt.Draw( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt.length, m_AdoInptPt.m_WavfmPt.m_RsltSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果波形器绘制音频输入结果波形到Surface成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果波形器绘制音频输入结果波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
				}

				//使用Wave文件写入器。
				if( m_AdoInptPt.m_WaveFileWriterPt.m_IsSave != 0 )
				{
					if( m_AdoInptPt.m_WaveFileWriterPt.m_SrcPt.WriteShort( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmSrcFrmPt.length ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始Wave文件写入器写入音频输入Pcm格式原始帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始Wave文件写入器写入音频输入Pcm格式原始帧失败。" );
					}
					if( m_AdoInptPt.m_WaveFileWriterPt.m_RsltPt.WriteShort( m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt.length ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果Wave文件写入器写入音频输入Pcm格式结果帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果Wave文件写入器写入音频输入Pcm格式结果帧失败。" );
					}
				}

				//使用音视频输入输出Avi文件。
				if( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt != 0 )
				{
					if( m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset != 0 ) //如果音视频输入输出Avi文件的音频输入流时间戳要重置。
					{
						p_NowTickMsec = SystemClock.uptimeMillis();

						m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx, p_NowTickMsec, null );
						m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx, p_NowTickMsec, null );
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件音频输入Pcm格式原始结果流的当前时间戳为 " + p_NowTickMsec + " 。" );

						m_AdoVdoInptOtptAviFilePt.m_AdoInptStrmTimeStampIsReset = 0; //设置音视频输入输出Avi文件的音频输入流时间戳不重置。
					}

					if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx, m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_AdoInptPt.m_FrmLenData, m_AdoInptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输入Pcm格式原始帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输入Pcm格式原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
					if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmRsltStrmIdx, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenData, m_AdoInptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输入Pcm格式结果帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输入Pcm格式结果帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
					//{ long p_Tick;HTLong p_CurTimeStamp = new HTLong(); p_Tick = SystemClock.uptimeMillis(); m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmGetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_AdoInptPcmSrcStrmIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件音频输入帧时间戳：" + p_Tick + "  " + p_CurTimeStamp.m_Val + "  " + ( p_Tick - p_CurTimeStamp.m_Val ) ); }
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：音频输入帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			} //处理音频输入帧结束。

			//处理音频输出帧开始。
			if( m_ThrdPt.m_AdoOtptPcmSrcFrmPt != null )
			{
				//使用波形器。
				if( m_AdoOtptPt.m_WavfmPt.m_IsDraw != 0 )
				{
					if( m_AdoOtptPt.m_WavfmPt.m_SrcPt.Draw( m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt.length, m_AdoOtptPt.m_WavfmPt.m_SrcSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始波形器绘制音频输入原始波形到Surface成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始波形器绘制音频输出原始波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
				}

				//使用Wave文件写入器。
				if( m_AdoOtptPt.m_WaveFileWriterPt.m_IsSave != 0 )
				{
					if( m_AdoOtptPt.m_WaveFileWriterPt.m_SrcPt.WriteShort( m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_ThrdPt.m_AdoOtptPcmSrcFrmPt.length ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始Wave文件写入器写入音频输出帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始Wave文件写入器写入音频输出帧失败。" );
					}
				}

				//使用音视频输入输出Avi文件。
				if( m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt != 0 )
				{
					if( m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset != 0 ) //如果音视频输入输出Avi文件的音频输出流时间戳要重置。
					{
						p_NowTickMsec = SystemClock.uptimeMillis();

						m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx, p_NowTickMsec, null );
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件音频输出Pcm格式原始流的当前时间戳为 " + p_NowTickMsec + " 。" );

						m_AdoVdoInptOtptAviFilePt.m_AdoOtptStrmTimeStampIsReset = 0; //设置音视频输入输出Avi文件的音频输出流时间戳不重置。
					}

					if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx, m_ThrdPt.m_AdoOtptPcmSrcFrmPt, m_AdoOtptPt.m_FrmLenData, m_AdoOtptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输出Pcm格式原始帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入音频输出Pcm格式原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
					//{ long p_Tick;HTLong p_CurTimeStamp = new HTLong(); p_Tick = SystemClock.uptimeMillis(); m_AdoVdoInptOtptAviFilePt.m_WriterPt.AdoStrmGetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_AdoOtptPcmSrcStrmIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件音频输出帧时间戳：" + p_Tick + "  " + p_CurTimeStamp.m_Val + "  " + ( p_Tick - p_CurTimeStamp.m_Val ) ); }
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：音频输出帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			} //处理音频输出帧结束。

			//处理视频输入帧开始。
			if( m_VdoInptPt.m_FrmCntnrPt != null ) p_TmpInt321 = m_VdoInptPt.m_FrmCntnrPt.size(); //获取视频输入帧容器的元素总数。
			else p_TmpInt321 = 0;
			if( ( p_TmpInt321 > 0 ) && //如果视频输入帧容器中有帧了。
				( ( m_ThrdPt.m_AdoInptPcmSrcFrmPt != null ) || ( m_AdoInptPt.m_PcmSrcFrmCntnrPt == null ) ) ) //且已经处理了音频输入帧或不使用Pcm格式原始帧容器。
			{
				m_ThrdPt.m_VdoInptFrmPt = ( VdoInpt.Frm ) m_VdoInptPt.m_FrmCntnrPt.poll(); //从视频输入帧容器中取出并删除第一个帧。
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输入帧容器中取出并删除第一个帧，视频输入帧容器元素总数：" + p_TmpInt321 + "。" );

				//使用音视频输入输出Avi文件。
				if( ( m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt != 0 ) && ( m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val != 0 ) )
				{
					if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.VdoStrmWriteByte( m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx, m_ThrdPt.m_VdoInptFrmPt.m_TimeStampMsec, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val, m_ErrInfoVstrPt ) == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入视频输入已编码格式结果帧成功。" );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入视频输入已编码格式结果帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
					}
					//{ HTLong p_CurTimeStamp = new HTLong(); m_AdoVdoInptOtptAviFilePt.m_WriterPt.VdoStrmGetCurTimeStamp( m_AdoVdoInptOtptAviFilePt.m_VdoInptEncdRsltStrmIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件视频输入帧时间戳：" + m_ThrdPt.m_VdoInptFrmPt.m_TimeStampMsec + "  " + p_CurTimeStamp.m_Val + "  " + ( m_ThrdPt.m_VdoInptFrmPt.m_TimeStampMsec - p_CurTimeStamp.m_Val ) ); }
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			} //处理视频输入帧结束。

			//处理视频输出帧开始。
			if( m_VdoOtptPt.m_FrmCntnrPt != null ) p_TmpInt321 = m_VdoOtptPt.m_FrmCntnrPt.size(); //获取视频输出帧容器的元素总数。
			else p_TmpInt321 = 0;
			if( p_TmpInt321 > 0 ) //如果视频输出帧容器中有帧了。
			{
				m_ThrdPt.m_VdoOtptFrmPt = ( VdoOtpt.Frm ) m_VdoOtptPt.m_FrmCntnrPt.poll(); //从视频输出帧容器中取出并删除第一个帧。
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输出帧容器中取出并删除第一个帧，视频输出帧容器元素总数：" + p_TmpInt321 + "。" );

				//使用音视频输入输出Avi文件。
				if( ( m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt != 0 ) && ( m_ThrdPt.m_VdoOtptFrmPt.m_EncdSrcFrmLenBytPt.m_Val != 0 ) )
				{
					Integer p_VdoOtptStrmAviFileIdx;

					p_VdoOtptStrmAviFileIdx = m_AdoVdoInptOtptAviFilePt.m_VdoOtptEncdSrcStrmIdxMapPt.get( m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx );
					if( p_VdoOtptStrmAviFileIdx == null )
					{
						HTInt p_TmpHTInt = new HTInt();
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.AddVdoStrm( 875967048/*H264*/, 50, p_TmpHTInt, m_ErrInfoVstrPt ) == 0 ) //最大采样频率应该尽量被1000整除。
						{
							p_VdoOtptStrmAviFileIdx = p_TmpHTInt.m_Val;
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：音视频输入输出Avi文件添加视频输出已编码格式原始流成功。索引：" + p_VdoOtptStrmAviFileIdx + "。" );
							m_AdoVdoInptOtptAviFilePt.m_VdoOtptEncdSrcStrmIdxMapPt.put( m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx, p_VdoOtptStrmAviFileIdx );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：音视频输入输出Avi文件添加视频输出已编码格式原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
						}
					}

					if( ( p_VdoOtptStrmAviFileIdx != null ) && ( p_VdoOtptStrmAviFileIdx != -1 ) )
					{
						if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.VdoStrmWriteByte( p_VdoOtptStrmAviFileIdx, m_ThrdPt.m_VdoOtptFrmPt.m_TimeStampMsec, m_ThrdPt.m_VdoOtptFrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_VdoOtptFrmPt.m_EncdSrcFrmLenBytPt.m_Val, m_ErrInfoVstrPt ) == 0 )
						{
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：使用音视频输入输出Avi文件写入器写入视频输出已编码格式原始帧成功。" );
						}
						else
						{
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：使用音视频输入输出Avi文件写入器写入视频输出已编码格式原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
						}
						//{ HTLong p_CurTimeStamp = new HTLong(); m_AdoVdoInptOtptAviFilePt.m_WriterPt.VdoStrmGetCurTimeStamp( p_VdoOtptStrmAviFileIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件视频输出帧时间戳：" + m_ThrdPt.m_VdoOtptFrmPt.m_TimeStampMsec + "  " + p_CurTimeStamp.m_Val + "  " + ( m_ThrdPt.m_VdoOtptFrmPt.m_TimeStampMsec - p_CurTimeStamp.m_Val ) ); }
					}
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			} //处理视频输出帧结束。

			//调用用户定义的读取音视频输入帧函数。
			OutUserReadAdoVdoInptFrmFunc:
			{
				if( m_ThrdPt.m_AdoInptPcmSrcFrmPt != null ) //如果有音频输入帧。
				{
					if( m_ThrdPt.m_AdoInptEncdRsltFrmPt != null ) //如果有音频输入已编码格式结果帧。
					{
						if( m_ThrdPt.m_VdoInptFrmPt != null ) //如果有视频输入帧。
						{
							if( m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt != null ) //如果有视频输入已编码格式结果帧。
							{
								UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
													   m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val,
													   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
							}
							else //如果没有视频输入已编码格式结果帧。
							{
								UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
													   m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val,
													   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
													   null, 0L );
							}
						}
						else //如果没有视频输入帧。
						{
							UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
												   m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val,
												   null, 0, 0, 0,
												   null, 0, 0, 0,
												   null, 0L );
						}
					}
					else //如果没有音频输入已编码格式结果帧。
					{
						if( m_ThrdPt.m_VdoInptFrmPt != null ) //如果有视频输入帧。
						{
							if( m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt != null ) //如果有视频输入已编码格式结果帧。
							{
								UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
													   null, 0, 0,
													   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
							}
							else //如果没有视频输入已编码格式结果帧。
							{
								UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
													   null, 0, 0,
													   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
													   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
													   null, 0L );
							}
						}
						else //如果没有视频输入帧。
						{
							UserReadAdoVdoInptFrm( m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
												   null, 0, 0,
												   null, 0, 0, 0,
												   null, 0, 0, 0,
												   null, 0L );
						}
					}
				}
				else //如果没有音频输入帧。
				{
					if( m_ThrdPt.m_VdoInptFrmPt != null ) //如果有视频输入帧。
					{
						if( m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt != null ) //如果有视频输入已编码格式结果帧。
						{
							UserReadAdoVdoInptFrm( null, null, 0, 0,
												   null, 0, 0,
												   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
												   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
												   m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
						}
						else //如果没有视频输入已编码格式结果帧。
						{
							UserReadAdoVdoInptFrm( null, null, 0, 0,
												   null, 0, 0,
												   m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
												   m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
												   null, 0 );
						}
					}
					else //如果没有视频输入帧。
					{
						break OutUserReadAdoVdoInptFrmFunc;
					}
				}

				if( m_IsPrintLogcat != 0 )
				{
					p_NowTickMsec = SystemClock.uptimeMillis();
					Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
					p_LastTickMsec = SystemClock.uptimeMillis();
				}
			}

			if( m_ThrdPt.m_AdoInptPcmSrcFrmPt != null ) //如果取出了音频输入Pcm格式原始帧，就放入到音频输入Pcm格式空闲帧队列。注意：从取出到放入过程中不能跳出，否则会内存泄露。
			{
				m_AdoInptPt.m_PcmIdleFrmCntnrPt.offer( m_ThrdPt.m_AdoInptPcmSrcFrmPt );
				m_ThrdPt.m_AdoInptPcmSrcFrmPt = null;
			}
			if( m_ThrdPt.m_AdoOtptPcmSrcFrmPt != null ) //如果取出了音频输出Pcm格式原始帧，就追加到Pcm格式空闲帧容器。注意：从取出到追加过程中不能跳出，否则会内存泄露。
			{
				m_AdoOtptPt.m_PcmIdleFrmCntnrPt.offer( m_ThrdPt.m_AdoOtptPcmSrcFrmPt );
				m_ThrdPt.m_AdoOtptPcmSrcFrmPt = null;
			}
			if( m_ThrdPt.m_VdoInptFrmPt != null ) //如果取出了视频输入帧，就追加到视频输入空闲帧容器。注意：从取出到追加过程中不能跳出，否则会内存泄露。
			{
				m_VdoInptPt.m_IdleFrmCntnrPt.offer( m_ThrdPt.m_VdoInptFrmPt );
				m_ThrdPt.m_VdoInptFrmPt = null;
			}
			if( m_ThrdPt.m_VdoOtptFrmPt != null ) //如果取出了视频输出帧，就追加到视频输出空闲帧容器。注意：从取出到追加过程中不能跳出，否则会内存泄露。
			{
				m_VdoOtptPt.m_IdleFrmCntnrPt.offer( m_ThrdPt.m_VdoOtptFrmPt );
				m_ThrdPt.m_VdoOtptFrmPt = null;
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//媒体处理线程的主函数。
	public void run()
	{
		long p_LastTickMsec = 0;

		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本地代码的指令集名称（CPU类型+ ABI约定）：" + android.os.Build.CPU_ABI + "，设备型号：" + android.os.Build.MODEL + "，上下文：" + m_CtxPt + "。" );

		m_RunFlag = RunFlag.Run; //设置本线程运行标记为正在运行。
		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：开始准备音视频输入输出帧处理。" );

		m_ThrdMsgQueuePt.SendMsg( 0, 1, ThrdMsgTyp.UserInit );
		m_ThrdMsgQueuePt.SendMsg( 0, 1, ThrdMsgTyp.AdoVdoInptOtptInit );

		//媒体处理循环开始。
		while( true )
		{
			while( m_ThrdMsgQueuePt.MsgPocsThrdMsgPocs() == 0 ); //进行线程消息处理，直到线程消息队列为空。
			if( m_LastCallUserInitOrDstoy != 0 ) break; //如果线程消息队列为空，且上一次调用了用户定义的销毁函数，就表示本线程需要退出。

			if( m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis();

			AdoVdoInptOtptFrmPocs(); //音视频输入输出帧处理。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出帧处理全部完毕，耗时 " + ( SystemClock.uptimeMillis() - p_LastTickMsec ) + " 毫秒。" );

			SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
		} //媒体处理循环结束。

		MediaPocsThrdTmpVarDstoy();

		WakeLockInitOrDstoy( 0 ); //销毁唤醒锁。

		m_RunFlag = RunFlag.Exit; //设置本线程运行标记为已经退出。
		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程已退出。" );
	}
}
