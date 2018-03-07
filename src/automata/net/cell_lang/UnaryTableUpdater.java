package net.cell_lang;


class UnaryTableUpdater {
  ArrayList<int> deleteList = new ArrayList<int>();
  ArrayList<int> insertList = new ArrayList<int>();

  UnaryTable table;
  ValueStoreUpdater store;

  public UnaryTableUpdater(UnaryTable table, ValueStoreUpdater store) {
    this.table = table;
    this.store = store;
  }

  public void clear() {
    deleteList.clear();
    UnaryTable.Iter it = table.getIter();
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void set(Obj value) {
    Clear();
    Miscellanea._assert(insertList.Count == 0);
    SeqOrSetIter it = value.getSeqOrSetIter();
    while (!it.done()) {
      Obj val = it.get();
      int surr = store.lookupValueEx(val);
      if (surr == -1)
        surr = store.insert(val);
      insertList.add(surr);
      it.next();
    }
  }

  public void delete(long value) {
    if (table.contains(value))
      deleteList.add(value);
  }

  public void insert(long value) {
    insertList.add(value);
  }

  public void apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      int surr = deleteList[i];
      if (table.contains(surr))
        table.delete(surr);
      else
        deleteList[i] = 0xFFFFFFFF;
    }

    var it = insertList.getEnumerator();
    while (it.moveNext()) {
      int surr = it.Current;
      if (!table.contains(surr)) {
        table.insert(surr);
        table.store.addRef(surr);
      }
    }
  }

  public void finish() {
    var it = deleteList.getEnumerator();
    while (it.moveNext()) {
      int surr = it.Current;
      if (surr != 0xFFFFFFFF)
        table.store.release(surr);
    }
  }

  public void reset() {
    deleteList.clear();
    insertList.clear();
  }
}
