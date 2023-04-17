package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc定点版声学回音消除器。
public class WebRtcAecm
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcAecmPt; //存放WebRtc定点版声学回音消除器的指针。

    //构造函数。
    public WebRtcAecm()
    {
        m_WebRtcAecmPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化WebRtc定点版声学回音消除器。
    public int Init( int SmplRate, long FrmLenUnit, int IsUseCNGMode, int EchoMode, int Delay, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecmPt == 0 )
        {
            HTLong p_WebRtcAecmPt = new HTLong();
            if( WebRtcAecmInit( p_WebRtcAecmPt, SmplRate, FrmLenUnit, IsUseCNGMode, EchoMode, Delay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc定点版声学回音消除。
    public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
    {
        return WebRtcAecmPocs( m_WebRtcAecmPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
    }

    //销毁WebRtc定点版声学回音消除器。
    public int Dstoy()
    {
        if( m_WebRtcAecmPt != 0 )
        {
            if( WebRtcAecmDstoy( m_WebRtcAecmPt ) == 0 )
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
    private native int WebRtcAecmInit( HTLong WebRtcAecmPt, int SmplRate, long FrmLenUnit, int IsUseCNGMode, int EchoMode, int Delay, long ErrInfoVstrPt );

    //设置WebRtc定点版声学回音消除器的回音延迟。
    private native int WebRtcAecmSetDelay( long WebRtcAecmPt, int Delay );

    //获取WebRtc定点版声学回音消除器的回音延迟。
    private native int WebRtcAecmGetDelay( long WebRtcAecmPt, HTInt DelayPt );

    //用WebRtc定点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc定点版声学回音消除。
    private native int WebRtcAecmPocs( long WebRtcAecmPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

    //销毁WebRtc定点版声学回音消除器。
    private native int WebRtcAecmDstoy( long WebRtcAecmPt );
}