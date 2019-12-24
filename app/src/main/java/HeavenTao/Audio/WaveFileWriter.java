package HeavenTao.Audio;

import HeavenTao.Data.*;

//Wave文件写入器类。
public class WaveFileWriter
{
    private long m_pvPoint; //存放Wave文件写入器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WaveFile" ); //加载libWaveFile.so。
    }

    //构造函数。
    public WaveFileWriter()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Wave文件写入器的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Wave文件写入器。
    public native long Init( byte pszi8WaveFileFullPath[], short i16NumChannel, int i32SamplingRate, int i32SamplingBit );

    //用Wave文件写入器写入数据。
    public native long WriteData( short pszi16Data[], int i32DataLength );

    //销毁Wave文件写入器。
    public native long Destory();
}
