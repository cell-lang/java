package net.cell_lang;


class OneWayBinTable {
  final int MinCapacity = 16;

  static uint[] emptyArray = new uint[0];

  public uint[] column;
  public OverflowTable overflowTable;
  public int count;

  public void Check() {
    overflowTable.Check(column, count);
  }

  public void Dump() {
    Console.WriteLine("count = " + count.ToString());
    Console.Write("column = [");
    for (int i=0 ; i < column.Length ; i++)
      Console.Write("{0}{1:X}", (i > 0 ? " " : ""), column[i]);
    Console.WriteLine("]");
    overflowTable.Dump();
  }

  public void Init() {
    column = emptyArray;
    overflowTable.Init();
    count = 0;
  }

  public void InitReverse(ref OneWayBinTable source) {
    Miscellanea.Assert(count == 0);

    uint[] srcCol = source.column;
    int len = srcCol.Length;

    for (uint i=0 ; i < len ; i++) {
      uint code = srcCol[i];
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

  public bool Contains(uint surr1, uint surr2) {
    if (surr1 >= column.Length)
      return false;
    uint code = column[surr1];
    if (code == OverflowTable.EmptyMarker)
      return false;
    if (code >> 29 == 0)
      return code == surr2;
    return overflowTable.In(surr2, code);
  }

  public bool ContainsKey(uint surr1) {
    return surr1 < column.Length && column[surr1] != OverflowTable.EmptyMarker;
  }

  public uint[] Lookup(uint surr) {
    if (surr >= column.Length)
      return emptyArray;
    uint code = column[surr];
    if (code == OverflowTable.EmptyMarker)
      return emptyArray;
    if (code >> 29 == 0)
      return new uint[] {code};

    uint count = overflowTable.Count(code);
    OverflowTable.Iter it = overflowTable.GetIter(code);
    uint[] surrs = new uint[count];
    int next = 0;
    while (!it.Done()) {
      surrs[next++] = it.Get();
      it.Next();
    }
    Miscellanea.Assert(next == count);
    return surrs;
  }

  public void Insert(uint surr1, uint surr2) {
    int size = column.Length;
    if (surr1 >= size) {
      int newSize = size == 0 ? MinCapacity : 2 * size;
      while (surr1 >= newSize)
        newSize *= 2;
      uint[] newColumn = new uint[newSize];
      Array.Copy(column, newColumn, size);
      for (int i=size ; i < newSize ; i++)
        newColumn[i] = OverflowTable.EmptyMarker;
      column = newColumn;
    }

    uint code = column[surr1];
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

  public void Delete(uint surr1, uint surr2) {
    uint code = column[surr1];
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

  public uint[,] Copy() {
    uint[,] res = new uint[count, 2];
    int next = 0;
    for (uint i=0 ; i < column.Length ; i++) {
      uint code = column[i];
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
