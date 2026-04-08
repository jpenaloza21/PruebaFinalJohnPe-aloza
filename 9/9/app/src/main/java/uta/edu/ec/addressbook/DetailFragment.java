package uta.edu.ec.addressbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import uta.edu.ec.addressbook.data.DatabaseDescription.Contact;
import android.content.ContentValues;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;

public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteListener {

    public interface DetailFragmentListener {
        void onContactDeleted();
        void onEditContact(Uri contactUri);
    }



    public static class ConfirmDeleteFragment extends DialogFragment {
        private DeleteListener deleteListener;
        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Fragment parent = getParentFragment();
            if (parent != null && parent instanceof DeleteListener)
                deleteListener = (DeleteListener) parent;
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            builder.setPositiveButton(R.string.button_delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            if (deleteListener != null)
                                deleteListener.onDeleteConfirmed();
                        }
                    }
            );

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }
    }

    private static final int CONTACT_LOADER = 0;

    private DetailFragmentListener listener;
    private Uri contactUri;

    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private TextView streetTextView;
    private TextView cityTextView;
    private TextView stateTextView;
    private TextView zipTextView;
    private ContentValues deletedContactValues;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DetailFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        if (arguments != null)
            contactUri =
                    arguments.getParcelable(MainActivity.CONTACT_URI);

        View view = inflater.inflate(
                R.layout.fragment_detail, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        streetTextView = view.findViewById(R.id.streetTextView);
        cityTextView = view.findViewById(R.id.cityTextView);
        stateTextView = view.findViewById(R.id.stateTextView);
        zipTextView = view.findViewById(R.id.zipTextView);

        LoaderManager.getInstance(this)
                .initLoader(CONTACT_LOADER, null, this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            listener.onEditContact(contactUri);
            return true;
        } else if (id == R.id.action_delete) {
            deleteContact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteContact() {
        ConfirmDeleteFragment dialog = new ConfirmDeleteFragment();
        dialog.show(getChildFragmentManager(), "confirm delete");
    }


    @Override
    public void onDeleteConfirmed() {
        if (getActivity() == null || contactUri == null) return;


        final android.content.ContentResolver resolver =
                getActivity().getContentResolver();


        android.database.Cursor cursor = resolver.query(
                contactUri, null, null, null, null);

        deletedContactValues = new ContentValues();
        if (cursor != null && cursor.moveToFirst()) {
            deletedContactValues.put(Contact.COLUMN_NAME,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_NAME)));
            deletedContactValues.put(Contact.COLUMN_PHONE,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_PHONE)));
            deletedContactValues.put(Contact.COLUMN_EMAIL,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_EMAIL)));
            deletedContactValues.put(Contact.COLUMN_STREET,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_STREET)));
            deletedContactValues.put(Contact.COLUMN_CITY,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_CITY)));
            deletedContactValues.put(Contact.COLUMN_STATE,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_STATE)));
            deletedContactValues.put(Contact.COLUMN_ZIP,
                    cursor.getString(cursor.getColumnIndexOrThrow(Contact.COLUMN_ZIP)));
            cursor.close();
        }


        resolver.delete(contactUri, null, null);


        final ContentValues savedValues = deletedContactValues;
        final CoordinatorLayout coordinatorLayout =
                getActivity().findViewById(R.id.coordinatorLayout);


        if (listener != null)
            listener.onContactDeleted();


        Snackbar.make(coordinatorLayout,
                        R.string.contact_deleted,
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Usar resolver guardado, no getActivity()
                        resolver.insert(Contact.CONTENT_URI, savedValues);
                    }
                }).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CONTACT_LOADER:
                return new CursorLoader(getActivity(),
                        contactUri, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int nameIndex = data.getColumnIndex(Contact.COLUMN_NAME);
            int phoneIndex = data.getColumnIndex(Contact.COLUMN_PHONE);
            int emailIndex = data.getColumnIndex(Contact.COLUMN_EMAIL);
            int streetIndex = data.getColumnIndex(Contact.COLUMN_STREET);
            int cityIndex = data.getColumnIndex(Contact.COLUMN_CITY);
            int stateIndex = data.getColumnIndex(Contact.COLUMN_STATE);
            int zipIndex = data.getColumnIndex(Contact.COLUMN_ZIP);

            nameTextView.setText(data.getString(nameIndex));
            phoneTextView.setText(data.getString(phoneIndex));
            emailTextView.setText(data.getString(emailIndex));
            streetTextView.setText(data.getString(streetIndex));
            cityTextView.setText(data.getString(cityIndex));
            stateTextView.setText(data.getString(stateIndex));
            zipTextView.setText(data.getString(zipIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}