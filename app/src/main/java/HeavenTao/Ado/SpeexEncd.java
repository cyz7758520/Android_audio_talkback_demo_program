package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex编码器。
public class SpeexEncd
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Speex" ); //加载libSpeex.so。
    }

    public long m_SpeexEncdPt; //存放Speex编码器的指针。

    //构造函数。
    public SpeexEncd()
    {
        m_SpeexEncdPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化Speex编码器。
    public int Init( int SmplRate, int UseCbrOrVbr, int Quality, int Complexity, int PlcExptLossRate )
    {
        if( m_SpeexEncdPt == 0 )
        {
            HTLong p_SpeexEncdPt = new HTLong();
            if( SpeexEncdInit( p_SpeexEncdPt, SmplRate, UseCbrOrVbr, Quality, Complexity, PlcExptLossRate ) == 0 )
            {
                m_SpeexEncdPt = p_SpeexEncdPt.m_Val;
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

    //用Speex编码器对单声道16位有符号整型20毫秒Pcm格式帧进行Speex格式编码。
    public int Pocs( short PcmFrmPt[], byte SpeexFrmPt[], long SpeexFrmSz, HTLong SpeexFrmLenPt, HTInt IsNeedTransPt )
    {
        return SpeexEncdPocs( m_SpeexEncdPt, PcmFrmPt, SpeexFrmPt, SpeexFrmSz, SpeexFrmLenPt, IsNeedTransPt );
    }

    //销毁Speex编码器。
    public int Dstoy()
    {
        if( m_SpeexEncdPt != 0 )
        {
            if( SpeexEncdDstoy( m_SpeexEncdPt ) == 0 )
            {
                m_SpeexEncdPt = 0;
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

    //创建并初始化Speex编码器。
    private native int SpeexEncdInit( HTLong SpeexEncdPt, int SmplRate, int UseCbrOrVbr, int Qualt, int Cmplxt, int PlcExptLossRate );

    //用Speex编码器对单声道16位有符号整型20毫秒Pcm格式帧进行Speex格式编码。
    private native int SpeexEncdPocs( long SpeexEncdPt, short PcmFrmPt[], byte SpeexFrmPt[], long SpeexFrmSz, HTLong SpeexFrmLenPt, HTInt IsNeedTransPt );

    //销毁Speex编码器。
    private native int SpeexEncdDstoy( long SpeexEncdPt );
}