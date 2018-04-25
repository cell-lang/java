package net.cell_lang;


class TernRelIter {
  Obj[] col1;
  Obj[] col2;
  Obj[] col3;
  int[] idxs;
  int next;
  int last;

  public TernRelIter(Obj[] col1, Obj[] col2, Obj[] col3, int[] idxs, int next, int last) {
    Miscellanea._assert(col1.length == col2.length && col1.length == col3.length);
    Miscellanea._assert(idxs == null || idxs.length == col1.length);
    Miscellanea._assert(next >= 0);
    Miscellanea._assert(last >= -1 && last < col1.length);
    this.col1 = col1;
    this.col2 = col2;
    this.col3 = col3;
    this.idxs = idxs;
    this.next = next;
    this.last = last;
  }

  public TernRelIter(Obj[] col1, Obj[] col2, Obj[] col3) {
    this(col1, col2, col3, null, 0, col1.length-1);
  }

  public Obj get1() {
    return col1[idxs == null ? next : idxs[next]];
  }

  public Obj get2() {
    return col2[idxs == null ? next : idxs[next]];
  }

  public Obj get3() {
    return col3[idxs == null ? next : idxs[next]];
  }

  public void next() {
    Miscellanea._assert(next <= last);
    next++;
  }

  public boolean done() {
    return next > last;
  }
}
