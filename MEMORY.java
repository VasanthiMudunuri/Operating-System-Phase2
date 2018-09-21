/*
MEMORY class :
This class contains the main memory.
initialMemoryPageFrameStatus() is used to set the initial status of 
the page frames in the main memory.
virtualAddressToPhysicalAddress() is used to change the virtual address
to physical address and returns the page number.
memory() method is used to read or write data in memory.It returns the
value in main memory.
readInputFromMemory() method is used to read input to the job present 
in the input data segment.
writeOutputToMemory() method is used to write output of the job into the 
output data segment.
*/
/*
GLOBAL VARIABLES :
'mainMemory' is the main memory which consists of 32 page frames. 
Each page frame is of 8 word capacity where each word is 16 bit.
'freeMemoryBitVector' holds the status of the 32 page frames in Memory.
'readMemoryAddress' is used to hold the value of address to read it.
'writeMemoryAddress' is used to hold the value of address to write to it.
'getPage' is used to hold the page number.
'writeOutputLength' holds the value of number of 
words written to output data segment.
*/

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class MEMORY 
{
public static char[][] mainMemory=new char[256][16];
public static int[] freeMemoryBitVector=new int[32];
public static int readMemoryAddress;
public static int writeMemoryAddress;
public static int getPage;
public static int writeOutputLength=0;

public static void initialMemoryPageFrameStatus()
{
Arrays.fill(freeMemoryBitVector,1);
freeMemoryBitVector[5]=0;
freeMemoryBitVector[8]=0;
freeMemoryBitVector[10]=0;
freeMemoryBitVector[17]=0;
freeMemoryBitVector[20]=0;
freeMemoryBitVector[31]=0;
} 
public static int virtualAddressToPhysicalAddress
(PCB pcb,String jobSegment,int programCounter)
{
getPage=programCounter/8;
for(Entry<String, LinkedHashMap<Integer, List<Integer>>> entry: 
LOADER.pageTables.entrySet())
{
LinkedHashMap<Integer,List<Integer>> pageTable=entry.getValue();
for(Entry<Integer, List<Integer>> entryValue: pageTable.entrySet())
{
int page=entryValue.getKey();
if(page==getPage)
{
return entryValue.getValue().get(0);
}
}
}
LOADER.pageFaultHandler(pcb,jobSegment,getPage);
LinkedHashMap<Integer,List<Integer>> newPageTable=
LOADER.pageTables.get(jobSegment);
for(Entry<Integer, List<Integer>> entry: newPageTable.entrySet())
{
int page=entry.getKey();
if(page==getPage)
{
return entry.getValue().get(0);
}
}
return 0;
}
public static String memory(PCB pcb,String X,String jobSegment,int Y,String Z)
{
int memoryPage=virtualAddressToPhysicalAddress(pcb,jobSegment,Y);
int displacement=Y%8;
int memoryAddress=memoryPage*8+displacement;
switch(X)
{
case "READ":Z="";
for(int i=0;i<16;i++)
{
Z=Z+mainMemory[memoryAddress][i];
}
return Z;
case "WRITE":int k=0;
LOADER.pageTables.get(jobSegment).get(getPage).set(2,1);
if(Z.length()==16)
{
for(int i=0;i<16;i++)
{
mainMemory[memoryAddress][i]=Z.charAt(k);
k++;
}
}
}
return "";
}
public static String readInputFromMemory(PCB pcb,String jobSegment)
{
String Z="";
if(jobSegment==null)
{
LOADER.segmentFaultHandler(pcb,pcb.jobId+"1");
HashMap<Integer,List<Integer>> getPage=
DISK.diskManager.get(pcb.inputDataSegmentInformation);
int pageNumber=getPage.keySet().iterator().next();
LOADER.pageFaultHandler(pcb,pcb.inputDataSegmentInformation,pageNumber);
LinkedHashMap<Integer,List<Integer>> pageTable=
LOADER.pageTables.get(pcb.inputDataSegmentInformation);
for(Entry<Integer, List<Integer>> entry: pageTable.entrySet())
{
int page=entry.getKey();
if(page==pageNumber)
{
readMemoryAddress=entry.getValue().get(0)*8;
}
}
for(int i=0;i<16;i++)
{
Z=Z+mainMemory[readMemoryAddress][i];
}
return Z;
}
else
{
readMemoryAddress+=1;
for(int i=0;i<16;i++)
{
Z=Z+mainMemory[readMemoryAddress][i];
}
return Z;
}
}
public static void writeOutputToMemory(PCB pcb,String jobSegment,String result)
{
int k=0;
if(jobSegment==null)
{
LOADER.segmentFaultHandler(pcb,pcb.jobId+"2");
HashMap<Integer,List<Integer>> getPage=
DISK.diskManager.get(pcb.outputDataSegmentInformation);
int pageNumber=getPage.keySet().iterator().next(); 
LOADER.pageFaultHandler(pcb,pcb.outputDataSegmentInformation,pageNumber);
LinkedHashMap<Integer,List<Integer>> pageTable=
LOADER.pageTables.get(pcb.outputDataSegmentInformation);
for(Entry<Integer, List<Integer>> entry: pageTable.entrySet())
{
int page=entry.getKey();
if(page==pageNumber)
{
writeMemoryAddress=entry.getValue().get(0)*8;
writeOutputLength+=4;
LOADER.pageTables.get(pcb.outputDataSegmentInformation).get(page).set(2,1);
LOADER.pageTables.get(pcb.outputDataSegmentInformation).get(page)
.set(3,writeOutputLength);
}
}
for(int i=0;i<16;i++)
{
mainMemory[writeMemoryAddress][i]=result.charAt(k);
k++;
}
}
else
{
HashMap<Integer,List<Integer>> getPage=
DISK.diskManager.get(pcb.outputDataSegmentInformation);
int pageNumber=getPage.keySet().iterator().next();
writeMemoryAddress+=1;
for(int i=0;i<16;i++)
{
mainMemory[writeMemoryAddress][i]=result.charAt(k);
k++;
}
writeOutputLength+=4;
LOADER.pageTables.get(pcb.outputDataSegmentInformation).get(pageNumber)
.set(3,writeOutputLength);
}
}
}
