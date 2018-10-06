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
  }
}
