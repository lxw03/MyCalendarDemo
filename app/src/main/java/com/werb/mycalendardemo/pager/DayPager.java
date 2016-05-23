package com.werb.mycalendardemo.pager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.werb.mycalendardemo.R;
import com.werb.mycalendardemo.ScheduleDetailActivity;
import com.werb.mycalendardemo.customview.pageradapter.RecycleAdapter;
import com.werb.mycalendardemo.database.AlarmDBSupport;
import com.werb.mycalendardemo.models.CalendarEvent;
import com.werb.mycalendardemo.utils.BusProvider;
import com.werb.mycalendardemo.utils.Events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by acer-pc on 2016/5/12.
 */
public class DayPager extends BasePager {

    @Bind(R.id.recycle_view)
    RecyclerView recycle_view;
    @Bind(R.id.day)
    TextView day;
    @Bind(R.id.week)
    TextView week;
    @Bind(R.id.haveOrNot)
    TextView haveOrNot;

    private List<Object> list;

    public DayPager(Activity mActivity) {
        super(mActivity);

    }

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.daypager_view, null);

        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void initData(List<CalendarEvent> eventList) {

        chooseDayFromClick();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(mActivity);
        recycle_view.setLayoutManager(manager);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        list= new AlarmDBSupport(mActivity).getDataByDay(calendar);
        RecycleAdapter dayPagerAdapter = new RecycleAdapter("dayPager", list,mActivity);
        recycle_view.setAdapter(dayPagerAdapter);

        day.setText(calendar.get(Calendar.DAY_OF_MONTH)+"");

        //设置星期格式
        SimpleDateFormat dayWeekFormatter = new SimpleDateFormat("E");
        String weekStr = dayWeekFormatter.format(calendar.getTimeInMillis());
        week.setText(weekStr);

        if(list.size()==0){
            haveOrNot.setText("还没有日程信息，点击加号添加");
        }else {
            haveOrNot.setText("这是您今日的日程信息");
        }

        //Item 点击事件
        dayPagerAdapter.setOnMyItemListener(new RecycleAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView alarm_id = (TextView) view.findViewById(R.id.Alarm_id);
                Toast.makeText(mActivity,position+"-"+alarm_id.getText().toString(),Toast.LENGTH_SHORT).show();
                if(!alarm_id.getText().toString().equals("0")){
                    Intent intent = new Intent(mActivity, ScheduleDetailActivity.class);
                    intent.putExtra("id",alarm_id.getText().toString());
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
            }
        });

       recycle_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
               BusProvider.getInstance().send(new Events.AgendaListViewTouchedEvent());
           }
       });

    }

    /**
     * 点击 CalendarView 选择不同的日期
     */
    private void chooseDayFromClick(){
        //BusProvider处理事件
        BusProvider.getInstance().toObserverable()
                .subscribe(event -> {
                    if (event instanceof Events.DayClickedEvent) {
                        //Day日期点击事件
                        Events.DayClickedEvent clickedEvent = (Events.DayClickedEvent) event;
                        list = new AlarmDBSupport(mActivity).getDataByDay(clickedEvent.getCalendar());

                        day.setText(clickedEvent.getCalendar().get(Calendar.DAY_OF_MONTH)+"");

                        //设置星期格式
                        SimpleDateFormat dayWeekFormatter = new SimpleDateFormat("E");
                        String weekStr = dayWeekFormatter.format(clickedEvent.getCalendar().getTimeInMillis());
                        week.setText(weekStr);

                        if(list.size()==0){
                            haveOrNot.setText("还没有日程信息，点击加号添加");
                        }else {
                            haveOrNot.setText("这是您今日的日程信息");
                        }

                        RecycleAdapter dayPagerAdapter = new RecycleAdapter("dayPager", list,mActivity);
                        recycle_view.setAdapter(dayPagerAdapter);
                        dayPagerAdapter.notifyDataSetChanged();

                        //Item 点击事件
                        dayPagerAdapter.setOnMyItemListener(new RecycleAdapter.MyItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                TextView alarm_id = (TextView) view.findViewById(R.id.Alarm_id);
                                Toast.makeText(mActivity,position+"-"+alarm_id.getText().toString(),Toast.LENGTH_SHORT).show();
                                if(!alarm_id.getText().toString().equals("0")){
                                    Intent intent = new Intent(mActivity, ScheduleDetailActivity.class);
                                    intent.putExtra("id",alarm_id.getText().toString());
                                    mActivity.startActivity(intent);
                                    mActivity.finish();
                                }
                            }
                        });
                    }
                });
    }
}
