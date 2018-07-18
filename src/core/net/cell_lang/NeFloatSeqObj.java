package net.cell_lang;


abstract class NeFloatSeqObj extends NeSeqObj {
  public Obj getObjAt(long idx) {
    return new FloatObj(getDoubleAt(idx));
  }

  public NeSeqObj append(Obj obj) {
    return obj.isFloat() ? append(obj.getDouble()) : super.append(obj);
  }

  public NeFloatSeqObj append(double value) {
    return FloatArrayObjs.append(this, value);
  }

  public SeqObj concat(Obj seq) {
    return seq instanceof NeFloatSeqObj ? concat((NeFloatSeqObj) seq) : super.concat(seq);
  }

  public NeFloatSeqObj concat(NeFloatSeqObj seq) {
    return FloatArrayObjs.concat(this, seq);
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract void copy(int first, int count, double[] buffer, int destOffset);
}