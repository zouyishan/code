/****************************************************************************
*
* NLPIR Document Compare System Copyright (c) 2000-2020
*	  �ṩ���ิ�ӵ��ĵ��ȽϹ��ܣ����ɱȽϽ����json��ʽ��
*     All rights reserved.
*
* This file is the confidential and proprietary property of
* Kevin Zhang and the possession or use of this file requires
* a written license from the author.
* Filename:
* Abstract:
*          DocCompare.h: definition of the NLPIR Document Compare System
*
* Date:     2019-8-9
*
* Notes:  ��һ�汾��ʵ�����о������գ�wanggang@nlpir.org��
*
****************************************************************************/
#ifndef __NLPIR_DOC_COMPARE_INCLUDED_H___
#ifdef OS_LINUX
#define DOCCOMPARE_API extern "C" 
#else
#ifdef DOCCOMPARE_EXPORTS
#define DOCCOMPARE_API extern "C" __declspec(dllexport)
#else
#define DOCCOMPARE_API extern "C"  __declspec(dllimport)
#endif
#endif

typedef size_t DC_HANDLE;//�ı��Ƚϵ�����HANDLE

/*********************************************************************
*
*  Func Name  : DC_Init
*
*  Description: Init DC
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
DOCCOMPARE_API int DC_Init(const char * sDataPath = 0, const char*sLicenceCode = 0);
/*********************************************************************
*
*  Func Name  : DC_Exit
*
*  Description: Exit DC
*               The function must be invoked before any operation listed as following
*
*  Parameters : void
*  Returns    : void
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API void DC_Exit(void);

/*********************************************************************
*
*  Func Name  : DC_NewInstance
*
*  Description: Init IE
*               The function must be invoked before any operation listed as following
*
*  Parameters : const char* outputDir:  Path where Data directory stored.
*				useJson: �Ƿ�ʹ��Json��ʽ;
*  Returns    : DC_HANDLE, failed return size_t -1;
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API DC_HANDLE DC_NewInstance(const char* outputDir,  bool useJson=false);

/*********************************************************************
*
*  Func Name  : DC_DeleteInstance
*
*  Description: Delete Instance
*
*  Parameters : DC_HANDLE����DC_NewInstance���ɵ�Handle
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API void DC_DeleteInstance(DC_HANDLE pDCHandle);

/*********************************************************************
*
*  Func Name  : DC_GetLastErrorMsg
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
DOCCOMPARE_API const char * DC_GetLastErrorMsg();

/*********************************************************************
*
*  Func Name  : DC_Cmp2Files
*
*  Description: �Ƚ������ĵ����������json��ʽ�洢������
*
*  Parameters : DC_HANDLE����DC_NewInstance���ɵ�Handle
*				const char* sFile1���Ƚϵ��ļ�1
*				const char *sFile1���Ƚϵ��ļ�2
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API const char * DC_Cmp2Files(DC_HANDLE pDCHandle, const char* sFile1, const char *sFile2);
//����ָ�����ļ�֮����бȶ�;���Է��Ĳ��ҹ��� sCmpFile:�Ƚ��ļ���sBeCmpedFile:���Ƚϵ��ļ�
//added in 2019/8/12

/*********************************************************************
*
*  Func Name  : DC_Cmp2Str
*
*  Description: �Ƚ������ĵ����������json��ʽ�洢������
*
*  Parameters : DC_HANDLE����DC_NewInstance���ɵ�Handle
*				const char* sStr1���Ƚϵ��ַ���1
*				const char *sStr2���Ƚϵ��ַ���2
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API const char * DC_Cmp2Str(DC_HANDLE pDCHandle, const char* sStr1, const char *sStr2);
//����ָ�����ļ�֮����бȶ�;���Է��Ĳ��ҹ��� sCmpFile:�Ƚ��ļ���sBeCmpedFile:���Ƚϵ��ļ�
//added in 2019/8/12


/*********************************************************************
*
*  Func Name  : DC_AddFile2Lib
*
*  Description: ��Ӵ��Ƚϵ��ĵ�����ģ���ĵ���
*
*  Parameters : DC_HANDLE����DC_NewInstance���ɵ�Handle
*				const char* sFilename��ģ���ļ���
*
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API int DC_AddFile2Lib(DC_HANDLE pDCHandle, const char* sFilename);

/*********************************************************************
*
*  Func Name  : DC_CmpFile2Lib
*
*  Description: ��Ӵ��Ƚϵ��ĵ�����ģ���ĵ���
*
*  Parameters : DC_HANDLE����DC_NewInstance���ɵ�Handle
*				const char* sFilename�����Ƚϵ��ı��ļ�
*
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2013-6-8
*********************************************************************/
DOCCOMPARE_API const char * DC_CmpFile2Lib(DC_HANDLE pDCHandle, const char* sFilename);

#endif // !__NLPIR_DOC_COMPARE_INCLUDED_H___
