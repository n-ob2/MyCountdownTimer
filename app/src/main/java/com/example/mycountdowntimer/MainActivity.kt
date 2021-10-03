package com.example.mycountdowntimer

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.mycountdowntimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var soundPool: SoundPool   //SoundPoolクラスの変数を保持（後で初期化）
    private var soundResId = 0  //サウンドファイルのリソースID保持

    inner class MyCountDownTimer(millisInFuture: Long,  //CountDownTimerクラスを継承したクラスを作成
                                    countDownInterval: Long
    ): CountDownTimer(millisInFuture, countDownInterval) {
        var isRunnig = false    //カウントダウン中か停止中か

        override fun onTick(millisUnitFinished: Long) {  //残り時間をテキストビューに表示
            val minute = millisUnitFinished / 1000L / 60L
            val second = millisUnitFinished / 1000L % 60L
            binding.timerText.text = "%1d:%2$02d".format(minute, second)
        }

        override fun onFinish() {
            binding.timerText.text = "0:00" //カウントダウン0:00表示
            soundPool.play(soundResId, 1.0f, 100f, 0, 0, 1.0f)  //サウンドが鳴る
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.timerText.text = "3:00"
        var timer = MyCountDownTimer(3 * 60 * 1000,100) // CountDownTomerを継承したクラスのインスタンス生成
        binding.playStop.setOnClickListener {   //ボタンがタップされた時の処理↓↓
            timer.isRunnig = when (timer.isRunnig){
                true -> {   //カウントダウン中にタップされたら
                    timer.cancel()  //タイマーのリセット？
                    binding.playStop.setImageResource(
                        R.drawable.ic_baseline_play_arrow_24
                    )
                    false
                }
                false ->{   //停止中にタップされたら
                    timer.start()
                    binding.playStop.setImageResource(
                        R.drawable.ic_baseline_stop_24
                    )
                    true
                }
            }
        }

        binding.spinner.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener{ //spinnerのプロパティにオブジェクト式で無名インナークラスのインスタンスを設定

                override fun onItemSelected(    //項目が選択された時の処理
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    timer.cancel()  //ボタンのアイコン切り替え
                    binding.playStop.setImageResource(
                        R.drawable.ic_baseline_play_arrow_24
                    )
                    val spinner = parent as? Spinner    //強制的な型変換(キャスト)parentをspinner型へ
                    val item = spinner?.selectedItem as? String //選択された項目を定数に代入
                    item?.let{  //選択した項目を画面とタイマーに設定
                        if(it.isNotEmpty()) binding.timerText.text = it  //時間表示
                        val times = it.split(":")   //ミリ秒に変換
                        val min = times[0].toLong()
                        val sec = times[1].toLong()
                        timer = MyCountDownTimer((min * 60 + sec) * 1000, 100)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { } //項目が選択されずにスピナーが閉じられた時の処理
            }

        binding.seekBar.setOnSeekBarChangeListener( // シークバーの値を変更した時の処理
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,//ユーザーが設定した値
                    fromUser: Boolean
                ) {
                    timer.cancel()
                    binding.playStop.setImageResource(  //アイコンの画像を差し替え
                        R.drawable.ic_baseline_play_arrow_24
                    )
                    val min = progress / 60L    //ユーザーが設定した値を分秒の表示に変更
                    val sec = progress % 60L
                    binding.timerText.text = "%1d:%2$02d".format(min, sec)
                    timer = MyCountDownTimer(progress * 1000L, 100)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) { } //シークバーに触れた時
                override fun onStopTrackingTouch(p0: SeekBar?) { }  //シークバーを離した時
            }

        )
    }

    override fun onResume() {  //サウンドをロードしておくための処理
        super.onResume()
        soundPool =
           SoundPool. Builder().run{    //SoundPoolのインスタンスを作成
               val audioAttributes = AudioAttributes.Builder().run{ //audioAttributesクラスのインスタンスを用意
                   setUsage(AudioAttributes.USAGE_ALARM)    //オーディオの使用目的を明記
                   build()  //インスタンスを生成
               }
               setMaxStreams(1) //同時に鳴らす音の数を設定
               setAudioAttributes(audioAttributes)  //AudioAttributesを設定する引数に定数audioAttributesをセット
               build()  //インスタンス生成
           }
        soundResId = soundPool.load(this, R.raw.bellsound, 1)   //サウンドリソースをロード
    }

    override fun onPause() {   //サウンドのメモリ開放の処理
        super.onPause()
        soundPool.release()
    }
}