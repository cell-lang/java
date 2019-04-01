package net.cell_lang;


public class UnitTests {
  public static void main(String[] args) {
    // Test_Ints12.run();
    // System.out.println("Ints12            OK");
    // Test_Ints21.run();
    // System.out.println("Ints21            OK");
    // Test_Ints123.run();
    // System.out.println("Ints123           OK");
    // Test_Ints231.run();
    // System.out.println("Ints231           OK");
    // Test_Ints312.run();
    // System.out.println("Ints312           OK");
    // Test_BinaryTable.run();
    // System.out.println("BinaryTable       OK");
    // Test_TernaryTable.run();
    // System.out.println("TernaryTable      OK");

    // Test_SymBinaryTable.run();
    // System.out.println("SymBinaryTable    OK");
    // Test_Sym12TernaryTable.run();
    // System.out.println("Sym12TernaryTable OK");

    // Test_AssocTable.run();
    // System.out.println("AssocTable       OK");
    Test_SlaveTernTable.run();
    System.out.println("SlaveTernTable   OK");

    // Test_BinaryTable.run2();
    // Test_BinaryTable.run3();
    // System.out.println("WTF?");

    //## TODO: RUN THESE!!!!!!!!!!!!
    // Test_AssocTable.run2();
    // Test_AssocTable.run3();
    // System.out.println("WTF?");

    // Test_ForeignKey_UU.run();
    // System.out.println("unary(a)     -> unary(a)        OK");
    // Test_ForeignKey_BU1.run();
    // System.out.println("binary(a, _) -> unary(a)        OK");
    // Test_ForeignKey_BU2.run();
    // System.out.println("binary(_, b) -> unary(b)        OK");
    // Test_ForeignKey_TU1.run();
    // System.out.println("ternary(a, _, _) -> unary(a)    OK");
    // Test_ForeignKey_TU2.run();
    // System.out.println("ternary(_, b, _) -> unary(b)    OK");
    // Test_ForeignKey_TU3.run();
    // System.out.println("ternary(_, _, c) -> unary(c)    OK");
    // Test_ForeignKey_UB1.run();
    // System.out.println("unary(a) -> binary(a, _)        OK");
    // Test_ForeignKey_UB2.run();
    // System.out.println("unary(b) -> binary(_, b)        OK");
    // Test_ForeignKey_UT1.run();
    // System.out.println("unary(a) -> ternary(a, _, _)    OK");
    // Test_ForeignKey_UT2.run();
    // System.out.println("unary(b) -> ternary(_, b, _)    OK");
    // Test_ForeignKey_UT3.run();
    // System.out.println("unary(c) -> ternary(_, _, c)    OK");
    // Test_ForeignKey_TB12.run();
    // System.out.println("ternary(a, b, _) -> binary(a, b)  OK");
    // Test_ForeignKey_BT12.run();
    // System.out.println("binary(a, b) -> ternary(a, b, _)  OK");

    // Test_ForeignKey_TB12S.run();
    // System.out.println("ternary(a | b, _) -> binary(a | b)  OK");
    // Test_ForeignKey_BT12S.run();
    // System.out.println("binary(a | b) -> ternary(a | b, _)  OK");

    // Test_ValueStore.run();

    // IntStore intStore = new IntStore();
    // IntStoreUpdater intStoreUpdater = new IntStoreUpdater(intStore);
    // ObjStore objStore = new ObjStore();
    // ObjStoreUpdater objStoreUpdater = new ObjStoreUpdater(objStore);
  }
}
