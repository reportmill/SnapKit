/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.Scanner;

/**
 * This is an interface to the console class to provide static convenience methods.
 */
public class ConsoleIO {

    /**
     * Maps to System.out.print.
     */
    public static void print(Object anObj)
    {
        System.out.print(anObj);
        System.out.flush();
    }

    /**
     * Maps to System.out.println.
     */
    public static void println()
    {
        System.out.println();
        System.out.flush();
    }

    /**
     * Maps to System.out.println.
     */
    public static void println(Object anObj)
    {
        System.out.println(anObj);
        System.out.flush();
    }

    /**
     * Maps to System.err.println.
     */
    public static void printErr(Object anObj)  { System.err.println(anObj); }

    /**
     * Maps to Scanner(System.in).nextLine().
     */
    public static String readln(String prompt)
    {
        if (prompt != null && !prompt.isEmpty())
            print(prompt);
        if (prompt != null && !prompt.isEmpty() && !Character.isWhitespace(prompt.charAt(prompt.length()-1)))
            print(' ');
        String value = new Scanner(System.in).nextLine();
        println(value);
        return value;
    }

    /**
     * Maps to Scanner(System.in).nextInt().
     */
    public static int readInt(String prompt)
    {
        print(prompt);
        if (prompt != null && !prompt.isEmpty() && !Character.isWhitespace(prompt.charAt(prompt.length()-1)))
            print(' ');
        int value = new Scanner(System.in).nextInt();
        println(value);
        return value;
    }

    /**
     * Maps to Scanner(System.in).nextDouble().
     */
    public static double readDouble(String prompt)
    {
        print(prompt);
        if (prompt != null && !prompt.isEmpty() && !Character.isWhitespace(prompt.charAt(prompt.length()-1)))
            print(' ');
        double value = new Scanner(System.in).nextDouble();
        println(value);
        return value;
    }

    /**
     * Maps to Math.random() * limit.
     */
    public static double random()  { return Math.random(); }

    /**
     * Maps to Math.random() * limit.
     */
    public static int randomInt(int limit)  { return (int) (Math.random() * limit); }

    /**
     * Maps to Math.random * limit.
     */
    public static double randomDouble(double limit)  { return (int) (Math.random() * limit); }

    /**
     * Show object.
     */
    public static void show(Object anObj)
    {
        Console console = Console.getShared();
        console.show(anObj);
    }

    /**
     * IO class like Java 24+.
     */
    public static class IO {
        /**
         * Writes a string representation of the specified object to the system console and then flushes that console.
         */
        public static void print(Object obj)  { ConsoleIO.print(obj); }

        /**
         * Terminates the current line on the system console and then flushes that console..
         */
        public static void println()  { ConsoleIO.println(); }

        /**
         * Writes a string representation of the specified object to the system console, terminates the line and then flushes that console.
         */
        public static void println(Object obj)  { ConsoleIO.println(obj); }

        /**
         * Reads a single line of text from the system console.
         */
        public static String readln()  { return ConsoleIO.readln(null); }

        /**
         * Writes a prompt as if by calling print, then reads a single line of text from the system console.
         */
        public static String readln(String prompt)  { return ConsoleIO.readln(prompt); }
    }
}
