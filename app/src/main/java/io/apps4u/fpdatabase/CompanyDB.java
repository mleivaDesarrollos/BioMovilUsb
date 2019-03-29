package io.apps4u.fpdatabase;

import android.provider.BaseColumns;

public class CompanyDB {
    public static abstract class Entry implements BaseColumns{
        public static final String TABLE_NAME = "Companys";
        public static final String ID = "ID";
        public static final String NAME = "NAME";
    }
}
