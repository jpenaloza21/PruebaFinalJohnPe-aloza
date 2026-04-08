package uta.edu.ec.addressbook;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements ContactsFragment.ContactsFragmentListener,
        DetailFragment.DetailFragmentListener,
        AddEditFragment.AddEditFragmentListener {
    public static final String CONTACT_URI = "contact_uri";

    private ContactsFragment contactsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            contactsFragment = new ContactsFragment();
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, contactsFragment);
            transaction.commit();
        } else {
            contactsFragment = (ContactsFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R.id.fragmentContainer);
        }
    }

    @Override
    public void onContactSelected(Uri contactUri) {
        displayContact(contactUri, R.id.fragmentContainer);
    }

    @Override
    public void onAddContact() {
        displayAddEditFragment(R.id.fragmentContainer, null);
    }

    private void displayContact(Uri contactUri, int viewID) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(CONTACT_URI, contactUri);
        detailFragment.setArguments(arguments);

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayAddEditFragment(int viewID, Uri contactUri) {
        AddEditFragment addEditFragment = new AddEditFragment();

        if (contactUri != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(CONTACT_URI, contactUri);
            addEditFragment.setArguments(arguments);
        }

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onContactDeleted() {
        getSupportFragmentManager().popBackStack();
        if (contactsFragment != null)
            contactsFragment.updateContactList();
    }

    @Override
    public void onEditContact(Uri contactUri) {
        displayAddEditFragment(R.id.fragmentContainer, contactUri);
    }

    @Override
    public void onAddEditCompleted(Uri contactUri) {
        getSupportFragmentManager().popBackStack();
        if (contactsFragment != null)
            contactsFragment.updateContactList();
    }
}