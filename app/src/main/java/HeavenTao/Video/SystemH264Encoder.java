package HeavenTao.Video;

import HeavenTao.Data.HTLong;
import HeavenTao.Data.VarStr;

//系统自带H264编码器类。
public class SystemH264Encoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SystemH264" ); //加载libSystemH264.so。
    }

    public long m_SystemH264EncoderPt; //存放系统自带H264编码器的指针。

    //构造函数。
    public SystemH264Encoder()
    {
        m_SystemH264EncoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化系统自带H264编码器。
    public int Init( int YU12FrameWidth, int YU12FrameHeight, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvlTimeSec, int Complexity, VarStr ErrInfoVarStrPt )
    {
        if( m_SystemH264EncoderPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( SystemH264EncoderInit( p_WebRtcNsPt, YU12FrameWidth, YU12FrameHeight, EncodedBitrate, BitrateControlMode, MaxFrameRate, IDRFrameIntvlTimeSec, Complexity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SystemH264EncoderPt = p_WebRtcNsPt.m_Val;
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
    public int Pocs( byte YU12FramePt[], long YU12FrameTimeStampMsec, byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt, long TimeOutMsec, VarStr ErrInfoVarStrPt )
    {
        return SystemH264EncoderPocs( m_SystemH264EncoderPt, YU12FramePt, YU12FrameTimeStampMsec, H264FramePt, H264FrameSz, H264FrameLenPt, TimeOutMsec, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁系统自带H264编码器。
    public int Dstoy( VarStr ErrInfoVarStrPt )
    {
        if( m_SystemH264EncoderPt != 0 )
        {
            if( SystemH264EncoderDstoy( m_SystemH264EncoderPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SystemH264EncoderPt = 0;
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
    public native int SystemH264EncoderInit( HTLong SystemH264EncoderPt, int YU12FrameWidth, int YU12FrameHeight, int EncodedBitrate, int BitrateControlMode, int MaxFrameRate, int IDRFrameIntvlTimeSec, int Cmplxt, long ErrInfoVarStrPt );

    //用系统自带H264编码器对8位无符号整型YU12格式帧进行H264格式编码。
    public native int SystemH264EncoderPocs( long SystemH264EncoderPt, byte YU12FramePt[], long YU12FrameTimeStampMsec, byte H264FramePt[], long H264FrameSz, HTLong H264FrameLenPt, long TimeOutMsec, long ErrInfoVarStrPt );

    //销毁系统自带H264编码器。
    public native int SystemH264EncoderDstoy( long SystemH264EncoderPt, long ErrInfoVarStrPt );
}