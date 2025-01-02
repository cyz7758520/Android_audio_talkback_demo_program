package HeavenTao.Data;

//动态字符串。
public class Vstr
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
	}

	public long m_VstrPt; //存放动态字符串的指针。

	//构造函数。
	public Vstr()
	{
		m_VstrPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	//创建并初始化动态字符串。
	public int Init( String SrcStrPt )
	{
		if( m_VstrPt == 0 )
		{
			HTLong p_VstrPt = new HTLong();
			if( VstrInit( p_VstrPt, SrcStrPt ) == 0 )
			{
				m_VstrPt = p_VstrPt.m_Val;
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

	//复制字符串到动态字符串。
	public int Cpy( String SrcStrPt )
	{
		return VstrCpy( m_VstrPt, SrcStrPt );
	}

	//插入字符串到动态字符串的指定位置。
	public int Ins( long Pos, String SrcStrPt )
	{
		return VstrIns( m_VstrPt, Pos, SrcStrPt );
	}

	//追加字符串到动态字符串的末尾。
	public int Cat( String SrcStrPt )
	{
		return VstrCat( m_VstrPt, SrcStrPt );
	}

	//清空动态字符串。
	public int SetEmpty()
	{
		return VstrSetEmpty( m_VstrPt );
	}

	//设置动态字符串的大小。
	public int SetSz( long VstrSzChr )
	{
		return VstrSetSz( m_VstrPt, VstrSzChr );
	}

	//获取动态字符串的字符串指针。
	public int GetStrPt( HTLong VstrStrPtPt )
	{
		return VstrGetStrPt( m_VstrPt, VstrStrPtPt );
	}

	//获取动态字符串的字符集。
	public int GetChrSet( HTInt VstrChrSetPt )
	{
		return VstrGetChrSet( m_VstrPt, VstrChrSetPt );
	}

	//获取动态字符串的大小。
	public int GetSz( HTLong VstrSzChrPt )
	{
		return VstrGetSz( m_VstrPt, VstrSzChrPt );
	}

	//获取动态字符串的长度。
	public int GetLen( HTLong VstrLenChrPt )
	{
		return VstrGetLen( m_VstrPt, VstrLenChrPt );
	}

	//获取动态字符串的大小。
	public int UrlParse( HTString PrtclVstrPt, HTString UsernameVstrPt, HTString PasswordVstrPt, HTString HostnameVstrPt, HTString PortVstrPt, HTString PathVstrPt, HTString ParametersVstrPt, HTString QueryVstrPt, HTString FragmentVstrPt, Vstr ErrInfoVstrPt )
	{
		return VstrUrlParse( m_VstrPt, PrtclVstrPt, UsernameVstrPt, PasswordVstrPt, HostnameVstrPt, PortVstrPt, PathVstrPt, ParametersVstrPt, QueryVstrPt, FragmentVstrPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//获取动态字符串。
	public String GetStr()
	{
		return VstrGetStr( m_VstrPt );
	}

	//销毁动态字符串。
	public int Dstoy()
	{
		if( m_VstrPt != 0 )
		{
			if( VstrDstoy( m_VstrPt ) == 0 )
			{
				m_VstrPt = 0;
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

	//创建并初始化动态字符串。
	private native int VstrInit( HTLong VstrPt, String SrcStrPt );
	//销毁动态字符串。
	private native int VstrDstoy( long VstrPt );

	//复制字符串到动态字符串。
	private native int VstrCpy( long VstrPt, String SrcStrPt );

	//插入字符串到动态字符串的指定位置。
	private native int VstrIns( long VstrPt, long Pos, String SrcStrPt );

	//追加字符串到动态字符串的末尾。
	private native int VstrCat( long VstrPt, String SrcStrPt );

	//清空动态字符串。
	private native int VstrSetEmpty( long VstrPt );
	//设置动态字符串的大小。
	private native int VstrSetSz( long VstrPt, long VstrSzChr );

	//获取动态字符串的字符串指针。
	private native int VstrGetStrPt( long VstrPt, HTLong VstrStrPtPt );
	//获取动态字符串的字符集。
	private native int VstrGetChrSet( long VstrPt, HTInt VstrChrSetPt );
	//获取动态字符串的大小。
	private native int VstrGetSz( long VstrPt, HTLong VstrSzChrPt );
	//获取动态字符串的长度。
	private native int VstrGetLen( long VstrPt, HTLong VstrLenChrPt );

	//动态字符串解析为Url。
	private native int VstrUrlParse( long VstrPt, HTString PrtclVstrPt, HTString UsernameVstrPt, HTString PasswordVstrPt, HTString HostnameVstrPt, HTString PortVstrPt, HTString PathVstrPt, HTString ParametersVstrPt, HTString QueryVstrPt, HTString FragmentVstrPt, long ErrInfoVstrPt );

	//获取动态字符串。
	private native String VstrGetStr( long VstrPt );
}