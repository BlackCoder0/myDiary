package com.code.mydiary.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.code.mydiary.R;

import java.util.ArrayList;

public class NoListAdapter extends RecyclerView.Adapter<NoListAdapter.ViewHolder> {

    private ArrayList<String> dataList;
    private OnContentChangeListener contentChangeListener;

    public interface OnContentChangeListener {
        void onContentChanged(int position, String content);
    }

    public NoListAdapter(ArrayList<String> dataList, OnContentChangeListener listener) {
        this.dataList = dataList;
        this.contentChangeListener = listener;
    }



    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText etNoItem;
        ImageView dot;
        CustomTextWatcher customTextWatcher;
        View nolistLine;

        public ViewHolder(@NonNull View itemView, CustomTextWatcher watcher) {
            super(itemView);
            etNoItem = itemView.findViewById(R.id.et_no_item);
            dot = itemView.findViewById(R.id.iv_dot);
            customTextWatcher = watcher;
            etNoItem.addTextChangedListener(customTextWatcher);
            nolistLine = itemView.findViewById(R.id.nolist_line);
        }
    }

    private class CustomTextWatcher implements TextWatcher {
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            dataList.set(position, s.toString());//更新
            if (contentChangeListener != null) {
                contentChangeListener.onContentChanged(position, s.toString());
            }
            // 删除notifyItemChanged，避免死循环和卡死
            // if (holderRef != null && holderRef.get() != null) {
            //     holderRef.get().etNoItem.post(() -> {
            //         notifyItemChanged(position);
            //     });
            // }
        }

        @Override
        public void afterTextChanged(Editable s) {}
        // 持有ViewHolder弱引用
        private java.lang.ref.WeakReference<ViewHolder> holderRef;
        public void setHolder(ViewHolder holder) {
            holderRef = new java.lang.ref.WeakReference<>(holder);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_list, parent, false);
        return new ViewHolder(view, new CustomTextWatcher());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.customTextWatcher.setPosition(position);
        holder.customTextWatcher.setHolder(holder); // 传递holder引用
        holder.etNoItem.setText(dataList.get(position));
        holder.dot.setVisibility(dataList.get(position).trim().isEmpty() ? View.INVISIBLE : View.VISIBLE);

        // 动态设置 nolist_line 颜色
        int color = holder.itemView.getContext().getResources().getColor(
                GenderResourceUtil.getTabMainColorRes(holder.itemView.getContext())
        );
        holder.nolistLine.setBackgroundColor(color);

        // EditText获取焦点时滚动到该项
        holder.etNoItem.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                RecyclerView recyclerView = (RecyclerView) holder.itemView.getParent();
                if (recyclerView != null) {
                    recyclerView.post(() -> recyclerView.smoothScrollToPosition(holder.getAdapterPosition()));
                }
            }
        });
    }
}