package net.cell_lang;


class OverflowTable {
  public static class Iter {
    int[] values;
    int   next;
    int   end;

    public Iter(int[] values, int first, int count) {
      this.values = values;
      this.next   = first;
      this.end    = first + count;
    }

    public int get() {
      Miscellanea._assert(!done());
      return values[next];
    }

    public boolean done() {
      return next >= end;
    }

    public void next() {
      Miscellanea._assert(!done());
      next++;
    }
  }


  static final int MinSize = 32;

  public static final int EmptyMarker  = 0xFFFFFFFF;

  static final int EndLowerMarker   = 0xDFFFFFFF;
  static final int End2UpperMarker  = 0x3FFFFFFF;
  static final int End4UpperMarker  = 0x5FFFFFFF;
  static final int End8UpperMarker  = 0x7FFFFFFF;
  static final int End16UpperMarker = 0x9FFFFFFF;

  public static final int PayloadMask  = 0x1FFFFFFF;

  static final int InlineTag            = 0;
  static final int Block2Tag            = 1;
  static final int Block4Tag            = 2;
  static final int Block8Tag            = 3;
  static final int Block16Tag           = 4;
  static final int HashedBlockTag       = 5;
  static final int AvailableTag         = 6;
  static final int Unused               = 7;

  int[] slots;
  int head2;
  int head4;
  int head8;
  int head16;

  public void check(int[] column, int count) {
    int len = slots.length;
    boolean[] slotOK = new boolean[len];
    for (int i=0 ; i < len ; i++)
      Miscellanea._assert(!slotOK[i]);

    if (head2 != EmptyMarker) {
      int curr = head2;
      check(slots[curr] == EndLowerMarker, "slots[curr] == EndLowerMarker (2), curr = " + Integer.toString(curr));
      for ( ; ; ) {
        check(curr < len, "curr < len");
        int slot1 = slots[curr + 1];
        int tag = slot1 >>> 29;
        int payload = slot1 & PayloadMask;
        check(slot1 == End2UpperMarker | (tag == Block2Tag & payload < len), "slot1 == End2UpperMarker | (tag == Block2Tag & payload < len)");
        check(!slotOK[curr] & !slotOK[curr+1], "!slotOK[curr] & !slotOK[curr+1]");
        slotOK[curr] = slotOK[curr+1] = true;
        if (slot1 == End2UpperMarker)
          break;
        int nextSlot0 = slots[payload];
        check(nextSlot0 >>> 29 == AvailableTag, "nextSlot0 >>> 29 == AvailableTag");
        check((nextSlot0 & PayloadMask) == curr, "(nextSlot0 & PayloadMask) == curr");
        curr = payload;
      }
    }

    if (head4 != EmptyMarker) {
      int curr = head4;
      check(slots[curr] == EndLowerMarker, "slots[curr] == EndLowerMarker (4), curr = " + Integer.toString(curr));
      for ( ; ; ) {
        check(curr < len, "curr < len");
        int slot1 = slots[curr + 1];
        int tag = slot1 >>> 29;
        int payload = slot1 & PayloadMask;
        check(slot1 == End4UpperMarker | (tag == Block4Tag & payload < len), "slot1 == End4UpperMarker | (tag == Block4Tag & payload < len)");
        for (int i=0 ; i < 4 ; i++) {
          check(!slotOK[curr+i], "!slotOK[curr+i]");
          slotOK[curr+i] = true;
        }
        if (slot1 == End4UpperMarker)
          break;
        int nextSlot0 = slots[payload];
        check(nextSlot0 >>> 29 == AvailableTag, "nextSlot0 >>> 29 == AvailableTag");
        check((nextSlot0 & PayloadMask) == curr, "(nextSlot0 & PayloadMask) == curr");
        curr = payload;
      }
    }

    if (head8 != EmptyMarker) {
      int curr = head8;
      check(slots[curr] == EndLowerMarker, "slots[curr] == EndLowerMarker (8), curr = " + Integer.toString(curr));
      for ( ; ; ) {
        check(curr < len, "curr < len");
        int slot1 = slots[curr + 1];
        int tag = slot1 >>> 29;
        int payload = slot1 & PayloadMask;
        check(slot1 == End8UpperMarker | (tag == Block8Tag & payload < len), "slot1 == End8UpperMarker | (tag == Block8Tag & payload < len)");
        for (int i=0 ; i < 8 ; i++) {
          check(!slotOK[curr+i], "!slotOK[curr+i]");
          slotOK[curr+i] = true;
        }
        if (slot1 == End8UpperMarker)
          break;
        int nextSlot0 = slots[payload];
        check(nextSlot0 >>> 29 == AvailableTag, "nextSlot0 >>> 29 == AvailableTag");
        check((nextSlot0 & PayloadMask) == curr, "(nextSlot0 & PayloadMask) == curr");
        curr = payload;
      }
    }

    if (head16 != EmptyMarker) {
      int curr = head16;
      check(slots[curr] == EndLowerMarker, "slots[curr] == EndLowerMarker (16), curr = " + Integer.toString(curr));
      for ( ; ; ) {
        check(curr < len, "curr < len");
        int slot1 = slots[curr + 1];
        int tag = slot1 >>> 29;
        int payload = slot1 & PayloadMask;
        check(slot1 == End16UpperMarker | (tag == Block16Tag & payload < len), "slot1 == End16UpperMarker | (tag == Block16Tag & payload < len)");
        for (int i=0 ; i < 16 ; i++) {
          check(!slotOK[curr+i], "!slotOK[curr+i]");
          slotOK[curr+i] = true;
        }
        if (slot1 == End16UpperMarker)
          break;
        int nextSlot0 = slots[payload];
        check(nextSlot0 >>> 29 == AvailableTag, "nextSlot0 >>> 29 == AvailableTag");
        check((nextSlot0 & PayloadMask) == curr, "(nextSlot0 & PayloadMask) == curr");
        curr = payload;
      }
    }

    int actualCount = 0;
    for (int i=0 ; i < column.length ; i++) {
      int content = column[i];
      if (content == EmptyMarker)
        continue;
      int tag = content >>> 29;
      int payload = content & PayloadMask;
      if (tag == 0) {
        check(payload < 1000, "payload < 1000");
        actualCount++;
      }
      else {
        actualCount += checkGroup(tag, payload, slotOK, actualCount);
      }
    }

    check(count == actualCount, "count == actualCount");

    for (int i=0 ; i < slotOK.length ; i++) {
      if (!slotOK[i]) {
        for (int j=0 ; j < slotOK.length ; j++) {
          if (j != 0 & j % 256 == 0)
            System.out.println();
          if (j != 0 & j % 128 == 0)
            System.out.println();
          if (j % 16 == 0)
            System.out.print("\n  ");
          System.out.printf("%d ", slotOK[j] ? 1 : 0);
        }
        System.out.println();
        System.out.println();
        System.out.printf("i = %d\n", i);
        System.out.println();
      }
      check(slotOK[i], "slotOK[i]");
    }
  }

  int checkGroup(int tag, int blockIdx, boolean[] slotOK, int totalCount) {
    check(tag >= Block2Tag, "tag >= Block2Tag");
    check(tag <= HashedBlockTag, "tag <= HashedBlockTag");

    int capacity, minUsage;
    if (tag == Block2Tag) {
      capacity = 2;
      minUsage = 2;
    }
    else if (tag == Block4Tag) {
      capacity = 4;
      minUsage = 2;
    }
    else if (tag == Block8Tag) {
      capacity = 8;
      minUsage = 4;
    }
    else if (tag == Block16Tag) {
      capacity = 16;
      minUsage = 7;
    }
    else {
      Miscellanea._assert(tag == HashedBlockTag);
      capacity = 16;
      minUsage = 7; // Unused
    }

    for (int i=0 ; i < capacity ; i++) {
      check(!slotOK[blockIdx+i], "!slotOK[blockIdx+i]");
      slotOK[blockIdx+i] = true;
    }

    if (tag != HashedBlockTag) {
      for (int i=0 ; i < capacity ; i++) {
        int slot = slots[blockIdx + i];
        if (i < minUsage)
          check(slot != EmptyMarker, "slot != EmptyMarker");
        if (slot == EmptyMarker) {
          for (int j=i+1 ; j < capacity ; j++)
            check(slots[blockIdx+j] == EmptyMarker, "slots[blockIdx+j] == EmptyMarker");
          break;
        }
        check(slot >>> 29 == 0, "slot >>> 29 == 0");
        check((slot & PayloadMask) < 1000, "(slot & PayloadMask) < 1000");
        totalCount++;
      }
    }
    else {
      int blockCount = slots[blockIdx];
      int actualBlockCount = 0;
      for (int i=1 ; i < 16 ; i++) {
        int slot = slots[blockIdx + i];
        if (slot != EmptyMarker) {
          int slotTag = slot >>> 29;
          int slotPayload = slot & PayloadMask;
          if (slotTag == 0) {
            check(slotPayload < 1000, "slotPayload < 1000");
            actualBlockCount++;
          }
          else
            actualBlockCount += checkGroup(slotTag, slotPayload, slotOK, actualBlockCount);
        }
      }
      check(blockCount == actualBlockCount, "blockCount == actualBlockCount");
      totalCount += blockCount;
    }

    return totalCount;
  }

  void check(boolean cond, String msg) {
    if (!cond) {
      System.out.println(msg);
      System.out.println("");
      dump();
      System.out.println("");
      Miscellanea._assert(false);
    }
  }

  public void dump() {
    System.out.print("  slots:");
    for (int i=0 ; i < slots.length ; i++) {
      if (i != 0 & i % 256 == 0)
        System.out.println();
      if (i != 0 & i % 128 == 0)
        System.out.println();
      if (i % 16 == 0)
        System.out.print("\n   ");
      else if (i % 8 == 0)
        System.out.print("  ");
      int slot = slots[i];
      int payload = slot & PayloadMask;
      System.out.printf("  %d:%3s", slot >>> 29, payload == 0x1FFFFFFF ? "-" : Integer.toString(payload));
    }
    System.out.println();
    System.out.println();
    System.out.printf(
      "  heads: 2 = %s, 4 = %s, 8 = %s, 16 = %s\n",
      head2  != EmptyMarker ? Integer.toString(head2)  : "-",
      head4  != EmptyMarker ? Integer.toString(head4)  : "-",
      head8  != EmptyMarker ? Integer.toString(head8)  : "-",
      head16 != EmptyMarker ? Integer.toString(head16) : "-"
    );
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public OverflowTable() {
    slots = new int[MinSize];
    for (int i=0 ; i < MinSize ; i += 16) {
      slots[i]   = (i - 16) | AvailableTag << 29;
      slots[i+1] = (i + 16) | Block16Tag << 29;
    }
    slots[0] = EndLowerMarker;
    slots[MinSize-16+1] = End16UpperMarker;
    head2 = head4 = head8 = EmptyMarker;
    head16 = 0;
  }

  public int insert(int handle, int value, boolean[] inserted) {
    int tag = handle >>> 29;
    int payload = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | payload) == handle);

    switch (tag) {
      case 0:
        // The entry was single-valued, and inlined.
        // The payload contains the existing value.
        Miscellanea._assert(handle == payload);
        Miscellanea._assert(payload != value);
        return insert2Block(handle, value, inserted);

      // From now on the payload is the index of the block

      case 1: // 2-slots block
        return insertWith2Block(payload, value, handle, inserted);

      case 2: // 4-slot block
        return insertWith4Block(payload, value, handle, inserted);

      case 3: // 8-slot block
        return insertWith8Block(payload, value, handle, inserted);

      case 4: // 16-slot block
        return insertWith16Block(payload, value, handle, inserted);

      case 5: // Hashed block
        insertIntoHashedBlock(payload, value, hashCode(value), inserted);
        return handle;

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  public int delete(int handle, int value, boolean[] deleted) {
    int tag = handle >>> 29;
    int blockIdx = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | blockIdx) == handle);

    switch (tag) {
      case 1: // 2-slots block
        return deleteFrom2Block(blockIdx, value, handle, deleted);

      case 2: // 4-slot block
        return deleteFrom4Block(blockIdx, value, handle, deleted);

      case 3: // 8-slot block
        return deleteFrom8Block(blockIdx, value, handle, deleted);

      case 4: // 16-slot block
        return deleteFrom16Block(blockIdx, value, handle, deleted);

      case 5: // Hashed block
        return deleteFromHashedBlock(blockIdx, value, handle, hashCode(value), deleted);

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  public boolean in(int value, int handle) {
    int tag = handle >>> 29;
    int blockIdx = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | blockIdx) == handle);

    switch (tag) {
      case 1: // 2-block slot
        return in2Block(value, blockIdx);

      case 2: // 4-block slot
        return inBlock(value, blockIdx, 4);

      case 3: // 8-block slot
        return inBlock(value, blockIdx, 8);

      case 4: // 16-slot block
        return inBlock(value, blockIdx, 16);

      case 5: // Hashed block
        return inHashedBlock(value, blockIdx, hashCode(value));

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  public int count(int handle) {
    int tag = handle >>> 29;
    int blockIdx = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | blockIdx) == handle);

    switch (tag) {
      case 0: // Inline
        return 1;

      case 1: // 2-block slot
        return 2;

      case 2: // 4-block slot
        return 2 + countFrom(blockIdx + 2, 2);

      case 3: // 8-block slot
        return 4 + countFrom(blockIdx + 4, 4);

      case 4: // 16-slot block
        return 7 + countFrom(blockIdx + 7, 9);

      case 5: // Hashed block
        return slots[blockIdx];

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  public Iter getIter(int handle) {
    int tag = handle >>> 29;
    int blockIdx = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | blockIdx) == handle);

    switch (tag) {
      // case 0: // Inline
      //  return 1;

      case 1: // 2-block slot
        return new Iter(slots, blockIdx, 2);

      case 2: // 4-block slot
      case 3: // 8-block slot
      case 4: // 16-slot block
        return new Iter(slots, blockIdx, count(handle));

      case 5: // Hashed block
        return hashedBlockIter(blockIdx);

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  boolean in2Block(int value, int blockIdx) {
    return value == slots[blockIdx] || value == slots[blockIdx+1];
  }

  boolean inBlock(int value, int blockIdx, int blockSize) {
    for (int i=0 ; i < blockSize ; i++) {
      int content = slots[blockIdx+i];
      if (content == value)
        return true;
      if (content == EmptyMarker)
        return false;
    }
    return false;
  }

  boolean inHashedBlock(int value, int blockIdx, int hashcode) {
    int slotIdx = blockIdx + hashcode % 15 + 1;
    int content = slots[slotIdx];
    if (content == value)
      return true;
    if (content == EmptyMarker)
      return false;
    int tag = content >>> 29;
    Miscellanea._assert(tag <= 5);
    if (tag == 0)
      return false;
    else if (tag < 5)
      return in(value, content);
    else
      return inHashedBlock(value, content & PayloadMask, hashcode / 15);
  }

  ////////////////////////////////////////////////////////////////////////////

  int countFrom(int offset, int max) {
    for (int i=0 ; i < max ; i++)
      if (slots[offset+i] == EmptyMarker)
        return i;
    return max;
  }

  ////////////////////////////////////////////////////////////////////////////

  Iter hashedBlockIter(int blockIdx) {
    int count = slots[blockIdx];
    int[] values = new int[count];
    int next = copyHashedBlock(blockIdx, values, 0);
    Miscellanea._assert(next == count);
    return new Iter(values, 0, count);
  }

  int copyHashedBlock(int blockIdx, int[] dest, int next) {
    for (int i=1 ; i < 16 ; i++) {
      int content = slots[blockIdx+i];
      if (content != EmptyMarker) {
        int tag = content >>> 29;
        if (tag == 0)
          dest[next++] = content;
        else
          next = copy(content, dest, next);
      }
    }
    return next;
  }

  int copy(int handle, int[] dest, int next) {
    int tag = handle >>> 29;
    int blockIdx = handle & PayloadMask;
    Miscellanea._assert(((tag << 29) | blockIdx) == handle, "((tag << 29) | blockIdx) == handle");

    switch (tag) {
      // case 0: // Inline
      //   dest[next++] = handle;
      //   return next;

      case 1: // 2-block slot
        dest[next++] = slots[blockIdx];
        dest[next++] = slots[blockIdx + 1];
        return next;

      case 2: // 4-block slot
        return copyNonEmpty(blockIdx, 4, dest, next);

      case 3: // 8-block slot
        return copyNonEmpty(blockIdx, 8, dest, next);

      case 4: // 16-slot block
        return copyNonEmpty(blockIdx, 16, dest, next);

      case 5: // Hashed block
        return copyHashedBlock(blockIdx, dest, next);

      default:
        throw Miscellanea.internalFail(); // Control flow cannot get here
    }
  }

  int copyNonEmpty(int offset, int max, int[] dest, int next) {
    for (int i=0 ; i < max ; i++) {
      int content = slots[offset + i];
      if (content == EmptyMarker)
        break;
      dest[next++] = content;
    }
    return next;
  }

  ////////////////////////////////////////////////////////////////////////////

  int insert2Block(int value0, int value1, boolean[] inserted) {
    // The newly inserted 2-block is not ordered
    // The returned handle is the address of the two-block,
    // tagged with the 2-values tag (= 1)

    // Checking first that the new value is not the same as the old one
    if (value0 == value1) {
      // When there's only a single value, the value and the handle are the same
      inserted[0] = false;
      return value0;
    }

    int blockIdx = alloc2Block();
    slots[blockIdx]   = value0;
    slots[blockIdx+1] = value1;

    inserted[0] = true;
    return blockIdx | (Block2Tag << 29);
  }

  int deleteFrom2Block(int blockIdx, int value, int handle, boolean[] deleted) {
    int value0 = slots[blockIdx];
    int value1 = slots[blockIdx+1];

    if (value != value0 & value != value1) {
      deleted[0] = false;
      return handle;
    }

    release2Block(blockIdx);
    deleted[0] = true;
    return value == value0 ? value1 : value0;
  }

  int insertWith2Block(int block2Idx, int value, int handle, boolean[] inserted) {
    // Going from a 2-block to a 4-block
    // Values are not sorted
    // The last slot is set to 0xFFFFFFFF
    // The return value is the address of the 4-block,
    // tagged with the 4-values tag (= 2)

    int value0 = slots[block2Idx];
    int value1 = slots[block2Idx+1];

    if (value == value0 | value == value1) {
      inserted[0] = false;
      return handle;
    }

    release2Block(block2Idx);

    int block4Idx = alloc4Block();
    slots[block4Idx]   = value0;
    slots[block4Idx+1] = value1;
    slots[block4Idx+2] = value;
    slots[block4Idx+3] = EmptyMarker;

    inserted[0] = true;
    return block4Idx | (Block4Tag << 29);
  }

  int deleteFrom4Block(int blockIdx, int value, int handle, boolean[] deleted) {
    int value0 = slots[blockIdx];
    int value1 = slots[blockIdx+1];
    int value2 = slots[blockIdx+2];
    int value3 = slots[blockIdx+3];

    if (value == value3) {
      deleted[0] = true;
      slots[blockIdx+3] = EmptyMarker;
    }
    else if (value == value2) {
      deleted[0] = true;
      if (value3 == EmptyMarker) {
        slots[blockIdx+2] = EmptyMarker;
      }
      else {
        slots[blockIdx+2] = value3;
        slots[blockIdx+3] = EmptyMarker;
      }
    }
    else if (value == value1) {
      deleted[0] = true;
      if (value2 == EmptyMarker) {
        release4Block(blockIdx);
        return value0;
      }
      else if (value3 == EmptyMarker) {
        slots[blockIdx+1] = value2;
        slots[blockIdx+2] = EmptyMarker;
      }
      else {
        slots[blockIdx+1] = value3;
        slots[blockIdx+3] = EmptyMarker;
      }
    }
    else if (value == value0) {
      deleted[0] = true;
      if (value2 == EmptyMarker) {
        release4Block(blockIdx);
        return value1;
      }
      else if (value3 == EmptyMarker) {
        slots[blockIdx]   = value2;
        slots[blockIdx+2] = EmptyMarker;
      }
      else {
        slots[blockIdx]   = value3;
        slots[blockIdx+3] = EmptyMarker;
      }
    }
    else
      deleted[0] = false;

    return handle;
  }

  int insertWith4Block(int block4Idx, int value, int handle, boolean[] inserted) {
    // The entry contains between two and four values already
    // The unused slots are at the end, and they are set to 0xFFFFFFFF

    int value0 = slots[block4Idx];
    int value1 = slots[block4Idx+1];
    int value2 = slots[block4Idx+2];
    int value3 = slots[block4Idx+3];

    if (value == value0 | value == value1 | value == value2 | value == value3) {
      inserted[0] = false;
      return handle;
    }

    inserted[0] = true;
    if (value3 == EmptyMarker) {
      // Easy case: the last slot is available
      // We store the new value there, and return the same handle
      slots[block4Idx+3] = value;
      return handle;
    }
    else if (value2 == EmptyMarker) {
      // Another easy case: the last but one slot is available
      slots[block4Idx+2] = value;
      return handle;
    }
    else {
      // The block is already full, we need to allocate an 8-block now
      // We store the values in the first five slots, and set the rest
      // to 0xFFFFFFFF. The return value is the index of the block,
      // tagged with the 8-value tag
      release4Block(block4Idx);

      int block8Idx = alloc8Block();
      slots[block8Idx]   = value0;
      slots[block8Idx+1] = value1;
      slots[block8Idx+2] = value2;
      slots[block8Idx+3] = value3;
      slots[block8Idx+4] = value;
      slots[block8Idx+5] = EmptyMarker;
      slots[block8Idx+6] = EmptyMarker;
      slots[block8Idx+7] = EmptyMarker;

      return block8Idx | (Block8Tag << 29);
    }
  }

  //## BAD BAD: THE IMPLEMENTATION IS ALMOST THE SAME AS THAT OF DeleteFrom16Block()
  int deleteFrom8Block(int blockIdx, int value, int handle, boolean[] deleted) {
    int lastValue = EmptyMarker;
    int targetIdx = -1;

    int idx = 0;
    while (idx < 8) {
      int valueI = slots[blockIdx + idx];
      if (valueI == value)
        targetIdx = idx;
      else if (valueI == EmptyMarker)
        break;
      else
        lastValue = valueI;
      idx++;
    }

    // <idx> is now the index of the first free block,
    // or <8> if the slot is full. It's also the number
    // of values in the block before the deletion
    Miscellanea._assert(idx >= 4);

    deleted[0] = targetIdx != -1;

    if (targetIdx == -1)
      return handle;

    if (targetIdx != idx)
      slots[blockIdx + targetIdx] = lastValue;
    slots[blockIdx + idx - 1] = EmptyMarker;

    if (idx == 4) {
      // We are down to 3 elements, so we release the upper half of the block
      release8BlockUpperHalf(blockIdx);
      return blockIdx | (Block4Tag << 29);
    }

    return handle;
  }

  int insertWith8Block(int block8Idx, int value, int handle, boolean[] inserted) {
    // The block contains between 4 and 8 values already
    // The unused ones are at the end, and they are set to 0xFFFFFFFF

    int value0 = slots[block8Idx];
    int value1 = slots[block8Idx+1];
    int value2 = slots[block8Idx+2];
    int value3 = slots[block8Idx+3];
    int value4 = slots[block8Idx+4];
    int value5 = slots[block8Idx+5];
    int value6 = slots[block8Idx+6];
    int value7 = slots[block8Idx+7];

    boolean isDuplicate = (value == value0 | value == value1 | value == value2 | value == value3) ||
                       (value == value4 | value == value5 | value == value6 | value == value7);
    inserted[0] = !isDuplicate;

    if (isDuplicate)
      return handle;

    if (value4 == EmptyMarker) {
      slots[block8Idx+4] = value;
      return handle;
    }

    if (value5 == EmptyMarker) {
      slots[block8Idx+5] = value;
      return handle;
    }

    if (value6 == EmptyMarker) {
      slots[block8Idx+6] = value;
      return handle;
    }

    if (value7 == EmptyMarker) {
      slots[block8Idx+7] = value;
      return handle;
    }

    release8Block(block8Idx);

    int block16Idx = alloc16Block();
    slots[block16Idx]   = value0;
    slots[block16Idx+1] = value1;
    slots[block16Idx+2] = value2;
    slots[block16Idx+3] = value3;
    slots[block16Idx+4] = value4;
    slots[block16Idx+5] = value5;
    slots[block16Idx+6] = value6;
    slots[block16Idx+7] = value7;
    slots[block16Idx+8] = value;
    for (int i=9 ; i < 16 ; i++)
      slots[block16Idx+i] = EmptyMarker;

    return block16Idx | (Block16Tag << 29);
  }

  //## BAD BAD: THE IMPLEMENTATION IS ALMOST THE SAME AS THAT OF DeleteFrom8Block()
  int deleteFrom16Block(int blockIdx, int value, int handle, boolean[] deleted) {
    int lastValue = EmptyMarker;
    int targetIdx = -1;

    int idx = 0;
    while (idx < 16) {
      int valueI = slots[blockIdx + idx];
      if (valueI == value)
        targetIdx = idx;
      else if (valueI == EmptyMarker)
        break;
      else
        lastValue = valueI;
      idx++;
    }

    // <idx> is now the index of the first free block,
    // or <16> if the slot is full. It's also the number
    // of values in the block before the deletion
    Miscellanea._assert(idx >= 7);

    deleted[0] = targetIdx != -1;

    if (targetIdx == -1)
      return handle;

    if (targetIdx != idx)
      slots[blockIdx + targetIdx] = lastValue;
    slots[blockIdx + idx - 1] = EmptyMarker;

    if (idx == 7) {
      // We are down to 6 elements, so we release the upper half of the block
      release16BlockUpperHalf(blockIdx);
      return blockIdx | (Block8Tag << 29);
    }

    return handle;
  }

  int insertWith16Block(int blockIdx, int value, int handle, boolean[] inserted) {
    // a 16-slot standard block, which can contain between 7 and 16 entries
    int value15 = slots[blockIdx+15];
    if (value15 == EmptyMarker) {
      // The slot still contains some empty space
      for (int i=0 ; i < 16 ; i++) {
        int valueI = slots[blockIdx+i];
        if (value == valueI) {
          inserted[0] = false;
          return handle;
        }
        if (valueI == EmptyMarker) {
          slots[blockIdx+i] = value;
          inserted[0] = true;
          return handle;
        }
      }
      Miscellanea._assert(false); //## CONTROL FLOW CAN NEVER MAKE IT HERE...
    }

    // The block is full, if the new value is not a duplicate
    // we need to turn this block into a hashed one
    for (int i=0 ; i < 16 ; i++)
      if (value == slots[blockIdx+i]) {
        inserted[0] = false;
        return handle;
      }

    // Allocating and initializing the hashed block
    int hashedBlockIdx = alloc16Block();
    slots[hashedBlockIdx] = 0;
    for (int i=1 ; i < 16 ; i++)
      slots[hashedBlockIdx + i] = EmptyMarker;

    // Transferring the existing values
    for (int i=0 ; i < 16 ; i++) {
      int content = slots[blockIdx+i];
      insertIntoHashedBlock(hashedBlockIdx, content, hashCode(content), inserted);
      Miscellanea._assert(inserted[0]);
    }

    // Releasing the old block
    release16Block(blockIdx);

    // Adding the new value
    insertIntoHashedBlock(hashedBlockIdx, value, hashCode(value), inserted);
    Miscellanea._assert(inserted[0]);

    // Returning the tagged index of the block
    return hashedBlockIdx | (HashedBlockTag << 29);
  }

  int deleteFromHashedBlock(int blockIdx, int value, int handle, int hashcode, boolean[] deleted) {
    int slotIdx = blockIdx + hashcode % 15 + 1;
    int content = slots[slotIdx];
    if (content == EmptyMarker) {
      deleted[0] = false;
      return handle;
    }
    int tag = content >>> 29;
    Miscellanea._assert(tag <= 5);
    if (tag == 0) {
      if (content == value) {
        deleted[0] = true;
        slots[slotIdx] = EmptyMarker;
      }
      else {
        deleted[0] = false;
        return handle;
      }
    }
    else if (tag < 5) {
      int newHandle = delete(content, value, deleted);
      slots[slotIdx] = newHandle;
    }
    else {
      int nestedBlockIdx = content & PayloadMask;
      int newHandle = deleteFromHashedBlock(nestedBlockIdx, value, content, hashcode / 15, deleted);
      slots[slotIdx] = newHandle;
    }

    if (deleted[0]) {
      int count = slots[blockIdx] - 1;
      Miscellanea._assert(count >= 6);
      if (count == 6)
        return shrinkHashedBlock(blockIdx);
      slots[blockIdx] = count;
    }

    return handle;
  }

  int shrinkHashedBlock(int blockIdx) {
    int slot1  = slots[blockIdx + 1];
    int slot2  = slots[blockIdx + 2];
    int slot3  = slots[blockIdx + 3];
    int slot4  = slots[blockIdx + 4];
    int slot5  = slots[blockIdx + 5];

    int nextIdx = copyAndReleaseBlock(slot1, (int) blockIdx);
    nextIdx = copyAndReleaseBlock(slot2, nextIdx);
    nextIdx = copyAndReleaseBlock(slot3, nextIdx);
    nextIdx = copyAndReleaseBlock(slot4, nextIdx);
    nextIdx = copyAndReleaseBlock(slot5, nextIdx);

    int endIdx = blockIdx + 6;
    for (int i=6 ; nextIdx < endIdx ; i++) {
      Miscellanea._assert(i < 16);
      nextIdx = copyAndReleaseBlock(slots[blockIdx + i], nextIdx);
    }

    slots[blockIdx + 6] = EmptyMarker;
    slots[blockIdx + 7] = EmptyMarker;

    release16BlockUpperHalf(blockIdx);
    return blockIdx | (Block8Tag << 29);
  }

  int copyAndReleaseBlock(int handle, int nextIdx) {
    if (handle != EmptyMarker) {
      int tag = handle >>> 29;
      int blockIdx = handle & PayloadMask;
      Miscellanea._assert(((tag << 29) | blockIdx) == handle, "((tag << 29) | blockIdx) == handle");

      switch (tag) {
        case 0: // Inline
          slots[nextIdx++] = handle;
          break;

        case 1: // 2-block slot
          slots[nextIdx++] = slots[blockIdx];
          slots[nextIdx++] = slots[blockIdx + 1];
          release2Block(blockIdx);
          break;

        case 2: // 4-block slot
          nextIdx = copyNonEmpty(blockIdx, 4, slots, nextIdx);
          release4Block(blockIdx);
          break;

        case 3: // 8-block slot
          nextIdx = copyNonEmpty(blockIdx, 8, slots, nextIdx);
          release8Block(blockIdx);
          break;

        // case 4: // 16-slot block
        //   nextIdx = copyNonEmpty(blockIdx, 16, slots, nextIdx);
        //   break;

        // case 5: // Hashed block
        //   nextIdx = copyHashedBlock(blockIdx, slots, nextIdx);
        //   break;

        default:
          throw Miscellanea.internalFail(); // Control flow cannot get here
      }
    }

    return nextIdx;
  }


  void insertIntoHashedBlock(int blockIdx, int value, int hashcode, boolean[] inserted) {
    int slotIdx = blockIdx + hashcode % 15 + 1;
    int content = slots[slotIdx];
    if (content == EmptyMarker) {
      slots[slotIdx] = value;
      slots[blockIdx]++;
      inserted[0] = true;
    }
    else {
      int tag = content >>> 29;
      Miscellanea._assert(tag <= 5);
      if (tag < 5) {
        int newHandle = insert(content, value, inserted);
        slots[slotIdx] = newHandle;
      }
      else
        insertIntoHashedBlock(content & PayloadMask, value, hashcode / 15, inserted);
      if (inserted[0])
        slots[blockIdx]++;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  int hashCode(int value) {
    return value;
  }

  ////////////////////////////////////////////////////////////////////////////

  int alloc2Block() {
    if (head2 != EmptyMarker) {
      Miscellanea._assert(slots[head2] == EndLowerMarker);
      Miscellanea._assert(slots[head2+1] == End2UpperMarker || slots[head2+1] >>> 29 == Block2Tag);

      int blockIdx = head2;
      head2 = removeBlockFromChain(blockIdx, EndLowerMarker, End2UpperMarker, head2);
      return blockIdx;
    }
    else {
      int block4Idx = alloc4Block();
      head2 = addBlockToChain(block4Idx, Block2Tag, End2UpperMarker, head2);
      return block4Idx + 2;
    }
  }

  void release2Block(int blockIdx) {
    Miscellanea._assert((blockIdx & 1) == 0);

    boolean isFirst = (blockIdx & 3) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 2 : -2);
    int otherBlockSlot0 = slots[otherBlockIdx];

    if (otherBlockSlot0 >>> 29 == AvailableTag) {
      Miscellanea._assert(slots[otherBlockIdx+1] >>> 29 == Block2Tag);

      // The matching block is available, so we release both at once as a 4-slot block
      // But first we have to remove the matching block from the 2-slot block chain
      head2 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, End2UpperMarker, head2);
      release4Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else {
      // The matching block is not available, so we
      // just add the new one to the 2-slot block chain
      head2 = addBlockToChain(blockIdx, Block2Tag, End2UpperMarker, head2);
    }
  }

  int alloc4Block() {
    if (head4 != EmptyMarker) {
      Miscellanea._assert(slots[head4] == EndLowerMarker);
      Miscellanea._assert(slots[head4+1] == End4UpperMarker | slots[head4+1] >>> 29 == Block4Tag);

      int blockIdx = head4;
      head4 = removeBlockFromChain(blockIdx, EndLowerMarker, End4UpperMarker, head4);
      return blockIdx;
    }
    else {
      int block8Idx = alloc8Block();
      head4 = addBlockToChain(block8Idx, Block4Tag, End4UpperMarker, head4);
      return block8Idx + 4;
    }
  }

  void release4Block(int blockIdx) {
    Miscellanea._assert((blockIdx & 3) == 0);

    boolean isFirst = (blockIdx & 7) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 4 : -4);
    int otherBlockSlot0 = slots[otherBlockIdx];
    int otherBlockSlot1 = slots[otherBlockIdx+1];

    if (otherBlockSlot0 >>> 29 == AvailableTag & otherBlockSlot1 >>> 29 == Block4Tag) {
      head4 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, End4UpperMarker, head4);
      release8Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else
      head4 = addBlockToChain(blockIdx, Block4Tag, End4UpperMarker, head4);
  }

  int alloc8Block() {
    if (head8 != EmptyMarker) {
      Miscellanea._assert(slots[head8] == EndLowerMarker);
      Miscellanea._assert(slots[head8+1] == End8UpperMarker | slots[head8+1] >>> 29 == Block8Tag);

      int blockIdx = head8;
      head8 = removeBlockFromChain(blockIdx, EndLowerMarker, End8UpperMarker, head8);
      return blockIdx;
    }
    else {
      int block16Idx = alloc16Block();
      Miscellanea._assert(slots[block16Idx] == EndLowerMarker);
      Miscellanea._assert(slots[block16Idx+1] == End16UpperMarker | slots[block16Idx+1] >>> 29 == Block16Tag);
      head8 = addBlockToChain(block16Idx, Block8Tag, End8UpperMarker, head8);
      return block16Idx + 8;
    }
  }

  void release8Block(int blockIdx) {
    Miscellanea._assert((blockIdx & 7) == 0);

    boolean isFirst = (blockIdx & 15) == 0;
    int otherBlockIdx = blockIdx + (isFirst ? 8 : -8);
    int otherBlockSlot0 = slots[otherBlockIdx];
    int otherBlockSlot1 = slots[otherBlockIdx+1];

    if (otherBlockSlot0 >>> 29 == AvailableTag & otherBlockSlot1 >>> 29 == Block8Tag) {
      head8 = removeBlockFromChain(otherBlockIdx, otherBlockSlot0, End8UpperMarker, head8);
      release16Block(isFirst ? blockIdx : otherBlockIdx);
    }
    else
      head8 = addBlockToChain(blockIdx, Block8Tag, End8UpperMarker, head8);
  }

  void release8BlockUpperHalf(int blockIdx) {
    head4 = addBlockToChain(blockIdx+4, Block4Tag, End4UpperMarker, head4);
  }

  int alloc16Block() {
    if (head16 == EmptyMarker) {
      int len = slots.length;
      int[] newSlots = new int[2*len];
      Miscellanea.arrayCopy(slots, newSlots, len);
      for (int i=len ; i < 2 * len ; i += 16) {
        newSlots[i]   = (i - 16) | AvailableTag << 29;
        newSlots[i+1] = (i + 16) | Block16Tag << 29;
      }
      newSlots[len] = EndLowerMarker;
      newSlots[2*len - 16 + 1] = End16UpperMarker;
      slots = newSlots;
      head16 = len;
    }

    Miscellanea._assert(slots[head16] == EndLowerMarker);
    Miscellanea._assert(slots[head16+1] == End16UpperMarker | slots[head16+1] >>> 29 == Block16Tag);

    int blockIdx = head16;
    head16 = removeBlockFromChain(blockIdx, EndLowerMarker, End16UpperMarker, head16);
    return blockIdx;
  }

  void release16Block(int blockIdx) {
    head16 = addBlockToChain(blockIdx, Block16Tag, End16UpperMarker, head16);
  }

  void release16BlockUpperHalf(int blockIdx) {
    head8 = addBlockToChain(blockIdx+8, Block8Tag, End8UpperMarker, head8);
  }

  ////////////////////////////////////////////////////////////////////////////

  int removeBlockFromChain(int blockIdx, int slot0, int endUpperMarker, int head) {
    int slot1 = slots[blockIdx + 1];

    if (slot0 != EndLowerMarker) {
      // Not the first block in the chain
      Miscellanea._assert(head != blockIdx);
      int prevBlockIdx = slot0 & PayloadMask;

      if (slot1 != endUpperMarker) {
        // The block is in the middle of the chain
        // The previous and next blocks must be repointed to each other
        int nextBlockIdx = slot1 & PayloadMask;
        slots[prevBlockIdx+1] = slot1;
        slots[nextBlockIdx]   = slot0;
      }
      else {
        // Last block in a chain with multiple blocks
        // The 'next' field of the previous block must be cleared
        slots[prevBlockIdx+1] = endUpperMarker;
      }
    }
    else {
      // First slot in the chain, must be the one pointed to by head
      Miscellanea._assert(head == blockIdx);

      if (slot1 != endUpperMarker) {
        // The head must be repointed at the next block,
        // whose 'previous' field must now be cleared
        int nextBlockIdx = slot1 & PayloadMask;
        head = nextBlockIdx;
        slots[nextBlockIdx] = EndLowerMarker;
      }
      else {
        // No 'previous' nor 'next' slots, it must be the only one
        // Just resetting the head of the 2-slot block chain
        head = EmptyMarker;
      }
    }

    return head;
  }

  int addBlockToChain(int blockIdx, int sizeTag, int endUpperMarker, int head) {
    // The 'previous' field of the newly released block must be cleared
    slots[blockIdx] = EndLowerMarker;
    if (head != EmptyMarker) {
      // If the list of blocks is not empty, we link the first two blocks
      slots[blockIdx+1] = head | sizeTag << 29;
      slots[head] = blockIdx | AvailableTag << 29;
    }
    else {
      // Otherwise we just clear then 'next' field of the newly released block
      slots[blockIdx+1] = endUpperMarker;
    }
    // The new block becomes the head one
    return blockIdx;
  }
}
