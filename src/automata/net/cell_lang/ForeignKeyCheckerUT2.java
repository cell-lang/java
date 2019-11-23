package net.cell_lang;


// unary_rel(x) -> ternary_rel(, x, _)

class ForeignKeyCheckerUT2 implements TernaryTableUpdater.DeleteChecker {
  UnaryTableUpdater source;
  TernaryTableUpdater target;

  public ForeignKeyCheckerUT2(UnaryTableUpdater source, TernaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store2);
    this.source = source;
    this.target = target;
  }

  public void check() {
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains2(inserts[i]))
          throw toTernaryForeignKeyViolation2(inserts[i]);
    }

    target.checkDeletes231(this);
  }

  public void mightHaveBeenDeleted(int arg1, int arg2, int arg3) {
    if (source.contains(arg2) && !target.contains2(arg2))
      throw toTernaryForeingKeyViolation2(arg1, arg2, arg3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException toTernaryForeignKeyViolation2(int surr) {
    Obj arg = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(source.relvarName, 2, target.relvarName, arg);
  }

  private ForeignKeyViolationException toTernaryForeingKeyViolation2(int surr1, int surr2, int surr3) {
    Obj arg1 = target.store1.surrToValue(surr1);
    Obj arg2 = target.store2.surrToValue(surr2);
    Obj arg3 = target.store3.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(source.relvarName, 2, target.relvarName, tuple);
  }
}