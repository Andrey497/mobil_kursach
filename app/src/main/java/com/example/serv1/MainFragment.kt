package com.example.serv1

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.serv1.databinding.FragmentMainBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


private const val PICK_IMAGE_REQUEST = 1


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var selectedImageFile: File
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPickPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            binding.tvResult.text=""
        }


        binding.btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
            binding.tvResult.text=""
        }

        binding.btnAnalysePhoto.setOnClickListener {
            if (::selectedImageFile.isInitialized) {
                uploadImage(selectedImageFile)
                binding.tvResult.text="Идёт анализ фото"
            } else {
                Toast.makeText(context, "Выберите фото из галереи", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                startActivityForResult(takePictureIntent, Companion.REQUEST_IMAGE_CAPTURE)
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = activity?.contentResolver?.query(data.data!!, filePathColumn, null, null, null)
                    cursor?.moveToFirst()
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    val picturePath = cursor?.getString(columnIndex!!)
                    cursor?.close()
                    selectedImageFile = File(picturePath!!)
                    if (selectedImageFile.exists()) {
                        binding.ivPhoto.setImageURI(Uri.fromFile(selectedImageFile))
                    }
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    val tempFile = createTempImageFile()
                    saveBitmapToFile(imageBitmap, tempFile)
                    selectedImageFile = tempFile
                    binding.ivPhoto.setImageBitmap(imageBitmap)
                }
            }
        }
    }

    private fun createTempImageFile(): File {
        return File.createTempFile(
            "temp_image",
            ".jpg",
            activity?.externalCacheDir
        )
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uploadImage(imageFile: File) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())

        val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)     // Установить таймаут подключения.
            .readTimeout(100, TimeUnit.SECONDS)        // Установить таймаут чтения.
            .writeTimeout(100, TimeUnit.SECONDS)       // Установить таймаут записи.
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaType())
            )
            .addFormDataPart(
                "name",
                imageFile.name,
            )
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:8000/api/image")
            .post(requestBody)
            .build()

        val responseCallback = object : Callback {
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    binding.tvResult.text = "Ошибка при отправке фото: ${e.message}"
                }
            }


            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                activity?.runOnUiThread {
                    if (responseData != null) {
                        val gson = Gson()
                        val imageInfo = gson.fromJson(responseData, ImageInfo::class.java)
                        val historyRecords = HistoryRecords()
                        binding.tvResult.text = "Вероятность: ${imageInfo.accuracy}\nБолезнь: ${imageInfo.disease}"

//                        val newYorkDateTimePattern = "dd.MM.yyyy HH:mm z"
//                        val formatter= DateTimeFormatter.ofPattern(newYorkDateTimePattern)
                        val time= LocalDateTime.now().toString()
                
                        historyRecords.list.add(HistoryRecord(time,imageInfo.disease,imageInfo.accuracy,selectedImageFile.path.toString()))
                        historyRecords.save(preferences)
                    } else {
                        binding.tvResult.text = "Пустой ответ от сервера"
                    }
                }
            }
        }

        client.newCall(request).enqueue(responseCallback)

        val thread = Thread {
            try {
                val response = client.newCall(request).execute()
                response.use {
//                    responseCallback.onResponse(call, response)
                }
            } catch (e: IOException) {
//                responseCallback.onFailure(call, e)
            }
        }

        thread.start()
    }
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 2
    }
}