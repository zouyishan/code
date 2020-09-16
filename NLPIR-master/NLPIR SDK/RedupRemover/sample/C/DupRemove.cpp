// DupRemove.cpp : �������̨Ӧ�ó������ڵ㡣
//

#include "../LJRedupRemover.h"

#include <string>
#include <vector>
#include <list>
#include <fstream>
#include <stdio.h>
#ifdef OS_LINUX
#include <dirent.h>
#include <unistd.h> 
#else
#include <direct.h>
#include <io.h>

#endif
#include <sys/stat.h>
#include <ctype.h>
using namespace std;
typedef  std::string  tstring;
//support subtree
bool gfn_vScanFiles(const char* sFolderName, std::vector<std::string>& vsFileName,const char* sFilter)
{//sFilter: "*.txt"
	tstring sDirFiles = sFolderName;	
	sDirFiles += "/";
	tstring sNoSuffixDir = sDirFiles;
	sDirFiles+="*.*";
	//sDirFiles +=sFilter;
#ifndef OS_LINUX
	intptr_t iNoFile, hFoundFile=-1;
	struct _finddata_t stFileStat;

	hFoundFile = _findfirst(sDirFiles.c_str(), &stFileStat);
	iNoFile =  (hFoundFile == -1);

	while (!iNoFile)
	{
		if (stFileStat.name[0] == '.')
		{//break;
			//iNoFile = _findnext(hFoundFile, &stFileStat);
		}
		else if(stFileStat.attrib & _A_SUBDIR) 
		{//	�˵�"."��".."	��Ŀ¼
			tstring newPath=sFolderName;
			newPath+="/";
			newPath+=stFileStat.name;
			gfn_vScanFiles(newPath.c_str(),vsFileName,sFilter);
		}
		else if (stricmp(stFileStat.name+strlen(stFileStat.name)-strlen(sFilter),sFilter)==0)
		{
			tstring sFullFilePath =  sNoSuffixDir + stFileStat.name;
			vsFileName.push_back(sFullFilePath);
		}
		iNoFile = _findnext(hFoundFile, &stFileStat);
	};
	if (hFoundFile!=-1)
		_findclose(hFoundFile);

#else

	DIR * dirp;  //Ŀ¼ָ�� 
	char sFileName[2048];
	char sFilterExt[1024]=".";
	strcpy(sFilterExt,sFilter);
	dirp = opendir(sFolderName);
	if(dirp==NULL) { 	
		//cerr<<" Failure to open the directory " << lpszDir<<endl;
		return false;
	} 

	struct dirent* direntp;                //ָ�������ҵ����ļ���ָ��
	struct stat st;                        //ȡ�ص��ļ���״̬ 

	while((direntp = readdir(dirp)) != NULL)   //ѭ����ȡһ���ļ�,ÿ��ȡһ��dirpָ�������ƶ�
	{
		if((strcmp(direntp->d_name,".")!=0) && (strcmp(direntp->d_name,"..")!=0))
		{		        
			strcpy(sFileName, sNoSuffixDir.c_str());
			strcat(sFileName, direntp->d_name);			
			if(stat(sFileName,&st) == -1) {
				//cerr<<"Error:stat " <<sFileName<<endl;
				continue;                        
			}			
			if((st.st_mode& S_IFMT) == S_IFDIR)
			{//�ж��ǲ���һ��Ŀ¼ 
				tstring newPath=sFolderName;
				newPath+="/";
				newPath+=direntp->d_name;
				gfn_vScanFiles(newPath.c_str(),vsFileName,sFilter);
			}
			else
			{
				tstring sFullFilePath =  sNoSuffixDir + direntp->d_name;
				if(sFullFilePath.rfind(sFilterExt) == sFullFilePath.size()-strlen(sFilterExt))
					vsFileName.push_back(sFullFilePath);
			}

		}//if
	}//while        
	if(dirp != NULL) closedir(dirp);
#endif
	return true;	

}

bool gfn_bReadFile(const char* lpszFilename, string& sFileText) {

	if(!lpszFilename || (strlen(lpszFilename)==0)) {
		return false;
	}

	sFileText = "";

	FILE* fp = fopen(lpszFilename, "rb");

	try{
		if(!fp) {
			return false;
		}//if

		fseek(fp, 0L, SEEK_END);
		long lSize = ftell(fp);
		fseek(fp, 0L, SEEK_SET);

		if(lSize>0){
			char* lpszFileText = (char*)calloc(lSize+1, sizeof(char));
			if(!lpszFilename) {
				fclose(fp);
				return false;
			}

			fread(lpszFileText, sizeof(char), lSize,fp);

			sFileText = lpszFileText;
			free(lpszFileText);
		}//if

		fclose(fp);

	}catch(...) {
		if(fp)
			fclose(fp);	
	}//try

	return true;
}//gfn_bReadFile

int main(int argc, char* argv[])
{
	/*
	FILE *fp = fopen("Data.txt", "wb");
	if (fp!=NULL)
	{
		fwrite("", 0, 0, fp);
		fclose(fp);
	}
	*/
	if (!RR_Init("Data.txt", "./",false,0,UTF8_CODE))
	{
		printf("%s\r\n", RR_GetLastErrMsg());
		printf("ȥ�������ʼ��ʧ�ܣ����˳�����飡����ϵwww.nlpir.org/��������΢�� @ICTCLAS�Ż�ƽ��ʿ ��\n");
		return-1;
	}

	vector<string> fileDir;
	string path = argv[1];
	string sContent = "";
	if(!gfn_vScanFiles(path.c_str(),fileDir,".txt"))
	{
		printf("��ȡ�ļ������ļ�ʧ��......\n");
		system("pause");
		return -1;
	}
	string sFilename;
	char *pcFindAll = new char[65565];
	pcFindAll[0]=0;
	char sID[100];
	for (int i = 0; i < fileDir.size();i++)
	//for (int i = 0; i < 10;i++)
	{
		sFilename = fileDir.at(i);
		gfn_bReadFile(sFilename.c_str(), sContent);
		/*
		if (i<5)
		{
			sContent="��������Ƥ��Ԥ�㹫��11�˱������������Ч����ǽ���ڶ��Ծ�(ͼ)������Ƶ�����³���������ӻ�̸ �İ������ն���ר�� | ũ��ķ���Ϊ�β�����GDP �ҿ�����̰�ٱ�������� | �ձ�Ů��:ȥ��ȥ�ΰݲ���������˵�����ķ����������˾�ܲ���ҹ��Ī˹�������������ƾ� | ����籣���˶�ʽ��������:������ɲ��ն�Ժʿ��˼����ľ�����ս��������� ��ɼ�����Դ�˷�";
		}
		else
		{
			sContent="���������ڳ����������ٻط��� ������������������ְλ�����ȴ�1300:1 ��700��ְλ�㱨�������˽�������Ͷ������� ʵ���߽�У԰����͸����Ƽ� |[��һ��]�Ծ��������µ��̣��������Ϊ����ƻ������iOS 8.1 ����ָ�� iPad�����������������»�����ż���������֩������Ȯ Ӣһ������145ֻ����ͬסʱ�� | �����ر���һĨ����Ѫ�� ��ԲԲ��Ѹ�»齿���ȵ� | �����¼ҽ���ʱ���ȵ� ��������һ���򾡹�ע | Э������ʧ���ͻ� ��ƽƻ�����۵�ѡ��";
		}*/
		sprintf(sID,"%d",i);
		sContent += "\r\n";
		if (RR_FileProcess(sContent.c_str(), sFilename.c_str()) == 1)
		//if (RR_FileProcess(sContent.c_str(), sFilename.c_str()) == 1)
		//if (RR_FileProcessE(sFilename.c_str()) == 1)
		{
			//memset(pcFindAll, 0, 65565);
			RR_FindRepeat(pcFindAll, true);//�ɲ�����м����
			printf("sID=%s FindAllRepeat are : %s \n",sFilename.c_str(),pcFindAll);
		}
	}

	if (pcFindAll != NULL)
	{
		delete[] pcFindAll;
		pcFindAll = NULL;
	}
	
	RR_Output("RepeatFile.txt");
	RR_SaveHistoryData("Data.txt");
	RR_Exit();
	return 0;
}

