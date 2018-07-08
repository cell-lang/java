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
      writer.write(Long.toString(data));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return Long.toString(data).length();
  }

  public ValueBase getValue() {
    return new IntValue(data);
  }

  //////////////////////////////////////////////////////////////////////////////

  static IntObj[] smallIntObjs = new IntObj[384];

  static {
    for (int i=0 ; i < 384 ; i++)
      smallIntObjs[i] = new IntObj(i - 128);
  }

  public static IntObj get(long value) {
    if (value >= -128 & value < 256) {
      IntObj obj = smallIntObjs[128 + (int) value];
      Miscellanea._assert(obj.data == value); //## REMOVE WHEN DONE
      return obj;
    }
    return new IntObj(value);
  }

  public static int compare(long x1, long x2) {
    return x1 == x2 ? 0 : (x1 < x2 ? -1 : 1);
  }
}
