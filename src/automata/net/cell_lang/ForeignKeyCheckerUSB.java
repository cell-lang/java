package net.cell_lang;


// unary_rel(a) -> sym_binary_rel(a | _)

class ForeignKeyCheckerUSB implements SymBinaryTableUpdater.DeleteChecker {
  UnaryTableUpdater source;
  SymBinaryTableUpdater target;

  public ForeignKeyCheckerUSB(UnaryTableUpdater source, SymBinaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    int count = source.insertCount;
    if (count > 0) {
      int[] inserts = source.insertList;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(inserts[i]))
          throw foreignKeyViolation(inserts[i]);
    }

    target.checkDeletes(this);
  }

  public void checkDelete(int arg1, int arg2, SymBinaryTableUpdater target) {
    if (source.contains(arg1) && !target.contains(arg1))
      throw foreignKeyViolation(arg1, arg2);
    if (source.contains(arg2) && !target.contains(arg2))
      throw foreignKeyViolation(arg2, arg1);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr) {
    Obj arg = source.store.surrToValue(surr);
    return ForeignKeyViolationException.unarySymBinary(source.relvarName, target.relvarName, arg);
  }

  private ForeignKeyViolationException foreignKeyViolation(int surr, int otherSurr) {
    Obj arg = source.store.surrToValue(surr);
    Obj otherArg = source.store.surrToValue(otherSurr);
    return ForeignKeyViolationException.unarySymBinary(source.relvarName, target.relvarName, arg, otherArg);
  }
}