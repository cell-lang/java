package net.cell_lang;

import java.io.StringWriter;
import java.io.PrintWriter;


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
}
