package HeavenTao.Media;

import android.app.Activity;
import android.content.Context;
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
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import HeavenTao.Audio.*;
import HeavenTao.Video.*;
import HeavenTao.Data.*;

//媒体处理线程类。
public abstract class MediaProcThread extends Thread
{
    public String m_CurClsNameStrPt = this.getClass().getSimpleName(); //存放当前类名称字符串。

    public int m_RunFlag = RUN_FLAG_NORUN; //存放本线程运行标记。
    public static final int RUN_FLAG_NORUN = 0; //运行标记：未开始运行。
    public static final int RUN_FLAG_INIT = 1; //运行标记：刚开始运行正在初始化。
    public static final int RUN_FLAG_PROC = 2; //运行标记：初始化完毕正在循环处理帧。
    public static final int RUN_FLAG_DESTROY = 3; //运行标记：跳出循环处理帧正在销毁。
    public static final int RUN_FLAG_END = 4; //运行标记：销毁完毕。
    public int m_ExitFlag = 0; //存放本线程退出标记，为0表示保持运行，为1表示请求退出，为2表示请求重启，为3表示请求重启但不执行用户定义的UserInit初始化函数和UserDestroy销毁函数。
    public int m_ExitCode = 0; //存放本线程退出代码，为0表示正常退出，为-1表示初始化失败，为-2表示处理失败。

    public static Context m_AppContextPt; //存放应用程序上下文类对象的内存指针。

    int m_IsSaveSettingToFile = 0; //存放是否保存设置到文件，为非0表示要保存，为0表示不保存。
    String m_SettingFileFullPathStrPt; //存放设置文件的完整路径字符串。

    public int m_IsPrintLogcat = 0; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。

    int m_IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。
    PowerManager.WakeLock m_ProximityScreenOffWakeLockPt; //存放接近息屏唤醒锁类对象的内存指针。
    PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁类对象的内存指针。

    public class AudioInput //音频输入类。
    {
        public int m_IsUseAudioInput; //存放是否使用音频输入，为0表示不使用，为非0表示要使用。

        public int m_SamplingRate = 16000; //存放采样频率，取值只能为8000、16000、32000。
        public int m_FrameLen = 320; //存放帧的数据长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960。

        public int m_IsUseSystemAecNsAgc = 0; //存放是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带），为0表示不使用，为非0表示要使用。

        public int m_UseWhatAec = 0; //存放使用什么声学回音消除器，为0表示不使用，为1表示Speex声学回音消除器，为2表示WebRtc定点版声学回音消除器，为2表示WebRtc浮点版声学回音消除器，为4表示SpeexWebRtc三重声学回音消除器。

        SpeexAec m_SpeexAecPt; //存放Speex声学回音消除器类对象的内存指针。
        int m_SpeexAecFilterLen; //存放Speex声学回音消除器的滤波器数据长度，单位毫秒。
        int m_SpeexAecIsUseRec; //存放Speex声学回音消除器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
        float m_SpeexAecEchoMultiple; //存放Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
        float m_SpeexAecEchoCont; //存放Speex声学回音消除器在残余回音消除时，残余回音的持续系数，系数越大消除越强，取值区间为[0.0,0.9]。
        int m_SpeexAecEchoSupes; //存放Speex声学回音消除器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_SpeexAecEchoSupesAct; //存放Speex声学回音消除器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_SpeexAecIsSaveMemFile; //存放Speex声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
        String m_SpeexAecMemFileFullPathStrPt; //存放Speex声学回音消除器的内存块文件完整路径字符串。

        WebRtcAecm m_WebRtcAecmPt; //存放WebRtc定点版声学回音消除器类对象的内存指针。
        int m_WebRtcAecmIsUseCNGMode; //存放WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecmEchoMode; //存放WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
        int m_WebRtcAecmDelay; //存放WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。

        WebRtcAec m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器类对象的内存指针。
        int m_WebRtcAecEchoMode; //存放WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
        int m_WebRtcAecDelay; //存放WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
        int m_WebRtcAecIsUseDelayAgnosticMode; //存放WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseExtdFilterMode; //存放WebRtc浮点版声学回音消除器是否使用扩展滤波器模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseRefinedFilterAdaptAecMode; //存放WebRtc浮点版声学回音消除器是否使用精制滤波器自适应Aec模式，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsUseAdaptAdjDelay; //存放WebRtc浮点版声学回音消除器是否使用自适应调节回音的延迟，为非0表示要使用，为0表示不使用。
        int m_WebRtcAecIsSaveMemFile; //存放WebRtc浮点版声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
        String m_WebRtcAecMemFileFullPathStrPt; //存放WebRtc浮点版声学回音消除器的内存块文件完整路径字符串。

        SpeexWebRtcAec m_SpeexWebRtcAecPt; //存放SpeexWebRtc三重声学回音消除器类对象的内存指针。
        int m_SpeexWebRtcAecWorkMode; //存放SpeexWebRtc三重声学回音消除器的工作模式，为1表示Speex声学回音消除器+WebRtc定点版声学回音消除器，为2表示WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器，为3表示Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
        int m_SpeexWebRtcAecSpeexAecFilterLen; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器的滤波器数据长度，单位毫秒。
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

        public int m_UseWhatNs = 0; //存放使用什么噪音抑制器，为0表示不使用，为1表示Speex预处理器的噪音抑制，为2表示WebRtc定点版噪音抑制器，为3表示WebRtc浮点版噪音抑制器，为4表示RNNoise噪音抑制器。

        SpeexPproc m_SpeexPprocPt; //存放Speex预处理器类对象的内存指针。
        int m_SpeexPprocIsUseNs; //存放Speex预处理器是否使用噪音抑制，为非0表示要使用，为0表示不使用。
        int m_SpeexPprocNoiseSupes; //存放Speex预处理器在噪音抑制时，噪音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
        int m_SpeexPprocIsUseDereverb; //存放Speex预处理器是否使用混响音消除，为非0表示要使用，为0表示不使用。

        WebRtcNsx m_WebRtcNsxPt; //存放WebRtc定点版噪音抑制器类对象的内存指针。
        int m_WebRtcNsxPolicyMode; //存放WebRtc定点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

        WebRtcNs m_WebRtcNsPt; //存放WebRtc浮点版噪音抑制器类对象的内存指针。
        int m_WebRtcNsPolicyMode; //存放WebRtc浮点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

        RNNoise m_RNNoisePt; //存放RNNoise噪音抑制器类对象的内存指针。

        int m_IsUseSpeexPprocOther; //存放Speex预处理器是否使用其他功能，为非0表示要使用，为0表示不使用。
        int m_SpeexPprocIsUseVad; //存放Speex预处理器是否使用语音活动检测，为非0表示要使用，为0表示不使用。
        int m_SpeexPprocVadProbStart; //存放Speex预处理器在语音活动检测时，从无语音活动到有语音活动的判断百分比概率，概率越大越难判断为有语音活，取值区间为[0,100]。
        int m_SpeexPprocVadProbCont; //存放Speex预处理器在语音活动检测时，从有语音活动到无语音活动的判断百分比概率，概率越大越容易判断为无语音活动，取值区间为[0,100]。
        int m_SpeexPprocIsUseAgc; //存放Speex预处理器是否使用自动增益控制，为非0表示要使用，为0表示不使用。
        int m_SpeexPprocAgcLevel; //存放Speex预处理器在自动增益控制时，增益的目标等级，目标等级越大增益越大，取值区间为[1,2147483647]。
        int m_SpeexPprocAgcIncrement; //存放Speex预处理器在自动增益控制时，每秒最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。
        int m_SpeexPprocAgcDecrement; //存放Speex预处理器在自动增益控制时，每秒最大减益的分贝值，分贝值越小减益越大，取值区间为[-2147483648,0]。
        int m_SpeexPprocAgcMaxGain; //存放Speex预处理器在自动增益控制时，最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。

        public int m_UseWhatEncoder = 0; //存放使用什么编码器，为0表示PCM原始数据，为1表示Speex编码器，为2表示Opus编码器。

        SpeexEncoder m_SpeexEncoderPt; //存放Speex编码器类对象的内存指针。
        int m_SpeexEncoderUseCbrOrVbr; //存放Speex编码器使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
        int m_SpeexEncoderQuality; //存放Speex编码器的编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
        int m_SpeexEncoderComplexity; //存放Speex编码器的编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
        int m_SpeexEncoderPlcExpectedLossRate; //存放Speex编码器在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。

        public int m_IsSaveAudioToFile = 0; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
        WaveFileWriter m_AudioInputWaveFileWriterPt; //存放音频输入Wave文件写入器对象的内存指针。
        WaveFileWriter m_AudioResultWaveFileWriterPt; //存放音频结果Wave文件写入器对象的内存指针。
        String m_AudioInputFileFullPathStrPt; //存放音频输入文件的完整路径字符串。
        String m_AudioResultFileFullPathStrPt; //存放音频结果文件的完整路径字符串。

        AudioRecord m_AudioInputDevicePt; //存放音频输入设备类对象的内存指针。
        int m_AudioInputDeviceBufSz; //存放音频输入设备缓冲区大小，单位字节。
        int m_AudioInputDeviceIsMute = 0; //存放音频输入设备是否静音，为0表示有声音，为非0表示静音。

        public LinkedList< short[] > m_AudioInputFrameLnkLstPt; //存放音频输入帧链表类对象的内存指针。
        public LinkedList< short[] > m_AudioInputIdleFrameLnkLstPt; //存放音频输入空闲帧链表类对象的内存指针。

        AudioInputThread m_AudioInputThreadPt; //存放音频输入线程类对象的内存指针。
    }

    public AudioInput m_AudioInputPt = new AudioInput(); //存放音频输入类对象的内存指针。

    public class AudioOutput //音频输出类。
    {
        public int m_IsUseAudioOutput; //存放是否使用音频输出，为0表示不使用，为非0表示要使用。

        public int m_SamplingRate = 16000; //存放采样频率，取值只能为8000、16000、32000。
        public int m_FrameLen = 320; //存放帧的数据长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960。

        public int m_UseWhatDecoder = 0; //存放使用什么解码器，为0表示PCM原始数据，为1表示Speex解码器，为2表示Opus解码器。

        SpeexDecoder m_SpeexDecoderPt; //存放Speex解码器类对象的内存指针。
        int m_SpeexDecoderIsUsePerceptualEnhancement; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

        public int m_IsSaveAudioToFile = 0; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
        WaveFileWriter m_AudioOutputWaveFileWriterPt; //存放音频输出Wave文件写入器对象的内存指针。
        String m_AudioOutputFileFullPathStrPt; //存放音频输出文件的完整路径字符串。

        public AudioTrack m_AudioOutputDevicePt; //存放音频输出设备类对象的内存指针。
        int m_AudioOutputDeviceBufSz; //存放音频输出设备缓冲区大小，单位字节。
        public int m_UseWhatAudioOutputDevice = 0; //存放使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
        public int m_UseWhatAudioOutputStreamType = 0; //存放使用什么音频输出流类型，为0表示通话类型，为非0表示媒体类型。
        int m_AudioOutputDeviceIsMute = 0; //存放音频输出设备是否静音，为0表示有声音，为非0表示静音。

        public LinkedList< short[] > m_AudioOutputFrameLnkLstPt; //存放音频输出帧链表类对象的内存指针。
        public LinkedList< short[] > m_AudioOutputIdleFrameLnkLstPt; //存放音频输出空闲帧链表类对象的内存指针。

        AudioOutputThread m_AudioOutputThreadPt; //存放音频输出线程类对象的内存指针。
    }

    public AudioOutput m_AudioOutputPt = new AudioOutput(); //存放音频输出类对象的内存指针。

    public class VideoInput //视频输入类。
    {
        public int m_IsUseVideoInput; //存放是否使用视频输入，为0表示不使用，为非0表示要使用。

        public int m_MaxSamplingRate = 24; //存放最大采样频率，取值范围为[1,60]，实际帧率和图像的亮度有关，亮度较高时采样频率可以达到最大值，亮度较低时系统就自动降低采样频率来提升亮度。
        public int m_FrameWidth = 640; //存放帧的宽度，单位为像素。
        public int m_FrameHeight = 480; //存放帧的高度，单位为像素。

        public int m_UseWhatEncoder = 0; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264编码器。

        OpenH264Encoder m_OpenH264EncoderPt; //存放OpenH264编码器类对象的内存指针。
        int m_OpenH264EncoderVideoType;//存放OpenH264编码器的视频类型，为0表示实时摄像头视频，为1表示实时屏幕内容视频，为2表示非实时摄像头视频，为3表示非实时屏幕内容视频，为4表示其他视频。
        int m_OpenH264EncoderEncodedBitrate; //存放OpenH264编码器的编码后比特率，单位为bps。
        int m_OpenH264EncoderBitrateControlMode; //存放OpenH264编码器的比特率控制模式，为0表示质量优先模式，为1表示比特率优先模式，为2表示缓冲区优先模式，为3表示时间戳优先模式。
        int m_OpenH264EncoderIDRFrameIntvl; //存放OpenH264编码器的IDR帧间隔帧数，单位为个，为0表示仅第一帧为IDR帧，为大于0表示每隔这么帧就至少有一个IDR帧。
        int m_OpenH264EncoderComplexity; //存放OpenH264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,2]。

        public Camera m_VideoInputDevicePt; //存放视频输入设备类对象的内存指针。
        public int m_UseWhatVideoInputDevice = 0; //存放使用什么视频输入设备，为0表示后置摄像头，为1表示前置摄像头。
        public HTSurfaceView m_VideoPreviewSurfaceViewPt; //存放视频预览SurfaceView类对象的内存指针。
        public byte m_VideoPreviewCallbackBufferPtPt[][]; //存放视频预览回调函数缓冲区的内存指针。
        public int m_VideoInputFrameRotate; //存放视频输入帧旋转的角度，只能为0、90、180、270。
        int m_VideoInputDeviceIsBlack = 0; //存放视频输入设备是否黑屏，为0表示有图像，为非0表示黑屏。

        public LinkedList< byte[] > m_NV21VideoInputFrameLnkLstPt; //存放NV21格式视频输入帧链表类对象的内存指针。
        public LinkedList< VideoInputFrameElm > m_VideoInputFrameLnkLstPt; //存放视频输入帧链表类对象的内存指针。
        public LinkedList< VideoInputFrameElm > m_VideoInputIdleFrameLnkLstPt; //存放视频输入空闲帧链表类对象的内存指针。

        VideoInputThread m_VideoInputThreadPt; //存放视频输入线程类对象的内存指针。
    }
    public class VideoInputFrameElm //视频输入帧链表元素类。
    {
        VideoInputFrameElm()
        {
            m_RotateYU12VideoInputFramePt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new byte[ m_VideoInputPt.m_FrameWidth * m_VideoInputPt.m_FrameHeight * 3 / 2 ] : null;
            m_RotateYU12VideoInputFrameWidthPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new HTInt() : null;
            m_RotateYU12VideoInputFrameHeightPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 ) ? new HTInt() : null;
            m_EncoderVideoInputFramePt = ( m_VideoInputPt.m_IsUseVideoInput != 0 && m_VideoInputPt.m_UseWhatEncoder != 0 ) ? new byte[ m_VideoInputPt.m_FrameWidth * m_VideoInputPt.m_FrameHeight * 3 / 2 ] : null;
            m_EncoderVideoInputFrameLenPt = ( m_VideoInputPt.m_IsUseVideoInput != 0 && m_VideoInputPt.m_UseWhatEncoder != 0 ) ? new HTLong( 0 ) : null;
        }
        byte m_RotateYU12VideoInputFramePt[]; //存放旋转后YU12格式视频输入帧的内存指针。
        HTInt m_RotateYU12VideoInputFrameWidthPt; //存放旋转后YU12格式视频输入帧的宽度。
        HTInt m_RotateYU12VideoInputFrameHeightPt; //存放旋转后YU12格式视频输入帧的高度。
        byte m_EncoderVideoInputFramePt[]; //存放已编码格式视频输入帧。
        HTLong m_EncoderVideoInputFrameLenPt; //存放已编码格式视频输入帧的数据长度，单位字节。
    }

    public VideoInput m_VideoInputPt = new VideoInput(); //存放视频输入类对象的内存指针。

    public class VideoOutput //视频输出类。
    {
        public int m_IsUseVideoOutput; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。

        public int m_FrameWidth = 640; //存放帧的宽度，单位为像素。
        public int m_FrameHeight = 480; //存放帧的高度，单位为像素。

        public int m_UseWhatDecoder = 0; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264解码器。

        OpenH264Decoder m_OpenH264DecoderPt; //存放OpenH264解码器类对象的内存指针。
        int m_OpenH264DecoderDecodeThreadNum; //存放OpenH264解码器的解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。

        HTSurfaceView m_VideoDisplaySurfaceViewPt; //存放视频显示SurfaceView类对象的内存指针。
        float m_VideoDisplayScale = 1.0f; //存放视频显示缩放倍数。
        int m_VideoOutputDeviceIsBlack = 0; //存放视频输出设备是否黑屏，为0表示有图像，为非0表示黑屏。

        public LinkedList< VideoOutputFrameElm > m_VideoOutputFrameLnkLstPt; //存放视频输出帧链表类对象的内存指针。
        public LinkedList< VideoOutputFrameElm > m_VideoOutputIdleFrameLnkLstPt; //存放视频输出空闲帧链表类对象的内存指针。

        VideoOutputThread m_VideoOutputThreadPt; //存放视频输出线程类对象的内存指针。
    }
    public class VideoOutputFrameElm //视频输出帧链表元素类。
    {
        VideoOutputFrameElm()
        {
            m_VideoOutputFramePt = ( m_VideoOutputPt.m_IsUseVideoOutput != 0 ) ? new byte[ m_VideoOutputPt.m_FrameWidth * m_VideoOutputPt.m_FrameHeight * 3 / 2 ] : null;
            m_VideoOutputFrameLen = 0;
        }
        byte m_VideoOutputFramePt[];
        long m_VideoOutputFrameLen;
    }

    public VideoOutput m_VideoOutputPt = new VideoOutput(); //存放视频输出类对象的内存指针。

    //用户定义的相关函数。
    public abstract int UserInit(); //用户定义的初始化函数，在本线程刚启动时回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract int UserProcess(); //用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract void UserDestroy(); //用户定义的销毁函数，在本线程退出时回调一次。

    public abstract int UserReadAudioVideoInputFrame( short PcmAudioInputFramePt[], short PcmAudioResultFramePt[], HTInt VoiceActStsPt, byte EncoderAudioInputFramePt[], HTLong EncoderAudioInputFrameLenPt, HTInt EncoderAudioInputFrameIsNeedTransPt, byte YU12VideoInputFramePt[], HTInt YU12VideoInputFrameWidthPt, HTInt YU12VideoInputFrameHeigthPt, byte EncoderVideoInputFramePt[], HTLong EncoderVideoInputFrameLenPt ); //用户定义的读取音视频输入帧函数，在读取到一个音频输入帧或视频输入帧并处理完后回调一次，为0表示成功，为非0表示失败。

    public abstract void UserWriteAudioOutputFrame( short PcmAudioOutputFramePt[], byte EncoderAudioOutputFramePt[], HTLong EncoderAudioOutputFrameLen ); //用户定义的写入音频输出帧函数，在需要写入一个音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。

    public abstract void UserGetPcmAudioOutputFrame( short PcmAudioOutputFramePt[] ); //用户定义的获取PCM格式音频输出帧函数，在解码完一个已编码音频输出帧时回调一次。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。

    public void UserWriteVideoOutputFrame( byte VideoOutputFramePt[], int VideoOutputFrameStart, long VideoOutputFrameLen ) //用户调用的写入视频输出帧函数，在用户需要显示一个视频输出帧时主调一次。
    {
        VideoOutputFrameElm p_VideoOutputFrameElmPt = null;
        int p_TmpInt32;

        out:
        {
            //写入一个视频输出帧到视频输出帧链表。
            if( m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt != null && VideoOutputFramePt != null && VideoOutputFrameLen > 0 && m_RunFlag == RUN_FLAG_PROC ) //如果要使用视频输出帧链表，且视频输出帧有图像活动，且媒体处理线程初始化完毕正在循环处理帧。
            {
                //获取一个视频输出空闲帧。
                if( ( p_TmpInt32 = m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt.size() ) > 0 ) //如果视频输出空闲帧链表中有视频输出空闲帧。
                {
                    //从视频输出空闲帧链表中取出第一个视频输出空闲帧。
                    synchronized( m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt )
                    {
                        p_VideoOutputFrameElmPt = m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt.getFirst();
                        m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从视频输出空闲帧链表中取出第一个视频输出空闲帧，视频输出空闲帧链表元素个数：" + p_TmpInt32 + "。" );
                }
                else //如果视频输出空闲帧链表中没有视频输出空闲帧。
                {
                    if( ( p_TmpInt32 = m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.size() ) <= 20 )
                    {
                        p_VideoOutputFrameElmPt = new VideoOutputFrameElm(); //创建一个视频输出空闲帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：视频输出空闲帧链表中没有视频输出空闲帧，创建一个视频输出空闲帧。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：视频输出帧链表中视频输出帧数量为" + p_TmpInt32 + "已经超过上限20，不再创建一个视频输出空闲帧。" );
                        break out;
                    }
                }

                //复制视频输出帧。
                System.arraycopy( VideoOutputFramePt, VideoOutputFrameStart, p_VideoOutputFrameElmPt.m_VideoOutputFramePt, 0, ( int ) VideoOutputFrameLen );
                p_VideoOutputFrameElmPt.m_VideoOutputFrameLen = VideoOutputFrameLen;

                //追加本次视频输出帧到视频输出帧链表。
                synchronized( m_VideoOutputPt.m_VideoOutputFrameLnkLstPt )
                {
                    m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.addLast( p_VideoOutputFrameElmPt );
                }
            }
        }
    }

    //构造函数。
    public MediaProcThread( Context AppContextPt )
    {
        m_AppContextPt = AppContextPt; //设置应用程序上下文类对象的内存指针。
    }

    //设置是否保存设置到文件。
    public void SetSaveSettingToFile( int IsSaveSettingToFile, String SettingFileFullPathStrPt )
    {
        m_IsSaveSettingToFile = IsSaveSettingToFile;
        m_SettingFileFullPathStrPt = SettingFileFullPathStrPt;
    }

    //设置是否打印Logcat日志。
    public void SetPrintLogcat( int IsPrintLogcat )
    {
        m_IsPrintLogcat = IsPrintLogcat;
    }

    //设置是否使用唤醒锁。
    public void SetUseWakeLock( int IsUseWakeLock )
    {
        m_IsUseWakeLock = IsUseWakeLock;

        if( m_RunFlag == RUN_FLAG_INIT || m_RunFlag == RUN_FLAG_PROC ) //如果本线程为刚开始运行正在初始化或初始化完毕正在循环处理帧，就立即修改唤醒锁。
        {
            WakeLockInitOrDestroy( IsUseWakeLock );
        }
    }

    //初始化或销毁唤醒锁。
    void WakeLockInitOrDestroy( int IsInitOrDestroy )
    {
        if( IsInitOrDestroy != 0 ) //如果要初始化唤醒锁。
        {
            if( m_AudioOutputPt.m_IsUseAudioOutput != 0 && m_AudioOutputPt.m_UseWhatAudioOutputDevice != 0 ) //如果要使用音频输出，且要使用听筒音频输出设备，就要使用接近息屏唤醒锁。
            {
                if( m_ProximityScreenOffWakeLockPt == null ) //如果接近息屏唤醒锁类对象还没有初始化。
                {
                    m_ProximityScreenOffWakeLockPt = ( ( PowerManager ) m_AppContextPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
                    if( m_ProximityScreenOffWakeLockPt != null )
                    {
                        m_ProximityScreenOffWakeLockPt.acquire();
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化接近息屏唤醒锁类对象成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化接近息屏唤醒锁类对象失败。" );
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
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁接近息屏唤醒锁类对象成功。" );
                }
            }

            if( m_FullWakeLockPt == null ) //如果屏幕键盘全亮唤醒锁类对象还没有初始化。
            {
                m_FullWakeLockPt = ( ( PowerManager ) m_AppContextPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
                if( m_FullWakeLockPt != null )
                {
                    m_FullWakeLockPt.acquire();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化屏幕键盘全亮唤醒锁类对象成功。" );
                }
                else
                {
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化屏幕键盘全亮唤醒锁类对象失败。" );
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
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁接近息屏唤醒锁类对象成功。" );
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
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁屏幕键盘全亮唤醒锁类对象成功。" );
            }
        }
    }

    //设置是否使用音频输入。
    public void SetUseAudioInput( int IsUseAudioInput, int SamplingRate, int FrameLenMsec )
    {
        if( ( ( IsUseAudioInput != 0 ) && ( ( SamplingRate != 8000 ) && ( SamplingRate != 16000 ) && ( SamplingRate != 32000 ) ) ) || //如果采样频率不正确。
            ( ( IsUseAudioInput != 0 ) && ( ( FrameLenMsec == 0 ) || ( FrameLenMsec % 10 != 0 ) ) ) ) //如果帧的毫秒长度不正确。
        {
            return;
        }

        m_AudioInputPt.m_IsUseAudioInput = IsUseAudioInput;
        m_AudioInputPt.m_SamplingRate = SamplingRate;
        m_AudioInputPt.m_FrameLen = FrameLenMsec * SamplingRate / 1000;
    }

    //设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
    public void SetAudioInputUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
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
    public void SetAudioInputUseSpeexPprocNs( int IsUseNs, int NoiseSupes, int IsUseDereverberation )
    {
        m_AudioInputPt.m_UseWhatNs = 1;
        m_AudioInputPt.m_SpeexPprocIsUseNs = IsUseNs;
        m_AudioInputPt.m_SpeexPprocNoiseSupes = NoiseSupes;
        m_AudioInputPt.m_SpeexPprocIsUseDereverb = IsUseDereverberation;
    }

    //设置音频输入要使用WebRtc定点版噪音抑制器。
    public void SetAudioInputUseWebRtcNsx( int PolicyMode )
    {
        m_AudioInputPt.m_UseWhatNs = 2;
        m_AudioInputPt.m_WebRtcNsxPolicyMode = PolicyMode;
    }

    //设置音频输入要使用WebRtc定点版噪音抑制器。
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

    //设置音频输入要使用Speex预处理器的其他功能。
    public void SetAudioInputUseSpeexPprocOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
    {
        m_AudioInputPt.m_IsUseSpeexPprocOther = IsUseOther;
        m_AudioInputPt.m_SpeexPprocIsUseVad = IsUseVad;
        m_AudioInputPt.m_SpeexPprocVadProbStart = VadProbStart;
        m_AudioInputPt.m_SpeexPprocVadProbCont = VadProbCont;
        m_AudioInputPt.m_SpeexPprocIsUseAgc = IsUseAgc;
        m_AudioInputPt.m_SpeexPprocAgcIncrement = AgcIncrement;
        m_AudioInputPt.m_SpeexPprocAgcDecrement = AgcDecrement;
        m_AudioInputPt.m_SpeexPprocAgcLevel = AgcLevel;
        m_AudioInputPt.m_SpeexPprocAgcMaxGain = AgcMaxGain;
    }

    //设置音频输入要使用PCM原始数据。
    public void SetAudioInputUsePcm()
    {
        m_AudioInputPt.m_UseWhatEncoder = 0;
    }

    //设置音频输入要使用Speex编码器。
    public void SetAudioInputUseSpeexEncoder( int UseCbrOrVbr, int Quality, int Complexity, int PlcExpectedLossRate )
    {
        m_AudioInputPt.m_UseWhatEncoder = 1;
        m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr = UseCbrOrVbr;
        m_AudioInputPt.m_SpeexEncoderQuality = Quality;
        m_AudioInputPt.m_SpeexEncoderComplexity = Complexity;
        m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate = PlcExpectedLossRate;
    }

    //设置音频输入要使用Opus编码器。
    public void SetAudioInputUseOpusEncoder()
    {
        m_AudioInputPt.m_UseWhatEncoder = 2;
    }

    //设置音频输入是否保存音频到文件。
    public void SetAudioInputSaveAudioToFile( int IsSaveAudioToFile, String AudioInputFileFullPathStrPt, String AudioResultFileFullPathStrPt )
    {
        m_AudioInputPt.m_IsSaveAudioToFile = IsSaveAudioToFile;
        m_AudioInputPt.m_AudioInputFileFullPathStrPt = AudioInputFileFullPathStrPt;
        m_AudioInputPt.m_AudioResultFileFullPathStrPt = AudioResultFileFullPathStrPt;
    }

    //设置音频输入设备是否静音。
    public void SetAudioInputDeviceIsMute( int IsMute )
    {
        m_AudioInputPt.m_AudioInputDeviceIsMute = IsMute;
    }

    //设置是否使用音频输出。
    public void SetUseAudioOutput( int IsUseAudioOutput, int SamplingRate, int FrameLenMsec )
    {
        if( ( ( IsUseAudioOutput != 0 ) && ( ( SamplingRate != 8000 ) && ( SamplingRate != 16000 ) && ( SamplingRate != 32000 ) ) ) || //如果采样频率不正确。
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
    public void SetAudioOutputSaveAudioToFile( int IsSaveAudioToFile, String AudioOutputFileFullPathStrPt )
    {
        m_AudioOutputPt.m_IsSaveAudioToFile = IsSaveAudioToFile;
        m_AudioOutputPt.m_AudioOutputFileFullPathStrPt = AudioOutputFileFullPathStrPt;
    }

    //设置音频输出使用的设备。
    public void SetAudioOutputUseDevice( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
    {
        if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) )//如果使用听筒，则不能使用媒体类型音频输出流。
        {
            return;
        }

        m_AudioOutputPt.m_UseWhatAudioOutputDevice = UseSpeakerOrEarpiece;
        m_AudioOutputPt.m_UseWhatAudioOutputStreamType = UseVoiceCallOrMusic;
        SetUseWakeLock( m_IsUseWakeLock ); //重新初始化唤醒锁。
    }

    //设置音频输出设备是否静音。
    public void SetAudioOutputDeviceIsMute( int IsMute )
    {
        m_AudioOutputPt.m_AudioOutputDeviceIsMute = IsMute; //设置音频输出设备是否静音。
    }

    //设置是否使用视频输入。
    public void SetUseVideoInput( int IsUseVideoInput, int MaxSamplingRate, int FrameWidth, int FrameHeight, HTSurfaceView VideoPreviewSurfaceViewPt )
    {
        if( ( ( IsUseVideoInput != 0 ) && ( MaxSamplingRate <= 0 ) ) || //如果采样频率不正确。
            ( ( IsUseVideoInput != 0 ) && ( FrameWidth <= 0 ) ) || //如果帧的宽度不正确。
            ( ( IsUseVideoInput != 0 ) && ( FrameHeight <= 0 ) ) || //如果帧的高度不正确。
            ( ( IsUseVideoInput != 0 ) && ( VideoPreviewSurfaceViewPt == null ) ) ) //如果视频预览SurfaceView类对象的内存指针不正确。
        {
            return;
        }

        m_VideoInputPt.m_IsUseVideoInput = IsUseVideoInput;
        m_VideoInputPt.m_MaxSamplingRate = MaxSamplingRate;
        m_VideoInputPt.m_FrameWidth = FrameWidth;
        m_VideoInputPt.m_FrameHeight = FrameHeight;
        m_VideoInputPt.m_VideoPreviewSurfaceViewPt = VideoPreviewSurfaceViewPt;
    }

    //设置视频输入要使用YU12原始数据。
    public void SetVideoInputUseYU12()
    {
        m_VideoInputPt.m_UseWhatEncoder = 0;
    }

    //设置视频输入要使用OpenH264编码器。
    public void SetVideoInputUseOpenH264( int VideoType, int EncodedBitrate, int BitrateControlMode, int IDRFrameIntvl, int Complexity )
    {
        m_VideoInputPt.m_UseWhatEncoder = 1;
        m_VideoInputPt.m_OpenH264EncoderVideoType = VideoType;
        m_VideoInputPt.m_OpenH264EncoderEncodedBitrate = EncodedBitrate;
        m_VideoInputPt.m_OpenH264EncoderBitrateControlMode = BitrateControlMode;
        m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl = IDRFrameIntvl;
        m_VideoInputPt.m_OpenH264EncoderComplexity = Complexity;
    }

    //设置视频输入使用的设备。
    public void SetVideoInputUseDevice( int UseFrontOrBack )
    {
        m_VideoInputPt.m_UseWhatVideoInputDevice = ( UseFrontOrBack == 0 ) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK; //设置视频输入设备。
    }

    //设置视频输入设备是否黑屏。
    public void SetVideoInputDeviceIsBlack( int IsBlack )
    {
        m_VideoInputPt.m_VideoInputDeviceIsBlack = IsBlack;
    }

    //设置是否使用视频输出。
    public void SetUseVideoOutput( int IsUseVideoOutput, int FrameWidth, int FrameHeight, HTSurfaceView VideoDisplaySurfaceViewPt, float VideoDisplayScale )
    {
        if( ( ( IsUseVideoOutput != 0 ) && ( FrameWidth <= 0 ) ) || //如果帧的宽度不正确。
            ( ( IsUseVideoOutput != 0 ) && ( FrameHeight <= 0 ) ) || //如果帧的高度不正确。
            ( ( IsUseVideoOutput != 0 ) && ( VideoDisplaySurfaceViewPt == null ) ) ) //如果视频显示SurfaceView类对象的内存指针不正确。
        {
            return;
        }

        m_VideoOutputPt.m_IsUseVideoOutput = IsUseVideoOutput;
        m_VideoOutputPt.m_FrameWidth = FrameWidth;
        m_VideoOutputPt.m_FrameHeight = FrameHeight;
        m_VideoOutputPt.m_VideoDisplaySurfaceViewPt = VideoDisplaySurfaceViewPt;
        m_VideoOutputPt.m_VideoDisplayScale = VideoDisplayScale;
    }

    //设置视频输出要使用YU12原始数据。
    public void SetVideoOutputUseYU12()
    {
        m_VideoOutputPt.m_UseWhatDecoder = 0;
    }

    //设置视频输出要使用OpenH264解码器。
    public void SetVideoOutputUseOpenH264( int DecodeThreadNum )
    {
        m_VideoOutputPt.m_UseWhatDecoder = 1;
        m_VideoOutputPt.m_OpenH264DecoderDecodeThreadNum = DecodeThreadNum;
    }

    //设置视频输出设备是否黑屏。
    public void SetVideoOutputDeviceIsBlack( int IsBlack )
    {
        m_VideoOutputPt.m_VideoOutputDeviceIsBlack = IsBlack;
    }

    //请求本线程退出。
    public int RequireExit( int ExitFlag, int IsBlockWait )
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

            p_Result = 0;
        }

        return p_Result;
    }

    //音频输入线程类。
    private class AudioInputThread extends Thread
    {
        public int m_ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_ExitFlag = 1;
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            short p_PcmAudioInputFramePt[]; //存放PCM格式音频输入帧。
            int p_TmpInt32;
            long p_LastMsec = 0;
            long p_NowMsec = 0;

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

            if( m_AudioInputPt.m_UseWhatAec != 0 ) //如果要使用音频输入的声学回音消除，就自适应计算声学回音的延迟，并设置到WebRtc定点版和浮点版声学回音消除器。
            {
                int p_Delay = 0; //存放声学回音的延迟，单位毫秒。
                HTInt p_HTIntDelay = new HTInt();

                //计算音频输出的延迟。
                m_AudioOutputPt.m_AudioOutputDevicePt.play(); //让音频输出设备类对象开始播放。
                p_PcmAudioInputFramePt = new short[ m_AudioOutputPt.m_FrameLen ]; //创建一个空的音频输出帧。
                p_LastMsec = System.currentTimeMillis();
                while( true )
                {
                    m_AudioOutputPt.m_AudioOutputDevicePt.write( p_PcmAudioInputFramePt, 0, p_PcmAudioInputFramePt.length ); //播放一个空的音频输出帧。
                    p_NowMsec = System.currentTimeMillis();
                    p_Delay += m_AudioOutputPt.m_FrameLen; //递增音频输出的延迟。
                    if( p_NowMsec - p_LastMsec >= 10 ) //如果播放耗时较长，就表示音频输出类对象的缓冲区已经写满，结束计算。
                    {
                        break;
                    }
                    p_LastMsec = p_NowMsec;
                }
                p_Delay = p_Delay * 1000 / m_AudioOutputPt.m_SamplingRate; //将音频输出的延迟转换为毫秒。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟：" + p_Delay + " 毫秒。" );

                //计算音频输入的延迟。
                m_AudioInputPt.m_AudioInputDevicePt.startRecording(); //让音频输入设备类对象开始录音。
                p_PcmAudioInputFramePt = new short[ m_AudioInputPt.m_FrameLen ]; //创建一个空的音频输入帧。
                p_LastMsec = System.currentTimeMillis();
                m_AudioInputPt.m_AudioInputDevicePt.read( p_PcmAudioInputFramePt, 0, p_PcmAudioInputFramePt.length ); //计算读取一个音频输入帧的耗时。
                p_NowMsec = System.currentTimeMillis();
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟：" + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );

                m_AudioOutputPt.m_AudioOutputThreadPt.start(); //启动音频输出线程。

                //计算声学回音的延迟。
                p_Delay = p_Delay + ( int ) ( p_NowMsec - p_LastMsec );
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
                m_AudioInputPt.m_AudioInputDevicePt.startRecording(); //让音频输入设备类对象开始录音。
                if( m_AudioOutputPt.m_IsUseAudioOutput != 0 ) //如果要使用音频输出。
                {
                    m_AudioOutputPt.m_AudioOutputDevicePt.play(); //让音频输出设备类对象开始播放。
                    m_AudioOutputPt.m_AudioOutputThreadPt.start(); //启动音频输出线程。
                }
            }

            //开始音频输入循环。
            out:
            while( true )
            {
                //获取一个音频输入空闲帧。
                if( ( p_TmpInt32 = m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.size() ) > 0 ) //如果音频输入空闲帧链表中有音频输入空闲帧。
                {
                    //从音频输入空闲帧链表中取出第一个音频输入空闲帧。
                    synchronized( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt )
                    {
                        p_PcmAudioInputFramePt = m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.getFirst();
                        m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：从音频输入空闲帧链表中取出第一个音频输入空闲帧，音频输入空闲帧链表元素个数：" + p_TmpInt32 + "。" );
                }
                else //如果音频输入空闲帧链表中没有音频输入空闲帧。
                {
                    if( ( p_TmpInt32 = m_AudioInputPt.m_AudioInputFrameLnkLstPt.size() ) <= 50 )
                    {
                        p_PcmAudioInputFramePt = new short[m_AudioInputPt.m_FrameLen]; //创建一个音频输入空闲帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：音频输入空闲帧链表中没有音频输入空闲帧，创建一个音频输入空闲帧。" );
                    }
                    else
                    {
                        p_PcmAudioInputFramePt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输入线程：音频输入帧链表中音频输入帧数量为" + p_TmpInt32 + "已经超过上限50，不再创建一个音频输入空闲帧。" );
                    }
                }

                if( p_PcmAudioInputFramePt != null ) //如果获取了一个音频输入空闲帧。
                {
                    if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                    //读取本次音频输入帧。
                    m_AudioInputPt.m_AudioInputDevicePt.read( p_PcmAudioInputFramePt, 0, p_PcmAudioInputFramePt.length );

                    //追加本次音频输入帧到音频输入帧链表。
                    synchronized( m_AudioInputPt.m_AudioInputFrameLnkLstPt )
                    {
                        m_AudioInputPt.m_AudioInputFrameLnkLstPt.addLast( p_PcmAudioInputFramePt );
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "音频输入线程：本次音频输入帧读取完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                    }
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            } //音频输入循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程已退出。" );
        }
    }

    //音频输出线程类。
    private class AudioOutputThread extends Thread
    {
        public int m_ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_ExitFlag = 1;
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            short p_PcmAudioOutputFramePt[]; //存放PCM格式音频输出帧。
            byte p_EncoderAudioOutputFramePt[] = ( m_AudioOutputPt.m_UseWhatDecoder != 0 ) ? new byte[ m_AudioOutputPt.m_FrameLen ] : null; //存放已编码格式音频输出帧。
            HTLong p_EncoderAudioOutputFrameLenPt = ( m_AudioOutputPt.m_UseWhatDecoder != 0 ) ? new HTLong() : null; //存放已编码格式音频输出帧的数据长度，单位字节。
            long p_LastMsec = 0;
            long p_NowMsec = 0;
            int p_TmpInt32;

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );

            //开始音频输出循环。
            out:
            while( true )
            {
                //获取一个音频输出空闲帧。
                if( ( p_TmpInt32 = m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.size() ) > 0 ) //如果音频输出空闲帧链表中有音频输出空闲帧。
                {
                    //从音频输出空闲帧链表中取出第一个音频输出空闲帧。
                    synchronized( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt )
                    {
                        p_PcmAudioOutputFramePt = m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.getFirst();
                        m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：从音频输出空闲帧链表中取出第一个音频输出空闲帧，音频输出空闲帧链表元素个数：" + p_TmpInt32 + "。" );
                }
                else //如果音频输出空闲帧链表中没有音频输出空闲帧。
                {
                    if( ( p_TmpInt32 = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.size() ) <= 50 )
                    {
                        p_PcmAudioOutputFramePt = new short[m_AudioOutputPt.m_FrameLen]; //创建一个音频输出空闲帧。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：音频输出空闲帧链表中没有音频输出空闲帧，创建一个音频输出空闲帧。" );
                    }
                    else
                    {
                        p_PcmAudioOutputFramePt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：音频输出帧链表中音频输出帧数量为" + p_TmpInt32 + "已经超过上限50，不再创建一个音频输出空闲帧。" );
                    }
                }

                if( p_PcmAudioOutputFramePt != null ) //如果获取了一个音频输出空闲帧。
                {
                    if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                    //调用用户定义的写入输出帧函数，并解码成PCM原始数据。
                    switch( m_AudioOutputPt.m_UseWhatDecoder ) //使用什么解码器。
                    {
                        case 0: //如果要使用PCM原始数据。
                        {
                            //调用用户定义的写入音频输出帧函数。
                            UserWriteAudioOutputFrame( p_PcmAudioOutputFramePt, null, null );
                            break;
                        }
                        case 1: //如果要使用Speex解码器。
                        {
                            //调用用户定义的写入音频输出帧函数。
                            UserWriteAudioOutputFrame( null, p_EncoderAudioOutputFramePt, p_EncoderAudioOutputFrameLenPt );

                            //使用Speex解码器。
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Proc( p_EncoderAudioOutputFramePt, p_EncoderAudioOutputFrameLenPt.m_Val, p_PcmAudioOutputFramePt ) == 0 )
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

                    //判断音频输出设备是否静音。在音频处理完后再设置静音，这样可以保证音频处理器的连续性。
                    if( m_AudioOutputPt.m_AudioOutputDeviceIsMute != 0 )
                    {
                        Arrays.fill( p_PcmAudioOutputFramePt, ( short ) 0 );
                    }

                    //写入本次输出帧。
                    m_AudioOutputPt.m_AudioOutputDevicePt.write( p_PcmAudioOutputFramePt, 0, p_PcmAudioOutputFramePt.length );

                    //调用用户定义的获取PCM格式输出帧函数。
                    UserGetPcmAudioOutputFrame( p_PcmAudioOutputFramePt );

                    //追加本次输出帧到音频输出帧链表。
                    synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                    {
                        m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.addLast( p_PcmAudioOutputFramePt );
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "音频输出线程：本次音频输出帧写入完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                    }
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            } //音频输出循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
        }
    }

    //视频输入线程类。
    private class VideoInputThread extends Thread implements Camera.PreviewCallback
    {
        public int m_ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_ExitFlag = 1;
        }

        //读取一个视频输入帧的预览回调函数，本函数是在主线程中运行的。
        @Override
        public void onPreviewFrame( byte[] data, Camera camera )
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

            byte p_NV21VideoInputFramePt[] = null;
            VideoInputFrameElm p_VideoInputFrameElmPt = null;
            long p_LastMsec = 0;
            long p_NowMsec = 0;
            int p_TmpInt32;

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：开始准备视频输入。" );

            //开始视频输入循环。
            out:
            while( true )
            {
                //处理视频输入帧。
                if( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.size() > 0 )//如果NV21格式视频输入帧链表中有帧了。
                {
                    //获取一个视频输入空闲帧。
                    if( ( p_TmpInt32 = m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.size() ) > 0 ) //如果视频输入空闲帧链表中有视频输入空闲帧。
                    {
                        //从视频输入空闲帧链表中取出第一个视频输入空闲帧。
                        synchronized( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt )
                        {
                            p_VideoInputFrameElmPt = m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.getFirst();
                            m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从视频输入空闲帧链表中取出第一个视频输入空闲帧，视频输入空闲帧链表元素个数：" + p_TmpInt32 + "。" );
                    }
                    else //如果视频输入空闲帧链表中没有视频输入空闲帧。
                    {
                        if( ( p_TmpInt32 = m_VideoInputPt.m_VideoInputFrameLnkLstPt.size() ) <= 20 )
                        {
                            p_VideoInputFrameElmPt = new VideoInputFrameElm(); //创建一个视频输入空闲帧。
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：视频输入空闲帧链表中没有视频输入空闲帧，创建一个视频输入空闲帧。" );
                        }
                        else
                        {
                            p_VideoInputFrameElmPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：视频输入帧链表中视频输入帧数量为" + p_TmpInt32 + "已经超过上限20，不再创建一个视频输入空闲帧。" );
                        }
                    }

                    if( p_VideoInputFrameElmPt != null ) //如果获取了一个视频输入空闲帧。
                    {
                        if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                        //从视频输入帧链表中取出第一个视频输入帧。
                        p_TmpInt32 = m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.size();
                        synchronized( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt )
                        {
                            p_NV21VideoInputFramePt = m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.getFirst();
                            m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：从NV21格式视频输入帧链表中取出第一个NV21格式视频输入帧，NV21格式视频输入帧链表元素个数：" + p_TmpInt32 + "。" );

                        //NV21格式视频输入帧旋转为YU12格式视频输入帧。
                        if( LibYUV.PictrRotate( p_NV21VideoInputFramePt, LibYUV.PICTR_FMT_NV21, m_VideoInputPt.m_FrameHeight, m_VideoInputPt.m_FrameWidth, m_VideoInputPt.m_VideoInputFrameRotate, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt.length, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameWidthPt, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameHeightPt, null ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧失败。" );
                            break out;
                        }

                        //判断视频输入设备是否黑屏。在视频输入处理完后再设置黑屏，这样可以保证视频输入处理器的连续性。
                        if( m_VideoInputPt.m_VideoInputDeviceIsBlack != 0 )
                        {
                            int p_TmpLen = p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameWidthPt.m_Val * p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameHeightPt.m_Val;
                            Arrays.fill( p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt, 0, p_TmpLen, ( byte ) 0 );
                            Arrays.fill( p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt, p_TmpLen, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt.length, ( byte ) 128 );
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
                                if( m_VideoInputPt.m_OpenH264EncoderPt.Proc( p_VideoInputFrameElmPt.m_RotateYU12VideoInputFramePt, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameWidthPt.m_Val, p_VideoInputFrameElmPt.m_RotateYU12VideoInputFrameHeightPt.m_Val, 0, p_VideoInputFrameElmPt.m_EncoderVideoInputFramePt, p_VideoInputFrameElmPt.m_EncoderVideoInputFramePt.length, p_VideoInputFrameElmPt.m_EncoderVideoInputFrameLenPt, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器成功。H264格式视频输入帧的数据长度：" + p_VideoInputFrameElmPt.m_EncoderVideoInputFrameLenPt.m_Val + "，时间戳：" + p_LastMsec );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器失败。" );
                                }
                                break;
                            }
                        }

                        //追加本次视频输入帧到视频输入帧链表。
                        synchronized( m_VideoInputPt.m_VideoInputFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputFrameLnkLstPt.addLast( p_VideoInputFrameElmPt );
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowMsec = System.currentTimeMillis();
                            Log.i( m_CurClsNameStrPt, "视频输入线程：本次视频输入帧处理完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                        }
                    }

                    //追加本次NV21格式视频输入帧到视频输入设备。
                    m_VideoInputPt.m_VideoInputDevicePt.addCallbackBuffer( p_NV21VideoInputFramePt );
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }

                SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
            } //视频输入循环完毕。

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输入线程：本线程已退出。" );
        }
    }

    //视频输出线程类。
    private class VideoOutputThread extends Thread
    {
        public int m_ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_ExitFlag = 1;
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            VideoOutputFrameElm p_EncoderVideoOutputFramePt = null;
            byte p_YU12VideoOutputFramePt[] = ( m_VideoOutputPt.m_UseWhatDecoder != 0 ) ? new byte[ 960 * 1280 * 3 / 2 ] : null;
            HTInt p_YU12VideoOutputFrameWidth = new HTInt();
            HTInt p_YU12VideoOutputFrameHeigth = new HTInt();
            byte p_ScaleYU12VideoOutputFramePt[] = null;
            HTInt p_ScaleYU12VideoOutputFrameWidth = null;
            HTInt p_ScaleYU12VideoOutputFrameHeigth = null;
            long p_LastMsec = 0;
            long p_NowMsec = 0;
            int p_TmpInt32;

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：开始准备视频输出。" );

            //开始视频输出循环。
            out:
            while( true )
            {
                //显示一个视频输出帧。
                if( ( p_TmpInt32 = m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.size() ) > 0 ) //如果视频输出帧链表中有帧了。
                {
                    if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                    //从视频输出帧链表中取出第一个视频输出帧。
                    synchronized( m_VideoOutputPt.m_VideoOutputFrameLnkLstPt )
                    {
                        p_EncoderVideoOutputFramePt = m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.getFirst();
                        m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.removeFirst();
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：从视频输出帧链表中取出第一个视频输出帧，视频输出帧链表元素个数：" + p_TmpInt32 + "。" );

                    //解码成YU12原始数据。
                    switch( m_VideoOutputPt.m_UseWhatDecoder ) //使用什么解码器。
                    {
                        case 0: //如果要使用YU12原始数据。
                        {
                            p_YU12VideoOutputFramePt = p_EncoderVideoOutputFramePt.m_VideoOutputFramePt;
                            p_YU12VideoOutputFrameWidth.m_Val = m_VideoOutputPt.m_FrameWidth;
                            p_YU12VideoOutputFrameHeigth.m_Val = m_VideoOutputPt.m_FrameHeight;

                            break;
                        }
                        case 1: //如果要使用OpenH264解码器。
                        {
                            if( m_VideoOutputPt.m_OpenH264DecoderPt.Proc( p_EncoderVideoOutputFramePt.m_VideoOutputFramePt, p_EncoderVideoOutputFramePt.m_VideoOutputFrameLen, p_YU12VideoOutputFramePt, p_YU12VideoOutputFramePt.length, p_YU12VideoOutputFrameWidth, p_YU12VideoOutputFrameHeigth, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出线程：使用OpenH264解码器失败。" );
                            }

                            break;
                        }
                    }

                    //判断音频输出设备是否静音。在音频处理完后再设置静音，这样可以保证音频处理器的连续性。
                    if( m_VideoOutputPt.m_VideoOutputDeviceIsBlack != 0 )
                    {
                        int p_TmpLen = p_YU12VideoOutputFrameWidth.m_Val * p_YU12VideoOutputFrameHeigth.m_Val;
                        Arrays.fill( p_YU12VideoOutputFramePt, 0, p_TmpLen, ( byte ) 0 );
                        Arrays.fill( p_YU12VideoOutputFramePt, p_TmpLen, p_TmpLen + p_TmpLen / 2, ( byte ) 128 );
                    }

                    //缩放视频输出帧。
                    if( m_VideoOutputPt.m_VideoDisplayScale != 1.0f )
                    {
                        if( p_ScaleYU12VideoOutputFramePt == null )
                        {
                            p_ScaleYU12VideoOutputFramePt = new byte[( int ) ( p_YU12VideoOutputFrameWidth.m_Val * m_VideoOutputPt.m_VideoDisplayScale * p_YU12VideoOutputFrameHeigth.m_Val * m_VideoOutputPt.m_VideoDisplayScale * 3 / 2 )];
                            p_ScaleYU12VideoOutputFrameWidth = new HTInt( ( int ) ( p_YU12VideoOutputFrameWidth.m_Val * m_VideoOutputPt.m_VideoDisplayScale ) );
                            p_ScaleYU12VideoOutputFrameHeigth = new HTInt( ( int ) ( p_YU12VideoOutputFrameHeigth.m_Val * m_VideoOutputPt.m_VideoDisplayScale ) );
                        }

                        if( LibYUV.PictrScale( p_YU12VideoOutputFramePt, LibYUV.PICTR_FMT_YU12, p_YU12VideoOutputFrameWidth.m_Val, p_YU12VideoOutputFrameHeigth.m_Val, p_ScaleYU12VideoOutputFramePt, p_ScaleYU12VideoOutputFramePt.length, p_ScaleYU12VideoOutputFrameWidth.m_Val, p_ScaleYU12VideoOutputFrameHeigth.m_Val, 3, null, null ) != 0 )
                        {
                            Log.e( m_CurClsNameStrPt, "视频输出线程：缩放失败。" );

                            System.arraycopy( p_YU12VideoOutputFramePt, 0, p_ScaleYU12VideoOutputFramePt, 0, p_YU12VideoOutputFrameWidth.m_Val * p_YU12VideoOutputFrameHeigth.m_Val * 3 / 2 );
                            p_ScaleYU12VideoOutputFrameWidth.m_Val = p_YU12VideoOutputFrameWidth.m_Val;
                            p_ScaleYU12VideoOutputFrameHeigth.m_Val = p_YU12VideoOutputFrameHeigth.m_Val;
                        }
                    }
                    else
                    {
                        p_ScaleYU12VideoOutputFramePt = p_YU12VideoOutputFramePt;
                        p_ScaleYU12VideoOutputFrameWidth = p_YU12VideoOutputFrameWidth;
                        p_ScaleYU12VideoOutputFrameHeigth = p_YU12VideoOutputFrameHeigth;
                    }

                    //设置视频显示SurfaceView类对象的宽高比。
                    m_VideoOutputPt.m_VideoDisplaySurfaceViewPt.setWidthToHeightRatio( ( float )p_ScaleYU12VideoOutputFrameWidth.m_Val / p_ScaleYU12VideoOutputFrameHeigth.m_Val );

                    //渲染视频输出帧到视频显示SurfaceView类对象。
                    if( LibYUV.PictrDrawToSurface( p_ScaleYU12VideoOutputFramePt, 0, LibYUV.PICTR_FMT_YU12, p_ScaleYU12VideoOutputFrameWidth.m_Val, p_ScaleYU12VideoOutputFrameHeigth.m_Val, m_VideoOutputPt.m_VideoDisplaySurfaceViewPt.getHolder().getSurface(), null ) != 0 )
                    {
                        Log.e( m_CurClsNameStrPt, "视频输出线程：渲染失败。" );
                    }

                    //追加本次视频输出帧到视频输出空闲帧链表。
                    synchronized( m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt )
                    {
                        m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt.addLast( p_EncoderVideoOutputFramePt );
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "视频输出线程：本次视频输出帧处理完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                    }
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
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

        short p_PcmAudioInputFramePt[] = null; //PCM格式音频输入帧。
        short p_PcmAudioOutputFramePt[] = null; //PCM格式音频输出帧。
        short p_PcmAudioResultFramePt[] = null; //PCM格式音频结果帧。
        short p_PcmAudioTmpFramePt[] = null; //PCM格式音频临时帧。
        short p_PcmAudioSwapFramePt[] = null; //PCM格式音频交换帧。
        HTInt p_VoiceActStsPt = null; //语音活动状态，为1表示有语音活动，为0表示无语音活动。
        byte p_EncoderAudioInputFramePt[] = null; //已编码格式音频输入帧。
        HTLong p_EncoderAudioInputFrameLenPt = null; //已编码格式音频输入帧的数据长度，单位字节。
        HTInt p_EncoderAudioInputFrameIsNeedTransPt = null; //已编码格式音频输入帧是否需要传输，为1表示需要传输，为0表示不需要传输。
        VideoInputFrameElm p_VideoInputFramePt = null; //视频输入帧。

        ReInit:
        while( true )
        {
            out:
            {
                m_RunFlag = RUN_FLAG_INIT; //设置本线程运行标记为刚开始运行正在初始化。

                if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本地代码的指令集名称（CPU类型+ ABI约定）为" + android.os.Build.CPU_ABI + "。手机型号为" + android.os.Build.MODEL + "。" );

                //初始化唤醒锁。
                WakeLockInitOrDestroy( m_IsUseWakeLock );

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
                        break out;
                    }
                }
                else //如果不要执行用户定义的初始化函数。
                {
                    m_ExitFlag = 0; //设置本线程退出标记为保持运行。
                    m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。
                }

                //保存设置到文件。
                if( m_IsSaveSettingToFile != 0 )
                {
                    File p_SettingFilePt = new File( m_SettingFileFullPathStrPt );
                    try
                    {
                        if( !p_SettingFilePt.exists() )
                        {
                            p_SettingFilePt.createNewFile();
                        }
                        FileWriter p_SettingFileWriterPt = new FileWriter( p_SettingFilePt );
                        p_SettingFileWriterPt.write( "m_AppContextPt：" + m_AppContextPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_IsSaveSettingToFile：" + m_IsSaveSettingToFile + "\n" );
                        p_SettingFileWriterPt.write( "m_SettingFileFullPathStrPt：" + m_SettingFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_IsPrintLogcat：" + m_IsPrintLogcat + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_IsUseWakeLock：" + m_IsUseWakeLock + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_IsUseAudioInput：" + m_AudioInputPt.m_IsUseAudioInput + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SamplingRate：" + m_AudioInputPt.m_SamplingRate + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_FrameLen：" + m_AudioInputPt.m_FrameLen + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioInputIsUseSystemAecNsAgc：" + m_AudioInputPt.m_IsUseSystemAecNsAgc + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioInputUseWhatAec：" + m_AudioInputPt.m_UseWhatAec + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecFilterLen：" + m_AudioInputPt.m_SpeexAecFilterLen + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecIsUseRec：" + m_AudioInputPt.m_SpeexAecIsUseRec + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoMultiple：" + m_AudioInputPt.m_SpeexAecEchoMultiple + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoCont：" + m_AudioInputPt.m_SpeexAecEchoCont + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoSupes：" + m_AudioInputPt.m_SpeexAecEchoSupes + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecEchoSupesAct：" + m_AudioInputPt.m_SpeexAecEchoSupesAct + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecIsSaveMemFile：" + m_AudioInputPt.m_SpeexAecIsSaveMemFile + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt：" + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmIsUseCNGMode：" + m_AudioInputPt.m_WebRtcAecmIsUseCNGMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmEchoMode：" + m_AudioInputPt.m_WebRtcAecmEchoMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecmDelay：" + m_AudioInputPt.m_WebRtcAecmDelay + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecEchoMode：" + m_AudioInputPt.m_WebRtcAecEchoMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecDelay：" + m_AudioInputPt.m_WebRtcAecDelay + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode：" + m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode：" + m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay：" + m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecIsSaveMemFile：" + m_AudioInputPt.m_WebRtcAecIsSaveMemFile + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt：" + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWorkMode：" + m_AudioInputPt.m_SpeexWebRtcAecWorkMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct：" + m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay：" + m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec：" + m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay：" + m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_UseWhatNs：" + m_AudioInputPt.m_UseWhatNs + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocIsUseNs：" + m_AudioInputPt.m_SpeexPprocIsUseNs + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocNoiseSupes：" + m_AudioInputPt.m_SpeexPprocNoiseSupes + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocIsUseDereverb：" + m_AudioInputPt.m_SpeexPprocIsUseDereverb + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcNsxPolicyMode：" + m_AudioInputPt.m_WebRtcNsxPolicyMode + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_WebRtcNsPolicyMode：" + m_AudioInputPt.m_WebRtcNsPolicyMode + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_IsUseSpeexPprocOther：" + m_AudioInputPt.m_IsUseSpeexPprocOther + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocIsUseVad：" + m_AudioInputPt.m_SpeexPprocIsUseVad + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocVadProbStart：" + m_AudioInputPt.m_SpeexPprocVadProbStart + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocVadProbCont：" + m_AudioInputPt.m_SpeexPprocVadProbCont + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocIsUseAgc：" + m_AudioInputPt.m_SpeexPprocIsUseAgc + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocAgcLevel：" + m_AudioInputPt.m_SpeexPprocAgcLevel + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocAgcIncrement：" + m_AudioInputPt.m_SpeexPprocAgcIncrement + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocAgcDecrement：" + m_AudioInputPt.m_SpeexPprocAgcDecrement + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexPprocAgcMaxGain：" + m_AudioInputPt.m_SpeexPprocAgcMaxGain + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_UseWhatCodec：" + m_AudioInputPt.m_UseWhatEncoder + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr：" + m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderQuality：" + m_AudioInputPt.m_SpeexEncoderQuality + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderComplexity：" + m_AudioInputPt.m_SpeexEncoderComplexity + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate：" + m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_IsSaveAudioToFile：" + m_AudioInputPt.m_IsSaveAudioToFile + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioInputFileFullPathStrPt：" + m_AudioInputPt.m_AudioInputFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioResultFileFullPathStrPt：" + m_AudioInputPt.m_AudioResultFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioInputDeviceBufSz：" + m_AudioInputPt.m_AudioInputDeviceBufSz + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioInputPt.m_AudioInputDeviceIsMute：" + m_AudioInputPt.m_AudioInputDeviceIsMute + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_IsUseAudioOutput：" + m_AudioOutputPt.m_IsUseAudioOutput + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_SamplingRate：" + m_AudioOutputPt.m_SamplingRate + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_FrameLen：" + m_AudioOutputPt.m_FrameLen + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_UseWhatCodec：" + m_AudioOutputPt.m_UseWhatDecoder + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement：" + m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_IsSaveAudioToFile：" + m_AudioOutputPt.m_IsSaveAudioToFile + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputFileFullPathStrPt：" + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputDeviceBufSz：" + m_AudioOutputPt.m_AudioOutputDeviceBufSz + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_UseWhatAudioOutputDevice：" + m_AudioOutputPt.m_UseWhatAudioOutputDevice + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_UseWhatAudioOutputStreamType：" + m_AudioOutputPt.m_UseWhatAudioOutputStreamType + "\n" );
                        p_SettingFileWriterPt.write( "m_AudioOutputPt.m_AudioOutputDeviceIsMute：" + m_AudioOutputPt.m_AudioOutputDeviceIsMute + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_IsUseVideoInput：" + m_VideoInputPt.m_IsUseVideoInput + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_MaxSamplingRate：" + m_VideoInputPt.m_MaxSamplingRate + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_FrameWidth：" + m_VideoInputPt.m_FrameWidth + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_FrameHeight：" + m_VideoInputPt.m_FrameHeight + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_UseWhatEncoder：" + m_VideoInputPt.m_UseWhatEncoder + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderVideoType：" + m_VideoInputPt.m_OpenH264EncoderVideoType + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderEncodedBitrate：" + m_VideoInputPt.m_OpenH264EncoderEncodedBitrate + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderBitrateControlMode：" + m_VideoInputPt.m_OpenH264EncoderBitrateControlMode + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl：" + m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_OpenH264EncoderComplexity：" + m_VideoInputPt.m_OpenH264EncoderComplexity + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_UseWhatVideoInputDevice：" + m_VideoInputPt.m_UseWhatVideoInputDevice + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoInputPt.m_VideoInputDeviceIsBlack：" + m_VideoInputPt.m_VideoInputDeviceIsBlack + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_IsUseVideoOutput：" + m_VideoOutputPt.m_IsUseVideoOutput + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_FrameWidth：" + m_VideoOutputPt.m_FrameWidth + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_FrameHeight：" + m_VideoOutputPt.m_FrameHeight + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_UseWhatDecoder：" + m_VideoOutputPt.m_UseWhatDecoder + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_OpenH264DecoderDecodeThreadNum：" + m_VideoOutputPt.m_OpenH264DecoderDecodeThreadNum + "\n" );
                        p_SettingFileWriterPt.write( "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_VideoDisplayScale：" + m_VideoOutputPt.m_VideoDisplayScale + "\n" );
                        p_SettingFileWriterPt.write( "m_VideoOutputPt.m_VideoOutputDeviceIsBlack：" + m_VideoOutputPt.m_VideoOutputDeviceIsBlack + "\n" );

                        p_SettingFileWriterPt.flush();
                        p_SettingFileWriterPt.close();
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + m_SettingFileFullPathStrPt + " 成功。" );
                    }
                    catch( IOException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：保存设置到文件 " + m_SettingFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
                        break out;
                    }
                }

                //创建PCM格式音频输入帧、PCM格式音频输出帧、PCM格式音频结果帧、PCM格式音频临时帧、PCM格式音频交换帧、语音活动状态、已编码格式音频输入帧、已编码格式音频输入帧的数据长度、已编码格式音频输入帧是否需要传输、视频输入帧。
                {
                    p_PcmAudioInputFramePt = null;
                    p_PcmAudioOutputFramePt = null;
                    p_PcmAudioResultFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new short[ m_AudioInputPt.m_FrameLen ] : null;
                    p_PcmAudioTmpFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new short[ m_AudioInputPt.m_FrameLen ] : null;
                    p_PcmAudioSwapFramePt = null;
                    p_VoiceActStsPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 ) ? new HTInt( 1 ) : null; //语音活动状态预设为1，为了让在不使用语音活动检测的情况下永远都是有语音活动。
                    p_EncoderAudioInputFramePt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new byte[ m_AudioInputPt.m_FrameLen ] : null;
                    p_EncoderAudioInputFrameLenPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new HTLong( 0 ) : null;
                    p_EncoderAudioInputFrameIsNeedTransPt = ( m_AudioInputPt.m_IsUseAudioInput != 0 && m_AudioInputPt.m_UseWhatEncoder != 0 ) ? new HTInt( 1 ) : null; //已编码格式音频输入帧是否需要传输预设为1，为了让在不使用非连续传输的情况下永远都是需要传输。
                    p_VideoInputFramePt = null;

                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建PCM格式音频输入帧、PCM格式音频输出帧、PCM格式音频结果帧、PCM格式音频临时帧、PCM格式音频交换帧、语音活动状态、已编码格式音频输入帧、已编码格式音频输入帧的数据长度、已编码格式音频输入帧是否需要传输、视频输入帧成功。" );
                }

                //初始化音频输入。
                if( m_AudioInputPt.m_IsUseAudioInput != 0 ) //如果要使用音频输入。
                {
                    //创建并初始化声学回音消除器类对象。
                    if( ( m_AudioInputPt.m_UseWhatAec != 0 ) && ( ( m_AudioOutputPt.m_IsUseAudioOutput == 0 ) || ( m_AudioOutputPt.m_SamplingRate != m_AudioInputPt.m_SamplingRate ) || ( m_AudioOutputPt.m_FrameLen != m_AudioInputPt.m_FrameLen ) ) )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：不使用音频输出、或音频输出的采样频率或帧的数据长度与音频输入不一致，不能使用声学回音消除器。" );
                        break out;
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
                                if( m_AudioInputPt.m_SpeexAecPt.InitByMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器类对象成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_SpeexAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据Speex声学回音消除器内存块文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器类对象失败。" );
                                }
                            }
                            if( m_AudioInputPt.m_SpeexAecPt == null )
                            {
                                m_AudioInputPt.m_SpeexAecPt = new SpeexAec();
                                if( m_AudioInputPt.m_SpeexAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器类对象成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_SpeexAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex声学回音消除器类对象失败。" );
                                    break out;
                                }
                            }
                            break;
                        }
                        case 2: //如果要使用WebRtc定点版声学回音消除器。
                        {
                            m_AudioInputPt.m_WebRtcAecmPt = new WebRtcAecm();
                            if( m_AudioInputPt.m_WebRtcAecmPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecmIsUseCNGMode, m_AudioInputPt.m_WebRtcAecmEchoMode, m_AudioInputPt.m_WebRtcAecmDelay ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcAecmPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版声学回音消除器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                        case 3: //如果要使用WebRtc浮点版声学回音消除器。
                        {
                            if( m_AudioInputPt.m_WebRtcAecIsSaveMemFile != 0 )
                            {
                                m_AudioInputPt.m_WebRtcAecPt = new WebRtcAec();
                                if( m_AudioInputPt.m_WebRtcAecPt.InitByMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器类对象成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_WebRtcAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器类对象失败。" );
                                }
                            }
                            if( m_AudioInputPt.m_WebRtcAecPt == null )
                            {
                                m_AudioInputPt.m_WebRtcAecPt = new WebRtcAec();
                                if( m_AudioInputPt.m_WebRtcAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器类对象成功。" );
                                }
                                else
                                {
                                    m_AudioInputPt.m_WebRtcAecPt = null;
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版声学回音消除器类对象失败。" );
                                    break out;
                                }
                            }
                            break;
                        }
                        case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                        {
                            m_AudioInputPt.m_SpeexWebRtcAecPt = new SpeexWebRtcAec();
                            if( m_AudioInputPt.m_SpeexWebRtcAecPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexWebRtcAecWorkMode, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecFilterLen, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecIsUseRec, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoMultiple, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoCont, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupes, m_AudioInputPt.m_SpeexWebRtcAecSpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmEchoMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecmDelay, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecEchoMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecDelay, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_SpeexWebRtcAecIsUseSameRoomAec, m_AudioInputPt.m_SpeexWebRtcAecSameRoomEchoMinDelay ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_SpeexWebRtcAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化SpeexWebRtc三重声学回音消除器类对象失败。" );
                                break out;
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
                            if( m_AudioInputPt.m_WebRtcNsxPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcNsxPolicyMode ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcNsxPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc定点版噪音抑制器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                        case 3: //如果要使用WebRtc浮点版噪音抑制器。
                        {
                            m_AudioInputPt.m_WebRtcNsPt = new WebRtcNs();
                            if( m_AudioInputPt.m_WebRtcNsPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcNsPolicyMode ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_WebRtcNsPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化WebRtc浮点版噪音抑制器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                        case 4: //如果要使用RNNoise噪音抑制器。
                        {
                            m_AudioInputPt.m_RNNoisePt = new RNNoise();
                            if( m_AudioInputPt.m_RNNoisePt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_RNNoisePt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化RNNoise噪音抑制器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                    }

                    //创建并初始化Speex预处理器类对象。
                    if( ( m_AudioInputPt.m_UseWhatNs == 1 ) || ( m_AudioInputPt.m_IsUseSpeexPprocOther != 0 ) )
                    {
                        if( m_AudioInputPt.m_UseWhatNs != 1 )
                        {
                            m_AudioInputPt.m_SpeexPprocIsUseNs = 0;
                            m_AudioInputPt.m_SpeexPprocIsUseDereverb = 0;
                        }
                        if( m_AudioInputPt.m_IsUseSpeexPprocOther == 0 )
                        {
                            m_AudioInputPt.m_SpeexPprocIsUseVad = 0;
                            m_AudioInputPt.m_SpeexPprocIsUseAgc = 0;
                        }
                        m_AudioInputPt.m_SpeexPprocPt = new SpeexPproc();
                        if( m_AudioInputPt.m_SpeexPprocPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexPprocIsUseNs, m_AudioInputPt.m_SpeexPprocNoiseSupes, m_AudioInputPt.m_SpeexPprocIsUseDereverb, m_AudioInputPt.m_SpeexPprocIsUseVad, m_AudioInputPt.m_SpeexPprocVadProbStart, m_AudioInputPt.m_SpeexPprocVadProbCont, m_AudioInputPt.m_SpeexPprocIsUseAgc, m_AudioInputPt.m_SpeexPprocAgcLevel, m_AudioInputPt.m_SpeexPprocAgcIncrement, m_AudioInputPt.m_SpeexPprocAgcDecrement, m_AudioInputPt.m_SpeexPprocAgcMaxGain ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器类对象成功。"  );
                        }
                        else
                        {
                            m_AudioInputPt.m_SpeexPprocPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex预处理器类对象失败。" );
                            break out;
                        }
                    }

                    //初始化编解码器对象。
                    switch( m_AudioInputPt.m_UseWhatEncoder )
                    {
                        case 0: //如果要使用PCM原始数据。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用PCM原始数据。" );
                            break;
                        }
                        case 1: //如果要使用Speex编解码器。
                        {
                            if( m_AudioInputPt.m_FrameLen != m_AudioInputPt.m_SamplingRate / 1000 * 20 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的数据长度不为20毫秒不能使用Speex编解码器。" );
                                break out;
                            }
                            m_AudioInputPt.m_SpeexEncoderPt = new SpeexEncoder();
                            if( m_AudioInputPt.m_SpeexEncoderPt.Init( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_SpeexEncoderUseCbrOrVbr, m_AudioInputPt.m_SpeexEncoderQuality, m_AudioInputPt.m_SpeexEncoderComplexity, m_AudioInputPt.m_SpeexEncoderPlcExpectedLossRate ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex编码器类对象成功。" );
                            }
                            else
                            {
                                m_AudioInputPt.m_SpeexEncoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex编码器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                        case 2: //如果要使用Opus编解码器。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus编解码器。" );
                            break out;
                        }
                    }

                    //创建并初始化音频输入Wave文件写入器类对象、音频结果Wave文件写入器类对象。
                    if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                    {
                        m_AudioInputPt.m_AudioInputWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.Init( m_AudioInputPt.m_AudioInputFileFullPathStrPt, ( short ) 1, m_AudioInputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AudioInputPt.m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioInputWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入文件 " + m_AudioInputPt.m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                            break out;
                        }
                        m_AudioInputPt.m_AudioResultWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.Init( m_AudioInputPt.m_AudioResultFileFullPathStrPt, ( short ) 1, m_AudioInputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AudioInputPt.m_AudioResultFileFullPathStrPt + " 的Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            m_AudioInputPt.m_AudioResultWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频结果文件 " + m_AudioInputPt.m_AudioResultFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                            break out;
                        }
                    }

                    //创建并初始化音频输入设备类对象。
                    try
                    {
                        m_AudioInputPt.m_AudioInputDeviceBufSz = AudioRecord.getMinBufferSize( m_AudioInputPt.m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                        m_AudioInputPt.m_AudioInputDeviceBufSz = ( m_AudioInputPt.m_AudioInputDeviceBufSz > m_AudioInputPt.m_FrameLen * 2 ) ? m_AudioInputPt.m_AudioInputDeviceBufSz : m_AudioInputPt.m_FrameLen * 2;
                        m_AudioInputPt.m_AudioInputDevicePt = new AudioRecord(
                                ( m_AudioInputPt.m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
                                m_AudioInputPt.m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioInputPt.m_AudioInputDeviceBufSz
                        );
                        if( m_AudioInputPt.m_AudioInputDevicePt.getState() == AudioRecord.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备类对象成功。音频输入设备缓冲区大小：" + m_AudioInputPt.m_AudioInputDeviceBufSz );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备类对象失败。" );
                            break out;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入设备类对象失败。原因：" + e.getMessage() );
                        break out;
                    }

                    //创建并初始化音频输入帧链表类对象。
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入帧链表类对象成功。" );

                    //创建并初始化音频输入空闲帧链表类对象。
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入空闲帧链表类对象成功。" );

                    //创建并初始化音频输入线程类对象。
                    m_AudioInputPt.m_AudioInputThreadPt = new AudioInputThread();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输入线程类对象成功。" );
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
                        case 1: //如果要使用Speex编解码器。
                        {
                            if( m_AudioOutputPt.m_FrameLen != m_AudioOutputPt.m_SamplingRate / 1000 * 20 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：帧的数据长度不为20毫秒不能使用Speex编解码器。" );
                                break out;
                            }
                            m_AudioOutputPt.m_SpeexDecoderPt = new SpeexDecoder();
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Init( m_AudioOutputPt.m_SamplingRate, m_AudioOutputPt.m_SpeexDecoderIsUsePerceptualEnhancement ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex解码器类对象成功。" );
                            }
                            else
                            {
                                m_AudioOutputPt.m_SpeexDecoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化Speex解码器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                        case 2: //如果要使用Opus编解码器。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：暂不支持使用Opus编解码器。" );
                            break out;
                        }
                    }

                    //创建并初始化音频输出Wave文件写入器类对象。
                    if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                    {
                        m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = new WaveFileWriter();
                        if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.Init( m_AudioOutputPt.m_AudioOutputFileFullPathStrPt, ( short ) 1, m_AudioOutputPt.m_SamplingRate, 16 ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + " 的Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出文件 " + m_AudioOutputPt.m_AudioOutputFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                            break out;
                        }
                    }

                    //设置音频输出设备。
                    if( m_AudioOutputPt.m_UseWhatAudioOutputDevice == 0 ) //如果要使用扬声器。
                    {
                        ( ( AudioManager )m_AppContextPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( true ); //打开扬声器。
                    }
                    else //如果要使用听筒。
                    {
                        ( ( AudioManager )m_AppContextPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( false ); //关闭扬声器。
                    }

                    //用第一种方法创建并初始化音频输出设备类对象。
                    try
                    {
                        m_AudioOutputPt.m_AudioOutputDeviceBufSz = m_AudioOutputPt.m_FrameLen * 2;
                        m_AudioOutputPt.m_AudioOutputDevicePt = new AudioTrack( ( m_AudioOutputPt.m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                m_AudioOutputPt.m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioOutputPt.m_AudioOutputDeviceBufSz,
                                AudioTrack.MODE_STREAM );
                        if( m_AudioOutputPt.m_AudioOutputDevicePt.getState() == AudioTrack.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备类对象成功。音频输出设备缓冲区大小：" + m_AudioOutputPt.m_AudioOutputDeviceBufSz );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备类对象失败。" );
                            m_AudioOutputPt.m_AudioOutputDevicePt.release();
                            m_AudioOutputPt.m_AudioOutputDevicePt = null;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第一种方法创建并初始化音频输出设备类对象失败。原因：" + e.getMessage() );
                    }

                    //用第二种方法创建并初始化音频输出设备类对象。
                    if( m_AudioOutputPt.m_AudioOutputDevicePt == null )
                    {
                        try
                        {
                            m_AudioOutputPt.m_AudioOutputDeviceBufSz = AudioTrack.getMinBufferSize( m_AudioOutputPt.m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                            m_AudioOutputPt.m_AudioOutputDevicePt = new AudioTrack( ( m_AudioOutputPt.m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                    m_AudioOutputPt.m_SamplingRate,
                                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    m_AudioOutputPt.m_AudioOutputDeviceBufSz,
                                    AudioTrack.MODE_STREAM );
                            if( m_AudioOutputPt.m_AudioOutputDevicePt.getState() == AudioTrack.STATE_INITIALIZED )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备类对象成功。音频输出设备缓冲区大小：" + m_AudioOutputPt.m_AudioOutputDeviceBufSz );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备类对象失败。" );
                                break out;
                            }
                        }
                        catch( IllegalArgumentException e )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：用第二种方法创建并初始化音频输出设备类对象失败。原因：" + e.getMessage() );
                            break out;
                        }
                    }

                    //创建并初始化音频输出帧链表类对象。
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出帧链表类对象成功。" );

                    //创建并初始化音频输出空闲帧链表类对象。
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出空闲帧链表类对象成功。" );

                    //创建并初始化音频输出线程类对象。
                    m_AudioOutputPt.m_AudioOutputThreadPt = new AudioOutputThread();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化音频输出线程类对象成功。" );
                } //初始化音频输出完毕。

                //初始化视频输入。
                if( m_VideoInputPt.m_IsUseVideoInput != 0 )
                {
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
                            if( m_VideoInputPt.m_OpenH264EncoderPt.Init( m_VideoInputPt.m_FrameWidth, m_VideoInputPt.m_FrameHeight, m_VideoInputPt.m_OpenH264EncoderVideoType, m_VideoInputPt.m_OpenH264EncoderEncodedBitrate, m_VideoInputPt.m_OpenH264EncoderBitrateControlMode, m_VideoInputPt.m_MaxSamplingRate, m_VideoInputPt.m_OpenH264EncoderIDRFrameIntvl, m_VideoInputPt.m_OpenH264EncoderComplexity, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器类对象成功。" );
                            }
                            else
                            {
                                m_VideoInputPt.m_OpenH264EncoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264编码器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                    }

                    //创建视频输入线程类对象。
                    m_VideoInputPt.m_VideoInputThreadPt = new VideoInputThread();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输入线程类对象成功。" );

                    //创建并初始化视频输入设备类对象。
                    {
                        //打开视频输入设备。
                        try
                        {
                            m_VideoInputPt.m_VideoInputDevicePt = Camera.open( m_VideoInputPt.m_UseWhatVideoInputDevice );
                        }
                        catch( RuntimeException e )
                        {
                            Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备类对象失败。原因：" + e.getMessage() );
                            break out;
                        }

                        Camera.Parameters p_CameraParaPt = m_VideoInputPt.m_VideoInputDevicePt.getParameters(); //获取视频输入设备的参数。

                        p_CameraParaPt.setPreviewFormat( ImageFormat.NV21 ); //设置预览帧的格式。

                        p_CameraParaPt.setPreviewFrameRate( m_VideoInputPt.m_MaxSamplingRate ); //设置最大采样频率。

                        p_CameraParaPt.setPreviewSize( m_VideoInputPt.m_FrameHeight, m_VideoInputPt.m_FrameWidth ); //设置预览帧的宽度为设置的高度，预览帧的高度为设置的宽度，因为预览帧处理的时候要旋转。

                        List<String> p_FocusModesListPt = p_CameraParaPt.getSupportedFocusModes();
                        String p_PreviewFocusModePt = "";
                        for( p_TmpInt321 = 0; p_TmpInt321 < p_FocusModesListPt.size(); p_TmpInt321++ )
                        {
                            switch( p_FocusModesListPt.get( p_TmpInt321 ) )
                            {
                                case Camera.Parameters.FOCUS_MODE_AUTO: break; //自动对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式启动焦点。
                                case Camera.Parameters.FOCUS_MODE_INFINITY:
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) &&
                                        !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_FIXED ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_INFINITY; break; //焦点设置在无限远处。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                case Camera.Parameters.FOCUS_MODE_MACRO: break; //微距（特写）对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式开始聚焦。
                                case Camera.Parameters.FOCUS_MODE_FIXED:
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                            !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
                                            !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_FIXED; break; //焦点是固定的。如果焦点无法调节，则相机始终处于此模式。如果相机具有自动对焦，则此模式可以固定焦点，通常处于超焦距。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                case Camera.Parameters.FOCUS_MODE_EDOF:
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
                                            !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_EDOF; break; //扩展景深（EDOF），对焦以数字方式连续进行。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
                                case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO:
                                    p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO; break; //用于视频的连续自动对焦模式。
                                case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE:
                                    if( !p_PreviewFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) )
                                        p_PreviewFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE; break; //用于拍照的连续自动对焦模式，比视频的连续自动对焦模式对焦速度更快。
                            }
                        }
                        p_CameraParaPt.setFocusMode( p_PreviewFocusModePt ); //设置对焦模式。

                        try
                        {
                            m_VideoInputPt.m_VideoInputDevicePt.setParameters( p_CameraParaPt ); //设置视频输入设备的参数。
                        }
                        catch( RuntimeException e )
                        {
                            Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备类对象失败。原因：" + e.getMessage() );
                            break out;
                        }

                        try
                        {
                            m_VideoInputPt.m_VideoInputDevicePt.setPreviewDisplay( m_VideoInputPt.m_VideoPreviewSurfaceViewPt.getHolder() ); //设置视频输入预览的SurfaceView类对象。
                            m_VideoInputPt.m_VideoPreviewSurfaceViewPt.setWidthToHeightRatio( ( float )m_VideoInputPt.m_FrameWidth / m_VideoInputPt.m_FrameHeight ); //设置视频预览SurfaceView类对象的宽高比。
                        }
                        catch( Exception ignored )
                        {
                        }
                        m_VideoInputPt.m_VideoInputDevicePt.setDisplayOrientation( 90 ); //调整相机拍到的图像旋转，不然竖着拿手机，图像是横着的。

                        //设置视频预览回调函数缓冲区的内存指针。
                        m_VideoInputPt.m_VideoPreviewCallbackBufferPtPt = new byte[ m_VideoInputPt.m_MaxSamplingRate ][ m_VideoInputPt.m_FrameWidth * m_VideoInputPt.m_FrameHeight * 3 / 2 ];
                        for( p_TmpInt321 = 0; p_TmpInt321 < m_VideoInputPt.m_MaxSamplingRate; p_TmpInt321++ )
                            m_VideoInputPt.m_VideoInputDevicePt.addCallbackBuffer( m_VideoInputPt.m_VideoPreviewCallbackBufferPtPt[p_TmpInt321] );

                        m_VideoInputPt.m_VideoInputDevicePt.setPreviewCallbackWithBuffer( m_VideoInputPt.m_VideoInputThreadPt ); //设置视频输入预览回调函数。

                        if( m_VideoInputPt.m_UseWhatVideoInputDevice == 1 ) //如果要使用前置摄像头。
                        {
                            m_VideoInputPt.m_VideoInputFrameRotate = 270; //设置视频输入帧的旋转角度。
                        }
                        else //如果要使用后置摄像头。
                        {
                            m_VideoInputPt.m_VideoInputFrameRotate = 90; //设置视频输入帧的旋转角度。
                        }

                        Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入设备类对象成功。" );
                    }

                    //创建并初始化NV21格式视频输入帧链表类对象。
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt = new LinkedList< byte[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化NV21格式视频输入帧链表类对象成功。" );

                    //创建并初始化视频输入帧链表类对象。
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt = new LinkedList< VideoInputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入帧链表类对象成功。" );

                    //创建并初始化视频输入空闲帧链表类对象。
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt = new LinkedList< VideoInputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输入空闲帧链表类对象成功。" );
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
                            if( m_VideoOutputPt.m_OpenH264DecoderPt.Init( m_VideoOutputPt.m_OpenH264DecoderDecodeThreadNum, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器类对象成功。" );
                            }
                            else
                            {
                                m_VideoOutputPt.m_OpenH264DecoderPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：创建并初始化OpenH264解码器类对象失败。" );
                                break out;
                            }
                            break;
                        }
                    }

                    //创建并初始化视频输出帧链表类对象。
                    m_VideoOutputPt.m_VideoOutputFrameLnkLstPt = new LinkedList< VideoOutputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输出帧链表类对象成功。" );

                    //创建并初始化视频输出空闲帧链表类对象。
                    m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt = new LinkedList< VideoOutputFrameElm >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建并初始化视频输出空闲帧链表类对象成功。" );

                    //创建视频输出线程类对象。
                    m_VideoOutputPt.m_VideoOutputThreadPt = new VideoOutputThread();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：创建视频输出线程类对象成功。" );
                } //初始化视频输出完毕。

                //启动音频输入线程、音频输出线程、视频输入线程、视频输出线程。必须在初始化最后启动这些线程，因为这些线程会使用初始化时的相关类对象。
                {
                    if( m_AudioInputPt.m_AudioInputThreadPt != null ) //如果要使用音频输入线程。
                    {
                        m_AudioInputPt.m_AudioInputThreadPt.start(); //启动音频输入线程，让音频输入线程再去启动音频输出线程。
                    }
                    else if( m_AudioOutputPt.m_AudioOutputDevicePt != null ) //如果要使用音频输出线程。
                    {
                        m_AudioOutputPt.m_AudioOutputDevicePt.play(); //让音频输出设备类对象开始播放。
                        m_AudioOutputPt.m_AudioOutputThreadPt.start(); //启动音频输出线程。
                    }

                    if( m_VideoInputPt.m_VideoInputDevicePt != null ) //如果要使用视频输入设备。
                    {
                        try
                        {
                            m_VideoInputPt.m_VideoInputDevicePt.startPreview(); //让视频输入设备类对象开始预览。
                        }
                        catch( RuntimeException e )
                        {
                            Log.e( m_CurClsNameStrPt, "媒体处理线程：让视频输入设备类对象开始预览失败。原因：" + e.getMessage() );
                            break out;
                        }
                        m_VideoInputPt.m_VideoInputThreadPt.start(); //启动视频输入线程。
                    }

                    if( m_VideoOutputPt.m_VideoOutputThreadPt != null ) //如果要使用视频输出线程。
                    {
                        m_VideoOutputPt.m_VideoOutputThreadPt.start(); //启动视频输出线程。
                    }
                }

                if( m_IsPrintLogcat != 0 )
                {
                    p_NowMsec = System.currentTimeMillis();
                    Log.i( m_CurClsNameStrPt, "媒体处理线程：媒体处理线程初始化完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，正式开始处理帧。" );
                }

                m_ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。
                m_RunFlag = RUN_FLAG_PROC; //设置本线程运行标记为初始化完毕正在循环处理帧。

                //开始音视频输入输出帧处理循环。
                while( true )
                {
                    if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                    //调用用户定义的处理函数。
                    p_TmpInt321 = UserProcess();
                    if( p_TmpInt321 == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt321 );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt321 );
                        break out;
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
                            p_PcmAudioInputFramePt = m_AudioInputPt.m_AudioInputFrameLnkLstPt.getFirst();
                            m_AudioInputPt.m_AudioInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

                        //从音频输出帧链表中取出第一个音频输出帧。
                        synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                        {
                            p_PcmAudioOutputFramePt = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.getFirst();
                            m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );

                        //将音频输入帧复制到音频结果帧，方便处理。
                        System.arraycopy( p_PcmAudioInputFramePt, 0, p_PcmAudioResultFramePt, 0, p_PcmAudioInputFramePt.length );
                    }
                    else if( ( p_TmpInt321 > 0 ) && ( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt == null ) ) //如果音频输入帧链表有帧了，且不使用音频输出帧链表，就开始取出。
                    {
                        //从音频输入帧链表中取出第一个音频输入帧。
                        synchronized( m_AudioInputPt.m_AudioInputFrameLnkLstPt )
                        {
                            p_PcmAudioInputFramePt = m_AudioInputPt.m_AudioInputFrameLnkLstPt.getFirst();
                            m_AudioInputPt.m_AudioInputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输入帧链表中取出第一个音频输入帧，音频输入帧链表元素个数：" + p_TmpInt321 + "。" );

                        //将音频输入帧复制到音频结果帧，方便处理。
                        System.arraycopy( p_PcmAudioInputFramePt, 0, p_PcmAudioResultFramePt, 0, p_PcmAudioInputFramePt.length );
                    }
                    else if( ( p_TmpInt322 > 0 ) && ( m_AudioInputPt.m_AudioInputFrameLnkLstPt == null ) ) //如果音频输出帧链表有帧了，且不使用音频输入帧链表，就开始取出。
                    {
                        //从音频输出帧链表中取出第一个音频输出帧。
                        synchronized( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt )
                        {
                            p_PcmAudioOutputFramePt = m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.getFirst();
                            m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：从音频输出帧链表中取出第一个音频输出帧，音频输出帧链表元素个数：" + p_TmpInt322 + "。" );
                    }

                    //处理音频输入帧。
                    if( p_PcmAudioInputFramePt != null )
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
                                if( m_AudioInputPt.m_SpeexAecPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioOutputFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 2: //如果要使用WebRtc定点版声学回音消除器。
                            {
                                if( m_AudioInputPt.m_WebRtcAecmPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioOutputFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 3: //如果要使用WebRtc浮点版声学回音消除器。
                            {
                                if( m_AudioInputPt.m_WebRtcAecPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioOutputFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                            {
                                if( m_AudioInputPt.m_SpeexWebRtcAecPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioOutputFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用SpeexWebRtc三重声学回音消除器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
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
                                if( m_AudioInputPt.m_WebRtcNsxPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc定点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 3: //如果要使用WebRtc浮点版噪音抑制器。
                            {
                                if( m_AudioInputPt.m_WebRtcNsPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用WebRtc浮点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 4: //如果要使用RNNoise噪音抑制器。
                            {
                                if( m_AudioInputPt.m_RNNoisePt.Proc( p_PcmAudioResultFramePt, p_PcmAudioTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器成功。" );
                                    p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用RNNoise噪音抑制器失败。" );
                                }
                                break;
                            }
                        }

                        //使用Speex预处理器。
                        if( ( m_AudioInputPt.m_UseWhatNs == 1 ) || ( m_AudioInputPt.m_IsUseSpeexPprocOther != 0 ) )
                        {
                            if( m_AudioInputPt.m_SpeexPprocPt.Proc( p_PcmAudioResultFramePt, p_PcmAudioTmpFramePt, p_VoiceActStsPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器成功。语音活动状态：" + p_VoiceActStsPt.m_Val );
                                p_PcmAudioSwapFramePt = p_PcmAudioResultFramePt;p_PcmAudioResultFramePt = p_PcmAudioTmpFramePt;p_PcmAudioTmpFramePt = p_PcmAudioSwapFramePt; //交换音频结果帧和音频临时帧。
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用Speex预处理器失败。" );
                            }
                        }

                        //判断音频输入设备是否静音。在音频输入处理完后再设置静音，这样可以保证音频输入处理器的连续性。
                        if( m_AudioInputPt.m_AudioInputDeviceIsMute != 0 )
                        {
                            Arrays.fill( p_PcmAudioResultFramePt, ( short ) 0 );
                            if( ( m_AudioInputPt.m_IsUseSpeexPprocOther != 0 ) && ( m_AudioInputPt.m_SpeexPprocIsUseVad != 0 ) ) //如果Speex预处理器使用了其他功能，且使用了语音活动检测。
                            {
                                p_VoiceActStsPt.m_Val = 0;
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
                                if( m_AudioInputPt.m_SpeexEncoderPt.Proc( p_PcmAudioResultFramePt, p_EncoderAudioInputFramePt, p_EncoderAudioInputFramePt.length, p_EncoderAudioInputFrameLenPt, p_EncoderAudioInputFrameIsNeedTransPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用Speex编码器成功。Speex格式音频输入帧的数据长度：" + p_EncoderAudioInputFrameLenPt.m_Val + "，Speex格式音频输入帧是否需要传输：" + p_EncoderAudioInputFrameIsNeedTransPt.m_Val );
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
                                break out;
                            }
                        }

                        //使用音频输入Wave文件写入器写入音频输入帧数据、音频结果Wave文件写入器写入音频结果帧数据。
                        if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                        {
                            if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.WriteData( p_PcmAudioInputFramePt, p_PcmAudioInputFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输入Wave文件写入器写入音频输入帧失败。" );
                            }
                            if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.WriteData( p_PcmAudioResultFramePt, p_PcmAudioResultFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频结果Wave文件写入器写入音频结果帧失败。" );
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
                    if( p_PcmAudioOutputFramePt != null )
                    {
                        //使用音频输出Wave文件写入器写入输出帧数据。
                        if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                        {
                            if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.WriteData( p_PcmAudioOutputFramePt, p_PcmAudioOutputFramePt.length ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：使用音频输出Wave文件写入器写入音频输出帧失败。" );
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
                        ( ( p_PcmAudioInputFramePt != null ) || ( m_AudioInputPt.m_AudioInputFrameLnkLstPt == null ) ) ) //且已经处理了音频输入帧或不使用音频输入帧链表。
                    {
                        //从视频输入帧链表中取出第一个视频输入帧。
                        synchronized( m_VideoInputPt.m_VideoInputFrameLnkLstPt )
                        {
                            p_VideoInputFramePt = m_VideoInputPt.m_VideoInputFrameLnkLstPt.getFirst();
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
                    if( ( p_PcmAudioInputFramePt != null ) || ( p_VideoInputFramePt != null ) )
                    {
                        if( p_VideoInputFramePt != null )
                            p_TmpInt321 = UserReadAudioVideoInputFrame( p_PcmAudioInputFramePt, p_PcmAudioResultFramePt, p_VoiceActStsPt, p_EncoderAudioInputFramePt, p_EncoderAudioInputFrameLenPt, p_EncoderAudioInputFrameIsNeedTransPt, p_VideoInputFramePt.m_RotateYU12VideoInputFramePt, p_VideoInputFramePt.m_RotateYU12VideoInputFrameWidthPt, p_VideoInputFramePt.m_RotateYU12VideoInputFrameHeightPt, p_VideoInputFramePt.m_EncoderVideoInputFramePt, p_VideoInputFramePt.m_EncoderVideoInputFrameLenPt );
                        else
                            p_TmpInt321 = UserReadAudioVideoInputFrame( p_PcmAudioInputFramePt, p_PcmAudioResultFramePt, p_VoiceActStsPt, p_EncoderAudioInputFramePt, p_EncoderAudioInputFrameLenPt, p_EncoderAudioInputFrameIsNeedTransPt, null, null, null, null, null );
                        if( p_TmpInt321 == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数成功。返回值：" + p_TmpInt321 );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数失败。返回值：" + p_TmpInt321 );
                            break out;
                        }
                    }

                    if( m_IsPrintLogcat != 0 )
                    {
                        p_NowMsec = System.currentTimeMillis();
                        Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的读取音视频输入帧函数完毕，耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                        p_LastMsec = System.currentTimeMillis();
                    }

                    if( p_PcmAudioInputFramePt != null ) //追加本次音频输入帧到音频输入空闲帧链表。
                    {
                        synchronized( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt )
                        {
                            m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.addLast( p_PcmAudioInputFramePt );
                        }
                        p_PcmAudioInputFramePt = null; //清空PCM格式音频输入帧。
                    }
                    if( p_PcmAudioOutputFramePt != null ) //追加本次音频输出帧到音频输出空闲帧链表。
                    {
                        synchronized( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt )
                        {
                            m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.addLast( p_PcmAudioOutputFramePt );
                        }
                        p_PcmAudioOutputFramePt = null; //清空PCM格式音频输出帧。
                    }
                    if( p_VideoInputFramePt != null ) //追加本次视频输入帧到视频输入空闲帧链表。
                    {
                        synchronized( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt )
                        {
                            m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.addLast( p_VideoInputFramePt );
                        }
                        p_VideoInputFramePt = null; //清空视频输入帧。
                    }

                    if( m_ExitFlag != 0 ) //如果本线程退出标记为请求退出。
                    {
                        m_ExitCode = 0; //处理已经成功了，再将本线程退出代码设置为正常退出。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：接收到退出请求，开始准备退出。" );
                        break out;
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                } //音视频输入输出帧处理循环完毕。
            }

            m_RunFlag = RUN_FLAG_DESTROY; //设置本线程运行标记为跳出循环处理帧正在销毁。
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程开始退出。" );

            //请求音视频输入输出线程退出。必须在销毁音视频输入输出前退出，因为音视频输入输出线程会使用音视频输入输出相关类对象。
            if( m_AudioInputPt.m_AudioInputThreadPt != null ) m_AudioInputPt.m_AudioInputThreadPt.RequireExit(); //请求音频输入线程退出。
            if( m_AudioOutputPt.m_AudioOutputThreadPt != null ) m_AudioOutputPt.m_AudioOutputThreadPt.RequireExit(); //请求音频输出线程退出。
            if( m_VideoInputPt.m_VideoInputThreadPt != null ) m_VideoInputPt.m_VideoInputThreadPt.RequireExit(); //请求视频输入线程退出。
            if( m_VideoOutputPt.m_VideoOutputThreadPt != null ) m_VideoOutputPt.m_VideoOutputThreadPt.RequireExit(); //请求视频输出线程退出。

            //销毁音频输入。
            {
                //销毁音频输入线程类对象。
                if( m_AudioInputPt.m_AudioInputThreadPt != null )
                {
                    try
                    {
                        m_AudioInputPt.m_AudioInputThreadPt.join(); //等待音频输入线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_AudioInputPt.m_AudioInputThreadPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入线程类对象成功。" );
                }

                //销毁音频输入空闲帧链表类对象。
                if( m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt != null )
                {
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt.clear();
                    m_AudioInputPt.m_AudioInputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入空闲帧链表类对象成功。" );
                }

                //销毁音频输入帧链表类对象。
                if( m_AudioInputPt.m_AudioInputFrameLnkLstPt != null )
                {
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt.clear();
                    m_AudioInputPt.m_AudioInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入帧链表类对象成功。" );
                }

                //销毁音频输入设备类对象。
                if( m_AudioInputPt.m_AudioInputDevicePt != null )
                {
                    if( m_AudioInputPt.m_AudioInputDevicePt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_AudioInputPt.m_AudioInputDevicePt.stop();
                    m_AudioInputPt.m_AudioInputDevicePt.release();
                    m_AudioInputPt.m_AudioInputDevicePt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入设备类对象成功。" );
                }

                //销毁音频输入Wave文件写入器类对象、音频结果Wave文件写入器类对象。
                if( m_AudioInputPt.m_IsSaveAudioToFile != 0 )
                {
                    if( m_AudioInputPt.m_AudioInputWaveFileWriterPt != null )
                    {
                        if( m_AudioInputPt.m_AudioInputWaveFileWriterPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输入Wave文件写入器类对象失败。" );
                        }
                        m_AudioInputPt.m_AudioInputWaveFileWriterPt = null;
                    }
                    if( m_AudioInputPt.m_AudioResultWaveFileWriterPt != null )
                    {
                        if( m_AudioInputPt.m_AudioResultWaveFileWriterPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频结果Wave文件写入器类对象失败。" );
                        }
                        m_AudioInputPt.m_AudioResultWaveFileWriterPt = null;
                    }
                }

                //销毁编码器类对象。
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
                            if( m_AudioInputPt.m_SpeexEncoderPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex编码器类对象失败。" );
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

                //销毁Speex预处理器类对象。
                if( m_AudioInputPt.m_SpeexPprocPt != null )
                {
                    if( m_AudioInputPt.m_SpeexPprocPt.Destroy() == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器类对象成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex预处理器类对象失败。" );
                    }
                    m_AudioInputPt.m_SpeexPprocPt = null;
                }

                //销毁噪音抑制器类对象。
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
                            if( m_AudioInputPt.m_WebRtcNsxPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版噪音抑制器类对象失败。" );
                            }
                            m_AudioInputPt.m_WebRtcNsxPt = null;
                        }
                        break;
                    }
                    case 3: //如果要使用WebRtc浮点版噪音抑制器类对象。
                    {
                        if( m_AudioInputPt.m_WebRtcNsPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcNsPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版噪音抑制器类对象失败。" );
                            }
                            m_AudioInputPt.m_WebRtcNsPt = null;
                        }
                        break;
                    }
                    case 4: //如果要使用RNNoise噪音抑制器类对象。
                    {
                        if( m_AudioInputPt.m_RNNoisePt != null )
                        {
                            if( m_AudioInputPt.m_RNNoisePt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁RNNoise噪音抑制器类对象失败。" );
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
                                if( m_AudioInputPt.m_SpeexAecPt.SaveMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_SpeexAecFilterLen, m_AudioInputPt.m_SpeexAecIsUseRec, m_AudioInputPt.m_SpeexAecEchoMultiple, m_AudioInputPt.m_SpeexAecEchoCont, m_AudioInputPt.m_SpeexAecEchoSupes, m_AudioInputPt.m_SpeexAecEchoSupesAct, m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 成功。" );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_SpeexAecMemFileFullPathStrPt + " 失败。" );
                                }
                            }
                            if( m_AudioInputPt.m_SpeexAecPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex声学回音消除器类对象失败。" );
                            }
                            m_AudioInputPt.m_SpeexAecPt = null;
                        }
                        break;
                    }
                    case 2: //如果要使用WebRtc定点版声学回音消除器。
                    {
                        if( m_AudioInputPt.m_WebRtcAecmPt != null )
                        {
                            if( m_AudioInputPt.m_WebRtcAecmPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc定点版声学回音消除器类对象失败。" );
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
                                if( m_AudioInputPt.m_WebRtcAecPt.SaveMemFile( m_AudioInputPt.m_SamplingRate, m_AudioInputPt.m_FrameLen, m_AudioInputPt.m_WebRtcAecEchoMode, m_AudioInputPt.m_WebRtcAecDelay, m_AudioInputPt.m_WebRtcAecIsUseDelayAgnosticMode, m_AudioInputPt.m_WebRtcAecIsUseExtdFilterMode, m_AudioInputPt.m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_AudioInputPt.m_WebRtcAecIsUseAdaptAdjDelay, m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt, null ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 成功。" );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_AudioInputPt.m_WebRtcAecMemFileFullPathStrPt + " 失败。" );
                                }
                            }
                            if( m_AudioInputPt.m_WebRtcAecPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁WebRtc浮点版声学回音消除器类对象失败。" );
                            }
                            m_AudioInputPt.m_WebRtcAecPt = null;
                        }
                        break;
                    }
                    case 4: //如果要使用SpeexWebRtc三重声学回音消除器。
                    {
                        if( m_AudioInputPt.m_SpeexWebRtcAecPt != null )
                        {
                            if( m_AudioInputPt.m_SpeexWebRtcAecPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁SpeexWebRtc三重声学回音消除器类对象失败。" );
                            }
                            m_AudioInputPt.m_SpeexWebRtcAecPt = null;
                        }
                        break;
                    }
                }
            } //销毁音频输入完毕。

            //销毁音频输出。
            {
                //销毁音频输出线程类对象。
                if( m_AudioOutputPt.m_AudioOutputThreadPt != null )
                {
                    try
                    {
                        m_AudioOutputPt.m_AudioOutputThreadPt.join(); //等待音频输出线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_AudioOutputPt.m_AudioOutputThreadPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出线程类对象成功。" );
                }

                //销毁音频输出空闲帧链表类对象。
                if( m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt != null )
                {
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt.clear();
                    m_AudioOutputPt.m_AudioOutputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出空闲帧链表类对象成功。" );
                }

                //销毁音频输出帧链表类对象。
                if( m_AudioOutputPt.m_AudioOutputFrameLnkLstPt != null )
                {
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt.clear();
                    m_AudioOutputPt.m_AudioOutputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出帧链表类对象成功。" );
                }

                //销毁音频输出设备类对象。
                if( m_AudioOutputPt.m_AudioOutputDevicePt != null )
                {
                    if( m_AudioOutputPt.m_AudioOutputDevicePt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_AudioOutputPt.m_AudioOutputDevicePt.stop();
                    m_AudioOutputPt.m_AudioOutputDevicePt.release();
                    m_AudioOutputPt.m_AudioOutputDevicePt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出设备类对象成功。" );
                }

                //销毁音频输出Wave文件写入器类对象。
                if( m_AudioOutputPt.m_IsSaveAudioToFile != 0 )
                {
                    if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt != null )
                    {
                        if( m_AudioOutputPt.m_AudioOutputWaveFileWriterPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁音频输出Wave文件写入器类对象失败。" );
                        }
                        m_AudioOutputPt.m_AudioOutputWaveFileWriterPt = null;
                    }
                }

                //销毁解码器类对象。
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
                            if( m_AudioOutputPt.m_SpeexDecoderPt.Destroy() == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁Speex解码器类对象失败。" );
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
                //销毁视频输入线程类对象。
                if( m_VideoInputPt.m_VideoInputThreadPt != null )
                {
                    try
                    {
                        m_VideoInputPt.m_VideoInputThreadPt.join(); //等待视频输入线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_VideoInputPt.m_VideoInputThreadPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程类对象成功。" );
                }

                //销毁视频输入设备类对象。
                if( m_VideoInputPt.m_VideoInputDevicePt != null )
                {
                    m_VideoInputPt.m_VideoInputDevicePt.setPreviewCallback( null ); //设置预览回调函数为空，防止出现java.lang.RuntimeException: Method called after release()异常。
                    m_VideoInputPt.m_VideoInputDevicePt.stopPreview(); //停止预览。
                    m_VideoInputPt.m_VideoInputDevicePt.release(); //销毁摄像头。
                    m_VideoInputPt.m_VideoInputDevicePt = null;
                    m_VideoInputPt.m_VideoPreviewCallbackBufferPtPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入设备类对象成功。" );
                }

                //销毁视频输入空闲帧链表类对象。
                if( m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt.clear();
                    m_VideoInputPt.m_VideoInputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入空闲帧链表类对象成功。" );
                }

                //销毁视频输入帧链表类对象。
                if( m_VideoInputPt.m_VideoInputFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt.clear();
                    m_VideoInputPt.m_VideoInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输入帧链表类对象成功。" );
                }

                //销毁NV21格式视频输入帧链表类对象。
                if( m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt != null )
                {
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt.clear();
                    m_VideoInputPt.m_NV21VideoInputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁NV21格式视频输入帧链表类对象成功。" );
                }

                //销毁编码器类对象。
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
                            if( m_VideoInputPt.m_OpenH264EncoderPt.Destroy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器类对象失败。" );
                            }
                            m_VideoInputPt.m_OpenH264EncoderPt = null;
                        }
                        break;
                    }
                }
            } //销毁视频输入完毕。

            //销毁视频输出。
            {
                //销毁音频输出线程类对象。
                if( m_VideoOutputPt.m_VideoOutputThreadPt != null )
                {
                    try
                    {
                        m_VideoOutputPt.m_VideoOutputThreadPt.join(); //等待视频输出线程退出。
                    }
                    catch( InterruptedException ignored )
                    {
                    }
                    m_VideoOutputPt.m_VideoOutputThreadPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出线程类对象成功。" );
                }

                //销毁视频输出空闲帧链表类对象。
                if( m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt != null )
                {
                    m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt.clear();
                    m_VideoOutputPt.m_VideoOutputIdleFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出空闲帧链表类对象成功。" );
                }

                //销毁视频输出帧链表类对象。
                if( m_VideoOutputPt.m_VideoOutputFrameLnkLstPt != null )
                {
                    m_VideoOutputPt.m_VideoOutputFrameLnkLstPt.clear();
                    m_VideoOutputPt.m_VideoOutputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁视频输出帧链表类对象成功。" );
                }

                //销毁解码器类对象。
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
                            if( m_VideoOutputPt.m_OpenH264DecoderPt.Destroy( null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器类对象成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264解码器类对象失败。" );
                            }
                            m_VideoOutputPt.m_OpenH264DecoderPt = null;
                        }
                        break;
                    }
                }
            } //销毁视频输出完毕。

            //销毁PCM格式音频输入帧、PCM格式音频输出帧、PCM格式音频结果帧、PCM格式音频临时帧、PCM格式音频交换帧、语音活动状态、已编码格式音频输入帧、已编码格式音频输入帧的数据长度、已编码格式音频输入帧是否需要传输、视频输入帧。
            {
                p_PcmAudioInputFramePt = null;
                p_PcmAudioOutputFramePt = null;
                p_PcmAudioResultFramePt = null;
                p_PcmAudioTmpFramePt = null;
                p_PcmAudioSwapFramePt = null;
                p_VoiceActStsPt = null;
                p_EncoderAudioInputFramePt = null;
                p_EncoderAudioInputFrameLenPt = null;
                p_EncoderAudioInputFrameIsNeedTransPt = null;
                p_VideoInputFramePt = null;

                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：销毁PCM格式音频输入帧、PCM格式音频输出帧、PCM格式音频结果帧、PCM格式音频临时帧、PCM格式音频交换帧、语音活动状态、已编码格式音频输入帧、已编码格式音频输入帧的数据长度、已编码格式音频输入帧是否需要传输、视频输入帧。" );
            }

            //销毁唤醒锁。
            WakeLockInitOrDestroy( 0 );

            if( m_ExitFlag != 3 ) //如果需要调用用户定义的销毁函数。
            {
                UserDestroy(); //调用用户定义的销毁函数。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：调用用户定义的销毁函数成功。" );
            }

            m_RunFlag = RUN_FLAG_END; //设置本线程运行标记为销毁完毕。

            if( ( m_ExitFlag == 0 ) || ( m_ExitFlag == 1 ) ) //如果用户需要直接退出。
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程已退出。" );
                break ReInit;
            }
            else //如果用户需要重新初始化。
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "媒体处理线程：本线程重新初始化。" );
            }
        }
    }
}