package net.cell_lang;


class UnaryTableUpdater {
  static int[] emptyArray = new int[0];

  boolean clear = false;

  int deleteCount = 0;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

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
    SeqOrSetIter it = value.getSeqOrSetIter();
    for (int i=0 ; i < size ; i++) {
      Miscellanea._assert(!it.done());
      Obj val = it.get();
      int surr = store.lookupValueEx(val);
      if (surr == -1)
        surr = store.insert(val);
      insertList[i] = surr;
      it.next();
    }
    Miscellanea._assert(it.done());
  }

  public void delete(long value) {
    if (table.contains((int) value))
      deleteList = Miscellanea.arrayAppend(deleteList, deleteCount++, (int) value);
  }

  public void insert(long value) {
    insertList = Miscellanea.arrayAppend(insertList, insertCount++, (int) value);
  }

  public void apply() {
    //## HUGE BUG HERE. IF THE TABLE IS CLEARED,
    //## THE REFERENCE COUNT IN THE VALUE STORE IS NOT DECREMENTED
    if (clear) {
      table.clear();
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
    for (int i=0 ; i < deleteCount ; i++) {
      int surr = deleteList[i];
      if (surr != 0xFFFFFFFF)
        table.store.release(surr);
    }
  }

  public void reset() {
    clear = false;
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 1024)
      deleteList = emptyArray;
    if (insertList.length > 1024)
      insertList = emptyArray;
  }
}
