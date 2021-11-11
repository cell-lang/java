package net.cell_lang;


class ForeignKeyCheckerSFCB {
  SlaveFloatColumnUpdater source;
  MasterBinaryTableUpdater target;

  int[] counter = new int[1];
  long[] longBuff = new long[256];


  public ForeignKeyCheckerSFCB(SlaveFloatColumnUpdater source, MasterBinaryTableUpdater target) {
    Miscellanea._assert(source.store1 == target.store1);
    Miscellanea._assert(source.store2 == target.store2);

    this.source = source;
    this.target = target;
  }

  public void check() {
    if (source.insertCount > 0)
      checkSourceInsertions();

    if (source.updateCount > 0)
      checkSourceUpdates();

    if (target.wasCleared())
      checkTargetClear();
    else if (target.hasPartialDeletes())
      checkTargetDeletes();
  }

  //////////////////////////////////////////////////////////////////////////////

  private void checkSourceInsertions() {
    int count = source.insertCount;
    long[] insertSurrs = source.insertSurrs;
    for (int i=0 ; i < count ; i++) {
      long packedArgs = insertSurrs[i];
      int arg1 = SlaveFloatColumnUpdater.arg1(packedArgs);
      int arg2 = SlaveFloatColumnUpdater.arg2(packedArgs);
      if (!target.contains(arg1, arg2))
        throw insertionForeignKeyViolation(arg1, arg2, source.insertValues[i]);
    }
  }

  private void checkSourceUpdates() {
    int count = source.updateCount;
    long[] updateSurrs = source.updateSurrs;
    for (int i=0 ; i < count ; i++) {
      long packedArgs = updateSurrs[i];
      int arg1 = SlaveFloatColumnUpdater.arg1(packedArgs);
      int arg2 = SlaveFloatColumnUpdater.arg2(packedArgs);
      if (!target.contains(arg1, arg2))
        throw insertionForeignKeyViolation(arg1, arg2, source.updateValues[i]);
    }
  }

  private void checkTargetClear() {
    Miscellanea._assert(target.wasCleared());

    FloatColumn.Iter it = source.column.getIter();
    while (!it.done()) {
      int assoc_surr = it.getIdx();
      if (!source.wasDeleted(assoc_surr)) {
        int arg1 = source.master.arg1(assoc_surr);
        int arg2 = source.master.arg2(assoc_surr);
        if (!target.contains(arg1, arg2))
          throw deletionForeignKeyViolation(arg1, arg2);
      }
      it.next();
    }
  }

  private void checkTargetDeletes() {
    long[] buffer = target.deletes(longBuff, counter);
    int count = counter[0];
    for (int i=0 ; i < count ; i++) {
      long entry = buffer[i];
      int arg1 = MasterBinaryTableUpdater.arg1(entry);
      int arg2 = MasterBinaryTableUpdater.arg2(entry);
      if (source.contains12(arg1, arg2) && !target.contains(arg1, arg2))
        throw deletionForeignKeyViolation(arg1, arg2);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolation(int surr1, int surr2, double value3) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    Obj[] tuple = new Obj[] {obj1, obj2, new FloatObj(value3)};
    return ForeignKeyViolationException.ternaryBinary(source.relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException deletionForeignKeyViolation(int surr1, int surr2) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    int assoc_surr = source.master.surrogate(surr1, surr2);
    Obj obj3 = new FloatObj(source.column.lookup(assoc_surr));
    Obj[] fromTuple = new Obj[] {obj1, obj2, obj3};
    Obj[] toTuple = new Obj[] {obj1, obj2};
    return ForeignKeyViolationException.ternaryBinary(source.relvarName, target.relvarName, fromTuple, toTuple);
  }
}