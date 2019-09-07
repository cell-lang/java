package net.cell_lang;

import java.util.Arrays;
import java.util.function.IntPredicate;


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

  // public void set(Obj value) {
  //   Miscellanea._assert(deleteCount == 0 & insertCount == 0);
  //   clear();
  //   int size = value.getSize();
  //   insertCount = size;
  //   insertList = new int[size];
  //   Obj[] elts = value.getArray((Obj[]) null);
  //   Miscellanea._assert(elts.length == size);
  //   for (int i=0 ; i < size ; i++) {
  //     Obj val = elts[i];
  //     int surr = store.valueToSurrEx(val);
  //     if (surr == -1)
  //       surr = store.insert(val);
  //     insertList[i] = surr;
  //   }
  // }

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

  public interface DeletabilityChecker {
    void check(UnaryTableUpdater updater, int surr);
  }

  public void checkDeletedKeys(DeletabilityChecker deletabilityChecker) {
    prepare();

    if (clear) {
      UnaryTable.Iter it = table.getIter();
      while (!it.done()) {
        int surr = it.get();
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deletabilityChecker.check(this, surr);
        it.next();
      }
    }
    else {
      for (int i=0 ; i < deleteCount ; i++) {
        int surr = deleteList[i];
        if (Arrays.binarySearch(insertList, 0, insertCount, surr) < 0)
          deletabilityChecker.check(this, surr);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  // unary_rel_1(x) -> unary_rel_2(x);
  public void checkForeignKeys(UnaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[i]))
        throw toUnaryForeignKeyViolation(insertList[i], target);
    target.checkDeletedKeys(deletabilityChecker);
  }

  DeletabilityChecker deletabilityChecker =
    new DeletabilityChecker() {
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
    target.checkDeletedKeys_1(binaryTableDeletabilityChecker1);
  }

  BinaryTableUpdater.DeletabilityChecker binaryTableDeletabilityChecker1 =
    new BinaryTableUpdater.DeletabilityChecker() {
      public boolean isLive(int surr) {
        return contains(surr);
      }

      public void onViolation(BinaryTableUpdater target, int surr1, int surr2) {
        throw toBinaryForeingKeyViolation1(surr1, surr2, target);
      }
    };

  // unary_rel(x) -> binary_rel(_, x);
  public void checkForeignKeys_2(BinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        throw toBinaryForeingKeyViolation2(insertList[i], target);
    target.checkDeletedKeys_2(binaryTableDeletabilityChecker2);
  }

  BinaryTableUpdater.DeletabilityChecker binaryTableDeletabilityChecker2 =
    new BinaryTableUpdater.DeletabilityChecker() {
      public boolean isLive(int surr) {
        return contains(surr);
      }

      public void onViolation(BinaryTableUpdater target, int surr1, int surr2) {
        throw toBinaryForeingKeyViolation2(surr1, surr2, target);
      }
    };

  // unary_rel(x) -> ternary_rel(x, _, _)
  public void checkForeignKeys_1(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains1(insertList[i]))
        throw toTernaryForeignKeyViolation1(insertList[i], target);
    target.checkDeletedKeys_1(ternaryTableDeletabilityChecker1);
  }

  TernaryTableUpdater.DeletabilityChecker ternaryTableDeletabilityChecker1 =
    new TernaryTableUpdater.DeletabilityChecker() {
      public boolean isLive(int surr) {
        return contains(surr);
      }

      public void onViolation(TernaryTableUpdater target, int surr1, int surr2, int surr3) {
        throw toTernaryForeingKeyViolation1(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> ternary_rel(, x, _)
  public void checkForeignKeys_2(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains2(insertList[i]))
        throw toTernaryForeignKeyViolation2(insertList[i], target);
    target.checkDeletedKeys_2(ternaryTableDeletabilityChecker2);
  }

  TernaryTableUpdater.DeletabilityChecker ternaryTableDeletabilityChecker2 =
    new TernaryTableUpdater.DeletabilityChecker() {
      public boolean isLive(int surr) {
        return contains(surr);
      }

      public void onViolation(TernaryTableUpdater target, int surr1, int surr2, int surr3) {
        throw toTernaryForeingKeyViolation2(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> ternary_rel(_, _, x)
  public void checkForeignKeys_3(TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains3(insertList[i]))
        throw toTernaryForeignKeyViolation3(insertList[i], target);
    target.checkDeletedKeys_3(ternaryTableDeletabilityChecker3);
  }

  TernaryTableUpdater.DeletabilityChecker ternaryTableDeletabilityChecker3 =
    new TernaryTableUpdater.DeletabilityChecker() {
      public boolean isLive(int surr) {
        return contains(surr);
      }

      public void onViolation(TernaryTableUpdater target, int surr1, int surr2, int surr3) {
        throw toTernaryForeingKeyViolation3(surr1, surr2, surr3, target);
      }
    };

  // unary_rel(x) -> sym_binary_rel(x, _) | sym_binary_rel(_, x)
  public boolean checkForeignKeys(SymBinaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains(insertList[i]))
        return false;
    return target.checkDeletedKeys((a1, a2) -> contains(a1) & contains(a2));
  }

  // unary_rel(x) -> sym_ternary_rel(x, _, _) | sym_ternary_rel(_, x, _)
  public boolean checkForeignKeys_1_2(Sym12TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains_1_2(insertList[i]))
        return false;
    return target.checkDeletedKeys_12((a1, a2) -> contains(a1) & contains(a2));
  }

  // unary_rel(x) -> sym_ternary_rel(_, _, x)
  public boolean checkForeignKeys_3(Sym12TernaryTableUpdater target) {
    for (int i=0 ; i < insertCount ; i++)
      if (!target.contains3(insertList[i]))
        return false;
    return target.checkDeletedKeys_3(this::contains);
  }

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
}
