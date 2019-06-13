package net.cell_lang;


final class ObjColumn extends ColumnBase {
  public static class Iter {
    private Obj[] column;
    private int left; // Includes current value
    private int idx;

    private static Iter emptyIter = new Iter();

    private Iter() {
      column = new Obj[0];
      left = 0;
      idx = 0;
    }

    private Iter(Obj[] column, int count) {
      Miscellanea._assert(count > 0);
      this.column = column;
      this.left = count;
      this.idx = 0;
      while (column[idx] == null)
        idx++;
    }

    public static Iter newIter(Obj[] column, int count) {
      return count != 0 ? new Iter(column, count) : emptyIter;
    }

    public boolean done() {
      return left <= 0;
    }

    public int getIdx() {
      return idx;
    }

    public Obj getValue() {
      return column[idx];
    }

    public void next() {
      if (--left > 0)
        do
          idx++;
        while (column[idx] == null);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int INIT_SIZE = 256;

  //////////////////////////////////////////////////////////////////////////////

  Obj[] column = new Obj[INIT_SIZE];

  //////////////////////////////////////////////////////////////////////////////

  public ObjColumn(SurrObjMapper mapper) {
    super(mapper);
    this.mapper = mapper;
  }

  public boolean contains1(int idx) {
    return idx < column.length && column[idx] != null;
  }

  public Obj lookup(int idx) {
    Obj value = column[idx];
    if (value == null)
      throw Miscellanea.softFail();
    return value;
  }

  public Iter getIter() {
    return Iter.newIter(column, count);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int idx, Obj value) {
    if (idx >= column.length)
      column = Array.extend(column, Array.capacity(column.length, idx+1));
    Obj currValue = column[idx];
    if (currValue == null) {
      count++;
      column[idx] = value;
    }
    else if (!value.isEq(currValue))
      throw Miscellanea.softFail();
  }

  public void update(int idx, Obj value) {
    if (idx >= column.length)
      column = Array.extend(column, Array.capacity(column.length, idx+1));
    if (column[idx] != null)
      count++;
    column[idx] = value;
  }

  public void delete(int idx) {
    if (idx < column.length && column[idx] != null) {
      column[idx] = null;
      count--;
    }
  }
}

