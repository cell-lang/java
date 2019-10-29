package net.cell_lang;


// A slot can be in any of the following states:
//   - Single value:        32 ones                  - 3 zeros   - 29 bit value 1
//   - Two values:          3 zeros - 29 bit value 2 - 3 zeros   - 29 bit value 1
//   - Index + count:       32 bit count             - 3 bit tag - 29 bit index
//     This type of slot can only be stored in a hashed block or passed in and out
//   - Empty:               32 zeros                 - 32 ones
//     This type of slot can only be stored in a block, but cannot be passed in or out

class OverflowTable extends ArraySliceAllocator {
  public  final static int INLINE_SLOT    = 0;
  private final static int SIZE_2_BLOCK   = 1;
  private final static int SIZE_4_BLOCK   = 2;
  private final static int SIZE_8_BLOCK   = 3;
  private final static int SIZE_16_BLOCK  = 4;
  private final static int HASHED_BLOCK   = 5;

  private final static int SIZE_2_BLOCK_MIN_COUNT   = 3;
  private final static int SIZE_4_BLOCK_MIN_COUNT   = 4;
  private final static int SIZE_8_BLOCK_MIN_COUNT   = 7;
  private final static int SIZE_16_BLOCK_MIN_COUNT  = 13;
  private final static int HASHED_BLOCK_MIN_COUNT   = 13;

  public final static long EMPTY_SLOT = 0xFFFFFFFFL;

  //////////////////////////////////////////////////////////////////////////////

  public long insert(long handle, int value) {
    int low = low(handle);
    int tag = tag(low);

    if (tag == 0)
      return insert2Block(handle, value);

    if (tag == HASHED_BLOCK)
      return insertIntoHashedBlock(payload(low), count(handle), value);

    return insertWithLinearBlock(handle, value);
  }

  public long insertUnique(long handle, int value) {
    int low = low(handle);
    int tag = tag(low);

    if (tag == 0)
      return insert2Block(handle, value);

    if (tag == HASHED_BLOCK)
      return insertUniqueIntoHashedBlock(payload(low), count(handle), value);

    return insertUniqueWithLinearBlock(handle, value);
  }

  public long delete(long handle, int value) {
    int low = low(handle);
    int tag = tag(low);

    // Miscellanea._assert(tag != INLINE_SLOT);

    if (tag == HASHED_BLOCK)
      return deleteFromHashedBlock(payload(low), count(handle), value);
    else
      return deleteFromLinearBlock(handle, value);
  }

  public boolean contains(long handle, int value) {
    int tag = tag(low(handle));
    int blockIdx = payload(low(handle));

    // Miscellanea._assert(tag != INLINE_SLOT);
    // Miscellanea._assert(tag(tag, blockIdx) == low(handle));

    if (tag != HASHED_BLOCK)
      return linearBlockContains(blockIdx, count(handle), value);
    else
      return hashedBlockContains(blockIdx, value);
  }

  public void copy(long handle, int[] buffer) {
    copy(handle, buffer, 0, 1);
  }

  public void copy(long handle, int[] buffer, int offset, int step) {
    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);

    // Miscellanea._assert(tag != INLINE_SLOT);
    // Miscellanea._assert(tag(tag, blockIdx) == low(handle));

    if (tag != HASHED_BLOCK) {
      int count = count(handle);
      int end = (count + 1) / 2;
      int targetIdx = offset;

      for (int i=0 ; i < end ; i++) {
        long slot = slot(blockIdx + i);
        int slotLow = low(slot);
        int slotHigh = high(slot);

        // Miscellanea._assert(slotLow != EMPTY_MARKER & tag(slotLow) == INLINE_SLOT);

        buffer[targetIdx] = slotLow;
        targetIdx += step;

        if (slotHigh != EMPTY_MARKER) {
          // Miscellanea._assert(tag(slotHigh) == INLINE_SLOT);

          buffer[targetIdx] = slotHigh;
          targetIdx += step;
        }
      }
    }
    else
      copyHashedBlock(blockIdx, buffer, offset, step, 0, 0);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public static int count(long slot) {
    // Miscellanea._assert(tag(low(slot)) >= SIZE_2_BLOCK & tag(low(slot)) <= HASHED_BLOCK);
    // // Miscellanea._assert(high(slot) > 2); // Not true when initializing a hashed block
    return high(slot);
  }

  ////////////////////////////////////////////////////////////////////////////

  private static int capacity(int tag) {
    // Miscellanea._assert(tag >= SIZE_2_BLOCK & tag <= SIZE_16_BLOCK);
    // Miscellanea._assert(SIZE_2_BLOCK == 1 | SIZE_16_BLOCK == 4);
    return 2 << tag;
  }

  private static long linearBlockHandle(int tag, int index, int count) {
    return combine(tag(tag, index), count);
  }

  private static long size2BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_2_BLOCK_MIN_COUNT & count <= 4);
    return combine(tag(SIZE_2_BLOCK, index), count);
  }

  private static long size4BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_4_BLOCK_MIN_COUNT & count <= 8);
    return combine(tag(SIZE_4_BLOCK, index), count);
  }

  private static long size8BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_8_BLOCK_MIN_COUNT & count <= 16);
    return combine(tag(SIZE_8_BLOCK, index), count);
  }

  private static long size16BlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // Miscellanea._assert(count >= SIZE_16_BLOCK_MIN_COUNT & count <= 32);
    return combine(tag(SIZE_16_BLOCK, index), count);
  }

  private static long hashedBlockHandle(int index, int count) {
    // Miscellanea._assert(tag(index) == 0);
    // // Miscellanea._assert(count >= 7); // Not true when initializing a hashed block
    long handle = combine(tag(HASHED_BLOCK, index), count);
    // Miscellanea._assert(tag(low(handle)) == HASHED_BLOCK);
    // Miscellanea._assert(payload(low(handle)) == index);
    // Miscellanea._assert(count(handle) == count);
    return handle;
  }

  private static int index(int value) {
    // Miscellanea._assert(tag(value) == INLINE_SLOT);
    return value & 0xF;
  }

  private static int clipped(int value) {
    return value >>> 4;
  }

  private static int unclipped(int value, int index) {
    // Miscellanea._assert(tag(value) == 0);
    // Miscellanea._assert(tag(value << 4) == 0);
    // Miscellanea._assert(index >= 0 & index < 16);
    return (value << 4) | index;
  }

  private int minCount(int tag) {
    if (tag == SIZE_2_BLOCK)
      return SIZE_2_BLOCK_MIN_COUNT;

    if (tag == SIZE_4_BLOCK)
      return SIZE_4_BLOCK_MIN_COUNT;

    if (tag == SIZE_8_BLOCK)
      return SIZE_8_BLOCK_MIN_COUNT;

    // Miscellanea._assert(tag == SIZE_16_BLOCK | tag == HASHED_BLOCK);
    // Miscellanea._assert(SIZE_16_BLOCK_MIN_COUNT == HASHED_BLOCK_MIN_COUNT);

    return SIZE_16_BLOCK_MIN_COUNT; // Same as HASHED_BLOCK_MIN_COUNT
  }

  private static boolean isEven(int value) {
    return (value % 2) == 0;
  }

  ////////////////////////////////////////////////////////////////////////////

  private void markSlotAsEmpty(int index) {
    setSlot(index, EMPTY_MARKER, 0);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  private boolean linearBlockContains(int blockIdx, int count, int value) {
    int end = (count + 1) / 2;
    for (int i=0 ; i < end ; i++) {
      long slot = slot(blockIdx + i);
      if (value == low(slot) | value == high(slot))
        return true;
    }
    return false;
  }

  private boolean hashedBlockContains(int blockIdx, int value) {
    int slotIdx = blockIdx + index(value);
    long slot = slot(slotIdx);

    if (slot == EMPTY_SLOT)
      return false;

    int low = low(slot);
    int high = high(slot);
    int tag = tag(low);

    if (tag == 0)
      return value == low | value == high;

    if (tag == HASHED_BLOCK)
      return hashedBlockContains(payload(low), clipped(value));

    return contains(slot, clipped(value));
  }

  ////////////////////////////////////////////////////////////////////////////

  private int copyHashedBlock(int blockIdx, int[] dest, int offset, int step, int shift, int leastBits) {
    int subshift = shift + 4;
    int targetIdx = offset;

    for (int i=0 ; i < 16 ; i++) {
      int slotLeastBits = (i << shift) + leastBits;
      long slot = slot(blockIdx + i);
      int low = low(slot);

      if (low != EMPTY_MARKER) {
        int tag = tag(low);
        if (tag == INLINE_SLOT) {
          dest[targetIdx] = (payload(low) << shift) + leastBits;
          targetIdx += step;

          int high = high(slot);
          if (high != EMPTY_MARKER) {
            dest[targetIdx] = (payload(high) << shift) + leastBits;
            targetIdx += step;
          }
        }
        else if (tag == HASHED_BLOCK) {
          targetIdx = copyHashedBlock(payload(low), dest, targetIdx, step, subshift, slotLeastBits);
        }
        else {
          int subblockIdx = payload(low);
          int count = count(slot);
          int end = (count + 1) / 2;

          for (int j=0 ; j < end ; j++) {
            long subslot = slot(subblockIdx + j);
            int sublow = low(subslot);
            int subhigh = high(subslot);

            // Miscellanea._assert(sublow != EMPTY_MARKER & tag(sublow) == 0);

            dest[targetIdx] = (sublow << subshift) + slotLeastBits;
            targetIdx += step;

            if (subhigh != EMPTY_MARKER) {
              dest[targetIdx] = (subhigh << subshift) + slotLeastBits;
              targetIdx += step;
            }
          }
        }
      }
    }
    return targetIdx;
  }

  ////////////////////////////////////////////////////////////////////////////

  private long insert2Block(long handle, int value) {
    int low = low(handle);
    int high = high(handle);

    // Miscellanea._assert(tag(low) == 0 & tag(high) == 0);

    // Checking for duplicates
    if (low == value | high == value)
      return handle;

    int blockIdx = alloc2Block();
    setSlot(blockIdx,     low,   high);
    setSlot(blockIdx + 1, value, EMPTY_MARKER);
    return size2BlockHandle(blockIdx, 3);
  }

  private long insertWithLinearBlock(long handle, int value) {
    // Miscellanea._assert(tag(low(handle)) >= SIZE_2_BLOCK & tag(low(handle)) <= SIZE_16_BLOCK);

    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);
    int count = count(handle);
    int end = (count + 1) / 2;

    // Checking for duplicates and inserting if the next free block is a high one
    for (int i=0 ; i < end ; i++) {
      long slot = slot(blockIdx + i);
      int slotLow = low(slot);
      int slotHigh = high(slot);
      if (value == slotLow | value == slotHigh)
        return handle;
      if (slotHigh == EMPTY_MARKER) {
        setSlot(blockIdx + i, slotLow, value);
        return linearBlockHandle(tag, blockIdx, count + 1);
      }
    }

    int capacity = capacity(tag);

    // Inserting the new value if there's still room here
    // It can only be in a low slot
    if (count < capacity) {
      setSlot(blockIdx + end, value, EMPTY_MARKER);
      return linearBlockHandle(tag, blockIdx, count + 1);
    }

    if (tag != SIZE_16_BLOCK) {
      // Allocating the new block
      int newBlockIdx = tag == SIZE_2_BLOCK ? alloc4Block() : (tag == SIZE_4_BLOCK ? alloc8Block() : alloc16Block());

      // Initializing the new block
      int idx = count / 2;
      for (int i=0 ; i < idx ; i++)
        setFullSlot(newBlockIdx + i, slot(blockIdx + i));
      setSlot(newBlockIdx + idx, value, EMPTY_MARKER);
      for (int i=idx+1 ; i < count ; i++)
        markSlotAsEmpty(newBlockIdx + i);

      // Releasing the old block
      if (tag == SIZE_2_BLOCK)
        release2Block(blockIdx);
      else if (tag == SIZE_4_BLOCK)
        release4Block(blockIdx);
      else
        release8Block(blockIdx);

      return linearBlockHandle(tag + 1, newBlockIdx, count + 1);
    }

    // Allocating and initializing the hashed block
    int hashedBlockIdx = alloc16Block();
    for (int i=0 ; i < 16 ; i++)
      markSlotAsEmpty(hashedBlockIdx + i);

    // Transferring the existing values
    for (int i=0 ; i < 16 ; i++) {
      long slot = slot(blockIdx + i);
      long tempHandle = insertIntoHashedBlock(hashedBlockIdx, 2 * i, low(slot));
      // Miscellanea._assert(count(tempHandle) == 2 * i + 1);
      // Miscellanea._assert(payload(low(tempHandle)) == hashedBlockIdx);
      tempHandle = insertIntoHashedBlock(hashedBlockIdx, 2 * i + 1, high(slot));
      // Miscellanea._assert(count(tempHandle) == 2 * (i + 1));
      // Miscellanea._assert(payload(low(tempHandle)) == hashedBlockIdx);
    }

    // Releasing the old block
    release16Block(blockIdx);

    // Adding the new value
    return insertIntoHashedBlock(hashedBlockIdx, 32, value);
  }

  private long insertIntoHashedBlock(int blockIdx, int count, int value) {
    int slotIdx = blockIdx + index(value);
    long slot = slot(slotIdx);
    int low = low(slot);

    // Checking for empty slots
    if (low == EMPTY_MARKER) {
      setSlot(slotIdx, value, EMPTY_MARKER);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    int tag = tag(low);

    // Checking for inline slots
    if (tag == INLINE_SLOT) {
      if (value == low)
        return hashedBlockHandle(blockIdx, count);
      int high = high(slot);
      if (high == EMPTY_MARKER) {
        setSlot(slotIdx, low, value);
        return hashedBlockHandle(blockIdx, count + 1);
      }
      // Miscellanea._assert(tag(high) == INLINE_SLOT);
      if (value == high)
        return hashedBlockHandle(blockIdx, count);
      long handle = insert2Block(combine(clipped(low), clipped(high)), clipped(value));
      // Miscellanea._assert(count(handle) == 3);
      setFullSlot(slotIdx, handle);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    // The slot is not an inline one. Inserting the clipped value into the subblock

    long handle;
    if (tag == HASHED_BLOCK)
      handle = insertIntoHashedBlock(payload(low), count(slot), clipped(value));
    else
      handle = insertWithLinearBlock(slot, clipped(value));

    if (handle == slot)
      return hashedBlockHandle(blockIdx, count);

    // Miscellanea._assert(count(handle) == count(slot) + 1);
    setFullSlot(slotIdx, handle);
    return hashedBlockHandle(blockIdx, count + 1);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long insertUniqueWithLinearBlock(long handle, int value) {
    // Miscellanea._assert(tag(low(handle)) >= SIZE_2_BLOCK & tag(low(handle)) <= SIZE_16_BLOCK);

    int low = low(handle);
    int tag = tag(low);
    int blockIdx = payload(low);
    int count = count(handle);
    int capacity = capacity(tag);

    // Inserting the new value if there's still room here
    if (count < capacity) {
      int slotIdx = blockIdx + count / 2;
      if (isEven(count))
        setSlot(slotIdx, value, EMPTY_MARKER);
      else
        setSlotHigh(slotIdx, value);
      return linearBlockHandle(tag, blockIdx, count + 1);
    }

    if (tag != SIZE_16_BLOCK) {
      // Allocating the new block
      int newBlockIdx = tag == SIZE_2_BLOCK ? alloc4Block() : (tag == SIZE_4_BLOCK ? alloc8Block() : alloc16Block());

      // Initializing the new block
      int idx = count / 2;
      for (int i=0 ; i < idx ; i++)
        setFullSlot(newBlockIdx + i, slot(blockIdx + i));
      setSlot(newBlockIdx + idx, value, EMPTY_MARKER);
      for (int i=idx+1 ; i < count ; i++)
        markSlotAsEmpty(newBlockIdx + i);

      // Releasing the old block
      if (tag == SIZE_2_BLOCK)
        release2Block(blockIdx);
      else if (tag == SIZE_4_BLOCK)
        release4Block(blockIdx);
      else
        release8Block(blockIdx);

      return linearBlockHandle(tag + 1, newBlockIdx, count + 1);
    }

    // Allocating and initializing the hashed block
    int hashedBlockIdx = alloc16Block();
    for (int i=0 ; i < 16 ; i++)
      markSlotAsEmpty(hashedBlockIdx + i);

    // Transferring the existing values
    for (int i=0 ; i < 16 ; i++) {
      long slot = slot(blockIdx + i);
      long tempHandle = insertUniqueIntoHashedBlock(hashedBlockIdx, 2 * i, low(slot));
      // Miscellanea._assert(count(tempHandle) == 2 * i + 1);
      // Miscellanea._assert(payload(low(tempHandle)) == hashedBlockIdx);
      tempHandle = insertUniqueIntoHashedBlock(hashedBlockIdx, 2 * i + 1, high(slot));
      // Miscellanea._assert(count(tempHandle) == 2 * (i + 1));
      // Miscellanea._assert(payload(low(tempHandle)) == hashedBlockIdx);
    }

    // Releasing the old block
    release16Block(blockIdx);

    // Adding the new value
    return insertUniqueIntoHashedBlock(hashedBlockIdx, 32, value);
  }

  private long insertUniqueIntoHashedBlock(int blockIdx, int count, int value) {
    int slotIdx = blockIdx + index(value);
    long slot = slot(slotIdx);
    int low = low(slot);

    // Checking for empty slots
    if (low == EMPTY_MARKER) {
      setSlot(slotIdx, value, EMPTY_MARKER);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    int tag = tag(low);

    // Checking for inline slots
    if (tag == INLINE_SLOT) {
      // Miscellanea._assert(value != low);
      int high = high(slot);
      if (high == EMPTY_MARKER) {
        setSlot(slotIdx, low, value);
        return hashedBlockHandle(blockIdx, count + 1);
      }
      // Miscellanea._assert(tag(high) == INLINE_SLOT);
      // Miscellanea._assert(value != high);
      long handle = insert2Block(combine(clipped(low), clipped(high)), clipped(value));
      // Miscellanea._assert(count(handle) == 3);
      setFullSlot(slotIdx, handle);
      return hashedBlockHandle(blockIdx, count + 1);
    }

    // The slot is not an inline one. Inserting the clipped value into the subblock

    long handle;
    if (tag == HASHED_BLOCK)
      handle = insertUniqueIntoHashedBlock(payload(low), count(slot), clipped(value));
    else
      handle = insertUniqueWithLinearBlock(slot, clipped(value));

    // Miscellanea._assert(count(handle) == count(slot) + 1);
    setFullSlot(slotIdx, handle);
    return hashedBlockHandle(blockIdx, count + 1);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long deleteFromLinearBlock(long handle, int value) {
    int tag = tag(low(handle));
    int blockIdx = payload(low(handle));
    int count = count(handle);

    int lastSlotIdx = (count + 1) / 2 - 1;
    long lastSlot = slot(blockIdx + lastSlotIdx);

    int lastLow = low(lastSlot);
    int lastHigh = high(lastSlot);

    // Miscellanea._assert(lastLow != EMPTY_MARKER && tag(lastLow) == INLINE_SLOT);
    // Miscellanea._assert(
    //   (lastHigh != EMPTY_MARKER && tag(lastHigh) == INLINE_SLOT) ||
    //   (lastHigh == EMPTY_MARKER && !isEven(count))
    // );

    // Checking the last slot first
    if (value == lastLow | value == lastHigh) {
      // Removing the value
      if (value == lastLow)
        setSlot(blockIdx + lastSlotIdx, lastHigh, EMPTY_MARKER);
      else
        setSlot(blockIdx + lastSlotIdx, lastLow, EMPTY_MARKER);

      // Shrinking the block if need be
      if (count == minCount(tag))
        return shrinkLinearBlock(tag, blockIdx, count - 1);
      else
        return linearBlockHandle(tag, blockIdx, count - 1);
    }

    // The last slot didn't contain the searched value, looking in the rest of the array
    for (int i = lastSlotIdx - 1 ; i >= 0 ; i--) {
      long slot = slot(blockIdx + i);
      int low = low(slot);
      int high = high(slot);

      // Miscellanea._assert(low != EMPTY_MARKER && tag(low) == INLINE_SLOT);
      // Miscellanea._assert(high != EMPTY_MARKER && tag(high) == INLINE_SLOT);

      if (value == low | value == high) {
        // Removing the last value to replace the one being deleted
        int last;
        if (isEven(count)) {
          last = lastHigh;
          setSlot(blockIdx + lastSlotIdx, lastLow, EMPTY_MARKER);
        }
        else {
          last = lastLow;
          markSlotAsEmpty(blockIdx + lastSlotIdx);
        }

        // Replacing the value to be deleted with the last one
        if (value == low)
          setSlot(blockIdx + i, last, high);
        else
          setSlot(blockIdx + i, low, last);

        // Shrinking the block if need be
        if (count == minCount(tag))
          return shrinkLinearBlock(tag, blockIdx, count - 1);
        else
          return linearBlockHandle(tag, blockIdx, count - 1);
      }
    }

    // Value not found
    return handle;
  }

  private long shrinkLinearBlock(int tag, int blockIdx, int count) {
    if (tag == SIZE_2_BLOCK) {
      // Miscellanea._assert(count == 2);
      return slot(blockIdx);
    }

    if (tag == SIZE_4_BLOCK) {
      // Miscellanea._assert(count == 3);
      int block2Idx = alloc2Block();
      setFullSlot(block2Idx, slot(blockIdx));
      setFullSlot(block2Idx + 1, slot(blockIdx + 1));
      release4Block(blockIdx);
      return size2BlockHandle(block2Idx, count);
    }

    if (tag == SIZE_8_BLOCK) {
      // Miscellanea._assert(count == 6);
      release8BlockUpperHalf(blockIdx);
      return size4BlockHandle(blockIdx, count);
    }

    // Miscellanea._assert(tag == SIZE_16_BLOCK);
    // Miscellanea._assert(count == 12);
    release16BlockUpperHalf(blockIdx);
    return size8BlockHandle(blockIdx, count);
  }

  ////////////////////////////////////////////////////////////////////////////

  private long deleteFromHashedBlock(int blockIdx, int count, int value) {
    int index = index(value);
    int slotIdx = blockIdx + index;
    long slot = slot(slotIdx);

    // If the slot is empty there's nothing to do
    if (slot == EMPTY_SLOT)
      return hashedBlockHandle(blockIdx, count);

    int low = low(slot);
    int high = high(slot);

    // If the slot is not inline, we recursively call delete(..) with a clipped value
    if (tag(low) != INLINE_SLOT) {
      long handle = delete(slot, clipped(value));
      if (handle == slot)
        return hashedBlockHandle(blockIdx, count);
      int handleLow = low(handle);
      if (tag(handleLow) == INLINE_SLOT)
        handle = combine(unclipped(handleLow, index), unclipped(high(handle), index));
      setFullSlot(slotIdx, handle);
    }
    else if (low == value) {
      if (high == EMPTY_MARKER)
        markSlotAsEmpty(slotIdx);
      else
        setSlot(slotIdx, high, EMPTY_MARKER);
    }
    else if (high == value) {
      // Miscellanea._assert(high != EMPTY_MARKER);
      setSlot(slotIdx, low, EMPTY_MARKER);
    }
    else {
      return hashedBlockHandle(blockIdx, count);
    }

    // Miscellanea._assert(count >= HASHED_BLOCK_MIN_COUNT);

    // The value has actually been deleted. Shrinking the block if need be
    if (count > HASHED_BLOCK_MIN_COUNT)
      return hashedBlockHandle(blockIdx, count - 1);
    else
      return shrinkHashedBlock(blockIdx);
  }

  private long shrinkHashedBlock(int blockIdx) {
    // Miscellanea._assert(HASHED_BLOCK_MIN_COUNT == 13);

    // Here we've exactly 12 elements left, therefore we need the save the first 6 slots
    long slot0  = slot(blockIdx);
    long slot1  = slot(blockIdx + 1);
    long slot2  = slot(blockIdx + 2);
    long slot3  = slot(blockIdx + 3);
    long slot4  = slot(blockIdx + 4);
    long slot5  = slot(blockIdx + 5);

    long state = combine(blockIdx, EMPTY_MARKER);
    state = copyAndReleaseBlock(slot0, state, 0);
    state = copyAndReleaseBlock(slot1, state, 1);
    state = copyAndReleaseBlock(slot2, state, 2);
    state = copyAndReleaseBlock(slot3, state, 3);
    state = copyAndReleaseBlock(slot4, state, 4);
    state = copyAndReleaseBlock(slot5, state, 5);

    int endIdx = blockIdx + 6;
    for (int i=6 ; low(state) < endIdx ; i++)
      state = copyAndReleaseBlock(slot(blockIdx + i), state, i);

    // Miscellanea._assert(state == combine(blockIdx + 6, EMPTY_MARKER));

    markSlotAsEmpty(blockIdx + 6);
    markSlotAsEmpty(blockIdx + 7);

    release16BlockUpperHalf(blockIdx);
    return size8BlockHandle(blockIdx, 12);
  }

  private long copyAndReleaseBlock(long handle, long state, int leastBits) {
    if (handle == EMPTY_SLOT)
      return state;

    int low = low(handle);
    int tag = tag(low);

    int nextIdx = low(state);
    int leftover = high(state);

    if (tag == INLINE_SLOT) {
      int high = high(handle);

      if (leftover != EMPTY_MARKER) {
        setSlot(nextIdx++, leftover, low);
        leftover = EMPTY_MARKER;
      }
      else
        leftover = low;

      if (high != EMPTY_MARKER)
        if (leftover != EMPTY_MARKER) {
          setSlot(nextIdx++, leftover, high);
          leftover = EMPTY_MARKER;
        }
        else
          leftover = high;
    }
    else {
      int blockIdx = payload(low);
      int end = (count(handle) + 1) / 2;

      for (int i=0 ; i < end ; i++) {
        long slot = slot(blockIdx + i);

        // Miscellanea._assert(slot != EMPTY_SLOT);

        int slotLow = low(slot);
        int slotHigh = high(slot);

        if (leftover != EMPTY_MARKER) {
          setSlot(nextIdx++, leftover, unclipped(slotLow, leastBits));
          leftover = EMPTY_MARKER;
        }
        else
          leftover = unclipped(slotLow, leastBits);

        if (slotHigh != EMPTY_MARKER) {
          if (leftover != EMPTY_MARKER) {
            setSlot(nextIdx++, leftover, unclipped(slotHigh, leastBits));
            leftover = EMPTY_MARKER;
          }
          else
            leftover = unclipped(slotHigh, leastBits);
        }
      }

      if (tag == SIZE_2_BLOCK) {
        release2Block(blockIdx);
      }
      else if (tag == SIZE_4_BLOCK) {
        release4Block(blockIdx);
      }
      else {
        // Both 16-slot and hashed blocks contain at least 7 elements, so they cannot appear
        // here, as the parent hashed block being shrunk has only six elements left
        // Miscellanea._assert(tag == SIZE_8_BLOCK);
        release8Block(blockIdx);
      }
    }

    return combine(nextIdx, leftover);
  }
}
