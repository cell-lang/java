 package net.cell_lang;


 public final class IntIntMap extends IntIdxMap {
  private static final int INIT_SIZE = 32;

  private int[] values = new int[INIT_SIZE];


  public void insert(int key, int value) {
    int idx = count();
    if (idx == values.length)
      values = Array.extend(values, 2 * idx);
    values[idx] = value;
    insertKey(key);
  }

  // public void clear() {
  //   super.clear();
  //   //## TODO: RESET IF THE THING HAS BECOME TOO LARGE
  // }

  public int valueAt(int idx) {
    return values[idx];
  }
}