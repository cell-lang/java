package net.cell_lang;


class BinaryTableUpdater {
  static class Tuple {
    public uint field1;
    public uint field2;

    public Tuple(uint field1, uint field2) {
      this.field1 = field1;
      this.field2 = field2;
    }

    override public string ToString() {
      return "(" + field1.ToString() + ", " + field2.ToString() + ")";
    }
  }

  List<Tuple> deleteList = new List<Tuple>();
  List<Tuple> insertList = new List<Tuple>();

  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  public BinaryTableUpdater(BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  public void Clear() {
    uint[,] columns = table.RawCopy();
    int len = columns.GetLength(0);
    deleteList.Clear();
    for (int i=0 ; i < len ; i++)
      deleteList.Add(new Tuple(columns[i, 0], columns[i, 1]));
  }

  public void Set(Obj value, bool flipped) {
    Clear();
    Miscellanea.Assert(insertList.Count == 0);
    BinRelIter it = value.GetBinRelIter();
    while (!it.Done()) {
      Obj val1 = flipped ? it.Get2() : it.Get1();
      Obj val2 = flipped ? it.Get1() : it.Get2();
      int surr1 = store1.LookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.Insert(val1);
      int surr2 = store2.LookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.Insert(val2);
      insertList.Add(new Tuple((uint) surr1, (uint) surr2));
      it.Next();
    }
  }

  public void Delete(long value1, long value2) {
    if (table.Contains((uint) value1, (uint) value2))
      deleteList.Add(new Tuple((uint) value1, (uint) value2));
  }

  public void Delete1(long value) {
    uint[] assocs = table.LookupByCol1((uint) value);
    for (int i=0 ; i < assocs.Length ; i++)
      deleteList.Add(new Tuple((uint) value, assocs[i]));
  }

  public void Delete2(long value) {
    uint[] assocs = table.LookupByCol2((uint) value);
    for (int i=0 ; i < assocs.Length ; i++)
      deleteList.Add(new Tuple(assocs[i], (uint) value));
  }

  public void Insert(long value1, long value2) {
    insertList.Add(new Tuple((uint) value1, (uint) value2));
  }

  public bool CheckUpdates_1() {
    Comparison<Tuple> cmp = delegate(Tuple t1, Tuple t2) {
      return (int) (t1.field1 != t2.field1 ? t1.field1 - t2.field1 : t1.field2 - t2.field2);
    };

    deleteList.Sort(cmp);
    insertList.Sort(cmp);

    int count = insertList.Count;
    if (count == 0)
      return true;

    Tuple prev = insertList[0];
    if (!ContainsField1(deleteList, prev.field1))
      if (table.ContainsField1(prev.field1))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList[i];
      if (curr.field1 == prev.field1 & curr.field2 != prev.field2)
        return false;
      if (!ContainsField1(deleteList, curr.field1))
        if (table.ContainsField1(curr.field1))
          return false;
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_1_2() {
    if (!CheckUpdates_1())
      return false;

    Comparison<Tuple> cmp = delegate(Tuple t1, Tuple t2) {
      return (int) (t1.field2 != t2.field2 ? t1.field2 - t2.field2 : t1.field1 - t2.field1);
    };

    deleteList.Sort(cmp);
    insertList.Sort(cmp);

    int count = insertList.Count;
    if (count == 0)
      return true;

    Tuple prev = insertList[0];
    if (!ContainsField2(deleteList, prev.field2))
      if (table.ContainsField2(prev.field2))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList[i];
      if (curr.field2 == prev.field2 & curr.field1 != prev.field1)
        return false;
      if (!ContainsField2(deleteList, curr.field2))
        if (table.ContainsField2(curr.field2))
          return false;
      prev = curr;
    }

    return true;
  }

  public void Apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      Tuple tuple = deleteList[i];
      if (table.Contains(tuple.field1, tuple.field2)) {
        table.Delete(tuple.field1, tuple.field2);
      }
      else
        deleteList[i] = new Tuple(0xFFFFFFFF, 0xFFFFFFFF);
    }

    var it = insertList.GetEnumerator();
    while (it.MoveNext()) {
      var curr = it.Current;
      if (!table.Contains(curr.field1, curr.field2)) {
        table.Insert(curr.field1, curr.field2);
        table.store1.AddRef(curr.field1);
        table.store2.AddRef(curr.field2);
      }
    }
  }

  public void Finish() {
    var it = deleteList.GetEnumerator();
    while (it.MoveNext()) {
      var tuple = it.Current;
      if (tuple.field1 != 0xFFFFFFFF) {
        Miscellanea.Assert(table.store1.LookupSurrogate(tuple.field1) != null);
        Miscellanea.Assert(table.store2.LookupSurrogate(tuple.field2) != null);
        table.store1.Release(tuple.field1);
        table.store2.Release(tuple.field2);
      }
    }
    Reset();
  }

  public void Reset() {
    deleteList.Clear();
    insertList.Clear();
  }

  public void Dump() {
    Console.Write("deleteList =");
    for (int i=0 ; i < deleteList.Count ; i++)
      Console.Write(" {0}", deleteList[i]);
    Console.WriteLine("");

    Console.Write("insertList =");
    for (int i=0 ; i < insertList.Count ; i++)
      Console.Write(" {0}", insertList[i]);
    Console.WriteLine("\n");

    Console.Write("deleteList =");
    for (int i=0 ; i < deleteList.Count ; i++) {
      Tuple tuple = deleteList[i];
      Obj obj1 = store1.LookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.LookupSurrogateEx(tuple.field2);
      Console.Write(" ({0}, {1})", obj1, obj2);
    }
    Console.WriteLine("");

    Console.Write("insertList =");
    for (int i=0 ; i < insertList.Count ; i++) {
      Tuple tuple = insertList[i];
      Obj obj1 = store1.LookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.LookupSurrogateEx(tuple.field2);
      Console.Write(" ({0}, {1})", obj1, obj2);
    }
    Console.WriteLine("\n\n{0}\n\n", table.Copy(true));

    store1.Dump();
    store2.Dump();
  }

  static bool ContainsField1(List<Tuple> tuples, uint field1) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      uint midField1 = tuples[mid].field1;
      if (midField1 > field1)
        high = mid - 1;
      else if (midField1 < field1)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static bool ContainsField2(List<Tuple> tuples, uint field2) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      uint midField2 = tuples[mid].field2;
      if (midField2 > field2)
        high = mid - 1;
      else if (midField2 < field2)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }
}
