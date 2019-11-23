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

  String relvarName;
  UnaryTable table;
  ValueStoreUpdater store;


  public UnaryTableUpdater(String relvarName, UnaryTable table, ValueStoreUpdater store) {
    this.relvarName = relvarName;
    this.table = table;
    this.store = store;
  }

  public void clear() {
    clear = true;
    deleteCount = 0;
  }

  public void delete(long value) {
    if (!clear || table.contains((int) value))
      deleteList = Array.append(deleteList, deleteCount++, (int) value);
  }

  public void insert(long value) {
    insertList = Array.append(insertList, insertCount++, (int) value);
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
        store.addRef(surr);
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
            store.release(base + j);
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (surr != 0xFFFFFFFF)
          store.release(surr);
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

    if (Arrays.binarySearch(insertList, 0, insertCount, surr) >= 0)
      return true;

    if (clear || Arrays.binarySearch(deleteList, 0, deleteCount, surr) >= 0)
      return false;

    return table.contains(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  public interface DeleteChecker {
    // arg1 is guaranteed to have been deleted and not reinserted
    void wasDeleted(int surr);
  }

  public void checkDeletedKeys(DeleteChecker deleteChecker) {
    prepare();

    if (clear) {
      UnaryTable.Iter it = table.getIter();
      while (!it.done()) {
        int surr = it.get();
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deleteChecker.wasDeleted(surr);
        it.next();
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deleteChecker.wasDeleted(surr);
      }
    }
  }
}
