package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.utility.Constants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class LineGraphActivity extends Activity {

    private ValueLineChart mLineChartView;
    private ProgressBar loading;
    private TextView noData;
    private String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mLineChartView = (ValueLineChart) findViewById(R.id.linechart);
        loading = (ProgressBar)findViewById(R.id.loading_progress);
        noData = (TextView) findViewById(R.id.no_data);
        loading.setVisibility(View.GONE);
        noData.setVisibility(View.GONE);
        mLineChartView.setVisibility(View.GONE);
        Intent intent = getIntent();
        companyName = intent.getStringExtra(Constants.COMPANY_SYMBOL);
        getData();
    }

    private void getData(){
        OkHttpClient client = new OkHttpClient();
        String url="http://chartapi.finance.yahoo.com/instrument/1.0/"+companyName+"/chartdata;type=quote;range=5y/json";
        Request request = new Request.Builder()
                .url(url)
                .build();

        final ValueLineSeries valueLineSeries = new ValueLineSeries();
        valueLineSeries.setColor(0xFF56B7F1);

        loading.setVisibility(View.VISIBLE);
        mLineChartView.setVisibility(View.GONE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                failed();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                   String result = response.body().string();
                    if (result.startsWith("finance_charts_json_callback(")) {
                        result = result.substring(29, result.length() - 1);
                    }
                    try {
                        JSONObject head = new JSONObject(result);
                        JSONArray series = head.getJSONArray("series");
                        for (int i = 0; i < series.length(); i++) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMDD");
                            String date = android.text.format.DateFormat.
                                    getMediumDateFormat(getApplicationContext()).
                                    format(simpleDateFormat.parse(series.getJSONObject(i).getString("Date")));
                            float value = Float.parseFloat(series.getJSONObject(i).getString("close"));
                            valueLineSeries.addPoint(new ValueLinePoint(date, value));
                        }
                        drawData(valueLineSeries);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }


    void drawData(final ValueLineSeries valueLineSeries){
        LineGraphActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
                mLineChartView.setVisibility(View.VISIBLE);

                mLineChartView.addSeries(valueLineSeries);
                mLineChartView.startAnimation();
            }
        });
    }

    void failed(){
        mLineChartView.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        noData.setVisibility(View.VISIBLE);
    }
}
