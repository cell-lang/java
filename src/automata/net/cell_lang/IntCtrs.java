package net.cell_lang;


class IntCtrs {
  IntMap map = new IntMap();

  public void increment(int id) {
    int counter = map.hasKey(id) ? map.get(id) : 0;
    map.set(id, counter + 1);
  }

  public boolean tryDecrement(int id) {
    if (map.hasKey(id)) {
      int counter = map.get(id) - 1;
      if (counter > 0)
        map.set(id, counter);
      else
        map.reset(id);
      return true;
    }
    else
      return false;
  }
}