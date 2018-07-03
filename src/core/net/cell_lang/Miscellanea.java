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
    return Builder.createTaggedObj(SymbTable.StringSymbId, IntArrayObjs.create(chars, count));

    // int len = str.length();
    // Obj[] chars = new Obj[len];
    // int count = 0;
    // int i = 0;
    // while (i < len) {
    //   int ch = Char.convertToUtf32(str, i);
    //   chars[count++] = IntObj.get(ch);
    //   i += Char.isSurrogatePair(str, i) ? 2 : 1;
    // }
    // return Builder.createTaggedObj(SymbTable.StringSymbId, IntArrayObjs.create(chars, count));
  }

  public static String objToStr(Obj str) {
    return null;
  }

  public static Obj fail() {
    printCallStack();
    System.exit(1);
    return null;
  }

  public static boolean exitOnSoftFail = false;

  public static RuntimeException softFail() {
    printCallStack();
    if (exitOnSoftFail)
      System.exit(1);
    throw new UnsupportedOperationException();
  }

  public static RuntimeException softFail(String msg) {
    if (exitOnSoftFail)
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

  //## REMOVE ONCE THE NEW CODE GENERATOR IS READY
  public static Obj hardFail() {
    printCallStack();
    System.exit(1);
    return null;
  }

  public static RuntimeException _hardFail() {
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
    return random.nextInt((int) max);
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

  public static long mantissa(double x) {
    throw implFail("_mantissa_() has not been implemented yet");
  }

  public static long decimalExponent(double x) {
    throw implFail("_dec_expr_() has not been implemented yet");
  }

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
    System.err.println("Call stack:\n");
    int size = stackDepth <= fnNamesStack.length ? stackDepth : fnNamesStack.length;
    for (int i=0 ; i < size ; i++)
      System.err.println("  " + fnNamesStack[i]);
    String outFnName = "debug" + File.separator + "stack-trace.txt";
    System.err.println("\nNow trying to write a full dump of the stack to " + outFnName);
    System.err.flush();
    try {
      FileOutputStream file = new FileOutputStream(outFnName);
      OutputStreamWriter writer = new OutputStreamWriter(file);
      for (int i=0 ; i < size ; i++)
        printStackFrame(i, writer);
      writer.write("\n");
      writer.flush();
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

  public static <T> void arrayCopy(T[] src, T[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void arrayCopy(int[] src, int[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void arrayCopy(long[] src, long[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int[] arrayAppend(int[] array, int count, int newValue) {
    Miscellanea._assert(count <= array.length);

    if (count == array.length) {
      int newLen = Math.max(32, (3 * count) / 2);
      int[] newArray = new int[newLen];
      arrayCopy(array, newArray, count);
      array = newArray;
    }

    array[count] = newValue;
    return array;
  }

  public static int[] array2Append(int[] array, int count, int val1, int val2) {
    Miscellanea._assert(2 * count <= array.length);

    if (array.length < 2 * (count + 1)) {
      int newLen = Math.max(64, 2 * ((3 * count) / 2));
      int[] newArray = new int[newLen];
      arrayCopy(array, newArray, 2 * count);
      array = newArray;
    }

    array[2 * count] = val1;
    array[2 * count + 1] = val2;

    return array;
  }

  public static int[] array3Append(int[] array, int count, int val1, int val2, int val3) {
    Miscellanea._assert(3 * count <= array.length);

    if (array.length < 3 * (count + 1)) {
      int newLen = Math.max(96, 3 * ((3 * count) / 2));
      int[] newArray = new int[newLen];
      arrayCopy(array, newArray, 3 * count);
      array = newArray;
    }

    int offset = 3 * count;
    array[offset] = val1;
    array[offset + 1] = val2;
    array[offset + 2] = val3;

    return array;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static void arrayReset(long[] array) {
    int len = array.length;
    for (int i=0 ; i < len ; i++)
      array[i] = 0;
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

  public static int hashcode(int n) {
    return n;
  }

  public static int hashcode(int n1, int n2) {
    return n1 ^ n2;
  }

  public static int hashcode(int n1, int n2, int n3) {
    return n1 ^ n2 ^ n3;
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
