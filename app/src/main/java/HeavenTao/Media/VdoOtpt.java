package HeavenTao.Media;

import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import HeavenTao.Vdo.*;
import HeavenTao.Data.*;

public class VdoOtpt //视频输出类。
{
	MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

	public int m_IsUseVdoOtpt; //存放是否使用视频输出，为0表示不使用，为非0表示要使用。

	public class VdoOtptStrm
	{
		public int m_VdoOtptStrmIdx; //存放视频输出流索引。

		public int m_IsUseVdoOtptStrm; //存放是否使用视频输出流，为0表示不使用，为非0表示要使用。

		public int m_UseWhatDecd; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264解码器，为2表示系统自带H264解码器。

		OpenH264Decd m_OpenH264DecdPt; //存放OpenH264解码器的指针。
		int m_OpenH264DecdDecdThrdNum; //存放OpenH264解码器的解码线程数，单位为个，为0表示直接在调用线程解码，为1或2或3表示解码子线程的数量。

		SystemH264Decd m_SystemH264DecdPt; //存放系统自带H264解码器的指针。

		HTSurfaceView m_VdoOtptDspySurfaceViewPt; //存放视频输出显示SurfaceView的指针。
		float m_VdoOtptDspyScale; //存放视频输出显示缩放倍数，为1.0f表示不缩放。
		int m_VdoOtptIsBlack; //存放视频输出是否黑屏，为0表示有图像，为非0表示黑屏。

		//视频输出线程的临时变量。
		byte m_VdoOtptRsltFrmPt[]; //存放视频输出结果帧的指针。
		byte m_VdoOtptTmpFrmPt[]; //存放视频输出临时帧的指针。
		byte m_VdoOtptSwapFrmPt[]; //存放视频输出交换帧的指针。
		HTLong m_VdoOtptRsltFrmLenPt; //存放视频输出结果帧的长度，单位字节。
		HTInt m_VdoOtptFrmWidthPt; //存放视频输出帧的宽度，单位为像素。
		HTInt m_VdoOtptFrmHeightPt; //存放视频输出帧的高度，单位为像素。
		long m_LastTimeMsec; //存放上次时间的毫秒数。
		long m_NowTimeMsec; //存放本次时间的毫秒数。

		VdoOtptThrd m_VdoOtptThrdPt; //存放视频输出线程的指针。
		int m_VdoOtptThrdExitFlag; //存放视频输出线程退出标记，0表示保持运行，1表示请求退出。
		
		//初始化解码器。
		public int DecdInit()
		{
			int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

			Out:
			{
				switch( m_UseWhatDecd )
				{
					case 0: //如果要使用YU12原始数据。
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用YU12原始数据。" );
						break;
					}
					case 1: //如果要使用OpenH264解码器。
					{
						m_OpenH264DecdPt = new OpenH264Decd();
						if( m_OpenH264DecdPt.Init( m_OpenH264DecdDecdThrdNum, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：初始化OpenH264解码器成功。" );
						}
						else
						{
							m_OpenH264DecdPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：初始化OpenH264解码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
							break Out;
						}
						break;
					}
					case 2: //如果要使用系统自带H264解码器。
					{
						m_SystemH264DecdPt = new SystemH264Decd();
						if( m_SystemH264DecdPt.Init( null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：初始化系统自带H264解码器成功。" );
						}
						else
						{
							m_SystemH264DecdPt = null;
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：初始化系统自带H264解码器失败。" );
							break Out;
						}
						break;
					}
				}

				p_Rslt = 0; //设置本函数执行成功。
			}

			//if( p_Rslt != 0 ) //如果本函数执行失败。
			{
			}
			return p_Rslt;
		}

		//销毁解码器。
		public void DecdDstoy()
		{
			switch( m_UseWhatDecd )
			{
				case 0: //如果要使用YU12原始数据。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁YU12原始数据成功。" );
					break;
				}
				case 1: //如果要使用OpenH264解码器。
				{
					if( m_OpenH264DecdPt != null )
					{
						if( m_OpenH264DecdPt.Dstoy( null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁OpenH264解码器成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁OpenH264解码器失败。" );
						}
						m_OpenH264DecdPt = null;
					}
					break;
				}
				case 2: //如果要使用系统自带H264解码器。
				{
					if( m_SystemH264DecdPt != null )
					{
						if( m_SystemH264DecdPt.Dstoy( null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁系统自带H264解码器成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁系统自带H264解码器失败。" );
						}
						m_SystemH264DecdPt = null;
					}
					break;
				}
			}
		}

		//初始化视频输出线程。
		public int ThrdInit()
		{
			int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

			Out:
			{
				//初始化视频输出线程的临时变量。
				{
					m_VdoOtptRsltFrmPt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出结果帧的指针。
					m_VdoOtptTmpFrmPt = new byte[ 960 * 1280 * 3 / 2 * 3 ]; //初始化视频输出临时帧的指针。
					m_VdoOtptSwapFrmPt = null; //初始化视频输出交换帧的指针。
					m_VdoOtptRsltFrmLenPt = new HTLong(); //初始化视频输出结果帧的长度。
					m_VdoOtptFrmWidthPt = new HTInt(); //初始化视频输出帧的宽度。
					m_VdoOtptFrmHeightPt = new HTInt(); //初始化视频输出帧的高度。
					m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
					m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：初始化视频输出线程的临时变量成功。" );
				}

				//创建视频输出线程。
				m_VdoOtptThrdExitFlag = 0; //设置视频输出线程退出标记为0表示保持运行。
				m_VdoOtptThrdPt = new VdoOtptThrd();
				m_VdoOtptThrdPt.start();
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：创建视频输出线程成功。" );

				p_Rslt = 0; //设置本函数执行成功。
			}

			//if( p_Rslt != 0 ) //如果本函数执行失败。
			{
			}
			return p_Rslt;
		}

		//销毁视频输出线程。
		public void ThrdDstoy()
		{
			//销毁视频输出线程。
			if( m_VdoOtptThrdPt != null )
			{
				m_VdoOtptThrdExitFlag = 1; //请求视频输出线程退出。
				try
				{
					m_VdoOtptThrdPt.join(); //等待视频输出线程退出。
				}
				catch( InterruptedException ignored )
				{
				}
				m_VdoOtptThrdPt = null;
				m_VdoOtptThrdExitFlag = 0;
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁视频输出线程成功。" );
			}

			//销毁视频输出线程的临时变量。
			{
				m_VdoOtptRsltFrmPt = null; //销毁视频输出结果帧的指针。
				m_VdoOtptTmpFrmPt = null; //销毁视频输出临时帧的指针。
				m_VdoOtptSwapFrmPt = null; //销毁视频输出交换帧的指针。
				m_VdoOtptRsltFrmLenPt = null; //销毁视频输出结果帧的长度。
				m_VdoOtptFrmWidthPt = null; //销毁视频输出帧的宽度。
				m_VdoOtptFrmHeightPt = null; //销毁视频输出帧的高度。
				m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
				m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输出流索引" + m_VdoOtptStrmIdx + "：销毁视频输出线程的临时变量成功。" );
			}
		}

		//初始化视频输出流。
		public int Init()
		{
			int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

			Out:
			{
				if( DecdInit() != 0 ) break Out;
				if( ThrdInit() != 0 ) break Out;

				p_Rslt = 0; //设置本函数执行成功。
			}

			//if( p_Rslt != 0 ) //如果本函数执行失败。
			{
			}
			return p_Rslt;
		}

		//销毁视频输出流。
		public void Dstoy()
		{
			ThrdDstoy();
			DecdDstoy();
		}

		//视频输出线程类。
		public class VdoOtptThrd extends Thread
		{
			public void run()
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：开始准备视频输出。" );

				//视频输出循环开始。
				while( true )
				{
					//开始处理视频输出帧。
					OutPocsVdoOtptFrm:
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) m_LastTimeMsec = System.currentTimeMillis();

						//调用用户定义的写入视频输出帧函数，并解码成YU12原始数据。
						switch( m_UseWhatDecd ) //使用什么解码器。
						{
							case 0: //如果使用YU12原始数据。
							{
								//调用用户定义的写入视频输出帧函数。
								m_VdoOtptFrmWidthPt.m_Val = 0;
								m_VdoOtptFrmHeightPt.m_Val = 0;
								m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptRsltFrmPt.length;
								m_MediaPocsThrdPt.UserWriteVdoOtptFrm( m_VdoOtptStrmIdx, m_VdoOtptRsltFrmPt, m_VdoOtptFrmWidthPt, m_VdoOtptFrmHeightPt, null, m_VdoOtptRsltFrmLenPt );

								if( ( m_VdoOtptFrmWidthPt.m_Val > 0 ) && ( m_VdoOtptFrmHeightPt.m_Val > 0 ) ) //如果本次写入了视频输出帧。
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用YU12原始数据成功。YU12格式帧宽度：" + m_VdoOtptFrmWidthPt.m_Val + "，YU12格式帧高度：" + m_VdoOtptFrmHeightPt + "。" );
								}
								else //如果本次没写入视频输出帧。
								{
									break OutPocsVdoOtptFrm;
								}
								break;
							}
							case 1: //如果使用OpenH264解码器。
							{
								//调用用户定义的写入视频输出帧函数。
								m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptTmpFrmPt.length;
								m_MediaPocsThrdPt.UserWriteVdoOtptFrm( m_VdoOtptStrmIdx, null, null, null, m_VdoOtptTmpFrmPt, m_VdoOtptRsltFrmLenPt );

								if( m_VdoOtptRsltFrmLenPt.m_Val > 0 ) //如果本次写入了视频输出帧。
								{
									//使用OpenH264解码器。
									if( m_OpenH264DecdPt.Pocs(
											m_VdoOtptTmpFrmPt, m_VdoOtptRsltFrmLenPt.m_Val,
											m_VdoOtptRsltFrmPt, m_VdoOtptRsltFrmPt.length, m_VdoOtptFrmWidthPt, m_VdoOtptFrmHeightPt,
											null ) == 0 )
									{
										if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用OpenH264解码器成功。已解码YU12格式帧宽度：" + m_VdoOtptFrmWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VdoOtptFrmHeightPt.m_Val + "。" );
										if( ( m_VdoOtptFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptFrmHeightPt.m_Val == 0 ) ) break OutPocsVdoOtptFrm; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
									}
									else
									{
										if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用OpenH264解码器失败，本次视频输出帧丢弃。" );
										break OutPocsVdoOtptFrm;
									}
								}
								else //如果本次没写入视频输出帧。
								{
									break OutPocsVdoOtptFrm;
								}
								break;
							}
							case 2: //如果使用系统自带H264解码器。
							{
								//调用用户定义的写入视频输出帧函数。
								m_VdoOtptRsltFrmLenPt.m_Val = m_VdoOtptTmpFrmPt.length;
								m_MediaPocsThrdPt.UserWriteVdoOtptFrm( m_VdoOtptStrmIdx, null, null, null, m_VdoOtptTmpFrmPt, m_VdoOtptRsltFrmLenPt );

								if( m_VdoOtptRsltFrmLenPt.m_Val != 0 ) //如果本次写入了视频输出帧。
								{
									//使用系统自带H264解码器。
									if( m_SystemH264DecdPt.Pocs(
											m_VdoOtptTmpFrmPt, m_VdoOtptRsltFrmLenPt.m_Val,
											m_VdoOtptRsltFrmPt, m_VdoOtptRsltFrmPt.length, m_VdoOtptFrmWidthPt, m_VdoOtptFrmHeightPt,
											40, null ) == 0 )
									{
										if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用系统自带H264解码器成功。已解码YU12格式帧宽度：" + m_VdoOtptFrmWidthPt.m_Val + "，已解码YU12格式帧高度：" + m_VdoOtptFrmHeightPt.m_Val + "。" );
										if( ( m_VdoOtptFrmWidthPt.m_Val == 0 ) || ( m_VdoOtptFrmHeightPt.m_Val == 0 ) ) break OutPocsVdoOtptFrm; //如果未解码出YU12格式帧，就把本次视频输出帧丢弃。
									}
									else
									{
										if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：使用系统自带H264解码器失败，本次视频输出帧丢弃。" );
										break OutPocsVdoOtptFrm;
									}
								}
								else //如果本次没写入视频输出帧。
								{
									break OutPocsVdoOtptFrm;
								}
								break;
							}
						}

						//用户定义的获取YU12格式视频输出帧函数。
						m_MediaPocsThrdPt.UserGetYU12VdoOtptFrm( m_VdoOtptStrmIdx, m_VdoOtptRsltFrmPt, m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptFrmHeightPt.m_Val );

						//判断视频输出是否黑屏。在视频处理完后再设置黑屏，这样可以保证视频处理器的连续性。
						if( m_VdoOtptIsBlack != 0 )
						{
							int p_TmpLen = m_VdoOtptFrmWidthPt.m_Val * m_VdoOtptFrmHeightPt.m_Val;
							Arrays.fill( m_VdoOtptRsltFrmPt, 0, p_TmpLen, ( byte ) 0 );
							Arrays.fill( m_VdoOtptRsltFrmPt, p_TmpLen, p_TmpLen + p_TmpLen / 2, ( byte ) 128 );
						}

						//缩放视频输出帧。
						if( m_VdoOtptDspyScale != 1.0f )
						{
							if( LibYUV.PictrScale(
									m_VdoOtptRsltFrmPt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptFrmHeightPt.m_Val,
									3,
									m_VdoOtptTmpFrmPt, m_VdoOtptTmpFrmPt.length, null, ( int )( m_VdoOtptFrmWidthPt.m_Val * m_VdoOtptDspyScale ), ( int )( m_VdoOtptFrmHeightPt.m_Val * m_VdoOtptDspyScale ),
									null ) != 0 )
							{
								Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：视频输出显示缩放失败，本次视频输出帧丢弃。" );
								break OutPocsVdoOtptFrm;
							}
							m_VdoOtptSwapFrmPt = m_VdoOtptRsltFrmPt; m_VdoOtptRsltFrmPt = m_VdoOtptTmpFrmPt; m_VdoOtptTmpFrmPt = m_VdoOtptSwapFrmPt; //交换视频结果帧和视频临时帧。

							m_VdoOtptFrmWidthPt.m_Val *= m_VdoOtptDspyScale;
							m_VdoOtptFrmHeightPt.m_Val *= m_VdoOtptDspyScale;
						}

						//设置视频输出显示SurfaceView的宽高比。
						m_VdoOtptDspySurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoOtptFrmWidthPt.m_Val / m_VdoOtptFrmHeightPt.m_Val );

						//显示视频输出帧。
						if( LibYUV.PictrDrawToSurface(
								m_VdoOtptRsltFrmPt, 0, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoOtptFrmWidthPt.m_Val, m_VdoOtptFrmHeightPt.m_Val,
								m_VdoOtptDspySurfaceViewPt.getHolder().getSurface(),
								null ) != 0 )
						{
							Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：绘制视频输出帧到视频输出显示SurfaceView失败，本次视频输出帧丢弃。" );
							break OutPocsVdoOtptFrm;
						}

						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
						{
							m_NowTimeMsec = System.currentTimeMillis();
							Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：本次视频输出帧处理完毕，耗时 " + ( m_NowTimeMsec - m_LastTimeMsec ) + " 毫秒。" );
						}
					} //处理视频输出帧完毕。

					if( m_VdoOtptThrdExitFlag == 1 ) //如果退出标记为请求退出。
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：接收到退出请求，开始准备退出。" );
						break;
					}

					SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
				} //视频输出循环结束。

				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输出线程：视频输出流索引" + m_VdoOtptStrmIdx + "：本线程已退出。" );
			}
		}
	}
	public LinkedList< VdoOtptStrm > m_VdoOtptStrmLnkLstPt; //存放视频输出流链表的指针。

	//添加视频输出流。
	public void AddVdoOtptStrm( int VdoOtptStrmIdx )
	{
		//查找视频输出流索引是否已经存在。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				return;
			}
		}

		//添加到视频输出流信息链表。
		VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt = new VdoOtpt.VdoOtptStrm();
		p_VdoOtptStrmPt.m_VdoOtptStrmIdx = VdoOtptStrmIdx;
		m_VdoOtptStrmLnkLstPt.addLast( p_VdoOtptStrmPt );
	}

	//删除视频输出流。
	public void DelVdoOtptStrm( int VdoOtptStrmIdx )
	{
		//查找索引是否已经存在。
		for( Iterator< VdoOtpt.VdoOtptStrm > p_VdoOtptStrmItrtr = m_VdoOtptStrmLnkLstPt.iterator(); p_VdoOtptStrmItrtr.hasNext(); )
		{
			VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt = p_VdoOtptStrmItrtr.next();
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				p_VdoOtptStrmPt.Dstoy();
				p_VdoOtptStrmItrtr.remove();
				return;
			}
		}
	}

	//设置视频输出流。
	public void SetVdoOtptStrm( int VdoOtptStrmIdx, HTSurfaceView VdoOtptDspySurfaceViewPt, float VdoOtptDspyScale )
	{
		if( ( VdoOtptDspySurfaceViewPt == null ) || //如果视频显示SurfaceView的指针不正确。
			( VdoOtptDspyScale <= 0 ) ) //如果视频输出显示缩放倍数不正确。
		{
			return;
		}

		//查找视频输出流索引。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

				p_VdoOtptStrmPt.m_VdoOtptDspySurfaceViewPt = VdoOtptDspySurfaceViewPt;
				p_VdoOtptStrmPt.m_VdoOtptDspyScale = VdoOtptDspyScale;

				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
				break;
			}
		}
	}

	//设置视频输出流要使用YU12原始数据。
	public void SetVdoOtptStrmUseYU12( int VdoOtptStrmIdx )
	{
		//查找视频输出流索引。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

				p_VdoOtptStrmPt.m_UseWhatDecd = 0;

				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
				break;
			}
		}
	}

	//设置视频输出流要使用OpenH264解码器。
	public void SetVdoOtptStrmUseOpenH264Decd( int VdoOtptStrmIdx, int DecdThrdNum )
	{
		//查找视频输出流索引。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

				p_VdoOtptStrmPt.m_UseWhatDecd = 1;
				p_VdoOtptStrmPt.m_OpenH264DecdDecdThrdNum = DecdThrdNum;

				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
				break;
			}
		}
	}

	//设置视频输出流要使用系统自带H264解码器。
	public void SetVdoOtptStrmUseSystemH264Decd( int VdoOtptStrmIdx )
	{
		//查找视频输出流索引。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Dstoy();

				p_VdoOtptStrmPt.m_UseWhatDecd = 2;

				if( ( m_IsUseVdoOtpt != 0 ) && ( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) ) p_VdoOtptStrmPt.Init();
				break;
			}
		}
	}

	//设置视频输出流是否黑屏。
	public void SetVdoOtptStrmIsBlack( int VdoOtptStrmIdx, int IsBlack )
	{
		//查找视频输出流索引。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx )
			{
				p_VdoOtptStrmPt.m_VdoOtptIsBlack = IsBlack;
				break;
			}
		}
	}

	//设置视频输出流是否使用。
	public void SetVdoOtptStrmIsUse( int VdoOtptStrmIdx, int IsUseVdoOtptStrm )
	{
		//查找视频输出流索引是否已经存在。
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			if( p_VdoOtptStrmPt.m_VdoOtptStrmIdx == VdoOtptStrmIdx ) //如果索引找到了。
			{
				if( IsUseVdoOtptStrm != 0 ) //如果是要使用视频输出流。
				{
					if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm == 0 ) //如果视频输出流当前为不使用。
					{
						if( m_IsUseVdoOtpt != 0 ) //如果要使用视频输出。
						{
							if( p_VdoOtptStrmPt.Init() == 0 ) //如果初始化视频输出流成功。
							{
								p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 1;
							}
						}
						else //如果不使用视频输出。
						{
							p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 1;
						}
					}
				}
				else //如果是不使用视频输出流。
				{
					if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 ) //如果视频输出流当前为要使用。
					{
						if( m_IsUseVdoOtpt != 0 ) //如果要使用视频输出。
						{
							p_VdoOtptStrmPt.Dstoy();
							p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 0;
						}
						else //如果不使用视频输出。
						{
							p_VdoOtptStrmPt.m_IsUseVdoOtptStrm = 0;
						}
					}
				}
			}
		}
	}
	
	//初始化视频输出。
	public int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		Out:
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

			for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
			{
				if( p_VdoOtptStrmPt.m_IsUseVdoOtptStrm != 0 )
				{
					if( p_VdoOtptStrmPt.Init() != 0 ) break Out;
				}
			}

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
			{
				p_NowMsec = System.currentTimeMillis(); //记录初始化结束的时间。
				Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输出耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//销毁视频输出。
	public void Dstoy()
	{
		for( VdoOtpt.VdoOtptStrm p_VdoOtptStrmPt : m_VdoOtptStrmLnkLstPt )
		{
			p_VdoOtptStrmPt.Dstoy();
		}
		if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输出成功。" );
	}
}