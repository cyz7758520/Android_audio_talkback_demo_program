package HeavenTao.Audio;

public class Ajb//自适应抖动缓冲器类
{
    private Long pclAjb;//自适应抖动缓冲器的内存指针

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so
        System.loadLibrary( "Ajb" ); //加载libAjb.so
    }

    //构造函数
    public Ajb()
    {
        pclAjb = new Long(0);
    }

    //析构函数
    public void finalize()
    {
        Destory();
    }

    //初始化自适应抖动缓冲器
    public long Init( int iSamplingRate, int iFrameSize, int iInactiveIsContinuePut )
    {
        if( pclAjb.longValue() == 0 )//如果专用自适应抖动缓冲器还没有初始化
        {
            return AjbInit( pclAjb, iSamplingRate, iFrameSize, iInactiveIsContinuePut );
        }
        else//如果专用自适应抖动缓冲器已经初始化
        {
            return 0;
        }
    }

    //获取自适应抖动缓冲器的内存指针
    public Long GetAjb()
    {
        return pclAjb;
    }

    //将一帧字节型音频数据放入自适应抖动缓冲器
    public int PutByteAudioData( byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutByteAudioData( pclAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //将一帧短整型音频数据放入自适应抖动缓冲器
    public int PutShortAudioData( short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutShortAudioData( pclAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //从自适应抖动缓冲器取出一帧字节型音频数据
    public int GetByteAudioData( byte clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetByteAudioData( pclAjb, clAudioData, clAudioDataSize );
    }

    //从自适应抖动缓冲器取出一帧短整型音频数据
    public int GetShortAudioData( short clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetShortAudioData( pclAjb, clAudioData, clAudioDataSize );
    }

    //获取当前已缓冲有语音活动音频数据帧数量
    public int GetCurHaveActiveBufferSize( Integer clBufferSize )
    {
        return AjbGetCurHaveActiveBufferSize( pclAjb, clBufferSize );
    }

    //获取当前已缓冲无语音活动音频数据帧数量
    public int GetCurHaveInactiveBufferSize( Integer clBufferSize )
    {
        return AjbGetCurHaveInactiveBufferSize( pclAjb, clBufferSize );
    }

    //获取当前需缓冲音频数据帧的数量
    public int GetCurNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetCurNeedBufferSize( pclAjb, clBufferSize );
    }

    //获取最大需缓冲音频数据帧的数量
    public int GetMaxNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetMaxNeedBufferSize( pclAjb, clBufferSize );
    }

    //获取最小需缓冲音频数据帧的数量
    public int GetMinNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetMinNeedBufferSize( pclAjb, clBufferSize );
    }

    //清空音频数据帧链表
    private int ClearAudioDataList()
    {
        return AjbClearAudioDataList( pclAjb );
    }

    //销毁自适应抖动缓冲器
    public void Destory()
    {
        AjbDestory( pclAjb );
        pclAjb = new Long(0);
    }

    private native long AjbInit( Long pclAjb, int iSamplingRate, int iFrameSize, int iInactiveIsContinuePut );//初始化自适应抖动缓冲器
    private native int AjbPutByteAudioData( Long pclAjb, byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );//将一帧字节型音频数据放入自适应抖动缓冲器
    private native int AjbPutShortAudioData( Long pclAjb, short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );//将一帧短整型音频数据放入自适应抖动缓冲器
    private native int AjbGetByteAudioData( Long pclAjb, byte clAudioData[], Integer clAudioDataSize );//从自适应抖动缓冲器取出一帧字节型音频数据
    private native int AjbGetShortAudioData( Long pclAjb, short clAudioData[], Integer clAudioDataSize );//从自适应抖动缓冲器取出一帧短整型音频数据
    private native int AjbGetCurHaveActiveBufferSize( Long pclAjb, Integer clBufferSize );//获取当前已缓冲有语音活动音频数据帧数量
    private native int AjbGetCurHaveInactiveBufferSize( Long pclAjb, Integer clBufferSize );//获取当前已缓冲无语音活动音频数据帧数量
    private native int AjbGetCurNeedBufferSize( Long pclAjb, Integer clBufferSize );//获取当前需缓冲音频数据帧的数量
    private native int AjbGetMaxNeedBufferSize( Long pclAjb, Integer clBufferSize );//获取最大需缓冲音频数据帧的数量
    private native int AjbGetMinNeedBufferSize( Long pclAjb, Integer clBufferSize );//获取最小需缓冲音频数据帧的数量
    private native int AjbClearAudioDataList( Long pclAjb );//清空音频数据帧链表
    private native void AjbDestory( Long pclAjb );//销毁自适应抖动缓冲器
}