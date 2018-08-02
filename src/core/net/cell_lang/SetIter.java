package net.cell_lang;


class SetIter {
  Obj[] objs;
  int next;
  int last;

  public SetIter(Obj[] objs, int next, int last) {
    this.objs = objs;
    this.next = next;
    this.last = last;
  }

  public Obj get() {
    Miscellanea._assert(next <= last);
    return objs[next];
  }

  public void next() {
    Miscellanea._assert(next <= last);
    next++;
  }

  public boolean done() {
    return next > last;
  }
}
