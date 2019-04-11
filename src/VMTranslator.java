import java.util.Scanner;

/**
 * Driver program, handles taking name of VM file from the user to translate into ASM file.
 * Algorithm:
 * 1:
 * 2:
 * @author Mark Alan Vincent II
 * @version 2.0
 */
public class VMTranslator {

    // private objects used in main.
    private static CodeWriter writer;
    private static Parser parser;
    private static Scanner keyboard;

    // Projects main method.
    public static void main(String[] args) {

        // welcome message
        System.out.println("Please enter the name of the VM file you want me to translate into an ASM file.");

        // Loads in the files to each dedicated object tool.
        keyboard = new Scanner(System.in);
        // String fileRead  = takeInput(keyboard);
        String fileRead = "Sys.vm";
        String fileWrite = "NestedCall.asm";
        // String fileWrite = fileRead.substring(0, fileRead.indexOf('.')) + ".asm";

        parser = new Parser(fileRead);
        writer = new CodeWriter(fileWrite);

        // begins loop through VM file.
        while (parser.hasMoreCommands()) {
            // parses the current line.
            parser.advance();

            // DEBUG for printing current command from parser.
            // System.out.println(parser.getCommandType() + " " + parser.getArg1() + " " + parser.getArg2());

            // skips this iteration if the command type is to be ignore / null
            if (parser.getCommandType() == null) {
                continue;
            }

            /*
            selects type of writing method based on parser's commandType

            * Uses both arg1 and arg2
            Memory Access Commands - push, pop

            * Uses arg1, arg2 is -1 (considered null)
            Arithmetic and Logical Commands - add, sub, neg, eg, gt, lt, and, or, not

            * Uses arg1, arg2 is -1 (considered null)
            Program Flow Commands - label, goto, if-goto

            * Uses arg1, arg2 (return does not use arg1 or arg2)
            Function calling Commands - function, call, return
            */
            switch (parser.getCommandType()) {
                case C_PUSH:
                    writer.writePushPop(parser.getCommandType(), parser.getArg1(), parser.getArg2());
                    break;
                case C_POP:
                    writer.writePushPop(parser.getCommandType(), parser.getArg1(), parser.getArg2());
                    break;
                case C_ARITHMETIC:
                    writer.writeArithmetic(parser.getArg1());
                    break;
                case C_LABEL:
                    writer.writeLabel(parser.getArg1());
                    break;
                case C_GOTO:
                    writer.writeGoTo(parser.getArg1());
                    break;
                case C_IF:
                    writer.writeIf(parser.getArg1());
                    break;
                case C_FUNCTION:
                    writer.writeFunction(parser.getArg1(), parser.getArg2());
                    break;
                case C_CALL:
                    writer.writeCall(parser.getArg1(), parser.getArg2());
                    break;
                case C_RETURN:
                    writer.writeReturn();
                    break;
                default:
                    // ignore case, doesn't write.
                    break;
            }
        }

        // writes infinite loop to prevent noOp
        writer.writeEndLoop();

        // closes the writer
        writer.close();

        // END OF MAIN METHOD
    }

    // Method to get input from user.
    private static String takeInput(Scanner keyboard) {
        return keyboard.nextLine();
    }

    // END OF CLASS VMTranslator
}
