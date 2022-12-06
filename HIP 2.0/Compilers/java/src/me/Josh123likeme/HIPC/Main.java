package me.Josh123likeme.HIPC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		
		List<String> filesToCompile = new ArrayList<String>();
		
		if (Arrays.asList(args).contains("-a")) {
			
			File folder = new File(System.getProperty("user.dir"));
			
			for (File file : folder.listFiles()) {
				
				if (file.isDirectory()) continue;
				
				if (file.getName().split("\\.")[file.getName().split("\\.").length - 1].equals("txt")) {
					
					filesToCompile.add(file.getName());
					
				}
				
			}
			
		}
		else {
			
			for (int i = 0; i < args.length; i++) {
				
				filesToCompile.add(args[i]);
				
			}		
			
		}
		
		for (String file : filesToCompile) {
			
			List<String> lines = new ArrayList<String>();
			
			//read file
			try {
			      File myObj = new File(file);
			      Scanner myReader = new Scanner(myObj);
			      while (myReader.hasNextLine()) {
			    	  String data = myReader.nextLine();
			        
			    	  String fixedData = "";
			        
			    	  for (int i = 0; i < data.length(); i++) {
			        	
			    		  if (!(data.charAt(i) == 0 || data.charAt(i) == ' ')) fixedData += data.charAt(i);
			        	
			    	  }
			        
			    	  lines.add(fixedData);
			        
			      	}
			      	myReader.close();
			    } catch (FileNotFoundException e) {
			    	
			    	System.out.println("An error has occurred whilst finding the file");
			    	e.printStackTrace();
			    }

			String[] linesAsArray = new String[lines.size()];
			
			for (int i = 0; i < linesAsArray.length; i++) {
				
				linesAsArray[i] = lines.get(i);
				
			}
			
			//start compilation
			System.out.println("starting to compile " + file);
			
			String[] compiled = compile(linesAsArray);
			
			byte[] bytes = new byte[compiled.length];
			
			for (int i = 0; i < bytes.length; i++) {
				
				byte b = 0;
				
				for (int j = 0; j < 8; j++) {
					
					if (compiled[i].charAt(j) == '1') b += 1 << (7 - j);
				
				}
				
				bytes[i] = b;
				
			}
			
			//output compiled file
			try {
				
				OutputStream outputStream = new FileOutputStream(file + ".bin");
				
				outputStream.write(bytes);

				
			} catch (IOException e) {
				
				System.out.println("An error has occurred whilst creating the binary image");
				e.printStackTrace();
				
			}
			
			System.out.println("succesfully compiled " + file);
			
		}
			
	}
	
	static String[] compile(String[] lines)
    {

        List<String> compiled = new ArrayList<String>();

        HashMap<String, Integer> jumpDestinations = new HashMap<String, Integer>();

        for (String line : lines)
        {

            if (line.length() == 0) continue;

            String opcode = line.split(",")[0];

            String[] operands = new String[line.split(",").length - 1];

            for (int i = 0; i < operands.length; i++)
            {

                operands[i] = line.split(",")[i + 1];

            }

            String[] linesToAdd = new String[0];
            
            //we will have to do a second pass for the jump instructions, so for now we will just write the opcode, spacer and the label
            if (opcode.equals("JMP"))
            {

                compiled.add("00000010");
                compiled.add("JUMP");
                compiled.add(operands[0]);

            }
            else if (opcode.equals("JEQ"))
            {

                compiled.add("01000001");
                compiled.add("JUMP");
                compiled.add(operands[0]);

            }
            else if (opcode.equals("JLT"))
            {

                compiled.add("01000010");
                compiled.add("JUMP");
                compiled.add(operands[0]);

            }
            else if (opcode.equals("JMT"))
            {

                compiled.add("01000011");
                compiled.add("JUMP");
                compiled.add(operands[0]);

            }
            else if (opcode.equals("JNE"))
            {

                compiled.add("01000100");
                compiled.add("JUMP");
                compiled.add(operands[0]);

            }

            //this is for jump destinations, we will store the address of the instruction to jump to here
            if (line.charAt(0) == ':')
            {

                jumpDestinations.put(line.substring(1), compiled.size());

            }

            //opcode selection
            switch (opcode)
            {

                case "NOP": linesToAdd = compile00(operands); break;
                case "TRM": linesToAdd = compile01(operands); break;
                //we would have JMP here

                case "LLR": linesToAdd = compile10(operands); break;
                case "LMR": linesToAdd = compile11(operands); break;
                case "LAR": linesToAdd = compile12(operands); break;

                case "SRM": linesToAdd = compile20(operands); break;
                case "SRA": linesToAdd = compile21(operands); break;

                case "MRR": linesToAdd = compile30(operands); break;

                case "CMP": linesToAdd = compile40(operands); break;
                //we would have JEQ here
                //we would have JLT here
                //we would have JMT here
                //we would have JNE here

                case "ADD": linesToAdd = compile50(operands); break;
                case "SUB": linesToAdd = compile51(operands); break;

                case "AND": linesToAdd = compile60(operands); break;
                case "OOR": linesToAdd = compile61(operands); break;
                case "XOR": linesToAdd = compile62(operands); break;
                case "NOT": linesToAdd = compile63(operands); break;
                case "LSL": linesToAdd = compile64(operands); break;
                case "LSR": linesToAdd = compile65(operands); break;

            }

            for (String lineToAdd : linesToAdd)
            {

                compiled.add(lineToAdd);

            }

        }

        //second pass to correct jump instructions
        for (int i = 0; i < compiled.size(); i++)
        {

            if (compiled.get(i) != "JUMP") continue;

            String address = toBinary("#" + jumpDestinations.get(compiled.get(i + 1)), 16);

            compiled.set(i, address.substring(0,8));
            compiled.set(i + 1, address.substring(8));

        }
        
        String[] compiledAsArray = new String[compiled.size()];
        
        for (int i = 0; i < compiledAsArray.length; i++) {
        	
        	compiledAsArray[i] = compiled.get(i);
        	
        }

        return compiledAsArray;

    }
	
	//NOP
    static String[] compile00(String[] opcodes)
    {

        return new String[] { "00000000" };

    }

    //TRM
    static String[] compile01(String[] opcodes)
    {

        return new String[] { "00000001" };

    }

    //LLR
    static String[] compile10(String[] opcodes)
    {

        String value = toBinary(opcodes[0], 16);
        String register = toBinary(opcodes[1], 4);

        return new String[] { "00010000", value.substring(0,8), value.substring(8), "0000" + register };

    }

    //LMR
    static String[] compile11(String[] opcodes)
    {

        String register = toBinary(opcodes[0], 4);
        String address = toBinary(opcodes[1], 16);

        return new String[] { "00010001", address.substring(0, 8), address.substring(8), "0000" + register };

    }

    //LAR
    static String[] compile12(String[] opcodes)
    {

        return new String[] { "00010010", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //SRM
    static String[] compile20(String[] opcodes)
    {

        String register = toBinary(opcodes[0], 4);
        String address = toBinary(opcodes[1], 16);

        return new String[] { "00100000", "0000" + register, address.substring(0, 8), address.substring(8) };

    }

    //SRA
    static String[] compile21(String[] opcodes)
    {

        return new String[] { "00100001", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //MRR
    static String[] compile30(String[] opcodes)
    {

        return new String[] { "00110010", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //CMP
    static String[] compile40(String[] opcodes)
    {

        return new String[] { "01000000", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //ADD
    static String[] compile50(String[] opcodes)
    {

        return new String[] { "01010000", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //SUB
    static String[] compile51(String[] opcodes)
    {

        return new String[] { "01010001", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //AND
    static String[] compile60(String[] opcodes)
    {

        return new String[] { "01100000", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //OR
    static String[] compile61(String[] opcodes)
    {

        return new String[] { "01100001", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //XOR
    static String[] compile62(String[] opcodes)
    {

        return new String[] { "01100010", toBinary(opcodes[0], 4) + toBinary(opcodes[1], 4) };

    }

    //NOT
    static String[] compile63(String[] opcodes)
    {

        return new String[] { "01100011", "0000" + toBinary(opcodes[0], 4) };

    }

    //LSL
    static String[] compile64(String[] opcodes)
    {

        return new String[] { "01100100", "0000" + toBinary(opcodes[0], 4) };

    }

    //LSR
    static String[] compile65(String[] opcodes)
    {

        return new String[] { "01100101", "0000" + toBinary(opcodes[0], 4) };

    }
    
    static String toBinary(String value, int targetNumberOfBits)
    {

        String bits = "";


        //if binary
        if (value.charAt(0) == 'b')
        {

            for (int i = 1; i < value.length(); i++)
            {

                bits += value.charAt(i);

            }

        }

        //if denary
        else if (value.charAt(0) == '#')
        {

            int valAsPositiveInt = Integer.parseInt(value.substring(1));
            
            if (valAsPositiveInt < 0) valAsPositiveInt += Math.pow(2, targetNumberOfBits);
            
            for (int i = (int) Math.pow(2, targetNumberOfBits - 1); i >= 1; i /= 2) {
            	
            	if (valAsPositiveInt - i >= 0) {
            		
            		bits += "1";
            		
            		valAsPositiveInt -= i;
            		
            	}
            	else {
            		
            		
            		bits += "0";
            	}
            	
            }
            
        }

        //if hexadecimal
        else if (value.charAt(0) == '$')
        {
        	
            for (char letter : value.substring(1).toCharArray())
            {

                switch (letter)
                {

                    case '0': bits += "0000"; break;
                    case '1': bits += "0001"; break;
                    case '2': bits += "0010"; break;
                    case '3': bits += "0011"; break;
                    case '4': bits += "0100"; break;
                    case '5': bits += "0101"; break;
                    case '6': bits += "0110"; break;
                    case '7': bits += "0111"; break;
                    case '8': bits += "1000"; break;
                    case '9': bits += "1001"; break;
                    case 'A': bits += "1010"; break;
                    case 'B': bits += "1011"; break;
                    case 'C': bits += "1100"; break;
                    case 'D': bits += "1101"; break;
                    case 'E': bits += "1110"; break;
                    case 'F': bits += "1111"; break;

                }

            }

        }
        
        if (bits.length() < targetNumberOfBits) bits = String.format("%1$" + targetNumberOfBits + "s", bits).replace(' ', '0');

        return bits.substring(bits.length() - targetNumberOfBits);

    }
	
}
