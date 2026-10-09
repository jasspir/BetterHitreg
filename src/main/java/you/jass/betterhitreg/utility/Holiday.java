package you.jass.betterhitreg.utility;

import java.time.LocalDate;

public class Holiday {
    private static LocalDate cachedDate;
    private static Palette cachedPalette;
    private static final String[] chineseNewYear = {
            "2000-02-05", "2001-01-24", "2002-02-12", "2003-02-01",
            "2004-01-22", "2005-02-09", "2006-01-29", "2007-02-18",
            "2008-02-07", "2009-01-26", "2010-02-14", "2011-02-03",
            "2012-01-23", "2013-02-10", "2014-01-31", "2015-02-19",
            "2016-02-08", "2017-01-28", "2018-02-16", "2019-02-05",
            "2020-01-25", "2021-02-12", "2022-02-01", "2023-01-22",
            "2024-02-10", "2025-01-29", "2026-02-17", "2027-02-06",
            "2028-01-26", "2029-02-13", "2030-02-03", "2031-01-23",
            "2032-02-11", "2033-01-31", "2034-02-19", "2035-02-08",
            "2036-01-28", "2037-02-15", "2038-02-04", "2039-01-24",
            "2040-02-12", "2041-02-01", "2042-01-22", "2043-02-10",
            "2044-01-30", "2045-02-17", "2046-02-06", "2047-01-26",
            "2048-02-14", "2049-02-02", "2050-01-23"
    };

    public static String hex(String role, String defaultHex) {
        Palette palette = currentPalette();
        if (palette == null) return defaultHex;
        String hex = palette.color(role);
        return hex == null ? defaultHex : hex;
    }

    public static synchronized Palette currentPalette() {
        LocalDate today = LocalDate.now();
        if (today.equals(cachedDate)) return cachedPalette;
        cachedDate = today;
        cachedPalette = detect(today);
        return cachedPalette;
    }

    private static Palette detect(LocalDate d) {
        int month = d.getMonthValue(), day = d.getDayOfMonth();
        if (month == 12 && day >= 20 && day <= 25) return Palette.CHRISTMAS;
        if (month == 10 && day >= 26) return Palette.HALLOWEEN;
        if (isChineseNewYear(d)) return Palette.CHINESE_NEW_YEAR;
        if (d.equals(easter(d.getYear()))) return Palette.EASTER;
        if (month == 1 && day == 1) return Palette.NEW_YEAR;
        return null;
    }

    private static boolean isChineseNewYear(LocalDate d) {
        int i = d.getYear() - 2000;
        return i >= 0 && i < chineseNewYear.length && d.equals(LocalDate.parse(chineseNewYear[i]));
    }

    private static LocalDate easter(int y) {
        int a = y % 19, b = y / 100, c = y % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3, h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451, n = h + l - 7 * m + 114;
        return LocalDate.of(y, n / 31, n % 31 + 1);
    }
}