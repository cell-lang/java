package net.cell_lang;

import java.io.StringWriter;


class KeyViolationException extends RuntimeException {
  String relvarName;
  int[] key;
  Obj[] tuple1, tuple2;
  boolean tuple2IsNew;

  KeyViolationException(String relvarName, int[] key, Obj[] tuple1, Obj[] tuple2, boolean tuple2IsNew) {
    this.relvarName = relvarName;
    this.key = key;
    this.tuple1 = tuple1;
    this.tuple2 = tuple2;
    this.tuple2IsNew = tuple2IsNew;
  }

  public String toString() {
    boolean isComposite = key.length > 1;
    StringWriter writer = new StringWriter();
    writer.write("Key violation: relation variable " + relvarName + ", column");
    if (isComposite)
      writer.write("s");
    writer.write(":");
    for (int i=0 ; i < key.length ; i++) {
      writer.write(" ");
      writer.write(Integer.toString(key[i]));
    }
    writer.write(tuple2IsNew ?
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

