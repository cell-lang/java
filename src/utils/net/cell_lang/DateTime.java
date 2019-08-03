package net.cell_lang;


class DateTime {
  static int[] nonLeapYearDaysPerMonth = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
  static int[] leapYearDaysPerMonth    = new int[] {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


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
}