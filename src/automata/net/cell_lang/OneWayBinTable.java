package net.cell_lang;


class OneWayBinTable {
  final int MinCapacity = 16;

  static int[] emptyArray = new int[0];

  public int[] column = emptyArray;
  public OverflowTable overflowTable = new OverflowTable();
  public int count = 0;


  public void check() {
    overflowTable.check(column, count);
  }

  public void dump() {
    System.out.println("count = " + Integer.toString(count));
    System.out.print("column = [");
    for (int i=0 ; i < column.length ; i++)
      System.out.printf("%s%X", i > 0 ? " " : "", column[i]);
    System.out.println("]");
    overflowTable.dump();
  }

  public void initReverse(OneWayBinTable source) {
    Miscellanea._assert(count == 0);

    int[] srcCol = source.column;
    int len = srcCol.length;

    for (int i=0 ; i < len ; i++) {
      int code = srcCol[i];
      if (code != OverflowTable.EmptyMarker)
        if (code >> 29 == 0) {
          insert(code, i);
        }
        else {
          OverflowTable.Iter it = source.overflowTable.getIter(code);
          while (!it.done()) {
            insert(it.get(), i);
            it.next();
          }
        }
    }
  }

  public boolean contains(int surr1, int surr2) {
    if (surr1 >= column.length)
      return false;
    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return false;
    if (code >> 29 == 0)
      return code == surr2;
    return overflowTable.in(surr2, code);
  }

  public boolean containsKey(int surr1) {
    return surr1 < column.length && column[surr1] != OverflowTable.EmptyMarker;
  }

  public int[] restrict(int surr) {
    if (surr >= column.length)
      return emptyArray;
    int code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return emptyArray;
    if (code >> 29 == 0)
      return new int[] {code};

    int count = overflowTable.count(code);
    OverflowTable.Iter it = overflowTable.getIter(code);
    int[] surrs = new int[count];
    int next = 0;
    while (!it.done()) {
      surrs[next++] = it.get();
      it.next();
    }
    Miscellanea._assert(next == count);
    return surrs;
  }

  public int restrict(int surr, int[] output) {
    if (surr >= column.length)
      return 0;
    int code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return 0;

    if (code >> 29 == 0) {
      output[0] = code;
      return 1;
    }

    int count = overflowTable.count(code);
    OverflowTable.Iter it = overflowTable.getIter(code);
    int next = 0;
    while (!it.done()) {
      output[next++] = it.get();
      it.next();
    }
    Miscellanea._assert(next == count);
    return count;
  }

  public int lookup(int surr) {
    if (surr >= column.length)
      return -1;
    int code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return -1;
    if (code >> 29 == 0)
      return code;
    throw Miscellanea.internalFail();
  }

  public int count(int surr) {
    if (surr >= column.length)
      return 0;
    int code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return 0;
    return overflowTable.count(code);
  }

  // public int zeroOneMany(int surr) {
  //   if (surr >= column.length)
  //     return 0;
  //   int code = column[surr];
  //   if (code == OverflowTable.EmptyMarker)
  //     return 0;
  //   if (code >> 29 == 0)
  //     return 1;
  //   return Integer.MAX_VALUE;
  // }

  public void insert(int surr1, int surr2) {
    int size = column.length;
    if (surr1 >= size) {
      int newSize = size == 0 ? MinCapacity : 2 * size;
      while (surr1 >= newSize)
        newSize *= 2;
      int[] newColumn = new int[newSize];
      Array.copy(column, newColumn, size);
      for (int i=size ; i < newSize ; i++)
        newColumn[i] = OverflowTable.EmptyMarker;
      column = newColumn;
    }

    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker) {
      column[surr1] = surr2;
      count++;
    }
    else {
      boolean[] inserted = new boolean[1];
      column[surr1] = overflowTable.insert(code, surr2, inserted);
      if (inserted[0])
        count++;
    }
  }

  private boolean[] deleted = new boolean[1];

  public void delete(int surr1, int surr2) {
    //## BUG BUG BUG: WHAT IF surr1 >= column.length?
    //## THIS KIND OF BUG MAY BE PRESENT ELSEWHERE IN THIS CLASS
    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return;
    if (code == surr2) {
      column[surr1] = OverflowTable.EmptyMarker;
      count--;
    }
    else if (code >> 29 != 0) {
      deleted[0] = false;
      column[surr1] = overflowTable.delete(code, surr2, deleted);
      if (deleted[0])
        count--;
    }
  }

  public int[] copy() {
    int[] res = new int[2 * count];
    int next = 0;
    for (int i=0 ; i < column.length ; i++) {
      int code = column[i];
      if (code != OverflowTable.EmptyMarker) {
        if (code >> 29 == 0) {
          res[next++] = i;
          res[next++] = code;
        }
        else {
          OverflowTable.Iter it = overflowTable.getIter(code);
          while (!it.done()) {
            res[next++] = i;
            res[next++] = it.get();
            it.next();
          }
        }
      }
    }
    Miscellanea._assert(next == 2 * count);
    return res;
  }

  public int[] copySym(int eqCount) {
    int[] res = new int[count+eqCount];
    int next = 0;
    for (int i=0 ; i < column.length ; i++) {
      int code = column[i];
      if (code != OverflowTable.EmptyMarker) {
        if (code >> 29 == 0) {
          if (i <= code) {
            res[next++] = i;
            res[next++] = code;
          }
        }
        else {
          OverflowTable.Iter it = overflowTable.getIter(code);
          while (!it.done()) {
            int value = it.get();
            if (i <= value) {
              res[next++] = i;
              res[next++] = value;
            }
            it.next();
          }
        }
      }
    }
    Miscellanea._assert(next == count + eqCount);
    return res;
  }

  public boolean isMap() {
    for (int i=0 ; i < column.length ; i++) {
      int code = column[i];
      if (code != OverflowTable.EmptyMarker & code >> 29 != 0)
        return false;
    }
    return true;
  }
}
