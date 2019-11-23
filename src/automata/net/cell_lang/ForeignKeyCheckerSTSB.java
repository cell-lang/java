package net.cell_lang;


// tern_rel(a | b, _) -> bin_rel(a | b)

class ForeignKeyCheckerSTSB implements SymBinaryTableUpdater.DeleteChecker {
  Sym12TernaryTableUpdater source;
  SymBinaryTableUpdater target;

  public ForeignKeyCheckerSTSB(Sym12TernaryTableUpdater source, SymBinaryTableUpdater target) {
    Miscellanea._assert(source.store12 == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(inserts[3*i], inserts[3*i+1]))
          throw foreignKeyViolation(inserts[3*i], inserts[3*i+1], inserts[3*i+2]);
    }

    target.checkDeletes(this);
  }

  public void checkDelete(int surr1, int surr2, SymBinaryTableUpdater target) {
    if (source.contains12(surr1, surr2) && !target.contains(surr1, surr2))
      throw foreignKeyViolation(surr1, surr2, source.lookupAny12(surr1, surr2));
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr1, int surr2, int surr3) {
    Obj arg1 = source.store12.surrToValue(surr1);
    Obj arg2 = source.store12.surrToValue(surr2);
    Obj arg3 = source.store3.surrToValue(surr3);
    return ForeignKeyViolationException.symTernarySymBinary(source.relvarName, target.relvarName, arg1, arg2, arg3);
  }
}