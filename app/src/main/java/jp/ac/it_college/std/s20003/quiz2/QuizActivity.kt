package jp.ac.it_college.std.s20003.quiz2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.core.os.HandlerCompat
import jp.ac.it_college.std.s20003.quiz2.databinding.ActivityQuizBinding
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.net.URL as URL

class QuizActivity : AppCompatActivity() {
    companion object {
        private const val DEBUG_TAG = "Quiz2"
        private const val QUIZDATA_URL =
            "https://script.google.com/macros/s/AKfycbznWpk2m8q6lbLWSS6qaz3uS6j3L4zPwv7CqDEiC433YOgAdaFekGJmjoAO60quMg6l/exec?f="
    }
    private lateinit var binding: ActivityQuizBinding
    /*
    private var quizId = -1
    private var quizName = ""
     */
    private var helper = DatabaseHelper(this)
    private var ansCnt: Int = 0
    private var qusCnt: Int = 0
    private var idData: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiveQuizData("${QUIZDATA_URL}data")

        binding.nextButton.isEnabled = false
        timer.start()
        binding.nextButton.setOnClickListener {
            next()
            timer.start()
        }
    }

    private val timer: CountDownTimer =
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second = millisUntilFinished / 1000L % 60L
                binding.timer.text = second.toString()
            }

            override fun onFinish() {
                binding.nextButton.isEnabled = true
                AlertDialog.Builder(this@QuizActivity)
                    .setTitle("時間切れ")
                    .setPositiveButton("OK", null)
                    .show()

                binding.button1.isEnabled = false
                binding.button2.isEnabled = false
                binding.button3.isEnabled = false
                binding.button4.isEnabled = false
                binding.button5.isEnabled = false
                binding.button6.isEnabled = false
            }
        }



    @Suppress("SameParameterValue")
    private fun receiveQuizData(urlFull: String) {
        val handler = HandlerCompat.createAsync(mainLooper)
        val executeService = Executors.newSingleThreadExecutor()

        executeService.submit @WorkerThread {
            var result = ""
            val url = URL(urlFull)
            val con = url.openConnection() as? HttpURLConnection

            con?.let {
                try {
                    it.connectTimeout = 20000
                    it.readTimeout = 20000
                    it.requestMethod = "GET"
                    it.connect()

                    val stream = it.inputStream
                    result = is2String(stream)
                    stream.close()
                } catch (e: SocketTimeoutException) {
                    Log.w(DEBUG_TAG, "通信タイムアウト")
                } catch (e: Exception) {
                    Log.w(DEBUG_TAG, "例外")
                }
                it.disconnect()
            }

            handler.post @UiThread {
                val rootJson = JSONArray(result)
                helper = DatabaseHelper(this)

                val db = helper.writableDatabase

                val sqlDelete = """
                    DELETE FROM quizData;
                """.trimIndent()
                var stmt = db.compileStatement(sqlDelete)
                stmt.executeUpdateDelete()

                val sqlInsert = """
                    INSERT INTO quizData
                    (_id, question, answer, choice1, choice2, choice3, choice4, choice5, choice6)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                    """.trimIndent()
                stmt = db.compileStatement(sqlInsert)

                val len = rootJson.length()
                for (i in 0 until len) {
                    val quizJson = rootJson.getJSONObject(i)
                    val questionJson = quizJson.getString("question")
                    val answersJson = quizJson.getLong("answers")
                    val choicesJson = quizJson.getJSONArray("choices")
                    val choice1 = choicesJson.getString(0)
                    val choice2 = choicesJson.getString(1)
                    val choice3 = choicesJson.getString(2)
                    val choice4 = choicesJson.getString(3)
                    val choice5 = choicesJson.getString(4)
                    val choice6 = choicesJson.getString(5)

                    stmt.run {
                        bindLong(1, i.toLong())
                        bindString(2, questionJson)
                        bindLong(3, answersJson)
                        bindString(4, choice1)
                        bindString(5, choice2)
                        bindString(6, choice3)
                        bindString(7, choice4)
                        bindString(8, choice5)
                        bindString(9, choice6)
                    }
                }
            }
        }
    }

    private fun is2String(stream: InputStream?): String {
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        return reader.readText()
    }

    private fun next() {
        qusCnt++
        if (qusCnt == 10) {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("ANSWER", ansCnt)
            startActivity(intent)
        } else {
            binding.button1.isEnabled = true
            binding.button2.isEnabled = true
            binding.button3.isEnabled = true
            binding.button4.isEnabled = true
            binding.button5.isEnabled = true
            binding.button6.isEnabled = true
            binding.nextButton.isEnabled = false
            idData = (0L..149).random()
            setQuizData(idData)
        }
    }

    @SuppressLint("Recycle")
    private fun setQuizData(s: Long) {
        val db = helper.readableDatabase
        val sqlSelect = """
            SELECT * FROM quizData
            WHERE _id = $s;
        """.trimIndent()
        val cursor = db.rawQuery(sqlSelect, null)
        while (cursor.moveToNext()) {
            val question = cursor.let {
                val result = it.getColumnIndex("question")
                it.getString(result)
            }
            val answers = cursor.let {
                val result = it.getColumnIndex("answer")
                it.getLong(result)
            }
            val choice1 = cursor.let {
                val result = it.getColumnIndex("choice1")
                it.getString(result)
            }
            val choice2 = cursor.let {
                val result = it.getColumnIndex("choice2")
                it.getString(result)
            }
            val choice3 = cursor.let {
                val result = it.getColumnIndex("choice3")
                it.getString(result)
            }
            val choice4 = cursor.let {
                val result = it.getColumnIndex("choice4")
                it.getString(result)
            }
            val choice5 = cursor.let {
                val result = it.getColumnIndex("choice5")
                it.getString(result)
            }
            val choice6 = cursor.let {
                val result = it.getColumnIndex("choice6")
                it.getString(result)
            }

            val array = arrayOf(choice1, choice2, choice3, choice4, choice5, choice6)
            val list: List<Int> = listOf(0, 1, 2, 3, 4, 5)
            val shuffle: List<Int> = list.shuffled()

            binding.question.text = question
            binding.button1.text = array[shuffle[0]]
            binding.button2.text = array[shuffle[1]]
            binding.button3.text = array[shuffle[2]]
            binding.button4.text = array[shuffle[3]]
            binding.button5.text = array[shuffle[4]]
            binding.button6.text = array[shuffle[5]]

            if (binding.button1.text.isEmpty()) {
                binding.button1.visibility = View.GONE
            }
            if (binding.button2.text.isEmpty()) {
                binding.button2.visibility = View.GONE
            }
            if (binding.button3.text.isEmpty()) {
                binding.button3.visibility = View.GONE
            }
            if (binding.button4.text.isEmpty()) {
                binding.button4.visibility = View.GONE
            }
            if (binding.button5.text.isEmpty()) {
                binding.button5.visibility = View.GONE
            }
            if (binding.button6.text.isEmpty()) {
                binding.button6.visibility = View.GONE
            }

            when (answers) {
                    1L -> {
                        binding.button1.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button1.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }

                        }
                        binding.button2.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button2.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }
                        }
                        binding.button3.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button3.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }
                        }
                        binding.button4.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button4.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }
                        }
                        binding.button5.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button5.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }
                        }
                        binding.button6.setOnClickListener {
                            timer.cancel()
                            binding.nextButton.isEnabled = true
                            if (binding.button6.text == choice1) {
                                correct()
                            } else {
                                incorrect()
                            }
                        }
                    }
                    2L -> {
                        var n = 2
                        binding.button1.setOnClickListener {
                            timer.cancel()
                            when (binding.button1.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }

                        }
                        binding.button2.setOnClickListener {
                            timer.cancel()
                            when (binding.button2.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }

                        }
                        binding.button3.setOnClickListener {
                            timer.cancel()
                            when (binding.button3.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }

                        }
                        binding.button4.setOnClickListener {
                            timer.cancel()
                            when (binding.button4.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }

                        }
                        binding.button5.setOnClickListener {
                            timer.cancel()
                            when (binding.button5.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }

                        }
                        binding.button6.setOnClickListener {
                            timer.cancel()
                            when (binding.button6.text) {
                                choice1 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                choice2 -> {
                                    when (n) {
                                        2 -> {
                                            n -= 1
                                            anotherQus()
                                        }
                                        else -> correct()
                                    }
                                }
                                else -> incorrect()
                            }
                        }
                    }
                }
        }
        cursor.close()

    }

    private fun correct() {
        ansCnt++
        AlertDialog.Builder(this)
            .setTitle("正解!!")
            .setPositiveButton("OK", null)
            .show()

        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false
        binding.button4.isEnabled = false
        binding.button5.isEnabled = false
        binding.button6.isEnabled = false
        binding.nextButton.isEnabled = true
    }

    private fun anotherQus() {
        AlertDialog.Builder(this)
            .setTitle("もう一問!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun incorrect() {
        AlertDialog.Builder(this)
            .setTitle("不正解...")
            .setPositiveButton("OK", null)
            .show()

        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false
        binding.button4.isEnabled = false
        binding.button5.isEnabled = false
        binding.button6.isEnabled = false
        binding.nextButton.isEnabled = true
    }

    override fun onDestroy() {
        helper.close()
        super.onDestroy()
    }

}
