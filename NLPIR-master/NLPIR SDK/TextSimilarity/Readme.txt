				          Readme
�ı����ƶȼ���ϵͳ��֧�ֶ�����룬֧���ı��ļ����ڴ��ı����㣬֧������ģ�ͣ���ģ�͡���ģ�͡���������ģ��
�����������£�

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

����test�ṩ��a.txt��b.txt������Ӧ��Ϊ��
Using Char model, Sim between file a.txt and file b.txt is 0.24
Using Word model, Sim between file a.txt and file b.txt is 0.20
Using Keyword model, Sim between file a.txt and file b.txt is 0.77


��ϵ��ʽ
	�Ż�ƽ ��ʿ ������
��������ѧ�������������ھ�ʵ���� ����
��ַ�������������йش��ϴ��5�� 100081
�绰��+86-10-68918642
Email:kevinzhang@bit.edu.cn
MSN:  pipy_zhang@msn.com;
����: http://ictclas.nlpir.org (NLPIR/ICTCLAS����)
΢��:http://www.weibo.com/drkevinzhang/
 
Dr. Kevin Zhang  (�Ż�ƽ��Zhang Hua-Ping)
Associate Professor, Graduate Supervisor
Director, Big Data Search and Mining Lab.
Beijing Institute of Technology 
Add: No.5, South St.,Zhongguancun,Haidian District,Beijing,P.R.C  PC:100081
Tel: +86-10-68918642
Email:kevinzhang@bit.edu.cn
MSN:  pipy_zhang@msn.com;
Website: http://ictclas.nlpir.org (NLPIR/ICTCLAS Official Website)
Microblog: http://www.weibo.com/drkevinzhang/