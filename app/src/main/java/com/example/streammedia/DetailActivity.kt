package com.example.streammedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.streammedia.databinding.ActivityDetailBinding
import com.example.streammedia.databinding.ActivityMainBinding
import com.example.streammedia.fragment.AFragment

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        supportFragmentManager.beginTransaction()
//            .setReorderingAllowed(true)
//            .setCustomAnimations(
//                R.anim.slide_in_right,   // enter
//                R.anim.slide_out_left,   // exit
//                R.anim.slide_in_left,    // popEnter
//                R.anim.slide_out_right   // popExit
//            )
//            .replace(R.id.container, AFragment())
//            .addToBackStack(null)
//            .commit()
    }
}