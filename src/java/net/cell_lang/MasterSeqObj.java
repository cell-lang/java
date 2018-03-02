package net.cell_lang;

import java.io.Writer;


class MasterSeqObj extends SeqObj {
  public int used;

  public void Dump() {
    System.out.printf("items.length = %d, length = %d, used = %d\n", items.length, length, used);
  }

  public MasterSeqObj(Obj[] items, int length) {
     super(items, length);
    // for (int i=0 ; i < length ; i++)
    //   Miscellanea.Assert(items[i] != null);
    this.used = length;
  }

  public MasterSeqObj(Obj[] items) {
    this(items, items.length);
  }

  public MasterSeqObj(long length) {
    super(length > 0 ? (int) length : 0);
    this.used = (int) length;
  }

  public Obj GetItem(long idx) {
    if (idx < length)
      return items[(int) idx];
    else
      throw new IndexOutOfBoundsException();
  }

  public SeqOrSetIter GetSeqOrSetIter() {
    return new SeqOrSetIter(items, 0, length-1);
  }

  public void InitAt(long idx, Obj value) {
    Miscellanea.Assert(idx >= 0 & idx < length);
    Miscellanea.Assert(items[(int) idx] == null);
    items[(int) idx] = value;
  }

  public Obj GetSlice(long first, long len) {
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new SliceObj(this, (int) first, (int) len);
  }

  public Obj Append(Obj obj) {
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

  public Obj Concat(Obj seq) {
    Miscellanea.Assert(seq != null);

    int seqLen = seq.GetSize();
    int newLen = length + seqLen;

    if (used == length && newLen < items.length) {
//        SeqObj seqObj = (SeqObj) seq;
//        Array.Copy(seqObj.items, seqObj.Offset(), items, length, seqObj.length);
      for (int i=0; i < seqLen ; i++)
        items[length+i] = seq.GetItem(i);
      used += seqLen;
      return new SliceObj(this, 0, newLen);
    }

    return super.Concat(seq);
    // return new RopeObj(this, seq);
  }

  protected int Offset() {
    return 0;
  }
}
