package net.cell_lang;

import java.util.HashMap;


public class IntMap {
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