package com.flitzen.crm.activity;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flitzen.crm.R;
import com.flitzen.crm.fragment.DashboardFragment;
import com.flitzen.crm.fragment.EnquiryFragment;
import com.flitzen.crm.fragment.OrdersFragment;
import com.flitzen.crm.fragment.QuotationFragment;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActionBarDrawerToggle drawerToggle;
    public Fragment currentFragment;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.txtDashBoard)
    TextView txtDashBoard;
    @BindView(R.id.txtEnquiry)
    TextView txtEnquiry;
    @BindView(R.id.txtQuotations)
    TextView txtQuotations;
    @BindView(R.id.txtOrders)
    TextView txtOrders;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);

        drawerToggle = setupDrawerToggle();

        txtDashBoard.setOnClickListener(this);
        txtEnquiry.setOnClickListener(this);
        txtQuotations.setOnClickListener(this);
        txtOrders.setOnClickListener(this);

        currentFragment=new DashboardFragment();
        loadFragment(currentFragment,getResources().getString(R.string.dashboard));
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
        overridePendingTransition(0, 0);
    }

    private void loadFragment(Fragment fragment, String fragmentName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        if (currentFragment instanceof DashboardFragment) {

        } else {
            fragmentTransaction.addToBackStack(fragmentName);
        }
        fragmentTransaction.commit();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtDashBoard:
                currentFragment=new DashboardFragment();
                loadFragment(currentFragment,getResources().getString(R.string.dashboard));
                break;

            case R.id.txtEnquiry:
                currentFragment=new EnquiryFragment();
                loadFragment(currentFragment,getResources().getString(R.string.enquiry));
                break;

            case R.id.txtQuotations:
                currentFragment=new QuotationFragment();
                loadFragment(currentFragment,getResources().getString(R.string.quotations));
                break;

            case R.id.txtOrders:
                currentFragment=new OrdersFragment();
                loadFragment(currentFragment,getResources().getString(R.string.orders));
                break;
        }
    }
}