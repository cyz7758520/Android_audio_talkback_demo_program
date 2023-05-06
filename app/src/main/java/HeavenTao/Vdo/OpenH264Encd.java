package HeavenTao.Vdo;

import HeavenTao.Data.*;

//OpenH264编码器。
public class OpenH264Encd
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
    }

    public long m_OpenH264EncdPt; //存放OpenH264编码器的指针。

    //构造函数。
    public OpenH264Encd()
    {
        m_OpenH264EncdPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化OpenH264编码器。
    public int Init( int EncdPictrWidth, int EncdPictrHeight, int VdoType, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlFrmCnt, int Complexity, Vstr ErrInfoVstrPt )
    {
        if( m_OpenH264EncdPt == 0 )
        {
            HTLong p_OpenH264EncdPt = new HTLong();
            if( OpenH264EncdInit( p_OpenH264EncdPt, EncdPictrWidth, EncdPictrHeight, VdoType, EncdBitrate, BitrateCtrlMode, MaxFrmRate, IDRFrmIntvlFrmCnt, Complexity, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_OpenH264EncdPt = p_OpenH264EncdPt.m_Val;
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

    //设置OpenH264编码器的编码后比特率。
    public int SetEncdBitrate( int EncdBitrate, Vstr ErrInfoVstrPt )
    {
        return OpenH264EncdSetEncdBitrate( m_OpenH264EncdPt, EncdBitrate, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取OpenH264编码器的编码后比特率。
    public int GetEncdBitrate( HTInt EncdBitratePt, Vstr ErrInfoVstrPt )
    {
        return OpenH264EncdGetEncdBitrate( m_OpenH264EncdPt, EncdBitratePt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用OpenH264编码器对8位无符号整型Yu12格式帧进行H264格式编码。
    public int Pocs( byte Yu12FrmPt[], int Yu12FrmWidth, int Yu12FrmHeight, long Yu12FrmTimeStampMsec,
                     byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt,
                     Vstr ErrInfoVstrPt )
    {
        return OpenH264EncdPocs( m_OpenH264EncdPt,
                                    Yu12FrmPt, Yu12FrmWidth, Yu12FrmHeight, Yu12FrmTimeStampMsec,
                                    H264FrmPt, H264FrmSz, H264FrmLenPt,
                                    ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //销毁OpenH264编码器。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_OpenH264EncdPt != 0 )
        {
            if( OpenH264EncdDstoy( m_OpenH264EncdPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_OpenH264EncdPt = 0;
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

    //创建并初始化OpenH264编码器。
    private native int OpenH264EncdInit( HTLong OpenH264EncdPt, int EncdPictrWidth, int EncdPictrHeight, int VdoType, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlFrmCnt, int Cmplxt, long ErrInfoVstrPt );

    //设置OpenH264编码器的编码后比特率。
    private native int OpenH264EncdSetEncdBitrate( long OpenH264EncdPt, int EncdBitrate, long ErrInfoVstrPt );

    //获取OpenH264编码器的编码后比特率。
    private native int OpenH264EncdGetEncdBitrate( long OpenH264EncdPt, HTInt EncdBitratePt, long ErrInfoVstrPt );

    //用OpenH264编码器对8位无符号整型Yu12格式帧进行H264格式编码。
    private native int OpenH264EncdPocs( long OpenH264EncdPt,
                                         byte Yu12FrmPt[], int Yu12FrmWidth, int Yu12FrmHeight, long Yu12FrmTimeStampMsec,
                                         byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt,
                                         long ErrInfoVstrPt );

    //销毁OpenH264编码器。
    private native int OpenH264EncdDstoy( long OpenH264EncdPt, long ErrInfoVstrPt );
}