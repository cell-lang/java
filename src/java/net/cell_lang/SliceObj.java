package net.cell_lang;

import java.io.Writer;


class SliceObj extends SeqObj {
  MasterSeqObj master;

  public void dump() {
    System.out.printf("offset = %d, length = %d\n", offset, length);
    System.out.printf(
      "master: items.length = %d, length = %d, used = %d",
      master.items.length, master.length, master.used
    );
    Miscellanea._assert(items == master.items);
  }

  public SliceObj(MasterSeqObj master, int offset, int length) {
    super(master.items, offset, length);
    // for (int i=0 ; i < offset+length ; i++)
    //   Miscellanea._assert(master.items[i] != null);
    this.master = master;
  }

  public SeqOrSetIter getSeqOrSetIter() {
    return new SeqOrSetIter(items, offset, offset+length-1);
  }

  public Obj getSlice(long first, long len) {
    if (first + len > length)
      throw new IndexOutOfBoundsException();
    return new SliceObj(master, offset + (int) first, (int) len);
  }

  public Obj append(Obj obj) {
    int used = offset + length;
    if (master.used == used && used + 1 < master.items.length) {
      master.items[used] = obj;
      return new SliceObj(master, offset, length+1);
    }
    else {
      Obj[] newItems = new Obj[length < 16 ? 32 : (3 * length) / 2];
      for (int i=0 ; i < length ; i++)
        newItems[i] = items[offset+i];
      newItems[length] = obj;
      return new MasterSeqObj(newItems, length+1);

    }
  }

  public Obj concat(Obj seq) {
    int seqLen = seq.getSize();
    int used = offset + length;
    int newLen = used + seqLen;

    if (master.used == used && newLen <= master.items.length) {
      for (int i=0 ; i < seqLen ; i++)
        master.items[used+i] = seq.getItem(i);
      master.used += seqLen;
      return new SliceObj(master, offset, newLen);
    }

    return super.concat(seq);
    // return new RopeObj(this, seq);
  }

  protected int offset() {
    return offset;
  }
}
