package net.cell_lang;

import java.util.IdentityHashMap;
import java.io.Writer;


class ObjPrinter implements ObjVisitor {
  Writer writer;
  int maxLineLen;
  boolean newLine = true;
  int indentLevel = 0;


  public ObjPrinter(Writer writer, int maxLineLen) {
    this.writer = writer;
    this.maxLineLen = maxLineLen;
  }



  //////////////////////////////////////////////////////////////////////////////

  public void taggedIntObj(int tag, long value) {
    try {
      if (tag == SymbObj.DateSymbId & isPrintableDate(value)) {
        int[] yearMonthDay = DateTime.getYearMonthDay((int) value);
        String str = String.format("`%d-%02d-%02d`", yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]);
        writer.write(str);
        return;
      }

      if (tag == SymbObj.TimeSymbId) {
        int days;
        long dayNsecs;

        if (value >= 0) {
          days = (int) (value / 86400000000000L);
          dayNsecs = value % 86400000000000L;
        }
        else {
          long revDayNsecs = value % 86400000000000L;
          if (revDayNsecs == 0) {
            days = (int) (value / 86400000000000L);
            dayNsecs = 0;
          }
          else {
            days = (int) (value / 86400000000000L) - 1;
            dayNsecs = 86400000000000L + revDayNsecs;
          }
        }

        if (isPrintableDate(days)) {
          int[] yearMonthDay = DateTime.getYearMonthDay((int) days);
          long secs = dayNsecs / 1000000000;
          long nanosecs = dayNsecs % 1000000000;
          String nsecsStr = "";
          if (nanosecs != 0) {
            nsecsStr = ".";
            int div = 100000000;
            while (div > 0 & nanosecs > 0) {
              nsecsStr += Character.toString((int) ('0' + nanosecs / div));
              nanosecs %= div;
              div /= 10;
            }
          }
          String str = String.format(
            "`%d-%02d-%02d %02d:%02d:%02d%s`",
            yearMonthDay[0], yearMonthDay[1], yearMonthDay[2],
            secs / 3600, (secs / 60) % 60, secs % 60, nsecsStr
          );
          writer.write(str);
          return;
        }
      }

      writer.write(SymbObj.idxToStr(tag));
      writer.write('(');
      writer.write(Long.toString(value));
      writer.write(')');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isPrintableDate(long days) {
    // The date has to be between `1582-10-15` and `9999-12-31`
    return days >= -141427 & days <= 2932896;
  }
}