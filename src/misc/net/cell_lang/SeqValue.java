package net.cell_lang;


class SeqValue extends ValueBase {
  ValueBase[] values;

  public SeqValue(ValueBase[] values) {
    this.values = values;
  }

  public boolean isSeq() {
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
    return Builder.createSeq(objs);
  }
}
