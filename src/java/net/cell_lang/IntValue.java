package net.cell_lang;


class IntValue extends ValueBase {
  long value;

  public IntValue(long value) {
    this.value = value;
  }

  public boolean isInt() {
    return true;
  }

  public long asLong() {
    return value;
  }

  public Obj asObj() {
    return IntObj.get(value);
  }
}
