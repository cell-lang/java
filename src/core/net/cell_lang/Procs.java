package net.cell_lang;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;


class Procs {
  public static Obj FileRead_P(Obj fname, Object env) {
    String fnameStr = fname.getString();
    try {
      byte[] content = Files.readAllBytes(Paths.get(fnameStr));
      Obj bytesObj = Builder.createSeqUnsigned(content);
      return Builder.createTaggedObj(SymbTable.JustSymbId, bytesObj);
    }
    catch (Exception e) {
      return SymbObj.get(SymbTable.NothingSymbId);
    }
  }

  public static Obj FileWrite_P(Obj fname, Obj data, Object env) {
    String fnameStr = fname.getString();
    byte[] bytes = data.getByteArray();
    try {
      Files.write(Paths.get(fnameStr), bytes);
      return SymbObj.get(SymbTable.TrueSymbId);
    }
    catch (Exception e) {
      return SymbObj.get(SymbTable.FalseSymbId);
    }
  }

  public static Obj FileAppend_P(Obj fname, Obj data, Object env) {
    String fnameStr = fname.getString();
    byte[] bytes = data.getByteArray();
    try {
      Files.write(Paths.get(fnameStr), bytes, StandardOpenOption.APPEND);
      return SymbObj.get(SymbTable.TrueSymbId);
    }
    catch (Exception e) {
      return SymbObj.get(SymbTable.FalseSymbId);
    }
  }

  public static void Print_P(Obj str, Object env) {
    System.out.print(str.getString());
    System.out.flush();
  }

  public static Obj GetChar_P(Object env) {
    int ch;
    try {
      ch = System.in.read();
    }
    catch (Exception e) {
      ch = -1;
    }
    if (ch != -1)
      return Builder.createTaggedObj(SymbTable.JustSymbId, IntObj.get(ch));
    else
      return SymbObj.get(SymbTable.NothingSymbId);
  }

  private static long startTicks = -1;
  public static Obj Ticks_P(Object env) {
    long ticks = System.currentTimeMillis();
    if (startTicks == -1)
      startTicks = ticks;
    return IntObj.get(ticks - startTicks);
  }

  public static void Exit_P(Obj code, Object env) {
    System.exit((int) code.getLong());
  }

  public static Obj Error_P(RelAutoBase automaton, RelAutoUpdaterBase updater, Object env) {
    Exception e = updater.lastException;
    if (e != null) {
      String msg;
      if (e instanceof KeyViolationException || e instanceof ForeignKeyViolationException) {
        msg = e.toString();
      }
      else {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        updater.lastException.printStackTrace(printWriter);
        printWriter.flush();
        msg = stringWriter.toString();
      }
      return Builder.createTaggedObj(SymbTable.JustSymbId, Conversions.stringToObj(msg));
    }
    else
      return SymbObj.get(SymbTable.NothingSymbId);
  }
}
