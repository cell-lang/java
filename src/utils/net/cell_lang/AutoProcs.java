package net.cell_lang;

import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


class AutoProcs {
  public static Obj Error_P(RelAutoBase automaton, RelAutoUpdaterBase updater, Object env) {
    Exception e = updater.lastException;
    String msg = "";
    if (e != null) {
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
    }
    return Conversions.stringToObj(msg);
  }

  public static Obj Save_P(Obj fnameObj, RelAutoBase automaton, RelAutoUpdaterBase updater, Object env) {
    String fname = fnameObj.getString();
    try {
      try (Writer writer = new BufferedWriter(new FileWriter(fname))) {
        automaton.writeState(writer);
      }
      return SymbObj.get(true);
    }
    catch (Exception e) {
      updater.lastException = e;
      return SymbObj.get(false);
    }
  }

  //## THIS IS IN THE WRONG PLACE...
  public static boolean load(Obj fnameObj, RelAutoBase automaton, RelAutoUpdaterBase updater) {
    String fname = fnameObj.getString();
    try {
      try (Reader reader = new FileReader(fname)) {
        automaton.loadState(reader);
      }
    }
    catch (Exception e) {
      updater.lastException = e;
      return false;
    }

    if (!automaton.fullCheck()) {
      updater.lastException = new RuntimeException("Invalid state");
      return false;
    }

    return true;
  }
}
