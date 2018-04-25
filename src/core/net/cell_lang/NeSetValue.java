package net.cell_lang;


class NeSetValue extends ValueBase {
  ValueBase[] values;

  public NeSetValue(ValueBase[] values) {
    this.values = values;
  }

  public boolean isSet() {
    return true;
  }

  public int size() {
    return values.length;
  }

  public Value item(int index) {
    return values[index];
  }

  public Obj asObj() {
    int len = values.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = values[i].asObj();
    return new NeSetObj(objs);
  }
}
