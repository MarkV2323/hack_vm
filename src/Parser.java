import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class is dedicated to crafting an object that allows for the processing of a VM file, one line at a time.
 * @author Mark Alan Vincent II
 * @version 2.0
 */
public class Parser {

    // private variables and objects
    private String arg1;
    private int arg2;
    private CommandType commandType;
    private Scanner inputFile;

    /**
     * Used for creating a new Parser type object.
     *
     * pre: pass a valid fileName that can be found.
     * post: Loads a inputStream from this file to read from.
     *
     * @param fileName The file's path. The file to be parsed with this object.
     */
    public Parser(String fileName) {
        // sets the inputFile scanner to load the fileName argument.
        try {
            this.inputFile = new Scanner(new File("src/" + fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Unable to locate file for translation. Exiting program.");
            System.exit(0);
        }

        // initializes variables to null types.
        this.arg1 = null;
        this.arg2 = -1;
        this.commandType = null;
    }

    /**
     * Handles parsing one line from the inputFile variable.
     * pre: Have a valid inputStream from fileName open.
     * post: parses values from the current line in the inputStream into the corresponding variables.
     */
    public void advance() {
        // Places the current line into an array of tokens, delimited by " " for each token.
        String[] tokens = inputFile.nextLine().trim().split(" ");

        // removes any comments
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].contains("//")) {
                tokens[i] = tokens[i].substring(0, tokens[i].indexOf('/'));
                tokens[i] = tokens[i].trim();
            }
        }

        /*
        Handles determining the commandType, and how to gather the rest of the data based on that.

        * Uses both arg1 and arg2
        Memory Access Commands - push, pop

        * Uses arg1, arg2 is -1 (considered null)
        Arithmetic and Logical Commands - add, sub, neg, eg, gt, lt, and, or, not

        * Uses arg1, arg2 is -1 (considered null)
        Program Flow Commands - label, goto, if-goto

        * Uses arg1, arg2 (return does not use arg1 or arg2)
        Function calling Commands - function, call, return
         */
        switch (tokens[0]) {
            case "push":
                commandType = CommandType.C_PUSH;
                arg1 = tokens[1];
                arg2 = Integer.valueOf(tokens[2]);
                break;
            case "pop":
                commandType = CommandType.C_POP;
                arg1 = tokens[1];
                arg2 = Integer.valueOf(tokens[2].trim());
                break;
            case "add":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "sub":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "neg":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "eq":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "gt":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "lt":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "and":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "or":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "not":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = tokens[0];
                arg2 = -1;
                break;
            case "label":
                commandType = CommandType.C_LABEL;
                arg1 = tokens[1];
                arg2 = -1;
                break;
            case "goto":
                commandType = CommandType.C_GOTO;
                arg1 = tokens[1];
                arg2 = -1;
                break;
            case "if-goto":
                commandType = CommandType.C_IF;
                arg1 = tokens[1];
                arg2 = -1;
                break;
            case "function":
                commandType = CommandType.C_FUNCTION;
                arg1 = tokens[1];
                arg2 = Integer.valueOf(tokens[2].trim());
                break;
            case "call":
                commandType = CommandType.C_CALL;
                arg1 = tokens[1];
                arg2 = Integer.valueOf(tokens[2].trim());
                break;
            case "return":
                commandType = CommandType.C_RETURN;
                arg1 = null;
                arg2 = -1;
                break;
            default:
                // ignore, may be whitespace/comment/unknown command
                commandType = null;
                arg1 = null;
                arg2 = -1;
                break;
        }

    }

    /**
     * Handles checking if the inputFile has anymore lines left to be parsed.
     * pre: Have an inputStream open from a fileName.
     * post: Checks if there are any more lines to read from in the fileName.
     * @return returns if the file has any lines remaining to be read.
     */
    public boolean hasMoreCommands() {
        return inputFile.hasNext();
    }


    /*
    Get methods for the private variables.
     */

    /**
     * pre: Have a valid value for arg1.
     * post: returns the value from arg1.
     * @return the arg1 variable.
     */
    public String getArg1() {
        return arg1;
    }

    /**
     * pre: Have a valid value for arg2.
     * post: returns the value from the arg2.
     * @return the arg2 variable
     */
    public int getArg2() {
        return arg2;
    }

    /**
     * pre: Have a valid commandType value for commandType.
     * post: returns the value from the commandType.
     * @return the commandType variable.
     */
    public CommandType getCommandType() {
        return commandType;
    }

}
