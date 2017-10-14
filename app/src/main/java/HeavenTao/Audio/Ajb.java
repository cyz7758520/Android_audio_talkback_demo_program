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

    //初始化专用自适应抖动缓冲器
    public int Init( int iSamplingRate, int iFrameSize, int iNotActiveIsContinuePut )
    {
        if( clAjb.longValue() == 0 )//如果专用自适应抖动缓冲器还没有初始化
        {
            return AjbInit( clAjb, iSamplingRate, iFrameSize, iNotActiveIsContinuePut );
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

    //放入一帧音频数据到自适应抖动缓冲器
    public int PutByteAudioData( byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutByteAudioData( clAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //放入一帧音频数据到自适应抖动缓冲器
    public int PutShortAudioData( short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp )
    {
        return AjbPutShortAudioData( clAjb, clAudioData, iAudioDataSize, liAudioDataTimeStamp );
    }

    //取出一帧音频数据从自适应抖动缓冲器
    public int GetByteAudioData( byte clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetByteAudioData( clAjb, clAudioData, clAudioDataSize );
    }

    //取出一帧音频数据从自适应抖动缓冲器
    public int GetShortAudioData( short clAudioData[], Integer clAudioDataSize )
    {
        return AjbGetShortAudioData( clAjb, clAudioData, clAudioDataSize );
    }

    //获取当前已缓冲音频数据帧的数量
    public int GetCurHaveBufferSize( Integer clBufferSize, int iIsIncludeMute )
    {
        return AjbGetCurHaveBufferSize( clAjb, clBufferSize, iIsIncludeMute );
    }

    //获取当前需缓冲音频数据帧的数量
    public int GetCurNeedBufferSize( Integer clBufferSize )
    {
        return AjbGetCurNeedBufferSize( clAjb, clBufferSize );
    }

    //销毁专用自适应抖动缓冲器
    public void Destory()
    {
        AjbDestory( clAjb );
        clAjb = new Long(0);
    }

    private native int AjbInit( Long clAjb, int iSamplingRate, int iFrameSize, int iNotActiveIsContinuePut );
    private native int AjbPutByteAudioData( Long clAjb, byte clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );
    private native int AjbPutShortAudioData( Long clAjb, short clAudioData[], int iAudioDataSize, long liAudioDataTimeStamp );
    private native int AjbGetByteAudioData( Long clAjb, byte clAudioData[], Integer clAudioDataSize );
    private native int AjbGetShortAudioData( Long clAjb, short clAudioData[], Integer clAudioDataSize );
    private native int AjbGetCurHaveBufferSize( Long clAjb, Integer clBufferSize, int iIsIncludeMute );
    private native int AjbGetCurNeedBufferSize( Long clAjb, Integer clBufferSize );
    private native int AjbClearAudioDataList( Long clAjb );
    private native void AjbDestory( Long clAjb );
}