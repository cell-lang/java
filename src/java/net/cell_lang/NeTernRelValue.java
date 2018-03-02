package net.cell_lang;


class NeTernRelValue extends ValueBase {
  ValueBase[] col1;
  ValueBase[] col2;
  ValueBase[] col3;

  public NeTernRelValue(ValueBase[] col1, ValueBase[] col2, ValueBase[] col3) {
    this.col1 = col1;
    this.col2 = col2;
    this.col3 = col3;
  }

  public boolean IsTernRel() {
    return true;
  }

  public int Size() {
    return col1.length;
  }

  public Value Arg1(int index) {
    return col1[index];
  }

  public Value Arg2(int index) {
    return col2[index];
  }

  public Value Arg3(int index) {
    return col3[index];
  }

  public Obj AsObj() {
    int len = col1.length;
    Obj[] col1 = new Obj[len];
    Obj[] col2 = new Obj[len];
    Obj[] col3 = new Obj[len];
    for (int i=0 ; i < len ; i++) {
      col1[i] = this.col1[i].AsObj();
      col2[i] = this.col2[i].AsObj();
      col3[i] = this.col3[i].AsObj();
    }
    return new NeTernRelObj(col1, col2, col3);
  }
}
