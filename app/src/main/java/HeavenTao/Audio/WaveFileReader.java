package HeavenTao.Audio;

import HeavenTao.Data.*;

//Wave文件读取器类。
public class WaveFileReader
{
    private long m_pvPoint; //存放Wave文件读取器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WaveFile" ); //加载libWaveFile.so。
    }

    //构造函数。
    public WaveFileReader()
    {
        m_pvPoint = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destory();
    }

    //获取Wave文件读取器的内存指针。
    public long GetPoint()
    {
        return m_pvPoint;
    }

    //创建并初始化Wave文件读取器。
    public native long Init( byte pszi8WaveFileFullPath[], short i16NumChannel, int i32SamplingRate, int i32SamplingBit );

    //用Wave文件读取器读取数据。
    public native long WriteData( short pszi16Data[], int i32DataLength );

    //销毁Wave文件读取器。
    public native long Destory();
}
