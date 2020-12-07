package com.golde.cowrywise.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.golde.cowrywise.R
import com.golde.cowrywise.models.Currency

class CurrenciesAdapter : RecyclerView.Adapter<CurrenciesAdapter.ViewHolder>() {
    lateinit var mClickListener: ClickListener
    private var currencies = emptyList<Currency>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.symbol.text = currencies[position].currency.trim()
        holder.nation.text = currencies[position].nation?.trim()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.currency_item_view, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    internal fun inflate(currencies: List<Currency>) {
        this.currencies = currencies
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val symbol: TextView = itemView.findViewById(R.id.symbol)
        val nation: TextView = itemView.findViewById(R.id.nation)

        override fun onClick(v: View) {
            mClickListener.onClick(v, adapterPosition, currencies[adapterPosition])
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

    interface ClickListener {
        fun onClick(view: View, pos: Int, currency: Currency)
    }
}
