package HeavenTao.Audio;

import HeavenTao.Data.*;

//音频自适应抖动缓冲器类。
public class AAjb
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    public long m_AAjbPt; //存放音频自适应抖动缓冲器的内存指针。

    //构造函数。
    public AAjb()
    {
        m_AAjbPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( null );
    }

    //创建并初始化音频自适应抖动缓冲器。
    public int Init( int SamplingRate, int FrameLen, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, VarStr ErrInfoVarStrPt )
    {
        if( m_AAjbPt == 0 )
        {
            HTLong p_AAjbPt = new HTLong();
            if( AAjbInit( p_AAjbPt, SamplingRate, FrameLen, IsHaveTimeStamp, TimeStampStep, InactIsContPut, MinNeedBufFrameCnt, MaxNeedBufFrameCnt, AdaptSensitivity, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AAjbPt = p_AAjbPt.m_Val;
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

    //放入一个字节型帧到音频自适应抖动缓冲器。
    public int PutOneByteFrame( int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return AAjbPutOneByteFrame( m_AAjbPt, TimeStamp, ByteFramePt, FrameStart, FrameLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //放入一个短整型帧到音频自适应抖动缓冲器。
    public int PutOneShortFrame( int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return AAjbPutOneShortFrame( m_AAjbPt, TimeStamp, ShortFramePt, FrameStart, FrameLen, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从音频自适应抖动缓冲器取出一个字节型帧。
    public int GetOneByteFrame( HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return AAjbGetOneByteFrame( m_AAjbPt, TimeStampPt, ByteFramePt, FrameStart, FrameSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //从音频自适应抖动缓冲器取出一个短整型帧。
    public int GetOneShortFrame( HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return AAjbGetOneShortFrame( m_AAjbPt, TimeStampPt, ShortFramePt, FrameStart, FrameSz, FrameLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取缓冲帧的数量。
    public int GetBufFrameCnt( HTInt CurHaveBufActFrameCntPt, HTInt CurHaveBufInactFrameCntPt, HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return AAjbGetBufFrameCnt( m_AAjbPt, CurHaveBufActFrameCntPt, CurHaveBufInactFrameCntPt, CurHaveBufFrameCntPt, MinNeedBufFrameCntPt, MaxNeedBufFrameCntPt, CurNeedBufFrameCntPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //清空音频自适应抖动缓冲器。
    public int Clear( int IsAutoLockUnlock, VarStr ErrInfoVarStrPt)
    {
        return AAjbClear( m_AAjbPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁音频自适应抖动缓冲器。
    public int Destroy( VarStr ErrInfoVarStrPt)
    {
        if( m_AAjbPt != 0 )
        {
            if( AAjbDestroy( m_AAjbPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AAjbPt = 0;
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

    //创建并初始化音频自适应抖动缓冲器。
    public native int AAjbInit( HTLong AAjbPt, int SamplingRate, int FrameLen, int IsHaveTimeStamp, int TimeStampStep, int InactIsContPut, int MinNeedBufFrameCnt, int MaxNeedBufFrameCnt, float AdaptSensitivity, long ErrInfoVarStrPt );

    //放入一个字节型帧到音频自适应抖动缓冲器。
    public native int AAjbPutOneByteFrame( long AAjbPt, int TimeStamp, byte ByteFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //放入一个短整型帧到音频自适应抖动缓冲器。
    public native int AAjbPutOneShortFrame( long AAjbPt, int TimeStamp, short ShortFramePt[], long FrameStart, long FrameLen, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从音频自适应抖动缓冲器取出一个字节型帧。
    public native int AAjbGetOneByteFrame( long AAjbPt, HTInt TimeStampPt, byte ByteFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //从音频自适应抖动缓冲器取出一个短整型帧。
    public native int AAjbGetOneShortFrame( long AAjbPt, HTInt TimeStampPt, short ShortFramePt[], long FrameStart, long FrameSz, HTLong FrameLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //获取缓冲帧的数量。
    public native int AAjbGetBufFrameCnt( long AAjbPt, HTInt CurHaveBufActFrameCntPt, HTInt CurHaveBufInactFrameCntPt, HTInt CurHaveBufFrameCntPt, HTInt MinNeedBufFrameCntPt, HTInt MaxNeedBufFrameCntPt, HTInt CurNeedBufFrameCntPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //清空音频自适应抖动缓冲器。
    public native int AAjbClear( long AAjbPt, int IsAutoLockUnlock, long ErrInfoVarStrPt);

    //销毁音频自适应抖动缓冲器。
    public native int AAjbDestroy( long AAjbPt, long ErrInfoVarStrPt);
}