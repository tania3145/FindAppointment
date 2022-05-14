package com.example.findappointment.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findappointment.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    public interface OnClickListener {
        void onClick(Event event);
    }

    public static class Event {
        private CalendarDay day;
        private int hour;
        private String name;
        private boolean show;
        private Object data;

        public Event(String name) {
            this(CalendarDay.today(), 0, name, true);
        }

        public Event(CalendarDay day, int hour, String name) {
            this(day, hour, name, true);
        }

        public Event(CalendarDay day, int hour, String name, boolean show) {
            this.day = day;
            this.hour = hour;
            this.name = name;
            this.show = show;
        }

        public CalendarDay getDay() {
            return day;
        }

        public void setDay(CalendarDay day) {
            this.day = day;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isShow() {
            return show;
        }

        public void setShow(boolean show) {
            this.show = show;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    private static class DayItemData {
        public static class Builder {
            private DayItemData item;

            public Builder() {
                this.item = new DayItemData();
            }

            public Builder(DayItemData item) {
                this.item = item;
            }

            public Builder withHour(String hour) {
                item.hour = hour;
                return this;
            }

            public Builder withIndex(int index) {
                item.index = index;
                return this;
            }

            public Builder withIsAvailable(boolean isAvailable) {
                item.isAvailable = isAvailable;
                return this;
            }

            public Builder withEvent(Event event) {
                item.event = event;
                return this;
            }

            public DayItemData build() {
                return item;
            }
        }

        private String hour;
        private int index;
        private boolean isAvailable;
        private Event event;

        private DayItemData() { }

        public String getHour() {
            return hour;
        }

        public int getIndex() {
            return index;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public Event getEvent() {
            return event;
        }
    }

    private static class DayItem extends RecyclerView.ViewHolder {
        private View view;
        private TextView hourText;
        private LinearLayout eventLayout;
        private Button eventButton;

        public DayItem(@NonNull View itemView) {
            super(itemView);

            view = itemView;
            hourText = view.findViewById(R.id.hour_text);
            eventLayout = view.findViewById(R.id.event_layout);
            eventButton = view.findViewById(R.id.event_button);
        }

        public void setHour(String hour) {
            hourText.setText(hour);
        }

        public void setUnavailable() {
            eventLayout.setBackgroundColor(Color.parseColor("#DAFFFC"));
            eventButton.setVisibility(View.INVISIBLE);
        }

        public void setAvailable() {
            eventLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            eventButton.setVisibility(View.INVISIBLE);
        }

        public void setEvent(Event event, OnClickListener onEventClickListener) {
            if (!event.isShow()) {
                return;
            }
            eventButton.setText(event.name);
            eventButton.setVisibility(View.VISIBLE);
            if (onEventClickListener == null) {
                return;
            }
            eventButton.setOnClickListener(view -> {
                onEventClickListener.onClick(event);
            });
        }
    }

    private RecyclerView dayView;
    private RecyclerView.Adapter<DayItem> dayViewAdapter;
    private DayItemData[] hours;
    private Map<CalendarDay, List<Event>> events;
    private MaterialCalendarView calendar;
    private OnClickListener onEventClickListener;
    private Event defaultEvent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hours = new DayItemData[24];
        for (int i = 0; i < 10; i++) {
            hours[i] = new DayItemData.Builder()
                    .withHour(String.format("0%d:00", i))
                    .withIndex(i)
                    .withIsAvailable(false)
                    .build();
        }
        for (int i = 10; i < hours.length; i++) {
            hours[i] = new DayItemData.Builder()
                    .withHour(String.format("%d:00", i))
                    .withIndex(i)
                    .withIsAvailable(false)
                    .build();
        }
        events = new HashMap<>();
    }

    private DayOfWeek getDayOfWeek(CalendarDay day) {
        return LocalDate.of(day.getYear(), day.getMonth() + 1, day.getDay()).getDayOfWeek();
    }

    private CalendarDay nextDay(CalendarDay day) {
        LocalDate nextDay = LocalDate.of(day.getYear(), day.getMonth() + 1, day.getDay());
        nextDay = nextDay.plusDays(1);
        return CalendarDay.from(nextDay.getYear(),
                nextDay.getMonth().getValue() - 1, nextDay.getDayOfMonth());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendar = root.findViewById(R.id.calendar_view);
        calendar.state().edit()
                .setMinimumDate(CalendarDay.today())
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();
        CalendarDay next = CalendarDay.today();
        DayOfWeek dof = getDayOfWeek(next);
        while (dof == DayOfWeek.SUNDAY || dof == DayOfWeek.SATURDAY) {
            next = nextDay(next);
            dof = getDayOfWeek(next);
        }
        calendar.setSelectedDate(next);
        calendar.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                DayOfWeek dof = getDayOfWeek(day);
                return dof == DayOfWeek.SUNDAY || dof == DayOfWeek.SATURDAY;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setDaysDisabled(true);
            }
        });
        calendar.setOnDateChangedListener((widget, date, selected) -> {
            reload();
        });

        dayView = root.findViewById(R.id.day_view);
        dayView.addItemDecoration(new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL));
        dayView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dayView.setHasFixedSize(true);
        dayViewAdapter = new RecyclerView.Adapter<DayItem>() {
            @NonNull
            @Override
            public DayItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new view, which defines the UI of the list item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_calendar_day_item, parent, false);
                return new DayItem(view);
            }

            @Override
            public void onBindViewHolder(@NonNull DayItem holder, int position) {
                holder.setHour(hours[position].getHour());
                if (hours[position].isAvailable()) {
                    holder.setAvailable();
                } else {
                    holder.setUnavailable();
                }
                if (hours[position].getEvent() != null) {
                    holder.setEvent(hours[position].getEvent(), onEventClickListener);
                }
            }

            @Override
            public int getItemCount() {
                return hours.length;
            }
        };
        dayView.setAdapter(dayViewAdapter);
        reload();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollToHour(7);
    }

    public void setAvailability(int startHour, int endHour) {
        for (int i = 0; i < hours.length; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(true)
                    .build();
        }
        for (int i = 0; i < startHour; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(false)
                    .build();
        }
        for (int i = endHour; i < hours.length; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(false)
                    .build();
        }
        reload();
    }

    public void scrollToHour(int hour) {
        dayView.scrollToPosition(hour);
    }

    public void addEvents(List<Event> events) {
        for (Event event : events) {
            List<Event> listOfEvents = this.events.getOrDefault(event.getDay(), new ArrayList<>());
            listOfEvents.add(event);
            this.events.put(event.getDay(), listOfEvents);
        }
        reload();
    }

    public void addDefaultDayEvent(Event event) {
        defaultEvent = event;
        reload();
    }

    public void addOnEventClickListener(OnClickListener listener) {
        onEventClickListener = listener;
    }

    public void reload() {
        if (calendar == null || dayViewAdapter == null) {
            return;
        }
        List<Event> listOfEvents = events.getOrDefault(calendar.getSelectedDate(),
                new ArrayList<>());
        for (int i = 0; i < hours.length; i++) {
            DayItemData.Builder builder = new DayItemData.Builder(hours[i])
                    .withEvent(null);
            if (hours[i].isAvailable()) {
                if (defaultEvent != null) {
                    builder.withEvent(new Event(calendar.getSelectedDate(),
                            i, defaultEvent.getName()));
                }
            }
            hours[i] = builder.build();
        }
        for (Event e : listOfEvents) {
            hours[e.getHour()] = new DayItemData.Builder(hours[e.getHour()])
                    .withEvent(e)
                    .build();
            dayViewAdapter.notifyItemChanged(e.getHour());
        }
        dayViewAdapter.notifyDataSetChanged();
    }
}