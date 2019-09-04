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

  private static final class UnaryBinaryForeignKeyType implements ForeignKeyType {
    int column;

    public UnaryBinaryForeignKeyType(int column) {
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

  public static ForeignKeyViolationException unaryUnary(String fromRelvar, String toRelvar, Obj[] fromTuple) {
    return unaryUnary(fromRelvar, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException unaryUnary(String fromRelvar, String toRelvar, Obj[] fromTuple, Obj[] toTuple) {
    return new ForeignKeyViolationException(UNARY_UNARY, fromRelvar, toRelvar, fromTuple, toTuple);
  }

  public static ForeignKeyViolationException unaryBinary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple) {
    return unaryBinary(fromRelvar, column, toRelvar, fromTuple, null);
  }

  public static ForeignKeyViolationException unaryBinary(String fromRelvar, int column, String toRelvar, Obj[] fromTuple, Obj[] toTuple) {
    ForeignKeyType type = new UnaryBinaryForeignKeyType(column);
    return new ForeignKeyViolationException(type, fromRelvar, toRelvar, fromTuple, toTuple);
  }

  //////////////////////////////////////////////////////////////////////////////

  public String toString() {
    StringWriter writer = new StringWriter();
    writer.write("Foreign key violation: " + fromRelvar + type.originArgs() + " -> " + toRelvar + type.targetArgs() + "\n");
    if (toTuple == null) {
      // The violation was caused by an insertion
      writer.write("The violation was caused by the insertion of the following tuple:\n  (");
      for (int i=0 ; i < fromTuple.length ; i++) {
        if (i > 0)
          writer.write(", ");
        fromTuple[i].print(writer, Integer.MAX_VALUE, true, 0);
      }
      writer.write(")\n");
    }
    else {

    }
    return writer.toString();
  }
}