package net.cell_lang;


// unary_rel(c) -> tern_rel(_ | _, c);

class ForeignKeyCheckerUST3 implements Sym12TernaryTableUpdater.DeleteChecker {
  UnaryTableUpdater source;
  Sym12TernaryTableUpdater target;

  public ForeignKeyCheckerUST3(UnaryTableUpdater source, Sym12TernaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store3);
    this.source = source;
    this.target = target;
  }

  public void check() {
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains3(inserts[i]))
          throw foreignKeyViolation(inserts[i]);
    }

    target.checkDeletes(this);
  }

  public void checkDelete(int arg1, int arg2, int arg3, Sym12TernaryTableUpdater updater) {
    if (source.contains(arg3) && !target.contains3(arg3))
      throw foreignKeyViolation(arg1, arg2, arg3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr) {
    Obj arg = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(source.relvarName, 3, target.relvarName, arg);
  }

  private ForeignKeyViolationException foreignKeyViolation(int surr1, int surr2, int surr3) {
    Obj arg1 = target.store12.surrToValue(surr1);
    Obj arg2 = target.store12.surrToValue(surr2);
    Obj arg3 = source.store.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(source.relvarName, 3, target.relvarName, tuple);
  }
}