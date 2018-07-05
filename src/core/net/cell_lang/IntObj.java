package net.cell_lang;

import java.io.Writer;



class IntObj extends Obj {
  IntObj(long value) {
    data = value;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int extraData() {
    return intObjExtraData();
  }

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(Long.toString(value));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return Long.toString(value).length();
  }

  public ValueBase getValue() {
    return new IntValue(value);
  }

  //////////////////////////////////////////////////////////////////////////////

  static IntObj[] byteObjs = new IntObj[384];

  static {
    for (int i=0 ; i < 384 ; i++)
      byteObjs[i] = new IntObj(i - 128);
  }

  public static IntObj get(long value) {
    if (value >= -128 & value < 256)
      IntObj obj = byteObjs[128 + (int) value];
      Miscellanea._assert(obj.data == value); //## REMOVE WHEN DONE
      return obj;
    }
    return new IntObj(value);
  }
}
