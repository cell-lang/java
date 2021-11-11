package net.cell_lang;


final class SlaveObjColumnUpdater {
  int deleteCount = 0;
  int[] deleteIdxs = Array.emptyIntArray;
  boolean deletesSorted = false;

  int insertCount = 0;
  long[] insertSurrs = Array.emptyLongArray;
  Obj[] insertValues = Array.emptyObjArray;
  boolean insertsSorted = false;

  int updateCount = 0;
  long[] updateSurrs = Array.emptyLongArray;
  Obj[] updateValues = Array.emptyObjArray;
  boolean updatesSorted;

  String relvarName;
  ObjColumn column;
  MasterBinaryTable master;
  MasterBinaryTableUpdater masterUpdater;
  ValueStoreUpdater store1, store2;

  //////////////////////////////////////////////////////////////////////////////

  private static long pack(int arg1, int arg2) {
    return Miscellanea.pack(arg1, arg2);
  }

  public static int arg1(long slot) {
    return Miscellanea.low(slot);
  }

  public static int arg2(long slot) {
    return Miscellanea.high(slot);
  }

  //////////////////////////////////////////////////////////////////////////////

  SlaveObjColumnUpdater(String relvarName, ObjColumn column, MasterBinaryTableUpdater master, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.relvarName = relvarName;
    this.column = column;
    this.master = master.table;
    this.masterUpdater = master;
    this.store1 = store1;
    this.store2 = store2;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void clear() {
    ObjColumn.Iter it = column.getIter();
    while (!it.done()) {
      delete(it.getIdx());
      it.next();
    }
  }

  public void delete1(int arg1) {
    //## THIS COULD BE MADE MORE EFFICIENT BY RETRIEVING JUST THE SURROGATES INSTEAD OF THE SECOND ARGUMENTS
    int[] arg2s = master.restrict1(arg1);
    for (int i=0 ; i < arg2s.length ; i++)
      delete12(arg1, arg2s[i]);
  }

  public void delete2(int arg2) {
    //## THIS COULD BE MADE MORE EFFICIENT BY RETRIEVING JUT THE SURROGATES INSTEAD OF THE FIRST ARGUMENTS
    int[] arg1s = master.restrict2(arg2);
    for (int i=0 ; i < arg1s.length ; i++)
      delete12(arg1s[i], arg2);
  }

  public void delete12(int arg1, int arg2) {
    int surr = master.surrogate(arg1, arg2);
    if (surr != 0xFFFFFFFF)
      delete(surr);
  }

  private void delete(int index) {
    deleteIdxs = Array.append(deleteIdxs, deleteCount++, index);
  }

  public void insert(int arg1, int arg2, Obj value) {
    insertSurrs = Array.append(insertSurrs, insertCount, pack(arg1, arg2));
    insertValues = Array.append(insertValues, insertCount++, value);
  }

  public void update(int arg1, int arg2, Obj value) {
    updateSurrs = Array.append(updateSurrs, updateCount, pack(arg1, arg2));
    updateValues = Array.append(updateValues, updateCount++, value);
  }

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      int index = deleteIdxs[i];
      column.delete(index);
    }

    for (int i=0 ; i < updateCount ; i++) {
      long slot = updateSurrs[i];
      int arg1 = arg1(slot);
      int arg2 = arg2(slot);
      Obj value = updateValues[i];
      int index = master.surrogate(arg1, arg2);
      column.update(index, value);
    }

    for (int i=0 ; i < insertCount ; i++) {
      long slot = insertSurrs[i];
      int arg1 = arg1(slot);
      int arg2 = arg2(slot);
      Obj value = insertValues[i];
      int index = master.surrogate(arg1, arg2);
      column.insert(index, value);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    deleteCount = 0;
    insertCount = 0;
    updateCount = 0;

    deletesSorted = false;
    insertsSorted = false;
    updatesSorted = false;

    if (deleteIdxs.length > 2048)
      deleteIdxs = Array.emptyIntArray;

    if (insertSurrs.length > 2048) {
      insertSurrs = Array.emptyLongArray;
      insertValues = Array.emptyObjArray;
    }

    if (updateSurrs.length > 2048) {
      updateSurrs = Array.emptyLongArray;
      updateValues = Array.emptyObjArray;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  boolean wasDeleted(int idx) {
    if (deleteCount > 32) {
      if (!deletesSorted) {
        Array.sort(deleteIdxs, deleteCount);
        deletesSorted = true;
      }
      return Array.sortedArrayContains(deleteIdxs, deleteCount, idx);
    }
    else {
      for (int i=0 ; i < deleteCount ; i++)
        if (deleteIdxs[i] == idx)
          return true;
      return false;
    }
  }

  boolean wasUpdated(long packedArgs) {
    Miscellanea._assert(updateCount <= 1 || updatesSorted);
    return Array.sortedArrayContains(updateSurrs, updateCount, packedArgs);
  }

  boolean contains12(int arg1, int arg2) {
    long packedArgs = pack(arg1, arg2);

    if (insertCount > 32) {
      if (!insertsSorted) {
        LongWithObjSorter.sort(insertSurrs, insertCount, insertValues);
        insertsSorted = true;
      }
      if (Array.sortedArrayContains(insertSurrs, insertCount, packedArgs))
        return true;
    }
    else {
      for (int i=0 ; i < insertCount ; i++)
        if (insertSurrs[i] == packedArgs)
          return true;
    }

    if (updateCount > 32) {
      if (!updatesSorted) {
        LongWithObjSorter.sort(updateSurrs, updateCount, updateValues);
        updatesSorted = true;
      }
      if (Array.sortedArrayContains(updateSurrs, updateCount, packedArgs))
        return true;
    }
    else {
      for (int i=0 ; i < updateCount ; i++)
        if (updateSurrs[i] == packedArgs)
          return true;
    }

    int assocSurr = master.surrogate(arg1, arg2);
    if (assocSurr == 0xFFFFFFFF || wasDeleted(assocSurr))
      return false;

    return column.contains1(assocSurr);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_12() {
    if (updateCount > 0) {
      if (!updatesSorted) {
        LongWithObjSorter.sort(updateSurrs, updateCount, updateValues);
        updatesSorted = true;
      }

      long prevPackedArgs = updateSurrs[0];
      for (int i=1 ; i < updateCount ; i++) {
        long packedArgs = updateSurrs[i];
        if (packedArgs == prevPackedArgs) {
          Obj obj = updateValues[i];
          Obj prevObj = updateValues[i - 1];
          if (!obj.isEq(prevObj))
            throw cols12KeyViolationException(arg1(packedArgs), arg2(packedArgs), obj, prevObj, true);
        }
        prevPackedArgs = packedArgs;
      }
    }

    if (insertCount > 0) {
      if (!insertsSorted) {
        LongWithObjSorter.sort(insertSurrs, insertCount, insertValues);
        insertsSorted = true;
      }

      long prevPackedArgs = -1;
      for (int i=0 ; i < insertCount ; i++) {
        long packedArgs = insertSurrs[i];
        if (i > 0 && packedArgs == prevPackedArgs) {
          Obj obj = insertValues[i];
          Obj prevObj = insertValues[i - 1];
          if (!obj.isEq(prevObj))
            throw cols12KeyViolationException(arg1(packedArgs), arg2(packedArgs), obj, prevObj, true);
        }

        int arg1 = arg1(packedArgs);
        int arg2 = arg2(packedArgs);

        int assocSurr = master.surrogate(arg1, arg2);
        if (assocSurr != 0xFFFFFFFF && column.contains1(assocSurr) && !wasDeleted(assocSurr) && !wasUpdated(packedArgs)) {
          Obj newValue = insertValues[i];
          Obj currValue = column.lookup(assocSurr);
          if (!newValue.isEq(currValue))
            throw cols12KeyViolationException(arg1, arg2, currValue, newValue, false);
        }

        prevPackedArgs = packedArgs;
      }
    }

    if (insertCount > 0 & updateCount > 0) {
      for (int i=0 ; i < insertCount ; i++) {
        long packedArgs = insertSurrs[i];
        int idx = Array.indexFirst(updateSurrs, updateCount, packedArgs);
        if (idx != -1) {
          Obj insertValue = insertValues[i];
          Obj updateValue = updateValues[idx];
          if (!insertValue.isEq(updateValue))
            throw cols12KeyViolationException(arg1(packedArgs), arg2(packedArgs), insertValue, updateValue, true);
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException cols12KeyViolationException(int arg1, int arg2, Obj obj3, Obj otherObj3, boolean betweenNew) {
    Obj obj1 = masterUpdater.store1.surrToValue(arg1);
    Obj obj2 = masterUpdater.store2.surrToValue(arg2);
    Obj[] tuple1 = new Obj[] {obj1, obj2, obj3};
    Obj[] tuple2 = new Obj[] {obj1, obj2, otherObj3};
    return new KeyViolationException(relvarName, KeyViolationException.key_12, tuple1, tuple2, betweenNew);
  }
}
