package net.cell_lang;


// bin_rel(a, b) -> ternary_rel(a, b, _)

class ForeignKeyCheckerBT implements TernaryTableUpdater.DeleteChecker {
  BinaryTableUpdater source;
  TernaryTableUpdater target;

  private int[] counter = new int[0];
  private long[] buffer = new long[256];


  public ForeignKeyCheckerBT(BinaryTableUpdater source, TernaryTableUpdater target) {
    Miscellanea._assert(source.store1 == target.store1 & source.store2 == target.store2);

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
        int arg2 = BinaryTableUpdater.arg2(entry);
        if (!target.contains12(arg1, arg2))
          throw insertionForeignKeyViolation(arg1, arg2);
      }
    }

    target.checkDeletes123(this);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void mightHaveBeenDeleted(int arg1, int arg2, int arg3) {
    if (source.contains(arg1, arg2) && !target.contains12(arg1, arg2))
      throw deletionForeignKeyViolation(arg1, arg2, arg3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException insertionForeignKeyViolation(int surr1, int surr2) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    return ForeignKeyViolationException.binaryTernary(source.relvarName, target.relvarName, obj1, obj2);
  }

  private ForeignKeyViolationException deletionForeignKeyViolation(int surr1, int surr2, int surr3) {
    Obj obj1 = source.store1.surrToValue(surr1);
    Obj obj2 = source.store2.surrToValue(surr2);
    Obj obj3 = target.store3.surrToValue(surr3);
    return ForeignKeyViolationException.binaryTernary(source.relvarName, target.relvarName, obj1, obj2, obj3);
  }
}
