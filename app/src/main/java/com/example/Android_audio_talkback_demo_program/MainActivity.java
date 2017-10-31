package com.example.Android_audio_talkback_demo_program;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;

import HeavenTao.Audio.*;

//音频输入线程类
class AudioInputThread extends Thread
{
    String clCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    int iExitFlag; //本线程退出标记，0表示保持运行，1表示请求退出

    MainActivity clMainActivity; //主界面类对象的内存指针
    AudioProcessThread clAudioProcessThread; //音频处理线程类对象的内存指针

    AudioRecord m_clAudioRecord; //录音类

    int m_iFrameSize; //一帧音频数据的采样数量，包括：8000Hz为160个采样，16000Hz为320个采样，32000Hz为640个采样
    int m_iSamplingRate; //音频数据的采样频率，包括：8000Hz，16000Hz，32000Hz

    LinkedList<short []> m_clAlreadyAudioInputLinkedList; //已录音的链表

    AudioOutputThread m_clAudioOutputThread; //存放音频输出线程类对象的内存指针

    WebRtcAecm clWebRtcAecm; //WebRtc移动版声学回音消除器类对象

    public void run()
    {
        this.setPriority( this.MAX_PRIORITY ); //设置线程优先级

        short m_szhiTempAudioInputData[];
        int iAudioDataNumber;
        int iTemp;
        Date clLastDate;
        Date clNowDate;

        iAudioDataNumber = 0;
        clLastDate = new Date();
        Log.i( clCurrentClassNameString, "音频输入线程：开始录音准备" );

        //跳过刚开始录音到的空的音频数据帧
        while( true )
        {
            m_szhiTempAudioInputData = new short[m_iFrameSize];
            m_clAudioRecord.read(m_szhiTempAudioInputData, 0, m_szhiTempAudioInputData.length);

            for( iTemp = 0; iTemp < m_szhiTempAudioInputData.length; iTemp++ )
            {
                if( m_szhiTempAudioInputData[iTemp] != 0 )
                    break;
            }
            if( iTemp < m_szhiTempAudioInputData.length )
            {
                break;
            }
        }

        clNowDate = new Date();
        Log.i( clCurrentClassNameString, "音频输入线程：" + "录音准备耗时：" + (clNowDate.getTime() - clLastDate.getTime()) + "，丢弃掉刚开始录音到的空数据，现在正式开始录音并启动音频输出线程，为了保证音频输入线程走在输出数据线程的前面" );
        if( ( clWebRtcAecm != null ) && ( clWebRtcAecm.m_iDelay == -1 ) ) //自适应设置WebRtc移动版声学回音消除器的回音延迟时间
        {
            clWebRtcAecm.m_iDelay = (int)(( clNowDate.getTime() - clLastDate.getTime() ) / 3);
            Log.i( clCurrentClassNameString, "音频输入线程：自适应设置WebRtc移动版声学回音消除器的回音延迟时间为 " + clWebRtcAecm.m_iDelay + " 毫秒" );
        }
        clLastDate = clNowDate;

        m_clAudioOutputThread.start(); //启动音频输出线程

        //开始循环录音
        out:
        while( true )
        {
            m_szhiTempAudioInputData = new short[m_iFrameSize];
            m_clAudioRecord.read( m_szhiTempAudioInputData, 0, m_szhiTempAudioInputData.length );

            clNowDate = new Date();
            iAudioDataNumber++;
            Log.i( clCurrentClassNameString, "音频输入线程：" + "音频数据帧序号：" + iAudioDataNumber + "，" + "读取耗时：" + (clNowDate.getTime() - clLastDate.getTime()) + "，" + "已录音链表元素个数：" + m_clAlreadyAudioInputLinkedList.size() );
            clLastDate = clNowDate;

            //追加一帧PCM格式音频数据到已录音的链表
            synchronized( m_clAlreadyAudioInputLinkedList )
            {
                m_clAlreadyAudioInputLinkedList.addLast( m_szhiTempAudioInputData );
            }

            m_szhiTempAudioInputData = null;

            if( iExitFlag == 1 )
            {
                Log.i( clCurrentClassNameString, "音频输入线程：本线程接收到退出请求，开始准备退出" );
                break out;
            }
        }

        if( iExitFlag == 1 ) //如果本线程退出时是接收到了退出请求，就表示本线程是正常退出的
        {
            clAudioProcessThread.iAudioInputThreadExitStatus = 1;
        }
        else//如果本线程退出时没有接收到退出请求，就表示本线程是异常退出的
        {
            clAudioProcessThread.iAudioInputThreadExitStatus = 2;
        }

        Log.i( clCurrentClassNameString, "音频输入线程：本线程已退出" );
    }
}

//音频输出线程类
class AudioOutputThread extends Thread
{
    String clCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    int iExitFlag; //本线程退出标记，0表示保持运行，1表示请求退出

    MainActivity clMainActivity; //主界面类对象的内存指针
    AudioProcessThread clAudioProcessThread; //音频处理线程类对象的内存指针

    AudioTrack m_clAudioTrack; //播放类

    int m_iFrameSize; //一帧音频数据的采样数量，包括：8000Hz为160个采样，16000Hz为320个采样，32000Hz为640个采样
    int m_iSamplingRate; //音频数据的采样频率，包括：8000Hz，16000Hz，32000Hz

    Ajb clAjb; //自适应抖动缓冲器类对象
    Integer clAjbGetAudioDataSize; //从自适应抖动缓冲器中取出的音频数据的内存长度

    SpeexDecoder clSpeexDecoder; //Speex解码器类对象

    LinkedList<short []> m_clAlreadyAudioInputLinkedList; //已录音的链表
    LinkedList<short []> m_clAlreadyAudioOutputLinkedList; //已播放的链表

    public void run()
    {
        this.setPriority( this.MAX_PRIORITY ); //设置线程优先级

        byte m_szhhiSpeexAudioOutputData[];
        short m_szhiPcmAudioOutputData[];
        int iAudioDataNumber;
        Date clLastDate;
        Date clNowDate;
        int iTemp;

        clLastDate = new Date();
        iAudioDataNumber = 0;
        Log.i( clCurrentClassNameString, "音频输出线程：准备开始播放" );

        //开始循环播放
        out:
        while( true )
        {
            //从自适应抖动缓冲器取出第一帧音频数据，并播放
            if( clAjb != null ) //如果使用了自适应抖动缓冲器
            {
                if( clSpeexDecoder != null ) //如果使用了Speex解码器
                {
                    m_szhhiSpeexAudioOutputData = new byte[m_iFrameSize];
                    m_szhiPcmAudioOutputData = new short[m_iFrameSize];
                    clAjbGetAudioDataSize = new Integer( m_iFrameSize );
                    synchronized( clAjb )
                    {
                        clAjb.GetByteAudioData( m_szhhiSpeexAudioOutputData, clAjbGetAudioDataSize );
                    }

                    if( clAjbGetAudioDataSize.intValue() == 0 )
                    {
                        Log.i( clCurrentClassNameString, "音频输出线程：从自适应抖动缓冲器取出一帧无语音活动的音频数据" );
                        iTemp = clSpeexDecoder.Decode( null, 0, m_szhiPcmAudioOutputData );
                        if( iTemp == 0 )
                        {

                        }
                        else
                        {
                            Log.e( clCurrentClassNameString, "clSpeexDecoder.Decode() 出错！错误码：" + iTemp );
                            break out;
                        }
                    }
                    else
                    {
                        Log.i( clCurrentClassNameString, "音频输出线程：从自适应抖动缓冲器取出一帧有语音活动的音频数据" );
                        iTemp = clSpeexDecoder.Decode( m_szhhiSpeexAudioOutputData, clAjbGetAudioDataSize.intValue(), m_szhiPcmAudioOutputData );
                        if( iTemp == 0 )
                        {

                        }
                        else
                        {
                            Log.e( clCurrentClassNameString, "clSpeexDecoder.Decode() 出错！错误码：" + iTemp );
                            break out;
                        }
                    }
                }
                else //如果没使用Speex解码器
                {
                    m_szhiPcmAudioOutputData = new short[m_iFrameSize];
                    clAjbGetAudioDataSize = new Integer( m_iFrameSize );
                    synchronized( clAjb )
                    {
                        clAjb.GetShortAudioData( m_szhiPcmAudioOutputData, clAjbGetAudioDataSize );
                    }

                    if( clAjbGetAudioDataSize.intValue() == 0 )
                    {
                        Log.i( clCurrentClassNameString, "音频输出线程：从自适应抖动缓冲器取出一帧无语音活动的音频数据" );
                    }
                    else
                    {
                        Log.i( clCurrentClassNameString, "音频输出线程：从自适应抖动缓冲器取出一帧有语音活动的音频数据" );
                    }
                }

                clAjb.GetCurHaveActiveBufferSize( clAjbGetAudioDataSize );
                Log.i( clCurrentClassNameString, "音频输出线程：自适应抖动缓冲器的当前已缓冲有语音活动音频数据帧数量为 " + clAjbGetAudioDataSize.intValue() + " 个" );
                clAjb.GetCurHaveInactiveBufferSize( clAjbGetAudioDataSize );
                Log.i( clCurrentClassNameString, "音频输出线程：自适应抖动缓冲器的当前已缓冲无语音活动音频数据帧数量为 " + clAjbGetAudioDataSize.intValue() + " 个" );
                clAjb.GetCurNeedBufferSize( clAjbGetAudioDataSize );
                Log.i( clCurrentClassNameString, "音频输出线程：自适应抖动缓冲器的当前需缓冲音频数据帧的数量为 " + clAjbGetAudioDataSize.intValue() + " 个" );
            }
            else
            {
                Log.e( clCurrentClassNameString, "音频输出线程：没有使用自适应抖动缓冲器！无法取出音频数据" );
                break out;
            }

            //开始播放这一帧音频数据
            m_clAudioTrack.write( m_szhiPcmAudioOutputData, 0, m_szhiPcmAudioOutputData.length );

            clNowDate = new Date();
            iAudioDataNumber++;
            Log.i( clCurrentClassNameString, "音频输出线程：" + "音频数据帧序号：" + iAudioDataNumber + "，" + "写入耗时：" + (clNowDate.getTime() - clLastDate.getTime()) + "，" + "已播放链表元素个数：" + m_clAlreadyAudioOutputLinkedList.size() );
            clLastDate = clNowDate;

            //追加一帧音频数据到已播放的链表
            synchronized( m_clAlreadyAudioOutputLinkedList)
            {
                m_clAlreadyAudioOutputLinkedList.addLast( m_szhiPcmAudioOutputData );
            }

            m_szhiPcmAudioOutputData = null;

            if( iExitFlag == 1 )
            {
                Log.i( clCurrentClassNameString, "音频输出线程：本线程接收到退出请求，开始准备退出" );
                break out;
            }
        }

        if( iExitFlag == 1 ) //如果本线程退出时是接收到了退出请求，就表示本线程是正常退出的
        {
            clAudioProcessThread.iAudioOutputThreadExitStatus = 1;
        }
        else//如果本线程退出时没有接收到退出请求，就表示本线程是异常退出的
        {
            clAudioProcessThread.iAudioOutputThreadExitStatus = 2;
        }

        Log.i( clCurrentClassNameString, "音频输出线程：本线程已退出" );
    }
}

//音频处理线程类
class AudioProcessThread extends Thread
{
    String clCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    int iExitFlag; //本线程退出标记，0表示保持运行，1表示请求退出
    int iAudioInputThreadExitStatus; //音频输入线程退出状态，0表示正在运行，1表示正常退出，2表示异常退出
    int iAudioOutputThreadExitStatus; //音频输出线程退出状态，0表示正在运行，1表示正常退出，2表示异常退出
    int iIsCreateServerOrClient; //创建服务端或者客户端标记，1表示创建服务端，0表示创建客户端
    String m_clIPAddressString; //IP地址字符串
    int m_iPort; //端口
    MainActivity clMainActivity; //主界面类对象的内存指针
    Handler clMainActivityHandler; //主界面消息处理类对象的内存指针

    ServerSocket m_clServerSocket; //TCP协议服务端套接字类
    Socket m_clClientSocket; //TCP协议客户端套接字类
    long lLastPacketSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断
    long lLastPacketRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断

    int m_iFrameSize; //一帧音频数据的采样数量，包括：8000Hz为160个采样，16000Hz为320个采样，32000Hz为640个采样
    int m_iSamplingRate; //音频数据的采样频率，包括：8000Hz，16000Hz，32000Hz

    int iIsUseWebRtcAec; //是否使用WebRtc声学回音消除器，非0表示要使用，0表示不使用
    int iWebRtcAecNlpMode; //WebRtc声学回音消除器的非线性滤波模式，0表示保守, 1表示适中, 2表示积极

    int iIsUseWebRtcAecm; //是否使用WebRtc移动版声学回音消除器，非0表示要使用，0表示不使用
    int iWebRtcAecmEchoMode; //WebRtc移动版声学回音消除器的消除模式，最低为0，最高为4
    int iWebRtcAecmDelay; //WebRtc移动版声学回音消除器的回音延迟时间，单位毫秒，-1表示自适应设置

    int iIsUseSpeexAec; //是否使用Speex声学回音消除器，非0表示要使用，0表示不使用
    int iSpeexAecFilterLength; //Speex声学回音消除器的过滤器长度，单位毫秒

    int iIsUseWebRtcNsx; //是否使用WebRtc定点噪音抑制器，非0表示要使用，0表示不使用
    int iWebRtcNsxPolicyMode; //WebRtc定点噪音抑制器的策略模式，0表示轻微, 1表示适中, 2表示积极

    int iIsUseSpeexPreprocessor; //是否使用Speex预处理器，非0表示要使用，0表示不使用
    int iSpeexPreprocessorIsUseNs; //是否使用Speex预处理器的NS噪音抑制，非0表示要使用，0表示不使用
    int iSpeexPreprocessorNoiseSuppress; //Speex预处理器在NS噪音抑制时，噪音的最大程度衰减的分贝值
    int iSpeexPreprocessorIsUseVad; //是否使用Speex预处理器的VAD语音活动检测，非0表示要使用，0表示不使用
    int iSpeexPreprocessorVadProbStart; //Speex预处理器在VAD语音活动检测时，从无语音活动到有语音活动的判断百分比概率，最小为0，最大为100
    int iSpeexPreprocessorVadProbContinue; //Speex预处理器在VAD语音活动检测时，从有语音活动到无语音活动的判断百分比概率，最小为0，最大为100
    int iSpeexPreprocessorIsUseAgc; //是否使用Speex预处理器的AGC自动增益控制，非0表示要使用，0表示不使用
    int iSpeexPreprocessorAgcLevel; //Speex预处理器在AGC自动增益控制时，自动增益的等级，最小为1，最大为32768
    int iSpeexPreprocessorIsUseRec; //是否使用Speex预处理器的REC残余回音消除，非0表示要使用，0表示不使用
    int iSpeexPreprocessorEchoSuppress; //Speex预处理器在REC残余回音消除时，残余回音的最大程度衰减的分贝值
    int iSpeexPreprocessorEchoSuppressActive; //Speex预处理器在REC残余回音消除时，有近端语音活动时的残余回音的最大程度衰减的分贝值

    int iIsUsePCM; //是否使用PCM裸数据，非0表示要使用，0表示不使用

    int iIsUseSpeexCodec; //是否使用Speex编解码器，非0表示要使用，0表示不使用
    int iSpeexCodecEncoderIsUseVbr; //是否使用Speex编码器的动态比特率，非0表示要使用，0表示不使用
    int iSpeexCodecEncoderQuality; //Speex编码器的质量等级。质量等级越高，音质越好，压缩率越低。最低为0，最高为10。
    int iSpeexCodecEncoderComplexity; //Speex编码器的复杂度。复杂度越高，压缩率越高，CPU使用率越高，音质越好。最低为0，最高为10。
    int iSpeexCodecEncoderPlcExpectedLossRate; //Speex编码器的数据包丢失隐藏的预计丢失率。预计丢失率越高，抗网络抖动越强，压缩率越低。最低为0，最高为100。

    int iIsUseOpusCodec; //否使用Opus编解码器，非0表示要使用，0表示不使用

    int iIsUseAjb; //是否使用自适应抖动缓冲器，非0表示要使用，0表示不使用

    AudioRecord m_clAudioRecord; //录音类
    AudioTrack m_clAudioTrack; //播放类

    WebRtcAec clWebRtcAec; //WebRtc声学回音消除器类对象
    WebRtcAecm clWebRtcAecm; //WebRtc移动版声学回音消除器类对象
    SpeexAec clSpeexAec; //Speex声学回音消除器类对象
    WebRtcNsx clWebRtcNsx; //WebRtc定点版噪音抑制器类对象
    SpeexPreprocessor clSpeexPreprocessor; //Speex预处理器类对象
    SpeexEncoder clSpeexEncoder; //Speex编码器类对象
    SpeexDecoder clSpeexDecoder; //Speex解码器类对象
    Ajb clAjb; //自适应抖动缓冲器类对象

    int iIsSaveAudioDataFile; //是否保存音频数据到文件
    FileOutputStream clAudioInputFileOutputStream; //音频输入数据文件
    FileOutputStream clAudioOutputFileOutputStream; //音频输出数据文件
    FileOutputStream clAudioResultFileOutputStream; //音频结果数据文件

    LinkedList<short[]> m_clAlreadyAudioInputLinkedList; //存放已录音的链表类对象的内存指针
    LinkedList<short[]> m_clAlreadyAudioOutputLinkedList; //存放已播放的链表类对象的内存指针

    AudioInputThread m_clAudioInputThread; //存放音频输入线程类对象的内存指针
    AudioOutputThread m_clAudioOutputThread; //存放音频输出线程类对象的内存指针

    public AudioProcessThread()
    {
    }

    public void run()
    {
        this.setPriority( this.MAX_PRIORITY); //设置线程优先级

        int iClientSocketIsNormalExit = 0; //TCP协议客户端套接字是否正常退出，0表示否，1表示是
        short p_szhiPCMAudioInputData[]; //PCM格式音频输入数据
        short p_szhiPCMAudioOutputData[]; //PCM格式音频输出数据
        short p_szhiPCMAudioTempData[] = new short[m_iFrameSize]; //PCM格式音频临时数据
        Long clVoiceActivityStatus; //语音活动状态，1表示有语音活动，0表示无语音活动
        byte p_szhhiSpeexAudioInputData[] = new byte[m_iFrameSize]; //Speex格式音频输入数据
        Long p_clSpeexAudioInputDataSize = new Long( 0 ); //Speex格式音频输入数据的内存长度，单位字节，大于0表示本帧Speex格式音频数据需要传输，等于0表示本帧Speex格式音频数据不需要传输
        byte p_szhhiTempData[] = new byte[m_iFrameSize * 2 + 8];
        int iLastAudioDataIsActive; //最后一帧音频数据是否有语音活动，1表示有语音活动，0表示无语音活动
        int iSocketPrereadSize; //本次套接字数据包的预读长度
        long lSendAudioDataTimeStamp; //发送音频数据的时间戳
        long lRecvAudioDataTimeStamp; //接收音频数据的时间戳
        int iTemp;

        while( true )
        {
            out:
            while (true)
            {
                if( iIsCreateServerOrClient == 1 ) //如果是创建本地TCP协议服务端套接字接受远端TCP协议客户端套接字的连接
                {
                    if( m_clServerSocket == null )
                    {
                        try
                        {
                            m_clServerSocket = new ServerSocket();
                            m_clServerSocket.setReuseAddress( true );
                            m_clServerSocket.bind( new InetSocketAddress( m_clIPAddressString, m_iPort ), 1); //创建服务端套接字
                            m_clServerSocket.setSoTimeout( 500 ); //设置accept()函数的超时时间

                            String clInfoString = "创建TCP协议服务端套接字[" + m_clServerSocket.getInetAddress().getHostAddress() + ":" + m_clServerSocket.getLocalPort() + "]成功！";
                            Log.i ( clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                        }
                        catch (IOException e)
                        {
                            String clInfoString = "创建TCP协议服务端套接字[" + m_clIPAddressString + ":" + m_iPort + "]失败！原因：" + e.toString();
                            Log.e(clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                            break out;
                        }
                    }

                    while (true)
                    {
                        //接受客户端套接字的连接
                        try
                        {
                            m_clClientSocket = m_clServerSocket.accept();
                        }
                        catch (IOException e)
                        {

                        }

                        if( m_clClientSocket != null ) //如果成功接受了客户端套接字的连接，就开始传输数据
                        {
                            try
                            {
                                m_clServerSocket.close(); //关闭本地TCP协议服务端套接字，防止还有其他客户端继续连接
                            }
                            catch( IOException e )
                            {
                            }
                            m_clServerSocket = null;

                            String clInfoString = System.currentTimeMillis() + " 接受了远端TCP协议客户端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]与本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]的连接！";
                            Log.i ( clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                            break;
                        }

                        if( iExitFlag != 0 )
                        {
                            Log.i( clCurrentClassNameString, "本线程接收到退出请求，开始准备退出" );
                            break out;
                        }
                    }
                }
                else if( iIsCreateServerOrClient == 0 ) //如果是创建本地TCP协议客户端套接字连接远端TCP协议服务端套接字
                {
                    m_clClientSocket = new Socket();
                    try
                    {
                        m_clClientSocket.connect( new InetSocketAddress( m_clIPAddressString, m_iPort ), 5000 ); //连接指定的IP地址和端口号，超时时间为5秒

                        String clInfoString = "创建本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]与远端TCP协议服务端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]的连接成功！";
                        Log.i ( clCurrentClassNameString, clInfoString);
                        Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                    }
                    catch( IOException e )
                    {
                        try
                        {
                            m_clClientSocket.close();
                        }
                        catch( IOException e1 )
                        {

                        }
                        m_clClientSocket = null;

                        String clInfoString = " 创建本地TCP协议客户端套接字与远端TCP协议服务端套接字[" + m_clIPAddressString + ":" + m_iPort + "]的连接失败！原因：" + e.getMessage();
                        Log.i ( clCurrentClassNameString, clInfoString);
                        Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                        break out;
                    }
                }
                else
                {
                    Log.e( clCurrentClassNameString, "无法判断是创建服务端还是客户端！" );
                    break out;
                }

                try
                {
                    m_clClientSocket.setTcpNoDelay(true); //设置TCP协议客户端套接字的TCP_NODELAY选项为true
                }
                catch (SocketException e)
                {
                    break out;
                }

                //初始化AudioRecord类对象
                m_clAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        m_iSamplingRate,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(m_iSamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT));
                if( m_clAudioRecord.getState() == AudioRecord.STATE_INITIALIZED )
                {
                    Log.i ( clCurrentClassNameString, "初始化AudioRecord类对象成功！" );
                }
                else
                {
                    Log.e(clCurrentClassNameString, "初始化AudioRecord类对象失败！" );
                    break out;
                }

                //初始化AudioTrack类对象
                m_clAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        m_iSamplingRate,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        AudioTrack.getMinBufferSize(m_iSamplingRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT),
                        AudioTrack.MODE_STREAM);
                if( m_clAudioTrack.getState() == AudioTrack.STATE_INITIALIZED )
                {
                    Log.i ( clCurrentClassNameString, "初始化AudioTrack类对象成功！" );
                }
                else
                {
                    Log.e(clCurrentClassNameString, "初始化AudioTrack类对象失败！" );
                    break out;
                }

                //初始化WebRtc声学回音消除器类对象
                if( iIsUseWebRtcAec != 0 )
                {
                    clWebRtcAec = new WebRtcAec();
                    iTemp = clWebRtcAec.Init ( m_iSamplingRate, iWebRtcAecNlpMode);
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化WebRtc声学回音消除器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化WebRtc声学回音消除器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化WebRtc移动版声学回音消除器类对象
                if( iIsUseWebRtcAecm != 0 )
                {
                    clWebRtcAecm = new WebRtcAecm();
                    iTemp = clWebRtcAecm.Init ( m_iSamplingRate, iWebRtcAecmEchoMode, iWebRtcAecmDelay);
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化WebRtc移动版声学回音消除器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化WebRtc移动版声学回音消除器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化Speex声学回音消除器类对象
                if( iIsUseSpeexAec != 0 )
                {
                    clSpeexAec = new SpeexAec();
                    iTemp = clSpeexAec.Init ( m_iFrameSize, m_iSamplingRate, iSpeexAecFilterLength);
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex声学回音消除器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex声学回音消除器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化WebRtc定点版噪音抑制器类对象
                if( iIsUseWebRtcNsx != 0 )
                {
                    clWebRtcNsx = new WebRtcNsx();
                    iTemp = clWebRtcNsx.Init ( m_iSamplingRate, iWebRtcNsxPolicyMode);
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex预处理器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex预处理器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化Speex预处理器类对象
                if( iIsUseSpeexPreprocessor != 0 )
                {
                    clSpeexPreprocessor = new SpeexPreprocessor();
                    if( clSpeexAec != null )
                        iTemp = clSpeexPreprocessor.Init ( m_iSamplingRate, m_iFrameSize, iSpeexPreprocessorIsUseNs, iSpeexPreprocessorNoiseSuppress, iSpeexPreprocessorIsUseVad, iSpeexPreprocessorVadProbStart, iSpeexPreprocessorVadProbContinue, iSpeexPreprocessorIsUseAgc, iSpeexPreprocessorAgcLevel, iSpeexPreprocessorIsUseRec, clSpeexAec.GetSpeexEchoState().longValue(), iSpeexPreprocessorEchoSuppress, iSpeexPreprocessorEchoSuppressActive);
                    else
                        iTemp = clSpeexPreprocessor.Init ( m_iSamplingRate, m_iFrameSize, iSpeexPreprocessorIsUseNs, iSpeexPreprocessorNoiseSuppress, iSpeexPreprocessorIsUseVad, iSpeexPreprocessorVadProbStart, iSpeexPreprocessorVadProbContinue, iSpeexPreprocessorIsUseAgc, iSpeexPreprocessorAgcLevel, 0, 0, 0, 0 );
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex预处理器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex预处理器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化PCM裸数据
                if( iIsUsePCM != 0 )
                {
                    //暂时没有什么要做的
                }

                //初始化Speex编码器类对象
                if( iIsUseSpeexCodec != 0 )
                {
                    clSpeexEncoder = new SpeexEncoder();
                    iTemp = clSpeexEncoder.Init( m_iSamplingRate, iSpeexCodecEncoderIsUseVbr, iSpeexCodecEncoderQuality, iSpeexCodecEncoderComplexity, iSpeexCodecEncoderPlcExpectedLossRate );
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex编码器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex编码器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化Speex解码器类对象
                if( iIsUseSpeexCodec != 0 )
                {
                    clSpeexDecoder = new SpeexDecoder();
                    iTemp = clSpeexDecoder.Init ( m_iSamplingRate);
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex解码器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化Speex解码器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //初始化Opus编解码器
                if( iIsUseOpusCodec != 0 )
                {
                    //暂时没有什么要做的
                }

                //初始化自适应抖动缓冲器类对象
                if( iIsUseAjb != 0 )
                {
                    clAjb = new Ajb();
                    iTemp = clAjb.Init( m_iSamplingRate, m_iFrameSize, 0 );
                    if( iTemp == 0 )
                    {
                        Log.i ( clCurrentClassNameString, "初始化自适应抖动缓冲器类对象成功！返回值：" + iTemp );
                    }
                    else
                    {
                        Log.i ( clCurrentClassNameString, "初始化自适应抖动缓冲器类对象失败！返回值：" + iTemp );
                        break out;
                    }
                }

                //创建各个链表类对象
                m_clAlreadyAudioInputLinkedList = new LinkedList<short[]>(); //创建已录音的链表类对象
                m_clAlreadyAudioOutputLinkedList = new LinkedList<short[]>(); //创建已播放的链表类对象

                //初始化音频数据文件
                if( iIsSaveAudioDataFile != 0 )
                {
                    //创建音频输入数据文件
                    try
                    {
                        clAudioInputFileOutputStream = new FileOutputStream( Environment.getExternalStorageDirectory() + "/AudioInput.pcm" );
                        Log.i( clCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioInput.pcm 音频输入数据文件成功！");
                    }
                    catch( FileNotFoundException e )
                    {
                        Log.e( clCurrentClassNameString, "创建 AudioInput.pcm 音频输入数据文件失败！原因：" + e.toString() );
                        break out;
                    }

                    //创建音频输出数据文件
                    try
                    {
                        clAudioOutputFileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/AudioOutput.pcm");
                        Log.i( clCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioOutput.pcm 音频输出数据文件成功！");
                    }
                    catch( FileNotFoundException e )
                    {
                        Log.e( clCurrentClassNameString, "创建 AudioOutput.pcm 音频输出数据文件失败！原因：" + e.toString() );
                        break out;
                    }

                    //创建音频结果数据文件
                    try
                    {
                        clAudioResultFileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/AudioResult.pcm");
                        Log.i ( clCurrentClassNameString, "创建 " + Environment.getExternalStorageDirectory() + "/AudioResult.pcm 音频结果数据文件成功！");
                    }
                    catch( FileNotFoundException e )
                    {
                        Log.e(clCurrentClassNameString, "创建 AudioOutput.pcm 音频结果数据文件失败！原因：" + e.toString() );
                        break out;
                    }
                }

                //创建各个线程类对象
                m_clAudioInputThread = new AudioInputThread(); //创建音频输入线程类对象
                m_clAudioOutputThread = new AudioOutputThread(); //创建音频输出线程类对象

                //设置各个线程的退出标记
                m_clAudioInputThread.iExitFlag = 0;
                m_clAudioOutputThread.iExitFlag = 0;

                //设置各个线程的音频数据的采样频率、一帧音频数据的采样数量
                m_clAudioInputThread.m_iFrameSize = m_iFrameSize;
                m_clAudioInputThread.m_iSamplingRate = m_iSamplingRate;
                m_clAudioOutputThread.m_iFrameSize = m_iFrameSize;
                m_clAudioOutputThread.m_iSamplingRate = m_iSamplingRate;

                //设置各个线程的各个链表类对象
                m_clAudioInputThread.m_clAlreadyAudioInputLinkedList = m_clAlreadyAudioInputLinkedList;
                m_clAudioOutputThread.m_clAlreadyAudioInputLinkedList = m_clAlreadyAudioInputLinkedList;
                m_clAudioOutputThread.m_clAlreadyAudioOutputLinkedList = m_clAlreadyAudioOutputLinkedList;

                //设置各个线程的音频数据的采样频率、一帧音频数据的采样数量
                m_clAudioInputThread.m_iFrameSize = m_iFrameSize;
                m_clAudioInputThread.m_iSamplingRate = m_iSamplingRate;
                m_clAudioOutputThread.m_iFrameSize = m_iFrameSize;
                m_clAudioOutputThread.m_iSamplingRate = m_iSamplingRate;

                //设置各个线程的对象
                m_clAudioInputThread.clMainActivity = clMainActivity;
                m_clAudioInputThread.clAudioProcessThread = this;
                m_clAudioInputThread.m_clAudioRecord = m_clAudioRecord;
                m_clAudioInputThread.m_clAudioOutputThread = m_clAudioOutputThread;
                m_clAudioInputThread.clWebRtcAecm = clWebRtcAecm;

                m_clAudioOutputThread.clMainActivity = clMainActivity;
                m_clAudioOutputThread.clAudioProcessThread = this;
                m_clAudioOutputThread.m_clAudioTrack = m_clAudioTrack;
                m_clAudioOutputThread.clAjb = clAjb;
                m_clAudioOutputThread.clSpeexDecoder = clSpeexDecoder;

                //在启动各个线程前先开始录音，再开始播放，这样可以进一步保证音频输入输出数据帧同步，且音频输入线程走在输出数据线程的前面
                m_clAudioRecord.startRecording(); //让AudioRecord类对象开始录音
                m_clAudioTrack.play(); //让AudioTrack类对象开始播放

                //启动音频输入线程，让音频输入线程去启动音频输出线程
                m_clAudioInputThread.start();

                lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间为当前时间
                lLastPacketRecvTime = System.currentTimeMillis(); //存放最后一个数据包的接收时间为当前时间

                iClientSocketIsNormalExit = 0; //TCP协议客户端套接字是否正常退出为0，表示否
                clVoiceActivityStatus = new Long( 1 ); //设置语音活动状态为1，为了让在没有使用语音活动检测的情况下永远都是有语音活动
                iLastAudioDataIsActive = 0; //设置最后一帧音频数据是否有语音活动为0，表示无语音活动
                iSocketPrereadSize = 0; //本次套接字数据包的预读长度为0
                lSendAudioDataTimeStamp = 0; //发送音频数据的时间戳为0
                lRecvAudioDataTimeStamp = 0; //接收音频数据的时间戳为0

                {
                    String clInfoString = "开始进行音频对讲！";
                    Log.i ( clCurrentClassNameString, clInfoString);
                    Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                }

                //开始进行音频处理
                while( true )
                {
                    if( ( m_clAlreadyAudioInputLinkedList.size() > 0 ) && ( m_clAlreadyAudioOutputLinkedList.size() > 0 ) ) //如果已录音的链表和已播放的链表中都有数据了，才开始处理
                    {
                        //先从已录音的链表中取出第一帧音频输入数据
                        synchronized (m_clAlreadyAudioInputLinkedList)
                        {
                            p_szhiPCMAudioInputData = m_clAlreadyAudioInputLinkedList.getFirst();
                            m_clAlreadyAudioInputLinkedList.removeFirst();
                        }
                        Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 从已录音的链表中取出第一帧音频数据" );

                        //再从已播放的链表中取出第一帧音频输出数据
                        synchronized( m_clAlreadyAudioOutputLinkedList )
                        {
                            p_szhiPCMAudioOutputData = m_clAlreadyAudioOutputLinkedList.getFirst();
                            m_clAlreadyAudioOutputLinkedList.removeFirst();
                        }
                        Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 从已播放的链表中取出第一帧音频数据" );

                        //写入音频输入数据帧到文件
                        if( clAudioInputFileOutputStream != null )
                        {
                            for (iTemp = 0; iTemp < p_szhiPCMAudioInputData.length; iTemp++)
                            {
                                p_szhhiTempData[6 + iTemp * 2] = (byte) (p_szhiPCMAudioInputData[iTemp] & 0xFF);
                                p_szhhiTempData[6 + iTemp * 2 + 1] = (byte) ((p_szhiPCMAudioInputData[iTemp] & 0xFF00 ) >> 8);
                            }

                            try
                            {
                                clAudioInputFileOutputStream.write(p_szhhiTempData, 6, p_szhiPCMAudioInputData.length * 2);

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频输入数据帧到文件成功！" );
                            }
                            catch (IOException e)
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频输入数据帧到文件失败！" );
                            }
                        }

                        //写入音频输出数据帧到文件
                        if( clAudioOutputFileOutputStream != null )
                        {
                            for (iTemp = 0; iTemp < p_szhiPCMAudioInputData.length; iTemp++)
                            {
                                p_szhhiTempData[6 + iTemp * 2] = (byte) (p_szhiPCMAudioOutputData[iTemp] & 0xFF);
                                p_szhhiTempData[6 + iTemp * 2 + 1] = (byte) ((p_szhiPCMAudioOutputData[iTemp] & 0xFF00 ) >> 8);
                            }

                            try
                            {
                                clAudioOutputFileOutputStream.write(p_szhhiTempData, 6, p_szhiPCMAudioOutputData.length * 2);

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频输出数据帧到文件成功！" );
                            }
                            catch (IOException e)
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频输出数据帧到文件失败！" );
                            }
                        }

                        //开始使用各项功能

                        //使用WebRtc声学回音消除器
                        if( clWebRtcAec != null )
                        {
                            iTemp = clWebRtcAec.Echo( p_szhiPCMAudioInputData, p_szhiPCMAudioOutputData, p_szhiPCMAudioTempData );
                            if( iTemp == 0 )
                            {
                                for( iTemp = 0; iTemp < p_szhiPCMAudioTempData.length; iTemp++)
                                    p_szhiPCMAudioInputData[iTemp] = p_szhiPCMAudioTempData[iTemp];

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc声学回音消除器成功！" );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc声学回音消除器失败！错误码：" + iTemp );
                            }
                        }

                        //使用WebRtc移动版声学回音消除器
                        if( clWebRtcAecm != null )
                        {
                            iTemp = clWebRtcAecm.Echo( p_szhiPCMAudioInputData, p_szhiPCMAudioOutputData, p_szhiPCMAudioTempData );
                            if( iTemp == 0 )
                            {
                                for( iTemp = 0; iTemp < p_szhiPCMAudioTempData.length; iTemp++)
                                    p_szhiPCMAudioInputData[iTemp] = p_szhiPCMAudioTempData[iTemp];

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc移动版声学回音消除器成功！" );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc移动版声学回音消除器失败！错误码：" + iTemp );
                            }
                        }

                        //使用Speex声学回音消除器
                        if( clSpeexAec != null )
                        {
                            iTemp = clSpeexAec.Aec( p_szhiPCMAudioInputData, p_szhiPCMAudioOutputData, p_szhiPCMAudioTempData );
                            if( iTemp == 0 )
                            {
                                for( iTemp = 0; iTemp < p_szhiPCMAudioTempData.length; iTemp++)
                                    p_szhiPCMAudioInputData[iTemp] = p_szhiPCMAudioTempData[iTemp];

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex声学回音消除器成功！" );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex声学回音消除器失败！错误码：" + iTemp );
                            }
                        }

                        //使用WebRtc定点噪音抑制器
                        if( clWebRtcNsx != null )
                        {
                            iTemp = clWebRtcNsx.Process( m_iSamplingRate, p_szhiPCMAudioInputData, p_szhiPCMAudioInputData.length );
                            if( iTemp == 0 )
                            {
                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc定点噪音抑制器成功！" );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用WebRtc定点噪音抑制器失败！错误码：" + iTemp );
                            }
                        }

                        //使用Speex预处理器
                        if( clSpeexPreprocessor != null )
                        {
                            iTemp = clSpeexPreprocessor.Preprocess( p_szhiPCMAudioInputData, clVoiceActivityStatus );
                            if( iTemp == 0 )
                            {
                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex预处理器成功！语音活动状态：" + clVoiceActivityStatus );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex预处理器失败！错误码：" + iTemp );
                            }
                        }

                        //使用Speex编码器
                        if( clSpeexEncoder != null )
                        {
                            p_clSpeexAudioInputDataSize = new Long( p_szhhiSpeexAudioInputData.length );
                            iTemp = clSpeexEncoder.Encode( p_szhiPCMAudioInputData, p_szhhiSpeexAudioInputData, p_clSpeexAudioInputDataSize );
                            if( iTemp == 0 )
                            {
                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex编码器成功！" );
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 使用Speex编码器失败！错误码：" + iTemp );
                            }
                        }

                        //写入音频结果数据帧到文件
                        if( clAudioResultFileOutputStream != null )
                        {
                            for (iTemp = 0; iTemp < p_szhiPCMAudioInputData.length; iTemp++)
                            {
                                p_szhhiTempData[6 + iTemp * 2] = (byte) (p_szhiPCMAudioInputData[iTemp] & 0xFF);
                                p_szhhiTempData[6 + iTemp * 2 + 1] = (byte) ((p_szhiPCMAudioInputData[iTemp] & 0xFF00 ) >> 8);
                            }

                            try
                            {
                                clAudioResultFileOutputStream.write(p_szhhiTempData, 6, p_szhiPCMAudioInputData.length * 2);

                                Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频结果数据帧到文件成功！" );
                            }
                            catch (IOException e)
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 写入音频结果数据帧到文件失败！" );
                            }
                        }

                        //使用TCP协议客户端套接字发送音频输入数据帧
                        {
                            if( clVoiceActivityStatus.intValue() == 1 ) //如果本帧音频输入数据为有语音活动
                            {
                                if( iIsUsePCM != 0 ) //如果使用了PCM裸数据
                                {
                                    for( iTemp = 0; iTemp < p_szhiPCMAudioInputData.length; iTemp++ )
                                    {
                                        p_szhhiTempData[6 + iTemp * 2] = (byte) (p_szhiPCMAudioInputData[iTemp] & 0xFF);
                                        p_szhhiTempData[6 + iTemp * 2 + 1] = (byte) ((p_szhiPCMAudioInputData[iTemp] & 0xFF00 ) >> 8);
                                    }

                                    iTemp = p_szhiPCMAudioInputData.length * 2 + 4; //预读长度=PCM格式音频输入数据长度+时间戳长度
                                }
                                else if( clSpeexEncoder != null ) //如果使用了Speex编码器
                                {
                                    if( p_clSpeexAudioInputDataSize.intValue() > 0 ) //如果本帧Speex格式音频输入数据需要传输
                                    {
                                        for( iTemp = 0; iTemp < p_clSpeexAudioInputDataSize.intValue(); iTemp++ )
                                        {
                                            p_szhhiTempData[6 + iTemp] = p_szhhiSpeexAudioInputData[iTemp];
                                        }

                                        iTemp = p_clSpeexAudioInputDataSize.intValue() + 4; //预读长度=Speex格式音频输入数据长度+时间戳长度
                                    }
                                    else //如果本帧Speex格式音频输入数据不需要传输
                                    {
                                        iTemp = 4; //预读长度=时间戳长度
                                    }
                                }
                                else
                                {
                                    Log.e( clCurrentClassNameString, "没有使用任何编解码器，无法发送音频输入数据!" );
                                    break out;
                                }
                            }
                            else //如果本帧音频输入数据为无语音活动
                            {
                                iTemp = 4; //预读长度=时间戳长度
                            }

                            if( ( iTemp != 4 ) || //如果本帧音频输入数据为有语音活动，就发送
                                    ( ( iTemp == 4 ) && ( iLastAudioDataIsActive != 0 ) ) ) //如果本帧音频输入数据为无语音活动，且最后一帧音频数据为有语音活动，就发送
                            {
                                //设置预读长度
                                p_szhhiTempData[0] = (byte) (iTemp & 0xFF);
                                p_szhhiTempData[1] = (byte) ((iTemp & 0xFF00 ) >> 8);

                                //设置时间戳
                                p_szhhiTempData[2] = (byte) (lSendAudioDataTimeStamp & 0xFF);
                                p_szhhiTempData[3] = (byte) ((lSendAudioDataTimeStamp & 0xFF00 ) >> 8);
                                p_szhhiTempData[4] = (byte) ((lSendAudioDataTimeStamp & 0xFF0000 ) >> 16);
                                p_szhhiTempData[5] = (byte) ((lSendAudioDataTimeStamp & 0xFF000000 ) >> 24);

                                try
                                {
                                    OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                                    clOutputStream.write( p_szhhiTempData, 0, iTemp + 2 );
                                    clOutputStream.flush(); //防止出现Software caused connection abort异常
                                    lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间

                                    Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 发送一帧音频输入数据成功！时间戳：" + lSendAudioDataTimeStamp + "，总长度：" + iTemp );
                                }
                                catch (IOException e)
                                {
                                    String clInfoString = System.currentTimeMillis() + " 发送一帧音频输入数据失败！原因：" + e.getMessage();
                                    Log.e( clCurrentClassNameString, clInfoString );
                                    Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage( clMessage );
                                    break out;
                                }
                            }

                            lSendAudioDataTimeStamp += m_iFrameSize; //时间戳递增一帧音频输入数据的采样数量

                            //记录最后一帧音频数据是否有语音活动
                            if( iTemp != 4 ) iLastAudioDataIsActive = 1;
                            else iLastAudioDataIsActive = 0;
                        }

                        p_szhiPCMAudioInputData = null;
                        p_szhiPCMAudioOutputData = null;
                    }

                    //接收远端发送过来的音频输出数据帧，然后放入自适应抖动缓冲器
                    try
                    {
                        InputStream clInputStream = m_clClientSocket.getInputStream();
                        if( ( iSocketPrereadSize == 0 ) && ( clInputStream.available() >= 2 ) ) //如果还没有接收预读长度，且客户端套接字可以接收到预读长度
                        {
                            //接收本帧音频数据的预读长度
                            if( clInputStream.read( p_szhhiTempData, 0, 2 ) != 2 ) //如果接收到预读长度的长度不对，就返回
                            {
                                Log.e(clCurrentClassNameString, System.currentTimeMillis() + " 接收到预读长度的长度不对！" );
                                break out;
                            }
                            if( ( p_szhhiTempData[0] == 'E' ) && ( p_szhhiTempData[1] == 'X' ) ) //如果接收到一个退出包
                            {
                                lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间
                                iClientSocketIsNormalExit = 1; //设置TCP协议客户端套接字是否正常退出为1，表示是

                                String clInfoString = System.currentTimeMillis() + " 接收到一个退出包！";
                                Log.i ( clCurrentClassNameString, clInfoString);
                                Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                                break out;
                            }
                            //读取本帧音频数据的预读长度
                            iSocketPrereadSize = (p_szhhiTempData[0] & 0xFF) + (((int) (p_szhhiTempData[1] & 0xFF)) << 8);
                            if( iSocketPrereadSize == 0 ) //如果预读长度为0，表示这是一个心跳包，就更新一下时间即可
                            {
                                lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间
                                Log.i ( clCurrentClassNameString, System.currentTimeMillis() + " 接收到一个心跳包！");
                            }
                            else if( iSocketPrereadSize < 4 )
                            {
                                Log.e(clCurrentClassNameString, System.currentTimeMillis() + " 接收到预读长度为" + iSocketPrereadSize + "小于4，表示没有时间戳，无法继续接收！" );
                                break out;
                            }
                            if( iSocketPrereadSize > p_szhhiTempData.length )
                            {
                                Log.e(clCurrentClassNameString, System.currentTimeMillis() + " 接收到预读长度大于接收缓存区的长度，无法继续接收！" );
                                break out;
                            }
                        }
                        if( ( iSocketPrereadSize != 0 ) && ( clInputStream.available() >= iSocketPrereadSize ) ) //如果已经接收了预读长度，且本次数据包可以一次性接收完毕
                        {
                            //接收本帧音频输出数据的时间戳
                            if( clInputStream.read( p_szhhiTempData, 0, 4 ) != 4 ) //如果接收到时间戳长度不对，就返回
                            {
                                Log.e( clCurrentClassNameString, System.currentTimeMillis() + " 接收到时间戳长度不对！" );
                                break out;
                            }
                            //读取本帧音频输出数据的时间戳
                            lRecvAudioDataTimeStamp = (p_szhhiTempData[0] & 0xFF) + (((int) (p_szhhiTempData[1] & 0xFF)) << 8) + (((int) (p_szhhiTempData[2] & 0xFF)) << 16) + (((int) (p_szhhiTempData[3] & 0xFF)) << 24);
                            //接收音频数据帧
                            if( clInputStream.read( p_szhhiTempData, 0, iSocketPrereadSize - 4 ) != iSocketPrereadSize - 4 ) //如果接收到数据长度不对，就返回
                            {
                                Log.e(clCurrentClassNameString, System.currentTimeMillis() + " 接收到的数据长度与预读长度不同！" );
                                break out;
                            }
                            if( ( iIsUsePCM == 1 ) && ( iSocketPrereadSize - 4 != 0 ) && ( iSocketPrereadSize - 4 != m_iFrameSize * 2 ) ) //如果使用了PCM裸数据，且接收到的PCM格式音频输出数据帧不是静音数据，且接收到的PCM格式音频输出数据帧的数据长度与帧长度不同
                            {
                                Log.e(clCurrentClassNameString, System.currentTimeMillis() + " 接收到的PCM格式音频数据帧的数据长度与帧长度不同！" );
                                break out;
                            }
                            lLastPacketRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间
                            Log.i ( clCurrentClassNameString, System.currentTimeMillis() + " 接收一帧音频输出数据成功！时间戳：" + lRecvAudioDataTimeStamp + "，总长度：" + iSocketPrereadSize);

                            //将本帧音频输出数据存放入自适应抖动缓冲器
                            if( clAjb != null ) //如果使用了自适应抖动缓冲器
                            {
                                if( iSocketPrereadSize - 4 == 0 ) //如果本帧音频输出数据为无语音活动
                                {
                                    if( clSpeexDecoder != null ) //如果使用了Speex解码器
                                    {
                                        //将本帧音频输出数据放入自适应抖动缓冲器
                                        synchronized( clAjb )
                                        {
                                            clAjb.PutByteAudioData( null, 0, lRecvAudioDataTimeStamp );
                                        }
                                    }
                                    else //如果没有使用Speex解码器
                                    {
                                        //将本帧音频输出数据放入自适应抖动缓冲器
                                        synchronized( clAjb )
                                        {
                                            clAjb.PutShortAudioData( null, 0, lRecvAudioDataTimeStamp );
                                        }
                                    }
                                }
                                else //如果本帧音频输出数据为有语音活动
                                {
                                    if( clSpeexDecoder != null ) //如果使用了Speex解码器
                                    {
                                        //将本帧音频输出数据放入自适应抖动缓冲器
                                        synchronized (clAjb)
                                        {
                                            clAjb.PutByteAudioData( p_szhhiTempData, iSocketPrereadSize - 4, lRecvAudioDataTimeStamp );
                                        }
                                    }
                                    else //如果没有使用Speex解码器
                                    {
                                        for( iTemp = 0; iTemp < m_iFrameSize; iTemp++ )
                                        {
                                            p_szhiPCMAudioTempData[iTemp] = (short)(((short)p_szhhiTempData[iTemp * 2]) & 0xFF | ((short)p_szhhiTempData[iTemp * 2 + 1]) << 8);
                                        }

                                        //将本帧音频输出数据放入自适应抖动缓冲器
                                        synchronized (clAjb)
                                        {
                                            clAjb.PutShortAudioData( p_szhiPCMAudioTempData, p_szhiPCMAudioTempData.length, lRecvAudioDataTimeStamp );
                                        }
                                    }
                                }
                            }
                            else
                            {
                                Log.e( clCurrentClassNameString, "没有使用自适应抖动缓冲器！无法接收音频数据" );

                                break out;
                            }

                            iSocketPrereadSize = 0; //清空预读长度
                        }
                    }
                    catch( IOException e )
                    {
                        String clInfoString = System.currentTimeMillis() + " 接收一帧音频输出数据失败！原因：" + e.getMessage();
                        Log.e(clCurrentClassNameString, clInfoString);
                        Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                        break out;
                    }

                    //发送心跳包
                    if( System.currentTimeMillis() - lLastPacketSendTime >= 500 ) //如果超过500毫秒没有发送任何数据包，就发送一个心跳包
                    {
                        //设置预读长度
                        p_szhhiTempData[0] = (byte) (0 );
                        p_szhhiTempData[1] = (byte) (0 );

                        try
                        {
                            OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                            clOutputStream.write( p_szhhiTempData, 0, 2 );
                            clOutputStream.flush(); //防止出现Software caused connection abort异常
                            lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间
                            Log.i ( clCurrentClassNameString, System.currentTimeMillis() + " 发送一个心跳包成功！");
                        }
                        catch (IOException e)
                        {
                            String clInfoString = System.currentTimeMillis() + " 发送一个心跳包失败！原因：" + e.getMessage();
                            Log.e(clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                            break out;
                        }
                    }

                    //判断连接是否中断
                    if( System.currentTimeMillis() - lLastPacketRecvTime > 2000 ) //如果超过2000毫秒没有接收任何数据包，就判定连接已经断开了
                    {
                        String clInfoString = System.currentTimeMillis() + " 超过2000毫秒没有接收任何数据包，判定连接已经断开了！";
                        Log.e(clCurrentClassNameString, clInfoString);
                        Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                        break out;
                    }

                    if( iExitFlag != 0 ) //如果本线程接收到退出请求
                    {
                        Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 本线程接收到退出请求，开始准备退出" );

                        iClientSocketIsNormalExit = 1; //设置TCP协议客户端套接字是否正常退出为1，表示是

                        //设置预读长度
                        p_szhhiTempData[0] = (byte) ('E');
                        p_szhhiTempData[1] = (byte) ('X');

                        try
                        {
                            OutputStream clOutputStream = m_clClientSocket.getOutputStream();
                            clOutputStream.write( p_szhhiTempData, 0, 2 );
                            clOutputStream.flush(); //防止出现Software caused connection abort异常
                            lLastPacketSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间
                            
                            String clInfoString = System.currentTimeMillis() + " 发送一个退出包成功！";
                            Log.i ( clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);
                        }
                        catch (IOException e)
                        {
                            String clInfoString = System.currentTimeMillis() + " 发送一个退出包失败！原因：" + e.getMessage();
                            Log.e(clCurrentClassNameString, clInfoString);
                            Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                            break out;
                        }

                        break out;
                    }
                    else if( ( iAudioInputThreadExitStatus != 0 ) || ( iAudioOutputThreadExitStatus != 0 ) ) //如果本线程发现有子线程已经退出
                    {
                        Log.i( clCurrentClassNameString, System.currentTimeMillis() + " 本线程发现有子线程已经退出，开始准备退出" );

                        break out;
                    }

                    SystemClock.sleep(1); //暂停一下，避免CPU使用率过高
                }
            }

            Log.i ( clCurrentClassNameString, "本线程开始退出" );

            //设置各个线程的正常退出标记
            if( m_clAudioInputThread != null ) m_clAudioInputThread.iExitFlag = 1;
            if( m_clAudioOutputThread != null ) m_clAudioOutputThread.iExitFlag = 1;

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

            if( clAudioInputFileOutputStream != null ) //关闭音频输入数据文件
            {
                try
                {
                    clAudioInputFileOutputStream.close();
                    clAudioInputFileOutputStream = null;
                }
                catch (IOException e)
                {

                }
            }
            if( clAudioOutputFileOutputStream != null ) //关闭音频输出数据文件
            {
                try
                {
                    clAudioOutputFileOutputStream.close();
                    clAudioOutputFileOutputStream = null;
                }
                catch (IOException e)
                {

                }
            }
            if( clAudioResultFileOutputStream != null ) //关闭音频结果数据文件
            {
                try
                {
                    clAudioResultFileOutputStream.close();
                    clAudioResultFileOutputStream = null;
                }
                catch (IOException e)
                {

                }
            }

            m_clAlreadyAudioInputLinkedList = null; //清空已录音的链表
            m_clAlreadyAudioOutputLinkedList = null; //清空已播放的链表

            if( clWebRtcAec != null ) //销毁WebRtc声学回音消除器类对象
            {
                clWebRtcAec.Destory();
                clWebRtcAec = null;
            }
            if( clWebRtcAecm != null ) //销毁WebRtc移动版声学回音消除器类对象
            {
                clWebRtcAecm.Destory();
                clWebRtcAecm = null;
            }
            if( clSpeexAec != null ) //销毁Speex声学回音消除器类对象
            {
                clSpeexAec.Destory();
                clSpeexAec = null;
            }
            if( clWebRtcNsx != null ) //销毁WebRtc定点版噪音抑制器类对象
            {
                clWebRtcNsx.Destory();
                clWebRtcNsx = null;
            }
            if( clSpeexPreprocessor != null ) //销毁Speex预处理器类对象
            {
                clSpeexPreprocessor.Destory();
                clSpeexPreprocessor = null;
            }
            if( clSpeexEncoder != null ) //销毁Speex编码器类对象
            {
                clSpeexEncoder.Destory();
                clSpeexEncoder = null;
            }
            if( clSpeexDecoder != null ) //销毁Speex解码器类对象
            {
                clSpeexDecoder.Destory();
                clSpeexDecoder = null;
            }
            if( m_clAudioRecord != null ) //销毁AudioRecord类对象
            {
                if( m_clAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING )
                {
                    m_clAudioRecord.stop();
                }
                m_clAudioRecord.release();
                m_clAudioRecord = null;
            }
            if( m_clAudioTrack != null ) //销毁AudioTrack类对象
            {
                if( m_clAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED )
                {
                    m_clAudioTrack.stop();
                }
                m_clAudioTrack.release();
                m_clAudioTrack = null;
            }
            if( m_clServerSocket != null ) //销毁TCP协议服务端套接字
            {
                try
                {
                    String clInfoString = "已关闭TCP协议服务端套接字[" + m_clServerSocket.getInetAddress().getHostAddress() + ":" + m_clServerSocket.getLocalPort() + "]！";
                    Log.i( clCurrentClassNameString, clInfoString );
                    Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                    m_clServerSocket.close();
                }
                catch (IOException e)
                {
                }
                m_clServerSocket = null;
            }
            if( m_clClientSocket != null ) //销毁TCP协议客户端套接字
            {
                try
                {
                    String clInfoString = "已断开本地TCP协议客户端套接字[" + m_clClientSocket.getLocalAddress().getHostAddress() + ":" + m_clClientSocket.getLocalPort() + "]与远端TCP协议客户端套接字[" + m_clClientSocket.getInetAddress().getHostAddress() + ":" + m_clClientSocket.getPort() + "]的连接！";
                    Log.i ( clCurrentClassNameString, clInfoString);
                    Message clMessage = new Message();clMessage.what = 2;clMessage.obj = clInfoString;clMainActivityHandler.sendMessage(clMessage);

                    m_clClientSocket.close();
                }
                catch (IOException e)
                {
                }
                m_clClientSocket = null;
            }

            if( ( iIsCreateServerOrClient == 1 ) && ( iExitFlag == 0 ) ) //如果当前是创建服务端，且本线程未接收到退出请求
            {
                iAudioInputThreadExitStatus = 0;
                iAudioOutputThreadExitStatus = 0;

                Log.i ( clCurrentClassNameString, "由于当前是创建服务端，且本线程未接收到退出请求，本线程继续保持监听" );
            }
            else if( ( iIsCreateServerOrClient == 0 ) && ( iClientSocketIsNormalExit == 0 ) && ( iExitFlag == 0 ) ) //如果当前是创建客户端，且TCP协议客户端套接字不是正常退出，且本线程未接收到退出请求
            {
                iAudioInputThreadExitStatus = 0;
                iAudioOutputThreadExitStatus = 0;

                Log.i ( clCurrentClassNameString, "当前是创建客户端，且TCP协议客户端套接字不是正常退出，且本线程未接收到退出请求，本线程在1秒后重连" );
                SystemClock.sleep( 1000 ); //暂停1秒
            }
            else //否则退出
            {
                break;
            }
        }

        //发送本线程退出消息给主界面线程
        Message clMessage = new Message();
        clMessage.what = 1;
        clMainActivityHandler.sendMessage( clMessage );

        Log.i ( clCurrentClassNameString, "本线程已退出" );
    }
}

//初始化线程类
class InitThread extends Thread
{
    String clCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    View clLayoutActivityMainView; //主界面布局控件的内存指针

    public void run()
    {
        String clIPAddressString = "127.0.0.1";
        String clPortString = "12345";

        //准备IP地址控件的内容为本机IP
        try
        {
            for( Enumeration<NetworkInterface> clEnumerationNetworkInterface = NetworkInterface.getNetworkInterfaces(); clEnumerationNetworkInterface.hasMoreElements();)
            {
                NetworkInterface clNetworkInterface = clEnumerationNetworkInterface.nextElement();
                if( clNetworkInterface.getName().compareTo( "usbnet0" ) != 0 ) //如果该网络接口设备不是USB接口对应的网络接口设备
                {
                    for( Enumeration<InetAddress> enumIpAddr = clNetworkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                    {
                        InetAddress clInetAddress = enumIpAddr.nextElement();
                        if( (!clInetAddress.isLoopbackAddress()) && (clInetAddress.getAddress().length == 4)) //如果该IP地址不是回环地址，且是IPv4的
                        {
                            clIPAddressString = clInetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        }
        catch( SocketException e)
        {
            clIPAddressString = "127.0.0.1";
        }

        //设置IP地址控件的内容
        ((EditText)clLayoutActivityMainView.findViewById( R.id.IPAddressEdit )).setText( clIPAddressString );

        //设置端口控件的内容
        ((EditText)clLayoutActivityMainView.findViewById( R.id.PortEdit )).setText( clPortString );
    }
}

public class MainActivity extends AppCompatActivity
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so
        System.loadLibrary( "WebRtcAec" ); //加载libWebRtcAec.so
        System.loadLibrary( "WebRtcAecm" ); //加载libWebRtcAecm.so
        System.loadLibrary( "WebRtcNs" ); //加载libWebRtcNs.so
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so
        System.loadLibrary( "Speex" ); //加载libSpeex.so
        System.loadLibrary( "Ajb" ); //加载libAjb.so
    }

    String clCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    View clLayoutActivityMainView; //主界面布局控件的内存指针
    View clLayoutActivitySettingView; //设置界面布局控件的内存指针
    View clLayoutActivityWebRtcAecView; //WebRtc声学回音消除器设置布局控件的内存指针
    View clLayoutActivityWebRtcAecmView; //WebRtc移动版声学回音消除器设置布局控件的内存指针
    View clLayoutActivitySpeexAecView; //Speex声学回音消除器设置布局控件的内存指针
    View clLayoutActivityWebRtcNsxView; //WebRtc定点噪音抑制器设置布局控件的内存指针
    View clLayoutActivitySpeexPreprocessorView; //Speex预处理器设置布局控件的内存指针
    View clLayoutActivitySpeexCodecView; //Speex编解码器设置布局控件的内存指针
    View clLayoutActivityReadMeView; //说明界面布局控件的内存指针
    View clLayoutActivityCurrentView; //当前界面布局控件的内存指针

    MainActivity clMainActivity; //主界面类对象的内存指针
    AudioProcessThread clAudioProcessThread; //音频处理线程类对象的内存指针
    Handler clMainActivityHandler; //主界面消息处理类对象的内存指针

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from( this );
        clLayoutActivityMainView = layoutInflater.inflate( R.layout.activity_main, null );
        clLayoutActivitySettingView = layoutInflater.inflate( R.layout.activity_setting, null );
        clLayoutActivityWebRtcAecView  = layoutInflater.inflate( R.layout.activity_webrtcaec, null );
        clLayoutActivityWebRtcAecmView = layoutInflater.inflate( R.layout.activity_webrtcaecm, null );
        clLayoutActivitySpeexAecView = layoutInflater.inflate( R.layout.activity_speexaec, null );
        clLayoutActivityWebRtcNsxView = layoutInflater.inflate( R.layout.activity_webrtcnsx, null );
        clLayoutActivitySpeexPreprocessorView = layoutInflater.inflate( R.layout.activity_speexpreprocessor, null );
        clLayoutActivitySpeexCodecView = layoutInflater.inflate( R.layout.activity_speexcodec, null );
        clLayoutActivityReadMeView = layoutInflater.inflate( R.layout.activity_readme, null );

        setContentView( clLayoutActivityMainView ); //设置界面的内容为主界面
        clLayoutActivityCurrentView = clLayoutActivityMainView;

        clMainActivity = this;

        //请求录音权限、网络权限、写入存储权限
        //if( ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1 );
        }

        //启动初始化线程
        InitThread clInitThread;
        clInitThread = new InitThread();
        clInitThread.clLayoutActivityMainView = clLayoutActivityMainView;
        clInitThread.start();

        //初始化消息处理类对象
        clMainActivityHandler = new Handler()
        {
            public void handleMessage( Message clMessage )
            {
                if( clMessage.what == 1 ) //如果是音频处理线程正常退出的消息
                {
                    clAudioProcessThread = null;

                    ((Button)findViewById( R.id.CreateServerButton )).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”
                    ((Button)findViewById( R.id.ConnectServerButton )).setEnabled( true ); //设置连接服务端按钮为可用
                    ((Button)findViewById( R.id.ConnectServerButton )).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”
                    ((Button)findViewById( R.id.CreateServerButton )).setEnabled( true ); //设置创建服务端按钮为可用
                    ((Button)findViewById( R.id.SettingButton )).setEnabled( true ); //设置设置按钮为可用
                }
                if( clMessage.what == 2 ) //如果是显示日志的消息
                {
                    LinearLayout clLogLinearLayout = (LinearLayout)clLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
                    TextView clTempTextView = new TextView(clMainActivity);
                    clTempTextView.setText( (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format( new Date() ) + "：" + (String)(clMessage.obj) );
                    clLogLinearLayout.addView(clTempTextView);
                }
            }
        };
    }

    //清空日志按钮
    public void OnClickClearLog( View v )
    {
        LinearLayout clLogLinearLayout = (LinearLayout)clLayoutActivityMainView.findViewById( R.id.LogLinearLayout );
        clLogLinearLayout.removeAllViews();
    }

    //返回键
    @Override
    public void onBackPressed()
    {
        if( clLayoutActivityCurrentView == clLayoutActivityMainView )
        {
            Log.i( clCurrentClassNameString, "用户在主界面按下返回键，本软件退出" );
            System.exit( 0 );
        }
        else if( clLayoutActivityCurrentView == clLayoutActivitySettingView )
        {
            this.OnClickSettingOk( null );
        }
        else if( ( clLayoutActivityCurrentView == clLayoutActivityWebRtcAecView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivityWebRtcAecmView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexAecView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivityWebRtcNsxView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexPreprocessorView ) ||
                 ( clLayoutActivityCurrentView == clLayoutActivitySpeexCodecView ) )
        {
            this.OnWebRtcAecSettingOkClick( null );
        }
        else if( clLayoutActivityCurrentView == clLayoutActivityReadMeView )
        {
            this.OnClickReadMeOk( null );
        }
    }

    //创建服务器或连接服务器按钮
    public void OnClickCreateServerAndConnectServer( View v )
    {
        int p_iResult = -1;

        out:
        while( true )
        {
            if( clAudioProcessThread == null ) //如果音频处理线程还没有启动
            {
                Log.i( clCurrentClassNameString, "开始启动音频处理线程" );

                clAudioProcessThread = new AudioProcessThread();

                if( v.getId() == R.id.CreateServerButton )
                {
                    clAudioProcessThread.iIsCreateServerOrClient = 1; //标记创建服务端接受客户端
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    clAudioProcessThread.iIsCreateServerOrClient = 0; //标记创建客户端连接服务端
                }

                clAudioProcessThread.clMainActivity = this;
                clAudioProcessThread.clMainActivityHandler = clMainActivityHandler;

                //设置IP地址字符串、端口、音频播放线程启动延迟
                clAudioProcessThread.m_clIPAddressString = ((EditText) clLayoutActivityMainView.findViewById(R.id.IPAddressEdit)).getText().toString();
                clAudioProcessThread.m_iPort = Integer.parseInt(((EditText) clLayoutActivityMainView.findViewById(R.id.PortEdit)).getText().toString());

                //设置音频数据的采样频率、一帧音频数据的采样数量
                String clTempString = ((Spinner) clLayoutActivitySettingView.findViewById(R.id.SamplingRate)).getSelectedItem().toString();
                if( clTempString.equals( "8000Hz" ) )
                {
                    clAudioProcessThread.m_iFrameSize = 160;
                    clAudioProcessThread.m_iSamplingRate = 8000;
                }
                else if( clTempString.equals( "16000Hz" ) )
                {
                    clAudioProcessThread.m_iFrameSize = 320;
                    clAudioProcessThread.m_iSamplingRate = 16000;
                }
                else if( clTempString.equals( "32000Hz" ) )
                {
                    clAudioProcessThread.m_iFrameSize = 640;
                    clAudioProcessThread.m_iSamplingRate = 32000;
                }

                //判断是否使用Speex声学回音消除器
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseSpeexAec)).isChecked() )
                {
                    clAudioProcessThread.iIsUseSpeexAec = 1;

                    try
                    {
                        clAudioProcessThread.iSpeexAecFilterLength = Integer.parseInt(((TextView) clLayoutActivitySpeexAecView.findViewById(R.id.SpeexAecFilterLength)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseSpeexAec = 0;
                }

                //判断是否使用WebRtc声学回音消除器
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseWebRtcAec)).isChecked() )
                {
                    clAudioProcessThread.iIsUseWebRtcAec = 1;

                    try
                    {
                        clAudioProcessThread.iWebRtcAecNlpMode = Integer.parseInt(((TextView) clLayoutActivityWebRtcAecView.findViewById(R.id.WebRtcAecNlpMode)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseWebRtcAec = 0;
                }

                //判断是否使用WebRtc移动版声学回音消除器
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseWebRtcAecm)).isChecked() )
                {
                    clAudioProcessThread.iIsUseWebRtcAecm = 1;

                    try
                    {
                        clAudioProcessThread.iWebRtcAecmEchoMode = Integer.parseInt(((TextView) clLayoutActivityWebRtcAecmView.findViewById(R.id.WebRtcAecmEchoMode)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }

                    try
                    {
                        clAudioProcessThread.iWebRtcAecmDelay = Integer.parseInt(((TextView) clLayoutActivityWebRtcAecmView.findViewById(R.id.WebRtcAecmDelay)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseWebRtcAecm = 0;
                }

                //判断是否使用WebRtc定点噪音抑制器
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseWebRtcNsx)).isChecked() )
                {
                    clAudioProcessThread.iIsUseWebRtcNsx = 1;

                    try
                    {
                        clAudioProcessThread.iWebRtcNsxPolicyMode = Integer.parseInt(((TextView) clLayoutActivityWebRtcNsxView.findViewById(R.id.WebRtcNsxPolicyMode)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
                        break out;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseWebRtcNsx = 0;
                }

                //判断是否使用Speex预处理
                if( ((CheckBox)clLayoutActivitySettingView.findViewById( R.id.CheckBoxIsUseSpeexPreprocessor )).isChecked() )
                {
                    clAudioProcessThread.iIsUseSpeexPreprocessor = 1;

                    if( ((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseNs )).isChecked() )
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseNs = 1;

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorNoiseSuppress = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorNoiseSuppress)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }
                    }
                    else
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseNs = 0;
                    }

                    if( ((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseVad )).isChecked() )
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseVad = 1;

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorVadProbStart = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorVadProbStart)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorVadProbContinue = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorVadProbContinue)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }
                    }
                    else
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseVad = 0;
                    }

                    if( ((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseAgc )).isChecked() )
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseAgc = 1;

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorAgcLevel = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorAgcLevel)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }
                    }
                    else
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseAgc = 0;
                    }

                    if( ((CheckBox)clLayoutActivitySpeexPreprocessorView.findViewById( R.id.CheckBoxSpeexPreprocessorIsUseRec )).isChecked() )
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseRec = 1;

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorEchoSuppress = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorEchoSuppress)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }

                        try
                        {
                            clAudioProcessThread.iSpeexPreprocessorEchoSuppressActive = Integer.parseInt(((TextView) clLayoutActivitySpeexPreprocessorView.findViewById(R.id.SpeexPreprocessorEchoSuppressActive)).getText().toString());
                        }
                        catch (NumberFormatException e)
                        {
                            Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                            break out;
                        }
                    }
                    else
                    {
                        clAudioProcessThread.iSpeexPreprocessorIsUseRec = 0;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseSpeexPreprocessor = 0;
                }

                //判断是否使用PCM裸数据
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUsePCM)).isChecked() )
                {
                    clAudioProcessThread.iIsUsePCM = 1;
                }
                else
                {
                    clAudioProcessThread.iIsUsePCM = 0;
                }

                //判断是否使用Speex编解码器
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseSpeexCodec)).isChecked() )
                {
                    clAudioProcessThread.iIsUseSpeexCodec = 1;

                    if( ((CheckBox)clLayoutActivitySpeexCodecView.findViewById( R.id.CheckBoxSpeexCodecEncoderIsUseVbr )).isChecked() )
                    {
                        clAudioProcessThread.iSpeexCodecEncoderIsUseVbr = 1;
                    }
                    else
                    {
                        clAudioProcessThread.iSpeexCodecEncoderIsUseVbr = 0;
                    }

                    try
                    {
                        clAudioProcessThread.iSpeexCodecEncoderQuality = Integer.parseInt(((TextView) clLayoutActivitySpeexCodecView.findViewById(R.id.SpeexCodecEncoderQuality)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }

                    try
                    {
                        clAudioProcessThread.iSpeexCodecEncoderQuality = Integer.parseInt(((TextView) clLayoutActivitySpeexCodecView.findViewById(R.id.SpeexCodecEncoderQuality)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }

                    try
                    {
                        clAudioProcessThread.iSpeexCodecEncoderComplexity = Integer.parseInt(((TextView) clLayoutActivitySpeexCodecView.findViewById(R.id.SpeexCodecEncoderComplexity)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }

                    try
                    {
                        clAudioProcessThread.iSpeexCodecEncoderPlcExpectedLossRate = Integer.parseInt(((TextView) clLayoutActivitySpeexCodecView.findViewById(R.id.SpeexCodecEncoderPlcExpectedLossRate)).getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        Toast.makeText(this, "请输入数字", Toast.LENGTH_LONG).show();
                        break out;
                    }
                }
                else
                {
                    clAudioProcessThread.iIsUseSpeexCodec = 0;
                }

                //判断是否使用Opus编解码器
                if( ((RadioButton)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUsePCM)).isChecked())
                {
                    clAudioProcessThread.iIsUseOpusCodec = 1;
                }
                else
                {
                    clAudioProcessThread.iIsUseOpusCodec = 0;
                }

                //判断是否使用自适应抖动缓冲器
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsUseAjb)).isChecked())
                {
                    clAudioProcessThread.iIsUseAjb = 1;
                }
                else
                {
                    clAudioProcessThread.iIsUseAjb = 0;
                }

                //判断是否保存音频数据到文件
                if( ((CheckBox)clLayoutActivitySettingView.findViewById(R.id.CheckBoxIsSaveAudioDataFile)).isChecked())
                {
                    clAudioProcessThread.iIsSaveAudioDataFile = 1;
                }
                else
                {
                    clAudioProcessThread.iIsSaveAudioDataFile = 0;
                }

                clAudioProcessThread.start();

                Log.i( clCurrentClassNameString, "启动音频处理线程完毕" );

                if( v.getId() == R.id.CreateServerButton )
                {
                    ((Button) findViewById( R.id.CreateServerButton )).setText( "中断" ); //设置创建服务端按钮的内容为“中断”
                    ((Button) findViewById(R.id.ConnectServerButton )).setEnabled( false ); //设置连接服务端按钮为不可用
                    ((Button)findViewById( R.id.SettingButton )).setEnabled( false ); //设置设置按钮为不可用
                }
                else if( v.getId() == R.id.ConnectServerButton )
                {
                    ((Button)findViewById( R.id.CreateServerButton )).setEnabled( false ); //设置创建服务端按钮为不可用
                    ((Button)findViewById( R.id.ConnectServerButton )).setText( "中断" ); //设置连接服务端按钮的内容为“中断”
                    ((Button)findViewById( R.id.SettingButton )).setEnabled( false ); //设置设置按钮为不可用
                }
            }
            else
            {
                clAudioProcessThread.iExitFlag = 1;

                try
                {
                    Log.i( clCurrentClassNameString, "开始等待音频处理线程退出" );
                    clAudioProcessThread.join(); //等待音频处理线程退出
                    Log.i( clCurrentClassNameString, "结束等待音频处理线程退出" );
                }
                catch (InterruptedException e)
                {

                }
            }

            p_iResult = 0;
            break out;
        }

        if( p_iResult != 0 )
        {
            clAudioProcessThread = null;
        }
    }

    //主界面设置按钮
    public void OnClickSetting( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //设置界面的确定按钮
    public void OnClickSettingOk( View clButton )
    {
        setContentView( clLayoutActivityMainView );
        clLayoutActivityCurrentView = clLayoutActivityMainView;
    }

    //WebRtc声学回音消除器设置按钮
    public void OnWebRtcAecSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcAecView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcAecView;
    }

    //WebRtc声学回音消除器设置界面的确定按钮
    public void OnWebRtcAecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //WebRtc移动版声学回音消除器设置按钮
    public void OnWebRtcAecmSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcAecmView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcAecmView;
    }

    //WebRtc移动版声学回音消除器设置界面的确定按钮
    public void OnWebRtcAecmSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex声学回音消除器设置按钮
    public void OnSpeexAecSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexAecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexAecView;
    }

    //Speex声学回音消除器设置界面的确定按钮
    public void OnSpeexAecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //WebRtc定点噪音抑制器设置按钮
    public void OnWebRtcNsxSettingClick( View clButton )
    {
        setContentView( clLayoutActivityWebRtcNsxView );
        clLayoutActivityCurrentView = clLayoutActivityWebRtcNsxView;
    }

    //WebRtc定点噪音抑制器设置界面的确定按钮
    public void OnWebRtcNsxSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex预处理器设置按钮
    public void OnSpeexPreprocessorSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexPreprocessorView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexPreprocessorView;
    }

    //Speex预处理器设置界面的确定按钮
    public void OnSpeexPreprocessorSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //Speex编解码器设置按钮
    public void OnSpeexCodecSettingClick( View clButton )
    {
        setContentView( clLayoutActivitySpeexCodecView );
        clLayoutActivityCurrentView = clLayoutActivitySpeexCodecView;
    }

    //Speex编解码器设置界面的确定按钮
    public void OnSpeexCodecSettingOkClick( View clButton )
    {
        setContentView( clLayoutActivitySettingView );
        clLayoutActivityCurrentView = clLayoutActivitySettingView;
    }

    //主界面说明按钮
    public void OnClickReadMe( View clButton )
    {
        setContentView( clLayoutActivityReadMeView );
        clLayoutActivityCurrentView = clLayoutActivityReadMeView;
    }

    //说明界面的确定按钮
    public void OnClickReadMeOk( View clButton )
    {
        setContentView( clLayoutActivityMainView );
        clLayoutActivityCurrentView = clLayoutActivityMainView;
    }
}
