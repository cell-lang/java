package net.cell_lang;


class TernaryTableUpdater {
  static int[] emptyArray = new int[0];

  // boolean clear = false;

  int deleteCount = 0;
  int[] deleteIdxs = emptyArray;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

  String relvarName;

  TernaryTable table;
  ValueStoreUpdater store1, store2, store3;

  enum Ord {ORD_NONE, ORD_123, ORD_231, ORD_312};
  Ord currOrd = Ord.ORD_NONE;

  public TernaryTableUpdater(String relvarName, TernaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2, ValueStoreUpdater store3) {
    this.relvarName = relvarName;
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
  }

  public void clear() {
    deleteCount = 0;
    TernaryTable.Iter it = table.getIter();
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), it.get2(), it.get3());
      it.next();
    }
  }

  public void insert(int value1, int value2, int value3) {
    insertList = Array.append3(insertList, insertCount++, value1, value2, value3);
  }

  public void delete(int value1, int value2, int value3) {
    int idx = table.containsAt(value1, value2, value3);
    if (idx != -1) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, idx);
      deleteList = Array.append3(deleteList, deleteCount++, value1, value2, value3);
    }
  }

  public void delete12(int value1, int value2) {
    TernaryTable.Iter12 it = table.getIter12(value1, value2);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, value1, value2, it.get1());
      it.next();
    }
  }

  public void delete13(int value1, int value3) {
    TernaryTable.Iter13 it = table.getIter13(value1, value3);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, value1, it.get1(), value3);
      it.next();
    }
  }

  public void delete23(int value2, int value3) {
    TernaryTable.Iter23 it = table.getIter23(value2, value3);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), value2, value3);
      it.next();
    }
  }

  public void delete1(int value1) {
    TernaryTable.Iter1 it = table.getIter1(value1);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, value1, it.get1(), it.get2());
      it.next();
    }
  }

  public void delete2(int arg2) {
    TernaryTable.Iter2 it = table.getIter2(arg2);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), arg2, it.get2());
      it.next();
    }
  }

  public void delete3(int value3) {
    TernaryTable.Iter3 it = table.getIter3(value3);
    while (!it.done()) {
      deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), it.get2(), value3);
      it.next();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // void dump(String msg) {
  //   if (Miscellanea.debugFlag) {
  //     System.out.println();
  //     System.out.println(msg);
  //     for (int i=0 ; i < deleteCount ; i++) {
  //       int idx = deleteIdxs[i];
  //       int offset = 3 * i;
  //       int arg0 = deleteList[offset];
  //       int arg1 = deleteList[offset+1];
  //       int arg2 = deleteList[offset+2];
  //       boolean ok = table.contains(arg0, arg1, arg2);
  //       System.out.printf("delete (%d, %d, %d) @ %d, %s\n", arg0, arg1, arg2, idx, ok);
  //     }
  //     System.out.println();
  //     for (int i=0 ; i < table.flatTuples.length / 3 ; i++) {
  //       int offset = 3 * i;
  //       int arg0 = table.flatTuples[offset];
  //       int arg1 = table.flatTuples[offset+1];
  //       int arg2 = table.flatTuples[offset+2];
  //       System.out.printf("%2d: (%2d, %2d, %2d)\n", i, arg0, arg1, arg2);
  //     }
  //     System.out.printf("\ncurrOrd = %s\n", currOrd);
  //   }
  // }

  public void apply() {
    if (currOrd == Ord.ORD_NONE) {
      // deleteList has not been reordered, so it still matches deleteIdxs
      for (int i=0 ; i < deleteCount ; i++)
        if (!table.deleteAt(deleteIdxs[i]))
          deleteList[3*i] = -1;
    }
    else if (deleteCount != 0) {
      // deleteList was reorder, so the correspondence with deleteIdxs has been lost
      // On the other hand, since deleteList is now ordered, we can eliminate the duplicates

      int DEBUG_count_1 = 0;
      int DEBUG_count_2 = 0;

      for (int i=0 ; i < deleteCount ; i++)
        if (!table.deleteAt(deleteIdxs[i]))
          DEBUG_count_1++;

      int prevArg1 = deleteList[0];
      int prevArg2 = deleteList[1];
      int prevArg3 = deleteList[2];
      for (int i=1 ; i < deleteCount ; i++) {
        int offset = 3 * i;
        int arg1 = deleteList[offset];
        int arg2 = deleteList[offset + 1];
        int arg3 = deleteList[offset + 2];
        if (arg1 == prevArg1 & arg2 == prevArg2 & arg3 == prevArg3) {
          deleteList[offset] = -1;
          DEBUG_count_2++;
        }
        else {
          prevArg1 = arg1;
          prevArg2 = arg2;
          prevArg3 = arg3;
        }
      }

      Miscellanea._assert(DEBUG_count_1 == DEBUG_count_2);
    }

    for (int i=0 ; i < insertCount ; i++) {
      int arg1 = insertList[3 * i];
      int arg2 = insertList[3 * i + 1];
      int arg3 = insertList[3 * i + 2];

      if (!table.contains(arg1, arg2, arg3)) {
        table.insert(arg1, arg2, arg3);
        store1.addRef(arg1);
        store2.addRef(arg2);
        store3.addRef(arg3);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 3 * i;
      int arg1 = deleteList[offset];
      if (arg1 != -1) {
        int arg2 = deleteList[offset+1];
        int arg3 = deleteList[offset+2];
        store1.release(arg1);
        store2.release(arg2);
        store3.release(arg3);
      }
    }
  }

  public void reset() {
    // clear = false;
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 3 * 1024) {
      deleteIdxs = emptyArray;
      deleteList = emptyArray;
    }

    if (insertList.length > 3 * 1024)
      insertList = emptyArray;

    currOrd = Ord.ORD_NONE;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void prepare123() {
    if (deleteCount != 0 | insertCount != 0) {
      Miscellanea._assert(currOrd == Ord.ORD_NONE | currOrd == Ord.ORD_123);
      if (currOrd != Ord.ORD_123) {
        Ints123.sort(deleteList, deleteCount);
        Ints123.sort(insertList, insertCount);
        currOrd = Ord.ORD_123;
      }
    }
  }

  public void prepare231() {
    if (deleteCount != 0 | insertCount != 0) {
      Miscellanea._assert(currOrd != Ord.ORD_312);
      if (currOrd != Ord.ORD_231) {
        Ints231.sort(deleteList, deleteCount);
        Ints231.sort(insertList, insertCount);
        currOrd = Ord.ORD_231;
      }
    }
  }

  public void prepare312() {
    if (deleteCount != 0 | insertCount != 0) {
      if (currOrd != Ord.ORD_312) {
        Ints312.sort(deleteList, deleteCount);
        Ints312.sort(insertList, insertCount);
        currOrd = Ord.ORD_312;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean contains1(int surr1) {
    prepare123();

    if (Ints123.contains1(insertList, insertCount, surr1))
      return true;

    if (!table.contains1(surr1))
      return false;

    int idx = Ints123.indexFirst1(deleteList, deleteCount, surr1);
    if (idx == -1)
      return true;
    int count = Ints123.count1(deleteList, deleteCount, surr1, idx);

    TernaryTable.Iter it = table.getIter1(surr1);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
      // and 2/3/1 order, since the first argument is always the same
      if (!Ints231.contains23(deleteList, idx, count, it.get1(), it.get2()))
        return true;
      it.next();
    }

    return false;
  }

  public boolean contains2(int surr2) {
    prepare231();

    if (Ints231.contains2(insertList, insertCount, surr2))
      return true;

    if (!table.contains2(surr2))
      return false;

    int idx = Ints231.indexFirst2(deleteList, deleteCount, surr2);
    if (idx == -1)
      return true;
    int count = Ints231.count2(deleteList, deleteCount, surr2, idx);

    TernaryTable.Iter it = table.getIter2(surr2);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 2/3/1
      // and 3/1/2 order, since the second argument is always the same
      if (!Ints312.contains13(deleteList, idx, count, it.get1(), it.get2()))
        return true;
      it.next();
    }

    return false;
  }

  public boolean contains3(int surr3) {
    prepare312();

    if (Ints312.contains3(insertList, insertCount, surr3))
      return true;

    if (!table.contains3(surr3))
      return false;

    int idx = Ints312.indexFirst3(deleteList, deleteCount, surr3);
    if (idx == -1)
      return true;
    int count = Ints312.count3(deleteList, deleteCount, surr3, idx);

    TernaryTable.Iter it = table.getIter3(surr3);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 3/1/2
      // and 1/2/3 order, since the third argument is always the same
      if (!Ints123.contains12(deleteList, idx, count, it.get1(), it.get2()))
        return true;
      it.next();
    }

    return false;
  }

  public boolean contains12(int surr1, int surr2) {
    prepare123();

    if (Ints123.contains12(insertList, insertCount, surr1, surr2))
      return true;

    if (!table.contains12(surr1, surr2))
      return false;

    int idx = Ints123.indexFirst12(deleteList, deleteCount, surr1, surr2);
    if (idx == -1)
      return true;
    int count = Ints123.count12(deleteList, deleteCount, surr1, surr2, idx);

    TernaryTable.Iter it = table.getIter12(surr1, surr2);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
      // and 3/1/2 order, since the first two arguments are the same
      if (!Ints312.contains3(deleteList, idx, count, it.get1()))
        return true;
      it.next();
    }

    return false;
  }

  public boolean contains13(int surr1, int surr3) {
    prepare312();

    if (Ints312.contains13(insertList, insertCount, surr1, surr3))
      return true;

    if (!table.contains13(surr1, surr3))
      return false;

    int idx = Ints312.indexFirst31(deleteList, deleteCount, surr3, surr1);
    if (idx == -1)
      return true;
    int count = Ints312.count13(deleteList, deleteCount, surr1, surr3, idx);

    TernaryTable.Iter it = table.getIter13(surr1, surr3);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 3/1/2
      // and 2/3/1 order, since the first and last argument are the same
      if (!Ints231.contains2(deleteList, idx, count, it.get1()))
        return true;
      it.next();
    }

    return false;
  }

  public boolean contains23(int surr2, int surr3) {
    prepare231();

    if (Ints231.contains23(insertList, insertCount, surr2, surr3))
      return true;

    if (!table.contains23(surr2, surr3))
      return false;

    int idx = Ints231.indexFirst23(deleteList, deleteCount, surr2, surr3);
    if (idx == -1)
      return true;
    int count = Ints231.count23(deleteList, deleteCount, surr2, surr3, idx);

    TernaryTable.Iter it = table.getIter23(surr2, surr3);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in any order, since two arguments are the same
      if (!Ints123.contains1(deleteList, idx, count, it.get1()))
        return true;
      it.next();
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int lookupAny12(int surr1, int surr2) {
    prepare123();

    if (Ints123.contains12(insertList, insertCount, surr1, surr2)) {
      int idxFirst = Ints123.indexFirst12(insertList, insertCount, surr1, surr2);
      return insertList[3 * idxFirst + 2];
    }

    int idx = Ints123.indexFirst12(deleteList, deleteCount, surr1, surr2);
    if (idx == -1)
      return table.getIter12(surr1, surr2).get1();

    int count = Ints123.count12(deleteList, deleteCount, surr1, surr2, idx);

    TernaryTable.Iter12 it = table.getIter12(surr1, surr2);
    while (!it.done()) {
      // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
      // and 3/1/2 order, since the first two arguments are the same
      if (!Ints312.contains3(deleteList, idx, count, it.get1()))
        return it.get1();
      it.next();
    }

    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_12() {
    if (insertCount != 0) {
      prepare123();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg1 == prevArg1 & arg2 == prevArg2 & arg3 != prevArg3)
          throw cols12KeyViolationException(arg1, arg2, arg3, prevArg3);

        if (!Ints123.contains12(deleteList, deleteCount, arg1, arg2) && table.contains12(arg1, arg2))
          throw cols12KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  public void checkKey_3() {
    if (insertCount != 0) {
      prepare312();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg3 == prevArg3 & (arg1 != prevArg1 | arg2 != prevArg2))
          throw col3KeyViolationException(arg1, arg2, arg3, prevArg1, prevArg2);

        if (!Ints312.contains3(deleteList, deleteCount, arg3) && table.contains3(arg3))
          throw col3KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  public void checkKey_23() {
    if (insertCount != 0) {
      prepare231();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg2 == prevArg2 & arg3 == prevArg3 & arg1 != prevArg1)
          throw cols23KeyViolationException(arg1, arg2, arg3, prevArg1);

        if (!Ints231.contains23(deleteList, deleteCount, arg2, arg3) && table.contains23(arg2, arg3))
          throw cols23KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  public void checkKey_13() {
    if (insertCount != 0) {
      prepare312();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg1 == prevArg1 & arg3 == prevArg3 & arg2 != prevArg2)
          throw cols13KeyViolationException(arg1, arg2, arg3, prevArg2);

        if (!Ints312.contains13(deleteList, deleteCount, arg1, arg3) && table.contains13(arg1, arg3))
          throw cols13KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public interface DeleteChecker {
    void mightHaveBeenDeleted(int surr1, int surr2, int surr3);
  }

  public void checkDeletes123(DeleteChecker deleteChecker) {
    prepare123();
    checkDeletes(deleteChecker);
  }

  public void checkDeletes231(DeleteChecker deleteChecker) {
    prepare231();
    checkDeletes(deleteChecker);
  }

  public void checkDeletes312(DeleteChecker deleteChecker) {
    prepare312();
    checkDeletes(deleteChecker);
  }

  private void checkDeletes(DeleteChecker deleteChecker) {
    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 3 * i;
      int surr1 = deleteList[offset];
      int surr2 = deleteList[offset+1];
      int surr3 = deleteList[offset+2];
      deleteChecker.mightHaveBeenDeleted(surr1, surr2, surr3);
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException cols12KeyViolationException(int arg1, int arg2, int arg3, int otherArg3) {
    return keyViolationException(arg1, arg2, arg3, arg1, arg2, otherArg3, KeyViolationException.key_12, true);
  }

  private KeyViolationException cols12KeyViolationException(int arg1, int arg2, int arg3) {
    int otherArg3 = table.lookup12(arg1, arg2);
    return keyViolationException(arg1, arg2, arg3, arg1, arg2, otherArg3, KeyViolationException.key_12, false);
  }

  private KeyViolationException col3KeyViolationException(int arg1, int arg2, int arg3, int otherArg1, int otherArg2) {
    return keyViolationException(arg1, arg2, arg3, otherArg1, otherArg2, arg3, KeyViolationException.key_3, true);
  }

  private KeyViolationException col3KeyViolationException(int arg1, int arg2, int arg3) {
    TernaryTable.Iter3 it = table.getIter3(arg3);
    int otherArg1 = it.get1();
    int otherArg2 = it.get2();
    return keyViolationException(arg1, arg2, arg3, otherArg1, otherArg2, arg3, KeyViolationException.key_3, false);
  }

  private KeyViolationException cols23KeyViolationException(int arg1, int arg2, int arg3, int otherArg1) {
    return keyViolationException(arg1, arg2, arg3, otherArg1, arg2, arg3, KeyViolationException.key_23, true);
  }

  private KeyViolationException cols23KeyViolationException(int arg1, int arg2, int arg3) {
    int otherArg1 = table.lookup23(arg2, arg3);
    return keyViolationException(arg1, arg2, arg3, otherArg1, arg2, arg3, KeyViolationException.key_23, false);
  }

  private KeyViolationException cols13KeyViolationException(int arg1, int arg2, int arg3, int otherArg2) {
    return keyViolationException(arg1, arg2, arg3, arg1, otherArg2, arg3, KeyViolationException.key_13, true);
  }

  private KeyViolationException cols13KeyViolationException(int arg1, int arg2, int arg3) {
    int otherArg2 = table.lookup13(arg1, arg3);
    return keyViolationException(arg1, arg2, arg3, arg1, otherArg2, arg3, KeyViolationException.key_13, false);
  }

  private KeyViolationException keyViolationException(int arg1, int arg2, int arg3, int otherArg1, int otherArg2, int otherArg3, int[] key, boolean betweenNew) {
    //## BUG: STORES MAY CONTAIN ONLY PART OF THE ACTUAL VALUE (id(5) -> 5)
    Obj obj1 = store1.surrToValue(arg1);
    Obj obj2 = store2.surrToValue(arg2);
    Obj obj3 = store3.surrToValue(arg3);

    Obj otherObj1 = arg1 == otherArg1 ? obj1 : store1.surrToValue(otherArg1);
    Obj otherObj2 = arg2 == otherArg2 ? obj2 : store2.surrToValue(otherArg2);
    Obj otherObj3 = arg3 == otherArg3 ? obj3 : store3.surrToValue(otherArg3);

    Obj[] tuple1 = new Obj[] {obj1, obj2, obj3};
    Obj[] tuple2 = new Obj[] {otherObj1, otherObj2, otherObj3};

    return new KeyViolationException(relvarName, key, tuple1, tuple2, betweenNew);
  }
}
