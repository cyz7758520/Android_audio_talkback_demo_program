package HeavenTao.Audio;

//自适应抖动缓冲器类。
public class Ajb
{
    private Long pclAjb; //自适应抖动缓冲器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Ajb" ); //加载libAjb.so。
    }

    //构造函数。
    public Ajb()
    {
        pclAjb = new Long(0);
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //初始化自适应抖动缓冲器。
    public long Init( int iSamplingRate, int iFrameSize, int iIsHaveTimeStamp, int iInactiveIsContinuePut, int iMaxNeedBufferDataFrameCount, int iMinNeedBufferDataFrameCount )
    {
        if( pclAjb == 0 )//如果自适应抖动缓冲器还没有初始化。
        {
            return AjbInit( pclAjb, iSamplingRate, iFrameSize, iIsHaveTimeStamp, iInactiveIsContinuePut, iMaxNeedBufferDataFrameCount, iMinNeedBufferDataFrameCount );
        }
        else //如果自适应抖动缓冲器已经初始化。
        {
            return 0;
        }
    }

    //销毁自适应抖动缓冲器。
    public void Destory()
    {
        AjbDestory( pclAjb );
    }

    //获取自适应抖动缓冲器的内存指针。
    public long GetAjb()
    {
        return pclAjb;
    }

    //放入一个字节型数据帧到自适应抖动缓冲器。
    public long PutOneByteDataFrame( int iTimeStamp, byte clData[], long lDataSize )
    {
        return AjbPutOneByteDataFrame( pclAjb, iTimeStamp, clData, lDataSize );
    }

    //放入一个短整型数据帧到自适应抖动缓冲器。
    public long PutOneShortDataFrame( int iTimeStamp, short clData[], long lDataSize )
    {
        return AjbPutOneShortDataFrame( pclAjb, iTimeStamp, clData, lDataSize );
    }

    //从自适应抖动缓冲器取出一个字节型数据帧。
    public long GetOneByteDataFrame( byte clData[], Long clDataSize )
    {
        return AjbGetOneByteDataFrame( pclAjb, clData, clDataSize );
    }

    //从自适应抖动缓冲器取出一个短整型数据帧。
    public long GetOneShortDataFrame( short clData[], Long clDataSize )
    {
        return AjbGetOneShortDataFrame( pclAjb, clData, clDataSize );
    }

    //获取当前已缓冲有活动数据帧的数量。
    public long GetCurHaveBufferActiveDataFrameCount( Integer clBufferSize )
    {
        return AjbGetCurHaveBufferActiveDataFrameCount( pclAjb, clBufferSize );
    }

    //获取当前已缓冲无活动数据帧的数量。
    public long GetCurHaveBufferInactiveDataFrameCount( Integer clBufferSize )
    {
        return AjbGetCurHaveBufferInactiveDataFrameCount( pclAjb, clBufferSize );
    }

    //获取当前已缓冲数据帧的数量。
    public long GetCurHaveBufferDataFrameCount( Integer clBufferSize )
    {
        return AjbGetCurHaveBufferDataFrameCount( pclAjb, clBufferSize );
    }

    //获取最大需缓冲数据帧的数量。
    public long GetMaxNeedBufferDataFrameCount( Integer clBufferSize )
    {
        return AjbGetMaxNeedBufferDataFrameCount( pclAjb, clBufferSize );
    }

    //获取最小需缓冲数据帧的数量。
    public long GetMinNeedBufferDataFrameCount( Integer clBufferSize )
    {
        return AjbGetMinNeedBufferDataFrameCount( pclAjb, clBufferSize );
    }

    //获取当前需缓冲数据帧的数量。
    public long GetCurNeedBufferDataFrameCount( Integer clBufferSize )
    {
        return AjbGetCurNeedBufferDataFrameCount( pclAjb, clBufferSize );
    }

    //清空自适应抖动缓冲器。
    private long ClearAjb()
    {
        return AjbClearAjb( pclAjb );
    }

    private native long AjbInit( Long pclAjb, int iSamplingRate, int iFrameSize, int iIsHaveTimeStamp, int iInactiveIsContinuePut, int iMaxNeedBufferDataFrameCount, int iMinNeedBufferDataFrameCount ); //初始化自适应抖动缓冲器。
    private native void AjbDestory( Long pclAjb ); //销毁自适应抖动缓冲器。

    private native long AjbPutOneByteDataFrame( Long pclAjb, int iTimeStamp, byte clData[], long lDataSize ); //放入一个字节型数据帧到自适应抖动缓冲器。
    private native long AjbPutOneShortDataFrame( Long pclAjb, int iTimeStamp, short clData[], long lDataSize ); //放入一个短整型数据帧到自适应抖动缓冲器。
    private native long AjbGetOneByteDataFrame( Long pclAjb, byte clData[], Long clDataSize ); //从自适应抖动缓冲器取出一个字节型数据帧。
    private native long AjbGetOneShortDataFrame( Long pclAjb, short clData[], Long clDataSize ); //从自适应抖动缓冲器取出一个短整型数据帧。

    private native long AjbGetCurHaveBufferActiveDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取当前已缓冲有活动数据帧的数量。
    private native long AjbGetCurHaveBufferInactiveDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取当前已缓冲无活动数据帧的数量。
    private native long AjbGetCurHaveBufferDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取当前已缓冲数据帧的数量。

    private native long AjbGetMaxNeedBufferDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取最大需缓冲数据帧的数量。
    private native long AjbGetMinNeedBufferDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取最小需缓冲数据帧的数量。
    private native long AjbGetCurNeedBufferDataFrameCount( Long pclAjb, Integer clBufferSize ); //获取当前需缓冲数据帧的数量。

    private native long AjbClearAjb( Long pclAjb ); //清空自适应抖动缓冲器。

}