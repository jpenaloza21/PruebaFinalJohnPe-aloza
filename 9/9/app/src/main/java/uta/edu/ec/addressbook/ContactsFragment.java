package uta.edu.ec.addressbook;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import uta.edu.ec.addressbook.data.DatabaseDescription.Contact;

public class ContactsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface ContactsFragmentListener {
        void onContactSelected(Uri contactUri);
        void onAddContact();
    }

    private static final int CONTACTS_LOADER = 0;

    private ContactsFragmentListener listener;
    private ContactsAdapter contactsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(
                R.layout.fragment_contacts, container, false);

        RecyclerView recyclerView =
                (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity().getBaseContext()));

        contactsAdapter = new ContactsAdapter(
                new ContactsAdapter.ContactClickListener() {
                    @Override
                    public void onClick(Uri contactUri) {
                        listener.onContactSelected(contactUri);
                    }
                }
        );

        recyclerView.setAdapter(contactsAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getContext()));
        recyclerView.setHasFixedSize(true);

        FloatingActionButton addButton =
                (FloatingActionButton) view.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAddContact();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ContactsFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoaderManager.getInstance(this)
                .initLoader(CONTACTS_LOADER, null, this);
    }

    public void updateContactList() {
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CONTACTS_LOADER:
                return new CursorLoader(getActivity(),
                        Contact.CONTENT_URI,
                        null, null, null,
                        Contact.COLUMN_NAME + " COLLATE NOCASE ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        contactsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contactsAdapter.swapCursor(null);
    }
}