package net.cell_lang;

import java.util.NoSuchElementException;


class NeBinRelValue extends ValueBase {
  ValueBase[] col1;
  ValueBase[] col2;
  boolean isMap;

  public NeBinRelValue(ValueBase[] col1, ValueBase[] col2, boolean isMap) {
    this.col1 = col1;
    this.col2 = col2;
    this.isMap = isMap;
  }

  public boolean IsBinRel() {
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

  public boolean IsRecord() {
    if (!isMap)
      return false;
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (!col1[i].IsSymb())
        return false;
    return isMap;
  }

  public Value Lookup(String field) {
    int len = col1.length;
    for (int i=0 ; i < len ; i++)
      if (col1[i].AsSymb() == field)
        return col1[i];
    throw new NoSuchElementException();
  }

  public Obj AsObj() {
    int len = col1.length;
    Obj[] col1 = new Obj[len];
    Obj[] col2 = new Obj[len];
    for (int i=0 ; i < len ; i++) {
      col1[i] = this.col1[i].AsObj();
      col2[i] = this.col2[i].AsObj();
    }
    return new NeBinRelObj(col1, col2, isMap);
  }
}
