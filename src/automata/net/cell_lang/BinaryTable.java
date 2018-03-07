package net.cell_lang;


class BinaryTable {
  public static class Iter {
    int next;
    int[,] entries;

    public Iter(int[,] entries) {
      this.entries = entries;
      next = 0;
    }

    public bool Done() {
      return next >= entries.GetLength(0);
    }

    public int GetField1() {
      return entries[next, 0];
    }

    public int GetField2() {
      return entries[next, 1];
    }

    public void Next() {
      next++;
    }
  }


  OneWayBinTable table1;
  OneWayBinTable table2;

  public ValueStore store1;
  public ValueStore store2;

  public BinaryTable(ValueStore store1, ValueStore store2) {
    table1.Init();
    table2.Init();
    this.store1 = store1;
    this.store2 = store2;
    // Check();
  }

  public void Check() {
    table1.Check();
    table2.Check();
  }

  public int Size() {
    return table1.count;
  }

  public bool Contains(long surr1, long surr2) {
    return table1.Contains(surr1, surr2);
  }

  public bool ContainsField1(int surr1) {
    return table1.ContainsKey(surr1);
  }

  public bool ContainsField2(int surr2) {
    if (table2.count == 0 & table1.count > 0)
      table2.InitReverse(ref table1);
    return table2.ContainsKey(surr2);
  }

  public int[] LookupByCol1(int surr) {
    return table1.Lookup(surr);
  }

  public int[] LookupByCol2(int surr) {
    if (table2.count == 0 & table1.count > 0)
      table2.InitReverse(ref table1);
    return table2.Lookup(surr);
  }

  public Iter GetIter() {
    return new Iter(table1.Copy());
  }

  public Iter GetIter1(long surr1) {
    int[] col2 = LookupByCol1(surr1);
    int[,] entries = new int[col2.length, 2];
    for (int i=0 ; i < col2.length ; i++) {
      entries[i, 0] = surr1;
      entries[i, 1] = col2[i];
    }
    return new Iter(entries);
  }

  public Iter GetIter2(long surr2) {
    int[] col1 = LookupByCol2(surr2);
    int[,] entries = new int[col1.length, 2];
    for (int i=0 ; i < col1.length ; i++) {
      entries[i, 0] = col1[i];
      entries[i, 1] = surr2;
    }
    return new Iter(entries);
  }

  public void Insert(int surr1, int surr2) {
    table1.Insert(surr1, surr2);
    if (table2.count > 0)
      table2.Insert(surr2, surr1);
    // Check();
  }

  public void Clear() {
    table1.Init();
    table2.Init();
    // Check();
  }

  public void Delete(int surr1, int surr2) {
    table1.Delete(surr1, surr2);
    if (table2.count > 0)
      table2.Delete(surr2, surr1);
    // Check();
  }

  public Obj Copy(bool flipped) {
    int count = table1.count;

    if (count == 0)
      return EmptyRelObj.Singleton();

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];

    int next = 0;
    for (int i=0 ; i < table1.column.length ; i++) {
      int code = table1.column[i];
      if (code != OverflowTable.EmptyMarker) {
        Obj val1 = store1.GetValue(i);
        if (code >> 29 == 0) {
          objs1[next] = val1;
          objs2[next++] = store2.GetValue(code);
        }
        else {
          OverflowTable.Iter it = table1.overflowTable.GetIter(code);
          while (!it.Done()) {
            int surr2 = it.Get();
            objs1[next] = val1;
            objs2[next++] = store2.GetValue(surr2);
            it.Next();
          }
        }
      }
    }
    Miscellanea.Assert(next == count);

    return Builder.CreateBinRel(flipped ? objs2 : objs1, flipped ? objs1 : objs2, count); //## THIS COULD BE MADE MORE EFFICIENT
  }

  public int[,] RawCopy() {
    return table1.Copy();
  }
}
