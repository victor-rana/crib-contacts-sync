package com.example.cribcontactssync

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.*
import java.util.*
import android.database.DatabaseUtils
import android.view.View


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_READ_CONTACTS = 100
    var etNumber: EditText? = null
    var etDays: EditText? = null
    private lateinit var btSubmit: Button
    var tvResult: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_SMS),PERMISSION_REQUEST_READ_CONTACTS)
        }

        btSubmit = findViewById(R.id.btSubmit)
        etNumber = findViewById(R.id.etNumber)
        etDays = findViewById(R.id.etDays)
        tvResult = findViewById(R.id.tvResult)

        btSubmit.setOnClickListener {
            if(TextUtils.isEmpty(etNumber!!.text.toString()) && TextUtils.isEmpty(etDays!!.text.toString())){
                Toast.makeText(this, "Please fill all the fields",Toast.LENGTH_LONG).show()
            }
            else{
                if (permissionCheck == PackageManager.PERMISSION_GRANTED){
                    showContacts()
                }else{
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_SMS),PERMISSION_REQUEST_READ_CONTACTS)
                }
            }
        }



    }

    private fun showContacts() {

        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = cal.get(Calendar.YEAR)
        cal[Calendar.MONTH] = cal.get(Calendar.MONTH)
        cal[Calendar.DATE] = cal.get(Calendar.DATE)
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        val days: Int = etDays!!.text.toString().toInt()
        val DAYS_MILIS: Int  = 86400000*days
        val selectionArgs = arrayOf("+91"+etNumber!!.text.toString())
        val whereAddress = "address = ?"
        val whereDate = "date BETWEEN " + (cal.timeInMillis - DAYS_MILIS) +
                " AND " + cal.timeInMillis.toString()
        val where = DatabaseUtils.concatenateWhere(whereAddress, whereDate)
        val cursor1 = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("_id", "thread_id", "address", "person", "date", "body", "type"),
            where.toString(),
            selectionArgs,
            "date DESC "
        )
        val msgData = StringBuffer()
        var msgCount: Int? = 0
        if (cursor1!!.moveToFirst()) {
            do {
                for (idx in 0 until cursor1!!.columnCount) {
                    msgData.append(
                        " " + cursor1.getColumnName(idx) + ":" + cursor1.getString(
                            idx
                        )
                    )
                }
                msgCount = msgCount!!.plus(1)
                Log.d("TAG", "showContacts: $msgData")
            } while (cursor1.moveToNext())
            showResult()
            tvResult!!.text = "$msgCount number of messages found"
        } else {
            Log.d("TAG", "showContacts: Error $where")
            showResult()
            tvResult!!.text = "Sorry, no messages found"
        }
    }

    private fun showResult(){
        if(tvResult!!.visibility == View.GONE){
            tvResult!!.visibility = View.VISIBLE
        }
    }
}