package HeavenTao.Media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Ado.*;
import HeavenTao.Data.*;

public class AdoInpt //存放音频输入。
{
    MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

    public int m_IsUse; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。
    public int m_IsInit; //存放是否初始化音频输入，为0表示未初始化，为非0表示已初始化。

    public int m_SmplRate; //存放采样频率，单位为赫兹，取值只能为8000、16000、32000、48000。
    public long m_FrmLenMsec; //存放帧的长度，单位为毫秒，取值只能为10毫秒的倍数。
    public long m_FrmLenUnit; //存放帧的长度，单位为采样单元，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
    public long m_FrmLenData; //存放帧的长度，单位为采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
    public long m_FrmLenByt; //存放帧的长度，单位为字节，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80*2、20毫秒为160*2、30毫秒为240*2，16000Hz的10毫秒为160*2、20毫秒为320*2、30毫秒为480*2，32000Hz的10毫秒为320*2、20毫秒为640*2、30毫秒为960*2，48000Hz的10毫秒为480*2、20毫秒为960*2、30毫秒为1440*2。

    public int m_IsUseSystemAecNsAgc; //存放是否使用系统自带声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带），为0表示不使用，为非0表示要使用。

    public int m_UseWhatAec; //存放使用什么声学回音消除器，为0表示不使用，为1表示Speex声学回音消除器，为2表示WebRtc定点版声学回音消除器，为2表示WebRtc浮点版声学回音消除器，为4表示SpeexWebRtc三重声学回音消除器。
    public int m_IsCanUseAec; //存放是否可以使用声学回音消除器，为0表示不可以，为非0表示可以。

    class SpeexAec //存放Speex声学回音消除器。
    {
        HeavenTao.Ado.SpeexAec m_Pt; //存放指针。
        int m_FilterLenMsec; //存放滤波器的长度，单位为毫秒。
        int m_IsUseRec; //存放是否使用残余回音消除，为非0表示要使用，为0表示不使用。
        float m_EchoMutp; //存放在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
        float m_EchoCntu; //存放在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
        int m_EchoSupes; //存放在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_EchoSupesAct; //存放在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_IsSaveMemFile; //存放是否保存内存块到文件，为非0表示要保存，为0表示不保存。
        String m_MemFileFullPathStrPt; //存放内存块文件完整路径字符串的指针。
    }
    SpeexAec m_SpeexAecPt = new SpeexAec();

    class WebRtcAecm //存放WebRtc定点版声学回音消除器。
    {
        HeavenTao.Ado.WebRtcAecm m_Pt; //存放指针。
        int m_IsUseCNGMode; //存放是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
        int m_EchoMode; //存放消除模式，消除模式越高消除越强，取值区间为[0,4]。
        int m_Delay; //存放回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
    }
    WebRtcAecm m_WebRtcAecmPt = new WebRtcAecm();

    class WebRtcAec //存放WebRtc浮点版声学回音消除器。
    {
        HeavenTao.Ado.WebRtcAec m_Pt; //存放指针。
        int m_EchoMode; //存放消除模式，消除模式越高消除越强，取值区间为[0,2]。
        int m_Delay; //存放回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
        int m_IsUseDelayAgstcMode; //存放是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
        int m_IsUseExtdFilterMode; //存放是否使用扩展滤波器模式，为非0表示要使用，为0表示不使用。
        int m_IsUseRefinedFilterAdaptAecMode; //存放是否使用精制滤波器自适应Aec模式，为非0表示要使用，为0表示不使用。
        int m_IsUseAdaptAdjDelay; //存放是否使用自适应调节回音延迟，为非0表示要使用，为0表示不使用。
        int m_IsSaveMemFile; //存放是否保存内存块到文件，为非0表示要保存，为0表示不保存。
        String m_MemFileFullPathStrPt; //存放内存块文件完整路径字符串的指针。
    }
    WebRtcAec m_WebRtcAecPt = new WebRtcAec();

    class SpeexWebRtcAec //存放SpeexWebRtc三重声学回音消除器。
    {
        HeavenTao.Ado.SpeexWebRtcAec m_Pt; //存放指针。
        int m_WorkMode; //存放工作模式，为1表示Speex声学回音消除器+WebRtc定点版声学回音消除器，为2表示WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器，为3表示Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
        int m_SpeexAecFilterLenMsec; //存放Speex声学回音消除器的滤波器的长度，单位毫秒。
        int m_SpeexAecIsUseRec; //存放Speex声学回音消除器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
        float m_SpeexAecEchoMutp; //存放Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
        float m_SpeexAecEchoCntu; //存放Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
        int m_SpeexAecEchoSupes; //存放Speex声学回音消除器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_SpeexAecEchoSupesAct; //存放Speex声学回音消除器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_WebRtcAecmIsUseCNGMode; //存放WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecmEchoMode; //存放WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
        int m_WebRtcAecmDelay; //存放WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
        int m_WebRtcAecEchoMode; //存放WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
        int m_WebRtcAecDelay; //存放WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
        int m_WebRtcAecIsUseDelayAgstcMode; //存放WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseExtdFilterMode; //存放WebRtc浮点版声学回音消除器是否使用扩展滤波器模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseRefinedFilterAdaptAecMode; //存放WebRtc浮点版声学回音消除器是否使用精制滤波器自适应Aec模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseAdaptAdjDelay; //存放WebRtc浮点版声学回音消除器是否使用自适应调节回音延迟，为非0表示要使用，为0表示不使用。
        int m_IsUseSameRoomAec; //存放是否使用同一房间声学回音消除，为非0表示要使用，为0表示不使用。
        int m_SameRoomEchoMinDelay; //存放同一房间回音最小延迟，单位毫秒，取值区间为[1,2147483647]。
    }
    SpeexWebRtcAec m_SpeexWebRtcAecPt = new SpeexWebRtcAec();

    public int m_UseWhatNs; //存放使用什么噪音抑制器，为0表示不使用，为1表示Speex预处理器的噪音抑制，为2表示WebRtc定点版噪音抑制器，为3表示WebRtc浮点版噪音抑制器，为4表示RNNoise噪音抑制器。

    class SpeexPrpocsNs //存放Speex预处理器的噪音抑制。
    {
        int m_IsUseNs; //存放是否使用噪音抑制，为非0表示要使用，为0表示不使用。
        int m_NoiseSupes; //存放在噪音抑制时，噪音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_IsUseDereverb; //存放是否使用混响音消除，为非0表示要使用，为0表示不使用。
    }
    SpeexPrpocsNs m_SpeexPrpocsNsPt = new SpeexPrpocsNs();

    class WebRtcNsx //存放WebRtc定点版噪音抑制器。
    {
        HeavenTao.Ado.WebRtcNsx m_Pt; //存放指针。
        int m_PolicyMode; //存放策略模式，策略模式越高抑制越强，取值区间为[0,3]。
    }
    WebRtcNsx m_WebRtcNsxPt = new WebRtcNsx();

    class WebRtcNs //存放WebRtc浮点版噪音抑制器。
    {
        HeavenTao.Ado.WebRtcNs m_Pt; //存放指针。
        int m_PolicyMode; //存放策略模式，策略模式越高抑制越强，取值区间为[0,3]。
    }
    WebRtcNs m_WebRtcNsPt = new WebRtcNs();

    class RNNoise //存放RNNoise噪音抑制器。
    {
        HeavenTao.Ado.RNNoise m_Pt; //存放指针。
    }
    RNNoise m_RNNoisePt = new RNNoise();

    class SpeexPrpocs
    {
        int m_IsUseSpeexPrpocs; //存放是否使用Speex预处理器，为非0表示要使用，为0表示不使用。
        HeavenTao.Ado.SpeexPrpocs m_Pt; //存放指针。
        int m_IsUseVad; //存放是否使用语音活动检测，为非0表示要使用，为0表示不使用。
        int m_VadProbStart; //存放在语音活动检测时，从无语音活动到有语音活动的判断百分比概率，概率越大越难判断为有语音活，取值区间为[0,100]。
        int m_VadProbCntu; //存放在语音活动检测时，从有语音活动到无语音活动的判断百分比概率，概率越大越容易判断为无语音活动，取值区间为[0,100]。
        int m_IsUseAgc; //存放是否使用自动增益控制，为非0表示要使用，为0表示不使用。
        int m_AgcLevel; //存放在自动增益控制时，增益的目标等级，目标等级越大增益越大，取值区间为[1,2147483647]。
        int m_AgcIncrement; //存放在自动增益控制时，每秒最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。
        int m_AgcDecrement; //存放在自动增益控制时，每秒最大减益的分贝值，分贝值越小减益越大，取值区间为[-2147483648,0]。
        int m_AgcMaxGain; //存放在自动增益控制时，最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。
    }
    SpeexPrpocs m_SpeexPrpocsPt = new SpeexPrpocs();

    public int m_UseWhatEncd; //存放使用什么编码器，为0表示PCM原始数据，为1表示Speex编码器，为2表示Opus编码器。

    class SpeexEncd //存放Speex编码器。
    {
        HeavenTao.Ado.SpeexEncd m_Pt; //存放指针。
        int m_UseCbrOrVbr; //存放使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
        int m_Qualt; //存放编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
        int m_Cmplxt; //存放编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
        int m_PlcExptLossRate; //存放在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。
    }
    SpeexEncd m_SpeexEncdPt = new SpeexEncd();

    class Wavfm //存放波形器。
    {
        public int m_IsDraw; //存放是否绘制，为非0表示要绘制，为0表示不绘制。
        AdoWavfm m_SrcPt; //存放原始的指针。
        AdoWavfm m_RsltPt; //存放结果的指针。
        SurfaceView m_SrcSurfacePt; //存放原始Surface的指针。
        SurfaceView m_RsltSurfacePt; //存放结果Surface的指针。
    }
    Wavfm m_WavfmPt = new Wavfm();

    class WaveFileWriter //存放Wave文件写入器。
    {
        public int m_IsSave; //存放是否保存，为非0表示要保存，为0表示不保存。
        HeavenTao.Media.WaveFileWriter m_SrcPt; //存放原始的指针。
        HeavenTao.Media.WaveFileWriter m_RsltPt; //存放结果的指针。
        String m_SrcFullPathStrPt; //存放原始完整路径字符串的指针。
        String m_RsltFullPathStrPt; //存放结果完整路径字符串的指针。
        long m_WrBufSzByt; //存放写入缓冲区的大小，单位为字节。
    }
    WaveFileWriter m_WaveFileWriterPt = new WaveFileWriter();

    class Dvc //存放设备。
    {
        AudioRecord m_Pt; //存放指针。
        int m_BufSzByt; //存放缓冲区大小，单位为字节。
        public int m_IsMute; //存放是否静音，为0表示有声音，为非0表示静音。
    }
    Dvc m_DvcPt = new Dvc();

    public ConcurrentLinkedQueue< short[] > m_PcmSrcFrmCntnrPt; //存放Pcm格式原始帧容器的指针。
    public ConcurrentLinkedQueue< short[] > m_PcmIdleFrmCntnrPt; //存放Pcm格式空闲帧容器的指针。

    class Thrd //存放线程。
    {
        int m_IsInitThrdTmpVar; //存放是否初始化线程的临时变量。
        short[] m_PcmSrcFrmPt; //存放Pcm格式原始帧的指针。
        int m_ElmTotal; //存放元素总数。
        long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
        long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

        AdoInptThrd m_ThrdPt; //存放线程的指针。
        int m_IsStartAdoOtptThrd; //存放是否开始音频输出线程，为0表示未开始，为1表示已开始。
        int m_ExitFlag; //存放退出标记，为0表示保持运行，为1表示请求退出。
    }
    Thrd m_ThrdPt = new Thrd();

    //初始化音频输入的声学回音消除器。
    public int AecInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            switch( m_UseWhatAec )
            {
                case 0: //如果不使用声学回音消除器。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：不使用声学回音消除器。" );
                    break;
                }
                case 1: //如果要使用Speex声学回音消除器。
                {
                    if( m_SpeexAecPt.m_IsSaveMemFile != 0 ) //如果Speex声学回音消除器要保存内存块到文件。
                    {
                        m_SpeexAecPt.m_Pt = new HeavenTao.Ado.SpeexAec();
                        if( m_SpeexAecPt.m_Pt.InitByMemFile( m_SmplRate, m_FrmLenUnit, m_SpeexAecPt.m_FilterLenMsec, m_SpeexAecPt.m_IsUseRec, m_SpeexAecPt.m_EchoMutp, m_SpeexAecPt.m_EchoCntu, m_SpeexAecPt.m_EchoSupes, m_SpeexAecPt.m_EchoSupesAct, m_SpeexAecPt.m_MemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：根据Speex声学回音消除器内存块文件 " + m_SpeexAecPt.m_MemFileFullPathStrPt + " 来初始化Speex声学回音消除器成功。" );
                        }
                        else
                        {
                            m_SpeexAecPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：根据Speex声学回音消除器内存块文件 " + m_SpeexAecPt.m_MemFileFullPathStrPt + " 来初始化Speex声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        }
                    }
                    if( m_SpeexAecPt.m_Pt == null )
                    {
                        m_SpeexAecPt.m_Pt = new HeavenTao.Ado.SpeexAec();
                        if( m_SpeexAecPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_SpeexAecPt.m_FilterLenMsec, m_SpeexAecPt.m_IsUseRec, m_SpeexAecPt.m_EchoMutp, m_SpeexAecPt.m_EchoCntu, m_SpeexAecPt.m_EchoSupes, m_SpeexAecPt.m_EchoSupesAct, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex声学回音消除器成功。" );
                        }
                        else
                        {
                            m_SpeexAecPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                    }
                    break;
                }
                case 2: //如果要使用WebRtc定点版声学回音消除器。
                {
                    m_WebRtcAecmPt.m_Pt = new HeavenTao.Ado.WebRtcAecm();
                    if( m_WebRtcAecmPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_WebRtcAecmPt.m_IsUseCNGMode, m_WebRtcAecmPt.m_EchoMode, m_WebRtcAecmPt.m_Delay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc定点版声学回音消除器成功。" );
                    }
                    else
                    {
                        m_WebRtcAecmPt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc定点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        break Out;
                    }
                    break;
                }
                case 3: //如果要使用WebRtc浮点版声学回音消除器。
                {
                    if( m_WebRtcAecPt.m_IsSaveMemFile != 0 ) //如果WebRtc浮点版声学回音消除器要保存内存块到文件。
                    {
                        m_WebRtcAecPt.m_Pt = new HeavenTao.Ado.WebRtcAec();
                        if( m_WebRtcAecPt.m_Pt.InitByMemFile( m_SmplRate, m_FrmLenUnit, m_WebRtcAecPt.m_EchoMode, m_WebRtcAecPt.m_Delay, m_WebRtcAecPt.m_IsUseDelayAgstcMode, m_WebRtcAecPt.m_IsUseExtdFilterMode, m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode, m_WebRtcAecPt.m_IsUseAdaptAdjDelay, m_WebRtcAecPt.m_MemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecPt.m_MemFileFullPathStrPt + " 来初始化WebRtc浮点版声学回音消除器成功。" );
                        }
                        else
                        {
                            m_WebRtcAecPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecPt.m_MemFileFullPathStrPt + " 来初始化WebRtc浮点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        }
                    }
                    if( m_WebRtcAecPt.m_Pt == null )
                    {
                        m_WebRtcAecPt.m_Pt = new HeavenTao.Ado.WebRtcAec();
                        if( m_WebRtcAecPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_WebRtcAecPt.m_EchoMode, m_WebRtcAecPt.m_Delay, m_WebRtcAecPt.m_IsUseDelayAgstcMode, m_WebRtcAecPt.m_IsUseExtdFilterMode, m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode, m_WebRtcAecPt.m_IsUseAdaptAdjDelay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc浮点版声学回音消除器成功。" );
                        }
                        else
                        {
                            m_WebRtcAecPt.m_Pt = null;
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc浮点版声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                            break Out;
                        }
                    }
                    break;
                }
                case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                {
                    m_SpeexWebRtcAecPt.m_Pt = new HeavenTao.Ado.SpeexWebRtcAec();
                    if( m_SpeexWebRtcAecPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_SpeexWebRtcAecPt.m_WorkMode, m_SpeexWebRtcAecPt.m_SpeexAecFilterLenMsec, m_SpeexWebRtcAecPt.m_SpeexAecIsUseRec, m_SpeexWebRtcAecPt.m_SpeexAecEchoMutp, m_SpeexWebRtcAecPt.m_SpeexAecEchoCntu, m_SpeexWebRtcAecPt.m_SpeexAecEchoSupes, m_SpeexWebRtcAecPt.m_SpeexAecEchoSupesAct, m_SpeexWebRtcAecPt.m_WebRtcAecmIsUseCNGMode, m_SpeexWebRtcAecPt.m_WebRtcAecmEchoMode, m_SpeexWebRtcAecPt.m_WebRtcAecmDelay, m_SpeexWebRtcAecPt.m_WebRtcAecEchoMode, m_SpeexWebRtcAecPt.m_WebRtcAecDelay, m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode, m_SpeexWebRtcAecPt.m_WebRtcAecIsUseExtdFilterMode, m_SpeexWebRtcAecPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_SpeexWebRtcAecPt.m_WebRtcAecIsUseAdaptAdjDelay, m_SpeexWebRtcAecPt.m_IsUseSameRoomAec, m_SpeexWebRtcAecPt.m_SameRoomEchoMinDelay, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化SpeexWebRtc三重声学回音消除器成功。" );
                    }
                    else
                    {
                        m_SpeexWebRtcAecPt.m_Pt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化SpeexWebRtc三重声学回音消除器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
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

    //销毁音频输入的声学回音消除器。
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
                if( m_SpeexAecPt.m_Pt != null )
                {
                    if( m_SpeexAecPt.m_IsSaveMemFile != 0 ) //如果Speex声学回音消除器要保存内存块到文件。
                    {
                        if( m_SpeexAecPt.m_Pt.SaveMemFile( m_SmplRate, m_FrmLenUnit, m_SpeexAecPt.m_FilterLenMsec, m_SpeexAecPt.m_IsUseRec, m_SpeexAecPt.m_EchoMutp, m_SpeexAecPt.m_EchoCntu, m_SpeexAecPt.m_EchoSupes, m_SpeexAecPt.m_EchoSupesAct, m_SpeexAecPt.m_MemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecPt.m_MemFileFullPathStrPt + " 成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecPt.m_MemFileFullPathStrPt + " 失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        }
                    }
                    if( m_SpeexAecPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex声学回音消除器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex声学回音消除器失败。" );
                    }
                    m_SpeexAecPt.m_Pt = null;
                }
                break;
            }
            case 2: //如果要使用WebRtc定点版声学回音消除器。
            {
                if( m_WebRtcAecmPt.m_Pt != null )
                {
                    if( m_WebRtcAecmPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc定点版声学回音消除器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc定点版声学回音消除器失败。" );
                    }
                    m_WebRtcAecmPt.m_Pt = null;
                }
                break;
            }
            case 3: //如果要使用WebRtc浮点版声学回音消除器。
            {
                if( m_WebRtcAecPt.m_Pt != null )
                {
                    if( m_WebRtcAecPt.m_IsSaveMemFile != 0 ) //如果WebRtc浮点版声学回音消除器要保存内存块到文件。
                    {
                        if( m_WebRtcAecPt.m_Pt.SaveMemFile( m_SmplRate, m_FrmLenUnit, m_WebRtcAecPt.m_EchoMode, m_WebRtcAecPt.m_Delay, m_WebRtcAecPt.m_IsUseDelayAgstcMode, m_WebRtcAecPt.m_IsUseExtdFilterMode, m_WebRtcAecPt.m_IsUseRefinedFilterAdaptAecMode, m_WebRtcAecPt.m_IsUseAdaptAdjDelay, m_WebRtcAecPt.m_MemFileFullPathStrPt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecPt.m_MemFileFullPathStrPt + " 成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecPt.m_MemFileFullPathStrPt + " 失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        }
                    }
                    if( m_WebRtcAecPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc浮点版声学回音消除器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc浮点版声学回音消除器失败。" );
                    }
                    m_WebRtcAecPt.m_Pt = null;
                }
                break;
            }
            case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
            {
                if( m_SpeexWebRtcAecPt.m_Pt != null )
                {
                    if( m_SpeexWebRtcAecPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁SpeexWebRtc三重声学回音消除器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁SpeexWebRtc三重声学回音消除器失败。" );
                    }
                    m_SpeexWebRtcAecPt.m_Pt = null;
                }
                break;
            }
        }
    }

    //设置音频输入是否可以使用声学回音消除器。
    void SetIsCanUseAec()
    {
        if( m_UseWhatAec != 0 ) //如果要使用声学回音消除器。
        {
            if( m_MediaPocsThrdPt.m_AdoInptPt.m_IsUse == 0 )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为不使用音频输入，所以不可以使用声学回音消除器。" );
                m_IsCanUseAec = 0;
            }
            else if( m_MediaPocsThrdPt.m_AdoOtptPt.m_IsUse == 0 )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为不使用音频输出，所以不可以使用声学回音消除器。" );
                m_IsCanUseAec = 0;
            }
            else if( m_MediaPocsThrdPt.m_AdoOtptPt.m_SmplRate != m_SmplRate )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为音频输出的采样频率与音频输入不一致，所以不可以使用声学回音消除器。" );
                m_IsCanUseAec = 0;
            }
            else if( ( m_MediaPocsThrdPt.m_AdoOtptPt.m_FrmLenMsec != m_FrmLenMsec ) || ( m_MediaPocsThrdPt.m_AdoOtptPt.m_FrmLenUnit != m_FrmLenUnit ) )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为音频输出帧的长度与音频输入不一致，所以不可以使用声学回音消除器。" );
                m_IsCanUseAec = 0;
            }
            else
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为要使用声学回音消除器，所以可以使用声学回音消除器。" );
                m_IsCanUseAec = 1;
            }
        }
        else //如果不使用声学回音消除器。
        {
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：因为不使用声学回音消除器，所以不可以使用声学回音消除器。" );
            m_IsCanUseAec = 0;
        }
    }

    //初始化音频输入的噪音抑制器。
    public int NsInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            switch( m_UseWhatNs )
            {
                case 0: //如果不使用噪音抑制器。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：不使用噪音抑制器。" );
                    break;
                }
                case 1: //如果要使用Speex预处理器的噪音抑制。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：在初始化Speex预处理器时一起初始化Speex预处理器的噪音抑制。" );
                    break;
                }
                case 2: //如果要使用WebRtc定点版噪音抑制器。
                {
                    m_WebRtcNsxPt.m_Pt = new HeavenTao.Ado.WebRtcNsx();
                    if( m_WebRtcNsxPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_WebRtcNsxPt.m_PolicyMode, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc定点版噪音抑制器成功。" );
                    }
                    else
                    {
                        m_WebRtcNsxPt.m_Pt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc定点版噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        break Out;
                    }
                    break;
                }
                case 3: //如果要使用WebRtc浮点版噪音抑制器。
                {
                    m_WebRtcNsPt.m_Pt = new HeavenTao.Ado.WebRtcNs();
                    if( m_WebRtcNsPt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_WebRtcNsPt.m_PolicyMode, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc浮点版噪音抑制器成功。" );
                    }
                    else
                    {
                        m_WebRtcNsPt.m_Pt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化WebRtc浮点版噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                        break Out;
                    }
                    break;
                }
                case 4: //如果要使用RNNoise噪音抑制器。
                {
                    m_RNNoisePt.m_Pt = new HeavenTao.Ado.RNNoise();
                    if( m_RNNoisePt.m_Pt.Init( m_SmplRate, m_FrmLenUnit, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化RNNoise噪音抑制器成功。" );
                    }
                    else
                    {
                        m_RNNoisePt.m_Pt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化RNNoise噪音抑制器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
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

    //销毁音频输入的噪音抑制器。
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
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：在销毁Speex预处理器时一起销毁Speex预处理器的噪音抑制。" );
                break;
            }
            case 2: //如果要使用WebRtc定点版噪音抑制器。
            {
                if( m_WebRtcNsxPt.m_Pt != null )
                {
                    if( m_WebRtcNsxPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc定点版噪音抑制器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc定点版噪音抑制器失败。" );
                    }
                    m_WebRtcNsxPt.m_Pt = null;
                }
                break;
            }
            case 3: //如果要使用WebRtc浮点版噪音抑制器。
            {
                if( m_WebRtcNsPt.m_Pt != null )
                {
                    if( m_WebRtcNsPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc浮点版噪音抑制器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁WebRtc浮点版噪音抑制器失败。" );
                    }
                    m_WebRtcNsPt.m_Pt = null;
                }
                break;
            }
            case 4: //如果要使用RNNoise噪音抑制器。
            {
                if( m_RNNoisePt.m_Pt != null )
                {
                    if( m_RNNoisePt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁RNNoise噪音抑制器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁RNNoise噪音抑制器失败。" );
                    }
                    m_RNNoisePt.m_Pt = null;
                }
                break;
            }
        }
    }

    //初始化音频输入的Speex预处理器。
    public int SpeexPrpocsInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( ( m_UseWhatNs == 1 ) || ( m_SpeexPrpocsPt.m_IsUseSpeexPrpocs != 0 ) )
            {
                m_SpeexPrpocsPt.m_Pt = new HeavenTao.Ado.SpeexPrpocs();
                if( m_SpeexPrpocsPt.m_Pt.Init(
                        m_SmplRate,
                        m_FrmLenUnit,
                        ( m_UseWhatNs == 1 ) ? m_SpeexPrpocsNsPt.m_IsUseNs : 0,
                        m_SpeexPrpocsNsPt.m_NoiseSupes,
                        ( m_UseWhatNs == 1 ) ? m_SpeexPrpocsNsPt.m_IsUseDereverb : 0,
                        ( m_SpeexPrpocsPt.m_IsUseSpeexPrpocs != 0 ) ? m_SpeexPrpocsPt.m_IsUseVad : 0,
                        m_SpeexPrpocsPt.m_VadProbStart,
                        m_SpeexPrpocsPt.m_VadProbCntu,
                        ( m_SpeexPrpocsPt.m_IsUseSpeexPrpocs != 0 ) ? m_SpeexPrpocsPt.m_IsUseAgc : 0,
                        m_SpeexPrpocsPt.m_AgcLevel,
                        m_SpeexPrpocsPt.m_AgcIncrement,
                        m_SpeexPrpocsPt.m_AgcDecrement,
                        m_SpeexPrpocsPt.m_AgcMaxGain,
                        m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex预处理器成功。" );
                }
                else
                {
                    m_SpeexPrpocsPt.m_Pt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex预处理器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
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

    //销毁音频输入的Speex预处理器。
    public void SpeexPrpocsDstoy()
    {
        if( m_SpeexPrpocsPt.m_Pt != null )
        {
            if( m_SpeexPrpocsPt.m_Pt.Dstoy() == 0 )
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex预处理器成功。" );
            }
            else
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex预处理器失败。" );
            }
            m_SpeexPrpocsPt.m_Pt = null;
        }
    }

    //初始化音频输入的编码器。
    public int EncdInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            switch( m_UseWhatEncd )
            {
                case 0: //如果要使用PCM原始数据。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化PCM原始数据成功。" );
                    break;
                }
                case 1: //如果要使用Speex编码器。
                {
                    if( m_FrmLenMsec != 20 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：帧的长度不为20毫秒不能使用Speex编码器。" );
                        break Out;
                    }
                    m_SpeexEncdPt.m_Pt = new HeavenTao.Ado.SpeexEncd();
                    if( m_SpeexEncdPt.m_Pt.Init( m_SmplRate, m_SpeexEncdPt.m_UseCbrOrVbr, m_SpeexEncdPt.m_Qualt, m_SpeexEncdPt.m_Cmplxt, m_SpeexEncdPt.m_PlcExptLossRate ) == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex编码器成功。" );
                    }
                    else
                    {
                        m_SpeexEncdPt.m_Pt = null;
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Speex编码器失败。" );
                        break Out;
                    }
                    break;
                }
                case 2: //如果要使用Opus编码器。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：暂不支持使用Opus编码器。" );
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

    //销毁音频输入的编码器。
    public void EncdDstoy()
    {
        switch( m_UseWhatEncd )
        {
            case 0: //如果要使用PCM原始数据。
            {
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁PCM原始数据成功。" );
                break;
            }
            case 1: //如果要使用Speex编码器。
            {
                if( m_SpeexEncdPt.m_Pt != null )
                {
                    if( m_SpeexEncdPt.m_Pt.Dstoy() == 0 )
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex编码器成功。" );
                    }
                    else
                    {
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Speex编码器失败。" );
                    }
                    m_SpeexEncdPt.m_Pt = null;
                }
                break;
            }
            case 2: //如果要使用Opus编码器。
            {
                break;
            }
        }
    }

    //初始化音频输入的波形器。
    public int WavfmInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_WavfmPt.m_IsDraw != 0 )
            {
                m_WavfmPt.m_SrcPt = new AdoWavfm();
                if( m_WavfmPt.m_SrcPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化原始波形器成功。" );
                }
                else
                {
                    m_WavfmPt.m_SrcPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                    break Out;
                }
                m_WavfmPt.m_RsltPt = new AdoWavfm();
                if( m_WavfmPt.m_RsltPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化结果波形器成功。" );
                }
                else
                {
                    m_WavfmPt.m_RsltPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化结果波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
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

    //销毁音频输入的波形器。
    public void WavfmDstoy()
    {
        if( m_WavfmPt.m_IsDraw != 0 )
        {
            if( m_WavfmPt.m_SrcPt != null )
            {
                if( m_WavfmPt.m_SrcPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁原始波形器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                }
                m_WavfmPt.m_SrcPt = null;
            }
            if( m_WavfmPt.m_RsltPt != null )
            {
                if( m_WavfmPt.m_RsltPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁结果波形器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁结果波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
                }
                m_WavfmPt.m_RsltPt = null;
            }
        }
    }

    //初始化音频输入的Wave文件写入器。
    public int WaveFileWriterInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_WaveFileWriterPt.m_IsSave != 0 )
            {
                m_WaveFileWriterPt.m_SrcPt = new HeavenTao.Media.WaveFileWriter();
                if( m_WaveFileWriterPt.m_SrcPt.Init( m_WaveFileWriterPt.m_SrcFullPathStrPt, m_WaveFileWriterPt.m_WrBufSzByt, ( short ) 1, m_SmplRate, 16 ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化原始Wave文件 " + m_WaveFileWriterPt.m_SrcFullPathStrPt + " 写入器成功。" );
                }
                else
                {
                    m_WaveFileWriterPt.m_SrcPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化原始Wave文件 " + m_WaveFileWriterPt.m_SrcFullPathStrPt + " 写入器失败。" );
                    break Out;
                }
                m_WaveFileWriterPt.m_RsltPt = new HeavenTao.Media.WaveFileWriter();
                if( m_WaveFileWriterPt.m_RsltPt.Init( m_WaveFileWriterPt.m_RsltFullPathStrPt, m_WaveFileWriterPt.m_WrBufSzByt, ( short ) 1, m_SmplRate, 16 ) == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化结果Wave文件 " + m_WaveFileWriterPt.m_RsltFullPathStrPt + " 写入器成功。" );
                }
                else
                {
                    m_WaveFileWriterPt.m_RsltPt = null;
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化结果Wave文件 " + m_WaveFileWriterPt.m_RsltFullPathStrPt + " 写入器失败。" );
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

    //销毁音频输入的Wave文件写入器。
    public void WaveFileWriterDstoy()
    {
        if( m_WaveFileWriterPt.m_IsSave != 0 )
        {
            if( m_WaveFileWriterPt.m_SrcPt != null )
            {
                if( m_WaveFileWriterPt.m_SrcPt.Dstoy() == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁原始Wave文件写入器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁原始Wave文件写入器失败。" );
                }
                m_WaveFileWriterPt.m_SrcPt = null;
            }
            if( m_WaveFileWriterPt.m_RsltPt != null )
            {
                if( m_WaveFileWriterPt.m_RsltPt.Dstoy() == 0 )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁结果Wave文件写入器成功。" );
                }
                else
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁结果Wave文件写入器失败。" );
                }
                m_WaveFileWriterPt.m_RsltPt = null;
            }
        }
    }

    //初始化音频输入的设备和线程。
    public int DvcAndThrdInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            //初始化设备。
            try
            {
                m_DvcPt.m_BufSzByt = AudioRecord.getMinBufferSize( m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                m_DvcPt.m_BufSzByt = ( m_DvcPt.m_BufSzByt > ( int )m_FrmLenByt ) ? m_DvcPt.m_BufSzByt : ( int )m_FrmLenByt;
                if( ActivityCompat.checkSelfPermission( MediaPocsThrd.m_MainActivityPt, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED )
                {
                    String p_InfoStrPt = "媒体处理线程：音频输入：初始化设备失败。原因：没有RECORD_AUDIO权限。";
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
                    if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                    break Out;
                }
                m_DvcPt.m_Pt = new AudioRecord(
                        ( m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
                        m_SmplRate,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        m_DvcPt.m_BufSzByt );
                if( m_DvcPt.m_Pt.getState() == AudioRecord.STATE_INITIALIZED )
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化设备成功。采样频率：" + m_SmplRate + "，缓冲区大小：" + m_DvcPt.m_BufSzByt + "。" );
                }
                else
                {
                    String p_InfoStrPt = "媒体处理线程：音频输入：初始化设备失败。";
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
                    if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                    break Out;
                }
            }
            catch( IllegalArgumentException e )
            {
                String p_InfoStrPt = "媒体处理线程：音频输入：初始化设备失败。原因：" + e.getMessage();
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
                if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                break Out;
            }

            //初始化Pcm格式原始帧容器。
            m_PcmSrcFrmCntnrPt = new ConcurrentLinkedQueue<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Pcm格式原始帧容器成功。" );

            //初始化Pcm格式空闲帧容器。
            m_PcmIdleFrmCntnrPt = new ConcurrentLinkedQueue<>();
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化Pcm格式空闲帧容器成功。" );

            //初始化线程的临时变量。
            {
                m_ThrdPt.m_IsInitThrdTmpVar = 1; //设置已初始化线程的临时变量。
                m_ThrdPt.m_PcmSrcFrmPt = null; //初始化Pcm格式原始帧的指针。
                m_ThrdPt.m_ElmTotal = 0; //初始化元素总数。
                m_ThrdPt.m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
                m_ThrdPt.m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化线程的临时变量成功。" );
            }

            //初始化线程。
            {
                m_ThrdPt.m_IsStartAdoOtptThrd = 0; //设置未开始音频输出线程。
                m_ThrdPt.m_ExitFlag = 0; //设置退出标记为0表示保持运行。
                m_ThrdPt.m_ThrdPt = new AdoInptThrd();
                m_ThrdPt.m_ThrdPt.start();
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化线程成功。" );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        //if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //销毁音频输入的设备和线程。
    public void DvcAndThrdDstoy()
    {
        //销毁线程。
        if( m_ThrdPt.m_ThrdPt != null )
        {
            m_ThrdPt.m_ExitFlag = 1; //请求线程退出。
            try
            {
                m_ThrdPt.m_ThrdPt.join(); //等待线程退出。
            }
            catch( InterruptedException ignored )
            {
            }
            m_ThrdPt.m_ThrdPt = null;
            m_ThrdPt.m_ExitFlag = 0;
            m_ThrdPt.m_IsStartAdoOtptThrd = 0;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁线程成功。" );
        }

        //销毁线程的临时变量。
        if( m_ThrdPt.m_IsInitThrdTmpVar != 0 )
        {
            m_ThrdPt.m_IsInitThrdTmpVar = 0; //设置未初始化线程的临时变量。
            m_ThrdPt.m_PcmSrcFrmPt = null; //销毁Pcm格式原始帧的指针。
            m_ThrdPt.m_ElmTotal = 0; //销毁元素总数。
            m_ThrdPt.m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
            m_ThrdPt.m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁线程的临时变量成功。" );
        }

        //销毁Pcm格式空闲帧容器。
        if( m_PcmIdleFrmCntnrPt != null )
        {
            m_PcmIdleFrmCntnrPt.clear();
            m_PcmIdleFrmCntnrPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Pcm格式空闲帧容器成功。" );
        }

        //销毁Pcm格式原始帧容器。
        if( m_PcmSrcFrmCntnrPt != null )
        {
            m_PcmSrcFrmCntnrPt.clear();
            m_PcmSrcFrmCntnrPt = null;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁Pcm格式原始帧容器成功。" );
        }

        //销毁设备。
        if( m_DvcPt.m_Pt != null )
        {
            if( m_DvcPt.m_Pt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_DvcPt.m_Pt.stop();
            m_DvcPt.m_Pt.release();
            m_DvcPt.m_Pt = null;
            m_DvcPt.m_BufSzByt = 0;
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁设备成功。" );
        }
    }

    //初始化音频输入。
    public int Init()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        long p_LastTickMsec = 0;
        long p_NowTickMsec = 0;

        Out:
        {
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis(); //记录初始化开始的嘀嗒钟。

            if( AecInit() != 0 ) break Out;
            if( NsInit() != 0 ) break Out;
            if( SpeexPrpocsInit() != 0 ) break Out;
            if( EncdInit() != 0 ) break Out;
            if( WaveFileWriterInit() != 0 ) break Out;
            if( WavfmInit() != 0 ) break Out;
            if( DvcAndThrdInit() != 0 ) break Out;

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
            {
                p_NowTickMsec = SystemClock.uptimeMillis(); //记录初始化结束的嘀嗒钟。
                Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：初始化耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
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
        long p_LastTickMsec = 0;
        long p_NowTickMsec = 0;

        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis(); //记录初始化开始的嘀嗒钟。

        DvcAndThrdDstoy();
        WavfmDstoy();
        WaveFileWriterDstoy();
        EncdDstoy();
        SpeexPrpocsDstoy();
        NsDstoy();
        AecDstoy();

        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
        {
            p_NowTickMsec = SystemClock.uptimeMillis(); //记录初始化结束的嘀嗒钟。
            Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输入：销毁耗时 " + ( p_NowTickMsec - p_LastTickMsec ) + " 毫秒。" );
        }
    }

    //音频输入线程。
    public class AdoInptThrd extends Thread
    {
        public void run()
        {
            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

            if( m_IsCanUseAec != 0 ) //如果可以使用声学回音消除器，就自适应计算回音延迟，并设置到声学回音消除器。放在音频输入线程中计算，可以减少媒体处理线程的初始化时间。
            {
                int p_AdoOtptDelay = -10; //存放音频输出延迟。播放的最后一个10ms空的音频输出帧不算音频输出延迟，因为是多写进去的。
                int p_AdoInptDelay = 0; //存放音频输入延迟。
                int p_Delay; //存放回音延迟，单位为毫秒。
                HTInt p_HTIntDelay = new HTInt();

                //计算音频输出的延迟。
                m_MediaPocsThrdPt.m_AdoOtptPt.m_DvcPt.m_Pt.play(); //让音频输出设备开始播放。
                m_ThrdPt.m_PcmSrcFrmPt = new short[ m_MediaPocsThrdPt.m_AdoOtptPt.m_SmplRate / 1000 * 10 ]; //创建一个10ms空的音频输出帧。
                m_ThrdPt.m_LastTickMsec = SystemClock.uptimeMillis();
                while( true )
                {
                    m_MediaPocsThrdPt.m_AdoOtptPt.m_DvcPt.m_Pt.write( m_ThrdPt.m_PcmSrcFrmPt, 0, m_ThrdPt.m_PcmSrcFrmPt.length ); //播放一个空的音频输出帧。
                    m_ThrdPt.m_NowTickMsec = SystemClock.uptimeMillis();
                    p_AdoOtptDelay += 10; //递增音频输出延迟。
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "本次音频输出耗时 " + ( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec ) + " 毫秒，音频输出延迟 " + p_AdoOtptDelay + " 毫秒。" );
                    if( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec >= 10 ) //如果播放耗时较长，就表示音频输出设备的缓冲区已经写满，结束计算。
                    {
                        break;
                    }
                    m_ThrdPt.m_LastTickMsec = m_ThrdPt.m_NowTickMsec;
                }
                m_ThrdPt.m_PcmSrcFrmPt = null;
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟 " + p_AdoOtptDelay + " 毫秒。" );

                //计算音频输入的延迟。
                m_DvcPt.m_Pt.startRecording(); //让音频输入设备开始录音。
                p_AdoInptDelay = 0; //音频输入延迟不方便计算，调用耗时在不同的设备都不一样，可能为0也可能很高，也数据不一定为全0，所以直接认定音频输入延迟为0ms。
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟 " + p_AdoInptDelay + " 毫秒。" );

                //计算回音延迟。
                p_Delay = p_AdoOtptDelay + p_AdoInptDelay;
                if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：" + "回音延迟 " + p_Delay + " 毫秒，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

                m_ThrdPt.m_IsStartAdoOtptThrd = 1; //设置已开始音频输出线程。在开始音频输出线程前设置，这样可以保证不会误判断。
                m_MediaPocsThrdPt.m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart = 1; //设置音频输出线程已开始。

                //设置到WebRtc定点版和浮点版声学回音消除器。
                if( ( m_WebRtcAecmPt.m_Pt != null ) && ( m_WebRtcAecmPt.m_Pt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc定点版声学回音消除器，且需要自适应设置回音延迟。
                {
                    m_WebRtcAecmPt.m_Pt.SetDelay( p_Delay / 2 ); //WebRtc定点版声学回音消除器的回音延迟应为实际回音延迟的二分之一，这样效果最好。
                    m_WebRtcAecmPt.m_Pt.GetDelay( p_HTIntDelay );
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_WebRtcAecPt.m_Pt != null ) && ( m_WebRtcAecPt.m_Pt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc浮点版声学回音消除器，且需要自适应设置回音延迟。
                {
                    if( m_WebRtcAecPt.m_IsUseDelayAgstcMode == 0 ) //如果WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_WebRtcAecPt.m_Pt.SetDelay( p_Delay );
                        m_WebRtcAecPt.m_Pt.GetDelay( p_HTIntDelay );
                    }
                    else //如果WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_WebRtcAecPt.m_Pt.SetDelay( 20 );
                        m_WebRtcAecPt.m_Pt.GetDelay( p_HTIntDelay );
                    }
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_SpeexWebRtcAecPt.m_Pt != null ) && ( m_SpeexWebRtcAecPt.m_Pt.GetWebRtcAecmDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc定点版声学回音消除器需要自适应设置回音延迟。
                {
                    m_SpeexWebRtcAecPt.m_Pt.SetWebRtcAecmDelay( p_Delay / 2 ); //设置WebRtc定点版声学回音消除器的回音延迟为实际回音延迟的二分之一，这样效果最好。
                    m_SpeexWebRtcAecPt.m_Pt.GetWebRtcAecmDelay( p_HTIntDelay );
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_SpeexWebRtcAecPt.m_Pt != null ) && ( m_SpeexWebRtcAecPt.m_Pt.GetWebRtcAecDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音延迟。
                {
                    if( m_SpeexWebRtcAecPt.m_WebRtcAecIsUseDelayAgstcMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_SpeexWebRtcAecPt.m_Pt.SetWebRtcAecDelay( p_Delay );
                        m_SpeexWebRtcAecPt.m_Pt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_SpeexWebRtcAecPt.m_Pt.SetWebRtcAecDelay( 20 );
                        m_SpeexWebRtcAecPt.m_Pt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
            } //自适应设置回音延迟完毕。
            else //如果不使用音频输入的声学回音消除，就直接启动音频输出线程。
            {
                m_DvcPt.m_Pt.startRecording(); //让音频输入设备开始录音。
                m_ThrdPt.m_IsStartAdoOtptThrd = 1; //设置已开始音频输出线程。在开始音频输出线程前设置，这样可以保证不会误判断。
                if( m_MediaPocsThrdPt.m_AdoOtptPt.m_IsInit != 0 ) //如果已初始化音频输出。
                {
                    m_MediaPocsThrdPt.m_AdoOtptPt.m_DvcPt.m_Pt.play(); //让音频输出设备开始播放。
                    m_MediaPocsThrdPt.m_AdoOtptPt.m_ThrdPt.m_ThrdIsStart = 1; //设置音频输出线程已开始。
                }
            }

            //音频输入循环开始。
            while( true )
            {
                OutPocs:
                {
                    //获取一个Pcm格式空闲帧。
                    m_ThrdPt.m_ElmTotal = m_PcmIdleFrmCntnrPt.size(); //获取Pcm格式空闲帧容器的元素总数。
                    if( m_ThrdPt.m_ElmTotal > 0 ) //如果Pcm格式空闲帧容器中有帧。
                    {
                        m_ThrdPt.m_PcmSrcFrmPt = m_PcmIdleFrmCntnrPt.poll(); //从Pcm格式空闲帧容器中取出并删除第一个帧。
                        if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：从Pcm格式空闲帧容器中取出并删除第一个帧，Pcm格式空闲帧容器元素总数：" + m_ThrdPt.m_ElmTotal + "。" );
                    }
                    else //如果Pcm格式空闲帧容器中没有帧。
                    {
                        m_ThrdPt.m_ElmTotal = m_PcmSrcFrmCntnrPt.size(); //获取Pcm格式原始帧容器的元素总数。
                        if( m_ThrdPt.m_ElmTotal <= 50 )
                        {
                            m_ThrdPt.m_PcmSrcFrmPt = new short[ ( int )m_FrmLenUnit ]; //创建一个Pcm格式空闲帧。
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：Pcm格式空闲帧容器中没有帧，创建一个Pcm格式空闲帧成功。" );
                        }
                        else
                        {
                            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：Pcm格式原始帧容器中帧总数为" + m_ThrdPt.m_ElmTotal + "已经超过上限50，不再创建Pcm格式空闲帧。" );
                            SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                            break OutPocs;
                        }
                    }

                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) m_ThrdPt.m_LastTickMsec = SystemClock.uptimeMillis();

                    //读取本次Pcm格式原始帧。
                    m_DvcPt.m_Pt.read( m_ThrdPt.m_PcmSrcFrmPt, 0, m_ThrdPt.m_PcmSrcFrmPt.length );

                    //放入本次Pcm格式原始帧到Pcm格式原始帧容器。注意：从取出到放入过程中不能跳出，否则会内存泄露。
                    {
                        m_PcmSrcFrmCntnrPt.offer( m_ThrdPt.m_PcmSrcFrmPt );
                        m_ThrdPt.m_PcmSrcFrmPt = null;
                    }

                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
                    {
                        m_ThrdPt.m_NowTickMsec = SystemClock.uptimeMillis();
                        Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：本次帧处理完毕，耗时 " + ( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec ) + " 毫秒。" );
                    }
                }

                if( m_ThrdPt.m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：接收到退出请求，开始准备退出。" );
                    break;
                }
            } //音频输入循环结束。

            if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输入线程：本线程已退出。" );
        }
    }
}