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
    int [] hashcodes = new int[len];
    for (int i=0 ; i < len ; i++) {
      Obj obj = values[i].asObj();
      objs[i] = obj;
      hashcodes[i] = obj.hashcode();
    }
    return new NeSetObj(objs, hashcodes);
  }
}
