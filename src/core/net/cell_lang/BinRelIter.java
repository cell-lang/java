package net.cell_lang;


class BinRelIter {
  Obj[] col1;
  Obj[] col2;
  int[] idxs;
  int next;
  int last;

  public static BinRelIter emptyIter =
    new BinRelIter(Array.emptyObjArray, Array.emptyObjArray, 0, -1);

  public BinRelIter(Obj[] col1, Obj[] col2, int[] idxs, int next, int last) {
    Miscellanea._assert(col1.length == col2.length);
    Miscellanea._assert(idxs == null || col1.length == idxs.length);
    Miscellanea._assert(next >= 0);
    Miscellanea._assert(last >= -1 & last < col1.length);
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

  public Obj get1() {
    return col1[idxs == null ? next : idxs[next]];
  }

  public Obj get2() {
    return col2[idxs == null ? next : idxs[next]];
  }

  public void next() {
    Miscellanea._assert(next <= last);
    next++;
  }

  public boolean done() {
    return next > last;
  }
}
