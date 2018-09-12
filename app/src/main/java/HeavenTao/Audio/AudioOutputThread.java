package HeavenTao.Audio;

import android.media.AudioTrack;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;

//音频输出线程类
public class AudioOutputThread extends Thread
{
    String m_pclCurrentClassNameString = this.getClass().getName().substring( this.getClass().getName().lastIndexOf( '.' ) + 1 ); //当前类名称字符串

    int m_i32ExitFlag = 0; //本线程退出标记，0表示保持运行，1表示请求退出

    AudioProcessThread m_pclAudioProcessThread; //音频处理线程类对象的内存指针

    AudioTrack m_pclAudioTrack; //播放类

    int m_i32SamplingRate; //音频数据的采样频率
    int m_i32FrameSize; //音频数据帧的长度

    LinkedList<short []> m_pclAlreadyAudioOutputLinkedList; //已播放的链表

    //请求本线程退出
    public void RequireExit()
    {
        m_i32ExitFlag = 1;
    }

    //本线程主函数
    public void run()
    {
        this.setPriority( MAX_PRIORITY ); //设置本线程优先级
        android.os.Process.setThreadPriority( -19 ); //设置本线程优先级

        int p_i32AudioDataNumber;
        Date p_pclLastDate;
        Date clNowDate;

        p_pclLastDate = new Date();
        p_i32AudioDataNumber = 0;
        Log.i( m_pclCurrentClassNameString, "音频输出线程：准备开始播放。" );

        //开始循环播放
        out:
        while( true )
        {
            short p_pszi16TempAudioOutputData[] = new short[m_i32FrameSize]; //存放一个PCM格式音频输出数据帧

            //从音频处理线程获取一个PCM格式音频输出数据帧
            m_pclAudioProcessThread.WriteAudioOutputDataFrame( p_pszi16TempAudioOutputData );

            //开始播放这个PCM格式音频输出数据帧
            m_pclAudioTrack.write( p_pszi16TempAudioOutputData, 0, p_pszi16TempAudioOutputData.length );

            clNowDate = new Date();
            p_i32AudioDataNumber++;
            Log.i( m_pclCurrentClassNameString, "音频输出线程：" + "音频数据帧序号：" + p_i32AudioDataNumber + "，" + "写入耗时：" + (clNowDate.getTime() - p_pclLastDate.getTime()) + "，" + "已播放链表元素个数：" + m_pclAlreadyAudioOutputLinkedList.size() + "。" );
            p_pclLastDate = clNowDate;

            //追加一个PCM格式音频输出数据帧到已播放的链表
            synchronized( m_pclAlreadyAudioOutputLinkedList)
            {
                m_pclAlreadyAudioOutputLinkedList.addLast( p_pszi16TempAudioOutputData );
            }
            p_pszi16TempAudioOutputData = null;

            if( m_i32ExitFlag == 1 ) //如果本线程退出标记为请求退出
            {
                Log.i( m_pclCurrentClassNameString, "音频输出线程：本线程接收到退出请求，开始准备退出。" );
                break out;
            }
        }

        Log.i( m_pclCurrentClassNameString, "音频输出线程：本线程已退出。" );
    }
}