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
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import HeavenTao.Data.*;

public abstract class MediaPocsThrd extends Thread //媒体处理线程。
{
    public static String m_CurClsNameStrPt = "MediaPocsThrd"; //存放当前类名称字符串。

    public MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

    class RunFlag //运行标记。
    {
        public static final int Norun = 0; //未开始运行。
        public static final int Run = 1; //正在运行。
        public static final int Exit = 2; //已经退出。
    }
    public int m_RunFlag; //存放本线程运行标记。

    class ExitCode //退出码。
    {
        public static final int Normal = 0; //正常退出。
        public static final int UserInit = 1; //调用用户定义的初始化函数失败。
        public static final int AdoVdoInptOtptInit = 2; //音视频输入输出初始化失败。
        public static final int MediaMsgPocs = 3; //媒体消息处理失败。
        public static final int AdoVdoInptOtptPocs = 4; //音视频输入输出处理失败。
    }
    public int m_ExitCode; //存放退出码。

    public int m_LastCallUserInitOrDstoy; //存放上一次调用了用户定义的初始化函数或销毁函数，为0表示初始化函数，为1表示销毁函数。
    public int m_ReadyExitCnt; //存放准备退出计数，为0表示不准备退出，大于0表示要准备退出。

    public enum MediaMsgTyp
    {
        SetAdoInpt,
        AdoInptSetIsUseSystemAecNsAgc,
        AdoInptSetUseNoAec,
        AdoInptSetUseSpeexAec,
        AdoInptSetUseWebRtcAecm,
        AdoInptSetUseWebRtcAec,
        AdoInptSetUseSpeexWebRtcAec,
        AdoInptSetUseNoNs,
        AdoInptSetUseSpeexPrpocsNs,
        AdoInptSetUseWebRtcNsx,
        AdoInptSetUseWebRtcNs,
        AdoInptSetUseRNNoise,
        AdoInptSetIsUseSpeexPrpocs,
        AdoInptSetUsePcm,
        AdoInptSetUseSpeexEncd,
        AdoInptSetUseOpusEncd,
        AdoInptSetIsSaveAdoToWaveFile,
        AdoInptSetIsDrawAdoWavfmToSurface,
        AdoInptSetIsMute,

        SetAdoOtpt,
        AdoOtptAddStrm,
        AdoOtptDelStrm,
        AdoOtptSetStrmUsePcm,
        AdoOtptSetStrmUseSpeexDecd,
        AdoOtptSetStrmUseOpusDecd,
        AdoOtptSetStrmIsUse,
        AdoOtptSetIsSaveAdoToWaveFile,
        AdoOtptSetIsDrawAdoWavfmToSurface,
        AdoOtptSetUseDvc,
        AdoOtptSetIsMute,

        SetVdoInpt,
        VdoInptSetUseYu12,
        VdoInptSetUseOpenH264Encd,
        VdoInptSetUseSystemH264Encd,
        VdoInptSetUseDvc,
        VdoInptSetIsBlack,

        VdoOtptAddStrm,
        VdoOtptDelStrm,
        VdoOtptSetStrm,
        VdoOtptSetStrmUseYu12,
        VdoOtptSetStrmUseOpenH264Decd,
        VdoOtptSetStrmUseSystemH264Decd,
        VdoOtptSetStrmIsBlack,
        VdoOtptSetStrmIsUse,

        SetIsUseAdoVdoInptOtpt,

        SetIsUseWakeLock,
        SetIsSaveAdoVdoInptOtptToAviFile,
        SaveStsToTxtFile,

        RqirExit,

        UserInit,
        UserDstoy,
        UserMsg,

        AdoVdoInptOtptInit,
        AdoVdoInptOtptDstoy,
    }
    public class MediaMsg
    {
        int m_MediaMsgRslt;
        MediaMsgTyp m_MediaMsgTyp;
        Object[] m_MsgArgCntnrPt;
    }
    public final ConcurrentLinkedDeque< MediaMsg > m_MediaMsgCntnrPt = new ConcurrentLinkedDeque<>(); //存放媒体消息容器的指针。这里忽略报错“Call requires API level 21 (current min is 14): new java.util.concurrent.ConcurrentLinkedDeque”。

    public static Context m_CtxPt; //存放上下文的指针。

    public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
    public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
    public Activity m_ShowToastActPt; //存放显示Toast界面的指针。

    int m_IsUseWakeLock; //存放是否使用唤醒锁，为非0表示要使用，为0表示不使用。
    PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁的指针。
    PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

    class AdoVdoInptOtptAviFile //存放音视频输入输出Avi文件。
    {
        AviFileWriter m_WriterPt; //存放写入器的指针。
        String m_FullPathStrPt; //存放完整路径字符串的指针。
        long m_WrBufSzByt; //存放写入缓冲区的大小，单位为字节。
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

    public AdoInpt m_AdoInptPt = new AdoInpt(); //存放音频输入的指针。
    public AdoOtpt m_AdoOtptPt = new AdoOtpt(); //存放音频输出的指针。
    public VdoInpt m_VdoInptPt = new VdoInpt(); //存放视频输入的指针。
    public VdoOtpt m_VdoOtptPt = new VdoOtpt(); //存放视频输出的指针。

    class Thrd //存放线程。
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
    Thrd m_ThrdPt = new Thrd();

    public Vstr m_ErrInfoVstrPt = new Vstr(); //存放错误信息动态字符串的指针。

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
    public MediaPocsThrd( Context CtxPt )
    {
        m_MediaPocsThrdPt = this; //设置媒体处理线程的指针。

        m_LastCallUserInitOrDstoy = 1; //设置上一次调用了用户定义的销毁函数。
        m_ReadyExitCnt = 1; //设置准备退出计数为1，当第一次处理调用用户定义的初始化函数消息时会递减。

        m_CtxPt = CtxPt; //设置上下文的指针。

        //初始化音频输入。
        m_AdoInptPt.m_MediaPocsThrdPt = this;
        SetAdoInpt( 0, 8000, 20, 0 );

        //初始化音频输出。
        m_AdoOtptPt.m_MediaPocsThrdPt = this;
        m_AdoOtptPt.m_StrmCntnrPt = new ArrayList< AdoOtpt.Strm >();
        SetAdoOtpt( 0, 8000, 20 );

        //初始化视频输入。
        m_VdoInptPt.m_MediaPocsThrdPt = this;
        SetVdoInpt( 0, 15, 480, 640, 0, null );
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

    //发送媒体消息。
    private int SendMediaMsg( int IsBlockWait, int AddFirstOrLast, MediaMsgTyp MediaMsgTyp, Object... MsgArgPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            MediaMsg p_MediaMsgPt = new MediaMsg();

            p_MediaMsgPt.m_MediaMsgRslt = 999;
            p_MediaMsgPt.m_MediaMsgTyp = MediaMsgTyp;
            p_MediaMsgPt.m_MsgArgCntnrPt = MsgArgPt;
            if( AddFirstOrLast == 0 ) m_MediaMsgCntnrPt.addFirst( p_MediaMsgPt );
            else m_MediaMsgCntnrPt.addLast( p_MediaMsgPt );

            if( IsBlockWait != 0 ) //如果要阻塞等待。
            {
                if( Thread.currentThread().getId() != m_MediaPocsThrdPt.getId() ) //如果发送媒体消息线程不是媒体处理线程。
                {
                    do
                    {
                        SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                    } while( ( m_MediaPocsThrdPt.isAlive() ) && ( p_MediaMsgPt.m_MediaMsgRslt == 999 ) );
                }
                else //如果发送媒体消息线程是媒体处理线程。
                {
                    do
                    {
                        OneMediaMsgPocs();
                    } while( p_MediaMsgPt.m_MediaMsgRslt == 999 );
                }
                p_Rslt = p_MediaMsgPt.m_MediaMsgRslt; //返回媒体消息处理结果。
            }
            else //如果不阻塞等待。
            {
                p_Rslt = 0; //返回媒体消息处理结果为成功。因为要让设置函数返回成功。
            }
        }

        return p_Rslt;
    }

    //媒体处理线程的设置音频输入。
    public int SetAdoInpt( int IsBlockWait, int SmplRate, long FrmLenMsec, int m_IsStartRecordingAfterRead )
    {
        if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
            ( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetAdoInpt, SmplRate, FrmLenMsec, m_IsStartRecordingAfterRead );
    }

    //媒体处理线程的音频输入设置设置是否使用系统自带声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
    public int AdoInptSetIsUseSystemAecNsAgc( int IsBlockWait, int IsUseSystemAecNsAgc )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetIsUseSystemAecNsAgc, IsUseSystemAecNsAgc );
    }

    //媒体处理线程的音频输入设置不使用声学回音消除器。
    public int AdoInptSetUseNoAec( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseNoAec );
    }

    //媒体处理线程的音频输入设置要使用Speex声学回音消除器。
    public int AdoInptSetUseSpeexAec( int IsBlockWait, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseSpeexAec, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, IsSaveMemFile, MemFileFullPathStrPt );
    }

    //媒体处理线程的音频输入设置要使用WebRtc定点版声学回音消除器。
    public int AdoInptSetUseWebRtcAecm( int IsBlockWait, int IsUseCNGMode, int EchoMode, int Delay )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseWebRtcAecm, IsUseCNGMode, EchoMode, Delay );
    }

    //媒体处理线程的音频输入设置要使用WebRtc浮点版声学回音消除器。
    public int AdoInptSetUseWebRtcAec( int IsBlockWait, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseWebRtcAec, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, IsSaveMemFile, MemFileFullPathStrPt );
    }

    //媒体处理线程的音频输入设置要使用SpeexWebRtc三重声学回音消除器。
    public int AdoInptSetUseSpeexWebRtcAec( int IsBlockWait, int WorkMode, int SpeexAecFilterLenMsec, int SpeexAecIsUseRec, float SpeexAecEchoMutp, float SpeexAecEchoCntu, int SpeexAecEchoSupes, int SpeexAecEchoSupesAct, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgstcMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseSpeexWebRtcAec, WorkMode, SpeexAecFilterLenMsec, SpeexAecIsUseRec, SpeexAecEchoMutp, SpeexAecEchoCntu, SpeexAecEchoSupes, SpeexAecEchoSupesAct, WebRtcAecmIsUseCNGMode, WebRtcAecmEchoMode, WebRtcAecmDelay, WebRtcAecEchoMode, WebRtcAecDelay, WebRtcAecIsUseDelayAgstcMode, WebRtcAecIsUseExtdFilterMode, WebRtcAecIsUseRefinedFilterAdaptAecMode, WebRtcAecIsUseAdaptAdjDelay, IsUseSameRoomAec, SameRoomEchoMinDelay );
    }

    //媒体处理线程的音频输入设置不使用噪音抑制器。
    public int AdoInptSetUseNoNs( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseNoNs );
    }

    //媒体处理线程的音频输入设置要使用Speex预处理器的噪音抑制。
    public int AdoInptSetUseSpeexPrpocsNs( int IsBlockWait, int IsUseNs, int NoiseSupes, int IsUseDereverb )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseSpeexPrpocsNs, IsUseNs, NoiseSupes, IsUseDereverb );
    }

    //媒体处理线程的音频输入设置要使用WebRtc定点版噪音抑制器。
    public int AdoInptSetUseWebRtcNsx( int IsBlockWait, int PolicyMode )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseWebRtcNsx, PolicyMode );
    }

    //媒体处理线程的音频输入设置要使用WebRtc浮点版噪音抑制器。
    public int AdoInptSetUseWebRtcNs( int IsBlockWait, int PolicyMode )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseWebRtcNs, PolicyMode );
    }

    //媒体处理线程的音频输入设置要使用RNNoise噪音抑制器。
    public int AdoInptSetUseRNNoise( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseRNNoise );
    }

    //媒体处理线程的音频输入设置是否使用Speex预处理器。
    public int AdoInptSetIsUseSpeexPrpocs( int IsBlockWait, int IsUseSpeexPrpocs, int IsUseVad, int VadProbStart, int VadProbCntu, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetIsUseSpeexPrpocs, IsUseSpeexPrpocs, IsUseVad, VadProbStart, VadProbCntu, IsUseAgc, AgcLevel, AgcIncrement, AgcDecrement, AgcMaxGain );
    }

    //媒体处理线程的音频输入设置要使用PCM原始数据。
    public int AdoInptSetUsePcm( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUsePcm );
    }

    //媒体处理线程的音频输入设置要使用Speex编码器。
    public int AdoInptSetUseSpeexEncd( int IsBlockWait, int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseSpeexEncd, UseCbrOrVbr, Qualt, Cmplxt, PlcExptLossRate );
    }

    //媒体处理线程的音频输入设置要使用Opus编码器。
    public int AdoInptSetUseOpusEncd( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetUseOpusEncd );
    }

    //媒体处理线程的音频输入设置是否绘制音频波形到Surface。
    public int AdoInptSetIsDrawAdoWavfmToSurface( int IsBlockWait, int IsDraw, SurfaceView SrcSurfacePt, SurfaceView RsltSurfacePt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetIsDrawAdoWavfmToSurface, IsDraw, SrcSurfacePt, RsltSurfacePt );
    }

    //媒体处理线程的音频输入设置是否保存音频到Wave文件。
    public int AdoInptSetIsSaveAdoToWaveFile( int IsBlockWait, int IsSave, String SrcFullPathStrPt, String RsltFullPathStrPt, long WrBufSzByt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetIsSaveAdoToWaveFile, IsSave, SrcFullPathStrPt, RsltFullPathStrPt, WrBufSzByt );
    }

    //媒体处理线程的音频输入设置是否静音。
    public int AdoInptSetIsMute( int IsBlockWait, int IsMute )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoInptSetIsMute, IsMute );
    }

    //媒体处理线程的设置音频输出。
    public int SetAdoOtpt( int IsBlockWait, int SmplRate, long FrmLenMsec )
    {
        if( ( ( SmplRate != 8000 ) && ( SmplRate != 16000 ) && ( SmplRate != 32000 ) && ( SmplRate != 48000 ) ) || //如果采样频率不正确。
            ( ( FrmLenMsec <= 0 ) || ( FrmLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetAdoOtpt, SmplRate, FrmLenMsec );
    }

    //媒体处理线程的音频输出添加流。
    public int AddAdoOtptStrm( int IsBlockWait, int StrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptAddStrm, StrmIdx );
    }

    //媒体处理线程的音频输出删除流。
    public int DelAdoOtptStrm( int IsBlockWait, int StrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptDelStrm, StrmIdx );
    }

    //媒体处理线程的音频输出设置流要使用PCM原始数据。
    public int AdoOtptSetStrmUsePcm( int IsBlockWait, int StrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetStrmUsePcm, StrmIdx );
    }

    //媒体处理线程的音频输出设置流要使用Speex解码器。
    public int AdoOtptSetStrmUseSpeexDecd( int IsBlockWait, int StrmIdx, int IsUsePrcplEnhsmt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetStrmUseSpeexDecd, StrmIdx, IsUsePrcplEnhsmt );
    }

    //媒体处理线程的音频输出设置流要使用Opus解码器。
    public int AdoOtptSetStrmUseOpusDecd( int IsBlockWait, int StrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetStrmUseOpusDecd, StrmIdx );
    }

    //媒体处理线程的音频输出设置流是否要使用。
    public int AdoOtptSetStrmIsUse( int IsBlockWait, int StrmIdx, int IsUseAdoOtptStrm )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetStrmIsUse, StrmIdx, IsUseAdoOtptStrm );
    }

    //媒体处理线程的音频输出设置是否绘制音频波形到Surface。
    public int AdoOtptSetIsDrawAdoWavfmToSurface( int IsBlockWait, int IsDraw, SurfaceView SrcSurfacePt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetIsDrawAdoWavfmToSurface, IsDraw, SrcSurfacePt );
    }

    //媒体处理线程的音频输出设置是否保存音频到Wave文件。
    public int AdoOtptSetIsSaveAdoToWaveFile( int IsBlockWait, int IsSave, String SrcFullPathStrPt, long WrBufSzByt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetIsSaveAdoToWaveFile, IsSave, SrcFullPathStrPt, WrBufSzByt );
    }

    //媒体处理线程的音频输出设置使用的设备。
    public int AdoOtptSetUseDvc( int IsBlockWait, int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
    {
        if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) ) //如果要使用听筒，则不能使用媒体类型音频输出流。
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetUseDvc, UseSpeakerOrEarpiece, UseVoiceCallOrMusic );
    }

    //媒体处理线程的音频输出设置是否静音。
    public int AdoOtptSetIsMute( int IsBlockWait, int IsMute )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.AdoOtptSetIsMute, IsMute );
    }

    //媒体处理线程的设置视频输入。
    public int SetVdoInpt( int IsBlockWait, int MaxSmplRate, int FrmWidth, int FrmHeight, int ScreenRotate, HTSurfaceView VdoInptPrvwSurfaceViewPt )
    {
        if( ( ( MaxSmplRate < 1 ) || ( MaxSmplRate > 60 ) ) || //如果采样频率不正确。
            ( ( FrmWidth <= 0 ) || ( ( FrmWidth & 1 ) != 0 ) ) || //如果帧的宽度不正确。
            ( ( FrmHeight <= 0 ) || ( ( FrmHeight & 1 ) != 0 ) ) || //如果帧的高度不正确。
            ( ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) ) //如果屏幕旋转的角度不正确。
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetVdoInpt, MaxSmplRate, FrmWidth, FrmHeight, ScreenRotate, VdoInptPrvwSurfaceViewPt );
    }

    //媒体处理线程的视频输入设置要使用Yu12原始数据。
    public int VdoInptSetUseYu12( int IsBlockWait )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoInptSetUseYu12 );
    }

    //媒体处理线程的视频输入设置要使用OpenH264编码器。
    public int VdoInptSetUseOpenH264Encd( int IsBlockWait, int VdoType, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvl, int Cmplxt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoInptSetUseOpenH264Encd, VdoType, EncdBitrate, BitrateCtrlMode, IDRFrmIntvl, Cmplxt );
    }

    //媒体处理线程的视频输入设置要使用系统自带H264编码器。
    public int VdoInptSetUseSystemH264Encd( int IsBlockWait, int EncdBitrate, int BitrateCtrlMode, int IDRFrmIntvlTimeSec, int Cmplxt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoInptSetUseSystemH264Encd, EncdBitrate, BitrateCtrlMode, IDRFrmIntvlTimeSec, Cmplxt );
    }

    //媒体处理线程的视频输入设置使用的设备。
    public int VdoInptSetUseDvc( int IsBlockWait, int UseFrontOrBack, int FrontCameraDvcId, int BackCameraDvcId )
    {
        if( ( ( UseFrontOrBack != 0 ) && ( UseFrontOrBack != 1 ) ) ||
            ( FrontCameraDvcId < -1 ) ||
            ( BackCameraDvcId < -1 ) )
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoInptSetUseDvc, UseFrontOrBack, FrontCameraDvcId, BackCameraDvcId );
    }

    //媒体处理线程的视频输入设置是否黑屏。
    public int VdoInptSetIsBlack( int IsBlockWait, int IsBlack )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoInptSetIsBlack, IsBlack );
    }

    //媒体处理线程的视频输出添加流。
    public int VdoOtptAddStrm( int IsBlockWait, int VdoOtptStrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptAddStrm, VdoOtptStrmIdx );
    }

    //媒体处理线程的视频输出删除流。
    public int VdoOtptDelStrm( int IsBlockWait, int VdoOtptStrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptDelStrm, VdoOtptStrmIdx );
    }

    //媒体处理线程的视频输出设置流。
    public int VdoOtptSetStrm( int IsBlockWait, int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt )
    {
        if( VdoOtptDspySurfaceViewPt == null ) //如果视频显示SurfaceView的指针不正确。
        {
            return -1;
        }

        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrm, VdoOtptStrmIdx, VdoOtptDspySurfaceViewPt );
    }

    //媒体处理线程的视频输出设置流要使用Yu12原始数据。
    public int VdoOtptSetStrmUseYu12( int IsBlockWait, int VdoOtptStrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrmUseYu12, VdoOtptStrmIdx );
    }

    //媒体处理线程的视频输出设置流要使用OpenH264解码器。
    public int VdoOtptSetStrmUseOpenH264Decd( int IsBlockWait, int VdoOtptStrmIdx, int DecdThrdNum )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrmUseOpenH264Decd, VdoOtptStrmIdx, DecdThrdNum );
    }

    //媒体处理线程的视频输出设置流要使用系统自带H264解码器。
    public int VdoOtptSetStrmUseSystemH264Decd( int IsBlockWait, int VdoOtptStrmIdx )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrmUseSystemH264Decd, VdoOtptStrmIdx );
    }

    //媒体处理线程的视频输出设置流是否黑屏。
    public int VdoOtptSetStrmIsBlack( int IsBlockWait, int VdoOtptStrmIdx, int IsBlack )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrmIsBlack, VdoOtptStrmIdx, IsBlack );
    }

    //媒体处理线程的视频输出设置流是否使用。
    public int VdoOtptSetStrmIsUse( int IsBlockWait, int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.VdoOtptSetStrmIsUse, VdoOtptStrmIdx, IsUseVdoOtptStrm );
    }

    //媒体处理线程的设置音视频输入输出是否使用。
    public int SetIsUseAdoVdoInptOtpt( int IsBlockWait, int IsUseAdoInpt, int IsUseAdoOtpt, int IsUseVdoInpt, int IsUseVdoOtpt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetIsUseAdoVdoInptOtpt, IsUseAdoInpt, IsUseAdoOtpt, IsUseVdoInpt, IsUseVdoOtpt );
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
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetIsUseWakeLock, IsUseWakeLock );
    }

    //媒体处理线程的设置是否保存音视频输入输出到Avi文件。
    public int SetIsSaveAdoVdoInptOtptToAviFile( int IsBlockWait, String AdoVdoInptOtptAviFileFullPathStrPt, long AdoVdoInptOtptAviFileWrBufSzByt, int IsSaveAdoInpt, int IsSaveAdoOtpt, int IsSaveVdoInpt, int IsSaveVdoOtpt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SetIsSaveAdoVdoInptOtptToAviFile, AdoVdoInptOtptAviFileFullPathStrPt, AdoVdoInptOtptAviFileWrBufSzByt, IsSaveAdoInpt, IsSaveAdoOtpt, IsSaveVdoInpt, IsSaveVdoOtpt );
    }

    //媒体处理线程的保存状态到文件。
    public int SaveStsToTxtFile( int IsBlockWait, String StngFileFullPathStrPt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.SaveStsToTxtFile, StngFileFullPathStrPt );
    }

    //初始化或销毁媒体处理线程的唤醒锁。
    private void WakeLockInitOrDstoy( int IsInitWakeLock )
    {
        if( IsInitWakeLock != 0 ) //如果要初始化唤醒锁。
        {
            if( ( m_AdoOtptPt.m_IsUse != 0 ) && ( m_AdoOtptPt.m_DvcPt.m_UseWhatDvc != 0 ) ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
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

    //请求媒体处理线程退出。
    public int RqirExit( int ExitFlag, int IsBlockWait )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( ( ExitFlag < 0 ) || ( ExitFlag > 3 ) ) //如果退出标记不正确。
            {
                break Out;
            }

            if( SendMediaMsg( 0, 1, MediaMsgTyp.RqirExit, ExitFlag ) != 0 ) //如果发送请求媒体处理线程退出消息失败。
            {
                break Out;
            }
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
            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return p_Rslt;
    }

    //发送用户消息到媒体处理线程。
    public int SendUserMsg( int IsBlockWait, Object... MsgArgPt )
    {
        return SendMediaMsg( IsBlockWait, 1, MediaMsgTyp.UserMsg, MsgArgPt );
    }

    //初始化媒体处理线程的Avi文件写入器。
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
                    if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.Init( m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt, m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt, 5, m_ErrInfoVstrPt ) == 0 )
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

    //销毁媒体处理线程的Avi文件写入器。
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

    //初始化媒体处理线程的临时变量。
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

    //销毁媒体处理线程的临时变量。
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

    //初始化音视频输入输出。
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

    //音视频输入输出销毁。
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

    //一条媒体消息处理。
    private int OneMediaMsgPocs()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为1表示媒体消息容器为空，为-1表示失败。
        MediaMsg p_MediaMsgPt = null;
        int p_TmpInt32;

        Out:
        {
            if( !m_MediaMsgCntnrPt.isEmpty() ) //如果有媒体消息需要处理。
            {
                p_MediaMsgPt = m_MediaMsgCntnrPt.pollFirst(); //从媒体消息容器中取出并删除第一个媒体消息。
                switch( p_MediaMsgPt.m_MediaMsgTyp )
                {
                    case SetAdoInpt:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            m_AdoInptPt.Dstoy();
                            if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
                        }

                        m_AdoInptPt.m_SmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_FrmLenMsec = ( Long ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_FrmLenUnit = m_AdoInptPt.m_FrmLenMsec * m_AdoInptPt.m_SmplRate / 1000;
                        m_AdoInptPt.m_FrmLenData = m_AdoInptPt.m_FrmLenUnit * 1;
                        m_AdoInptPt.m_FrmLenByt = m_AdoInptPt.m_FrmLenData * 2;
                        m_AdoInptPt.m_IsStartRecordingAfterRead = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

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
                    case AdoInptSetIsUseSystemAecNsAgc:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            m_AdoInptPt.DvcAndThrdDstoy();
                            if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.DvcAndThrdDstoy();
                        }

                        m_AdoInptPt.m_IsUseSystemAecNsAgc = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];

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
                    case AdoInptSetUseNoAec:
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
                    case AdoInptSetUseSpeexAec:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

                        m_AdoInptPt.m_UseWhatAec = 1;
                        m_AdoInptPt.m_SpeexAecPt.m_FilterLenMsec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_SpeexAecPt.m_IsUseRec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_SpeexAecPt.m_EchoMutp = ( Float ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_SpeexAecPt.m_EchoCntu = ( Float ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_AdoInptPt.m_SpeexAecPt.m_EchoSupes = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];
                        m_AdoInptPt.m_SpeexAecPt.m_EchoSupesAct = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 5 ];
                        m_AdoInptPt.m_SpeexAecPt.m_IsSaveMemFile = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 6 ];
                        m_AdoInptPt.m_SpeexAecPt.m_MemFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 7 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.AecInit() != 0 ) break Out;
                            m_AdoInptPt.SetIsCanUseAec();
                        }
                        break;
                    }
                    case AdoInptSetUseWebRtcAecm:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

                        m_AdoInptPt.m_UseWhatAec = 2;
                        m_AdoInptPt.m_WebRtcAecmPt.m_IsUseCNGMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_WebRtcAecmPt.m_EchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_WebRtcAecmPt.m_Delay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.AecInit() != 0 ) break Out;
                            m_AdoInptPt.SetIsCanUseAec();
                        }
                        break;
                    }
                    case AdoInptSetUseWebRtcAec:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

                        m_AdoInptPt.m_UseWhatAec = 3;
                        m_AdoInptPt.m_WebRtcAecPt.m_EchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_Delay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_IsUseDelayAgstcMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_IsUseExtdFilterMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_IsUseAdaptAdjDelay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 5 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_IsSaveMemFile = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 6 ];
                        m_AdoInptPt.m_WebRtcAecPt.m_MemFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 7 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.AecInit() != 0 ) break Out;
                            m_AdoInptPt.SetIsCanUseAec();
                        }
                        break;
                    }
                    case AdoInptSetUseSpeexWebRtcAec:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.AecDstoy();

                        m_AdoInptPt.m_UseWhatAec = 4;
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WorkMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecFilterLenMsec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecIsUseRec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoMutp = ( Float ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoCntu = ( Float ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupes = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 5 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SpeexAecEchoSupesAct = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 6 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmIsUseCNGMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 7 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 8 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecmDelay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 9 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecEchoMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 10 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecDelay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 11 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 12 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseExtdFilterMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 13 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 14 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_WebRtcAecIsUseAdaptAdjDelay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 15 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_IsUseSameRoomAec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 16 ];
                        m_AdoInptPt.m_SpeexWebRtcAecPt.m_SameRoomEchoMinDelay = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 17 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.AecInit() != 0 ) break Out;
                            m_AdoInptPt.SetIsCanUseAec();
                        }
                        break;
                    }
                    case AdoInptSetUseNoNs:
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
                    case AdoInptSetUseSpeexPrpocsNs:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            m_AdoInptPt.NsDstoy();
                            m_AdoInptPt.SpeexPrpocsDstoy();
                        }

                        m_AdoInptPt.m_UseWhatNs = 1;
                        m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseNs = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_SpeexPrpocsNsPt.m_NoiseSupes = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_SpeexPrpocsNsPt.m_IsUseDereverb = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.NsInit() != 0 ) break Out;
                            if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
                        }
                        break;
                    }
                    case AdoInptSetUseWebRtcNsx:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            m_AdoInptPt.NsDstoy();
                            m_AdoInptPt.SpeexPrpocsDstoy();
                        }

                        m_AdoInptPt.m_UseWhatNs = 2;
                        m_AdoInptPt.m_WebRtcNsxPt.m_PolicyMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.NsInit() != 0 ) break Out;
                            if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
                        }
                        break;
                    }
                    case AdoInptSetUseWebRtcNs:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            m_AdoInptPt.NsDstoy();
                            m_AdoInptPt.SpeexPrpocsDstoy();
                        }

                        m_AdoInptPt.m_UseWhatNs = 3;
                        m_AdoInptPt.m_WebRtcNsPt.m_PolicyMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.NsInit() != 0 ) break Out;
                            if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
                        }
                        break;
                    }
                    case AdoInptSetUseRNNoise:
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
                    case AdoInptSetIsUseSpeexPrpocs:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.SpeexPrpocsDstoy();

                        m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseSpeexPrpocs = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseVad = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbStart = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_VadProbCntu = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_IsUseAgc = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_AgcLevel = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 5 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_AgcIncrement = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 6 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_AgcDecrement = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 7 ];
                        m_AdoInptPt.m_SpeexPrpocsPt.m_AgcMaxGain = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 8 ];

                        if( m_AdoInptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.SpeexPrpocsInit() != 0 ) break Out;
                            m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val = 1;
                        }
                        break;
                    }
                    case AdoInptSetUsePcm:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

                        m_AdoInptPt.m_UseWhatEncd = 0;

                        if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
                        MediaPocsThrdTmpVarInit();
                        break;
                    }
                    case AdoInptSetUseSpeexEncd:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

                        m_AdoInptPt.m_UseWhatEncd = 1;
                        m_AdoInptPt.m_SpeexEncdPt.m_UseCbrOrVbr = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_SpeexEncdPt.m_Qualt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_SpeexEncdPt.m_Cmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_SpeexEncdPt.m_PlcExptLossRate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];

                        if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
                        MediaPocsThrdTmpVarInit();
                        break;
                    }
                    case AdoInptSetUseOpusEncd:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.EncdDstoy();

                        m_AdoInptPt.m_UseWhatEncd = 2;

                        if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.EncdInit() != 0 ) break Out;
                        MediaPocsThrdTmpVarInit();
                        break;
                    }
                    case AdoInptSetIsSaveAdoToWaveFile:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.WaveFileWriterDstoy();

                        m_AdoInptPt.m_WaveFileWriterPt.m_IsSave = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_WaveFileWriterPt.m_RsltFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoInptPt.m_WaveFileWriterPt.m_WrBufSzByt = ( long ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];

                        if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.WaveFileWriterInit() != 0 ) break Out;
                        break;
                    }
                    case AdoInptSetIsDrawAdoWavfmToSurface:
                    {
                        if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.WavfmDstoy();

                        m_AdoInptPt.m_WavfmPt.m_IsDraw = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoInptPt.m_WavfmPt.m_SrcSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoInptPt.m_WavfmPt.m_RsltSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

                        if( m_AdoInptPt.m_IsInit != 0 ) if( m_AdoInptPt.WavfmInit() != 0 ) break Out;
                        break;
                    }
                    case AdoInptSetIsMute:
                    {
                        m_AdoInptPt.m_DvcPt.m_IsMute = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        break;
                    }
                    case SetAdoOtpt:
                    {
                        if( m_AdoOtptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
                            m_AdoOtptPt.Dstoy();
                        }

                        m_AdoOtptPt.m_SmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoOtptPt.m_FrmLenMsec = ( Long ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
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
                    case AdoOtptAddStrm:
                    {
                        m_AdoOtptPt.AddStrm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case AdoOtptDelStrm:
                    {
                        m_AdoOtptPt.DelStrm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case AdoOtptSetStrmUsePcm:
                    {
                        m_AdoOtptPt.SetStrmUsePcm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case AdoOtptSetStrmUseSpeexDecd:
                    {
                        m_AdoOtptPt.SetStrmUseSpeexDecd( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case AdoOtptSetStrmUseOpusDecd:
                    {
                        m_AdoOtptPt.SetStrmUseOpusDecd( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case AdoOtptSetStrmIsUse:
                    {
                        m_AdoOtptPt.SetStrmIsUse( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case AdoOtptSetIsSaveAdoToWaveFile:
                    {
                        if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.WaveFileWriterDstoy();

                        m_AdoOtptPt.m_WaveFileWriterPt.m_IsSave = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoOtptPt.m_WaveFileWriterPt.m_SrcFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoOtptPt.m_WaveFileWriterPt.m_WrBufSzByt = ( long ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

                        if( m_AdoOtptPt.m_IsInit != 0 ) if( m_AdoOtptPt.WaveFileWriterInit() != 0 ) break Out;
                        break;
                    }
                    case AdoOtptSetIsDrawAdoWavfmToSurface:
                    {
                        if( m_AdoOtptPt.m_IsInit != 0 ) m_AdoOtptPt.WavfmDstoy();

                        m_AdoOtptPt.m_WavfmPt.m_IsDraw = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoOtptPt.m_WavfmPt.m_SrcSurfacePt = ( SurfaceView ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];

                        if( m_AdoOtptPt.m_IsInit != 0 ) if( m_AdoOtptPt.WavfmInit() != 0 ) break Out;
                        break;
                    }
                    case AdoOtptSetUseDvc:
                    {
                        if( m_AdoOtptPt.m_IsInit != 0 )
                        {
                            if( m_AdoInptPt.m_IsInit != 0 ) m_AdoInptPt.DvcAndThrdDstoy();
                            m_AdoOtptPt.DvcAndThrdDstoy();
                        }

                        m_AdoOtptPt.m_DvcPt.m_UseWhatDvc = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoOtptPt.m_DvcPt.m_UseWhatStreamType = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
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
                    case AdoOtptSetIsMute:
                    {
                        m_AdoOtptPt.m_DvcPt.m_IsMute = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        break;
                    }
                    case SetVdoInpt:
                    {
                        if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

                        m_VdoInptPt.m_MaxSmplRate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_VdoInptPt.m_FrmWidth = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_VdoInptPt.m_FrmHeight = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_VdoInptPt.m_Yu12FrmLenByt = m_VdoInptPt.m_FrmWidth * m_VdoInptPt.m_FrmHeight * 3 / 2;
                        m_VdoInptPt.m_ScreenRotate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt = ( HTSurfaceView ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];

                        if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
                        break;
                    }
                    case VdoInptSetUseYu12:
                    {
                        if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

                        m_VdoInptPt.m_UseWhatEncd = 0;

                        if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
                        break;
                    }
                    case VdoInptSetUseOpenH264Encd:
                    {
                        if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

                        m_VdoInptPt.m_UseWhatEncd = 1;
                        m_VdoInptPt.m_OpenH264EncdPt.m_VdoType = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_VdoInptPt.m_OpenH264EncdPt.m_EncdBitrate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_VdoInptPt.m_OpenH264EncdPt.m_BitrateCtrlMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_VdoInptPt.m_OpenH264EncdPt.m_IDRFrmIntvl = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_VdoInptPt.m_OpenH264EncdPt.m_Cmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];

                        if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
                        break;
                    }
                    case VdoInptSetUseSystemH264Encd:
                    {
                        if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

                        m_VdoInptPt.m_UseWhatEncd = 2;
                        m_VdoInptPt.m_SystemH264EncdPt.m_EncdBitrate = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_VdoInptPt.m_SystemH264EncdPt.m_BitrateCtrlMode = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_VdoInptPt.m_SystemH264EncdPt.m_IDRFrmIntvlTimeSec = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_VdoInptPt.m_SystemH264EncdPt.m_Cmplxt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];

                        if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
                        break;
                    }
                    case VdoInptSetUseDvc:
                    {
                        if( m_VdoInptPt.m_IsInit != 0 ) m_VdoInptPt.Dstoy();

                        m_VdoInptPt.m_DvcPt.m_UseWhatDvc = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_VdoInptPt.m_DvcPt.m_FrontCameraId = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_VdoInptPt.m_DvcPt.m_BackCameraId = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];

                        if( m_VdoInptPt.m_IsInit != 0 ) if( m_VdoInptPt.Init() != 0 ) break Out;
                        break;
                    }
                    case VdoInptSetIsBlack:
                    {
                        m_VdoInptPt.m_DvcPt.m_IsBlack = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        break;
                    }
                    case VdoOtptAddStrm:
                    {
                        m_VdoOtptPt.AddStrm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case VdoOtptDelStrm:
                    {
                        m_VdoOtptPt.DelStrm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case VdoOtptSetStrm:
                    {
                        m_VdoOtptPt.SetStrm( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( HTSurfaceView ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case VdoOtptSetStrmUseYu12:
                    {
                        m_VdoOtptPt.SetStrmUseYu12( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case VdoOtptSetStrmUseOpenH264Decd:
                    {
                        m_VdoOtptPt.SetStrmUseOpenH264Decd( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case VdoOtptSetStrmUseSystemH264Decd:
                    {
                        m_VdoOtptPt.SetStrmUseSystemH264Decd( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] );
                        break;
                    }
                    case VdoOtptSetStrmIsBlack:
                    {
                        m_VdoOtptPt.SetStrmIsBlack( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case VdoOtptSetStrmIsUse:
                    {
                        m_VdoOtptPt.SetStrmIsUse( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ], ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ] );
                        break;
                    }
                    case SetIsUseAdoVdoInptOtpt:
                    {
                        int p_IsUseAdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        int p_IsUseAdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        int p_IsUseVdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        int p_IsUseVdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];

                        if( p_IsUseAdoInpt >= 0 ) m_AdoInptPt.m_IsUse = p_IsUseAdoInpt;
                        if( p_IsUseAdoOtpt >= 0 ) m_AdoOtptPt.m_IsUse = p_IsUseAdoOtpt;
                        if( p_IsUseVdoInpt >= 0 ) m_VdoInptPt.m_IsUse = p_IsUseVdoInpt;
                        if( p_IsUseVdoOtpt >= 0 ) m_VdoOtptPt.m_IsUse = p_IsUseVdoOtpt;

                        if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
                        WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
                        break;
                    }
                    case SetIsUseWakeLock:
                    {
                        m_IsUseWakeLock = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
                        break;
                    }
                    case SetIsSaveAdoVdoInptOtptToAviFile:
                    {
                        //AdoVdoInptOtptAviFileWriterDstoy(); //这里不用销毁。

                        m_AdoVdoInptOtptAviFilePt.m_FullPathStrPt = ( String )p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
                        m_AdoVdoInptOtptAviFilePt.m_WrBufSzByt = ( Long )p_MediaMsgPt.m_MsgArgCntnrPt[ 1 ];
                        m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 2 ];
                        m_AdoVdoInptOtptAviFilePt.m_IsSaveAdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 3 ];
                        m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoInpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 4 ];
                        m_AdoVdoInptOtptAviFilePt.m_IsSaveVdoOtpt = ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 5 ];

                        if( m_LastCallUserInitOrDstoy == 0 ) if( AdoVdoInptOtptAviFileWriterInit() != 0 ) break Out;
                        break;
                    }
                    case SaveStsToTxtFile:
                    {
                        String p_StngFileFullPathStrPt = ( String ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ];
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
                            p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_IsSaveMemFile：" + m_AdoInptPt.m_SpeexAecPt.m_IsSaveMemFile + "\n" );
                            p_StngFileWriterPt.write( "m_AdoInptPt.m_SpeexAecPt.m_MemFileFullPathStrPt：" + m_AdoInptPt.m_SpeexAecPt.m_MemFileFullPathStrPt + "\n" );
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
                            p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_IsSaveMemFile：" + m_AdoInptPt.m_WebRtcAecPt.m_IsSaveMemFile + "\n" );
                            p_StngFileWriterPt.write( "m_AdoInptPt.m_WebRtcAecPt.m_MemFileFullPathStrPt：" + m_AdoInptPt.m_WebRtcAecPt.m_MemFileFullPathStrPt + "\n" );
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
                            p_StngFileWriterPt.write( "m_AdoOtptPt.m_DvcPt.m_UseWhatDvc：" + m_AdoOtptPt.m_DvcPt.m_UseWhatDvc + "\n" );
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
                    case RqirExit:
                    {
                        switch( ( Integer ) p_MediaMsgPt.m_MsgArgCntnrPt[ 0 ] )
                        {
                            case 1: //为请求退出。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求：退出。" );

                                //执行顺序：媒体销毁，用户销毁并退出。
                                if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
                                {
                                    if( SendMediaMsg( 1, 0, MediaMsgTyp.UserDstoy ) != 0 ) break Out;
                                    if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
                                }
                                else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
                                {
                                    m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                                }
                                break;
                            }
                            case 2: //请求重启。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求：重启。" );

                                //执行顺序：媒体销毁，用户销毁，用户初始化，媒体初始化。
                                if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
                                if( SendMediaMsg( 1, 0, MediaMsgTyp.UserInit ) != 0 ) break Out;
                                if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数。
                                {
                                    if( SendMediaMsg( 1, 0, MediaMsgTyp.UserDstoy ) != 0 ) break Out;
                                    if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
                                }
                                else //如果上一次调用了用户定义的销毁函数，就不再进行媒体销毁，用户销毁。
                                {
                                    m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
                                }
                                break;
                            }
                            case 3: //请求重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求：重启但不执行用户定义的UserInit初始化函数和UserDstoy销毁函数。" );

                                //执行顺序：媒体销毁，媒体初始化。
                                if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptInit ) != 0 ) break Out;
                                if( SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptDstoy ) != 0 ) break Out;
                                m_ReadyExitCnt--; //设置准备退出计数递减。因为在请求退出时递增了。
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
                        p_TmpInt32 = UserInit(); //调用用户定义的初始化函数。
                        if( p_TmpInt32 == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt32 );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt32 );
                            break Out;
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
                        p_TmpInt32 = UserMsg( p_MediaMsgPt.m_MsgArgCntnrPt );
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
                    case AdoVdoInptOtptInit:
                    {
                        if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就初始化音视频输入输出、音视频输入输出Avi文件写入器。
                        {
                            if( AdoVdoInptOtptInit() != 0 ) break Out;
                            if( AdoVdoInptOtptAviFileWriterInit() != 0 ) break Out;
                        }
                        break;
                    }
                    case AdoVdoInptOtptDstoy:
                    {
                        AdoVdoInptOtptAviFileWriterDstoy();
                        AdoVdoInptOtptDstoy();
                        break;
                    }
                }

                p_MediaMsgPt.m_MediaMsgRslt = 0; //设置媒体消息处理结果为成功。
                p_Rslt = 0; //设置本函数执行成功。
            }
            else
            {
                p_Rslt = 1; //设置本函数执行成功。
            }
        }

        if( p_Rslt < 0 ) //如果本函数执行失败。
        {
            if( p_MediaMsgPt.m_MediaMsgTyp == MediaMsgTyp.UserInit )
                m_ExitCode = ExitCode.UserInit; //设置退出码为调用用户定义的初始化函数失败。
            else if( p_MediaMsgPt.m_MediaMsgTyp == MediaMsgTyp.AdoVdoInptOtptInit )
                m_ExitCode = ExitCode.AdoVdoInptOtptInit; //设置退出码为音视频输入输出初始化失败。
            else
                m_ExitCode = ExitCode.MediaMsgPocs; //设置退出码为媒体消息处理失败。

            if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就执行销毁。
            {
                //执行顺序：媒体销毁，用户销毁并退出。
                m_ReadyExitCnt++;
                SendMediaMsg( 1, 0, MediaMsgTyp.UserDstoy );
                SendMediaMsg( 1, 0, MediaMsgTyp.AdoVdoInptOtptDstoy );
            }

            p_MediaMsgPt.m_MediaMsgRslt = -1; //设置媒体消息处理结果为失败。
        }
        return p_Rslt;
    }

    //音视频输入输出帧处理。
    private int AdoVdoInptOtptFrmPocs()
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
                p_TmpInt321 = UserPocs();
                if( p_TmpInt321 == 0 )
                {
                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowTickMsec = SystemClock.uptimeMillis();
                        Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt321 + "。耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                        p_LastTickMsec = SystemClock.uptimeMillis();
                    }
                }
                else
                {
                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowTickMsec = SystemClock.uptimeMillis();
                        Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt321 + "。耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
                        p_LastTickMsec = SystemClock.uptimeMillis();
                    }
                    break Out;
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
                        case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
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
                    case 0: //如果要使用PCM原始数据。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
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
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：音视频输入输出Avi文件添加视频输出已编码格式原始流成功。索引：" + p_VdoOtptStrmAviFileIdx + "。" );
                            m_AdoVdoInptOtptAviFilePt.m_VdoOtptEncdSrcStrmIdxMapPt.put( m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx, p_VdoOtptStrmAviFileIdx );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：音视频输入输出Avi文件添加视频输出已编码格式原始流失败。原因：" + m_ErrInfoVstrPt.GetStr() );
                        }
                    }

                    if( p_VdoOtptStrmAviFileIdx != -1 )
                    {
                        if( m_AdoVdoInptOtptAviFilePt.m_WriterPt.VdoStrmWriteByte( p_VdoOtptStrmAviFileIdx, m_ThrdPt.m_VdoOtptFrmPt.m_TimeStampMsec, m_ThrdPt.m_VdoOtptFrmPt.m_EncdSrcFrmPt, m_ThrdPt.m_VdoOtptFrmPt.m_EncdSrcFrmLenBytPt.m_Val, m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：使用音视频输入输出Avi文件写入器写入视频输出已编码格式原始帧成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出流索引 " + m_ThrdPt.m_VdoOtptFrmPt.m_StrmIdx + "：使用音视频输入输出Avi文件写入器写入视频输出已编码格式原始帧失败。原因：" + m_ErrInfoVstrPt.GetStr() );
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
                                UserReadAdoVdoInptFrm(
                                        m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
                                        m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
                            }
                            else //如果没有视频输入已编码格式结果帧。
                            {
                                UserReadAdoVdoInptFrm(
                                        m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
                                        m_ThrdPt.m_AdoInptEncdRsltFrmPt, m_ThrdPt.m_AdoInptEncdRsltFrmLenBytPt.m_Val, m_ThrdPt.m_AdoInptEncdRsltFrmIsNeedTransPt.m_Val,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
                                        null, 0L );
                            }
                        }
                        else //如果没有视频输入帧。
                        {
                            UserReadAdoVdoInptFrm(
                                    m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
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
                                UserReadAdoVdoInptFrm(
                                        m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
                                        null, 0, 0,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
                            }
                            else //如果没有视频输入已编码格式结果帧。
                            {
                                UserReadAdoVdoInptFrm(
                                        m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
                                        null, 0, 0,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
                                        m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
                                        null, 0L );
                            }
                        }
                        else //如果没有视频输入帧。
                        {
                            UserReadAdoVdoInptFrm(
                                    m_ThrdPt.m_AdoInptPcmSrcFrmPt, m_ThrdPt.m_AdoInptPcmRsltFrmPt, m_AdoInptPt.m_FrmLenUnit, m_ThrdPt.m_AdoInptPcmRsltFrmVoiceActStsPt.m_Val,
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
                            UserReadAdoVdoInptFrm(
                                    null, null, 0, 0,
                                    null, 0, 0,
                                    m_ThrdPt.m_VdoInptFrmPt.m_Nv21SrcFrmPt, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmWidth, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmHeight, m_VdoInptPt.m_DvcPt.m_Nv21SrcFrmLenByt,
                                    m_ThrdPt.m_VdoInptFrmPt.m_Yu12RsltFrmPt, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleWidth, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleHeight, m_VdoInptPt.m_DvcPt.m_Yu12SrcFrmScaleLenByt,
                                    m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmPt, m_ThrdPt.m_VdoInptFrmPt.m_EncdRsltFrmLenBytPt.m_Val );
                        }
                        else //如果没有视频输入已编码格式结果帧。
                        {
                            UserReadAdoVdoInptFrm(
                                    null, null, 0, 0,
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
            m_ExitCode = ExitCode.AdoVdoInptOtptPocs; //设置退出码为音视频输入输出处理失败。

            if( m_LastCallUserInitOrDstoy == 0 ) //如果上一次调用了用户定义的初始化函数，就执行销毁。
            {
                //执行顺序：媒体销毁，用户销毁并退出。
                m_ReadyExitCnt++;
                SendMediaMsg( 0, 0, MediaMsgTyp.UserDstoy );
                SendMediaMsg( 0, 0, MediaMsgTyp.AdoVdoInptOtptDstoy );
            }
        }
        return p_Rslt;
    }

    //本线程执行函数。
    public void run()
    {
        long p_LastTickMsec = 0;

        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本地代码的指令集名称（CPU类型+ ABI约定）：" + android.os.Build.CPU_ABI + "，设备型号：" + android.os.Build.MODEL + "，上下文：" + m_CtxPt + "。" );

        m_RunFlag = RunFlag.Run; //设置本线程运行标记为正在运行。
        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：开始准备音视频输入输出帧处理。" );

        SendMediaMsg( 0, 1, MediaMsgTyp.UserInit );
        SendMediaMsg( 0, 1, MediaMsgTyp.AdoVdoInptOtptInit );

        //媒体处理循环开始。
        while( true )
        {
            while( OneMediaMsgPocs() != 1 ); //进行一条媒体消息处理，直到媒体消息容器为空。
            if( m_ReadyExitCnt > 0 ) break; //如果媒体消息容器为空，且媒体处理线程准备退出。

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