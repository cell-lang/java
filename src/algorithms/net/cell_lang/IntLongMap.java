 package net.cell_lang;


 public final class IntLongMap extends IntIdxMap {
  private static final int INIT_SIZE = 32;

  private long[] values = new long[INIT_SIZE];


  public void insert(int key, long value) {
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

  public long valueAt(int idx) {
    return values[idx];
  }
}