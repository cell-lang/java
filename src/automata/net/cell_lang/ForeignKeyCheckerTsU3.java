package net.cell_lang;


// tern_rel(_, _, c) -> unary_rel(c);

class ForeignKeyCheckerTsU3 implements UnaryTableUpdater.DeleteChecker {
  SlaveTernaryTableUpdater source;
  UnaryTableUpdater target;

  public ForeignKeyCheckerTsU3(SlaveTernaryTableUpdater source, UnaryTableUpdater target) {
    Miscellanea._assert(source.store3 == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    // Checking that every new entry satisfies the foreign key
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(inserts[3*i+2]))
          throw foreignKeyViolation(inserts[3*i], inserts[3*i+1], inserts[3*i+2]);
    }

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int surr3) {
    if (source.contains3(surr3))
      throw foreignKeyViolation(surr3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int arg1Surr, int arg2Surr, int arg3Surr) {
    Obj[] tuple = new Obj[] {
      source.store1.surrToValue(arg1Surr),
      source.store2.surrToValue(arg2Surr),
      source.store3.surrToValue(arg3Surr)
    };
    return ForeignKeyViolationException.ternaryUnary(source.relvarName, 3, target.relvarName, tuple);
  }

  private ForeignKeyViolationException foreignKeyViolation(int delSurr) {
    SlaveTernaryTable.Iter3 it = source.table.getIter3(delSurr);
    Obj arg1 = source.store1.surrToValue(it.get1());
    Obj arg2 = source.store2.surrToValue(it.get2());
    Obj arg3 = source.store3.surrToValue(delSurr);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.ternaryUnary(source.relvarName, 3, target.relvarName, tuple, arg3);
  }
}