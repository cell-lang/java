package net.cell_lang;


class OneWayBinTable {
  final int MinCapacity = 16;

  static int[] emptyArray = new int[0];

  public int[] column;
  public OverflowTable overflowTable;
  public int count;

  public void check() {
    overflowTable.check(column, count);
  }

  public void dump() {
    System.out.println("count = " + count.toString());
    System.out.print("column = [");
    for (int i=0 ; i < column.length ; i++)
      System.out.print("{0}{1:X}", (i > 0 ? " " : ""), column[i]);
    System.out.println("]");
    overflowTable.dump();
  }

  public void init() {
    column = emptyArray;
    overflowTable.init();
    count = 0;
  }

  public void initReverse(ref OneWayBinTable source) {
    Miscellanea._assert(count == 0);

    int[] srcCol = source.column;
    int len = srcCol.length;

    for (int i=0 ; i < len ; i++) {
      int code = srcCol[i];
      if (code != OverflowTable.EmptyMarker)
        if (code >> 29 == 0) {
          Insert(code, i);
        }
        else {
          OverflowTable.Iter it = source.overflowTable.getIter(code);
          while (!it.done()) {
            Insert(it.get(), i);
            it.next();
          }
        }
    }
  }

  public bool Contains(int surr1, int surr2) {
    if (surr1 >= column.length)
      return false;
    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return false;
    if (code >> 29 == 0)
      return code == surr2;
    return overflowTable.in(surr2, code);
  }

  public bool ContainsKey(int surr1) {
    return surr1 < column.length && column[surr1] != OverflowTable.EmptyMarker;
  }

  public int[] lookup(int surr) {
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

  public void insert(int surr1, int surr2) {
    int size = column.length;
    if (surr1 >= size) {
      int newSize = size == 0 ? MinCapacity : 2 * size;
      while (surr1 >= newSize)
        newSize *= 2;
      int[] newColumn = new int[newSize];
      Miscellanea.arrayCopy(column, newColumn, size);
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
      bool inserted;
      column[surr1] = overflowTable.insert(code, surr2, out inserted);
      if (inserted)
        count++;
    }
  }

  public void delete(int surr1, int surr2) {
    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return;
    if (code == surr2) {
      column[surr1] = OverflowTable.EmptyMarker;
      count--;
    }
    else if (code >> 29 != 0) {
      bool deleted;
      column[surr1] = overflowTable.delete(code, surr2, out deleted);
      if (deleted)
        count--;
    }
  }

  public int[,] Copy() {
    int[,] res = new int[count, 2];
    int next = 0;
    for (int i=0 ; i < column.length ; i++) {
      int code = column[i];
      if (code != OverflowTable.EmptyMarker) {
        if (code >> 29 == 0) {
          res[next, 0] = i;
          res[next++, 1] = code;
        }
        else {
          OverflowTable.Iter it = overflowTable.getIter(code);
          while (!it.done()) {
            res[next, 0] = i;
            res[next++, 1] = it.get();
            it.next();
          }
        }
      }
    }
    Miscellanea._assert(next == count);
    return res;
  }
}
