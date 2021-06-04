package com.example.intershiptest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class ReposDialogFragment extends DialogFragment {

    ListView reposList;

    String repositories;

    String[] reposArray;
    ArrayList<String> reposArrayList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

                repositories = getArguments().getString("repositories");
                String[] words = repositories.split(".SPLIT.");
                for (String word : words) {
                        reposArrayList.add(word);
                }
        reposArray = new String[reposArrayList.size()];
        for (int i = 0; i < reposArrayList.size(); i++) {
            reposArray[i] = reposArrayList.get(i);
        }
        View v = inflater.inflate(R.layout.repos_view, null);
        reposList = v.findViewById(R.id.reposList);

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, reposArray);
                        adapter.notifyDataSetChanged();
                        reposArray = null;
                        reposArrayList.clear();
                        reposList.setAdapter(adapter);







        return v;
    }

}
