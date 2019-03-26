package net.cell_lang;

import java.util.HashMap;


public class SurrObjMap {
  HashMap<Integer, Obj> map = new HashMap<Integer, Obj>();

  public void set(int key, Obj value) {
    map.put(key, value);
  }

  public void reset(int key) {
    map.remove(key);
  }

  public void reset() {
    map.clear();
  }

  public Obj get(int key) {
    return map.get(key);
  }

  public boolean hasKey(int key) {
    return map.get(key) != null;
  }

  public int iter() {
    throw new RuntimeException();
  }

  public int next(int iter) {
    throw new RuntimeException();
  }

  public boolean done(int iter) {
    throw new RuntimeException();
  }

  public int key(int iter) {
    throw new RuntimeException();
  }

  public Obj value(int iter) {
    throw new RuntimeException();
  }
}