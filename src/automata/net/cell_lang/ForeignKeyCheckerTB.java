package net.cell_lang;


// tern_rel(a, b, _) -> binary_rel(a, b)

class ForeignKeyCheckerTB {
  TernaryTableUpdater source;
  BinaryTableUpdater target;

  int[] counter = new int[1];
  long[] longBuff = new long[256];


  public ForeignKeyCheckerTB(TernaryTableUpdater source, BinaryTableUpdater target) {
    Miscellanea._assert(source.store1 == target.store1);
    Miscellanea._assert(source.store2 == target.store2);

    this.source = source;
    this.target = target;
  }

  public void check() {
    if (source.insertCount > 0)
      checkSourceInsertions();

    if (target.wasCleared())
      checkTargetClear();
    else if (target.hasPartialDeletes())
      checkTargetDeletes();
  }

  //////////////////////////////////////////////////////////////////////////////

  private void checkSourceInsertions() {
    int count = source.insertCount;
    int[] inserts = source.insertList;
    for (int i=0 ; i < count ; i++) {
      int offset = 3 * i;
      int arg1 = inserts[offset];
      int arg2 = inserts[offset + 1];
      if (!target.contains(arg1, arg2))
        throw insertionForeignKeyViolation(arg1, arg2, inserts[offset+2]);
    }
  }

  private void checkTargetClear() {
    Miscellanea._assert(target.wasCleared());

    TernaryTable.Iter123 it = source.table.getIter();
    while (!it.done()) {
      int arg1 = it.get1();
      int arg2 = it.get2();
      if (source.contains12(arg1, arg2) && !target.contains(arg1, arg2))
        throw deletionForeignKeyViolation(arg1, arg2);
      it.next();
    }
  }

  private void checkTargetDeletes() {
    long[] buffer = target.deletes(longBuff, counter);
    int count = counter[0];
    for (int i=0 ; i < count ; i++) {
      long entry = buffer[i];
      int arg1 = BinaryTableUpdater.arg1(entry);
      int arg2 = BinaryTableUpdater.arg2(entry);
      if (source.contains12(arg1, arg2) && !target.contains(arg1, arg2))
        throw deletionForeignKeyViolation(arg1, arg2);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolation(int surr1, int surr2, int surr3) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    Obj obj3 = source.store3.surrToValue(surr3);
    Obj[] tuple = new Obj[] {obj1, obj2, obj3};
    return ForeignKeyViolationException.ternaryBinary(source.relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException deletionForeignKeyViolation(int surr1, int surr2) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    Obj obj3 = source.store3.surrToValue(source.lookupAny12(surr1, surr2));
    Obj[] fromTuple = new Obj[] {obj1, obj2, obj3};
    Obj[] toTuple = new Obj[] {obj1, obj2};
    return ForeignKeyViolationException.ternaryBinary(source.relvarName, target.relvarName, fromTuple, toTuple);
  }
}
