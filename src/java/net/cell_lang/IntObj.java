package net.cell_lang;

import java.io.Writer;



class IntObj extends Obj {
  long value;

  IntObj(long value) {
    this.value = value;
  }

  public static IntObj Get(long value) {
    if (value >= 0 & value < 256)
      return byteObjs[(int) value];
    return new IntObj(value);
  }

  public boolean IsInt() {
    return true;
  }

  public boolean IsInt(long value) {
    return this.value == value;
  }

  public long GetLong() {
    return value;
  }

  public boolean IsEq(Obj obj) {
    return obj.IsInt(value);
  }

  public int hashCode() {
    return ((int) (value >> 32)) ^ ((int) value);
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      Long obj = new Long(value);
      writer.write(obj.toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    Long obj = new Long(value);
    return obj.toString().length();
  }

  public ValueBase GetValue() {
    return new IntValue(value);
  }

  protected int TypeId() {
    return 1;
  }

  protected int InternalCmp(Obj obj) {
    long other_value = obj.GetLong();
    return value == other_value ? 0 : (value < other_value ? 1 : -1);
  }

  static IntObj[] byteObjs = new IntObj[256];

  static {
    for (int i=0 ; i < byteObjs.length ; i++)
      byteObjs[i] = new IntObj(i);
  }
}
