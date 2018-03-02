package net.cell_lang;


class TernRelIter {
  Obj[] col1;
  Obj[] col2;
  Obj[] col3;
  int[] idxs;
  int next;
  int last;

  public TernRelIter(Obj[] col1, Obj[] col2, Obj[] col3, int[] idxs, int next, int last) {
    Miscellanea.Assert(col1.length == col2.length && col1.length == col3.length);
    Miscellanea.Assert(idxs == null || idxs.length == col1.length);
    Miscellanea.Assert(next >= 0);
    Miscellanea.Assert(last >= -1 && last < col1.length);
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

  public Obj Get1() {
    return col1[idxs == null ? next : idxs[next]];
  }

  public Obj Get2() {
    return col2[idxs == null ? next : idxs[next]];
  }

  public Obj Get3() {
    return col3[idxs == null ? next : idxs[next]];
  }

  public void Next() {
    Miscellanea.Assert(next <= last);
    next++;
  }

  public boolean Done() {
    return next > last;
  }
}
