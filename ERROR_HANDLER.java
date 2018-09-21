/*
ERROR_HANDLER class :
This class handles all the warnings and errors that occur 
during execution of the job. 
In case of a warning, it writes the message into the output file 
and continues execution.
in case of an error,it writes the message into the output file
and halts the execution.
Error_Handler() is used to handle this functionality.
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ERROR_HANDLER {

public static void Error_Handler(int error)
{
switch(error)
{	
case 1:SYSTEM.terminatingMessage="Warning Message";
SYSTEM.errorMessage="Job Id is not provided,"+
"default value in ascending order is given";
break;
case 2:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Integer value out of range(-2^13 to (2^13)-1)";
break;
case 3:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid input. Only integers in "
	+ "range(-2^13 to (2^13)-1) are supported";
break;
case 4:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Input file not found";
break;
case 5:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Initial program counter not provided ";
break;
case 6:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid program counter";
break;
case 7:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="load address dosen't exist";
break;
case 8:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid memory address";
break;
case 9:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Program size not specified";
break;
case 10:SYSTEM.terminatingMessage="Warning Message";
SYSTEM.errorMessage="Trace bit not specified,by default 1 is given";
break;
case 11:SYSTEM.terminatingMessage="Warning Message";
SYSTEM.errorMessage="Bad Trace flag";
break;
case 12:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid hex format";
break;
case 13:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid loader format";
break;
case 14:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid opcode";
break; 
case 15:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Execution entered into infinite loop";
break;
case 16:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Stack underflow";
break;
case 17:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Stack overflow";
break;       
case 18:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid effective address";
break;
case 19:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Memory is full";
break;
case 20:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Output path does not exist";
break;
case 21:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Disk is full";
break;
case 22:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="more than one **INPUT";
break;
case 23:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="missing **JOB";
break;
case 24:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="missing **INPUT";
break;
case 25:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="missing **FIN";
break;
case 26:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="conflict between # of input words"
		+ "specified in the **JOB line and the # of input "
		+ "items given in the INPUT section";
break;
case 27:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="divide by zero";
break;
case 28:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="reading beyond the end of file";
break;
case 29:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="writing beyond the end of file";
break;
case 30:SYSTEM.terminatingMessage="Abnormal Termination";
SYSTEM.errorMessage="Invalid disk address";
break;
}
try
{
File output=new File("output_file.txt");
if(output.exists())
{
output.delete();
}
output.createNewFile();
FileWriter fileWriter=new FileWriter(output);
BufferedWriter outputWriter=new BufferedWriter(fileWriter);
outputWriter.write("JOB ID             :\t"+SYSTEM.jobId+"(HEX)");
outputWriter.newLine();
outputWriter.write("Error Message      :\t"+SYSTEM.errorMessage);
outputWriter.newLine();
outputWriter.write("Job Termination    :\t"+SYSTEM.terminatingMessage);
outputWriter.newLine();
outputWriter.write("System Clock       :\t"+SYSTEM.clock+"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Input/Output time  :\t"+SYSTEM.iotime+"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Execution time     :\t"+(
SYSTEM.clock-(SYSTEM.iotime+SYSTEM.segmentFaultTime+SYSTEM.pageFaultTime))+
"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Page Fault time    :\t"+SYSTEM.pageFaultTime+"(DECIMAL)");
outputWriter.newLine();
outputWriter.write("Segment Fault time :\t"+SYSTEM.segmentFaultTime
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
outputWriter.close(); 
}
catch(Exception e)
{
/*check if the output path exists - error*/
ERROR_HANDLER.Error_Handler(20);
System.exit(0);
}
}
}
