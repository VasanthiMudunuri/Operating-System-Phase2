/*
PCB class :
Every job in the system will be represented by a process control block.
generatePMT() is used to output PMT table for every 15vtu.
generateOutputFile() is used to send information into the output file.
clearMemoryAndDiskJobFrames() is used to clear the occupied page frames 
in memory and disk by the job.
*/
/*
GLOBAL VARIABLES :
'jobId' holds the value of the job id.
'programSegmentInformation' is the pointer to the PMT table of the program 
segment.
'inputDataSegmentInformation' is the pointer to the PMT table of the input 
data segment.
'outputDataSegmentInformation' is the pointer to the PMT table of the output 
data segment.
'jobPageFrames' contains the index of the allocated page frames in memory 
to job.
*/

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class PCB {
public int jobId;
public String programSegmentInformation;
public String inputDataSegmentInformation;
public String outputDataSegmentInformation;
public int[] jobPageFrames=new int[LOADER.numberOfJobPageFrames];
public void generatePMT(PCB pcb,BufferedWriter outputWriter)
{
try
{
outputWriter.write("Input-Data Segment      :\t"+
pcb.inputDataSegmentInformation);
outputWriter.newLine();
outputWriter.write("Page Map Table for Input-Data Segment"
+" and Input Data Segment :");
outputWriter.newLine();
if(LOADER.pageTables.get(pcb.inputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: 
LOADER.pageTables.get(pcb.inputDataSegmentInformation).entrySet())
{
outputWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputWriter.newLine();
for(int i=0;i<8;i++)
{
int memoryAddress=entry.getValue().get(0)*8+i;
outputWriter.write(MEMORY.mainMemory[memoryAddress]);
i++;
}
}
}
outputWriter.newLine();
outputWriter.write("Output-Data Segment     :\t"+
pcb.outputDataSegmentInformation);
outputWriter.newLine();
outputWriter.write("Page Map Table for Output-Data Segment and "+
"Output Data Segment :");
outputWriter.newLine();
if(LOADER.pageTables.get(pcb.outputDataSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: 
LOADER.pageTables.get(pcb.outputDataSegmentInformation).entrySet())
{
outputWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputWriter.newLine();
for(int i=0;i<8;i++)
{
int memoryAddress=entry.getValue().get(0)*8+i;
outputWriter.write(MEMORY.mainMemory[memoryAddress]);
outputWriter.write(" ");
}
}
}
outputWriter.newLine();
outputWriter.write("Page Map Table for Program Segment    :\t");
outputWriter.newLine();
if(LOADER.pageTables.get(pcb.programSegmentInformation)!=null)
{
for(Entry<Integer, List<Integer>> entry: 
LOADER.pageTables.get(pcb.programSegmentInformation).entrySet())
{
outputWriter.write("Page "+entry.getKey()+"-frame "+entry.getValue().get(0));
outputWriter.newLine();
}
}
}
catch(Exception e)
{
/*check if output path exists - error*/
ERROR_HANDLER.Error_Handler(20);
System.exit(0);
}
}
public void generateOutputFile(PCB pcb,BufferedWriter outputWriter)
{
try
{
outputWriter.write("Job Termination         :\t"+SYSTEM.terminatingMessage);
outputWriter.newLine();
outputWriter.write("System Clock            :\t"+Integer.toHexString
(SYSTEM.clock).toUpperCase()+"(HEX)");
outputWriter.newLine();
outputWriter.write("Input/Output time       :\t"+SYSTEM.iotime+"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Execution time          :\t"+
(SYSTEM.clock-(SYSTEM.iotime+SYSTEM.segmentFaultTime+SYSTEM.pageFaultTime))+
"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Page Fault time         :\t"+SYSTEM.pageFaultTime+
"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Segment Fault time      :\t"+SYSTEM.segmentFaultTime
+"(DECIMAL)");
outputWriter.newLine();
int memoryFrames=0;
int memoryWordLength=0;
for(Entry<String, LinkedHashMap<Integer, List<Integer>>> entry: 
LOADER.pageTables.entrySet())
{
LinkedHashMap<Integer, List<Integer>> pageTable = entry.getValue();
for(Entry<Integer, List<Integer>> entryValue: pageTable.entrySet())
{
memoryFrames+=1;
memoryWordLength=memoryWordLength+(entryValue.getValue().get(3)/4);
}
}
int diskFrames=0;
int diskWordLength=0;
outputWriter.write("Memory utilization both as a ratio and as a percentage :");
outputWriter.newLine();
outputWriter.write(memoryWordLength+"/256 words "+((memoryWordLength*100)/256)
+" percentage used");
outputWriter.newLine();
outputWriter.write(memoryFrames+"/32 frames "+((memoryFrames*100)/32)+
" percentage used");
outputWriter.newLine();
for(Entry<String, HashMap<Integer, List<Integer>>> entry: 
DISK.diskManager.entrySet())
{
HashMap<Integer, List<Integer>> pageTable=entry.getValue();
for(Entry<Integer, List<Integer>> entryValue: pageTable.entrySet())
{
diskFrames+=1;
diskWordLength=diskWordLength+(entryValue.getValue().get(1)/4);	
}
}
outputWriter.write("Disk utilization both as a ratio "
+"and as a percentage   :\t");
outputWriter.newLine();
outputWriter.write(diskWordLength+"/2048 words "+((diskWordLength*100)/2048)+
" percentage used");
outputWriter.newLine();
outputWriter.write(diskFrames+"/256 frames "+((diskFrames*100)/256)+
" percentage used");
outputWriter.newLine();
int memoryFragmentation=(24-((SYSTEM.jobSize%8)+SYSTEM.inputDataSegmentSize+
SYSTEM.outputDataSegmentSize));
outputWriter.write("Memory Fragmentation    :\t"+memoryFragmentation+
" words unused");
outputWriter.newLine();
int diskFragmentation=(24-((SYSTEM.jobSize%8)+SYSTEM.inputDataSegmentSize+
SYSTEM.outputDataSegmentSize));
outputWriter.write("Disk Fragmentation      :\t"+diskFragmentation+
" words unused");
outputWriter.newLine();
}
catch(Exception e)
{
/*check if output path exists - error*/
ERROR_HANDLER.Error_Handler(20);
System.exit(0);
}
}

public void clearMemoryAndDiskJobFrames(PCB pcb)
{
for(int i=0;i<pcb.jobPageFrames.length;i++)
{
MEMORY.freeMemoryBitVector[pcb.jobPageFrames[i]]=0;
}
for(int i=0;i<SYSTEM.availablePageFrames.length;i++)
{
DISK.freeDiskBitVector[SYSTEM.availablePageFrames[i]]=0;
}
if(pcb.programSegmentInformation!=null)
{
LOADER.pageTables.remove(pcb.programSegmentInformation);
DISK.diskManager.remove(pcb.programSegmentInformation);
}
if(pcb.inputDataSegmentInformation!=null)
{
LOADER.pageTables.remove(pcb.inputDataSegmentInformation);
DISK.diskManager.remove(pcb.inputDataSegmentInformation);
}
if(pcb.outputDataSegmentInformation!=null)
{
LOADER.pageTables.remove(pcb.outputDataSegmentInformation);
DISK.diskManager.remove(pcb.outputDataSegmentInformation);
}
}
}
