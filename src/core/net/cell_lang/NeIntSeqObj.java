package net.cell_lang;


abstract class NeIntSeqObj extends NeSeqObj {
  public SeqObj concat(Obj seq) {
    if (seq instanceof NeIntSeqObj)
      return concat((NeIntSeqObj) seq);
    else
      return super.concat(seq);
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

  public abstract NeIntSeqObj concat(NeIntSeqObj seq);
  public abstract void copy(int first, int count, long[] buffer, int destOffset);
}