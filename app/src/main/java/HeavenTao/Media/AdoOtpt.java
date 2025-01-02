package HeavenTao.Media;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Ado.*;
import HeavenTao.Data.*;

public class AdoOtpt //音频输出。
{
	MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

	public int m_IsUse; //存放是否使用音频输出，为0表示不使用，为非0表示要使用。
	public int m_IsInit; //存放是否初始化音频输出，为0表示未初始化，为非0表示已初始化。

	public int m_SmplRate; //存放采样频率，取值只能为8000、16000、32000、48000。
	public long m_FrmLenMsec; //存放帧的长度，单位为毫秒，取值只能为10毫秒的倍数。
	public long m_FrmLenUnit; //存放帧的长度，单位为采样单元，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
	public long m_FrmLenData; //存放帧的长度，单位为采样数据，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80、20毫秒为160、30毫秒为240，16000Hz的10毫秒为160、20毫秒为320、30毫秒为480，32000Hz的10毫秒为320、20毫秒为640、30毫秒为960，48000Hz的10毫秒为480、20毫秒为960、30毫秒为1440。
	public long m_FrmLenByt; //存放帧的长度，单位为字节，取值只能为10毫秒的倍数。例如：8000Hz的10毫秒为80*2、20毫秒为160*2、30毫秒为240*2，16000Hz的10毫秒为160*2、20毫秒为320*2、30毫秒为480*2，32000Hz的10毫秒为320*2、20毫秒为640*2、30毫秒为960*2，48000Hz的10毫秒为480*2、20毫秒为960*2、30毫秒为1440*2。

	public class Strm //存放流。
	{
		public int m_Idx; //存放索引。

		public int m_IsUse; //存放是否使用流，为0表示不使用，为非0表示要使用。

		public int m_UseWhatDecd; //存放使用什么解码器，为0表示Pcm原始数据，为1表示Speex解码器，为2表示Opus解码器。

		class SpeexDecd //存放Speex解码器。
		{
			public HeavenTao.Ado.SpeexDecd m_Pt; //存放指针。
			public int m_IsUsePrcplEnhsmt; //存放是否使用知觉增强，为非0表示要使用，为0表示不使用。
		}
		SpeexDecd m_SpeexDecdPt = new SpeexDecd();

		//初始化音频输出的流。
		public int Init()
		{
			int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

			Out:
			{
				//初始化解码器。
				switch( m_UseWhatDecd )
				{
					case 0: //如果要使用Pcm原始数据。
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：初始化Pcm原始数据成功。" );
						break;
					}
					case 1: //如果要使用Speex解码器。
					{
						if( m_FrmLenMsec != 20 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：帧的长度不为20毫秒不能使用Speex解码器。" );
							break Out;
						}
						m_SpeexDecdPt.m_Pt = new HeavenTao.Ado.SpeexDecd();
						if( m_SpeexDecdPt.m_Pt.Init( m_SmplRate, m_SpeexDecdPt.m_IsUsePrcplEnhsmt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：初始化Speex解码器成功。" );
						}
						else
						{
							m_SpeexDecdPt.m_Pt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：初始化Speex解码器失败。" );
							break Out;
						}
						break;
					}
					case 2: //如果要使用Opus解码器。
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：暂不支持使用Opus解码器。" );
						break Out;
					}
				}

				p_Rslt = 0; //设置本函数执行成功。
			}

			//if( p_Rslt != 0 ) //如果本函数执行失败。
			{
			}
			return p_Rslt;
		}

		//销毁音频输出的流。
		public void Dstoy()
		{
			//销毁解码器。
			switch( m_UseWhatDecd )
			{
				case 0: //如果要使用Pcm原始数据。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：销毁Pcm原始数据成功。" );
					break;
				}
				case 1: //如果要使用Speex解码器。
				{
					if( m_SpeexDecdPt.m_Pt != null )
					{
						if( m_SpeexDecdPt.m_Pt.Dstoy() == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：销毁Speex解码器成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：销毁Speex解码器失败。" );
						}
						m_SpeexDecdPt.m_Pt = null;
					}
					break;
				}
				case 2: //如果要使用Opus解码器。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：销毁Opus解码器成功。" );
					break;
				}
			}

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出流索引" + m_Idx + "：音频输出流销毁成功。" );
		}
	}
	public ArrayList< Strm > m_StrmCntnrPt; //存放流容器的指针。
	public int m_StrmUseTotal; //存放流要使用的总数。

	class Wavfm //存放波形器。
	{
		public int m_IsDraw; //存放是否绘制，为非0表示要绘制，为0表示不绘制。
		AdoWavfm m_SrcPt; //存放原始的指针。
		SurfaceView m_SrcSurfacePt; //存放原始Surface的指针。
	}
	Wavfm m_WavfmPt = new Wavfm();

	class WaveFileWriter //存放Wave文件写入器。
	{
		public int m_IsSave; //存放是否保存，为非0表示要保存，为0表示不保存。
		HeavenTao.Media.WaveFileWriter m_SrcPt; //存放原始的指针。
		String m_SrcFullPathStrPt; //存放原始完整路径字符串的指针。
		long m_WrBufSzByt; //存放写入缓冲区的大小，单位为字节。
	}
	WaveFileWriter m_WaveFileWriterPt = new WaveFileWriter();

	public class Dvc //存放设备。
	{
		public AudioTrack m_Pt; //存放指针。
		int m_BufSzByt; //存放缓冲区大小，单位为字节。
		public int m_UseWhatStreamType; //存放使用什么流类型，为0表示通话类型，为非0表示媒体类型。
		public int m_IsMute; //存放是否静音，为0表示有声音，为非0表示静音。
	}
	public Dvc m_DvcPt = new Dvc();

	public ConcurrentLinkedQueue< short[] > m_PcmSrcFrmCntnrPt; //存放Pcm格式原始帧容器的指针。
	public ConcurrentLinkedQueue< short[] > m_PcmIdleFrmCntnrPt; //存放Pcm格式空闲帧容器的指针。

	class Thrd //存放线程。
	{
		int m_IsInitThrdTmpVar; //存放是否初始化线程的临时变量。
		short m_PcmSrcFrmPt[]; //存放Pcm格式原始帧的指针。
		byte m_EncdSrcFrmPt[]; //存放已编码格式原始帧的指针。
		HTLong m_EncdSrcFrmLenBytPt; //存放已编码格式原始帧的长度的指针，单位为字节。
		int m_PcmMixFrmPt[]; //存放Pcm格式混音帧的指针。
		int m_ElmTotal; //存放元素总数。
		long m_LastTickMsec; //存放上次的嘀嗒钟，单位为毫秒。
		long m_NowTickMsec; //存放本次的嘀嗒钟，单位为毫秒。

		AdoOtptThrd m_ThrdPt; //存放线程的指针。
		int m_ThrdIsStart; //存放线程是否开始，为0表示未开始，为1表示已开始。
		int m_ExitFlag; //存放线程退出标记，为0表示保持运行，为1表示请求退出。
	}
	Thrd m_ThrdPt = new Thrd();

	//添加音频输出的流。
	public void AddStrm( int StrmIdx )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					return;
				}
			}

			//添加到流容器。
			Strm p_StrmPt = new Strm();
			p_StrmPt.m_Idx = StrmIdx;
			m_StrmCntnrPt.add( p_StrmPt );
		}
	}

	//删除音频输出的流。
	public void DelStrm( int StrmIdx )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Iterator< Strm > p_StrmItrtr = m_StrmCntnrPt.iterator(); p_StrmItrtr.hasNext(); )
			{
				Strm p_StrmPt = p_StrmItrtr.next();
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					//从流容器删除。
					if( m_IsInit != 0 ) //如果已初始化音频输出。
					{
						p_StrmPt.Dstoy();
					}
					if( p_StrmPt.m_IsUse != 0 ) //如果要使用音频输出流。
					{
						m_StrmUseTotal--;
					}
					p_StrmItrtr.remove();
					return;
				}
			}
		}
	}

	//设置音频输出的流要使用Pcm原始数据。
	public void SetStrmUsePcm( int StrmIdx )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

					p_StrmPt.m_UseWhatDecd = 0;

					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
					return;
				}
			}
		}
	}

	//设置音频输出的流要使用Speex解码器。
	public void SetStrmUseSpeexDecd( int StrmIdx, int IsUsePrcplEnhsmt )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

					p_StrmPt.m_UseWhatDecd = 1;
					p_StrmPt.m_SpeexDecdPt.m_IsUsePrcplEnhsmt = IsUsePrcplEnhsmt;

					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
					return;
				}
			}
		}
	}

	//设置音频输出的流要使用Opus编码器。
	public void SetStrmUseOpusDecd( int StrmIdx )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Dstoy();

					p_StrmPt.m_UseWhatDecd = 2;

					if( ( m_IsInit != 0 ) && ( p_StrmPt.m_IsUse != 0 ) ) p_StrmPt.Init();
					return;
				}
			}
		}
	}

	//设置音频输出的流是否使用。
	public void SetStrmIsUse( int StrmIdx, int IsUseStrm )
	{
		synchronized( m_StrmCntnrPt )
		{
			//查找流索引。
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_Idx == StrmIdx ) //如果流索引找到了。
				{
					if( IsUseStrm != 0 ) //如果要使用流。
					{
						if( p_StrmPt.m_IsUse == 0 ) //如果当前不使用流。
						{
							if( m_IsInit != 0 ) //如果已初始化音频输出。
							{
								if( p_StrmPt.Init() != 0 ) //如果初始化音频输出的流失败。
								{
									return;
								}
							}
							p_StrmPt.m_IsUse = 1;
							m_StrmUseTotal++;
						}
					}
					else //如果不使用流。
					{
						if( p_StrmPt.m_IsUse != 0 ) //如果当前要使用流。
						{
							if( m_IsInit != 0 ) //如果已初始化音频输出。
							{
								p_StrmPt.Dstoy();
							}
							p_StrmPt.m_IsUse = 0;
							m_StrmUseTotal--;
						}
					}
					return;
				}
			}
		}
	}

	//初始化音频输出的流容器。
	public int StrmCntnrInit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			for( Strm p_StrmPt : m_StrmCntnrPt )
			{
				if( p_StrmPt.m_IsUse != 0 )
				{
					if( p_StrmPt.Init() != 0 ) break Out;
				}
			}
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化流容器成功。" );

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输出的流容器。
	public void StrmCntnrDstoy()
	{
		for( Strm p_StrmPt : m_StrmCntnrPt )
		{
			p_StrmPt.Dstoy();
		}
		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁流容器成功。" );
	}

	//初始化音频输出的波形器。
	public int WavfmInit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_WavfmPt.m_IsDraw != 0 )
			{
				m_WavfmPt.m_SrcPt = new AdoWavfm();
				if( m_WavfmPt.m_SrcPt.Init( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化原始波形器成功。" );
				}
				else
				{
					m_WavfmPt.m_SrcPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
					break Out;
				}
				m_WavfmPt.m_SrcSurfacePt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS ); //设置预览Surface视图的类型。老机型上必须要用。
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输出的波形器。
	public void WavfmDstoy()
	{
		if( m_WavfmPt.m_IsDraw != 0 )
		{
			if( m_WavfmPt.m_SrcPt != null )
			{
				if( m_WavfmPt.m_SrcPt.Dstoy( m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁原始波形器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁原始波形器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
				}
				m_WavfmPt.m_SrcPt = null;
			}
		}
	}

	//初始化音频输出的Wave文件写入器。
	public int WaveFileWriterInit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_WaveFileWriterPt.m_IsSave != 0 )
			{
				m_WaveFileWriterPt.m_SrcPt = new HeavenTao.Media.WaveFileWriter();
				if( m_WaveFileWriterPt.m_SrcPt.Init( m_WaveFileWriterPt.m_SrcFullPathStrPt, m_WaveFileWriterPt.m_WrBufSzByt, ( short ) 1, m_SmplRate, 16 ) == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化原始Wave文件 " + m_WaveFileWriterPt.m_SrcFullPathStrPt + " 写入器成功。" );
				}
				else
				{
					m_WaveFileWriterPt.m_SrcPt = null;
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化原始Wave文件 " + m_WaveFileWriterPt.m_SrcFullPathStrPt + " 写入器失败。" );
					break Out;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输出的Wave文件写入器。
	public void WaveFileWriterDstoy()
	{
		if( m_WaveFileWriterPt.m_IsSave != 0 )
		{
			if( m_WaveFileWriterPt.m_SrcPt != null )
			{
				if( m_WaveFileWriterPt.m_SrcPt.Dstoy() == 0 )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁原始Wave文件写入器成功。" );
				}
				else
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁原始Wave文件写入器失败。" );
				}
				m_WaveFileWriterPt.m_SrcPt = null;
			}
		}
	}

	//初始化音频输出的设备和线程。
	public int DvcAndThrdInit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			//初始化设备。
			{
				//打开蓝牙Sco协议。每次初始化设备都要打开，因为切换设备后可能会关闭。
				( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).setBluetoothScoOn( true );
				( ( AudioManager ) MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).startBluetoothSco();

				//设置默认的设备。必须要在初始化设备前设置，否则可能会失效。
				if( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftSpeaker ) //如果要使用默认扬声器。
				{
					( ( AudioManager )MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( true ); //打开扬声器。
				}
				else if( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftEarpiece ) //如果要使用默认听筒。
				{
					( ( AudioManager )MediaPocsThrd.m_CtxPt.getSystemService( Context.AUDIO_SERVICE ) ).setSpeakerphoneOn( false ); //关闭扬声器。
				}

				//用第一种方法创建并初始化设备。
				try
				{
					m_DvcPt.m_BufSzByt = 2;
					m_DvcPt.m_Pt = new AudioTrack( ( m_DvcPt.m_UseWhatStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
												   m_SmplRate,
												   AudioFormat.CHANNEL_CONFIGURATION_MONO,
												   AudioFormat.ENCODING_PCM_16BIT,
												   m_DvcPt.m_BufSzByt,
												   AudioTrack.MODE_STREAM );
					if( m_DvcPt.m_Pt.getState() == AudioTrack.STATE_INITIALIZED )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第一种方法初始化设备成功。采样频率：" + m_SmplRate + "，缓冲区大小：" + m_DvcPt.m_BufSzByt + "。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第一种方法初始化设备失败。" );
						m_DvcPt.m_Pt.release();
						m_DvcPt.m_Pt = null;
					}
				}
				catch( IllegalArgumentException e )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第一种方法初始化设备失败。原因：" + e.getMessage() );
				}

				//用第二种方法初始化设备。
				if( m_DvcPt.m_Pt == null )
				{
					try
					{
						m_DvcPt.m_BufSzByt = AudioTrack.getMinBufferSize( m_SmplRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT );
						m_DvcPt.m_Pt = new AudioTrack( ( m_DvcPt.m_UseWhatStreamType == 0 ) ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
													   m_SmplRate,
													   AudioFormat.CHANNEL_CONFIGURATION_MONO,
													   AudioFormat.ENCODING_PCM_16BIT,
													   m_DvcPt.m_BufSzByt,
													   AudioTrack.MODE_STREAM );
						if( m_DvcPt.m_Pt.getState() == AudioTrack.STATE_INITIALIZED )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第二种方法初始化设备成功。采样频率：" + m_SmplRate + "，缓冲区大小：" + m_DvcPt.m_BufSzByt + "。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第二种方法初始化设备失败。" );
							break Out;
						}
					}
					catch( IllegalArgumentException e )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：用第二种方法初始化设备失败。原因：" + e.getMessage() );
						break Out;
					}
				}

				//设置音频输出使用的设备。
				if( android.os.Build.VERSION.SDK_INT >= 23 )
				{
					/*if( ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftSpeaker ) || //如果要使用默认扬声器或默认听筒。这里不获取音频输出当前路由的设备，因为未开启音频输出时获取的可能会在开启音频输出后变化，导致误认为设备不一致而重新初始化设备。
						( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftEarpiece ) )
					{
						m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt = m_DvcPt.m_Pt.getRoutedDevice(); //获取音频输出当前路由的设备。
					}
					else */if( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) //如果要使用指定的。
					{
						if( !m_DvcPt.m_Pt.setPreferredDevice( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt ) ) //如果设置音频输出使用的设备失败。
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：设置音频输出使用的设备失败，发送音频输入输出设备关闭线程消息。" );
							m_MediaPocsThrdPt.m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcClos );
							p_Rslt = 0; //这里返回成功是为了防止媒体处理线程报错退出。
							break Out;
						}
					}

					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
					{
						AudioDeviceInfo p_AdoOtptPreferredDevice = m_DvcPt.m_Pt.getPreferredDevice();
						AudioDeviceInfo p_AdoOtptRoutedDevice = m_DvcPt.m_Pt.getRoutedDevice();

						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：设置音频输出使用的设备[" + m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_NameStrPt + "]成功。" +
																"指定设备：" + ( ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) ? ( MediaPocsThrd.GetAdoDvcInfoTypeName( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getType() ) + m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getProductName() ) : "空" ) + "。" +
																"首选设备：" + ( ( p_AdoOtptPreferredDevice != null ) ? ( MediaPocsThrd.GetAdoDvcInfoTypeName( p_AdoOtptPreferredDevice.getType() ) + p_AdoOtptPreferredDevice.getProductName() ) : "空" ) + "。" +
																"路由设备：" + ( ( p_AdoOtptRoutedDevice != null ) ? ( MediaPocsThrd.GetAdoDvcInfoTypeName( p_AdoOtptRoutedDevice.getType() ) + p_AdoOtptRoutedDevice.getProductName() ) : "空" ) + "。" );

						if( p_AdoOtptPreferredDevice == null )
						{
							Log.w( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的首选设备为空。" );
						}
						else if( p_AdoOtptPreferredDevice.getId() != m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getId() )
						{
							Log.w( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的首选设备与指定设备不一致。" );
						}
						else if( p_AdoOtptRoutedDevice == null )
						{
							Log.w( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的路由设备为空。" );
						}
						else if( p_AdoOtptRoutedDevice.getId() != m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getId() )
						{
							Log.w( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的路由设备与指定设备不一致。" );
						}
					}
				}

				//发送音频输入输出设备改变线程消息。
				if( ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) && //如果要使用指定设备，且不使用音频输入，或音频输入已初始化，才发送消息，避免重复发送。
					( ( m_MediaPocsThrdPt.m_AdoInptPt.m_IsUse == 0 ) || ( m_MediaPocsThrdPt.m_AdoInptPt.m_IsInit != 0 ) ) )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：发送音频输入输出设备改变线程消息。" );
					m_MediaPocsThrdPt.m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcChg );
				}
			}

			//初始化Pcm格式原始帧容器。
			m_PcmSrcFrmCntnrPt = new ConcurrentLinkedQueue<>();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化Pcm格式原始帧容器成功。" );

			//初始化Pcm格式空闲帧容器。
			m_PcmIdleFrmCntnrPt = new ConcurrentLinkedQueue<>();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化Pcm格式空闲帧容器成功。" );

			//初始化线程的临时变量。
			{
				m_ThrdPt.m_IsInitThrdTmpVar = 1; //设置已初始化线程的临时变量。
				m_ThrdPt.m_PcmSrcFrmPt = null; //初始化Pcm格式原始帧的指针。
				m_ThrdPt.m_PcmMixFrmPt = new int[ ( int )m_FrmLenUnit ]; //初始化Pcm格式混音帧的指针。
				m_ThrdPt.m_EncdSrcFrmPt = new byte[ ( int )m_FrmLenByt ]; //初始化已编码格式原始帧的指针。
				m_ThrdPt.m_EncdSrcFrmLenBytPt = new HTLong(); //初始化已编码格式原始帧的长度的指针。
				m_ThrdPt.m_ElmTotal = 0; //初始化元素总数。
				m_ThrdPt.m_LastTickMsec = 0; //初始化上次的嘀嗒钟。
				m_ThrdPt.m_NowTickMsec = 0; //初始化本次的嘀嗒钟。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化线程的临时变量成功。" );
			}

			//初始化线程。
			{
				m_ThrdPt.m_ThrdIsStart = 0; //设置线程为未开始。
				m_ThrdPt.m_ExitFlag = 0; //设置线程退出标记为0表示保持运行。
				m_ThrdPt.m_ThrdPt = new AdoOtptThrd();
				m_ThrdPt.m_ThrdPt.start();
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化线程成功。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		//if( p_Rslt != 0 ) //如果本函数执行失败。
		{
		}
		return p_Rslt;
	}

	//销毁音频输出的设备和线程。
	public void DvcAndThrdDstoy()
	{
		//销毁线程。
		if( m_ThrdPt.m_ThrdPt != null )
		{
			m_ThrdPt.m_ExitFlag = 1; //请求线程退出。
			try
			{
				m_ThrdPt.m_ThrdPt.join(); //等待线程退出。
			}
			catch( InterruptedException ignored )
			{
			}
			m_ThrdPt.m_ThrdPt = null;
			m_ThrdPt.m_ThrdIsStart = 0;
			m_ThrdPt.m_ExitFlag = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁线程成功。" );
		}

		//销毁线程的临时变量。
		if( m_ThrdPt.m_IsInitThrdTmpVar != 0 )
		{
			m_ThrdPt.m_IsInitThrdTmpVar = 0; //设置未初始化线程的临时变量。
			m_ThrdPt.m_PcmSrcFrmPt = null; //销毁Pcm格式原始帧的指针。
			m_ThrdPt.m_PcmMixFrmPt = null; //销毁Pcm格式混音帧的指针。
			m_ThrdPt.m_EncdSrcFrmPt = null; //销毁已编码格式原始帧的指针。
			m_ThrdPt.m_EncdSrcFrmLenBytPt = null; //销毁已编码格式原始帧的长度的指针。
			m_ThrdPt.m_ElmTotal = 0; //销毁元素总数。
			m_ThrdPt.m_LastTickMsec = 0; //销毁上次的嘀嗒钟。
			m_ThrdPt.m_NowTickMsec = 0; //销毁本次的嘀嗒钟。
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁线程的临时变量成功。" );
		}

		//销毁Pcm格式空闲帧容器。
		if( m_PcmIdleFrmCntnrPt != null )
		{
			m_PcmIdleFrmCntnrPt.clear();
			m_PcmIdleFrmCntnrPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁Pcm格式空闲帧容器成功。" );
		}

		//销毁Pcm格式原始帧容器。
		if( m_PcmSrcFrmCntnrPt != null )
		{
			m_PcmSrcFrmCntnrPt.clear();
			m_PcmSrcFrmCntnrPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁Pcm格式原始帧容器成功。" );
		}

		//销毁设备。
		if( m_DvcPt.m_Pt != null )
		{
			if( m_DvcPt.m_Pt.getPlayState() != AudioTrack.PLAYSTATE_STOPPED ) m_DvcPt.m_Pt.stop();
			m_DvcPt.m_Pt.release();
			m_DvcPt.m_Pt = null;
			m_DvcPt.m_BufSzByt = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁设备成功。" );
		}
	}

	//初始化音频输出。
	public int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		Out:
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录初始化开始的时间。

			if( StrmCntnrInit() != 0 ) break Out;
			if( WaveFileWriterInit() != 0 ) break Out;
			if( WavfmInit() != 0 ) break Out;
			if( DvcAndThrdInit() != 0 ) break Out;

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
			{
				p_NowMsec = SystemClock.uptimeMillis(); //记录初始化结束的时间。
				Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：初始化耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//销毁音频输出。
	public void Dstoy()
	{
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = SystemClock.uptimeMillis(); //记录销毁开始的时间。

		DvcAndThrdDstoy();
		WavfmDstoy();
		WaveFileWriterDstoy();
		StrmCntnrDstoy();

		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
		{
			p_NowMsec = SystemClock.uptimeMillis(); //记录销毁结束的时间。
			Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：销毁耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
		}
	}

	//音频输出线程。
	public class AdoOtptThrd extends Thread
	{
		public void run()
		{
			while( ( m_ThrdPt.m_ThrdIsStart == 0 ) && ( m_ThrdPt.m_ExitFlag == 0 ) ) SystemClock.sleep( 1 ); //等待线程开始。这里判断退出标记是因为音频输入可能会初始化失败导致不会让线程开始。

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：开始准备音频输出。" );

			//音频输出循环开始。
			while( true )
			{
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
				{
					AudioDeviceInfo p_AdoOtptRoutedDevice = m_DvcPt.m_Pt.getRoutedDevice();
					/*Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：当前音频输出使用的设备[" + m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_NameStrPt + "]。" +
															"指定设备：" + ( ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) ? ( MediaPocsThrd.GetAdoDvcInfoTypeName( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getType() ) + m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getProductName() ) : "空" ) + "。" +
															"路由设备：" + ( ( p_AdoOtptRoutedDevice != null ) ? ( MediaPocsThrd.GetAdoDvcInfoTypeName( p_AdoOtptRoutedDevice.getType() ) + p_AdoOtptRoutedDevice.getProductName() ) : "空" ) + "。" );*/
					if( ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftSpeaker ) || //如果要使用默认扬声器或默认听筒。
						( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.AdoInptOtptDvcInfo.DvcTyp.DftEarpiece ) )
					{
						if( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt == null ) //如果音频输出指定的设备信息为空。
						{
							if( p_AdoOtptRoutedDevice != null ) //如果音频输出路由的设备信息不为空。
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的指定设备改为[" + ( MediaPocsThrd.GetAdoDvcInfoTypeName( p_AdoOtptRoutedDevice.getType() ) + p_AdoOtptRoutedDevice.getProductName() ) + "]，发送音频输入输出设备改变线程消息。" );
								m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt = p_AdoOtptRoutedDevice;
								m_MediaPocsThrdPt.m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcChg ); //补发音频输入输出设备改变线程消息。
							}
						}
						else //如果音频输出指定的设备信息不为空。
						{
							if( ( p_AdoOtptRoutedDevice != null ) && ( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getId() != p_AdoOtptRoutedDevice.getId() ) ) //如果音频输出指定的设备与路由的设备不一致。
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：音频输出：音频输出的指定设备[" + ( MediaPocsThrd.GetAdoDvcInfoTypeName( m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getType() ) + m_MediaPocsThrdPt.m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getProductName() ) + "]与路由设备[" + ( MediaPocsThrd.GetAdoDvcInfoTypeName( p_AdoOtptRoutedDevice.getType() ) + p_AdoOtptRoutedDevice.getProductName() ) + "]不一致，发送音频输入输出设备关闭线程消息。" );
								m_MediaPocsThrdPt.m_ThrdMsgQueuePt.SendMsg( 0, 0, MediaPocsThrd.ThrdMsgTyp.ThrdMsgTypAdoInptOtptDvcClos );
								break; //这里要退出线程，防止多次发送线程消息。
							}
						}
					}
				}

				OutPocs:
				{
					//获取一个Pcm格式空闲帧。
					m_ThrdPt.m_ElmTotal = m_PcmIdleFrmCntnrPt.size(); //获取Pcm格式空闲帧容器的元素总数。
					if( m_ThrdPt.m_ElmTotal > 0 ) //如果Pcm格式空闲帧容器中有帧。
					{
						m_ThrdPt.m_PcmSrcFrmPt = m_PcmIdleFrmCntnrPt.poll(); //从Pcm格式空闲帧容器中取出并删除第一个帧。
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：从Pcm格式空闲帧容器中取出并删除第一个帧，Pcm格式空闲帧容器元素总数：" + m_ThrdPt.m_ElmTotal + "。" );
					}
					else //如果Pcm格式空闲帧容器中没有帧。
					{
						m_ThrdPt.m_ElmTotal = m_PcmSrcFrmCntnrPt.size(); //获取Pcm格式原始帧容器的元素总数。
						if( m_ThrdPt.m_ElmTotal <= 50 )
						{
							m_ThrdPt.m_PcmSrcFrmPt = new short[ ( int )m_FrmLenUnit ]; //创建一个Pcm格式空闲帧。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：Pcm格式空闲帧容器中没有帧，创建一个Pcm格式空闲帧成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：Pcm格式原始帧容器中帧总数为" + m_ThrdPt.m_ElmTotal + "已经超过上限50，不再创建Pcm格式空闲帧。" );
							SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
							break OutPocs;
						}
					}

					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) m_ThrdPt.m_LastTickMsec = SystemClock.uptimeMillis();

					//调用用户定义的写入音频输出帧函数，并解码成Pcm原始数据，最后混音。
					synchronized( m_StrmCntnrPt )
					{
						if( m_StrmUseTotal > 0 ) //如果有流要使用。
						{
							Iterator< Strm > p_StrmItrtr = m_StrmCntnrPt.iterator();
							Strm p_StrmPt; //存放流的指针。

							while( p_StrmItrtr.hasNext() ) //查找第一条要使用的流。
							{
								p_StrmPt = ( Strm )p_StrmItrtr.next();

								if( p_StrmPt.m_IsUse != 0 ) //如果该流为要使用。
								{
									switch( p_StrmPt.m_UseWhatDecd ) //使用什么解码器。
									{
										case 0: //如果要使用Pcm原始数据。
										{
											//调用用户定义的写入音频输出帧函数。
											m_MediaPocsThrdPt.UserWriteAdoOtptFrm( p_StrmPt.m_Idx,
																				   m_ThrdPt.m_PcmSrcFrmPt, ( int ) m_FrmLenUnit,
																				   null, 0, null );

											//调用用户定义的获取音频输出帧函数。
											m_MediaPocsThrdPt.UserGetAdoOtptFrm( p_StrmPt.m_Idx,
																				 m_ThrdPt.m_PcmSrcFrmPt, m_FrmLenUnit,
																				 null, 0 );
											break;
										}
										case 1: //如果要使用Speex解码器。
										{
											//调用用户定义的写入音频输出帧函数。
											m_MediaPocsThrdPt.UserWriteAdoOtptFrm( p_StrmPt.m_Idx,
																				   null, 0,
																				   m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmPt.length, m_ThrdPt.m_EncdSrcFrmLenBytPt );

											//使用Speex解码器。
											if( p_StrmPt.m_SpeexDecdPt.m_Pt.Pocs( m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmLenBytPt.m_Val, m_ThrdPt.m_PcmSrcFrmPt ) == 0 )
											{
												if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：使用Speex解码器成功。" );
											}
											else
											{
												if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：使用Speex解码器失败。" );
											}

											//调用用户定义的获取音频输出帧函数。
											m_MediaPocsThrdPt.UserGetAdoOtptFrm( p_StrmPt.m_Idx,
																				 m_ThrdPt.m_PcmSrcFrmPt, m_FrmLenUnit,
																				 m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmLenBytPt.m_Val );
											break;
										}
										case 2: //如果要使用Opus解码器。
										{
											if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：暂不支持使用Opus解码器。" );
										}
									}

									break;
								}
							}

							if( m_StrmUseTotal > 1 ) //如果有超过1条流要使用。
							{
								for( int p_TmpInt = 0; p_TmpInt < m_ThrdPt.m_PcmMixFrmPt.length; p_TmpInt++ ) //将第一个流的Pcm格式原始帧复制到Pcm格式混音帧。
								{
									m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] = m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ];
								}

								while( p_StrmItrtr.hasNext() ) //查找其他要使用的流。
								{
									p_StrmPt = ( Strm )p_StrmItrtr.next();

									if( p_StrmPt.m_IsUse != 0 ) //如果该流为要使用。
									{
										switch( p_StrmPt.m_UseWhatDecd ) //使用什么解码器。
										{
											case 0: //如果要使用Pcm原始数据。
											{
												//调用用户定义的写入音频输出帧函数。
												m_MediaPocsThrdPt.UserWriteAdoOtptFrm( p_StrmPt.m_Idx,
																					   m_ThrdPt.m_PcmSrcFrmPt, ( int ) m_FrmLenUnit,
																					   null, 0, null );

												//调用用户定义的获取音频输出帧函数。
												m_MediaPocsThrdPt.UserGetAdoOtptFrm( p_StrmPt.m_Idx,
																					 m_ThrdPt.m_PcmSrcFrmPt, m_FrmLenUnit,
																					 null, 0 );
												break;
											}
											case 1: //如果要使用Speex解码器。
											{
												//调用用户定义的写入音频输出帧函数。
												m_MediaPocsThrdPt.UserWriteAdoOtptFrm( p_StrmPt.m_Idx,
																					   null, 0,
																					   m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmPt.length, m_ThrdPt.m_EncdSrcFrmLenBytPt );

												//使用Speex解码器。
												if( p_StrmPt.m_SpeexDecdPt.m_Pt.Pocs( m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmLenBytPt.m_Val, m_ThrdPt.m_PcmSrcFrmPt ) == 0 )
												{
													if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：使用Speex解码器成功。" );
												}
												else
												{
													if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：使用Speex解码器失败。" );
												}

												//调用用户定义的获取音频输出帧函数。
												m_MediaPocsThrdPt.UserGetAdoOtptFrm( p_StrmPt.m_Idx,
																					 m_ThrdPt.m_PcmSrcFrmPt, m_FrmLenUnit,
																					 m_ThrdPt.m_EncdSrcFrmPt, m_ThrdPt.m_EncdSrcFrmLenBytPt.m_Val );
												break;
											}
											case 2: //如果要使用Opus解码器。
											{
												if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：音频输出流索引" + p_StrmPt.m_Idx + "：暂不支持使用Opus解码器。" );
											}
										}

										for( int p_TmpInt = 0; p_TmpInt < m_ThrdPt.m_PcmMixFrmPt.length; p_TmpInt++ ) //混音。
										{
											m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] = m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ] + m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] - ( ( m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ] * m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] ) >> 0x10 );
										}
									}
								}

								for( int p_TmpInt = 0; p_TmpInt < m_ThrdPt.m_PcmMixFrmPt.length; p_TmpInt++ ) //将Pcm格式混音帧复制到Pcm格式原始帧。
								{
									if( m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] > 32767 ) m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ] = 32767;
									else if( m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ] < -32768 ) m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ] = -32768;
									else m_ThrdPt.m_PcmSrcFrmPt[ p_TmpInt ] = ( short ) m_ThrdPt.m_PcmMixFrmPt[ p_TmpInt ];
								}
							}
						}
						else //如果没有流要使用。
						{
							Arrays.fill( m_ThrdPt.m_PcmSrcFrmPt, ( short ) 0 );
						}
					}

					//判断设备是否静音。在音频处理完后再设置静音，这样可以保证音频处理器的连续性。
					if( m_DvcPt.m_IsMute != 0 )
					{
						Arrays.fill( m_ThrdPt.m_PcmSrcFrmPt, ( short ) 0 );
					}

					//写入本次Pcm格式原始帧到设备。
					m_DvcPt.m_Pt.write( m_ThrdPt.m_PcmSrcFrmPt, 0, m_ThrdPt.m_PcmSrcFrmPt.length );

					//放入本次Pcm格式原始帧到Pcm格式原始帧容器。注意：从取出到放入过程中不能跳出，否则会内存泄露。
					{
						m_PcmSrcFrmCntnrPt.offer( m_ThrdPt.m_PcmSrcFrmPt );
						m_ThrdPt.m_PcmSrcFrmPt = null;
					}

					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
					{
						m_ThrdPt.m_NowTickMsec = SystemClock.uptimeMillis();
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：本次帧处理完毕，耗时 " + ( m_ThrdPt.m_NowTickMsec - m_ThrdPt.m_LastTickMsec ) + " 毫秒。" );
					}
				}

				if( m_ThrdPt.m_ExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：接收退出请求。" );
					break;
				}
			} //音频输出循环结束。

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "音频输出线程：本线程已退出。" );
		}
	}
}
