/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;

/**
 * This is an interface to the console class to provide static convenience methods.
 */
public class ConsoleX {

    /**
     * Conveniences.
     */
    public static void print(Object anObj)  { System.out.print(anObj); }

    /**
     * Conveniences.
     */
    public static void println(Object anObj)  { System.out.println(anObj); }

    /**
     * Show object.
     */
    public static void show(Object anObj)
    {
        Console console = Console.getShared();
        console.show(anObj);
    }
}
