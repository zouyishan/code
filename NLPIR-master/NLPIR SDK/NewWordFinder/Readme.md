## NLPIR NewWordFinder##


�´ʷ���֧�ֻ�ϱ���

1 ��ʼ��ʱ����������Ϊ-1

if(!NWF_Init(sDataPath,nCode))
	{
		printf("ICTCLAS INIT FAILED!\n");
		return ;
	}

2�������ļ������ڴ���� NWF_GetFileNewWords/NWF_GetNewWords��bool bFormatJson����Ϊtrue���json��ʽ��falseΪXML��ʽ��
Json��ʽ���£�

##Json��ʽ����##
[
   {
      "freq" : 152,
      "pos" : "n_new",
      "weight" : 77.884208081632579,
      "word" : "���ʼ�ֵ"
   },
   {
      "freq" : 71,
      "pos" : "n_new",
      "weight" : 75.102183562405372,
      "word" : "���ڹ�ȨͶ��"
   }
]
	
	
3)��������ļ���ʱ�򣬲���������ģʽ���ȵ���NWF_Batch_Start��ѭ������NWF_Batch_AddFile NWF_Batch_AddMem����ļ������ڴ棬����NWF_Batch_Complete������������NWF_Batch_GetResult������

