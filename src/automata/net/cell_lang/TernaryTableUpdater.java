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

  public void clear() {
    deleteList.clear();
    TernaryTable.Iter it = table.getIter();
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void set(Obj value, int idx1, int idx2, int idx3) {
    Miscellanea._assert(deleteList.Count == 0 || deleteList.Count == table.count);
    Miscellanea._assert(insertList.Count == 0);

    Clear();
    TernRelIter it = value.getTernRelIter();
    while (!it.done()) {
      Obj val1 = idx1 == 0 ? it.get1() : (idx1 == 1 ? it.get2() : it.get3());
      Obj val2 = idx2 == 0 ? it.get1() : (idx2 == 1 ? it.get2() : it.get3());
      Obj val3 = idx3 == 0 ? it.get1() : (idx3 == 1 ? it.get2() : it.get3());
      int surr1 = store1.lookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.insert(val1);
      int surr2 = store2.lookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.insert(val2);
      int surr3 = store3.lookupValueEx(val3);
      if (surr3 == -1)
        surr3 = store3.insert(val3);
      insertList.add(new TernaryTable.tuple(surr1, surr2, surr3));
      it.next();
    }
  }

  public void insert(long value1, long value2, long value3) {
    insertList.add(new TernaryTable.tuple(value1, value2, value3));
  }

  public void delete(long value1, long value2, long value3) {
    if (table.contains(value1, value2, value3))
      deleteList.add(new TernaryTable.tuple(value1, value2, value3));
  }

  public void delete12(long value1, long value2) {
    TernaryTable.Iter it = table.getIter12(value1, value2);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete13(long value1, long value3) {
    TernaryTable.Iter it = table.getIter13(value1, value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete23(long value2, long value3) {
    TernaryTable.Iter it = table.getIter23(value2, value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete1(long value1) {
    TernaryTable.Iter it = table.getIter1(value1);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete2(long value2) {
    TernaryTable.Iter it = table.getIter2(value2);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete3(long value3) {
    TernaryTable.Iter it = table.getIter3(value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public bool CheckUpdates_12() {
    deleteList.sort(compare123);
    insertList.sort(compare123);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains12(deleteList, prev.field1OrNext, prev.field2OrEmptyMarker))
      if (table.contains12(prev.field1OrNext, prev.field2OrEmptyMarker))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 != prev.field3
         )
        return false;
      if (!Contains12(deleteList, curr.field1OrNext, curr.field2OrEmptyMarker))
        if (table.contains12(curr.field1OrNext, curr.field2OrEmptyMarker))
          return false;
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_3() {
    if (!CheckUpdates_12())
      return false;

    deleteList.sort(compare312);
    insertList.sort(compare312);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains3(deleteList, prev.field3))
      if (table.contains3(prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field3 == prev.field3 &
           (curr.field1OrNext != prev.field1OrNext | curr.field2OrEmptyMarker != prev.field2OrEmptyMarker)
         )
        return false;
      if (!Contains3(deleteList, prev.field3))
        if (table.contains3(prev.field3))
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_23() {
    if (!CheckUpdates_12())
      return false;

    deleteList.sort(compare231);
    insertList.sort(compare231);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains23(deleteList, prev.field2OrEmptyMarker, prev.field3))
      if (table.contains23(prev.field2OrEmptyMarker, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 == prev.field3 &
           curr.field1OrNext != prev.field1OrNext
         )
        return false;
      if (!Contains23(deleteList, curr.field2OrEmptyMarker, curr.field3))
        if (table.contains23(curr.field2OrEmptyMarker, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public bool CheckUpdates_12_23_31() {
    if (!CheckUpdates_12_23())
      return false;

    deleteList.sort(compare312);
    insertList.sort(compare312);

    int count = insertList.Count;
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList[0];
    if (!Contains31(deleteList, prev.field3, prev.field1OrNext))
      if (table.contains13(prev.field1OrNext, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList[i];
      if ( curr.field3 == prev.field3 &
           curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker != prev.field2OrEmptyMarker
         )
        return false;
      if (!Contains31(deleteList, curr.field3, curr.field1OrNext))
        if (table.contains13(curr.field1OrNext, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public void apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      var tuple = deleteList[i];
      if (table.contains(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3))
        table.delete(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3);
      else
        deleteList[i] = new TernaryTable.tuple(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    var it = insertList.getEnumerator();
    while (it.moveNext()) {
      var curr = it.Current;
      if (!table.contains(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3)) {
        table.insert(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3);
        table.store1.addRef(curr.field1OrNext);
        table.store2.addRef(curr.field2OrEmptyMarker);
        table.store3.addRef(curr.field3);
      }
    }
  }

  public void finish() {
    var it = deleteList.getEnumerator();
    while (it.moveNext()) {
      var tuple = it.Current;
      if (tuple.field1OrNext != 0xFFFFFFFF) {
        table.store1.release(tuple.field1OrNext);
        table.store2.release(tuple.field2OrEmptyMarker);
        table.store3.release(tuple.field3);
      }
    }
  }

  public void reset() {
    deleteList.clear();
    insertList.clear();
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
