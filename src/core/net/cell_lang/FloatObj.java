package net.cell_lang;

import java.io.Writer;


final class FloatObj extends Obj {
  public FloatObj(double value) {
    data = floatObjData(value);
    extraData = floatObjExtraData();
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  @Override
  public int hashcode() {
    return hashcode(getDouble());
  }

  public TypeCode getTypeCode() {
    return TypeCode.FLOAT;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(FloatObjPrinter.print(getDouble()));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return FloatObjPrinter.printSize(getDouble());
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int compare(double x1, double x2) {
    return IntObj.compare(floatObjData(x1), floatObjData(x2));
  }

  public static int hashcode(double x) {
    return Hashing.hashcode64(Double.doubleToRawLongBits(x));
  }
}


//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

class FloatObjPrinter {
  static char[] chars = new char[256]; //## MTBUG

  public static int printSize(double value) {
    if (Double.isNaN(value))
      return 3;

    if (Double.isInfinite(value))
      return value > 0 ? 8 : 9;

    if (value == 0.0)
      return 3;

    if (value == -0.0)
      return 4;

    return print(value, chars);
  }

  public static String print(double value) {
    if (Double.isNaN(value))
      return "NaN";

    if (Double.isInfinite(value))
      return value > 0 ? "Infinity" : "-Infinity";

    if (value == 0.0)
      return "0.0";

    if (value == -0.0)
      return "-0.0";

    int len = print(value, chars);
    return new String(chars, 0, len);
  }

  private static int print(double value, char[] chars) {
    int offset = 0;

    if (value < 0) {
      chars[0] = '-';
      value = -value;
      offset++;
    }

    int exponent = 0;
    while (value >= 1e18) {
      value /= 10.0;
      exponent++;
    }

    while (value <= 1e17) {
      value *= 10.0;
      exponent--;
    }

    long intValue = (long) value;

    if (intValue % 100000 > 99000)
      intValue += 1000;
    else
      intValue += 500;

    intValue = intValue / 1000;
    exponent += 3;

    while (intValue % 10 == 0) {
      intValue /= 10;
      exponent++;
    }

    if (exponent == 0) {
      int idx = printInteger(intValue, chars, offset);
      chars[idx++] = '.';
      chars[idx++] = '0';
      return idx;
    }

    int digits = numOfDigits(intValue);

    if (exponent < 0) {
      int absExp = -exponent;

      // 0.00000123456 -> 123456 * 10 ^ -11

      // 12345.6             1      0       7     -exponent < digits
      // 1234.56             2      0       7     -exponent < digits
      // 123.456             3      0       7     -exponent < digits
      // 12.3456             4      0       7     -exponent < digits
      // 1.23456             5      0       7     -exponent < digits
      // 0.123456            6      0       8
      // 0.0123456           7      1       9
      // 0.00123456          8      2       10
      // 0.000123456         9      3       11
      // 0.0000123456        10     4       12
      // 0.00000123456       11     5       13
      // 0.000000123456      12     6       14
      // 0.0000000123456     13     7       15

      if (absExp < digits)
        return printNumberWithDot(intValue, digits - absExp, chars, offset);

      int extraZeros = absExp - digits; // Number of zeros right after the dot
      int length = digits + 2 + extraZeros;

      if (extraZeros <= 2 || (extraZeros <= 8 && length <= 16)) {
        chars[offset++] = '0';
        chars[offset++] = '.';
        for (int i=0 ; i < extraZeros ; i++)
          chars[offset++] = '0';
        return printInteger(intValue, chars, offset);
      }
    }

    // Default processing using the exponential notation
    // The exponent is either > 0 or << 0
    exponent += digits - 1;
    if (digits > 1)
      offset = printNumberWithDot(intValue, 1, chars, offset);
    else
      offset = printInteger(intValue, chars, offset);
    chars[offset++] = 'e';
    if (exponent < 0) {
      chars[offset++] = '-';
      exponent = -exponent;
    }
    return printInteger(exponent, chars, offset);
  }

  private static int printInteger(long value, char[] chars, int idx) {
    int digits = numOfDigits(value);
    int i = idx + digits - 1;
    do {
      chars[i--] = (char) ('0' + value % 10);
      value /= 10;
    } while (value > 0);
    return idx + digits;
  }

  private static int printNumberWithDot(long value, int left, char[] chars, int offset) {
    int digits = numOfDigits(value);
    int dotIdx = offset + left;
    int lastIdx = offset + digits;
    for (int i=lastIdx ; i >= offset ; i--)
      if (i != dotIdx) {
        chars[i] = (char) ('0' + value % 10);
        value /= 10;
      }
      else
        chars[i] = '.';
    return lastIdx + 1;
  }

  private static int numOfDigits(long value) {
    if (value < 10)
      return 1;

    int digits = 1;
    do {
      value = value / 10;
      digits++;
    } while (value >= 10);
    return digits;
  }
}
