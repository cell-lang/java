package net.cell_lang;


// unary_rel(x) -> binary_rel(x, _);

final class ForeignKeyCheckerUB1 {
  private UnaryTableUpdater source;
  private BinaryTableUpdater target;

  private int[] counter = new int[1];
  private int[] intBuff = new int[256];


  public ForeignKeyCheckerUB1(UnaryTableUpdater source, BinaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store1);

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
      if (!target.contains1(elt))
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
      if (source.contains(elt) && !target.contains1(elt))
          throw deletionForeignKeyViolationException(elt, target.anyDeletedArg2(elt));
      it.next();
    }
  }

  private void checkTargetDeletes() {
    int[] buffer = target.deletes1(intBuff, counter);
    int count = counter[0];
    for (int i=0 ; i < count ; i++) {
      int elt = buffer[i];
      if (source.contains(elt) && !target.contains1(elt))
        throw deletionForeignKeyViolationException(elt, target.anyDeletedArg2(elt));
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolationException(int surr) {
    Obj obj = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unaryBinary(source.relvarName, 1, target.relvarName, obj);
  }

  private ForeignKeyViolationException deletionForeignKeyViolationException(int surr1, int surr2) {
    Obj obj1 = source.store.surrToValue(surr1);
    Obj obj2 = target.store2.surrToValue(surr2);
    Obj[] tuple = new Obj[] {obj1, obj2};
    return ForeignKeyViolationException.unaryBinary(source.relvarName, 1, target.relvarName, tuple);
  }
}
