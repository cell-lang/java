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

  public NeSeqObj append(boolean value) {
    return append(SymbObj.get(value));
  }

  public NeSeqObj append(long value) {
    return append(IntObj.get(value));
  }

  public NeSeqObj append(double value) {
    return append(new FloatObj(value));
  }
}