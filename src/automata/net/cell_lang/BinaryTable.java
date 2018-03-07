package net.cell_lang;


class BinaryTable {
  public static class Iter {
    int next;
    uint[,] entries;

    public Iter(uint[,] entries) {
      this.entries = entries;
      next = 0;
    }

    public bool Done() {
      return next >= entries.GetLength(0);
    }

    public uint GetField1() {
      return entries[next, 0];
    }

    public uint GetField2() {
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
    return table1.Contains((uint) surr1, (uint) surr2);
  }

  public bool ContainsField1(uint surr1) {
    return table1.ContainsKey(surr1);
  }

  public bool ContainsField2(uint surr2) {
    if (table2.count == 0 & table1.count > 0)
      table2.InitReverse(ref table1);
    return table2.ContainsKey(surr2);
  }

  public uint[] LookupByCol1(uint surr) {
    return table1.Lookup(surr);
  }

  public uint[] LookupByCol2(uint surr) {
    if (table2.count == 0 & table1.count > 0)
      table2.InitReverse(ref table1);
    return table2.Lookup(surr);
  }

  public Iter GetIter() {
    return new Iter(table1.Copy());
  }

  public Iter GetIter1(long surr1) {
    uint[] col2 = LookupByCol1((uint) surr1);
    uint[,] entries = new uint[col2.Length, 2];
    for (int i=0 ; i < col2.Length ; i++) {
      entries[i, 0] = (uint) surr1;
      entries[i, 1] = col2[i];
    }
    return new Iter(entries);
  }

  public Iter GetIter2(long surr2) {
    uint[] col1 = LookupByCol2((uint) surr2);
    uint[,] entries = new uint[col1.Length, 2];
    for (int i=0 ; i < col1.Length ; i++) {
      entries[i, 0] = col1[i];
      entries[i, 1] = (uint) surr2;
    }
    return new Iter(entries);
  }

  public void Insert(uint surr1, uint surr2) {
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

  public void Delete(uint surr1, uint surr2) {
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
    for (uint i=0 ; i < table1.column.Length ; i++) {
      uint code = table1.column[i];
      if (code != OverflowTable.EmptyMarker) {
        Obj val1 = store1.GetValue(i);
        if (code >> 29 == 0) {
          objs1[next] = val1;
          objs2[next++] = store2.GetValue(code);
        }
        else {
          OverflowTable.Iter it = table1.overflowTable.GetIter(code);
          while (!it.Done()) {
            uint surr2 = it.Get();
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

  public uint[,] RawCopy() {
    return table1.Copy();
  }
}
