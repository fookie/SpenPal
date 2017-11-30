package com.evelin.spenpal.swipemenu.interfaces;

import com.evelin.spenpal.swipemenu.bean.SwipeMenu;
import com.evelin.spenpal.swipemenu.view.SwipeMenuView;


public interface OnSwipeItemClickListener {
    void onItemClick(SwipeMenuView view, SwipeMenu menu, int index);
}