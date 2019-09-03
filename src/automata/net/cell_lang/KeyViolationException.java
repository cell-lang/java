package net.cell_lang;

import java.io.StringWriter;


class KeyViolationException extends RuntimeException {
  public static int[] key_1 = new int[] {1};
  public static int[] key_2 = new int[] {2};
  public static int[] key_3 = new int[] {3};
  public static int[] key_12 = new int[] {1, 2};
  public static int[] key_13 = new int[] {1, 3};
  public static int[] key_23 = new int[] {2, 3};

  String relvarName;
  int[] key;
  Obj[] tuple1, tuple2;
  boolean betweenNew;

  KeyViolationException(String relvarName, int[] key, Obj[] tuple1, Obj[] tuple2, boolean betweenNew) {
    this.relvarName = relvarName;
    this.key = key;
    this.tuple1 = tuple1;
    this.tuple2 = tuple2;
    this.betweenNew = betweenNew;
  }

  public String toString() {
    boolean isComposite = key.length > 1;
    StringWriter writer = new StringWriter();
    writer.write("Key violation: relation variable: " + relvarName + ", column");
    if (isComposite)
      writer.write("s");
    writer.write(":");
    for (int i=0 ; i < key.length ; i++) {
      writer.write(" ");
      writer.write(Integer.toString(key[i]));
    }
    writer.write(betweenNew ?
      "\nAttempt to insert conflicting tuples:\n" :
      "\nAttempt to insert tuple that conflicts with existing one:\n"
    );
    writer.write("  (");
    for (int i=0 ; i < tuple1.length ; i++) {
      if (i > 0)
        writer.write(", ");
      tuple1[i].print(writer, Integer.MAX_VALUE, true, 0);
    }
    writer.write(")\n  (");
    for (int i=0 ; i < tuple2.length ; i++) {
      if (i > 0)
        writer.write(", ");
      tuple2[i].print(writer, Integer.MAX_VALUE, true, 0);
    }
    writer.write(")\n");
    return writer.toString();
  }
}

