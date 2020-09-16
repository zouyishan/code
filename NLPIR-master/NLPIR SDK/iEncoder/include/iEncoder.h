/****************************************************************************
*
* NLPIR iEncoder intelligent encoding (IE) system Copyright (c) 2000-2020
*     Dr. Kevin Zhang (Hua-Ping Zhang)
*     All rights reserved.
*
* This file is the confidential and proprietary property of
* Kevin Zhang and the possession or use of this file requires
* a written license from the author.
* Filename:
* Abstract:
*          iEncodingAPI.h: definition of NLPIR iEncoding intelligent encoding system
* Author:   Kevin Zhang
*          Email: pipy_zhang@msn.com kevinzhang@bit.edu.cn
*			Weibo: http://weibo.com/drkevinzhang
*			Homepage: http://www.nlpir.org
* Date:     2010-7-13
*			Adding IE_Tokenizer4IR in 2019/12/26
* Notes:
*
****************************************************************************/
#if !defined(__IE_IENCODER_API_H_INCLUDED__)
#define __IE_IENCODER_API_H_INCLUDED__

#ifdef OS_LINUX
#define IENCODINGAPI_API extern "C" 
#else
#ifdef IENCODINGAPI_EXPORTS
#define IENCODINGAPI_API extern "C" __declspec(dllexport)
#else
#define IENCODINGAPI_API extern "C"  __declspec(dllimport)
#endif
#endif

// ��������
#define IE_UNKNOWN_CODE		0
#define IE_UTF8_CODE		1
#define IE_GBK_CODE			2
#define IE_BIG5_CODE		3
#define IE_SHIFT_JIS		4
#define IE_EUC_JP			5
#define IE_EUC_KR			6
#define IE_UTF7_CODE		7
#define IE_UNICODE_CODE		8

typedef int IE_CODE_TYPE;

/*********************************************************************
*
*  Func Name  : Init
*
*  Description: Init IE
*               The function must be invoked before any operation listed as following
*
*  Parameters : const char * sInitDirPath=NULL
*				 sDataPath:  Path where Data directory stored.
*				 the default value is NULL, it indicates the initial directory is current working directory path
*				 encode: encoding code;
*				 sLicenseCode: license code for unlimited usage. common user ignore it
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
IENCODINGAPI_API int IE_Init(const char * sDataPath = 0,  const char*sLicenceCode = 0);
/*********************************************************************
*
*  Func Name  : IE_Exit
*
*  Description: ����ת����������
*
*  Parameters : None
*
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2002-8-6
*********************************************************************/
IENCODINGAPI_API void IE_Exit();

/*********************************************************************
*
*  Func Name  : IE_GetLastErrorMsg
*
*  Description: GetLastErrorMessage
*
*
*  Parameters : const char* ���صĴ�����Ϣ
*
*
*  Returns    : the result buffer pointer
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API const char * IE_GetLastErrorMsg();

/*********************************************************************
*
*  Func Name  : IE_CodeDetect
*
*  Description: ����˵������������ʶ��
*
*
*  Parameters : sSrc: [IN], Դ�ı�
*				nSrcLen: [IN], Դ�ı��ĳ���
*
*
*  Returns    : Դ�ı��ı�������
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API IE_CODE_TYPE IE_CodeDetect(const char* sSrc, size_t nSrcLen);


/*********************************************************************
*
*  Func Name  : IE_ToUnicode
*
*  Description: ����ת����ת��ΪUnicode���룬Unicode�������Little endian
*               �Ա���δ֪���ı������Ƚ��б���ʶ��
*
*
*  Parameters : enCodeType: [IN], Դ�ı���ʹ�õı��룬��Դ�ı��ı���δ֪������UNKNOWN_CODE
*				sSrc: [IN], Դ�ı�
*				nSrcLen: [IN], Դ�ı��ĳ���
*				sDest: [OUT], ����ת������ı�
*				nDestLen: [IN/OUT], pcDest����Ч���ȣ��������ת������ı�����
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API size_t IE_ToUnicode(const char* sSrc, size_t nSrcLen, char* sDest, size_t nDestLen, IE_CODE_TYPE codeType = IE_UNKNOWN_CODE);

/*********************************************************************
*
*  Func Name  : IE_ToUnicodeFile
*
*  Description: ����ת����ת��ΪUnicode���룬Unicode�������Little endian
*               ���ı��ļ�ת��Ϊ�ض���ʽ���ı��ļ�
*
*
*  Parameters : sSrcFilename: [IN], Դ�ı��ı��ļ�
*				sDestFilename: [OUT], ����ת������ı��ļ�
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API  size_t IE_ToUnicodeFile(const char* sSrcFilename, const char* sDestFilename);
/*********************************************************************
*
*  Func Name  : IE_ToUtf8
*
*  Description: ����ת����ת��ΪUtf8����
*               �Ա���δ֪���ı������Ƚ��б���ʶ��
*
*
*  Parameters : enCodeType: [IN], Դ�ı���ʹ�õı��룬��Դ�ı��ı���δ֪������UNKNOWN_CODE
*				sSrc: [IN], Դ�ı�
*				nSrcLen: [IN], Դ�ı��ĳ���
*				sDest: [OUT], ����ת������ı�
*				nDestLen: [IN/OUT], pcDest����Ч���ȣ��������ת������ı�����
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API size_t IE_ToUtf8( const char* sSrc, size_t nSrcLen, char* pcDest, size_t iDestLen, IE_CODE_TYPE codeType = IE_UNKNOWN_CODE);
/*********************************************************************
*
*  Func Name  : IE_ToUtf8File
*
*  Description: ����ת����ת��ΪUtf8����
*               ���ı��ļ�ת��Ϊ�ض���ʽ���ı��ļ�
*
*
*  Parameters : sSrcFilename: [IN], Դ�ı��ı��ļ�
*				sDestFilename: [OUT], ����ת������ı��ļ�
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API size_t IE_ToUtf8File(const char* sSrcFilename, const char* sDestFilename);
/*********************************************************************
*
*  Func Name  : IE_ToGBK
*
*  Description: ����ת����ת��ΪGBK����
*               �Ա���δ֪���ı������Ƚ��б���ʶ��
*
*
*  Parameters : enCodeType: [IN], Դ�ı���ʹ�õı��룬��Դ�ı��ı���δ֪������UNKNOWN_CODE
*				sSrc: [IN], Դ�ı�
*				nSrcLen: [IN], Դ�ı��ĳ���
*				sDest: [OUT], ����ת������ı�
*				nDestLen: [IN/OUT], pcDest����Ч���ȣ��������ת������ı�����
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API size_t IE_ToGBK(const char* sSrc, size_t nSrcLen, char* pcDest, size_t iDestLen, IE_CODE_TYPE codeType = IE_UNKNOWN_CODE);
/*********************************************************************
*
*  Func Name  : IE_ToGBKFile
*
*  Description: ����ת����ת��ΪGBK����
*               ���ı��ļ�ת��Ϊ�ض���ʽ���ı��ļ�
*
*
*  Parameters : sSrcFilename: [IN], Դ�ı��ı��ļ�
*				sDestFilename: [OUT], ����ת������ı��ļ�
*
*  Returns    : ʵ��ת�����ֽ�����������(size_t)-1
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
IENCODINGAPI_API size_t IE_ToGBKFile(const char* sSrcFilename, const char* sDestFilename);
#endif
