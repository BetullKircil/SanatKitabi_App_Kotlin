package com.betulkircil.sanat_kitabi_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.betulkircil.sanat_kitabi_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var design: ActivityMainBinding
    private lateinit var ArtList: ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        design = ActivityMainBinding.inflate(layoutInflater)
        val view = design.root
        setContentView(view)
        ArtList = ArrayList<Art>()
        artAdapter = ArtAdapter(ArtList)
        design.recyclerView.layoutManager = LinearLayoutManager(this)
        design.recyclerView.adapter = artAdapter

        try {
            val dataBase = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
            val cursor = dataBase.rawQuery("SELECT * FROM arts", null)
            val artNameIndex = cursor.getColumnIndex("artName")
            val artistNameIndex = cursor.getColumnIndex("artistName")
            val dateIndex = cursor.getColumnIndex("date")
            val imageIndex = cursor.getColumnIndex("image")
            val idIndex = cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name = cursor.getString(artNameIndex)
                val id = cursor.getInt(idIndex)
                val art = Art(name, id)
                ArtList.add(art)
            }
            artAdapter.notifyDataSetChanged()
            cursor.close()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflater -> layout ile kodu birbirine bagliycaz
        menuInflater.inflate(R.menu.options_menu, menu) //menuyu activiteye bagladik
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.artMenu){
            val intent = Intent(this@MainActivity, ArtActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}