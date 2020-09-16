#ifdef _WIN64
#pragma comment(lib, "..\\..\\..\\bin\\JZSearch\\x64\\JZSearchAPI.lib")
#else
#pragma comment(lib, "../lib/win32/JZSearchAPI.lib")
#endif

#include <cstdio>
#include <string>
#include <iostream>
#include <sstream>
#include "../API/JZSearchAPI.h"
#include <io.h>
#include <direct.h>

using namespace std;

const string rootDir = "D:\\NLPIR\\JZSearchInstall\\dict\\";//�����ʵ��ŵ�ַ���ĵ�ַ�����и�DataĿ¼
const string testFilesPath = rootDir + "../test/��������(20181212)/";//���Ե��ı��ļ�����·��
const string indexDir = rootDir + "../indexfile/";//�����洢�ĵ�ַ
const string indexPath = indexDir + "JZSearch";//��������
const string fieldDir = rootDir + "../Field/";//�ֶδ洢�ĵ�ַ
const string fieldPath = fieldDir + "field.dat";//�ֶδ洢���ļ���
const int encoding = INDEX_ENCODING_GBK; // ������Ϣ
SEARCHER_HANDLE handle = -1;

enum SearchOption
{
	TopDirectoryOnly = 0,
	AllDirectories = 1,
};

void GetFiles(string folderPath, vector<string> &files, SearchOption op)
{
	_finddata_t FileInfo;
	string strfind = folderPath + "\\*";
	intptr_t Handle = _findfirst(strfind.c_str(), &FileInfo);

	if (Handle == -1L)
	{
		return;
	}
	do
	{
		//�ж��Ƿ�����Ŀ¼
		if (FileInfo.attrib & _A_SUBDIR)
		{
			//���������Ҫ
			if ((strcmp(FileInfo.name, ".") != 0) && (strcmp(FileInfo.name, "..") != 0))
			{
				string newPath = folderPath + "\\" + FileInfo.name;
				if (op == AllDirectories)
				{
					GetFiles(newPath, files, op);
				}
			}
		}
		else
		{
			files.push_back(folderPath + "\\" + FileInfo.name);
		}
	} while (_findnext(Handle, &FileInfo) == 0);
	_findclose(Handle);
}

vector<string> GetFiles(string folderPath, SearchOption op = AllDirectories)
{
	vector<string> files;
	GetFiles(folderPath, files, op);
	return files;
}

void CreateField()
{
	if (!_access(fieldDir.c_str(), _A_NORMAL))
	{
		_mkdir(fieldDir.c_str());
	}
	else
	{
		ostringstream ss;
		ss << "rm -rf " << "\"" << fieldDir << "\"";
		system(ss.str().c_str());

		if (_access(fieldDir.c_str(), _A_NORMAL))
		{
			printf("������ֶ�ʧ��");
			return;
		}
	}

	// ��ʼ������Ҫ��֤ȫ��ִֻ��һ��
	if (JZIndexer_Init(rootDir.c_str(), "", encoding, true) == 0)
	{
		printf("������ʼ��ʧ��");
		return;
	}

	printf("������ʼ���ɹ�");
	JZIndexer_FieldAdd("id", "", FIELD_TYPE_INT, true, true);
	JZIndexer_FieldAdd("key", "", FIELD_TYPE_TEXT, true, true);
	JZIndexer_FieldAdd("text", "", FIELD_TYPE_TEXT, true, true);
	JZIndexer_FieldAdd("value", "", FIELD_TYPE_FLOAT, true, true);

	if (JZIndexer_FieldSave(fieldPath.c_str()))
	{
		printf("�ֶι������");
	}
	else
	{
		printf("�ֶι���ʧ��");
	}
	JZIndexer_Exit();
	printf("");
}

void StartIndex()
{
	if (!_access(indexDir.c_str(), _A_NORMAL))
	{
		_mkdir(indexDir.c_str());
	}
	else
	{
		ostringstream ss;
		ss << "rm -rf " << "\"" << indexDir << "\"";
		system(ss.str().c_str());

		if (_access(indexDir.c_str(), _A_NORMAL))
		{
			printf("���������ʧ��");
			return;
		}
	}

	// 2������µ������ļ�
	int flag = JZIndexer_Init(rootDir.c_str(), fieldPath.c_str(), encoding, 1); // ��ʼ������Ҫ��֤ȫ��ִֻ��һ��

	if (flag == 0)
	{
		printf("JZIndexer_Init��ʼ��ʧ��");
		JZIndexer_Exit();
		return;
	}

	printf("JZIndexer_Init��ʼ���ɹ�");

	JZSEARCH_HANDLE indexer = JZIndexer_NewInstance(indexPath.c_str());
	if (indexer<0)
	{
		printf("JZIndexer����ʵ��ʧ��");
		JZIndexer_DeleteInstance(indexer);
		JZIndexer_Exit();

		return;
	}
	printf("JZIndexer����ʵ���ɹ�");

	

	vector<string> files = GetFiles(testFilesPath, AllDirectories);
	JZSearch_SetIndexSizeLimit(-1);             // ������ǰ���ø÷���

	int i = 0;
	char sVal[10];
	for (auto file : files)
	{
		string fileName = file.substr(file.find_last_of("\\") + 1);
		// string sContext = FileOperate.ReadFile(file);
		JZIndexer_MemIndexing(indexer,fileName.c_str(), "key", 0);
		// search.JZIndexer_MemIndexing(handle, sContext, "value", 0);
		JZIndexer_FileIndexing(indexer,file.c_str(), "text");
		sprintf(sVal, "%d", i);
//		JZIndexer_IntIndexing(indexer,i, "id");
		JZIndexer_MemIndexing(indexer, sVal, "id");
		JZIndexer_FloatIndexing(indexer,i, "value");
		i++;

		if (JZIndexer_AddDoc(indexer) < 1)
		{
			printf("�������ʧ�ܣ�%s\n", fileName.c_str());
			return;
		}
		printf("������ӳɹ���%s\n", fileName.c_str());
	}
	JZIndexer_Save(indexer);
	JZIndexer_DeleteInstance(indexer);
	JZIndexer_Exit();
	printf("�����������\n");
}

void StartSearch(string keyword = "")
{
	if (handle < 0)
	{
		// ��ʼ������Ҫ��֤ȫ��ִֻ��һ��
		handle = JZSearch_Init(indexPath.c_str(), rootDir.c_str(), fieldPath.c_str(), 512000000, encoding, 0, true);
		if (handle < 0)
		{
			return;
		}

	}

	JZSEARCH_HANDLE  instance=JZSearcher_NewInstance(SORT_TYPE_RELEVANCE, handle);
	//����ʵ��ʧ�ܣ��򷵻�Ϊ-1������Ϊʵ����Handle��ÿ�����߳�ʹ��һ��ʵ��

	if (instance<0)
	{
		printf("JZSearcher����ʵ��ʧ�ܣ�������");
		return;
	}
	printf("JZSearcher_NewInstance����ʵ���ɹ�");

	JZSearch_SetAbstractArgu(1000, "", "", 0);
	string query = "[cmd] listall";
	if (!keyword.empty())
	{
		printf("��ʼ�Թؼ��֡�%s����������", keyword.c_str());
		ostringstream ss;
		ss << "[FIELD] * [OR] " << keyword << " [SORT] relevance";
		query = ss.str();
	}
	query = " [field] * [and] ������ [field] id [max] 6";
	////string xml = searcher->Search(query.c_str(), 0, 10);
	const char *pResult = JZSearcher_Search(instance, query.c_str(), 0, 10);
	//query_line: ��ѯ���ʽ
	//nStart:��¼��ʼ��ַ
	//nPageCount����ǰҳ���ؽ����Ŀ
	//��ǰҳ��Ҫ�������еĽ��jason�ַ���
	//const char *sResultXMLFile	Ĭ��Ϊ0�����򣬴洢������ļ��У�added in 2
	if (pResult!=0)
	{
		cout << pResult << endl;
	}

	JZSearcher_DeleteInstance(instance);//�ͷ�ʵ��


}

int main(void)
{
	while (true)
	{
		printf("\n�����ֶΣ����룺0\n");
		printf("�������������룺1\n");
		printf("���������룺2\n");
		printf("�г������������ݣ�3\n");
		printf("�˳������룺����\n");
		int input;
		cin >> input;

		switch (input)
		{
			case 0:
				CreateField();
				break;

			case 1:
				StartIndex();
				break;

			case 2:
			{
				printf("�������������ݣ�");
				string keyword;
				//std::getline(std::cin, keyword);
				cin >> keyword;
				//StartSearch("[field] id [and] 3");
				StartSearch(keyword);
				break;
			}

			case 3:
				StartSearch();
				break;

			default:

				JZSearch_Exit();//ȫ���˳�����Ҫʹ�����
				return 1;
		}
	}
}