package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex解码器。
public class SpeexDecd
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    public long m_SpeexDecdPt; //存放Speex解码器的指针。

    //构造函数。
    public SpeexDecd()
    {
        m_SpeexDecdPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化Speex解码器。
    public int Init( int SmplRate, int IsUsePrcplEnhsmt )
    {
        if( m_SpeexDecdPt == 0 )
        {
            HTLong p_SpeexDecdPt = new HTLong();
            if( SpeexDecdInit( p_SpeexDecdPt, SmplRate, IsUsePrcplEnhsmt ) == 0 )
            {
                m_SpeexDecdPt = p_SpeexDecdPt.m_Val;
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

    //用Speex解码器对单声道16位有符号整型20毫秒Pcm格式帧进行Speex格式编码。
    public int Pocs( byte SpeexFrmPt[], long SpeexFrmLen, short PcmFrmPt[] )
    {
        return SpeexDecdPocs( m_SpeexDecdPt, SpeexFrmPt, SpeexFrmLen, PcmFrmPt );
    }

    //销毁Speex解码器。
    public int Dstoy()
    {
        if( m_SpeexDecdPt != 0 )
        {
            if( SpeexDecdDstoy( m_SpeexDecdPt ) == 0 )
            {
                m_SpeexDecdPt = 0;
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

    //创建并初始化Speex解码器。
    private native int SpeexDecdInit( HTLong SpeexDecdPt, int SmplRate, int IsUsePrcplEnhsmt );

    //用Speex解码器对单声道16位有符号整型20毫秒Speex格式帧进行Pcm格式解码。
    private native int SpeexDecdPocs( long SpeexDecdPt, byte SpeexFrmPt[], long SpeexFrmLen, short PcmFrmPt[] );

    //销毁Speex解码器。
    private native int SpeexDecdDstoy( long SpeexDecdPt );
}