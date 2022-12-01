package HeavenTao.Ado;

import HeavenTao.Data.*;

//Wave文件读取器类。
public class WaveFileReader
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "WaveFile" ); //加载libWaveFile.so。
    }

    public long m_WaveFileReaderPt; //存放Wave文件读取器的指针。

    //构造函数。
    public WaveFileReader()
    {
        m_WaveFileReaderPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy();
    }

    //创建并初始化Wave文件读取器。
    public int Init( String WaveFileFullPathStrPt, HTInt NumChanlPt, HTInt SmplRatePt, HTInt SmplBitPt )
    {
        if( m_WaveFileReaderPt == 0 )
        {
            HTLong p_WaveFileReaderPt = new HTLong();
            if( WaveFileReaderInit( p_WaveFileReaderPt, WaveFileFullPathStrPt, NumChanlPt, SmplRatePt, SmplBitPt ) == 0 )
            {
                m_WaveFileReaderPt = p_WaveFileReaderPt.m_Val;
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

    //用Wave文件读取器读取Short型数据。
    public int ReadShort( short DataPt[], long DataSz, HTLong DataLenPt )
    {
        return WaveFileReaderReadShort( m_WaveFileReaderPt, DataPt, DataSz, DataLenPt );
    }

    //销毁Wave文件读取器。
    public int Dstoy()
    {
        if( m_WaveFileReaderPt != 0 )
        {
            if( WaveFileReaderDstoy( m_WaveFileReaderPt ) == 0 )
            {
                m_WaveFileReaderPt = 0;
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

    //创建并初始化Wave文件读取器。
    public native int WaveFileReaderInit( HTLong WaveFileReaderPt, String WaveFileFullPathStrPt, HTInt NumChanlPt, HTInt SmplRatePt, HTInt SmplBitPt );

    //用Wave文件读取器读取数据。
    public native int WaveFileReaderReadShort( long WaveFileReaderPt, short DataPt[], long DataSz, HTLong DataLenPt );

    //销毁Wave文件读取器。
    public native int WaveFileReaderDstoy( long WaveFileReaderPt );
}
