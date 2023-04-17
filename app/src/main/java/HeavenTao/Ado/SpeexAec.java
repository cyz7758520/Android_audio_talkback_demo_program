package HeavenTao.Ado;

import HeavenTao.Data.*;

//Speex声学回音消除器。
public class SpeexAec
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SpeexDsp" ); //加载libSpeexDsp.so。
    }

    public long m_SpeexAecPt; //存放Speex声学回音消除器的指针。

    //构造函数。
    public SpeexAec()
    {
        m_SpeexAecPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化Speex声学回音消除器。
    public int Init( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, Vstr ErrInfoVstrPt )
    {
        if( m_SpeexAecPt == 0 )
        {
            HTLong p_SpeexAecPt = new HTLong();
            if( SpeexAecInit( p_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_SpeexAecPt = p_SpeexAecPt.m_Val;
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

    //根据Speex声学回音消除器内存块来创建并初始化Speex声学回音消除器。
    public int InitByMem( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, byte MemPt[], long MemLenByt, Vstr ErrInfoVstrPt )
    {
        if( m_SpeexAecPt == 0 )
        {
            HTLong p_SpeexAecPt = new HTLong();
            if( SpeexAecInitByMem( p_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, MemPt, MemLenByt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_SpeexAecPt = p_SpeexAecPt.m_Val;
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

    //根据Speex声学回音消除器内存块文件来创建并初始化Speex声学回音消除器。
    public int InitByMemFile( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, String MemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        if( m_SpeexAecPt == 0 )
        {
            HTLong p_SpeexAecPt = new HTLong();
            if( SpeexAecInitByMemFile( p_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, MemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_SpeexAecPt = p_SpeexAecPt.m_Val;
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

    //获取Speex声学回音消除器内存块的数据长度。
    public int GetMemLen( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, HTLong MemLenBytPt )
    {
        return SpeexAecGetMemLen( m_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, MemLenBytPt );
    }

    //获取Speex声学回音消除器的内存块。
    public int GetMem( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, byte MemPt[], long MemSzByt )
    {
        return SpeexAecGetMem( m_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, MemPt, MemSzByt );
    }

    //将Speex声学回音消除器内存块保存到指定的文件。
    public int SaveMemFile( int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, String MemFileFullPathStrPt, Vstr ErrInfoVstrPt )
    {
        return SpeexAecSaveMemFile( m_SpeexAecPt, SmplRate, FrmLenUnit, FilterLenMsec, IsUseRec, EchoMutp, EchoCntu, EchoSupes, EchoSupesAct, MemFileFullPathStrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用Speex声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行Speex声学回音消除。
    public int Pocs( short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] )
    {
        return SpeexAecPocs( m_SpeexAecPt, InptFrmPt, OtptFrmPt, RsltFrmPt );
    }

    //销毁Speex声学回音消除器。
    public int Dstoy()
    {
        if( m_SpeexAecPt != 0 )
        {
            if( SpeexAecDstoy( m_SpeexAecPt ) == 0 )
            {
                m_SpeexAecPt = 0;
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

    //创建并初始化Speex声学回音消除器。
    private native int SpeexAecInit( HTLong SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, long ErrInfoVstrPt );

    //根据Speex声学回音消除器内存块来创建并初始化Speex声学回音消除器。
    private native int SpeexAecInitByMem( HTLong SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, byte MemPt[], long MemLenByt, long ErrInfoVstrPt );

    //根据Speex声学回音消除器内存块文件来创建并初始化Speex声学回音消除器。
    private native int SpeexAecInitByMemFile( HTLong SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, String MemFileFullPathStrPt, long ErrInfoVstrPt );

    //获取Speex声学回音消除器内存块的数据长度。
    private native int SpeexAecGetMemLen( long SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, HTLong MemLenBytPt );

    //获取Speex声学回音消除器的内存块。
    private native int SpeexAecGetMem( long SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, byte MemPt[], long MemSzByt );

    //将Speex声学回音消除器内存块保存到指定的文件。
    private native int SpeexAecSaveMemFile( long SpeexAecPt, int SmplRate, long FrmLenUnit, int FilterLenMsec, int IsUseRec, float EchoMutp, float EchoCntu, int EchoSupes, int EchoSupesAct, String MemFileFullPathStrPt, long ErrInfoVstrPt );

    //用Speex声学回音消除器对单声道16位有符号整型Pcm格式输入帧进行Speex声学回音消除。
    private native int SpeexAecPocs( long SpeexAecPt, short InptFrmPt[], short OtptFrmPt[], short RsltFrmPt[] );

    //销毁Speex声学回音消除器。
    private native int SpeexAecDstoy( long SpeexAecPt );
}