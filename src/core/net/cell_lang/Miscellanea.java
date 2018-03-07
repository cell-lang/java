package net.cell_lang;

import java.util.ArrayList;
import java.util.Random;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;


class Miscellanea {
  public static Obj strToObj(String str) {
    int len = str.length();
    Obj[] chars = new Obj[len];
    int count = 0;
    for (int i=0 ; i < len ; i++) {
      int ch = str.codePointAt(i);
      chars[count++] = IntObj.get(ch);
      if (ch > Character.MAX_VALUE)
        i++;
    }
    return new TaggedObj(SymbTable.StringSymbId, new MasterSeqObj(chars, count));

    // int len = str.length();
    // Obj[] chars = new Obj[len];
    // int count = 0;
    // int i = 0;
    // while (i < len) {
    //   int ch = Char.convertToUtf32(str, i);
    //   chars[count++] = IntObj.get(ch);
    //   i += Char.isSurrogatePair(str, i) ? 2 : 1;
    // }
    // return new TaggedObj(SymbTable.StringSymbId, new MasterSeqObj(chars, count));
  }

  public static String objToStr(Obj str) {
    return null;
  }

  public static Obj fail() {
    PrintCallStack();
    System.exit(1);
    return null;
  }

  public static RuntimeException softFail() {
    PrintCallStack();
    throw new UnsupportedOperationException();
  }

  public static Obj softFail(String msg) {
    System.err.println(msg);
    throw softFail();
  }

  public static Obj hardFail() {
    PrintCallStack();
    System.exit(1);
    return null;
  }

  public static void implFail(String msg) {
    if (msg != null)
      System.err.println(msg + "\n");
    PrintCallStack();
    System.exit(1);
  }

  public static void internalFail() {
    System.err.println("Internal error!\n");
    PrintCallStack();
    System.exit(1);
  }

  public static void printAssertionFailedMsg(String file, int line, String text) {
    if (text == null)
      System.out.printf("\nAssertion failed. File: %s, line: %d\n\n\n", file, line);
    else
      System.out.printf("\nAssertion failed: %s\nFile: %s, line: %d\n\n\n", text, file, line);
  }

  public static void dumpVar(String name, Obj obj) {
    try {
      String str = printedObjOrFilename(obj, true);
      System.out.printf("%s = %s\n\n", name, str);
    }
    catch (Exception e) {

    }
  }

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

  public static void _assert(boolean cond) {
    _assert(cond, null);
  }

  public static void _assert(boolean cond, String message) {
    if (!cond) {
      System.out.println("Assertion failed" + (message != null ? ": " + message : ""));
      if (stackDepth > 0) {
        PrintCallStack();
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

  static void PrintCallStack() {
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
        PrintStackFrame(i, writer);
      file.close();
      System.err.println("");
    }
    catch (Exception e) {
      System.err.printf("Could not write a dump of the stack to %s. Did you create the \"debug\" directory?\n", outFnName);
    }
  }

  static void PrintStackFrame(int frameIdx, Writer writer) {
    try {
      Obj[] args = argsStack[frameIdx];
      writer.write(fnNamesStack[frameIdx] + "(");
      if (args != null) {
        writer.write("\n");
        for (int i=0 ; i < args.length ; i++)
          PrintIndentedArg(args[i], i == args.length - 1, writer);
      }
      writer.write(")\n\n");
    }
    catch (Exception e) {

    }
  }

  static void PrintIndentedArg(Obj arg, boolean isLast, Writer writer) {
    try {
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
    catch (Exception e) {

    }
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
    if (str.length() <= 50)
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

  public static void arrayCopy(Obj[] src, Obj[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
  }

  public static void arrayCopy(int[] src, int[] dest, int count) {
    for (int i=0 ; i < count ; i++)
      dest[i] = src[i];
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

  // public static int[] CodePoints(String str) {
  //   int len = str.length();
  //   List<int> cps = new List<int>(len);
  //   for (int i=0 ; i < len ; i++) {
  //     cps.add(Char.convertToUtf32(str, i));
  //     if (Char.isHighSurrogate(str[i]))
  //       i++;
  //   }
  //   return cps.toArray();
  // }

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
