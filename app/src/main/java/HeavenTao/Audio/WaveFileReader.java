package HeavenTao.Audio;

import HeavenTao.Data.*;

//Wave文件读取器类。
public class WaveFileReader
{
    private long m_WaveFileReaderPt; //存放Wave文件读取器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WaveFile" ); //加载libWaveFile.so。
    }

    //构造函数。
    public WaveFileReader()
    {
        m_WaveFileReaderPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取Wave文件读取器的内存指针。
    public long GetWaveFileReaderPt()
    {
        return m_WaveFileReaderPt;
    }

    //创建并初始化Wave文件读取器。
    public native int Init( byte WaveFileFullPathStrPt[], HTShort NumChanlPt, HTInt SamplingRatePt, HTInt SamplingBitPt );

    //用Wave文件读取器读取数据。
    public native int ReadData( short DataPt[], long DataSz, HTLong DataLenPt );

    //销毁Wave文件读取器。
    public native int Destroy();
}
