package HeavenTao.Audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;

//音频处理线程类
public abstract class AudioProcessThread extends Thread
{
    public String m_pclCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    public int m_i32ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出
    public int m_i32ExitCode = 0; //本线程退出代码，0表示正常退出，-1表示初始化失败，-2表示处理失败

    public int m_i32SamplingRate; //音频数据的采样频率，包括：8000Hz，16000Hz，32000Hz
    public int m_i32FrameSize; //音频数据帧的长度，包括：8000Hz为160个采样，16000Hz为320个采样，32000Hz为640个采样

    AudioRecord m_pclAudioRecord; //录音类
    AudioTrack m_pclAudioTrack; //播放类

    int m_i32UseWhatAec; //使用什么声学回音消除器，0表示不使用，1表示WebRtc声学回音消除器，2表示WebRtc移动版声学回音消除器，3表示Speex声学回音消除器

    int m_i32WebRtcAecNlpMode; //WebRtc声学回音消除器的非线性滤波模式，0表示保守, 1表示适中, 2表示积极

    int m_i32WebRtcAecmEchoMode; //WebRtc移动版声学回音消除器的消除模式，最低为0，最高为4
    int m_i32WebRtcAecmDelay; //WebRtc移动版声学回音消除器的回音延迟时间，单位毫秒，-1表示自适应设置

    int m_i32SpeexAecFilterLength; //Speex声学回音消除器的过滤器长度，单位毫秒

    int m_i32IsUseWebRtcNsx; //是否使用WebRtc定点版噪音抑制器，非0表示要使用，0表示不使用
    int m_i32WebRtcNsxPolicyMode; //WebRtc定点噪音抑制器的策略模式，0表示轻微, 1表示适中, 2表示积极，3表示激进

    int m_i32IsUseSpeexPreprocessor; //是否使用Speex预处理器，非0表示要使用，0表示不使用
    int m_i32SpeexPreprocessorIsUseNs; //是否使用Speex预处理器的NS噪音抑制，非0表示要使用，0表示不使用
    int m_i32SpeexPreprocessorNoiseSuppress; //Speex预处理器在NS噪音抑制时，噪音的最大程度衰减的分贝值
    int m_i32SpeexPreprocessorIsUseVad; //是否使用Speex预处理器的VAD语音活动检测，非0表示要使用，0表示不使用
    int m_i32SpeexPreprocessorVadProbStart; //Speex预处理器在VAD语音活动检测时，从无语音活动到有语音活动的判断百分比概率，最小为0，最大为100
    int m_i32SpeexPreprocessorVadProbContinue; //Speex预处理器在VAD语音活动检测时，从有语音活动到无语音活动的判断百分比概率，最小为0，最大为100
    int m_i32SpeexPreprocessorIsUseAgc; //是否使用Speex预处理器的AGC自动增益控制，非0表示要使用，0表示不使用
    int m_i32SpeexPreprocessorAgcLevel; //Speex预处理器在AGC自动增益控制时，自动增益的等级，最小为1，最大为32768
    int m_i32SpeexPreprocessorAgcMaxGain; //Speex预处理器在AGC自动增益控制时，最大增益的分贝值
    int m_i32SpeexPreprocessorIsUseRec; //是否使用Speex预处理器的REC残余回音消除，非0表示要使用，0表示不使用
    int m_i32SpeexPreprocessorEchoSuppress; //Speex预处理器在REC残余回音消除时，残余回音的最大程度衰减的分贝值
    int m_i32SpeexPreprocessorEchoSuppressActive; //Speex预处理器在REC残余回音消除时，有近端语音活动时的残余回音的最大程度衰减的分贝值

    public int m_i32UseWhatCodec; //使用什么编解码器，0表示PCM原始数据，1表示Speex编解码器，2表示Opus编解码器。

    int m_i32SpeexCodecEncoderUseCbrOrVbr; //使用Speex编码器的固定比特率或者动态比特率，0表示固定比特率，1表示动态比特率。
    int m_i32SpeexCodecEncoderQuality; //Speex编码器的质量等级。质量等级越高，音质越好，压缩率越低。最低为0，最高为10。
    int m_i32SpeexCodecEncoderComplexity; //Speex编码器的复杂度。复杂度越高，压缩率越高，CPU使用率越高，音质越好。最低为0，最高为10。
    int m_i32SpeexCodecEncoderPlcExpectedLossRate; //Speex编码器的数据包丢失隐藏的预计丢失率。预计丢失率越高，抗网络抖动越强，压缩率越低。最低为0，最高为100。

    WebRtcAec m_pclWebRtcAec; //WebRtc声学回音消除器类对象的内存指针
    WebRtcAecm m_pclWebRtcAecm; //WebRtc移动版声学回音消除器类对象的内存指针
    SpeexAec m_pclSpeexAec; //Speex声学回音消除器类对象的内存指针
    WebRtcNsx m_pclWebRtcNsx; //WebRtc定点版噪音抑制器类对象的内存指针
    SpeexPreprocessor m_pclSpeexPreprocessor; //Speex预处理器类对象的内存指针
    SpeexEncoder m_pclSpeexEncoder; //Speex编码器类对象的内存指针
    SpeexDecoder m_pclSpeexDecoder; //Speex解码器类对象的内存指针

    LinkedList<short[]> m_pclAlreadyAudioInputLinkedList; //存放已录音的链表类对象的内存指针
    LinkedList<short[]> m_pclAlreadyAudioOutputLinkedList; //存放已播放的链表类对象的内存指针

    AudioInputThread m_clAudioInputThread; //存放音频输入线程类对象的内存指针
    AudioOutputThread m_clAudioOutputThread; //存放音频输出线程类对象的内存指针

    //用户定义的相关函数
    public abstract long UserInit(); //用户定义的初始化函数，在本线程刚启动时调用一次，返回值表示是否成功，0表示成功，非0表示失败
    public abstract long UserProcess(); //用户定义的处理函数，在本线程运行时每隔1毫秒就调用一次，返回值表示是否成功，0表示成功，非0表示失败
    public abstract long UserDestory(); //用户定义的销毁函数，在本线程退出时调用一次，返回值表示是否重新初始化，0表示直接退出，非0表示重新初始化
    public abstract long UserReadAudioInputDataFrame( short pszi16PcmAudioInputDataFrame[], short pszi16PcmAudioResultDataFrame[], int i32VoiceActivityStatus, byte pszi8SpeexAudioInputDataFrame[], int i32SpeexAudioInputDataFrameSize, int i32SpeexAudioInputDataFrameIsNeedTrans ); //用户定义的读取音频输入数据帧函数，在采样完一个音频输入数据帧并处理完后回调一次
    public abstract void UserWriteAudioOutputDataFrame( short pszi16PcmAudioOutputDataFrame[], byte p_pszi8SpeexAudioInputDataFrame[], int p_pszi32SpeexAudioInputDataFrameSize[] ); //用户定义的写入音频输出数据帧函数，在需要播放一个音频输出数据帧时回调一次
    public abstract void UserGetPcmAudioOutputDataFrame( short pszi16PcmAudioOutputDataFrame[] ); //用户定义的获取PCM格式音频输出数据帧函数，在解码完一个音频输出数据帧时回调一次

    //请求本线程退出
    public void RequireExit()
    {
        m_i32ExitFlag = 1;
    }

    //设置音频数据
    public long SetAudioData( int i32SamplingRate )
    {
        long p_i64Result = -1; //存放本函数执行结果的值，0表示成功，非0表示失败。

        out:
        {
            //判断各个变量是否正确。
            if( ( i32SamplingRate != 8000 ) && ( i32SamplingRate != 16000 ) && ( i32SamplingRate != 32000 ) ) //如果采样频率不是8000、16000、32000。
            {
                break out;
            }

            if( i32SamplingRate == 8000 )
            {
                m_i32SamplingRate = 8000; //设置音频数据的采样频率
                m_i32FrameSize = 160; //设置音频数据帧的长度
            }
            else if( i32SamplingRate == 16000 )
            {
                m_i32SamplingRate = 16000; //设置音频数据的采样频率
                m_i32FrameSize = 320; //设置音频数据帧的长度
            }
            else if( i32SamplingRate == 32000 )
            {
                m_i32SamplingRate = 32000; //设置音频数据的采样频率
                m_i32FrameSize = 640; //设置音频数据帧的长度
            }

            p_i64Result = 0;
        }

        return p_i64Result;
    }

    //设置不使用声学回音消除器
    public void SetUseNoAec()
    {
        m_i32UseWhatAec = 0;
    }

    //设置使用WebRtc声学回音消除器
    public void SetUseWebRtcAec( int i32WebRtcAecNlpMode )
    {
        m_i32UseWhatAec = 1;
        m_i32WebRtcAecNlpMode = i32WebRtcAecNlpMode;
    }

    //设置使用WebRtc移动版声学回音消除器
    public void SetUseWebRtcAecm( int i32WebRtcAecmEchoMode, int i32WebRtcAecmDelay )
    {
        m_i32UseWhatAec = 2;
        m_i32WebRtcAecmEchoMode = i32WebRtcAecmEchoMode;
        m_i32WebRtcAecmDelay = i32WebRtcAecmDelay;
    }

    //设置使用Speex声学回音消除器
    public void SetUseSpeexAec( int i32SpeexAecFilterLength )
    {
        m_i32UseWhatAec = 3;
        m_i32SpeexAecFilterLength = i32SpeexAecFilterLength;
    }

    //设置WebRtc定点版噪音抑制器
    public void SetWebRtcNsx( int i32IsUseWebRtcNsx, int i32WebRtcNsxPolicyMode )
    {
        m_i32IsUseWebRtcNsx = i32IsUseWebRtcNsx;
        m_i32WebRtcNsxPolicyMode = i32WebRtcNsxPolicyMode;
    }

    //设置Speex预处理器
    public void SetSpeexPreprocessor( int i32IsUseSpeexPreprocessor, int i32SpeexPreprocessorIsUseNs, int i32SpeexPreprocessorNoiseSuppress, int i32SpeexPreprocessorIsUseVad, int i32SpeexPreprocessorVadProbStart, int i32SpeexPreprocessorVadProbContinue, int i32SpeexPreprocessorIsUseAgc, int i32SpeexPreprocessorAgcLevel, int i32SpeexPreprocessorAgcMaxGain, int i32SpeexPreprocessorIsUseRec, int i32SpeexPreprocessorEchoSuppress, int i32SpeexPreprocessorEchoSuppressActive )
    {
        m_i32IsUseSpeexPreprocessor = i32IsUseSpeexPreprocessor;
        m_i32SpeexPreprocessorIsUseNs = i32SpeexPreprocessorIsUseNs;
        m_i32SpeexPreprocessorNoiseSuppress = i32SpeexPreprocessorNoiseSuppress;
        m_i32SpeexPreprocessorIsUseVad = i32SpeexPreprocessorIsUseVad;
        m_i32SpeexPreprocessorVadProbStart = i32SpeexPreprocessorVadProbStart;
        m_i32SpeexPreprocessorVadProbContinue = i32SpeexPreprocessorVadProbContinue;
        m_i32SpeexPreprocessorIsUseAgc = i32SpeexPreprocessorIsUseAgc;
        m_i32SpeexPreprocessorAgcLevel = i32SpeexPreprocessorAgcLevel;
        m_i32SpeexPreprocessorAgcMaxGain = i32SpeexPreprocessorAgcMaxGain;
        m_i32SpeexPreprocessorIsUseRec = i32SpeexPreprocessorIsUseRec;
        m_i32SpeexPreprocessorEchoSuppress = i32SpeexPreprocessorEchoSuppress;
        m_i32SpeexPreprocessorEchoSuppressActive = i32SpeexPreprocessorEchoSuppressActive;
    }

    //设置使用PCM原始数据
    public void SetUsePcm()
    {
        m_i32UseWhatCodec = 0;
    }

    //设置使用Speex编解码器
    public void SetUseSpeexCodec( int i32SpeexCodecEncoderUseCbrOrVbr, int i32SpeexCodecEncoderQuality, int i32SpeexCodecEncoderComplexity, int i32SpeexCodecEncoderPlcExpectedLossRate )
    {
        m_i32UseWhatCodec = 1;
        m_i32SpeexCodecEncoderUseCbrOrVbr = i32SpeexCodecEncoderUseCbrOrVbr;
        m_i32SpeexCodecEncoderQuality = i32SpeexCodecEncoderQuality;
        m_i32SpeexCodecEncoderComplexity = i32SpeexCodecEncoderComplexity;
        m_i32SpeexCodecEncoderPlcExpectedLossRate = i32SpeexCodecEncoderPlcExpectedLossRate;
    }

    //设置使用Opus编解码器
    public void SetUseOpusCodec()
    {
        m_i32UseWhatCodec = 2;
    }

    //写入音频输出数据帧
    public void WriteAudioOutputDataFrame( short pszi16PcmAudioOutputDataFrame[] )
    {
        long p_i64Temp;

        //将音频输出数据帧解码成PCM原始数据
        switch( m_i32UseWhatCodec )//使用什么编解码器
        {
            case 0: //如果使用PCM原始数据
            {
                //调用用户定义的写入音频输出数据帧函数
                UserWriteAudioOutputDataFrame( pszi16PcmAudioOutputDataFrame, null, null );

                break;
            }
            case 1: //如果使用Speex编解码器
            {
                byte p_pszi8SpeexAudioInputDataFrame[] = new byte[m_i32FrameSize]; //Speex格式音频输出数据帧
                int p_pszi32SpeexAudioInputDataFrameSize[] = new int[1]; //Speex格式音频输出数据帧的内存长度，单位字节

                //调用用户定义的写入音频输出数据帧函数
                UserWriteAudioOutputDataFrame( null, p_pszi8SpeexAudioInputDataFrame, p_pszi32SpeexAudioInputDataFrameSize );

                //使用Speex解码器
                if( p_pszi32SpeexAudioInputDataFrameSize[0] != 0 ) //如果这个Speex格式音频输出数据帧接收到了。
                {
                    p_i64Temp = m_pclSpeexDecoder.Decode( p_pszi8SpeexAudioInputDataFrame, p_pszi32SpeexAudioInputDataFrameSize[0], pszi16PcmAudioOutputDataFrame );
                }
                else //如果这个Speex格式音频输出数据帧丢失了。
                {
                    p_i64Temp = m_pclSpeexDecoder.Decode( null, 0, pszi16PcmAudioOutputDataFrame );
                }
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex解码器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex解码器失败。返回值：" + p_i64Temp );
                }
                break;
            }
            case 2: //如果使用Opus编解码器
            {
                Log.e( m_pclCurrentClassNameString, "音频处理线程：暂不支持使用Opus解码器。" );
            }
        }

        //调用用户定义的获取PCM格式音频输出数据帧函数
        UserGetPcmAudioOutputDataFrame( pszi16PcmAudioOutputDataFrame );
    }
    
    //本线程执行函数
    public void run()
    {
        this.setPriority( this.MAX_PRIORITY); //设置本线程优先级
        android.os.Process.setThreadPriority( -19 ); //设置本线程优先级

        int p_i32Temp;
        long p_i64Temp;

        reinit:
        while( true )
        {
            out:
            {
                m_i32ExitCode = -1; //先将本线程退出代码预设为初始化失败，如果初始化失败，这个退出代码就不用再设置了，如果初始化成功，再设置为成功的退出代码

                //调用用户定义的初始化函数
                p_i64Temp = UserInit();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的初始化函数成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的初始化函数失败。返回值：" + p_i64Temp );
                    break out;
                }

                //初始化AudioRecord类对象
                try
                {
                    m_pclAudioRecord = new AudioRecord( MediaRecorder.AudioSource.MIC,
                            m_i32SamplingRate,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            AudioRecord.getMinBufferSize( m_i32SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT ) );
                    if( m_pclAudioRecord.getState() == AudioRecord.STATE_INITIALIZED )
                    {
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化AudioRecord类对象成功。" );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化AudioRecord类对象失败。" );
                        break out;
                    }
                }
                catch( IllegalArgumentException e )
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化AudioRecord类对象失败。原因：" + e.getMessage() );
                    break out;
                }

                //初始化AudioTrack类对象
                try
                {
                    m_pclAudioTrack = new AudioTrack( AudioManager.STREAM_MUSIC,
                            m_i32SamplingRate,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            m_i32FrameSize * 2,
                            //AudioTrack.getMinBufferSize( m_i32SamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT ),
                            AudioTrack.MODE_STREAM );
                    if( m_pclAudioTrack.getState() == AudioTrack.STATE_INITIALIZED )
                    {
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化AudioTrack类对象成功。" );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化AudioTrack类对象失败。" );
                        break out;
                    }
                }
                catch( IllegalArgumentException e )
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化AudioTrack类对象失败。原因：" + e.getMessage() );
                    break out;
                }

                //使用什么声学回音消除器
                switch( m_i32UseWhatAec )
                {
                    case 0: //如果不使用声学回音消除器
                    {
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用声学回音消除器。" );
                        break;
                    }
                    case 1: //如果使用WebRtc声学回音消除器
                    {
                        m_pclWebRtcAec = new WebRtcAec();
                        p_i64Temp = m_pclWebRtcAec.Init( m_i32SamplingRate, m_i32WebRtcAecNlpMode );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc声学回音消除器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc声学回音消除器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 2: //如果使用WebRtc移动版声学回音消除器
                    {
                        m_pclWebRtcAecm = new WebRtcAecm();
                        p_i64Temp = m_pclWebRtcAecm.Init( m_i32SamplingRate, m_i32WebRtcAecmEchoMode, m_i32WebRtcAecmDelay );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc移动版声学回音消除器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化WebRtc移动版声学回音消除器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 3: //如果使用Speex声学回音消除器
                    {
                        m_pclSpeexAec = new SpeexAec();
                        p_i64Temp = m_pclSpeexAec.Init( m_i32SamplingRate, m_i32FrameSize, m_i32SpeexAecFilterLength );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex声学回音消除器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex声学回音消除器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                }

                //初始化WebRtc定点版噪音抑制器类对象
                if( m_i32IsUseWebRtcNsx != 0 )
                {
                    m_pclWebRtcNsx = new WebRtcNsx();
                    p_i64Temp = m_pclWebRtcNsx.Init( m_i32SamplingRate, m_i32WebRtcNsxPolicyMode );
                    if( p_i64Temp == 0 )
                    {
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象成功。返回值：" + p_i64Temp );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象失败。返回值：" + p_i64Temp );
                        break out;
                    }
                }

                //初始化Speex预处理器类对象
                if( m_i32IsUseSpeexPreprocessor != 0 )
                {
                    m_pclSpeexPreprocessor = new SpeexPreprocessor();
                    if( m_pclSpeexAec != null )
                        p_i64Temp = m_pclSpeexPreprocessor.Init( m_i32SamplingRate, m_i32FrameSize, m_i32SpeexPreprocessorIsUseNs, m_i32SpeexPreprocessorNoiseSuppress, m_i32SpeexPreprocessorIsUseVad, m_i32SpeexPreprocessorVadProbStart, m_i32SpeexPreprocessorVadProbContinue, m_i32SpeexPreprocessorIsUseAgc, m_i32SpeexPreprocessorAgcLevel, m_i32SpeexPreprocessorAgcMaxGain, m_i32SpeexPreprocessorIsUseRec, m_pclSpeexAec.GetSpeexEchoState(), m_i32SpeexPreprocessorEchoSuppress, m_i32SpeexPreprocessorEchoSuppressActive );
                    else
                        p_i64Temp = m_pclSpeexPreprocessor.Init( m_i32SamplingRate, m_i32FrameSize, m_i32SpeexPreprocessorIsUseNs, m_i32SpeexPreprocessorNoiseSuppress, m_i32SpeexPreprocessorIsUseVad, m_i32SpeexPreprocessorVadProbStart, m_i32SpeexPreprocessorVadProbContinue, m_i32SpeexPreprocessorIsUseAgc, m_i32SpeexPreprocessorAgcLevel, m_i32SpeexPreprocessorAgcMaxGain, 0, 0, 0, 0 );
                    if( p_i64Temp == 0 )
                    {
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象成功。返回值：" + p_i64Temp );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex预处理器类对象失败。返回值：" + p_i64Temp );
                        break out;
                    }
                }

                //使用什么编解码器
                switch( m_i32UseWhatCodec )
                {
                    case 0: //如果使用PCM原始数据
                    {
                        //什么都不要做
                        break;
                    }
                    case 1: //如果使用Speex编解码器
                    {
                        m_pclSpeexEncoder = new SpeexEncoder();
                        p_i64Temp = m_pclSpeexEncoder.Init( m_i32SamplingRate, m_i32SpeexCodecEncoderUseCbrOrVbr, m_i32SpeexCodecEncoderQuality, m_i32SpeexCodecEncoderComplexity, m_i32SpeexCodecEncoderPlcExpectedLossRate );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex编码器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex编码器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }

                        m_pclSpeexDecoder = new SpeexDecoder();
                        p_i64Temp = m_pclSpeexDecoder.Init( m_i32SamplingRate );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：初始化Speex解码器类对象成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：初始化Speex解码器类对象失败。返回值：" + p_i64Temp );
                            break out;
                        }
                        break;
                    }
                    case 2: //如果使用Opus编解码器
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：暂不支持使用Opus编解码器。" );
                        break out;
                    }
                }

                //初始化各个链表类对象
                m_pclAlreadyAudioInputLinkedList = new LinkedList<short[]>(); //初始化已录音的链表类对象
                m_pclAlreadyAudioOutputLinkedList = new LinkedList<short[]>(); //初始化已播放的链表类对象

                //初始化各个线程类对象
                m_clAudioInputThread = new AudioInputThread(); //初始化音频输入线程类对象
                m_clAudioOutputThread = new AudioOutputThread(); //初始化音频输出线程类对象

                //设置各个线程的退出标记
                m_i32ExitFlag = 0;
                m_clAudioInputThread.m_i32ExitFlag = 0;
                m_clAudioOutputThread.m_i32ExitFlag = 0;

                //设置各个线程的各个链表类对象
                m_clAudioInputThread.m_pclAlreadyAudioInputLinkedList = m_pclAlreadyAudioInputLinkedList;
                m_clAudioOutputThread.m_pclAlreadyAudioOutputLinkedList = m_pclAlreadyAudioOutputLinkedList;

                //设置各个线程的音频数据的采样频率
                m_clAudioInputThread.m_i32SamplingRate = m_i32SamplingRate;
                m_clAudioOutputThread.m_i32SamplingRate = m_i32SamplingRate;

                //设置各个线程的音频数据帧的长度
                m_clAudioInputThread.m_i32FrameSize = m_i32FrameSize;
                m_clAudioOutputThread.m_i32FrameSize = m_i32FrameSize;

                //设置各个线程的对象
                m_clAudioInputThread.m_pclAudioRecord = m_pclAudioRecord;
                m_clAudioInputThread.m_pclAudioOutputThread = m_clAudioOutputThread;
                m_clAudioInputThread.m_pclWebRtcAecm = m_pclWebRtcAecm;

                m_clAudioOutputThread.m_pclAudioProcessThread = this;
                m_clAudioOutputThread.m_pclAudioTrack = m_pclAudioTrack;

                //在启动各个线程前先开始录音，再开始播放，这样可以进一步保证音频输入输出数据帧同步，且音频输入线程走在输出数据线程的前面
                m_pclAudioRecord.startRecording(); //让AudioRecord类对象开始录音
                m_pclAudioTrack.play(); //让AudioTrack类对象开始播放

                //启动音频输入线程，让音频输入线程去启动音频输出线程
                m_clAudioInputThread.start();

                m_i32ExitCode = -2; //初始化已经成功了，再将本线程退出代码预设为处理失败，如果处理失败，这个退出代码就不用再设置了，如果处理成功，再设置为成功的退出代码
                Log.i( m_pclCurrentClassNameString, "音频处理线程：音频处理线程初始化完毕，正式开始处理音频数据。" );

                //以下变量要在初始化以后再声明才行
                short p_pszi16PcmAudioInputDataFrame[]; //PCM格式音频输入数据帧
                short p_pszi16PcmAudioOutputDataFrame[]; //PCM格式音频输出数据帧
                short p_pszi16PcmAudioResultDataFrame[] = new short[m_i32FrameSize]; //PCM格式音频结果数据帧
                short p_pszi16PcmAudioTempDataFrame[] = new short[m_i32FrameSize]; //PCM格式音频临时数据帧
                Integer p_pclVoiceActivityStatus = new Integer( 1 ); //语音活动状态，1表示有语音活动，0表示无语音活动，预设为1，为了让在没有使用语音活动检测的情况下永远都是有语音活动
                byte p_pszi8SpeexAudioInputDataFrame[] = ( m_i32UseWhatCodec == 1 )? new byte[m_i32FrameSize] : null; //Speex格式音频输入数据帧
                Integer p_pclSpeexAudioInputDataFrameSize = new Integer( 0 ); //Speex格式音频输入数据帧的内存长度，单位字节
                Integer p_pclSpeexAudioInputDataFrameIsNeedTrans = new Integer( 1 ); //Speex格式音频输入数据帧是否需要传输，1表示需要传输，0表示不需要传输，预设为1，为了让在没有使用非连续传输的情况下永远都是需要传输

                while( true )
                {
                    //调用用户定义的处理函数
                    p_i64Temp = UserProcess();
                    if( p_i64Temp == 0 )
                    {
                        //Log.i( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的处理函数成功。返回值：" + p_i64Temp );
                    }
                    else
                    {
                        Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的处理函数失败。返回值：" + p_i64Temp );
                        break out;
                    }

                    //开始处理一个音频输入数据帧
                    if( ( m_pclAlreadyAudioInputLinkedList.size() > 0 ) && ( m_pclAlreadyAudioOutputLinkedList.size() > 0 ) || //如果已录音的链表和已播放的链表中都有音频数据帧了，才开始处理
                        ( m_pclAlreadyAudioInputLinkedList.size() > 15 ) ) //如果已录音的链表里已经累积很多音频输入数据帧了，说明已播放的链表里迟迟没有音频输出数据帧，也开始处理
                    {
                        //先从已录音的链表中取出第一个音频输入数据帧
                        synchronized( m_pclAlreadyAudioInputLinkedList )
                        {
                            p_pszi16PcmAudioInputDataFrame = m_pclAlreadyAudioInputLinkedList.getFirst();
                            m_pclAlreadyAudioInputLinkedList.removeFirst();
                        }
                        Log.i( m_pclCurrentClassNameString, "音频处理线程：从已录音的链表中取出第一个音频输入数据帧。" );

                        //再从已播放的链表中取出第一个音频输出数据帧
                        if( m_pclAlreadyAudioOutputLinkedList.size() > 0) //如果已播放的链表里有音频输出数据帧
                        {
                            synchronized( m_pclAlreadyAudioOutputLinkedList )
                            {
                                p_pszi16PcmAudioOutputDataFrame = m_pclAlreadyAudioOutputLinkedList.getFirst();
                                m_pclAlreadyAudioOutputLinkedList.removeFirst();
                            }
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：从已播放的链表中取出第一个音频输出数据帧。" );
                        }
                        else //如果已播放的链表里没有音频输出数据帧
                        {
                            p_pszi16PcmAudioOutputDataFrame = new short[m_i32FrameSize];
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：已播放的链表中没有音频输出数据帧，用一个空帧代替。" );
                        }

                        //将音频输入数据帧复制到音频结果数据帧，方便处理
                        for( p_i32Temp = 0; p_i32Temp < p_pszi16PcmAudioResultDataFrame.length; p_i32Temp++ )
                            p_pszi16PcmAudioResultDataFrame[p_i32Temp] = p_pszi16PcmAudioInputDataFrame[p_i32Temp];

                        //使用什么声学回音消除器
                        switch( m_i32UseWhatAec )
                        {
                            case 0: //如果不使用声学回音消除器
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：不使用声学回音消除器。" );
                                break;
                            case 1: //如果使用WebRtc声学回音消除器
                                p_i64Temp = m_pclWebRtcAec.Echo( p_pszi16PcmAudioResultDataFrame, p_pszi16PcmAudioOutputDataFrame, p_pszi16PcmAudioTempDataFrame );
                                if( p_i64Temp == 0 )
                                {
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < p_pszi16PcmAudioTempDataFrame.length; p_i32Temp++ )
                                        p_pszi16PcmAudioResultDataFrame[p_i32Temp] = p_pszi16PcmAudioTempDataFrame[p_i32Temp];
                                }
                                else
                                {
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            case 2: //如果使用WebRtc移动版声学回音消除器
                                p_i64Temp = m_pclWebRtcAecm.Echo( p_pszi16PcmAudioResultDataFrame, p_pszi16PcmAudioOutputDataFrame, p_pszi16PcmAudioTempDataFrame );
                                if( p_i64Temp == 0 )
                                {
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc移动版声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < p_pszi16PcmAudioTempDataFrame.length; p_i32Temp++ )
                                        p_pszi16PcmAudioResultDataFrame[p_i32Temp] = p_pszi16PcmAudioTempDataFrame[p_i32Temp];
                                }
                                else
                                {
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc移动版声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            case 3: //如果使用Speex声学回音消除器
                                p_i64Temp = m_pclSpeexAec.Aec( p_pszi16PcmAudioResultDataFrame, p_pszi16PcmAudioOutputDataFrame, p_pszi16PcmAudioTempDataFrame );
                                if( p_i64Temp == 0 )
                                {
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex声学回音消除器成功。返回值：" + p_i64Temp );

                                    for( p_i32Temp = 0; p_i32Temp < p_pszi16PcmAudioTempDataFrame.length; p_i32Temp++ )
                                        p_pszi16PcmAudioResultDataFrame[p_i32Temp] = p_pszi16PcmAudioTempDataFrame[p_i32Temp];
                                }
                                else
                                {
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex声学回音消除器失败。返回值：" + p_i64Temp );
                                }
                                break;
                        }

                        //使用WebRtc定点噪音抑制器
                        if( m_i32IsUseWebRtcNsx != 0 )
                        {
                            p_i64Temp = m_pclWebRtcNsx.Process( m_i32SamplingRate, p_pszi16PcmAudioResultDataFrame, p_pszi16PcmAudioResultDataFrame.length );
                            if( p_i64Temp == 0 )
                            {
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点噪音抑制器成功。返回值：" + p_i64Temp );
                            }
                            else
                            {
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：使用WebRtc定点噪音抑制器失败。返回值：" + p_i64Temp );
                            }
                        }

                        //使用Speex预处理器
                        if( m_i32IsUseSpeexPreprocessor != 0 )
                        {
                            p_i64Temp = m_pclSpeexPreprocessor.Preprocess( p_pszi16PcmAudioResultDataFrame, p_pclVoiceActivityStatus );
                            if( p_i64Temp == 0 )
                            {
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex预处理器成功。语音活动状态：" + p_pclVoiceActivityStatus + "，返回值：" + p_i64Temp );
                            }
                            else
                            {
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex预处理器失败。返回值：" + p_i64Temp );
                            }
                        }

                        //使用什么编码器
                        switch( m_i32UseWhatCodec )
                        {
                            case 0: //如果使用PCM原始数据
                            {
                                Log.i( m_pclCurrentClassNameString, "音频处理线程：使用PCM原始数据。" );
                                break;
                            }
                            case 1: //如果使用Speex编码器
                            {
                                p_pclSpeexAudioInputDataFrameSize = new Integer( p_pszi8SpeexAudioInputDataFrame.length );
                                p_i64Temp = m_pclSpeexEncoder.Encode( p_pszi16PcmAudioResultDataFrame, p_pszi8SpeexAudioInputDataFrame, p_pclSpeexAudioInputDataFrameSize, p_pclSpeexAudioInputDataFrameIsNeedTrans );
                                if( p_i64Temp == 0 )
                                {
                                    Log.i( m_pclCurrentClassNameString, "音频处理线程：使用Speex编码器成功。Speex格式音频输入数据帧的内存长度：" + p_pclSpeexAudioInputDataFrameSize + "，Speex格式音频输入数据帧是否需要传输：" + p_pclSpeexAudioInputDataFrameIsNeedTrans + "，返回值：" + p_i64Temp );
                                }
                                else
                                {
                                    Log.e( m_pclCurrentClassNameString, "音频处理线程：使用Speex编码器失败。返回值：" + p_i64Temp );
                                }
                                break;
                            }
                            case 2: //如果使用Opus编码器
                            {
                                Log.e( m_pclCurrentClassNameString, "音频处理线程：暂不支持使用Opus编码器。" );
                                break out;
                            }
                        }

                        //调用用户定义的读取音频输入数据帧函数
                        p_i64Temp = UserReadAudioInputDataFrame( p_pszi16PcmAudioInputDataFrame, p_pszi16PcmAudioResultDataFrame, p_pclVoiceActivityStatus, p_pszi8SpeexAudioInputDataFrame, p_pclSpeexAudioInputDataFrameSize, p_pclSpeexAudioInputDataFrameIsNeedTrans );
                        if( p_i64Temp == 0 )
                        {
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的读取音频输入数据帧函数成功。返回值：" + p_i64Temp );
                        }
                        else
                        {
                            Log.e( m_pclCurrentClassNameString, "音频处理线程：调用用户定义的读取音频输入数据帧函数失败。返回值：" + p_i64Temp );
                            break out;
                        }

                        Log.i( m_pclCurrentClassNameString, "音频处理线程：本音频数据帧处理完毕。" );

                        if( m_i32ExitFlag != 0 ) //如果本线程退出标记为请求退出
                        {
                            m_i32ExitCode = 0; //处理已经成功了，再将本线程退出代码设置为正常退出
                            Log.i( m_pclCurrentClassNameString, "音频处理线程：接收到退出请求，开始准备退出。" );
                            break out;
                        }
                    }

                    SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高
                }
            }

            Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程开始退出。" );

            //设置各个线程的退出标记为请求退出
            if( m_clAudioInputThread != null ) m_clAudioInputThread.RequireExit();
            if( m_clAudioOutputThread != null ) m_clAudioOutputThread.RequireExit();

            //等待音频输入线程退出
            if( m_clAudioInputThread != null )
            {
                try
                {
                    m_clAudioInputThread.join();
                    m_clAudioInputThread = null;
                }
                catch( InterruptedException e )
                {

                }
            }
            //等待音频输出线程退出
            if( m_clAudioOutputThread != null )
            {
                try
                {
                    m_clAudioOutputThread.join();
                    m_clAudioOutputThread = null;
                }
                catch( InterruptedException e )
                {

                }
            }

            m_pclAlreadyAudioInputLinkedList = null; //清空已录音的链表
            m_pclAlreadyAudioOutputLinkedList = null; //清空已播放的链表

            if( m_pclWebRtcAec != null ) //销毁WebRtc声学回音消除器类对象
            {
                p_i64Temp = m_pclWebRtcAec.Destory();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc声学回音消除器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc声学回音消除器失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcAec = null;
            }
            if( m_pclWebRtcAecm != null ) //销毁WebRtc移动版声学回音消除器类对象
            {
                p_i64Temp = m_pclWebRtcAecm.Destory();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc移动版声学回音消除器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc移动版声学回音消除器失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcAecm = null;
            }
            if( m_pclSpeexAec != null ) //销毁Speex声学回音消除器类对象
            {
                p_i64Temp = m_pclSpeexAec.Destory();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex声学回音消除器失败。返回值：" + p_i64Temp );
                }
                m_pclSpeexAec = null;
            }
            if( m_pclWebRtcNsx != null ) //销毁WebRtc定点版噪音抑制器类对象
            {
                p_i64Temp = m_pclWebRtcNsx.Destory();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版噪音抑制器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁WebRtc定点版噪音抑制器失败。返回值：" + p_i64Temp );
                }
                m_pclWebRtcNsx = null;
            }
            if( m_pclSpeexPreprocessor != null ) //销毁Speex预处理器类对象
            {
                p_i64Temp = m_pclSpeexPreprocessor.Destory();
                if( p_i64Temp == 0 )
                {
                    Log.i( m_pclCurrentClassNameString, "音频处理线程：销毁Speex预处理器成功。返回值：" + p_i64Temp );
                }
                else
                {
                    Log.e( m_pclCurrentClassNameString, "音频处理线程：销毁Speex预处理器失败。返回值：" + p_i64Temp );
                }
                m_pclSpeexPreprocessor = null;
            }
            if( m_pclSpeexEncoder != null ) //销毁Speex编码器类对象
            {
                m_pclSpeexEncoder.Destory();
                m_pclSpeexEncoder = null;
            }
            if( m_pclSpeexDecoder != null ) //销毁Speex解码器类对象
            {
                m_pclSpeexDecoder.Destory();
                m_pclSpeexDecoder = null;
            }
            if( m_pclAudioRecord != null ) //销毁AudioRecord类对象
            {
                if( m_pclAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING )
                {
                    m_pclAudioRecord.stop();
                }
                m_pclAudioRecord.release();
                m_pclAudioRecord = null;
            }
            if( m_pclAudioTrack != null ) //销毁AudioTrack类对象
            {
                if( m_pclAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED )
                {
                    m_pclAudioTrack.stop();
                }
                m_pclAudioTrack.release();
                m_pclAudioTrack = null;
            }

            //调用用户定义的销毁函数
            if( UserDestory() == 0 ) //如果用户需要直接退出
            {
                Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程已退出。" );
                break reinit;
            }
            else //如果用户需用重新初始化
            {
                Log.i( m_pclCurrentClassNameString, "音频处理线程：本线程重新初始化。" );
            }
        }
    }
}