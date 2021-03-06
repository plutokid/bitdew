/**
 * Combinate file
 * all the chunks in the same directory
 * Method: FileChannel
 */
package xtremweb.core.util.filesplit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.StringTokenizer;

import java.io.*;
import java.nio.channels.*;

public class CombinatorChannel {

    public String srcDirectory = null;  //  directory      must end with "\\" in Windows   or "/" in Linux
    public String[] separatedFiles;     //name of each block
    public String[][] separatedFilesAndSize;   //name and size
    public int FileNum=0; //the number of blocks
    public String fileRealName=""; //get the original file name
    
    /**
     * Constructor
     * @param 
     * @return 
     */
    public CombinatorChannel() {
    }
    
    /**
     * 
     * @param str   block directory   add "\\"      "/"
     * @return 
     */
    public void setDirectory(String str)
    {
	srcDirectory=str;
	getFileAttribute(srcDirectory);
    }
    
    /**
     * 
     * @param sFileName  name of one block
     * @return  the name of original file
     */
    public String getRealName(String sFileName) {
	int len=sFileName.length();
	return sFileName.substring(0,len-9);
    }
    
    /**
     * get the size of one block
     * @param FileName name of one block
     * @return
     */
    public long getFileSize(String FileName)  {
	FileName=srcDirectory+FileName;
	return (new File(FileName).length());
    }
    
    /**
     * get attributes
     * @param drictory 
     */
    public void getFileAttribute(String drictory) {
	File file=new File(drictory);
	separatedFiles=new String[file.list().length];  //1-D filename of each bolck
	separatedFiles=file.list();
	
	//2-D name and size
	separatedFilesAndSize=new String[separatedFiles.length][2];
	Arrays.sort(separatedFiles);  //sort
	FileNum=separatedFiles.length;  //how many blocks in the directory
	System.out.println("getFileAttribute FileNum="+FileNum);
	for(int i=0;i<FileNum;i++) {
	    separatedFilesAndSize[i][0]=separatedFiles[i];  //name
	    separatedFilesAndSize[i][1]=String.valueOf(getFileSize(separatedFiles[i]));  //size
	}
	fileRealName=getRealName(separatedFiles[FileNum-1]);  //original name
    }
    
    /**
     * RandomAccessFile
     * @return true successful
     */
    public boolean CombFile() throws IOException{
	long alreadyWrite=0;
	long count = 0;
	boolean b = false;
	
	File out = new File(srcDirectory+fileRealName);
	for(int i=0;i<FileNum;i++) {
	    System.out.println("combine: read index:" + i);
	    String in = srcDirectory + separatedFilesAndSize[i][0]; 
	    count = Long.parseLong(separatedFilesAndSize[i][1]);
	    b = copyFile(in, out, alreadyWrite, count);
	    alreadyWrite = alreadyWrite + count;
	}

	return b;
    }
    
    public  boolean copyFile(String in, File out, long position, long count) throws IOException {
	FileChannel inChannel = new FileInputStream(new File(in)).getChannel();
	FileChannel outChannel = new FileOutputStream(out).getChannel();
	try {
	    outChannel.transferFrom(inChannel, position, count);
	}
	catch (IOException e) {
	    return false;
	}
	finally {
	    if (inChannel != null) inChannel.close();
	    if (outChannel != null) outChannel.close();
	}
	return true;
    }

    public static void main(String[] args) throws IOException {
	CombinatorChannel combinator = new CombinatorChannel();    
	combinator.setDirectory("/home/btang/test/");
	if (combinator.CombFile()) {
	    System.out.println("Combination successful");
	} else {
	    System.out.println("Combination not successful");
	}
    }
       
}