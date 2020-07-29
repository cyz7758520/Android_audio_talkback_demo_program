package HeavenTao.Audio;

import HeavenTao.Data.*;

//Wave文件写入器类。
public class WaveFileWriter
{
    private long m_WaveFileWriterPt; //存放Wave文件写入器的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WaveFile" ); //加载libWaveFile.so。
    }

    //构造函数。
    public WaveFileWriter()
    {
        m_WaveFileWriterPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取Wave文件写入器的内存指针。
    public long GetWaveFileWriterPt()
    {
        return m_WaveFileWriterPt;
    }

    //创建并初始化Wave文件写入器。
    public native int Init( String WaveFileFullPathStrPt, short NumChanl, int SamplingRate, int SamplingBit );

    //用Wave文件写入器写入数据。
    public native int WriteData( short DataPt[], long DataLen );

    //销毁Wave文件写入器。
    public native int Destroy();
}
