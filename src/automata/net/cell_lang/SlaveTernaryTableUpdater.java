package net.cell_lang;


class SlaveTernaryTableUpdater {
  boolean clear = false;

  int deleteCount = 0;
  int[] deleteList = Array.emptyIntArray;
  int[] surrs12_args3 = Array.emptyIntArray;

  int insertCount = 0;
  int[] insertList = Array.emptyIntArray;

  // Temporary buffer used throughout the class
  // Does not hold any permanent information
  int[] buffer = Array.emptyIntArray;

  String relvarName;

  SlaveTernaryTable table;
  MasterBinaryTable table12;
  BinaryTable table3;

  ValueStoreUpdater store1, store2, store3;

  enum Ord {ORD_NONE, ORD_123, ORD_231, ORD_312};
  Ord currOrd = Ord.ORD_NONE;


  public SlaveTernaryTableUpdater(String relvarName, SlaveTernaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2, ValueStoreUpdater store3) {
    this.relvarName = relvarName;
    this.table = table;
    this.table12 = table.table12;
    this.table3 = table.table3;
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
  }

  public void clear() {
    clear = true;
    deleteCount = 0;
  }

  public void insert(int arg1, int arg2, int arg3) {
    insertList = Array.append3(insertList, insertCount++, arg1, arg2, arg3);
  }

  public void delete(int arg1, int arg2, int arg3) {
    if (!clear) {
      int surr12 = table12.surrogate(arg1, arg2);
      if (surr12 != 0xFFFFFFFF)
        if (table3.contains(surr12, arg3)) {
          surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
          deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
        }
    }
  }

  public void delete12(int arg1, int arg2) {
    if (!clear) {
      int surr12 = table12.surrogate(arg1, arg2);
      if (surr12 != 0xFFFFFFFF) {
        int count3 = table3.count1(surr12);
        if (count3 == 1) {
          //## THIS IS PROBABLY UNNECESSARY NOW - CHECK
          int arg3 = table3.lookup1(surr12);
          surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
          deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
        }
        else if (count3 > 1) {
          adjustBuffer(count3);
          int _count = table3.restrict1(surr12, buffer);
          Miscellanea._assert(_count == count3);
          for (int j=0 ; j < count3 ; j++) {
            int arg3 = buffer[j];
            surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
            deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
          }
        }
      }
    }
  }

  public void delete13(int arg1, int arg3) {
    if (!clear) {
      //## HERE WE SHOULD EVALUATE THE (TWO? MORE?) POSSIBLE EXECUTION PATHS
      // Current path: arg1 -> arg2* -> surr12 -> arg3
      // Alternative path: arg3 -> surr12* -> == arg1, arg2
      int count = table12.count1(arg1);
      if (count > 0) {
        adjustBuffer(count);
        //## COULD BE MORE EFFICIENT IF WE HAD A MasterBinaryTable.restrict1(int, int[], int[]) HERE
        int _count = table12.restrict1(arg1, buffer);
        Miscellanea._assert(_count == count);
        for (int i=0 ; i < count ; i++) {
          int arg2 = buffer[i];
          int surr12 = table12.surrogate(arg1, arg2);
          if (table3.contains(surr12, arg3)) {
            surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
            deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
          }
        }
      }
    }
  }

  public void delete23(int arg2, int arg3) {
    if (!clear) {
      //## HERE WE SHOULD EVALUATE THE (TWO? MORE?) POSSIBLE EXECUTION PATHS
      // Current path: arg3 -> surr12* -> == arg2, arg1
      // Alternative path: arg2 -> arg1* -> surr12 -> == arg3
      int count = table3.count2(arg3);
      if (count > 0) {
        adjustBuffer(count);
        int _count = table3.restrict2(arg3, buffer);
        Miscellanea._assert(_count == count);
        for (int i=0 ; i < count ; i++) {
          int surr12 = buffer[i];
          if (table12.arg2(surr12) == arg2) {
            int arg1 = table12.arg1(surr12);
            surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
            deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
          }
        }
      }
    }
  }

  public void delete1(int arg1) {
    if (!clear) {
      int count = table12.count1(arg1);
      if (count > 0) {
        adjustBuffer(count);
        //## COULD BE MORE EFFICIENT IF WE HAD A MasterBinaryTable.restrict1(int, int[], int]) HERE
        int _count = table12.restrict1(arg1, buffer);
        Miscellanea._assert(_count == count);
        for (int i=0 ; i < count ; i++)
          delete12(arg1, buffer[i]);
      }
    }
  }

  public void delete2(int arg2) {
    if (!clear) {
      int count = table12.count2(arg2);
      if (count > 0) {
        adjustBuffer(count);
        int _count = table12.restrict2(arg2, buffer);
        Miscellanea._assert(_count == count);
        for (int i=0 ; i < count ; i++)
          delete12(buffer[i], arg2);
      }
    }
  }

  public void delete3(int arg3) {
    if (!clear) {
      int count = table3.count2(arg3);
      if (count > 0) {
        adjustBuffer(count);
        int _count = table3.restrict2(arg3, buffer);
        Miscellanea._assert(_count == count);
        for (int i=0 ; i < count ; i++) {
          int surr12 = buffer[i];
          int arg1 = table12.arg1(surr12);
          int arg2 = table12.arg2(surr12);

          surrs12_args3 = Array.append2(surrs12_args3, deleteCount, surr12, arg3);
          deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private void adjustBuffer(int minSize) {
    if (buffer.length < minSize)
      buffer = new int[Array.capacity(buffer.length, minSize)];
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void apply() {
    if (clear) {
      table.clear();
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int offset = 2 * i;
        int surr12 = surrs12_args3[offset];
        int arg3 = surrs12_args3[offset + 1];

        if (table.delete(surr12, arg3))
          store3.markForDelayedRelease(arg3);
      }
    }

    for (int i=0 ; i < insertCount ; i++) {
      int offset = 3 * i;
      int arg1 = insertList[3 * i];
      int arg2 = insertList[3 * i + 1];
      int arg3 = insertList[3 * i + 2];

      int surr12 = table12.surrogate(arg1, arg2);
      Miscellanea._assert(surr12 != 0xFFFFFFFF);
      if (table.insert(surr12, arg3))
        store3.addRef(arg3);
    }
  }

  public void reset() {
    clear = false;

    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 3 * 1024) {
      deleteList = Array.emptyIntArray;
      surrs12_args3 = Array.emptyIntArray;
    }

    if (insertList.length > 3 * 1024)
      insertList = Array.emptyIntArray;

    if (buffer.length > 1024)
      buffer = Array.emptyIntArray;

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

  public boolean contains1(int arg1) {
    prepare123();

    if (Ints123.contains1(insertList, insertCount, arg1))
      return true;

    if (clear)
      return false;

    if (!table.contains1(arg1))
      return false;

    int idx = Ints123.indexFirst1(deleteList, deleteCount, arg1);
    if (idx == -1)
      return true;
    int count = Ints123.count1(deleteList, deleteCount, arg1, idx);

    int[] args2 = table12.restrict1(arg1);
    int[] args3 = Array.emptyIntArray;
    for (int i2=0 ; i2 < args2.length ; i2++) {
      int arg2 = args2[i2];
      int surr12 = table12.surrogate(arg1, arg2);
      int count3 = table3.count1(surr12);
      if (count3 > 0) {
        if (args3.length < count3)
          args3 = new int[Array.capacity(args3.length, count3, 8)];
        table3.restrict1(surr12, args3);
        for (int i3=0 ; i3 < count3 ; i3++) {
          // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
          // and 2/3/1 order, since the first argument is always the same
          if (!Ints231.contains23(deleteList, idx, count, arg2, args3[i3]))
            return true;
        }
      }
    }

    return false;
  }

  public boolean contains2(int arg2) {
    prepare231();

    if (Ints231.contains2(insertList, insertCount, arg2))
      return true;

    if (clear)
      return false;

    if (!table.contains2(arg2))
      return false;

    int idx = Ints231.indexFirst2(deleteList, deleteCount, arg2);
    if (idx == -1)
      return true;
    int count = Ints231.count2(deleteList, deleteCount, arg2, idx);

    int[] args1 = table12.restrict2(arg2);
    int[] args3 = Array.emptyIntArray;
    for (int i1=0 ; i1 < args1.length ; i1++) {
      int arg1 = args1[i1];
      int surr12 = table12.surrogate(arg1, arg2);
      int count3 = table3.count1(surr12);
      if (count3 > 0) {
        if (args3.length < count3)
          args3 = new int[Array.capacity(args3.length, count3, 8)];
        table3.restrict1(surr12, args3);
        for (int i3=0 ; i3 < count3 ; i3++) {
          // Tuples in the [idx, idx+count) range are sorted in both 2/3/1
          // and 3/1/2 order, since the second argument is always the same
          if (!Ints312.contains13(deleteList, idx, count, arg1, args3[i3]))
            return true;
        }
      }
    }

    return false;
  }

  public boolean contains3(int arg3) {
    prepare312();

    if (Ints312.contains3(insertList, insertCount, arg3))
      return true;

    if (clear)
      return false;

    if (!table.contains3(arg3))
      return false;

    int idx = Ints312.indexFirst3(deleteList, deleteCount, arg3);
    if (idx == -1)
      return true;
    int count = Ints312.count3(deleteList, deleteCount, arg3, idx);

    int[] surrs12 = table3.restrict2(arg3);
    for (int i=0 ; i < surrs12.length ; i++) {
      int surr12 = surrs12[i];
      if (table12.isValidSurr(surr12)) {
        int arg1 = table12.arg1(surr12);
        int arg2 = table12.arg2(surr12);
        // Tuples in the [idx, idx+count) range are sorted in both 3/1/2
        // and 1/2/3 order, since the third argument is always the same
        if (!Ints123.contains12(deleteList, idx, count, arg1, arg2))
          return true;
      }
    }

    return false;
  }

  public boolean contains12(int arg1, int arg2) {
    prepare123();

    if (Ints123.contains12(insertList, insertCount, arg1, arg2))
      return true;

    if (clear)
      return false;

    if (!table.contains12(arg1, arg2))
      return false;

    int idx = Ints123.indexFirst12(deleteList, deleteCount, arg1, arg2);
    if (idx == -1)
      return true;
    int count = Ints123.count12(deleteList, deleteCount, arg1, arg2, idx);

    int surr12 = table12.surrogate(arg1, arg2);
    Miscellanea._assert(surr12 != 0xFFFFFFFF);
    int[] args3 = table3.restrict1(surr12);
    Miscellanea._assert(args3.length > 0);
    for (int i=0 ; i < args3.length ; i++)
      // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
      // and 3/1/2 order, since the first two arguments are the same
      if (!Ints312.contains3(deleteList, idx, count, args3[i]))
        return true;

    return false;
  }

  public boolean contains13(int arg1, int arg3) {
    prepare312();

    if (Ints312.contains13(insertList, insertCount, arg1, arg3))
      return true;

    if (clear)
      return false;

    if (!table.contains13(arg1, arg3))
      return false;

    int idx = Ints312.indexFirst31(deleteList, deleteCount, arg3, arg1);
    if (idx == -1)
      return true;
    int count = Ints312.count13(deleteList, deleteCount, arg1, arg3, idx);

    int[] args2 = table12.restrict1(arg1);
    for (int i=0 ; i < args2.length ; i++) {
      int arg2 = args2[i];
      int surr12 = table12.surrogate(arg1, arg2);
      if (table3.contains(surr12, arg3))
        // Tuples in the [idx, idx+count) range are sorted in both 3/1/2
        // and 2/3/1 order, since the first and last argument are the same
        if (!Ints231.contains2(deleteList, idx, count, args2[i]))
          return true;
    }

    return false;
  }

  public boolean contains23(int arg2, int arg3) {
    prepare231();

    if (Ints231.contains23(insertList, insertCount, arg2, arg3))
      return true;

    if (clear)
      return false;

    if (!table.contains23(arg2, arg3))
      return false;

    int idx = Ints231.indexFirst23(deleteList, deleteCount, arg2, arg3);
    if (idx == -1)
      return true;
    int count = Ints231.count23(deleteList, deleteCount, arg2, arg3, idx);

    int[] surrs12 = table3.restrict2(arg3);
    for (int i=0 ; i < surrs12.length ; i++) {
      int arg1 = table12.arg1(surrs12[i]);
      // Tuples in the [idx, idx+count) range are sorted in any order, since two arguments are the same
      if (!Ints123.contains1(deleteList, idx, count, arg1))
        return true;
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int lookupAny12(int arg1, int arg2) {
    prepare123();

    // Looking among the newly inserted entries
    if (Ints123.contains12(insertList, insertCount, arg1, arg2)) {
      int idxFirst = Ints123.indexFirst12(insertList, insertCount, arg1, arg2);
      return insertList[3 * idxFirst + 2];
    }

    // Locating start of deleted entries
    int idx = Ints123.indexFirst12(deleteList, deleteCount, arg1, arg2);

    // Retrieving values for arg3
    int surr12 = table12.surrogate(arg1, arg2);
    Miscellanea._assert(surr12 != 0xFFFFFFFF);
    int[] args3 = table3.restrict1(surr12);

    // Returning the first value is no tuple was deleted
    if (idx == -1)
      return args3[0];

    // Counting the number of deleted entries
    int count = Ints123.count12(deleteList, deleteCount, arg1, arg2, idx);

    for (int i=0 ; i < args3.length ; i++) {
      int arg3 = args3[i];
      // Tuples in the [idx, idx+count) range are sorted in both 1/2/3
      // and 3/1/2 order, since the first two arguments are the same
      if (!Ints312.contains3(deleteList, idx, count, arg3))
        return arg3;
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
    int surr12 = table3.restrict2(arg3)[0]; //## BAD: WE WOULD NEED lookupAny2(..)
    int otherArg1 = table12.arg1(surr12);
    int otherArg2 = table12.arg2(surr12);
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
