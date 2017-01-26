package net.yepsoftware.takemymoney.activities.menu.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.activities.AuthenticationActivity;
import net.yepsoftware.takemymoney.activities.ContactInfoActivity;
import net.yepsoftware.takemymoney.activities.RegistrationActivity;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private ListView listView;
    private ArrayList<String> settingsList;
    private TextView label;
    private Button button;
    private TextView label2;
    private Button button2;
    private Switch autoLoginSwitch;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        listView = (ListView) view.findViewById(R.id.listView);
        label = (TextView)view.findViewById(R.id.label);
        button = (Button) view.findViewById(R.id.button);
        label2 = (TextView)view.findViewById(R.id.label2);
        button2 = (Button) view.findViewById(R.id.button2);
        autoLoginSwitch = (Switch) view.findViewById(R.id.autoLoginSwitch);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSettingsInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        customizeLayout();
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSettingsInteraction(Uri uri);
    }

    private void customizeLayout(){
        switch (PreferencesHelper.getAppState(getActivity())){
            case PreferencesHelper.APP_STATE_AUTHENTICATED:
                setLayoutAuthenticatedState();
                break;
            case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                setLayoutUnauthenticatedState();
                break;
            case PreferencesHelper.APP_STATE_UNREGISTERED:
                setLayoutUnregisteredState();
                break;
        }
    }

    private void setLayoutAuthenticatedState(){
        label.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        label2.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        autoLoginSwitch.setVisibility(View.VISIBLE);
        settingsList = new ArrayList<>();
        settingsList.add("My profile");
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, settingsList);
        listView.setAdapter(arrayAdapter);
        autoLoginSwitch.setChecked(PreferencesHelper.isAutoLogin(getActivity()));
        autoLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesHelper.setAutoLogin(getActivity(), isChecked);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent = new Intent(getActivity(), ContactInfoActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void setLayoutUnauthenticatedState(){
        label2.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        autoLoginSwitch.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        label.setText("You need to Sign In in order to change settings");
        button.setText("Sign In");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setLayoutUnregisteredState(){
        listView.setVisibility(View.GONE);
        autoLoginSwitch.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        label2.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        label.setText("You need to Register in order to access settings");
        button.setText("Register");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }
}
