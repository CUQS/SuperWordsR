package ListViewControl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.superwordsr.R


class CheckBoxAdapter(context: Context, private var list: ArrayList<HashMap<String, Any>>): BaseAdapter() {

    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var tv: TextView
    private lateinit var cb: CheckBox

    override fun getCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewReturn: View
        if (convertView==null) {
            viewReturn = layoutInflater.inflate(R.layout.listview_words, null)
            val viewHolder = ViewHolder()
            tv = viewReturn.findViewById(R.id.item_tv)
            cb = viewReturn.findViewById(R.id.item_cb)
            viewHolder.tv = tv
            viewHolder.cb = cb
            viewReturn.tag = viewHolder
        }
        else {
            viewReturn = convertView
            val viewHolder: ViewHolder = convertView.tag as ViewHolder
            tv = viewHolder.tv
            cb = viewHolder.cb
        }
        tv.text = list[position]["name"] as String
        cb.isChecked = list[position]["boolean"] as Boolean
        val colorListI: String = list[position]["color"] as String
        if (colorListI=="MAGENTA") tv.setBackgroundColor(Color.MAGENTA)
        if (colorListI=="TRANSPARENT") tv.setBackgroundColor(Color.TRANSPARENT)
        return viewReturn
    }
}