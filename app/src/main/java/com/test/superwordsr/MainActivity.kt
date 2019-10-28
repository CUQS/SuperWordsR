package com.test.superwordsr

import ObjBox.HourlyObj
import ObjBox.ObjectBox
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnInvokeView
import com.lzf.easyfloat.permission.PermissionUtils
import com.squareup.picasso.Picasso
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var hourlyObj: Box<HourlyObj>
    private var floatFlag: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()  // 初始化UI
        hourlyObj = ObjectBox.boxStore.boxFor()  // 初始化ObjectBox
        checkPermission()  // 初始化悬浮窗
    }

    override fun onDestroy() {
        EasyFloat.dismissAppFloat(this)
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v) {
            // 显示和隐藏悬浮窗按钮
            btnFloatShow -> {
                if (!floatFlag) {
                    EasyFloat.showAppFloat(this)
                }
                else {
                    EasyFloat.hideAppFloat(this)
                }
                floatFlag = !floatFlag
            }
        }
    }

    private fun showAppFloat() {
        EasyFloat.with(this)
            .setShowPattern(ShowPattern.ALL_TIME)
            .setGravity(Gravity.CENTER)
            .setSidePattern(SidePattern.RESULT_SIDE)
            .setLayout(R.layout.float_layout, OnInvokeView {
                // 悬浮窗的点击事件
                it.findViewById<ImageView>(R.id.imgViewFloat).setOnClickListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "回到主页", Toast.LENGTH_SHORT).show()
                }
                // load the image with Picasso
                Picasso.get().load(R.drawable.pokemon).into(it.findViewById<ImageView>(R.id.imgViewFloat))
            })
            .show()
    }
    /**
     * 检测浮窗权限是否开启，若没有给与申请提示框（非必须，申请依旧是EasyFloat内部内保进行）
     */
    private fun checkPermission() {
        if (PermissionUtils.checkPermission(this)) {
            showAppFloat()
        } else {
            AlertDialog.Builder(this)
                .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                .setPositiveButton("去开启") { _, _ ->
                    showAppFloat()
                }
                .setNegativeButton("取消") { _, _ -> }
                .show()
        }
    }
    /**
     * 初始化控件
     */
    private fun initUI() {
        btnFloatShow.setOnClickListener(this)
    }

}
