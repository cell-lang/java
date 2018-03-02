package net.cell_lang;


class SeqOrSetIter {
  Obj[] objs;
  int next;
  int last;

  public SeqOrSetIter(Obj[] objs, int next, int last) {
    this.objs = objs;
    this.next = next;
    this.last = last;
  }

  public Obj Get() {
    Miscellanea.Assert(next <= last);
    return objs[next];
  }

  public void Next() {
    Miscellanea.Assert(next <= last);
    next++;
  }

  public boolean Done() {
    return next > last;
  }
}
