package net.cell_lang;

import java.io.Writer;


final class FloatObj extends Obj {
  public FloatObj(double value) {
    data = Double.longBitsToDouble(value);
    Miscellanea._assert(getDouble() == value); //## REMOVE AFTER TESTING
  }

  //////////////////////////////////////////////////////////////////////////////

  public int extraData() {
    return doubleObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(Double.toString(value));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return Double.toString(value).length();
  }

  public ValueBase getValue() {
    return new FloatValue(value);
  }
}
