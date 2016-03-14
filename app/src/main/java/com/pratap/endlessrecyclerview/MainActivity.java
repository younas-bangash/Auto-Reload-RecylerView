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

    private Toolbar toolbar;

    private TextView tvEmptyView;
    private RecyclerView mRecyclerView;
    private DataAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private int mStart=0,mEnd=20;
    private List<Student> studentList;
    public static int pageNumber;


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

        GetGroupData(""+mStart,""+mEnd);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                studentList.add(null);
                mAdapter.notifyItemInserted(studentList.size() - 1);
                int start = studentList.size();
                int end = start + 20;
                ++pageNumber;
                GetGroupData(""+start,""+end);
            }
        });
    }

    public void GetGroupData(String LimitStart, String LimitEnd) {
        Map<String, String> params = new HashMap<>();
        params.put("LimitStart", LimitStart);
        params.put("LimitEnd", LimitEnd);
        Custom_Volly_Request jsonObjReq = new Custom_Volly_Request(Request.Method.POST,
                "autoload.php", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ResponseSuccess",response.toString());
                        int json_status ;
                        try {
                            JSONArray catalouge;
                            json_status = response.getInt("success");
                            if (json_status == 1) {
                                if (pageNumber > 1) {
                                    studentList.remove(studentList.size() - 1);
                                    mAdapter.notifyItemRemoved(studentList.size());
                                }
                                catalouge = response.getJSONArray("orders");
                                for (int i = 0; i < catalouge.length(); i++) {
                                    JSONObject jobj = catalouge.getJSONObject(i);
                                    Student st;
                                    st= new Student(jobj.getString("bname"),"");
                                    studentList.add(st);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.notifyItemInserted(studentList.size());
                                        }
                                    });
                                }
                                mAdapter.setLoaded();
                            }

//                            mAdapter = new BuyPropertyAdapter(mResult,getApplicationContext());
//                            recyclerView.setAdapter(mAdapter);
//                            pDialog.setVisibility(View.GONE);
//                            mAdapter.setOnItemClickListener(new BuyPropertyAdapter.MyClickListener() {
//                                @Override
//                                public void onItemClick(int position, View v) {
//                                    if(mMixCatalougeCheck){
//                                        Intent a1 = new Intent(CustomizeCatalougeMainActivity.this, MixCatalougeMainActivity.class);
//                                        a1.putExtra("HeadCategory",mResult.get(position).getmHeadCategory());
//                                        a1.putExtra("CollectionID",mResult.get(position).getMcollection_id());
//                                        a1.putExtra("BrandName", mBrandName);
//                                        a1.putExtra("BrandID", mBrandID);
//                                        startActivity(a1);
//                                    }
//                                }
//                            });
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
