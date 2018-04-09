package net.cell_lang.ord;


public class Ints123 {
  public static void sort(int[] array, int size) {
    throw new RuntimeException();
  }

  public static boolean contains12(int[] array, int size, int val1, int val2) {
    throw new RuntimeException();
  }
}

// static boolean contains12(ArrayList<TernaryTable.Tuple> tuples, int field1, int field2) {
//   int low = 0;
//   int high = tuples.size() - 1;

//   while (low <= high) {
//     int mid = (int) (((long) low + (long) high) / 2);
//     TernaryTable.Tuple tuple = tuples.get(mid);
//     if (tuple.field1OrNext > field1)
//       high = mid - 1;
//     else if (tuple.field1OrNext < field1)
//       low = mid + 1;
//     else if (tuple.field2OrEmptyMarker > field2)
//       high = mid - 1;
//     else if (tuple.field2OrEmptyMarker < field2)
//       low = mid + 1;
//     else
//       return true;
//   }

//   return false;
// }


// static int compare123(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
//   if (t1.field1OrNext != t2.field1OrNext)
//     return t1.field1OrNext - t2.field1OrNext;
//   else if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
//     return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
//   else
//     return t1.field3 - t2.field3;
// }
