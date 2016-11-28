package org.globalappinitiative.wtbutest;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.globalappinitiative.wtbutest.request.RequestDelegate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int position;

    private Spinner spinner;

    private ListView[] lists;

    private ArrayList<ArrayList<ScheduleItem>> schedule;

    private OnFragmentInteractionListener mListener;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeUI();
    }

    protected void initializeUI() {
        schedule = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ArrayList<ScheduleItem> innerList = new ArrayList<ScheduleItem>();
            schedule.add(innerList);
        }

        lists = new ListView[7];
        lists[0] = (ListView) getView().findViewById(R.id.day_list_monday);
        lists[1] = (ListView) getView().findViewById(R.id.day_list_tuesday);
        lists[2] = (ListView) getView().findViewById(R.id.day_list_wednesday);
        lists[3] = (ListView) getView().findViewById(R.id.day_list_thursday);
        lists[4] = (ListView) getView().findViewById(R.id.day_list_friday);
        lists[5] = (ListView) getView().findViewById(R.id.day_list_saturday);
        lists[6] = (ListView) getView().findViewById(R.id.day_list_sunday);

        getSchedule();
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

        lists[0].setVisibility(View.VISIBLE);
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
            hour.setText(Integer.toString(show.getShowTime()));
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
