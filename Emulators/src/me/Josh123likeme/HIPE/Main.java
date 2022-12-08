package me.Josh123likeme.HIPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		
		if (args.length == 0) throw new IllegalArgumentException("you need to specify which image to emulate");
		
		if (args.length > 1) throw new IllegalArgumentException("you can only emulate one image at a time");
		
		//read file
		List<Byte> bytes = new ArrayList<Byte>();
		
		try {
		    // create a reader
		    FileInputStream fis = new FileInputStream(new File(System.getProperty("user.dir") + "\\" + args[0]));

		    // read one byte at a time
		    int ch;
		    while ((ch = fis.read()) != -1) {
		        bytes.add((byte) ch);
		    }

		    // close the reader
		    fis.close();

		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		String[] lines = new String[bytes.size()];
		
		for (int i = 0; i < lines.length; i++) {
			
			String line = "";
			
			for (int j = 0; j < 8; j++) {
				
				line += (bytes.get(i) >> (7 - j)) & 1;
				
			}
			
			lines[i] = line;
			
		}
		
		emulate(lines);
		
	}
	
	static void emulate(String[] bytes) {
		
		String[] registers = new String[16];
		
		//register preload
		for (int i = 0; i < registers.length; i++) {
			
			registers[i] = "0000000000000000";
			
		}
		
		String comparisonFlag = "EQ";
		
		int pc = 0;
		
		while (pc < bytes.length && !bytes[pc].equals("00000001")) {
			
			String current = bytes[pc];
			
			System.out.println(pc + ": " + current);
			
			//NOP
			if (current.equals("00000000")) {
				
				pc += 1;
				
			}
			
			//JMP
			else if (current.equals("00000010")) {
				
				pc = Integer.parseInt(bytes[pc + 1] + bytes[pc + 2], 2);
				
			}
			
			//LLR
			else if (current.equals("00010000")) {
				
				registers[Integer.parseInt(bytes[pc + 3], 2)] = bytes[pc + 1] + bytes[pc + 2];
				
				pc += 4;
				
			}
			
			//LMR
			else if (current.equals("00010001"));
			
			//LAR
			else if (current.equals("00010010"));
			
			//SRM
			else if (current.equals("00100000"));
			
			//SRA
			else if (current.equals("00100001"));
			
			//MRR
			else if (current.equals("00110000")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)] = registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)];
				
				pc += 2;
				
			}
			
			//CMP
			else if (current.equals("01000000")) {
				
				String value1 = registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)];
				String value2 = registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)];
				
				String result = binaryAdd(value1, binarySignFlip(value2));
				
				if (result.charAt(0) == '1') comparisonFlag = "LT";
				else if (result.contains("1")) comparisonFlag = "MT";
				else comparisonFlag = "EQ";
				
				pc += 2;
				
			}
			
			//JEQ
			else if (current.equals("01000001")) {
				
				if (comparisonFlag == "EQ") pc = Integer.parseInt(bytes[pc + 1] + bytes[pc + 2], 2);
				else pc += 3;
				
			}
			
			//JLT
			else if (current.equals("01000010")) {
				
				if (comparisonFlag == "LT") pc = Integer.parseInt(bytes[pc + 1] + bytes[pc + 2], 2);
				else pc += 3;
				
			}
			
			//JMT
			else if (current.equals("01000011")) {
				
				if (comparisonFlag == "MT") pc = Integer.parseInt(bytes[pc + 1] + bytes[pc + 2], 2);
				else pc += 3;
				
			}
			
			//JNE
			else if (current.equals("01000100")) {
				
				if (comparisonFlag == "LT" || comparisonFlag == "MT") pc = Integer.parseInt(bytes[pc + 1] + bytes[pc + 2], 2);
				else pc += 3;
				
			}
			
			//ADD
			else if (current.equals("01010000")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = 
						binaryAdd(registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)], registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]);
				
				pc += 2;
				
			}
			
			//SUB
			else if (current.equals("01010001")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = 
						binaryAdd(registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)], binarySignFlip(registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]));
				
				pc += 2;
				
			}
			
			//AND
			else if (current.equals("01100000")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = 
						binaryAnd(registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)], registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]);
				
				pc += 2;
				
			}
			
			//OOR
			else if (current.equals("01100001")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = 
						binaryOr(registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)], registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]);
				
				pc += 2;
				
			}
			
			//XOR
			else if (current.equals("01100010")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = 
						binaryXor(registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)], registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]);
				
				pc += 2;
				
			}
			
			//NOT
			else if (current.equals("01100011")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)] = binaryNot(registers[Integer.parseInt(bytes[pc + 1].substring(4,8), 2)]);
				
				pc += 2;
				
			}
			
			//LSL
			else if (current.equals("01100100")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = binaryShiftLeft(bytes[pc + 1].substring(0,4), Integer.parseInt(bytes[pc + 1].substring(4,8), 2));
				
				pc += 2;
				
			}
			
			//LSR
			else if (current.equals("01100101")) {
				
				registers[Integer.parseInt(bytes[pc + 1].substring(0,4), 2)] = binaryShiftRight(bytes[pc + 1].substring(0,4), Integer.parseInt(bytes[pc + 1].substring(4,8), 2));
				
				pc += 2;
				
			}
			
		}
		
		if (pc < bytes.length && bytes[pc].equals("00000001")) System.out.println("Hit a terminate instruction");
		else System.out.println("Reached the end of the instructions");
		
		//debug stuff
		System.out.println("\nRegister states:");
		System.out.println("R  Binary           Hex  Den");
		
		for (int i = 0; i < 16; i++) {
			
			System.out.println(denToHexAtoF(i) + ": "
					+ registers[i] + " "
					+ binToHex(registers[i], 4) + " " 
					+ Integer.parseInt(registers[i], 2));
			
		}
		
	}
	
	static String binaryAdd(String value1, String value2) {
		
		String resultBackwards = "";
		
		boolean carry = false;
		
		for (int i = 15; i >= 0; i--) {
			
			int numberOfOnes = carry ? 1 : 0;
			
			if (value1.charAt(i) == '1') numberOfOnes++;
			if (value2.charAt(i) == '1') numberOfOnes++;
			
			if (numberOfOnes == 1 || numberOfOnes == 3) resultBackwards += "1";
			else resultBackwards += "0";
				
			carry = numberOfOnes == 2 || numberOfOnes == 3 ? true : false;
			
		}
		
		String result = "";
		
		for (int i = 15; i >= 0; i--) {
			
			result += resultBackwards.charAt(i);
			
		}
		
		return result;
		
	}
	
	static String binarySignFlip(String value) {
		
		return binaryAdd(binaryNot(value), "0000000000000001");
		
	}
	
	static String binaryAnd(String value1, String value2) {
		
		String result = "";
		
		for (int i = 0; i < 16; i++) {
			
			result = value1.charAt(i) == value2.charAt(i) ? "1" : "0";
			
		}
		
		return result;
		
	}
	
	static String binaryOr(String value1, String value2) {
		
		String result = "";
		
		for (int i = 0; i < 16; i++) {
			
			result = value1.charAt(i) == '1' || value2.charAt(i) == '1' ? "1" : "0";
			
		}
		
		return result;
		
	}
	
	static String binaryXor(String value1, String value2) {
		
		String result = "";
		
		for (int i = 0; i < 16; i++) {
			
			result = value1.charAt(i) != value2.charAt(i) ? "1" : "0";
			
		}
		
		return result;
		
	}
	
	static String binaryNot(String value) {
		
		String result = "";
		
		for (int i = 0; i < 16; i++) {
			
			result += value.charAt(i) == '1' ? "0" : "1";
			
		}
		
		return result;
		
	}
	
	static String binaryShiftLeft(String value, int amount) {
		
		String result = "";
		
		for (int i = amount; i < 16; i++) {
			
			result += value.charAt(i);
			
		}
		
		for (int i = 0; i < amount; i++) {
			
			result += 0;
			
		}
		
		return result;
		
	}
	
	static String binaryShiftRight(String value, int amount) {
		
		String result = "";
		
		for (int i = 0; i < amount; i++) {
			
			result += value.charAt(0);
			
		}
		
		for (int i = amount; i < 16 - amount; i++) {
			
			result += value.charAt(i);
			
		}
		
		return result;
		
	}
	
	static String denToHexAtoF(int denary) {
		
		switch(denary) {
		
			case 0: return "0";
			case 1: return "1";
			case 2: return "2";
			case 3: return "3";
			case 4: return "4";
			case 5: return "5";
			case 6: return "6";
			case 7: return "7";
			case 8: return "8";
			case 9: return "9";
			case 10: return "A";
			case 11: return "B";
			case 12: return "C";
			case 13: return "D";
			case 14: return "E";
			case 15: return "F";
		
		}
		
		return "huh????";
		
	}
	
	static String binToHex(String binary, int numberOfCharacters) {
		
		String hex = "";
		
		for (int i = 0; i < numberOfCharacters; i++)
        {

            switch (binary.substring(i * 4, i * 4 + 4))
            {

                case "0000": hex += "0"; break;
                case "0001": hex += "1"; break;
                case "0010": hex += "2"; break;
                case "0011": hex += "3"; break;
                case "0100": hex += "4"; break;
                case "0101": hex += "5"; break;
                case "0110": hex += "6"; break;
                case "0111": hex += "7"; break;
                case "1000": hex += "8"; break;
                case "1001": hex += "9"; break;
                case "1010": hex += "A"; break;
                case "1011": hex += "B"; break;
                case "1100": hex += "C"; break;
                case "1101": hex += "D"; break;
                case "1110": hex += "E"; break;
                case "1111": hex += "F"; break;

            }

        }
		
		return hex;
		
	}
	
}
