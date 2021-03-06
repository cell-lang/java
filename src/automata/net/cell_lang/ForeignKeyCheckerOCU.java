package net.cell_lang;


// bin_rel(a, _) -> unary_rel(a);

class ForeignKeyCheckerOCU implements UnaryTableUpdater.DeleteChecker {
  ObjColumnUpdater source;
  UnaryTableUpdater target;

  public ForeignKeyCheckerOCU(ObjColumnUpdater source, UnaryTableUpdater target) {
    Miscellanea._assert(source.store == target.store);
    this.source = source;
    this.target = target;
  }

  public void check() {
    // Checking that every new entry satisfies the foreign key
    int count = source.insertCount;
    if (count > 0) {
      int[] idxs = source.insertIdxs;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(idxs[i]))
          throw foreignKeyViolation(idxs[i], source.insertValues[i]);
    }

    count = source.updateCount;
    if (count > 0) {
      int[] idxs = source.updateIdxs;
      for (int i=0 ; i < count ; i++)
        if (!target.contains(idxs[i]))
          throw foreignKeyViolation(idxs[i], source.updateValues[i]);
    }

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletedKeys(this);
  }

  public void wasDeleted(int surr) {
    if (source.contains1(surr))
      throw foreignKeyViolation(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int keySurr, Obj value) {
    Obj[] tuple = new Obj[] {source.store.surrToValue(keySurr), value};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 1, target.relvarName, tuple);
  }

  private ForeignKeyViolationException foreignKeyViolation(int keySurr) {
    Obj key = source.store.surrToValue(keySurr);
    Obj[] fromTuple = new Obj[] {key, source.lookup(keySurr)};
    return ForeignKeyViolationException.binaryUnary(source.relvarName, 1, target.relvarName, fromTuple, key);
  }
}
