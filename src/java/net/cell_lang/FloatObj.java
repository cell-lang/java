package net.cell_lang;

import java.io.Writer;



class FloatObj extends Obj {
  double value;

  public FloatObj(double value) {
    this.value = value;
  }

  public boolean IsFloat() {
    return true;
  }

  public boolean IsFloat(double value) {
    return this.value == value;
  }

  public double GetDouble() {
    return value;
  }

  public boolean IsEq(Obj obj) {
    return obj.IsFloat(value);
  }

  public int hashCode() {
    long longVal = Double.doubleToLongBits(value);
    return ((int) (longVal >> 32)) ^ ((int) longVal);
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      Double obj = new Double(value);
      writer.write(obj.toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    Double obj = new Double(value);
    return obj.toString().length();
  }

  public ValueBase GetValue() {
    return new FloatValue(value);
  }

  protected int TypeId() {
    return 2;
  }

  protected int InternalCmp(Obj other) {
    double other_value = other.GetDouble();
    return value == other_value ? 0 : (value < other_value ? 1 : -1);
  }
}
