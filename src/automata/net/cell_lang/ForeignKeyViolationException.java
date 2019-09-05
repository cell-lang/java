package net.cell_lang;

import java.io.StringWriter;


class ForeignKeyViolationException extends RuntimeException {
  private interface ForeignKeyType {
    String originArgs();
    String targetArgs();
  }

  private static final class NoArgsForeignKeyType implements ForeignKeyType {
    String originArgs, targetArgs;

    public NoArgsForeignKeyType(String originArgs, String targetArgs) {
      this.originArgs = originArgs;
      this.targetArgs = targetArgs;
    }

    public String originArgs() {
      return originArgs;
    }

    public String targetArgs() {
      return targetArgs;
    }
  }

  private static final class BinaryUnaryForeignKeyType implements ForeignKeyType {
    int column;

    public BinaryUnaryForeignKeyType(int column) {
      Miscellanea._assert(column == 1 | column == 2);
      this.column = column;
    }

    public String originArgs() {
      return column == 1 ? "(a, _)" : "(_, a)";
    }

    public String targetArgs() {
      return "(a)";
    }
  }

  private static final class TernaryUnaryForeignKeyType implements ForeignKeyType {
    int column;

    public TernaryUnaryForeignKeyType(int column) {
      Miscellanea._assert(column == 1 | column == 2 | column == 3);
      this.column = column;
    }

    public String originArgs() {
      return column == 1 ? "(a, _, _)" : column == 2 ? "(_, a, _)" : "(_, _, a)";
    }

    public String targetArgs() {
      return "(a)";
    }
  }

  public static final ForeignKeyType UNARY_UNARY = new NoArgsForeignKeyType("(a)", "(a)");
  public static final ForeignKeyType BINARY_TERNARY = new NoArgsForeignKeyType("(a, b)", "(a, b, _)");
  public static final ForeignKeyType TERNARY_BINARY = new NoArgsForeignKeyType("(a, b, _)", "(a, b)");
  public static final ForeignKeyType UNARY_SYM_BINARY = new NoArgsForeignKeyType("(a)", "(a | _)");
  public static final ForeignKeyType UNARY_SYM_TERNARY = new NoArgsForeignKeyType("(a)", "(a | _, _)");
  public static final ForeignKeyType SYM_BINARY_UNARY = new NoArgsForeignKeyType("(a | _)", "(a)");
  public static final ForeignKeyType SYM_TERNARY_UNARY = new NoArgsForeignKeyType("(a | _, _)", "(a)");
  public static final ForeignKeyType SYM_BINARY_SYM_TERNARY = new NoArgsForeignKeyType("(a | b)", "(a | b, _)");
  public static final ForeignKeyType SYM_TERNARY_SYM_BINARY = new NoArgsForeignKeyType("(a | b, _)", "(a | b)");

  // type ForeignKeyType = unary_unary, unary_binary(<0..1>), unary_ternary(<0..2>),
  //                       binary_unary(<0..1>), ternary_unary(<0..2>),
  //                       binary_ternary, ternary_binary,
  //                       unary_sym_binary, unary_sym_ternary,
  //                       sym_binary_unary, sym_ternary_unary,
  //                       sym_binary_sym_ternary, sym_ternary_sym_binary;


  ForeignKeyType type;
  String fromRelvar, toRelvar;
  Obj[] fromTuple, toTuple;

  private ForeignKeyViolationException(ForeignKeyType type, String fromRelvar, String toRelvar, Obj[] fromTuple, Obj[] toTuple) {
    this.type = type;
    this.fromRelvar = fromRelvar;
    this.toRelvar = toRelvar;
    this.fromTuple = fromTuple;
    this.toTuple = toTuple;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static ForeignKeyViolationException unaryUnary(String fromRelvar, String toRelvar, Obj value) {
    Obj[] tuple = new Obj[] {value};
    return new ForeignKeyViolationException(UNARY_UNARY, fromRelvar, toRelvar, tuple, tuple);
  }

  public static ForeignKeyViolationException binaryUnary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple) {
    return binaryUnary(fromRelvar, column, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException binaryUnary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple, Obj toArg) {
    ForeignKeyType type = new BinaryUnaryForeignKeyType(column);
    Obj[] toTuple = toArg != null ? new Obj[] {toArg} : null;
    return new ForeignKeyViolationException(type, fromRelvar, toRelvar, fromTuple, toTuple);
  }

  public static ForeignKeyViolationException symBinaryUnary(String fromRelvar, String toRelvar, Obj[] fromTuple) {
    return symBinaryUnary(fromRelvar, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException symBinaryUnary(String fromRelvar, String toRelvar, Obj[] fromTuple, Obj toArg) {
    Obj[] toTuple = toArg != null ? new Obj[] {toArg} : null;
    return new ForeignKeyViolationException(SYM_BINARY_UNARY, fromRelvar, toRelvar, fromTuple, toTuple);
  }

  public static ForeignKeyViolationException ternaryUnary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple) {
    return ternaryUnary(fromRelvar, column, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException ternaryUnary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple, Obj toArg) {
    ForeignKeyType type = new TernaryUnaryForeignKeyType(column);
    Obj[] toTuple = toArg != null ? new Obj[] {toArg} : null;
    return new ForeignKeyViolationException(type, fromRelvar, toRelvar, fromTuple, toTuple);
  }

  public static ForeignKeyViolationException symTernary12Unary(String fromRelvar, String toRelvar, Obj[] fromTuple) {
    return new ForeignKeyViolationException(SYM_TERNARY_UNARY, fromRelvar, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException symTernary12Unary(String fromRelvar, String toRelvar, Obj[] fromTuple, Obj toArg) {
    return new ForeignKeyViolationException(SYM_TERNARY_UNARY, fromRelvar, toRelvar, fromTuple, new Obj[] {toArg});
  }

  public static ForeignKeyViolationException symTernary3Unary(String fromRelvar, String toRelvar, Obj[] fromTuple) {
    return ternaryUnary(fromRelvar, 3, toRelvar, fromTuple);
  }

  public static ForeignKeyViolationException symTernary3Unary(String fromRelvar, String toRelvar, Obj[] fromTuple, Obj toArg) {
    return ternaryUnary(fromRelvar, 3, toRelvar, fromTuple, toArg);
  }

  //////////////////////////////////////////////////////////////////////////////

  public String toString() {
    StringWriter writer = new StringWriter();
    writer.write("Foreign key violation: " + fromRelvar + type.originArgs() + " -> " + toRelvar + type.targetArgs() + "\n");
    if (toTuple == null) {
      // The violation was caused by an insertion
      writer.write("The failure was caused by the attempted insertion of the following tuple:\n  (");
      for (int i=0 ; i < fromTuple.length ; i++) {
        if (i > 0)
          writer.write(", ");
        fromTuple[i].print(writer, Integer.MAX_VALUE, true, 0);
      }
      writer.write(")\n");
    }
    else {
      // The violation was caused by a deletion in the target table
      writer.write("The failure was caused by the attempted deletion of:\n  " + toRelvar + "(");
      for (int i=0 ; i < toTuple.length ; i++) {
        if (i > 0)
          writer.write(", ");
        toTuple[i].print(writer, Integer.MAX_VALUE, true, 0);
      }
      writer.write(")\n");
      writer.write("which was prevented by the presence of:\n  " + fromRelvar + "(");
      for (int i=0 ; i < fromTuple.length ; i++) {
        if (i > 0)
          writer.write(", ");
        fromTuple[i].print(writer, Integer.MAX_VALUE, true, 0);
      }
      writer.write(")\n");
    }
    return writer.toString();
  }
}