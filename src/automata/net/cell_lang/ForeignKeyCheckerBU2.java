package net.cell_lang;


// bin_rel(_, b) -> unary_rel(b);

class ForeignKeyCheckerBU2 implements UnaryTableUpdater.DeleteChecker {
  BinaryTableUpdater source;
  UnaryTableUpdater target;

  private int[] counter = new int[1];
  private long[] buffer = new long[256];


  public ForeignKeyCheckerBU2(BinaryTableUpdater source, UnaryTableUpdater target) {
    this.source = source;
    this.target = target;
  }

  public void check() {
    if (source.hasInsertions()) {
      long[] buffer = source.insertions(this.buffer, counter);
      int count = counter[0];
      for (int i=0 ; i < count ; i++) {
        long entry = buffer[i];
        int arg2 = BinaryTableUpdater.arg2(entry);
        if (!target.contains(arg2))
          throw insertionForeignKeyViolation(BinaryTableUpdater.arg1(entry), arg2);
      }
    }

    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int arg2) {
    // arg2 is guaranteed to have been deleted and not reinserted
    if (source.contains2(arg2))
      throw deletionForeignKeyViolation(arg2);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolation(int surr1, int surr2) {
    Obj[] tuple = new Obj[] {source.store1.surrToValue(surr1), source.store2.surrToValue(surr2)};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 2, target.relvarName, tuple);
  }

  private ForeignKeyViolationException deletionForeignKeyViolation(int surr2) {
    int surr1 = source.table.restrict2(surr2)[0]; //## BAD: VERY INEFFICIENT
    Obj obj2 = source.store2.surrToValue(surr2);
    Obj[] tuple = new Obj[] {source.store1.surrToValue(surr1), obj2};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 1, target.relvarName, tuple, obj2);
  }
}
