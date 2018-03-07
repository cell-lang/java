package net.cell_lang;

import java.io.Writer;



class IntObj extends Obj {
  long value;

  IntObj(long value) {
    this.value = value;
  }

  public static IntObj get(long value) {
    if (value >= 0 & value < 256)
      return byteObjs[(int) value];
    return new IntObj(value);
  }

  public boolean isInt() {
    return true;
  }

  public boolean isInt(long value) {
    return this.value == value;
  }

  public long getLong() {
    return value;
  }

  public boolean isEq(Obj obj) {
    return obj.isInt(value);
  }

  public int hashCode() {
    return ((int) (value >> 32)) ^ ((int) value);
  }

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      Long obj = new Long(value);
      writer.write(obj.toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    Long obj = new Long(value);
    return obj.toString().length();
  }

  public ValueBase getValue() {
    return new IntValue(value);
  }

  protected int typeId() {
    return 1;
  }

  protected int internalCmp(Obj obj) {
    long otherValue = obj.getLong();
    return value == otherValue ? 0 : (value < otherValue ? 1 : -1);
  }

  static IntObj[] byteObjs = new IntObj[256];

  static {
    for (int i=0 ; i < byteObjs.length ; i++)
      byteObjs[i] = new IntObj(i);
  }
}
