package net.cell_lang;

import java.io.Writer;


class MasterSeqObj extends SeqObj {
  public int used;

  public void dump() {
    System.out.printf("items.length = %d, length = %d, used = %d\n", items.length, length, used);
  }

  public MasterSeqObj(Obj[] items, int length) {
     super(items, length);
    // for (int i=0 ; i < length ; i++)
    //   Miscellanea._assert(items[i] != null);
    this.used = length;
  }

  public MasterSeqObj(Obj[] items) {
    this(items, items.length);
  }

  public MasterSeqObj(long length) {
    super(length > 0 ? (int) length : 0);
    this.used = (int) length;
  }

  public Obj getItem(long idx) {
    if (idx < length)
      return items[(int) idx];
    else
      throw new IndexOutOfBoundsException();
  }

  public SeqOrSetIter getSeqOrSetIter() {
    return new SeqOrSetIter(items, 0, length-1);
  }

  public void initAt(long idx, Obj value) {
    Miscellanea._assert(idx >= 0 & idx < length);
    Miscellanea._assert(items[(int) idx] == null);
    items[(int) idx] = value;
  }

  public Obj getSlice(long first, long len) {
    //## DON'T I NEED TO CHECK THAT BOTH first AND len ARE NONNEGATIVE?
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new SliceObj(this, (int) first, (int) len);
  }

  public Obj append(Obj obj) {
    if (used == length && length + 1 < items.length) {
      items[length] = obj;
      return new SliceObj(this, 0, length+1);
    }
    else {
      Obj[] newItems = new Obj[length < 16 ? 32 : (3 * length) / 2];
      for (int i=0 ; i < length ; i++)
        newItems[i] = items[i];
      newItems[length] = obj;
      return new MasterSeqObj(newItems, length+1);
    }
  }

  public Obj concat(Obj seq) {
    Miscellanea._assert(seq != null);

    int seqLen = seq.getSize();
    int newLen = length + seqLen;

    if (used == length && newLen < items.length) {
//        SeqObj seqObj = (SeqObj) seq;
//        Array.copy(seqObj.items, seqObj.offset(), items, length, seqObj.length);
      for (int i=0; i < seqLen ; i++)
        items[length+i] = seq.getItem(i);
      used += seqLen;
      return new SliceObj(this, 0, newLen);
    }

    return super.concat(seq);
    // return new RopeObj(this, seq);
  }

  protected int offset() {
    return 0;
  }
}
