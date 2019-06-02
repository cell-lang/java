package net.cell_lang;

import java.util.function.IntPredicate;


class TernaryTableUpdater {
  static int[] emptyArray = new int[0];

  // boolean clear = false;

  int deleteCount = 0;
  int[] deleteIdxs = emptyArray;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

  TernaryTable table;
  ValueStoreUpdater store1, store2, store3;

  enum Ord {ORD_NONE, ORD_123, ORD_231, ORD_312};
  Ord currOrd = Ord.ORD_NONE;

  public TernaryTableUpdater(TernaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2, ValueStoreUpdater store3) {
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
      if (deleteCount >= deleteIdxs.length) {
        int capacity = Array.nextCapacity(deleteIdxs.length);
        deleteIdxs = Array.extend(deleteIdxs, capacity);
        deleteList = Array.extend(deleteList, 3 * capacity);
      }
      deleteIdxs[deleteCount] = it.index();
      int offset = 3 * deleteCount;
      deleteList[offset]   = it.get1();
      deleteList[offset+1] = arg2;
      deleteList[offset+2] = it.get2();
      deleteCount++;

      // deleteIdxs = Array.append(deleteIdxs, deleteCount, it.index());
      // deleteList = Array.append3(deleteList, deleteCount++, it.get1(), arg2, it.get2());

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

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      if (!table.deleteAt(deleteIdxs[i])) {
        int offset = 3 * i;
        table.store1.addRef(deleteList[offset]);
        table.store2.addRef(deleteList[offset+1]);
        table.store3.addRef(deleteList[offset+2]);
      }
    }

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[3 * i];
      int field2 = insertList[3 * i + 1];
      int field3 = insertList[3 * i + 2];

      if (!table.contains(field1, field2, field3)) {
        table.insert(field1, field2, field3);
        table.store1.addRef(field1);
        table.store2.addRef(field2);
        table.store3.addRef(field3);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[3 * i];
      int field2 = deleteList[3 * i + 1];
      int field3 = deleteList[3 * i + 2];
      table.store1.release(field1);
      table.store2.release(field2);
      table.store3.release(field3);
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
    Miscellanea._assert(currOrd == Ord.ORD_NONE | currOrd == Ord.ORD_123);
    if (currOrd != Ord.ORD_123) {
      Ints123.sort(deleteList, deleteCount);
      Ints123.sort(insertList, insertCount);
      currOrd = Ord.ORD_123;
    }
  }

  public void prepare231() {
    Miscellanea._assert(currOrd != Ord.ORD_312);
    if (currOrd != Ord.ORD_231) {
      Ints231.sort(deleteList, deleteCount);
      Ints231.sort(insertList, insertCount);
      currOrd = Ord.ORD_231;
    }
  }

  public void prepare312() {
    if (currOrd != Ord.ORD_312) {
      Ints312.sort(deleteList, deleteCount);
      Ints312.sort(insertList, insertCount);
      currOrd = Ord.ORD_312;
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
      if (!Ints231.contains23(deleteList, idx, count, it.get2(), it.get3())) //## BUG BUG BUG
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
      if (!Ints312.contains13(deleteList, idx, count, it.get1(), it.get3())) //## BUG BUG BUG
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
      if (!Ints231.contains2(deleteList, idx, count, it.get2())) //## BUG BUG BUG
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
      if (!Ints123.contains1(deleteList, idx, count, it.get1())) //## BUG BUG BUG
        return true;
      it.next();
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean checkKey_12() {
    if (insertCount == 0)
      return true;

    prepare123();

    int prevField1 = -1;
    int prevField2 = -1;
    int prevField3 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[3 * i];
      int field2 = insertList[3 * i + 1];
      int field3 = insertList[3 * i + 2];

      if (field1 == prevField1 & field2 == prevField2 & field3 != prevField3)
        return false;

      if (!Ints123.contains12(deleteList, deleteCount, field1, field2) && table.contains12(field1, field2))
        return false;

      prevField1 = field1;
      prevField2 = field2;
      prevField3 = field3;
    }

    return true;
  }

  public boolean checkKey_3() {
    if (insertCount == 0)
      return true;

    prepare312();

    int prevField1 = -1;
    int prevField2 = -1;
    int prevField3 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[3 * i];
      int field2 = insertList[3 * i + 1];
      int field3 = insertList[3 * i + 2];

      if (field3 == prevField3 & (field1 != prevField1 | field2 != prevField2))
        return false;

      if (!Ints312.contains3(deleteList, deleteCount, field3) && table.contains3(field3))
        return false;

      prevField1 = field1;
      prevField2 = field2;
      prevField3 = field3;
    }

    return true;
  }

  public boolean checkKey_23() {
    if (insertCount == 0)
      return true;

    prepare231();

    int prevField1 = -1;
    int prevField2 = -1;
    int prevField3 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[3 * i];
      int field2 = insertList[3 * i + 1];
      int field3 = insertList[3 * i + 2];

      if (field2 == prevField2 & field3 == prevField3 & field1 != prevField1)
        return false;

      if (!Ints231.contains23(deleteList, deleteCount, field2, field3) && table.contains23(field2, field3))
        return false;

      prevField1 = field1;
      prevField2 = field2;
      prevField3 = field3;
    }

    return true;
  }

  public boolean checkKey_13() {
    if (insertCount == 0)
      return true;

    prepare312();

    int prevField1 = -1;
    int prevField2 = -1;
    int prevField3 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[3 * i];
      int field2 = insertList[3 * i + 1];
      int field3 = insertList[3 * i + 2];

      if (field1 == prevField1 & field3 == prevField3 & field2 != prevField2)
        return false;

      if (!Ints312.contains13(deleteList, deleteCount, field1, field3) && table.contains13(field1, field3))
        return false;

      prevField1 = field1;
      prevField2 = field2;
      prevField3 = field3;
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean checkDeletedKeys_1(IntPredicate source) {
    prepare123();

    for (int i=0 ; i < deleteCount ; ) {
      int surr1 = deleteList[3 * i];
      if (!Ints123.contains1(insertList, insertCount, surr1) && source.test(surr1)) {
        Miscellanea._assert(source.test(surr1));
        int surr2 = deleteList[3 * i + 1];
        int surr3 = deleteList[3 * i + 2];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i] == surr1 ; i++) {
          int currSurr2 = deleteList[3 * i + 1];
          int currSurr3 = deleteList[3 * i + 2];
          if (currSurr2 != surr2 | currSurr3 != surr3) {
            surr2 = currSurr2;
            surr3 = currSurr3;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count1Eq(surr1, removedCount))
          return false;
      }
      else
        i++;
    }
    return true;
  }

  public boolean checkDeletedKeys_2(IntPredicate source) {
    prepare231();

    for (int i=0 ; i < deleteCount ; ) {
      int surr2 = deleteList[3 * i + 1];
      if (!Ints231.contains2(insertList, insertCount, surr2) && source.test(surr2)) {
        Miscellanea._assert(source.test(surr2));
        int surr1 = deleteList[3 * i];
        int surr3 = deleteList[3 * i + 2];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i+1] == surr2 ; i++) {
          int currSurr1 = deleteList[3 * i];
          int currSurr3 = deleteList[3 * i + 2];
          if (currSurr1 != surr1 | currSurr3 != surr3) {
            surr1 = currSurr1;
            surr3 = currSurr3;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count2Eq(surr2, removedCount))
          return false;
      }
      else
        i++;
    }
    return true;
  }

  public boolean checkDeletedKeys_3(IntPredicate source) {
    prepare312();

    for (int i=0 ; i < deleteCount ; ) {
      int surr3 = deleteList[3 * i + 2];
      if (!Ints312.contains3(insertList, insertCount, surr3) && source.test(surr3)) {
        Miscellanea._assert(source.test(surr3));
        int surr1 = deleteList[3 * i];
        int surr2 = deleteList[3 * i + 1];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i+2] == surr3 ; i++) {
          int currSurr1 = deleteList[3 * i];
          int currSurr2 = deleteList[3 * i + 1];
          if (currSurr1 != surr1 | currSurr2 != surr2) {
            surr1 = currSurr1;
            surr2 = currSurr2;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count3Eq(surr3, removedCount))
          return false;
      }
      else
        i++;
    }

    return true;
  }

  public boolean checkDeletedKeys_12(BiIntPredicate source) {
    prepare123();

    for (int i=0 ; i < deleteCount ; ) {
      int offset = 3 * i;
      int surr1 = deleteList[offset];
      int surr2 = deleteList[offset + 1];
      if (!Ints123.contains12(insertList, insertCount, surr1, surr2) && source.test(surr1, surr2)) {
        int surr3 = deleteList[3 * i + 2];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i] == surr1 && deleteList[3*i+1] == surr2 ; i++) {
          int currSurr3 = deleteList[3 * i + 2];
          if (currSurr3 != surr3) {
            surr3 = currSurr3;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count12Eq(surr1, surr2, removedCount))
          return false;
      }
      else
        i++;
    }
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // tern_rel(a, _, _) -> unary_rel(a);
  public boolean checkForeignKeys_1(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains1);
  }

  // tern_rel(_, b, _) -> unary_rel(b);
  public boolean checkForeignKeys_2(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+1]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains2);
  }

  // tern_rel(_, _, c) -> unary_rel(c)
  public boolean checkForeignKeys_3(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+2]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains3);
  }

  // tern_rel(a, b, _) -> binary_rel(a, b)
  public boolean checkForeignKeys_12(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i], insertList[3*i+1]))
        return false;
    return target.checkDeletedKeys_12(this::contains12);
  }
}
