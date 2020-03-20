/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timings;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import in.co.rajkumaar.amritarepo.timings.fragments.PublicTransportFragment;
import in.co.rajkumaar.amritarepo.timings.fragments.ShuttleBusFragment;

public class TransportCategoryAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public TransportCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ShuttleBusFragment();
        } else {
            return new PublicTransportFragment();

        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Shuttle Buses";
        } else {
            return "Public Transport";
        }
    }
}