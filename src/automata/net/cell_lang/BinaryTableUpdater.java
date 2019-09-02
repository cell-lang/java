package net.cell_lang;

import java.util.function.IntPredicate;


class BinaryTableUpdater extends BinRelUpdateErrorFactory {
  int deleteCount = 0;
  int[] deleteList = Array.emptyIntArray;

  int insertCount = 0;
  int[] insertList = Array.emptyIntArray;

  int updateCount = 0;
  int[] updateList = Array.emptyIntArray;

  enum Ord {ORD_NONE, ORD_12, ORD_21};
  Ord currOrd = Ord.ORD_NONE;

  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  public BinaryTableUpdater(String relvarName, BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    //## BUG: Stores may contain only part of the value (id(5) -> 5)
    super(relvarName, table::restrict1, table::restrict2, store1::surrToValue, store2::surrToValue);
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  public void clear() {
    deleteList = table.rawCopy();
    deleteCount = deleteList.length / 2;
  }

  public void delete(int arg1, int arg2) {
    if (table.contains(arg1, arg2))
      deleteList = Array.append2(deleteList, deleteCount++, arg1, arg2);
  }

  public void delete1(int value) {
    int[] assocs = table.restrict1((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList = Array.append2(deleteList, deleteCount++, value, assocs[i]);
  }

  public void delete2(int value) {
    int[] assocs = table.restrict2((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList = Array.append2(deleteList, deleteCount++, assocs[i], value);
  }

  public void insert(int arg1, int arg2) {
    insertList = Array.append2(insertList, insertCount++, arg1, arg2);
  }

  public void update(int arg1, int arg2) {
    updateList = Array.append2(updateList, updateCount++, arg1, arg2);
  }

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      int arg1 = deleteList[2 * i];
      int arg2 = deleteList[2 * i + 1];
      if (table.contains(arg1, arg2))
        table.delete(arg1, arg2);
      else
        deleteList[2 * i] = 0xFFFFFFFF;
    }

    int releaseCount = 0;
    for (int i=0 ; i < updateCount ; i++) {
      int arg1 = updateList[2 * i];
      int arg2 = updateList[2 * i + 1];

      int oldArg2 = table.update1(arg1, arg2);

      if (oldArg2 == -1) {
        store1.addRef(arg1);
        store2.addRef(arg2);
      }
      else if (arg2 != oldArg2) {
        store2.addRef(arg2);
        // Storing the value of the old surrogate so that it can be released later
        updateList[releaseCount++] = oldArg2;
      }
    }
    if (updateList.length != 0)
      updateList[releaseCount] = -1;

    for (int i=0 ; i < insertCount ; i++) {
      int arg1 = insertList[2 * i];
      int arg2 = insertList[2 * i + 1];
      if (!table.contains(arg1, arg2)) {
        table.insert(arg1, arg2);
        store1.addRef(arg1);
        store2.addRef(arg2);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int arg1 = deleteList[2 * i];
      if (arg1 != 0xFFFFFFFF) {
        int arg2 = deleteList[2 * i + 1];
        // Miscellanea._assert(table.store1.surrToObjValue(arg1) != null);
        // Miscellanea._assert(table.store2.surrToObjValue(arg2) != null);
        store1.release(arg1);
        store2.release(arg2);
      }
    }

    if (updateCount != 0) {
      for (int i=0 ; ; i++) {
        int arg2 = updateList[i];
        if (arg2 == -1)
          break;
        // Miscellanea._assert(table.store2.surrToObjValue(arg2) != null);
        store2.release(arg2);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    deleteCount = 0;
    insertCount = 0;
    updateCount = 0;

    if (deleteList.length > 2 * 1024)
      deleteList = Array.emptyIntArray;
    if (insertList.length > 2 * 1024)
      insertList = Array.emptyIntArray;
    if (updateList.length > 2 * 1024)
      updateList = Array.emptyIntArray;

    currOrd = Ord.ORD_NONE;
  }

  public void prepare12() {
    Miscellanea._assert(currOrd != Ord.ORD_21);
    if (currOrd != Ord.ORD_12) {
      Ints12.sort(deleteList, deleteCount);
      Ints12.sort(insertList, insertCount);
      currOrd = Ord.ORD_12;
    }
  }

  public void prepare21() {
    if (currOrd != Ord.ORD_21) {
      Ints21.sort(deleteList, deleteCount);
      Ints21.sort(insertList, insertCount);
      currOrd = Ord.ORD_21;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains(int surr1, int surr2) {
    prepare12();

    if (Ints12.contains(insertList, insertCount, surr1, surr2))
      return true;

    if (Ints12.contains(deleteList, deleteCount, surr1, surr2))
      return false;

    return table.contains(surr1, surr2);
  }

  public boolean contains1(int surr1) {
    prepare12();

    if (Ints12.contains1(insertList, insertCount, surr1))
      return true;

    if (!table.contains1(surr1))
      return false;

    int idx = Ints12.indexFirst1(deleteList, deleteCount, surr1);
    if (idx == -1)
      return true;
    int count = Ints12.count1(deleteList, deleteCount, surr1, idx);

    //## BAD: THIS IS VERY INEFFICIENT IF THERE'S A LOT OF ENTRIES WHOSE FIRST ARGUMENT IS surr1
    int[] vals2 = table.restrict1(surr1);

    for (int i=0 ; i < vals2.length ; i++)
      // Elements in range [idx, idx+count) are sorted in both orders,
      // since the left value is always the same
      if (!Ints21.contains2(deleteList, idx, count, vals2[i]))
        return true;

    return false;
  }

  public boolean contains2(int surr2) {
    prepare21();

    if (Ints21.contains2(insertList, insertCount, surr2))
      return true;

    if (!table.contains2(surr2))
      return false;

    int idx = Ints21.indexFirst2(deleteList, deleteCount, surr2);
    if (idx == -1)
      return true;
    int count = Ints21.count2(deleteList, deleteCount, surr2, idx);

    //## BAD: THIS IS VERY INEFFICIENT IF THERE'S A LOT OF ENTRIES WHOSE SECOND ARGUMENT IS surr2
    int[] vals1 = table.restrict2(surr2);

    for (int i=0 ; i < vals1.length ; i++)
      // Elements in range [idx, idx+count) are sorted in both orders,
      // since the right value is always the same
      if (!Ints12.contains1(deleteList, idx, count, vals1[i]))
        return true;

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_1() {
    if (insertCount != 0) {
      prepare12();

      int prev1 = -1;
      int prev2 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int curr1 = insertList[2 * i];
        int curr2 = insertList[2 * i + 1];

        if (curr1 == prev1 & curr2 != prev2)
          throw col1KeyViolation(curr1, curr2, prev2);

        if (!Ints12.contains1(deleteList, deleteCount, curr1) && table.contains1(curr1))
          throw col1KeyViolation(curr1, curr2);

        prev1 = curr1;
        prev2 = curr2;
      }
    }
  }

  public void checkKey_2() {
    if (insertCount != 0) {
      prepare21();

      int prev1 = -1;
      int prev2 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int curr1 = insertList[2 * i];
        int curr2 = insertList[2 * i + 1];

        if (curr2 == prev2 & curr1 != prev1)
          throw col2KeyViolation(curr1, curr2, prev1);


        if (!Ints21.contains2(deleteList, deleteCount, curr2) && table.contains2(curr2))
          throw col2KeyViolation(curr1, curr2);

        prev1 = curr1;
        prev2 = curr2;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean checkDeletedKeys_1(IntPredicate source) {
    prepare12();

    for (int i=0 ; i < deleteCount ; ) {
      int surr1 = deleteList[2 * i];
      if (!Ints12.contains1(insertList, insertCount, surr1) && source.test(surr1)) {
        int count = table.count1(surr1);
        Miscellanea._assert(count > 0);

        int surr2 = deleteList[2 * i + 1];
        int deleteCount1 = table.contains(surr1, surr2) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[2*i] == surr1 ; i++) {
          int currSurr2 = deleteList[2*i+1];
          if (currSurr2 != surr2) {
            surr2 = currSurr2;
            if (table.contains(surr1, surr2))
              deleteCount1++;
          }
        }

        Miscellanea._assert(deleteCount1 <= count);
        if (deleteCount1 == count)
          return false;
      }
      else
        i++;
    }

    return true;
  }

  public boolean checkDeletedKeys_2(IntPredicate source) {
    prepare21();

    for (int i=0 ; i < deleteCount ; ) {
      int surr2 = deleteList[2 * i + 1];
      if (!Ints21.contains2(insertList, insertCount, surr2) && source.test(surr2)) {
        int count = table.count2(surr2);
        Miscellanea._assert(count > 0);

        int surr1 = deleteList[2 * i];
        int deleteCount2 = table.contains(surr1, surr2) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[2*i+1] == surr2 ; i++) {
          int currSurr1 = deleteList[2 * i];
          if (currSurr1 != surr1) {
            surr1 = currSurr1;
            if (table.contains(surr1, surr2))
              deleteCount2++;
          }
        }

        if (deleteCount2 == count)
          return false;
      }
      else
        i++;
    }

    return true;
  }

  public boolean checkDeletedKeys_12(BiIntPredicate source) {
    prepare12();

    for (int i=0 ; i < deleteCount ; i++) {
      int surr1 = deleteList[2 * i];
      int surr2 = deleteList[2 * i + 1];
      if (!Ints12.contains(insertList, insertCount, surr1, surr2))
        if (source.test(surr1, surr2))
          return false;
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  // bin_rel(a, _) -> unary_rel(a);
  public boolean checkForeignKeys_1(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[2*i]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains1);
  }

  // bin_rel(_, b) -> unary_rel(b);
  public boolean checkForeignKeys_2(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[2*i+1]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains2);
  }

  // bin_rel(a, b) -> ternary_rel(a, b, _)
  public boolean checkForeignKeys_12(TernaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains12(insertList[2*i], insertList[2*i+1]))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys_12(this::contains);
  }

  //////////////////////////////////////////////////////////////////////////////

  public void dump(boolean flipped) {
    System.out.print("deleteList =");
    for (int i=0 ; i < deleteCount ; i++)
      if (flipped)
        System.out.printf(" (%d, %d)", deleteList[2 * i + 1], deleteList[2 * i]);
      else
        System.out.printf(" (%d, %d)", deleteList[2 * i], deleteList[2 * i + 1]);
    System.out.println();

    System.out.print("insertList =");
    for (int i=0 ; i < insertCount ; i++)
      if (flipped)
        System.out.printf(" (%d, %d)", insertList[2 * i + 1], insertList[2 * i]);
      else
        System.out.printf(" (%d, %d)", insertList[2 * i], insertList[2 * i + 1]);
    System.out.println("\n");

    System.out.print("deleteList =");
    for (int i=0 ; i < deleteCount ; i++) {
      int arg1 = deleteList[2 * i];
      int arg2 = deleteList[2 * i + 1];
      Obj obj1 = store1.surrToValue(arg1);
      Obj obj2 = store2.surrToValue(arg2);
      if (flipped) {
        Obj tmp = obj1;
        obj1 = obj2;
        obj2 = tmp;
      }
      System.out.printf(" (%s, %s)", obj1.toString(), obj2.toString());
    }
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertCount ; i++) {
      int arg1 = insertList[2 * i];
      int arg2 = insertList[2 * i + 1];
      Obj obj1 = store1.surrToValue(arg1);
      Obj obj2 = store2.surrToValue(arg2);
      if (flipped) {
        Obj tmp = obj1;
        obj1 = obj2;
        obj2 = tmp;
      }
      System.out.printf(" (%s, %s)",
        obj1 != null ? obj1.toString() : "null",
        obj2 != null ? obj2.toString() : "null"
      );
    }

    System.out.printf("\n\n%s\n\n", table.copy(flipped).toString());

    // store1.dump();
    // store2.dump();
  }
}
