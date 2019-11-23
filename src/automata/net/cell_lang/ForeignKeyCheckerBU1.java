package net.cell_lang;


// bin_rel(a, _) -> unary_rel(a);

class ForeignKeyCheckerBU1 implements UnaryTableUpdater.DeleteChecker {
  BinaryTableUpdater source;
  UnaryTableUpdater target;

  private int[] counter = new int[1];
  private long[] buffer = new long[256];


  public ForeignKeyCheckerBU1(BinaryTableUpdater source, UnaryTableUpdater target) {
    Miscellanea._assert(source.store1 == target.store);

    this.source = source;
    this.target = target;
  }

  public void check() {
    if (source.hasInsertions()) {
      long[] buffer = source.insertions(this.buffer, counter);
      int count = counter[0];
      for (int i=0 ; i < count ; i++) {
        long entry = buffer[i];
        int arg1 = BinaryTableUpdater.arg1(entry);
        if (!target.contains(arg1))
          throw insertionForeignKeyViolation(arg1, BinaryTableUpdater.arg2(entry));
      }
    }

    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int arg1) {
    // arg1 is guaranteed to have been deleted and not reinserted
    if (source.contains1(arg1))
      throw deletionForeignKeyViolation(arg1);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolation(int surr1, int surr2) {
    Obj[] tuple = new Obj[] {source.store1.surrToValue(surr1), source.store2.surrToValue(surr2)};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 1, target.relvarName, tuple);
  }

  private ForeignKeyViolationException deletionForeignKeyViolation(int surr1) {
    int surr2 = source.table.restrict1(surr1)[0]; //## BAD: VERY INEFFICIENT
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj[] tuple = new Obj[] {obj1, source.store2.surrToValue(surr2)};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 1, target.relvarName, tuple, obj1);
  }
}
