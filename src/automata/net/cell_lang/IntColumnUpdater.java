package net.cell_lang;

import java.util.function.IntPredicate;


final class IntColumnUpdater {
  int deleteCount = 0;
  int[] deleteIdxs = Array.emptyIntArray;

  int insertCount = 0;
  int[] insertIdxs = Array.emptyIntArray;
  long[] insertValues = Array.emptyLongArray;

  int updateCount = 0;
  int[] updateIdxs = Array.emptyIntArray;
  long[] updateValues = Array.emptyLongArray;

  int maxIdx = -1;
  boolean dirty = false;
  long[] bitmap = Array.emptyLongArray;

  IntColumn column;

  //////////////////////////////////////////////////////////////////////////////

  IntColumnUpdater(IntColumn column, ValueStoreUpdater storeUpdater) {
    this.column = column;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void clear() {
    throw Miscellanea.internalFail();
  }

  public void delete1(int index) {
    delete(index);
  }

  public void delete(int index) {
    if (deleteCount < deleteIdxs.length)
      deleteIdxs[deleteCount++] = index;
    else
      deleteIdxs = Array.append(deleteIdxs, deleteCount++, index);
    if (index > maxIdx)
      maxIdx = index;
  }

  public void insert(int index, long value) {
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

  public void update(int index, long value) {
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
    for (int i=0 ; i < deleteCount ; i++) {
      int index = deleteIdxs[i];
      column.delete(index);
    }

    for (int i=0 ; i < updateCount ; i++) {
      int index = updateIdxs[i];
      long value = updateValues[i];
      column.update(index, value);
    }

    for (int i=0 ; i < insertCount ; i++) {
      int index = insertIdxs[i];
      long value = insertValues[i];
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

      int count = deleteCount + insertCount + updateCount;

      if (3 * count < bitmap.length) {
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

    deleteCount = 0;
    insertCount = 0;
    updateCount = 0;

    if (deleteIdxs.length > 2048)
      deleteIdxs = Array.emptyIntArray;

    if (insertIdxs.length > 2048) {
      insertIdxs = Array.emptyIntArray;
      insertValues = Array.emptyLongArray;
    }

    if (updateIdxs.length > 2048) {
      updateIdxs = Array.emptyIntArray;
      updateValues = Array.emptyLongArray;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains1(int surr1) {
    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean checkKey_1() {
    if (insertCount == 0 & updateCount == 0)
      return true;
    Miscellanea._assert(maxIdx != -1);

    if (maxIdx / 32 >= bitmap.length)
      bitmap = Array.extend(bitmap, Array.capacity(bitmap.length, maxIdx / 32 + 1));

    // 00 - untouched
    // 01 - deleted
    // 10 - inserted
    // 11 - updated or inserted and deleted

    dirty = true;

    for (int i=0 ; i < deleteCount ; i++) {
      int idx = deleteIdxs[i];
      int slotIdx = idx / 32;
      int bitsShift = 2 * (idx % 32);
      bitmap[slotIdx] |= 1L << bitsShift;
    }

    for (int i=0 ; i < updateCount ; i++) {
      int idx = updateIdxs[i];
      int slotIdx = idx / 32;
      int bitsShift = 2 * (idx % 32);
      long slot = bitmap[slotIdx];
      if (((slot >> bitsShift) & 2) != 0) {
        //## HERE I WOULD ACTUALLY NEED TO CHECK THAT THE NEW VALUE IS DIFFERENT FROM THE OLD ONE
        return false;
      }
      bitmap[slotIdx] = slot | (3L << bitsShift);
    }

    for (int i=0 ; i < insertCount ; i++) {
      int idx = insertIdxs[i];
      int slotIdx = idx / 32;
      int bitsShift = 2 * (idx % 32);
      long slot = bitmap[slotIdx];
      int bits = (int) ((slot >> bitsShift) & 3);
      if ((bits == 0 && column.contains1(idx)) | bits >= 2) {
        //## HERE I WOULD ACTUALLY NEED TO CHECK THAT THE NEW VALUE IS DIFFERENT FROM THE OLD ONE
        return false;
      }
      bitmap[slotIdx] = slot | (2L << bitsShift);
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean checkDeletedKeys_1(IntPredicate source) {
    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////

  // bin_rel(a, _) -> unary_rel(a);
  public boolean checkForeignKeys_1(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertIdxs[i]))
        return false;

    for (int i=0 ; i < updateCount ; i++)
      if (!target.contains(updateIdxs[i]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains1);
  }
}