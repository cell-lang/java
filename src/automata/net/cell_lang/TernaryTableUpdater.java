package net.cell_lang;


class TernaryTableUpdater {
  static int[] emptyArray = new int[0];

  // boolean clear = false;

  int deleteCount = 0;
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
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, it.get1(), it.get2(), it.get3());
      it.next();
    }
  }

  public void set(Obj value, int idx1, int idx2, int idx3) {
    Miscellanea._assert(deleteCount == 0 || deleteCount == table.count);
    Miscellanea._assert(insertCount == 0);

    clear();
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
      insertList = Miscellanea.array3Append(insertList, insertCount++, surr1, surr2, surr3);
      it.next();
    }
  }

  public void insert(int value1, int value2, int value3) {
    insertList = Miscellanea.array3Append(insertList, insertCount++, value1, value2, value3);
  }

  public void delete(int value1, int value2, int value3) {
    if (table.contains(value1, value2, value3))
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, value1, value2, value3);
  }

  public void delete12(int value1, int value2) {
    TernaryTable.Iter it = table.getIter12(value1, value2);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, value1, value2, it.get3());
      it.next();
    }
  }

  public void delete13(int value1, int value3) {
    TernaryTable.Iter it = table.getIter13(value1, value3);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, value1, it.get2(), value3);
      it.next();
    }
  }

  public void delete23(int value2, int value3) {
    TernaryTable.Iter it = table.getIter23(value2, value3);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, it.get1(), value2, value3);
      it.next();
    }
  }

  public void delete1(int value1) {
    TernaryTable.Iter it = table.getIter1(value1);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, value1, it.get2(), it.get3());
      it.next();
    }
  }

  public void delete2(int value2) {
    TernaryTable.Iter it = table.getIter2(value2);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, it.get1(), value2, it.get3());
      it.next();
    }
  }

  public void delete3(int value3) {
    TernaryTable.Iter it = table.getIter3(value3);
    while (!it.done()) {
      deleteList = Miscellanea.array3Append(deleteList, deleteCount++, it.get1(), it.get2(), value3);
      it.next();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[3 * i];
      int field2 = deleteList[3 * i + 1];
      int field3 = deleteList[3 * i + 2];
      if (table.contains(field1, field2, field3))
        table.delete(field1, field2, field3);
      else
        deleteList[3 * i] = 0xFFFFFFFF;
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
      if (field1 != 0xFFFFFFFF) {
        int field2 = deleteList[3 * i + 1];
        int field3 = deleteList[3 * i + 2];
        table.store1.release(field1);
        table.store2.release(field2);
        table.store3.release(field3);
      }
    }
  }

  public void reset() {
    // clear = false;
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 3 * 1024)
      deleteList = emptyArray;
    if (insertList.length > 3 * 1024)
      insertList = emptyArray;

    currOrd = Ord.ORD_NONE;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void prepare123() {
    if (currOrd != Ord.ORD_123) {
      Ints123.sort(deleteList, deleteCount);
      Ints123.sort(insertList, insertCount);
      currOrd = Ord.ORD_123;
    }
  }

  public void prepare231() {
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
      if (!Ints231.contains23(deleteList, idx, count, it.get2(), it.get3()))
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
      if (!Ints312.contains13(deleteList, idx, count, it.get1(), it.get3()))
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
      if (!Ints312.contains3(deleteList, idx, count, it.get3()))
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
      if (!Ints231.contains2(deleteList, idx, count, it.get2()))
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
  //////////////////////////////////////////////////////////////////////////////

  public boolean checkKeys_12() {
    if (insertCount == 0)
      return true;

    Ints123.sort(deleteList, deleteCount);
    Ints123.sort(insertList, insertCount);

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

  public boolean checkKeys_12_3() {
    if (insertCount == 0)
      return true;

    if (!checkKeys_12())
      return false;

    Ints312.sort(deleteList, deleteCount);
    Ints312.sort(insertList, insertCount);

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

  public boolean checkKeys_12_23() {
    if (insertCount == 0)
      return true;

    if (!checkKeys_12())
      return false;

    Ints231.sort(deleteList, deleteCount);
    Ints231.sort(insertList, insertCount);

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

  public boolean checkKeys_12_23_31() {
    if (insertCount == 0)
      return true;

    if (!checkKeys_12_23())
      return false;

    Ints312.sort(deleteList, deleteCount);
    Ints312.sort(insertList, insertCount);

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

  public boolean checkDeletedKeys_1(UnaryTableUpdater source) {
    prepare123();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr1 = deleteList[3 * i];
      if (!Ints123.contains1(insertList, insertCount, surr1))
        if (source.contains(surr1))
          return false;
    }
    return true;
  }

  public boolean checkDeletedKeys_2(UnaryTableUpdater source) {
    prepare231();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr2 = deleteList[3 * i + 1];
      if (!Ints231.contains2(insertList, insertCount, surr2))
        if (source.contains(surr2))
          return false;
    }
    return true;
  }

  public boolean checkDeletedKeys_3(UnaryTableUpdater source) {
    prepare312();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr3 = deleteList[3 * i + 2];
      if (!Ints312.contains3(insertList, insertCount, surr3))
        if (source.contains(surr3))
          return false;
    }
    return true;
  }

  public boolean checkDeletedKeys_12(BinaryTableUpdater source, boolean flip) {
    prepare123();

    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 3 * i;
      int surr1 = deleteList[offset];
      int surr2 = deleteList[offset + 1];
      if (!Ints123.contains12(insertList, insertCount, surr1, surr2))
        if (source.contains(flip ? surr2 : surr1, flip ? surr1 : surr2))
          return false;
    }
    return true;
  }

  public boolean checkDeletedKeys_13(BinaryTableUpdater source, boolean flip) {
    prepare312();

    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 3 * i;
      int surr1 = deleteList[offset];
      int surr3 = deleteList[offset + 1];
      if (!Ints312.contains13(insertList, insertCount, surr1, surr3))
        if (source.contains(flip ? surr3 : surr1, flip ? surr1 : surr3))
          return false;
    }
    return true;
  }

  public boolean checkDeletedKeys_23(BinaryTableUpdater source, boolean flip) {
    prepare231();

    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 3 * i;
      int surr2 = deleteList[offset + 1];
      int surr3 = deleteList[offset + 2];
      if (!Ints231.contains23(insertList, insertCount, surr2, surr3))
        if (source.contains(flip ? surr3 : surr2, flip ? surr2 : surr3))
          return false;
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
    return target.checkDeletedKeys_1(this);
  }

  // tern_rel(_, b, _) -> unary_rel(b);
  public boolean checkForeignKeys_2(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+1]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys_2(this);
  }

  // tern_rel(_, _, c) -> unary_rel(c)
  public boolean checkForeignKeys_3(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+2]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys_3(this);
  }

  // tern_rel(a, b, _) -> binary_rel(a, b)
  public boolean checkForeignKeys_12(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i], insertList[3*i+1]))
        return false;
    return target.checkDeletedKeys_12(this, false);
  }

  // tern_rel(b, a, _) -> binary_rel(a, b)
  public boolean checkForeignKeys_21(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+1], insertList[3*i]))
        return false;
    return target.checkDeletedKeys_12(this, true);
  }

  // tern_rel(a, _, b) -> binary_rel(a, b)
  public boolean checkForeignKeys_13(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i], insertList[3*i+2]))
        return false;
    return target.checkDeletedKeys_13(this, false);
  }

  // tern_rel(b, _, a) -> binary_rel(a, b)
  public boolean checkForeignKeys_31(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+2], insertList[3*i]))
        return false;
    return target.checkDeletedKeys_13(this, true);
  }

  // tern_rel(_, a, b) -> binary_rel(a, b)
  public boolean checkForeignKeys_23(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+1], insertList[3*i+2]))
        return false;
    return target.checkDeletedKeys_23(this, false);
  }

  // tern_rel(_, b, a) -> binary_rel(a, b)
  public boolean checkForeignKeys_32(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+2], insertList[3*i+1]))
        return false;
    return target.checkDeletedKeys_23(this, true);
  }
}
