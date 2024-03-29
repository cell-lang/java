package net.cell_lang;


final class FloatColumn extends ColumnBase {
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
      this.idx = 0;
      while (isNull(column[idx]))
        idx++;
    }

    public static Iter newIter(double[] column, int count) {
      return count != 0 ? new Iter(column, count) : emptyIter;
    }

    public boolean done() {
      return left <= 0;
    }

    public int getIdx() {
      return idx;
    }

    public double getValue() {
      return column[idx];
    }

    public void next() {
      if (--left > 0)
        do
          idx++;
        while (isNull(column[idx]));
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int INIT_SIZE = 256;

  // private final static long NULL_BIT_MASK = 0x7FF8000000000000L;
  // private final static long NULL_BIT_MASK = 0x7FFFFFFFFFFFFFFFL;
  private final static long NULL_BIT_MASK = 0x7FFA3E90779F7D08L; // Random NaN

  private final static double NULL = Double.longBitsToDouble(NULL_BIT_MASK);

  static {
    Miscellanea._assert(Double.isNaN(NULL));
  }

  //////////////////////////////////////////////////////////////////////////////

  double[] column = new double[INIT_SIZE];

  //////////////////////////////////////////////////////////////////////////////

  public FloatColumn(SurrObjMapper mapper) {
    super(mapper);
    Array.fill(column, NULL);
  }

  public FloatColumn() {
    this(null);
  }

  public int size() {
    return count;
  }

  public boolean contains1(int idx) {
    return idx < column.length && !isNull(column[idx]);
  }

  public double lookup(int idx) {
    double value = column[idx];
    if (isNull(value))
      throw Miscellanea.softFail();
    return value;
  }

  public Iter getIter() {
    return Iter.newIter(column, count);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int index, double value) {
    if (index >= column.length)
      column = Array.extend(column, Array.capacity(column.length, index+1), NULL);
    double currValue = column[index];
    if (!isNull(currValue)) {
      if (value != currValue)
        throw Miscellanea.softFail();
    }
    else {
      column[index] = Double.isNaN(value) ? Double.NaN : value;
      count++;
    }
  }

  public void update(int index, double value) {
    if (index >= column.length)
      column = Array.extend(column, Array.capacity(column.length, index+1), NULL);
    if (isNull(column[index]))
      count++;
    column[index] = Double.isNaN(value) ? Double.NaN : value;
  }

  public void delete(int index) {
    if (index < column.length && !isNull(column[index])) {
      column[index] = NULL;
      count--;
    }
  }

  public void clear() {
    count = 0;
    if (column.length != INIT_SIZE)
      column = new double[INIT_SIZE];
    Array.fill(column, NULL);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static boolean isNull(double value) {
    return Double.doubleToRawLongBits(value) == NULL_BIT_MASK;
  }
}

