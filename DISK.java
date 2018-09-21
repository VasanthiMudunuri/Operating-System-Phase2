/*
DISK class :
This class contains the virtual memory.
initialDiskFrameStatus() is used to set the initial status of
the page frames in disk.
getIndexOfAvailablePageFramesInDisk() returns the indexes of the 
available page frames in memory.
savePageToDisk() is used to save the page to disk.
updateDiskManager() is used to update the information about the 
page frames of the disk.
createDiskTable() is used to create a table to manage the information 
present in the page frames of the disk.
getPageContentinBinary() is used to change the page content from hex
to binary format before saving it to disk.
hexToBinary() is used to change each word in hex to binary of 16 bit.
*/
/*
GLOBAL VARIABLES :
'virtualMemory' is the virtual memory which consists of 256 page
frames.Each page frame is of 8 word capacity where each word is 16 bit.
'freeDiskBitVector' holds the status of the 32 page frames in Disk.
'diskManager' is a table to manage information present in the disk.
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DISK {
public static char[][] virtualMemory=new char[2048][16];
public static int[] freeDiskBitVector=new int[256];
public static HashMap<String,HashMap<Integer,List<Integer>>> diskManager=
new HashMap<String,HashMap<Integer,List<Integer>>>();

public static void initialDiskFrameStatus(int jobid)
{
if(jobid==1)
{
Arrays.fill(freeDiskBitVector,0);
}
}
public static int[] getIndexOfAvailablePageFramesInDisk()
{
int[] availablePageFrames=new int[256];
int index=0;
for(int i=0;i<DISK.freeDiskBitVector.length;i++)
{
if(DISK.freeDiskBitVector[i]==0)
{
availablePageFrames[index]=i;
index++;
}
}
return availablePageFrames;
}
public static void savePageToDisk
(String jobSegment,int pageNumber,int diskPage,String page)
{
if(page.length()>32)
{
/*check for invalid loader format - error*/
ERROR_HANDLER.Error_Handler(13);
System.exit(0);
}
if(page.equals(""))
{
freeDiskBitVector[diskPage]=1;
updateDiskManager(jobSegment, pageNumber,diskPage,0);
}
String pageContent=getPageContentinBinary(page);
int k=0;
for(int i=0;i<page.length()/4;i++)
{
for(int j=0;j<16;j++)
{
int diskAdress=diskPage*8+i;
if(diskPage<0 || diskPage>255)
{
/*check for invalid disk address - error*/
ERROR_HANDLER.Error_Handler(30);
System.exit(0);
}
virtualMemory[diskAdress][j]=pageContent.charAt(k);
k++;
}
}
freeDiskBitVector[diskPage]=1;
updateDiskManager(jobSegment, pageNumber, diskPage,page.length());
}
public static void updateDiskManager
(String jobSegment,int pageNumber,int diskPage,int pageContentLength)
{
HashMap<Integer,List<Integer>> diskTable=diskManager.get(jobSegment);
List<Integer> pageInformation=new ArrayList<Integer>();
pageInformation.add(0,diskPage);
pageInformation.add(1,pageContentLength);
diskTable.put(pageNumber,pageInformation);
diskManager.put(jobSegment,diskTable);
}
public static void createDiskTable(String jobSegment)
{
diskManager.put(jobSegment,new HashMap<Integer,List<Integer>>());
}
public static String getPageContentinBinary(String pageContent)
{
String word;
int wordIndex=0;
String binaryContent="";
while(wordIndex<pageContent.length())
{
word=pageContent.substring(wordIndex,Math.min(wordIndex+4,
pageContent.length()));
binaryContent+=DISK.hexToBinary(word);
wordIndex+=4;
}
return binaryContent;
}
public static String hexToBinary(String hex)
{
if(!(hex.matches("^[0-9a-fA-F]+$")))
{
/*check for invalid hex format - error*/
ERROR_HANDLER.Error_Handler(12);
System.exit(0);
}
int hexInt=Integer.parseInt(hex,16);
String binary=Integer.toBinaryString(hexInt);
int binaryLength=binary.length();
int requiredlength=hex.length()*4;
if(binaryLength<requiredlength*4)
{
for(int i=0;i<requiredlength-binaryLength;i++)
{
binary="0"+binary;
}
}
return binary;
}
}
