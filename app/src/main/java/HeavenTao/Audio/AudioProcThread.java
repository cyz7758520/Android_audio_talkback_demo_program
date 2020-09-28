package HeavenTao.Audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import HeavenTao.Data.*;

//音频处理线程类。
public abstract class AudioProcThread extends Thread
{
    public String m_CurClsNameStrPt = this.getClass().getSimpleName(); //存放当前类名称字符串。

    public int m_ExitFlag = 0; //存放本线程退出标记，为0表示保持运行，为1表示请求退出，为2表示请求重启，为3表示请求重启但不执行用户定义的UserInit初始化函数和UserDestroy销毁函数。
    public int m_ExitCode = 0; //存放本线程退出代码，为0表示正常退出，为-1表示初始化失败，为-2表示处理失败。

    public static Context m_AppContextPt; //存放应用程序上下文类对象的内存指针。
    public int m_SamplingRate = 16000; //存放采样频率，取值只能为8000、16000、32000。
    public int m_FrameLen = 320; //存放帧的数据长度，单位采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960。

    int m_IsSaveSettingToFile = 0; //存放是否保存设置到文件，为非0表示要保存，为0表示不保存。
    String m_SettingFileFullPathStrPt; //存放设置文件的完整路径字符串。

    public int m_IsPrintLogcat = 0; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。

    public LinkedList< short[] > m_InputFrameLnkLstPt; //存放输入帧链表类对象的内存指针。
    public LinkedList< short[] > m_OutputFrameLnkLstPt; //存放输出帧链表类对象的内存指针。

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

    public int m_UseWhatCodec = 0; //存放使用什么编解码器，为0表示PCM原始数据，为1表示Speex编解码器，为2表示Opus编解码器。

    SpeexEncoder m_SpeexEncoderPt; //存放Speex编码器类对象的内存指针。
    SpeexDecoder m_SpeexDecoderPt; //存放Speex解码器类对象的内存指针。
    int m_SpeexCodecEncoderUseCbrOrVbr; //存放Speex编码器使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
    int m_SpeexCodecEncoderQuality; //存放Speex编码器的编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
    int m_SpeexCodecEncoderComplexity; //存放Speex编码器的编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
    int m_SpeexCodecEncoderPlcExpectedLossRate; //存放Speex编码器在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。
    int m_SpeexCodecDecoderIsUsePerceptualEnhancement; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

    public int m_IsSaveAudioToFile = 0; //存放是否保存音频到文件，为非0表示要保存，为0表示不保存。
    WaveFileWriter m_AudioInputWaveFileWriterPt; //存放音频输入Wave文件写入器对象的内存指针。
    WaveFileWriter m_AudioOutputWaveFileWriterPt; //存放音频输出Wave文件写入器对象的内存指针。
    WaveFileWriter m_AudioResultWaveFileWriterPt; //存放音频结果Wave文件写入器对象的内存指针。
    String m_AudioInputFileFullPathStrPt; //存放音频输入文件的完整路径字符串。
    String m_AudioOutputFileFullPathStrPt; //存放音频输出文件的完整路径字符串。
    String m_AudioResultFileFullPathStrPt; //存放音频结果文件的完整路径字符串。

    AudioRecord m_AudioRecordPt; //存放音频输入类对象的内存指针。
    int m_AudioRecordBufSz; //存放音频输入类对象的缓冲区大小，单位字节。
    public int m_UseWhatAudioOutputDevice = 0; //存放使用什么音频输出设备，为0表示扬声器，为非0表示听筒。
    public int m_UseWhatAudioOutputStreamType = 0; //存放使用什么音频输出流类型，为0表示通话类型，为非0表示媒体类型。
    AudioTrack m_AudioTrackPt; //存放音频输出类对象的内存指针。
    int m_AudioTrackBufSz; //存放音频输出类对象的缓冲区大小，单位字节。

    AudioInputThread m_AudioInputThreadPt; //存放音频输入线程类对象的内存指针。
    AudioOutputThread m_AudioOutputThreadPt; //存放音频输出线程类对象的内存指针。

    //用户定义的相关函数。
    public abstract int UserInit(); //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract int UserProcess(); //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract void UserDestroy(); //用户定义的销毁函数，在本线程退出时调用一次。

    public abstract int UserReadInputFrame( short PcmInputFramePt[], short PcmResultFramePt[], int VoiceActSts, byte SpeexInputFramePt[], HTLong SpeexInputFrameLen, HTInt SpeexInputFrameIsNeedTrans ); //用户定义的读取输入帧函数，在读取到一个输入帧并处理完后回调一次，为0表示成功，为非0表示失败。

    public abstract void UserWriteOutputFrame( short PcmOutputFramePt[], byte SpeexOutputFramePt[], HTLong SpeexOutputFrameLenPt ); //用户定义的写入输出帧函数，在需要写入一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。

    public abstract void UserGetPcmOutputFrame( short PcmOutputFramePt[] ); //用户定义的获取PCM格式输出帧函数，在解码完一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。

    //初始化音频处理线程类对象。
    public int Init( Context AppContextPt, int SamplingRate, int FrameLenMsec )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //判断各个变量是否正确。
            if( ( AppContextPt == null ) || //如果上下文类对象不正确。
                ( ( SamplingRate != 8000 ) && ( SamplingRate != 16000 ) && ( SamplingRate != 32000 ) ) || //如果采样频率不正确。
                ( ( FrameLenMsec == 0 ) || ( FrameLenMsec % 10 != 0 ) ) ) //如果帧的毫秒长度不正确。
            {
                break out;
            }

            m_AppContextPt = AppContextPt; //设置应用程序上下文类对象的内存指针。
            m_SamplingRate = SamplingRate; //设置采样频率。
            m_FrameLen = FrameLenMsec * SamplingRate / 1000; //设置帧的数据长度。

            p_Result = 0;
        }

        return p_Result;
    }

    //设置保存设置到文件。
    public void SetSaveSettingToFile( int IsSaveSettingToFile, String SettingFileFullPathStrPt )
    {
        m_IsSaveSettingToFile = IsSaveSettingToFile;
        m_SettingFileFullPathStrPt = SettingFileFullPathStrPt;
    }

    //设置打印日志。
    public void SetPrintLogcat( int IsPrintLogcat )
    {
        m_IsPrintLogcat = IsPrintLogcat;
    }

    //设置使用的音频输出设备。
    public int SetUseDevice( int UseSpeakerOrEarpiece, int UseVoiceCallOrMusic )
    {
        int p_Result = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        out:
        {
            //判断各个变量是否正确。
            if( ( UseSpeakerOrEarpiece != 0 ) && ( UseVoiceCallOrMusic != 0 ) ) //如果使用听筒时不能使用媒体类型音频输出流。
            {
                break out;
            }

            m_UseWhatAudioOutputDevice = UseSpeakerOrEarpiece; //设置音频输出设备。
            m_UseWhatAudioOutputStreamType = UseVoiceCallOrMusic; //设置音频输出流类型。

            p_Result = 0;
        }

        return p_Result;
    }

    //设置使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器（系统不一定自带）。
    public void SetUseSystemAecNsAgc( int IsUseSystemAecNsAgc )
    {
        m_IsUseSystemAecNsAgc = IsUseSystemAecNsAgc;
    }

    //设置不使用声学回音消除器。
    public void SetUseNoAec()
    {
        m_UseWhatAec = 0;
    }

    //设置使用Speex声学回音消除器。
    public void SetUseSpeexAec( int FilterLen, int IsUseRec, float EchoMultiple, float EchoCont, int EchoSupes, int EchoSupesActive, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        m_UseWhatAec = 1;
        m_SpeexAecFilterLen = FilterLen;
        m_SpeexAecIsUseRec = IsUseRec;
        m_SpeexAecEchoMultiple = EchoMultiple;
        m_SpeexAecEchoCont = EchoCont;
        m_SpeexAecEchoSupes = EchoSupes;
        m_SpeexAecEchoSupesAct = EchoSupesActive;
        m_SpeexAecIsSaveMemFile = IsSaveMemFile;
        m_SpeexAecMemFileFullPathStrPt = MemFileFullPathStrPt;
    }

    //设置使用WebRtc定点版声学回音消除器。
    public void SetUseWebRtcAecm( int IsUseCNGMode, int EchoMode, int Delay )
    {
        m_UseWhatAec = 2;
        m_WebRtcAecmIsUseCNGMode = IsUseCNGMode;
        m_WebRtcAecmEchoMode = EchoMode;
        m_WebRtcAecmDelay = Delay;
    }

    //设置使用WebRtc浮点版声学回音消除器。
    public void SetUseWebRtcAec( int EchoMode, int Delay, int IsUseDelayAgnosticMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, int IsSaveMemFile, String MemFileFullPathStrPt )
    {
        m_UseWhatAec = 3;
        m_WebRtcAecEchoMode = EchoMode;
        m_WebRtcAecDelay = Delay;
        m_WebRtcAecIsUseDelayAgnosticMode = IsUseDelayAgnosticMode;
        m_WebRtcAecIsUseExtdFilterMode = IsUseExtdFilterMode;
        m_WebRtcAecIsUseRefinedFilterAdaptAecMode = IsUseRefinedFilterAdaptAecMode;
        m_WebRtcAecIsUseAdaptAdjDelay = IsUseAdaptAdjDelay;
        m_WebRtcAecIsSaveMemFile = IsSaveMemFile;
        m_WebRtcAecMemFileFullPathStrPt = MemFileFullPathStrPt;
    }

    //设置使用SpeexWebRtc三重声学回音消除器。
    public void SetUseSpeexWebRtcAec( int WorkMode, int SpeexAecFilterLen, int SpeexAecIsUseRec, float SpeexAecEchoMultiple, float SpeexAecEchoCont, int SpeexAecEchoSuppress, int SpeexAecEchoSuppressActive, int WebRtcAecmIsUseCNGMode, int WebRtcAecmEchoMode, int WebRtcAecmDelay, int WebRtcAecEchoMode, int WebRtcAecDelay, int WebRtcAecIsUseDelayAgnosticMode, int WebRtcAecIsUseExtdFilterMode, int WebRtcAecIsUseRefinedFilterAdaptAecMode, int WebRtcAecIsUseAdaptAdjDelay )
    {
        m_UseWhatAec = 4;
        m_SpeexWebRtcAecWorkMode = WorkMode;
        m_SpeexWebRtcAecSpeexAecFilterLen = SpeexAecFilterLen;
        m_SpeexWebRtcAecSpeexAecIsUseRec = SpeexAecIsUseRec;
        m_SpeexWebRtcAecSpeexAecEchoMultiple = SpeexAecEchoMultiple;
        m_SpeexWebRtcAecSpeexAecEchoCont = SpeexAecEchoCont;
        m_SpeexWebRtcAecSpeexAecEchoSupes = SpeexAecEchoSuppress;
        m_SpeexWebRtcAecSpeexAecEchoSupesAct = SpeexAecEchoSuppressActive;
        m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode = WebRtcAecmIsUseCNGMode;
        m_SpeexWebRtcAecWebRtcAecmEchoMode = WebRtcAecmEchoMode;
        m_SpeexWebRtcAecWebRtcAecmDelay = WebRtcAecmDelay;
        m_SpeexWebRtcAecWebRtcAecEchoMode = WebRtcAecEchoMode;
        m_SpeexWebRtcAecWebRtcAecDelay = WebRtcAecDelay;
        m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode = WebRtcAecIsUseDelayAgnosticMode;
        m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode = WebRtcAecIsUseExtdFilterMode;
        m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode = WebRtcAecIsUseRefinedFilterAdaptAecMode;
        m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay = WebRtcAecIsUseAdaptAdjDelay;
    }

    //设置不使用噪音抑制器。
    public void SetUseNoNs()
    {
        m_UseWhatNs = 0;
    }

    //设置使用Speex预处理器的噪音抑制。
    public void SetUseSpeexPprocNs( int IsUseNs, int NoiseSupes, int IsUseDereverberation )
    {
        m_UseWhatNs = 1;
        m_SpeexPprocIsUseNs = IsUseNs;
        m_SpeexPprocNoiseSupes = NoiseSupes;
        m_SpeexPprocIsUseDereverb = IsUseDereverberation;
    }

    //设置使用WebRtc定点版噪音抑制器。
    public void SetUseWebRtcNsx( int PolicyMode )
    {
        m_UseWhatNs = 2;
        m_WebRtcNsxPolicyMode = PolicyMode;
    }

    //设置使用WebRtc定点版噪音抑制器。
    public void SetUseWebRtcNs( int PolicyMode )
    {
        m_UseWhatNs = 3;
        m_WebRtcNsPolicyMode = PolicyMode;
    }

    //设置使用RNNoise噪音抑制器。
    public void SetUseRNNoise()
    {
        m_UseWhatNs = 4;
    }

    //设置Speex预处理器的其他功能。
    public void SetSpeexPprocOther( int IsUseOther, int IsUseVad, int VadProbStart, int VadProbCont, int IsUseAgc, int AgcLevel, int AgcIncrement, int AgcDecrement, int AgcMaxGain )
    {
        m_IsUseSpeexPprocOther = IsUseOther;
        m_SpeexPprocIsUseVad = IsUseVad;
        m_SpeexPprocVadProbStart = VadProbStart;
        m_SpeexPprocVadProbCont = VadProbCont;
        m_SpeexPprocIsUseAgc = IsUseAgc;
        m_SpeexPprocAgcIncrement = AgcIncrement;
        m_SpeexPprocAgcDecrement = AgcDecrement;
        m_SpeexPprocAgcLevel = AgcLevel;
        m_SpeexPprocAgcMaxGain = AgcMaxGain;
    }

    //设置使用PCM原始数据。
    public void SetUsePcm()
    {
        m_UseWhatCodec = 0;
    }

    //设置使用Speex编解码器。
    public void SetUseSpeexCodec( int EncoderUseCbrOrVbr, int EncoderQuality, int EncoderComplexity, int EncoderPlcExpectedLossRate, int DecoderIsUsePerceptualEnhancement )
    {
        m_UseWhatCodec = 1;
        m_SpeexCodecEncoderUseCbrOrVbr = EncoderUseCbrOrVbr;
        m_SpeexCodecEncoderQuality = EncoderQuality;
        m_SpeexCodecEncoderComplexity = EncoderComplexity;
        m_SpeexCodecEncoderPlcExpectedLossRate = EncoderPlcExpectedLossRate;
        m_SpeexCodecDecoderIsUsePerceptualEnhancement = DecoderIsUsePerceptualEnhancement;
    }

    //设置使用Opus编解码器。
    public void SetUseOpusCodec()
    {
        m_UseWhatCodec = 2;
    }

    //设置保存音频到文件。
    public void SetSaveAudioToFile( int IsSaveAudioToFile, String AudioInputFileFullPathStrPt, String AudioOutputFileFullPathStrPt, String AudioResultFileFullPathStrPt )
    {
        m_IsSaveAudioToFile = IsSaveAudioToFile;
        m_AudioInputFileFullPathStrPt = AudioInputFileFullPathStrPt;
        m_AudioOutputFileFullPathStrPt = AudioOutputFileFullPathStrPt;
        m_AudioResultFileFullPathStrPt = AudioResultFileFullPathStrPt;
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

            m_ExitFlag = ExitFlag;

            if( IsBlockWait != 0 )
            {
                if( ExitFlag == 1 ) //如果是请求退出。
                {
                    try
                    {
                        this.join();
                    }
                    catch( InterruptedException e )
                    {

                    }
                }
                else //如果是请求重启。
                {
                    //等待重启完毕。
                    do
                    {
                        SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                    }
                    while( m_ExitFlag != 0 );
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

            short p_TmpInputFramePt[];
            long p_LastMsec = 0;
            long p_NowMsec = 0;

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：开始准备音频输入。" );

            //计算声学回音的延迟，并自适应设置到WebRtc定点版和浮点版声学回音消除器。
            {
                int p_Delay = 0; //存放声学回音的延迟，单位毫秒。
                HTInt p_HTIntDelay = new HTInt();

                //计算音频输出的延迟。
                m_AudioTrackPt.play(); //让音频输出类对象开始播放。
                p_TmpInputFramePt = new short[m_FrameLen]; //创建一个空输出帧。
                p_LastMsec = System.currentTimeMillis();
                skip:
                while( true )
                {
                    m_AudioTrackPt.write( p_TmpInputFramePt, 0, p_TmpInputFramePt.length ); //播放一个空输出帧。
                    p_NowMsec = System.currentTimeMillis();
                    p_Delay += m_FrameLen; //递增音频输出的延迟。
                    if( p_NowMsec - p_LastMsec >= 10 ) //如果播放耗时较长，就表示音频输出类对象的缓冲区已经写满，结束计算。
                    {
                        break skip;
                    }
                    p_LastMsec = p_NowMsec;
                }
                p_Delay = p_Delay * 1000 / m_SamplingRate; //将音频输出的延迟转换为毫秒。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输出延迟：" + p_Delay + " 毫秒。" );

                //计算音频输入的延迟。
                m_AudioRecordPt.startRecording(); //让音频输入类对象开始录音。
                p_TmpInputFramePt = new short[m_FrameLen];
                p_LastMsec = System.currentTimeMillis();
                m_AudioRecordPt.read( p_TmpInputFramePt, 0, p_TmpInputFramePt.length );
                p_NowMsec = System.currentTimeMillis();
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "音频输入延迟：" + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );

                //计算声学回音的延迟。
                p_Delay = p_Delay + ( int ) ( p_NowMsec - p_LastMsec );
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：" + "声学回音延迟：" + p_Delay + " 毫秒，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

                m_AudioOutputThreadPt.start(); //启动音频输出线程。

                //自适应设置到WebRtc定点版和浮点版声学回音消除器。
                if( ( m_WebRtcAecmPt != null ) && ( m_WebRtcAecmPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果使用了WebRtc定点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    m_WebRtcAecmPt.SetDelay( p_Delay / 2 );
                    m_WebRtcAecmPt.GetDelay( p_HTIntDelay );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_WebRtcAecPt != null ) && ( m_WebRtcAecPt.GetDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果使用了WebRtc浮点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    if( m_WebRtcAecIsUseDelayAgnosticMode == 0 ) //如果WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_WebRtcAecPt.SetDelay( p_Delay );
                        m_WebRtcAecPt.GetDelay( p_HTIntDelay );
                    }
                    else //如果WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_WebRtcAecPt.SetDelay( 20 );
                        m_WebRtcAecPt.GetDelay( p_HTIntDelay );
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_SpeexWebRtcAecPt != null ) && ( m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果使用了SpeexWebRtc三重声学回音消除器，且WebRtc定点版声学回音消除器需要自适应设置回音的延迟。
                {
                    m_SpeexWebRtcAecPt.SetWebRtcAecmDelay( p_Delay / 2 );
                    m_SpeexWebRtcAecPt.GetWebRtcAecmDelay( p_HTIntDelay );
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }
                if( ( m_SpeexWebRtcAecPt != null ) && ( m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay ) == 0 ) && ( p_HTIntDelay.m_Val == 0 ) ) //如果使用了SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
                {
                    if( m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_SpeexWebRtcAecPt.SetWebRtcAecDelay( p_Delay );
                        m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_SpeexWebRtcAecPt.SetWebRtcAecDelay( 20 );
                        m_SpeexWebRtcAecPt.GetWebRtcAecDelay( p_HTIntDelay );
                    }
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：自适应设置SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟为 " + p_HTIntDelay.m_Val + " 毫秒。" );
                }

                p_LastMsec = p_NowMsec;
            }

            //开始音频输入循环。
            out:
            while( true )
            {
                p_TmpInputFramePt = new short[m_FrameLen];

                //读取本次输入帧。
                m_AudioRecordPt.read( p_TmpInputFramePt, 0, p_TmpInputFramePt.length );

                if( m_IsPrintLogcat != 0 )
                {
                    p_NowMsec = System.currentTimeMillis();
                    Log.i( m_CurClsNameStrPt, "音频输入线程：读取耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，" + "输入帧链表元素个数：" + m_InputFrameLnkLstPt.size() + "。" );
                    p_LastMsec = p_NowMsec;
                }

                //追加本次输入帧到输入帧链表。
                synchronized( m_InputFrameLnkLstPt )
                {
                    m_InputFrameLnkLstPt.addLast( p_TmpInputFramePt );
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            }

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

            short p_TmpOutputFramePt[];
            long p_LastMsec = 0;
            long p_NowMsec = 0;

            if( m_IsPrintLogcat != 0 )
            {
                p_LastMsec = System.currentTimeMillis();
                Log.i( m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );
            }

            //开始音频输出循环。
            out:
            while( true )
            {
                p_TmpOutputFramePt = new short[m_FrameLen];

                //调用用户定义的写入输出帧函数，并解码成PCM原始数据。
                switch( m_UseWhatCodec ) //使用什么编解码器。
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        //调用用户定义的写入输出帧函数。
                        UserWriteOutputFrame( p_TmpOutputFramePt, null, null );
                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        byte p_SpeexOutputFramePt[] = new byte[m_FrameLen]; //Speex格式输出帧。
                        HTLong p_SpeexOutputFrameLenPt = new HTLong(); //Speex格式输出帧的数据长度，单位字节。

                        //调用用户定义的写入输出帧函数。
                        UserWriteOutputFrame( null, p_SpeexOutputFramePt, p_SpeexOutputFrameLenPt );

                        //使用Speex解码器。
                        if( m_SpeexDecoderPt.Proc( ( p_SpeexOutputFrameLenPt.m_Val != 0 ) ? p_SpeexOutputFramePt : null, p_SpeexOutputFrameLenPt.m_Val, p_TmpOutputFramePt ) == 0 ) //如果本次Speex格式输出帧接收到了或丢失了。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：使用Speex解码器成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：使用Speex解码器失败。" );
                        }
                        break;
                    }
                    case 2: //如果使用Opus编解码器。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出线程：暂不支持使用Opus解码器。" );
                    }
                }

                //写入本次输出帧。
                m_AudioTrackPt.write( p_TmpOutputFramePt, 0, p_TmpOutputFramePt.length );

                //调用用户定义的获取PCM格式输出帧函数。
                UserGetPcmOutputFrame( p_TmpOutputFramePt );

                if( m_IsPrintLogcat != 0 )
                {
                    p_NowMsec = System.currentTimeMillis();
                    Log.i( m_CurClsNameStrPt, "音频输出线程：写入耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒，输出帧链表元素个数：" + m_OutputFrameLnkLstPt.size() + "。" );
                    p_LastMsec = p_NowMsec;
                }

                //追加本次输出帧到输出帧链表。
                synchronized( m_OutputFrameLnkLstPt )
                {
                    m_OutputFrameLnkLstPt.addLast( p_TmpOutputFramePt );
                }

                if( m_ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            }

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
        }
    }

    //本线程执行函数。
    public void run()
    {
        this.setPriority( this.MAX_PRIORITY ); //设置本线程优先级。
        Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

        int p_TmpInt32 = 0;
        long p_LastMsec = 0;
        long p_NowMsec = 0;

        short p_PcmInputFramePt[] = null; //PCM格式输入帧。
        short p_PcmOutputFramePt[] = null; //PCM格式输出帧。
        short p_PcmResultFramePt[] = null; //PCM格式结果帧。
        short p_PcmTmpFramePt[] = null; //PCM格式临时帧。
        short p_PcmSwapFramePt[] = null; //PCM格式交换帧。
        HTInt p_VoiceActStsPt = null; //语音活动状态，为1表示有语音活动，为0表示无语音活动。
        byte p_SpeexInputFramePt[] = null; //Speex格式输入帧。
        HTLong p_SpeexInputFrameLenPt = null; //Speex格式输入帧的数据长度，单位字节。
        HTInt p_SpeexInputFrameIsNeedTransPt = null; //Speex格式输入帧是否需要传输，为1表示需要传输，为0表示不需要传输。

        ReInit:
        while( true )
        {
            out:
            {
                if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：本地代码的指令集名称（CPU类型+ ABI约定）为" + android.os.Build.CPU_ABI + "。手机型号为" + android.os.Build.MODEL + "。" );

                if( m_ExitFlag != 3 ) //如果需要执行用户定义的初始化函数。
                {
                    m_ExitFlag = 0; //设置本线程退出标记为保持运行。
                    m_ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。

                    //调用用户定义的初始化函数。
                    p_TmpInt32 = UserInit();
                    if( p_TmpInt32 == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：调用用户定义的初始化函数成功。返回值：" + p_TmpInt32 );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：调用用户定义的初始化函数失败。返回值：" + p_TmpInt32 );
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
                        p_SettingFileWriterPt.write(
                            "m_AppContextPt：" + m_AppContextPt +
                                    "\nm_SamplingRate：" + m_SamplingRate +
                                    "\nm_FrameLen：" +  m_FrameLen +
                                    "\n" +
                                    "\nm_IsSaveSettingToFile：" + m_IsSaveSettingToFile +
                                    "\nm_SettingFileFullPathStrPt：" + m_SettingFileFullPathStrPt +
                                    "\n" +
                                    "\nm_IsPrintLogcat：" + m_IsPrintLogcat +
                                    "\n" +
                                    "\nm_IsUseSystemAecNsAgc：" + m_IsUseSystemAecNsAgc +
                                    "\n" +
                                    "\nm_UseWhatAec：" + m_UseWhatAec +
                                    "\n" +
                                    "\nm_SpeexAecFilterLen：" + m_SpeexAecFilterLen +
                                    "\nm_SpeexAecIsUseRec：" + m_SpeexAecIsUseRec +
                                    "\nm_SpeexAecEchoMultiple：" + m_SpeexAecEchoMultiple +
                                    "\nm_SpeexAecEchoCont：" + m_SpeexAecEchoCont +
                                    "\nm_SpeexAecEchoSupes：" + m_SpeexAecEchoSupes +
                                    "\nm_SpeexAecEchoSupesAct：" + m_SpeexAecEchoSupesAct +
                                    "\nm_SpeexAecIsSaveMemFile：" + m_SpeexAecIsSaveMemFile +
                                    "\nm_SpeexAecMemFileFullPathStrPt：" + m_SpeexAecMemFileFullPathStrPt +
                                    "\n" +
                                    "\nm_WebRtcAecmIsUseCNGMode：" + m_WebRtcAecmIsUseCNGMode +
                                    "\nm_WebRtcAecmEchoMode：" + m_WebRtcAecmEchoMode +
                                    "\nm_WebRtcAecmDelay：" + m_WebRtcAecmDelay +
                                    "\n" +
                                    "\nm_WebRtcAecEchoMode：" + m_WebRtcAecEchoMode +
                                    "\nm_WebRtcAecDelay：" + m_WebRtcAecDelay +
                                    "\nm_WebRtcAecIsUseDelayAgnosticMode：" + m_WebRtcAecIsUseDelayAgnosticMode +
                                    "\nm_WebRtcAecIsUseExtdFilterMode：" + m_WebRtcAecIsUseExtdFilterMode +
                                    "\nm_WebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_WebRtcAecIsUseRefinedFilterAdaptAecMode +
                                    "\nm_WebRtcAecIsUseAdaptAdjDelay：" + m_WebRtcAecIsUseAdaptAdjDelay +
                                    "\nm_WebRtcAecIsSaveMemFile：" + m_WebRtcAecIsSaveMemFile +
                                    "\nm_WebRtcAecMemFileFullPathStrPt：" + m_WebRtcAecMemFileFullPathStrPt +
                                    "\n" +
                                    "\nm_SpeexWebRtcAecWorkMode：" + m_SpeexWebRtcAecWorkMode +
                                    "\nm_SpeexWebRtcAecSpeexAecFilterLen：" + m_SpeexWebRtcAecSpeexAecFilterLen +
                                    "\nm_SpeexWebRtcAecSpeexAecEchoMultiple：" + m_SpeexWebRtcAecSpeexAecEchoMultiple +
                                    "\nm_SpeexWebRtcAecSpeexAecEchoCont：" + m_SpeexWebRtcAecSpeexAecEchoCont +
                                    "\nm_SpeexWebRtcAecSpeexAecEchoSupes：" + m_SpeexWebRtcAecSpeexAecEchoSupes +
                                    "\nm_SpeexWebRtcAecSpeexAecEchoSupesAct：" + m_SpeexWebRtcAecSpeexAecEchoSupesAct +
                                    "\nm_SpeexWebRtcAecWebRtcAecmIsUseCNGMode：" + m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecmEchoMode：" + m_SpeexWebRtcAecWebRtcAecmEchoMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecmDelay：" + m_SpeexWebRtcAecWebRtcAecmDelay +
                                    "\nm_SpeexWebRtcAecWebRtcAecEchoMode：" + m_SpeexWebRtcAecWebRtcAecEchoMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecDelay：" + m_SpeexWebRtcAecWebRtcAecDelay +
                                    "\nm_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode：" + m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode：" + m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode：" + m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode +
                                    "\nm_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay：" + m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay +
                                    "\n" +
                                    "\nm_UseWhatNs：" + m_UseWhatNs +
                                    "\n" +
                                    "\nm_SpeexPprocIsUseNs：" + m_SpeexPprocIsUseNs +
                                    "\nm_SpeexPprocNoiseSupes：" + m_SpeexPprocNoiseSupes +
                                    "\nm_SpeexPprocIsUseDereverb：" + m_SpeexPprocIsUseDereverb +
                                    "\n" +
                                    "\nm_WebRtcNsxPolicyMode：" + m_WebRtcNsxPolicyMode +
                                    "\n" +
                                    "\nm_WebRtcNsPolicyMode：" + m_WebRtcNsPolicyMode +
                                    "\n" +
                                    "\nm_IsUseSpeexPprocOther：" + m_IsUseSpeexPprocOther +
                                    "\nm_SpeexPprocIsUseVad：" + m_SpeexPprocIsUseVad +
                                    "\nm_SpeexPprocVadProbStart：" + m_SpeexPprocVadProbStart +
                                    "\nm_SpeexPprocVadProbCont：" + m_SpeexPprocVadProbCont +
                                    "\nm_SpeexPprocIsUseAgc：" + m_SpeexPprocIsUseAgc +
                                    "\nm_SpeexPprocAgcLevel：" + m_SpeexPprocAgcLevel +
                                    "\nm_SpeexPprocAgcIncrement：" + m_SpeexPprocAgcIncrement +
                                    "\nm_SpeexPprocAgcDecrement：" + m_SpeexPprocAgcDecrement +
                                    "\nm_SpeexPprocAgcMaxGain：" + m_SpeexPprocAgcMaxGain +
                                    "\n" +
                                    "\nm_UseWhatCodec：" + m_UseWhatCodec +
                                    "\n" +
                                    "\nm_SpeexCodecEncoderUseCbrOrVbr：" + m_SpeexCodecEncoderUseCbrOrVbr +
                                    "\nm_SpeexCodecEncoderQuality：" + m_SpeexCodecEncoderQuality +
                                    "\nm_SpeexCodecEncoderComplexity：" + m_SpeexCodecEncoderComplexity +
                                    "\nm_SpeexCodecEncoderPlcExpectedLossRate：" + m_SpeexCodecEncoderPlcExpectedLossRate +
                                    "\nm_SpeexCodecDecoderIsUsePerceptualEnhancement：" + m_SpeexCodecDecoderIsUsePerceptualEnhancement +
                                    "\n" +
                                    "\nm_IsSaveAudioToFile：" + m_IsSaveAudioToFile +
                                    "\nm_AudioInputFileFullPathStrPt：" + m_AudioInputFileFullPathStrPt +
                                    "\nm_AudioOutputFileFullPathStrPt：" + m_AudioOutputFileFullPathStrPt +
                                    "\nm_AudioResultFileFullPathStrPt：" + m_AudioResultFileFullPathStrPt
                        );
                        p_SettingFileWriterPt.flush();
                        p_SettingFileWriterPt.close();
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：保存设置到文件 " + m_SettingFileFullPathStrPt + " 成功。" );
                    }
                    catch( IOException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：保存设置到文件 " + m_SettingFileFullPathStrPt + " 失败。原因：" + e.getMessage() );
                        break out;
                    }
                }

                //创建PCM格式输入帧、PCM格式输出帧、PCM格式结果帧、PCM格式临时帧、PCM格式交换帧、语音活动状态、Speex格式输入帧、Speex格式输入帧的数据长度、Speex格式输入帧是否需要传输。
                {
                    p_PcmInputFramePt = null;
                    p_PcmOutputFramePt = null;
                    p_PcmResultFramePt = new short[m_FrameLen];
                    p_PcmTmpFramePt = new short[m_FrameLen];
                    p_PcmSwapFramePt = null;
                    p_VoiceActStsPt = new HTInt( 1 ); //语音活动状态预设为1，为了让在没有使用语音活动检测的情况下永远都是有语音活动。
                    p_SpeexInputFramePt = ( m_UseWhatCodec == 1 ) ? new byte[m_FrameLen] : null; //Speex格式输入帧。
                    p_SpeexInputFrameLenPt = ( m_UseWhatCodec == 1 ) ? new HTLong( 0 ) : null; //Speex格式输入帧的数据长度，单位字节。
                    p_SpeexInputFrameIsNeedTransPt = ( m_UseWhatCodec == 1 ) ? new HTInt( 1 ) : null; //Speex格式输入帧是否需要传输，1表示需要传输，0表示不需要传输，预设为1为了让在没有使用非连续传输的情况下永远都是需要传输。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建PCM格式输入帧、PCM格式输出帧、PCM格式结果帧、PCM格式临时帧、PCM格式交换帧、语音活动状态、Speex格式输入帧、Speex格式输入帧的数据长度、Speex格式输入帧是否需要传输成功。" );
                }

                //创建并初始化输入帧链表类对象、输出帧链表类对象。
                {
                    m_InputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化输入帧链表类对象成功。" );
                    m_OutputFrameLnkLstPt = new LinkedList< short[] >();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化输出帧链表类对象成功。" );
                }

                //创建并初始化声学回音消除器类对象。
                switch( m_UseWhatAec )
                {
                    case 0: //如果不使用声学回音消除器。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：不使用声学回音消除器。" );
                        break;
                    }
                    case 1: //如果使用Speex声学回音消除器。
                    {
                        if( m_SpeexAecIsSaveMemFile != 0 )
                        {
                            m_SpeexAecPt = new SpeexAec();
                            if( m_SpeexAecPt.InitByMemFile( m_SamplingRate, m_FrameLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMultiple, m_SpeexAecEchoCont, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct, m_SpeexAecMemFileFullPathStrPt, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：根据Speex声学回音消除器内存块文件 " + m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_SpeexAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：根据Speex声学回音消除器内存块文件 " + m_SpeexAecMemFileFullPathStrPt + " 来创建并初始化Speex声学回音消除器类对象失败。" );
                            }
                        }
                        if( m_SpeexAecPt == null )
                        {
                            m_SpeexAecPt = new SpeexAec();
                            if( m_SpeexAecPt.Init( m_SamplingRate, m_FrameLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMultiple, m_SpeexAecEchoCont, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_SpeexAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex声学回音消除器类对象失败。" );
                                break out;
                            }
                        }
                        break;
                    }
                    case 2: //如果使用WebRtc定点版声学回音消除器。
                    {
                        m_WebRtcAecmPt = new WebRtcAecm();
                        if( m_WebRtcAecmPt.Init( m_SamplingRate, m_FrameLen, m_WebRtcAecmIsUseCNGMode, m_WebRtcAecmEchoMode, m_WebRtcAecmDelay ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc定点版声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            m_WebRtcAecmPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc定点版声学回音消除器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                    case 3: //如果使用WebRtc浮点版声学回音消除器。
                    {
                        if( m_WebRtcAecIsSaveMemFile != 0 )
                        {
                            m_WebRtcAecPt = new WebRtcAec();
                            if( m_WebRtcAecPt.InitByMemFile( m_SamplingRate, m_FrameLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgnosticMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay, m_WebRtcAecMemFileFullPathStrPt, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_WebRtcAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：根据WebRtc浮点版声学回音消除器内存块文件 " + m_WebRtcAecMemFileFullPathStrPt + " 来创建并初始化WebRtc浮点版声学回音消除器类对象失败。" );
                            }
                        }
                        if( m_WebRtcAecPt == null )
                        {
                            m_WebRtcAecPt = new WebRtcAec();
                            if( m_WebRtcAecPt.Init( m_SamplingRate, m_FrameLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgnosticMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc浮点版声学回音消除器类对象成功。" );
                            }
                            else
                            {
                                m_WebRtcAecPt = null;
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc浮点版声学回音消除器类对象失败。" );
                                break out;
                            }
                        }
                        break;
                    }
                    case 4: //如果使用SpeexWebRtc三重声学回音消除器。
                    {
                        m_SpeexWebRtcAecPt = new SpeexWebRtcAec();
                        if( m_SpeexWebRtcAecPt.Init( m_SamplingRate, m_FrameLen, m_SpeexWebRtcAecWorkMode, m_SpeexWebRtcAecSpeexAecFilterLen, m_SpeexWebRtcAecSpeexAecIsUseRec, m_SpeexWebRtcAecSpeexAecEchoMultiple, m_SpeexWebRtcAecSpeexAecEchoCont, m_SpeexWebRtcAecSpeexAecEchoSupes, m_SpeexWebRtcAecSpeexAecEchoSupesAct, m_SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_SpeexWebRtcAecWebRtcAecmEchoMode, m_SpeexWebRtcAecWebRtcAecmDelay, m_SpeexWebRtcAecWebRtcAecEchoMode, m_SpeexWebRtcAecWebRtcAecDelay, m_SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode, m_SpeexWebRtcAecWebRtcAecIsUseExtdFilterMode, m_SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecMode, m_SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelay ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化SpeexWebRtc三重声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            m_SpeexWebRtcAecPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化SpeexWebRtc三重声学回音消除器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                }

                //创建并初始化噪音抑制器对象。
                switch( m_UseWhatNs )
                {
                    case 0: //如果不使用噪音抑制器。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：不使用噪音抑制器。" );
                        break;
                    }
                    case 1: //如果使用Speex预处理器的噪音抑制。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：稍后在初始化Speex预处理器时一起初始化Speex预处理器的噪音抑制。" );
                        break;
                    }
                    case 2: //如果使用WebRtc定点版噪音抑制器。
                    {
                        m_WebRtcNsxPt = new WebRtcNsx();
                        if( m_WebRtcNsxPt.Init( m_SamplingRate, m_FrameLen, m_WebRtcNsxPolicyMode ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc定点版噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            m_WebRtcNsxPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc定点版噪音抑制器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                    case 3: //如果使用WebRtc浮点版噪音抑制器。
                    {
                        m_WebRtcNsPt = new WebRtcNs();
                        if( m_WebRtcNsPt.Init( m_SamplingRate, m_FrameLen, m_WebRtcNsPolicyMode ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc浮点版噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            m_WebRtcNsPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化WebRtc浮点版噪音抑制器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                    case 4: //如果使用RNNoise噪音抑制器。
                    {
                        m_RNNoisePt = new RNNoise();
                        if( m_RNNoisePt.Init( m_SamplingRate, m_FrameLen ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化RNNoise噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            m_RNNoisePt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化RNNoise噪音抑制器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                }

                //创建并初始化Speex预处理器类对象。
                if( ( m_UseWhatNs == 1 ) || ( m_IsUseSpeexPprocOther != 0 ) )
                {
                    if( m_UseWhatNs != 1 )
                    {
                        m_SpeexPprocIsUseNs = 0;
                        m_SpeexPprocIsUseDereverb = 0;
                    }
                    if( m_IsUseSpeexPprocOther == 0 )
                    {
                        m_SpeexPprocIsUseVad = 0;
                        m_SpeexPprocIsUseAgc = 0;
                    }
                    m_SpeexPprocPt = new SpeexPproc();
                    if( m_SpeexPprocPt.Init( m_SamplingRate, m_FrameLen, m_SpeexPprocIsUseNs, m_SpeexPprocNoiseSupes, m_SpeexPprocIsUseDereverb, m_SpeexPprocIsUseVad, m_SpeexPprocVadProbStart, m_SpeexPprocVadProbCont, m_SpeexPprocIsUseAgc, m_SpeexPprocAgcLevel, m_SpeexPprocAgcIncrement, m_SpeexPprocAgcDecrement, m_SpeexPprocAgcMaxGain ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex预处理器类对象成功。"  );
                    }
                    else
                    {
                        m_SpeexPprocPt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex预处理器类对象失败。" );
                        break out;
                    }
                }

                //初始化编解码器对象。
                switch( m_UseWhatCodec )
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用PCM原始数据。" );
                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        if( m_FrameLen != m_SamplingRate / 1000 * 20 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：帧的数据长度不为20毫秒不能使用Speex编解码器。" );
                            break out;
                        }
                        m_SpeexEncoderPt = new SpeexEncoder();
                        if( m_SpeexEncoderPt.Init( m_SamplingRate, m_SpeexCodecEncoderUseCbrOrVbr, m_SpeexCodecEncoderQuality, m_SpeexCodecEncoderComplexity, m_SpeexCodecEncoderPlcExpectedLossRate ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex编码器类对象成功。" );
                        }
                        else
                        {
                            m_SpeexEncoderPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex编码器类对象失败。" );
                            break out;
                        }
                        m_SpeexDecoderPt = new SpeexDecoder();
                        if( m_SpeexDecoderPt.Init( m_SamplingRate, m_SpeexCodecDecoderIsUsePerceptualEnhancement ) == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex解码器类对象成功。" );
                        }
                        else
                        {
                            m_SpeexDecoderPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化Speex解码器类对象失败。" );
                            break out;
                        }
                        break;
                    }
                    case 2: //如果使用Opus编解码器。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：暂不支持使用Opus编解码器。" );
                        break out;
                    }
                }

                //创建并初始化音频输入Wave文件写入器类对象、音频输出Wave文件写入器类对象、音频结果Wave文件写入器类对象。
                if( m_IsSaveAudioToFile != 0 )
                {
                    m_AudioInputWaveFileWriterPt = new WaveFileWriter();
                    if( m_AudioInputWaveFileWriterPt.Init( m_AudioInputFileFullPathStrPt, ( short ) 1, m_SamplingRate, 16 ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输入文件 " + m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类对象成功。" );
                    }
                    else
                    {
                        m_AudioInputWaveFileWriterPt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输入文件 " + m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                        break out;
                    }
                    m_AudioOutputWaveFileWriterPt = new WaveFileWriter();
                    if( m_AudioOutputWaveFileWriterPt.Init( m_AudioOutputFileFullPathStrPt, ( short ) 1, m_SamplingRate, 16 ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输出文件 " + m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类类对象成功。" );
                    }
                    else
                    {
                        m_AudioOutputWaveFileWriterPt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输出文件 " + m_AudioOutputFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                        break out;
                    }
                    m_AudioResultWaveFileWriterPt = new WaveFileWriter();
                    if( m_AudioResultWaveFileWriterPt.Init( m_AudioResultFileFullPathStrPt, ( short ) 1, m_SamplingRate, 16 ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频结果文件 " + m_AudioInputFileFullPathStrPt + " 的Wave文件写入器类对象成功。" );
                    }
                    else
                    {
                        m_AudioResultWaveFileWriterPt = null;
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频结果文件 " + m_AudioResultFileFullPathStrPt + " 的Wave文件写入器类对象失败。" );
                        break out;
                    }
                }

                //创建并初始化音频输入类对象、音频输出类对象、音频输入线程类对象、音频输出线程类对象。
                {
                    //创建并初始化音频输入类对象。
                    try
                    {
                        m_AudioRecordBufSz = AudioRecord.getMinBufferSize( m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                        m_AudioRecordBufSz = m_AudioRecordBufSz > m_FrameLen * 2 ? m_AudioRecordBufSz : m_FrameLen * 2;
                        m_AudioRecordPt = new AudioRecord(
                                ( m_IsUseSystemAecNsAgc != 0 ) ? ( ( android.os.Build.VERSION.SDK_INT >= 11 ) ? MediaRecorder.AudioSource.VOICE_COMMUNICATION : MediaRecorder.AudioSource.MIC ) : MediaRecorder.AudioSource.MIC,
                                m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioRecordBufSz
                        );
                        if( m_AudioRecordPt.getState() == AudioRecord.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输入类对象成功。音频输入缓冲区大小：" + m_AudioRecordBufSz );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输入类对象失败。" );
                            break out;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：创建并初始化音频输入类对象失败。原因：" + e.getMessage() );
                        break out;
                    }

                    //设置音频输出设备。
                    if( m_UseWhatAudioOutputDevice == 0 ) //如果使用扬声器。
                    {
                        ( ( AudioManager )m_AppContextPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( true ); //打开扬声器。
                    }
                    else //如果使用听筒。
                    {
                        ( ( AudioManager )m_AppContextPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( false ); //关闭扬声器。
                    }

                    //用第一种方法创建并初始化音频输出类对象。
                    try
                    {
                        m_AudioTrackBufSz = m_FrameLen * 2;
                        m_AudioTrackPt = new AudioTrack( ( m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                m_SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_AudioTrackBufSz,
                                AudioTrack.MODE_STREAM );
                        if( m_AudioTrackPt.getState() == AudioTrack.STATE_INITIALIZED )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：用第一种方法创建并初始化音频输出类对象成功。音频输出缓冲区大小：" + m_AudioTrackBufSz );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：用第一种方法创建并初始化音频输出类对象失败。" );
                            m_AudioTrackPt.release();
                            m_AudioTrackPt = null;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：用第一种方法创建并初始化音频输出类对象失败。原因：" + e.getMessage() );
                    }

                    //用第二种方法创建并初始化音频输出类对象。
                    if( m_AudioTrackPt == null )
                    {
                        try
                        {
                            m_AudioTrackBufSz = AudioTrack.getMinBufferSize( m_SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                            m_AudioTrackPt = new AudioTrack( ( m_UseWhatAudioOutputStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                                    m_SamplingRate,
                                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    m_AudioTrackBufSz,
                                    AudioTrack.MODE_STREAM );
                            if( m_AudioTrackPt.getState() == AudioTrack.STATE_INITIALIZED )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：用第二种方法创建并初始化音频输出类对象成功。音频输出缓冲区大小：" + m_AudioTrackBufSz );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：用第二种方法创建并初始化音频输出类对象失败。" );
                                break out;
                            }
                        }
                        catch( IllegalArgumentException e )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：用第二种方法创建并初始化音频输出类对象失败。原因：" + e.getMessage() );
                            break out;
                        }
                    }

                    //创建并启动音频输入线程类对象、音频输出线程类对象。必须在初始化音频输入类对象、音频输出类对象后初始化音频输入线程类对象、音频输出线程类对象，因为音频输入线程类对象、音频输出线程类对象会使用音频输入类对象、音频输出类对象。
                    m_AudioInputThreadPt = new AudioInputThread(); //初始化音频输入线程类对象。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建音频输入线程类对象成功。" );
                    m_AudioOutputThreadPt = new AudioOutputThread(); //初始化音频输出线程类对象。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：创建音频输出线程类对象成功。" );
                    m_AudioInputThreadPt.start(); //启动音频输入线程，让音频输入线程去启动音频输出线程。
                }

                m_ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。

                if( m_IsPrintLogcat != 0 )
                {
                    p_NowMsec = System.currentTimeMillis();
                    Log.i( m_CurClsNameStrPt, "音频处理线程：音频处理线程初始化完毕，耗时：" + ( p_NowMsec - p_LastMsec ) + " 毫秒，正式开始处理音频。" );
                }

                while( true )
                {
                    if( m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis();

                    //调用用户定义的处理函数。
                    p_TmpInt32 = UserProcess();
                    if( p_TmpInt32 == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：调用用户定义的处理函数成功。返回值：" + p_TmpInt32 );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：调用用户定义的处理函数失败。返回值：" + p_TmpInt32 );
                        break out;
                    }

                    //开始处理输入帧。
                    if( ( m_InputFrameLnkLstPt.size() > 0 ) && ( m_OutputFrameLnkLstPt.size() > 0 ) || //如果输入帧链表和输出帧链表中都有帧了，才开始处理。
                        ( m_InputFrameLnkLstPt.size() > 15 ) ) //如果输入帧链表里已经累积很多输入帧了，说明输出帧链表里迟迟没有音频输出帧，也开始处理。
                    {
                        //从输入帧链表中取出第一个输入帧。
                        synchronized( m_InputFrameLnkLstPt )
                        {
                            p_PcmInputFramePt = m_InputFrameLnkLstPt.getFirst();
                            m_InputFrameLnkLstPt.removeFirst();
                        }
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：从输入帧链表中取出第一个输入帧。" );

                        //从输出帧链表中取出第一个输出帧。
                        if( m_OutputFrameLnkLstPt.size() > 0 ) //如果输出帧链表里有输出帧。
                        {
                            synchronized( m_OutputFrameLnkLstPt )
                            {
                                p_PcmOutputFramePt = m_OutputFrameLnkLstPt.getFirst();
                                m_OutputFrameLnkLstPt.removeFirst();
                            }
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：从输出帧链表中取出第一个输出帧。" );
                        }
                        else //如果输出帧链表里没有输出帧。
                        {
                            p_PcmOutputFramePt = new short[m_FrameLen];
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：输出帧链表中没有输出帧，用一个空帧代替。" );
                        }

                        //将输入帧复制到结果帧，方便处理。
                        System.arraycopy( p_PcmInputFramePt, 0, p_PcmResultFramePt, 0, m_FrameLen );

                        //使用声学回音消除器。
                        switch( m_UseWhatAec )
                        {
                            case 0: //如果不使用声学回音消除器。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：不使用声学回音消除器。" );
                                break;
                            }
                            case 1: //如果使用Speex声学回音消除器。
                            {
                                if( m_SpeexAecPt.Proc( p_PcmResultFramePt, p_PcmOutputFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用Speex声学回音消除器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用Speex声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 2: //如果使用WebRtc定点版声学回音消除器。
                            {
                                if( m_WebRtcAecmPt.Proc( p_PcmResultFramePt, p_PcmOutputFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用WebRtc定点版声学回音消除器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用WebRtc定点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 3: //如果使用WebRtc浮点版声学回音消除器。
                            {
                                if( m_WebRtcAecPt.Proc( p_PcmResultFramePt, p_PcmOutputFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用WebRtc浮点版声学回音消除器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用WebRtc浮点版声学回音消除器失败。" );
                                }
                                break;
                            }
                            case 4: //如果使用SpeexWebRtc三重浮点版声学回音消除器。
                            {
                                if( m_SpeexWebRtcAecPt.Proc( p_PcmResultFramePt, p_PcmOutputFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用SpeexWebRtc三重浮点版声学回音消除器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用SpeexWebRtc三重浮点版声学回音消除器失败。" );
                                }
                                break;
                            }
                        }

                        //使用噪音抑制器。
                        switch( m_UseWhatNs )
                        {
                            case 0: //如果不使用噪音抑制器。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：不使用噪音抑制器。" );
                                break;
                            }
                            case 1: //如果使用Speex预处理器的噪音抑制。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：稍后在使用Speex预处理器时一起使用噪音抑制。" );
                                break;
                            }
                            case 2: //如果使用WebRtc定点版噪音抑制器。
                            {
                                if( m_WebRtcNsxPt.Proc( p_PcmResultFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用WebRtc定点版噪音抑制器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用WebRtc定点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 3: //如果使用WebRtc浮点版噪音抑制器。
                            {
                                if( m_WebRtcNsPt.Proc( p_PcmResultFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用WebRtc浮点版噪音抑制器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用WebRtc浮点版噪音抑制器失败。" );
                                }
                                break;
                            }
                            case 4: //如果使用RNNoise噪音抑制器。
                            {
                                if( m_RNNoisePt.Proc( p_PcmResultFramePt, p_PcmTmpFramePt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用RNNoise噪音抑制器成功。" );
                                    p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用RNNoise噪音抑制器失败。" );
                                }
                                break;
                            }
                        }

                        //使用Speex预处理器。
                        if( ( m_UseWhatNs == 1 ) || ( m_IsUseSpeexPprocOther != 0 ) )
                        {
                            if( m_SpeexPprocPt.Proc( p_PcmResultFramePt, p_PcmTmpFramePt, p_VoiceActStsPt ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用Speex预处理器成功。语音活动状态：" + p_VoiceActStsPt.m_Val );
                                p_PcmSwapFramePt = p_PcmResultFramePt;p_PcmResultFramePt = p_PcmTmpFramePt;p_PcmTmpFramePt = p_PcmSwapFramePt; //交换结果帧和临时帧。
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用Speex预处理器失败。" );
                            }
                        }

                        //使用编码器。
                        switch( m_UseWhatCodec )
                        {
                            case 0: //如果使用PCM原始数据。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用PCM原始数据。" );
                                break;
                            }
                            case 1: //如果使用Speex编码器。
                            {
                                if( m_SpeexEncoderPt.Proc( p_PcmResultFramePt, p_SpeexInputFramePt, p_SpeexInputFramePt.length, p_SpeexInputFrameLenPt, p_SpeexInputFrameIsNeedTransPt ) == 0 )
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用Speex编码器成功。Speex格式输入帧的数据长度：" + p_SpeexInputFrameLenPt.m_Val + "，Speex格式输入帧是否需要传输：" + p_SpeexInputFrameIsNeedTransPt.m_Val );
                                }
                                else
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用Speex编码器失败。" );
                                }
                                break;
                            }
                            case 2: //如果使用Opus编码器。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：暂不支持使用Opus编码器。" );
                                break out;
                            }
                        }

                        //使用音频输入Wave文件写入器写入输入帧数据、音频输出Wave文件写入器写入输出帧数据、音频结果Wave文件写入器写入结果帧数据。
                        if( m_IsSaveAudioToFile != 0 )
                        {
                            if( m_AudioInputWaveFileWriterPt.WriteData( p_PcmInputFramePt, m_FrameLen ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用音频输入Wave文件写入器写入输入帧数据成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用音频输入Wave文件写入器写入输入帧数据失败。" );
                            }
                            if( m_AudioOutputWaveFileWriterPt.WriteData( p_PcmOutputFramePt, m_FrameLen ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用音频输入Wave文件写入器写入输出帧数据成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用音频输出Wave文件写入器写入输出帧数据失败。" );
                            }
                            if( m_AudioResultWaveFileWriterPt.WriteData( p_PcmResultFramePt, m_FrameLen ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：使用音频输入Wave文件写入器写入结果帧数据成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：使用音频结果Wave文件写入器写入结果帧数据失败。" );
                            }
                        }

                        //调用用户定义的读取输入帧函数。
                        p_TmpInt32 = UserReadInputFrame( p_PcmInputFramePt, p_PcmResultFramePt, p_VoiceActStsPt.m_Val, p_SpeexInputFramePt, p_SpeexInputFrameLenPt, p_SpeexInputFrameIsNeedTransPt );
                        if( p_TmpInt32 == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：调用用户定义的读取输入帧函数成功。返回值：" + p_TmpInt32 );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：调用用户定义的读取输入帧函数失败。返回值：" + p_TmpInt32 );
                            break out;
                        }

                        if( m_IsPrintLogcat != 0 )
                        {
                            p_NowMsec = System.currentTimeMillis();
                            Log.i( m_CurClsNameStrPt, "音频处理线程：本音频帧处理完毕，耗时：" + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
                        }
                    }

                    if( m_ExitFlag != 0 ) //如果本线程退出标记为请求退出。
                    {
                        m_ExitCode = 0; //处理已经成功了，再将本线程退出代码设置为正常退出。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：接收到退出请求，开始准备退出。" );
                        break out;
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                }
            }

            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：本线程开始退出。" );

            //销毁音频输入线程类对象、音频输出线程类对、音频输入类对象、音频输出类对象。
            {
                {//必须在销毁音频输入类对象、音频输出类对象前销毁音频输入线程类对象、音频输出线程类对象，因为音频输入线程类对象、音频输出线程类对象会使用音频输入类对象、音频输出类对象。
                    if( m_AudioInputThreadPt != null ) m_AudioInputThreadPt.RequireExit(); //请求音频输入线程退出。
                    if( m_AudioOutputThreadPt != null ) m_AudioOutputThreadPt.RequireExit(); //请求音频输出线程退出。
                    if( m_AudioInputThreadPt != null )
                    {
                        try
                        {
                            m_AudioInputThreadPt.join(); //等待音频输入线程退出。
                            m_AudioInputThreadPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输入线程类对象成功。" );
                        }
                        catch( InterruptedException e )
                        {
                        }
                    }
                    if( m_AudioOutputThreadPt != null )
                    {
                        try
                        {
                            m_AudioOutputThreadPt.join(); //等待音频输出线程退出。
                            m_AudioOutputThreadPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输出线程类对象成功。" );
                        }
                        catch( InterruptedException e )
                        {
                        }
                    }
                }
                if( m_AudioRecordPt != null )
                {
                    if( m_AudioRecordPt.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) m_AudioRecordPt.stop();
                    m_AudioRecordPt.release();
                    m_AudioRecordPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输入类对象成功。" );
                }
                if( m_AudioTrackPt != null )
                {
                    if( m_AudioTrackPt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_AudioTrackPt.stop();
                    m_AudioTrackPt.release();
                    m_AudioTrackPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输出类对象成功。" );
                }
            }

            //销毁音频输入Wave文件写入器类对象、音频输出Wave文件写入器类对象、音频结果Wave文件写入器类对象。
            if( m_IsSaveAudioToFile != 0 )
            {
                if( m_AudioInputWaveFileWriterPt != null )
                {
                    if( m_AudioInputWaveFileWriterPt.Destroy() == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输入Wave文件写入器类对象成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁音频输入Wave文件写入器类对象失败。" );
                    }
                    m_AudioInputWaveFileWriterPt = null;
                }
                if( m_AudioOutputWaveFileWriterPt != null )
                {
                    if( m_AudioOutputWaveFileWriterPt.Destroy() == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频输出Wave文件写入器类对象成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁音频输出Wave文件写入器类对象失败。" );
                    }
                    m_AudioOutputWaveFileWriterPt = null;
                }
                if( m_AudioResultWaveFileWriterPt != null )
                {
                    if( m_AudioResultWaveFileWriterPt.Destroy() == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁音频结果Wave文件写入器类对象成功。" );
                    }
                    else
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁音频结果Wave文件写入器类对象失败。" );
                    }
                    m_AudioResultWaveFileWriterPt = null;
                }
            }

            //销毁编解码器类对象。
            switch( m_UseWhatCodec )
            {
                case 0: //如果使用PCM原始数据。
                {
                    break;
                }
                case 1: //如果使用Speex编码器。
                {
                    if( m_SpeexEncoderPt != null )
                    {
                        if( m_SpeexEncoderPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁Speex编码器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁Speex编码器类对象失败。" );
                        }
                        m_SpeexEncoderPt = null;
                    }
                    if( m_SpeexDecoderPt != null )
                    {
                        if( m_SpeexDecoderPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁Speex解码器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁Speex解码器类对象失败。" );
                        }
                        m_SpeexDecoderPt = null;
                    }
                    break;
                }
                case 2: //如果使用Opus编码器。
                {
                    break;
                }
            }

            //销毁Speex预处理器类对象。
            if( m_SpeexPprocPt != null )
            {
                if( m_SpeexPprocPt.Destroy() == 0 )
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁Speex预处理器类对象成功。" );
                }
                else
                {
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁Speex预处理器类对象失败。" );
                }
                m_SpeexPprocPt = null;
            }

            //销毁噪音抑制器。
            switch( m_UseWhatNs )
            {
                case 0: //如果不使用噪音抑制器。
                {
                    break;
                }
                case 1: //如果使用Speex预处理器的噪音抑制。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：之前在销毁Speex预处理器时一起销毁Speex预处理器的噪音抑制。" );
                    break;
                }
                case 2: //如果使用WebRtc定点版噪音抑制器。
                {
                    if( m_WebRtcNsxPt != null )
                    {
                        if( m_WebRtcNsxPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc定点版噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc定点版噪音抑制器类对象失败。" );
                        }
                        m_WebRtcNsxPt = null;
                    }
                    break;
                }
                case 3: //如果使用WebRtc浮点版噪音抑制器类对象。
                {
                    if( m_WebRtcNsPt != null )
                    {
                        if( m_WebRtcNsPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc浮点版噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc浮点版噪音抑制器类对象失败。" );
                        }
                        m_WebRtcNsPt = null;
                    }
                    break;
                }
                case 4: //如果使用RNNoise噪音抑制器类对象。
                {
                    if( m_RNNoisePt != null )
                    {
                        if( m_RNNoisePt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁RNNoise噪音抑制器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁RNNoise噪音抑制器类对象失败。" );
                        }
                        m_RNNoisePt = null;
                    }
                    break;
                }
            }

            //销毁声学回音消除器。
            switch( m_UseWhatAec )
            {
                case 0: //如果不使用声学回音消除器。
                {
                    break;
                }
                case 1: //如果使用Speex声学回音消除器。
                {
                    if( m_SpeexAecPt != null )
                    {
                        if( m_SpeexAecIsSaveMemFile != 0 )
                        {
                            if( m_SpeexAecPt.SaveMemFile( m_SamplingRate, m_FrameLen, m_SpeexAecFilterLen, m_SpeexAecIsUseRec, m_SpeexAecEchoMultiple, m_SpeexAecEchoCont, m_SpeexAecEchoSupes, m_SpeexAecEchoSupesAct, m_SpeexAecMemFileFullPathStrPt, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecMemFileFullPathStrPt + " 成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：将Speex声学回音消除器内存块保存到指定的文件 " + m_SpeexAecMemFileFullPathStrPt + " 失败。" );
                            }
                        }
                        if( m_SpeexAecPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁Speex声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁Speex声学回音消除器类对象失败。" );
                        }
                        m_SpeexAecPt = null;
                    }
                    break;
                }
                case 2: //如果使用WebRtc定点版声学回音消除器。
                {
                    if( m_WebRtcAecmPt != null )
                    {
                        if( m_WebRtcAecmPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc定点版声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc定点版声学回音消除器类对象失败。" );
                        }
                        m_WebRtcAecmPt = null;
                    }
                    break;
                }
                case 3: //如果使用WebRtc浮点版声学回音消除器。
                {
                    if( m_WebRtcAecPt != null )
                    {
                        if( m_WebRtcAecIsSaveMemFile != 0 )
                        {
                            if( m_WebRtcAecPt.SaveMemFile( m_SamplingRate, m_FrameLen, m_WebRtcAecEchoMode, m_WebRtcAecDelay, m_WebRtcAecIsUseDelayAgnosticMode, m_WebRtcAecIsUseExtdFilterMode, m_WebRtcAecIsUseRefinedFilterAdaptAecMode, m_WebRtcAecIsUseAdaptAdjDelay, m_WebRtcAecMemFileFullPathStrPt, null ) == 0 )
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecMemFileFullPathStrPt + " 成功。" );
                            }
                            else
                            {
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：将WebRtc浮点版声学回音消除器内存块保存到指定的文件 " + m_WebRtcAecMemFileFullPathStrPt + " 失败。" );
                            }
                        }
                        if( m_WebRtcAecPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc浮点版声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁WebRtc浮点版声学回音消除器类对象失败。" );
                        }
                        m_WebRtcAecPt = null;
                    }
                    break;
                }
                case 4: //如果使用SpeexWebRtc三重声学回音消除器。
                {
                    if( m_SpeexWebRtcAecPt != null )
                    {
                        if( m_SpeexWebRtcAecPt.Destroy() == 0 )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁SpeexWebRtc三重声学回音消除器类对象成功。" );
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频处理线程：销毁SpeexWebRtc三重声学回音消除器类对象失败。" );
                        }
                        m_SpeexWebRtcAecPt = null;
                    }
                    break;
                }
            }

            //销毁输入帧链表类对象、输出帧链表类对象。
            {
                if( m_InputFrameLnkLstPt != null )
                {
                    m_InputFrameLnkLstPt.clear();
                    m_InputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁输入帧链表类对象成功。" );
                }
                if( m_OutputFrameLnkLstPt != null )
                {
                    m_OutputFrameLnkLstPt.clear();
                    m_OutputFrameLnkLstPt = null;
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁输出帧链表类对象成功。" );
                }
            }

            //销毁PCM格式输入帧、PCM格式输出帧、PCM格式结果帧、PCM格式临时帧、PCM格式交换帧、语音活动状态、Speex格式输入帧、Speex格式输入帧的数据长度、Speex格式输入帧是否需要传输。
            {
                p_PcmInputFramePt = null; //PCM格式输入帧。
                p_PcmOutputFramePt = null; //PCM格式输出帧。
                p_PcmResultFramePt = null; //PCM格式结果帧。
                p_PcmTmpFramePt = null; //PCM格式临时帧。
                p_PcmSwapFramePt = null; //PCM格式交换帧。
                p_VoiceActStsPt = null; //语音活动状态，1表示有语音活动，0表示无语音活动，预设为1，为了让在没有使用语音活动检测的情况下永远都是有语音活动。
                p_SpeexInputFramePt = null; //Speex格式输入帧。
                p_SpeexInputFrameLenPt = null; //Speex格式输入帧的数据长度，单位字节。
                p_SpeexInputFrameIsNeedTransPt = null; //Speex格式输入帧是否需要传输，1表示需要传输，0表示不需要传输，预设为1为了让在没有使用非连续传输的情况下永远都是需要传输。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：销毁PCM格式输入帧、PCM格式输出帧、PCM格式结果帧、PCM格式临时帧、PCM格式交换帧、语音活动状态、Speex格式输入帧、Speex格式输入帧的数据长度、Speex格式输入帧是否需要传输成功。" );
            }

            if( m_ExitFlag != 3 ) //如果需要调用用户定义的销毁函数。
            {
                UserDestroy(); //调用用户定义的销毁函数。
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：调用用户定义的销毁函数成功。" );
            }

            if( ( m_ExitFlag == 0 ) || ( m_ExitFlag == 1 ) ) //如果用户需要直接退出。
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：本线程已退出。" );
                break ReInit;
            }
            else //如果用户需用重新初始化。
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频处理线程：本线程重新初始化。" );
            }
        }
    }
}