package HeavenTao.Ado;

import HeavenTao.Data.*;

//WebRtc浮点版声学回音消除器类。
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
    public int Init( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInit( p_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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
    public int InitByMem( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemLen, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMem( p_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemPt, WebRtcAecMemLen, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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
    public int InitByMemFile( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        if( m_WebRtcAecPt == 0 )
        {
            HTLong p_WebRtcAecPt = new HTLong();
            if( WebRtcAecInitByMemFile( p_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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
    public int GetMemLen( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong WebrtcAecMemLenPt )
    {
        return WebRtcAecGetMemLen( m_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebrtcAecMemLenPt );
    }

    //获取WebRtc浮点版声学回音消除器的内存块。
    public int GetMem( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemSz )
    {
        return WebRtcAecGetMem( m_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemPt, WebRtcAecMemSz );
    }

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public int SaveMemFile( int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        return WebRtcAecSaveMemFile( m_WebRtcAecPt, SmplRate, FrmLen, EchoMode, Delay, IsUseDelayAgstcMode, IsUseExtdFilterMode, IsUseRefinedFilterAdaptAecMode, IsUseAdaptAdjDelay, WebRtcAecMemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
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

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
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
    public native int WebRtcAecInit( HTLong WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, long ErrInfoVstrPt );

    //根据WebRtc浮点版声学回音消除器内存块来创建并初始化WebRtc浮点版声学回音消除器。
    public native int WebRtcAecInitByMem( HTLong WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemLen, long ErrInfoVstrPt );

    //根据WebRtc浮点版声学回音消除器内存块文件来创建并初始化WebRtc浮点版声学回音消除器。
    public native int WebRtcAecInitByMemFile( HTLong WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, long ErrInfoVstrPt );

    //获取WebRtc浮点版声学回音消除器内存块的数据长度。
    public native int WebRtcAecGetMemLen( long WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, HTLong WebrtcAecMemLenPt );

    //获取WebRtc浮点版声学回音消除器的内存块。
    public native int WebRtcAecGetMem( long WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, byte WebRtcAecMemPt[], long WebRtcAecMemSz );

    //将WebRtc浮点版声学回音消除器内存块保存到指定的文件。
    public native int WebRtcAecSaveMemFile( long WebRtcAecPt, int SmplRate, int FrmLen, int EchoMode, int Delay, int IsUseDelayAgstcMode, int IsUseExtdFilterMode, int IsUseRefinedFilterAdaptAecMode, int IsUseAdaptAdjDelay, String WebRtcAecMemFileFullPathStrPt, long ErrInfoVstrPt );

    //设置WebRtc浮点版声学回音消除器的回音延迟。
    public native int WebRtcAecSetDelay( long WebRtcAecPt, int Delay );

    //获取WebRtc浮点版声学回音消除器的回音延迟。
    public native int WebRtcAecGetDelay( long WebRtcAecPt, HTInt DelayPt );

    //用WebRtc浮点版声学回音消除器对单声道16位有符号整型PCM格式输入帧进行WebRtc浮点版声学回音消除。
    public native int WebRtcAecPocs( long WebRtcAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

    //销毁WebRtc浮点版声学回音消除器。
    public native int WebRtcAecDstoy( long WebRtcAecPt );
}