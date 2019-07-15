package net.cell_lang;


class SurrSet {
  IntIdxMap map = new IntIdxMap();

  public boolean includes(int elt) {
    int idx = map.index(elt);
    return idx != -1 && map.flagsAt(idx) == 0;
  }

  public void insert(int elt) {
    int idx = map.index(elt);
    if (idx == -1)
      map.insertKey(elt);
    else
      map.setFlags(idx, 0);
  }

  public void remove(int elt) {
    int idx = map.index(elt);
    if (idx != -1)
      map.setFlags(idx, 1);
  }

  public void clear() {
    map.clear();
  }

  //////////////////////////////////////////////////////////////////////////////

  public int first() {
    return next(-1);
  }

  public boolean done(int index) {
    return index >= map.count();
  }

  public int next(int index) {
    do
      index++;
    while (index < map.count() && map.flagsAt(index) != 0);
    return index;
  }

  public int value(int index) {
    return map.keyAt(index);
  }
}
