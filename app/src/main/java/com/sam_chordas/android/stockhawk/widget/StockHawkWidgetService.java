package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;
import com.sam_chordas.android.stockhawk.utility.Constants;

/**
 * Created by sbjr on 7/21/16.
 */

public class StockHawkWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockHawkWidgetFactory(getApplicationContext(),intent);
    }
}



class StockHawkWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    Cursor cursor;
    int appWidgetId;

    public StockHawkWidgetFactory(Context context, Intent intent) {
        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
    }

    @Override
    public void onCreate() {
        if(cursor!=null){
            cursor.close();
        }
        cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
    }

    @Override
    public void onDataSetChanged() {
        if(cursor!=null){
            cursor.close();
        }
        cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
    }

    @Override
    public void onDestroy() {
        if(cursor!=null){
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        String symbol = "";
        String bidPrice = "";
        String change = "";
        int isUp = 1;
        if (cursor.moveToPosition(position)) {
            final int symbolColIndex = cursor.getColumnIndex(QuoteColumns.SYMBOL);
            final int bidPriceColIndex = cursor.getColumnIndex(QuoteColumns.BIDPRICE);
            final int changeColIndex = cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE);
            final int isUpIndex = cursor.getColumnIndex(QuoteColumns.ISUP);
            symbol = cursor.getString(symbolColIndex);
            bidPrice = cursor.getString(bidPriceColIndex);
            change = cursor.getString(changeColIndex);
            isUp = cursor.getInt(isUpIndex);
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        rv.setTextViewText(R.id.stock_symbol, symbol);
        rv.setTextViewText(R.id.bid_price, bidPrice);
        rv.setTextViewText(R.id.change, change);
        if (isUp == 1) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        final Intent intent = new Intent();
        intent.putExtra(Constants.COMPANY_SYMBOL,symbol);
        rv.setOnClickFillInIntent(R.id.holder, intent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
