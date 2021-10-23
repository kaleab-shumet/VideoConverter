package com.tnt.videoconverter

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.bumptech.glide.Glide
import com.vmadalin.easypermissions.EasyPermissions
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA
import droidninja.filepicker.FilePickerConst.REQUEST_CODE_PHOTO
import droidninja.filepicker.utils.ContentUriUtils
import java.io.File


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var outputFormats: MutableList<String>
    private lateinit var bitrateList: MutableList<String>
    private lateinit var videoPath: String
    private lateinit var videoThumbnail: ImageView
    private lateinit var filePathTv: TextView
    private lateinit var fileNameEditText: EditText
    private val REQUEST_CODE_FOR_EXTERNAL_STORAGE: Int = 123


    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var selectButton: Button = findViewById(R.id.btn_select_file)
        var addTagButton: Button = findViewById(R.id.add_tag)
        var convertButton: Button = findViewById(R.id.btn_convert)
        fileNameEditText = findViewById(R.id.filename_edit_text)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        filePathTv = findViewById(R.id.file_path_tv)

        //initOutputFormatSpinner()
        initBitrateSpinner()

        selectButton.setOnClickListener {
            checkPermission { selectFiles() }
        }

        convertButton.setOnClickListener {
            convertVideo()
        }

        addTagButton.setOnClickListener {
            showMetadataDialog()
        }

    }

    private fun selectFiles() {


        FilePickerBuilder.instance
            .setMaxCount(1) //optional
            .enableImagePicker(false)
            .enableVideoPicker(true)
            .setActivityTheme(R.style.LibAppTheme_Dark) //optional
            .enableSelectAll(false)
            .pickPhoto(this)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            this,
            "You should grant permissions to work with the app !",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_PHOTO -> if (resultCode === RESULT_OK) {
                val selectedItem = data?.getParcelableArrayListExtra<Uri>(KEY_SELECTED_MEDIA)

                if (selectedItem != null) {
                    videoPath = ContentUriUtils.getFilePath(this, selectedItem.get(0)).toString()
                    Log.d(TAG, "onActivityResult: $videoPath")

                    val videoUri: Uri = selectedItem.get(0)
                    Glide.with(this)
                        .load(videoUri)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(videoThumbnail as ImageView)

                    val videoFile = File(videoPath)
                    filePathTv.text = videoFile.name

                    val extensionIndex = videoFile.name.lastIndexOf('.')


                    fileNameEditText.setText(
                        videoFile.name.substring(
                            startIndex = 0,
                            endIndex = extensionIndex
                        )
                    )

                }

            }
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    private fun checkPermission(HasPermission: () -> Unit) {

        if (EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)) {
            // Already have permission, do the thing
            // ...
            Log.d(TAG, "checkPermission: Ya has Permission")
            HasPermission()
        } else {
            // Do not have permissions, request them now
            Log.d(TAG, "checkPermission: Nooooooo Permission")
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_storage_message),
                REQUEST_CODE_FOR_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
            )
        }


    }

    /*
        fun initOutputFormatSpinner() {
            val outputFormatSpinner: Spinner = findViewById(R.id.output_format_spinner)

            outputFormats = ArrayList()
            outputFormats.add("MP3")
            outputFormats.add("M4A")
            outputFormats.add("FLAC")
            outputFormats.add("AAC")
            outputFormats.add("WAV")
            outputFormats.add("WMV")

            outputFormatSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {


                    Toast.makeText(baseContext, outputFormats[position], Toast.LENGTH_LONG).show()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }


            val outputFormatAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                this,
                android.R.layout.simple_spinner_item,
                outputFormats as List<Any?>
            )

            outputFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            outputFormatSpinner.adapter = outputFormatAdapter

        }
    */
    fun initBitrateSpinner() {
        val bitrateSpinner: Spinner = findViewById(R.id.bitrate_spinner)

        bitrateList = ArrayList()
        bitrateList.add("320K")
        bitrateList.add("256K")
        bitrateList.add("224K")
        bitrateList.add("192K")
        bitrateList.add("160K")
        bitrateList.add("128K")
        bitrateList.add("112K")
        bitrateList.add("96K")

        bitrateSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {

                Toast.makeText(baseContext, bitrateList[position], Toast.LENGTH_LONG).show()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val bitrateAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            bitrateList as List<Any?>
        )
        bitrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bitrateSpinner.adapter = bitrateAdapter

    }

    fun convertVideo() {

        //Log.d(TAG, "convertVideo: "+commonDocumentDirPath("Mp4-To-Mp3"))


        // Creating External Directory to save the converted file
        val folderToSave = commonDocumentDirPath("Mp4-To-Mp3")

        val selectedFileFormat = "." + "mp3"
        val oldFileName = File(videoPath).name
        val indexOfExt = oldFileName.lastIndexOf('.')
        val fileName: String =
            oldFileName.substring(startIndex = 0, endIndex = indexOfExt) + selectedFileFormat
        val finalFileName = "$folderToSave/$fileName"



        Log.d(TAG, "convertVideo: $finalFileName")

        val command: String = "-i $videoPath $finalFileName"


        FFmpegKit.executeAsync(command,
            { session ->
                val state = session.state
                val returnCode = session.returnCode

                // CALLED WHEN SESSION IS EXECUTED
                Log.d(
                    TAG,
                    java.lang.String.format(
                        "FFmpeg process exited with state %s and rc %s.%s",
                        state,
                        returnCode,
                        session.failStackTrace
                    )
                )
            }, { log ->
                Log.d(TAG, "convertVideo: " + log.message)
                // CALLED WHEN SESSION PRINTS LOGS
            }) {
            // CALLED WHEN SESSION GENERATES STATISTICS
        }
    }


    fun commonDocumentDirPath(FolderName: String): File? {
        var dir: File? = null
        dir =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    .toString() + "/" + FolderName
            )

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir
    }

    fun showMetadataDialog() {
        val factory = LayoutInflater.from(this)

        val dilogView: View = factory.inflate(R.layout.metadata_dialog, null)

        val titleEditText = dilogView.findViewById<View>(R.id.tile_edit_text) as EditText
        val artistEditText = dilogView.findViewById<View>(R.id.artist_edit_text) as EditText
        val albumEditText = dilogView.findViewById<View>(R.id.album_edit_text) as EditText


        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Set Metadata")
            .setView(dilogView)
            .setPositiveButton("Save")
            { _, _ ->

                Log.i("AlertDialog", "titleEditText " + titleEditText.text.toString())
                Log.i("AlertDialog", "artistEditText " + artistEditText.text.toString())
                Log.i("AlertDialog", "albumEditText " + albumEditText.text.toString())

            }
        alert.show()
    }

}