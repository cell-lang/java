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
    long longVal = Double.doubleToLongBits(value);
    return ((int) (longVal >> 32)) ^ ((int) longVal);
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
    double otherValue = other.getDouble();
    return value == otherValue ? 0 : (value < otherValue ? 1 : -1);
  }
}
