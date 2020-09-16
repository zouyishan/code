package cuny.blender.englishie.nlp.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileCopy {
	static String encoding = "UTF-8";
	//static String home = "C:/Users/blender/workspace/blender/corpus/ASR/";
	static final String home = "C:/Users/blender/workspace5/TAC/corpus/TAC_2009_KBP_Evaluation_Source_Data/";
	static String fileList = home+"filelist";
	static String target = home+"output/";

	public static void main(String[] args) throws IOException {
		/*try {
			File srcDir = new File(home);
			
			if (srcDir.exists() && srcDir.isDirectory()) {
				
				File[] fileList = srcDir.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					String fileName = fileList[i].getAbsolutePath();
					File srcSubDir = new File(fileName);
					if (srcSubDir.isDirectory()){
						System.out.println("Enter Dir " + fileName +"\n");
						File[] subFileList = srcSubDir.listFiles();
						for (int j = 0; j < subFileList.length; j++) {
							String src = subFileList[j].getAbsolutePath();
							File srcFile = new File(src);
							if (srcFile.isFile()&&src.endsWith(".xml")){
								copy(src, target);
							}
						}
					}
				}	
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}*/
		//generateFilelist();
		copyByFilelist(fileList,home);
		
	}

	public static void generateFilelist(){
		Vector <String> files = new Vector <String>();
		String filelist= "C:/Users/blender/Desktop/2009_eval_entity_annotation.tab";
		
		try {
			BufferedReader reader = new BufferedReader (new FileReader(filelist));
			int docCount1 = 0;
			String currentDoc;
			while ((currentDoc = reader.readLine()) != null) {
				if (!Character.isDigit(currentDoc.charAt(0))){
					continue;
				}
				String [] str=currentDoc.split("\\s+");
				currentDoc=str[4];
				if (!files.contains(currentDoc)){
					files.add(currentDoc);
				}
				
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		String toFileName ="C:/Users/blender/Desktop/filelist";
		try {
			PrintStream textWriter = new PrintStream(new FileOutputStream(toFileName));
			for (int i=0;i<files.size();i++){
				textWriter.println(files.get(i));
			}
			textWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//copy files from srcPath by filelist
	public static void copyByFilelist (String filelist, String srcPath)
			throws IOException{
		try {
			String mapfile = "C:/Users/blender/workspace5/TAC/corpus/TAC_2009_KBP_Evaluation_Source_Data/docs/docid_to_file_mapping.tab";
			BufferedReader reader = new BufferedReader (new FileReader(mapfile));
			Hashtable filepathmap = new Hashtable();
			String line;
			while ((line = reader.readLine()) != null) {
				String [] str=line.split("\\s+");
				filepathmap.put(str[0], str[1]);
			}
			reader.close();
			
			BufferedReader reader1 = new BufferedReader (new FileReader(filelist));
			int docCount1 = 0;
			String currentDoc;
			while ((currentDoc = reader1.readLine()) != null) {
				//String [] str=currentDoc.split("\\s+");
				//currentDoc=str[0];
				//currentDoc=str[1];
				String path = (String)filepathmap.get(currentDoc);
				docCount1++;
				File srcFile = new File(srcPath+path/*.replace(".sgm", ".Plainsent")*/);
				if (srcFile.exists()){
					copy(srcPath+path/*.replace(".sgm", ".Plainsent")*/, target+currentDoc+".sgm"/*.replace(".sgm", ".sen")*/);
				}
			}
			reader1.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	

	public static void copy(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName()/*.replace(".sen", ".sen")*/);

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName()
					+ "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}

		try {
			BufferedReader reader = new BufferedReader (new FileReader(fromFileName));
			PrintStream textWriter = new PrintStream(new FileOutputStream(toFileName));
			String line;
			
			while ((line = reader.readLine()) != null) {
				textWriter.println(line);
			}
			
			reader.close();
			textWriter.close();
		}catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
		}
	}
	
	public static void copy1(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName()/*.replace(".sen", ".sen")*/);

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName()
					+ "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}

		try {
			BufferedReader reader = new BufferedReader (new FileReader(fromFileName));
			PrintStream textWriter = new PrintStream(new FileOutputStream(toFileName));
			String line;
			textWriter.print("<TEXT>");
			String pattern = "^\\d+(.\\d+)?\\s+\\d+(.\\d+)?\\s+\\d+(.\\d+)?\\s+(.*)$";
			Pattern text = Pattern.compile(pattern);
			while ((line = reader.readLine()) != null) {
				Matcher fit = text.matcher(line); 
				if (fit.find()) {
					String src = fit.group(4);
					if (src.length()==0)
						continue;
					char ch=src.charAt(0);
					if (Character.isLowerCase(ch)){
						ch=Character.toUpperCase(ch);
						src = ch+src.substring(1);
					}
					textWriter.println(src+".");
				}
				else
					textWriter.println(line);
			}
			textWriter.println("</TEXT>");
			reader.close();
			textWriter.close();
		}catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
		}
	}
}
