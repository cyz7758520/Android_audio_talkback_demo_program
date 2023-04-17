package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc定点版噪音抑制器。
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
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化WebRtc定点版噪音抑制器。
    public int Init( int SmplRate, long FrmLenUnit, int PolicyMode, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcNsxPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( WebRtcNsxInit( p_WebRtcNsPt, SmplRate, FrmLenUnit, PolicyMode, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //用WebRtc定点版噪音抑制器对单声道16位有符号整型Pcm格式帧进行WebRtc定点版噪音抑制。
    public int Pocs( short FrmPt[], short RsltFrmPt[] )
    {
        return WebRtcNsxPocs( m_WebRtcNsxPt, FrmPt, RsltFrmPt );
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
    private native int WebRtcNsxInit( HTLong WebRtcNsxPt, int SmplRate, long FrmLenUnit, int PolicyMode, long ErrInfoVstrPt );

    //用WebRtc定点版噪音抑制器对单声道16位有符号整型Pcm格式帧进行WebRtc定点版噪音抑制。
    private native int WebRtcNsxPocs( long WebRtcNsxPt, short FrmPt[], short RsltFrmPt[] );

    //销毁WebRtc定点版噪音抑制器。
    private native int WebRtcNsxDstoy( long WebRtcNsxPt );
}
