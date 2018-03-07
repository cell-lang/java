package net.cell_lang;


class UnaryTableUpdater {
  List<uint> deleteList = new List<uint>();
  List<uint> insertList = new List<uint>();

  UnaryTable table;
  ValueStoreUpdater store;

  public UnaryTableUpdater(UnaryTable table, ValueStoreUpdater store) {
    this.table = table;
    this.store = store;
  }

  public void Clear() {
    deleteList.Clear();
    UnaryTable.Iter it = table.GetIter();
    while (!it.Done()) {
      deleteList.Add(it.Get());
      it.Next();
    }
  }

  public void Set(Obj value) {
    Clear();
    Miscellanea.Assert(insertList.Count == 0);
    SeqOrSetIter it = value.GetSeqOrSetIter();
    while (!it.Done()) {
      Obj val = it.Get();
      int surr = store.LookupValueEx(val);
      if (surr == -1)
        surr = store.Insert(val);
      insertList.Add((uint) surr);
      it.Next();
    }
  }

  public void Delete(long value) {
    if (table.Contains((uint) value))
      deleteList.Add((uint) value);
  }

  public void Insert(long value) {
    insertList.Add((uint) value);
  }

  public void Apply() {
    for (int i=0 ; i < deleteList.Count ; i++) {
      uint surr = deleteList[i];
      if (table.Contains(surr))
        table.Delete(surr);
      else
        deleteList[i] = 0xFFFFFFFF;
    }

    var it = insertList.GetEnumerator();
    while (it.MoveNext()) {
      uint surr = it.Current;
      if (!table.Contains(surr)) {
        table.Insert(surr);
        table.store.AddRef(surr);
      }
    }
  }

  public void Finish() {
    var it = deleteList.GetEnumerator();
    while (it.MoveNext()) {
      uint surr = it.Current;
      if (surr != 0xFFFFFFFF)
        table.store.Release(surr);
    }
  }

  public void Reset() {
    deleteList.Clear();
    insertList.Clear();
  }
}
