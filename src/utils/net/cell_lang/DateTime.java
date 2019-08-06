package net.cell_lang;


class DateTime {
  static int[] nonLeapYearDaysPerMonth = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  static int[] leapYearDaysPerMonth    = new int[] {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  private static int[] monthsOffsets = new int[] {-1, 30, 58, 89, 119, 150, 180, 211, 242, 272, 303, 333};

  static boolean isLeapYear(int year) {
    return ((year % 4 == 0) & (year % 100 != 0)) | (year % 400 == 0);
  }

  public static int[] getYearMonthDay(int days) {
    if (days >= 0) {
      int year = 1970;
      for ( ; ; ) {
        boolean isLeapYear = isLeapYear(year);
        int yearLen = isLeapYear ? 366 : 365;
        if (days < yearLen) {
          int[] daysPerMonth = isLeapYear ? leapYearDaysPerMonth : nonLeapYearDaysPerMonth;
          for (int monthIdx=0 ; monthIdx < 12 ; monthIdx++) {
            int monthLen = monthIdx == 1 & isLeapYear ? 29 : daysPerMonth[monthIdx];
            if (days < monthLen)
              return new int[] {year, monthIdx+1, days+1};
            days = days - monthLen;
          }
          throw Miscellanea.internalFail();
        }
        year = year + 1;
        days = days - yearLen;
      }
    }
    else {
      int year = 1969;
      for ( ; ; ) {
        boolean isLeapYear = isLeapYear(year);
        int yearLen = isLeapYear ? 366 : 365;
        days = days + yearLen;
        if (days >= 0) {
          int[] daysPerMonth = isLeapYear ? leapYearDaysPerMonth : nonLeapYearDaysPerMonth;
          for (int monthIdx=0 ; monthIdx < 12 ; monthIdx++) {
            int monthLen = monthIdx == 1 & isLeapYear ? 29 : daysPerMonth[monthIdx];
            if (days < monthLen)
              return new int[] {year, monthIdx+1, days+1};
            days = days - monthLen;
          }
          throw Miscellanea.internalFail();
        }
        year = year - 1;
      }
    }
  }

  public static boolean isValidDate(int year, int month, int day) {
    if (month <= 0 | month > 12 | day <= 0)
      return false;

    boolean isLeapYear = isLeapYear(year);

    if (month == 2 & day == 29)
      return isLeapYear;

    int[] daysPerMonth = isLeapYear ? leapYearDaysPerMonth : nonLeapYearDaysPerMonth;
    return day <= daysPerMonth[month-1];
  }

  public static boolean isWithinRange(int daysSinceEpoc, long dayTimeNs) {
    // Valid range is from 1677-09-21 00:12:43.145224192 to 2262-04-11 23:47:16.854775807 inclusive
    return (daysSinceEpoc >  -106752 & daysSinceEpoc < 106751) |
           (daysSinceEpoc == -106752 & dayTimeNs >= 763145224192L) |
           (daysSinceEpoc ==  106751 & dayTimeNs <= 85636854775807L);
  }

  public static int daysSinceEpoc(int year, int month, int day) {
    Miscellanea._assert(isValidDate(year, month, day));

    boolean isLeapYear = isLeapYear(year);

    int daysSinceYearStart;
    if (month == 2 & day == 29) {
      daysSinceYearStart = 59;
    }
    else {
      daysSinceYearStart = monthsOffsets[month-1];
      if (month > 2 & isLeapYear)
        daysSinceYearStart++;
    }

    int leapYears;
    if (year > 2000) {
      int delta2001 = year - 2001;
      leapYears = 8 + delta2001 / 4 - delta2001 / 100 + delta2001 / 400;
    }
    else {
      int delta2000 = 2000 - year;
      leapYears = 7 - delta2000 / 4 + delta2000 / 100 - delta2000 / 400;
    }

    int daysAtYearStart = 365 * (year - 1970) + leapYears;
    return daysAtYearStart + daysSinceYearStart;
  }

  public static long epocTimeNs(int daysSinceEpoc, long dayTimeNs) {
    return 86400000000000L * daysSinceEpoc + dayTimeNs;
  }
}