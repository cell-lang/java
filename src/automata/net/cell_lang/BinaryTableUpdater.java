package net.cell_lang;


class BinaryTableUpdater {
  static class Tuple {
    public int field1;
    public int field2;

    public Tuple(int field1, int field2) {
      this.field1 = field1;
      this.field2 = field2;
    }

    @Override
    public String toString() {
      return "(" + field1.toString() + ", " + field2.toString() + ")";
    }
  }

  ArrayList<Tuple> deleteList = new ArrayList<Tuple>();
  ArrayList<Tuple> insertList = new ArrayList<Tuple>();

  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  public BinaryTableUpdater(BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  public void clear() {
    int[,] columns = table.rawCopy();
    int len = columns.getLength(0);
    deleteList.clear();
    for (int i=0 ; i < len ; i++)
      deleteList.add(new Tuple(columns[i, 0], columns[i, 1]));
  }

  public void set(Obj value, boolean flipped) {
    Clear();
    Miscellanea._assert(insertList.Count == 0);
    BinRelIter it = value.getBinRelIter();
    while (!it.done()) {
      Obj val1 = flipped ? it.get2() : it.get1();
      Obj val2 = flipped ? it.get1() : it.get2();
      int surr1 = store1.lookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.insert(val1);
      int surr2 = store2.lookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.insert(val2);
      insertList.add(new Tuple(surr1, surr2));
      it.next();
    }
  }

  public void delete(long value1, long value2) {
    if (table.contains(value1, value2))
      deleteList.add(new Tuple(value1, value2));
  }

  public void delete1(long value) {
    int[] assocs = table.lookupByCol1(value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList.add(new Tuple(value, assocs[i]));
  }

  public void delete2(long value) {
    int[] assocs = table.lookupByCol2(value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList.add(new Tuple(assocs[i], value));
  }

  public void insert(long value1, long value2) {
    insertList.add(new Tuple(value1, value2));
  }

  public boolean CheckUpdates_1() {
    Comparison<Tuple> cmp = delegate(Tuple t1, Tuple t2) {
      return (int) (t1.field1 != t2.field1 ? t1.field1 - t2.field1 : t1.field2 - t2.field2);
    };

    deleteList.sort(cmp);
    insertList.sort(cmp);

    int count = insertList.Count;
    if (count == 0)
      return true;

    Tuple prev = insertList[0];
    if (!ContainsField1(deleteList, prev.field1))
      if (table.containsField1(prev.field1))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList[i];
      if (curr.field1 == prev.field1 & curr.field2 != prev.field2)
        return false;
      if (!ContainsField1(deleteList, curr.field1))
        if (table.containsField1(curr.field1))
          return false;
      prev = curr;
    }

    return true;
  }

  public boolean CheckUpdates_1_2() {
    if (!CheckUpdates_1())
      return false;

    Comparison<Tuple> cmp = delegate(Tuple t1, Tuple t2) {
      return (int) (t1.field2 != t2.field2 ? t1.field2 - t2.field2 : t1.field1 - t2.field1);
    };

    deleteList.sort(cmp);
    insertList.sort(cmp);

    int count = insertList.Count;
    if (count == 0)
      return true;

    Tuple prev = insertList[0];
    if (!ContainsField2(deleteList, prev.field2))
      if (table.containsField2(prev.field2))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList[i];
      if (curr.field2 == prev.field2 & curr.field1 != prev.field1)
        return false;
      if (!ContainsField2(deleteList, curr.field2))
        if (table.containsField2(curr.field2))
          return false;
      prev = curr;
    }

    return true;
  }

  public void apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      Tuple tuple = deleteList[i];
      if (table.contains(tuple.field1, tuple.field2)) {
        table.delete(tuple.field1, tuple.field2);
      }
      else
        deleteList[i] = new Tuple(0xFFFFFFFF, 0xFFFFFFFF);
    }

    var it = insertList.getEnumerator();
    while (it.moveNext()) {
      var curr = it.Current;
      if (!table.contains(curr.field1, curr.field2)) {
        table.insert(curr.field1, curr.field2);
        table.store1.addRef(curr.field1);
        table.store2.addRef(curr.field2);
      }
    }
  }

  public void finish() {
    var it = deleteList.getEnumerator();
    while (it.moveNext()) {
      var tuple = it.Current;
      if (tuple.field1 != 0xFFFFFFFF) {
        Miscellanea._assert(table.store1.lookupSurrogate(tuple.field1) != null);
        Miscellanea._assert(table.store2.lookupSurrogate(tuple.field2) != null);
        table.store1.release(tuple.field1);
        table.store2.release(tuple.field2);
      }
    }
    Reset();
  }

  public void reset() {
    deleteList.clear();
    insertList.clear();
  }

  public void dump() {
    System.out.print("deleteList =");
    for (int i=0 ; i < deleteList.Count ; i++)
      System.out.print(" {0}", deleteList[i]);
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertList.Count ; i++)
      System.out.print(" {0}", insertList[i]);
    System.out.println("\n");

    System.out.print("deleteList =");
    for (int i=0 ; i < deleteList.Count ; i++) {
      Tuple tuple = deleteList[i];
      Obj obj1 = store1.lookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.lookupSurrogateEx(tuple.field2);
      System.out.print(" ({0}, {1})", obj1, obj2);
    }
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertList.Count ; i++) {
      Tuple tuple = insertList[i];
      Obj obj1 = store1.lookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.lookupSurrogateEx(tuple.field2);
      System.out.print(" ({0}, {1})", obj1, obj2);
    }
    System.out.println("\n\n{0}\n\n", table.copy(true));

    store1.dump();
    store2.dump();
  }

  static boolean ContainsField1(ArrayList<Tuple> tuples, int field1) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField1 = tuples[mid].field1;
      if (midField1 > field1)
        high = mid - 1;
      else if (midField1 < field1)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static boolean ContainsField2(ArrayList<Tuple> tuples, int field2) {
    int low = 0;
    int high = tuples.Count - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField2 = tuples[mid].field2;
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
