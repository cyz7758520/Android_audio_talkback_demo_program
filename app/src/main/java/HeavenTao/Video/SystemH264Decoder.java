package HeavenTao.Video;

import HeavenTao.Data.*;

//系统自带H264编码器类。
public class SystemH264Decoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "SystemH264" ); //加载libSystemH264.so。
    }

    public long m_SystemH264DecoderPt; //存放系统自带H264解码器的指针。

    //构造函数。
    public SystemH264Decoder()
    {
        m_SystemH264DecoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //创建并初始化系统自带H264解码器。
    public int Init( VarStr ErrInfoVarStrPt )
    {
        if( m_SystemH264DecoderPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( SystemH264DecoderInit( p_WebRtcNsPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SystemH264DecoderPt = p_WebRtcNsPt.m_Val;
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

    //用系统自带H264解码器对H264格式进行8位无符号整型YU12格式帧解码。
    public int Proc( byte H264FramePt[], long H264FrameLen, byte YU12FramePt[], long YU12FrameSz, HTInt YU12FrameWidth, HTInt YU12FrameHeight, long TimeOutMsec, VarStr ErrInfoVarStrPt )
    {
        return SystemH264DecoderProc( m_SystemH264DecoderPt, H264FramePt, H264FrameLen, YU12FramePt, YU12FrameSz, YU12FrameWidth, YU12FrameHeight, TimeOutMsec, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁系统自带H264解码器。
    public int Destroy( VarStr ErrInfoVarStrPt )
    {
        if( m_SystemH264DecoderPt != 0 )
        {
            if( SystemH264DecoderDestroy( m_SystemH264DecoderPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_SystemH264DecoderPt = 0;
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

    //创建并初始化系统自带H264解码器。
    public native int SystemH264DecoderInit( HTLong SystemH264DecoderPt, long ErrInfoVarStrPt );

    //用系统自带H264解码器对H264格式进行8位无符号整型YU12格式帧解码。
    public native int SystemH264DecoderProc( long SystemH264DecoderPt, byte H264FramePt[], long H264FrameLen, byte YU12FramePt[], long YU12FrameSz, HTInt YU12FrameWidth, HTInt YU12FrameHeight, long TimeOutMsec, long ErrInfoVarStrPt );

    //销毁系统自带H264解码器。
    public native int SystemH264DecoderDestroy( long SystemH264DecoderPt, long ErrInfoVarStrPt );
}