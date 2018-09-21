/*
LOADER class :
This class is responsible to assign memory page frames to the job.
It loads one page each time from DISK to main memory.
loader() method assigns the memory page frames to the job,
loads the first page into main memory and then adds it to the ready 
queue to start its execution.
createPageTable() method creates the page table for the job segment.
segmentFaultHandler() method handles the segment fault.
pageFaultHandler() method handles the page fault.
setMemoryPageFrameStatus() sets the initial status of the page frames
in the memory allocated to the job.
pageReplacement() method implements second chance algorithm to replace 
a page in memory.
loadPageInMemoryToDisk() method loads the page in memory to disk.
loadPageInDiskToMmeory() method loads the page in disk to memory.
getIndexOfAvailablePageFramesInMemory() returns the indexes of the page 
frames available in memory.
allocateMemoryPageFramesToJob() method allocates the page frames in memory 
to the job. 
*/
/*
GLOBAL VARIABLES :
'numberOfJobPageFrames' holds the number of memory frames allocated to the job.
'initialProgramCounterPage' to hold the page which contains the first 
instruction to be executed.
'availablePageFrames' contains the indexes of the available page frames 
in memory.
'JobLengthInPages' contains the number of pages job contains.
'alloactedPageFrames' contains the indexes of allocated page frames to job.
'allocatedPageFrameIndex' holds the initial allocated frame index to load 
into memory.
'memoryPageFrameIndex' is a pointer to the index of the allocated frames to 
implement second chance algorithm
'pageTables' is the page table for paging.
'memoryPageFrameStatus' to hold status of allocated page frames in memory. 
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
 
public class LOADER 
{
public static int numberOfJobPageFrames;
public static int initialProgramCounterPage;
public static int[] availablePageFrames;
public static int jobLengthInPages; 
public static int[] allocatedPageFrames;
public static int allocatedPageFrameIndex=1;
public static int memoryPageFrameIndex=0;
public static LinkedHashMap<String,LinkedHashMap<Integer,List<Integer>>> 
pageTables=new LinkedHashMap<String,LinkedHashMap<Integer,List<Integer>>>();
public static HashMap<Integer,Integer> memoryPageFrameStatus=
new HashMap<Integer,Integer>();
public static void loader(PCB pcb)
{
jobLengthInPages=(SYSTEM.jobSize+SYSTEM.inputDataSegmentSize+
SYSTEM.outputDataSegmentSize)/8;
numberOfJobPageFrames=Math.min(6,jobLengthInPages+2);
initialProgramCounterPage=(
Integer.parseInt(SYSTEM.initialProgramCounter,16)/8);
if(getIndexOfAvailablePageFramesInMemory().length==0)
{
/*check if memory is full - error*/
ERROR_HANDLER.Error_Handler(19);
System.exit(0);
}
availablePageFrames=getIndexOfAvailablePageFramesInMemory();
pcb.jobPageFrames=allocateMemoryPageFramesToJob();
allocatedPageFrames=pcb.jobPageFrames;
pcb.programSegmentInformation=pcb.jobId+"0";
createPageTable(pcb.programSegmentInformation);
loadPageInDiskToMemory(pcb.programSegmentInformation,
initialProgramCounterPage,allocatedPageFrames[0]);
SYSTEM.readyQueue.add(pcb);
setMemoryPageFrameStatus(pcb);
}
public static void createPageTable(String jobSegment)
{
pageTables.put(jobSegment,new LinkedHashMap<Integer,List<Integer>>());
}
public static void segmentFaultHandler(PCB pcb,String jobSegment)
{
SYSTEM.clock+=5; 
SYSTEM.segmentFaultTime+=5;
if(jobSegment.equals(pcb.jobId+"1"))
{
pcb.inputDataSegmentInformation=pcb.jobId+"1";
createPageTable(pcb.inputDataSegmentInformation);
}
else if(jobSegment.equals(pcb.jobId+"2"))
{
pcb.outputDataSegmentInformation=pcb.jobId+"2";
createPageTable(pcb.outputDataSegmentInformation);
}
}
public static void pageFaultHandler(PCB pcb,String jobSegment,int pageNumber)
{
SYSTEM.clock+=10;
SYSTEM.pageFaultTime+=10;
if(allocatedPageFrameIndex==numberOfJobPageFrames)
{
pageReplacement(pcb,jobSegment,pageNumber);
}
else
{
loadPageInDiskToMemory(jobSegment, pageNumber,
allocatedPageFrames[allocatedPageFrameIndex]);
allocatedPageFrameIndex+=1;	
}
}
public static void setMemoryPageFrameStatus(PCB pcb)
{
for(int i=0;i<pcb.jobPageFrames.length;i++)
{
memoryPageFrameStatus.put(pcb.jobPageFrames[i],0);
} 
}
public static void pageReplacement(PCB pcb,String jobSegment,int pageNumber)
{
if(memoryPageFrameIndex==numberOfJobPageFrames)
{
memoryPageFrameIndex=0;
}
int[] memoryPageFrames=pcb.jobPageFrames;
for(Entry<String, LinkedHashMap<Integer, List<Integer>>> entry: 
pageTables.entrySet())
{
LinkedHashMap<Integer,List<Integer>> pageTable=entry.getValue();
for(Entry<Integer, List<Integer>> entryValue: pageTable.entrySet())
{
int frame=entryValue.getValue().get(0);
int reference=entryValue.getValue().get(1);
if(frame==memoryPageFrames[memoryPageFrameIndex])
{
if(reference==0 && memoryPageFrameStatus.get(frame)!=1 )
{
if(entryValue.getValue().get(2)==1)
{
loadPageInMemoryToDisk(entry.getKey(),frame,
DISK.diskManager.get(entry.getKey()).get(entryValue.getKey()).get(0));
}
pageTables.get(entry.getKey()).remove(entryValue.getKey());
loadPageInDiskToMemory(jobSegment,pageNumber,frame);
memoryPageFrameStatus.put(frame,1);
return;	
}
else if(reference==1)
{
entryValue.getValue().set(1,0);
memoryPageFrameStatus.put(frame,0);
}
}
}
}
memoryPageFrameIndex++;
pageReplacement(pcb,jobSegment,pageNumber);
}
public static void loadPageInMemoryToDisk
(String jobSegment,int memoryPage,int pageNumber)
{
for(int i=0;i<8;i++)
{
for(int j=0;j<16;j++)
{
int memoryAddress=memoryPage*8+i;
int diskAddress=DISK.diskManager.get(jobSegment).get(pageNumber).get(0)*8+i;
DISK.virtualMemory[diskAddress][j]=MEMORY.mainMemory[memoryAddress][j];
}
}
}
public static void loadPageInDiskToMemory
(String jobSegment,int pageNumber,int memoryPage)
{
for(int i=0;i<8;i++)
{
for(int j=0;j<16;j++)
{
int memoryAddress=memoryPage*8+i;
if(memoryPage<0 || memoryPage>32)
{
/*check for invalid memory address - error*/
ERROR_HANDLER.Error_Handler(8);
System.exit(0);
}
int diskAddress=DISK.diskManager.get(jobSegment).get(pageNumber).get(0)*8+i;
MEMORY.mainMemory[memoryAddress][j]=DISK.virtualMemory[diskAddress][j];
}
}
MEMORY.freeMemoryBitVector[memoryPage]=1;
List<Integer> pageInformation=new ArrayList<Integer>();
pageInformation.add(0,memoryPage);
pageInformation.add(1,1);
pageInformation.add(2,0);
pageInformation.add(3,DISK.diskManager.get(jobSegment).get(pageNumber).get(1));
pageTables.get(jobSegment).put(pageNumber,pageInformation);
}
public static int[] getIndexOfAvailablePageFramesInMemory()
{
int[] availablePageFrames=new int[32];
int index=0;
for(int i=0;i<MEMORY.freeMemoryBitVector.length;i++)
{
if(MEMORY.freeMemoryBitVector[i]==0)
{
availablePageFrames[index]=i;
index++;
}
}
return availablePageFrames;
}
public static int[] allocateMemoryPageFramesToJob()
{
int[] allocatedPageFrames=new int[numberOfJobPageFrames];
for(int i=0;i<numberOfJobPageFrames;i++)
{
allocatedPageFrames[i]=availablePageFrames[i];
}
return allocatedPageFrames;
}
}
