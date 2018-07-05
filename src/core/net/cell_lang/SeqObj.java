package net.cell_lang;


abstract class SeqObj extends Obj {
  public boolean getBoolAt(long idx) {
    return getObjAt(idx).getBool();
  }

  public long getLongAt(long idx) {
    return getObjAt(idx).getLong();
  }

  public double getDoubleAt(long idx) {
    return getObjAt(idx).getDouble();
  }

  public SeqObj append(boolean value) {
    return append(SymbObj.get(value));
  }
}