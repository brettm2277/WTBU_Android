package org.globalappinitiative.wtbu;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.globalappinitiative.wtbu.request.RequestDelegate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ScheduleFragment extends Fragment
    implements AdapterView.OnItemSelectedListener {

    private int position;

    private Spinner spinner;

    private ListView[] lists;

    private ArrayList<ArrayList<ScheduleItem>> schedule;

    private OnFragmentInteractionListener mListener;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lists = new ListView[7];

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeUI();
    }

    protected void initializeUI() {
        lists[0] = (ListView) getView().findViewById(R.id.day_list_monday);
        lists[1] = (ListView) getView().findViewById(R.id.day_list_tuesday);
        lists[2] = (ListView) getView().findViewById(R.id.day_list_wednesday);
        lists[3] = (ListView) getView().findViewById(R.id.day_list_thursday);
        lists[4] = (ListView) getView().findViewById(R.id.day_list_friday);
        lists[5] = (ListView) getView().findViewById(R.id.day_list_saturday);
        lists[6] = (ListView) getView().findViewById(R.id.day_list_sunday);

        schedule = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            ArrayList<ScheduleItem> innerList = new ArrayList<ScheduleItem>();
            schedule.add(innerList);
        }

        getSchedule();
    }

    private void getSchedule() {
        String url = "https://gaiwtbubackend.herokuapp.com/regularShowsInfo?SongID=1234";
        AppVolleyState.sobjectRequest(url, null,
            new RequestDelegate<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                         // Get the results from the response JSON
                         JSONArray resultsJSON = response.getJSONArray("results");
                         // Now iterate through the results and add them to the programming list
                         for (int i = 0; i < resultsJSON.length(); i++) {
                             JSONObject result = resultsJSON.getJSONObject(i);
                             // Get the show name from the result json
                             String showName = result.getString("ShowName");
                             // Get the show time as well. Stored in the JSON in the form: hr:mim:sec, just need to convert the hours (first two characters) to an integer
                             int showTime = Integer.parseInt(result.getString("OnairTime").substring(0, 2)); // <--- Why is the end index exclusive? Is this a standard Java thing?
                             // If the showtime is at midnight (0 hours), set it to 24 for the purposes of sorting
                             // The same show can occur multiple times per week, so be sure to add all of them. Begin by getting the JSON array of weekdays
                             JSONArray weekdays = result.getJSONArray("Weekdays");
                             // Now for each entry in weekdays:
                             for (int j = 0; j < weekdays.length(); j++) {
                                 String weekday = weekdays.getString(j);
                                 // Now finally construct the ScheduleItem from the parsed data
                                 ScheduleItem program = new ScheduleItem(weekday, showTime, showName);
                                 schedule.get(program.getDayOfWeek()).add(program);
                             }
                         }
                         // Sort the shows for each day
                         for (int i = 0; i < 7; i++) {
                            Collections.sort(schedule.get(i));
                         }
                         populateViews();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast errorMsg = Toast.makeText(getActivity(), "Error loading schedule data. Please check connection", Toast.LENGTH_SHORT);
                    errorMsg.show();
                }
            });
    }

    private void populateViews() {
        for (int i = 0; i < 7; i++) {
            lists[i].setAdapter(new ScheduleFragment.customListAdapter(getActivity(), schedule.get(i)));
            lists[i].setVisibility(View.INVISIBLE);
            final int ind = i;
            lists[i].setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    ImageView starImage = (ImageView) v.findViewById(R.id.schedule_entry_star);
                    ScheduleItem listElement = (ScheduleItem) lists[ind].getItemAtPosition(position);
                    if (((MyApplication) ScheduleFragment.this.getActivity().getApplication()).checkFavorite(ScheduleFragment.this.position, listElement.getShowTime())) {
                        ((MyApplication) ScheduleFragment.this.getActivity().getApplication()).removeFavorite(ScheduleFragment.this.position, listElement.getShowTime());
                        starImage.setImageResource(R.drawable.star_empty);
                    } else {
                        starImage.setImageResource(R.drawable.star_full);
                        ((MyApplication) ScheduleFragment.this.getActivity().getApplication()).addFavorite(ScheduleFragment.this.position, listElement.getShowTime());
                    }
                }
            });
        }

        int dayOfWeekIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;
        lists[dayOfWeekIndex].setVisibility(View.VISIBLE);

    }

    public void setupSpinner(Spinner spinner) {
        String[] items = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_text);

        //assign adapter to the Spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);  //sunday = 1, saturday = 7
        int hour = c.get(Calendar.HOUR_OF_DAY);
        spinner.setSelection(day - 1);
        position = day - 1;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        // Hide all the scrollViews
        for (int i = 0; i < 7; i++) {
            lists[i].setVisibility(View.INVISIBLE);
        }
        // Show the one that corresponds to the position (which for some reason does not begin at zero)
        lists[position].setVisibility(View.VISIBLE);
        this.position = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.schedule_menu, menu);
        MenuItem mitem = menu.findItem(R.id.item1);
        spinner = (Spinner) mitem.getActionView();
        setupSpinner(spinner);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    private class customListAdapter extends BaseAdapter {

        private List<ScheduleItem> listElements;
        private LayoutInflater inflater;

        public customListAdapter(Context context, ArrayList<ScheduleItem> singleDaySchedule) {
            this.listElements = singleDaySchedule;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return listElements.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return listElements.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.schedule_entry, parent, false);
            }

            ScheduleItem show = (ScheduleItem) getItem(position);
            TextView hour = (TextView) convertView.findViewById(R.id.schedule_entry_hour);
            TextView name = (TextView) convertView.findViewById(R.id.schedule_entry_text);
            if (DateFormat.is24HourFormat(getContext())) {
                hour.setText(Integer.toString(show.getShowTime()));
            } else {
                hour.setText(show.getFullShowTime());
            }
            name.setText(show.getTitle());

            ImageView starImage = (ImageView) convertView.findViewById(R.id.schedule_entry_star);

            if (((MyApplication) ScheduleFragment.this.getActivity().getApplication()).checkFavorite(ScheduleFragment.this.position, show.getShowTime())) {
                starImage.setImageResource(R.drawable.star_full);
            } else {
                starImage.setImageResource(R.drawable.star_empty);
            }
            return convertView;
        }
    }
}
