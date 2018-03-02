package net.cell_lang;


class NeSetValue extends ValueBase {
  ValueBase[] values;

  public NeSetValue(ValueBase[] values) {
    this.values = values;
  }

  public boolean IsSet() {
    return true;
  }

  public int Size() {
    return values.length;
  }

  public Value Item(int index) {
    return values[index];
  }

  public Obj AsObj() {
    int len = values.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = values[i].AsObj();
    return new NeSetObj(objs);
  }
}
