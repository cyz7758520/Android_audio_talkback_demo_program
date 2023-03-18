package HeavenTao.Media;

import android.Manifest;
import android.app.Activity;
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
import java.util.HashMap;
import java.util.LinkedList;

import HeavenTao.Data.*;

//媒体处理线程。
public abstract class MediaPocsThrd extends Thread
{
    public static String m_CurClsNameStrPt = "MediaPocsThrd"; //存放当前类名称字符串。

    public enum RunFlag
    {
        Norun, //运行标记：未开始运行。
        Run, //运行标记：正在运行。
        Exit, //运行标记：已经退出。
    }
    public RunFlag m_RunFlag; //存放本线程运行标记。

    public enum ExitCode
    {
        Normal, //退出码：正常退出。
        UserInit, //退出码：调用用户定义的初始化函数失败。
        AdoVdoInptOtptInit, //退出码：音视频输入输出初始化失败。
        MediaMsgPocs, //退出码：媒体消息处理失败。
        AdoVdoInptOtptPocs, //退出码：音视频输入输出处理失败。
    }
    public ExitCode m_ExitCode; //存放退出码。

    public int m_LastCallUserInitOrDstoy; //存放上一次调用了用户定义的初始化函数或销毁函数，为0表示初始化函数，为1表示销毁函数。
    public int m_ReadyExitCnt; //存放准备退出计数，为0表示不准备退出，大于0表示要准备退出。

    public enum MsgTyp
    {
        SetAdoInpt,
        SetAdoInptIsUseSystemAecNsAgc,
        SetAdoInptUseNoAec,
        SetAdoInptUseSpeexAec,
        SetAdoInptUseWebRtcAecm,
        SetAdoInptUseWebRtcAec,
        SetAdoInptUseSpeexWebRtcAec,
        SetAdoInptUseNoNs,
        SetAdoInptUseSpeexPrpocsNs,
        SetAdoInptUseWebRtcNsx,
        SetAdoInptUseWebRtcNs,
        SetAdoInptUseRNNoise,
        SetAdoInptIsUseSpeexPrpocsOther,
        SetAdoInptUsePcm,
        SetAdoInptUseSpeexEncd,
        SetAdoInptUseOpusEncd,
        SetAdoInptIsSaveAdoToWaveFile,
        SetAdoInptIsDrawAdoWavfmToSurface,
        SetAdoInptIsMute,

        SetAdoOtpt,
        AddAdoOtptStrm,
        DelAdoOtptStrm,
        SetAdoOtptStrmUsePcm,
        SetAdoOtptStrmUseSpeexDecd,
        SetAdoOtptStrmUseOpusDecd,
        SetAdoOtptStrmIsUse,
        SetAdoOtptIsSaveAdoToWaveFile,
        SetAdoOtptIsDrawAdoWavfmToSurface,
        SetAdoOtptUseDvc,
        SetAdoOtptIsMute,

        SetVdoInpt,
        SetVdoInptUseYU12,
        SetVdoInptUseOpenH264Encd,
        SetVdoInptUseSystemH264Encd,
        SetVdoInptUseDvc,
        SetVdoInptIsBlack,

        AddVdoOtptStrm,
        DelVdoOtptStrm,
        SetVdoOtptStrm,
        SetVdoOtptStrmUseYU12,
        SetVdoOtptStrmUseOpenH264Decd,
        SetVdoOtptStrmUseSystemH264Decd,
        SetVdoOtptStrmIsBlack,
        SetVdoOtptStrmIsUse,

        SetIsUseAdoVdoInptOtpt,

        SetIsUseWakeLock,
        SetIsSaveAdoVdoInptOtptToAviFile,
        SaveStngToFile,

        RqirExit,

        UserInit,
        UserDstoy,
        UserMsg,

        AdoVdoInptOtptInit,
        AdoVdoInptOtptDstoy,
    }
    public class MediaMsg
    {
        MsgTyp m_MsgTyp;
        LinkedList< Object > m_MsgArgLnkLstPt;

        MediaMsg( int AddFirstOrLast, MsgTyp MsgTyp, Object... MsgArgPt )
        {
            m_MsgTyp = MsgTyp;
            if( MsgArgPt.length > 0 )
            {
                m_MsgArgLnkLstPt = new LinkedList< Object >();
                for( Object OneMsgArg : MsgArgPt ) m_MsgArgLnkLstPt.addLast( OneMsgArg );
            }
            synchronized( m_MediaMsgLnkLstPt )
            {
                if( AddFirstOrLast == 0 ) m_MediaMsgLnkLstPt.addFirst( this );
                else m_MediaMsgLnkLstPt.addLast( this );
            }
        }
    }
    public final LinkedList< MediaMsg > m_MediaMsgLnkLstPt; //存放媒体消息链表的指针。

    public static Activity m_MainActivityPt; //存放主界面的指针。

    public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
    public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
    public Activity m_ShowToastActivityPt; //存放显示Toast界面的指针。

    int m_IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。
    PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁的指针。
    PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

    AviFileWriter m_AdoVdoInptOtptAviFileWriterPt; //存放音视频输入输出Avi文件写入器的指针。
    String m_AdoVdoInptOtptAviFileFullPathStrPt; //存放音视频输入输出Avi文件完整路径字符串的指针。
    long m_AdoVdoInptOtptAviFileWrBufSzByt; //存放音视频输入输出Avi文件写入缓冲区的大小，单位为字节。
    int m_AdoVdoInptOtptAviFileIsSaveAdoInpt; //存放音视频输入输出Avi文件是否保存音频输入，为非0表示要保存，为0表示不保存。
    int m_AdoVdoInptOtptAviFileIsSaveAdoOtpt; //存放音视频输入输出Avi文件是否保存音频输出，为非0表示要保存，为0表示不保存。
    int m_AdoVdoInptOtptAviFileIsSaveVdoInpt; //存放音视频输入输出Avi文件是否保存视频输入，为非0表示要保存，为0表示不保存。
    int m_AdoVdoInptOtptAviFileIsSaveVdoOtpt; //存放音视频输入输出Avi文件是否保存视频输出，为非0表示要保存，为0表示不保存。
    int m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx; //存放音视频输入输出Avi文件Pcm格式音频输入原始流的索引。
    int m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx; //存放音视频输入输出Avi文件Pcm格式音频输入结果流的索引。
    int m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset; //存放音视频输入输出Avi文件音频输入流时间戳是否重置，为非0表示要重置，为0表示不重置。
    int m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx; //存放音视频输入输出Avi文件Pcm格式音频输出原始流的索引。
    int m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset; //存放音视频输入输出Avi文件音频输出流时间戳是否重置，为非0表示要重置，为0表示不重置。
    long m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec; //存放音视频输入输出Avi文件音频输入输出流的当前时间戳，单位为毫秒。
    int m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx; //存放音视频输入输出Avi文件已编码格式视频输入结果流的索引。
    HashMap< Integer, Integer > m_AdoVdoInptOtptAviFileEncdVdoOtptSrcStrmIdxMapPt; //存放音视频输入输出Avi文件已编码格式视频输出原始流的索引映射的指针。

    public AdoInpt m_AdoInptPt; //存放音频输入的指针。
    public AdoOtpt m_AdoOtptPt; //存放音频输出的指针。
    public VdoInpt m_VdoInptPt; //存放视频输入的指针。
    public VdoOtpt m_VdoOtptPt; //存放视频输出的指针。

    //媒体处理线程的临时变量。
    short m_PcmAdoInptSrcFrmPt[]; //存放Pcm格式音频输入原始帧的指针。
    short m_PcmAdoInptRsltFrmPt[]; //存放Pcm格式音频输入结果帧的指针。
    short m_PcmAdoInptTmpFrmPt[]; //存放Pcm格式音频输入临时帧的指针。
    short m_PcmAdoOtptSrcFrmPt[]; //存放Pcm格式音频输出原始帧的指针。
    HTInt m_PcmAdoInptRsltFrmVoiceActStsPt; //存放Pcm格式音频输入原始帧语音活动状态的指针，为非0表示有语音活动，为0表示无语音活动。
    byte m_EncdAdoInptRsltFrmPt[]; //存放已编码格式音频输入结果帧的指针，大小为 m_AdoInptPt.m_FrmLenByt 字节。
    HTLong m_EncdAdoInptRsltFrmLenBytPt; //存放已编码格式音频输入结果帧长度的的指针，单位为字节。
    HTInt m_EncdAdoInptRsltFrmIsNeedTransPt; //存放已编码格式音频输入结果帧是否需要传输的指针，为非0表示需要传输，为0表示不要传输。
    VdoInpt.VdoInptFrm m_VdoInptFrmPt; //存放视频输入帧的指针。
    VdoOtpt.VdoOtptFrm m_VdoOtptFrmPt; //存放视频输出帧的指针。

    public Vstr m_ErrInfoVstrPt; //存放错误信息动态字符串的指针。

    //用户定义的相关回调函数。

    //用户定义的初始化函数。
    public abstract int UserInit();

    //用户定义的处理函数。
    public abstract int UserPocs();

    //用户定义的销毁函数。
    public abstract void UserDstoy();

    //用户定义的消息函数。
    public abstract int UserMsg( Object MsgArgPt[] );

    //用户定义的读取音视频输入帧函数。
    public abstract void UserReadAdoVdoInptFrm( short PcmAdoInptSrcFrmPt[], short PcmAdoInptRsltFrmPt[], int PcmAdoInptFrmLenUnit, int PcmAdoInptRsltFrmVoiceActSts,
                                                byte EncdAdoInptRsltFrmPt[], long EncdAdoInptRsltFrmLenByt, int EncdAdoInptRsltFrmIsNeedTrans,
                                                byte NV21VdoInptSrcFrmPt[], int NV21VdoInptSrcFrmWidthPt, int NV21VdoInptSrcFrmHeightPt, long NV21VdoInptSrcFrmLenByt,
                                                byte YU12VdoInptRsltFrmPt[], int YU12VdoInptRsltFrmWidth, int YU12VdoInptRsltFrmHeight, long YU12VdoInptRsltFrmLenByt,
                                                byte EncdVdoInptRsltFrmPt[], long EncdVdoInptRsltFrmLenByt );

    //用户定义的写入音频输出帧函数。
    public abstract void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
                                              short PcmAdoOtptSrcFrmPt[], int PcmAdoOtptFrmLenUnit,
                                              byte EncdAdoOtptSrcFrmPt[], long EncdAdoOtptSrcFrmSzByt, HTLong EncdAdoOtptSrcFrmLenBytPt );

    //用户定义的获取Pcm格式音频输出帧函数。
    public abstract void UserGetAdoOtptFrm( int AdoOtptStrmIdx,
                                            short PcmAdoOtptSrcFrmPt[], long PcmAdoOtptFrmLenUnit,
                                            byte EncdAdoOtptSrcFrmPt[], long EncdAdoOtptSrcFrmLenByt );

    //用户定义的写入视频输出帧函数。
    public abstract void UserWriteVdoOtptFrm( int VdoOtptStrmIdx,
                                              byte YU12VdoOtptSrcFrmPt[], HTInt YU12VdoOtptSrcFrmWidthPt, HTInt YU12VdoOtptSrcFrmHeightPt,
                                              byte EncdVdoOtptSrcFrmPt[], long EncdVdoOtptSrcFrmSzByt, HTLong EncdVdoOtptSrcFrmLenBytPt );

    //用户定义的获取YU12格式视频输出帧函数。
    public abstract void UserGetYU12VdoOtptFrm( int VdoOtptStrmIdx,
                                                byte YU12VdoOtptSrcFrmPt[], int YU12VdoOtptSrcFrmWidth, int YU12VdoOtptSrcFrmHeight,
                                                byte EncdVdoOtptSrcFrmPt[], long EncdVdoOtptSrcFrmLenByt );

    //构造函数。
    public MediaPocsThrd( Activity MainActivityPt )
    {
        m_LastCallUserInitOrDstoy = 1; //设置上一次调用了用户定义的销毁函数。
        m_ReadyExitCnt = 1; //设置准备退出计数为1，当第一次处理调用用户定义的初始化函数消息时会递减。

        m_MediaMsgLnkLstPt = new LinkedList< MediaMsg >(); //初始化媒体消息链表。

        m_MainActivityPt = MainActivityPt; //设置主界面的指针。

        //初始化音频输入。
        m_AdoInptPt = new AdoInpt();
        m_AdoInptPt.m_MediaPocsThrdPt = this;
        SetAdoInpt( 8000, 20 );

        //初始化音频输出。
        m_AdoOtptPt = new AdoOtpt();
        m_AdoOtptPt.m_MediaPocsThrdPt = this;
        m_AdoOtptPt.m_AdoOtptStrmLnkLstPt = new LinkedList< AdoOtpt.AdoOtptStrm >();
        SetAdoOtpt( 8000, 20 );

        //初始化视频输入。
        m_VdoInptPt = new VdoInpt();
        m_VdoInptPt.m_MediaPocsThrdPt = this;
        SetVdoInpt( 15, 480, 640, 0, null );
        SetVdoInptUseDvc( 0, -1, -1 );

        //初始化视频输出。
        m_VdoOtptPt = new VdoOtpt();
        m_VdoOtptPt.m_MediaPocsThrdPt = this;
        m_VdoOtptPt.m_VdoOtptStrmLnkLstPt = new LinkedList< VdoOtpt.VdoOtptStrm >();

        //初始化错误信息动态字符串。
        m_ErrInfoVstrPt = new Vstr();
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

    //设置音频输入。
    public void SetAdoInpt( int SmplRate, int FrmLenMsec )
    {
        if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
            ( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.SetAdoInpt, SmplRate, FrmLenMsec );
    }

    //设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
    public void SetAdoInptIsUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptIsUseSystemAecNsAgc, IsUseSystemAecNsAgc );
    }

    //设置音频输入不使用声学回音消除器。
    public void SetAdoInptUseNoAec()
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseNoAec );
    }

    //设置音频输入要使用Speex声学回音消除器。
    public void SetAdoInptUseSpeexAec( int FilterLen, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseSpeexAec, FilterLen, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, IsSaveMemFile, MemFileFullPathStrPt );
    }

    //设置音频输入要使用WebRtc定点版声学回音消除器。
    public void SetAdoInptUseWebRtcAecm( int IsUseCNGMode, int EchoMode, int Delay )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseWebRtcAecm, IsUseCNGMode, EchoMode, Delay );
    }

    //设置音频输入要使用WebRtc浮点版声学回音消除器。
    public void SetAdoInptUseWebRtcAec( int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseWebRtcAec, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, IsSaveMemFile, MemFileFullPathStrPt );
    }

    //设置音频输入要使用SpeexWebRtc三重声学回音消除器。
    public void SetAdoInptUseSpeexWebRtcAec( int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMutp, float SpeexAecEchoCntu, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseSpeexWebRtcAec, WorkMode, SpeexAecFilterLen, SpeexAecIsUseRec, SpeexAecEchoMutp, SpeexAecEchoCntu, SpeexAecEchoSupes, SpeexAecEchoSupesAct, WebRtcAecmIsUseCNGMode, WebRtcAecmEchoMode, WebRtcAecmDelay, WebRtcAecEchoMode, WebRtcAecDelay, WebRtcAecIsUseDelayAgstcMode, WebRtcAecIsUseExtdFilterMode, WebRtcAecIsUseRefinedFilterAdaptAecMode, WebRtcAecIsUseAdaptAdjDelay, IsUseSameRoomAec, SameRoomEchoMinDelay );
    }

    //设置音频输入不使用噪音抑制器。
    public void SetAdoInptUseNoNs()
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseNoNs );
    }

    //设置音频输入要使用Speex预处理器的噪音抑制。
    public void SetAdoInptUseSpeexPrpocsNs( int IsUseNs, int NoiseSupes, int IsUseDereverb )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseSpeexPrpocsNs, IsUseNs, NoiseSupes, IsUseDereverb );
    }

    //设置音频输入要使用WebRtc定点版噪音抑制器。
    public void SetAdoInptUseWebRtcNsx( int PolicyMode )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseWebRtcNsx, PolicyMode );
    }

    //设置音频输入要使用WebRtc浮点版噪音抑制器。
    public void SetAdoInptUseWebRtcNs( int PolicyMode )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseWebRtcNs, PolicyMode );
    }

    //设置音频输入要使用RNNoise噪音抑制器。
    public void SetAdoInptUseRNNoise()
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseRNNoise );
    }

    //设置音频输入是否使用Speex预处理器的其他功能。
    public void SetAdoInptIsUseSpeexPrpocsOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCntu, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptIsUseSpeexPrpocsOther, IsUseOther, IsUseVad, VadProbStart, VadProbCntu, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain );
    }

    //设置音频输入要使用PCM原始数据。
    public void SetAdoInptUsePcm()
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUsePcm );
    }

    //设置音频输入要使用Speex编码器。
    public void SetAdoInptUseSpeexEncd( int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseSpeexEncd, UseCbrOrVbr, Qualt, Cmplxt, PlcExptLossRate );
    }

    //设置音频输入要使用Opus编码器。
    public void SetAdoInptUseOpusEncd()
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptUseOpusEncd );
    }

    //设置音频输入是否保存音频到Wave文件。
    public void SetAdoInptIsSaveAdoToWaveFile( int IsSaveAdoToWaveFile, String AdoInptSrcWaveFileFullPathStrPt, String AdoInptRsltWaveFileFullPathStrPt, long AdoInptFileWrBufSzByt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptIsSaveAdoToWaveFile, IsSaveAdoToWaveFile, AdoInptSrcWaveFileFullPathStrPt, AdoInptRsltWaveFileFullPathStrPt, AdoInptFileWrBufSzByt );
    }

    //设置音频输入是否绘制音频波形到Surface。
    public void SetAdoInptIsDrawAdoWavfmToSurface( int IsDrawAdoWavfmToSurface, SurfaceView AdoInptWavfmSurfacePt, SurfaceView AdoRsltWavfmSurfacePt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptIsDrawAdoWavfmToSurface, IsDrawAdoWavfmToSurface, AdoInptWavfmSurfacePt, AdoRsltWavfmSurfacePt );
    }

    //设置音频输入是否静音。
    public void SetAdoInptIsMute( int IsMute )
    {
        new MediaMsg( 1, MsgTyp.SetAdoInptIsMute, IsMute );
    }

    //设置音频输出。
    public void SetAdoOtpt( int SmplRate, int FrmLenMsec )
    {
        if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
            ( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.SetAdoOtpt, SmplRate, FrmLenMsec );
    }

    //添加音频输出流。
    public void AddAdoOtptStrm( int AdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.AddAdoOtptStrm, AdoOtptStrmIdx );
    }

    //删除音频输出流。
    public void DelAdoOtptStrm( int AdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.DelAdoOtptStrm, AdoOtptStrmIdx );
    }

    //设置音频输出流要使用PCM原始数据。
    public void SetAdoOtptStrmUsePcm( int AdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptStrmUsePcm, AdoOtptStrmIdx );
    }

    //设置音频输出流要使用Speex解码器。
    public void SetAdoOtptStrmUseSpeexDecd( int AdoOtptStrmIdx, int IsUsePrcplEnhsmt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptStrmUseSpeexDecd, AdoOtptStrmIdx, IsUsePrcplEnhsmt );
    }

    //设置音频输出流要使用Opus编码器。
    public void SetAdoOtptStrmUseOpusDecd( int AdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptStrmUseOpusDecd, AdoOtptStrmIdx );
    }

    //设置音频输出流是否使用。
    public void SetAdoOtptStrmIsUse( int AdoOtptStrmIdx, int IsUseAdoOtptStrm )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptStrmIsUse, AdoOtptStrmIdx, IsUseAdoOtptStrm );
    }

    //设置音频输出是否保存音频到Wave文件。
    public void SetAdoOtptIsSaveAdoToWaveFile( int IsSaveAdoToWaveFile, String AdoOtptWaveFileFullPathStrPt, long AdoOtptWaveFileWrBufSzByt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptIsSaveAdoToWaveFile, IsSaveAdoToWaveFile, AdoOtptWaveFileFullPathStrPt, AdoOtptWaveFileWrBufSzByt );
    }

    //设置音频输出是否绘制音频波形到Surface。
    public void SetAdoOtptIsDrawAdoWavfmToSurface( int IsDrawAudioToSurface, SurfaceView AdoOtptSrcWavfmSurfacePt )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptIsDrawAdoWavfmToSurface, IsDrawAudioToSurface, AdoOtptSrcWavfmSurfacePt );
    }

    //设置音频输出使用的设备。
    public void SetAdoOtptUseDvc( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
    {
        if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) ) //如果使用听筒，则不能使用媒体类型音频输出流。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.SetAdoOtptUseDvc, UseSpeakerOrEarpiece, UseVoiceCallOrMusic );
    }

    //设置音频输出是否静音。
    public void SetAdoOtptIsMute( int IsMute )
    {
        new MediaMsg( 1, MsgTyp.SetAdoOtptIsMute, IsMute );
    }

    //设置视频输入。
    public void SetVdoInpt( int MaxSmplRate, int FrmWidth, int FrmHeight, int ScreenRotate, HTSurfaceView VdoInptPrvwSurfaceViewPt )
    {
        if( ( ( MaxSmplRate < 1 ) || ( MaxSmplRate > 60 ) ) || //如果采样频率不正确。
            ( ( FrmWidth <= 0 ) || ( ( FrmWidth & 1 ) != 0 ) ) || //如果帧的宽度不正确。
            ( ( FrmHeight <= 0 ) || ( ( FrmHeight & 1 ) != 0 ) ) || //如果帧的高度不正确。
            ( ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) ) //如果屏幕旋转的角度不正确。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.SetVdoInpt, MaxSmplRate, FrmWidth, FrmHeight, ScreenRotate, VdoInptPrvwSurfaceViewPt );
    }

    //设置视频输入要使用YU12原始数据。
    public void SetVdoInptUseYU12()
    {
        new MediaMsg( 1, MsgTyp.SetVdoInptUseYU12 );
    }

    //设置视频输入要使用OpenH264编码器。
    public void SetVdoInptUseOpenH264Encd( int VdoType, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvl, int Cmplxt )
    {
        new MediaMsg( 1, MsgTyp.SetVdoInptUseOpenH264Encd, VdoType, EncdBitrate, BitrateCtrlMode, IDRFrmIntvl, Cmplxt );
    }

    //设置视频输入要使用系统自带H264编码器。
    public void SetVdoInptUseSystemH264Encd( int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvlTimeSec, int Cmplxt )
    {
        new MediaMsg( 1, MsgTyp.SetVdoInptUseSystemH264Encd, EncdBitrate, BitrateCtrlMode, IDRFrmIntvlTimeSec, Cmplxt );
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

        new MediaMsg( 1, MsgTyp.SetVdoInptUseDvc, UseFrontOrBack, FrontCameraDvcId, BackCameraDvcId );
    }

    //设置视频输入是否黑屏。
    public void SetVdoInptIsBlack( int IsBlack )
    {
        new MediaMsg( 1, MsgTyp.SetVdoInptIsBlack, IsBlack );
    }

    //添加视频输出流。
    public void AddVdoOtptStrm( int VdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.AddVdoOtptStrm, VdoOtptStrmIdx );
    }

    //删除视频输出流。
    public void DelVdoOtptStrm( int VdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.DelVdoOtptStrm, VdoOtptStrmIdx );
    }

    //设置视频输出流。
    public void SetVdoOtptStrm( int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt )
    {
        if( VdoOtptDspySurfaceViewPt == null ) //如果视频显示SurfaceView的指针不正确。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.SetVdoOtptStrm, VdoOtptStrmIdx, VdoOtptDspySurfaceViewPt );
    }

    //设置视频输出流要使用YU12原始数据。
    public void SetVdoOtptStrmUseYU12( int VdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.SetVdoOtptStrmUseYU12, VdoOtptStrmIdx );
    }

    //设置视频输出流要使用OpenH264解码器。
    public void SetVdoOtptStrmUseOpenH264Decd( int VdoOtptStrmIdx, int DecdThrdNum )
    {
        new MediaMsg( 1, MsgTyp.SetVdoOtptStrmUseOpenH264Decd, VdoOtptStrmIdx, DecdThrdNum );
    }

    //设置视频输出流要使用系统自带H264解码器。
    public void SetVdoOtptStrmUseSystemH264Decd( int VdoOtptStrmIdx )
    {
        new MediaMsg( 1, MsgTyp.SetVdoOtptStrmUseSystemH264Decd, VdoOtptStrmIdx );
    }

    //设置视频输出流是否黑屏。
    public void SetVdoOtptStrmIsBlack( int VdoOtptStrmIdx, int IsBlack )
    {
        new MediaMsg( 1, MsgTyp.SetVdoOtptStrmIsBlack, VdoOtptStrmIdx, IsBlack );
    }

    //设置视频输出流是否使用。
    public void SetVdoOtptStrmIsUse( int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
    {
        new MediaMsg( 1, MsgTyp.SetVdoOtptStrmIsUse, VdoOtptStrmIdx, IsUseVdoOtptStrm );
    }

    //设置是否使用音视频输入输出。
    public void SetIsUseAdoVdoInptOtpt( int IsUseAdoInpt, int IsUseAdoOtpt, int IsUseVdoInpt, int IsUseVdoOtpt )
    {
        new MediaMsg( 1, MsgTyp.SetIsUseAdoVdoInptOtpt, IsUseAdoInpt, IsUseAdoOtpt, IsUseVdoInpt, IsUseVdoOtpt );
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
        new MediaMsg( 1, MsgTyp.SetIsUseWakeLock, IsUseWakeLock );
    }

    //设置是否保存音视频输入输出到Avi文件。
    public void SetIsSaveAdoVdoInptOtptToAviFile( String AdoVdoInptOtptAviFileFullPathStrPt, long AdoVdoInptOtptAviFileWrBufSzByt, int IsSaveAdoInpt, int IsSaveAdoOtpt, int IsSaveVdoInpt, int IsSaveVdoOtpt )
    {
        new MediaMsg( 1, MsgTyp.SetIsSaveAdoVdoInptOtptToAviFile, AdoVdoInptOtptAviFileFullPathStrPt, AdoVdoInptOtptAviFileWrBufSzByt, IsSaveAdoInpt, IsSaveAdoOtpt, IsSaveVdoInpt, IsSaveVdoOtpt );
    }

    //保存设置到文件。
    public void SaveStngToFile( String StngFileFullPathStrPt )
    {
        new MediaMsg( 1, MsgTyp.SaveStngToFile, StngFileFullPathStrPt );
    }

    //初始化或销毁唤醒锁。
    private void WakeLockInitOrDstoy( int IsInitWakeLock )
    {
        if( IsInitWakeLock != 0 ) //如果要初始化唤醒锁。
        {
            if( ( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) && ( m_AdoOtptPt.m_UseWhatAdoOtptDvc != 0 ) ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
            {
                if( m_ProximityScreenOffWakeLockPt == null ) //如果接近息屏唤醒锁还没有初始化。
                {
                    m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) m_MainActivityPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
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
                m_FullWakeLockPt = ( ( PowerManager ) m_MainActivityPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
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

    //请求本线程退出。
    public void RqirExit( int ExitFlag, int IsBlockWait )
    {
        if( ( ExitFlag < 0 ) || ( ExitFlag > 3 ) ) //如果退出标记不正确。
        {
            return;
        }

        new MediaMsg( 1, MsgTyp.RqirExit, ExitFlag );
        m_ReadyExitCnt++; //设置准备退出计数递增。

        if( IsBlockWait != 0 ) //如果需要阻塞等待。
        {
            if( ExitFlag == 1 ) //如果是请求退出。
            {
                while( this.isAlive() == true ) //如果媒体处理线程还在运行。
                {
                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                }
            }
        }
    }

    //发送用户消息。
    public void SendUserMsg( Object... MsgArgPt )
    {
        new MediaMsg( 1, MsgTyp.UserMsg, new Object[]{ MsgArgPt } );
    }

    //初始化媒体处理线程的Avi文件写入器。
    private int AviFileWriterInit()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        HTInt p_TmpHTIntPt = new HTInt();

        Out:
        {
            if( ( m_AdoVdoInptOtptAviFileIsSaveAdoInpt != 0 ) || ( m_AdoVdoInptOtptAviFileIsSaveAdoOtpt != 0 ) || ( m_AdoVdoInptOtptAviFileIsSaveVdoInpt != 0 ) || ( m_AdoVdoInptOtptAviFileIsSaveVdoOtpt != 0 ) )
            {
                if( m_AdoVdoInptOtptAviFileWriterPt == null )
                {
                    m_AdoVdoInptOtptAviFileWriterPt = new AviFileWriter();
                    if( m_AdoVdoInptOtptAviFileWriterPt.Init( m_AdoVdoInptOtptAviFileFullPathStrPt, m_AdoVdoInptOtptAviFileWrBufSzByt, 5, m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音视频输入输出Avi文件 " + m_AdoVdoInptOtptAviFileFullPathStrPt + " 的Avi文件写入器成功。" );
                    }
                    else
                    {
                        m_AdoVdoInptOtptAviFileWriterPt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：初始化音视频输入输出Avi文件 " + m_AdoVdoInptOtptAviFileFullPathStrPt + " 的Avi文件写入器失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                        break Out;
                    }
                    long p_AdoVdoStartTimeStamp = SystemClock.uptimeMillis();
                    m_AdoVdoInptOtptAviFileWriterPt.SetStartTimeStamp( p_AdoVdoStartTimeStamp, null );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件时间线的起始时间戳为 " + p_AdoVdoStartTimeStamp + " 。" );
                    m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx = -1;
                    m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx = -1;
                    m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx = -1;
                    m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx = -1;
                    m_AdoVdoInptOtptAviFileEncdVdoOtptSrcStrmIdxMapPt = new HashMap< Integer, Integer >();
                }

                if( ( m_AdoVdoInptOtptAviFileIsSaveAdoInpt != 0 ) && ( m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset != 0 ) ) //如果要保存音频输入，且音频输入流时间戳要重置。
                {
                    if( m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx == -1 )
                    {
                        if( m_AdoVdoInptOtptAviFileWriterPt.AddAdoStrm( 1, m_AdoInptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
                        {
                            m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx = p_TmpHTIntPt.m_Val;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输入原始流成功。索引：" + m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx + "。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输入原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                        if( m_AdoVdoInptOtptAviFileWriterPt.AddAdoStrm( 1, m_AdoInptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
                        {
                            m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx = p_TmpHTIntPt.m_Val;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输入结果流成功。索引：" + m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx + "。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输入结果流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                    }

                    m_AdoVdoInptOtptAviFileWriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx, m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec, null );
                    m_AdoVdoInptOtptAviFileWriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx, m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec, null );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件Pcm格式音频输入原始结果流的当前时间戳为 " + m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec + " 。" );

                    m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset = 0; //设置音频输入流时间戳不重置。
                }

                if( ( m_AdoVdoInptOtptAviFileIsSaveAdoOtpt != 0 ) && ( m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset != 0 ) ) //如果要保存音频输出，且音频输出流时间戳要重置。
                {
                    if( m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx == -1 )
                    {
                        if( m_AdoVdoInptOtptAviFileWriterPt.AddAdoStrm( 1, m_AdoOtptPt.m_SmplRate, 1, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 )
                        {
                            m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx = p_TmpHTIntPt.m_Val;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输出原始流成功。索引：" + m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx + "。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加Pcm格式音频输出原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                    }

                    m_AdoVdoInptOtptAviFileWriterPt.AdoStrmSetCurTimeStamp( m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx, m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec, null );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：设置音视频输入输出Avi文件Pcm格式音频输出原始流的当前时间戳为 " + m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec + " 。" );

                    m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset = 0; //设置音频输出流时间戳不重置。
                }

                if( ( m_AdoVdoInptOtptAviFileIsSaveVdoInpt != 0 ) && ( m_VdoInptPt.m_IsInitVdoInpt != 0 ) ) //如果要保存视频输入，且已初始化视频输入。
                {
                    if( m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx == -1 )
                    {
                        if( m_AdoVdoInptOtptAviFileWriterPt.AddVdoStrm( 875967048/*H264*/, 50, p_TmpHTIntPt, m_ErrInfoVstrPt ) == 0 ) //最大采样频率应该尽量被1000整除。
                        {
                            m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx = p_TmpHTIntPt.m_Val;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加已编码格式视频输入结果流成功。索引：" + m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx + "。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音视频输入输出Avi文件添加已编码格式视频输入结果流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
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

    //销毁媒体处理线程的Avi文件写入器。
    private void AviFileWriterDstoy()
    {
        if( m_AdoVdoInptOtptAviFileWriterPt != null )
        {
            if( m_AdoVdoInptOtptAviFileWriterPt.Dstoy( m_ErrInfoVstrPt ) == 0 )
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音视频输入输出Avi文件写入器成功。" );
            }
            else
            {
                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音视频输入输出Avi文件写入器失败。原因：" + m_ErrInfoVstrPt.GetStr() );
            }
            m_AdoVdoInptOtptAviFileWriterPt = null;
            m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx = -1;
            m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx = -1;
            m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset = 0;
            m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx = -1;
            m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset = 0;
            m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec = 0;
            m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx = -1;
            m_AdoVdoInptOtptAviFileEncdVdoOtptSrcStrmIdxMapPt = null;
        }
    }

    //初始化媒体处理线程的临时变量。
    private void MediaPocsThrdTmpVarInit()
    {
        if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
        {
            m_PcmAdoInptSrcFrmPt = null;
            m_PcmAdoOtptSrcFrmPt = null;
            if( ( m_PcmAdoInptRsltFrmPt == null ) || ( m_PcmAdoInptRsltFrmPt.length != m_AdoInptPt.m_FrmLenData ) )
            {
                m_PcmAdoInptRsltFrmPt = new short[ m_AdoInptPt.m_FrmLenData ];
                m_PcmAdoInptTmpFrmPt = new short[ m_AdoInptPt.m_FrmLenData ];
            }
            if( m_PcmAdoInptRsltFrmVoiceActStsPt == null ) m_PcmAdoInptRsltFrmVoiceActStsPt = new HTInt( 1 ); //语音活动状态预设为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
            else m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val = 1;
            if( m_AdoInptPt.m_UseWhatEncd != 0 )
            {
                if( ( m_EncdAdoInptRsltFrmPt == null ) || ( m_EncdAdoInptRsltFrmPt.length != m_AdoInptPt.m_FrmLenByt ) ) m_EncdAdoInptRsltFrmPt = new byte[ m_AdoInptPt.m_FrmLenByt ];
                if( m_EncdAdoInptRsltFrmLenBytPt == null ) m_EncdAdoInptRsltFrmLenBytPt = new HTLong();
                if( m_EncdAdoInptRsltFrmIsNeedTransPt == null ) m_EncdAdoInptRsltFrmIsNeedTransPt = new HTInt( 1 ); //设置已编码格式音频结果帧是否需要传输为1，为了让在不使用非连续传输的情况下永远都是需要传输。
                else m_EncdAdoInptRsltFrmIsNeedTransPt.m_Val = 1;
            }
            else
            {
                m_EncdAdoInptRsltFrmPt = null;
                m_EncdAdoInptRsltFrmLenBytPt = null;
                m_EncdAdoInptRsltFrmIsNeedTransPt = null;
            }
        }
        else
        {
            m_PcmAdoInptSrcFrmPt = null;
            m_PcmAdoOtptSrcFrmPt = null;
            m_PcmAdoInptRsltFrmPt = null;
            m_PcmAdoInptTmpFrmPt = null;
            m_PcmAdoInptRsltFrmVoiceActStsPt = null;
            m_EncdAdoInptRsltFrmPt = null;
            m_EncdAdoInptRsltFrmLenBytPt = null;
            m_EncdAdoInptRsltFrmIsNeedTransPt = null;
        }
        m_VdoInptFrmPt = null;
        m_VdoOtptFrmPt = null;
        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化媒体处理线程的临时变量成功。" );
    }

    //销毁媒体处理线程的临时变量。
    private void MediaPocsThrdTmpVarDstoy()
    {
        m_PcmAdoInptSrcFrmPt = null;
        m_PcmAdoOtptSrcFrmPt = null;
        m_PcmAdoInptRsltFrmPt = null;
        m_PcmAdoInptTmpFrmPt = null;
        m_PcmAdoInptRsltFrmVoiceActStsPt = null;
        m_EncdAdoInptRsltFrmPt = null;
        m_EncdAdoInptRsltFrmLenBytPt = null;
        m_EncdAdoInptRsltFrmIsNeedTransPt = null;
        m_VdoInptFrmPt = null;
        m_VdoOtptFrmPt = null;
        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁媒体处理线程的临时变量成功。" );
    }

    //初始化音视频输入输出。
    private int AdoVdoInptOtptInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果要使用音频输出。在初始化音频输入前初始化音频输出，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
            {
                if( m_AdoOtptPt.m_IsInitAdoOtpt == 0 ) //如果未初始化音频输出。
                {
                    if( m_AdoOtptPt.Init() != 0 ) break Out;
                    m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件音频输出流时间戳要重置。
                    m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec = SystemClock.uptimeMillis(); //设置音视频输入输出Avi文件音频输入输出流的当前时间戳。在这里设置是为了防止音视频输入输出初始化时堆积大量帧，导致音视频输入输出Avi文件不同步。
                    if( m_AdoInptPt.m_IsUseAdoInpt == 0 ) //如果不使用音频输入。
                    {
                        m_AdoOtptPt.m_IsInitAdoOtpt = 1; //设置已初始化音频输出。
                        m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
                        m_AdoOtptPt.m_AdoOtptThrdIsStart = 1; //设置音频输出线程已开始。
                    } //如果要使用音频输入，就不设置已初始化音频输出，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
                }
                else //如果已初始化音频输出。
                {
                    if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) //如果要使用音频输入。
                    {
                        if( m_AdoInptPt.m_IsInitAdoInpt == 0 ) //如果未初始化音频输入。
                        {
                            m_AdoOtptPt.DvcAndThrdDstoy(); //销毁并初始化音频输出设备和线程，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
                            if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break Out;
                            m_AdoVdoInptOtptAviFileAdoOtptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件音频输出流时间戳要重置。
                            m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec = SystemClock.uptimeMillis(); //设置音视频输入输出Avi文件音频输入输出流的当前时间戳。在这里设置是为了防止音视频输入输出初始化时堆积大量帧，导致音视频输入输出Avi文件不同步。
                        } //如果音频输入已初始化，表示音频输入输出都已初始化，无需再销毁并初始化。
                    }
                }
            }
            else //如果不使用音频输出。
            {
                if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) //如果已初始化音频输出。
                {
                    m_AdoOtptPt.Dstoy();
                    m_AdoOtptPt.m_IsInitAdoOtpt = 0; //设置未初始化音频输出。
                }
            }

            if( m_AdoInptPt.m_IsUseAdoInpt != 0 ) //如果要使用音频输入。
            {
                if( m_AdoInptPt.m_IsInitAdoInpt == 0 ) //如果未初始化音频输入。
                {
                    if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
                    {
                        m_AdoOtptPt.m_IsInitAdoOtpt = 1; //设置音频输出已初始化。在这里设置是因为音频输出实际已初始化，音频输出在等待音频输入线程让音频输出设备开始播放和开始音频输出线程。
                    }
                    m_AdoInptPt.SetIsCanUseAec();
                    if( m_AdoInptPt.Init() != 0 ) break Out; //在音频输出初始化后再初始化音频输入，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
                    m_AdoInptPt.m_IsInitAdoInpt = 1; //设置已初始化音频输入。
                    m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件音频输入流时间戳要重置。
                    m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec = SystemClock.uptimeMillis(); //设置音视频输入输出Avi文件音频输入输出流的当前时间戳。在这里设置是为了防止音视频输入输出初始化时堆积大量帧，导致音视频输入输出Avi文件不同步。
                    MediaPocsThrdTmpVarInit();
                }
                else //如果已初始化音频输入。
                {
                    if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) //如果要使用音频输出。
                    {
                        if( m_AdoOtptPt.m_IsInitAdoOtpt == 0 ) //如果音频输出未初始化。
                        {
                            m_AdoInptPt.DvcAndThrdDstoy(); //销毁并初始化音频输入设备和线程，因为要音频输入线程让音频输出设备开始播放和开始音频输出线程。
                            m_AdoOtptPt.m_IsInitAdoOtpt = 1; //设置音频输出已初始化。在这里设置是因为音频输出实际已初始化，音频输出在等待音频输入线程让音频输出设备开始播放和开始音频输出线程。
                            m_AdoInptPt.SetIsCanUseAec();
                            if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break Out;
                            m_AdoVdoInptOtptAviFileAdoInptStrmTimeStampIsReset = 1; //设置音视频输入输出Avi文件音频输入流时间戳要重置。
                            m_AdoVdoInptOtptAviFileAdoInptOtptStrmCurTimeStampMsec = SystemClock.uptimeMillis(); //设置音视频输入输出Avi文件音频输入输出流的当前时间戳。在这里设置是为了防止音视频输入输出初始化时堆积大量帧，导致音视频输入输出Avi文件不同步。
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
                if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) //如果已初始化音频输入。
                {
                    m_AdoInptPt.Dstoy();
                    m_AdoInptPt.m_IsInitAdoInpt = 0; //设置未初始化音频输入。
                    m_AdoInptPt.SetIsCanUseAec();
                    MediaPocsThrdTmpVarInit();
                }
            }

            if( m_VdoInptPt.m_IsUseVdoInpt != 0 ) //如果要使用视频输入。
            {
                if( m_VdoInptPt.m_IsInitVdoInpt == 0 ) //如果未初始化视频输入。
                {
                    if( m_VdoInptPt.Init() != 0 ) break Out;
                    m_VdoInptPt.m_IsInitVdoInpt = 1; //设置已初始化视频输入。
                }
            }
            else //如果不使用视频输入。
            {
                if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) //如果已初始化视频输入。
                {
                    m_VdoInptPt.Dstoy();
                    m_VdoInptPt.m_IsInitVdoInpt = 0; //设置未初始化视频输入。
                }
            }

            if( m_VdoOtptPt.m_IsUseVdoOtpt != 0 ) //如果要使用视频输出。
            {
                if( m_VdoOtptPt.m_IsInitVdoOtpt == 0 ) //如果未初始化视频输出。
                {
                    if( m_VdoOtptPt.Init() != 0 ) break Out;
                    m_VdoOtptPt.m_IsInitVdoOtpt = 1; //设置已初始化视频输出。
                }
            }
            else //如果不使用视频输出。
            {
                if( m_VdoOtptPt.m_IsInitVdoOtpt != 0 ) //如果已初始化视频输出。
                {
                    m_VdoOtptPt.Dstoy();
                    m_VdoOtptPt.m_IsInitVdoOtpt = 0; //设置未初始化视频输出。
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //音视频输入输出销毁。
    private void AdoVdoInptOtptDstoy()
    {
        if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) //如果未初始化音频输入。
        {
            m_AdoInptPt.Dstoy();
            m_AdoInptPt.m_IsInitAdoInpt = 0; //设置未初始化音频输入。
            MediaPocsThrdTmpVarInit();
        }

        if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) //如果已初始化音频输出。
        {
            m_AdoOtptPt.Dstoy();
            m_AdoOtptPt.m_IsInitAdoOtpt = 0; //设置未初始化音频输出。
        }

        if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) //如果已初始化视频输入。
        {
            m_VdoInptPt.Dstoy();
            m_VdoInptPt.m_IsInitVdoInpt = 0; //设置未初始化视频输入。
        }

        if( m_VdoOtptPt.m_IsInitVdoOtpt != 0 ) //如果已初始化视频输出。
        {
            m_VdoOtptPt.Dstoy();
            m_VdoOtptPt.m_IsInitVdoOtpt = 0; //设置未初始化视频输出。
        }
    }

    //本线程执行函数。
    public void run()
    {
        int p_TmpInt321;
        int p_TmpInt322;
        long p_LastTickMsec = 0;
        long p_NowTickMsec = 0;

        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本地代码的指令集名称（CPU类型+ ABI约定）：" + android.os.Build.CPU_ABI + "，设备型号：" + android.os.Build.MODEL + "，主界面：" + m_MainActivityPt + "。" );

        m_RunFlag = RunFlag.Run; //设置本线程运行标记为正在运行。
        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：开始准备音视频输入输出帧处理。" );

        RqirExit( 2, 0 ); //请求重启，相当于调用用户定义的初始化函数和音视频输入输出初始化函数。

        //媒体处理循环开始。
        while( true )
        {
            int p_MediaPocsRslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
            MediaMsg p_MediaMsgPt;

            OutMediaPocs:
            {
                if( m_MediaMsgLnkLstPt.size() > 0 ) //如果有媒体消息需要处理。
                {
                    synchronized( m_MediaMsgLnkLstPt )
                    {
                        p_MediaMsgPt = m_MediaMsgLnkLstPt.getFirst();
                        m_MediaMsgLnkLstPt.removeFirst();
                    }
                    switch( p_MediaMsgPt.m_MsgTyp )
                    {
                        case SetAdoInpt:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.Dstoy();
                                if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
                            }

                            m_AdoInptPt.m_SmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_FrmLenMsec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_FrmLenUnit = m_AdoInptPt.m_FrmLenMsec * m_AdoInptPt.m_SmplRate / 1000;
                            m_AdoInptPt.m_FrmLenData = m_AdoInptPt.m_FrmLenUnit * 1;
                            m_AdoInptPt.m_FrmLenByt = m_AdoInptPt.m_FrmLenData * 2;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                                if( m_AdoInptPt.Init() != 0 ) break OutMediaPocs;
                                MediaPocsThrdTmpVarInit();
                            }
                            break;
                        }
                        case SetAdoInptIsUseSystemAecNsAgc:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.DvcAndThrdDstoy();
                                if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
                            }

                            m_AdoInptPt.m_IsUseSystemAecNsAgc = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptUseNoAec:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

                            m_AdoInptPt.m_UseWhatAec = 0;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.AecInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                            }
                            break;
                        }
                        case SetAdoInptUseSpeexAec:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

                            m_AdoInptPt.m_UseWhatAec = 1;
                            m_AdoInptPt.m_SpeexAecFilterLen = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_SpeexAecIsUseRec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_SpeexAecEchoMutp = ( Float ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_SpeexAecEchoCntu = ( Float ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_AdoInptPt.m_SpeexAecEchoSupes = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );
                            m_AdoInptPt.m_SpeexAecEchoSupesAct = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 5 );
                            m_AdoInptPt.m_SpeexAecIsSaveMemFile = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 6 );
                            m_AdoInptPt.m_SpeexAecMemFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 7 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.AecInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                            }
                            break;
                        }
                        case SetAdoInptUseWebRtcAecm:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

                            m_AdoInptPt.m_UseWhatAec = 2;
                            m_AdoInptPt.m_WebRtcAecmIsUseCNGMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_WebRtcAecmEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_WebRtcAecmDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.AecInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                            }
                            break;
                        }
                        case SetAdoInptUseWebRtcAec:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

                            m_AdoInptPt.m_UseWhatAec = 3;
                            m_AdoInptPt.m_WebRtcAecEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_WebRtcAecDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_WebRtcAecIsUseDelayAgstcMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_WebRtcAecIsUseExtdFilterMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_AdoInptPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );
                            m_AdoInptPt.m_WebRtcAecIsUseAdaptAdjDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 5 );
                            m_AdoInptPt.m_WebRtcAecIsSaveMemFile = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 6 );
                            m_AdoInptPt.m_WebRtcAecMemFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 7 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.AecInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                            }
                            break;
                        }
                        case SetAdoInptUseSpeexWebRtcAec:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.AecDstoy();

                            m_AdoInptPt.m_UseWhatAec = 4;
                            m_AdoInptPt.m_SpeexWebRtcAecWorkMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecFilterLen = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecIsUseRec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMutp = ( Float ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCntu = ( Float ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupes = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 5 );
                            m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 6 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 7 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 8 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecmDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 9 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 10 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 11 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgstcMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 12 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 13 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 14 );
                            m_AdoInptPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 15 );
                            m_AdoInptPt.m_SpeexWebRtcAecIsUseSameRoomAec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 16 );
                            m_AdoInptPt.m_SpeexWebRtcAecSameRoomEchoMinDelay = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 17 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.AecInit() != 0 ) break OutMediaPocs;
                                m_AdoInptPt.SetIsCanUseAec();
                            }
                            break;
                        }
                        case SetAdoInptUseNoNs:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.NsDstoy();
                                m_AdoInptPt.SpeexPrpocsDstoy();
                            }

                            m_AdoInptPt.m_UseWhatNs = 0;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.NsInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptUseSpeexPrpocsNs:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.NsDstoy();
                                m_AdoInptPt.SpeexPrpocsDstoy();
                            }

                            m_AdoInptPt.m_UseWhatNs = 1;
                            m_AdoInptPt.m_SpeexPrpocsIsUseNs = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_SpeexPrpocsNoiseSupes = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_SpeexPrpocsIsUseDereverb = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.NsInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptUseWebRtcNsx:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.NsDstoy();
                                m_AdoInptPt.SpeexPrpocsDstoy();
                            }

                            m_AdoInptPt.m_UseWhatNs = 2;
                            m_AdoInptPt.m_WebRtcNsxPolicyMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.NsInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptUseWebRtcNs:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.NsDstoy();
                                m_AdoInptPt.SpeexPrpocsDstoy();
                            }

                            m_AdoInptPt.m_UseWhatNs = 3;
                            m_AdoInptPt.m_WebRtcNsPolicyMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.NsInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptUseRNNoise:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                m_AdoInptPt.NsDstoy();
                                m_AdoInptPt.SpeexPrpocsDstoy();
                            }

                            m_AdoInptPt.m_UseWhatNs = 4;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.NsInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case SetAdoInptIsUseSpeexPrpocsOther:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.SpeexPrpocsDstoy();

                            m_AdoInptPt.m_IsUseSpeexPrpocsOther = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_SpeexPrpocsIsUseVad = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_SpeexPrpocsVadProbStart = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_SpeexPrpocsVadProbCntu = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_AdoInptPt.m_SpeexPrpocsIsUseAgc = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );
                            m_AdoInptPt.m_SpeexPrpocsAgcLevel = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 5 );
                            m_AdoInptPt.m_SpeexPrpocsAgcIncrement = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 6 );
                            m_AdoInptPt.m_SpeexPrpocsAgcDecrement = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 7 );
                            m_AdoInptPt.m_SpeexPrpocsAgcMaxGain = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 8 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                            {
                                if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break OutMediaPocs;
                                m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val = 1;
                            }
                            break;
                        }
                        case SetAdoInptUsePcm:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

                            m_AdoInptPt.m_UseWhatEncd = 0;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break OutMediaPocs;
                            MediaPocsThrdTmpVarInit();
                            break;
                        }
                        case SetAdoInptUseSpeexEncd:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

                            m_AdoInptPt.m_UseWhatEncd = 1;
                            m_AdoInptPt.m_SpeexEncdUseCbrOrVbr = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_SpeexEncdQualt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_SpeexEncdCmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_SpeexEncdPlcExptLossRate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break OutMediaPocs;
                            MediaPocsThrdTmpVarInit();
                            break;
                        }
                        case SetAdoInptUseOpusEncd:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.EncdDstoy();

                            m_AdoInptPt.m_UseWhatEncd = 2;

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break OutMediaPocs;
                            MediaPocsThrdTmpVarInit();
                            break;
                        }
                        case SetAdoInptIsSaveAdoToWaveFile:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.WaveFileWriterDstoy();

                            m_AdoInptPt.m_IsSaveAdoToWaveFile = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_AdoInptSrcWaveFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_AdoInptRsltWaveFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoInptPt.m_AdoInptFileWrBufSzByt = ( long ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) if( m_AdoInptPt.WaveFileWriterInit() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetAdoInptIsDrawAdoWavfmToSurface:
                        {
                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.WavfmDstoy();

                            m_AdoInptPt.m_IsDrawAdoWavfmToSurface = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoInptPt.m_AdoInptSrcWavfmSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoInptPt.m_AdoRsltWavfmSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );

                            if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) if( m_AdoInptPt.WavfmInit() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetAdoInptIsMute:
                        {
                            m_AdoInptPt.m_AdoInptIsMute = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            break;
                        }
                        case SetAdoOtpt:
                        {
                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 )
                            {
                                if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
                                m_AdoOtptPt.Dstoy();
                            }

                            m_AdoOtptPt.m_SmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoOtptPt.m_FrmLenMsec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoOtptPt.m_FrmLenUnit = m_AdoOtptPt.m_FrmLenMsec * m_AdoOtptPt.m_SmplRate / 1000;
                            m_AdoOtptPt.m_FrmLenData = m_AdoOtptPt.m_FrmLenUnit * 1;
                            m_AdoOtptPt.m_FrmLenByt = m_AdoOtptPt.m_FrmLenData * 2;

                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 )
                            {
                                if( m_AdoOtptPt.Init() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                                {
                                    m_AdoInptPt.SetIsCanUseAec();
                                    if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                                }
                                else
                                {
                                    m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
                                    m_AdoOtptPt.m_AdoOtptThrdIsStart = 1; //设置音频输出线程已开始。
                                }
                            }
                            break;
                        }
                        case AddAdoOtptStrm:
                        {
                            m_AdoOtptPt.AddAdoOtptStrm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case DelAdoOtptStrm:
                        {
                            m_AdoOtptPt.DelAdoOtptStrm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetAdoOtptStrmUsePcm:
                        {
                            m_AdoOtptPt.SetAdoOtptStrmUsePcm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetAdoOtptStrmUseSpeexDecd:
                        {
                            m_AdoOtptPt.SetAdoOtptStrmUseSpeexDecd( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetAdoOtptStrmUseOpusDecd:
                        {
                            m_AdoOtptPt.SetAdoOtptStrmUseOpusDecd( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetAdoOtptStrmIsUse:
                        {
                            m_AdoOtptPt.SetAdoOtptStrmIsUse( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetAdoOtptIsSaveAdoToWaveFile:
                        {
                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) m_AdoOtptPt.WaveFileWriterDstoy();

                            m_AdoOtptPt.m_IsSaveAdoToWaveFile = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoOtptPt.m_AdoOtptWaveFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoOtptPt.m_AdoOtptWaveFileWrBufSzByt = ( long ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );

                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) if( m_AdoOtptPt.WaveFileWriterInit() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetAdoOtptIsDrawAdoWavfmToSurface:
                        {
                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) m_AdoOtptPt.WavfmDstoy();

                            m_AdoOtptPt.m_IsDrawAdoWavfmToSurface = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoOtptPt.m_AdoOtptSrcWavfmSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );

                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 ) if( m_AdoOtptPt.WavfmInit() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetAdoOtptUseDvc:
                        {
                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 )
                            {
                                if( m_AdoInptPt.m_IsInitAdoInpt != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
                                m_AdoOtptPt.DvcAndThrdDstoy();
                            }

                            m_AdoOtptPt.m_UseWhatAdoOtptDvc = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoOtptPt.m_UseWhatAdoOtptStreamType = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。

                            if( m_AdoOtptPt.m_IsInitAdoOtpt != 0 )
                            {
                                if( m_AdoOtptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                                if( m_AdoInptPt.m_IsInitAdoInpt != 0 )
                                {
                                    if( m_AdoInptPt.DvcAndThrdInit() != 0 ) break OutMediaPocs;
                                }
                                else
                                {
                                    m_AdoOtptPt.m_AdoOtptDvcPt.play(); //让音频输出设备开始播放。
                                    m_AdoOtptPt.m_AdoOtptThrdIsStart = 1; //设置音频输出线程已开始。
                                }
                            }
                            break;
                        }
                        case SetAdoOtptIsMute:
                        {
                            m_AdoOtptPt.m_AdoOtptIsMute = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            break;
                        }
                        case SetVdoInpt:
                        {
                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) m_VdoInptPt.Dstoy();

                            m_VdoInptPt.m_MaxSmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_VdoInptPt.m_FrmWidth = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_VdoInptPt.m_FrmHeight = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_VdoInptPt.m_YU12FrmLenByt = m_VdoInptPt.m_FrmWidth * m_VdoInptPt.m_FrmHeight * 3 / 2;
                            m_VdoInptPt.m_ScreenRotate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_VdoInptPt.m_VdoInptPrvwSurfaceViewPt = ( HTSurfaceView ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );

                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetVdoInptUseYU12:
                        {
                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) m_VdoInptPt.Dstoy();

                            m_VdoInptPt.m_UseWhatEncd = 0;

                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetVdoInptUseOpenH264Encd:
                        {
                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) m_VdoInptPt.Dstoy();

                            m_VdoInptPt.m_UseWhatEncd = 1;
                            m_VdoInptPt.m_OpenH264EncdVdoType = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_VdoInptPt.m_OpenH264EncdEncdBitrate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_VdoInptPt.m_OpenH264EncdBitrateCtrlMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_VdoInptPt.m_OpenH264EncdIDRFrmIntvl = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_VdoInptPt.m_OpenH264EncdCmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );

                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetVdoInptUseSystemH264Encd:
                        {
                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) m_VdoInptPt.Dstoy();

                            m_VdoInptPt.m_UseWhatEncd = 2;
                            m_VdoInptPt.m_SystemH264EncdEncdBitrate = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_VdoInptPt.m_SystemH264EncdBitrateCtrlMode = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_VdoInptPt.m_SystemH264EncdIDRFrmIntvlTimeSec = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_VdoInptPt.m_SystemH264EncdCmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );

                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetVdoInptUseDvc:
                        {
                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) m_VdoInptPt.Dstoy();

                            m_VdoInptPt.m_UseWhatVdoInptDvc = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_VdoInptPt.m_FrontCameraDvcId = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_VdoInptPt.m_BackCameraDvcId = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );

                            if( m_VdoInptPt.m_IsInitVdoInpt != 0 ) if( m_VdoInptPt.Init() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SetVdoInptIsBlack:
                        {
                            m_VdoInptPt.m_VdoInptIsBlack = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            break;
                        }
                        case AddVdoOtptStrm:
                        {
                            m_VdoOtptPt.AddVdoOtptStrm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case DelVdoOtptStrm:
                        {
                            m_VdoOtptPt.DelVdoOtptStrm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetVdoOtptStrm:
                        {
                            m_VdoOtptPt.SetVdoOtptStrm( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( HTSurfaceView ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetVdoOtptStrmUseYU12:
                        {
                            m_VdoOtptPt.SetVdoOtptStrmUseYU12( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetVdoOtptStrmUseOpenH264Decd:
                        {
                            m_VdoOtptPt.SetVdoOtptStrmUseOpenH264Decd( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetVdoOtptStrmUseSystemH264Decd:
                        {
                            m_VdoOtptPt.SetVdoOtptStrmUseSystemH264Decd( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            break;
                        }
                        case SetVdoOtptStrmIsBlack:
                        {
                            m_VdoOtptPt.SetVdoOtptStrmIsBlack( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetVdoOtptStrmIsUse:
                        {
                            m_VdoOtptPt.SetVdoOtptStrmIsUse( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ), ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 ) );
                            break;
                        }
                        case SetIsUseAdoVdoInptOtpt:
                        {
                            int p_IsUseAdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            int p_IsUseAdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            int p_IsUseVdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            int p_IsUseVdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );

                            if( p_IsUseAdoInpt >= 0 ) m_AdoInptPt.m_IsUseAdoInpt = p_IsUseAdoInpt;
                            if( p_IsUseAdoOtpt >= 0 ) m_AdoOtptPt.m_IsUseAdoOtpt = p_IsUseAdoOtpt;
                            if( p_IsUseVdoInpt >= 0 ) m_VdoInptPt.m_IsUseVdoInpt = p_IsUseVdoInpt;
                            if( p_IsUseVdoOtpt >= 0 ) m_VdoOtptPt.m_IsUseVdoOtpt = p_IsUseVdoOtpt;

                            new MediaMsg( 0, MsgTyp.AdoVdoInptOtptInit );
                            WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
                            break;
                        }
                        case SetIsUseWakeLock:
                        {
                            m_IsUseWakeLock = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
                            break;
                        }
                        case SetIsSaveAdoVdoInptOtptToAviFile:
                        {
                            //AviFileWriterDstoy(); //这里不用销毁。

                            m_AdoVdoInptOtptAviFileFullPathStrPt = ( String )p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            m_AdoVdoInptOtptAviFileWrBufSzByt = ( Long )p_MediaMsgPt.m_MsgArgLnkLstPt.get( 1 );
                            m_AdoVdoInptOtptAviFileIsSaveAdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 2 );
                            m_AdoVdoInptOtptAviFileIsSaveAdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 3 );
                            m_AdoVdoInptOtptAviFileIsSaveVdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 4 );
                            m_AdoVdoInptOtptAviFileIsSaveVdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 5 );

                            if( AviFileWriterInit() != 0 ) break OutMediaPocs;
                            break;
                        }
                        case SaveStngToFile:
                        {
                            String p_StngFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 );
                            File p_StngFilePt = new File( p_StngFileFullPathStrPt );

                            try
                            {
                                if( !p_StngFilePt.exists() )
                                {
                                    p_StngFilePt.createNewFile();
                                }
                                FileWriter p_StngFileWriterPt = new FileWriter( p_StngFilePt );

                                p_StngFileWriterPt.write( "m_MainActivityPt：" + m_MainActivityPt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_IsPrintLogcat：" + m_IsPrintLogcat + "\n" );
                                p_StngFileWriterPt.write( "m_IsShowToast：" + m_IsShowToast + "\n" );
                                p_StngFileWriterPt.write( "m_ShowToastActivityPt：" + m_ShowToastActivityPt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_IsUseWakeLock：" + m_IsUseWakeLock + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileFullPathStrPt：" + m_AdoVdoInptOtptAviFileFullPathStrPt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileWrBufSzByt：" + m_AdoVdoInptOtptAviFileWrBufSzByt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileIsSaveAdoInpt：" + m_AdoVdoInptOtptAviFileIsSaveAdoInpt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileIsSaveAdoOtpt：" + m_AdoVdoInptOtptAviFileIsSaveAdoOtpt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileIsSaveVdoInpt：" + m_AdoVdoInptOtptAviFileIsSaveVdoInpt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoVdoInptOtptAviFileIsSaveVdoOtpt：" + m_AdoVdoInptOtptAviFileIsSaveVdoOtpt + "\n" );
                                p_StngFileWriterPt.write( "\n" );

                                p_StngFileWriterPt.write( "m_AdoInptPt.m_IsUseAdoInpt：" + m_AdoInptPt.m_IsUseAdoInpt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_IsInitAdoInpt：" + m_AdoInptPt.m_IsInitAdoInpt + "\n" );
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
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecFilterLen：" + m_AdoInptPt.m_SpeexAecFilterLen + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecIsUseRec：" + m_AdoInptPt.m_SpeexAecIsUseRec + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoMutp：" + m_AdoInptPt.m_SpeexAecEchoMutp + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecEchoCntu：" + m_AdoInptPt.m_SpeexAecEchoCntu + "\n" );
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
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMutp：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoMutp + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCntu：" + m_AdoInptPt.m_SpeexWebRtcAecSpeexAecEchoCntu + "\n" );
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
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexPrpocsVadProbCntu：" + m_AdoInptPt.m_SpeexPrpocsVadProbCntu + "\n" );
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
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_IsSaveAdoToWaveFile：" + m_AdoInptPt.m_IsSaveAdoToWaveFile + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptSrcWaveFileFullPathStrPt：" + m_AdoInptPt.m_AdoInptSrcWaveFileFullPathStrPt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptRsltWaveFileFullPathStrPt：" + m_AdoInptPt.m_AdoInptRsltWaveFileFullPathStrPt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptFileWrBufSzByt：" + m_AdoInptPt.m_AdoInptFileWrBufSzByt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_IsDrawAdoWavfmToSurface：" + m_AdoInptPt.m_IsDrawAdoWavfmToSurface + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptSrcWavfmSurfacePt：" + m_AdoInptPt.m_AdoInptSrcWavfmSurfacePt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoRsltWavfmSurfacePt：" + m_AdoInptPt.m_AdoRsltWavfmSurfacePt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptDvcBufSzByt：" + m_AdoInptPt.m_AdoInptDvcBufSzByt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoInptPt.m_AdoInptIsMute：" + m_AdoInptPt.m_AdoInptIsMute + "\n" );
                                p_StngFileWriterPt.write( "\n" );

                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsUseAdoOtpt：" + m_AdoOtptPt.m_IsUseAdoOtpt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsInitAdoOtpt：" + m_AdoOtptPt.m_IsInitAdoOtpt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_SmplRate：" + m_AdoOtptPt.m_SmplRate + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenMsec：" + m_AdoOtptPt.m_FrmLenMsec + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenUnit：" + m_AdoOtptPt.m_FrmLenUnit + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenData：" + m_AdoOtptPt.m_FrmLenData + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_FrmLenByt：" + m_AdoOtptPt.m_FrmLenByt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptStrmLnkLstPt：" + m_AdoOtptPt.m_AdoOtptStrmLnkLstPt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                for( AdoOtpt.AdoOtptStrm p_AdoOtptStrm : m_AdoOtptPt.m_AdoOtptStrmLnkLstPt )
                                {
                                    p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptStrmIdx：" + p_AdoOtptStrm.m_AdoOtptStrmIdx + "\n" );
                                    p_StngFileWriterPt.write( "\n" );
                                    p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatDecd：" + p_AdoOtptStrm.m_UseWhatDecd + "\n" );
                                    p_StngFileWriterPt.write( "\n" );
                                    p_StngFileWriterPt.write( "m_AdoOtptPt.m_SpeexDecdIsUsePrcplEnhsmt：" + p_AdoOtptStrm.m_SpeexDecdIsUsePrcplEnhsmt + "\n" );
                                    p_StngFileWriterPt.write( "\n" );
                                }
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsSaveAdoToWaveFile：" + m_AdoOtptPt.m_IsSaveAdoToWaveFile + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptWaveFileFullPathStrPt：" + m_AdoOtptPt.m_AdoOtptWaveFileFullPathStrPt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptWaveFileWrBufSzByt：" + m_AdoOtptPt.m_AdoOtptWaveFileWrBufSzByt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_IsDrawAdoWavfmToSurface：" + m_AdoOtptPt.m_IsDrawAdoWavfmToSurface + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptSrcWavfmSurfacePt：" + m_AdoOtptPt.m_AdoOtptSrcWavfmSurfacePt + "\n" );
                                p_StngFileWriterPt.write( "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptDvcBufSzByt：" + m_AdoOtptPt.m_AdoOtptDvcBufSzByt + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatAdoOtptDvc：" + m_AdoOtptPt.m_UseWhatAdoOtptDvc + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_UseWhatAdoOtptStreamType：" + m_AdoOtptPt.m_UseWhatAdoOtptStreamType + "\n" );
                                p_StngFileWriterPt.write( "m_AdoOtptPt.m_AdoOtptIsMute：" + m_AdoOtptPt.m_AdoOtptIsMute + "\n" );
                                p_StngFileWriterPt.write( "\n" );

                                p_StngFileWriterPt.write( "m_VdoInptPt.m_IsUseVdoInpt：" + m_VdoInptPt.m_IsUseVdoInpt + "\n" );
                                p_StngFileWriterPt.write( "m_VdoInptPt.m_IsInitVdoInpt：" + m_VdoInptPt.m_IsInitVdoInpt + "\n" );
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
                                p_StngFileWriterPt.write( "m_VdoOtptPt.m_IsInitVdoOtpt：" + m_VdoOtptPt.m_IsInitVdoOtpt + "\n" );
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
                                    p_StngFileWriterPt.write( "m_VdoOtptPt.m_VdoOtptDspySurfaceViewPt：" + p_VdoOtptStrm.m_VdoOtptIsBlack + "\n" );
                                    p_StngFileWriterPt.write( "\n" );
                                }

                                p_StngFileWriterPt.flush();
                                p_StngFileWriterPt.close();
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + p_StngFileFullPathStrPt + " 成功。" );
                            } catch( IOException e )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + p_StngFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
                                break OutMediaPocs;
                            }
                            break;
                        }
                        case RqirExit:
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求，开始准备退出。" );

                            switch( ( Integer ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) )
                            {
                                case 1: //为请求退出。
                                {
                                    //执行顺序：媒体销毁，用户销毁并退出。
                                    synchronized( m_MediaMsgLnkLstPt )
                                    {
                                        if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
                                        {
                                            new MediaMsg( 0, MsgTyp.UserDstoy );
                                            new MediaMsg( 0, MsgTyp.AdoVdoInptOtptDstoy );
                                        }
                                        else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
                                        {
                                            m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                                        }
                                    }
                                    break;
                                }
                                case 2: //请求重启。
                                {
                                    //执行顺序：媒体销毁，用户销毁，用户初始化，媒体初始化。
                                    synchronized( m_MediaMsgLnkLstPt )
                                    {
                                        new MediaMsg( 0, MsgTyp.AdoVdoInptOtptInit );
                                        new MediaMsg( 0, MsgTyp.UserInit );
                                        if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
                                        {
                                            new MediaMsg( 0, MsgTyp.UserDstoy );
                                            new MediaMsg( 0, MsgTyp.AdoVdoInptOtptDstoy );
                                        }
                                        else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
                                        {
                                            m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                                        }
                                    }
                                    break;
                                }
                                case 3: //请求重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。
                                {
                                    //执行顺序：媒体销毁，媒体初始化。
                                    synchronized( m_MediaMsgLnkLstPt )
                                    {
                                        new MediaMsg( 0, MsgTyp.AdoVdoInptOtptInit );
                                        new MediaMsg( 0, MsgTyp.AdoVdoInptOtptDstoy );
                                        m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                        case UserInit:
                        {
                            m_ExitCode = ExitCode.Normal; //清空退出码。
                            m_LastCallUserInitOrDstoy = 0; //设置上一次调用了用户定义的初始化函数。
                            m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                            p_TmpInt321 = UserInit(); //调用用户定义的初始化函数。
                            if( p_TmpInt321 == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt321 );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt321 );
                                break OutMediaPocs;
                            }
                            break;
                        }
                        case UserDstoy:
                        {
                            m_LastCallUserInitOrDstoy = 1;
                            UserDstoy(); //调用用户定义的销毁函数。
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
                            break;
                        }
                        case UserMsg:
                        {
                            p_TmpInt321 = UserMsg( ( Object[] ) p_MediaMsgPt.m_MsgArgLnkLstPt.get( 0 ) );
                            if( p_TmpInt321 == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的消息函数成功。返回值：" + p_TmpInt321 );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的消息函数失败。返回值：" + p_TmpInt321 );
                                break OutMediaPocs;
                            }
                            break;
                        }
                        case AdoVdoInptOtptInit:
                        {
                            if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就初始化音视频输入输出、音视频输入输出Avi文件写入器。
                            {
                                if( AdoVdoInptOtptInit() != 0 ) break OutMediaPocs;
                                if( AviFileWriterInit() != 0 ) break OutMediaPocs;
                            }
                            break;
                        }
                        case AdoVdoInptOtptDstoy:
                        {
                            AviFileWriterDstoy();
                            AdoVdoInptOtptDstoy();
                            break;
                        }
                    }
                }
                else //如果没有媒体消息需要处理。
                {
                    p_MediaMsgPt = null;
                    if( m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis();

                    //调用用户定义的处理函数。
                    {
                        p_TmpInt321 = UserPocs();
                        if( p_TmpInt321 == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt321 );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt321 );
                            break OutMediaPocs;
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                            p_LastTickMsec = SystemClock.uptimeMillis();
                        }
                    }

                    //取出Pcm格式音频输入原始帧和Pcm格式音频输出原始帧。
                    if( m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt != null ) p_TmpInt321 = m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt.size(); //获取Pcm格式音频输入原始帧链表的元素总数。
                    else p_TmpInt321 = 0;
                    if( m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt != null ) p_TmpInt322 = m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt.size(); //获取Pcm格式音频输出原始帧链表的元素总数。
                    else p_TmpInt322 = 0;
                    if( m_AdoInptPt.m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器。
                    {
                        if( ( p_TmpInt321 > 0 ) && ( p_TmpInt322 > 0 ) ) //如果Pcm格式音频输入原始帧链表和Pcm格式音频输出原始帧链表中都有帧了，就开始取出。
                        {
                            //从Pcm格式音频输入原始帧链表中取出第一个帧。
                            synchronized( m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt )
                            {
                                m_PcmAdoInptSrcFrmPt = m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt.getFirst();
                                m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt.removeFirst();
                            }
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从Pcm格式音频输入原始帧链表中取出第一个帧，Pcm格式音频输入原始帧链表元素总数：" + p_TmpInt321 + "。" );

                            //从Pcm格式音频输出原始帧链表中取出第一个帧。
                            synchronized( m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt )
                            {
                                m_PcmAdoOtptSrcFrmPt = m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt.getFirst();
                                m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt.removeFirst();
                            }
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从Pcm格式音频输出原始帧链表中取出第一个帧，Pcm格式音频输出原始帧链表元素总数：" + p_TmpInt322 + "。" );

                            //将Pcm格式音频输入原始帧复制到Pcm格式音频输入结果帧，方便处理。
                            System.arraycopy( m_PcmAdoInptSrcFrmPt, 0, m_PcmAdoInptRsltFrmPt, 0, m_PcmAdoInptSrcFrmPt.length );
                        }
                    }
                    else //如果不可以使用声学回音消除器。
                    {
                        if( p_TmpInt321 > 0 ) //如果Pcm格式音频输入原始帧链表有帧了，就开始取出。
                        {
                            //从Pcm格式音频输入原始帧链表中取出第一个帧。
                            synchronized( m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt )
                            {
                                m_PcmAdoInptSrcFrmPt = m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt.getFirst();
                                m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt.removeFirst();
                            }
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从Pcm格式音频输入原始帧链表中取出第一个帧，Pcm格式音频输入原始帧链表元素总数：" + p_TmpInt321 + "。" );

                            //将Pcm格式音频输入原始帧复制到Pcm格式音频输入结果帧，方便处理。
                            System.arraycopy( m_PcmAdoInptSrcFrmPt, 0, m_PcmAdoInptRsltFrmPt, 0, m_PcmAdoInptSrcFrmPt.length );
                        }

                        if( p_TmpInt322 > 0 ) //如果Pcm格式音频输出原始帧链表有帧了，就开始取出。
                        {
                            //从Pcm格式音频输出原始帧链表中取出第一个帧。
                            synchronized( m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt )
                            {
                                m_PcmAdoOtptSrcFrmPt = m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt.getFirst();
                                m_AdoOtptPt.m_PcmAdoOtptSrcFrmLnkLstPt.removeFirst();
                            }
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从Pcm格式音频输出原始帧链表中取出第一个帧，Pcm格式音频输出原始帧链表元素总数：" + p_TmpInt322 + "。" );
                        }
                    }

                    //处理音频输入帧开始。
                    if( m_PcmAdoInptSrcFrmPt != null )
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
                                    if( ( m_AdoInptPt.m_SpeexAecPt != null ) && ( m_AdoInptPt.m_SpeexAecPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoOtptSrcFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 ) )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器成功。" );
                                        short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                                    }
                                    else
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器失败。" );
                                    }
                                    break;
                                }
                                case 2: //如果要使用WebRtc定点版声学回音消除器。
                                {
                                    if( ( m_AdoInptPt.m_WebRtcAecmPt != null ) && ( m_AdoInptPt.m_WebRtcAecmPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoOtptSrcFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 ) )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器成功。" );
                                        short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                                    }
                                    else
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器失败。" );
                                    }
                                    break;
                                }
                                case 3: //如果要使用WebRtc浮点版声学回音消除器。
                                {
                                    if( ( m_AdoInptPt.m_WebRtcAecPt != null ) && ( m_AdoInptPt.m_WebRtcAecPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoOtptSrcFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 ) )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器成功。" );
                                        short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                                    }
                                    else
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器失败。" );
                                    }
                                    break;
                                }
                                case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                                {
                                    if( ( m_AdoInptPt.m_SpeexWebRtcAecPt != null ) && ( m_AdoInptPt.m_SpeexWebRtcAecPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoOtptSrcFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 ) )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器成功。" );
                                        short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
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
                                if( m_AdoInptPt.m_WebRtcNsxPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器成功。" );
                                    short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 3: //如果要使用WebRtc浮点版噪音抑制器。
                            {
                                if( m_AdoInptPt.m_WebRtcNsPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器成功。" );
                                    short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 4: //如果要使用RNNoise噪音抑制器。
                            {
                                if( m_AdoInptPt.m_RNNoisePt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptTmpFrmPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器成功。" );
                                    short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
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
                            if( m_AdoInptPt.m_SpeexPrpocsPt.Pocs( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptTmpFrmPt, m_PcmAdoInptRsltFrmVoiceActStsPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器成功。语音活动状态：" + m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val );
                                short p_TmpPt[] = m_PcmAdoInptRsltFrmPt;m_PcmAdoInptRsltFrmPt = m_PcmAdoInptTmpFrmPt;m_PcmAdoInptTmpFrmPt = p_TmpPt; //交换Pcm格式音频输入结果帧和Pcm格式音频输入临时帧。
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器失败。" );
                            }
                        }

                        //判断音频输入是否静音。在音频输入处理完后再设置静音，这样可以保证音频输入处理器的连续性。
                        if( m_AdoInptPt.m_AdoInptIsMute != 0 )
                        {
                            Arrays.fill( m_PcmAdoInptRsltFrmPt, ( short ) 0 );
                            if( ( m_AdoInptPt.m_IsUseSpeexPrpocsOther != 0 ) && ( m_AdoInptPt.m_SpeexPrpocsIsUseVad != 0 ) ) //如果Speex预处理器要使用其他功能，且要使用语音活动检测。
                            {
                                m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val = 0;
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
                                if( m_AdoInptPt.m_SpeexEncdPt.Pocs( m_PcmAdoInptRsltFrmPt, m_EncdAdoInptRsltFrmPt, m_EncdAdoInptRsltFrmPt.length, m_EncdAdoInptRsltFrmLenBytPt, m_EncdAdoInptRsltFrmIsNeedTransPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器成功。Speex格式音频输入帧的长度：" + m_EncdAdoInptRsltFrmLenBytPt.m_Val + "，Speex格式音频输入帧是否需要传输：" + m_EncdAdoInptRsltFrmIsNeedTransPt.m_Val );
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

                        //使用音频输入原始波形器绘制音频输入原始波形到Surface、音频输入结果波形器绘制音频输入结果波形到Surface。
                        if( m_AdoInptPt.m_IsDrawAdoWavfmToSurface != 0 )
                        {
                            if( m_AdoInptPt.m_AdoInptSrcWavfmPt.Draw( m_PcmAdoInptSrcFrmPt, m_PcmAdoInptSrcFrmPt.length, m_AdoInptPt.m_AdoInptSrcWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始波形器绘制音频输入原始波形到Surface成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始波形器绘制音频输入原始波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                            if( m_AdoInptPt.m_AdoRsltWavfmPt.Draw( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptRsltFrmPt.length, m_AdoInptPt.m_AdoRsltWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果波形器绘制音频输入结果波形到Surface成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果波形器绘制音频输入结果波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                        }

                        //使用音频输入原始Wave文件写入器写入Pcm格式音频输入原始帧、音频输入结果Wave文件写入器写入Pcm格式音频输入结果帧。
                        if( m_AdoInptPt.m_IsSaveAdoToWaveFile != 0 )
                        {
                            if( m_AdoInptPt.m_AdoInptSrcWaveFileWriterPt.WriteShort( m_PcmAdoInptSrcFrmPt, m_PcmAdoInptSrcFrmPt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始Wave文件写入器写入Pcm格式音频输入原始帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入原始Wave文件写入器写入Pcm格式音频输入原始帧失败。" );
                            }
                            if( m_AdoInptPt.m_AdoInptRsltWaveFileWriterPt.WriteShort( m_PcmAdoInptRsltFrmPt, m_PcmAdoInptRsltFrmPt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果Wave文件写入器写入Pcm格式音频输入结果帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入结果Wave文件写入器写入Pcm格式音频输入结果帧失败。" );
                            }
                        }

                        //使用音视频输入输出Avi文件写入器写入Pcm格式音频输入原始帧、Pcm格式音频输入结果帧。
                        if( m_AdoVdoInptOtptAviFileIsSaveAdoInpt != 0 )
                        {
                            long p_Tick;HTLong p_CurTimeStamp = new HTLong(); p_Tick = SystemClock.uptimeMillis(); m_AdoVdoInptOtptAviFileWriterPt.AdoStrmGetCurTimeStamp( m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件音频输入帧时间戳：" + p_Tick + "  " + p_CurTimeStamp.m_Val + "  " + ( p_Tick - p_CurTimeStamp.m_Val ) );
                            if( m_AdoVdoInptOtptAviFileWriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePcmAdoInptSrcStrmIdx, m_PcmAdoInptSrcFrmPt, m_AdoInptPt.m_FrmLenData, m_AdoInptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输入原始帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输入原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                            if( m_AdoVdoInptOtptAviFileWriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePcmAdoInptRsltStrmIdx, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenData, m_AdoInptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输入结果帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输入结果帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( m_CurClsNameStrPt, "媒体处理线程：本次音频输入帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                            p_LastTickMsec = SystemClock.uptimeMillis();
                        }
                    } //处理音频输入帧结束。

                    //处理音频输出帧开始。
                    if( m_PcmAdoOtptSrcFrmPt != null )
                    {
                        //使用音频输出原始波形器绘制音频输出原始波形到Surface。
                        if( m_AdoOtptPt.m_IsDrawAdoWavfmToSurface != 0 )
                        {
                            if( m_AdoOtptPt.m_AdoOtptSrcWavfmPt.Draw( m_PcmAdoOtptSrcFrmPt, m_PcmAdoOtptSrcFrmPt.length, m_AdoOtptPt.m_AdoOtptSrcWavfmSurfacePt.getHolder().getSurface(), m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始波形器绘制音频输入原始波形到Surface成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始波形器绘制音频输出原始波形到Surface失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                        }

                        //使用音频输出原始Wave文件写入器写入输出帧数据。
                        if( m_AdoOtptPt.m_IsSaveAdoToWaveFile != 0 )
                        {
                            if( m_AdoOtptPt.m_AdoOtptSrcWaveFileWriterPt.WriteShort( m_PcmAdoOtptSrcFrmPt, m_PcmAdoOtptSrcFrmPt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始Wave文件写入器写入音频输出帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出原始Wave文件写入器写入音频输出帧失败。" );
                            }
                        }

                        //使用音视频输入输出Avi文件写入器写入Pcm格式音频输出原始帧。
                        if( m_AdoVdoInptOtptAviFileIsSaveAdoOtpt != 0 )
                        {
                            long p_Tick;HTLong p_CurTimeStamp = new HTLong(); p_Tick = SystemClock.uptimeMillis(); m_AdoVdoInptOtptAviFileWriterPt.AdoStrmGetCurTimeStamp( m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx, p_CurTimeStamp, null ); Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件音频输出帧时间戳：" + p_Tick + "  " + p_CurTimeStamp.m_Val + "  " + ( p_Tick - p_CurTimeStamp.m_Val ) );
                            if( m_AdoVdoInptOtptAviFileWriterPt.AdoStrmWriteShort( m_AdoVdoInptOtptAviFilePcmAdoOtptSrcStrmIdx, m_PcmAdoOtptSrcFrmPt, m_AdoOtptPt.m_FrmLenData, m_AdoOtptPt.m_FrmLenMsec, m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输出原始帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入Pcm格式音频输出原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( m_CurClsNameStrPt, "媒体处理线程：本次音频输出帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                            p_LastTickMsec = SystemClock.uptimeMillis();
                        }
                    } //处理音频输出帧结束。

                    //处理视频输入帧开始。
                    if( m_VdoInptPt.m_VdoInptFrmLnkLstPt != null ) p_TmpInt321 = m_VdoInptPt.m_VdoInptFrmLnkLstPt.size(); //获取视频输入帧链表的元素总数。
                    else p_TmpInt321 = 0;
                    if( ( p_TmpInt321 > 0 ) && //如果视频输入帧链表中有帧了。
                        ( ( m_PcmAdoInptSrcFrmPt != null ) || ( m_AdoInptPt.m_PcmAdoInptSrcFrmLnkLstPt == null ) ) ) //且已经处理了音频输入帧或不使用Pcm格式音频输入原始帧链表。
                    {
                        //从视频输入帧链表中取出第一个帧。
                        synchronized( m_VdoInptPt.m_VdoInptFrmLnkLstPt )
                        {
                            m_VdoInptFrmPt = m_VdoInptPt.m_VdoInptFrmLnkLstPt.getFirst();
                            m_VdoInptPt.m_VdoInptFrmLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输入帧链表中取出第一个帧，视频输入帧链表元素总数：" + p_TmpInt321 + "。" );

                        //使用音视频输入输出Avi文件写入器写入已编码格式视频输入结果帧。
                        if( ( m_AdoVdoInptOtptAviFileIsSaveVdoInpt != 0 ) && ( m_VdoInptFrmPt.m_EncdVdoInptRsltFrmLenBytPt.m_Val != 0 ) )
                        {
                            Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件视频输入帧时间戳：" + m_VdoInptFrmPt.m_TimeStampMsec );
                            if( m_AdoVdoInptOtptAviFileWriterPt.VdoStrmWriteByte( m_AdoVdoInptOtptAviFileEncdVdoInptRsltStrmIdx, m_VdoInptFrmPt.m_TimeStampMsec, m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt, m_VdoInptFrmPt.m_EncdVdoInptRsltFrmLenBytPt.m_Val, m_ErrInfoVstrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入已编码格式视频输入结果帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音视频输入输出Avi文件写入器写入已编码格式视频输入结果帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                            }
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( m_CurClsNameStrPt, "媒体处理线程：本次视频输入帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                            p_LastTickMsec = SystemClock.uptimeMillis();
                        }
                    } //处理视频输入帧结束。

                    //处理视频输出帧开始。
                    if( m_VdoOtptPt.m_VdoOtptFrmLnkLstPt != null ) p_TmpInt321 = m_VdoOtptPt.m_VdoOtptFrmLnkLstPt.size(); //获取视频输出帧链表的元素总数。
                    else p_TmpInt321 = 0;
                    if( p_TmpInt321 > 0 ) //如果视频输出帧链表中有帧了。
                    {
                        //从视频输出帧链表中取出第一个帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输出帧链表中取出第一个帧，视频输出帧链表元素总数：" + m_VdoOtptPt.m_VdoOtptFrmLnkLstPt.size() + "。" );
                        synchronized( m_VdoOtptPt.m_VdoOtptFrmLnkLstPt )
                        {
                            m_VdoOtptFrmPt = m_VdoOtptPt.m_VdoOtptFrmLnkLstPt.getFirst();
                            m_VdoOtptPt.m_VdoOtptFrmLnkLstPt.removeFirst();
                        }

                        //使用音视频输入输出Avi文件写入器写入已编码格式视频输出原始帧。
                        if( ( m_AdoVdoInptOtptAviFileIsSaveVdoOtpt != 0 ) && ( m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val != 0 ) )
                        {
                            Integer p_VdoOtptStrmAviFileIdx;

                            p_VdoOtptStrmAviFileIdx = m_AdoVdoInptOtptAviFileEncdVdoOtptSrcStrmIdxMapPt.get( m_VdoOtptFrmPt.m_VdoOtptStrmIdx );
                            if( p_VdoOtptStrmAviFileIdx == null )
                            {
                                HTInt p_TmpHTInt = new HTInt();
                                if( m_AdoVdoInptOtptAviFileWriterPt.AddVdoStrm( 875967048/*H264*/, 50, p_TmpHTInt, m_ErrInfoVstrPt ) == 0 ) //最大采样频率应该尽量被1000整除。
                                {
                                    p_VdoOtptStrmAviFileIdx = p_TmpHTInt.m_Val;
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptFrmPt.m_VdoOtptStrmIdx + "：音视频输入输出Avi文件添加已编码格式视频输出原始流成功。索引：" + p_VdoOtptStrmAviFileIdx + "。" );
                                    m_AdoVdoInptOtptAviFileEncdVdoOtptSrcStrmIdxMapPt.put( m_VdoOtptFrmPt.m_VdoOtptStrmIdx, p_VdoOtptStrmAviFileIdx );
                                }
							    else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptFrmPt.m_VdoOtptStrmIdx + "：音视频输入输出Avi文件添加已编码格式视频输出原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                                }
                            }

                            if( p_VdoOtptStrmAviFileIdx != -1 )
                            {
                                Log.e( m_CurClsNameStrPt, "音视频输入输出Avi文件视频输出帧时间戳：" + m_VdoOtptFrmPt.m_TimeStampMsec );
                                if( m_AdoVdoInptOtptAviFileWriterPt.VdoStrmWriteByte( p_VdoOtptStrmAviFileIdx, m_VdoOtptFrmPt.m_TimeStampMsec, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmPt, m_VdoOtptFrmPt.m_EncdVdoOtptSrcFrmLenBytPt.m_Val, m_ErrInfoVstrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptFrmPt.m_VdoOtptStrmIdx + "：使用音视频输入输出Avi文件写入器写入已编码格式视频输出原始帧成功。" );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_VdoOtptFrmPt.m_VdoOtptStrmIdx + "：使用音视频输入输出Avi文件写入器写入已编码格式视频输出原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                                }
                            }
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowTickMsec = SystemClock.uptimeMillis();
                            Log.i( m_CurClsNameStrPt, "媒体处理线程：本次视频输出帧处理完毕，耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                            p_LastTickMsec = SystemClock.uptimeMillis();
                        }
                    } //处理视频输出帧结束。

                    //调用用户定义的读取音视频输入帧函数。
                    OutUserReadAdoVdoInptFrmFunc:
                    {
                        if( m_PcmAdoInptSrcFrmPt != null ) //如果有音频输入帧。
                        {
                            if( m_EncdAdoInptRsltFrmPt != null ) //如果有已编码格式音频输入结果帧。
                            {
                                if( m_VdoInptFrmPt != null ) //如果有视频输入帧。
                                {
                                    if( m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt != null ) //如果有已编码格式视频输入结果帧。
                                    {
                                        UserReadAdoVdoInptFrm(
                                                m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                                m_EncdAdoInptRsltFrmPt, m_EncdAdoInptRsltFrmLenBytPt.m_Val, m_EncdAdoInptRsltFrmIsNeedTransPt.m_Val,
                                                m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                                m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
                                                m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt, m_VdoInptFrmPt.m_EncdVdoInptRsltFrmLenBytPt.m_Val );
                                    }
                                    else //如果没有已编码格式视频输入结果帧。
                                    {
                                        UserReadAdoVdoInptFrm(
                                                m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                                m_EncdAdoInptRsltFrmPt, m_EncdAdoInptRsltFrmLenBytPt.m_Val, m_EncdAdoInptRsltFrmIsNeedTransPt.m_Val,
                                                m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                                m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
                                                null, 0 );
                                    }
                                }
                                else //如果没有视频输入帧。
                                {
                                    UserReadAdoVdoInptFrm(
                                            m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                            m_EncdAdoInptRsltFrmPt, m_EncdAdoInptRsltFrmLenBytPt.m_Val, m_EncdAdoInptRsltFrmIsNeedTransPt.m_Val,
                                            null, 0, 0, 0,
                                            null, 0, 0, 0,
                                            null, 0 );
                                }
                            }
                            else //如果没有已编码格式音频输入结果帧。
                            {
                                if( m_VdoInptFrmPt != null ) //如果有视频输入帧。
                                {
                                    if( m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt != null ) //如果有已编码格式视频输入结果帧。
                                    {
                                        UserReadAdoVdoInptFrm(
                                                m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                                null, 0, 0,
                                                m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                                m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
                                                m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt, m_VdoInptFrmPt.m_EncdVdoInptRsltFrmLenBytPt.m_Val );
                                    }
                                    else //如果没有已编码格式视频输入结果帧。
                                    {
                                        UserReadAdoVdoInptFrm(
                                                m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                                null, 0, 0,
                                                m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                                m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
                                                null, 0 );
                                    }
                                }
                                else //如果没有视频输入帧。
                                {
                                    UserReadAdoVdoInptFrm(
                                            m_PcmAdoInptSrcFrmPt, m_PcmAdoInptRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_PcmAdoInptRsltFrmVoiceActStsPt.m_Val,
                                            null, 0, 0,
                                            null, 0, 0, 0,
                                            null, 0, 0, 0,
                                            null, 0 );
                                }
                            }
                        }
                        else //如果没有音频输入帧。
                        {
                            if( m_VdoInptFrmPt != null ) //如果有视频输入帧。
                            {
                                if( m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt != null ) //如果有已编码格式视频输入结果帧。
                                {
                                    UserReadAdoVdoInptFrm(
                                            null, null, 0, 0,
                                            null, 0, 0,
                                            m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                            m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
                                            m_VdoInptFrmPt.m_EncdVdoInptRsltFrmPt, m_VdoInptFrmPt.m_EncdVdoInptRsltFrmLenBytPt.m_Val );
                                }
                                else //如果没有已编码格式视频输入结果帧。
                                {
                                    UserReadAdoVdoInptFrm(
                                            null, null, 0, 0,
                                            null, 0, 0,
                                            m_VdoInptFrmPt.m_NV21VdoInptSrcFrmPt, m_VdoInptPt.m_NV21VdoInptSrcFrmWidth, m_VdoInptPt.m_NV21VdoInptSrcFrmHeight, m_VdoInptPt.m_NV21VdoInptSrcFrmLenByt,
                                            m_VdoInptFrmPt.m_YU12VdoInptRsltFrmPt, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleWidth, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleHeight, m_VdoInptPt.m_YU12VdoInptSrcFrmScaleLenByt,
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

                    if( m_PcmAdoInptSrcFrmPt != null ) //如果取出了Pcm格式音频输入原始帧，就追加到Pcm格式音频输入空闲帧链表。注意：从取出到追加过程中不能跳出，否则会内存泄露。
                    {
                        synchronized( m_AdoInptPt.m_PcmAdoInptIdleFrmLnkLstPt )
                        {
                            m_AdoInptPt.m_PcmAdoInptIdleFrmLnkLstPt.addLast( m_PcmAdoInptSrcFrmPt );
                        }
                        m_PcmAdoInptSrcFrmPt = null;
                    }
                    if( m_PcmAdoOtptSrcFrmPt != null ) //如果取出了Pcm格式音频输出原始帧，就追加到Pcm格式音频输出空闲帧链表。注意：从取出到追加过程中不能跳出，否则会内存泄露。
                    {
                        synchronized( m_AdoOtptPt.m_PcmAdoOtptIdleFrmLnkLstPt )
                        {
                            m_AdoOtptPt.m_PcmAdoOtptIdleFrmLnkLstPt.addLast( m_PcmAdoOtptSrcFrmPt );
                        }
                        m_PcmAdoOtptSrcFrmPt = null;
                    }
                    if( m_VdoInptFrmPt != null ) //如果取出了视频输入帧，就追加到视频输入空闲帧链表。注意：从取出到追加过程中不能跳出，否则会内存泄露。
                    {
                        synchronized( m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt )
                        {
                            m_VdoInptPt.m_VdoInptIdleFrmLnkLstPt.addLast( m_VdoInptFrmPt );
                        }
                        m_VdoInptFrmPt = null;
                    }
                    if( m_VdoOtptFrmPt != null ) //如果取出了视频输出帧，就追加到视频输出空闲帧链表。注意：从取出到追加过程中不能跳出，否则会内存泄露。
                    {
                        synchronized( m_VdoOtptPt.m_VdoOtptIdleFrmLnkLstPt )
                        {
                            m_VdoOtptPt.m_VdoOtptIdleFrmLnkLstPt.addLast( m_VdoOtptFrmPt );
                        }
                        m_VdoOtptFrmPt = null;
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                } //音视频输入输出帧处理结束。

                p_MediaPocsRslt = 0; //设置本媒体消息处理成功。
            }

            if( p_MediaPocsRslt != 0 ) //如果本次媒体处理失败。
            {
                if( p_MediaMsgPt != null ) //如果是媒体消息处理失败。
                {
                    if( p_MediaMsgPt.m_MsgTyp == MsgTyp.UserInit )
                        m_ExitCode = ExitCode.UserInit; //设置退出码为调用用户定义的初始化函数失败。
                    else if( p_MediaMsgPt.m_MsgTyp == MsgTyp.AdoVdoInptOtptInit )
                        m_ExitCode = ExitCode.AdoVdoInptOtptInit; //设置退出码为音视频输入输出初始化失败。
                    else
                        m_ExitCode = ExitCode.MediaMsgPocs; //设置退出码为媒体消息处理失败。
                }
                else m_ExitCode = ExitCode.AdoVdoInptOtptPocs; //设置退出码为音视频输入输出处理失败。
                if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就执行销毁。
                {
                    //执行顺序：媒体销毁，用户销毁并退出。
                    m_ReadyExitCnt++;
                    new MediaMsg( 0, MsgTyp.UserDstoy );
                    new MediaMsg( 0, MsgTyp.AdoVdoInptOtptDstoy );
                }
            }

            if( ( m_MediaMsgLnkLstPt.size() == 0 ) && ( m_ReadyExitCnt > 0 ) ) break; //如果媒体消息处理完毕，且媒体处理线程准备退出。
        } //媒体处理循环结束。

        MediaPocsThrdTmpVarDstoy();

        WakeLockInitOrDstoy( 0 ); //销毁唤醒锁。

        m_RunFlag = RunFlag.Exit; //设置本线程运行标记为已经退出。
        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程已退出。" );
    }
}