package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器。
public class WebRtcAec
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "WebRtc" ); //加载libWebRtc.so。
    }

    public long m_WebRtcAecPt; //存放WebRtc浮点版声学回音消除器的指针。

    //构造函数。
    public WebRtcAec()
    {
        m_WebRtcAecPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化WebRtc浮点版声学回音消除器。
    public int Init( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInit( p_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public int InitByMem( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte MemPt[], long MemLenByt, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMem( p_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, MemPt, MemLenByt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    public int InitByMemFile( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String MemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMemFile( p_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, MemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_WebRtcAecPt = p_WebRtcAecPt.m_Val;
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

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public int GetMemLen( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong MemLenBytPt )
    {
        return WebRtcAecGetMemLen( m_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, MemLenBytPt );
    }

    //获取WebRtc浮点版声学回音消除器的内存块。
    public int GetMem( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte MemPt[], long MemSzByt )
    {
        return WebRtcAecGetMem( m_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, MemPt, MemSzByt );
    }

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public int SaveMemFile( int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String MemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        return WebRtcAecSaveMemFile( m_WebRtcAecPt, SmplRate, FrmLenUnit, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, MemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public int SetDelay( int Delay )
    {
        return WebRtcAecSetDelay( m_WebRtcAecPt, Delay );
    }

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public int GetDelay( HTInt DelayPt )
    {
        return WebRtcAecGetDelay( m_WebRtcAecPt, DelayPt );
    }

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc浮点版声学回音消除。
    public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
    {
        return WebRtcAecPocs( m_WebRtcAecPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
    }

    //销毁WebRtc浮点版声学回音消除器。
    public int Dstoy()
    {
        if( m_WebRtcAecPt != 0 )
        {
            if( WebRtcAecDstoy( m_WebRtcAecPt ) == 0 )
            {
                m_WebRtcAecPt = 0;
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

    //创建并初始化WebRtc浮点版声学回音消除器。
    private native int WebRtcAecInit( HTLong WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, long ErrInfoVstrPt );

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    private native int WebRtcAecInitByMem( HTLong WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte MemPt[], long MemLenByt, long ErrInfoVstrPt );

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    private native int WebRtcAecInitByMemFile( HTLong WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String MemFileFullPathStrPt, long ErrInfoVstrPt );

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    private native int WebRtcAecGetMemLen( long WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong MemLenBytPt );

    //获取WebRtc浮点版声学回音消除器的内存块。
    private native int WebRtcAecGetMem( long WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte MemPt[], long MemSzByt );

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    private native int WebRtcAecSaveMemFile( long WebRtcAecPt, int SmplRate, long FrmLenUnit, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String MemFileFullPathStrPt, long ErrInfoVstrPt );

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    private native int WebRtcAecSetDelay( long WebRtcAecPt, int Delay );

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    private native int WebRtcAecGetDelay( long WebRtcAecPt, HTInt DelayPt );

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行WebRtc浮点版声学回音消除。
    private native int WebRtcAecPocs( long WebRtcAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

    //销毁WebRtc浮点版声学回音消除器。
    private native int WebRtcAecDstoy( long WebRtcAecPt );
}