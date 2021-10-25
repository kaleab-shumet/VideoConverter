package tnt.mp4tomp3.m4atomp3

import android.content.Context
import android.util.AttributeSet
import android.view.View

import androidx.recyclerview.widget.RecyclerView


class RecyclerViewEmptySupport : RecyclerView {
    private var emptyView: View? = null
    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            dataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            dataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            dataChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            dataChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            dataChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            dataChanged()
        }
    }

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    )

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
    }

    private fun dataChanged() {
        val adapter = adapter
        if (adapter != null && emptyView != null) {
            if (adapter.itemCount == 0) {
                emptyView!!.visibility = View.VISIBLE
                this@RecyclerViewEmptySupport.visibility = View.GONE
            } else {
                emptyView!!.visibility = View.GONE
                this@RecyclerViewEmptySupport.visibility = View.VISIBLE
            }
        }
    }
}