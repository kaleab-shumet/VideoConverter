package tnt.mp4tomp3.m4atomp3


import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File


class FilesRecyclerAdapter(private var selectedFilesList: MutableList<String>) :
    RecyclerView.Adapter<FilesRecyclerAdapter.FileItemViewHolder>() {

    private val typeMp4 = 1
    private val typeM4a = 2


    val TAG = "FilesRecyclerAdapter"

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {

        Log.d(TAG, "onCreateViewHolder: " + this.selectedFilesList)


        context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        val listItemView: View = layoutInflater.inflate(R.layout.file_list_item, parent, false)
        return FileItemViewHolder(listItemView)

    }


    override fun getItemCount(): Int {
        return this.selectedFilesList.count()
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.selectedFilesList.get(position).endsWith(".mp4")) {
            typeMp4
        } else {
            typeM4a
        }
    }

    class FileItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.file_thumbnail_image_view)
        val fileItemDelete: ImageView = itemView.findViewById(R.id.fileitem_delete)
        val fileNameTv: TextView = itemView.findViewById(R.id.filename_tv)

    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val selectedFilePath = this.selectedFilesList[position]
        val selectedFile = File(selectedFilePath)
        val selectedFileName = selectedFile.name

        holder.fileNameTv.text = selectedFileName

        holder.fileItemDelete.setOnClickListener {

            this.selectedFilesList.removeAt(position)
            this.notifyItemRemoved(position)
            notifyItemRangeChanged(position, this.selectedFilesList.size)

        }

        if (getItemViewType(position) == typeMp4) {
            val uri: Uri = Uri.fromFile(selectedFile)
            Glide.with(context)
                .load(uri)
                .thumbnail(0.1f)
                .centerCrop()
                .into(holder.thumbnailImageView)
        } else {
            Glide.with(context)
                .load(R.drawable.ic_m4a)
                .centerCrop()
                .into(holder.thumbnailImageView)
        }


    }

}


