package net.cell_lang;


// unary_rel(a) -> tern_rel(a | _, _)

class ForeignKeyCheckerUST12 implements Sym12TernaryTableUpdater.DeleteChecker {
  UnaryTableUpdater source;
  Sym12TernaryTableUpdater target;

  public ForeignKeyCheckerUST12(UnaryTableUpdater source, Sym12TernaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store12);
    this.source = source;
    this.target = target;
  }

  public void check() {
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains_1_2(inserts[i]))
          throw foreignKeyViolation(inserts[i]);
    }
    target.checkDeletes(this);
  }

  public void checkDelete(int arg1, int arg2, int arg3, Sym12TernaryTableUpdater updater) {
    if (source.contains(arg1) && !target.contains_1_2(arg1))
      throw foreignKeyViolation(arg1, arg2, arg3);
    if (source.contains(arg2) && !target.contains_1_2(arg2))
      throw foreignKeyViolation(arg2, arg1, arg3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr) {
    Obj arg = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unarySym12Ternary(source.relvarName, target.relvarName, arg);
  }

  private ForeignKeyViolationException foreignKeyViolation(int delSurr12, int otherSurr12, int surr3) {
    Obj delArg12 = source.store.surrToValue(delSurr12);
    Obj otherArg12 = source.store.surrToValue(otherSurr12);
    Obj arg3 = target.store3.surrToValue(surr3);
    return ForeignKeyViolationException.unarySym12Ternary(source.relvarName, target.relvarName, delArg12, otherArg12, arg3);
  }
}