package HeavenTao.TinyXml2;

import HeavenTao.Data.*;

public class XMLDocument
{
	static
	{
		System.loadLibrary( "c++_shared" ); //加载libc++_shared.so。
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "MediaFile" ); //加载libMediaFile.so。
	}

	public long m_XMLDocumentPt; //存放XMLDocument指针。

	//构造函数。
	public XMLDocument()
	{
		m_XMLDocumentPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy();
	}

	public int Init()
	{
		if( m_XMLDocumentPt == 0 )
		{
			HTLong p_XMLDocumentPt = new HTLong();
			if( XMLDocumentInit( p_XMLDocumentPt ) == 0 )
			{
				m_XMLDocumentPt = p_XMLDocumentPt.m_Val;
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
	public int Dstoy()
	{
		if( m_XMLDocumentPt != 0 )
		{
			if( XMLDocumentDstoy( m_XMLDocumentPt ) == 0 )
			{
				m_XMLDocumentPt = 0;
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

	public int NewElement( XMLElement XMLNewElementPt, String NamePt )
	{
		return XMLDocumentNewElement( m_XMLDocumentPt, XMLNewElementPt, NamePt );
	}

	public int InsertEndChild( XMLElement XMLChildElementPt )
	{
		return XMLDocumentInsertEndChild( m_XMLDocumentPt, ( XMLChildElementPt != null ) ? XMLChildElementPt.m_XMLElementPt : 0 );
	}

	public int FirstChildElement( XMLElement XMLFirstChildElementPt )
	{
		return XMLDocumentFirstChildElement( m_XMLDocumentPt, XMLFirstChildElementPt );
	}

	public int SaveFile( String FileFullNamePt )
	{
		return XMLDocumentSaveFile( m_XMLDocumentPt, FileFullNamePt );
	}

	public int LoadFile( String FileFullNamePt )
	{
		return XMLDocumentLoadFile( m_XMLDocumentPt, FileFullNamePt );
	}

	private native int XMLDocumentInit( HTLong XMLDocumentPt );
	private native int XMLDocumentDstoy( long XMLDocumentPt );

	private native int XMLDocumentNewElement( long XMLDocumentPt, XMLElement XMLNewElementPt, String NamePt );

	private native int XMLDocumentInsertEndChild( long XMLDocumentPt, long XMLChildElementPt );

	private native int XMLDocumentFirstChildElement( long XMLDocumentPt, XMLElement XMLFirstChildElementPt );

	private native int XMLDocumentSaveFile( long XMLDocumentPt, String FileFullNamePt );

	private native int XMLDocumentLoadFile( long XMLDocumentPt, String FileFullNamePt );
}
