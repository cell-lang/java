package net.cell_lang;

import java.util.Arrays;


class UnaryTableUpdater {
  static int[] emptyArray = new int[0];

  boolean clear = false;
  long[] bitmapCopy = null;

  int deleteCount = 0;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

  boolean prepared = false;

  UnaryTable table;
  ValueStoreUpdater store;


  public UnaryTableUpdater(UnaryTable table, ValueStoreUpdater store) {
    this.table = table;
    this.store = store;
  }

  public void clear() {
    clear = true;
  }

  public void set(Obj value) {
    Miscellanea._assert(deleteCount == 0 & insertCount == 0);
    clear();
    int size = value.getSize();
    insertCount = size;
    insertList = new int[size];
    Obj[] elts = value.getArray((Obj[]) null);
    Miscellanea._assert(elts.length == size);
    for (int i=0 ; i < size ; i++) {
      Obj val = elts[i];
      int surr = store.lookupValueEx(val);
      if (surr == -1)
        surr = store.insert(val);
      insertList[i] = surr;
    }
  }

  public void delete(long value) {
    if (table.contains((int) value))
      deleteList = Miscellanea.arrayAppend(deleteList, deleteCount++, (int) value);
  }

  public void insert(long value) {
    insertList = Miscellanea.arrayAppend(insertList, insertCount++, (int) value);
  }

  public void apply() {
    if (clear) {
      int max = 0;
      for (int i=0 ; i < insertCount ; i++) {
        int surr = insertList[i];
        if (surr > max)
          max = surr;
      }
      bitmapCopy = table.clear(max + 1);
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (table.contains(surr))
          table.delete(surr);
        else
          deleteList[i] = 0xFFFFFFFF;
      }
    }

    for (int i=0 ; i < insertCount ; i++) {
      int surr = insertList[i];
      if (!table.contains(surr)) {
        table.insert(surr);
        table.store.addRef(surr);
      }
    }
  }

  public void finish() {
    if (clear) {
      int len = bitmapCopy.length;
      for (int i=0 ; i < len ; i++) {
        long mask = bitmapCopy[i];
        int base = 64 * i;
        for (int j=0 ; j < 64 ; j++)
          if (((mask >>> j) & 1) != 0)
            table.store.release(base + j);
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (surr != 0xFFFFFFFF)
          table.store.release(surr);
      }
    }
  }

  public void reset() {
    clear = false;
    bitmapCopy = null;

    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 1024)
      deleteList = emptyArray;
    if (insertList.length > 1024)
      insertList = emptyArray;

    prepared = false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void prepare() {
    if (!prepared) {
      prepared = true;
      Arrays.sort(deleteList, 0, deleteCount);
      Arrays.sort(insertList, 0, insertCount);
    }
  }

  public boolean contains(int surr) {
    prepare();

    if (Arrays.binarySearch(insertList, 0, insertCount, surr) != -1)
      return true;

    if (Arrays.binarySearch(deleteList, 0, deleteCount, surr) != -1)
      return false;

    return table.contains(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean checkDeletedKeys(UnaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains(surr))
          return false;
    }

    return true;
  }

  public boolean checkDeletedKeys_1(BinaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains1(surr))
          return false;
    }

    return true;
  }

  public boolean checkDeletedKeys_2(BinaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains2(surr))
          return false;
    }

    return true;
  }

  public boolean checkDeletedKeys_1(TernaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains1(surr))
          return false;
    }

    return true;
  }

  public boolean checkDeletedKeys_2(TernaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains2(surr))
          return false;
    }

    return true;
  }

  public boolean checkDeletedKeys_3(TernaryTableUpdater source) {
    prepare();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (Arrays.binarySearch(insertList, 0, insertCount, surr) == -1)
        if (source.contains3(surr))
          return false;
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  // unary_rel_1(x) -> unary_rel_2(x);
  public boolean checkForeignKeys(UnaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this);
  }

  // unary_rel(x) -> binary_rel(x, _);
  public boolean checkForeignKeys_1(BinaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains1(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys_1(this);
  }

  // unary_rel(x) -> binary_rel(_, x);
  public boolean checkForeignKeys_2(BinaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys_2(this);
  }

  public boolean checkForeignKeys_1(TernaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains1(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys_1(this);
  }

  public boolean checkForeignKeys_2(TernaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys_2(this);
  }

  public boolean checkForeignKeys_3(TernaryTableUpdater target) {
    // Checking that every new elements satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains3(insertList[i]))
        return false;

    // Checking that no elements were invalidated by a deletion on the target table
    return target.checkDeletedKeys_3(this);
  }
}
