/****************************************************************************
*
* DocParser  (c) 2019-2020
*     Dr. Kevin Zhang (Hua-Ping Zhang)
*     All rights reserved.
*
* This file is the confidential and proprietary property of
* Kevin Zhang and the possession or use of this file requires
* a written license from the author.
* Filename:
* Abstract:
*          DocParser.h: definition of the NLPIR Parser API
* Author:   Kevin Zhang
*          Email: pipy_zhang@msn.com kevinzhang@bit.edu.cn
*			Weibo: http://weibo.com/drkevinzhang
*			Homepage: http://www.nlpir.org
* Date:     2019-12-10
*
* Notes:
*
****************************************************************************/
#if !defined(__DOCPARSER_DOC_PARSER_H_INCLUDED__)
#define __DOCPARSER_DOC_PARSER_H_INCLUDED__

#ifdef OS_LINUX
#define DOCPARSER_API extern "C" 
#else
#ifdef DOCPARSER_EXPORTS
#define DOCPARSER_API extern "C" __declspec(dllexport)
#else
#define DOCPARSER_API extern "C"  __declspec(dllimport)
#endif
#endif

//�ڲ�����ʹ��

#ifdef DOCPARSER_INTERNAL_CALL
#define DOCPARSER_API 
#endif

#define DP_HANDLE  size_t 

/*********************************************************************
*
*  Func Name  : DP_Init
*
*  Description: Init Document Parser
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
DOCPARSER_API int DP_Init(const char * sDataPath = 0, const char*sLicenceCode = 0);

/*********************************************************************
*
*  Func Name  : DP_Parse
*
*  Description: Parse a document file
*
*
*  Parameters : DP_HANDLE: the result handle
*
*				 
*  Returns    : DP_HANDLE: the result handle
*				-1, if failure
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2003-12-22
*********************************************************************/
DOCPARSER_API DP_HANDLE DP_Parse(const char *sDocFilename);

/*********************************************************************
*
*  Func Name  : DP_GetText
*
*  Description: ��ȡ�ĵ����ı�����
*
*
*  Parameters : DP_HANDLE: �ĵ����������handle
*
*
*  Returns    : const char *: �ı����� ���ı�����
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2019-12-10
*********************************************************************/
DOCPARSER_API const char * DP_GetText(DP_HANDLE handle);

/*********************************************************************
*
*  Func Name  : DP_GetTextE
*
*  Description: ��ȡ�ĵ����ı�����
*
*
*  Parameters : DP_HANDLE: �ĵ����������handle
*
*
*  Returns    : const char *: �ı����ݣ�����ʽ��JSON��
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2019-12-10
*********************************************************************/
DOCPARSER_API const char * DP_GetTextE(DP_HANDLE handle);

/*********************************************************************
*
*  Func Name  : DP_GetTables
*
*  Description: ��ȡ�ĵ��ı����ϸ��Ϣ
*
*
*  Parameters : DP_HANDLE: �ĵ����������handle
*
*
*  Returns    : JSON��ʽ�ı�������ַ���
*	
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2019-12-10
*********************************************************************/
DOCPARSER_API const char * DP_GetTables(DP_HANDLE handle);

/*********************************************************************
*
*  Func Name  : DP_GetFigures
*
*  Description: ��ȡ�ĵ���ͼƬ��ϸ��Ϣ
*
*
*  Parameters : DP_HANDLE: �ĵ����������handle
*
*
*  Returns    : JSON��ʽ��ͼƬ�����ַ���
*
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2019-12-10
*********************************************************************/
DOCPARSER_API const char * DP_GetFigures(DP_HANDLE handle);

/*********************************************************************
*
*  Func Name  : DP_ReleaseHandle
*
*  Description: �ͷŷ������
*
*
*  Parameters : DP_HANDLE: �ĵ����������handle
*
*
*  Returns    : void
*
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2019-12-10
*********************************************************************/
DOCPARSER_API void DP_ReleaseHandle(DP_HANDLE handle);

/*********************************************************************
*
*  Func Name  : DP_GetLastErrorMsg
*
*  Description: GetLastErrorMessage
*
*
*  Parameters : void
*
*
*  Returns    : the result buffer pointer
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2014-2-27
*********************************************************************/
DOCPARSER_API const char * DP_GetLastErrorMsg();

#endif
