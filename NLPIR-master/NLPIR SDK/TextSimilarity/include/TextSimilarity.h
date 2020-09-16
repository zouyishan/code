/****************************************************************************
*
* NLPIR/ICTCLAS Lexical Analysis System Copyright (c) 2000-2018
*     Dr. Kevin Zhang (Hua-Ping Zhang)
*     All rights reserved.
*
* This file is the confidential and proprietary property of
* Kevin Zhang and the possession or use of this file requires
* a written license from the author.
* Filename:
* Abstract:
*          TextSimilarity.h: definition of the Text Similarity system API
* Author:   Kevin Zhang
*          Email: pipy_zhang@msn.com kevinzhang@bit.edu.cn
*			Weibo: http://weibo.com/drkevinzhang
*			Homepage: http://www.nlpir.org
* Date:     2018-2-28
*
* Notes:
*
****************************************************************************/
#if !defined(__TS_TEXT_SIMILARITY_H_INCLUDED__)
#define __TS_TEXT_SIMILARITY_H_INCLUDED__

#ifdef OS_LINUX
#define TEXTSIMILARITY_API extern "C" 
#else
#ifdef TEXTSIMILARITY_EXPORTS
#define TEXTSIMILARITY_API extern "C" __declspec(dllexport)
#else
#define TEXTSIMILARITY_API extern "C"  __declspec(dllimport)
#endif
#endif

//�������ƶȵ�ģ��ѡ��
#define SIM_MODEL_CHAR 1//��ģ�ͣ��ٶ���죬��������Թ淶�Ķ��ı�
#define SIM_MODEL_WORD 2//��ģ�ͣ��ٶ����У����������������淶�ĳ��ĵ�
#define SIM_MODEL_KEY 3//�����ģ�ͣ��ٶ�����������������࣬�ʺ��ڸ����ı�

#define UNKNOWN_CODE -1//����Ǹ��ֱ����ϣ�����Ϊ-1��ϵͳ�Զ���⣬���ڲ�ת�������ķ�ʱ�䣬���Ƽ�ʹ��
#define GBK_CODE 0//Ĭ��֧��GBK����
#define UTF8_CODE GBK_CODE+1//UTF8����
#define BIG5_CODE GBK_CODE+2//BIG5����
#define GBK_FANTI_CODE GBK_CODE+3//GBK���룬�������������
#define UTF8_FANTI_CODE GBK_CODE+4//UTF8����

/*********************************************************************
*
*  Func Name  : Init
*
*  Description: Init Text Similarity
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
TEXTSIMILARITY_API int TS_Init(const char * sDataPath = 0, int encode = GBK_CODE, const char*sLicenceCode = 0);
/*********************************************************************
*
*  Func Name  : TS_Exit
*
*  Description: Exist Text Similarity and free related buffer
*               Exit the program and free memory
*				 The function must be invoked while you needn't any lexical anlysis
*
*  Parameters : None
*
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2002-8-6
*********************************************************************/
TEXTSIMILARITY_API bool TS_Exit();
/*********************************************************************
*
*  Func Name  : TS_GetLastErrorMsg
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
TEXTSIMILARITY_API const char * TS_GetLastErrorMsg();


/*********************************************************************
*
*  Func Name  : TS_ComputeSim
*
*  Description: �����ڴ��ı������ƶ�
*
*
*  Parameters : sText1: �ı�����1�� sText2:�ı�����2
*				nModel:ѡ�����ƶȱȽ�ģ��
*  Returns    : the result buffer pointer
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2003-12-22
*********************************************************************/
TEXTSIMILARITY_API double TS_ComputeSim(const char *sText1, const char *sText2,int nModel= SIM_MODEL_WORD);

/*********************************************************************
*
*  Func Name  : TS_ComputeSimF
*
*  Description: �����ı��ļ������ƶ�
*
*
*  Parameters : sTextFilename1: �ı��ļ���1�� sTextFilename2:�ı��ļ���2
*				nModel:ѡ�����ƶȱȽ�ģ��
*  Returns    : the result buffer pointer
*
*  Author     : Kevin Zhang
*  History    :
*              1.create 2003-12-22
*********************************************************************/
TEXTSIMILARITY_API double TS_ComputeSimFile(const char *sTextFilename1, const char *sTextFilename2, int nModel = SIM_MODEL_WORD);

#endif