package HeavenTao.Vdo;

import HeavenTao.Data.HTLong;
import HeavenTao.Data.Vstr;

//系统自带H264编码器类。
public class SystemH264Encd
{
    static
    {
        if( android.os.Build.VERSION.SDK_INT >= 21 )
        {
            System.loadLibrary( "Func" ); //加载libFunc.so。
            System.loadLibrary( "SystemH264" ); //加载libSystemH264.so。
        }
    }

    public long m_SystemH264EncdPt; //存放系统自带H264编码器的指针。

    //构造函数。
    public SystemH264Encd()
    {
        m_SystemH264EncdPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化系统自带H264编码器。
    public int Init( int YU12FrmWidth, int YU12FrmHeight, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlTimeSec, int Complexity, Vstr ErrInfoVstrPt )
    {
        if( m_SystemH264EncdPt == 0 )
        {
            if( android.os.Build.VERSION.SDK_INT < 21 )
            {
                ErrInfoVstrPt.Cpy( "当前系统不自带H264编码器。" );
                return -1;
            }

            HTLong p_WebRtcNsPt = new HTLong();
            if( SystemH264EncdInit( p_WebRtcNsPt, YU12FrmWidth, YU12FrmHeight, EncdBitrate, BitrateCtrlMode, MaxFrmRate, IDRFrmIntvlTimeSec, Complexity, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_SystemH264EncdPt = p_WebRtcNsPt.m_Val;
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

    //用系统自带H264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public int Pocs( byte YU12FrmPt[], long YU12FrmTimeStampMsec, byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt, long TimeOutMsec, Vstr ErrInfoVstrPt )
    {
        return SystemH264EncdPocs( m_SystemH264EncdPt, YU12FrmPt, YU12FrmTimeStampMsec, H264FrmPt, H264FrmSz, H264FrmLenPt, TimeOutMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //销毁系统自带H264编码器。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_SystemH264EncdPt != 0 )
        {
            if( SystemH264EncdDstoy( m_SystemH264EncdPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_SystemH264EncdPt = 0;
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

    //创建并初始化系统自带H264编码器。
    public native int SystemH264EncdInit( HTLong SystemH264EncdPt, int YU12FrmWidth, int YU12FrmHeight, int EncdBitrate, int BitrateCtrlMode, int MaxFrmRate, int IDRFrmIntvlTimeSec, int Cmplxt, long ErrInfoVstrPt );

    //用系统自带H264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int SystemH264EncdPocs( long SystemH264EncdPt, byte YU12FrmPt[], long YU12FrmTimeStampMsec, byte H264FrmPt[], long H264FrmSz, HTLong H264FrmLenPt, long TimeOutMsec, long ErrInfoVstrPt );

    //销毁系统自带H264编码器。
    public native int SystemH264EncdDstoy( long SystemH264EncdPt, long ErrInfoVstrPt );
}