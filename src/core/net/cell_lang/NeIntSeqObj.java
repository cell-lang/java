package net.cell_lang;


abstract class NeIntSeqObj extends NeSeqObj {
  public Obj getObjAt(long idx) {
    return IntObj.get(getLongAt(idx));
  }

  public NeSeqObj append(Obj obj) {
    return obj.isInt() ? append(obj.getLong()) : super.append(obj);
  }

  public NeIntSeqObj append(long value) {
    return IntArrayObjs.append(this, value);
  }

  public SeqObj concat(Obj seq) {
    return seq instanceof NeIntSeqObj ? concat((NeIntSeqObj) seq) : super.concat(seq);
  }

  public NeIntSeqObj concat(NeIntSeqObj seq) {
    return IntArrayObjs.concat(this, seq);
  }

  public int internalOrder(Obj other) {
    if (other instanceof NeIntSeqObj) {
      Miscellanea._assert(getSize() == other.getSize());

      int len = getSize();
      for (int i=0 ; i < len ; i++) {
        long elt = getLongAt(i);
        long otherElt = other.getLongAt(i);
        if (elt != otherElt)
          return (int) (elt - otherElt);
      }
      return 0;
    }
    else
      return super.internalOrder(other);
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract void copy(int first, int count, long[] buffer, int destOffset);
}