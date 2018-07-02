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
    return hashCode(value);
  }

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

  protected int typeId() {
    return staticTypeId;
  }

  protected int internalCmp(Obj obj) {
    return compare(value, obj.getLong());
  }

  //////////////////////////////////////////////////////////////////////////////

  static final int staticTypeId = 1;

  public static int hashCode(long n) {
    return ((int) (n >> 32)) ^ ((int) n);
  }

  public static int compare(long n1, long n2) {
    return n1 == n2 ? 0 : (n1 < n2 ? 1 : -1);
  }

  public static int compare(long n, Obj obj) {
    int objTypeId = obj.typeId();
    if (objTypeId == staticTypeId)
      return compare(n, obj.getLong());
    else
      return staticTypeId < objTypeId ? 1 : -1;
  }

  public static int compare(Obj obj, long n) {
    return -compare(n, obj);
  }

  static IntObj[] byteObjs = new IntObj[256];

  static {
    for (int i=0 ; i < byteObjs.length ; i++)
      byteObjs[i] = new IntObj(i);
  }
}
