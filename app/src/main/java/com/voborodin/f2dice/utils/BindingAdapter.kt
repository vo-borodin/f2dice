package com.voborodin.f2dice.utils


import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.voborodin.f2dice.R

@BindingAdapter("app:imageResource")
fun setResultImage(imageView: ImageView, result: String?) {
    result?.let {
       when(result){
           "2"->imageView.setImageResource(R.drawable.ic_group_2)
           "3"->imageView.setImageResource(R.drawable.ic_group_3)
           "4"->imageView.setImageResource(R.drawable.ic_group_4)
           "5"->imageView.setImageResource(R.drawable.ic_group_5)
           "6"->imageView.setImageResource(R.drawable.ic_group_6)
           "7"->imageView.setImageResource(R.drawable.ic_group_7)
           "8"->imageView.setImageResource(R.drawable.ic_group_8)
           "9"->imageView.setImageResource(R.drawable.ic_group_9)
           "10"->imageView.setImageResource(R.drawable.ic_group_10)
           "11"->imageView.setImageResource(R.drawable.ic_group_11)
           "12"->imageView.setImageResource(R.drawable.ic_group_12)
            else->imageView.setImageResource(R.drawable.ic_no_result)

       }
    }
}