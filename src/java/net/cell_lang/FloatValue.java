package net.cell_lang;


class FloatValue extends ValueBase {
  double value;

  public FloatValue(double value) {
    this.value = value;
  }

  public boolean IsFloat() {
    return true;
  }

  public double AsDouble() {
    return value;
  }

  public Obj AsObj() {
    return new FloatObj(value);
  }
}
