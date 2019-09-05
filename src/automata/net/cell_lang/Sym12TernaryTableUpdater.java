package net.cell_lang;

import java.util.function.IntPredicate;


class Sym12TernaryTableUpdater {
  static int[] emptyArray = new int[0];

  // boolean clear = false;

  int deleteCount = 0;
  int[] deleteList = emptyArray;
  int[] deleteList3;

  int insertCount = 0;
  int[] insertList = emptyArray;
  int[] insertList12;
  int[] insertList3;

  String relvarName;

  Sym12TernaryTable table;
  ValueStoreUpdater store12, store3;

  boolean prepared = false;

  public Sym12TernaryTableUpdater(String relvarName, Sym12TernaryTable table, ValueStoreUpdater store12, ValueStoreUpdater store3) {
    this.relvarName = relvarName;
    this.table = table;
    this.store12 = store12;
    this.store3 = store3;
  }

  public void clear() {
    deleteCount = 0;
    Sym12TernaryTable.Iter123 it = table.getIter();
    while (!it.done()) {
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), it.get2(), it.get3());
      it.next();
    }
  }

  public void insert(int value1, int value2, int value3) {
    if (value1 > value2) {
      int tmp = value1;
      value1 = value2;
      value2 = tmp;
    }
    insertList = Array.append3(insertList, insertCount++, value1, value2, value3);
  }

  public void delete(int value1, int value2, int value3) {
    if (value1 > value2) {
      int tmp = value1;
      value1 = value2;
      value2 = tmp;
    }
    if (table.contains(value1, value2, value3))
      deleteList = Array.append3(deleteList, deleteCount++, value1, value2, value3);
  }

  public void delete12(int value1, int value2) {
    if (value1 > value2) {
      int tmp = value1;
      value1 = value2;
      value2 = tmp;
    }
    Sym12TernaryTable.Iter12 it = table.getIter12(value1, value2);
    while (!it.done()) {
      deleteList = Array.append3(deleteList, deleteCount++, value1, value2, it.get1());
      it.next();
    }
  }

  public void delete_13_23(int arg12, int arg3) {
    Sym12TernaryTable.Iter it = table.getIter_13_23(arg12, arg3);
    while (!it.done()) {
      int arg1 = arg12;
      int arg2 = it.get1();
      if (arg1 > arg2) {
        arg1 = arg2;
        arg2 = arg12;
      }
      deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
      it.next();
    }
  }

  public void delete_1_2(int arg12) {
    Sym12TernaryTable.Iter it = table.getIter_1_2(arg12);
    while (!it.done()) {
      int arg1 = arg12;
      int arg2 = it.get1();
      int arg3 = it.get2();
      if (arg1 > arg2) {
        arg1 = arg2;
        arg2 = arg12;
      }
      deleteList = Array.append3(deleteList, deleteCount++, arg1, arg2, arg3);
      it.next();
    }
  }

  public void delete3(int value3) {
    Sym12TernaryTable.Iter3 it = table.getIter3(value3);
    while (!it.done()) {
      Miscellanea._assert(it.get1() <= it.get2());
      deleteList = Array.append3(deleteList, deleteCount++, it.get1(), it.get2(), value3);
      it.next();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void apply() {
    for (int i=0 ; i < deleteCount ; i++) {
      int arg1 = deleteList[3 * i];
      int arg2 = deleteList[3 * i + 1];
      int arg3 = deleteList[3 * i + 2];
      if (table.contains(arg1, arg2, arg3))
        table.delete(arg1, arg2, arg3);
      else
        deleteList[3 * i] = 0xFFFFFFFF;
    }

    for (int i=0 ; i < insertCount ; i++) {
      int arg1 = insertList[3 * i];
      int arg2 = insertList[3 * i + 1];
      int arg3 = insertList[3 * i + 2];

      if (!table.contains(arg1, arg2, arg3)) {
        table.insert(arg1, arg2, arg3);
        store12.addRef(arg1);
        store12.addRef(arg2);
        store3.addRef(arg3);
      }
    }
  }

  public void finish() {
    for (int i=0 ; i < deleteCount ; i++) {
      int arg1 = deleteList[3 * i];
      if (arg1 != 0xFFFFFFFF) {
        int arg2 = deleteList[3 * i + 1];
        int arg3 = deleteList[3 * i + 2];
        store12.release(arg1);
        store12.release(arg2);
        store3.release(arg3);
      }
    }
  }

  public void reset() {
    // clear = false;
    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 3 * 1024)
      deleteList = emptyArray;
    if (insertList.length > 3 * 1024)
      insertList = emptyArray;

    deleteList3 = null;
    insertList12 = null;
    insertList3 = null;

    prepared = false;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private void prepare() {
    if (!prepared) {
      for (int i=0 ; i < deleteCount ; i++) {
        Miscellanea._assert(deleteList[3 * i] <= deleteList[3 * i + 1]);
      }
      Ints123.sort(deleteList, deleteCount);
      Ints123.sort(insertList, insertCount);
      prepared = true;
    }
  }

  private void prepareDelete3() {
    if (deleteList3 == null)
      if (deleteCount > 0) {
        deleteList3 = new int[deleteCount];
        for (int i=0 ; i < deleteCount ; i++)
          deleteList3[i] = deleteList[3 * i + 2];
        Ints.sort(deleteList3);
      }
      else
        deleteList3 = emptyArray;
  }

  private void prepareInsert12() {
    if (insertList12 == null)
      if (insertCount > 0) {
        insertList12 = new int[2 * insertCount];
        for (int i=0 ; i < insertCount ; i++) {
          insertList12[2 * i] = insertList[3 * i];
          insertList12[2 * i + 1] = insertList[3 * i + 1];
        }
        Ints.sort(insertList12);
      }
      else
        insertList12 = emptyArray;
  }

  private void prepareInsert3() {
    if (insertList3 == null)
      if (insertCount > 0) {
        insertList3 = new int[insertCount];
        for (int i=0 ; i < insertCount ; i++)
          insertList3[i] = insertList[3 * i + 2];
        Ints.sort(insertList3);
      }
      else
        insertList3 = emptyArray;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean contains12(int surr1, int surr2) {
    if (surr1 > surr2) {
      int tmp = surr1;
      surr1 = surr2;
      surr2 = tmp;
    }

    prepare();
    if (Ints123.contains12(insertList, insertCount, surr1, surr2))
      return true;

    if (table.contains12(surr1, surr2)) {
      Sym12TernaryTable.Iter12 it = table.getIter12(surr1, surr2);
      Miscellanea._assert(!it.done());
      do {
        if (!Ints123.contains(deleteList, deleteCount, surr1, surr2, it.get1()))
          return true;
        it.next();
      } while (!it.done());
    }

    return false;
  }

  public boolean contains_1_2(int arg12) {
    prepareInsert12();
    if (Ints.contains(insertList12, arg12))
      return true;

    if (table.contains_1_2(arg12)) {
      //## THIS COULD BE MADE FASTER BY CHECKING FIRST WHETHER arg12 APPEARS IN THE DELETE LIST AT ALL
      prepare();
      Sym12TernaryTable.Iter it = table.getIter_1_2(arg12);
      Miscellanea._assert(!it.done());
      do {
        int arg1 = arg12;
        int arg2 = it.get1();
        int arg3 = it.get2();
        if (arg1 >= arg2) {
          arg1 = arg2;
          arg2 = arg12;
        }
        if (!Ints123.contains(deleteList, deleteCount, arg1, arg2, arg3))
          return true;
        it.next();
      } while (!it.done());
    }

    return false;
  }

  public boolean contains3(int surr3) {
    prepareInsert3();
    if (Ints.contains(insertList3, surr3))
      return true;

    if (table.contains3(surr3)) {
      //## THIS COULD BE MADE FASTER BY CHECKING FIRST WHETHER surr3 APPEARS IN THE DELETE LIST AT ALL
      prepare();
      Sym12TernaryTable.Iter3 it = table.getIter3(surr3);
      Miscellanea._assert(!it.done());
      do {
        if (!Ints123.contains(deleteList, deleteCount, it.get1(), it.get2(), surr3))
          return true;
        it.next();
      } while (!it.done());
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void checkKey_12() {
    if (insertCount != 0) {
      prepare();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg1 == prevArg1 & arg2 == prevArg2 & arg3 != prevArg3)
          throw cols12KeyViolationException(arg1, arg2, arg3, prevArg3);

        if (!Ints123.contains12(deleteList, deleteCount, arg1, arg2) && table.contains12(arg1, arg2))
          throw cols12KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  public void checkKey_3() {
    if (insertCount != 0) {
      prepareDelete3();

      int prevArg1 = -1;
      int prevArg2 = -1;
      int prevArg3 = -1;

      for (int i=0 ; i < insertCount ; i++) {
        int arg1 = insertList[3 * i];
        int arg2 = insertList[3 * i + 1];
        int arg3 = insertList[3 * i + 2];

        if (arg3 == prevArg3 & (arg1 != prevArg1 | arg2 != prevArg2))
          throw col3KeyViolationException(arg1, arg2, arg3, prevArg1, prevArg2);

        if (!Ints.contains(deleteList, arg3) && table.contains3(arg3))
          throw col3KeyViolationException(arg1, arg2, arg3);

        prevArg1 = arg1;
        prevArg2 = arg2;
        prevArg3 = arg3;
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean checkDeletedKeys_12(BiIntPredicate source) {
    prepare();

    for (int i=0 ; i < deleteCount ; ) {
      int offset = 3 * i;
      int surr1 = deleteList[offset];
      int surr2 = deleteList[offset + 1];
      if (!Ints123.contains12(insertList, insertCount, surr1, surr2) && source.test(surr1, surr2)) {
        int surr3 = deleteList[3 * i + 2];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i] == surr1 && deleteList[3*i+1] == surr2 ; i++) {
          int currSurr3 = deleteList[3 * i + 2];
          if (currSurr3 != surr3) {
            surr3 = currSurr3;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count12Eq(surr1, surr2, removedCount))
          return false;
      }
      else
        i++;
    }
    return true;
  }

  public boolean checkDeletedKeys_3(IntPredicate source) {
    prepare();
    prepareInsert3();

    for (int i=0 ; i < deleteCount ; ) {
      int surr3 = deleteList[3 * i + 2];
      if (!Ints.contains(insertList3, surr3) && source.test(surr3)) {
        int surr1 = deleteList[3 * i];
        int surr2 = deleteList[3 * i + 1];
        int removedCount = table.contains(surr1, surr2, surr3) ? 1 : 0;
        for (i++ ; i < deleteCount && deleteList[3*i+2] == surr3 ; i++) {
          int currSurr1 = deleteList[3 * i];
          int currSurr2 = deleteList[3 * i + 1];
          if (currSurr1 != surr1 | currSurr2 != surr2) {
            surr1 = currSurr1;
            surr2 = currSurr2;
            if (table.contains(surr1, surr2, surr3))
              removedCount++;
          }
        }
        if (table.count3Eq(surr3, removedCount))
          return false;
      }
      else
        i++;
    }

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // tern_rel(a, b, _) -> unary_rel(a), unary_rel(b)
  public void checkForeignKeys_1_2(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++) {
      if (!target.contains(insertList[3*i]) | !target.contains(insertList[3*i+1]))
        throw toUnaryForeignKeyViolation12(insertList[3*i], insertList[3*i+1], insertList[3*i+2], target);
    }

    // Checking that no entries were invalidates by a deletion on the target table
    target.checkDeletedKeys(deletabilityChecker12);
  }

  UnaryTableUpdater.DeletabilityChecker deletabilityChecker12 =
    new UnaryTableUpdater.DeletabilityChecker() {
      public void check(UnaryTableUpdater target, int surr12) {
        if (contains_1_2(surr12))
          throw toUnaryForeignKeyViolation12(surr12, target);
      }
    };

  // tern_rel(_, _, c) -> unary_rel(c)
  public void checkForeignKeys_3(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i+2]))
        throw toUnaryForeignKeyViolation3(insertList[3*i], insertList[3*i+1], insertList[3*i+2], target);

    // Checking that no entries were invalidated by a deletion on the target table
    target.checkDeletedKeys(deletabilityChecker3);
  }

  UnaryTableUpdater.DeletabilityChecker deletabilityChecker3 =
    new UnaryTableUpdater.DeletabilityChecker() {
      public void check(UnaryTableUpdater target, int surr3) {
        if (contains3(surr3))
          throw toUnaryForeignKeyViolation3(surr3, target);
      }
    };

  // tern_rel(a, b, _) -> binary_rel(a, b)
  public boolean checkForeignKeys_12(SymBinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[3*i], insertList[3*i+1]))
        return false;
    return target.checkDeletedKeys(this::contains12);
  }

  //////////////////////////////////////////////////////////////////////////////

  private KeyViolationException cols12KeyViolationException(int arg1, int arg2, int arg3, int otherArg3) {
    return keyViolationException(arg1, arg2, arg3, arg1, arg2, otherArg3, KeyViolationException.key_12, true);
  }

  private KeyViolationException cols12KeyViolationException(int arg1, int arg2, int arg3) {
    int otherArg3 = table.lookup12(arg1, arg2);
    return keyViolationException(arg1, arg2, arg3, arg1, arg2, otherArg3, KeyViolationException.key_12, false);
  }

  private KeyViolationException col3KeyViolationException(int arg1, int arg2, int arg3, int otherArg1, int otherArg2) {
    return keyViolationException(arg1, arg2, arg3, otherArg1, otherArg2, arg3, KeyViolationException.key_3, true);
  }

  private KeyViolationException col3KeyViolationException(int arg1, int arg2, int arg3) {
    Sym12TernaryTable.Iter3 it = table.getIter3(arg3);
    int otherArg1 = it.get1();
    int otherArg2 = it.get2();
    return keyViolationException(arg1, arg2, arg3, otherArg1, otherArg2, arg3, KeyViolationException.key_3, false);
  }

  private KeyViolationException keyViolationException(int arg1, int arg2, int arg3, int otherArg1, int otherArg2, int otherArg3, int[] key, boolean betweenNew) {
    //## BUG: STORES MAY CONTAIN ONLY PART OF THE ACTUAL VALUE (id(5) -> 5)
    Obj obj1 = store12.surrToValue(arg1);
    Obj obj2 = store12.surrToValue(arg2);
    Obj obj3 = store3.surrToValue(arg3);

    Obj otherObj1 = arg1 == otherArg1 ? obj1 : store12.surrToValue(otherArg1);
    Obj otherObj2 = arg2 == otherArg2 ? obj2 : store12.surrToValue(otherArg2);
    Obj otherObj3 = arg3 == otherArg3 ? obj3 : store3.surrToValue(otherArg3);

    Obj[] tuple1 = new Obj[] {obj1, obj2, obj3};
    Obj[] tuple2 = new Obj[] {otherObj1, otherObj2, otherObj3};

    return new KeyViolationException(relvarName, key, tuple1, tuple2, betweenNew);
  }

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException toUnaryForeignKeyViolation12(int arg1Surr, int arg2Surr, int arg3Surr, UnaryTableUpdater target) {
    Obj[] tuple = new Obj[] {store12.surrToValue(arg1Surr), store12.surrToValue(arg2Surr), store3.surrToValue(arg3Surr)};
    return ForeignKeyViolationException.symTernary12Unary(relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toUnaryForeignKeyViolation12(int arg12Surr, UnaryTableUpdater target) {
    Sym12TernaryTable.Iter it = table.getIter_1_2(arg12Surr);
    Obj arg1 = store12.surrToValue(arg12Surr);
    Obj arg2 = store12.surrToValue(it.get1());
    Obj arg3 = store3.surrToValue(it.get2());
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.symTernary12Unary(relvarName, target.relvarName, tuple, arg1);
  }

  private ForeignKeyViolationException toUnaryForeignKeyViolation3(int arg1Surr, int arg2Surr, int arg3Surr, UnaryTableUpdater target) {
    Obj[] tuple = new Obj[] {store12.surrToValue(arg1Surr), store12.surrToValue(arg2Surr), store3.surrToValue(arg3Surr)};
    return ForeignKeyViolationException.symTernary3Unary(relvarName, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toUnaryForeignKeyViolation3(int arg3Surr, UnaryTableUpdater target) {
    Sym12TernaryTable.Iter3 it = table.getIter3(arg3Surr);
    Obj arg1 = store12.surrToValue(it.get1());
    Obj arg2 = store12.surrToValue(it.get2());
    Obj arg3 = store3.surrToValue(arg3Surr);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.symTernary3Unary(relvarName, target.relvarName, tuple, arg3);
  }
}
