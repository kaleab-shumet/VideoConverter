package tnt.mp4tomp3.m4atomp3

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.vmadalin.easypermissions.EasyPermissions
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager
import java.io.File


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var addFilesButton: Button
    private lateinit var folderToSave: File
    private lateinit var builder: AlertDialog.Builder
    private lateinit var convertButton: Button
    private lateinit var outPutDirTv: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerViewEmptySupport
    private lateinit var fileRecyclerAdapter: FilesRecyclerAdapter
    private lateinit var selectedFilesList: MutableList<String>
    private lateinit var bitrateList: MutableList<String>


    val REQUEST_CODE_FOR_EXTERNAL_STORAGE: Int = 123

    private var selectedBitrate = ""

    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFilesButton = findViewById(R.id.btn_select_file)
        convertButton = findViewById(R.id.btn_convert)

        progressBar = findViewById(R.id.progress_bar)

        outPutDirTv = findViewById(R.id.output_dir_tv)

        val emptyView: TextView = findViewById(R.id.empty_text_view)

        builder = AlertDialog.Builder(this)

        folderToSave = commonDocumentDirPath("Mp4-To-Mp3")


        initBitrateSpinner()


        recyclerView = findViewById(R.id.file_list_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.setEmptyView(emptyView)



        addFilesButton.setOnClickListener {
            checkPermission { selectFiles() }
        }

        convertButton.setOnClickListener {

            if (!this::selectedFilesList.isInitialized || selectedFilesList.isEmpty())
                Toast.makeText(this, "Please add files to convert into MP3", Toast.LENGTH_LONG)
                    .show()
            else convertVideo()
        }


    }

    private fun selectFiles() {

        FilePickerManager
            .from(this)
            .setTheme(R.style.FilePickerThemeRail)
            .maxSelectable(15)
            .filter(object : AbstractFileFilter() {
                override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                    return ArrayList(listData.filter { item ->
                        item.isDir || item.fileName.endsWith(".mp4") || item.fileName.endsWith(".m4a")
                    })
                }
            })
            .forResult(FilePickerManager.REQUEST_CODE)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            this,
            "You should grant permissions to work with the app !",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        selectFiles()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    selectedFilesList = FilePickerManager.obtainData() as MutableList<String>
                    fileRecyclerAdapter = FilesRecyclerAdapter(selectedFilesList)
                    recyclerView.adapter = fileRecyclerAdapter


                } else {
                    Toast.makeText(this, "You didn't choose anything~", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initBitrateSpinner() {
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
                selectedBitrate = bitrateList[position]
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
        bitrateSpinner.setSelection(1)

    }

    private fun removeExt(fileName: String): String {
        val oldFileName = File(fileName).name
        val indexOfExt = oldFileName.lastIndexOf('.')
        return oldFileName.substring(startIndex = 0, endIndex = indexOfExt)
    }

    private fun convertVideo() {

        convertButton.isEnabled = false
        addFilesButton.isEnabled = false


        var totalDuration: Long = 0
        for (s in selectedFilesList) {
            val sDuration = getDuration(File(s))!!.toLong()
            if (sDuration > totalDuration)
                totalDuration = sDuration
        }

        Log.d(TAG, "convertVideo: sDuration-$totalDuration")


        val outDirStr = "Location: " + folderToSave.absolutePath
        outPutDirTv.text = outDirStr
        outPutDirTv.visibility = VISIBLE

        var multipleInputCommand = ""

        for (selectedFilePath in this.selectedFilesList)
            multipleInputCommand += " -i \"$selectedFilePath\""


        for (i in 0 until this.selectedFilesList.size) {
            val mAudFilePath =
                "${folderToSave.absolutePath}/${removeExt(this.selectedFilesList[i])}-${
                    Utils().randomString(
                        3
                    )
                }.mp3"
            multipleInputCommand += " -map $i  -vn -b:a $selectedBitrate \"$mAudFilePath\""
        }

        Log.d(TAG, "convertVideo: multipleInputCommand:$multipleInputCommand")


        Thread {
            FFmpegKit.executeAsync(
                multipleInputCommand,
                { session ->
                    val state = session.state
                    val returnCode = session.returnCode


                    Log.d(
                        TAG,
                        java.lang.String.format(
                            "FFmpeg process exited with state %s and rc %s.%s",
                            state,
                            returnCode,
                            session.failStackTrace
                        )
                    )

                    runOnUiThread {
                        displayCompletedDialog()
                    }

                }, { log ->

                    Log.d(TAG, "convertVideo: $log")


                }) { stat ->
                run {

                    val percentage: Double = stat.time.toDouble() / totalDuration

                    runOnUiThread {
                        progressBar.setProgress((percentage * 100).toInt(), true)
                    }

                    Log.d(TAG, "convertVideo: stat->initDuration Percentage -> $percentage")

                }
                // CALLED WHEN SESSION GENERATES STATISTICS
            }
        }.start()

    }


    private fun displayCompletedDialog() {
        builder
            .setTitle("Conversion Complete")
            .setMessage("You can get your files in \n" + folderToSave.absolutePath)
            .setCancelable(false)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                convertButton.isEnabled = true
                addFilesButton.isEnabled = true
                val removedLen = selectedFilesList.size
                selectedFilesList.clear()
                fileRecyclerAdapter.notifyItemRangeRemoved(0, removedLen)
                progressBar.setProgress(0, true)
            }
        val alert = builder.create()
        alert.show()
    }

    private fun commonDocumentDirPath(FolderName: String): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .toString() + "/" + FolderName
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }


    private fun getDuration(file: File): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    }


}