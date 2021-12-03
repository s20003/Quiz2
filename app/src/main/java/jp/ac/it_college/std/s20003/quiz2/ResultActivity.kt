package jp.ac.it_college.std.s20003.quiz2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.ac.it_college.std.s20003.quiz2.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ansCnt = intent.getIntExtra("ANSWER", 0)
        val ansView = ansCnt.toString()

        binding.resultView.text = "$ansView / 10"

    }
}