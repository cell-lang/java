package net.cell_lang;


// bin_rel(a | b) -> ternary_rel(a | b, _)

class ForeignKeyCheckerSBST implements Sym12TernaryTableUpdater.DeleteChecker {
  SymBinaryTableUpdater source;
  Sym12TernaryTableUpdater target;

  public ForeignKeyCheckerSBST(SymBinaryTableUpdater source, Sym12TernaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store12);
    this.source = source;
    this.target = target;
  }

  public void check() {
    // Checking that every new entry satisfies the foreign key
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains12(inserts[2*i], inserts[2*i+1]))
          throw foreignKeyViolation(inserts[2*i], inserts[2*i+1]);
    }

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletes(this);
  }

  public void checkDelete(int surr1, int surr2, int surr3, Sym12TernaryTableUpdater target) {
    if (source.contains(surr1, surr2) && !target.contains12(surr1, surr2))
      throw foreignKeyViolation(surr1, surr2, surr3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr1, int surr2) {
    Obj arg1 = source.store.surrToValue(surr1);
    Obj arg2 = source.store.surrToValue(surr2);
    return ForeignKeyViolationException.symBinarySymTernary(source.relvarName, target.relvarName, arg1, arg2);
  }

  private ForeignKeyViolationException foreignKeyViolation(int surr1, int surr2, int surr3) {
    Obj arg1 = source.store.surrToValue(surr1);
    Obj arg2 = source.store.surrToValue(surr2);
    Obj arg3 = target.store3.surrToValue(surr3);
    return ForeignKeyViolationException.symBinarySymTernary(source.relvarName, target.relvarName, arg1, arg2, arg3);
  }
}