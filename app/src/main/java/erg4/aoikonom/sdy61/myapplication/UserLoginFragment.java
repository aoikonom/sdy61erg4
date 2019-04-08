package erg4.aoikonom.sdy61.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserLoginFragment extends Fragment {


    public UserLoginFragment() {
        // Required empty public constructor
    }

    public static UserLoginFragment newInstance() {
        UserLoginFragment fragment = new UserLoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.user_login_fragment, container, false);

        return rootView;
    }
}
