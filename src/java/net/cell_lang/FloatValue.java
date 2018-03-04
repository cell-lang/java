package net.cell_lang;


class FloatValue extends ValueBase {
  double value;

  public FloatValue(double value) {
    this.value = value;
  }

  public boolean isFloat() {
    return true;
  }

  public double asDouble() {
    return value;
  }

  public Obj asObj() {
    return new FloatObj(value);
  }
}
