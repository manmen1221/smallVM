package pers.lyt.jdvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Vm {

	public static void main(String[] args) {
		Vm vm = new Vm();
		vm.control();
		// vm.readFF();
		// vm.printFFQ();
	}

	public String inputFilePath = "E:\\实验报告\\软件开发实践\\实验一\\新建文本文档.txt";
	public String binaryFilePath = "E:\\实验报告\\软件开发实践\\实验一\\字节码文件";
	private byte[] fFQ = new byte[128];
	private int pc = 0;
	private byte[][] z = new byte[32][4];
	private int sp = 0;

	Vm() {
		for (int i = 0; i < fFQ.length; i++) {
			fFQ[i] = ZhuJiFu.HALT.getByte();
		}
	}

	public void control() {//简易控制台指令

		Scanner input = new Scanner(System.in);
		String command;
		System.out.println("简单虚拟机已建立");
		printLine();
		for (;;) {
			String temp[] = input.next().split(" ");
			command = temp[0];
			switch (command) {
			case "help":
				printHelp();
				break;
			case "?":
				printHelp();
				break;
			case "rdfile":
				readFF();
				break;
			case "prtffq":
				printFFQ();
				break;
			case "prtffqbt":
				printFFQByte();
				break;
			case "prtwk":
				printWork();
				break;
			case "nextstep":
				nextStep();
				break;
			case "run":
				printWork();
				while(nextStep());
				break;
			case "pcreset":
				pc = 0;
				sp = 0;
				break;
			case "quit":
				System.out.println("虚拟机已结束！");
				return;
			default:
				break;
			}
		}

	}
	private void printWork() {//打印当前PC和SP
		printLine();
		System.out.println("当前PC=" + pc + "\n方法区：");
		for (int i = 0; i < 3; i++) {
			printpc(i);
		}
		System.out.println();
		System.out.println("当前SP=" + (sp - 1) + "\n(增长方式←)栈：");
		for (int i = 0; i < 4; i++) {
			printz(i);
		}
		printLine();

	}

	private void printz(int type) {//打印当前SP
		for (int i = sp - 1; i > sp - 4; i--) {
			if (i >= 0 && i <= z.length) {
				switch (type) {
				case 0:
					if (i == (sp - 1))
						System.out.print("↓");
					System.out.print("\t\t\t\t\t");
					break;
				case 1:
					byte[] temp = new byte[4];
					for (int j = 0; j < 4; j++) {
						temp[j] = z[i][j];
					}
					System.out.print(ReType.bytesToInt(temp) + "\t\t\t\t\t");
					break;
				case 2:
					for (int j = 0; j < 4; j++) {
						System.out.print(replace0(Integer.toBinaryString(ReType.bytesToInt(z[i][3 - j]))));
					}
					System.out.print("\t");
					break;
				case 3:
					System.out.print(i + "\t\t\t\t\t");
					break;
				default:
					break;
				}
			}
		}
		System.out.println();
	}

	private void printpc(int type) {//打印当前PC
		for (int i = pc - 1; i < pc + 6; i++) {
			if (i >= 0 && i <= fFQ.length) {
				switch (type) {
				case 0:
					if (i == pc)
						System.out.print("↓");
					System.out.print("\t\t");
					break;
				case 1:
					System.out.print(replace0(Integer.toBinaryString(ReType.bytesToInt(fFQ[i]))) + "\t");
					break;
				case 2:
					System.out.print(i + "\t\t");
					break;
				default:
					break;
				}
			}
		}
		System.out.println();
	}

	private void printHelp() {//打印帮助
		printLine();
		System.out.println("可识别的命令：\n" + "help\t\t查看可识别的指令\n" + "rdfile\t\t从预设文件读取\n" + "prtffq\t\t打印方法区\n"
				+ "prtffqbt\t以二进制显示指令打印方法区\n" + "prtwk\t\t打印当前栈和方法区指针\n" + "nextstep\t执行下一条\n" + "run\t\t完整演示执行\n"
				+ "pcreset\t\t重置pc\n" + "quit\t\t退出\n");
		printLine();
	}

	private void printLine() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
	}

	private void printFFQByte() {//打印方法区，指令按二进制显示
		printLine();
		System.out.println("当前方法区二进制：");
		for (int i = 0; i < fFQ.length; i++) {
			print(i, replace0(Integer.toBinaryString(ReType.bytesToInt(fFQ[i]))));
		}
		printLine();
	}

	private void printFFQ() {//打印方法区，指令按助记符显示
		printLine();
		System.out.println("当前方法区：");
		for (int i = 0; i < fFQ.length;) {
			ZhuJiFu zjf = ZhuJiFu.getZhuJiFu(fFQ[i]);
			print(i, zjf.name() + "\t");
			i++;
			if (ZhuJiFu.hasPara(zjf)) {
				for (int j = 0; j < 4; j++) {
					print(i + j, replace0(Integer.toBinaryString(ReType.bytesToInt(fFQ[i + j]))));
				}
				i += 4;
			}
		}
		printLine();
	}

	private String replace0(String str) {//对二进制字符串前面补零到8位
		String temp = "";
		for (int i = str.length(); i < 8; i++)
			temp += "0";
		temp += str;
		return temp;
	}

	private void print(int i, String bt) {//打印方法区时自动换行
		System.out.print(bt + "\t");
		if (((i + 1) % 8) == 0)
			System.out.println();
	}

	/*private void readFF() {//读文件并记录到方法区
		BufferedReader br;
		try {
			printLine();
			System.out.println("尝试读文件");
			br = new BufferedReader(new FileReader(inputFilePath));
			String line;
			while ((line = br.readLine()) != null) {
				String[] temp = line.split(" ");
				ZhuJiFu zjf = ZhuJiFu.getZhuJiFu(temp[0]);
				fFQ[pc] = zjf.getByte();
				pc++;
				if (ZhuJiFu.hasPara(zjf)) {
					byte[] byteTemp = ReType.intToBytes(Integer.parseInt(temp[1]));
					for (int i = 0; i < 4; i++) {
						fFQ[pc + i] = byteTemp[3 - i];
					}
					pc += 4;
				}
				if (zjf == ZhuJiFu.HALT)
					break;
			}
			pc = 0;
			br.close();
			System.out.println("已完成");
			printLine();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("错误！！");
		}
	}*/
	
	private void readFF() {
///////////////////////////////////////////////////////////////////////
		BufferedReader br;
		List<Byte> list=new ArrayList<Byte>();
		printLine();
		try {
			System.out.println("尝试读语言文件");
			br = new BufferedReader(new FileReader(inputFilePath));
			String line;
			while ((line = br.readLine()) != null) {
				String[] temp = line.split(" ");
				ZhuJiFu zjf = ZhuJiFu.getZhuJiFu(temp[0]);
				list.add(zjf.getByte());
				if (ZhuJiFu.hasPara(zjf)) {
					byte[] byteTemp = ReType.intToBytes(Integer.parseInt(temp[1]));
					for (int i = 0; i < 4; i++) {
						list.add(byteTemp[3 - i]);
					}
				}
				if (zjf == ZhuJiFu.HALT)
					continue;
			}
			br.close();
			System.out.println("已读语言");
		} catch (Exception e) {
			System.out.println("读语言错误！！");
		}
///////////////////////////////////////////////////////////////////////
		File target = new File(binaryFilePath);
		if (target.exists()) {
			System.out.println("字节码文件已存在");
			System.out.println("尝试删除已存在字节码文件");
			if(target.delete())
				System.out.println("删除成功");
			else
				System.out.println("删除失败");
	    }
        try {
        	System.out.println("尝试写字节码文件");
            if (target.createNewFile()){
            	FileWriter fileWriter = new FileWriter(target.getAbsoluteFile(), true);
                for (int i = 0; i <list.size(); i++) {
                    fileWriter.write(list.get(i).byteValue());
                }
                fileWriter.close();
                System.out.println("已写字节码文件");
            }
            else throw new Exception();
        } catch (Exception e) {
            System.out.println("写字节码错误！！");
        }
///////////////////////////////////////////////////////////////////////
        try {
        	System.out.println("尝试装载字节码");
        	FileInputStream in=new FileInputStream(target.getAbsoluteFile());
        	int count;
        	boolean time=true,broken=false;
        	while((count = in.read(fFQ))>0){
        		if(time)
        			System.out.println("已读取字节数："+count);
        		else {
        			System.out.println("溢出字节数："+count);
        			broken=true;
        		}
        		time=false;
        	}
        	if(broken) {
        		new Exception("方法区数据溢出").printStackTrace();
        		for (int i = 0; i < fFQ.length; i++) {
        			fFQ[i] = ZhuJiFu.HALT.getByte();
        		}
        		System.out.println("方法区已重置");
        		printLine();
        		in.close();
        		return;
        	}
        	in.close();
        }catch(Exception e) {
            System.out.println("装载字节码错误！！");
        }
        pc = 0;
		sp = 0;
        printLine();
///////////////////////////////////////////////////////////////////////
	}

	private void Load(byte[] address) {//Load指令
		int adrs = ReType.bytesToInt(address);
		for (byte i = 0; i < 4; i++) {
			z[sp][i] = fFQ[adrs + i];
		}
		sp++;
	}

	private void Store(byte[] address) {//Store指令
		int adrs = ReType.bytesToInt(address);
		for (byte i = 0; i < 4; i++) {
			fFQ[adrs + i] = z[sp - 1][i];
		}
		sp--;
	}

	private void Push(byte[] value) {//Push指令
		for (byte i = 0; i < 4; i++) {
			z[sp][i] = value[i];
		}
		sp++;
	}

	private byte[] Peek() {//取栈顶但是不删除
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++) {
			temp[i] = z[sp][i];
		}
		return temp;
	}

	private byte[] Pop() {//Pop指令
		sp--;
		return Peek();
	}

	private void Dup() {//Dup指令
		for (byte i = 0; i < 4; i++) {
			z[sp][i] = z[sp - 1][i];
		}
		sp++;
	}

	private void Swap() {//Swap指令
		for (byte i = 0; i < 4; i++) {
			byte temp = z[sp][i];
			z[sp][i] = z[sp - 1][i];
			z[sp - 1][i] = temp;
		}
	}

	private void Add() {//Add指令
		int temp1 = ReType.bytesToInt(Pop());
		int temp2 = ReType.bytesToInt(Pop());
		Push(ReType.intToBytes(temp2 + temp1));
	}

	private void Sub() {//Sub指令
		int temp1 = ReType.bytesToInt(Pop());
		int temp2 = ReType.bytesToInt(Pop());
		Push(ReType.intToBytes(temp2 - temp1));
	}

	private void Mul() {//Mul指令
		int temp1 = ReType.bytesToInt(Pop());
		int temp2 = ReType.bytesToInt(Pop());
		Push(ReType.intToBytes(temp2 * temp1));
	}

	private void Div() {//Div指令
		int temp1 = ReType.bytesToInt(Pop());
		int temp2 = ReType.bytesToInt(Pop());
		Push(ReType.intToBytes(temp2 / temp1));
	}

	private void Ifeq(byte[] offset) {//Ifeq指令
		if (ReType.bytesToInt(Pop()) == 0) {
			pc += ReType.bytesToInt(offset);
		}
	}

	private void Ifne(byte[] offset) {//Ifne指令
		if (ReType.bytesToInt(Pop()) != 0) {
			pc += ReType.bytesToInt(offset);
		}
	}

	private boolean nextStep() {//运行并显示下一步状态
		ZhuJiFu zjf = ZhuJiFu.getZhuJiFu(fFQ[pc]);
		pc++;
		switch (zjf) {
		case LOAD:
			Load(loadPara());
			break;
		case STORE:
			Store(loadPara());
			break;
		case PUSH:
			Push(loadPara());
			break;
		case POP:
			Pop();
			break;
		case DUP:
			Dup();
			break;
		case SWAP:
			Swap();
			break;
		case ADD:
			Add();
			break;
		case SUB:
			Sub();
			break;
		case MUL:
			Mul();
			break;
		case DIV:
			Div();
			break;
		case IFEQ:
			Ifeq(loadPara());
			break;
		case IFNE:
			Ifne(loadPara());
			break;
		case HALT:
			pc--;
			return false;
		case NOP:
			break;
		default:
			pc--;
			return false;
		}
		printWork();
		return true;
	}

	private byte[] loadPara() {//当运行到需要参数的指令时，调用此方法获取4bytes的参数
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++)
			temp[3 - i] = fFQ[pc + i];
		pc += 4;
		return temp;
	}
}

class ReType {//int和byte之间的转换
	public static int bytesToInt(byte[] value) {
		int int1 = value[0] & 0xff;
		int int2 = (value[1] & 0xff) << 8;
		int int3 = (value[2] & 0xff) << 16;
		int int4 = (value[3] & 0xff) << 24;
		return int1 | int2 | int3 | int4;
	}

	public static int bytesToInt(byte value) {
		int int1 = value & 0xff;
		return int1;
	}

	public static byte[] intToBytes(int value) {
		byte[] bytes = new byte[4];
		bytes[3] = (byte) (value >> 24);
		bytes[2] = (byte) (value >> 16);
		bytes[1] = (byte) (value >> 8);
		bytes[0] = (byte) (value);
		return bytes;
	}
}

enum ZhuJiFu {//助记符
	ERROR(-1, "ERROR"), LOAD(12, "LOAD"), STORE(1, "STORE"), PUSH(2, "PUSH"), POP(3, "POP"), DUP(4, "DUP"),
	SWAP(5, "SWAP"), ADD(6, "ADD"), SUB(7, "SUB"), MUL(8, "MUL"), DIV(9, "DIV"), IFEQ(10, "IFEQ"), IFNE(11, "IFNE"),
	HALT(0, "HALT"), NOP(13, "NOP");

	private byte value;
	private String name;

	ZhuJiFu(int value, String name) {
		this.value = toByte(value);
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public byte getByte() {
		return value;
	}

	private static byte toByte(int value) {
		byte[] bytes = ReType.intToBytes(value);
		return bytes[0];
	}

	public static ZhuJiFu getZhuJiFu(byte value) {
		switch (value) {
		case 12:
			return LOAD;
		case 1:
			return STORE;
		case 2:
			return PUSH;
		case 3:
			return POP;
		case 4:
			return DUP;
		case 5:
			return SWAP;
		case 6:
			return ADD;
		case 7:
			return SUB;
		case 8:
			return MUL;
		case 9:
			return DIV;
		case 10:
			return IFEQ;
		case 11:
			return IFNE;
		case 0:
			return HALT;
		case 13:
			return NOP;
		default:
			return ERROR;
		}
	}

	public static ZhuJiFu getZhuJiFu(int value) {
		return getZhuJiFu(toByte(value));
	}

	public static ZhuJiFu getZhuJiFu(String value) {
		switch (value) {
		case "LOAD":
			return LOAD;
		case "STORE":
			return STORE;
		case "PUSH":
			return PUSH;
		case "POP":
			return POP;
		case "DUP":
			return DUP;
		case "SWAP":
			return SWAP;
		case "ADD":
			return ADD;
		case "SUB":
			return SUB;
		case "MUL":
			return MUL;
		case "DIV":
			return DIV;
		case "IFEQ":
			return IFEQ;
		case "IFNE":
			return IFNE;
		case "HALT":
			return HALT;
		case "NOP":
			return NOP;
		default:
			return ERROR;
		}
	}

	public static boolean hasPara(ZhuJiFu zjf) {
		switch (zjf) {
		case LOAD:
			return true;
		case STORE:
			return true;
		case PUSH:
			return true;
		case IFEQ:
			return true;
		case IFNE:
			return true;
		default:
			return false;
		}
	}
}