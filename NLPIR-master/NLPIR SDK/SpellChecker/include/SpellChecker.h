// ���� ifdef ���Ǵ���ʹ�� DLL �������򵥵�
// ��ı�׼�������� DLL �е������ļ��������������϶���� SPELLCHECKER_EXPORTS
// ���ű���ġ���ʹ�ô� DLL ��
// �κ�������Ŀ�ϲ�Ӧ����˷��š�������Դ�ļ��а������ļ����κ�������Ŀ���Ὣ
// SPELLCHECKER_API ������Ϊ�Ǵ� DLL ����ģ����� DLL ���ô˺궨���
// ������Ϊ�Ǳ������ġ�
#if !defined(__NLPIR_SPELL_CHECK_2020_H_INCLUDED__)
#define __NLPIR_SPELL_CHECK_2020_H_INCLUDED__

#ifdef OS_LINUX
	#define SPELLCHECKER_API extern "C" 
#else
	#ifdef SPELLCHECKER_EXPORTS
		#define SPELLCHECKER_API extern "C" __declspec(dllexport)
	#else
		#define SPELLCHECKER_API extern "C" __declspec(dllimport)
	#endif
#endif


/*********************************************************************
*
*  Func Name  : NLPRIR_SpellChecker
*
*  Description: NLPIR spell checker У�Թ���
*
*  Parameters : const char * sLine: input sentence or line
*				 encode: encoding code; 1: utf-8���룬0: ANSI/GBK����
*				 sPath:  Path where Data directory stored.
*				 the default value is NULL, it indicates the initial directory is current working directory path��
*				 Data���ڵ��ļ���·������������ú���Ȩ�ļ���
*  Returns    : success or fail
*  Author     : Kevin Zhang
*  History    :
*              1.create 2020-5-23
*********************************************************************/
SPELLCHECKER_API const char* NLPRIR_SpellChecker(const char *sLine,int encode=1, const char *sPath=0);

SPELLCHECKER_API void NLPRIR_SpellChecker_Exit();
#endif//__NLPIR_SPELL_CHECK_2020_H_INCLUDED__