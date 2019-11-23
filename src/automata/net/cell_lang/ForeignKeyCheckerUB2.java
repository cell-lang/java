package net.cell_lang;


// unary_rel(x) -> binary_rel(_, x);

final class ForeignKeyCheckerUB2 {
  private UnaryTableUpdater source;
  private BinaryTableUpdater target;

  private int[] counter = new int[1];
  private int[] intBuff = new int[256];


  public ForeignKeyCheckerUB2(UnaryTableUpdater source, BinaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store2);

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
    //## METHODS OF source MUST NOT BE CALLED WHILE WE'RE ITERATING OVER source.insertList
    for (int i=0 ; i < source.insertCount ; i++) {
      int elt = source.insertList[i];
      if (!target.contains2(elt))
        throw insertionForeignKeyViolationException(elt);
    }
  }

  private void checkTargetClear() {
    Miscellanea._assert(target.wasCleared());

    if (source.clear)
      return;

    UnaryTable.Iter it = source.table.getIter();
    while (!it.done()) {
      int elt = it.get();
      if (source.contains(elt) && !target.contains2(elt))
          throw deletionForeignKeyViolationException(target.anyDeletedArg1(elt), elt);
      it.next();
    }
  }

  private void checkTargetDeletes() {
    int[] buffer = target.deletes2(intBuff, counter);
    int count = counter[0];
    for (int i=0 ; i < count ; i++) {
      int elt = buffer[i];
      if (source.contains(elt) && !target.contains2(elt))
        throw deletionForeignKeyViolationException(target.anyDeletedArg1(elt), elt);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolationException(int surr) {
    Obj obj = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unaryBinary(source.relvarName, 2, target.relvarName, obj);
  }

  private ForeignKeyViolationException deletionForeignKeyViolationException(int surr1, int surr2) {
    Obj obj1 = target.store1.surrToValue(surr1);
    Obj obj2 = source.store.surrToValue(surr2);
    Obj[] tuple = new Obj[] {obj1, obj2};
    return ForeignKeyViolationException.unaryBinary(source.relvarName, 2, target.relvarName, tuple);
  }
}
