package com.pratap.endlessrecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Toolbar toolbar;

    private TextView tvEmptyView;
    private RecyclerView mRecyclerView;
    private DataAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private int mStart=0,mEnd=20;
    private List<Student> studentList;
    private List<Student> mTempCheck;
    public static int pageNumber;
    public int total_size=0;


    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageNumber = 1;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvEmptyView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        studentList = new ArrayList<>();
        mTempCheck=new ArrayList<>();
        handler = new Handler();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Android Students");

        }

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DataAdapter(studentList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        GetGroupData("" + mStart, "" + mEnd);
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if( mTempCheck.size()> 0) {
                    studentList.add(null);
                    mAdapter.notifyItemInserted(studentList.size() - 1);
                    int start = pageNumber * 20;
                    start = start + 1;
                    ++ pageNumber;
                    mTempCheck.clear();
                    GetGroupData("" + start,""+ mEnd);
                }
            }
        });
    }

    public void GetGroupData(final String LimitStart, final String LimitEnd) {
        Map<String, String> params = new HashMap<>();
        params.put("LimitStart", LimitStart);
        params.put("Limit", LimitEnd);
        Custom_Volly_Request jsonObjReq = new Custom_Volly_Request(Request.Method.POST,
                "Your php file link", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ResponseSuccess",response.toString());
                        int json_status ;
                        try {
                            JSONArray catalouge;
                            json_status = response.getInt("success");
                            if (json_status == 1) {

                                Log.d("ResponseSuccess", "GetGroupData() called with: LimitStart = "+ pageNumber
                                        + "[" + LimitStart + "], LimitEnd = [" + LimitEnd + "]");

                                if (pageNumber > 1 ) {
                                    studentList.remove(studentList.size() - 1);
                                    mAdapter.notifyItemRemoved(studentList.size());
                                }
                                catalouge = response.getJSONArray("orders");
                                total_size = total_size+catalouge.length();
                                for (int i = 0; i < catalouge.length(); i++) {
                                    JSONObject jobj = catalouge.getJSONObject(i);
                                    Student st;
                                    st= new Student(jobj.getString("bname"),"");
                                    if(! mTempCheck.contains(st))
                                        mTempCheck.add(st);
                                    if(! studentList.contains(st))
                                        studentList.add(st);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.notifyItemInserted(studentList.size());
                                        }
                                    });
                                }
                            }else{
                                studentList.remove(studentList.size() - 1);
                                mAdapter.notifyItemRemoved(studentList.size());
                            }

                            mAdapter.setLoaded(false);
                        }
                        catch (JSONException e) {
                            Log.d("ResponseError",e.toString());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ResponseErrorVolly: " + error.getMessage());

            }});
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
    // load initial data
    private void loadData(int start,int end,boolean notifyadapter) {
        for (int i = start; i <= end; i++) {
            studentList.add(new Student("Student " + i, "androidstudent" + i + "@gmail.com"));
            if(notifyadapter)
                mAdapter.notifyItemInserted(studentList.size());
        }
    }
}
