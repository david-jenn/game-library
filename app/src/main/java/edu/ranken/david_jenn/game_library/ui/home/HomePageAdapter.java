package edu.ranken.david_jenn.game_library.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.ranken.david_jenn.game_library.ui.game.GameListFragment;
import edu.ranken.david_jenn.game_library.ui.user.UserListFragment;

public class HomePageAdapter extends FragmentStateAdapter {

    public HomePageAdapter(FragmentActivity activity) { super(activity); }
    public HomePageAdapter(Fragment fragment) { super(fragment); }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new GameListFragment();
            case 1: return new UserListFragment();
            default: throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
