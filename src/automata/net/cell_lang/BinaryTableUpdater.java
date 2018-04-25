package net.cell_lang;


class BinaryTableUpdater {
  static int[] emptyArray = new int[0];

  int deleteCount = 0;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  public BinaryTableUpdater(BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  public void clear() {
    deleteList = table.rawCopy();
    deleteCount = deleteList.length / 2;
  }

  public void set(Obj value, boolean flipped) {
    clear();
    Miscellanea._assert(insertCount == 0);
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
      insertList = Miscellanea.array2Append(insertList, insertCount++, surr1, surr2);
      it.next();
    }
  }

  public void delete(int value1, int value2) {
    if (table.contains(value1, value2))
      deleteList = Miscellanea.array2Append(deleteList, deleteCount++, value1, value2);
  }

  public void delete1(int value) {
    int[] assocs = table.lookupByCol1((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList = Miscellanea.array2Append(deleteList, deleteCount++, value, assocs[i]);
  }

  public void delete2(int value) {
    int[] assocs = table.lookupByCol2((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList = Miscellanea.array2Append(deleteList, deleteCount++, assocs[i], value);
  }

  public void insert(int value1, int value2) {
    insertList = Miscellanea.array2Append(insertList, insertCount++, value1, value2);
  }

  public boolean checkUpdates_1() {
    if (insertCount == 0)
      return true;

    Ints12.sort(deleteList, deleteCount);
    Ints12.sort(insertList, insertCount);

    int prev1 = -1;
    int prev2 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int curr1 = insertList[2 * i];
      int curr2 = insertList[2 * i + 1];

      if (curr1 == prev1 & curr2 != prev2)
        return false;

      if (!Ints12.contains1(deleteList, deleteCount, curr1) && table.contains1(curr1))
        return false;

      prev1 = curr1;
      prev2 = curr2;
    }

    return true;
  }

  public boolean checkUpdates_1_2() {
    if (insertCount == 0)
      return true;

    if (!checkUpdates_1())
      return false;

    Ints21.sort(deleteList, deleteCount);
    Ints21.sort(insertList, insertCount);

    int prev1 = -1;
    int prev2 = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int curr1 = insertList[2 * i];
      int curr2 = insertList[2 * i + 1];

      if (curr2 == prev2 & curr1 != prev1)
        return false;


      if (!Ints21.contains2(deleteList, deleteCount, curr2) && table.contains2(curr2))
        return false;

      prev1 = curr1;
      prev2 = curr2;
    }

    return true;
  }

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[2 * i];
      int field2 = deleteList[2 * i + 1];
      if (table.contains(field1, field2))
        table.delete(field1, field2);
      else
        deleteList[2 * i] = 0xFFFFFFFF;
    }

    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[2 * i];
      int field2 = insertList[2 * i + 1];
      if (!table.contains(field1, field2)) {
        table.insert(field1, field2);
        table.store1.addRef(field1);
        table.store2.addRef(field2);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[2 * i];
      if (field1 != 0xFFFFFFFF) {
        int field2 = deleteList[2 * i + 1];
        Miscellanea._assert(table.store1.lookupSurrogate(field1) != null);
        Miscellanea._assert(table.store2.lookupSurrogate(field2) != null);
        table.store1.release(field1);
        table.store2.release(field2);
      }
    }
  }

  public void reset() {
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 2 * 1024)
      deleteList = emptyArray;
    if (insertList.length > 2 * 1024)
      insertList = emptyArray;
  }

  public void dump(boolean flipped) {
    System.out.print("deleteList =");
    for (int i=0 ; i < deleteCount ; i++)
      System.out.printf(" (%d, %d)", deleteList[2 * i], deleteList[2 * i + 1]);
    System.out.println();

    System.out.print("insertList =");
    for (int i=0 ; i < insertCount ; i++)
      System.out.printf(" (%d, %d)", insertList[2 * i], insertList[2 * i + 1]);
    System.out.println("\n");

    System.out.print("deleteList =");
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[2 * i];
      int field2 = deleteList[2 * i + 1];
      Obj obj1 = store1.lookupSurrogateEx(field1);
      Obj obj2 = store2.lookupSurrogateEx(field2);
      System.out.printf(" (%s, %s)", obj1.toString(), obj2.toString());
    }
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertCount ; i++) {
      int field1 = insertList[2 * i];
      int field2 = insertList[2 * i + 1];
      Obj obj1 = store1.lookupSurrogateEx(field1);
      Obj obj2 = store2.lookupSurrogateEx(field2);
      System.out.printf(" (%s, %s)",
        obj1 != null ? obj1.toString() : "null",
        obj2 != null ? obj2.toString() : "null"
      );
    }

    System.out.printf("\n\n%s\n\n", table.copy(flipped).toString());

    store1.dump();
    store2.dump();
  }
}
