package net.cell_lang;


class BinaryTableUpdater {
  private boolean hasDeletes = false;
  private boolean clear = false;

  private int deleteCount = 0;
  private long[] deleteList = Array.emptyLongArray;
  private boolean deleteListIsSorted = false; // Sorted exclusively in left-to-right order

  private int delete1Count = 0;
  private int[] delete1List = Array.emptyIntArray;
  private boolean delete1ListIsSorted = false;

  private int delete2Count = 0;
  private int[] delete2List = Array.emptyIntArray;
  private boolean delete2ListIsSorted = false;

  private int[] buffer = Array.emptyIntArray;

  private int insertCount = 0;
  private long[] insertList = Array.emptyLongArray;
  private Ord insertListOrd = Ord.ORD_NONE;


  private enum Ord {ORD_NONE, ORD_12, ORD_21};


  String relvarName;
  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  //////////////////////////////////////////////////////////////////////////////

  private static long tuple(int arg1, int arg2) {
    return Miscellanea.pack(arg2, arg1);
  }

  static int arg1(long tuple) {
    return Miscellanea.high(tuple);
  }

  static int arg2(long tuple) {
    return Miscellanea.low(tuple);
  }

  //////////////////////////////////////////////////////////////////////////////

  public BinaryTableUpdater(String relvarName, BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.relvarName = relvarName;
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void clear() {
    hasDeletes = true;
    clear = true;
  }

  public void delete(int arg1, int arg2) {
    hasDeletes = true;
    deleteList = Array.append(deleteList, deleteCount++, tuple(arg1, arg2));
  }

  public void delete1(int arg1) {
    if (table.contains1(arg1)) {
      hasDeletes = true;
      delete1List = Array.append(delete1List, delete1Count++, arg1);
    }
  }

  public void delete2(int arg2) {
    if (table.contains2(arg2)) {
      hasDeletes = true;
      delete2List = Array.append(delete2List, delete2Count++, arg2);
    }
  }

  public void insert(int arg1, int arg2) {
    insertList = Array.append(insertList, insertCount++, tuple(arg1, arg2));
  }

  //////////////////////////////////////////////////////////////////////////////

  public void apply() {
    if (hasDeletes) {
      if (clear) {
        //## IF THE RIGHT-TO-LEFT SEARCH DATA STRUCTURES HAVE BEEN BUILD
        //## THERE'S A MUCH FASTER WAY TO IMPLEMENT THIS
        int left = table.size();
        for (int arg1=0 ; left > 0 ; arg1++) {
          int count = table.count1(arg1);
          if (count > 0) {
            store1.markForDelayedRelease(arg1, count);
            if (buffer.length < count)
              buffer = new int[Array.capacity(buffer.length, count)];
            table.delete1(arg1, buffer);
            for (int i2=0 ; i2 < count ; i2++)
              store2.markForDelayedRelease(buffer[i2]);
            left -= count;
          }
        }
      }
      else {
        if (delete1Count != 0) {
          int next = 0;
          for (int i1=0 ; i1 < delete1Count ; i1++) {
            int arg1 = delete1List[i1];
            int count = table.count1(arg1);
            if (count > 0) {
              store1.markForDelayedRelease(arg1, count);
              if (buffer.length < count)
                buffer = new int[Array.capacity(buffer.length, count)];
              table.delete1(arg1, buffer);
              for (int i2=0 ; i2 < count ; i2++)
                store2.markForDelayedRelease(buffer[i2]);
            }
          }
        }

        if (delete2Count != 0) {
          int next = 0;
          for (int i2=0 ; i2 < delete2Count ; i2++) {
            int arg2 = delete2List[i2];
            int count = table.count2(arg2);
            if (count > 0) {
              store2.markForDelayedRelease(arg2, count);
              if (buffer.length < count)
                buffer = new int[Array.capacity(buffer.length, count)];
              table.delete2(arg2, buffer);
              for (int i1=0 ; i1 < count ; i1++)
                store1.markForDelayedRelease(buffer[i1]);
            }
          }
        }

        for (int i=0 ; i < deleteCount ; i++) {
          long entry = deleteList[i];
          int arg1 = arg1(entry);
          int arg2 = arg2(entry);
          if (table.delete(arg1, arg2)) {
            store1.markForDelayedRelease(arg1);
            store2.markForDelayedRelease(arg2);
          }
        }
      }
    }

    for (int i=0 ; i < insertCount ; i++) {
      long entry = insertList[i];
      int arg1 = arg1(entry);
      int arg2 = arg2(entry);
      if (table.insert(arg1, arg2)) {
        store1.addRef(arg1);
        store2.addRef(arg2);
      }
    }
  }

  public void finish() {

  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    if (hasDeletes) {
      hasDeletes = false;

      if (clear)
        clear = false;

      if (deleteCount > 0) {
        deleteCount = 0;
        deleteListIsSorted = false;
        if (deleteList.length > 1024)
          deleteList = Array.emptyLongArray;
      }

      if (delete1Count > 0) {
        delete1Count = 0;
        delete1ListIsSorted = false;
        if (delete1List.length > 1024)
          delete1List = Array.emptyIntArray;
      }

      if (delete2Count > 0) {
        delete2Count = 0;
        delete1ListIsSorted = false;
        if (delete2List.length > 1024)
          delete2List = Array.emptyIntArray;
      }

      if (buffer.length > 1024)
        buffer = Array.emptyIntArray;
    }

    if (insertCount > 0) {
      insertCount = 0;
      insertListOrd = Ord.ORD_NONE;
      if (insertList.length > 1024)
        insertList = Array.emptyLongArray;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains(int arg1, int arg2) {
    if (wasInserted(arg1, arg2))
      return true;

    if (clear || wereDeleted1(arg1) || wereDeleted2(arg2) || wasDeleted(arg1, arg2))
      return false;

    return table.contains(arg1, arg2);
  }

  public boolean contains1(int arg1) {
    return wasInserted1(arg1) || containsUndeleted1(arg1);
  }

  public boolean contains2(int arg2) {
    return wasInserted2(arg2) || containsUndeleted2(arg2);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////// These methods are reserved for foreign key checkers /////////////

  boolean hasInsertions() {
    return insertCount > 0;
  }

  long[] insertions(long[] buffer, int[] counter) {
    if (insertCount > buffer.length)
      buffer = new long[insertCount];
    counter[0] = insertCount;
    Array.copy(insertList, buffer, insertCount);
    return buffer;
  }

  boolean wasCleared() {
    return clear;
  }

  boolean hasPartialDeletes() {
    return deleteCount > 0 || delete1Count > 0 || delete2Count > 0;
  }

  long[] deletes(long[] buffer, int[] counter) {
    int count = deleteCount;
    for (int i=0 ; i < delete1Count ; i++)
      count += table.count1(delete1List[i]);
    for (int i=0 ; i < delete2Count ; i++)
      count += table.count2(delete2List[i]);
    counter[0] = count;

    if (count > buffer.length)
      buffer = new long[count];
    Array.copy(deleteList, buffer, deleteCount);
    int idx = deleteCount;

    for (int i1=0 ; i1 < delete1Count ; i1++) {
      int arg1 = delete1List[i1];
      int[] arg2s = table.restrict1(arg1);
      for (int i2=0 ; i2 < arg2s.length ; i2++)
        buffer[idx++] = tuple(arg1, arg2s[i2]);
    }

    for (int i2=0 ; i2 < delete2Count ; i2++) {
      int arg2 = delete2List[i2];
      int[] arg1s = table.restrict2(arg2);
      for (int i1=0 ; i1 < arg1s.length ; i1++)
        buffer[idx++] = tuple(arg1s[i1], arg2);
    }

    Miscellanea._assert(idx == count);
    return buffer;
  }

  int[] deletes1(int[] buffer, int[] counter) {
    int count = deleteCount + delete1Count;
    for (int i=0 ; i < delete2Count ; i++)
      count += table.count2(delete2List[i]);
    counter[0] = count;
    if (count > buffer.length)
      buffer = new int[count];
    int idx = 0;
    for (int i=0 ; i < deleteCount ; i++)
      buffer[idx++] = arg1(deleteList[i]);
    for (int i=0 ; i < delete1Count ; i++)
      buffer[idx++] = delete1List[i];
    for (int i2=0 ; i2 < delete2Count ; i2++) {
      int[] arg1s = table.restrict2(delete2List[i2]);
      for (int i1=0 ; i1 < arg1s.length ; i1++)
        buffer[idx++] = arg1s[i1];
    }
    Miscellanea._assert(idx == count);
    return buffer;
  }

  int[] deletes2(int[] buffer, int[] counter) {
    int count = deleteCount + delete2Count;
    for (int i=0 ; i < delete1Count ; i++)
      count += table.count1(delete1List[i]);
    counter[0] = count;
    if (count > buffer.length)
      buffer = new int[count];
    int idx = 0;
    for (int i=0 ; i < deleteCount ; i++)
      buffer[idx++] = arg2(deleteList[i]);
    for (int i1=0 ; i1 < delete1Count ; i1++) {
      int[] arg2s = table.restrict1(delete1List[i1]);
      for (int i2=0 ; i2 < arg2s.length ; i2++)
        buffer[idx++] = arg2s[i2];
    }
    Miscellanea._assert(idx == count);
    return buffer;
  }

  int anyDeletedArg1(int arg2) {
    //## NOT ESPECIALLY EFFICIENTS, BUT IT'S CALLED ONLY WHEN A FOREIGN KEY HAS BEEN VIOLATED
    int[] arg1s = table.restrict2(arg2);
    for (int i=0 ; i < arg1s.length ; i++)
      if (!contains(arg1s[i], arg2))
        return arg1s[i];
    throw Miscellanea.internalFail();
  }

  int anyDeletedArg2(int arg1) {
    //## NOT ESPECIALLY EFFICIENTS, BUT IT'S CALLED ONLY WHEN A FOREIGN KEY HAS BEEN VIOLATED
    int[] arg2s = table.restrict1(arg1);
    for (int i=0 ; i < arg2s.length ; i++)
      if (!contains(arg1, arg2s[i]))
        return arg2s[i];
    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean containsUndeleted1(int arg1) {
    if (clear || wereDeleted1(arg1))
      return false;

    if (!table.contains1(arg1))
      return false;

    //## BAD: THIS IS VERY INEFFICIENT IF THERE'S A LOT OF ENTRIES WHOSE FIRST ARGUMENT IS arg1
    int[] args2 = table.restrict1(arg1);
    for (int i=0 ; i < args2.length ; i++) {
      int arg2 = args2[i];
      if (wereDeleted2(arg2) || wasDeleted(arg1, arg2))
        return false;
    }

    return true;
  }

  private boolean containsUndeleted2(int arg2) {
    if (clear || wereDeleted2(arg2))
      return false;

    if (!table.contains2(arg2))
      return false;

    //## BAD: THIS IS VERY INEFFICIENT IF THERE'S A LOT OF ENTRIES WHOSE SECOND ARGUMENT IS arg2
    int[] args1 = table.restrict2(arg2);
    for (int i=0 ; i < args1.length ; i++) {
      int arg1 = args1[i];
      if (wereDeleted1(arg1) || wasDeleted(arg1, arg2))
        return false;
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean wasInserted(int arg1, int arg2) {
    if (insertCount <= 16) {
      for (int i=0 ; i < insertCount ; i++) {
        long entry = insertList[i];
        if (arg1 == arg1(entry) & arg2 == arg2(entry))
          return true;
      }
      return false;
    }
    else {
      sortInsertList();
      return Array.sortedArrayContains(insertList, insertCount, tuple(arg1, arg2));
    }
  }

  private boolean wasInserted1(int arg1) {
    if (insertCount <= 16) {
      for (int i=0 ; i < insertCount ; i++) {
        long entry = insertList[i];
        if (arg1 == arg1(entry))
          return true;
      }
      return false;
    }
    else {
      sortInsertList();
      return PackedIntPairs.containsMajor(insertList, insertCount, arg1);
    }
  }

  private boolean wasInserted2(int arg2) {
    if (insertCount <= 16) {
      for (int i=0 ; i < insertCount ; i++) {
        long entry = insertList[i];
        if (arg2 == arg2(entry))
          return true;
      }
      return false;
    }
    else {
      sortInsertListFlipped();
      return PackedIntPairs.containsMinor(insertList, insertCount, arg2);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean wasDeleted(int arg1, int arg2) {
    if (deleteCount <= 16) {
      for (int i=0 ; i < deleteCount ; i++) {
        long entry = deleteList[i];
        if (arg1(entry) == arg1 & arg2(entry) == arg2)
          return true;
      }
      return false;
    }
    else {
      sortDeleteList();
      return Array.sortedArrayContains(deleteList, deleteCount, tuple(arg1, arg2));
    }
  }

  private boolean wereDeleted1(int arg1) {
    if (delete1Count <= 16) {
      for (int i=0 ; i < delete1Count ; i++)
        if (delete1List[i] == arg1)
          return true;
      return false;
    }
    else {
      sortDelete1List();
      return Array.sortedArrayContains(delete1List, delete1Count, arg1);
    }
  }

  private boolean wereDeleted2(int arg2) {
    if (delete2Count <= 16) {
      for (int i=0 ; i < delete2Count ; i++)
        if (delete2List[i] == arg2)
          return true;
      return false;
    }
    else {
      sortDelete2List();
      return Array.sortedArrayContains(delete2List, delete2Count, arg2);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private void sortInsertList() {
    if (insertListOrd != Ord.ORD_12) {
      Miscellanea._assert(insertListOrd == Ord.ORD_NONE);
      PackedIntPairs.sort(insertList, insertCount);
      insertListOrd = Ord.ORD_12;
    }
  }

  private void sortInsertListFlipped() {
    if (insertListOrd != Ord.ORD_21) {
      PackedIntPairs.sortFlipped(insertList, insertCount);
      insertListOrd = Ord.ORD_21;
    }
  }

  private void sortDeleteList() {
    if (!deleteListIsSorted) {
      Array.sort(deleteList, deleteCount);
      deleteListIsSorted = true;
    }
  }

  private void sortDelete1List() {
    if (!delete1ListIsSorted) {
      Array.sort(delete1List, delete1Count);
      delete1ListIsSorted = true;
    }
  }

  private void sortDelete2List() {
    if (!delete2ListIsSorted) {
      Array.sort(delete2List, delete2Count);
      delete2ListIsSorted = true;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_1() {
    if (insertCount != 0) {
      sortInsertList();

      long prev = -1;
      for (int i=0 ; i < insertCount ; i++) {
        long entry = insertList[i];

        if (entry != prev) {
          int arg1 = arg1(entry);

          if (arg1 == arg1(prev))
            throw col1KeyViolation(arg1, arg2(entry), arg2(prev));

          if (containsUndeleted1(arg1))
            throw col1KeyViolation(arg1, arg2(entry));
        }

        prev = entry;
      }
    }
  }

  public void checkKey_2() {
    if (insertCount != 0) {
      sortInsertListFlipped();

      long prev = -1;
      for (int i=0 ; i < insertCount ; i++) {
        long entry = insertList[i];

        if (entry != prev) {
          int arg2 = arg2(entry);

          if (arg2 == arg2(prev))
            throw col2KeyViolation(arg1(entry), arg2, arg1(prev));

          if (containsUndeleted2(arg2))
            throw col2KeyViolation(arg1(entry), arg2);
        }

        prev = entry;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException col1KeyViolation(int surr1, int surr2, int otherArg2Surr) {
    return col1KeyViolation(surr1, surr2, otherArg2Surr, true);
  }

  private KeyViolationException col1KeyViolation(int surr1, int surr2) {
    return col1KeyViolation(surr1, surr2, table.restrict1(surr1)[0], false);
  }

  private KeyViolationException col1KeyViolation(int surr1, int surr2, int otherArg2Surr, boolean betweenNew) {
    //## BUG: STORES MAY CONTAIN ONLY PART OF THE ACTUAL VALUE (id(5) -> 5)
    Obj obj1 = store1.surrToValue(surr1);
    Obj[] tuple1 = new Obj[] {obj1, store2.surrToValue(surr2)};
    Obj[] tuple2 = new Obj[] {obj1, store2.surrToValue(otherArg2Surr)};
    return new KeyViolationException(relvarName, KeyViolationException.key_1, tuple1, tuple2, betweenNew);
  }

  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException col2KeyViolation(int surr1, int surr2, int otherArg1Surr) {
    return col2KeyViolation(surr1, surr2, otherArg1Surr, true);
  }

  private KeyViolationException col2KeyViolation(int surr1, int surr2) {
    return col2KeyViolation(surr1, surr2, table.restrict2(surr2)[0], false);
  }

  private KeyViolationException col2KeyViolation(int surr1, int surr2, int otherArg1Surr, boolean betweenNew) {
    //## BUG: STORES MAY CONTAIN ONLY PART OF THE ACTUAL VALUE (id(5) -> 5)
    Obj obj2 = store2.surrToValue(surr2);
    Obj[] tuple1 = new Obj[] {store1.surrToValue(surr1), obj2};
    Obj[] tuple2 = new Obj[] {store1.surrToValue(otherArg1Surr), obj2};
    return new KeyViolationException(relvarName, KeyViolationException.key_2, tuple1, tuple2, betweenNew);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // public void dump(boolean flipped) {
  //   System.out.print("deleteList =");
  //   for (int i=0 ; i < deleteCount ; i++)
  //     if (flipped)
  //       System.out.printf(" (%d, %d)", deleteList[2 * i + 1], deleteList[2 * i]);
  //     else
  //       System.out.printf(" (%d, %d)", deleteList[2 * i], deleteList[2 * i + 1]);
  //   System.out.println();

  //   System.out.print("insertList =");
  //   for (int i=0 ; i < insertCount ; i++)
  //     if (flipped)
  //       System.out.printf(" (%d, %d)", insertList[2 * i + 1], insertList[2 * i]);
  //     else
  //       System.out.printf(" (%d, %d)", insertList[2 * i], insertList[2 * i + 1]);
  //   System.out.println("\n");

  //   System.out.print("deleteList =");
  //   for (int i=0 ; i < deleteCount ; i++) {
  //     int arg1 = deleteList[2 * i];
  //     int arg2 = deleteList[2 * i + 1];
  //     Obj obj1 = store1.surrToValue(arg1);
  //     Obj obj2 = store2.surrToValue(arg2);
  //     if (flipped) {
  //       Obj tmp = obj1;
  //       obj1 = obj2;
  //       obj2 = tmp;
  //     }
  //     System.out.printf(" (%s, %s)", obj1.toString(), obj2.toString());
  //   }
  //   System.out.println("");

  //   System.out.print("insertList =");
  //   for (int i=0 ; i < insertCount ; i++) {
  //     int arg1 = insertList[2 * i];
  //     int arg2 = insertList[2 * i + 1];
  //     Obj obj1 = store1.surrToValue(arg1);
  //     Obj obj2 = store2.surrToValue(arg2);
  //     if (flipped) {
  //       Obj tmp = obj1;
  //       obj1 = obj2;
  //       obj2 = tmp;
  //     }
  //     System.out.printf(" (%s, %s)",
  //       obj1 != null ? obj1.toString() : "null",
  //       obj2 != null ? obj2.toString() : "null"
  //     );
  //   }

  //   System.out.printf("\n\n%s\n\n", table.copy(flipped).toString());

  //   // store1.dump();
  //   // store2.dump();
  // }
}
