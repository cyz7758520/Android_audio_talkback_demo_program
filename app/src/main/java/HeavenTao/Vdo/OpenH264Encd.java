package HeavenTao.Vdo;

import HeavenTao.Data.*;

//OpenH264编码器类。
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
    public void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化OpenH264编码器。
    public int Init( int EncdPictrWidth, int EncdPictrHeight, int VdoType, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlFrmCnt, int Complexity, VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264EncdPt == 0 )
        {
            HTLong p_OpenH264EncdPt = new HTLong();
            if( OpenH264EncdInit( p_OpenH264EncdPt, EncdPictrWidth, EncdPictrHeight, VdoType, EncdBitrate, BitrateCtrlMode, MaxFrmRate, IDRFrmIntvlFrmCnt, Complexity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public int SetEncdBitrate( int EncdBitrate, VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncdSetEncdBitrate( m_OpenH264EncdPt, EncdBitrate, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取OpenH264编码器的编码后比特率。
    public int GetEncdBitrate( HTInt EncdBitratePt, VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncdGetEncdBitrate( m_OpenH264EncdPt, EncdBitratePt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //用OpenH264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public int Pocs( byte YU12FrmPt[], int YU12FrmWidth, int YU12FrmHeight, long YU12FrmTimeStampMsec,
                     byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt,
                     VarStr ErrInfoVarStrPt )
    {
        return OpenH264EncdPocs( m_OpenH264EncdPt,
                                    YU12FrmPt, YU12FrmWidth, YU12FrmHeight, YU12FrmTimeStampMsec,
                                    H264FrmPt, H264FrmSz, H264FrmLenPt,
                                    ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁OpenH264编码器。
    public int Dstoy( VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264EncdPt != 0 )
        {
            if( OpenH264EncdDstoy( m_OpenH264EncdPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public native int OpenH264EncdInit( HTLong OpenH264EncdPt, int EncdPictrWidth, int EncdPictrHeight, int VdoType, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlFrmCnt, int Cmplxt, long ErrInfoVarStrPt );

    //设置OpenH264编码器的编码后比特率。
    public native int OpenH264EncdSetEncdBitrate( long OpenH264EncdPt, int EncdBitrate, long ErrInfoVarStrPt );

    //获取OpenH264编码器的编码后比特率。
    public native int OpenH264EncdGetEncdBitrate( long OpenH264EncdPt, HTInt EncdBitratePt, long ErrInfoVarStrPt );

    //用OpenH264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int OpenH264EncdPocs( long OpenH264EncdPt,
                                           byte YU12FrmPt[], int YU12FrmWidth, int YU12FrmHeight, long YU12FrmTimeStampMsec,
                                           byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt,
                                           long ErrInfoVarStrPt );

    //销毁OpenH264编码器。
    public native int OpenH264EncdDstoy( long OpenH264EncdPt, long ErrInfoVarStrPt );
}