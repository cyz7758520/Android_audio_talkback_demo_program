package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc定点版噪音抑制器类。
public class WebRtcNsx
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcNsxPt; //WebRtc定点版噪音抑制器的指针。

    //构造函数。
    public WebRtcNsx()
    {
        m_WebRtcNsxPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy();
    }

    //创建并初始化WebRtc定点版噪音抑制器。
    public int Init( int SamplingRate, int FrameLen, int PolicyMode, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcNsxPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( WebRtcNsxInit( p_WebRtcNsPt, SamplingRate, FrameLen, PolicyMode, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcNsxPt = p_WebRtcNsPt.m_Val;
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

    //用WebRtc定点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc定点版噪音抑制。
    public int Pocs( short FramePt[], short ResultFramePt[] )
    {
        return WebRtcNsxPocs( m_WebRtcNsxPt, FramePt, ResultFramePt );
    }

    //销毁WebRtc定点版噪音抑制器。
    public int Dstoy()
    {
        if( m_WebRtcNsxPt != 0 )
        {
            if( WebRtcNsxDstoy( m_WebRtcNsxPt ) == 0 )
            {
                m_WebRtcNsxPt = 0;
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

    //创建并初始化WebRtc定点版噪音抑制器。
    public native int WebRtcNsxInit( HTLong WebRtcNsxPt, int SamplingRate, int FrameLen, int PolicyMode, long ErrInfoVarStrPt );

    //用WebRtc定点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc定点版噪音抑制。
    public native int WebRtcNsxPocs( long WebRtcNsxPt, short FramePt[], short ResultFramePt[] );

    //销毁WebRtc定点版噪音抑制器。
    public native int WebRtcNsxDstoy( long WebRtcNsxPt );
}
