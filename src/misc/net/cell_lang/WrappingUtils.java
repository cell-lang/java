package net.cell_lang;


class WrappingUtils {
  public static boolean tableContains(UnaryTable table, Obj elem) {
    int surr = table.store.lookupValue(elem);
    if (surr == -1)
      return false;
    return table.contains((int) surr);
  }

  public static boolean tableContains(BinaryTable table, Obj field1, Obj field2) {
    int surr1 = table.store1.lookupValue(field1);
    if (surr1 == -1)
      return false;
    int surr2 = table.store2.lookupValue(field2);
    if (surr2 == -1)
      return false;
    return table.contains((int) surr1, (int) surr2);
  }

  public static boolean tableContains(TernaryTable table, Obj field1, Obj field2, Obj field3) {
    int surr1 = table.store1.lookupValue(field1);
    if (surr1 == -1)
      return false;
    int surr2 = table.store2.lookupValue(field2);
    if (surr2 == -1)
      return false;
    int surr3 = table.store3.lookupValue(field3);
    if (surr3 == -1)
      return false;
    return table.contains((int) surr1, (int) surr2, (int) surr3);
  }
}
