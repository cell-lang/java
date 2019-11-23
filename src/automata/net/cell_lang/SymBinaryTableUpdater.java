package net.cell_lang;

import java.util.Arrays;


class SymBinaryTableUpdater {
  static int[] emptyArray = new int[0];

  int deleteCount = 0;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;
  int[] insertList_1_2;

  boolean prepared = false;

  String relvarName;

  SymBinaryTable table;
  ValueStoreUpdater store;


  public SymBinaryTableUpdater(String relvarName, SymBinaryTable table, ValueStoreUpdater store) {
    this.relvarName = relvarName;
    this.table = table;
    this.store = store;
  }

  public void clear() {
    deleteList = table.rawCopy();
    deleteCount = deleteList.length / 2;
  }

  public void delete(int value1, int value2) {
    if (table.contains(value1, value2)) {
      boolean swap = value1 > value2;
      int minorVal = swap ? value2 : value1;
      int majorVal = swap ? value1 : value2;
      deleteList = Array.append2(deleteList, deleteCount++, minorVal, majorVal);
    }
  }

  public void delete(int value) {
    int[] assocs = table.restrict(value);
    for (int i=0 ; i < assocs.length ; i++) {
      int otherVal = assocs[i];
      boolean swap = value > otherVal;
      int minorVal = swap ? otherVal : value;
      int majorVal = swap ? value : otherVal;
      deleteList = Array.append2(deleteList, deleteCount++, minorVal, majorVal);
    }
  }

  public void insert(int value1, int value2) {
    boolean swap = value1 > value2;
    int minorVal = swap ? value2 : value1;
    int majorVal = swap ? value1 : value2;
    insertList = Array.append2(insertList, insertCount++, minorVal, majorVal);
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
        store.addRef(field1);
        store.addRef(field2);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int field1 = deleteList[2 * i];
      if (field1 != 0xFFFFFFFF) {
        int field2 = deleteList[2 * i + 1];
        // Miscellanea._assert(table.store.surrToObjValue(field1) != null);
        // Miscellanea._assert(table.store.surrToObjValue(field2) != null);
        store.release(field1);
        store.release(field2);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 2 * 1024)
      deleteList = emptyArray;
    if (insertList.length > 2 * 1024)
      insertList = emptyArray;

    insertList_1_2 = null;

    prepared = false;
  }

  public void prepare() {
    if (!prepared) {
      Ints12.sort(deleteList, deleteCount);
      Ints12.sort(insertList, insertCount);
      prepared = true;
    }
  }

  private void prepareInsert_1_2() {
    if (insertList_1_2 == null)
      if (insertCount > 0) {
        insertList_1_2 = Arrays.copyOf(insertList, insertCount);
        Ints.sort(insertList_1_2);
      }
      else
        insertList_1_2 = emptyArray;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains(int surr1, int surr2) {
    prepare();

    if (surr1 > surr2) {
      int tmp = surr1;
      surr1 = surr2;
      surr2 = tmp;
    }

    if (Ints12.contains(insertList, insertCount, surr1, surr2))
      return true;

    if (Ints12.contains(deleteList, deleteCount, surr1, surr2))
      return false;

    return table.contains(surr1, surr2);
  }

  public boolean contains(int surr) {
    prepareInsert_1_2();

    if (Ints.contains(insertList_1_2, insertCount, surr))
      return true;

    if (!table.contains(surr))
      return false;

    prepare();

    //## BAD: THIS IS VERY INEFFICIENT IF THERE'S A LOT OF ENTRIES WHOSE FIRST ARGUMENT IS surr
    int[] surrs = table.restrict(surr);
    for (int i=0 ; i < surrs.length ; i++) {
      int surr1 = surr;
      int surr2 = surrs[i];
      if (surr1 > surr2) {
        surr1 = surr2;
        surr2 = surr;
      }
      if (!Ints12.contains(deleteList, deleteCount, surr1, surr2))
        return true;
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public interface DeleteChecker {
    void checkDelete(int surr1, int surr2, SymBinaryTableUpdater target);
  }

  public void checkDeletes(DeleteChecker deleteChecker) {
    // Needs to be called before iterating through deleteList, otherwise a call to
    // checkDelete() -> contains() -> prepare() could reorder it while it's being iterated on
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int offset = 2 * i;
      int surr1 = deleteList[offset];
      int surr2 = deleteList[offset + 1];
      deleteChecker.checkDelete(surr1, surr2, this);
    }
  }
}
