package net.cell_lang;


class IntValue extends ValueBase {
  long value;

  public IntValue(long value) {
    this.value = value;
  }

  public boolean IsInt() {
    return true;
  }

  public long AsLong() {
    return value;
  }

  public Obj AsObj() {
    return IntObj.Get(value);
  }
}
