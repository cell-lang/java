package net.cell_lang;


class OneWayBinTable {
  final int MinCapacity = 16;

  static int[] emptyArray = new int[0];

  public int[] column;
  public OverflowTable overflowTable;
  public int count;

  public void Check() {
    overflowTable.Check(column, count);
  }

  public void Dump() {
    System.out.println("count = " + count.toString());
    System.out.print("column = [");
    for (int i=0 ; i < column.length ; i++)
      System.out.print("{0}{1:X}", (i > 0 ? " " : ""), column[i]);
    System.out.println("]");
    overflowTable.Dump();
  }

  public void Init() {
    column = emptyArray;
    overflowTable.Init();
    count = 0;
  }

  public void InitReverse(ref OneWayBinTable source) {
    Miscellanea.Assert(count == 0);

    int[] srcCol = source.column;
    int len = srcCol.length;

    for (int i=0 ; i < len ; i++) {
      int code = srcCol[i];
      if (code != OverflowTable.EmptyMarker)
        if (code >> 29 == 0) {
          Insert(code, i);
        }
        else {
          OverflowTable.Iter it = source.overflowTable.GetIter(code);
          while (!it.Done()) {
            Insert(it.Get(), i);
            it.Next();
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
    return overflowTable.In(surr2, code);
  }

  public bool ContainsKey(int surr1) {
    return surr1 < column.length && column[surr1] != OverflowTable.EmptyMarker;
  }

  public int[] Lookup(int surr) {
    if (surr >= column.length)
      return emptyArray;
    int code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return emptyArray;
    if (code >> 29 == 0)
      return new int[] {code};

    int count = overflowTable.Count(code);
    OverflowTable.Iter it = overflowTable.GetIter(code);
    int[] surrs = new int[count];
    int next = 0;
    while (!it.Done()) {
      surrs[next++] = it.Get();
      it.Next();
    }
    Miscellanea.Assert(next == count);
    return surrs;
  }

  public void Insert(int surr1, int surr2) {
    int size = column.length;
    if (surr1 >= size) {
      int newSize = size == 0 ? MinCapacity : 2 * size;
      while (surr1 >= newSize)
        newSize *= 2;
      int[] newColumn = new int[newSize];
      Array.Copy(column, newColumn, size);
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
      column[surr1] = overflowTable.Insert(code, surr2, out inserted);
      if (inserted)
        count++;
    }
  }

  public void Delete(int surr1, int surr2) {
    int code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return;
    if (code == surr2) {
      column[surr1] = OverflowTable.EmptyMarker;
      count--;
    }
    else if (code >> 29 != 0) {
      bool deleted;
      column[surr1] = overflowTable.Delete(code, surr2, out deleted);
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
          OverflowTable.Iter it = overflowTable.GetIter(code);
          while (!it.Done()) {
            res[next, 0] = i;
            res[next++, 1] = it.Get();
            it.Next();
          }
        }
      }
    }
    Miscellanea.Assert(next == count);
    return res;
  }
}
