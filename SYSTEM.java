/*
SYSTEM class :
It is the main class that has the control of execution of entire project.
It reads the input job line by line, executes all the instructions and 
returns the output of the job.
It has been implemented according to the specifications given.
*/
/*
GLOBAL VARIABLES :
'inputJob' contains path of the input file passed as an argument 
which contains instructions in HEX.
'inputDataSegmentSize' to hold the number of inputs in the given job.
'outputDataSegmentSize' to hold the number of outputs expected in the 
given job.
'startingAddress' is the starting address indicating where the current 
job should be loaded.
'traceSwitch' is the trace flag.
'jodId' is the current job ID.
'initialProgramCounter' is the initial instruction of the current job
to be executed.
'jobSize' is the size of the current job.
'clock' is the System clock.
'iotime' is the input/output time.
'segmentFaultTime' to calculate segment fault time.
'pageFualtTime' to calculate page fault time.
'errorMessage' is the message to be displayed in case of an error.
'terminatingMessage' is the message indicating type of job termination 
whether abnormal or normal.
'availablePageFrames' holds the indexes of the available frames in Disk.
'readyQueue' to hold jobs that are ready to execute.
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

public class SYSTEM 
{
public static String inputJob = "";
public static int inputDataSegmentSize=0;
public static int outputDataSegmentSize=0;
public static int startingAddress;
public static String traceSwitch = "";
public static String jobId = "";
public static String initialProgramCounter = "";
public static int jobSize;
public static int clock = 0;
public static int iotime = 0;
public static int segmentFaultTime=0;
public static int pageFaultTime=0;
public static String errorMessage = "";
public static String terminatingMessage = "Normal Termination";
public static int[] availablePageFrames;
public static Queue<PCB> readyQueue=new LinkedList<PCB>(); 

public static void main(String[] args) throws Exception
{
SYSTEM.inputJob = args[0];
PCB pcb=new PCB();
try 
{
BufferedReader bufferedReader = new BufferedReader
(new FileReader(SYSTEM.inputJob));
String firstLine = bufferedReader.readLine();
String secondLine=bufferedReader.readLine();
String currentLine=null;
String pageContent="";
int index=0;
if(firstLine.contains("**JOB"))
{
String[] Job = firstLine.split(" ");
inputDataSegmentSize=Integer.parseInt(Job[1]);
outputDataSegmentSize=Integer.parseInt(Job[2]);
}
else
{
/*check for **JOB - error*/
ERROR_HANDLER.Error_Handler(23);
System.exit(0);
}
String[] jobDetails=secondLine.split(" ");
jobId=jobDetails[0];
if(jobId.isEmpty())
{
/*check for job id - warning*/
ERROR_HANDLER.Error_Handler(1);
jobId="01";
}
pcb.jobId=Integer.parseInt(jobId,16);
DISK.initialDiskFrameStatus(pcb.jobId);
DISK.createDiskTable(pcb.jobId+"0");
if(inputDataSegmentSize>0)
{
DISK.createDiskTable(pcb.jobId+"1");
}
DISK.createDiskTable(pcb.jobId+"2");
availablePageFrames=DISK.getIndexOfAvailablePageFramesInDisk();
if(availablePageFrames.length==0)
{
/*check if disk is full - error*/
ERROR_HANDLER.Error_Handler(21);
System.exit(0);
}
if(jobDetails[1].isEmpty())
{
/*check for load address - error*/
ERROR_HANDLER.Error_Handler(7);
System.exit(0);
}
startingAddress=Integer.parseInt(jobDetails[1],16);
initialProgramCounter=jobDetails[2];
if(initialProgramCounter.isEmpty())
{
/*check for initial program counter - error*/
ERROR_HANDLER.Error_Handler(5);
System.exit(0);
}
if(jobDetails[3].isEmpty())
{
/*check for job size - error*/
ERROR_HANDLER.Error_Handler(9);
System.exit(0);
}
jobSize=Integer.parseInt(jobDetails[3],16);
traceSwitch=jobDetails[4];
if(traceSwitch.isEmpty())
{
/*check for trace bit (default 1 is given)- warning*/
ERROR_HANDLER.Error_Handler(10);
traceSwitch="1";
}
if(!(traceSwitch.equals("1") || traceSwitch.equals("0")))
{
/*check for bad trace flag - error*/
ERROR_HANDLER.Error_Handler(11);
}
if(traceSwitch.equals("0"))
{
File traceFile=new File("trace_file.txt");
if(traceFile.exists())
{
traceFile.delete();
} 
}
while((currentLine=bufferedReader.readLine())!=null)
{
pageContent=pageContent+currentLine;
if(pageContent.length()==32)
{
DISK.savePageToDisk(pcb.jobId+"0",index,availablePageFrames[index],
pageContent);
index+=1;
pageContent="";
}
if(pageContent.length()!=32 && (pageContent.contains("**INPUT")
	|| pageContent.contains("**FIN")))
{
while((currentLine=bufferedReader.readLine())!=null)	
{
pageContent+=currentLine;
}
if(pageContent.contains("**INPUT"))
{
if((pageContent.split(Pattern.quote("INPUT"),-1).length-1)>=2)
{
/*check for more than one **INPUT - error*/
ERROR_HANDLER.Error_Handler(22);
System.exit(0);
}
if(!(pageContent.contains("**FIN")))
{
/*check for **FIN - error*/
ERROR_HANDLER.Error_Handler(25);
System.exit(0);
}
String programContent=pageContent.substring(0,pageContent.indexOf("**INPUT"));
DISK.savePageToDisk(pcb.jobId+"0",index,availablePageFrames[index],
programContent);
index+=1;
String inputContent=pageContent.substring(pageContent.indexOf('T')+1,
pageContent.indexOf("**FIN"));
if((inputContent.length()/4)!=SYSTEM.inputDataSegmentSize)
{
/*check for # of input words - error*/
ERROR_HANDLER.Error_Handler(26);
System.exit(0);
}
DISK.savePageToDisk(pcb.jobId+"1",index,availablePageFrames[index], 
inputContent);
index+=1;
pageContent="";
}
else
{
if(SYSTEM.inputDataSegmentSize>0 && !(pageContent.contains("**INPUT")))
{
/*check for **INPUT - error*/
ERROR_HANDLER.Error_Handler(24);
System.exit(0);	
}
if(!(pageContent.contains("**FIN")))
{
/*check for **FIN - error*/
ERROR_HANDLER.Error_Handler(25);
System.exit(0);
}
String programContent=pageContent.substring(0,pageContent.indexOf("**FIN"));
DISK.savePageToDisk(pcb.jobId+"0",index,availablePageFrames[index],
programContent);
index+=1;
pageContent="";	
}
DISK.savePageToDisk(pcb.jobId+"2",index,availablePageFrames[index],"");
index+=1;
}
}
bufferedReader.close();
} 
catch (Exception e) 
{
if (e instanceof FileNotFoundException) 
{
/*file not found - error*/
ERROR_HANDLER.Error_Handler(4);
System.exit(0);
}
}
MEMORY.initialMemoryPageFrameStatus();
LOADER.loader(pcb);
CPU.cpu(readyQueue,initialProgramCounter, traceSwitch);
}
}
