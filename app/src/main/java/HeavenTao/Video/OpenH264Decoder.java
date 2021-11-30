package HeavenTao.Video;

import HeavenTao.Data.*;

//OpenH264解码器类。
public class OpenH264Decoder
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "OpenH264" ); //加载libOpenH264.so。
    }

    public long m_OpenH264DecoderPt; //存放OpenH264解码器的指针。

    //构造函数。
    public OpenH264Decoder()
    {
        m_OpenH264DecoderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化OpenH264解码器。
    public int Init( int DecodeThrdNum, VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264DecoderPt == 0 )
        {
            HTLong p_OpenH264DecoderPt = new HTLong();
            if( OpenH264DecoderInit( p_OpenH264DecoderPt, DecodeThrdNum, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_OpenH264DecoderPt = p_OpenH264DecoderPt.m_Val;
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

    //用OpenH264解码器对H264格式进行8位无符号整型YU12格式帧解码。
    public int Pocs( byte H264FramePt[], long H264FrameLen,
                     byte YU12FramePt[], long YU12FrameSz, HTInt YU12FrameWidth, HTInt YU12FrameHeight,
                     VarStr ErrInfoVarStrPt )
    {
        return OpenH264DecoderPocs( m_OpenH264DecoderPt,
                                    H264FramePt, H264FrameLen,
                                    YU12FramePt, YU12FrameSz, YU12FrameWidth, YU12FrameHeight,
                                    ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁OpenH264解码器。
    public int Dstoy( VarStr ErrInfoVarStrPt )
    {
        if( m_OpenH264DecoderPt != 0 )
        {
            if( OpenH264DecoderDstoy( m_OpenH264DecoderPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_OpenH264DecoderPt = 0;
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

    //创建并初始化OpenH264解码器。
    public native int OpenH264DecoderInit( HTLong OpenH264DecoderPt, int DecodeThrdNum, long ErrInfoVarStrPt );

    //用OpenH264解码器对H264格式进行8位无符号整型YU12格式帧解码。
    public native int OpenH264DecoderPocs( long OpenH264DecoderPt,
                                           byte H264FramePt[], long H264FrameLen,
                                           byte YU12FramePt[], long YU12FrameSz, HTInt YU12FrameWidth, HTInt YU12FrameHeight,
                                           long ErrInfoVarStrPt );

    //销毁OpenH264解码器。
    public native int OpenH264DecoderDstoy( long OpenH264DecoderPt, long ErrInfoVarStrPt );
}