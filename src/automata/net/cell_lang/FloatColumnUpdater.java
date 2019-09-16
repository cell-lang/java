package net.cell_lang;


final class FloatColumnUpdater {
  boolean clear = false;
  int deleteCount = 0;
  int[] deleteIdxs = Array.emptyIntArray;

  int insertCount = 0;
  int[] insertIdxs = Array.emptyIntArray;
  double[] insertValues = Array.emptyDoubleArray;

  int updateCount = 0;
  int[] updateIdxs = Array.emptyIntArray;
  double[] updateValues = Array.emptyDoubleArray;

  // int minIdx = Integer.MAX_VALUE; //## IMPLEMENT THIS
  int maxIdx = -1;
  boolean dirty = false;
  boolean col1KeyViolated = false;
  long[] bitmap = Array.emptyLongArray;

  String relvarName;
  FloatColumn column;
  ValueStoreUpdater store;

  //////////////////////////////////////////////////////////////////////////////

  FloatColumnUpdater(String relvarName, FloatColumn column, ValueStoreUpdater store) {
    this.relvarName = relvarName;
    this.column = column;
    this.store = store;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void clear() {
    clear = true;
    deleteCount = 0;
  }

  public void delete1(int index) {
    delete(index);
  }

  public void delete(int index) {
    if (!clear) {
      if (deleteCount < deleteIdxs.length)
        deleteIdxs[deleteCount++] = index;
      else
        deleteIdxs = Array.append(deleteIdxs, deleteCount++, index);
      if (index > maxIdx)
        maxIdx = index;
    }
  }

  public void insert(int index, double value) {
    if (insertCount < insertIdxs.length) {
      insertIdxs[insertCount] = index;
      insertValues[insertCount++] = value;
    }
    else {
      insertIdxs = Array.append(insertIdxs, insertCount, index);
      insertValues = Array.append(insertValues, insertCount++, value);
    }
    if (index > maxIdx)
      maxIdx = index;
  }

  public void update(int index, double value) {
    if (updateCount < updateIdxs.length) {
      updateIdxs[updateCount] = index;
      updateValues[updateCount++] = value;
    }
    else {
      updateIdxs = Array.append(updateIdxs, updateCount, index);
      updateValues = Array.append(updateValues, updateCount++, value);
    }
    if (index > maxIdx)
      maxIdx = index;
  }

  public void apply() {
    if (clear) {
      column.clear();
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int index = deleteIdxs[i];
        column.delete(index);
      }
    }

    for (int i=0 ; i < updateCount ; i++) {
      int index = updateIdxs[i];
      double value = updateValues[i];
      column.update(index, value);
    }

    for (int i=0 ; i < insertCount ; i++) {
      int index = insertIdxs[i];
      double value = insertValues[i];
      column.insert(index, value);
    }
  }

  public void finish() {

  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    maxIdx = -1;

    if (dirty) {
      dirty = false;
      col1KeyViolated = false;

      int count = deleteCount + insertCount + updateCount;

      if (!clear && 3 * count < bitmap.length) {
        for (int i=0 ; i < deleteCount ; i++)
          bitmap[deleteIdxs[i] / 32] = 0;

        for (int i=0 ; i < updateCount ; i++)
          bitmap[updateIdxs[i] / 32] = 0;

        for (int i=0 ; i < insertCount ; i++)
          bitmap[insertIdxs[i] / 32] = 0;
      }
      else
        Array.fill(bitmap, 0);
    }

    clear = false;

    deleteCount = 0;
    insertCount = 0;
    updateCount = 0;

    if (deleteIdxs.length > 2048)
      deleteIdxs = Array.emptyIntArray;

    if (insertIdxs.length > 2048) {
      insertIdxs = Array.emptyIntArray;
      insertValues = Array.emptyDoubleArray;
    }

    if (updateIdxs.length > 2048) {
      updateIdxs = Array.emptyIntArray;
      updateValues = Array.emptyDoubleArray;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean contains1(int surr1) {
    if (surr1 > maxIdx)
      return !clear && column.contains1(surr1);

    // This call is only needed to build the delete/update/insert bitmap
    if (!dirty)
      buildBitmapAndCheckKey();

    int slotIdx = surr1 / 32;
    int bitsShift = 2 * (surr1 % 32);
    long slot = bitmap[slotIdx];
    long status = slot >> bitsShift;

    if ((status & 2) != 0)
      return true;  // Inserted/updated
    else if ((status & 1) != 0)
      return false; // Deleted and not reinserted
    else
      return column.contains1(surr1); // Untouched
  }

  //////////////////////////////////////////////////////////////////////////////

  private double lookup(int surr1) {
    if (surr1 <= maxIdx & (insertCount != 0 | updateCount != 0)) {
      Miscellanea._assert(dirty);

      int slotIdx = surr1 / 32;
      int bitsShift = 2 * (surr1 % 32);
      long slot = bitmap[slotIdx];
      long status = slot >> bitsShift;

      if ((status & 2) != 0) {
        for (int i=0 ; i < insertCount ; i++)
          if (insertIdxs[i] == surr1)
            return insertValues[i];

        for (int i=0 ; i < updateCount ; i++)
          if (updateIdxs[i] == surr1)
            return updateValues[i];

        Miscellanea.internalFail();
      }
    }

    return column.lookup(surr1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_1() {
    if (insertCount != 0 | updateCount != 0) {
      Miscellanea._assert(maxIdx != -1);
      Miscellanea._assert(!dirty);

      buildBitmapAndCheckKey();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private void buildBitmapAndCheckKey() {
    dirty = true;

    if (maxIdx / 32 >= bitmap.length)
      bitmap = Array.extend(bitmap, Array.capacity(bitmap.length, maxIdx / 32 + 1));

    // 00 - untouched
    // 01 - deleted
    // 10 - inserted
    // 11 - updated or inserted and deleted

    if (clear) {
      Array.fill(bitmap, 0x5555555555555555L); //## BAD: THIS CAN BE MADE MORE EFFICIENT
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int idx = deleteIdxs[i];
        int slotIdx = idx / 32;
        int bitsShift = 2 * (idx % 32);
        bitmap[slotIdx] |= 1L << bitsShift;
      }
    }

    for (int i=0 ; i < updateCount ; i++) {
      int idx = updateIdxs[i];
      int slotIdx = idx / 32;
      int bitsShift = 2 * (idx % 32);
      long slot = bitmap[slotIdx];
      if (((slot >> bitsShift) & 2) != 0)
        //## HERE I WOULD ACTUALLY NEED TO CHECK THAT THE NEW VALUE IS DIFFERENT FROM THE OLD ONE
        throw col1KeyViolation(idx, updateValues[i], true);
      bitmap[slotIdx] = slot | (3L << bitsShift);
    }

    for (int i=0 ; i < insertCount ; i++) {
      int idx = insertIdxs[i];
      int slotIdx = idx / 32;
      int bitsShift = 2 * (idx % 32);
      long slot = bitmap[slotIdx];
      int bits = (int) ((slot >> bitsShift) & 3);
      if (bits >= 2)
        //## HERE I WOULD ACTUALLY NEED TO CHECK THAT THE NEW VALUE IS DIFFERENT FROM THE OLD ONE
        throw col1KeyViolation(idx, insertValues[i], true);
      if ((bits == 0 && column.contains1(idx)))
        //## HERE I WOULD ACTUALLY NEED TO CHECK THAT THE NEW VALUE IS DIFFERENT FROM THE OLD ONE
        throw col1KeyViolation(idx, insertValues[i], false);
      bitmap[slotIdx] = slot | (2L << bitsShift);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  // bin_rel(a, _) -> unary_rel(a);
  public void checkForeignKeys_1(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertIdxs[i]))
        throw foreignKeyViolation(insertIdxs[i], insertValues[i], target);

    for (int i=0 ; i < updateCount ; i++)
      if (!target.contains(updateIdxs[i]))
        throw foreignKeyViolation(updateIdxs[i], updateValues[i], target);

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletedKeys(deleteChecker);
  }

  UnaryTableUpdater.DeleteChecker deleteChecker =
    new UnaryTableUpdater.DeleteChecker() {
      public void check(UnaryTableUpdater updater, int surr) {
        if (contains1(surr))
          throw foreignKeyViolation(surr, updater);
      }
    };

  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException col1KeyViolation(int idx, double value, boolean betweenNew) {
    if (betweenNew) {
      for (int i=0 ; i < updateCount ; i++)
        if (updateIdxs[i] == idx)
          return col1KeyViolation(idx, value, updateValues[i], betweenNew);

      for (int i=0 ; i < insertCount ; i++)
        if (insertIdxs[i] == idx)
          return col1KeyViolation(idx, value, insertValues[i], betweenNew);

      throw Miscellanea.internalFail();
    }
    else
      return col1KeyViolation(idx, value, column.lookup(idx), betweenNew);
  }

  private KeyViolationException col1KeyViolation(int idx, double value, double otherValue, boolean betweenNew) {
    //## BUG: Stores may contain only part of the value (id(5) -> 5)
    Obj key = store.surrToValue(idx);
    Obj[] tuple1 = new Obj[] {key, new FloatObj(value)};
    Obj[] tuple2 = new Obj[] {key, new FloatObj(otherValue)};
    return new KeyViolationException(relvarName, KeyViolationException.key_1, tuple1, tuple2, betweenNew);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException foreignKeyViolation(int keySurr, double value, UnaryTableUpdater target) {
    Obj[] tuple = new Obj[] {store.surrToValue(keySurr), new FloatObj(value)};
    return ForeignKeyViolationException.binaryUnary(relvarName, 1, target.relvarName, tuple);
  }

  private ForeignKeyViolationException foreignKeyViolation(int keySurr, UnaryTableUpdater target) {
    Obj key = store.surrToValue(keySurr);
    Obj[] fromTuple = new Obj[] {key, new FloatObj(lookup(keySurr))};
    return ForeignKeyViolationException.binaryUnary(relvarName, 1, target.relvarName, fromTuple, key);
  }
}