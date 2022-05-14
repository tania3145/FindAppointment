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
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

public class CalendarFragment extends Fragment {

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

            public Builder withHasEvent(boolean hasEvent) {
                item.hasEvent = hasEvent;
                return this;
            }

            public DayItemData build() {
                return item;
            }
        }

        private String hour;
        private int index;
        private boolean isAvailable;
        private boolean hasEvent;

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

        public boolean isHasEvent() {
            return hasEvent;
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
        }

        public void setAvailable() {
            eventLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        public void addEvent() {

        }
    }

    private RecyclerView dayView;
    private RecyclerView.Adapter<DayItem> dayViewAdapter;
    private DayItemData[] hours;
    private MaterialCalendarView calendar;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendar = root.findViewById(R.id.calendar_view);

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
            }

            @Override
            public int getItemCount() {
                return hours.length;
            }
        };
        dayView.setAdapter(dayViewAdapter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollToHour(7);
        setAvailability(9, 17);
    }

    public void setAvailability(int startHour, int endHour) {
        // calendar.;

        for (int i = 0; i < hours.length; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(true)
                    .build();
        }
        for (int i = 0; i < startHour; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(false)
                    .build();
            dayViewAdapter.notifyItemChanged(i);
        }
        for (int i = endHour; i < hours.length; i++) {
            hours[i] = new DayItemData.Builder(hours[i])
                    .withIsAvailable(false)
                    .build();
            dayViewAdapter.notifyItemChanged(i);
        }
    }

    public void scrollToHour(int hour) {
        dayView.scrollToPosition(hour);
    }

    public void addEvent() {
        System.out.println("Added event");
    }
}