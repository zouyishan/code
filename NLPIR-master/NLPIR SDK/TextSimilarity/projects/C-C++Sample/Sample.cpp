// Sample.cpp : �������̨Ӧ�ó������ڵ㡣
//
#include <stdio.h>
#include "TextSimilarity.h"

#ifndef OS_LINUX
#ifndef WIN64
#pragma comment(lib, "../../../bin/TextSimilarity/TextSimilarity.lib")
#else
#pragma comment(lib, "../../../bin/TextSimilarity/x64/TextSimilarity.lib")
#endif
#endif

int main()
{

	if (!TS_Init("D:\\NLPIR", UTF8_CODE))//��������һ��Ŀ¼�£�����ΪUTF8���룬Ĭ��ΪGBK����ķִ�
	{
		printf("KS  INIT FAILED!\n");
		return 1;
	}
	char sFiles[2][100] = { "a.txt", "b.txt" };
	double dSim = TS_ComputeSimFile(sFiles[0], sFiles[1], SIM_MODEL_CHAR);
	//�����ַ����ƶ�ģ�ͣ��ŵ��ٶȿ죬ȱ�������岻�㣬�Ƚ������ļ����ƶȣ�������ڴ�Ƚϣ���ֱ�ӵ���TS_ComputeSim����
	printf("Using Char model, Sim between file %s and file %s is %.2lf\n", sFiles[0],sFiles[1],dSim);

	dSim = TS_ComputeSimFile(sFiles[0], sFiles[1], SIM_MODEL_WORD);
	printf("Using Word model, Sim between file %s and file %s is %.2lf\n", sFiles[0], sFiles[1], dSim);
	//���ô����ƶ�ģ�ͣ�������������֮�䣬�Ƚ������ļ����ƶȣ�������ڴ�Ƚϣ���ֱ�ӵ���TS_ComputeSim����

	dSim = TS_ComputeSimFile(sFiles[0], sFiles[1], SIM_MODEL_KEY);
	printf("Using Keyword model, Sim between file %s and file %s is %.2lf\n", sFiles[0], sFiles[1], dSim);
	//�����������ƶ�ģ�ͣ��ŵ������忼�ǽ϶࣬Ч���ã�ȱ�����ٶ������Ƚ������ļ����ƶȣ�������ڴ�Ƚϣ���ֱ�ӵ���TS_ComputeSim����

	TS_Exit();
	//��ʹ���ˣ�ȫ����Ҫ�˳�
	return 0;
}

