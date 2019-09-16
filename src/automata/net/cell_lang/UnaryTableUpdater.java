package net.cell_lang;

import java.util.Arrays;


class UnaryTableUpdater {
  static int[] emptyArray = new int[0];

  boolean clear = false;
  long[] bitmapCopy = null;

  int deleteCount = 0;
  int[] deleteList = emptyArray;

  int insertCount = 0;
  int[] insertList = emptyArray;

  boolean prepared = false;

  String relvarName;
  UnaryTable table;
  ValueStoreUpdater store;


  public UnaryTableUpdater(String relvarName, UnaryTable table, ValueStoreUpdater store) {
    this.relvarName = relvarName;
    this.table = table;
    this.store = store;
  }

  public void clear() {
    clear = true;
    deleteCount = 0;
  }

  public void delete(long value) {
    if (!clear || table.contains((int) value))
      deleteList = Array.append(deleteList, deleteCount++, (int) value);
  }

  public void insert(long value) {
    insertList = Array.append(insertList, insertCount++, (int) value);
  }

  public void apply() {
    if (clear) {
      int max = 0;
      for (int i=0 ; i < insertCount ; i++) {
        int surr = insertList[i];
        if (surr > max)
          max = surr;
      }
      bitmapCopy = table.clear(max + 1);
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (table.contains(surr))
          table.delete(surr);
        else
          deleteList[i] = 0xFFFFFFFF;
      }
    }

    for (int i=0 ; i < insertCount ; i++) {
      int surr = insertList[i];
      if (!table.contains(surr)) {
        table.insert(surr);
        store.addRef(surr);
      }
    }
  }

  public void finish() {
    if (clear) {
      int len = bitmapCopy.length;
      for (int i=0 ; i < len ; i++) {
        long mask = bitmapCopy[i];
        int base = 64 * i;
        for (int j=0 ; j < 64 ; j++)
          if (((mask >>> j) & 1) != 0)
            store.release(base + j);
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (surr != 0xFFFFFFFF)
          store.release(surr);
      }
    }
  }

  public void reset() {
    clear = false;
    bitmapCopy = null;

    deleteCount = 0;
    insertCount = 0;

    if (deleteList.length > 1024)
      deleteList = emptyArray;
    if (insertList.length > 1024)
      insertList = emptyArray;

    prepared = false;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void prepare() {
    if (!prepared) {
      prepared = true;
      Arrays.sort(deleteList, 0, deleteCount);
      Arrays.sort(insertList, 0, insertCount);
    }
  }

  public boolean contains(int surr) {
    prepare();

    if (Arrays.binarySearch(insertList, 0, insertCount, surr) >= 0)
      return true;

    if (clear || Arrays.binarySearch(deleteList, 0, deleteCount, surr) >= 0)
      return false;

    return table.contains(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  public interface DeleteChecker {
    void check(UnaryTableUpdater updater, int surr);
  }

  public void checkDeletedKeys(DeleteChecker deleteChecker) {
    prepare();

    if (clear) {
      UnaryTable.Iter it = table.getIter();
      while (!it.done()) {
        int surr = it.get();
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deleteChecker.check(this, surr);
        it.next();
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deleteChecker.check(this, surr);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  // unary_rel_1(x) -> unary_rel_2(x);
  public void checkForeignKeys(UnaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[i]))
        throw toUnaryForeignKeyViolation(insertList[i], target);
    target.checkDeletedKeys(deleteChecker);
  }

  DeleteChecker deleteChecker =
    new DeleteChecker() {
      public void check(UnaryTableUpdater target, int surr) {
        if (contains(surr))
          throw toUnaryForeignKeyViolation(surr, target);
      }
    };

  // unary_rel(x) -> binary_rel(x, _);
  public void checkForeignKeys_1(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains1(insertList[i]))
        throw toBinaryForeingKeyViolation1(insertList[i], target);
    target.checkDeletes12(binaryTableDeleteChecker1);
  }

  BinaryTableUpdater.DeleteChecker binaryTableDeleteChecker1 =
    new BinaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, BinaryTableUpdater target) {
        if (contains(surr1) && !target.contains1(surr1))
          throw toBinaryForeingKeyViolation1(surr1, surr2, target);
      }
    };

  // unary_rel(x) -> binary_rel(_, x);
  public void checkForeignKeys_2(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        throw toBinaryForeingKeyViolation2(insertList[i], target);
    target.checkDeletes21(binaryTableDeleteChecker2);
  }

  BinaryTableUpdater.DeleteChecker binaryTableDeleteChecker2 =
    new BinaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, BinaryTableUpdater target) {
        if (contains(surr2) && !target.contains2(surr2))
          throw toBinaryForeingKeyViolation2(surr1, surr2, target);
      }
    };

  // unary_rel(x) -> ternary_rel(x, _, _)
  public void checkForeignKeys_1(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains1(insertList[i]))
        throw toTernaryForeignKeyViolation1(insertList[i], target);
    target.checkDeletes123(ternaryTableDeleteChecker1);
  }

  TernaryTableUpdater.DeleteChecker ternaryTableDeleteChecker1 =
    new TernaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
        if (contains(surr1) && !target.contains1(surr1))
          throw toTernaryForeingKeyViolation1(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> ternary_rel(, x, _)
  public void checkForeignKeys_2(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        throw toTernaryForeignKeyViolation2(insertList[i], target);
    target.checkDeletes231(ternaryTableDeleteChecker2);
  }

  TernaryTableUpdater.DeleteChecker ternaryTableDeleteChecker2 =
    new TernaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
        if (contains(surr2) && !target.contains2(surr2))
          throw toTernaryForeingKeyViolation2(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> ternary_rel(_, _, x)
  public void checkForeignKeys_3(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains3(insertList[i]))
        throw toTernaryForeignKeyViolation3(insertList[i], target);
    target.checkDeletes312(ternaryTableDeleteChecker3);
  }

  TernaryTableUpdater.DeleteChecker ternaryTableDeleteChecker3 =
    new TernaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
        if (contains(surr3) && !target.contains3(surr3))
          throw toTernaryForeingKeyViolation3(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> sym_binary_rel(x, _) | sym_binary_rel(_, x)
  public void checkForeignKeys(SymBinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[i]))
        throw toSymBinaryForeingKeyViolation(insertList[i], target);
    target.checkDeletes(symBinaryTableDeleteChecker);
  }

  SymBinaryTableUpdater.DeleteChecker symBinaryTableDeleteChecker =
    new SymBinaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, SymBinaryTableUpdater target) {
        if (contains(surr1) && !target.contains(surr1))
          throw toSymBinaryForeingKeyViolation(surr1, surr2, target);
        if (contains(surr2) && !target.contains(surr2))
          throw toSymBinaryForeingKeyViolation(surr2, surr1, target);
      }
    };

  // unary_rel(x) -> sym_ternary_rel(x, _, _) | sym_ternary_rel(_, x, _)
  public void checkForeignKeys_1_2(Sym12TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains_1_2(insertList[i]))
        throw toSymTernaryForeignKeyViolation12(insertList[i], target);
    target.checkDeletes(symTernaryTableDeleteChecker12);
  }

  Sym12TernaryTableUpdater.DeleteChecker symTernaryTableDeleteChecker12 =
    new Sym12TernaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, int surr3, Sym12TernaryTableUpdater updater) {
        if (contains(surr1) && !updater.contains_1_2(surr1))
          throw toSymTernaryForeignKeyViolation12(surr1, surr2, surr3, updater);
        if (contains(surr2) && !updater.contains_1_2(surr2))
          throw toSymTernaryForeignKeyViolation12(surr2, surr1, surr3, updater);
      }
    };

  // unary_rel(x) -> sym_ternary_rel(_, _, x)
  public void checkForeignKeys_3(Sym12TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains3(insertList[i]))
        throw toSymTernaryForeignKeyViolation3(insertList[i], target);
    target.checkDeletes(symTernaryTableDeleteChecker3);
  }

  Sym12TernaryTableUpdater.DeleteChecker symTernaryTableDeleteChecker3 =
    new Sym12TernaryTableUpdater.DeleteChecker() {
      public void checkDelete(int surr1, int surr2, int surr3, Sym12TernaryTableUpdater updater) {
        if (contains(surr3) && !updater.contains3(surr3))
          throw toSymTernaryForeingKeyViolation3(surr1, surr2, surr3, updater);
      }
    };

  //////////////////////////////////////////////////////////////////////////////

  private ForeignKeyViolationException toUnaryForeignKeyViolation(int surr, UnaryTableUpdater target) {
    return ForeignKeyViolationException.unaryUnary(relvarName, target.relvarName, store.surrToValue(surr));
  }

  private ForeignKeyViolationException toBinaryForeingKeyViolation1(int surr, BinaryTableUpdater target) {
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryBinary(relvarName, 1, target.relvarName, arg);
  }

  private ForeignKeyViolationException toBinaryForeingKeyViolation1(int surr1, int surr2, BinaryTableUpdater target) {
    Miscellanea._assert(store == target.store1);
    Obj arg1 = store.surrToValue(surr1);
    Obj arg2 = target.store2.surrToValue(surr2);
    Obj[] tuple = new Obj[] {arg1, arg2};
    return ForeignKeyViolationException.unaryBinary(relvarName, 1, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toBinaryForeingKeyViolation2(int surr, BinaryTableUpdater target) {
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryBinary(relvarName, 2, target.relvarName, arg);
  }

  private ForeignKeyViolationException toBinaryForeingKeyViolation2(int surr1, int surr2, BinaryTableUpdater target) {
    Miscellanea._assert(store == target.store2);
    Obj arg1 = target.store1.surrToValue(surr1);
    Obj arg2 = store.surrToValue(surr2);
    Obj[] tuple = new Obj[] {arg1, arg2};
    return ForeignKeyViolationException.unaryBinary(relvarName, 2, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toTernaryForeignKeyViolation1(int surr, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store1);
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(relvarName, 1, target.relvarName, arg);
  }

  private ForeignKeyViolationException toTernaryForeingKeyViolation1(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store1);
    Obj arg1 = store.surrToValue(surr1);
    Obj arg2 = target.store2.surrToValue(surr2);
    Obj arg3 = target.store3.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(relvarName, 1, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toTernaryForeignKeyViolation2(int surr, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store2);
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(relvarName, 2, target.relvarName, arg);
  }

  private ForeignKeyViolationException toTernaryForeingKeyViolation2(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store2);
    Obj arg1 = target.store1.surrToValue(surr1);
    Obj arg2 = store.surrToValue(surr2);
    Obj arg3 = target.store3.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(relvarName, 2, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toTernaryForeignKeyViolation3(int surr, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store3);
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(relvarName, 3, target.relvarName, arg);
  }

  private ForeignKeyViolationException toTernaryForeingKeyViolation3(int surr1, int surr2, int surr3, TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store3);
    Obj arg1 = target.store1.surrToValue(surr1);
    Obj arg2 = target.store2.surrToValue(surr2);
    Obj arg3 = store.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(relvarName, 3, target.relvarName, tuple);
  }

  private ForeignKeyViolationException toSymBinaryForeingKeyViolation(int surr, SymBinaryTableUpdater target) {
    Miscellanea._assert(store == target.store);
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unarySymBinary(relvarName, target.relvarName, arg);
  }

  private ForeignKeyViolationException toSymBinaryForeingKeyViolation(int surr, int otherSurr, SymBinaryTableUpdater target) {
    Miscellanea._assert(store == target.store);
    Obj arg = store.surrToValue(surr);
    Obj otherArg = store.surrToValue(otherSurr);
    return ForeignKeyViolationException.unarySymBinary(relvarName, target.relvarName, arg, otherArg);
  }

  private ForeignKeyViolationException toSymTernaryForeignKeyViolation12(int surr, Sym12TernaryTableUpdater target) {
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unarySym12Ternary(relvarName, target.relvarName, arg);
  }

  private ForeignKeyViolationException toSymTernaryForeignKeyViolation12(int delSurr12, int otherSurr12, int surr3, Sym12TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store12);
    Obj delArg12 = store.surrToValue(delSurr12);
    Obj otherArg12 = store.surrToValue(otherSurr12);
    Obj arg3 = store.surrToValue(surr3);
    return ForeignKeyViolationException.unarySym12Ternary(relvarName, target.relvarName, delArg12, otherArg12, arg3);
  }

  private ForeignKeyViolationException toSymTernaryForeignKeyViolation3(int surr, Sym12TernaryTableUpdater target) {
    Obj arg = store.surrToValue(surr);
    return ForeignKeyViolationException.unaryTernary(relvarName, 3, target.relvarName, arg);
  }

  private ForeignKeyViolationException toSymTernaryForeingKeyViolation3(int surr1, int surr2, int surr3, Sym12TernaryTableUpdater target) {
    Miscellanea._assert(store == target.store3);
    Obj arg1 = target.store12.surrToValue(surr1);
    Obj arg2 = target.store12.surrToValue(surr2);
    Obj arg3 = store.surrToValue(surr3);
    Obj[] tuple = new Obj[] {arg1, arg2, arg3};
    return ForeignKeyViolationException.unaryTernary(relvarName, 3, target.relvarName, tuple);
  }
}
