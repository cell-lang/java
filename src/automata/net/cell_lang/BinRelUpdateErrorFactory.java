package net.cell_lang;

import java.util.function.IntFunction;


class BinRelUpdateErrorFactory {
  private static final int[] col1Key = new int[] {1};
  private static final int[] col2Key = new int[] {2};

  private String relvarName;
  private IntFunction<int[]> arg1To2;
  private IntFunction<int[]> arg2To1;
  private SurrObjMapper mapper1;
  private SurrObjMapper mapper2;


  protected BinRelUpdateErrorFactory(String relvarName, IntFunction<int[]> arg1To2, IntFunction<int[]> arg2To1, SurrObjMapper mapper1, SurrObjMapper mapper2) {
    this.relvarName = relvarName;
    this.arg1To2 = arg1To2;
    this.arg2To1 = arg2To1;
    this.mapper1 = mapper1;
    this.mapper2 = mapper2;
  }

  //////////////////////////////////////////////////////////////////////////////

  protected KeyViolationException col1KeyViolation(int arg1Surr, int arg2Surr, int otherArg2Surr) {
    return col1KeyViolation(arg1Surr, arg2Surr, otherArg2Surr, true);
  }

  protected KeyViolationException col1KeyViolation(int arg1Surr, int arg2Surr) {
    return col1KeyViolation(arg1Surr, arg2Surr, arg1To2.apply(arg1Surr)[0], false);
  }

  private KeyViolationException col1KeyViolation(int arg1Surr, int arg2Surr, int otherArg2Surr, boolean betweenNew) {
    Obj arg1 = mapper1.surrToObj(arg1Surr);
    Obj[] tuple1 = new Obj[] {arg1, mapper2.surrToObj(arg2Surr)};
    Obj[] tuple2 = new Obj[] {arg1, mapper2.surrToObj(otherArg2Surr)};
    return new KeyViolationException(relvarName, col1Key, tuple1, tuple2, betweenNew);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected KeyViolationException col2KeyViolation(int arg1Surr, int arg2Surr, int otherArg1Surr) {
    return col2KeyViolation(arg1Surr, arg2Surr, otherArg1Surr, true);
  }

  protected KeyViolationException col2KeyViolation(int arg1Surr, int arg2Surr) {
    return col2KeyViolation(arg1Surr, arg2Surr, arg2To1.apply(arg2Surr)[0], false);
  }

  private KeyViolationException col2KeyViolation(int arg1Surr, int arg2Surr, int otherArg1Surr, boolean betweenNew) {
    Obj arg2 = mapper2.surrToObj(arg2Surr);
    Obj[] tuple1 = new Obj[] {mapper1.surrToObj(arg1Surr), arg2};
    Obj[] tuple2 = new Obj[] {mapper1.surrToObj(otherArg1Surr), arg2};
    return new KeyViolationException(relvarName, col2Key, tuple1, tuple2, betweenNew);
  }
}