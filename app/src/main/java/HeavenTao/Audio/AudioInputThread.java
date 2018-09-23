package HeavenTao.Audio;

import android.media.AudioRecord;
import android.util.Log;
import android.os.Process;

import java.util.Date;
import java.util.LinkedList;

//音频输入线程类。
public class AudioInputThread extends Thread
{
    String m_pclCurrentClassNameString = this.getClass().getSimpleName(); //当前类名称字符串。

    int m_i32ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出。

    AudioRecord m_pclAudioRecord; //录音类。

    int m_i32SamplingRate; //音频数据的采样频率。
    int m_i32FrameSize; //音频数据帧的长度。

    LinkedList<short []> m_pclAlreadyAudioInputLinkedList; //已录音的链表。

    AudioOutputThread m_pclAudioOutputThread; //存放音频输出线程类对象的内存指针。

    WebRtcAecm m_pclWebRtcAecm; //WebRtc移动版声学回音消除器类对象。

    //请求本线程退出。
    public void RequireExit()
    {
        m_i32ExitFlag = 1;
    }

    //本线程主函数。
    public void run()
    {
        this.setPriority( this.MAX_PRIORITY ); //设置本线程优先级。
        Process.setThreadPriority( Process.THREAD_PRIORITY_URGENT_AUDIO ); //设置本线程优先级。

        short p_pszi16TempAudioInputData[];
        int p_i32AudioDataNumber;
        int p_i32Temp;
        Date p_pclLastDate;
        Date p_pclNowDate;

        p_i32AudioDataNumber = 0;
        p_pclLastDate = new Date();
        Log.i( m_pclCurrentClassNameString, "音频输入线程：开始录音准备。" );

        //跳过刚开始录音到的空的音频输入数据帧。
        while( true )
        {
            p_pszi16TempAudioInputData = new short[m_i32FrameSize];
            m_pclAudioRecord.read(p_pszi16TempAudioInputData, 0, p_pszi16TempAudioInputData.length);

            for( p_i32Temp = 0; p_i32Temp < p_pszi16TempAudioInputData.length; p_i32Temp++ )
            {
                if( p_pszi16TempAudioInputData[p_i32Temp] != 0 )
                    break;
            }
            if( p_i32Temp < p_pszi16TempAudioInputData.length )
            {
                break;
            }
        }

        p_pclNowDate = new Date();
        Log.i( m_pclCurrentClassNameString, "音频输入线程：" + "录音准备耗时：" + (p_pclNowDate.getTime() - p_pclLastDate.getTime()) + "，丢弃掉刚开始录音到的空数据，现在正式开始录音并启动音频输出线程，为了保证音频输入线程走在输出数据线程的前面。" );
        if( ( m_pclWebRtcAecm != null ) && ( m_pclWebRtcAecm.m_i32Delay == -1 ) ) //如果使用了WebRtc移动版声学回音消除器，且需要自适应设置WebRtc移动版声学回音消除器的回音延迟时间。
        {
            m_pclWebRtcAecm.m_i32Delay = (int)(( p_pclNowDate.getTime() - p_pclLastDate.getTime() ) / 3);
            Log.i( m_pclCurrentClassNameString, "音频输入线程：自适应设置WebRtc移动版声学回音消除器的回音延迟时间为 " + m_pclWebRtcAecm.m_i32Delay + " 毫秒。" );
        }
        p_pclLastDate = p_pclNowDate;

        m_pclAudioOutputThread.start(); //启动音频输出线程。

        //开始循环录音。
        out:
        while( true )
        {
            p_pszi16TempAudioInputData = new short[m_i32FrameSize];

            //开始读取这个PCM格式音频输入数据帧。
            m_pclAudioRecord.read( p_pszi16TempAudioInputData, 0, p_pszi16TempAudioInputData.length );

            p_pclNowDate = new Date();
            p_i32AudioDataNumber++;
            Log.i( m_pclCurrentClassNameString, "音频输入线程：" + "音频数据帧序号：" + p_i32AudioDataNumber + "，" + "读取耗时：" + (p_pclNowDate.getTime() - p_pclLastDate.getTime()) + "，" + "已录音链表元素个数：" + m_pclAlreadyAudioInputLinkedList.size() + "。" );
            p_pclLastDate = p_pclNowDate;

            //追加一个PCM格式音频输入数据帧到已录音的链表。
            synchronized( m_pclAlreadyAudioInputLinkedList )
            {
                m_pclAlreadyAudioInputLinkedList.addLast( p_pszi16TempAudioInputData );
            }
            p_pszi16TempAudioInputData = null;

            if( m_i32ExitFlag == 1 ) //如果本线程退出标记为请求退出。
            {
                Log.i( m_pclCurrentClassNameString, "音频输入线程：本线程接收到退出请求，开始准备退出。" );
                break out;
            }
        }

        Log.i( m_pclCurrentClassNameString, "音频输入线程：本线程已退出。" );
    }
}