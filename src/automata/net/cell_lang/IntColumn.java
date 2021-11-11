package net.cell_lang;

import java.util.HashSet;


final class IntColumn extends ColumnBase {
  public class Iter {
    private int left; // Includes current value
    private int idx;


    public Iter() {
      this.left = count;
      this.idx = 0;
      if (left > 0)
        while (isNull(idx))
          idx++;
    }

    public boolean done() {
      return left <= 0;
    }

    public int getIdx() {
      return idx;
    }

    public long getValue() {
      return column[idx];
    }

    public void next() {
      if (--left > 0)
        do
          idx++;
        while (isNull(idx));
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int INIT_SIZE = 256;

  // private final static long NULL = -9223372036854775808L;
  private final static long NULL = -5091454680840284659L; // Random null value

  //////////////////////////////////////////////////////////////////////////////

  long[] column = new long[INIT_SIZE];
  HashSet<Integer> collisions = new HashSet<Integer>();

  //////////////////////////////////////////////////////////////////////////////

  public IntColumn(SurrObjMapper mapper) {
    super(mapper);
    Array.fill(column, NULL);
  }

  public IntColumn() {
    this(null);
  }

  public boolean contains1(int idx) {
    return idx < column.length && !isNull(idx);
  }

  public long lookup(int idx) {
    long value = column[idx];
    if (isNull(idx, value))
      throw Miscellanea.softFail();
    return value;
  }

  public Iter getIter() {
    return new Iter();
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int idx, long value) {
    if (idx >= column.length)
      column = Array.extend(column, Array.capacity(column.length, idx+1), NULL);

    long currValue = column[idx];
    if (isNull(idx, currValue)) {
      count++;
      column[idx] = value;
      if (value == NULL)
        collisions.add(idx);
    }
    else {
      // The value is already set, so we need to fail if the new value is different from the existing one
      if (value != currValue)
        throw Miscellanea.softFail();
    }
  }

  public void update(int idx, long value) {
    if (idx >= column.length)
      column = Array.extend(column, Array.capacity(column.length, idx+1), NULL);

    long currValue = column[idx];
    if (currValue != NULL) {
      // There is an existing value, and it's not NULL
      column[idx] = value;
      if (value == NULL)
        collisions.add(idx);
    }
    else if (collisions.contains(idx)) {
      // The existing value happens to be NULL
      if (value != NULL) {
        column[idx] = value;
        collisions.remove(idx);
      }
    }
    else {
      // No existing value
      count++;
      column[idx] = value;
      if (value == NULL)
        collisions.add(idx);
    }
  }

  public void delete(int idx) {
    if (idx < column.length) {
      long value = column[idx];
      if (value != NULL) {
        column[idx] = NULL;
        count--;
      }
      else if (collisions.contains(idx)) {
        collisions.remove(idx);
        count--;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean isNull(int idx) {
    return isNull(idx, column[idx]);
  }

  private boolean isNull(int idx, long value) {
    return value == NULL && !collisions.contains(idx);
  }
}

