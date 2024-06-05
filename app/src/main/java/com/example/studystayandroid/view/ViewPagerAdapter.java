package com.example.studystayandroid.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studystayandroid.model.User;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private User currentUser;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RentedFragment();
            case 1:
                return new ListedFragment();
            default:
                return new RentedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}