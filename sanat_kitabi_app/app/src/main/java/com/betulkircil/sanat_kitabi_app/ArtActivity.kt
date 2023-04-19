package com.betulkircil.sanat_kitabi_app

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.betulkircil.sanat_kitabi_app.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.sql.SQLData

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    private lateinit var dataBase: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        RegisterLauncher()
        dataBase = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.editTextName.setText("")
            binding.editTextArtist.setText("")
            binding.editTextDate.setText("")
            binding.imageView.setImageResource(R.drawable.select_img)
        }
        else if(info.equals("old")){
            binding.btnSave.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 0)
            val cursor = dataBase.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIndex = cursor.getColumnIndex("artName")
            val artistNameIndex = cursor.getColumnIndex("artistName")
            val dateIndex = cursor.getColumnIndex("date")
            val imageIndex = cursor.getColumnIndex("image")
            val idIndex = cursor.getColumnIndex("id")
            while(cursor.moveToNext()){
                binding.editTextName.setText(cursor.getString(artNameIndex))
                binding.editTextArtist.setText(cursor.getString(artistNameIndex))
                binding.editTextDate.setText(cursor.getString(dateIndex))
                val byteArray = cursor.getBlob(imageIndex) //byte array verecek bu byte arrayi png'ye ceviricez
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }
    }

    fun Kaydet(view: View) {
        val artName = binding.editTextName.text.toString()
        val artistName = binding.editTextArtist.text.toString()
        val date = binding.editTextDate.text.toString()
        if(selectedBitmap != null){
            val kucultlmusBitmap = GorselKucult(selectedBitmap!!, 300)  //gorseli kucultme

            val outPutStream = ByteArrayOutputStream()  //veritabanina kaydetmek icin gorseli byte array'e ceviriyoruz
            kucultlmusBitmap.compress(Bitmap.CompressFormat.PNG, 50, outPutStream)
            val byteArray = outPutStream.toByteArray()

            try {
                dataBase.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, date VARCHAR, image BLOB)")  //TABLE OLUSTURUYORUZ
                val sqlString = "INSERT INTO arts(artName, artistName, date, image) VALUES(?, ?, ? , ?)"

                val statment = dataBase.compileStatement(sqlString)
                statment.bindString(1, artName)
                statment.bindString(2, artistName)
                statment.bindString(3, date)
                statment.bindBlob(4, byteArray)
                statment.execute()
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            val intent = Intent(this@ArtActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  //verileri ekledikten sonra main aktiviteye geri donerken acik olan tum aktiviteleri kapatir
            startActivity(intent)
        }
    }

    private fun GorselKucult(image: Bitmap, maksimumSize: Int): Bitmap{
        var genislik = image.width
        var yukseklik = image.height
        var oran: Double = genislik.toDouble() / yukseklik.toDouble()
        if(oran > 1){
            //lanscape
            genislik = maksimumSize
            val scaledHeight = genislik / oran
            yukseklik = scaledHeight.toInt()
        }
        else{
            //portrait
            yukseklik = maksimumSize
            val scaledWidth = yukseklik * oran
            genislik = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, genislik, yukseklik, true)
    }

    fun GorselSec(view: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                    }).show()
                }
                else{
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                    }).show()
                }
                else{
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else{
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun RegisterLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)  image uri datasinin sqlite veri tabanina kaydedecegimiz icin oncelikle bitmape cevirmemiz gerekir ondan sonra bitmap source'u veritabanina kaydedebiliriz
                    if(imageData != null){  //image data null degilse try catch'e gir ve decode et
                        try{
                            if(Build.VERSION.SDK_INT >= 28)
                            {
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                            else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
            else{
                Toast.makeText(this@ArtActivity, "Permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}