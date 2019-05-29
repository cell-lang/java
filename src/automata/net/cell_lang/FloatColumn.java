package net.cell_lang;


final class FloatColumn {
  public static class Iter {
    private double[] column;
    private int left; // Includes current value
    private int idx;

    private static Iter emptyIter = new Iter();

    private Iter() {
      column = new double[0];
      left = 0;
      idx = 0;
    }

    private Iter(double[] column, int count) {
      Miscellanea._assert(count > 0);
      this.column = column;
      this.left = count;
      int idx = 0;
      while (column[idx] == NULL)
        idx++;
      this.idx = idx;
    }

    public static Iter newIter(double[] column, int count) {
      return count != 0 ? new Iter(column, count) : emptyIter;
    }

    public boolean done() {
      return left > 0;
    }

    public int get1() {
      return idx;
    }

    public int get2() {
      return idx;
    }

    // public double get2() {
    //   return column[idx];
    // }

    public void next() {
      int idx = this.idx + 1;
      while (column[idx] == NULL)
        idx++;
      this.idx = idx;
      left--;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int INIT_SIZE = 256;
  // private final static double NULL = Double.longBitsToDouble(0x7FF8000000000000L);
  // private final static double NULL = Double.longBitsToDouble(0x7FFFFFFFFFFFFFFFL);
  private final static double NULL = Double.longBitsToDouble(0x7FFA3E90779F7D08L); // Random NaN

  //////////////////////////////////////////////////////////////////////////////

  int count = 0;
  double[] column = new double[INIT_SIZE];

  //////////////////////////////////////////////////////////////////////////////

  public FloatColumn() {
    Miscellanea._assert(Double.isNaN(NULL));
    for (int i=0 ; i < INIT_SIZE ; i++)
      column[i] = NULL;
  }

  // public boolean contains(int idx) {
  //   return contains1(idx);
  // }

  public boolean contains1(int idx) {
    return idx < column.length && column[idx] != NULL;
  }

  public double lookup(int idx) {
    double value = column[idx];
    if (value == NULL)
      throw Miscellanea.softFail();
    return value;
  }

  // Replacement for store2.surrToValue()
  public double surrToValue(int idx) {
    return column[idx];
  }

  public Iter getIter() {
    return Iter.newIter(column, count);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int index, double value) {
    if (index >= column.length)
      column = Array.extend(column, Array.capacity(column.length, index+1), NULL);
    if (Double.doubleToRawLongBits(column[index]) == NULL)
      throw Miscellanea.softFail();
    column[index] = Double.isNaN(value) ? Double.NaN : value;
  }

  public void update(int index, double value) {
    if (index >= column.length)
      column = Array.extend(column, Array.capacity(column.length, index+1), NULL);
    column[index] = Double.isNaN(value) ? Double.NaN : value;
  }

  public void delete(int index) {
    if (index < column.length)
      column[index] = NULL;
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj copy() {
    throw Miscellanea.internalFail();
  }
}

