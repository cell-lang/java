package net.cell_lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;


class Miscellanea {
  public static void performProcessEndActions() {

  }

  public static Obj strToObj(String str) {
    int len = str.length();
    long[] chars = new long[len];
    int count = 0;
    for (int i=0 ; i < len ; i++) {
      int ch = str.codePointAt(i);
      chars[count++] = ch;
      if (ch > Character.MAX_VALUE)
        i++;
    }
    return Builder.createTaggedObj(SymbObj.StringSymbId, Builder.createSeq(chars, count));
  }

  public static String objToStr(Obj str) {
    return null;
  }

  public static Obj fail() {
    printCallStack();
    System.exit(1);
    return null;
  }

  public static boolean debugMode = false;
  public static boolean insideTransaction = false;

  public static RuntimeException softFail() {
    printCallStack();
    if (debugMode & !insideTransaction)
      System.exit(1);
    throw new UnsupportedOperationException();
  }

  public static RuntimeException softFail(String msg) {
    if (debugMode)
      System.err.println(msg);
    return softFail();
  }

  public static RuntimeException softFail(String msg, String varName, Obj obj) {
    System.err.println(msg);
    dumpVar(varName, obj);
    return softFail();
  }

  public static RuntimeException softFail(String msg, String var1Name, Obj obj1, String var2Name, Obj obj2) {
    System.err.println(msg);
    dumpVar(var1Name, obj1);
    dumpVar(var2Name, obj2);
    return softFail();
  }

  public static RuntimeException hardFail() {
    printCallStack();
    System.exit(1);
    return null;
  }

  public static RuntimeException implFail(String msg) {
    if (msg != null)
      System.err.println(msg + "\n");
    printCallStack();
    System.exit(1);
    return null;
  }

  public static RuntimeException internalFail() {
    return internalFail(null);
  }

  public static RuntimeException internalFail(Obj obj) {
    System.err.println("Internal error!\n");
    printCallStack();
    if (obj != null) {
      dumpVar("this", obj);
      System.out.printf("this.getClass().getSimpleName() = %s\n", obj.getClass().getSimpleName());
    }
    Exception e = new RuntimeException();
    e.printStackTrace();
    System.exit(1);
    return null;
  }

  public static void printAssertionFailedMsg(String file, int line, String text) {
    if (text == null)
      System.out.printf("\nAssertion failed. File: %s, line: %d\n\n\n", file, line);
    else
      System.out.printf("\nAssertion failed: %s\nFile: %s, line: %d\n\n\n", text, file, line);
  }

  public static void printFailReachedMsg(String file, int line) {
    System.out.printf("\nFail statement reached. File: %s, line: %d\n\n\n", file, line);
  }

  public static void dumpVar(String name, Obj obj) {
    try {
      String str = printedObjOrFilename(obj, true);
      System.out.printf("%s = %s\n\n", name, str);
    }
    catch (Exception e) {

    }
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  static Random random = new Random(0);

  public static long randNat(long max) {
    return random.nextInt(max > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) max);
  }

  static int nextUniqueNat = 0;
  public static long uniqueNat() {
    return nextUniqueNat++;
  }

  public static long getTickCount() {
    return System.currentTimeMillis();
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static long round(double x) {
    return (long) x;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int castLongToInt(long x) {
    if (x == (int) x)
      return (int) x;
    else
      throw softFail();
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  // public static void sort(long[] array) {
  //   Arrays.sort(array);
  // }

  // public static void sort(long[] array, int start, int end) {
  //   Arrays.sort(array, start, end);
  // }

  // public static int anyIndexOrEncodeInsertionPointIntoSortedArray(int[] array, int value) {
  //   return Arrays.binarySearch(array, value);
  // }

  // public static int anyIndexOrEncodeInsertionPointIntoSortedArray(int[] array, int start, int end, int value) {
  //   return Arrays.binarySearch(array, start, end, value);
  // }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void _assert(boolean cond) {
    _assert(cond, null);
  }

  public static void _assert(boolean cond, String message) {
    if (!cond) {
      System.out.println("Assertion failed" + (message != null ? ": " + message : ""));
      if (stackDepth > 0) {
        printCallStack();
      }
      else {
        Exception e = new Exception();
        StackTraceElement[] trace = e.getStackTrace();
        for (int i=trace.length-1 ; i >= 0 ; i--)
          System.err.println(trace[i].toString());
      }
      System.exit(1);
    }
  }

  public static void trace(boolean cond, String message) {
    if (!cond) {
      System.out.println("*** TRACE: " + message);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  static int      stackDepth = 0;
  static String[] fnNamesStack = new String[100];
  static Obj[][]  argsStack = new Obj[100][];

  public static void pushCallInfo(String fnName, Obj[] args) {
    if (stackDepth < 100) {
      fnNamesStack[stackDepth] = fnName;
      argsStack[stackDepth]    = args;
    }
    stackDepth++;
  }

  public static void popCallInfo() {
    stackDepth--;
  }

  static void printCallStack() {
    if (stackDepth == 0)
      return;
    System.err.println("\nCall stack:\n");
    int size = stackDepth <= fnNamesStack.length ? stackDepth : fnNamesStack.length;
    for (int i=0 ; i < size ; i++)
      System.err.println("  " + fnNamesStack[i]);
    String outFnName = "debug" + File.separator + "stack-trace.txt";
    String outJavaFnName = "debug" + File.separator + "java-stack-trace.txt";
    System.err.println("\nNow trying to write a full dump of the stack to " + outFnName);
    System.err.flush();
    try {
      FileOutputStream file = new FileOutputStream(outFnName);
      OutputStreamWriter streamWriter = new OutputStreamWriter(file);
      for (int i=0 ; i < size ; i++)
        printStackFrame(i, streamWriter);
      streamWriter.write("\n");
      streamWriter.flush();
      file.close();

      file = new FileOutputStream(outJavaFnName);
      PrintWriter printWriter = new PrintWriter(file);
      Exception e = new Exception();
      e.printStackTrace(printWriter);
      printWriter.write("\n");
      printWriter.flush();
      file.close();
    }
    catch (Exception e) {
      System.err.printf("Could not write a dump of the stack to %s. Did you create the \"debug\" directory?\n", outFnName);
    }
  }

  static void printStackFrame(int frameIdx, Writer writer) throws IOException {
    Obj[] args = argsStack[frameIdx];
    writer.write(fnNamesStack[frameIdx] + "(");
    if (args != null) {
      writer.write("\n");
      for (int i=0 ; i < args.length ; i++)
        printIndentedArg(args[i], i == args.length - 1, writer);
    }
    writer.write(")\n\n");
    writer.flush();
  }

  static void printIndentedArg(Obj arg, boolean isLast, Writer writer) throws IOException {
    String str = arg.isBlankObj() ? "<closure>" : printedObjOrFilename(arg, false);
    for (int i=0 ; i < str.length() ; i++) {
      if (i == 0 || str.charAt(i) == '\n')
        writer.write("  ");
      writer.write(str.charAt(i));
    }
    if (!isLast)
      writer.write(',');
    writer.write("\n");
    writer.flush();
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static long pack(int low, int high) {
    long slot = (((long) low) & 0xFFFFFFFFL) | (((long) high) << 32);
    // Miscellanea._assert(low(slot) == low & high(slot) == high);
    return slot;
  }

  public static int low(long slot) {
    return (int) (slot & 0xFFFFFFFFL);
  }

  public static int high(long slot) {
    return (int) (slot >>> 32);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  static ArrayList<Obj> filedObjs = new ArrayList<Obj>();

  static String printedObjOrFilename(Obj obj, boolean addPath) {
    String path = addPath ? "debug" + File.separator : "";

    for (int i=0 ; i < filedObjs.size() ; i++)
      if (filedObjs.get(i).isEq(obj))
        return String.format("<%sobj-%d.txt>", path, i);

    String str = obj.toString();
    if (str.length() <= 100)
      return str;

    String outFnName = String.format("debug%sobj-%d.txt", File.separator, filedObjs.size());

    try {
      try (PrintWriter out = new PrintWriter(outFnName)) {
        out.println(str);
      }
    }
    catch (Exception e) {

    }

    filedObjs.add(obj);
    return String.format("<%sobj-%d.txt>", path, filedObjs.size()-1);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int extend(int size, int minSize) {
    while (size < minSize)
      size *= 2;
    return size;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static boolean isHexDigit(byte b) {
    char ch = (char) b;
    return ('0' <= ch & ch <= '9') | ('a' <= ch & ch <= 'f') | ('A' <= ch & ch <= 'F');
  }

  public static int hexDigitValue(byte b) {
    char ch = (char) b;
    return ch - (ch >= '0' & ch <= '9' ? '0' : (ch >= 'a' & ch <= 'f' ? 'a' : 'A'));
  }

  public static int[] codePoints(String str) {
    int len = str.length();
    int[] codePoints = new int[len];
    int count = 0;
    for (int i=0 ; i < len ; i++) {
      int ch = str.codePointAt(i);
      codePoints[count++] = ch;
      if (ch > Character.MAX_VALUE)
        i++;
    }
    return count == len ? codePoints : Arrays.copyOf(codePoints, count);
  }

  public static boolean debugFlag = false;

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void writeIndentedNewLine(Writer writer, int level) {
    writeIndentedNewLine(writer, "", level);
  }

  public static void writeIndentedNewLine(Writer writer, String str, int level) {
    try {
      writer.write(str);
      writer.write("\n");
      for (int i=0 ; i < level ; i++)
        writer.write("  ");
    }
    catch (Exception e) {

    }
  }
}
