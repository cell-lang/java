package net.cell_lang.ord;


public class Ints312 {
  public static void sort(int[] array, int size) {
    throw new RuntimeException();
  }

  public static boolean contains3(int[] array, int size, int val3) {
    throw new RuntimeException();
  }

  public static boolean contains13(int[] array, int size, int val1, int val3) {
    throw new RuntimeException();
  }
}


// static boolean contains31(ArrayList<TernaryTable.Tuple> tuples, int field3, int field1) {
//   int low = 0;
//   int high = tuples.size() - 1;

//   while (low <= high) {
//     int mid = (int) (((long) low + (long) high) / 2);
//     TernaryTable.Tuple tuple = tuples.get(mid);
//     if (tuple.field3 > field3)
//       high = mid - 1;
//     else if (tuple.field3 < field3)
//       low = mid + 1;
//     else if (tuple.field1OrNext > field1)
//       high = mid - 1;
//     else if (tuple.field1OrNext < field1)
//       low = mid + 1;
//     else
//       return true;
//   }

//   return false;
// }

// static boolean contains3(ArrayList<TernaryTable.Tuple> tuples, int field3) {
//   int low = 0;
//   int high = tuples.size() - 1;

//   while (low <= high) {
//     int mid = (int) (((long) low + (long) high) / 2);
//     int midField3 = tuples.get(mid).field3;
//     if (midField3 > field3)
//       high = mid - 1;
//     else if (midField3 < field3)
//       low = mid + 1;
//     else
//       return true;
//   }

//   return false;
// }


// static int compare312(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
//   if (t1.field3 != t2.field3)
//     return t1.field3 - t2.field3;
//   if (t1.field1OrNext != t2.field1OrNext)
//     return t1.field1OrNext - t2.field1OrNext;
//   else
//     return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
// }
