package net.cell_lang;


class BinRelIter {
  Obj[] col1;
  Obj[] col2;
  int[] idxs;
  int next;
  int last;

  public BinRelIter(Obj[] col1, Obj[] col2, int[] idxs, int next, int last) {
    Miscellanea.Assert(col1.length == col2.length);
    Miscellanea.Assert(idxs == null || col1.length == idxs.length);
    Miscellanea.Assert(next >= 0);
    Miscellanea.Assert(last >= -1 & last < col1.length);
    this.col1 = col1;
    this.col2 = col2;
    this.idxs = idxs;
    this.next = next;
    this.last = last;
  }

  public BinRelIter(Obj[] col1, Obj[] col2, int next, int last) {
    this(col1, col2, null, next, last);
  }

  public BinRelIter(Obj[] col1, Obj[] col2) {
    this(col1, col2, null, 0, col1.length-1);
  }

  public Obj Get1() {
    return col1[idxs == null ? next : idxs[next]];
  }

  public Obj Get2() {
    return col2[idxs == null ? next : idxs[next]];
  }

  public void Next() {
    Miscellanea.Assert(next <= last);
    next++;
  }

  public boolean Done() {
    return next > last;
  }
}
