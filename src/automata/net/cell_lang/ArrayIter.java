package net.cell_lang;


class ArrayIter {
  int idx;
  int[] values;

  public ArrayIter(int[] values) {
    this.values = values;
  }

  public boolean done() {
    return idx >= values.length;
  }

  public int get() {
    return values[idx];
  }

  public void next() {
    Miscellanea._assert(idx < values.length);
    idx++;
  }
}