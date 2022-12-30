package HeavenTao.Media;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import HeavenTao.Vdo.*;
import HeavenTao.Data.*;

public class VdoInpt //视频输入类。
{
	MediaPocsThrd m_MediaPocsThrdPt; //存放媒体处理线程的指针。

	public int m_IsUseVdoInpt; //存放是否使用视频输入，为0表示不使用，为非0表示要使用。
	public int m_IsInitVdoInpt; //存放是否初始化视频输入，为0表示未初始化，为非0表示已初始化。

	public int m_MaxSmplRate; //存放最大采样频率，取值范围为[1,60]，实际帧率和图像的亮度有关，亮度较高时采样频率可以达到最大值，亮度较低时系统就自动降低采样频率来提升亮度。
	public int m_FrmWidth; //存放屏幕旋转0度时，帧的宽度，单位为像素。
	public int m_FrmHeight; //存放屏幕旋转0度时，帧的高度，单位为像素。
	public int m_ScreenRotate; //存放屏幕旋转的角度，只能为0、90、180、270，0度表示竖屏，其他表示顺时针旋转。

	public int m_UseWhatEncd; //存放使用什么编码器，为0表示YU12原始数据，为1表示OpenH264编码器，为2表示系统自带H264编码器。

	OpenH264Encd m_OpenH264EncdPt; //存放OpenH264编码器的指针。
	int m_OpenH264EncdVdoType;//存放OpenH264编码器的视频类型，为0表示实时摄像头视频，为1表示实时屏幕内容视频，为2表示非实时摄像头视频，为3表示非实时屏幕内容视频，为4表示其他视频。
	int m_OpenH264EncdEncdBitrate; //存放OpenH264编码器的编码后比特率，单位为bps。
	int m_OpenH264EncdBitrateCtrlMode; //存放OpenH264编码器的比特率控制模式，为0表示质量优先模式，为1表示比特率优先模式，为2表示缓冲区优先模式，为3表示时间戳优先模式。
	int m_OpenH264EncdIDRFrmIntvl; //存放OpenH264编码器的IDR帧间隔帧数，单位为个，为0表示仅第一帧为IDR帧，为大于0表示每隔这么帧就至少有一个IDR帧。
	int m_OpenH264EncdCmplxt; //存放OpenH264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

	SystemH264Encd m_SystemH264EncdPt; //存放系统自带H264编码器的指针。
	int m_SystemH264EncdEncdBitrate; //存放系统自带H264编码器的编码后比特率，单位为bps。
	int m_SystemH264EncdBitrateCtrlMode; //存放系统自带H264编码器的比特率控制模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ(0x00)表示质量模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR(0x01)表示动态比特率模式，为MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR(0x02)表示固定比特率模式。
	int m_SystemH264EncdIDRFrmIntvlTimeSec; //存放系统自带H264编码器的IDR帧间隔时间，单位为秒，为负数表示仅第一帧为IDR帧，为0表示每一帧都为IDR帧，为大于0表示每这么多秒就有一个IDR帧。
	int m_SystemH264EncdCmplxt; //存放系统自带H264编码器的复杂度，复杂度越高压缩率不变、CPU使用率越高、画质越好，取值区间为[0,2]。

	public Camera m_VdoInptDvcPt; //存放视频输入设备的指针。
	public int m_UseWhatVdoInptDvc; //存放使用什么视频输入设备，为0表示前置摄像头，为1表示后置摄像头。
	int m_FrontCameraDvcId; //存放前置摄像头的设备ID，为-1表示自动查找。
	int m_BackCameraDvcId; //存放后置摄像头的设备ID，为-1表示自动查找。
	public HTSurfaceView m_VdoInptPrvwSurfaceViewPt; //存放视频输入预览SurfaceView的指针。
	SurfaceHolder.Callback m_VdoInptPrvwSurfaceClbkPt; //存放视频输入预览Surface回调函数的指针。
	public byte m_VdoInptPrvwClbkBufPtPt[][]; //存放视频输入预览回调函数缓冲区的指针。
	VodInptPrvwClbk m_VodInptPrvwClbkPt; //存放视频输入预览回调函数的指针。
	int m_VdoInptDvcFrmWidth; //存放视频输入设备帧的宽度，单位为像素。
	int m_VdoInptDvcFrmHeight; //存放视频输入设备帧的高度，单位为像素。
	int m_VdoInptDvcFrmIsCrop; //存放视频输入设备帧是否裁剪，为0表示不裁剪，为非0表示要裁剪。
	int m_VdoInptDvcFrmCropX; //存放视频输入设备帧裁剪区域左上角的横坐标，单位像素。
	int m_VdoInptDvcFrmCropY; //存放视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
	int m_VdoInptDvcFrmCropWidth; //存放视频输入设备帧裁剪区域的宽度，单位像素。
	int m_VdoInptDvcFrmCropHeight; //存放视频输入设备帧裁剪区域的高度，单位像素。
	int m_VdoInptDvcFrmRotate; //存放视频输入设备帧旋转的角度，只能为0、90、180、270，0度表示横屏，其他表示顺时针旋转。
	int m_VdoInptDvcFrmRotateWidth; //存放视频输入设备帧旋转后的宽度，单位为像素。
	int m_VdoInptDvcFrmRotateHeight; //存放视频输入设备帧旋转后的高度，单位为像素。
	int m_VdoInptDvcFrmIsScale; //存放视频输入设备帧是否缩放，为0表示不缩放，为非0表示要缩放。
	public int m_VdoInptDvcFrmScaleWidth; //存放视频输入帧缩放后的宽度，单位为像素。
	public int m_VdoInptDvcFrmScaleHeight; //存放视频输入帧缩放后的高度，单位为像素。
	int m_VdoInptIsBlack; //存放视频输入是否黑屏，为0表示有图像，为非0表示黑屏。

	public class VdoInptFrmElm //视频输入帧链表元素类。
	{
		byte m_YU12VdoInptFrmPt[]; //存放YU12格式视频输入帧的指针。
		HTInt m_YU12VdoInptFrmWidthPt; //存放YU12格式视频输入帧的宽度。
		HTInt m_YU12VdoInptFrmHeightPt; //存放YU12格式视频输入帧的高度。
		byte m_EncdVdoInptFrmPt[]; //存放已编码格式视频输入帧。
		HTLong m_EncdVdoInptFrmLenPt; //存放已编码格式视频输入帧的长度，单位字节。
	}
	public LinkedList< byte[] > m_NV21VdoInptFrmLnkLstPt; //存放NV21格式视频输入帧链表的指针。
	public LinkedList< VdoInptFrmElm > m_VdoInptFrmLnkLstPt; //存放视频输入帧链表的指针。
	public LinkedList< VdoInptFrmElm > m_VdoInptIdleFrmLnkLstPt; //存放视频输入空闲帧链表的指针。

	int m_IsInitVdoInptThrdTmpVar; //存放是否初始化视频输入线程的临时变量。
	long m_LastVdoInptFrmTimeMsec; //存放上一个视频输入帧的时间，单位毫秒。
	long m_VdoInptFrmTimeStepMsec; //存放视频输入帧的时间步进，单位毫秒。
	byte m_VdoInptFrmPt[]; //存放视频输入帧的指针。
	byte m_VdoInptRsltFrmPt[]; //存放视频输入结果帧的指针。
	byte m_VdoInptTmpFrmPt[]; //存放视频输入临时帧的指针。
	byte m_VdoInptSwapFrmPt[]; //存放视频输入交换帧的指针。
	long m_VdoInptRsltFrmSz; //存放视频输入结果帧的内存大小，单位字节。
	HTLong m_VdoInptRsltFrmLenPt; //存放视频输入结果帧的长度，单位字节。
	VdoInptFrmElm m_VdoInptFrmElmPt; //存放视频输入帧元素的指针。
	int m_VdoInptFrmLnkLstElmTotal; //存放视频输入帧链表的元数总数。
	long m_LastTimeMsec; //存放上次时间的毫秒数。
	long m_NowTimeMsec; //存放本次时间的毫秒数。

	VdoInptThrd m_VdoInptThrdPt; //存放视频输入线程的指针。
	int m_VdoInptThrdExitFlag; //存放视频输入线程退出标记，0表示保持运行，1表示请求退出。

	//初始化视频输入。
	int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		long p_LastMsec = 0;
		long p_NowMsec = 0;

		Out:
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) p_LastMsec = System.currentTimeMillis(); //记录初始化开始的时间。

			//初始化视频输入设备。
			{
				//打开视频输入设备。
				{
					int p_CameraDvcId = 0;
					Camera.CameraInfo p_CameraInfoPt = new Camera.CameraInfo();

					//查找视频输入设备对应的ID。
					if( m_UseWhatVdoInptDvc == 0 ) //如果要使用前置摄像头。
					{
						p_CameraDvcId = m_FrontCameraDvcId;
					}
					else if( m_UseWhatVdoInptDvc == 1 ) //如果要使用后置摄像头。
					{
						p_CameraDvcId = m_BackCameraDvcId;
					}
					if( p_CameraDvcId == -1 ) //如果需要自动查找设备ID。
					{
						for( p_CameraDvcId = 0; p_CameraDvcId < Camera.getNumberOfCameras(); p_CameraDvcId++ )
						{
							try
							{
								Camera.getCameraInfo( p_CameraDvcId, p_CameraInfoPt );
							}
							catch( Exception e )
							{
								String p_InfoStrPt = "媒体处理线程：获取视频输入设备 " + p_CameraDvcId + " 的信息失败。原因：" + e.getMessage();
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
								if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
								break Out;
							}
							if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
							{
								if( m_UseWhatVdoInptDvc == 0 ) break;
							}
							else if( p_CameraInfoPt.facing == Camera.CameraInfo.CAMERA_FACING_BACK )
							{
								if( m_UseWhatVdoInptDvc == 1 ) break;
							}
						}
						if( p_CameraDvcId == Camera.getNumberOfCameras() )
						{
							String p_InfoStrPt = "媒体处理线程：查找视频输入设备对应的ID失败。原因：没有" + ( ( m_UseWhatVdoInptDvc == 0 ) ? "前置摄像头。" : "后置摄像头。" );
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
							if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break Out;
						}
					}
	
					//打开视频输入设备。
					try
					{
						m_VdoInptDvcPt = Camera.open( p_CameraDvcId );
					}
					catch( RuntimeException e )
					{
						String p_InfoStrPt = "媒体处理线程：初始化视频输入设备失败。原因：打开视频输入设备失败。原因：" + e.getMessage();
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, p_InfoStrPt );
						if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
						break Out;
					}
				}

				Camera.Parameters p_CameraParaPt = m_VdoInptDvcPt.getParameters(); //获取视频输入设备的参数。

				p_CameraParaPt.setPreviewFormat( ImageFormat.NV21 ); //设置预览帧的格式。

				p_CameraParaPt.setPreviewFrameRate( m_MaxSmplRate ); //设置最大采样频率。

				//遍历视频输入设备支持的帧大小，并智能选择满足目标的视频输入设备帧大小。
				int p_TgtVdoInptFrmWidth = m_FrmHeight; //存放目标视频输入帧的宽度，单位为像素。
				int p_TgtVdoInptFrmHeight = m_FrmWidth; //存放目标视频输入帧的高度，单位为像素。
				double p_TgtVdoInptFrmAspectRatio = ( double )p_TgtVdoInptFrmWidth / ( double )p_TgtVdoInptFrmHeight; //存放目标视频输入帧的宽高比。
				List< Camera.Size > p_SupportedPrvwSizesListPt = p_CameraParaPt.getSupportedPreviewSizes(); //设置视频输入设备支持的预览帧大小。
				Camera.Size p_CameraSizePt; //存放本次的帧大小。
				double p_VdoInptDvcFrmAspectRatio; //存放本次视频输入设备帧的宽高比。
				int p_VdoInptDvcFrmCropX; //存放本次视频输入设备帧裁剪区域左上角的横坐标，单位像素。
				int p_VdoInptDvcFrmCropY; //存放本次视频输入设备帧裁剪区域左上角的纵坐标，单位像素。
				int p_VdoInptDvcFrmCropWidth; //存放本次视频输入设备帧裁剪区域的宽度，单位像素。
				int p_VdoInptDvcFrmCropHeight; //存放本次视频输入设备帧裁剪区域的高度，单位像素。
				int p_IsSetSelCur; //存放是否设置选择的为本次的，为0表示不设置，为非0表示要设置。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：目标视频输入的帧大小：宽度：" + p_TgtVdoInptFrmWidth + "，高度：" + p_TgtVdoInptFrmHeight + "。" );
				for( int p_TmpInt = 0; p_TmpInt < p_SupportedPrvwSizesListPt.size(); p_TmpInt++ )
				{
					p_CameraSizePt = p_SupportedPrvwSizesListPt.get( p_TmpInt );
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的帧大小：宽度：" + p_CameraSizePt.width + "，高度：" + p_CameraSizePt.height + "。" );

					//设置本次视频输入设备帧的宽高比、裁剪宽度、裁剪高度、裁剪区域左上角的坐标。
					p_VdoInptDvcFrmAspectRatio = ( double )p_CameraSizePt.width / ( double )p_CameraSizePt.height;
					if( p_VdoInptDvcFrmAspectRatio >= p_TgtVdoInptFrmAspectRatio ) //如果本次视频输入设备帧的宽高比目标的大，就表示需要裁剪宽度。
					{
						p_VdoInptDvcFrmCropWidth = ( int )( ( double )p_CameraSizePt.height * p_TgtVdoInptFrmAspectRatio ); //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
						p_VdoInptDvcFrmCropWidth -= p_VdoInptDvcFrmCropWidth % 2;
						p_VdoInptDvcFrmCropHeight = p_CameraSizePt.height; //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。

						p_VdoInptDvcFrmCropX = ( p_CameraSizePt.width - p_VdoInptDvcFrmCropWidth ) / 2; //设置本次视频输入设备帧裁剪区域左上角的横坐标，使裁剪区域居中。
						p_VdoInptDvcFrmCropX -= p_VdoInptDvcFrmCropX % 2;
						p_VdoInptDvcFrmCropY = 0; //设置本次视频输入设备帧裁剪区域左上角的纵坐标。
					}
					else //如果本次视频输入设备帧的宽高比目标的小，就表示需要裁剪高度。
					{
						p_VdoInptDvcFrmCropWidth = p_CameraSizePt.width; //设置本次视频输入设备帧裁剪区域左上角的宽度，使裁剪区域居中。
						p_VdoInptDvcFrmCropHeight = ( int )( ( double )p_CameraSizePt.width / p_TgtVdoInptFrmAspectRatio ); //设置本次视频输入设备帧裁剪区域左上角的高度，使裁剪区域居中。
						p_VdoInptDvcFrmCropHeight -= p_VdoInptDvcFrmCropHeight % 2;

						p_VdoInptDvcFrmCropX = 0; //设置本次视频输入设备帧裁剪区域左上角的横坐标。
						p_VdoInptDvcFrmCropY = ( p_CameraSizePt.height - p_VdoInptDvcFrmCropHeight ) / 2; //设置本次视频输入设备帧裁剪区域左上角的纵坐标，使裁剪区域居中。
						p_VdoInptDvcFrmCropY -= p_VdoInptDvcFrmCropY % 2;
					}

					//如果选择的帧裁剪区域不满足目标的（包括选择的帧裁剪区域为0），则只要本次的帧裁剪区域比选择的大，就设置选择的为本次的。
					//如果选择的帧裁剪区域满足目标的，就只要本次的帧裁剪区域满足目标的，且本次的帧裁剪区域比选择的小、或本次的帧裁剪区域相同但裁剪量比选择的小，就设置选择的为本次的。
					p_IsSetSelCur = 0;
					if( ( m_VdoInptDvcFrmCropWidth < p_TgtVdoInptFrmWidth ) && ( m_VdoInptDvcFrmCropHeight < p_TgtVdoInptFrmHeight ) )
					{
						if( ( p_VdoInptDvcFrmCropWidth > m_VdoInptDvcFrmCropWidth ) && ( p_VdoInptDvcFrmCropHeight > m_VdoInptDvcFrmCropHeight ) )
						{
							p_IsSetSelCur = 1;
						}
					}
					else
					{
						if(
							( ( p_VdoInptDvcFrmCropWidth >= p_TgtVdoInptFrmWidth ) && ( p_VdoInptDvcFrmCropHeight >= p_TgtVdoInptFrmHeight ) )
							&&
							(
								( ( p_VdoInptDvcFrmCropWidth < m_VdoInptDvcFrmCropWidth ) || ( p_VdoInptDvcFrmCropHeight < m_VdoInptDvcFrmCropHeight ) )
								||
								( ( p_VdoInptDvcFrmCropWidth == m_VdoInptDvcFrmCropWidth ) && ( p_VdoInptDvcFrmCropHeight == m_VdoInptDvcFrmCropHeight ) && ( p_VdoInptDvcFrmCropX + p_VdoInptDvcFrmCropY < m_VdoInptDvcFrmCropX + m_VdoInptDvcFrmCropY ) )
							)
						)
						{
							p_IsSetSelCur = 1;
						}
					}
					if( p_IsSetSelCur != 0 ) //如果要设置选择的为本次的。
					{
						m_VdoInptDvcFrmWidth = p_CameraSizePt.width;
						m_VdoInptDvcFrmHeight = p_CameraSizePt.height;

						m_VdoInptDvcFrmCropX = p_VdoInptDvcFrmCropX;
						m_VdoInptDvcFrmCropY = p_VdoInptDvcFrmCropY;
						m_VdoInptDvcFrmCropWidth = p_VdoInptDvcFrmCropWidth;
						m_VdoInptDvcFrmCropHeight = p_VdoInptDvcFrmCropHeight;
					}
				}
				p_CameraParaPt.setPreviewSize( m_VdoInptDvcFrmWidth, m_VdoInptDvcFrmHeight ); //设置预览帧的宽度为设置的高度，预览帧的高度为设置的宽度，因为预览帧处理的时候要旋转。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：选择视频输入设备的帧大小：宽：" + m_VdoInptDvcFrmWidth + "，高：" + m_VdoInptDvcFrmHeight + "。" );

				//判断视频输入设备帧是否裁剪。
				if( ( m_VdoInptDvcFrmWidth > m_VdoInptDvcFrmCropWidth ) || //如果视频输入设备帧的宽度比裁剪宽度大，就表示需要裁剪宽度。
					( m_VdoInptDvcFrmHeight > m_VdoInptDvcFrmCropHeight ) ) //如果视频输入设备帧的高度比裁剪高度大，就表示需要裁剪高度。
				{
					m_VdoInptDvcFrmIsCrop = 1; //设置视频输入设备帧要裁剪。
				}
				else //如果视频输入设备帧的宽度和高度与裁剪宽度和高度一致，就表示不需要裁剪。
				{
					m_VdoInptDvcFrmIsCrop = 0; //设置视频输入设备帧不裁剪。
				}
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否裁剪：" + m_VdoInptDvcFrmIsCrop + "，左上角的横坐标：" + m_VdoInptDvcFrmCropX + "，纵坐标：" + m_VdoInptDvcFrmCropY + "，裁剪区域的宽度：" + m_VdoInptDvcFrmCropWidth + "，高度：" + m_VdoInptDvcFrmCropHeight + "。" );

				//设置视频输入设备帧的旋转。
				if( m_UseWhatVdoInptDvc == 0 ) //如果要使用前置摄像头。
				{
					m_VdoInptDvcFrmRotate = ( 270 + m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
				}
				else //如果要使用后置摄像头。
				{
					m_VdoInptDvcFrmRotate = ( 450 - m_ScreenRotate ) % 360; //设置视频输入帧的旋转角度。
				}
				if( ( m_VdoInptDvcFrmRotate == 0 ) || ( m_VdoInptDvcFrmRotate == 180 ) ) //如果旋转后为横屏。
				{
					m_VdoInptDvcFrmRotateWidth = m_VdoInptDvcFrmCropWidth; //设置视频输入设备帧旋转后的宽度。
					m_VdoInptDvcFrmRotateHeight = m_VdoInptDvcFrmCropHeight; //设置视频输入设备帧旋转后的高度。
				}
				else //如果旋转后为竖屏。
				{
					m_VdoInptDvcFrmRotateWidth = m_VdoInptDvcFrmCropHeight; //设置视频输入设备帧旋转后的宽度。
					m_VdoInptDvcFrmRotateHeight = m_VdoInptDvcFrmCropWidth; //设置视频输入设备帧旋转后的高度。
				}
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧旋转后的宽度：" + m_VdoInptDvcFrmRotateWidth + "，旋转后的高度：" + m_VdoInptDvcFrmRotateHeight + "。" );

				//判断视频输入设备帧是否缩放。
				if( ( m_VdoInptDvcFrmCropWidth != p_TgtVdoInptFrmWidth ) || ( m_VdoInptDvcFrmCropHeight != p_TgtVdoInptFrmHeight ) )
				{
					m_VdoInptDvcFrmIsScale = 1; //设置视频输入设备帧要缩放。
				}
				else
				{
					m_VdoInptDvcFrmIsScale = 0; //设置视频输入设备帧不缩放。
				}
				if( ( m_VdoInptDvcFrmRotate == 0 ) || ( m_VdoInptDvcFrmRotate == 180 ) ) //如果旋转后为横屏。
				{
					m_VdoInptDvcFrmScaleWidth = p_TgtVdoInptFrmWidth; //设置视频输入设备帧缩放后的宽度。
					m_VdoInptDvcFrmScaleHeight = p_TgtVdoInptFrmHeight; //设置视频输入设备帧缩放后的高度。
				}
				else //如果旋转后为竖屏。
				{
					m_VdoInptDvcFrmScaleWidth = p_TgtVdoInptFrmHeight; //设置视频输入设备帧缩放后的宽度。
					m_VdoInptDvcFrmScaleHeight = p_TgtVdoInptFrmWidth; //设置视频输入设备帧缩放后的高度。
				}
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备帧是否缩放：" + m_VdoInptDvcFrmIsScale + "，缩放后的宽度：" + m_VdoInptDvcFrmScaleWidth + "，缩放后的高度：" + m_VdoInptDvcFrmScaleHeight + "。" );

				//设置视频输入设备的对焦模式。
				List<String> p_FocusModesListPt = p_CameraParaPt.getSupportedFocusModes();
				String p_PrvwFocusModePt = "";
				for( int p_TmpInt = 0; p_TmpInt < p_FocusModesListPt.size(); p_TmpInt++ )
				{
					switch( p_FocusModesListPt.get( p_TmpInt ) )
					{
						case Camera.Parameters.FOCUS_MODE_AUTO: //自动对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式启动焦点。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_AUTO，自动对焦模式。" );
							break;
						case Camera.Parameters.FOCUS_MODE_MACRO: //微距（特写）对焦模式。应用程序应调用autoFocus（AutoFocusCallback）以此模式开始聚焦。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_MACRO，微距（特写）对焦模式。" );
							break;
						case Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO: //用于视频的连续自动对焦模式。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_VIDEO，用于视频的连续自动对焦模式。" );
							p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
							break;
						case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE: //用于拍照的连续自动对焦模式，比视频的连续自动对焦模式对焦速度更快。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_CONTINUOUS_PICTURE，用于拍照的连续自动对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
							break;
						case Camera.Parameters.FOCUS_MODE_EDOF: //扩展景深（EDOF）对焦模式，对焦以数字方式连续进行。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_EDOF，扩展景深（EDOF）对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_EDOF;
							break;
						case Camera.Parameters.FOCUS_MODE_FIXED: //固定焦点对焦模式。如果焦点无法调节，则相机始终处于此模式。如果相机具有自动对焦，则此模式可以固定焦点，通常处于超焦距。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_FIXED，固定焦点对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_FIXED;
							break;
						case Camera.Parameters.FOCUS_MODE_INFINITY: //无限远焦点对焦模式。在这种模式下，应用程序不应调用autoFocus（AutoFocusCallback）。
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：视频输入设备支持的对焦模式：FOCUS_MODE_INFINITY，无限远焦点对焦模式。" );
							if( !p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_EDOF ) &&
									!p_PrvwFocusModePt.equals( Camera.Parameters.FOCUS_MODE_FIXED ) )
								p_PrvwFocusModePt = Camera.Parameters.FOCUS_MODE_INFINITY;
							break;
					}
				}
				p_CameraParaPt.setFocusMode( p_PrvwFocusModePt ); //设置对焦模式。

				try
				{
					m_VdoInptDvcPt.setParameters( p_CameraParaPt ); //设置参数到视频输入设备。
				}
				catch( RuntimeException e )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage() );
					if( m_MediaPocsThrdPt.m_IsShowToast != 0 ) m_MediaPocsThrdPt.m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_MediaPocsThrdPt.m_ShowToastActivityPt, "媒体处理线程：初始化视频输入设备失败。原因：设置参数到视频输入设备失败。原因：" + e.getMessage(), Toast.LENGTH_LONG ).show(); } } );
					break Out;
				}

				m_VdoInptPrvwSurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS ); //设置视频输入预览Surface的类型。老机型上必须要用。
				m_VdoInptPrvwSurfaceClbkPt = new SurfaceHolder.Callback() //创建视频输入预览Surface的回调函数。
				{
					@Override public void surfaceCreated( SurfaceHolder holder )
					{
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoInptPrvwSurface Created" );
						m_MediaPocsThrdPt.SetVdoInptUseDvc( m_UseWhatVdoInptDvc, -1, -1 ); //重启视频输入。
					}

					@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
					{
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoInptPrvwSurface Changed" );
					}

					@Override public void surfaceDestroyed( SurfaceHolder holder )
					{
						Log.i( MediaPocsThrd.m_CurClsNameStrPt, "VdoInptPrvwSurface Destroyed" );
					}
				};
				m_VdoInptPrvwSurfaceViewPt.getHolder().addCallback( m_VdoInptPrvwSurfaceClbkPt ); //设置视频输入预览Surface的回调函数。
				try
				{
					m_VdoInptDvcPt.setPreviewDisplay( m_VdoInptPrvwSurfaceViewPt.getHolder() ); //设置视频输入预览SurfaceView。
				}
				catch( Exception ignored )
				{
				}
				if( m_ScreenRotate == 0 || m_ScreenRotate == 180 ) //如果屏幕为竖屏。
				{
					m_VdoInptPrvwSurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoInptDvcFrmHeight / m_VdoInptDvcFrmWidth ); //设置视频输入预览SurfaceView的宽高比。
				}
				else //如果屏幕为横屏。
				{
					m_VdoInptPrvwSurfaceViewPt.setWidthToHeightRatio( ( float )m_VdoInptDvcFrmWidth / m_VdoInptDvcFrmHeight ); //设置视频输入预览SurfaceView的宽高比。
				}
				m_VdoInptDvcPt.setDisplayOrientation( ( 450 - m_ScreenRotate ) % 360 ); //调整相机拍到的图像旋转，不然竖着拿手机，图像是横着的。

				//设置视频输入预览回调函数缓冲区的指针。
				m_VdoInptPrvwClbkBufPtPt = new byte[ m_MaxSmplRate ][ m_VdoInptDvcFrmWidth * m_VdoInptDvcFrmHeight * 3 / 2 ];
				for( int p_TmpInt = 0; p_TmpInt < m_MaxSmplRate; p_TmpInt++ )
					m_VdoInptDvcPt.addCallbackBuffer( m_VdoInptPrvwClbkBufPtPt[p_TmpInt] );

				//设置视频输入预览回调函数。
				m_VodInptPrvwClbkPt = new VodInptPrvwClbk();
				m_VdoInptDvcPt.setPreviewCallbackWithBuffer( m_VodInptPrvwClbkPt );

				//设置视频输入设备开始预览。
				try
				{
					m_VdoInptDvcPt.startPreview();
				}
				catch( RuntimeException e )
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：设置视频输入设备开始预览失败。原因：" + e.getMessage() );
					break Out;
				}

				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入设备成功。" );
			}

			//初始化编码器。
			switch( m_UseWhatEncd )
			{
				case 0: //如果要使用YU12原始数据。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化YU12原始数据成功。" );
					break;
				}
				case 1: //如果要使用OpenH264编码器。
				{
					m_OpenH264EncdPt = new OpenH264Encd();
					if( m_OpenH264EncdPt.Init( m_VdoInptDvcFrmScaleWidth, m_VdoInptDvcFrmScaleHeight, m_OpenH264EncdVdoType, m_OpenH264EncdEncdBitrate, m_OpenH264EncdBitrateCtrlMode, m_MaxSmplRate, m_OpenH264EncdIDRFrmIntvl, m_OpenH264EncdCmplxt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化OpenH264编码器成功。" );
					}
					else
					{
						m_OpenH264EncdPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化OpenH264编码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
				case 2: //如果要使用系统自带H264编码器。
				{
					m_SystemH264EncdPt = new SystemH264Encd();
					if( m_SystemH264EncdPt.Init( m_VdoInptDvcFrmScaleWidth, m_VdoInptDvcFrmScaleHeight, m_SystemH264EncdEncdBitrate, m_SystemH264EncdBitrateCtrlMode, m_MaxSmplRate, m_SystemH264EncdIDRFrmIntvlTimeSec, m_SystemH264EncdCmplxt, m_MediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化系统自带H264编码器成功。" );
					}
					else
					{
						m_SystemH264EncdPt = null;
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化系统自带H264编码器失败。原因：" + m_MediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() );
						break Out;
					}
					break;
				}
			}

			//初始化视频输入线程的临时变量。
			{
				m_IsInitVdoInptThrdTmpVar = 1; //设置已初始化视频输入线程的临时变量。
				m_LastVdoInptFrmTimeMsec = 0; //初始化上一个视频输入帧的时间。
				m_VdoInptFrmTimeStepMsec = 1000 / m_MaxSmplRate; //初始化视频输入帧的时间步进。
				m_VdoInptFrmPt = null; //初始化视频输入帧的指针。
				if( m_FrmWidth * m_FrmHeight >= m_VdoInptDvcFrmWidth * m_VdoInptDvcFrmHeight ) //如果视频输入帧的大小大于等于视频输入设备帧的大小。
				{
					m_VdoInptRsltFrmSz = m_FrmWidth * m_FrmHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
				}
				else //如果视频输入帧的大小小于视频输入设备帧的大小。
				{
					m_VdoInptRsltFrmSz = m_VdoInptDvcFrmWidth * m_VdoInptDvcFrmHeight * 3 / 2; //初始化视频输入结果帧的内存大小。
				}
				m_VdoInptRsltFrmPt = new byte[( int )m_VdoInptRsltFrmSz]; //初始化视频输入结果帧的指针。
				m_VdoInptTmpFrmPt = new byte[( int )m_VdoInptRsltFrmSz]; //初始化视频输入临时帧的指针。
				m_VdoInptSwapFrmPt = null; //初始化视频输入交换帧的指针。
				m_VdoInptRsltFrmLenPt = new HTLong(); //初始化视频输入结果帧的长度。
				m_VdoInptFrmElmPt = null; //初始化视频输入帧元素的指针。
				m_VdoInptFrmLnkLstElmTotal = 0; //初始化视频输入帧链表的元数总数。
				m_LastTimeMsec = 0; //初始化上次时间的毫秒数。
				m_NowTimeMsec = 0; //初始化本次时间的毫秒数。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入线程的临时变量成功。" );
			}

			//初始化NV21格式视频输入帧链表。
			m_NV21VdoInptFrmLnkLstPt = new LinkedList< byte[] >();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化NV21格式视频输入帧链表成功。" );

			//初始化视频输入帧链表。
			m_VdoInptFrmLnkLstPt = new LinkedList< VdoInptFrmElm >();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入帧链表成功。" );

			//初始化视频输入空闲帧链表。
			m_VdoInptIdleFrmLnkLstPt = new LinkedList< VdoInptFrmElm >();
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入空闲帧链表成功。" );

			//初始化视频输入线程。
			{
				m_VdoInptThrdExitFlag = 0; //设置视频输入线程退出标记为0表示保持运行。
				m_VdoInptThrdPt = new VdoInptThrd();
				m_VdoInptThrdPt.start(); //启动视频输入线程。
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：创建视频输入线程成功。" );
			}

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
			{
				p_NowMsec = System.currentTimeMillis(); //记录初始化结束的时间。
				Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：初始化视频输入耗时 " + ( p_NowMsec - p_LastMsec ) + " 毫秒。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//销毁视频输入。
	void Dstoy()
	{
		//销毁视频输入线程。
		if( m_VdoInptThrdPt != null )
		{
			m_VdoInptThrdExitFlag = 1; //请求视频输入线程退出。
			try
			{
				m_VdoInptThrdPt.join(); //等待视频输入线程退出。
			}
			catch( InterruptedException ignored )
			{
			}
			m_VdoInptThrdPt = null;
			m_VdoInptThrdExitFlag = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程成功。" );
		}

		//销毁视频输入线程的临时变量。
		if( m_IsInitVdoInptThrdTmpVar != 0 )
		{
			m_IsInitVdoInptThrdTmpVar = 0; //设置未初始化视频输入线程的临时变量。
			m_LastVdoInptFrmTimeMsec = 0; //销毁上一个视频输入帧的时间。
			m_VdoInptFrmTimeStepMsec = 0; //销毁视频输入帧的时间步进。
			m_VdoInptFrmPt = null; //销毁视频输入帧的指针。
			m_VdoInptRsltFrmPt = null; //初始化视频输入结果帧的指针。
			m_VdoInptTmpFrmPt = null; //初始化视频输入临时帧的指针。
			m_VdoInptSwapFrmPt = null; //初始化视频输入交换帧的指针。
			m_VdoInptRsltFrmLenPt = null; //初始化视频输入结果帧的长度。
			m_VdoInptRsltFrmSz = 0; //销毁视频输入结果帧的内存大小。
			m_VdoInptFrmElmPt = null; //销毁视频输入帧元素的指针。
			m_VdoInptFrmLnkLstElmTotal = 0; //销毁视频输入帧链表的元数总数。
			m_LastTimeMsec = 0; //销毁上次时间的毫秒数。
			m_NowTimeMsec = 0; //销毁本次时间的毫秒数。
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输入线程的临时变量成功。" );
		}

		//销毁视频输入设备。
		if( m_VdoInptDvcPt != null )
		{
			m_VdoInptDvcPt.setPreviewCallback( null ); //设置预览回调函数为空，防止出现java.lang.RuntimeException: Method called after release()异常。
			m_VdoInptDvcPt.stopPreview(); //停止预览。
			m_VdoInptDvcPt.release(); //销毁摄像头。
			m_VdoInptDvcPt = null;
			m_VdoInptPrvwSurfaceViewPt.getHolder().removeCallback( m_VdoInptPrvwSurfaceClbkPt );
			m_VdoInptPrvwSurfaceClbkPt = null;
			MediaPocsThrd.m_MainActivityPt.runOnUiThread( new Runnable() { public void run() { m_VdoInptPrvwSurfaceViewPt.setVisibility( View.GONE ); m_VdoInptPrvwSurfaceViewPt.setVisibility( View.VISIBLE ); } } ); //重建视频输入预览Surface视图。
			m_VdoInptPrvwClbkBufPtPt = null;
			m_VodInptPrvwClbkPt = null;
			m_VdoInptDvcFrmRotate = 0;
			m_VdoInptDvcFrmWidth = 0;
			m_VdoInptDvcFrmHeight = 0;
			m_VdoInptDvcFrmIsCrop = 0;
			m_VdoInptDvcFrmCropX = 0;
			m_VdoInptDvcFrmCropY = 0;
			m_VdoInptDvcFrmCropWidth = 0;
			m_VdoInptDvcFrmCropHeight = 0;
			m_VdoInptDvcFrmIsScale = 0;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输入设备成功。" );
		}

		//销毁视频输入空闲帧链表。
		if( m_VdoInptIdleFrmLnkLstPt != null )
		{
			m_VdoInptIdleFrmLnkLstPt.clear();
			m_VdoInptIdleFrmLnkLstPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输入空闲帧链表成功。" );
		}

		//销毁视频输入帧链表。
		if( m_VdoInptFrmLnkLstPt != null )
		{
			m_VdoInptFrmLnkLstPt.clear();
			m_VdoInptFrmLnkLstPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁视频输入帧链表成功。" );
		}

		//销毁NV21格式视频输入帧链表。
		if( m_NV21VdoInptFrmLnkLstPt != null )
		{
			m_NV21VdoInptFrmLnkLstPt.clear();
			m_NV21VdoInptFrmLnkLstPt = null;
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁NV21格式视频输入帧链表成功。" );
		}

		//销毁编码器。
		switch( m_UseWhatEncd )
		{
			case 0: //如果要使用YU12原始数据。
			{
				if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁YU12原始数据成功。" );
				break;
			}
			case 1: //如果要使用OpenH264编码器。
			{
				if( m_OpenH264EncdPt != null )
				{
					if( m_OpenH264EncdPt.Dstoy( null ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁OpenH264编码器失败。" );
					}
					m_OpenH264EncdPt = null;
				}
				break;
			}
			case 2: //如果要使用系统自带H264编码器。
			{
				if( m_SystemH264EncdPt != null )
				{
					if( m_SystemH264EncdPt.Dstoy( null ) == 0 )
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器成功。" );
					}
					else
					{
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "媒体处理线程：销毁系统自带H264编码器失败。" );
					}
					m_SystemH264EncdPt = null;
				}
				break;
			}
		}
	}

	//视频输入预览回调类。
	public class VodInptPrvwClbk implements Camera.PreviewCallback
	{
		//读取一个视频输入帧的预览回调函数，本函数是在主线程中运行的。
		@Override public void onPreviewFrame( byte[] data, Camera camera )
		{
			if( m_NV21VdoInptFrmLnkLstPt != null ) //如果NV21格式视频输入帧链表已初始化。
			{
				//追加本次视频输入帧到视频输入帧链表。
				synchronized( m_NV21VdoInptFrmLnkLstPt )
				{
					m_NV21VdoInptFrmLnkLstPt.addLast( data );
				}
			}
			else //如果NV21格式视频输入帧链表未初始化，因为视频输入设备是在其初始化前就初始化了。
			{
				m_VdoInptDvcPt.addCallbackBuffer( m_VdoInptFrmPt );
			}
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：读取一个视频输入帧。" );
		}
	}

	//视频输入线程类。
	public class VdoInptThrd extends Thread
	{
		public void run()
		{
			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：开始准备视频输入。" );

			//视频输入循环开始。
			while( true )
			{
				if( m_NV21VdoInptFrmLnkLstPt.size() > 0 )//如果NV21格式视频输入帧链表中有帧了。
				{
					//开始处理视频输入帧。
					OutPocsVdoInptFrm:
					{
						//从NV21格式视频输入帧链表中取出第一个视频输入帧。
						m_VdoInptFrmLnkLstElmTotal = m_NV21VdoInptFrmLnkLstPt.size();
						synchronized( m_NV21VdoInptFrmLnkLstPt )
						{
							m_VdoInptFrmPt = m_NV21VdoInptFrmLnkLstPt.getFirst();
							m_NV21VdoInptFrmLnkLstPt.removeFirst();
						}
						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：从NV21格式视频输入帧链表中取出第一个NV21格式视频输入帧，NV21格式视频输入帧链表元素个数：" + m_VdoInptFrmLnkLstElmTotal + "。" );

						//丢弃采样频率过快的视频输入帧。
						m_LastTimeMsec = System.currentTimeMillis();
						if( m_LastVdoInptFrmTimeMsec != 0 ) //如果已经设置过上一个视频输入帧的时间。
						{
							if( m_LastTimeMsec - m_LastVdoInptFrmTimeMsec >= m_VdoInptFrmTimeStepMsec )
							{
								m_LastVdoInptFrmTimeMsec += m_VdoInptFrmTimeStepMsec;
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：采样频率过快，本次视频输入设备帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}
						else //如果没有设置过上一个视频输入帧的时间。
						{
							m_LastVdoInptFrmTimeMsec = m_LastTimeMsec;
						}

						//裁剪视频输入设备帧。
						if( m_VdoInptDvcFrmIsCrop != 0 )
						{
							if( LibYUV.PictrCrop(
									m_VdoInptFrmPt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VdoInptDvcFrmWidth, m_VdoInptDvcFrmHeight,
									m_VdoInptDvcFrmCropX, m_VdoInptDvcFrmCropY, m_VdoInptDvcFrmCropWidth, m_VdoInptDvcFrmCropHeight,
									m_VdoInptTmpFrmPt, m_VdoInptRsltFrmSz, m_VdoInptRsltFrmLenPt, null, null,
									null ) == 0 )
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧成功。" );
								m_VdoInptSwapFrmPt = m_VdoInptTmpFrmPt;
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：裁剪视频输入设备帧失败，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}
						else
						{
							m_VdoInptSwapFrmPt = m_VdoInptFrmPt;
						}

						//NV21格式视频输入帧旋转为YU12格式视频输入帧。
						if( LibYUV.PictrRotate(
								m_VdoInptSwapFrmPt, LibYUV.PICTR_FMT_BT601F8_NV21, m_VdoInptDvcFrmCropWidth, m_VdoInptDvcFrmCropHeight,
								m_VdoInptDvcFrmRotate,
								m_VdoInptRsltFrmPt, m_VdoInptRsltFrmPt.length, null, null,
								null ) == 0 )
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧成功。" );
						}
						else
						{
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：NV21格式视频输入帧旋转为YU12格式视频输入帧失败，本次视频输入帧丢弃。" );
							break OutPocsVdoInptFrm;
						}

						//缩放视频输入设备帧。
						if( m_VdoInptDvcFrmIsScale != 0 )
						{
							if( LibYUV.PictrScale(
									m_VdoInptRsltFrmPt, LibYUV.PICTR_FMT_BT601F8_YU12_I420, m_VdoInptDvcFrmRotateWidth, m_VdoInptDvcFrmRotateHeight,
									3,
									m_VdoInptTmpFrmPt, m_VdoInptRsltFrmSz, m_VdoInptRsltFrmLenPt, m_VdoInptDvcFrmScaleWidth, m_VdoInptDvcFrmScaleHeight,
									null ) == 0 )
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧成功。" );
								m_VdoInptSwapFrmPt = m_VdoInptRsltFrmPt; m_VdoInptRsltFrmPt = m_VdoInptTmpFrmPt; m_VdoInptTmpFrmPt = m_VdoInptSwapFrmPt; //交换视频输入结果帧和视频输入临时帧。
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：缩放视频输入设备帧失败，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}

						//获取一个视频输入空闲帧。
						if( ( m_VdoInptFrmLnkLstElmTotal = m_VdoInptIdleFrmLnkLstPt.size() ) > 0 ) //如果视频输入空闲帧链表中有视频输入空闲帧。
						{
							//从视频输入空闲帧链表中取出第一个视频输入空闲帧。
							synchronized( m_VdoInptIdleFrmLnkLstPt )
							{
								m_VdoInptFrmElmPt = m_VdoInptIdleFrmLnkLstPt.getFirst();
								m_VdoInptIdleFrmLnkLstPt.removeFirst();
							}
							if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：从视频输入空闲帧链表中取出第一个视频输入空闲帧，视频输入空闲帧链表元素个数：" + m_VdoInptFrmLnkLstElmTotal + "。" );
						}
						else //如果视频输入空闲帧链表中没有视频输入空闲帧。
						{
							if( ( m_VdoInptFrmLnkLstElmTotal = m_VdoInptFrmLnkLstPt.size() ) <= 20 )
							{
								m_VdoInptFrmElmPt = new VdoInptFrmElm(); //创建一个视频输入空闲帧。
								m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt = new byte[ m_VdoInptDvcFrmScaleWidth * m_VdoInptDvcFrmScaleHeight * 3 / 2 ];
								m_VdoInptFrmElmPt.m_YU12VdoInptFrmWidthPt = new HTInt();
								m_VdoInptFrmElmPt.m_YU12VdoInptFrmHeightPt = new HTInt();
								m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt = ( m_UseWhatEncd != 0 ) ? new byte[ m_VdoInptDvcFrmScaleWidth * m_VdoInptDvcFrmScaleHeight * 3 / 2 ] : null;
								m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt = ( m_UseWhatEncd != 0 ) ? new HTLong( 0 ) : null;
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：视频输入空闲帧链表中没有视频输入空闲帧，创建一个视频输入空闲帧。" );
							}
							else
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：视频输入帧链表中视频输入帧数量为" + m_VdoInptFrmLnkLstElmTotal + "已经超过上限20，不再创建一个视频输入空闲帧，本次视频输入帧丢弃。" );
								break OutPocsVdoInptFrm;
							}
						}

						//将视频结果帧复制到视频输入帧元素。
						System.arraycopy( m_VdoInptRsltFrmPt, 0, m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, 0, m_VdoInptDvcFrmScaleWidth * m_VdoInptDvcFrmScaleHeight * 3 / 2 );
						m_VdoInptFrmElmPt.m_YU12VdoInptFrmWidthPt.m_Val = m_VdoInptDvcFrmScaleWidth;
						m_VdoInptFrmElmPt.m_YU12VdoInptFrmHeightPt.m_Val = m_VdoInptDvcFrmScaleHeight;

						//判断视频输入是否黑屏。在视频输入处理完后再设置黑屏，这样可以保证视频输入处理器的连续性。
						if( m_VdoInptIsBlack != 0 )
						{
							int p_TmpLen = m_VdoInptDvcFrmScaleWidth * m_VdoInptDvcFrmScaleHeight;
							Arrays.fill( m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, 0, p_TmpLen, ( byte ) 0 );
							Arrays.fill( m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, p_TmpLen, m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt.length, ( byte ) 128 );
						}

						//使用编码器。
						switch( m_UseWhatEncd )
						{
							case 0: //如果要使用YU12原始数据。
							{
								if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用YU12原始数据。" );
								break;
							}
							case 1: //如果要使用OpenH264编码器。
							{
								if( m_OpenH264EncdPt.Pocs(
										m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, m_VdoInptDvcFrmScaleWidth, m_VdoInptDvcFrmScaleHeight, m_LastTimeMsec,
										m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt, m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt.length, m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt,
										null ) == 0 )
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器成功。H264格式视频输入帧的长度：" + m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt.m_Val + "，时间戳：" + m_LastTimeMsec + "，类型：" + ( m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt[4] & 0xff ) + "。" );
								}
								else
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用OpenH264编码器失败，本次视频输入帧丢弃。" );
									break OutPocsVdoInptFrm;
								}
								break;
							}
							case 2: //如果要使用系统自带H264编码器。
							{
								if( m_SystemH264EncdPt.Pocs(
										m_VdoInptFrmElmPt.m_YU12VdoInptFrmPt, m_LastTimeMsec,
										m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt, ( long )m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt.length, m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt,
										1000 / m_MaxSmplRate * 2 / 3, null ) == 0 )
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器成功。H264格式视频输入帧的长度：" + m_VdoInptFrmElmPt.m_EncdVdoInptFrmLenPt.m_Val + "，时间戳：" + m_LastTimeMsec + "，类型：" + ( m_VdoInptFrmElmPt.m_EncdVdoInptFrmPt[4] & 0xff ) + "。" );
								}
								else
								{
									if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：使用系统自带H264编码器失败，本次视频输入帧丢弃。" );
									break OutPocsVdoInptFrm;
								}
								break;
							}
						}

						//追加本次视频输入帧到视频输入帧链表。
						synchronized( m_VdoInptFrmLnkLstPt )
						{
							m_VdoInptFrmLnkLstPt.addLast( m_VdoInptFrmElmPt );
						}
						m_VdoInptFrmElmPt = null;

						if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 )
						{
							m_NowTimeMsec = System.currentTimeMillis();
							Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：本次视频输入帧处理完毕，耗时 " + ( m_NowTimeMsec - m_LastTimeMsec ) + " 毫秒。" );
						}
					} //处理视频输入帧完毕。

					if( m_VdoInptFrmElmPt != null ) //如果获取的视频输入空闲帧没有追加到视频输入帧链表。
					{
						m_VdoInptIdleFrmLnkLstPt.addLast( m_VdoInptFrmElmPt );
						m_VdoInptFrmElmPt = null;
					}

					//追加本次NV21格式视频输入帧到视频输入设备。
					m_VdoInptDvcPt.addCallbackBuffer( m_VdoInptFrmPt );
				}

				if( m_VdoInptThrdExitFlag == 1 ) //如果退出标记为请求退出。
				{
					if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：接收到退出请求，开始准备退出。" );
					break;
				}

				SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
			} //视频输入循环结束。

			if( m_MediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( MediaPocsThrd.m_CurClsNameStrPt, "视频输入线程：本线程已退出。" );
		}
	}
}