package HeavenTao.Audio;

import HeavenTao.Data.*;

//WebRtc浮点版噪音抑制器类。
public class WebRtcNs
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcNsPt; //WebRtc浮点版噪音抑制器的指针。

    //构造函数。
    public WebRtcNs()
    {
        m_WebRtcNsPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //创建并初始化WebRtc浮点版噪音抑制器。
    public int Init( int SamplingRate, int FrameLen, int PolicyMode, VarStr ErrInfoVarStrPt )
    {
        if( m_WebRtcNsPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( WebRtcNsInit( p_WebRtcNsPt, SamplingRate, FrameLen, PolicyMode, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_WebRtcNsPt = p_WebRtcNsPt.m_Val;
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

    //用WebRtc浮点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc浮点版噪音抑制。
    public int Proc( short FrameObj[], short ResultFrameObj[] )
    {
        return WebRtcNsProc( m_WebRtcNsPt, FrameObj, ResultFrameObj );
    }

    //销毁WebRtc浮点版噪音抑制器。
    public int Destroy()
    {
        if( m_WebRtcNsPt != 0 )
        {
            if( WebRtcNsDestroy( m_WebRtcNsPt ) == 0 )
            {
                m_WebRtcNsPt = 0;
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

    //创建并初始化WebRtc浮点版噪音抑制器。
    public native int WebRtcNsInit( HTLong WebRtcNsPt, int SamplingRate, int FrameLen, int PolicyMode, long ErrInfoVarStrPt );

    //用WebRtc浮点版噪音抑制器对单声道16位有符号整型PCM格式帧进行WebRtc浮点版噪音抑制。
    public native int WebRtcNsProc( long WebRtcNsPt, short FrameObj[], short ResultFrameObj[] );

    //销毁WebRtc浮点版噪音抑制器。
    public native int WebRtcNsDestroy( long WebRtcNsPt );
}
