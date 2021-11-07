package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc定点版声学回音消除器类。
public class WebRtcAecm
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcAecmPt; //存放WebRtc定点版声学回音消除器的内存指针。

    //构造函数。
    public WebRtcAecm()
    {
        m_WebRtcAecmPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化WebRtc定点版声学回音消除器。
    public int Init( int SamplingRate, int FrameLen, int IsUseCNGMode, int EchoMode, int Delay, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcAecmPt == 0 )
        {
            HTLong p_WebRtcAecmPt = new HTLong();
            if( WebRtcAecmInit( p_WebRtcAecmPt, SamplingRate, FrameLen, IsUseCNGMode, EchoMode, Delay, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcAecmPt = p_WebRtcAecmPt.m_Val;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //设置WebRtc定点版声学回音消除器的回音延迟。
    public int SetDelay( int Delay )
    {
        return WebRtcAecmSetDelay( m_WebRtcAecmPt, Delay );
    }

    //获取WebRtc定点版声学回音消除器的回音延迟。
    public int GetDelay( HTInt DelayPt )
    {
        return WebRtcAecmGetDelay( m_WebRtcAecmPt, DelayPt );
    }

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc定点版声学回音消除。
    public int Proc( short InputFramePt[], short OutputFramePt[], short ResultFramePt[] )
    {
        return WebRtcAecmProc( m_WebRtcAecmPt, InputFramePt, OutputFramePt, ResultFramePt );
    }

    //销毁WebRtc定点版声学回音消除器。
    public int Destroy()
    {
        if( m_WebRtcAecmPt != 0 )
        {
            if( WebRtcAecmDestroy( m_WebRtcAecmPt ) == 0 )
            {
                m_WebRtcAecmPt = 0;
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return 0;
        }
    }

    //创建并初始化WebRtc定点版声学回音消除器。
    public native int WebRtcAecmInit( HTLong WebRtcAecmPt, int SamplingRate, int FrameLen, int IsUseCNGMode, int EchoMode, int Delay, long ErrInfoVarStrPt );

    //设置WebRtc定点版声学回音消除器的回音延迟。
    public native int WebRtcAecmSetDelay( long WebRtcAecmPt, int Delay );

    //获取WebRtc定点版声学回音消除器的回音延迟。
    public native int WebRtcAecmGetDelay( long WebRtcAecmPt, HTInt DelayPt );

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc定点版声学回音消除。
    public native int WebRtcAecmProc( long WebRtcAecmPt, short InputFramePt[], short OutputFramePt[], short ResultFramePt[] );

    //销毁WebRtc定点版声学回音消除器。
    public native int WebRtcAecmDestroy( long WebRtcAecmPt );
}