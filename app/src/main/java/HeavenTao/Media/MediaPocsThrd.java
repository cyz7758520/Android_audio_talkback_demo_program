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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import HeavenTao.Audio.*;
import HeavenTao.Video.*;
import HeavenTao.Data.*;
import HeavenTao.Media.*;

//媒体处理线程类。
public abstract class MediaPocsThrd extends Thread
{
    public static String m_CurClsNameStrPt = "MediaPocsThrd"; //存放当前类名称字符串。

    public int m_RunFlag; //存放本线程运行标记。
    public static final int RUN_FLAG_NORUN = 0; //运行标记：未开始运行。
    public static final int RUN_FLAG_INIT = 1; //运行标记：刚开始运行正在初始化。
    public static final int RUN_FLAG_PROC = 2; //运行标记：初始化完毕正在循环处理帧。
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

    public class AudioInput //音频输入类。
    {
        public int m_IsUseAudioInput; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。

        public int m_SamplingRate; //存放采样频率，取值只能为8000、16000、32000、48000。
        public int m_FrameLen; //存放帧的长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。

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
        int m_WebRtcAecIsUseDelayAgnosticMode; //存放WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
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
        int m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
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

        public int m_UseWhatEncoder; //存放使用什么编码器，为0表示PCM原始数据，为1表示Speex编码器，为2表示Opus编码器。

        SpeexEncoder m_SpeexEncoderPt; //存放Speex编码器的指针。
        int m_SpeexEncoderUseCbrOrVbr; //存放Speex编码器使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
        int m_SpeexEncoderQualt; //存放Speex编码器的编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
        int m_SpeexEncoderCmplxt; //存放Speex编码器的编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
        int m_SpeexEncoderPlcExpectedLossRate; //存放Speex编码器在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。

        public int m_IsSaveAudioToFile; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
        WaveFileWriter m_AudioInputWaveFileWriterPt; //存放音频输入Wave文件写入器对象的指针。
        WaveFileWriter m_AudioResultWaveFileWriterPt; //存放音频结果Wave文件写入器对象的指针。
        String m_AudioInputFileFullPathStrPt; //存放音频输入文件的完整路径字符串的指针。
        String m_AudioResultFileFullPathStrPt; //存放音频结果文件的完整路径字符串的指针。

        public int m_IsDrawAudioOscilloToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
        SurfaceView m_AudioInputOscilloSurfacePt; //存放音频输入波形Surface对象的指针。
        AudioOscillo m_AudioInputOscilloPt; //存放音频输入波形器对象的指针。
        SurfaceView m_AudioResultOscilloSurfacePt; //存放音频结果波形Surface对象的指针。
        AudioOscillo m_AudioResultOscilloPt; //存放音频结果波形器对象的指针。

        AudioRecord m_AudioInputDvcPt; //存放音频输入设备的指针。
        int m_AudioInputDvcBufSz; //存放音频输入设备缓冲区大小，单位字节。
        int m_AudioInputIsMute; //存放音频输入是否静音，为0表示有声音，为非0表示静音。

        public LinkedList< short[] > m_AudioInputFrameLnkLstPt; //存放音频输入帧链表的指针。
        public LinkedList< short[] > m_AudioInputIdleFrameLnkLstPt; //存放音频输入空闲帧链表的指针。

        //音频输入线程的临时变量。
        short m_AudioInputFramePt[]; //存放音频输入帧的指针。
        int m_AudioInputFrameLnkLstElmTotal; //存放音频输入帧链表的元数总数。
        long m_LastTimeMsec; //存放上次时间的毫秒数。
        long m_NowTimeMsec; //存放本次时间的毫秒数。

        AudioInputThrd m_AudioInputThrdPt; //存放音频输入线程的指针。
        int m_AudioInputThrdExitFlag; //存放音频输入线程退出标记，0表示保持运行，1表示请求退出。
    }

    public AudioInput m_AudioInputPt = new AudioInput(); //存放音频输入的指针。

    public class AudioOutput //音频输出类。
    {
        public int m_IsUseAudioOutput; //存放是否使用音频输出，为0表示不使用，为非0表示要使用。

        public int m_SamplingRate; //存放采样频率，取值只能为8000、16000、32000、48000。
        public int m_FrameLen; //存放帧的长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。

        public int m_UseWhatDecoder; //存放使用什么解码器，为0表示PCM原始数据，为1表示Speex解码器，为2表示Opus解码器。

        SpeexDecoder m_SpeexDecoderPt; //存放Speex解码器的指针。
        int m_SpeexDecoderIsUsePerceptualEnhancement; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

        public int m_IsSaveAudioToFile; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
        WaveFileWriter m_AudioOutputWaveFileWriterPt; //存放音频输出Wave文件写入器对象的指针。
        String m_AudioOutputFileFullPathStrPt; //存放音频输出文件的完整路径字符串。

        public int m_IsDrawAudioOscilloToSurface; //存放是否绘制音频波形到Surface，为非0表示要绘制，为0表示不绘制。
        SurfaceView m_AudioOutputOscilloSurfacePt; //存放音频输出波形Surface对象的指针。
        AudioOscillo m_AudioOutputOscilloPt; //存放音频输出波形器对象的指针。

        public AudioTrack m_AudioOutputDvcPt; //存放音频输出设备的指针。
        int m_AudioOutputDvcBufSz; //存放音频输出设备缓冲区大小，单位字节。
        public int m_UseWhatAudioOutputDvc; //存放使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
        public int m_UseWhatAudioOutputStreamType; //存放使用什么音频输出流类型，为0表示通话类型，为非0表示媒体类型。
        int m_AudioOutputIsMute; //存放音频输出是否静音，为0表示有声音，为非0表示静音。

        public LinkedList< short[] > m_AudioOutputFrameLnkLstPt; //存放音频输出帧链表的指针。
        public LinkedList< short[] > m_AudioOutputIdleFrameLnkLstPt; //存放音频输出空闲帧链表的指针。

        //音频输出线程的临时变量。
        short m_AudioOutputFramePt[]; //存放音频输出帧的指针。
        byte m_EncodedAudioOutputFramePt[]; //存放已编码格式音频输出帧的指针。
        HTLong m_AudioOutputFrameLenPt; //存放音频输出帧的长度，单位字节。
        int m_AudioOutputFrameLnkLstElmTotal; //存放音频输出帧链表的元数总数。
        long m_LastTimeMsec; //存放上次时间的毫秒数。
        long m_NowTimeMsec; //存放本次时间的毫秒数。

        AudioOutputThrd m_AudioOutputThrdPt; //存放音频输出线程的指针。
        int m_AudioOutputThrdExitFlag; //存放音频输出线程退出标记，0表示保持运行，1表示请求退出。
    }

    public AudioOutput m_AudioOutputPt = new AudioOutput(); //存放音频输出的指针。

    public class VideoInput //视频输入类。
    {
        public int m_IsUseVideoInput; //存放是否使用视频输入，为0表示不使用，为非0表示要使用。

        public int m_MaxSamplingRate; //存放最大采样频率，取值范围为[1,60]，实际帧率和图像的亮度有关，亮度较高时采样频率可以达到最大值，亮度较低时系统就自动降低采样频率来提升亮度。
        public int m_FrameWidth; //存放屏幕旋转0度时，帧的宽度，单位为像素。
        public int m_FrameHeight; //存放屏幕旋转0度时，帧的高度，单位为像素。
        public int m_ScreenRotate; //存放屏幕旋转的角度，只能为0、90、180、270，0度表示竖屏，其他表示顺时针旋转。

        public int m_UseWhatEncoder; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264编码器，为2表示系统自带H264编码器。

        OpenH264Encoder m_OpenH264EncoderPt; //存放OpenH264编码器的指针。
        int m_OpenH264EncoderVideoType;//存放OpenH264编码器的视频类型，为0表示实时摄像头视频，为1表示实时屏幕内容视频，为2表示非实时摄像头视频，为3表示非实时屏幕内容视频，为4表示其他视频。
        int m_OpenH264EncoderEncodedBitrate; //存放OpenH264编码器的编码后比特率，单位为bps。
        int m_OpenH264EncoderBitrateControlMode; //存放OpenH264编码器的比特率控制模式，为0表示质量优先模式，为1表示比特率优先模式，为2表示缓冲区优先模式，为3表示时间戳优先模式。
        int m_OpenH264EncoderIDRFrameIntvl; //存放OpenH264编码器的IDR帧间隔帧数，单位为个，为0表示仅第一帧为IDR帧，为大于0表示每隔这么帧就至少有一个IDR帧。
        int m_OpenH264EncoderCmplxt; //存放OpenH264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

        SystemH264Encoder m_SystemH264EncoderPt; //存放系统自带H264编码器的指针。
        int m_SystemH264EncoderEncodedBitrate; //存放系统自带H264编码器的编码后比特率，单位为bps。
        int m_SystemH264EncoderBitrateControlMode; //存放系统自带H264编码器的比特率控制模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ(0x00)表示质量模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR(0x01)表示动态比特率模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR(0x02)表示固定比特率模式。
        int m_SystemH264EncoderIDRFrameIntvlTimeSec; //存放系统自带H264编码器的IDR帧间隔时间，单位为秒，为负数表示仅第一帧为IDR帧，为0表示每一帧都为IDR帧，为大于0表示每这么多秒就有一个IDR帧。
        int m_SystemH264EncoderCmplxt; //存放系统自带H264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

        public Camera m_VideoInputDvcPt; //存放视频输入设备的指针。
        public int m_UseWhatVideoInputDvc; //存放使用什么视频输入设备，为0表示前置摄像头，为1表示后置摄像头。
        int m_FrontCameraDvcId = -1; //存放前置摄像头的设备ID，为-1表示自动查找。
        int m_BackCameraDvcId = -1; //存放后置摄像头的设备ID，为-1表示自动查找。
        public HTSurfaceView m_VideoInputPreviewSurfaceViewPt; //存放视频输入预览SurfaceView的指针。
        public byte m_VideoInputPreviewCallbackBufferPtPt[][]; //存放视频输入预览回调函数缓冲区的指针。
        int m_VideoInputDvcFrameWidth; //存放视频输入设备帧的宽度，单位为像素。
        int m_VideoInputDvcFrameHeight; //存放视频输入设备帧的高度，单位为像素。
        int m_VideoInputDvcFrameIsCrop; //存放视频输入设备帧是否裁剪，为0表示不裁剪，为非0表示要裁剪。
        int m_VideoInputDvcFrameCropX; //存放视频输入设备帧裁剪区域左上角的横坐标，单位像素。
        int m_VideoInputDvcFrameCropY; //存放视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
        int m_VideoInputDvcFrameCropWidth; //存放视频输入设备帧裁剪区域的宽度，单位像素。
        int m_VideoInputDvcFrameCropHeight; //存放视频输入设备帧裁剪区域的高度，单位像素。
        int m_VideoInputDvcFrameRotate; //存放视频输入设备帧旋转的角度，只能为0、90、180、270，0度表示横屏，其他表示顺时针旋转。
        int m_VideoInputDvcFrameRotateWidth; //存放视频输入设备帧旋转后的宽度，单位为像素。
        int m_VideoInputDvcFrameRotateHeight; //存放视频输入设备帧旋转后的高度，单位为像素。
        int m_VideoInputDvcFrameIsScale; //存放视频输入设备帧是否缩放，为0表示不缩放，为非0表示要缩放。
        public int m_VideoInputDvcFrameScaleWidth; //存放视频输入帧缩放后的宽度，单位为像素。
        public int m_VideoInputDvcFrameScaleHeight; //存放视频输入帧缩放后的高度，单位为像素。
        int m_VideoInputIsBlack; //存放视频输入是否黑屏，为0表示有图像，为非0表示黑屏。

        public class VideoInputFrameElm //视频输入帧链表元素类。
        {
            VideoInputFrameElm()
            {
                m_YU12VideoInputFramePt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new byte[ m_VideoInputPt.m_VideoInputDvcFrameScaleWidth * m_VideoInputPt.m_VideoInputDvcFrameScaleHeight * 3 / 2 ] : null;
                m_YU12VideoInputFrameWidthPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new HTInt() : null;
                m_YU12VideoInputFrameHeightPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new HTInt() : null;
                m_EncodedVideoInputFramePt = ( m_VideoInputPt.m_IsUseVideoInput != 0 && m_VideoInputPt.m_UseWhatEncoder != 0 ) ? new byte[ m_VideoInputPt.m_VideoInputDvcFrameScaleWidth * m_VideoInputPt.m_VideoInputDvcFrameScaleHeight * 3 / 2 ] : null;
                m_EncodedVideoInputFrameLenPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 && m_VideoInputPt.m_UseWhatEncoder != 0 ) ? new HTLong( 0 ) : null;
            }
            byte m_YU12VideoInputFramePt[]; //存放YU12格式视频输入帧的指针。
            HTInt m_YU12VideoInputFrameWidthPt; //存放YU12格式视频输入帧的宽度。
            HTInt m_YU12VideoInputFrameHeightPt; //存放YU12格式视频输入帧的高度。
            byte m_EncodedVideoInputFramePt[]; //存放已编码格式视频输入帧。
            HTLong m_EncodedVideoInputFrameLenPt; //存放已编码格式视频输入帧的长度，单位字节。
        }
        public LinkedList< byte[] > m_NV21VideoInputFrameLnkLstPt; //存放NV21格式视频输入帧链表的指针。
        public LinkedList< VideoInputFrameElm > m_VideoInputFrameLnkLstPt; //存放视频输入帧链表的指针。
        public LinkedList< VideoInputFrameElm > m_VideoInputIdleFrameLnkLstPt; //存放视频输入空闲帧链表的指针。

        //视频输入线程的临时变量。
        byte m_VideoInputFramePt[]; //存放视频输入帧的指针。
        byte m_VideoInputResultFramePt[]; //存放视频输入结果帧的指针。
        byte m_VideoInputTmpFramePt[]; //存放视频输入临时帧的指针。
        byte m_VideoInputSwapFramePt[]; //存放视频输入交换帧的指针。
        long m_VideoInputResultFrameSz; //存放视频输入结果帧的内存大小，单位字节。
        HTLong m_VideoInputResultFrameLenPt; //存放视频输入结果帧的长度，单位字节。
        VideoInputFrameElm m_VideoInputFrameElmPt; //存放视频输入帧元素的指针。
        int m_VideoInputFrameLnkLstElmTotal; //存放视频输入帧链表的元数总数。
        long m_LastTimeMsec; //存放上次时间的毫秒数。
        long m_NowTimeMsec; //存放本次时间的毫秒数。

        VideoInputThrd m_VideoInputThrdPt; //存放视频输入线程的指针。
        int m_VideoInputThrdExitFlag; //存放视频输入线程退出标记，0表示保持运行，1表示请求退出。
    }

    public VideoInput m_VideoInputPt = new VideoInput(); //存放视频输入的指针。

    public class VideoOutput //视频输出类。
    {
        public int m_IsUseVideoOutput; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。

        public int m_UseWhatDecoder; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264解码器，为2表示系统自带H264解码器。

        OpenH264Decoder m_OpenH264DecoderPt; //存放OpenH264解码器的指针。
        int m_OpenH264DecoderDecodeThrdNum; //存放OpenH264解码器的解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。

        SystemH264Decoder m_SystemH264DecoderPt; //存放系统自带H264解码器的指针。

        HTSurfaceView m_VideoOutputDisplaySurfaceViewPt; //存放视频输出显示SurfaceView的指针。
        float m_VideoOutputDisplayScale; //存放视频输出显示缩放倍数，为1.0f表示不缩放。
        int m_VideoOutputIsBlack; //存放视频输出是否黑屏，为0表示有图像，为非0表示黑屏。

        //视频输出线程的临时变量。
        byte m_VideoOutputResultFramePt[]; //存放视频输出结果帧的指针。
        byte m_VideoOutputTmpFramePt[]; //存放视频输出临时帧的指针。
        byte m_VideoOutputSwapFramePt[]; //存放视频输出交换帧的指针。
        HTLong m_VideoOutputResultFrameLenPt; //存放视频输出结果帧的长度，单位字节。
        HTInt m_VideoOutputFrameWidthPt; //存放视频输出帧的宽度，单位为像素。
        HTInt m_VideoOutputFrameHeightPt; //存放视频输出帧的高度，单位为像素。
        long m_LastTimeMsec; //存放上次时间的毫秒数。
        long m_NowTimeMsec; //存放本次时间的毫秒数。

        VideoOutputThrd m_VideoOutputThrdPt; //存放视频输出线程的指针。
        int m_VideoOutputThrdExitFlag; //存放视频输出线程退出标记，0表示保持运行，1表示请求退出。
    }

    public VideoOutput m_VideoOutputPt = new VideoOutput(); //存放视频输出的指针。

    //媒体处理线程的临时变量。
    short m_PcmAudioInputFramePt[] = null; //存放PCM格式音频输入帧的指针。
    short m_PcmAudioOutputFramePt[] = null; //存放PCM格式音频输出帧的指针。
    short m_PcmAudioResultFramePt[] = null; //存放PCM格式音频结果帧的指针。
    short m_PcmAudioTmpFramePt[] = null; //存放PCM格式音频临时帧的指针。
    short m_PcmAudioSwapFramePt[] = null; //存放PCM格式音频交换帧的指针。
    HTInt m_VoiceActStsPt = null; //存放语音活动状态，为1表示有语音活动，为0表示无语音活动。
    byte m_EncodedAudioInputFramePt[] = null; //存放已编码格式音频输入帧的指针。
    HTLong m_EncodedAudioInputFrameLenPt = null; //存放已编码格式音频输入帧的长度，单位字节。
    HTInt m_EncodedAudioInputFrameIsNeedTransPt = null; //存放已编码格式音频输入帧是否需要传输的指针，为1表示需要传输，为0表示不需要传输。
    VideoInput.VideoInputFrameElm m_VideoInputFramePt = null; //存放视频输入帧的指针。

    public VarStr m_ErrInfoVarStrPt; //存放错误信息动态字符串的指针。

    //用户定义的相关回调函数。

    //用户定义的初始化函数，在本线程刚启动时回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public abstract int UserInit();

    //用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。
    public abstract int UserPocs();

    //用户定义的销毁函数，在本线程退出时回调一次。
    public abstract void UserDstoy();

    //用户定义的读取音视频输入帧函数，在读取到一个音频输入帧或视频输入帧并处理完后回调一次，为0表示成功，为非0表示失败。
    public abstract int UserReadAudioVideoInputFrame( short PcmAudioInputFramePt[], short PcmAudioResultFramePt[], HTInt VoiceActStsPt, byte EncodedAudioInputFramePt[], HTLong EncodedAudioInputFrameLenPt, HTInt EncodedAudioInputFrameIsNeedTransPt,
                                                      byte YU12VideoInputFramePt[], HTInt YU12VideoInputFrameWidthPt, HTInt YU12VideoInputFrameHeightPt, byte EncodedVideoInputFramePt[], HTLong EncodedVideoInputFrameLenPt );

    //用户定义的写入音频输出帧函数，在需要写入一个音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。
    public abstract void UserWriteAudioOutputFrame( short PcmAudioOutputFramePt[], byte EncodedAudioOutputFramePt[], HTLong AudioOutputFrameLenPt );

    //用户定义的获取PCM格式音频输出帧函数，在解码完一个已编码音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。
    public abstract void UserGetPcmAudioOutputFrame( short PcmAudioOutputFramePt[], long PcmAudioOutputFrameLen );

    //用户定义的写入视频输出帧函数，在可以显示一个视频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。
    public abstract void UserWriteVideoOutputFrame( byte YU12VideoOutputFramePt[], HTInt YU12VideoInputFrameWidthPt, HTInt YU12VideoInputFrameHeightPt, byte EncodedVideoOutputFramePt[], HTLong VideoOutputFrameLenPt );

    //用户定义的获取YU12格式视频输出帧函数，在解码完一个已编码视频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。
    public abstract void UserGetYU12VideoOutputFrame( byte YU12VideoOutputFramePt[], int YU12VideoOutputFrameWidth, int YU12VideoOutputFrameHeight );

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

    //设置是否打印Logcat日志，并显示Toast。
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

        if( m_RunFlag == RUN_FLAG_INIT || m_RunFlag == RUN_FLAG_PROC ) //如果本线程为刚开始运行正在初始化或初始化完毕正在循环处理帧，就立即修改唤醒锁。
        {
            WakeLockInitOrDstoy( IsUseWakeLock );
        }
    }

    //初始化或销毁唤醒锁。
    void WakeLockInitOrDstoy( int IsInitOrDstoy )
    {
        if( IsInitOrDstoy != 0 ) //如果要初始化唤醒锁。
        {
            if( m_AudioOutputPt.m_IsUseAudioOutput != 0 && m_AudioOutputPt.m_UseWhatAudioOutputDvc != 0 ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
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
    public void SetIsUseAudioInput( int IsUseAudioInput, int SamplingRate, int FrameLenMsec )
    {
        if( ( ( IsUseAudioInput != 0 ) && ( ( SamplingRate != 8000 ) && ( SamplingRate != 16000 ) && ( SamplingRate != 32000 ) && ( SamplingRate != 48000 ) ) ) || //如果采样频率不正确。
            ( ( IsUseAudioInput != 0 ) && ( ( FrameLenMsec <= 0 ) || ( FrameLenMsec % 10 != 0 ) ) ) ) //如果帧的毫秒长度不正确。
        {
            return;
        }

        m_AudioInputPt.m_IsUseAudioInput = IsUseAudioInput;
        m_AudioInputPt.m_SamplingRate = SamplingRate;
        m_AudioInputPt.m_FrameLen = FrameLenMsec * SamplingRate / 1000;
    }

    //设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
    public void SetAudioInputIsUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
    {
        m_AudioInputPt.m_IsUseSystemAecNsAgc = IsUseSystemAecNsAgc;
    }

    //设置音频输入不使用声学回音消除器。
    public void SetAudioInputUseNoAec()
    {
        m_AudioInputPt.m_UseWhatAec = 0;
    }

    //设置音频输入要使用Speex声学回音消除器。
    public void SetAudioInputUseSpeexAec( int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesActive, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        m_AudioInputPt.m_UseWhatAec = 1;
        m_AudioInputPt.m_SpeexAecFilterLen = FilterLen;
        m_AudioInputPt.m_SpeexAecIsUseRec = IsUseRec;
        m_AudioInputPt.m_SpeexAecEchoMultiple = EchoMultiple;
        m_AudioInputPt.m_SpeexAecEchoCont = EchoCont;
        m_AudioInputPt.m_SpeexAecEchoSupes = EchoSupes;
        m_AudioInputPt.m_SpeexAecEchoSupesAct = EchoSupesActive;
        m_AudioInputPt.m_SpeexAecIsSaveMemFile = IsSaveMemFile;
        m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt = MemFileFullPathStrPt;
    }

    //设置音频输入要使用WebRtc定点版声学回音消除器。
    public void SetAudioInputUseWebRtcAecm( int IsUseCNGMode, int EchoMode, int Delay )
    {
        m_AudioInputPt.m_UseWhatAec = 2;
        m_AudioInputPt.m_WebRtcAecmIsUseCNGMode = IsUseCNGMode;
        m_AudioInputPt.m_WebRtcAecmEchoMode = EchoMode;
        m_AudioInputPt.m_WebRtcAecmDelay = Delay;
    }

    //设置音频输入要使用WebRtc浮点版声学回音消除器。
    public void SetAudioInputUseWebRtcAec( int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        m_AudioInputPt.m_UseWhatAec = 3;
        m_AudioInputPt.m_WebRtcAecEchoMode = EchoMode;
        m_AudioInputPt.m_WebRtcAecDelay = Delay;
        m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode = IsUseDelayAgnosticMode;
        m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode = IsUseExtdFilterMode;
        m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode = IsUseRefinedFilterAdaptAecMode;
        m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay = IsUseAdaptAdjDelay;
        m_AudioInputPt.m_WebRtcAecIsSaveMemFile = IsSaveMemFile;
        m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt = MemFileFullPathStrPt;
    }

    //设置音频输入要使用SpeexWebRtc三重声学回音消除器。
    public void SetAudioInputUseSpeexWebRtcAec( int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSuppress, int SpeexAecEchoSuppressActive, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgnosticMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay, int IsUseSameRoomAec, int SameRoomEchoMinDelay )
    {
        m_AudioInputPt.m_UseWhatAec = 4;
        m_AudioInputPt.m_SpeexWebRtcAecWorkMode = WorkMode;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen = SpeexAecFilterLen;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec = SpeexAecIsUseRec;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple = SpeexAecEchoMultiple;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont = SpeexAecEchoCont;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes = SpeexAecEchoSuppress;
        m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct = SpeexAecEchoSuppressActive;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode = WebRtcAecmIsUseCNGMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode = WebRtcAecmEchoMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay = WebRtcAecmDelay;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode = WebRtcAecEchoMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay = WebRtcAecDelay;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode = WebRtcAecIsUseDelayAgnosticMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode = WebRtcAecIsUseExtdFilterMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode = WebRtcAecIsUseRefinedFilterAdaptAecMode;
        m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay = WebRtcAecIsUseAdaptAdjDelay;
        m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec = IsUseSameRoomAec;
        m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay = SameRoomEchoMinDelay;
    }

    //设置音频输入不使用噪音抑制器。
    public void SetAudioInputUseNoNs()
    {
        m_AudioInputPt.m_UseWhatNs = 0;
    }

    //设置音频输入要使用Speex预处理器的噪音抑制。
    public void SetAudioInputUseSpeexPrpocsNs( int IsUseNs, int NoiseSupes, int IsUseDereverberation )
    {
        m_AudioInputPt.m_UseWhatNs = 1;
        m_AudioInputPt.m_SpeexPrpocsIsUseNs = IsUseNs;
        m_AudioInputPt.m_SpeexPrpocsNoiseSupes = NoiseSupes;
        m_AudioInputPt.m_SpeexPrpocsIsUseDereverb = IsUseDereverberation;
    }

    //设置音频输入要使用WebRtc定点版噪音抑制器。
    public void SetAudioInputUseWebRtcNsx( int PolicyMode )
    {
        m_AudioInputPt.m_UseWhatNs = 2;
        m_AudioInputPt.m_WebRtcNsxPolicyMode = PolicyMode;
    }

    //设置音频输入要使用WebRtc浮点版噪音抑制器。
    public void SetAudioInputUseWebRtcNs( int PolicyMode )
    {
        m_AudioInputPt.m_UseWhatNs = 3;
        m_AudioInputPt.m_WebRtcNsPolicyMode = PolicyMode;
    }

    //设置音频输入要使用RNNoise噪音抑制器。
    public void SetAudioInputUseRNNoise()
    {
        m_AudioInputPt.m_UseWhatNs = 4;
    }

    //设置音频输入是否使用Speex预处理器的其他功能。
    public void SetAudioInputIsUseSpeexPrpocsOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
    {
        m_AudioInputPt.m_IsUseSpeexPrpocsOther = IsUseOther;
        m_AudioInputPt.m_SpeexPrpocsIsUseVad = IsUseVad;
        m_AudioInputPt.m_SpeexPrpocsVadProbStart = VadProbStart;
        m_AudioInputPt.m_SpeexPrpocsVadProbCont = VadProbCont;
        m_AudioInputPt.m_SpeexPrpocsIsUseAgc = IsUseAgc;
        m_AudioInputPt.m_SpeexPrpocsAgcIncrement = AgcIncrement;
        m_AudioInputPt.m_SpeexPrpocsAgcDecrement = AgcDecrement;
        m_AudioInputPt.m_SpeexPrpocsAgcLevel = AgcLevel;
        m_AudioInputPt.m_SpeexPrpocsAgcMaxGain = AgcMaxGain;
    }

    //设置音频输入要使用PCM原始数据。
    public void SetAudioInputUsePcm()
    {
        m_AudioInputPt.m_UseWhatEncoder = 0;
    }

    //设置音频输入要使用Speex编码器。
    public void SetAudioInputUseSpeexEncoder( int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExpectedLossRate )
    {
        m_AudioInputPt.m_UseWhatEncoder = 1;
        m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr = UseCbrOrVbr;
        m_AudioInputPt.m_SpeexEncoderQualt = Qualt;
        m_AudioInputPt.m_SpeexEncoderCmplxt = Cmplxt;
        m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate = PlcExpectedLossRate;
    }

    //设置音频输入要使用Opus编码器。
    public void SetAudioInputUseOpusEncoder()
    {
        m_AudioInputPt.m_UseWhatEncoder = 2;
    }

    //设置音频输入是否保存音频到文件。
    public void SetAudioInputIsSaveAudioToFile( int IsSaveAudioToFile, String AudioInputFileFullPathStrPt, String AudioResultFileFullPathStrPt )
    {
        m_AudioInputPt.m_IsSaveAudioToFile = IsSaveAudioToFile;
        m_AudioInputPt.m_AudioInputFileFullPathStrPt = AudioInputFileFullPathStrPt;
        m_AudioInputPt.m_AudioResultFileFullPathStrPt = AudioResultFileFullPathStrPt;
    }

    //设置音频输入是否绘制音频波形到Surface。
    public void SetAudioInputIsDrawAudioOscilloToSurface( int IsDrawAudioOscilloToSurface, SurfaceView AudioInputOscilloSurfacePt, SurfaceView AudioResultOscilloSurfacePt )
    {
        m_AudioInputPt.m_IsDrawAudioOscilloToSurface = IsDrawAudioOscilloToSurface;
        m_AudioInputPt.m_AudioInputOscilloSurfacePt = AudioInputOscilloSurfacePt;
        m_AudioInputPt.m_AudioResultOscilloSurfacePt = AudioResultOscilloSurfacePt;
    }

    //设置音频输入是否静音。
    public void SetAudioInputIsMute( int IsMute )
    {
        m_AudioInputPt.m_AudioInputIsMute = IsMute;
    }

    //设置是否使用音频输出。
    public void SetIsUseAudioOutput( int IsUseAudioOutput, int SamplingRate, int FrameLenMsec )
    {
        if( ( ( IsUseAudioOutput != 0 ) && ( ( SamplingRate != 8000 ) && ( SamplingRate != 16000 ) && ( SamplingRate != 32000 ) && ( SamplingRate != 48000 ) ) ) || //如果采样频率不正确。
            ( ( IsUseAudioOutput != 0 ) && ( ( FrameLenMsec == 0 ) || ( FrameLenMsec % 10 != 0 ) ) ) ) //如果帧的毫秒长度不正确。
        {
            return;
        }

        m_AudioOutputPt.m_IsUseAudioOutput = IsUseAudioOutput;
        m_AudioOutputPt.m_SamplingRate = SamplingRate;
        m_AudioOutputPt.m_FrameLen = FrameLenMsec * SamplingRate / 1000;
    }

    //设置音频输出要使用PCM原始数据。
    public void SetAudioOutputUsePcm()
    {
        m_AudioOutputPt.m_UseWhatDecoder = 0;
    }

    //设置音频输出要使用Speex解码器。
    public void SetAudioOutputUseSpeexDecoder( int IsUsePerceptualEnhancement )
    {
        m_AudioOutputPt.m_UseWhatDecoder = 1;
        m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement = IsUsePerceptualEnhancement;
    }

    //设置音频输出要使用Opus编码器。
    public void SetAudioOutputUseOpusDecoder()
    {
        m_AudioOutputPt.m_UseWhatDecoder = 2;
    }

    //设置音频输出是否保存音频到文件。
    public void SetAudioOutputIsSaveAudioToFile( int IsSaveAudioToFile, String AudioOutputFileFullPathStrPt )
    {
        m_AudioOutputPt.m_IsSaveAudioToFile = IsSaveAudioToFile;
        m_AudioOutputPt.m_AudioOutputFileFullPathStrPt = AudioOutputFileFullPathStrPt;
    }

    //设置音频输出是否绘制音频波形到Surface。
    public void SetAudioOutputIsDrawAudioOscilloToSurface( int IsDrawAudioToSurface, SurfaceView AudioOutputOscilloSurfacePt )
    {
        m_AudioOutputPt.m_IsDrawAudioOscilloToSurface = IsDrawAudioToSurface;
        m_AudioOutputPt.m_AudioOutputOscilloSurfacePt = AudioOutputOscilloSurfacePt;
    }

    //设置音频输出使用的设备。
    public void SetAudioOutputUseDvc( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
    {
        if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) )//如果使用听筒，则不能使用媒体类型音频输出流。
        {
            return;
        }

        m_AudioOutputPt.m_UseWhatAudioOutputDvc = UseSpeakerOrEarpiece;
        m_AudioOutputPt.m_UseWhatAudioOutputStreamType = UseVoiceCallOrMusic;
        SetIsUseWakeLock( m_IsUseWakeLock ); //重新初始化唤醒锁。
    }

    //设置音频输出是否静音。
    public void SetAudioOutputIsMute( int IsMute )
    {
        m_AudioOutputPt.m_AudioOutputIsMute = IsMute; //设置音频输出是否静音。
    }

    //设置是否使用视频输入。
    public void SetIsUseVideoInput( int IsUseVideoInput, int MaxSamplingRate, int FrameWidth, int FrameHeight, int ScreenRotate, HTSurfaceView VideoInputPreviewSurfaceViewPt )
    {
        if( ( ( IsUseVideoInput != 0 ) && ( ( MaxSamplingRate < 1 ) || ( MaxSamplingRate > 60 ) ) ) || //如果采样频率不正确。
            ( ( IsUseVideoInput != 0 ) && ( ( FrameWidth <= 0 ) || ( ( FrameWidth & 1 ) != 0 ) ) ) || //如果帧的宽度不正确。
            ( ( IsUseVideoInput != 0 ) && ( ( FrameHeight <= 0 ) || ( ( FrameHeight & 1 ) != 0 ) ) ) || //如果帧的高度不正确。
            ( ( IsUseVideoInput != 0 ) && ( ScreenRotate != 0 ) && ( ScreenRotate != 90 ) && ( ScreenRotate != 180 ) && ( ScreenRotate != 270 ) ) || //如果屏幕旋转的角度不正确。
            ( ( IsUseVideoInput != 0 ) && ( VideoInputPreviewSurfaceViewPt == null ) ) ) //如果视频预览SurfaceView的指针不正确。
        {
            return;
        }

        m_VideoInputPt.m_IsUseVideoInput = IsUseVideoInput;
        m_VideoInputPt.m_MaxSamplingRate = MaxSamplingRate;
        m_VideoInputPt.m_FrameWidth = FrameWidth;
        m_VideoInputPt.m_FrameHeight = FrameHeight;
        m_VideoInputPt.m_ScreenRotate = ScreenRotate;
        m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt = VideoInputPreviewSurfaceViewPt;
    }

    //设置视频输入要使用YU12原始数据。
    public void SetVideoInputUseYU12()
    {
        m_VideoInputPt.m_UseWhatEncoder = 0;
    }

    //设置视频输入要使用OpenH264编码器。
    public void SetVideoInputUseOpenH264Encoder( int VideoType, int EncodedBitrate, int BitrateControlMode, int IDRFrameIntvl, int Cmplxt )
    {
        m_VideoInputPt.m_UseWhatEncoder = 1;
        m_VideoInputPt.m_OpenH264EncoderVideoType = VideoType;
        m_VideoInputPt.m_OpenH264EncoderEncodedBitrate = EncodedBitrate;
        m_VideoInputPt.m_OpenH264EncoderBitrateControlMode = BitrateControlMode;
        m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl = IDRFrameIntvl;
        m_VideoInputPt.m_OpenH264EncoderCmplxt = Cmplxt;
    }

    //设置视频输入要使用系统自带H264编码器。
    public void SetVideoInputUseSystemH264Encoder( int EncodedBitrate, int BitrateControlMode, int IDRFrameIntvlTimeSec, int Cmplxt )
    {
        m_VideoInputPt.m_UseWhatEncoder = 2;
        m_VideoInputPt.m_SystemH264EncoderEncodedBitrate = EncodedBitrate;
        m_VideoInputPt.m_SystemH264EncoderBitrateControlMode = BitrateControlMode;
        m_VideoInputPt.m_SystemH264EncoderIDRFrameIntvlTimeSec = IDRFrameIntvlTimeSec;
        m_VideoInputPt.m_SystemH264EncoderCmplxt = Cmplxt;
    }

    //设置视频输入使用的设备。
    public void SetVideoInputUseDvc( int UseFrontOrBack, int FrontCameraDvcId, int BackCameraDvcId )
    {
        if( ( ( UseFrontOrBack != 0 ) && ( UseFrontOrBack != 1 ) ) ||
              ( FrontCameraDvcId < -1 ) ||
              ( BackCameraDvcId < -1 ) )
        {
            return;
        }

        m_VideoInputPt.m_UseWhatVideoInputDvc = UseFrontOrBack; //设置视频输入设备。
        m_VideoInputPt.m_FrontCameraDvcId = FrontCameraDvcId; //设置视频输入前置摄像头的设备ID。
        m_VideoInputPt.m_BackCameraDvcId = BackCameraDvcId; //设置视频输入后置摄像头的设备ID。
    }

    //设置视频输入是否黑屏。
    public void SetVideoInputIsBlack( int IsBlack )
    {
        m_VideoInputPt.m_VideoInputIsBlack = IsBlack;
    }

    //设置是否使用视频输出。
    public void SetIsUseVideoOutput( int IsUseVideoOutput, HTSurfaceView VideoOutputDisplaySurfaceViewPt, float VideoDisplayScale )
    {
        if( ( ( IsUseVideoOutput != 0 ) && ( VideoOutputDisplaySurfaceViewPt == null ) ) || //如果视频显示SurfaceView的指针不正确。
            ( ( IsUseVideoOutput != 0 ) && ( VideoDisplayScale <= 0 ) ) ) //如果视频显示缩放倍数不正确。
        {
            return;
        }

        m_VideoOutputPt.m_IsUseVideoOutput = IsUseVideoOutput;
        m_VideoOutputPt.m_VideoOutputDisplaySurfaceViewPt = VideoOutputDisplaySurfaceViewPt;
        m_VideoOutputPt.m_VideoOutputDisplayScale = VideoDisplayScale;
    }

    //设置视频输出要使用YU12原始数据。
    public void SetVideoOutputUseYU12()
    {
        m_VideoOutputPt.m_UseWhatDecoder = 0;
    }

    //设置视频输出要使用OpenH264解码器。
    public void SetVideoOutputUseOpenH264Decoder( int DecodeThrdNum )
    {
        m_VideoOutputPt.m_UseWhatDecoder = 1;
        m_VideoOutputPt.m_OpenH264DecoderDecodeThrdNum = DecodeThrdNum;
    }

    //设置视频输出要使用系统自带H264解码器。
    public void SetVideoOutputUseSystemH264Decoder()
    {
        m_VideoOutputPt.m_UseWhatDecoder = 2;
    }

    //设置视频输出是否黑屏。
    public void SetVideoOutputIsBlack( int IsBlack )
    {
        m_VideoOutputPt.m_VideoOutputIsBlack = IsBlack;
    }

    //请求权限。
    public static void RqstPrmsn( Activity RqstActivity, int IsRqstInternet, int IsRqstModifyAudioStng, int IsRqstForegroundService, int IsRqstWakeLock, int IsRqstRecordAudio, int IsRqstCamera, int DeniedIsPrintLogcat, int DeniedIsShowToast )
    {
        String p_DeniedPermissionStrPt = "拒绝的权限：";
        int p_DeniedPermissionNum = 0;
        String[] p_RqstPermissionStrArrPt = new String[6];

        //检测网络权限。
        if( ( IsRqstInternet != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.INTERNET;
            p_DeniedPermissionStrPt += "网络  ";
            p_DeniedPermissionNum++;
        }

        //检测修改音频设置权限。
        if( ( IsRqstModifyAudioStng != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.MODIFY_AUDIO_SETTINGS ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.MODIFY_AUDIO_SETTINGS;
            p_DeniedPermissionStrPt += "修改音频设置  ";
            p_DeniedPermissionNum++;
        }

        //检测前台服务权限。
        if( ( IsRqstForegroundService != 0 ) && ( android.os.Build.VERSION.SDK_INT >= 28 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.FOREGROUND_SERVICE ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.FOREGROUND_SERVICE;
            p_DeniedPermissionStrPt += "前台服务  ";
            p_DeniedPermissionNum++;
        }

        //检测唤醒锁权限。
        if( ( IsRqstWakeLock != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.WAKE_LOCK ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.WAKE_LOCK;
            p_DeniedPermissionStrPt += "唤醒锁  ";
            p_DeniedPermissionNum++;
        }

        //检测录音权限。
        if( ( IsRqstRecordAudio != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.RECORD_AUDIO;
            p_DeniedPermissionStrPt += "录音  ";
            p_DeniedPermissionNum++;
        }

        //检测摄像头权限。
        if( ( IsRqstCamera != 0 ) && ( ContextCompat.checkSelfPermission( RqstActivity, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) )
        {
            p_RqstPermissionStrArrPt[p_DeniedPermissionNum] = Manifest.permission.CAMERA;
            p_DeniedPermissionStrPt += "摄像头  ";
            p_DeniedPermissionNum++;
        }

        if( p_DeniedPermissionNum > 0 ) //有拒绝的权限。
        {
            //请求权限。
            ActivityCompat.requestPermissions( RqstActivity, p_RqstPermissionStrArrPt, 1 );

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
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //判断各个变量是否正确。
            if( ( ExitFlag < 0 ) || ( ExitFlag > 3 ) ) //如果退出标记不正确。
            {
                break out;
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

            p_Result = 0; //设置本函数执行成功。
        }

        return p_Result;
    }

    //音频输入线程类。
    private class AudioInputThrd extends Thread
    {
        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

            if( m_AudioInputPt.m_UseWhatAec != 0 ) //如果要使用音频输入的声学回音消除，就自适应计算声学回音的延迟，并设置到声学回音消除器。放在音频输入线程中计算，可以减少媒体处理线程的初始化时间。
            {
                int p_Delay = 0; //存放声学回音的延迟，单位毫秒。
                HTInt p_HTIntDelay = new HTInt();

                //计算音频输出的延迟。
                m_AudioOutputPt.m_AudioOutputDvcPt.play(); //让音频输出设备开始播放。
                m_AudioInputPt.m_AudioInputFramePt = new short[ m_AudioOutputPt.m_FrameLen ]; //创建一个空的音频输出帧。
                m_AudioInputPt.m_LastTimeMsec = System.currentTimeMillis();
                while( true )
                {
                    m_AudioOutputPt.m_AudioOutputDvcPt.write( m_AudioInputPt.m_AudioInputFramePt, 0, m_AudioInputPt.m_AudioInputFramePt.length ); //播放一个空的音频输出帧。
                    m_AudioInputPt.m_NowTimeMsec = System.currentTimeMillis();
                    p_Delay += m_AudioOutputPt.m_FrameLen; //递增音频输出的延迟。
                    if( m_AudioInputPt.m_NowTimeMsec - m_AudioInputPt.m_LastTimeMsec >= 10 ) //如果播放耗时较长，就表示音频输出设备的缓冲区已经写满，结束计算。
                    {
                        break;
                    }
                    m_AudioInputPt.m_LastTimeMsec = m_AudioInputPt.m_NowTimeMsec;
                }
                p_Delay = p_Delay * 1000 / m_AudioOutputPt.m_SamplingRate; //将音频输出的延迟转换为毫秒。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟：" + p_Delay + " 毫秒。" );

                //计算音频输入的延迟。
                m_AudioInputPt.m_AudioInputDvcPt.startRecording(); //让音频输入设备开始录音。
                m_AudioInputPt.m_AudioInputFramePt = new short[ m_AudioInputPt.m_FrameLen ]; //创建一个空的音频输入帧。
                m_AudioInputPt.m_LastTimeMsec = System.currentTimeMillis();
                m_AudioInputPt.m_AudioInputDvcPt.read( m_AudioInputPt.m_AudioInputFramePt, 0, m_AudioInputPt.m_AudioInputFramePt.length ); //计算读取一个音频输入帧的耗时。
                m_AudioInputPt.m_NowTimeMsec = System.currentTimeMillis();
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟：" + ( m_AudioInputPt.m_NowTimeMsec - m_AudioInputPt.m_LastTimeMsec ) + " 毫秒。" );

                m_AudioOutputPt.m_AudioOutputThrdPt.start(); //启动音频输出线程。

                //计算声学回音的延迟。
                p_Delay = p_Delay + ( int ) ( m_AudioInputPt.m_NowTimeMsec - m_AudioInputPt.m_LastTimeMsec );
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "声学回音延迟：" + p_Delay + " 毫秒，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

                //设置到WebRtc定点版和浮点版声学回音消除器。
                if( ( m_AudioInputPt.m_WebRtcAecmPt != null ) && ( m_AudioInputPt.m_WebRtcAecmPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc定点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    m_AudioInputPt.m_WebRtcAecmPt.SetDelay( p_Delay / 2 );
                    m_AudioInputPt.m_WebRtcAecmPt.GetDelay( p_HTIntDelay );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_AudioInputPt.m_WebRtcAecPt != null ) && ( m_AudioInputPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用WebRtc浮点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    if( m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode == 0 ) //如果WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_AudioInputPt.m_WebRtcAecPt.SetDelay( p_Delay );
                        m_AudioInputPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay );
                    }
                    else //如果WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_AudioInputPt.m_WebRtcAecPt.SetDelay( 20 );
                        m_AudioInputPt.m_WebRtcAecPt.GetDelay( p_HTIntDelay );
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_AudioInputPt.m_SpeexWebRtcAecPt != null ) && ( m_AudioInputPt.m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc定点版声学回音消除器需要自适应设置回音的延迟。
                {
                    m_AudioInputPt.m_SpeexWebRtcAecPt.SetWebRtcAecmDelay( p_Delay / 2 );
                    m_AudioInputPt.m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_AudioInputPt.m_SpeexWebRtcAecPt != null ) && ( m_AudioInputPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果要使用SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
                {
                    if( m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_AudioInputPt.m_SpeexWebRtcAecPt.SetWebRtcAecDelay( p_Delay );
                        m_AudioInputPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_AudioInputPt.m_SpeexWebRtcAecPt.SetWebRtcAecDelay( 20 );
                        m_AudioInputPt.m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
            }
            else //如果不使用音频输入的声学回音消除，就直接启动音频输出线程。
            {
                m_AudioInputPt.m_AudioInputDvcPt.startRecording(); //让音频输入设备开始录音。
                if( m_AudioOutputPt.m_IsUseAudioOutput != 0 ) //如果要使用音频输出。
                {
                    m_AudioOutputPt.m_AudioOutputDvcPt.play(); //让音频输出设备开始播放。
                    m_AudioOutputPt.m_AudioOutputThrdPt.start(); //启动音频输出线程。
                }
            }

            //开始音频输入循环。
            OutAudioInputLoop:
            while( true )
            {
                //获取一个音频输入空闲帧。
                if( ( m_AudioInputPt.m_AudioInputFrameLnkLstElmTotal = m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.size() ) > 0 ) //如果音频输入空闲帧链表中有音频输入空闲帧。
                {
                    //从音频输入空闲帧链表中取出第一个音频输入空闲帧。
                    synchronized( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt )
                    {
                        m_AudioInputPt.m_AudioInputFramePt = m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.getFirst();
                        m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：从音频输入空闲帧链表中取出第一个音频输入空闲帧，音频输入空闲帧链表元素个数：" + m_AudioInputPt.m_AudioInputFrameLnkLstElmTotal + "。" );
                }
                else //如果音频输入空闲帧链表中没有音频输入空闲帧。
                {
                    if( ( m_AudioInputPt.m_AudioInputFrameLnkLstElmTotal = m_AudioInputPt.m_AudioInputFrameLnkLstPt.size() ) <= 50 )
                    {
                        m_AudioInputPt.m_AudioInputFramePt = new short[m_AudioInputPt.m_FrameLen]; //创建一个音频输入空闲帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：音频输入空闲帧链表中没有音频输入空闲帧，创建一个音频输入空闲帧。" );
                    }
                    else
                    {
                        m_AudioInputPt.m_AudioInputFramePt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输入线程：音频输入帧链表中音频输入帧数量为" + m_AudioInputPt.m_AudioInputFrameLnkLstElmTotal + "已经超过上限50，不再创建一个音频输入空闲帧。" );
                    }
                }

                if( m_AudioInputPt.m_AudioInputFramePt != null ) //如果获取了一个音频输入空闲帧。
                {
                    if( m_IsPrintLogcat != 0 ) m_AudioInputPt.m_LastTimeMsec = System.currentTimeMillis();

                    //读取本次音频输入帧。
                    m_AudioInputPt.m_AudioInputDvcPt.read( m_AudioInputPt.m_AudioInputFramePt, 0, m_AudioInputPt.m_AudioInputFramePt.length );

                    //追加本次音频输入帧到音频输入帧链表。
                    synchronized( m_AudioInputPt.m_AudioInputFrameLnkLstPt )
                    {
                        m_AudioInputPt.m_AudioInputFrameLnkLstPt.addLast( m_AudioInputPt.m_AudioInputFramePt );
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        m_AudioInputPt.m_NowTimeMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "音频输入线程：本次音频输入帧读取完毕，耗时 " + ( m_AudioInputPt.m_NowTimeMsec - m_AudioInputPt.m_LastTimeMsec ) + " 毫秒。" );
                    }
                }

                if( m_AudioInputPt.m_AudioInputThrdExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break OutAudioInputLoop;
                }
            } //音频输入循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程已退出。" );
        }
    }

    //音频输出线程类。
    private class AudioOutputThrd extends Thread
    {
        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );

            //开始音频输出循环。
            OutAudioOutputLoop:
            while( true )
            {
                //获取一个音频输出空闲帧。
                if( ( m_AudioOutputPt.m_AudioOutputFrameLnkLstElmTotal = m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.size() ) > 0 ) //如果音频输出空闲帧链表中有音频输出空闲帧。
                {
                    //从音频输出空闲帧链表中取出第一个音频输出空闲帧。
                    synchronized( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt )
                    {
                        m_AudioOutputPt.m_AudioOutputFramePt = m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.getFirst();
                        m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：从音频输出空闲帧链表中取出第一个音频输出空闲帧，音频输出空闲帧链表元素个数：" + m_AudioOutputPt.m_AudioOutputFrameLnkLstElmTotal + "。" );
                }
                else //如果音频输出空闲帧链表中没有音频输出空闲帧。
                {
                    if( ( m_AudioOutputPt.m_AudioOutputFrameLnkLstElmTotal = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.size() ) <= 50 )
                    {
                        m_AudioOutputPt.m_AudioOutputFramePt = new short[m_AudioOutputPt.m_FrameLen]; //创建一个音频输出空闲帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：音频输出空闲帧链表中没有音频输出空闲帧，创建一个音频输出空闲帧。" );
                    }
                    else
                    {
                        m_AudioOutputPt.m_AudioOutputFramePt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：音频输出帧链表中音频输出帧数量为" + m_AudioOutputPt.m_AudioOutputFrameLnkLstElmTotal + "已经超过上限50，不再创建一个音频输出空闲帧。" );
                    }
                }

                if( m_AudioOutputPt.m_AudioOutputFramePt != null ) //如果获取了一个音频输出空闲帧。
                {
                    if( m_IsPrintLogcat != 0 ) m_AudioOutputPt.m_LastTimeMsec = System.currentTimeMillis();

                    //调用用户定义的写入音频输出帧函数，并解码成PCM原始数据。
                    switch( m_AudioOutputPt.m_UseWhatDecoder ) //使用什么解码器。
                    {
                        case 0: //如果要使用PCM原始数据。
                        {
                            //调用用户定义的写入音频输出帧函数。
                            m_AudioOutputPt.m_AudioOutputFrameLenPt.m_Val = m_AudioOutputPt.m_AudioOutputFramePt.length;
                            UserWriteAudioOutputFrame( m_AudioOutputPt.m_AudioOutputFramePt, null, m_AudioOutputPt.m_AudioOutputFrameLenPt );
                            break;
                        }
                        case 1: //如果要使用Speex解码器。
                        {
                            //调用用户定义的写入音频输出帧函数。
                            m_AudioOutputPt.m_AudioOutputFrameLenPt.m_Val = m_AudioOutputPt.m_EncodedAudioOutputFramePt.length;
                            UserWriteAudioOutputFrame( null, m_AudioOutputPt.m_EncodedAudioOutputFramePt, m_AudioOutputPt.m_AudioOutputFrameLenPt );

                            //使用Speex解码器。
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Pocs( m_AudioOutputPt.m_EncodedAudioOutputFramePt, m_AudioOutputPt.m_AudioOutputFrameLenPt.m_Val, m_AudioOutputPt.m_AudioOutputFramePt ) == 0 )
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
                    if( m_AudioOutputPt.m_AudioOutputIsMute != 0 )
                    {
                        Arrays.fill( m_AudioOutputPt.m_AudioOutputFramePt, ( short ) 0 );
                    }

                    //写入本次音频输出帧到音频输出设备。
                    m_AudioOutputPt.m_AudioOutputDvcPt.write( m_AudioOutputPt.m_AudioOutputFramePt, 0, m_AudioOutputPt.m_AudioOutputFramePt.length );

                    //调用用户定义的获取PCM格式音频输出帧函数。
                    UserGetPcmAudioOutputFrame( m_AudioOutputPt.m_AudioOutputFramePt, m_AudioOutputPt.m_AudioOutputFramePt.length );

                    //追加本次音频输出帧到音频输出帧链表。
                    synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                    {
                        m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.addLast( m_AudioOutputPt.m_AudioOutputFramePt );
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        m_AudioOutputPt.m_NowTimeMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "音频输出线程：本次音频输出帧写入完毕，耗时 " + ( m_AudioOutputPt.m_NowTimeMsec - m_AudioOutputPt.m_LastTimeMsec ) + " 毫秒。" );
                    }
                }

                if( m_AudioOutputPt.m_AudioOutputThrdExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break OutAudioOutputLoop;
                }
            } //音频输出循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
        }
    }

    //视频输入线程类。
    private class VideoInputThrd extends Thread implements Camera.PreviewCallback
    {
        //读取一个视频输入帧的预览回调函数，本函数是在主线程中运行的。
        @Override public void onPreviewFrame( byte[] data, Camera camera )
        {
            //追加本次视频输入帧到视频输入帧链表。
            synchronized( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt )
            {
                m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.addLast( data );
            }
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：读取一个视频输入帧。" );
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：开始准备视频输入。" );

            //开始视频输入循环。
            OutVideoInputLoop:
            while( true )
            {
                //处理视频输入帧。
                if( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.size() > 0 )//如果NV21格式视频输入帧链表中有帧了。
                {
                    //获取一个视频输入空闲帧。
                    if( ( m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal = m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.size() ) > 0 ) //如果视频输入空闲帧链表中有视频输入空闲帧。
                    {
                        //从视频输入空闲帧链表中取出第一个视频输入空闲帧。
                        synchronized( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputFrameElmPt = m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.getFirst();
                            m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从视频输入空闲帧链表中取出第一个视频输入空闲帧，视频输入空闲帧链表元素个数：" + m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal + "。" );
                    }
                    else //如果视频输入空闲帧链表中没有视频输入空闲帧。
                    {
                        if( ( m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal = m_VideoInputPt.m_VideoInputFrameLnkLstPt.size() ) <= 20 )
                        {
                            m_VideoInputPt.m_VideoInputFrameElmPt = m_VideoInputPt.new VideoInputFrameElm(); //创建一个视频输入空闲帧。
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：视频输入空闲帧链表中没有视频输入空闲帧，创建一个视频输入空闲帧。" );
                        }
                        else
                        {
                            m_VideoInputPt.m_VideoInputFrameElmPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：视频输入帧链表中视频输入帧数量为" + m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal + "已经超过上限20，不再创建一个视频输入空闲帧。" );
                        }
                    }

                    if( m_VideoInputPt.m_VideoInputFrameElmPt != null ) //如果获取了一个视频输入空闲帧。
                    {
                        if( m_IsPrintLogcat != 0 ) m_VideoInputPt.m_LastTimeMsec = System.currentTimeMillis();

                        //从视频输入帧链表中取出第一个视频输入帧。
                        m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal = m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.size();
                        synchronized( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputFramePt = m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.getFirst();
                            m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从NV21格式视频输入帧链表中取出第一个NV21格式视频输入帧，NV21格式视频输入帧链表元素个数：" + m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal + "。" );

                        skip:
                        {
                            //裁剪视频输入设备帧。
                            if( m_VideoInputPt.m_VideoInputDvcFrameIsCrop != 0 )
                            {
                                if( LibYUV.PictrCrop( m_VideoInputPt.m_VideoInputFramePt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VideoInputPt.m_VideoInputDvcFrameWidth, m_VideoInputPt.m_VideoInputDvcFrameHeight,
                                                      m_VideoInputPt.m_VideoInputDvcFrameCropX, m_VideoInputPt.m_VideoInputDvcFrameCropY, m_VideoInputPt.m_VideoInputDvcFrameCropWidth, m_VideoInputPt.m_VideoInputDvcFrameCropHeight,
                                                      m_VideoInputPt.m_VideoInputTmpFramePt, m_VideoInputPt.m_VideoInputResultFrameSz, m_VideoInputPt.m_VideoInputResultFrameLenPt, null, null,
                                                      null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧成功。" );
                                    m_VideoInputPt.m_VideoInputSwapFramePt = m_VideoInputPt.m_VideoInputTmpFramePt;
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧失败，本次视频输入帧丢弃。" );
                                    break skip;
                                }
                            }
                            else
                            {
                                m_VideoInputPt.m_VideoInputSwapFramePt = m_VideoInputPt.m_VideoInputFramePt;
                            }

                            //NV21格式视频输入帧旋转为YU12格式视频输入帧。
                            if( LibYUV.PictrRotate( m_VideoInputPt.m_VideoInputSwapFramePt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VideoInputPt.m_VideoInputDvcFrameCropWidth, m_VideoInputPt.m_VideoInputDvcFrameCropHeight,
                                                    m_VideoInputPt.m_VideoInputDvcFrameRotate,
                                                    m_VideoInputPt.m_VideoInputResultFramePt, m_VideoInputPt.m_VideoInputResultFramePt.length, null, null,
                                                    null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧失败，本次视频输入帧丢弃。" );
                                break skip;
                            }

                            //缩放视频输入设备帧。
                            if( m_VideoInputPt.m_VideoInputDvcFrameIsScale != 0 )
                            {
                                if( LibYUV.PictrScale( m_VideoInputPt.m_VideoInputResultFramePt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VideoInputPt.m_VideoInputDvcFrameRotateWidth, m_VideoInputPt.m_VideoInputDvcFrameRotateHeight,
                                                       3,
                                                       m_VideoInputPt.m_VideoInputTmpFramePt, m_VideoInputPt.m_VideoInputResultFrameSz, m_VideoInputPt.m_VideoInputResultFrameLenPt, m_VideoInputPt.m_VideoInputDvcFrameScaleWidth, m_VideoInputPt.m_VideoInputDvcFrameScaleHeight,
                                                       null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧成功。" );
                                    m_VideoInputPt.m_VideoInputSwapFramePt = m_VideoInputPt.m_VideoInputResultFramePt; m_VideoInputPt.m_VideoInputResultFramePt = m_VideoInputPt.m_VideoInputTmpFramePt; m_VideoInputPt.m_VideoInputTmpFramePt = m_VideoInputPt.m_VideoInputSwapFramePt; //交换视频输入结果帧和视频输入临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧失败，本次视频输入帧丢弃。" );
                                    break skip;
                                }
                            }

                            //将视频结果帧复制到视频输入帧元素。
                            System.arraycopy( m_VideoInputPt.m_VideoInputResultFramePt, 0, m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt, 0, m_VideoInputPt.m_VideoInputDvcFrameScaleWidth * m_VideoInputPt.m_VideoInputDvcFrameScaleHeight * 3 / 2 );
                            m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFrameWidthPt.m_Val = m_VideoInputPt.m_VideoInputDvcFrameScaleWidth;
                            m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFrameHeightPt.m_Val = m_VideoInputPt.m_VideoInputDvcFrameScaleHeight;

                            //判断视频输入是否黑屏。在视频输入处理完后再设置黑屏，这样可以保证视频输入处理器的连续性。
                            if( m_VideoInputPt.m_VideoInputIsBlack != 0 )
                            {
                                int p_TmpLen = m_VideoInputPt.m_VideoInputDvcFrameScaleWidth * m_VideoInputPt.m_VideoInputDvcFrameScaleHeight;
                                Arrays.fill( m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt, 0, p_TmpLen, ( byte ) 0 );
                                Arrays.fill( m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt, p_TmpLen, m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt.length, ( byte ) 128 );
                            }

                            //使用编码器。
                            switch( m_VideoInputPt.m_UseWhatEncoder )
                            {
                                case 0: //如果要使用YU12原始数据。
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用YU12原始数据。" );
                                    break;
                                }
                                case 1: //如果要使用OpenH264编码器。
                                {
                                    if( m_VideoInputPt.m_OpenH264EncoderPt.Pocs( m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt, m_VideoInputPt.m_VideoInputDvcFrameScaleWidth, m_VideoInputPt.m_VideoInputDvcFrameScaleHeight, m_VideoInputPt.m_LastTimeMsec,
                                                                                 m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt, m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt.length, m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFrameLenPt,
                                                                                 null ) == 0 )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器成功。H264格式视频输入帧的长度：" + m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFrameLenPt.m_Val + "，时间戳：" + m_VideoInputPt.m_LastTimeMsec + "，类型：" + ( m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt[4] & 0xff ) + "。" );
                                    }
                                    else
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器失败，本次视频输入帧丢弃。" );
                                        break skip;
                                    }
                                    break;
                                }
                                case 2: //如果要使用系统自带H264编码器。
                                {
                                    if( m_VideoInputPt.m_SystemH264EncoderPt.Pocs( m_VideoInputPt.m_VideoInputFrameElmPt.m_YU12VideoInputFramePt, m_VideoInputPt.m_LastTimeMsec,
                                                                                   m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt, ( long )m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt.length, m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFrameLenPt,
                                                                                   1000 / m_VideoInputPt.m_MaxSamplingRate * 2 / 3, null ) == 0 )
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器成功。H264格式视频输入帧的长度：" + m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFrameLenPt.m_Val + "，时间戳：" + m_VideoInputPt.m_LastTimeMsec + "，类型：" + ( m_VideoInputPt.m_VideoInputFrameElmPt.m_EncodedVideoInputFramePt[4] & 0xff ) + "。" );
                                    }
                                    else
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器失败，本次视频输入帧丢弃。" );
                                        break skip;
                                    }
                                    break;
                                }
                            }
                        }

                        //追加本次视频输入帧到视频输入帧链表。
                        synchronized( m_VideoInputPt.m_VideoInputFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputFrameLnkLstPt.addLast( m_VideoInputPt.m_VideoInputFrameElmPt );
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            m_VideoInputPt.m_NowTimeMsec = System.currentTimeMillis();
                            Log.i( m_CurClsNameStrPt, "视频输入线程：本次视频输入帧处理完毕，耗时 " + ( m_VideoInputPt.m_NowTimeMsec - m_VideoInputPt.m_LastTimeMsec ) + " 毫秒。" );
                        }
                    }

                    //追加本次NV21格式视频输入帧到视频输入设备。
                    m_VideoInputPt.m_VideoInputDvcPt.addCallbackBuffer( m_VideoInputPt.m_VideoInputFramePt );
                }

                if( m_VideoInputPt.m_VideoInputThrdExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break OutVideoInputLoop;
                }

                SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
            } //视频输入循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程已退出。" );
        }
    }

    //视频输出线程类。
    private class VideoOutputThrd extends Thread
    {
        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：开始准备视频输出。" );

            //开始视频输出循环。
            OutVideoOutputLoop:
            while( true )
            {
                skip:
                {
                    if( m_IsPrintLogcat != 0 ) m_VideoOutputPt.m_LastTimeMsec = System.currentTimeMillis();

                    //调用用户定义的写入视频输出帧函数，并解码成YU12原始数据。
                    switch( m_VideoOutputPt.m_UseWhatDecoder ) //使用什么解码器。
                    {
                        case 0: //如果使用YU12原始数据。
                        {
                            //调用用户定义的写入视频输出帧函数。
                            m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val = 0;
                            m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val = 0;
                            m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val = m_VideoOutputPt.m_VideoOutputResultFramePt.length;
                            UserWriteVideoOutputFrame( m_VideoOutputPt.m_VideoOutputResultFramePt, m_VideoOutputPt.m_VideoOutputFrameWidthPt, m_VideoOutputPt.m_VideoOutputFrameHeightPt, null, m_VideoOutputPt.m_VideoOutputResultFrameLenPt );

                            if( ( m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val > 0 ) && ( m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val > 0 ) ) //如果本次写入了视频输出帧。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用YU12原始数据成功。YU12格式帧宽度：" + m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val + "，YU12格式帧高度：" + m_VideoOutputPt.m_VideoOutputFrameHeightPt + "。" );
                            }
                            else //如果本次没写入视频输出帧。
                            {
                                break skip;
                            }
                            break;
                        }
                        case 1: //如果使用OpenH264解码器。
                        {
                            //调用用户定义的写入视频输出帧函数。
                            m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val = m_VideoOutputPt.m_VideoOutputTmpFramePt.length;
                            UserWriteVideoOutputFrame( null, null, null, m_VideoOutputPt.m_VideoOutputTmpFramePt, m_VideoOutputPt.m_VideoOutputResultFrameLenPt );

                            if( m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val > 0 ) //如果本次写入了视频输出帧。
                            {
                                //使用OpenH264解码器。
                                if( m_VideoOutputPt.m_OpenH264DecoderPt.Pocs( m_VideoOutputPt.m_VideoOutputTmpFramePt, m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val,
                                                                              m_VideoOutputPt.m_VideoOutputResultFramePt, m_VideoOutputPt.m_VideoOutputResultFramePt.length, m_VideoOutputPt.m_VideoOutputFrameWidthPt, m_VideoOutputPt.m_VideoOutputFrameHeightPt,
                                                                              null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器成功。已解码YU12格式帧宽度：" + m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val + "。" );
                                    if( ( m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val == 0 ) || ( m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val == 0 ) ) break skip; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器失败，本次视频输出帧丢弃。" );
                                    break skip;
                                }
                            }
                            else //如果本次没写入视频输出帧。
                            {
                                break skip;
                            }
                            break;
                        }
                        case 2: //如果使用系统自带H264解码器。
                        {
                            //调用用户定义的写入视频输出帧函数。
                            m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val = m_VideoOutputPt.m_VideoOutputTmpFramePt.length;
                            UserWriteVideoOutputFrame( null, null, null, m_VideoOutputPt.m_VideoOutputTmpFramePt, m_VideoOutputPt.m_VideoOutputResultFrameLenPt );

                            if( m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val != 0 ) //如果本次写入了视频输出帧。
                            {
                                //使用系统自带H264解码器。
                                if( m_VideoOutputPt.m_SystemH264DecoderPt.Pocs( m_VideoOutputPt.m_VideoOutputTmpFramePt, m_VideoOutputPt.m_VideoOutputResultFrameLenPt.m_Val,
                                                                                m_VideoOutputPt.m_VideoOutputResultFramePt, m_VideoOutputPt.m_VideoOutputResultFramePt.length, m_VideoOutputPt.m_VideoOutputFrameWidthPt, m_VideoOutputPt.m_VideoOutputFrameHeightPt,
                                                                                40, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用系统自带H264解码器成功。已解码YU12格式帧宽度：" + m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val + "。" );
                                    if( ( m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val == 0 ) || ( m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val == 0 ) ) break skip; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出线程：使用系统自带H264解码器失败，本次视频输出帧丢弃。" );
                                    break skip;
                                }
                            }
                            else //如果本次没写入视频输出帧。
                            {
                                break skip;
                            }
                            break;
                        }
                    }

                    //用户定义的获取YU12格式视频输出帧函数。
                    UserGetYU12VideoOutputFrame( m_VideoOutputPt.m_VideoOutputResultFramePt, m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val, m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val );

                    //判断视频输出是否黑屏。在视频处理完后再设置黑屏，这样可以保证视频处理器的连续性。
                    if( m_VideoOutputPt.m_VideoOutputIsBlack != 0 )
                    {
                        int p_TmpLen = m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val * m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val;
                        Arrays.fill( m_VideoOutputPt.m_VideoOutputResultFramePt, 0, p_TmpLen, ( byte ) 0 );
                        Arrays.fill( m_VideoOutputPt.m_VideoOutputResultFramePt, p_TmpLen, p_TmpLen + p_TmpLen / 2, ( byte ) 128 );
                    }

                    //缩放视频输出帧。
                    if( m_VideoOutputPt.m_VideoOutputDisplayScale != 1.0f )
                    {
                        if( LibYUV.PictrScale( m_VideoOutputPt.m_VideoOutputResultFramePt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val, m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val,
                                               3,
                                               m_VideoOutputPt.m_VideoOutputTmpFramePt, m_VideoOutputPt.m_VideoOutputTmpFramePt.length, null, ( int )( m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val * m_VideoOutputPt.m_VideoOutputDisplayScale ), ( int )( m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val * m_VideoOutputPt.m_VideoOutputDisplayScale ),
                                               null ) != 0 )
                        {
                            Log.e( m_CurClsNameStrPt, "视频输出线程：视频输出显示缩放失败，本次视频输出帧丢弃。" );
                            break skip;
                        }
                        m_VideoOutputPt.m_VideoOutputSwapFramePt = m_VideoOutputPt.m_VideoOutputResultFramePt; m_VideoOutputPt.m_VideoOutputResultFramePt = m_VideoOutputPt.m_VideoOutputTmpFramePt; m_VideoOutputPt.m_VideoOutputTmpFramePt = m_VideoOutputPt.m_VideoOutputSwapFramePt; //交换视频结果帧和视频临时帧。

                        m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val *= m_VideoOutputPt.m_VideoOutputDisplayScale;
                        m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val *= m_VideoOutputPt.m_VideoOutputDisplayScale;
                    }

                    //设置视频输出显示SurfaceView的宽高比。
                    m_VideoOutputPt.m_VideoOutputDisplaySurfaceViewPt.setWidthToHeightRatio( ( float )m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val / m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val );

                    //显示视频输出帧。
                    if( LibYUV.PictrDrawToSurface( m_VideoOutputPt.m_VideoOutputResultFramePt, 0, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VideoOutputPt.m_VideoOutputFrameWidthPt.m_Val, m_VideoOutputPt.m_VideoOutputFrameHeightPt.m_Val, m_VideoOutputPt.m_VideoOutputDisplaySurfaceViewPt.getHolder().getSurface(), null ) != 0 )
                    {
                        Log.e( m_CurClsNameStrPt, "视频输出线程：绘制视频输出帧到视频输出显示SurfaceView失败，本次视频输出帧丢弃。" );
                        break skip;
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        m_VideoOutputPt.m_NowTimeMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "视频输出线程：本次视频输出帧处理完毕，耗时 " + ( m_VideoOutputPt.m_NowTimeMsec - m_VideoOutputPt.m_LastTimeMsec ) + " 毫秒。" );
                    }
                }

                if( m_VideoOutputPt.m_VideoOutputThrdExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break OutVideoOutputLoop;
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

                        p_StngFileWriterPt.write( "m_AudioInputPt.m_IsUseAudioInput：" + m_AudioInputPt.m_IsUseAudioInput + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SamplingRate：" + m_AudioInputPt.m_SamplingRate + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_FrameLen：" + m_AudioInputPt.m_FrameLen + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputIsUseSystemAecNsAgc：" + m_AudioInputPt.m_IsUseSystemAecNsAgc + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputUseWhatAec：" + m_AudioInputPt.m_UseWhatAec + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecFilterLen：" + m_AudioInputPt.m_SpeexAecFilterLen + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecIsUseRec：" + m_AudioInputPt.m_SpeexAecIsUseRec + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoMultiple：" + m_AudioInputPt.m_SpeexAecEchoMultiple + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoCont：" + m_AudioInputPt.m_SpeexAecEchoCont + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoSupes：" + m_AudioInputPt.m_SpeexAecEchoSupes + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoSupesAct：" + m_AudioInputPt.m_SpeexAecEchoSupesAct + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecIsSaveMemFile：" + m_AudioInputPt.m_SpeexAecIsSaveMemFile + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt：" + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmIsUseCNGMode：" + m_AudioInputPt.m_WebRtcAecmIsUseCNGMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmEchoMode：" + m_AudioInputPt.m_WebRtcAecmEchoMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmDelay：" + m_AudioInputPt.m_WebRtcAecmDelay + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecEchoMode：" + m_AudioInputPt.m_WebRtcAecEchoMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecDelay：" + m_AudioInputPt.m_WebRtcAecDelay + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode：" + m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode：" + m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay：" + m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsSaveMemFile：" + m_AudioInputPt.m_WebRtcAecIsSaveMemFile + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt：" + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWorkMode：" + m_AudioInputPt.m_SpeexWebRtcAecWorkMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec：" + m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay：" + m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_UseWhatNs：" + m_AudioInputPt.m_UseWhatNs + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsIsUseNs：" + m_AudioInputPt.m_SpeexPrpocsIsUseNs + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsNoiseSupes：" + m_AudioInputPt.m_SpeexPrpocsNoiseSupes + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsIsUseDereverb：" + m_AudioInputPt.m_SpeexPrpocsIsUseDereverb + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcNsxPolicyMode：" + m_AudioInputPt.m_WebRtcNsxPolicyMode + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_WebRtcNsPolicyMode：" + m_AudioInputPt.m_WebRtcNsPolicyMode + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_IsUseSpeexPrpocsOther：" + m_AudioInputPt.m_IsUseSpeexPrpocsOther + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsIsUseVad：" + m_AudioInputPt.m_SpeexPrpocsIsUseVad + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsVadProbStart：" + m_AudioInputPt.m_SpeexPrpocsVadProbStart + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsVadProbCont：" + m_AudioInputPt.m_SpeexPrpocsVadProbCont + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsIsUseAgc：" + m_AudioInputPt.m_SpeexPrpocsIsUseAgc + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsAgcLevel：" + m_AudioInputPt.m_SpeexPrpocsAgcLevel + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsAgcIncrement：" + m_AudioInputPt.m_SpeexPrpocsAgcIncrement + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsAgcDecrement：" + m_AudioInputPt.m_SpeexPrpocsAgcDecrement + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexPrpocsAgcMaxGain：" + m_AudioInputPt.m_SpeexPrpocsAgcMaxGain + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_UseWhatEncoder：" + m_AudioInputPt.m_UseWhatEncoder + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr：" + m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderQualt：" + m_AudioInputPt.m_SpeexEncoderQualt + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderCmplxt：" + m_AudioInputPt.m_SpeexEncoderCmplxt + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate：" + m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_IsSaveAudioToFile：" + m_AudioInputPt.m_IsSaveAudioToFile + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputFileFullPathStrPt：" + m_AudioInputPt.m_AudioInputFileFullPathStrPt + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioResultFileFullPathStrPt：" + m_AudioInputPt.m_AudioResultFileFullPathStrPt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_IsDrawAudioOscilloToSurface：" + m_AudioInputPt.m_IsDrawAudioOscilloToSurface + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputOscilloSurfacePt：" + m_AudioInputPt.m_AudioInputOscilloSurfacePt + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioResultOscilloSurfacePt：" + m_AudioInputPt.m_AudioResultOscilloSurfacePt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputDvcBufSz：" + m_AudioInputPt.m_AudioInputDvcBufSz + "\n" );
                        p_StngFileWriterPt.write( "m_AudioInputPt.m_AudioInputIsMute：" + m_AudioInputPt.m_AudioInputIsMute + "\n" );
                        p_StngFileWriterPt.write( "\n" );

                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_IsUseAudioOutput：" + m_AudioOutputPt.m_IsUseAudioOutput + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_SamplingRate：" + m_AudioOutputPt.m_SamplingRate + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_FrameLen：" + m_AudioOutputPt.m_FrameLen + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_UseWhatDecoder：" + m_AudioOutputPt.m_UseWhatDecoder + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement：" + m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_IsSaveAudioToFile：" + m_AudioOutputPt.m_IsSaveAudioToFile + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputFileFullPathStrPt：" + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_IsDrawAudioOscilloToSurface：" + m_AudioOutputPt.m_IsDrawAudioOscilloToSurface + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputOscilloSurfacePt：" + m_AudioOutputPt.m_AudioOutputOscilloSurfacePt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputDvcBufSz：" + m_AudioOutputPt.m_AudioOutputDvcBufSz + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_UseWhatAudioOutputDvc：" + m_AudioOutputPt.m_UseWhatAudioOutputDvc + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_UseWhatAudioOutputStreamType：" + m_AudioOutputPt.m_UseWhatAudioOutputStreamType + "\n" );
                        p_StngFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputIsMute：" + m_AudioOutputPt.m_AudioOutputIsMute + "\n" );
                        p_StngFileWriterPt.write( "\n" );

                        p_StngFileWriterPt.write( "m_VideoInputPt.m_IsUseVideoInput：" + m_VideoInputPt.m_IsUseVideoInput + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_MaxSamplingRate：" + m_VideoInputPt.m_MaxSamplingRate + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_FrameWidth：" + m_VideoInputPt.m_FrameWidth + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_FrameHeight：" + m_VideoInputPt.m_FrameHeight + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_ScreenRotate：" + m_VideoInputPt.m_ScreenRotate + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_UseWhatEncoder：" + m_VideoInputPt.m_UseWhatEncoder + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderVideoType：" + m_VideoInputPt.m_OpenH264EncoderVideoType + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderEncodedBitrate：" + m_VideoInputPt.m_OpenH264EncoderEncodedBitrate + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderBitrateControlMode：" + m_VideoInputPt.m_OpenH264EncoderBitrateControlMode + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl：" + m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderCmplxt：" + m_VideoInputPt.m_OpenH264EncoderCmplxt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_SystemH264EncoderEncodedBitrate：" + m_VideoInputPt.m_SystemH264EncoderEncodedBitrate + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_SystemH264EncoderBitrateControlMode：" + m_VideoInputPt.m_SystemH264EncoderBitrateControlMode + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_SystemH264EncoderIDRFrameIntvlTimeSec：" + m_VideoInputPt.m_SystemH264EncoderIDRFrameIntvlTimeSec + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_SystemH264EncoderCmplxt：" + m_VideoInputPt.m_SystemH264EncoderCmplxt + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_UseWhatVideoInputDvc：" + m_VideoInputPt.m_UseWhatVideoInputDvc + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt：" + m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt + "\n" );
                        p_StngFileWriterPt.write( "m_VideoInputPt.m_VideoInputIsBlack：" + m_VideoInputPt.m_VideoInputIsBlack + "\n" );
                        p_StngFileWriterPt.write( "\n" );

                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_IsUseVideoOutput：" + m_VideoOutputPt.m_IsUseVideoOutput + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_UseWhatDecoder：" + m_VideoOutputPt.m_UseWhatDecoder + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_OpenH264DecoderDecodeThrdNum：" + m_VideoOutputPt.m_OpenH264DecoderDecodeThrdNum + "\n" );
                        p_StngFileWriterPt.write( "\n" );
                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_VideoOutputDisplaySurfaceViewPt：" + m_VideoOutputPt.m_VideoOutputDisplaySurfaceViewPt + "\n" );
                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_VideoOutputDisplayScale：" + m_VideoOutputPt.m_VideoOutputDisplayScale + "\n" );
                        p_StngFileWriterPt.write( "m_VideoOutputPt.m_VideoOutputIsBlack：" + m_VideoOutputPt.m_VideoOutputIsBlack + "\n" );
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
                if( m_AudioInputPt.m_IsUseAudioInput != 0 ) //如果要使用音频输入。
                {
                    //创建并初始化声学回音消除器。
                    if( m_AudioInputPt.m_UseWhatAec != 0 ) //如果要使用声学回音消除器。
                    {
                        if( m_AudioOutputPt.m_IsUseAudioOutput == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：不使用音频输出时，不能使用声学回音消除器。" );
                            break OutMediaInitAndPocs;
                        }
                        if( m_AudioOutputPt.m_SamplingRate != m_AudioInputPt.m_SamplingRate )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音频输出的采样频率与音频输入不一致时，不能使用声学回音消除器。" );
                            break OutMediaInitAndPocs;
                        }
                        if( m_AudioOutputPt.m_FrameLen != m_AudioInputPt.m_FrameLen )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：音频输出帧的长度与音频输入不一致时，不能使用声学回音消除器。" );
                            break OutMediaInitAndPocs;
                        }
                    }
                    switch( m_AudioInputPt.m_UseWhatAec )
                    {
                        case 0: //如果不使用声学回音消除器。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用声学回音消除器。" );
                            break;
                        }
                        case 1: //如果要使用Speex声学回音消除器。
                        {
                            if( m_AudioInputPt.m_SpeexAecIsSaveMemFile != 0 )
                            {
                                m_AudioInputPt.m_SpeexAecPt = new SpeexAec();
                                if( m_AudioInputPt.m_SpeexAecPt.InitByMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_SpeexAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                }
                            }
                            if( m_AudioInputPt.m_SpeexAecPt == null )
                            {
                                m_AudioInputPt.m_SpeexAecPt = new SpeexAec();
                                if( m_AudioInputPt.m_SpeexAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_SpeexAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                    break OutMediaInitAndPocs;
                                }
                            }
                            break;
                        }
                        case 2: //如果要使用WebRtc定点版声学回音消除器。
                        {
                            m_AudioInputPt.m_WebRtcAecmPt = new WebRtcAecm();
                            if( m_AudioInputPt.m_WebRtcAecmPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecmIsUseCNGMode, m_AudioInputPt.m_WebRtcAecmEchoMode, m_AudioInputPt.m_WebRtcAecmDelay, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcAecmPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                        case 3: //如果要使用WebRtc浮点版声学回音消除器。
                        {
                            if( m_AudioInputPt.m_WebRtcAecIsSaveMemFile != 0 )
                            {
                                m_AudioInputPt.m_WebRtcAecPt = new WebRtcAec();
                                if( m_AudioInputPt.m_WebRtcAecPt.InitByMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_WebRtcAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                }
                            }
                            if( m_AudioInputPt.m_WebRtcAecPt == null )
                            {
                                m_AudioInputPt.m_WebRtcAecPt = new WebRtcAec();
                                if( m_AudioInputPt.m_WebRtcAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_WebRtcAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                    break OutMediaInitAndPocs;
                                }
                            }
                            break;
                        }
                        case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                        {
                            m_AudioInputPt.m_SpeexWebRtcAecPt = new SpeexWebRtcAec();
                            if( m_AudioInputPt.m_SpeexWebRtcAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexWebRtcAecWorkMode, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec, m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_SpeexWebRtcAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                    }

                    //创建并初始化噪音抑制器对象。
                    switch( m_AudioInputPt.m_UseWhatNs )
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
                            m_AudioInputPt.m_WebRtcNsxPt = new WebRtcNsx();
                            if( m_AudioInputPt.m_WebRtcNsxPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcNsxPolicyMode, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcNsxPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                        case 3: //如果要使用WebRtc浮点版噪音抑制器。
                        {
                            m_AudioInputPt.m_WebRtcNsPt = new WebRtcNs();
                            if( m_AudioInputPt.m_WebRtcNsPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcNsPolicyMode, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcNsPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                        case 4: //如果要使用RNNoise噪音抑制器。
                        {
                            m_AudioInputPt.m_RNNoisePt = new RNNoise();
                            if( m_AudioInputPt.m_RNNoisePt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_RNNoisePt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                    }

                    //创建并初始化Speex预处理器。
                    if( ( m_AudioInputPt.m_UseWhatNs == 1 ) || ( m_AudioInputPt.m_IsUseSpeexPrpocsOther != 0 ) )
                    {
                        if( m_AudioInputPt.m_UseWhatNs != 1 )
                        {
                            m_AudioInputPt.m_SpeexPrpocsIsUseNs = 0;
                            m_AudioInputPt.m_SpeexPrpocsIsUseDereverb = 0;
                        }
                        if( m_AudioInputPt.m_IsUseSpeexPrpocsOther == 0 )
                        {
                            m_AudioInputPt.m_SpeexPrpocsIsUseVad = 0;
                            m_AudioInputPt.m_SpeexPrpocsIsUseAgc = 0;
                        }
                        m_AudioInputPt.m_SpeexPrpocsPt = new SpeexPrpocs();
                        if( m_AudioInputPt.m_SpeexPrpocsPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexPrpocsIsUseNs, m_AudioInputPt.m_SpeexPrpocsNoiseSupes, m_AudioInputPt.m_SpeexPrpocsIsUseDereverb, m_AudioInputPt.m_SpeexPrpocsIsUseVad, m_AudioInputPt.m_SpeexPrpocsVadProbStart, m_AudioInputPt.m_SpeexPrpocsVadProbCont, m_AudioInputPt.m_SpeexPrpocsIsUseAgc, m_AudioInputPt.m_SpeexPrpocsAgcLevel, m_AudioInputPt.m_SpeexPrpocsAgcIncrement, m_AudioInputPt.m_SpeexPrpocsAgcDecrement, m_AudioInputPt.m_SpeexPrpocsAgcMaxGain, m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器成功。"  );
                        }
                        else
                        {
                            m_AudioInputPt.m_SpeexPrpocsPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                            break OutMediaInitAndPocs;
                        }
                    }

                    //初始化编码器对象。
                    switch( m_AudioInputPt.m_UseWhatEncoder )
                    {
                        case 0: //如果要使用PCM原始数据。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
                            break;
                        }
                        case 1: //如果要使用Speex编码器。
                        {
                            if( m_AudioInputPt.m_FrameLen != m_AudioInputPt.m_SamplingRate / 1000 * 20 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的长度不为20毫秒不能使用Speex编码器。" );
                                break OutMediaInitAndPocs;
                            }
                            m_AudioInputPt.m_SpeexEncoderPt = new SpeexEncoder();
                            if( m_AudioInputPt.m_SpeexEncoderPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr, m_AudioInputPt.m_SpeexEncoderQualt, m_AudioInputPt.m_SpeexEncoderCmplxt, m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex编码器成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_SpeexEncoderPt = null;
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
                    if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                    {
                        m_AudioInputPt.m_AudioInputWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.Init( m_AudioInputPt.m_AudioInputFileFullPathStrPt, ( short ) 1, m_AudioInputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AudioInputPt.m_AudioInputFileFullPathStrPt + " 的Wave文件写入器成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioInputWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AudioInputPt.m_AudioInputFileFullPathStrPt + " 的Wave文件写入器失败。" );
                            break OutMediaInitAndPocs;
                        }
                        m_AudioInputPt.m_AudioResultWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.Init( m_AudioInputPt.m_AudioResultFileFullPathStrPt, ( short ) 1, m_AudioInputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AudioInputPt.m_AudioResultFileFullPathStrPt + " 的Wave文件写入器成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioResultWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AudioInputPt.m_AudioResultFileFullPathStrPt + " 的Wave文件写入器失败。" );
                            break OutMediaInitAndPocs;
                        }
                    }

                    //创建并初始化音频输入波形器、音频结果波形器。
                    if( m_AudioInputPt.m_IsDrawAudioOscilloToSurface != 0 )
                    {
                        m_AudioInputPt.m_AudioInputOscilloPt = new AudioOscillo();
                        if( m_AudioInputPt.m_AudioInputOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入波形器成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioInputOscilloPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                            break OutMediaInitAndPocs;
                        }
                        m_AudioInputPt.m_AudioResultOscilloPt = new AudioOscillo();
                        if( m_AudioInputPt.m_AudioResultOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果波形器成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioResultOscilloPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                            break OutMediaInitAndPocs;
                        }
                    }

                    //创建并初始化音频输入设备。
                    try
                    {
                        m_AudioInputPt.m_AudioInputDvcBufSz = AudioRecord.getMinBufferSize( m_AudioInputPt.m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                        m_AudioInputPt.m_AudioInputDvcBufSz = ( m_AudioInputPt.m_AudioInputDvcBufSz > m_AudioInputPt.m_FrameLen * 2 ) ? m_AudioInputPt.m_AudioInputDvcBufSz : m_AudioInputPt.m_FrameLen * 2;
                        m_AudioInputPt.m_AudioInputDvcPt = new AudioRecord(
                                ( m_AudioInputPt.m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
                                m_AudioInputPt.m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioInputPt.m_AudioInputDvcBufSz
                        );
                        if( m_AudioInputPt.m_AudioInputDvcPt.getState() == AudioRecord.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备成功。音频输入设备缓冲区大小：" + m_AudioInputPt.m_AudioInputDvcBufSz );
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
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入帧链表成功。" );

                    //创建并初始化音频输入空闲帧链表。
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入空闲帧链表成功。" );

                    //初始化音频输入线程的临时变量。
                    {
                        m_AudioInputPt.m_AudioInputFramePt = null; //初始化音频输入帧的指针。
                        m_AudioInputPt.m_AudioInputFrameLnkLstElmTotal = 0; //初始化音频输入帧链表的元数总数。
                        m_AudioInputPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
                        m_AudioInputPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音频输入线程的临时变量成功。" );
                    }

                    //创建并初始化音频输入线程。
                    m_AudioInputPt.m_AudioInputThrdExitFlag = 0; //设置音频输入线程退出标记为0表示保持运行。
                    m_AudioInputPt.m_AudioInputThrdPt = new AudioInputThrd();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入线程成功。" );
                } //初始化音频输入完毕。

                //初始化音频输出。
                if( m_AudioOutputPt.m_IsUseAudioOutput != 0 ) //如果要使用音频输出。
                {
                    //初始化解码器对象。
                    switch( m_AudioOutputPt.m_UseWhatDecoder )
                    {
                        case 0: //如果要使用PCM原始数据。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
                            break;
                        }
                        case 1: //如果要使用Speex解码器。
                        {
                            if( m_AudioOutputPt.m_FrameLen != m_AudioOutputPt.m_SamplingRate / 1000 * 20 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的长度不为20毫秒不能使用Speex解码器。" );
                                break OutMediaInitAndPocs;
                            }
                            m_AudioOutputPt.m_SpeexDecoderPt = new SpeexDecoder();
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Init( m_AudioOutputPt.m_SamplingRate, m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex解码器成功。" );
                            }
                            else
                            {
                                m_AudioOutputPt.m_SpeexDecoderPt = null;
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
                    if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                    {
                        m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.Init( m_AudioOutputPt.m_AudioOutputFileFullPathStrPt, ( short ) 1, m_AudioOutputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + " 的Wave文件写入器成功。" );
                        }
                        else
                        {
                            m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + " 的Wave文件写入器失败。" );
                            break OutMediaInitAndPocs;
                        }
                    }

                    //创建并初始化音频输出波形器。
                    if( m_AudioOutputPt.m_IsDrawAudioOscilloToSurface != 0 )
                    {
                        m_AudioOutputPt.m_AudioOutputOscilloPt = new AudioOscillo();
                        if( m_AudioOutputPt.m_AudioOutputOscilloPt.Init( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出波形器成功。" );
                        }
                        else
                        {
                            m_AudioOutputPt.m_AudioOutputOscilloPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                            break OutMediaInitAndPocs;
                        }
                    }

                    //设置音频输出设备。
                    if( m_AudioOutputPt.m_UseWhatAudioOutputDvc == 0 ) //如果要使用扬声器。
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
                        m_AudioOutputPt.m_AudioOutputDvcBufSz = m_AudioOutputPt.m_FrameLen * 2;
                        m_AudioOutputPt.m_AudioOutputDvcPt = new AudioTrack( ( m_AudioOutputPt.m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                m_AudioOutputPt.m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioOutputPt.m_AudioOutputDvcBufSz,
                                AudioTrack.MODE_STREAM );
                        if( m_AudioOutputPt.m_AudioOutputDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AudioOutputPt.m_AudioOutputDvcBufSz );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备失败。" );
                            m_AudioOutputPt.m_AudioOutputDvcPt.release();
                            m_AudioOutputPt.m_AudioOutputDvcPt = null;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备失败。原因：" + e.getMessage() );
                    }

                    //用第二种方法创建并初始化音频输出设备。
                    if( m_AudioOutputPt.m_AudioOutputDvcPt == null )
                    {
                        try
                        {
                            m_AudioOutputPt.m_AudioOutputDvcBufSz = AudioTrack.getMinBufferSize( m_AudioOutputPt.m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                            m_AudioOutputPt.m_AudioOutputDvcPt = new AudioTrack( ( m_AudioOutputPt.m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                    m_AudioOutputPt.m_SamplingRate,
                                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    m_AudioOutputPt.m_AudioOutputDvcBufSz,
                                    AudioTrack.MODE_STREAM );
                            if( m_AudioOutputPt.m_AudioOutputDvcPt.getState() == AudioTrack.STATE_INITIALIZED )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备成功。音频输出设备缓冲区大小：" + m_AudioOutputPt.m_AudioOutputDvcBufSz );
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
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出帧链表成功。" );

                    //创建并初始化音频输出空闲帧链表。
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出空闲帧链表成功。" );

                    //初始化音频输出线程的临时变量。
                    {
                        m_AudioOutputPt.m_AudioOutputFramePt = null; //初始化音频输出帧的指针。
                        m_AudioOutputPt.m_EncodedAudioOutputFramePt = ( m_AudioOutputPt.m_UseWhatDecoder != 0 ) ? new byte[ m_AudioOutputPt.m_FrameLen ] : null; //初始化已编码格式音频输出帧的指针。
                        m_AudioOutputPt.m_AudioOutputFrameLenPt = new HTLong(); //初始化音频输出帧的长度，单位字节。
                        m_AudioOutputPt.m_AudioOutputFrameLnkLstElmTotal = 0; //初始化音频输出帧链表的元数总数。
                        m_AudioOutputPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
                        m_AudioOutputPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化音频输出线程的临时变量成功。" );
                    }

                    //创建并初始化音频输出线程。
                    m_AudioOutputPt.m_AudioOutputThrdExitFlag = 0; //设置音频输出线程退出标记为0表示保持运行。
                    m_AudioOutputPt.m_AudioOutputThrdPt = new AudioOutputThrd();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出线程成功。" );
                } //初始化音频输出完毕。

                //初始化视频输入。
                if( m_VideoInputPt.m_IsUseVideoInput != 0 )
                {
                    //创建视频输入线程。
                    m_VideoInputPt.m_VideoInputThrdExitFlag = 0; //设置视频输入线程退出标记为0表示保持运行。
                    m_VideoInputPt.m_VideoInputThrdPt = new VideoInputThrd();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输入线程成功。" );

                    //创建并初始化视频输入设备。
                    {
                        //打开视频输入设备。
                        {
                            int p_CameraDvcId = 0;
                            Camera.CameraInfo p_CameraInfoPt = new Camera.CameraInfo();

                            //查找视频输入设备对应的ID。
                            if( m_VideoInputPt.m_UseWhatVideoInputDvc == 0 ) //如果要使用前置摄像头。
                            {
                                p_CameraDvcId = m_VideoInputPt.m_FrontCameraDvcId;
                            }
                            else if( m_VideoInputPt.m_UseWhatVideoInputDvc == 1 ) //如果要使用后置摄像头。
                            {
                                p_CameraDvcId = m_VideoInputPt.m_BackCameraDvcId;
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
                                        if( m_VideoInputPt.m_UseWhatVideoInputDvc == 0 ) break;
                                    }
                                    else if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_BACK )
                                    {
                                        if( m_VideoInputPt.m_UseWhatVideoInputDvc == 1 ) break;
                                    }
                                }
                                if( p_CameraDvcId == Camera.getNumberOfCameras() )
                                {
                                    String p_InfoStrPt = "媒体处理线程：查找视频输入设备对应的ID失败。原因：没有" + ( ( m_VideoInputPt.m_UseWhatVideoInputDvc == 0 ) ? "前置摄像头。" : "后置摄像头。" );
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                    if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                                    break OutMediaInitAndPocs;
                                }
                            }

                            //打开视频输入设备。
                            try
                            {
                                m_VideoInputPt.m_VideoInputDvcPt = Camera.open( p_CameraDvcId );
                            }
                            catch( RuntimeException e )
                            {
                                String p_InfoStrPt = "媒体处理线程：创建并初始化视频输入设备失败。原因：打开视频输入设备失败。原因：" + e.getMessage();
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                                break OutMediaInitAndPocs;
                            }
                        }

                        Camera.Parameters p_CameraParaPt = m_VideoInputPt.m_VideoInputDvcPt.getParameters(); //获取视频输入设备的参数。

                        p_CameraParaPt.setPreviewFormat( ImageFormat.NV21 ); //设置预览帧的格式。

                        p_CameraParaPt.setPreviewFrameRate( m_VideoInputPt.m_MaxSamplingRate ); //设置最大采样频率。

                        //选择合适的视频输入设备帧大小。
                        int p_VideoInputTargetFrameWidth = m_VideoInputPt.m_FrameHeight; //存放视频输入目标帧的宽度，单位为像素。
                        int p_VideoInputTargetFrameHeight = m_VideoInputPt.m_FrameWidth; //存放视频输入目标帧的高度，单位为像素。
                        double p_TargetFrameWidthToHeightRatio = ( double )p_VideoInputTargetFrameWidth / ( double )p_VideoInputTargetFrameHeight; //存放目标帧的宽高比。
                        List< Camera.Size > p_SupportedPreviewSizesListPt = p_CameraParaPt.getSupportedPreviewSizes(); //设置视频输入设备支持的预览帧大小。
                        Camera.Size p_CameraSizePt; //存放本次的帧大小。
                        double p_VideoInputDvcFrameWidthToHeightRatio; //存放本次视频输入设备帧的宽高比。
                        int p_VideoInputDvcFrameCropX; //存放本次视频输入设备帧裁剪区域左上角的横坐标，单位像素。
                        int p_VideoInputDvcFrameCropY; //存放本次视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
                        int p_VideoInputDvcFrameCropWidth; //存放本次视频输入设备帧裁剪区域的宽度，单位像素。
                        int p_VideoInputDvcFrameCropHeight; //存放本次视频输入设备帧裁剪区域的高度，单位像素。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备目标的帧大小：width：" + p_VideoInputTargetFrameWidth + " height：" + p_VideoInputTargetFrameHeight );
                        for( p_TmpInt321 = 0; p_TmpInt321 < p_SupportedPreviewSizesListPt.size(); p_TmpInt321++ )
                        {
                            p_CameraSizePt = p_SupportedPreviewSizesListPt.get( p_TmpInt321 );
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的帧大小：width：" + p_CameraSizePt.width + " height：" + p_CameraSizePt.height );

                            //设置本次视频输入设备帧的宽高比、裁剪宽度、裁剪高度。
                            p_VideoInputDvcFrameWidthToHeightRatio = ( double )p_CameraSizePt.width / ( double )p_CameraSizePt.height;
                            if( p_VideoInputDvcFrameWidthToHeightRatio >= p_TargetFrameWidthToHeightRatio ) //如果本次视频输入设备帧的宽高比目标帧的大，就表示需要裁剪宽度。
                            {
                                p_VideoInputDvcFrameCropWidth = ( int )( ( double )p_CameraSizePt.height * p_TargetFrameWidthToHeightRatio ); //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
                                p_VideoInputDvcFrameCropWidth -= p_VideoInputDvcFrameCropWidth % 2;
                                p_VideoInputDvcFrameCropHeight = p_CameraSizePt.height; //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。

                                p_VideoInputDvcFrameCropX = ( p_CameraSizePt.width - p_VideoInputDvcFrameCropWidth ) / 2; //设置本次视频输入设备帧裁剪区域左上角的横坐标，使裁剪区域居中。
                                p_VideoInputDvcFrameCropX -= p_VideoInputDvcFrameCropX % 2;
                                p_VideoInputDvcFrameCropY = 0; //设置本次视频输入设备帧裁剪区域左上角的纵坐标。
                            }
                            else //如果本次视频输入设备帧的宽高比指定帧的小，就表示需要裁剪高度。
                            {
                                p_VideoInputDvcFrameCropWidth = p_CameraSizePt.width; //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
                                p_VideoInputDvcFrameCropHeight = ( int )( ( double )p_CameraSizePt.width / p_TargetFrameWidthToHeightRatio ); //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。
                                p_VideoInputDvcFrameCropHeight -= p_VideoInputDvcFrameCropHeight % 2;

                                p_VideoInputDvcFrameCropX = 0; //设置本次视频输入设备帧裁剪区域左上角的横坐标。
                                p_VideoInputDvcFrameCropY = ( p_CameraSizePt.height - p_VideoInputDvcFrameCropHeight ) / 2; //设置本次视频输入设备帧裁剪区域左上角的纵坐标，使裁剪区域居中。
                                p_VideoInputDvcFrameCropY -= p_VideoInputDvcFrameCropY % 2;
                            }

                            //如果选择的帧裁剪区域不满足指定的（包括选择的帧裁剪区域为0），但是本次的帧裁剪区域比选择的高，就设置选择的为本次的。
                            //如果本次的帧裁剪区域满足指定的（选择的帧裁剪区域肯定也满足指定的，如果选择的帧裁剪区域不满足指定的，那么就会走上一条判断），但是本次的帧裁剪区域比选择的低，就设置选择的为本次的。
                            if(
                                (
                                  ( ( m_VideoInputPt.m_VideoInputDvcFrameCropWidth < p_VideoInputTargetFrameWidth ) || ( m_VideoInputPt.m_VideoInputDvcFrameCropHeight < p_VideoInputTargetFrameHeight ) )
                                  &&
                                  ( ( p_VideoInputDvcFrameCropWidth > m_VideoInputPt.m_VideoInputDvcFrameCropWidth ) && ( p_VideoInputDvcFrameCropHeight > m_VideoInputPt.m_VideoInputDvcFrameCropHeight ) )
                                )
                                ||
                                (
                                  ( ( p_VideoInputDvcFrameCropWidth >= p_VideoInputTargetFrameWidth ) && ( p_VideoInputDvcFrameCropHeight >= p_VideoInputTargetFrameHeight ) )
                                  &&
                                  (
                                    ( ( p_VideoInputDvcFrameCropWidth < m_VideoInputPt.m_VideoInputDvcFrameCropWidth ) || ( p_VideoInputDvcFrameCropHeight < m_VideoInputPt.m_VideoInputDvcFrameCropHeight ) )
                                    ||
                                    ( ( p_VideoInputDvcFrameCropWidth == m_VideoInputPt.m_VideoInputDvcFrameCropWidth ) && ( p_VideoInputDvcFrameCropHeight == m_VideoInputPt.m_VideoInputDvcFrameCropHeight ) && ( p_VideoInputDvcFrameCropX + p_VideoInputDvcFrameCropY < m_VideoInputPt.m_VideoInputDvcFrameCropX + m_VideoInputPt.m_VideoInputDvcFrameCropY ) )
                                  )
                                )
                              )
                            {
                                m_VideoInputPt.m_VideoInputDvcFrameWidth = p_CameraSizePt.width;
                                m_VideoInputPt.m_VideoInputDvcFrameHeight = p_CameraSizePt.height;

                                m_VideoInputPt.m_VideoInputDvcFrameCropX = p_VideoInputDvcFrameCropX;
                                m_VideoInputPt.m_VideoInputDvcFrameCropY = p_VideoInputDvcFrameCropY;
                                m_VideoInputPt.m_VideoInputDvcFrameCropWidth = p_VideoInputDvcFrameCropWidth;
                                m_VideoInputPt.m_VideoInputDvcFrameCropHeight = p_VideoInputDvcFrameCropHeight;
                            }
                        }
                        p_CameraParaPt.setPreviewSize( m_VideoInputPt.m_VideoInputDvcFrameWidth, m_VideoInputPt.m_VideoInputDvcFrameHeight ); //设置预览帧的宽度为设置的高度，预览帧的高度为设置的宽度，因为预览帧处理的时候要旋转。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备选择的帧大小：width：" + m_VideoInputPt.m_VideoInputDvcFrameWidth + " height：" + m_VideoInputPt.m_VideoInputDvcFrameHeight );

                        //判断视频输入设备帧是否裁剪。
                        if(
                            ( m_VideoInputPt.m_VideoInputDvcFrameWidth > m_VideoInputPt.m_VideoInputDvcFrameCropWidth ) //如果视频输入设备帧的宽度比裁剪宽度大，就表示需要裁剪宽度。
                            ||
                            ( m_VideoInputPt.m_VideoInputDvcFrameHeight > m_VideoInputPt.m_VideoInputDvcFrameCropHeight ) //如果视频输入设备帧的高度比裁剪高度大，就表示需要裁剪高度。
                          )
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameIsCrop = 1; //设置视频输入设备帧要裁剪。
                        }
                        else //如果视频输入设备帧的宽度和高度与裁剪宽度和高度一致，就表示不需要裁剪。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameIsCrop = 0; //设置视频输入设备帧不裁剪。
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否裁剪：" + m_VideoInputPt.m_VideoInputDvcFrameIsCrop + "  左上角的横坐标：" + m_VideoInputPt.m_VideoInputDvcFrameCropX + "  纵坐标：" + m_VideoInputPt.m_VideoInputDvcFrameCropY + "  裁剪区域的宽度：" + m_VideoInputPt.m_VideoInputDvcFrameCropWidth + "  高度：" + m_VideoInputPt.m_VideoInputDvcFrameCropHeight + "。" );

                        //设置视频输入设备帧的旋转。
                        if( m_VideoInputPt.m_UseWhatVideoInputDvc == 0 ) //如果要使用前置摄像头。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameRotate = ( 270 + m_VideoInputPt.m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
                        }
                        else //如果要使用后置摄像头。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameRotate = ( 450 - m_VideoInputPt.m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
                        }
                        if( ( m_VideoInputPt.m_VideoInputDvcFrameRotate == 0 ) || ( m_VideoInputPt.m_VideoInputDvcFrameRotate == 180 ) ) //如果旋转后为横屏。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameRotateWidth = m_VideoInputPt.m_VideoInputDvcFrameCropWidth; //设置视频输入设备帧旋转后的宽度。
                            m_VideoInputPt.m_VideoInputDvcFrameRotateHeight = m_VideoInputPt.m_VideoInputDvcFrameCropHeight; //设置视频输入设备帧旋转后的高度。
                        }
                        else //如果旋转后为竖屏。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameRotateWidth = m_VideoInputPt.m_VideoInputDvcFrameCropHeight; //设置视频输入设备帧旋转后的宽度。
                            m_VideoInputPt.m_VideoInputDvcFrameRotateHeight = m_VideoInputPt.m_VideoInputDvcFrameCropWidth; //设置视频输入设备帧旋转后的高度。
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧旋转后的宽度：" + m_VideoInputPt.m_VideoInputDvcFrameRotateWidth + "，旋转后的高度：" + m_VideoInputPt.m_VideoInputDvcFrameRotateHeight + "。" );

                        //判断视频输入设备帧是否缩放。
                        if( ( m_VideoInputPt.m_VideoInputDvcFrameCropWidth != p_VideoInputTargetFrameWidth ) || ( m_VideoInputPt.m_VideoInputDvcFrameCropHeight != p_VideoInputTargetFrameHeight ) )
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameIsScale = 1; //设置视频输入设备帧要缩放。
                        }
                        else
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameIsScale = 0; //设置视频输入设备帧不缩放。
                        }
                        if( ( m_VideoInputPt.m_VideoInputDvcFrameRotate == 0 ) || ( m_VideoInputPt.m_VideoInputDvcFrameRotate == 180 ) ) //如果旋转后为横屏。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameScaleWidth = p_VideoInputTargetFrameWidth; //设置视频输入设备帧缩放后的宽度。
                            m_VideoInputPt.m_VideoInputDvcFrameScaleHeight = p_VideoInputTargetFrameHeight; //设置视频输入设备帧缩放后的高度。
                        }
                        else //如果旋转后为竖屏。
                        {
                            m_VideoInputPt.m_VideoInputDvcFrameScaleWidth = p_VideoInputTargetFrameHeight; //设置视频输入设备帧缩放后的宽度。
                            m_VideoInputPt.m_VideoInputDvcFrameScaleHeight = p_VideoInputTargetFrameWidth; //设置视频输入设备帧缩放后的高度。
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否缩放：" + m_VideoInputPt.m_VideoInputDvcFrameIsScale + "，缩放后的宽度：" + m_VideoInputPt.m_VideoInputDvcFrameScaleWidth + "，缩放后的高度：" + m_VideoInputPt.m_VideoInputDvcFrameScaleHeight + "。" );

                        //设置视频输入设备的对焦模式。
                        List<String> p_FocusModesListPt = p_CameraParaPt.getSupportedFocusModes();
                        String p_PreviewFocusModePt = "";
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
                                    p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                                    break;
                                case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE: //用于拍照的连续自动对焦模式，比视频的连续自动对焦模式对焦速度更快。
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_PICTURE，用于拍照的连续自动对焦模式。" );
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                                    break;
                                case Camera.Parameters.FOCUS_MODE_EDOF: //扩展景深（EDOF）对焦模式，对焦以数字方式连续进行。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_EDOF，扩展景深（EDOF）对焦模式。" );
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_EDOF;
                                    break;
                                case Camera.Parameters.FOCUS_MODE_FIXED: //固定焦点对焦模式。如果焦点无法调节，则相机始终处于此模式。如果相机具有自动对焦，则此模式可以固定焦点，通常处于超焦距。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_FIXED，固定焦点对焦模式。" );
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                            !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
                                            !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_FIXED;
                                    break;
                                case Camera.Parameters.FOCUS_MODE_INFINITY: //无限远焦点对焦模式。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_INFINITY，无限远焦点对焦模式。" );
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_FIXED ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_INFINITY;
                                    break;
                            }
                        }
                        p_CameraParaPt.setFocusMode( p_PreviewFocusModePt ); //设置对焦模式。

                        try
                        {
                            m_VideoInputPt.m_VideoInputDvcPt.setParameters( p_CameraParaPt ); //设置参数到视频输入设备。
                        }
                        catch( RuntimeException e )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage() );
                            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, "媒体处理线程：创建并初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage(), Toast.LENGTH_LONG ).show(); } } );
                            break OutMediaInitAndPocs;
                        }

                        try
                        {
                            m_VideoInputPt.m_VideoInputDvcPt.setPreviewDisplay( m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt.getHolder() ); //设置视频输入预览SurfaceView。
                            if( m_VideoInputPt.m_ScreenRotate == 0 || m_VideoInputPt.m_ScreenRotate == 180 ) //如果屏幕为竖屏。
                            {
                                m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt.setWidthToHeightRatio( ( float )m_VideoInputPt.m_VideoInputDvcFrameHeight / m_VideoInputPt.m_VideoInputDvcFrameWidth ); //设置视频输入预览SurfaceView的宽高比。
                            }
                            else //如果屏幕为横屏。
                            {
                                m_VideoInputPt.m_VideoInputPreviewSurfaceViewPt.setWidthToHeightRatio( ( float )m_VideoInputPt.m_VideoInputDvcFrameWidth / m_VideoInputPt.m_VideoInputDvcFrameHeight ); //设置视频输入预览SurfaceView的宽高比。
                            }
                        }
                        catch( Exception ignored )
                        {
                        }
                        m_VideoInputPt.m_VideoInputDvcPt.setDisplayOrientation( ( 450 - m_VideoInputPt.m_ScreenRotate ) % 360 ); //调整相机拍到的图像旋转，不然竖着拿手机，图像是横着的。

                        //设置视频输入预览回调函数缓冲区的指针。
                        m_VideoInputPt.m_VideoInputPreviewCallbackBufferPtPt = new byte[ m_VideoInputPt.m_MaxSamplingRate ][ m_VideoInputPt.m_VideoInputDvcFrameWidth * m_VideoInputPt.m_VideoInputDvcFrameHeight * 3 / 2 ];
                        for( p_TmpInt321 = 0; p_TmpInt321 < m_VideoInputPt.m_MaxSamplingRate; p_TmpInt321++ )
                            m_VideoInputPt.m_VideoInputDvcPt.addCallbackBuffer( m_VideoInputPt.m_VideoInputPreviewCallbackBufferPtPt[p_TmpInt321] );

                        m_VideoInputPt.m_VideoInputDvcPt.setPreviewCallbackWithBuffer( m_VideoInputPt.m_VideoInputThrdPt ); //设置视频输入预览回调函数。

                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备成功。" );
                    }

                    //初始化编码器对象。
                    switch( m_VideoInputPt.m_UseWhatEncoder )
                    {
                        case 0: //如果要使用YU12原始数据。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用YU12原始数据。" );
                            break;
                        }
                        case 1: //如果要使用OpenH264编码器。
                        {
                            m_VideoInputPt.m_OpenH264EncoderPt = new OpenH264Encoder();
                            if( m_VideoInputPt.m_OpenH264EncoderPt.Init( m_VideoInputPt.m_VideoInputDvcFrameScaleWidth, m_VideoInputPt.m_VideoInputDvcFrameScaleHeight, m_VideoInputPt.m_OpenH264EncoderVideoType, m_VideoInputPt.m_OpenH264EncoderEncodedBitrate, m_VideoInputPt.m_OpenH264EncoderBitrateControlMode, m_VideoInputPt.m_MaxSamplingRate, m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl, m_VideoInputPt.m_OpenH264EncoderCmplxt, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器成功。" );
                            }
                            else
                            {
                                m_VideoInputPt.m_OpenH264EncoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                        case 2: //如果要使用系统自带H264编码器。
                        {
                            m_VideoInputPt.m_SystemH264EncoderPt = new SystemH264Encoder();
                            if( m_VideoInputPt.m_SystemH264EncoderPt.Init( m_VideoInputPt.m_VideoInputDvcFrameScaleWidth, m_VideoInputPt.m_VideoInputDvcFrameScaleHeight, m_VideoInputPt.m_SystemH264EncoderEncodedBitrate, m_VideoInputPt.m_SystemH264EncoderBitrateControlMode, m_VideoInputPt.m_MaxSamplingRate, m_VideoInputPt.m_SystemH264EncoderIDRFrameIntvlTimeSec, m_VideoInputPt.m_SystemH264EncoderCmplxt, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264编码器成功。" );
                            }
                            else
                            {
                                m_VideoInputPt.m_SystemH264EncoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264编码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                    }

                    //初始化视频输入线程的临时变量。
                    {
                        m_VideoInputPt.m_VideoInputFramePt = null; //初始化视频输入帧的指针。
                        if( m_VideoInputPt.m_FrameWidth * m_VideoInputPt.m_FrameHeight >= m_VideoInputPt.m_VideoInputDvcFrameWidth * m_VideoInputPt.m_VideoInputDvcFrameHeight ) //如果视频输入帧指定的大小大于等于视频输入设备帧的大小。
                        {
                            m_VideoInputPt.m_VideoInputResultFrameSz = m_VideoInputPt.m_FrameWidth * m_VideoInputPt.m_FrameHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
                        }
                        else //如果视频输入帧指定的大小小于视频输入设备帧的大小。
                        {
                            m_VideoInputPt.m_VideoInputResultFrameSz = m_VideoInputPt.m_VideoInputDvcFrameWidth * m_VideoInputPt.m_VideoInputDvcFrameHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
                        }
                        m_VideoInputPt.m_VideoInputResultFramePt = new byte[( int )m_VideoInputPt.m_VideoInputResultFrameSz]; //初始化视频输入结果帧的指针。
                        m_VideoInputPt.m_VideoInputTmpFramePt = new byte[( int )m_VideoInputPt.m_VideoInputResultFrameSz]; //初始化视频输入临时帧的指针。
                        m_VideoInputPt.m_VideoInputSwapFramePt = null; //初始化视频输入交换帧的指针。
                        m_VideoInputPt.m_VideoInputResultFrameLenPt = new HTLong(); //初始化视频输入结果帧的长度。
                        m_VideoInputPt.m_VideoInputFrameElmPt = null; //初始化视频输入帧元素的指针。
                        m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal = 0; //初始化视频输入帧链表的元数总数。
                        m_VideoInputPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
                        m_VideoInputPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化视频输入线程的临时变量成功。" );
                    }

                    //创建并初始化NV21格式视频输入帧链表。
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt = new LinkedList< byte[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化NV21格式视频输入帧链表成功。" );

                    //创建并初始化视频输入帧链表。
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt = new LinkedList< VideoInput.VideoInputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入帧链表成功。" );

                    //创建并初始化视频输入空闲帧链表。
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt = new LinkedList< VideoInput.VideoInputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入空闲帧链表成功。" );
                } //初始化视频输入完毕。

                //初始化视频输出。
                if( m_VideoOutputPt.m_IsUseVideoOutput != 0 )
                {
                    //初始化解码器对象。
                    switch( m_VideoOutputPt.m_UseWhatDecoder )
                    {
                        case 0: //如果要使用YU12原始数据。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用YU12原始数据。" );
                            break;
                        }
                        case 1: //如果要使用OpenH264解码器。
                        {
                            m_VideoOutputPt.m_OpenH264DecoderPt = new OpenH264Decoder();
                            if( m_VideoOutputPt.m_OpenH264DecoderPt.Init( m_VideoOutputPt.m_OpenH264DecoderDecodeThrdNum, m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器成功。" );
                            }
                            else
                            {
                                m_VideoOutputPt.m_OpenH264DecoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                        case 2: //如果要使用系统自带H264解码器。
                        {
                            m_VideoOutputPt.m_SystemH264DecoderPt = new SystemH264Decoder();
                            if( m_VideoOutputPt.m_SystemH264DecoderPt.Init( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264解码器成功。" );
                            }
                            else
                            {
                                m_VideoOutputPt.m_SystemH264DecoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化系统自带H264解码器失败。" );
                                break OutMediaInitAndPocs;
                            }
                            break;
                        }
                    }

                    //初始化视频输出线程的临时变量。
                    {
                        m_VideoOutputPt.m_VideoOutputResultFramePt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出结果帧的指针。
                        m_VideoOutputPt.m_VideoOutputTmpFramePt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出临时帧的指针。
                        m_VideoOutputPt.m_VideoOutputSwapFramePt = null; //初始化视频输出交换帧的指针。
                        m_VideoOutputPt.m_VideoOutputResultFrameLenPt = new HTLong(); //初始化视频输出结果帧的长度。
                        m_VideoOutputPt.m_VideoOutputFrameWidthPt = new HTInt(); //初始化视频输出帧的宽度。
                        m_VideoOutputPt.m_VideoOutputFrameHeightPt = new HTInt(); //初始化视频输出帧的高度。
                        m_VideoOutputPt.m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
                        m_VideoOutputPt.m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化视频输出线程的临时变量成功。" );
                    }
                    
                    //创建视频输出线程。
                    m_VideoOutputPt.m_VideoOutputThrdExitFlag = 0; //设置视频输出线程退出标记为0表示保持运行。
                    m_VideoOutputPt.m_VideoOutputThrdPt = new VideoOutputThrd();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输出线程成功。" );
                } //初始化视频输出完毕。

                //初始化媒体处理线程的临时变量。
                {
                    m_PcmAudioInputFramePt = null;
                    m_PcmAudioOutputFramePt = null;
                    m_PcmAudioResultFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new short[ m_AudioInputPt.m_FrameLen ] : null;
                    m_PcmAudioTmpFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new short[ m_AudioInputPt.m_FrameLen ] : null;
                    m_PcmAudioSwapFramePt = null;
                    m_VoiceActStsPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new HTInt( 1 ) : null; //语音活动状态预设为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
                    m_EncodedAudioInputFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new byte[ m_AudioInputPt.m_FrameLen ] : null;
                    m_EncodedAudioInputFrameLenPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new HTLong( 0 ) : null;
                    m_EncodedAudioInputFrameIsNeedTransPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new HTInt( 1 ) : null; //已编码格式音频输入帧是否需要传输预设为1，为了让在不使用非连续传输的情况下永远都是需要传输。
                    m_VideoInputFramePt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：初始化媒体处理线程的临时变量成功。" );
                } //初始化媒体处理线程的临时变量完毕。

                //启动音频输入线程、音频输出线程、视频输入线程、视频输出线程。必须在初始化最后启动这些线程，因为这些线程会使用初始化时的相关。
                {
                    if( m_AudioInputPt.m_AudioInputThrdPt != null ) //如果要使用音频输入线程。
                    {
                        m_AudioInputPt.m_AudioInputThrdPt.start(); //启动音频输入线程，让音频输入线程再去启动音频输出线程。
                    }
                    else if( m_AudioOutputPt.m_AudioOutputDvcPt != null ) //如果要使用音频输出线程。
                    {
                        m_AudioOutputPt.m_AudioOutputDvcPt.play(); //让音频输出设备开始播放。
                        m_AudioOutputPt.m_AudioOutputThrdPt.start(); //启动音频输出线程。
                    }

                    if( m_VideoInputPt.m_VideoInputDvcPt != null ) //如果要使用视频输入设备。
                    {
                        try
                        {
                            m_VideoInputPt.m_VideoInputDvcPt.startPreview(); //让视频输入设备开始预览。
                        }
                        catch( RuntimeException e )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：让视频输入设备开始预览失败。原因：" + e.getMessage() );
                            break OutMediaInitAndPocs;
                        }
                        m_VideoInputPt.m_VideoInputThrdPt.start(); //启动视频输入线程。
                    }

                    if( m_VideoOutputPt.m_VideoOutputThrdPt != null ) //如果要使用视频输出线程。
                    {
                        m_VideoOutputPt.m_VideoOutputThrdPt.start(); //启动视频输出线程。
                    }
                }

                if( m_IsPrintLogcat != 0 )
                {
                    p_NowMsec = System.currentTimeMillis();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：媒体处理线程初始化完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，正式开始处理帧。" );
                }

                m_ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。
                m_RunFlag = RUN_FLAG_PROC; //设置本线程运行标记为初始化完毕正在循环处理帧。

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
                    if( m_AudioInputPt.m_AudioInputFrameLnkLstPt != null ) p_TmpInt321 = m_AudioInputPt.m_AudioInputFrameLnkLstPt.size(); //获取音频输入帧链表的元素个数。
                    else p_TmpInt321 = 0;
                    if( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt != null ) p_TmpInt322 = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.size(); //获取音频输出帧链表的元素个数。
                    else p_TmpInt322 = 0;
                    if( ( p_TmpInt321 > 0 ) && ( p_TmpInt322 > 0 ) ) //如果音频输入帧链表和音频输出帧链表中都有帧了，才开始取出。
                    {
                        //从音频输入帧链表中取出第一个音频输入帧。
                        synchronized( m_AudioInputPt.m_AudioInputFrameLnkLstPt )
                        {
                            m_PcmAudioInputFramePt = m_AudioInputPt.m_AudioInputFrameLnkLstPt.getFirst();
                            m_AudioInputPt.m_AudioInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

                        //从音频输出帧链表中取出第一个音频输出帧。
                        synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                        {
                            m_PcmAudioOutputFramePt = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.getFirst();
                            m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );

                        //将音频输入帧复制到音频结果帧，方便处理。
                        System.arraycopy( m_PcmAudioInputFramePt, 0, m_PcmAudioResultFramePt, 0, m_PcmAudioInputFramePt.length );
                    }
                    else if( ( p_TmpInt321 > 0 ) && ( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt == null ) ) //如果音频输入帧链表有帧了，且不使用音频输出帧链表，就开始取出。
                    {
                        //从音频输入帧链表中取出第一个音频输入帧。
                        synchronized( m_AudioInputPt.m_AudioInputFrameLnkLstPt )
                        {
                            m_PcmAudioInputFramePt = m_AudioInputPt.m_AudioInputFrameLnkLstPt.getFirst();
                            m_AudioInputPt.m_AudioInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

                        //将音频输入帧复制到音频结果帧，方便处理。
                        System.arraycopy( m_PcmAudioInputFramePt, 0, m_PcmAudioResultFramePt, 0, m_PcmAudioInputFramePt.length );
                    }
                    else if( ( p_TmpInt322 > 0 ) && ( m_AudioInputPt.m_AudioInputFrameLnkLstPt == null ) ) //如果音频输出帧链表有帧了，且不使用音频输入帧链表，就开始取出。
                    {
                        //从音频输出帧链表中取出第一个音频输出帧。
                        synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                        {
                            m_PcmAudioOutputFramePt = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.getFirst();
                            m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );
                    }

                    //处理音频输入帧。
                    if( m_PcmAudioInputFramePt != null )
                    {
                        //使用声学回音消除器。
                        switch( m_AudioInputPt.m_UseWhatAec )
                        {
                            case 0: //如果不使用声学回音消除器。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：不使用声学回音消除器。" );
                                break;
                            }
                            case 1: //如果要使用Speex声学回音消除器。
                            {
                                if( ( m_AudioInputPt.m_SpeexAecPt != null ) && ( m_AudioInputPt.m_SpeexAecPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioOutputFramePt, m_PcmAudioTmpFramePt ) == 0 ) )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 2: //如果要使用WebRtc定点版声学回音消除器。
                            {
                                if( ( m_AudioInputPt.m_WebRtcAecmPt != null ) && ( m_AudioInputPt.m_WebRtcAecmPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioOutputFramePt, m_PcmAudioTmpFramePt ) == 0 ) )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 3: //如果要使用WebRtc浮点版声学回音消除器。
                            {
                                if( ( m_AudioInputPt.m_WebRtcAecPt != null ) && ( m_AudioInputPt.m_WebRtcAecPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioOutputFramePt, m_PcmAudioTmpFramePt ) == 0 ) )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                            {
                                if( ( m_AudioInputPt.m_SpeexWebRtcAecPt != null ) && ( m_AudioInputPt.m_SpeexWebRtcAecPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioOutputFramePt, m_PcmAudioTmpFramePt ) == 0 ) )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器失败。" );
                                }
                                break;
                            }
                        }

                        //使用噪音抑制器。
                        switch( m_AudioInputPt.m_UseWhatNs )
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
                                if( m_AudioInputPt.m_WebRtcNsxPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 3: //如果要使用WebRtc浮点版噪音抑制器。
                            {
                                if( m_AudioInputPt.m_WebRtcNsPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 4: //如果要使用RNNoise噪音抑制器。
                            {
                                if( m_AudioInputPt.m_RNNoisePt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器成功。" );
                                    m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器失败。" );
                                }
                                break;
                            }
                        }

                        //使用Speex预处理器。
                        if( ( m_AudioInputPt.m_UseWhatNs == 1 ) || ( m_AudioInputPt.m_IsUseSpeexPrpocsOther != 0 ) )
                        {
                            if( m_AudioInputPt.m_SpeexPrpocsPt.Pocs( m_PcmAudioResultFramePt, m_PcmAudioTmpFramePt, m_VoiceActStsPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器成功。语音活动状态：" + m_VoiceActStsPt.m_Val );
                                m_PcmAudioSwapFramePt = m_PcmAudioResultFramePt;m_PcmAudioResultFramePt = m_PcmAudioTmpFramePt;m_PcmAudioTmpFramePt = m_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器失败。" );
                            }
                        }

                        //判断音频输入是否静音。在音频输入处理完后再设置静音，这样可以保证音频输入处理器的连续性。
                        if( m_AudioInputPt.m_AudioInputIsMute != 0 )
                        {
                            Arrays.fill( m_PcmAudioResultFramePt, ( short ) 0 );
                            if( ( m_AudioInputPt.m_IsUseSpeexPrpocsOther != 0 ) && ( m_AudioInputPt.m_SpeexPrpocsIsUseVad != 0 ) ) //如果Speex预处理器要使用其他功能，且要使用语音活动检测。
                            {
                                m_VoiceActStsPt.m_Val = 0;
                            }
                        }

                        //使用编码器。
                        switch( m_AudioInputPt.m_UseWhatEncoder )
                        {
                            case 0: //如果要使用PCM原始数据。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
                                break;
                            }
                            case 1: //如果要使用Speex编码器。
                            {
                                if( m_AudioInputPt.m_SpeexEncoderPt.Pocs( m_PcmAudioResultFramePt, m_EncodedAudioInputFramePt, m_EncodedAudioInputFramePt.length, m_EncodedAudioInputFrameLenPt, m_EncodedAudioInputFrameIsNeedTransPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器成功。Speex格式音频输入帧的长度：" + m_EncodedAudioInputFrameLenPt.m_Val + "，Speex格式音频输入帧是否需要传输：" + m_EncodedAudioInputFrameIsNeedTransPt.m_Val );
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
                        if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                        {
                            if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.WriteData( m_PcmAudioInputFramePt, m_PcmAudioInputFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧失败。" );
                            }
                            if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.WriteData( m_PcmAudioResultFramePt, m_PcmAudioResultFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧失败。" );
                            }
                        }

                        //使用音频输入波形器绘制音频输入波形到Surface、音频结果波形器绘制音频结果波形到Surface。
                        if( m_AudioInputPt.m_IsDrawAudioOscilloToSurface != 0 )
                        {
                            if( m_AudioInputPt.m_AudioInputOscilloPt.Draw( m_PcmAudioInputFramePt, m_PcmAudioInputFramePt.length, m_AudioInputPt.m_AudioInputOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入波形器绘制音频输入波形到Surface失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                            }
                            if( m_AudioInputPt.m_AudioResultOscilloPt.Draw( m_PcmAudioResultFramePt, m_PcmAudioResultFramePt.length, m_AudioInputPt.m_AudioResultOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
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
                    if( m_PcmAudioOutputFramePt != null )
                    {
                        //使用音频输出Wave文件写入器写入输出帧数据。
                        if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                        {
                            if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.WriteData( m_PcmAudioOutputFramePt, m_PcmAudioOutputFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧失败。" );
                            }
                        }

                        //使用音频输出波形器绘制音频输出波形到Surface。
                        if( m_AudioOutputPt.m_IsDrawAudioOscilloToSurface != 0 )
                        {
                            if( m_AudioOutputPt.m_AudioOutputOscilloPt.Draw( m_PcmAudioOutputFramePt, m_PcmAudioOutputFramePt.length, m_AudioOutputPt.m_AudioOutputOscilloSurfacePt.getHolder().getSurface(), m_ErrInfoVarStrPt ) == 0 )
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
                    if( ( m_VideoInputPt.m_VideoInputFrameLnkLstPt != null ) && ( ( p_TmpInt321 = m_VideoInputPt.m_VideoInputFrameLnkLstPt.size() ) > 0 ) && //如果要使用视频输入，且视频输入帧链表中有帧了。
                        ( ( m_PcmAudioInputFramePt != null ) || ( m_AudioInputPt.m_AudioInputFrameLnkLstPt == null ) ) ) //且已经处理了音频输入帧或不使用音频输入帧链表。
                    {
                        //从视频输入帧链表中取出第一个视频输入帧。
                        synchronized( m_VideoInputPt.m_VideoInputFrameLnkLstPt )
                        {
                            m_VideoInputFramePt = m_VideoInputPt.m_VideoInputFrameLnkLstPt.getFirst();
                            m_VideoInputPt.m_VideoInputFrameLnkLstPt.removeFirst();
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
                    if( ( m_PcmAudioInputFramePt != null ) || ( m_VideoInputFramePt != null ) ) //如果取出了音频输入帧或视频输入帧。
                    {
                        if( m_VideoInputFramePt != null ) //如果取出了视频输入帧。
                            p_TmpInt321 = UserReadAudioVideoInputFrame( m_PcmAudioInputFramePt, m_PcmAudioResultFramePt, m_VoiceActStsPt, m_EncodedAudioInputFramePt, m_EncodedAudioInputFrameLenPt, m_EncodedAudioInputFrameIsNeedTransPt, m_VideoInputFramePt.m_YU12VideoInputFramePt, m_VideoInputFramePt.m_YU12VideoInputFrameWidthPt, m_VideoInputFramePt.m_YU12VideoInputFrameHeightPt, m_VideoInputFramePt.m_EncodedVideoInputFramePt, m_VideoInputFramePt.m_EncodedVideoInputFrameLenPt );
                        else
                            p_TmpInt321 = UserReadAudioVideoInputFrame( m_PcmAudioInputFramePt, m_PcmAudioResultFramePt, m_VoiceActStsPt, m_EncodedAudioInputFramePt, m_EncodedAudioInputFrameLenPt, m_EncodedAudioInputFrameIsNeedTransPt, null, null, null, null, null );
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

                    if( m_PcmAudioInputFramePt != null ) //追加本次音频输入帧到音频输入空闲帧链表。
                    {
                        synchronized( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt )
                        {
                            m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.addLast( m_PcmAudioInputFramePt );
                        }
                        m_PcmAudioInputFramePt = null; //清空PCM格式音频输入帧。
                    }
                    if( m_PcmAudioOutputFramePt != null ) //追加本次音频输出帧到音频输出空闲帧链表。
                    {
                        synchronized( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt )
                        {
                            m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.addLast( m_PcmAudioOutputFramePt );
                        }
                        m_PcmAudioOutputFramePt = null; //清空PCM格式音频输出帧。
                    }
                    if( m_VideoInputFramePt != null ) //追加本次视频输入帧到视频输入空闲帧链表。
                    {
                        synchronized( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.addLast( m_VideoInputFramePt );
                        }
                        m_VideoInputFramePt = null; //清空视频输入帧。
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
            if( m_AudioInputPt.m_AudioInputThrdPt != null ) m_AudioInputPt.m_AudioInputThrdExitFlag = 1; //请求音频输入线程退出。
            if( m_AudioOutputPt.m_AudioOutputThrdPt != null ) m_AudioOutputPt.m_AudioOutputThrdExitFlag = 1; //请求音频输出线程退出。
            if( m_VideoInputPt.m_VideoInputThrdPt != null ) m_VideoInputPt.m_VideoInputThrdExitFlag = 1; //请求视频输入线程退出。
            if( m_VideoOutputPt.m_VideoOutputThrdPt != null ) m_VideoOutputPt.m_VideoOutputThrdExitFlag = 1; //请求视频输出线程退出。

            //销毁音频输入。
            {
                //销毁音频输入线程。
                if( m_AudioInputPt.m_AudioInputThrdPt != null )
                {
                    try
                    {
                        m_AudioInputPt.m_AudioInputThrdPt.join(); //等待音频输入线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_AudioInputPt.m_AudioInputThrdPt = null;
                    m_AudioInputPt.m_AudioInputThrdExitFlag = 0;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入线程成功。" );
                }

                //销毁音频输入空闲帧链表。
                if( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt != null )
                {
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.clear();
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入空闲帧链表成功。" );
                }

                //销毁音频输入帧链表。
                if( m_AudioInputPt.m_AudioInputFrameLnkLstPt != null )
                {
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt.clear();
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入帧链表成功。" );
                }

                //销毁音频输入设备。
                if( m_AudioInputPt.m_AudioInputDvcPt != null )
                {
                    if( m_AudioInputPt.m_AudioInputDvcPt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_AudioInputPt.m_AudioInputDvcPt.stop();
                    m_AudioInputPt.m_AudioInputDvcPt.release();
                    m_AudioInputPt.m_AudioInputDvcPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入设备成功。" );
                }

                //销毁音频输入波形器、音频结果波形器。
                if( m_AudioInputPt.m_IsDrawAudioOscilloToSurface != 0 )
                {
                    if( m_AudioInputPt.m_AudioInputOscilloPt != null )
                    {
                        if( m_AudioInputPt.m_AudioInputOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                        }
                        m_AudioInputPt.m_AudioInputOscilloPt = null;
                    }
                    if( m_AudioInputPt.m_AudioResultOscilloPt != null )
                    {
                        if( m_AudioInputPt.m_AudioResultOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                        }
                        m_AudioInputPt.m_AudioResultOscilloPt = null;
                    }
                }

                //销毁音频输入Wave文件写入器、音频结果Wave文件写入器。
                if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                {
                    if( m_AudioInputPt.m_AudioInputWaveFileWriterPt != null )
                    {
                        if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.Dstoy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器失败。" );
                        }
                        m_AudioInputPt.m_AudioInputWaveFileWriterPt = null;
                    }
                    if( m_AudioInputPt.m_AudioResultWaveFileWriterPt != null )
                    {
                        if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.Dstoy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器失败。" );
                        }
                        m_AudioInputPt.m_AudioResultWaveFileWriterPt = null;
                    }
                }

                //销毁编码器。
                switch( m_AudioInputPt.m_UseWhatEncoder )
                {
                    case 0: //如果要使用PCM原始数据。
                    {
                        break;
                    }
                    case 1: //如果要使用Speex编码器。
                    {
                        if( m_AudioInputPt.m_SpeexEncoderPt != null )
                        {
                            if( m_AudioInputPt.m_SpeexEncoderPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器失败。" );
                            }
                            m_AudioInputPt.m_SpeexEncoderPt = null;
                        }
                        break;
                    }
                    case 2: //如果要使用Opus编码器。
                    {
                        break;
                    }
                }

                //销毁Speex预处理器。
                if( m_AudioInputPt.m_SpeexPrpocsPt != null )
                {
                    if( m_AudioInputPt.m_SpeexPrpocsPt.Dstoy() == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器失败。" );
                    }
                    m_AudioInputPt.m_SpeexPrpocsPt = null;
                }

                //销毁噪音抑制器。
                switch( m_AudioInputPt.m_UseWhatNs )
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
                        if( m_AudioInputPt.m_WebRtcNsxPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcNsxPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器失败。" );
                            }
                            m_AudioInputPt.m_WebRtcNsxPt = null;
                        }
                        break;
                    }
                    case 3: //如果要使用WebRtc浮点版噪音抑制器。
                    {
                        if( m_AudioInputPt.m_WebRtcNsPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcNsPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器失败。" );
                            }
                            m_AudioInputPt.m_WebRtcNsPt = null;
                        }
                        break;
                    }
                    case 4: //如果要使用RNNoise噪音抑制器。
                    {
                        if( m_AudioInputPt.m_RNNoisePt != null )
                        {
                            if( m_AudioInputPt.m_RNNoisePt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器失败。" );
                            }
                            m_AudioInputPt.m_RNNoisePt = null;
                        }
                        break;
                    }
                }

                //销毁声学回音消除器。
                switch( m_AudioInputPt.m_UseWhatAec )
                {
                    case 0: //如果不使用声学回音消除器。
                    {
                        break;
                    }
                    case 1: //如果要使用Speex声学回音消除器。
                    {
                        if( m_AudioInputPt.m_SpeexAecPt != null )
                        {
                            if( m_AudioInputPt.m_SpeexAecIsSaveMemFile != 0 )
                            {
                                if( m_AudioInputPt.m_SpeexAecPt.SaveMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 成功。" );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                }
                            }
                            if( m_AudioInputPt.m_SpeexAecPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器失败。" );
                            }
                            m_AudioInputPt.m_SpeexAecPt = null;
                        }
                        break;
                    }
                    case 2: //如果要使用WebRtc定点版声学回音消除器。
                    {
                        if( m_AudioInputPt.m_WebRtcAecmPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcAecmPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器失败。" );
                            }
                            m_AudioInputPt.m_WebRtcAecmPt = null;
                        }
                        break;
                    }
                    case 3: //如果要使用WebRtc浮点版声学回音消除器。
                    {
                        if( m_AudioInputPt.m_WebRtcAecPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcAecIsSaveMemFile != 0 )
                            {
                                if( m_AudioInputPt.m_WebRtcAecPt.SaveMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt, m_ErrInfoVarStrPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 成功。" );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                                }
                            }
                            if( m_AudioInputPt.m_WebRtcAecPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器失败。" );
                            }
                            m_AudioInputPt.m_WebRtcAecPt = null;
                        }
                        break;
                    }
                    case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                    {
                        if( m_AudioInputPt.m_SpeexWebRtcAecPt != null )
                        {
                            if( m_AudioInputPt.m_SpeexWebRtcAecPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器失败。" );
                            }
                            m_AudioInputPt.m_SpeexWebRtcAecPt = null;
                        }
                        break;
                    }
                }
            } //销毁音频输入完毕。

            //销毁音频输出。
            {
                //销毁音频输出线程。
                if( m_AudioOutputPt.m_AudioOutputThrdPt != null )
                {
                    try
                    {
                        m_AudioOutputPt.m_AudioOutputThrdPt.join(); //等待音频输出线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_AudioOutputPt.m_AudioOutputThrdPt = null;
                    m_AudioOutputPt.m_AudioOutputThrdExitFlag = 0;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出线程成功。" );
                }

                //销毁音频输出空闲帧链表。
                if( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt != null )
                {
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.clear();
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出空闲帧链表成功。" );
                }

                //销毁音频输出帧链表。
                if( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt != null )
                {
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.clear();
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出帧链表成功。" );
                }

                //销毁音频输出设备。
                if( m_AudioOutputPt.m_AudioOutputDvcPt != null )
                {
                    if( m_AudioOutputPt.m_AudioOutputDvcPt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_AudioOutputPt.m_AudioOutputDvcPt.stop();
                    m_AudioOutputPt.m_AudioOutputDvcPt.release();
                    m_AudioOutputPt.m_AudioOutputDvcPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出设备成功。" );
                }

                //销毁音频输出波形器。
                if( m_AudioOutputPt.m_IsDrawAudioOscilloToSurface != 0 )
                {
                    if( m_AudioOutputPt.m_AudioOutputOscilloPt != null )
                    {
                        if( m_AudioOutputPt.m_AudioOutputOscilloPt.Dstoy( m_ErrInfoVarStrPt ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出波形器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出波形器失败。原因：" + m_ErrInfoVarStrPt.GetStr() );
                        }
                        m_AudioOutputPt.m_AudioOutputOscilloPt = null;
                    }
                }

                //销毁音频输出Wave文件写入器。
                if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                {
                    if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt != null )
                    {
                        if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.Dstoy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器失败。" );
                        }
                        m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = null;
                    }
                }

                //销毁解码器。
                switch( m_AudioOutputPt.m_UseWhatDecoder )
                {
                    case 0: //如果要使用PCM原始数据。
                    {
                        break;
                    }
                    case 1: //如果要使用Speex解码器。
                    {
                        if( m_AudioOutputPt.m_SpeexDecoderPt != null )
                        {
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Dstoy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器失败。" );
                            }
                            m_AudioOutputPt.m_SpeexDecoderPt = null;
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
                if( m_VideoInputPt.m_VideoInputThrdPt != null )
                {
                    try
                    {
                        m_VideoInputPt.m_VideoInputThrdPt.join(); //等待视频输入线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_VideoInputPt.m_VideoInputThrdPt = null;
                    m_VideoInputPt.m_VideoInputThrdExitFlag = 0;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程成功。" );
                }

                //销毁视频输入线程的临时变量。
                {
                    m_VideoInputPt.m_VideoInputFramePt = null; //销毁视频输入帧的指针。
                    m_VideoInputPt.m_VideoInputResultFramePt = null; //初始化视频输入结果帧的指针。
                    m_VideoInputPt.m_VideoInputTmpFramePt = null; //初始化视频输入临时帧的指针。
                    m_VideoInputPt.m_VideoInputSwapFramePt = null; //初始化视频输入交换帧的指针。
                    m_VideoInputPt.m_VideoInputResultFrameLenPt = null; //初始化视频输入结果帧的长度。
                    m_VideoInputPt.m_VideoInputResultFrameSz = 0; //销毁视频输入结果帧的内存大小。
                    m_VideoInputPt.m_VideoInputFrameElmPt = null; //销毁视频输入帧元素的指针。
                    m_VideoInputPt.m_VideoInputFrameLnkLstElmTotal = 0; //销毁视频输入帧链表的元数总数。
                    m_VideoInputPt.m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
                    m_VideoInputPt.m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程的临时变量成功。" );
                }

                //销毁视频输入设备。
                if( m_VideoInputPt.m_VideoInputDvcPt != null )
                {
                    m_VideoInputPt.m_VideoInputDvcPt.setPreviewCallback( null ); //设置预览回调函数为空，防止出现java.lang.RuntimeException: Method called after release()异常。
                    m_VideoInputPt.m_VideoInputDvcPt.stopPreview(); //停止预览。
                    m_VideoInputPt.m_VideoInputDvcPt.release(); //销毁摄像头。
                    m_VideoInputPt.m_VideoInputDvcPt = null;
                    m_VideoInputPt.m_VideoInputPreviewCallbackBufferPtPt = null;
                    m_VideoInputPt.m_VideoInputDvcFrameRotate = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameWidth = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameHeight = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameIsCrop = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameCropX = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameCropY = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameCropWidth = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameCropHeight = 0;
                    m_VideoInputPt.m_VideoInputDvcFrameIsScale = 0;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入设备成功。" );
                }

                //销毁视频输入空闲帧链表。
                if( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.clear();
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入空闲帧链表成功。" );
                }

                //销毁视频输入帧链表。
                if( m_VideoInputPt.m_VideoInputFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt.clear();
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入帧链表成功。" );
                }

                //销毁NV21格式视频输入帧链表。
                if( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.clear();
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁NV21格式视频输入帧链表成功。" );
                }

                //销毁编码器。
                switch( m_VideoInputPt.m_UseWhatEncoder )
                {
                    case 0: //如果要使用YU12原始数据。
                    {
                        break;
                    }
                    case 1: //如果要使用OpenH264编码器。
                    {
                        if( m_VideoInputPt.m_OpenH264EncoderPt != null )
                        {
                            if( m_VideoInputPt.m_OpenH264EncoderPt.Dstoy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器失败。" );
                            }
                            m_VideoInputPt.m_OpenH264EncoderPt = null;
                        }
                        break;
                    }
                    case 2: //如果要使用系统自带H264编码器。
                    {
                        if( m_VideoInputPt.m_SystemH264EncoderPt != null )
                        {
                            if( m_VideoInputPt.m_SystemH264EncoderPt.Dstoy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器失败。" );
                            }
                            m_VideoInputPt.m_SystemH264EncoderPt = null;
                        }
                        break;
                    }
                }
            } //销毁视频输入完毕。

            //销毁视频输出。
            {
                //销毁视频输出线程。
                if( m_VideoOutputPt.m_VideoOutputThrdPt != null )
                {
                    try
                    {
                        m_VideoOutputPt.m_VideoOutputThrdPt.join(); //等待视频输出线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_VideoOutputPt.m_VideoOutputThrdPt = null;
                    m_VideoOutputPt.m_VideoOutputThrdExitFlag = 0;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出线程成功。" );
                }

                //销毁视频输出线程的临时变量。
                {
                    m_VideoOutputPt.m_VideoOutputResultFramePt = null; //销毁视频输出结果帧的指针。
                    m_VideoOutputPt.m_VideoOutputTmpFramePt = null; //销毁视频输出临时帧的指针。
                    m_VideoOutputPt.m_VideoOutputSwapFramePt = null; //销毁视频输出交换帧的指针。
                    m_VideoOutputPt.m_VideoOutputResultFrameLenPt = null; //销毁视频输出结果帧的长度。
                    m_VideoOutputPt.m_VideoOutputFrameWidthPt = null; //销毁视频输出帧的宽度。
                    m_VideoOutputPt.m_VideoOutputFrameHeightPt = null; //销毁视频输出帧的高度。
                    m_VideoOutputPt.m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
                    m_VideoOutputPt.m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出线程的临时变量成功。" );
                }

                //销毁解码器。
                switch( m_VideoOutputPt.m_UseWhatDecoder )
                {
                    case 0: //如果要使用YU12原始数据。
                    {
                        break;
                    }
                    case 1: //如果要使用OpenH264解码器。
                    {
                        if( m_VideoOutputPt.m_OpenH264DecoderPt != null )
                        {
                            if( m_VideoOutputPt.m_OpenH264DecoderPt.Dstoy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器失败。" );
                            }
                            m_VideoOutputPt.m_OpenH264DecoderPt = null;
                        }
                        break;
                    }
                    case 2: //如果要使用系统自带H264解码器。
                    {
                        if( m_VideoOutputPt.m_SystemH264DecoderPt != null )
                        {
                            if( m_VideoOutputPt.m_SystemH264DecoderPt.Dstoy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264解码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264解码器失败。" );
                            }
                            m_VideoOutputPt.m_SystemH264DecoderPt = null;
                        }
                        break;
                    }
                }
            } //销毁视频输出完毕。

            //销毁媒体处理线程的临时变量。
            {
                m_PcmAudioInputFramePt = null;
                m_PcmAudioOutputFramePt = null;
                m_PcmAudioResultFramePt = null;
                m_PcmAudioTmpFramePt = null;
                m_PcmAudioSwapFramePt = null;
                m_VoiceActStsPt = null;
                m_EncodedAudioInputFramePt = null;
                m_EncodedAudioInputFrameLenPt = null;
                m_EncodedAudioInputFrameIsNeedTransPt = null;
                m_VideoInputFramePt = null;
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁媒体处理线程的临时变量成功。" );
            } //销毁媒体处理线程的临时变量完毕。

            //销毁唤醒锁。
            WakeLockInitOrDstoy( 0 );

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

            if( m_ExitFlag != 3 ) //如果需要调用用户定义的销毁函数。
            {
                UserDstoy(); //调用用户定义的销毁函数。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
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