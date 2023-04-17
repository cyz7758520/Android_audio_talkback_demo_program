package HeavenTao.Ado;

import HeavenTao.Data.*;

//RNNoise噪音抑制器。
public class RNNoise
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
        System.loadLibrary( "RNNoise" ); //加载libRNNoise.so。
    }

    public long m_RNNoisePt; //RNNoise噪音抑制器的指针。

    //构造函数。
    public RNNoise()
    {
        m_RNNoisePt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化RNNoise噪音抑制器。
    public int Init( int SmplRate, long FrmLenUnit, Vstr ErrInfoVstrPt )
    {
        if( m_RNNoisePt == 0 )
        {
            HTLong p_RNNoisePt = new HTLong();
            if( RNNoiseInit( p_RNNoisePt, SmplRate, FrmLenUnit, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_RNNoisePt = p_RNNoisePt.m_Val;
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

    //用RNNoise噪音抑制器对单声道16位有符号整型Pcm格式帧进行RNNoise噪音抑制。
    public int Pocs( short FrmPt[], short RsltFrmPt[] )
    {
        return RNNoisePocs( m_RNNoisePt, FrmPt, RsltFrmPt );
    }

    //销毁RNNoise噪音抑制器。
    public int Dstoy()
    {
        if( m_RNNoisePt != 0 )
        {
            if( RNNoiseDstoy( m_RNNoisePt ) == 0 )
            {
                m_RNNoisePt = 0;
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

    //创建并初始化RNNoise噪音抑制器。
    private native int RNNoiseInit( HTLong RNNoisePt, int SmplRate, long FrmLenUnit, long ErrInfoVstrPt );

    //用RNNoise噪音抑制器对单声道16位有符号整型Pcm格式帧进行RNNoise噪音抑制。
    private native int RNNoisePocs( long RNNoisePt, short FrmPt[], short RsltFrmPt[] );

    //销毁RNNoise噪音抑制器。
    private native int RNNoiseDstoy( long RNNoisePt );
}
