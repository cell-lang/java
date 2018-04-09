package net.cell_lang.ord;


public class Ints231 {
  public static void sort(int[] array, int size) {
    throw new RuntimeException();
  }

  public static boolean contains23(int[] array, int size, int val2, int val3) {
    throw new RuntimeException();
  }
}


// static boolean contains23(ArrayList<TernaryTable.Tuple> tuples, int field2, int field3) {
//   int low = 0;
//   int high = tuples.size() - 1;

//   while (low <= high) {
//     int mid = (int) (((long) low + (long) high) / 2);
//     TernaryTable.Tuple tuple = tuples.get(mid);
//     if (tuple.field2OrEmptyMarker > field2)
//       high = mid - 1;
//     else if (tuple.field2OrEmptyMarker < field2)
//       low = mid + 1;
//     else if (tuple.field3 > field3)
//       high = mid - 1;
//     else if (tuple.field3 < field3)
//       low = mid + 1;
//     else
//       return true;
//   }

//   return false;
// }


// static int compare231(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
//   if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
//     return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
//   else if (t1.field3 != t2.field3)
//     return t1.field3 - t2.field3;
//   else
//     return t1.field1OrNext - t2.field1OrNext;
// }
