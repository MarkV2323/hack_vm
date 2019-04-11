import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * This class handles writing the ASM language to a file, from the type of VM command provided.
 * @author Mark Vincent II
 * @version 2.0
 */
public class CodeWriter {

    // private variables and objects
    private PrintWriter outputFile;
    private String fileName;
    private int numLabels;
    /**
     * Handles the creation and initialization of a new CodeWriter object.
     * @param fileName the file you want to write to. (will create a new file if need be)
     */
    public CodeWriter(String fileName){
        // Sets up outputFile to open a stream to the fileName argument.
        try {
            this.outputFile = new PrintWriter(new File("src/" + fileName));
        } catch (FileNotFoundException e) {
            System.out.println("I was unable to create " + fileName + ", exiting program.");
            System.exit(0);
        }

        this.fileName = fileName;
        this.numLabels = 0;
    }


    /**
     * Useful for informing the codeWriter object that a new VM file has started.
     *
     * pre: pass a valid fileName that codeWriter will now use.
     * post: Doesn't open a new stream (same ASM file), though variables will be named differently as a result.
     *
     * @param name the name of the new VM file.
     */
    public void setFileName(String name) {
        fileName = name;
    }

    /**
     * Writes the bootstrap code that initializes the VM. Will always be placed at the beginning of an ASM file.
     *
     * pre: Have a valid output stream open.
     * post: Writes the initial ASM code for VM programs.
     */
    public void writeInit() {
        // outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
        outputFile.write("@256\n" + "D=A\n" + "@SP\n" + "M=D\n"); // Does SP = 256.
        // call Sys.init
        outputFile.flush();
    }

    /**
     * Writes a function in ASM.
     *
     * pre: must have a unique name. all saving of prior frame done through call. creates own frame.
     * post: writes ASM to allocate LCL variables to 0. Creates calling label.
     *
     * @param functionName the name of the function.
     * @param numVars the number of local variables the function has.
     */
    public void writeFunction(String functionName, int numVars) {
        outputFile.write("(" + fileName + "." + functionName + ")\n"); // writes the label.
        outputFile.write("@SP\n" + "D=M\n" + "@LCL\n" + "M=D\n"); // sets up the new LCL pointer.
        outputFile.flush();
        // initializes the local variables to 0.
        for (int i = 0; i < numVars; i++) {
            outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=0\n"); // push operation
            outputFile.flush();
        }

    }

    /**
     * Calls a function that has been defined..
     *
     * pre: push arguments onto stack prior to making a call
     * post: saves the current frame onto the stack, jumps into the function.
     *
     * @param functionName the name of the function to call.
     * @param numArgs the number of arguments to give to the function.
     */
    public void writeCall(String functionName, int numArgs) {

//        // stores the arg value in temp15
        if (numArgs != 0) {
            outputFile.write("@" + numArgs + "\n" + "D=A\n"); //stores numArgs in D
            outputFile.write("@SP\n" + "A=M\n" + "A=A-D\n" + "D=A\n");
            outputFile.write("@15\n" + "M=D\n"); //stores @SP - numArgs in D
        } else {
            // stores current SP into temp15, will later be used as the pointer for ARG
            outputFile.write("@SP\n" + "D=M-1\n" + "@15\n" + "M=D\n");
        }

        // writes in a comment.
        outputFile.write("// call " + functionName + " with " + numArgs + "\n");

        // begins storing segments. returnAdd -> LCL -> ARG -> THIS -> THAT
        outputFile.write("@RETURN_ADDRESS_" + numLabels + "\n" + "D=A\n"); // gets returnAdd into D
        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n");      // push operation
        outputFile.flush();

        outputFile.write("@LCL\n" + "D=M\n");                                // gets LCL pointer into D
        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n");        // push operation
        outputFile.flush();

        outputFile.write("@ARG\n" + "D=M\n");                               // gets ARG pointer into D
        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n");       // push operation
        outputFile.flush();

        outputFile.write("@THIS\n" + "D=M\n");                               // gets THIS pointer into D
        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n");        // push operation
        outputFile.flush();

        outputFile.write("@THAT\n" + "D=M\n");                               // gets THAT pointer into D
        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n");        // push operation
        outputFile.flush();


        outputFile.write("@15\n" + "D=M\n" + "@ARG\n" + "M=D\n");           // sets ARG pointer to new ARG space.

        // Writes the jump to the function, also writes the return address label.
        // also increments the numLabels to keep a unique list of labels.
        outputFile.write("@" + fileName + "." + functionName + "\n");
        outputFile.write("0;JMP\n");
        outputFile.write("(" + "RETURN_ADDRESS_" + numLabels + ")\n");
        outputFile.flush();
        numLabels++;

    }

    /**
     * returns from the current function.
     * pre: ASM code is already inside of a valid function.
     * post: returns outside of the function.
     */
    public void writeReturn() {
        // copies returnValue into arg0
        outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
        outputFile.write("@ARG\n"); // points to current ARG pointer
        outputFile.write("A=M\n");  // sets address to ARG pointer value.
        outputFile.write("M=D\n");  // Stores contents of D into RAM[ARG]
        outputFile.flush();

        // gets caller's SP location and stores in temp15.
        outputFile.write("@ARG\n" + "A=M\n" + "A=A+1\n" + "D=A\n"); // points to ARG1, stores ARG1* in D.
        outputFile.write("@15\n" + "M=D\n"); // points to temp15, stores D in temp15.
        outputFile.flush();

        // stores the return address into temp14, and caller SP into temp14.
        // gets return address ([LCL* - 5]) into D, then stores on temp14
        outputFile.write("@LCL\n" + "A=M\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "D=M\n");
        outputFile.write("@14\n" + "M=D\n"); // points to temp14, stores D in temp14.

        // points SP to temp0(@15).
        outputFile.write("@15\n" + "D=M\n" + "@SP\n" + "M=D\n");

        // restores the segment pointers. THAT[LCL* -1] -> THIS[LCL* -2] -> ARG[LCL* -3] -> LCL[LCL* - 4]
        outputFile.write("@LCL\n" + "A=M\n" + "A=A-1\n" + "D=M\n"); // gets OG THAT into D
        outputFile.write("@THAT\n" + "M=D\n"); // stores OG THAT* into THAT*

        outputFile.write("@LCL\n" + "A=M\n" + "A=A-1\n" + "A=A-1\n" + "D=M\n"); // gets OG THIS into D
        outputFile.write("@THIS\n" + "M=D\n"); // stores OG THIS* into THIS*

        outputFile.write("@LCL\n" + "A=M\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "D=M\n"); // gets OG ARG into D
        outputFile.write("@ARG\n" + "M=D\n"); // stores OG ARG* into ARG*

        outputFile.write("@LCL\n" + "A=M\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "A=A-1\n" + "D=M\n"); // gets OG LCL into D
        outputFile.write("@LCL\n" + "M=D\n"); // stores OG LCL* into LCL*
        outputFile.flush();

        // jumps to temp14 (@14), or the return address.
        outputFile.write("@14\n" + "A=M\n" + "0;JMP\n");
        outputFile.flush();
    }

    /**
     * Writes the ASM code for a label.
     *
     * pre: Have a valid label to translate into ASM.
     * post: Writes the label in the ASM file.
     *
     * @param label the label to write.
     */
    public void writeLabel(String label) {
        outputFile.write("(" + label + ")\n"); // writes (label) in ASM. non unique.
        outputFile.flush();
    }


    /**
     * Writes the ASM code to implement a no condition branch towards a specific label.
     *
     * pre: a valid label within the function.
     * post: writes a jump in ASM to that label.
     *
     * @param label the label to jump to in ASM.
     */
    public void writeGoTo(String label) {
        outputFile.write("@" + label + "\n"); // points to label.
        outputFile.write("0;JMP\n"); // Jumps to label
        outputFile.flush();
    }

    /**
     * A conditional goTo statement, based off the current value on the stack, will jump to specified label.
     *
     * pre: pass a valid label to jump to.
     * post: will jump based on the item from the stack (pop), jump if stack pop == 0
     *
     * @param label the label to jump to.
     */
    public void writeIf(String label) {
        outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on stack into D
        outputFile.write("@" + label + "\n"); // points to label.
        outputFile.write("D;JNE\n"); // Jumps if D != 0, else if D == 0 continues execution.
        outputFile.flush();
    }

    /**
     * Handles writing ASM translation of specific VM arithmetic & logical commands
     *
     * pre: Pass in a valid type of Arithmetic command.
     * post: Will write the corresponding ASM code to perform the operation specified by the command.
     *
     * @param command the type of command to write.
     */
    public void writeArithmetic(String command) {
        // handles each arithmetic & logical command to be performed on the stack.
        // add, sub, neg, eq, gt, lt, and, or, not
        switch (command) {
            case "add":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M+D\n"); // pop operation on x2 AND D = x1 + x2
                outputFile.write("M=D\n"); // Saves operation on stack pointer's new address.
                outputFile.write("@SP\n" + "M=M+1\n"); // after the push, increments the SP address again
                outputFile.flush(); // writes
                break;
            case "sub":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M-D\n"); // pop operation on x2 AND D = x1 - x2
                outputFile.write("M=D\n"); // Saves operation on stack pointer's new address.
                outputFile.write("@SP\n" + "M=M+1\n"); // after the push, increments the SP address again
                outputFile.flush(); // writes
                break;
            case "neg":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("D=-D\n"); // D = -D negative
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                break;
            case "eq":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on y
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M-D\n"); // pop operation on x AND D = x - y
                outputFile.write("@TRUE." + numLabels + "\n"  + "D;JEQ\n"); // X = Y
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=0\n"); // push operation for 0 (false)
                outputFile.write("@ENDCOMP." + numLabels + "\n" + "0;JMP\n"); // Jumps to end
                outputFile.write("(TRUE." + numLabels + ")\n"); // TRUE label with unique tag
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=-1\n"); // push operation for -1 (true)
                outputFile.write("(ENDCOMP." + numLabels + ")\n");
                outputFile.flush(); // writes
                numLabels++;
                break;
            case "gt":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on y
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M-D\n"); // pop operation on x AND D = x - y
                outputFile.write("@TRUE." + numLabels + "\n" + "D;JGT\n"); // X > Y
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=0\n"); // push operation for 0 (false)
                outputFile.write("@ENDCOMP." + numLabels + "\n" + "0;JMP\n"); // Jumps to end
                outputFile.write("(TRUE." + numLabels + ")\n"); // TRUE label with unique tag
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=-1\n"); // push operation for -1 (true)
                outputFile.write("(ENDCOMP." + numLabels + ")\n"); // End label
                outputFile.flush(); // writes
                numLabels++;
                break;
            case "lt":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on y
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M-D\n"); // pop operation on x AND D = x - y
                outputFile.write("@TRUE." + numLabels + "\n" + "D;JLT\n"); // X < Y
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=0\n"); // push operation for 0 (false)
                outputFile.write("@ENDCOMP." + numLabels + "\n" + "0;JMP\n"); // Jumps to end
                outputFile.write("(TRUE." + numLabels + ")\n"); // TRUE label with unique tag
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=-1\n"); // push operation for -1 (true)
                outputFile.write("(ENDCOMP." + numLabels + ")\n"); // End label
                outputFile.flush(); // writes
                numLabels++;
                break;
            case "and":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=D&M\n"); // pop operation on x2 AND x1 AND x2 operation
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                outputFile.flush(); // writes
                break;
            case "or":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=D|M\n"); // pop operation on x2 AND x1 AND x2 operation
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                outputFile.flush(); // writes
                break;
            case "not":
                outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n");   // pop operation on x1
                outputFile.write("D=!D\n"); // D = -D negative
                outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                outputFile.flush(); // writes
                break;
            default:
                // ignore, error
                System.out.println("Problem with arithmetic or logical command, not found. Exiting Program.");
                outputFile.close();
                System.exit(0);
                break;
        }

    }


    /**
     *
     * the following is a list of the different segments
     * argument - RAM[2]
     * local - RAM[1] address = LCL + i, SP--, *addr = *SP
     * static - RAM[16] - RAM[255] These are the variables in ASM programs.
     * constant - Doesn't hold a mapped location in RAM. No pop function exist for constant.
     * this - RAM[3]
     * that - RAM[4]
     * pointer - both RAM[3] and RAM[4]
     * temp - RAM[5] - RAM[12]
     *
     * pre: Pass in specific arguments to make a valid push or pop command.
     * post: Writes ASM code based off the arguments provided.
     *
     * @param commandType the type of command to write.
     * @param segment the type of segment (location in RAM) to manipulate.
     * @param index the specific index of the segment provided.
     */
    public void writePushPop(CommandType commandType, String segment, int index) {
        // decides to perform a pop or push translation.
        // @sp -> AM=M+1 -> A=A-1 -> M=D will always push D register onto the stack (with increment)
        if (commandType == CommandType.C_PUSH) {
            switch (segment) {
                case "argument":
                    outputFile.write("@ARG\n"); // points to current ARG
                    writeIncrement(index, segment); // Increments to ARG specified by index
                    outputFile.write("D=M\n"); // Stores contents of RAM[(ARG + index)] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "local":
                    outputFile.write("@LCL\n"); // points to current LCL
                    writeIncrement(index, segment); // Increments to LCL specified by index
                    outputFile.write("D=M\n"); // Stores contents of RAM[(LCL + index)] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "static":
                    outputFile.write("@" + fileName + "." + index + "\n"); // points to @fileName.index
                    outputFile.write("D=M\n"); // Stores contents of RAM[fileName.index] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "constant":
                    // Loads constant value into D, than moves it onto the stack via stack pointer.
                    outputFile.write("@" + index + "\n" + "D=A\n"); // Load D register
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "this":
                    outputFile.write("@THIS\n"); // points to current THIS
                    writeIncrement(index, segment); // Increments to THIS specified by index
                    outputFile.write("D=M\n"); // Stores contents of RAM[(THIS + index)] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "that":
                    outputFile.write("@THAT\n"); // points to current THAT
                    writeIncrement(index, segment); // Increments to THAT specified by index
                    outputFile.write("D=M\n"); // Stores contents of RAM[(THAT + index)] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                case "pointer":
                    // pointer 0 (THIS), else pointer 1 (THAT)
                    if (index < 1) {
                        outputFile.write("@THIS\n"); // points to current THIS
                        outputFile.write("D=M\n"); // Stores contents of RAM[THIS] into D
                        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    } else {
                        outputFile.write("@THAT\n"); // points to current THAT
                        outputFile.write("D=M\n"); // Stores contents of RAM[THAT] into D
                        outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    }
                    outputFile.flush();
                    break;
                case "temp":
                    outputFile.write("@5\n"); // points to temp start, RAM[5]
                    writeIncrement(index+1, segment); // Increments to temp + specified by index
                    outputFile.write("D=M\n"); // Stores contents of RAM[(5 + index)] into D
                    outputFile.write("@SP\n" + "AM=M+1\n" + "A=A-1\n" + "M=D\n"); // push operation
                    outputFile.flush();
                    break;
                default:
                    // exit program, bad segment
                    System.out.println("Bad segment when translating VM line. Exiting program.");
                    close();
                    System.exit(0);
            }
        } else if (commandType == CommandType.C_POP) {
            // @sp -> AM=M-1 -> D=M will always pop off stack onto D register, and decrement stack pointer.
            switch (segment) {
                case "argument":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@ARG\n"); // points to current ARG
                    writeIncrement(index, segment); // Increments to ARG specified by index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[(ARG + index)]
                    outputFile.flush();
                    break;
                case "local":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@LCL\n"); // points to current LCL
                    writeIncrement(index, segment); // Increments to ARG specified by index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[(LCL + index)]
                    outputFile.flush();
                    break;
                case "static":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@" + fileName + "." + index + "\n"); // points to @fileName.index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[fileName.index]
                    outputFile.flush();
                    break;
                case "this":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@THIS\n"); // points to current THIS
                    writeIncrement(index, segment); // Increments to THIS specified by index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[(THIS + index)]
                    outputFile.flush();
                    break;
                case "that":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@THAT\n"); // points to current THAT
                    writeIncrement(index, segment); // Increments to THAT specified by index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[(THAT + index)]
                    outputFile.flush();
                    break;
                case "pointer":
                    if (index < 1) {
                        outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                        outputFile.write("@THIS\n"); // points to current THIS
                        outputFile.write("M=D\n"); // Stores contents of RAM[THIS] into D
                    } else {
                        outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                        outputFile.write("@THAT\n"); // points to current THAT
                        outputFile.write("M=D\n"); // Stores contents of RAM[THAT] into D
                    }
                    outputFile.flush();
                    break;
                case "temp":
                    outputFile.write("@SP\n" + "AM=M-1\n" + "D=M\n"); // pop operation
                    outputFile.write("@5\n"); // points to temp start, RAM[5]
                    writeIncrement(index+1, segment); // Increments to temp + specified by index
                    outputFile.write("M=D\n"); // Stores contents of D into RAM[(5 + index)]
                    outputFile.flush();
                    break;
                default:
                    // exit program, bad segment
                    System.out.println("Bad segment when translating VM line. Exiting program.");
                    close();
                    System.exit(0);
            }
        } else {
            System.out.println("Bad commandType found when attempting to translate. Exiting program.");
            close();
            System.exit(0);
        }
    }

    /**
     * Handles closing the printWriter stream.
     * pre: An open printWriter stream.
     * post: Closes the printWriter stream.
     */
    public void close() {
        outputFile.close();
    }

    // Method for writing an i amount of M=M+1's for finding *addresses
    private void writeIncrement(int i, String segment) {
        // first write uses the address of the A register
        if (i != 0 && !segment.equals("temp")) {
            outputFile.write("A=M+1\n");
        }
        for (int j = 1; j < i; j++) {
            outputFile.write("A=A+1\n");
        }

        if (i == 0) {
            outputFile.write("A=M\n");
        }
        outputFile.flush();
    }

    /**
     * Method for writing an infinite loop, to prevent no op slides.
     * pre: finished with writing to ASM file.
     * post: Closes off ASM file with an infinite loop for security measures.
     */
    public void writeEndLoop() {
        outputFile.write("(END.ALL.LOOP)\n");
        outputFile.write("@END.ALL.LOOP\n");
        outputFile.write("0;JMP\n");
        outputFile.flush(); // writes
    }

}
