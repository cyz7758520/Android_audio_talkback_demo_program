package HeavenTao.Vdo;

import android.graphics.Bitmap;
import android.view.Surface;

import HeavenTao.Data.*;

//图片处理。
public class LibYUV
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "LibYUV" ); //加载libLibYUV.so。
	}

	//构造函数。
	public LibYUV()
	{

	}

	//析构函数。
	protected void finalize()
	{

	}

	//图片格式。
	public static final int PICTR_FMT_BT601F8_NV12      = 0;   //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','UVUVUVUV……'。
	public static final int PICTR_FMT_BT601F8_NV21      = 1;   //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','VUVUVUVU……'。
	public static final int PICTR_FMT_BT601F8_Yu12_I420 = 2;   //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','UUUU……','VVVV……'。
	public static final int PICTR_FMT_BT601F8_YV12      = 3;   //BT.601标准，Full Range，8位，内存排列：'YYYYYYYY……','VVVV……','UUUU……'。
	public static final int PICTR_FMT_SRGBF8_RGB555     = 4;   //sRGB标准，Full Range，8位，内存排列：'R','G','B'。
	public static final int PICTR_FMT_SRGBF8_BGR555     = 5;   //sRGB标准，Full Range，8位，内存排列：'B','G','R'。
	public static final int PICTR_FMT_SRGBF8_RGB565     = 6;   //sRGB标准，Full Range，8位，内存排列：'R','G','B'。
	public static final int PICTR_FMT_SRGBF8_BGR565     = 7;   //sRGB标准，Full Range，8位，内存排列：'B','G','R'。
	public static final int PICTR_FMT_SRGBF8_RGB888     = 8;   //sRGB标准，Full Range，8位，内存排列：'R','G','B'。
	public static final int PICTR_FMT_SRGBF8_BGR888     = 9;   //sRGB标准，Full Range，8位，内存排列：'B','G','R'。
	public static final int PICTR_FMT_SRGBF8_ARGB8888   = 10;  //sRGB标准，Full Range，8位，内存排列：'A','R','G','B'。
	public static final int PICTR_FMT_SRGBF8_ABGR8888   = 11;  //sRGB标准，Full Range，8位，内存排列：'A','B','G','R'。
	public static final int PICTR_FMT_SRGBF8_RGBA8888   = 12;  //sRGB标准，Full Range，8位，内存排列：'R','G','B','A'。
	public static final int PICTR_FMT_SRGBF8_BGRA8888   = 13;  //sRGB标准，Full Range，8位，内存排列：'B','G','R','A'。
	public static final int PICTR_FMT_UNKOWN            = 99;  //未知格式。

	//旋转角度。
	public static final int ROTATE_DEGREE_0             = 0;   //0度。
	public static final int ROTATE_DEGREE_90            = 90;  //90度。
	public static final int ROTATE_DEGREE_180           = 180; //180度。
	public static final int ROTATE_DEGREE_270           = 270; //270度。

	public static int PictrCrop( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
								 int CropX, int CropY, int CropWidth, int CropHeight,
								 byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
								 Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrCrop( SrcPictrPt, SrcPictrFmt, SrcPictrWidth,
								SrcPictrHeight,
								CropX, CropY, CropWidth, CropHeight,
								DstPictrPt, DstPictrSz, DstPictrLenPt, DstPictrWidthPt, DstPictrHeightPt,
								( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrRotate( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
								   int RotateDegree,
								   byte DstPictrPt[], long DstPictrSz, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
								   Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrRotate( SrcPictrPt, SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,
								  RotateDegree,
								  DstPictrPt, DstPictrSz, DstPictrWidthPt, DstPictrHeightPt,
								  ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrScale( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
								  int Quality,
								  byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrWidth, int DstPictrHeight,
								  Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrScale( SrcPictrPt, SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,
								 Quality,
								 DstPictrPt, DstPictrSz, DstPictrLenPt, DstPictrWidth, DstPictrHeight,
								 ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrFmtCnvrt( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
									 byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrFmt,
									 Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrFmtCnvrt( SrcPictrPt, SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,
									DstPictrPt, DstPictrSz, DstPictrLenPt, DstPictrFmt,
									( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrFmtCnvrtWithStride( byte SrcPictrPlane1Pt[], long SrcPictrPlane1Start, int SrcPictrPlane1Stride,
											   byte SrcPictrPlane2Pt[], long SrcPictrPlane2Start, int SrcPictrPlane2Stride,
											   byte SrcPictrPlane3Pt[], long SrcPictrPlane3Start, int SrcPictrPlane3Stride,
											   int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,

											   byte DstPictrPlane1Pt[], long DstPictrPlane1Start, long DstPictrPlane1Sz, int DstPictrPlane1Stride, HTLong DstPictrPlane1LenPt,
											   byte DstPictrPlane2Pt[], long DstPictrPlane2Start, long DstPictrPlane2Sz, int DstPictrPlane2Stride, HTLong DstPictrPlane2LenPt,
											   byte DstPictrPlane3Pt[], long DstPictrPlane3Start, long DstPictrPlane3Sz, int DstPictrPlane3Stride, HTLong DstPictrPlane3LenPt,
											   int DstPictrFmt,

											   Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrFmtCnvrtWithStride( SrcPictrPlane1Pt, SrcPictrPlane1Start, SrcPictrPlane1Stride,
											  SrcPictrPlane2Pt, SrcPictrPlane2Start, SrcPictrPlane2Stride,
											  SrcPictrPlane3Pt, SrcPictrPlane3Start, SrcPictrPlane3Stride,
											  SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,

											  DstPictrPlane1Pt, DstPictrPlane1Start, DstPictrPlane1Sz,
											  DstPictrPlane1Stride, DstPictrPlane1LenPt,
											  DstPictrPlane2Pt, DstPictrPlane2Start, DstPictrPlane2Sz,
											  DstPictrPlane2Stride, DstPictrPlane2LenPt,
											  DstPictrPlane3Pt, DstPictrPlane3Start, DstPictrPlane3Sz,
											  DstPictrPlane3Stride, DstPictrPlane3LenPt,
											  DstPictrFmt,

											  ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrDrawToBitmap( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
										 Bitmap DstBitmapPt,
										 Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrDrawToBitmap( SrcPictrPt, SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,
										DstBitmapPt,
										( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	public static int PictrDrawToSurface( byte SrcPictrPt[], long SrcPictrStart, int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
										  Surface DstSurfacePt,
										  Vstr ErrInfoVstrPt )
	{
		return LibYUVPictrDrawToSurface( SrcPictrPt, SrcPictrStart, SrcPictrFmt, SrcPictrWidth, SrcPictrHeight,
										 DstSurfacePt,
										 ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	private native static int LibYUVPictrCrop( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
											   int CropX, int CropY, int CropWidth, int CropHeight,
											   byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
											   long ErrInfoVstrPt );

	private native static int LibYUVPictrRotate( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
												 int RotateDegree,
												 byte DstPictrPt[], long DstPictrSz, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt,
												 long ErrInfoVstrPt );

	private native static int LibYUVPictrScale( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
												int Quality,
												byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrWidth, int DstPictrHeight,
												long ErrInfoVstrPt );

	private native static int LibYUVPictrFmtCnvrt( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
												   byte DstPictrPt[], long DstPictrSz, HTLong DstPictrLenPt, int DstPictrFmt,
												   long ErrInfoVstrPt );

	private native static int LibYUVPictrFmtCnvrtWithStride( byte SrcPictrPlane1Pt[], long SrcPictrPlane1Start, int SrcPictrPlane1Stride,
															 byte SrcPictrPlane2Pt[], long SrcPictrPlane2Start, int SrcPictrPlane2Stride,
															 byte SrcPictrPlane3Pt[], long SrcPictrPlane3Start, int SrcPictrPlane3Stride,
															 int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,

															 byte DstPictrPlane1Pt[], long DstPictrPlane1Start, long DstPictrPlane1Sz, int DstPictrPlane1Stride, HTLong DstPictrPlane1LenPt,
															 byte DstPictrPlane2Pt[], long DstPictrPlane2Start, long DstPictrPlane2Sz, int DstPictrPlane2Stride, HTLong DstPictrPlane2LenPt,
															 byte DstPictrPlane3Pt[], long DstPictrPlane3Start, long DstPictrPlane3Sz, int DstPictrPlane3Stride, HTLong DstPictrPlane3LenPt,
															 int DstPictrFmt,

															 long ErrInfoVstrPt );

	private native static int LibYUVPictrDrawToBitmap( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
													   Bitmap DstBitmapPt,
													   long ErrInfoVstrPt );

	private native static int LibYUVPictrDrawToSurface( byte SrcPictrPt[], long SrcPictrStart, int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight,
														Surface DstSurfacePt,
														long ErrInfoVstrPt );
}