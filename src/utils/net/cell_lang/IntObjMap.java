package net.cell_lang;


public class IntObjMap extends IntIdxMap {
  private static final int INIT_SIZE = 32;

  private Obj[] values = new Obj[INIT_SIZE];


  public void insert(int key, Obj value) {
    int idx = count();
    if (idx == values.length)
      values = Array.extend(values, 2 * idx);
    values[idx] = value;
    insertKey(key);
  }

  // public void clear() {
  //   super.clear();
  //   //## TODO: RESET IF THE THING HAS BECOME TOO LARGE
  //   //## SHOULD THE VALUES BE RESET?
  // }

  public Obj valueAt(int idx) {
    return values[idx];
  }
}