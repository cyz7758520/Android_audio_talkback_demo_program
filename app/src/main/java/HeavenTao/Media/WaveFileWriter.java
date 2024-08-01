package HeavenTao.Media;

import HeavenTao.Data.*;

//Wave文件写入器。
public class WaveFileWriter
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "MediaFile" ); //加载libMediaFile.so。
	}

	public long m_WaveFileWriterPt; //存放Wave文件写入器的指针。

	//构造函数。
	public WaveFileWriter()
	{
		m_WaveFileWriterPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//创建并初始化Wave文件写入器。
	public int Init( String WaveFileFullPathStrPt, long WaveFileWrBufSzByt, int NumChanl, int SmplRate, int SmplBit )
	{
		if( m_WaveFileWriterPt == 0 )
		{
			HTLong p_WaveFileWriterPt = new HTLong();
			if( WaveFileWriterInit( p_WaveFileWriterPt, WaveFileFullPathStrPt, WaveFileWrBufSzByt, NumChanl, SmplRate, SmplBit ) == 0 )
			{
				m_WaveFileWriterPt = p_WaveFileWriterPt.m_Val;
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

	//用Wave文件写入器写入Short型数据。
	public int WriteShort( short DataPt[], long DataLen )
	{
		return WaveFileWriterWriteShort( m_WaveFileWriterPt, DataPt, DataLen );
	}

	//销毁Wave文件写入器。
	public int Dstoy()
	{
		if( m_WaveFileWriterPt != 0 )
		{
			if( WaveFileWriterDstoy( m_WaveFileWriterPt ) == 0 )
			{
				m_WaveFileWriterPt = 0;
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

	//创建并初始化Wave文件写入器。
	private native int WaveFileWriterInit( HTLong WaveFileWriterPt, String WaveFileFullPathStrPt, long WaveFileWrBufSzByt, int NumChanl, int SmplRate, int SmplBit );

	//用Wave文件写入器写入数据。
	private native int WaveFileWriterWriteShort( long WaveFileWriterPt, short DataPt[], long DataLen );

	//销毁Wave文件写入器。
	private native int WaveFileWriterDstoy( long WaveFileWriterPt );
}
