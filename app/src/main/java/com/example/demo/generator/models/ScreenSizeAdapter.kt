package com.example.demo.generator.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.demo.R

class ScreenSizeAdapter(context: Context, resource: Int, objects: ArrayList<ScreenResolutions>) :
    ArrayAdapter<ScreenResolutions>(context, resource, objects) {

    private var dataList: ArrayList<ScreenResolutions>

    private var mContext: Context? = null

    private var itemLayout = 0


    init {
        this.dataList = objects
        this.mContext = context
        this.itemLayout = resource
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // return super.getView(position, convertView, parent)
        var view: View? = convertView
        val model = getItem(position)
        if (convertView == null) {
            view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        }
        val txtScreenName: TextView? = view?.findViewById(R.id.txtScreenName)
        val txtScreenSizes: TextView? = view?.findViewById(R.id.txtScreenSize)

        txtScreenName?.text = model.name
        txtScreenSizes?.text =""+ model.width +" X "+ model.height +"    "+model.screenSize
        return view!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        val model = getItem(position)
        if (convertView == null) {
            view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        }
        val txtScreenName: TextView? = view?.findViewById(R.id.txtScreenName)
        val txtScreenSizes: TextView? = view?.findViewById(R.id.txtScreenSize)

        txtScreenName?.text = model.name
        txtScreenSizes?.text =""+ model.width +" X "+ model.height +"    "+model.screenSize+" Inches"
        return view!!
    }

    override fun getItem(position: Int): ScreenResolutions {
        return dataList.get(position)
    }
}