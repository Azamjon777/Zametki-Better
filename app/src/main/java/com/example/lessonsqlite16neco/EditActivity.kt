package com.example.lessonsqlite16neco

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lessonsqlite16neco.db.MyDbManager
import com.example.lessonsqlite16neco.db.MyIntentConstants
import kotlinx.android.synthetic.main.edit_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    var id = 0
    var isEditState = false
    var tempImageUri = "empty"
    val myDbManager = MyDbManager(this)

    private var launcher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_activity)

        getCurrentTime()

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result : ActivityResult->
            if (result.resultCode == RESULT_OK){
                imMainImage.setImageURI(result.data?.data)
                tempImageUri = result.data?.data.toString()
                contentResolver.takePersistableUriPermission(result.data?.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        getMyIntents()

    }
    override fun onResume() {
        super.onResume()
        myDbManager.openDb()
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManager.closeDb()
    }


    fun onClickAddImage(view: View){
        mainImageLayout.visibility = View.VISIBLE
        fbAddImage.visibility = View.GONE
    }

    fun onClickDeleteImage(view: View) {
        mainImageLayout.visibility = View.GONE
        fbAddImage.visibility = View.VISIBLE
        tempImageUri = "empty"
    }

    fun onClickChooseImage(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        launcher?.launch(intent)
    }
    fun onClickSave(view: View) {
        val myTitle = edTitle.text.toString()
        val myDesc = edDesc.text.toString()


        if (myTitle !="" && myDesc != ""){
            CoroutineScope(Dispatchers.Main).launch {
                if (isEditState){
                    myDbManager.updateItem(myTitle, myDesc, tempImageUri, id, getCurrentTime())
                }else{
                    myDbManager.insertToDb(myTitle, myDesc, tempImageUri, getCurrentTime())
                }
                finish()
            }
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()

        }
        else{
            Toast.makeText(this, "Заполните все строчки", Toast.LENGTH_SHORT).show()
        }

    }

    fun onEditEnable(view: View) {
        edTitle.isEnabled = true
        edDesc.isEnabled = true
        fbEdit.visibility = View.GONE
        fbAddImage.visibility = View.VISIBLE
        if (tempImageUri!="empty"){
            fbAddImage.visibility = View.GONE
        }
        if (tempImageUri =="empty"){
            imgButtonEdit.visibility = View.VISIBLE
            imgButtonDelete.visibility = View.VISIBLE
        }
        Toast.makeText(this, "Режим редактиования", Toast.LENGTH_SHORT).show()
    }

    fun getMyIntents() {
        val i = intent
        fbEdit.visibility = View.GONE



        if (i != null){
            if (i.getStringExtra(MyIntentConstants.I_TITLE_KEY) != null){

                fbAddImage.visibility = View.GONE
                isEditState = true
                edTitle.isEnabled = false
                edDesc.isEnabled = false
                fbEdit.visibility = View.VISIBLE

                edTitle.setText(i.getStringExtra(MyIntentConstants.I_TITLE_KEY))
                edDesc.setText(i.getStringExtra(MyIntentConstants.I_DESC_KEY))

                id = i.getIntExtra(MyIntentConstants.I_ID_KEY, 0)

                if (i.getStringExtra(MyIntentConstants.I_URI_KEY) != "empty"){

                    mainImageLayout.visibility = View.VISIBLE
                    tempImageUri = i.getStringExtra(MyIntentConstants.I_TITLE_KEY)!!
                    imMainImage.setImageURI(Uri.parse(tempImageUri))
                    imgButtonDelete.visibility = View.GONE
                    imgButtonEdit.visibility = View.GONE
                }

            }
        }
    }

    private fun getCurrentTime():String{
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yy kk:mm", Locale.getDefault())
        return formatter.format(time)
    }

}