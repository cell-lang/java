package net.cell_lang;


class TernaryTableUpdater {
  ArrayList<TernaryTable.Tuple> deleteList = new ArrayList<TernaryTable.Tuple>();
  ArrayList<TernaryTable.Tuple> insertList = new ArrayList<TernaryTable.Tuple>();

  TernaryTable table;
  ValueStoreUpdater store1, store2, store3;

  public TernaryTableUpdater(TernaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2, ValueStoreUpdater store3) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
  }

  public void Clear() {
    deleteList.Clear();
    TernaryTable.Iter it = table.GetIter();
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Set(Obj value, int idx1, int idx2, int idx3) {
    Miscellanea._assert(deleteList.Count == 0 || deleteList.Count == table.count);
    Miscellanea._assert(insertList.Count == 0);

    Clear();
    TernRelIter it = value.GetTernRelIter();
    while (!it.Done()) {
      Obj val1 = idx1 == 0 ? it.Get1() : (idx1 == 1 ? it.Get2() : it.Get3());
      Obj val2 = idx2 == 0 ? it.Get1() : (idx2 == 1 ? it.Get2() : it.Get3());
      Obj val3 = idx3 == 0 ? it.Get1() : (idx3 == 1 ? it.Get2() : it.Get3());
      int surr1 = store1.LookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.Insert(val1);
      int surr2 = store2.LookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.Insert(val2);
      int surr3 = store3.LookupValueEx(val3);
      if (surr3 == -1)
        surr3 = store3.Insert(val3);
      insertList.Add(new TernaryTable.Tuple(surr1, surr2, surr3));
      it.Next();
    }
  }

  public void Insert(long value1, long value2, long value3) {
    insertList.Add(new TernaryTable.Tuple(value1, value2, value3));
  }

  public void Delete(long value1, long value2, long value3) {
    if (table.Contains(value1, value2, value3))
      deleteList.Add(new TernaryTable.Tuple(value1, value2, value3));
  }

  public void Delete12(long value1, long value2) {
    TernaryTable.Iter it = table.GetIter12(value1, value2);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Delete13(long value1, long value3) {
    TernaryTable.Iter it = table.GetIter13(value1, value3);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Delete23(long value2, long value3) {
    TernaryTable.Iter it = table.GetIter23(value2, value3);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Delete1(long value1) {
    TernaryTable.Iter it = table.GetIter1(value1);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Delete2(long value2) {
    TernaryTable.Iter it = table.GetIter2(value2);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Delete3(long value3) {
    TernaryTable.Iter it = table.GetIter3(value3);
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public bool CheckUpdates_12() {
    deleteList.Sort(compare123);
    insertList.Sort(compare123);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains12(deleteList, prev.field1OrNext, prev.field2OrEmptyMarker))
      if (table.Contains12(prev.field1OrNext, prev.field2OrEmptyMarker))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 != prev.field3
         )
        return false;
      if (!Contains12(deleteList, curr.field1OrNext, curr.field2OrEmptyMarker))
        if (table.Contains12(curr.field1OrNext, curr.field2OrEmptyMarker))
          return false;
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_3() {
    if (!CheckUpdates_12())
      return false;

    deleteList.Sort(compare312);
    insertList.Sort(compare312);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains3(deleteList, prev.field3))
      if (table.Contains3(prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field3 == prev.field3 &
           (curr.field1OrNext != prev.field1OrNext | curr.field2OrEmptyMarker != prev.field2OrEmptyMarker)
         )
        return false;
      if (!Contains3(deleteList, prev.field3))
        if (table.Contains3(prev.field3))
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_23() {
    if (!CheckUpdates_12())
      return false;

    deleteList.Sort(compare231);
    insertList.Sort(compare231);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains23(deleteList, prev.field2OrEmptyMarker, prev.field3))
      if (table.Contains23(prev.field2OrEmptyMarker, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 == prev.field3 &
           curr.field1OrNext != prev.field1OrNext
         )
        return false;
      if (!Contains23(deleteList, curr.field2OrEmptyMarker, curr.field3))
        if (table.Contains23(curr.field2OrEmptyMarker, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_23_31() {
    if (!CheckUpdates_12_23())
      return false;

    deleteList.Sort(compare312);
    insertList.Sort(compare312);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains31(deleteList, prev.field3, prev.field1OrNext))
      if (table.Contains13(prev.field1OrNext, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field3 == prev.field3 &
           curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker != prev.field2OrEmptyMarker
         )
        return false;
      if (!Contains31(deleteList, curr.field3, curr.field1OrNext))
        if (table.Contains13(curr.field1OrNext, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public void Apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      var tuple = deleteList[i];
      if (table.Contains(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3))
        table.Delete(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3);
      else
        deleteList[i] = new TernaryTable.Tuple(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    var it = insertList.GetEnumerator();
    while (it.MoveNext()) {
      var curr = it.Current;
      if (!table.Contains(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3)) {
        table.Insert(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3);
        table.store1.AddRef(curr.field1OrNext);
        table.store2.AddRef(curr.field2OrEmptyMarker);
        table.store3.AddRef(curr.field3);
      }
    }
  }

  public void Finish() {
    var it = deleteList.GetEnumerator();
    while (it.MoveNext()) {
      var tuple = it.Current;
      if (tuple.field1OrNext != 0xFFFFFFFF) {
        table.store1.Release(tuple.field1OrNext);
        table.store2.Release(tuple.field2OrEmptyMarker);
        table.store3.Release(tuple.field3);
      }
    }
  }

  public void Reset() {
    deleteList.Clear();
    insertList.Clear();
  }


  static bool Contains12(ArrayList<TernaryTable.Tuple> tuples, int field1, int field2) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples[mid];
      if (tuple.field1OrNext > field1)
        high = mid - 1;
      else if (tuple.field1OrNext < field1)
        low = mid + 1;
      else if (tuple.field2OrEmptyMarker > field2)
        high = mid - 1;
      else if (tuple.field2OrEmptyMarker < field2)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static bool Contains23(ArrayList<TernaryTable.Tuple> tuples, int field2, int field3) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples[mid];
      if (tuple.field2OrEmptyMarker > field2)
        high = mid - 1;
      else if (tuple.field2OrEmptyMarker < field2)
        low = mid + 1;
      else if (tuple.field3 > field3)
        high = mid - 1;
      else if (tuple.field3 < field3)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static bool Contains31(ArrayList<TernaryTable.Tuple> tuples, int field3, int field1) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples[mid];
      if (tuple.field3 > field3)
        high = mid - 1;
      else if (tuple.field3 < field3)
        low = mid + 1;
      else if (tuple.field1OrNext > field1)
        high = mid - 1;
      else if (tuple.field1OrNext < field1)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static bool Contains3(ArrayList<TernaryTable.Tuple> tuples, int field3) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField3 = tuples[mid].field3;
      if (midField3 > field3)
        high = mid - 1;
      else if (midField3 < field3)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static Comparison<TernaryTable.Tuple> compare123 = delegate(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field1OrNext != t2.field1OrNext)
      return (int) (t1.field1OrNext - t2.field1OrNext);
    else if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
      return (int) (t1.field2OrEmptyMarker - t2.field2OrEmptyMarker);
    else
      return (int) (t1.field3 - t2.field3);
  };

  static Comparison<TernaryTable.Tuple> compare231 = delegate(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
      return (int) (t1.field2OrEmptyMarker - t2.field2OrEmptyMarker);
    else if (t1.field3 != t2.field3)
      return (int) (t1.field3 - t2.field3);
    else
      return (int) (t1.field1OrNext - t2.field1OrNext);
  };

  static Comparison<TernaryTable.Tuple> compare312 = delegate(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field3 != t2.field3)
      return (int) (t1.field3 - t2.field3);
    if (t1.field1OrNext != t2.field1OrNext)
      return (int) (t1.field1OrNext - t2.field1OrNext);
    else
      return (int) (t1.field2OrEmptyMarker - t2.field2OrEmptyMarker);
  };
}
