package net.cell_lang;

import java.io.Writer;



class FloatObj extends Obj {
  double value;

  public FloatObj(double value) {
    this.value = value;
  }

  public boolean isFloat() {
    return true;
  }

  public boolean isFloat(double value) {
    return this.value == value;
  }

  public double getDouble() {
    return value;
  }

  public boolean isEq(Obj obj) {
    return obj.isFloat(value);
  }

  public int hashCode() {
    return hashCode(value);
  }

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

  protected int typeId() {
    return 2;
  }

  protected int internalCmp(Obj other) {
    return compare(value, other.getDouble());
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int hashCode(double x) {
    long l = Double.doubleToLongBits(x);
    return ((int) (l >> 32)) ^ ((int) l);
  }

  public static int compare(double x1, double x2) {
    return x1 == x2 ? 0 : (x1 < x2 ? 1 : -1);
  }
}
