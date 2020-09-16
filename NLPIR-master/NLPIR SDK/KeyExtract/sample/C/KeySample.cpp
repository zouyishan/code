// KeySample.cpp : �������̨Ӧ�ó������ڵ㡣
//
#include <stdio.h>
#include "KeyExtract.h"

#ifndef OS_LINUX
#ifndef WIN64
#pragma comment(lib, "../../../bin/KeyExtract/KeyExtract.lib")
#else
#pragma comment(lib, "../../../bin/KeyExtract/x64/KeyExtract.lib")
#endif
#endif

int main(int argc, const char * argv[])
{

	if(!KeyExtract_Init("D:\\NLPIR",GBK_CODE,""))//��������һ��Ŀ¼�£�Ĭ��ΪGBK����ķִ�
	{
		printf("KeyExtract  INIT FAILED!\n");
		return 1;
	}
	//int nCount = KeyExtract_ImportKeyBlackList(NULL, "#nr#ns#");
	int nCount;// = KeyExtract_ImportUserDict("keywords.txt");
	const char *pKeys;//=KeyExtract_GetKeyWords("Ϊ��߹��̽���ʩ���ֳ��豸�͹�ҵ�����ܵ����ӹ��̵�ʩ��ˮƽ����ǿ���ӹ���ʩ�����̵��������ơ�",50,true);
	//���ı��з����ؼ���
	pKeys = KeyExtract_GetFileKeyWords("d:/nlpir/test/The Farmer in the Dell.txt");
	printf("%s", pKeys);
	
	pKeys = KeyExtract_GetKeyWords("Ϊ��߹��̽���ʩ���ֳ��豸�͹�ҵ�����ܵ����ӹ��̵�ʩ��ˮƽ����ǿ���ӹ���ʩ�����̵��������ơ�", 50, true);;
	printf("Before ImportUserDict Keywords are:\n%s\n", pKeys);
	const char *pUserDict = "�������� key";
	KeyExtract_AddUserWord(pUserDict);
	//nCount = KeyExtract_ImportUserDict("keywords.txt");
	pKeys = KeyExtract_GetKeyWords("Ϊ��߹��̽���ʩ���ֳ��豸�͹�ҵ�����ܵ����ӹ��̵�ʩ��ˮƽ����ǿ���ӹ���ʩ�����̵��������ơ�", 50, true);;
	printf("After KeyExtract_AddUserWord %s Keywords are:\n%s\n", pUserDict, pKeys);
	//nCount = KeyExtract_ImportUserDict("keywords2.txt");
	pUserDict = "ʩ������ key";
	KeyExtract_AddUserWord(pUserDict);
	//nCount = KeyExtract_ImportUserDict("keywords.txt");
	pKeys = KeyExtract_GetKeyWords("Ϊ��߹��̽���ʩ���ֳ��豸�͹�ҵ�����ܵ����ӹ��̵�ʩ��ˮƽ����ǿ���ӹ���ʩ�����̵��������ơ�", 50, true);;
	printf("After KeyExtract_AddUserWord %s Keywords are:\n%s\n", pUserDict, pKeys);
	
	KeyExtract_Exit();
	return 0;
}

