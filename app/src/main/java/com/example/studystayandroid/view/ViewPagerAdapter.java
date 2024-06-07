package com.example.studystayandroid.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studystayandroid.model.User;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private User currentUser;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, User user) {
        super(fragmentActivity);
        this.currentUser = user;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return RentedFragment.newInstance(currentUser);
            case 1:
                return ListedFragment.newInstance(currentUser);
            default:
                return RentedFragment.newInstance(currentUser);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
