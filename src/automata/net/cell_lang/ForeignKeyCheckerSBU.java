package net.cell_lang;


// bin_rel(a | b) -> unary_rel(a), unary_rel(b);

class ForeignKeyCheckerSBU implements UnaryTableUpdater.DeleteChecker {
  SymBinaryTableUpdater source;
  UnaryTableUpdater target;

  public ForeignKeyCheckerSBU(SymBinaryTableUpdater source, UnaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    // Checking that every new entry satisfies the foreign key
    int count = source.insertCount;
    if (count == 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < 2 * count ; i++)
        if (!target.contains(inserts[i])) {
          int surr1 = i % 2 == 0 ? inserts[i] : inserts[i-1];
          int surr2 = i % 2 == 0 ? inserts[i+1] : inserts[i];
          throw foreignKeyViolation(surr1, surr2);
        }
    }

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int surr) {
    if (source.contains(surr))
      throw foreignKeyViolation(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int arg1Surr, int arg2Surr) {
    Obj[] tuple = new Obj[] {source.store.surrToValue(arg1Surr), source.store.surrToValue(arg2Surr)};
    return ForeignKeyViolationException.symBinaryUnary(source.relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException foreignKeyViolation(int delSurr) {
    int otherSurr = source.table.restrict(delSurr)[0];
    Obj arg1 = source.store.surrToValue(delSurr);
    Obj[] tuple1 = new Obj[] {arg1, source.store.surrToValue(otherSurr)};
    return ForeignKeyViolationException.symBinaryUnary(source.relvarName, target.relvarName, tuple1, arg1);
  }
}
