package net.cell_lang;


// unary_rel_1(x) -> unary_rel_2(x);

class ForeignKeyCheckerUU implements UnaryTableUpdater.DeleteChecker {
  UnaryTableUpdater source;
  UnaryTableUpdater target;

  public ForeignKeyCheckerUU(UnaryTableUpdater source, UnaryTableUpdater target) {
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

    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int surr) {
    if (source.contains(surr))
      throw foreignKeyViolation(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int surr) {
    return ForeignKeyViolationException.unaryUnary(source.relvarName, target.relvarName, source.store.surrToValue(surr));
  }
}
