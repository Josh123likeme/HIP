using System;
using System.Collections.Generic;
using System.Collections;
using System.Linq;
using System.IO;

namespace HIPC_Compiler
{
    class Program
    {
        static void Main(string[] args)
        {

            string[] filesToCompile = null;

            if (args.Contains("-a"))
            {

                filesToCompile = Directory.GetFileSystemEntries(Directory.GetCurrentDirectory(), "*.txt", SearchOption.TopDirectoryOnly);

            }
            else
            {

                filesToCompile = new string[args.Length];

                for (int i = 0; i < filesToCompile.Length; i++)
                {

                    filesToCompile[i] = args[i];

                }

            }

            foreach (string filepath in filesToCompile)
            {

                string[] compiled = Compile(File.ReadAllLines(filepath));

                byte[] asBytes = new byte[compiled.Length];

                for (int i = 0; i < asBytes.Length; i++)
                {

                    asBytes[i] = ConvertStringToByte(compiled[i]);

                }

                File.WriteAllBytes("C-" + Path.GetFileName(filepath) + ".bin", asBytes);

            }

        }

        static string ToBinary(string value, int targetNumberOfBits)
        {

            string bits = "";


            //if binary
            if (value[0] == 'b')
            {

                for (int i = 1; i < value.Length; i++)
                {

                    bits += value[i];

                }

            }

            //if denary
            else if (value[0] == '#')
            {

                int valAsInt = int.Parse(value.Substring(1));

                var myBitArray = new BitArray(BitConverter.GetBytes(valAsInt));

                for (int i = targetNumberOfBits - 1; i >= 0; i--)
                {

                    bits += myBitArray[i] ? '1' : '0';

                }

            }

            //if hexadecimal
            else if (value[0] == '$')
            {

                foreach (char letter in value.Substring(1))
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

            if (bits.Length < targetNumberOfBits) return bits.PadLeft(targetNumberOfBits, '0');

            return bits.Substring(bits.Length - targetNumberOfBits);

        }

        private static byte ConvertStringToByte(string source)
        {
            byte result = 0;
            // This assumes the array never contains more than 8 elements!
            int index = 8 - source.Length;

            // Loop through the array
            foreach (char c in source)
            {
                // if the element is 'true' set the bit at that position
                if (c == '1')
                    result |= (byte)(1 << (7 - index));

                index++;
            }

            return result;

        }

        static string[] Compile(string[] lines)
        {

            List<string> compiled = new List<string>();

            Dictionary<string, int> jumpDestinations = new Dictionary<string, int>();

            foreach (string badLine in lines)
            {

                string line = badLine.Trim();

                if (line.Length == 0) continue;

                string opcode = line.Split(',')[0];

                string[] operands = new string[line.Split(',').Length - 1];

                for (int i = 0; i < operands.Length; i++)
                {

                    operands[i] = line.Split(',')[i + 1];

                }

                string[] linesToAdd = new string[0];

                //we will have to do a second pass for the jump instructions, so for now we will just write the opcode, spacer and the label
                if (opcode == "JMP")
                {

                    compiled.Add("00000010");
                    compiled.Add("JUMP");
                    compiled.Add(operands[0]);

                }
                else if (opcode == "JEQ")
                {

                    compiled.Add("01000001");
                    compiled.Add("JUMP");
                    compiled.Add(operands[0]);

                }
                else if (opcode == "JLT")
                {

                    compiled.Add("01000010");
                    compiled.Add("JUMP");
                    compiled.Add(operands[0]);

                }
                else if (opcode == "JMT")
                {

                    compiled.Add("01000011");
                    compiled.Add("JUMP");
                    compiled.Add(operands[0]);

                }
                else if (opcode == "JNE")
                {

                    compiled.Add("01000100");
                    compiled.Add("JUMP");
                    compiled.Add(operands[0]);

                }

                //this is for jump destinations, we will store the address of the instruction to jump to here
                if (line[0] == ':')
                {

                    jumpDestinations[line.Substring(1)] = compiled.Count;

                }

                //opcode selection
                switch (opcode)
                {

                    case "NOP": linesToAdd = Compile00(operands); break;
                    case "TRM": linesToAdd = Compile01(operands); break;
                    //we would have JMP here

                    case "LLR": linesToAdd = Compile10(operands); break;
                    case "LMR": linesToAdd = Compile11(operands); break;
                    case "LAR": linesToAdd = Compile12(operands); break;

                    case "SRM": linesToAdd = Compile20(operands); break;
                    case "SRA": linesToAdd = Compile21(operands); break;

                    case "MRR": linesToAdd = Compile30(operands); break;

                    case "CMP": linesToAdd = Compile40(operands); break;
                    //we would have JEQ here
                    //we would have JLT here
                    //we would have JMT here
                    //we would have JNE here

                    case "ADD": linesToAdd = Compile50(operands); break;
                    case "SUB": linesToAdd = Compile51(operands); break;

                    case "AND": linesToAdd = Compile60(operands); break;
                    case "OOR": linesToAdd = Compile61(operands); break;
                    case "XOR": linesToAdd = Compile62(operands); break;
                    case "NOT": linesToAdd = Compile63(operands); break;
                    case "LSL": linesToAdd = Compile64(operands); break;
                    case "LSR": linesToAdd = Compile65(operands); break;

                }

                foreach (string lineToAdd in linesToAdd)
                {

                    compiled.Add(lineToAdd);

                }

            }

            //second pass to correct jump instructions
            for (int i = 0; i < compiled.Count; i++)
            {

                if (compiled[i] != "JUMP") continue;

                string address = ToBinary("#" + jumpDestinations[compiled[i + 1]], 16);

                compiled[i] = address.Substring(0,8);
                compiled[i + 1] = address.Substring(8);

            }

            return compiled.ToArray();

        }

        //NOP
        static string[] Compile00(string[] opcodes)
        {

            return new string[] { "00000000" };

        }

        //TRM
        static string[] Compile01(string[] opcodes)
        {

            return new string[] { "00000001" };

        }

        //LLR
        static string[] Compile10(string[] opcodes)
        {
  
            string value = ToBinary(opcodes[0], 16);
            string register = ToBinary(opcodes[1], 4);

            return new string[] { "00010000", value.Substring(0,8), value.Substring(8), "0000" + register };

        }

        //LMR
        static string[] Compile11(string[] opcodes)
        {

            string register = ToBinary(opcodes[0], 4);
            string address = ToBinary(opcodes[1], 16);

            return new string[] { "00010001", address.Substring(0, 8), address.Substring(8), "0000" + register };

        }

        //LAR
        static string[] Compile12(string[] opcodes)
        {

            return new string[] { "00010010", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //SRM
        static string[] Compile20(string[] opcodes)
        {

            string register = ToBinary(opcodes[0], 4);
            string address = ToBinary(opcodes[1], 16);

            return new string[] { "00100000", "0000" + register, address.Substring(0, 8), address.Substring(8) };

        }

        //SRA
        static string[] Compile21(string[] opcodes)
        {

            return new string[] { "00100001", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //MRR
        static string[] Compile30(string[] opcodes)
        {

            return new string[] { "00110010", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //CMP
        static string[] Compile40(string[] opcodes)
        {

            return new string[] { "01000000", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //ADD
        static string[] Compile50(string[] opcodes)
        {

            return new string[] { "01010000", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //SUB
        static string[] Compile51(string[] opcodes)
        {

            return new string[] { "01010001", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //AND
        static string[] Compile60(string[] opcodes)
        {

            return new string[] { "01100000", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //OR
        static string[] Compile61(string[] opcodes)
        {

            return new string[] { "01100001", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //XOR
        static string[] Compile62(string[] opcodes)
        {

            return new string[] { "01100010", ToBinary(opcodes[0], 4) + ToBinary(opcodes[1], 4) };

        }

        //NOT
        static string[] Compile63(string[] opcodes)
        {

            return new string[] { "01100011", "0000" + ToBinary(opcodes[0], 4) };

        }

        //LSL
        static string[] Compile64(string[] opcodes)
        {

            return new string[] { "01100100", "0000" + ToBinary(opcodes[0], 4) };

        }

        //LSR
        static string[] Compile65(string[] opcodes)
        {

            return new string[] { "01100101", "0000" + ToBinary(opcodes[0], 4) };

        }

    }

}
