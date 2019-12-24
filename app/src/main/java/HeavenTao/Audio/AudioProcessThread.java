package HeavenTao.Audio;

import android.app.Activity;
import android.content.Context;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import HeavenTao.Data.*;

//音频处理线程类。
public abstract class AudioProcessThread extends Thread
{
    public String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //当前类名称字符串

    public int m_i32ExitFlag = 0; //本线程退出标记，为0表示保持运行，为1表示请求退出。
    public int m_i32ExitCode = 0; //本线程退出代码，为0表示正常退出，为-1表示初始化失败，为-2表示处理失败。

    static Context m_pclApplicationContext; //存放应用程序上下文类对象的内存指针。
    public int m_i32SamplingRate = 16000; //采样频率，取值只能为8000、16000、32000。
    public long m_i64FrameLength = 320; //帧的数据长度，取值只能为：8000Hz为160个采样，16000Hz为320个采样，32000Hz为640个采样。

    public int m_i32IsPrintLogcat = 1; //存放是否打印Logcat日志。

    PowerManager.WakeLock m_clProximityScreenOffWakeLock; //存放接近息屏唤醒锁类对象的内存指针。
    PowerManager.WakeLock m_clFullWakeLock; //存放屏幕键盘全亮唤醒锁类对象的内存指针。
    int m_i32IsUseWakeLock; //存放是否使用唤醒锁，非0表示要使用，0表示不使用。

    AudioRecord m_pclAudioRecord; //存放音频输入类对象的内存指针。
    int m_i32AudioRecordBufferSize; //存放音频输入类对象的缓冲区大小，单位字节。

    AudioTrack m_pclAudioTrack; //存放音频输出类对象的内存指针。
    int m_i32AudioTrackBufferSize; //存放音频输出类对象的缓冲区大小，单位字节。

    int m_i32UseWhatAec; //存放使用什么声学回音消除器，为0表示不使用，为1表示Speex声学回音消除器，为2表示WebRtc定点版声学回音消除器，为2表示WebRtc浮点版声学回音消除器，为4表示SpeexWebRtc三重声学回音消除器。

    SpeexAec m_pclSpeexAec; //存放Speex声学回音消除器类对象的内存指针。
    int m_i32SpeexAecFilterLength; //存放Speex声学回音消除器的滤波器数据长度，单位毫秒。
    int m_i32SpeexAecIsSaveMemoryFile; //存放Speex声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
    String m_pclSpeexAecMemoryFileFullPath; //存放Speex声学回音消除器的内存块文件完整路径字符串。

    WebRtcAecm m_pclWebRtcAecm; //存放WebRtc定点版声学回音消除器类对象的内存指针。
    int m_i32WebRtcAecmIsUseCNGMode; //存放WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
    int m_i32WebRtcAecmEchoMode; //存放WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
    int m_i32WebRtcAecmDelay; //存放WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。

    WebRtcAec m_pclWebRtcAec; //存放WebRtc浮点版声学回音消除器类对象的内存指针。
    int m_i32WebRtcAecEchoMode; //存放WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
    int m_i32WebRtcAecDelay; //存放WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
    int m_i32WebRtcAecIsUseDelayAgnosticMode; //存放WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
    int m_i32WebRtcAecIsUseAdaptiveAdjustDelay; //存放WebRtc浮点版声学回音消除器是否使用自适应调节回音的延迟，为非0表示要使用，为0表示不使用。
    int m_i32WebRtcAecIsSaveMemoryFile; //存放WebRtc浮点版声学回音消除器是否保存内存块到文件，为非0表示要保存，为0表示不保存。
    String m_pclWebRtcAecMemoryFileFullPath; //存放WebRtc浮点版声学回音消除器的内存块文件完整路径字符串。

    SpeexWebRtcAec m_pclSpeexWebRtcAec; //存放SpeexWebRtc三重声学回音消除器类对象的内存指针。
    int m_i32SpeexWebRtcAecWorkMode; //存放SpeexWebRtc三重声学回音消除器的工作模式，为1表示Speex声学回音消除器+WebRtc定点版声学回音消除器，为2表示WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器，为3表示Speex声学回音消除器+WebRtc定点版声学回音消除器+WebRtc浮点版声学回音消除器。
    int m_i32SpeexWebRtcAecSpeexAecFilterLength; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器的滤波器数据长度，单位毫秒。
    float m_fSpeexWebRtcAecSpeexAecEchoMultiple; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
    int m_i32SpeexWebRtcAecSpeexAecEchoSuppress; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
    int m_i32SpeexWebRtcAecSpeexAecEchoSuppressActive; //存放SpeexWebRtc三重声学回音消除器的Speex声学回音消除器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
    int m_i32SpeexWebRtcAecWebRtcAecmIsUseCNGMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器是否使用舒适噪音生成模式，为非0表示要使用，为0表示不使用。
    int m_i32SpeexWebRtcAecWebRtcAecmEchoMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,4]。
    int m_i32SpeexWebRtcAecWebRtcAecmDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
    int m_i32SpeexWebRtcAecWebRtcAecEchoMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的消除模式，消除模式越高消除越强，取值区间为[0,2]。
    int m_i32SpeexWebRtcAecWebRtcAecDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器的回音延迟，单位毫秒，取值区间为[-2147483648,2147483647]，为0表示自适应设置。
    int m_i32SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用回音延迟不可知模式，为非0表示要使用，为0表示不使用。
    int m_i32SpeexWebRtcAecWebRtcAecIsUseAdaptiveAdjustDelay; //存放SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器是否使用自适应调节回音的延迟，为非0表示要使用，为0表示不使用。

    int m_i32UseWhatNs; //存放使用什么噪音抑制器，为0表示不使用，为1表示Speex预处理器的噪音抑制，为2表示WebRtc定点版噪音抑制器，为3表示WebRtc浮点版噪音抑制器，为4表示RNNoise噪音抑制器。

    int m_i32SpeexPreprocessorIsUseNs; //存放Speex预处理器是否使用噪音抑制，为非0表示要使用，为0表示不使用。
    int m_i32SpeexPreprocessorNoiseSuppress; //存放Speex预处理器在噪音抑制时，噪音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
    int m_i32SpeexPreprocessorIsUseDereverberation; //存放Speex预处理器是否使用混响音消除，为非0表示要使用，为0表示不使用。
    int m_i32SpeexPreprocessorIsUseRec; //存放Speex预处理器是否使用残余回音消除，为非0表示要使用，为0表示不使用。
    float m_fSpeexPreprocessorEchoMultiple; //存放Speex预处理器在残余回音消除时，残余回音的倍数，倍数越大消除越强，取值区间为[0.0,100.0]。
    int m_i32SpeexPreprocessorEchoSuppress; //存放Speex预处理器在残余回音消除时，残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。
    int m_i32SpeexPreprocessorEchoSuppressActive; //存放Speex预处理器在残余回音消除时，有近端语音活动时残余回音最大衰减的分贝值，分贝值越小衰减越大，取值区间为[-2147483648,0]。

    WebRtcNsx m_pclWebRtcNsx; //存放WebRtc定点版噪音抑制器类对象的内存指针。
    int m_i32WebRtcNsxPolicyMode; //存放WebRtc定点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

    WebRtcNs m_pclWebRtcNs; //存放WebRtc浮点版噪音抑制器类对象的内存指针。
    int m_i32WebRtcNsPolicyMode; //存放WebRtc浮点版噪音抑制器的策略模式，策略模式越高抑制越强，取值区间为[0,3]。

    RNNoise m_pclRNNoise; //RNNoise噪音抑制器类对象的内存指针。

    SpeexPreprocessor m_pclSpeexPreprocessor; //存放Speex预处理器类对象的内存指针。
    int m_i32IsUseSpeexPreprocessorOther; //存放Speex预处理器是否使用其他功能，为非0表示要使用，为0表示不使用。
    int m_i32SpeexPreprocessorIsUseVad; //存放Speex预处理器是否使用语音活动检测，为非0表示要使用，为0表示不使用。
    int m_i32SpeexPreprocessorVadProbStart; //存放Speex预处理器在语音活动检测时，从无语音活动到有语音活动的判断百分比概率，概率越大越难判断为有语音活，取值区间为[0,100]。
    int m_i32SpeexPreprocessorVadProbContinue; //存放Speex预处理器在语音活动检测时，从有语音活动到无语音活动的判断百分比概率，概率越大越容易判断为无语音活动，取值区间为[0,100]。
    int m_i32SpeexPreprocessorIsUseAgc; //存放Speex预处理器是否使用自动增益控制，为非0表示要使用，为0表示不使用。
    int m_i32SpeexPreprocessorAgcLevel; //存放Speex预处理器在自动增益控制时，增益的目标等级，目标等级越大增益越大，取值区间为[1,2147483647]。
    int m_i32SpeexPreprocessorAgcIncrement; //存放Speex预处理器在自动增益控制时，每秒最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。
    int m_i32SpeexPreprocessorAgcDecrement; //存放Speex预处理器在自动增益控制时，每秒最大减益的分贝值，分贝值越小减益越大，取值区间为[-2147483648,0]。
    int m_i32SpeexPreprocessorAgcMaxGain; //存放Speex预处理器在自动增益控制时，最大增益的分贝值，分贝值越大增益越大，取值区间为[0,2147483647]。

    public int m_i32UseWhatCodec; //存放使用什么编解码器，为0表示PCM原始数据，为1表示Speex编解码器，为2表示Opus编解码器。

    SpeexEncoder m_pclSpeexEncoder; //存放Speex编码器类对象的内存指针。
    SpeexDecoder m_pclSpeexDecoder; //存放Speex解码器类对象的内存指针。
    int m_i32SpeexCodecEncoderUseCbrOrVbr; //存放Speex编码器使用固定比特率还是动态比特率进行编码，为0表示要使用固定比特率，为非0表示要使用动态比特率。
    int m_i32SpeexCodecEncoderQuality; //存放Speex编码器的编码质量等级，质量等级越高音质越好、压缩率越低，取值区间为[0,10]。
    int m_i32SpeexCodecEncoderComplexity; //存放Speex编码器的编码复杂度，复杂度越高压缩率不变、CPU使用率越高、音质越好，取值区间为[0,10]。
    int m_i32SpeexCodecEncoderPlcExpectedLossRate; //存放Speex编码器在数据包丢失隐藏时，数据包的预计丢失概率，预计丢失概率越高抗网络抖动越强、压缩率越低，取值区间为[0,100]。
    int m_i32SpeexCodecDecoderIsUsePerceptualEnhancement; //存放Speex解码器是否使用知觉增强，为非0表示要使用，为0表示不使用。

    WaveFileWriter m_pclAudioInputWaveFileWriter; //存放音频输入Wave文件写入器对象的内存指针。
    WaveFileWriter m_pclAudioOutputWaveFileWriter; //存放音频输出Wave文件写入器对象的内存指针。
    WaveFileWriter m_pclAudioResultWaveFileWriter; //存放音频结果Wave文件写入器对象的内存指针。
    int m_i32IsSaveAudioToFile; //存放是否保存音频到文件，非0表示要使用，0表示不使用。
    String m_pclAudioInputFileFullPath; //存放音频输入文件的完整路径字符串。
    String m_pclAudioOutputFileFullPath; //存放音频输出文件的完整路径字符串。
    String m_pclAudioResultFileFullPath; //存放音频结果文件的完整路径字符串。

    LinkedList< short[] > m_pclInputFrameLinkedList; //存放输入帧链表类对象的内存指针。
    LinkedList< short[] > m_pclOutputFrameLinkedList; //存放已出帧链表类对象的内存指针。

    AudioInputThread m_pclAudioInputThread; //存放音频输入线程类对象的内存指针。
    AudioOutputThread m_pclAudioOutputThread; //存放音频输出线程类对象的内存指针。

    long m_i64HasVoiceActivityFrameTotal; //有语音活动帧总数。

    //音频输入线程类。
    private class AudioInputThread extends Thread
    {
        public int m_i32ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_i32ExitFlag = 1;
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            short p_pszi16TempInputFrame[];
            Date p_pclLastDate;
            Date p_pclNowDate;

            if( m_i32IsPrintLogcat != 0 ) Log.i( m_pclCurrentClassNameString, "音频输入线程：开始准备音频输入。" );

            //计算WebRtc定点版和浮点版声学回音消除器的回音延迟。
            {
                HTInteger pclDelay = new HTInteger( 0 );
                int p_i32Temp;

                p_pclLastDate = new Date();

                //跳过刚开始读取到的空输入帧。
                skip:
                while( true )
                {
                    p_pszi16TempInputFrame = new short[( int ) m_i64FrameLength];
                    m_pclAudioRecord.read( p_pszi16TempInputFrame, 0, p_pszi16TempInputFrame.length );

                    for( p_i32Temp = 0; p_i32Temp < p_pszi16TempInputFrame.length; p_i32Temp++ )
                    {
                        if( p_pszi16TempInputFrame[p_i32Temp] != 0 )
                            break skip;
                    }
                }

                p_pclNowDate = new Date();

                m_pclAudioOutputThread.start(); //启动音频输出线程。

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频输入线程：" + "准备耗时：" + ( p_pclNowDate.getTime() - p_pclLastDate.getTime() ) + "，丢弃掉刚开始读取到的空输入帧，现在启动音频输出线程，并开始音频输入循环，为了保证音频输入线程走在输出数据线程的前面。" );

                p_i32Temp = ( int ) ( ( m_i32AudioTrackBufferSize / 2 - m_i64FrameLength ) * 1000 / m_i32SamplingRate + ( p_pclNowDate.getTime() - p_pclLastDate.getTime() ) );
                if( ( m_pclWebRtcAecm != null ) && ( m_pclWebRtcAecm.GetDelay( pclDelay ) == 0 ) && ( pclDelay.m_i32Value == 0 ) ) //如果使用了WebRtc定点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    m_pclWebRtcAecm.SetDelay( p_i32Temp > 80 + 60 ? p_i32Temp - 60 : 80 );
                    m_pclWebRtcAecm.GetDelay( pclDelay );
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输入线程：自适应设置WebRtc定点版声学回音消除器的回音延迟为 " + pclDelay.m_i32Value + " 毫秒。" );
                }
                if( ( m_pclWebRtcAec != null ) && ( m_pclWebRtcAec.GetDelay( pclDelay ) == 0 ) && ( pclDelay.m_i32Value == 0 ) ) //如果使用了WebRtc浮点版声学回音消除器，且需要自适应设置回音的延迟。
                {
                    if( m_i32WebRtcAecIsUseDelayAgnosticMode == 0 ) //如果不使用WebRtc浮点版声学回音消除器的回音延迟不可知模式。
                    {
                        m_pclWebRtcAec.SetDelay( p_i32Temp );
                        m_pclWebRtcAec.GetDelay( pclDelay );
                    }
                    else //如果要使用WebRtc浮点版声学回音消除器的回音延迟不可知模式。
                    {
                        m_pclWebRtcAec.SetDelay( 20 );
                        m_pclWebRtcAec.GetDelay( pclDelay );
                    }
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输入线程：自适应设置WebRtc浮点版声学回音消除器的回音延迟为 " + pclDelay.m_i32Value + " 毫秒。" );
                }
                /*if( ( m_pclSpeexWebRtcAec != null ) && ( m_pclSpeexWebRtcAec.GetWebRtcAecmDelay( pclDelay ) == 0 ) && ( pclDelay == 0 ) ) //如果使用了SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
                {
                    m_pclSpeexWebRtcAec.SetWebRtcAecmDelay( p_i32Temp > 80 + 60 ? p_i32Temp - 60 : 80 );
                    m_pclSpeexWebRtcAec.GetWebRtcAecmDelay( pclDelay );
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输入线程：SpeexWebRtc三重声学回音消除器的WebRtc定点版声学回音消除器自适应设置回音延迟为 " + pclDelay + " 毫秒。" );
                }
                if( ( m_pclSpeexWebRtcAec != null ) && ( m_pclSpeexWebRtcAec.GetWebRtcAecDelay( pclDelay ) == 0 ) && ( pclDelay == 0 ) ) //如果使用了SpeexWebRtc三重声学回音消除器，且WebRtc浮点版声学回音消除器需要自适应设置回音的延迟。
                {
                    if( m_i32SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode == 0 ) //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器不使用回音延迟不可知模式。
                    {
                        m_pclSpeexWebRtcAec.SetWebRtcAecDelay( p_i32Temp );
                        m_pclSpeexWebRtcAec.GetWebRtcAecDelay( pclDelay );
                    }
                    else //如果SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器要使用回音延迟不可知模式。
                    {
                        m_pclSpeexWebRtcAec.SetWebRtcAecDelay( 20 );
                        m_pclSpeexWebRtcAec.GetWebRtcAecDelay( pclDelay );
                    }
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输入线程：SpeexWebRtc三重声学回音消除器的WebRtc浮点版声学回音消除器自适应设置的回音延迟为 " + pclDelay + " 毫秒。" );
                }*/

                p_pclLastDate = p_pclNowDate;
            }

            //开始音频输入循环。
            out:
            while( true )
            {
                p_pszi16TempInputFrame = new short[( int ) m_i64FrameLength];

                //读取本次输入帧。
                m_pclAudioRecord.read( p_pszi16TempInputFrame, 0, p_pszi16TempInputFrame.length );

                p_pclNowDate = new Date();
                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频输入线程：读取耗时：" + ( p_pclNowDate.getTime() - p_pclLastDate.getTime() ) + " 毫秒，" + "输入帧链表元素个数：" + m_pclInputFrameLinkedList.size() + "。" );
                p_pclLastDate = p_pclNowDate;

                //追加本次输入帧到输入帧链表。
                synchronized( m_pclInputFrameLinkedList )
                {
                    m_pclInputFrameLinkedList.addLast( p_pszi16TempInputFrame );
                }

                if( m_i32ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            }

            if( m_i32IsPrintLogcat != 0 ) Log.i( m_pclCurrentClassNameString, "音频输入线程：本线程已退出。" );
        }
    }

    //音频输出线程类。
    private class AudioOutputThread extends Thread
    {
        public int m_i32ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

        //请求本线程退出。
        public void RequireExit()
        {
            m_i32ExitFlag = 1;
        }

        public void run()
        {
            this.setPriority( MAX_PRIORITY ); //设置本线程优先级。
            Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

            short p_pszi16TempOutputData[];
            Date p_pclLastDate;
            Date p_clNowDate;

            p_pclLastDate = new Date();
            if( m_i32IsPrintLogcat != 0 ) Log.i( m_pclCurrentClassNameString, "音频输出线程：开始准备音频输出。" );

            //开始音频输出循环。
            out:
            while( true )
            {
                p_pszi16TempOutputData = new short[( int ) m_i64FrameLength];

                //从音频处理线程获取一个输出帧。
                WriteOutputFrame( p_pszi16TempOutputData );

                //写入本次输出帧。
                m_pclAudioTrack.write( p_pszi16TempOutputData, 0, p_pszi16TempOutputData.length );

                p_clNowDate = new Date();
                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频输出线程：写入耗时：" + ( p_clNowDate.getTime() - p_pclLastDate.getTime() ) + " 毫秒，" + "输出帧链表元素个数：" + m_pclOutputFrameLinkedList.size() + "。" );
                p_pclLastDate = p_clNowDate;

                //追加本次输出帧到输出帧链表。
                synchronized( m_pclOutputFrameLinkedList )
                {
                    m_pclOutputFrameLinkedList.addLast( p_pszi16TempOutputData );
                }

                if( m_i32ExitFlag == 1 ) //如果退出标记为请求退出。
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
                    break out;
                }
            }

            if( m_i32IsPrintLogcat != 0 ) Log.i( m_pclCurrentClassNameString, "音频输出线程：本线程已退出。" );
        }
    }

    //用户定义的相关函数。
    public abstract long UserInit(); //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract long UserProcess(); //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，为0表示成功，为非0表示失败。

    public abstract long UserDestory(); //用户定义的销毁函数，在本线程退出时调用一次，返回值表示是否重新初始化，为0表示直接退出，为非0表示重新初始化。

    public abstract long UserReadInputFrame( short pszi16PcmInputFrame[], short pszi16PcmResultFrame[], int i32VoiceActivityStatus, byte pszi8SpeexInputFrame[], long i64SpeexInputFrameLength, int i32SpeexInputFrameIsNeedTrans ); //用户定义的读取输入帧函数，在读取到一个输入帧并处理完后回调一次，为0表示成功，为非0表示失败。

    public abstract void UserWriteOutputFrame( short pszi16PcmOutputFrame[], byte p_pszi8SpeexOutputFrame[], long p_pszi64SpeexOutputFrameLength[] ); //用户定义的写入输出帧函数，在需要写入一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。

    public abstract void UserGetPcmOutputFrame( short pszi16PcmOutputFrame[] ); //用户定义的获取PCM格式输出帧函数，在解码完一个输出帧时回调一次。注意：本函数不是在音频处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致回音消除失败。

    //请求本线程退出。
    public void RequireExit()
    {
        m_i32ExitFlag = 1;
    }

    //初始化音频处理线程类对象。
    public long Init( Context pclApplicationContext, int i32SamplingRate )
    {
        long p_i64Result = -1; //存放本函数执行结果的值，0表示成功，非0表示失败。

        out:
        {
            //判断各个变量是否正确。
            if( ( pclApplicationContext == null ) || //如果上下文类对象不正确。
                    ( i32SamplingRate != 8000 ) && ( i32SamplingRate != 16000 ) && ( i32SamplingRate != 32000 ) ) //如果采样频率不正确。
            {
                break out;
            }

            m_pclApplicationContext = pclApplicationContext; //设置应用程序上下文类对象的内存指针。

            if( i32SamplingRate == 8000 )
            {
                m_i32SamplingRate = 8000; //设置采样频率。
                m_i64FrameLength = 160; //设置帧的数据长度。
            }
            else if( i32SamplingRate == 16000 )
            {
                m_i32SamplingRate = 16000; //设置采样频率。
                m_i64FrameLength = 320; //设置帧的数据长度。
            }
            else
            {
                m_i32SamplingRate = 32000; //设置采样频率。
                m_i64FrameLength = 640; //设置帧的数据长度。
            }

            p_i64Result = 0;
        }

        return p_i64Result;
    }

    //设置打印日志。
    public void SetPrintLogcat( int i32IsPrintLogcat )
    {
        m_i32IsPrintLogcat = i32IsPrintLogcat;
    }

    //设置使用唤醒锁。
    public void SetUseWakeLock( int i32IsUseWakeLock )
    {
        m_i32IsUseWakeLock = i32IsUseWakeLock;
    }

    //设置不使用声学回音消除器。
    public void SetUseNoAec()
    {
        m_i32UseWhatAec = 0;
    }

    //设置使用Speex声学回音消除器。
    public void SetUseSpeexAec( int i32FilterLength, int i32IsSaveMemoryFile, String pclMemoryFileFullPath )
    {
        m_i32UseWhatAec = 1;
        m_i32SpeexAecFilterLength = i32FilterLength;
        m_i32SpeexAecIsSaveMemoryFile = i32IsSaveMemoryFile;
        m_pclSpeexAecMemoryFileFullPath = pclMemoryFileFullPath;
    }

    //设置使用WebRtc定点版声学回音消除器。
    public void SetUseWebRtcAecm( int i32IsUseCNGMode, int i32EchoMode, int i32Delay )
    {
        m_i32UseWhatAec = 2;
        m_i32WebRtcAecmIsUseCNGMode = i32IsUseCNGMode;
        m_i32WebRtcAecmEchoMode = i32EchoMode;
        m_i32WebRtcAecmDelay = i32Delay;
    }

    //设置使用WebRtc浮点版声学回音消除器。
    public void SetUseWebRtcAec( int i32EchoMode, int i32Delay, int i32IsUseDelayAgnosticMode, int i32IsUseAdaptiveAdjustDelay, int i32IsSaveMemoryFile, String pclMemoryFileFullPath )
    {
        m_i32UseWhatAec = 3;
        m_i32WebRtcAecEchoMode = i32EchoMode;
        m_i32WebRtcAecDelay = i32Delay;
        m_i32WebRtcAecIsUseDelayAgnosticMode = i32IsUseDelayAgnosticMode;
        m_i32WebRtcAecIsUseAdaptiveAdjustDelay = i32IsUseAdaptiveAdjustDelay;
        m_i32WebRtcAecIsSaveMemoryFile = i32IsSaveMemoryFile;
        m_pclWebRtcAecMemoryFileFullPath = pclMemoryFileFullPath;
    }

    //设置使用SpeexWebRtc三重声学回音消除器。
    public void SetUseSpeexWebRtcAec( int i32WorkMode, int i32FilterLength, float fSpeexAecEchoMultiple, int i32SpeexAecEchoSuppress, int i32SpeexAecEchoSuppressActive, int i32WebRtcAecmIsUseCNGMode, int i32WebRtcAecmEchoMode, int i32WebRtcAecmDelay, int i32WebRtcAecEchoMode, int i32WebRtcAecDelay, int i32WebRtcAecIsUseDelayAgnosticMode, int i32WebRtcAecIsUseAdaptiveAdjustDelay )
    {
        m_i32UseWhatAec = 4;
        m_i32SpeexWebRtcAecWorkMode = i32WorkMode;
        m_i32SpeexWebRtcAecSpeexAecFilterLength = i32FilterLength;
        m_fSpeexWebRtcAecSpeexAecEchoMultiple = fSpeexAecEchoMultiple;
        m_i32SpeexWebRtcAecSpeexAecEchoSuppress = i32SpeexAecEchoSuppress;
        m_i32SpeexWebRtcAecSpeexAecEchoSuppressActive = i32SpeexAecEchoSuppressActive;
        m_i32SpeexWebRtcAecWebRtcAecmIsUseCNGMode = i32WebRtcAecmIsUseCNGMode;
        m_i32SpeexWebRtcAecWebRtcAecmEchoMode = i32WebRtcAecmEchoMode;
        m_i32SpeexWebRtcAecWebRtcAecmDelay = i32WebRtcAecmDelay;
        m_i32SpeexWebRtcAecWebRtcAecEchoMode = i32WebRtcAecEchoMode;
        m_i32SpeexWebRtcAecWebRtcAecDelay = i32WebRtcAecDelay;
        m_i32SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode = i32WebRtcAecIsUseDelayAgnosticMode;
        m_i32SpeexWebRtcAecWebRtcAecIsUseAdaptiveAdjustDelay = i32WebRtcAecIsUseAdaptiveAdjustDelay;
    }

    //设置不使用噪音抑制器。
    public void SetUseNoNs()
    {
        m_i32UseWhatNs = 0;
    }

    //设置使用Speex预处理器的噪音抑制。
    public void SetUseSpeexPreprocessorNs( int i32IsUseNs, int i32NoiseSuppress, int i32IsUseDereverberation, int i32IsUseRec, float m_fEchoMultiple, int i32EchoSuppress, int i32EchoSuppressActive )
    {
        m_i32UseWhatNs = 1;
        m_i32SpeexPreprocessorIsUseNs = i32IsUseNs;
        m_i32SpeexPreprocessorNoiseSuppress = i32NoiseSuppress;
        m_i32SpeexPreprocessorIsUseDereverberation = i32IsUseDereverberation;
        m_i32SpeexPreprocessorIsUseRec = i32IsUseRec;
        m_fSpeexPreprocessorEchoMultiple = m_fEchoMultiple;
        m_i32SpeexPreprocessorEchoSuppress = i32EchoSuppress;
        m_i32SpeexPreprocessorEchoSuppressActive = i32EchoSuppressActive;
    }

    //设置使用WebRtc定点版噪音抑制器。
    public void SetUseWebRtcNsx( int i32PolicyMode )
    {
        m_i32UseWhatNs = 2;
        m_i32WebRtcNsxPolicyMode = i32PolicyMode;
    }

    //设置使用WebRtc定点版噪音抑制器。
    public void SetUseWebRtcNs( int i32PolicyMode )
    {
        m_i32UseWhatNs = 3;
        m_i32WebRtcNsPolicyMode = i32PolicyMode;
    }

    //设置使用RNNoise噪音抑制器。
    public void SetUseRNNoise()
    {
        m_i32UseWhatNs = 4;
    }

    //设置Speex预处理器的其他功能。
    public void SetSpeexPreprocessorOther( int i32IsUseOther, int i32IsUseVad, int i32VadProbStart, int i32VadProbContinue, int i32IsUseAgc, int i32AgcLevel, int i32AgcIncrement, int i32AgcDecrement, int i32AgcMaxGain )
    {
        m_i32IsUseSpeexPreprocessorOther = i32IsUseOther;
        m_i32SpeexPreprocessorIsUseVad = i32IsUseVad;
        m_i32SpeexPreprocessorVadProbStart = i32VadProbStart;
        m_i32SpeexPreprocessorVadProbContinue = i32VadProbContinue;
        m_i32SpeexPreprocessorIsUseAgc = i32IsUseAgc;
        m_i32SpeexPreprocessorAgcIncrement = i32AgcIncrement;
        m_i32SpeexPreprocessorAgcDecrement = i32AgcDecrement;
        m_i32SpeexPreprocessorAgcLevel = i32AgcLevel;
        m_i32SpeexPreprocessorAgcMaxGain = i32AgcMaxGain;
    }

    //设置使用PCM原始数据。
    public void SetUsePcm()
    {
        m_i32UseWhatCodec = 0;
    }

    //设置使用Speex编解码器。
    public void SetUseSpeexCodec( int i32EncoderUseCbrOrVbr, int i32EncoderQuality, int i32EncoderComplexity, int i32EncoderPlcExpectedLossRate, int i32DecoderIsUsePerceptualEnhancement )
    {
        m_i32UseWhatCodec = 1;
        m_i32SpeexCodecEncoderUseCbrOrVbr = i32EncoderUseCbrOrVbr;
        m_i32SpeexCodecEncoderQuality = i32EncoderQuality;
        m_i32SpeexCodecEncoderComplexity = i32EncoderComplexity;
        m_i32SpeexCodecEncoderPlcExpectedLossRate = i32EncoderPlcExpectedLossRate;
        m_i32SpeexCodecDecoderIsUsePerceptualEnhancement = i32DecoderIsUsePerceptualEnhancement;
    }

    //设置使用Opus编解码器。
    public void SetUseOpusCodec()
    {
        m_i32UseWhatCodec = 2;
    }

    //设置保存音频到文件。
    public void SetSaveAudioToFile( int i32IsSaveAudioToFile, String pclAudioInputFileFullPath, String pclAudioOutputFileFullPath, String pclAudioResultFileFullPath )
    {
        m_i32IsSaveAudioToFile = i32IsSaveAudioToFile;
        m_pclAudioInputFileFullPath = pclAudioInputFileFullPath;
        m_pclAudioOutputFileFullPath = pclAudioOutputFileFullPath;
        m_pclAudioResultFileFullPath = pclAudioResultFileFullPath;
    }

    //写入PCM格式输出帧到音频输出线程。
    public void WriteOutputFrame( short pszi16PcmOutputFrame[] )
    {
        long p_i64Temp;

        //调用用户定义的写入输出帧函数，并解码成PCM原始数据。
        switch( m_i32UseWhatCodec ) //使用什么编解码器。
        {
            case 0: //如果使用PCM原始数据。
            {
                //调用用户定义的写入输出帧函数。
                UserWriteOutputFrame( pszi16PcmOutputFrame, null, null );

                break;
            }
            case 1: //如果使用Speex编解码器。
            {
                byte p_pszi8SpeexOutputFrame[] = new byte[( int ) m_i64FrameLength]; //Speex格式输出帧。
                long p_pszi64SpeexOutputFrameLength[] = new long[1]; //Speex格式输出帧的内存长度，单位字节。

                //调用用户定义的写入输出帧函数。
                UserWriteOutputFrame( null, p_pszi8SpeexOutputFrame, p_pszi64SpeexOutputFrameLength );

                //使用Speex解码器。
                if( p_pszi64SpeexOutputFrameLength[0] != 0 ) //如果本次Speex格式输出帧接收到了。
                {
                    p_i64Temp = m_pclSpeexDecoder.Process( p_pszi8SpeexOutputFrame, ( int ) p_pszi64SpeexOutputFrameLength[0], pszi16PcmOutputFrame );
                }
                else //如果本次Speex格式输出帧丢失了。
                {
                    p_i64Temp = m_pclSpeexDecoder.Process( null, 0, pszi16PcmOutputFrame );
                }
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "使用Speex解码器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "使用Speex解码器失败。返回值：" + p_i64Temp );
                }
                break;
            }
            case 2: //如果使用Opus编解码器。
            {
                if( m_i32IsPrintLogcat != 0 )
                    Log.e( m_pclCurrentClassNameString, "暂不支持使用Opus解码器。" );
            }
        }

        //调用用户定义的获取PCM格式输出帧函数。
        UserGetPcmOutputFrame( pszi16PcmOutputFrame );
    }

    //本线程执行函数。
    public void run()
    {
        this.setPriority( this.MAX_PRIORITY ); //设置本线程优先级。
        Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

        int p_i32Temp;
        long p_i64Temp;

        reinit:
        while( true )
        {
            out:
            {
                m_i32ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码。

                //初始化唤醒锁类对象。
                if( m_i32IsUseWakeLock != 0 )
                {
                    //初始化接近息屏唤醒锁类对象。
                    m_clProximityScreenOffWakeLock = ( ( PowerManager ) m_pclApplicationContext.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_pclCurrentClassNameString );
                    if( m_clProximityScreenOffWakeLock != null )
                    {
                        m_clProximityScreenOffWakeLock.acquire();

                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化接近息屏唤醒锁类对象成功。" );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化接近息屏唤醒锁类对象失败。" );
                        break out;
                    }

                    //初始化屏幕键盘全亮唤醒锁类对象。
                    m_clFullWakeLock = ( ( PowerManager ) m_pclApplicationContext.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_pclCurrentClassNameString );
                    if( m_clFullWakeLock != null )
                    {
                        m_clFullWakeLock.acquire();

                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化屏幕键盘全亮唤醒锁类对象成功。" );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化屏幕键盘全亮唤醒锁类对象成功。" );
                        break out;
                    }
                }

                //调用用户定义的初始化函数。
                p_i64Temp = UserInit();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的初始化函数成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的初始化函数失败。返回值：" + p_i64Temp );
                    break out;
                }

                //初始化音频输入类对象。
                try
                {
                    m_i32AudioRecordBufferSize = AudioRecord.getMinBufferSize( m_i32SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                    m_i32AudioRecordBufferSize = ( int ) ( m_i32AudioRecordBufferSize > m_i64FrameLength * 2 ? m_i32AudioRecordBufferSize : m_i64FrameLength * 2 );
                    m_pclAudioRecord = new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            m_i32SamplingRate,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            m_i32AudioRecordBufferSize
                    );
                    if( m_pclAudioRecord.getState() == AudioRecord.STATE_INITIALIZED )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化音频输入类对象成功。音频输入缓冲区大小：" + m_i32AudioRecordBufferSize );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化音频输入类对象失败。" );
                        break out;
                    }
                }
                catch( IllegalArgumentException e )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化音频输入类对象失败。原因：" + e.getMessage() );
                    break out;
                }

                //用第一种方法初始化音频输出类对象。
                try
                {
                    m_i32AudioTrackBufferSize = ( int ) m_i64FrameLength * 2;
                    m_pclAudioTrack = new AudioTrack( AudioManager.STREAM_MUSIC,
                            m_i32SamplingRate,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            m_i32AudioTrackBufferSize,
                            AudioTrack.MODE_STREAM );
                    if( m_pclAudioTrack.getState() == AudioTrack.STATE_INITIALIZED )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：用第一种方法初始化音频输出类对象成功。音频输出缓冲区大小：" + m_i32AudioTrackBufferSize );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：用第一种方法初始化音频输出类对象失败。" );
                        m_pclAudioTrack.release();
                        m_pclAudioTrack = null;
                    }
                }
                catch( IllegalArgumentException e )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：用第一种方法初始化音频输出类对象失败。原因：" + e.getMessage() );
                }

                //用第二种方法初始化音频输出类对象。
                if( m_pclAudioTrack == null )
                {
                    try
                    {
                        m_i32AudioTrackBufferSize = AudioTrack.getMinBufferSize( m_i32SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
                        m_pclAudioTrack = new AudioTrack( AudioManager.STREAM_MUSIC,
                                m_i32SamplingRate,
                                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                m_i32AudioTrackBufferSize,
                                AudioTrack.MODE_STREAM );
                        if( m_pclAudioTrack.getState() == AudioTrack.STATE_INITIALIZED )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：用第二种方法初始化音频输出类对象成功。音频输出缓冲区大小：" + m_i32AudioTrackBufferSize );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：用第二种方法初始化音频输出类对象失败。" );
                            break out;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：用第二种方法初始化音频输出类对象失败。原因：" + e.getMessage() );
                        break out;
                    }
                }

                //初始化声学回音消除器对象。
                switch( m_i32UseWhatAec )
                {
                    case 0: //如果不使用声学回音消除器。
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用声学回音消除器。" );
                        break;
                    }
                    case 1: //如果使用Speex声学回音消除器。
                    {
                        //读取Speex声学回音消除器的内存块到文件。
                        if( ( m_i32SpeexAecIsSaveMemoryFile != 0 ) && ( new File( m_pclSpeexAecMemoryFileFullPath ).exists() ) )
                        {
                            byte p_pszi8SpeexAecMemory[];
                            long p_i64SpeexAecMemoryLength;
                            FileInputStream p_pclSpeexAecMemoryFileInputStream = null;

                            ReadSpeexAecMemoryFile:
                            {
                                try
                                {
                                    p_pclSpeexAecMemoryFileInputStream = new FileInputStream( m_pclSpeexAecMemoryFileFullPath );
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器内存块文件 " + m_pclSpeexAecMemoryFileFullPath + " 的文件输入流对象成功。" );
                                }
                                catch( FileNotFoundException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器内存块文件 " + m_pclSpeexAecMemoryFileFullPath + " 的文件输入流对象失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }

                                p_pszi8SpeexAecMemory = new byte[8];

                                //读取Speex声学回音消除器内存块文件的采样频率。
                                try
                                {
                                    if( p_pclSpeexAecMemoryFileInputStream.read( p_pszi8SpeexAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有采样频率。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的采样频率成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的采样频率失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8SpeexAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8SpeexAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8SpeexAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8SpeexAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32SamplingRate )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：Speex声学回音消除器内存块文件中的采样频率已被修改，需要重新初始化。" );
                                    break ReadSpeexAecMemoryFile;
                                }

                                //读取Speex声学回音消除器内存块文件的帧数据长度。
                                try
                                {
                                    if( p_pclSpeexAecMemoryFileInputStream.read( p_pszi8SpeexAecMemory, 0, 8 ) != 8 )
                                    {
                                        throw new IOException( "文件中没有帧的数据长度。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的帧数据长度成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的帧数据长度失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }
                                p_i64Temp = ( ( long ) p_pszi8SpeexAecMemory[0] & 0xFF ) + ( ( ( long ) p_pszi8SpeexAecMemory[1] & 0xFF ) << 8 ) + ( ( ( long ) p_pszi8SpeexAecMemory[2] & 0xFF ) << 16 ) + ( ( ( long ) p_pszi8SpeexAecMemory[3] & 0xFF ) << 24 ) + ( ( ( long ) p_pszi8SpeexAecMemory[4] & 0xFF ) << 32 ) + ( ( ( long ) p_pszi8SpeexAecMemory[5] & 0xFF ) << 40 ) + ( ( ( long ) p_pszi8SpeexAecMemory[6] & 0xFF ) << 48 ) + ( ( ( long ) p_pszi8SpeexAecMemory[7] & 0xFF ) << 56 );
                                if( p_i64Temp != m_i64FrameLength )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：Speex声学回音消除器内存块文件中的帧数据长度已被修改，需要重新初始化。" );
                                    break ReadSpeexAecMemoryFile;
                                }

                                //读取Speex声学回音消除器内存块文件的滤波器数据长度。
                                try
                                {
                                    if( p_pclSpeexAecMemoryFileInputStream.read( p_pszi8SpeexAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有滤波器的数据长度。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的滤波器数据长度成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件的滤波器数据长度失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8SpeexAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8SpeexAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8SpeexAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8SpeexAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32SpeexAecFilterLength )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：Speex声学回音消除器内存块文件中的滤波器数据长度已被修改，需要重新初始化。" );
                                    break ReadSpeexAecMemoryFile;
                                }

                                //跳过Speex声学回音消除器内存块文件的有语音活动帧总数。
                                try
                                {
                                    if( p_pclSpeexAecMemoryFileInputStream.skip( 8 ) != 8 )
                                    {
                                        throw new IOException( "文件中没有有语音活动帧总数。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：跳过Speex声学回音消除器内存块文件的有语音活动帧总数成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：跳过Speex声学回音消除器内存块文件的有语音活动帧总数失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }

                                //读取Speex声学回音消除器内存块文件的内存块。
                                try
                                {
                                    p_i64SpeexAecMemoryLength = p_pclSpeexAecMemoryFileInputStream.available();
                                    if( p_i64SpeexAecMemoryLength <= 0 )
                                    {
                                        throw new IOException( "文件中没有内存块。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：获取Speex声学回音消除器内存块文件的内存块数据长度成功。内存块数据长度：" + p_i64SpeexAecMemoryLength );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：获取Speex声学回音消除器内存块文件的内存块数据长度失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }
                                p_pszi8SpeexAecMemory = new byte[( int ) p_i64SpeexAecMemoryLength];
                                try
                                {
                                    if( p_pclSpeexAecMemoryFileInputStream.read( p_pszi8SpeexAecMemory, 0, ( int ) p_i64SpeexAecMemoryLength ) != p_i64SpeexAecMemoryLength )
                                    {
                                        throw new IOException( "文件中没有内存块。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件中的内存块成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器内存块文件中的内存块失败。原因：" + e.toString() );
                                    break ReadSpeexAecMemoryFile;
                                }

                                m_pclSpeexAec = new SpeexAec();
                                p_i64Temp = m_pclSpeexAec.InitFromMemory( p_pszi8SpeexAecMemory, p_i64SpeexAecMemoryLength );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：根据Speex声学回音消除器内存块来初始化Speex声学回音消除器类对象成功。返回值：" + p_i64Temp );
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：根据Speex声学回音消除器内存块来初始化Speex声学回音消除器类对象失败，重新初始化。返回值：" + p_i64Temp );
                                    break ReadSpeexAecMemoryFile;
                                }
                            }

                            //销毁Speex声学回音消除器内存块文件的文件输入流对象。
                            if( p_pclSpeexAecMemoryFileInputStream != null )
                            {
                                try
                                {
                                    p_pclSpeexAecMemoryFileInputStream.close();
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器内存块文件的文件输入流对象成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器内存块文件的文件输入流对象失败。原因：" + e.toString() );
                                }
                            }
                        }

                        if( m_pclSpeexAec == null )
                        {
                            m_pclSpeexAec = new SpeexAec();
                            p_i64Temp = m_pclSpeexAec.Init( m_i32SamplingRate, m_i64FrameLength, m_i32SpeexAecFilterLength );
                            if( p_i64Temp == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex声学回音消除器类对象成功。返回值：" + p_i64Temp );
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex声学回音消除器类对象失败。返回值：" + p_i64Temp );
                                break out;
                            }
                        }
                        break;
                    }
                    case 2: //如果使用WebRtc定点版声学回音消除器。
                    {
                        m_pclWebRtcAecm = new WebRtcAecm();
                        p_i64Temp = m_pclWebRtcAecm.Init( m_i32SamplingRate, m_i64FrameLength, m_i32WebRtcAecmIsUseCNGMode, m_i32WebRtcAecmEchoMode, m_i32WebRtcAecmDelay );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc定点版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc定点版声学回音消除器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 3: //如果使用WebRtc浮点版声学回音消除器。
                    {
                        //读取WebRtc浮点版声学回音消除器的内存块到文件。
                        if( ( m_i32WebRtcAecIsSaveMemoryFile != 0 ) && ( new File( m_pclWebRtcAecMemoryFileFullPath ).exists() ) )
                        {
                            byte p_pszi8WebRtcAecMemory[];
                            long p_i64WebRtcAecMemoryLength;
                            FileInputStream p_pclWebRtcAecMemoryFileInputStream = null;

                            ReadWebRtcAecMemoryFile:
                            {
                                try
                                {
                                    p_pclWebRtcAecMemoryFileInputStream = new FileInputStream( m_pclWebRtcAecMemoryFileFullPath );
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器内存块文件 " + m_pclWebRtcAecMemoryFileFullPath + " 的文件输入流对象成功。" );
                                }
                                catch( FileNotFoundException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器内存块文件 " + m_pclWebRtcAecMemoryFileFullPath + " 的文件输入流对象失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                p_pszi8WebRtcAecMemory = new byte[8];

                                //读取WebRtc浮点版声学回音消除器内存块文件的采样频率。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有采样频率。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的采样频率成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的采样频率失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32SamplingRate )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的采样频率已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的帧数据长度。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 8 ) != 8 )
                                    {
                                        throw new IOException( "文件中没有帧的数据长度。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的帧数据长度成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的帧数据长度失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i64Temp = ( ( long ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( long ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[4] & 0xFF ) << 32 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[5] & 0xFF ) << 40 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[6] & 0xFF ) << 48 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[7] & 0xFF ) << 56 );
                                if( p_i64Temp != m_i64FrameLength )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的帧数据长度已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的消除模式。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有消除模式。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的消除模式成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的消除模式失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32WebRtcAecEchoMode )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的消除模式已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的回音延迟。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有消除模式。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的回音延迟成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的回音延迟失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32WebRtcAecDelay )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的回音延迟已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的是否使用回音延迟不可知模式。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有消除模式。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的是否使用回音延迟不可知模式成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的是否使用回音延迟不可知模式失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32WebRtcAecIsUseDelayAgnosticMode )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的是否使用回音延迟不可知模式已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的是否使用自适应调节回音的延迟。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 4 ) != 4 )
                                    {
                                        throw new IOException( "文件中没有消除模式。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的是否使用自适应调节回音的延迟成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件的是否使用自适应调节回音的延迟失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_i32Temp = ( ( int ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( int ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( int ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 );
                                if( p_i32Temp != m_i32WebRtcAecIsUseAdaptiveAdjustDelay )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器内存块文件中的是否使用自适应调节回音的延迟已被修改，需要重新初始化。" );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //跳过WebRtc浮点版声学回音消除器内存块文件的有语音活动帧总数。
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.skip( 8 ) != 8 )
                                    {
                                        throw new IOException( "文件中没有有语音活动帧总数。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：跳过WebRtc浮点版声学回音消除器内存块文件的有语音活动帧总数成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：跳过WebRtc浮点版声学回音消除器内存块文件的有语音活动帧总数失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                //读取WebRtc浮点版声学回音消除器内存块文件的内存块。
                                try
                                {
                                    p_i64WebRtcAecMemoryLength = p_pclWebRtcAecMemoryFileInputStream.available();
                                    if( p_i64WebRtcAecMemoryLength <= 0 )
                                    {
                                        throw new IOException( "文件中没有内存块。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：获取WebRtc浮点版声学回音消除器内存块文件的内存块数据长度成功。内存块数据长度：" + p_i64WebRtcAecMemoryLength );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：获取WebRtc浮点版声学回音消除器内存块文件的内存块数据长度失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }
                                p_pszi8WebRtcAecMemory = new byte[( int ) p_i64WebRtcAecMemoryLength];
                                try
                                {
                                    if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, ( int ) p_i64WebRtcAecMemoryLength ) != p_i64WebRtcAecMemoryLength )
                                    {
                                        throw new IOException( "文件中没有内存块。" );
                                    }
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件中的内存块成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器内存块文件中的内存块失败。原因：" + e.toString() );
                                    break ReadWebRtcAecMemoryFile;
                                }

                                m_pclWebRtcAec = new WebRtcAec();
                                p_i64Temp = m_pclWebRtcAec.InitFromMemory( p_pszi8WebRtcAecMemory, p_i64WebRtcAecMemoryLength );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：根据WebRtc浮点版声学回音消除器内存块来初始化WebRtc浮点版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：根据WebRtc浮点版声学回音消除器内存块来初始化WebRtc浮点版声学回音消除器类对象失败，重新初始化。返回值：" + p_i64Temp );
                                    break ReadWebRtcAecMemoryFile;
                                }
                            }

                            //销毁WebRtc浮点版声学回音消除器内存块文件的文件输入流对象。
                            if( p_pclWebRtcAecMemoryFileInputStream != null )
                            {
                                try
                                {
                                    p_pclWebRtcAecMemoryFileInputStream.close();
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器内存块文件的文件输入流对象成功。" );
                                }
                                catch( IOException e )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器内存块文件的文件输入流对象失败。原因：" + e.toString() );
                                }
                            }
                        }

                        if( m_pclWebRtcAec == null )
                        {
                            m_pclWebRtcAec = new WebRtcAec();
                            p_i64Temp = m_pclWebRtcAec.Init( m_i32SamplingRate, m_i64FrameLength, m_i32WebRtcAecEchoMode, m_i32WebRtcAecDelay, m_i32WebRtcAecIsUseDelayAgnosticMode, m_i32WebRtcAecIsUseAdaptiveAdjustDelay );
                            if( p_i64Temp == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc浮点版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc浮点版声学回音消除器类对象失败。返回值：" + p_i64Temp );
                                break out;
                            }
                        }
                        break;
                    }
                    case 4: //如果使用SpeexWebRtc三重声学回音消除器。
                    {
                        m_pclSpeexWebRtcAec = new SpeexWebRtcAec();
                        p_i64Temp = m_pclSpeexWebRtcAec.Init( m_i32SamplingRate, m_i64FrameLength, m_i32SpeexWebRtcAecWorkMode, m_i32SpeexWebRtcAecSpeexAecFilterLength, m_fSpeexWebRtcAecSpeexAecEchoMultiple, m_i32SpeexWebRtcAecSpeexAecEchoSuppress, m_i32SpeexWebRtcAecSpeexAecEchoSuppressActive, m_i32SpeexWebRtcAecWebRtcAecmIsUseCNGMode, m_i32SpeexWebRtcAecWebRtcAecmEchoMode, m_i32SpeexWebRtcAecWebRtcAecmDelay, m_i32SpeexWebRtcAecWebRtcAecEchoMode, m_i32SpeexWebRtcAecWebRtcAecDelay, m_i32SpeexWebRtcAecWebRtcAecIsUseDelayAgnosticMode, m_i32SpeexWebRtcAecWebRtcAecIsUseAdaptiveAdjustDelay );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化SpeexWebRtc三重声学回音消除器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化SpeexWebRtc三重声学回音消除器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                }

                //初始化噪音抑制器对象。
                switch( m_i32UseWhatNs )
                {
                    case 0: //如果不使用噪音抑制器。
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用噪音抑制器。" );
                        break;
                    }
                    case 1: //如果使用Speex预处理器的噪音抑制。
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：稍后在初始化Speex预处理器时一起初始化Speex预处理器的噪音抑制。" );
                        break;
                    }
                    case 2: //如果使用WebRtc定点版噪音抑制器。
                    {
                        m_pclWebRtcNsx = new WebRtcNsx();
                        p_i64Temp = m_pclWebRtcNsx.Init( m_i32SamplingRate, m_i64FrameLength, m_i32WebRtcNsxPolicyMode );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc定点版噪音抑制器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc定点版噪音抑制器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 3: //如果使用WebRtc浮点版噪音抑制器。
                    {
                        m_pclWebRtcNs = new WebRtcNs();
                        p_i64Temp = m_pclWebRtcNs.Init( m_i32SamplingRate, m_i64FrameLength, m_i32WebRtcNsxPolicyMode );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc浮点版噪音抑制器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc浮点版噪音抑制器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 4: //如果使用RNNoise噪音抑制器。
                    {
                        m_pclRNNoise = new RNNoise();
                        p_i64Temp = m_pclRNNoise.Init( m_i32SamplingRate, m_i64FrameLength );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化RNNoise噪音抑制器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化RNNoise噪音抑制器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                }

                //初始化Speex预处理器类对象。
                if( ( m_i32UseWhatNs == 1 ) || ( m_i32IsUseSpeexPreprocessorOther != 0 ) )
                {
                    if( m_i32UseWhatNs != 1 )
                    {
                        m_i32SpeexPreprocessorIsUseNs = 0;
                        m_i32SpeexPreprocessorIsUseDereverberation = 0;
                        m_i32SpeexPreprocessorIsUseRec = 0;
                    }

                    if( m_i32IsUseSpeexPreprocessorOther == 0 )
                    {
                        m_i32SpeexPreprocessorIsUseVad = 0;
                        m_i32SpeexPreprocessorIsUseAgc = 0;
                    }

                    m_pclSpeexPreprocessor = new SpeexPreprocessor();
                    if( m_pclSpeexAec != null )
                        p_i64Temp = m_pclSpeexPreprocessor.Init( m_i32SamplingRate, m_i64FrameLength, m_i32SpeexPreprocessorIsUseNs, m_i32SpeexPreprocessorNoiseSuppress, m_i32SpeexPreprocessorIsUseDereverberation, m_i32SpeexPreprocessorIsUseVad, m_i32SpeexPreprocessorVadProbStart, m_i32SpeexPreprocessorVadProbContinue, m_i32SpeexPreprocessorIsUseAgc, m_i32SpeexPreprocessorAgcLevel, m_i32SpeexPreprocessorAgcIncrement, m_i32SpeexPreprocessorAgcDecrement, m_i32SpeexPreprocessorAgcMaxGain, m_i32SpeexPreprocessorIsUseRec, m_pclSpeexAec.GetPoint(), m_fSpeexPreprocessorEchoMultiple, m_i32SpeexPreprocessorEchoSuppress, m_i32SpeexPreprocessorEchoSuppressActive );
                    else
                        p_i64Temp = m_pclSpeexPreprocessor.Init( m_i32SamplingRate, m_i64FrameLength, m_i32SpeexPreprocessorIsUseNs, m_i32SpeexPreprocessorNoiseSuppress, m_i32SpeexPreprocessorIsUseDereverberation, m_i32SpeexPreprocessorIsUseVad, m_i32SpeexPreprocessorVadProbStart, m_i32SpeexPreprocessorVadProbContinue, m_i32SpeexPreprocessorIsUseAgc, m_i32SpeexPreprocessorAgcLevel, m_i32SpeexPreprocessorAgcIncrement, m_i32SpeexPreprocessorAgcDecrement, m_i32SpeexPreprocessorAgcMaxGain, 0, 0, 0, 0, 0 );
                    if( p_i64Temp == 0 )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象成功。返回值：" + p_i64Temp );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象失败。返回值：" + p_i64Temp );
                        break out;
                    }
                }

                //初始化编解码器对象。
                switch( m_i32UseWhatCodec )
                {
                    case 0: //如果使用PCM原始数据。
                    {
                        //什么都不要做。
                        break;
                    }
                    case 1: //如果使用Speex编解码器。
                    {
                        m_pclSpeexEncoder = new SpeexEncoder();
                        p_i64Temp = m_pclSpeexEncoder.Init( m_i32SamplingRate, m_i32SpeexCodecEncoderUseCbrOrVbr, m_i32SpeexCodecEncoderQuality, m_i32SpeexCodecEncoderComplexity, m_i32SpeexCodecEncoderPlcExpectedLossRate );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex编码器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex编码器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }

                        m_pclSpeexDecoder = new SpeexDecoder();
                        p_i64Temp = m_pclSpeexDecoder.Init( m_i32SamplingRate, m_i32SpeexCodecDecoderIsUsePerceptualEnhancement );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex解码器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex解码器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 2: //如果使用Opus编解码器。
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：暂不支持使用Opus编解码器。" );
                        break out;
                    }
                }

                //初始化各个音频文件的文件输出流对象。
                if( m_i32IsSaveAudioToFile != 0 )
                {
                    //创建并初始化音频输入Wave文件写入器对象。
                    m_pclAudioInputWaveFileWriter = new WaveFileWriter();
                    if( m_pclAudioInputWaveFileWriter.Init( ( m_pclAudioInputFileFullPath + "\0" ).getBytes(), ( short ) 1, m_i32SamplingRate, 16 ) == 0 )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "创建音频输入文件 " + m_pclAudioInputFileFullPath + " 的Wave文件写入器对象成功。" );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "创建音频输入文件 " + m_pclAudioInputFileFullPath + " 的Wave文件写入器对象失败。" );
                        break out;
                    }

                    //创建并初始化音频输出Wave文件写入器对象。
                    m_pclAudioOutputWaveFileWriter = new WaveFileWriter();
                    if( m_pclAudioOutputWaveFileWriter.Init( ( m_pclAudioOutputFileFullPath + "\0" ).getBytes(), ( short ) 1, m_i32SamplingRate, 16 ) == 0 )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "创建音频输出文件 " + m_pclAudioOutputFileFullPath + " 的Wave文件写入器对象成功。" );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "创建音频输出文件 " + m_pclAudioOutputFileFullPath + " 的Wave文件写入器对象失败。" );
                        break out;
                    }

                    //创建并初始化音频结果Wave文件写入器对象。
                    m_pclAudioResultWaveFileWriter = new WaveFileWriter();
                    if( m_pclAudioResultWaveFileWriter.Init( ( m_pclAudioResultFileFullPath + "\0" ).getBytes(), ( short ) 1, m_i32SamplingRate, 16 ) == 0 )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "创建音频结果文件 " + m_pclAudioResultFileFullPath + " 的Wave文件写入器对象成功。" );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "创建音频结果文件 " + m_pclAudioResultFileFullPath + " 的Wave文件写入器对象失败。" );
                        break out;
                    }
                }

                //初始化各个链表类对象。
                m_pclInputFrameLinkedList = new LinkedList< short[] >(); //初始化输入帧链表类对象。
                m_pclOutputFrameLinkedList = new LinkedList< short[] >(); //初始化输出帧链表类对象。

                //初始化各个线程类对象。
                m_pclAudioInputThread = new AudioInputThread(); //初始化音频输入线程类对象。
                m_pclAudioOutputThread = new AudioOutputThread(); //初始化音频输出线程类对象。

                m_pclAudioRecord.startRecording(); //让音频输入类对象开始录音。
                m_pclAudioTrack.play(); //让音频输出类对象开始播放。

                //启动音频输入线程，让音频输入线程去启动音频输出线程。
                m_pclAudioInputThread.start();

                m_i32ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码。
                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：音频处理线程初始化完毕，正式开始处理音频。" );

                //以下变量要在初始化以后再声明才行。
                short p_pszi16PcmInputFrame[]; //PCM格式输入帧。
                short p_pszi16PcmOutputFrame[]; //PCM格式音频输出帧。
                short p_pszi16PcmResultFrame[] = new short[( int ) m_i64FrameLength]; //PCM格式结果帧。
                short p_pszi16PcmTempFrame[] = new short[( int ) m_i64FrameLength]; //PCM格式临时帧。
                HTInteger p_pclVoiceActivityStatus = new HTInteger( 1 ); //语音活动状态，1表示有语音活动，0表示无语音活动，预设为1，为了让在没有使用语音活动检测的情况下永远都是有语音活动。
                m_i64HasVoiceActivityFrameTotal = 0; //有语音活动帧总数清0。
                byte p_pszi8SpeexInputFrame[] = ( m_i32UseWhatCodec == 1 ) ? new byte[( int ) m_i64FrameLength] : null; //Speex格式输入帧。
                HTLong p_pclSpeexInputFrameLength = new HTLong( 0 ); //Speex格式输入帧数组的数据长度，单位字节。
                HTInteger p_pclSpeexInputFrameIsNeedTrans = new HTInteger( 1 ); //Speex格式输入帧是否需要传输，1表示需要传输，0表示不需要传输，预设为1为了让在没有使用非连续传输的情况下永远都是需要传输。

                while( true )
                {
                    //调用用户定义的处理函数。
                    p_i64Temp = UserProcess();
                    if( p_i64Temp == 0 )
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的处理函数成功。返回值：" + p_i64Temp );
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的处理函数失败。返回值：" + p_i64Temp );
                        break out;
                    }

                    //开始处理输入帧。
                    if( ( m_pclInputFrameLinkedList.size() > 0 ) && ( m_pclOutputFrameLinkedList.size() > 0 ) || //如果输入帧链表和输出帧链表中都有帧了，才开始处理。
                            ( m_pclInputFrameLinkedList.size() > 15 ) ) //如果输入帧链表里已经累积很多输入帧了，说明输出帧链表里迟迟没有音频输出帧，也开始处理。
                    {
                        //从输入帧链表中取出第一个输入帧。
                        synchronized( m_pclInputFrameLinkedList )
                        {
                            p_pszi16PcmInputFrame = m_pclInputFrameLinkedList.getFirst();
                            m_pclInputFrameLinkedList.removeFirst();
                        }
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：从输入帧链表中取出第一个输入帧。" );

                        //从输出帧链表中取出第一个输出帧。
                        if( m_pclOutputFrameLinkedList.size() > 0 ) //如果输出帧链表里有输出帧。
                        {
                            synchronized( m_pclOutputFrameLinkedList )
                            {
                                p_pszi16PcmOutputFrame = m_pclOutputFrameLinkedList.getFirst();
                                m_pclOutputFrameLinkedList.removeFirst();
                            }
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：从输出帧链表中取出第一个输出帧。" );
                        }
                        else //如果输出帧链表里没有输出帧。
                        {
                            p_pszi16PcmOutputFrame = new short[( int ) m_i64FrameLength];
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：输出帧链表中没有输出帧，用一个空帧代替。" );
                        }

                        //将输入帧复制到结果帧，方便处理。
                        for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                            p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmInputFrame[p_i32Temp];

                        //使用什么声学回音消除器。
                        switch( m_i32UseWhatAec )
                        {
                            case 0: //如果不使用声学回音消除器。
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用声学回音消除器。" );
                                break;
                            case 1: //如果使用Speex声学回音消除器。
                                p_i64Temp = m_pclSpeexAec.Process( p_pszi16PcmResultFrame, p_pszi16PcmOutputFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            case 2: //如果使用WebRtc定点版声学回音消除器。
                                p_i64Temp = m_pclWebRtcAecm.Process( p_pszi16PcmResultFrame, p_pszi16PcmOutputFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点版声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点版声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            case 3: //如果使用WebRtc浮点版声学回音消除器。
                                p_i64Temp = m_pclWebRtcAec.Process( p_pszi16PcmResultFrame, p_pszi16PcmOutputFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc浮点版声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc浮点版声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            case 4: //如果使用SpeexWebRtc三重浮点版声学回音消除器。
                                p_i64Temp = m_pclSpeexWebRtcAec.Process( p_pszi16PcmResultFrame, p_pszi16PcmOutputFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用SpeexWebRtc三重浮点版声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用SpeexWebRtc三重浮点版声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                        }

                        //使用什么噪音抑制器。
                        switch( m_i32UseWhatNs )
                        {
                            case 0: //如果不使用噪音抑制器。
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用噪音抑制器。" );
                                break;
                            }
                            case 1: //如果使用Speex预处理器的噪音抑制。
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：稍后在使用Speex预处理器时一起使用噪音抑制。" );
                                break;
                            }
                            case 2: //如果使用WebRtc定点版噪音抑制器。
                            {
                                p_i64Temp = m_pclWebRtcNsx.Process( p_pszi16PcmResultFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点版噪音抑制器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点版噪音抑制器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            }
                            case 3: //如果使用WebRtc浮点版噪音抑制器。
                            {
                                p_i64Temp = m_pclWebRtcNs.Process( p_pszi16PcmResultFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc浮点版噪音抑制器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc浮点版噪音抑制器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            }
                            case 4: //如果使用RNNoise噪音抑制器。
                            {
                                p_i64Temp = m_pclRNNoise.Process( p_pszi16PcmResultFrame, p_pszi16PcmTempFrame );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用RNNoise噪音抑制器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                        p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用RNNoise噪音抑制器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            }
                        }

                        //使用Speex预处理器。
                        if( ( m_i32UseWhatNs == 1 ) || ( m_i32IsUseSpeexPreprocessorOther != 0 ) )
                        {
                            p_i64Temp = m_pclSpeexPreprocessor.Process( p_pszi16PcmResultFrame, p_pszi16PcmTempFrame, p_pclVoiceActivityStatus );
                            if( p_i64Temp == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex预处理器成功。语音活动状态：" + p_pclVoiceActivityStatus.m_i32Value + "，返回值：" + p_i64Temp );

                                for( p_i32Temp = 0; p_i32Temp < m_i64FrameLength; p_i32Temp++ )
                                    p_pszi16PcmResultFrame[p_i32Temp] = p_pszi16PcmTempFrame[p_i32Temp];
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex预处理器失败。返回值：" + p_i64Temp );
                            }
                        }

                        //递增有语音活动帧总数。
                        m_i64HasVoiceActivityFrameTotal += p_pclVoiceActivityStatus.m_i32Value;

                        //使用什么编码器。
                        switch( m_i32UseWhatCodec )
                        {
                            case 0: //如果使用PCM原始数据。
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用PCM原始数据。" );
                                break;
                            }
                            case 1: //如果使用Speex编码器。
                            {
                                p_i64Temp = m_pclSpeexEncoder.Process( p_pszi16PcmResultFrame, p_pszi8SpeexInputFrame, p_pszi8SpeexInputFrame.length, p_pclSpeexInputFrameLength, p_pclSpeexInputFrameIsNeedTrans );
                                if( p_i64Temp == 0 )
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex编码器成功。Speex格式输入帧的内存长度：" + p_pclSpeexInputFrameLength + "，Speex格式输入帧是否需要传输：" + p_pclSpeexInputFrameIsNeedTrans + "，返回值：" + p_i64Temp );
                                }
                                else
                                {
                                    if( m_i32IsPrintLogcat != 0 )
                                        Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex编码器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            }
                            case 2: //如果使用Opus编码器。
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：暂不支持使用Opus编码器。" );
                                break out;
                            }
                        }

                        //用音频输入Wave文件写入器写入输入帧数据。
                        if( m_pclAudioInputWaveFileWriter != null )
                        {
                            if( m_pclAudioInputWaveFileWriter.WriteData( p_pszi16PcmInputFrame, ( int ) m_i64FrameLength ) == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：用音频输入Wave文件写入器写入输入帧数据成功。" );
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：用音频输入Wave文件写入器写入输入帧数据失败。" );
                            }
                        }

                        //用音频输出Wave文件写入器写入输出帧数据。
                        if( m_pclAudioOutputWaveFileWriter != null )
                        {
                            if( m_pclAudioOutputWaveFileWriter.WriteData( p_pszi16PcmOutputFrame, ( int ) m_i64FrameLength ) == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：用音频输出Wave文件写入器写入输出帧数据成功。" );
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：用音频输出Wave文件写入器写入输出帧数据失败。" );
                            }
                        }

                        //用音频结果Wave文件写入器写入结果帧数据。
                        if( m_pclAudioResultWaveFileWriter != null )
                        {
                            if( m_pclAudioResultWaveFileWriter.WriteData( p_pszi16PcmResultFrame, ( int ) m_i64FrameLength ) == 0 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：用音频结果Wave文件写入器写入结果帧数据成功。" );
                            }
                            else
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：用音频结果Wave文件写入器写入结果帧数据失败。" );
                            }
                        }

                        //调用用户定义的读取输入帧函数。
                        p_i64Temp = UserReadInputFrame( p_pszi16PcmInputFrame, p_pszi16PcmResultFrame, p_pclVoiceActivityStatus.m_i32Value, p_pszi8SpeexInputFrame, p_pclSpeexInputFrameLength.m_i64Value, p_pclSpeexInputFrameIsNeedTrans.m_i32Value );
                        if( p_i64Temp == 0 )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：音频处理线程：调用用户定义的读取输入帧函数成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的读取输入帧函数失败。返回值：" + p_i64Temp );
                            break out;
                        }

                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：本音频帧处理完毕。" );

                        if( m_i32ExitFlag != 0 ) //如果本线程退出标记为请求退出。
                        {
                            m_i32ExitCode = 0; //处理已经成功了，再将本线程退出代码设置为正常退出。
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：接收到退出请求，开始准备退出。" );
                            break out;
                        }
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
                }
            }

            if( m_i32IsPrintLogcat != 0 ) Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程开始退出。" );

            //请求音频输入线程退出。
            if( m_pclAudioInputThread != null )
            {
                m_pclAudioInputThread.RequireExit();
            }

            //请求音频输出线程退出。
            if( m_pclAudioOutputThread != null )
            {
                m_pclAudioOutputThread.RequireExit();
            }

            //等待音频输入线程退出。
            if( m_pclAudioInputThread != null )
            {
                try
                {
                    m_pclAudioInputThread.join();
                    m_pclAudioInputThread = null;
                }
                catch( InterruptedException e )
                {

                }
            }

            //等待音频输出线程退出。
            if( m_pclAudioOutputThread != null )
            {
                try
                {
                    m_pclAudioOutputThread.join();
                    m_pclAudioOutputThread = null;
                }
                catch( InterruptedException e )
                {

                }
            }

            //销毁音频输入Wave文件写入器对象。
            if( m_pclAudioInputWaveFileWriter != null )
            {
                m_pclAudioInputWaveFileWriter.Destory();
            }

            //销毁音频输出Wave文件写入器对象。
            if( m_pclAudioOutputWaveFileWriter != null )
            {
                m_pclAudioOutputWaveFileWriter.Destory();
            }

            //销毁音频结果Wave文件写入器对象。
            if( m_pclAudioResultWaveFileWriter != null )
            {
                m_pclAudioResultWaveFileWriter.Destory();
            }

            //销毁输入帧链表类对象。
            if( m_pclInputFrameLinkedList != null )
            {
                m_pclInputFrameLinkedList.clear();
                m_pclInputFrameLinkedList = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁输入帧链表类对象成功。" );
            }

            //销毁输出帧链表类对象。
            if( m_pclOutputFrameLinkedList != null )
            {
                m_pclOutputFrameLinkedList.clear();
                m_pclOutputFrameLinkedList = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁输出帧链表类对象成功。" );
            }

            //销毁Speex声学回音消除器类对象。
            if( m_pclSpeexAec != null )
            {
                //保存Speex声学回音消除器的内存块到文件。
                if( m_i32SpeexAecIsSaveMemoryFile != 0 )
                {
                    File file = new File( m_pclSpeexAecMemoryFileFullPath );
                    FileInputStream p_pclSpeexAecMemoryFileInputStream = null;
                    FileOutputStream p_pclSpeexAecMemoryFileOutputStream = null;
                    long p_i64SpeexAecMemoryFileVoiceActivityStatusTotal = 0;
                    byte p_pszi8SpeexAecMemory[];
                    HTLong pclSpeexAecMemoryLength = new HTLong();

                    ReadSpeexAecMemoryFile:
                    if( file.exists() )
                    {
                        try
                        {
                            p_pclSpeexAecMemoryFileInputStream = new FileInputStream( m_pclSpeexAecMemoryFileFullPath );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器的内存块文件 " + m_pclAudioInputFileFullPath + " 的文件输入流对象成功。" );
                        }
                        catch( FileNotFoundException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器的内存块文件 " + m_pclAudioInputFileFullPath + " 的文件输入流对象失败。原因：" + e.toString() );
                            break ReadSpeexAecMemoryFile;
                        }

                        p_pszi8SpeexAecMemory = new byte[8];

                        //跳过Speex声学回音消除器内存块文件的采样频率、帧的数据长度、过滤器的数据长度。
                        try
                        {
                            if( p_pclSpeexAecMemoryFileInputStream.skip( 16 ) != 16 )
                            {
                                throw new IOException( "文件中没有有语音活动帧总数。" );
                            }
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：跳过Speex声学回音消除器内存块文件的采样频率、帧的数据长度、过滤器的数据长度成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：跳过Speex声学回音消除器内存块文件的采样频率、帧的数据长度、过滤器的数据长度失败。原因：" + e.toString() );
                            break ReadSpeexAecMemoryFile;
                        }

                        try
                        {
                            if( p_pclSpeexAecMemoryFileInputStream.read( p_pszi8SpeexAecMemory, 0, 8 ) != 8 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：Speex声学回音消除器的内存块文件中没有有语音活动帧总数。" );
                                break ReadSpeexAecMemoryFile;
                            }
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：读取Speex声学回音消除器的内存块文件中的有语音活动帧总数失败。原因：" + e.toString() );
                            break ReadSpeexAecMemoryFile;
                        }

                        p_i64SpeexAecMemoryFileVoiceActivityStatusTotal = ( ( long ) p_pszi8SpeexAecMemory[0] & 0xFF ) + ( ( ( long ) p_pszi8SpeexAecMemory[1] & 0xFF ) << 8 ) + ( ( ( long ) p_pszi8SpeexAecMemory[2] & 0xFF ) << 16 ) + ( ( ( long ) p_pszi8SpeexAecMemory[3] & 0xFF ) << 24 ) + ( ( ( long ) p_pszi8SpeexAecMemory[4] & 0xFF ) << 32 ) + ( ( ( long ) p_pszi8SpeexAecMemory[5] & 0xFF ) << 40 ) + ( ( ( long ) p_pszi8SpeexAecMemory[6] & 0xFF ) << 48 ) + ( ( ( long ) p_pszi8SpeexAecMemory[7] & 0xFF ) << 56 );
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：Speex声学回音消除器的内存块文件中的有语音活动帧总数为：" + p_i64SpeexAecMemoryFileVoiceActivityStatusTotal + "，本次的：" + m_i64HasVoiceActivityFrameTotal + "。" );
                    }

                    if( p_pclSpeexAecMemoryFileInputStream != null )
                    {
                        try
                        {
                            p_pclSpeexAecMemoryFileInputStream.close();
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器的内存块文件的文件输入流对象成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器的内存块文件的文件输入流对象失败。原因：" + e.toString() );
                        }
                    }

                    WriteSpeexAecMemoryFile:
                    if( ( m_i64HasVoiceActivityFrameTotal >= 1500 ) || ( m_i64HasVoiceActivityFrameTotal > p_i64SpeexAecMemoryFileVoiceActivityStatusTotal ) ) //如果本次有语音活动帧总数超过30秒，或本次的有语音活动帧总数比Speex声学回音消除器的内存块文件中的大。
                    {
                        if( m_pclSpeexAec.GetMemoryLength( pclSpeexAecMemoryLength ) != 0 )
                        {
                            break WriteSpeexAecMemoryFile;
                        }

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream = new FileOutputStream( m_pclSpeexAecMemoryFileFullPath );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器的内存块文件 " + m_pclSpeexAecMemoryFileFullPath + " 的文件输出流对象成功。" );
                        }
                        catch( FileNotFoundException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：创建Speex声学回音消除器的内存块文件 " + m_pclSpeexAecMemoryFileFullPath + " 的文件输出流对象失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }

                        p_pszi8SpeexAecMemory = new byte[( int ) pclSpeexAecMemoryLength.m_i64Value];

                        //写入采样频率到Speex声学回音消除器的内存块文件。
                        p_pszi8SpeexAecMemory[0] = ( byte ) ( m_i32SamplingRate & 0xFF );
                        p_pszi8SpeexAecMemory[1] = ( byte ) ( m_i32SamplingRate >> 8 & 0xFF );
                        p_pszi8SpeexAecMemory[2] = ( byte ) ( m_i32SamplingRate >> 16 & 0xFF );
                        p_pszi8SpeexAecMemory[3] = ( byte ) ( m_i32SamplingRate >> 24 & 0xFF );

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.write( p_pszi8SpeexAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入采样频率到Speex声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入采样频率到Speex声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }

                        //写入帧的数据长度到Speex声学回音消除器的内存块文件。
                        p_pszi8SpeexAecMemory[0] = ( byte ) ( m_i64FrameLength & 0xFF );
                        p_pszi8SpeexAecMemory[1] = ( byte ) ( m_i64FrameLength >> 8 & 0xFF );
                        p_pszi8SpeexAecMemory[2] = ( byte ) ( m_i64FrameLength >> 16 & 0xFF );
                        p_pszi8SpeexAecMemory[3] = ( byte ) ( m_i64FrameLength >> 24 & 0xFF );
                        p_pszi8SpeexAecMemory[4] = ( byte ) ( m_i64FrameLength >> 32 & 0xFF );
                        p_pszi8SpeexAecMemory[5] = ( byte ) ( m_i64FrameLength >> 40 & 0xFF );
                        p_pszi8SpeexAecMemory[6] = ( byte ) ( m_i64FrameLength >> 48 & 0xFF );
                        p_pszi8SpeexAecMemory[7] = ( byte ) ( m_i64FrameLength >> 56 & 0xFF );

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.write( p_pszi8SpeexAecMemory, 0, 8 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入帧的数据长度到Speex声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入帧的数据长度到Speex声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }

                        //写入滤波器的数据长度到Speex声学回音消除器的内存块文件。
                        p_pszi8SpeexAecMemory[0] = ( byte ) ( m_i32SpeexAecFilterLength & 0xFF );
                        p_pszi8SpeexAecMemory[1] = ( byte ) ( m_i32SpeexAecFilterLength >> 8 & 0xFF );
                        p_pszi8SpeexAecMemory[2] = ( byte ) ( m_i32SpeexAecFilterLength >> 16 & 0xFF );
                        p_pszi8SpeexAecMemory[3] = ( byte ) ( m_i32SpeexAecFilterLength >> 24 & 0xFF );

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.write( p_pszi8SpeexAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入滤波器的数据长度到Speex声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入滤波器的数据长度到Speex声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }

                        //写入有语音活动帧总数到Speex声学回音消除器的内存块文件。
                        p_pszi8SpeexAecMemory[0] = ( byte ) ( m_i64HasVoiceActivityFrameTotal & 0xFF );
                        p_pszi8SpeexAecMemory[1] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 8 & 0xFF );
                        p_pszi8SpeexAecMemory[2] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 16 & 0xFF );
                        p_pszi8SpeexAecMemory[3] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 24 & 0xFF );
                        p_pszi8SpeexAecMemory[4] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 32 & 0xFF );
                        p_pszi8SpeexAecMemory[5] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 40 & 0xFF );
                        p_pszi8SpeexAecMemory[6] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 48 & 0xFF );
                        p_pszi8SpeexAecMemory[7] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 56 & 0xFF );

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.write( p_pszi8SpeexAecMemory, 0, 8 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入有语音活动帧总数到Speex声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入有语音活动帧总数到Speex声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }

                        //写入内存块到Speex声学回音消除器内存块文件。
                        if( m_pclSpeexAec.GetMemory( p_pszi8SpeexAecMemory, pclSpeexAecMemoryLength.m_i64Value ) != 0 )
                        {
                            break WriteSpeexAecMemoryFile;
                        }

                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.write( p_pszi8SpeexAecMemory, 0, ( int ) pclSpeexAecMemoryLength.m_i64Value );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入内存块到Speex声学回音消除器内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入内存块到Speex声学回音消除器内存块文件失败。原因：" + e.toString() );
                            break WriteSpeexAecMemoryFile;
                        }
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：因为本次有语音活动帧总数没有超过30秒，或本次的有语音活动帧总数比Speex声学回音消除器内存块文件中的小，所以本次不保存Speex声学回音消除器内存块到文件。" );
                    }

                    if( p_pclSpeexAecMemoryFileOutputStream != null )
                    {
                        try
                        {
                            p_pclSpeexAecMemoryFileOutputStream.close();
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器的内存块文件的文件输出流对象成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器的内存块文件的文件输出流对象失败。原因：" + e.toString() );
                        }
                    }
                }

                p_i64Temp = m_pclSpeexAec.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclSpeexAec = null;
            }

            //销毁WebRtc定点版声学回音消除器类对象。
            if( m_pclWebRtcAecm != null )
            {
                p_i64Temp = m_pclWebRtcAecm.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版声学回音消除器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcAecm = null;
            }

            //销毁WebRtc浮点版声学回音消除器类对象。
            if( m_pclWebRtcAec != null )
            {
                //保存WebRtc浮点版声学回音消除器的内存块到文件。
                if( m_i32WebRtcAecIsSaveMemoryFile != 0 )
                {
                    File file = new File( m_pclWebRtcAecMemoryFileFullPath );
                    FileInputStream p_pclWebRtcAecMemoryFileInputStream = null;
                    FileOutputStream p_pclWebRtcAecMemoryFileOutputStream = null;
                    long p_i64WebRtcAecMemoryFileVoiceActivityStatusTotal = 0;
                    byte p_pszi8WebRtcAecMemory[];
                    HTLong pclWebRtcAecMemoryLength = new HTLong( 0 );

                    ReadWebRtcAecMemoryFile:
                    if( file.exists() )
                    {
                        try
                        {
                            p_pclWebRtcAecMemoryFileInputStream = new FileInputStream( m_pclWebRtcAecMemoryFileFullPath );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器的内存块文件 " + m_pclAudioInputFileFullPath + " 的文件输入流对象成功。" );
                        }
                        catch( FileNotFoundException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器的内存块文件 " + m_pclAudioInputFileFullPath + " 的文件输入流对象失败。原因：" + e.toString() );
                            break ReadWebRtcAecMemoryFile;
                        }

                        p_pszi8WebRtcAecMemory = new byte[8];

                        //跳过WebRtc浮点版声学回音消除器内存块文件的前8个参数。
                        try
                        {
                            if( p_pclWebRtcAecMemoryFileInputStream.skip( 36 ) != 36 )
                            {
                                throw new IOException( "文件中没有有语音活动帧总数。" );
                            }
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：跳过WebRtc浮点版声学回音消除器内存块文件的采样频率、帧的数据长度、过滤器的数据长度成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：跳过WebRtc浮点版声学回音消除器内存块文件的采样频率、帧的数据长度、过滤器的数据长度失败。原因：" + e.toString() );
                            break ReadWebRtcAecMemoryFile;
                        }

                        try
                        {
                            if( p_pclWebRtcAecMemoryFileInputStream.read( p_pszi8WebRtcAecMemory, 0, 8 ) != 8 )
                            {
                                if( m_i32IsPrintLogcat != 0 )
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器的内存块文件中没有有语音活动帧总数。" );
                                break ReadWebRtcAecMemoryFile;
                            }
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：读取WebRtc浮点版声学回音消除器的内存块文件中的有语音活动帧总数失败。原因：" + e.toString() );
                            break ReadWebRtcAecMemoryFile;
                        }

                        p_i64WebRtcAecMemoryFileVoiceActivityStatusTotal = ( ( long ) p_pszi8WebRtcAecMemory[0] & 0xFF ) + ( ( ( long ) p_pszi8WebRtcAecMemory[1] & 0xFF ) << 8 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[2] & 0xFF ) << 16 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[3] & 0xFF ) << 24 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[4] & 0xFF ) << 32 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[5] & 0xFF ) << 40 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[6] & 0xFF ) << 48 ) + ( ( ( long ) p_pszi8WebRtcAecMemory[7] & 0xFF ) << 56 );
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：WebRtc浮点版声学回音消除器的内存块文件中的有语音活动帧总数为：" + p_i64WebRtcAecMemoryFileVoiceActivityStatusTotal + "，本次的：" + m_i64HasVoiceActivityFrameTotal + "。" );
                    }

                    if( p_pclWebRtcAecMemoryFileInputStream != null )
                    {
                        try
                        {
                            p_pclWebRtcAecMemoryFileInputStream.close();
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器的内存块文件的文件输入流对象成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器的内存块文件的文件输入流对象失败。原因：" + e.toString() );
                        }
                    }

                    WriteWebRtcAecMemoryFile:
                    if( ( m_i64HasVoiceActivityFrameTotal >= 1500 ) || ( m_i64HasVoiceActivityFrameTotal > p_i64WebRtcAecMemoryFileVoiceActivityStatusTotal ) ) //如果本次有语音活动帧总数超过30秒，或本次的有语音活动帧总数比WebRtc浮点版声学回音消除器的内存块文件中的大。
                    {
                        if( m_pclWebRtcAec.GetMemoryLength( pclWebRtcAecMemoryLength ) != 0 )
                        {
                            break WriteWebRtcAecMemoryFile;
                        }

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream = new FileOutputStream( m_pclWebRtcAecMemoryFileFullPath );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器的内存块文件 " + m_pclWebRtcAecMemoryFileFullPath + " 的文件输出流对象成功。" );
                        }
                        catch( FileNotFoundException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：创建WebRtc浮点版声学回音消除器的内存块文件 " + m_pclWebRtcAecMemoryFileFullPath + " 的文件输出流对象失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        p_pszi8WebRtcAecMemory = new byte[( int ) pclWebRtcAecMemoryLength.m_i64Value];

                        //写入采样频率到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i32SamplingRate & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i32SamplingRate >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i32SamplingRate >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i32SamplingRate >> 24 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入采样频率到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入采样频率到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入帧的数据长度到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i64FrameLength & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i64FrameLength >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i64FrameLength >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i64FrameLength >> 24 & 0xFF );
                        p_pszi8WebRtcAecMemory[4] = ( byte ) ( m_i64FrameLength >> 32 & 0xFF );
                        p_pszi8WebRtcAecMemory[5] = ( byte ) ( m_i64FrameLength >> 40 & 0xFF );
                        p_pszi8WebRtcAecMemory[6] = ( byte ) ( m_i64FrameLength >> 48 & 0xFF );
                        p_pszi8WebRtcAecMemory[7] = ( byte ) ( m_i64FrameLength >> 56 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 8 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入帧的数据长度到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入帧的数据长度到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入消除模式到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i32WebRtcAecEchoMode & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i32WebRtcAecEchoMode >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i32WebRtcAecEchoMode >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i32WebRtcAecEchoMode >> 24 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入消除模式到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入消除模式到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入回音的延迟到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i32WebRtcAecDelay & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i32WebRtcAecDelay >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i32WebRtcAecDelay >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i32WebRtcAecDelay >> 24 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入回音的延迟到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入回音的延迟到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入是否使用回音延迟不可知模式到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i32WebRtcAecIsUseDelayAgnosticMode & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i32WebRtcAecIsUseDelayAgnosticMode >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i32WebRtcAecIsUseDelayAgnosticMode >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i32WebRtcAecIsUseDelayAgnosticMode >> 24 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入是否使用回音延迟不可知模式到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入是否使用回音延迟不可知模式到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入是否使用自适应调节回音的延迟到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i32WebRtcAecIsUseAdaptiveAdjustDelay & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i32WebRtcAecIsUseAdaptiveAdjustDelay >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i32WebRtcAecIsUseAdaptiveAdjustDelay >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i32WebRtcAecIsUseAdaptiveAdjustDelay >> 24 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 4 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入是否使用自适应调节回音的延迟到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入是否使用自适应调节回音的延迟到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入有语音活动帧总数到WebRtc浮点版声学回音消除器的内存块文件。
                        p_pszi8WebRtcAecMemory[0] = ( byte ) ( m_i64HasVoiceActivityFrameTotal & 0xFF );
                        p_pszi8WebRtcAecMemory[1] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 8 & 0xFF );
                        p_pszi8WebRtcAecMemory[2] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 16 & 0xFF );
                        p_pszi8WebRtcAecMemory[3] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 24 & 0xFF );
                        p_pszi8WebRtcAecMemory[4] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 32 & 0xFF );
                        p_pszi8WebRtcAecMemory[5] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 40 & 0xFF );
                        p_pszi8WebRtcAecMemory[6] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 48 & 0xFF );
                        p_pszi8WebRtcAecMemory[7] = ( byte ) ( m_i64HasVoiceActivityFrameTotal >> 56 & 0xFF );

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, 8 );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入有语音活动帧总数到WebRtc浮点版声学回音消除器的内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入有语音活动帧总数到WebRtc浮点版声学回音消除器的内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }

                        //写入内存块到WebRtc浮点版声学回音消除器内存块文件。
                        if( m_pclWebRtcAec.GetMemory( p_pszi8WebRtcAecMemory, pclWebRtcAecMemoryLength.m_i64Value ) != 0 )
                        {
                            break WriteWebRtcAecMemoryFile;
                        }

                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.write( p_pszi8WebRtcAecMemory, 0, ( int ) pclWebRtcAecMemoryLength.m_i64Value );
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：写入内存块到WebRtc浮点版声学回音消除器内存块文件成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：写入内存块到WebRtc浮点版声学回音消除器内存块文件失败。原因：" + e.toString() );
                            break WriteWebRtcAecMemoryFile;
                        }
                    }
                    else
                    {
                        if( m_i32IsPrintLogcat != 0 )
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：因为本次有语音活动帧总数没有超过30秒，或本次的有语音活动帧总数比WebRtc浮点版声学回音消除器内存块文件中的小，所以本次不保存WebRtc浮点版声学回音消除器内存块到文件。" );
                    }

                    if( p_pclWebRtcAecMemoryFileOutputStream != null )
                    {
                        try
                        {
                            p_pclWebRtcAecMemoryFileOutputStream.close();
                            if( m_i32IsPrintLogcat != 0 )
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器的内存块文件的文件输出流对象成功。" );
                        }
                        catch( IOException e )
                        {
                            if( m_i32IsPrintLogcat != 0 )
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器的内存块文件的文件输出流对象失败。原因：" + e.toString() );
                        }
                    }
                }

                p_i64Temp = m_pclWebRtcAec.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版声学回音消除器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcAec = null;
            }

            //销毁SpeexWebRtc三重声学回音消除器类对象。
            if( m_pclSpeexWebRtcAec != null )
            {
                p_i64Temp = m_pclSpeexWebRtcAec.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁SpeexWebRtc三重声学回音消除器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁SpeexWebRtc三重声学回音消除器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclSpeexWebRtcAec = null;
            }

            //销毁WebRtc定点版噪音抑制器类对象。
            if( m_pclWebRtcNsx != null )
            {
                p_i64Temp = m_pclWebRtcNsx.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版噪音抑制器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版噪音抑制器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcNsx = null;
            }

            //销毁WebRtc浮点版噪音抑制器类对象。
            if( m_pclWebRtcNs != null )
            {
                p_i64Temp = m_pclWebRtcNs.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版噪音抑制器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc浮点版噪音抑制器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcNs = null;
            }

            //销毁RNNoise噪音抑制器类对象。
            if( m_pclRNNoise != null )
            {
                p_i64Temp = m_pclRNNoise.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁RNNoise噪音抑制器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁RNNoise噪音抑制器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclRNNoise = null;
            }

            //销毁Speex预处理器类对象。
            if( m_pclSpeexPreprocessor != null )
            {
                p_i64Temp = m_pclSpeexPreprocessor.Destory();
                if( p_i64Temp == 0 )
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex预处理器类对象成功。返回值：" + p_i64Temp );
                }
                else
                {
                    if( m_i32IsPrintLogcat != 0 )
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex预处理器类对象失败。返回值：" + p_i64Temp );
                }
                m_pclSpeexPreprocessor = null;
            }

            //销毁Speex编码器类对象。
            if( m_pclSpeexEncoder != null )
            {
                m_pclSpeexEncoder.Destory();
                m_pclSpeexEncoder = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex编码器类对象成功。" );
            }

            //销毁Speex解码器类对象。
            if( m_pclSpeexDecoder != null )
            {
                m_pclSpeexDecoder.Destory();
                m_pclSpeexDecoder = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex解码器类对象成功。" );
            }

            //销毁音频输入类对象。
            if( m_pclAudioRecord != null )
            {
                if( m_pclAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING )
                {
                    m_pclAudioRecord.stop();
                }
                m_pclAudioRecord.release();
                m_pclAudioRecord = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁音频输入类对象成功。" );
            }

            //销毁音频输出类对象。
            if( m_pclAudioTrack != null )
            {
                if( m_pclAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED )
                {
                    m_pclAudioTrack.stop();
                }
                m_pclAudioTrack.release();
                m_pclAudioTrack = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁音频输出类对象成功。" );
            }

            //调用用户定义的销毁函数。
            p_i64Temp = UserDestory();

            //销毁接近息屏唤醒锁类对象。
            if( m_clProximityScreenOffWakeLock != null )
            {
                m_clProximityScreenOffWakeLock.release();
                m_clProximityScreenOffWakeLock = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁接近息屏唤醒锁类对象成功。" );
            }

            //销毁屏幕键盘全亮唤醒锁类对象。
            if( m_clFullWakeLock != null )
            {
                m_clFullWakeLock.release();
                m_clFullWakeLock = null;

                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁屏幕键盘全亮唤醒锁类对象成功。" );
            }

            if( p_i64Temp == 0 ) //如果用户需要直接退出。
            {
                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程已退出。" );
                break reinit;
            }
            else //如果用户需用重新初始化。
            {
                if( m_i32IsPrintLogcat != 0 )
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程重新初始化。" );
            }
        }
    }
}