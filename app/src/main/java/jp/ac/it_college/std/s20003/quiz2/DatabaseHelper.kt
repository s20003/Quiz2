package jp.ac.it_college.std.s20003.quiz2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "quizdata.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLES quizdata (
                _id      INTEGER PRIMARY KEY,
                qustion  TEXT,
                answer   LONG,
                choice1  TEXT,
                choice2  TEXT,
                choice3  TEXT,
                choice4  TEXT,
                choice5  TEXT,
                choice6  TEXT,
            );
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) = Unit
}