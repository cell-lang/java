package net.cell_lang;

import java.util.HashMap;


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

  public int get(int id) {
    return map.hasKey(id) ? map.get(id) : 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  //## REPLACE WITH IntIntMap ONCE DELETION AND UPDATE HAVE BEEN IMPLEMENTED
  static class IntMap {
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

    public void set(int key, int value) {
      map.put(key, value);
    }

    public void reset(int key) {
      map.remove(key);
    }

    public int get(int key) {
      Integer value = map.get(key);
      return value.intValue();
    }

    public boolean hasKey(int key) {
      return map.get(key) != null;
    }
  }
}
