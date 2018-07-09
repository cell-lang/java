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

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(Double.toString(getDouble()));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return Double.toString(getDouble()).length();
  }

  public ValueBase getValue() {
    return new FloatValue(getDouble());
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int compare(double x1, double x2) {
    return IntObj.compare(floatObjData(x1), floatObjData(x2));
  }
}
