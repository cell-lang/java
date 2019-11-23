package net.cell_lang;


// tern_rel(a | b, _) -> unary_rel(a), unary_rel(b);

class ForeignKeyCheckerST12U implements UnaryTableUpdater.DeleteChecker {
  Sym12TernaryTableUpdater source;
  UnaryTableUpdater target;

  public ForeignKeyCheckerST12U(Sym12TernaryTableUpdater source, UnaryTableUpdater target) {
    Miscellanea._assert(source.store12 == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    // Checking that every new entry satisfies the foreign key
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(inserts[3*i]) | !target.contains(inserts[3*i+1]))
          throw foreignKeyViolation(inserts[3*i], inserts[3*i+1], inserts[3*i+2]);
    }

    // Checking that no entries were invalidates by a deletion on the target table
    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int surr12) {
    if (source.contains_1_2(surr12))
      throw foreignKeyViolation(surr12);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int arg1Surr, int arg2Surr, int arg3Surr) {
    Obj[] tuple = new Obj[] {
      source.store12.surrToValue(arg1Surr),
      source.store12.surrToValue(arg2Surr),
      source.store3.surrToValue(arg3Surr)
    };
    return ForeignKeyViolationException.symTernary12Unary(source.relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException foreignKeyViolation(int arg12Surr) {
    Sym12TernaryTable.Iter it = source.table.getIter_1_2(arg12Surr);
    Obj arg1 = source.store12.surrToValue(arg12Surr);
    Obj arg2 = source.store12.surrToValue(it.get1());
    Obj arg3 = source.store3.surrToValue(it.get2());
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.symTernary12Unary(source.relvarName, target.relvarName, tuple, arg1);
  }
}
