package HeavenTao.Audio;

public class Ajb//自适应抖动缓冲器类
{
    private Long clAjb;//自适应抖动缓冲器的内存指针

    //构造函数
    public Ajb()
    {
        clAjb = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化自适应抖动缓冲器
    public int Init( int iSamplingRate, int iFrameSize, int iInactiveIsContinuePut )
    {
        if( clAjb.longValue() == 0 )//如果专用自适应抖动缓冲器还没有初始化
        {
            return AjbInit( clAjb, iSamplingRate, iFrameSize, iInactiveIsContinuePut );
        }
        else//如果专用自适应抖动缓冲器已经初始化
        {
            return 0;
        }
    }

    //获取自适应抖动缓冲器的内存指针
    public Long GetAjb()
    {
        return clAjb;
    }

    //将一帧字节型音频数据放入自适应抖动缓冲器
    public int PutByteAudioData( byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutByteAudioData( clAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //将一帧短整型音频数据放入自适应抖动缓冲器
    public int PutShortAudioData( short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutShortAudioData( clAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //从自适应抖动缓冲器取出一帧字节型音频数据
    public int GetByteAudioData( byte clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetByteAudioData( clAjb, clAudioData, clAudioDataSize );
    }

    //从自适应抖动缓冲器取出一帧短整型音频数据
    public int GetShortAudioData( short clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetShortAudioData( clAjb, clAudioData, clAudioDataSize );
    }

    //获取当前已缓冲有语音活动音频数据帧数量
    public int GetCurHaveActiveBufferSize( Integer clBufferSize )
    {
        return AjbGetCurHaveActiveBufferSize( clAjb, clBufferSize );
    }

    //获取当前已缓冲无语音活动音频数据帧数量
    public int GetCurHaveInactiveBufferSize( Integer clBufferSize )
    {
        return AjbGetCurHaveInactiveBufferSize( clAjb, clBufferSize );
    }

    //获取当前需缓冲音频数据帧的数量
    public int GetCurNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetCurNeedBufferSize( clAjb, clBufferSize );
    }

    //获取最大需缓冲音频数据帧的数量
    public int GetMaxNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetMaxNeedBufferSize( clAjb, clBufferSize );
    }

    //获取最小需缓冲音频数据帧的数量
    public int GetMinNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetMinNeedBufferSize( clAjb, clBufferSize );
    }

    //清空音频数据帧链表
    private int ClearAudioDataList()
    {
        return AjbClearAudioDataList( clAjb );
    }

    //销毁自适应抖动缓冲器
    public void Destory()
    {
        AjbDestory( clAjb );
        clAjb = new Long(0);
    }

    private native int AjbInit( Long clAjb, int iSamplingRate, int iFrameSize, int iInactiveIsContinuePut );//初始化自适应抖动缓冲器
    private native int AjbPutByteAudioData( Long clAjb, byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );//将一帧字节型音频数据放入自适应抖动缓冲器
    private native int AjbPutShortAudioData( Long clAjb, short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );//将一帧短整型音频数据放入自适应抖动缓冲器
    private native int AjbGetByteAudioData( Long clAjb, byte clAudioData[], Integer clAudioDataSize );//从自适应抖动缓冲器取出一帧字节型音频数据
    private native int AjbGetShortAudioData( Long clAjb, short clAudioData[], Integer clAudioDataSize );//从自适应抖动缓冲器取出一帧短整型音频数据
    private native int AjbGetCurHaveActiveBufferSize( Long clAjb, Integer clBufferSize );//获取当前已缓冲有语音活动音频数据帧数量
    private native int AjbGetCurHaveInactiveBufferSize( Long clAjb, Integer clBufferSize );//获取当前已缓冲无语音活动音频数据帧数量
    private native int AjbGetCurNeedBufferSize( Long clAjb, Integer clBufferSize );//获取当前需缓冲音频数据帧的数量
    private native int AjbGetMaxNeedBufferSize( Long clAjb, Integer clBufferSize );//获取最大需缓冲音频数据帧的数量
    private native int AjbGetMinNeedBufferSize( Long clAjb, Integer clBufferSize );//获取最小需缓冲音频数据帧的数量
    private native int AjbClearAudioDataList( Long clAjb );//清空音频数据帧链表
    private native void AjbDestory( Long clAjb );//销毁自适应抖动缓冲器
}